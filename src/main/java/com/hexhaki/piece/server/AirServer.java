package com.hexhaki.piece.server;

import com.hexhaki.fx.Impact;
import com.hexhaki.piece.PieceRules;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CWater;
import com.hexhaki.piece.style.Hit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.4.1: the Six Powers finger pistol with Armament Haki shoots (the owner: "(finger pistol with haki shoots projectiles)"): as the
 * finger is driven in, a bullet of Haki-hardened air leaves it along the look ({@link #shoot}), {@link #SPEED} blocks a tick for
 * {@link #RANGE} blocks; it strikes the first foe along its path (hurt, a light throw) or the first block, and is gone. Every client
 * near sees it fly and strike ({@link S2CWater#AIR_SHOT}, {@link S2CWater#AIR_HIT}; {@code client/fx/AirFx}).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class AirServer {
    public static final double SPEED = 4.0;
    public static final double RANGE = 18.0;
    public static final float DAMAGE = 4.0F;
    /** How close (blocks, past the body) it passes to strike. */
    static final double STRIKES = 0.3;

    static final class Bullet {
        final LivingEntity user;
        final ServerLevel level;
        final int id;
        final float damage;
        final Predicate<LivingEntity> foe;
        final DamageSource source;
        final Vec3 velocity;
        Vec3 at;
        double flown;

        Bullet(LivingEntity user, ServerLevel level, int id, float damage, Predicate<LivingEntity> foe, DamageSource source, Vec3 at,
               Vec3 velocity) {
            this.user = user;
            this.level = level;
            this.id = id;
            this.damage = damage;
            this.foe = foe;
            this.source = source;
            this.at = at;
            this.velocity = velocity;
        }
    }

    private static final List<Bullet> FLYING = new ArrayList<>();
    private static int next;

    private AirServer() {
    }

    /** {@code user} shoots a bullet of hardened air from its finger along its look ({@code mastery} scales its damage). */
    public static void shoot(LivingEntity user, int mastery, Predicate<LivingEntity> foe, DamageSource source) {
        if (!(user.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Vec3 look = user.m_20154_();
        Vec3 from = user.m_146892_().m_82549_(look.m_82490_(0.7)).m_82520_(0.0, -0.2, 0.0);
        Vec3 velocity = look.m_82490_(SPEED);
        Bullet bullet = new Bullet(user, level, ++next, DAMAGE * PieceRules.masteryDamage(mastery), foe, source, from, velocity);
        FLYING.add(bullet);
        PieceNetwork.around(user, new S2CWater(S2CWater.AIR_SHOT, bullet.id, user.m_19879_(), 0, -1, from.f_82479_, from.f_82480_, from.f_82481_,
            (float) velocity.f_82479_, (float) velocity.f_82480_, (float) velocity.f_82481_));
        StyleCombat.sound(user, "fx.whoosh_light", 0.9F);
    }

    @SubscribeEvent
    public static void airServerTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || FLYING.isEmpty()) {
            return;
        }
        for (Iterator<Bullet> it = FLYING.iterator(); it.hasNext(); ) {
            Bullet bullet = it.next();
            if (bullet.level != level) {
                continue;
            }
            try {
                if (fly(bullet)) {
                    it.remove();
                }
            } catch (RuntimeException failure) {
                it.remove();
            }
        }
    }

    /** One tick of a bullet's flight; true when it is gone. */
    static boolean fly(Bullet bullet) {
        Vec3 from = bullet.at;
        Vec3 to = from.m_82549_(bullet.velocity);
        LivingEntity struck = null;
        double first = Double.MAX_VALUE;
        AABB around = new AABB(Math.min(from.f_82479_, to.f_82479_), Math.min(from.f_82480_, to.f_82480_), Math.min(from.f_82481_, to.f_82481_),
            Math.max(from.f_82479_, to.f_82479_), Math.max(from.f_82480_, to.f_82480_), Math.max(from.f_82481_, to.f_82481_)).m_82400_(STRIKES + 1.0);
        for (LivingEntity other : bullet.level.m_6443_(LivingEntity.class, around,
            e -> e != bullet.user && e.m_6084_() && !e.m_5833_() && bullet.foe.test(e))) {
            double along = WaterServer.closest(from, to, StyleCombat.middle(other));
            Vec3 point = from.m_82549_(to.m_82546_(from).m_82490_(along));
            if (other.m_20191_().m_82400_(STRIKES).m_82390_(point) && along < first) {
                first = along;
                struck = other;
            }
        }
        BlockHitResult wall = bullet.level.m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bullet.user));
        double wallAt = wall != null && wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_().m_82554_(from) / Math.max(1.0E-6, to.m_82554_(from))
            : Double.MAX_VALUE;
        Vec3 dir = bullet.velocity.m_82541_();
        if (struck != null && first <= wallAt) {
            Vec3 at = from.m_82549_(to.m_82546_(from).m_82490_(first));
            struck.f_19802_ = 0;
            if (struck.m_6469_(bullet.source, bullet.damage)) {
                Impact.hit(bullet.user, struck, StyleCombat.spec(Hit.Weight.LIGHT, new Vec3(dir.f_82479_ * 0.3, 0.06, dir.f_82481_ * 0.3)));
            }
            hit(bullet, at, dir);
            return true;
        }
        if (wallAt <= 1.0) {
            hit(bullet, wall.m_82450_(), dir);
            return true;
        }
        bullet.at = to;
        bullet.flown += SPEED;
        if (bullet.flown >= RANGE) {
            hit(bullet, to, dir);
            return true;
        }
        return false;
    }

    static void hit(Bullet bullet, Vec3 at, Vec3 dir) {
        PieceNetwork.around(bullet.user, new S2CWater(S2CWater.AIR_HIT, bullet.id, bullet.user.m_19879_(), 0, -1, at.f_82479_, at.f_82480_,
            at.f_82481_, (float) dir.f_82479_, (float) dir.f_82480_, (float) dir.f_82481_));
        StyleCombat.sound(bullet.user, "fx.impact_light", 0.8F);
    }
}
