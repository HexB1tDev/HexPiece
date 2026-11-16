package com.hexhaki.piece.client.npc;

import com.hexhaki.piece.PieceEntities;
import com.hexhaki.piece.client.render.FemaleFigure;
import com.hexhaki.piece.npc.SeaFighter;
import com.hexhaki.piece.style.StyleMove;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.1: a pirate or a marine, on a player's body with its side's skin ({@code textures/piece/npc/<side>_<look>.png}); a woman with the
 * female body. While it fights, its body plays the very keyframes a player's does ({@link StylePoser}): limbs on the model, the whole body's
 * turn, lean and step about its hips. (Without bendy-lib on an NPC, a straight limb points between where the bent one's halves would:
 * its pitch plus 45% of the elbow's or knee's bend.)
 */
public final class SeaFighterRenderer extends HumanoidMobRenderer<SeaFighter, PlayerModel<SeaFighter>> {
    private static final ResourceLocation[][] SKINS = new ResourceLocation[2][8];

    static {
        for (int i = 0; i < 8; i++) {
            SKINS[0][i] = ResourceLocation.m_214293_("hexhaki", "textures/piece/npc/pirate_" + i + ".png");
            SKINS[1][i] = ResourceLocation.m_214293_("hexhaki", "textures/piece/npc/marine_" + i + ".png");
        }
    }

    public SeaFighterRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.m_174023_(ModelLayers.f_171162_)), 0.5F);
        this.m_115326_(new Bust(this));
    }

    @Override
    public ResourceLocation m_5478_(SeaFighter fighter) {
        return SKINS[fighter.faction() == com.hexhaki.piece.Faction.MARINE ? 1 : 0][Math.floorMod(fighter.look(), 8)];
    }

    /** The move's moment for this frame, or -1 when it is not fighting. */
    static float moment(SeaFighter fighter, float partial) {
        StyleMove move = fighter.shownMove();
        if (move == null || fighter.m_9236_() == null) {
            return -1.0F;
        }
        long now = fighter.m_9236_().m_46467_() & 0x3FFFFFFF;
        float tick = (float) (now - fighter.shownStart()) + partial;
        return tick < 0 || tick > move.duration() + 1 ? -1.0F : tick;
    }

    @Override
    public void m_7392_(SeaFighter fighter, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        float tick = moment(fighter, partial);
        if (tick < 0) {
            super.m_7392_(fighter, yaw, partial, pose, buffers, light);
            return;
        }
        float[][] p = StylePoser.sample(fighter.shownMove().animation(), tick);
        float[] body = p[0];
        float bodyYaw = fighter.f_20884_ + (fighter.f_20883_ - fighter.f_20884_) * partial;
        pose.m_85836_();
        // the whole body in the fighter's own facing: its step (blocks), then its turn, lean and roll about the hips (0.7 blocks up)
        pose.m_252781_(Axis.f_252436_.m_252977_(-bodyYaw));
        pose.m_252880_(-body[0], body[1], -body[2]);
        pose.m_252880_(0.0F, 0.7F, 0.0F);
        // In this frame +z is the fighter's forward and y is up: the animation's yaw + turns left (about +y), its pitch + leans back and its
        // bend + hunches forward (about x, + tipping forward), its roll + leans left (about z, - here).
        pose.m_252781_(Axis.f_252403_.m_252977_(-body[5]));
        pose.m_252781_(Axis.f_252436_.m_252977_(body[4]));
        pose.m_252781_(Axis.f_252529_.m_252977_(-body[3] + body[6] * 0.5F));
        pose.m_252880_(0.0F, -0.7F, 0.0F);
        pose.m_252781_(Axis.f_252436_.m_252977_(bodyYaw));
        Model.pose = p;
        try {
            super.m_7392_(fighter, yaw, partial, pose, buffers, light);
        } finally {
            Model.pose = null;
            pose.m_85849_();
        }
    }

    static final class Model extends PlayerModel<SeaFighter> {
        /** The pose of the fighter drawn now (the render thread's alone), or null. */
        static float[][] pose;

        Model(ModelPart root) {
            super(root, false);
        }

        static void apply(ModelPart part, float[] channels, float foldShare) {
            float rad = (float) (Math.PI / 180.0);
            part.f_104203_ = (channels[3] + channels[6] * foldShare) * rad;
            part.f_104204_ = channels[4] * rad;
            part.f_104205_ = channels[5] * rad;
        }

        @Override
        public void m_6973_(SeaFighter fighter, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super.m_6973_(fighter, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            float[][] p = pose;
            if (p == null) {
                return;
            }
            float rad = (float) (Math.PI / 180.0);
            this.f_102808_.f_104203_ = p[1][3] * rad + headPitch * rad * 0.3F;
            this.f_102808_.f_104204_ = p[1][4] * rad + netHeadYaw * rad * 0.3F;
            this.f_102808_.f_104205_ = p[1][5] * rad;
            apply(this.f_102811_, p[2], 0.45F);
            apply(this.f_102812_, p[3], 0.45F);
            apply(this.f_102813_, p[4], 0.45F);
            apply(this.f_102814_, p[5], 0.45F);
            this.f_102809_.m_104315_(this.f_102808_);
            this.f_103375_.m_104315_(this.f_102811_);
            this.f_103374_.m_104315_(this.f_102812_);
            this.f_103377_.m_104315_(this.f_102813_);
            this.f_103376_.m_104315_(this.f_102814_);
        }
    }

    /** A woman's bust (the female body), painted with her skin; her legs and arms shaped as she is drawn. */
    static final class Bust extends RenderLayer<SeaFighter, PlayerModel<SeaFighter>> {
        Bust(RenderLayerParent<SeaFighter, PlayerModel<SeaFighter>> parent) {
            super(parent);
        }

        @Override
        public void m_6494_(PoseStack pose, MultiBufferSource buffers, int light, SeaFighter fighter, float limbSwing, float limbSwingAmount,
                            float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!fighter.female() || fighter.m_20145_()) {
                return;
            }
            try {
                double y = fighter.f_19791_ + (fighter.m_20186_() - fighter.f_19791_) * partialTick;
                float bounce = FemaleFigure.jiggle(fighter.m_19879_(), y, ageInTicks);
                RenderType skin = RenderType.m_110473_(SKINS[fighter.faction() == com.hexhaki.piece.Faction.MARINE ? 1 : 0][Math.floorMod(fighter.look(), 8)]);
                FemaleFigure.bust(this.m_117386_().f_102810_, pose, buffers.m_6299_(skin), light, bounce, false);
                FemaleFigure.bust(this.m_117386_().f_102810_, pose, buffers.m_6299_(skin), light, bounce, true);
            } catch (RuntimeException failure) {
                // a broken layer never stops the fighter from being drawn
            }
        }
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(PieceEntities.PIRATE.get(), SeaFighterRenderer::new);
            event.registerEntityRenderer(PieceEntities.MARINE.get(), SeaFighterRenderer::new);
        }
    }

    static Minecraft minecraft() {
        return Minecraft.m_91087_();
    }
}
