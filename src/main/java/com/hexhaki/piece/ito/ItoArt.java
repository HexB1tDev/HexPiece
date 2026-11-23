package com.hexhaki.piece.ito;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.3.7: the Ito Ito no Mi's strings in 3D (pure; client/ito/ItoFx and tools/preview/ItoShots draw the very same quads), eye-centred
 * (x, y, z from the eye) as every fruit's art, built in the house style (POINT ONE: alpha blended, depth tested, never additive; the
 * owner's word on thin, flat effects: "really poor and poorly made"). A string is never a thin pale line: it is a strand of light as the
 * Light fruit's beam is, a white-hot core under a pink-white halo under a wide soft pink glow, each a ribbon turned to the eye and soft
 * at its edges; strings come several together. With Armament (Haki) on, every string is coated: a near-black core in a violet glow, as
 * the katanas and Supa's blades are. Overheat is a rope of tightly wound string, red-hot and banded as the Magma fruit's fire is (white at
 * its heart, yellow, orange, red at its rim), two bright strands twisting round it.
 *
 * <p>No allocation per frame: every point goes through static scratch arrays (the render thread only).
 */
public final class ItoArt {
    private ItoArt() {
    }

    /** A string's core, halo and glow; coated (Armament on). */
    public static final int CORE = 0xFFFFFF;
    public static final int HALO = 0xFFE4F2;
    public static final int GLOW = 0xF59ACB;
    public static final int COAT_CORE = 0x1C0C28;
    public static final int COAT_HALO = 0x5E2E92;
    public static final int COAT_GLOW = 0x9A5BE0;
    /** Overheat's bands, its heart to its rim, and its glow; coated, its heart dark and its rim violet-red. */
    public static final int[] HEAT = {0xFFF7DC, 0xFFD25A, 0xFF8A24, 0xE0401C};
    public static final int HEAT_GLOW = 0xFF6A26;
    public static final int[] COAT_HEAT = {0x2A0C1C, 0x6A1A3E, 0xB0285E, 0xE04A6A};
    public static final int COAT_HEAT_GLOW = 0xC03A8A;

    /** How long each shows (ticks): the lash, a hit, a sky step. */
    public static final double LASH_LIFE = 8;
    public static final double HIT_LIFE = 10;
    public static final double SKY_LIFE = 14;
    /** The Birdcage's radius at {@code age} ticks of {@code life}: closing in steadily from {@code radius} to {@code end} (the server
     *  cuts at it and every client draws it from the same numbers). */
    public static double cageRadius(double radius, double end, double age, double life) {
        double t = Math.max(0.0, Math.min(1.0, age / Math.max(1.0, life)));
        return radius + (end - radius) * t;
    }

    /** The Birdcage's strings rise from the ground over this many ticks; its bars. */
    public static final double CAGE_RISE = 24;
    public static final int CAGE_BARS = 40;

    // ------------------------------------------------------------------------------------------------------ the strand

    static final int MOST = 64;
    static final double[] PX = new double[MOST * 3];
    static final double[] SIDE = new double[MOST * 3];
    static final double[] FADE = new double[MOST];
    static int count;

    /** Starts a string's points. */
    static void begin() {
        count = 0;
    }

    /** One more point of the string, {@code fade} of its light there (0 to 1). */
    static void point(double x, double y, double z, double fade) {
        if (count >= MOST) {
            return;
        }
        PX[count * 3] = x;
        PX[count * 3 + 1] = y;
        PX[count * 3 + 2] = z;
        FADE[count] = fade;
        count++;
    }

    /**
     * The string through the points given since {@link #begin}: {@code width} its core's half width (blocks), {@code coat} coated,
     * {@code alpha} of it there. Three ribbons turned to the eye, each soft at its edges: the glow (3.2 times as wide), the halo (1.5
     * times), the core.
     */
    static void strand(FireMesh.Sink sink, double width, boolean coat, double alpha) {
        if (count < 2 || alpha < 0.01) {
            return;
        }
        sides();
        // 3.3.7 (the owner, on the first strings: "Not a fan of the strings, it looks like dust"): a core thinner than a pixel, clear at
        // its edges, broke up into specks. Now the core is solid (as opaque at its edges as on its line) and never under about two
        // pixels wide however far it is; the halo at least five; the glow soft and low under them.
        // The three ribbons are never in one plane: drawn through the same line they fought in the depth test pixel by pixel (the
        // specks the owner saw as dust); each lies a hair nearer the eye than the one under it, so the core is always cleanly on top.
        // 3.3.7 (the owner, next: "its meant to be slimmer, they're too thick"): every string under half as wide (SLIM), its core
        // still solid and never under about a pixel and a half; ("sharper in the end too not just square ends"): its last stretch
        // narrows to a point (a ring, {@link #ring}, has no end and does not)
        double w = width * SLIM;
        layer(sink, w * 3.6, coat ? COAT_GLOW : GLOW, 0.14 * alpha, 0.0, 0.0, 0.0);
        layer(sink, w * 1.6, coat ? COAT_HALO : HALO, 0.45 * alpha, 0.0, 1.6, 0.002);
        layer(sink, w * 0.7, coat ? COAT_CORE : CORE, 1.0 * alpha, 1.0, 0.8, 0.004);
    }

    /** How slim the strings are against the widths each move gives them; how much of a string's length its point takes. */
    static final double SLIM = 0.42;
    static final double POINT = 0.3;
    /** While a closed ring is drawn: no point at its end. */
    static boolean round;

    /** The string through the points given, closed on itself (a ring: no pointed end). */
    static void ring(FireMesh.Sink sink, double width, boolean coat, double alpha) {
        round = true;
        strand(sink, width, coat, alpha);
        round = false;
    }

    /**
     * How much of its width a string keeps at point {@code i} of {@link #count}: 1 along it, narrowing to 0 at its tip over {@link #POINT}
     * of its length and at its start over {@link #POINT_START} (the owner, shown God Thread's spears: "where are the sharp ends?": both
     * ends of every string are points, a spear's head a sharp diamond).
     */
    static double taper(int i) {
        if (round || count < 2) {
            return 1.0;
        }
        double left = (count - 1 - i) / ((count - 1) * POINT);
        double from = i / ((count - 1) * POINT_START);
        return Math.min(1.0, Math.max(0.0, Math.min(left, from)));
    }

    /** How much of a string's length its starting point takes. */
    static final double POINT_START = 0.15;

    /** About the angle one pixel takes on a phone's screen (radians): a string's core is never thinner than a couple of these. */
    static final double PIXEL = 0.0016;

    /** Each point's side: square to the string and to the line of sight (a ribbon through the points faces the eye). */
    static void sides() {
        for (int i = 0; i < count; i++) {
            int a = Math.max(0, i - 1);
            int b = Math.min(count - 1, i + 1);
            double tx = PX[b * 3] - PX[a * 3];
            double ty = PX[b * 3 + 1] - PX[a * 3 + 1];
            double tz = PX[b * 3 + 2] - PX[a * 3 + 2];
            double vx = PX[i * 3];
            double vy = PX[i * 3 + 1];
            double vz = PX[i * 3 + 2];
            double sx = ty * vz - tz * vy;
            double sy = tz * vx - tx * vz;
            double sz = tx * vy - ty * vx;
            double l = Math.sqrt(sx * sx + sy * sy + sz * sz);
            if (l < 1.0E-9) {
                sx = 1;
                sy = 0;
                sz = 0;
                l = 1;
            }
            SIDE[i * 3] = sx / l;
            SIDE[i * 3 + 1] = sy / l;
            SIDE[i * 3 + 2] = sz / l;
        }
    }

    /** One ribbon of the strand: {@code half} wide each side, {@code rgb} at {@code alpha} on its line, {@code edge} of that at its edges. */
    static void layer(FireMesh.Sink sink, double half, int rgb, double alpha, double edge) {
        layer(sink, half, rgb, alpha, edge, 0.0, 0.0);
    }

    /**
     * As above, at least {@code pixels} pixels wide each side wherever it is (a line that never breaks into specks), drawn {@code lift}
     * of its distance nearer the eye (layers through one line never share a plane).
     */
    static void layer(FireMesh.Sink sink, double half, int rgb, double alpha, double edge, double pixels, double lift) {
        double k = 1.0 - lift;
        for (int i = 0; i + 1 < count; i++) {
            int a = i * 3;
            int b = (i + 1) * 3;
            int ca = FireMesh.argb(alpha * FADE[i], rgb);
            int cb = FireMesh.argb(alpha * FADE[i + 1], rgb);
            int ea = FireMesh.argb(alpha * FADE[i] * edge, rgb);
            int eb = FireMesh.argb(alpha * FADE[i + 1] * edge, rgb);
            // right by the eye (one's own strings in first person) a string thins, never a band across the view
            double da = Math.sqrt(PX[a] * PX[a] + PX[a + 1] * PX[a + 1] + PX[a + 2] * PX[a + 2]);
            double db = Math.sqrt(PX[b] * PX[b] + PX[b + 1] * PX[b + 1] + PX[b + 2] * PX[b + 2]);
            double ha = Math.max(half * near(PX[a], PX[a + 1], PX[a + 2], NEAR), da * PIXEL * pixels) * taper(i);
            double hb = Math.max(half * near(PX[b], PX[b + 1], PX[b + 2], NEAR), db * PIXEL * pixels) * taper(i + 1);
            for (int side = -1; side <= 1; side += 2) {
                double ox = SIDE[a] * ha * side;
                double oy = SIDE[a + 1] * ha * side;
                double oz = SIDE[a + 2] * ha * side;
                double qx = SIDE[b] * hb * side;
                double qy = SIDE[b + 1] * hb * side;
                double qz = SIDE[b + 2] * hb * side;
                FireMesh.quad(sink, PX[a] * k, PX[a + 1] * k, PX[a + 2] * k, ca, PX[b] * k, PX[b + 1] * k, PX[b + 2] * k, cb, (PX[b] + qx) * k,
                    (PX[b + 1] + qy) * k, (PX[b + 2] + qz) * k, eb, (PX[a] + ox) * k, (PX[a + 1] + oy) * k, (PX[a + 2] + oz) * k, ea);
            }
        }
    }

    /** Within this far of the eye a string thins (a fifth of its width at the eye), and Overheat's rope within {@link #NEAR_ROPE}. */
    static final double NEAR = 1.5;
    static final double NEAR_ROPE = 3.0;

    /** How much of its width a string keeps at (x, y, z) from the eye. */
    static double near(double x, double y, double z, double within) {
        return Math.max(0.2, Math.min(1.0, Math.sqrt(x * x + y * y + z * z) / within));
    }

    /** A straight string from A to B in {@code pieces} pieces, its light fading from {@code fa} to {@code fb}. */
    public static void line(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double width, boolean coat,
                            double alpha, double fa, double fb, int pieces) {
        // enough pieces for its two points to narrow smoothly to nothing with a body between them
        pieces = Math.max(pieces, 6);
        begin();
        for (int k = 0; k <= pieces; k++) {
            double t = k / (double) pieces;
            point(ax + (bx - ax) * t, ay + (by - ay) * t, az + (bz - az) * t, fa + (fb - fa) * t);
        }
        strand(sink, width, coat, alpha);
    }

    static double ease(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return 1.0 - (1.0 - t) * (1.0 - t);
    }

    static double clamp(double t) {
        return Math.max(0.0, Math.min(1.0, t));
    }

    // ------------------------------------------------------------------------------------------------------ the moves

    /**
     * The attack button's lash: five strings whipping out of the hand (x, y, z) across the front (facing fx, fz), {@code reach} long,
     * sweeping right to left ({@code which} 1) or left to right; the tips lag behind as a whip's do; gone in {@link #LASH_LIFE}.
     */
    public static void lash(FireMesh.Sink sink, double x, double y, double z, double fx, double fz, double reach, int which, double age,
                            boolean coat) {
        if (age < 0 || age > LASH_LIFE) {
            return;
        }
        double fl = Math.max(1.0E-6, Math.hypot(fx, fz));
        fx /= fl;
        fz /= fl;
        double rx = -fz;
        double rz = fx;
        double dir = which == 1 ? -1.0 : 1.0;
        double sweep = ease(age / 4.0);
        double fade = age < 4.0 ? 1.0 : 1.0 - (age - 4.0) / (LASH_LIFE - 4.0);
        for (int s = 0; s < 5; s++) {
            double spread = (s - 2) * 0.12;
            begin();
            for (int k = 0; k <= 10; k++) {
                double t = k / 10.0;
                // the angle across the front: the base leads, the tip lags (a whip)
                double lead = sweep - 0.35 * t * (1.0 - sweep);
                double ang = dir * (1.1 - 2.2 * clamp(lead)) + spread;
                double r = reach * t * (0.25 + 0.75 * ease(age / 2.5));
                double px = x + (fx * Math.cos(ang) + rx * Math.sin(ang)) * r;
                double pz = z + (fz * Math.cos(ang) + rz * Math.sin(ang)) * r;
                double py = y + 0.25 * Math.sin(Math.PI * t) * (s % 2 == 0 ? 1 : -0.6) - 0.15 * t;
                point(px, py, pz, (1.0 - 0.55 * t) * fade);
            }
            strand(sink, 0.04, coat, 1.0);
        }
        // the sweep the strings cut through the air: a crescent of glow from where they began to where they are
        double from = dir * 1.1;
        double to = dir * (1.1 - 2.2 * clamp(sweep));
        double start = Math.atan2(-fx, -fz); // FireMesh.arc's angle of the way ahead (as LightArt.wave turns it)
        double a0 = Math.min(from, to);
        double a1 = Math.max(from, to);
        if (a1 - a0 > 0.05) {
            // stacked five deep as the Light fruit's wave is (a flat band alone reads as a line from the side)
            double ring = reach * 0.8;
            for (int k = -2; k <= 2; k++) {
                double h = y - 0.1 + k * 0.08;
                double core = k == 0 ? 1.0 : Math.abs(k) == 1 ? 0.65 : 0.35;
                FireMesh.arc(sink, x, h, z, 0, 1, 0, ring, ring * 0.22 * fade + 0.06, start - a1, a1 - a0, 0.0, coat ? COAT_GLOW : GLOW,
                    0.3 * fade * core);
                FireMesh.arc(sink, x, h, z, 0, 1, 0, ring, ring * 0.07 * fade + 0.03, start - a1, a1 - a0, 0.0, coat ? COAT_HALO : CORE,
                    0.7 * fade * core);
            }
        }
    }

    /** A string bullet at (x, y, z) flying along (ux, uy, uz) (unit): a bright dart, its thread trailing back, a glint at its head. */
    public static void bullet(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double age, boolean coat) {
        double grow = ease(age / 1.5);
        line(sink, x - ux * 2.2 * grow, y - uy * 2.2 * grow, z - uz * 2.2 * grow, x, y, z, 0.08, coat, 1.0, 0.5, 1.0, 3);
        line(sink, x - ux * 8.0 * grow, y - uy * 8.0 * grow, z - uz * 8.0 * grow, x - ux * 2.2 * grow, y - uy * 2.2 * grow,
            z - uz * 2.2 * grow, 0.04, coat, 0.85, 0.0, 0.5, 4);
        FireMesh.shell(sink, x, y, z, 0.4, coat ? COAT_GLOW : GLOW, 0.4, 0.0);
        FireMesh.shell(sink, x, y, z, 0.13, coat ? COAT_HALO : CORE, 0.9, 0.3);
        FireMesh.ring(sink, x - ux * 0.3, y - uy * 0.3, z - uz * 0.3, ux, uy, uz, 0.22, 0.06, -0.1, coat ? COAT_HALO : HALO, 0.6);
    }

    /**
     * Strings striking at (x, y, z) (coming along ux, uy, uz), {@code size} across: a flash, a ring and short cut strands flying off,
     * gone in {@link #HIT_LIFE}.
     */
    public static void hit(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double size, double age,
                           boolean coat, int seed) {
        if (age < 0 || age > HIT_LIFE) {
            return;
        }
        double t = age / HIT_LIFE;
        FireMesh.shell(sink, x, y, z, size * (0.3 + 0.5 * ease(t * 2)), coat ? COAT_GLOW : GLOW, 0.4 * (1.0 - t), 0.0);
        FireMesh.shell(sink, x, y, z, size * 0.18 * (1.0 - t), coat ? COAT_HALO : CORE, 0.9 * (1.0 - t), 0.2);
        FireMesh.ring(sink, x, y, z, ux, uy, uz, size * (0.2 + 0.9 * ease(t)), size * 0.35 * (1.0 - t) + 0.05, 0.0, coat ? COAT_GLOW : GLOW,
            0.45 * (1.0 - t));
        FireMesh.ring(sink, x, y, z, ux, uy, uz, size * (0.2 + 0.9 * ease(t)), size * 0.1 * (1.0 - t) + 0.03, 0.0, coat ? COAT_HALO : CORE,
            0.8 * (1.0 - t));
        for (int i = 0; i < 6; i++) {
            double a = FireMesh.rand(seed, i) * 2 * Math.PI;
            double up = FireMesh.rand(seed, i + 10) * 1.2 - 0.4;
            double dx = Math.cos(a);
            double dz = Math.sin(a);
            double l = Math.sqrt(dx * dx + up * up + dz * dz);
            double out = size * (0.15 + 0.9 * ease(t));
            double len = size * 0.35 * (1.0 - t);
            double sx = x + dx / l * out;
            double sy = y + up / l * out;
            double sz = z + dz / l * out;
            line(sink, sx, sy, sz, sx + dx / l * len, sy + up / l * len, sz + dz / l * len, 0.03, coat, 1.0 - t, 1.0, 0.2, 1);
        }
    }

    /**
     * Goshikito: five strings slashing down from the sky onto the ground at (x, y, z) along (wx, wz), {@code spread} either side;
     * falling for {@code fall} ticks, then five glowing cuts in the ground and a burst of dust, all gone by {@code life}.
     */
    public static void goshiki(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double spread, double fall, double age,
                               double life, boolean coat, int seed) {
        if (age < 0 || age > life) {
            return;
        }
        double wl = Math.max(1.0E-6, Math.hypot(wx, wz));
        wx /= wl;
        wz /= wl;
        double sx = -wz;
        double sz = wx;
        double half = 4.0;
        for (int k = 0; k < 5; k++) {
            double off = (k - 2) * spread * 0.5;
            double bx = x + sx * off;
            double bz = z + sz * off;
            if (age < fall + 6) {
                // the string, from high behind down onto its line: its tip falls first, then it whips up and away
                double drop = ease(age / fall);
                double lift = age > fall ? ease((age - fall) / 6.0) : 0.0;
                double topX = bx - wx * 6.0;
                double topY = y + 18.0;
                double topZ = bz - wz * 6.0;
                double tipX = bx + wx * half * (drop * 2 - 1);
                double tipZ = bz + wz * half * (drop * 2 - 1);
                double tipY = y + 18.0 * (1.0 - drop) + 0.05 + 10.0 * lift;
                line(sink, topX, topY, topZ, tipX, tipY, tipZ, 0.11 + 0.02 * (k % 2), coat, 1.0 - lift, 0.25, 1.0, 10);
            }
            if (age >= fall) {
                // the cut in the ground along its line, glowing then cooling
                double u = (age - fall) / (life - fall);
                double glow = (1.0 - u) * (1.0 - u);
                line(sink, bx - wx * half, y + 0.04, bz - wz * half, bx + wx * half, y + 0.04, bz + wz * half, 0.11, coat, glow, 0.5, 1.0, 6);
            }
        }
        if (age >= fall && age < fall + 14) {
            double u = (age - fall) / 14.0;
            FireMesh.palette(DUST);
            for (int i = 0; i < 5; i++) {
                double a = (i - 2) * spread * 0.5;
                FireMesh.puff(sink, x + sx * a + wx * (FireMesh.rand(seed, i) - 0.5) * 4, y + 0.3 + 0.6 * u, z + sz * a + wz * (FireMesh.rand(seed, i)
                    - 0.5) * 4, 0.5 + 0.6 * u, 0, 1, 0, 0.2, 0.3, 0.5 - 0.3 * u, 0.3, false, 0.6 * (1.0 - u), age, seed + i);
            }
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /** The ground's dust thrown up by a cut (FireMesh.puff's palette while it is drawn): earth brown to pale. */
    public static final int[] DUST = {0x4A3A2C, 0x5E4B3A, 0x74604C, 0x8A7660, 0xA08C76, 0xB6A38E, 0xCCBAA6, 0xE0D2C0};

    static final double[] RING = new double[25 * 9 * 3];
    static final int[] RING_C = new int[25 * 9];

    /**
     * Overheat: a rope of red-hot string from the hand (hx, hy, hz) along (wx, wz), {@code length} long when it is all out (in 5 ticks,
     * a wave running down it as down a whip), held, then drawn back and gone by {@code life}: a tube banded white-hot to red by how
     * square it faces the eye, a wide orange glow round it, two bright strands twisting round it.
     */
    public static void overheat(FireMesh.Sink sink, double hx, double hy, double hz, double wx, double wz, double length, double age,
                                double life, boolean coat, int seed) {
        if (age < 0 || age > life) {
            return;
        }
        double wl = Math.max(1.0E-6, Math.hypot(wx, wz));
        wx /= wl;
        wz /= wl;
        double sx = -wz;
        double sz = wx;
        double out = age < 5 ? ease(age / 5.0) : age > life - 6 ? clamp((life - age) / 6.0) : 1.0;
        double len = length * out;
        double radius = 0.2;
        int along = 24;
        int around = 8;
        int[] bands = coat ? COAT_HEAT : HEAT;
        double fade = age > life - 3 ? (life - age) / 3.0 : 1.0;
        // the rope's line: out along the way, a whip's wave running down it and dying away
        begin();
        for (int j = 0; j <= along; j++) {
            double t = j / (double) along;
            double wave = Math.sin(t * 7.0 - age * 1.6) * 0.9 * t * Math.max(0.0, 1.0 - age / 10.0);
            double lift = 0.4 * Math.sin(Math.PI * t) * out - 0.25 * t;
            point(hx + wx * len * t + sx * wave, hy + lift, hz + wz * len * t + sz * wave, 1.0 - 0.3 * t);
        }
        int n = count;
        for (int j = 0; j < n; j++) {
            HX[j * 3] = PX[j * 3];
            HX[j * 3 + 1] = PX[j * 3 + 1];
            HX[j * 3 + 2] = PX[j * 3 + 2];
        }
        // its glow first (behind it): the line as a wide soft ribbon of orange, thinning near the eye as the rope does
        sides();
        for (int j = 0; j < n; j++) {
            double k = near(HX[j * 3], HX[j * 3 + 1], HX[j * 3 + 2], NEAR_ROPE);
            FADE[j] *= k * k;
        }
        layer(sink, radius * 3.4, coat ? COAT_HEAT_GLOW : HEAT_GLOW, 0.3 * fade, 0.0);
        // the banded tube round that line
        double[] b = T_BASIS;
        for (int j = 0; j < n; j++) {
            int p = Math.max(0, j - 1);
            int q = Math.min(n - 1, j + 1);
            FireMesh.basis(HX[q * 3] - HX[p * 3], HX[q * 3 + 1] - HX[p * 3 + 1], HX[q * 3 + 2] - HX[p * 3 + 2], b);
            double t = j / (double) (n - 1);
            double r = radius * (1.0 - 0.55 * t) * (0.6 + 0.4 * out) * near(HX[j * 3], HX[j * 3 + 1], HX[j * 3 + 2], NEAR_ROPE);
            for (int i = 0; i <= around; i++) {
                double a = 2 * Math.PI * i / around;
                double ox = b[0] * Math.cos(a) + b[3] * Math.sin(a);
                double oy = b[1] * Math.cos(a) + b[4] * Math.sin(a);
                double oz = b[2] * Math.cos(a) + b[5] * Math.sin(a);
                double px = HX[j * 3] + ox * r;
                double py = HX[j * 3 + 1] + oy * r;
                double pz = HX[j * 3 + 2] + oz * r;
                int v = j * (around + 1) + i;
                RING[v * 3] = px;
                RING[v * 3 + 1] = py;
                RING[v * 3 + 2] = pz;
                double el = Math.max(1.0E-6, Math.sqrt(px * px + py * py + pz * pz));
                double facing = Math.abs((ox * px + oy * py + oz * pz) / el);
                int band = facing > 0.8 ? 0 : facing > 0.55 ? 1 : facing > 0.3 ? 2 : 3;
                RING_C[v] = FireMesh.argb(0.95 * fade, bands[band]);
            }
        }
        for (int j = 0; j + 1 < n; j++) {
            for (int i = 0; i < around; i++) {
                int a = j * (around + 1) + i;
                int c = a + around + 1;
                FireMesh.quad(sink, RING[a * 3], RING[a * 3 + 1], RING[a * 3 + 2], RING_C[a], RING[(a + 1) * 3], RING[(a + 1) * 3 + 1],
                    RING[(a + 1) * 3 + 2], RING_C[a + 1], RING[(c + 1) * 3], RING[(c + 1) * 3 + 1], RING[(c + 1) * 3 + 2], RING_C[c + 1], RING[c * 3],
                    RING[c * 3 + 1], RING[c * 3 + 2], RING_C[c]);
            }
        }
        // two bright strands wound round it (it is a rope of string), turning as it lashes
        for (int h = 0; h < 2; h++) {
            begin();
            for (int j = 0; j < n; j++) {
                int p = Math.max(0, j - 1);
                int q = Math.min(n - 1, j + 1);
                FireMesh.basis(HX[q * 3] - HX[p * 3], HX[q * 3 + 1] - HX[p * 3 + 1], HX[q * 3 + 2] - HX[p * 3 + 2], b);
                double t = j / (double) (n - 1);
                double a = t * 18.0 + h * Math.PI + age * 0.8;
                double r = radius * (1.0 - 0.55 * t) * (0.6 + 0.4 * out) * 1.05;
                point(HX[j * 3] + (b[0] * Math.cos(a) + b[3] * Math.sin(a)) * r, HX[j * 3 + 1] + (b[1] * Math.cos(a) + b[4] * Math.sin(a)) * r,
                    HX[j * 3 + 2] + (b[2] * Math.cos(a) + b[5] * Math.sin(a)) * r, 1.0 - 0.4 * t);
            }
            strand(sink, 0.018, coat, fade);
        }
        // heat at the hand and a crack of light at the tip as it snaps out
        FireMesh.shell(sink, hx, hy, hz, 0.5 * near(hx, hy, hz, NEAR_ROPE), coat ? COAT_HEAT_GLOW : HEAT_GLOW, 0.35 * fade, 0.0);
        if (age > 3 && age < 9) {
            double u = (age - 3) / 6.0;
            double tx = HX[(n - 1) * 3];
            double ty = HX[(n - 1) * 3 + 1];
            double tz = HX[(n - 1) * 3 + 2];
            FireMesh.shell(sink, tx, ty, tz, 0.4 + 0.8 * u, coat ? COAT_HEAT_GLOW : HEAT_GLOW, 0.6 * (1.0 - u), 0.0);
            FireMesh.ring(sink, tx, ty, tz, wx, 0, wz, 0.3 + 1.2 * u, 0.15 * (1.0 - u) + 0.02, 0.0, coat ? COAT_HEAT[3] : HEAT[1], 0.7 * (1.0 - u));
        }
    }

    static final double[] HX = new double[MOST * 3];
    static final double[] T_BASIS = new double[6];

    /**
     * Parasite: strings from the hand (hx, hy, hz) to the held body's head, both arms and both legs ({@code at}: five points, x, y, z
     * each), sagging a little, pulsing, little knots of light where they hold.
     */
    public static void parasite(FireMesh.Sink sink, double hx, double hy, double hz, double[] at, double age, double alpha, boolean coat) {
        double grow = ease(age / 4.0);
        for (int k = 0; k < 5; k++) {
            double tx = at[k * 3];
            double ty = at[k * 3 + 1];
            double tz = at[k * 3 + 2];
            double dx = tx - hx;
            double dy = ty - hy;
            double dz = tz - hz;
            double l = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double sag = Math.min(0.6, l * 0.05) * (1.0 + 0.2 * Math.sin(age * 0.4 + k));
            begin();
            for (int j = 0; j <= 10; j++) {
                double t = j / 10.0 * grow;
                double pulse = 0.75 + 0.25 * Math.sin(age * 0.6 - t * 6.0 + k);
                point(hx + dx * t, hy + dy * t - sag * 4.0 * t * (1.0 - t), hz + dz * t, pulse);
            }
            strand(sink, 0.035, coat, alpha);
            if (grow > 0.99) {
                FireMesh.shell(sink, tx, ty, tz, 0.24, coat ? COAT_GLOW : GLOW, 0.55 * alpha, 0.0);
                FireMesh.shell(sink, tx, ty, tz, 0.08, coat ? COAT_HALO : CORE, 0.95 * alpha, 0.2);
            }
        }
    }

    /**
     * Spider's Web in front of its user: its middle (x, y, z), facing (nx, ny, nz) (unit, away from its user), {@code radius}: twelve
     * strands out from the middle, five rings of strands between them sagging inward as a spider's do; spreading over 5 ticks, gone in
     * the last 6 of {@code life}.
     */
    public static void web(FireMesh.Sink sink, double x, double y, double z, double nx, double ny, double nz, double radius, double age,
                           double life, boolean coat, double alpha) {
        if (age < 0 || age > life) {
            return;
        }
        double grow = ease(age / 5.0);
        double fade = (age > life - 6 ? clamp((life - age) / 6.0) : 1.0) * alpha;
        double[] b = T_BASIS;
        FireMesh.basis(nx, ny, nz, b);
        double r = radius * grow;
        int spokes = 12;
        double billow = 0.15 * Math.sin(age * 0.2);
        for (int k = 0; k < spokes; k++) {
            double a = 2 * Math.PI * k / spokes + 0.13;
            double cx = b[0] * Math.cos(a) + b[3] * Math.sin(a);
            double cy = b[1] * Math.cos(a) + b[4] * Math.sin(a);
            double cz = b[2] * Math.cos(a) + b[5] * Math.sin(a);
            begin();
            for (int j = 0; j <= 4; j++) {
                double t = j / 4.0;
                double bow = billow * Math.sin(Math.PI * t);
                point(x + cx * r * t + nx * bow, y + cy * r * t + ny * bow, z + cz * r * t + nz * bow, 1.0 - 0.3 * t);
            }
            strand(sink, 0.038, coat, fade);
        }
        for (int ring = 1; ring <= 5; ring++) {
            double rr = r * ring / 5.0;
            begin();
            for (int k = 0; k <= spokes * 3; k++) {
                double a = 2 * Math.PI * k / (spokes * 3) + 0.13;
                // between two spokes the thread sags in toward the middle
                double sagIn = 1.0 - 0.12 * Math.sin(Math.PI * ((k % 3) / 3.0));
                double cx = b[0] * Math.cos(a) + b[3] * Math.sin(a);
                double cy = b[1] * Math.cos(a) + b[4] * Math.sin(a);
                double cz = b[2] * Math.cos(a) + b[5] * Math.sin(a);
                double bow = billow * Math.sin(Math.PI * rr / Math.max(0.01, r));
                point(x + cx * rr * sagIn + nx * bow, y + cy * rr * sagIn + ny * bow, z + cz * rr * sagIn + nz * bow, 0.85);
            }
            ring(sink, 0.03, coat, fade);
        }
        FireMesh.shell(sink, x, y, z, 0.5 * grow, coat ? COAT_GLOW : GLOW, 0.45 * fade, 0.0);
    }

    /**
     * The Birdcage: its middle on the ground (x, y, z), {@code radius} now: {@link #CAGE_BARS} strings from its crown down to the ground
     * all round (rising out of the ground over {@link #CAGE_RISE} ticks), a ring of light where they meet the ground; {@code alpha} of
     * it there.
     */
    public static void cage(FireMesh.Sink sink, double x, double y, double z, double radius, double age, double alpha, boolean coat) {
        double rise = ease(age / CAGE_RISE);
        double dist = Math.sqrt(x * x + y * y + z * z);
        // fewer pieces a bar when the cage is small on the screen
        int pieces = dist - radius > 60 ? 5 : 10;
        double width = 0.08 + radius * 0.003;
        for (int k = 0; k < CAGE_BARS; k++) {
            double a = 2 * Math.PI * k / CAGE_BARS + 0.05;
            double ca = Math.cos(a);
            double sa = Math.sin(a);
            begin();
            for (int j = 0; j <= pieces; j++) {
                // up from the ground (elevation 0) to the crown (90 degrees), as far as it has risen
                double e = Math.PI / 2 * j / pieces * rise;
                double rr = radius * Math.cos(e);
                point(x + ca * rr, y + radius * Math.sin(e), z + sa * rr, 0.75 + 0.25 * Math.sin(e));
            }
            strand(sink, width, coat, alpha);
        }
        // two rings round it: where the bars meet the ground, and a third of the way up
        for (int ring = 0; ring < 2; ring++) {
            double e = ring == 0 ? 0.02 : 0.6;
            if (e > Math.PI / 2 * rise) {
                continue;
            }
            double rr = radius * Math.cos(e);
            begin();
            for (int k = 0; k <= CAGE_BARS; k++) {
                double a = 2 * Math.PI * k / CAGE_BARS + 0.05;
                point(x + Math.cos(a) * rr, y + radius * Math.sin(e), z + Math.sin(a) * rr, ring == 0 ? 1.0 : 0.7);
            }
            ring(sink, width * (ring == 0 ? 1.4 : 0.9), coat, alpha);
        }
    }

    /**
     * Break White: waves of white string rolling out over the ground from its middle (x, y, z), {@code reach} far: each wave a ring of
     * strings arching up and over as a breaker does, lower as it rolls out; {@code age} of {@code life}.
     */
    public static void waves(FireMesh.Sink sink, double x, double y, double z, double reach, int waves, double every, double speed,
                             double age, double life, boolean coat) {
        if (age < 0 || age > life) {
            return;
        }
        int sheet = coat ? COAT_HALO : HALO;
        for (int w = 0; w < waves; w++) {
            double r = (age - w * every) * speed;
            if (r <= 0.2 || r > reach) {
                continue;
            }
            double u = r / reach;
            double h = 1.9 * (1.0 - 0.6 * u) * ease(r / 1.5);
            double fade = u > 0.8 ? (1.0 - u) / 0.2 : 1.0;
            int seg = Math.max(24, Math.min(WAVE_SEG, (int) (r * 4)));
            // the breaker's cross-section: up from behind its front, over the crest, curling forward and down
            for (int j = 0; j < WAVE_PROFILE; j++) {
                double t = j / (double) (WAVE_PROFILE - 1);
                PR[j] = r - 1.0 + 1.6 * t;
                PH[j] = h * Math.sin(Math.PI * Math.pow(t, 0.8)) + 0.05;
                PA[j] = fade * (0.1 + 0.32 * Math.sin(Math.PI * t));
            }
            for (int i = 0; i <= seg; i++) {
                double a = 2 * Math.PI * i / seg + 0.3 * w;
                double ca = Math.cos(a);
                double sa = Math.sin(a);
                for (int j = 0; j < WAVE_PROFILE; j++) {
                    int v = i * WAVE_PROFILE + j;
                    WP[v * 3] = x + ca * PR[j];
                    WP[v * 3 + 1] = y + PH[j];
                    WP[v * 3 + 2] = z + sa * PR[j];
                }
            }
            // the sheet of string, see-through, brightest along its crest
            for (int i = 0; i < seg; i++) {
                for (int j = 0; j + 1 < WAVE_PROFILE; j++) {
                    int a = i * WAVE_PROFILE + j;
                    int b = a + WAVE_PROFILE;
                    int ca = FireMesh.argb(PA[j], sheet);
                    int cb = FireMesh.argb(PA[j + 1], sheet);
                    FireMesh.quad(sink, WP[a * 3], WP[a * 3 + 1], WP[a * 3 + 2], ca, WP[(a + 1) * 3], WP[(a + 1) * 3 + 1], WP[(a + 1) * 3 + 2], cb,
                        WP[(b + 1) * 3], WP[(b + 1) * 3 + 1], WP[(b + 1) * 3 + 2], cb, WP[b * 3], WP[b * 3 + 1], WP[b * 3 + 2], ca);
                }
            }
            // strings running over it, every other piece of the round
            for (int i = 0; i < seg; i += 2) {
                begin();
                for (int j = 0; j < WAVE_PROFILE; j++) {
                    int v = i * WAVE_PROFILE + j;
                    point(WP[v * 3], WP[v * 3 + 1], WP[v * 3 + 2], 0.6 + 0.4 * Math.sin(Math.PI * j / (WAVE_PROFILE - 1)));
                }
                strand(sink, 0.035, coat, fade);
            }
            // its crest, one bright string all the way round
            int crest = WAVE_PROFILE / 2 + 1;
            begin();
            for (int i = 0; i <= seg; i++) {
                int v = i * WAVE_PROFILE + crest;
                point(WP[v * 3], WP[v * 3 + 1], WP[v * 3 + 2], 1.0);
            }
            ring(sink, 0.06, coat, fade);
            // the white churned up where it meets the ground
            FireMesh.ring(sink, x, y + 0.08, z, 0, 1, 0, r + 0.5, 0.9, 0.2, sheet, 0.35 * fade);
        }
    }

    static final int WAVE_SEG = 64;
    static final int WAVE_PROFILE = 6;
    static final double[] PR = new double[WAVE_PROFILE];
    static final double[] PH = new double[WAVE_PROFILE];
    static final double[] PA = new double[WAVE_PROFILE];
    static final double[] WP = new double[(WAVE_SEG + 1) * WAVE_PROFILE * 3];

    /**
     * God Thread: sixteen Haki-coated spears of string fired at once from round the user's chest (ox, oy, oz) to meet at (tx, ty, tz),
     * arriving at {@code flight}: each a thick twisted spear, black at its core in a violet glow, its point leading.
     */
    public static void thread(FireMesh.Sink sink, double tx, double ty, double tz, double ox, double oy, double oz, int spears, double age,
                              double flight) {
        if (age < 0 || age > flight + 4) {
            return;
        }
        double dx = tx - ox;
        double dy = ty - oy;
        double dz = tz - oz;
        double l = Math.max(1.0E-6, Math.sqrt(dx * dx + dy * dy + dz * dz));
        double ux = dx / l;
        double uy = dy / l;
        double uz = dz / l;
        double[] b = T_BASIS;
        FireMesh.basis(ux, uy, uz, b);
        double t = clamp(age / flight);
        double fade = age > flight ? 1.0 - (age - flight) / 4.0 : 1.0;
        for (int k = 0; k < spears; k++) {
            double a = 2 * Math.PI * k / spears;
            double ring = 2.4 + 0.4 * (k % 2);
            // from a ring round and behind the chest, curving in to the point
            double sx0 = ox - ux * 1.0 + (b[0] * Math.cos(a) + b[3] * Math.sin(a)) * ring;
            double sy0 = oy - uy * 1.0 + (b[1] * Math.cos(a) + b[4] * Math.sin(a)) * ring + 0.6;
            double sz0 = oz - uz * 1.0 + (b[2] * Math.cos(a) + b[5] * Math.sin(a)) * ring;
            double head = ease(t);
            double tail = Math.max(0.0, head - 0.35);
            begin();
            for (int j = 0; j <= 8; j++) {
                double s = tail + (head - tail) * j / 8.0;
                double bend = 1.0 - s;
                double px = sx0 + (tx - sx0) * s + (sx0 - ox) * 0.4 * s * bend;
                double py = sy0 + (ty - sy0) * s + (sy0 - oy) * 0.4 * s * bend;
                double pz = sz0 + (tz - sz0) * s + (sz0 - oz) * 0.4 * s * bend;
                point(px, py, pz, 0.4 + 0.6 * j / 8.0);
            }
            strand(sink, 0.14, true, fade);
            // its head: a thicker point of coated string, a violet glint on its tip
            double hx = PX[(count - 1) * 3];
            double hy = PX[(count - 1) * 3 + 1];
            double hz = PX[(count - 1) * 3 + 2];
            double kx = hx - PX[(count - 2) * 3];
            double ky = hy - PX[(count - 2) * 3 + 1];
            double kz = hz - PX[(count - 2) * 3 + 2];
            double kl = Math.max(1.0E-6, Math.sqrt(kx * kx + ky * ky + kz * kz));
            line(sink, hx - kx / kl * 1.4, hy - ky / kl * 1.4, hz - kz / kl * 1.4, hx + kx / kl * 0.3, hy + ky / kl * 0.3, hz + kz / kl * 0.3, 0.26,
                true, fade, 1.0, 0.8, 8);
            FireMesh.shell(sink, hx, hy, hz, 0.45, COAT_GLOW, 0.5 * fade, 0.0);
            FireMesh.shell(sink, hx, hy, hz, 0.12, 0xE8D4FF, 0.8 * fade, 0.2);
        }
    }

    /**
     * Sora no Michi: a step on strings in the air at the feet (x, y, z): three strings hung from far up in the sky down to the hands and
     * feet, a ring of light under the feet; gone in {@link #SKY_LIFE}.
     */
    public static void sky(FireMesh.Sink sink, double x, double y, double z, double age, boolean coat, int seed) {
        if (age < 0 || age > SKY_LIFE) {
            return;
        }
        double t = age / SKY_LIFE;
        double fade = 1.0 - t * t;
        for (int k = 0; k < 3; k++) {
            double a = FireMesh.rand(seed, k) * 2 * Math.PI;
            double ox = Math.cos(a) * 0.4;
            double oz = Math.sin(a) * 0.4;
            line(sink, x + ox * 0.4, y + 0.1 + 0.5 * k, z + oz * 0.4, x + ox * 8, y + 22.0, z + oz * 8, 0.05, coat, fade, 1.0, 0.0, 8);
        }
        FireMesh.ring(sink, x, y + 0.05, z, 0, 1, 0, 0.4 + 0.8 * ease(t * 2), 0.12 * (1.0 - t) + 0.02, 0.0, coat ? COAT_HALO : HALO, 0.7 * fade);
        FireMesh.shell(sink, x, y + 0.1, z, 0.5, coat ? COAT_GLOW : GLOW, 0.3 * fade, 0.0);
    }

    /**
     * Self-Surgery: strings stitching the body whose middle is (x, y, z), {@code height} tall: crossed stitches of light over it,
     * pulling tight, gone by {@code life}.
     */
    public static void stitch(FireMesh.Sink sink, double x, double y, double z, double height, double age, double life, int seed) {
        if (age < 0 || age > life) {
            return;
        }
        double t = age / life;
        double fade = Math.min(1.0, age / 4.0) * (1.0 - t * t);
        for (int i = 0; i < 8; i++) {
            double a = FireMesh.rand(seed, i) * 2 * Math.PI;
            double h = (FireMesh.rand(seed, i + 20) - 0.5) * height * 0.8;
            double cx = x + Math.cos(a) * 0.36;
            double cz = z + Math.sin(a) * 0.36;
            double cy = y + h;
            double tx = -Math.sin(a) * 0.14;
            double tz = Math.cos(a) * 0.14;
            double pull = 1.0 - 0.3 * ease(age / 10.0);
            line(sink, cx - tx * pull, cy - 0.14 * pull, cz - tz * pull, cx + tx * pull, cy + 0.14 * pull, cz + tz * pull, 0.026, false, fade, 1.0, 1.0,
                1);
            line(sink, cx - tx * pull, cy + 0.14 * pull, cz - tz * pull, cx + tx * pull, cy - 0.14 * pull, cz + tz * pull, 0.026, false, fade, 1.0, 1.0,
                1);
        }
    }
}
