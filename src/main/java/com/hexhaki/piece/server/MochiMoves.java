package com.hexhaki.piece.server;

import com.hexhaki.fx.Impact;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.mochi.MochiDraw;
import com.hexhaki.piece.mochi.MochiSounds;
import com.hexhaki.piece.mochi.MochiV2;
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
 * 3.6.3 (the owner: "I WANT THE OTHER MOVES, all of them, I want yours gone", after sending the Fruit Battlegrounds wiki's Dough and
 * DoughV2 pages; their answers: "Dough base + V2 awakened", keep only the left-click punches): the Mochi Mochi no Mi's moves, rebuilt
 * on the reference's moveset (docs/handoff/next-ai-3.6.0.md M and N) in our own art. The base kit: LMB Mochi Punch, Z Grilled Mochi
 * (held), X Mochi Muddle, C Lotus Mochi Combo, V Chestnut Mochi, B Mochi Anemone (held), N Buzz Cut Mochi; M the Awakening (its own set
 * comes in the next version). The server decides every hit and sends one small message for each thing to draw (S2CUoFx kinds 256 to
 * 319); every client builds the mochi itself (client/mochi/MochiFx, mochi/MochiDraw). With Armament on, every fist and its arm is
 * Haki-black and hits a quarter harder; the Conqueror's lightning (POINT ONE) at every strike and out of every blast.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class MochiMoves {
    /** A punch (LMB): user; x, y, z the shoulder, a the way, bx the arm (0 right, 1 left), size its reach. */
    public static final int PUNCH = 256;
    /** Grilled Mochi (Z): user; bx YAKI_SWELL (ticks the longest hold), YAKI_FLY (x, y, z from, a the step a tick, by the charge, ticks
     * its flight) or YAKI_DROP. */
    public static final int YAKI = 260;
    public static final int YAKI_SWELL = 0;
    public static final int YAKI_FLY = 1;
    public static final int YAKI_DROP = 2;
    /** Buzz Cut Mochi's wheel: user; bx 1 rolling (x, y, z to, a from, ticks), 0 stopped (x, y, z where). */
    public static final int WHEEL = 261;
    /** Mochi Anemone (B) held: user; bx 1 on (x, y, z the area's middle on the ground, size its radius, ticks), 0 off. */
    public static final int ANEMONE = 263;
    /** One of the Anemone's arms: user; id the donut, x, y, z where it strikes. */
    public static final int ANEMONE_PUNCH = 264;
    /** A body stuck in mochi: id the body, x, y, z its feet, size its height, ticks how long (0: let go). */
    public static final int STUCK = 266;
    /** A splat of mochi: x, y, z, a the way, size, id its seed. */
    public static final int SPLAT = 268;
    /** Grilled Mochi's blast: x, y, z, size, id its seed. */
    public static final int BOOM = 269;
    /** The Awakening: user; bx 1 on (ticks), 0 off (its own moves: the next version). */
    public static final int AWAKEN = 270;
    /** Mochi Muddle (X): user; x, y, z the trail's start on the ground, a the flat way, size its length, ticks how long it lies, id its seed. */
    public static final int MUDDLE = 277;
    /**
     * Lotus Mochi Combo (C): user; bx the phase: LOTUS_PUNCH (the user's donut: x, y, z the feet), LOTUS_HIT (id the body hit, x, y, z
     * its middle), LOTUS_AIR (id the body, a the side the blow comes from, by which blow), LOTUS_SLAM (id the body, x, y, z the giant
     * fist's donut over it), LOTUS_GROUND (x, y, z where it lands, id its seed).
     */
    public static final int LOTUS = 278;
    public static final int LOTUS_PUNCH = 0;
    public static final int LOTUS_SLAM = 1;
    public static final int LOTUS_HIT = 2;
    public static final int LOTUS_AIR = 3;
    public static final int LOTUS_GROUND = 4;
    /** Chestnut Mochi (V): user; bx 1 out (ticks), 0 gone. */
    public static final int CHESTNUT = 279;
    /** A spike shot by Chestnut Mochi's donut: user; x, y, z where it strikes, id its seed. */
    public static final int CHESTNUT_SPIKE = 280;

    /** The keys: N, M (client/FruitKeyN). */
    static final int N_SLOT = 40;
    static final int M_SLOT = 41;

    /** The punches: every so many ticks, how far, how wide, how hard. */
    static final int PUNCH_EVERY = 8;
    public static final double PUNCH_REACH = 6.0;
    static final double PUNCH_ARC = 28.0;
    static final float PUNCH_DAMAGE = 5.0F;
    /**
     * Grilled Mochi (the owner, for this move as the old C: "should be held, the longer its held the bigger the explosion, only 1 arm should
     * be pointed, no other part of the body should be animated"): full at {@link #YAKI_FULL} ticks, let go by itself at {@link #YAKI_MOST};
     * its explosion from {@link #YAKI_RADIUS_LEAST} (a tap) to {@link #YAKI_RADIUS_MOST} (full); a body it meets carried on it for
     * {@link #GRILLED_CARRY} ticks before it bursts.
     */
    public static final int YAKI_FULL = 60;
    public static final int YAKI_MOST = 100;
    public static final double YAKI_SPEED = 2.2;
    static final double YAKI_RANGE = 40.0;
    public static final double YAKI_RADIUS_LEAST = 2.2;
    public static final double YAKI_RADIUS_MOST = 7.0;
    public static final int GRILLED_CARRY = 8;

    /** How full Grilled Mochi is after {@code held} ticks (a tap a sixth). */
    public static double yakiCharge(double held) {
        return Math.max(1.0 / 6.0, Math.min(1.0, held / YAKI_FULL));
    }

    /** Grilled Mochi's explosion's radius at {@code charge} (0 to 1). */
    public static double yakiRadius(double charge) {
        return YAKI_RADIUS_LEAST + (YAKI_RADIUS_MOST - YAKI_RADIUS_LEAST) * charge;
    }

    /** Mochi Muddle: the trail's length and width, how fast it spreads (blocks a tick), how long it lies, the hurt at its start and end. */
    static final float MUDDLE_HURT_NEAR = 7.0F;
    static final float MUDDLE_HURT_FAR = 4.0F;
    static final int MUDDLE_STUCK = 40;
    /**
     * Lotus Mochi Combo: its reach (the reference: the fist reaches the one about six blocks ahead), its blows: the punch, each blow in
     * the air, the slam (its beats are the art's: MochiDraw.LOTUS_*).
     */
    public static final double LOTUS_REACH = 8.0;
    public static final float LOTUS_PUNCH_HURT = 6.0F;
    public static final float LOTUS_AIR_HURT = 3.0F;
    public static final float LOTUS_SLAM_HURT = 12.0F;
    /** Chestnut Mochi: how long its donut stays, a spike every so many ticks, at the nearest foe within this. */
    public static final int CHESTNUT_TICKS = (int) (MochiDraw.CHESTNUT_FORM + MochiDraw.CHESTNUT_FIRE);
    public static final int CHESTNUT_EVERY = 4;
    public static final double CHESTNUT_RANGE = 16.0;
    static final float CHESTNUT_HURT = 1.1F;
    /** Mochi Anemone: how long it may be held, how far off its area can be, the area's radius, a volley every so many ticks. */
    public static final int ANEMONE_MOST = 100;
    public static final double ANEMONE_RANGE = 14.0;
    public static final double ANEMONE_RADIUS = 3.4;
    static final int ANEMONE_EVERY = 3;
    /** Buzz Cut Mochi: how far and how long the wheel rolls, its size; five minutes' cooldown (the owner's own word for it). */
    /**
     * 3.6.3: Buzz Cut Mochi's phases (kind WHEEL, bx): the roll begins (ticks the most it rolls), it ends with no one met (unrolls),
     * caught (x, y, z the user's feet, by the yaw they faced, id the first one caught), the slam's ground (x, y, z), the swing's blow
     * (x, y, z the ball), one more stuck to the ball (id).
     */
    public static final int BUZZ_ROLLING = 1;
    public static final int BUZZ_MISSED = 0;
    public static final int BUZZ_CAUGHT = 2;
    public static final int BUZZ_GROUND = 3;
    public static final int BUZZ_BLOW = 4;
    public static final int BUZZ_STUCK = 6;
    /** The most Buzz Cut Mochi carries on its ball, the swing's blow and the slam's (the slam the far harder: the owner's word). */
    static final int BUZZ_MOST = 6;
    static final float BUZZ_BLOW_HURT = 8.0F;
    static final float BUZZ_SLAM_HURT = 20.0F;
    public static final int BUZZ_COOLDOWN = 6000;

    /**
     * 3.6.4: the Awakening (M: the reference's DoughV2 kit) and its left click, Trident Lacerations (kind TRIDENT, bx the swing 0 to 4
     * as it begins: x, y, z where it aims, a the flat way; bx 10 + the swing where it struck: x, y, z the one struck, by the ground's
     * height there). The swings follow one another while clicked within {@link #TRIDENT_RESET} ticks; the 5th breaks a raised shield
     * and throws the one struck straight up.
     */
    public static final int TRIDENT = 281;
    public static final int TRIDENT_STRUCK = 10;
    static final int TRIDENT_EVERY = 9;
    static final int TRIDENT_RESET = 30;
    public static final double TRIDENT_REACH = 4.4;
    static final double TRIDENT_ARC = 70.0;
    static final float TRIDENT_HURT = 5.0F;
    static final float TRIDENT_LAST_HURT = 7.0F;
    static final double TRIDENT_LAUNCH = 1.45;
    /** How often an awakened user's state is sent again (for whoever comes near later), the Awakening's cooldown after it comes on. */
    public static final int AWAKE_RESEND = 40;
    static final int AWAKEN_COOL = 40;

    static final class State {
        long punchNext;
        int punches;
        long yakiSince = -1;
        long anemoneSince = -1;
        long anemoneNext;
        int anemoneVolleys;
        Vec3 anemoneAt;
        long busyUntil;
        /** 3.6.4: the Awakening on; the last trident swing (0 to 4) and when; when the next may come. */
        boolean awakened;
        int trident = -1;
        long tridentLast;
        long tridentNext;
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
        return s.yakiSince >= 0 || s.anemoneSince >= 0 || now < s.busyUntil;
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

    // ------------------------------------------------------------------------------------------------------ 3.6.4: the Awakening

    /**
     * M: the Awakening on or off (the owner: "Dough base + V2 awakened"): while it is on the keys are the reference's DoughV2 kit
     * (MochiKit.AWAKENED) and the left click swings Mogura. A burst of dough and magenta Haki round the user as it comes on.
     */
    static void awaken(ServerPlayer player, State s, long now) {
        if (!s.awakened && !ItoMoves.ready(player, MochiKit.AWAKEN)) {
            return;
        }
        s.awakened = !s.awakened;
        s.trident = -1;
        if (s.awakened) {
            ItoMoves.cool(player, MochiKit.AWAKEN, AWAKEN_COOL);
            animate(player, "mochi_awaken");
            s.busyUntil = now + 8;
        }
        fx(player, AWAKEN, ++seeds, player.m_20182_(), Vec3.f_82478_, s.awakened ? 1 : 0, 0, 0, 0);
        sound(player, player.m_20182_(), MochiSounds.SQUISH, 1.3F, s.awakened ? 0.75F : 1.1F);
    }

    /** A key (Z to N) while awakened: the DoughV2 move in its place. */
    static void pressAwake(ServerPlayer player, State s, int slot, long now) {
        // the awakened moves are built one by one (3.6.4); a key with none yet does nothing
    }

    /**
     * LMB awakened: Trident Lacerations (mochi/MochiV2, the reference's frames 15 to 78): the next of five swings of Mogura (a thrust, a
     * level slash, a spin all round, a rising diagonal, a low sweep up), each striking on MochiV2.TRIDENT_HIT whoever is in front
     * within {@link #TRIDENT_REACH} (the spin: all round); the 5th breaks a raised shield and throws them straight up.
     */
    static void tridentSwing(ServerPlayer player, State s, long now) {
        if (now < s.tridentNext || now < s.busyUntil || s.yakiSince >= 0 || s.anemoneSince >= 0 || player.m_20159_()) {
            return;
        }
        int k = s.trident < 0 || now - s.tridentLast > TRIDENT_RESET ? 0 : (s.trident + 1) % MochiV2.TRIDENT_SWINGS;
        s.trident = k;
        s.tridentLast = now;
        s.tridentNext = now + (k == MochiV2.TRIDENT_SWINGS - 1 ? TRIDENT_EVERY + 6 : TRIDENT_EVERY);
        animate(player, "mochi_trident_" + (k + 1));
        Vec3 f = ItoMoves.flat(player);
        LivingEntity aimed = PikaMoves.aimed(player, TRIDENT_REACH, 0.6);
        Vec3 at = aimed != null ? StyleCombat.middle(aimed) : ItoMoves.chest(player).m_82549_(f.m_82490_(2.6));
        fx(player, TRIDENT, ++seeds, at, f, k, 0, TRIDENT_REACH, 0);
        sound(player, ItoMoves.chest(player), k >= 3 ? com.hexhaki.piece.sword.SwordSounds.SWING_HEAVY : com.hexhaki.piece.sword.SwordSounds.SWING,
            0.9F, 1.05F - k * 0.04F);
        later(player, MochiV2.TRIDENT_HIT, () -> tridentHit(player, k));
    }

    /** A trident swing's blow: whoever is within reach in front (the spin: all round) is struck. */
    static void tridentHit(ServerPlayer player, int k) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 chest = ItoMoves.chest(player);
        Vec3 f = ItoMoves.flat(player);
        boolean last = k == MochiV2.TRIDENT_SWINGS - 1;
        int struck = 0;
        for (LivingEntity body : PikaMoves.foes(player, chest, TRIDENT_REACH + 1.0)) {
            Vec3 d = StyleCombat.middle(body).m_82546_(chest);
            double flat = Math.hypot(d.f_82479_, d.f_82481_);
            if (flat > TRIDENT_REACH || Math.abs(d.f_82480_) > 2.5) {
                continue;
            }
            if (k != 2 && flat > 0.6 && (d.f_82479_ * f.f_82479_ + d.f_82481_ * f.f_82481_) / flat < Math.cos(Math.toRadians(TRIDENT_ARC))) {
                continue;
            }
            float damage = last ? TRIDENT_LAST_HURT : TRIDENT_HURT;
            if (last && body instanceof net.minecraft.world.entity.player.Player guard && guard.m_21254_()) {
                guard.m_36384_(true); // "the last hit bypassing": the raised shield broken through
            }
            if (MaguMoves.hurt(player, body, coated(player) ? damage * 1.25F : damage)) {
                Vec3 away = flat > 1.0E-6 ? new Vec3(d.f_82479_ / flat, 0, d.f_82481_ / flat) : f;
                StyleCombat.push(body, last ? new Vec3(away.f_82479_ * 0.1, TRIDENT_LAUNCH, away.f_82481_ * 0.1)
                    : body.m_20184_().m_82490_(0.3).m_82549_(away.m_82490_(0.25)).m_82520_(0, 0.08, 0));
            }
            Vec3 at = StyleCombat.middle(body);
            fx(player, TRIDENT, ++seeds, at, f, TRIDENT_STRUCK + k, body.m_20186_(), 0, 0);
            ItoMoves.lightning(player, at);
            if (++struck >= 6) {
                break;
            }
        }
        if (struck > 0) {
            sounds(player, chest.m_82549_(f.m_82490_(2.0)), last ? MochiSounds.SLAM : MochiSounds.PUNCH, last ? 1.0F : 0.8F, last ? 1.3F : 1.1F, 1, 2);
            if (last) {
                Impact.quake(level, chest, 6.0, 0.3F, 5);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    static void press(ServerPlayer player, int slot) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || player.m_20159_()) {
            return;
        }
        if (slot == M_SLOT) {
            awaken(player, s, now);
        } else if (s.awakened) {
            pressAwake(player, s, slot, now);
        } else if (slot == N_SLOT) {
            buzzCut(player, s, now);
        } else if (slot == MochiKit.GRILLED.slot()) {
            yakiSwell(player, s, now);
        } else if (slot == MochiKit.MUDDLE.slot()) {
            muddle(player, s, now);
        } else if (slot == MochiKit.LOTUS.slot()) {
            lotus(player, s, now);
        } else if (slot == MochiKit.CHESTNUT.slot()) {
            chestnut(player, s, now);
        } else if (slot == MochiKit.ANEMONE.slot()) {
            anemone(player, s, now);
        }
    }

    /** A held key let go: Grilled Mochi flies, the Anemone ends. */
    static void pressUp(ServerPlayer player, int slot) {
        State s = STATES.get(player.m_20148_());
        if (s != null && slot == MochiKit.ANEMONE.slot() && s.anemoneSince >= 0) {
            anemoneEnd(player, s);
        }
        if (s != null && slot == MochiKit.GRILLED.slot() && s.yakiSince >= 0) {
            yakiFire(player, s);
        }
    }

    // ------------------------------------------------------------------------------------------------------ LMB: Mochi Punch

    static void punch(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (s.awakened) {
            tridentSwing(player, s, now);
            return;
        }
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

    // ------------------------------------------------------------------------------------------------------ Z: Grilled Mochi

    static void yakiSwell(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.GRILLED)) {
            return;
        }
        s.yakiSince = now;
        animate(player, "mochi_yaki_charge");
        fx(player, YAKI, 0, ItoMoves.chest(player), player.m_20154_(), YAKI_SWELL, 0, 0, YAKI_MOST + 2);
        sound(player, ItoMoves.chest(player), MochiSounds.INFLATE, 1.0F, 1.0F);
    }

    /** The swollen forearm bursts: its fist flies out on a jet of fire, exploding at the first body or block, or at its range. */
    static void yakiFire(ServerPlayer player, State s) {
        long now = now(player);
        double charge = yakiCharge(now - s.yakiSince);
        s.yakiSince = -1;
        s.busyUntil = now + 6;
        ItoMoves.cool(player, MochiKit.GRILLED, (int) (100 + 60 * charge));
        Vec3 look = player.m_20154_();
        Vec3 from = ItoMoves.chest(player).m_82549_(look.m_82490_(1.0));
        Vec3 step = look.m_82490_(YAKI_SPEED);
        int flight = (int) Math.ceil(YAKI_RANGE / YAKI_SPEED);
        int seed = ++seeds;
        animate(player, "mochi_yaki_fire");
        fx(player, YAKI, seed, from, step, YAKI_FLY, charge, 0, flight);
        sound(player, from, MochiSounds.FIRE, 1.1F, 0.9F);
        LIVE.add(new Fist((ServerLevel) player.m_9236_(), player, from, step, now + flight, seed, charge));
    }

    /** Yaki Mochi's burning fist in flight. */
    static final class Fist implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        Vec3 at;
        final Vec3 step;
        final long until;
        final int seed;
        final double charge;
        LivingEntity carried;
        long carryUntil;

        Fist(ServerLevel level, ServerPlayer owner, Vec3 at, Vec3 step, long until, int seed, double charge) {
            this.level = level;
            this.owner = owner;
            this.at = at;
            this.step = step;
            this.until = until;
            this.seed = seed;
            this.charge = charge;
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
            if (this.carried == null) {
                for (LivingEntity body : ItoMoves.foes(this.owner, this.at.m_82549_(end).m_82490_(0.5), this.step.m_82553_() * 0.5 + 1.3)) {
                    Vec3 d = StyleCombat.middle(body).m_82546_(this.at);
                    Vec3 u = this.step.m_82541_();
                    double along = Math.max(0.0, Math.min(this.step.m_82553_(), d.m_82526_(u)));
                    if (d.m_82546_(u.m_82490_(along)).m_82553_() < 1.3) {
                        // 3.6.3 (the owner's reference: "If it hits or runs into an enemy, it will hit the enemy and then push them along with
                        // the projectile"): struck, then carried on the fist until it explodes
                        this.carried = body;
                        this.carryUntil = now + GRILLED_CARRY;
                        hit(this.owner, body, (float) (3.0 + 3.0 * this.charge), this.step.m_82490_(0.4));
                        break;
                    }
                }
            }
            if (this.carried != null) {
                if (!this.carried.m_6084_() || now >= this.carryUntil) {
                    boom(this.at);
                    return true;
                }
                this.carried.m_20256_(this.step.m_82490_(0.85).m_82520_(0, 0.04, 0));
                this.carried.f_19864_ = true;
                this.carried.f_19789_ = 0.0F;
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
            // 3.6.3: the longer it was held, the bigger and harder its explosion
            double radius = yakiRadius(this.charge);
            float damage = (float) (9.0 + 11.0 * this.charge) * (coat ? 1.25F : 1.0F);
            UoMoves.explode(this.owner, this.level, where, this.step.m_82541_(), radius, damage, 6, 1.0 + 0.6 * this.charge);
            fx(this.owner, YAKI, this.seed, where, Vec3.f_82478_, YAKI_DROP, 0, 0, 0);
            fx(this.owner, BOOM, this.seed, where, Vec3.f_82478_, 0, 0, radius * 0.65, 0);
            Impact.quake(this.level, where, 12.0 + 12.0 * this.charge, (float) (0.35 + 0.4 * this.charge), 9);
            sound(this.owner, where, MochiSounds.BOOM, (float) (1.0 + 0.8 * this.charge), (float) (1.1 - 0.25 * this.charge));
        }
    }

    // ------------------------------------------------------------------------------------------------------ X: Mochi Muddle

    /**
     * Mochi Muddle (the reference: "User forms a trail of dough on the ground, stunning any player that walks inside it. Damage decreases
     * the farther the player is from the beginning of the trail"): a trail of dough poured out along the ground ahead, spreading
     * blocks a tick; whoever it reaches or who walks in is glued fast and hurt, less the further along it. 3.6.3, on the reference's beats
     * (MochiDraw.MUDDLE_*): the charge with the arms out, the dive into a splash at the user's feet, a pool round them, the puddle
     * running out ahead from MUDDLE_RUN.
     */
    static void muddle(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.MUDDLE)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.MUDDLE, 240);
        // 3.6.3 (the reference's beats: MochiDraw.MUDDLE_*): the charge with the arms out, the dive, the splash at MUDDLE_START
        s.busyUntil = now + MochiDraw.MUDDLE_START + 14;
        int seed = ++seeds;
        animate(player, "mochi_muddle");
        sound(player, ItoMoves.chest(player), MochiSounds.INFLATE, 0.8F, 0.8F);
        later(player, MochiDraw.MUDDLE_START - 4, () -> {
            Vec3 f = ItoMoves.flat(player);
            Vec3 from = grounded(player, player.m_20182_());
            // drawn from 4 ticks before the splash (the dough engulfing the user)
            fx(player, MUDDLE, seed, from, f, 0, 0, MochiDraw.MUDDLE_LENGTH, (int) MochiDraw.MUDDLE_LIE + 4);
            later(player, 4, () -> {
                sound(player, from, MochiSounds.SQUISH, 1.3F, 0.7F);
                LIVE.add(new Muddle((ServerLevel) player.m_9236_(), player, from, f, now(player)));
            });
        });
    }

    /** The trail lying on the ground: who is on it is glued and hurt (each once). */
    static final class Muddle implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final Vec3 from;
        final Vec3 way;
        final long born;
        final Set<Integer> struck = new HashSet<>();

        Muddle(ServerLevel level, ServerPlayer owner, Vec3 from, Vec3 way, long born) {
            this.level = level;
            this.owner = owner;
            this.from = from;
            this.way = way;
            this.born = born;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            long age = now - this.born;
            if (!this.owner.m_6084_() || age >= MochiDraw.MUDDLE_LIE - 10) {
                return true;
            }
            if (age % 2 != 0) {
                return false;
            }
            // 3.6.3: the pool round the splash at once, the puddle running out ahead from MUDDLE_RUN, as the art draws it
            double reach = MochiDraw.muddleReach(age);
            double pool = age >= 3 ? MochiDraw.MUDDLE_POOL : 0.0;
            Vec3 side = new Vec3(-this.way.f_82481_, 0, this.way.f_82479_);
            Vec3 mid = this.from.m_82549_(this.way.m_82490_(reach * 0.5));
            for (LivingEntity body : ItoMoves.foes(this.owner, mid, reach * 0.5 + MochiDraw.MUDDLE_FAR + pool)) {
                Vec3 d = body.m_20182_().m_82546_(this.from);
                double along = d.m_82526_(this.way);
                double across = Math.abs(d.m_82526_(side));
                boolean inPool = d.f_82479_ * d.f_82479_ + d.f_82481_ * d.f_82481_ < pool * pool;
                boolean inTrail = along >= 0.0 && along <= reach && across <= MochiDraw.muddleWidth(along) * 0.5 + body.m_20205_() * 0.5;
                if (!inPool && !inTrail || Math.abs(body.m_20186_() - this.from.f_82480_) > 1.2) {
                    continue;
                }
                stick(this.owner, body, MUDDLE_STUCK);
                if (this.struck.add(body.m_19879_())) {
                    double t = Math.max(0.0, Math.min(1.0, along / MochiDraw.MUDDLE_LENGTH));
                    hit(this.owner, body, (float) (MUDDLE_HURT_NEAR + (MUDDLE_HURT_FAR - MUDDLE_HURT_NEAR) * t), new Vec3(0, 0.05, 0));
                }
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ C: Lotus Mochi Combo

    /**
     * Lotus Mochi Combo (the reference: "User summons an armored dough fist and punches and whoever is hit will be launched up in the air
     * and then thrown back down"), 3.6.3 tick by tick after its GIF (MochiDraw.LOTUS_*): a ribbon of dough swirls into a big donut at
     * the user's left; at {@code LOTUS_PUNCH} its fist punches along the user's aim (whoever it reaches is hit); at {@code LOTUS_LAUNCH}
     * they are thrown {@code LOTUS_HIGH} up and held there ({@link Juggled}), struck in the air at each of {@code LOTUS_AIR}; a giant fist
     * out of a donut over them ({@code LOTUS_DROP}) drives them down from {@code LOTUS_FALL} into the ground at {@code LOTUS_GROUND}: the
     * slam's blow, a crater that grows back, a quake, the knockdown, the Conqueror's lightning out of the impact.
     */
    static void lotus(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.LOTUS)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.LOTUS, 240);
        s.busyUntil = now + MochiDraw.LOTUS_PUNCH + 4;
        int seed = ++seeds;
        animate(player, "mochi_lotus");
        fx(player, LOTUS, seed, player.m_20182_(), player.m_20154_(), LOTUS_PUNCH, 0, LOTUS_REACH, (int) MochiDraw.LOTUS_LIFE);
        sound(player, ItoMoves.chest(player), MochiSounds.STRETCH, 1.0F, 0.9F);
        later(player, (int) MochiDraw.LOTUS_BALL, () -> sound(player, ItoMoves.chest(player), MochiSounds.SLAM, 0.7F, 1.3F));
        later(player, MochiDraw.LOTUS_PUNCH, () -> {
            LivingEntity body = PikaMoves.aimed(player, LOTUS_REACH, 0.8);
            if (body == null) {
                List<LivingEntity> ahead = ItoMoves.ahead(player, LOTUS_REACH, PUNCH_ARC);
                body = ahead.isEmpty() ? null : ahead.get(0);
            }
            sound(player, ItoMoves.chest(player), MochiSounds.STRETCH, 1.1F, 1.2F);
            if (body == null) {
                return;
            }
            LivingEntity caught = body;
            Vec3 f = ItoMoves.flat(player);
            fx(player, LOTUS, caught.m_19879_(), StyleCombat.middle(caught), f, LOTUS_HIT, 0, 0, 0);
            hit(player, caught, LOTUS_PUNCH_HURT, f.m_82490_(0.05));
            stick(player, caught, MochiDraw.LOTUS_LAUNCH - MochiDraw.LOTUS_PUNCH);
            later(player, MochiDraw.LOTUS_LAUNCH - MochiDraw.LOTUS_PUNCH, () -> launch(player, caught, seed));
        });
    }

    /** The Lotus combo's launch: the one hit thrown up and juggled to the ground (the art's beats, from the launch). */
    static void launch(ServerPlayer player, LivingEntity caught, int seed) {
        if (!caught.m_6084_()) {
            return;
        }
        STUCK_UNTIL.remove(caught.m_19879_());
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 from = caught.m_20182_();
        Vec3 ground = grounded(player, from);
        int start = MochiDraw.LOTUS_LAUNCH;
        // 3.6.4 (the owner: "from donut to donut ... glued with each punch to the next, fast"): the body knocked along the stations the
        // way the user faced at the punch (MochiDraw.lotusBody); each blow lands as its donut's fist reaches it (the client draws the
        // donuts from the launch: MochiDraw.lotusDonut)
        Vec3 faced = ItoMoves.flat(player);
        LIVE.add(new Juggled(level, player, caught, ground, now(player) - start, faced.f_82479_, faced.f_82481_));
        sound(player, from, MochiSounds.SLAM, 1.0F, 1.4F);
        int[] air = MochiDraw.LOTUS_AIR;
        for (int k = 0; k < air.length; k++) {
            int which = k;
            later(player, air[k] - start, () -> {
                if (!caught.m_6084_()) {
                    return;
                }
                double[] d = MochiDraw.lotusDonut(which, faced.f_82479_, faced.f_82481_, new double[6]);
                hit(player, caught, LOTUS_AIR_HURT, Vec3.f_82478_);
                sound(player, StyleCombat.middle(caught), MochiSounds.PUNCH, 1.0F, 0.9F + 0.08F * which);
                fx(player, LOTUS, caught.m_19879_(), StyleCombat.middle(caught), new Vec3(d[3], d[4], d[5]), LOTUS_AIR, which, 0, 0);
            });
        }
        later(player, MochiDraw.LOTUS_DROP - start, () -> {
            if (caught.m_6084_()) {
                double[] last = MochiDraw.lotusBody(MochiDraw.LOTUS_FALL, faced.f_82479_, faced.f_82481_, new double[3]);
                Vec3 top = ground.m_82520_(last[0], last[1] + 7.0, last[2]);
                fx(player, LOTUS, caught.m_19879_(), top, new Vec3(0, -1, 0), LOTUS_SLAM, 0, 0, (int) MochiDraw.LOTUS_SLAM_LIFE);
                sound(player, top, MochiSounds.STRETCH, 1.2F, 0.7F);
            }
        });
        later(player, MochiDraw.LOTUS_GROUND - start, () -> {
            if (!caught.m_6084_()) {
                return;
            }
            Vec3 at = new Vec3(caught.m_20185_(), ground.f_82480_, caught.m_20189_());
            fx(player, LOTUS, ++seeds, at, new Vec3(0, 1, 0), LOTUS_GROUND, 0, 0, (int) MochiDraw.LOTUS_GROUND_LIFE);
            hit(player, caught, LOTUS_SLAM_HURT, new Vec3(0, -0.6, 0));
            HakiFalls.knockDown(level, caught, 30, false);
            Craters.open(level, player, at.m_82520_(0, -0.6, 0), 2.2);
            // POINT ONE: a move that ends in an explosion throws the Conqueror's lightning out of that explosion
            ConquerorAura.exploded(player, level, at.m_82520_(0, 0.5, 0), 3.0);
            Impact.quake(level, at, 14.0, 0.5F, 8);
            sound(player, at, MochiSounds.SLAM, 1.6F, 0.8F);
        });
    }

    /**
     * 3.6.3: the one the Lotus combo hit, moved along MochiDraw.lotusBody (3.6.4: from donut to donut) over the ground it stood on (thrown up, hanging there, driven
     * down), its own motion and fall kept still meanwhile, let go on the ground.
     */
    static final class Juggled implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final LivingEntity body;
        final Vec3 ground;
        final long born;
        final double wx;
        final double wz;
        final double[] at = new double[3];

        Juggled(ServerLevel level, ServerPlayer owner, LivingEntity body, Vec3 ground, long born, double wx, double wz) {
            this.level = level;
            this.owner = owner;
            this.body = body;
            this.ground = ground;
            this.born = born;
            this.wx = wx;
            this.wz = wz;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            double age = now - this.born;
            if (!this.body.m_6084_() || this.body.m_9236_() != this.level || !this.owner.m_6084_() || age >= MochiDraw.LOTUS_GROUND) {
                return true;
            }
            MochiDraw.lotusBody(age, this.wx, this.wz, this.at);
            this.body.m_20256_(Vec3.f_82478_);
            this.body.f_19864_ = true;
            this.body.f_19789_ = 0.0F;
            this.body.m_6021_(this.ground.f_82479_ + this.at[0], this.ground.f_82480_ + this.at[1], this.ground.f_82481_ + this.at[2]);
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ V: Chestnut Mochi

    /**
     * Chestnut Mochi (the reference: "User creates a donut in the air covered in spikes that shoots spikes attacking the nearest player
     * that will always hit as long as they are in range"): 3.6.3 on its GIF's beats (MochiDraw.CHESTNUT_*): a comet of dough flung up
     * forms a spiked disc high over the user, which fires a spike every {@link #CHESTNUT_EVERY} ticks at the nearest foe within
     * {@link #CHESTNUT_RANGE} until {@link #CHESTNUT_TICKS}, then falls onto the user and splats.
     */
    static void chestnut(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.CHESTNUT)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.CHESTNUT, 260);
        s.busyUntil = now + 6;
        animate(player, "mochi_chestnut");
        fx(player, CHESTNUT, ++seeds, player.m_20182_(), Vec3.f_82478_, 1, 0, 0, CHESTNUT_TICKS);
        sound(player, ItoMoves.chest(player), MochiSounds.SQUISH, 1.0F, 1.1F);
        LIVE.add(new Chestnut((ServerLevel) player.m_9236_(), player, now));
    }

    /** Where Chestnut Mochi's donut floats over {@code player} (over the right shoulder, a little behind). */
    public static Vec3 chestnutAt(Vec3 feet, double yawDegrees, double height) {
        double[] o = MochiDraw.chestnutOffset(yawDegrees, height, new double[3]);
        return new Vec3(feet.f_82479_ + o[0], feet.f_82480_ + o[1], feet.f_82481_ + o[2]);
    }

    /** The spiked donut out over its user, shooting. */
    static final class Chestnut implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final long born;

        Chestnut(ServerLevel level, ServerPlayer owner, long born) {
            this.level = level;
            this.owner = owner;
            this.born = born;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            long age = now - this.born;
            if (!this.owner.m_6084_() || age >= CHESTNUT_TICKS) {
                fx(this.owner, CHESTNUT, 0, this.owner.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
                return true;
            }
            // 3.6.3: no spike before the disc has formed out of its flash (the reference's frame 36)
            if (age < MochiDraw.CHESTNUT_FORM || age % CHESTNUT_EVERY != 0) {
                return false;
            }
            Vec3 from = chestnutAt(this.owner.m_20182_(), this.owner.m_146908_(), this.owner.m_20206_());
            LivingEntity best = null;
            double bestD = CHESTNUT_RANGE * CHESTNUT_RANGE;
            for (LivingEntity body : ItoMoves.foes(this.owner, from, CHESTNUT_RANGE)) {
                double d = body.m_20280_(this.owner);
                if (d < bestD) {
                    bestD = d;
                    best = body;
                }
            }
            if (best != null) {
                Vec3 at = StyleCombat.middle(best);
                fx(this.owner, CHESTNUT_SPIKE, ++seeds, at, Vec3.f_82478_, 0, 0, 0, 0);
                Vec3 way = at.m_82546_(from);
                way = way.m_82556_() < 1.0E-6 ? Vec3.f_82478_ : way.m_82541_();
                if (MaguMoves.hurt(this.owner, best, coated(this.owner) ? CHESTNUT_HURT * 1.25F : CHESTNUT_HURT)) {
                    StyleCombat.push(best, best.m_20184_().m_82490_(0.5).m_82549_(way.m_82490_(0.08)));
                }
                sounds(this.owner, at, MochiSounds.PUNCH, 0.5F, 1.4F, 1, 3);
                // POINT ONE: the Conqueror's lightning where each spike strikes (with Haki and Ryou on; ItoMoves paces it)
                ItoMoves.lightning(this.owner, at);
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ B: Mochi Anemone

    /**
     * Mochi Anemone (the reference: "User summons a barrage of armored fists and will continuously attack anything that gets caught inside,
     * can be held to increase the duration"): while B is held, a cluster of donuts hangs over the place aimed at (up to
     * {@link #ANEMONE_RANGE} off) and Haki fists punch down out of them into it; whoever is inside is held and hit over and over.
     */
    static void anemone(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.ANEMONE)) {
            return;
        }
        s.anemoneSince = now;
        s.anemoneNext = now + 5;
        s.anemoneAt = grounded(player, ItoMoves.looked(player, ANEMONE_RANGE));
        animate(player, "mochi_donuts");
        fx(player, ANEMONE, ++seeds, s.anemoneAt, Vec3.f_82478_, 1, 0, ANEMONE_RADIUS, ANEMONE_MOST);
        sound(player, s.anemoneAt, MochiSounds.SQUISH, 1.0F, 1.0F);
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
        Vec3 at = s.anemoneAt;
        List<LivingEntity> inside = ItoMoves.foes(player, at.m_82520_(0, 1.0, 0), ANEMONE_RADIUS + 0.5);
        for (int k = 0; k < 2; k++) {
            int donut = s.anemoneVolleys++ % MochiDraw.ANEMONE_DONUTS;
            Vec3 strike;
            if (!inside.isEmpty()) {
                strike = StyleCombat.middle(inside.get((s.anemoneVolleys + k) % inside.size()));
            } else {
                double a = FireMesh.rand(s.anemoneVolleys, 3) * Math.PI * 2.0;
                double r = Math.sqrt(FireMesh.rand(s.anemoneVolleys, 7)) * ANEMONE_RADIUS;
                strike = at.m_82520_(Math.cos(a) * r, 0.2, Math.sin(a) * r);
            }
            fx(player, ANEMONE_PUNCH, donut, strike, Vec3.f_82478_, 0, 0, 0, 0);
        }
        sounds(player, at, MochiSounds.STRETCH, 0.6F, 1.3F, 1, 4);
        later(player, 2, () -> {
            for (LivingEntity body : inside) {
                if (body.m_6084_()) {
                    stick(player, body, 10);
                    hit(player, body, 2.2F, new Vec3(0, -0.1, 0));
                }
            }
        });
    }

    static void anemoneEnd(ServerPlayer player, State s) {
        long held = now(player) - s.anemoneSince;
        s.anemoneSince = -1;
        s.busyUntil = now(player) + 4;
        ItoMoves.cool(player, MochiKit.ANEMONE, (int) (140 + held));
        fx(player, ANEMONE, 0, s.anemoneAt != null ? s.anemoneAt : player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
        animate(player, "pika_laser_end");
    }

    // ------------------------------------------------------------------------------------------------------ N: Buzz Cut Mochi

    /**
     * Buzz Cut Mochi (N; the reference's Buzzcut and the owner's own cinematic, next-ai-3.6.3.md L): the user becomes a wheel rolling
     * where they look (their own client moves them: MochiFx.roll) for up to MochiDraw.BUZZ_ROLL ticks; the first foe it meets (and anyone
     * near) is caught: {@link Whirl}.
     */
    static void buzzCut(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.BUZZ_CUT)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.BUZZ_CUT, BUZZ_COOLDOWN);
        s.busyUntil = now + MochiDraw.BUZZ_ROLL + (long) MochiDraw.BUZZ_END + 4;
        player.f_19789_ = 0.0F;
        // the rumble is the client's (client/mochi/MochiFx: held only while the wheel rolls)
        fx(player, WHEEL, ++seeds, player.m_20182_(), player.m_20154_(), BUZZ_ROLLING, 0, MochiDraw.BUZZ_WHEEL, MochiDraw.BUZZ_ROLL);
        LIVE.add(new Wheel((ServerLevel) player.m_9236_(), player, now));
    }

    /** The wheel rolling (the user's own client moves it): the first foe it meets caught, with anyone near them. */
    static final class Wheel implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final long born;

        Wheel(ServerLevel level, ServerPlayer owner, long born) {
            this.level = level;
            this.owner = owner;
            this.born = born;
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
            long age = now - this.born;
            Vec3 at = this.owner.m_20182_().m_82520_(0, MochiDraw.BUZZ_WHEEL, 0);
            if (age >= 3) {
                List<LivingEntity> met = ItoMoves.foes(this.owner, at, MochiDraw.BUZZ_WHEEL + 0.6);
                if (!met.isEmpty()) {
                    List<LivingEntity> caught = new ArrayList<>(ItoMoves.foes(this.owner, met.get(0).m_20182_(), 2.8));
                    if (!caught.contains(met.get(0))) {
                        caught.add(0, met.get(0));
                    }
                    while (caught.size() > BUZZ_MOST) {
                        caught.remove(caught.size() - 1);
                    }
                    catchThem(caught);
                    return true;
                }
            }
            if (age >= MochiDraw.BUZZ_ROLL) {
                fx(this.owner, WHEEL, 0, this.owner.m_20182_(), Vec3.f_82478_, BUZZ_MISSED, 0, 0, 0);
                return true;
            }
            return false;
        }

        void catchThem(List<LivingEntity> caught) {
            ServerPlayer player = this.owner;
            Vec3 feet = player.m_20182_();
            float yaw0 = player.m_146908_();
            fx(player, WHEEL, caught.get(0).m_19879_(), feet, Vec3.f_82478_, BUZZ_CAUGHT, yaw0, 0, (int) MochiDraw.BUZZ_END);
            for (int k = 1; k < caught.size(); k++) {
                fx(player, WHEEL, caught.get(k).m_19879_(), feet, Vec3.f_82478_, BUZZ_STUCK, 0, 0, 0);
            }
            animate(player, "mochi_buzz");
            sound(player, feet, MochiSounds.SQUISH, 1.2F, 0.8F);
            LIVE.add(new Whirl(this.level, player, caught, feet, yaw0, now(player)));
        }
    }

    /**
     * Buzz Cut Mochi from the catch (MochiDraw.BUZZ_*): the heavy swing's blow at BUZZ_HIT sticks them to the giant ball (the impact
     * frame, the lightning), they are carried on it (MochiDraw.buzzStuck: whirled round the user, raised overhead) and anyone the ball
     * passes near is caught too, then slammed under it into the ground at BUZZ_SLAM: the far harder blow, a crater, the knockdown, the
     * quake and the Conqueror's lightning out of the impact; let go there.
     */
    static final class Whirl implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final List<LivingEntity> caught;
        final Vec3 feet;
        final float yaw0;
        final long born;
        boolean struck;
        boolean slammed;

        Whirl(ServerLevel level, ServerPlayer owner, List<LivingEntity> caught, Vec3 feet, float yaw0, long born) {
            this.level = level;
            this.owner = owner;
            this.caught = new ArrayList<>(caught);
            this.feet = feet;
            this.yaw0 = yaw0;
            this.born = born;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            double t = now - this.born;
            if (!this.owner.m_6084_() || t >= MochiDraw.BUZZ_END) {
                return true;
            }
            this.owner.f_19789_ = 0.0F;
            this.caught.removeIf(b -> !b.m_6084_() || b.m_9236_() != this.level);
            double[] arm = MochiDraw.buzzArm(t, this.feet.f_82479_, this.feet.f_82480_, this.feet.f_82481_, this.yaw0, new double[8]);
            Vec3 ball = new Vec3(arm[0] + arm[3] * arm[6], arm[1] + arm[4] * arm[6], arm[2] + arm[5] * arm[6]);
            if (!this.struck && t >= MochiDraw.BUZZ_HIT) {
                this.struck = true;
                fx(this.owner, WHEEL, ++seeds, ball, Vec3.f_82478_, BUZZ_BLOW, 0, 0, 0);
                for (LivingEntity body : this.caught) {
                    hit(this.owner, body, BUZZ_BLOW_HURT, Vec3.f_82478_);
                }
                sound(this.owner, ball, MochiSounds.SLAM, 1.4F, 1.0F);
            }
            if (t >= MochiDraw.BUZZ_HIT && t < MochiDraw.BUZZ_WHIRL && ((long) t) % 2 == 0 && this.caught.size() < BUZZ_MOST) {
                // anything the whirling ball passes near is stuck to it too (the owner: "Anything caught while the mace is spinning gets
                // attached to it as well")
                for (LivingEntity body : ItoMoves.foes(this.owner, ball, MochiDraw.buzzBall(t) + 1.0)) {
                    if (!this.caught.contains(body) && this.caught.size() < BUZZ_MOST) {
                        this.caught.add(body);
                        fx(this.owner, WHEEL, body.m_19879_(), this.feet, Vec3.f_82478_, BUZZ_STUCK, 0, 0, 0);
                    }
                }
            }
            if (t >= MochiDraw.BUZZ_HIT && !this.slammed) {
                double[] at = new double[3];
                for (int k = 0; k < this.caught.size(); k++) {
                    LivingEntity body = this.caught.get(k);
                    MochiDraw.buzzStuck(t, k, this.feet.f_82479_, this.feet.f_82480_, this.feet.f_82481_, this.yaw0, at);
                    body.m_20256_(Vec3.f_82478_);
                    body.f_19864_ = true;
                    body.f_19789_ = 0.0F;
                    body.m_6021_(at[0], at[1], at[2]);
                }
            }
            if (!this.slammed && t >= MochiDraw.BUZZ_SLAM) {
                this.slammed = true;
                Vec3 ground = grounded(this.owner, new Vec3(ball.f_82479_, this.feet.f_82480_ + 2.0, ball.f_82481_));
                fx(this.owner, WHEEL, ++seeds, ground, Vec3.f_82478_, BUZZ_GROUND, 0, 0, (int) MochiDraw.BUZZ_CRATER_LIFE);
                double[] at = new double[3];
                for (int k = 0; k < this.caught.size(); k++) {
                    LivingEntity body = this.caught.get(k);
                    MochiDraw.buzzStuck(t, k, this.feet.f_82479_, ground.f_82480_, this.feet.f_82481_, this.yaw0, at);
                    body.m_6021_(at[0], ground.f_82480_, at[2]);
                    hit(this.owner, body, BUZZ_SLAM_HURT, new Vec3(0, -0.6, 0));
                    HakiFalls.knockDown(this.level, body, 60, false);
                }
                Craters.open(this.level, this.owner, ground.m_82520_(0, -0.8, 0), 4.0);
                // POINT ONE: the slam's explosion throws the Conqueror's lightning out of it
                ConquerorAura.exploded(this.owner, this.level, ground.m_82520_(0, 0.6, 0), 4.5);
                Impact.quake(this.level, ground, 24.0, 0.8F, 14);
                sound(this.owner, ground, MochiSounds.SLAM, 2.0F, 0.7F);
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ holding fast

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

    // ------------------------------------------------------------------------------------------------------ the tick

    static void tick(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        long now = now(player);
        if (s.awakened && now % AWAKE_RESEND == 0) {
            fx(player, AWAKEN, 0, player.m_20182_(), Vec3.f_82478_, 1, 1, 0, 0);
        }
        if (s.yakiSince >= 0 && now - s.yakiSince >= YAKI_MOST) {
            yakiFire(player, s); // held its longest: it goes by itself
        }
        if (s.anemoneSince >= 0) {
            anemoneTick(player, s, now);
        }
    }

    static void forget(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        if (s.yakiSince >= 0) {
            s.yakiSince = -1;
            fx(player, YAKI, 0, player.m_20182_(), Vec3.f_82478_, YAKI_DROP, 0, 0, 0);
        }
        if (s.anemoneSince >= 0) {
            anemoneEnd(player, s);
        }
        if (s.awakened) {
            fx(player, AWAKEN, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
        }
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
