package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.noro.NoroMirror;
import com.hexhaki.piece.noro.NoroSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.3.4: the Noro Noro no Mi's moves (server/NoroKit; the keys the owner accepted, docs/handoff 8zw). Foxy's Noroma photons as pink-violet
 * rings of light: every one that touches a body or a shot slows it for 30 seconds (server/NoroSlow). What everyone sees is one small
 * {@link S2CUoFx} (kinds {@link #SHOT} to {@link #SNAP}, 180 to 199) and every client draws the photons from it (client/noro/NoroFx,
 * noro/NoroArt); the beam each client traces itself from the user's own look (the same blocks, the same mirrors: noro/NoroMirror).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class NoroMoves {
    // ---- what the client draws (S2CUoFx kinds 180 to 199 are the Noro fruit's)
    /** A burst of photons flying: id its seed, x, y, z from, a its step a tick, ticks its flight. */
    public static final int SHOT = 180;
    /** A burst ending: id its seed, x, y, z where, bx 1 when it caught something. */
    public static final int POP = 181;
    /** The beam on (user) and off. */
    public static final int BEAM_ON = 182;
    public static final int BEAM_OFF = 183;
    /** Slowed: id the body, user, x, y, z where, a a projectile's crawl a tick, bx 1 a projectile, ticks how long. */
    public static final int SLOW = 184;
    /** Its slowdown over: id the body. */
    public static final int UNSLOW = 185;
    /** A blow held back: id the body, user the striker, x, y, z the body's middle, a the blow's way, bx the blows so far. */
    public static final int STORE = 186;
    /** The held blows landing at once: id the body, x, y, z its middle, bx how many, size its strength 0 to 1. */
    public static final int RELEASE = 187;
    /** The mirror: id its seed, x, y, z its middle, a its face, ticks its life; gone early: MIRROR_OFF (id). */
    public static final int MIRROR = 188;
    public static final int MIRROR_OFF = 189;
    /** The Slow Field's dome: id its seed, x, y, z its foot, size its radius, ticks its life. */
    public static final int FIELD = 190;
    /** A shot turned back off the mirror: x, y, z where. */
    public static final int REFLECT = 191;
    /** Time's Up: the user's snap (user), x, y, z the hand. */
    public static final int SNAP = 192;

    // ---- numbers
    /** A press of RMB shorter than this (ticks) is a burst; held longer, the beam (at most {@link #BEAM_MOST} ticks). */
    static final int TAP = 5;
    public static final int BEAM_MOST = 70;
    public static final double BEAM_RANGE = 30.0;
    /** A burst: its speed (blocks a tick), how far, how close it must pass. */
    public static final double SHOT_SPEED = 1.8;
    static final double SHOT_RANGE = 36.0;
    static final double SHOT_REACH = 0.6;
    static final double BEAM_REACH = 0.7;
    /** Noroma Spread: how many bursts, across how many degrees each side. */
    static final int SPREAD_COUNT = 7;
    static final double SPREAD_DEGREES = 35.0;
    /** The mirror: how far in front, how long (ticks). */
    static final double MIRROR_AHEAD = 2.2;
    public static final int MIRROR_LIFE = 100;
    /** The Slow Field: its radius, life (ticks). */
    public static final double FIELD_RADIUS = 6.0;
    public static final int FIELD_LIFE = 160;

    static final class State {
        long busyUntil;
        long useSince = -1;
        long beamSince = -1;
        Mirror mirror;
    }

    /** A mirror in the air: its owner, its geometry, until when. */
    record Mirror(ServerPlayer owner, ServerLevel level, NoroMirror glass, long until, int seed) {
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final List<UoMoves.Live> LIVE = new ArrayList<>();
    static final List<Mirror> MIRRORS = new ArrayList<>();
    private static int seeds;

    private NoroMoves() {
    }

    static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.m_20148_(), k -> new State());
    }

    static long now(ServerPlayer player) {
        return player.m_9236_().m_46467_();
    }

    static void fx(ServerPlayer player, int kind, int id, Vec3 at, Vec3 a, double bx, double size, int ticks) {
        PieceNetwork.around(player, new S2CUoFx(kind, id, player.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_, (float) a.f_82479_,
            (float) a.f_82480_, (float) a.f_82481_, (float) bx, 0, 0, (float) size, ticks));
    }

    static void sound(ServerPlayer player, Vec3 at, String name, float volume, float pitch) {
        SoundEvent event = com.hexhaki.piece.PieceSounds.event(name);
        if (event != null) {
            player.m_9236_().m_6263_(null, at.f_82479_, at.f_82480_, at.f_82481_, event, SoundSource.PLAYERS, volume, pitch);
        }
    }

    /** Per user and event: the tick its window began and how many played in it ({@link #sounds}). */
    private static final Map<String, long[]> HEARD = new HashMap<>();

    /**
     * 3.3.4: at most {@code most} of one event of one user's in any {@code gap} ticks: a crowd slowed by the Slow Field or released by
     * Time's Up at once is one or two sounds, not twenty (the handoff's rule since 2.9.5: many sounds at once are loud).
     */
    static void sounds(ServerPlayer player, Vec3 at, String name, float volume, float pitch, int most, int gap) {
        long now = now(player);
        long[] heard = HEARD.computeIfAbsent(player.m_20148_() + name, k -> new long[] {Long.MIN_VALUE / 2, 0});
        if (now - heard[0] >= gap || now < heard[0]) {
            heard[0] = now;
            heard[1] = 0;
        }
        if (heard[1]++ < most) {
            sound(player, at, name, volume, pitch);
        }
    }

    static boolean ready(ServerPlayer player, FruitMove move) {
        return move.cooldown() == null || FruitData.ready(player, move.cooldown()) && FruitEnergy.afford(player, move);
    }

    static void cool(ServerPlayer player, FruitMove move, int ticks) {
        FruitEnergy.spend(player, move, ticks);
        FruitData.cooldown(player, move.cooldown(), ticks);
        FruitServer.sync(player);
    }

    static void animate(ServerPlayer player, String name) {
        HakiServerController.animate(player, name, 1);
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
        return s.useSince >= 0 || s.beamSince >= 0 || now < s.busyUntil;
    }

    /** Between the hands, a little in front of the chest: where the photons come out of. */
    public static Vec3 hands(Entity player) {
        Vec3 look = player.m_20154_();
        return player.m_146892_().m_82549_(look.m_82490_(0.7)).m_82520_(0.0, -0.35, 0.0);
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    static void press(ServerPlayer player, int slot) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || player.m_20159_()) {
            return;
        }
        if (slot == NoroKit.SPREAD.slot()) {
            spread(player, s);
        } else if (slot == NoroKit.MIRROR.slot()) {
            mirror(player, s);
        } else if (slot == NoroKit.TIMES_UP.slot()) {
            timesUp(player, s);
        } else if (slot == NoroKit.FIELD.slot()) {
            field(player, s);
        }
    }

    // ------------------------------------------------------------------------------------------------------ RMB: Noroma Photon

    static void use(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || player.m_20159_() || !ready(player, NoroKit.PHOTON)) {
            return;
        }
        s.useSince = now;
    }

    static void useUp(ServerPlayer player) {
        State s = state(player);
        if (s.beamSince >= 0) {
            stopBeam(player, s);
            return;
        }
        if (s.useSince < 0) {
            return;
        }
        s.useSince = -1;
        burst(player, s);
    }

    /** A tap: one burst of photons out of the hands, flying straight where the user looks. */
    static void burst(ServerPlayer player, State s) {
        cool(player, NoroKit.PHOTON, 16);
        s.busyUntil = now(player) + 6;
        animate(player, "hie_partisan_launch");
        later(player, 3, () -> shoot(player, hands(player), player.m_20154_()));
        sound(player, hands(player), NoroSounds.PHOTON, 0.9F, 1.0F);
    }

    /** RMB held past a tap: the steady beam, until it is let go (or {@link #BEAM_MOST} ticks). */
    static void startBeam(ServerPlayer player, State s) {
        s.useSince = -1;
        s.beamSince = now(player);
        animate(player, "pika_laser_charge");
        fx(player, BEAM_ON, 0, hands(player), Vec3.f_82478_, 0, 0, BEAM_MOST);
    }

    static void stopBeam(ServerPlayer player, State s) {
        long held = now(player) - s.beamSince;
        s.beamSince = -1;
        cool(player, NoroKit.PHOTON, 40 + (int) Math.min(80, held));
        s.busyUntil = now(player) + 6;
        animate(player, "pika_laser_end");
        fx(player, BEAM_OFF, 0, hands(player), Vec3.f_82478_, 0, 0, 0);
    }

    /** One tick of the beam: along the look (off the mirrors), everything it touches slowed. */
    static void beamTick(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 from = hands(player);
        Vec3 look = player.m_20154_();
        double[] d = {look.f_82479_, look.f_82480_, look.f_82481_};
        double left = BEAM_RANGE;
        for (int bounce = 0; bounce < 3 && left > 0.1; bounce++) {
            Vec3 way = new Vec3(d[0], d[1], d[2]);
            Vec3 to = from.m_82549_(way.m_82490_(left));
            BlockHitResult block = level.m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            double reach = block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_().m_82554_(from) : left;
            Mirror hit = null;
            double at = reach;
            for (Mirror m : MIRRORS) {
                double t = m.level == level ? m.glass.hit(from.f_82479_, from.f_82480_, from.f_82481_, d[0], d[1], d[2], at) : -1;
                if (t > 0) {
                    at = t;
                    hit = m;
                }
            }
            Vec3 end = from.m_82549_(way.m_82490_(at));
            catchAlong(player, from, end, BEAM_REACH);
            if (hit == null) {
                return;
            }
            hit.glass.reflect(d);
            left -= at;
            from = end;
        }
    }

    /** Everything along from {@code a} to {@code b} (within {@code reach}): living foes and other people's shots, slowed. */
    static void catchAlong(ServerPlayer player, Vec3 a, Vec3 b, double reach) {
        for (LivingEntity body : PikaMoves.crossed(player, a, b, reach)) {
            if (!NoroSlow.slowed(body) && NoroSlow.slow(player, body)) {
                ConquerorAura.exploded(player, (ServerLevel) player.m_9236_(), StyleCombat.middle(body), 1.5);
            }
        }
        for (Projectile shot : shotsAlong(player, a, b, reach + 0.3)) {
            if (!NoroSlow.slowed(shot)) {
                NoroSlow.slow(player, shot);
            }
        }
    }

    /** Projectiles (not the user's) within {@code reach} of the way from {@code a} to {@code b}. */
    static List<Projectile> shotsAlong(ServerPlayer player, Vec3 a, Vec3 b, double reach) {
        List<Projectile> out = new ArrayList<>();
        Vec3 way = b.m_82546_(a);
        double length = way.m_82553_();
        if (length < 1.0E-6) {
            return out;
        }
        Vec3 unit = way.m_82490_(1.0 / length);
        for (Projectile shot : player.m_9236_().m_6443_(Projectile.class, UoMoves.box(a, b).m_82400_(reach + 0.5),
            e -> e.m_6084_() && e.m_19749_() != player)) {
            Vec3 rel = shot.m_20182_().m_82546_(a);
            double along = Math.max(0.0, Math.min(length, rel.m_82526_(unit)));
            if (rel.m_82546_(unit.m_82490_(along)).m_82553_() <= reach) {
                out.add(shot);
            }
        }
        return out;
    }

    /** A burst of photons flying from {@code from} along {@code way}: the first body or shot it passes is slowed and it ends there. */
    static void shoot(ServerPlayer player, Vec3 from, Vec3 way) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 step = way.m_82541_().m_82490_(SHOT_SPEED);
        int flight = (int) Math.ceil(SHOT_RANGE / SHOT_SPEED);
        int seed = ++seeds;
        fx(player, SHOT, seed, from, step, 0, 0, flight);
        LIVE.add(new Shot(player, level, from, step, flight, seed));
    }

    /** A flying burst. */
    static final class Shot implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        Vec3 at;
        Vec3 step;
        int left;
        final int seed;
        int bounces;

        Shot(ServerPlayer user, ServerLevel level, Vec3 at, Vec3 step, int left, int seed) {
            this.user = user;
            this.level = level;
            this.at = at;
            this.step = step;
            this.left = left;
            this.seed = seed;
        }

        @Override
        public ServerLevel level() {
            return level;
        }

        @Override
        public boolean tick(long now) {
            if (left-- <= 0 || !user.m_6084_()) {
                fx(user, POP, seed, at, Vec3.f_82478_, 0, 0, 0);
                return true;
            }
            Vec3 to = at.m_82549_(step);
            double length = step.m_82553_();
            Vec3 unit = step.m_82490_(1.0 / length);
            // a mirror in the way: the burst bounces off it (a new burst from there)
            for (Mirror m : MIRRORS) {
                double t = m.level == level && bounces < 2 ? m.glass.hit(at.f_82479_, at.f_82480_, at.f_82481_, unit.f_82479_, unit.f_82480_,
                    unit.f_82481_, length) : -1;
                if (t > 0) {
                    Vec3 hit = at.m_82549_(unit.m_82490_(t));
                    double[] d = {step.f_82479_, step.f_82480_, step.f_82481_};
                    m.glass.reflect(d);
                    fx(user, POP, seed, hit, Vec3.f_82478_, 0, 0, 0);
                    fx(user, REFLECT, 0, hit, Vec3.f_82478_, 0, 0, 0);
                    int next = ++seeds;
                    Vec3 bounced = new Vec3(d[0], d[1], d[2]);
                    fx(user, SHOT, next, hit, bounced, 0, 0, left);
                    Shot again = new Shot(user, level, hit, bounced, left, next);
                    again.bounces = bounces + 1;
                    LIVE.add(again);
                    return true;
                }
            }
            BlockHitResult block = level.m_45547_(new ClipContext(at, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));
            Vec3 end = block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_() : to;
            LivingEntity first = null;
            double best = Double.MAX_VALUE;
            for (LivingEntity body : PikaMoves.crossed(user, at, end, SHOT_REACH)) {
                double d = StyleCombat.middle(body).m_82557_(at);
                if (d < best) {
                    best = d;
                    first = body;
                }
            }
            Entity caught = first;
            if (caught == null) {
                List<Projectile> shots = shotsAlong(user, at, end, SHOT_REACH + 0.3);
                caught = shots.isEmpty() ? null : shots.get(0);
            }
            if (caught != null) {
                Vec3 where = caught instanceof LivingEntity body ? StyleCombat.middle(body) : caught.m_20182_();
                NoroSlow.slow(user, caught);
                fx(user, POP, seed, where, Vec3.f_82478_, 1, 0, 0);
                ConquerorAura.exploded(user, level, where, 1.5);
                return true;
            }
            if (end != to) {
                fx(user, POP, seed, end, Vec3.f_82478_, 0, 0, 0);
                return true;
            }
            at = to;
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ Z: Noroma Spread

    /** A fan of bursts across the front, each its own way. */
    static void spread(ServerPlayer player, State s) {
        if (!ready(player, NoroKit.SPREAD)) {
            return;
        }
        cool(player, NoroKit.SPREAD, 140);
        s.busyUntil = now(player) + 12;
        animate(player, "hie_pheasant");
        sound(player, hands(player), NoroSounds.PHOTON, 1.1F, 0.85F);
        later(player, 5, () -> {
            Vec3 look = player.m_20154_();
            double yaw = Math.atan2(look.f_82481_, look.f_82479_);
            double flat = Math.sqrt(look.f_82479_ * look.f_82479_ + look.f_82481_ * look.f_82481_);
            for (int k = 0; k < SPREAD_COUNT; k++) {
                double off = Math.toRadians(-SPREAD_DEGREES + 2 * SPREAD_DEGREES * k / (SPREAD_COUNT - 1));
                Vec3 way = new Vec3(Math.cos(yaw + off) * flat, look.f_82480_, Math.sin(yaw + off) * flat);
                shoot(player, hands(player), way);
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ X: Noroma Mirror

    /** A mirror in the air in front, facing the user, for {@link #MIRROR_LIFE} ticks: shots that hit it turn back, slowed. */
    static void mirror(ServerPlayer player, State s) {
        if (!ready(player, NoroKit.MIRROR)) {
            return;
        }
        cool(player, NoroKit.MIRROR, 240);
        s.busyUntil = now(player) + 10;
        animate(player, "hie_ball_gather");
        later(player, 6, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 look = player.m_20154_();
            Vec3 middle = player.m_146892_().m_82549_(look.m_82490_(MIRROR_AHEAD)).m_82520_(0.0, -0.2, 0.0);
            if (s.mirror != null) {
                MIRRORS.remove(s.mirror);
                fx(player, MIRROR_OFF, s.mirror.seed, middle, Vec3.f_82478_, 0, 0, 0);
            }
            int seed = ++seeds;
            Mirror m = new Mirror(player, level, new NoroMirror(middle.f_82479_, middle.f_82480_, middle.f_82481_, -look.f_82479_,
                -look.f_82480_, -look.f_82481_), level.m_46467_() + MIRROR_LIFE, seed);
            s.mirror = m;
            MIRRORS.add(m);
            fx(player, MIRROR, seed, middle, look.m_82490_(-1.0), 0, NoroMirror.HALF, MIRROR_LIFE);
            sound(player, middle, NoroSounds.MIRROR, 1.0F, 1.0F);
        });
    }

    /** Each tick: shots crossing a mirror are turned back off it and slowed. */
    static void mirrors(ServerLevel level, long now) {
        for (int i = MIRRORS.size() - 1; i >= 0; i--) {
            Mirror m = MIRRORS.get(i);
            if (m.level != level) {
                continue;
            }
            if (now >= m.until || !m.owner.m_6084_()) {
                MIRRORS.remove(i);
                State s = STATES.get(m.owner.m_20148_());
                if (s != null && s.mirror == m) {
                    s.mirror = null;
                }
                continue;
            }
            NoroMirror g = m.glass;
            Vec3 c = new Vec3(g.cx, g.cy, g.cz);
            for (Projectile shot : level.m_6443_(Projectile.class, UoMoves.box(c, c).m_82400_(NoroMirror.HALF + 3.0),
                e -> e.m_6084_() && e.m_19749_() != m.owner && !NoroSlow.slowed(e))) {
                Vec3 p = shot.m_20182_();
                Vec3 v = shot.m_20184_();
                double length = v.m_82553_();
                if (length < 1.0E-4) {
                    continue;
                }
                Vec3 unit = v.m_82490_(1.0 / length);
                double t = g.hit(p.f_82479_, p.f_82480_, p.f_82481_, unit.f_82479_, unit.f_82480_, unit.f_82481_, length + 0.5);
                if (t < 0) {
                    continue;
                }
                double[] d = {v.f_82479_, v.f_82480_, v.f_82481_};
                g.reflect(d);
                Vec3 hit = p.m_82549_(unit.m_82490_(t));
                shot.m_6034_(hit.f_82479_ + d[0] * 0.05, hit.f_82480_ + d[1] * 0.05, hit.f_82481_ + d[2] * 0.05);
                shot.m_20256_(new Vec3(d[0], d[1], d[2]));
                if (shot instanceof AbstractHurtingProjectile fireball) {
                    // 3.3.4: a fireball pushes itself along its power every tick: turned too, or it would fly back at the user
                    double[] power = {fireball.f_36813_, fireball.f_36814_, fireball.f_36815_};
                    g.reflect(power);
                    fireball.f_36813_ = power[0];
                    fireball.f_36814_ = power[1];
                    fireball.f_36815_ = power[2];
                }
                NoroSlow.slow(m.owner, shot);
                fx(m.owner, REFLECT, 0, hit, Vec3.f_82478_, 0, 0, 0);
                sound(m.owner, hit, NoroSounds.MIRROR, 0.7F, 1.3F);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ C: Time's Up

    /** A snap: every slowdown the user put on ends now, every held blow landing at once. */
    static void timesUp(ServerPlayer player, State s) {
        if (!ready(player, NoroKit.TIMES_UP)) {
            return;
        }
        cool(player, NoroKit.TIMES_UP, 100);
        s.busyUntil = now(player) + 8;
        animate(player, "hie_partisan_raise");
        later(player, 4, () -> {
            Vec3 hand = player.m_146892_().m_82520_(0.0, 0.3, 0.0);
            fx(player, SNAP, 0, hand, Vec3.f_82478_, 0, 0, 0);
            sound(player, hand, NoroSounds.SLOW, 0.8F, 1.6F);
            NoroSlow.timesUp(player);
        });
    }

    // ------------------------------------------------------------------------------------------------------ V: Slow Field

    /** A dome of photons round the user's feet for {@link #FIELD_LIFE} ticks: whatever comes into it is slowed. */
    static void field(ServerPlayer player, State s) {
        if (!ready(player, NoroKit.FIELD)) {
            return;
        }
        cool(player, NoroKit.FIELD, 400);
        s.busyUntil = now(player) + 14;
        animate(player, "hie_ice_age");
        later(player, 5, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 foot = player.m_20182_();
            int seed = ++seeds;
            fx(player, FIELD, seed, foot, Vec3.f_82478_, 0, FIELD_RADIUS, FIELD_LIFE);
            sound(player, foot, NoroSounds.FIELD, 1.2F, 1.0F);
            long until = level.m_46467_() + FIELD_LIFE;
            LIVE.add(new UoMoves.Live() {
                @Override
                public boolean tick(long now) {
                    if (now >= until || !player.m_6084_()) {
                        return true;
                    }
                    if (now % 2 != 0) {
                        return false;
                    }
                    for (LivingEntity body : PikaMoves.foes(player, foot.m_82520_(0, 1.0, 0), FIELD_RADIUS)) {
                        if (!NoroSlow.slowed(body)) {
                            NoroSlow.slow(player, body);
                        }
                    }
                    for (Projectile shot : level.m_6443_(Projectile.class, UoMoves.box(foot, foot).m_82400_(FIELD_RADIUS),
                        e -> e.m_6084_() && e.m_19749_() != player && !NoroSlow.slowed(e))) {
                        if (shot.m_20182_().m_82554_(foot) <= FIELD_RADIUS) {
                            NoroSlow.slow(player, shot);
                        }
                    }
                    return false;
                }

                @Override
                public ServerLevel level() {
                    return level;
                }
            });
        });
    }

    // ------------------------------------------------------------------------------------------------------ the ticks

    /** Each tick of a player with the kit: a held RMB turning into the beam, the beam's own tick. */
    static void tick(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        long now = now(player);
        if (s.useSince >= 0 && now - s.useSince >= TAP) {
            startBeam(player, s);
        }
        if (s.beamSince >= 0) {
            if (now - s.beamSince >= BEAM_MOST || !player.m_6084_()) {
                stopBeam(player, s);
            } else {
                beamTick(player);
            }
        }
    }

    static void forget(ServerPlayer player) {
        State s = STATES.remove(player.m_20148_());
        if (s != null && s.beamSince >= 0) {
            fx(player, BEAM_OFF, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0);
        }
        if (s != null && s.mirror != null) {
            MIRRORS.remove(s.mirror);
            fx(player, MIRROR_OFF, s.mirror.seed, player.m_20182_(), Vec3.f_82478_, 0, 0, 0);
        }
    }

    static void clear() {
        STATES.clear();
        LIVE.clear();
        MIRRORS.clear();
        HEARD.clear();
    }

    /** The world's tick: the bursts in flight, the fields, the mirrors. */
    @SubscribeEvent
    public static void noroTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || LIVE.isEmpty() && MIRRORS.isEmpty()) {
            return;
        }
        long now = level.m_46467_();
        if (!MIRRORS.isEmpty()) {
            mirrors(level, now);
        }
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
    }
}
