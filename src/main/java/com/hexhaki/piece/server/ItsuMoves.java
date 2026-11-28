package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.FruitUtil;
import com.hexhaki.fx.Impact;
import com.hexhaki.piece.itsumade.ItsuForm;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 3.9.7: the Itsumade's moves in its form (the owner's spec: "M1- Wind Gust: a slow move, the user's wings flap to create massive wind
 * gusts that push away every entity backwards that is in the cone infront of the user. M2- The user grabs with his talons whatever entity
 * and is free to carry them endlessly. (only while flying) (Recasting M2, drops them) ... X - Bow : The user's massive transformed body
 * start emmiting conqueror's haki lightning stunning anything they hit for a period of 15 seconds (THe lightning never comes from the
 * inside of the transformation model). C - Enourmous bite : The user lunges it's massive neck forward biting where he looked at, causing
 * damage, bleed and broken bones (our effect from gura) V - Yokai Beam : Same as yokai blast but more concentrated, hotter, goes through
 * more blocks and explodes at the end into a big ball of fire."). The server's half: what each does to the world, when; every client
 * draws it (client/itsumade/ItsuFx, S2CUoFx kinds 442 to 449).
 */
public final class ItsuMoves {
    /** S2CUoFx kinds (client/itsumade/ItsuFx). */
    public static final int GUST = 442;
    public static final int BITE = 443;
    public static final int BOW = 444;
    public static final int BOLT = 445;
    public static final int CHARGE = 446;
    public static final int BEAM = 447;
    public static final int BEAM_OFF = 448;
    public static final int GRIP = 449;

    /** Wind Gust: the wings' flap lands this many ticks after M1 (its slow wind-up); the cone (degrees either side, blocks). */
    public static final int GUST_AT = 12;
    public static final int GUST_TICKS = 34;
    static final double GUST_HALF = 50.0;
    static final double GUST_RANGE = 24.0;
    static final int GUST_COOL = 50;
    /** Enormous Bite: the jaws shut this many ticks after C; how far ahead of the mouth, how wide, how hard. */
    public static final int BITE_AT = 9;
    public static final int BITE_TICKS = 26;
    static final double BITE_REACH = 3.5;
    static final double BITE_RADIUS = 4.5;
    static final float BITE_DAMAGE = 24.0F;
    static final int BITE_COOL = 100;
    /** Bow: how long it crackles (ticks), how often it strikes, how far, how long each hit is down. */
    public static final int BOW_TICKS = 300;
    static final int BOW_EVERY = 5;
    static final double BOW_RANGE = 16.0;
    static final int BOW_STUN = 40;
    static final float BOW_DAMAGE = 3.0F;
    static final int BOW_COOL = 600;
    /** Yokai Beam: the charge (ticks for full, the least), and the beam's reach, width and life by the charge. */
    static final int BEAM_FULL = 80;
    static final int BEAM_LEAST = 8;
    static final int BEAM_COOL = 240;

    static final class State {
        long gustAt = -1;
        long biteAt = -1;
        long bowUntil = -1;
        long chargeFrom = -1;
        Entity carried;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private ItsuMoves() {
    }

    static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.m_20148_(), id -> new State());
    }

    static boolean form(ServerPlayer player) {
        return ItsuServer.phase(player) == ItsuForm.ITSU;
    }

    static boolean ready(ServerPlayer player, FruitMove move) {
        return UoMoves.ready(player, move);
    }

    static void cool(ServerPlayer player, FruitMove move, int ticks) {
        UoMoves.cool(player, move, ticks);
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    public static void press(ServerPlayer player, int slot) {
        if (!form(player)) {
            return;
        }
        long now = player.m_9236_().m_46467_();
        if (slot == ItsuKit.BOW.slot()) {
            bow(player, now);
        } else if (slot == ItsuKit.BITE.slot()) {
            bite(player, now);
        } else if (slot == ItsuKit.YOKAI_BEAM.slot()) {
            State s = state(player);
            if (s.chargeFrom < 0 && ready(player, ItsuKit.YOKAI_BEAM)) {
                s.chargeFrom = now;
                ItoMoves.fx(player, CHARGE, 1, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
                ItoMoves.sound(player, player.m_20182_(), com.hexhaki.piece.dragon.UoSounds.CHARGE_BUILD, 2.4F, 0.6F);
            }
        }
    }

    public static void release(ServerPlayer player, int slot) {
        if (slot != ItsuKit.YOKAI_BEAM.slot()) {
            return;
        }
        State s = state(player);
        if (s.chargeFrom < 0) {
            return;
        }
        long held = player.m_9236_().m_46467_() - s.chargeFrom;
        s.chargeFrom = -1;
        ItoMoves.fx(player, CHARGE, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
        if (held < BEAM_LEAST || !form(player)) {
            return;
        }
        float charge = (float) Math.min(1.0, held / (double) BEAM_FULL);
        cool(player, ItsuKit.YOKAI_BEAM, BEAM_COOL);
        yokaiBeam(player, charge);
    }

    /** M1 in its form: the Wind Gust. */
    public static void attack(ServerPlayer player) {
        if (!form(player)) {
            return;
        }
        State s = state(player);
        long now = player.m_9236_().m_46467_();
        if (s.gustAt >= 0 || !ready(player, ItsuKit.WIND_GUST)) {
            return;
        }
        cool(player, ItsuKit.WIND_GUST, GUST_COOL);
        s.gustAt = now + GUST_AT;
        ItoMoves.fx(player, GUST, 0, player.m_20182_(), player.m_20154_(), 0, 0, 0, 0);
    }

    /** M2 in its form, flying: the talons close on what is under them; again, they open. */
    public static void use(ServerPlayer player) {
        if (!form(player)) {
            return;
        }
        State s = state(player);
        if (s.carried != null) {
            drop(player, s);
            return;
        }
        if (player.m_20096_()) {
            return; // only while flying
        }
        Vec3 talons = player.m_20182_().m_82520_(0.0, 0.5, 0.0);
        LivingEntity best = null;
        double bestD = Double.MAX_VALUE;
        for (LivingEntity body : player.m_9236_().m_6443_(LivingEntity.class, UoMoves.box(talons, talons).m_82400_(6.0),
            e -> e != player && e.m_6084_() && !e.m_5833_() && !e.m_20159_())) {
            double d = body.m_20182_().m_82554_(talons);
            if (d < bestD) {
                bestD = d;
                best = body;
            }
        }
        if (best != null) {
            s.carried = best;
            ItoMoves.fx(player, GRIP, best.m_19879_(), player.m_20182_(), Vec3.f_82478_, 1, 0, 0, 0);
            ItoMoves.sound(player, talons, com.hexhaki.piece.dragon.UoSounds.BITE, 1.6F, 0.6F);
            ConquerorAura.mixed(player, best.m_20182_().m_82520_(0.0, best.m_20206_() * 0.5, 0.0), 2.0, 4, -1.0); // 4.0.0: POINT ONE
        }
    }

    static void drop(ServerPlayer player, State s) {
        if (s.carried != null) {
            s.carried.f_19789_ = 0.0F;
            ItoMoves.fx(player, GRIP, s.carried.m_19879_(), player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
        }
        s.carried = null;
    }

    // ------------------------------------------------------------------------------------------------------ the moves

    /** X: Bow. */
    static void bow(ServerPlayer player, long now) {
        State s = state(player);
        if (s.bowUntil > now || !ready(player, ItsuKit.BOW)) {
            return;
        }
        cool(player, ItsuKit.BOW, BOW_COOL);
        s.bowUntil = now + BOW_TICKS;
        ItoMoves.fx(player, BOW, 1, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, BOW_TICKS);
        ItoMoves.sound(player, player.m_20182_(), com.hexhaki.piece.dragon.UoSounds.ROAR, 3.0F, 0.55F);
    }

    /** C: Enormous Bite. */
    static void bite(ServerPlayer player, long now) {
        State s = state(player);
        if (s.biteAt >= 0 || !ready(player, ItsuKit.BITE)) {
            return;
        }
        cool(player, ItsuKit.BITE, BITE_COOL);
        s.biteAt = now + BITE_AT;
        ItoMoves.fx(player, BITE, 0, player.m_20182_(), player.m_20154_(), 0, 0, 0, BITE_TICKS);
        ItoMoves.sound(player, mouth(player), com.hexhaki.piece.dragon.UoSounds.LUNGE, 2.4F, 0.7F);
    }

    /** V let go: the Yokai Beam out of the jaws (the beam the dragon's, longer, hotter, its end a great ball of fire). */
    static void yokaiBeam(ServerPlayer player, float charge) {
        double width = 1.6 + 2.4 * charge;
        double length = 40.0 + 60.0 * charge;
        int life = 30 + (int) (70 * charge);
        UoMoves.beamFrom(player, true, width, length, life, 6.0F + 8.0F * charge, 1.4 + 2.0 * charge, charge, ItsuMoves::mouth,
            ItsuMoves::form);
        player.m_20334_(0.0, 0.0, 0.0);
        player.f_19864_ = true;
        ItoMoves.fx(player, BEAM, 0, mouth(player), player.m_20154_(), width, length, charge, life);
        ItoMoves.sound(player, mouth(player), com.hexhaki.piece.dragon.UoSounds.ROAR, 3.0F, 0.6F);
        ItoMoves.sound(player, mouth(player), com.hexhaki.piece.dragon.UoSounds.BEAM, 3.0F + charge, 0.7F);
    }

    /**
     * Where its mouth is, near enough for the server (the client draws from the real one): the Itsumade stands over the player's feet,
     * its head well up and ahead along the look.
     */
    static Vec3 mouth(ServerPlayer player) {
        Vec3 look = player.m_20154_();
        Vec3 flat = new Vec3(look.f_82479_, 0.0, look.f_82481_);
        flat = flat.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.m_82541_();
        double up = player.m_20096_() ? 6.2 : 5.0;
        return player.m_20182_().m_82549_(flat.m_82490_(5.0)).m_82520_(0.0, up + look.f_82480_ * 2.0, 0.0);
    }

    // ------------------------------------------------------------------------------------------------------ each tick

    public static void tick(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        if (!form(player)) {
            forget(player);
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        long now = level.m_46467_();
        if (s.gustAt >= 0 && now >= s.gustAt) {
            s.gustAt = -1;
            gust(player, level);
        }
        if (s.biteAt >= 0 && now >= s.biteAt) {
            s.biteAt = -1;
            shut(player, level);
        }
        if (s.bowUntil > now && now % BOW_EVERY == 0) {
            strike(player, level, now);
        }
        if (s.carried != null) {
            Entity body = s.carried;
            if (!body.m_6084_() || body.m_9236_() != level || player.m_20096_()) {
                drop(player, s);
            } else {
                Vec3 at = player.m_20182_().m_82520_(0.0, -Math.max(1.0, body.m_20206_() * 0.6), 0.0);
                body.m_6021_(at.f_82479_, at.f_82480_, at.f_82481_);
                body.m_20334_(0.0, 0.0, 0.0);
                body.f_19789_ = 0.0F;
                body.f_19864_ = true;
            }
        }
    }

    /** The gust lands: everything in the cone ahead is thrown back away from it, harder the nearer. */
    static void gust(ServerPlayer player, ServerLevel level) {
        Vec3 from = player.m_20182_().m_82520_(0.0, 4.0, 0.0);
        Vec3 look = player.m_20154_();
        double cos = Math.cos(Math.toRadians(GUST_HALF));
        for (LivingEntity body : level.m_6443_(LivingEntity.class, UoMoves.box(from, from).m_82400_(GUST_RANGE),
            e -> e != player && e.m_6084_() && !e.m_5833_() && FruitUtil.foe(player, e))) {
            Vec3 rel = body.m_20182_().m_82520_(0.0, body.m_20206_() * 0.5, 0.0).m_82546_(from);
            double d = rel.m_82553_();
            if (d > GUST_RANGE || d < 1.0E-3 || rel.m_82541_().m_82526_(look) < cos) {
                continue;
            }
            double k = 1.0 - d / GUST_RANGE;
            Vec3 away = new Vec3(rel.f_82479_, 0.0, rel.f_82481_);
            away = away.m_82556_() < 1.0E-6 ? new Vec3(look.f_82479_, 0.0, look.f_82481_) : away.m_82541_();
            Vec3 thrown = away.m_82490_(1.2 + 3.2 * k).m_82520_(0.0, 0.4 + 0.5 * k, 0.0);
            Impact.hit(player, body, StyleCombat.spec(com.hexhaki.piece.style.Hit.Weight.HEAVY, thrown));
        }
        // 4.0.0 (POINT ONE: with Ryou, every move throws the Conqueror's lightning): out over the cone where the gust lands
        ConquerorAura.mixed(player, from.m_82549_(look.m_82490_(8.0)), 4.0, 8, -1.0);
        ItoMoves.sound(player, from, com.hexhaki.piece.dragon.UoSounds.GUST, 3.2F, 0.55F);
        ItoMoves.sound(player, from, com.hexhaki.piece.dragon.UoSounds.WHOOSH, 3.0F, 0.6F);
    }

    /** The jaws shut where it looked: hurt, bleeding, bones broken. */
    static void shut(ServerPlayer player, ServerLevel level) {
        Vec3 jaws = mouth(player).m_82549_(player.m_20154_().m_82490_(BITE_REACH));
        DamageSource source = StyleCombat.of(player);
        for (LivingEntity body : level.m_6443_(LivingEntity.class, UoMoves.box(jaws, jaws).m_82400_(BITE_RADIUS + 2.0),
            e -> e != player && e.m_6084_() && !e.m_5833_() && FruitUtil.foe(player, e))) {
            if (body.m_20191_().m_82400_(BITE_RADIUS).m_82390_(jaws)) {
                body.f_19802_ = 0;
                if (body.m_6469_(source, BITE_DAMAGE)) {
                    com.hexhaki.gore.Gore.bleed(player, body, 4, 160);
                    BrokenBones.struck(level, body, 60);
                }
            }
        }
        ConquerorAura.mixed(player, jaws, 3.0, 6, -1.0); // 4.0.0: POINT ONE
        ItoMoves.sound(player, jaws, com.hexhaki.piece.dragon.UoSounds.BITE, 2.8F, 0.55F);
    }

    /** Bow: a bolt of the Conqueror's lightning off its body onto what is near (stunned), or into the ground about it. */
    static void strike(ServerPlayer player, ServerLevel level, long now) {
        Vec3 centre = player.m_20182_().m_82520_(0.0, 4.0, 0.0);
        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity body : level.m_6443_(LivingEntity.class, UoMoves.box(centre, centre).m_82400_(BOW_RANGE),
            e -> e != player && e.m_6084_() && !e.m_5833_() && FruitUtil.foe(player, e))) {
            double d = body.m_20182_().m_82554_(centre) + player.m_217043_().m_188500_() * 6.0;
            if (d < best) {
                best = d;
                target = body;
            }
        }
        Vec3 to;
        if (target != null) {
            to = target.m_20182_().m_82520_(0.0, target.m_20206_() * 0.6, 0.0);
            target.f_19802_ = 0;
            target.m_6469_(StyleCombat.of(player), BOW_DAMAGE);
            HakiFalls.knockDown(level, target, BOW_STUN, false);
        } else {
            double a = player.m_217043_().m_188500_() * Math.PI * 2.0;
            double r = 5.0 + player.m_217043_().m_188500_() * 7.0;
            to = UoMoves.ground(level, centre.m_82520_(Math.cos(a) * r, 0.0, Math.sin(a) * r));
        }
        ItoMoves.fx(player, BOLT, (int) (now & 0x7FFFFFFF), to, Vec3.f_82478_, 0, 0, 0, 0);
    }

    public static void forget(ServerPlayer player) {
        State s = STATES.remove(player.m_20148_());
        if (s != null) {
            drop(player, s);
            if (s.chargeFrom >= 0) {
                ItoMoves.fx(player, CHARGE, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
            }
            if (s.bowUntil > player.m_9236_().m_46467_()) {
                ItoMoves.fx(player, BOW, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
            }
        }
    }
}
