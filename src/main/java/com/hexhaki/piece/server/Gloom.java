package com.hexhaki.piece.server;

import com.hexhaki.piece.PieceEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.3.6 (the owner: "make sure gloom is it's own custom effect, causes blindness and slowness, attacks of all kinds, haki, fruit,
 * style, m1s, etc, miss often (miss the Horo Horo user and any other entity the player tries to hit"; asked, they chose 8 in 10 missing
 * and 8 seconds): Gloom, the despair a Negative Hollow leaves in whoever it passes through (server/HoroMoves.despair). While it lasts the
 * body is blind (the game's Blindness, renewed a second at a time and its icon hidden: Gloom's own shows) and slowed (its own modifier,
 * as the game's Slowness III), and every attack it makes, of any kind (a punch, a sword, a Haki blow, a fruit move, a fighting style's,
 * an arrow), misses {@link #MISS} of the time, whoever it is aimed at.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class Gloom extends MobEffect {
    /** How long a Negative Hollow's Gloom lasts (ticks) and how often a gloomed body's attacks miss. */
    public static final int TICKS = 160;
    public static final double MISS = 0.8;
    /** The slowness: as the game's Slowness III (-15 % a level of the speed, all told). */
    static final double SLOW = -0.45;
    static final String SLOW_ID = "a3d1f6e2-5b7c-4e19-8c2d-6f0b9e4a1c37";
    /** The blindness renewed every so many ticks, for so long (never under the second in which the game fades it out). */
    static final int RENEW = 20;
    static final int BLIND = 45;

    public Gloom() {
        super(MobEffectCategory.HARMFUL, 0x343A78);
        this.m_19472_(Attributes.f_22279_, SLOW_ID, SLOW, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    /** The blindness renewed once a second (and on the first tick). */
    @Override
    public boolean m_6584_(int duration, int amplifier) {
        return duration % RENEW == 0 || duration == TICKS;
    }

    @Override
    public void m_6742_(LivingEntity body, int amplifier) {
        if (body.m_9236_().f_46443_) {
            return;
        }
        body.m_7292_(new MobEffectInstance(MobEffects.f_19610_, BLIND, 0, false, false, false));
    }

    /** Whether {@code body} is in Gloom now. */
    public static boolean gloomed(LivingEntity body) {
        return body.m_21023_(PieceEffects.GLOOM.get());
    }

    /** {@code body} in Gloom for {@code ticks} (a longer one already on it is kept). */
    public static void put(LivingEntity body, int ticks) {
        body.m_7292_(new MobEffectInstance(PieceEffects.GLOOM.get(), ticks, 0, false, false, true));
        body.m_7292_(new MobEffectInstance(MobEffects.f_19610_, BLIND, 0, false, false, false));
    }

    /** A gloomed body's attack, of any kind and on anyone, misses {@link #MISS} of the time. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void miss(LivingAttackEvent event) {
        Entity striker = event.getSource().m_7639_();
        if (striker instanceof LivingEntity body && body != event.getEntity() && gloomed(body) && body.m_217043_().m_188501_() < MISS) {
            event.setCanceled(true);
        }
    }
}
