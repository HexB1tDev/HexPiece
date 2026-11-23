package com.hexhaki.piece;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 2.7.3 (the owner: "plus you gain a new universal effect called Transformed, doubles your HP and you get resistance as well. This effect
 * will also apply to Nika (the fruit)"): the Transformed effect, while the Uo Uo no Mi's dragon is out and while Gear 5 is on
 * (server/Transformed): its own modifier doubles the maximum health; Resistance II comes with it.
 */
public final class PieceEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, HexPiece.MODID);
    /** The modifier's fixed id (so the game finds and removes the same one). */
    static final String HEALTH_ID = "6f2b1d34-6c5e-4f1a-9a3e-7b1f0d2c7e41";
    public static final RegistryObject<MobEffect> TRANSFORMED = EFFECTS.register("transformed", Transformed::new);
    /** 3.3.6: Gloom (server/Gloom), what a Negative Hollow leaves in whoever it passes through. */
    public static final RegistryObject<MobEffect> GLOOM = EFFECTS.register("gloom", com.hexhaki.piece.server.Gloom::new);

    private PieceEffects() {
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }

    /** Max health x 2: the base and its additions doubled (MULTIPLY_BASE 1). */
    static final class Transformed extends MobEffect {
        Transformed() {
            super(MobEffectCategory.BENEFICIAL, 0x4FB7E8);
            this.m_19472_(Attributes.f_22276_, HEALTH_ID, 1.0, AttributeModifier.Operation.MULTIPLY_BASE);
        }
    }
}
