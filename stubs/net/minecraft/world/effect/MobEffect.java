// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraft.world.effect;

public class MobEffect {
    /** 1.3.40: a kitchen effect's (the original's bleeding effect calls it too) */
    protected MobEffect(MobEffectCategory category, int color) {
    }

    /** addAttributeModifier(attribute, uuid, amount, operation) (2.7.3: Transformed's double health) */
    public MobEffect m_19472_(net.minecraft.world.entity.ai.attributes.Attribute attribute, String id, double amount,
                              net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation operation) { throw new UnsupportedOperationException(); }

    /** isDurationEffectTick(duration, amplifier) (3.3.6: Gloom renews the blindness it causes) */
    public boolean m_6584_(int duration, int amplifier) { throw new UnsupportedOperationException(); }

    /** applyEffectTick(body, amplifier) */
    public void m_6742_(net.minecraft.world.entity.LivingEntity body, int amplifier) { throw new UnsupportedOperationException(); }
}
