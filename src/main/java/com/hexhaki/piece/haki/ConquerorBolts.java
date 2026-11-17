package com.hexhaki.piece.haki;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * 2.1.1: the Conqueror's black lightning that tears out of a charging or releasing player (pure, tested). It replaces the body of the
 * original {@code ConquerorLightningPath.generate} through {@link LightningPaths}, keeping its contract: the same inputs (charge 0..1,
 * Conqueror mastery 0..1000, a seed, dome or not), bolts relative to the player's feet that start round the body (2.2.6: on the shell,
 * {@link #SHELL_OUT} out) and run out to the original's radius, and a pure function of the seed, so the server's contact stuns land exactly
 * where every client draws the bolts.
 *
 * <p>What changed from the original: every bolt arcs (it bows sideways and lifts off the ground in its middle instead of running
 * straight), crackles (midpoint displacement, {@link Jag}), and splits (forks peel off the main bolts and die out fast), and the count
 * grows with charge and mastery. The old bolts were 4 to 9 points jittered along a straight line.
 *
 * <p>2.1.8: the bolts no longer fan round the player on the ground: they go out along directions spread over the whole sphere, a fresh
 * set every time ({@link Sphere}), to the radius: up into the sky, level, or down, a bolt that goes down striking the ground and racing
 * along it outward. The server stuns along the very same bolts.
 */
public final class ConquerorBolts {
    /** Longest piece of a main bolt before it is broken again, in blocks. */
    public static final double PIECE = 2.6;
    /** Most points on one stretch of a main bolt (the original's longest had 10); a bolt that strikes the ground has two stretches. */
    public static final int MOST_POINTS = 17;
    /** 2.1.8: the sphere's centre, the player's middle (blocks above the feet). */
    public static final double CENTRE = 1.0;
    /** Lowest a point may run (the bolts skim the ground, never through it), and the most a bolt bows up in its middle. */
    public static final double FLOOR = 0.1;
    public static final double MOST_LIFT = 1.4;
    /** Up-and-down crackle as a share of the sideways crackle. */
    public static final double FLAT = 0.3;
    /**
     * 2.2.6 (the owner, drawing a dome round the player with the bolts tearing out of it: "Haki is not meant to come directly from the
     * player's body"): the shell the lightning comes out of, an egg round the body centred on {@link #CENTRE}: this far out level with
     * its middle, this far above (and below: the ground cuts it) its middle.
     */
    public static final double SHELL_OUT = 2.0;
    public static final double SHELL_UP = 1.9;
    /** Where a root on the ground sits (the shell's foot, a ring round the player). */
    static final double GROUND_ROOT = FLOOR + 0.15;

    private ConquerorBolts() {
    }

    /** How many main bolts: a dome sends 3..7 round the player, a plain charge 1..3. */
    public static int count(float charge, int mastery, boolean dome) {
        float c = clampCharge(charge);
        float m = Math.max(0.0F, Math.min(1.0F, mastery / 1000.0F));
        if (dome) {
            return Math.max(3, Math.min(7, 3 + (int) Math.floor(c * 2.0F + m * 2.0F)));
        }
        return 1 + (c >= 0.62F || m >= 0.8F ? 1 : 0) + (c >= 0.95F && m >= 0.6F ? 1 : 0);
    }

    static float clampCharge(float charge) {
        return Math.max(0.04F, Math.min(1.0F, charge));
    }

    /** Every bolt (main bolts first, then forks) as a flattened xyz polyline relative to the player's feet. */
    public static List<double[]> generate(float charge, int mastery, long seed, boolean dome, double radius) {
        float c = clampCharge(charge);
        SplittableRandom random = new SplittableRandom(seed ^ (dome ? 0x5245_4744_4F4D_45L : 0x4348_4152_4745L));
        int count = count(charge, mastery, dome);
        // 2.1.8 (the owner: "The lightning should spread in a perfect sphere randomly every single time, being able to target random
        // areas of that sphere"): the bolts go out along directions spread over the whole sphere, fresh every time (Sphere)
        double[] directions = Sphere.directions(count, random);
        List<double[]> mains = new ArrayList<>();
        List<double[]> forks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double dx = directions[i * 3];
            double dy = directions[i * 3 + 1];
            double dz = directions[i * 3 + 2];
            double flat = Math.hypot(dx, dz);
            double hx;
            double hz;
            if (flat < 0.15) {
                double a = random.nextDouble() * Math.PI * 2.0;
                hx = Math.cos(a);
                hz = Math.sin(a);
            } else {
                hx = dx / flat;
                hz = dz / flat;
            }
            // 2.2.6: out of the shell round the body (a little in or out of it), never out of the body itself
            double[] root = shell(dx, dy, dz, 0.96 + random.nextDouble() * 0.1);
            if (root[1] < GROUND_ROOT) {
                root = ring(hx, hz);
            }
            double reach = Math.max(radius * (0.82 + random.nextDouble() * 0.18), Math.hypot(root[0], root[2]) + 2.0);
            double[] tip = {dx * reach, CENTRE + dy * reach, dz * reach};
            double[] bolt;
            if (tip[1] >= FLOOR + 0.05) {
                // up into the air, or out level: straight out along its line, bowing as it goes
                bolt = arc(root, tip, random, dome ? 0.15 : 0.1, (dome ? 0.09 : 0.06) * (1.0 - Math.abs(dy)), c);
            } else {
                // down: it strikes the ground where its line meets it (never inside the shell), then races along the ground outward
                double t = (CENTRE - FLOOR) / -dy;
                double rootOut = Math.hypot(root[0], root[2]);
                double out = Math.max(Math.hypot(dx * t, dz * t), rootOut + 0.4);
                double[] hit = {hx * out, FLOOR, hz * out};
                double run = Math.max(radius * 0.3, Math.min(radius - out, reach - t));
                double[] end = {hx * (out + run), FLOOR + random.nextDouble() * 0.5, hz * (out + run)};
                double[] along = arc(hit, end, random, dome ? 0.15 : 0.1, dome ? 0.09 : 0.06, c);
                if (root[1] <= GROUND_ROOT + 1.0E-9) {
                    // a root on the shell's foot: it runs out along the ground from there
                    along = arc(root, end, random, dome ? 0.15 : 0.1, dome ? 0.09 : 0.06, c);
                    bolt = along;
                } else {
                    double[] down = arc(root, hit, random, 0.05, 0.0, c);
                    bolt = new double[down.length + along.length - 3];
                    System.arraycopy(down, 0, bolt, 0, down.length);
                    System.arraycopy(along, 3, bolt, down.length, along.length - 3);
                }
            }
            mains.add(bolt);
            int forkCount = dome ? 1 + (random.nextDouble() < 0.6 ? 1 : 0) : (c >= 0.58F ? 1 : 0);
            for (int f = 0; f < forkCount; f++) {
                forks.add(fork(bolt, random, f));
            }
        }
        List<double[]> all = new ArrayList<>(mains);
        all.addAll(forks);
        return all;
    }

    /** 2.2.6: the point of the shell along the direction (dx, dy, dz) from its centre (unit), {@code scale} times as far. */
    public static double[] shell(double dx, double dy, double dz, double scale) {
        double r = scale / Math.sqrt((dx * dx + dz * dz) / (SHELL_OUT * SHELL_OUT) + dy * dy / (SHELL_UP * SHELL_UP));
        return new double[] {dx * r, CENTRE + dy * r, dz * r};
    }

    /** 2.2.6: the shell's foot along the heading (hx, hz): where it meets the ground round the player. */
    static double[] ring(double hx, double hz) {
        double k = (GROUND_ROOT - CENTRE) / SHELL_UP;
        double out = SHELL_OUT * Math.sqrt(Math.max(0.0, 1.0 - k * k));
        return new double[] {hx * out, GROUND_ROOT, hz * out};
    }

    /** 2.2.6: how far a point (relative to the feet) is through the shell: under 1 inside it, 1 on it. */
    public static double through(double x, double y, double z) {
        double h = (x * x + z * z) / (SHELL_OUT * SHELL_OUT);
        double v = (y - CENTRE) / SHELL_UP;
        return Math.sqrt(h + v * v);
    }

    /**
     * 2.2.6: a stroke's points (relative to the feet of the player it plays on) moved off the body: every point inside the shell goes out
     * onto it along the line from the shell's middle (above the ground), so lightning laid on the body crawls over the shell round it
     * instead. The same points move the same way, so the strokes of one bolt stay one bolt. {@code points} itself is never changed.
     */
    public static double[] offBody(double[] points) {
        double[] out = null;
        for (int i = 0; i < points.length / 3; i++) {
            double x = points[i * 3];
            double y = points[i * 3 + 1];
            double z = points[i * 3 + 2];
            double e = through(x, y, z);
            if (e >= 1.0) {
                continue;
            }
            if (out == null) {
                out = points.clone();
            }
            double dx = x;
            double dy = y - CENTRE;
            double dz = z;
            if (e < 1.0E-6) {
                dy = 1.0;
                e = 1.0 / SHELL_UP;
            }
            double k = 1.0 / Math.max(1.0E-6, e);
            double onto = CENTRE + dy * k;
            if (onto >= GROUND_ROOT) {
                out[i * 3] = dx * k;
                out[i * 3 + 1] = onto;
                out[i * 3 + 2] = dz * k;
                continue;
            }
            // low by the feet: out level to the shell's foot (the ground cuts the shell there), never under the ground
            double low = Math.max(GROUND_ROOT, y0(onto, points[i * 3 + 1]));
            double v = (low - CENTRE) / SHELL_UP;
            double reach = SHELL_OUT * Math.sqrt(Math.max(0.0, 1.0 - v * v));
            double flat = Math.hypot(dx, dz);
            out[i * 3] = flat < 1.0E-9 ? reach : dx / flat * reach;
            out[i * 3 + 1] = low;
            out[i * 3 + 2] = flat < 1.0E-9 ? 0.0 : dz / flat * reach;
        }
        return out == null ? points : out;
    }

    /** The height a low point keeps: its own, if it is above the shell's foot. */
    static double y0(double projected, double own) {
        return Math.max(projected, own);
    }

    /**
     * A bolt from {@code root} to {@code tip}: first a bowed spine (sideways by {@code bow} of its length, lifted by {@code lift} in the
     * middle), then each spine piece crackled.
     */
    static double[] arc(double[] root, double[] tip, SplittableRandom random, double bow, double lift, float charge) {
        double dx = tip[0] - root[0];
        double dy = tip[1] - root[1];
        double dz = tip[2] - root[2];
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int knots = Math.max(2, Math.min(5, (int) Math.ceil(length / (PIECE * 3.0)) + 1));
        double side = (random.nextDouble() < 0.5 ? -1 : 1) * bow * length * (0.6 + random.nextDouble() * 0.4);
        double[] across = Jag.across(dx, 0, dz);
        List<double[]> spine = new ArrayList<>();
        for (int k = 0; k <= knots; k++) {
            double t = k / (double) knots;
            double bend = Math.sin(Math.PI * t);
            spine.add(new double[] {
                root[0] + dx * t + across[0] * side * bend,
                root[1] + dy * t + Math.min(MOST_LIFT, lift * length) * bend,
                root[2] + dz * t + across[2] * side * bend});
        }
        double rough = 0.16 + 0.08 * charge;
        List<Double> flat = new ArrayList<>();
        int budget = MOST_POINTS - 1;
        for (int k = 0; k < knots; k++) {
            double[] a = spine.get(k);
            double[] b = spine.get(k + 1);
            double piece = distance(a, b);
            int levels = Math.min(Jag.levels(piece), log2(Math.max(1, budget / (knots - k))));
            double[] jagged = Jag.path(a, b, levels, rough, random.nextLong(), FLAT);
            int points = jagged.length / 3;
            for (int p = k == 0 ? 0 : 1; p < points; p++) {
                flat.add(jagged[p * 3]);
                flat.add(jagged[p * 3 + 1]);
                flat.add(jagged[p * 3 + 2]);
            }
            budget -= points - 1;
        }
        double[] out = new double[flat.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = flat.get(i);
        }
        for (int i = 1; i < out.length; i += 3) {
            out[i] = Math.max(FLOOR, out[i]);
        }
        out[0] = root[0];
        out[1] = root[1];
        out[2] = root[2];
        out[out.length - 3] = tip[0];
        out[out.length - 2] = tip[1];
        out[out.length - 1] = tip[2];
        return out;
    }

    /** A fork off {@code bolt}: from a point 30..70% along it, peeling away at 25..50 degrees in any direction, a fifth to a third of it long. */
    static double[] fork(double[] bolt, SplittableRandom random, int index) {
        int points = bolt.length / 3;
        int at = Math.max(1, Math.min(points - 2, (int) Math.round(points * (0.3 + random.nextDouble() * 0.4))));
        double[] from = {bolt[at * 3], bolt[at * 3 + 1], bolt[at * 3 + 2]};
        double dx = bolt[(at + 1) * 3] - from[0];
        double dy = bolt[(at + 1) * 3 + 1] - from[1];
        double dz = bolt[(at + 1) * 3 + 2] - from[2];
        double dl = Math.max(1.0E-9, Math.sqrt(dx * dx + dy * dy + dz * dz));
        dx /= dl;
        dy /= dl;
        dz /= dl;
        double[] across = Jag.across(dx, dy, dz);
        double spin = random.nextDouble() * Math.PI * 2.0;
        double px = across[0] * Math.cos(spin) + across[3] * Math.sin(spin);
        double py = across[1] * Math.cos(spin) + across[4] * Math.sin(spin);
        double pz = across[2] * Math.cos(spin) + across[5] * Math.sin(spin);
        double turn = Math.toRadians(25 + random.nextDouble() * 25) * (index % 2 == 0 ? 1 : -1);
        double total = 0;
        for (int p = 0; p + 1 < points; p++) {
            total += distance(new double[] {bolt[p * 3], bolt[p * 3 + 1], bolt[p * 3 + 2]},
                new double[] {bolt[p * 3 + 3], bolt[p * 3 + 4], bolt[p * 3 + 5]});
        }
        double length = total * (0.2 + random.nextDouble() * 0.13);
        double[] tip = {
            from[0] + (dx * Math.cos(turn) + px * Math.sin(turn)) * length,
            Math.max(FLOOR, from[1] + (dy * Math.cos(turn) + py * Math.sin(turn)) * length),
            from[2] + (dz * Math.cos(turn) + pz * Math.sin(turn)) * length};
        double[] fork = Jag.path(from, tip, Math.min(2, Jag.levels(length)), 0.24, random.nextLong(), FLAT);
        for (int i = 1; i < fork.length; i += 3) {
            fork[i] = Math.max(FLOOR, fork[i]);
        }
        return fork;
    }

    static double distance(double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = b[2] - a[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    static int log2(int n) {
        int levels = 0;
        while ((1 << (levels + 1)) <= n) {
            levels++;
        }
        return levels;
    }
}
