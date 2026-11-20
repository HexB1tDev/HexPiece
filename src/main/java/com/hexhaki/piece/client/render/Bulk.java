package com.hexhaki.piece.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 2.7.4 (the owner: "Smooth out the mod to avoid lag"): a mesh's vertices written into the game's entity buffers one call each (the
 * buffer's own fast path for whole vertices), the pose multiplied in here once per mesh, instead of the seven calls a vertex (each its
 * own matrix multiply and format checks) of the usual chain. Same vertices, same render types.
 */
public final class Bulk {
    private Bulk() {
    }

    /**
     * The pose's point matrix (3 x 4, row-major) and normal matrix (3 x 3) into {@code m} and {@code n}, read through the calls the
     * original makes (a direction for each axis, the point at the origin).
     */
    public static void read(PoseStack.Pose pose, float[] m, float[] n) {
        Matrix4f view = pose.m_252922_();
        Matrix3f turn = pose.m_252943_();
        Vector3f o = view.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
        Vector3f ax = view.transformDirection(new Vector3f(1.0F, 0.0F, 0.0F));
        Vector3f ay = view.transformDirection(new Vector3f(0.0F, 1.0F, 0.0F));
        Vector3f az = view.transformDirection(new Vector3f(0.0F, 0.0F, 1.0F));
        m[0] = ax.x;
        m[1] = ay.x;
        m[2] = az.x;
        m[3] = o.x;
        m[4] = ax.y;
        m[5] = ay.y;
        m[6] = az.y;
        m[7] = o.y;
        m[8] = ax.z;
        m[9] = ay.z;
        m[10] = az.z;
        m[11] = o.z;
        Vector3f nx = turn.transform(new Vector3f(1.0F, 0.0F, 0.0F));
        Vector3f ny = turn.transform(new Vector3f(0.0F, 1.0F, 0.0F));
        Vector3f nz = turn.transform(new Vector3f(0.0F, 0.0F, 1.0F));
        n[0] = nx.x;
        n[1] = ny.x;
        n[2] = nz.x;
        n[3] = nx.y;
        n[4] = ny.y;
        n[5] = nz.y;
        n[6] = nx.z;
        n[7] = ny.z;
        n[8] = nz.z;
    }

    /** One whole vertex through {@code m} and {@code n} (see {@link #read}). */
    public static void vertex(VertexConsumer out, float[] m, float[] n, float x, float y, float z, float r, float g, float b, float a, float u,
                              float v, int overlay, int light, float nx, float ny, float nz) {
        out.m_5954_(m[0] * x + m[1] * y + m[2] * z + m[3], m[4] * x + m[5] * y + m[6] * z + m[7], m[8] * x + m[9] * y + m[10] * z + m[11],
            r, g, b, a, u, v, overlay, light, n[0] * nx + n[1] * ny + n[2] * nz, n[3] * nx + n[4] * ny + n[5] * nz,
            n[6] * nx + n[7] * ny + n[8] * nz);
    }
}
