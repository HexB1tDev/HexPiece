package com.hexhaki.piece.server;

import com.hexhaki.piece.haki.ScarLedger;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

/**
 * 2.2.5: temporary, self-repairing terrain destruction, taken from HexGhoul (its {@code TerrainScars}; the owner: "then repair themselves
 * automaticly (same as HexGhoul)"): the Conqueror's fissures ({@link Fissures}) tear blocks out through here.
 *
 * <p>Blocks are taken out and put back without neighbour updates, so sand does not fall, water does not flow in and what hangs on a
 * block does not pop off while the hole is open. Nothing is ever dropped: anything with a block entity (chests, furnaces, beds,
 * spawners ...), fluids, unbreakable blocks, the walls behind paintings and item frames, and redstone or multi-block parts that react
 * to their own placement are left alone; so is every block the player may not break (spawn protection; and, HexPiece's addition, every
 * block Forge's break event refuses them: claims and protection mods, as the Haki slash asks). Each comes back 15 to 25 seconds later,
 * the lowest first (sand and gravel never fall into a hole about to close), later while a creature stands in it, never over a player's
 * head, never over a block a player has put there since; everything is put back when the server stops.
 */
public final class Scars {
    /** Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE: the clients told, no neighbour updates. */
    static final int SILENT = 2 | 16;
    // 2.4.5 (the full release's lag): at most 240 blocks torn and 200 grown back a tick (were 700 and 900): each is a light update and
    // a chunk rebuild on every client near, and a release's fissures and their regrowth came down in a handful of ticks
    static final int DESTROY_BUDGET = 240;
    static final int CHECK_BUDGET = 12_000;
    static final int RESTORE_BUDGET = 200;
    static final long RESTORE_MIN = 300L;
    static final long RESTORE_JITTER = 200L;
    static final int BLOCK_BREAK = 2001;
    /** A 4x4 painting's farthest support block is about 2.1 blocks from its centre. */
    static final double HANGING_CLEARANCE = 2.6;
    private static final Map<ServerLevel, Scars> LEVELS = new IdentityHashMap<>();

    private final ServerLevel level;
    private final ScarLedger<BlockState> ledger = new ScarLedger<>();
    private long budgetTick = Long.MIN_VALUE;
    private int destroyed;
    private int checked;
    private int effects;

    private Scars(ServerLevel level) {
        this.level = level;
    }

    static Scars of(ServerLevel level) {
        return LEVELS.computeIfAbsent(level, Scars::new);
    }

    static void tickAll() {
        LEVELS.values().removeIf(scars -> {
            scars.restoreDue();
            return scars.ledger.size() == 0;
        });
    }

    /** Puts everything back now: the server is stopping. */
    static void restoreAll() {
        for (Scars scars : LEVELS.values()) {
            scars.restoreEverything();
        }
        LEVELS.clear();
    }

    int pending() {
        return this.ledger.size();
    }

    private void refreshBudget() {
        long now = this.level.m_46467_();
        if (now != this.budgetTick) {
            this.budgetTick = now;
            this.destroyed = 0;
            this.checked = 0;
            this.effects = 0;
        }
    }

    /** Whether this tick's budgets still allow more tearing. */
    boolean room() {
        this.refreshBudget();
        return this.destroyed < DESTROY_BUDGET && this.checked < CHECK_BUDGET && !this.ledger.full();
    }

    /**
     * Tears {@code pos} out if it may be (see the class); {@code supports}: the positions of paintings and frames near it. 2.3.3: an
     * owner that is no player (a sea fighter's Haki slash, allowed by the mobGriefing rule) has no claims or spawn protection asked.
     */
    boolean carve(LivingEntity owner, BlockPos pos, List<double[]> supports) {
        return this.tear(owner, pos, supports) == TORN;
    }

    /** What {@link #tear} did: tore the block out; left it (air, a fluid, a block entity, a painting's wall ...); was refused it. */
    static final int TORN = 1;
    static final int LEFT = 0;
    static final int REFUSED = -1;

    /**
     * 2.3.3: {@link #carve}, saying why not: {@link #REFUSED} for a block nothing may tear out (unbreakable, protected, beyond the
     * world), which stops a slash's row there (nothing behind it is cut), {@link #LEFT} for one it passes.
     */
    int tear(LivingEntity owner, BlockPos pos, List<double[]> supports) {
        return this.tear(owner, pos, supports, -1L);
    }

    /**
     * 2.7.8 (the owner, on the Blast Breath beam: "it should also destroy blocks and self repair after 30 seconds"): {@link #tear},
     * the block grown back {@code restoreIn} ticks later (and a few more, so a crater heals as a ripple), or 15 to 25 seconds when
     * negative.
     */
    int tear(LivingEntity owner, BlockPos pos, List<double[]> supports, long restoreIn) {
        this.refreshBudget();
        long key = pos.m_121878_();
        if (this.ledger.contains(key)) {
            return LEFT;
        }
        this.checked++;
        for (double[] support : supports) {
            double dx = pos.m_123341_() + 0.5 - support[0];
            double dy = pos.m_123342_() + 0.5 - support[1];
            double dz = pos.m_123343_() + 0.5 - support[2];
            if (dx * dx + dy * dy + dz * dz < HANGING_CLEARANCE * HANGING_CLEARANCE) {
                return LEFT;
            }
        }
        ServerPlayer player = owner instanceof ServerPlayer p ? p : null;
        if (pos.m_123342_() <= this.level.m_141937_() || pos.m_123342_() >= this.level.m_151558_() - 1 || !this.level.m_46805_(pos)
            || player != null && !this.level.m_7966_(player, pos)) {
            return REFUSED;
        }
        BlockState state = this.level.m_8055_(pos);
        if (state.m_60795_() || !state.m_60819_().m_76178_()) {
            return LEFT;
        }
        if (state.m_60800_(this.level, pos) < 0.0F) {
            return REFUSED;
        }
        if (!this.carvable(state, pos)) {
            return LEFT;
        }
        if (player != null) {
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(this.level, pos, state, player);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                return REFUSED; // claimed, protected, or otherwise not theirs to break
            }
        }
        long now = this.level.m_46467_();
        long restoreAt = restoreIn >= 0L ? now + restoreIn + Math.floorMod(key * 0x9E3779B97F4A7C15L, 40L)
            : now + RESTORE_MIN + Math.floorMod(key * 0x9E3779B97F4A7C15L, RESTORE_JITTER);
        if (!this.ledger.record(key, state, restoreAt)) {
            return LEFT;
        }
        if (this.effects < 24 && Math.floorMod(key, 7L) == 0L) {
            this.effects++;
            this.level.m_46796_(BLOCK_BREAK, pos, Block.m_49956_(state));
        }
        this.level.m_7731_(pos, Blocks.f_50016_.m_49966_(), SILENT);
        this.destroyed++;
        return TORN;
    }

    private boolean carvable(BlockState state, BlockPos pos) {
        if (state.m_60795_() || !state.m_60819_().m_76178_() || state.m_155947_()) {
            return false;
        }
        if (state.m_60800_(this.level, pos) < 0.0F) {
            return false;
        }
        Block block = state.m_60734_();
        return !(block instanceof TntBlock || block instanceof BaseRailBlock || block instanceof RedStoneWireBlock || block instanceof DiodeBlock
            || block instanceof PistonBaseBlock || block instanceof PistonHeadBlock || block instanceof ObserverBlock || block instanceof LeverBlock
            || block instanceof ButtonBlock || block instanceof BasePressurePlateBlock || block instanceof RedstoneTorchBlock
            || block instanceof DoorBlock || block instanceof NetherPortalBlock || block instanceof ScaffoldingBlock);
    }

    private void restoreDue() {
        long now = this.level.m_46467_();
        int restored = 0;
        ScarLedger.Scar<BlockState> scar;
        while (restored < RESTORE_BUDGET && (scar = this.ledger.poll(now)) != null) {
            BlockPos pos = BlockPos.m_122022_(scar.position);
            if (!this.level.m_46805_(pos)) {
                this.ledger.defer(scar, now + 100L, false);
                continue;
            }
            BlockState current = this.level.m_8055_(pos);
            if (!replaceable(current)) {
                // someone built here since: their block stays
                this.ledger.finish(scar);
                continue;
            }
            if (scar.original.m_60734_() instanceof FallingBlock && this.ledger.contains(pos.m_7495_().m_121878_())) {
                // its support first, so sand and gravel never fall into a hole about to close
                this.ledger.defer(scar, now + 2L, false);
                continue;
            }
            List<LivingEntity> inside = this.level.m_6443_(LivingEntity.class,
                new AABB(pos.m_123341_(), pos.m_123342_(), pos.m_123343_(), pos.m_123341_() + 1.0, pos.m_123342_() + 1.0, pos.m_123343_() + 1.0),
                LivingEntity::m_6084_);
            // never seal a ceiling over a player's head either
            boolean player = !this.level.m_6443_(Player.class, new AABB(pos.m_123341_() - 0.3, pos.m_123342_() - 1.5, pos.m_123343_() - 0.3,
                pos.m_123341_() + 1.3, pos.m_123342_() + 1.0, pos.m_123343_() + 1.3), Player::m_6084_).isEmpty();
            if (player || !inside.isEmpty() && scar.retries() < 15) {
                this.ledger.defer(scar, now + 20L, true);
                continue;
            }
            this.level.m_7731_(pos, scar.original, SILENT);
            this.ledger.finish(scar);
            restored++;
        }
    }

    private void restoreEverything() {
        List<ScarLedger.Scar<BlockState>> all = new ArrayList<>();
        for (ScarLedger.Scar<BlockState> scar : this.ledger.all()) {
            all.add(scar);
        }
        all.sort((a, b) -> Integer.compare(BlockPos.m_122022_(a.position).m_123342_(), BlockPos.m_122022_(b.position).m_123342_()));
        for (ScarLedger.Scar<BlockState> scar : all) {
            BlockPos pos = BlockPos.m_122022_(scar.position);
            if (replaceable(this.level.m_8055_(pos))) {
                this.level.m_7731_(pos, scar.original, SILENT);
            }
        }
        this.ledger.clear();
    }

    private static boolean replaceable(BlockState current) {
        return current.m_60795_() || !current.m_60819_().m_76178_() || current.m_60734_() instanceof BaseFireBlock;
    }
}
