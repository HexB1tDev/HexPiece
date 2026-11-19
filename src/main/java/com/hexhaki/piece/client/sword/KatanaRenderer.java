package com.hexhaki.piece.client.sword;

import com.hexhaki.piece.HexPiece;
import com.hexhaki.piece.sword.BladeCoat;
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

    /** 2.6.2: blocks per model px of {@code katana} as it is drawn ({@link Katana#size}: every blade but Yoru 1.25 times its mesh). */
    public static float px(Katana katana) {
        return SCALE * (katana == null ? 1.0F : katana.size());
    }
    /** Model pixels: about how long a katana is from pommel to tip, the middle of a sheathed one, and where the right fist's centre
     * grips the handle (the guard at 0, the fist's top just under it; tools/sword_anims.py uses the same). */
    public static final float LENGTH = 28.0F;
    /** 2.2.7: Yoru, pommel to point (model px). */
    public static final float YORU_LENGTH = 44.0F;
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
        if (saya && katana.carry() == Katana.Carry.BACK) {
            return null; // 2.2.7: Yoru has no saya (it is tied on the back)
        }
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
        draw(pose, buffers, katana, blade, saya, light, shine, 0.0F);
    }

    /**
     * 2.6.1 (the owner: "Haki coating is ONLY on the blade of the swords, never on the handle or saya", "progressive not instant (like
     * the arms slowly turning black)"): the sword with its steel (the mesh's 01_Blade part) coated {@code coat} of the way
     * ({@link BladeCoat}: from the guard up to the point, each vertex a little out of step), everything else (the habaki, guard, handle,
     * saya) in its own colours. Until 2.6.0 the whole sword, handle and guard too, went black at once through Armament's black buffers
     * (only the saya was kept out, 2.2.6). The glints run white over the black steel (the owner's unsheath reference).
     */
    public static void draw(PoseStack pose, MultiBufferSource buffers, Katana katana, boolean blade, boolean saya, int light, float shine,
                            float coat) {
        pose.m_85836_();
        float px = px(katana);
        pose.m_85841_(px, px, px);
        PoseStack.Pose last = pose.m_85850_();
        VertexConsumer solid = buffers.m_6299_(RenderType.m_110458_(texture(katana)));
        String lod = lod(last);
        KatanaMesh bladeMesh = blade ? mesh(katana, false, lod) : null;
        KatanaMesh sayaMesh = saya ? mesh(katana, true, lod) : null;
        if (sayaMesh != null) {
            quads(last, solid, sayaMesh, 0, sayaMesh.quadCount(), light);
        }
        if (bladeMesh != null) {
            if (coat <= 0.0F || bladeMesh.bladeCount <= 0 || !katana.blackens()) {
                quads(last, solid, bladeMesh, 0, bladeMesh.quadCount(), light);
            } else {
                int first = bladeMesh.bladeFirst;
                int end = first + bladeMesh.bladeCount;
                quads(last, solid, bladeMesh, 0, first, light);
                quads(last, solid, bladeMesh, end, bladeMesh.quadCount() - end, light);
                coated(last, solid, bladeMesh, first, bladeMesh.bladeCount, light, coat, Math.max(1.0F, katana.tip()));
            }
            if (shine > 0.0F && bladeMesh.bladeCount > 0) {
                // 2.1.9: alpha blended (some setups draw additive geometry opaque: the owner saw the sword's light as dark)
                // 2.6.3 (the owner: "the haki is not coating the very top of the blade well"): the white glint dimmed on coated steel
                highlight(last, buffers.m_6299_(RenderType.m_269166_()), bladeMesh, shine * (1.0F - 0.75F * Math.max(0.0F, Math.min(1.0F, coat))));
            }
        }
        pose.m_85849_();
    }

    /** 2.6.4: the fully coated steel's multiplier (0..255): a very dark purple. */
    static final int COAT_R = 62;
    static final int COAT_G = 34;
    static final int COAT_B = 88;

    /** 2.6.1: the steel's quads, each vertex as black as the coat has made it ({@link BladeCoat#at}: by how far up the blade it is). */
    static void coated(PoseStack.Pose pose, VertexConsumer buffer, KatanaMesh mesh, int first, int count, int light, float coat, float tip) {
        Matrix4f matrix = pose.m_252922_();
        Matrix3f normal = pose.m_252943_();
        int[] q = mesh.quads;
        float[] p = mesh.pos;
        float[] t = mesh.uv;
        float[] n = mesh.nrm;
        for (int i = first * 4, end = (first + count) * 4; i < end; i++) {
            int v = q[i];
            // 2.6.4 (the owner: "the textures should blacken and turn very dark purple on the temper"): the steel multiplied toward a
            // very dark purple, not black: the temper's bright hamon still shows through, deep purple on near black
            float b = BladeCoat.at(p[v * 3 + 1] / tip, v, coat);
            int r = Math.round(255.0F - (255.0F - COAT_R) * b);
            int g = Math.round(255.0F - (255.0F - COAT_G) * b);
            int bl = Math.round(255.0F - (255.0F - COAT_B) * b);
            buffer.m_252986_(matrix, p[v * 3], p[v * 3 + 1], p[v * 3 + 2]).m_6122_(r, g, bl, 255).m_7421_(t[v * 2], t[v * 2 + 1])
                .m_86008_(OverlayTexture.f_118083_).m_85969_(light).m_252939_(normal, n[v * 3], n[v * 3 + 1], n[v * 3 + 2]).m_5752_();
        }
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

    /** 2.2.7: Yoru's ropes ({@link com.hexhaki.piece.sword.BackCarry}), made once: body-part px. */
    private static java.util.List<com.hexhaki.piece.sword.BackCarry.Quad> ropes;

    /**
     * 2.2.7: the ropes that tie Yoru on, in the body part's frame (px scaled to blocks), lit by the game's entity light as the sword
     * is: the plain white texture tinted per corner by the hemp's strands.
     */
    public static void ropes(PoseStack pose, MultiBufferSource buffers, int light) {
        if (ropes == null) {
            ropes = com.hexhaki.piece.sword.BackCarry.ropes();
        }
        pose.m_85836_();
        pose.m_85841_(SCALE, SCALE, SCALE);
        PoseStack.Pose last = pose.m_85850_();
        Matrix4f matrix = last.m_252922_();
        Matrix3f normal = last.m_252943_();
        VertexConsumer buffer = buffers.m_6299_(RenderType.m_110458_(WHITE));
        for (com.hexhaki.piece.sword.BackCarry.Quad quad : ropes) {
            for (int k = 0; k < 4; k++) {
                int c = quad.rgb()[k];
                buffer.m_252986_(matrix, quad.xyz()[k * 3], quad.xyz()[k * 3 + 1], quad.xyz()[k * 3 + 2])
                    .m_6122_((c >> 16) & 255, (c >> 8) & 255, c & 255, 255).m_7421_(0.5F, 0.5F).m_86008_(OverlayTexture.f_118083_)
                    .m_85969_(light).m_252939_(normal, quad.normals()[k * 3], quad.normals()[k * 3 + 1], quad.normals()[k * 3 + 2])
                    .m_5752_();
            }
        }
        pose.m_85849_();
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
            buffer.m_252986_(matrix, p[v * 3], p[v * 3 + 1], p[v * 3 + 2]).m_85950_(1.0F, 1.0F, 0.97F, Math.min(0.85F, light)).m_5752_();
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
