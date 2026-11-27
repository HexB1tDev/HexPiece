package com.hexhaki.piece.world;

/**
 * 3.9.5 (the owner: "The world generation is not adapting to Terra Blender and Biomes of Plenty well ... the islands are way too fucking
 * close, sometimes merged together"; before: "should be more apart, you should need to travel to find islands, theres too many"): the
 * archipelago's islands placed, not left to a noise. The world is cut into cells {@link #CELL} blocks wide; most cells hold one island
 * ({@link #EMPTY} of them none: open sea), its middle jittered at most {@link #JITTER} of a cell from the cell's middle, its size and its
 * coast's shape seeded (a lobed outline, {@link #LOBES}). Each island's size is held to {@link #APART} of the way to its nearest
 * neighbour less {@link #GAP}, so two islands are always far apart and can never merge, whatever biome mods (TerraBlender, Biomes O'
 * Plenty) place on them. Pure: the density function {@code hexhaki:island_field} (world/IslandLines) reads it per column, the tests
 * check it.
 *
 * <p>Its value: {@link #SEA} out at sea, rising across a shelf {@link #SHELF} blocks wide to {@link #COAST} at the coast and on to
 * {@link #PEAK} inland: data/hexhaki/worldgen/density_function/islands.json adds a little of the vanilla continents for a ragged coast,
 * and overworld/continents.json widens the coast band into the long beaches.
 */
public final class IslandField {
    public static final double CELL = 2800.0;
    public static final double JITTER = 0.5;
    public static final double EMPTY = 0.25;
    public static final double SMALLEST = 230.0;
    public static final double LARGEST = 760.0;
    /** The lobes' shares of the radius (2, 3 and 5 lobes round the island). */
    static final double[] LOBES = {0.2, 0.13, 0.07};
    public static final double APART = 0.5;
    public static final double GAP = 260.0;
    public static final double SHELF = 260.0;
    public static final double SEA = -0.95;
    public static final double COAST = -0.15;
    public static final double PEAK = 0.65;

    private IslandField() {
    }

    /** One cell's island: its middle (x, z), its radius (before the lobes), the lobes' phases; null if the cell is open sea. */
    static double[] island(int i, int j, long seed) {
        long h = mix(seed, i, j);
        if (unit(h, 0) < EMPTY) {
            return null;
        }
        double cx = (i + 0.5 + JITTER * (unit(h, 1) - 0.5)) * CELL;
        double cz = (j + 0.5 + JITTER * (unit(h, 2) - 0.5)) * CELL;
        double r = SMALLEST + (LARGEST - SMALLEST) * unit(h, 3) * unit(h, 4) * 1.6;
        return new double[] {cx, cz, Math.min(LARGEST, r), unit(h, 5) * 6.2832, unit(h, 6) * 6.2832, unit(h, 7) * 6.2832};
    }

    /** The island of cell (i, j) with its radius held clear of its neighbours (APART of the way to the nearest, less GAP). */
    public static double[] held(int i, int j, long seed) {
        double[] me = island(i, j, seed);
        if (me == null) {
            return null;
        }
        double nearest = Double.MAX_VALUE;
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                if (di == 0 && dj == 0) {
                    continue;
                }
                double[] other = island(i + di, j + dj, seed);
                if (other != null) {
                    nearest = Math.min(nearest, Math.hypot(other[0] - me[0], other[1] - me[1]));
                }
            }
        }
        double reach = 1.0 + LOBES[0] + LOBES[1] + LOBES[2];
        double most = (nearest * APART - GAP - SHELF) / reach;
        me[2] = Math.min(me[2], most);
        return me[2] < 80.0 ? null : me; // too hemmed in by its neighbours for an island: open sea
    }

    /** An island's coast's distance from its middle in the direction {@code angle} (its lobes). */
    static double radius(double[] is, double angle) {
        return is[2] * (1.0 + LOBES[0] * Math.sin(2.0 * angle + is[3]) + LOBES[1] * Math.sin(3.0 * angle + is[4])
            + LOBES[2] * Math.sin(5.0 * angle + is[5]));
    }

    /** The field at the column (x, z): SEA far out, COAST on a coast, up to PEAK inland. */
    public static double field(double x, double z, long seed) {
        int ci = (int) Math.floor(x / CELL);
        int cj = (int) Math.floor(z / CELL);
        double best = 0.0;
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                double[] is = held(ci + di, cj + dj, seed);
                if (is == null) {
                    continue;
                }
                double dx = x - is[0];
                double dz = z - is[1];
                double d = Math.sqrt(dx * dx + dz * dz);
                double r = radius(is, Math.atan2(dz, dx));
                if (d > r + SHELF) {
                    continue;
                }
                // 0 at the shelf's outer edge, 0.5 on the coast, 1 a shelf's width inland (eased)
                double p = Math.max(0.0, Math.min(1.0, 0.5 + (r - d) / (2.0 * SHELF)));
                best = Math.max(best, p * p * (3.0 - 2.0 * p));
            }
        }
        // the coast (p 0.5) at COAST: below it toward SEA, above it toward PEAK
        return best < 0.5 ? SEA + (COAST - SEA) * best * 2.0 : COAST + (PEAK - COAST) * (best - 0.5) * 2.0;
    }

    static long mix(long seed, int i, int j) {
        long h = seed ^ 0x9E3779B97F4A7C15L;
        h = (h ^ (i * 0xC2B2AE3D27D4EB4FL)) * 0x165667B19E3779F9L;
        h = (h ^ (j * 0x27D4EB2F165667C5L)) * 0x85EBCA77C2B2AE63L;
        return h ^ (h >>> 31);
    }

    /** The k-th uniform number 0..1 of a hash. */
    static double unit(long h, int k) {
        long x = h + k * 0x9E3779B97F4A7C15L;
        x = (x ^ (x >>> 30)) * 0xBF58476D1CE4E5B9L;
        x = (x ^ (x >>> 27)) * 0x94D049BB133111EBL;
        x ^= x >>> 31;
        return (x >>> 11) * 0x1.0p-53;
    }
}
