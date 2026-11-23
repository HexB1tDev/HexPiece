package com.hexhaki.piece.client.ito;

import com.hexhaki.piece.PieceEntities;
import com.hexhaki.piece.client.npc.StylePoser;
import com.hexhaki.piece.ito.StringKnight;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.4.2 (the owner: "Black knight looks odd as well, it should be a clone"): the Black Knight (ito/StringKnight) drawn as a true clone
 * of its weaver: the player model in the weaver's own skin, slim or wide armed as the weaver is, walking with the game's own stride; its
 * lashes posed from the weaver's own String Slash (tools/ito_anims.py, through {@link StylePoser}, as the pirates' moves are). The
 * strings it is woven of are drawn over it by client/ito/ItoFx (told here which knights were drawn this frame).
 */
public final class KnightRenderer extends HumanoidMobRenderer<StringKnight, PlayerModel<StringKnight>> {
    /** Steve's skin, while its weaver is unknown to this client. */
    static final ResourceLocation STEVE = ResourceLocation.m_214293_("minecraft", "textures/entity/player/wide/steve.png");
    /** How long a lash shows (ticks) and how long the pose takes to settle after it. */
    static final float CUT_TICKS = 10.0F;
    static final float RETURN = 4.0F;

    private final KnightRenderer slim;

    public KnightRenderer(EntityRendererProvider.Context context) {
        this(context, false);
    }

    KnightRenderer(EntityRendererProvider.Context context, boolean slim) {
        super(context, new Model(context.m_174023_(slim ? ModelLayers.f_171166_ : ModelLayers.f_171162_), slim), 0.5F);
        this.slim = slim ? null : new KnightRenderer(context, true);
    }

    static AbstractClientPlayer weaver(StringKnight knight) {
        return knight.m_9236_() != null && knight.m_9236_().m_6815_(knight.weaverId()) instanceof AbstractClientPlayer player ? player : null;
    }

    @Override
    public ResourceLocation m_5478_(StringKnight knight) {
        AbstractClientPlayer weaver = weaver(knight);
        return weaver != null ? weaver.m_108560_() : STEVE;
    }

    @Override
    public void m_7392_(StringKnight knight, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        AbstractClientPlayer weaver = weaver(knight);
        if (this.slim != null && weaver != null && "slim".equals(weaver.m_108564_())) {
            this.slim.m_7392_(knight, yaw, partial, pose, buffers, light);
            return;
        }
        ItoFx.knightDrawn(knight, partial);
        float[][] p = null;
        int cut = knight.shownCut();
        if (cut >= 0 && knight.m_9236_() != null) {
            float tick = ((knight.m_9236_().m_46467_() - knight.shownCutStart()) & 0x3FFFFFFF) + partial;
            if (tick >= 0.0F && tick < CUT_TICKS + RETURN) {
                p = StylePoser.sample(cut == 0 ? "ito_slash_1" : "ito_slash_2", tick);
                float out = tick <= CUT_TICKS ? 1.0F : Math.max(0.0F, 1.0F - (tick - CUT_TICKS) / RETURN);
                for (float[] part : p) {
                    part[StylePoser.WEIGHT] *= out;
                }
            }
        }
        Model.pose = p;
        try {
            super.m_7392_(knight, yaw, partial, pose, buffers, light);
        } finally {
            Model.pose = null;
        }
    }

    /** The player model, its arms posed by the knight's lash when it strikes (blended by each part's weight, as the pirates'). */
    static final class Model extends PlayerModel<StringKnight> {
        static float[][] pose;

        Model(ModelPart root, boolean slim) {
            super(root, slim);
        }

        static void apply(ModelPart part, float[] channels) {
            float rad = (float) (Math.PI / 180.0);
            float w = channels[StylePoser.WEIGHT];
            part.f_104203_ += ((channels[3] + channels[6] * 0.45F) * rad - part.f_104203_) * w;
            part.f_104204_ += (channels[4] * rad - part.f_104204_) * w;
            part.f_104205_ += (channels[5] * rad - part.f_104205_) * w;
        }

        @Override
        public void m_6973_(StringKnight knight, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super.m_6973_(knight, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            float[][] p = pose;
            if (p == null) {
                return;
            }
            apply(this.f_102811_, p[2]);
            apply(this.f_102812_, p[3]);
            this.f_103375_.m_104315_(this.f_102811_);
            this.f_103374_.m_104315_(this.f_102812_);
        }
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(PieceEntities.KNIGHT.get(), KnightRenderer::new);
        }
    }

    static Minecraft minecraft() {
        return Minecraft.m_91087_();
    }
}
