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
    /** Buzz Cut Mochi's club: user; x, y, z the body it slams. */
    public static final int CLUB = 262;
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
    public static final int CHESTNUT_TICKS = 100;
    public static final int CHESTNUT_EVERY = 4;
    public static final double CHESTNUT_RANGE = 16.0;
    static final float CHESTNUT_HURT = 1.3F;
    /** Mochi Anemone: how long it may be held, how far off its area can be, the area's radius, a volley every so many ticks. */
    public static final int ANEMONE_MOST = 100;
    public static final double ANEMONE_RANGE = 14.0;
    public static final double ANEMONE_RADIUS = 3.4;
    static final int ANEMONE_EVERY = 3;
    /** Buzz Cut Mochi: how far and how long the wheel rolls, its size; five minutes' cooldown (the owner's own word for it). */
    public static final double WHEEL_RANGE = 16.0;
    public static final int WHEEL_TICKS = 18;
    public static final double WHEEL_RADIUS = 2.2;
    public static final int BUZZ_COOLDOWN = 6000;

    static final class State {
        long punchNext;
        int punches;
        long yakiSince = -1;
        long anemoneSince = -1;
        long anemoneNext;
        int anemoneVolleys;
        Vec3 anemoneAt;
        long busyUntil;
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

    // ------------------------------------------------------------------------------------------------------ the keys

    static void press(ServerPlayer player, int slot) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || player.m_20159_()) {
            return;
        }
        if (slot == M_SLOT) {
            player.m_5661_(net.minecraft.network.chat.Component.m_237113_("The Mochi Awakening's own moves come in the next version"), true);
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
        LIVE.add(new Juggled(level, player, caught, ground, now(player) - start));
        sound(player, from, MochiSounds.SLAM, 1.0F, 1.4F);
        int[] air = MochiDraw.LOTUS_AIR;
        for (int k = 0; k < air.length; k++) {
            int which = k;
            later(player, air[k] - start, () -> {
                if (!caught.m_6084_()) {
                    return;
                }
                // each blow from another side of them (left, right, from below)
                Vec3 side = ItoMoves.flat(player);
                Vec3 across = new Vec3(-side.f_82481_, 0, side.f_82479_);
                Vec3 way = which == 0 ? across.m_82520_(0, 0.6, 0) : which == 1 ? across.m_82490_(-1).m_82520_(0, 0.6, 0) : new Vec3(0, -1, 0)
                    .m_82549_(side.m_82490_(-0.4));
                fx(player, LOTUS, caught.m_19879_(), StyleCombat.middle(caught), way.m_82541_(), LOTUS_AIR, which, 0, 0);
                hit(player, caught, LOTUS_AIR_HURT, Vec3.f_82478_);
            });
        }
        later(player, MochiDraw.LOTUS_DROP - start, () -> {
            if (caught.m_6084_()) {
                Vec3 top = caught.m_20182_().m_82520_(0, 7.0, 0);
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
     * 3.6.3: the one the Lotus combo hit, moved along MochiDraw.lotusHeight over the ground it stood on (thrown up, hanging there, driven
     * down), its own motion and fall kept still meanwhile, let go on the ground.
     */
    static final class Juggled implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final LivingEntity body;
        final Vec3 ground;
        final long born;

        Juggled(ServerLevel level, ServerPlayer owner, LivingEntity body, Vec3 ground, long born) {
            this.level = level;
            this.owner = owner;
            this.body = body;
            this.ground = ground;
            this.born = born;
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
            double y = this.ground.f_82480_ + MochiDraw.lotusHeight(age);
            this.body.m_20256_(Vec3.f_82478_);
            this.body.f_19864_ = true;
            this.body.f_19789_ = 0.0F;
            this.body.m_6021_(this.ground.f_82479_, y, this.ground.f_82481_);
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ V: Chestnut Mochi

    /**
     * Chestnut Mochi (the reference: "User creates a donut in the air covered in spikes that shoots spikes attacking the nearest player
     * that will always hit as long as they are in range"): a spiked donut floats up over the user's shoulder for
     * {@link #CHESTNUT_TICKS} ticks, firing a spike every {@link #CHESTNUT_EVERY} at the nearest foe within {@link #CHESTNUT_RANGE}.
     */
    static void chestnut(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.CHESTNUT)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.CHESTNUT, 260);
        s.busyUntil = now + 6;
        animate(player, "mochi_amadare");
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
            if (age < 8 || age % CHESTNUT_EVERY != 0) {
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

    static void buzzCut(ServerPlayer player, State s, long now) {
        if (!ItoMoves.ready(player, MochiKit.BUZZ_CUT)) {
            return;
        }
        ItoMoves.cool(player, MochiKit.BUZZ_CUT, BUZZ_COOLDOWN);
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
