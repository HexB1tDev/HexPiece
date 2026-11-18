package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.haki.FistAura;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 2.5.3: draws Ryou's fist aura ({@link FistAura}, built per arm by {@link RyouHands#glow}). It replaces 2.4.7's post-chain glow
 * (RyouGlow), which flickered on the owner's device and whose blit, run in the middle of the level, left the projection flat for
 * everything drawn after it: the Conqueror's release lightning (and the sword slashes, the air and the water) vanished while Ryou was up
 * (the owner: "whenever Ryou haki is activated Haki isn't shooting lightning on Conquerors release").
 * <p>
 * Drawn as {@code RenderType.entityShadow} over the plain white texture: the game's own entity shadow (on every device the game runs
 * on), alpha blended with no cut-off (its shader discards nothing, so the haze fades out smoothly), depth tested, no depth written,
 * unlit (the aura is light). Every quad faces the eye (FistAura winds them), so back-face culling never takes one.
 * <p>
 * In the level, the aura does not write depth, so whatever opaque is drawn after it would paint over it: the quads of every player
 * seen in the level are kept (already through their pose, as the hand layer would have sent them) and drawn once the entities and
 * block entities are drawn ({@code AFTER_BLOCK_ENTITIES}). The first-person hand and the inventory's player are drawn at once, into
 * their own buffers.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class RyouAura {
    private static RenderType type;
    /** The level's auras this frame: positions as the pose put them, colours. */
    private static float[] xyz = new float[12 * 512];
    private static int[] argb = new int[4 * 512];
    private static int quads;
    /** Between the sky and the block entities: the level's entities are being drawn. */
    private static boolean inLevel;
    /** Something in drawing failed: no aura again this session (the hands stay black). */
    private static boolean broken;

    private RyouAura() {
    }

    static RenderType type() {
        if (type == null) {
            type = RenderType.m_110485_(RyouHands.WHITE);
        }
        return type;
    }

    /**
     * One arm's aura, through {@code last} (the arm's pose: arm space to the view), into {@code buffers} (or kept for the end of the
     * level's entities).
     */
    static void draw(PoseStack.Pose last, MultiBufferSource buffers, float bendAxis, float bendAmount, boolean slim, boolean right,
                     float age, int seed, int rgb, float strength, float punch) {
        if (broken) {
            return;
        }
        try {
            Matrix4f pose = last.m_252922_();
            Matrix4f toView = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(pose);
            Matrix4f projection = RenderSystem.getProjectionMatrix();
            // the screen's winding turns round when the arm's frame is mirrored (the inventory's flipped z) or the projection is (the
            // GUI's, y down): both in the inventory, neither in the world or the first-person hand
            boolean mirrored = toView.determinant() < 0.0F != flips(projection);
            Matrix4f back = new Matrix4f(toView).invert();
            double[] view;
            if (orthographic(projection)) {
                Vector3f origin = back.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
                Vector3f toward = back.transformPosition(new Vector3f(0.0F, 0.0F, 1.0F));
                view = new double[] {toward.x - origin.x, toward.y - origin.y, toward.z - origin.z, 0};
            } else {
                Vector3f eye = back.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
                view = new double[] {eye.x, eye.y, eye.z, 1};
            }
            if (inLevel) {
                FistAura.build(bendAxis, bendAmount, slim, right, view, mirrored, age, seed, rgb, strength, punch, (q, c) -> keep(pose, q, c));
            } else {
                VertexConsumer buffer = buffers.m_6299_(type());
                Matrix3f normal = last.m_252943_();
                FistAura.build(bendAxis, bendAmount, slim, right, view, mirrored, age, seed, rgb, strength, punch,
                    (q, c) -> write(buffer, pose, normal, q, c));
            }
        } catch (RuntimeException | LinkageError failure) {
            broken = true;
        }
    }

    /** True when {@code projection} turns the screen over (one of its x and y axes reversed: the GUI's orthographic projection). */
    static boolean flips(Matrix4f projection) {
        Vector3f o = projection.transformProject(new Vector3f(0.0F, 0.0F, -1.5F));
        Vector3f x = projection.transformProject(new Vector3f(1.0F, 0.0F, -1.5F));
        Vector3f y = projection.transformProject(new Vector3f(0.0F, 1.0F, -1.5F));
        return (x.x - o.x) * (y.y - o.y) < 0.0F;
    }

    /** True for an orthographic projection (the inventory): a point's place on the screen does not shrink with its depth. */
    static boolean orthographic(Matrix4f projection) {
        Vector3f near = projection.transformProject(new Vector3f(1.0F, 1.0F, -1.0F));
        Vector3f far = projection.transformProject(new Vector3f(1.0F, 1.0F, -2.0F));
        return Math.abs(near.x - far.x) < 1.0E-5F && Math.abs(near.y - far.y) < 1.0E-5F;
    }

    static void write(VertexConsumer buffer, Matrix4f pose, Matrix3f normal, float[] q, int[] c) {
        for (int k = 0; k < 4; k++) {
            int colour = c[k];
            buffer.m_252986_(pose, q[k * 3], q[k * 3 + 1], q[k * 3 + 2])
                .m_85950_(((colour >> 16) & 255) / 255.0F, ((colour >> 8) & 255) / 255.0F, (colour & 255) / 255.0F, (colour >>> 24) / 255.0F)
                .m_7421_(0.5F, 0.5F).m_86008_(OverlayTexture.f_118083_).m_85969_(RyouHands.FULL_BRIGHT).m_252939_(normal, 0.0F, 1.0F, 0.0F)
                .m_5752_();
        }
    }

    /** A quad kept for the end of the level's entities, through {@code pose} as the buffer would have put it. */
    static void keep(Matrix4f pose, float[] q, int[] c) {
        if ((quads + 1) * 12 > xyz.length) {
            if (quads >= 8192) {
                return;
            }
            xyz = java.util.Arrays.copyOf(xyz, xyz.length * 2);
            argb = java.util.Arrays.copyOf(argb, argb.length * 2);
        }
        Vector3f at = new Vector3f();
        for (int k = 0; k < 4; k++) {
            pose.transformPosition(at.set(q[k * 3], q[k * 3 + 1], q[k * 3 + 2]));
            xyz[quads * 12 + k * 3] = at.x;
            xyz[quads * 12 + k * 3 + 1] = at.y;
            xyz[quads * 12 + k * 3 + 2] = at.z;
            argb[quads * 4 + k] = c[k];
        }
        quads++;
    }

    @SubscribeEvent
    public static void auraStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            quads = 0;
            inLevel = true;
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            inLevel = false;
            if (quads == 0 || broken) {
                quads = 0;
                return;
            }
            try {
                MultiBufferSource.BufferSource source = Minecraft.m_91087_().m_91269_().m_110104_();
                VertexConsumer buffer = source.m_6299_(type());
                Matrix4f identity = new Matrix4f();
                Matrix3f normal = event.getPoseStack().m_85850_().m_252943_(); // unused by the shader
                float[] q = new float[12];
                int[] c = new int[4];
                for (int i = 0; i < quads; i++) {
                    System.arraycopy(xyz, i * 12, q, 0, 12);
                    System.arraycopy(argb, i * 4, c, 0, 4);
                    write(buffer, identity, normal, q, c);
                }
                source.m_109912_(type());
            } catch (RuntimeException | LinkageError failure) {
                broken = true;
            } finally {
                quads = 0;
            }
        }
    }
}
