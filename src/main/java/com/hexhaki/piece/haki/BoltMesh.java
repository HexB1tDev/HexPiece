package com.hexhaki.piece.haki;

import java.util.SplittableRandom;

/**
 * 2.1.5: a {@link Bolt}'s geometry for one frame (pure: the game feeds it to its buffers, {@code tools/preview/BoltPreview} rasterises
 * the very same quads to judge them).
 *
 * <p>Everything is in camera space (the eye at the origin, so the numbers stay small and the floats exact). Every layer is a ribbon
 * that turns to face the eye at each point of the path, with a round disc at every joint and end, so from any side it reads as a round
 * tube that bends with the lightning:
 * <ol>
 *   <li><b>outline</b>: black, a little wider than the core, at least about a pixel and a half thick at any distance (the anime's ink
 *       line);</li>
 *   <li><b>body</b>: the original's near-black strokes, a black tube;</li>
 *   <li><b>core</b>: the colour, shaded across like a lit tube (darker at its rims, lighter along its middle);</li>
 *   <li><b>heart</b>: a thin white-hot line in the middle of the core;</li>
 *   <li><b>glow</b>: an additive halo in the colour, strongest at the bolt, nothing at its rims (drawn without depth writes).</li>
 * </ol>
 * In time: the bolt races out from its root (a hot spark leads it), flashes wide and white the moment it lands, flickers tick by tick,
 * dances into a new crackle every two ticks, and in the last part of its life thins away, core first, outline last.
 */
public final class BoltMesh {
    /** Ink colour of the outline (not pure black: a hint of blood, as the anime's Conqueror ink). */
    public static final int INK = 0xFF07020A;
    /** Life share after which the bolt thins away. */
    public static final double FADE_FROM = 0.55;
    /** Ticks the landing flash lasts. */
    public static final double FLASH = 1.6;
    /** Octagon joints. */
    static final int ROUND = 8;
    /** The most a ribbon widens at a kink. */
    static final double MITRE = 2.0;
    /** The original's black body is three times its red core: drawn at this share, the red reads inside a firm black line. */
    static final double BODY = 0.62;
    /** The core drawn this much fuller than the original's beam, so its colour carries. */
    static final double CORE = 1.25;

    /** Where the geometry goes: {@code glow} quads are additive and write no depth; the others are opaque. */
    public interface Sink {
        void quad(boolean glow, double[] xyz, int[] argb);
    }

    /** How a bolt's frame sits in camera space: its origin and a turn about the vertical (JOML's rotateY: x' = x cos + z sin). */
    public record Frame(double x, double y, double z, double cos, double sin) {
        public static Frame at(double x, double y, double z) {
            return new Frame(x, y, z, 1.0, 0.0);
        }

        /** Turned by {@code radians} about the vertical. */
        public static Frame turned(double x, double y, double z, double radians) {
            return new Frame(x, y, z, Math.cos(radians), Math.sin(radians));
        }

        void place(double[] local, double[] out, int at) {
            double lx = local[0];
            double lz = local[2];
            out[at] = this.x + lx * this.cos + lz * this.sin;
            out[at + 1] = this.y + local[1];
            out[at + 2] = this.z - lx * this.sin + lz * this.cos;
        }
    }

    private BoltMesh() {
    }

    /** 0..1: how strongly the bolt shows at {@code age} ticks into its life (for its thinning and the glow). */
    public static double fade(double life) {
        if (life <= FADE_FROM) {
            return 1.0;
        }
        double k = Math.min(1.0, (life - FADE_FROM) / (1.0 - FADE_FROM));
        return 1.0 - k * k * (3.0 - 2.0 * k);
    }

    /** 0..1: the landing flash, the moment the bolt reaches its end. */
    public static double flash(Bolt bolt, double age) {
        double since = age - bolt.crawl;
        if (since < 0 || since > FLASH) {
            return 0.0;
        }
        return 1.0 - since / FLASH;
    }

    /** 0.72..1: this tick's flicker. */
    public static double flicker(Bolt bolt, double age) {
        long tick = (long) Math.floor(Math.max(0.0, age));
        return 0.72 + 0.28 * new SplittableRandom(bolt.seed ^ (tick * 0x2545F4914F6CDD1DL)).nextDouble();
    }

    /**
     * The bolt at {@code age} ticks after its delay (nothing before 0 or after its duration), placed by {@code frame}; {@code pixel} is
     * the size of a screen pixel one block from the eye (the minimum line widths come from it).
     */
    public static void build(Bolt bolt, Frame frame, double age, double pixel, Sink sink) {
        build(bolt, frame, age, pixel, 1.0, sink);
    }

    /**
     * As {@link #build(Bolt, Frame, double, double, Sink)}, the glow scaled by {@code shine}: 1 in the dark, about half under a noon
     * sky (added light washes out against a bright sky, and lightning reads by its ink there).
     */
    public static void build(Bolt bolt, Frame frame, double age, double pixel, double shine, Sink sink) {
        if (age < 0 || age > bolt.duration) {
            return;
        }
        int full = bolt.count();
        // far away, the finest kinks are smaller than a pixel: every second (fourth ...) point is the coarser crackle they ride on
        double[] mid = new double[3];
        bolt.dancing(full / 2, age, mid);
        double[] at = new double[3];
        frame.place(mid, at, 0);
        double away = Math.max(0.5, Math.sqrt(at[0] * at[0] + at[1] * at[1] + at[2] * at[2]));
        double pieceLength = Math.max(1.0E-6, length(bolt) / (full - 1));
        int step = 1;
        while (pieceLength * step / (pixel * away) < 5.0 && (full - 1) % (step * 2) == 0 && (full - 1) / (step * 2) >= 2) {
            step *= 2;
        }
        int n = (full - 1) / step + 1;
        double[] all = new double[n * 3];
        float[] widths = new float[n];
        double[] local = new double[3];
        for (int i = 0; i < n; i++) {
            bolt.dancing(i * step, age, local);
            frame.place(local, all, i * 3);
            widths[i] = bolt.widths[i * step];
        }
        // the strike: only the part the bolt has raced along so far
        double grow = bolt.crawl <= 0 ? 1.0 : Math.min(1.0, age / bolt.crawl);
        double[] lengths = new double[n];
        for (int i = 1; i < n; i++) {
            lengths[i] = lengths[i - 1] + distance(all, i - 1, i);
        }
        double reach = lengths[n - 1] * grow;
        int m = 1;
        while (m < n - 1 && lengths[m] < reach) {
            m++;
        }
        double[] p = new double[(m + 1) * 3];
        float[] w = new float[m + 1];
        System.arraycopy(all, 0, p, 0, m * 3);
        System.arraycopy(widths, 0, w, 0, m);
        double span = lengths[m] - lengths[m - 1];
        double t = span < 1.0E-9 ? 1.0 : Math.max(0.0, Math.min(1.0, (reach - lengths[m - 1]) / span));
        for (int k = 0; k < 3; k++) {
            p[m * 3 + k] = all[(m - 1) * 3 + k] + (all[m * 3 + k] - all[(m - 1) * 3 + k]) * t;
        }
        w[m] = (float) (widths[m - 1] + (widths[m] - widths[m - 1]) * t);
        if (reach < 1.0E-4) {
            return;
        }
        int count = m + 1;
        double life = age / bolt.duration;
        double fade = fade(life);
        double flash = flash(bolt, age);
        double flicker = flicker(bolt, age);
        double[] dist = new double[count];
        for (int i = 0; i < count; i++) {
            dist[i] = Math.sqrt(p[i * 3] * p[i * 3] + p[i * 3 + 1] * p[i * 3 + 1] + p[i * 3 + 2] * p[i * 3 + 2]);
        }
        double[] side = sides(p, count);
        int color = bolt.color;
        switch (bolt.kind) {
            case Bolt.BODY -> {
                double scale = Math.sqrt(fade) * (1.0 + 0.15 * flash);
                double[] half = new double[count];
                for (int i = 0; i < count; i++) {
                    half[i] = Math.max(w[i] * 0.5 * BODY * scale, 1.3 * pixel * dist[i] * fade);
                }
                int body = 0xFF000000 | (color & 0xFFFFFF);
                tube(p, count, side, half, body, body, 0.0, pixel, dist, sink);
            }
            case Bolt.EDGE -> {
                // behind the black body (the same width rule as BODY's), a hot line in the colour hugging it and a wide glow round it
                double scale = Math.sqrt(fade) * (1.0 + 0.15 * flash);
                double bright = Math.min(1.0, flicker * (0.85 + 0.15 * fade) + 0.4 * flash);
                double[] rim = new double[count];
                double[] halo = new double[count];
                for (int i = 0; i < count; i++) {
                    double body = Math.max(w[i] * 0.5 * BODY * scale, 1.3 * pixel * dist[i] * fade);
                    rim[i] = body * 1.12 + 1.8 * pixel * dist[i] * fade;
                    halo[i] = body * 2.3 + 0.12;
                }
                int glowColor = scale(color, (0.6 * bright * fade + 0.35 * flash) * shine);
                // the glow too only outside the rim: a full halo ribbon would twist in front of the black where the bolt turns
                glowBand(p, count, side, rim, halo, glowColor, sink);
                int hot = mix(scale(color, bright), 0xFFFFFFFF, 0.3 + 0.35 * flash);
                // only the bands outside the black (nothing of the rim lies behind the body to show through where a wide ribbon twists)
                double[] inner = new double[count];
                for (int i = 0; i < count; i++) {
                    inner[i] = Math.max(w[i] * 0.5 * BODY * scale, 1.3 * pixel * dist[i] * fade) * 0.96;
                }
                band(p, count, side, inner, rim, hot, scale(color, 0.75 + 0.25 * bright), -0.5, dist, sink);
            }
            case Bolt.AURA -> {
                double strength = ((color >>> 24) & 255) / 255.0 * 0.7 * fade * flicker * (1.0 + 0.6 * flash);
                double[] half = new double[count];
                for (int i = 0; i < count; i++) {
                    half[i] = w[i] * 0.7 * (0.6 + 0.4 * fade);
                }
                // additive light cannot darken: a dark aura glows in its own hue, brought up to full
                int top = Math.max(1, Math.max((color >>> 16) & 255, Math.max((color >>> 8) & 255, color & 255)));
                glow(p, count, side, half, scale(color, strength * shine * 255.0 / top), sink);
            }
            case Bolt.HEART -> {
                double scale = fade * (1.0 + 0.4 * flash);
                double[] half = new double[count];
                for (int i = 0; i < count; i++) {
                    half[i] = Math.max(w[i] * 0.5 * scale, 0.5 * pixel * dist[i] * fade);
                }
                int hot = mix(color, 0xFFFFFFFF, 0.35 + 0.4 * flash);
                tube(p, count, side, half, hot, hot, 3.0, pixel, dist, sink);
            }
            default -> {
                double scale = fade * (1.0 + 0.3 * flash);
                double bright = Math.min(1.0, flicker * (0.82 + 0.18 * fade) + 0.4 * flash);
                double[] core = new double[count];
                double[] ink = new double[count];
                double[] heart = new double[count];
                double[] halo = new double[count];
                for (int i = 0; i < count; i++) {
                    double c = Math.max(w[i] * 0.5 * CORE * scale, 0.75 * pixel * dist[i] * fade);
                    core[i] = c;
                    ink[i] = c + (0.38 * c + 1.5 * pixel * dist[i]) * Math.min(1.0, fade * 1.6);
                    heart[i] = c * (0.34 + 0.2 * flash);
                    halo[i] = c * (4.0 + 1.5 * flash) + 0.05 * fade;
                }
                int glowColor = scale(color, (0.75 * bright * fade + 0.3 * flash) * shine);
                // far away a bolt is a few pixels wide: what would not show is left out (the glow under two pixels, the heart and the
                // crackle under one), so a field of distant lightning stays cheap to draw
                int middleAt = count / 2;
                double corePx = core[middleAt] / (pixel * dist[middleAt]);
                if (halo[middleAt] / (pixel * dist[middleAt]) >= 2.0) {
                    glow(p, count, side, halo, glowColor, sink);
                }
                tube(p, count, side, ink, INK, INK, 0.0, pixel, dist, sink);
                int rim = scale(color, 0.6 + 0.3 * bright);
                int middle = mix(scale(color, bright), 0xFFFFFFFF, 0.12 + 0.25 * flash);
                tube(p, count, side, core, rim, middle, 1.0, pixel, dist, sink);
                int hot = mix(color, 0xFFFFFFFF, 0.6 + 0.3 * flash);
                if (heart[middleAt] / (pixel * dist[middleAt]) >= 0.6) {
                    tube(p, count, side, heart, hot, hot, 2.0, pixel, dist, sink);
                }
                if (corePx >= 1.2) {
                    spurs(bolt, p, count, core, age, fade, rim, middle, glowColor, pixel, sink);
                }
                if (grow < 1.0) {
                    // the spark leading the strike
                    double r = core[count - 1] * 6.0 + 0.07;
                    disc(p, count - 1, side, r, scale(mix(color, 0xFFFFFFFF, 0.55), shine), 0, true, 0.0, pixel, dist[count - 1], sink);
                }
            }
        }
    }

    /**
     * The crackle around a live bolt: short spurs that flick out of its kinks, a new set every two ticks (most kinks bare), each a
     * tiny bolt of its own (outline and core) thinning to nothing.
     */
    static void spurs(Bolt bolt, double[] p, int count, double[] core, double age, double fade, int rim, int middle, int glowColor,
                      double pixel, Sink sink) {
        if (fade < 0.35 || count < 3) {
            return;
        }
        long frame = (long) Math.floor(Math.max(0.0, age) / 2.0);
        SplittableRandom random = new SplittableRandom(bolt.seed ^ 0x5350_5552L ^ frame * 0xD1B54A32D192ED03L);
        int points = 4;
        double[] spur = new double[points * 3];
        double[] half = new double[points];
        double[] ink = new double[points];
        double[] halo = new double[points];
        double[] dist = new double[points];
        for (int i = 1; i + 1 < count; i++) {
            double roll = random.nextDouble();
            double length = (0.5 + 1.2 * random.nextDouble()) * (core[i] * 6.0 + 0.04);
            double angle = random.nextDouble() * Math.PI * 2.0;
            long kinks = random.nextLong();
            if (roll > 0.2) {
                continue;
            }
            double tx = p[(i + 1) * 3] - p[(i - 1) * 3];
            double ty = p[(i + 1) * 3 + 1] - p[(i - 1) * 3 + 1];
            double tz = p[(i + 1) * 3 + 2] - p[(i - 1) * 3 + 2];
            double tl = Math.max(1.0E-9, Math.sqrt(tx * tx + ty * ty + tz * tz));
            double[] across = Jag.across(tx, ty, tz);
            double ox = across[0] * Math.cos(angle) + across[3] * Math.sin(angle);
            double oy = across[1] * Math.cos(angle) + across[4] * Math.sin(angle);
            double oz = across[2] * Math.cos(angle) + across[5] * Math.sin(angle);
            // out at a slant along the bolt, crackling as it goes
            double dx = ox + tx / tl * 0.6;
            double dy = oy + ty / tl * 0.6;
            double dz = oz + tz / tl * 0.6;
            double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double[] a = {p[i * 3], p[i * 3 + 1], p[i * 3 + 2]};
            double[] b = {a[0] + dx / dl * length, a[1] + dy / dl * length, a[2] + dz / dl * length};
            double[] path = Bolt.path(a, b, 2, 0.3, kinks);
            for (int k = 0; k < points; k++) {
                int from = k < 3 ? k : 4;
                spur[k * 3] = path[from * 3];
                spur[k * 3 + 1] = path[from * 3 + 1];
                spur[k * 3 + 2] = path[from * 3 + 2];
                dist[k] = Math.sqrt(spur[k * 3] * spur[k * 3] + spur[k * 3 + 1] * spur[k * 3 + 1] + spur[k * 3 + 2] * spur[k * 3 + 2]);
                double t = k / (double) (points - 1);
                half[k] = Math.max(core[i] * 0.36 * (1.0 - 0.75 * t), 0.7 * pixel * dist[k] * fade);
                ink[k] = half[k] + (0.3 * half[k] + 0.9 * pixel * dist[k]) * Math.min(1.0, fade * 1.6);
                halo[k] = half[k] * 3.0 + 0.012;
            }
            double[] side = sides(spur, points);
            glow(spur, points, side, halo, scale(glowColor, 0.55), sink);
            tube(spur, points, side, ink, INK, INK, 0.0, pixel, dist, sink);
            tube(spur, points, side, half, middle, middle, 1.0, pixel, dist, sink);
        }
    }

    /** Ticks a landing's flash lasts on the ground, and its scorch. */
    public static final double LANDING_FLASH = 7.0;
    public static final double SCORCH = 40.0;

    /**
     * 2.1.5: where a bolt's free end strikes the ground, at {@code (x, y, z)} on its surface (camera space), {@code age} ticks after it
     * landed: a flash of its colour spreading on the ground (additive, a hot white middle), fading fast, and a dark scorch mark that
     * lingers ({@link #SCORCH} ticks; translucent, so the ground shows through as it fades). {@code size} about 1 for a Conqueror bolt.
     */
    public static void landing(double x, double y, double z, double age, int color, double size, double shine, Sink sink) {
        if (age < 0 || age > SCORCH) {
            return;
        }
        if (age <= LANDING_FLASH) {
            double k = 1.0 - age / LANDING_FLASH;
            double spread = size * (0.7 + 0.9 * Math.min(1.0, age / 2.5));
            flat(x, y + 0.03, z, spread, scale(color, 1.0 * k * k * shine), color & 0xFF000000, true, sink);
            if (age < 4.0) {
                double hot = 1.0 - age / 4.0;
                flat(x, y + 0.035, z, size * 0.32, scale(mix(color, 0xFFFFFFFF, 0.6), hot * shine), color & 0xFF000000, true, sink);
            }
        }
        double fade = 1.0 - age / SCORCH;
        int ink = mix(INK, color, 0.12) & 0xFFFFFF;
        int centre = (int) Math.round(0.72 * fade * 255) << 24 | ink;
        flat(x, y + 0.015, z, size * 0.52, centre, ink, false, sink);
    }

    /** Ticks the full-power shockwave takes to sweep out. */
    public static final double WAVE = 14.0;

    /**
     * 2.1.7: the full-power release's shockwave, {@code age} ticks after it, centred at {@code (x, y, z)} (camera space, the ground under
     * the player): a flat ring racing out to {@code reach} (fast, then easing), its front an inked line with a red edge, a band of red
     * light trailing it and fading behind, all of it thinning away as it reaches the end.
     */
    public static void ring(double x, double y, double z, double age, double reach, int color, double shine, Sink sink) {
        if (age < 0 || age > WAVE) {
            return;
        }
        double t = age / WAVE;
        double radius = reach * (1.0 - Math.pow(1.0 - t, 2.5));
        double fade = t < 0.6 ? 1.0 : 1.0 - (t - 0.6) / 0.4;
        double band = 2.0 + radius * 0.12;
        int segments = 96;
        int none = color & 0xFF000000;
        int light = scale(color, 0.8 * fade * shine);
        double line = (0.12 + radius * 0.004) * (0.4 + 0.6 * fade);
        for (int k = 0; k < segments; k++) {
            double a0 = Math.PI * 2.0 * k / segments;
            double a1 = Math.PI * 2.0 * (k + 1) / segments;
            double c0 = Math.cos(a0);
            double s0 = Math.sin(a0);
            double c1 = Math.cos(a1);
            double s1 = Math.sin(a1);
            double inner = Math.max(0.0, radius - band);
            // the light trailing the front
            emit(true, new double[] {x + c0 * inner, y + 0.08, z + s0 * inner, x + c1 * inner, y + 0.08, z + s1 * inner, x + c1 * radius, y + 0.08,
                z + s1 * radius, x + c0 * radius, y + 0.08, z + s0 * radius}, new int[] {none, none, light, light}, sink);
            // the front: ink, and a red edge just inside it
            double out = radius + line;
            emit(false, new double[] {x + c0 * radius, y + 0.1, z + s0 * radius, x + c1 * radius, y + 0.1, z + s1 * radius, x + c1 * out, y + 0.1,
                z + s1 * out, x + c0 * out, y + 0.1, z + s0 * out}, new int[] {INK, INK, INK, INK}, sink);
            double in = Math.max(0.0, radius - line * 0.8);
            int edge = scale(color, Math.min(1.0, 0.7 + 0.3 * fade));
            emit(false, new double[] {x + c0 * in, y + 0.105, z + s0 * in, x + c1 * in, y + 0.105, z + s1 * in, x + c1 * radius, y + 0.105,
                z + s1 * radius, x + c0 * radius, y + 0.105, z + s0 * radius}, new int[] {edge, edge, edge, edge}, sink);
        }
    }

    /** A flat disc on the ground (twelve wedges), {@code centre} in its middle, {@code edge} at its rim. */
    static void flat(double x, double y, double z, double radius, int centre, int edge, boolean glow, Sink sink) {
        int n = 12;
        double[] rim = new double[n * 3];
        for (int k = 0; k < n; k++) {
            double a = Math.PI * 2.0 * k / n;
            rim[k * 3] = x + Math.cos(a) * radius;
            rim[k * 3 + 1] = y;
            rim[k * 3 + 2] = z + Math.sin(a) * radius;
        }
        int[] c = {centre, edge, edge, edge};
        for (int k = 0; k < n; k += 2) {
            double[] v = {x, y, z, rim[k * 3], rim[k * 3 + 1], rim[k * 3 + 2], rim[((k + 1) % n) * 3], rim[((k + 1) % n) * 3 + 1],
                rim[((k + 1) % n) * 3 + 2], rim[((k + 2) % n) * 3], rim[((k + 2) % n) * 3 + 1], rim[((k + 2) % n) * 3 + 2]};
            emit(glow, v, c, sink);
        }
    }

    /** Unit vectors across the path at each point, square to it and to the line of sight (3 per point). */
    static double[] sides(double[] p, int count) {
        double[] side = new double[count * 3];
        for (int i = 0; i < count; i++) {
            int a = Math.max(0, i - 1);
            int b = Math.min(count - 1, i + 1);
            double tx = p[b * 3] - p[a * 3];
            double ty = p[b * 3 + 1] - p[a * 3 + 1];
            double tz = p[b * 3 + 2] - p[a * 3 + 2];
            // the eye is at the origin: the line of sight to the point is the point itself
            double vx = p[i * 3];
            double vy = p[i * 3 + 1];
            double vz = p[i * 3 + 2];
            double sx = ty * vz - tz * vy;
            double sy = tz * vx - tx * vz;
            double sz = tx * vy - ty * vx;
            double l = Math.sqrt(sx * sx + sy * sy + sz * sz);
            if (l < 1.0E-9) {
                // looking straight down the bolt: any direction across the sight will do
                double[] across = Jag.across(vx, vy, vz);
                sx = across[0];
                sy = across[1];
                sz = across[2];
                l = 1.0;
            }
            sx /= l;
            sy /= l;
            sz /= l;
            if (i > 0 && sx * side[i * 3 - 3] + sy * side[i * 3 - 2] + sz * side[i * 3 - 1] < 0) {
                // keep turning the same way along the path, or the ribbon twists through itself
                sx = -sx;
                sy = -sy;
                sz = -sz;
            }
            side[i * 3] = sx;
            side[i * 3 + 1] = sy;
            side[i * 3 + 2] = sz;
        }
        return side;
    }

    /**
     * How much wider the ribbon must be at each point to keep its full width across a kink (the side there is square to the mean of
     * the two pieces, not to either): 1 on a straight run, up to {@link #MITRE} at a sharp bend.
     */
    static double[] mitres(double[] p, int count, double[] side) {
        double[] m = new double[count];
        for (int i = 0; i < count; i++) {
            int a = i == 0 ? 0 : i - 1;
            int b = i == 0 ? 1 : i;
            double dx = p[b * 3] - p[a * 3];
            double dy = p[b * 3 + 1] - p[a * 3 + 1];
            double dz = p[b * 3 + 2] - p[a * 3 + 2];
            double l = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (l < 1.0E-9) {
                m[i] = 1.0;
                continue;
            }
            double along = (side[i * 3] * dx + side[i * 3 + 1] * dy + side[i * 3 + 2] * dz) / l;
            m[i] = Math.min(MITRE, 1.0 / Math.sqrt(Math.max(1.0E-6, 1.0 - along * along)));
        }
        return m;
    }

    /** An opaque tube: the ribbon (rims {@code rim}, middle {@code middle}) and a round disc at every point. */
    static void tube(double[] p, int count, double[] side, double[] half, int rim, int middle, double layer, double pixel, double[] dist,
                     Sink sink) {
        double[] v = new double[12];
        int[] c = new int[4];
        double[] m = mitres(p, count, side);
        for (int i = 0; i + 1 < count; i++) {
            double pushA = push(layer, dist[i]);
            double pushB = push(layer, dist[i + 1]);
            double ha = half[i] * m[i];
            double hb = half[i + 1] * m[i + 1];
            // shaded across only where it is wide enough on screen to show it
            boolean shaded = rim != middle && Math.max(half[i] / (pixel * dist[i]), half[i + 1] / (pixel * dist[i + 1])) > 2.5;
            if (!shaded) {
                corner(p, side, i, -ha, pushA, dist[i], v, 0);
                corner(p, side, i + 1, -hb, pushB, dist[i + 1], v, 3);
                corner(p, side, i + 1, hb, pushB, dist[i + 1], v, 6);
                corner(p, side, i, ha, pushA, dist[i], v, 9);
                c[0] = middle;
                c[1] = middle;
                c[2] = middle;
                c[3] = middle;
                emit(false, v, c, sink);
            } else {
                for (int s = -1; s <= 1; s += 2) {
                    corner(p, side, i, s * ha, pushA, dist[i], v, 0);
                    corner(p, side, i + 1, s * hb, pushB, dist[i + 1], v, 3);
                    corner(p, side, i + 1, 0, pushB, dist[i + 1], v, 6);
                    corner(p, side, i, 0, pushA, dist[i], v, 9);
                    c[0] = rim;
                    c[1] = rim;
                    c[2] = middle;
                    c[3] = middle;
                    emit(false, v, c, sink);
                }
            }
        }
        for (int i = 0; i < count; i++) {
            // the mitred ribbon already closes gentle bends: a disc only rounds the ends and the sharp kinks (where the mitre gives out)
            double px = half[i] / (pixel * dist[i]);
            if (px < 1.2 || i > 0 && i < count - 1 && (m[i] < MITRE * 0.8 || px < 1.6)) {
                continue;
            }
            disc(p, i, side, half[i], middle, rim, false, layer, pixel, dist[i], sink);
        }
    }

    /**
     * 2.2.5: two bands along the path, from {@code inner} to {@code outer} each side of it ({@code near} at the inner edge, {@code far}
     * at the outer), mitred at its joints: an outline round a body without anything behind the body.
     */
    static void band(double[] p, int count, double[] side, double[] inner, double[] outer, int near, int far, double layer, double[] dist,
                     Sink sink) {
        double[] v = new double[12];
        int[] c = new int[4];
        double[] m = mitres(p, count, side);
        for (int i = 0; i + 1 < count; i++) {
            double pushA = push(layer, dist[i]);
            double pushB = push(layer, dist[i + 1]);
            for (int s = -1; s <= 1; s += 2) {
                corner(p, side, i, s * inner[i] * m[i], pushA, dist[i], v, 0);
                corner(p, side, i + 1, s * inner[i + 1] * m[i + 1], pushB, dist[i + 1], v, 3);
                corner(p, side, i + 1, s * outer[i + 1] * m[i + 1], pushB, dist[i + 1], v, 6);
                corner(p, side, i, s * outer[i] * m[i], pushA, dist[i], v, 9);
                c[0] = near;
                c[1] = near;
                c[2] = far;
                c[3] = far;
                emit(false, v, c, sink);
            }
        }
    }

    /** 2.2.5: a glow in two bands, {@code color} at {@code inner} fading to nothing at {@code outer}, each side of the path. */
    static void glowBand(double[] p, int count, double[] side, double[] inner, double[] outer, int color, Sink sink) {
        if ((color & 0xFFFFFF) == 0) {
            return;
        }
        double[] v = new double[12];
        int[] c = new int[4];
        int none = color & 0xFF000000;
        double[] m = mitres(p, count, side);
        for (int i = 0; i + 1 < count; i++) {
            for (int s = -1; s <= 1; s += 2) {
                corner(p, side, i, s * inner[i] * m[i], -0.02, 1.0, v, 0);
                corner(p, side, i + 1, s * inner[i + 1] * m[i + 1], -0.02, 1.0, v, 3);
                corner(p, side, i + 1, s * outer[i + 1] * m[i + 1], -0.02, 1.0, v, 6);
                corner(p, side, i, s * outer[i] * m[i], -0.02, 1.0, v, 9);
                c[0] = color;
                c[1] = color;
                c[2] = none;
                c[3] = none;
                emit(true, v, c, sink);
            }
        }
    }

    /** An additive halo along the path: {@code color} along the bolt, nothing at the rims. */
    static void glow(double[] p, int count, double[] side, double[] half, int color, Sink sink) {
        if ((color & 0xFFFFFF) == 0) {
            return;
        }
        double[] v = new double[12];
        int[] c = new int[4];
        int none = color & 0xFF000000;
        double[] m = mitres(p, count, side);
        for (int i = 0; i + 1 < count; i++) {
            for (int s = -1; s <= 1; s += 2) {
                corner(p, side, i, s * half[i] * m[i], -0.02, 1.0, v, 0);
                corner(p, side, i + 1, s * half[i + 1] * m[i + 1], -0.02, 1.0, v, 3);
                corner(p, side, i + 1, 0, -0.02, 1.0, v, 6);
                corner(p, side, i, 0, -0.02, 1.0, v, 9);
                c[0] = none;
                c[1] = none;
                c[2] = color;
                c[3] = color;
                emit(true, v, c, sink);
            }
        }
        cap(p, 0, 1, side, half[0] * m[0], color, sink);
        cap(p, count - 1, count - 2, side, half[count - 1] * m[count - 1], color, sink);
    }

    /** A glow's rounded end at point {@code i}: a half disc facing away from point {@code from}, fading to nothing at its rim. */
    static void cap(double[] p, int i, int from, double[] side, double radius, int color, Sink sink) {
        if (radius <= 0 || from < 0 || from == i) {
            return;
        }
        double px = p[i * 3];
        double py = p[i * 3 + 1];
        double pz = p[i * 3 + 2];
        double l = Math.max(1.0E-9, Math.sqrt(px * px + py * py + pz * pz));
        double back = 1.0 + 0.02 / l;
        double cx = px * back;
        double cy = py * back;
        double cz = pz * back;
        double ux = side[i * 3];
        double uy = side[i * 3 + 1];
        double uz = side[i * 3 + 2];
        double wx = py / l * uz - pz / l * uy;
        double wy = pz / l * ux - px / l * uz;
        double wz = px / l * uy - py / l * ux;
        double ox = px - p[from * 3];
        double oy = py - p[from * 3 + 1];
        double oz = pz - p[from * 3 + 2];
        if (wx * ox + wy * oy + wz * oz < 0) {
            wx = -wx;
            wy = -wy;
            wz = -wz;
        }
        double[] rim = new double[5 * 3];
        for (int k = 0; k <= 4; k++) {
            double a = Math.PI * k / 4.0;
            double ca = Math.cos(a) * radius;
            double sa = Math.sin(a) * radius;
            rim[k * 3] = cx + ux * ca + wx * sa;
            rim[k * 3 + 1] = cy + uy * ca + wy * sa;
            rim[k * 3 + 2] = cz + uz * ca + wz * sa;
        }
        int none = color & 0xFF000000;
        int[] c = {color, none, none, none};
        for (int k = 0; k < 4; k += 2) {
            double[] v = {cx, cy, cz, rim[k * 3], rim[k * 3 + 1], rim[k * 3 + 2], rim[(k + 1) * 3], rim[(k + 1) * 3 + 1], rim[(k + 1) * 3 + 2],
                rim[(k + 2) * 3], rim[(k + 2) * 3 + 1], rim[(k + 2) * 3 + 2]};
            emit(true, v, c, sink);
        }
    }

    /** A round disc facing the eye at point {@code i}: {@code centre} in its middle, {@code edge} at its rim. */
    static void disc(double[] p, int i, double[] side, double radius, int centre, int edge, boolean glow, double layer, double pixel,
                     double dist, Sink sink) {
        if (radius <= 0) {
            return;
        }
        double push = glow ? -0.02 : push(layer, dist);
        double px = p[i * 3];
        double py = p[i * 3 + 1];
        double pz = p[i * 3 + 2];
        double l = Math.max(1.0E-9, dist);
        double back = 1.0 - push / l;
        double cx = px * back;
        double cy = py * back;
        double cz = pz * back;
        // the disc's plane: across (the side) and the side turned about the line of sight
        double ux = side[i * 3];
        double uy = side[i * 3 + 1];
        double uz = side[i * 3 + 2];
        double vx = py / l * uz - pz / l * uy;
        double vy = pz / l * ux - px / l * uz;
        double vz = px / l * uy - py / l * ux;
        double[] rim = new double[ROUND * 3];
        for (int k = 0; k < ROUND; k++) {
            double a = Math.PI * 2.0 * k / ROUND;
            double ca = Math.cos(a) * radius;
            double sa = Math.sin(a) * radius;
            rim[k * 3] = cx + ux * ca + vx * sa;
            rim[k * 3 + 1] = cy + uy * ca + vy * sa;
            rim[k * 3 + 2] = cz + uz * ca + vz * sa;
        }
        double[] v = new double[12];
        int[] c = {centre, edge, edge, edge};
        for (int k = 0; k < ROUND; k += 2) {
            v[0] = cx;
            v[1] = cy;
            v[2] = cz;
            for (int j = 0; j < 3; j++) {
                int r = (k + j) % ROUND;
                v[3 + j * 3] = rim[r * 3];
                v[4 + j * 3] = rim[r * 3 + 1];
                v[5 + j * 3] = rim[r * 3 + 2];
            }
            emit(glow, v, c, sink);
        }
    }

    /** How far a layer is drawn toward the eye so it sits on the one under it: a little more far away, where depth is coarse. */
    static double push(double layer, double dist) {
        return layer * (0.0016 + 0.0011 * dist);
    }

    static void corner(double[] p, double[] side, int i, double offset, double push, double dist, double[] out, int at) {
        double px = p[i * 3];
        double py = p[i * 3 + 1];
        double pz = p[i * 3 + 2];
        double l = Math.max(1.0E-9, Math.sqrt(px * px + py * py + pz * pz));
        double back = 1.0 - push / l;
        out[at] = px * back + side[i * 3] * offset;
        out[at + 1] = py * back + side[i * 3 + 1] * offset;
        out[at + 2] = pz * back + side[i * 3 + 2] * offset;
    }

    /**
     * A quad, wound to face the eye (counter-clockwise as seen from the origin), whichever way it was built. A quad folded across a
     * sharp kink (its two triangles facing opposite ways) goes as two triangles, each wound to face the eye, so no half of it is
     * culled into a hole.
     */
    static void emit(boolean glow, double[] v, int[] c, Sink sink) {
        double first = facing(v, 0, 1, 2);
        double second = facing(v, 0, 2, 3);
        if (first >= 0 && second >= 0) {
            sink.quad(glow, v.clone(), c.clone());
        } else if (first <= 0 && second <= 0) {
            sink.quad(glow, new double[] {v[0], v[1], v[2], v[9], v[10], v[11], v[6], v[7], v[8], v[3], v[4], v[5]},
                new int[] {c[0], c[3], c[2], c[1]});
        } else {
            triangle(glow, v, c, 0, 1, 2, first, sink);
            triangle(glow, v, c, 0, 2, 3, second, sink);
        }
    }

    /** One triangle as a quad with its last corner doubled, wound to face the eye. */
    static void triangle(boolean glow, double[] v, int[] c, int a, int b, int d, double facing, Sink sink) {
        int p = facing >= 0 ? b : d;
        int q = facing >= 0 ? d : b;
        sink.quad(glow, new double[] {v[a * 3], v[a * 3 + 1], v[a * 3 + 2], v[p * 3], v[p * 3 + 1], v[p * 3 + 2], v[q * 3], v[q * 3 + 1],
            v[q * 3 + 2], v[q * 3], v[q * 3 + 1], v[q * 3 + 2]}, new int[] {c[a], c[p], c[q], c[q]});
    }

    /** Positive when the triangle i, j, k turns counter-clockwise seen from the origin. */
    static double facing(double[] v, int i, int j, int k) {
        double ax = v[j * 3] - v[i * 3];
        double ay = v[j * 3 + 1] - v[i * 3 + 1];
        double az = v[j * 3 + 2] - v[i * 3 + 2];
        double bx = v[k * 3] - v[i * 3];
        double by = v[k * 3 + 1] - v[i * 3 + 1];
        double bz = v[k * 3 + 2] - v[i * 3 + 2];
        double nx = ay * bz - az * by;
        double ny = az * bx - ax * bz;
        double nz = ax * by - ay * bx;
        return -(nx * v[i * 3] + ny * v[i * 3 + 1] + nz * v[i * 3 + 2]);
    }

    /** The length of a bolt's path at rest. */
    static double length(Bolt bolt) {
        double total = 0;
        for (int i = 1; i < bolt.count(); i++) {
            total += distance(bolt.points, i - 1, i);
        }
        return total;
    }

    static double distance(double[] p, int a, int b) {
        double dx = p[b * 3] - p[a * 3];
        double dy = p[b * 3 + 1] - p[a * 3 + 1];
        double dz = p[b * 3 + 2] - p[a * 3 + 2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** Ticks the Conqueror's centre flash lasts. */
    public static final double BURST = 18.0;

    /**
     * 2.2.5: the flash where the player stands at the Conqueror's full-power release (the owner's anime frames: a blinding pink-white
     * light round the tiny figure, the bolts tearing out of it): a disc facing the eye at (x, y, z) (camera space), {@code radius}
     * blocks, white-hot in the middle through the colour to nothing at its rim; it swells in two ticks and dies over {@link #BURST}.
     */
    public static void burst(double x, double y, double z, double age, double radius, int color, double shine, Sink sink) {
        if (age < 0 || age > BURST) {
            return;
        }
        double grow = Math.min(1.0, (age + 0.5) / 2.5);
        double life = age < 1.5 ? 1.0 : Math.pow(1.0 - (age - 1.5) / (BURST - 1.5), 1.6);
        double r = radius * (0.5 + 0.5 * grow);
        double l = Math.max(1.0E-6, Math.sqrt(x * x + y * y + z * z));
        // the disc's axes: square to the line of sight
        double[] u = {-z, 0, x};
        double ul = Math.hypot(u[0], u[2]);
        u = ul < 1.0E-6 ? new double[] {1, 0, 0} : new double[] {u[0] / ul, 0, u[2] / ul};
        double[] v = {y / l * u[2] - z / l * u[1], z / l * u[0] - x / l * u[2], x / l * u[1] - y / l * u[0]};
        int sides = 24;
        double[] rings = {0.0, 0.3, 0.62, 1.0};
        int[] colours = {scale(mix(color, 0xFFFFFFFF, 0.9), life * shine), scale(mix(color, 0xFFFFFFFF, 0.7), life * shine),
            scale(mix(color, 0xFFFFFFFF, 0.4), life * 0.65 * shine), 0};
        double[] q = new double[12];
        int[] c = new int[4];
        for (int ring = 0; ring + 1 < rings.length; ring++) {
            for (int k = 0; k < sides; k++) {
                double a0 = Math.PI * 2.0 * k / sides;
                double a1 = Math.PI * 2.0 * (k + 1) / sides;
                double[][] corners = {{rings[ring], a0}, {rings[ring + 1], a0}, {rings[ring + 1], a1}, {rings[ring], a1}};
                for (int j = 0; j < 4; j++) {
                    double rr = corners[j][0] * r;
                    double ca = Math.cos(corners[j][1]) * rr;
                    double sa = Math.sin(corners[j][1]) * rr;
                    q[j * 3] = x + u[0] * ca + v[0] * sa;
                    q[j * 3 + 1] = y + u[1] * ca + v[1] * sa;
                    q[j * 3 + 2] = z + u[2] * ca + v[2] * sa;
                }
                c[0] = colours[ring];
                c[1] = colours[ring + 1];
                c[2] = colours[ring + 1];
                c[3] = colours[ring];
                emit(true, q, c, sink);
            }
        }
    }

    /** {@code argb}'s colour times {@code k} (clamped), opaque. */
    public static int scale(int argb, double k) {
        int r = (int) Math.min(255, Math.max(0, Math.round(((argb >>> 16) & 255) * k)));
        int g = (int) Math.min(255, Math.max(0, Math.round(((argb >>> 8) & 255) * k)));
        int b = (int) Math.min(255, Math.max(0, Math.round((argb & 255) * k)));
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    /** {@code a} toward {@code b} by {@code t}, opaque. */
    public static int mix(int a, int b, double t) {
        double k = Math.max(0.0, Math.min(1.0, t));
        int r = (int) Math.round(((a >>> 16) & 255) * (1 - k) + ((b >>> 16) & 255) * k);
        int g = (int) Math.round(((a >>> 8) & 255) * (1 - k) + ((b >>> 8) & 255) * k);
        int bl = (int) Math.round((a & 255) * (1 - k) + (b & 255) * k);
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }
}
