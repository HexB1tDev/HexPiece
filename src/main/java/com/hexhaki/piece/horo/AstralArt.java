package com.hexhaki.piece.horo;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.style.SkinBox;
import com.hexhaki.piece.style.StyleRig;
import com.hexhaki.piece.style.StyleShow;

/**
 * 3.3.6: Astral Projection as everyone sees it (pure; client/horo/HoroFx and tools/preview/AstralPreview draw the very same quads): the
 * spirit is the user's own body (the player model exactly, StyleRig's boxes in the user's own skin, its outer layer too, as the Light
 * fruit's clones are made), washed ghost-white and see-through, floating as a Japanese ghost does (arms out in front, hands hanging, legs
 * trailing), fading toward its feet, a pale shell of light round every box; the body it left stands where it was in its own skin, lit
 * and shaded by the world as the game lights a player, its head dropped as if asleep.
 *
 * <p>No allocation per frame beyond StyleRig's own: the corners, faces and skin coordinates go through static scratch arrays (the render
 * thread only).
 */
public final class AstralArt {
    private AstralArt() {
    }

    /** Takes the skin's quads: four corners (x, y, z each, eye-relative), their skin coordinates (u, v each, 0 to 1), one colour, the
     *  face's normal (unit, the world's frame). */
    public interface Skin {
        void quad(double[] xyz, float[] uv, int argb, double nx, double ny, double nz);
    }

    /** The spirit's wash (a ghost's white, a breath of lavender) and how much of the skin shows through it. */
    public static final int WASH = 0xF2EEFF;
    public static final double WASH_ALPHA = 0.62;
    /** The spirit's shells of light round every box (px out, opacity) and their colours, palest outermost. */
    static final double[][] SHELLS = {{0.35, 0.14}, {1.4, 0.11}, {3.2, 0.05}};
    static final int[] SHELL = {0xFFFFFF, 0xE6DEFF, 0xCFC4F2};
    /** How much of the spirit's legs show: the thighs, the shins (a ghost fades toward its feet). */
    static final double THIGH = 0.6;
    static final double SHIN = 0.16;

    static final int[] PARTS = {StyleRig.RIGHT_ARM, StyleRig.LEFT_ARM, StyleRig.RIGHT_LEG, StyleRig.LEFT_LEG};
    static final SkinBox.Part[] SKINS = {SkinBox.RIGHT_ARM, SkinBox.LEFT_ARM, SkinBox.RIGHT_LEG, SkinBox.LEFT_LEG};

    static final double[] C = new double[24];
    static final double[] W = new double[24];
    static final double[] XYZ = new double[12];
    static final float[] UV = new float[8];
    static final double[] F3 = new double[3];
    static final double[] O3 = new double[3];

    // ------------------------------------------------------------------------------------------------------ the poses

    /**
     * The spirit's pose {@code age} ticks in, its head turned as the player looks ({@code headPitch} down, {@code headYaw} from its body,
     * degrees, as the game turns a player's head), into {@code pose} ({@code [part][x, y, z, pitch, yaw, roll, bend, weight]}): a
     * Japanese ghost's float, both arms out in front with the hands hanging from the wrists, the legs trailing behind, bobbing and
     * swaying gently.
     */
    public static void floating(double age, double headPitch, double headYaw, float[][] pose) {
        clear(pose);
        double bob = Math.sin(age * 0.1);
        double sway = Math.sin(age * 0.067);
        set(pose, StyleRig.BODY, 0.0, 0.07 * bob, 0.0, 0.0, 0.0, 2.0 * sway, 4.0);
        set(pose, StyleRig.HEAD, 0.0, 0.0, 0.0, headPitch, headYaw, 0.0, 0.0);
        set(pose, StyleRig.RIGHT_ARM, 0.0, 0.0, 0.0, -90.0 + 4.0 * bob, 8.0, 0.0, 34.0 + 6.0 * bob);
        set(pose, StyleRig.LEFT_ARM, 0.0, 0.0, 0.0, -90.0 + 4.0 * bob, -8.0, 0.0, 34.0 + 6.0 * bob);
        set(pose, StyleRig.RIGHT_LEG, 0.0, 0.0, 0.0, 9.0 + 3.0 * sway, 0.0, 2.0, 6.0 + 3.0 * bob);
        set(pose, StyleRig.LEFT_LEG, 0.0, 0.0, 0.0, 13.0 - 3.0 * sway, 0.0, -2.0, 8.0 - 3.0 * bob);
    }

    /** The body left behind: standing, arms hanging, its head dropped forward as if asleep, into {@code pose}. */
    public static void asleep(float[][] pose) {
        clear(pose);
        set(pose, StyleRig.BODY, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.0);
        set(pose, StyleRig.HEAD, 0.0, 0.0, 0.0, 32.0, 0.0, 6.0, 0.0);
        set(pose, StyleRig.RIGHT_ARM, 0.0, 0.0, 0.0, 2.0, 0.0, 3.0, 6.0);
        set(pose, StyleRig.LEFT_ARM, 0.0, 0.0, 0.0, 2.0, 0.0, -3.0, 6.0);
        set(pose, StyleRig.RIGHT_LEG, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
        set(pose, StyleRig.LEFT_LEG, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0);
    }

    static void clear(float[][] pose) {
        for (float[] part : pose) {
            java.util.Arrays.fill(part, 0.0F);
        }
    }

    static void set(float[][] pose, int part, double x, double y, double z, double pitch, double yaw, double roll, double bend) {
        float[] p = pose[part];
        p[0] = (float) x;
        p[1] = (float) y;
        p[2] = (float) z;
        p[3] = (float) pitch;
        p[4] = (float) yaw;
        p[5] = (float) roll;
        p[6] = (float) bend;
        p[7] = 1.0F;
    }

    // ------------------------------------------------------------------------------------------------------ the spirit

    /**
     * The spirit posed by {@code pose} at {@code place} (its feet, eye-relative, its facing), {@code alpha} of it there: its skin washed
     * ghost-white and see-through into {@code skin} (null: not now), its shells of light into {@code glow} (null: not now). The game
     * draws the skin first and the light after (one buffer type at a time, the owner's device).
     */
    public static void spirit(Skin skin, FireMesh.Sink glow, float[][] pose, StyleShow.Place place, double alpha) {
        if (alpha < 0.01) {
            return;
        }
        if (skin != null) {
            boxes(skin, null, pose, place, 0.0, alpha, true, 0);
        }
        if (glow != null) {
            for (int layer = 0; layer < SHELLS.length; layer++) {
                boxes(null, glow, pose, place, SHELLS[layer][0], 1.0, true, FireMesh.argb(SHELLS[layer][1] * alpha, SHELL[layer]));
            }
            // a faint glow at its heart: a ghost lights the dark a little
            place.put(StyleRig.torso(pose, 0.0, 5.0, 0.0), O3);
            FireMesh.shell(glow, O3[0], O3[1], O3[2], 0.9, SHELL[1], 0.07 * alpha, 0.0);
        }
    }

    /** The body left behind: the user's own skin, whole, lit by the world ({@code skin} takes each face's normal). */
    public static void left(Skin skin, float[][] pose, StyleShow.Place place) {
        boxes(skin, null, pose, place, 0.0, 1.0, false, 0);
    }

    /** Each box's faces (SkinBox.faces is built once here): [box][outer], boxes the head, the torso's halves, then each limb's halves. */
    static final int[][][][] FACES = new int[11][2][][];

    static {
        for (int outer = 0; outer < 2; outer++) {
            FACES[0][outer] = SkinBox.faces(SkinBox.HEAD, outer == 1, 0, 8);
            FACES[1][outer] = SkinBox.faces(SkinBox.BODY, outer == 1, 0, 6);
            FACES[2][outer] = SkinBox.faces(SkinBox.BODY, outer == 1, 6, 12);
            for (int i = 0; i < 4; i++) {
                for (int half = 0; half < 2; half++) {
                    FACES[3 + i * 2 + half][outer] = SkinBox.faces(SKINS[i], outer == 1, half * 6, half * 6 + 6);
                }
            }
        }
    }

    /**
     * Every box once: into {@code skin} (its skin and its outer layer, {@code alpha} of it there; a {@code ghost}'s washed and fading
     * toward its feet) or into {@code glow} as a shell {@code grow} px out in {@code shell} (its opacity fading down the legs too).
     */
    static void boxes(Skin skin, FireMesh.Sink glow, float[][] pose, StyleShow.Place place, double grow, double alpha, boolean ghost,
                      int shell) {
        for (int outer = 0; outer < (glow != null ? 1 : 2); outer++) {
            double g = glow != null ? grow : outer == 0 ? 0.0 : 0.25;
            head(pose, glow != null ? grow : outer == 0 ? 0.0 : 0.5);
            box(skin, glow, place, FACES[0][outer], alpha, ghost, shell);
            torso(pose, 0, 6, g);
            box(skin, glow, place, FACES[1][outer], alpha, ghost, shell);
            torso(pose, 6, 12, g);
            box(skin, glow, place, FACES[2][outer], alpha, ghost, shell);
            for (int i = 0; i < 4; i++) {
                for (int half = 0; half < 2; half++) {
                    double[] h = StyleRig.half(pose, PARTS[i], half == 1, g);
                    System.arraycopy(h, 0, C, 0, 24);
                    double fade = !ghost || i < 2 ? 1.0 : half == 0 ? THIGH : SHIN;
                    box(skin, glow, place, FACES[3 + i * 2 + half][outer], alpha * fade, ghost, scale(shell, fade));
                }
            }
        }
    }

    static int scale(int argb, double by) {
        int alpha = (int) Math.round(((argb >>> 24) & 255) * by);
        return alpha << 24 | argb & 0xFFFFFF;
    }

    /** One box's faces ({@link #C}, the player's frame) placed, into the skin or as a shell. */
    static void box(Skin skin, FireMesh.Sink glow, StyleShow.Place place, int[][] faces, double alpha, boolean ghost, int shell) {
        double mx = 0;
        double my = 0;
        double mz = 0;
        for (int k = 0; k < 8; k++) {
            F3[0] = C[k * 3];
            F3[1] = C[k * 3 + 1];
            F3[2] = C[k * 3 + 2];
            place.put(F3, O3);
            W[k * 3] = O3[0];
            W[k * 3 + 1] = O3[1];
            W[k * 3 + 2] = O3[2];
            mx += O3[0] / 8;
            my += O3[1] / 8;
            mz += O3[2] / 8;
        }
        if (glow != null) {
            for (int[] face : faces) {
                FireMesh.quad(glow, W[face[0] * 3], W[face[0] * 3 + 1], W[face[0] * 3 + 2], shell, W[face[1] * 3], W[face[1] * 3 + 1],
                    W[face[1] * 3 + 2], shell, W[face[2] * 3], W[face[2] * 3 + 1], W[face[2] * 3 + 2], shell, W[face[3] * 3], W[face[3] * 3 + 1],
                    W[face[3] * 3 + 2], shell);
            }
            return;
        }
        int argb = ghost ? FireMesh.argb(WASH_ALPHA * alpha, WASH) : FireMesh.argb(alpha, 0xFFFFFF);
        for (int[] face : faces) {
            for (int k = 0; k < 4; k++) {
                System.arraycopy(W, face[k] * 3, XYZ, k * 3, 3);
            }
            UV[0] = face[4] / 64.0F;
            UV[1] = face[5] / 64.0F;
            UV[2] = face[6] / 64.0F;
            UV[3] = face[5] / 64.0F;
            UV[4] = face[6] / 64.0F;
            UV[5] = face[7] / 64.0F;
            UV[6] = face[4] / 64.0F;
            UV[7] = face[7] / 64.0F;
            // the face's normal, out of the box
            double ax = XYZ[3] - XYZ[0];
            double ay = XYZ[4] - XYZ[1];
            double az = XYZ[5] - XYZ[2];
            double bx = XYZ[9] - XYZ[0];
            double by = XYZ[10] - XYZ[1];
            double bz = XYZ[11] - XYZ[2];
            double nx = ay * bz - az * by;
            double ny = az * bx - ax * bz;
            double nz = ax * by - ay * bx;
            double nl = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (nl < 1.0E-12) {
                nx = 0;
                ny = 1;
                nz = 0;
                nl = 1;
            }
            double cx = (XYZ[0] + XYZ[6]) * 0.5 - mx;
            double cy = (XYZ[1] + XYZ[7]) * 0.5 - my;
            double cz = (XYZ[2] + XYZ[8]) * 0.5 - mz;
            double out = nx * cx + ny * cy + nz * cz < 0 ? -1.0 / nl : 1.0 / nl;
            // only the faces turned to the eye (the eye is the origin here): a see-through spirit shows one skin, not its far side through
            // its near one (culled here, not by the device: the owner's phone draws everything both sides)
            if ((nx * (XYZ[0] + XYZ[6]) + ny * (XYZ[1] + XYZ[7]) + nz * (XYZ[2] + XYZ[8])) * out > 0.0) {
                continue;
            }
            skin.quad(XYZ, UV, argb, nx * out, ny * out, nz * out);
        }
    }

    static void head(float[][] pose, double grow) {
        for (int k = 0; k < 8; k++) {
            double[] p = StyleRig.head(pose, (k & 1) == 0 ? -4.0 - grow : 4.0 + grow, (k & 2) == 0 ? -8.0 - grow : grow,
                (k & 4) == 0 ? -4.0 - grow : 4.0 + grow);
            System.arraycopy(p, 0, C, k * 3, 3);
        }
    }

    static void torso(float[][] pose, int top, int bottom, double grow) {
        for (int k = 0; k < 8; k++) {
            double y = (k & 2) == 0 ? top - (top == 0 ? grow : 0.0) : bottom + (bottom == 12 ? grow : 0.0);
            double[] p = StyleRig.torso(pose, (k & 1) == 0 ? -4.0 - grow : 4.0 + grow, y, (k & 4) == 0 ? -2.0 - grow : 2.0 + grow);
            System.arraycopy(p, 0, C, k * 3, 3);
        }
    }
}
