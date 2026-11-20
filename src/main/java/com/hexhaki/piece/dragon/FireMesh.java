package com.hexhaki.piece.dragon;

/**
 * 2.7.6 (the owner: "the attacks have to be more aggressive more violent ... sparks, explosions, GO CRAZY all of it must be 3d", and,
 * with two frames of Kaido's Bolo Breath, "the fire has to be beautiful I mean look at the reference"): the Uo Uo no Mi's fire,
 * air and wind as real 3D geometry, worked out here with no game class (the game draws it, tools/preview/FxPreview renders it
 * off-game to judge it against docs/handoff/uo-fire-target-*.jpg, the tests check it).
 *
 * <p>The look is the anime's: flat cel bands of colour, white-hot, pale yellow, yellow, amber, orange, red, deep red, never a
 * smooth gradient; a flame's heart is the colour of its age (white as it leaves the mouth, red when it is spent) and its rim burns a
 * band or two hotter (the yellow outline round the red in the reference); noise breaks every band's edge into licks. Each puff of
 * fire is an icosphere pushed out by noise and drawn out into a tear behind its motion; a breath is a stream of them over a white
 * core, with white shock crescents round it; an explosion a flash, a ball of puffs that burn down to smoke, a shock shell and ring,
 * rock shards and streaks. Everything comes in eye-centred coordinates (the eye at the origin, as RenderLevelStageEvent's pose
 * expects) and goes out as quads with a colour per corner (a triangle repeats its last corner).
 */
public final class FireMesh {
    /** Where the quads go: four corners (x, y, z each) and four ARGB colours; both arrays are reused, copy them. */
    public interface Sink {
        void quad(double[] xyz, int[] argb);
    }

    // ------------------------------------------------------------------------------------------------------ the anime's palette

    /** The cel bands, coolest first: spent ember, deep red, red, orange, amber, yellow, pale yellow, white-hot. */
    public static final int[] BAND = {0x6E1006, 0xA3170A, 0xD8301A, 0xF5651A, 0xFFA215, 0xFFD52B, 0xFFF29A, 0xFFFDF2};
    /** Where each band starts (heat 0 to 1). */
    static final double[] EDGE = {0.10, 0.21, 0.34, 0.47, 0.60, 0.75, 0.88};

    /** 2.7.6: other materials cut the same way: smoke (soot to ash), Wind Force's wind (deep to pale green), air (grey-blue to white). */
    public static final int[] SMOKE = {0x2A2220, 0x3A302C, 0x4B403B, 0x5E534D, 0x726760, 0x887D75, 0x9F948C, 0xB6ABA2};
    public static final int[] WIND = {0x1F6B45, 0x2E8A57, 0x48A86A, 0x6CC584, 0x95DCA2, 0xBDEEC4, 0xDDF8E0, 0xF4FFF4};
    public static final int[] AIR = {0x8FA3B8, 0xA8BACB, 0xBFCEDB, 0xD3DFE8, 0xE2EBF2, 0xEEF4F8, 0xF7FAFC, 0xFFFFFF};
    /** The palette the next puffs are cut from ({@link #BAND} unless a caller sets another for its own puffs, then puts it back). */
    static int[] palette = BAND;

    /** The next puffs in {@code bands} (one of {@link #BAND}, {@link #SMOKE}, {@link #WIND}, {@link #AIR}). */
    public static void palette(int[] bands) {
        palette = bands;
    }

    /** The band of {@code heat} at {@code alpha}. */
    public static int fire(double heat, double alpha) {
        int i = 0;
        while (i < EDGE.length && heat >= EDGE[i]) {
            i++;
        }
        return argb(alpha, BAND[i]);
    }

    public static int argb(double alpha, int rgb) {
        int a = (int) Math.round(Math.max(0.0, Math.min(1.0, alpha)) * 255.0);
        return a << 24 | rgb & 0xFFFFFF;
    }

    /** {@code a} to {@code b} by {@code t} (RGB only). */
    public static int mix(int a, int b, double t) {
        double u = Math.max(0.0, Math.min(1.0, t));
        int r = (int) Math.round(((a >> 16) & 255) * (1 - u) + ((b >> 16) & 255) * u);
        int g = (int) Math.round(((a >> 8) & 255) * (1 - u) + ((b >> 8) & 255) * u);
        int bl = (int) Math.round((a & 255) * (1 - u) + (b & 255) * u);
        return r << 16 | g << 8 | bl;
    }

    // ------------------------------------------------------------------------------------------------------ the spheres

    /** Unit icospheres: 20, 80 and 320 faces (their corners' directions, then their faces' corner indices). */
    static final float[][] DIRS = new float[3][];
    static final int[][] FACES = new int[3][];

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
        for (int level = 0; level < 3; level++) {
            DIRS[level] = new float[verts.size() * 3];
            for (int i = 0; i < verts.size(); i++) {
                for (int k = 0; k < 3; k++) {
                    DIRS[level][i * 3 + k] = (float) verts.get(i)[k];
                }
            }
            FACES[level] = faces;
            if (level == 2) {
                break;
            }
            java.util.Map<Long, Integer> mid = new java.util.HashMap<>();
            int[] next = new int[faces.length * 4];
            int o = 0;
            for (int i = 0; i < faces.length; i += 3) {
                int a = faces[i];
                int b = faces[i + 1];
                int c = faces[i + 2];
                int ab = middle(verts, mid, a, b);
                int bc = middle(verts, mid, b, c);
                int ca = middle(verts, mid, c, a);
                int[] add = {a, ab, ca, b, bc, ab, c, ca, bc, ab, bc, ca};
                System.arraycopy(add, 0, next, o, 12);
                o += 12;
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

    // ------------------------------------------------------------------------------------------------------ noise

    /** A hash of a lattice point to -1..1. */
    static double hash(int x, int y, int z, int seed) {
        int h = x * 374761393 + y * 668265263 + z * 1274126177 + seed * 1442695041;
        h = (h ^ (h >>> 13)) * 1274126177;
        h ^= h >>> 16;
        return (h & 0xFFFFFF) / (double) 0x7FFFFF - 1.0;
    }

    /** Smooth value noise, -1..1. */
    public static double noise(double x, double y, double z, int seed) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        int iz = (int) Math.floor(z);
        double fx = x - ix;
        double fy = y - iy;
        double fz = z - iz;
        double u = fx * fx * (3 - 2 * fx);
        double v = fy * fy * (3 - 2 * fy);
        double w = fz * fz * (3 - 2 * fz);
        double a = lerp(u, hash(ix, iy, iz, seed), hash(ix + 1, iy, iz, seed));
        double b = lerp(u, hash(ix, iy + 1, iz, seed), hash(ix + 1, iy + 1, iz, seed));
        double c = lerp(u, hash(ix, iy, iz + 1, seed), hash(ix + 1, iy, iz + 1, seed));
        double d = lerp(u, hash(ix, iy + 1, iz + 1, seed), hash(ix + 1, iy + 1, iz + 1, seed));
        return lerp(w, lerp(v, a, b), lerp(v, c, d));
    }

    static double lerp(double t, double a, double b) {
        return a + (b - a) * t;
    }

    /** A number 0..1 for {@code i} under {@code seed} (the same every frame). */
    public static double rand(int seed, int i) {
        return (hash(i, i * 7 + 3, seed, seed ^ 0x5bd1e995) + 1.0) * 0.5;
    }

    // ------------------------------------------------------------------------------------------------------ scratch (render thread)

    static final double[] XYZ = new double[12];
    static final int[] ARGB = new int[4];
    static final double[] P = new double[162 * 3];
    static final int[] C = new int[162];

    static void tri(Sink sink, double[] p, int[] c, int a, int b, int d) {
        XYZ[0] = p[a * 3];
        XYZ[1] = p[a * 3 + 1];
        XYZ[2] = p[a * 3 + 2];
        XYZ[3] = p[b * 3];
        XYZ[4] = p[b * 3 + 1];
        XYZ[5] = p[b * 3 + 2];
        XYZ[6] = p[d * 3];
        XYZ[7] = p[d * 3 + 1];
        XYZ[8] = p[d * 3 + 2];
        XYZ[9] = XYZ[6];
        XYZ[10] = XYZ[7];
        XYZ[11] = XYZ[8];
        ARGB[0] = c[a];
        ARGB[1] = c[b];
        ARGB[2] = c[d];
        ARGB[3] = c[d];
        sink.quad(XYZ, ARGB);
    }

    public static void quad(Sink sink, double ax, double ay, double az, int ca, double bx, double by, double bz, int cb, double cx, double cy,
                     double cz, int cc, double dx, double dy, double dz, int cd) {
        XYZ[0] = ax;
        XYZ[1] = ay;
        XYZ[2] = az;
        XYZ[3] = bx;
        XYZ[4] = by;
        XYZ[5] = bz;
        XYZ[6] = cx;
        XYZ[7] = cy;
        XYZ[8] = cz;
        XYZ[9] = dx;
        XYZ[10] = dy;
        XYZ[11] = dz;
        ARGB[0] = ca;
        ARGB[1] = cb;
        ARGB[2] = cc;
        ARGB[3] = cd;
        sink.quad(XYZ, ARGB);
    }

    /** 2.7.6: how much detail the player's particle setting allows (1 all, 0.5 decreased, 0.25 minimal): the client sets it. */
    public static double quality = 1.0;

    /** The detail a sphere of {@code radius} at {@code distance} from the eye needs (0: 20 faces, 1: 80, 2: 320). */
    static int detail(double radius, double distance) {
        double size = radius / Math.max(0.5, distance) * quality;
        return size > 0.06 ? 2 : size > 0.012 ? 1 : 0;
    }

    // ------------------------------------------------------------------------------------------------------ the primitives

    /** Where a flame's rim burns (yellow): every puff's outline is pulled toward it, whatever its heart. */
    public static final double RIM = 0.68;

    /**
     * One puff of fire at ({@code x}, {@code y}, {@code z}): {@code radius} across, moving along ({@code vx}, {@code vy}, {@code vz})
     * and drawn out behind by {@code stretch} (0 round, 1 a tear twice as long), its outline torn into tongues by {@code tongues}; its
     * heart at {@code heat}, its outline {@code rim} of the way to {@link #RIM} (the reference's yellow round red), a thin deep-red
     * line at the very edge when {@code outline}; {@code time} (ticks) rolls its noise, {@code seed} makes it its own. Every colour
     * band is cut out of the triangles along its edge, so the bands are flat and sharp as the anime's, never a smear.
     */
    public static void puff(Sink sink, double x, double y, double z, double radius, double vx, double vy, double vz, double stretch,
                            double tongues, double heat, double rim, boolean outline, double alpha, double time, int seed) {
        if (radius < 0.02 || alpha < 0.004) {
            return;
        }
        double dist = Math.sqrt(x * x + y * y + z * z);
        int level = detail(radius, dist);
        float[] dirs = DIRS[level];
        int[] faces = FACES[level];
        int n = dirs.length / 3;
        double vl = Math.sqrt(vx * vx + vy * vy + vz * vz);
        double ux = vl > 1.0E-6 ? vx / vl : 0.0;
        double uy = vl > 1.0E-6 ? vy / vl : 0.0;
        double uz = vl > 1.0E-6 ? vz / vl : 0.0;
        double ox = rand(seed, 1) * 50.0;
        double oy = rand(seed, 2) * 50.0;
        double oz = rand(seed, 3) * 50.0;
        double roll = time * 0.13;
        for (int i = 0; i < n; i++) {
            double dx = dirs[i * 3];
            double dy = dirs[i * 3 + 1];
            double dz = dirs[i * 3 + 2];
            double big = noise(dx * 1.4 + ox, dy * 1.4 + oy - roll, dz * 1.4 + oz, seed);
            double fine = noise(dx * 2.6 + oz, dy * 2.6 + ox - roll * 1.8, dz * 2.6 + oy, seed + 17);
            double ridge = 1.0 - Math.abs(fine);
            double r = radius * (0.8 + 0.26 * big + tongues * ridge * ridge * ridge);
            double back = -(dx * ux + dy * uy + dz * uz);
            if (back > 0.0 && stretch > 0.0) {
                r *= 1.0 + stretch * back * back * (0.6 + 0.8 * ridge * ridge);
            }
            double px = x + dx * r;
            double py = y + dy * r;
            double pz = z + dz * r;
            P[i * 3] = px;
            P[i * 3 + 1] = py;
            P[i * 3 + 2] = pz;
            double el = Math.max(1.0E-6, Math.sqrt(px * px + py * py + pz * pz));
            double facing = -(dx * px + dy * py + dz * pz) / el;
            double edge = 1.0 - Math.min(1.0, Math.abs(facing));
            double pull = rim * Math.min(1.0, Math.pow(edge, 1.4) * 1.6);
            double h = heat + (RIM - heat) * pull + 0.16 * big;
            if (outline && edge > 0.94 && heat < 0.86) {
                h = 0.15; // the dark red line round a spent flame
            }
            H[i] = h;
        }
        for (int f = 0; f < faces.length; f += 3) {
            banded(sink, faces[f], faces[f + 1], faces[f + 2], alpha);
        }
    }

    static final double[] H = new double[162];
    static final double[] POLY = new double[8 * 4];
    static final double[] CLIP = new double[8 * 4];

    static int band(double heat) {
        int i = 0;
        while (i < EDGE.length && heat >= EDGE[i]) {
            i++;
        }
        return i;
    }

    /** One face of a puff, cut along the band edges its heat crosses (heat is linear over a face), each piece in its band's colour. */
    static void banded(Sink sink, int a, int b, int c, double alpha) {
        int ba = band(H[a]);
        int bb = band(H[b]);
        int bc = band(H[c]);
        if (ba == bb && bb == bc) {
            int col = argb(alpha, palette[ba]);
            C[a] = col;
            C[b] = col;
            C[c] = col;
            tri(sink, P, C, a, b, c);
            return;
        }
        int lo = Math.min(ba, Math.min(bb, bc));
        int hi = Math.max(ba, Math.max(bb, bc));
        for (int k = lo; k <= hi; k++) {
            int[] idx = {a, b, c};
            for (int v = 0; v < 3; v++) {
                POLY[v * 4] = P[idx[v] * 3];
                POLY[v * 4 + 1] = P[idx[v] * 3 + 1];
                POLY[v * 4 + 2] = P[idx[v] * 3 + 2];
                POLY[v * 4 + 3] = H[idx[v]];
            }
            int m = 3;
            if (k > 0) {
                m = clip(m, EDGE[k - 1], true);
            }
            if (k < EDGE.length && m >= 3) {
                m = clip(m, EDGE[k], false);
            }
            if (m < 3) {
                continue;
            }
            int col = argb(alpha, palette[k]);
            for (int i = 1; i + 1 < m; i += 2) {
                int j = Math.min(i + 2, m - 1);
                quad(sink, POLY[0], POLY[1], POLY[2], col, POLY[i * 4], POLY[i * 4 + 1], POLY[i * 4 + 2], col, POLY[(i + 1) * 4],
                    POLY[(i + 1) * 4 + 1], POLY[(i + 1) * 4 + 2], col, POLY[j * 4], POLY[j * 4 + 1], POLY[j * 4 + 2], col);
            }
        }
    }

    /** {@link #POLY}'s first {@code m} corners kept where heat is over (or under) {@code edge}; returns how many are left. */
    static int clip(int m, double edge, boolean over) {
        int out = 0;
        for (int i = 0; i < m; i++) {
            int j = (i + 1) % m;
            double hi = POLY[i * 4 + 3];
            double hj = POLY[j * 4 + 3];
            boolean in = over ? hi >= edge : hi < edge;
            boolean jin = over ? hj >= edge : hj < edge;
            if (in) {
                System.arraycopy(POLY, i * 4, CLIP, out * 4, 4);
                out++;
            }
            if (in != jin) {
                double t = (edge - hi) / (hj - hi);
                for (int k = 0; k < 4; k++) {
                    CLIP[out * 4 + k] = POLY[i * 4 + k] + (POLY[j * 4 + k] - POLY[i * 4 + k]) * t;
                }
                out++;
            }
        }
        System.arraycopy(CLIP, 0, POLY, 0, out * 4);
        return out;
    }

    /** A soft-edged ball in one colour: {@code centre} alpha in its middle, {@code rim} alpha at its outline (a shock shell: rim > centre). */
    public static void shell(Sink sink, double x, double y, double z, double radius, int rgb, double centre, double rim) {
        if (radius < 0.02 || Math.max(centre, rim) < 0.004) {
            return;
        }
        double dist = Math.sqrt(x * x + y * y + z * z);
        // a glow (clear at its rim) never shows its outline: 80 faces do; a shock shell (bright rim) needs the round outline
        int level = rim > centre ? detail(radius, dist) : Math.min(1, detail(radius, dist));
        float[] dirs = DIRS[level];
        int[] faces = FACES[level];
        int n = dirs.length / 3;
        for (int i = 0; i < n; i++) {
            double dx = dirs[i * 3];
            double dy = dirs[i * 3 + 1];
            double dz = dirs[i * 3 + 2];
            double px = x + dx * radius;
            double py = y + dy * radius;
            double pz = z + dz * radius;
            P[i * 3] = px;
            P[i * 3 + 1] = py;
            P[i * 3 + 2] = pz;
            double el = Math.max(1.0E-6, Math.sqrt(px * px + py * py + pz * pz));
            double facing = Math.min(1.0, Math.abs((dx * px + dy * py + dz * pz) / el));
            double edge = 1.0 - facing;
            // a shock shell's rim lights only at the outline; a glow thins all the way out from its middle (never a disc)
            C[i] = argb(rim > centre ? centre + (rim - centre) * edge * edge * edge : rim + (centre - rim) * facing * facing * facing, rgb);
        }
        for (int f = 0; f < faces.length; f += 3) {
            tri(sink, P, C, faces[f], faces[f + 1], faces[f + 2]);
        }
    }

    /** Two unit vectors square to {@code (ux, uy, uz)} (unit) into {@code out} (6). */
    public static void basis(double ux, double uy, double uz, double[] out) {
        double ax = Math.abs(uy) < 0.9 ? 0.0 : 1.0;
        double ay = Math.abs(uy) < 0.9 ? 1.0 : 0.0;
        double sx = uy * 0.0 - uz * ay;
        double sy = uz * ax - ux * 0.0;
        double sz = ux * ay - uy * ax;
        double sl = Math.sqrt(sx * sx + sy * sy + sz * sz);
        sx /= sl;
        sy /= sl;
        sz /= sl;
        out[0] = sx;
        out[1] = sy;
        out[2] = sz;
        out[3] = uy * sz - uz * sy;
        out[4] = uz * sx - ux * sz;
        out[5] = ux * sy - uy * sx;
    }

    static final double[] B = new double[6];
    static final double[] RING_A = new double[3 * 49];
    static final double[] RING_B = new double[3 * 49];

    /**
     * A crescent ribbon round ({@code ux}, {@code uy}, {@code uz}) (unit): {@code radius} out, {@code width} wide at its middle and
     * nothing at its ends, from {@code start} through {@code sweep} radians, leaning {@code lean} along the axis at its ends (a shock
     * arc: the white slashes round the breath in the reference).
     */
    public static void arc(Sink sink, double x, double y, double z, double ux, double uy, double uz, double radius, double width, double start,
                           double sweep, double lean, int rgb, double alpha) {
        if (alpha < 0.004 || radius < 0.02) {
            return;
        }
        basis(ux, uy, uz, B);
        int steps = Math.max(6, Math.min(48, (int) (Math.abs(sweep) * radius * 2.0 / Math.max(0.5, Math.sqrt(x * x + y * y + z * z) * 0.06))));
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double a = start + sweep * t;
            double cos = Math.cos(a);
            double sin = Math.sin(a);
            double ox = B[0] * cos + B[3] * sin;
            double oy = B[1] * cos + B[4] * sin;
            double oz = B[2] * cos + B[5] * sin;
            double w = width * Math.sin(Math.PI * t);
            double along = lean * (2.0 * t - 1.0) * (2.0 * t - 1.0);
            double inner = radius - w * 0.5;
            double outer = radius + w * 0.5;
            RING_A[i * 3] = x + ox * inner + ux * along;
            RING_A[i * 3 + 1] = y + oy * inner + uy * along;
            RING_A[i * 3 + 2] = z + oz * inner + uz * along;
            RING_B[i * 3] = x + ox * outer + ux * along;
            RING_B[i * 3 + 1] = y + oy * outer + uy * along;
            RING_B[i * 3 + 2] = z + oz * outer + uz * along;
        }
        int c = argb(alpha, rgb);
        int fade = argb(alpha * 0.25, rgb);
        for (int i = 0; i < steps; i++) {
            int j = i + 1;
            quad(sink, RING_A[i * 3], RING_A[i * 3 + 1], RING_A[i * 3 + 2], fade, RING_B[i * 3], RING_B[i * 3 + 1], RING_B[i * 3 + 2], c,
                RING_B[j * 3], RING_B[j * 3 + 1], RING_B[j * 3 + 2], c, RING_A[j * 3], RING_A[j * 3 + 1], RING_A[j * 3 + 2], fade);
        }
    }

    /**
     * A ring of shock round ({@code ux}, {@code uy}, {@code uz}) (unit): a band {@code width} wide at {@code radius}, its middle at
     * {@code alpha} and both edges clear, its outer edge {@code rise} further along the axis (a low cone, so it is never a flat line).
     */
    public static void ring(Sink sink, double x, double y, double z, double ux, double uy, double uz, double radius, double width, double rise,
                            int rgb, double alpha) {
        if (alpha < 0.004 || radius < 0.02) {
            return;
        }
        basis(ux, uy, uz, B);
        int steps = Math.max(10, Math.min(48, (int) (radius * 6.0 / Math.max(0.5, Math.sqrt(x * x + y * y + z * z) * 0.05))));
        int mid = argb(alpha, rgb);
        int clear = argb(0.0, rgb);
        double r0 = Math.max(0.0, radius - width * 0.5);
        double r2 = radius + width * 0.5;
        for (int i = 0; i < steps; i++) {
            double a0 = i * Math.PI * 2.0 / steps;
            double a1 = (i + 1) * Math.PI * 2.0 / steps;
            double c0 = Math.cos(a0);
            double s0 = Math.sin(a0);
            double c1 = Math.cos(a1);
            double s1 = Math.sin(a1);
            double ax = B[0] * c0 + B[3] * s0;
            double ay = B[1] * c0 + B[4] * s0;
            double az = B[2] * c0 + B[5] * s0;
            double bx = B[0] * c1 + B[3] * s1;
            double by = B[1] * c1 + B[4] * s1;
            double bz = B[2] * c1 + B[5] * s1;
            double h1 = rise * 0.5;
            quad(sink, x + ax * r0, y + ay * r0, z + az * r0, clear, x + ax * radius + ux * h1, y + ay * radius + uy * h1,
                z + az * radius + uz * h1, mid, x + bx * radius + ux * h1, y + by * radius + uy * h1, z + bz * radius + uz * h1, mid,
                x + bx * r0, y + by * r0, z + bz * r0, clear);
            quad(sink, x + ax * radius + ux * h1, y + ay * radius + uy * h1, z + az * radius + uz * h1, mid, x + ax * r2 + ux * rise,
                y + ay * r2 + uy * rise, z + az * r2 + uz * rise, clear, x + bx * r2 + ux * rise, y + by * r2 + uy * rise,
                z + bz * r2 + uz * rise, clear, x + bx * radius + ux * h1, y + by * radius + uy * h1, z + bz * radius + uz * h1, mid);
        }
    }

    /**
     * A beam of light from ({@code x}, {@code y}, {@code z}) along ({@code ux}, {@code uy}, {@code uz}) (unit), {@code length} long
     * and {@code width} wide: two crossed blades, cut into {@code dashes} dashes (0: whole), bright at its root, clear at its tip.
     */
    public static void ray(Sink sink, double x, double y, double z, double ux, double uy, double uz, double length, double width, int dashes,
                           double phase, int rgb, double alpha) {
        if (alpha < 0.004 || length < 0.05) {
            return;
        }
        basis(ux, uy, uz, B);
        int pieces = Math.max(1, dashes);
        for (int d = 0; d < pieces; d++) {
            double t0 = (d + (dashes > 0 ? (phase % 1.0 + 1.0) % 1.0 * 0.5 : 0.0)) / pieces;
            double t1 = Math.min(1.0, t0 + (dashes > 0 ? 0.55 / pieces : 1.0 / pieces));
            if (t0 >= 1.0) {
                continue;
            }
            double a0 = alpha * (1.0 - t0);
            double a1 = alpha * (1.0 - t1);
            double w0 = width * (1.0 - 0.6 * t0);
            double w1 = width * (1.0 - 0.6 * t1);
            double px = x + ux * length * t0;
            double py = y + uy * length * t0;
            double pz = z + uz * length * t0;
            double qx = x + ux * length * t1;
            double qy = y + uy * length * t1;
            double qz = z + uz * length * t1;
            for (int side = 0; side < 2; side++) {
                double sx = B[side * 3];
                double sy = B[side * 3 + 1];
                double sz = B[side * 3 + 2];
                int c0 = argb(a0, rgb);
                int c1 = argb(a1, rgb);
                quad(sink, px - sx * w0, py - sy * w0, pz - sz * w0, argb(0.0, rgb), px, py, pz, c0, qx, qy, qz, c1, qx - sx * w1,
                    qy - sy * w1, qz - sz * w1, argb(0.0, rgb));
                quad(sink, px, py, pz, c0, px + sx * w0, py + sy * w0, pz + sz * w0, argb(0.0, rgb), qx + sx * w1, qy + sy * w1,
                    qz + sz * w1, argb(0.0, rgb), qx, qy, qz, c1);
            }
        }
    }

    /**
     * A 3D spark: a long thin double pyramid at ({@code x}, {@code y}, {@code z}) pointing along its motion ({@code vx}, {@code vy},
     * {@code vz}), {@code length} long (its head white, its tail {@code rgb} and clear).
     */
    public static void streak(Sink sink, double x, double y, double z, double vx, double vy, double vz, double length, double width, int rgb,
                              double alpha) {
        double vl = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (vl < 1.0E-6 || alpha < 0.004) {
            return;
        }
        double ux = vx / vl;
        double uy = vy / vl;
        double uz = vz / vl;
        basis(ux, uy, uz, B);
        double hx = x + ux * length * 0.25;
        double hy = y + uy * length * 0.25;
        double hz = z + uz * length * 0.25;
        double tx = x - ux * length * 0.75;
        double ty = y - uy * length * 0.75;
        double tz = z - uz * length * 0.75;
        int head = argb(alpha, 0xFFFDF2);
        int tail = argb(0.0, rgb);
        int mid = argb(alpha, rgb);
        double[][] ring = new double[4][];
        for (int k = 0; k < 4; k++) {
            double a = k * Math.PI / 2.0;
            double ox = (B[0] * Math.cos(a) + B[3] * Math.sin(a)) * width;
            double oy = (B[1] * Math.cos(a) + B[4] * Math.sin(a)) * width;
            double oz = (B[2] * Math.cos(a) + B[5] * Math.sin(a)) * width;
            ring[k] = new double[] {x + ox, y + oy, z + oz};
        }
        for (int k = 0; k < 4; k++) {
            double[] a = ring[k];
            double[] b = ring[(k + 1) % 4];
            quad(sink, hx, hy, hz, head, a[0], a[1], a[2], mid, b[0], b[1], b[2], mid, b[0], b[1], b[2], mid);
            quad(sink, tx, ty, tz, tail, b[0], b[1], b[2], mid, a[0], a[1], a[2], mid, a[0], a[1], a[2], mid);
        }
    }

    /** A rock torn loose: a rough box {@code size} across at ({@code x}, {@code y}, {@code z}), turned by {@code spin}, lit from above. */
    public static void shard(Sink sink, double x, double y, double z, double size, double spin, int seed, int rgb, double alpha) {
        if (alpha < 0.004 || size < 0.02) {
            return;
        }
        double[] c = new double[24];
        double a = spin + rand(seed, 5) * 6.28;
        double b = spin * 0.7 + rand(seed, 6) * 6.28;
        double ca = Math.cos(a);
        double sa = Math.sin(a);
        double cb = Math.cos(b);
        double sb = Math.sin(b);
        for (int k = 0; k < 8; k++) {
            double px = ((k & 1) == 0 ? -0.5 : 0.5) * size * (0.7 + 0.6 * rand(seed, 10 + k));
            double py = ((k & 2) == 0 ? -0.5 : 0.5) * size * (0.5 + 0.5 * rand(seed, 20 + k));
            double pz = ((k & 4) == 0 ? -0.5 : 0.5) * size * (0.7 + 0.6 * rand(seed, 30 + k));
            double qx = px * ca - pz * sa;
            double qz = px * sa + pz * ca;
            double qy = py * cb - qz * sb;
            qz = py * sb + qz * cb;
            c[k * 3] = x + qx;
            c[k * 3 + 1] = y + qy;
            c[k * 3 + 2] = z + qz;
        }
        int[][] sides = {{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};
        for (int[] s : sides) {
            double ex = c[s[1] * 3] - c[s[0] * 3];
            double ey = c[s[1] * 3 + 1] - c[s[0] * 3 + 1];
            double ez = c[s[1] * 3 + 2] - c[s[0] * 3 + 2];
            double fx = c[s[3] * 3] - c[s[0] * 3];
            double fy = c[s[3] * 3 + 1] - c[s[0] * 3 + 1];
            double fz = c[s[3] * 3 + 2] - c[s[0] * 3 + 2];
            double ny = ez * fx - ex * fz;
            double nl = Math.sqrt(Math.pow(ey * fz - ez * fy, 2) + ny * ny + Math.pow(ex * fy - ey * fx, 2));
            double lit = 0.55 + 0.45 * Math.abs(ny / Math.max(1.0E-9, nl));
            int shade = mix(0, rgb, lit);
            int col = argb(alpha, shade);
            quad(sink, c[s[0] * 3], c[s[0] * 3 + 1], c[s[0] * 3 + 2], col, c[s[1] * 3], c[s[1] * 3 + 1], c[s[1] * 3 + 2], col, c[s[2] * 3],
                c[s[2] * 3 + 1], c[s[2] * 3 + 2], col, c[s[3] * 3], c[s[3] * 3 + 1], c[s[3] * 3 + 2], col);
        }
    }

    /**
     * A tube from A to B, {@code ra} round at A and {@code rb} at B, its colour {@code ca} to {@code cb} along it, its rim {@code rim}
     * of the way to {@code edge} (a white-hot core with a yellow skin), wobbling by {@code wobble} of its radius as {@code time} runs.
     */
    public static void tube(Sink sink, double ax, double ay, double az, double bx, double by, double bz, double ra, double rb, int ca, int cb,
                            int edge, double rim, double alpha, double wobble, double time, int seed) {
        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-4 || alpha < 0.004) {
            return;
        }
        double ux = dx / len;
        double uy = dy / len;
        double uz = dz / len;
        basis(ux, uy, uz, B);
        int around = 12;
        int along = Math.max(2, Math.min(24, (int) (len / 1.2)));
        double[] prev = new double[around * 3];
        int[] prevC = new int[around];
        double[] cur = new double[around * 3];
        int[] curC = new int[around];
        for (int j = 0; j <= along; j++) {
            double t = j / (double) along;
            double r = ra + (rb - ra) * t;
            double cx = ax + dx * t;
            double cy = ay + dy * t;
            double cz = az + dz * t;
            int base = mix(ca, cb, t);
            for (int i = 0; i < around; i++) {
                double a = i * Math.PI * 2.0 / around;
                double ox = B[0] * Math.cos(a) + B[3] * Math.sin(a);
                double oy = B[1] * Math.cos(a) + B[4] * Math.sin(a);
                double oz = B[2] * Math.cos(a) + B[5] * Math.sin(a);
                double rr = r * (1.0 + wobble * noise(Math.cos(a) * 1.3 + 7.0, Math.sin(a) * 1.3, t * len * 0.35 - time * 0.45, seed));
                double px = cx + ox * rr;
                double py = cy + oy * rr;
                double pz = cz + oz * rr;
                cur[i * 3] = px;
                cur[i * 3 + 1] = py;
                cur[i * 3 + 2] = pz;
                double el = Math.max(1.0E-6, Math.sqrt(px * px + py * py + pz * pz));
                double facing = Math.min(1.0, Math.abs((ox * px + oy * py + oz * pz) / el));
                double e = 1.0 - facing;
                curC[i] = argb(alpha, e * e > 1.0 - rim ? edge : base);
            }
            if (j > 0) {
                for (int i = 0; i < around; i++) {
                    int k = (i + 1) % around;
                    quad(sink, prev[i * 3], prev[i * 3 + 1], prev[i * 3 + 2], prevC[i], prev[k * 3], prev[k * 3 + 1], prev[k * 3 + 2], prevC[k],
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

    private FireMesh() {
    }
}
