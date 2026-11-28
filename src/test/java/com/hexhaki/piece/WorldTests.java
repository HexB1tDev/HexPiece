package com.hexhaki.piece;

import com.hexhaki.piece.world.RiverField;
import java.util.SplittableRandom;

/**
 * 3.8.3: the archipelago's rivers (world/RiverField; 4.0.0 each island's own): every river crosses its island from sea to sea (the
 * owner: "every single river IS CONNECTED TO THE SEA no matter what").
 */
public final class WorldTests {
    private WorldTests() {
    }

    public static void main(String[] args) {
        rivers();
        islands();
        Check.done("WorldTests");
    }

    /**
     * 3.9.5 (the owner: "the islands are way too fucking close, sometimes merged together"): the placed islands (world/IslandField) are
     * never closer, coast to coast, than twice the gap (each island's furthest reach, lobes and shelf included, held off its neighbours);
     * the sea between is open sea; some cells have none; the land is a small share; the field stays within its bounds.
     */
    static void islands() {
        double reach = 1.0 + 0.2 + 0.13 + 0.07;
        for (long seed : new long[] {0L, 2L, 7L, -42L, 123456789L}) {
            int empty = 0;
            int cells = 0;
            double closest = Double.MAX_VALUE;
            for (int i = -15; i <= 15; i++) {
                for (int j = -15; j <= 15; j++) {
                    cells++;
                    double[] a = com.hexhaki.piece.world.IslandField.held(i, j, seed);
                    if (a == null) {
                        empty++;
                        continue;
                    }
                    for (int di = -2; di <= 2; di++) {
                        for (int dj = -2; dj <= 2; dj++) {
                            if (di == 0 && dj == 0) {
                                continue;
                            }
                            double[] b = com.hexhaki.piece.world.IslandField.held(i + di, j + dj, seed);
                            if (b != null) {
                                double d = Math.hypot(a[0] - b[0], a[1] - b[1]);
                                closest = Math.min(closest, d - (a[2] + b[2]) * reach - 2.0 * com.hexhaki.piece.world.IslandField.SHELF);
                            }
                        }
                    }
                }
            }
            Check.that(closest >= 2.0 * com.hexhaki.piece.world.IslandField.GAP - 1.0, "seed " + seed + ": islands never close: the nearest two "
                + Math.round(closest) + " blocks of open sea apart, shelves and all");
            Check.that(empty > cells / 10 && empty < cells / 2, "seed " + seed + ": some cells open sea (" + empty + " of " + cells + ")");
            int land = 0;
            int n = 0;
            for (double x = -20000; x < 20000; x += 97) {
                for (double z = -20000; z < 20000; z += 101) {
                    double f = com.hexhaki.piece.world.IslandField.field(x, z, seed);
                    Check.that(f >= com.hexhaki.piece.world.IslandField.SEA - 1e-9 && f <= com.hexhaki.piece.world.IslandField.PEAK + 1e-9, "within bounds");
                    land += f > com.hexhaki.piece.world.IslandField.COAST ? 1 : 0;
                    n++;
                }
            }
            Check.that(land > n * 0.015 && land < n * 0.1, "seed " + seed + ": the land a small share (" + (100.0 * land / n) + "%)");
        }
    }

    /**
     * 4.0.0: each island's own rivers (world/RiverField): every river's two ends are out at sea; along its line it crosses its island's
     * coast exactly twice (in and out: it never runs along a coast, never makes a ring or a lake); two rivers of one island cross at 60 to
     * 120 degrees; the field is 0 on a river and within 0..1 everywhere.
     */
    static void rivers() {
        SplittableRandom random = new SplittableRandom(7);
        int rivers = 0;
        int twos = 0;
        for (long seed : new long[] {0L, 1L, 2L, -42L, 123456789L, Long.MAX_VALUE}) {
            for (int i = -6; i <= 6; i++) {
                for (int j = -6; j <= 6; j++) {
                    RiverField.River[] all = RiverField.rivers(i, j, seed);
                    double[] is = com.hexhaki.piece.world.IslandField.held(i, j, seed);
                    Check.that(is == null ? all.length == 0 : all.length >= 1 && all.length <= 2, "an island has one or two rivers, the sea none");
                    for (RiverField.River river : all) {
                        rivers++;
                        int last = river.count() - 1;
                        Check.that(com.hexhaki.piece.world.IslandField.field(river.x(0), river.z(0), seed) < com.hexhaki.piece.world.IslandField.COAST
                            && com.hexhaki.piece.world.IslandField.field(river.x(last), river.z(last), seed) < com.hexhaki.piece.world.IslandField.COAST,
                            "seed " + seed + ": a river's two ends are at sea");
                        // walked along its line a block at a time: it goes onto land once and off it once
                        int crossings = 0;
                        boolean wasLand = false;
                        for (int k = 0; k < last; k++) {
                            double dx = river.x(k + 1) - river.x(k);
                            double dz = river.z(k + 1) - river.z(k);
                            int steps = (int) Math.ceil(Math.hypot(dx, dz));
                            for (int s = 0; s < steps; s++) {
                                double t = (double) s / steps;
                                boolean land = com.hexhaki.piece.world.IslandField.field(river.x(k) + dx * t, river.z(k) + dz * t, seed)
                                    > com.hexhaki.piece.world.IslandField.COAST;
                                if (land != wasLand) {
                                    crossings++;
                                    wasLand = land;
                                }
                            }
                        }
                        Check.that(crossings == 2, "seed " + seed + " cell " + i + "," + j + ": a river crosses its island's coast twice (" + crossings + ")");
                        Check.near(RiverField.field(river.x(last / 2), river.z(last / 2), seed), 0.0, 1e-9, "the field is 0 on a river's line");
                    }
                    if (all.length == 2) {
                        twos++;
                        double ax = all[0].x(all[0].count() - 1) - all[0].x(0);
                        double az = all[0].z(all[0].count() - 1) - all[0].z(0);
                        double bx = all[1].x(all[1].count() - 1) - all[1].x(0);
                        double bz = all[1].z(all[1].count() - 1) - all[1].z(0);
                        double angle = Math.toDegrees(Math.acos(Math.abs(ax * bx + az * bz) / (Math.hypot(ax, az) * Math.hypot(bx, bz))));
                        Check.that(angle >= Math.min(RiverField.CROSS_LEAST, 180.0 - RiverField.CROSS_MOST) - 1e-6,
                            "two rivers of an island cross at a wide angle (" + angle + ")");
                    }
                }
            }
            for (int k = 0; k < 3000; k++) {
                double v = RiverField.field((random.nextDouble() - 0.5) * 1.0e5, (random.nextDouble() - 0.5) * 1.0e5, seed);
                Check.that(v >= 0.0 && v <= 1.0, "the field stays within 0..1");
            }
        }
        Check.that(rivers > 200 && twos > 20, "rivers laid (" + rivers + ", " + twos + " islands with two)");
        boolean differ = false;
        for (int i = -3; i <= 3 && !differ; i++) {
            for (int j = -3; j <= 3 && !differ; j++) {
                RiverField.River[] one = RiverField.rivers(i, j, 1L);
                RiverField.River[] two = RiverField.rivers(i, j, 2L);
                differ = one.length != two.length || one.length > 0 && !java.util.Arrays.equals(one[0].points(), two[0].points());
            }
        }
        Check.that(differ, "another world, other rivers");
    }
}
