package com.hexhaki.piece.haki;

import java.util.SplittableRandom;

/**
 * 2.1.1: the shape of a Haki lightning bolt (pure, tested in {@code LightningTests}). A straight line from A to B is broken by midpoint
 * displacement: every level halves each piece and pushes the new middle sideways, across the line, by a share of that piece's length
 * that shrinks level by level. The result is the crackle of real lightning: big kinks first, small ones riding on them. The same
 * endpoints and seed always give the same bolt, so two layers drawn over one segment (the dark sheath and the red filament) stay
 * glued together, and server and client agree on where a bolt runs.
 */
public final class Jag {
    private Jag() {
    }

    /** Pieces a segment of this length is broken into, as a level count (each level doubles them). */
    public static int levels(double length) {
        if (length < 0.45) {
            return 0;
        }
        if (length < 1.6) {
            return 1;
        }
        if (length < 5.0) {
            return 2;
        }
        return 3;
    }

    /**
     * The bolt from {@code a} to {@code b} (xyz each) as {@code 2^levels + 1} points, flattened xyz. The ends are exactly {@code a} and
     * {@code b}. {@code rough} is how far the first middle may stray, as a share of the whole length (0.2 is a lively bolt).
     */
    public static double[] path(double[] a, double[] b, int levels, double rough, long seed) {
        return path(a, b, levels, rough, seed, 1.0);
    }

    /**
     * As {@link #path(double[], double[], int, double, long)}, the second sideways push scaled by {@code flat} (the first push is the
     * horizontal one for a level line): ground lightning crackles left and right far more than up and down.
     */
    public static double[] path(double[] a, double[] b, int levels, double rough, long seed, double flat) {
        int pieces = 1 << Math.max(0, levels);
        double[] points = new double[(pieces + 1) * 3];
        System.arraycopy(a, 0, points, 0, 3);
        System.arraycopy(b, 0, points, pieces * 3, 3);
        SplittableRandom random = new SplittableRandom(seed);
        double amplitude = rough;
        for (int step = pieces; step > 1; step /= 2) {
            for (int start = 0; start < pieces; start += step) {
                int end = start + step;
                int mid = start + step / 2;
                double sx = points[start * 3];
                double sy = points[start * 3 + 1];
                double sz = points[start * 3 + 2];
                double dx = points[end * 3] - sx;
                double dy = points[end * 3 + 1] - sy;
                double dz = points[end * 3 + 2] - sz;
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double[] across = across(dx, dy, dz);
                double u = random.nextDouble(-1.0, 1.0) * amplitude * length;
                double v = random.nextDouble(-1.0, 1.0) * amplitude * length * flat;
                // Lightning wanders: the middle also slides a little along the line, so kinks are never evenly spaced.
                double along = 0.5 + random.nextDouble(-0.12, 0.12);
                points[mid * 3] = sx + dx * along + across[0] * u + across[3] * v;
                points[mid * 3 + 1] = sy + dy * along + across[1] * u + across[4] * v;
                points[mid * 3 + 2] = sz + dz * along + across[2] * u + across[5] * v;
            }
            amplitude *= 0.58;
        }
        return points;
    }

    /** Two unit vectors across the direction (dx, dy, dz): {u, v} flattened. Any direction works, straight up included. */
    public static double[] across(double dx, double dy, double dz) {
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-9) {
            return new double[] {1, 0, 0, 0, 0, 1};
        }
        double fx = dx / length;
        double fy = dy / length;
        double fz = dz / length;
        // Cross with world up unless the line is nearly vertical, then with world x.
        double rx = Math.abs(fy) > 0.92 ? 1 : 0;
        double ry = Math.abs(fy) > 0.92 ? 0 : 1;
        double ux = fy * 0 - fz * ry;
        double uy = fz * rx - fx * 0;
        double uz = fx * ry - fy * rx;
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul;
        uy /= ul;
        uz /= ul;
        double vx = fy * uz - fz * uy;
        double vy = fz * ux - fx * uz;
        double vz = fx * uy - fy * ux;
        return new double[] {ux, uy, uz, vx, vy, vz};
    }

    /** A seed from the segment's ends (to 1/64 of a block) and a salt: both layers of one segment get the same one. */
    public static long seed(double ax, double ay, double az, double bx, double by, double bz, long salt) {
        long h = 0x9E3779B97F4A7C15L ^ salt;
        for (double value : new double[] {ax, ay, az, bx, by, bz}) {
            h = (h ^ Math.round(value * 64.0)) * 0xBF58476D1CE4E5B9L;
            h ^= h >>> 31;
        }
        return h;
    }

    /** Width along a bolt at {@code t} (0 at its root, 1 at its tip): full at the root, thinning to {@code tip}. */
    public static double taper(double t, double tip) {
        double clamped = Math.max(0.0, Math.min(1.0, t));
        return 1.0 - (1.0 - tip) * clamped * clamped * (3.0 - 2.0 * clamped) * 0.5 - (1.0 - tip) * clamped * 0.5;
    }
}
