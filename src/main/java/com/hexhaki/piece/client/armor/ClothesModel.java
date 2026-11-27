package com.hexhaki.piece.client.armor;

import com.hexhaki.piece.HexPiece;
import com.hexhaki.piece.armor.CoatCloth;
import com.hexhaki.piece.armor.PieceArmor;
import com.hexhaki.piece.client.render.Bulk;
import com.hexhaki.piece.client.sword.KatanaMesh;
import com.hexhaki.piece.client.sword.KatanaRenderer;
import com.hexhaki.piece.client.sword.SwordLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/**
 * 3.8.9 (the owner: "I also will be sending clothes replacements for the ones currently in game"): an armor piece drawn as the owner's
 * own clothes (Clothes_Replacements.blend, baked by tools/clothes_bake.py into {@code assets/hexhaki/clothes/<piece>.hkm}, one atlas):
 * each group of its faces on the body part that carries it, as the wearer's model is posed (copied from the armor layer's), with the
 * bends playerAnimator gives a player (a forearm's, a knee's; the body's bend, which the layer is given whole, undone for the hips and
 * legs); a slim wearer's sleeves narrowed to its arms. A coat moves as armor/CoatCloth says (the owner: "The arms cannot clip through the
 * coats, when walking the coat should be pushed back like the wind is pushing it", "the arms should never interact with the coat's arms",
 * "head is clipping through the coat"): blown back by the wearer's own speed, its sleeves hinged at the shoulders, kept off the body,
 * the legs, the arms and the head. tools/clothes_view.py renders the same.
 */
public final class ClothesModel extends HumanoidModel<LivingEntity> {
    private static final Map<String, KatanaMesh> MESHES = new HashMap<>();
    private static final Set<String> FAILED = new HashSet<>();
    /** Entity id -> {back, across, flutter phase, last frame's nano time}: each wearer's wind, eased. */
    private static final Map<Integer, double[]> WINDS = new HashMap<>();
    static final float FULL_WITHIN = 3.0F;
    static final float MID_WITHIN = 12.0F;
    /** How fast the wind follows the wearer's speed (of the way a second) and the flutter's pace (radians a second, still and running). */
    static final double EASE = 5.0;
    static final double FLUTTER_STILL = 2.0;
    static final double FLUTTER_RUN = 9.0;
    private final PieceArmor piece;
    private final boolean slim;
    private LivingEntity wearer;
    private final Map<KatanaMesh, Cloth> cloths = new HashMap<>();

    /** A coat mesh's working arrays (made once a level of detail). */
    static final class Cloth {
        final int n;
        final float[] rest;
        final byte[] kind;
        final float[] ways;
        final float[] moved;
        /** Each quad's winding against the owner's normals (1 or -1), so a bent face is lit on the side it was. */
        final byte[] winding;

        Cloth(KatanaMesh mesh, boolean slim) {
            this.n = mesh.positions().length / 3;
            this.rest = mesh.positions();
            this.kind = new byte[this.n];
            java.util.Arrays.fill(this.kind, (byte) -1);
            int[] q = mesh.quadIndices();
            for (int g = 0; g < mesh.groupNames.length; g++) {
                String name = mesh.groupNames[g];
                byte k = name.equals("right_sleeve") ? CoatCloth.SLEEVE_RIGHT : name.equals("left_sleeve") ? CoatCloth.SLEEVE_LEFT
                    : name.startsWith("body_") ? (name.endsWith("_in") ? CoatCloth.LINING_KIND : CoatCloth.BODY) : -1;
                for (int i = mesh.groupFirst[g] * 4, end = (mesh.groupFirst[g] + mesh.groupCount[g]) * 4; i < end; i++) {
                    if (k >= 0) {
                        this.kind[q[i]] = k;
                    }
                }
            }
            this.ways = CoatCloth.restWays(this.rest, this.n, slim);
            this.moved = new float[this.n * 3];
            float[] nrm = mesh.normals();
            this.winding = new byte[q.length / 4];
            for (int f = 0; f < this.winding.length; f++) {
                int a = q[f * 4];
                int b = q[f * 4 + 1];
                int d = q[f * 4 + 2];
                float[] cr = cross(this.rest, a, b, d);
                float dot = cr[0] * nrm[a * 3] + cr[1] * nrm[a * 3 + 1] + cr[2] * nrm[a * 3 + 2];
                this.winding[f] = (byte) (dot < 0.0F ? -1 : 1);
            }
        }
    }

    public ClothesModel(PieceArmor piece, boolean slim) {
        super(layer().m_171564_());
        this.piece = piece;
        this.slim = slim;
    }

    /** An empty part tree (the player's parts at their pivots): the clothes are drawn on them, not built of their boxes. */
    static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.m_171576_();
        root.m_171599_("head", CubeListBuilder.m_171558_(), PartPose.m_171423_(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.m_171599_("hat", CubeListBuilder.m_171558_(), PartPose.m_171423_(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.m_171599_("body", CubeListBuilder.m_171558_(), PartPose.m_171423_(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.m_171599_("right_arm", CubeListBuilder.m_171558_(), PartPose.m_171423_(-5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.m_171599_("left_arm", CubeListBuilder.m_171558_(), PartPose.m_171423_(5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.m_171599_("right_leg", CubeListBuilder.m_171558_(), PartPose.m_171423_(-1.9F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        root.m_171599_("left_leg", CubeListBuilder.m_171558_(), PartPose.m_171423_(1.9F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        return LayerDefinition.m_171565_(mesh, 64, 64);
    }

    /** The wearer's pose, as the armor layer prepared it (attacks, swimming, riding, crouching), and who wears it this frame. */
    public void prepare(HumanoidModel<?> source, LivingEntity wearer) {
        this.f_102608_ = source.f_102608_;
        this.f_102609_ = source.f_102609_;
        this.f_102610_ = source.f_102610_;
        this.f_102817_ = source.f_102817_;
        this.f_102815_ = source.f_102815_;
        this.f_102816_ = source.f_102816_;
        this.f_102808_.m_104315_(source.f_102808_);
        this.f_102809_.m_104315_(source.f_102809_);
        this.f_102810_.m_104315_(source.f_102810_);
        this.f_102811_.m_104315_(source.f_102811_);
        this.f_102812_.m_104315_(source.f_102812_);
        this.f_102813_.m_104315_(source.f_102813_);
        this.f_102814_.m_104315_(source.f_102814_);
        this.wearer = wearer;
    }

    static KatanaMesh mesh(String name) {
        KatanaMesh mesh = MESHES.get(name);
        if (mesh == null && !FAILED.contains(name)) {
            try {
                mesh = KatanaMesh.read("/assets/hexhaki/clothes/" + name + ".hkm", name);
                MESHES.put(name, mesh);
            } catch (Exception failure) {
                FAILED.add(name);
                HexPiece.LOGGER.error("HexPiece could not read the clothes mesh {}", name, failure);
            }
        }
        return mesh;
    }

    static String lod(PoseStack.Pose pose) {
        Vector3f origin = new Vector3f(0.0F, 0.0F, 0.0F);
        pose.m_252922_().transformPosition(origin);
        float distance = (float) Math.sqrt(origin.x * origin.x + origin.y * origin.y + origin.z * origin.z);
        return distance < FULL_WITHIN ? "" : distance < MID_WITHIN ? "_mid" : "_far";
    }

    boolean coat() {
        return this.piece == PieceArmor.ADMIRAL_COAT || this.piece == PieceArmor.BLACKBEARD_COAT;
    }

    @Override
    public void m_7695_(PoseStack pose, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
        try {
            KatanaMesh mesh = mesh(this.piece.key() + lod(pose.m_85850_()));
            if (mesh == null) {
                return;
            }
            AbstractClientPlayer player = this.wearer instanceof AbstractClientPlayer p ? p : null;
            float[] bodyBend = player == null ? new float[2] : SwordLayer.bend(player, "body");
            boolean coat = coat();
            for (int g = 0; g < mesh.groupNames.length; g++) {
                String name = mesh.groupNames[g];
                if (coat && (name.startsWith("body_") || name.endsWith("_sleeve"))) {
                    continue; // drawn below, moved as a coat
                }
                rigid(pose, buffer, light, mesh, g, name, player, bodyBend);
            }
            if (coat) {
                cloth(pose, buffer, light, mesh, player, bodyBend);
            }
        } catch (RuntimeException failure) {
            // clothes that cannot be drawn never stop the wearer from being drawn
        }
    }

    ModelPart part(String name) {
        if (name.startsWith("head")) {
            return this.f_102808_;
        }
        if (name.startsWith("right_arm")) {
            return this.f_102811_;
        }
        if (name.startsWith("left_arm")) {
            return this.f_102812_;
        }
        if (name.startsWith("right_leg")) {
            return this.f_102813_;
        }
        if (name.startsWith("left_leg")) {
            return this.f_102814_;
        }
        return this.f_102810_;
    }

    /** One group on its part: the hips and legs out of the body's bend, a forearm's and a knee's own bend, a slim arm narrowed. */
    void rigid(PoseStack pose, VertexConsumer buffer, int light, KatanaMesh mesh, int g, String name, AbstractClientPlayer player,
               float[] bodyBend) {
        boolean leg = name.contains("_leg");
        boolean arm = name.contains("_arm");
        boolean lower = name.endsWith("_lower") || name.endsWith("_lower_in");
        pose.m_85836_();
        if (leg || name.startsWith("body_lower")) {
            SwordLayer.bendAbout(pose, 0.375F, bodyBend[0], -bodyBend[1]);
        }
        part(name).m_104299_(pose);
        if (lower && player != null && (arm || leg)) {
            String which = name.startsWith("right") ? (arm ? "rightArm" : "rightLeg") : (arm ? "leftArm" : "leftLeg");
            float[] bend = SwordLayer.bend(player, which);
            SwordLayer.bendAbout(pose, arm ? 0.25F : 0.375F, bend[0], bend[1]);
        }
        if (arm && this.slim) {
            // a slim arm's box is 3 px across (its middle half a px further in): the sleeve narrowed onto it
            float wide = name.startsWith("right") ? -1.0F : 1.0F;
            pose.m_252880_(wide * 0.5F / 16.0F, 0.0F, 0.0F);
            pose.m_85841_(0.75F, 1.0F, 1.0F);
            pose.m_252880_(-wide / 16.0F, 0.0F, 0.0F);
        }
        pose.m_85841_(1.0F / 16.0F, 1.0F / 16.0F, 1.0F / 16.0F);
        KatanaRenderer.quads(pose.m_85850_(), buffer, mesh, mesh.groupFirst[g], mesh.groupCount[g], light);
        pose.m_85849_();
    }

    /** A coat's body and sleeves, moved as armor/CoatCloth says, in model px, drawn from the model's root. */
    void cloth(PoseStack pose, VertexConsumer buffer, int light, KatanaMesh mesh, AbstractClientPlayer player, float[] bodyBend) {
        Cloth c = this.cloths.computeIfAbsent(mesh, m -> new Cloth(m, this.slim));
        double[] wind = wind();
        CoatCloth.body(c.rest, c.kind, c.n, (float) wind[0], (float) wind[1], (float) wind[2], this.f_102813_.f_104203_,
            this.f_102814_.f_104203_, c.moved);
        float[] body = frame(this.f_102810_);
        float[] model = new float[c.n * 3];
        for (int i = 0; i < c.n; i++) {
            float x = c.moved[i * 3];
            float y = c.moved[i * 3 + 1];
            float z = c.moved[i * 3 + 2];
            model[i * 3] = body[0] * x + body[1] * y + body[2] * z + body[3];
            model[i * 3 + 1] = body[4] * x + body[5] * y + body[6] * z + body[7];
            model[i * 3 + 2] = body[8] * x + body[9] * y + body[10] * z + body[11];
        }
        float[][] frames = new float[4][];
        for (int side = 0; side < 2; side++) {
            ModelPart arm = side == 0 ? this.f_102811_ : this.f_102812_;
            float[] upper = frame(arm);
            float[] bend = player == null ? new float[2] : SwordLayer.bend(player, side == 0 ? "rightArm" : "leftArm");
            frames[side * 2] = upper;
            frames[side * 2 + 1] = times(upper, bendFrame(4.0F, bend[0], bend[1]));
        }
        CoatCloth.keepOut(model, c.kind, c.rest, c.n, c.ways, frames, frame(this.f_102808_), this.slim);
        // the chest and the sleeves bent with the body (the layer's own pose), the hips' cloth out of the bend
        pose.m_85836_();
        pose.m_85841_(1.0F / 16.0F, 1.0F / 16.0F, 1.0F / 16.0F);
        PoseStack.Pose bent = pose.m_85850_();
        pose.m_85849_();
        pose.m_85836_();
        SwordLayer.bendAbout(pose, 0.375F, bodyBend[0], -bodyBend[1]);
        pose.m_85841_(1.0F / 16.0F, 1.0F / 16.0F, 1.0F / 16.0F);
        PoseStack.Pose straight = pose.m_85850_();
        float[] mb = new float[12];
        float[] nb = new float[9];
        float[] ms = new float[12];
        float[] ns = new float[9];
        Bulk.read(bent, mb, nb);
        Bulk.read(straight, ms, ns);
        pose.m_85849_();
        int[] q = mesh.quadIndices();
        float[] uv = mesh.uvs();
        for (int g = 0; g < mesh.groupNames.length; g++) {
            String name = mesh.groupNames[g];
            if (!name.startsWith("body_") && !name.endsWith("_sleeve")) {
                continue;
            }
            boolean upright = name.startsWith("body_lower");
            float[] m = upright ? ms : mb;
            float[] nm = upright ? ns : nb;
            for (int f = mesh.groupFirst[g], end = mesh.groupFirst[g] + mesh.groupCount[g]; f < end; f++) {
                int a = q[f * 4];
                int b = q[f * 4 + 1];
                int d = q[f * 4 + 2];
                // the face's own normal as it is now (the cloth is bent), on the side the owner's faced
                float[] cr = cross(model, a, b, d);
                float nx = cr[0] * c.winding[f];
                float ny = cr[1] * c.winding[f];
                float nz = cr[2] * c.winding[f];
                float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                if (len > 1.0E-9F) {
                    nx /= len;
                    ny /= len;
                    nz /= len;
                }
                for (int k = 0; k < 4; k++) {
                    int v = q[f * 4 + k];
                    Bulk.vertex(buffer, m, nm, model[v * 3], model[v * 3 + 1], model[v * 3 + 2], 1.0F, 1.0F, 1.0F, 1.0F, uv[v * 2],
                        uv[v * 2 + 1], OverlayTexture.f_118083_, light, nx, ny, nz);
                }
            }
        }
    }

    /** (p[b] - p[a]) x (p[d] - p[a]) of points packed x, y, z. */
    static float[] cross(float[] p, int a, int b, int d) {
        float ux = p[b * 3] - p[a * 3];
        float uy = p[b * 3 + 1] - p[a * 3 + 1];
        float uz = p[b * 3 + 2] - p[a * 3 + 2];
        float vx = p[d * 3] - p[a * 3];
        float vy = p[d * 3 + 1] - p[a * 3 + 1];
        float vz = p[d * 3 + 2] - p[a * 3 + 2];
        return new float[] {uy * vz - uz * vy, uz * vx - ux * vz, ux * vy - uy * vx};
    }

    /** The wearer's wind this frame ({back, across, phase}): their step this tick along and across the way their body faces, eased. */
    double[] wind() {
        LivingEntity e = this.wearer;
        if (e == null) {
            return new double[3];
        }
        double[] w = WINDS.computeIfAbsent(e.m_19879_(), id -> new double[] {0.0, 0.0, 0.0, System.nanoTime()});
        long now = System.nanoTime();
        double dt = Math.min(0.25, Math.max(0.0, (now - w[3]) / 1.0E9));
        w[3] = now;
        float[] target = CoatCloth.wind(e.m_20185_() - e.f_19854_, e.m_20189_() - e.f_19856_, e.f_20883_);
        double k = 1.0 - Math.exp(-EASE * dt);
        w[0] += (target[0] - w[0]) * k;
        w[1] += (target[1] - w[1]) * k;
        w[2] += dt * (FLUTTER_STILL + (FLUTTER_RUN - FLUTTER_STILL) * Math.min(1.0, Math.abs(w[0]) + Math.abs(w[1])));
        if (WINDS.size() > 256) {
            WINDS.clear();
        }
        return w;
    }

    /** A part's frame (its own px -> model px), 3 x 4 row-major: its pivot, then Rz Ry Rx as the game turns it. */
    static float[] frame(ModelPart part) {
        double cx = Math.cos(part.f_104203_);
        double sx = Math.sin(part.f_104203_);
        double cy = Math.cos(part.f_104204_);
        double sy = Math.sin(part.f_104204_);
        double cz = Math.cos(part.f_104205_);
        double sz = Math.sin(part.f_104205_);
        // Rz * Ry * Rx
        return new float[] {
            (float) (cz * cy), (float) (cz * sy * sx - sz * cx), (float) (cz * sy * cx + sz * sx), part.f_104200_,
            (float) (sz * cy), (float) (sz * sy * sx + cz * cx), (float) (sz * sy * cx - cz * sx), part.f_104201_,
            (float) (-sy), (float) (cy * sx), (float) (cy * cx), part.f_104202_};
    }

    /** bendy-lib's bend of a limb's lower half: {@code amount} radians about (cos(-axis), 0, sin(-axis)) at {@code y} px down. */
    static float[] bendFrame(float y, float axis, float amount) {
        float ax = (float) Math.cos(-axis);
        float az = (float) Math.sin(-axis);
        float c = (float) Math.cos(amount);
        float s = (float) Math.sin(amount);
        float t = 1.0F - c;
        float[] r = {c + ax * ax * t, -az * s, ax * az * t, 0.0F, az * s, c, -ax * s, 0.0F, az * ax * t, ax * s, c + az * az * t, 0.0F};
        // about the point (0, y, 0): p' = R (p - Y) + Y
        r[3] = -(r[1] * y);
        r[7] = y - r[5] * y;
        r[11] = -(r[9] * y);
        return r;
    }

    static float[] times(float[] a, float[] b) {
        float[] r = new float[12];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                float v = a[i * 4] * b[j] + a[i * 4 + 1] * b[4 + j] + a[i * 4 + 2] * b[8 + j];
                r[i * 4 + j] = j == 3 ? v + a[i * 4 + 3] : v;
            }
        }
        return r;
    }
}
