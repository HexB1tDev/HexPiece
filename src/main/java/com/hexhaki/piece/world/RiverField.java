package com.hexhaki.piece.world;

import java.util.SplittableRandom;

/**
 * 3.8.3: where the archipelago's rivers run (the owner: "every single river IS CONNECTED TO THE SEA no matter what, it's meant to be
 * made for a boat travel/exploration type of thing"). Pure, seeded by the world's seed; read by the density function
 * {@code hexhaki:river_lines} ({@link RiverLines}), whose value is the overworld's ridges (data/minecraft/worldgen/density_function/
 * overworld/ridges.json): 0 on a river's line, rising to 1 half way to the next, so the game's own valleys, river biomes and river
 * channels lie along these lines.
 *
 * <p>Why every river reaches the sea: the rivers of a family are the level lines {@code F = k S} of
 * {@code F(x, z) = x cos a + z sin a + W(x, z)}, where {@code W} is a sum of waves whose slopes add up to at most {@link #BENDS}
 * (0.85) &lt; 1. So the slope of {@code F} never falls under 0.15 anywhere: {@code F} has no hill top and no hollow, and a level line
 * of a function with neither can never close on itself (a closed line would ring a top or a hollow). Every river line runs on
 * without end; an island is bounded, so every river that enters an island leaves it again: both its ends are in the sea. Vanilla's
 * rivers are the zero lines of a noise, which close into rings (lakes cut off from the sea). Three families cross (a river network
 * of open triangles: a boat can turn at the crossings). The river channel is kept under the sea level along its whole line (offset.json's cap).
 */
public final class RiverField {
    /** The distance between two rivers of each of the three families (blocks, across their run). */
    public static final double SPACING_A = 1500.0;
    public static final double SPACING_B = 1900.0;
    public static final double SPACING_C = 2400.0;
    /** How much narrower the rivers' valleys are than the plain triangle between them (the value reaches 1 sooner). */
    public static final double NARROW = 6.0;
    /** The bends' waves: wavelengths (blocks) and each one's share of the slope budget (all together {@link #BENDS}). */
    static final double[] WAVES = {3100.0, 1650.0, 880.0, 430.0, 210.0};
    static final double[] SHARE = {0.30, 0.22, 0.16, 0.10, 0.07};
    public static final double BENDS = 0.85;

    /** One family of rivers: their way, their spacing, and their bends. */
    public record Family(double cos, double sin, double spacing, double phase, double[] kx, double[] kz, double[] amp, double[] shift) {
        /** {@code F} at (x, z). */
        public double f(double x, double z) {
            double w = 0.0;
            for (int i = 0; i < this.amp.length; i++) {
                w += this.amp[i] * Math.sin(this.kx[i] * x + this.kz[i] * z + this.shift[i]);
            }
            return x * this.cos + z * this.sin + w;
        }

        /** 0 on a river of this family, 1 half way between two. */
        public double distance(double x, double z) {
            double u = this.f(x, z) / this.spacing + this.phase;
            return Math.abs(u - Math.rint(u)) * 2.0;
        }
    }

    private static volatile long cachedSeed;
    private static volatile Family[] cached;

    private RiverField() {
    }

    /** The three families of a world's rivers. */
    public static Family[] families(long seed) {
        Family[] f = cached;
        if (f != null && cachedSeed == seed) {
            return f;
        }
        SplittableRandom random = new SplittableRandom(seed ^ 0x5249_5645_5253L);
        double a = random.nextDouble() * Math.PI;
        double b = a + Math.toRadians(50.0 + 20.0 * random.nextDouble());
        double c = b + Math.toRadians(50.0 + 20.0 * random.nextDouble());
        f = new Family[] {family(random, a, SPACING_A), family(random, b, SPACING_B), family(random, c, SPACING_C)};
        cached = f;
        cachedSeed = seed;
        return f;
    }

    static Family family(SplittableRandom random, double angle, double spacing) {
        int n = WAVES.length;
        double[] kx = new double[n];
        double[] kz = new double[n];
        double[] amp = new double[n];
        double[] shift = new double[n];
        for (int i = 0; i < n; i++) {
            double k = Math.PI * 2.0 / WAVES[i];
            double way = random.nextDouble() * Math.PI * 2.0;
            kx[i] = Math.cos(way) * k;
            kz[i] = Math.sin(way) * k;
            // each wave's slope is at most its share of the budget: amplitude x wavenumber
            amp[i] = SHARE[i] / k;
            shift[i] = random.nextDouble() * Math.PI * 2.0;
        }
        return new Family(Math.cos(angle), Math.sin(angle), spacing, random.nextDouble(), kx, kz, amp, shift);
    }

    /** The rivers' field at (x, z): 0 on a river, rising to 1 away from every river (narrowed by {@link #NARROW}). */
    public static double field(double x, double z, long seed) {
        Family[] f = families(seed);
        double d = Math.min(f[0].distance(x, z), Math.min(f[1].distance(x, z), f[2].distance(x, z))) * NARROW;
        return Math.min(1.0, d);
    }

    /** The most the bends' slopes add up to (the proof's number: under 1). */
    public static double slopeBound() {
        double sum = 0.0;
        for (double s : SHARE) {
            sum += s;
        }
        return sum;
    }
}
