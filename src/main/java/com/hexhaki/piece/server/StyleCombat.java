package com.hexhaki.piece.server;

import com.hexhaki.fx.Impact;
import com.hexhaki.fruit.Fruits;
import com.hexhaki.piece.PieceRules;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CStyleFx;
import com.hexhaki.piece.style.Hit;
import com.hexhaki.piece.style.Motion;
import com.hexhaki.piece.style.StyleMove;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 2.1.1: how a fighting-style move acts in the world, for a player or an NPC alike (the server only): who its blows find, how hard they
 * land, how the user is carried. Blows go through the original's own impact system ({@code Impact.hit}: hit-stop, shake, flash, the
 * launch) and sounds ({@code Fruits.sound}: its whooshes and impacts), so a style hit feels like the mod's other hits; Armament Haki's
 * damage applies as to any blow (the damage is a player attack, or the NPC's mob attack).
 */
public final class StyleCombat {
    private StyleCombat() {
    }

    /** The user's facing, flat (yaw only). */
    public static Vec3 facing(LivingEntity user) {
        double yaw = Math.toRadians(user.m_146908_());
        return new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
    }

    static Vec3 middle(LivingEntity entity) {
        return entity.m_20182_().m_82520_(0.0, entity.m_20206_() * 0.5, 0.0);
    }

    /** Living bodies (not the user) within {@code reach} of the user's middle and within {@code arc} degrees either side of its facing. */
    public static List<LivingEntity> arc(LivingEntity user, double reach, double arc, Predicate<LivingEntity> foe) {
        Vec3 from = middle(user);
        Vec3 look = facing(user);
        double cos = Math.cos(Math.toRadians(Math.min(180.0, arc)));
        List<LivingEntity> found = new ArrayList<>();
        AABB box = user.m_20191_().m_82400_(reach + 1.0);
        for (LivingEntity other : user.m_9236_().m_6443_(LivingEntity.class, box, e -> e != user && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
            Vec3 to = middle(other).m_82546_(from);
            double flat = Math.sqrt(to.f_82479_ * to.f_82479_ + to.f_82481_ * to.f_82481_);
            if (flat - other.m_20205_() * 0.5 > reach || Math.abs(to.f_82480_) > reach * 0.75 + 1.0) {
                continue;
            }
            if (arc < 180.0 && flat > 0.6 && (to.f_82479_ * look.f_82479_ + to.f_82481_ * look.f_82481_) / flat < cos) {
                continue;
            }
            found.add(other);
        }
        return found;
    }

    /** Living bodies along a shot from the user's eyes along its look, {@code range} long and {@code radius} thick; and where it ends. */
    public static List<LivingEntity> line(LivingEntity user, double range, double radius, Predicate<LivingEntity> foe, Vec3[] end) {
        Vec3 from = user.m_146892_();
        Vec3 look = user.m_20154_();
        Vec3 to = from.m_82549_(look.m_82490_(range));
        end[0] = to;
        List<LivingEntity> found = new ArrayList<>();
        AABB box = new AABB(Math.min(from.f_82479_, to.f_82479_), Math.min(from.f_82480_, to.f_82480_), Math.min(from.f_82481_, to.f_82481_),
            Math.max(from.f_82479_, to.f_82479_), Math.max(from.f_82480_, to.f_82480_), Math.max(from.f_82481_, to.f_82481_)).m_82400_(radius + 1.0);
        for (LivingEntity other : user.m_9236_().m_6443_(LivingEntity.class, box, e -> e != user && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
            Vec3 c = middle(other);
            Vec3 rel = c.m_82546_(from);
            double along = Math.max(0.0, Math.min(range, rel.f_82479_ * look.f_82479_ + rel.f_82480_ * look.f_82480_ + rel.f_82481_ * look.f_82481_));
            Vec3 closest = from.m_82549_(look.m_82490_(along));
            if (closest.m_82554_(c) <= radius + other.m_20205_() * 0.5 + 0.3) {
                found.add(other);
            }
        }
        return found;
    }

    /** The user carried by its move this tick (players are told; NPCs move on their own next tick). */
    public static void carry(LivingEntity user, StyleMove move, int tick) {
        carry(user, move.motion(), tick);
    }

    /** 2.1.4: the same for any motion (the Swordsman's cuts). */
    public static void carry(LivingEntity user, Motion motion, int tick) {
        Vec3 look = facing(user);
        Vec3 now = user.m_20184_();
        switch (motion.kind()) {
            case LUNGE -> {
                if (tick == motion.tick()) {
                    push(user, new Vec3(now.f_82479_ * 0.2 + look.f_82479_ * motion.forward(), Math.max(now.f_82480_, motion.up()),
                        now.f_82481_ * 0.2 + look.f_82481_ * motion.forward()));
                }
            }
            case DASH -> {
                if (tick >= motion.tick() && tick <= motion.until()) {
                    push(user, new Vec3(look.f_82479_ * motion.forward(), Math.min(now.f_82480_, 0.05), look.f_82481_ * motion.forward()));
                } else if (tick == motion.until() + 1) {
                    push(user, new Vec3(look.f_82479_ * 0.15, now.f_82480_, look.f_82481_ * 0.15));
                }
            }
            case LEAP -> {
                if (tick == motion.tick()) {
                    push(user, new Vec3(look.f_82479_ * motion.forward(), motion.up(), look.f_82481_ * motion.forward()));
                } else if (tick == motion.until()) {
                    push(user, new Vec3(look.f_82479_ * 0.25, -1.7, look.f_82481_ * 0.25));
                }
            }
            default -> {
            }
        }
        if (motion.kind() != Motion.Kind.NONE) {
            user.f_19789_ = 0.0F;
        }
    }

    static void push(LivingEntity user, Vec3 velocity) {
        user.m_20256_(velocity);
        user.f_19864_ = true;
    }

    /**
     * One blow of {@code move} lands: everything it finds is hurt (a player's damage scaled by its style mastery), stunned, and thrown
     * through the original's impact; {@code struck} (the bodies this move hit already) keeps a {@link StyleMove.Effect#ONCE} move to
     * one hit each. Returns how many it hit.
     */
    public static int land(LivingEntity user, StyleMove move, Hit hit, int mastery, Predicate<LivingEntity> foe, Set<Integer> struck,
                           DamageSource source) {
        if (!(user.m_9236_() instanceof ServerLevel level)) {
            return 0;
        }
        Vec3[] end = new Vec3[1];
        if (!hit.line()) {
            // 2.1.2: every blow thrown cuts the air on every client near (StyleFx), hit or miss: its weight, + 10 all round.
            Vec3 look = facing(user);
            int code = hit.weight().ordinal() + (hit.arc() >= 180.0 ? 10 : 0);
            PieceNetwork.around(user, new S2CStyleFx(move.ordinal(), 3, user.m_19879_(), user.m_20185_(), user.m_20186_(), user.m_20189_(),
                look.f_82479_, code, look.f_82481_));
        }
        List<LivingEntity> targets = hit.line() ? line(user, hit.reach(), hit.arc(), foe, end) : arc(user, hit.reach(), hit.arc(), foe);
        if (hit.line()) {
            Vec3 from = user.m_146892_();
            PieceNetwork.around(user, new S2CStyleFx(move.ordinal(), 2, user.m_19879_(), end[0].f_82479_, end[0].f_82480_, end[0].f_82481_,
                from.f_82479_, from.f_82480_, from.f_82481_));
        }
        float damage = hit.damage() * PieceRules.masteryDamage(mastery);
        Vec3 look = facing(user);
        int count = 0;
        for (LivingEntity target : targets) {
            if (move.effect() == StyleMove.Effect.ONCE && !struck.add(target.m_19879_())) {
                continue;
            }
            target.f_19802_ = 0;
            if (!target.m_6469_(source, damage)) {
                continue;
            }
            count++;
            Vec3 launch = new Vec3(look.f_82479_ * hit.back(), hit.up(), look.f_82481_ * hit.back());
            Impact.hit(user, target, spec(hit, launch));
            if (hit.stun() > 0) {
                target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, hit.stun(), 3, false, false, false));
            }
            if (move.effect() == StyleMove.Effect.FIRE) {
                target.m_20254_(4);
            }
            Vec3 at = middle(target);
            PieceNetwork.around(target, new S2CStyleFx(move.ordinal(), 1, user.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_,
                look.f_82479_, 0.0, look.f_82481_));
        }
        if (move.effect() == StyleMove.Effect.QUAKE && hit.weight().ordinal() >= Hit.Weight.HEAVY.ordinal()) {
            Vec3 ground = user.m_20182_().m_82549_(look.m_82490_(Math.min(2.5, hit.reach() * 0.5)));
            Impact.quake(level, ground, Math.min(9.0, hit.reach() + 2.0), hit.weight() == Hit.Weight.FINISHER ? 1.0F : 0.7F, 10);
        }
        sound(user, count > 0 ? (hit.weight().ordinal() >= Hit.Weight.HEAVY.ordinal() ? "fx.impact_heavy" : "fx.impact_light")
            : (hit.weight().ordinal() >= Hit.Weight.HEAVY.ordinal() ? "fx.whoosh_heavy" : "fx.whoosh_light"), count > 0 ? 1.0F : 0.7F);
        return count;
    }

    static Impact.Spec spec(Hit hit, Vec3 launch) {
        return spec(hit.weight(), launch);
    }

    /** 2.1.4: the original's impact for a weight of blow. */
    public static Impact.Spec spec(Hit.Weight weight, Vec3 launch) {
        return switch (weight) {
            case LIGHT -> Impact.Spec.light(launch);
            case MEDIUM -> Impact.Spec.medium(launch);
            case HEAVY -> Impact.Spec.heavy(launch);
            case FINISHER -> Impact.Spec.finisher(launch);
        };
    }

    /** One of the original's combat sounds (its fx pack) at the user. */
    public static void sound(LivingEntity user, String name, float volume) {
        SoundEvent event = Fruits.sound(name);
        if (event != null) {
            user.m_9236_().m_6263_(null, user.m_20185_(), user.m_20186_(), user.m_20189_(), event, SoundSource.PLAYERS, volume,
                0.92F + user.m_217043_().m_188501_() * 0.16F);
        }
    }

    /** A player's damage source. */
    public static DamageSource of(ServerPlayer player) {
        return player.m_269291_().m_269075_(player);
    }
}
