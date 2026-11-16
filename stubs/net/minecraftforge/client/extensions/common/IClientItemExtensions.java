// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraftforge.client.extensions.common;

/** 4.1.20: Forge 47's client item extensions (an item's own renderer, BEWLR, for a model whose parent is builtin/entity). */
public interface IClientItemExtensions {
    /** getCustomRenderer (Forge's name: Forge's own interface) */
    default net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() { throw new UnsupportedOperationException(); }

    /** 2.1.4: getArmPose (Forge's name) */
    default net.minecraft.client.model.HumanoidModel.ArmPose getArmPose(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.InteractionHand hand,
        net.minecraft.world.item.ItemStack stack) { throw new UnsupportedOperationException(); }
}
