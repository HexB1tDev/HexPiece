package com.hexhaki.piece.style;

/**
 * 2.8.3: where a fighting-style animation puts the player's body (pure; the same geometry as tools/anim.py, which the animations are
 * judged by, and PlayerAnimator + bendy-lib draw): every limb a 4 x 12 x 4 box bent at its middle (the lower half turned about the
 * joint by the part's bend), turned by its pitch, yaw and roll (ModelPart's Z, Y, X), the head and both arms carried by the body's bend
 * (about the mid-torso, + leans forward), then the whole body moved, leaned and spun about 0.7 blocks above the feet (PlayerAnimator's
 * order), scaled as the game scales the player (0.9375). Points come out in the player's frame: x to their left, y up, z ahead, blocks,
 * the feet at the origin. The style effects (client/fx/StyleVfx, style/StyleArt) follow the fist or foot that strikes from this.
 *
 * <p>A sampled pose is {@code [part][x, y, z, pitch, yaw, roll, bend, weight]}, parts body, head, rightArm, leftArm, rightLeg,
 * leftLeg (client/npc/StylePoser).
 */
public final class StyleRig {
    private StyleRig() {
    }

    public static final int BODY = 0;
    public static final int HEAD = 1;
    public static final int RIGHT_ARM = 2;
    public static final int LEFT_ARM = 3;
    public static final int RIGHT_LEG = 4;
    public static final int LEFT_LEG = 5;
    static final double SCALE = 0.9375;
    /** PlayerAnimator turns the whole body about this height (blocks above the feet). */
    static final double PIVOT = 0.7;
    /** Each limb in model pixels: its pivot (x, y), where its box runs along it (from, to), the box's x middle. */
    static final double[][] LIMB = {
        null, null,
        {-5.0, 2.0, -2.0, 10.0, -1.0},
        {5.0, 2.0, -2.0, 10.0, 1.0},
        {-1.9, 12.0, 0.0, 12.0, 0.0},
        {1.9, 12.0, 0.0, 12.0, 0.0},
    };

    /** A limb's joint (elbow, knee) along it (0 its top, 1 its end): bendy-lib bends at the box's middle. */
    public static double joint(int part) {
        return 0.5;
    }

    /**
     * The point {@code along} a limb (0 the shoulder or hip end of its box, 1 the hand or foot end; beyond 1 past it), {@code out} px off
     * its middle line toward its front (model -z before it is turned), in the player's frame.
     */
    public static double[] limb(float[][] pose, int part, double along, double out) {
        return limb(pose, part, 0.0, along, -out);
    }

    /** A point of a limb's box: {@code dx} px across it from its middle, {@code along} it, {@code dz} px (model z: + its back). */
    public static double[] limb(float[][] pose, int part, double dx, double along, double dz) {
        return limb(pose, part, dx, along, dz, along > 0.5);
    }

    /** {@code bent}: the point belongs to the lower half (its joint end turned with it). */
    static double[] limb(float[][] pose, int part, double dx, double along, double dz, boolean bent) {
        double[] l = LIMB[part];
        double w = pose[part][7];
        double y = l[2] + (l[3] - l[2]) * along;
        double mid = (l[2] + l[3]) * 0.5;
        double x = l[4] + dx;
        double z = dz;
        if (bent) {
            // the lower half turned back about the joint by the bend (bendy-lib)
            double b = Math.toRadians(pose[part][6] * w);
            double ly = y - mid;
            double y2 = ly * Math.cos(b) - z * Math.sin(b);
            double z2 = ly * Math.sin(b) + z * Math.cos(b);
            y = mid + y2;
            z = z2;
        }
        double[] p = turn(x, y, z, pose[part][3] * w, pose[part][4] * w, pose[part][5] * w);
        p[0] += l[0] + pose[part][0] * w;
        p[1] += l[1] + pose[part][1] * w;
        p[2] += pose[part][2] * w;
        if (part == RIGHT_ARM || part == LEFT_ARM) {
            carry(pose, p);
        }
        return place(pose, p);
    }

    /**
     * The eight corners of a limb's half ({@code lower} the forearm or shin), {@code grow} px bigger all round: corner k = x side (bit 0),
     * along (bit 1: 0 the top of the half), z (bit 2: 0 the front), x, y, z each in the player's frame.
     */
    public static double[] half(float[][] pose, int part, boolean lower, double grow) {
        double[] out = new double[24];
        double[] l = LIMB[part];
        double len = l[3] - l[2];
        double a0 = lower ? 0.5 : 0.0;
        double a1 = lower ? 1.0 : 0.5;
        for (int k = 0; k < 8; k++) {
            double dx = (k & 1) == 0 ? -2.0 - grow : 2.0 + grow;
            double along = (k & 2) == 0 ? a0 - (lower ? 0.0 : grow / len) : a1 + (lower ? grow / len : 0.0);
            double dz = (k & 4) == 0 ? -2.0 - grow : 2.0 + grow;
            double[] p = limb(pose, part, dx, along, dz, lower);
            out[k * 3] = p[0];
            out[k * 3 + 1] = p[1];
            out[k * 3 + 2] = p[2];
        }
        return out;
    }

    /** A point of the head (model px from the neck, y down, front -z), in the player's frame. */
    public static double[] head(float[][] pose, double x, double y, double z) {
        double w = pose[HEAD][7];
        double[] p = turn(x, y, z, pose[HEAD][3] * w, pose[HEAD][4] * w, pose[HEAD][5] * w);
        carry(pose, p);
        return place(pose, p);
    }

    /** A point of the torso (model px from the neck, y down, front -z): its upper half carried by the body's bend. */
    public static double[] torso(float[][] pose, double x, double y, double z) {
        double[] p = {x, y, z};
        if (y < 6.0) {
            carry(pose, p);
        }
        return place(pose, p);
    }

    /** ModelPart's turn: Z, then Y, then X ({@code rotationZYX}), degrees, on a model point. */
    static double[] turn(double x, double y, double z, double pitch, double yaw, double roll) {
        double px = Math.toRadians(pitch);
        double py = Math.toRadians(yaw);
        double pz = Math.toRadians(roll);
        double y1 = y * Math.cos(px) - z * Math.sin(px);
        double z1 = y * Math.sin(px) + z * Math.cos(px);
        double x2 = x * Math.cos(py) + z1 * Math.sin(py);
        double z2 = -x * Math.sin(py) + z1 * Math.cos(py);
        double x3 = x2 * Math.cos(pz) - y1 * Math.sin(pz);
        double y3 = x2 * Math.sin(pz) + y1 * Math.cos(pz);
        return new double[] {x3, y3, z2};
    }

    /** The body's bend: the head and arms (and the torso's upper half) turned about the mid-torso, + leaning forward. */
    static void carry(float[][] pose, double[] p) {
        double b = Math.toRadians(pose[BODY][6] * pose[BODY][7]);
        if (Math.abs(b) < 1.0E-6) {
            return;
        }
        double y = p[1] - 6.0;
        double z = p[2];
        p[1] = 6.0 + y * Math.cos(b) - z * Math.sin(b);
        p[2] = y * Math.sin(b) + z * Math.cos(b);
    }

    /** A model point (px, y down from the neck, front -z) to the player's frame, then the body's own move, lean and spin. */
    static double[] place(float[][] pose, double[] m) {
        double x = m[0] / 16.0 * SCALE;
        double y = (1.501 - m[1] / 16.0) * SCALE;
        double z = -m[2] / 16.0 * SCALE;
        float bw = pose[BODY][7];
        // the entity's own frame has x to the right and z behind: pitch and roll turn the other way in this one, yaw the same
        double pitch = Math.toRadians(-pose[BODY][3] * bw);
        double yaw = Math.toRadians(pose[BODY][4] * bw);
        double roll = Math.toRadians(-pose[BODY][5] * bw);
        double ry = y - PIVOT;
        double y1 = ry * Math.cos(pitch) - z * Math.sin(pitch);
        double z1 = ry * Math.sin(pitch) + z * Math.cos(pitch);
        double x2 = x * Math.cos(yaw) + z1 * Math.sin(yaw);
        double z2 = -x * Math.sin(yaw) + z1 * Math.cos(yaw);
        double x3 = x2 * Math.cos(roll) - y1 * Math.sin(roll);
        double y3 = x2 * Math.sin(roll) + y1 * Math.cos(roll);
        return new double[] {x3 - pose[BODY][0] * bw, y3 + PIVOT + pose[BODY][1] * bw, z2 - pose[BODY][2] * bw};
    }
}
