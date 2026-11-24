package com.hexhaki.piece.server;

import com.hexhaki.fx.Impact;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.mochi.MochiDraw;
import com.hexhaki.piece.mochi.MochiSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.5.5: the Mochi Mochi no Mi's moves (server/MochiKit; the keys the owner accepted: docs/handoff 8zzo). The server decides every hit
 * and sends one small message for each thing to draw (S2CUoFx kinds 256 to 319: the kind is two bytes since 3.5.5); every client builds
 * the mochi itself (client/mochi/MochiFx, mochi/MochiDraw). With Armament on, every fist and its arm is Haki-black and hits a quarter
 * harder; the Conqueror's lightning (POINT ONE) at every strike and out of every blast (ItoMoves.lightning, ConquerorAura.exploded,
 * UoMoves.explode).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class MochiMoves {
    /** A punch: user; x, y, z the shoulder, a the way, bx the arm (0 right, 1 left), size its reach. */
    public static final int PUNCH = 256;
    /** Mochi Tsuki: user; bx TSUKI_CHARGE (ticks the longest hold) or TSUKI_THRUST (x, y, z the hand, a the way, size the reach, by the charge). */
    public static final int TSUKI = 257;
    public static final int TSUKI_CHARGE = 0;
    public static final int TSUKI_THRUST = 1;
    public static final int TSUKI_DROP = 2;
    /** Yanagi Mochi: user; x, y, z the hip, a the flat way, id its seed. */
    public static final int YANAGI = 258;
    /** Kaku Mochi: user; x, y, z where the blocks slam, a the flat way. */
    public static final int KAKU = 259;
    /** Yaki Mochi: user; bx YAKI_SWELL (ticks), YAKI_FLY (x, y, z from, a the step a tick, ticks its flight) or YAKI_DROP. */
    public static final int YAKI = 260;
    public static final int YAKI_SWELL = 0;
    public static final int YAKI_FLY = 1;
    public static final int YAKI_DROP = 2;
    /** Zan Giri Mochi's wheel: user; bx 1 rolling (x, y, z to, a from, ticks), 0 stopped (x, y, z where). */
    public static final int WHEEL = 261;
    /** Zan Giri Mochi's club: user; x, y, z the body it slams. */
    public static final int CLUB = 262;
    /** Mochi Anemone: user; bx 1 on (ticks), 0 off. */
    public static final int ANEMONE = 263;
    /** One of the Anemone's arms punching: user; id the ring, x, y, z the target. */
    public static final int ANEMONE_PUNCH = 264;
    /** A sticky glob flying: x, y, z from, a the step a tick, ticks its flight, id its seed. */
    public static final int GLOB = 265;
    /** A body stuck in mochi: id the body, x, y, z its feet, size its height, ticks how long (0: let go). */
    public static final int STUCK = 266;
    /** A patch of sticky mochi on the ground: x, y, z, size its radius, ticks how long. */
    public static final int PATCH = 267;
    /** A splat of mochi: x, y, z, a the way, size, id its seed. */
    public static final int SPLAT = 268;
    /** Yaki Mochi's blast: x, y, z, size, id its seed. */
    public static final int BOOM = 269;
    /** The Awakening: user; bx 1 on (size the ground's radius, ticks), 0 off. */
    public static final int AWAKEN = 270;
    /** Nagare Mochi's wave: user; x, y, z from, a the flat way, size how far, ticks, id its seed. */
    public static final int NAGARE = 271;
    /** Kagami Mochi's walls: x, y, z the target on the ground, a the flat way they face, size how far apart. */
    public static final int KAGAMI = 272;
    /** Amadare Mochi: x, y, z the ground in the middle, size the radius, ticks, id its seed. */
    public static final int AMADARE = 273;
    /** Muso Donuts: user; x, y, z the target, ticks, id its seed. */
    public static final int DONUTS = 274;
    /** Mochi Ginkaku: x, y, z the ground in the middle, size the radius, ticks. */
    public static final int DOME = 275;
    /** Future Sight: user; bx 1 the window open (ticks); a hole: bx 2 (x, y, z, a the blow's way). */
    public static final int DODGE = 276;
    public static final int DODGE_OPEN = 1;
    public static final int DODGE_HOLE = 2;

    /** The keys: N, M, and the double tap of sneak (client/FruitKeyN). */
    static final int N_SLOT = 40;
    static final int M_SLOT = 41;
    public static final int DODGE_SLOT = 43;

    /** The punches: every so many ticks, how far, how wide, how hard. */
    static final int PUNCH_EVERY = 8;
    public static final double PUNCH_REACH = 6.0;
    static final double PUNCH_ARC = 28.0;
    static final float PUNCH_DAMAGE = 5.0F;
    /** Mochi Tsuki: the hold that fills it, the longest, its reach and blow empty and full. */
    public static final int TSUKI_FULL = 40;
    static final int TSUKI_MOST = 70;
    public static final double TSUKI_REACH = 8.0;
    public static final double TSUKI_REACH_FULL = 16.0;
    /** Yaki Mochi: the swelling, the fist's speed and range, the blast. */
    public static final int YAKI_SWELL_TICKS = 16;
    public static final double YAKI_SPEED = 2.2;
    static final double YAKI_RANGE = 40.0;
    public static final double YAKI_RADIUS = 3.5;
    /** Zan Giri Mochi: how far and how long the wheel rolls, how near it snags. */
    public static final double WHEEL_RANGE = 16.0;
    public static final int WHEEL_TICKS = 18;
    public static final double WHEEL_RADIUS = 2.2;
    /** The Anemone: how long it may be held, a punch every so many ticks, how near. */
    public static final int ANEMONE_MOST = 100;
    static final int ANEMONE_EVERY = 4;
    static final double ANEMONE_REACH = 8.0;
    /** Sticky Mochi: the glob's speed and range, how long it glues, how long a patch stays. */
    public static final double GLOB_SPEED = 1.4;
    static final double GLOB_RANGE = 30.0;
    public static final int STUCK_TICKS = 60;
    public static final int PATCH_TICKS = 100;
    static final double PATCH_RADIUS = 1.6;
    /** The Awakening: how long, its ground's radius. */
    public static final int AWAKEN_TICKS = 600;
    public static final double AWAKEN_RADIUS = 12.0;
    /** Nagare Mochi: how fast and far the wave rolls, how wide and high. */
    public static final double NAGARE_SPEED = 0.7;
    public static final double NAGARE_RANGE = 22.0;
    public static final double NAGARE_WIDTH = 8.0;
    public static final double NAGARE_HEIGHT = 3.6;
    /** Amadare Mochi: tendrils, one every so many ticks, over a radius. */
    public static final int AMADARE_COUNT = 15;
    public static final int AMADARE_EVERY = 2;
    public static final double AMADARE_RADIUS = 6.0;
    public static final double AMADARE_SKY = 14.0;
    /** Muso Donuts: the first fist after so many ticks, one every so many. */
    public static final int DONUT_FIRST = 6;
    public static final int DONUT_EVERY = 4;
    /** Mochi Ginkaku: its radius, how long it stands. */
    public static final double DOME_RADIUS = 6.0;
    public static final int DOME_TICKS = 100;
    /** Future Sight: how long the window stays open. */
    public static final int DODGE_TICKS = 60;

    static final class State {
        long punchNext;
        int punches;
        long tsukiSince = -1;
        long yakiSince = -1;
        long anemoneSince = -1;
        long anemoneNext;
        int anemonePunches;
        long awakenUntil = -1;
        long dodgeUntil = -1;
        long busyUntil;
        long holeHeard;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final List<UoMoves.Live> LIVE = new ArrayList<>();
    /** Who is stuck until when (a body is stuck once at a time). */
    private static final Map<Integer, Long> STUCK_UNTIL = new HashMap<>();
    private static int seeds;

    private MochiMoves() {
    }

    static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.m_20148_(), k -> new State());
    }

    static long now(ServerPlayer player) {
        return player.m_9236_().m_46467_();
    }

    static boolean coated(ServerPlayer player) {
        return ItoMoves.coated(player);
    }

    static void fx(ServerPlayer player, int kind, int id, Vec3 at, Vec3 a, double bx, double by, double size, int ticks) {
        ItoMoves.fx(player, kind, id, at, a, bx, by, size, ticks);
    }

    static void sound(ServerPlayer player, Vec3 at, String name, float volume, float pitch) {
        ItoMoves.sound(player, at, name, volume, pitch);
    }

    static void sounds(ServerPlayer player, Vec3 at, String name, float volume, float pitch, int most, int gap) {
        ItoMoves.sounds(player, at, name, volume, pitch, most, gap);
    }

    static void animate(ServerPlayer player, String name) {
        ItoMoves.animate(player, name);
    }

    static void later(ServerPlayer player, int ticks, Runnable action) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        LIVE.add(new UoMoves.Later(level, now(player) + Math.max(0, ticks), () -> {
            if (player.m_6084_()) {
                action.run();
            }
        }));
    }

    static boolean busy(State s, long now) {
        return s.tsukiSince >= 0 || s.yakiSince >= 0 || s.anemoneSince >= 0 || now < s.busyUntil;
    }

    static boolean awakened(State s, long now) {
        return s.awakenUntil >= 0 && now < s.awakenUntil;
    }

    /** A mochi blow on {@code body}: damage (a quarter more with Armament), a push, a splat where it lands, the Conqueror's lightning. */
    static void hit(ServerPlayer player, LivingEntity body, float damage, Vec3 push) {
        Vec3 at = StyleCombat.middle(body);
        if (MaguMoves.hurt(player, body, coated(player) ? damage * 1.25F : damage)) {
            StyleCombat.push(body, body.m_20184_().m_82490_(0.3).m_82549_(push));
        }
        Vec3 way = push.m_82556_() < 1.0E-6 ? new Vec3(0, 1, 0) : push.m_82541_();
        fx(player, SPLAT, ++seeds, at, way, 0, 0, Math.min(1.6, 0.6 + damage / 12.0), 0);
        sounds(player, at, MochiSounds.PUNCH, 0.9F, 0.9F + (seeds % 5) * 0.05F, 2, 3);
        ItoMoves.lightning(player, at);
    }

    /** The ground under {@code at} (within 6 up and 12 down), or {@code at}. */
    static Vec3 grounded(ServerPlayer player, Vec3 at) {
        Vec3 g = ItoMoves.ground((ServerLevel) player.m_9236_(), at, 6.0, 12.0);
        return g != null ? g : at;
    }

    /** Whom the user aims at within {@code range} (their middle), or the point looked at. */
    static Vec3 target(ServerPlayer player, double range) {
        LivingEntity body = PikaMoves.aimed(player, range, 0.2);
        return body != null ? StyleCombat.middle(body) : ItoMoves.looked(player, range);
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    static void press(ServerPlayer player, int slot) {
        State s = state(player);
        long now = now(player);
        if (slot == DODGE_SLOT) {
            dodge(player, s, now);
            return;
        }
        if (slot == M_SLOT && awakened(s, now)) {
            awakenOff(player, s);
            return;
        }
        if (busy(s, now) || player.m_20159_()) {
            return;
        }
        boolean awake = awakened(s, now);
        if (slot == M_SLOT) {
            awaken(player, s, now);
        } else if (slot == N_SLOT) {
            sticky(player, s, now);
        } else if (slot == MochiKit.YANAGI.slot()) {
            if (awake) {
                nagare(player, s, now);
            } else {
                yanagi(player, s, now);
            }
        } else if (slot == MochiKit.KAKU.slot()) {
            if (awake) {
                kagami(player, s, now);
            } else {
                kaku(player, s, now);
            }
        } else if (slot == MochiKit.YAKI.slot()) {
            if (awake) {
                amadare(player, s, now);
            } else {
                yakiSwell(player, s, now);
            }
        } else if (slot == MochiKit.ZANGIRI.slot()) {
            if (awake) {
                donuts(player, s, now);
            } else {
                zanGiri(player, s, now);
            }
        } else if (slot == MochiKit.ANEMONE.slot()) {
            if (awake) {
                ginkaku(player, s, now);
            } else {
                anemone(player, s, now);
            }
        }
    }

    /** A held key let go: the Anemone ends. */
    static void pressUp(ServerPlayer player, int slot) {
        State s = STATES.get(player.m_20148_());
        if (s != null && slot == MochiKit.ANEMONE.slot() && s.anemoneSince >= 0) {
            anemoneEnd(player, s);
        }
    }

    // ------------------------------------------------------------------------------------------------------ LMB: Mochi Punch

    static void punch(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || now < s.punchNext || player.m_20159_()) {
            return;
        }
        s.punchNext = now + PUNCH_EVERY;
        int which = s.punches++ % 2;
        animate(player, which == 0 ? "mochi_punch_1" : "mochi_punch_2");
        Vec3 look = player.m_20154_();
        Vec3 f = ItoMoves.flat(player);
        Vec3 side = new Vec3(-f.f_82481_, 0, f.f_82479_);
        Vec3 shoulder = ItoMoves.chest(player).m_82520_(0, 0.1, 0).m_82549_(side.m_82490_(which == 0 ? 0.36 : -0.36));
        fx(player, PUNCH, which, shoulder, look, which, 0, PUNCH_REACH, 0);
        sound(player, shoulder, MochiSounds.STRETCH, 0.7F, 1.25F + which * 0.1F);
        later(player, (int) MochiDraw.PUNCH_OUT, () -> {
            for (LivingEntity body : ItoMoves.ahead(player, PUNCH_REACH, PUNCH_ARC)) {
                hit(player, body, PUNCH_DAMAGE, f.m_82490_(0.55).m_82520_(0, 0.12, 0));
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ RMB: Mochi Tsuki

    static void use(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || player.m_20159_() || !ItoMoves.ready(player, MochiKit.TSUKI)) {
            return;
        }
        s.tsukiSince = now;
        animate(player, "mochi_tsuki_charge");
        fx(player, TSUKI, 0, ItoMoves.chest(player), player.m_20154_(), TSUKI_CHARGE, 0, 0, TSUKI_MOST);
        sound(player, ItoMoves.chest(player), MochiSounds.STRETCH, 0.8F, 0.8F);
        sound(player, ItoMoves.chest(player), MochiSounds.DRILL, 0.5F, 0.8F);
    }

    static void useUp(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s != null && s.tsukiSince >= 0) {
            tsukiThrust(player, s);
        }
    }

    /** How full Mochi Tsuki is after {@code held} ticks (0.2 a tap, full at {@link #TSUKI_FULL}). */
    public static double tsukiCharge(double held) {
        return Math.max(0.2, Math.min(1.0, held / TSUKI_FULL));
    }

    static void tsukiThrust(ServerPlayer player, State s) {
        long now = now(player);
        double charge = tsukiCharge(now - s.tsukiSince);
        s.tsukiSince = -1;
        s.busyUntil = now + 8;
        ItoMoves.cool(player, MochiKit.TSUKI, (int) (40 + 30 * charge));
        double reach = TSUKI_REACH + (TSUKI_REACH_FULL - TSUKI_REACH) * charge;
        float damage = (float) (7.0 + 9.0 * charge);
        Vec3 look = player.m_20154_();
        Vec3 from = ItoMoves.chest(player);
        animate(player, "mochi_tsuki_thrust");
        fx(player, TSUKI, 0, from, look, TSUKI_THRUST, charge, reach, (int) MochiDraw.TSUKI_LIFE);
        sound(player, from, MochiSounds.DRILL, 1.1F, 1.0F);
        later(player, 2, () -> {
            Set<Integer> struck = new HashSet<>();
            for (LivingEntity body : ItoMoves.foes(player, from.m_82549_(look.m_82490_(reach * 0.5)), reach * 0.5 + 1.5)) {
                Vec3 d = StyleCombat.middle(body).m_82546_(from);
                double along = d.m_82526_(look);
                if (along < 0 || along > reach + 0.5 || d.m_82546_(look.m_82490_(along)).m_82553_() > 1.4 || !struck.add(body.m_19879_())) {
                    continue;
                }
                hit(player, body, damage, look.m_82490_(1.1).m_82520_(0, 0.2, 0));
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ Z: Yanagi Mochi

    static void yanagi(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.YANAGI)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.YANAGI, 90);
        s.busyUntil = now + (long) MochiDraw.YANAGI_LIFE;
        Vec3 f = ItoMoves.flat(player);
        int seed = ++seeds;
        animate(player, "mochi_yanagi");
        fx(player, YANAGI, seed, player.m_20182_().m_82520_(0, 0.8, 0), f, 0, 0, 0, (int) MochiDraw.YANAGI_LIFE);
        Vec3 side = new Vec3(-f.f_82481_, 0, f.f_82479_);
        for (int k = 1; k <= 5; k++) {
            later(player, k * 4, () -> {
                Vec3 feet = player.m_20182_();
                sounds(player, feet.m_82549_(f.m_82490_(3.5)), MochiSounds.STOMP, 0.9F, 0.9F + seeds % 3 * 0.1F, 1, 3);
                for (LivingEntity body : ItoMoves.foes(player, feet.m_82549_(f.m_82490_(4.0)), 4.5)) {
                    Vec3 d = body.m_20182_().m_82546_(feet);
                    double along = d.m_82526_(f);
                    if (along > 1.0 && along < 7.2 && Math.abs(d.m_82526_(side)) < 3.0) {
                        hit(player, body, 3.0F, new Vec3(0, -0.2, 0).m_82549_(f.m_82490_(0.15)));
                    }
                }
            });
        }
    }

    // ------------------------------------------------------------------------------------------------------ X: Kaku Mochi

    static void kaku(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.KAKU)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.KAKU, 110);
        s.busyUntil = now + (long) MochiDraw.KAKU_SLAM + 4;
        Vec3 f = ItoMoves.flat(player);
        LivingEntity aimed = PikaMoves.aimed(player, 8.0, 0.2);
        Vec3 at = grounded(player, aimed != null ? aimed.m_20182_() : player.m_20182_().m_82549_(f.m_82490_(4.5)));
        animate(player, "mochi_kaku");
        fx(player, KAKU, ++seeds, at, f, 0, 0, 0, (int) MochiDraw.KAKU_LIFE);
        sound(player, ItoMoves.chest(player), MochiSounds.STRETCH, 0.9F, 0.8F);
        later(player, (int) MochiDraw.KAKU_SLAM, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            for (LivingEntity body : ItoMoves.foes(player, at, 3.2)) {
                hit(player, body, 12.0F, f.m_82490_(0.3).m_82520_(0, -0.3, 0));
                HakiFalls.knockDown(level, body, 30, false);
            }
            ConquerorAura.exploded(player, level, at.m_82520_(0, 0.5, 0), 3.0);
            Impact.quake(level, at, 16.0, 0.5F, 8);
            fx(player, SPLAT, ++seeds, at.m_82520_(0, 0.4, 0), new Vec3(0, 1, 0), 0, 0, 2.0, 0);
            sound(player, at, MochiSounds.SLAM, 1.4F, 0.9F);
        });
    }

    // ------------------------------------------------------------------------------------------------------ C: Yaki Mochi

    static void yakiSwell(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.YAKI)) {
            return;
        }
        s.yakiSince = now;
        animate(player, "mochi_yaki_charge");
        fx(player, YAKI, 0, ItoMoves.chest(player), player.m_20154_(), YAKI_SWELL, 0, 0, YAKI_SWELL_TICKS + 2);
        sound(player, ItoMoves.chest(player), MochiSounds.INFLATE, 1.0F, 1.0F);
    }

    /** The swollen forearm bursts: its fist flies out on a jet of fire, exploding at the first body or block, or at its range. */
    static void yakiFire(ServerPlayer player, State s) {
        long now = now(player);
        s.yakiSince = -1;
        s.busyUntil = now + 6;
        ItoMoves.cool(player, MochiKit.YAKI, 140);
        Vec3 look = player.m_20154_();
        Vec3 from = ItoMoves.chest(player).m_82549_(look.m_82490_(1.0));
        Vec3 step = look.m_82490_(YAKI_SPEED);
        int flight = (int) Math.ceil(YAKI_RANGE / YAKI_SPEED);
        int seed = ++seeds;
        animate(player, "mochi_yaki_fire");
        fx(player, YAKI, seed, from, step, YAKI_FLY, 0, 0, flight);
        sound(player, from, MochiSounds.FIRE, 1.1F, 0.9F);
        LIVE.add(new Fist((ServerLevel) player.m_9236_(), player, from, step, now + flight, seed));
    }

    /** Yaki Mochi's burning fist in flight. */
    static final class Fist implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        Vec3 at;
        final Vec3 step;
        final long until;
        final int seed;

        Fist(ServerLevel level, ServerPlayer owner, Vec3 at, Vec3 step, long until, int seed) {
            this.level = level;
            this.owner = owner;
            this.at = at;
            this.step = step;
            this.until = until;
            this.seed = seed;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (!this.owner.m_6084_()) {
                return true;
            }
            Vec3 next = this.at.m_82549_(this.step);
            BlockHitResult block = this.level.m_45547_(new ClipContext(this.at, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
            Vec3 end = block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_() : next;
            for (LivingEntity body : ItoMoves.foes(this.owner, this.at.m_82549_(end).m_82490_(0.5), this.step.m_82553_() * 0.5 + 1.3)) {
                Vec3 d = StyleCombat.middle(body).m_82546_(this.at);
                Vec3 u = this.step.m_82541_();
                double along = Math.max(0.0, Math.min(this.step.m_82553_(), d.m_82526_(u)));
                if (d.m_82546_(u.m_82490_(along)).m_82553_() < 1.3) {
                    boom(this.at.m_82549_(u.m_82490_(along)));
                    return true;
                }
            }
            if (block.m_6662_() == HitResult.Type.BLOCK || now >= this.until) {
                boom(end);
                return true;
            }
            this.at = next;
            return false;
        }

        void boom(Vec3 where) {
            boolean coat = coated(this.owner);
            UoMoves.explode(this.owner, this.level, where, this.step.m_82541_(), YAKI_RADIUS, coat ? 17.5F : 14.0F, 4, 1.3);
            fx(this.owner, YAKI, this.seed, where, Vec3.f_82478_, YAKI_DROP, 0, 0, 0);
            fx(this.owner, BOOM, this.seed, where, Vec3.f_82478_, 0, 0, YAKI_RADIUS * 0.65, 0);
            Impact.quake(this.level, where, 18.0, 0.55F, 9);
            sound(this.owner, where, MochiSounds.BOOM, 1.5F, 1.0F);
        }
    }

    // ------------------------------------------------------------------------------------------------------ V: Zan Giri Mochi

    static void zanGiri(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.ZANGIRI)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.ZANGIRI, 180);
        Vec3 f = ItoMoves.flat(player);
        Vec3 from = player.m_20182_();
        Vec3 want = from.m_82549_(f.m_82490_(WHEEL_RANGE));
        BlockHitResult block = player.m_9236_().m_45547_(new ClipContext(from.m_82520_(0, 1.0, 0), want.m_82520_(0, 1.0, 0), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, player));
        Vec3 to = block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_().m_82520_(0, -1.0, 0).m_82546_(f.m_82490_(0.8)) : want;
        int ticks = Math.max(4, (int) Math.round(WHEEL_TICKS * to.m_82554_(from) / WHEEL_RANGE));
        s.busyUntil = now + ticks + (long) MochiDraw.CLUB_LIFE;
        player.f_19789_ = 0.0F;
        fx(player, WHEEL, ++seeds, to, from, 1, 0, WHEEL_RADIUS, ticks);
        sound(player, from, MochiSounds.ROLL, 1.0F, 1.2F);
        LIVE.add(new Wheel((ServerLevel) player.m_9236_(), player, from, to, now, ticks));
    }

    /** The wheel rolling: the first foe near it snagged and slammed by the club. */
    static final class Wheel implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final Vec3 from;
        final Vec3 to;
        final long born;
        final int ticks;

        Wheel(ServerLevel level, ServerPlayer owner, Vec3 from, Vec3 to, long born, int ticks) {
            this.level = level;
            this.owner = owner;
            this.from = from;
            this.to = to;
            this.born = born;
            this.ticks = ticks;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (!this.owner.m_6084_()) {
                return true;
            }
            this.owner.f_19789_ = 0.0F;
            Vec3 at = this.owner.m_20182_().m_82520_(0, WHEEL_RADIUS * 0.6, 0);
            for (LivingEntity body : ItoMoves.foes(this.owner, at, WHEEL_RADIUS + 0.8)) {
                snag(body);
                return true;
            }
            if (now - this.born >= this.ticks) {
                fx(this.owner, WHEEL, 0, this.owner.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
                return true;
            }
            return false;
        }

        void snag(LivingEntity body) {
            ServerPlayer player = this.owner;
            fx(player, WHEEL, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
            stick(player, body, (int) MochiDraw.CLUB_SLAM + 2);
            animate(player, "mochi_club");
            fx(player, CLUB, body.m_19879_(), body.m_20182_(), Vec3.f_82478_, 0, 0, 0, (int) MochiDraw.CLUB_LIFE);
            sound(player, body.m_20182_(), MochiSounds.SQUISH, 1.0F, 0.9F);
            later(player, (int) MochiDraw.CLUB_SLAM, () -> {
                Vec3 at = body.m_20182_();
                if (body.m_6084_()) {
                    hit(player, body, 16.0F, new Vec3(0, -0.6, 0));
                    HakiFalls.knockDown(this.level, body, 40, false);
                }
                ConquerorAura.exploded(player, this.level, at.m_82520_(0, 0.6, 0), 3.0);
                Impact.quake(this.level, at, 18.0, 0.6F, 10);
                fx(player, SPLAT, ++seeds, at.m_82520_(0, 0.5, 0), new Vec3(0, 1, 0), 0, 0, 2.2, 0);
                sound(player, at, MochiSounds.SLAM, 1.5F, 0.85F);
            });
        }
    }

    // ------------------------------------------------------------------------------------------------------ B: Mochi Anemone

    static void anemone(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.ANEMONE)) {
            return;
        }
        s.anemoneSince = now;
        s.anemoneNext = now + 6;
        animate(player, "mochi_anemone");
        fx(player, ANEMONE, 0, player.m_20182_(), Vec3.f_82478_, 1, 0, 0, ANEMONE_MOST);
        sound(player, player.m_20182_(), MochiSounds.SQUISH, 1.0F, 1.0F);
    }

    static void anemoneTick(ServerPlayer player, State s, long now) {
        if (now - s.anemoneSince >= ANEMONE_MOST) {
            anemoneEnd(player, s);
            return;
        }
        if (now < s.anemoneNext) {
            return;
        }
        s.anemoneNext = now + ANEMONE_EVERY;
        List<LivingEntity> near = ItoMoves.foes(player, player.m_20182_().m_82520_(0, 1.0, 0), ANEMONE_REACH);
        near.sort((a, b) -> Double.compare(a.m_20280_(player), b.m_20280_(player)));
        for (int i = 0; i < Math.min(2, near.size()); i++) {
            LivingEntity body = near.get(i);
            int ring = s.anemonePunches++ % MochiDraw.ANEMONE_RINGS;
            fx(player, ANEMONE_PUNCH, ring, StyleCombat.middle(body), Vec3.f_82478_, 0, 0, 0, 0);
            sounds(player, player.m_20182_(), MochiSounds.STRETCH, 0.6F, 1.3F, 1, 4);
            later(player, 2, () -> {
                if (body.m_6084_()) {
                    Vec3 away = body.m_20182_().m_82546_(player.m_20182_());
                    away = new Vec3(away.f_82479_, 0, away.f_82481_);
                    away = away.m_82556_() < 1.0E-4 ? Vec3.f_82478_ : away.m_82541_();
                    hit(player, body, 3.5F, away.m_82490_(0.4).m_82520_(0, 0.1, 0));
                }
            });
        }
    }

    static void anemoneEnd(ServerPlayer player, State s) {
        long held = now(player) - s.anemoneSince;
        s.anemoneSince = -1;
        s.busyUntil = now(player) + 4;
        ItoMoves.cool(player, MochiKit.ANEMONE, (int) (120 + held));
        fx(player, ANEMONE, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
        animate(player, "pika_laser_end");
    }

    // ------------------------------------------------------------------------------------------------------ N: Sticky Mochi

    static void sticky(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.STICKY)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.STICKY, 70);
        s.busyUntil = now + 5;
        Vec3 look = player.m_20154_();
        Vec3 from = ItoMoves.chest(player).m_82549_(look.m_82490_(0.9));
        Vec3 step = look.m_82490_(GLOB_SPEED);
        int flight = (int) Math.ceil(GLOB_RANGE / GLOB_SPEED);
        int seed = ++seeds;
        animate(player, "mochi_throw");
        fx(player, GLOB, seed, from, step, 0, 0, 0, flight);
        sound(player, from, MochiSounds.SQUISH, 0.9F, 1.2F);
        LIVE.add(new Glob((ServerLevel) player.m_9236_(), player, from, step, now + flight, seed));
    }

    /** Glues {@code body} where it stands for {@code ticks} (once at a time: a body already stuck is held to its end). */
    static void stick(ServerPlayer player, LivingEntity body, int ticks) {
        long now = now(player);
        Long until = STUCK_UNTIL.get(body.m_19879_());
        if (until != null && now < until) {
            return;
        }
        STUCK_UNTIL.put(body.m_19879_(), now + ticks);
        fx(player, STUCK, body.m_19879_(), body.m_20182_(), Vec3.f_82478_, 0, 0, body.m_20206_(), ticks);
        LIVE.add(new Stuck((ServerLevel) player.m_9236_(), player, body, body.m_20182_(), now + ticks));
    }

    /** A body held in mochi: put back where it was stuck once it has moved, its fall kept. */
    static final class Stuck implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final LivingEntity body;
        final Vec3 at;
        final long until;

        Stuck(ServerLevel level, ServerPlayer owner, LivingEntity body, Vec3 at, long until) {
            this.level = level;
            this.owner = owner;
            this.body = body;
            this.at = at;
            this.until = until;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (!this.body.m_6084_() || this.body.m_9236_() != this.level || now >= this.until) {
                return true;
            }
            Vec3 v = this.body.m_20184_();
            this.body.m_20256_(new Vec3(0.0, Math.min(v.f_82480_, 0.0), 0.0));
            this.body.f_19864_ = true;
            Vec3 p = this.body.m_20182_();
            double dx = p.f_82479_ - this.at.f_82479_;
            double dz = p.f_82481_ - this.at.f_82481_;
            if (dx * dx + dz * dz > 0.15 * 0.15 || p.f_82480_ > this.at.f_82480_ + 0.3) {
                this.body.m_6021_(this.at.f_82479_, Math.min(p.f_82480_, this.at.f_82480_), this.at.f_82481_);
            }
            return false;
        }
    }

    /** Catches the shots near {@code at} (within {@code r}): held fast, given back their fall when let go. */
    static void catchShots(ServerLevel level, ServerPlayer owner, Vec3 at, double r, Set<Projectile> caught) {
        for (Projectile shot : level.m_6443_(Projectile.class, UoMoves.box(at, at).m_82400_(r + 1.0), e -> e.m_6084_())) {
            if (shot.m_19749_() == owner || caught.contains(shot) || shot.m_20182_().m_82554_(at) > r) {
                continue;
            }
            shot.m_20256_(Vec3.f_82478_);
            shot.m_20242_(true);
            caught.add(shot);
        }
        for (Projectile shot : caught) {
            if (shot.m_6084_()) {
                shot.m_20256_(Vec3.f_82478_);
            }
        }
    }

    static void letShotsGo(Set<Projectile> caught) {
        for (Projectile shot : caught) {
            if (shot.m_6084_()) {
                shot.m_20242_(false);
            }
        }
        caught.clear();
    }

    /** A sticky glob in flight: glues the first body it meets, or lands as a sticky patch; shots flying into it stick. */
    static final class Glob implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        Vec3 at;
        final Vec3 step;
        final long until;
        final int seed;
        final Set<Projectile> caught = new HashSet<>();

        Glob(ServerLevel level, ServerPlayer owner, Vec3 at, Vec3 step, long until, int seed) {
            this.level = level;
            this.owner = owner;
            this.at = at;
            this.step = step;
            this.until = until;
            this.seed = seed;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (!this.owner.m_6084_()) {
                letShotsGo(this.caught);
                return true;
            }
            catchShots(this.level, this.owner, this.at, 1.5, this.caught);
            Vec3 next = this.at.m_82549_(this.step);
            for (LivingEntity body : ItoMoves.foes(this.owner, next, 1.2)) {
                hit(this.owner, body, 3.0F, this.step.m_82490_(0.1));
                stick(this.owner, body, STUCK_TICKS);
                fx(this.owner, GLOB, this.seed, next, Vec3.f_82478_, 0, 0, 0, 0);
                sound(this.owner, next, MochiSounds.SPLAT, 1.0F, 1.0F);
                letShotsGo(this.caught);
                return true;
            }
            BlockHitResult block = this.level.m_45547_(new ClipContext(this.at, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
            if (block.m_6662_() == HitResult.Type.BLOCK || now >= this.until) {
                Vec3 where = grounded(this.owner, block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_() : next);
                fx(this.owner, GLOB, this.seed, where, Vec3.f_82478_, 0, 0, 0, 0);
                fx(this.owner, PATCH, this.seed, where, Vec3.f_82478_, 0, 0, PATCH_RADIUS, PATCH_TICKS);
                sound(this.owner, where, MochiSounds.SPLAT, 1.0F, 0.9F);
                LIVE.add(new Patch(this.level, this.owner, where, now + PATCH_TICKS, this.caught));
                return true;
            }
            this.at = next;
            return false;
        }
    }

    /** A sticky patch on the ground: whoever steps in it is glued a while; shots near it stick fast until it goes. */
    static final class Patch implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final Vec3 at;
        final long until;
        final Set<Projectile> caught;

        Patch(ServerLevel level, ServerPlayer owner, Vec3 at, long until, Set<Projectile> caught) {
            this.level = level;
            this.owner = owner;
            this.at = at;
            this.until = until;
            this.caught = caught;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (!this.owner.m_6084_() || now >= this.until) {
                letShotsGo(this.caught);
                return true;
            }
            catchShots(this.level, this.owner, this.at.m_82520_(0, 0.5, 0), PATCH_RADIUS, this.caught);
            if (now % 4 == 0) {
                for (LivingEntity body : ItoMoves.foes(this.owner, this.at, PATCH_RADIUS + 0.5)) {
                    if (Math.abs(body.m_20186_() - this.at.f_82480_) < 0.8) {
                        stick(this.owner, body, 30);
                    }
                }
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ M: the Awakening

    static void awaken(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.AWAKEN)) {
            return;
        }
        s.awakenUntil = now + AWAKEN_TICKS;
        s.busyUntil = now + 10;
        animate(player, "mochi_awaken");
        fx(player, AWAKEN, 0, player.m_20182_(), Vec3.f_82478_, 1, 0, AWAKEN_RADIUS, AWAKEN_TICKS);
        sound(player, player.m_20182_(), MochiSounds.ROLL, 1.4F, 0.8F);
        sound(player, player.m_20182_(), MochiSounds.SQUISH, 1.2F, 0.7F);
        Impact.quake((ServerLevel) player.m_9236_(), player.m_20182_(), 20.0, 0.4F, 12);
    }

    static void awakenOff(ServerPlayer player, State s) {
        if (s.awakenUntil < 0) {
            return;
        }
        s.awakenUntil = -1;
        ItoMoves.cool(player, MochiKit.AWAKEN, 600);
        fx(player, AWAKEN, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
    }

    // awakened Z: Nagare Mochi
    static void nagare(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.NAGARE)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.NAGARE, 110);
        s.busyUntil = now + 10;
        Vec3 f = ItoMoves.flat(player);
        Vec3 from = grounded(player, player.m_20182_());
        int ticks = (int) Math.ceil(NAGARE_RANGE / NAGARE_SPEED);
        int seed = ++seeds;
        animate(player, "mochi_wave");
        fx(player, NAGARE, seed, from, f, 0, 0, NAGARE_RANGE, ticks);
        sound(player, from, MochiSounds.ROLL, 1.4F, 0.9F);
        Set<Integer> caught = new HashSet<>();
        Vec3 side = new Vec3(-f.f_82481_, 0, f.f_82479_);
        for (int k = 1; k <= ticks; k++) {
            double front = 2.0 + NAGARE_SPEED * k;
            later(player, k, () -> {
                for (LivingEntity body : ItoMoves.foes(player, from.m_82549_(f.m_82490_(front)), NAGARE_WIDTH * 0.5 + 1.0)) {
                    Vec3 d = body.m_20182_().m_82546_(from);
                    double along = d.m_82526_(f);
                    if (along > front - 1.6 && along < front + 0.8 && Math.abs(d.m_82526_(side)) < NAGARE_WIDTH * 0.5 && caught.add(body.m_19879_())) {
                        hit(player, body, 6.0F, f.m_82490_(0.3));
                        stick(player, body, 50);
                    }
                }
            });
        }
    }

    // awakened X: Kagami Mochi
    static void kagami(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.KAGAMI)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.KAGAMI, 140);
        s.busyUntil = now + 10;
        Vec3 f = ItoMoves.flat(player);
        Vec3 at = grounded(player, target(player, 24.0));
        animate(player, "mochi_walls");
        fx(player, KAGAMI, ++seeds, at, f, 0, 0, 4.0, (int) MochiDraw.WALLS_LIFE);
        sound(player, at, MochiSounds.ROLL, 1.2F, 1.1F);
        later(player, (int) MochiDraw.WALLS_SHUT, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            for (LivingEntity body : ItoMoves.foes(player, at.m_82520_(0, 1.5, 0), 4.2)) {
                hit(player, body, 16.0F, new Vec3(0, 0.2, 0));
                stick(player, body, 20);
            }
            ConquerorAura.exploded(player, level, at.m_82520_(0, 1.5, 0), 3.5);
            Impact.quake(level, at, 18.0, 0.55F, 10);
            sound(player, at, MochiSounds.SLAM, 1.5F, 0.8F);
        });
    }

    // awakened C: Amadare Mochi
    static void amadare(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.AMADARE)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.AMADARE, 140);
        s.busyUntil = now + 10;
        Vec3 at = grounded(player, target(player, 30.0));
        int seed = ++seeds;
        int ticks = AMADARE_COUNT * AMADARE_EVERY + 20;
        animate(player, "mochi_rain");
        fx(player, AMADARE, seed, at, Vec3.f_82478_, 0, 0, AMADARE_RADIUS, ticks);
        sound(player, at.m_82520_(0, AMADARE_SKY, 0), MochiSounds.SQUISH, 1.3F, 0.7F);
        for (int k = 0; k < AMADARE_COUNT; k++) {
            int kk = k;
            later(player, k * AMADARE_EVERY + 4, () -> {
                // where tendril k strikes: the very point every client draws it (MochiDraw.rain)
                double ang = 2 * Math.PI * FireMesh.rand(seed, kk);
                double d = AMADARE_RADIUS * Math.sqrt(FireMesh.rand(seed, kk + 50));
                Vec3 p = at.m_82520_(Math.cos(ang) * d, 0, Math.sin(ang) * d);
                sounds(player, p, MochiSounds.SPLAT, 0.8F, 0.9F + kk % 3 * 0.1F, 1, 3);
                for (LivingEntity body : ItoMoves.foes(player, p.m_82520_(0, 1.0, 0), 1.8)) {
                    hit(player, body, 4.0F, new Vec3(0, -0.3, 0));
                    HakiFalls.knockDown((ServerLevel) player.m_9236_(), body, 12, false);
                }
            });
        }
    }

    // awakened V: Muso Donuts and Chikara Mochi
    static void donuts(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.DONUTS)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.DONUTS, 180);
        s.busyUntil = now + 10;
        LivingEntity aimed = PikaMoves.aimed(player, 40.0, 0.2);
        Vec3 at = aimed != null ? StyleCombat.middle(aimed) : ItoMoves.looked(player, 40.0);
        int seed = ++seeds;
        int ticks = DONUT_FIRST + MochiDraw.DONUTS * DONUT_EVERY + (int) MochiDraw.DONUT_FIST + 8;
        animate(player, "mochi_donuts");
        fx(player, DONUTS, seed, at, Vec3.f_82478_, 0, 0, 0, ticks);
        sound(player, player.m_20182_(), MochiSounds.POP, 1.2F, 0.8F);
        for (int k = 0; k < MochiDraw.DONUTS; k++) {
            later(player, DONUT_FIRST + k * DONUT_EVERY, () -> sound(player, player.m_20182_(), MochiSounds.STRETCH, 0.8F, 0.9F));
            later(player, DONUT_FIRST + k * DONUT_EVERY + (int) MochiDraw.DONUT_FIST, () -> {
                Vec3 where = aimed != null && aimed.m_6084_() ? StyleCombat.middle(aimed) : at;
                boolean coat = coated(player);
                UoMoves.explode(player, (ServerLevel) player.m_9236_(), where, ItoMoves.flat(player), 2.5, coat ? 11.25F : 9.0F, 0, 1.0);
                fx(player, SPLAT, ++seeds, where, ItoMoves.flat(player), 0, 0, 1.6, 0);
                sounds(player, where, MochiSounds.PUNCH, 1.2F, 0.8F, 1, 2);
            });
        }
    }

    // awakened B: Mochi Ginkaku
    static void ginkaku(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.GINKAKU)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.GINKAKU, 220);
        s.busyUntil = now + 10;
        Vec3 at = grounded(player, target(player, 24.0));
        animate(player, "mochi_dome");
        fx(player, DOME, ++seeds, at, Vec3.f_82478_, 0, 0, DOME_RADIUS, DOME_TICKS);
        sound(player, at, MochiSounds.ROLL, 1.4F, 0.7F);
        LIVE.add(new Dome((ServerLevel) player.m_9236_(), player, at, now, now + DOME_TICKS));
    }

    /** The dome standing: whoever is in it is held in (pushed back at its wall) and squeezed; it crushes them as it falls in. */
    static final class Dome implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final Vec3 at;
        final long born;
        final long until;

        Dome(ServerLevel level, ServerPlayer owner, Vec3 at, long born, long until) {
            this.level = level;
            this.owner = owner;
            this.at = at;
            this.born = born;
            this.until = until;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (!this.owner.m_6084_()) {
                return true;
            }
            if (now >= this.until) {
                for (LivingEntity body : ItoMoves.foes(this.owner, this.at, DOME_RADIUS + 0.5)) {
                    hit(this.owner, body, 12.0F, new Vec3(0, -0.4, 0));
                    HakiFalls.knockDown(this.level, body, 30, false);
                }
                ConquerorAura.exploded(this.owner, this.level, this.at.m_82520_(0, 1.0, 0), DOME_RADIUS);
                Impact.quake(this.level, this.at, 22.0, 0.65F, 12);
                fx(this.owner, SPLAT, ++seeds, this.at.m_82520_(0, 1.0, 0), new Vec3(0, 1, 0), 0, 0, 3.0, 0);
                sound(this.owner, this.at, MochiSounds.SLAM, 1.6F, 0.7F);
                return true;
            }
            if (now - this.born < 10 || now % 2 != 0) {
                return false;
            }
            for (LivingEntity body : ItoMoves.foes(this.owner, this.at, DOME_RADIUS + 1.5)) {
                Vec3 d = body.m_20182_().m_82546_(this.at);
                Vec3 flat = new Vec3(d.f_82479_, 0, d.f_82481_);
                double dist = flat.m_82553_();
                if (dist > DOME_RADIUS + 0.8 || dist < 1.0E-4) {
                    continue;
                }
                if (dist > DOME_RADIUS - 1.0) {
                    // at its wall: pushed back in
                    Vec3 in = flat.m_82490_(-1.0 / dist);
                    Vec3 v = body.m_20184_();
                    double out = Math.max(0.0, -(v.f_82479_ * in.f_82479_ + v.f_82481_ * in.f_82481_));
                    body.m_20256_(v.m_82549_(in.m_82490_(out + 0.3)));
                    body.f_19864_ = true;
                }
                if ((now - this.born) % 10 == 0) {
                    hit(this.owner, body, 2.0F, Vec3.f_82478_);
                }
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ Future Sight

    static void dodge(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.DODGE)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.DODGE, 400);
        s.dodgeUntil = now + DODGE_TICKS;
        fx(player, DODGE, 0, player.m_20182_(), Vec3.f_82478_, DODGE_OPEN, 0, 0, DODGE_TICKS);
        sound(player, player.m_20182_(), MochiSounds.SQUISH, 0.9F, 1.3F);
    }

    /** While Future Sight is open, a blow passes through a hole in the mochi body: none of it lands (true stops it). */
    static boolean blocks(ServerPlayer player, DamageSource source) {
        State s = STATES.get(player.m_20148_());
        long now = now(player);
        if (s == null || s.dodgeUntil < 0 || now >= s.dodgeUntil) {
            return false;
        }
        Entity from = source.m_7639_() != null ? source.m_7639_() : source.m_7640_();
        if (from == null || from == player) {
            return false; // only blows from someone (a hit, a shot, a blast): never a fall, the void, drowning or a command
        }
        Vec3 chest = ItoMoves.chest(player);
        Vec3 way = chest.m_82546_(from.m_146892_());
        way = way.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : way.m_82541_();
        if (now - s.holeHeard >= 3) {
            s.holeHeard = now;
            fx(player, DODGE, ++seeds, chest.m_82546_(way.m_82490_(0.2)), way, DODGE_HOLE, 0, 0, 0);
            sound(player, chest, MochiSounds.SQUISH, 0.7F, 1.4F);
        }
        return true;
    }

    // ------------------------------------------------------------------------------------------------------ the tick

    static void tick(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        long now = now(player);
        if (s.tsukiSince >= 0 && now - s.tsukiSince >= TSUKI_MOST) {
            tsukiThrust(player, s); // held its longest: it goes by itself
        }
        if (s.yakiSince >= 0 && now - s.yakiSince >= YAKI_SWELL_TICKS) {
            yakiFire(player, s);
        }
        if (s.anemoneSince >= 0) {
            anemoneTick(player, s, now);
        }
        if (s.awakenUntil >= 0 && now >= s.awakenUntil) {
            awakenOff(player, s);
        }
    }

    static void forget(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        if (s.tsukiSince >= 0) {
            s.tsukiSince = -1;
            fx(player, TSUKI, 0, player.m_20182_(), Vec3.f_82478_, TSUKI_DROP, 0, 0, 0);
        }
        if (s.yakiSince >= 0) {
            s.yakiSince = -1;
            fx(player, YAKI, 0, player.m_20182_(), Vec3.f_82478_, YAKI_DROP, 0, 0, 0);
        }
        if (s.anemoneSince >= 0) {
            anemoneEnd(player, s);
        }
        awakenOff(player, s);
        STATES.remove(player.m_20148_());
    }

    /** The server stopping (before the save): everything dropped. */
    static void clear() {
        STATES.clear();
        LIVE.clear();
        STUCK_UNTIL.clear();
    }

    /** The world's tick: the fists and globs in flight, the stuck bodies, the dome, everything timed. */
    @SubscribeEvent
    public static void mochiTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || LIVE.isEmpty()) {
            return;
        }
        long now = level.m_46467_();
        List<UoMoves.Live> due = new ArrayList<>();
        for (UoMoves.Live live : LIVE) {
            if (live.level() == level) {
                due.add(live);
            }
        }
        for (UoMoves.Live live : due) {
            boolean done;
            try {
                done = live.tick(now);
            } catch (RuntimeException failure) {
                done = true;
            }
            if (done) {
                LIVE.remove(live);
            }
        }
        if (STUCK_UNTIL.size() > 256) {
            STUCK_UNTIL.values().removeIf(t -> t < now);
        }
    }
}
