package com.hexhaki.piece.haki;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * 2.2.5 (the owner: "At full charge, it should cause severe tears on the floor like fissures where the lightnings hit, then repair
 * themselves automaticly (same as HexGhoul)"): where the Conqueror's full-power storm tears the ground (pure, tested). The storm
 * ({@link ConquerorStorm}) is rebuilt on the server from the very seed the clients draw it from; every stretch of a strand that runs
 * on the ground ({@link ConquerorStorm.Strand#tear} above 0) becomes a fissure under it: a jagged slot across its line, widest and
 * deepest where the bolt first strikes near the player and narrowing as it races out, opened the tick the bolt's strike reaches it
 * (at {@link ConquerorStorm#SPEED}), with short side cracks breaking off it. The server finds the ground under each cut and tears the
 * blocks out ({@code server/Fissures}); they grow back on their own later.
 */
public final class Fissure {
    /** A cut's spacing along a fissure (blocks). */
    static final double STEP = 0.6;
    /**
     * At full tear: half the width (blocks) and the depth below the surface. 2.4.5 (the owner: "Conqueror Haki full release lags a
     * ton when releasing the full charge"): 1.5 and 3.5 (were 2.4 and 6): a release tore out about 10,000 blocks over 50 chunk
     * sections in under a second (700 a tick, each a light update and a chunk rebuild on every client near), and grew them all back
     * at once 15-25 s later; now about a third of that, spread thinner ({@code server/Scars}' budgets).
     */
    /**
     * 2.9.4 (the owner: "current Haki Conqueror's release is dealing a bit too much world destruction ... whenever they hits the floor,
     * like lines of like little small fissures instead of like these massive gaping holes ... just 2 lines wide max, and just varying
     * between 1 and 2 ... It just follows the lightning"): a crack is a line one to two blocks wide (half its width: 0.5 to 1; 1.5 and
     * up to 2.25 before), wandering only a little off the bolt's line.
     */
    public static final double HALF_WIDTH = 1.0;
    public static final double HALF_LEAST = 0.5;
    public static final double DEPTH = 3.5;

    /**
     * One slot of a fissure: at (x, z) blocks from the player's feet, across the line (nx, nz), {@code half} wide each side,
     * {@code depth} deep at its middle (shallower to its sides), opened {@code tick} ticks after the release.
     */
    public record Cut(double x, double z, double nx, double nz, double half, double depth, int tick) {
    }

    private Fissure() {
    }

    /** Every cut of a storm's fissures, in the order they open. */
    public static List<Cut> cuts(List<ConquerorStorm.Strand> storm, long seed) {
        List<Cut> out = new ArrayList<>();
        SplittableRandom random = new SplittableRandom(seed ^ 0x4649_5353L);
        for (ConquerorStorm.Strand strand : storm) {
            double[] p = strand.points();
            double walked = 0;
            for (int i = 0; i + 1 < strand.count(); i++) {
                double ax = p[i * 3];
                double az = p[i * 3 + 2];
                double bx = p[i * 3 + 3];
                double bz = p[i * 3 + 5];
                double dx = bx - ax;
                double dz = bz - az;
                double length = Math.sqrt(dx * dx + dz * dz + (p[i * 3 + 4] - p[i * 3 + 1]) * (p[i * 3 + 4] - p[i * 3 + 1]));
                double flat = Math.sqrt(dx * dx + dz * dz);
                double ta = strand.tear()[i];
                double tb = strand.tear()[i + 1];
                if (ta > 0 && tb > 0 && flat > 1.0E-6) {
                    double nx = -dz / flat;
                    double nz = dx / flat;
                    int samples = Math.max(1, (int) Math.ceil(flat / STEP));
                    for (int k = 0; k < samples; k++) {
                        double f = (k + 0.5) / samples;
                        double tear = ta + (tb - ta) * f;
                        // the crack's own jag: it wanders a little either side of the bolt's line, and pinches and gapes
                        double wobble = (random.nextDouble() - 0.5) * 0.4;
                        double gape = 0.85 + 0.15 * random.nextDouble();
                        double x = ax + dx * f + nx * wobble;
                        double z = az + dz * f + nz * wobble;
                        int tick = (int) Math.ceil(strand.delay() + (walked + length * f) / ConquerorStorm.SPEED);
                        double half = Math.min(HALF_WIDTH, Math.max(HALF_LEAST, (HALF_LEAST + (HALF_WIDTH - HALF_LEAST) * tear) * gape));
                        double depth = 1.0 + (DEPTH - 1.0) * tear * (0.8 + 0.4 * random.nextDouble());
                        out.add(new Cut(x, z, nx, nz, half, depth, tick));
                        if (tear > 0.25 && random.nextDouble() < 0.06) {
                            side(out, random, x, z, dx / flat, dz / flat, tear, tick);
                        }
                    }
                }
                walked += length;
            }
        }
        out.sort((a, b) -> Integer.compare(a.tick, b.tick));
        return out;
    }

    /** A side crack breaking off a fissure at (x, z): a few blocks long, angled forward, thin and shallow. */
    static void side(List<Cut> out, SplittableRandom random, double x, double z, double fx, double fz, double tear, int tick) {
        double sign = random.nextBoolean() ? 1 : -1;
        double angle = Math.toRadians(35 + 30 * random.nextDouble()) * sign;
        double cx = fx * Math.cos(angle) - fz * Math.sin(angle);
        double cz = fx * Math.sin(angle) + fz * Math.cos(angle);
        double length = 2.0 + 4.0 * random.nextDouble() * tear;
        int samples = (int) Math.ceil(length / STEP);
        for (int k = 1; k <= samples; k++) {
            double f = k / (double) samples;
            double half = (0.5 + 0.4 * tear) * (1.0 - 0.6 * f);
            double depth = (1.0 + 2.5 * tear) * (1.0 - 0.6 * f);
            out.add(new Cut(x + cx * length * f, z + cz * length * f, -cz, cx, half, depth, tick + 1 + (int) (f * 2)));
        }
    }

    /** How deep a cut goes {@code across} blocks from its middle (0 past its half width): a V, steep sided. */
    public static double depthAt(Cut cut, double across) {
        double k = Math.abs(across) / Math.max(1.0E-6, cut.half);
        return k >= 1.0 ? 0.0 : cut.depth * Math.sqrt(1.0 - k * k);
    }
}
