package com.hexhaki.piece.style;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.water.WaterMesh;

/**
 * 2.8.3: the fighting styles' blows in 3D (pure; the game's client/fx/StyleVfx and tools/preview/StylePreview draw the very same
 * quads; the owner: "better VFX on the M1s (m1 vfx on water kungfu suck ass), add variatations with the haki on/off"). Drawn as the
 * dragon's fire is (FireMesh): flat cel bands laid one inside another, alpha blended, never additive, right without culling.
 * <ul>
 *   <li>{@link #smear}: the sheet a striking fist or foot sweeps through the air, cut from the animation itself (StyleRig): widest and
 *       faintest outside, a hot narrow core along the tip's path, the newest edge at the limb, its tail fading;</li>
 *   <li>{@link #thrust}: a straight blow's push: rings of shocked air thrown off the fist (a low cone, as a sonic boom) and speed lines
 *       racing ahead of it;</li>
 *   <li>{@link #impact}: where a blow lands: the anime's impact star (sharp spikes), a ring blown open, a flash at the heart;</li>
 *   <li>{@link #splash}: Fishman Karate's water where it strikes: a crown of water thrown open, drops flung out and falling, ripples
 *       racing through the air one after another;</li>
 *   <li>{@link #helix}: water wound round the striking arm, spiralling out past the hand;</li>
 *   <li>{@link #crackle}: Armament Haki in a blow: black bolts edged in red crackling off it, new every tick.</li>
 * </ul>
 * Every look has a Haki twin ({@link #haki}): the same blow in hardened black, its edges red.
 */
public final class StyleArt {
    private StyleArt() {
    }

    /** A material: four cel bands, outermost (widest, faintest) first, each its own opacity. */
    public record Look(int[] bands, double[] alpha) {
        public int band(int i) {
            return this.bands[Math.max(0, Math.min(3, i))];
        }
    }

    /** Black Leg: kicks hot from the air they cut (orange to white). */
    public static final Look KICK = new Look(new int[] {0xE0571C, 0xFF9A3C, 0xFFDDA6, 0xFFFFFF}, new double[] {0.6, 0.75, 0.88, 1.0});
    /** Six Powers: pressed air (grey-blue to white, nearly clear). */
    public static final Look AIR = new Look(new int[] {0x9FB4C8, 0xD3DFE8, 0xEEF4F8, 0xFFFFFF}, new double[] {0.35, 0.5, 0.7, 0.95});
    /** Fishman Karate: water (deep blue, sea blue, pale, foam white). */
    public static final Look WATER = new Look(new int[] {0x1458A8, 0x2F8FDA, 0x8CD2F8, 0xF2FCFF}, new double[] {0.72, 0.8, 0.88, 1.0});
    /** Iron Fist: shocked air and dust (tan to white). */
    public static final Look SHOCK = new Look(new int[] {0xB08850, 0xEBD3A0, 0xFFF4DC, 0xFFFFFF}, new double[] {0.5, 0.65, 0.82, 1.0});
    /** The Flame Leg: fire, cut as the dragon's (FireMesh.BAND). */
    public static final Look FIRE = new Look(new int[] {0xD8301A, 0xF5651A, 0xFFD52B, 0xFFFDF2}, new double[] {0.75, 0.85, 0.92, 1.0});
    /** Armament Haki: hardened black, its edge red. */
    public static final Look HAKI = new Look(new int[] {0xD0122E, 0x3A0A16, 0x0E0A12, 0x020203}, new double[] {0.7, 0.88, 0.94, 1.0});
    /** Armament Haki in Fishman Karate's water: black water, its edge red, a dark sea blue between. */
    public static final Look HAKI_WATER = new Look(new int[] {0xD0122E, 0x0E2240, 0x0A0E1C, 0x020306}, new double[] {0.72, 0.88, 0.94, 1.0});

    /** The Haki twin of a look. */
    public static Look haki(Look look) {
        return look == WATER ? HAKI_WATER : HAKI;
    }

    /** A style's look. */
    public static Look of(FightingStyle style, boolean haki) {
        Look look = switch (style) {
            case BLACK_LEG -> KICK;
            case SIX_POWERS -> AIR;
            case FISHMAN_KARATE -> WATER;
            default -> SHOCK;
        };
        return haki ? haki(look) : look;
    }

    static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    static double smooth(double t) {
        double u = clamp(t);
        return u * u * (3.0 - 2.0 * u);
    }

    /** {@code 1 - (1 - t)^3}: fast out, settling. */
    static double out(double t) {
        double u = 1.0 - clamp(t);
        return 1.0 - u * u * u;
    }

    /**
     * A ring round {@code dir} as FireMesh.ring draws one (a band, clear at both edges, its outer edge {@code rise} along the axis), cut
     * finer: a big ripple near the eye stays round.
     */
    public static void ring(FireMesh.Sink sink, double x, double y, double z, double[] dir, double radius, double width, double rise, int rgb,
                            double alpha) {
        if (alpha < 0.004 || radius < 0.02) {
            return;
        }
        double dist = Math.sqrt(x * x + y * y + z * z);
        int steps = (int) Math.max(16, Math.min(72, radius * 48.0 / (0.6 + dist * 0.25)));
        double[] b = new double[6];
        FireMesh.basis(dir[0], dir[1], dir[2], b);
        int mid = FireMesh.argb(alpha, rgb);
        int clear = FireMesh.argb(0.0, rgb);
        double r0 = Math.max(0.0, radius - width * 0.5);
        double r2 = radius + width * 0.5;
        double h1 = rise * 0.5;
        for (int i = 0; i < steps; i++) {
            double a0 = i * Math.PI * 2.0 / steps;
            double a1 = (i + 1) * Math.PI * 2.0 / steps;
            double ax = b[0] * Math.cos(a0) + b[3] * Math.sin(a0);
            double ay = b[1] * Math.cos(a0) + b[4] * Math.sin(a0);
            double az = b[2] * Math.cos(a0) + b[5] * Math.sin(a0);
            double bx = b[0] * Math.cos(a1) + b[3] * Math.sin(a1);
            double by = b[1] * Math.cos(a1) + b[4] * Math.sin(a1);
            double bz = b[2] * Math.cos(a1) + b[5] * Math.sin(a1);
            double mx0 = x + ax * radius + dir[0] * h1;
            double my0 = y + ay * radius + dir[1] * h1;
            double mz0 = z + az * radius + dir[2] * h1;
            double mx1 = x + bx * radius + dir[0] * h1;
            double my1 = y + by * radius + dir[1] * h1;
            double mz1 = z + bz * radius + dir[2] * h1;
            FireMesh.quad(sink, x + ax * r0, y + ay * r0, z + az * r0, clear, mx0, my0, mz0, mid, mx1, my1, mz1, mid, x + bx * r0, y + by * r0,
                z + bz * r0, clear);
            FireMesh.quad(sink, mx0, my0, mz0, mid, x + ax * r2 + dir[0] * rise, y + ay * r2 + dir[1] * rise, z + az * r2 + dir[2] * rise, clear,
                x + bx * r2 + dir[0] * rise, y + by * r2 + dir[1] * rise, z + bz * r2 + dir[2] * rise, clear, mx1, my1, mz1, mid);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------ the smear

    /**
     * The sheet a limb sweeps: {@code n} moments, oldest first, each its inner point (toward the joint) and outer point (the fist or
     * foot, a little past it) in {@code inner} and {@code outer} (x, y, z each, eye-relative), and how fast the limb went there
     * ({@code strength}, 0 to 1: a slow wind-up leaves nothing). Four bands laid one in another: the outer faint and wide, the inner
     * hot, narrow, short and hugging the tip's path.
     */
    public static void smear(FireMesh.Sink sink, double[] inner, double[] outer, double[] strength, int n, Look look) {
        if (n < 2) {
            return;
        }
        for (int layer = 0; layer < 4; layer++) {
            double keep = 1.0 - 0.17 * layer;
            double v0 = 0.1 + 0.16 * layer;
            double v1 = 1.0 + 0.1 * (3 - layer) / 3.0;
            double first = (n - 1) * (1.0 - keep);
            int k0 = (int) Math.floor(first);
            int rgb = look.bands[layer];
            double a = look.alpha[layer];
            double[] pa = new double[3];
            double[] pb = new double[3];
            double[] qa = new double[3];
            double[] qb = new double[3];
            double lastAlpha = -1;
            for (int k = k0; k < n; k++) {
                double t = Math.max(first, k);
                double tail = smooth((t - first) / Math.max(1.0e-6, (n - 1 - first) * 0.6));
                double s = sample(strength, t, n);
                double alpha = a * s * tail;
                point(inner, outer, t, n, v0, qa);
                point(inner, outer, t, n, v1, qb);
                if (lastAlpha >= 0 && (alpha > 0.004 || lastAlpha > 0.004)) {
                    FireMesh.quad(sink, pa[0], pa[1], pa[2], FireMesh.argb(lastAlpha * 0.3, rgb), pb[0], pb[1], pb[2],
                        FireMesh.argb(lastAlpha, rgb), qb[0], qb[1], qb[2], FireMesh.argb(alpha, rgb), qa[0], qa[1], qa[2],
                        FireMesh.argb(alpha * 0.3, rgb));
                }
                System.arraycopy(qa, 0, pa, 0, 3);
                System.arraycopy(qb, 0, pb, 0, 3);
                lastAlpha = alpha;
            }
        }
    }

    static double sample(double[] values, double t, int n) {
        int i = (int) Math.floor(t);
        if (i >= n - 1) {
            return values[n - 1];
        }
        double f = t - i;
        return values[i] * (1 - f) + values[i + 1] * f;
    }

    /** The sheet at moment {@code t} (fractional), {@code v} of the way from inner (0) to outer (1, and past it). */
    static void point(double[] inner, double[] outer, double t, int n, double v, double[] out) {
        int i = Math.min(n - 2, (int) Math.floor(t));
        double f = Math.min(1.0, t - i);
        for (int c = 0; c < 3; c++) {
            double in = inner[i * 3 + c] * (1 - f) + inner[(i + 1) * 3 + c] * f;
            double ou = outer[i * 3 + c] * (1 - f) + outer[(i + 1) * 3 + c] * f;
            out[c] = in + (ou - in) * v;
        }
    }

    // ------------------------------------------------------------------------------------------------------------------ the thrust

    /** How long a straight blow's push lasts (ticks). */
    public static final double THRUST_LIFE = 6.0;

    /**
     * A straight blow's push from the fist at {@code at} along {@code dir} (unit), {@code age} ticks after it lands its full length,
     * {@code size} its weight (0.8 a jab to 1.6 a finisher): two rings of shocked air opening off the fist (cones flared back toward the
     * arm), speed lines racing ahead.
     */
    public static void thrust(FireMesh.Sink sink, double[] at, double[] dir, double age, double size, Look look, int seed) {
        double life = age / THRUST_LIFE;
        if (life >= 1.0 || life < 0.0) {
            return;
        }
        for (int r = 0; r < 2; r++) {
            double t = (life - r * 0.22) / (1.0 - r * 0.22);
            if (t <= 0.0) {
                continue;
            }
            double e = out(t);
            double d = 0.15 + (0.75 + 0.35 * r) * e * size;
            double radius = size * (0.2 + (0.62 - 0.14 * r) * e);
            double fade = Math.pow(1.0 - t, 1.4);
            double cx = at[0] + dir[0] * d;
            double cy = at[1] + dir[1] * d;
            double cz = at[2] + dir[2] * d;
            ring(sink, cx, cy, cz, dir, radius, size * 0.26 * (1.0 - 0.5 * t), -size * 0.4, look.band(1),
                Math.min(1.0, look.alpha[1] * 1.3) * fade);
            ring(sink, cx, cy, cz, dir, radius * 0.96, size * 0.1, -size * 0.34, look.band(3),
                look.alpha[3] * fade);
        }
        if (look == WATER || look == HAKI_WATER) {
            return; // water is pushed by its rings alone (its speed lines read as ice)
        }
        double[] b = new double[6];
        FireMesh.basis(dir[0], dir[1], dir[2], b);
        int lines = 7;
        double e = out(life);
        for (int i = 0; i < lines; i++) {
            double angle = (i + FireMesh.rand(seed, i) * 0.6) * Math.PI * 2.0 / lines;
            double radial = size * (0.16 + 0.22 * FireMesh.rand(seed, i + 17));
            double lead = -0.3 + (1.4 + 0.8 * FireMesh.rand(seed, i + 31)) * e * size;
            double ox = (b[0] * Math.cos(angle) + b[3] * Math.sin(angle)) * radial;
            double oy = (b[1] * Math.cos(angle) + b[4] * Math.sin(angle)) * radial;
            double oz = (b[2] * Math.cos(angle) + b[5] * Math.sin(angle)) * radial;
            double length = size * (0.45 + 0.4 * FireMesh.rand(seed, i + 47)) * (1.0 - 0.5 * life);
            FireMesh.streak(sink, at[0] + ox + dir[0] * lead, at[1] + oy + dir[1] * lead, at[2] + oz + dir[2] * lead, dir[0], dir[1],
                dir[2], length, size * 0.04, look.band(2), (1.0 - life));
        }
    }

    // ------------------------------------------------------------------------------------------------------------------ the impact

    /** How long a landed blow's burst lasts (ticks). */
    public static final double IMPACT_LIFE = 7.0;

    /**
     * Where a blow lands, {@code at}, struck along {@code dir} (unit), {@code age} ticks on: the impact star (spikes thrown out across
     * the blow and a little on along it, grown in two ticks, thinning as they go), a ring blown open, a flash at the heart.
     */
    public static void impact(FireMesh.Sink sink, double[] at, double[] dir, double age, double size, Look look, int seed) {
        double life = age / IMPACT_LIFE;
        if (life >= 1.0 || life < 0.0) {
            return;
        }
        double[] b = new double[6];
        FireMesh.basis(dir[0], dir[1], dir[2], b);
        double grow = smooth(age / 2.0);
        double fade = 1.0 - smooth((life - 0.25) / 0.75);
        int spikes = 9;
        for (int i = 0; i < spikes; i++) {
            double angle = (i + 0.7 * FireMesh.rand(seed, i)) * Math.PI * 2.0 / spikes;
            double tilt = 0.15 + 0.45 * FireMesh.rand(seed, i + 13);
            double ux = b[0] * Math.cos(angle) + b[3] * Math.sin(angle) + dir[0] * tilt;
            double uy = b[1] * Math.cos(angle) + b[4] * Math.sin(angle) + dir[1] * tilt;
            double uz = b[2] * Math.cos(angle) + b[5] * Math.sin(angle) + dir[2] * tilt;
            double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
            ux /= ul;
            uy /= ul;
            uz /= ul;
            double length = size * (0.8 + 0.9 * FireMesh.rand(seed, i + 29)) * (i % 3 == 0 ? 1.4 : 1.0) * (0.6 + 0.4 * grow + 0.3 * life);
            double start = size * 0.12 * life * 2.0;
            double sx = at[0] + ux * start;
            double sy = at[1] + uy * start;
            double sz = at[2] + uz * start;
            double width = size * 0.12 * (1.0 - 0.7 * life);
            FireMesh.ray(sink, sx, sy, sz, ux, uy, uz, length * grow, width * 1.9, 0, 0, look.band(1), Math.min(1.0, look.alpha[1] * 1.3) * fade);
            FireMesh.ray(sink, sx, sy, sz, ux, uy, uz, length * grow * 0.85, width, 0, 0, look.band(3), look.alpha[3] * fade);
        }
        double e = out(life);
        ring(sink, at[0], at[1], at[2], dir, size * (0.18 + 0.72 * e), size * 0.32 * (1.0 - life), size * 0.18,
            look.band(1), Math.min(1.0, look.alpha[1] * 1.2) * (1.0 - life));
        ring(sink, at[0], at[1], at[2], dir, size * (0.18 + 0.72 * e), size * 0.1 * (1.0 - life), size * 0.18,
            look.band(3), look.alpha[3] * (1.0 - life));
        if (age < 3.0) {
            double flash = 1.0 - age / 3.0;
            FireMesh.shell(sink, at[0], at[1], at[2], size * (0.22 + 0.2 * grow) * flash, look.band(3), 0.95 * flash, 0.0);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------ the water

    /** How long Fishman Karate's splash lasts (ticks). */
    public static final double SPLASH_LIFE = 16.0;
    /** How hard the drops fall (blocks a tick, each tick). */
    static final double FALL = 0.035;

    /**
     * Water where a Fishman Karate blow strikes ({@code at}, along {@code dir}), {@code age} ticks on: a crown thrown open along the
     * blow (a cone of water, its lip white), drops flung out round it that arc and fall, three ripples racing out through the air one
     * after another. {@code look}: {@link #WATER} or its Haki twin.
     */
    public static void splash(FireMesh.Sink sink, double[] at, double[] dir, double age, double size, Look look, int seed) {
        double life = age / SPLASH_LIFE;
        if (life >= 1.0 || life < 0.0) {
            return;
        }
        // the crown: quick, a cone of water flared out along the blow
        double crown = age / 7.0;
        if (crown < 1.0) {
            double e = out(crown);
            double fade = 1.0 - smooth((crown - 0.35) / 0.65);
            double cx = at[0] + dir[0] * 0.1;
            double cy = at[1] + dir[1] * 0.1;
            double cz = at[2] + dir[2] * 0.1;
            ring(sink, cx, cy, cz, dir, size * (0.15 + 0.6 * e), size * 0.42 * (1.0 - 0.6 * crown),
                size * (0.25 + 0.55 * e), look.band(1), look.alpha[1] * fade);
            ring(sink, cx, cy, cz, dir, size * (0.15 + 0.62 * e), size * 0.2 * (1.0 - 0.6 * crown),
                size * (0.3 + 0.6 * e), look.band(2), look.alpha[2] * fade);
            ring(sink, cx + dir[0] * size * 0.3 * e, cy + dir[1] * size * 0.3 * e, cz + dir[2] * size * 0.3 * e, dir, size * (0.2 + 0.78 * e), size * 0.08, size * 0.15, look.band(3), look.alpha[3] * fade);
        }
        // the ripples: two rings racing out through the air, one after the other (a crest of foam on a band of sea)
        for (int r = 0; r < 2; r++) {
            double t = (age - r * 2.5) / 8.0;
            if (t <= 0.0 || t >= 1.0) {
                continue;
            }
            double e = out(t);
            double d = 0.15 + 0.55 * e * size;
            double radius = size * (0.25 + 0.75 * e);
            double fade = 1.0 - smooth((t - 0.3) / 0.7);
            double cx = at[0] + dir[0] * d;
            double cy = at[1] + dir[1] * d;
            double cz = at[2] + dir[2] * d;
            ring(sink, cx, cy, cz, dir, radius, size * 0.22 * (1.0 - 0.5 * t), -size * 0.1, look.band(1), look.alpha[1] * 0.8 * fade);
            ring(sink, cx, cy, cz, dir, radius * 1.02, size * 0.07 * (1.0 - 0.4 * t), -size * 0.08, look.band(3), look.alpha[3] * fade);
        }
        // the drops: flung out across and along the blow, arcing down
        double[] b = new double[6];
        FireMesh.basis(dir[0], dir[1], dir[2], b);
        int drops = 16;
        WaterMesh.Look water = look == WATER ? WaterMesh.WATER : WaterMesh.HAKI;
        for (int i = 0; i < drops; i++) {
            double angle = (i + 0.8 * FireMesh.rand(seed, i)) * Math.PI * 2.0 / drops;
            double across = 0.12 + 0.16 * FireMesh.rand(seed, i + 5);
            double along = 0.08 + 0.2 * FireMesh.rand(seed, i + 9);
            double vx = (b[0] * Math.cos(angle) + b[3] * Math.sin(angle)) * across + dir[0] * along;
            double vy = (b[1] * Math.cos(angle) + b[4] * Math.sin(angle)) * across + dir[1] * along + 0.07;
            double vz = (b[2] * Math.cos(angle) + b[5] * Math.sin(angle)) * across + dir[2] * along;
            double drag = Math.pow(0.9, age);
            double travelled = (1.0 - drag) / 0.1;
            double x = at[0] + vx * travelled * size;
            double y = at[1] + vy * travelled * size - FALL * age * age * 0.5;
            double z = at[2] + vz * travelled * size;
            double fy = vy * drag - FALL * age;
            double radius = size * (0.045 + 0.045 * FireMesh.rand(seed, i + 41)) * (1.0 - 0.5 * life);
            drop(sink, x, y, z, radius, new double[] {vx * drag, fy, vz * drag}, water, age, seed + i, 1.0 - smooth((life - 0.6) / 0.4));
        }
    }

    /** One drop of water, drawn out along its flight. */
    static void drop(FireMesh.Sink sink, double x, double y, double z, double radius, double[] velocity, WaterMesh.Look look, double time,
                     int seed, double opacity) {
        if (radius < 0.008 || opacity < 0.01) {
            return;
        }
        double speed = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1] + velocity[2] * velocity[2]);
        double[] along = speed < 1.0e-6 ? new double[] {0, -1, 0} : new double[] {velocity[0] / speed, velocity[1] / speed, velocity[2] / speed};
        WaterMesh.Body body = new WaterMesh.Body(x, y, z, radius, along, Math.min(2.0, speed * 9.0), 0.1, time, seed, opacity);
        int[] res = WaterMesh.resolution(radius, Math.sqrt(x * x + y * y + z * z));
        WaterMesh.body(body, Math.max(4, res[0] / 2), Math.max(6, res[1] / 2), look, (xyz, argb) -> {
            double[] q = {xyz[0], xyz[1], xyz[2], xyz[3], xyz[4], xyz[5], xyz[6], xyz[7], xyz[8], xyz[6], xyz[7], xyz[8]};
            sink.quad(q, new int[] {argb[0], argb[1], argb[2], argb[2]});
        });
    }

    /**
     * Water wound round a striking arm, from {@code from} (the elbow) to {@code to} (the hand) and on past it along the blow: two strands
     * spiralling round it, turning with {@code age}, opening as they leave the hand, {@code strength} of the way in (0 to 1).
     */
    public static void helix(FireMesh.Sink sink, double[] from, double[] to, double age, double strength, Look look) {
        if (strength < 0.01) {
            return;
        }
        double ax = to[0] - from[0];
        double ay = to[1] - from[1];
        double az = to[2] - from[2];
        double length = Math.sqrt(ax * ax + ay * ay + az * az);
        if (length < 1.0e-4) {
            return;
        }
        ax /= length;
        ay /= length;
        az /= length;
        double[] b = new double[6];
        FireMesh.basis(ax, ay, az, b);
        double reach = length + 0.9 * strength;
        int steps = 18;
        for (int strand = 0; strand < 2; strand++) {
            double phase = strand * Math.PI + age * 0.9;
            for (int layer = 0; layer < 2; layer++) {
                int rgb = layer == 0 ? look.band(1) : look.band(3);
                double alpha = (layer == 0 ? look.alpha[1] : look.alpha[3]) * strength;
                double width = layer == 0 ? 0.1 : 0.045;
                double[] last = null;
                double lastW = 0;
                double lastA = 0;
                for (int i = 0; i <= steps; i++) {
                    double t = i / (double) steps;
                    double d = reach * t;
                    double open = d > length ? (d - length) / Math.max(0.1, reach - length) : 0.0;
                    double radius = 0.13 + 0.25 * open * open;
                    double turn = phase + t * Math.PI * 3.0;
                    double ox = (b[0] * Math.cos(turn) + b[3] * Math.sin(turn)) * radius;
                    double oy = (b[1] * Math.cos(turn) + b[4] * Math.sin(turn)) * radius;
                    double oz = (b[2] * Math.cos(turn) + b[5] * Math.sin(turn)) * radius;
                    double[] p = {from[0] + ax * d + ox, from[1] + ay * d + oy, from[2] + az * d + oz};
                    double w = width * (0.4 + 0.6 * Math.sin(Math.PI * Math.min(1.0, t * 1.15)));
                    double a = alpha * smooth(t * 4.0) * (1.0 - smooth((t - 0.75) / 0.25));
                    if (last != null) {
                        // a flat strip facing out from the axis: across it, the axis's own side direction
                        double sx = ox / radius;
                        double sy = oy / radius;
                        double sz = oz / radius;
                        double nx = ay * sz - az * sy;
                        double ny = az * sx - ax * sz;
                        double nz = ax * sy - ay * sx;
                        FireMesh.quad(sink, last[0] - nx * lastW, last[1] - ny * lastW, last[2] - nz * lastW, FireMesh.argb(lastA, rgb),
                            last[0] + nx * lastW, last[1] + ny * lastW, last[2] + nz * lastW, FireMesh.argb(lastA, rgb), p[0] + nx * w,
                            p[1] + ny * w, p[2] + nz * w, FireMesh.argb(a, rgb), p[0] - nx * w, p[1] - ny * w, p[2] - nz * w,
                            FireMesh.argb(a, rgb));
                    }
                    last = p;
                    lastW = w;
                    lastA = a;
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------------ Haki

    /**
     * Armament Haki crackling off a blow at {@code at}: {@code bolts} jagged bolts out every way (a little along {@code dir}), each a red
     * glow round a black core, new shapes every tick ({@code tick}), {@code reach} long, fading by {@code fade}.
     */
    public static void crackle(FireMesh.Sink sink, double[] at, double[] dir, int tick, int bolts, double reach, double fade, int seed) {
        if (fade < 0.01) {
            return;
        }
        for (int i = 0; i < bolts; i++) {
            int s = seed * 31 + tick * 7 + i * 131;
            double ux = FireMesh.rand(s, 1) * 2 - 1 + dir[0] * 0.6;
            double uy = FireMesh.rand(s, 2) * 2 - 1 + dir[1] * 0.6;
            double uz = FireMesh.rand(s, 3) * 2 - 1 + dir[2] * 0.6;
            double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
            if (ul < 1.0e-6) {
                continue;
            }
            ux /= ul;
            uy /= ul;
            uz /= ul;
            double length = reach * (0.55 + 0.45 * FireMesh.rand(s, 4));
            int segments = 5;
            double[] b = new double[6];
            FireMesh.basis(ux, uy, uz, b);
            double[] prev = {at[0], at[1], at[2]};
            for (int k = 1; k <= segments; k++) {
                double t = k / (double) segments;
                double jitter = k == segments ? 0.0 : length * 0.22;
                double j1 = (FireMesh.rand(s, 10 + k) * 2 - 1) * jitter;
                double j2 = (FireMesh.rand(s, 20 + k) * 2 - 1) * jitter;
                double[] p = {at[0] + ux * length * t + b[0] * j1 + b[3] * j2, at[1] + uy * length * t + b[1] * j1 + b[4] * j2,
                    at[2] + uz * length * t + b[2] * j1 + b[5] * j2};
                double w0 = 0.05 * (1.0 - (t - 1.0 / segments) * 0.8);
                double w1 = 0.05 * (1.0 - t * 0.8);
                bolt(sink, prev, p, w0 * 2.2, w1 * 2.2, 0xD0102E, 0.55 * fade);
                bolt(sink, prev, p, w0, w1, 0x050307, 0.95 * fade);
                prev = p;
            }
        }
    }

    /** A bolt's segment: two crossed strips {@code w0} to {@code w1} wide. */
    static void bolt(FireMesh.Sink sink, double[] a, double[] c, double w0, double w1, int rgb, double alpha) {
        double dx = c[0] - a[0];
        double dy = c[1] - a[1];
        double dz = c[2] - a[2];
        double l = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (l < 1.0e-6) {
            return;
        }
        double[] b = new double[6];
        FireMesh.basis(dx / l, dy / l, dz / l, b);
        int col = FireMesh.argb(alpha, rgb);
        for (int side = 0; side < 2; side++) {
            double sx = b[side * 3];
            double sy = b[side * 3 + 1];
            double sz = b[side * 3 + 2];
            FireMesh.quad(sink, a[0] - sx * w0, a[1] - sy * w0, a[2] - sz * w0, col, a[0] + sx * w0, a[1] + sy * w0, a[2] + sz * w0, col,
                c[0] + sx * w1, c[1] + sy * w1, c[2] + sz * w1, col, c[0] - sx * w1, c[1] - sy * w1, c[2] - sz * w1, col);
        }
    }
}
