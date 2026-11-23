package com.hexhaki.piece.client.gear;

import com.hexhaki.client.devilfruit.RubberLimbStates;
import com.hexhaki.client.devilfruit.render.GearOutfitLayer;
import com.hexhaki.client.devilfruit.render.RubberLimbGeometry;
import com.hexhaki.client.devilfruit.render.RubberLimbMesh;
import com.hexhaki.devilfruit.data.StretchStyle;
import com.hexhaki.piece.gear.NikaArc;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/**
 * 3.3.9: Dawn Pistol's fist (the owner accepted "the stretched arm ending in a big ballooned rubber fist like the Bajrang Gun's (smaller)
 * coated black by Haki"). The original's {@code RubberLimbLayer.draw} draws every stretched limb: the arm through
 * {@code GearOutfitLayer.emitWithCuff} (the skin and the jacket's cuff), the sleeve and, for a Haki throw (the stretch's {@code variant}
 * 1: it is drawn Haki-black, 0.11 0.11 0.14), the dawn fire round it, both through {@code RubberLimbMesh.emit}. patch/PiecePatch
 * .pistolArm makes it ask here: {@link #begin} which stretch it draws; {@link #emitSkin} and {@link #emitOther} in place of those calls.
 * For a Dawn Pistol (the PISTOL style with variant 1) the hand cube is drawn at {@link NikaFist#HIDE} (hidden inside) and the Bajrang
 * Gun's ballooned fist (gear/NikaArt.fist) is drawn round it {@link #BALLOON} times the hand's size, Haki-black ({@link #COAT}), inflating
 * as the arm reaches out; anything else is drawn exactly as before. Third person only: the original's first person is its own
 * (renderFirstPerson), left alone.
 */
public final class NikaPistolArm {
    /** The fist's radius over the hand cube's half size, when the arm is out (it inflates from {@link #FROM} of it). 3.3.9's first
     * renders: 5.5 the owner called "way too big"; 3.0 (about three-quarters of a block across). */
    static final float BALLOON = 3.0F;
    static final float FROM = 0.4F;
    /** How far out (blocks from the shoulder) the arm is when the fist has fully inflated. */
    static final float FULL_AT = 2.5F;
    /** The Haki coat's colour over the cel bands (dark, a little violet, so the knuckles still read). */
    public static final int COAT = 0x3A3248;

    static RubberLimbStates.Stretch current;
    /**
     * 3.4.1 (the owner: "does the arm bend? I dont want it to be a straight line", then, tracing Luffy's arm in green on their Egghead
     * frame, "Excuse me??? How IS THIS IN HELL STRAIGHT"): a Dawn Pistol's arm follows gear/NikaArc's looping path (whipped back from the
     * shoulder, a full loop up and out to its side, then into the body fast), drawn as a smooth chain of the original's own limb mesh: a
     * quadratic B-spline through the path's points, each piece one RubberLimbMesh curve (the skin's first with the jacket's cuff), each
     * piece's mesh turned square to the loop's plane.
     */
    static final double[] KNOT = new double[NikaArc.KNOTS * 3];
    static final double[] PLANE = new double[3];
    static final Vector3f PS = new Vector3f();
    static final Vector3f PC = new Vector3f();
    static final Vector3f PE = new Vector3f();
    static final Vector3f SIDE = new Vector3f();
    static final Vector3f LAST = new Vector3f();

    private NikaPistolArm() {
    }

    /** At the head of the original's draw: the stretch it is about to draw. */
    public static void begin(RubberLimbStates.Stretch stretch) {
        current = stretch;
        reaching = false;
    }

    /**
     * 3.4.1 (the owner: "the fist goes through the body, stretching the skin"): while the struck body's skin stretches (client/gear
     * NikaSkinClient), this thrower's arm reaches on through the body to the fist inside the skin's end: its end moved along its own way to
     * the shoulder's distance from the body plus the skin's stretch (the model's frame is the world's at 0.9375 to a block). Set by the
     * skin's pass (it has the player) and kept for the sleeve's and dawn fire's of the same arm.
     */
    static boolean reaching;
    static final Vector3f REACH = new Vector3f();
    /** Where the arm meets the body (the loop is laid out to here; the arm runs straight on through it to {@link #REACH}). */
    static final Vector3f BODY_AT = new Vector3f();

    static Vector3f through(Vector3f start, Vector3f end, AbstractClientPlayer player) {
        reaching = false;
        net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.m_91087_().f_91073_;
        if (player == null || level == null) {
            return end;
        }
        float partial = net.minecraft.client.Minecraft.m_91087_().m_91296_();
        double more = NikaSkinClient.reach(level, player.m_19879_(), level.m_46467_() + partial);
        LivingEntity body = NikaSkinClient.BODY;
        if (more < 0 || body == null) {
            return end;
        }
        double px = player.f_19790_ + (player.m_20185_() - player.f_19790_) * partial;
        double py = player.f_19791_ + (player.m_20186_() - player.f_19791_) * partial + 1.35;
        double pz = player.f_19792_ + (player.m_20189_() - player.f_19792_) * partial;
        double bx = body.f_19790_ + (body.m_20185_() - body.f_19790_) * partial;
        double by = body.f_19791_ + (body.m_20186_() - body.f_19791_) * partial + body.m_20206_() * 0.55;
        double bz = body.f_19792_ + (body.m_20189_() - body.f_19792_) * partial;
        double world = Math.sqrt((bx - px) * (bx - px) + (by - py) * (by - py) + (bz - pz) * (bz - pz)) + Math.max(0, more);
        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float dz = end.z - start.z;
        float l = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (l < 1.0E-4F) {
            return end;
        }
        float k = (float) (world / 0.9375) / l;
        float kb = (float) ((world - Math.max(0, more)) / 0.9375) / l;
        reaching = true;
        BODY_AT.set(start.x + dx * kb, start.y + dy * kb, start.z + dz * kb);
        return REACH.set(start.x + dx * k, start.y + dy * k, start.z + dz * k);
    }

    static boolean dawnPistol() {
        RubberLimbStates.Stretch s = current;
        return s != null && s.variant == 1 && s.style == StretchStyle.PISTOL;
    }

    /** The skin's pass (the arm and the jacket's cuff): a Dawn Pistol's fist first, into the same batch, then the arm, its hand hidden. */
    public static void emitSkin(PoseStack.Pose pose, VertexConsumer out, int light, RubberLimbGeometry geometry, boolean slim, float twist,
                                Vector3f start, Vector3f control, Vector3f end, Vector3f side, float width, float hand, int segments, float a,
                                float b, float c, float d, MultiBufferSource buffers, AbstractClientPlayer player) {
        if (dawnPistol()) {
            through(start, end, player);
        }
        // the loop is laid out to the body while the skin stretches (it holds still), the arm running straight on through it to the fist
        Vector3f to = dawnPistol() && reaching ? BODY_AT : end;
        if (!dawnPistol() || !loop(start, to, side)) {
            GearOutfitLayer.emitWithCuff(pose, out, light, geometry, slim, twist, start, control, end, side, width, hand, segments, a, b, c, d,
                buffers, player);
            return;
        }
        boolean on = reaching;
        int last = NikaArc.KNOTS - 2;
        try {
            // the fist along the path's last stretch (or the straight run on through the body)
            set(LAST, NikaArc.KNOTS - 2);
            fist(pose, out, light, start, on ? BODY_AT : LAST, on ? REACH : end, side, hand, slim);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: Dawn Pistol's fist could not be drawn", failure);
        }
        // every piece but the first into the skin's batch, then the first with the cuff (it may move on to the cuff's own batch)
        int per = Math.max(3, segments / 4);
        for (int k = 1; k <= last; k++) {
            piece(k);
            RubberLimbMesh.emit(pose, out, light, geometry, slim, twist, PS, PC, PE, SIDE, width, k == last && !on ? hand * NikaFist.HIDE : 0.0F,
                per, a, b, c, d);
        }
        if (on) {
            onward();
            RubberLimbMesh.emit(pose, out, light, geometry, slim, twist, PS, PC, PE, SIDE, width, hand * NikaFist.HIDE, per, a, b, c, d);
        }
        piece(0);
        GearOutfitLayer.emitWithCuff(pose, out, light, geometry, slim, twist, PS, PC, PE, SIDE, width, 0.0F, per, a, b, c, d, buffers, player);
    }

    /** The straight run from the body on to the fist at the stretched skin's end, into PS, PC, PE. */
    static void onward() {
        PS.set(BODY_AT.x, BODY_AT.y, BODY_AT.z);
        PC.set((BODY_AT.x + REACH.x) * 0.5F, (BODY_AT.y + REACH.y) * 0.5F, (BODY_AT.z + REACH.z) * 0.5F);
        PE.set(REACH.x, REACH.y, REACH.z);
    }

    /** The sleeve's and the dawn fire's passes: a Dawn Pistol's arm along its loop, its hand cube hidden in the fist. */
    public static void emitOther(PoseStack.Pose pose, VertexConsumer out, int light, RubberLimbGeometry geometry, boolean slim, float twist,
                                 Vector3f start, Vector3f control, Vector3f end, Vector3f side, float width, float hand, int segments, float a,
                                 float b, float c, float d) {
        boolean on = dawnPistol() && reaching;
        if (!dawnPistol() || !loop(start, on ? BODY_AT : end, side)) {
            RubberLimbMesh.emit(pose, out, light, geometry, slim, twist, start, control, end, side, width, hand, segments, a, b, c, d);
            return;
        }
        int last = NikaArc.KNOTS - 2;
        int per = Math.max(3, segments / 4);
        for (int k = 0; k <= last; k++) {
            piece(k);
            RubberLimbMesh.emit(pose, out, light, geometry, slim, twist, PS, PC, PE, SIDE, width, k == last && !on ? hand * NikaFist.HIDE : 0.0F, per,
                a, b, c, d);
        }
        if (on) {
            onward();
            RubberLimbMesh.emit(pose, out, light, geometry, slim, twist, PS, PC, PE, SIDE, width, hand * NikaFist.HIDE, per, a, b, c, d);
        }
    }

    /**
     * Lays the path out (the model's frame: up is -y, a right arm's outside -x) and {@link #SIDE} square to its plane (on the original's
     * side's side); false when the arm is too short to loop.
     */
    static boolean loop(Vector3f start, Vector3f end, Vector3f side) {
        float fx = end.x - start.x;
        float fy = end.y - start.y;
        float fz = end.z - start.z;
        if (fx * fx + fy * fy + fz * fz < 0.25F) {
            return false;
        }
        RubberLimbStates.Stretch s = current;
        boolean right = s != null && s.limb != null && s.limb.right();
        if (!NikaArc.plane(fx, fy, fz, 0, -1, 0, right ? -1 : 1, 0, 0, PLANE)) {
            return false;
        }
        NikaArc.path(start.x, start.y, start.z, end.x, end.y, end.z, PLANE[0], PLANE[1], PLANE[2], KNOT);
        // square to the loop's plane: the way x the loop's direction (every tangent of the path lies in that plane)
        float nx = (float) (fy * PLANE[2] - fz * PLANE[1]);
        float ny = (float) (fz * PLANE[0] - fx * PLANE[2]);
        float nz = (float) (fx * PLANE[1] - fy * PLANE[0]);
        float nl = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (nl < 1.0E-6F) {
            return false;
        }
        float sign = nx * side.x + ny * side.y + nz * side.z < 0 ? -1.0F : 1.0F;
        SIDE.set(nx / nl * sign, ny / nl * sign, nz / nl * sign);
        return true;
    }

    /** Piece {@code k} of the B-spline into PS, PC, PE: from the middle of knots k and k + 1 (the first from knot 0), round knot k + 1, to
     * the middle of knots k + 1 and k + 2 (the last to the end). */
    static void piece(int k) {
        int last = NikaArc.KNOTS - 2;
        if (k == 0) {
            set(PS, 0);
        } else {
            mid(PS, k, k + 1);
        }
        set(PC, k + 1);
        if (k == last) {
            set(PE, NikaArc.KNOTS - 1);
        } else {
            mid(PE, k + 1, k + 2);
        }
    }

    static void set(Vector3f v, int i) {
        v.set((float) KNOT[i * 3], (float) KNOT[i * 3 + 1], (float) KNOT[i * 3 + 2]);
    }

    static void mid(Vector3f v, int i, int j) {
        v.set((float) (KNOT[i * 3] + KNOT[j * 3]) * 0.5F, (float) (KNOT[i * 3 + 1] + KNOT[j * 3 + 1]) * 0.5F,
            (float) (KNOT[i * 3 + 2] + KNOT[j * 3 + 2]) * 0.5F);
    }

    /**
     * The fist at the arm's end, knuckles along the arm's last stretch (from the bow's control point), the back of the hand up (the
     * model's frame: y down), a right hand on the right arm (the owner: "thats the left hand on the right arm"), inflating as it reaches.
     */
    static void fist(PoseStack.Pose pose, VertexConsumer out, int light, Vector3f start, Vector3f control, Vector3f end, Vector3f side,
                     float hand, boolean slim) {
        float ox = end.x - start.x;
        float oy = end.y - start.y;
        float oz = end.z - start.z;
        float reach = (float) Math.sqrt(ox * ox + oy * oy + oz * oz);
        float grow = FROM + (1.0F - FROM) * Math.min(1.0F, reach / FULL_AT);
        float fx = end.x - control.x;
        float fy = end.y - control.y;
        float fz = end.z - control.z;
        float fl = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (fl < 1.0E-3F) {
            fx = ox;
            fy = oy;
            fz = oz;
            fl = reach;
        }
        if (fl < 1.0E-4F) {
            return;
        }
        fx /= fl;
        fy /= fl;
        fz /= fl;
        float r = hand * 0.125F * BALLOON * grow;
        RubberLimbStates.Stretch s = current;
        boolean right = s != null && s.limb != null && s.limb.right();
        int tint = com.hexhaki.piece.gear.NikaArt.tint;
        com.hexhaki.piece.gear.NikaArt.tint = COAT;
        try {
            // the back of the hand up: the model's up is -y
            NikaFist.fistAt(pose, out, light, end.x + fx * r, end.y + fy * r, end.z + fz * r, fx, fy, fz, 0.0F, -1.0F, 0.0F, r, slim, right);
        } finally {
            com.hexhaki.piece.gear.NikaArt.tint = tint;
        }
    }
}
