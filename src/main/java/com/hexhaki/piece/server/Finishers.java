package com.hexhaki.piece.server;

import com.hexhaki.gore.Gore;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CFinisher;
import com.hexhaki.piece.sword.Finisher;
import com.hexhaki.registry.ModSounds;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * The Ryou finisher (2.1.4; the owner: "with ryou active, the right click has a finisher, whenever a mob is killed by it they stop ...
 * a very precise red line slowly appears ... both halfs of the mob fall each to it's side, blood splatters all over the floor"), since
 * 2.2.1 HexGhoul's Steel Blade finisher turned upright (the owner: "the slice in half should 100% be taken from Hex Ghoul ... the cut and
 * everything about it. Just make sure instead of horizontal, it's vertical").
 * <p>
 * A Haki slash released with Ryou on that would kill doesn't: the body is held where it stands for {@link Finisher#STUN} ticks while
 * every client draws the line round it along the blade's upright plane (client/sword/FinisherFx); it can do nothing at all (no blow, no
 * item, no step; a mob's mind switched off) and nothing else may hurt it. When the line meets itself every client cuts it in two (the
 * halves fall each to its side as HexGhoul's ragdoll, the insides on each cut) and it dies, the slash's kill, the original mod's blood
 * bursting from the cut. Bosses and bodies too big for it are simply hit.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class Finishers {
    /** The bodies under the finisher, by entity id. */
    private static final Map<Integer, Dying> DYING = new HashMap<>();
    /** The finisher's own killing blow is landing (nothing else gets through to a body under it). */
    private static boolean killing;
    /** The most bodies held at once (a slash through a crowd still finishes each). */
    static final int MOST = 64;

    record Dying(LivingEntity victim, ServerLevel level, Vec3 dir, DamageSource source, long from, boolean hadNoAi) {
    }

    private Finishers() {
    }

    /** True while {@code entity} is under the finisher: held, unable to act, unhurt by anything else. */
    public static boolean dying(Entity entity) {
        if (entity == null || DYING.isEmpty()) {
            return false;
        }
        Dying d = DYING.get(entity.m_19879_());
        return d != null && d.level() == entity.m_9236_();
    }

    /** True if {@code body} can be finished: not a boss, nor anything a boss's size. */
    static boolean eligible(LivingEntity body) {
        return !(body instanceof WitherBoss) && body.m_20205_() <= 3.0F && body.m_20206_() <= 4.0F && body.m_9236_() instanceof ServerLevel;
    }

    /** The finisher begins on {@code victim}, the blade going {@code dir} (false if it cannot: it is simply hit). */
    public static boolean start(LivingEntity killer, LivingEntity victim, Vec3 dir, float damage, DamageSource source) {
        if (DYING.containsKey(victim.m_19879_())) {
            return true;
        }
        if (!eligible(victim) || DYING.size() >= MOST || !(victim.m_9236_() instanceof ServerLevel level)) {
            return false;
        }
        boolean hadNoAi = false;
        if (victim instanceof Mob mob) {
            hadNoAi = mob.m_21525_();
            mob.m_21557_(true);
        }
        Vec3 flat = new Vec3(dir.f_82479_, 0.0, dir.f_82481_);
        flat = flat.m_82556_() < 1.0E-6 ? StyleCombat.facing(killer) : flat.m_82541_();
        DYING.put(victim.m_19879_(), new Dying(victim, level, flat, source, level.m_46467_(), hadNoAi));
        hold(victim);
        PieceNetwork.around(victim, new S2CFinisher(victim.m_19879_(), S2CFinisher.FINISH, Finisher.STUN, victim.f_20883_, (float)flat.f_82479_,
            (float)flat.f_82481_));
        // The line drawing itself in: a thin, high ring.
        level.m_6263_(null, victim.m_20185_(), victim.m_20186_() + victim.m_20206_() * 0.55, victim.m_20189_(), ModSounds.ARMAMENT_CRACKLE.get(),
            SoundSource.PLAYERS, 0.6F, 1.9F);
        return true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || DYING.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<Integer, Dying>> it = DYING.entrySet().iterator();
        while (it.hasNext()) {
            Dying d = it.next().getValue();
            LivingEntity victim = d.victim();
            if (!victim.m_6084_() || victim.m_213877_()) {
                it.remove();
                release(d);
                continue;
            }
            if (d.level().m_46467_() - d.from() < Finisher.STUN) {
                hold(victim);
                continue;
            }
            it.remove();
            split(d);
        }
    }

    /** Held where it stands: its own walking slowed to nothing, its motion stopped (it may still fall), a mob's path and target dropped. */
    static void hold(LivingEntity body) {
        body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 3, 10, false, false, false));
        Vec3 motion = body.m_20184_();
        body.m_20256_(new Vec3(0.0, Math.min(motion.f_82480_, 0.0), 0.0));
        if (body instanceof Player) {
            body.f_19864_ = true;
        }
        if (body instanceof Mob mob) {
            mob.m_21573_().m_26573_();
            mob.m_6710_(null);
        }
    }

    /** The line has met itself: every client cuts the body in two, the original's blood bursts from the cut, and it dies, the slash's kill. */
    static void split(Dying d) {
        LivingEntity body = d.victim();
        PieceNetwork.around(body, new S2CFinisher(body.m_19879_(), S2CFinisher.SPLIT, 0, body.f_20883_, (float)d.dir().f_82479_, (float)d.dir().f_82481_));
        Vec3 at = body.m_20182_().m_82520_(0.0, body.m_20206_() * 0.55, 0.0);
        Entity killer = d.source().m_7639_();
        if (Gore.bleeds(body)) {
            Gore.burst(body, at, new Vec3(-d.dir().f_82481_, 0.35, d.dir().f_82479_), 2.2F, killer);
            Gore.burst(body, at, new Vec3(d.dir().f_82481_, 0.35, -d.dir().f_82479_), 2.2F, killer);
        }
        d.level().m_6263_(null, at.f_82479_, at.f_82480_, at.f_82481_, ModSounds.INTERNAL_HIT.get(), SoundSource.PLAYERS, 1.0F, 0.7F);
        killing = true;
        try {
            body.f_19802_ = 0;
            body.m_6469_(d.source(), Float.MAX_VALUE);
        } finally {
            killing = false;
        }
        if (body.m_6084_()) {
            release(d); // a totem kept it: it gets its mind back
        }
    }

    static void release(Dying d) {
        if (d.victim().m_6084_() && d.victim() instanceof Mob mob) {
            mob.m_21557_(d.hadNoAi());
        }
    }

    // ------------------------------------------------------------------ nothing hurts it, it hurts nothing

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void attack(LivingAttackEvent event) {
        if (DYING.isEmpty()) {
            return;
        }
        if (!killing && dying(event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        DamageSource source = event.getSource();
        if (source != null && (dying(source.m_7639_()) || dying(source.m_7640_()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void swing(AttackEntityEvent event) {
        if (dying(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void useItem(PlayerInteractEvent.RightClickItem event) {
        if (dying(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void useBlock(PlayerInteractEvent.RightClickBlock event) {
        if (dying(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void touchEntity(PlayerInteractEvent.EntityInteract event) {
        if (dying(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void startUsing(LivingEntityUseItemEvent.Start event) {
        if (dying(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        DYING.clear();
        killing = false;
    }
}
