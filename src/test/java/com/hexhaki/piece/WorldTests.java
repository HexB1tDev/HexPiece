package com.hexhaki.piece;

import com.hexhaki.piece.world.RiverField;
import java.util.SplittableRandom;

/**
 * 3.8.3: the archipelago's rivers (world/RiverField): every river line runs on without end (never a ring), so every river crosses each
 * island from sea to sea (the owner: "every single river IS CONNECTED TO THE SEA no matter what").
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

    static void rivers() {
        Check.that(RiverField.slopeBound() < 1.0 && Math.abs(RiverField.slopeBound() - RiverField.BENDS) < 1e-9, "the bends' slopes add up to under 1");
        SplittableRandom random = new SplittableRandom(7);
        for (long seed : new long[] {0L, 1L, -42L, 123456789L, Long.MAX_VALUE}) {
            RiverField.Family[] families = RiverField.families(seed);
            Check.that(families.length == 3, "three families of rivers");
            for (RiverField.Family f : families) {
                // along a family's own way across its rivers its F only ever rises (by at least 1 - 0.85 a block): each of its rivers
                // (F = k S) crosses every such line exactly once, so it can never close into a ring
                for (int line = 0; line < 40; line++) {
                    double x = (random.nextDouble() - 0.5) * 2.0e5;
                    double z = (random.nextDouble() - 0.5) * 2.0e5;
                    double last = f.f(x, z);
                    for (int step = 1; step <= 400; step++) {
                        double px = x + f.cos() * step * 7.0;
                        double pz = z + f.sin() * step * 7.0;
                        double now = f.f(px, pz);
                        Check.that(now - last >= 7.0 * (1.0 - RiverField.BENDS) - 1e-9, "F rises along its way: no river ever turns back");
                        last = now;
                    }
                }
                // and its slope is never under 0.15 anywhere (no top, no hollow: no ring of a level line round one)
                for (int k = 0; k < 2000; k++) {
                    double x = (random.nextDouble() - 0.5) * 2.0e5;
                    double z = (random.nextDouble() - 0.5) * 2.0e5;
                    double h = 0.01;
                    double gx = (f.f(x + h, z) - f.f(x - h, z)) / (2 * h);
                    double gz = (f.f(x, z + h) - f.f(x, z - h)) / (2 * h);
                    Check.that(Math.hypot(gx, gz) >= 1.0 - RiverField.BENDS - 1e-6, "F's slope never vanishes");
                }
            }
            // the field: 0 on a river (found by halving along a family's way), within 0..1 everywhere
            RiverField.Family a = families[0];
            double x = 1234.5;
            double z = -987.25;
            double fx = a.f(x, z) / a.spacing() + a.phase();
            double target = (Math.floor(fx) + 1.0 - a.phase()) * a.spacing();
            double lo = 0.0;
            double hi = a.spacing() / (1.0 - RiverField.BENDS);
            for (int i = 0; i < 80; i++) {
                double mid = (lo + hi) / 2.0;
                if (a.f(x + a.cos() * mid, z + a.sin() * mid) < target) {
                    lo = mid;
                } else {
                    hi = mid;
                }
            }
            double rx = x + a.cos() * lo;
            double rz = z + a.sin() * lo;
            Check.near(RiverField.field(rx, rz, seed), 0.0, 1e-6, "the field is 0 on a river's line");
            for (int k = 0; k < 3000; k++) {
                double v = RiverField.field((random.nextDouble() - 0.5) * 1.0e5, (random.nextDouble() - 0.5) * 1.0e5, seed);
                Check.that(v >= 0.0 && v <= 1.0, "the field stays within 0..1");
            }
        }
        // a river near the middle of a sampled island runs out to the sea on both sides: walking along it both ways never ends
        // (its F stays k S while it goes on: the line is followed by stepping across F's slope)
        RiverField.Family b = RiverField.families(5L)[1];
        double x = 0.0;
        double z = 0.0;
        double k = Math.rint(b.f(x, z) / b.spacing() + b.phase());
        double level = (k - b.phase()) * b.spacing();
        for (int way = -1; way <= 1; way += 2) {
            double px = x;
            double pz = z;
            double walked = 0.0;
            for (int step = 0; step < 4000; step++) {
                // onto the line, then a block along it
                for (int i = 0; i < 6; i++) {
                    double h = 0.01;
                    double gx = (b.f(px + h, pz) - b.f(px - h, pz)) / (2 * h);
                    double gz = (b.f(px, pz + h) - b.f(px, pz - h)) / (2 * h);
                    double g2 = gx * gx + gz * gz;
                    double err = b.f(px, pz) - level;
                    px -= err * gx / g2;
                    pz -= err * gz / g2;
                }
                double h = 0.01;
                double gx = (b.f(px + h, pz) - b.f(px - h, pz)) / (2 * h);
                double gz = (b.f(px, pz + h) - b.f(px, pz - h)) / (2 * h);
                double gl = Math.hypot(gx, gz);
                px += way * -gz / gl;
                pz += way * gx / gl;
                walked += 1.0;
            }
            Check.that(walked >= 4000 && Math.hypot(px - x, pz - z) > 2500.0, "a river followed 4000 blocks either way goes on, far away");
        }
        Check.that(RiverField.field(500, 500, 1L) != RiverField.field(500, 500, 2L), "another world, other rivers");
    }
}
