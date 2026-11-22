package com.hexhaki.piece.gura;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.1.1: the Gura Gura no Mi's quakes in 3D, as Whitebeard's and Blackbeard's look in the references (docs/handoff/gura-ref/sheets):
 * the air cracked like glass (a web of jagged white lines in a cyan glow, radial cracks joined by broken rings, there in a flash and
 * shattering away), the quake bubble round the fist (a see-through white sphere with a bright rim, arcs and ripples round it), the
 * shock racing out ahead (white rings and curved shock lines, streaks), the ground struck (cracks glowing then cooling dark, rings
 * pressed into it, white shock lines thrown up, rocks and a dust ring), the rift, the island shaken and the tsunami (a curling wall of
 * water in flat cel bands, foam on its crest, spray off its lip). Pure: every point relative to the eye (the camera at the origin),
 * every quad into a {@link FireMesh.Sink}, so tools/preview/FxPreview renders it off-game exactly as the game draws it. Everything is
 * alpha blended and depth tested (client/gura/GuraFx: the game's debugQuads), never additive. No allocation per frame.
 */
public final class GuraArt {
    private GuraArt() {
    }

    /** The cracks' white core, their cyan glow; the bubble's white; the shock's white. */
    public static final int CORE = 0xFFFFFF;
    public static final int GLOW = 0x7FE6FF;
    public static final int AIR = 0xEAF8FF;
    public static final int SHOCK = 0xF2FAFF;
    /** The ground's cracks once cooled, its rocks. */
    public static final int DARK = 0x1E1814;
    public static final int ROCK = 0x6E6258;

    static double clamp(double v) {
        return v < 0.0 ? 0.0 : v > 1.0 ? 1.0 : v;
    }

    static double ease(double t) {
        double u = clamp(t);
        return 1.0 - (1.0 - u) * (1.0 - u);
    }

    // ------------------------------------------------------------------------------------------------------ lines

    static final double[] B = new double[6];
    static final double[] B2 = new double[6];

    /**
     * A line from A to B facing the eye, {@code width} across: a hard core in {@code rgb} at {@code alpha} ({@code soft}: its edges clear,
     * a glow). Its width axis is square to the line and to the way to its middle (the eye at the origin), so it never shows edge on.
     */
    static void line(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double width, int rgb, double alpha,
                     boolean soft) {
        if (alpha < 0.004 || width <= 0.0) {
            return;
        }
        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        double mx = (ax + bx) * 0.5;
        double my = (ay + by) * 0.5;
        double mz = (az + bz) * 0.5;
        double wx = dy * mz - dz * my;
        double wy = dz * mx - dx * mz;
        double wz = dx * my - dy * mx;
        double wl = Math.sqrt(wx * wx + wy * wy + wz * wz);
        if (wl < 1.0E-9) {
            return;
        }
        double h = width * 0.5 / wl;
        wx *= h;
        wy *= h;
        wz *= h;
        int c = FireMesh.argb(alpha, rgb);
        if (!soft) {
            FireMesh.quad(sink, ax - wx, ay - wy, az - wz, c, ax + wx, ay + wy, az + wz, c, bx + wx, by + wy, bz + wz, c, bx - wx, by - wy, bz - wz, c);
            return;
        }
        int clear = FireMesh.argb(0.0, rgb);
        FireMesh.quad(sink, ax, ay, az, c, ax + wx, ay + wy, az + wz, clear, bx + wx, by + wy, bz + wz, clear, bx, by, bz, c);
        FireMesh.quad(sink, ax - wx, ay - wy, az - wz, clear, ax, ay, az, c, bx, by, bz, c, bx - wx, by - wy, bz - wz, clear);
    }

    /** A line lying flat on the ground (its width across it, level), {@code lift} over the ground's {@code y}. */
    static void flat(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double width, int rgb, double alpha,
                     boolean soft) {
        if (alpha < 0.004 || width <= 0.0) {
            return;
        }
        double dx = bx - ax;
        double dz = bz - az;
        double l = Math.sqrt(dx * dx + dz * dz);
        if (l < 1.0E-9) {
            return;
        }
        double wx = -dz / l * width * 0.5;
        double wz = dx / l * width * 0.5;
        int c = FireMesh.argb(alpha, rgb);
        if (!soft) {
            FireMesh.quad(sink, ax - wx, ay, az - wz, c, ax + wx, ay, az + wz, c, bx + wx, by, bz + wz, c, bx - wx, by, bz - wz, c);
            return;
        }
        int clear = FireMesh.argb(0.0, rgb);
        FireMesh.quad(sink, ax, ay, az, c, ax + wx, ay, az + wz, clear, bx + wx, by, bz + wz, clear, bx, by, bz, c);
        FireMesh.quad(sink, ax - wx, ay, az - wz, clear, ax, ay, az, c, bx, by, bz, c, bx - wx, by, bz - wz, clear);
    }

    // ------------------------------------------------------------------------------------------------------ the air cracked like glass

    /**
     * How long the air stays cracked (ticks). 3.1.2 (the owner: "it's too large and it's like spawning instantly. Like it should crack
     * slowly, and the lines of the crack should be way slimmer. I mean, it should actually be like a glass"): its cracks run out over
     * {@link #CRACK_GROW} ticks in little jumps, as glass gives, hold, and it shatters away from {@link #CRACK_HOLD}.
     */
    public static final double CRACK_LIFE = 44.0;
    public static final double CRACK_HOLD = 26.0;
    public static final double CRACK_GROW = 12.0;
    static final int SPOKES = 11;
    static final int STEPS = 6;
    static final double[] RINGS = {0.3, 0.55, 0.8};
    /** One web's spokes: [spoke][step] (angle, radius fraction) worked out for its seed; each spoke's start and how long it runs. */
    static final double[] SPOKE_A = new double[SPOKES * (STEPS + 1)];
    static final double[] SPOKE_R = new double[SPOKES * (STEPS + 1)];
    static final double[] REACH = new double[SPOKES];

    static void web(int seed, double age) {
        for (int i = 0; i < SPOKES; i++) {
            double a0 = Math.PI * 2.0 * (i + 0.45 * (FireMesh.rand(seed, i) - 0.5)) / SPOKES;
            double length = 0.65 + 0.35 * FireMesh.rand(seed, 40 + i);
            double a = a0;
            for (int k = 0; k <= STEPS; k++) {
                if (k > 0) {
                    a += 0.26 * (FireMesh.rand(seed, 100 + i * 13 + k) - 0.5);
                }
                SPOKE_A[i * (STEPS + 1) + k] = a;
                SPOKE_R[i * (STEPS + 1) + k] = length * k / STEPS * (k == 0 ? 0.0 : 0.85 + 0.3 * FireMesh.rand(seed, 200 + i * 7 + k));
            }
            // each crack starts on its own (up to 3 ticks late) and runs out in little jumps, a pause between each, as glass gives
            double start = 3.0 * FireMesh.rand(seed, 60 + i);
            double run = CRACK_GROW - 3.0 - 2.0 * FireMesh.rand(seed, 70 + i);
            double t = clamp((age - start) / run) * 8.0;
            double hop = Math.floor(t);
            REACH[i] = Math.min(1.0, (hop + ease((t - hop) * 3.0)) / 8.0);
        }
    }

    /** Where a spoke reaches {@code fraction} of the web's radius: its angle there (the web's lines join the spokes on its rings). */
    static double spokeAngle(int i, double fraction) {
        for (int k = 1; k <= STEPS; k++) {
            double r0 = SPOKE_R[i * (STEPS + 1) + k - 1];
            double r1 = SPOKE_R[i * (STEPS + 1) + k];
            if (fraction <= r1 || k == STEPS) {
                double t = r1 - r0 < 1.0E-6 ? 0.0 : clamp((fraction - r0) / (r1 - r0));
                return SPOKE_A[i * (STEPS + 1) + k - 1] + (SPOKE_A[i * (STEPS + 1) + k] - SPOKE_A[i * (STEPS + 1) + k - 1]) * t;
            }
        }
        return SPOKE_A[i * (STEPS + 1)];
    }

    /**
     * The air cracked like glass at ({@code x}, {@code y}, {@code z}) (refs 06, 15, 19, 39, 47): a web {@code radius} across in the
     * plane square to ({@code nx}, {@code ny}, {@code nz}) (unit: the way the fist struck), {@code age} ticks old. A small flash where it
     * struck; its cracks run out from there one by one in little jumps over {@link #CRACK_GROW} ticks, side cracks branching off them,
     * the broken rings closing between them as they pass; hair-thin white lines with a faint cyan edge; from {@link #CRACK_HOLD} it
     * breaks up: the pieces drift apart and fade and shards of it fall away, gone by {@link #CRACK_LIFE}.
     */
    public static void crack(FireMesh.Sink sink, double x, double y, double z, double nx, double ny, double nz, double radius, double age,
                             int seed) {
        if (age < 0.0 || age > CRACK_LIFE) {
            return;
        }
        FireMesh.basis(nx, ny, nz, B);
        web(seed, age);
        double broken = clamp((age - CRACK_HOLD) / (CRACK_LIFE - CRACK_HOLD - 2.0));
        double alpha = 1.0 - broken * broken;
        double core = Math.max(0.012, radius * 0.011);
        double glow = core * 3.2;
        // a small flash where the fist struck
        if (age < 3.0) {
            double f = 1.0 - age / 3.0;
            FireMesh.shell(sink, x, y, z, radius * 0.1, CORE, 0.75 * f, 0.0);
            FireMesh.shell(sink, x, y, z, radius * 0.25, GLOW, 0.2 * f, 0.0);
        }
        double drift = broken * radius * 0.25;
        double push = broken * radius * 0.2;
        // the cracks, and the side cracks off them
        for (int i = 0; i < SPOKES; i++) {
            double reach = REACH[i];
            for (int k = 0; k < STEPS; k++) {
                int j = i * (STEPS + 1) + k;
                double r0 = SPOKE_R[j];
                double r1 = SPOKE_R[j + 1];
                if (r0 >= reach) {
                    break;
                }
                double t = r1 > reach ? (reach - r0) / Math.max(1.0E-6, r1 - r0) : 1.0;
                double a0 = SPOKE_A[j];
                double a1 = SPOKE_A[j] + (SPOKE_A[j + 1] - SPOKE_A[j]) * t;
                double e1 = r0 + (r1 - r0) * t;
                double out = drift * (0.5 + r0);
                double p = push * (0.4 + 0.6 * FireMesh.rand(seed, 300 + j));
                segment(sink, x, y, z, radius, a0, r0, a1, e1, out, p, core * (1.0 - 0.4 * r0), glow, alpha * (1.0 - 0.3 * r0));
                if ((k == 2 || k == 4) && t >= 1.0 && FireMesh.rand(seed, 400 + j) < 0.7) {
                    double side = FireMesh.rand(seed, 420 + j) < 0.5 ? 1.0 : -1.0;
                    double grown = clamp((reach - r1) / 0.12);
                    double ab = a1 + side * (0.35 + 0.3 * FireMesh.rand(seed, 440 + j));
                    double eb = r1 + (0.1 + 0.12 * FireMesh.rand(seed, 460 + j)) * grown;
                    segment(sink, x, y, z, radius, a1, r1, ab, eb, out, p, core * 0.6, glow * 0.7, alpha * 0.8);
                }
            }
        }
        // the broken rings closing between the cracks as they pass (a third of each ring's pieces missing)
        for (int ring = 0; ring < RINGS.length; ring++) {
            double f = RINGS[ring];
            for (int i = 0; i < SPOKES; i++) {
                if (FireMesh.rand(seed, 500 + ring * 31 + i) < 0.32) {
                    continue;
                }
                int n = (i + 1) % SPOKES;
                double closed = clamp((Math.min(REACH[i], REACH[n]) - f) / 0.15);
                if (closed <= 0.0) {
                    continue;
                }
                double fa = f * (0.92 + 0.16 * FireMesh.rand(seed, 600 + ring * 31 + i));
                double fb = f * (0.92 + 0.16 * FireMesh.rand(seed, 600 + ring * 31 + n));
                double aa = spokeAngle(i, fa);
                double ab = spokeAngle(n, fb);
                if (ab < aa) {
                    ab += Math.PI * 2.0;
                }
                // a kink in the middle, as glass breaks; drawn from one crack across to the next
                double am = (aa + ab) * 0.5;
                double fm = (fa + fb) * 0.5 * (0.9 + 0.12 * FireMesh.rand(seed, 700 + ring * 31 + i));
                double out = drift * (0.5 + f);
                double p = push * (0.4 + 0.6 * FireMesh.rand(seed, 800 + ring * 31 + i));
                double first = Math.min(1.0, closed * 2.0);
                double second = Math.max(0.0, closed * 2.0 - 1.0);
                double a = alpha * (0.9 - 0.3 * f);
                segment(sink, x, y, z, radius, aa, fa, aa + (am - aa) * first, fa + (fm - fa) * first, out, p, core * 0.75, glow * 0.8, a);
                if (second > 0.0) {
                    segment(sink, x, y, z, radius, am, fm, am + (ab - am) * second, fm + (fb - fm) * second, out, p, core * 0.75, glow * 0.8, a);
                }
            }
        }
        // shards of it falling away as it breaks
        if (broken > 0.0) {
            for (int i = 0; i < 14; i++) {
                double a = Math.PI * 2.0 * FireMesh.rand(seed, 900 + i);
                double r = radius * (0.2 + 0.7 * FireMesh.rand(seed, 920 + i)) * (1.0 + broken * 0.8);
                double along = broken * radius * (0.2 + 0.5 * FireMesh.rand(seed, 940 + i));
                double fall = broken * broken * radius * 1.2;
                double ox = B[0] * Math.cos(a) * r + B[3] * Math.sin(a) * r + nx * along;
                double oy = B[1] * Math.cos(a) * r + B[4] * Math.sin(a) * r + ny * along - fall;
                double oz = B[2] * Math.cos(a) * r + B[5] * Math.sin(a) * r + nz * along;
                FireMesh.shard(sink, x + ox, y + oy, z + oz, radius * (0.03 + 0.03 * FireMesh.rand(seed, 960 + i)), broken * 7.0, seed + i,
                    0xE6FAFF, 0.8 * (1.0 - broken));
            }
        }
    }

    /** One piece of the web from (angle, radius fraction) a to b, pushed out by {@code out} in its plane and {@code push} along it. */
    static void segment(FireMesh.Sink sink, double x, double y, double z, double radius, double a0, double r0, double a1, double r1, double out,
                        double push, double core, double glow, double alpha) {
        double ca = Math.cos(a0);
        double sa = Math.sin(a0);
        double cb = Math.cos(a1);
        double sb = Math.sin(a1);
        double am = (a0 + a1) * 0.5;
        double ox = (B[0] * Math.cos(am) + B[3] * Math.sin(am)) * out;
        double oy = (B[1] * Math.cos(am) + B[4] * Math.sin(am)) * out;
        double oz = (B[2] * Math.cos(am) + B[5] * Math.sin(am)) * out;
        double nx = B[1] * B[5] - B[2] * B[4];
        double ny = B[2] * B[3] - B[0] * B[5];
        double nz = B[0] * B[4] - B[1] * B[3];
        ox += nx * push;
        oy += ny * push;
        oz += nz * push;
        double ax = x + ox + (B[0] * ca + B[3] * sa) * r0 * radius;
        double ay = y + oy + (B[1] * ca + B[4] * sa) * r0 * radius;
        double az = z + oz + (B[2] * ca + B[5] * sa) * r0 * radius;
        double bx = x + ox + (B[0] * cb + B[3] * sb) * r1 * radius;
        double by = y + oy + (B[1] * cb + B[4] * sb) * r1 * radius;
        double bz = z + oz + (B[2] * cb + B[5] * sb) * r1 * radius;
        line(sink, ax, ay, az, bx, by, bz, glow, GLOW, 0.35 * alpha, true);
        line(sink, ax, ay, az, bx, by, bz, core, CORE, alpha, false);
    }

    // ------------------------------------------------------------------------------------------------------ the quake bubble

    /**
     * The quake bubble round the fist at ({@code x}, {@code y}, {@code z}) (refs 00, 01, 18, 24, 53), {@code radius} across, {@code age}
     * ticks old: a see-through white sphere with a bright rim and a white heart, trembling, arcs of white racing round it, ripples
     * thrown off it, little cracks flickering at its skin.
     */
    public static void bubble(FireMesh.Sink sink, double x, double y, double z, double radius, double age, int seed) {
        if (radius < 0.02) {
            return;
        }
        double r = radius * (1.0 + 0.05 * Math.sin(age * 2.6) + 0.03 * Math.sin(age * 7.3));
        FireMesh.shell(sink, x, y, z, r * 1.9, 0xBFEFFF, 0.1, 0.0);
        FireMesh.shell(sink, x, y, z, r, AIR, 0.08, 0.85);
        FireMesh.shell(sink, x, y, z, r * 0.55, CORE, 0.55, 0.0);
        for (int k = 0; k < 2; k++) {
            double u = ((age + k * 3.5) % 7.0) / 7.0;
            FireMesh.shell(sink, x, y, z, r * (1.0 + 0.9 * u), AIR, 0.0, 0.5 * (1.0 - u));
        }
        for (int i = 0; i < 3; i++) {
            double ax = FireMesh.rand(seed, i * 3) - 0.5;
            double ay = FireMesh.rand(seed, i * 3 + 1) - 0.5;
            double az = FireMesh.rand(seed, i * 3 + 2) - 0.5;
            double al = Math.max(1.0E-6, Math.sqrt(ax * ax + ay * ay + az * az));
            FireMesh.arc(sink, x, y, z, ax / al, ay / al, az / al, r * 1.12, r * 0.07, age * 0.55 + i * 2.1, 1.9, 0.0, CORE, 0.95);
        }
        // little cracks at its skin, a new few every other tick
        int beat = (int) (age / 2.0);
        double core = Math.max(0.012, r * 0.04);
        for (int i = 0; i < 5; i++) {
            int s = seed * 31 + beat * 7 + i;
            double th = Math.PI * 2.0 * FireMesh.rand(s, 1);
            double ph = Math.acos(2.0 * FireMesh.rand(s, 2) - 1.0);
            double dx = Math.sin(ph) * Math.cos(th);
            double dy = Math.cos(ph);
            double dz = Math.sin(ph) * Math.sin(th);
            double l0 = r * 1.0;
            double lm = r * (1.25 + 0.2 * FireMesh.rand(s, 3));
            double l1 = r * (1.5 + 0.4 * FireMesh.rand(s, 4));
            double kx = (FireMesh.rand(s, 5) - 0.5) * r * 0.3;
            double ky = (FireMesh.rand(s, 6) - 0.5) * r * 0.3;
            double kz = (FireMesh.rand(s, 7) - 0.5) * r * 0.3;
            line(sink, x + dx * l0, y + dy * l0, z + dz * l0, x + dx * lm + kx, y + dy * lm + ky, z + dz * lm + kz, core * 5.0, GLOW, 0.4, true);
            line(sink, x + dx * l0, y + dy * l0, z + dz * l0, x + dx * lm + kx, y + dy * lm + ky, z + dz * lm + kz, core, CORE, 0.9, false);
            line(sink, x + dx * lm + kx, y + dy * lm + ky, z + dz * lm + kz, x + dx * l1, y + dy * l1, z + dz * l1, core * 0.8, CORE, 0.7, false);
        }
    }

    // ------------------------------------------------------------------------------------------------------ the shock

    /** How long a shock of {@code reach} blocks lasts: it goes out at {@code speed} blocks a tick and fades in 5 more ticks. */
    public static double shockLife(double reach, double speed) {
        return reach / speed + 5.0;
    }

    /**
     * A punch's shock (refs 02, 04, 07, 52) out of ({@code x}, {@code y}, {@code z}) along ({@code ux}, {@code uy}, {@code uz}) (unit) at
     * {@code speed} blocks a tick to {@code reach}, opening {@code half} radians: a white ring at its front and one behind it, curved
     * white shock lines leaning back off it, white streaks racing along with it; {@code strength} (0 to 1) makes it bolder.
     */
    public static void shock(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double reach, double half,
                             double strength, double speed, double age, int seed) {
        if (age < 0.0 || age > shockLife(reach, speed)) {
            return;
        }
        double d = Math.min(reach, age * speed);
        double fade = (1.0 - 0.55 * d / reach) * clamp((shockLife(reach, speed) - age) / 5.0);
        double tan = Math.tan(half);
        // 3.1.3 (the owner, with a screenshot of two white arcs up in the sky bowed the wrong way: "the rings around the impact are like
        // opposite to where the impact was"): the drawn front never wider than 1.5 + 0.16 blocks a block out (a 40-block shock's was a
        // 21-block ring in the sky), and its arcs lean a little ahead, never back toward the eye (leaning back, their ends came so much
        // nearer the eye than their middles that they bowed the other way)
        double rho = Math.min(0.35 + d * tan, 1.5 + d * 0.16);
        double fx = x + ux * d;
        double fy = y + uy * d;
        double fz = z + uz * d;
        double bold = (0.6 + 0.4 * strength) * clamp(d / 1.5);
        FireMesh.ring(sink, fx, fy, fz, ux, uy, uz, rho, 0.2 + rho * 0.1, rho * 0.08, SHOCK, 0.9 * fade * bold);
        if (d > 2.0) {
            double d2 = d * 0.72;
            double r2 = Math.min(0.35 + d2 * tan, 1.5 + d2 * 0.16);
            FireMesh.ring(sink, x + ux * d2, y + uy * d2, z + uz * d2, ux, uy, uz, r2, 0.15 + r2 * 0.08, r2 * 0.06, SHOCK, 0.45 * fade * bold);
        }
        // a swell of air at its front (the bold ones)
        if (strength > 0.45) {
            FireMesh.shell(sink, fx - ux * rho * 0.3, fy - uy * rho * 0.3, fz - uz * rho * 0.3, rho * 0.85, AIR, 0.0, 0.3 * fade);
        }
        for (int i = 0; i < 9; i++) {
            double lag = i * 0.4;
            double ri = rho * (0.8 + 0.35 * FireMesh.rand(seed, i));
            double start = Math.PI * 2.0 * FireMesh.rand(seed, 10 + i) + age * 0.05;
            double sweep = 0.6 + 1.0 * FireMesh.rand(seed, 20 + i);
            FireMesh.arc(sink, fx - ux * lag, fy - uy * lag, fz - uz * lag, ux, uy, uz, ri, 0.08 + ri * 0.07, start, sweep, ri * 0.12, SHOCK,
                0.95 * fade * bold);
        }
        FireMesh.basis(ux, uy, uz, B2);
        for (int i = 0; i < 14; i++) {
            double a = Math.PI * 2.0 * FireMesh.rand(seed, 40 + i);
            double off = tan * (0.25 + 0.75 * FireMesh.rand(seed, 50 + i));
            double vx = ux + (B2[0] * Math.cos(a) + B2[3] * Math.sin(a)) * off;
            double vy = uy + (B2[1] * Math.cos(a) + B2[4] * Math.sin(a)) * off;
            double vz = uz + (B2[2] * Math.cos(a) + B2[5] * Math.sin(a)) * off;
            double vl = Math.sqrt(vx * vx + vy * vy + vz * vz);
            double along = d * (0.55 + 0.45 * FireMesh.rand(seed, 60 + i));
            if (along < 4.0) {
                continue; // never a blade of it across the eye (first person: it leaves one's own fist)
            }
            FireMesh.streak(sink, x + vx / vl * along, y + vy / vl * along, z + vz / vl * along, vx, vy, vz, Math.min(2.6 + d * 0.16, along * 0.4),
                0.035 + 0.025 * strength, SHOCK, 0.85 * fade);
        }
    }

    // ------------------------------------------------------------------------------------------------------ the ground

    /** How long the ground shows a quake (its cracks cool and fade, its dust settles). */
    public static final double QUAKE_LIFE = 60.0;
    public static final double TREMOR_LIFE = 30.0;
    static final double[] P = new double[3 * 12];

    /**
     * One crack in the ground from ({@code x}, {@code z}) at the ground's {@code y} out along {@code angle} to {@code length}, jagged, in
     * {@code steps} pieces: dark, with a white-hot core while {@code hot} (0 to 1) lasts.
     */
    static void groundCrack(FireMesh.Sink sink, double x, double y, double z, double angle, double start, double length, int steps, double width,
                            double hot, double alpha, int seed) {
        double px = x + Math.cos(angle) * start;
        double pz = z + Math.sin(angle) * start;
        double a = angle;
        for (int k = 1; k <= steps; k++) {
            a += 0.5 * (FireMesh.rand(seed, k) - 0.5);
            double step = (length - start) / steps;
            double qx = px + Math.cos(a) * step;
            double qz = pz + Math.sin(a) * step;
            double w = width * (1.0 - 0.7 * k / steps);
            flat(sink, px, y, pz, qx, y, qz, w, DARK, alpha * 0.92, false);
            if (hot > 0.0) {
                flat(sink, px, y + 0.01, pz, qx, y + 0.01, qz, w * 0.45, CORE, alpha * hot, false);
                flat(sink, px, y + 0.02, pz, qx, y + 0.02, qz, w * 3.0, GLOW, alpha * hot * 0.35, true);
            }
            px = qx;
            pz = qz;
        }
    }

    /**
     * The ground struck at ({@code x}, {@code y}, {@code z}) (on it) (refs 03, 04, 12, 32, 34): cracks shooting out {@code radius} and
     * glowing white, cooling dark; rings pressed into the ground one after another; a wall of shock running out; white shock lines thrown
     * up; rocks on arcs; a ring of dust rising and settling. {@code strength} 0 to 1.
     */
    public static void quake(FireMesh.Sink sink, double x, double y, double z, double radius, double strength, double age, int seed) {
        if (age < 0.0 || age > QUAKE_LIFE) {
            return;
        }
        double fade = clamp((QUAKE_LIFE - age) / 20.0);
        double hot = clamp(1.0 - age / 12.0);
        int cracks = 8 + (int) Math.min(6.0, radius);
        double grow = ease(age / 3.0);
        for (int i = 0; i < cracks; i++) {
            double a = Math.PI * 2.0 * (i + 0.5 * FireMesh.rand(seed, i)) / cracks;
            double length = radius * (0.8 + 0.35 * FireMesh.rand(seed, 30 + i)) * grow;
            groundCrack(sink, x, y + 0.04, z, a, radius * 0.38, Math.max(radius * 0.45, length), 5, 0.07 + 0.015 * radius, hot, fade, seed + i * 17);
            if (i % 2 == 0) {
                // a branch off it
                double b = a + (FireMesh.rand(seed, 90 + i) - 0.5) * 1.4;
                double from = radius * (0.5 + 0.2 * FireMesh.rand(seed, 95 + i));
                groundCrack(sink, x + Math.cos(a) * from, y + 0.04, z + Math.sin(a) * from, b, 0.0, Math.max(0.3, length * 0.35), 3,
                    0.05 + 0.01 * radius, hot, fade, seed + i * 29);
            }
        }
        // the rings pressed into the ground, one after another, white, fading
        for (int k = 0; k < 3; k++) {
            double born = k * 1.2;
            double u = age - born;
            if (u < 0.0 || u > 20.0) {
                continue;
            }
            double rr = radius * (0.42 + 0.2 * k) * ease(u / 3.0);
            FireMesh.ring(sink, x, y + 0.06, z, 0, 1, 0, rr, 0.18 + 0.04 * radius, 0.02, 0xE8FAFF, 0.85 * (1.0 - u / 20.0));
        }
        // the wall of shock running out over the ground
        if (age < 10.0) {
            double u = age / 10.0;
            FireMesh.ring(sink, x, y + 0.1, z, 0, 1, 0, radius * 1.8 * ease(u), 0.6 + 0.1 * radius, 0.8 + 0.6 * strength, SHOCK, 0.7 * (1.0 - u));
        }
        // white shock lines thrown up
        if (age < 8.0) {
            double u = age / 8.0;
            for (int i = 0; i < 8; i++) {
                double a = Math.PI * 2.0 * (i + FireMesh.rand(seed, 60 + i)) / 8.0;
                double el = Math.toRadians(48.0 + 30.0 * FireMesh.rand(seed, 70 + i));
                double dx = Math.cos(a) * Math.cos(el);
                double dy = Math.sin(el);
                double dz = Math.sin(a) * Math.cos(el);
                FireMesh.ray(sink, x, y, z, dx, dy, dz, radius * (0.7 + 0.6 * strength) * ease(u * 3.0), 0.08 + 0.04 * radius, 0, 0.0, SHOCK,
                    0.9 * (1.0 - u));
            }
        }
        rocks(sink, x, y, z, radius, strength, age, 10, seed);
        dust(sink, x, y, z, radius, age, 6, 1.0, seed);
    }

    /** Rocks thrown out on arcs from ({@code x}, {@code y}, {@code z}), landing and fading. */
    static void rocks(FireMesh.Sink sink, double x, double y, double z, double radius, double strength, double age, int count, int seed) {
        if (age > 34.0) {
            return;
        }
        int n = (int) Math.max(3, count * FireMesh.quality);
        for (int i = 0; i < n; i++) {
            int s = seed * 13 + i;
            double a = Math.PI * 2.0 * FireMesh.rand(s, 1);
            double out = (0.18 + 0.22 * FireMesh.rand(s, 2)) * (0.6 + 0.4 * radius / 4.0) * (0.7 + 0.5 * strength);
            double up = 0.35 + 0.35 * FireMesh.rand(s, 3) + 0.2 * strength;
            double t = age;
            double h = up * t - 0.045 * t * t;
            double land = up / 0.045 * 2.0;
            if (t > land) {
                t = land;
                h = 0.0;
            }
            double px = x + Math.cos(a) * out * t;
            double pz = z + Math.sin(a) * out * t;
            double size = 0.18 + 0.3 * FireMesh.rand(s, 4) * Math.min(1.5, radius / 3.0);
            FireMesh.shard(sink, px, y + Math.max(0.0, h) + size * 0.4, pz, size, age * 0.3, s, ROCK, clamp((34.0 - age) / 8.0));
        }
    }

    /** A ring of dust puffs round ({@code x}, {@code z}) on the ground, rising and spreading, settling away. */
    static void dust(FireMesh.Sink sink, double x, double y, double z, double radius, double age, int count, double scale, int seed) {
        double life = 50.0;
        if (age < 0.5 || age > life) {
            return;
        }
        int n = (int) Math.max(3, count * FireMesh.quality);
        double u = age / life;
        FireMesh.palette(FireMesh.SMOKE);
        for (int i = 0; i < n; i++) {
            int s = seed * 17 + i;
            double a = Math.PI * 2.0 * (i + FireMesh.rand(s, 1)) / n;
            double r = radius * (0.65 + 0.4 * FireMesh.rand(s, 2)) * (0.7 + 0.5 * ease(age / 8.0));
            double size = Math.min(1.8, radius * scale * (0.11 + 0.07 * FireMesh.rand(s, 3))) * (0.5 + 0.8 * ease(age / 10.0));
            double rise = size * 0.5 + u * radius * 0.2;
            FireMesh.puff(sink, x + Math.cos(a) * r, y + rise, z + Math.sin(a) * r, size, Math.cos(a), 0.4, Math.sin(a), 0.25, 0.1,
                0.82 + 0.12 * FireMesh.rand(s, 4), 0.1, false, 0.55 * (1.0 - u * u), age, s);
        }
        FireMesh.palette(FireMesh.BAND);
    }

    /** The landing quake round the feet at ({@code x}, {@code y}, {@code z}): a ring of shock over the ground, short cracks, dust. */
    public static void tremor(FireMesh.Sink sink, double x, double y, double z, double radius, double age, int seed) {
        if (age < 0.0 || age > TREMOR_LIFE) {
            return;
        }
        double fade = clamp((TREMOR_LIFE - age) / 10.0);
        if (age < 8.0) {
            double u = age / 8.0;
            FireMesh.ring(sink, x, y + 0.08, z, 0, 1, 0, radius * 1.4 * ease(u), 0.4 + 0.08 * radius, 0.4 + 0.1 * radius, SHOCK, 0.7 * (1.0 - u));
        }
        double hot = clamp(1.0 - age / 6.0);
        for (int i = 0; i < 6; i++) {
            double a = Math.PI * 2.0 * (i + 0.6 * FireMesh.rand(seed, i)) / 6.0;
            groundCrack(sink, x, y + 0.04, z, a, 0.35, radius * (0.4 + 0.25 * FireMesh.rand(seed, 10 + i)) * ease(age / 2.0) + 0.4, 3, 0.09, hot,
                fade, seed + i * 11);
        }
        dust(sink, x, y, z, radius * 0.8, age * 1.6, 5, 0.9, seed);
    }

    // ------------------------------------------------------------------------------------------------------ Shima Yurashi

    /**
     * Shima Yurashi held, {@code held} ticks in, round the feet at ({@code x}, {@code y}, {@code z}) out to {@code radius}: cracks spreading
     * over the ground from under him (hot at their tips), a pulse of shock over the ground at every lurch ({@code every} ticks), dust
     * jumping off the ground at the cracks.
     */
    public static void shake(FireMesh.Sink sink, double x, double y, double z, double radius, double held, int every, int seed) {
        double spread = ease(held / 50.0);
        for (int i = 0; i < 12; i++) {
            double a = Math.PI * 2.0 * (i + 0.6 * FireMesh.rand(seed, i)) / 12.0;
            double length = 1.6 + radius * (0.55 + 0.45 * FireMesh.rand(seed, 20 + i)) * spread;
            groundCrack(sink, x, y + 0.04, z, a, 1.2, length, 7, 0.16, 0.25 + 0.35 * (0.5 + 0.5 * Math.sin(held * 0.7 + i)), 1.0, seed + i * 19);
        }
        double beat = (held % every) / every;
        if (held > 4.0) {
            FireMesh.ring(sink, x, y + 0.1, z, 0, 1, 0, 1.5 + radius * ease(beat * 1.3), 0.8, 0.5, SHOCK, 0.45 * (1.0 - beat));
            FireMesh.ring(sink, x, y + 0.06, z, 0, 1, 0, 1.5 + radius * 0.5 * ease(beat * 1.6), 0.4, 0.2, GLOW, 0.3 * (1.0 - beat));
        }
        // dust jumping off the ground at each lurch, along the cracks
        FireMesh.palette(FireMesh.SMOKE);
        int lurch = (int) (held / every);
        double u = beat;
        int n = (int) Math.max(3, 6 * FireMesh.quality);
        for (int i = 0; i < n; i++) {
            int s = seed * 7 + lurch * 37 + i;
            double a = Math.PI * 2.0 * FireMesh.rand(s, 1);
            double r = 2.0 + (radius * spread) * FireMesh.rand(s, 2);
            double size = 0.6 + 0.6 * FireMesh.rand(s, 3);
            FireMesh.puff(sink, x + Math.cos(a) * r, y + size * 0.5 + u * 1.2, z + Math.sin(a) * r, size * (0.6 + 0.6 * ease(u * 2.0)), 0, 1, 0,
                0.3, 0.1, 0.84, 0.1, false, 0.55 * (1.0 - u), held, s);
        }
        FireMesh.palette(FireMesh.BAND);
    }

    /** How long the island's tilt shows over {@code radius} blocks (its wall runs out at {@code speed} blocks a tick). */
    public static double tiltLife(double radius, double speed) {
        return radius / speed + 30.0;
    }

    /**
     * Shima Yurashi let go (the island tilts): a wall of shock running out over the ground from ({@code x}, {@code y}, {@code z}) to
     * {@code radius}, a wall of dust following it, rocks thrown up at its front, the cracks under him glowing and cooling.
     */
    public static void tilt(FireMesh.Sink sink, double x, double y, double z, double radius, double power, double speed, double age, int seed) {
        double life = tiltLife(radius, speed);
        if (age < 0.0 || age > life) {
            return;
        }
        double fade = clamp((life - age) / 20.0);
        double r = Math.min(radius, age * speed);
        double front = clamp(1.0 - r / radius * 0.5) * clamp((radius / speed + 4.0 - age) / 4.0);
        if (front > 0.0) {
            FireMesh.ring(sink, x, y + 0.15, z, 0, 1, 0, r, 1.4, 1.6 + 1.2 * power, SHOCK, 0.8 * front);
            FireMesh.ring(sink, x, y + 0.08, z, 0, 1, 0, r * 0.8, 0.8, 0.6, GLOW, 0.45 * front);
        }
        for (int i = 0; i < 12; i++) {
            double a = Math.PI * 2.0 * (i + 0.6 * FireMesh.rand(seed, i)) / 12.0;
            groundCrack(sink, x, y + 0.04, z, a, 1.2, 1.6 + radius * (0.55 + 0.45 * FireMesh.rand(seed, 20 + i)), 7, 0.18,
                clamp(1.0 - age / 10.0), fade, seed + i * 19);
        }
        // the dust wall behind the shock, and rocks thrown up as it passes
        FireMesh.palette(FireMesh.SMOKE);
        int n = (int) Math.max(5, 10 * FireMesh.quality);
        for (int i = 0; i < n; i++) {
            int s = seed * 5 + i;
            double a = Math.PI * 2.0 * (i + 0.5 * FireMesh.rand(s, 1)) / n;
            double born = (radius * (0.3 + 0.7 * FireMesh.rand(s, 2))) / speed;
            double u = (age - born) / 30.0;
            if (u < 0.0 || u > 1.0) {
                continue;
            }
            double rr = born * speed;
            double size = 1.0 + 0.8 * FireMesh.rand(s, 3);
            FireMesh.puff(sink, x + Math.cos(a) * rr, y + size * 0.6 + u * 2.0, z + Math.sin(a) * rr, size * (0.6 + 0.8 * ease(u * 3.0)), Math.cos(a),
                0.5, Math.sin(a), 0.3, 0.1, 0.84, 0.1, false, 0.6 * (1.0 - u * u), age, s);
        }
        FireMesh.palette(FireMesh.BAND);
        for (int i = 0; i < 12; i++) {
            int s = seed * 3 + i;
            double a = Math.PI * 2.0 * FireMesh.rand(s, 1);
            double rr = radius * (0.25 + 0.7 * FireMesh.rand(s, 2));
            double t = age - rr / speed;
            if (t < 0.0 || t > 26.0) {
                continue;
            }
            double up = 0.45 + 0.3 * FireMesh.rand(s, 3);
            double h = Math.max(0.0, up * t - 0.045 * t * t);
            double size = 0.25 + 0.35 * FireMesh.rand(s, 4);
            FireMesh.shard(sink, x + Math.cos(a) * (rr + t * 0.12), y + h + size * 0.4, z + Math.sin(a) * (rr + t * 0.12), size, t * 0.3, s, ROCK,
                clamp((26.0 - t) / 6.0));
        }
    }

    // ------------------------------------------------------------------------------------------------------ the ultimate's wave

    /** How long the ultimate's wave shows (ticks): out to its reach, then the dust settling. */
    public static double heaveLife(double reach, double speed) {
        return reach / speed + 34.0;
    }

    /**
     * 3.1.6, the Gura ultimate's wave over its cone (the blocks heaving are client/gura/HeaveDraw's, from gura/Heave): from the feet
     * ({@code x}, {@code y}, {@code z}) along the flat ({@code dx}, {@code dz}) (unit), {@code cone} degrees each side, out to
     * {@code reach} at {@code speed} blocks a tick, {@code age} ticks in. A white shock arc riding the front and a glow under it, ground
     * cracks running out through the cone, a wall of dust rising behind the front, rocks thrown up as it passes.
     */
    public static void heave(FireMesh.Sink sink, double x, double y, double z, double dx, double dz, double cone, double reach, double speed,
                             double age, int seed) {
        double life = heaveLife(reach, speed);
        if (age < 0.0 || age > life) {
            return;
        }
        double half = Math.toRadians(cone);
        double way = Math.atan2(-dx, -dz); // as FireMesh.basis(0, 1, 0) turns it
        double centre = Math.atan2(dz, dx); // as cos/sin place a point in the world
        double r = Math.min(reach, Math.max(0.5, age * speed));
        double front = clamp((reach / speed + 3.0 - age) / 3.0);
        if (front > 0.0) {
            FireMesh.arc(sink, x, y + 0.5, z, 0, 1, 0, r, 1.6, way - half, half * 2.0, 1.6, SHOCK, 0.85 * front);
            FireMesh.arc(sink, x, y + 0.2, z, 0, 1, 0, r * 0.96, 1.0, way - half * 0.9, half * 1.8, 0.4, GLOW, 0.45 * front);
        }
        double fade = clamp((life - age) / 16.0);
        for (int i = 0; i < 9; i++) {
            double a = centre + half * (2.0 * (i + 0.5 * FireMesh.rand(seed, 40 + i)) / 9.0 - 1.0);
            double length = Math.min(r, reach * (0.55 + 0.45 * FireMesh.rand(seed, 60 + i)));
            groundCrack(sink, x, y + 0.04, z, a, 2.5, length, 9, 0.22, clamp(1.0 - (age - length / speed) / 12.0), fade, seed + i * 23);
        }
        // the dust wall behind the front, and rocks thrown up as it passes
        FireMesh.palette(FireMesh.SMOKE);
        // (thin: the heaving squares are the move, the dust must never hide them)
        int n = (int) Math.max(4, 8 * FireMesh.quality);
        for (int i = 0; i < n; i++) {
            int s = seed * 7 + i;
            double a = centre + half * (2.0 * FireMesh.rand(s, 1) - 1.0);
            double rr = reach * (0.35 + 0.65 * FireMesh.rand(s, 2));
            double u = (age - rr / speed) / 34.0;
            if (u < 0.0 || u > 1.0) {
                continue;
            }
            double size = 1.0 + 0.8 * FireMesh.rand(s, 3);
            FireMesh.puff(sink, x + Math.cos(a) * rr, y + size * 0.5 + u * 2.0, z + Math.sin(a) * rr, size * (0.6 + 0.8 * ease(u * 3.0)),
                Math.cos(a), 0.6, Math.sin(a), 0.3, 0.1, 0.84, 0.1, false, 0.4 * (1.0 - u * u), age, s);
        }
        FireMesh.palette(FireMesh.BAND);
        int rocks = (int) Math.max(8, 22 * FireMesh.quality);
        for (int i = 0; i < rocks; i++) {
            int s = seed * 3 + 500 + i;
            double a = centre + half * (2.0 * FireMesh.rand(s, 1) - 1.0);
            double rr = reach * (0.1 + 0.9 * FireMesh.rand(s, 2));
            double t = age - rr / speed;
            if (t < 0.0 || t > 30.0) {
                continue;
            }
            double up = 0.6 + 0.4 * FireMesh.rand(s, 3);
            double h = Math.max(0.0, up * t - 0.045 * t * t);
            double size = 0.3 + 0.45 * FireMesh.rand(s, 4);
            FireMesh.shard(sink, x + Math.cos(a) * (rr + t * 0.1), y + h + size * 0.4, z + Math.sin(a) * (rr + t * 0.1), size, t * 0.3, s, ROCK,
                clamp((30.0 - t) / 6.0));
        }
    }

    // ------------------------------------------------------------------------------------------------------ the rift

    /** How long the rift shows after it has opened all the way (ticks). */
    public static final double RIFT_AFTER = 40.0;

    /**
     * The ground split (refs 28, 37, 41, 55) from ({@code x}, {@code y}, {@code z}) along ({@code fx}, {@code fz}) (flat, unit), opening
     * {@code step} blocks a tick to {@code length}, {@code age} ticks in; {@code heights} the ground's height under each block along it
     * (relative to {@code y}, one a block). Its edges jagged and dark, the gap between them a dark crevasse, white light shooting out
     * of it at the front (white shock lines, a flash, rocks thrown up), side cracks off its edges, dust rising behind the front.
     */
    public static void rift(FireMesh.Sink sink, double x, double y, double z, double fx, double fz, double length, double step, double[] heights,
                            double age, int seed) {
        rift(sink, x, y, z, fx, fz, length, step, heights, 1.0, age, seed);
    }

    /** As above, {@code wide} times as wide (3.1.3: with Ryou on). */
    public static void rift(FireMesh.Sink sink, double x, double y, double z, double fx, double fz, double length, double step, double[] heights,
                            double wide, double age, int seed) {
        double opened = length / step;
        if (age < 0.0 || age > opened + RIFT_AFTER) {
            return;
        }
        double fade = clamp((opened + RIFT_AFTER - age) / 20.0);
        double d = Math.min(length, age * step);
        double sx = -fz;
        double sz = fx;
        int blocks = (int) Math.floor(d);
        double prevL = 0;
        double prevLz = 0;
        double prevR = 0;
        double prevRz = 0;
        double prevY = 0;
        double prevCx = 0;
        double prevCz = 0;
        for (int k = 0; k <= blocks; k++) {
            double h = heights != null && k < heights.length ? heights[k] : 0.0;
            double cy = y + h + 0.05;
            double cx = x + fx * k;
            double cz = z + fz * k;
            double since = age - k / step;
            double w = (0.25 + 0.9 * ease(since / 4.0)) * (k == 0 ? 0.4 : 1.0) * (1.0 - 0.3 * k / length) * wide;
            double jl = (FireMesh.rand(seed, k) - 0.5) * 0.35;
            double jr = (FireMesh.rand(seed, 200 + k) - 0.5) * 0.35;
            double lx = cx + sx * (w * 0.5 + jl);
            double lz = cz + sz * (w * 0.5 + jl);
            double rx = cx - sx * (w * 0.5 + jr);
            double rz = cz - sz * (w * 0.5 + jr);
            if (k > 0) {
                int dark = FireMesh.argb(0.93 * fade, 0x0C0907);
                FireMesh.quad(sink, prevL, prevY, prevLz, dark, lx, cy, lz, dark, rx, cy, rz, dark, prevR, prevY, prevRz, dark);
                flat(sink, prevL, prevY + 0.01, prevLz, lx, cy + 0.01, lz, 0.14, DARK, fade, false);
                flat(sink, prevR, prevY + 0.01, prevRz, rx, cy + 0.01, rz, 0.14, DARK, fade, false);
                // the light in the split: white hot near the front, cooling behind it
                double hot = clamp(1.0 - (d - k) / 9.0) * clamp(1.0 - (age - opened) / 10.0);
                if (hot > 0.0) {
                    flat(sink, prevCx, prevY + 0.03, prevCz, cx, cy + 0.03, cz, w * 0.5, CORE, 0.95 * hot, false);
                    flat(sink, prevCx, prevY + 0.04, prevCz, cx, cy + 0.04, cz, w * 2.2, GLOW, 0.45 * hot, true);
                }
                // side cracks off the edges (3.1.6: a narrow fissure, the ultimate's, fewer: five of them run at once)
                if (k % (wide < 1.0 ? 5 : 2) == 0) {
                    double side = FireMesh.rand(seed, 400 + k) < 0.5 ? 1.0 : -1.0;
                    double bx = side > 0 ? lx : rx;
                    double bz = side > 0 ? lz : rz;
                    double a = Math.atan2(sz * side, sx * side) + (FireMesh.rand(seed, 500 + k) - 0.5) * 1.2;
                    groundCrack(sink, bx - Math.cos(a) * 0.1, cy - 0.02, bz - Math.sin(a) * 0.1, a, 0.1, 1.0 + 1.6 * FireMesh.rand(seed, 600 + k), 3,
                        0.1, hot * 0.6, fade * ease(since / 3.0), seed + k * 23);
                }
            }
            prevL = lx;
            prevLz = lz;
            prevR = rx;
            prevRz = rz;
            prevY = cy;
            prevCx = cx;
            prevCz = cz;
        }
        // the front: a flash, white shock lines thrown up out of the split
        if (age < opened + 3.0) {
            double h = heights != null && blocks < heights.length ? heights[blocks] : 0.0;
            double tx = x + fx * d;
            double ty = y + h;
            double tz = z + fz * d;
            FireMesh.shell(sink, tx, ty + 0.4, tz, 1.1, CORE, 0.55, 0.0);
            for (int i = 0; i < 5; i++) {
                double a = Math.PI * 2.0 * FireMesh.rand(seed + (int) age, 700 + i);
                double el = Math.toRadians(55.0 + 25.0 * FireMesh.rand(seed + (int) age, 710 + i));
                FireMesh.ray(sink, tx, ty, tz, Math.cos(a) * Math.cos(el) * 0.6 + fx * 0.4, Math.sin(el), Math.sin(a) * Math.cos(el) * 0.6 + fz * 0.4,
                    2.6 + 1.4 * FireMesh.rand(seed, 720 + i), 0.14, 0, 0.0, SHOCK, 0.9);
            }
        }
        // rocks thrown up as the front passes, and dust rising behind it
        int shown = 0;
        FireMesh.palette(FireMesh.SMOKE);
        int puffs = wide < 1.0 ? 2 : 9; // 3.1.6: a narrow fissure (five run at once) raises less dust and fewer rocks
        for (int k = 1; k <= blocks && shown < puffs; k += wide < 1.0 ? 11 : 4) {
            double since = age - k / step;
            if (since > 40.0) {
                continue;
            }
            shown++;
            double h = heights != null && k < heights.length ? heights[k] : 0.0;
            int s = seed * 7 + k;
            double side = FireMesh.rand(s, 1) < 0.5 ? 1.0 : -1.0;
            double u = since / 40.0;
            double size = 0.9 + 0.6 * FireMesh.rand(s, 2);
            FireMesh.puff(sink, x + fx * k + sx * side * (0.8 + u * 0.8), y + h + size * 0.5 + u * 1.8, z + fz * k + sz * side * (0.8 + u * 0.8),
                size * (0.4 + 0.5 * ease(since / 8.0)), sx * side, 0.6, sz * side, 0.3, 0.1, 0.82 + 0.1 * FireMesh.rand(s, 3), 0.1, false,
                0.6 * (1.0 - u * u), age, s);
        }
        FireMesh.palette(FireMesh.BAND);
        for (int k = 1; k <= blocks; k += wide < 1.0 ? 4 : 2) {
            double t = age - k / step;
            if (t < 0.0 || t > 24.0) {
                continue;
            }
            double h = heights != null && k < heights.length ? heights[k] : 0.0;
            for (int j = 0; j < 2; j++) {
                int s = seed * 11 + k * 3 + j;
                double side = j == 0 ? 1.0 : -1.0;
                double up = 0.4 + 0.3 * FireMesh.rand(s, 1);
                double hh = Math.max(0.0, up * t - 0.045 * t * t);
                double out = (0.08 + 0.08 * FireMesh.rand(s, 2)) * t;
                double size = 0.22 + 0.3 * FireMesh.rand(s, 3);
                FireMesh.shard(sink, x + fx * k + sx * side * (0.6 + out), y + h + hh + size * 0.4, z + fz * k + sz * side * (0.6 + out), size, t * 0.3, s,
                    ROCK, clamp((24.0 - t) / 6.0));
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ the tsunami

    /** The wave's profile (across it: ahead, up; in units of its height), back at the ground to the curl of its lip and down its face. */
    static final double[] PROFILE = {
        -0.58, 0.0, -0.44, 0.2, -0.31, 0.45, -0.18, 0.72, -0.05, 0.92, 0.1, 1.0, 0.27, 0.97, 0.42, 0.87, 0.48, 0.7, 0.34, 0.63, 0.21, 0.48,
        0.13, 0.25, 0.14, 0.0,
    };
    /** Its flat colours, one a band: deep blue at the back, lighter up to the white crest and lip, blue again down its face. */
    static final int[] WATER = {
        0x0A3A73, 0x0F4F94, 0x1A6AB5, 0x3D8FD1, 0x86C8EE, 0xF2FAFF, 0xFAFDFF, 0xD6EFFC, 0x8DCDF2, 0x4C9EDB, 0x2A7AC2, 0x17609F,
    };
    static final int COLUMNS = 22;
    static final double[] ROW_A = new double[3 * 13];
    static final double[] ROW_B = new double[3 * 13];

    /** How long the tsunami shows: its life and its break. */
    public static double tsunamiLife(int life) {
        return life + 12.0;
    }

    /**
     * The tsunami (refs 29, 51): a wall of water {@code width} wide and {@code height} high whose middle starts at ({@code x}, {@code y},
     * {@code z}) on the ground and rolls on ({@code vx}, {@code vz}) a tick for {@code life} ticks, {@code age} ticks in: it rises in
     * {@code rise} ticks, its crest curling over, its height swelling in the middle and falling away at its ends, foam on its crest and at
     * its foot, spray thrown off its lip; at the end it breaks forward and falls. {@code baseY}: its foot's height now (relative to the
     * eye, following the ground or the water). 3.1.2: it stays where it rose for {@code hold} ticks (slowly growing out of the water),
     * then rolls (the two walls of the tsunami, left and right, rolling in to the middle).
     */
    public static void tsunami(FireMesh.Sink sink, double x, double baseY, double z, double vx, double vz, double width, double height, int life,
                               int rise, int hold, double age, int seed) {
        double end = tsunamiLife(life);
        if (age < 0.0 || age > end) {
            return;
        }
        double speed = Math.sqrt(vx * vx + vz * vz);
        double fx = speed < 1.0E-6 ? 0.0 : vx / speed;
        double fz = speed < 1.0E-6 ? 1.0 : vz / speed;
        double sx = -fz;
        double sz = fx;
        double travelled = speed * Math.max(0.0, Math.min(age, life + 6.0) - hold);
        double cx = x + fx * travelled;
        double cz = z + fz * travelled;
        double grow = ease(age / rise);
        double fall = clamp((age - life) / 12.0);
        double lean = fall;
        double alpha = 0.95 * (1.0 - fall * fall);
        double h = height * grow * (1.0 - 0.75 * fall);
        int points = PROFILE.length / 2;
        double[] prev = ROW_A;
        double[] cur = ROW_B;
        for (int j = 0; j <= COLUMNS; j++) {
            double w = (j / (double) COLUMNS - 0.5) * width;
            double e = Math.abs(2.0 * w / width);
            double taper = (1.0 - 0.35 * e * e) * clamp((1.0 - e) * 6.0 + 0.12);
            double hk = h * taper * (1.0 + 0.06 * Math.sin(age * 0.4 + j * 0.9 + seed));
            double off = 0.5 * Math.sin(j * 0.55 + age * 0.25 + seed) + lean * height * 0.35;
            for (int i = 0; i < points; i++) {
                double px = PROFILE[i * 2];
                double py = PROFILE[i * 2 + 1];
                if (i >= 6 && i <= 9) {
                    // the lip curling over, and falling as it breaks
                    px += lean * 0.35;
                    py -= lean * 0.35 * py;
                }
                double ahead = px * Math.max(hk, height * 0.25) + off;
                cur[i * 3] = cx + sx * w + fx * ahead;
                cur[i * 3 + 1] = baseY + py * hk;
                cur[i * 3 + 2] = cz + sz * w + fz * ahead;
            }
            if (j > 0) {
                for (int i = 0; i + 1 < points; i++) {
                    int c = FireMesh.argb(alpha, WATER[i]);
                    FireMesh.quad(sink, prev[i * 3], prev[i * 3 + 1], prev[i * 3 + 2], c, cur[i * 3], cur[i * 3 + 1], cur[i * 3 + 2], c,
                        cur[(i + 1) * 3], cur[(i + 1) * 3 + 1], cur[(i + 1) * 3 + 2], c, prev[(i + 1) * 3], prev[(i + 1) * 3 + 1],
                        prev[(i + 1) * 3 + 2], c);
                }
                // light streaks up its back (ref 29)
                if (j % 2 == 0) {
                    line(sink, cur[3], cur[4], cur[5], cur[3 * 3], cur[3 * 3 + 1], cur[3 * 3 + 2], 0.22, 0x9FD6F5, 0.45 * alpha, true);
                }
                // white streaks down its face
                if (j % 3 == 1) {
                    int k1 = 9;
                    int k2 = 11;
                    line(sink, cur[k1 * 3], cur[k1 * 3 + 1], cur[k1 * 3 + 2], cur[k2 * 3], cur[k2 * 3 + 1] + 0.3, cur[k2 * 3 + 2], 0.18, 0xE8F6FF,
                        0.55 * alpha, true);
                }
                // foam on the crest and at the foot
                if (j % 2 == 0 && hk > 0.6) {
                    foam(sink, cur[6 * 3], cur[6 * 3 + 1], cur[6 * 3 + 2], 0.75 + 0.45 * hk / height + lean * 0.8, fx, fz, alpha, age, seed * 31 + j);
                }
                if (j % 4 == 0 && hk > 0.6) {
                    foam(sink, cur[12 * 3] + fx * 0.4, baseY + 0.3, cur[12 * 3 + 2] + fz * 0.4, 0.5 + lean * 0.6, fx, fz, alpha * 0.9, age,
                        seed * 37 + j);
                }
                // spray off the lip
                if (j % 2 == 1 && hk > 1.0) {
                    double t = (age * 0.35 + FireMesh.rand(seed, 900 + j)) % 1.0;
                    double lx = cur[7 * 3] + fx * (0.6 + 2.2 * t);
                    double ly = cur[7 * 3 + 1] + 0.5 + 1.2 * t - 1.8 * t * t;
                    double lz = cur[7 * 3 + 2] + fz * (0.6 + 2.2 * t);
                    FireMesh.streak(sink, lx, ly, lz, fx, 0.4 - t, fz, 0.9, 0.06, 0xEAF8FF, 0.75 * alpha * (1.0 - t));
                }
            }
            double[] swap = prev;
            prev = cur;
            cur = swap;
        }
    }

    /** A clump of white foam at ({@code x}, {@code y}, {@code z}). */
    static void foam(FireMesh.Sink sink, double x, double y, double z, double size, double fx, double fz, double alpha, double age, int seed) {
        FireMesh.palette(FireMesh.AIR);
        FireMesh.puff(sink, x, y, z, size, fx, 0.3, fz, 0.3, 0.18, 0.82, 0.1, false, alpha, age * 1.5, seed);
        FireMesh.palette(FireMesh.BAND);
    }
}
