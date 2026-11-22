package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.fx.Impact;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.magu.MaguSounds;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.1.3: the Magu Magu no Mi's moves (server/MaguKit), as Akainu uses them in the references (docs/handoff/magu-ref). Every hit, burn and
 * throw is worked out here on the server's ticks, in step with its animation (tools/magu_anims.py keeps the same beats); what everyone
 * sees is one small {@link S2CUoFx} (kinds {@link #FIST} to {@link #TOUCH}, 100 to 119) and every client builds the magma from it
 * (client/magu/MaguFx, magu/MaguArt). Nothing becomes real lava: the burning ground is drawn and its burns are worked out here, and the
 * world's blocks are only ever torn out through the self-repairing Craters.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class MaguMoves {
    // ---- what the client draws (S2CUoFx kinds 100 to 119 are the Magma fruit's)
    /** A fist of magma flying: x, y, z from, a its step a tick, size its radius, ticks its flight, id its seed. */
    public static final int FIST = 100;
    /** Magma bursting: x, y, z, size its radius, bx its strength (0 to 1), id its seed. */
    public static final int BURST = 101;
    /** The ground burning: x, y, z on the ground, size its radius, ticks its life, id its seed. */
    public static final int POOL = 102;
    /** Dai Funka's fist swelling (user), and let go. */
    public static final int SWELL_ON = 103;
    public static final int SWELL_OFF = 104;
    /** A meteor of magma falling: x, y, z from, a where it lands, size its radius, ticks its fall, id its seed. */
    public static final int METEOR = 105;
    /** Ryusei Kazan's fist punched into the sky (user): x, y, z from. */
    public static final int SKY = 106;
    /** The hound of Inugami Guren: x, y, z its start on the ground, a its step a tick, size its scale, ticks its life, id its seed. */
    public static final int HOUND = 107;
    /** Bakuretsu Kazan: the ground boiling round x, y, z, size its radius, ticks its life, id its seed. */
    public static final int ERUPT = 108;
    /** One geyser of magma: x, y, z its foot, size its height, id its seed. */
    public static final int GEYSER = 109;
    /** Magma Flow (user): x, y, z where the feet arrive, a where they left, ticks it takes, id its seed. */
    public static final int FLOW = 110;
    /** Meigo's lunge (user): x, y, z where the feet arrive, a where they left, id whom it follows, ticks it takes, bx the hold after. */
    public static final int DASH = 111;
    /** Meigo's fist through them: x, y, z the struck middle, a the fist's way, size its strength, id its seed. */
    public static final int PIERCE = 112;
    /** A bare hand burned on the magma body: x, y, z. */
    public static final int TOUCH = 113;

    // ---- numbers
    /** Dai Funka: a tap shorter than this (ticks), a full charge, the small fist's and the giant fist's speeds (blocks a tick), reach. */
    static final int TAP = 6;
    static final int FUNKA_FULL = 40;
    static final double SHOT_SPEED = 2.4;
    static final double GIANT_SPEED = 1.7;
    static final double FUNKA_RANGE = 40.0;
    /** Meigo: its reach, the lunge's speed (blocks a tick). */
    static final double MEIGO_RANGE = 16.0;
    static final double MEIGO_SPEED = 2.6;
    /** Ryusei Kazan: the most it is held, a meteor every this many ticks after the wind-up, their fall, where they land, how wide. */
    static final int RAIN_MAX = 80;
    static final int RAIN_WINDUP = 8;
    static final int RAIN_EVERY = 4;
    public static final int METEOR_FALL = 12;
    static final double RAIN_RANGE = 40.0;
    static final double RAIN_SPREAD = 8.0;
    /** Inugami Guren: the hound's speed (blocks a tick), life (ticks), size. */
    public static final double HOUND_SPEED = 1.3;
    public static final int HOUND_LIFE = 24;
    public static final double HOUND_SCALE = 2.2;
    /** Bakuretsu Kazan: the boiling ground's radius, life (ticks), a geyser every this many ticks while it erupts, for how long. */
    public static final double ERUPT_RADIUS = 9.0;
    public static final int ERUPT_LIFE = 100;
    static final int GEYSER_EVERY = 5;
    static final int GEYSERS_FOR = 45;
    public static final double GEYSER_HEIGHT = 7.0;
    /** Magma Flow: how far, how fast (blocks a tick). */
    static final double FLOW_RANGE = 18.0;
    static final double FLOW_SPEED = 1.6;
    /** Burning ground: how often it burns those in it (ticks), and the most patches alive at once. */
    static final int POOL_EVERY = 10;
    static final int MOST_POOLS = 24;

    /** Each player's state of the kit. */
    static final class State {
        long busyUntil;
        long chargeSince = -1;
        long rainSince = -1;
        long moveUntil;
    }

    /** A patch of burning ground: where, how wide, until when. */
    record Pool(ServerLevel level, ServerPlayer owner, Vec3 at, double radius, long until) {
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final List<UoMoves.Live> LIVE = new ArrayList<>();
    private static final List<Pool> POOLS = new ArrayList<>();
    private static int seeds;
    private static int frames = (1 << 30) + (1 << 29);

    private MaguMoves() {
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

    static boolean busy(State s, long now) {
        return s.chargeSince >= 0 || s.rainSince >= 0 || now < s.busyUntil || now < s.moveUntil;
    }

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

    /** The solid ground under {@code at} (from {@code up} over it to {@code down} under it), or null. */
    static Vec3 ground(ServerLevel level, Vec3 at, double up, double down) {
        BlockHitResult hit = level.m_45547_(new ClipContext(at.m_82520_(0, up, 0), at.m_82520_(0, -down, 0), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, null));
        return hit.m_6662_() == HitResult.Type.BLOCK ? hit.m_82450_() : null;
    }

    /** Burned: damage and set alight for {@code seconds}. */
    static boolean burn(ServerPlayer player, LivingEntity body, float damage, Vec3 thrown, boolean heavy, int seconds) {
        body.m_20254_(seconds);
        return thrown != null ? PikaMoves.hit(player, body, damage, thrown, heavy) : hurt(player, body, damage);
    }

    static boolean hurt(ServerPlayer player, LivingEntity body, float damage) {
        body.f_19802_ = 0;
        return body.m_6469_(StyleCombat.of(player), damage);
    }

    /** Magma bursting at {@code at}: the blast (fire, a throw), what is seen, the burning ground under it. */
    static void erupt(ServerPlayer player, Vec3 at, Vec3 way, double radius, float damage, int fire, double knock, double pool, int poolLife) {
        erupt(player, at, way, radius, damage, fire, knock, pool, poolLife, 0);
    }

    /** As above; {@code ended}: the seed of the flying fist or meteor that burst here (the clients stop drawing it), 0 none. */
    static void erupt(ServerPlayer player, Vec3 at, Vec3 way, double radius, float damage, int fire, double knock, double pool, int poolLife,
                      int ended) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        UoMoves.explode(player, level, at, way, radius, damage, fire, knock);
        fx(player, BURST, ++seeds, at, way, Math.min(1.0, radius / 8.0), ended, 0, radius, 0);
        if (pool > 0.0) {
            pool(player, at, pool, poolLife);
        }
    }

    /** A patch of burning ground under {@code at} (if there is ground within a few blocks): drawn, and burning whoever stands in it. */
    static void pool(ServerPlayer player, Vec3 at, double radius, int life) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 g = ground(level, at, 1.0, 4.0);
        if (g == null) {
            return;
        }
        if (POOLS.size() >= MOST_POOLS) {
            POOLS.remove(0);
        }
        POOLS.add(new Pool(level, player, g, radius, level.m_46467_() + life));
        fx(player, POOL, ++seeds, g, Vec3.f_82478_, 0, 0, 0, radius, life);
    }

    /** Every foe within {@code reach} of the way from {@code a} to {@code b}. */
    static List<LivingEntity> along(ServerPlayer player, Vec3 a, Vec3 b, double reach) {
        return PikaMoves.crossed(player, a, b, reach);
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    static void press(ServerPlayer player, int slot) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || player.m_20159_()) {
            return;
        }
        if (slot == MaguKit.MEIGO.slot()) {
            meigo(player, s, now);
        } else if (slot == MaguKit.RYUSEI.slot()) {
            rain(player, s, now);
        } else if (slot == MaguKit.INUGAMI.slot()) {
            hound(player, s, now);
        } else if (slot == MaguKit.BAKURETSU.slot()) {
            bakuretsu(player, s, now);
        } else if (slot == MaguKit.FLOW.slot()) {
            flow(player, s, now);
        }
    }

    static void release(ServerPlayer player, int slot) {
        State s = state(player);
        if (slot == MaguKit.RYUSEI.slot() && s.rainSince >= 0) {
            stopRain(player, s);
        }
    }

    // ------------------------------------------------------------------------------------------------------ RMB: Dai Funka

    static void use(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || !ready(player, MaguKit.DAI_FUNKA)) {
            return;
        }
        s.chargeSince = now;
        animate(player, "magu_charge");
        fx(player, SWELL_ON, 0);
    }

    static void useUp(ServerPlayer player) {
        State s = state(player);
        if (s.chargeSince < 0) {
            return;
        }
        long held = now(player) - s.chargeSince;
        s.chargeSince = -1;
        fx(player, SWELL_OFF, 0);
        if (held < TAP) {
            shot(player, s, 0.0F);
        } else {
            shot(player, s, (float) Math.min(1.0, held / (double) FUNKA_FULL));
        }
    }

    /**
     * Dai Funka (refs 04, 07, 12, 14, 19, 24): tapped ({@code charge} 0), a fist of magma shot out of the fist, a trail of fire and smoke,
     * bursting where it meets a body or a block (a small blast, fire, a little burning ground); held, the fist swells into a giant fist
     * of magma ({@code charge} up to 1) that flies slower and explodes where it lands: a blast 3 to 8 blocks, everyone burned and thrown,
     * a self-repairing crater and the ground burning round it; an impact frame at a full charge.
     */
    static void shot(ServerPlayer player, State s, float charge) {
        boolean giant = charge > 0.0F;
        cool(player, MaguKit.DAI_FUNKA, giant ? 60 + (int) (80 * charge) : 14);
        s.busyUntil = now(player) + (giant ? 12 : 6);
        animate(player, giant ? "magu_release" : "magu_punch");
        later(player, giant ? 1 : 2, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 look = player.m_20154_();
            Vec3 from = fist(player).m_82549_(look.m_82490_(giant ? 0.6 : 0.2));
            double speed = giant ? GIANT_SPEED : SHOT_SPEED;
            double size = giant ? 0.8 + 1.4 * charge : 0.42;
            Vec3 step = look.m_82490_(speed);
            int flight = (int) Math.ceil(FUNKA_RANGE / speed);
            int seed = ++seeds;
            fx(player, FIST, seed, from, step, 0, 0, 0, size, flight);
            sound(player, from, MaguSounds.WHOOSH, giant ? 1.6F : 1.0F, giant ? 0.75F : 1.1F);
            LIVE.add(new UoMoves.Live() {
                int age;
                Vec3 at = from;

                @Override
                public boolean tick(long now) {
                    this.age++;
                    if (!player.m_6084_()) {
                        return true;
                    }
                    Vec3 to = this.at.m_82549_(step);
                    BlockHitResult wall = level.m_45547_(new ClipContext(this.at, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                    Vec3 end = wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_() : to;
                    LivingEntity first = null;
                    double nearest = Double.MAX_VALUE;
                    for (LivingEntity body : along(player, this.at, end, size + 0.5)) {
                        double d = body.m_20182_().m_82557_(this.at);
                        if (d < nearest) {
                            nearest = d;
                            first = body;
                        }
                    }
                    boolean done = first != null || wall.m_6662_() == HitResult.Type.BLOCK || this.age >= flight;
                    this.at = first != null ? StyleCombat.middle(first) : end;
                    if (!done) {
                        return false;
                    }
                    if (giant) {
                        double radius = 3.0 + 5.0 * charge;
                        if (first != null) {
                            burn(player, first, 6.0F + 8.0F * charge, look.m_82490_(1.4 + 1.6 * charge).m_82520_(0, 0.5, 0), true, 8);
                            if (charge >= 0.99F) {
                                Vec3 flat = flat(player);
                                PieceNetwork.around(player, new com.hexhaki.piece.network.S2CClash(++frames, this.at.f_82479_, this.at.f_82480_,
                                    this.at.f_82481_, (float) flat.f_82479_, (float) flat.f_82481_, 2.2F, 0, 0, player.m_19879_(), first.m_19879_(), 0));
                            }
                        }
                        erupt(player, this.at, look, radius, 6.0F + 10.0F * charge, 6 + (int) (6 * charge), 1.6 + 2.4 * charge, 0.0, 0, seed);
                        Vec3 g = ground(level, this.at, 1.5, 5.0);
                        if (g != null) {
                            Craters.open(level, player, g, 1.4 + 2.0 * charge);
                            pool(player, g, 2.0 + 2.5 * charge, 100 + (int) (60 * charge));
                            for (int k = 0; k < (int) (1 + 3 * charge); k++) {
                                double a = Math.PI * 2.0 * k / 3.0 + level.m_213780_().m_188500_();
                                pool(player, g.m_82520_(Math.cos(a) * radius * 0.6, 0, Math.sin(a) * radius * 0.6), 1.4 + charge, 80 + (int) (40 * charge));
                            }
                        }
                        Impact.quake(level, this.at, 24.0 + 24.0 * charge, 0.5F + 0.5F * charge, 14);
                        sound(player, this.at, MaguSounds.BOOM, 2.2F, 0.95F - 0.2F * charge);
                        sound(player, this.at, MaguSounds.SIZZLE, 1.2F, 0.8F);
                    } else {
                        if (first != null) {
                            burn(player, first, 6.0F, look.m_82490_(0.7).m_82520_(0, 0.3, 0), false, 5);
                        }
                        erupt(player, this.at, look, 2.4, 4.0F, 4, 0.8, 1.4, 70, seed);
                        sound(player, this.at, MaguSounds.BURST, 1.3F, 1.1F);
                    }
                    return true;
                }

                @Override
                public ServerLevel level() {
                    return level;
                }
            });
        });
    }

    // ------------------------------------------------------------------------------------------------------ Z: Meigo

    /**
     * Meigo (refs 00, 16, 44): a lunge to just in front of the one looked at (the player's own client carries the body, client/magu/MaguFx)
     * and the magma fist driven straight through them: an impact frame, the magma bursting out of their back, burned for 10 seconds,
     * thrown and knocked down. No one there: the lunge goes 8 blocks where he looks and the fist stabs the air.
     */
    static void meigo(ServerPlayer player, State s, long now) {
        if (!ready(player, MaguKit.MEIGO)) {
            return;
        }
        cool(player, MaguKit.MEIGO, 140);
        LivingEntity target = PikaMoves.aimed(player, MEIGO_RANGE, 0.06);
        Vec3 from = player.m_20182_();
        Vec3 way;
        Vec3 to;
        if (target != null) {
            Vec3 t = target.m_20182_();
            way = new Vec3(t.f_82479_ - from.f_82479_, 0, t.f_82481_ - from.f_82481_);
            way = way.m_82556_() < 1.0E-6 ? flat(player) : way.m_82541_();
            double lift = Math.max(0.0, target.m_20206_() * 0.6 - 1.2);
            to = PikaMoves.room(player, t, t.m_82546_(way.m_82490_(target.m_20205_() * 0.5 + 0.9)).m_82520_(0, lift, 0));
        } else {
            way = flat(player);
            to = PikaMoves.room(player, from, from.m_82549_(way.m_82490_(8.0)));
        }
        int ticks = Math.max(2, (int) Math.ceil(to.m_82554_(from) / MEIGO_SPEED));
        int hold = 6;
        s.moveUntil = now + ticks + hold;
        s.busyUntil = now + ticks + hold + 2;
        player.f_19789_ = 0.0F;
        fx(player, DASH, target != null ? target.m_19879_() : -1, to, from, hold, 0, 0, 0, ticks);
        animate(player, "magu_meigo_wind");
        sound(player, from, MaguSounds.WHOOSH, 1.2F, 0.9F);
        Vec3 aim = way;
        later(player, ticks - 1, () -> animate(player, "magu_meigo"));
        later(player, ticks, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            if (target != null && target.m_6084_()) {
                Vec3 struck = StyleCombat.middle(target);
                if (burn(player, target, 13.0F, aim.m_82490_(2.6).m_82520_(0, 0.5, 0), true, 10)) {
                    PieceNetwork.around(player, new com.hexhaki.piece.network.S2CClash(++frames, struck.f_82479_, struck.f_82480_, struck.f_82481_,
                        (float) aim.f_82479_, (float) aim.f_82481_, 2.0F, 0, 0, player.m_19879_(), target.m_19879_(), 0));
                    GuraMoves.downOnLanding(level, target, 80);
                }
                fx(player, PIERCE, ++seeds, struck, aim, 0, 0, 0, 1.0, 0);
                for (LivingEntity body : along(player, struck, struck.m_82549_(aim.m_82490_(4.0)), 1.4)) {
                    if (body != target) {
                        burn(player, body, 5.0F, aim.m_82490_(1.2).m_82520_(0, 0.3, 0), false, 5);
                    }
                }
                Impact.quake(level, struck, 24.0, 0.7F, 12);
                sound(player, struck, MaguSounds.BURST, 1.6F, 0.85F);
                sound(player, struck, MaguSounds.SIZZLE, 1.3F, 1.0F);
            } else {
                Vec3 ahead = fist(player).m_82549_(aim.m_82490_(0.8));
                fx(player, PIERCE, ++seeds, ahead, aim, 0, 0, 0, 0.7, 0);
                for (LivingEntity body : along(player, ahead, ahead.m_82549_(aim.m_82490_(3.0)), 1.2)) {
                    burn(player, body, 7.0F, aim.m_82490_(1.4).m_82520_(0, 0.3, 0), true, 6);
                }
                sound(player, ahead, MaguSounds.BURST, 1.2F, 1.0F);
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ X: Ryusei Kazan

    /**
     * Ryusei Kazan (refs 17, 18, 20): a fist of magma punched up into the sky (magu_sky), and while X is held (up to {@link #RAIN_MAX}
     * ticks) fists of magma fall like meteors where he looks, one every {@link #RAIN_EVERY} ticks, {@link #RAIN_SPREAD} blocks round,
     * each bursting into fire and leaving a little burning ground.
     */
    static void rain(ServerPlayer player, State s, long now) {
        if (!ready(player, MaguKit.RYUSEI)) {
            return;
        }
        s.rainSince = now;
        animate(player, "magu_sky");
        later(player, 3, () -> {
            Vec3 from = player.m_20182_().m_82520_(0, 2.2, 0);
            fx(player, SKY, ++seeds, from, Vec3.f_82478_, 0, 0, 0, 0.6, 0);
            sound(player, from, MaguSounds.WHOOSH, 1.4F, 0.7F);
        });
    }

    static void rainTick(ServerPlayer player, State s, long now) {
        long held = now - s.rainSince;
        if (held > RAIN_MAX) {
            stopRain(player, s);
            return;
        }
        if (held < RAIN_WINDUP || (held - RAIN_WINDUP) % RAIN_EVERY != 0) {
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 centre = PikaMoves.point(player, RAIN_RANGE);
        double a = level.m_213780_().m_188500_() * Math.PI * 2.0;
        double r = RAIN_SPREAD * Math.sqrt(level.m_213780_().m_188500_());
        Vec3 aim = centre.m_82520_(Math.cos(a) * r, 0, Math.sin(a) * r);
        Vec3 g = ground(level, aim, 6.0, 12.0);
        Vec3 land = g != null ? g : aim;
        Vec3 from = land.m_82520_((level.m_213780_().m_188500_() - 0.5) * 10.0, 28.0, (level.m_213780_().m_188500_() - 0.5) * 10.0);
        int seed = ++seeds;
        fx(player, METEOR, seed, from, land, 0, 0, 0, 0.9, METEOR_FALL);
        later(player, METEOR_FALL, () -> {
            erupt(player, land.m_82520_(0, 0.4, 0), new Vec3(0, 1, 0), 2.8, 5.0F, 4, 0.9, 1.6, 60);
            Impact.quake(level, land, 16.0, 0.35F, 8);
            sound(player, land, MaguSounds.BURST, 1.1F, 0.9F + level.m_213780_().m_188501_() * 0.25F);
        });
    }

    static void stopRain(ServerPlayer player, State s) {
        long held = now(player) - s.rainSince;
        s.rainSince = -1;
        cool(player, MaguKit.RYUSEI, 120 + (int) Math.min(RAIN_MAX, held) * 2);
        animate(player, "magu_sky_end");
    }

    // ------------------------------------------------------------------------------------------------------ C: Inugami Guren

    /**
     * Inugami Guren (ref 01, the hounds of magma blooming out of him): both arms thrust out and a giant hound's head of magma surges out
     * along the ground ({@link #HOUND_SPEED} blocks a tick for {@link #HOUND_LIFE} ticks), jaws snapping: whoever it bites is burned and
     * carried in its jaws; at the end it bursts into a flood of magma (a blast, the ground burning) and they are thrown down.
     */
    static void hound(ServerPlayer player, State s, long now) {
        if (!ready(player, MaguKit.INUGAMI)) {
            return;
        }
        cool(player, MaguKit.INUGAMI, 200);
        s.busyUntil = now + 12;
        animate(player, "magu_hound");
        later(player, 3, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 flat = flat(player);
            Vec3 start = player.m_20182_().m_82549_(flat.m_82490_(2.0));
            Vec3 g = ground(level, start, 2.0, 6.0);
            if (g != null) {
                start = g;
            }
            Vec3 step = flat.m_82490_(HOUND_SPEED);
            int seed = ++seeds;
            fx(player, HOUND, seed, start, step, 0, 0, 0, HOUND_SCALE, HOUND_LIFE);
            sound(player, start, MaguSounds.ROAR, 2.0F, 0.85F);
            Vec3 origin = start;
            List<LivingEntity> caught = new ArrayList<>();
            LIVE.add(new UoMoves.Live() {
                int age;
                double y = origin.f_82480_;

                @Override
                public boolean tick(long t) {
                    this.age++;
                    Vec3 front = origin.m_82549_(step.m_82490_(this.age));
                    Vec3 under = ground(level, new Vec3(front.f_82479_, this.y, front.f_82481_), 4.0, 8.0);
                    if (under != null) {
                        this.y += (under.f_82480_ - this.y) * 0.5;
                    }
                    Vec3 jaws = new Vec3(front.f_82479_, this.y + HOUND_SCALE * 0.9, front.f_82481_);
                    if (this.age > HOUND_LIFE || !player.m_6084_()) {
                        erupt(player, jaws, flat, 4.5, 8.0F, 6, 1.8, 3.0, 100);
                        for (LivingEntity body : caught) {
                            if (body.m_6084_()) {
                                body.m_20334_(flat.f_82479_ * 1.2, 0.6, flat.f_82481_ * 1.2);
                                body.f_19864_ = true;
                                GuraMoves.downOnLanding(level, body, 70);
                            }
                        }
                        Impact.quake(level, jaws, 30.0, 0.8F, 14);
                        sound(player, jaws, MaguSounds.BOOM, 2.0F, 1.0F);
                        return true;
                    }
                    for (LivingEntity body : PikaMoves.foes(player, jaws, HOUND_SCALE * 1.3)) {
                        if (!caught.contains(body)) {
                            caught.add(body);
                            burn(player, body, 6.0F, null, false, 6);
                            sound(player, jaws, MaguSounds.SIZZLE, 1.0F, 0.9F);
                        }
                    }
                    for (LivingEntity body : caught) {
                        if (!body.m_6084_()) {
                            continue;
                        }
                        Vec3 want = jaws.m_82546_(body.m_20182_());
                        body.m_20334_(step.f_82479_ + want.f_82479_ * 0.3, Math.max(0.0, want.f_82480_ * 0.3), step.f_82481_ + want.f_82481_ * 0.3);
                        body.f_19864_ = true;
                        body.f_19789_ = 0.0F;
                        if (this.age % 6 == 0) {
                            hurt(player, body, 2.0F);
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

    // ------------------------------------------------------------------------------------------------------ V: Bakuretsu Kazan

    /**
     * Bakuretsu Kazan (refs 11, 38: the ground under him bubbling magma): both fists into the ground, and the ground round him
     * ({@link #ERUPT_RADIUS}) boils into magma for {@link #ERUPT_LIFE} ticks: for the first {@link #GEYSERS_FOR} a geyser bursts out of it
     * every {@link #GEYSER_EVERY} ticks, throwing up and burning whoever stands there; all the while it burns whoever is in it.
     */
    static void bakuretsu(ServerPlayer player, State s, long now) {
        if (!ready(player, MaguKit.BAKURETSU)) {
            return;
        }
        cool(player, MaguKit.BAKURETSU, 240);
        s.busyUntil = now + 14;
        animate(player, "magu_erupt");
        later(player, 4, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 feet = player.m_20182_();
            Vec3 g = ground(level, feet.m_82520_(0, 0.2, 0), 0.5, 3.0);
            Vec3 centre = g != null ? g : feet;
            fx(player, ERUPT, ++seeds, centre, Vec3.f_82478_, 0, 0, 0, ERUPT_RADIUS, ERUPT_LIFE);
            Impact.quake(level, centre, 30.0, 0.7F, 14);
            sound(player, centre, MaguSounds.BOOM, 1.6F, 1.15F);
            sound(player, centre, MaguSounds.ROAR, 1.8F, 0.8F);
            LIVE.add(new UoMoves.Live() {
                int age;

                @Override
                public boolean tick(long t) {
                    this.age++;
                    if (this.age > ERUPT_LIFE || !player.m_6084_()) {
                        return true;
                    }
                    if (this.age <= GEYSERS_FOR && this.age % GEYSER_EVERY == 0) {
                        double a = level.m_213780_().m_188500_() * Math.PI * 2.0;
                        double r = 2.0 + (ERUPT_RADIUS - 2.5) * Math.sqrt(level.m_213780_().m_188500_());
                        Vec3 spot = centre.m_82520_(Math.cos(a) * r, 0, Math.sin(a) * r);
                        Vec3 sg = ground(level, spot, 3.0, 5.0);
                        Vec3 base = sg != null ? sg : spot;
                        fx(player, GEYSER, ++seeds, base, Vec3.f_82478_, 0, 0, 0, GEYSER_HEIGHT, 0);
                        for (LivingEntity body : PikaMoves.foes(player, base.m_82520_(0, 1.0, 0), 2.0)) {
                            burn(player, body, 5.0F, new Vec3(0, 1.5, 0), true, 5);
                        }
                        sound(player, base, MaguSounds.BURST, 1.2F, 0.8F + level.m_213780_().m_188501_() * 0.3F);
                    }
                    if (this.age % POOL_EVERY == 0) {
                        for (LivingEntity body : PikaMoves.foes(player, centre, ERUPT_RADIUS)) {
                            if (body.m_20096_() && Math.abs(body.m_20186_() - centre.f_82480_) < 2.0) {
                                burn(player, body, 1.5F, null, false, 3);
                            }
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

    // ------------------------------------------------------------------------------------------------------ B: Magma Flow

    /**
     * Magma Flow (refs 38, 39: the body melting into magma, rising out of a sea of fire): melted into a stream of magma (the body hidden,
     * the player's own client carrying it along the ground, client/magu/MaguFx) up to {@link #FLOW_RANGE} blocks where he looks,
     * burning whoever it runs over; at the end he rises out of it in an eruption that throws up those near.
     */
    static void flow(ServerPlayer player, State s, long now) {
        if (!ready(player, MaguKit.FLOW)) {
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 from = player.m_20182_();
        Vec3 way = flat(player);
        Vec3 aim = from.m_82549_(way.m_82490_(FLOW_RANGE)).m_82520_(0, 1.0, 0);
        Vec3 g = ground(level, aim, 3.0, 8.0);
        Vec3 to = PikaMoves.room(player, from, g != null ? g : aim.m_82520_(0, -1.0, 0));
        cool(player, MaguKit.FLOW, 160);
        int ticks = Math.max(3, (int) Math.ceil(to.m_82554_(from) / FLOW_SPEED));
        s.moveUntil = now + ticks + 4;
        s.busyUntil = now + ticks + 8;
        player.f_19789_ = 0.0F;
        int seed = ++seeds;
        fx(player, FLOW, seed, to, from, 0, 0, 0, 0, ticks);
        animate(player, "magu_melt");
        sound(player, from, MaguSounds.SIZZLE, 1.4F, 0.8F);
        sound(player, from, MaguSounds.WHOOSH, 1.0F, 0.7F);
        Set<Integer> burned = new HashSet<>();
        LIVE.add(new UoMoves.Live() {
            int age;
            Vec3 last = from;

            @Override
            public boolean tick(long t) {
                this.age++;
                if (!player.m_6084_()) {
                    return true;
                }
                Vec3 at = player.m_20182_();
                for (LivingEntity body : along(player, this.last, at, 1.6)) {
                    if (burned.add(body.m_19879_())) {
                        burn(player, body, 6.0F, way.m_82490_(0.5).m_82520_(0, 0.5, 0), false, 6);
                    }
                }
                this.last = at;
                if (this.age < ticks) {
                    return false;
                }
                animate(player, "magu_rise");
                erupt(player, at.m_82520_(0, 0.3, 0), new Vec3(0, 1, 0), 3.5, 6.0F, 5, 0.6, 2.2, 80);
                for (LivingEntity body : PikaMoves.foes(player, at, 3.5)) {
                    body.m_20334_(body.m_20184_().f_82479_, 1.2, body.m_20184_().f_82481_);
                    body.f_19864_ = true;
                }
                Impact.quake(level, at, 20.0, 0.5F, 10);
                sound(player, at, MaguSounds.BURST, 1.4F, 0.9F);
                return true;
            }

            @Override
            public ServerLevel level() {
                return level;
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ the body of magma

    /**
     * The passive (the owner: "Add it"): a blow passed through the magma body without Haki (the original's Logia) burns the one who struck
     * it bare-handed (the attacker is the blow itself, near).
     */
    static void dodged(ServerPlayer player, DamageSource source) {
        if (!(source.m_7639_() instanceof LivingEntity attacker) || source.m_7640_() != attacker || attacker.m_20182_().m_82554_(player.m_20182_()) > 5.0) {
            return;
        }
        burn(player, attacker, 3.0F, null, false, 4);
        Vec3 at = StyleCombat.middle(attacker).m_82549_(StyleCombat.middle(player)).m_82490_(0.5);
        fx(player, TOUCH, ++seeds, at, Vec3.f_82478_, 0, 0, 0, 0.6, 0);
        sound(player, at, MaguSounds.SIZZLE, 0.9F, 1.2F);
    }

    // ------------------------------------------------------------------------------------------------------ the clock

    static void tick(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        net.minecraft.world.entity.Entity entity = player;
        if (entity.m_6060_()) {
            LivingEntity body = player;
            body.m_20095_(); // fire cannot hold on magma
        }
        if (s == null) {
            return;
        }
        long now = now(player);
        if (now < s.moveUntil) {
            player.f_19789_ = 0.0F;
        }
        if (s.rainSince >= 0) {
            rainTick(player, s, now);
        }
        if (s.chargeSince >= 0 && now - s.chargeSince > FUNKA_FULL + 40) {
            useUp(player);
        }
    }

    static void forget(ServerPlayer player) {
        State s = STATES.remove(player.m_20148_());
        if (s == null) {
            return;
        }
        if (s.chargeSince >= 0) {
            fx(player, SWELL_OFF, 0);
        }
    }

    static void clear() {
        STATES.clear();
        LIVE.clear();
        POOLS.clear();
    }

    /** The world's tick: every move's lives, and the burning ground burning whoever stands in it. */
    @SubscribeEvent
    public static void maguTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || LIVE.isEmpty() && POOLS.isEmpty()) {
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
        if (now % POOL_EVERY == 0 && !POOLS.isEmpty()) {
            List<Pool> pools = new ArrayList<>(POOLS);
            for (Pool pool : pools) {
                if (pool.level() != level) {
                    continue;
                }
                if (now >= pool.until() || !pool.owner().m_6084_()) {
                    POOLS.remove(pool);
                    continue;
                }
                for (LivingEntity body : PikaMoves.foes(pool.owner(), pool.at(), pool.radius())) {
                    if (Math.abs(body.m_20186_() - pool.at().f_82480_) < 1.2) {
                        burn(pool.owner(), body, 1.5F, null, false, 3);
                    }
                }
            }
        }
    }
}
