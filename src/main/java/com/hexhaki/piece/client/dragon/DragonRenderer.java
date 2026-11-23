package com.hexhaki.piece.client.dragon;

import com.hexhaki.piece.HexPiece;
import com.hexhaki.piece.dragon.DragonForm;
import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.DragonPose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 2.7.0: draws a player's dragon (the owner's model, dragon/DragonModel, posed by dragon/DragonForm) in the level, after the entities:
 * every part skinned on the CPU and written straight into the game's own entity buffers (one bulk vertex call each, the place and the
 * view already multiplied in), through the game's own RenderTypes: the body cutout without culling (right from both sides, as the
 * owner's device needs), the fire added full bright. About 10,400 faces; one pose and one set of buffers per dragon, reused.
 */
final class DragonRenderer {
    static final ResourceLocation TEXTURE = ResourceLocation.m_214293_("hexhaki", "textures/piece/dragon/seiryu.png");
    private static DragonModel model;
    private static DragonForm.Shape shape;
    private static boolean failed;
    private static final Map<Integer, Skin> SKINS = new HashMap<>();

    /** One dragon's working pose and skinned parts. */
    static final class Skin {
        final DragonPose pose;
        final float[][] pos;
        final float[][] nrm;
        final float[] anchor = new float[3];
        final float[] shift = new float[3];
        /** 2.7.6: the inside of the mouth and its way out (model units), DragonForm.mouth's. */
        final float[] mouth = new float[6];
        final float[] place = new float[12];

        Skin(DragonModel model) {
            this.pose = new DragonPose(model);
            this.pos = new float[model.parts.length][];
            this.nrm = new float[model.parts.length][];
            for (int i = 0; i < model.parts.length; i++) {
                this.pos[i] = new float[model.parts[i].vertices() * 3];
                this.nrm[i] = new float[model.parts[i].vertices() * 3];
            }
        }
    }

    private DragonRenderer() {
    }

    /** The model, read once from the mod's own jar (null if it could not be). */
    static DragonModel model() {
        if (model == null && !failed) {
            try (InputStream in = DragonRenderer.class.getResourceAsStream("/assets/hexhaki/dragon/seiryu.sry")) {
                if (in == null) {
                    throw new java.io.IOException("assets/hexhaki/dragon/seiryu.sry is missing");
                }
                model = DragonModel.read(in);
                shape = new DragonForm.Shape(model);
            } catch (java.io.IOException | RuntimeException e) {
                failed = true;
                HexPiece.LOGGER.error("HexPiece: the dragon's model could not be read", e);
            }
        }
        return model;
    }

    static void forget(int id) {
        SKINS.remove(id);
    }

    static void clear() {
        SKINS.clear();
    }

    /**
     * One dragon: {@code dx, dy, dz} the player's feet from the camera, the form {@code seconds} into {@code phase}, the hover's clock,
     * the flight (0 to 1), the clock for its wave, its facing, pitch and bank, its light and overlay.
     */
    static void draw(int id, ResourceLocation texture, PoseStack stack, MultiBufferSource.BufferSource buffers, double dx, double dy, double dz,
                     int phase, float seconds,
                     float hover, float flight, float age, float yaw, float pitch, float bank, int light, int overlay, float mouth, float bite,
                     float[] mouthOut) {
        DragonModel m = model();
        if (m == null) {
            return;
        }
        Skin skin = SKINS.computeIfAbsent(id, k -> new Skin(m));
        DragonForm.pose(m, shape, skin.pose, phase, seconds, hover, flight, age, mouth, bite, skin.anchor);
        DragonForm.shift(shape, phase, seconds, skin.shift);
        DragonForm.placement(skin.anchor, skin.shift, DragonForm.grow(phase, seconds), yaw, pitch, bank, skin.place);
        // 2.7.6 (the owner: the fire "coming from inside the mouth not from anywhere else"): the inside of the gape and the mouth's own
        // way out (DragonForm.mouth), placed: where the breath, the charged ball and the blast come from
        if (mouthOut != null && DragonForm.mouth(shape, skin.pose, DragonForm.jawAngle(mouth, bite), skin.mouth)) {
            float[] in = skin.mouth;
            float[] pl = skin.place;
            float wl = 0.0F;
            for (int r = 0; r < 3; r++) {
                mouthOut[r] = pl[r * 4] * in[0] + pl[r * 4 + 1] * in[1] + pl[r * 4 + 2] * in[2] + pl[r * 4 + 3];
                mouthOut[3 + r] = pl[r * 4] * in[3] + pl[r * 4 + 1] * in[4] + pl[r * 4 + 2] * in[5];
                wl += mouthOut[3 + r] * mouthOut[3 + r];
            }
            wl = (float) Math.sqrt(Math.max(1.0E-12F, wl));
            for (int r = 3; r < 6; r++) {
                mouthOut[r] /= wl;
            }
        }
        // the view times the player's place times the dragon's: one 3 x 4 for the points, its rotation for the normals
        Matrix4f view = stack.m_85850_().m_252922_();
        Matrix3f turn = stack.m_85850_().m_252943_();
        float[] p = skin.place;
        float[] world = {p[0], p[1], p[2], (float) (p[3] + dx), p[4], p[5], p[6], (float) (p[7] + dy), p[8], p[9], p[10], (float) (p[11] + dz)};
        // the view's columns, read through the calls the original makes (a direction for each axis, the point at the origin)
        Vector3f o = view.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
        Vector3f ax = view.transformDirection(new Vector3f(1.0F, 0.0F, 0.0F));
        Vector3f ay = view.transformDirection(new Vector3f(0.0F, 1.0F, 0.0F));
        Vector3f az = view.transformDirection(new Vector3f(0.0F, 0.0F, 1.0F));
        float[] v = {ax.x, ay.x, az.x, o.x, ax.y, ay.y, az.y, o.y, ax.z, ay.z, az.z, o.z};
        float[] mat = mul(v, world);
        Vector3f nx = turn.transform(new Vector3f(1.0F, 0.0F, 0.0F));
        Vector3f ny = turn.transform(new Vector3f(0.0F, 1.0F, 0.0F));
        Vector3f nz = turn.transform(new Vector3f(0.0F, 0.0F, 1.0F));
        float[] n = {nx.x, ny.x, nz.x, nx.y, ny.y, nz.y, nx.z, ny.z, nz.z};
        float g = DragonForm.grow(phase, seconds) * DragonForm.SCALE;
        // 3.4.8 (the owner: "optimize the mod as much as you can"): a dragon wholly outside the view (behind the eye, off to a side) is
        // neither skinned nor sent (the skinning is the dragon's whole per-frame cost); the mouth above is placed all the same (the
        // breath comes from it). Its bounding sphere: every vertex's farthest reach from the model's origin, scaled, with room for the
        // pose's swing.
        Matrix4f inverse = new Matrix4f(view).invert();
        Vector3f eye = inverse.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
        Vector3f ahead = inverse.transformPosition(new Vector3f(0.0F, 0.0F, -1.0F));
        double fx = ahead.x - eye.x;
        double fy = ahead.y - eye.y;
        double fz = ahead.z - eye.z;
        double fl = Math.max(1.0E-9, Math.sqrt(fx * fx + fy * fy + fz * fz));
        if (!com.hexhaki.piece.haki.BoltMesh.seen(world[3], world[7], world[11], extent(m) * g * 1.3 + 2.0, fx / fl, fy / fl, fz / fl)) {
            return;
        }
        float[] nm = new float[9];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                nm[r * 3 + c] = (n[r * 3] * p[c] + n[r * 3 + 1] * p[4 + c] + n[r * 3 + 2] * p[8 + c]) / g;
            }
        }
        RenderType body = RenderType.m_110458_(texture);
        VertexConsumer out = buffers.m_6299_(body);
        for (int i = 0; i < m.parts.length; i++) {
            DragonModel.Part part = m.parts[i];
            skin.pose.skin(part, skin.pos[i], skin.nrm[i]);
            if (part.kind != DragonModel.GLOW) {
                emit(out, part, skin.pos[i], skin.nrm[i], mat, nm, light, overlay);
            }
        }
        buffers.m_109912_(body);
        RenderType fire = RenderType.m_110488_(texture);
        VertexConsumer glow = buffers.m_6299_(fire);
        for (int i = 0; i < m.parts.length; i++) {
            if (m.parts[i].kind == DragonModel.GLOW) {
                emit(glow, m.parts[i], skin.pos[i], skin.nrm[i], mat, nm, 0xF000F0, overlay);
            }
        }
        buffers.m_109912_(fire);
    }

    private static float extent = -1.0F;

    /** 3.4.8: the farthest any of the model's vertices lies from its origin (model units), worked out once. */
    static float extent(DragonModel m) {
        if (extent < 0.0F) {
            float most = 0.0F;
            for (DragonModel.Part part : m.parts) {
                for (int v = 0; v < part.vertices(); v++) {
                    float x = part.pos[v * 3];
                    float y = part.pos[v * 3 + 1];
                    float z = part.pos[v * 3 + 2];
                    most = Math.max(most, (float) Math.sqrt(x * x + y * y + z * z));
                }
            }
            extent = most;
        }
        return extent;
    }

    /** a x b, both 3 x 4 affine row-major. */
    static float[] mul(float[] a, float[] b) {
        float[] o = new float[12];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 4; c++) {
                o[r * 4 + c] = a[r * 4] * b[c] + a[r * 4 + 1] * b[4 + c] + a[r * 4 + 2] * b[8 + c] + (c == 3 ? a[r * 4 + 3] : 0.0F);
            }
        }
        return o;
    }

    static void emit(VertexConsumer out, DragonModel.Part part, float[] pos, float[] nrm, float[] m, float[] n, int light, int overlay) {
        int[] q = part.quads;
        float[] uv = part.uv;
        for (int i = 0; i < q.length; i++) {
            int k = q[i];
            float x = pos[k * 3];
            float y = pos[k * 3 + 1];
            float z = pos[k * 3 + 2];
            float nx = nrm[k * 3];
            float ny = nrm[k * 3 + 1];
            float nz = nrm[k * 3 + 2];
            float wx = n[0] * nx + n[1] * ny + n[2] * nz;
            float wy = n[3] * nx + n[4] * ny + n[5] * nz;
            float wz = n[6] * nx + n[7] * ny + n[8] * nz;
            out.m_5954_(m[0] * x + m[1] * y + m[2] * z + m[3], m[4] * x + m[5] * y + m[6] * z + m[7], m[8] * x + m[9] * y + m[10] * z + m[11],
                1.0F, 1.0F, 1.0F, 1.0F, uv[k * 2], uv[k * 2 + 1], overlay, light, wx, wy, wz);
        }
    }
}
