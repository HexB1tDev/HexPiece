package com.hexhaki.piece.itsumade;

import com.hexhaki.piece.dragon.FireMesh;
import java.util.Random;

/**
 * 3.9.8: the Itsumade's fire and its Haki's light, drawn as the house draws fire (dragon/FireMesh: flat cel bands cut sharp, never a
 * smear), pure so the previews draw exactly what the game draws (tools/preview/FxPreview: iball, ibeam, iring, iclouds, iburst). Every
 * point is camera-relative; every quad goes to a {@link FireMesh.Sink}. The callers set nothing: each draw sets the palette it needs and
 * puts {@link FireMesh#BAND} back.
 */
public final class ItsuArt {
    /** Its black flames (soot black to a deep violet). */
    public static final int[] YOKAI_BLACK = {0x040205, 0x0A050D, 0x120917, 0x1B0D22, 0x26112E, 0x33163B, 0x431B4A, 0x56225C};
    /** Its Haki's light (dark to light): near-black indigo, indigo, violet, pale violet, green-white (server/RyouServer.ITSU_COLOR). */
    public static final int[] HAKI = {0x07051A, 0x120C3A, 0x1E1A66, 0x2B3FD8, 0x4A52F0, 0x7E6CF5, 0xA9F5D2, 0xEFFFF7};
    /** Thy Death's clouds: soot black with a violet cast. */
    public static final int[] STORM = {0x050408, 0x09070E, 0x0E0B15, 0x14101D, 0x1A1526, 0x221B30, 0x2B233B, 0x352C47};
    public static final int INDIGO = 0x2B3FD8;
    public static final int GREEN = 0x22E08A;
    public static final int NIGHT = 0x07051A;

    private ItsuArt() {
    }

    /**
     * 4.0.2 (the owner: "I asked for a ball of fire specifically different from Kaido"; their Yokai Beam.gif: a blinding white-yellow
     * ball in the open beak, an orange halo round it, thin red-orange streaks across the view): the Yokai fire's sun, {@code radius} its
     * white-hot core: glows of yellow, orange and red round it, two lens streaks across the view through it, at {@code alpha}.
     */
    public static void sun(FireMesh.Sink sink, double x, double y, double z, double radius, double alpha, double time) {
        if (radius <= 0.01 || alpha <= 0.01) {
            return;
        }
        double pulse = 1.0 + 0.06 * Math.sin(time * 1.3);
        // its light round it, thin over its heart (so the heart shows white-hot through it) and thicker toward the edge
        FireMesh.shell(sink, x, y, z, radius * 3.4 * pulse, 0xFF3A12, 0.06 * alpha, 0.0);
        FireMesh.shell(sink, x, y, z, radius * 2.1 * pulse, 0xFF8A2A, 0.08 * alpha, 0.3 * alpha);
        FireMesh.shell(sink, x, y, z, radius * 1.35, 0xFFE27A, 0.1 * alpha, 0.5 * alpha);
        FireMesh.shell(sink, x, y, z, radius, 0xFFFFF6, alpha, 0.95 * alpha);
        streaks(sink, x, y, z, radius * 7.0, radius * 0.07, alpha, 0);
    }

    /**
     * Lens streaks through ({@code x, y, z}) across the view (level with the horizon as the view has it): a long thin red-orange one and a
     * shorter yellow one just over it, bright at the point and gone at their ends.
     */
    static void streaks(FireMesh.Sink sink, double x, double y, double z, double half, double thick, double alpha, int seed) {
        double l = Math.sqrt(x * x + y * y + z * z);
        if (l < 1.0E-3) {
            return;
        }
        // across the view: square to the look at the point and to up
        double sx = -z / l;
        double sz = x / l;
        double sl = Math.hypot(sx, sz);
        if (sl < 1.0E-4) {
            sx = 1.0;
            sz = 0.0;
            sl = 1.0;
        }
        sx /= sl;
        sz /= sl;
        streak(sink, x, y, z, sx, sz, half, thick, 0xFF4A1E, alpha * 0.85);
        streak(sink, x, y + thick * 2.5, z, sx, sz, half * 0.55, thick * 0.6, 0xFFE27A, alpha * 0.9);
    }

    static void streak(FireMesh.Sink sink, double x, double y, double z, double sx, double sz, double half, double thick, int rgb,
                       double alpha) {
        int c = argb(rgb, alpha);
        int clear = rgb & 0xFFFFFF;
        for (int side = -1; side <= 1; side += 2) {
            double ex = x + sx * half * side;
            double ez = z + sz * half * side;
            sink.quad(new double[] {x, y + thick, z, ex, y + thick * 0.2, ez, ex, y - thick * 0.2, ez, x, y - thick, z},
                new int[] {c, clear, clear, c});
        }
    }

    /**
     * The Yokai fire gathering (in the jaws, in the hand): its sun growing, black flames licking round it, and (once it is big) the
     * orange halo of the reference round it facing ({@code wx, wy, wz}) the way it will pour.
     */
    public static void ball(FireMesh.Sink sink, double x, double y, double z, double radius, double time, int seed) {
        ball(sink, x, y, z, radius, time, seed, 0.0, 0.0, 0.0);
    }

    public static void ball(FireMesh.Sink sink, double x, double y, double z, double radius, double time, int seed, double wx, double wy,
                            double wz) {
        try {
            sun(sink, x, y, z, radius * 0.55, 1.0, time);
            double l = Math.sqrt(wx * wx + wy * wy + wz * wz);
            if (l > 1.0E-6) {
                halo(sink, x, y, z, wx / l, wy / l, wz / l, radius * 1.6, 0.8, time);
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /** The reference's orange halo: a thin bright ring square to {@code n}, short rays standing out of it, turning slowly. */
    public static void halo(FireMesh.Sink sink, double x, double y, double z, double nx, double ny, double nz, double radius, double alpha,
                            double time) {
        ring(sink, x, y, z, nx, ny, nz, radius, radius * 0.12, 0xFFB040, 0xFF6A1A, alpha);
        double[] bb = new double[6];
        FireMesh.basis(nx, ny, nz, bb);
        int rays = 16;
        int c = argb(0xFFC860, alpha * 0.8);
        int clear = 0xFF6A1A;
        for (int i = 0; i < rays; i++) {
            double a = time * 0.05 + i * Math.PI * 2.0 / rays;
            double ux = bb[0] * Math.cos(a) + bb[3] * Math.sin(a);
            double uy = bb[1] * Math.cos(a) + bb[4] * Math.sin(a);
            double uz = bb[2] * Math.cos(a) + bb[5] * Math.sin(a);
            double vx = -bb[0] * Math.sin(a) + bb[3] * Math.cos(a);
            double vy = -bb[1] * Math.sin(a) + bb[4] * Math.cos(a);
            double vz = -bb[2] * Math.sin(a) + bb[5] * Math.cos(a);
            double r0 = radius * 1.06;
            double r1 = radius * (i % 2 == 0 ? 1.45 : 1.25);
            double w = radius * 0.035;
            sink.quad(new double[] {x + ux * r0 + vx * w, y + uy * r0 + vy * w, z + uz * r0 + vz * w, x + ux * r1, y + uy * r1, z + uz * r1,
                x + ux * r1, y + uy * r1, z + uz * r1, x + ux * r0 - vx * w, y + uy * r0 - vy * w, z + uz * r0 - vz * w},
                new int[] {c, clear, clear, c});
        }
    }

    /**
     * A beam of the Yokai fire from ({@code x, y, z}) along {@code w} (unit) for {@code reach} blocks, {@code width} across (thin for its
     * first {@code flare} blocks), at {@code fade}: the reference's light first: a white-hot core, an orange glow along it, three thin
     * orange laser lines turning round it, orange fire rolling along it, black flames twisting round in tongues; its sun and its halo
     * where it leaves.
     */
    public static void beam(FireMesh.Sink sink, double x, double y, double z, double[] w, double reach, double width, double fade, double age,
                            int seed, double flare) {
        try {
            double[] bb = new double[6];
            FireMesh.basis(w[0], w[1], w[2], bb);
            // the white-hot core
            FireMesh.tube(sink, x, y, z, x + w[0] * reach, y + w[1] * reach, z + w[2] * reach, width * 0.16, width * 0.26, 0xFFFFF6, 0xFFF4B0,
                0xFFD25A, 0.4, fade, 0.06, age, seed);
            // its light along it
            int glows = (int) Math.max(2, Math.min(24, reach / Math.max(1.0, width * 1.5)));
            for (int k = 0; k < glows; k++) {
                double d = reach * (k + 0.5) / glows;
                com.hexhaki.piece.dragon.UoFire.glow(sink, x + w[0] * d, y + w[1] * d, z + w[2] * d, width * 1.5, 0xFF8A2A, 0.22 * fade);
            }
            // three thin laser lines turning round it
            for (int k = 0; k < 3; k++) {
                double a = age * 0.25 + k * Math.PI * 2.0 / 3.0;
                double off = width * 0.55;
                double ox = (bb[0] * Math.cos(a) + bb[3] * Math.sin(a)) * off;
                double oy = (bb[1] * Math.cos(a) + bb[4] * Math.sin(a)) * off;
                double oz = (bb[2] * Math.cos(a) + bb[5] * Math.sin(a)) * off;
                FireMesh.tube(sink, x + ox, y + oy, z + oz, x + w[0] * reach + ox, y + w[1] * reach + oy, z + w[2] * reach + oz, width * 0.03,
                    width * 0.03, 0xFFE27A, 0xFF8A2A, 0xFF4A1E, 0.3, 0.85 * fade, 0.0, age, seed + 11 + k);
            }
            double step = Math.max(0.35, width * 0.5);
            int n = (int) Math.min(200, reach / step);
            for (int i = 0; i <= n; i++) {
                double d = i * step;
                double taper = Math.min(1.0, 0.35 + d / flare);
                double px = x + w[0] * d;
                double py = y + w[1] * d;
                double pz = z + w[2] * d;
                // the orange fire rolling along it, see-through enough that the core shows
                if (i % 2 == 0) {
                    double heat = 0.6 + 0.2 * FireMesh.rand(seed * 613 + i, 4);
                    FireMesh.palette(FireMesh.BAND);
                    FireMesh.puff(sink, px, py, pz, width * 0.55 * taper, w[0] * 0.8, w[1] * 0.8, w[2] * 0.8, 0.9, 0.5, heat, 0.3, false,
                        0.5 * fade, age + i * 0.29, seed * 613 + i);
                }
            }
            // the black flames: two dark ribbons twisting round it, from thin at the mouth to their full width, flickering along
            for (int k = 0; k < 2; k++) {
                double[] prev = null;
                int pieces = (int) Math.max(6, Math.min(160, reach / Math.max(0.25, width * 0.22)));
                for (int i = 0; i <= pieces; i++) {
                    double d = reach * i / pieces;
                    double taper = Math.min(1.0, 0.3 + d / flare);
                    double a = age * 0.5 + d / Math.max(0.5, width) * 1.1 + k * Math.PI;
                    double off = width * 0.6 * taper;
                    double[] p = {x + w[0] * d + (bb[0] * Math.cos(a) + bb[3] * Math.sin(a)) * off,
                        y + w[1] * d + (bb[1] * Math.cos(a) + bb[4] * Math.sin(a)) * off,
                        z + w[2] * d + (bb[2] * Math.cos(a) + bb[5] * Math.sin(a)) * off};
                    if (prev != null) {
                        double flick = 0.75 + 0.25 * Math.sin(age * 1.7 + i * 0.9 + k);
                        FireMesh.tube(sink, prev[0], prev[1], prev[2], p[0], p[1], p[2], width * 0.09 * taper * flick, width * 0.09 * taper * flick,
                            0x1B0D22, 0x0A050D, 0x431B4A, 0.3, 0.92 * fade, 0.0, age, seed * 31 + i * 2 + k);
                    }
                    prev = p;
                }
            }
            // where it leaves: its sun and its halo
            sun(sink, x, y, z, width * 0.45, fade, age);
            halo(sink, x + w[0] * width * 0.6, y + w[1] * width * 0.6, z + w[2] * width * 0.6, w[0], w[1], w[2], width * 1.4, 0.8 * fade, age);
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /**
     * 4.0.2 (the owner: "I asked for a big ball of fire at the explosion"): the Yokai Beam's end, {@code radius} round, {@code k} 0 the
     * blast to 1 gone: a sun swelling out in its first ticks inside a ball of rolling orange fire, black flames tearing up off it, a glow
     * far round it and the lens streaks through it; then the fire cools from yellow to red and rises away as dark smoke.
     */
    public static void fireball(FireMesh.Sink sink, double x, double y, double z, double radius, double k, double time, int seed) {
        if (k >= 1.0) {
            return;
        }
        double swell = Math.min(1.0, k * 7.0);
        double r = radius * (0.35 + 0.65 * swell * swell * (3 - 2 * swell));
        double fade = k < 0.6 ? 1.0 : Math.max(0.0, 1.0 - (k - 0.6) / 0.4);
        double cool = clamp01(k / 0.8);
        double lift = radius * 0.35 * k;
        try {
            sun(sink, x, y + lift, z, r * 0.42 * (1.0 - 0.5 * cool), fade * (1.0 - 0.6 * cool), time);
            FireMesh.palette(FireMesh.BAND);
            int n = 12;
            for (int i = 0; i < n; i++) {
                double[] d = sphere(i, n);
                double rr = r * (0.52 + 0.1 * FireMesh.rand(seed + i, 1));
                FireMesh.puff(sink, x + d[0] * r * 0.55, y + lift + d[1] * r * 0.5, z + d[2] * r * 0.55, rr, d[0] * 0.3, 0.25, d[2] * 0.3, 0.5,
                    0.8, 0.85 - 0.45 * cool, 0.4, true, 0.85 * fade, time + i, seed * 17 + i);
            }
            if (k > 0.45) {
                FireMesh.palette(FireMesh.SMOKE);
                double smoke = (k - 0.45) / 0.55;
                for (int i = 0; i < 8; i++) {
                    double[] d = sphere(i, 8);
                    FireMesh.puff(sink, x + d[0] * r * 0.6, y + lift + r * (0.4 + smoke * 0.8) + d[1] * r * 0.3, z + d[2] * r * 0.6,
                        r * (0.4 + 0.3 * smoke), 0.0, 0.2, 0.0, 0.3, 0.6, 0.3, 0.2, false, 0.7 * (1.0 - smoke), time + i, seed * 41 + i);
                }
            }
            com.hexhaki.piece.dragon.UoFire.glow(sink, x, y + lift, z, r * 2.2, 0xFF7A22, 0.35 * fade * (1.0 - cool * 0.7));
            streaks(sink, x, y + lift, z, r * 4.0 * (1.0 - 0.5 * k), r * 0.05, fade * (1.0 - cool), seed);
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /** The {@code i}-th of {@code n} points spread over a sphere (unit). */
    static double[] sphere(int i, int n) {
        double y = 1.0 - (i + 0.5) * 2.0 / n;
        double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
        double a = i * 2.399963;
        return new double[] {Math.cos(a) * r, y, Math.sin(a) * r};
    }

    /**
     * A flat ring ({@code x, y, z} its middle, {@code n} its normal), {@code radius} out to its edge and {@code width} wide: {@code inner}
     * on its inside edge (a third of {@code alpha}), {@code outer} at its middle ({@code alpha} three quarters), fading out to nothing at
     * its outside edge.
     */
    public static void ring(FireMesh.Sink sink, double x, double y, double z, double nx, double ny, double nz, double radius, double width,
                            int inner, int outer, double alpha) {
        if (alpha <= 0.01 || radius <= 0.01) {
            return;
        }
        double l = Math.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= l;
        ny /= l;
        nz /= l;
        // two ways across the ring's plane
        double ax = Math.abs(ny) < 0.9 ? 0 : 1;
        double ay = Math.abs(ny) < 0.9 ? 1 : 0;
        double ux = ay * nz;
        double uy = -ax * nz;
        double uz = ax * ny - ay * nx;
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul;
        uy /= ul;
        uz /= ul;
        double vx = ny * uz - nz * uy;
        double vy = nz * ux - nx * uz;
        double vz = nx * uy - ny * ux;
        int a = (int) (Math.min(1.0, alpha) * 255.0);
        int ci = ((a / 3) << 24) | (inner & 0xFFFFFF);
        int cm = ((a * 3 / 4) << 24) | (outer & 0xFFFFFF);
        int co = outer & 0xFFFFFF;
        int segments = (int) Math.max(24, Math.min(96, radius * 10));
        double r0 = Math.max(0.0, radius - width);
        double rm = r0 + (radius - r0) * 0.5;
        double[] xyz = new double[12];
        int[] argb = new int[4];
        for (int i = 0; i < segments; i++) {
            double t0 = i * Math.PI * 2.0 / segments;
            double t1 = (i + 1) * Math.PI * 2.0 / segments;
            double c0 = Math.cos(t0);
            double s0 = Math.sin(t0);
            double c1 = Math.cos(t1);
            double s1 = Math.sin(t1);
            put(xyz, 0, x, y, z, ux, uy, uz, vx, vy, vz, c0 * r0, s0 * r0);
            put(xyz, 1, x, y, z, ux, uy, uz, vx, vy, vz, c0 * rm, s0 * rm);
            put(xyz, 2, x, y, z, ux, uy, uz, vx, vy, vz, c1 * rm, s1 * rm);
            put(xyz, 3, x, y, z, ux, uy, uz, vx, vy, vz, c1 * r0, s1 * r0);
            argb[0] = ci;
            argb[1] = cm;
            argb[2] = cm;
            argb[3] = ci;
            sink.quad(xyz, argb);
            put(xyz, 0, x, y, z, ux, uy, uz, vx, vy, vz, c0 * rm, s0 * rm);
            put(xyz, 1, x, y, z, ux, uy, uz, vx, vy, vz, c0 * radius, s0 * radius);
            put(xyz, 2, x, y, z, ux, uy, uz, vx, vy, vz, c1 * radius, s1 * radius);
            put(xyz, 3, x, y, z, ux, uy, uz, vx, vy, vz, c1 * rm, s1 * rm);
            argb[0] = cm;
            argb[1] = co;
            argb[2] = co;
            argb[3] = cm;
            sink.quad(xyz, argb);
        }
    }

    static void put(double[] xyz, int k, double x, double y, double z, double ux, double uy, double uz, double vx, double vy, double vz,
                    double a, double b) {
        xyz[k * 3] = x + ux * a + vx * b;
        xyz[k * 3 + 1] = y + uy * a + vy * b;
        xyz[k * 3 + 2] = z + uz * a + vz * b;
    }

    /**
     * 4.0.2 (the owner: "the clouds should be fucking clouds or not whatever the fuck this is"): Thy Death's storm over ({@code x, z}) at the
     * height {@code y}, {@code radius} round: soft billows of dark storm cloud, flattened (wider than tall), their tops lit a grey-violet
     * and their bellies near black, every edge soft (no outline, never a solid lump), a lower layer of darker cloud under them, all
     * churning slowly; drawn from the furthest to the nearest so each soft edge lies over what is behind it. {@code grow} (0 to 1) how far
     * they have gathered, {@code alpha} how thick.
     */
    public static void clouds(FireMesh.Sink sink, double x, double y, double z, double radius, double grow, double alpha, double time, int seed) {
        if (alpha <= 0.01) {
            return;
        }
        Random r = new Random(seed * 7919L);
        int top = 96;
        int under = 34;
        double[][] puffs = new double[top + under][];
        for (int i = 0; i < top + under; i++) {
            boolean low = i >= top;
            double a = r.nextDouble() * Math.PI * 2.0 + time * 0.0025 * (i % 2 == 0 ? 1 : -1);
            double d = radius * Math.sqrt(r.nextDouble()) * (low ? 0.85 : 1.0);
            double size = (low ? 7.0 : 5.5) + r.nextDouble() * 5.0;
            double swell = 0.55 + 0.45 * grow + 0.05 * Math.sin(time * 0.05 + i);
            puffs[i] = new double[] {x + Math.cos(a) * d, y + (low ? -2.2 : (r.nextDouble() - 0.3) * 3.0), z + Math.sin(a) * d,
                size * swell, low ? 1 : 0, i};
        }
        java.util.Arrays.sort(puffs, (p, q) -> Double.compare(q[0] * q[0] + q[1] * q[1] + q[2] * q[2], p[0] * p[0] + p[1] * p[1] + p[2] * p[2]));
        for (double[] p : puffs) {
            boolean low = p[4] > 0.5;
            softPuff(sink, p[0], p[1], p[2], p[3], p[3] * (low ? 0.42 : 0.55), low ? 0x2A2532 : 0x6A6478, low ? 0x0C0A10 : 0x1C1924,
                alpha * (low ? 0.9 : 0.82));
        }
    }

    /**
     * A soft billow facing the eye (at the origin): {@code half} wide and {@code tall} high each way from its middle, lit {@code lit} at
     * its top and {@code dark} at its belly, thickest in its middle and clear at its edge.
     */
    static void softPuff(FireMesh.Sink sink, double x, double y, double z, double half, double tall, int lit, int dark, double alpha) {
        double l = Math.sqrt(x * x + y * y + z * z);
        if (l < half * 0.5) {
            return; // the eye inside it: drawn, it would fill the view
        }
        double vx = x / l;
        double vy = y / l;
        double vz = z / l;
        // across the view, and up it
        double rx = -vz;
        double rz = vx;
        double rl = Math.hypot(rx, rz);
        if (rl < 1.0E-4) {
            rx = 1.0;
            rz = 0.0;
            rl = 1.0;
        }
        rx /= rl;
        rz /= rl;
        // up the view: across x the look (square to both, pointing up)
        double ux = -rz * vy;
        double uy = rz * vx - rx * vz;
        double uz = rx * vy;
        int mid = argb(mix(dark, lit, 0.5), alpha);
        int segments = 20;
        double[] xyz = new double[12];
        for (int i = 0; i < segments; i++) {
            double a0 = i * Math.PI * 2.0 / segments;
            double a1 = (i + 1) * Math.PI * 2.0 / segments;
            // a lumpy outline (a cloud, not a disc)
            double k0 = 1.0 + 0.16 * Math.sin(a0 * 3.0 + x) + 0.08 * Math.sin(a0 * 5.0 + z);
            double k1 = 1.0 + 0.16 * Math.sin(a1 * 3.0 + x) + 0.08 * Math.sin(a1 * 5.0 + z);
            double c0 = Math.cos(a0) * half * k0;
            double s0 = Math.sin(a0) * tall * k0;
            double c1 = Math.cos(a1) * half * k1;
            double s1 = Math.sin(a1) * tall * k1;
            // the rim lit by how high it is, and clear
            int e0 = mix(dark, lit, 0.5 + 0.5 * Math.sin(a0)) & 0xFFFFFF;
            int e1 = mix(dark, lit, 0.5 + 0.5 * Math.sin(a1)) & 0xFFFFFF;
            // half way out: still thick
            int h0 = argb(mix(dark, lit, 0.5 + 0.45 * Math.sin(a0)), alpha * 0.8);
            int h1 = argb(mix(dark, lit, 0.5 + 0.45 * Math.sin(a1)), alpha * 0.8);
            double m = 0.55;
            // inner: middle to half way
            xyz[0] = x;
            xyz[1] = y;
            xyz[2] = z;
            xyz[3] = x + (rx * c0 + ux * s0) * m;
            xyz[4] = y + uy * s0 * m;
            xyz[5] = z + (rz * c0 + uz * s0) * m;
            xyz[6] = x + (rx * c1 + ux * s1) * m;
            xyz[7] = y + uy * s1 * m;
            xyz[8] = z + (rz * c1 + uz * s1) * m;
            xyz[9] = x;
            xyz[10] = y;
            xyz[11] = z;
            sink.quad(xyz.clone(), new int[] {mid, h0, h1, mid});
            // outer: half way to the clear edge
            double[] o = {x + (rx * c0 + ux * s0) * m, y + uy * s0 * m, z + (rz * c0 + uz * s0) * m, x + rx * c0 + ux * s0, y + uy * s0,
                z + rz * c0 + uz * s0, x + rx * c1 + ux * s1, y + uy * s1, z + rz * c1 + uz * s1, x + (rx * c1 + ux * s1) * m,
                y + uy * s1 * m, z + (rz * c1 + uz * s1) * m};
            sink.quad(o, new int[] {h0, e0, e1, h1});
        }
    }

    /**
     * Thy Death's bolt bursting where it strikes the ground ({@code x, y, z}): a ball of its Haki's light {@code radius} round, swelling
     * in its first ticks, a ring of it running out over the ground; {@code k} 0 the strike to 1 gone.
     */
    public static void burst(FireMesh.Sink sink, double x, double y, double z, double radius, double k, double time, int seed) {
        if (k >= 1.0) {
            return;
        }
        double swell = radius * Math.min(1.0, 0.35 + k * 4.0);
        try {
            FireMesh.palette(HAKI);
            FireMesh.puff(sink, x, y + swell * 0.6, z, swell * 0.55, 0.0, 0.05, 0.0, 0.0, 0.4, 0.95 - 0.3 * k, 0.1, false, 1.0 - k, time, seed + 3);
            FireMesh.puff(sink, x, y + swell * 0.6, z, swell, 0.0, 0.05, 0.0, 0.0, 0.7, 0.42 - 0.2 * k, 0.15, true, 0.6 * (1.0 - k * k), time, seed);
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /**
     * One bolt of Thy Death ({@code dx, dy, dz}: blocks from the cloud down to where it strikes): 4.0.2 (the owner: "shooting from the
     * cloud, guess what it is? The old outdated lighting, the 2D lighting"): the release's very bolt (haki/ConquerorStorm.between), at the
     * release's size, its rim tinted to its owner's Haki when it is let go.
     */
    public static java.util.List<com.hexhaki.piece.haki.Bolt> skyStroke(double dx, double dy, double dz, long seed) {
        return com.hexhaki.piece.haki.ConquerorStorm.between(new double[] {0, 0, 0}, new double[] {dx, dy, dz}, 1.0, 0.0F, seed);
    }

    // ------------------------------------------------------------------------------------------------------ 3.9.9: Hellish Teleport

    /**
     * The gate's fire (the owner's reference, Haki_and_Portalfire.gif): cyan flames, white-cyan in their hearts, their edges hot pink:
     * FireMesh pulls a puff's edge toward its RIM band, so pink sits just under the cyan.
     */
    public static final int[] GATE_FIRE = {0x2A0630, 0x5A0C55, 0x9A1478, 0xD8249A, 0xFF4FB8, 0xFF6FC8, 0x3FE6F0, 0xC8FFFF};
    /**
     * 3.9.9 (the owner: "the summoning circle lacks runes. the color also isn't it, the original is a very dark shade of purple/blue";
     * input/itsumade/pictures/pentagram_frame.png): the circle's own colours: a very dark purple-blue disc, a darker band round it full of
     * pale lavender runes, the star's points a deep blue with a pink mark in each, every line thin and pale cyan-white.
     */
    public static final int DISC = 0x17143F;
    public static final int RING_BAND = 0x0C0A27;
    public static final int STAR_FILL = 0x1C2A74;
    public static final int LINE_CORE = 0xF2FDFF;
    public static final int LINE = 0xA9E6FF;
    public static final int LINE_GLOW = 0x4A3FD0;
    public static final int RUNE = 0xD9C8FF;
    public static final int RUNE_GLOW = 0x6A3FD8;
    public static final int MARK = 0xFF4FC8;
    public static final int MARK_GLOW = 0x8A1A78;

    /** The ground's height under a point ({@code x, z} camera-relative, as every point here), for the pentagram to lie on. */
    public interface Ground {
        double at(double x, double z);
    }

    /** How long the pentagram takes to form (ticks): its circles traced, then the star drawn point to point, then it burns. */
    public static final int GATE_FORM = 200;
    public static final double GATE_RADIUS = 3.0;
    /** How many runes run round its band. */
    static final int RUNES = 40;

    /**
     * The pentagram ({@code GATE_RADIUS} round its middle {@code x, z}; six blocks across) at {@code age} ticks of its forming, laid on
     * the ground as {@code ground} has it (every piece of it on the top of the block under it, its lines climbing the face of a step), at
     * {@code alpha}: its dark disc welling up (to 100), the outer circle traced round (to 60), the inner circle (40 to 90), the runes
     * written one after another round the band (50 to 130), the star's five lines drawn point to point (80 to 160) and its marks (150 to
     * 175), then all of it burning brighter to {@link #GATE_FORM}; cyan fire with pink edges licking up round the outer circle as it forms.
     */
    public static void pentagram(FireMesh.Sink sink, Ground ground, double x, double z, double age, double alpha, int seed) {
        if (alpha <= 0.01) {
            return;
        }
        double r = GATE_RADIUS;
        double ri = r * 0.84;
        double burn = clamp01((age - 150.0) / 50.0);
        double width = 0.035 + 0.015 * burn;
        double a = alpha * (0.8 + 0.2 * burn);
        disc(sink, ground, x, z, r, ri, alpha * clamp01(age / 100.0), clamp01((age - 120.0) / 50.0));
        // the circles: the outer edge and both edges of the band
        arcOnGround(sink, ground, x, z, r, 0.0, clamp01(age / 60.0), width * 1.3, a);
        arcOnGround(sink, ground, x, z, ri, Math.PI, clamp01((age - 40.0) / 50.0), width, a);
        // the runes round the band, written one after another
        double rb = (r + ri) * 0.5;
        for (int j = 0; j < RUNES; j++) {
            double shown = clamp01((age - 50.0 - j * 80.0 / RUNES) / 6.0);
            if (shown <= 0.0) {
                break;
            }
            double th = j * Math.PI * 2.0 / RUNES + 0.05;
            rune(sink, ground, x, z, th, rb, (r - ri) * 0.68, seed * 31 + j, a * shown);
        }
        // the star: five points on the inner circle, each joined to the one two along, drawn one after another
        for (int k = 0; k < 5; k++) {
            double t = clamp01((age - 80.0 - k * 16.0) / 16.0);
            if (t <= 0.0) {
                continue;
            }
            double a0 = Math.PI / 2.0 + k * 2.0 * Math.PI * 2.0 / 5.0;
            double a1 = Math.PI / 2.0 + (k + 1) * 2.0 * Math.PI * 2.0 / 5.0;
            double x0 = x + Math.cos(a0) * ri;
            double z0 = z + Math.sin(a0) * ri;
            double x1 = x + Math.cos(a1) * ri;
            double z1 = z + Math.sin(a1) * ri;
            lineOnGround(sink, ground, x0, z0, x0 + (x1 - x0) * t, z0 + (z1 - z0) * t, width, a);
        }
        // a pink mark in each point of the star, pointing in to its middle
        for (int k = 0; k < 5; k++) {
            double shown = clamp01((age - 150.0 - k * 4.0) / 6.0);
            if (shown > 0.0) {
                mark(sink, ground, x, z, Math.PI / 2.0 + k * Math.PI * 2.0 / 5.0, ri, a * shown);
            }
        }
        // the fire round it, as far round as the circle has gone, taller once it burns: tongues of cyan with pink edges licking up
        double round = clamp01(age / 60.0);
        int flames = 56;
        FireMesh.palette(GATE_FIRE);
        for (int k = 0; k < flames; k++) {
            double u = (k + 0.5) / flames;
            if (u > round) {
                break;
            }
            double ang = u * Math.PI * 2.0;
            double rr = r + 0.22 + 0.12 * Math.sin(k * 1.9);
            double fx = x + Math.cos(ang) * rr;
            double fz = z + Math.sin(ang) * rr;
            double fy = ground.at(fx, fz);
            double flick = 0.7 + 0.3 * Math.sin(age * 0.9 + k * 2.3);
            double size = (0.12 + 0.09 * burn) * flick * Math.min(1.0, (round - u) * 12.0);
            // moving down so FireMesh draws its tongue out behind it, upward: a flame's tip
            FireMesh.puff(sink, fx, fy + size * 0.8, fz, size, 0.0, -1.0, 0.0, 1.1 + 0.5 * burn, 0.55, 0.86, 0.7, false, alpha,
                age + k * 1.7, seed * 53 + k);
        }
        FireMesh.palette(FireMesh.BAND);
    }

    /**
     * The circle's dark disc on the ground: cut into small pieces round its middle (rings of 0.375, pieces of about 0.4 round), each flat
     * on the block under it, coloured by where it lies (the band, a point of the star, the rest); the star's points fill in at
     * {@code star}.
     */
    static void disc(FireMesh.Sink sink, Ground ground, double x, double z, double r, double ri, double alpha, double star) {
        if (alpha <= 0.01) {
            return;
        }
        int rings = 8;
        double[] xyz = new double[12];
        int[] argb = new int[4];
        for (int i = 0; i < rings; i++) {
            double r0 = r * i / rings;
            double r1 = r * (i + 1) / rings;
            int segs = (int) Math.max(10, Math.ceil(r1 * Math.PI * 2.0 / 0.4));
            for (int j = 0; j < segs; j++) {
                double a0 = j * Math.PI * 2.0 / segs;
                double a1 = (j + 1) * Math.PI * 2.0 / segs;
                double rm = (r0 + r1) * 0.5;
                double am = (a0 + a1) * 0.5;
                double cx = Math.cos(am) * rm;
                double cz = Math.sin(am) * rm;
                double h = ground.at(x + cx, z + cz) + 0.012;
                int c;
                if (rm > ri) {
                    c = argb(RING_BAND, alpha);
                } else if (star > 0.0 && inStar(cx, cz, ri)) {
                    c = argb(mix(DISC, STAR_FILL, star), alpha);
                } else {
                    c = argb(DISC, alpha);
                }
                // each piece a little over its neighbours (the rings' pieces do not meet corner to corner: no hairline between them)
                double o0 = Math.max(0.0, r0 - 0.03);
                double o1 = r1 + 0.03;
                double b0 = a0 - 0.012;
                double b1 = a1 + 0.012;
                xyz[0] = x + Math.cos(b0) * o0;
                xyz[1] = h;
                xyz[2] = z + Math.sin(b0) * o0;
                xyz[3] = x + Math.cos(b0) * o1;
                xyz[4] = h;
                xyz[5] = z + Math.sin(b0) * o1;
                xyz[6] = x + Math.cos(b1) * o1;
                xyz[7] = h;
                xyz[8] = z + Math.sin(b1) * o1;
                xyz[9] = x + Math.cos(b1) * o0;
                xyz[10] = h;
                xyz[11] = z + Math.sin(b1) * o0;
                argb[0] = c;
                argb[1] = c;
                argb[2] = c;
                argb[3] = c;
                sink.quad(xyz, argb);
            }
        }
    }

    /** Whether ({@code px, pz}) lies in the star drawn on the circle {@code ri} round (its ten corners, the points' and the inner ones). */
    static boolean inStar(double px, double pz, double ri) {
        double inner = ri * Math.cos(Math.PI * 2.0 / 5.0) / Math.cos(Math.PI / 5.0);
        boolean in = false;
        for (int i = 0, j = 9; i < 10; j = i++) {
            double ai = Math.PI / 2.0 + i * Math.PI / 5.0;
            double aj = Math.PI / 2.0 + j * Math.PI / 5.0;
            double rI = i % 2 == 0 ? ri : inner;
            double rJ = j % 2 == 0 ? ri : inner;
            double xi = Math.cos(ai) * rI;
            double zi = Math.sin(ai) * rI;
            double xj = Math.cos(aj) * rJ;
            double zj = Math.sin(aj) * rJ;
            if ((zi > pz) != (zj > pz) && px < (xj - xi) * (pz - zi) / (zj - zi) + xi) {
                in = !in;
            }
        }
        return in;
    }

    static int mix(int a, int b, double t) {
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (r << 16) | (g << 8) | bl;
    }

    /**
     * One rune in the band, its foot toward the middle, read round the circle: two to four strokes out of a little alphabet of bars,
     * slants, hooks and forks (seeded, so each rune is its own and always the same), pale lavender over a violet glow.
     */
    static void rune(FireMesh.Sink sink, Ground ground, double x, double z, double th, double rb, double tall, int seed, double alpha) {
        // across (along the circle) and up (out from the middle) the rune
        double tx = -Math.sin(th);
        double tz = Math.cos(th);
        double nx = Math.cos(th);
        double nz = Math.sin(th);
        double cx = x + nx * rb;
        double cz = z + nz * rb;
        double hh = tall * 0.5;
        double hw = tall * 0.32;
        // each stroke: from (u0, v0) to (u1, v1), in halves of the rune's width and height
        double[][] alphabet = {
            {0, -1, 0, 1}, {-1, -1, 1, 1}, {1, -1, -1, 1}, {-1, 1, 1, 1}, {-1, 0, 1, 0}, {-1, -1, 1, -1}, {0, 1, 1, 0.3},
            {-1, 1, 0, 0}, {0, 0, 1, 1}, {-1, -1, -1, 1}, {1, -1, 1, 1}, {0, -0.2, 1, -1}, {-1, 0.4, 0, -0.4}};
        Random r = new Random(seed * 2654435761L);
        int strokes = 2 + r.nextInt(3);
        int first = r.nextInt(3); // most runes stand on a stem
        for (int s = 0; s < strokes; s++) {
            double[] st = alphabet[s == 0 ? (first == 0 ? 0 : first == 1 ? 9 : 10) : r.nextInt(alphabet.length)];
            double x0 = cx + tx * st[0] * hw + nx * st[1] * hh;
            double z0 = cz + tz * st[0] * hw + nz * st[1] * hh;
            double x1 = cx + tx * st[2] * hw + nx * st[3] * hh;
            double z1 = cz + tz * st[2] * hw + nz * st[3] * hh;
            stroke(sink, ground, x0, z0, x1, z1, tall * 0.075, RUNE, RUNE_GLOW, alpha);
        }
    }

    /** A pink mark in the point of the star at {@code th}: a chevron pointing in to the middle, a stroke through it. */
    static void mark(FireMesh.Sink sink, Ground ground, double x, double z, double th, double ri, double alpha) {
        double nx = Math.cos(th);
        double nz = Math.sin(th);
        double tx = -nz;
        double tz = nx;
        double tip = ri * 0.42;
        double back = ri * 0.62;
        double half = ri * 0.08;
        stroke(sink, ground, x + nx * back + tx * half, z + nz * back + tz * half, x + nx * tip, z + nz * tip, 0.04, MARK, MARK_GLOW, alpha);
        stroke(sink, ground, x + nx * back - tx * half, z + nz * back - tz * half, x + nx * tip, z + nz * tip, 0.04, MARK, MARK_GLOW, alpha);
        stroke(sink, ground, x + nx * (back + ri * 0.08), z + nz * (back + ri * 0.08), x + nx * (tip + ri * 0.04), z + nz * (tip + ri * 0.04),
            0.03, MARK, MARK_GLOW, alpha);
    }

    /** A short stroke flat on the ground (the height under its middle): its colour over a soft glow of {@code glow}. */
    static void stroke(FireMesh.Sink sink, Ground ground, double x0, double z0, double x1, double z1, double width, int colour, int glow,
                       double alpha) {
        double dx = x1 - x0;
        double dz = z1 - z0;
        double l = Math.hypot(dx, dz);
        if (l < 1.0E-6 || alpha <= 0.01) {
            return;
        }
        double sx = -dz / l;
        double sz = dx / l;
        // the stroke runs a hair past its ends so strokes meet
        double ex = dx / l * width * 0.5;
        double ez = dz / l * width * 0.5;
        double y = ground.at((x0 + x1) * 0.5, (z0 + z1) * 0.5) + 0.03;
        int g = argb(glow, alpha);
        double gw = width * 0.95;
        sink.quad(new double[] {x0 - ex * 2 + sx * gw, y, z0 - ez * 2 + sz * gw, x1 + ex * 2 + sx * gw, y, z1 + ez * 2 + sz * gw,
            x1 + ex * 2 - sx * gw, y, z1 + ez * 2 - sz * gw, x0 - ex * 2 - sx * gw, y, z0 - ez * 2 - sz * gw}, new int[] {g, g, g, g});
        int c = argb(colour, alpha);
        double w = width * 0.5;
        sink.quad(new double[] {x0 - ex + sx * w, y + 0.004, z0 - ez + sz * w, x1 + ex + sx * w, y + 0.004, z1 + ez + sz * w,
            x1 + ex - sx * w, y + 0.004, z1 + ez - sz * w, x0 - ex - sx * w, y + 0.004, z0 - ez - sz * w}, new int[] {c, c, c, c});
    }

    static double clamp01(double v) {
        return v < 0.0 ? 0.0 : v > 1.0 ? 1.0 : v;
    }

    /** An arc of a circle on the ground from {@code start} (radians) round {@code part} of the way. */
    static void arcOnGround(FireMesh.Sink sink, Ground ground, double x, double z, double radius, double start, double part, double width,
                            double alpha) {
        if (part <= 0.0) {
            return;
        }
        int n = (int) Math.max(8, Math.ceil(radius * 2.0 * Math.PI * part / 0.35));
        double px = x + Math.cos(start) * radius;
        double pz = z + Math.sin(start) * radius;
        for (int i = 1; i <= n; i++) {
            double ang = start + part * Math.PI * 2.0 * i / n;
            double qx = x + Math.cos(ang) * radius;
            double qz = z + Math.sin(ang) * radius;
            segment(sink, ground, px, pz, qx, qz, width, alpha);
            px = qx;
            pz = qz;
        }
    }

    /** A straight line on the ground, cut into short pieces so each lies on its own block. */
    static void lineOnGround(FireMesh.Sink sink, Ground ground, double x0, double z0, double x1, double z1, double width, double alpha) {
        double l = Math.hypot(x1 - x0, z1 - z0);
        int n = (int) Math.max(1, Math.ceil(l / 0.3));
        for (int i = 0; i < n; i++) {
            double t0 = (double) i / n;
            double t1 = (double) (i + 1) / n;
            segment(sink, ground, x0 + (x1 - x0) * t0, z0 + (z1 - z0) * t0, x0 + (x1 - x0) * t1, z0 + (z1 - z0) * t1, width, alpha);
        }
    }

    /**
     * One short piece of a glowing line flat on the ground (a violet glow, the pink line, its pink-white heart), a hair over the block's
     * top; where the ground steps between its ends, a piece up (or down) the step's face joins them.
     */
    static void segment(FireMesh.Sink sink, Ground ground, double x0, double z0, double x1, double z1, double width, double alpha) {
        double dx = x1 - x0;
        double dz = z1 - z0;
        double l = Math.hypot(dx, dz);
        if (l < 1.0E-6) {
            return;
        }
        double sx = -dz / l;
        double sz = dx / l;
        double y0 = ground.at(x0, z0) + 0.03;
        double y1 = ground.at(x1, z1) + 0.03;
        double mx = (x0 + x1) * 0.5;
        double mz = (z0 + z1) * 0.5;
        if (Math.abs(y1 - y0) > 0.3) {
            // a step: each half on its own block, a riser up the face between them
            flat(sink, x0, y0, z0, mx, mz, sx, sz, width, alpha);
            flat(sink, mx, y1, mz, x1, z1, sx, sz, width, alpha);
            riser(sink, mx, mz, y0, y1, sx, sz, width, alpha);
            return;
        }
        segmentAt(sink, x0, y0, z0, x1, y1, z1, sx, sz, width, alpha);
    }

    static void flat(FireMesh.Sink sink, double x0, double y, double z0, double x1, double z1, double sx, double sz, double width,
                     double alpha) {
        segmentAt(sink, x0, y, z0, x1, y, z1, sx, sz, width, alpha);
    }

    /**
     * One piece of line from ({@code x0, y0, z0}) to ({@code x1, y1, z1}), {@code sx, sz} across it: a solid violet halo, the pale line,
     * its white heart, each a hair over the last. Solid, never see-through at its edges: a see-through glow still hides what is drawn
     * after it under it (the disc showed the grass through in teeth round every line).
     */
    static void segmentAt(FireMesh.Sink sink, double x0, double y0, double z0, double x1, double y1, double z1, double sx, double sz,
                          double width, double alpha) {
        double[] widths = {width * 2.3, width, width * 0.4};
        int[] colours = {LINE_GLOW, LINE, LINE_CORE};
        double[] xyz = new double[12];
        int[] argb = new int[4];
        for (int k = 0; k < 3; k++) {
            double w = widths[k];
            double lift = k * 0.004;
            int c = argb(colours[k], alpha);
            xyz[0] = x0 + sx * w;
            xyz[1] = y0 + lift;
            xyz[2] = z0 + sz * w;
            xyz[3] = x1 + sx * w;
            xyz[4] = y1 + lift;
            xyz[5] = z1 + sz * w;
            xyz[6] = x1 - sx * w;
            xyz[7] = y1 + lift;
            xyz[8] = z1 - sz * w;
            xyz[9] = x0 - sx * w;
            xyz[10] = y0 + lift;
            xyz[11] = z0 - sz * w;
            argb[0] = c;
            argb[1] = c;
            argb[2] = c;
            argb[3] = c;
            sink.quad(xyz, argb);
        }
    }

    /** The glow's half strip: {@code inner} along the line, {@code outer} (clear) {@code w} out to the side. */
    static void half(FireMesh.Sink sink, double x0, double y0, double z0, double x1, double y1, double z1, double sx, double sz, double w,
                     int inner, int outer) {
        double[] xyz = {x0, y0, z0, x1, y1, z1, x1 + sx * w, y1, z1 + sz * w, x0 + sx * w, y0, z0 + sz * w};
        sink.quad(xyz, new int[] {inner, inner, outer, outer});
    }

    /** The line up a step's face (vertical, square to the line), from {@code y0} to {@code y1}. */
    static void riser(FireMesh.Sink sink, double x, double z, double y0, double y1, double sx, double sz, double width, double alpha) {
        int g = argb(LINE_GLOW, alpha);
        int c = argb(LINE, alpha);
        double gw = width * 2.3;
        sink.quad(new double[] {x + sx * gw, y0, z + sz * gw, x + sx * gw, y1, z + sz * gw, x - sx * gw, y1, z - sz * gw, x - sx * gw, y0,
            z - sz * gw}, new int[] {g, g, g, g});
        double w = width;
        // the line a hair out from its halo on both faces of the step's face
        sink.quad(new double[] {x + sx * w, y0, z + sz * w, x + sx * w, y1, z + sz * w, x - sx * w, y1, z - sz * w, x - sx * w, y0, z - sz * w},
            new int[] {c, c, c, c});
    }

    static int argb(int rgb, double alpha) {
        return ((int) (Math.max(0.0, Math.min(1.0, alpha)) * 255.0) << 24) | (rgb & 0xFFFFFF);
    }

    /**
     * A pillar of black flames from {@code y0} to {@code y1} over ({@code x, z}), {@code radius} round, at {@code alpha}: 4.0.2 (the owner:
     * "you should be able to see yourself going into the beam of fire into the sky"): hollow, never a solid column: four ribbons of black
     * flame twisting up (or down, when {@code down}) round an open core lit a faint violet, slim black tongues licking off them, the gate's
     * cyan and pink fire among them now and then: whatever is carried up its middle shows through.
     */
    public static void pillar(FireMesh.Sink sink, double x, double y0, double y1, double z, double radius, double age, double alpha,
                              boolean down, int seed) {
        if (alpha <= 0.01 || y1 <= y0) {
            return;
        }
        double h = y1 - y0;
        double flow = down ? -age * 0.9 : age * 0.9;
        try {
            // the ribbons
            int pieces = (int) Math.min(120, Math.max(8, h / 1.0));
            for (int k = 0; k < 6; k++) {
                double[] prev = null;
                for (int i = 0; i <= pieces; i++) {
                    double d = h * i / pieces;
                    double a = (d - flow) / (radius * (1.8 + 0.25 * k)) + k * Math.PI / 3.0;
                    double r = radius * (1.0 + 0.12 * Math.sin(d * 0.7 + k + age * 0.3));
                    double[] p = {x + Math.cos(a) * r, y0 + d, z + Math.sin(a) * r};
                    if (prev != null) {
                        double flick = 0.45 + 0.55 * Math.abs(Math.sin(age * 1.3 + i * 0.6 + k * 2.0));
                        FireMesh.tube(sink, prev[0], prev[1], prev[2], p[0], p[1], p[2], radius * 0.08 * flick, radius * 0.08 * flick, 0x1B0D22,
                            0x0A050D, 0x56225C, 0.35, 0.9 * alpha, 0.0, age, seed * 31 + i * 4 + k);
                    }
                    prev = p;
                }
            }
            // slim tongues off them, and the gate's fire now and then
            int tongues = (int) Math.min(140, h / 0.8);
            for (int i = 0; i < tongues; i++) {
                double d = ((i * 0.8 - flow * 0.5) % h + h) % h;
                double a = i * 2.39 + age * 0.05;
                double tx = x + Math.cos(a) * radius * 1.1;
                double tz = z + Math.sin(a) * radius * 1.1;
                FireMesh.palette(i % 7 == 3 ? GATE_FIRE : YOKAI_BLACK);
                FireMesh.puff(sink, tx, y0 + d, tz, radius * 0.2, Math.cos(a) * 0.2, down ? 1.0 : -1.0, Math.sin(a) * 0.2, 2.4, 1.0,
                    i % 7 == 3 ? 0.86 : 0.5, 0.35, false, 0.85 * alpha, age + i * 0.37, seed * 977 + i);
            }
            // the open core's faint violet light
            int lights = (int) Math.max(2, Math.min(14, h / (radius * 3.0)));
            for (int i = 0; i < lights; i++) {
                double d = h * (i + 0.5) / lights;
                FireMesh.shell(sink, x, y0 + d, z, radius * 1.4, LINE_GLOW, 0.12 * alpha, 0.0);
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /**
     * A body in black flames ({@code x, y, z} its feet, {@code height} tall), {@code k} 0 to 1: slim tongues of black flame licking up all
     * round it (rising over it in its first third), the body showing between them, then (past 0.35) burning down and away. 4.0.2: tongues,
     * never a lump over it (the owner: you see yourself going up into the beam). {@code toward} is no longer used: the body is carried.
     */
    public static void engulf(FireMesh.Sink sink, double x, double y, double z, double height, double k, double[] toward, double age, int seed) {
        if (k >= 1.0) {
            return;
        }
        double over = clamp01(k * 3.0);
        double gone = clamp01((k - 0.35) / 0.65);
        try {
            FireMesh.palette(YOKAI_BLACK);
            int n = 7;
            for (int i = 0; i < n; i++) {
                double a = i * 2.39 + age * 0.15;
                double up = height * ((i * 0.37) % 1.0) * over;
                double r = 0.42 + 0.06 * Math.sin(age * 0.7 + i);
                double size = (0.11 + 0.04 * Math.sin(age * 0.9 + i * 1.7)) * (1.0 - gone * 0.7);
                FireMesh.puff(sink, x + Math.cos(a) * r, y + 0.1 + up, z + Math.sin(a) * r, size, Math.cos(a) * 0.15, -1.0, Math.sin(a) * 0.15,
                    2.6, 1.0, 0.5, 0.35, false, 0.9 * (1.0 - gone * gone), age + i, seed * 71 + i);
            }
            FireMesh.shell(sink, x, y + height * 0.5, z, height * 0.7, LINE_GLOW, 0.14 * (1.0 - gone), 0.0);
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }

}
