package com.hexhaki.piece.gura;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * 3.1.6: the Gura ultimate's ground (server/GuraUltimate; the owner: "The entire floor in front like bends and breaks into like squares
 * ... causing the floor to like shift and like go up and like break into many fissures and just up and down"). Pure: which squares of
 * the ground in the cone break loose and how each one moves. The ground is cut into plates on the world's own block grid (2 by 2 blocks,
 * now and then a 4 by 4 slab); as the wave reaches a plate it is thrown up, tips over on its own axis, falls back, bounces up again
 * smaller and settles, all within {@link #LIFE} ticks (client/gura/HeaveDraw draws its blocks with the world's own block models where
 * this puts them). Fewer plates break the wider the cone reaches, so as many blocks move at once whatever its reach (the phone's budget).
 */
public final class Heave {
    /** The cone: how far it reaches (blocks; x 1.6 with Ryou on), its half angle (degrees), how fast the wave goes (blocks a tick). */
    public static final double REACH = 44.0;
    public static final double CONE = 38.0;
    public static final double WAVE = 2.0;
    /** How long a plate moves once the wave reaches it (ticks). */
    public static final double LIFE = 46.0;
    /** The most blocks drawn in one frame. */
    public static final int MOST_DRAWN = 420;
    /** Of the plates in a cone of {@link #REACH}, the share that breaks loose (less in a wider one). */
    static final double BREAK = 0.42;
    /** The nearest a plate breaks (blocks from the feet). */
    static final double NEAREST = 3.0;

    /**
     * One plate: {@code size} by {@code size} blocks from block (x, z), {@code dist} from the feet, thrown up {@code amp} blocks at
     * most, tipping about the flat axis (ax, az) by {@code tilt} radians at most, the wave reaching it after {@code delay} ticks.
     */
    public record Plate(int x, int z, int size, double dist, double amp, double ax, double az, double tilt, double delay) {
    }

    private Heave() {
    }

    /** The plates that break in the cone from the feet (ox, oz) along the flat way (dx, dz), {@code cone} degrees each side. */
    public static List<Plate> plates(double ox, double oz, double dx, double dz, double cone, double reach, double wave, int seed) {
        double l = Math.sqrt(dx * dx + dz * dz);
        if (l < 1.0E-6) {
            dx = 0.0;
            dz = 1.0;
        } else {
            dx /= l;
            dz /= l;
        }
        double cos = Math.cos(Math.toRadians(cone));
        double share = BREAK * Math.min(1.0, (REACH / reach) * (REACH / reach));
        List<Plate> out = new ArrayList<>();
        int x0 = (int) Math.floor((ox - reach) / 4.0) * 4;
        int z0 = (int) Math.floor((oz - reach) / 4.0) * 4;
        int x1 = (int) Math.ceil(ox + reach);
        int z1 = (int) Math.ceil(oz + reach);
        for (int sx = x0; sx < x1; sx += 4) {
            for (int sz = z0; sz < z1; sz += 4) {
                SplittableRandom cell = new SplittableRandom(mix(seed, sx, sz));
                if (cell.nextDouble() < 0.28) {
                    add(out, cell, sx, sz, 4, ox, oz, dx, dz, cos, reach, wave, share * 1.2);
                } else {
                    for (int k = 0; k < 4; k++) {
                        add(out, cell, sx + (k & 1) * 2, sz + (k >> 1) * 2, 2, ox, oz, dx, dz, cos, reach, wave, share);
                    }
                }
            }
        }
        out.sort((a, b) -> Double.compare(a.delay, b.delay));
        return out;
    }

    static void add(List<Plate> out, SplittableRandom r, int x, int z, int size, double ox, double oz, double dx, double dz, double cos,
                    double reach, double wave, double share) {
        double cx = x + size * 0.5 - ox;
        double cz = z + size * 0.5 - oz;
        double d = Math.sqrt(cx * cx + cz * cz);
        double breaks = r.nextDouble();
        double a = r.nextDouble();
        double b = r.nextDouble();
        double c = r.nextDouble();
        double e = r.nextDouble();
        if (d < NEAREST || d > reach || (cx * dx + cz * dz) / d < cos || breaks >= share) {
            return;
        }
        double near = 1.0 - 0.45 * d / reach;
        double amp = (0.45 + 0.95 * a) * near * (size > 2 ? 0.8 : 1.0);
        double turn = c * Math.PI * 2.0;
        double tilt = Math.toRadians(6.0 + 16.0 * b) * (size > 2 ? 0.7 : 1.0);
        out.add(new Plate(x, z, size, d, amp, Math.cos(turn), Math.sin(turn), tilt, d / wave + 2.0 * e));
    }

    static long mix(int seed, int x, int z) {
        long h = seed * 0x9E3779B97F4A7C15L;
        h ^= x * 0xC2B2AE3D27D4EB4FL;
        h ^= z * 0x165667B19E3779F9L;
        return h ^ (h >>> 29);
    }

    // ------------------------------------------------------------------------------------------------------ 3.1.8: the ground bent

    /** The farthest the ground is bent (blocks; the wave and the strikes still reach {@link #REACH} x 1.6 with Ryou on). */
    public static final double BEND_MOST = 56.0;
    /** The deepest and the highest a column moves (blocks). */
    public static final int SINK_MOST = 9;
    public static final int LIFT_MOST = 4;

    /**
     * 3.1.8 (the owner: "the ground should like subside ... parts of it should lift, other parts should like go down. Like, it should
     * fucking bend the terrain where it passed"; their reference, Blackbeard's "Tremor Crush!"). 3.2.2 (the owner: "But in a cone, like a
     * cone forming this, like slowly forming, going forward, like pushing forward ... you made a sphere, like a circle"): how many blocks
     * the column at ({@code along}, {@code side}) from the feet (in the cone's own frame; {@code x}, {@code z} its world block) moves, up
     * (+) or down (-), for a bend {@code length} long (at most {@link #BEND_MOST}) and {@code cone} degrees each side. The cone is pressed
     * down into a trench that deepens and widens with the distance (5 to 8 blocks at its deepest, shallowing again at the front), arcs
     * stepped across it every five blocks (round the feet, as the wave runs), its two sides heaved up along the cone's edges and the
     * ground shoved forward into a pile at its front; inside it the ground broken into 4 by 4 slabs, some lifted a block or two, some
     * sunk (half as much in its deep middle). Nothing behind the feet moves. The wave (server/GuraUltimate.Bend) moves the columns as it
     * reaches them, so the trench forms pushing forward.
     */
    public static int bend(double along, double side, int x, int z, double length, double cone, int seed) {
        double d = Math.sqrt(along * along + side * side);
        if (along <= 0.0 || d < NEAREST) {
            return 0;
        }
        double half = Math.toRadians(cone);
        double angle = Math.atan2(Math.abs(side), along);
        double edge = d * Math.sin(angle - half); // blocks out past the cone's side (under 0 inside)
        double front = d - length; // blocks past its front
        if (edge > 6.0 || front > 6.0) {
            return 0;
        }
        double depth = Math.max(5.0, Math.min(8.0, length * 0.15));
        double q = Math.min(1.0, angle / half);
        double deep = depth * smooth(NEAREST, 0.45 * length, along) * (1.0 - smooth(0.8 * length, length, d));
        boolean inside = angle < half && d <= length;
        double h = 0.0;
        if (inside) {
            h -= deep * (1.0 - q * q);
            h += Math.cos(2.0 * Math.PI * d / 5.0) * Math.sqrt(1.0 - q * q) * deep / depth; // the arcs stepped across it
        }
        // its sides heaved up along the cone's edges (higher the farther out), and the ground shoved forward at its front
        h += 3.4 * smooth(NEAREST, 0.4 * length, along) * Math.exp(-(edge / 3.0) * (edge / 3.0)) * (1.0 - smooth(length, length + 5.0, d));
        double spill = Math.toRadians(6.0);
        if (angle < half + spill) {
            h += 3.6 * Math.exp(-(front / 3.0) * (front / 3.0)) * (1.0 - smooth(half, half + spill, angle));
        }
        if (inside) {
            // slabs of the ground (4 by 4 on the world's grid) lifted or sunk, fewer in the deep middle (the trench's shape must read)
            double u = new SplittableRandom(mix(seed + 77, Math.floorDiv(x, 4), Math.floorDiv(z, 4))).nextDouble();
            double fade = (1.0 - 0.4 * d / length) * (q < 0.7 && deep > depth * 0.5 ? 0.5 : 1.0);
            h += (u < 0.12 ? 2.0 : u < 0.3 ? 1.2 : u > 0.9 ? -2.0 : u > 0.74 ? -1.2 : 0.0) * fade;
        }
        int o = (int) Math.round(h);
        return Math.max(-SINK_MOST, Math.min(LIFT_MOST, o));
    }

    /** 0 at {@code from}, 1 at {@code to}, smooth between. */
    static double smooth(double from, double to, double v) {
        double t = (v - from) / (to - from);
        t = t < 0.0 ? 0.0 : t > 1.0 ? 1.0 : t;
        return t * t * (3.0 - 2.0 * t);
    }

    /** How high the plate is {@code t} ticks after the strike (blocks; under 0 it has sunk back into the ground). */
    public static double lift(Plate p, double t) {
        double u = t - p.delay;
        if (u <= 0.0 || u >= LIFE) {
            return 0.0;
        }
        return p.amp * Math.exp(-u / 13.0) * Math.sin(Math.PI * u / 8.0) * fade(u);
    }

    /** How far the plate is tipped (radians) {@code t} ticks after the strike. */
    public static double tip(Plate p, double t) {
        double u = t - p.delay;
        if (u <= 0.0 || u >= LIFE) {
            return 0.0;
        }
        return p.tilt * Math.exp(-u / 13.0) * Math.sin(Math.PI * u / 8.0 + 0.6) * Math.min(1.0, u / 1.5) * fade(u);
    }

    /** The last few ticks of its life brought smoothly to rest. */
    static double fade(double u) {
        double left = (LIFE - u) / 6.0;
        return left >= 1.0 ? 1.0 : Math.max(0.0, left);
    }

    /** Whether any of the plate shows over the ground {@code t} ticks after the strike. */
    public static boolean shows(Plate p, double t) {
        double h = lift(p, t);
        return h + Math.abs(tip(p, t)) * p.size * 0.5 > 0.03;
    }

    /** How many blocks are drawn {@code t} ticks after the strike (each plate's, and the block under it once it is up past half a block). */
    public static int drawn(List<Plate> plates, double t) {
        int n = 0;
        for (Plate p : plates) {
            if (shows(p, t)) {
                n += p.size * p.size * (lift(p, t) > 0.5 ? 2 : 1);
            }
        }
        return n;
    }
}
