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
    /** 3.6.3: Buzz Cut Mochi's mace (the owner: "glossy, jet-black Armament Haki, with deep crimson highlights"): Haki lit in crimson. */
    public static final int[] HAKI_CRIMSON = {0xFF6070, 0x3A1A22, 0x24141A, 0x150B10, 0x8E1020};
    public static final int[] HAKI = {0x8FA8FF, 0x3A3B4E, 0x24242F, 0x15151C, 0x4D5FCB};
    /** 3.6.3: Buzz Cut Mochi's crater (the reference's frames 185 to 200: a splatter of pure black): black, a dull grey sheen. */
    public static final int[] INK = {0x2E2E34, 0x101013, 0x0A0A0C, 0x050506, 0x1C1C21};
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
        if (bands == MOCHI || bands == HAKI || bands == HAKI_CRIMSON || bands == IRON) {
            // the side of the surface turned to the eye (a solid's inside seen through its open end lit as its outside)
            double s = (nx * x + ny * y + nz * z) > 0.0 ? -1.0 : 1.0;
            return FireMesh.argb(alpha, lit(nx * s, ny * s, nz * s, -x / el, -y / el, -z / el, bands));
        }
        double lit = Math.max(0.0, nx * LX + ny * LY + nz * LZ);
        return FireMesh.argb(alpha, band(facing, lit, bands));
    }

    /** The light's way, made unit. */
    static final double LL = Math.sqrt(LX * LX + LY * LY + LZ * LZ);

    /**
     * 3.6.3 (the owner: "The Mochi material has to look more realistic, have more depth, shade and color to it, more 3d material too, its
     * too flat"): mochi, Haki and iron lit smoothly instead of in five flat bands, at normal (nx, ny, nz) seen along (vx, vy, vz) (unit,
     * toward the eye). Mochi: a soft light that wraps round it (it is a little see-through), cream where lit, a cool lilac in its shade
     * and deeper still where it turns away (its thickness), a warm bounce from the ground under it, a warm glow through its lit edges and
     * a soft satin shine. Haki: glossy black, a cold blue rim where it turns away and a sharp blue-white shine. Iron: dark grey metal.
     */
    static int lit(double nx, double ny, double nz, double vx, double vy, double vz, int[] bands) {
        double lx = LX / LL;
        double ly = LY / LL;
        double lz = LZ / LL;
        double lam = nx * lx + ny * ly + nz * lz;
        double facing = Math.max(0.0, Math.min(1.0, nx * vx + ny * vy + nz * vz));
        double edge = 1.0 - facing;
        double hx = lx + vx;
        double hy = ly + vy;
        double hz = lz + vz;
        double hl = Math.max(1.0E-9, Math.sqrt(hx * hx + hy * hy + hz * hz));
        double half = Math.max(0.0, (nx * hx + ny * hy + nz * hz) / hl);
        if (bands == MOCHI) {
            // most of it white (the reference's dough glows white over all but its turned-away side)
            double wrap = Math.max(0.0, Math.min(1.0, (lam + 0.55) / 1.0));
            wrap = wrap * wrap * (3.0 - 2.0 * wrap);
            int c = FireMesh.mix(MOCHI_SHADE, MOCHI_BODY, wrap);
            // its thickness: deeper where it turns away from the eye, most on the shaded side
            c = FireMesh.mix(c, MOCHI_DEEP, edge * edge * (0.6 - 0.35 * wrap));
            // the ground's warm bounce under it
            c = FireMesh.mix(c, MOCHI_BOUNCE, Math.max(0.0, -ny) * 0.3 * (1.0 - wrap));
            // light through its lit edges (soft, a little see-through)
            c = FireMesh.mix(c, MOCHI_GLOW, Math.pow(edge, 2.2) * Math.max(0.0, lam + 0.2) * 0.55);
            // a soft satin shine
            double shine = Math.pow(half, 26.0) * 0.55 + Math.pow(half, 6.0) * 0.08;
            return FireMesh.mix(c, 0xFFFFFF, Math.min(1.0, shine));
        }
        if (bands == HAKI || bands == HAKI_CRIMSON) {
            boolean red = bands == HAKI_CRIMSON;
            double diff = Math.max(0.0, lam) * 0.8 + 0.2 * (0.5 + 0.5 * ny);
            int c = FireMesh.mix(HAKI_BODY, HAKI_LIT, diff);
            c = FireMesh.mix(c, red ? 0x8E1020 : HAKI_RIM, Math.pow(edge, 4.0) * (red ? 0.85 : 0.75));
            double shine = Math.pow(half, 70.0) * 0.95 + Math.pow(half, 10.0) * 0.18;
            return FireMesh.mix(c, red ? 0xFF6070 : HAKI_SHINE, Math.min(1.0, shine));
        }
        double diff = Math.max(0.0, lam) * 0.75 + 0.25 * (0.5 + 0.5 * ny);
        int c = FireMesh.mix(0x2A2A33, 0x8A8A98, diff);
        c = FireMesh.mix(c, 0x9EA0B8, Math.pow(edge, 3.0) * 0.6);
        return FireMesh.mix(c, 0xE4E6F2, Math.min(1.0, Math.pow(half, 40.0) * 0.8));
    }

    /** 3.6.3: mochi lit, in its shade, turning away, the ground's bounce, the light through its edges. */
    // 3.6.3 (the owner: the moves to look "exactly like the reference GIFs ... to very minute detail"): the reference's dough is near
    // pure white (253 of 255 where lit), its shade only a soft cool grey (about 215, 213, 222), its deepest about 204, 195, 200
    static final int MOCHI_BODY = 0xFFFFFF;
    static final int MOCHI_SHADE = 0xDAD6E0;
    static final int MOCHI_DEEP = 0xC6BEC9;
    static final int MOCHI_BOUNCE = 0xEEE8E4;
    static final int MOCHI_GLOW = 0xFFFFFF;
    /** 3.6.3: Haki-black in the dark and lit, its cold rim, its shine (blue: never white). */
    static final int HAKI_BODY = 0x0E0E14;
    static final int HAKI_LIT = 0x2C2D3A;
    static final int HAKI_RIM = 0x3B4FBF;
    static final int HAKI_SHINE = 0x8EA6FF;

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
        // the frame: the first square to the way, then carried point to point (parallel transport); 3.6.3: where points coincide (a
        // tube with no length yet, on its first frame) the last good way is kept (up if there is none), never a 0/0
        double wx = 0.0;
        double wy = 1.0;
        double wz = 0.0;
        for (int j = 1; j < n; j++) {
            double ox = TP[j * 3] - TP[0];
            double oy = TP[j * 3 + 1] - TP[1];
            double oz = TP[j * 3 + 2] - TP[2];
            double ol = Math.sqrt(ox * ox + oy * oy + oz * oz);
            if (ol > 1.0E-6) {
                wx = ox / ol;
                wy = oy / ol;
                wz = oz / ol;
                break;
            }
        }
        double tx = wx;
        double ty = wy;
        double tz = wz;
        double tl;
        double[] q = Q;
        FireMesh.basis(tx, ty, tz, q);
        double sx = q[0];
        double sy = q[1];
        double sz = q[2];
        for (int j = 0; j < n; j++) {
            int a = Math.max(0, j - 1);
            int b = Math.min(n - 1, j + 1);
            tx = TP[b * 3] - TP[a * 3];
            ty = TP[b * 3 + 1] - TP[a * 3 + 1];
            tz = TP[b * 3 + 2] - TP[a * 3 + 2];
            tl = Math.sqrt(tx * tx + ty * ty + tz * tz);
            if (tl > 1.0E-6) {
                tx /= tl;
                ty /= tl;
                tz /= tl;
                wx = tx;
                wy = ty;
                wz = tz;
            } else {
                tx = wx;
                ty = wy;
                tz = wz;
            }
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

    // ------------------------------------------------------------------------------------------------------ the bar

    /** A bar's cross-section: this many points round its rounded square, and how square (as {@link #blob}'s: toward 0 a sharp box). */
    static final int BAR_AROUND = 16;
    static final double BAR_SQUARE = 0.3;

    /**
     * 3.5.5 (the owner: "the arms are like squares. They're like basically rectangles that with the fist at the very edge"): the points
     * given (tubePoint: {@code r} its half width there) drawn as a bar of rounded square section, one flat face up (its frame starts level
     * across the way and is carried along it, so a bent arm never twists), {@code tall} its height to its width, both ends closed flat;
     * banded as every solid of the house.
     */
    public static void bar(FireMesh.Sink sink, int[] bands, double tall) {
        int n = tubeCount;
        if (n < 2) {
            return;
        }
        double tx = TP[3] - TP[0];
        double ty = TP[4] - TP[1];
        double tz = TP[5] - TP[2];
        double tl = Math.max(1.0E-9, Math.sqrt(tx * tx + ty * ty + tz * tz));
        tx /= tl;
        ty /= tl;
        tz /= tl;
        // level across the way (the way x up); straight up or down: any side
        double sx = -tz;
        double sy = 0.0;
        double sz = tx;
        double sl = Math.sqrt(sx * sx + sz * sz);
        if (sl < 1.0E-4) {
            double[] q = Q;
            FireMesh.basis(tx, ty, tz, q);
            sx = q[0];
            sy = q[1];
            sz = q[2];
        } else {
            sx /= sl;
            sz /= sl;
        }
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
            double d = sx * tx + sy * ty + sz * tz;
            sx -= d * tx;
            sy -= d * ty;
            sz -= d * tz;
            sl = Math.sqrt(sx * sx + sy * sy + sz * sz);
            if (sl < 1.0E-6) {
                double[] q = Q;
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
        int around = BAR_AROUND;
        double e = BAR_SQUARE;
        for (int start = 0; start < n - 1; start += RINGS_MOST) {
            int end = Math.min(n - 1, start + RINGS_MOST);
            for (int j = start; j <= end; j++) {
                for (int i = 0; i <= around; i++) {
                    int v = (j - start) * (around + 1) + i;
                    barPoint(j, i, e, tall, v, bands);
                }
            }
            grid(sink, end - start, around);
        }
        // the two ends closed flat
        for (int k = 0; k < 2; k++) {
            int j = k == 0 ? 0 : n - 1;
            int a = k == 0 ? 0 : n - 2;
            int b = k == 0 ? 1 : n - 1;
            double wx = TP[b * 3] - TP[a * 3];
            double wy = TP[b * 3 + 1] - TP[a * 3 + 1];
            double wz = TP[b * 3 + 2] - TP[a * 3 + 2];
            double wl = Math.max(1.0E-9, Math.sqrt(wx * wx + wy * wy + wz * wz));
            double sign = k == 0 ? -1.0 : 1.0;
            double nx = wx / wl * sign;
            double ny = wy / wl * sign;
            double nz = wz / wl * sign;
            double cx = TP[j * 3];
            double cy = TP[j * 3 + 1];
            double cz = TP[j * 3 + 2];
            int cc = shade(cx, cy, cz, nx, ny, nz, bands, 1.0);
            for (int i = 0; i <= around; i++) {
                barPoint(j, i, e, tall, i, bands);
                GRID_C[i] = cc;
            }
            for (int i = 0; i < around; i += 2) {
                if (k == 0) {
                    FireMesh.quad(sink, cx, cy, cz, cc, GRID[(i + 2) * 3], GRID[(i + 2) * 3 + 1], GRID[(i + 2) * 3 + 2], cc, GRID[(i + 1) * 3],
                        GRID[(i + 1) * 3 + 1], GRID[(i + 1) * 3 + 2], cc, GRID[i * 3], GRID[i * 3 + 1], GRID[i * 3 + 2], cc);
                } else {
                    FireMesh.quad(sink, cx, cy, cz, cc, GRID[i * 3], GRID[i * 3 + 1], GRID[i * 3 + 2], cc, GRID[(i + 1) * 3],
                        GRID[(i + 1) * 3 + 1], GRID[(i + 1) * 3 + 2], cc, GRID[(i + 2) * 3], GRID[(i + 2) * 3 + 1], GRID[(i + 2) * 3 + 2], cc);
                }
            }
        }
        tubeCount = 0;
    }

    /** Point {@code i} round the bar's section at its point {@code j}, into GRID and GRID_C at {@code v}. */
    private static void barPoint(int j, int i, double e, double tall, int v, int[] bands) {
        // the section's flat faces square to the side and to the up of the frame (its rounded corners between)
        double an = 2 * Math.PI * i / BAR_AROUND;
        double ca = Math.cos(an);
        double sa = Math.sin(an);
        double a = spow(ca, e);
        double b = spow(sa, e) * tall;
        double na = spow(ca, 2.0 - e);
        double nb = spow(sa, 2.0 - e) / tall;
        double r = TR[j];
        double ox = TF[j * 6] * a + TF[j * 6 + 3] * b;
        double oy = TF[j * 6 + 1] * a + TF[j * 6 + 4] * b;
        double oz = TF[j * 6 + 2] * a + TF[j * 6 + 5] * b;
        double nx = TF[j * 6] * na + TF[j * 6 + 3] * nb;
        double ny = TF[j * 6 + 1] * na + TF[j * 6 + 4] * nb;
        double nz = TF[j * 6 + 2] * na + TF[j * 6 + 5] * nb;
        double nl = Math.max(1.0E-9, Math.sqrt(nx * nx + ny * ny + nz * nz));
        double x = TP[j * 3] + ox * r;
        double y = TP[j * 3 + 1] + oy * r;
        double z = TP[j * 3 + 2] + oz * r;
        GRID[v * 3] = x;
        GRID[v * 3 + 1] = y;
        GRID[v * 3 + 2] = z;
        GRID_C[v] = shade(x, y, z, nx / nl, ny / nl, nz / nl, bands, 1.0);
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

    /**
     * 3.6.3 (the owner's references: the awakened Dough's donuts, puffy lumpy rings of dough with drips hanging off them): a donut round
     * (ax, ay, az) (unit) at (cx, cy, cz), {@code big} its radius and {@code thick} its tube's, its surface swelling in soft lumps
     * ({@code lump}, a share of its tube) that turn with it ({@code turn} radians round its axis: a rolling wheel's lumps roll with it) and
     * heave with {@code time}; {@code drips} drops of dough hanging off its underside (0 for none), stretching and swinging.
     */
    public static void donut(FireMesh.Sink sink, double cx, double cy, double cz, double ax, double ay, double az, double big, double thick,
                             double lump, double turn, int drips, double time, int seed, int[] bands) {
        if (big < 0.02 || thick < 0.01) {
            return;
        }
        double[] q = Q;
        FireMesh.basis(ax, ay, az, q);
        double q0 = q[0];
        double q1 = q[1];
        double q2 = q[2];
        double q3 = q[3];
        double q4 = q[4];
        double q5 = q[5];
        double dist = Math.sqrt(cx * cx + cy * cy + cz * cz);
        double size = (big + thick) / Math.max(0.5, dist) * FireMesh.quality;
        int rows = size > 0.25 ? 20 : size > 0.08 ? 16 : 10; // (rows + 1) x (cols + 1) within GRID: 21 x 13
        int cols = size > 0.25 ? 12 : 8;
        if (low) {
            // 3.6.3: a field of many small donuts (the Anemone's) drawn coarser
            rows = Math.min(rows, 12);
            cols = Math.min(cols, 8);
        }
        double tt = time * 0.04;
        for (int j = 0; j <= rows; j++) {
            double u = 2 * Math.PI * j / rows + turn;
            double cu = Math.cos(u);
            double su = Math.sin(u);
            double ox = q0 * cu + q3 * su;
            double oy = q1 * cu + q4 * su;
            double oz = q2 * cu + q5 * su;
            // the lumps: low noise round the ring (fixed to the dough as it turns) and round its tube
            double lu = 2 * Math.PI * j / rows;
            for (int i = 0; i <= cols; i++) {
                double v = 2 * Math.PI * i / cols;
                double cv = Math.cos(v);
                double sv = Math.sin(v);
                double swell = 1.0 + lump * (0.65 * FireMesh.noise(Math.cos(lu) * 1.6 + seed * 0.13, Math.sin(lu) * 1.6, cv * 0.9 + tt, seed)
                    + 0.35 * FireMesh.noise(Math.cos(lu) * 3.4, Math.sin(lu) * 3.4 + seed * 0.29, sv * 1.7 + tt, seed + 3));
                double r = thick * swell;
                double nx = ox * cv + ax * sv;
                double ny = oy * cv + ay * sv;
                double nz = oz * cv + az * sv;
                double x = cx + ox * big + nx * r;
                double y = cy + oy * big + ny * r;
                double z = cz + oz * big + nz * r;
                int k = j * (cols + 1) + i;
                GRID[k * 3] = x;
                GRID[k * 3 + 1] = y;
                GRID[k * 3 + 2] = z;
                GRID_C[k] = shade(x, y, z, nx, ny, nz, bands, 1.0);
            }
        }
        grid(sink, rows, cols);
        // the drips: off its lowest side (the way down laid across the ring's plane), stretching down and swinging a little
        double dx = -ay * ax;
        double dy = -1.0 + ay * ay;
        double dz = -ay * az;
        double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (drips <= 0 || dl < 0.2) {
            return;
        }
        dx /= dl;
        dy /= dl;
        dz /= dl;
        double sx = ay * dz - az * dy;
        double sy = az * dx - ax * dz;
        double sz = ax * dy - ay * dx;
        for (int k = 0; k < drips; k++) {
            double at = (k - (drips - 1) * 0.5) * 0.55 + (FireMesh.rand(seed, k) - 0.5) * 0.3;
            double ca = Math.cos(at);
            double sa = Math.sin(at);
            double px = cx + (dx * ca + sx * sa) * (big + thick * 0.55);
            double py = cy + (dy * ca + sy * sa) * (big + thick * 0.55);
            double pz = cz + (dz * ca + sz * sa) * (big + thick * 0.55);
            double len = thick * (0.7 + 0.6 * FireMesh.rand(seed, k + 11)) * (1.0 + 0.12 * Math.sin(time * 0.11 + k * 2.1));
            double sway = 0.06 * thick * Math.sin(time * 0.09 + k * 1.3);
            tubeBegin();
            for (int s = 0; s <= 4; s++) {
                double f = s / 4.0;
                double w = thick * 0.5 * (1.0 - 0.55 * f * f);
                tubePoint(px + sx * sway * f, py - len * f, pz + sz * sway * f, w);
            }
            tube(sink, 8, bands, 0.0, 0.0);
            ball(sink, px + sx * sway, py - len - thick * 0.12, pz + sz * sway, thick * 0.3, 0, 1, 0, 1.3, 0.0, time, seed + k, bands);
        }
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
     * A mochi fist at (cx, cy, cz) punching along (fx, fy, fz) (unit), its knuckles' row along the side (sx, sy, sz) (unit, square to it,
     * the user's right), {@code size} its half width; {@code coat} Haki-black. 3.5.5 (the owner: "the arms are like squares. They're like
     * basically rectangles that with the fist at the very edge"): a block of a fist, as square as the arm it ends: the hand a box a little
     * wider than the arm, four square knuckles across its front, the thumb folded across under them on the inner side ({@code left}: a
     * left fist, its thumb on the right).
     */
    public static void fist(FireMesh.Sink sink, double cx, double cy, double cz, double fx, double fy, double fz, double sx, double sy,
                            double sz, double size, boolean coat, double time, int seed, boolean left) {
        int[] bands = coat ? HAKI : MOCHI;
        // up: the side across the way (the back of the hand)
        double ux = sy * fz - sz * fy;
        double uy = sz * fx - sx * fz;
        double uz = sx * fy - sy * fx;
        double h = size;
        if (low) {
            // 3.6.3: a field of many small fists (the Anemone's donuts): one rounded block with its knuckles' ridge, no fingers
            blob(sink, cx + fx * h * 0.05, cy + fy * h * 0.05, cz + fz * h * 0.05, fx, fy, fz, ux, uy, uz, h * 0.8, h * 0.78, h * 0.98, 0.3, 0.0,
                time, seed, bands, 1.0);
            return;
        }
        // the hand: a box, its back where the arm ends
        blob(sink, cx - fx * h * 0.22, cy - fy * h * 0.22, cz - fz * h * 0.22, fx, fy, fz, ux, uy, uz, h * 0.62, h * 0.76, h * 0.96, 0.3, 0.0,
            time, seed, bands, 1.0);
        // four knuckles across its front, square, the middle two a little ahead
        for (int k = 0; k < 4; k++) {
            double off = (k - 1.5) * h * 0.47;
            double fwd = h * (0.52 + (k == 1 || k == 2 ? 0.05 : 0.0));
            blob(sink, cx + fx * fwd + sx * off + ux * h * 0.14, cy + fy * fwd + sy * off + uy * h * 0.14, cz + fz * fwd + sz * off + uz * h * 0.14,
                fx, fy, fz, ux, uy, uz, h * 0.3, h * 0.36, h * 0.21, 0.4, 0.0, time, seed + k, bands, 1.0);
        }
        // the thumb folded across under the knuckles from the inner side
        double in = left ? 1.0 : -1.0;
        double tx = cx + fx * h * 0.5 - ux * h * 0.44 + sx * in * h * 0.22;
        double ty = cy + fy * h * 0.5 - uy * h * 0.44 + sy * in * h * 0.22;
        double tz = cz + fz * h * 0.5 - uz * h * 0.44 + sz * in * h * 0.22;
        blob(sink, tx, ty, tz, sx, sy, sz, fx, fy, fz, h * 0.48, h * 0.2, h * 0.19, 0.4, 0.0, time, seed + 7, bands, 1.0);
    }

    /** As {@link #fist} with a right fist. */
    public static void fist(FireMesh.Sink sink, double cx, double cy, double cz, double fx, double fy, double fz, double sx, double sy,
                            double sz, double size, boolean coat, double time, int seed) {
        fist(sink, cx, cy, cz, fx, fy, fz, sx, sy, sz, size, coat, time, seed, false);
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
