package com.hexhaki.piece.client.sword;

import com.hexhaki.client.RemoteHakiStates;
import com.hexhaki.client.render.ArmamentItemBuffers;
import com.hexhaki.piece.client.StanceLayer;
import com.hexhaki.piece.sword.BladePaths;
import com.hexhaki.piece.sword.BackCarry;
import com.hexhaki.piece.sword.Katana;
import com.hexhaki.piece.sword.Koiguchi;
import com.hexhaki.piece.sword.SwordAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

/**
 * 2.1.4: the katana on a player: its saya at the left hip (the owner: "Attach the matching sheath to the player's hip so it follows the
 * body naturally during walking, sprinting, crouching and attacks"), the blade in it with its handle out, or in the right hand when
 * drawn ("While drawn, the empty sheath stays attached to the hip and the same sword appears in the attacking hand").
 * <p>
 * Every transform is the game's own, in the order tools/sword_rig.py reproduces them for the previews and the server's blade paths:
 * the saya on the body part (the upper-body bend every layer is given undone: the hips do not hunch), at the koiguchi (KOIGUCHI), turned
 * and moved by the animation's leftItem channel (the left hand working the saya), then its rest frame (HIP); the blade on the right arm
 * part, bent with the forearm as the game bends a held item, at the fist's centre, turned by the animation's rightItem rotation (the
 * wrist) and slid along the handle by its rightItem y, then the grip (the blade along the fist, the edge down). While the blade slides
 * along the saya's line (a draw leaving it, a sheathing going home, BladePaths.notoOut) it is laid exactly along the saya's curve, so
 * no frame between two ticks can put it through the saya's side.
 * <p>
 * It is a {@link PlayerItemInHandLayer}: playerAnimator's first-person pass draws only that kind of layer, so the drawn blade is in the
 * hand in first person too (there the saya, out of sight at the hip, is left out). The katana item itself draws nothing in a hand (its
 * own renderer), so it is never seen twice. Armament Haki blackens the blade as the original blackens a held weapon, and (2.1.5) makes
 * it crackle with Haki lightning as it is drawn, charged and swung ({@link HakiDraw}).
 */
public final class SwordLayer extends PlayerItemInHandLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    static final float PX = 1.0F / 16.0F;
    /** The koiguchi at the front of the left hip (body-part px), and the saya's rest frame there (degrees, Z then Y then X). */
    static final float[] KOIGUCHI = {3.4F, 10.0F, -4.2F};
    static final float HIP_PITCH = 114.953F;
    static final float HIP_YAW = 22.4011F;
    static final float HIP_ROLL = 95.6825F;
    /** The blades' curve (model px): drawn out of the saya a blade turns about (SORI, 0, 0) (tools/sword_rig.py arc). */
    static final float SORI = 180.0F;

    public SwordLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent, null);
    }

    @Override
    public void m_6494_(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                        float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        try {
            Katana katana = SwordClient.katana(player.m_19879_());
            if (katana == null || player.m_20145_()) {
                return;
            }
            draw(pose, buffers, light, player, katana, partialTick);
        } catch (RuntimeException failure) {
            // a sword that cannot be drawn never stops the player from being drawn
        }
    }

    void draw(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, Katana katana, float partial) {
        PlayerModel<AbstractClientPlayer> model = this.m_117386_();
        int anim = SwordClient.anim(player.m_19879_());
        float ticks = SwordClient.ticks(player, partial);
        boolean drawn = SwordAnims.drawn(anim, ticks, katana);
        boolean firstPerson = FirstPersonMode.isFirstPersonPass();
        ModifierLayer<IAnimation> layer = SwordClient.existing(player);
        // 2.2.7: Yoru never turns black (it is black already: only its Haki lightning shows)
        MultiBufferSource bladeBuffers = blackened(player) && katana.blackens() ? ArmamentItemBuffers.black(buffers) : buffers;
        if (katana.carry() == Katana.Carry.BACK) {
            back(pose, buffers, bladeBuffers, light, player, katana, model, layer, anim, ticks, drawn, firstPerson, partial);
            return;
        }
        // 2.3.2 (the owner, of a draw: "pulls sword out, but nothing goes with the arm?"): between its noto beats the blade slides along
        // the saya's line with the hand, whether or not it is out yet: a draw's blade counts as drawn only once it has left the saya
        // (its draw tick), and before 2.3.2 it stayed home in the saya all the while the hand drew it out
        float[] slideOut = notoOut(SwordAnims.name(anim, katana), ticks);
        boolean sliding = slideOut != null && (drawn || slideOut[0] > 0.0F);
        if (!firstPerson || sliding) {
            pose.m_85836_();
            saya(pose, player, model, layer, partial);
            if (!drawn && !sliding) {
                if (!firstPerson) {
                    KatanaRenderer.draw(pose, buffers, katana, false, true, light, 0.0F);
                    // 2.1.5: standing ready, the thumb breaks the saya's seal now and then (sword/Koiguchi)
                    double ready = anim == SwordAnims.NONE ? StanceLayer.swordReady(player, partial) : -1.0;
                    float push = ready < 0 ? 0.0F : Koiguchi.push(ready);
                    if (push > 0.0F) {
                        float s = KatanaRenderer.SCALE;
                        pose.m_252880_(SORI * s, 0.0F, 0.0F);
                        pose.m_252781_(Axis.f_252403_.m_252977_((float) Math.toDegrees(push / SORI)));
                        pose.m_252880_(-SORI * s, 0.0F, 0.0F);
                    }
                    KatanaRenderer.draw(pose, bladeBuffers, katana, true, false, light, 0.0F);
                    if (push > 0.0F) {
                        HakiDraw.sliding(pose, buffers, player, SwordAnims.NONE, (float) ready, push, partial);
                    }
                }
            } else {
                if (!firstPerson) {
                    KatanaRenderer.draw(pose, buffers, katana, false, true, light, 0.0F);
                }
                if (sliding) {
                    // along the saya's own curve, out the interpolated amount
                    float s = KatanaRenderer.SCALE;
                    pose.m_252880_(SORI * s, 0.0F, 0.0F);
                    pose.m_252781_(Axis.f_252403_.m_252977_((float) Math.toDegrees(slideOut[0] / SORI)));
                    pose.m_252880_(-SORI * s, 0.0F, 0.0F);
                    KatanaRenderer.draw(pose, bladeBuffers, katana, true, false, light, 1.0F);
                    SwordFx.record(player, pose, ticks);
                    HakiDraw.sliding(pose, buffers, player, anim, ticks, slideOut[0], partial);
                }
            }
            pose.m_85849_();
        }
        if (drawn && !sliding) {
            pose.m_85836_();
            hand(pose, player, model, layer, partial);
            // its black steel only glints (the owner's unsheath reference: white glints running along a black blade)
            KatanaRenderer.draw(pose, bladeBuffers, katana, true, false, light, katana.blackens() ? 1.0F : 0.45F);
            SwordFx.record(player, pose, ticks);
            HakiDraw.held(pose, buffers, player, anim, ticks, partial);
            pose.m_85849_();
        }
    }

    /**
     * 2.2.7: Yoru, worn on the back (sword/BackCarry: tied on by two ropes, its hilt over the right shoulder), or in the right hand when
     * drawn (its own draw over the shoulder hands it over the very tick the fist closes on it there, as tools/sword_anims.py solves it).
     * The ropes stay on the back either way; first person shows only the hand.
     */
    void back(PoseStack pose, MultiBufferSource buffers, MultiBufferSource bladeBuffers, int light, AbstractClientPlayer player, Katana katana,
              PlayerModel<AbstractClientPlayer> model, ModifierLayer<IAnimation> layer, int anim, float ticks, boolean drawn,
              boolean firstPerson, float partial) {
        if (!firstPerson) {
            pose.m_85836_();
            // the upper body's own bend and turn carry it (unlike the saya at the hips)
            model.f_102810_.m_104299_(pose);
            KatanaRenderer.ropes(pose, buffers, light);
            if (!drawn) {
                pose.m_252880_((float) BackCarry.GUARD[0] * PX, (float) BackCarry.GUARD[1] * PX, (float) BackCarry.GUARD[2] * PX);
                pose.m_252781_(Axis.f_252403_.m_252977_((float) -BackCarry.TILT));
                pose.m_252781_(Axis.f_252529_.m_252977_((float) -BackCarry.LEAN));
                KatanaRenderer.draw(pose, bladeBuffers, katana, true, false, light, 0.0F);
            }
            pose.m_85849_();
        }
        if (drawn) {
            pose.m_85836_();
            hand(pose, player, model, layer, partial);
            // its black steel only glints (the owner's unsheath reference: white glints running along a black blade)
            KatanaRenderer.draw(pose, bladeBuffers, katana, true, false, light, katana.blackens() ? 1.0F : 0.45F);
            SwordFx.record(player, pose, ticks);
            HakiDraw.held(pose, buffers, player, anim, ticks, partial);
            pose.m_85849_();
        }
    }

    static boolean blackened(AbstractClientPlayer player) {
        RemoteHakiStates.State state = RemoteHakiStates.get(player.m_19879_());
        return state != null && state.armament();
    }

    /** Between an animation's noto beats, how far out of the saya the blade is this frame ({out}); null outside them. */
    static float[] notoOut(String animation, float ticks) {
        if (animation == null) {
            return null;
        }
        float[] pairs = BladePaths.notoOut(animation);
        if (pairs == null || pairs.length < 4 || ticks < pairs[0] || ticks > pairs[pairs.length - 2]) {
            return null;
        }
        for (int i = 0; i + 3 < pairs.length; i += 2) {
            float t0 = pairs[i];
            float t1 = pairs[i + 2];
            if (ticks >= t0 && ticks <= t1) {
                float u = t1 > t0 ? (ticks - t0) / (t1 - t0) : 1.0F;
                return new float[] {pairs[i + 1] + (pairs[i + 3] - pairs[i + 1]) * u};
            }
        }
        return null;
    }

    static Vec3f channel(ModifierLayer<IAnimation> layer, String part, TransformType type, float partial) {
        if (layer == null || !layer.isActive()) {
            return Vec3f.ZERO;
        }
        Vec3f value = layer.get3DTransform(part, type, partial, Vec3f.ZERO);
        return value == null ? Vec3f.ZERO : value;
    }

    static float[] bend(AbstractClientPlayer player, String part) {
        if (player instanceof IAnimatedPlayer animated) {
            AnimationApplier animation = animated.playerAnimator_getAnimation();
            if (animation != null && animation.isActive()) {
                Pair<Float, Float> pair = animation.getBend(part);
                if (pair != null) {
                    return new float[] {pair.getLeft(), pair.getRight()};
                }
            }
        }
        return new float[] {0.0F, 0.0F};
    }

    /** Turns the pose by {@code amount} about (cos(-axis), 0, sin(-axis)) at {@code y} blocks down (bendy-lib's and the held item's). */
    static void bendAbout(PoseStack pose, float y, float axis, float amount) {
        if (Math.abs(amount) < 1.0E-5F) {
            return;
        }
        pose.m_252880_(0.0F, y, 0.0F);
        pose.m_252781_(new Quaternionf().rotateAxis(amount, (float) Math.cos(-axis), 0.0F, (float) Math.sin(-axis)));
        pose.m_252880_(0.0F, -y, 0.0F);
    }

    /** The saya's frame (katana model px scaled by KatanaRenderer.SCALE are drawn into it). */
    static void saya(PoseStack pose, AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model, ModifierLayer<IAnimation> layer,
                     float partial) {
        float[] body = bend(player, "body");
        bendAbout(pose, 0.375F, body[0], -body[1]); // the layer was given the upper body's hunch: the hips do not hunch
        model.f_102810_.m_104299_(pose);
        pose.m_252880_(KOIGUCHI[0] * PX, KOIGUCHI[1] * PX, KOIGUCHI[2] * PX);
        Vec3f move = channel(layer, "leftItem", TransformType.POSITION, partial);
        Vec3f turn = channel(layer, "leftItem", TransformType.ROTATION, partial);
        pose.m_252880_(move.getX().floatValue() * PX, move.getY().floatValue() * PX, move.getZ().floatValue() * PX);
        pose.m_252781_(Axis.f_252403_.m_252977_((float) Math.toDegrees(turn.getZ().floatValue())));
        pose.m_252781_(Axis.f_252436_.m_252977_((float) Math.toDegrees(turn.getY().floatValue())));
        pose.m_252781_(Axis.f_252529_.m_252977_((float) Math.toDegrees(turn.getX().floatValue())));
        pose.m_252781_(Axis.f_252403_.m_252977_(HIP_ROLL));
        pose.m_252781_(Axis.f_252436_.m_252977_(HIP_YAW));
        pose.m_252781_(Axis.f_252529_.m_252977_(HIP_PITCH));
    }

    /** The drawn blade's frame in the right hand. */
    static void hand(PoseStack pose, AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model, ModifierLayer<IAnimation> layer,
                     float partial) {
        ModelPart arm = model.f_102811_;
        arm.m_104299_(pose);
        float[] forearm = bend(player, "rightArm");
        bendAbout(pose, 0.25F, forearm[0], forearm[1]);
        pose.m_252880_(("slim".equals(player.m_108564_()) ? -0.5F : -1.0F) * PX, 8.5F * PX, 0.0F);
        Vec3f wrist = channel(layer, "rightItem", TransformType.ROTATION, partial);
        float slide = channel(layer, "rightItem", TransformType.POSITION, partial).getY().floatValue();
        pose.m_252781_(Axis.f_252403_.m_252977_((float) Math.toDegrees(wrist.getZ().floatValue())));
        pose.m_252781_(Axis.f_252436_.m_252977_((float) Math.toDegrees(wrist.getY().floatValue())));
        pose.m_252781_(Axis.f_252529_.m_252977_((float) Math.toDegrees(wrist.getX().floatValue())));
        // the grip: the blade (model +y) along the fist's -z, the edge (model -x) down the fist, the handle's grip point in the fist
        pose.m_252781_(Axis.f_252403_.m_252977_(-90.0F));
        pose.m_252781_(Axis.f_252529_.m_252977_(-90.0F));
        pose.m_252880_(0.0F, -(KatanaRenderer.GRIP - slide) * KatanaRenderer.SCALE, 0.0F);
    }

    /** The layer on both player renderers (wide and slim arms), as they are built. */
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
                    renderer.m_115326_(new SwordLayer(renderer));
                }
            }
        }
    }
}
