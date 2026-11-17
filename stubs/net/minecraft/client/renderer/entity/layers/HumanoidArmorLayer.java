// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraft.client.renderer.entity.layers;

/** 2.3.1: HumanoidArmorLayer (fsw C_4437_, yarn ArmorFeatureRenderer): the Marines' uniforms on the NPCs. */
public class HumanoidArmorLayer<T extends net.minecraft.world.entity.LivingEntity, M extends net.minecraft.client.model.HumanoidModel<T>,
    A extends net.minecraft.client.model.HumanoidModel<T>> extends RenderLayer<T, M> {
    public HumanoidArmorLayer(net.minecraft.client.renderer.entity.RenderLayerParent<T, M> parent, A inner, A outer,
                              net.minecraft.client.resources.model.ModelManager models) {
        super(parent);
    }

    @Override
    public void m_6494_(com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource buffers, int packedLight, T entity,
                        float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        throw new UnsupportedOperationException();
    }
}
