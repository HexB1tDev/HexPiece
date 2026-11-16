// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraft.client.renderer.entity.layers;

/** 2.1.4: ftf (intermediary class_5697, yarn PlayerHeldItemFeatureRenderer): the only layer playerAnimator keeps in its first-person pass. */
public class PlayerItemInHandLayer<T extends net.minecraft.world.entity.player.Player, M extends net.minecraft.client.model.EntityModel<T>> extends ItemInHandLayer<T, M> {
    public PlayerItemInHandLayer(net.minecraft.client.renderer.entity.RenderLayerParent<T, M> parent, net.minecraft.client.renderer.ItemInHandRenderer renderer) {
        super(parent, renderer);
    }
}
