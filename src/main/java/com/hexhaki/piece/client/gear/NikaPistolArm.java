package com.hexhaki.piece.client.gear;

import com.hexhaki.client.devilfruit.RubberLimbStates;
import com.hexhaki.client.devilfruit.render.GearOutfitLayer;
import com.hexhaki.client.devilfruit.render.RubberLimbGeometry;
import com.hexhaki.client.devilfruit.render.RubberLimbMesh;
import com.hexhaki.devilfruit.data.StretchStyle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
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

    private NikaPistolArm() {
    }

    /** At the head of the original's draw: the stretch it is about to draw. */
    public static void begin(RubberLimbStates.Stretch stretch) {
        current = stretch;
    }

    static boolean dawnPistol() {
        RubberLimbStates.Stretch s = current;
        return s != null && s.variant == 1 && s.style == StretchStyle.PISTOL;
    }

    /** The skin's pass (the arm and the jacket's cuff): a Dawn Pistol's fist first, into the same batch, then the arm, its hand hidden. */
    public static void emitSkin(PoseStack.Pose pose, VertexConsumer out, int light, RubberLimbGeometry geometry, boolean slim, float twist,
                                Vector3f start, Vector3f control, Vector3f end, Vector3f side, float width, float hand, int segments, float a,
                                float b, float c, float d, MultiBufferSource buffers, AbstractClientPlayer player) {
        boolean dawn = dawnPistol();
        if (dawn) {
            try {
                fist(pose, out, light, start, control, end, side, hand, slim);
            } catch (RuntimeException failure) {
                com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: Dawn Pistol's fist could not be drawn", failure);
            }
        }
        GearOutfitLayer.emitWithCuff(pose, out, light, geometry, slim, twist, start, control, end, side, width, dawn ? hand * NikaFist.HIDE : hand,
            segments, a, b, c, d, buffers, player);
    }

    /** The sleeve's and the dawn fire's passes: a Dawn Pistol's hand cube hidden in the fist. */
    public static void emitOther(PoseStack.Pose pose, VertexConsumer out, int light, RubberLimbGeometry geometry, boolean slim, float twist,
                                 Vector3f start, Vector3f control, Vector3f end, Vector3f side, float width, float hand, int segments, float a,
                                 float b, float c, float d) {
        RubberLimbMesh.emit(pose, out, light, geometry, slim, twist, start, control, end, side, width, dawnPistol() ? hand * NikaFist.HIDE : hand,
            segments, a, b, c, d);
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
