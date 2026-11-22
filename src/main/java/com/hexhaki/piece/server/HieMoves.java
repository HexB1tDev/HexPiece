package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.fx.Impact;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.hie.HieArt;
import com.hexhaki.piece.hie.HieSounds;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CClash;
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
 * 3.1.9: the Hie Hie no Mi's moves (server/HieKit), as Kuzan uses them in the references (docs/handoff/hie-ref). Every hit and freeze is
 * worked out here on the server's ticks, in step with its animation (tools/hie_anims.py keeps the same beats); what everyone sees is one
 * small {@link S2CUoFx} (kinds {@link #FREEZE} to {@link #CHILL}, 120 to 159) and every client builds the ice from it (client/hie/HieFx,
 * hie/HieArt). The ground under Ice Age and the road turns to real ice for a while (server/Scars puts it back); the bodies frozen solid
 * (server/HieFreeze) and the Ice Ball are drawn crystal (3.2.2: real ice blocks melted into water).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class HieMoves {
    // ---- what the client draws (S2CUoFx kinds 120 to 139 are the Ice fruit's)
    /** A body frozen solid (id: the body; user: whose ice): x, y, z its feet, bx its height, by 1 inside an Ice Ball, size its half width, ticks held. */
    public static final int FREEZE = 120;
    /** A frozen body let go (id: the body): x, y, z its middle, by 1 shattered, bz 1 it was inside a ball, size the shatter's. */
    public static final int THAW = 121;
    /** A spear of ice flying: x, y, z from (its middle), a its step a tick, bz the slot it left + 1 (0 none), ticks its flight, id its seed. */
    public static final int SPEAR = 122;
    /** Partisan's spear number id formed in the air round the user. */
    public static final int FORM = 123;
    /** Every spear still round the user gone (let go of, or cut short). */
    public static final int FORM_OFF = 124;
    /** Ice bursting: x, y, z, size its radius, bx its strength (0 to 1), by the seed of what burst here (the clients stop drawing it), id its seed. */
    public static final int BURST = 125;
    /** The Ice Ball's orb gathering between the user's hands for ticks. */
    public static final int GATHER = 126;
    /** The Ice Ball's orb flying: x, y, z from, a its step a tick, size its radius, ticks its flight, id its seed. */
    public static final int ORB = 127;
    /** The Ice Ball's prison: x, y, z its middle, size its radius, ticks its life, by the orb's seed, id its seed. */
    public static final int BALL = 128;
    /** Pheasant Beak: x, y, z its start, a its step a tick, size its scale, ticks its life, id its seed. */
    public static final int PHEASANT = 129;
    /** Ice Age: x, y, z where it started on the ground, a its way (level), size its reach, bx its half width (radians), ticks its life, id. */
    public static final int SHEET = 130;
    /** The ice road: x, y, z from, a to (both on the ground), ticks it takes to grow, bx its life, id its seed. */
    public static final int ROAD = 131;
    /** The user sliding (its own client carries it): x, y, z where the feet arrive, a where they left, id whom it follows, ticks, bx the hold. */
    public static final int SLIDE = 132;
    /** The user's fist coated in ice, and the ice gone. */
    public static final int COAT_ON = 133;
    public static final int COAT_OFF = 134;
    /** The Ice Saber in the user's hand for ticks, and gone. */
    public static final int SABER_ON = 135;
    public static final int SABER_OFF = 136;
    /** A cut of the saber: x, y, z its middle, a its turning axis, bx by bz the look, size its radius, id its seed. */
    public static final int SLASH = 137;
    /** The Logia dodge: the user's body of ice shattering and coming back: x, y, z its middle, size its height. */
    public static final int SCATTER = 138;
    /** Ice shattering where no body is (the Ice Ball's end, the saber let go): x, y, z, size. */
    public static final int SHATTER = 139;
    /**
     * 3.2.2: the frost on a body (id: the body, user: whose cold): x, y, z its feet, bx 1 from the arm (an attacker's own blow), by its
     * height, size how far the frost has spread (0 to 1), ticks how long it stays without another hit. Kinds 140 to 159 are the Ice
     * fruit's too.
     */
    public static final int CHILL = 140;

    // ---- numbers
    /** The N key's ability number (client/FruitKeyN.ABILITY, as GuraUltimate.SLOT: out of the original's quick slots 0 to 8). */
    static final int N_SLOT = 40;
    /** Partisan: a tap shorter than this (ticks); the first spear formed this many ticks in, then one every so many, at most so many. */
    static final int TAP = 6;
    public static final int FORM_FIRST = 4;
    public static final int FORM_EVERY = 5;
    public static final int SPEARS_MOST = 8;
    static final int PARTISAN_HOLD_MOST = 140;
    static final double SPEAR_SPEED = 2.4;
    static final double SPEAR_RANGE = 48.0;
    /** Ice Time: reach, the lunge's speed (blocks a tick), how long they stay frozen. */
    static final double TIME_RANGE = 12.0;
    static final double TIME_SPEED = 2.4;
    /** 3.2.2 (the owner: Ice Time "meant to be one of the most satisfying abilities"): held 3.5 s, then it shatters. */
    static final int TIME_HOLD = 70;
    /** Ice Ball: the gathering (ticks), the orb's speed and reach, the prison's radius and how long it holds. */
    public static final int GATHER_TICKS = 10;
    static final double ORB_SPEED = 1.6;
    static final double ORB_RANGE = 36.0;
    public static final double ORB_RADIUS = 0.35;
    public static final double BALL_RADIUS = 2.6;
    public static final int BALL_HOLD = 80;
    /** Pheasant Beak: its speed (blocks a tick), size and life (ticks). */
    public static final double PHEASANT_SPEED = 1.6;
    public static final double PHEASANT_SCALE = 2.4;
    public static final int PHEASANT_LIFE = 30;
    /** Ice Age: its reach, half its width (radians), how long the ice lies there, how long it holds those it freezes. */
    public static final double AGE_REACH = 26.0;
    public static final double AGE_HALF = Math.toRadians(50.0);
    public static final int AGE_LIFE = 240;
    static final int AGE_HOLD = 80;
    /** The ice road: reach, the slide's speed (blocks a tick), how long the road lies there. */
    static final double ROAD_RANGE = 22.0;
    static final double ROAD_SPEED = 1.8;
    public static final int ROAD_LIFE = 160;
    /**
     * 3.1.9 (the owner, asked: "Real ice ground"): how long the frozen ground (Ice Age's, the road's) stays before it is put back (ticks).
     * 3.2.2 (the owner: "Ice World is lasting way too long"): 8 s, was 20.
     */
    public static final int GROUND_BACK = 160;
    /**
     * 3.2.2 (the owner: "it's meant to freeze everyone, like, in there"; "anything that comes in there should freeze"): while Ice Age's
     * ground lies there, every so many ticks everyone in its cone who is not frozen (and did not just thaw) freezes for so long.
     */
    static final int AGE_FIELD_EVERY = 4;
    static final int AGE_FIELD_HOLD = 60;
    /** Ice Age's ridges of ice: along this many lines out from where it started (and here and there). */
    static final int RIDGES = 5;
    /** The Ice Saber: how long it lasts (ticks), a cut at most this often, its reach. */
    public static final int SABER_TIME = 400;
    static final int CUT_EVERY = 9;
    static final double CUT_REACH = 4.2;

    /** Each player's state of the kit. */
    static final class State {
        long busyUntil;
        long chargeSince = -1;
        int formed;
        long moveUntil;
        long saberUntil = -1;
        long cutNext;
        int cuts;
        long scatterNext;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final List<UoMoves.Live> LIVE = new ArrayList<>();
    private static int seeds;
    private static int frames = (1 << 30) + (1 << 29) + (1 << 27);

    private HieMoves() {
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
        // 3.2.0: and the fruit energy for it (server/FruitEnergy)
        return move.cooldown() == null || FruitData.ready(player, move.cooldown()) && FruitEnergy.afford(player, move);
    }

    static void cool(ServerPlayer player, FruitMove move, int ticks) {
        FruitEnergy.spend(player, move, ticks); // 3.2.0: the move began: its energy is spent
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

    /** Whether {@code player} holds the Ice Saber now (its left click cuts, it does not punch). */
    static boolean saberOut(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        return s != null && s.saberUntil >= 0;
    }

    /** A blow on purpose on {@code body}: a body {@code player} froze is smashed; any other is frosted {@code hits}. */
    static void cold(ServerPlayer player, LivingEntity body, int hits) {
        if (!HieFreeze.smash(player, body)) {
            HieFreeze.frost(player, body, hits, false);
        }
    }

    static boolean busy(State s, long now) {
        return s.chargeSince >= 0 || now < s.busyUntil || now < s.moveUntil;
    }

    static Vec3 flat(ServerPlayer player) {
        return MaguMoves.flat(player);
    }

    static Vec3 fist(ServerPlayer player) {
        return MaguMoves.fist(player);
    }

    static boolean hurt(ServerPlayer player, LivingEntity body, float damage) {
        return MaguMoves.hurt(player, body, damage);
    }

    /** Where the aim is: the middle of the foe under the crosshair, or where the look meets a block, within {@code range}. */
    static Vec3 aim(ServerPlayer player, double range) {
        LivingEntity target = PikaMoves.aimed(player, range, 0.05);
        return target != null ? StyleCombat.middle(target) : PikaMoves.point(player, range);
    }

    /** Where Partisan's {@code k}th spear hangs round {@code player} (hie/HieArt.slot, turned with the player's look). */
    static Vec3 slot(ServerPlayer player, int k) {
        double[] s = HieArt.slot(k);
        Vec3 f = flat(player);
        Vec3 right = new Vec3(-f.f_82481_, 0, f.f_82479_);
        return player.m_20182_().m_82549_(right.m_82490_(s[0])).m_82520_(0, s[1], 0).m_82549_(f.m_82490_(-s[2]));
    }

    /** A burst of ice at {@code at} (seen by everyone near), {@code ended} the seed of what burst there (0 none). */
    static void burst(ServerPlayer player, Vec3 at, double radius, double strength, int ended) {
        fx(player, BURST, ++seeds, at, Vec3.f_82478_, strength, ended, 0, radius, 0);
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    static void press(ServerPlayer player, int slot) {
        State s = state(player);
        long now = now(player);
        if (slot == N_SLOT && s.saberUntil >= 0) {
            saberOff(player, s, true); // the saber let go of, whatever else is going on
            return;
        }
        if (busy(s, now) || player.m_20159_()) {
            return;
        }
        if (slot == N_SLOT) {
            saber(player, s, now);
            return;
        }
        if (slot == HieKit.ICE_TIME.slot()) {
            iceTime(player, s, now);
        } else if (slot == HieKit.ICE_BALL.slot()) {
            iceBall(player, s, now);
        } else if (slot == HieKit.PHEASANT.slot()) {
            pheasant(player, s, now);
        } else if (slot == HieKit.ICE_AGE.slot()) {
            iceAge(player, s, now);
        } else if (slot == HieKit.ICE_ROAD.slot()) {
            iceRoad(player, s, now);
        }
    }

    static void release(ServerPlayer player, int slot) {
        // no held quick key: only the use button is held (Partisan)
    }

    /** A move began: a drawn saber goes first (it would sit in the fist through the other moves). */
    static boolean begin(ServerPlayer player, State s, FruitMove move) {
        if (!ready(player, move)) {
            return false;
        }
        if (s.saberUntil >= 0) {
            saberOff(player, s, true);
        }
        return true;
    }

    // ------------------------------------------------------------------------------------------------------ RMB: Ice Block: Partisan

    static void use(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || !begin(player, s, HieKit.PARTISAN)) {
            return;
        }
        s.chargeSince = now;
        s.formed = 0;
        animate(player, "hie_partisan_raise");
    }

    /** While the use button is held: one more spear of ice in the air round him every {@link #FORM_EVERY} ticks (ref 62). */
    static void partisanTick(ServerPlayer player, State s, long now) {
        long held = now - s.chargeSince;
        if (held > PARTISAN_HOLD_MOST) {
            useUp(player);
            return;
        }
        if (s.formed < SPEARS_MOST && held >= FORM_FIRST && (held - FORM_FIRST) % FORM_EVERY == 0) {
            fx(player, FORM, s.formed);
            sound(player, slot(player, s.formed), HieSounds.FORM, 0.55F, 1.25F + 0.04F * s.formed);
            s.formed++;
        }
    }

    static void useUp(ServerPlayer player) {
        State s = state(player);
        if (s.chargeSince < 0) {
            return;
        }
        long held = now(player) - s.chargeSince;
        int formed = s.formed;
        s.chargeSince = -1;
        s.formed = 0;
        if (held < TAP || formed == 0) {
            // a tap (or nothing formed yet): one spear thrown out of the hand
            fx(player, FORM_OFF, 0);
            cool(player, HieKit.PARTISAN, 14);
            s.busyUntil = now(player) + 6;
            animate(player, "hie_partisan_throw");
            later(player, 2, () -> {
                Vec3 from = fist(player).m_82549_(player.m_20154_().m_82490_(0.6));
                spear(player, from, aim(player, SPEAR_RANGE), 0, 6.0F);
                sound(player, from, HieSounds.FORM, 0.9F, 1.4F);
            });
            return;
        }
        // held: every spear formed flies at the aim, one a tick
        cool(player, HieKit.PARTISAN, 40 + 8 * formed);
        s.busyUntil = now(player) + 8;
        animate(player, "hie_partisan_launch");
        Vec3 target = aim(player, SPEAR_RANGE);
        for (int k = 0; k < formed; k++) {
            int slot = k;
            later(player, 1 + k, () -> spear(player, slot(player, slot), target, slot + 1, 4.0F));
        }
        later(player, 1 + formed, () -> fx(player, FORM_OFF, 0));
        sound(player, player.m_146892_(), HieSounds.HIT, 1.0F, 0.8F);
    }

    /**
     * One spear of ice from {@code from} at {@code target} ({@link #SPEAR_SPEED} blocks a tick): it strikes the first body on its way
     * ({@code damage}, a little thrown, the frost left in them: HieFreeze.frost; a body he froze shatters) or bursts on the first block; {@code slot} the spear that
     * left the air round him (+1; 0 one out of the hand).
     */
    static void spear(ServerPlayer player, Vec3 from, Vec3 target, int slot, float damage) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 way = target.m_82546_(from);
        way = way.m_82556_() < 1.0E-6 ? player.m_20154_() : way.m_82541_();
        Vec3 step = way.m_82490_(SPEAR_SPEED);
        int flight = (int) Math.ceil(SPEAR_RANGE / SPEAR_SPEED);
        int seed = ++seeds;
        fx(player, SPEAR, seed, from, step, 0, 0, slot, HieArt.SPEAR_LENGTH, flight);
        Vec3 dir = way;
        LIVE.add(new UoMoves.Live() {
            int age;
            Vec3 at = from;

            @Override
            public boolean tick(long now) {
                this.age++;
                if (!player.m_6084_() || this.age > flight) {
                    return true;
                }
                Vec3 to = this.at.m_82549_(step);
                BlockHitResult wall = level.m_45547_(new ClipContext(this.at, to.m_82549_(dir.m_82490_(HieArt.SPEAR_LENGTH * 0.5)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                Vec3 end = wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_() : to;
                LivingEntity first = null;
                double nearest = Double.MAX_VALUE;
                for (LivingEntity body : PikaMoves.crossed(player, this.at, end, 0.5)) {
                    double d = body.m_20182_().m_82557_(this.at);
                    if (d < nearest) {
                        nearest = d;
                        first = body;
                    }
                }
                if (first != null) {
                    Vec3 struck = StyleCombat.middle(first);
                    cold(player, first, 1);
                    PikaMoves.hit(player, first, damage, dir.m_82490_(0.4).m_82520_(0, 0.15, 0), false);
                    burst(player, struck, 0.9, 0.3, seed);
                    ConquerorAura.exploded(player, level, struck, 1.2);
                    sound(player, struck, HieSounds.HIT, 0.9F, 1.0F + level.m_213780_().m_188501_() * 0.2F);
                    return true;
                }
                if (wall.m_6662_() == HitResult.Type.BLOCK) {
                    burst(player, end, 0.8, 0.2, seed);
                    sound(player, end, HieSounds.HIT, 0.6F, 1.2F);
                    return true;
                }
                this.at = to;
                return false;
            }

            @Override
            public ServerLevel level() {
                return level;
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ Z: Ice Time

    /**
     * Ice Time (refs 47, 51, 65, 77, 78): a lunge to just in front of the one looked at (the player's own client carries the body,
     * client/hie/HieFx) and he takes hold of them: they freeze solid in an instant ({@link #TIME_HOLD} ticks). No one there: a short lunge
     * and whoever is within reach of the hand freezes.
     */
    static void iceTime(ServerPlayer player, State s, long now) {
        if (!begin(player, s, HieKit.ICE_TIME)) {
            return;
        }
        cool(player, HieKit.ICE_TIME, 160);
        LivingEntity target = PikaMoves.aimed(player, TIME_RANGE, 0.06);
        Vec3 from = player.m_20182_();
        Vec3 way;
        Vec3 to;
        if (target != null) {
            Vec3 t = target.m_20182_();
            way = new Vec3(t.f_82479_ - from.f_82479_, 0, t.f_82481_ - from.f_82481_);
            way = way.m_82556_() < 1.0E-6 ? flat(player) : way.m_82541_();
            to = PikaMoves.room(player, t, t.m_82546_(way.m_82490_(target.m_20205_() * 0.5 + 0.85)));
        } else {
            way = flat(player);
            to = PikaMoves.room(player, from, from.m_82549_(way.m_82490_(6.0)));
        }
        int ticks = Math.max(2, (int) Math.ceil(to.m_82554_(from) / TIME_SPEED));
        int hold = 8;
        s.moveUntil = now + ticks + hold;
        s.busyUntil = now + ticks + hold + 2;
        player.f_19789_ = 0.0F;
        fx(player, SLIDE, target != null ? target.m_19879_() : -1, to, from, hold, 0, 0, 0, ticks);
        animate(player, "hie_grab_reach");
        sound(player, from, HieSounds.FORM, 0.9F, 1.1F);
        Vec3 aim = way;
        later(player, ticks - 1, () -> animate(player, "hie_grab"));
        later(player, ticks, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            if (target != null && target.m_6084_() && target.m_20182_().m_82554_(player.m_20182_()) < 4.0) {
                Vec3 struck = StyleCombat.middle(target);
                hurt(player, target, 5.0F);
                HieFreeze.freeze(player, target, TIME_HOLD, false);
                ConquerorAura.mixed(player, struck, 2.5, 7, -1.0);
                Impact.quake(level, struck, 16.0, 0.35F, 8);
            } else {
                Vec3 hand = fist(player).m_82549_(aim.m_82490_(0.6));
                burst(player, hand, 0.7, 0.2, 0);
                for (LivingEntity body : PikaMoves.foes(player, hand, 2.2)) {
                    hurt(player, body, 4.0F);
                    HieFreeze.freeze(player, body, TIME_HOLD - 15, false);
                }
                sound(player, hand, HieSounds.FREEZE, 0.8F, 1.2F);
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ X: Ice Ball

    /**
     * Ice Ball (refs 76, 09, 69: "Ice... Ball!", Garp sealed in a great ball of ice): an orb of ice gathered between the hands for
     * {@link #GATHER_TICKS} ticks and thrown along the look; where it meets a body or a block a ball of ice {@link #BALL_RADIUS} round
     * swallows everyone near, frozen inside it for {@link #BALL_HOLD} ticks, until it cracks and shatters (the Conqueror's lightning out of
     * that shattering, with Haki).
     */
    static void iceBall(ServerPlayer player, State s, long now) {
        if (!begin(player, s, HieKit.ICE_BALL)) {
            return;
        }
        cool(player, HieKit.ICE_BALL, 220);
        s.busyUntil = now + GATHER_TICKS + 8;
        animate(player, "hie_ball_gather");
        fx(player, GATHER, 0, Vec3.f_82478_, Vec3.f_82478_, 0, 0, 0, ORB_RADIUS, GATHER_TICKS);
        sound(player, player.m_146892_(), HieSounds.FORM, 0.8F, 0.8F);
        later(player, GATHER_TICKS, () -> {
            animate(player, "hie_ball_throw");
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 look = player.m_20154_();
            Vec3 from = player.m_146892_().m_82520_(0, -0.45, 0).m_82549_(look.m_82490_(1.0));
            Vec3 step = look.m_82490_(ORB_SPEED);
            int flight = (int) Math.ceil(ORB_RANGE / ORB_SPEED);
            int seed = ++seeds;
            fx(player, ORB, seed, from, step, 0, 0, 0, ORB_RADIUS, flight);
            sound(player, from, HieSounds.HIT, 1.0F, 0.7F);
            LIVE.add(new UoMoves.Live() {
                int age;
                Vec3 at = from;

                @Override
                public boolean tick(long t) {
                    this.age++;
                    if (!player.m_6084_()) {
                        return true;
                    }
                    Vec3 to = this.at.m_82549_(step);
                    BlockHitResult wall = level.m_45547_(new ClipContext(this.at, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                    Vec3 end = wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_() : to;
                    boolean met = !PikaMoves.crossed(player, this.at, end, ORB_RADIUS + 0.5).isEmpty();
                    this.at = end;
                    if (met || wall.m_6662_() == HitResult.Type.BLOCK || this.age >= flight) {
                        prison(player, level, end, seed);
                        return true;
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

    /**
     * The ball of ice where the orb landed: 3.2.2 (the owner: "the ice sometimes melts"; "in a crystal like you had before that then
     * shatters") a ball of cut crystal {@link #BALL_RADIUS} round, sat on the ground under it, drawn by every client (hie/HieArt.prison),
     * everyone in it frozen inside; its cracks run over it before it shatters (the Conqueror's lightning out of that shattering, with Haki).
     * No block of the world changes.
     */
    static void prison(ServerPlayer player, ServerLevel level, Vec3 at, int orb) {
        Vec3 g = MaguMoves.ground(level, at, 1.0, 4.0);
        Vec3 centre = g != null ? g.m_82520_(0, BALL_RADIUS * 0.72, 0) : at;
        for (LivingEntity body : PikaMoves.foes(player, centre, BALL_RADIUS + 0.4)) {
            hurt(player, body, 6.0F);
            HieFreeze.freeze(player, body, BALL_HOLD, true);
        }
        int seed = ++seeds;
        fx(player, BALL, seed, centre, Vec3.f_82478_, 0, orb, 0, BALL_RADIUS, BALL_HOLD);
        Impact.quake(level, centre, 20.0, 0.45F, 10);
        sound(player, centre, HieSounds.FREEZE, 1.6F, 0.75F);
        sound(player, centre, HieSounds.FORM, 1.2F, 0.7F);
        later(player, BALL_HOLD - HieFreeze.CRACK_TICKS, () -> sound(player, centre, HieSounds.CRACK, 1.4F, 0.85F));
        later(player, BALL_HOLD, () -> {
            fx(player, SHATTER, seed, centre, Vec3.f_82478_, 0, 0, 0, BALL_RADIUS * 1.2, 0);
            ConquerorAura.exploded(player, level, centre, BALL_RADIUS * 1.6);
            Impact.quake(level, centre, 24.0, 0.5F, 10);
            sound(player, centre, HieSounds.SHATTER, 1.8F, 0.8F);
        });
    }

    // ------------------------------------------------------------------------------------------------------ the frozen ground

    /**
     * 3.1.9 (the owner: "The ground can also turn into like actual ice blocks"; asked, "Real ice ground"): the column of ground at
     * ({@code x}, {@code z}) near height {@code y} frozen: its top block (the first solid block or still water from 5 over {@code y} to 9
     * under) swapped to packed ice (the sea's surface frozen into ice to walk on; a plant on it torn out first; trees' leaves left), then
     * {@code ridge} blocks of ice stacked on it where no one stands; all put back after {@code back} ticks (server/Scars). Returns the
     * frozen top's height (the block's top), or NaN where nothing froze.
     */
    static double freezeColumn(ServerPlayer owner, ServerLevel level, Scars scars, int x, double y, int z, int ridge, long back) {
        BlockHitResult hit = level.m_45547_(new ClipContext(new Vec3(x + 0.5, y + 5.0, z + 0.5), new Vec3(x + 0.5, y - 9.0, z + 0.5),
            ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null));
        if (hit.m_6662_() != HitResult.Type.BLOCK || !scars.room()) {
            return Double.NaN;
        }
        net.minecraft.core.BlockPos pos = hit.m_82425_();
        net.minecraft.world.level.block.state.BlockState state = level.m_8055_(pos);
        if (state.m_60734_() instanceof net.minecraft.world.level.block.LeavesBlock) {
            return Double.NaN;
        }
        boolean sea = state.m_60734_() == net.minecraft.world.level.block.Blocks.f_49990_;
        net.minecraft.world.level.block.state.BlockState packed = net.minecraft.world.level.block.Blocks.f_50354_.m_49966_();
        if (!sea) {
            net.minecraft.core.BlockPos above = new net.minecraft.core.BlockPos(pos.m_123341_(), pos.m_123342_() + 1, pos.m_123343_());
            if (!level.m_8055_(above).m_60795_() && level.m_8055_(above).m_60819_().m_76178_()) {
                scars.tear(owner, above, List.of(), back);
            }
        }
        if (scars.swap(owner, pos, packed, List.of(), back, sea) != Scars.TORN) {
            return Double.NaN;
        }
        if (ridge > 0) {
            net.minecraft.world.level.block.state.BlockState blue = net.minecraft.world.level.block.Blocks.f_50568_.m_49966_();
            for (int k = 1; k <= ridge; k++) {
                net.minecraft.core.BlockPos up = new net.minecraft.core.BlockPos(pos.m_123341_(), pos.m_123342_() + k, pos.m_123343_());
                if (!level.m_8055_(up).m_60795_() || !level.m_6443_(LivingEntity.class, new net.minecraft.world.phys.AABB(up.m_123341_(), up.m_123342_(),
                    up.m_123343_(), up.m_123341_() + 1.0, up.m_123342_() + 1.0, up.m_123343_() + 1.0), LivingEntity::m_6084_).isEmpty()) {
                    break;
                }
                scars.swap(owner, up, k == ridge ? blue : packed, List.of(), back + 2L * k);
            }
        }
        return pos.m_123342_() + 1.0;
    }

    // ------------------------------------------------------------------------------------------------------ C: Ice Block: Pheasant Beak

    /**
     * Ice Block: Pheasant Beak: a great pheasant of ice ({@link #PHEASANT_SCALE}) flies along the aim ({@link #PHEASANT_SPEED} blocks a tick
     * for {@link #PHEASANT_LIFE} ticks), freezing everyone it passes through, and bursts into spikes of ice where its beak meets a block
     * or at the end of its flight: everyone in the burst frozen too, the Conqueror's lightning out of it with Haki.
     */
    static void pheasant(ServerPlayer player, State s, long now) {
        if (!begin(player, s, HieKit.PHEASANT)) {
            return;
        }
        cool(player, HieKit.PHEASANT, 180);
        s.busyUntil = now + 10;
        animate(player, "hie_pheasant");
        later(player, 3, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 look = player.m_20154_();
            Vec3 chest = player.m_146892_().m_82520_(0, -0.2, 0);
            Vec3 start = chest.m_82549_(look.m_82490_(PHEASANT_SCALE * 1.2));
            BlockHitResult near = level.m_45547_(new ClipContext(chest, start, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
            if (near.m_6662_() == HitResult.Type.BLOCK) {
                start = chest.m_82549_(look.m_82490_(Math.max(0.5, near.m_82450_().m_82554_(chest) - 0.6)));
            }
            Vec3 step = look.m_82490_(PHEASANT_SPEED);
            int seed = ++seeds;
            fx(player, PHEASANT, seed, start, step, 0, 0, 0, PHEASANT_SCALE, PHEASANT_LIFE);
            sound(player, start, HieSounds.FORM, 1.8F, 0.75F);
            sound(player, start, HieSounds.FREEZE, 1.0F, 1.3F);
            Vec3 origin = start;
            Set<Integer> struck = new HashSet<>();
            LIVE.add(new UoMoves.Live() {
                int age;

                @Override
                public boolean tick(long t) {
                    this.age++;
                    Vec3 was = origin.m_82549_(step.m_82490_(this.age - 1));
                    Vec3 body = origin.m_82549_(step.m_82490_(this.age));
                    Vec3 beak = body.m_82549_(look.m_82490_(PHEASANT_SCALE * 1.5));
                    BlockHitResult hit = level.m_45547_(new ClipContext(was.m_82549_(look.m_82490_(PHEASANT_SCALE * 1.5)), beak, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, null));
                    boolean met = hit.m_6662_() == HitResult.Type.BLOCK;
                    for (LivingEntity foe : PikaMoves.foes(player, body, PHEASANT_SCALE * 1.15)) {
                        if (struck.add(foe.m_19879_())) {
                            hurt(player, foe, 7.0F);
                            HieFreeze.freeze(player, foe, 60, false);
                        }
                    }
                    if (met || this.age >= PHEASANT_LIFE || !player.m_6084_()) {
                        Vec3 at = met ? hit.m_82450_().m_82546_(look.m_82490_(0.5)) : beak;
                        UoMoves.explode(player, level, at, look, 4.5, 8.0F, 0, 0.6);
                        burst(player, at, 4.5, 1.0, seed);
                        for (LivingEntity foe : PikaMoves.foes(player, at, 4.5)) {
                            HieFreeze.freeze(player, foe, 50, false);
                        }
                        Impact.quake(level, at, 30.0, 0.7F, 14);
                        sound(player, at, HieSounds.SHATTER, 1.8F, 0.8F);
                        sound(player, at, HieSounds.HIT, 1.4F, 0.7F);
                        return true;
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

    // ------------------------------------------------------------------------------------------------------ V: Ice Age

    /**
     * Ice Age (refs 04, 06, 32, 42, 45, 48): his palm to the ground, and a freezing wave runs out ahead of him in a wide cone
     * ({@link #AGE_REACH} blocks, {@link #AGE_HALF} each side) at HieArt.SHEET_SPEED blocks a tick: the ground and the sea a sheet of ice
     * with jagged ridges (drawn: no block changes), everyone it reaches frozen solid ({@link #AGE_HOLD} ticks), the Conqueror's lightning
     * at each with Haki.
     */
    static void iceAge(ServerPlayer player, State s, long now) {
        if (!begin(player, s, HieKit.ICE_AGE)) {
            return;
        }
        cool(player, HieKit.ICE_AGE, 260);
        s.busyUntil = now + 16;
        animate(player, "hie_ice_age");
        later(player, 5, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 feet = player.m_20182_();
            Vec3 g = MaguMoves.ground(level, feet.m_82520_(0, 0.2, 0), 0.5, 3.0);
            Vec3 origin = g != null ? g : feet;
            Vec3 way = flat(player);
            int seed = ++seeds;
            fx(player, SHEET, seed, origin, way, AGE_HALF, 0, 0, AGE_REACH, AGE_LIFE);
            ConquerorAura.mixed(player, origin.m_82549_(way.m_82490_(0.8)).m_82520_(0, 0.2, 0), 3.0, 8, -0.2);
            Impact.quake(level, origin, 40.0, 0.6F, 20);
            sound(player, origin, HieSounds.AGE, 2.0F, 0.9F);
            sound(player, origin, HieSounds.FREEZE, 1.4F, 0.7F);
            double cos = Math.cos(AGE_HALF);
            Set<Integer> frozen = new HashSet<>();
            boolean griefing = level.m_46469_().m_46207_(net.minecraft.world.level.GameRules.f_46132_);
            // every column of ground in the cone (its middle within the reach and the half width), nearest first, with its ridge
            List<int[]> columns = new ArrayList<>();
            int reach = (int) Math.ceil(AGE_REACH);
            int ox = (int) Math.floor(origin.f_82479_);
            int oz = (int) Math.floor(origin.f_82481_);
            for (int dx = -reach; dx <= reach && griefing; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double cx = ox + dx + 0.5 - origin.f_82479_;
                    double cz = oz + dz + 0.5 - origin.f_82481_;
                    double d = Math.sqrt(cx * cx + cz * cz);
                    if (d > AGE_REACH || d > 1.2 && (cx * way.f_82479_ + cz * way.f_82481_) / d < cos) {
                        continue;
                    }
                    // ridges: along RIDGES lines out from where it started (growing with the distance), and one here and there
                    double side = Math.atan2(cx * way.f_82481_ - cz * way.f_82479_, cx * way.f_82479_ + cz * way.f_82481_);
                    double lane = (side / AGE_HALF * 0.5 + 0.5) * (RIDGES - 1);
                    boolean onLine = Math.abs(lane - Math.round(lane)) * AGE_HALF * 2.0 / (RIDGES - 1) * d < 0.6 && d > 4.0;
                    int hash = (ox + dx) * 73856093 ^ (oz + dz) * 19349663 ^ seed * 83492791;
                    int ridge = onLine && (hash & 3) != 0 ? 1 + (int) Math.min(2, d / 9.0) + ((hash >>> 4) & 1)
                        : (hash >>> 8 & 31) == 0 ? 1 + (hash >>> 13 & 1) : 0;
                    columns.add(new int[] {ox + dx, oz + dz, (int) Math.round(d * 100), ridge});
                }
            }
            columns.sort((a, b) -> Integer.compare(a[2], b[2]));
            Scars scars = Scars.of(level);
            LIVE.add(new UoMoves.Live() {
                int age;
                int next;

                @Override
                public boolean tick(long t) {
                    this.age++;
                    double front = Math.min(AGE_REACH + 1.0, this.age * HieArt.SHEET_SPEED);
                    if (this.age > GROUND_BACK || !player.m_6084_()) {
                        return true;
                    }
                    boolean running = this.age * HieArt.SHEET_SPEED <= AGE_REACH + 2.0;
                    if (!running && this.age % AGE_FIELD_EVERY != 0) {
                        return false;
                    }
                    // the ground freezing as the wave runs over it (at most so many columns a tick: the rest the next)
                    int done = 0;
                    while (this.next < columns.size() && columns.get(this.next)[2] <= front * 100 && done < 140 && scars.room()) {
                        int[] c = columns.get(this.next++);
                        freezeColumn(player, level, scars, c[0], origin.f_82480_, c[1], c[3], GROUND_BACK);
                        done++;
                    }
                    int sparks = 0;
                    for (LivingEntity body : PikaMoves.foes(player, origin, front + 1.0)) {
                        Vec3 p = body.m_20182_();
                        double dx = p.f_82479_ - origin.f_82479_;
                        double dz = p.f_82481_ - origin.f_82481_;
                        double d = Math.sqrt(dx * dx + dz * dz);
                        if (d > front || Math.abs(p.f_82480_ - origin.f_82480_) > 6.0) {
                            continue;
                        }
                        if (d > 1.0 && (dx * way.f_82479_ + dz * way.f_82481_) / d < cos) {
                            continue;
                        }
                        if (frozen.add(body.m_19879_())) {
                            // the wave reaching them: struck and frozen (the lightning out of the first two, with Haki)
                            hurt(player, body, 6.0F);
                            HieFreeze.freeze(player, body, AGE_HOLD, false);
                            if (sparks < 2 && ConquerorAura.mixed(player, StyleCombat.middle(body), 2.2, 6, -1.0)) {
                                sparks++;
                            }
                        } else if (this.age % AGE_FIELD_EVERY == 0 && !HieFreeze.frozen(body) && !HieFreeze.graced(body, t)) {
                            // still in the frozen world (or come into it): frozen again while it lasts
                            HieFreeze.freeze(player, body, AGE_FIELD_HOLD, false);
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

    // ------------------------------------------------------------------------------------------------------ B: Ice Road and the ice fist

    /**
     * The ice road and the ice fist (refs 01, 15, 38: the Garp fight): a road of jagged ice shoots along the ground at the one looked at (or
     * 18 blocks ahead) and he slides along it ({@link #ROAD_SPEED} blocks a tick, his own client carrying him), his fist coated in ice, and
     * drives it into them: an impact frame, thrown back, the cold left in them, a burst of ice (the Conqueror's lightning out of it with
     * Haki).
     */
    static void iceRoad(ServerPlayer player, State s, long now) {
        if (!begin(player, s, HieKit.ICE_ROAD)) {
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        LivingEntity target = PikaMoves.aimed(player, ROAD_RANGE, 0.06);
        Vec3 from = player.m_20182_();
        Vec3 way;
        Vec3 aimAt;
        if (target != null) {
            Vec3 t = target.m_20182_();
            way = new Vec3(t.f_82479_ - from.f_82479_, 0, t.f_82481_ - from.f_82481_);
            way = way.m_82556_() < 1.0E-6 ? flat(player) : way.m_82541_();
            aimAt = t.m_82546_(way.m_82490_(target.m_20205_() * 0.5 + 0.9));
        } else {
            way = flat(player);
            aimAt = from.m_82549_(way.m_82490_(18.0));
        }
        Vec3 g = MaguMoves.ground(level, aimAt.m_82520_(0, 1.5, 0), 3.0, 8.0);
        Vec3 to = PikaMoves.room(player, from, g != null ? g : aimAt);
        cool(player, HieKit.ICE_ROAD, 200);
        int ticks = Math.max(3, (int) Math.ceil(to.m_82554_(from) / ROAD_SPEED));
        int hold = 6;
        s.moveUntil = now + ticks + hold;
        s.busyUntil = now + ticks + hold + 2;
        player.f_19789_ = 0.0F;
        int seed = ++seeds;
        fx(player, ROAD, seed, from, to, ROAD_LIFE, 0, 0, 0, ticks);
        road(player, level, from, to, ticks);
        fx(player, SLIDE, target != null ? target.m_19879_() : -1, to, from, hold, 0, 0, 0, ticks);
        fx(player, COAT_ON, 0);
        animate(player, "hie_road_slide");
        sound(player, from, HieSounds.FORM, 1.5F, 0.85F);
        Vec3 aim = way;
        later(player, ticks - 1, () -> animate(player, "hie_road_punch"));
        later(player, ticks, () -> {
            if (target != null && target.m_6084_() && target.m_20182_().m_82554_(player.m_20182_()) < 4.0) {
                Vec3 struck = StyleCombat.middle(target);
                if (PikaMoves.hit(player, target, 12.0F, aim.m_82490_(2.2).m_82520_(0, 0.5, 0), true)) {
                    PieceNetwork.around(player, new S2CClash(++frames, struck.f_82479_, struck.f_82480_, struck.f_82481_, (float) aim.f_82479_,
                        (float) aim.f_82481_, 2.0F, 0, 0, player.m_19879_(), target.m_19879_(), 0));
                }
                HieFreeze.frost(player, target, 2, false);
                UoMoves.explode(player, level, struck, aim, 2.4, 4.0F, 0, 1.0);
                burst(player, struck, 2.4, 1.0, 0);
                Impact.quake(level, struck, 24.0, 0.7F, 12);
                sound(player, struck, HieSounds.HIT, 1.6F, 0.75F);
                sound(player, struck, HieSounds.SHATTER, 1.1F, 0.9F);
            } else {
                Vec3 hand = fist(player).m_82549_(aim.m_82490_(0.8));
                burst(player, hand, 1.2, 0.4, 0);
                for (LivingEntity body : PikaMoves.foes(player, hand, 2.0)) {
                    PikaMoves.hit(player, body, 8.0F, aim.m_82490_(1.2).m_82520_(0, 0.3, 0), true);
                    HieFreeze.frost(player, body, 1, false);
                }
                sound(player, hand, HieSounds.HIT, 1.2F, 0.9F);
            }
        });
        later(player, ticks + 6, () -> fx(player, COAT_OFF, 0));
    }

    /**
     * The road's real ice (the owner, asked: "Real ice ground"): the ground along the way from {@code from} to {@code to}, three blocks wide,
     * frozen to packed ice as the road shoots on over {@code ticks} ticks, a jagged block or two of ice by both sides of it every other
     * block; all put back after {@link #GROUND_BACK} ticks.
     */
    static void road(ServerPlayer player, ServerLevel level, Vec3 from, Vec3 to, int ticks) {
        if (!level.m_46469_().m_46207_(net.minecraft.world.level.GameRules.f_46132_)) {
            return;
        }
        Vec3 way = new Vec3(to.f_82479_ - from.f_82479_, 0, to.f_82481_ - from.f_82481_);
        double length = way.m_82553_();
        if (length < 1.0) {
            return;
        }
        Vec3 unit = way.m_82490_(1.0 / length);
        Vec3 side = new Vec3(-unit.f_82481_, 0, unit.f_82479_);
        int steps = (int) Math.ceil(length) + 1;
        double perTick = steps / (double) Math.max(1, ticks);
        Scars scars = Scars.of(level);
        double[] y = {from.f_82480_};
        int[] k = {0};
        LIVE.add(new UoMoves.Live() {
            int age;

            @Override
            public boolean tick(long t) {
                this.age++;
                int upTo = Math.min(steps, (int) Math.ceil(this.age * perTick) + 1);
                while (k[0] < upTo && scars.room()) {
                    int i = k[0]++;
                    Vec3 p = from.m_82549_(unit.m_82490_(i));
                    for (int w = -1; w <= 1; w++) {
                        Vec3 q = p.m_82549_(side.m_82490_(w));
                        double top = freezeColumn(player, level, scars, (int) Math.floor(q.f_82479_), y[0], (int) Math.floor(q.f_82481_), 0, GROUND_BACK);
                        if (w == 0 && !Double.isNaN(top)) {
                            y[0] = top;
                        }
                    }
                    if (i % 2 == 1 && i > 1 && i < steps - 2) {
                        for (int w = -2; w <= 2; w += 4) {
                            Vec3 q = p.m_82549_(side.m_82490_(w));
                            int high = 1 + ((i * 31 + w) & 1);
                            freezeColumn(player, level, scars, (int) Math.floor(q.f_82479_), y[0], (int) Math.floor(q.f_82481_), high, GROUND_BACK);
                        }
                    }
                }
                return k[0] >= steps || this.age > ticks + 20;
            }

            @Override
            public ServerLevel level() {
                return level;
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ N: Ice Saber

    /** The Ice Saber: a sword of ice in the hand for {@link #SABER_TIME} ticks; the attack button cuts with it while it is out. */
    static void saber(ServerPlayer player, State s, long now) {
        if (!ready(player, HieKit.SABER)) {
            return;
        }
        s.saberUntil = now + SABER_TIME;
        s.cuts = 0;
        FruitEnergy.spend(player, HieKit.SABER, SABER_TIME); // 3.2.0: spent as it forms (its cooldown starts when it goes)
        s.cutNext = now + 6;
        fx(player, SABER_ON, 0, Vec3.f_82478_, Vec3.f_82478_, 0, 0, 0, 0, SABER_TIME);
        animate(player, "hie_saber_draw");
        sound(player, fist(player), HieSounds.FORM, 1.0F, 1.35F);
    }

    /** The saber goes ({@code shatter}: it shatters in the hand); its cooldown starts. */
    static void saberOff(ServerPlayer player, State s, boolean shatter) {
        s.saberUntil = -1;
        fx(player, SABER_OFF, 0);
        if (shatter) {
            Vec3 hand = fist(player);
            fx(player, SHATTER, ++seeds, hand, Vec3.f_82478_, 0, 0, 0, 0.7, 0);
            sound(player, hand, HieSounds.SHATTER, 0.8F, 1.3F);
        }
        FruitData.cooldown(player, HieKit.SABER.cooldown(), 200);
        FruitServer.sync(player);
    }

    /** The attack button: with the saber out, a cut (two cuts by turns, a cold crescent; three cuts on one body freeze it). */
    static void attack(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null || s.saberUntil < 0) {
            return;
        }
        long now = now(player);
        if (now < s.cutNext || busy(s, now)) {
            return;
        }
        s.cutNext = now + CUT_EVERY;
        s.cuts++;
        boolean second = s.cuts % 2 == 0;
        animate(player, second ? "hie_saber_cut_2" : "hie_saber_cut_1");
        later(player, 2, () -> {
            Vec3 eye = player.m_146892_();
            Vec3 look = player.m_20154_();
            Vec3 f = flat(player);
            Vec3 right = new Vec3(-f.f_82481_, 0, f.f_82479_);
            double tilt = Math.toRadians(50.0);
            Vec3 axis = second ? new Vec3(0, Math.cos(tilt), 0).m_82549_(right.m_82490_(Math.sin(tilt))) : new Vec3(0, 1, 0);
            Vec3 middle = eye.m_82520_(0, -0.35, 0);
            fx(player, SLASH, ++seeds, middle, axis, look.f_82479_, look.f_82480_, look.f_82481_, 2.4, 0);
            double cos = Math.cos(Math.toRadians(75.0));
            boolean sparked = false;
            for (LivingEntity body : PikaMoves.foes(player, eye.m_82549_(look.m_82490_(CUT_REACH * 0.5)), CUT_REACH)) {
                Vec3 rel = StyleCombat.middle(body).m_82546_(eye);
                double d = rel.m_82553_();
                if (d > CUT_REACH + body.m_20205_() * 0.5 || d > 1.0E-6 && rel.m_82526_(look) / d < cos) {
                    continue;
                }
                cold(player, body, 1);
                PikaMoves.hit(player, body, 6.0F, look.m_82490_(0.5).m_82520_(0, 0.2, 0), false);
                if (!sparked) {
                    sparked = ConquerorAura.mixed(player, StyleCombat.middle(body), 1.6, 5, -1.0);
                }
            }
            sound(player, middle, HieSounds.HIT, 0.9F, 1.3F + (second ? 0.1F : 0.0F));
        });
    }

    // ------------------------------------------------------------------------------------------------------ the body of ice

    /**
     * The passive (ref 64): a blow passed through the body of ice without Haki (the original's Logia): the body shatters into shards and
     * comes back together (seen by everyone near, at most every 10 ticks), and whoever struck it bare-handed is frostbitten.
     */
    static void dodged(ServerPlayer player, DamageSource source) {
        State s = state(player);
        long now = now(player);
        if (now >= s.scatterNext) {
            s.scatterNext = now + 10;
            fx(player, SCATTER, ++seeds, StyleCombat.middle(player), Vec3.f_82478_, 0, 0, 0, player.m_20206_(), 0);
            sound(player, StyleCombat.middle(player), HieSounds.SHATTER, 0.6F, 1.4F);
        }
        if (!(source.m_7639_() instanceof LivingEntity attacker) || source.m_7640_() != attacker || attacker.m_20182_().m_82554_(player.m_20182_()) > 5.0) {
            return;
        }
        hurt(player, attacker, 3.0F);
        // 3.2.2 (the owner: "whenever an entity punches you that doesn't have Haki on, their limbs that they hit with like turn blue"):
        // the frost starts at the arm that struck
        HieFreeze.frost(player, attacker, 1, true);
        Vec3 at = StyleCombat.middle(attacker).m_82549_(StyleCombat.middle(player)).m_82490_(0.5);
        burst(player, at, 0.6, 0.1, 0);
        sound(player, at, HieSounds.HIT, 0.8F, 1.2F);
    }

    // ------------------------------------------------------------------------------------------------------ the clock

    static void tick(ServerPlayer player) {
        walkOnWater(player);
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        long now = now(player);
        if (now < s.moveUntil) {
            player.f_19789_ = 0.0F;
        }
        if (s.chargeSince >= 0) {
            partisanTick(player, s, now);
        }
        if (s.saberUntil >= 0 && now >= s.saberUntil) {
            saberOff(player, s, true);
        }
    }

    /**
     * The passive (refs 80, 81: the sea freezing under his bicycle; the owner: "I'm not stopping you from using Minecraft stuff"): still
     * water under the feet (stepped onto, or fallen onto from a little height) freezes into the game's own frosted ice in a small disc
     * round them, as the Frost Walker's does, and melts back into water a few seconds later (server/Scars puts the water back). Sneaking
     * lets him sink in; already in the water, nothing freezes round him.
     */
    static void walkOnWater(ServerPlayer player) {
        if ((player.f_19797_ & 1) != 0 || player.m_6047_() || player.m_20159_()) {
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 p = player.m_20182_();
        int bx = (int) Math.floor(p.f_82479_);
        int by = (int) Math.floor(p.f_82480_ - 0.5);
        int bz = (int) Math.floor(p.f_82481_);
        net.minecraft.world.level.block.state.BlockState under = level.m_8055_(new net.minecraft.core.BlockPos(bx, by, bz));
        if (under.m_60734_() != net.minecraft.world.level.block.Blocks.f_49990_) {
            under = level.m_8055_(new net.minecraft.core.BlockPos(bx, by - 1, bz));
            by--;
            if (under.m_60734_() != net.minecraft.world.level.block.Blocks.f_49990_) {
                return;
            }
        }
        double top = by + 1.0;
        if (p.f_82480_ < top - 0.02 || p.f_82480_ > top + 1.6) {
            return;
        }
        Scars scars = Scars.of(level);
        net.minecraft.world.level.block.state.BlockState frost = net.minecraft.world.level.block.Blocks.f_50449_.m_49966_();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz > 5) {
                    continue;
                }
                net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(bx + dx, by, bz + dz);
                net.minecraft.world.level.block.state.BlockState state = level.m_8055_(pos);
                if (state.m_60734_() != net.minecraft.world.level.block.Blocks.f_49990_ || !state.m_60819_().m_76170_()
                    || !level.m_8055_(new net.minecraft.core.BlockPos(bx + dx, by + 1, bz + dz)).m_60795_()) {
                    continue;
                }
                scars.swap(player, pos, frost, List.of(), 60L + ((bx + dx) * 7 + (bz + dz) * 13 & 31), true);
            }
        }
    }

    static void forget(ServerPlayer player) {
        State s = STATES.remove(player.m_20148_());
        if (s == null) {
            return;
        }
        if (s.chargeSince >= 0) {
            fx(player, FORM_OFF, 0);
        }
        if (s.saberUntil >= 0) {
            fx(player, SABER_OFF, 0);
        }
        fx(player, COAT_OFF, 0);
    }

    static void clear() {
        STATES.clear();
        LIVE.clear();
        HieFreeze.clear();
    }

    /** The world's tick: every move's lives. */
    @SubscribeEvent
    public static void hieTick(TickEvent.LevelTickEvent event) {
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
}
