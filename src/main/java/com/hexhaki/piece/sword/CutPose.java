package com.hexhaki.piece.sword;

/**
 * 2.2.1: a body's pose as the game drew it (pure): for each of a humanoid's six parts, where its pivot is (model pixels), how it is turned
 * (radians, vanilla's x, y, z) and scaled, exactly as {@code ModelPart.translateAndRotate} places it; and where a point of a part lands in
 * the finisher's space ({@link #place}). The client reads it off the model the body was just drawn with ({@code HumanoidModel}), so the
 * line and the pieces are where the body was, whatever it was doing.
 *
 * <p>Model space is vanilla's: pixels, y down, the body's front toward minus z. The finisher's space (HexGhoul's carcass space): blocks,
 * x to the body's left, y up from its feet, z its front; a turn about y by minus the body's yaw puts it in the world.
 */
public final class CutPose {
    /** The parts, in this order everywhere. */
    public static final String[] PARTS = {"head", "torso", "rightArm", "leftArm", "rightLeg", "leftLeg"};
    /** How high vanilla lifts the model off the feet ({@code LivingEntityRenderer}'s translate, blocks). */
    static final double LIFT = 1.501;

    /** Per part: pivot x, y, z (pixels), turn x, y, z (radians), scale x, y, z. */
    final double[][] parts = new double[PARTS.length][];

    /** A body standing straight, its arms at its sides: vanilla's own pivots. */
    public CutPose() {
        set("head", 0, 0, 0, 0, 0, 0);
        set("torso", 0, 0, 0, 0, 0, 0);
        set("rightArm", -5, 2, 0, 0, 0, 0);
        set("leftArm", 5, 2, 0, 0, 0, 0);
        set("rightLeg", -1.9, 12, 0, 0, 0, 0);
        set("leftLeg", 1.9, 12, 0, 0, 0, 0);
    }

    /** Sets {@code part}: its pivot (pixels) and turn (radians), unscaled. */
    public CutPose set(String part, double x, double y, double z, double xRot, double yRot, double zRot) {
        return set(part, x, y, z, xRot, yRot, zRot, 1, 1, 1);
    }

    /** Sets {@code part}: its pivot (pixels), turn (radians) and scale. */
    public CutPose set(String part, double x, double y, double z, double xRot, double yRot, double zRot, double xs, double ys, double zs) {
        this.parts[index(part)] = new double[] {x, y, z, xRot, yRot, zRot, xs, ys, zs};
        return this;
    }

    /** Part {@code part}'s values (pivot, turn, scale), as {@link #set} took them. */
    public double[] get(String part) {
        return this.parts[index(part)].clone();
    }

    static int index(String part) {
        for (int i = 0; i < PARTS.length; i++) {
            if (PARTS[i].equals(part)) {
                return i;
            }
        }
        throw new IllegalArgumentException(part);
    }

    /** A point of {@code part} (its own pixels) in model space (pixels): scaled, turned z * y * x, moved to the pivot, as vanilla does. */
    public double[] model(String part, double[] local) {
        double[] p = this.parts[index(part)];
        double[] v = {local[0] * p[6], local[1] * p[7], local[2] * p[8]};
        v = turn(v, p[3], p[4], p[5]);
        return new double[] {v[0] + p[0], v[1] + p[1], v[2] + p[2]};
    }

    /** A vector of {@code part} (its own axes) in model space: turned only. */
    public double[] axis(String part, double[] local) {
        double[] p = this.parts[index(part)];
        return turn(new double[] {local[0] * p[6], local[1] * p[7], local[2] * p[8]}, p[3], p[4], p[5]);
    }

    /** JOML's rotationZYX(z, y, x) applied to {@code v}: x first, then y, then z. */
    static double[] turn(double[] v, double x, double y, double z) {
        double cx = Math.cos(x);
        double sx = Math.sin(x);
        double y1 = v[1] * cx - v[2] * sx;
        double z1 = v[1] * sx + v[2] * cx;
        double x1 = v[0];
        double cy = Math.cos(y);
        double sy = Math.sin(y);
        double x2 = x1 * cy + z1 * sy;
        double z2 = -x1 * sy + z1 * cy;
        double cz = Math.cos(z);
        double sz = Math.sin(z);
        return new double[] {x2 * cz - y1 * sz, x2 * sz + y1 * cz, z2};
    }

    /**
     * Where a point of {@code part} (its own pixels) lands in the finisher's space (blocks: x the body's left, y up from its feet, z its
     * front), {@code scale} the renderer's own (a player's 0.9375, most mobs 1).
     */
    public double[] place(String part, double[] local, double scale) {
        return fromModel(model(part, local), scale);
    }

    /** A model-space point (pixels) in the finisher's space (blocks). */
    public static double[] fromModel(double[] m, double scale) {
        return new double[] {m[0] / 16.0 * scale, (LIFT - m[1] / 16.0) * scale, -m[2] / 16.0 * scale};
    }

    /** A model-space vector (pixels) in the finisher's space (blocks). */
    public static double[] vectorFromModel(double[] m, double scale) {
        return new double[] {m[0] / 16.0 * scale, -m[1] / 16.0 * scale, -m[2] / 16.0 * scale};
    }
}
