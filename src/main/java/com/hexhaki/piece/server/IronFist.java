package com.hexhaki.piece.server;

import com.hexhaki.data.HakiData;
import com.hexhaki.fruit.FruitUtil;
import com.hexhaki.fx.Impact;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.ironfist.IronMove;
import com.hexhaki.piece.ironfist.IronSounds;
import com.hexhaki.piece.network.PieceNetwork;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.5.8: Iron Fist made anew as Garp's fighting style (the owner's seven moves, verbatim in docs/handoff 8zzs; their keys: LMB punches,
 * RMB Fist of Love, Z Meteor Fist held, X Mountain Crusher, C Blue Hole, V Galaxy Divide, B Infinitum Explosion, N Galaxy Impact). The
 * server decides every blow and every block torn out, and sends one small message for each thing to draw (S2CUoFx kinds 320 to 339:
 * client/ironfist/IronFx draws them on the beats of ironfist/IronMove). Terrain torn out grows back by itself (server/Scars: nothing
 * dropped, nothing protected or unbreakable touched, the world's mobGriefing rule obeyed). The Ryou moves throw the Conqueror's
 * release's own bolts (ConquerorAura.bolts). No impact frame on a hit (3.5.9: only a clash has one).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class IronFist {
    public static final int PUNCH = 320;
    public static final int LOVE = 321;
    /** Meteor Fist held: bx 1 (ticks: the longest hold), 0 let go of with nothing thrown. */
    public static final int METEOR = 322;
    /** The cannonball flying: id, x, y, z from, a its step a tick, size its radius, ticks its flight, by its charge. */
    public static final int BALL = 323;
    /** The cannonball bursting: id, x, y, z, size the blast's radius. */
    public static final int BALL_END = 324;
    /** Mountain Crusher: bx 0 the coil (ticks), 1 the uppercut (x, y, z the fist, a the launch, size its strength). */
    public static final int CRUSHER = 325;
    /** Blue Hole: bx 0 the uppercut wound (x, y, z the fist, ticks), 1 it lands (x, y, z the fist, size its strength), 2 the leap after
     *  the body (x, y, z to, a from, ticks), 3 the grab at the top (id the body, x, y, z, ticks held), 4 a miss or cut short, 5 the throw
     *  down (x, y, z the top, a the ground, ticks), 6 the user's way down (x, y, z to, a from, ticks), 7 the hole (x, y, z its mouth, size
     *  its depth). */
    public static final int HOLE = 326;
    /** Galaxy Divide: bx 0 the crouch, 1 the leap (x, y, z to, a from, ticks), 2 the shock (x, y, z its start on the ground, a the flat way,
     *  size its length, ticks its run). */
    public static final int DIVIDE = 327;
    /** Infinitum Explosion: bx 1 begun (ticks), 2 a blast (x, y, z, a the way, by the punch's number), 3 the finish (x, y, z, a), 0 cut short. */
    public static final int INF = 328;
    /** Galaxy Impact: bx 1 begun (x, y, z the feet, ticks), 2 the punch (x, y, z the ground struck, by how far below the fist), 0 cut short. */
    public static final int IMPACT = 329;
    /** Chunks of the ground thrown: id the seed, x, y, z, size how far, by how high, ticks how many. */
    public static final int DEBRIS = 330;

    /** C2SStyleAction's N key (Galaxy Impact) and a key let go (its slot + RELEASE). */
    public static final int N_KEY = 32;
    public static final int RELEASE = 16;

    static final class State {
        final Map<IronMove, Long> ready = new EnumMap<>(IronMove.class);
        long busyUntil;
        long punchNext;
        boolean left = true;
        long meteorSince = -1;
        long since;
        IronMove doing;
        boolean mayfly;
        boolean lifting;
        long fallSafeUntil;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final List<UoMoves.Live> LIVE = new ArrayList<>();
    private static int seeds;

    private IronFist() {
    }

    static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.m_20148_(), k -> new State());
    }

    static long now(ServerPlayer player) {
        return player.m_9236_().m_46467_();
    }

    static void fx(ServerPlayer player, int kind, int id, Vec3 at, Vec3 a, double bx, double by, double size, int ticks) {
        ItoMoves.fx(player, kind, id, at, a, bx, by, size, ticks);
    }

    static void sound(ServerPlayer player, Vec3 at, String name, float volume, float pitch) {
        ItoMoves.sound(player, at, name, volume, pitch);
    }

    static void animate(ServerPlayer player, String name) {
        FruitUtil.animate(player, name, 2);
    }

    static void later(ServerPlayer player, int ticks, Runnable action) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        LIVE.add(new UoMoves.Later(level, now(player) + Math.max(0, ticks), () -> {
            if (player.m_6084_()) {
                action.run();
            }
        }));
    }

    static boolean armament(ServerPlayer player) {
        try {
            return HakiData.flag(player, "armamentOn");
        } catch (RuntimeException e) {
            return false;
        }
    }

    static Vec3 flat(ServerPlayer player) {
        return MaguMoves.flat(player);
    }

    static Vec3 chest(ServerPlayer player) {
        return ItoMoves.chest(player);
    }

    /** The right fist, roughly (the server's guess: clients draw it where the clip puts it). */
    static Vec3 fist(ServerPlayer player, double out) {
        Vec3 look = player.m_20154_();
        Vec3 f = flat(player);
        Vec3 right = new Vec3(-f.f_82481_, 0, f.f_82479_);
        return chest(player).m_82549_(right.m_82490_(0.3)).m_82549_(look.m_82490_(out));
    }

    static Vec3 ground(ServerLevel level, Vec3 at, double up, double down) {
        Vec3 g = ItoMoves.ground(level, at, up, down);
        return g != null ? g : at;
    }

    /** A blow on {@code body}: damage, a push, the stagger; true if it landed. */
    static boolean hit(ServerPlayer player, LivingEntity body, float damage, Vec3 push, int stun) {
        if (!MaguMoves.hurt(player, body, damage)) {
            return false;
        }
        StyleCombat.push(body, body.m_20184_().m_82490_(0.2).m_82549_(push));
        if (stun > 0) {
            body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, stun, 3, false, false, false));
        }
        return true;
    }

    /** Living foes within {@code reach} in front of the player and {@code arc} degrees each side of the look. */
    static List<LivingEntity> ahead(ServerPlayer player, double reach, double arc) {
        return ItoMoves.ahead(player, reach, arc);
    }

    static void debris(ServerPlayer player, Vec3 at, double reach, double lift, int count) {
        fx(player, DEBRIS, ++seeds, at, Vec3.f_82478_, 0, lift, reach, count);
    }

    static boolean cooled(ServerPlayer player, State s, IronMove move, long now) {
        Long ready = s.ready.get(move);
        if (ready != null && now < ready && !OpMode.on(player)) { // 3.6.2: no cooldowns in op mode
            player.m_5661_(Component.m_237113_(move.display() + ": " + String.format(java.util.Locale.ROOT, "%.1f", (ready - now) / 20.0) + "s"),
                true);
            return false;
        }
        return true;
    }

    static boolean needs(ServerPlayer player, IronMove move) {
        boolean ok = switch (move.need()) {
            case NONE -> true;
            case ARMAMENT -> armament(player);
            case RYOU -> ConquerorAura.ryou(player);
            case CONQUEROR_RYOU -> ConquerorAura.ryou(player) && ConquerorAura.conqueror(player);
        };
        if (!ok) {
            player.m_5661_(Component.m_237113_(move.display() + ": needs " + move.need().text + " on"), true);
        }
        return ok;
    }

    static void cool(State s, IronMove move, long now, int ticks) {
        s.ready.put(move, now + ticks);
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    /** server/StyleServer: an Iron Fist fighter in combat mode pressed {@code code} (a Slot's number, N_KEY, or + RELEASE: let go). */
    public static void press(ServerPlayer player, int code) {
        State s = state(player);
        long now = now(player);
        if (code >= RELEASE && code < N_KEY) {
            if (code - RELEASE == com.hexhaki.piece.style.Slot.SPECIAL_A.ordinal() && s.meteorSince >= 0) {
                meteorThrow(player, s, now);
            }
            return;
        }
        if (now < s.busyUntil || player.m_20159_()) {
            return;
        }
        if (code == N_KEY) {
            impact(player, s, now);
            return;
        }
        com.hexhaki.piece.style.Slot slot = com.hexhaki.piece.style.Slot.byOrdinal(code);
        switch (slot) {
            case M1_1, M1_2, M1_3, M1_4 -> punch(player, s, now);
            case HEAVY -> love(player, s, now);
            case SPECIAL_A -> meteorHold(player, s, now);
            case SPECIAL_B -> crusher(player, s, now);
            case EXTRA_C -> blueHole(player, s, now);
            case EXTRA_V -> divide(player, s, now);
            case EXTRA_B -> infinitum(player, s, now);
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ LMB: the punches

    static void punch(ServerPlayer player, State s, long now) {
        if (now < s.punchNext) {
            return;
        }
        s.punchNext = now + IronMove.PUNCH_EVERY;
        s.busyUntil = now + IronMove.PUNCH_HIT + 1;
        boolean left = s.left;
        s.left = !left;
        animate(player, left ? "style_brawler_punch_l" : "style_brawler_punch_r");
        sound(player, chest(player), IronSounds.SWING, 0.6F, 1.2F + (left ? 0.0F : 0.1F));
        boolean[] cut = ClashBoard.swing(player, now, now + IronMove.PUNCH_HIT, IronMove.PUNCH_REACH, armament(player) ? 5.0 : 4.0,
            com.hexhaki.piece.clash.ClashRules.Kind.FIST); // 3.8.4: on the clash board
        later(player, IronMove.PUNCH_HIT, () -> {
            if (ClashBoard.lands(player, cut)) {
                return; // met by a blow coming back: the clash played instead
            }
            Vec3 f = flat(player);
            boolean landed = false;
            for (LivingEntity body : ahead(player, IronMove.PUNCH_REACH, 35)) {
                landed |= hit(player, body, armament(player) ? 5.0F : 4.0F, f.m_82490_(0.35).m_82520_(0, 0.08, 0), 0);
            }
            fx(player, PUNCH, 0, fist(player, 1.6), player.m_20154_(), left ? 0 : 1, landed ? 1 : 0, 0, 0);
            if (landed) {
                sound(player, fist(player, 1.6), IronSounds.PUNCH, 0.8F, 1.1F);
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ RMB: Fist of Love

    static void love(ServerPlayer player, State s, long now) {
        if (!cooled(player, s, IronMove.LOVE, now)) {
            return;
        }
        cool(s, IronMove.LOVE, now, IronMove.LOVE.cooldown());
        s.busyUntil = now + IronMove.LOVE_TICKS - 2;
        s.punchNext = now + IronMove.LOVE_TICKS - 2;
        animate(player, "style_brawler_love");
        later(player, IronMove.LOVE_HIT, () -> {
            LivingEntity body = PikaMoves.aimed(player, IronMove.LOVE_REACH, 0.6);
            if (body == null) {
                List<LivingEntity> near = ahead(player, IronMove.LOVE_REACH, 40);
                body = near.isEmpty() ? null : near.get(0);
            }
            Vec3 at = body != null ? body.m_20182_().m_82520_(0, body.m_20206_() + 0.1, 0) : fist(player, 2.2).m_82520_(0, 0.6, 0);
            boolean landed = body != null && hit(player, body, armament(player) ? 8.0F : 6.5F, new Vec3(0, -0.7, 0), 16);
            fx(player, LOVE, ++seeds, at, player.m_20154_(), 0, landed ? 1 : 0, 1.0, 0);
            sound(player, at, landed ? IronSounds.PUNCH : IronSounds.SWING, 1.0F, landed ? 0.85F : 1.1F);
            if (landed) {
                Impact.quake((ServerLevel) player.m_9236_(), at, 8.0, 0.3F, 4);
                ItoMoves.lightning(player, at);
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ Z: Meteor Fist

    static void meteorHold(ServerPlayer player, State s, long now) {
        if (s.meteorSince >= 0 || !cooled(player, s, IronMove.METEOR, now)) {
            return;
        }
        s.meteorSince = now;
        animate(player, "style_brawler_meteor_hold");
        fx(player, METEOR, 0, chest(player), player.m_20154_(), 1, 0, 0, IronMove.METEOR_MOST);
        sound(player, chest(player), IronSounds.IRON, 0.9F, 0.8F);
    }

    static void meteorThrow(ServerPlayer player, State s, long now) {
        double charge = IronMove.meteorCharge(now - s.meteorSince);
        s.meteorSince = -1;
        s.busyUntil = now + IronMove.METEOR_THROW + 4;
        cool(s, IronMove.METEOR, now, IronMove.METEOR.cooldown());
        animate(player, "style_brawler_meteor_throw");
        later(player, IronMove.METEOR_THROW, () -> {
            Vec3 look = player.m_20154_();
            Vec3 from = fist(player, 1.2).m_82520_(0, 0.5, 0);
            double speed = IronMove.meteorSpeed(charge);
            Vec3 step = look.m_82490_(speed);
            int seed = ++seeds;
            fx(player, BALL, seed, from, step, 0, charge, IronMove.METEOR_RADIUS, IronMove.METEOR_FLIGHT);
            fx(player, METEOR, 0, from, look, 0, 0, 0, 0);
            sound(player, from, IronSounds.THROW, 1.1F, 0.9F);
            LIVE.add(new Ball((ServerLevel) player.m_9236_(), player, from, step, charge, now(player) + IronMove.METEOR_FLIGHT, seed));
        });
    }

    /** The cannonball in flight: it bursts on the first body or block it meets, or at the end of its flight. */
    static final class Ball implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        Vec3 at;
        final Vec3 step;
        final double charge;
        final long until;
        final int seed;

        Ball(ServerLevel level, ServerPlayer owner, Vec3 at, Vec3 step, double charge, long until, int seed) {
            this.level = level;
            this.owner = owner;
            this.at = at;
            this.step = step;
            this.charge = charge;
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
            double r = IronMove.METEOR_RADIUS;
            Vec3 u = this.step.m_82541_();
            double len = this.step.m_82553_();
            for (LivingEntity body : ItoMoves.foes(this.owner, this.at.m_82549_(next).m_82490_(0.5), len * 0.5 + r + 1.0)) {
                Vec3 d = StyleCombat.middle(body).m_82546_(this.at);
                double along = Math.max(0.0, Math.min(len, d.m_82526_(u)));
                if (d.m_82546_(u.m_82490_(along)).m_82553_() < r + body.m_20205_() * 0.6 + 0.3) {
                    burst(this.at.m_82549_(u.m_82490_(along)));
                    return true;
                }
            }
            BlockHitResult block = this.level.m_45547_(new ClipContext(this.at, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
            if (block.m_6662_() == HitResult.Type.BLOCK || now >= this.until) {
                burst(block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_() : next);
                return true;
            }
            this.at = next;
            return false;
        }

        void burst(Vec3 where) {
            double blast = IronMove.meteorBlast(this.charge);
            ServerPlayer player = this.owner;
            fx(player, BALL_END, this.seed, where, this.step.m_82541_(), 0, this.charge, blast, 0);
            float damage = (float) (10.0 + 12.0 * this.charge) * (armament(player) ? 1.2F : 1.0F);
            for (LivingEntity body : ItoMoves.foes(player, where, blast + 1.0)) {
                Vec3 away = StyleCombat.middle(body).m_82546_(where);
                double d = away.m_82553_();
                double fall = Math.max(0.35, 1.0 - d / (blast + 1.0));
                Vec3 push = (d < 1.0E-3 ? this.step.m_82541_() : away.m_82490_(1.0 / d)).m_82490_(1.4 + 1.0 * this.charge).m_82520_(0, 0.45, 0);
                hit(player, body, damage * (float) fall, push.m_82490_(fall), 0);
            }
            Craters.open(this.level, player, where, 1.8 + 2.4 * this.charge);
            debris(player, where, blast * 1.6, 0.55 + 0.3 * this.charge, (int) (8 + 10 * this.charge));
            Impact.quake(this.level, where, 14.0 + 10.0 * this.charge, 0.5F + 0.3F * (float) this.charge, 8);
            ConquerorAura.exploded(player, this.level, where, blast);
            sound(player, where, IronSounds.BOOM, 1.4F + 0.4F * (float) this.charge, 0.95F - 0.15F * (float) this.charge);
        }
    }

    // ------------------------------------------------------------------------------------------------------ X: Mountain Crusher

    static void crusher(ServerPlayer player, State s, long now) {
        if (!cooled(player, s, IronMove.CRUSHER, now) || !needs(player, IronMove.CRUSHER)) {
            return;
        }
        cool(s, IronMove.CRUSHER, now, IronMove.CRUSHER.cooldown());
        s.busyUntil = now + IronMove.CRUSHER_TICKS;
        animate(player, "style_brawler_crusher");
        fx(player, CRUSHER, 0, fist(player, 0.4), player.m_20154_(), 0, 0, 0, IronMove.CRUSHER_COIL);
        sound(player, chest(player), IronSounds.CHARGE, 0.9F, 1.1F);
        later(player, IronMove.CRUSHER_COIL, () -> StyleCombat.push(player, flat(player).m_82490_(0.9).m_82520_(0, 0.05, 0)));
        later(player, IronMove.CRUSHER_HIT, () -> {
            Vec3 f = flat(player);
            Vec3 launch = f.m_82490_(1.5).m_82520_(0, 1.7, 0);
            Vec3 at = fist(player, 1.5);
            LivingEntity target = PikaMoves.aimed(player, IronMove.CRUSHER_REACH, 0.6);
            if (target == null) {
                List<LivingEntity> near = ahead(player, IronMove.CRUSHER_REACH, 45);
                target = near.isEmpty() ? null : near.get(0);
            }
            fx(player, CRUSHER, ++seeds, at, launch.m_82541_(), 1, 0, target != null ? 1.0 : 0.7, 0);
            sound(player, at, IronSounds.SMASH, 1.4F, 0.8F);
            boolean ryou = ConquerorAura.ryou(player);
            ConquerorAura.bolts(player, at, ryou ? 5.0 : 3.5, ryou ? 8 : 5, -1.4, 1.0);
            if (target == null) {
                return;
            }
            // it breaks any guard: a raised shield knocked aside first, the blow lands whole
            if (target instanceof Player body && body.m_21254_()) {
                body.m_36384_(true);
            }
            if (hit(player, target, ryou ? 26.0F : 20.0F, launch, 0)) {
                Impact.quake((ServerLevel) player.m_9236_(), at, 16.0, 0.6F, 8);
                LIVE.add(new Tunnel((ServerLevel) player.m_9236_(), player, target, now(player) + IronMove.CRUSHER_TEAR));
            }
        });
    }

    /** A body launched by Mountain Crusher, tearing a way through the blocks in its path. */
    static final class Tunnel implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final LivingEntity body;
        final long until;

        Tunnel(ServerLevel level, ServerPlayer owner, LivingEntity body, long until) {
            this.level = level;
            this.owner = owner;
            this.body = body;
            this.until = until;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (!this.body.m_6084_() || now >= this.until || !griefing(this.level)) {
                return true;
            }
            Scars scars = Scars.of(this.level);
            Vec3 p = this.body.m_20182_().m_82549_(this.body.m_20184_());
            double w = this.body.m_20205_() * 0.5 + 0.6;
            double h = this.body.m_20206_() + 0.4;
            List<double[]> none = List.of();
            for (int x = (int) Math.floor(p.f_82479_ - w); x <= (int) Math.floor(p.f_82479_ + w); x++) {
                for (int y = (int) Math.floor(p.f_82480_); y <= (int) Math.floor(p.f_82480_ + h); y++) {
                    for (int z = (int) Math.floor(p.f_82481_ - w); z <= (int) Math.floor(p.f_82481_ + w); z++) {
                        if (scars.room()) {
                            scars.tear(this.owner, new BlockPos(x, y, z), none, Craters.REPAIR);
                        }
                    }
                }
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ C: Blue Hole

    /**
     * 3.5.9 (the owner after 3.5.8: "Uppercut / Did it hit someone? No? Ends here. If Yes: the entity goes flying then the user catches
     * up (jumps to the entity) grabs them and throws them down to the floor, hard"): an uppercut; on a miss the move ends there (a short
     * cooldown). On a hit the body is carried up ({@link Flight}), the user leaps after it (their own client carries them, bx 2), grabs it
     * at the top, throws it down into the ground (the hole) and comes down after it to the hole's edge (bx 6).
     */
    static void blueHole(ServerPlayer player, State s, long now) {
        if (!cooled(player, s, IronMove.BLUE_HOLE, now) || !needs(player, IronMove.BLUE_HOLE)) {
            return;
        }
        cool(s, IronMove.BLUE_HOLE, now, IronMove.HOLE_MISS_COOLDOWN);
        s.busyUntil = now + IronMove.HOLE_HIT + 4;
        animate(player, "style_brawler_blue_hole");
        fx(player, HOLE, 0, fist(player, 0.4), player.m_20154_(), 0, 0, 0, IronMove.HOLE_HIT);
        sound(player, chest(player), IronSounds.SWING, 1.0F, 0.8F);
        later(player, IronMove.HOLE_HIT, () -> uppercut(player, s));
    }

    static void uppercut(ServerPlayer player, State s) {
        Vec3 f = flat(player);
        Vec3 at = fist(player, 1.1).m_82520_(0, 0.3, 0);
        LivingEntity aimed = PikaMoves.aimed(player, IronMove.HOLE_REACH, 0.6);
        if (aimed == null) {
            List<LivingEntity> near = ahead(player, IronMove.HOLE_REACH, 45);
            aimed = near.isEmpty() ? null : near.get(0);
        }
        boolean ryou = ConquerorAura.ryou(player);
        if (aimed == null || !hit(player, aimed, ryou ? 12.0F : 9.0F, Vec3.f_82478_, 0)) {
            // no one there: the swing cuts the air and the move ends here (the clip let go of after the uppercut's follow-through)
            fx(player, HOLE, ++seeds, at, new Vec3(0, 1, 0), 4, 0, 0, 0);
            later(player, 1, () -> FruitUtil.animate(player, "__clear__", 4));
            return;
        }
        LivingEntity target = aimed;
        ServerLevel level = (ServerLevel) player.m_9236_();
        long now = now(player);
        cool(s, IronMove.BLUE_HOLE, now, IronMove.BLUE_HOLE.cooldown());
        s.busyUntil = now + IronMove.HOLE_TICKS - IronMove.HOLE_HIT;
        safeFall(player, s, now + IronMove.HOLE_TICKS + 40);
        Vec3 start = target.m_20182_();
        Vec3 apex = start.m_82520_(0, headroom(level, target, IronMove.HOLE_HEIGHT), 0);
        Vec3 mouth = ground(level, start, 1.5, 24.0);
        Vec3 d = new Vec3(start.f_82479_ - player.m_20185_(), 0, start.f_82481_ - player.m_20189_());
        Vec3 way = d.m_82556_() < 1.0E-4 ? f : d.m_82541_();
        Flight flight = new Flight(level, player, target, now, start, apex, mouth);
        LIVE.add(flight);
        fx(player, HOLE, ++seeds, at, new Vec3(0, 1, 0), 1, 0, ryou ? 1.0 : 0.8, 0);
        sound(player, at, IronSounds.SMASH, 1.3F, 0.95F);
        ConquerorAura.bolts(player, at, ryou ? 4.5 : 3.0, ryou ? 7 : 4, -1.2, 1.0);
        // after it: up to where the right hand meets its face at the top
        later(player, IronMove.HOLE_JUMP, () -> {
            if (flight.over) {
                return;
            }
            Vec3 from = player.m_20182_();
            Vec3 to = apex.m_82549_(way.m_82490_(-(0.95 + target.m_20205_() * 0.45))).m_82520_(0, target.m_20206_() * 0.85 - 1.6, 0);
            fx(player, HOLE, ++seeds, to, from, 2, 0, 0, IronMove.HOLE_LEAP);
            fly(player, s, true);
            sound(player, from, IronSounds.DASH, 1.1F, 1.0F);
            debris(player, from, 2.5, 0.3, 5);
        });
        later(player, IronMove.HOLE_RISE, () -> {
            if (flight.over) {
                return;
            }
            fx(player, HOLE, target.m_19879_(), apex, way, 3, 0, 0, IronMove.HOLE_HANG);
            sound(player, apex, IronSounds.PUNCH, 1.0F, 0.7F);
            ConquerorAura.bolts(player, apex.m_82520_(0, target.m_20206_() * 0.6, 0), 3.0, 4, -1.5, 1.0);
        });
        int top = IronMove.HOLE_RISE + IronMove.HOLE_HANG;
        later(player, top, () -> {
            if (flight.over) {
                return;
            }
            fx(player, HOLE, ++seeds, apex, mouth, 5, 0, 0, IronMove.HOLE_FALL);
            sound(player, apex, IronSounds.THROW, 1.4F, 0.7F);
        });
        // and down after it, to the hole's edge on the side it came from
        later(player, top + 1, () -> {
            if (flight.over && !flight.slammed) {
                return;
            }
            Vec3 edge = mouth.m_82549_(way.m_82490_(-(2.8 + target.m_20205_() * 0.5)));
            Vec3 land = ground(level, edge, 3.0, 30.0);
            fx(player, HOLE, ++seeds, land, player.m_20182_(), 6, 0, 0, IronMove.HOLE_DROP);
        });
    }

    /** How far {@code body} can go straight up, at most {@code most} blocks (short of a ceiling). */
    static double headroom(ServerLevel level, LivingEntity body, double most) {
        Vec3 head = body.m_20182_().m_82520_(0, body.m_20206_(), 0);
        BlockHitResult block = level.m_45547_(new ClipContext(head, head.m_82520_(0, most + 0.5, 0), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, null));
        if (block.m_6662_() != HitResult.Type.BLOCK) {
            return most;
        }
        return Math.max(0.0, Math.min(most, block.m_82450_().f_82480_ - head.f_82480_ - 0.4));
    }

    /** Where a dash along {@code f} from {@code from} stops: {@code most} on, or short of the first wall. */
    static Vec3 blocked(ServerPlayer player, Vec3 from, Vec3 f, double most) {
        Vec3 want = from.m_82549_(f.m_82490_(most));
        BlockHitResult block = player.m_9236_().m_45547_(new ClipContext(from.m_82520_(0, 1.0, 0), want.m_82520_(0, 1.0, 0), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, player));
        return block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_().m_82520_(0, -1.0, 0).m_82546_(f.m_82490_(0.8)) : want;
    }

    /**
     * The uppercut's body: flown up (IronMove.holeRise), held at the top while the user grabs it, thrown down onto {@code mouth} (faster
     * and faster), then the hole ({@link #slam}). Carried every tick (no fall, no drift); let go if either dies.
     */
    static final class Flight implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final LivingEntity body;
        final long born;
        final Vec3 start;
        final Vec3 apex;
        final Vec3 mouth;
        boolean over;
        boolean slammed;

        Flight(ServerLevel level, ServerPlayer owner, LivingEntity body, long born, Vec3 start, Vec3 apex, Vec3 mouth) {
            this.level = level;
            this.owner = owner;
            this.body = body;
            this.born = born;
            this.start = start;
            this.apex = apex;
            this.mouth = mouth;
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
            if (!this.body.m_6084_() || !this.owner.m_6084_() || !STATES.containsKey(this.owner.m_20148_())) {
                this.over = true;
                if (this.owner.m_6084_()) {
                    fx(this.owner, HOLE, 0, this.owner.m_20182_(), Vec3.f_82478_, 4, 0, 0, 0);
                    FruitUtil.animate(this.owner, "__clear__", 4);
                }
                return true;
            }
            double t = now - this.born;
            int top = IronMove.HOLE_RISE + IronMove.HOLE_HANG;
            Vec3 p;
            if (t <= IronMove.HOLE_RISE) {
                double up = this.apex.f_82480_ - this.start.f_82480_;
                p = this.start.m_82520_(0, up * IronMove.holeRise(t) / IronMove.HOLE_HEIGHT, 0);
            } else if (t <= top) {
                p = this.apex;
            } else if (t < top + IronMove.HOLE_FALL) {
                p = this.apex.m_82549_(this.mouth.m_82546_(this.apex).m_82490_(IronMove.holeThrow(t - top)));
            } else {
                this.over = true;
                this.slammed = true;
                slam(this.owner, this.body, this.mouth);
                return true;
            }
            this.body.m_20256_(Vec3.f_82478_);
            this.body.f_19864_ = true;
            this.body.m_6021_(p.f_82479_, p.f_82480_, p.f_82481_);
            this.body.f_19789_ = 0.0F;
            return false;
        }
    }

    static void slam(ServerPlayer player, LivingEntity target, Vec3 mouth) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        int bx = (int) Math.floor(mouth.f_82479_);
        int by = (int) Math.floor(mouth.f_82480_ - 0.01);
        int bz = (int) Math.floor(mouth.f_82481_);
        // the hole: an irregular shaft straight down, fifteen blocks
        List<int[]> cells = new ArrayList<>();
        for (int y = 0; y < IronMove.HOLE_DEPTH; y++) {
            double r = 1.1 + 0.55 * Craters.noise(bx, by - y, bz) + (y < 2 ? 0.8 : 0.0);
            int ri = (int) Math.ceil(r);
            for (int x = -ri; x <= ri; x++) {
                for (int z = -ri; z <= ri; z++) {
                    double jag = r * (0.85 + 0.3 * Craters.noise(bx + x, by - y, bz + z));
                    if (x * x + z * z <= jag * jag) {
                        cells.add(new int[] {bx + x, by - y, bz + z, y});
                    }
                }
            }
        }
        dig(level, player, cells, 0.0, Craters.REPAIR + 200);
        fx(player, HOLE, ++seeds, mouth, flat(player), 7, 0, IronMove.HOLE_DEPTH, 0);
        Craters.open(level, player, mouth, 2.6);
        debris(player, mouth, 7.0, 0.8, 16);
        Impact.quake(level, mouth, 22.0, 0.75F, 12);
        ConquerorAura.bolts(player, mouth.m_82520_(0, 0.5, 0), 5.0, 9, -0.5, 1.3);
        sound(player, mouth, IronSounds.SLAM, 1.6F, 0.75F);
        if (!target.m_6084_()) {
            return;
        }
        hit(player, target, ConquerorAura.ryou(player) ? 24.0F : 20.0F, new Vec3(0, -1.2, 0), IronMove.HOLE_STUN);
        later(player, 2, () -> {
            if (!target.m_6084_()) {
                return;
            }
            // to the bottom of what could be dug
            int y = by;
            while (y > by - IronMove.HOLE_DEPTH + 1 && level.m_8055_(new BlockPos(bx, y - 1, bz)).m_60795_()) {
                y--;
            }
            target.m_6021_(bx + 0.5, y, bz + 0.5);
            target.m_20256_(new Vec3(0, -1.0, 0));
            target.f_19864_ = true;
            HakiFalls.knockDown(level, target, IronMove.HOLE_STUN, false);
        });
    }

    // ------------------------------------------------------------------------------------------------------ V: Galaxy Divide

    static void divide(ServerPlayer player, State s, long now) {
        if (!cooled(player, s, IronMove.DIVIDE, now) || !needs(player, IronMove.DIVIDE)) {
            return;
        }
        cool(s, IronMove.DIVIDE, now, IronMove.DIVIDE.cooldown());
        s.busyUntil = now + IronMove.DIVIDE_PUNCH + 10;
        animate(player, "style_brawler_divide");
        fx(player, DIVIDE, 0, player.m_20182_(), flat(player), 0, 0, 0, IronMove.DIVIDE_CROUCH);
        safeFall(player, s, now + 120);
        LivingEntity aimed = PikaMoves.aimed(player, 40.0, 0.6);
        later(player, IronMove.DIVIDE_CROUCH, () -> {
            Vec3 from = player.m_20182_();
            Vec3 f = flat(player);
            if (aimed != null && aimed.m_6084_()) {
                Vec3 d = aimed.m_20182_().m_82546_(from);
                Vec3 df = new Vec3(d.f_82479_, 0, d.f_82481_);
                if (df.m_82556_() > 1.0E-3) {
                    f = df.m_82541_();
                }
            }
            Vec3 to = blocked(player, from, f, 7.0).m_82520_(0, 7.0, 0);
            fx(player, DIVIDE, ++seeds, to, from, 1, 0, 0, IronMove.DIVIDE_LEAP);
            fly(player, s, true);
            sound(player, from, IronSounds.DASH, 1.2F, 0.8F);
            debris(player, from, 3.5, 0.4, 8);
            Impact.quake((ServerLevel) player.m_9236_(), from, 10.0, 0.4F, 6);
            for (int k = 2; k < IronMove.DIVIDE_PUNCH - IronMove.DIVIDE_CROUCH; k += 3) {
                later(player, k, () -> ConquerorAura.bolts(player, fist(player, 0.3).m_82520_(0, 0.4, 0), 3.5, 5, -3.0, 1.2));
            }
            Vec3 way = f;
            later(player, IronMove.DIVIDE_PUNCH - IronMove.DIVIDE_CROUCH, () -> divideShock(player, s, way));
        });
    }

    static void divideShock(ServerPlayer player, State s, Vec3 f) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        fly(player, s, false);
        Vec3 base = ground(level, player.m_20182_().m_82549_(f.m_82490_(2.0)), 2.0, 14.0);
        int run = (int) Math.ceil(IronMove.DIVIDE_LENGTH / IronMove.DIVIDE_SPEED);
        fx(player, DIVIDE, ++seeds, base, f, 2, 0, IronMove.DIVIDE_LENGTH, run);
        sound(player, base, IronSounds.SMASH, 1.6F, 0.7F);
        sound(player, base, IronSounds.RUMBLE, 1.4F, 0.8F);
        // the world split along its line, a slice at a time as the front passes
        Vec3 side = new Vec3(-f.f_82481_, 0, f.f_82479_);
        List<int[]> cells = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (double along = 0.0; along <= IronMove.DIVIDE_LENGTH; along += 0.5) {
            int order = (int) (along / IronMove.DIVIDE_SPEED);
            for (double sd = -IronMove.DIVIDE_WIDTH * 0.5; sd <= IronMove.DIVIDE_WIDTH * 0.5; sd += 0.5) {
                double px = base.f_82479_ + f.f_82479_ * along + side.f_82479_ * sd;
                double pz = base.f_82481_ + f.f_82481_ * along + side.f_82481_ * sd;
                int x = (int) Math.floor(px);
                int z = (int) Math.floor(pz);
                double jag = Math.abs(sd) / (IronMove.DIVIDE_WIDTH * 0.5);
                int bottom = (int) Math.floor(base.f_82480_) - 3 - (int) (4 * (1.0 - jag) * (0.6 + 0.4 * Craters.noise(x, 0, z)));
                int top = (int) Math.floor(base.f_82480_) + (int) (IronMove.DIVIDE_HEIGHT * (0.75 + 0.25 * Craters.noise(x, 7, z)));
                for (int y = bottom; y <= top; y++) {
                    long key = ((long) x & 0x3FFFFFF) << 38 | ((long) z & 0x3FFFFFF) << 12 | (y & 0xFFF);
                    if (seen.add(key)) {
                        cells.add(new int[] {x, y, z, order});
                    }
                }
            }
        }
        dig(level, player, cells, 1.0, Craters.REPAIR + 300);
        Set<Integer> struck = new HashSet<>();
        for (int k = 0; k <= run; k++) {
            int kk = k;
            later(player, k, () -> {
                double front = kk * IronMove.DIVIDE_SPEED;
                Vec3 at = base.m_82549_(f.m_82490_(front));
                for (LivingEntity body : ItoMoves.foes(player, at.m_82520_(0, 6.0, 0), 12.0)) {
                    Vec3 d = body.m_20182_().m_82546_(base);
                    double along = d.m_82526_(f);
                    double sd = d.m_82526_(side);
                    if (along > front - 3.0 && along < front + 1.0 && Math.abs(sd) < IronMove.DIVIDE_WIDTH * 0.5 + 1.2
                        && d.f_82480_ > -3.0 && d.f_82480_ < IronMove.DIVIDE_HEIGHT && struck.add(body.m_19879_())) {
                        Vec3 out = side.m_82490_(sd >= 0 ? 1.0 : -1.0).m_82490_(1.6).m_82549_(f.m_82490_(1.0)).m_82520_(0, 0.8, 0);
                        hit(player, body, 26.0F, out, 30);
                    }
                }
                if (kk % 3 == 0) {
                    ConquerorAura.bolts(player, at.m_82520_(0, 4.0 + 6.0 * FireMesh.rand(kk, 3), 0), 6.0, 6, -4.0, 1.5);
                    debris(player, at, 6.0, 0.7, 6);
                }
            });
        }
        Impact.quake(level, base.m_82549_(f.m_82490_(IronMove.DIVIDE_LENGTH * 0.5)), 40.0, 0.7F, 20);
    }

    // ------------------------------------------------------------------------------------------------------ B: Infinitum Explosion

    static void infinitum(ServerPlayer player, State s, long now) {
        if (!cooled(player, s, IronMove.INFINITUM, now) || !needs(player, IronMove.INFINITUM)) {
            return;
        }
        cool(s, IronMove.INFINITUM, now, IronMove.INFINITUM.cooldown());
        s.busyUntil = now + IronMove.INF_TICKS;
        animate(player, "style_brawler_infinitum");
        fx(player, INF, 0, player.m_20182_(), flat(player), 1, 0, 0, IronMove.INF_TICKS);
        sound(player, chest(player), IronSounds.CHARGE, 1.0F, 0.9F);
        int n = IronMove.barrageCount();
        Set<Integer> held = new HashSet<>();
        for (int k = 0; k < n; k++) {
            int kk = k;
            later(player, IronMove.barrageTick(k), () -> {
                Vec3 f = flat(player);
                Vec3 front = chest(player).m_82549_(f.m_82490_(2.2));
                for (LivingEntity body : ahead(player, 4.8, 55)) {
                    held.add(body.m_19879_());
                    // struck and kept there: drawn in a little to the barrage's front
                    Vec3 in = front.m_82546_(StyleCombat.middle(body)).m_82490_(0.08);
                    hit(player, body, 2.6F, in.m_82520_(0, 0.02, 0), 10);
                }
                fx(player, INF, kk, front, f, 2, kk, 0, 0);
                if (kk % 2 == 0) {
                    sound(player, front, IronSounds.PUNCH, 0.7F, 1.0F + Math.min(0.5F, kk * 0.02F));
                }
                if (kk % 4 == 0) {
                    ConquerorAura.bolts(player, front, 2.8, 3, -1.6, 1.0);
                }
                if (kk % 5 == 0) {
                    StyleCombat.push(player, f.m_82490_(0.28));
                    player.f_19789_ = 0.0F;
                }
                if (kk % 6 == 3) {
                    Vec3 g = ground((ServerLevel) player.m_9236_(), front.m_82549_(f.m_82490_(1.2)), 1.5, 3.0);
                    Craters.open((ServerLevel) player.m_9236_(), player, g, 1.6);
                }
            });
        }
        later(player, IronMove.INF_END + 2, () -> sound(player, chest(player), IronSounds.CHARGE, 1.0F, 1.3F));
        later(player, IronMove.INF_FINISH, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 f = flat(player);
            Vec3 at = chest(player).m_82549_(f.m_82490_(2.0));
            fx(player, INF, ++seeds, at, f, 3, 0, 0, 0);
            for (LivingEntity body : ahead(player, 10.0, 45)) {
                hit(player, body, 20.0F, f.m_82490_(2.8).m_82520_(0, 0.6, 0), 30);
                HakiFalls.knockDown(level, body, 40, false);
            }
            Vec3 g = ground(level, at.m_82549_(f.m_82490_(4.5)), 2.0, 5.0);
            Craters.open(level, player, g, 5.0);
            Craters.open(level, player, ground(level, at.m_82549_(f.m_82490_(9.0)), 2.0, 6.0), 3.5);
            debris(player, g, 9.0, 0.75, 18);
            Impact.quake(level, g, 30.0, 0.85F, 14);
            ConquerorAura.bolts(player, at.m_82549_(f.m_82490_(3.0)), 7.0, 10, -2.0, 1.6);
            StyleCombat.push(player, f.m_82490_(-0.45).m_82520_(0, 0.1, 0));
            sound(player, at, IronSounds.BOOM, 1.7F, 0.75F);
        });
    }

    // ------------------------------------------------------------------------------------------------------ N: Galaxy Impact

    static void impact(ServerPlayer player, State s, long now) {
        if (!cooled(player, s, IronMove.IMPACT, now) || !needs(player, IronMove.IMPACT)) {
            return;
        }
        if (!player.m_20096_()) {
            player.m_5661_(Component.m_237113_("Galaxy Impact: from the ground"), true);
            return;
        }
        float cost = (float) (HakiData.maxEnergy(player) * IronMove.IMPACT_ENERGY);
        if (!HakiData.unlimitedEnergy(player)) {
            if (HakiData.energy(player) < cost && !OpMode.on(player)) { // 3.6.2: op mode spends no energy (HakiData.consume's own)
                player.m_5661_(Component.m_237113_("Galaxy Impact: not enough Haki"), true);
                return;
            }
            HakiData.consume(player, cost);
        }
        cool(s, IronMove.IMPACT, now, IronMove.IMPACT.cooldown());
        s.busyUntil = now + IronMove.IMPACT_TICKS;
        s.doing = IronMove.IMPACT;
        s.since = now;
        Vec3 feet = player.m_20182_();
        animate(player, "style_brawler_impact");
        fx(player, IMPACT, ++seeds, feet, flat(player), 1, 0, 0, IronMove.IMPACT_TICKS);
        safeFall(player, s, now + IronMove.IMPACT_TICKS + 60);
        fly(player, s, true);
        sound(player, feet, IronSounds.RUMBLE, 1.2F, 0.6F);
        ServerLevel level = (ServerLevel) player.m_9236_();
        // 3.5.9 (the owner: "galaxy impact is too laggy"): every burst of lightning, rock and torn block cut down hard
        for (int t = 4; t < IronMove.IMPACT_JUMP; t += IronMove.IMPACT_CROUCH_EVERY) {
            int tt = t;
            later(player, t, () -> {
                ConquerorAura.bolts(player, chest(player), 2.2 + tt * 0.05, 2, -1.4, 1.0);
                Impact.quake(level, feet, 10.0, 0.15F + tt * 0.01F, 6);
            });
        }
        later(player, IronMove.IMPACT_JUMP, () -> {
            Craters.open(level, player, feet, 3.5);
            debris(player, feet, 9.0, 0.9, 18);
            Impact.quake(level, feet, 40.0, 0.7F, 12);
            sound(player, feet, IronSounds.SMASH, 1.6F, 0.6F);
            sound(player, feet, IronSounds.DASH, 1.4F, 0.6F);
        });
        for (int t = IronMove.IMPACT_TOP; t < IronMove.IMPACT_SILENCE; t += IronMove.IMPACT_CHARGE_EVERY) {
            int tt = t;
            later(player, t, () -> {
                double power = IronMove.impactCharge(tt);
                Vec3 fist = fist(player, 0.2).m_82520_(0, 0.5, 0);
                ConquerorAura.bolts(player, fist, 15.0 + 10.0 * power, IronMove.impactBolts(power), -IronMove.IMPACT_HEIGHT - 1.0, 1.4 + 1.2 * power);
                Vec3 below = ground(level, player.m_20182_(), 0.0, IronMove.IMPACT_HEIGHT + 20.0);
                for (LivingEntity body : ItoMoves.foes(player, below, 40.0)) {
                    body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, IronMove.IMPACT_CHARGE_EVERY + 4, 1 + (int) (2 * power), false, false, false));
                }
                if ((tt - IronMove.IMPACT_TOP) % (2 * IronMove.IMPACT_CHARGE_EVERY) == 0) {
                    Impact.quake(level, below, 45.0, 0.2F + 0.4F * (float) power, 8);
                }
            });
        }
        later(player, IronMove.IMPACT_PUNCH, () -> impactPunch(player, s, level));
        later(player, IronMove.IMPACT_TICKS, () -> {
            fly(player, s, false);
            s.doing = null;
        });
    }

    static void impactPunch(ServerPlayer player, State s, ServerLevel level) {
        Vec3 fist = fist(player, 0.6);
        Vec3 look = player.m_20154_();
        Vec3 down = look.f_82480_ > -0.35 ? new Vec3(look.f_82479_ * 0.45, -1.0, look.f_82481_ * 0.45).m_82541_() : look;
        BlockHitResult block = level.m_45547_(new ClipContext(fist, fist.m_82549_(down.m_82490_(90.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
            player));
        Vec3 struck = block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_() : ground(level, fist.m_82549_(down.m_82490_(IronMove.IMPACT_HEIGHT)), 10.0,
            60.0);
        double drop = fist.m_82554_(struck);
        fx(player, IMPACT, ++seeds, struck, down, 2, drop, IronMove.IMPACT_DOME, 0);
        StyleCombat.push(player, down.m_82490_(-0.25));
        double landing = IronMove.impactLanding(drop) - IronMove.IMPACT_PUNCH;
        later(player, (int) Math.round(landing), () -> {
            sound(player, struck, IronSounds.SMASH, 2.0F, 0.5F);
            ConquerorAura.bolts(player, struck.m_82520_(0, 2.0, 0), 12.0, 6, -1.0, 2.0);
        });
        later(player, (int) Math.round(landing) + 4, () -> impactBlast(player, level, struck));
    }

    static void impactBlast(ServerPlayer player, ServerLevel level, Vec3 at) {
        sound(player, at, IronSounds.BOOM, 3.0F, 0.5F);
        sound(player, at, IronSounds.RUMBLE, 3.0F, 0.45F);
        Impact.quake(level, at, 150.0, 1.0F, 40);
        // the crater: torn out as the dome passes, deepest in its middle, ragged
        double speed = IronMove.IMPACT_DOME / IronMove.IMPACT_DOME_TICKS;
        int cx = (int) Math.floor(at.f_82479_);
        int cy = (int) Math.floor(at.f_82480_ - 0.01);
        int cz = (int) Math.floor(at.f_82481_);
        List<int[]> cells = new ArrayList<>();
        int r = (int) IronMove.IMPACT_CRATER + 2;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                double d = Math.sqrt(x * x + z * z);
                double edge = IronMove.IMPACT_CRATER * (0.85 + 0.3 * Craters.noise(cx + x, 0, cz + z));
                if (d > edge) {
                    continue;
                }
                double depth = IronMove.IMPACT_DEPTH * (0.75 + 0.5 * Craters.noise(cx + x, 3, cz + z)) * Math.sqrt(Math.max(0.0, 1.0 - (d / edge) * (d / edge)));
                int order = (int) (d / speed);
                for (int y = -(int) Math.round(depth); y <= 6; y++) {
                    cells.add(new int[] {cx + x, cy + y, cz + z, order});
                }
            }
        }
        // the middle ring: whatever stands on the ground torn apart, the ground broken here and there; the edge: scattered
        for (int k = 0; k < IronMove.IMPACT_SCATTER; k++) {
            double a = 2 * Math.PI * FireMesh.rand(cx + k, 1);
            double d = IronMove.IMPACT_CRATER + (IronMove.IMPACT_DOME - IronMove.IMPACT_CRATER) * Math.sqrt(FireMesh.rand(cz + k, 2));
            int x = cx + (int) Math.round(Math.cos(a) * d);
            int z = cz + (int) Math.round(Math.sin(a) * d);
            double keep = d < 35.0 ? 1.0 : 0.35;
            if (FireMesh.rand(k, 9) > keep) {
                continue;
            }
            int order = (int) (d / speed);
            int top = d < 35.0 ? 6 : 3;
            int deep = 1;
            for (int y = -deep; y <= top; y++) {
                cells.add(new int[] {x, cy + y, z, order});
            }
        }
        dig(level, player, cells, 1.0, Craters.REPAIR + 600);
        Set<Integer> struck = new HashSet<>();
        for (int t = 0; t <= IronMove.IMPACT_DOME_TICKS; t += 4) {
            int tt = t;
            later(player, t, () -> {
                double radius = IronMove.IMPACT_DOME * Math.min(1.0, (tt + 4) / (double) IronMove.IMPACT_DOME_TICKS);
                for (LivingEntity body : ItoMoves.foes(player, at, radius + 2.0)) {
                    if (body == player || !struck.add(body.m_19879_())) {
                        continue;
                    }
                    Vec3 away = body.m_20182_().m_82546_(at);
                    double d = away.m_82553_();
                    double fall = Math.max(0.0, 1.0 - d / IronMove.IMPACT_DOME);
                    Vec3 out = (d < 1.0E-3 ? new Vec3(0, 1, 0) : away.m_82490_(1.0 / d)).m_82490_(1.5 + 2.5 * fall).m_82520_(0, 0.8 + 0.8 * fall, 0);
                    hit(player, body, (float) (10.0 + 50.0 * fall * fall), out, 60);
                    HakiFalls.knockDown(level, body, 40 + (int) (60 * fall), false);
                }
                if (tt % 12 == 0) {
                    double a = tt * 0.9;
                    Vec3 bolt = at.m_82520_(Math.cos(a) * radius * 0.6, 3.0 + radius * 0.3, Math.sin(a) * radius * 0.6);
                    ConquerorAura.bolts(player, bolt, 18.0 + radius * 0.2, 6, -(3.0 + radius * 0.3), 2.0);
                    debris(player, at, radius * 1.1, 1.0 + radius * 0.012, 10);
                }
            });
        }
    }

    // ------------------------------------------------------------------------------------------------------ the body in the air

    /** Lets the player hang in the air while a move carries them (so no server kicks a floating fighter), or gives it back. */
    static void fly(ServerPlayer player, State s, boolean on) {
        Abilities abilities = player.m_150110_();
        if (on) {
            if (!s.lifting) {
                s.mayfly = abilities.f_35936_;
                s.lifting = true;
            }
            abilities.f_35936_ = true;
            abilities.f_35935_ = false;
        } else {
            if (!s.lifting) {
                return;
            }
            s.lifting = false;
            boolean creative = player.m_7500_() || player.m_5833_();
            abilities.f_35936_ = creative || s.mayfly;
            if (!abilities.f_35936_) {
                abilities.f_35935_ = false;
            }
        }
        player.m_6885_();
    }

    static void safeFall(ServerPlayer player, State s, long until) {
        s.fallSafeUntil = Math.max(s.fallSafeUntil, until);
        player.f_19789_ = 0.0F;
    }

    // ------------------------------------------------------------------------------------------------------ the ground torn out

    static boolean griefing(ServerLevel level) {
        return level.m_46469_().m_46207_(net.minecraft.world.level.GameRules.f_46132_);
    }

    /**
     * Tears {@code cells} out ({x, y, z, order} each), cell by cell as their order comes ({@code perTick} orders a tick; 0: all at once),
     * within Scars' budgets, every one grown back {@code repair} ticks later (and a little more: the crater heals as a ripple).
     */
    static void dig(ServerLevel level, ServerPlayer owner, List<int[]> cells, double perTick, long repair) {
        if (cells.isEmpty() || !griefing(level)) {
            return;
        }
        cells.sort((a, b) -> Integer.compare(a[3], b[3]));
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (int[] c : cells) {
            minX = Math.min(minX, c[0]);
            minY = Math.min(minY, c[1]);
            minZ = Math.min(minZ, c[2]);
            maxX = Math.max(maxX, c[0]);
            maxY = Math.max(maxY, c[1]);
            maxZ = Math.max(maxZ, c[2]);
        }
        List<double[]> supports = new ArrayList<>();
        double m = Scars.HANGING_CLEARANCE;
        for (HangingEntity hanging : level.m_6443_(HangingEntity.class, new AABB(minX - m, minY - m, minZ - m, maxX + 1 + m, maxY + 1 + m, maxZ + 1 + m),
            e -> true)) {
            Vec3 p = hanging.m_20182_();
            supports.add(new double[] {p.f_82479_, p.f_82480_, p.f_82481_});
        }
        LIVE.add(new Dig(level, owner, cells, perTick, repair, supports, level.m_46467_()));
    }

    static final class Dig implements UoMoves.Live {
        final ServerLevel level;
        final ServerPlayer owner;
        final List<int[]> cells;
        final double perTick;
        final long repair;
        final List<double[]> supports;
        final long born;
        int next;

        Dig(ServerLevel level, ServerPlayer owner, List<int[]> cells, double perTick, long repair, List<double[]> supports, long born) {
            this.level = level;
            this.owner = owner;
            this.cells = cells;
            this.perTick = perTick;
            this.repair = repair;
            this.supports = supports;
            this.born = born;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            Scars scars = Scars.of(this.level);
            double reached = this.perTick <= 0.0 ? Double.MAX_VALUE : (now - this.born + 1) * this.perTick;
            while (this.next < this.cells.size() && this.cells.get(this.next)[3] <= reached && scars.room()) {
                int[] c = this.cells.get(this.next++);
                scars.tear(this.owner, new BlockPos(c[0], c[1], c[2]), this.supports, this.repair + (c[3] & 31));
            }
            return this.next >= this.cells.size() || now - this.born > 600;
        }
    }

    // ------------------------------------------------------------------------------------------------------ the tick

    static void tick(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        long now = now(player);
        if (s.meteorSince >= 0 && now - s.meteorSince >= IronMove.METEOR_MOST) {
            meteorThrow(player, s, now); // held its longest: it goes by itself
        }
        if (now < s.fallSafeUntil) {
            player.f_19789_ = 0.0F;
        }
        if (s.lifting && (s.doing == null && now >= s.busyUntil || !player.m_6084_())) {
            fly(player, s, false);
        }
    }

    /** Combat mode dropped, the style changed, death, a log out: whatever is held let go, the air given back. */
    public static void forget(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        if (s.meteorSince >= 0) {
            s.meteorSince = -1;
            fx(player, METEOR, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
        }
        fly(player, s, false);
        STATES.remove(player.m_20148_());
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player && STATES.containsKey(player.m_20148_())) {
            tick(player);
        }
    }

    @SubscribeEvent
    public static void ironTick(TickEvent.LevelTickEvent event) {
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

    @SubscribeEvent
    public static void stopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
        LIVE.clear();
        STATES.clear();
    }
}
