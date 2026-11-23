package com.hexhaki.piece.gear;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.2.6: the Nika fruit's drawn shapes, in the fruits' style (POINT ONE: meshes in flat bands, alpha-blended, depth-tested). Pure,
 * previewable (tools/preview FxPreview {@code |nfist}).
 * <p>
 * The Bajrang Gun's giant fist (the owner: "The fist is like should be, I don't know, perhaps like more shaped like a fist and more
 * rounder. I'm not saying make it completely round, but like it should be a fist, a big one"; their reference: Gear 5 Luffy over his
 * giant fist, ballooned like rubber but plainly a fist): a fist blown up like a balloon, each part a rounded lump (a superellipsoid:
 * round, a little boxy): the back of the hand and palm, four fingers in a row along its front (their first joints, a knuckle over each,
 * the curled tips tucked under), the thumb folded across their lower front and its ball at the side, the wrist behind. Cel-shaded: each face in one of four bands by how it faces the light.
 * Its colour is {@link #tint} (white in the game: the player's own skin texel colours it; a skin tone in the preview).
 */
public final class NikaArt {
    /** The light (unit) in the sink's own frame (the client sets the model's, where y is down; the preview the world's). */
    static double lx = -0.33;
    static double ly = 0.79;
    static double lz = -0.51;
    /** Multiplied into every band (0xRRGGBB). */
    public static int tint = 0xFFFFFF;

    private NikaArt() {
    }

    /** The light's way in the sink's frame. */
    public static void light(double x, double y, double z) {
        double l = Math.sqrt(x * x + y * y + z * z);
        lx = x / l;
        ly = y / l;
        lz = z / l;
    }

    static final double[] F = new double[3];
    static final double[] U = new double[3];
    static final double[] S = new double[3];
    static final double[] FINGERS = {-0.57, -0.19, 0.19, 0.57};
    static final double[] REACH = {0.44, 0.50, 0.51, 0.46};

    /**
     * The fist, its middle at ({@code x}, {@code y}, {@code z}), its knuckles toward ({@code fx}, {@code fy}, {@code fz}), the back of the
     * hand toward ({@code ux}, {@code uy}, {@code uz}) (made square to the first), {@code r} its radius (blocks); {@code fine}: more faces
     * (a big fist near the eye). About 700 quads (fine 1,100).
     */
    public static void fist(FireMesh.Sink sink, double x, double y, double z, double fx, double fy, double fz, double ux, double uy, double uz,
                            double r, boolean fine) {
        fist(sink, x, y, z, fx, fy, fz, ux, uy, uz, r, fine, false);
    }

    /**
     * 3.3.9 (the owner, on Dawn Pistol's first fist: "thats the left hand on the right arm"): the fist as above, built as a left hand
     * (its thumb on the side of knuckles x back of the hand) or, {@code right}, mirrored into a right hand (the thumb on the other side).
     */
    public static void fist(FireMesh.Sink sink, double x, double y, double z, double fx, double fy, double fz, double ux, double uy, double uz,
                            double r, boolean fine, boolean right) {
        if (r < 0.01) {
            return;
        }
        double fl = Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (fl < 1.0E-9) {
            return;
        }
        F[0] = fx / fl;
        F[1] = fy / fl;
        F[2] = fz / fl;
        double d = ux * F[0] + uy * F[1] + uz * F[2];
        U[0] = ux - F[0] * d;
        U[1] = uy - F[1] * d;
        U[2] = uz - F[2] * d;
        double ul = Math.sqrt(U[0] * U[0] + U[1] * U[1] + U[2] * U[2]);
        if (ul < 1.0E-6) {
            // any up square to the knuckles
            double[] t = Math.abs(F[1]) < 0.9 ? new double[] {0, 1, 0} : new double[] {1, 0, 0};
            d = t[0] * F[0] + t[1] * F[1] + t[2] * F[2];
            U[0] = t[0] - F[0] * d;
            U[1] = t[1] - F[1] * d;
            U[2] = t[2] - F[2] * d;
            ul = Math.sqrt(U[0] * U[0] + U[1] * U[1] + U[2] * U[2]);
        }
        U[0] /= ul;
        U[1] /= ul;
        U[2] /= ul;
        double hand = right ? -1.0 : 1.0;
        S[0] = (F[1] * U[2] - F[2] * U[1]) * hand;
        S[1] = (F[2] * U[0] - F[0] * U[2]) * hand;
        S[2] = (F[0] * U[1] - F[1] * U[0]) * hand;
        int n = fine ? 14 : 10;
        int m = fine ? 9 : 7;
        // the back of the hand and the palm: the main mass, a little boxy
        blob(sink, x, y, z, r, -0.10, 0.02, 0.0, 0.62, 0.58, 0.80, 2.7, n, m);
        // the wrist behind it, going back into the arm
        blob(sink, x, y, z, r, -0.74, 0.0, 0.0, 0.40, 0.38, 0.46, 2.2, n - 2, m - 2);
        // four fingers curled under, side by side, the middle two the farthest out: the first joints in a row along the front, a
        // knuckle over each, the curled tips tucked in under them
        for (int k = 0; k < 4; k++) {
            blob(sink, x, y, z, r, REACH[k], 0.0, FINGERS[k], 0.30, 0.30, 0.205, 2.4, n - 2, m - 1);
            blob(sink, x, y, z, r, REACH[k] + 0.05, 0.27, FINGERS[k], 0.20, 0.15, 0.18, 2.0, n - 4, m - 3);
            blob(sink, x, y, z, r, REACH[k] - 0.10, -0.34, FINGERS[k], 0.22, 0.16, 0.18, 2.2, n - 4, m - 3);
        }
        // the thumb folded across the front of the first two fingers, low, and its ball at the side
        blob(sink, x, y, z, r, 0.66, -0.34, 0.22, 0.17, 0.17, 0.36, 2.2, n - 2, m - 2);
        blob(sink, x, y, z, r, 0.10, -0.22, 0.62, 0.40, 0.27, 0.22, 2.2, n - 2, m - 2);
    }

    /**
     * One rounded lump of the fist: its middle {@code a}, {@code b}, {@code c} (times {@code r}) along the knuckles, the back of the hand
     * and the thumb's side from the fist's middle, its half sizes {@code rf}, {@code ru}, {@code rs} (times {@code r}) the same ways,
     * {@code e} how boxy (2: an egg; more: boxier), {@code n} faces round and {@code m} rings.
     */
    static void blob(FireMesh.Sink sink, double x, double y, double z, double r, double a, double b, double c, double rf, double ru, double rs,
                     double e, int n, int m) {
        double cx = x + (F[0] * a + U[0] * b + S[0] * c) * r;
        double cy = y + (F[1] * a + U[1] * b + S[1] * c) * r;
        double cz = z + (F[2] * a + U[2] * b + S[2] * c) * r;
        double ka = rf * r;
        double kb = ru * r;
        double kc = rs * r;
        double power = 2.0 / e;
        for (int i = 0; i < m; i++) {
            double p0 = -Math.PI / 2 + Math.PI * i / m;
            double p1 = -Math.PI / 2 + Math.PI * (i + 1) / m;
            for (int j = 0; j < n; j++) {
                double l0 = 2 * Math.PI * j / n;
                double l1 = 2 * Math.PI * (j + 1) / n;
                corner(0, p0, l0, power, ka, kb, kc, cx, cy, cz);
                corner(1, p0, l1, power, ka, kb, kc, cx, cy, cz);
                corner(2, p1, l1, power, ka, kb, kc, cx, cy, cz);
                corner(3, p1, l0, power, ka, kb, kc, cx, cy, cz);
                // the face's way out (the lump's own normal at its middle), banded by the light
                double pm = (p0 + p1) * 0.5;
                double lm = (l0 + l1) * 0.5;
                double nf = Math.cos(pm) * Math.cos(lm) / ka;
                double nu = Math.sin(pm) / kb;
                double ns = Math.cos(pm) * Math.sin(lm) / kc;
                double wx = F[0] * nf + U[0] * nu + S[0] * ns;
                double wy = F[1] * nf + U[1] * nu + S[1] * ns;
                double wz = F[2] * nf + U[2] * nu + S[2] * ns;
                double wl = Math.sqrt(wx * wx + wy * wy + wz * wz);
                double lit = wl < 1.0E-12 ? 0.0 : (wx * lx + wy * ly + wz * lz) / wl;
                int col = shade(lit);
                for (int k = 0; k < 4; k++) {
                    COL[k] = col;
                }
                sink.quad(QUAD, COL);
            }
        }
    }

    static final double[] QUAD = new double[12];
    static final int[] COL = new int[4];

    // ------------------------------------------------------------------------------------------------------ 3.3.9: Dawn Pistol's POW

    /** How long the POW lasts (ticks), and its look: the Haki's black-violet ink outline, a white burst, a warm heart. */
    public static final double POW_LIFE = 12.0;
    static final int POW_SPIKES = 10;
    public static final int POW_INK = 0x1C1426;
    public static final int POW_WHITE = 0xFFFFFF;
    public static final int POW_CORE = 0xFFE7A0;
    static final double[] POW_B = new double[6];
    static final double[] POW_PTS = new double[POW_SPIKES * 2 * 3];

    /**
     * 3.3.9 (the owner accepted "a cartoon 'POW' burst on hit (a bold star-shaped flash and shock ring)"): where a Dawn Pistol lands, a
     * comic-book starburst facing the eye, held just in front of the body struck (middle at {@code x y z}, eye-centred; {@code size} its
     * height): a jagged star of {@link #POW_SPIKES} long and short spikes in three flat bands (the Haki-black ink outline, white, a warm
     * heart), each a hair nearer the eye than the one under it (one plane would speckle: the Ito strings' lesson); it pops out past its
     * size in two ticks, settles, holds and shrinks away; two white shock rings race out round the punch's line ({@code wx wy wz}) and
     * eight impact lines shoot out from the star's edge. About 270 quads.
     */
    public static void pow(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double age, double size, int seed) {
        if (age < 0 || age > POW_LIFE) {
            return;
        }
        double dist = Math.sqrt(x * x + y * y + z * z);
        if (dist < 0.3) {
            return;
        }
        double vx = x / dist;
        double vy = y / dist;
        double vz = z / dist;
        // the pop: out past its size in two ticks, settling to it by four, held, then shrinking and fading from seven
        double scale;
        if (age < 2.0) {
            scale = 1.3 * Math.sin(Math.PI * 0.5 * age / 2.0);
        } else if (age < 4.0) {
            scale = 1.3 - 0.3 * (age - 2.0) / 2.0;
        } else if (age < 7.0) {
            scale = 1.0 + 0.04 * Math.sin((age - 4.0) * 2.4);
        } else {
            scale = 1.0 - 0.45 * (age - 7.0) / (POW_LIFE - 7.0);
        }
        double alpha = age < 7.0 ? 1.0 : Math.max(0.0, 1.0 - (age - 7.0) / (POW_LIFE - 7.0));
        double radius = (0.5 + 0.3 * Math.max(0.5, Math.min(3.0, size))) * Math.max(0.05, scale);
        // just in front of the body, toward the eye
        double front = Math.min(dist * 0.5, 0.35 + 0.12 * size);
        double cx = x - vx * front;
        double cy = y - vy * front;
        double cz = z - vz * front;
        FireMesh.basis(vx, vy, vz, POW_B);
        double turn = FireMesh.rand(seed, 1) * Math.PI;
        int n = POW_SPIKES * 2;
        for (int k = 0; k < n; k++) {
            double a = turn + k * Math.PI / POW_SPIKES;
            double r = k % 2 == 0 ? 0.92 + 0.2 * FireMesh.rand(seed, k + 2) : 0.46 + 0.1 * FireMesh.rand(seed, k + 40);
            double ca = Math.cos(a) * r;
            double sa = Math.sin(a) * r;
            POW_PTS[k * 3] = POW_B[0] * ca + POW_B[3] * sa;
            POW_PTS[k * 3 + 1] = POW_B[1] * ca + POW_B[4] * sa;
            POW_PTS[k * 3 + 2] = POW_B[2] * ca + POW_B[5] * sa;
        }
        // the three bands, far to near: the ink outline, the white burst, the warm heart
        star(sink, cx, cy, cz, vx, vy, vz, dist, 0, radius * 1.16, FireMesh.argb(0.95 * alpha, POW_INK));
        star(sink, cx, cy, cz, vx, vy, vz, dist, 1, radius, FireMesh.argb(0.95 * alpha, POW_WHITE));
        star(sink, cx, cy, cz, vx, vy, vz, dist, 2, radius * 0.5, FireMesh.argb(0.9 * alpha, POW_CORE));
        // impact lines from the star's edge, between its long spikes
        double lines = radius * (0.6 + 0.5 * Math.min(1.0, age / 3.0));
        for (int k = 0; k < 8; k++) {
            double a = turn + (k + 0.5) * Math.PI * 2.0 / 8.0 + 0.2 * FireMesh.rand(seed, k + 80);
            double ox = POW_B[0] * Math.cos(a) + POW_B[3] * Math.sin(a);
            double oy = POW_B[1] * Math.cos(a) + POW_B[4] * Math.sin(a);
            double oz = POW_B[2] * Math.cos(a) + POW_B[5] * Math.sin(a);
            double from = radius * 1.12;
            FireMesh.ray(sink, cx + ox * from - vx * dist * 0.006, cy + oy * from - vy * dist * 0.006, cz + oz * from - vz * dist * 0.006,
                ox, oy, oz, lines, radius * 0.07, 0, 0.0, POW_WHITE, 0.9 * alpha);
        }
        // the shock rings round the punch's line, racing out (the second two ticks behind)
        double wl = Math.sqrt(wx * wx + wy * wy + wz * wz);
        if (wl > 1.0E-6) {
            for (int k = 0; k < 2; k++) {
                double t = age - k * 2.0;
                if (t < 0 || t > 9.0) {
                    continue;
                }
                double ring = 0.35 + 0.36 * t * (1.0 + 0.25 * size);
                FireMesh.ring(sink, x, y, z, wx / wl, wy / wl, wz / wl, ring, 0.45 - 0.15 * k, 0.3, POW_WHITE, (0.85 - 0.3 * k) * (1.0 - t / 9.0));
            }
        }
    }

    /** One band of the star: {@link #POW_PTS} at {@code radius}, lifted {@code layer} hairs toward the eye, a fan from its middle. */
    static void star(FireMesh.Sink sink, double cx, double cy, double cz, double vx, double vy, double vz, double dist, int layer, double radius,
                     int argb) {
        double lift = dist * 0.003 * layer;
        double mx = cx - vx * lift;
        double my = cy - vy * lift;
        double mz = cz - vz * lift;
        int n = POW_SPIKES * 2;
        for (int k = 0; k < n; k++) {
            int j = (k + 1) % n;
            FireMesh.quad(sink, mx, my, mz, argb, mx + POW_PTS[k * 3] * radius, my + POW_PTS[k * 3 + 1] * radius, mz + POW_PTS[k * 3 + 2] * radius,
                argb, mx + POW_PTS[j * 3] * radius, my + POW_PTS[j * 3 + 1] * radius, mz + POW_PTS[j * 3 + 2] * radius, argb, mx, my, mz, argb);
        }
    }

    static void corner(int k, double p, double l, double power, double ka, double kb, double kc, double cx, double cy, double cz) {
        double cp = Math.cos(p);
        double sf = signedPow(cp * Math.cos(l), power) * ka;
        double su = signedPow(Math.sin(p), power) * kb;
        double ss = signedPow(cp * Math.sin(l), power) * kc;
        QUAD[k * 3] = cx + F[0] * sf + U[0] * su + S[0] * ss;
        QUAD[k * 3 + 1] = cy + F[1] * sf + U[1] * su + S[1] * ss;
        QUAD[k * 3 + 2] = cz + F[2] * sf + U[2] * su + S[2] * ss;
    }

    static double signedPow(double v, double power) {
        return Math.copySign(Math.pow(Math.abs(v), power), v);
    }

    /** A face's colour: {@link #tint} in one of four bands (cel-shaded), by how it faces the light. */
    static int shade(double lit) {
        double band = lit > 0.5 ? 1.0 : lit > 0.1 ? 0.86 : lit > -0.3 ? 0.72 : 0.6;
        int rr = (int) Math.round(((tint >> 16) & 255) * band);
        int gg = (int) Math.round(((tint >> 8) & 255) * band);
        int bb = (int) Math.round((tint & 255) * band);
        return 0xFF000000 | rr << 16 | gg << 8 | bb;
    }
}
