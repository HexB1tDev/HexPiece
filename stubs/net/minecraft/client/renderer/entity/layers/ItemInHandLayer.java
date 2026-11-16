// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraft.client.renderer.entity.layers;

/** 2.1.4: fsz (intermediary class_989, yarn HeldItemFeatureRenderer). */
public class ItemInHandLayer<T extends net.minecraft.world.entity.LivingEntity, M extends net.minecraft.client.model.EntityModel<T>> extends RenderLayer<T, M> {
    public ItemInHandLayer(net.minecraft.client.renderer.entity.RenderLayerParent<T, M> parent, net.minecraft.client.renderer.ItemInHandRenderer renderer) {
        super(parent);
    }

    @Override
    public void m_6494_(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers, int packedLight, T entity,
                        float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
