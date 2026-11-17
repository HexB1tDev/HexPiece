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

/**
 * 2.1.3: Ryou's hands (the owner: "Ryou should look like blackened hands with a strong, clean glow outlining their shape. Keep the black
 * coating readable beneath the coloured rim. Concentrate the effect around the hands and their silhouette, with a restrained transition
 * into the wrist. Enhance the outline's brightness, definition and subtle pulsing instead of covering the arms with red effects ... The
 * outline must follow the animated hand geometry in first and third person, respect depth and remain attractive without requiring a
 * shader pack. Prevent flickering, detached outlines and glow leaking through walls").
 * <p>
 * Called where the original's hand layer drew Advanced Haki ({@code HakiHandLayer.render} and {@code renderFirstPerson}, through
 * {@code patch/PiecePatch.java}): the coating is drawn plain black ({@link #coating}), the arm aura is left off while Ryou is up
 * ({@link #armAura}), the fist flames are gone ({@link #flames}) and the fist glow is the rim ({@link #glow}).
 * <p>
 * The rim is an inverted hull: a box a little larger than the hand (the lowest four pixels of the arm, fist and wrist) whose faces are
 * wound inward, so with back faces culled only its far side is drawn, and the hand itself (drawn first, writing depth) hides all of it
 * but a clean band around the silhouette: first a dark band ({@code entityTranslucentCull}, black at 85%, so the colour reads on a
 * bright sky too), then the rim's light added over it and a faint halo falling off round it ({@code eyes}: full bright, additive, no depth written);
 * all depth tested, so walls hide it like anything else; the side faces fade to nothing up the wrist; a slow pulse; a flare
 * when Ryou comes on ({@link RyouFx#flare}). It is posed by the arm's own transform and bent at the elbow exactly as bendy-lib bends
 * the arm itself (playerAnimator's bend for that arm, {@link #bent}), so it follows every animation and never comes off the hand.
 */
public final class RyouHands {
    static final ResourceLocation WHITE = ResourceLocation.m_214293_("hexhaki", "textures/piece/white.png");
    /** Model pixels in blocks. */
    static final float PX = 1.0F / 16.0F;
    /** The hand: from the wrist to the end of the fist (arm space, blocks). */
    static final float WRIST = 6.5F * PX;
    static final float LIT = 7.7F * PX;
    static final float FIST = 10.0F * PX;
    static final float RIM = 0.55F * PX;
    /** The coat: just outside the hand and the sleeve's outer layer (a quarter pixel out), and where its fade up the wrist begins. */
    static final float COAT = 0.35F * PX;
    static final float COAT_TOP = 5.4F * PX;
    /**
     * The halo: three hulls ever wider and fainter; their light adds up (no depth is written), so it falls off in steps from the rim
     * outward instead of standing round the hand as one flat band.
     */
    static final float[] HALOS = {1.0F * PX, 1.45F * PX, 1.9F * PX};
    static final float[] HALO_STRENGTHS = {0.13F, 0.08F, 0.05F};
    /** How dark the band under the rim's light is (alpha). */
    static final float DARK = 0.85F;
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
        if (!arm.f_104207_) {
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

    /** The rim round one hand, in the player's own Ryou colour. */
    public static void glow(PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, ModelPart arm, float age, boolean slim,
                            boolean right) {
        if (!arm.f_104207_) {
            return;
        }
        int color = RyouColors.orNeutral(player.m_19879_());
        float pulse = pulse(age, player.m_19879_()) + RyouFx.flare(player.m_19879_());
        float[] bend = bend(player, right);
        pose.m_85836_();
        arm.m_104299_(pose);
        PoseStack.Pose last = pose.m_85850_();
        float half = slim ? 1.5F * PX : 2.0F * PX;
        float centre = (slim ? 0.5F : 1.0F) * PX * (right ? -1.0F : 1.0F);
        float[] box = {centre / PX - half / PX, centre / PX + half / PX};
        // first the rim's dark band (alpha blended: black stays black under any light), then its light added over it, then the halo:
        // the band keeps the colour itself on a bright sky, where light added alone would wash out to white
        shell(last, buffers.m_6299_(RenderType.m_110470_(WHITE)), bend, box, centre, half, RIM, 0, DARK * pulse);
        // 2.1.9: the light alpha blended, not additive (some setups draw additive geometry opaque: dark boxes round the hands)
        VertexConsumer light = buffers.m_6299_(RenderType.m_269166_());
        shell(last, light, bend, box, centre, half, RIM, color, pulse);
        for (int i = 0; i < HALOS.length; i++) {
            shell(last, light, bend, box, centre, half, HALOS[i], color, pulse * HALO_STRENGTHS[i]);
        }
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

    /** The hull round the hand, {@code grow} wider than it on every side, its sides fading out up the wrist. */
    static void shell(PoseStack.Pose pose, VertexConsumer buffer, float[] bend, float[] box, float centre, float half, float grow, int color, float strength) {
        float x0 = centre - half - grow;
        float x1 = centre + half + grow;
        float z0 = -2.0F * PX - grow;
        float z1 = 2.0F * PX + grow;
        float y1 = FIST + grow;
        // the four sides in two bands each: the wrist band fades in, the hand band is lit; then the end of the fist
        float[][] corners = {{x0, z0}, {x1, z0}, {x1, z1}, {x0, z1}};
        float[][] normals = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        for (int side = 0; side < 4; side++) {
            float[] a = corners[side];
            float[] b = corners[(side + 1) % 4];
            float nx = normals[side][0];
            float nz = normals[side][1];
            band(pose, buffer, bend, box, a, b, WRIST, LIT, 0.0F, 1.0F, nx, nz, color, strength);
            band(pose, buffer, bend, box, a, b, LIT, y1, 1.0F, 1.0F, nx, nz, color, strength);
        }
        float[][] end = {{x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}};
        quad(pose, buffer, bend, box, end, new float[] {1, 1, 1, 1}, 0, 1, 0, color, strength);
    }

    /** One band of a side, from {@code top} to {@code bottom} (lit {@code litTop} to {@code litBottom}). */
    static void band(PoseStack.Pose pose, VertexConsumer buffer, float[] bend, float[] box, float[] a, float[] b, float top, float bottom, float litTop,
                     float litBottom, float nx, float nz, int color, float strength) {
        float[][] points = {{a[0], top, a[1]}, {b[0], top, b[1]}, {b[0], bottom, b[1]}, {a[0], bottom, a[1]}};
        quad(pose, buffer, bend, box, points, new float[] {litTop, litTop, litBottom, litBottom}, nx, 0, nz, color, strength);
    }

    /**
     * A face of the hull, wound so it faces INTO the hull (an inverted hull: culled from outside, drawn from inside), whatever order its
     * corners came in: the corners are reversed when their winding faces along the outward normal.
     */
    static void quad(PoseStack.Pose pose, VertexConsumer buffer, float[] bend, float[] box, float[][] points, float[] lit, float nx, float ny, float nz,
                     int color, float strength) {
        float[] e1 = {points[1][0] - points[0][0], points[1][1] - points[0][1], points[1][2] - points[0][2]};
        float[] e2 = {points[2][0] - points[0][0], points[2][1] - points[0][1], points[2][2] - points[0][2]};
        float cx = e1[1] * e2[2] - e1[2] * e2[1];
        float cy = e1[2] * e2[0] - e1[0] * e2[2];
        float cz = e1[0] * e2[1] - e1[1] * e2[0];
        boolean outward = cx * nx + cy * ny + cz * nz > 0;
        Matrix4f matrix = pose.m_252922_();
        Matrix3f normal = pose.m_252943_();
        float r = ((color >> 16) & 255) / 255.0F;
        float g = ((color >> 8) & 255) / 255.0F;
        float bl = (color & 255) / 255.0F;
        for (int i = 0; i < 4; i++) {
            int k = outward ? 3 - i : i;
            float[] p = bent(bend, box[0], box[1], points[k][0], points[k][1], points[k][2]);
            float light = Math.min(1.0F, strength * lit[k]);
            if (color != 0) {
                // the light: its colour at full brightness, as opaque as it is strong (position and colour only: debugQuads)
                buffer.m_252986_(matrix, p[0], p[1], p[2]).m_85950_(r, g, bl, Math.min(0.95F, light)).m_5752_();
                continue;
            }
            // the dark band: black, its strength the alpha
            buffer.m_252986_(matrix, p[0], p[1], p[2]).m_85950_(0.0F, 0.0F, 0.0F, light).m_7421_(k == 1 || k == 2 ? 1 : 0,
                k >= 2 ? 1 : 0).m_86008_(OverlayTexture.f_118083_).m_85969_(FULL_BRIGHT).m_252939_(normal, -nx, -ny, -nz).m_5752_();
        }
    }
}
