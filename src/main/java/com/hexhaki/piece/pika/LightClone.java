package com.hexhaki.piece.pika;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.style.SkinBox;
import com.hexhaki.piece.style.StyleRig;
import com.hexhaki.piece.style.StyleShow;

/**
 * 2.8.8 (the owner: "the light clones are bad to look it, overall terrible"): a clone of light as the user's own body (pure; the game's
 * client/pika/PikaFx and tools/preview/ClonePreview draw the very same quads). Where 2.7.8 drew a man of glowing boxes of its own
 * proportions, a clone is now the player model exactly (StyleRig: head, body, both arms and legs bent at elbow and knee, posed by the
 * Light fruit's own animations), in the user's own skin washed white and gold and lit from within (no shading: it is light), its edges
 * burning gold in two shells round every box, a glow at its heart, and Ama no Murakumo in its right hand held by the grip.
 */
public final class LightClone {
    private LightClone() {
    }

    /** Takes the skin's quads: four corners (x, y, z each, eye-relative), their skin coordinates (u, v each, 0 to 1), one colour. */
    public interface Skin {
        void quad(double[] xyz, float[] uv, int argb);
    }

    /** The light the skin is washed in (white going gold), and the two shells of gold round every box (px out, opacity). */
    public static final int WASH = 0xFFF4D2;
    static final double[][] SHELLS = {{0.06, 0.45}, {0.9, 0.34}, {2.4, 0.12}};
    /** The shells' light: white-hot just over the skin (its colours show through it, lit up: it is light), then gold. */
    static final int[] SHELL = {0xFFFDF0, 0xFFD860, 0xFFD860};

    /**
     * The clone posed by {@code pose} (StyleRig's sampled pose) standing at {@code place} (its feet, eye-relative, its facing),
     * {@code alpha} of it there; with {@code sword}, Ama no Murakumo in the right hand; {@code shimmer} rolls the light.
     */
    public static void body(Skin skin, FireMesh.Sink glow, float[][] pose, StyleShow.Place place, double alpha, boolean sword, double shimmer) {
        if (alpha < 0.01) {
            return;
        }
        int wash = FireMesh.argb(0.88 * alpha, WASH);
        double[] c = new double[24];
        double[] w = new double[24];
        // the head and the body (its halves: the upper carried by the bend)
        for (int layer = 0; layer <= SHELLS.length; layer++) {
            double grow = layer == 0 ? 0.0 : SHELLS[layer - 1][0];
            head(pose, grow, c);
            emit(skin, glow, place, c, w, SkinBox.HEAD, 0, 8, layer, wash, alpha);
            torso(pose, 0, 6, grow, c);
            emit(skin, glow, place, c, w, SkinBox.BODY, 0, 6, layer, wash, alpha);
            torso(pose, 6, 12, grow, c);
            emit(skin, glow, place, c, w, SkinBox.BODY, 6, 12, layer, wash, alpha);
            int[] parts = {StyleRig.RIGHT_ARM, StyleRig.LEFT_ARM, StyleRig.RIGHT_LEG, StyleRig.LEFT_LEG};
            SkinBox.Part[] skins = {SkinBox.RIGHT_ARM, SkinBox.LEFT_ARM, SkinBox.RIGHT_LEG, SkinBox.LEFT_LEG};
            for (int i = 0; i < 4; i++) {
                for (int half = 0; half < 2; half++) {
                    double[] h = StyleRig.half(pose, parts[i], half == 1, grow);
                    System.arraycopy(h, 0, c, 0, 24);
                    emit(skin, glow, place, c, w, skins[i], half * 6, half * 6 + 6, layer, wash, alpha);
                }
            }
        }
        double[] heart = new double[3];
        place.put(StyleRig.torso(pose, 0.0, 4.0, 0.0), heart);
        LightArt.glowLocal(glow, heart[0], heart[1], heart[2], 0.9 + 0.1 * Math.sin(shimmer), 0xFFE070, 0.4 * alpha);
        if (sword) {
            sword(glow, pose, place, alpha, shimmer);
        }
    }

    /** One box: its skin (layer 0) or one of its shells of light (1, 2: faces of gold, clear at their rims). */
    static void emit(Skin skin, FireMesh.Sink glow, StyleShow.Place place, double[] c, double[] w, SkinBox.Part part, int from, int to, int layer,
                     int wash, double alpha) {
        double[] f = new double[3];
        double[] o = new double[3];
        for (int k = 0; k < 8; k++) {
            f[0] = c[k * 3];
            f[1] = c[k * 3 + 1];
            f[2] = c[k * 3 + 2];
            place.put(f, o);
            System.arraycopy(o, 0, w, k * 3, 3);
        }
        if (layer == 0) {
            for (int[] face : SkinBox.faces(part, false, from, to)) {
                double[] xyz = new double[12];
                for (int k = 0; k < 4; k++) {
                    System.arraycopy(w, face[k] * 3, xyz, k * 3, 3);
                }
                float u0 = face[4] / 64.0F;
                float v0 = face[5] / 64.0F;
                float u1 = face[6] / 64.0F;
                float v1 = face[7] / 64.0F;
                skin.quad(xyz, new float[] {u0, v0, u1, v0, u1, v1, u0, v1}, wash);
            }
            return;
        }
        int on = FireMesh.argb(SHELLS[layer - 1][1] * alpha, SHELL[layer - 1]);
        for (int[] face : SkinBox.faces(part, false, from, to)) {
            FireMesh.quad(glow, w[face[0] * 3], w[face[0] * 3 + 1], w[face[0] * 3 + 2], on, w[face[1] * 3], w[face[1] * 3 + 1],
                w[face[1] * 3 + 2], on, w[face[2] * 3], w[face[2] * 3 + 1], w[face[2] * 3 + 2], on, w[face[3] * 3], w[face[3] * 3 + 1],
                w[face[3] * 3 + 2], on);
        }
    }

    static void head(float[][] pose, double grow, double[] out) {
        for (int k = 0; k < 8; k++) {
            double[] p = StyleRig.head(pose, (k & 1) == 0 ? -4.0 - grow : 4.0 + grow, (k & 2) == 0 ? -8.0 - grow : grow,
                (k & 4) == 0 ? -4.0 - grow : 4.0 + grow);
            System.arraycopy(p, 0, out, k * 3, 3);
        }
    }

    static void torso(float[][] pose, int top, int bottom, double grow, double[] out) {
        for (int k = 0; k < 8; k++) {
            double y = (k & 2) == 0 ? top - (top == 0 ? grow : 0.0) : bottom + (bottom == 12 ? grow : 0.0);
            double[] p = StyleRig.torso(pose, (k & 1) == 0 ? -4.0 - grow : 4.0 + grow, y, (k & 4) == 0 ? -2.0 - grow : 2.0 + grow);
            System.arraycopy(p, 0, out, k * 3, 3);
        }
    }

    /**
     * How far ahead of the fist the guard sits (blocks). 2.9.0 (the owner: "The sword grabbing is still broken in third person because
     * you're grabbing it way too up"): the hand closes at the bottom of the grip, just over its end (0.24 before: the hand under the
     * guard and most of the grip sticking out below it); LightArt.blade's grip runs 0.8 back from the guard.
     */
    public static final double GUARD = 0.66;

    /** Ama no Murakumo in the right hand: the blade out of the front of the fist, the grip through it, the guard just ahead of it. */
    public static void sword(FireMesh.Sink glow, float[][] pose, StyleShow.Place place, double alpha, double shimmer) {
        double[] fist = new double[3];
        double[] front = new double[3];
        double[] elbow = new double[3];
        place.put(StyleRig.limb(pose, StyleRig.RIGHT_ARM, 0.0, 0.967, 0.0), fist);
        place.put(StyleRig.limb(pose, StyleRig.RIGHT_ARM, 0.0, 0.967, -2.0), front);
        place.put(StyleRig.limb(pose, StyleRig.RIGHT_ARM, 0.0, 0.55, 0.0), elbow);
        sword(glow, fist, front, elbow, alpha, shimmer);
    }

    /**
     * 2.9.0: Ama no Murakumo held by a fist already placed: its middle, a point out of its front (pixels of the arm's frame: -2 in z) and
     * the elbow, eye-relative (client/FirstPersonLight places them in the view).
     */
    public static void sword(FireMesh.Sink glow, double[] fist, double[] front, double[] elbow, double alpha, double shimmer) {
        double[] out = unit(front[0] - fist[0], front[1] - fist[1], front[2] - fist[2]);
        double[] along = unit(fist[0] - elbow[0], fist[1] - elbow[1], fist[2] - elbow[2]);
        // 2.9.0: out of the front of the fist, leaning LightArtGrip.TILT on toward the line of the forearm (as THIRD_PERSON_GRIP turns it)
        double tilt = Math.toRadians(LightArt.LightArtGrip.TILT);
        double[] blade = unit(out[0] * Math.cos(tilt) + along[0] * Math.sin(tilt), out[1] * Math.cos(tilt) + along[1] * Math.sin(tilt),
            out[2] * Math.cos(tilt) + along[2] * Math.sin(tilt));
        held(glow, fist, blade, along, LightArt.BLADE_LENGTH, LightArt.BLADE_WIDTH, alpha, shimmer);
    }

    /**
     * Ama no Murakumo held at {@code fist} (eye-relative), its blade along {@code blade} (unit), the forearm along {@code along}
     * (unit): LightArt.blade's own frame (its blade down its -y, the cross along z) set so, the guard {@link #GUARD} out along the blade.
     */
    public static void held(FireMesh.Sink glow, double[] fist, double[] blade, double[] along, double length, double width, double alpha,
                            double shimmer) {
        double[] y = {-blade[0], -blade[1], -blade[2]};
        double[] x = unit(along[1] * y[2] - along[2] * y[1], along[2] * y[0] - along[0] * y[2], along[0] * y[1] - along[1] * y[0]);
        double[] z = {x[1] * y[2] - x[2] * y[1], x[2] * y[0] - x[0] * y[2], x[0] * y[1] - x[1] * y[0]};
        double ox = fist[0] + blade[0] * GUARD;
        double oy = fist[1] + blade[1] * GUARD;
        double oz = fist[2] + blade[2] * GUARD;
        int[] faded = new int[4];
        double[] out = new double[12];
        FireMesh.Sink frame = (xyz, argb) -> {
            for (int k = 0; k < 4; k++) {
                double lx = xyz[k * 3];
                double ly = xyz[k * 3 + 1];
                double lz = xyz[k * 3 + 2];
                out[k * 3] = ox + x[0] * lx + y[0] * ly + z[0] * lz;
                out[k * 3 + 1] = oy + x[1] * lx + y[1] * ly + z[1] * lz;
                out[k * 3 + 2] = oz + x[2] * lx + y[2] * ly + z[2] * lz;
                faded[k] = (int) Math.round(((argb[k] >>> 24) & 255) * alpha) << 24 | argb[k] & 0xFFFFFF;
            }
            glow.quad(out, faded);
        };
        LightArt.blade(frame, length, width, 0.5 + 0.5 * Math.sin(shimmer));
    }

    static double[] unit(double x, double y, double z) {
        double l = Math.sqrt(x * x + y * y + z * z);
        return l < 1.0e-9 ? new double[] {0.0, 1.0, 0.0} : new double[] {x / l, y / l, z / l};
    }
}
