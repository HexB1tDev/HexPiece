package com.hexhaki.piece.sword;

/**
 * 2.5.4 (the owner: "the sword first person, if the sword is holstered the hand should be resting on the holster too, not just on the
 * players hand, the first persons are meant to reflect exactly what the player is doing"): the first-person view of a sheathed katana
 * at the hip (pure, the same steps for the game, client/sword/HolsterHand, and the preview, tools/holster_preview.py). Until 2.5.3 the
 * first-person hand carried the sheathed sword like an item; in third person the sword is at the left hip and the left hand rests on
 * its handle. Now first person shows that: the handle and guard rising from the bottom left of the view, the saya's mouth under the
 * guard, the left hand resting over the handle by the guard, as the game shows its own hands (anchored to the view, swaying with it).
 *
 * <p>Set by intent: where the guard is, which way the handle points, the edge up; where the fist rests on the handle, which way the
 * forearm runs, the back of the hand up. The turns are worked out from those ({@link #euler}). Steps are applied in order to the hand's
 * pose (the game's PoseStack): {@code {0, x, y, z}} translate (blocks), {@code {1, a}}, {@code {2, a}}, {@code {3, a}} rotate about x, y,
 * z (degrees). View space: x right, y up, the eye looking down -z.
 */
public final class HolsterView {
    public static final int MOVE = 0;
    public static final int TURN_X = 1;
    public static final int TURN_Y = 2;
    public static final int TURN_Z = 3;

    /** The guard (tsuba) in the view (blocks), the way the handle points from it, and the way the blade's edge faces (worn edge up). */
    static final double[] GUARD = {-0.52, -0.56, -1.0};
    static final double[] HANDLE = {0.78, 0.36, -0.52};
    static final double[] EDGE = {0.0, 1.0, 0.25};
    /** Where the fist rests: model px down the handle from the guard, and how far above its axis (the hand over the handle). */
    static final double REST = 5.0;
    static final double OVER = 1.6;
    /** The forearm's way (from the elbow to the fist) and the back of the hand's (the arm box's +x for the left arm: its outer side). */
    static final double[] FOREARM = {-0.2, 0.6, -0.78};
    static final double[] BACK_OF_HAND = {-0.2, 0.7, 0.68};
    /** The left arm's pivot and the fist's centre (model px), wide and slim, and the game's idle sway of the arm in its own hand pass. */
    static final float[] PIVOT_WIDE = {5.0F, 2.0F, 0.0F};
    static final float[] PIVOT_SLIM = {5.0F, 2.5F, 0.0F};
    static final float[] FIST_WIDE = {1.0F, 8.5F, 0.0F};
    static final float[] FIST_SLIM = {0.5F, 8.5F, 0.0F};
    static final float IDLE_ROLL = -0.1F;

    private HolsterView() {
    }

    /** How far the view sinks as the sword is taken up (the game's equip: 0 up, 1 down) and breathes ({@code age} ticks). */
    static double[] lift(float equip, float age) {
        return new double[] {0.0, -0.6 * equip + 0.006 * Math.sin(age * 0.075), 0.0};
    }

    /** The katana's frame in the view: model x, y, z (the edge to -x, the blade up +y) as columns. */
    static double[][] katanaFrame() {
        double[] y = norm(scale(HANDLE, -1));
        double[] edge = norm(sub(EDGE, scale(y, dot(EDGE, y))));
        double[] x = scale(edge, -1);
        double[] z = cross(x, y);
        return columns(x, y, z);
    }

    /** The left arm's frame in the view: model x (its outer side, the back of the hand), y (shoulder to fist), z, as columns. */
    static double[][] armFrame() {
        double[] y = norm(FOREARM);
        double[] x = norm(sub(BACK_OF_HAND, scale(y, dot(BACK_OF_HAND, y))));
        double[] z = cross(x, y);
        return columns(x, y, z);
    }

    /** The steps to the katana's frame (model px, scaled inside its renderer): the guard at the origin. */
    public static float[][] katana(float equip, float age) {
        double[] up = lift(equip, age);
        double[] t = euler(katanaFrame());
        return new float[][] {
            {MOVE, (float) (GUARD[0] + up[0]), (float) (GUARD[1] + up[1]), (float) (GUARD[2] + up[2])},
            {TURN_Y, (float) t[0]}, {TURN_X, (float) t[1]}, {TURN_Z, (float) t[2]},
        };
    }

    /** Where the fist's centre rests, in the view (blocks, no lift): {@link #REST} px down the handle, {@link #OVER} px above it. */
    static double[] fist() {
        double[][] k = katanaFrame();
        double[] handle = norm(HANDLE);
        double[] edgeUp = new double[] {-k[0][0], -k[1][0], -k[2][0]};
        return add(add(GUARD, scale(handle, REST / 16.0)), scale(edgeUp, OVER / 16.0));
    }

    /**
     * The steps to the frame the game's {@code PlayerRenderer.renderLeftHand} draws the left arm in (its part then moves to its pivot and
     * sways {@link #IDLE_ROLL}): the fist's centre at {@link #fist}, the arm along {@link #armFrame}.
     */
    public static float[][] arm(boolean slim, float equip, float age) {
        double[] up = lift(equip, age);
        double[] f = fist();
        double[] t = euler(armFrame());
        float[] pivot = slim ? PIVOT_SLIM : PIVOT_WIDE;
        float[] centre = slim ? FIST_SLIM : FIST_WIDE;
        return new float[][] {
            {MOVE, (float) (f[0] + up[0]), (float) (f[1] + up[1]), (float) (f[2] + up[2])},
            {TURN_Y, (float) t[0]}, {TURN_X, (float) t[1]}, {TURN_Z, (float) t[2]},
            {MOVE, -centre[0] / 16.0F, -centre[1] / 16.0F, -centre[2] / 16.0F},
            {TURN_Z, (float) Math.toDegrees(-IDLE_ROLL)},
            {MOVE, -pivot[0] / 16.0F, -pivot[1] / 16.0F, -pivot[2] / 16.0F},
        };
    }

    /** The turns (degrees) about y, then x, then z whose product is the rotation {@code r} (rows): r = Ry(a) Rx(b) Rz(c). */
    static double[] euler(double[][] r) {
        double b = Math.asin(Math.max(-1.0, Math.min(1.0, -r[1][2])));
        double a = Math.atan2(r[0][2], r[2][2]);
        double c = Math.atan2(r[1][0], r[1][1]);
        return new double[] {Math.toDegrees(a), Math.toDegrees(b), Math.toDegrees(c)};
    }

    /** One step as a 4x4 matrix (rows), as the game's PoseStack applies it. */
    public static double[][] step(float[] s) {
        double[][] m = identity();
        if (s[0] == MOVE) {
            m[0][3] = s[1];
            m[1][3] = s[2];
            m[2][3] = s[3];
            return m;
        }
        if (s[0] == DrawView.ROTATE) {
            // 2.5.6: about any axis (Rodrigues)
            double a = Math.toRadians(s[1]);
            double l = Math.sqrt(s[2] * s[2] + s[3] * s[3] + s[4] * s[4]);
            double x = s[2] / l;
            double y = s[3] / l;
            double z = s[4] / l;
            double c = Math.cos(a);
            double n = Math.sin(a);
            double t = 1 - c;
            m[0][0] = t * x * x + c;
            m[0][1] = t * x * y - n * z;
            m[0][2] = t * x * z + n * y;
            m[1][0] = t * x * y + n * z;
            m[1][1] = t * y * y + c;
            m[1][2] = t * y * z - n * x;
            m[2][0] = t * x * z - n * y;
            m[2][1] = t * y * z + n * x;
            m[2][2] = t * z * z + c;
            return m;
        }
        double a = Math.toRadians(s[1]);
        double c = Math.cos(a);
        double n = Math.sin(a);
        if (s[0] == TURN_X) {
            m[1][1] = c;
            m[1][2] = -n;
            m[2][1] = n;
            m[2][2] = c;
        } else if (s[0] == TURN_Y) {
            m[0][0] = c;
            m[0][2] = n;
            m[2][0] = -n;
            m[2][2] = c;
        } else {
            m[0][0] = c;
            m[0][1] = -n;
            m[1][0] = n;
            m[1][1] = c;
        }
        return m;
    }

    /** The steps multiplied out (rows). */
    public static double[][] matrix(float[][] steps) {
        double[][] m = identity();
        for (float[] s : steps) {
            m = mul(m, step(s));
        }
        return m;
    }

    static double[][] identity() {
        return new double[][] {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
    }

    public static double[][] mul(double[][] a, double[][] b) {
        double[][] out = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    out[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return out;
    }

    static double[][] columns(double[] x, double[] y, double[] z) {
        return new double[][] {{x[0], y[0], z[0]}, {x[1], y[1], z[1]}, {x[2], y[2], z[2]}};
    }

    static double[] add(double[] a, double[] b) {
        return new double[] {a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    static double[] sub(double[] a, double[] b) {
        return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    static double[] scale(double[] a, double k) {
        return new double[] {a[0] * k, a[1] * k, a[2] * k};
    }

    static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static double len(double[] a) {
        return Math.sqrt(dot(a, a));
    }

    static double[] norm(double[] a) {
        double l = Math.sqrt(dot(a, a));
        return scale(a, 1.0 / l);
    }
}
