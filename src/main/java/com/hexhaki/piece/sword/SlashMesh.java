package com.hexhaki.piece.sword;

import java.util.Random;

/**
 * 2.2.4, the Haki slash as an energy wave. The owner: "it's not about just being tall. It's meant to be like an energy wave ... see
 * Mihawk's energy projectile from his blade. It is much bigger than that, and it's thick", then, showing the manga's panel (a towering
 * wave of light tearing up out of the ground, its back torn into spikes, rubble flying): "its not meant to be a smooth thing, it's
 * meant to be like this". Its geometry, pure, so the game ({@code client/sword/SlashRenderer}) and the preview (tools/preview/BoltPreview,
 * the {@code bigslash} sheet) draw the same quads.
 *
 * <p>The wave stands in the vertical plane of its flight on the server's cutting line, rising out of the trench it cuts
 * ({@link SwordRules#slashCentre}): a steep leading edge up to a sharp peak {@code height} above the trench, a long back slope torn into
 * spikes that stream back and up (longest halfway up, flickering), two short ones at the crest. Its body is thick across its flight
 * ({@link SwordRules#slashThicknessAt}: widest in the ground, thin at the peak), so from behind its owner it is a spiked wall of light,
 * from the side the manga's wave. Drawn as the manga draws it:</p>
 * <ul>
 *   <li>the body: the colour (a little toward white), shaded flat with a touch of the light where it faces the eye, streaked whiter
 *       along its flow (thin bands up the body and down each spike);</li>
 *   <li>an ink outline round the whole shape (its back hull pushed out a pixel or two: the Haki lightning's outline);</li>
 *   <li>a soft glow of the colour round the body.</li>
 * </ul>
 * The body, the streaks and the outline are opaque quads wound counter-clockwise as the eye sees them (a batch that writes depth and
 * culls back faces); the glow is translucent (a batch drawn after, both faces). Coordinates are relative to the eye (the eye at the origin).
 */
public final class SlashMesh {
    public static final int ROWS = 26;
    public static final int RING = 20;
    /** Tongues streaming off its back slope, tearing off its sides, at its crest. */
    static final int BACK_SPIKES = 22;
    static final int SIDE_SPIKES = 0;
    static final int CREST_SPIKES = 3;
    static final int SPIKE_ROWS = 7;
    /** A tongue's section: across the wave (thin) and in its plane (broad), times its radius. */
    static final double BLADE_THIN = 0.6;
    static final double BLADE_BROAD = 1.5;
    static final int SPIKE_RING = 7;

    /** Quads: four corners (x, y, z each) and four ARGB colours a quad. */
    public static final class Quads {
        public double[] v = new double[12 * 512];
        public int[] c = new int[4 * 512];
        public int n;

        void add(double[] p0, double[] p1, double[] p2, double[] p3, int c0, int c1, int c2, int c3) {
            if ((this.n + 1) * 12 > this.v.length) {
                this.v = java.util.Arrays.copyOf(this.v, this.v.length * 2);
                this.c = java.util.Arrays.copyOf(this.c, this.c.length * 2);
            }
            int o = this.n * 12;
            double[][] p = {p0, p1, p2, p3};
            for (int k = 0; k < 4; k++) {
                this.v[o + k * 3] = p[k][0];
                this.v[o + k * 3 + 1] = p[k][1];
                this.v[o + k * 3 + 2] = p[k][2];
            }
            int q = this.n * 4;
            this.c[q] = c0;
            this.c[q + 1] = c1;
            this.c[q + 2] = c2;
            this.c[q + 3] = c3;
            this.n++;
        }

        /** A triangle (a quad with its last corner doubled) wound so the eye sees it counter-clockwise. */
        void triangle(double[] p0, double[] p1, double[] p2, int c0, int c1, int c2) {
            this.triangleOnly(p0, p1, p2, c0, c1, c2);
        }

        /**
         * A quad wound so the eye sees it counter-clockwise (a front face): judged by its diagonals' normal (sound for any quad, one
         * whose corners meet at a tip too), against its middle.
         */
        void facing(double[] p0, double[] p1, double[] p2, double[] p3, int c0, int c1, int c2, int c3) {
            double ax = p2[0] - p0[0];
            double ay = p2[1] - p0[1];
            double az = p2[2] - p0[2];
            double bx = p3[0] - p1[0];
            double by = p3[1] - p1[1];
            double bz = p3[2] - p1[2];
            double nx = ay * bz - az * by;
            double ny = az * bx - ax * bz;
            double nz = ax * by - ay * bx;
            double mx = p0[0] + p1[0] + p2[0] + p3[0];
            double my = p0[1] + p1[1] + p2[1] + p3[1];
            double mz = p0[2] + p1[2] + p2[2] + p3[2];
            boolean flip = nx * mx + ny * my + nz * mz > 0;
            double[] q1 = flip ? p3 : p1;
            double[] q3 = flip ? p1 : p3;
            int d1 = flip ? c3 : c1;
            int d3 = flip ? c1 : c3;
            if (away(p0, q1, p2) || away(p0, p2, q3)) {
                // a twisted quad (a section shrinking to a tip): its two triangles, each wound on its own
                this.triangleOnly(p0, q1, p2, c0, d1, c2);
                this.triangleOnly(p0, p2, q3, c0, c2, d3);
                return;
            }
            this.add(p0, q1, p2, q3, c0, d1, c2, d3);
        }

        /** Whether a triangle turns its back on the eye (beyond a sliver). */
        static boolean away(double[] a, double[] b, double[] c) {
            double nx = (b[1] - a[1]) * (c[2] - a[2]) - (b[2] - a[2]) * (c[1] - a[1]);
            double ny = (b[2] - a[2]) * (c[0] - a[0]) - (b[0] - a[0]) * (c[2] - a[2]);
            double nz = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
            double mx = a[0] + b[0] + c[0];
            double my = a[1] + b[1] + c[1];
            double mz = a[2] + b[2] + c[2];
            double dot = nx * mx + ny * my + nz * mz;
            return dot > 1.0E-4 * Math.sqrt((nx * nx + ny * ny + nz * nz) * (mx * mx + my * my + mz * mz));
        }

        /** One triangle (a quad with its last corner doubled), wound toward the eye. */
        void triangleOnly(double[] a, double[] b, double[] c, int ca, int cb, int cc) {
            if (away(a, b, c)) {
                this.add(a, c, b, b, ca, cc, cb, cb);
            } else {
                this.add(a, b, c, c, ca, cb, cc, cc);
            }
        }

        public void clear() {
            this.n = 0;
        }

        /**
         * Puts the quads in order from the farthest to the nearest (their middles' distance from the eye), as Minecraft sorts a
         * translucent batch. Stable: quads as far as each other keep their order.
         */
        public void sortBackToFront() {
            Integer[] order = new Integer[this.n];
            double[] far = new double[this.n];
            for (int q = 0; q < this.n; q++) {
                order[q] = q;
                double x = 0;
                double y = 0;
                double z = 0;
                for (int k = 0; k < 4; k++) {
                    x += this.v[q * 12 + k * 3];
                    y += this.v[q * 12 + k * 3 + 1];
                    z += this.v[q * 12 + k * 3 + 2];
                }
                far[q] = x * x + y * y + z * z;
            }
            java.util.Arrays.sort(order, (a, b) -> Double.compare(far[b], far[a]));
            double[] v = new double[this.v.length];
            int[] c = new int[this.c.length];
            for (int i = 0; i < this.n; i++) {
                System.arraycopy(this.v, order[i] * 12, v, i * 12, 12);
                System.arraycopy(this.c, order[i] * 4, c, i * 4, 4);
            }
            this.v = v;
            this.c = c;
        }
    }

    /**
     * Where the wave is: the foot of its cutting line (the trench's bottom under the line, relative to the eye), its flight along the
     * ground and across it, its height, how far it has grown out (0..1: its back and spikes grow in over its first blocks).
     */
    public record Frame(double[] base, double[] flat, double[] side, double height, double grown) {
        /** {@code dx, dz}: the flight's direction (any length; straight up or down flies along x). */
        public static Frame of(double[] base, double dx, double dz, double height, double grown) {
            double l = Math.sqrt(dx * dx + dz * dz);
            double[] flat = l < 1.0E-6 ? new double[] {1, 0, 0} : new double[] {dx / l, 0, dz / l};
            return new Frame(base, flat, new double[] {-flat[2], 0, flat[0]}, height, Math.max(0.05, Math.min(1.0, grown)));
        }

        /** The leading edge's distance ahead of the line at {@code v} up (0 the trench, 1 the peak), in blocks. */
        public double front(double v) {
            return this.height * (SwordRules.SLASH_FRONT * Math.pow(Math.max(0.0, 1.0 - v), 0.55) + 0.015);
        }

        /** The back edge's distance behind the line (negative), shorter while it grows out. */
        public double back(double v) {
            return -this.height * SwordRules.SLASH_BACK * Math.pow(Math.max(0.0, 1.0 - v), 1.25) * this.grown;
        }

        /** The body's half thickness across its flight at {@code v} (blocks). */
        public double across(double v) {
            return SwordRules.slashThicknessAt(this.height, v) / 2.0;
        }

        /** A point: {@code f} ahead of the line, {@code y} up from the trench, {@code s} to the side. */
        public double[] at(double f, double y, double s) {
            return new double[] {this.base[0] + this.flat[0] * f + this.side[0] * s, this.base[1] + y,
                this.base[2] + this.flat[2] * f + this.side[2] * s};
        }
    }

    /**
     * One tongue of energy: its root (ahead of the line, up, to the side), its elevation above the level and its heading (0 straight
     * back, + to the side), its length, its root radius and which way it curls sideways.
     */
    record Spike(double f, double y, double s, double elevation, double heading, double length, double radius, double curl) {
        /** Its direction, in (ahead, up, side). */
        double[] dir() {
            double c = Math.cos(this.elevation);
            return new double[] {-c * Math.cos(this.heading), Math.sin(this.elevation), c * Math.sin(this.heading)};
        }
    }

    private SlashMesh() {
    }

    static int argb(double a, double r, double g, double b) {
        return (int) Math.round(clamp(a) * 255) << 24 | (int) Math.round(clamp(r) * 255) << 16 | (int) Math.round(clamp(g) * 255) << 8
            | (int) Math.round(clamp(b) * 255);
    }

    static double clamp(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }

    static double length(double[] p) {
        return Math.sqrt(p[0] * p[0] + p[1] * p[1] + p[2] * p[2]);
    }

    /** The outline's width at p: about two pixels at any distance, never under 0.05 blocks. */
    static double ink(double[] p) {
        return Math.max(0.05, length(p) * 0.0022);
    }

    /**
     * The tongues of slash {@code seed}: streaming up and back off its back slope (nearly parallel, as the panel draws them, of very
     * different lengths, splayed a little to either side), tearing out and up off its sides, and a few short ones at its crest; each
     * flickering in length.
     */
    static Spike[] spikes(Frame f, long seed, double ticks) {
        Random random = new Random(seed * 0x9E3779B97F4A7C15L + 77);
        double h = f.height;
        Spike[] out = new Spike[BACK_SPIKES + SIDE_SPIKES + CREST_SPIKES];
        int n = 0;
        for (int k = 0; k < BACK_SPIKES; k++) {
            double v = 0.05 + 0.82 * (k + 0.15 + random.nextDouble() * 0.7) / BACK_SPIKES;
            double depth = 0.62 + random.nextDouble() * 0.18;
            double root = f.back(v) * depth + f.front(v) * (1 - depth);
            double side = (random.nextDouble() * 2 - 1) * 0.55 * f.across(v);
            double elevation = Math.toRadians(48 + 12 * v + (random.nextDouble() - 0.5) * 12);
            // low ones stay in the wave's plane and short (no claws round its foot), the upper ones fan out into a crown
            double heading = Math.toRadians(((random.nextDouble() * 2 - 1) * 15 + Math.signum(side) * 5) * Math.min(1.0, v * 2.2));
            boolean longOne = random.nextDouble() < 0.4 && v > 0.25;
            double length = h * (longOne ? 0.3 + 0.18 * random.nextDouble() : 0.1 + 0.16 * random.nextDouble())
                * (0.6 + 0.4 * Math.sin(Math.PI * Math.min(1.0, 0.15 + v)));
            out[n++] = flicker(new Spike(root, v * h, side, elevation, heading, length, h * (0.017 + 0.013 * random.nextDouble()),
                Math.signum(side)), f, ticks, random);
        }
        for (int k = 0; k < SIDE_SPIKES; k++) {
            double v = 0.08 + 0.7 * random.nextDouble();
            double at = 0.55 + 0.4 * random.nextDouble();
            double root = f.back(v) * at + f.front(v) * (1 - at);
            double sign = k % 2 == 0 ? 1 : -1;
            double side = sign * 0.7 * f.across(v);
            double elevation = Math.toRadians(38 + 24 * random.nextDouble());
            double heading = sign * Math.toRadians(58 + 24 * random.nextDouble());
            double length = h * (0.06 + 0.1 * random.nextDouble()) * (1.0 - v * 0.4);
            out[n++] = flicker(new Spike(root, v * h, side, elevation, heading, length, h * (0.018 + 0.01 * random.nextDouble()), sign), f,
                ticks, random);
        }
        for (int k = 0; k < CREST_SPIKES; k++) {
            double v = 0.8 + 0.12 * random.nextDouble();
            double root = f.back(v) * 0.5 + f.front(v) * 0.5;
            double elevation = Math.toRadians(62 + 16 * random.nextDouble());
            double heading = Math.toRadians((random.nextDouble() * 2 - 1) * 30);
            double length = h * (0.06 + 0.07 * random.nextDouble());
            out[n++] = flicker(new Spike(root, v * h, 0.0, elevation, heading, length, h * 0.02, Math.signum(heading)), f, ticks, random);
        }
        return out;
    }

    /** A tongue's length flickering with the wave's age, and grown in with the wave. */
    static Spike flicker(Spike k, Frame f, double ticks, Random random) {
        double phase = random.nextDouble() * Math.PI * 2;
        double length = k.length * (0.86 + 0.14 * Math.sin(ticks * (0.7 + random.nextDouble() * 0.6) + phase)) * Math.min(1.0, f.grown * 2.5);
        return new Spike(k.f, k.y, k.s, k.elevation, k.heading, length, k.radius, k.curl);
    }

    /** The body's surface: rows up from the trench (closer together toward the peak), round its section: {point, normal}. */
    static double[][][][] body(Frame f, double grow) {
        double[][][][] grid = new double[ROWS + 1][RING][][];
        for (int i = 0; i <= ROWS; i++) {
            double v = 1.0 - Math.pow(1.0 - i / (double) ROWS, 1.35);
            double front = f.front(v);
            double back = f.back(v);
            double mid = (front + back) / 2.0;
            double rf = (front - back) / 2.0;
            double rs = f.across(v);
            for (int j = 0; j < RING; j++) {
                double t = j * Math.PI * 2 / RING;
                double cos = Math.cos(t);
                double sin = Math.sin(t);
                double[] p = f.at(mid + rf * cos, v * f.height, rs * sin);
                double g = grow > 0 ? grow * ink(p) : 0.0;
                double[] q = f.at(mid + (rf + g) * cos, v * f.height + (i == ROWS ? g : 0.0), (rs + g) * sin);
                double nx = cos / Math.max(1.0E-6, rf);
                double ny = sin / Math.max(1.0E-6, rs);
                double l = Math.hypot(nx, ny);
                double[] n = {f.flat[0] * nx + f.side[0] * ny, 0.35 * l * v, f.flat[2] * nx + f.side[2] * ny};
                double nl = Math.max(1.0E-9, length(n));
                grid[i][j] = new double[][] {q, {n[0] / nl, n[1] / nl, n[2] / nl}};
            }
        }
        return grid;
    }

    /** A tongue's surface: rows from its root to its point, a round section tapering to nothing: {point, normal}. */
    static double[][][][] spike(Frame f, Spike k, double grow) {
        double[][][][] grid = new double[SPIKE_ROWS + 1][SPIKE_RING][][];
        double[][] basis = basis(k);
        for (int i = 0; i <= SPIKE_ROWS; i++) {
            double s = i / (double) SPIKE_ROWS;
            double[] axis = spikeAxis(k, s);
            double r = k.radius * Math.pow(1.0 - s, 0.85);
            for (int j = 0; j < SPIKE_RING; j++) {
                double t = j * Math.PI * 2 / SPIKE_RING;
                // a blade of flame: thin across (e1), broad in the wave's plane (e2)
                double ca = Math.cos(t) * BLADE_THIN;
                double sb = Math.sin(t) * BLADE_BROAD;
                double[] off = new double[3];
                double[] n = new double[3];
                for (int a = 0; a < 3; a++) {
                    off[a] = basis[1][a] * ca + basis[2][a] * sb;
                    n[a] = basis[1][a] * Math.cos(t) / BLADE_THIN + basis[2][a] * Math.sin(t) / BLADE_BROAD;
                }
                double[] p = f.at(axis[0] + off[0] * r, axis[1] + off[1] * r, axis[2] + off[2] * r);
                double g = grow > 0 ? grow * ink(p) : 0.0;
                double tip = i == SPIKE_ROWS ? g : 0.0;
                double[] dir = {off[0] * r + n[0] / Math.max(1.0E-9, length(n)) * g, off[1] * r + n[1] / Math.max(1.0E-9, length(n)) * g,
                    off[2] * r + n[2] / Math.max(1.0E-9, length(n)) * g};
                double[] q = f.at(axis[0] + dir[0] + basis[0][0] * tip, axis[1] + dir[1] + basis[0][1] * tip, axis[2] + dir[2] + basis[0][2] * tip);
                grid[i][j] = new double[][] {q, world(f, n)};
            }
        }
        return grid;
    }

    /** A tongue's direction and the two axes of its section, in (ahead, up, side). */
    static double[][] basis(Spike k) {
        double[] d = k.dir();
        // across it, level; then the third, square to both
        double[] e1 = {-d[2], 0, d[0]};
        double l = Math.hypot(e1[0], e1[2]);
        e1 = l < 1.0E-6 ? new double[] {0, 0, 1} : new double[] {e1[0] / l, 0, e1[2] / l};
        double[] e2 = {e1[1] * d[2] - e1[2] * d[1], e1[2] * d[0] - e1[0] * d[2], e1[0] * d[1] - e1[1] * d[0]};
        return new double[][] {d, e1, e2};
    }

    /** A direction in (ahead, up, side) as a world direction. */
    static double[] world(Frame f, double[] n) {
        double[] w = {f.flat[0] * n[0] + f.side[0] * n[2], n[1], f.flat[2] * n[0] + f.side[2] * n[2]};
        double l = Math.max(1.0E-9, length(w));
        return new double[] {w[0] / l, w[1] / l, w[2] / l};
    }

    /** A tongue's axis {@code s} (0 root .. 1 point) of the way along, in (ahead, up, side): curling up and a little sideways, like flame. */
    static double[] spikeAxis(Spike k, double s) {
        double[] d = k.dir();
        double reach = k.length * s;
        return new double[] {k.f + d[0] * reach, k.y + d[1] * reach + 0.12 * k.length * s * s,
            k.s + d[2] * reach + k.curl * 0.06 * k.length * Math.sin(Math.PI * s)};
    }

    /** Whether a quad of a grid faces the eye (its normal toward the origin). */
    static boolean facing(double[][] a, double[][] b, double[][] c, double[][] d) {
        double px = a[0][0] + b[0][0] + c[0][0] + d[0][0];
        double py = a[0][1] + b[0][1] + c[0][1] + d[0][1];
        double pz = a[0][2] + b[0][2] + c[0][2] + d[0][2];
        double nx = a[1][0] + b[1][0] + c[1][0] + d[1][0];
        double ny = a[1][1] + b[1][1] + c[1][1] + d[1][1];
        double nz = a[1][2] + b[1][2] + c[1][2] + d[1][2];
        return px * nx + py * ny + pz * nz < 0;
    }

    /** How squarely the surface at p faces the eye (1 facing it, 0 edge on). */
    static double square(double[][] pn) {
        double[] p = pn[0];
        double[] n = pn[1];
        double l = Math.max(1.0E-9, length(p));
        return Math.abs(p[0] * n[0] + p[1] * n[1] + p[2] * n[2]) / l;
    }

    /**
     * The wave of slash {@code seed}: {@code rgb} its colour, {@code ticks} its age (the spikes' flicker), {@code fade} 0..1 as it dies.
     * The body, its streaks and its outline go to {@code solid}, its glow to {@code glow}.
     */
    public static void wave(Quads solid, Quads glow, Frame f, long seed, int rgb, double ticks, double fade) {
        double r = ((rgb >> 16) & 255) / 255.0;
        double g = ((rgb >> 8) & 255) / 255.0;
        double b = (rgb & 255) / 255.0;
        // the body's colour: the colour a little toward white (light, not paint)
        double br = r + (1 - r) * 0.12;
        double bg = g + (1 - g) * 0.12;
        double bb = b + (1 - b) * 0.12;
        double alpha = clamp(fade);
        Spike[] spikes = spikes(f, seed, ticks);
        // the body and the spikes: their near sides, then their outlines (the hulls' far sides, pushed out)
        surface(solid, body(f, 0.0), true, br, bg, bb, alpha);
        for (Spike k : spikes) {
            surface(solid, spike(f, k, 0.0), true, br, bg, bb, alpha, true);
        }
        surface(solid, body(f, 1.0), false, br, bg, bb, alpha);
        for (Spike k : spikes) {
            surface(solid, spike(f, k, 1.0), false, br, bg, bb, alpha, true);
        }
        streaks(solid, f, spikes, seed, br, bg, bb, alpha);
        // the glow round the body: most opaque at its edge, both halves
        double[][][][] halo = new double[ROWS + 1][RING][][];
        double grow = f.height * 0.035 + 0.15;
        for (int i = 0; i <= ROWS; i++) {
            double v = 1.0 - Math.pow(1.0 - i / (double) ROWS, 1.35);
            double front = f.front(v);
            double back = f.back(v);
            for (int j = 0; j < RING; j++) {
                double t = j * Math.PI * 2 / RING;
                double[] p = f.at((front + back) / 2 + ((front - back) / 2 + grow) * Math.cos(t), v * f.height + (i == ROWS ? grow : 0),
                    (f.across(v) + grow) * Math.sin(t));
                double[] n = {f.flat[0] * Math.cos(t) + f.side[0] * Math.sin(t), 0, f.flat[2] * Math.cos(t) + f.side[2] * Math.sin(t)};
                halo[i][j] = new double[][] {p, n, {v}};
            }
        }
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < RING; j++) {
                double[][][] q = {halo[i][j], halo[i + 1][j], halo[i + 1][(j + 1) % RING], halo[i][(j + 1) % RING]};
                int[] c = new int[4];
                for (int m = 0; m < 4; m++) {
                    double edge = Math.pow(1.0 - square(q[m]), 1.5);
                    c[m] = argb(alpha * 0.34 * (0.15 + 0.85 * edge) * (1.0 - q[m][2][0] * 0.5), r, g, b);
                }
                glow.add(q[0][0], q[1][0], q[2][0], q[3][0], c[0], c[1], c[2], c[3]);
            }
        }
    }

    /** A grid's quads: {@code near} its side facing the eye, lit; else its far side (an outline hull), in ink. */
    static void surface(Quads out, double[][][][] grid, boolean near, double r, double g, double b, double alpha) {
        surface(out, grid, near, r, g, b, alpha, false);
    }

    /** {@code split}: as two triangles each wound toward the eye (a tongue's quads twist where it curls; a culled half would leave a hole). */
    static void surface(Quads out, double[][][][] grid, boolean near, double r, double g, double b, double alpha, boolean split) {
        int rows = grid.length - 1;
        int ring = grid[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < ring; j++) {
                double[][][] q = {grid[i][j], grid[i + 1][j], grid[i + 1][(j + 1) % ring], grid[i][(j + 1) % ring]};
                if (facing(q[0], q[1], q[2], q[3]) != near) {
                    continue;
                }
                int[] c = new int[4];
                for (int m = 0; m < 4; m++) {
                    if (near) {
                        // flat colour, a touch brighter where it faces the eye, a touch darker where it turns away
                        double light = 0.8 + 0.25 * square(q[m]);
                        c[m] = argb(alpha, r * light, g * light, b * light);
                    } else {
                        c[m] = argb(alpha, 0.03 + r * 0.06, 0.03 + g * 0.06, 0.04 + b * 0.06);
                    }
                }
                if (split) {
                    out.triangle(q[0][0], q[1][0], q[2][0], c[0], c[1], c[2]);
                    out.triangle(q[0][0], q[2][0], q[3][0], c[0], c[2], c[3]);
                } else {
                    out.facing(q[0][0], q[1][0], q[2][0], q[3][0], c[0], c[1], c[2], c[3]);
                }
            }
        }
    }

    /** The streaks: thin whiter bands along the flow, up the body's both faces and down the middle of each spike. */
    static void streaks(Quads out, Frame f, Spike[] spikes, long seed, double r, double g, double b, double alpha) {
        Random random = new Random(seed * 31 + 5);
        int white = argb(alpha, r + (1 - r) * 0.75, g + (1 - g) * 0.75, b + (1 - b) * 0.75);
        int bands = 4;
        for (int k = 0; k < bands; k++) {
            double w = -0.7 + 1.35 * (k + 0.3 + random.nextDouble() * 0.4) / bands;
            double top = 0.55 + 0.35 * random.nextDouble();
            double width = f.height * (0.008 + 0.006 * random.nextDouble());
            for (int sideSign = -1; sideSign <= 1; sideSign += 2) {
                int n = 12;
                double[][] left = new double[n + 1][];
                double[][] right = new double[n + 1][];
                for (int i = 0; i <= n; i++) {
                    double v = 0.03 + (top - 0.03) * i / n;
                    double front = f.front(v);
                    double back = f.back(v);
                    double mid = (front + back) / 2;
                    double rf = (front - back) / 2;
                    double half = width * (0.3 + 0.7 * Math.sin(Math.PI * i / n));
                    // on the surface where cos t = w, a little outside it (never inside the body)
                    double s = Math.sqrt(Math.max(0.0, 1.0 - w * w)) * f.across(v) * 1.012 + 0.01;
                    left[i] = f.at(mid + rf * w - half, v * f.height, sideSign * s);
                    right[i] = f.at(mid + rf * w + half, v * f.height, sideSign * s);
                }
                strip(out, left, right, white);
            }
        }
        for (Spike k : spikes) {
            double[][] basis = basis(k);
            for (int sideSign = -1; sideSign <= 1; sideSign += 2) {
                int n = 6;
                double[][] left = new double[n + 1][];
                double[][] right = new double[n + 1][];
                for (int i = 0; i <= n; i++) {
                    double s = 0.05 + 0.7 * i / n;
                    double[] axis = spikeAxis(k, s);
                    double radius = k.radius * Math.pow(1.0 - s, 0.85);
                    double lift = sideSign * (radius * BLADE_THIN * 1.03 + 0.01);
                    double half = radius * BLADE_BROAD * 0.14;
                    double[] l = new double[3];
                    double[] rr = new double[3];
                    for (int a = 0; a < 3; a++) {
                        l[a] = axis[a] + basis[1][a] * lift - basis[2][a] * half;
                        rr[a] = axis[a] + basis[1][a] * lift + basis[2][a] * half;
                    }
                    left[i] = f.at(l[0], l[1], l[2]);
                    right[i] = f.at(rr[0], rr[1], rr[2]);
                }
                strip(out, left, right, white);
            }
        }
    }

    static void strip(Quads out, double[][] left, double[][] right, int colour) {
        for (int i = 0; i + 1 < left.length; i++) {
            out.facing(left[i], right[i], right[i + 1], left[i + 1], colour, colour, colour, colour);
        }
    }

    /** A point {@code out} blocks ahead of the leading edge, {@code v} up (0 the trench, 1 the peak): where the crackle runs. */
    public static double[] edge(Frame f, double v, double out, double across) {
        return f.at(f.front(v) + out, v * f.height, across);
    }
}
