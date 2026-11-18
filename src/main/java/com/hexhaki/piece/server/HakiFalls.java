package com.hexhaki.piece.server;

import com.hexhaki.piece.haki.StunFall;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CFall;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.4.4: every Haki stun's knockdown ({@link StunFall}; the original's {@code HakiServerController.knockDown}, which every stun goes
 * through: the Conqueror's release, its tap, its full charge, running into a charging user, the last stand's blast, is replaced by
 * {@link #knockDown}: {@code patch/PiecePatch} TRAMPOLINES). The original forced the swimming pose on the body (on anything but a player:
 * its head tipped back, "look up weird", the owner) and played a player its knockdown animations; now every body, players and every
 * mob, modded or not, falls down and lies there stunned: it is kept in the original's forced stun (frozen, its aim and path dropped)
 * for as long as it lies there ({@link StunFall#down}), and every client near sees it fall ({@link S2CFall}, drawn by
 * {@code client/fx/Tumbles}).
 *
 * <p>The way it falls: away from whoever let the Haki out, as best the knockdown can tell (it is not told): the push the stun just gave
 * it (the tap throws it back from the user), else the last release near it ({@link #released}), else the nearest other player, else
 * any way; {@link StunFall#direction} then sends most falls about straight away, some to one side.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class HakiFalls {
    /** The original's forced stun: the game tick it ends (the body's own data; its tick handler freezes the body until then). */
    static final String STUN = "HexHakiForcedConquerorStunUntil";
    /** A release counts as the one that knocked a body down this long after (ticks) and this near (blocks). */
    static final long RELEASE_FRESH = 60L;
    static final double RELEASE_NEAR = 64.0;
    /** The nearest other player within this (blocks) counts as the Haki user. */
    static final double USER_NEAR = 32.0;
    /** A push faster than this (blocks a tick, along the ground) tells the way the stun came from. */
    static final double PUSHED = 0.03;

    /** A body down: when it fell and until when it lies there (game ticks), the way it fell, its seed. */
    record Down(ServerLevel level, long start, long until, float dx, float dz, long seed) {
    }

    private static final Map<Integer, Down> DOWN = new HashMap<>();
    /** Each level's last Haki release: where (x, z) and when. */
    private static final Map<ServerLevel, double[]> RELEASES = new WeakHashMap<>();

    private HakiFalls() {
    }

    /** {@code player} let its Haki out (ConquerorRelease.visual): the bodies it knocks down fall away from it. */
    public static void released(ServerPlayer player) {
        if (player.m_9236_() instanceof ServerLevel level) {
            RELEASES.put(level, new double[] {player.m_20185_(), player.m_20189_(), level.m_46467_()});
        }
    }

    /**
     * The original's knockdown ({@code ticks} long; {@code lastStand}: the last stand's blast did it): {@code body} falls down and lies
     * there stunned. Called again while it is down, it only stays down longer.
     */
    public static void knockDown(ServerLevel level, LivingEntity body, int ticks, boolean lastStand) {
        if (level == null || body == null || !body.m_6084_() || body.m_213877_()) {
            return;
        }
        long now = level.m_46467_();
        int down = StunFall.down(ticks);
        // kept in the original's forced stun while it lies there (frozen, sprinting stopped, a mob's aim and path dropped each tick)
        CompoundTag data = body.getPersistentData();
        data.m_128356_(STUN, Math.max(data.m_128454_(STUN), now + down));
        body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, down + 5, 10, false, false, false));
        if (body instanceof Mob mob) {
            mob.m_6710_(null);
            mob.m_21573_().m_26573_();
        }
        int id = body.m_19879_();
        Down was = DOWN.get(id);
        if (was != null && was.level() == level && now < was.until() + StunFall.GETUP_TICKS) {
            long until = Math.max(was.until(), now + down);
            if (until > was.until()) {
                DOWN.put(id, new Down(level, was.start(), until, was.dx(), was.dz(), was.seed()));
                PieceNetwork.around(body, new S2CFall(id, was.dx(), was.dz(), (int) (until - was.start()), was.seed()));
            }
            return;
        }
        double[] away = away(level, body, now);
        double[] way = StunFall.direction(away[0], away[1], body.m_217043_().m_188500_(), body.m_217043_().m_188500_());
        long seed = body.m_217043_().m_188505_();
        Down fall = new Down(level, now, now + down, (float) way[0], (float) way[1], seed);
        DOWN.put(id, fall);
        PieceNetwork.around(body, new S2CFall(id, fall.dx(), fall.dz(), down, seed));
    }

    /** The way away from whoever knocked {@code body} down (x, z; zero: not known). */
    static double[] away(ServerLevel level, LivingEntity body, long now) {
        Vec3 push = body.m_20184_();
        if (Math.hypot(push.f_82479_, push.f_82481_) > PUSHED) {
            return new double[] {push.f_82479_, push.f_82481_};
        }
        double[] release = RELEASES.get(level);
        if (release != null && now - (long) release[2] <= RELEASE_FRESH) {
            double dx = body.m_20185_() - release[0];
            double dz = body.m_20189_() - release[1];
            if (Math.hypot(dx, dz) <= RELEASE_NEAR && Math.hypot(dx, dz) > 1.0E-3) {
                return new double[] {dx, dz};
            }
        }
        Player nearest = null;
        double best = USER_NEAR * USER_NEAR;
        for (Player player : level.m_6907_()) {
            if (player == body || !player.m_6084_()) {
                continue;
            }
            double d = player.m_20280_(body);
            if (d < best) {
                best = d;
                nearest = player;
            }
        }
        if (nearest != null) {
            return new double[] {body.m_20185_() - nearest.m_20185_(), body.m_20189_() - nearest.m_20189_()};
        }
        return new double[] {0.0, 0.0};
    }

    /** True while {@code body} lies stunned (or is getting up). */
    public static boolean down(LivingEntity body) {
        return body != null && DOWN.containsKey(body.m_19879_());
    }

    @SubscribeEvent
    public static void fallTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || DOWN.isEmpty()) {
            return;
        }
        long now = level.m_46467_();
        DOWN.values().removeIf(down -> down.level() == level && (now >= down.until() + StunFall.GETUP_TICKS || now < down.start()));
    }
}
