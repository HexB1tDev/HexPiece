package com.hexhaki.piece.client.sword;

import com.hexhaki.piece.HexPiece;
import com.hexhaki.piece.sword.Katana;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 2.1.4: draws the owner's katanas and their saya from their own meshes and textures, in every place one is seen (in hand, at the
 * hip, in an item frame or on the ground: {@link KatanaItemRenderer}, {@link SwordLayer}).
 * <p>
 * The owner: "Improve the finish with crisp edges, convincing metal shading and controlled highlights that make the blades shine as
 * they move ... Keep the results compatible with the project's existing rendering pipeline." The whole mesh is drawn with the game's
 * own entity shading (its two lights and ambient, by each face's normal, crisp: cutout, no blur); then the blade alone once more,
 * additive: the light of the sky reflected off the steel, worked out from where each face of the blade points in view this frame
 * ({@link #shine}), a soft band that slides along the blade as it turns and flashes along the edge when it faces the sky. Nothing else
 * of the sword shines.
 * <p>
 * Frame: model pixels (the guard at y 0, the blade up +y, its edge to -x, the handle down -y), drawn at {@link #SCALE} (the meshes are
 * 27.5 pixels long: one of them is one of the game's).
 */
public final class KatanaRenderer {
    /** Model pixels to blocks: the owner's meshes are drawn in Minecraft pixels, so one of theirs is one of the game's (the whole
     * katana about 1.75 blocks, its blade about 1.3; the owner, on 0.62: "the sword is wayyyy too small"). */
    public static final float SCALE = 1.0F / 16.0F;
    /** Model pixels: about how long a katana is from pommel to tip, the middle of a sheathed one, and where the right fist's centre
     * grips the handle (the guard at 0, the fist's top just under it; tools/sword_anims.py uses the same). */
    public static final float LENGTH = 28.0F;
    public static final float MIDDLE = 7.4F;
    public static final float GRIP = -2.4F;
    static final ResourceLocation WHITE = ResourceLocation.m_214293_("hexhaki", "textures/piece/white.png");
    static final int FULL_BRIGHT = 0xF000F0;
    private static final Map<String, KatanaMesh> MESHES = new java.util.HashMap<>();
    private static final Set<String> FAILED = new HashSet<>();
    /** Camera distances (blocks) within which the owner's full mesh, then the mid level of detail, are drawn (beyond: the far one). */
    static final float FULL_WITHIN = 2.5F;
    static final float MID_WITHIN = 10.0F;

    private KatanaRenderer() {
    }

    public static ResourceLocation texture(Katana katana) {
        return ResourceLocation.m_214293_("hexhaki", "textures/katana/" + katana.key() + ".png");
    }

    /** The mesh at a level of detail ("" the owner's whole mesh, "_mid", "_far"), read once. */
    static KatanaMesh mesh(Katana katana, boolean saya, String lod) {
        String name = katana.key() + (saya ? "_sheath" : "") + lod;
        KatanaMesh mesh = MESHES.get(name);
        if (mesh == null && !FAILED.contains(name)) {
            try {
                mesh = KatanaMesh.load(name);
                MESHES.put(name, mesh);
            } catch (Exception failure) {
                FAILED.add(name);
                HexPiece.LOGGER.error("HexPiece could not read the katana mesh {}", name, failure);
            }
        }
        return mesh;
    }

    /** The level of detail for a katana whose origin the pose puts {@code distance} blocks from the eye. */
    static String lod(PoseStack.Pose pose) {
        Vector3f origin = new Vector3f(0.0F, 0.0F, 0.0F);
        pose.m_252922_().transformPosition(origin);
        float distance = (float) Math.sqrt(origin.x * origin.x + origin.y * origin.y + origin.z * origin.z);
        return distance < FULL_WITHIN ? "" : distance < MID_WITHIN ? "_mid" : "_far";
    }

    /**
     * The blade (with its handle and guard) and/or the saya, in the current pose's frame (model pixels scaled by {@link #SCALE}).
     * {@code shine} 0 draws no highlight, 1 the full one.
     */
    public static void draw(PoseStack pose, MultiBufferSource buffers, Katana katana, boolean blade, boolean saya, int light, float shine) {
        pose.m_85836_();
        pose.m_85841_(SCALE, SCALE, SCALE);
        PoseStack.Pose last = pose.m_85850_();
        VertexConsumer solid = buffers.m_6299_(RenderType.m_110458_(texture(katana)));
        String lod = lod(last);
        KatanaMesh bladeMesh = blade ? mesh(katana, false, lod) : null;
        KatanaMesh sayaMesh = saya ? mesh(katana, true, lod) : null;
        if (sayaMesh != null) {
            quads(last, solid, sayaMesh, 0, sayaMesh.quadCount(), light);
        }
        if (bladeMesh != null) {
            quads(last, solid, bladeMesh, 0, bladeMesh.quadCount(), light);
            if (shine > 0.0F && bladeMesh.bladeCount > 0) {
                highlight(last, buffers.m_6299_(RenderType.m_110488_(WHITE)), bladeMesh, shine);
            }
        }
        pose.m_85849_();
    }

    static void quads(PoseStack.Pose pose, VertexConsumer buffer, KatanaMesh mesh, int first, int count, int light) {
        Matrix4f matrix = pose.m_252922_();
        Matrix3f normal = pose.m_252943_();
        int[] q = mesh.quads;
        float[] p = mesh.pos;
        float[] t = mesh.uv;
        float[] n = mesh.nrm;
        for (int i = first * 4, end = (first + count) * 4; i < end; i++) {
            int v = q[i];
            buffer.m_252986_(matrix, p[v * 3], p[v * 3 + 1], p[v * 3 + 2]).m_6122_(255, 255, 255, 255).m_7421_(t[v * 2], t[v * 2 + 1])
                .m_86008_(OverlayTexture.f_118083_).m_85969_(light).m_252939_(normal, n[v * 3], n[v * 3 + 1], n[v * 3 + 2]).m_5752_();
        }
    }

    /** The blade's highlight: each corner as bright as {@link #shine} finds it this frame, added over the steel. */
    static void highlight(PoseStack.Pose pose, VertexConsumer buffer, KatanaMesh mesh, float strength) {
        Matrix4f matrix = pose.m_252922_();
        Matrix3f normal = pose.m_252943_();
        int[] q = mesh.quads;
        float[] p = mesh.pos;
        float[] n = mesh.nrm;
        Vector3f view = new Vector3f();
        Vector3f at = new Vector3f();
        for (int i = mesh.bladeFirst * 4, end = (mesh.bladeFirst + mesh.bladeCount) * 4; i < end; i++) {
            int v = q[i];
            view.set(n[v * 3], n[v * 3 + 1], n[v * 3 + 2]);
            normal.transform(view);
            at.set(p[v * 3], p[v * 3 + 1], p[v * 3 + 2]);
            matrix.transformPosition(at);
            float light = shine(view.x, view.y, view.z, at.x, at.y, at.z) * strength;
            buffer.m_252986_(matrix, p[v * 3], p[v * 3 + 1], p[v * 3 + 2]).m_85950_(light, light, light * 0.97F, 1.0F).m_7421_(0.5F, 0.5F)
                .m_86008_(OverlayTexture.f_118083_).m_85969_(FULL_BRIGHT).m_252939_(normal, n[v * 3], n[v * 3 + 1], n[v * 3 + 2]).m_5752_();
        }
    }

    /**
     * How much sky the steel reflects at a point (view space: the eye at the origin looking down -z, y up): the eye's ray reflected off
     * the surface; the higher it goes, the more light, a soft band from just above the horizon up (so the highlight is a band that
     * slides as the blade turns, never a flat glow), plus a thin flash where the surface is seen almost edge-on (the edge catching the
     * light). 0..0.85.
     */
    public static float shine(float nx, float ny, float nz, float px, float py, float pz) {
        float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        float pl = (float) Math.sqrt(px * px + py * py + pz * pz);
        if (nl < 1.0E-6F || pl < 1.0E-6F) {
            return 0.0F;
        }
        nx /= nl;
        ny /= nl;
        nz /= nl;
        float ix = px / pl;
        float iy = py / pl;
        float iz = pz / pl;
        float d = ix * nx + iy * ny + iz * nz;
        float ry = iy - 2.0F * d * ny;
        float band = Math.max(0.0F, Math.min(1.0F, (ry - 0.12F) / 0.55F));
        float grazing = 1.0F - Math.abs(d);
        float edge = grazing > 0.9F ? (grazing - 0.9F) * 4.0F : 0.0F;
        return Math.min(0.85F, band * band * 0.7F + edge * Math.max(0.0F, ry + 0.3F));
    }
}
