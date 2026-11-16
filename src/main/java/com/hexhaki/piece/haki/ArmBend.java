package com.hexhaki.piece.haki;

/**
 * 2.1.3: where a point of a bent arm is drawn (pure, tested): bendy-lib 4.0.0's own {@code applyBend} on an arm's cuboid, as HexGhoul's
 * verified {@code BodyFrame.bend} ports it. Arm space is the arm's own frame after {@code ModelPart.translateAndRotate} (blocks; y down
 * from the shoulder pivot): the box is x {@code minX..maxX} pixels, y -2..10, z -2..2, bent about its middle (y 4). The half nearer the
 * hand is sheared and turned about the box's centre by {@code amount} about the axis (cos a, 0, sin a); the upper half is only sheared,
 * so the seam stays closed. A negative amount flexes the forearm forward (to -z), as the animations write elbows.
 */
public final class ArmBend {
    static final float PX = 1.0F / 16.0F;

    private ArmBend() {
    }

    /** The point (x, y, z blocks, arm space) of an arm whose box spans {@code minX..maxX} pixels, bent by (axis, amount) radians. */
    public static float[] bent(float axis, float amount, float minX, float maxX, float x, float y, float z) {
        if (Math.abs(amount) < 1.0E-4) {
            return new float[] {x, y, z};
        }
        double qx = x / PX;
        double qy = y / PX;
        double qz = z / PX;
        double ax = Math.cos(axis);
        double az = Math.sin(axis);
        double cx = (minX + maxX) / 2.0;
        double cy = 4.0;
        double distA = qy - 10.0;
        double distB = qy + 2.0;
        double halfSize = 6.0;
        // normal = (0, 1, 0) x axis = (az, 0, -ax)
        double fromBend = -(az * (qx - cx) - ax * qz);
        double shear = Math.tan(amount / 2.0) * fromBend;
        if (Math.abs(distA) < Math.abs(distB)) {
            qy += -distA / halfSize * shear;
            double dx = qx - cx;
            double dy = qy - cy;
            double dz = qz;
            double c = Math.cos(amount);
            double s = Math.sin(amount);
            // Rodrigues about (ax, 0, az): k x d = (-az * dy, az * dx - ax * dz, ax * dy)
            double kd = ax * dx + az * dz;
            double rx = dx * c + (-az * dy) * s + ax * kd * (1 - c);
            double ry = dy * c + (az * dx - ax * dz) * s;
            double rz = dz * c + (ax * dy) * s + az * kd * (1 - c);
            qx = cx + rx;
            qy = cy + ry;
            qz = rz;
        } else {
            qy += -distB / halfSize * shear;
        }
        return new float[] {(float) (qx * PX), (float) (qy * PX), (float) (qz * PX)};
    }
}
