package com.hexhaki.piece.client.horo;

import com.hexhaki.piece.PieceEntities;
import com.hexhaki.piece.horo.AstralBody;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.4.2: the Horo user's body left behind (horo/AstralBody) draws nothing of its own and casts no shadow: client/horo/HoroFx already
 * draws it where it stands, in its owner's own skin, standing asleep (judged in 3.3.6). It is there to be struck.
 */
public final class BodyRenderer extends HumanoidMobRenderer<AstralBody, PlayerModel<AstralBody>> {
    static final ResourceLocation STEVE = ResourceLocation.m_214293_("minecraft", "textures/entity/player/wide/steve.png");

    public BodyRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.m_174023_(ModelLayers.f_171162_), false), 0.0F);
    }

    @Override
    public ResourceLocation m_5478_(AstralBody body) {
        return STEVE;
    }

    @Override
    public void m_7392_(AstralBody body, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        // drawn by HoroFx
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(PieceEntities.ASTRAL_BODY.get(), BodyRenderer::new);
        }
    }
}
