package com.hexhaki.piece.world;

import java.util.SplittableRandom;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 3.8.3: where the archipelago's rivers run (the owner: "every single river IS CONNECTED TO THE SEA no matter what, it's meant to be
 * made for a boat travel/exploration type of thing"). Pure, seeded by the world's seed; read by the density function
 * {@code hexhaki:river_lines} ({@link RiverLines}), whose value is the overworld's ridges (data/minecraft/worldgen/density_function/
 * overworld/ridges.json): 0 on a river's line, rising to 1 {@link #VALLEY} blocks from it, so the game's own valleys, river biomes and
 * river channels lie along these lines (the channel kept under the sea level along its whole line: offset.json's cap).
 *
 * <p>4.0.0: each island's own rivers (world/IslandField places the islands). Before, three families of endless wavy lines crossed the
 * whole world: every river reached the sea, but they cut straight across the islands, ran along just inside a coast and cut thin strips
 * off it, and where two met at a shallow angle they opened a lake over half an island. Now each island has one river crossing it from
 * sea to sea through its middle ({@link #TWO}: a big one has two, crossing once near its middle at {@link #CROSS_LEAST} to
 * {@link #CROSS_MOST} degrees). A river is a chord through the island, bent gently ({@link #BEND} of the island's radius at most), its
 * two ends well out at sea past the island's furthest coast and its shelf: it is open at both ends (no ring, no lake), it reaches the
 * sea both ways (a boat goes through), and it never runs along a coast (it crosses each coast once).
 */
public final class RiverField {
    /** How far from a river's line the valley reaches (blocks): the field rises from 0 on the line to 1 there. */
    public static final double VALLEY = 140.0;
    /** The island radius (blocks, before its lobes) from which an island has two rivers. */
    public static final double TWO = 380.0;
    /** The two rivers of an island cross at this many degrees at least and at most. */
    public static final double CROSS_LEAST = 60.0;
    public static final double CROSS_MOST = 120.0;
    /** How far a river passes off the island's middle, and how far it bends off its chord (shares of the island's radius). */
    public static final double OFF_MIDDLE = 0.3;
    public static final double BEND = 0.26;
    /** How many pieces a river's line is drawn in. */
    public static final int PIECES = 40;

    /** One river: its points along the line (x, z pairs), from sea to sea. */
    public record River(double[] points) {
        public int count() {
            return this.points.length / 2;
        }

        public double x(int i) {
            return this.points[i * 2];
        }

        public double z(int i) {
            return this.points[i * 2 + 1];
        }

        /** The distance from ({@code px, pz}) to this river's line (blocks). */
        public double distance(double px, double pz) {
            double best = Double.MAX_VALUE;
            for (int i = 0; i + 1 < this.count(); i++) {
                double ax = this.x(i);
                double az = this.z(i);
                double dx = this.x(i + 1) - ax;
                double dz = this.z(i + 1) - az;
                double l2 = dx * dx + dz * dz;
                double t = l2 < 1.0E-9 ? 0.0 : Math.max(0.0, Math.min(1.0, ((px - ax) * dx + (pz - az) * dz) / l2));
                double ex = ax + dx * t - px;
                double ez = az + dz * t - pz;
                best = Math.min(best, ex * ex + ez * ez);
            }
            return Math.sqrt(best);
        }
    }

    private static final River[] NONE = new River[0];
    /** Each cell's rivers, by world seed and cell (a few thousand kept: the world generation asks for the same cells over and over). */
    private static final ConcurrentHashMap<Long, River[]> KEPT = new ConcurrentHashMap<>();
    private static volatile long keptSeed;

    private RiverField() {
    }

    static long key(int i, int j) {
        return ((long) i << 32) ^ (j & 0xFFFFFFFFL);
    }

    /** The rivers of cell (i, j)'s island (none if the cell is open sea). */
    public static River[] rivers(int i, int j, long seed) {
        if (keptSeed != seed) {
            KEPT.clear();
            keptSeed = seed;
        }
        River[] kept = KEPT.get(key(i, j));
        if (kept != null) {
            return kept;
        }
        River[] made = make(i, j, seed);
        if (KEPT.size() > 4096) {
            KEPT.clear();
        }
        KEPT.put(key(i, j), made);
        return made;
    }

    static River[] make(int i, int j, long seed) {
        double[] is = IslandField.held(i, j, seed);
        if (is == null) {
            return NONE;
        }
        SplittableRandom random = new SplittableRandom(IslandField.mix(seed ^ 0x5249_5645_5253L, i, j));
        double r = is[2];
        int n = r >= TWO ? 2 : 1;
        River[] out = new River[n];
        double first = random.nextDouble() * Math.PI;
        for (int k = 0; k < n; k++) {
            double way = k == 0 ? first : first + Math.toRadians(CROSS_LEAST + (CROSS_MOST - CROSS_LEAST) * random.nextDouble());
            out[k] = chord(is, way, random);
        }
        return out;
    }

    /**
     * A river across the island {@code is} along {@code way} (radians): from the sea past one side, through the island near its middle,
     * out to the sea past the other, bent gently by two waves that vanish at its ends.
     */
    static River chord(double[] is, double way, SplittableRandom random) {
        double r = is[2];
        double reach = r * (1.0 + IslandField.LOBES[0] + IslandField.LOBES[1] + IslandField.LOBES[2]) + IslandField.SHELF * 0.6;
        double ux = Math.cos(way);
        double uz = Math.sin(way);
        double sx = -uz;
        double sz = ux;
        double off = (random.nextDouble() * 2.0 - 1.0) * OFF_MIDDLE * r;
        // three bends: a long sweep, a shorter swing, a small wiggle (their sizes add up to BEND of the radius at most)
        double a1 = (random.nextDouble() * 0.5 + 0.5) * (random.nextBoolean() ? 1 : -1) * BEND * r * 0.55;
        double a2 = (random.nextDouble() * 0.5 + 0.5) * (random.nextBoolean() ? 1 : -1) * BEND * r * 0.3;
        double a3 = (random.nextDouble() * 2.0 - 1.0) * BEND * r * 0.15;
        double p1 = random.nextDouble() * Math.PI * 2.0;
        double[] points = new double[(PIECES + 1) * 2];
        for (int k = 0; k <= PIECES; k++) {
            double t = (double) k / PIECES;
            double along = (t * 2.0 - 1.0) * reach;
            // the bends: a long one and a shorter one, both held to nothing at the ends (sin(pi t) envelope)
            double envelope = Math.sin(Math.PI * t);
            double side = off + envelope * (a1 * Math.sin(Math.PI * t * 1.5 + p1) + a2 * Math.sin(Math.PI * t * 3.5 + p1 * 1.7)
                + a3 * Math.sin(Math.PI * t * 7.0 + p1 * 2.3));
            points[k * 2] = is[0] + ux * along + sx * side;
            points[k * 2 + 1] = is[1] + uz * along + sz * side;
        }
        return new River(points);
    }

    /** The rivers' field at (x, z): 0 on a river, rising to 1 {@link #VALLEY} blocks off it (and 1 at sea, away from every island). */
    public static double field(double x, double z, long seed) {
        int ci = (int) Math.floor(x / IslandField.CELL);
        int cj = (int) Math.floor(z / IslandField.CELL);
        double best = VALLEY;
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                for (River river : rivers(ci + di, cj + dj, seed)) {
                    // only a river whose island is near enough to matter
                    int mid = river.count() / 2;
                    double reach = Math.hypot(river.x(0) - river.x(mid), river.z(0) - river.z(mid)) + VALLEY;
                    if (Math.hypot(x - river.x(mid), z - river.z(mid)) > reach + VALLEY) {
                        continue;
                    }
                    best = Math.min(best, river.distance(x, z));
                }
            }
        }
        return Math.min(1.0, best / VALLEY);
    }
}
