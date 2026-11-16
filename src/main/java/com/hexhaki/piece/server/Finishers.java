package com.hexhaki.piece.server;

import com.hexhaki.gore.Gore;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CFinisher;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.4, the Ryou finisher (the owner: "with ryou active, the right click has a finisher, whenever a mob is killed by it they stop for
 * 1-2 seconds, then a very precise red line slowly appears from bottom to top to bottom (all around the mob), when it reaches the bottom,
 * both halfs of the mob fall each to it's side, blood splatters all over the floor ... for a player, a red line also appears in their
 * screen vision, they are stunned but can look around, then they die leaving behind their body also perfectly cut in half").
 * <p>
 * When a Haki slash released with Ryou on would kill what it hits, the blow is held back: the victim stops where it stands for
 * {@link #FREEZE} ticks (a mob's mind switched off, a player slowed to nothing and unable to jump, free to look round), then a red line
 * traces the cut round its body for {@link #TRACE} ticks (client/sword/FinisherFx draws it on everyone's screen, and down the middle of
 * the victim's own view), and it falls in two along the slash's plane: the original's own halving ({@code Gore.halve}: the body cut by
 * the plane, the halves falling apart, the blood pouring and staining the floor).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class Finishers {
    public static final int FREEZE = 24;
    public static final int TRACE = 26;
    private static final List<Doom> ACTIVE = new ArrayList<>();
    private static final Set<Integer> DOOMED = new HashSet<>();

    record Doom(LivingEntity victim, Vec3 dir, float damage, DamageSource source, long start, boolean hadNoAi) {
    }

    private Finishers() {
    }

    /** The finisher begins on {@code victim} (false if it cannot: a boss is simply hit). */
    public static boolean start(LivingEntity killer, LivingEntity victim, Vec3 dir, float damage, DamageSource source) {
        if (DOOMED.contains(victim.m_19879_())) {
            return true;
        }
        if (victim instanceof WitherBoss || !(victim.m_9236_() instanceof ServerLevel level)) {
            return false;
        }
        boolean hadNoAi = false;
        if (victim instanceof Mob mob) {
            hadNoAi = mob.m_21525_();
            mob.m_21557_(true);
        }
        int hold = FREEZE + TRACE + 6;
        victim.m_7292_(new MobEffectInstance(MobEffects.f_19597_, hold, 255, false, false, false));
        victim.m_7292_(new MobEffectInstance(MobEffects.f_19603_, hold, 200, false, false, false));
        victim.m_20256_(Vec3.f_82478_);
        Vec3 flat = new Vec3(dir.f_82479_, 0.0, dir.f_82481_);
        flat = flat.m_82556_() < 1.0E-6 ? StyleCombat.facing(killer) : flat.m_82541_();
        long now = level.m_46467_();
        ACTIVE.add(new Doom(victim, flat, damage, source, now, hadNoAi));
        DOOMED.add(victim.m_19879_());
        PieceNetwork.around(victim, new S2CFinisher(victim.m_19879_(), (int) (now & 0x3FFFFFFF), FREEZE, TRACE, (float) flat.f_82479_,
            (float) flat.f_82481_));
        return true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) {
            return;
        }
        Iterator<Doom> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Doom doom = it.next();
            LivingEntity victim = doom.victim();
            if (!victim.m_6084_() || victim.m_213877_()) {
                DOOMED.remove(victim.m_19879_());
                it.remove();
                continue;
            }
            Vec3 v = victim.m_20184_();
            victim.m_20256_(new Vec3(0.0, Math.min(0.0, v.f_82480_), 0.0));
            long elapsed = victim.m_9236_().m_46467_() - doom.start();
            if (elapsed < FREEZE + TRACE) {
                continue;
            }
            it.remove();
            DOOMED.remove(victim.m_19879_());
            try {
                // the cut: the slash's vertical plane (its normal the slash's side), drawn from the top down, the halves falling apart
                Vec3 side = new Vec3(-doom.dir().f_82481_, 0.0, doom.dir().f_82479_);
                float damage = Math.max(doom.damage(), victim.m_21223_() + 1000.0F);
                Gore.halve(victim, doom.source(), damage, side, new Vec3(0.0, -1.0, 0.0), doom.dir(), false);
            } finally {
                if (victim.m_6084_() && victim instanceof Mob mob) {
                    mob.m_21557_(doom.hadNoAi());
                }
            }
        }
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        ACTIVE.clear();
        DOOMED.clear();
    }
}
