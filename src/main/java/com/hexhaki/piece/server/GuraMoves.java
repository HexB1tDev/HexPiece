package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.fruit.FruitUtil;
import com.hexhaki.fx.Impact;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.gura.GuraSounds;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.1.1: the Gura Gura no Mi's moves (server/GuraKit), as Whitebeard and Blackbeard use them in the references (docs/handoff/gura-ref).
 * Every hit, throw and tear in the ground is worked out here on the server's ticks, in step with its animation (tools/gura_anims.py
 * keeps the same beats); what everyone sees is one small {@link S2CUoFx} (kinds {@link #CRACK} to {@link #TREMOR}, 60 to 79) and every
 * client builds the effect from it (client/gura/GuraFx, gura/GuraArt). The shock of a punch reaches each body as the drawn shock does
 * ({@link #SHOCK_SPEED} blocks a tick).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class GuraMoves {
    // ---- what the client draws (S2CUoFx kinds 60 to 79 are the Gura fruit's)
    /** The air cracked like glass: x, y, z its middle, a the way it faces (the punch's), size its radius, ticks its seed. */
    public static final int CRACK = 60;
    /** The quake bubble gathering on the fist (user; id 0 Kaishin's, 1 Gekishin's), and let go. */
    public static final int BUBBLE_ON = 61;
    public static final int BUBBLE_OFF = 62;
    /** A punch's shockwave: x, y, z the fist, a its way (unit), size its reach, bx its half-angle (radians), by its strength (0 to 1). */
    public static final int SHOCK = 63;
    /** The ground struck: x, y, z on the ground, size its radius, bx its strength (0 to 1), ticks its seed. */
    public static final int QUAKE = 64;
    /** Shima Yurashi held (user; x, y, z the middle, size its radius, ticks the most it lasts), and let go (bx its power). */
    public static final int SHAKE_ON = 65;
    public static final int SHAKE_OFF = 66;
    /** The ground split: x, y, z its start on the ground, a its flat way, size its length, ticks how long it takes to open. */
    public static final int RIFT = 67;
    /** The tsunami: x, y, z its middle's start, a its step a tick, size its width, bx its height, ticks its life, id its seed. */
    public static final int TSUNAMI = 68;
    /** Kabutowari's leap (user): x, y, z where the feet land, a where they left, id whom it follows (-1), ticks its flight, bx its height. */
    public static final int LEAP = 69;
    /** Gekishin's dash (user): x, y, z where the feet arrive, a where they left, id whom it follows, ticks it takes, bx the hold after. */
    public static final int DASH = 70;
    /** The landing quake round the feet: x, y, z the feet, size its radius. */
    public static final int TREMOR = 71;

    // ---- numbers
    /** How fast every drawn shock goes out (blocks a tick): a body is struck as it reaches them. */
    public static final double SHOCK_SPEED = 3.0;
    /** Kaishin: a tap shorter than this (ticks) is the quick punch; a full charge after {@link #KAISHIN_FULL}; the reaches. */
    static final int TAP = 6;
    static final int KAISHIN_FULL = 40;
    static final double TAP_REACH = 14.0;
    static final double KAISHIN_REACH = 18.0;
    static final double KAISHIN_MORE = 22.0;
    /** Shima Yurashi: its radius (blocks), the most it is held (ticks), how often the ground lurches (ticks). */
    public static final double SHAKE_RADIUS = 24.0;
    public static final int SHAKE_MAX = 100;
    static final int SHAKE_EVERY = 10;
    /** Kabutowari: how far it leaps at most, how fast across the ground (blocks a tick), the shortest and longest flight (ticks). */
    static final double LEAP_RANGE = 26.0;
    static final double LEAP_SPEED = 1.9;
    static final int LEAP_LEAST = 6;
    static final int LEAP_MOST = 16;
    /** The tsunami: its width, height (blocks), speed (blocks a tick), life (ticks) and how long it takes to rise. */
    public static final double TSUNAMI_WIDTH = 22.0;
    public static final double TSUNAMI_HEIGHT = 9.0;
    public static final double TSUNAMI_SPEED = 1.1;
    public static final int TSUNAMI_LIFE = 38;
    public static final int TSUNAMI_RISE = 8;
    /** Gekishin: a full charge (ticks), its reach (blocks), how fast the dash goes (blocks a tick), the launch's speed at full. */
    static final int GEKISHIN_FULL = 30;
    static final double GEKISHIN_RANGE = 16.0;
    static final double GEKISHIN_DASH = 3.0;
    static final double GEKISHIN_SPEED = 4.6;
    /** The Ground Splitter: its length (blocks), how far it opens a tick, and the blocks it may tear out in all. */
    public static final double RIFT_LENGTH = 36.0;
    public static final double RIFT_STEP = 2.0;
    static final int RIFT_MOST_BLOCKS = 220;
    /** The landing quake: the least fall (blocks) that quakes, and how often it may (ticks). */
    static final float TREMOR_FALL = 4.0F;
    static final int TREMOR_EVERY = 10;
    /** How long a body thrown by a quake stays down where it lands (ticks). */
    static final int STUN = 80;

    /** Each player's state of the kit. */
    static final class State {
        long busyUntil;
        long chargeSince = -1;
        long fistSince = -1;
        long shakeSince = -1;
        /** A leap or a dash the player's own client is flying, until. */
        long moveUntil;
        float lastFall;
        long tremorAt = -100;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final List<UoMoves.Live> LIVE = new ArrayList<>();
    private static int seeds;
    private static int frames = (1 << 30) + (1 << 28);

    private GuraMoves() {
    }

    static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.m_20148_(), k -> new State());
    }

    static long now(ServerPlayer player) {
        return player.m_9236_().m_46467_();
    }

    static void fx(ServerPlayer player, int kind, int id, Vec3 at, Vec3 a, double bx, double by, double bz, double size, int ticks) {
        PieceNetwork.around(player, new S2CUoFx(kind, id, player.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_, (float) a.f_82479_,
            (float) a.f_82480_, (float) a.f_82481_, (float) bx, (float) by, (float) bz, (float) size, ticks));
    }

    static void fx(ServerPlayer player, int kind, int id) {
        PieceNetwork.around(player, S2CUoFx.of(kind, id, player.m_19879_()));
    }

    static void sound(ServerPlayer player, Vec3 at, String name, float volume, float pitch) {
        SoundEvent event = com.hexhaki.piece.PieceSounds.event(name);
        if (event != null) {
            player.m_9236_().m_6263_(null, at.f_82479_, at.f_82480_, at.f_82481_, event, SoundSource.PLAYERS, volume, pitch);
        }
    }

    static boolean ready(ServerPlayer player, FruitMove move) {
        return move.cooldown() == null || FruitData.ready(player, move.cooldown());
    }

    static void cool(ServerPlayer player, FruitMove move, int ticks) {
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

    /** Whether a move holds the player now (a charge, the shake, a leap or a dash, or a move's own wind-down). */
    static boolean busy(State s, long now) {
        return s.chargeSince >= 0 || s.fistSince >= 0 || s.shakeSince >= 0 || now < s.busyUntil || now < s.moveUntil;
    }

    // ------------------------------------------------------------------------------------------------------ where things are

    /** The ground's flat way of the look (never zero). */
    static Vec3 flat(ServerPlayer player) {
        Vec3 look = player.m_20154_();
        Vec3 flat = new Vec3(look.f_82479_, 0, look.f_82481_);
        return flat.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.m_82541_();
    }

    /** The punching fist, out in front of the right shoulder at about the eye's height. */
    static Vec3 fist(ServerPlayer player) {
        Vec3 look = player.m_20154_();
        Vec3 side = look.m_82537_(new Vec3(0, 1, 0));
        side = side.m_82556_() < 1.0E-6 ? new Vec3(1, 0, 0) : side.m_82541_();
        return player.m_146892_().m_82549_(look.m_82490_(1.0)).m_82549_(side.m_82490_(0.3)).m_82520_(0.0, -0.25, 0.0);
    }

    static Vec3 centre(ServerPlayer player) {
        return player.m_20182_().m_82520_(0.0, 0.9, 0.0);
    }

    /** The ground under {@code at} (from {@code up} over it to {@code down} under it), or null where there is none. */
    static Vec3 ground(ServerLevel level, Vec3 at, double up, double down) {
        BlockHitResult hit = level.m_45547_(new ClipContext(at.m_82520_(0, up, 0), at.m_82520_(0, -down, 0), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, null));
        return hit.m_6662_() == HitResult.Type.BLOCK ? hit.m_82450_() : null;
    }

    /**
     * {@code body} knocked down (ragdolled) where it comes to rest after being thrown: on the ground after its first two ticks, or after
     * two seconds, for the rest of {@code stun} ticks (HakiFalls, as Blast Breath's blast and the Light fruit's kick do).
     */
    static void downOnLanding(ServerLevel level, LivingEntity body, int stun) {
        long start = level.m_46467_();
        LIVE.add(new UoMoves.Live() {
            int age;

            @Override
            public boolean tick(long now) {
                this.age++;
                if (!body.m_6084_()) {
                    return true;
                }
                if (this.age > 2 && body.m_20096_() || this.age >= 40) {
                    HakiFalls.knockDown(level, body, (int) Math.max(30L, stun - (now - start)), false);
                    return true;
                }
                return false;
            }

            @Override
            public ServerLevel level() {
                return level;
            }
        });
    }

    /**
     * A shock out of {@code from} along {@code way} (unit): every foe within {@code reach} and {@code degrees} of it struck as the shock
     * reaches them ({@link #SHOCK_SPEED}), thrown away from the fist and along the way by {@code knock}, knocked down where they land
     * when {@code down}. Returns the bodies it will strike (nearest first).
     */
    static List<LivingEntity> shock(ServerPlayer player, Vec3 from, Vec3 way, double reach, double degrees, float damage, double knock,
                                    boolean down, Set<Integer> skip) {
        double cos = Math.cos(Math.toRadians(degrees));
        ServerLevel level = (ServerLevel) player.m_9236_();
        List<LivingEntity> struck = new ArrayList<>();
        for (LivingEntity body : PikaMoves.foes(player, from.m_82549_(way.m_82490_(reach * 0.5)), reach * 0.5 + 2.0)) {
            if (skip != null && skip.contains(body.m_19879_())) {
                continue;
            }
            Vec3 rel = StyleCombat.middle(body).m_82546_(from);
            double d = rel.m_82553_();
            if (d > reach + body.m_20205_() * 0.5 || d > 0.8 && rel.m_82490_(1.0 / d).m_82526_(way) < cos) {
                continue;
            }
            struck.add(body);
        }
        struck.sort((a, b) -> Double.compare(a.m_20182_().m_82557_(from), b.m_20182_().m_82557_(from)));
        for (LivingEntity body : struck) {
            Vec3 rel = StyleCombat.middle(body).m_82546_(from);
            double d = rel.m_82553_();
            Vec3 out = new Vec3(rel.f_82479_, 0, rel.f_82481_);
            out = out.m_82556_() < 1.0E-6 ? way : out.m_82541_();
            Vec3 push = out.m_82490_(0.5).m_82549_(way.m_82490_(0.5));
            Vec3 thrown = push.m_82490_(knock).m_82520_(0, 0.3 + 0.25 * knock, 0);
            later(player, (int) (d / SHOCK_SPEED), () -> {
                if (body.m_6084_() && PikaMoves.hit(player, body, damage, thrown, knock > 1.5) && down) {
                    downOnLanding(level, body, STUN);
                }
            });
        }
        return struck;
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    static void press(ServerPlayer player, int slot) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || player.m_20159_()) {
            return;
        }
        if (slot == GuraKit.SHIMA.slot()) {
            shake(player, s, now);
        } else if (slot == GuraKit.KABUTOWARI.slot()) {
            kabutowari(player, s, now);
        } else if (slot == GuraKit.TSUNAMI.slot()) {
            tsunami(player, s, now);
        } else if (slot == GuraKit.GEKISHIN.slot()) {
            fistCharge(player, s, now);
        } else if (slot == GuraKit.SPLITTER.slot()) {
            split(player, s, now);
        }
    }

    static void release(ServerPlayer player, int slot) {
        State s = state(player);
        if (slot == GuraKit.SHIMA.slot() && s.shakeSince >= 0) {
            shakeOff(player, s);
        } else if (slot == GuraKit.GEKISHIN.slot() && s.fistSince >= 0) {
            gekishin(player, s);
        }
    }

    // ------------------------------------------------------------------------------------------------------ RMB: Kaishin

    static void use(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || !ready(player, GuraKit.KAISHIN)) {
            return;
        }
        s.chargeSince = now;
        animate(player, "gura_charge");
        fx(player, BUBBLE_ON, 0);
    }

    static void useUp(ServerPlayer player) {
        State s = state(player);
        if (s.chargeSince < 0) {
            return;
        }
        long held = now(player) - s.chargeSince;
        s.chargeSince = -1;
        fx(player, BUBBLE_OFF, 0);
        if (held < TAP) {
            punch(player, s);
        } else {
            kaishin(player, s, (float) Math.min(1.0, held / (double) KAISHIN_FULL));
        }
    }

    /**
     * Kaishin tapped (refs 01, 07, 15): a punch into the air, the bubble flashing on the fist, the air cracking like glass where it
     * strikes and a shock racing out {@link #TAP_REACH} blocks ahead, throwing whoever it meets.
     */
    static void punch(ServerPlayer player, State s) {
        cool(player, GuraKit.KAISHIN, 16);
        s.busyUntil = now(player) + 6;
        animate(player, "gura_punch");
        later(player, 2, () -> {
            Vec3 look = player.m_20154_();
            Vec3 fist = fist(player);
            fx(player, CRACK, 0, fist.m_82549_(look.m_82490_(0.5)), look, 0, 0, 0, 1.5, ++seeds);
            fx(player, SHOCK, 0, fist, look, 0.45, 0.35, 0, TAP_REACH, ++seeds);
            shock(player, fist, look, TAP_REACH, 26.0, 5.0F, 1.6, false, null);
            Impact.quake((ServerLevel) player.m_9236_(), fist, 18.0, 0.35F, 8);
            sound(player, fist, GuraSounds.BOOM, 1.3F, 1.2F);
            sound(player, fist, GuraSounds.CRACK, 0.9F, 1.1F);
        });
    }

    /**
     * Kaishin held (refs 00, 02, 52): the quake gathered on the drawn-back fist ({@code charge} 0 to 1), let go: the air shatters, the
     * shock goes {@link #KAISHIN_REACH} to 40 blocks, everyone in it thrown far (knocked down from half a charge), the ground under its
     * way torn into craters as it passes (from a third of a charge), an impact frame at a full charge.
     */
    static void kaishin(ServerPlayer player, State s, float charge) {
        cool(player, GuraKit.KAISHIN, 40 + (int) (60 * charge));
        s.busyUntil = now(player) + 12;
        animate(player, "gura_release");
        later(player, 1, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 look = player.m_20154_();
            Vec3 fist = fist(player);
            double reach = KAISHIN_REACH + KAISHIN_MORE * charge;
            fx(player, CRACK, 1, fist.m_82549_(look.m_82490_(0.7)), look, 0, 0, 0, 2.0 + 3.0 * charge, ++seeds);
            fx(player, SHOCK, 1, fist, look, 0.36 + 0.12 * charge, 0.5 + 0.5 * charge, 0, reach, ++seeds);
            List<LivingEntity> struck = shock(player, fist, look, reach, 24.0 + 6.0 * charge, 6.0F + 10.0F * charge, 2.0 + 3.0 * charge,
                charge >= 0.5F, null);
            if (charge >= 0.99F && !struck.isEmpty()) {
                LivingEntity first = struck.get(0);
                Vec3 at = StyleCombat.middle(first);
                Vec3 flat = flat(player);
                later(player, (int) (at.m_82554_(fist) / SHOCK_SPEED), () -> PieceNetwork.around(player, new com.hexhaki.piece.network.S2CClash(
                    ++frames, at.f_82479_, at.f_82480_, at.f_82481_, (float) flat.f_82479_, (float) flat.f_82481_, 2.4F, 0, 0,
                    player.m_19879_(), first.m_19879_(), 0)));
            }
            if (charge >= 0.3F) {
                // the ground under the shock's way torn up as it passes (self-repairing craters)
                Vec3 flat = flat(player);
                int craters = 0;
                for (double d = 5.0; d < reach && craters < 5; d += 7.0) {
                    Vec3 over = fist.m_82549_(look.m_82490_(d));
                    Vec3 g = ground(level, over, 1.0, 4.0);
                    if (g == null) {
                        continue;
                    }
                    craters++;
                    double radius = 1.2 + 1.3 * charge;
                    int seed = ++seeds;
                    later(player, (int) (d / SHOCK_SPEED), () -> {
                        Craters.open(level, player, g, radius);
                        fx(player, QUAKE, 0, g, flat, 0.4 + 0.4 * charge, 0, 0, radius * 1.8, seed);
                    });
                }
            }
            Impact.quake(level, fist, 30.0 + 20.0 * charge, 0.6F + 0.4F * charge, 14);
            sound(player, fist, GuraSounds.BOOM, 2.0F, 0.95F - 0.15F * charge);
            sound(player, fist, GuraSounds.CRACK, 1.4F, 0.9F);
            if (charge >= 0.6F) {
                sound(player, fist, GuraSounds.QUAKE, 1.6F, 1.0F);
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ Z: Shima Yurashi

    /**
     * Shima Yurashi (refs 47, 48): both arms thrown out to the sides, the air cracking at both fists, and he takes hold of the air;
     * while held (up to {@link #SHAKE_MAX} ticks) the ground quakes {@link #SHAKE_RADIUS} blocks round: every view shakes and tilts
     * (client/gura/GuraFx), foes stagger every {@link #SHAKE_EVERY} ticks; the player walks slowly.
     */
    static void shake(ServerPlayer player, State s, long now) {
        if (!ready(player, GuraKit.SHIMA)) {
            return;
        }
        s.shakeSince = now;
        animate(player, "gura_shake");
        fx(player, SHAKE_ON, 0, centre(player), Vec3.f_82478_, 0, 0, 0, SHAKE_RADIUS, SHAKE_MAX);
        sound(player, player.m_20182_(), GuraSounds.BOOM, 1.4F, 1.1F);
        later(player, 4, () -> {
            if (s.shakeSince < 0) {
                return;
            }
            Vec3 flat = flat(player);
            Vec3 right = new Vec3(-flat.f_82481_, 0, flat.f_82479_);
            Vec3 chest = player.m_20182_().m_82520_(0, 1.45, 0).m_82549_(flat.m_82490_(0.25));
            fx(player, CRACK, 2, chest.m_82549_(right.m_82490_(1.25)), right, 0, 0, 0, 1.7, ++seeds);
            fx(player, CRACK, 2, chest.m_82549_(right.m_82490_(-1.25)), right.m_82490_(-1), 0, 0, 0, 1.7, ++seeds);
            sound(player, chest, GuraSounds.CRACK, 1.2F, 0.95F);
        });
    }

    static void shakeTick(ServerPlayer player, State s, long now) {
        long held = now - s.shakeSince;
        Vec3 v = player.m_20184_();
        player.m_20334_(v.f_82479_ * 0.5, v.f_82480_, v.f_82481_ * 0.5);
        if (held >= SHAKE_MAX) {
            shakeOff(player, s);
            return;
        }
        if (held < 6 || (held - 6) % SHAKE_EVERY != 0) {
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 c = player.m_20182_();
        float grip = (float) Math.min(1.0, held / 60.0);
        Impact.quake(level, c, SHAKE_RADIUS * 1.5, 0.3F + 0.4F * grip, 12);
        for (LivingEntity body : PikaMoves.foes(player, c, SHAKE_RADIUS)) {
            if (!body.m_20096_()) {
                continue;
            }
            // the ground lurching under them: a stagger off their feet
            double a = level.m_213780_().m_188500_() * Math.PI * 2.0;
            body.m_20334_(Math.cos(a) * 0.18, 0.28 + 0.12 * grip, Math.sin(a) * 0.18);
            body.f_19864_ = true;
            body.f_19802_ = 0;
            body.m_6469_(StyleCombat.of(player), 1.0F);
        }
    }

    /** Let go: he pulls the air down, the island tilts: everyone in range thrown out and knocked down, the biggest lurch of every view. */
    static void shakeOff(ServerPlayer player, State s) {
        long held = now(player) - s.shakeSince;
        s.shakeSince = -1;
        float power = (float) Math.max(0.25, Math.min(1.0, held / 60.0));
        cool(player, GuraKit.SHIMA, 140 + (int) held);
        s.busyUntil = now(player) + 10;
        animate(player, "gura_tilt");
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 c = player.m_20182_();
        fx(player, SHAKE_OFF, 0, centre(player), Vec3.f_82478_, power, 0, 0, SHAKE_RADIUS, ++seeds);
        for (LivingEntity body : PikaMoves.foes(player, c, SHAKE_RADIUS)) {
            Vec3 away = body.m_20182_().m_82546_(c);
            away = new Vec3(away.f_82479_, 0, away.f_82481_);
            away = away.m_82556_() < 1.0E-6 ? flat(player) : away.m_82541_();
            Vec3 thrown = away.m_82490_(1.4 + 2.2 * power).m_82520_(0, 0.7 + 0.6 * power, 0);
            int delay = (int) (body.m_20182_().m_82554_(c) / SHOCK_SPEED);
            later(player, delay, () -> {
                if (body.m_6084_() && PikaMoves.hit(player, body, 4.0F + 8.0F * power, thrown, true)) {
                    downOnLanding(level, body, STUN);
                }
            });
        }
        Impact.quake(level, c, SHAKE_RADIUS * 2.0, 1.0F, 24);
        sound(player, c, GuraSounds.QUAKE, 2.2F, 0.9F);
        sound(player, c, GuraSounds.BOOM, 2.0F, 0.8F);
    }

    // ------------------------------------------------------------------------------------------------------ X: Kabutowari

    /**
     * Kabutowari (refs 04, 06, 32, 52): a leap at the one looked at (the player's own client flies the arc, client/gura/GuraFx) and
     * the quake fist brought down on their head as he lands: the air cracked over them, slammed into the ground, a crater with its rings,
     * everyone near thrown up. No one aimed at: he dives fist first into the ground where he looks (Blackbeard onto Law).
     */
    static void kabutowari(ServerPlayer player, State s, long now) {
        if (!ready(player, GuraKit.KABUTOWARI)) {
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        LivingEntity target = PikaMoves.aimed(player, LEAP_RANGE, 0.06);
        Vec3 from = player.m_20182_();
        Vec3 to;
        Vec3 way;
        if (target != null) {
            Vec3 t = target.m_20182_();
            way = new Vec3(t.f_82479_ - from.f_82479_, 0, t.f_82481_ - from.f_82481_);
            way = way.m_82556_() < 1.0E-6 ? flat(player) : way.m_82541_();
            to = PikaMoves.room(player, t, t.m_82546_(way.m_82490_(target.m_20205_() * 0.5 + 0.7)));
        } else {
            way = flat(player);
            Vec3 aim = PikaMoves.point(player, LEAP_RANGE).m_82546_(player.m_20154_().m_82490_(0.6));
            Vec3 g = ground(level, aim, 1.0, 8.0);
            to = PikaMoves.room(player, from, g != null ? g : aim.m_82520_(0, -0.9, 0));
        }
        cool(player, GuraKit.KABUTOWARI, 120);
        double across = Math.sqrt(Math.pow(to.f_82479_ - from.f_82479_, 2) + Math.pow(to.f_82481_ - from.f_82481_, 2));
        int ticks = Math.max(LEAP_LEAST, Math.min(LEAP_MOST, (int) Math.ceil(across / LEAP_SPEED)));
        double arc = 2.5 + across * 0.18 + Math.max(0.0, to.f_82480_ - from.f_82480_);
        s.moveUntil = now + ticks + 3;
        s.busyUntil = now + ticks + 8;
        player.f_19789_ = 0.0F;
        animate(player, "gura_leap");
        fx(player, LEAP, target != null ? target.m_19879_() : -1, to, from, arc, 0, 0, 0, ticks);
        sound(player, from, GuraSounds.WHOOSH, 1.2F, 0.85F);
        later(player, ticks - 3, () -> animate(player, "gura_slam"));
        Vec3 aimWay = way;
        later(player, ticks, () -> slam(player, target, aimWay));
    }

    /** Kabutowari landing: the fist down on them (or into the ground), the crater, the rings, everyone near thrown up. */
    static void slam(ServerPlayer player, LivingEntity target, Vec3 way) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 feet = player.m_20182_();
        Vec3 g = ground(level, feet.m_82549_(way.m_82490_(0.8)), 1.0, 4.0);
        Vec3 spot = g != null ? g : feet;
        Set<Integer> skip = new HashSet<>();
        if (target != null && target.m_6084_() && target.m_20182_().m_82554_(feet) < 4.5) {
            skip.add(target.m_19879_());
            Vec3 head = target.m_20182_().m_82520_(0, target.m_20206_() + 0.35, 0);
            fx(player, CRACK, 3, head, player.m_20154_().m_82490_(-1), 0, 0, 0, 2.4, ++seeds);
            if (PikaMoves.hit(player, target, 12.0F, new Vec3(way.f_82479_ * 0.2, -1.6, way.f_82481_ * 0.2), true)) {
                downOnLanding(level, target, STUN + 20);
            }
            Vec3 tg = ground(level, target.m_20182_(), 1.0, 4.0);
            if (tg != null) {
                spot = tg;
            }
        } else {
            fx(player, CRACK, 3, spot.m_82520_(0, 0.6, 0).m_82549_(way.m_82490_(0.2)), new Vec3(0, 1, 0), 0, 0, 0, 2.0, ++seeds);
        }
        Craters.open(level, player, spot, 2.6);
        fx(player, QUAKE, 1, spot, way, 1.0, 0, 0, 7.0, ++seeds);
        for (LivingEntity body : PikaMoves.foes(player, spot, 6.5)) {
            if (skip.contains(body.m_19879_())) {
                continue;
            }
            Vec3 out = body.m_20182_().m_82546_(spot);
            out = new Vec3(out.f_82479_, 0, out.f_82481_);
            out = out.m_82556_() < 1.0E-6 ? way : out.m_82541_();
            PikaMoves.hit(player, body, 5.0F, out.m_82490_(0.7).m_82520_(0, 1.1, 0), false);
        }
        Impact.quake(level, spot, 36.0, 1.0F, 18);
        sound(player, spot, GuraSounds.BOOM, 2.2F, 0.85F);
        sound(player, spot, GuraSounds.QUAKE, 1.6F, 1.05F);
        sound(player, spot, GuraSounds.CRUMBLE, 1.2F, 1.0F);
    }

    // ------------------------------------------------------------------------------------------------------ C: the tsunami

    /**
     * The tsunami (refs 29, 51): both fists driven out, a wall of water rises ahead ({@link #TSUNAMI_WIDTH} wide, {@link #TSUNAMI_HEIGHT}
     * high, in {@link #TSUNAMI_RISE} ticks) and rolls on at {@link #TSUNAMI_SPEED} blocks a tick for {@link #TSUNAMI_LIFE} ticks,
     * carrying everyone it meets; where it breaks they are dumped down and knocked down. Drawn, never real water: nothing is flooded.
     */
    static void tsunami(ServerPlayer player, State s, long now) {
        if (!ready(player, GuraKit.TSUNAMI)) {
            return;
        }
        cool(player, GuraKit.TSUNAMI, 220);
        s.busyUntil = now + 14;
        animate(player, "gura_tsunami");
        later(player, 4, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 flat = flat(player);
            Vec3 start = player.m_20182_().m_82549_(flat.m_82490_(3.0));
            Vec3 g = ground(level, start, 3.0, 8.0);
            if (g != null) {
                start = g;
            }
            Vec3 step = flat.m_82490_(TSUNAMI_SPEED);
            int seed = ++seeds;
            fx(player, TSUNAMI, seed, start, step, TSUNAMI_HEIGHT, 0, 0, TSUNAMI_WIDTH, TSUNAMI_LIFE);
            sound(player, start, GuraSounds.WAVE, 2.4F, 1.0F);
            sound(player, player.m_20182_(), GuraSounds.BOOM, 1.4F, 1.1F);
            LIVE.add(new Wave(level, player, start, step, flat));
        });
    }

    /** The tsunami rolling on: whoever its face meets is caught and carried; where it breaks, everyone it carried is dumped down. */
    static final class Wave implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final Vec3 start;
        final Vec3 step;
        final Vec3 flat;
        final Vec3 side;
        final List<LivingEntity> caught = new ArrayList<>();
        double groundY;
        int age;

        Wave(ServerLevel level, ServerPlayer owner, Vec3 start, Vec3 step, Vec3 flat) {
            this.level = level;
            this.owner = owner;
            this.start = start;
            this.step = step;
            this.flat = flat;
            this.side = new Vec3(-flat.f_82481_, 0, flat.f_82479_);
            this.groundY = start.f_82480_;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            this.age++;
            if (this.age > TSUNAMI_LIFE || !this.owner.m_6084_()) {
                for (LivingEntity body : this.caught) {
                    if (body.m_6084_()) {
                        body.m_20334_(this.step.f_82479_ * 0.5, -0.8, this.step.f_82481_ * 0.5);
                        body.f_19864_ = true;
                        downOnLanding(this.level, body, 60);
                    }
                }
                return true;
            }
            Vec3 front = this.start.m_82549_(this.step.m_82490_(this.age));
            Vec3 g = ground(this.level, new Vec3(front.f_82479_, this.groundY, front.f_82481_), 6.0, 10.0);
            if (g != null) {
                this.groundY += (g.f_82480_ - this.groundY) * 0.5;
            }
            double height = TSUNAMI_HEIGHT * Math.min(1.0, this.age / (double) TSUNAMI_RISE);
            Vec3 middle = new Vec3(front.f_82479_, this.groundY + height * 0.5, front.f_82481_);
            for (LivingEntity body : PikaMoves.foes(this.owner, middle, TSUNAMI_WIDTH * 0.5 + 2.0)) {
                if (this.caught.contains(body)) {
                    continue;
                }
                Vec3 rel = body.m_20182_().m_82546_(front);
                double along = rel.m_82526_(this.flat);
                double lateral = rel.m_82526_(this.side);
                double up = body.m_20186_() - this.groundY;
                if (Math.abs(lateral) <= TSUNAMI_WIDTH * 0.5 && along > -3.0 && along < 1.5 && up > -2.0 && up < height + 1.0) {
                    this.caught.add(body);
                    body.f_19802_ = 0;
                    body.m_6469_(StyleCombat.of(this.owner), 4.0F);
                }
            }
            for (LivingEntity body : this.caught) {
                if (!body.m_6084_()) {
                    continue;
                }
                double up = body.m_20186_() - this.groundY;
                double lift = up < height * 0.6 ? 0.2 : up < height ? 0.04 : -0.1;
                body.m_20334_(this.step.f_82479_, lift, this.step.f_82481_);
                body.f_19864_ = true;
                body.f_19789_ = 0.0F;
                if (this.age % 8 == 0) {
                    body.f_19802_ = 0;
                    body.m_6469_(StyleCombat.of(this.owner), 1.0F);
                }
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ V: Gekishin

    static void fistCharge(ServerPlayer player, State s, long now) {
        if (!ready(player, GuraKit.GEKISHIN)) {
            return;
        }
        s.fistSince = now;
        animate(player, "gura_fist_charge");
        fx(player, BUBBLE_ON, 1);
    }

    /**
     * Gekishin (refs 00, 11, 14, 53): the bubble gathered on the drawn-back fist, let go: a dash to just in front of the one looked at
     * (up to {@link #GEKISHIN_RANGE} blocks; the player's own client carries the body) and the bubble driven point-blank into them: an
     * impact frame, the shock bursting out of their back (the air cracked behind them, whoever stands there struck too), and they are
     * sent flying through every block up to iron (the Light fruit's PikaMoves.Launch). No one there: the punch cracks the air ahead.
     */
    static void gekishin(ServerPlayer player, State s) {
        long now = now(player);
        long held = now - s.fistSince;
        s.fistSince = -1;
        fx(player, BUBBLE_OFF, 1);
        float charge = (float) Math.min(1.0, held / (double) GEKISHIN_FULL);
        cool(player, GuraKit.GEKISHIN, 120 + (int) (60 * charge));
        s.busyUntil = now + 10;
        LivingEntity target = PikaMoves.aimed(player, GEKISHIN_RANGE, 0.06);
        if (target == null) {
            animate(player, "gura_gekishin");
            later(player, 1, () -> {
                Vec3 look = player.m_20154_();
                Vec3 fist = fist(player);
                double reach = 12.0 + 10.0 * charge;
                fx(player, CRACK, 1, fist.m_82549_(look.m_82490_(0.6)), look, 0, 0, 0, 2.0 + 2.0 * charge, ++seeds);
                fx(player, SHOCK, 1, fist, look, 0.3, 0.6 + 0.4 * charge, 0, reach, ++seeds);
                shock(player, fist, look, reach, 18.0, 7.0F + 8.0F * charge, 2.2 + 2.0 * charge, charge >= 0.5F, null);
                Impact.quake((ServerLevel) player.m_9236_(), fist, 24.0, 0.6F, 12);
                sound(player, fist, GuraSounds.BOOM, 1.8F, 0.95F);
                sound(player, fist, GuraSounds.CRACK, 1.2F, 1.0F);
            });
            return;
        }
        Vec3 from = player.m_20182_();
        Vec3 t = target.m_20182_();
        Vec3 way = new Vec3(t.f_82479_ - from.f_82479_, 0, t.f_82481_ - from.f_82481_);
        way = way.m_82556_() < 1.0E-6 ? flat(player) : way.m_82541_();
        double lift = Math.max(0.0, target.m_20206_() * 0.6 - 1.2);
        Vec3 to = PikaMoves.room(player, t, t.m_82546_(way.m_82490_(target.m_20205_() * 0.5 + 0.9)).m_82520_(0, lift, 0));
        int ticks = Math.max(2, (int) Math.ceil(to.m_82554_(from) / GEKISHIN_DASH));
        int hold = 6;
        s.moveUntil = now + ticks + hold;
        s.busyUntil = now + ticks + hold + 2;
        player.f_19789_ = 0.0F;
        fx(player, DASH, target.m_19879_(), to, from, hold, 0, 0, 0, ticks);
        sound(player, from, GuraSounds.WHOOSH, 1.0F, 1.2F);
        Vec3 aim = way;
        later(player, ticks - 1, () -> animate(player, "gura_gekishin"));
        later(player, ticks, () -> {
            if (target.m_6084_()) {
                gekishinLands(player, target, aim, charge);
            }
        });
    }

    static void gekishinLands(ServerPlayer player, LivingEntity target, Vec3 way, float charge) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        double speed = GEKISHIN_SPEED * (0.6 + 0.4 * charge);
        Vec3 thrown = way.m_82490_(speed).m_82520_(0, 0.45, 0);
        Vec3 struck = StyleCombat.middle(target);
        if (!PikaMoves.hit(player, target, 9.0F + 13.0F * charge, thrown, true)) {
            return;
        }
        PieceNetwork.around(player, new com.hexhaki.piece.network.S2CClash(++frames, struck.f_82479_, struck.f_82480_, struck.f_82481_,
            (float) way.f_82479_, (float) way.f_82481_, 1.6F + 1.0F * charge, 0, 0, player.m_19879_(), target.m_19879_(), 0));
        Vec3 behind = struck.m_82549_(way.m_82490_(target.m_20205_() * 0.5 + 0.9));
        fx(player, CRACK, 4, behind, way, 0, 0, 0, 2.2 + 2.0 * charge, ++seeds);
        fx(player, SHOCK, 2, struck, way, 0.28, 0.6 + 0.4 * charge, 0, 10.0 + 8.0 * charge, ++seeds);
        Set<Integer> skip = new HashSet<>();
        skip.add(target.m_19879_());
        shock(player, struck, way, 10.0 + 8.0 * charge, 16.0, 4.0F + 5.0F * charge, 1.8 + 1.4 * charge, false, skip);
        Impact.quake(level, struck, 26.0 + 16.0 * charge, 0.7F + 0.3F * charge, 14);
        sound(player, struck, GuraSounds.BOOM, 2.0F, 0.9F);
        sound(player, struck, GuraSounds.HIT, 1.6F, 0.9F);
        sound(player, struck, GuraSounds.CRACK, 1.2F, 1.0F);
        PikaMoves.Launch launch = new PikaMoves.Launch(level, player, target, way, speed);
        launch.stopped = (owner, at) -> {
            fx(owner, QUAKE, 2, at, way, 0.35, 0, 0, 2.4, ++seeds);
            sound(owner, at, GuraSounds.HIT, 1.4F, 0.75F);
            sound(owner, at, GuraSounds.CRUMBLE, 1.0F, 1.1F);
        };
        LIVE.add(launch);
    }

    // ------------------------------------------------------------------------------------------------------ B: the Ground Splitter

    /**
     * The Ground Splitter (refs 28, 37, 41, 55): both fists into the ground, and a rift runs out ahead {@link #RIFT_LENGTH} blocks,
     * opening {@link #RIFT_STEP} blocks a tick along the ground's own lie (torn out through the self-repairing Scars, back in 15 to 25 s,
     * never dropped), everyone along it thrown into the air and knocked down.
     */
    static void split(ServerPlayer player, State s, long now) {
        if (!ready(player, GuraKit.SPLITTER)) {
            return;
        }
        cool(player, GuraKit.SPLITTER, 180);
        s.busyUntil = now + 16;
        animate(player, "gura_split");
        later(player, 4, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 flat = flat(player);
            Vec3 start = player.m_20182_().m_82549_(flat.m_82490_(1.4));
            Vec3 g = ground(level, start, 2.0, 6.0);
            if (g != null) {
                start = g;
            }
            int ticks = (int) Math.ceil(RIFT_LENGTH / RIFT_STEP);
            fx(player, RIFT, ++seeds, start, flat, 0, 0, 0, RIFT_LENGTH, ticks);
            fx(player, QUAKE, 3, start, flat, 0.6, 0, 0, 3.5, ++seeds);
            Impact.quake(level, start, 30.0, 0.8F, 14);
            sound(player, start, GuraSounds.QUAKE, 2.0F, 1.0F);
            sound(player, start, GuraSounds.CRUMBLE, 1.8F, 0.9F);
            LIVE.add(new Rift(level, player, start, flat));
        });
    }

    /** The rift opening along the ground, a few blocks a tick. */
    static final class Rift implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final Vec3 start;
        final Vec3 flat;
        final Vec3 side;
        final Set<Integer> struck = new HashSet<>();
        final List<double[]> supports = new ArrayList<>();
        double y;
        int age;
        int torn;

        Rift(ServerLevel level, ServerPlayer owner, Vec3 start, Vec3 flat) {
            this.level = level;
            this.owner = owner;
            this.start = start;
            this.flat = flat;
            this.side = new Vec3(-flat.f_82481_, 0, flat.f_82479_);
            this.y = start.f_82480_;
            Vec3 end = start.m_82549_(flat.m_82490_(RIFT_LENGTH));
            for (net.minecraft.world.entity.decoration.HangingEntity hanging : level.m_6443_(
                net.minecraft.world.entity.decoration.HangingEntity.class, UoMoves.box(start, end).m_82400_(Scars.HANGING_CLEARANCE + 3.0),
                e -> true)) {
                Vec3 p = hanging.m_20182_();
                this.supports.add(new double[] {p.f_82479_, p.f_82480_, p.f_82481_});
            }
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            this.age++;
            double d0 = (this.age - 1) * RIFT_STEP;
            double d1 = Math.min(RIFT_LENGTH, this.age * RIFT_STEP);
            boolean griefing = this.level.m_46469_().m_46207_(net.minecraft.world.level.GameRules.f_46132_);
            Scars scars = Scars.of(this.level);
            for (double d = d0; d < d1; d += 1.0) {
                Vec3 p = this.start.m_82549_(this.flat.m_82490_(d));
                Vec3 g = ground(this.level, new Vec3(p.f_82479_, this.y, p.f_82481_), 4.0, 8.0);
                if (g == null) {
                    continue;
                }
                this.y = g.f_82480_;
                if (griefing) {
                    int top = (int) Math.floor(g.f_82480_ - 0.01);
                    for (int depth = 0; depth < 3; depth++) {
                        int wide = depth < 2 ? 1 : 0;
                        for (int o = -wide; o <= wide && this.torn < RIFT_MOST_BLOCKS && scars.room(); o++) {
                            Vec3 q = g.m_82549_(this.side.m_82490_(o * 0.9));
                            net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos((int) Math.floor(q.f_82479_), top - depth,
                                (int) Math.floor(q.f_82481_));
                            if (scars.tear(this.owner, pos, this.supports, -1L) == Scars.TORN) {
                                this.torn++;
                            }
                        }
                    }
                }
                for (LivingEntity body : PikaMoves.foes(this.owner, g.m_82520_(0, 1.0, 0), 2.6)) {
                    if (!this.struck.add(body.m_19879_())) {
                        continue;
                    }
                    double which = body.m_20182_().m_82546_(g).m_82526_(this.side) >= 0 ? 1.0 : -1.0;
                    Vec3 thrown = this.side.m_82490_(0.5 * which).m_82549_(this.flat.m_82490_(0.2)).m_82520_(0, 1.35, 0);
                    if (PikaMoves.hit(this.owner, body, 7.0F, thrown, true)) {
                        downOnLanding(this.level, body, STUN);
                    }
                }
            }
            if (this.age % 3 == 0) {
                Vec3 p = this.start.m_82549_(this.flat.m_82490_(d1));
                Impact.quake(this.level, new Vec3(p.f_82479_, this.y, p.f_82481_), 20.0, 0.5F, 8);
            }
            if (this.age % 4 == 0) {
                Vec3 p = this.start.m_82549_(this.flat.m_82490_(d1));
                sound(this.owner, new Vec3(p.f_82479_, this.y, p.f_82481_), GuraSounds.CRUMBLE, 1.2F, 0.9F + 0.04F * (this.age % 5));
            }
            return d1 >= RIFT_LENGTH || !this.owner.m_6084_();
        }
    }

    // ------------------------------------------------------------------------------------------------------ the passive: landing quakes

    /**
     * The landing quake (the owner, asked: "Add it"; ref 50, the foot set down): a hard landing (a fall of {@link #TREMOR_FALL} blocks
     * and more) quakes the ground round the feet, a ring that throws back whoever stands near; no fall damage ever (GuraKit.immune).
     */
    static void tremor(ServerPlayer player, State s, float fall) {
        long now = now(player);
        if (now - s.tremorAt < TREMOR_EVERY) {
            return;
        }
        s.tremorAt = now;
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 feet = player.m_20182_();
        double strength = Math.min(1.0, fall / 20.0);
        double radius = Math.min(7.0, 2.5 + fall * 0.2);
        fx(player, TREMOR, 0, feet, Vec3.f_82478_, 0, 0, 0, radius, ++seeds);
        for (LivingEntity body : PikaMoves.foes(player, feet, radius)) {
            Vec3 out = body.m_20182_().m_82546_(feet);
            out = new Vec3(out.f_82479_, 0, out.f_82481_);
            out = out.m_82556_() < 1.0E-6 ? flat(player) : out.m_82541_();
            PikaMoves.hit(player, body, (float) Math.min(8.0, 1.0 + fall * 0.3), out.m_82490_(0.6 + 0.8 * strength).m_82520_(0, 0.35, 0), false);
        }
        Impact.quake(level, feet, radius * 3.0, (float) (0.3 + 0.5 * strength), 10);
        sound(player, feet, GuraSounds.BOOM, (float) (0.7 + 0.8 * strength), 1.25F - 0.25F * (float) strength);
    }

    // ------------------------------------------------------------------------------------------------------ the clock

    static void tick(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (now < s.moveUntil) {
            player.f_19789_ = 0.0F; // a leap or a dash of its own: no landing quake from it (its own slam is the quake)
        }
        boolean onGround = player.m_20096_();
        if (onGround && s.lastFall >= TREMOR_FALL && now >= s.moveUntil && !player.m_20072_()) {
            tremor(player, s, s.lastFall);
        }
        s.lastFall = onGround ? 0.0F : player.f_19789_;
        if (s.shakeSince >= 0) {
            shakeTick(player, s, now);
        }
        if (s.chargeSince >= 0 && now - s.chargeSince > KAISHIN_FULL + 40) {
            useUp(player);
        }
        if (s.fistSince >= 0 && now - s.fistSince > GEKISHIN_FULL + 40) {
            gekishin(player, s);
        }
    }

    /** The fruit set aside or lost: nothing of it keeps going. */
    static void forget(ServerPlayer player) {
        State s = STATES.remove(player.m_20148_());
        if (s == null) {
            return;
        }
        if (s.chargeSince >= 0) {
            fx(player, BUBBLE_OFF, 0);
        }
        if (s.fistSince >= 0) {
            fx(player, BUBBLE_OFF, 1);
        }
        if (s.shakeSince >= 0) {
            fx(player, SHAKE_OFF, -1, centre(player), Vec3.f_82478_, 0, 0, 0, 0, 0);
        }
    }

    static void clear() {
        STATES.clear();
        LIVE.clear();
    }

    @SubscribeEvent
    public static void guraTick(TickEvent.LevelTickEvent event) {
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
    }

    /** Whether {@code body} is one of the player's foes (the original's rule). */
    static boolean foe(ServerPlayer player, LivingEntity body) {
        return FruitUtil.foe(player, body);
    }
}
