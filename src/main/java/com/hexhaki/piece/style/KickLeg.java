package com.hexhaki.piece.style;

/**
 * 2.8.2 (the owner: "give the current M1s a first person (some are missing, like 6 powers, black leg, water kungfu and iron first)"):
 * a leg as a style's animation poses it, for the first-person view (PlayerAnimator draws only the arms there; client/FirstPersonLegs
 * draws the kicking legs from this). Pure: the player's leg as the game's model has it (a 4 x 12 x 4 box hung from the hip, 1.9 px
 * either side of the middle, 12 px up), split at the knee so the animation's bend flexes it (bendy-lib's way: the shin turned back
 * about the knee), turned by the leg's pitch, yaw and roll (ModelPart's Z, then Y, then X), in the player's own frame (x to their
 * left, y up, z ahead, blocks, the feet at the origin, the game's 0.9375 player scale). Each half comes with its faces and the skin's
 * rows for them (64 x 64 skins: the leg at 0,16 or 16,48, the trousers at 0,32 or 0,48).
 */
public final class KickLeg {
    private KickLeg() {
    }

    public static final double SCALE = 0.9375;
    /** Model pixels. */
    static final double PX = 1.0 / 16.0;

    /**
     * The eight corners of one half of a leg ({@code right}, {@code lower} the shin), {@code grow} px bigger all round (the trousers),
     * in the player's frame: x, y, z each, corner k = x side (bit 0: 0 the leg's -x), y (bit 1: 0 the top of the half), z (bit 2: 0 the
     * front). Angles in degrees as the animation gives them.
     */
    public static double[] corners(boolean right, boolean lower, double pitch, double yaw, double roll, double bend, double grow) {
        double[] out = new double[24];
        double pivotX = right ? -1.9 : 1.9;
        double px = Math.toRadians(pitch);
        double py = Math.toRadians(yaw);
        double pz = Math.toRadians(roll);
        double kb = Math.toRadians(bend);
        for (int k = 0; k < 8; k++) {
            double x = (k & 1) == 0 ? -2.0 - grow : 2.0 + grow;
            double y = (k & 2) == 0 ? -grow : 6.0 + grow;
            double z = (k & 4) == 0 ? -2.0 - grow : 2.0 + grow;
            if (lower) {
                // the shin: turned back about the knee by the bend, then hung 6 px below the hip
                double y2 = y * Math.cos(kb) - z * Math.sin(kb);
                double z2 = y * Math.sin(kb) + z * Math.cos(kb);
                y = y2 + 6.0;
                z = z2;
            }
            // ModelPart: Z, then Y, then X (rotationZYX applied as Rz * Ry * Rx)
            double y1 = y * Math.cos(px) - z * Math.sin(px);
            double z1 = y * Math.sin(px) + z * Math.cos(px);
            double x2 = x * Math.cos(py) + z1 * Math.sin(py);
            double z2 = -x * Math.sin(py) + z1 * Math.cos(py);
            double x3 = x2 * Math.cos(pz) - y1 * Math.sin(pz);
            double y3 = x2 * Math.sin(pz) + y1 * Math.cos(pz);
            // model pixels (y down, the front -z, the hip at 12 px) to the player's frame
            double mx = (pivotX + x3) * PX;
            double my = (12.0 + y3) * PX;
            double mz = z2 * PX;
            out[k * 3] = mx * SCALE;
            out[k * 3 + 1] = (1.501 - my) * SCALE;
            out[k * 3 + 2] = -mz * SCALE;
        }
        return out;
    }

    /** PlayerAnimator turns the whole body about this height (blocks above the feet). */
    static final double PIVOT = 0.7;

    /**
     * One half of a leg ({@code part} 4 the right, 5 the left) where the body really has it, from a sampled pose ({@code [part][x, y, z,
     * pitch, yaw, roll, bend, weight]}, part 0 the body): posed and weighted as the animation has it, then the body's own move, lean and
     * spin carried as PlayerAnimator carries them (about {@link #PIVOT}: roll, then yaw, then pitch), in the player's frame as
     * {@link #corners} (the feet at the origin): first person draws the legs attached to the body, as third person shows them.
     */
    public static double[] onBody(int part, boolean lower, float[][] pose, double grow) {
        float w = pose[part][7];
        double[] c = corners(part == 4, lower, pose[part][3] * w, pose[part][4] * w, pose[part][5] * w, pose[part][6] * w, grow);
        float bw = pose[0][7];
        // the entity's own frame has x to the right and z behind: pitch and roll turn the other way in this one, yaw the same
        double pitch = Math.toRadians(-pose[0][3] * bw);
        double yaw = Math.toRadians(pose[0][4] * bw);
        double roll = Math.toRadians(-pose[0][5] * bw);
        double moveX = -pose[0][0] * bw;
        double moveY = pose[0][1] * bw;
        double moveZ = -pose[0][2] * bw;
        double cp = Math.cos(pitch);
        double sp = Math.sin(pitch);
        double cy = Math.cos(yaw);
        double sy = Math.sin(yaw);
        double cr = Math.cos(roll);
        double sr = Math.sin(roll);
        for (int k = 0; k < 8; k++) {
            double x = c[k * 3];
            double y = c[k * 3 + 1] - PIVOT;
            double z = c[k * 3 + 2];
            double y1 = y * cp - z * sp;
            double z1 = y * sp + z * cp;
            double x2 = x * cy + z1 * sy;
            double z2 = -x * sy + z1 * cy;
            double x3 = x2 * cr - y1 * sr;
            double y3 = x2 * sr + y1 * cr;
            c[k * 3] = x3 + moveX;
            c[k * 3 + 1] = y3 + PIVOT + moveY;
            c[k * 3 + 2] = z2 + moveZ;
        }
        return c;
    }

    /**
     * The faces of a half: four corner indices each (as {@link #corners} numbers them), then the skin's u0, v0, u1, v1 (pixels of a 64
     * x 64 skin) for the leg whose texture starts at ({@code u}, {@code v}); the hip's top face on the thigh only, the sole on the shin
     * only.
     */
    public static int[][] faces(boolean lower, int u, int v) {
        int row0 = v + 4 + (lower ? 6 : 0);
        int row1 = row0 + 6;
        java.util.List<int[]> out = new java.util.ArrayList<>();
        out.add(new int[] {0, 1, 3, 2, u + 4, row0, u + 8, row1});
        out.add(new int[] {5, 4, 6, 7, u + 12, row0, u + 16, row1});
        out.add(new int[] {1, 5, 7, 3, u + 8, row0, u + 12, row1});
        out.add(new int[] {4, 0, 2, 6, u, row0, u + 4, row1});
        if (!lower) {
            out.add(new int[] {4, 5, 1, 0, u + 4, v, u + 8, v + 4});
        } else {
            out.add(new int[] {2, 3, 7, 6, u + 8, v, u + 12, v + 4});
        }
        return out.toArray(new int[0][]);
    }

    /** Where a leg's skin starts on a 64 x 64 skin: the leg, then the trousers. */
    public static int[] skin(boolean right, boolean trousers) {
        if (right) {
            return trousers ? new int[] {0, 32} : new int[] {0, 16};
        }
        return trousers ? new int[] {0, 48} : new int[] {16, 48};
    }
}
