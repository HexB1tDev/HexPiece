package com.hexhaki.piece.hie;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.1.9: the Hie Hie no Mi's drawn ice. The owner, shown the first renders (every piece of ice a pointed hexagonal crystal): "They're just
 * like crystals. I mean, all of them are just crystals. Looks weird ... The ground can also turn into like actual ice blocks"; asked, he
 * chose real ice blocks for frozen bodies and the ground (server/HieFreeze, server/HieMoves) and "Smooth glassy ice" for everything that
 * flies or is held: so every shape here is smooth and rounded (a lathe round its axis, a rounded lump), pale and see-through in its
 * middle, white toward its rim where it turns from the eye (glass's own look), a soft white highlight where it faces the light; no
 * pointed facets, no edge lines. Frost mist is soft white balls. Pure: every point relative to the eye (at the origin, as the game's level
 * pass and tools/preview/FxPreview both have it), every quad into a {@link FireMesh.Sink}; alpha blended, depth tested, never additive;
 * no allocation per frame.
 */
public final class HieArt {
    private HieArt() {
    }

    /** The ice's heart (pale cyan, see-through), its rim (white), the cold glow round it, the cyan of its streaks. */
    public static final int HEART = 0x9ADCF6;
    public static final int RIM = 0xFFFFFF;
    public static final int GLOW = 0xA8E8FF;
    public static final int CYAN = 0x45D2FF;

    /** How fast Ice Age's freeze runs over the ground (blocks a tick): the server freezes the real ground at it, the clients' mist rolls with it. */
    public static final double SHEET_SPEED = 1.5;

    /** The light (unit): from above, a little from the left and the front. */
    static final double LX = -0.38;
    static final double LY = 0.82;
    static final double LZ = 0.43;

    static double clamp(double v) {
        return v < 0.0 ? 0.0 : v > 1.0 ? 1.0 : v;
    }

    static double ease(double t) {
        double u = clamp(t);
        return 1.0 - (1.0 - u) * (1.0 - u);
    }

    /** Overshooting a little, then settling (ice snapping into shape). */
    static double snap(double t) {
        double u = clamp(t) - 1.0;
        return 1.0 + u * u * (2.7 * u + 1.7);
    }

    /**
     * Glass's colour at a point of its surface: {@code nx, ny, nz} its normal (unit), {@code vx, vy, vz} the way to the eye (unit): clear and
     * pale cyan where it faces the eye, white and more solid where it turns away (its rim), a soft white highlight facing the light.
     */
    static int glass(double nx, double ny, double nz, double vx, double vy, double vz, double alpha) {
        double facing = Math.abs(nx * vx + ny * vy + nz * vz);
        double edge = 1.0 - facing;
        double lit = nx * LX + ny * LY + nz * LZ;
        double white = clamp(edge * 1.35 - 0.15);
        double a = 0.34 + 0.56 * Math.pow(edge, 1.3);
        if (lit > 0.8) {
            double h = clamp((lit - 0.8) / 0.12);
            white = Math.max(white, h);
            a = Math.max(a, 0.55 + 0.3 * h);
        }
        return FireMesh.argb(alpha * a, FireMesh.mix(HEART, RIM, white));
    }

    // ------------------------------------------------------------------------------------------------------ the lathe

    static final double[] B = new double[6];
    static final int MOST_SIDES = 12;
    static final double[] PA = new double[MOST_SIDES * 3];
    static final double[] PB = new double[MOST_SIDES * 3];
    static final int[] CA = new int[MOST_SIDES];
    static final int[] CB = new int[MOST_SIDES];

    /**
     * A smooth shape of ice turned round an axis: from ({@code x}, {@code y}, {@code z}) along ({@code ux}, {@code uy}, {@code uz}) (unit),
     * {@code length} long, its radius {@code radius} times {@code profile}[i] at evenly spaced points along it (0 closes it to a point),
     * {@code flat} how round across its second axis (1 round, 0.3 a blade), {@code sides} round (3 to 12). {@code view}: the way to the
     * eye for every point (when the frame is not the eye's, as a held blade's), or null (the eye at the origin).
     */
    public static void lathe(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double length, double radius,
                             double[] profile, double flat, int sides, double alpha, double[] view) {
        int n = Math.max(3, Math.min(MOST_SIDES, sides));
        if (alpha < 0.004 || length < 0.01 || radius < 0.002 || profile.length < 2) {
            return;
        }
        FireMesh.basis(ux, uy, uz, B);
        int rings = profile.length;
        double step = length / (rings - 1);
        double[] prev = PA;
        int[] prevC = CA;
        double[] cur = PB;
        int[] curC = CB;
        for (int j = 0; j < rings; j++) {
            double t = j * step;
            double r = radius * profile[j];
            // the slope of its outline here (for the normal)
            double before = profile[Math.max(0, j - 1)];
            double after = profile[Math.min(rings - 1, j + 1)];
            double slope = radius * (after - before) / (step * (j > 0 && j < rings - 1 ? 2.0 : 1.0));
            double cx = x + ux * t;
            double cy = y + uy * t;
            double cz = z + uz * t;
            for (int i = 0; i < n; i++) {
                double a = i * 6.283185307 / n;
                double ca = Math.cos(a);
                double sa = Math.sin(a);
                double ox = B[0] * ca + B[3] * sa * flat;
                double oy = B[1] * ca + B[4] * sa * flat;
                double oz = B[2] * ca + B[5] * sa * flat;
                double px = cx + ox * r;
                double py = cy + oy * r;
                double pz = cz + oz * r;
                cur[i * 3] = px;
                cur[i * 3 + 1] = py;
                cur[i * 3 + 2] = pz;
                // the normal: out from the axis (across a flat blade, mostly its broad face), tipped back by the outline's slope
                double fl = Math.max(0.2, flat);
                double nxr = B[0] * ca * fl + B[3] * sa;
                double nyr = B[1] * ca * fl + B[4] * sa;
                double nzr = B[2] * ca * fl + B[5] * sa;
                double nl = Math.sqrt(nxr * nxr + nyr * nyr + nzr * nzr);
                double nx = nxr / nl - ux * slope;
                double ny = nyr / nl - uy * slope;
                double nz = nzr / nl - uz * slope;
                nl = Math.sqrt(nx * nx + ny * ny + nz * nz);
                nx /= nl;
                ny /= nl;
                nz /= nl;
                double vx;
                double vy;
                double vz;
                if (view != null) {
                    vx = view[0];
                    vy = view[1];
                    vz = view[2];
                } else {
                    double pl = Math.max(1.0E-6, Math.sqrt(px * px + py * py + pz * pz));
                    vx = -px / pl;
                    vy = -py / pl;
                    vz = -pz / pl;
                }
                curC[i] = glass(nx, ny, nz, vx, vy, vz, alpha);
            }
            if (j > 0) {
                for (int i = 0; i < n; i++) {
                    int k = (i + 1) % n;
                    FireMesh.quad(sink, prev[i * 3], prev[i * 3 + 1], prev[i * 3 + 2], prevC[i], prev[k * 3], prev[k * 3 + 1], prev[k * 3 + 2], prevC[k],
                        cur[k * 3], cur[k * 3 + 1], cur[k * 3 + 2], curC[k], cur[i * 3], cur[i * 3 + 1], cur[i * 3 + 2], curC[i]);
                }
            }
            double[] swap = prev;
            prev = cur;
            cur = swap;
            int[] swapC = prevC;
            prevC = curC;
            curC = swapC;
        }
    }

    // ------------------------------------------------------------------------------------------------------ the rounded lump

    /** A unit icosahedron and its once-divided form (80 faces). */
    static final double[][] BALL_DIRS = new double[2][];
    static final int[][] BALL_FACES = new int[2][];

    static {
        double t = (1.0 + Math.sqrt(5.0)) / 2.0;
        double[][] v = {{-1, t, 0}, {1, t, 0}, {-1, -t, 0}, {1, -t, 0}, {0, -1, t}, {0, 1, t}, {0, -1, -t}, {0, 1, -t}, {t, 0, -1},
            {t, 0, 1}, {-t, 0, -1}, {-t, 0, 1}};
        int[] f = {0, 11, 5, 0, 5, 1, 0, 1, 7, 0, 7, 10, 0, 10, 11, 1, 5, 9, 5, 11, 4, 11, 10, 2, 10, 7, 6, 7, 1, 8, 3, 9, 4, 3, 4, 2, 3, 2,
            6, 3, 6, 8, 3, 8, 9, 4, 9, 5, 2, 4, 11, 6, 2, 10, 8, 6, 7, 9, 8, 1};
        java.util.List<double[]> verts = new java.util.ArrayList<>();
        for (double[] p : v) {
            double l = Math.sqrt(p[0] * p[0] + p[1] * p[1] + p[2] * p[2]);
            verts.add(new double[] {p[0] / l, p[1] / l, p[2] / l});
        }
        int[] faces = f;
        for (int level = 0; level < 2; level++) {
            double[] dirs = new double[verts.size() * 3];
            for (int i = 0; i < verts.size(); i++) {
                System.arraycopy(verts.get(i), 0, dirs, i * 3, 3);
            }
            BALL_DIRS[level] = dirs;
            BALL_FACES[level] = faces;
            if (level == 1) {
                break;
            }
            java.util.Map<Long, Integer> mid = new java.util.HashMap<>();
            int[] next = new int[faces.length * 4];
            int k = 0;
            for (int i = 0; i < faces.length; i += 3) {
                int a = faces[i];
                int b = faces[i + 1];
                int c = faces[i + 2];
                int ab = middle(verts, mid, a, b);
                int bc = middle(verts, mid, b, c);
                int ca = middle(verts, mid, c, a);
                int[] add = {a, ab, ca, b, bc, ab, c, ca, bc, ab, bc, ca};
                System.arraycopy(add, 0, next, k, 12);
                k += 12;
            }
            faces = next;
        }
    }

    static int middle(java.util.List<double[]> verts, java.util.Map<Long, Integer> mid, int a, int b) {
        long key = Math.min(a, b) * 100000L + Math.max(a, b);
        Integer known = mid.get(key);
        if (known != null) {
            return known;
        }
        double[] p = verts.get(a);
        double[] q = verts.get(b);
        double x = p[0] + q[0];
        double y = p[1] + q[1];
        double z = p[2] + q[2];
        double l = Math.sqrt(x * x + y * y + z * z);
        verts.add(new double[] {x / l, y / l, z / l});
        mid.put(key, verts.size() - 1);
        return verts.size() - 1;
    }

    static final double[] LP = new double[42 * 3];
    static final int[] LC = new int[42];

    /**
     * A rounded lump of ice {@code radius} round at ({@code x}, {@code y}, {@code z}): a little uneven (no two alike), {@code stretch} times
     * as long along ({@code ux}, {@code uy}, {@code uz}) (unit; 1 a ball), turned by {@code spin}, in glass's colours.
     */
    public static void lump(FireMesh.Sink sink, double x, double y, double z, double radius, double ux, double uy, double uz, double stretch,
                            double spin, double alpha, int seed) {
        if (alpha < 0.004 || radius < 0.01) {
            return;
        }
        double dist = Math.sqrt(x * x + y * y + z * z);
        int level = radius / Math.max(0.5, dist) > 0.03 ? 1 : 0;
        double[] dirs = BALL_DIRS[level];
        int[] faces = BALL_FACES[level];
        int n = dirs.length / 3;
        double cs = Math.cos(spin);
        double sn = Math.sin(spin);
        double tilt = 0.5 + FireMesh.rand(seed, 3) * 2.0;
        double ct = Math.cos(tilt);
        double st = Math.sin(tilt);
        for (int i = 0; i < n; i++) {
            double dx = dirs[i * 3];
            double dy = dirs[i * 3 + 1];
            double dz = dirs[i * 3 + 2];
            double rx = dx * cs - dz * sn;
            double rz = dx * sn + dz * cs;
            double ry = dy * ct - rz * st;
            rz = dy * st + rz * ct;
            double r = radius * (0.88 + 0.24 * FireMesh.rand(seed, 100 + i % 12));
            double along = rx * ux + ry * uy + rz * uz;
            double ex = (stretch - 1.0) * along;
            double px = x + (rx + ux * ex) * r;
            double py = y + (ry + uy * ex) * r;
            double pz = z + (rz + uz * ex) * r;
            LP[i * 3] = px;
            LP[i * 3 + 1] = py;
            LP[i * 3 + 2] = pz;
            double pl = Math.max(1.0E-6, Math.sqrt(px * px + py * py + pz * pz));
            LC[i] = glass(rx, ry, rz, -px / pl, -py / pl, -pz / pl, alpha);
        }
        for (int f = 0; f < faces.length; f += 3) {
            FireMesh.tri(sink, LP, LC, faces[f], faces[f + 1], faces[f + 2]);
        }
    }

    // ------------------------------------------------------------------------------------------------------ mist

    /** A puff of white frost mist: soft balls of white, clear at their edge (never a hard outline). */
    public static void mist(FireMesh.Sink sink, double x, double y, double z, double r, double alpha, double time, int seed) {
        double drift = time * 0.01;
        FireMesh.shell(sink, x, y, z, r, 0xF4FAFF, alpha * 0.55, 0.0);
        FireMesh.shell(sink, x + r * (0.35 + drift) * (FireMesh.rand(seed, 7) - 0.5), y + r * 0.2, z + r * 0.4 * (FireMesh.rand(seed, 8) - 0.5), r * 0.7,
            0xFFFFFF, alpha * 0.5, 0.0);
    }

    /** The wall of mist rolling out ahead of Ice Age's freeze and the road's growing end: a puff at each of {@code count} points. */
    public static void mistRow(FireMesh.Sink sink, double[] points, int count, double size, double alpha, double time, int seed) {
        for (int i = 0; i < count; i++) {
            mist(sink, points[i * 3], points[i * 3 + 1], points[i * 3 + 2], size * (0.8 + 0.5 * FireMesh.rand(seed, 30 + i)), alpha, time, seed + i);
        }
    }

    // ------------------------------------------------------------------------------------------------------ ice breaking

    /** Chunks of ice thrown out of ({@code x}, {@code y}, {@code z}) {@code size} across, falling and fading: what breaking ice throws. */
    static void chunks(FireMesh.Sink sink, double x, double y, double z, double size, int count, double age, double life, int seed) {
        double fade = 1.0 - clamp((age - life * 0.6) / (life * 0.4));
        for (int k = 0; k < count; k++) {
            double a = 6.283 * FireMesh.rand(seed, 500 + k);
            double up = 0.2 + 0.8 * FireMesh.rand(seed, 510 + k);
            double speed = size * (0.1 + 0.14 * FireMesh.rand(seed, 520 + k));
            double px = x + Math.cos(a) * speed * age;
            double py = y + up * speed * 1.3 * age - 0.03 * age * age;
            double pz = z + Math.sin(a) * speed * age;
            double r = size * (0.08 + 0.1 * FireMesh.rand(seed, 540 + k));
            lump(sink, px, py, pz, r, Math.cos(a), 0.3, Math.sin(a), 1.3, age * 0.3 + k, 0.95 * fade, seed + 60 + k);
        }
    }

    /** How long ice breaking takes (ticks). */
    public static final double SHATTER_LIFE = 30.0;

    /**
     * Ice breaking at ({@code x}, {@code y}, {@code z}) ({@code size} across; refs 09, 69: Garp breaking out of his block; the real blocks
     * break with the game's own particles beside it): a white flash, rounded chunks of ice thrown out and falling, a ring of frost racing
     * out, mist.
     */
    public static void shatter(FireMesh.Sink sink, double x, double y, double z, double size, double age, int seed) {
        if (age < 0.0 || age > SHATTER_LIFE) {
            return;
        }
        double u = age / SHATTER_LIFE;
        if (age < 4.0) {
            double f = 1.0 - age / 4.0;
            FireMesh.shell(sink, x, y, z, size * (0.7 + 0.5 * age / 4.0), RIM, 0.5 * f, 0.0);
            FireMesh.shell(sink, x, y, z, size * (1.1 + 0.8 * age / 4.0), GLOW, 0.22 * f, 0.0);
        }
        chunks(sink, x, y, z, size, size > 1.5 ? 16 : 11, age, SHATTER_LIFE, seed);
        FireMesh.ring(sink, x, y - size * 0.4, z, 0, 1, 0, size * (0.5 + 1.8 * ease(u * 2.0)), 0.25 + size * 0.25, size * 0.12, RIM,
            0.5 * (1.0 - clamp(u * 2.0)));
        for (int k = 0; k < 3; k++) {
            double a = 6.283 * (k + FireMesh.rand(seed, 560 + k)) / 3.0;
            double out = size * (0.3 + 0.9 * ease(u * 1.5));
            mist(sink, x + Math.cos(a) * out, y - size * 0.2 + 0.4 * u, z + Math.sin(a) * out, size * (0.35 + 0.35 * u), 0.4 * (1.0 - u), age,
                seed + 80 + k);
        }
    }

    public static double burstLife(double radius) {
        return 30.0 + radius * 2.0;
    }

    /**
     * Ice bursting at ({@code x}, {@code y}, {@code z}) ({@code radius}, {@code strength} 0 to 1; a spear's landing, the pheasant's end, the
     * ice fist): a flash, a rounded mass of ice swelling there and breaking, its chunks thrown out and falling, a ring of frost along the
     * ground, mist billowing.
     */
    public static void burst(FireMesh.Sink sink, double x, double y, double z, double radius, double strength, double age, int seed) {
        double life = burstLife(radius);
        if (age < 0.0 || age > life) {
            return;
        }
        double rr = Math.max(0.6, radius);
        double u = age / life;
        if (age < 5.0) {
            double f = 1.0 - age / 5.0;
            FireMesh.shell(sink, x, y, z, rr * (0.4 + 0.5 * age / 5.0), RIM, 0.55 * f, 0.0);
            FireMesh.shell(sink, x, y, z, rr * (0.8 + 0.9 * age / 5.0), GLOW, 0.25 * f, 0.0);
        }
        // the mass of ice: swelling in two ticks, held, then breaking into its chunks
        double hold = 6.0 + 6.0 * strength;
        if (age < hold + 1.0) {
            double g = snap(age / 2.0) * (1.0 - clamp(age - hold));
            int pieces = rr > 2.0 ? 5 : 3;
            for (int k = 0; k < pieces; k++) {
                double a = 6.283 * (k + FireMesh.rand(seed, 600 + k) * 0.5) / pieces;
                double off = k == 0 ? 0.0 : rr * 0.35;
                lump(sink, x + Math.cos(a) * off, y + (k == 0 ? 0.0 : rr * 0.1), z + Math.sin(a) * off, rr * (k == 0 ? 0.45 : 0.3) * g, 0, 1, 0, 1.2,
                    k, 0.95, seed + 10 + k);
            }
        } else {
            chunks(sink, x, y, z, rr * 1.6, Math.min(18, 8 + (int) (rr * 2.0)), age - hold, life - hold, seed + 20);
        }
        FireMesh.ring(sink, x, y - rr * 0.15, z, 0, 1, 0, rr * (0.4 + 1.6 * ease(u * 3.0)), 0.3 + rr * 0.2, rr * 0.1, RIM,
            0.55 * (1.0 - clamp(u * 3.0)));
        int puffs = rr > 2.5 ? 5 : 3;
        for (int k = 0; k < puffs; k++) {
            double a = 6.283 * (k + FireMesh.rand(seed, 660 + k)) / puffs;
            double out = rr * (0.3 + 0.8 * ease(u * 1.6));
            mist(sink, x + Math.cos(a) * out, y + 0.3 * rr * u, z + Math.sin(a) * out, rr * (0.3 + 0.4 * u), 0.42 * (1.0 - u), age, seed + 170 + k);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Ice Block: Partisan

    /** A partisan's length (blocks). */
    public static final double SPEAR_LENGTH = 2.4;
    /** A spear's outline from its back to its point: a slim shaft, a broad leaf of a head. */
    static final double[] SPEAR = {0.0, 0.26, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.42, 0.78, 1.0, 0.9, 0.74, 0.56, 0.38, 0.2, 0.0};

    /**
     * A spear of ice (ref 62, "Ice Block: Partisan"): its middle at ({@code x}, {@code y}, {@code z}), pointing along ({@code ux}, {@code uy},
     * {@code uz}) (unit), {@code length} long: a smooth slim shaft and a broad leaf of a head, in glass's colours; {@code grow} 0 to 1 as it
     * takes shape in the air.
     */
    public static void spear(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double length, double grow,
                             double alpha, int seed) {
        if (grow <= 0.01) {
            return;
        }
        double l = length * snap(grow);
        double k = l / SPEAR_LENGTH;
        lathe(sink, x - ux * l * 0.5, y - uy * l * 0.5, z - uz * l * 0.5, ux, uy, uz, l, 0.22 * k, SPEAR, 0.45, 8, alpha, null);
    }

    /**
     * Where Partisan's {@code k}th spear (0 to 7) hangs in the air round him (ref 62: a fan of them over his shoulders and out to both
     * sides): {right, up, back} from his feet in his own turned frame (right = his right, back = behind him).
     */
    public static double[] slot(int k) {
        int side = k % 2 == 0 ? 1 : -1;
        int rank = k / 2;
        return new double[] {side * (0.9 + 0.55 * rank), 2.2 + 0.45 * (rank % 2) + 0.25 * rank, 0.35 + 0.15 * rank};
    }

    /** A frosty trail behind something flying along ({@code vx}, {@code vy}, {@code vz}): mist puffs and a pale streak. */
    public static void trail(FireMesh.Sink sink, double x, double y, double z, double vx, double vy, double vz, double radius, double age, int seed) {
        double vl = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (vl < 1.0E-6) {
            return;
        }
        for (int k = 0; k < 4; k++) {
            double back = radius * (1.4 + 1.3 * k);
            mist(sink, x - vx / vl * back, y - vy / vl * back, z - vz / vl * back, radius * (0.6 + 0.25 * k), 0.38 - 0.08 * k, age, seed + 200 + k);
        }
        FireMesh.streak(sink, x, y, z, vx, vy, vz, radius * 5.0, radius * 0.25, GLOW, 0.55);
    }

    // ------------------------------------------------------------------------------------------------------ Ice Ball

    /**
     * The Ice Ball's orb (ref 76, "Ice... Ball!"): a smooth ball of ice {@code radius} round, a cold glow round it and cyan streaks winding
     * round it.
     */
    public static void orb(FireMesh.Sink sink, double x, double y, double z, double radius, double age, int seed) {
        lump(sink, x, y, z, radius, 0, 1, 0, 1.0, age * 0.15, 0.95, seed);
        FireMesh.shell(sink, x, y, z, radius * 1.9, GLOW, 0.24, 0.0);
        for (int k = 0; k < 3; k++) {
            double ax = Math.cos(k * 2.1 + 0.3);
            double ay = 0.55;
            double az = Math.sin(k * 2.1 + 0.3);
            double al = Math.sqrt(ax * ax + ay * ay + az * az);
            FireMesh.arc(sink, x, y, z, ax / al, ay / al, az / al, radius * (1.35 + 0.15 * k), radius * 0.16, age * 0.45 + k * 2.1, 2.4, 0.0,
                k == 0 ? RIM : CYAN, 0.85);
        }
    }

    /** The orb gathering between the hands (ref 76): cyan streaks drawn in to it, the orb growing out of nothing. */
    public static void gather(FireMesh.Sink sink, double x, double y, double z, double radius, double age, int seed) {
        double g = clamp(age / 10.0);
        orb(sink, x, y, z, radius * (0.2 + 0.8 * g), age, seed);
        for (int k = 0; k < 8; k++) {
            double a = 6.283 * FireMesh.rand(seed, 700 + k);
            double b = (FireMesh.rand(seed, 710 + k) - 0.5) * 2.0;
            double ph = (age * 0.12 + FireMesh.rand(seed, 720 + k)) % 1.0;
            double d = radius * (3.2 * (1.0 - ph) + 0.6);
            double c = Math.sqrt(1 - b * b * 0.5);
            double dx = Math.cos(a) * c;
            double dz = Math.sin(a) * c;
            double dy = b * 0.7;
            FireMesh.streak(sink, x + dx * d, y + dy * d, z + dz * d, -dx, -dy, -dz, radius * 1.2, radius * 0.06, CYAN, 0.8 * ph);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Ice Block: Pheasant Beak

    static final double[] BODY = {0.0, 0.55, 0.82, 0.97, 1.0, 0.95, 0.82, 0.62, 0.4, 0.18, 0.0};
    static final double[] HEAD = {0.0, 0.7, 0.98, 1.0, 0.8, 0.45, 0.0};
    static final double[] BEAK = {1.0, 0.85, 0.62, 0.38, 0.16, 0.0};
    static final double[] FEATHER = {0.45, 0.85, 1.0, 1.0, 0.97, 0.88, 0.7, 0.42, 0.0};
    static final double[] PLUME = {0.6, 1.0, 0.95, 0.8, 0.6, 0.38, 0.15, 0.0};

    /**
     * Ice Block: Pheasant Beak: a great bird of smooth ice flying along ({@code fx}, {@code fy}, {@code fz}) (unit), its middle at ({@code x},
     * {@code y}, {@code z}), {@code scale} blocks to its body's half length: a rounded body and head, a long beak, a crest, wings of
     * long feathers spread wide and beating slowly, three long tail feathers streaming behind; a cold glow round it.
     */
    public static void pheasant(FireMesh.Sink sink, double x, double y, double z, double fx, double fy, double fz, double scale, double age, int seed) {
        double fl = Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (fl < 1.0E-6) {
            return;
        }
        fx /= fl;
        fy /= fl;
        fz /= fl;
        // its own frame: f ahead, s to its right (level), u its up
        double sx = -fz;
        double sz = fx;
        double sl = Math.sqrt(sx * sx + sz * sz);
        if (sl < 1.0E-6) {
            sx = 1.0;
            sz = 0.0;
        } else {
            sx /= sl;
            sz /= sl;
        }
        double sy = 0.0;
        double ux = sy * fz - sz * fy;
        double uy = sz * fx - sx * fz;
        double uz = sx * fy - sy * fx;
        double s = scale * snap(age / 5.0);
        FireMesh.shell(sink, x, y, z, s * 1.7, GLOW, 0.18, 0.0);
        lathe(sink, x - fx * s, y - fy * s, z - fz * s, fx, fy, fz, s * 2.1, s * 0.55, BODY, 0.85, 12, 0.95, null);
        double hx = x + fx * s * 1.0 + ux * s * 0.38;
        double hy = y + fy * s * 1.0 + uy * s * 0.38;
        double hz = z + fz * s * 1.0 + uz * s * 0.38;
        lathe(sink, hx - fx * s * 0.3, hy - fy * s * 0.3, hz - fz * s * 0.3, fx, fy, fz, s * 0.75, s * 0.3, HEAD, 1.0, 10, 0.95, null);
        double bx = fx - ux * 0.1;
        double by = fy - uy * 0.1;
        double bz = fz - uz * 0.1;
        double bl = Math.sqrt(bx * bx + by * by + bz * bz);
        lathe(sink, hx + fx * s * 0.38, hy + fy * s * 0.38, hz + fz * s * 0.38, bx / bl, by / bl, bz / bl, s * 0.85, s * 0.12, BEAK, 0.8, 8, 0.95, null);
        // the crest: two plumes swept back off the head
        for (int k = 0; k < 2; k++) {
            double side = k == 0 ? 0.22 : -0.22;
            double cx = -fx * 0.75 + ux * 0.6 + sx * side;
            double cy = -fy * 0.75 + uy * 0.6 + sy * side;
            double cz = -fz * 0.75 + uz * 0.6 + sz * side;
            double cl = Math.sqrt(cx * cx + cy * cy + cz * cz);
            lathe(sink, hx + ux * s * 0.15, hy + uy * s * 0.15, hz + uz * s * 0.15, cx / cl, cy / cl, cz / cl, s * 0.55, s * 0.07, PLUME, 0.4, 6, 0.9,
                null);
        }
        // the wings: four long feathers a side fanned out and swept back, beating slowly
        double beat = Math.sin(age * 0.45 + FireMesh.rand(seed, 5) * 6.0) * 0.32;
        for (int side = -1; side <= 1; side += 2) {
            double rootX = x + sx * side * s * 0.32 + ux * s * 0.2 + fx * s * 0.2;
            double rootY = y + sy * side * s * 0.32 + uy * s * 0.2 + fy * s * 0.2;
            double rootZ = z + sz * side * s * 0.32 + uz * s * 0.2 + fz * s * 0.2;
            for (int k = 0; k < 5; k++) {
                double back = 0.12 + 0.22 * k;
                double lift = beat + 0.2 - 0.05 * k;
                double dx = sx * side - fx * back + ux * lift;
                double dy = sy * side - fy * back + uy * lift;
                double dz = sz * side - fz * back + uz * lift;
                double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
                lathe(sink, rootX - fx * s * 0.13 * k, rootY - fy * s * 0.13 * k, rootZ - fz * s * 0.13 * k, dx / dl, dy / dl, dz / dl,
                    s * (2.4 - 0.25 * k), s * (0.44 - 0.03 * k), FEATHER, 0.2, 8, 0.95, null);
            }
        }
        // the tail: three long feathers streaming behind, waving
        for (int k = -1; k <= 1; k++) {
            double wave = Math.sin(age * 0.35 + k * 1.3) * 0.12;
            double dx = -fx + sx * (k * 0.16) + ux * (0.06 + wave);
            double dy = -fy + sy * (k * 0.16) + uy * (0.06 + wave);
            double dz = -fz + sz * (k * 0.16) + uz * (0.06 + wave);
            double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
            lathe(sink, x - fx * s * 0.8, y - fy * s * 0.8, z - fz * s * 0.8, dx / dl, dy / dl, dz / dl, s * (k == 0 ? 3.0 : 2.4), s * 0.2, PLUME, 0.25, 8,
                0.88, null);
        }
    }

    // ------------------------------------------------------------------------------------------------------ the ice fist

    /**
     * The fist coated in ice (refs 15, 38: the ice fist driven into Garp's): a rounded mass of ice grown round the fist at ({@code x}, {@code y},
     * {@code z}), swelling a little ahead along ({@code ux}, {@code uy}, {@code uz}) (unit); {@code size} 1 for a fist.
     */
    public static void coat(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double size, double age, int seed) {
        double g = snap(age / 4.0) * size;
        lump(sink, x + ux * 0.05 * g, y + uy * 0.05 * g, z + uz * 0.05 * g, 0.24 * g, ux, uy, uz, 1.35, 0.4, 0.82, seed);
        FireMesh.basis(ux, uy, uz, B);
        for (int k = 0; k < 4; k++) {
            double a = 6.283 * k / 4.0 + 0.4;
            double ox = B[0] * Math.cos(a) + B[3] * Math.sin(a);
            double oy = B[1] * Math.cos(a) + B[4] * Math.sin(a);
            double oz = B[2] * Math.cos(a) + B[5] * Math.sin(a);
            lump(sink, x + (ux * 0.12 + ox * 0.17) * g, y + (uy * 0.12 + oy * 0.17) * g, z + (uz * 0.12 + oz * 0.17) * g, 0.1 * g, ux, uy, uz, 1.2,
                k, 0.88, seed + 3 + k);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Ice Saber

    /** The Ice Saber's blade length (blocks, from the guard). */
    public static final double SABER_LENGTH = 1.7;
    static final double[] BLADE = {1.0, 1.0, 0.97, 0.93, 0.87, 0.78, 0.62, 0.36, 0.0};
    static final double[] GUARD = {0.0, 0.75, 1.0, 1.0, 0.75, 0.0};
    static final double[] GRIP = {0.9, 1.0, 1.0, 1.0, 1.0, 1.15, 0.0};
    static final double[] HELD_VIEW = new double[3];

    /**
     * The Ice Saber (in a hand's grip frame, as LightArt.blade: the blade from the guard at the origin down -y, the grip up +y): a long
     * smooth flat blade of ice, a rounded guard across it at the hand, a grip of ice; its highlight moving with {@code shimmer} (0 to 1).
     */
    public static void saber(FireMesh.Sink sink, double length, double shimmer) {
        HELD_VIEW[0] = Math.sin(shimmer * 6.283) * 0.35;
        HELD_VIEW[1] = 0.0;
        HELD_VIEW[2] = 0.94;
        lathe(sink, 0, -0.02, 0, 0, -1, 0, length, 0.095, BLADE, 0.3, 10, 0.92, HELD_VIEW);
        lathe(sink, -0.2, 0.0, 0, 1, 0, 0, 0.4, 0.06, GUARD, 0.55, 8, 0.95, HELD_VIEW);
        lathe(sink, 0, 0.0, 0, 0, 1, 0, 0.3, 0.038, GRIP, 1.0, 8, 0.95, HELD_VIEW);
    }

    /** How long a cut's crescent lasts (ticks). */
    public static final double SLASH_LIFE = 12.0;

    /** One cut of the Ice Saber: a crescent of frost round ({@code ax}, {@code ay}, {@code az}) (unit: the cut's turning axis), {@code radius}. */
    public static void slash(FireMesh.Sink sink, double x, double y, double z, double ax, double ay, double az, double start, double radius, double age,
                             int seed) {
        if (age < 0.0 || age > SLASH_LIFE) {
            return;
        }
        double u = age / SLASH_LIFE;
        double a = 1.0 - u * u;
        FireMesh.arc(sink, x, y, z, ax, ay, az, radius, radius * 0.2, start, 2.2, 0.0, RIM, 0.9 * a);
        FireMesh.arc(sink, x, y, z, ax, ay, az, radius * 1.06, radius * 0.34, start - 0.1, 2.4, 0.0, CYAN, 0.4 * a);
        FireMesh.basis(ax, ay, az, B);
        for (int k = 0; k < 5; k++) {
            double t = start + 2.2 * (k + 0.5) / 5.0;
            double ox = B[0] * Math.cos(t) + B[3] * Math.sin(t);
            double oy = B[1] * Math.cos(t) + B[4] * Math.sin(t);
            double oz = B[2] * Math.cos(t) + B[5] * Math.sin(t);
            double out = radius * (1.0 + 0.25 * u);
            lump(sink, x + ox * out, y + oy * out - 0.004 * age * age, z + oz * out, radius * 0.06 * (1.0 - u), ox, oy, oz, 1.4, age * 0.4 + k, 0.9 * a,
                seed + k);
        }
    }

    // ------------------------------------------------------------------------------------------------------ the body of ice

    /** How long the ice body's shattering and coming back together takes (ticks). */
    public static final double SCATTER_LIFE = 14.0;

    /**
     * The Logia dodge (ref 64: his body shattering into pieces of ice under a blow and coming back together): pieces of ice thrown out of the
     * body ({@code x}, {@code y}, {@code z} its middle, {@code height} tall) and drawn back in, a puff of mist.
     */
    public static void scatter(FireMesh.Sink sink, double x, double y, double z, double height, double age, int seed) {
        if (age < 0.0 || age > SCATTER_LIFE) {
            return;
        }
        double u = age / SCATTER_LIFE;
        double out = Math.sin(Math.PI * u) * 1.1;
        for (int k = 0; k < 14; k++) {
            double a = 6.283 * FireMesh.rand(seed, 4000 + k);
            double b = (FireMesh.rand(seed, 4010 + k) - 0.5) * 2.0;
            double c = Math.sqrt(Math.max(0.0, 1.0 - b * b * 0.6));
            double dx = Math.cos(a) * c;
            double dy = b * 0.8;
            double dz = Math.sin(a) * c;
            lump(sink, x + dx * (0.3 + out), y + dy * height * 0.45 + dy * out * 0.5, z + dz * (0.3 + out), 0.1, dx, dy, dz, 1.4, age * 0.5 + k,
                0.9 * (1.0 - u * u), seed + k);
        }
        mist(sink, x, y, z, 0.6 + 0.6 * u, 0.35 * (1.0 - u), age, seed + 40);
    }
}
