package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.FruitUtil;
import com.hexhaki.fx.Impact;
import com.hexhaki.piece.itsumade.ItsuForm;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.registry.ModSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 3.9.8: the Itsumade eater's moves as a human (the owner's spec: "X - Gorosei Stare (Focus your sight on a target and send them
 * flying.) C - Enourmous Presence (Your body unleashes conqueror's haki automaticly for 10 seconds, once in a while a lightning comes out
 * of the body stunning anything it touches.) V - Yokai Blast (Point your hand forward and charge a beam of bright orange/black flames,
 * cannot be aimed after release, beam continues for a minimum of 1 second and at max charge 5 seconds ... goes through a maximum of 15
 * blocks at max charge and 5 blocks at minimum then self repairs) B- Thy Death: Conqueror's haki lightning fall from the clouds down to
 * the floor (making sure it always hits the ground) for 10 seconds in a circle around the player (25 block radius) when touching the
 * floor the lightning explodes into an AOE of a sphere with 3 diameter, stun + damage."). The server's half: what each does to the world,
 * when; every client draws it (client/itsumade/ItsuSaintFx, S2CUoFx kinds 450 to 458).
 */
public final class ItsuSaint {
    /** S2CUoFx kinds (client/itsumade/ItsuSaintFx). */
    public static final int STARE = 450;
    public static final int PRESENCE = 451;
    public static final int CHARGE = 452;
    public static final int BLAST = 453;
    public static final int BLAST_OFF = 454;
    public static final int STORM = 455;
    public static final int SKY_BOLT = 456;
    public static final int BODY_BOLT = 457;
    public static final int PULSE = 458;

    /** Gorosei Stare: how far it sees a target, how long the glare takes to land (ticks), how hard it throws, how long they are down. */
    static final double STARE_RANGE = 48.0;
    public static final int STARE_AT = 8;
    static final double STARE_THROW = 6.0;
    static final int STARE_STUN = 50;
    static final float STARE_DAMAGE = 6.0F;
    static final int STARE_COOL = 160;
    /** Enormous Presence: how long (ticks), its pressure (how often, how far, how long each is held down), its bolts (how far, how long). */
    public static final int PRESENCE_TICKS = 200;
    static final int PRESENCE_EVERY = 40;
    static final double PRESENCE_RADIUS = 12.0;
    static final int PRESENCE_HOLD = 24;
    static final double PRESENCE_BOLT_RANGE = 10.0;
    static final int PRESENCE_STUN = 40;
    static final float PRESENCE_DAMAGE = 3.0F;
    static final int PRESENCE_COOL = 600;
    /** Yokai Blast: the charge (ticks for full, the least), the beam's life and how deep it bores by the charge, its speed and reach. */
    public static final int BLAST_FULL = 60;
    static final int BLAST_LEAST = 6;
    static final int BLAST_COOL = 200;
    public static final double BLAST_SPEED = 8.0;
    /** Thy Death: how long the storm lasts (ticks), when the first bolt falls, how often, how far round the player, the blast's size. */
    public static final int DEATH_TICKS = 200;
    public static final int DEATH_GATHER = 20;
    static final int DEATH_EVERY = 3;
    public static final double DEATH_RADIUS = 25.0;
    public static final double DEATH_BLAST = 1.5;
    public static final double CLOUD_HEIGHT = 34.0;
    static final float DEATH_DAMAGE = 10.0F;
    static final int DEATH_STUN = 50;
    static final int DEATH_COOL = 900;

    /**
     * 4.0.0: the N key's ability number (client/FruitKeyN.ABILITY, as GuraUltimate.SLOT): N is a key of HexPiece's own, sent as 40, not
     * as the "[N]" move's slot (3.9.9 compared it with TELEPORT.slot(), so N never opened the menu).
     */
    public static final int N_SLOT = 40;

    static final class State {
        long stareAt = -1;
        int stared;
        long presenceUntil = -1;
        long nextBolt;
        long chargeFrom = -1;
        long stormFrom = -1;
        double cloudY;
        Blast blast;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private ItsuSaint() {
    }

    static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.m_20148_(), id -> new State());
    }

    static boolean human(ServerPlayer player) {
        return ItsuServer.phase(player) == ItsuForm.HUMAN;
    }

    static boolean ready(ServerPlayer player, FruitMove move) {
        return UoMoves.ready(player, move);
    }

    static void cool(ServerPlayer player, FruitMove move, int ticks) {
        UoMoves.cool(player, move, ticks);
    }

    static void play(ServerPlayer player, Vec3 at, SoundEvent sound, float volume, float pitch) {
        player.m_9236_().m_6263_(null, at.f_82479_, at.f_82480_, at.f_82481_, sound, SoundSource.PLAYERS, volume, pitch);
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    public static void press(ServerPlayer player, int slot) {
        if (!human(player)) {
            return;
        }
        long now = player.m_9236_().m_46467_();
        if (slot == ItsuKit.STARE.slot()) {
            stare(player, now);
        } else if (slot == ItsuKit.PRESENCE.slot()) {
            presence(player, now);
        } else if (slot == ItsuKit.BLAST.slot()) {
            State s = state(player);
            if (s.chargeFrom < 0 && s.blast == null && ready(player, ItsuKit.BLAST)) {
                s.chargeFrom = now;
                ItoMoves.animate(player, "itsu_blast_charge");
                ItoMoves.fx(player, CHARGE, 1, hand(player), Vec3.f_82478_, 0, 0, 0, 0);
                ItoMoves.sound(player, hand(player), com.hexhaki.piece.dragon.UoSounds.CHARGE_BUILD, 1.4F, 0.9F);
            }
        } else if (slot == ItsuKit.DEATH.slot()) {
            death(player, now);
        } else if (slot == N_SLOT) {
            ItsuGates.menu(player); // 3.9.9
        }
    }

    public static void release(ServerPlayer player, int slot) {
        if (slot != ItsuKit.BLAST.slot()) {
            return;
        }
        State s = state(player);
        if (s.chargeFrom < 0) {
            return;
        }
        long held = player.m_9236_().m_46467_() - s.chargeFrom;
        s.chargeFrom = -1;
        ItoMoves.fx(player, CHARGE, 0, hand(player), Vec3.f_82478_, 0, 0, 0, 0);
        if (held < BLAST_LEAST || !human(player)) {
            ItoMoves.animate(player, "itsu_blast_end");
            return;
        }
        float charge = (float) Math.min(1.0, held / (double) BLAST_FULL);
        cool(player, ItsuKit.BLAST, BLAST_COOL);
        blast(player, s, charge);
    }

    // ------------------------------------------------------------------------------------------------------ X: Gorosei Stare

    /** X: the first foe on the eye's line within STARE_RANGE (nothing in sight: nothing happens, nothing spent). */
    static void stare(ServerPlayer player, long now) {
        State s = state(player);
        if (s.stareAt >= 0 || !ready(player, ItsuKit.STARE)) {
            return;
        }
        LivingEntity target = sighted(player, STARE_RANGE);
        if (target == null) {
            return;
        }
        cool(player, ItsuKit.STARE, STARE_COOL);
        s.stareAt = now + STARE_AT;
        s.stared = target.m_19879_();
        ItoMoves.animate(player, "itsu_stare");
        // held where they stand while the glare lands
        target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, STARE_AT + 2, 5, false, false));
        ItoMoves.fx(player, STARE, target.m_19879_(), StyleCombat.middle(target), Vec3.f_82478_, 0, 0, 0, STARE_AT);
        ItoMoves.sound(player, player.m_146892_(), ItsuServer.STARE, 1.4F, 1.0F);
    }

    /** The glare lands: the target thrown far straight back and felled, hurt; the Conqueror's lightning on it with Ryou (POINT ONE). */
    static void glare(ServerPlayer player, ServerLevel level, State s) {
        if (!(level.m_6815_(s.stared) instanceof LivingEntity body) || !body.m_6084_()
            || body.m_20182_().m_82554_(player.m_20182_()) > STARE_RANGE + 8.0) {
            return;
        }
        Vec3 at = StyleCombat.middle(body);
        MaguMoves.hurt(player, body, STARE_DAMAGE);
        ConquerorRelease.throwBack(player, level, body, STARE_THROW, STARE_STUN);
        Vec3 way = at.m_82546_(player.m_146892_());
        way = way.m_82556_() < 1.0E-6 ? player.m_20154_() : way.m_82541_();
        ItoMoves.fx(player, STARE, body.m_19879_(), at, way, 1, 0, 0, 0);
        ItoMoves.sound(player, at, ItsuServer.THROWN, 1.6F, 1.0F);
        ConquerorAura.mixed(player, at, 2.5, 6, -1.0);
    }

    /** The first foe on the eye's line (within {@code range}, not behind a block), or null. */
    static LivingEntity sighted(ServerPlayer player, double range) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 eye = player.m_146892_();
        Vec3 end = eye.m_82549_(player.m_20154_().m_82490_(range));
        BlockHitResult wall = level.m_45547_(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 to = wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_() : end;
        LivingEntity best = null;
        double bestAlong = 2.0;
        for (LivingEntity body : level.m_6443_(LivingEntity.class, UoMoves.box(eye, to).m_82400_(1.5),
            e -> e != player && e.m_6084_() && !e.m_5833_() && FruitUtil.foe(player, e))) {
            double along = WaterServer.closest(eye, to, StyleCombat.middle(body));
            Vec3 point = eye.m_82549_(to.m_82546_(eye).m_82490_(along));
            // a little leeway round the body: a glare need not be pixel-true
            if (along < bestAlong && body.m_20191_().m_82400_(0.9).m_82390_(point)) {
                bestAlong = along;
                best = body;
            }
        }
        return best;
    }

    // ------------------------------------------------------------------------------------------------------ C: Enormous Presence

    static void presence(ServerPlayer player, long now) {
        State s = state(player);
        if (s.presenceUntil > now || !ready(player, ItsuKit.PRESENCE)) {
            return;
        }
        cool(player, ItsuKit.PRESENCE, PRESENCE_COOL);
        s.presenceUntil = now + PRESENCE_TICKS;
        s.nextBolt = now + 6;
        ItoMoves.animate(player, "itsu_presence");
        ItoMoves.fx(player, PRESENCE, 1, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, PRESENCE_TICKS);
        play(player, player.m_20182_(), ModSounds.CONQUEROR_RELEASE.get(), 2.0F, 0.7F);
        play(player, player.m_20182_(), ModSounds.CONQUEROR_SUPREME_RUMBLE.get(), 1.2F, 0.8F);
        pressure(player, (ServerLevel) player.m_9236_());
    }

    /** One wave of its Conqueror's Haki: what the release would take near it is pressed down a moment. */
    static void pressure(ServerPlayer player, ServerLevel level) {
        for (LivingEntity body : ConquerorRelease.taken(player, level, PRESENCE_RADIUS)) {
            ConquerorRelease.hold(level, body, PRESENCE_HOLD);
        }
        ItoMoves.fx(player, PULSE, 0, player.m_20182_(), Vec3.f_82478_, PRESENCE_RADIUS, 0, 0, 0);
        play(player, player.m_20182_(), ModSounds.CONQUEROR_CHARGE.get(), 0.9F, 0.6F);
    }

    /** A bolt of the Conqueror's lightning out of the body onto a foe near (hurt, stunned), or into the ground about it. */
    static void bodyBolt(ServerPlayer player, ServerLevel level, long now) {
        Vec3 centre = player.m_20182_().m_82520_(0.0, 1.0, 0.0);
        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity body : PikaMoves.foes(player, centre, PRESENCE_BOLT_RANGE)) {
            double d = body.m_20182_().m_82554_(centre) + player.m_217043_().m_188500_() * 4.0;
            if (d < best) {
                best = d;
                target = body;
            }
        }
        Vec3 to;
        if (target != null) {
            to = StyleCombat.middle(target);
            MaguMoves.hurt(player, target, PRESENCE_DAMAGE);
            HakiFalls.knockDown(level, target, PRESENCE_STUN, false);
        } else {
            double a = player.m_217043_().m_188500_() * Math.PI * 2.0;
            double r = 2.5 + player.m_217043_().m_188500_() * 4.0;
            to = UoMoves.ground(level, centre.m_82520_(Math.cos(a) * r, 0.0, Math.sin(a) * r));
        }
        ItoMoves.fx(player, BODY_BOLT, (int) (now & 0x7FFFFFFF), to, Vec3.f_82478_, 0, 0, 0, 0);
        play(player, to, ModSounds.CONQUEROR_RELEASE.get(), 0.9F, 1.1F + player.m_217043_().m_188501_() * 0.3F);
    }

    // ------------------------------------------------------------------------------------------------------ V: Yokai Blast

    /** Where the pointed right hand is, near enough for the server (every client draws from the pose's own). */
    static Vec3 hand(ServerPlayer player) {
        Vec3 look = player.m_20154_();
        Vec3 flat = new Vec3(look.f_82479_, 0.0, look.f_82481_);
        flat = flat.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.m_82541_();
        Vec3 right = new Vec3(-flat.f_82481_, 0.0, flat.f_82479_);
        return player.m_20182_().m_82520_(0.0, 1.38, 0.0).m_82549_(right.m_82490_(0.36)).m_82549_(look.m_82490_(0.8));
    }

    static void blast(ServerPlayer player, State s, float charge) {
        Vec3 way = player.m_20154_();
        int life = 20 + Math.round(80 * charge);
        double bore = 5.0 + 10.0 * charge;
        double width = 0.7 + 0.5 * charge;
        double length = 28.0 + 12.0 * charge;
        s.blast = new Blast(player, (ServerLevel) player.m_9236_(), way, width, length, bore, life, 4.0F + 3.0F * charge);
        UoMoves.live(s.blast);
        ItoMoves.animate(player, "itsu_blast_fire");
        ItoMoves.fx(player, BLAST, 0, hand(player), way, width, length, charge, life);
        ItoMoves.sound(player, hand(player), com.hexhaki.piece.dragon.UoSounds.BEAM, 2.2F, 0.85F);
        ItoMoves.sound(player, hand(player), com.hexhaki.piece.dragon.UoSounds.FLAME_START, 1.8F, 0.8F);
    }

    /**
     * The Yokai Blast burning: straight along the way it was let go on (never aimed again), from the hand; its front races out, it burns
     * what is in its width every 4 ticks, and where it meets the ground it bores on through as many blocks as the charge allows (Craters:
     * nothing dropped, every block back 30 s later); past that it stops at the wall.
     */
    static final class Blast implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        final Vec3 way;
        final double width;
        final double length;
        final double bore;
        final int life;
        final float damage;
        int age;
        double wallAt = -1.0;

        Blast(ServerPlayer user, ServerLevel level, Vec3 way, double width, double length, double bore, int life, float damage) {
            this.user = user;
            this.level = level;
            this.way = way;
            this.width = width;
            this.length = length;
            this.bore = bore;
            this.life = life;
            this.damage = damage;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            this.age++;
            State s = STATES.get(this.user.m_20148_());
            boolean gone = !this.user.m_6084_() || this.user.m_9236_() != this.level || s == null || s.blast != this || !human(this.user);
            if (gone || this.age > this.life) {
                if (s != null && s.blast == this) {
                    s.blast = null;
                }
                PieceNetwork.around(this.user, S2CUoFx.of(BLAST_OFF, 0, this.user.m_19879_()));
                if (this.user.m_6084_()) {
                    ItoMoves.animate(this.user, "itsu_blast_end");
                }
                return true;
            }
            Vec3 from = hand(this.user);
            double out = Math.min(this.length, this.age * BLAST_SPEED + 1.0);
            Vec3 end = from.m_82549_(this.way.m_82490_(out));
            BlockHitResult wall = this.level.m_45547_(new ClipContext(from, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.user));
            boolean hit = wall.m_6662_() == HitResult.Type.BLOCK;
            double reach = hit ? wall.m_82450_().m_82554_(from) : out;
            if (hit) {
                if (this.wallAt < 0.0) {
                    this.wallAt = reach;
                }
                // through the ground as deep as the charge allows, a little each other tick
                if (reach - this.wallAt < this.bore && this.age % 2 == 0) {
                    Craters.open(this.level, this.user, from.m_82549_(this.way.m_82490_(reach + 0.6)), 1.25 + this.width * 0.3);
                }
                if (this.age % 10 == 0) {
                    ConquerorAura.mixed(this.user, from.m_82549_(this.way.m_82490_(reach)), 2.0, 4, -1.0);
                }
            }
            if (this.age % 4 == 0) {
                DamageSource source = StyleCombat.of(this.user);
                for (LivingEntity body : this.level.m_6443_(LivingEntity.class, UoMoves.box(from, from.m_82549_(this.way.m_82490_(reach)))
                    .m_82400_(this.width + 1.0), e -> e != this.user && e.m_6084_() && !e.m_5833_() && FruitUtil.foe(this.user, e))) {
                    Vec3 rel = StyleCombat.middle(body).m_82546_(from);
                    double along = rel.m_82526_(this.way);
                    if (along < 0.0 || along > reach + 1.0
                        || rel.m_82546_(this.way.m_82490_(along)).m_82553_() > this.width + body.m_20205_() * 0.5) {
                        continue;
                    }
                    body.f_19802_ = 0;
                    if (body.m_6469_(source, this.damage)) {
                        body.m_20254_(4);
                        Impact.hit(this.user, body, StyleCombat.spec(com.hexhaki.piece.style.Hit.Weight.LIGHT,
                            this.way.m_82490_(0.6).m_82520_(0.0, 0.1, 0.0)));
                    }
                }
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ B: Thy Death

    static void death(ServerPlayer player, long now) {
        State s = state(player);
        if (s.stormFrom >= 0 || !ready(player, ItsuKit.DEATH)) {
            return;
        }
        cool(player, ItsuKit.DEATH, DEATH_COOL);
        s.stormFrom = now;
        s.cloudY = player.m_20186_() + CLOUD_HEIGHT;
        ItoMoves.animate(player, "itsu_death");
        ItoMoves.fx(player, STORM, 1, player.m_20182_(), Vec3.f_82478_, s.cloudY, DEATH_RADIUS, 0, DEATH_TICKS);
        play(player, player.m_20182_(), ModSounds.CONQUEROR_SUPREME_RUMBLE.get(), 2.5F, 0.6F);
    }

    /**
     * One bolt of Thy Death: somewhere in the circle round the player (spread over its whole area), onto the ground under it (the first
     * block or water down from the clouds; never into the air), blowing up in a ball DEATH_BLAST round where it lands.
     */
    static void skyBolt(ServerPlayer player, ServerLevel level, State s, long now) {
        double a = player.m_217043_().m_188500_() * Math.PI * 2.0;
        double r = DEATH_RADIUS * Math.sqrt(0.04 + 0.96 * player.m_217043_().m_188500_());
        double x = player.m_20185_() + Math.cos(a) * r;
        double z = player.m_20189_() + Math.sin(a) * r;
        double top = Math.max(s.cloudY - 2.0, player.m_20186_() + 8.0);
        BlockHitResult down = level.m_45547_(new ClipContext(new Vec3(x, top, z), new Vec3(x, player.m_20186_() - 48.0, z),
            ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null));
        if (down.m_6662_() != HitResult.Type.BLOCK) {
            return; // nothing under it to strike: the next one lands
        }
        Vec3 at = down.m_82450_();
        double reach = DEATH_BLAST + 0.5;
        for (LivingEntity body : level.m_6443_(LivingEntity.class, UoMoves.box(at, at).m_82400_(reach + 1.0),
            e -> e != player && e.m_6084_() && !e.m_5833_() && FruitUtil.foe(player, e))) {
            if (body.m_20191_().m_82400_(reach).m_82390_(at)) {
                MaguMoves.hurt(player, body, DEATH_DAMAGE);
                HakiFalls.knockDown(level, body, DEATH_STUN, false);
            }
        }
        ItoMoves.fx(player, SKY_BOLT, (int) (now & 0x7FFFFFFF), at, Vec3.f_82478_, s.cloudY, DEATH_BLAST, 0, 0);
        ItoMoves.sounds(player, at, com.hexhaki.piece.dragon.UoSounds.EXPLODE, 1.6F, 1.15F + player.m_217043_().m_188501_() * 0.3F, 2, 5);
        if (now % 2 == 0) {
            play(player, at, ModSounds.LAST_STAND_LIGHTNING.get(), 1.6F, 0.9F + player.m_217043_().m_188501_() * 0.2F);
        }
    }

    // ------------------------------------------------------------------------------------------------------ each tick

    public static void tick(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        long now = level.m_46467_();
        if (!human(player)) {
            // turned: what the human was doing stops (the storm overhead plays out)
            stopHuman(player, s, now);
        }
        if (s.stareAt >= 0 && now >= s.stareAt) {
            s.stareAt = -1;
            glare(player, level, s);
        }
        if (s.presenceUntil > now) {
            long left = s.presenceUntil - now;
            if (left % PRESENCE_EVERY == 0) {
                pressure(player, level);
            }
            if (now >= s.nextBolt) {
                s.nextBolt = now + 10 + player.m_217043_().m_188503_(9);
                bodyBolt(player, level, now);
            }
        }
        if (s.stormFrom >= 0) {
            long age = now - s.stormFrom;
            if (age >= DEATH_TICKS) {
                s.stormFrom = -1;
            } else if (age >= DEATH_GATHER && age % DEATH_EVERY == 0) {
                skyBolt(player, level, s, now);
            }
        }
    }

    static void stopHuman(ServerPlayer player, State s, long now) {
        if (s.chargeFrom >= 0) {
            s.chargeFrom = -1;
            ItoMoves.fx(player, CHARGE, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
        }
        s.blast = null; // it sees it is not its own any more and goes out
        if (s.presenceUntil > now) {
            s.presenceUntil = -1;
            ItoMoves.fx(player, PRESENCE, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
        }
        s.stareAt = -1;
    }

    public static void forget(ServerPlayer player) {
        State s = STATES.remove(player.m_20148_());
        if (s != null) {
            long now = player.m_9236_().m_46467_();
            stopHuman(player, s, now);
            if (s.stormFrom >= 0) {
                ItoMoves.fx(player, STORM, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
            }
        }
    }
}
