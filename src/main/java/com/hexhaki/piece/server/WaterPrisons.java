package com.hexhaki.piece.server;

import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CWater;
import com.hexhaki.piece.water.WaterPrison;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.4.0: Fishman Karate's Water Prison in the world ({@link WaterPrison}; {@code client/fx/WaterFx} draws it). Thrown only at a body in
 * the user's sights ({@link #sights}; a sea fighter's at its foe): from {@link WaterPrison#GATHER} every client near sees the water
 * gather round it ({@link S2CWater#PRISON_GATHER}); on {@link WaterPrison#SEAL} it is held ({@link S2CWater#PRISON_SEAL}): lifted
 * {@link WaterPrison#LIFT} off the ground and kept there (floating, its legs slowed to nothing, put back if it drifts), throwing nothing
 * and striking nothing, free only to look around, for {@link WaterPrison#HOLD} ticks; then (or once {@link WaterPrison#BREAKS} damage
 * has been dealt to it, or it lets out its Conqueror's Haki: {@link #conqueror}) the bubble bursts ({@link S2CWater#PRISON_BURST}) and
 * it drops.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class WaterPrisons {
    /** A body held: its world, who holds it, where, until when, the damage dealt to it inside. */
    static final class Prison {
        final ServerLevel level;
        final LivingEntity user;
        final LivingEntity body;
        final Vec3 hold;
        final long until;
        final boolean floated;
        float taken;

        Prison(ServerLevel level, LivingEntity user, LivingEntity body, Vec3 hold, long until, boolean floated) {
            this.level = level;
            this.user = user;
            this.body = body;
            this.hold = hold;
            this.until = until;
            this.floated = floated;
        }
    }

    /** The bodies held, by entity id; the target each user's Water Prison goes for while its move plays, by the user's id. */
    private static final Map<Integer, Prison> HELD = new HashMap<>();
    private static final Map<Integer, LivingEntity> AIMED = new HashMap<>();
    /** A held body that drifts this far (blocks) from where it is held is put back. */
    static final double DRIFT = 0.12;

    private WaterPrisons() {
    }

    /** The foe in {@code user}'s sights (the nearest its look passes through, within reach), or null. */
    public static LivingEntity sights(LivingEntity user, Predicate<LivingEntity> foe) {
        Vec3 eye = user.m_146892_();
        Vec3 look = user.m_20154_();
        double[] e = {eye.f_82479_, eye.f_82480_, eye.f_82481_};
        double[] d = {look.f_82479_, look.f_82480_, look.f_82481_};
        LivingEntity best = null;
        double nearest = Double.MAX_VALUE;
        for (LivingEntity other : user.m_9236_().m_6443_(LivingEntity.class, user.m_20191_().m_82400_(WaterPrison.RANGE),
            body -> body != user && body.m_6084_() && !body.m_5833_() && foe.test(body))) {
            AABB box = other.m_20191_();
            double at = WaterPrison.sights(e, d, new double[] {box.f_82288_, box.f_82289_, box.f_82290_},
                new double[] {box.f_82291_, box.f_82292_, box.f_82293_});
            if (at >= 0 && at < nearest) {
                nearest = at;
                best = other;
            }
        }
        return best;
    }

    /** {@code user}'s Water Prison (begun now) goes for {@code target}. */
    public static void aim(LivingEntity user, LivingEntity target) {
        AIMED.put(user.m_19879_(), target);
    }

    /** One tick of {@code user}'s Water Prison move, {@code tick} ticks in: the water gathers round its target, then it is sealed in. */
    public static void tick(LivingEntity user, int tick) {
        LivingEntity target = AIMED.get(user.m_19879_());
        if (target == null || !(user.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        if (!target.m_6084_() || target.m_9236_() != user.m_9236_() || target.m_20280_(user) > (WaterPrison.RANGE + 4) * (WaterPrison.RANGE + 4)) {
            AIMED.remove(user.m_19879_());
            return;
        }
        if (tick == WaterPrison.GATHER) {
            PieceNetwork.around(target, new S2CWater(S2CWater.PRISON_GATHER, 0, user.m_19879_(), 0, target.m_19879_(), target.m_20185_(),
                target.m_20186_(), target.m_20189_(), 0, 0, 0));
            WaterServer.vanilla(level, target.m_20182_(), "block.bubble_column.upwards_inside", 0.9F, 0.8F);
        }
        if (tick == WaterPrison.SEAL) {
            AIMED.remove(user.m_19879_());
            seal(level, user, target);
        }
    }

    /** The move ended or was cut short: its target is let go of (one sealed in stays held). */
    public static void stop(LivingEntity user) {
        AIMED.remove(user.m_19879_());
    }

    static void seal(ServerLevel level, LivingEntity user, LivingEntity body) {
        Prison old = HELD.remove(body.m_19879_());
        if (old != null) {
            release(old, false);
        }
        Vec3 hold = body.m_20182_().m_82520_(0.0, WaterPrison.LIFT, 0.0);
        boolean floated = true; // a body that floats on its own is not one a fighter can catch: gravity is given back on release
        body.m_20242_(true);
        body.m_6021_(hold.f_82479_, hold.f_82480_, hold.f_82481_);
        body.m_20256_(Vec3.f_82478_);
        long now = level.m_46467_();
        HELD.put(body.m_19879_(), new Prison(level, user, body, hold, now + WaterPrison.HOLD, floated));
        PieceNetwork.around(body, new S2CWater(S2CWater.PRISON_SEAL, 0, user.m_19879_(), WaterPrison.HOLD, body.m_19879_(), hold.f_82479_,
            hold.f_82480_, hold.f_82481_, 0, 0, 0));
        WaterServer.vanilla(level, hold, "entity.player.splash", 1.0F, 0.75F);
        WaterServer.vanilla(level, hold, "ambient.underwater.enter", 0.8F, 1.0F);
    }

    /** True while {@code body} is held in a Water Prison. */
    public static boolean held(Entity body) {
        return body != null && HELD.containsKey(body.m_19879_());
    }

    /** {@code player} let out its Conqueror's Haki: a prison round it bursts. */
    public static void conqueror(ServerPlayer player) {
        Prison prison = HELD.remove(player.m_19879_());
        if (prison != null) {
            release(prison, true);
        }
    }

    /** A prison let go of: the body drops; {@code burst}: the bubble bursts (every client near sees it, hears it). */
    static void release(Prison prison, boolean burst) {
        LivingEntity body = prison.body;
        if (prison.floated) {
            body.m_20242_(false);
        }
        Vec3 at = body.m_20182_().m_82520_(0.0, body.m_20206_() * 0.5, 0.0);
        PieceNetwork.around(body, new S2CWater(S2CWater.PRISON_BURST, 0, prison.user.m_19879_(), burst ? 1 : 0, body.m_19879_(), at.f_82479_,
            at.f_82480_, at.f_82481_, 0, 0, 0));
        WaterServer.vanilla(prison.level, at, "entity.generic.splash", 1.0F, burst ? 0.8F : 1.1F);
    }

    @SubscribeEvent
    public static void prisonTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || HELD.isEmpty()) {
            return;
        }
        long now = level.m_46467_();
        for (Iterator<Map.Entry<Integer, Prison>> it = HELD.entrySet().iterator(); it.hasNext(); ) {
            Prison prison = it.next().getValue();
            if (prison.level != level) {
                continue;
            }
            LivingEntity body = prison.body;
            if (!body.m_6084_() || body.m_213877_() || body.m_9236_() != level || now >= prison.until) {
                it.remove();
                release(prison, body.m_6084_());
                continue;
            }
            // held where it floats: put back if it drifts, its legs slowed to nothing, nothing thrown
            if (body.m_20182_().m_82554_(prison.hold) > DRIFT) {
                body.m_6021_(prison.hold.f_82479_, prison.hold.f_82480_, prison.hold.f_82481_);
            }
            body.m_20256_(Vec3.f_82478_);
            body.f_19789_ = 0.0F;
            body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 3, 9, false, false, false));
            Clashes.stagger(body, now, 3);
        }
    }

    /** A held body is hurt: the damage dealt inside the bubble adds up, and enough bursts it. */
    @SubscribeEvent
    public static void prisonHurt(LivingHurtEvent event) {
        Prison prison = HELD.get(event.getEntity().m_19879_());
        if (prison == null) {
            return;
        }
        prison.taken += event.getAmount();
        if (WaterPrison.bursts(prison.taken)) {
            HELD.remove(event.getEntity().m_19879_());
            release(prison, true);
        }
    }

    /** A perfect stun: a held body strikes nothing (its blows, and what it shoots). */
    @SubscribeEvent
    public static void prisonAttack(LivingAttackEvent event) {
        if (HELD.isEmpty()) {
            return;
        }
        Entity attacker = event.getSource().m_7639_();
        if (attacker != null && attacker != event.getEntity() && HELD.containsKey(attacker.m_19879_())) {
            event.setCanceled(true);
        }
    }
}
