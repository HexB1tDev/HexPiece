package com.hexhaki.piece.client.render;

import com.hexhaki.piece.client.PieceClient;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 2.1.1: a female player's bust, painted with her own skin, not drawn under a chestplate nor while she is unseen. */
public final class FigureLayer extends RenderLayer<AbstractClientPlayer, EntityModel<AbstractClientPlayer>> {
    public FigureLayer(RenderLayerParent<AbstractClientPlayer, EntityModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void m_6494_(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                        float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        try {
            if (!PieceClient.gender(player.m_19879_()).female() || player.m_20145_() || !(this.m_117386_() instanceof PlayerModel<?> model)
                || !player.m_6844_(net.minecraft.world.entity.EquipmentSlot.CHEST).m_41619_()) {
                return;
            }
            double y = player.f_19791_ + (player.m_20186_() - player.f_19791_) * partialTick;
            float bounce = FemaleFigure.jiggle(player.m_19879_(), y, ageInTicks);
            RenderType skin = RenderType.m_110473_(player.m_108560_());
            FemaleFigure.bust(model.f_102810_, pose, buffers.m_6299_(skin), light, bounce, false);
            if (model.f_103378_.f_104207_) {
                FemaleFigure.bust(model.f_102810_, pose, buffers.m_6299_(skin), light, bounce, true);
            }
        } catch (RuntimeException failure) {
            // a broken layer never stops the player from being drawn
        }
    }

    /** The bust layer on both player renderers (wide and slim arms) and any other player skin, as they are built. */
    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        @SuppressWarnings({"rawtypes", "unchecked"})
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            for (String skin : event.getSkins()) {
                LivingEntityRenderer renderer = event.getSkin(skin);
                if (renderer != null) {
                    renderer.m_115326_(new FigureLayer(renderer));
                }
            }
        }
    }
}
