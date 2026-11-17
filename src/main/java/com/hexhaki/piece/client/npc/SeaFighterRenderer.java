package com.hexhaki.piece.client.npc;

import com.hexhaki.piece.PieceEntities;
import com.hexhaki.piece.client.render.FemaleFigure;
import com.hexhaki.piece.npc.SeaFighter;
import com.hexhaki.piece.client.sword.KatanaRenderer;
import com.hexhaki.piece.clash.ClashRules;
import com.hexhaki.piece.style.StyleMove;
import com.hexhaki.piece.sword.Katana;
import com.hexhaki.piece.npc.Gesture;
import com.hexhaki.piece.npc.GesturePose;
import com.hexhaki.piece.sword.SwordAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
    /** 2.3.2: each skin's shut eyes (tools/npc_skins.py: the same face's, from HexGhoul), laid over it while it blinks. */
    private static final ResourceLocation[][] CLOSED = new ResourceLocation[2][8];

    static {
        for (int i = 0; i < 8; i++) {
            SKINS[0][i] = ResourceLocation.m_214293_("hexhaki", "textures/piece/npc/pirate_" + i + ".png");
            SKINS[1][i] = ResourceLocation.m_214293_("hexhaki", "textures/piece/npc/marine_" + i + ".png");
            CLOSED[0][i] = ResourceLocation.m_214293_("hexhaki", "textures/piece/npc/pirate_" + i + "_closed.png");
            CLOSED[1][i] = ResourceLocation.m_214293_("hexhaki", "textures/piece/npc/marine_" + i + "_closed.png");
        }
    }

    public SeaFighterRenderer(EntityRendererProvider.Context context) {
        // 2.3.2 (the owner: "the NPCS are all alex"): the slim-armed player model
        super(context, new Model(context.m_174023_(ModelLayers.f_171166_)), 0.5F);
        this.m_115326_(new Blink(this));
        this.m_115326_(new Bust(this));
        this.m_115326_(new KatanaLayer(this));
        // 2.3.1: what it wears (a Marine's uniform: the owner's armor, drawn by its own model through Forge's armor-model hook)
        this.m_115326_(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.m_174023_(ModelLayers.f_171164_)),
            new HumanoidModel<>(context.m_174023_(ModelLayers.f_171165_)), context.m_266367_()));
    }

    /** What the fighter plays now: a style move's or (2.1.4) its sword's animation, how long it is, and the moment (-1: nothing). */
    record Shown(String animation, int duration, float tick, int anim) {
    }

    /** 2.3.3: Shown.anim of an idle gesture (its own envelope: never faded by {@link #held}). */
    static final int GESTURE = -2;

    static Shown shown(SeaFighter fighter, float partial) {
        if (fighter.m_9236_() == null) {
            return null;
        }
        long now = fighter.m_9236_().m_46467_() & 0x3FFFFFFF;
        int anim = fighter.shownSword();
        if (anim != SwordAnims.NONE) {
            float ticks = ((now - fighter.shownSwordStart()) & 0x3FFFFFFF) + partial;
            String name = SwordAnims.name(anim);
            if (name != null) {
                return new Shown(name, SwordAnims.duration(anim), SwordAnims.frame(anim, ticks), anim);
            }
        }
        StyleMove move = fighter.shownMove();
        if (move == null && fighter.shownClash()) {
            // 2.3.6: knocked back by a clash, its recoil (a player's own keyframes)
            float tick = (float) ((now - fighter.shownStart()) & 0x3FFFFFFF) + partial;
            return tick > ClashRules.RECOIL + RETURN ? null : new Shown(ClashRules.recoil(fighter.style()), ClashRules.RECOIL, tick, -1);
        }
        if (move == null) {
            // 2.3.3: standing idle, a gesture (npc/GesturePose eases it in and out itself)
            Gesture gesture = fighter.gesture();
            if (gesture != Gesture.NONE) {
                float age = (float) ((now - fighter.gestureStart()) & 0x3FFFFFFF) + partial;
                return age > gesture.ticks ? null : new Shown("gesture", gesture.ticks, age, GESTURE);
            }
            return null;
        }
        float tick = (float) (now - fighter.shownStart()) + partial;
        return tick < 0 || tick > move.duration() + RETURN ? null : new Shown(move.animation(), move.duration(), tick, -1);
    }

    @Override
    public ResourceLocation m_5478_(SeaFighter fighter) {
        return SKINS[fighter.faction() == com.hexhaki.piece.Faction.MARINE ? 1 : 0][Math.floorMod(fighter.look(), 8)];
    }


    /** 2.1.3: ticks after a move's end over which the fighter returns to its own pose (as a player's animation returns by stopTick). */
    static final float RETURN = 5.0F;

    /** How much of a move still shows at {@code tick}: 1 until its end, then down to 0 over {@link #RETURN} ticks. */
    static float held(Shown shown) {
        if (shown.anim() == GESTURE || shown.anim() >= 0 && SwordAnims.loops(shown.anim())) {
            return 1.0F;
        }
        float tick = shown.tick();
        float over = tick - shown.duration();
        float t = over <= 0.0F ? 1.0F : Math.max(0.0F, 1.0F - over / RETURN);
        return t * t * (3.0F - 2.0F * t);
    }

    @Override
    public void m_7392_(SeaFighter fighter, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        Shown shown = shown(fighter, partial);
        if (shown == null || shown.tick() > shown.duration() + RETURN && !(shown.anim() >= 0 && SwordAnims.loops(shown.anim()))) {
            Model.pose = null;
            super.m_7392_(fighter, yaw, partial, pose, buffers, light);
            return;
        }
        float[][] p = shown.anim() == GESTURE ? GesturePose.pose(fighter.gesture(), shown.tick()) : StylePoser.sample(shown.animation(), shown.tick());
        float out = held(shown);
        for (float[] part : p) {
            part[StylePoser.WEIGHT] *= out;
        }
        float[] body = p[0].clone();
        for (int channel = 0; channel < StylePoser.WEIGHT; channel++) {
            body[channel] *= p[0][StylePoser.WEIGHT];
        }
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
            super(root, true);
        }

        /** The part posed by the animation, blended over the fighter's own pose by the part's weight (2.1.3). */
        static void apply(ModelPart part, float[] channels, float foldShare) {
            float rad = (float) (Math.PI / 180.0);
            float w = channels[StylePoser.WEIGHT];
            part.f_104203_ += ((channels[3] + channels[6] * foldShare) * rad - part.f_104203_) * w;
            part.f_104204_ += (channels[4] * rad - part.f_104204_) * w;
            part.f_104205_ += (channels[5] * rad - part.f_104205_) * w;
        }

        @Override
        public void m_6973_(SeaFighter fighter, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super.m_6973_(fighter, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            float[][] p = pose;
            if (p == null) {
                return;
            }
            float rad = (float) (Math.PI / 180.0);
            float headWeight = p[1][StylePoser.WEIGHT];
            this.f_102808_.f_104203_ += (p[1][3] * rad + headPitch * rad * 0.3F - this.f_102808_.f_104203_) * headWeight;
            this.f_102808_.f_104204_ += (p[1][4] * rad + netHeadYaw * rad * 0.3F - this.f_102808_.f_104204_) * headWeight;
            this.f_102808_.f_104205_ += (p[1][5] * rad - this.f_102808_.f_104205_) * headWeight;
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

    /**
     * 2.1.4: a pirate swordsman's katana, as a player's ({@code client/sword/SwordLayer}): the saya at the left hip (turned by the
     * animation's saya channel), the blade in it or, drawn, in the right hand turned by its wrist channel. An NPC has no bendy-lib: its
     * arm is drawn straight at its pitch plus 45% of the elbow's bend, so the blade is turned by the rest of the bend at the hand, to
     * point where a player's would.
     */
    static final class KatanaLayer extends RenderLayer<SeaFighter, PlayerModel<SeaFighter>> {
        static final float PX = 1.0F / 16.0F;

        KatanaLayer(RenderLayerParent<SeaFighter, PlayerModel<SeaFighter>> parent) {
            super(parent);
        }

        @Override
        public void m_6494_(PoseStack pose, MultiBufferSource buffers, int light, SeaFighter fighter, float limbSwing, float limbSwingAmount,
                            float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            try {
                Katana katana = fighter.katana();
                if (katana == null || fighter.m_20145_()) {
                    return;
                }
                Shown shown = shown(fighter, partialTick);
                int anim = shown == null || shown.anim() < 0 ? SwordAnims.NONE : shown.anim();
                float ticks = shown == null ? 0.0F : shown.tick();
                boolean drawn = SwordAnims.drawn(anim, ticks);
                float[][] p = Model.pose;
                PlayerModel<SeaFighter> model = this.m_117386_();
                pose.m_85836_();
                model.f_102810_.m_104299_(pose);
                pose.m_252880_(3.4F * PX, 10.0F * PX, -4.2F * PX);
                if (p != null && anim != SwordAnims.NONE) {
                    float[] saya = p[7];
                    pose.m_252880_(saya[0] * PX, saya[1] * PX, saya[2] * PX);
                    pose.m_252781_(Axis.f_252403_.m_252977_(saya[5]));
                    pose.m_252781_(Axis.f_252436_.m_252977_(saya[4]));
                    pose.m_252781_(Axis.f_252529_.m_252977_(saya[3]));
                }
                pose.m_252781_(Axis.f_252403_.m_252977_(95.6825F));
                pose.m_252781_(Axis.f_252436_.m_252977_(22.4011F));
                pose.m_252781_(Axis.f_252529_.m_252977_(114.953F));
                KatanaRenderer.draw(pose, buffers, katana, !drawn, true, light, 0.0F);
                pose.m_85849_();
                if (drawn && p != null) {
                    pose.m_85836_();
                    model.f_102811_.m_104299_(pose);
                    pose.m_252880_(-1.0F * PX, 8.5F * PX, 0.0F);
                    pose.m_252781_(Axis.f_252529_.m_252977_(p[2][6] * 0.55F)); // the rest of the elbow's bend, at the hand
                    float[] wrist = p[6];
                    pose.m_252781_(Axis.f_252403_.m_252977_(wrist[5]));
                    pose.m_252781_(Axis.f_252436_.m_252977_(wrist[4]));
                    pose.m_252781_(Axis.f_252529_.m_252977_(wrist[3]));
                    pose.m_252781_(Axis.f_252403_.m_252977_(-90.0F));
                    pose.m_252781_(Axis.f_252529_.m_252977_(-90.0F));
                    pose.m_252880_(0.0F, -(KatanaRenderer.GRIP - wrist[1]) * KatanaRenderer.SCALE, 0.0F);
                    KatanaRenderer.draw(pose, buffers, katana, true, false, light, 1.0F);
                    pose.m_85849_();
                }
            } catch (RuntimeException failure) {
                // a broken layer never stops the fighter from being drawn
            }
        }
    }

    /**
     * 2.3.2: its eyes shut now and then, a short blink (now and then twice), as HexGhoul's Eto, Touka, Ken and villagers blink
     * ({@link com.hexhaki.piece.npc.Blinks}): the skin's shut-eyes overlay drawn over the model.
     */
    static final class Blink extends RenderLayer<SeaFighter, PlayerModel<SeaFighter>> {
        Blink(RenderLayerParent<SeaFighter, PlayerModel<SeaFighter>> parent) {
            super(parent);
        }

        @Override
        public void m_6494_(PoseStack pose, MultiBufferSource buffers, int light, SeaFighter fighter, float limbSwing, float limbSwingAmount,
                            float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (fighter.m_20145_() || !com.hexhaki.piece.npc.Blinks.shut(fighter.m_19879_() * 31 + 7, ageInTicks)) {
                return;
            }
            try {
                ResourceLocation shut = CLOSED[fighter.faction() == com.hexhaki.piece.Faction.MARINE ? 1 : 0][Math.floorMod(fighter.look(), 8)];
                this.m_117386_().m_7695_(pose, buffers.m_6299_(RenderType.m_110473_(shut)), light, OverlayTexture.f_118083_, 1.0F, 1.0F, 1.0F, 1.0F);
            } catch (RuntimeException failure) {
                // a broken layer never stops the fighter from being drawn
            }
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
