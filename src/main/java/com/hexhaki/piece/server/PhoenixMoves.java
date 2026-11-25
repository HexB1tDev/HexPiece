package com.hexhaki.piece.server;

import com.hexhaki.fx.Impact;
import com.hexhaki.piece.dragon.UoSounds;
import com.hexhaki.piece.ironfist.IronSounds;
import com.hexhaki.piece.phoenix.PhoenixForm;
import com.hexhaki.piece.phoenix.PhoenixMoveArt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.7.2 (the owner: "Proceed"; the reference's Move Info and GIFs, docs/handoff/next-ai-3.6.3.md AD): the Phoenix's moves, each in both
 * forms (the human's and the phoenix's: server/PhoenixServer.phase). X Fujiazami: the dash in blue fire (the phoenix: the ram), carrying
 * whoever it meets and throwing them on at its end; C Talon Slam: the leap, the burning leg, the slam and its crater (the phoenix, held:
 * Hawk Rush, the rush, the grab, the whirl and the throw). The server moves the user and decides every hit; every client draws it from
 * small messages (S2CUoFx kinds 401 to 405: client/phoenix/PhoenixFx, the fire phoenix/PhoenixMoveArt). The Conqueror's lightning out of
 * every blast and at every blow (POINT ONE). Damage about the reference's fifth (its 60 is 12).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class PhoenixMoves {
    /** Fujiazami: user; bx FUJI_CHARGE (x, y, z the feet), FUJI_DASH (x, y, z from, a the whole way), FUJI_END (x, y, z where, a the way);
     * by 1 as the phoenix. */
    public static final int FUJI = 401;
    public static final int FUJI_CHARGE = 0;
    public static final int FUJI_DASH = 1;
    public static final int FUJI_END = 2;
    /** 3.7.5: the phoenix's X, the fire tornado: x, y, z its foot at the start, a its step a tick, size its size (1, 1.4 with the
     * Conqueror's Haki and Ryou), ticks how long it runs. */
    public static final int FUJI_TORNADO = 3;
    /** Talon Slam: user; bx TALON_CROUCH, TALON_LEAP (x, y, z the feet), TALON_TOP (ticks the hang), TALON_BLAST (x, y, z the ground). */
    public static final int TALON = 402;
    public static final int TALON_CROUCH = 0;
    public static final int TALON_LEAP = 1;
    public static final int TALON_TOP = 2;
    public static final int TALON_BLAST = 3;
    /** Hawk Rush: user; bx HAWK_WRAP, HAWK_RUSH (on), HAWK_GRAB (id the body seized), HAWK_END. */
    public static final int HAWK = 403;
    public static final int HAWK_WRAP = 0;
    public static final int HAWK_RUSH = 1;
    public static final int HAWK_GRAB = 2;
    public static final int HAWK_END = 3;
    /** 3.7.5: the throw's blast (x, y, z where the one thrown was). */
    public static final int HAWK_THROW = 4;
    /** The human's temporary phoenix wings and tail: user; bx 1 on (ticks how long, by the pose: WINGS_SWEPT, WINGS_SPREAD), 0 off. */
    public static final int WINGS = 404;
    public static final int WINGS_SWEPT = 0;
    public static final int WINGS_SPREAD = 1;
    /** The phoenix's pose for a move: user; bx the act (ACT_*: 0 none), ticks how long. */
    public static final int ACT = 405;
    public static final int ACT_SWEEP = 1;
    public static final int ACT_RAM = 2;
    public static final int ACT_GLIDE = 3;
    public static final int ACT_REACH = 4;
    public static final int ACT_WHIRL = 5;
    /** 3.7.3: spinning in place, wings spread (the whirlpool); held spread and still (Bluebird's charge). */
    public static final int ACT_SPIN = 6;
    public static final int ACT_SPREAD = 7;
    /**
     * 3.7.3: Phoenix Inferno: user; bx INFERNO_FORM (x, y, z the chest: the wings and tail forming), INFERNO_KICK (x, y, z the hip, a the
     * way; by 1 the last kick, up), INFERNO_LAND (x, y, z the feet: the burst scattering the wings), VORTEX_ON (x, y, z the ground under
     * it, ticks), VORTEX_RISE (x, y, z where it rose from), VORTEX_DIVE (a the way down), VORTEX_BLAST (x, y, z the ground).
     */
    public static final int INFERNO = 406;
    public static final int INFERNO_FORM = 0;
    public static final int INFERNO_KICK = 1;
    public static final int INFERNO_LAND = 2;
    public static final int VORTEX_ON = 10;
    public static final int VORTEX_RISE = 11;
    public static final int VORTEX_DIVE = 12;
    public static final int VORTEX_BLAST = 13;
    /**
     * 3.7.3: Bluebird: user; bx BLUE_CHARGE (ticks the longest hold; by 1 the phoenix), BLUE_FIRE (id the ball, x, y, z from, a its step
     * a tick, size its heart, ticks its longest flight), BLUE_BOOM (id the ball, x, y, z where, size the blast's radius), BLUE_OFF.
     */
    public static final int BLUE = 407;
    public static final int BLUE_CHARGE = 0;
    public static final int BLUE_FIRE = 1;
    public static final int BLUE_BOOM = 2;
    public static final int BLUE_OFF = 3;
    /** 3.7.7: the phoenix's flamethrower (M1 held): user; bx 1 on, 0 off. */
    public static final int FLAME = 408;
    /**
     * 3.7.7 (the owner: "Transformed M1, shall make the bird open it's beak and act as a flamethrower (blasting flames at the target like the
     * dragon does)"), as the dragon's breath (server/UoMoves.flame): how far it reaches (blocks), its cone's half-angle (degrees), how long
     * it can be held (ticks), the shortest flame (ticks), its burn every {@link #FLAME_EVERY} ticks, how long a body caught burns
     * (seconds), its cooldown once it stops; where the beak is (blocks over the feet, ahead along the look: the phoenix's, hovering).
     */
    public static final double FLAME_RANGE = 24.0;
    static final double FLAME_CONE = 10.0;
    public static final int FLAME_MAX = 100;
    static final int FLAME_LEAST = 6;
    static final int FLAME_EVERY = 5;
    public static final float FLAME_HURT = 3.0F;
    static final int FLAME_FIRE = 5;
    static final int FLAME_COOL = 80;
    public static final double BEAK_UP = 2.2;
    public static final double BEAK_AHEAD = 2.3;

    /** Damage (the reference's at max level, about a fifth: Fujiazami 60 and 75, Talon Slam 7 then 70, Hawk Rush 95). */
    public static final float FUJI_HURT = 12.0F;
    public static final float FUJI_RAM_HURT = 15.0F;
    public static final float TALON_LEAP_HURT = 1.5F;
    public static final float TALON_SLAM_HURT = 14.0F;
    public static final float HAWK_GRAB_HURT = 4.0F;
    public static final float HAWK_THROW_HURT = 15.0F;
    /** Who is caught along the dash (blocks from the one dashing), how far the stagger at the leap reaches. */
    public static final double FUJI_CATCH = 2.2;
    public static final double FUJI_RAM_CATCH = 3.0;
    public static final double TALON_STAGGER = 3.0;
    public static final double HAWK_CATCH = 2.6;
    public static final double HAWK_SPEED = 1.5;
    static final int FUJI_COOL = 160;
    /** 3.7.5: the fire tornado (the owner: "X transformed becomes a massive tornado like the dragon fruit but bigger and made of beautiful
     * flames (blue and yellow), causes burn and damage + carry stun"): its pace, how far it runs, its burn every 10 ticks, how long a
     * body caught burns (seconds), its cooldown. */
    static final double TORNADO_SPEED = 0.5;
    static final double TORNADO_RANGE = 35.0;
    public static final float TORNADO_HURT = 2.5F;
    static final int TORNADO_FIRE = 4;
    static final int TORNADO_COOL = 260;
    static final int TALON_COOL = 200;
    static final int HAWK_COOL = 220;
    /** 3.7.3 (the reference's: Phoenix Inferno 12 ticks of 2.5 then 75, transformed 42 ticks of 0.9, 75 the leap, 75 the slam; Bluebird
     * 45, transformed 4 of 45). */
    public static final float INFERNO_KICK_HURT = 0.5F;
    public static final float INFERNO_LAST_HURT = 15.0F;
    public static final float VORTEX_HURT = 0.4F;
    public static final float VORTEX_LEAP_HURT = 15.0F;
    public static final float VORTEX_BLAST_HURT = 15.0F;
    public static final float BLUE_HURT = 9.0F;
    /** 3.7.5: the phoenix's one Bluebird (was four of 9). */
    public static final float BLUE_BIRD_HURT = 22.0F;
    static final int INFERNO_COOL = 300;
    static final int BLUE_COOL = 240;

    static final class State {
        long busyUntil;
        UoMoves.Live move;
        /** Hawk Rush held (let go: it ends once the shortest rush is done). */
        boolean hawkHeld;
        /** 3.7.3: Bluebird held (let go: the ball flies, once the shortest hold is done). */
        boolean blueHeld;
        /** 3.7.7: M1 held (let go: the flame stops, once the shortest is done). */
        boolean flameHeld;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final List<UoMoves.Live> LIVE = new ArrayList<>();
    private static int seeds;

    private PhoenixMoves() {
    }

    static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.m_20148_(), k -> new State());
    }

    static long now(ServerPlayer player) {
        return player.m_9236_().m_46467_();
    }

    static void fx(ServerPlayer player, int kind, int id, Vec3 at, Vec3 a, double bx, double by, int ticks) {
        ItoMoves.fx(player, kind, id, at, a, bx, by, 0, ticks);
    }

    static void fx(ServerPlayer player, int kind, int id, Vec3 at, Vec3 a, double bx, double by, double size, int ticks) {
        ItoMoves.fx(player, kind, id, at, a, bx, by, size, ticks);
    }

    static void sound(ServerPlayer player, Vec3 at, String name, float volume, float pitch) {
        ItoMoves.sound(player, at, name, volume, pitch);
    }

    /** Whether the user is the phoenix now (the change itself: neither). */
    static boolean phoenix(ServerPlayer player) {
        return PhoenixServer.phase(player) == PhoenixForm.PHOENIX;
    }

    static boolean changing(ServerPlayer player) {
        int phase = PhoenixServer.phase(player);
        return phase == PhoenixForm.FORMING || phase == PhoenixForm.UNFORMING;
    }

    /** A blow of fire on {@code body}: damage, a push, the Conqueror's lightning at it. */
    static void hit(ServerPlayer player, LivingEntity body, float damage, Vec3 push) {
        if (MaguMoves.hurt(player, body, ItoMoves.coated(player) ? damage * 1.25F : damage)) {
            StyleCombat.push(body, push);
        }
        ItoMoves.lightning(player, StyleCombat.middle(body));
    }

    static void press(ServerPlayer player, int slot) {
        State s = state(player);
        long now = now(player);
        if (s.move != null && !LIVE.contains(s.move)) {
            s.move = null; // it ended without saying so (its user gone and back)
        }
        if (now < s.busyUntil || s.move != null || player.m_20159_() || changing(player)) {
            return;
        }
        boolean bird = phoenix(player);
        if (slot == PhoenixKit.FUJIAZAMI.slot()) {
            com.hexhaki.fruit.FruitMove move = bird ? PhoenixKit.FUJIAZAMI_PHOENIX : PhoenixKit.FUJIAZAMI;
            if (ItoMoves.ready(player, move)) {
                ItoMoves.cool(player, move, bird ? TORNADO_COOL : FUJI_COOL);
                // 3.7.5: as the phoenix, X is the fire tornado (the ram is gone: "the phoenix should never do a 360")
                start(player, s, bird ? new FireTornado(player, now) : new Dash(player, false, now));
            }
        } else if (slot == PhoenixKit.TALON_SLAM.slot()) {
            if (bird) {
                if (ItoMoves.ready(player, PhoenixKit.HAWK_RUSH)) {
                    ItoMoves.cool(player, PhoenixKit.HAWK_RUSH, HAWK_COOL);
                    s.hawkHeld = true;
                    start(player, s, new Rush(player, now));
                }
            } else if (ItoMoves.ready(player, PhoenixKit.TALON_SLAM)) {
                ItoMoves.cool(player, PhoenixKit.TALON_SLAM, TALON_COOL);
                start(player, s, new Slam(player, now));
            }
        } else if (slot == PhoenixKit.INFERNO.slot()) {
            com.hexhaki.fruit.FruitMove move = bird ? PhoenixKit.INFERNO_PHOENIX : PhoenixKit.INFERNO;
            if (ItoMoves.ready(player, move)) {
                ItoMoves.cool(player, move, INFERNO_COOL);
                start(player, s, bird ? new Vortex(player, now) : new Kicks(player, now));
            }
        } else if (slot == PhoenixKit.BLUEBIRD.slot()) {
            com.hexhaki.fruit.FruitMove move = bird ? PhoenixKit.BLUEBIRD_PHOENIX : PhoenixKit.BLUEBIRD;
            if (ItoMoves.ready(player, move)) {
                ItoMoves.cool(player, move, BLUE_COOL);
                s.blueHeld = true;
                start(player, s, new Charge(player, bird, now));
            }
        }
    }

    static void release(ServerPlayer player, int slot) {
        State s = STATES.get(player.m_20148_());
        if (s != null && slot == PhoenixKit.TALON_SLAM.slot()) {
            s.hawkHeld = false;
        }
        if (s != null && slot == PhoenixKit.BLUEBIRD.slot()) {
            s.blueHeld = false;
        }
    }

    /**
     * 3.7.7: M1 held down or let go as the phoenix (network/C2SDragon): the flamethrower on (not while another move runs, while it
     * changes, or before its cooldown is over) and off. Returns at once for anyone who is not the phoenix.
     */
    public static void flame(ServerPlayer player, boolean on) {
        if (!on) {
            State s = STATES.get(player.m_20148_());
            if (s != null) {
                s.flameHeld = false;
            }
            return;
        }
        if (!phoenix(player)) {
            return;
        }
        State s = state(player);
        long now = now(player);
        if (s.move != null && !LIVE.contains(s.move)) {
            s.move = null;
        }
        if (now < s.busyUntil || s.move != null || player.m_20159_() || !ItoMoves.ready(player, PhoenixKit.FLAMETHROWER)) {
            return;
        }
        s.flameHeld = true;
        start(player, s, new Flame(player, now));
    }

    static void start(ServerPlayer player, State s, UoMoves.Live move) {
        s.move = move;
        LIVE.add(move);
    }

    static void done(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s != null) {
            s.move = null;
            s.busyUntil = now(player) + 4;
        }
    }

    /** Sets the user going at {@code v} (blocks a tick) this tick, their fall forgotten (the game's gravity made up for). */
    static void drive(ServerPlayer player, Vec3 v) {
        // the game's pull down (0.08 a tick) made up for, unless standing on the ground or flying (the phoenix: no gravity)
        boolean still = player.m_20096_() && v.f_82480_ <= 0.0 || player.m_150110_().f_35935_;
        player.m_20256_(v.m_82520_(0.0, still ? 0.0 : 0.08, 0.0));
        player.f_19864_ = true;
        player.f_19789_ = 0.0F;
    }

    /** Holds {@code body} at {@code at} this tick (carried, seized). */
    static void pin(LivingEntity body, Vec3 at) {
        body.m_20256_(Vec3.f_82478_);
        body.f_19864_ = true;
        body.m_6021_(at.f_82479_, at.f_82480_, at.f_82481_);
    }

    // ------------------------------------------------------------------------------------------------------ X: Fujiazami

    /**
     * Fujiazami (the reference's: "dashes forward in a burst blue flames and pulls anyone in range with it eventually throwing the
     * enemy forward dealing damage after the dash is finished"): the fire gathering at the feet (the phoenix: its wings swept forward),
     * the dash along the look (the human level along the ground, the phoenix the way it looks), whoever comes within reach carried in
     * front, thrown on at its end in a spray of flame (the Conqueror's lightning out of it). A wall stops it short.
     */
    static final class Dash implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        final boolean bird;
        final long born;
        final int seed = ++seeds;
        final int wind;
        final int ticks;
        final double reach;
        final List<LivingEntity> caught = new ArrayList<>();
        Vec3 way;
        Vec3 from;
        boolean over;

        Dash(ServerPlayer user, boolean bird, long now) {
            this.user = user;
            this.level = (ServerLevel) user.m_9236_();
            this.bird = bird;
            this.born = now;
            this.wind = bird ? PhoenixMoveArt.FUJI_WIND : PhoenixMoveArt.FUJI_CHARGE;
            this.ticks = bird ? PhoenixMoveArt.FUJI_RAM : PhoenixMoveArt.FUJI_DASH;
            this.reach = bird ? PhoenixMoveArt.FUJI_RAM_REACH : PhoenixMoveArt.FUJI_REACH;
            Vec3 feet = user.m_20182_();
            if (bird) {
                fx(user, ACT, this.seed, feet, Vec3.f_82478_, ACT_SWEEP, 0, this.wind);
                sound(user, feet, UoSounds.GUST, 1.2F, 1.1F);
            } else {
                ItoMoves.animate(user, "phoenix_fuji");
                fx(user, WINGS, this.seed, feet, Vec3.f_82478_, 1, WINGS_SWEPT, this.wind + this.ticks + 10);
                sound(user, feet, UoSounds.FLAME_START, 1.0F, 1.2F);
            }
            fx(user, FUJI, this.seed, feet, user.m_20154_(), FUJI_CHARGE, bird ? 1 : 0, this.wind);
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (this.over) {
                return true;
            }
            if (!this.user.m_6084_() || this.user.m_9236_() != this.level || !STATES.containsKey(this.user.m_20148_())) {
                this.over = true;
                return true;
            }
            long t = now - this.born;
            if (t < this.wind) {
                drive(this.user, Vec3.f_82478_);
                return false;
            }
            if (t == this.wind) {
                Vec3 look = this.user.m_20154_();
                this.way = this.bird ? look.m_82541_() : ItoMoves.flat(this.user);
                this.from = this.user.m_20182_();
                Vec3 to = IronFist.blocked(this.user, this.from, this.way, this.reach);
                fx(this.user, FUJI, this.seed, this.from, to.m_82546_(this.from), FUJI_DASH, this.bird ? 1 : 0, this.ticks);
                if (this.bird) {
                    fx(this.user, ACT, this.seed, this.from, Vec3.f_82478_, ACT_RAM, 0, this.ticks);
                }
                sound(this.user, this.from, IronSounds.DASH, 1.2F, this.bird ? 0.8F : 1.0F);
                sound(this.user, this.from, UoSounds.WHOOSH, 1.2F, this.bird ? 0.75F : 0.95F);
            }
            Vec3 at = this.user.m_20182_();
            if (t < this.wind + this.ticks) {
                double step = this.reach / this.ticks;
                Vec3 before = at;
                drive(this.user, this.way.m_82490_(step));
                // whoever comes within reach is carried in front
                double catchR = this.bird ? FUJI_RAM_CATCH : FUJI_CATCH;
                Vec3 middle = at.m_82520_(0.0, 1.0, 0.0);
                for (LivingEntity body : ItoMoves.foes(this.user, middle, catchR)) {
                    if (!this.caught.contains(body)) {
                        this.caught.add(body);
                    }
                }
                Vec3 front = at.m_82549_(this.way.m_82490_(this.bird ? 2.4 : 1.4));
                for (LivingEntity body : this.caught) {
                    if (body.m_6084_()) {
                        pin(body, front.m_82549_(this.way.m_82490_(step)));
                    }
                }
                // a wall: it stops short (the dash's own end comes now)
                if (t > this.wind + 1 && this.last != null && before.m_82554_(this.last) < step * 0.25) {
                    end(at);
                    return true;
                }
                this.last = before;
                return false;
            }
            end(at);
            return true;
        }

        Vec3 last;

        void end(Vec3 at) {
            this.over = true;
            Vec3 heart = at.m_82520_(0.0, 1.0, 0.0).m_82549_(this.way.m_82490_(this.bird ? 2.4 : 1.4));
            fx(this.user, FUJI, this.seed, heart, this.way, FUJI_END, this.bird ? 1 : 0, PhoenixMoveArt.FUJI_BURST);
            if (this.bird) {
                fx(this.user, ACT, this.seed, at, Vec3.f_82478_, ACT_GLIDE, 0, PhoenixMoveArt.FUJI_GLIDE);
            }
            drive(this.user, this.way.m_82490_(0.2));
            Vec3 flat = new Vec3(this.way.f_82479_, 0.0, this.way.f_82481_);
            flat = flat.m_82556_() < 1.0E-6 ? Vec3.f_82478_ : flat.m_82541_();
            for (LivingEntity body : this.caught) {
                if (body.m_6084_()) {
                    hit(this.user, body, this.bird ? FUJI_RAM_HURT : FUJI_HURT, flat.m_82490_(this.bird ? 2.2 : 1.8).m_82520_(0.0, 0.55, 0.0));
                }
            }
            ConquerorAura.exploded(this.user, this.level, heart, this.bird ? 4.0 : 3.0);
            sound(this.user, heart, UoSounds.BLAST, this.bird ? 2.0F : 1.4F, this.bird ? 0.85F : 1.05F);
            done(this.user);
        }
    }

    /** The fire art's lifetimes (phoenix/PhoenixBlast) as tick counts for the messages. */
    static final class PhoenixBlastTicks {
        static final double BURST = com.hexhaki.piece.phoenix.PhoenixBlast.BURST_LIFE;
    }

    /**
     * 3.7.5, X as the phoenix (the owner: "X transformed becomes a massive tornado like the dragon fruit but bigger and made of beautiful
     * flames (blue and yellow), causes burn and damage + carry stun. (With COnqueror + Ryou haki makes lightning from the tornado e makes
     * it bigger."): the phoenix sweeps its wings forward and beats them, and a fire tornado rises in front of it and runs on along the
     * ground the way it looked ({@link #TORNADO_RANGE} blocks): whoever its funnel takes is carried round and up in it (the stun), burnt
     * ({@link #TORNADO_HURT} every 10 ticks, set alight) and flung on at its end. With the Conqueror's Haki and Ryou on it is 1.4 times as
     * big and throws the Conqueror's lightning out of its funnel.
     */
    static final class FireTornado implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        final long born;
        final int seed = ++seeds;
        final List<LivingEntity> caught = new ArrayList<>();
        final double big;
        Vec3 base;
        Vec3 step;
        int life;
        boolean over;

        FireTornado(ServerPlayer user, long now) {
            this.user = user;
            this.level = (ServerLevel) user.m_9236_();
            this.born = now;
            this.big = ConquerorAura.awakened(user) ? 1.4 : 1.0;
            fx(user, ACT, this.seed, user.m_20182_(), Vec3.f_82478_, ACT_SWEEP, 0, PhoenixMoveArt.FUJI_WIND);
            // its flames end as the tornado rises (the charge's life is its ticks and 2)
            fx(user, FUJI, this.seed, user.m_20182_(), Vec3.f_82478_, FUJI_CHARGE, 1, PhoenixMoveArt.FUJI_WIND - 2);
            sound(user, user.m_20182_(), UoSounds.GUST, 1.3F, 0.9F);
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (this.over) {
                return true;
            }
            if (!this.user.m_6084_() || this.user.m_9236_() != this.level) {
                return true;
            }
            long t = now - this.born;
            if (t < PhoenixMoveArt.FUJI_WIND) {
                if (STATES.containsKey(this.user.m_20148_())) {
                    drive(this.user, Vec3.f_82478_);
                }
                return false;
            }
            if (this.base == null) {
                Vec3 flat = ItoMoves.flat(this.user);
                Vec3 ahead = this.user.m_20182_().m_82549_(flat.m_82490_(4.0));
                Vec3 g = ItoMoves.ground(this.level, ahead, 3.0, 16.0);
                this.base = g != null ? g : ahead;
                this.step = flat.m_82490_(TORNADO_SPEED);
                this.life = (int) Math.round(TORNADO_RANGE / TORNADO_SPEED);
                fx(this.user, FUJI, this.seed, this.base, this.step, FUJI_TORNADO, 0, this.big, this.life);
                sound(this.user, this.base, UoSounds.TORNADO, 2.0F, 0.9F);
                sound(this.user, this.base, UoSounds.FLAME_START, 1.6F, 0.8F);
                done(this.user);
            }
            int age = (int) (t - PhoenixMoveArt.FUJI_WIND);
            this.base = this.base.m_82549_(this.step);
            if (age % 80 == 0 && age > 0 && age + 20 < this.life) {
                sound(this.user, this.base.m_82520_(0.0, 2.0, 0.0), UoSounds.TORNADO, 2.0F, 0.9F);
            }
            double tall = com.hexhaki.piece.phoenix.PhoenixBlast.TORNADO_TALL * this.big;
            double low = com.hexhaki.piece.phoenix.PhoenixBlast.TORNADO_LOW * this.big;
            double high = com.hexhaki.piece.phoenix.PhoenixBlast.TORNADO_HIGH * this.big;
            for (LivingEntity body : ItoMoves.foes(this.user, this.base.m_82520_(0.0, tall * 0.5, 0.0), high + tall * 0.5)) {
                double h = Math.max(0.0, Math.min(tall, body.m_20186_() - this.base.f_82480_));
                double radius = low + (high - low) * h / tall;
                double dx = body.m_20185_() - this.base.f_82479_;
                double dz = body.m_20189_() - this.base.f_82481_;
                double r = Math.sqrt(dx * dx + dz * dz);
                if (r > radius + 0.6 || body.m_20186_() < this.base.f_82480_ - 1.0) {
                    continue;
                }
                if (!this.caught.contains(body)) {
                    this.caught.add(body);
                }
                // carried round the funnel (counter-clockwise from above), drawn to a ring inside it, lifted, on with it
                double ring = Math.max(1.0, radius * 0.55);
                double ux = r < 1.0E-3 ? 1.0 : dx / r;
                double uz = r < 1.0E-3 ? 0.0 : dz / r;
                double pull = (ring - r) * 0.35;
                double vx = -uz * 0.6 + ux * pull + this.step.f_82479_;
                double vz = ux * 0.6 + uz * pull + this.step.f_82481_;
                double vy = h < tall - 2.0 ? 0.24 : 0.02;
                body.m_20334_(vx, vy, vz);
                body.f_19864_ = true;
                body.f_19789_ = 0.0F;
                if (age % 10 == 0) {
                    hit(this.user, body, TORNADO_HURT, Vec3.f_82478_);
                    body.m_20254_(TORNADO_FIRE);
                }
            }
            // the Conqueror's lightning out of its funnel (visual only)
            if (this.big > 1.0 && age % 6 == 0) {
                double a = age * 0.7;
                double h = tall * (0.2 + 0.6 * ((age * 0.37) % 1.0));
                double rad = low + (high - low) * h / tall;
                ConquerorAura.mixed(this.user, this.base.m_82520_(Math.cos(a) * rad, h, Math.sin(a) * rad), 4.0, 3, -1.0);
            }
            if (age >= this.life) {
                this.over = true;
                Vec3 forward = this.step.m_82541_();
                for (LivingEntity body : this.caught) {
                    if (body.m_6084_()) {
                        body.m_20334_(forward.f_82479_ * 1.4, 0.8, forward.f_82481_ * 1.4);
                        body.f_19864_ = true;
                    }
                }
                return true;
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ C: Talon Slam

    /**
     * Talon Slam (the reference's: "the user jumps up, stunning players that are near, lights their right leg on fire, and then smashes
     * down in front or below them with an explosion of blue flames"): the crouch, the leap ({@link PhoenixMoveArt#TALON_HEIGHT} up, those
     * within {@link #TALON_STAGGER} staggered), the hang with the leg lit, the dive onto the ground in front (or the one aimed at) and the
     * blast (the Conqueror's lightning out of it: UoMoves.explode) with its burning crater.
     */
    static final class Slam implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        final long born;
        final int seed = ++seeds;
        Vec3 feet;
        Vec3 target;
        boolean over;

        Slam(ServerPlayer user, long now) {
            this.user = user;
            this.level = (ServerLevel) user.m_9236_();
            this.born = now;
            this.feet = user.m_20182_();
            ItoMoves.animate(user, "phoenix_talon");
            fx(user, TALON, this.seed, this.feet, Vec3.f_82478_, TALON_CROUCH, 0, PhoenixMoveArt.TALON_CROUCH);
            // 3.7.6 (the owner: the arms become the wings "for every move literally"): the arms are wings from the crouch to the slam
            fx(user, WINGS, this.seed, user.m_20182_(), Vec3.f_82478_, 1, WINGS_SPREAD, PhoenixMoveArt.TALON_CROUCH + PhoenixMoveArt.TALON_RISE
                + PhoenixMoveArt.TALON_HANG + PhoenixMoveArt.TALON_DIVE + 2);
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (this.over) {
                return true;
            }
            if (!this.user.m_6084_() || this.user.m_9236_() != this.level || !STATES.containsKey(this.user.m_20148_())) {
                this.over = true;
                return true;
            }
            long t = now - this.born;
            int crouch = PhoenixMoveArt.TALON_CROUCH;
            int top = crouch + PhoenixMoveArt.TALON_RISE;
            int hang = top + PhoenixMoveArt.TALON_HANG;
            if (t < crouch) {
                drive(this.user, Vec3.f_82478_);
                return false;
            }
            if (t == crouch) {
                this.feet = this.user.m_20182_();
                fx(this.user, TALON, this.seed, this.feet, Vec3.f_82478_, TALON_LEAP, 0, 10);
                sound(this.user, this.feet, IronSounds.DASH, 1.2F, 0.7F);
                for (LivingEntity body : ItoMoves.foes(this.user, this.feet.m_82520_(0.0, 1.0, 0.0), TALON_STAGGER)) {
                    hit(this.user, body, TALON_LEAP_HURT, new Vec3(0.0, 0.25, 0.0));
                    Impact.hit(this.user, body, StyleCombat.spec(com.hexhaki.piece.style.Hit.Weight.MEDIUM, new Vec3(0.0, 0.25, 0.0)));
                }
            }
            if (t < top) {
                // risen on an eased curve: quick off the ground, slowing to the top
                double a = PhoenixMoveArt.TALON_HEIGHT * ease((t - crouch + 1) / (double) PhoenixMoveArt.TALON_RISE);
                double b = PhoenixMoveArt.TALON_HEIGHT * ease((t - crouch) / (double) PhoenixMoveArt.TALON_RISE);
                drive(this.user, new Vec3(0.0, a - b, 0.0));
                return false;
            }
            if (t == top) {
                fx(this.user, TALON, this.seed, this.user.m_20182_(), Vec3.f_82478_, TALON_TOP, 0, PhoenixMoveArt.TALON_HANG);
                sound(this.user, this.user.m_20182_(), UoSounds.FLAME_START, 1.1F, 0.9F);
            }
            if (t < hang) {
                drive(this.user, Vec3.f_82478_);
                return false;
            }
            if (t == hang) {
                LivingEntity aimed = PikaMoves.aimed(this.user, PhoenixMoveArt.TALON_REACH + 2.0, 0.8);
                Vec3 at = this.user.m_20182_();
                Vec3 ahead = aimed != null ? aimed.m_20182_() : this.feet.m_82549_(ItoMoves.flat(this.user).m_82490_(PhoenixMoveArt.TALON_REACH * 0.6));
                Vec3 flatTo = new Vec3(ahead.f_82479_ - at.f_82479_, 0.0, ahead.f_82481_ - at.f_82481_);
                if (flatTo.m_82553_() > PhoenixMoveArt.TALON_REACH) {
                    flatTo = flatTo.m_82541_().m_82490_(PhoenixMoveArt.TALON_REACH);
                }
                Vec3 g = ItoMoves.ground(this.level, at.m_82549_(flatTo), 2.0, PhoenixMoveArt.TALON_HEIGHT + 8.0);
                this.target = g != null ? g : new Vec3(at.f_82479_ + flatTo.f_82479_, this.feet.f_82480_, at.f_82481_ + flatTo.f_82481_);
                sound(this.user, at, UoSounds.WHOOSH, 1.2F, 0.8F);
            }
            Vec3 at = this.user.m_20182_();
            Vec3 to = this.target.m_82546_(at);
            boolean landed = t > hang + 1 && (this.user.m_20096_() || to.m_82553_() < 0.6);
            if (!landed && t < hang + PhoenixMoveArt.TALON_DIVE) {
                double speed = 1.0 + 0.35 * (t - hang);
                drive(this.user, to.m_82553_() <= speed ? to : to.m_82541_().m_82490_(speed));
                return false;
            }
            this.over = true;
            Vec3 ground = this.user.m_20096_() ? this.user.m_20182_() : this.target;
            drive(this.user, Vec3.f_82478_);
            UoMoves.explode(this.user, this.level, ground.m_82520_(0.0, 0.5, 0.0), new Vec3(0.0, 1.0, 0.0), PhoenixMoveArt.TALON_RADIUS,
                ItoMoves.coated(this.user) ? TALON_SLAM_HURT * 1.25F : TALON_SLAM_HURT, 0, 1.3);
            Impact.quake(this.level, ground, 16.0, 0.55F, 10);
            fx(this.user, TALON, this.seed, ground, Vec3.f_82478_, TALON_BLAST, 0, PhoenixMoveArt.TALON_CRATER);
            sound(this.user, ground, IronSounds.SLAM, 1.6F, 0.9F);
            sound(this.user, ground, UoSounds.EXPLODE, 1.6F, 1.1F);
            done(this.user);
            return true;
        }

        static double ease(double t) {
            t = Math.max(0.0, Math.min(1.0, t));
            return 1.0 - (1.0 - t) * (1.0 - t);
        }
    }

    // ------------------------------------------------------------------------------------------------------ C (the phoenix): Hawk Rush

    /**
     * Hawk Rush (the reference's: "the user begins to fly until it makes a contact of a opponent, then the user grabs the opponent with a
     * deadly claw grab and knocks the opponent forward. This move can be held to fly for a longer time"): the wings wrapped forward, the
     * rush along the look while C is held (the shortest {@link PhoenixMoveArt#HAWK_LEAST}, the longest {@link PhoenixMoveArt#HAWK_MOST}),
     * the first one met seized under the claws, whirled round once and thrown on.
     */
    static final class Rush implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        final long born;
        final int seed = ++seeds;
        LivingEntity seized;
        long grabbed = -1;
        boolean over;

        Rush(ServerPlayer user, long now) {
            this.user = user;
            this.level = (ServerLevel) user.m_9236_();
            this.born = now;
            fx(user, HAWK, this.seed, ItoMoves.chest(user), Vec3.f_82478_, HAWK_WRAP, 0, PhoenixMoveArt.HAWK_WRAP);
            fx(user, ACT, this.seed, user.m_20182_(), Vec3.f_82478_, ACT_SWEEP, 0, PhoenixMoveArt.HAWK_WRAP);
            sound(user, user.m_20182_(), UoSounds.GUST, 1.2F, 1.3F);
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (this.over) {
                return true;
            }
            if (!this.user.m_6084_() || this.user.m_9236_() != this.level || !STATES.containsKey(this.user.m_20148_()) || !phoenix(this.user)) {
                stop();
                return true;
            }
            long t = now - this.born;
            int wrap = PhoenixMoveArt.HAWK_WRAP;
            if (t < wrap) {
                drive(this.user, Vec3.f_82478_);
                return false;
            }
            Vec3 look = this.user.m_20154_();
            if (t == wrap) {
                fx(this.user, HAWK, this.seed, this.user.m_20182_(), look, HAWK_RUSH, 0, PhoenixMoveArt.HAWK_MOST);
                fx(this.user, ACT, this.seed, this.user.m_20182_(), Vec3.f_82478_, ACT_REACH, 0, PhoenixMoveArt.HAWK_MOST);
                sound(this.user, this.user.m_20182_(), UoSounds.WHOOSH, 1.4F, 0.8F);
            }
            if (this.seized == null) {
                State s = STATES.get(this.user.m_20148_());
                boolean held = s != null && s.hawkHeld;
                if (t - wrap >= PhoenixMoveArt.HAWK_MOST || !held && t - wrap >= PhoenixMoveArt.HAWK_LEAST) {
                    stop();
                    return true;
                }
                drive(this.user, look.m_82490_(HAWK_SPEED));
                Vec3 claws = this.user.m_20182_().m_82549_(look.m_82490_(1.2));
                for (LivingEntity body : ItoMoves.foes(this.user, claws, HAWK_CATCH)) {
                    this.seized = body;
                    this.grabbed = now;
                    hit(this.user, body, HAWK_GRAB_HURT, Vec3.f_82478_);
                    fx(this.user, HAWK, body.m_19879_(), StyleCombat.middle(body), look, HAWK_GRAB, 0, PhoenixMoveArt.HAWK_WHIRL);
                    fx(this.user, ACT, this.seed, this.user.m_20182_(), Vec3.f_82478_, ACT_REACH, 0, PhoenixMoveArt.HAWK_WHIRL);
                    sound(this.user, claws, IronSounds.SLAM, 1.2F, 1.3F);
                    break;
                }
                return false;
            }
            // 3.7.5 (the owner: "the moves should never make the bird spin a 360"): seized, carried a moment under its claws as it climbs,
            // then thrown on the way it looks in a blast of its fire
            long w = now - this.grabbed;
            drive(this.user, look.m_82490_(0.35).m_82520_(0.0, 0.12, 0.0));
            if (w < PhoenixMoveArt.HAWK_WHIRL && this.seized.m_6084_()) {
                pin(this.seized, this.user.m_20182_().m_82549_(look.m_82490_(1.6)).m_82520_(0.0, -1.1, 0.0));
                return false;
            }
            if (this.seized.m_6084_()) {
                Vec3 flat = ItoMoves.flat(this.user);
                Vec3 at = StyleCombat.middle(this.seized);
                hit(this.user, this.seized, HAWK_THROW_HURT, flat.m_82490_(2.4).m_82520_(0.0, 0.6, 0.0));
                Impact.hit(this.user, this.seized, StyleCombat.spec(com.hexhaki.piece.style.Hit.Weight.HEAVY,
                    flat.m_82490_(2.4).m_82520_(0.0, 0.6, 0.0)));
                fx(this.user, HAWK, this.seed, at, flat, HAWK_THROW, 0, (int) PhoenixBlastTicks.BURST);
                ConquerorAura.exploded(this.user, this.level, at, 2.5);
                sound(this.user, at, UoSounds.BLAST, 1.4F, 1.2F);
            }
            stop();
            return true;
        }

        void stop() {
            this.over = true;
            fx(this.user, HAWK, this.seed, this.user.m_20182_(), Vec3.f_82478_, HAWK_END, 0, 0);
            fx(this.user, ACT, this.seed, this.user.m_20182_(), Vec3.f_82478_, 0, 0, 0);
            done(this.user);
        }
    }

    // ------------------------------------------------------------------------------------------------------ 3.7.3, V: Phoenix Inferno

    /**
     * Phoenix Inferno, human (the reference's: "The user forms temporary wings and a tail and then does a barrage of kicks dealing
     * multiple ticks of damage before doing a backflip, ending the move"): the wings and tail form in a burst of fire, twelve kicks at
     * whoever is in front (held where they stand), the last kick up launching them, a backflip, the landing burst scattering the wings.
     */
    static final class Kicks implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        final long born;
        final int seed = ++seeds;
        final List<LivingEntity> held = new ArrayList<>();
        boolean over;

        Kicks(ServerPlayer user, long now) {
            this.user = user;
            this.level = (ServerLevel) user.m_9236_();
            this.born = now;
            ItoMoves.animate(user, "phoenix_inferno");
            fx(user, WINGS, this.seed, user.m_20182_(), Vec3.f_82478_, 1, WINGS_SPREAD, PhoenixMoveArt.INFERNO_FLIP + 2);
            fx(user, INFERNO, this.seed, ItoMoves.chest(user), Vec3.f_82478_, INFERNO_FORM, 0, 14);
            sound(user, user.m_20182_(), UoSounds.FLAME_START, 1.1F, 1.0F);
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (this.over) {
                return true;
            }
            if (!this.user.m_6084_() || this.user.m_9236_() != this.level || !STATES.containsKey(this.user.m_20148_())) {
                this.over = true;
                return true;
            }
            long t = now - this.born;
            Vec3 flat = ItoMoves.flat(this.user);
            Vec3 feet = this.user.m_20182_();
            Vec3 hip = feet.m_82520_(0.0, 0.75, 0.0);
            int form = PhoenixMoveArt.INFERNO_FORM;
            int last = PhoenixMoveArt.INFERNO_LAST;
            if (t >= form && t < last && (t - form) % PhoenixMoveArt.INFERNO_EVERY == 0) {
                fx(this.user, INFERNO, this.seed + (int) t, hip, flat, INFERNO_KICK, 0, 6);
                ItoMoves.sounds(this.user, hip, com.hexhaki.piece.pika.PikaSounds.KICK_BLAST, 0.7F, 1.2F + 0.05F * (t % 4), 1, 2);
                for (LivingEntity body : ItoMoves.ahead(this.user, PhoenixMoveArt.INFERNO_REACH, 60.0)) {
                    if (!this.held.contains(body)) {
                        this.held.add(body);
                    }
                    hit(this.user, body, INFERNO_KICK_HURT, Vec3.f_82478_);
                }
            }
            // whoever is kicked is held where they stand through the barrage
            if (t < last) {
                for (LivingEntity body : this.held) {
                    if (body.m_6084_()) {
                        body.m_20256_(Vec3.f_82478_);
                        body.f_19864_ = true;
                    }
                }
            }
            if (t == last) {
                fx(this.user, INFERNO, this.seed, hip, flat, INFERNO_KICK, 1, 6);
                sound(this.user, hip, com.hexhaki.piece.pika.PikaSounds.KICK_BLAST, 1.2F, 0.85F);
                for (LivingEntity body : ItoMoves.ahead(this.user, PhoenixMoveArt.INFERNO_REACH + 0.5, 70.0)) {
                    Vec3 up = flat.m_82490_(0.6).m_82520_(0.0, 1.2, 0.0);
                    hit(this.user, body, INFERNO_LAST_HURT, up);
                    Impact.hit(this.user, body, StyleCombat.spec(com.hexhaki.piece.style.Hit.Weight.HEAVY, up));
                }
                // the backflip's hop up and back
                drive(this.user, flat.m_82490_(-0.25).m_82520_(0.0, 0.5, 0.0));
            }
            if (t > last && t < PhoenixMoveArt.INFERNO_FLIP) {
                this.user.f_19789_ = 0.0F;
            }
            if (t == PhoenixMoveArt.INFERNO_FLIP) {
                fx(this.user, INFERNO, this.seed, feet, flat, INFERNO_LAND, 0, 14);
                sound(this.user, feet, UoSounds.BLAST, 1.0F, 1.3F);
                ConquerorAura.exploded(this.user, this.level, feet.m_82520_(0.0, 1.0, 0.0), 2.5);
            }
            if (t >= PhoenixMoveArt.INFERNO_LAND) {
                this.over = true;
                done(this.user);
                return true;
            }
            return false;
        }
    }

    /**
     * Phoenix Inferno, as the phoenix (the reference's: "user starts spinning around dealing damage to enemy, after spinning user flies
     * up and falls down followed up with a blue flame explosion"): the phoenix spins in place inside a whirlpool of fire for
     * {@link PhoenixMoveArt#VORTEX} ticks (those inside it burnt every 3 ticks and held), rises (those inside launched up with it), dives
     * back down onto the whirlpool's middle and blows up there (UoMoves.explode: the Conqueror's lightning out of it).
     */
    static final class Vortex implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        final long born;
        final int seed = ++seeds;
        final Vec3 middle;
        boolean over;

        Vortex(ServerPlayer user, long now) {
            this.user = user;
            this.level = (ServerLevel) user.m_9236_();
            this.born = now;
            Vec3 feet = user.m_20182_();
            Vec3 g = ItoMoves.ground(this.level, feet, 1.0, 12.0);
            this.middle = g != null ? g : feet;
            fx(user, INFERNO, this.seed, this.middle, Vec3.f_82478_, VORTEX_ON, 0, PhoenixMoveArt.VORTEX);
            // 3.7.5: held in its whirlpool, wings spread (no spin: "the moves should never make the bird spin a 360")
            fx(user, ACT, this.seed, feet, Vec3.f_82478_, ACT_SPREAD, 0, PhoenixMoveArt.VORTEX);
            sound(user, feet, UoSounds.TORNADO, 1.4F, 1.1F);
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (this.over) {
                return true;
            }
            if (!this.user.m_6084_() || this.user.m_9236_() != this.level || !STATES.containsKey(this.user.m_20148_()) || !phoenix(this.user)) {
                this.over = true;
                fx(this.user, ACT, this.seed, this.user.m_20182_(), Vec3.f_82478_, 0, 0, 0);
                return true;
            }
            long t = now - this.born;
            int spin = PhoenixMoveArt.VORTEX;
            int rise = spin + PhoenixMoveArt.VORTEX_RISE;
            Vec3 heart = this.middle.m_82520_(0.0, 1.0, 0.0);
            if (t < spin) {
                drive(this.user, Vec3.f_82478_);
                if (t % 3 == 0) {
                    for (LivingEntity body : ItoMoves.foes(this.user, heart, PhoenixMoveArt.VORTEX_RADIUS)) {
                        hit(this.user, body, VORTEX_HURT, Vec3.f_82478_);
                        body.m_20256_(heart.m_82546_(StyleCombat.middle(body)).m_82490_(0.06));
                        body.f_19864_ = true;
                    }
                }
                return false;
            }
            if (t == spin) {
                fx(this.user, INFERNO, this.seed, this.user.m_20182_(), Vec3.f_82478_, VORTEX_RISE, 0, PhoenixMoveArt.VORTEX_RISE);
                sound(this.user, this.user.m_20182_(), UoSounds.WHOOSH, 1.4F, 0.9F);
                for (LivingEntity body : ItoMoves.foes(this.user, heart, PhoenixMoveArt.VORTEX_RADIUS)) {
                    Vec3 up = new Vec3(0.0, 1.4, 0.0);
                    hit(this.user, body, VORTEX_LEAP_HURT, up);
                    Impact.hit(this.user, body, StyleCombat.spec(com.hexhaki.piece.style.Hit.Weight.HEAVY, up));
                }
            }
            if (t < rise) {
                drive(this.user, new Vec3(0.0, PhoenixMoveArt.VORTEX_HEIGHT / PhoenixMoveArt.VORTEX_RISE, 0.0));
                return false;
            }
            Vec3 at = this.user.m_20182_();
            Vec3 to = this.middle.m_82546_(at);
            if (t == rise) {
                fx(this.user, INFERNO, this.seed, at, to, VORTEX_DIVE, 0, PhoenixMoveArt.VORTEX_DIVE + 4);
                fx(this.user, ACT, this.seed, at, Vec3.f_82478_, ACT_RAM, 0, PhoenixMoveArt.VORTEX_DIVE + 2);
                sound(this.user, at, UoSounds.WHOOSH, 1.4F, 0.7F);
            }
            boolean landed = t > rise + 1 && (this.user.m_20096_() || to.m_82553_() < 0.8);
            if (!landed && t < rise + PhoenixMoveArt.VORTEX_DIVE + 4) {
                double speed = 1.8 + 0.3 * (t - rise);
                drive(this.user, to.m_82553_() <= speed ? to : to.m_82541_().m_82490_(speed));
                return false;
            }
            this.over = true;
            drive(this.user, Vec3.f_82478_);
            UoMoves.explode(this.user, this.level, heart, new Vec3(0.0, 1.0, 0.0), PhoenixMoveArt.VORTEX_BLAST,
                ItoMoves.coated(this.user) ? VORTEX_BLAST_HURT * 1.25F : VORTEX_BLAST_HURT, 0, 1.6);
            Impact.quake(this.level, this.middle, 24.0, 0.8F, 12);
            fx(this.user, INFERNO, this.seed, this.middle, Vec3.f_82478_, VORTEX_BLAST, 0, PhoenixMoveArt.VORTEX_BLAST_LIFE);
            sound(this.user, this.middle, UoSounds.EXPLODE, 2.0F, 0.8F);
            sound(this.user, this.middle, IronSounds.SLAM, 1.6F, 0.8F);
            done(this.user);
            return true;
        }
    }

    // ------------------------------------------------------------------------------------------------------ 3.7.3, B: Bluebird

    /**
     * Bluebird held (the reference's: "The user forms temporary wings and creates a ball of yellow and blue flame in front of them. When
     * they let go of their mouse, the projectile will be launched forward ... Holding this skill increases its hitbox. On transform user
     * fires 4 balls instead of one"): the wings and tail, the ball growing in front of the chest while B is held (the phoenix: its wings
     * held spread, the flames gathering, a white orb at its chest), let go (after the shortest hold, or at the longest) and it flies.
     */
    static final class Charge implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        final boolean bird;
        final long born;
        final int seed = ++seeds;
        long fired = -1;
        double size;
        boolean over;

        Charge(ServerPlayer user, boolean bird, long now) {
            this.user = user;
            this.level = (ServerLevel) user.m_9236_();
            this.bird = bird;
            this.born = now;
            if (bird) {
                fx(user, ACT, this.seed, user.m_20182_(), Vec3.f_82478_, ACT_SPREAD, 0, PhoenixMoveArt.BLUE_MOST + PhoenixMoveArt.BLUE_GAP);
            } else {
                ItoMoves.animate(user, "phoenix_bluebird");
                fx(user, WINGS, this.seed, user.m_20182_(), Vec3.f_82478_, 1, WINGS_SPREAD, PhoenixMoveArt.BLUE_MOST + 8);
            }
            fx(user, BLUE, this.seed, ItoMoves.chest(user), Vec3.f_82478_, BLUE_CHARGE, bird ? 1 : 0, PhoenixMoveArt.BLUE_MOST);
            sound(user, user.m_20182_(), UoSounds.CHARGE, 1.0F, bird ? 0.9F : 1.2F);
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (this.over) {
                return true;
            }
            boolean gone = !this.user.m_6084_() || this.user.m_9236_() != this.level || !STATES.containsKey(this.user.m_20148_())
                || this.bird != phoenix(this.user);
            if (gone) {
                this.over = true;
                fx(this.user, BLUE, this.seed, this.user.m_20182_(), Vec3.f_82478_, BLUE_OFF, 0, 0);
                fx(this.user, ACT, this.seed, this.user.m_20182_(), Vec3.f_82478_, 0, 0, 0);
                return true;
            }
            long t = now - this.born;
            if (this.fired < 0) {
                State s = STATES.get(this.user.m_20148_());
                boolean held = s != null && s.blueHeld;
                if (this.bird) {
                    drive(this.user, Vec3.f_82478_);
                }
                if (t < PhoenixMoveArt.BLUE_MOST && (held || t < PhoenixMoveArt.BLUE_LEAST)) {
                    return false;
                }
                this.fired = now;
                this.size = PhoenixMoveArt.blueSize(t);
                if (!this.bird) {
                    ItoMoves.animate(this.user, "phoenix_bluebird_throw");
                    fx(this.user, WINGS, this.seed, this.user.m_20182_(), Vec3.f_82478_, 1, WINGS_SPREAD, 8);
                }
            }
            long f = now - this.fired;
            // 3.7.5 (the owner: "the B move should be 1 projectile"); 3.7.6 a sphere of flames ("a sphere not a bird")
            int balls = 1;
            if (f % PhoenixMoveArt.BLUE_GAP == 0 && f / PhoenixMoveArt.BLUE_GAP < balls) {
                int k = (int) (f / PhoenixMoveArt.BLUE_GAP);
                Vec3 look = this.user.m_20154_();
                double speed = this.bird ? PhoenixMoveArt.BLUE_BIRD_SPEED : PhoenixMoveArt.BLUE_SPEED;
                double heart = this.bird ? 2.2 : this.size;
                Vec3 from = ItoMoves.chest(this.user).m_82549_(look.m_82490_(this.bird ? 3.0 : 1.2)).m_82520_(0.0, this.bird ? 0.8 : 0.0, 0.0);
                Ball ball = new Ball(this.user, from, look.m_82490_(speed), heart, this.bird, ++seeds);
                LIVE.add(ball);
                fx(this.user, BLUE, ball.id, from, look.m_82490_(speed), BLUE_FIRE, this.bird ? 1 : 0, heart,
                    (int) (PhoenixMoveArt.BLUE_RANGE / speed) + 2);
                sound(this.user, from, UoSounds.FIREBALL, 1.2F, this.bird ? 0.9F + 0.08F * k : 1.0F);
                if (k == balls - 1) {
                    this.over = true;
                    if (this.bird) {
                        fx(this.user, ACT, this.seed, this.user.m_20182_(), Vec3.f_82478_, 0, 0, 0);
                    }
                    done(this.user);
                    return true;
                }
            }
            return false;
        }
    }

    /** One of Bluebird's balls in flight: on until it meets someone, a block or the end of its range; then its blast. */
    static final class Ball implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        final Vec3 step;
        final double size;
        final boolean bird;
        final int id;
        Vec3 at;
        double gone;
        boolean over;

        Ball(ServerPlayer user, Vec3 from, Vec3 step, double size, boolean bird, int id) {
            this.user = user;
            this.level = (ServerLevel) user.m_9236_();
            this.at = from;
            this.step = step;
            this.size = size;
            this.bird = bird;
            this.id = id;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (this.over) {
                return true;
            }
            if (!this.user.m_6084_() || this.user.m_9236_() != this.level) {
                this.over = true;
                return true;
            }
            Vec3 next = this.at.m_82549_(this.step);
            net.minecraft.world.phys.BlockHitResult block = this.level.m_45547_(new net.minecraft.world.level.ClipContext(this.at, next,
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, this.user));
            boolean wall = block.m_6662_() == net.minecraft.world.phys.HitResult.Type.BLOCK;
            if (wall) {
                next = block.m_82450_();
            }
            boolean met = !ItoMoves.foes(this.user, next, this.size * 0.5 + 0.6).isEmpty();
            this.at = next;
            this.gone += this.step.m_82553_();
            if (wall || met || this.gone >= PhoenixMoveArt.BLUE_RANGE) {
                this.over = true;
                // the massive blast: wide (the phoenix's the widest), harder the bigger the sphere (3.7.6: a great explosion, no bird)
                boolean bird = this.bird;
                double radius = bird ? 7.0 : 2.5 + 2.0 * this.size;
                float hurt = bird ? BLUE_BIRD_HURT : BLUE_HURT * (float) (0.8 + 0.4 * this.size);
                UoMoves.explode(this.user, this.level, this.at, this.step.m_82541_(), radius, ItoMoves.coated(this.user) ? hurt * 1.25F : hurt, 0,
                    1.4);
                Impact.quake(this.level, this.at, radius * 3.0, 0.6F, 10);
                fx(this.user, BLUE, this.id, this.at, this.step, BLUE_BOOM, bird ? 1 : 0, radius, (int) PhoenixBlastTicks.BURST);
                sound(this.user, this.at, UoSounds.EXPLODE, 1.4F, 1.15F);
                return true;
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ the rest

    // ------------------------------------------------------------------------------------------------------ M1: the flamethrower

    /** 3.7.7: where the phoenix's beak is (as it hovers: PhoenixForm's model placed as client/phoenix/PhoenixFx places it). */
    static Vec3 beak(ServerPlayer player) {
        return player.m_20182_().m_82520_(0.0, BEAK_UP, 0.0).m_82549_(player.m_20154_().m_82490_(BEAK_AHEAD));
    }

    /**
     * The phoenix's flamethrower (M1 held), as the dragon's breath (server/UoMoves.breathe): the beak opens and a cone of blue fire pours
     * out of it onto what the player aims at (UoMoves.toward: the crosshair's first block or foe), stopped by the first block; every
     * {@link #FLAME_EVERY} ticks whoever is in it is burnt and set alight, the Conqueror's lightning at the one burnt (or where the flame
     * strikes a wall: POINT ONE). It lasts while M1 is held, at most {@link #FLAME_MAX} ticks; its cooldown starts when it stops.
     */
    static final class Flame implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        final long born;
        boolean over;

        Flame(ServerPlayer user, long now) {
            this.user = user;
            this.level = (ServerLevel) user.m_9236_();
            this.born = now;
            fx(user, FLAME, 0, user.m_20182_(), Vec3.f_82478_, 1, 0, FLAME_MAX);
            sound(user, beak(user), UoSounds.FLAME_START, 1.8F, 1.15F);
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (this.over) {
                return true;
            }
            State s = STATES.get(this.user.m_20148_());
            long t = now - this.born;
            if (!this.user.m_6084_() || this.user.m_9236_() != this.level || s == null || !phoenix(this.user) || t > FLAME_MAX
                || !s.flameHeld && t >= FLAME_LEAST) {
                stop();
                return true;
            }
            if (t % FLAME_EVERY == 0) {
                burn();
            }
            if (t >= 30 && (t - 30) % 45 == 0) {
                sound(this.user, beak(this.user), UoSounds.FLAME_LOOP, 1.6F, 1.15F); // its roar held while it burns
            }
            return false;
        }

        void stop() {
            this.over = true;
            fx(this.user, FLAME, 0, this.user.m_20182_(), Vec3.f_82478_, 0, 0, 0);
            ItoMoves.cool(this.user, PhoenixKit.FLAMETHROWER, FLAME_COOL);
            done(this.user);
        }

        /** One burn of the cone. */
        void burn() {
            Vec3 from = beak(this.user);
            Vec3 look = UoMoves.toward(this.user, from, FLAME_RANGE);
            Vec3 to = from.m_82549_(look.m_82490_(FLAME_RANGE));
            net.minecraft.world.phys.BlockHitResult wall = this.level.m_45547_(new net.minecraft.world.level.ClipContext(from, to,
                net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, this.user));
            boolean blocked = wall.m_6662_() == net.minecraft.world.phys.HitResult.Type.BLOCK;
            double reach = blocked ? wall.m_82450_().m_82554_(from) : FLAME_RANGE;
            double tan = Math.tan(Math.toRadians(FLAME_CONE));
            net.minecraft.world.phys.AABB box = UoMoves.box(from, from.m_82549_(look.m_82490_(reach))).m_82400_(reach * tan + 1.0);
            boolean struck = false;
            for (LivingEntity body : this.level.m_6443_(LivingEntity.class, box, e -> e != this.user && e.m_6084_() && !e.m_5833_()
                && com.hexhaki.fruit.FruitUtil.foe(this.user, e))) {
                Vec3 rel = StyleCombat.middle(body).m_82546_(from);
                double along = rel.m_82526_(look);
                if (along < 0.0 || along > reach) {
                    continue;
                }
                double off = rel.m_82546_(look.m_82490_(along)).m_82553_();
                if (off <= along * tan + 0.8 + body.m_20205_() * 0.5) {
                    hit(this.user, body, FLAME_HURT, look.m_82490_(0.2));
                    body.m_20254_(FLAME_FIRE);
                    struck = true;
                }
            }
            if (!struck && blocked) {
                ItoMoves.lightning(this.user, wall.m_82450_()); // the Conqueror's lightning where the flame strikes
            }
        }
    }

    static void forget(ServerPlayer player) {
        State s = STATES.remove(player.m_20148_());
        if (s != null && s.move != null) {
            fx(player, ACT, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0);
            fx(player, WINGS, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0);
            fx(player, HAWK, 0, player.m_20182_(), Vec3.f_82478_, HAWK_END, 0, 0);
            fx(player, BLUE, 0, player.m_20182_(), Vec3.f_82478_, BLUE_OFF, 0, 0);
            fx(player, FLAME, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0);
        }
    }

    static void clear() {
        STATES.clear();
        LIVE.clear();
    }

    /** The world's tick: every move under way. */
    @SubscribeEvent
    public static void phoenixTick(TickEvent.LevelTickEvent event) {
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
            boolean over;
            try {
                over = live.tick(now);
            } catch (RuntimeException failure) {
                over = true;
            }
            if (over) {
                LIVE.remove(live);
            }
        }
    }
}
