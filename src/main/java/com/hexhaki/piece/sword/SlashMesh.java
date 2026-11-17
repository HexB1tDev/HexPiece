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
 * from the side the manga's wave.</p>
 *
 * <p>2.2.6 (the owner: "the shape is right but its a solid thing, its clearly not a flowing energy beam"): drawn as light, not a solid
 * (see {@link #wave}): a see-through saturated skin dense at its rims, a white-hot core, bands of light pouring back from the cutting edge
 * into the tongues, racing streaks, a white-hot cutting edge, flickering see-through tongues and a halo, all alpha blended and sorted far
 * to near (the batch writes depth). Coordinates are relative to the eye (the eye at the origin).
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
    static final int SPIKE_RING = 10;

    /** Quads: four corners (x, y, z each) and four ARGB colours a quad. */
    public static final class Quads {
        public double[] v = new double[12 * 512];
        public int[] c = new int[4 * 512];
        public int n;
        /**
         * 2.2.6: the drawing layer of each quad, and the one quads get as they are added: see-through layers are drawn in this order
         * (the back of the wave first, its near skin last), each sorted far to near, so no see-through layer hides one behind it.
         */
        public int[] layers = new int[512];
        public int layer;

        void add(double[] p0, double[] p1, double[] p2, double[] p3, int c0, int c1, int c2, int c3) {
            if ((this.n + 1) * 12 > this.v.length) {
                this.v = java.util.Arrays.copyOf(this.v, this.v.length * 2);
                this.c = java.util.Arrays.copyOf(this.c, this.c.length * 2);
            }
            if (this.n >= this.layers.length) {
                this.layers = java.util.Arrays.copyOf(this.layers, this.layers.length * 2);
            }
            this.layers[this.n] = this.layer;
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
            this.layer = 0;
        }

        /**
         * Puts the quads in order from the farthest to the nearest (their middles' distance from the eye), as Minecraft sorts a
         * translucent batch, 2.2.6: layer by layer ({@link #layers}). Stable: quads as far as each other keep their order.
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
            int[] layer = this.layers;
            java.util.Arrays.sort(order, (a, b) -> layer[a] != layer[b] ? Integer.compare(layer[a], layer[b]) : Double.compare(far[b], far[a]));
            double[] v = new double[this.v.length];
            int[] c = new int[this.c.length];
            int[] l = new int[this.layers.length];
            for (int i = 0; i < this.n; i++) {
                System.arraycopy(this.v, order[i] * 12, v, i * 12, 12);
                System.arraycopy(this.c, order[i] * 4, c, i * 4, 4);
                l[i] = layer[order[i]];
            }
            this.v = v;
            this.c = c;
            this.layers = l;
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
     * 2.2.6 (the owner: "the energy slice from the swordsman M2 looks horrible, it's meant to be a energy wave, the shape is right but its
     * a solid thing, its clearly not a flowing energy beam"): the wave of slash {@code seed}, {@code rgb} its colour, {@code ticks} its
     * age (its flow and the tongues' flicker), {@code fade} 0..1 as it dies. Same shape as 2.2.4's, but light, not a solid: every layer
     * goes to {@code glow} (alpha blended, the world seen through it), {@code solid} gets nothing.
     * <ol>
     *   <li>the skin: the colour, deep and saturated, thin where it faces the eye and dense at its rims (a glowing volume seen through),
     *       its leading edge whitening into the cutting edge;</li>
     *   <li>the core: inside it, white-hot where it faces the eye;</li>
     *   <li>both swept by bands of light streaming back from the edge and up into the tongues ({@link #flow}), so it pours;</li>
     *   <li>streaks: thin white dashes racing back along its flanks and rising, as the panel's speed lines;</li>
     *   <li>the cutting edge: a white-hot line up the front;</li>
     *   <li>the tongues: see-through flames, white at the root, the colour toward a tip that thins to nothing, pulses running out;</li>
     *   <li>the halo round it all.</li>
     * </ol>
     */
    public static void wave(Quads solid, Quads glow, Frame f, long seed, int rgb, double ticks, double fade) {
        double r = ((rgb >> 16) & 255) / 255.0;
        double g = ((rgb >> 8) & 255) / 255.0;
        double b = (rgb & 255) / 255.0;
        double alpha = clamp(fade);
        if (alpha <= 0.0) {
            return;
        }
        double phase = (seed & 1023) / 1023.0 * Math.PI * 2.0;
        Spike[] spikes = spikes(f, seed, ticks);
        double[] colour = {r, g, b};
        // the skin and the core, their far halves first; the tongues; their near halves; then what lies on the skin
        shell(glow, f, 1.0, 0.0, colour, ticks, phase, alpha, false);
        shell(glow, f, 0.5, 0.0, colour, ticks, phase, alpha, true);
        glow.layer = TONGUES;
        for (Spike k : spikes) {
            tongue(glow, f, k, colour, ticks, phase, alpha);
        }
        glow.layer = STREAKS;
        streaks(glow, f, seed, colour, ticks, alpha);
        edgeLine(glow, f, colour, ticks, alpha);
        // the halo round the body: faint, most opaque at its edge
        double grow = f.height * 0.035 + 0.15;
        double[][][][] halo = new double[ROWS + 1][RING][][];
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
                    c[m] = argb(alpha * 0.2 * (0.15 + 0.85 * edge) * (1.0 - q[m][2][0] * 0.5), r, g, b);
                }
                glow.layer = facing(q[0], q[1], q[2], q[3]) ? HALO_NEAR : HALO_FAR;
                glow.add(q[0][0], q[1][0], q[2][0], q[3][0], c[0], c[1], c[2], c[3]);
            }
        }
    }

    /** Rows round the body's section for the energy shells (more than the halo's: the flowing bands need the points). */
    static final int SHELL_RING = 32;
    /** 2.2.6: the see-through layers, in the order they are drawn. */
    static final int HALO_FAR = 0;
    static final int SKIN_FAR = 1;
    static final int CORE_FAR = 2;
    static final int TONGUES = 3;
    static final int CORE_NEAR = 4;
    static final int SKIN_NEAR = 5;
    static final int HALO_NEAR = 6;
    static final int STREAKS = 7;

    /**
     * 2.2.6: how bright the flowing light is at {@code u} (0 the leading edge .. 1 the back) and {@code v} (0 the trench .. 1 the peak),
     * {@code ticks} into the wave's life: two sets of bands sweeping back from the edge and up (the slower broad, the faster narrow and
     * slanted the other way), 0..1.
     */
    static double flow(double u, double v, double ticks, double phase) {
        double broad = 0.5 + 0.5 * Math.sin(Math.PI * 2.0 * (1.5 * u + 0.45 * v - 0.2 * ticks) + phase);
        double fine = 0.5 + 0.5 * Math.sin(Math.PI * 2.0 * (3.4 * u - 1.1 * v - 0.38 * ticks) + phase * 1.7);
        return 0.62 * broad + 0.38 * fine;
    }

    /**
     * 2.2.6: one of the body's two energy shells, its section {@code scale} of the body's ({@code pad} blocks more): the skin
     * ({@code core} false: the deep colour, see-through where it faces the eye, dense at its rims, white along its leading edge) or the
     * core ({@code core} true: white-hot where it faces the eye, nothing at its rims). Both faces (the batch does not cull).
     */
    static void shell(Quads out, Frame f, double scale, double pad, double[] colour, double ticks, double phase, double alpha, boolean core) {
        double[][] pts = new double[(ROWS + 1) * SHELL_RING][];
        double[] us = new double[SHELL_RING];
        double[] vs = new double[ROWS + 1];
        double[][] normals = new double[(ROWS + 1) * SHELL_RING][];
        for (int i = 0; i <= ROWS; i++) {
            double v = 1.0 - Math.pow(1.0 - i / (double) ROWS, 1.35);
            vs[i] = v;
            double front = f.front(v);
            double back = f.back(v);
            double mid = (front + back) / 2.0;
            double rf = (front - back) / 2.0 * scale + pad;
            double rs = f.across(v) * scale + pad;
            for (int j = 0; j < SHELL_RING; j++) {
                double t = j * Math.PI * 2 / SHELL_RING;
                double cos = Math.cos(t);
                double sin = Math.sin(t);
                us[j] = (1.0 - cos) / 2.0;
                double top = i == ROWS ? pad : 0.0;
                pts[i * SHELL_RING + j] = f.at(mid + rf * cos, v * f.height + top, rs * sin);
                double nx = cos / Math.max(1.0E-6, rf);
                double ny = sin / Math.max(1.0E-6, rs);
                double l = Math.hypot(nx, ny);
                double[] n = {f.flat[0] * nx + f.side[0] * ny, 0.35 * l * v, f.flat[2] * nx + f.side[2] * ny};
                double nl = Math.max(1.0E-9, length(n));
                normals[i * SHELL_RING + j] = new double[] {n[0] / nl, n[1] / nl, n[2] / nl};
            }
        }
        int[] colours = new int[pts.length];
        for (int i = 0; i <= ROWS; i++) {
            for (int j = 0; j < SHELL_RING; j++) {
                int at = i * SHELL_RING + j;
                double facing = square(new double[][] {pts[at], normals[at]});
                double u = us[j];
                double v = vs[i];
                double light = flow(u, v, ticks, phase);
                // the leading edge (u near 0) whitens into the cutting edge; the back thins out into the tongues
                double edge = Math.max(0.0, 1.0 - u / 0.16);
                edge *= edge;
                double a;
                double white;
                if (core) {
                    a = 0.72 * Math.pow(facing, 1.2) * (0.4 + 0.8 * light) * (1.0 - 0.5 * u) * (1.0 - 0.3 * v);
                    white = 0.35 + 0.45 * light * light;
                } else {
                    double rim = Math.pow(1.0 - facing, 1.6);
                    a = (0.28 + 0.6 * rim) * (0.4 + 0.8 * light) * (1.0 - 0.35 * u) + 0.45 * edge;
                    white = 0.04 + 0.55 * light * light * light + 0.7 * edge;
                }
                // the very top thins away too, so the peak is a point of light, not a cap
                a *= 1.0 - 0.5 * Math.max(0.0, (v - 0.85) / 0.15);
                double k = clamp(white);
                // the deep colour: saturated (its weakest channels pulled down a little), toward white where it burns hottest
                double[] deep = saturated(colour);
                double dr = deep[0];
                double dg = deep[1];
                double db = deep[2];
                colours[at] = argb(Math.min(0.9, a) * alpha, dr + (1 - dr) * k, dg + (1 - dg) * k, db + (1 - db) * k);
            }
        }
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < SHELL_RING; j++) {
                int a0 = i * SHELL_RING + j;
                int a1 = (i + 1) * SHELL_RING + j;
                int a2 = (i + 1) * SHELL_RING + (j + 1) % SHELL_RING;
                int a3 = i * SHELL_RING + (j + 1) % SHELL_RING;
                if (((colours[a0] | colours[a1] | colours[a2] | colours[a3]) >>> 24) == 0) {
                    continue;
                }
                boolean near = facing(new double[][] {pts[a0], normals[a0]}, new double[][] {pts[a1], normals[a1]},
                    new double[][] {pts[a2], normals[a2]}, new double[][] {pts[a3], normals[a3]});
                out.layer = core ? near ? CORE_NEAR : CORE_FAR : near ? SKIN_NEAR : SKIN_FAR;
                out.add(pts[a0], pts[a1], pts[a2], pts[a3], colours[a0], colours[a1], colours[a2], colours[a3]);
            }
        }
    }

    /** 2.2.6: the colour made richer: its weaker channels pulled down toward nothing, its strongest kept (light seen through light). */
    static double[] saturated(double[] c) {
        double top = Math.max(c[0], Math.max(c[1], c[2]));
        double[] out = new double[3];
        for (int i = 0; i < 3; i++) {
            double share = top <= 0 ? 0 : c[i] / top;
            out[i] = top * Math.pow(share, 1.6) * 0.95;
        }
        return out;
    }

    /**
     * 2.2.6: a tongue as a see-through flame: white-hot at its root, the colour toward its tip, thinning to nothing there, densest down
     * its middle as the eye sees it, pulses of light running out along it.
     */
    static void tongue(Quads out, Frame f, Spike k, double[] colour, double ticks, double phase, double alpha) {
        double[][][][] grid = spike(f, k, 0.0);
        int rows = grid.length - 1;
        int ring = grid[0].length;
        double pulse0 = (k.f * 0.37 + k.y * 0.61) / Math.max(1.0E-6, f.height);
        int[][] c = new int[rows + 1][ring];
        for (int i = 0; i <= rows; i++) {
            double s = i / (double) rows;
            double pulse = 0.5 + 0.5 * Math.sin(Math.PI * 2.0 * (2.2 * s - 0.33 * ticks + pulse0 * 3.0) + phase);
            for (int j = 0; j < ring; j++) {
                double facing = square(grid[i][j]);
                // its root fades into the body it pours out of (no stack of hard roots seen through the skin)
                double root = Math.min(1.0, 0.1 + s / 0.22);
                double a = 0.78 * Math.pow(1.0 - s, 0.85) * root * (0.35 + 0.65 * Math.pow(facing, 0.7)) * (0.6 + 0.5 * pulse);
                double white = clamp(0.5 * (1.0 - s) * (1.0 - s) + 0.2 * pulse * (1.0 - s));
                double[] deep = saturated(colour);
                double dr = deep[0];
                double dg = deep[1];
                double db = deep[2];
                c[i][j] = argb(Math.min(0.9, a) * alpha, dr + (1 - dr) * white, dg + (1 - dg) * white, db + (1 - db) * white);
            }
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < ring; j++) {
                int j1 = (j + 1) % ring;
                out.add(grid[i][j][0], grid[i + 1][j][0], grid[i + 1][j1][0], grid[i][j1][0], c[i][j], c[i + 1][j], c[i + 1][j1], c[i][j1]);
            }
        }
    }

    /**
     * 2.2.6: the streaks: thin white dashes on both flanks, each racing back from the leading edge and rising toward the back, a new
     * dash every so often, as the manga's speed lines run along a wave.
     */
    static void streaks(Quads out, Frame f, long seed, double[] colour, double ticks, double alpha) {
        Random random = new Random(seed * 31 + 5);
        int lines = 7;
        int n = 26;
        double wr = colour[0] + (1 - colour[0]) * 0.82;
        double wg = colour[1] + (1 - colour[1]) * 0.82;
        double wb = colour[2] + (1 - colour[2]) * 0.82;
        for (int line = 0; line < lines; line++) {
            double v0 = 0.04 + 0.7 * (line + 0.2 + random.nextDouble() * 0.6) / lines;
            double rise = (1.0 - v0) * (0.15 + 0.2 * random.nextDouble());
            double width = f.height * (0.006 + 0.005 * random.nextDouble());
            double offset = random.nextDouble();
            double speed = 0.07 + 0.05 * random.nextDouble();
            double dash = 0.28 + 0.2 * random.nextDouble();
            for (int sign = -1; sign <= 1; sign += 2) {
                double head = (offset + ticks * speed + (sign > 0 ? 0.5 : 0.0)) % 1.0;
                double[][] left = new double[n + 1][];
                double[][] right = new double[n + 1][];
                int[] c = new int[n + 1];
                for (int i = 0; i <= n; i++) {
                    double s = i / (double) n;
                    double u = 0.03 + 0.9 * s;
                    double v = Math.min(0.97, v0 + rise * Math.pow(s, 1.3));
                    double front = f.front(v);
                    double back = f.back(v);
                    double mid = (front + back) / 2;
                    double rf = (front - back) / 2;
                    double cos = 1.0 - 2.0 * u;
                    double sin = Math.sqrt(Math.max(0.0, 1.0 - cos * cos));
                    double across = sign * (sin * f.across(v) * 1.015 + 0.01);
                    left[i] = f.at(mid + rf * cos, v * f.height - width, across);
                    right[i] = f.at(mid + rf * cos, v * f.height + width, across);
                    // the dash: bright behind its head, trailing off, nothing ahead of it
                    double behind = head - s;
                    if (behind < 0) {
                        behind += 1.0;
                    }
                    double a = behind > dash ? 0.0 : Math.pow(1.0 - behind / dash, 1.4);
                    a *= Math.min(1.0, s / 0.06) * Math.min(1.0, (1.0 - s) / 0.15);
                    c[i] = argb(0.9 * a * alpha, wr, wg, wb);
                }
                for (int i = 0; i < n; i++) {
                    if (((c[i] | c[i + 1]) >>> 24) == 0) {
                        continue;
                    }
                    out.add(left[i], right[i], right[i + 1], left[i + 1], c[i], c[i], c[i + 1], c[i + 1]);
                }
            }
        }
    }

    /** 2.2.6: the cutting edge: a white-hot line up the wave's front, turned to the eye, flickering, thinning to the peak. */
    static void edgeLine(Quads out, Frame f, double[] colour, double ticks, double alpha) {
        int n = 24;
        double[][] p = new double[n + 1][];
        for (int i = 0; i <= n; i++) {
            double v = 0.97 * i / n;
            p[i] = f.at(f.front(v) + f.height * 0.004, v * f.height, 0.0);
        }
        double wr = colour[0] + (1 - colour[0]) * 0.9;
        double wg = colour[1] + (1 - colour[1]) * 0.9;
        double wb = colour[2] + (1 - colour[2]) * 0.9;
        double flick = 0.85 + 0.15 * Math.sin(ticks * 2.3);
        for (int i = 0; i < n; i++) {
            double[][] q = new double[4][];
            int[] c = new int[4];
            for (int e = 0; e < 2; e++) {
                int at = i + e;
                double[] a = p[Math.max(0, at - 1)];
                double[] b = p[Math.min(n, at + 1)];
                double[] t = {b[0] - a[0], b[1] - a[1], b[2] - a[2]};
                double[] eye = p[at];
                double[] s = {t[1] * eye[2] - t[2] * eye[1], t[2] * eye[0] - t[0] * eye[2], t[0] * eye[1] - t[1] * eye[0]};
                double sl = Math.max(1.0E-9, length(s));
                double half = (f.height * 0.011 + 0.03) * (1.0 - 0.75 * at / (double) n);
                q[e * 2] = new double[] {p[at][0] - s[0] / sl * half, p[at][1] - s[1] / sl * half, p[at][2] - s[2] / sl * half};
                q[e * 2 + 1] = new double[] {p[at][0] + s[0] / sl * half, p[at][1] + s[1] / sl * half, p[at][2] + s[2] / sl * half};
                c[e * 2] = argb(0.88 * flick * alpha, wr, wg, wb);
                c[e * 2 + 1] = c[e * 2];
            }
            out.add(q[0], q[1], q[3], q[2], c[0], c[1], c[3], c[2]);
        }
    }

    /** A point {@code out} blocks ahead of the leading edge, {@code v} up (0 the trench, 1 the peak): where the crackle runs. */
    public static double[] edge(Frame f, double v, double out, double across) {
        return f.at(f.front(v) + out, v * f.height, across);
    }
}
