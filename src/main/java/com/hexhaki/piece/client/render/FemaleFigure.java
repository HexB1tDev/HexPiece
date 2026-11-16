package com.hexhaki.piece.client.render;

import com.hexhaki.piece.FemaleBody;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 2.1.1: the female body ({@link FemaleBody}) drawn on a humanoid model, ported from HexGhoul's: wider, deeper legs, a wide-armed skin's
 * arms slim, the bust (a wedge on the torso's front painted with the skin's own torso, the jacket layer over it) bouncing as she moves.
 */
public final class FemaleFigure {
    private static final Map<Integer, double[]> JIGGLES = new HashMap<>();

    private FemaleFigure() {
    }

    static ModelPart[] legs(HumanoidModel<?> model) {
        if (model instanceof PlayerModel<?> player) {
            return new ModelPart[] {model.f_102813_, model.f_102814_, player.f_103377_, player.f_103376_};
        }
        return new ModelPart[] {model.f_102813_, model.f_102814_, null, null};
    }

    static ModelPart[] arms(HumanoidModel<?> model) {
        if (model instanceof PlayerModel<?> player) {
            return new ModelPart[] {model.f_102811_, model.f_102812_, player.f_103375_, player.f_103374_};
        }
        return new ModelPart[] {model.f_102811_, model.f_102812_, null, null};
    }

    static void shape(HumanoidModel<?> model, boolean slimArms) {
        for (ModelPart leg : legs(model)) {
            if (leg != null) {
                leg.f_233553_ *= FemaleBody.HIPS;
                leg.f_233555_ *= FemaleBody.THIGHS;
            }
        }
        if (slimArms) {
            for (ModelPart arm : arms(model)) {
                if (arm != null) {
                    arm.f_233553_ *= FemaleBody.ARMS;
                }
            }
        }
    }

    static void unshape(HumanoidModel<?> model, boolean slimArms) {
        for (ModelPart leg : legs(model)) {
            if (leg != null) {
                leg.f_233553_ /= FemaleBody.HIPS;
                leg.f_233555_ /= FemaleBody.THIGHS;
            }
        }
        if (slimArms) {
            for (ModelPart arm : arms(model)) {
                if (arm != null) {
                    arm.f_233553_ /= FemaleBody.ARMS;
                }
            }
        }
    }

    /** The bust on {@code torso} as it is posed, its tip {@code bounce} pixels up; {@code jacket}: the skin's second layer over it. */
    public static void bust(ModelPart torso, PoseStack pose, VertexConsumer consumer, int light, float bounce, boolean jacket) {
        pose.m_85836_();
        torso.m_104299_(pose);
        Matrix4f m = pose.m_85850_().m_252922_();
        Matrix3f n = pose.m_85850_().m_252943_();
        float grow = jacket ? 0.25F : 0.0F;
        float x0 = -4.0F - grow;
        float x1 = 4.0F + grow;
        float top = FemaleBody.BUST_TOP;
        float tip = FemaleBody.BUST_TIP - bounce;
        float bottom = FemaleBody.BUST_BOTTOM;
        float front = FemaleBody.BUST_FRONT - grow;
        float out = FemaleBody.BUST_OUT - grow;
        float v0 = jacket ? 36.0F : 20.0F;
        quad(consumer, m, n, light, v0, new float[][] {{x0, top, front}, {x1, top, front}, {x1, tip, out}, {x0, tip, out}}, 0.0F, -0.8F, -1.0F);
        quad(consumer, m, n, light, v0, new float[][] {{x0, tip, out}, {x1, tip, out}, {x1, bottom, front}, {x0, bottom, front}}, 0.0F, 0.6F, -1.0F);
        for (float x : new float[] {x0, x1}) {
            quad(consumer, m, n, light, v0, new float[][] {{x, top, front}, {x, tip, out}, {x, bottom, front}, {x, bottom, front}}, Math.signum(x),
                0.0F, 0.0F);
        }
        pose.m_85849_();
    }

    private static void quad(VertexConsumer consumer, Matrix4f m, Matrix3f n, int light, float v0, float[][] corners, float nx, float ny,
                             float nz) {
        for (float[] c : corners) {
            float across = Math.max(-4.0F, Math.min(4.0F, c[0]));
            float u = (24.0F + across - (nx != 0.0F ? Math.signum(across) * 0.25F : 0.0F)) / 64.0F;
            float v = (v0 + c[1]) / 64.0F;
            consumer.m_252986_(m, c[0] / 16.0F, c[1] / 16.0F, c[2] / 16.0F).m_6122_(255, 255, 255, 255).m_7421_(u, v)
                .m_86008_(OverlayTexture.f_118083_).m_85969_(light).m_252939_(n, nx, ny, nz).m_5752_();
        }
    }

    /** How far (model pixels, up) the bust of body {@code id} is off its rest, the body at height {@code y}, the clock in ticks. */
    public static float jiggle(int id, double y, double clock) {
        double[] state = JIGGLES.get(id);
        if (state == null) {
            JIGGLES.put(id, new double[] {0.0, 0.0, y, clock});
            return 0.0F;
        }
        if (clock > state[3]) {
            FemaleBody.jiggle(state, y - state[2], clock - state[3]);
            state[2] = y;
            state[3] = clock;
        }
        if (JIGGLES.size() > 512) {
            JIGGLES.clear();
        }
        return (float) (state[0] * 16.0);
    }
}
