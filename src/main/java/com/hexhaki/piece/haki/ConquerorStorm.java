package com.hexhaki.piece.haki;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * 2.1.7: the Conqueror's full-power release as a storm (pure, tested in {@code LightningTests}). The owner: "the haki full power
 * explosion should be ridiculously bigger than what it currently is, lightning spreading further etc".
 *
 * <p>On top of the original's own release (its range lightning, burst and dome, which stay), a release at full charge tears the ground
 * open with lightning out to {@link #reach} (60 blocks, up to 120 at full mastery, further than the original's 50 at the very most):
 * <ul>
 *   <li><b>the sphere</b> (2.1.8): 8 to 13 huge bolts racing out from the player at {@link #SPEED} blocks a tick along directions spread
 *       over the whole sphere, a fresh set every release ({@link Sphere}): up into the sky, level, and down, the ones that go down
 *       striking the ground and racing along it outward; wandering, now and then a branch, thick and white-hot at the root, thinning far
 *       out (the owner, after a first dense try: "too much lightning way too much": few and far, not a carpet);</li>
 *   <li><b>the shockwave</b>: an inked ring sweeping out to the reach ({@code client/haki/HakiBolts} draws it from {@link #reach}).</li>
 * </ul>
 * Every piece is laid as the original lays its lightning (a black body, a red core, near the player a hot heart), through
 * {@link BoltPainter}, so it crackles, re-strikes and dances like the rest. Every point is given as a height above the ground under it:
 * the client lays the storm on the real terrain when it lets it go.
 */
public final class ConquerorStorm {
    public static final double SPEED = 12.0;
    public static final int BODY = 0xEC050307;
    public static final int CORE = 0xFFFF1433;
    public static final int HEART = 0xFFFF7889;
    /** The crackle's levels for a storm's pieces (16 kinks to a five-block piece). */
    public static final int LEVELS = 4;
    static final double STEP = 5.0;

    /** One beam of the storm: from {@code a} to {@code b} (x, height above the ground, z; blocks from the player's feet). */
    public record Call(double[] a, double[] b, float width, int color, int duration, float delay) {
    }

    private ConquerorStorm() {
    }

    /** How far the storm reaches at this Conqueror mastery (0..1000). */
    public static double reach(int mastery) {
        return 60.0 + 60.0 * Math.max(0.0, Math.min(1.0, mastery / 1000.0));
    }

    /** The whole storm of one full-power release. */
    public static List<Call> release(int mastery, long seed, boolean supreme) {
        double m = Math.max(0.0, Math.min(1.0, mastery / 1000.0));
        double reach = reach(mastery);
        SplittableRandom random = new SplittableRandom(seed ^ 0x5354_4F52_4DL);
        List<Call> out = new ArrayList<>();
        // 2.1.8 (the owner: "The lightning should spread in a perfect sphere randomly every single time, being able to target random areas
        // of that sphere"): the huge bolts go out over the whole sphere, a fresh set every release (Sphere); still few of them ("too much
        // lightning way too much")
        int count = 8 + (int) Math.round(3 * m) + (supreme ? 2 : 0);
        double[] directions = Sphere.directions(count, random);
        for (int i = 0; i < count; i++) {
            double[] dir = {directions[i * 3], directions[i * 3 + 1], directions[i * 3 + 2]};
            bolt(out, random, dir, reach * (0.75 + 0.25 * random.nextDouble()), (float) (random.nextDouble() * 1.5));
        }
        return out;
    }

    /**
     * One huge bolt from the player out along {@code dir}, {@code length} blocks: in pieces of about {@link #STEP}, wandering a little
     * round its line; one that goes down strikes the ground and races along it outward for the rest; now and then a branch.
     */
    static void bolt(List<Call> out, SplittableRandom random, double[] dir, double length, float delay) {
        double[] a = {dir[0] * 0.9, 1.2 + dir[1] * 0.3, dir[2] * 0.9};
        double[] h = dir.clone();
        double flat = Math.hypot(dir[0], dir[2]);
        double yaw = flat < 0.15 ? random.nextDouble() * Math.PI * 2.0 : Math.atan2(dir[2], dir[0]);
        boolean grounded = false;
        double walked = 0.0;
        while (walked < length - 0.5) {
            double step = Math.min(length - walked, STEP * (0.7 + 0.6 * random.nextDouble()));
            double[] b;
            if (!grounded) {
                double[] wander = Sphere.unit(random);
                h = normalise(new double[] {h[0] * 0.7 + dir[0] * 0.3 + wander[0] * 0.2, h[1] * 0.7 + dir[1] * 0.3 + wander[1] * 0.2,
                    h[2] * 0.7 + dir[2] * 0.3 + wander[2] * 0.2});
                b = new double[] {a[0] + h[0] * step, a[1] + h[1] * step, a[2] + h[2] * step};
                if (b[1] < 0.15) {
                    // it struck the ground: from here it runs along it, outward
                    grounded = true;
                    b[1] = 0.15 + 0.4 * random.nextDouble();
                }
            } else {
                yaw += random.nextDouble(-0.3, 0.3);
                b = new double[] {a[0] + Math.cos(yaw) * step, 0.15 + 0.6 * random.nextDouble() * random.nextDouble(), a[2] + Math.sin(yaw) * step};
            }
            double t = Math.min(1.0, walked / length);
            float at = delay + (float) (walked / SPEED);
            int life = 20 + random.nextInt(8);
            float body = (float) (0.46 - 0.36 * t);
            float core = (float) (0.15 - 0.115 * t);
            if (t < 0.5) {
                // far out the core's own ink is the outline: the black body only where the bolt is thick
                out.add(new Call(a, b, body, BODY, life, at));
            }
            out.add(new Call(a, b, core, CORE, life, at));
            if (t < 0.3) {
                out.add(new Call(a, b, core * 0.3F, HEART, life - 2, at));
            }
            if (walked > 8.0 && random.nextDouble() < 0.1) {
                branch(out, random, b, h, grounded, yaw, Math.min(length - walked, 6.0 + random.nextDouble() * 10.0), at + (float) (step / SPEED),
                    core * 0.6F);
            }
            walked += step;
            a = b;
        }
    }

    /** A branch off a bolt at {@code from}: two or three pieces peeling away from its heading, thin, never branching again. */
    static void branch(List<Call> out, SplittableRandom random, double[] from, double[] heading, boolean grounded, double yaw, double length,
                       float delay, float core) {
        double[] a = from;
        double[] wander = Sphere.unit(random);
        double[] h = normalise(new double[] {heading[0] + wander[0] * 0.8, heading[1] + wander[1] * 0.8, heading[2] + wander[2] * 0.8});
        double turn = yaw + random.nextDouble(-1.0, 1.0);
        int pieces = 2 + random.nextInt(2);
        for (int p = 0; p < pieces; p++) {
            double step = length / pieces;
            double[] b = grounded ? new double[] {a[0] + Math.cos(turn) * step, 0.15 + 0.4 * random.nextDouble(), a[2] + Math.sin(turn) * step}
                : new double[] {a[0] + h[0] * step, Math.max(0.15, a[1] + h[1] * step), a[2] + h[2] * step};
            float at = delay + (float) (p * step / SPEED);
            out.add(new Call(a, b, core * (1.0F - 0.25F * p), CORE, 16 + random.nextInt(6), at));
            a = b;
        }
    }

    static double[] normalise(double[] v) {
        double l = Math.max(1.0E-9, Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]));
        return new double[] {v[0] / l, v[1] / l, v[2] / l};
    }
}
