package com.hexhaki.piece.server;

import com.hexhaki.devilfruit.gameplay.GearFields;
import com.hexhaki.piece.gear.RubberWave;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.3.2 (the owner: "world of rubber makes it so for everyone else, it's main goal is to confuse the target, displace them and
 * overwhelm"): Rubber World (M) on everyone but its user. The original's field (GearFields) bounced every body on the ground in it every
 * tick, its user too; patch/PiecePatch empties that bounce (GearFields.bounceWithin) and hands the field and its user here as it is made
 * ({@link #add}, in place of releaseRubberWorld's GearFields.add, which it still calls: no fall damage in the field, the clients told of
 * it every second). Each tick, every other body standing in it is thrown by the rubber ground itself: where a wave's crest (gear/
 * RubberWave, the very waves every client draws) rises under its feet, it is flung up and sideways at random, a little outward or
 * back toward the middle (it tumbles about in the field rather than being swept out of it), its path lost
 * (a mob stops, spins round and forgets whom it was after) and a player's head swims (nausea). The user stands untouched (Gear 5's own
 * jumps are rubber enough: server/NikaJump). Cheap: one box query a field a tick, two wave heights a body, nothing when no field lasts.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class RubberChaos {
    /** Nausea on a thrown player (ticks), refreshed by every throw. */
    static final int DIZZY = 140;
    static final int MOST_FIELDS = 16;

    static final class Field {
        ServerLevel level;
        double x;
        double y;
        double z;
        double radius;
        long born;
        long end;
        UUID user;
    }

    static final List<Field> FIELDS = new ArrayList<>();
    /** By entity id: the tick a body may be thrown again. */
    static final Map<Integer, Long> NEXT = new HashMap<>();

    private RubberChaos() {
    }

    /** patch/PiecePatch, in place of GearServerController.releaseRubberWorld's GearFields.add(level, centre, radius, ticks). */
    public static void add(ServerLevel level, Vec3 centre, double radius, int ticks, ServerPlayer user) {
        GearFields.add(level, centre, radius, ticks);
        if (FIELDS.size() >= MOST_FIELDS) {
            FIELDS.remove(0);
        }
        Field f = new Field();
        f.level = level;
        f.x = centre.f_82479_;
        f.y = centre.f_82480_;
        f.z = centre.f_82481_;
        f.radius = radius;
        f.born = level.m_46467_();
        f.end = f.born + ticks;
        f.user = user.m_20148_();
        FIELDS.add(f);
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || FIELDS.isEmpty() || !(event.level instanceof ServerLevel level)) {
            return;
        }
        long now = level.m_46467_();
        for (int i = FIELDS.size() - 1; i >= 0; i--) {
            Field f = FIELDS.get(i);
            if (f.level == level && now > f.end) {
                FIELDS.remove(i);
            }
        }
        for (Field f : FIELDS) {
            if (f.level == level) {
                shake(level, f, now);
            }
        }
        if (FIELDS.isEmpty()) {
            NEXT.clear();
        }
    }

    static void shake(ServerLevel level, Field f, long now) {
        AABB box = new AABB(f.x - f.radius, f.y - 3, f.z - f.radius, f.x + f.radius, f.y + 6, f.z + f.radius);
        double t = now - f.born;
        double end = f.end - f.born;
        RandomSource random = level.m_213780_();
        for (LivingEntity body : level.m_6443_(LivingEntity.class, box, e -> e.m_6084_() && !e.m_5833_() && !e.m_20148_().equals(f.user))) {
            if (!body.m_20096_() || body.m_20159_() || body instanceof Player p && p.m_150110_().f_35935_) {
                continue;
            }
            double dx = body.m_20185_() - f.x;
            double dz = body.m_20189_() - f.z;
            double r = Math.sqrt(dx * dx + dz * dz);
            if (r > f.radius) {
                continue;
            }
            double angle = Math.atan2(dz, dx);
            double s = RubberWave.toss(RubberWave.rise(r, angle, t, f.radius, end) - RubberWave.rise(r, angle, t - 1, f.radius, end));
            if (s <= 0) {
                continue;
            }
            Long next = NEXT.get(body.m_19879_());
            if (next != null && now < next) {
                continue;
            }
            NEXT.put(body.m_19879_(), now + RubberWave.TOSS_GAP);
            throwBody(body, r > 1.0E-6 ? dx / r : 1.0, r > 1.0E-6 ? dz / r : 0.0, s, random);
        }
    }

    /** Flung by the rubber: up, out along (ox, oz), sideways at random; its path and aim lost. */
    static void throwBody(LivingEntity body, double ox, double oz, double s, RandomSource random) {
        double side = RubberWave.tossSide(s) * (random.m_188500_() < 0.5 ? -1 : 1) * (0.5 + random.m_188500_());
        double out = RubberWave.tossOut(s) * (random.m_188500_() < RubberWave.BACK_CHANCE ? -RubberWave.BACK : 1);
        Vec3 v = body.m_20184_();
        body.m_20334_(v.f_82479_ * 0.3 + ox * out - oz * side, RubberWave.tossUp(s), v.f_82481_ * 0.3 + oz * out + ox * side);
        body.f_19864_ = true;
        body.f_19789_ = 0.0F;
        if (body instanceof Mob mob) {
            mob.m_21573_().m_26573_();
            mob.m_6710_(null);
            float yaw = mob.m_146908_() + (float) ((random.m_188500_() < 0.5 ? -1 : 1) * (90 + 90 * random.m_188500_()));
            mob.m_146922_(yaw);
            mob.f_20883_ = yaw;
            mob.f_20885_ = yaw;
        } else if (body instanceof Player) {
            body.m_7292_(new MobEffectInstance(MobEffects.f_19604_, DIZZY, 0, false, false, true));
        }
    }
}
