package com.hexhaki.piece.mochi;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.5.5: the Mochi Mochi no Mi's art in 3D (pure; client/mochi/MochiFx and tools/preview/MochiShots draw the very same quads), eye-centred
 * as every fruit's art, in the house style (POINT ONE: solid, cel-banded, alpha blended and depth tested, never additive). After the
 * owner's references (Katakuri's mochi in the anime): a soft cream-white, puffy and glossy, its shadow lilac-grey; with Armament Haki
 * every fist, block and club glossy black with a cold blue rim (the anime's Haki shine).
 *
 * <p>The pieces: {@link #blob} (a ball, an egg, a rounded box: a super-ellipsoid along any three axes, its surface softly lumpy),
 * {@link #tube} (a banded tube along points, round each point as wide as given), {@link #torus} (a ring: the donuts, the anemone's rings,
 * a hole opening), {@link #fist} (a big mochi fist), {@link #cone} (a spike, a prong). Each solid is banded by how square it faces the eye
 * and how much it faces the light from above (the house's bands), and is opaque: nothing see-through is ever laid over it (it would punch
 * holes: docs/handoff 8zzk).
 *
 * <p>No allocation a frame: every point goes through static scratch arrays (the render thread only).
 */
public final class MochiArt {
    private MochiArt() {
    }

    /** Mochi: its heart (lit, square to the eye) to its rim. */
    public static final int[] MOCHI = {0xFFFFFF, 0xFFFAF0, 0xF4EADF, 0xDDCFCC, 0xB7A5B4};
    /** Haki-coated: black, a little lighter where lit, a cold blue rim and a blue-white shine where the light catches it. */
    public static final int[] HAKI = {0x8FA8FF, 0x3A3B4E, 0x24242F, 0x15151C, 0x4D5FCB};
    /** Mogura, the trident: dark iron, its edge lit. */
    public static final int[] IRON = {0xB8B8C4, 0x6E6E7C, 0x464652, 0x2C2C36, 0x82829A};
    /** The light falls from above, a little from the side. */
    static final double LX = 0.32;
    static final double LY = 0.9;
    static final double LZ = 0.28;

    /** The band a point takes: {@code facing} (0 to 1) how square it faces the eye, {@code lit} (0 to 1) how much it faces the light. */
    static int band(double facing, double lit, int[] bands) {
        double shade = 0.5 * facing + 0.5 * lit;
        if (shade > 0.82 && lit > 0.7) {
            return bands[0];
        }
        if (shade > 0.52) {
            return bands[1];
        }
        if (shade > 0.34) {
            return bands[2];
        }
        if (shade > 0.18) {
            return bands[3];
        }
        return bands[4];
    }

    /** A point's colour on a solid: at (x, y, z) from the eye, its normal (nx, ny, nz) (unit). */
    static int shade(double x, double y, double z, double nx, double ny, double nz, int[] bands, double alpha) {
        double el = Math.max(1.0E-6, Math.sqrt(x * x + y * y + z * z));
        double facing = Math.abs((nx * x + ny * y + nz * z) / el);
        double lit = Math.max(0.0, nx * LX + ny * LY + nz * LZ);
        return FireMesh.argb(alpha, band(facing, lit, bands));
    }

    // ------------------------------------------------------------------------------------------------------ the blob

    static final int RINGS_MOST = 16;
    static final int SIDES_MOST = 16;
    static final double[] GRID = new double[(RINGS_MOST + 1) * (SIDES_MOST + 1) * 3];
    static final int[] GRID_C = new int[(RINGS_MOST + 1) * (SIDES_MOST + 1)];

    /** While set, blobs are drawn coarse (a field of many lumps: the mochi ground). */
    public static boolean low;

    /** x^e keeping the sign. */
    static double spow(double x, double e) {
        return Math.copySign(Math.pow(Math.abs(x), e), x);
    }

    /**
     * A blob at (cx, cy, cz) from the eye: along the axes u (ux, uy, uz), v and w (unit, square to each other) {@code ru}, {@code rv},
     * {@code rw} out; {@code square} its shape (1 a ball or an egg, toward 0.25 a box with soft edges); {@code lump} how lumpy its surface
     * is (0 smooth), turning with {@code time} and {@code seed}; banded in {@code bands}, {@code alpha} of it (1 opaque). Its detail by its
     * size on the screen.
     */
    public static void blob(FireMesh.Sink sink, double cx, double cy, double cz, double ux, double uy, double uz, double vx, double vy, double vz,
                            double ru, double rv, double rw, double square, double lump, double time, int seed, int[] bands, double alpha) {
        double wx = uy * vz - uz * vy;
        double wy = uz * vx - ux * vz;
        double wz = ux * vy - uy * vx;
        double dist = Math.sqrt(cx * cx + cy * cy + cz * cz);
        double size = Math.max(ru, Math.max(rv, rw)) / Math.max(0.5, dist) * FireMesh.quality;
        int rings = size > 0.25 ? 12 : size > 0.08 ? 9 : 6;
        int sides = size > 0.25 ? 16 : size > 0.08 ? 12 : 8;
        if (low) {
            rings = Math.min(rings, 6);
            sides = Math.min(sides, 12);
        }
        double e = square;
        double ne = 2.0 - square;
        for (int j = 0; j <= rings; j++) {
            double phi = -Math.PI / 2 + Math.PI * j / rings;
            double cp = Math.cos(phi);
            double sp = Math.sin(phi);
            for (int i = 0; i <= sides; i++) {
                double th = 2 * Math.PI * i / sides;
                double ct = Math.cos(th);
                double st = Math.sin(th);
                // on the super-ellipsoid: (cos phi cos th, sin phi, cos phi sin th) each raised to e (keeping its sign)
                double a = spow(cp, e) * spow(ct, e);
                double b = spow(sp, e);
                double c = spow(cp, e) * spow(st, e);
                double bump = lump > 0.0 ? 1.0 + lump * FireMesh.noise(a * 1.7 + seed * 0.37, b * 1.7, c * 1.7 + time * 0.05, seed) : 1.0;
                double px = cx + (ux * a * ru + vx * b * rv + wx * c * rw) * bump;
                double py = cy + (uy * a * ru + vy * b * rv + wy * c * rw) * bump;
                double pz = cz + (uz * a * ru + vz * b * rv + wz * c * rw) * bump;
                // its normal: the super-ellipsoid's (the powers 2 - e), scaled back by the radii
                double na = spow(cp, ne) * spow(ct, ne) / ru;
                double nb = spow(sp, ne) / rv;
                double nc = spow(cp, ne) * spow(st, ne) / rw;
                double nx = ux * na + vx * nb + wx * nc;
                double ny = uy * na + vy * nb + wy * nc;
                double nz = uz * na + vz * nb + wz * nc;
                double nl = Math.max(1.0E-9, Math.sqrt(nx * nx + ny * ny + nz * nz));
                int v = j * (sides + 1) + i;
                GRID[v * 3] = px;
                GRID[v * 3 + 1] = py;
                GRID[v * 3 + 2] = pz;
                GRID_C[v] = shade(px, py, pz, nx / nl, ny / nl, nz / nl, bands, alpha);
            }
        }
        grid(sink, rings, sides);
    }

    /** The grid of points in {@link #GRID} as quads, {@code rows} by {@code cols}. */
    static void grid(FireMesh.Sink sink, int rows, int cols) {
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                int a = j * (cols + 1) + i;
                int c = a + cols + 1;
                FireMesh.quad(sink, GRID[a * 3], GRID[a * 3 + 1], GRID[a * 3 + 2], GRID_C[a], GRID[(a + 1) * 3], GRID[(a + 1) * 3 + 1],
                    GRID[(a + 1) * 3 + 2], GRID_C[a + 1], GRID[(c + 1) * 3], GRID[(c + 1) * 3 + 1], GRID[(c + 1) * 3 + 2], GRID_C[c + 1],
                    GRID[c * 3], GRID[c * 3 + 1], GRID[c * 3 + 2], GRID_C[c]);
            }
        }
    }

    /** A ball (or an egg along the way (ux, uy, uz), {@code stretch} times as long). */
    public static void ball(FireMesh.Sink sink, double cx, double cy, double cz, double r, double ux, double uy, double uz, double stretch,
                            double lump, double time, int seed, int[] bands) {
        double[] b = B;
        axes(ux, uy, uz, b);
        blob(sink, cx, cy, cz, b[0], b[1], b[2], b[3], b[4], b[5], r * stretch, r, r, 1.0, lump, time, seed, bands, 1.0);
    }

    static final double[] B = new double[6];

    /** Into out: (ux, uy, uz) made unit, then a unit axis square to it (the second of the blob's axes). */
    static void axes(double ux, double uy, double uz, double[] out) {
        double l = Math.max(1.0E-9, Math.sqrt(ux * ux + uy * uy + uz * uz));
        ux /= l;
        uy /= l;
        uz /= l;
        double[] q = Q;
        FireMesh.basis(ux, uy, uz, q);
        out[0] = ux;
        out[1] = uy;
        out[2] = uz;
        out[3] = q[0];
        out[4] = q[1];
        out[5] = q[2];
    }

    static final double[] Q = new double[6];

    // ------------------------------------------------------------------------------------------------------ the tube

    static final int TUBE_MOST = 33;
    static final double[] TP = new double[TUBE_MOST * 3];
    static final double[] TR = new double[TUBE_MOST];
    static final double[] TF = new double[TUBE_MOST * 6];
    static int tubeCount;

    /** Starts a tube's points. */
    public static void tubeBegin() {
        tubeCount = 0;
    }

    /** One more point of the tube, {@code r} round there. */
    public static void tubePoint(double x, double y, double z, double r) {
        if (tubeCount >= TUBE_MOST) {
            return;
        }
        TP[tubeCount * 3] = x;
        TP[tubeCount * 3 + 1] = y;
        TP[tubeCount * 3 + 2] = z;
        TR[tubeCount] = r;
        tubeCount++;
    }

    /**
     * The tube through the points given: {@code around} sides, banded; its frame carried along it (it never twists as it bends);
     * {@code twist} turns of stripes round it ({@code stripe} their band: a drill's thread), 0 for none.
     */
    public static void tube(FireMesh.Sink sink, int around, int[] bands, double twist, double spin) {
        int n = tubeCount;
        if (n < 2) {
            return;
        }
        // the frame: the first square to the way, then carried point to point (parallel transport)
        double tx = TP[3] - TP[0];
        double ty = TP[4] - TP[1];
        double tz = TP[5] - TP[2];
        double tl = Math.max(1.0E-9, Math.sqrt(tx * tx + ty * ty + tz * tz));
        double[] q = Q;
        FireMesh.basis(tx / tl, ty / tl, tz / tl, q);
        double sx = q[0];
        double sy = q[1];
        double sz = q[2];
        for (int j = 0; j < n; j++) {
            int a = Math.max(0, j - 1);
            int b = Math.min(n - 1, j + 1);
            tx = TP[b * 3] - TP[a * 3];
            ty = TP[b * 3 + 1] - TP[a * 3 + 1];
            tz = TP[b * 3 + 2] - TP[a * 3 + 2];
            tl = Math.max(1.0E-9, Math.sqrt(tx * tx + ty * ty + tz * tz));
            tx /= tl;
            ty /= tl;
            tz /= tl;
            // the side carried along: what of it lies along the way taken away
            double d = sx * tx + sy * ty + sz * tz;
            sx -= d * tx;
            sy -= d * ty;
            sz -= d * tz;
            double sl = Math.sqrt(sx * sx + sy * sy + sz * sz);
            if (sl < 1.0E-6) {
                FireMesh.basis(tx, ty, tz, q);
                sx = q[0];
                sy = q[1];
                sz = q[2];
            } else {
                sx /= sl;
                sy /= sl;
                sz /= sl;
            }
            TF[j * 6] = sx;
            TF[j * 6 + 1] = sy;
            TF[j * 6 + 2] = sz;
            TF[j * 6 + 3] = ty * sz - tz * sy;
            TF[j * 6 + 4] = tz * sx - tx * sz;
            TF[j * 6 + 5] = tx * sy - ty * sx;
        }
        around = Math.min(around, SIDES_MOST);
        // more points than the grid holds: drawn in runs
        for (int start = 0; start < n - 1; start += RINGS_MOST) {
            int end = Math.min(n - 1, start + RINGS_MOST);
            for (int j = start; j <= end; j++) {
                double s = j / (double) (n - 1);
                for (int i = 0; i <= around; i++) {
                    double an = 2 * Math.PI * i / around;
                    double ox = TF[j * 6] * Math.cos(an) + TF[j * 6 + 3] * Math.sin(an);
                    double oy = TF[j * 6 + 1] * Math.cos(an) + TF[j * 6 + 4] * Math.sin(an);
                    double oz = TF[j * 6 + 2] * Math.cos(an) + TF[j * 6 + 5] * Math.sin(an);
                    double x = TP[j * 3] + ox * TR[j];
                    double y = TP[j * 3 + 1] + oy * TR[j];
                    double z = TP[j * 3 + 2] + oz * TR[j];
                    int v = (j - start) * (around + 1) + i;
                    GRID[v * 3] = x;
                    GRID[v * 3 + 1] = y;
                    GRID[v * 3 + 2] = z;
                    int c = shade(x, y, z, ox, oy, oz, bands, 1.0);
                    if (twist > 0.0 && Math.cos(2.0 * (an - s * twist * 2 * Math.PI - spin)) > 0.3) {
                        // a drill's thread: a darker band winding round it
                        c = FireMesh.argb(1.0, FireMesh.mix(c & 0xFFFFFF, bands[4], 0.7));
                    }
                    GRID_C[v] = c;
                }
            }
            grid(sink, end - start, around);
        }
        tubeCount = 0;
    }

    // ------------------------------------------------------------------------------------------------------ the ring

    /**
     * A ring round (ax, ay, az) (unit) at (cx, cy, cz): {@code big} its radius, {@code thick} its tube's; banded. {@code open} (0 to 1)
     * how much of it is there (a ring growing round, or a hole closing back).
     */
    public static void torus(FireMesh.Sink sink, double cx, double cy, double cz, double ax, double ay, double az, double big, double thick,
                             double open, int[] bands) {
        if (open < 0.01 || big < 0.02) {
            return;
        }
        double[] q = Q;
        FireMesh.basis(ax, ay, az, q);
        double dist = Math.sqrt(cx * cx + cy * cy + cz * cz);
        double size = (big + thick) / Math.max(0.5, dist) * FireMesh.quality;
        // round enough to read as a ring at any size (16 pieces round near, 12 far), its tube 8 round
        int rows = size > 0.08 ? 16 : 12;
        int cols = size > 0.25 ? 10 : 8;
        for (int j = 0; j <= rows; j++) {
            double u = 2 * Math.PI * open * j / rows;
            double cu = Math.cos(u);
            double su = Math.sin(u);
            // the ring's middle line here, and the way out from the axis
            double ox = q[0] * cu + q[3] * su;
            double oy = q[1] * cu + q[4] * su;
            double oz = q[2] * cu + q[5] * su;
            for (int i = 0; i <= cols; i++) {
                double v = 2 * Math.PI * i / cols;
                double nx = ox * Math.cos(v) + ax * Math.sin(v);
                double ny = oy * Math.cos(v) + ay * Math.sin(v);
                double nz = oz * Math.cos(v) + az * Math.sin(v);
                double x = cx + ox * big + nx * thick;
                double y = cy + oy * big + ny * thick;
                double z = cz + oz * big + nz * thick;
                int k = j * (cols + 1) + i;
                GRID[k * 3] = x;
                GRID[k * 3 + 1] = y;
                GRID[k * 3 + 2] = z;
                GRID_C[k] = shade(x, y, z, nx, ny, nz, bands, 1.0);
            }
        }
        grid(sink, rows, cols);
    }

    // ------------------------------------------------------------------------------------------------------ the cone

    /** A cone from its base (bx, by, bz), {@code r} round, to its point (px, py, pz): a spike, a prong; banded. */
    public static void cone(FireMesh.Sink sink, double bx, double by, double bz, double px, double py, double pz, double r, int[] bands) {
        tubeBegin();
        double dx = px - bx;
        double dy = py - by;
        double dz = pz - bz;
        for (int k = 0; k <= 3; k++) {
            double t = k / 3.0;
            tubePoint(bx + dx * t, by + dy * t, bz + dz * t, r * (1.0 - t) * (k == 3 ? 0.0 : 1.0));
        }
        tube(sink, 6, bands, 0.0, 0.0);
    }

    // ------------------------------------------------------------------------------------------------------ the fist

    /**
     * A big mochi fist at (cx, cy, cz) punching along (fx, fy, fz) (unit), its knuckles' row along the side (sx, sy, sz) (unit, square
     * to it), {@code size} its half width; {@code coat} Haki-black. A soft rounded box of a hand, four fingers rolled over its front, the
     * thumb across them.
     */
    public static void fist(FireMesh.Sink sink, double cx, double cy, double cz, double fx, double fy, double fz, double sx, double sy,
                            double sz, double size, boolean coat, double time, int seed) {
        int[] bands = coat ? HAKI : MOCHI;
        // up: square to the way and the side (the back of the hand)
        double ux = fy * sz - fz * sy;
        double uy = fz * sx - fx * sz;
        double uz = fx * sy - fy * sx;
        // the hand: a soft box, longer along the way than deep
        blob(sink, cx - fx * size * 0.15, cy - fy * size * 0.15, cz - fz * size * 0.15, fx, fy, fz, ux, uy, uz, size * 0.9, size * 0.72,
            size, 0.55, 0.0, time, seed, bands, 1.0);
        // four fingers rolled over its front, a little apart
        for (int k = 0; k < 4; k++) {
            double off = (k - 1.5) * size * 0.46;
            double fwd = size * (0.62 - Math.abs(k - 1.5) * 0.05);
            blob(sink, cx + fx * fwd + sx * off + ux * size * 0.12, cy + fy * fwd + sy * off + uy * size * 0.12, cz + fz * fwd + sz * off
                + uz * size * 0.12, fx, fy, fz, ux, uy, uz, size * 0.3, size * 0.36, size * 0.22, 0.8, 0.0, time, seed + k, bands, 1.0);
        }
        // the thumb across the fingers' front, low
        double tx = cx + fx * size * 0.62 - ux * size * 0.42 - sx * size * 0.35;
        double ty = cy + fy * size * 0.62 - uy * size * 0.42 - sy * size * 0.35;
        double tz = cz + fz * size * 0.62 - uz * size * 0.42 - sz * size * 0.35;
        blob(sink, tx, ty, tz, sx, sy, sz, fx, fy, fz, size * 0.42, size * 0.22, size * 0.2, 0.8, 0.0, time, seed + 7, bands, 1.0);
    }

    // ------------------------------------------------------------------------------------------------------ the soft glow and splats

    /** A soft glow round a point (a puff of flour-white light, clear at its edge): behind solids it never covers them. */
    public static void glow(FireMesh.Sink sink, double x, double y, double z, double r, int rgb, double alpha) {
        FireMesh.shell(sink, x, y, z, r, rgb, alpha, 0.0);
    }

    /** The cream glow of mochi; Haki's cold blue. */
    public static final int GLOW = 0xFFF2DE;
    public static final int HAKI_GLOW = 0x6E82F0;
}
