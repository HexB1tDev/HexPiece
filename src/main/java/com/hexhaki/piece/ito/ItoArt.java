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

    /** How long each shows (ticks): the lash, a hit. */
    public static final double LASH_LIFE = 8;
    public static final double HIT_LIFE = 10;
    /**
     * 3.4.2: the Birdcage's radius {@code age} ticks into closing over {@code close} ticks: from {@code radius} to {@code shut}, slowly
     * at first, fastest in the middle, slowing as it shuts (the server cuts and pulls at it, every client draws it from the same numbers).
     */
    public static double closing(double radius, double shut, double age, double close) {
        double t = clamp(age / Math.max(1.0, close));
        return radius + (shut - radius) * t * t * (3.0 - 2.0 * t);
    }

    /** 3.4.2: let go, the Birdcage's strings spread down from its crown to the ground over this many ticks; its bars. */
    public static final double CAGE_SPREAD = 20;
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
        // 3.4.2: a strand may be drawn in another palette for a while (Overheat's red-hot strands, cooling as they scatter)
        int[] t = coat || tint == null ? null : tint;
        // a hot strand glows harder (a red-hot string, never a pale wisp)
        layer(sink, w * 3.6, coat ? COAT_GLOW : t != null ? t[2] : GLOW, (t != null ? 0.3 : 0.14) * alpha, 0.0, 0.0, 0.0);
        layer(sink, w * 1.6, coat ? COAT_HALO : t != null ? t[1] : HALO, (t != null ? 0.7 : 0.45) * alpha, 0.0, 1.6, 0.002);
        layer(sink, w * 0.7, coat ? COAT_CORE : t != null ? t[0] : CORE, 1.0 * alpha, 1.0, 0.8, 0.004);
    }

    /** 3.4.2: the palette strands are drawn in while set ({core, halo, glow}; null: the string's own white and pink). */
    static int[] tint;
    /** Red-hot strands, and the same cooling (Overheat's strands as they scatter). */
    static final int[] HOT = {0xFFE2A8, 0xFF9436, 0xFF4A1A};
    static final int[] COOLING = {0xFF9A50, 0xD0401A, 0x7A1A10};
    /** A white burst's strands: the string's own white over a stronger pink glow (a burst of strings, never pale wisps). */
    static final int[] BRIGHT = {0xFFFFFF, 0xFFC8E6, 0xF06CB8};

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
     * Goshikito (3.4.2, redone after the owner: "Z Often doesnt work and looks bad overall (redo entirely)"). Five strings out of the
     * fingers of the hand at (hx, hy, hz): while the hand is raised ({@link #GOSHIKI_UP} ticks) they hang up into the sky, swaying;
     * then they are raked over and down onto the strip that starts at the user's feet (sx, sy, sz) and runs {@code length} along (wx,
     * wz), landing on {@link #GOSHIKI_RAKE} tick, the far end of each lagging as a whip's tip does, a glowing sheet left in the air where
     * each swept; landed, they cut five long lines into the ground (glowing, then cooling) and throw up dust along them, the strings
     * drawn back into the fingers; all gone by {@code life}.
     */
    public static void goshiki(FireMesh.Sink sink, double hx, double hy, double hz, double sx, double sy, double sz, double wx, double wz,
                               double length, double age, double life, boolean coat, int seed) {
        if (age < 0 || age > life) {
            return;
        }
        double wl = Math.max(1.0E-6, Math.hypot(wx, wz));
        wx /= wl;
        wz /= wl;
        double px = -wz;
        double pz = wx;
        for (int k = 0; k < 5; k++) {
            double off = (k - 2) * GOSHIKI_SPACING;
            // where this string lands: its line on the ground, from near the feet to the strip's far end
            double fx = sx + wx * length + px * off;
            double fz = sz + wz * length + pz * off;
            double fy = sy + 0.06;
            if (age < GOSHIKI_RAKE + GOSHIKI_BACK) {
                drawRake(sink, hx, hy, hz, fx, fy, fz, wx, wz, px * off * 0.02, pz * off * 0.02, k, age, coat);
                if (age > GOSHIKI_UP - 0.5 && age < GOSHIKI_RAKE + 2) {
                    // the sheet each string sweeps through the air on its way down
                    sweep(sink, hx, hy, hz, fx, fy, fz, wx, wz, k, age, coat);
                }
            }
            if (age >= GOSHIKI_RAKE) {
                // the cut in the ground along its line, running out from the feet to the far end in 3 ticks, glowing then cooling
                double u = (age - GOSHIKI_RAKE) / (life - GOSHIKI_RAKE);
                double run = ease((age - GOSHIKI_RAKE) / 3.0);
                double glow = (1.0 - u) * (1.0 - u);
                double ax = sx + wx * 1.0 + px * off;
                double az = sz + wz * 1.0 + pz * off;
                double bx = ax + (fx - ax) * run;
                double bz = az + (fz - az) * run;
                // the groove: a dark cut in the ground, its glowing heart cooling to the dark of the cut
                int dark = FireMesh.argb(0.6, 0x24160F);
                FireMesh.quad(sink, ax - px * 0.09, sy + 0.025, az - pz * 0.09, dark, bx - px * 0.09, sy + 0.025, bz - pz * 0.09, dark, bx + px * 0.09,
                    sy + 0.025, bz + pz * 0.09, dark, ax + px * 0.09, sy + 0.025, az + pz * 0.09, dark);
                // its light laid flat on the ground (a ribbon turned to the eye reads as a hair along the ground from a low view)
                int hot = FireMesh.argb(0.9 * glow, coat ? COAT_HALO : HALO);
                int soft = FireMesh.argb(0.0, coat ? COAT_GLOW : GLOW);
                int mid = FireMesh.argb(0.35 * glow, coat ? COAT_GLOW : GLOW);
                for (int side = -1; side <= 1; side += 2) {
                    FireMesh.quad(sink, ax, sy + 0.04, az, hot, bx, sy + 0.04, bz, hot, bx + px * 0.06 * side, sy + 0.04, bz + pz * 0.06 * side, hot,
                        ax + px * 0.06 * side, sy + 0.04, az + pz * 0.06 * side, hot);
                    FireMesh.quad(sink, ax + px * 0.06 * side, sy + 0.045, az + pz * 0.06 * side, mid, bx + px * 0.06 * side, sy + 0.045,
                        bz + pz * 0.06 * side, mid, bx + px * 0.4 * side, sy + 0.045, bz + pz * 0.4 * side, soft, ax + px * 0.4 * side, sy + 0.045,
                        az + pz * 0.4 * side, soft);
                }
                line(sink, ax, sy + 0.07, az, bx, sy + 0.07, bz, 0.12, coat, glow, 0.7, 1.0, 9);
            }
        }
        if (age >= GOSHIKI_RAKE && age < GOSHIKI_RAKE + 16) {
            // dust thrown up along the five lines as they are cut, nearest first
            double u = (age - GOSHIKI_RAKE) / 16.0;
            FireMesh.palette(DUST);
            for (int i = 0; i < 5; i++) {
                double along = 2.0 + (length - 2.0) * (i + 0.5) / 5.0;
                double born = 3.0 * along / length;
                double a = (age - GOSHIKI_RAKE - born) / (16.0 - born);
                if (a < 0 || a > 1) {
                    continue;
                }
                double side = (FireMesh.rand(seed, i) - 0.5) * 4 * GOSHIKI_SPACING;
                FireMesh.puff(sink, sx + wx * along + px * side, sy + 0.3 + 0.9 * a, sz + wz * along + pz * side, 0.45 + 0.7 * a, 0, 1, 0, 0.2, 0.3,
                    0.5 - 0.3 * a, 0.3, false, 0.55 * (1.0 - a) * (1.0 - u * 0.3), age, seed + i);
            }
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /** Goshikito's beats: the hand is up by this tick, the strings land on {@link #GOSHIKI_RAKE}, and are drawn back over BACK more. */
    public static final double GOSHIKI_UP = 3;
    public static final double GOSHIKI_RAKE = 5;
    static final double GOSHIKI_BACK = 6;
    /** How far apart the five strings run (blocks; server/ItoMoves.GOSHIKI_SPREAD, the same). */
    public static final double GOSHIKI_SPACING = 0.9;

    /** How far over a Goshikito string has swung at {@code age}: 0 hanging straight up out of the hand, 1 landed on the ground. */
    static double swing(double age) {
        if (age <= GOSHIKI_UP) {
            return 0.0;
        }
        double t = clamp((age - GOSHIKI_UP) / (GOSHIKI_RAKE - GOSHIKI_UP));
        return t * t; // slow over the top, fast as it comes down
    }

    /**
     * One Goshikito string from the hand: hanging up while the hand is raised; swung over toward its line on the ground, its tip lagging;
     * landed, along to its far end; then drawn back into the fingers from its tip.
     */
    static void drawRake(FireMesh.Sink sink, double hx, double hy, double hz, double fx, double fy, double fz, double wx, double wz,
                         double ox, double oz, int k, double age, boolean coat) {
        double dx = fx - hx;
        double dy = fy - hy;
        double dz = fz - hz;
        double reach = Math.sqrt(dx * dx + dy * dy + dz * dz);
        // the landed way (to its far end), and straight up a little back with the string's own fan
        double lx = dx / reach;
        double ly = dy / reach;
        double lz = dz / reach;
        double ux = -wx * 0.18 + ox * 6;
        double uy = 1.0;
        double uz = -wz * 0.18 + oz * 6;
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul;
        uy /= ul;
        uz /= ul;
        double grow = ease(age / 2.5);
        double back = age > GOSHIKI_RAKE ? clamp((age - GOSHIKI_RAKE) / GOSHIKI_BACK) : 0.0;
        double len = reach * (0.35 + 0.65 * grow) * (1.0 - back);
        if (len < 0.2) {
            return;
        }
        begin();
        for (int j = 0; j <= 11; j++) {
            double f = j / 11.0;
            // the tip lags behind the base as it swings (a whip), and sways while hanging
            double sw = swing(age - 0.9 * f);
            double sway = age < GOSHIKI_UP ? 0.25 * f * Math.sin(age * 0.9 + k * 1.3) : 0.0;
            double ax = ux + (lx - ux) * sw;
            double ay = uy + (ly - uy) * sw;
            double az = uz + (lz - uz) * sw;
            double al = Math.sqrt(ax * ax + ay * ay + az * az);
            double r = len * f;
            point(hx + ax / al * r - wz * sway, hy + ay / al * r, hz + az / al * r + wx * sway, 0.55 + 0.45 * (1.0 - f * 0.5));
        }
        strand(sink, 0.14, coat, 1.0);
    }

    /** The glowing sheet a Goshikito string leaves in the air as it swings down (between where it was a tick ago and where it is). */
    static void sweep(FireMesh.Sink sink, double hx, double hy, double hz, double fx, double fy, double fz, double wx, double wz, int k, double age,
                      boolean coat) {
        double dx = fx - hx;
        double dy = fy - hy;
        double dz = fz - hz;
        double reach = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double lx = dx / reach;
        double ly = dy / reach;
        double lz = dz / reach;
        int rgb = coat ? COAT_GLOW : GLOW;
        int pieces = 8;
        for (int j = 0; j < pieces; j++) {
            double f0 = (j + 1.0) / pieces;
            double f1 = (double) j / pieces;
            double[] a = SW_A;
            double[] b = SW_B;
            at(a, hx, hy, hz, wx, wz, lx, ly, lz, reach, age, f0, 0);
            at(a, hx, hy, hz, wx, wz, lx, ly, lz, reach, age - 1.2, f0, 3);
            at(b, hx, hy, hz, wx, wz, lx, ly, lz, reach, age, f1, 0);
            at(b, hx, hy, hz, wx, wz, lx, ly, lz, reach, age - 1.2, f1, 3);
            double alpha = 0.18 * (1.0 - clamp((age - GOSHIKI_RAKE) / 2.0)) * (0.3 + 0.7 * f0);
            FireMesh.quad(sink, b[0], b[1], b[2], FireMesh.argb(alpha * 0.6, rgb), a[0], a[1], a[2], FireMesh.argb(alpha, rgb), a[3], a[4], a[5],
                FireMesh.argb(0.0, rgb), b[3], b[4], b[5], FireMesh.argb(0.0, rgb));
        }
    }

    static final double[] SW_A = new double[6];
    static final double[] SW_B = new double[6];

    /** A point {@code f} along a Goshikito string at {@code age} (as drawRake, without the sway), into out[o..o+2]. */
    static void at(double[] out, double hx, double hy, double hz, double wx, double wz, double lx, double ly, double lz, double reach, double age,
                   double f, int o) {
        double ux = -wx * 0.18;
        double uy = 1.0;
        double uz = -wz * 0.18;
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        double sw = swing(age - 0.9 * f);
        double ax = ux / ul + (lx - ux / ul) * sw;
        double ay = uy / ul + (ly - uy / ul) * sw;
        double az = uz / ul + (lz - uz / ul) * sw;
        double al = Math.sqrt(ax * ax + ay * ay + az * az);
        double r = reach * f;
        out[o] = hx + ax / al * r;
        out[o + 1] = hy + ay / al * r;
        out[o + 2] = hz + az / al * r;
    }

    /** The ground's dust thrown up by a cut (FireMesh.puff's palette while it is drawn): earth brown to pale. */
    public static final int[] DUST = {0x4A3A2C, 0x5E4B3A, 0x74604C, 0x8A7660, 0xA08C76, 0xB6A38E, 0xCCBAA6, 0xE0D2C0};

    static final double[] RING = new double[33 * 9 * 3];
    static final int[] RING_C = new int[33 * 9];

    /**
     * A rope of red-hot string through the {@code n} points in {@link #HX} (Overheat's): its glow (a wide soft orange ribbon), a tube
     * banded white-hot to red by how square it faces the eye, {@code r0} round at its first point and {@code r1} at its last, two bright
     * strands wound round it turning with {@code age}; {@code fade} of it there.
     */
    static void rope(FireMesh.Sink sink, int n, double r0, double r1, int[] bands, int glow, double fade, double age) {
        if (n < 2 || fade < 0.01) {
            return;
        }
        int around = 8;
        for (int j = 0; j < n; j++) {
            PX[j * 3] = HX[j * 3];
            PX[j * 3 + 1] = HX[j * 3 + 1];
            PX[j * 3 + 2] = HX[j * 3 + 2];
            double k = near(HX[j * 3], HX[j * 3 + 1], HX[j * 3 + 2], NEAR_ROPE);
            FADE[j] = k * k;
        }
        count = n;
        boolean was = round;
        round = true;
        sides();
        layer(sink, Math.max(r0, r1) * 3.4, glow, 0.3 * fade, 0.0);
        round = was;
        double[] b = T_BASIS;
        for (int j = 0; j < n; j++) {
            int p = Math.max(0, j - 1);
            int q = Math.min(n - 1, j + 1);
            FireMesh.basis(HX[q * 3] - HX[p * 3], HX[q * 3 + 1] - HX[p * 3 + 1], HX[q * 3 + 2] - HX[p * 3 + 2], b);
            double t = j / (double) (n - 1);
            double r = (r0 + (r1 - r0) * t) * near(HX[j * 3], HX[j * 3 + 1], HX[j * 3 + 2], NEAR_ROPE);
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
        // two bright strands wound round it (a rope of string), turning
        tint = HOT;
        for (int h = 0; h < 2; h++) {
            begin();
            for (int j = 0; j < n; j++) {
                int p = Math.max(0, j - 1);
                int q = Math.min(n - 1, j + 1);
                FireMesh.basis(HX[q * 3] - HX[p * 3], HX[q * 3 + 1] - HX[p * 3 + 1], HX[q * 3 + 2] - HX[p * 3 + 2], b);
                double t = j / (double) (n - 1);
                double a = t * 3.0 * n + h * Math.PI + age * 0.8;
                double r = (r0 + (r1 - r0) * t) * 1.05;
                point(HX[j * 3] + (b[0] * Math.cos(a) + b[3] * Math.sin(a)) * r, HX[j * 3 + 1] + (b[1] * Math.cos(a) + b[4] * Math.sin(a)) * r,
                    HX[j * 3 + 2] + (b[2] * Math.cos(a) + b[5] * Math.sin(a)) * r, 1.0 - 0.3 * t);
            }
            boolean r = round;
            round = true;
            strand(sink, 0.04, false, fade);
            round = r;
        }
        tint = null;
    }

    /** How far ahead of the fist Overheat's rope winds itself into a knot. */
    public static final double KNOT_AHEAD = 0.55;

    /**
     * Overheat held (3.4.2, the owner: "The charging should be one arm pointed forward and the strings gathering infront of the fist"):
     * six strings spiralling in from round the fist at (x, y, z) to a knot {@link #KNOT_AHEAD} along (fx, fy, fz), red-hot where they
     * reach it; the knot a banded white-hot ball growing with the {@code charge} (0 to 1), two strands wound round it, a heat glow.
     */
    public static void knot(FireMesh.Sink sink, double x, double y, double z, double fx, double fy, double fz, double charge, double age,
                            boolean coat, int seed) {
        knot(sink, x, y, z, fx, fy, fz, charge, age, coat, seed, 1.0);
    }

    /** As above, {@code scale} its size (one's own in first person is drawn smaller: right by the eye it would fill the view). */
    public static void knot(FireMesh.Sink sink, double x, double y, double z, double fx, double fy, double fz, double charge, double age,
                            boolean coat, int seed, double scale) {
        double fl = Math.max(1.0E-6, Math.sqrt(fx * fx + fy * fy + fz * fz));
        fx /= fl;
        fy /= fl;
        fz /= fl;
        double[] b = K_BASIS;
        FireMesh.basis(fx, fy, fz, b);
        double kx = x + fx * KNOT_AHEAD * scale;
        double ky = y + fy * KNOT_AHEAD * scale;
        double kz = z + fz * KNOT_AHEAD * scale;
        double nearK = near(kx, ky, kz, 1.2) * scale;
        double kr = (0.08 + 0.17 * charge) * nearK;
        int[] bands = coat ? COAT_HEAT : HEAT;
        double grow = ease(age / 4.0);
        tint = HOT;
        for (int k = 0; k < 6; k++) {
            double a0 = 2 * Math.PI * k / 6 + FireMesh.rand(seed, k) * 0.6;
            double reach = (0.42 + 0.25 * FireMesh.rand(seed, k + 9)) * (0.85 + 0.2 * Math.sin(age * 0.25 + k)) * nearK * grow;
            begin();
            for (int j = 0; j <= 9; j++) {
                double f = j / 9.0;
                double ang = a0 + f * 5.2 + age * 0.32;
                double r = kr * 1.05 + reach * Math.pow(1.0 - f, 1.2);
                double back = -0.45 * (1.0 - f) * nearK;
                point(kx + (b[0] * Math.cos(ang) + b[3] * Math.sin(ang)) * r + fx * back,
                    ky + (b[1] * Math.cos(ang) + b[4] * Math.sin(ang)) * r + fy * back,
                    kz + (b[2] * Math.cos(ang) + b[5] * Math.sin(ang)) * r + fz * back, 0.3 + 0.7 * f);
            }
            strand(sink, 0.07 * scale, coat, 0.95);
        }
        tint = null;
        if (kr < 0.01) {
            return;
        }
        FireMesh.shell(sink, kx, ky, kz, kr * 3.4, coat ? COAT_HEAT_GLOW : HEAT_GLOW, 0.26, 0.0);
        FireMesh.shell(sink, kx, ky, kz, kr * 1.1, bands[2], 0.85, 0.35);
        FireMesh.shell(sink, kx, ky, kz, kr * 0.75, bands[1], 0.95, 0.6);
        FireMesh.shell(sink, kx, ky, kz, kr * 0.45, bands[0], 1.0, 0.9);
        // two strands wound round the knot, turning: a ball of rope
        tint = HOT;
        for (int h = 0; h < 2; h++) {
            begin();
            double tilt = h * 1.1 + age * 0.15;
            for (int j = 0; j <= 16; j++) {
                double a = 2 * Math.PI * j / 16 + age * 0.5 * (h == 0 ? 1 : -1);
                double cx = Math.cos(a) * kr * 1.15;
                double cy = Math.sin(a) * kr * 1.15;
                double ux = b[0] * Math.cos(tilt) + fx * Math.sin(tilt);
                double uy = b[1] * Math.cos(tilt) + fy * Math.sin(tilt);
                double uz = b[2] * Math.cos(tilt) + fz * Math.sin(tilt);
                point(kx + ux * cx + b[3] * cy, ky + uy * cx + b[4] * cy, kz + uz * cx + b[5] * cy, 1.0);
            }
            ring(sink, 0.045 * scale, coat, 1.0);
        }
        tint = null;
    }

    static final double[] K_BASIS = new double[6];

    /** How long Overheat's rope takes to fall apart after its blast. */
    public static final double BREAK_LIFE = 12;

    /**
     * Overheat fired (3.4.2, the owner: "a charge beam that DOES NOT COME BACK (explodes on landing)"): a red-hot rope from the hand (hx,
     * hy, hz) along (ux, uy, uz) (unit), its head out to {@code length} by {@code flight} ticks, thicker with the {@code charge}, a
     * white-hot glare at its head; then, never drawn back, it falls apart where it lay: seven pieces flung off and dropping, cooling from
     * orange to dark red, gone in {@link #BREAK_LIFE} (the blast and its scattering strings are {@link #scatter}).
     */
    public static void beam(FireMesh.Sink sink, double hx, double hy, double hz, double ux, double uy, double uz, double length, double charge,
                            double age, double flight, boolean coat, int seed) {
        beam(sink, hx, hy, hz, ux, uy, uz, length, charge, age, flight, coat, seed, false);
    }

    /**
     * As above; {@code ribbon}: one's own beam in first person, seen from right behind the fist, drawn as a red-hot string turned to the
     * eye (a tube seen end-on shows only its red rim: a flat wedge across the view).
     */
    public static void beam(FireMesh.Sink sink, double hx, double hy, double hz, double ux, double uy, double uz, double length, double charge,
                            double age, double flight, boolean coat, int seed, boolean ribbon) {
        if (age < 0 || age > flight + BREAK_LIFE) {
            return;
        }
        if (ribbon && age <= flight) {
            double head = length * clamp(age / Math.max(1.0, flight));
            tint = HOT;
            boolean was = round;
            round = true; // its end is at the fist, out of view: no point there
            begin();
            for (int j = 0; j <= 12; j++) {
                double t = j / 12.0;
                point(hx + ux * head * t, hy + uy * head * t, hz + uz * head * t, 0.8 + 0.2 * t);
            }
            strand(sink, 0.1 + 0.06 * charge, coat, 1.0);
            round = was;
            tint = null;
            double tx = hx + ux * head;
            double ty = hy + uy * head;
            double tz = hz + uz * head;
            FireMesh.shell(sink, tx, ty, tz, 0.55 + 0.4 * charge, coat ? COAT_HEAT_GLOW : HEAT_GLOW, 0.45, 0.0);
            FireMesh.shell(sink, tx, ty, tz, 0.22 + 0.12 * charge, (coat ? COAT_HEAT : HEAT)[0], 1.0, 0.6);
            return;
        }
        int[] bands = coat ? COAT_HEAT : HEAT;
        int glow = coat ? COAT_HEAT_GLOW : HEAT_GLOW;
        double[] b = T_BASIS;
        FireMesh.basis(ux, uy, uz, b);
        double r0 = 0.12 + 0.1 * charge;
        if (age <= flight) {
            double head = length * clamp(age / Math.max(1.0, flight));
            int n = 24;
            for (int j = 0; j < n; j++) {
                double t = j / (double) (n - 1);
                double wave = 0.22 * Math.sin(t * 9.0 - age * 2.2) * t * (1.0 - t * 0.5);
                HX[j * 3] = hx + ux * head * t + b[0] * wave;
                HX[j * 3 + 1] = hy + uy * head * t + b[1] * wave;
                HX[j * 3 + 2] = hz + uz * head * t + b[2] * wave;
            }
            rope(sink, n, r0, r0 * 0.75, bands, glow, 1.0, age);
            double tx = HX[(n - 1) * 3];
            double ty = HX[(n - 1) * 3 + 1];
            double tz = HX[(n - 1) * 3 + 2];
            FireMesh.shell(sink, tx, ty, tz, 0.55 + 0.4 * charge, glow, 0.45, 0.0);
            FireMesh.shell(sink, tx, ty, tz, 0.22 + 0.12 * charge, bands[0], 1.0, 0.6);
            FireMesh.ring(sink, tx, ty, tz, ux, uy, uz, 0.35 + 0.3 * charge, 0.12, -0.2, bands[1], 0.7);
            return;
        }
        double u = (age - flight) / BREAK_LIFE;
        int[] cool = {COOLING[0], COOLING[1], COOLING[2], 0x3A0C08};
        int[] used = coat ? COAT_HEAT : u < 0.3 ? bands : cool;
        for (int k = 0; k < 7; k++) {
            double t0 = k / 7.0;
            double t1 = (k + 1) / 7.0;
            double shrink = (t1 - t0) * (1.0 - 0.55 * u) * 0.5;
            double mid = (t0 + t1) * 0.5;
            double a = FireMesh.rand(seed, k) * 2 * Math.PI;
            double fling = (0.6 + 1.6 * FireMesh.rand(seed, k + 7)) * u * (1.0 + u);
            double ox = (b[0] * Math.cos(a) + b[3] * Math.sin(a)) * fling;
            double oy = (b[1] * Math.cos(a) + b[4] * Math.sin(a)) * fling - 2.2 * u * u;
            double oz = (b[2] * Math.cos(a) + b[5] * Math.sin(a)) * fling;
            int n = 5;
            for (int j = 0; j < n; j++) {
                double t = mid + (j / (double) (n - 1) - 0.5) * 2.0 * shrink;
                double curl = 0.3 * u * Math.sin(j * 1.4 + k);
                HX[j * 3] = hx + ux * length * t + ox + b[3] * curl;
                HX[j * 3 + 1] = hy + uy * length * t + oy + b[4] * curl;
                HX[j * 3 + 2] = hz + uz * length * t + oz + b[5] * curl;
            }
            rope(sink, n, r0 * (1.0 - 0.4 * u), r0 * 0.5 * (1.0 - 0.4 * u), used, glow, (1.0 - u) * (1.0 - u * 0.5), age + k);
        }
    }

    /** How long a burst of strings flies. */
    public static final double SCATTER_LIFE = 18;

    /**
     * Strings bursting out of (x, y, z) in every direction, {@code size} far (3.4.2: Overheat's explosion, the owner: "when it ends the
     * strings scatter into every direction"; the Black Knight bursting, "it explodes into tons of strings"; the Birdcage shut): a flash,
     * a shock shell, and eighteen strings (twelve {@code lite}) flung out, curling, slowing and falling; red-hot and cooling over a banded
     * fireball when {@code heat}, white (violet coated) otherwise; gone in {@link #SCATTER_LIFE}.
     */
    public static void scatter(FireMesh.Sink sink, double x, double y, double z, double size, boolean heat, double age, boolean coat, int seed,
                               boolean lite) {
        if (age < 0 || age > SCATTER_LIFE) {
            return;
        }
        double t = age / SCATTER_LIFE;
        int[] bands = coat ? COAT_HEAT : HEAT;
        if (age < 6) {
            double f = 1.0 - age / 6.0;
            FireMesh.shell(sink, x, y, z, size * (0.16 + 0.1 * age / 6.0), heat ? bands[0] : CORE, 0.9 * f * f, 0.3 * f);
            if (!heat) {
                FireMesh.shell(sink, x, y, z, size * (0.3 + 0.15 * age / 6.0), coat ? COAT_GLOW : GLOW, 0.45 * f, 0.0);
            }
        }
        if (heat) {
            // the blast: banded fire as the Magma fruit's bursts are (FireMesh's fire), billowing out and up, over a heat glow
            double u = clamp(age / 14.0);
            FireMesh.shell(sink, x, y, z, size * (0.35 + 0.25 * ease(u * 2.0)), coat ? COAT_HEAT_GLOW : HEAT_GLOW, 0.3 * (1.0 - u), 0.0);
            if (u < 1.0) {
                for (int i = 0; i < 4; i++) {
                    double a = Math.PI * 2.0 * (i + FireMesh.rand(seed, i + 200)) / 4;
                    double out = size * 0.16 * ease(u * 1.6);
                    FireMesh.puff(sink, x + Math.cos(a) * out, y + size * 0.1 * u, z + Math.sin(a) * out, size * (0.2 + 0.08 * u) * (1.0 - 0.35 * u),
                        Math.cos(a), 0.6, Math.sin(a), 0.8, 0.35, 0.9 - 0.45 * u, 0.6, true, 1.0 - u * u, age * 2.0, seed + i);
                }
            }
        }
        FireMesh.shell(sink, x, y, z, size * (0.25 + 0.35 * ease(t * 2.0)), heat ? (coat ? COAT_HEAT_GLOW : HEAT_GLOW) : coat ? COAT_GLOW : GLOW, 0.0,
            0.16 * (1.0 - t) * (1.0 - t));
        int n = lite ? 12 : 18;
        for (int i = 0; i < n; i++) {
            double yy = 1.0 - 2.0 * (i + 0.5) / n;
            double rr = Math.sqrt(Math.max(0.0, 1.0 - yy * yy));
            double th = i * 2.39996 + seed * 0.37;
            double dx = Math.cos(th) * rr + (FireMesh.rand(seed, i) - 0.5) * 0.3;
            double dy = yy * 0.85 + 0.15;
            double dz = Math.sin(th) * rr + (FireMesh.rand(seed, i + 40) - 0.5) * 0.3;
            double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= dl;
            dy /= dl;
            dz /= dl;
            double far = size * ease(Math.min(1.0, t * 1.4)) * (0.6 + 0.4 * FireMesh.rand(seed, i + 80));
            double len = size * (0.2 + 0.14 * FireMesh.rand(seed, i + 120)) * (1.0 - 0.45 * t);
            // a curl square to its way
            double qx = dz;
            double qz = -dx;
            double ql = Math.max(1.0E-6, Math.hypot(qx, qz));
            qx /= ql;
            qz /= ql;
            double curl = len * 0.4 * (FireMesh.rand(seed, i + 160) - 0.5) * 2.0 * (0.4 + t);
            begin();
            for (int j = 0; j <= 5; j++) {
                double f = j / 5.0;
                double d = Math.max(0.0, far - len * (1.0 - f));
                double bend = curl * Math.sin(Math.PI * f);
                point(x + dx * d + qx * bend, y + dy * d - 0.9 * t * t * size * 0.3, z + dz * d + qz * bend, 0.4 + 0.6 * f);
            }
            tint = heat ? (t < 0.35 ? HOT : COOLING) : BRIGHT;
            strand(sink, heat ? 0.12 : 0.1, coat, 1.0 - t * t);
            tint = null;
        }
    }

    /**
     * The Birdcage held (3.4.2, the owner: "the user points at the sky and shoots a stedy stream of strings that spread into the cage
     * when it reaches the right height"): six strings twisting up together from the raised hand (hx, hy, hz) to the cage's crown (cx,
     * cy, cz) (as high as the cage is wide: its {@code radius} grows with the hold), bright beads running up them, a glow at the crown
     * and the first strings fanning out down the dome from it, showing how big the cage will be.
     */
    public static void stream(FireMesh.Sink sink, double hx, double hy, double hz, double cx, double cy, double cz, double radius, double age,
                              boolean coat, int seed) {
        double grow = ease(age / 4.0);
        double tx = hx + (cx - hx) * grow;
        double ty = hy + (cy - hy) * grow;
        double tz = hz + (cz - hz) * grow;
        for (int k = 0; k < 6; k++) {
            begin();
            for (int j = 0; j <= 14; j++) {
                double f = j / 14.0;
                double ang = 2 * Math.PI * k / 6 + f * 9.0 + age * 0.6;
                double r = 0.1 + 0.22 * f;
                point(hx + (tx - hx) * f + Math.cos(ang) * r, hy + (ty - hy) * f, hz + (tz - hz) * f + Math.sin(ang) * r, 0.7 + 0.3 * f);
            }
            // thick enough to read as a stream from across the field (the string's own white over a strong pink glow)
            tint = coat ? null : BRIGHT;
            strand(sink, 0.1 + 0.004 * Math.abs(ty - hy), coat, 1.0);
            tint = null;
        }
        // beads of light running up the stream
        for (int k = 0; k < 3; k++) {
            double f = (age * 0.07 + k / 3.0) % 1.0;
            FireMesh.shell(sink, hx + (tx - hx) * f, hy + (ty - hy) * f, hz + (tz - hz) * f, 0.22, coat ? COAT_HALO : CORE, 0.8, 0.2);
        }
        if (grow < 0.99) {
            return;
        }
        FireMesh.shell(sink, cx, cy, cz, 1.0 + radius * 0.03, coat ? COAT_GLOW : GLOW, 0.4, 0.0);
        FireMesh.shell(sink, cx, cy, cz, 0.35, coat ? COAT_HALO : CORE, 0.9, 0.3);
        // the dome's first strings, fanning down from the crown (its middle is radius below it, on the ground)
        double gy = cy - radius;
        for (int k = 0; k < 12; k++) {
            double a = 2 * Math.PI * k / 12 + 0.05 + age * 0.01;
            begin();
            for (int j = 0; j <= 5; j++) {
                double e = Math.PI / 2 - 0.42 * j / 5.0 * (0.85 + 0.15 * Math.sin(age * 0.3 + k));
                point(cx + Math.cos(a) * radius * Math.cos(e), gy + radius * Math.sin(e), cz + Math.sin(a) * radius * Math.cos(e), 1.0 - 0.4 * j / 5.0);
            }
            strand(sink, 0.07 + radius * 0.002, coat, 1.0);
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
     * 3.4.2: the strings a Black Knight is woven of, over its body (its feet at (x, y, z), {@code height} tall): five slim strings wound
     * round it from its feet to its shoulders, slowly turning, faint enough that the clone's own skin shows through.
     */
    public static void woven(FireMesh.Sink sink, double x, double y, double z, double height, double time, boolean coat, int seed) {
        for (int k = 0; k < 5; k++) {
            begin();
            double a0 = 2 * Math.PI * k / 5 + time * 0.05 * (k % 2 == 0 ? 1 : -1) + seed;
            for (int j = 0; j <= 11; j++) {
                double f = j / 11.0;
                double a = a0 + f * Math.PI * 2.4;
                double r = 0.36 + 0.06 * Math.sin(f * Math.PI * 3 + k);
                point(x + Math.cos(a) * r, y + 0.1 + f * height * 0.78, z + Math.sin(a) * r, 0.45 + 0.35 * Math.sin(Math.PI * f));
            }
            strand(sink, 0.03, coat, 0.7);
        }
    }

    /**
     * The Birdcage: its middle on the ground (x, y, z), {@code radius} now: {@link #CAGE_BARS} strings from its crown down to the ground
     * all round, come down from the crown as far as {@code spread} (0 to 1: 3.4.2, let go they spread from the crown the stream reached
     * down to the ground), a ring of light where they meet the ground; {@code tension} (0 to 1) while it closes: the strings pulled taut,
     * brighter and thicker, quivering; {@code alpha} of it there.
     */
    public static void cage(FireMesh.Sink sink, double x, double y, double z, double radius, double spread, double tension, double alpha,
                            boolean coat) {
        double dist = Math.sqrt(x * x + y * y + z * z);
        // fewer pieces a bar when the cage is small on the screen
        int pieces = dist - radius > 60 ? 5 : 8;
        double width = (0.08 + radius * 0.003) * (1.0 + 0.6 * tension);
        double low = Math.PI / 2 * (1.0 - clamp(spread));
        for (int k = 0; k < CAGE_BARS; k++) {
            double a = 2 * Math.PI * k / CAGE_BARS + 0.05;
            double ca = Math.cos(a);
            double sa = Math.sin(a);
            double quiver = tension * 0.012 * radius * Math.sin(k * 2.3 + tension * 40.0);
            begin();
            for (int j = 0; j <= pieces; j++) {
                // down from the crown (elevation 90 degrees) as far as it has spread
                double e = Math.PI / 2 - (Math.PI / 2 - low) * j / pieces;
                double rr = radius * Math.cos(e) + quiver * Math.sin(Math.PI * j / pieces);
                point(x + ca * rr, y + radius * Math.sin(e), z + sa * rr, 0.75 + 0.25 * Math.sin(e) + 0.25 * tension);
            }
            strand(sink, width, coat, alpha);
        }
        // two rings round it: where the bars meet the ground, and a third of the way up
        for (int ring = 0; ring < 2; ring++) {
            double e = ring == 0 ? 0.02 : 0.6;
            if (e < low) {
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
        if (spread < 1.0) {
            // the crown the stream reached, still glowing as the strings spread from it
            FireMesh.shell(sink, x, y + radius, z, 1.0 + radius * 0.03, coat ? COAT_GLOW : GLOW, 0.4 * alpha * (1.0 - spread), 0.0);
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
            // strings running over it, every third piece of the round (3.4.2: every other was the costliest thing the fruit drew)
            for (int i = 0; i < seg; i += 3) {
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
            if (k % 2 == 0) {
                // 3.4.2: the glow and glint on every other tip (they all meet at one point: sixteen balls of light cost more than
                // the whole Birdcage)
                FireMesh.shell(sink, hx, hy, hz, 0.55, COAT_GLOW, 0.5 * fade, 0.0);
                FireMesh.shell(sink, hx, hy, hz, 0.14, 0xE8D4FF, 0.8 * fade, 0.2);
            }
        }
    }

    /**
     * Sky Road (3.4.3, the owner: "Extremely thin, semi-transparent white strings shoot upward from the player's fingertips, extending
     * toward invisible anchor points high above them ... They must remain visually thin and elegant, never looking like giant ropes or
     * bulky grappling hooks ... occasional tiny flashes as strings tighten"): one string from the hand (ax, ay, az) up to (bx, by, bz) (its
     * anchor, or as far as it has been thrown), thin, fading toward the sky; {@code taut} (0 to 1) brightens it as it tightens, a tiny
     * flash at the hand and, once {@code caught}, where it holds in the sky.
     */
    public static void sky(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double alpha, double taut,
                           boolean caught, boolean coat) {
        if (alpha < 0.01) {
            return;
        }
        line(sink, ax, ay, az, bx, by, bz, 0.022 + 0.01 * taut, coat, Math.min(1.0, alpha), 0.9, 0.2, 10);
        if (taut > 0.01) {
            FireMesh.shell(sink, ax, ay, az, 0.12 + 0.08 * taut, coat ? COAT_HALO : CORE, 0.7 * taut, 0.2);
            if (caught) {
                FireMesh.shell(sink, bx, by, bz, 0.35 + 0.25 * taut, coat ? COAT_GLOW : GLOW, 0.5 * taut, 0.0);
                FireMesh.shell(sink, bx, by, bz, 0.12, coat ? COAT_HALO : CORE, 0.9 * taut, 0.3);
            }
        }
    }

    /**
     * Sky Road's faint streak behind the feet: through the feet's last places {@code feet} (world: x, y, z each, the newest first),
     * {@code count} of them, less of it the slower ({@code speed} 0 to 1), fading toward its tail.
     */
    public static void streak(FireMesh.Sink sink, double[] feet, int count, double cx, double cy, double cz, double speed, boolean coat) {
        begin();
        for (int i = 0; i < count; i++) {
            point(feet[i * 3] - cx, feet[i * 3 + 1] - cy, feet[i * 3 + 2] - cz, 1.0 - (double) i / count);
        }
        strand(sink, 0.035, coat, 0.35 * speed);
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
