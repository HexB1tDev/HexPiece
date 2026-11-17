package com.hexhaki.piece.server;

import com.hexhaki.fx.Impact;
import com.hexhaki.piece.PieceConfig;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CSlash;
import com.hexhaki.piece.network.S2CSwordFx;
import com.hexhaki.piece.style.Hit;
import com.hexhaki.piece.sword.SwordRules;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.4: the Swordsman's Haki slash, flown on the server (no entity: every client flies the same slash from {@link S2CSlash}). The
 * owner: "With Haki active, it releases a travelling slash projectile ... The projectile must create a narrow vertical cutting plane
 * along its travel path, rather than an explosion or a large rectangular excavation. Charging increases the height of this plane and
 * the visible slash together. It can cut block types regardless of their normal hardness or required mining tool, including obsidian,
 * with bedrock remaining immune. Bedrock must stop the cut locally so blocks directly behind it are not removed through it. Make the
 * attack's visible shape, entity hit volume and removed blocks agree. Perform terrain destruction on the server, respecting world
 * boundaries, loaded chunks, block-break cancellation and existing protection rules. Handle block entities and drops correctly without
 * duplication. Pirate terrain destruction must also respect the server's mob-griefing settings. Bound both block checks and block
 * changes per tick, deduplicate affected positions and impose finite projectile range and lifetime."
 * <ul>
 *   <li>2.2.4: the wave's body ({@code SlashMesh}) is the plane's thickness: {@link SwordRules#slashThicknessAt} at each height (9 blocks
 *       in the ground at full charge, thinning to its peak); each row of the trench is that wide.</li>
 *   <li>The slash is a vertical plane {@code height} tall and {@link SwordRules#slashThickness} thick, standing up from a little under
 *       its owner's feet ({@link SwordRules#slashCentre}; 2.2.0: up to 64 blocks tall, flying up to 220), flying {@code speed} blocks a
 *       tick for {@code range} blocks; everything whose body that plane passes through is hit
 *       once. The clients draw exactly that plane (client/sword/SlashRenderer).</li>
 *   <li>Its blocks: the cells the plane passes through, one block thick, in rows a block apart up its height. Each cell is looked at
 *       once (deduplicated), in the order the plane reaches it; air and fluids are passed; any block the game can break at all is cut,
 *       whatever its hardness or tool (obsidian too); an unbreakable one (bedrock, a hardness below 0) stops its row there, as does a
 *       break a protection mod cancels ({@link BlockEvent.BreakEvent}, posted as the player who cut it), so nothing behind them in that
 *       row is cut; unloaded chunks and the world's height limits stop it too. A cut block is broken by the game itself
 *       ({@code destroyBlock}, with drops: a chest spills what it held once). Pirates cut terrain only while the mobGriefing rule
 *       allows it.</li>
 *   <li>At most {@link SwordRules#CUT_CHECKS_PER_TICK} cells looked at and {@link PieceConfig#CUT_BREAKS} blocks broken per slash per
 *       tick (the rest wait for the next tick), and four times that for all slashes together; a slash is gone {@link #LINGER} ticks
 *       after its flight at the latest.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class HakiSlashes {
    /** Ticks a slash may keep cutting after its flight (2.2.4: the wave's wider trench takes longer within the same budgets). */
    static final int LINGER = 120;
    private static final List<Slash> ACTIVE = new ArrayList<>();
    private static int nextId = 1;

    static final class Slash {
        int id;
        ServerLevel level;
        LivingEntity owner;
        Vec3 start;
        Vec3 dir;
        double height;
        double range;
        double travelled;
        /** 2.2.0: blocks a tick, the plane's thickness, its centre above the start, the chance a plain cut block drops. */
        double speed;
        double thick;
        double centre;
        double drops;
        float damage;
        int color;
        boolean finisher;
        boolean terrain;
        DamageSource source;
        Predicate<LivingEntity> foe;
        int age;
        final Set<Integer> hit = new HashSet<>();
        final Set<Long> seen = new HashSet<>();
        final ArrayDeque<long[]> pending = new ArrayDeque<>();
        boolean[] blocked;
    }

    private HakiSlashes() {
    }

    /** A slash sets off from {@code from} along {@code dir} (the aim at release). */
    public static void launch(LivingEntity owner, Vec3 from, Vec3 dir, double height, double range, double speed, float damage, int color,
                              boolean finisher, boolean terrain, DamageSource source, Predicate<LivingEntity> foe) {
        if (!(owner.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        double length = dir.m_82553_();
        Slash slash = new Slash();
        slash.id = nextId++;
        slash.level = level;
        slash.owner = owner;
        slash.start = from;
        slash.dir = length < 1.0E-6 ? StyleCombat.facing(owner) : dir.m_82490_(1.0 / length);
        slash.height = height;
        slash.range = range;
        slash.speed = Math.max(0.5, speed);
        slash.thick = SwordRules.slashThickness(height);
        slash.centre = SwordRules.slashCentre(height);
        slash.drops = SwordRules.dropChance(height);
        slash.damage = damage;
        slash.color = color;
        slash.finisher = finisher;
        boolean griefing = owner instanceof ServerPlayer || level.m_46469_().m_46207_(GameRules.f_46132_);
        slash.terrain = terrain && griefing;
        slash.source = source;
        slash.foe = foe;
        slash.blocked = new boolean[Math.max(1, (int) Math.ceil(height))];
        ACTIVE.add(slash);
        PieceNetwork.around(owner, message(slash, slash.range));
    }

    static S2CSlash message(Slash slash, double range) {
        return new S2CSlash(slash.id, slash.start.f_82479_, slash.start.f_82480_, slash.start.f_82481_, (float) slash.dir.f_82479_,
            (float) slash.dir.f_82480_, (float) slash.dir.f_82481_, (float) slash.height, (float) range, (float) slash.speed,
            slash.color, (int) ((slash.level.m_46467_() - slash.age) & 0x3FFFFFFF));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) {
            return;
        }
        int[] budget = {SwordRules.CUT_CHECKS_PER_TICK * 4, 4 * (int) Math.round(PieceConfig.get(PieceConfig.CUT_BREAKS,
            SwordRules.CUT_BREAKS_PER_TICK)) * 4};
        Iterator<Slash> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Slash slash = it.next();
            boolean done;
            try {
                done = step(slash, budget);
            } catch (RuntimeException failure) {
                done = true; // a slash that fails is dropped, never the server
            }
            if (done) {
                it.remove();
            }
        }
    }

    /** One tick of a slash; true when it is over. */
    static boolean step(Slash slash, int[] budget) {
        slash.age++;
        if (slash.owner.m_213877_() || slash.level != slash.owner.m_9236_() && !(slash.owner instanceof ServerPlayer)) {
            return true;
        }
        double before = slash.travelled;
        if (before < slash.range) {
            slash.travelled = Math.min(slash.range, before + slash.speed);
            Vec3 a = slash.start.m_82549_(slash.dir.m_82490_(before));
            Vec3 b = slash.start.m_82549_(slash.dir.m_82490_(slash.travelled));
            hitBodies(slash, a, b);
            if (slash.terrain) {
                queueCells(slash, a, b);
            }
        }
        if (slash.terrain) {
            cut(slash, budget);
        }
        boolean flown = slash.travelled >= slash.range;
        return flown && (slash.pending.isEmpty() || slash.age > slash.range / slash.speed + LINGER);
    }

    /** Everything the plane passed through between a and b, not hit yet. */
    static void hitBodies(Slash slash, Vec3 a, Vec3 b) {
        double half = slash.height / 2.0;
        double thick = slash.thick / 2.0;
        double up = slash.centre;
        AABB area = new AABB(Math.min(a.f_82479_, b.f_82479_), Math.min(a.f_82480_, b.f_82480_) + up - half, Math.min(a.f_82481_, b.f_82481_),
            Math.max(a.f_82479_, b.f_82479_), Math.max(a.f_82480_, b.f_82480_) + up + half, Math.max(a.f_82481_, b.f_82481_)).m_82400_(thick + 0.3);
        double length = b.m_82546_(a).m_82553_();
        int samples = Math.max(2, (int) Math.ceil(length / 0.3) + 1);
        for (LivingEntity other : slash.level.m_6443_(LivingEntity.class, area,
            e -> e != slash.owner && e.m_6084_() && !e.m_5833_() && !slash.hit.contains(e.m_19879_()) && slash.foe.test(e))) {
            AABB body = other.m_20191_();
            boolean inside = false;
            for (int k = 0; k < samples && !inside; k++) {
                Vec3 c = a.m_82549_(b.m_82546_(a).m_82490_(k / (double) (samples - 1)));
                inside = c.f_82479_ >= body.f_82288_ - thick && c.f_82479_ <= body.f_82291_ + thick
                    && c.f_82481_ >= body.f_82290_ - thick && c.f_82481_ <= body.f_82293_ + thick
                    && c.f_82480_ + up + half >= body.f_82289_ && c.f_82480_ + up - half <= body.f_82292_;
            }
            if (!inside) {
                continue;
            }
            slash.hit.add(other.m_19879_());
            Vec3 middle = StyleCombat.middle(other);
            if (slash.finisher && other.m_21223_() <= slash.damage && Finishers.start(slash.owner, other, slash.dir, slash.damage,
                slash.source)) {
                continue; // the Ryou finisher takes it from here
            }
            other.f_19802_ = 0;
            if (other.m_6469_(slash.source, slash.damage)) {
                Impact.hit(slash.owner, other, StyleCombat.spec(Hit.Weight.FINISHER, slash.dir.m_82490_(1.2).m_82520_(0.0, 0.25, 0.0)));
                PieceNetwork.around(other, new S2CSwordFx(S2CSwordFx.HIT, middle.f_82479_, middle.f_82480_, middle.f_82481_, slash.dir.f_82479_,
                    Hit.Weight.FINISHER.ordinal(), slash.dir.f_82481_, slash.color));
            }
        }
    }

    /** The cells the plane reached this tick, in the order it reached them, each once. */
    static void queueCells(Slash slash, Vec3 a, Vec3 b) {
        double length = b.m_82546_(a).m_82553_();
        int samples = Math.max(1, (int) Math.ceil(length / 0.5));
        int rows = slash.blocked.length;
        double bottom = slash.centre - slash.height / 2.0;
        double rowHeight = slash.height / rows;
        // across the plane's thickness: a sample every half block (a thick one cuts a wider gash)
        double sx = -slash.dir.f_82481_;
        double sz = slash.dir.f_82479_;
        double sl = Math.sqrt(sx * sx + sz * sz);
        if (sl < 1.0E-6) {
            sx = 1.0;
            sz = 0.0;
        } else {
            sx /= sl;
            sz /= sl;
        }
        // 2.2.4: each row as wide as the wave's body at its height (widest in the ground, thin at the peak: SwordRules.slashThicknessAt)
        double[] widths = new double[rows];
        int[] acrosses = new int[rows];
        for (int row = 0; row < rows; row++) {
            widths[row] = SwordRules.slashThicknessAt(slash.height, (row + 0.5) / rows);
            acrosses[row] = Math.max(1, (int) Math.ceil(widths[row] / 0.5));
        }
        for (int k = 1; k <= samples; k++) {
            Vec3 c = a.m_82549_(b.m_82546_(a).m_82490_(k / (double) samples));
            for (int row = 0; row < rows; row++) {
                double y = c.f_82480_ + bottom + (row + 0.5) * rowHeight;
                int across = acrosses[row];
                for (int w = 0; w < across; w++) {
                    double off = across == 1 ? 0.0 : (w / (double) (across - 1) - 0.5) * widths[row];
                    BlockPos pos = BlockPos.m_274561_(c.f_82479_ + sx * off, y, c.f_82481_ + sz * off);
                    long key = pos.m_121878_();
                    if (slash.seen.add(key)) {
                        slash.pending.add(new long[] {key, row});
                    }
                }
            }
        }
    }

    /** Cuts the waiting cells, within the per-slash and the shared budgets. */
    static void cut(Slash slash, int[] budget) {
        int checks = SwordRules.CUT_CHECKS_PER_TICK;
        int breaks = (int) Math.round(PieceConfig.get(PieceConfig.CUT_BREAKS, SwordRules.CUT_BREAKS_PER_TICK));
        ServerLevel level = slash.level;
        int bottom = level.m_141937_();
        int top = level.m_151558_();
        while (!slash.pending.isEmpty() && checks > 0 && breaks > 0 && budget[0] > 0 && budget[1] > 0) {
            long[] next = slash.pending.poll();
            int row = (int) next[1];
            checks--;
            budget[0]--;
            if (slash.blocked[row]) {
                continue;
            }
            BlockPos pos = BlockPos.m_122022_(next[0]);
            if (pos.m_123342_() < bottom || pos.m_123342_() >= top || !level.m_46805_(pos)) {
                slash.blocked[row] = true; // the world's edge: nothing beyond it in this row
                continue;
            }
            BlockState state = level.m_8055_(pos);
            if (state.m_60795_() || !state.m_60819_().m_76178_()) {
                continue; // air, water and lava are passed through
            }
            if (state.m_60800_(level, pos) < 0.0F) {
                slash.blocked[row] = true; // bedrock (and the like) stops the cut here: nothing behind it in this row is cut
                continue;
            }
            if (slash.owner instanceof ServerPlayer player) {
                BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
                MinecraftForge.EVENT_BUS.post(event);
                if (event.isCanceled()) {
                    slash.blocked[row] = true; // protected: nothing behind it in this row either
                    continue;
                }
            }
            // a block with a block entity (a chest) always drops (what it holds is never lost); a plain one by the explosion rule
            boolean drop = state.m_155947_() || java.util.concurrent.ThreadLocalRandom.current().nextDouble() < slash.drops;
            if (level.m_46953_(pos, drop, slash.owner)) {
                breaks--;
                budget[1]--;
            }
        }
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        ACTIVE.clear();
    }
}
