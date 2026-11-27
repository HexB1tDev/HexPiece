package com.hexhaki.piece.client.gun;

import com.hexhaki.piece.client.sword.SwordFx;
import com.hexhaki.piece.client.sword.SwordLayer;
import com.hexhaki.piece.gun.Gun;
import com.hexhaki.piece.gun.GunAnims;
import com.hexhaki.piece.gun.GunRules;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.8.8: the gun in a player's right hand, as the animations move it (the gun item itself draws nothing in a hand). The chain is the
 * game's own and tools/gun_anims.py's: the right arm part, bent with the forearm as a held item is, the fist's centre, then the finger
 * on the trigger ({@link #FINGER}, moved by the clip's right-item move), turned there by the clip's right-item turn (so a spin is a spin
 * round the finger), then the grip (the barrel along the fist, its top to the back of the hand), the trigger on the finger. In the
 * reload the lead ball is drawn in the left hand's fingertips and then going into the muzzle; on the shot a muzzle flash, and a puff of
 * smoke from the muzzle.
 */
public final class GunLayer extends PlayerItemInHandLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    static final float PX = 1.0F / 16.0F;
    /** The finger on the trigger (fist px): tools/gun_anims.py FINGER (where the grip puts the trigger with the gun's pivot at the fist). */
    static final float[] FINGER = {0.0F, 1.96F, 0.21F};
    /** The left hand's fingertips (fist px), where it holds the ball (gun_anims.py FINGERTIP); the ball's size there (BALL_SCALE). */
    static final float[] FINGERTIP = {0.0F, 2.2F, -0.6F};
    static final float BALL_IN_HAND = 0.7F * PX;
    /** Players whose muzzle smoke for the shot under way was let out already (entity id -> the shot's start tick). */
    private static final Map<Integer, Integer> SMOKED = new HashMap<>();

    public GunLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent, null);
    }

    @Override
    public void m_6494_(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                        float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        try {
            Gun gun = GunClient.gun(player.m_19879_());
            if (gun == null || player.m_20145_() || FirstPersonMode.isFirstPersonPass()) {
                return;
            }
            draw(pose, buffers, light, player, gun, partialTick);
        } catch (RuntimeException failure) {
            // a gun that cannot be drawn never stops the player from being drawn
        }
    }

    void draw(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, Gun gun, float partial) {
        PlayerModel<AbstractClientPlayer> model = this.m_117386_();
        int anim = GunClient.anim(player.m_19879_());
        float ticks = GunClient.ticks(player, partial);
        ModifierLayer<IAnimation> layer = GunClient.existing(player);
        pose.m_85836_();
        hand(pose, player, model, layer, partial);
        GunRenderer.draw(pose, buffers, gun, light);
        float into = GunAnims.ballInMuzzle(anim, ticks);
        if (into >= 0.0F) {
            // the ball pressed into the muzzle: at its mouth, sinking in
            pose.m_85836_();
            pose.m_85841_(Gun.SCALE, Gun.SCALE, Gun.SCALE);
            pose.m_252880_(GunRenderer.MUZZLE[0], GunRenderer.MUZZLE[1], GunRenderer.MUZZLE[2] + 1.3F * into);
            GunRenderer.ball(pose, buffers, light, 1.0F, false);
            pose.m_85849_();
        }
        if (GunAnims.firing(anim) && ticks >= GunRules.AIM && ticks < GunRules.AIM + 2.0F) {
            flash(pose, buffers, (ticks - GunRules.AIM) / 2.0F);
            smoke(pose, player, anim);
        }
        pose.m_85849_();
        if (GunAnims.ballInHand(anim, ticks)) {
            pose.m_85836_();
            leftFist(pose, player, model, partial);
            pose.m_252880_(FINGERTIP[0] * PX, FINGERTIP[1] * PX, FINGERTIP[2] * PX);
            GunRenderer.ball(pose, buffers, light, BALL_IN_HAND, false);
            pose.m_85849_();
        }
    }

    /** The gun's frame (model px are scaled by GunRenderer) in the right hand: tools/gun_anims.py gun_matrix. */
    static void hand(PoseStack pose, AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model, ModifierLayer<IAnimation> layer,
                     float partial) {
        model.f_102811_.m_104299_(pose);
        float[] forearm = SwordLayer.bend(player, "rightArm");
        SwordLayer.bendAbout(pose, 0.25F, forearm[0], forearm[1]);
        pose.m_252880_(("slim".equals(player.m_108564_()) ? -0.5F : -1.0F) * PX, 8.5F * PX, 0.0F);
        Vec3f move = SwordLayer.channel(layer, "rightItem", TransformType.POSITION, partial);
        Vec3f turn = SwordLayer.channel(layer, "rightItem", TransformType.ROTATION, partial);
        pose.m_252880_((FINGER[0] + move.getX().floatValue()) * PX, (FINGER[1] + move.getY().floatValue()) * PX,
            (FINGER[2] + move.getZ().floatValue()) * PX);
        pose.m_252781_(Axis.f_252403_.m_252977_((float) Math.toDegrees(turn.getZ().floatValue())));
        pose.m_252781_(Axis.f_252436_.m_252977_((float) Math.toDegrees(turn.getY().floatValue())));
        pose.m_252781_(Axis.f_252529_.m_252977_((float) Math.toDegrees(turn.getX().floatValue())));
        // the grip: the barrel (model -z) along the fist (+y), the gun's top (+y) to the back of the hand (-z): Ry(180) Rx(90)
        pose.m_252781_(Axis.f_252436_.m_252977_(180.0F));
        pose.m_252781_(Axis.f_252529_.m_252977_(90.0F));
        pose.m_252880_(-GunRenderer.TRIGGER[0] * Gun.SCALE, -GunRenderer.TRIGGER[1] * Gun.SCALE, -GunRenderer.TRIGGER[2] * Gun.SCALE);
    }

    /** The left fist's centre frame (px scaled to blocks): the left arm part, its forearm bend, the fist. */
    static void leftFist(PoseStack pose, AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model, float partial) {
        model.f_102812_.m_104299_(pose);
        float[] forearm = SwordLayer.bend(player, "leftArm");
        SwordLayer.bendAbout(pose, 0.25F, forearm[0], forearm[1]);
        pose.m_252880_(("slim".equals(player.m_108564_()) ? 0.5F : 1.0F) * PX, 8.5F * PX, 0.0F);
    }

    /** The muzzle flash ({@code age} 0 to 1 over its two ticks): a star of flame out of the muzzle, a white core, fading. */
    static void flash(PoseStack pose, MultiBufferSource buffers, float age) {
        pose.m_85836_();
        pose.m_85841_(Gun.SCALE, Gun.SCALE, Gun.SCALE);
        pose.m_252880_(GunRenderer.MUZZLE[0], GunRenderer.MUZZLE[1], GunRenderer.MUZZLE[2]);
        Matrix4f m = pose.m_85850_().m_252922_();
        VertexConsumer out = buffers.m_6299_(RenderType.m_269166_());
        float fade = 1.0F - age;
        float reach = 7.0F * (0.6F + 0.8F * age);
        float wide = 2.4F * (1.0F - 0.4F * age);
        for (int k = 0; k < 4; k++) {
            float a = (float) (Math.PI * k / 4.0);
            float cx = (float) Math.cos(a) * wide;
            float cy = (float) Math.sin(a) * wide;
            // a flame petal: from the muzzle's rim out along the barrel to a point, both faces
            quad(out, m, -cx, -cy, 0.0F, cx, cy, 0.0F, cx * 0.3F, cy * 0.3F, -reach, -cx * 0.3F, -cy * 0.3F, -reach, 0xFFC860, 0xFF7A20, fade);
        }
        float core = 1.6F * (1.0F - 0.5F * age);
        quad(out, m, -core, -core, -0.2F, core, -core, -0.2F, core, core, -0.2F, -core, core, -0.2F, 0xFFFFF0, 0xFFE090, fade);
        pose.m_85849_();
    }

    static void quad(VertexConsumer out, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2,
                     float z2, float x3, float y3, float z3, int near, int far, float alpha) {
        float[][] p = {{x0, y0, z0}, {x1, y1, z1}, {x2, y2, z2}, {x3, y3, z3}};
        for (int pass = 0; pass < 2; pass++) {
            for (int k = 0; k < 4; k++) {
                int v = pass == 0 ? k : 3 - k;
                int c = v < 2 ? near : far;
                float a = alpha * (v < 2 ? 0.95F : 0.0F);
                out.m_252986_(m, p[v][0], p[v][1], p[v][2]).m_85950_(((c >> 16) & 255) / 255.0F, ((c >> 8) & 255) / 255.0F,
                    (c & 255) / 255.0F, a).m_5752_();
            }
        }
    }

    /** Once a shot: a puff of the game's own cloud out of the muzzle, where this frame draws it. */
    static void smoke(PoseStack pose, AbstractClientPlayer player, int anim) {
        int start = (int) (player.m_9236_().m_46467_() & 0x3FFFFFFF) - GunRules.AIM;
        Integer was = SMOKED.get(player.m_19879_());
        if (was != null && Math.abs(was - start) < 4) {
            return;
        }
        SMOKED.put(player.m_19879_(), start);
        pose.m_85836_();
        pose.m_85841_(Gun.SCALE, Gun.SCALE, Gun.SCALE);
        Matrix4f m = pose.m_85850_().m_252922_();
        double[] at = SwordFx.world(m, GunRenderer.MUZZLE[0], GunRenderer.MUZZLE[1], GunRenderer.MUZZLE[2]);
        double[] ahead = SwordFx.world(m, GunRenderer.MUZZLE[0], GunRenderer.MUZZLE[1], GunRenderer.MUZZLE[2] - 8.0F);
        pose.m_85849_();
        GunBullets.muzzleSmoke(at, new double[] {ahead[0] - at[0], ahead[1] - at[1], ahead[2] - at[2]});
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
                    renderer.m_115326_(new GunLayer(renderer));
                }
            }
        }
    }
}
