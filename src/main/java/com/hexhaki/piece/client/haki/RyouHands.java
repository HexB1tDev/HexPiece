package com.hexhaki.piece.client.haki;

import com.hexhaki.client.render.HakiHandLayer;
import com.hexhaki.piece.haki.ArmBend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
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
        coating(pose, buffers, light, arm, player, right, bend(player, right));
    }

    /**
     * 2.5.4: the coat on the game's own first-person hand: straight, as that arm is drawn ({@link #glowFirstPerson}).
     * <p>
     * 2.5.8 (the owner, with a screenshot of the first-person view, Haki on: only the purple aura's outline where the arm should be,
     * "another bug when haki is on"): the original's first-person Armament ({@code HakiRenderHooks.ForgeBus.arm}) cancels the game's
     * arm and draws none of its own: its coat was a whole black arm. Since 2.3.4 the coat is only the hands and is left off where the
     * hands are blackened in the skin itself (HakiSkin), so nothing drew the arm at all. The arm is drawn here first, by the game's own
     * hand renderer (the player's skin, blackened through the original's skin hook; its sleeve), then the coat where the skin could
     * not be blackened, then the original's aura and glow over it.
     */
    public static void coatingFirstPerson(PoseStack pose, MultiBufferSource buffers, int light, ModelPart arm, float mastery, boolean acoc,
                                          AbstractClientPlayer player, boolean right) {
        if (Minecraft.m_91087_().m_91290_().m_114382_(player) instanceof PlayerRenderer renderer) {
            if (right) {
                renderer.m_117770_(pose, buffers, light, player);
            } else {
                renderer.m_117813_(pose, buffers, light, player);
            }
        }
        coating(pose, buffers, light, arm, player, right, new float[] {0.0F, 0.0F});
    }

    static void coating(PoseStack pose, MultiBufferSource buffers, int light, ModelPart arm, AbstractClientPlayer player, boolean right,
                        float[] bend) {
        // 2.3.4 (the owner: "it's not the actual skin blackening"): the hands are blackened in the skin itself (HakiSkin); this coat
        // is only drawn where that copy could not be made
        if (!arm.f_104207_ || HakiSkin.blackened(player)) {
            return;
        }
        boolean slim = "slim".equals(player.m_108564_());
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
     * The aura round one hand, in the player's own Ryou colour. 2.4.7 - 2.5.2 handed the fist's box to a post-chain outline (RyouGlow:
     * it flickered on the owner's device and hid the release's lightning). 2.5.3 (the owner, with the picture
     * docs/handoff/ryou-fist-target.jpg: "this is the fucking target look"): the aura is geometry hugging the fist's outline as the eye
     * sees it ({@link com.hexhaki.piece.haki.FistAura}: a hot rim, a bright band, a soft flame-edged haze trailing up the arm, sparks),
     * bent with the elbow as the arm is, drawn by {@link RyouAura}. Breathing slowly, flaring as Ryou comes on, more sparks in a punch.
     */
    public static void glow(PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, ModelPart arm, float age, boolean slim,
                            boolean right) {
        glow(pose, buffers, player, arm, age, slim, right, bend(player, right));
    }

    /**
     * 2.5.4: the aura on the game's own first-person hand (the original's {@code HakiHandLayer.renderFirstPerson}, PiecePatch
     * CALL_REDIRECTS): that arm is drawn straight, whatever animation plays (playerAnimator leaves the first-person hand alone), so its
     * aura is not bent with the animation's elbow (the swordsman's stance bends the left one about 50 degrees, and the hand resting on
     * the holstered sword, client/sword/HolsterHand, is that arm).
     */
    public static void glowFirstPerson(PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, ModelPart arm, float age,
                                       boolean slim, boolean right) {
        glow(pose, buffers, player, arm, age, slim, right, new float[] {0.0F, 0.0F});
    }

    static void glow(PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, ModelPart arm, float age, boolean slim,
                     boolean right, float[] bend) {
        if (!arm.f_104207_) {
            return;
        }
        int id = player.m_19879_();
        int color = RyouColors.orNeutral(id);
        float strength = Math.min(1.0F, pulse(age, id) + RyouFx.flare(id));
        float punch = player.m_21324_(net.minecraft.client.Minecraft.m_91087_().m_91296_());
        pose.m_85836_();
        arm.m_104299_(pose);
        RyouAura.draw(pose.m_85850_(), buffers, bend[0], bend[1], slim, right, age, id * 31 + (right ? 1 : 2), color, strength,
            punch);
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
