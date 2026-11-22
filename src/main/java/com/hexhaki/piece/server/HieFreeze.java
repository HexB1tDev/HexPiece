package com.hexhaki.piece.server;

import com.hexhaki.piece.hie.HieSounds;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.1.9 (the owner: "Like actually freeze them and like blocks of ice"; shown drawn crystal: "The ground can also turn into like actual ice
 * blocks, bro. I'm not stopping you from using Minecraft stuff", and asked, "Real ice blocks"): a body frozen solid by the Hie Hie no Mi.
 * Real ice blocks are put round it (every free block its body takes up, through server/Scars: protection, claims and the mobGriefing rule
 * obeyed, and put back if the server stops; where none may go, every client draws the game's own ice block there instead, client/hie/
 * HieFx), it is held exactly where it froze (put back if it drifts, no gravity, its legs slowed to nothing), it strikes nothing (its
 * blows and shots cancelled, as a Water Prison's) and its ice does not choke it. In its last {@link #CRACK_TICKS} ticks the game's own
 * cracks run over the ice; when its time is up, or once it has taken {@link #BREAKS} damage, the ice breaks with the game's own
 * ice-breaking particles and sound, the glassy shards of hie/HieArt and a last bite of damage. The hits that do not freeze at once leave
 * the cold in a body ({@link #chill}): slowed, and frozen solid on the {@link #CHILL_FREEZES}th.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class HieFreeze {
    /** A body frozen: whose ice, where it is held, until when, inside an Ice Ball (that client draws the ball, not a block), damage taken. */
    static final class Hold {
        final ServerLevel level;
        final ServerPlayer owner;
        final LivingEntity body;
        final Vec3 at;
        final long until;
        final boolean ball;
        /** The ice blocks put round it (BlockPos longs), and the first breaker id its cracks are sent under. */
        final long[] ice;
        final int crackId;
        float taken;
        /** Broken by the damage it took: it shatters on the next tick (never inside the blow's own damage). */
        boolean broken;

        Hold(ServerLevel level, ServerPlayer owner, LivingEntity body, Vec3 at, long until, boolean ball, long[] ice) {
            this.level = level;
            this.owner = owner;
            this.body = body;
            this.at = at;
            this.until = until;
            this.ball = ball;
            this.ice = ice;
            this.crackId = CRACK_IDS + (body.m_19879_() & 0xFFFFF) * 16;
        }
    }

    /** Damage taken inside the ice that breaks it early. */
    static final float BREAKS = 14.0F;
    /** The shatter's last bite. */
    static final float SHATTER_DAMAGE = 4.0F;
    /** A held body that drifts this far (blocks) is put back. */
    static final double DRIFT = 0.12;
    /** The most bodies frozen at once (the oldest thaws first). */
    static final int MOST = 48;
    /** Chill: how many cold hits freeze a body solid, for how long (ticks), and how long the cold stays without another hit. */
    public static final int CHILL_FREEZES = 3;
    static final int CHILL_FREEZE_TICKS = 60;
    static final int CHILL_KEEP = 100;
    /** The game's cracks run over a frozen body's ice in its last this many ticks. */
    public static final int CRACK_TICKS = 24;
    /** Breaker ids for the cracks (out of any entity's id). */
    static final int CRACK_IDS = 0x50000000;
    /** The ice put round a body: the most blocks, the game's break effect for at most this many of them. */
    static final int MOST_ICE = 12;
    static final int MOST_BREAKS = 6;

    private static final Map<Integer, Hold> HELD = new HashMap<>();
    /** Each chilled body's cold: {hits, the tick of the last}. */
    private static final Map<Integer, long[]> CHILL = new HashMap<>();

    private HieFreeze() {
    }

    /** Whether {@code body} is frozen solid now. */
    public static boolean frozen(Entity body) {
        return body != null && HELD.containsKey(body.m_19879_());
    }

    /**
     * {@code owner} freezes {@code body} solid for {@code ticks} where it is ({@code ball}: inside an Ice Ball, which the clients draw in
     * its place). A body already frozen is held longer (never shorter). Returns whether it froze.
     */
    public static boolean freeze(ServerPlayer owner, LivingEntity body, int ticks, boolean ball) {
        if (!(body.m_9236_() instanceof ServerLevel level) || !body.m_6084_() || ticks <= 0) {
            return false;
        }
        long now = level.m_46467_();
        Hold old = HELD.get(body.m_19879_());
        if (old != null && old.until >= now + ticks) {
            return false;
        }
        if (old == null && HELD.size() >= MOST) {
            Hold oldest = null;
            for (Hold h : HELD.values()) {
                if (oldest == null || h.until < oldest.until) {
                    oldest = h;
                }
            }
            if (oldest != null) {
                HELD.remove(oldest.body.m_19879_());
                thaw(oldest, false);
            }
        }
        Vec3 at = old != null ? old.at : body.m_20182_();
        body.m_20242_(true);
        body.m_20256_(Vec3.f_82478_);
        boolean inBall = ball || old != null && old.ball;
        long[] ice = old != null ? old.ice : inBall ? new long[0] : encase(owner, level, body, at, ticks);
        Hold hold = new Hold(level, owner, body, at, now + ticks, inBall, ice);
        HELD.put(body.m_19879_(), hold);
        CHILL.remove(body.m_19879_());
        // bz: whether real ice went round it (else every client draws the game's own ice block there)
        PieceNetwork.around(body, new S2CUoFx(HieMoves.FREEZE, body.m_19879_(), owner.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_, 0, 0, 0,
            body.m_20206_(), inBall ? 1.0F : 0.0F, ice.length > 0 ? 1.0F : 0.0F, (float) (body.m_20205_() * 0.5), ticks));
        if (old == null) {
            HieMoves.sound(owner, at.m_82520_(0, body.m_20206_() * 0.5, 0), HieSounds.FREEZE, 1.1F, 0.9F + level.m_213780_().m_188501_() * 0.2F);
        }
        return true;
    }

    /**
     * Real ice in every free block {@code body}'s box takes up, standing at {@code at} (air only: never a block of the world, never the
     * ground under it), through Scars (put back a little after {@code ticks} if the break never comes); none when the mobGriefing rule
     * is off. Returns the blocks put.
     */
    static long[] encase(ServerPlayer owner, ServerLevel level, LivingEntity body, Vec3 at, int ticks) {
        if (!level.m_46469_().m_46207_(net.minecraft.world.level.GameRules.f_46132_)) {
            return new long[0];
        }
        double half = body.m_20205_() * 0.5;
        int x0 = (int) Math.floor(at.f_82479_ - half + 0.05);
        int x1 = (int) Math.floor(at.f_82479_ + half - 0.05);
        int y0 = (int) Math.floor(at.f_82480_ + 0.05);
        int y1 = (int) Math.floor(at.f_82480_ + body.m_20206_() - 0.05);
        int z0 = (int) Math.floor(at.f_82481_ - half + 0.05);
        int z1 = (int) Math.floor(at.f_82481_ + half - 0.05);
        Scars scars = Scars.of(level);
        net.minecraft.world.level.block.state.BlockState ice = net.minecraft.world.level.block.Blocks.f_50126_.m_49966_();
        long[] put = new long[MOST_ICE];
        int n = 0;
        for (int y = y0; y <= y1 && n < MOST_ICE; y++) {
            for (int x = x0; x <= x1 && n < MOST_ICE; x++) {
                for (int z = z0; z <= z1 && n < MOST_ICE; z++) {
                    net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x, y, z);
                    if (!level.m_8055_(pos).m_60795_()) {
                        continue;
                    }
                    if (scars.swap(owner, pos, ice, List.of(), ticks + 4L) == Scars.TORN) {
                        put[n++] = pos.m_121878_();
                    }
                }
            }
        }
        return java.util.Arrays.copyOf(put, n);
    }

    /**
     * The ice round a body breaks: each block still ice (or the water it melted into) goes back to air, the game's own ice-breaking
     * particles and sound at a few of them, the cracks taken off.
     */
    static void breakIce(ServerLevel level, long[] ice, int crackId) {
        net.minecraft.world.level.block.state.BlockState air = net.minecraft.world.level.block.Blocks.f_50016_.m_49966_();
        int effect = net.minecraft.world.level.block.Block.m_49956_(net.minecraft.world.level.block.Blocks.f_50126_.m_49966_());
        for (int k = 0; k < ice.length; k++) {
            net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.m_122022_(ice[k]);
            level.m_6801_(crackId + k, pos, -1);
            net.minecraft.world.level.block.state.BlockState state = level.m_8055_(pos);
            boolean melted = state.m_60734_() == net.minecraft.world.level.block.Blocks.f_49990_;
            if (state.m_60734_() == net.minecraft.world.level.block.Blocks.f_50126_ || melted) {
                level.m_7731_(pos, air, 2 | 16);
                if (k < MOST_BREAKS) {
                    level.m_46796_(2001, pos, effect);
                }
            }
        }
    }

    /**
     * A cold hit on {@code body} ({@code hits} of them): slowed more with each; the {@link #CHILL_FREEZES}th within {@link #CHILL_KEEP}
     * ticks of the last freezes it solid. Returns whether it froze.
     */
    public static boolean chill(ServerPlayer owner, LivingEntity body, int hits) {
        if (!(body.m_9236_() instanceof ServerLevel level) || !body.m_6084_() || frozen(body)) {
            return false;
        }
        long now = level.m_46467_();
        long[] cold = CHILL.get(body.m_19879_());
        if (cold == null || now - cold[1] > CHILL_KEEP) {
            cold = new long[] {0, now};
            if (CHILL.size() > 256) {
                CHILL.values().removeIf(c -> now - c[1] > CHILL_KEEP);
            }
            CHILL.put(body.m_19879_(), cold);
        }
        cold[0] += hits;
        cold[1] = now;
        if (cold[0] >= CHILL_FREEZES) {
            return freeze(owner, body, CHILL_FREEZE_TICKS, false);
        }
        body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 60, (int) cold[0], false, false, false));
        return false;
    }

    /** The ice round a held body breaks ({@code shatter}: it shatters, with its last bite and its sound; else it only melts away). */
    static void thaw(Hold hold, boolean shatter) {
        LivingEntity body = hold.body;
        body.m_20242_(false);
        if (hold.ice.length > 0) {
            breakIce(hold.level, hold.ice, hold.crackId);
        }
        Vec3 middle = body.m_20182_().m_82520_(0, body.m_20206_() * 0.5, 0);
        PieceNetwork.around(body, new S2CUoFx(HieMoves.THAW, body.m_19879_(), hold.owner.m_19879_(), middle.f_82479_, middle.f_82480_,
            middle.f_82481_, 0, 0, 0, 0, shatter ? 1.0F : 0.0F, hold.ball ? 1.0F : 0.0F, (float) Math.max(0.9, body.m_20206_() * 0.6), 0));
        if (shatter && body.m_6084_()) {
            if (hold.owner.m_6084_()) {
                HieMoves.hurt(hold.owner, body, SHATTER_DAMAGE);
            }
            if (!hold.ball) {
                HieMoves.sound(hold.owner, middle, HieSounds.SHATTER, 0.9F, 0.95F + hold.level.m_213780_().m_188501_() * 0.2F);
            }
        }
    }

    /** {@code owner}'s player left (or the fruit went): nothing changes for the bodies it froze (they thaw on their own). */
    static void clear() {
        for (Hold h : new ArrayList<>(HELD.values())) {
            h.body.m_20242_(false);
            if (h.ice.length > 0) {
                breakIce(h.level, h.ice, h.crackId);
            }
        }
        HELD.clear();
        CHILL.clear();
    }

    @SubscribeEvent
    public static void freezeTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || HELD.isEmpty()) {
            return;
        }
        long now = level.m_46467_();
        List<Hold> done = new ArrayList<>();
        for (Hold hold : HELD.values()) {
            if (hold.level != level) {
                continue;
            }
            LivingEntity body = hold.body;
            if (!body.m_6084_() || body.m_213877_() || body.m_9236_() != level || now >= hold.until || hold.broken) {
                done.add(hold);
                continue;
            }
            // frozen where it stands: put back if it drifts, no motion, its legs slowed to nothing, nothing it strikes lands
            if (body.m_20182_().m_82554_(hold.at) > DRIFT) {
                body.m_6021_(hold.at.f_82479_, hold.at.f_82480_, hold.at.f_82481_);
            }
            body.m_20256_(Vec3.f_82478_);
            body.f_19789_ = 0.0F;
            body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 3, 9, false, false, false));
            Clashes.stagger(body, now, 3);
            // the game's own cracks running over its ice before it breaks
            long left = hold.until - now;
            if (left <= CRACK_TICKS && hold.ice.length > 0 && left % 3 == 0) {
                int progress = (int) Math.min(9, (CRACK_TICKS - left) * 10 / CRACK_TICKS);
                for (int k = 0; k < hold.ice.length; k++) {
                    level.m_6801_(hold.crackId + k, net.minecraft.core.BlockPos.m_122022_(hold.ice[k]), progress);
                }
                if (left == CRACK_TICKS - CRACK_TICKS % 3) {
                    HieMoves.sound(hold.owner, hold.at.m_82520_(0, body.m_20206_() * 0.5, 0), HieSounds.CRACK, 0.9F, 1.0F);
                }
            }
        }
        for (Hold hold : done) {
            HELD.remove(hold.body.m_19879_());
            thaw(hold, hold.body.m_6084_());
        }
    }

    /** Damage dealt to a frozen body adds up; enough breaks the ice early (it shatters). */
    @SubscribeEvent
    public static void freezeHurt(LivingHurtEvent event) {
        if (HELD.isEmpty()) {
            return;
        }
        Hold hold = HELD.get(event.getEntity().m_19879_());
        if (hold == null) {
            return;
        }
        hold.taken += event.getAmount();
        if (hold.taken >= BREAKS) {
            hold.broken = true;
        }
    }

    /** A frozen body strikes nothing (its blows, and what it shoots). */
    @SubscribeEvent
    public static void freezeAttack(LivingAttackEvent event) {
        if (HELD.isEmpty()) {
            return;
        }
        // its own ice never chokes it
        if (HELD.containsKey(event.getEntity().m_19879_()) && "inWall".equals(event.getSource().m_19385_())) {
            event.setCanceled(true);
            return;
        }
        Entity attacker = event.getSource().m_7639_();
        if (attacker != null && attacker != event.getEntity() && HELD.containsKey(attacker.m_19879_())) {
            event.setCanceled(true);
        }
    }
}
