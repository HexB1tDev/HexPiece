package com.hexhaki.piece.client.haki;

import com.hexhaki.client.render.HakiHandLayer;
import com.hexhaki.piece.haki.ArmBend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 2.1.3: Ryou's hands (the owner: "Ryou should look like blackened hands with a strong, clean glow outlining their shape. Keep the black
 * coating readable beneath the coloured rim. Concentrate the effect around the hands and their silhouette, with a restrained transition
 * into the wrist. Enhance the outline's brightness, definition and subtle pulsing instead of covering the arms with red effects ... The
 * outline must follow the animated hand geometry in first and third person, respect depth and remain attractive without requiring a
 * shader pack. Prevent flickering, detached outlines and glow leaking through walls").
 * <p>
 * Called where the original's hand layer drew Advanced Haki ({@code HakiHandLayer.render} and {@code renderFirstPerson}, through
 * {@code patch/PiecePatch.java}): the coating is drawn plain black ({@link #coating}), the arm aura is left off while Ryou is up
 * ({@link #armAura}), the fist flames are gone ({@link #flames}) and the fist glow is the game's own glowing outline ({@link #glow},
 * 2.4.7).
 * <p>
 * 2.3.3: the outline lies on the black coat: a band in the player's colour along every edge of every face of the hand, fading up the
 * wrist (see {@link #glow}; the inverted hull it replaced drew solid boxes on the owner's setup); a slow pulse; a flare when Ryou
 * comes on ({@link RyouFx#flare}). Coat and outline are posed by the arm's own transform and bent at the elbow exactly as bendy-lib
 * bends the arm itself (playerAnimator's bend for that arm, {@link #bent}), so they follow every animation and never come off the hand.
 */
public final class RyouHands {
    static final ResourceLocation WHITE = ResourceLocation.m_214293_("hexhaki", "textures/piece/white.png");
    /** Model pixels in blocks. */
    static final float PX = 1.0F / 16.0F;
    /** The hand: from the wrist to the end of the fist (arm space, blocks). */
    static final float WRIST = 6.5F * PX;
    static final float FIST = 10.0F * PX;
    /** The coat: just outside the hand and the sleeve's outer layer (a quarter pixel out), and where its fade up the wrist begins. */
    static final float COAT = 0.35F * PX;
    static final float COAT_TOP = 5.4F * PX;
    static final int FULL_BRIGHT = 0xF000F0;

    private RyouHands() {
    }

    /**
     * The coating: black, over the HAND only (2.1.9, the owner: "The whole arm is black instead of just the hands"; the original's coating
     * covered the whole arm). A shell a little outside the hand and the sleeve's outer layer, from the wrist to the end of the fist, its
     * top fading out up the wrist so it never ends in a hard line; bent with the elbow as the arm is.
     */
    public static void coating(PoseStack pose, MultiBufferSource buffers, int light, ModelPart arm, float mastery, boolean acoc,
                               AbstractClientPlayer player, boolean right) {
        // 2.3.4 (the owner: "it's not the actual skin blackening"): the hands are blackened in the skin itself (HakiSkin); this coat
        // is only drawn where that copy could not be made
        if (!arm.f_104207_ || HakiSkin.blackened(player)) {
            return;
        }
        boolean slim = "slim".equals(player.m_108564_());
        float[] bend = bend(player, right);
        pose.m_85836_();
        arm.m_104299_(pose);
        PoseStack.Pose last = pose.m_85850_();
        float half = slim ? 1.5F * PX : 2.0F * PX;
        float centre = (slim ? 0.5F : 1.0F) * PX * (right ? -1.0F : 1.0F);
        float[] box = {centre / PX - half / PX, centre / PX + half / PX};
        VertexConsumer buffer = buffers.m_6299_(RenderType.m_110470_(WHITE));
        float grow = COAT;
        float x0 = centre - half - grow;
        float x1 = centre + half + grow;
        float z0 = -2.0F * PX - grow;
        float z1 = 2.0F * PX + grow;
        float y1 = FIST + grow;
        float[][] corners = {{x0, z0}, {x1, z0}, {x1, z1}, {x0, z1}};
        float[][] normals = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        for (int side = 0; side < 4; side++) {
            float[] a = corners[side];
            float[] b = corners[(side + 1) % 4];
            float nx = normals[side][0];
            float nz = normals[side][1];
            float[][] fade = {{a[0], COAT_TOP, a[1]}, {b[0], COAT_TOP, b[1]}, {b[0], WRIST, b[1]}, {a[0], WRIST, a[1]}};
            coat(last, buffer, bend, box, fade, new float[] {0, 0, 1, 1}, nx, 0, nz, light);
            float[][] hand = {{a[0], WRIST, a[1]}, {b[0], WRIST, b[1]}, {b[0], y1, b[1]}, {a[0], y1, a[1]}};
            coat(last, buffer, bend, box, hand, new float[] {1, 1, 1, 1}, nx, 0, nz, light);
        }
        float[][] end = {{x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}};
        coat(last, buffer, bend, box, end, new float[] {1, 1, 1, 1}, 0, 1, 0, light);
        pose.m_85849_();
    }

    /** A face of the coat, wound to face OUT (seen from outside the hand), black, as opaque as {@code cover} at each corner. */
    static void coat(PoseStack.Pose pose, VertexConsumer buffer, float[] bend, float[] box, float[][] points, float[] cover, float nx, float ny, float nz,
                     int light) {
        float[] e1 = {points[1][0] - points[0][0], points[1][1] - points[0][1], points[1][2] - points[0][2]};
        float[] e2 = {points[2][0] - points[0][0], points[2][1] - points[0][1], points[2][2] - points[0][2]};
        float cx = e1[1] * e2[2] - e1[2] * e2[1];
        float cy = e1[2] * e2[0] - e1[0] * e2[2];
        float cz = e1[0] * e2[1] - e1[1] * e2[0];
        boolean outward = cx * nx + cy * ny + cz * nz > 0;
        Matrix4f matrix = pose.m_252922_();
        Matrix3f normal = pose.m_252943_();
        for (int i = 0; i < 4; i++) {
            int k = outward ? i : 3 - i;
            float[] p = bent(bend, box[0], box[1], points[k][0], points[k][1], points[k][2]);
            buffer.m_252986_(matrix, p[0], p[1], p[2]).m_85950_(0.07F, 0.07F, 0.09F, 0.97F * cover[k]).m_7421_(0.5F, 0.5F)
                .m_86008_(OverlayTexture.f_118083_).m_85969_(light).m_252939_(normal, nx, ny, nz).m_5752_();
        }
    }

    /** The original's arm aura, but not while Ryou is up: Ryou is the hands, not a glow over the arms. */
    public static void armAura(PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, ModelPart arm, float mastery,
                               boolean acoc, float age, boolean slim, boolean right) {
        if (!acoc) {
            HakiHandLayer.renderArmAura(pose, buffers, player, arm, mastery, acoc, age, slim, right);
        }
    }

    /** The original's fist flames: none (the owner: "Remove the fire particles from every Ryou activation, idle and movement effect"). */
    public static void flames(PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, ModelPart arm, float mastery,
                              float age, boolean slim, boolean right) {
    }

    /**
     * The glow round one hand, in the player's own Ryou colour. 2.4.7 (the owner: "I dont want the Ryou haki to be a painted outline
     * ... the glow around the outline is not meant to be literal painted lines, it's meant to be kinda like the minecraft glow effect
     * around the firsts, thats it"): the painted bands along the hand's edges (2.3.3) and the halo discs (2.3.4) are gone; the hand is
     * black in the skin itself ({@link HakiSkin}, spreading pixel by pixel) and glows as the game makes a glowing entity glow: its box
     * (just outside the hand, from the wrist to the end of the fist, bent with the elbow as the arm is) is handed to {@link RyouGlow},
     * which outlines it with the game's own glowing-outline shaders. Breathing slowly, flaring as Ryou comes on.
     */
    public static void glow(PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, ModelPart arm, float age, boolean slim,
                            boolean right) {
        if (!arm.f_104207_) {
            return;
        }
        int color = RyouColors.orNeutral(player.m_19879_());
        float strength = Math.min(1.0F, pulse(age, player.m_19879_()) + RyouFx.flare(player.m_19879_()));
        float[] bend = bend(player, right);
        pose.m_85836_();
        arm.m_104299_(pose);
        float half = slim ? 1.5F * PX : 2.0F * PX;
        float centre = (slim ? 0.5F : 1.0F) * PX * (right ? -1.0F : 1.0F);
        float[] box = {centre / PX - half / PX, centre / PX + half / PX};
        float x0 = centre - half - COAT;
        float x1 = centre + half + COAT;
        float z0 = -2.0F * PX - COAT;
        float z1 = 2.0F * PX + COAT;
        float[][] square = {{x0, z0}, {x1, z0}, {x1, z1}, {x0, z1}};
        float[][] corners = new float[8][];
        for (int i = 0; i < 4; i++) {
            corners[i] = bent(bend, box[0], box[1], square[i][0], WRIST, square[i][1]);
            corners[i + 4] = bent(bend, box[0], box[1], square[i][0], FIST + COAT, square[i][1]);
        }
        RyouGlow.box(pose.m_85850_().m_252922_(), corners, color, strength);
        pose.m_85849_();
    }

    /** 0.82 to 1: a slow breath of light, each player out of step with the others. */
    static float pulse(float age, int entity) {
        return 0.91F + 0.09F * (float) Math.sin(age * 0.17F + entity * 1.7F);
    }

    /** The arm's bend now (axis, angle in radians), as the original reads it; {0, 0} when none. */
    static float[] bend(AbstractClientPlayer player, boolean right) {
        if (player instanceof IAnimatedPlayer animated) {
            AnimationApplier animation = animated.playerAnimator_getAnimation();
            if (animation != null && animation.isActive()) {
                Pair<Float, Float> pair = animation.getBend(right ? "rightArm" : "leftArm");
                if (pair != null) {
                    return new float[] {pair.getLeft(), pair.getRight()};
                }
            }
        }
        return new float[] {0.0F, 0.0F};
    }

    /** A point of the arm (arm space, blocks) where the bent arm has it ({@link ArmBend}). */
    static float[] bent(float[] bend, float minX, float maxX, float x, float y, float z) {
        return ArmBend.bent(bend[0], bend[1], minX, maxX, x, y, z);
    }
}
