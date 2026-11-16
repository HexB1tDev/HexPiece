package com.hexhaki.piece.haki;

import java.util.SplittableRandom;

/**
 * 2.1.8: directions spread over a whole sphere (pure, tested). The owner: "The lightning should spread in a perfect sphere randomly every
 * single time, being able to target random areas of that sphere, currently there's spots where the lightning isn't being shot at".
 *
 * <p>{@code count} directions: the points of a Fibonacci sphere (each owning an equal share of the sphere's surface, so one release
 * leaves no big gap), each jittered inside its own share, then the whole set turned at random. Every release gets a fresh set from its
 * seed, so over many releases every part of the sphere is struck, up, round and down.
 */
public final class Sphere {
    static final double GOLDEN = Math.PI * (3.0 - Math.sqrt(5.0));

    private Sphere() {
    }

    /** {@code count} unit directions (x, y up, z), flattened. */
    public static double[] directions(int count, SplittableRandom random) {
        int n = Math.max(1, count);
        double[] out = new double[n * 3];
        double offset = random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < n; i++) {
            // the band this point owns (equal area), the point a little off its middle and a little off the lattice's turn
            double y = 1.0 - 2.0 * (i + 0.5 + random.nextDouble(-0.3, 0.3)) / n;
            double ring = Math.sqrt(Math.max(0.0, 1.0 - y * y));
            double phi = offset + i * GOLDEN + random.nextDouble(-0.2, 0.2) * GOLDEN;
            out[i * 3] = Math.cos(phi) * ring;
            out[i * 3 + 1] = y;
            out[i * 3 + 2] = Math.sin(phi) * ring;
        }
        // the whole set turned to a random orientation (a uniform random rotation: a random axis, a random angle)
        double[] axis = unit(random);
        double angle = random.nextDouble() * Math.PI * 2.0;
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        for (int i = 0; i < n; i++) {
            double x = out[i * 3];
            double y = out[i * 3 + 1];
            double z = out[i * 3 + 2];
            double dot = axis[0] * x + axis[1] * y + axis[2] * z;
            double cx = axis[1] * z - axis[2] * y;
            double cy = axis[2] * x - axis[0] * z;
            double cz = axis[0] * y - axis[1] * x;
            out[i * 3] = x * c + cx * s + axis[0] * dot * (1 - c);
            out[i * 3 + 1] = y * c + cy * s + axis[1] * dot * (1 - c);
            out[i * 3 + 2] = z * c + cz * s + axis[2] * dot * (1 - c);
        }
        return out;
    }

    /** A uniformly random unit vector. */
    public static double[] unit(SplittableRandom random) {
        double y = random.nextDouble(-1.0, 1.0);
        double phi = random.nextDouble() * Math.PI * 2.0;
        double r = Math.sqrt(1.0 - y * y);
        return new double[] {Math.cos(phi) * r, y, Math.sin(phi) * r};
    }
}
