package com.hexhaki.piece.gear;

/**
 * 3.4.1: Dawn Pistol's arm, never a straight line (the owner: "does the arm bend? I dont want it to be a straight line", then, tracing
 * Luffy's arm on their Egghead frame in green, "Excuse me??? How IS THIS IN HELL STRAIGHT": from Luffy the arm whips back first, swings
 * round in a loop and comes into Lucci fast, a long winding rubber path). Pure: the path from the shoulder to the end as {@link #KNOTS}
 * points, a looping trochoid in the plane of the way and one other direction (up and out to the arm's side): its forward progress
 * {@code u L - A sin(w u)} runs backward while {@code A w > L} (the loops), its offset {@code A (1 - cos(w u)) - 2 A u} returns to the
 * line at the end; {@code w} 3 pi: it leaves the shoulder going back (the whip back), loops once and arrives going forward fast, at the
 * top of a loop. The client draws the arm as a smooth chain of the original's curves through these points (client/gear/NikaPistolArm:
 * a quadratic B-spline, each piece the original's own limb mesh), the preview the same (tools/preview NikaShots).
 */
public final class NikaArc {
    public static final int KNOTS = 14;
    /** The loop's size over the arm's length, and the turn along it. */
    static final double LOOP = 0.18;
    static final double TURN = 3 * Math.PI;
    /** A short arm grows its loops in over this many blocks (it is not bent double). */
    static final double GROW_OVER = 2.0;

    private NikaArc() {
    }

    /**
     * The path from (sx, sy, sz) to (tx, ty, tz) into {@code out} ({@link #KNOTS} x 3), looping toward (hx, hy, hz) (unit, square to the
     * way: up and out); a degenerate way gives the straight line.
     */
    public static void path(double sx, double sy, double sz, double tx, double ty, double tz, double hx, double hy, double hz, double[] out) {
        double fx = tx - sx;
        double fy = ty - sy;
        double fz = tz - sz;
        double len = Math.sqrt(fx * fx + fy * fy + fz * fz);
        double a = len < 1.0E-4 ? 0.0 : LOOP * len * Math.min(1.0, len / GROW_OVER);
        if (len > 1.0E-4) {
            fx /= len;
            fy /= len;
            fz /= len;
        }
        for (int i = 0; i < KNOTS; i++) {
            double u = i / (double) (KNOTS - 1);
            double f = u * len - a * Math.sin(TURN * u);
            double h = a * (1 - Math.cos(TURN * u)) - 2 * a * u;
            out[i * 3] = sx + fx * f + hx * h;
            out[i * 3 + 1] = sy + fy * f + hy * h;
            out[i * 3 + 2] = sz + fz * f + hz * h;
        }
    }

    /**
     * The loop's direction for a way (fx, fy, fz) (any length): {@code up} and {@code out} mixed 0.8 to 0.6 and made square to the way, into
     * {@code h} (3); false when the way is too short to tell.
     */
    public static boolean plane(double fx, double fy, double fz, double ux, double uy, double uz, double ox, double oy, double oz, double[] h) {
        double fl = Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (fl < 1.0E-6) {
            return false;
        }
        fx /= fl;
        fy /= fl;
        fz /= fl;
        double x = 0.8 * ux + 0.6 * ox;
        double y = 0.8 * uy + 0.6 * oy;
        double z = 0.8 * uz + 0.6 * oz;
        double d = x * fx + y * fy + z * fz;
        x -= fx * d;
        y -= fy * d;
        z -= fz * d;
        double l = Math.sqrt(x * x + y * y + z * z);
        if (l < 1.0E-4) {
            // straight up (or along the side): any square direction
            x = Math.abs(fy) < 0.9 ? -fz : 1.0;
            y = 0.0;
            z = Math.abs(fy) < 0.9 ? fx : 0.0;
            l = Math.sqrt(x * x + z * z);
        }
        h[0] = x / l;
        h[1] = y / l;
        h[2] = z / l;
        return true;
    }
}
