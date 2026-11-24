package com.hexhaki.piece.haki;

import java.util.SplittableRandom;

/**
 * 2.1.5: a {@link Bolt}'s geometry for one frame (pure: the game feeds it to its buffers, {@code tools/preview/BoltPreview} rasterises
 * the very same quads to judge them).
 *
 * <p>Everything is in camera space (the eye at the origin, so the numbers stay small and the floats exact).
 *
 * <p>2.2.6 (the owner: "THE haki lightning has to be more 3d with black outlines, all of it, literally all of it", and "I dont like the
 * ending tips of the lightning it's like they're stopping in a square"): every stroke is a real tube now, not a flat ribbon turned to the
 * eye. Its near half is built round its path (rings of up to nine points, mitred at the joints, a dome on its ends and sharp kinks) and
 * lit by a key light from above and to the left of the view ({@link #light}): a shadow side, a lit side, a hard highlight, and for the
 * coloured core a white-hot middle where it faces the eye. Round every one of them, a black ink outline (a ribbon behind it, wider by
 * about a pixel and a half at any distance, coming to a point past a pointed end). The kinds:
 * <ol>
 *   <li><b>body</b>: the original's near-black strokes: a black tube, glossy, edge-lit in its colour (the anime's black lightning);</li>
 *   <li><b>core</b>: the colour, lifted out of the body toward the eye ({@link #CORE_LIFT}), its own ink, a halo and spurs;</li>
 *   <li><b>heart</b>: the core's own white-hot middle (nothing of its own);</li>
 *   <li><b>edge</b>: the storm's coloured rim round its huge black bolts, inked outside, glowing;</li>
 *   <li><b>aura</b>: a faint glow only.</li>
 * </ol>
 * In time: the bolt races out from its root behind a sharp point (a hot spark leads it), flashes wide and white the moment it lands,
 * flickers tick by tick, dances into a new crackle every two ticks, and in the last part of its life thins away, core first, outline
 * last. A free end always comes to a point, never a square.
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
    /**
     * 2.2.6: how many of its radii a core's path is lifted toward the eye: a body round it is up to 1.6 times as wide (and {@link #DEPTH}
     * as deep), and the core's whole near half (to its rims) must stand in front of the body's surface.
     */
    static final double CORE_LIFT = 0.8;
    /**
     * 2.2.6: how deep a tube is toward the eye, as a share of its width (lit as a round tube all the same): a shallower tube pokes less
     * through what it passes behind (a body, a wall) while it shades exactly as a round one.
     */
    static final double DEPTH = 0.5;
    /** 2.2.6: the key light in view space (right, up, toward the eye): above and to the left, a little in front. */
    public static final double[] VIEW_LIGHT = unit(-0.42, 0.78, 0.46);
    /** The free end of a stroke is pointed when its last width is under this share of its widest. */
    static final double POINTED = 0.15;

    /** The key light in the frame the quads are built in (unit, toward the light); set by the caller for each frame it draws. */
    private static double lightX = VIEW_LIGHT[0];
    private static double lightY = VIEW_LIGHT[1];
    private static double lightZ = VIEW_LIGHT[2];

    /** Where the geometry goes: {@code glow} quads are blended light that writes no colour of its own; the others are opaque. */
    public interface Sink {
        void quad(boolean glow, double[] xyz, int[] argb);
    }

    /**
     * 2.2.6: how a tube is coloured at a point of its surface: {@code facing} 1 where it faces the eye, 0 edge-on; {@code lambert} the
     * cosine to the key light (-1..1); {@code spec} the highlight (0..1).
     */
    interface Shade {
        int at(double facing, double lambert, double spec);
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

        public void place(double[] local, double[] out, int at) {
            double lx = local[0];
            double lz = local[2];
            out[at] = this.x + lx * this.cos + lz * this.sin;
            out[at + 1] = this.y + local[1];
            out[at + 2] = this.z - lx * this.sin + lz * this.cos;
        }
    }

    private BoltMesh() {
    }

    /**
     * 2.2.6: the key light for what is built next, as a direction in its frame (toward the light; any length): the caller turns
     * {@link #VIEW_LIGHT} into the frame it builds in, so the light always comes from the same side of the screen.
     */
    public static void light(double x, double y, double z) {
        double[] u = unit(x, y, z);
        lightX = u[0];
        lightY = u[1];
        lightZ = u[2];
    }

    static double[] unit(double x, double y, double z) {
        double l = Math.sqrt(x * x + y * y + z * z);
        return l < 1.0E-9 ? new double[] {0, 1, 0} : new double[] {x / l, y / l, z / l};
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
    /**
     * 2.5.2 (the owner's 2.4.4 "Conqueror Haki full release lags a ton"): a stroke wholly outside the view is not built. The half-angle
     * round the view's forward that is drawn, kept wider than any field of view the game shows (110 degrees, sped up by a sprint, on an
     * ultrawide screen reaches about 76 degrees at the corners).
     */
    public static final double VIEW_HALF = Math.toRadians(80.0);

    /**
     * True if a sphere at {@code x, y, z} from the eye, {@code r} round, can show in a view looking along the unit {@code fx, fy, fz}
     * (any part of it within {@link #VIEW_HALF} of the forward, or the eye inside it).
     */
    public static boolean seen(double x, double y, double z, double r, double fx, double fy, double fz) {
        double d = Math.sqrt(x * x + y * y + z * z);
        if (d <= r) {
            return true;
        }
        double cos = Math.max(-1.0, Math.min(1.0, (x * fx + y * fy + z * fz) / d));
        return Math.acos(cos) - Math.asin(r / d) <= VIEW_HALF;
    }

    /**
     * 3.4.8 (the owner: "optimize the mod as much as you can"): the arrays one drawing works with, handed out from a pool and given back
     * all at once when the next drawing starts ({@link Scratch#reset} at the head of {@link #build}, {@link #landing}, {@link #ring} and
     * {@link #burst}): each comes zeroed over the length asked for, as a new one would. Until now every bolt allocated some 30 arrays a
     * frame and every quad two more (thousands a frame through a release: garbage the phone's collector stuttered over). The render
     * thread only (the preview tools run on one thread too).
     */
    static final class Scratch {
        double[][] doubles = new double[64][];
        float[][] floats = new float[16][];
        int[][] ints = new int[32][];
        int d;
        int f;
        int i;

        void reset() {
            this.d = 0;
            this.f = 0;
            this.i = 0;
        }

        double[] d(int n) {
            if (this.d == this.doubles.length) {
                this.doubles = java.util.Arrays.copyOf(this.doubles, this.d * 2);
            }
            double[] a = this.doubles[this.d];
            if (a == null || a.length < n) {
                a = new double[Math.max(n, 32)];
                this.doubles[this.d] = a;
            } else {
                java.util.Arrays.fill(a, 0, n, 0.0);
            }
            this.d++;
            return a;
        }

        float[] f(int n) {
            if (this.f == this.floats.length) {
                this.floats = java.util.Arrays.copyOf(this.floats, this.f * 2);
            }
            float[] a = this.floats[this.f];
            if (a == null || a.length < n) {
                a = new float[Math.max(n, 32)];
                this.floats[this.f] = a;
            } else {
                java.util.Arrays.fill(a, 0, n, 0.0F);
            }
            this.f++;
            return a;
        }

        int[] i(int n) {
            if (this.i == this.ints.length) {
                this.ints = java.util.Arrays.copyOf(this.ints, this.i * 2);
            }
            int[] a = this.ints[this.i];
            if (a == null || a.length < n) {
                a = new int[Math.max(n, 32)];
                this.ints[this.i] = a;
            } else {
                java.util.Arrays.fill(a, 0, n, 0);
            }
            this.i++;
            return a;
        }
    }

    static final Scratch S = new Scratch();
    static final double[] WOUND_V = new double[12];
    static final int[] WOUND_C = new int[4];

    public static void build(Bolt bolt, Frame frame, double age, double pixel, Sink sink) {
        build(bolt, frame, age, pixel, 1.0, sink);
    }

    /**
     * As {@link #build(Bolt, Frame, double, double, Sink)}, the glow scaled by {@code shine}: 1 in the dark, about half under a noon
     * sky (added light washes out against a bright sky, and lightning reads by its ink there).
     */
    public static void build(Bolt bolt, Frame frame, double age, double pixel, double shine, Sink sink) {
        S.reset(); // 3.4.8: this drawing's arrays from the pool
        if (age < 0 || age > bolt.duration || bolt.kind == Bolt.HEART) {
            return; // 2.5.2: a heart draws nothing (its core is white-hot already): its points are not even placed
        }
        int full = bolt.count();
        // far away, the finest kinks are smaller than a pixel: every second (fourth ...) point is the coarser crackle they ride on
        double[] mid = S.d(3);
        bolt.dancing(full / 2, age, mid);
        double[] at = S.d(3);
        frame.place(mid, at, 0);
        double away = Math.max(0.5, Math.sqrt(at[0] * at[0] + at[1] * at[1] + at[2] * at[2]));
        double pieceLength = Math.max(1.0E-6, length(bolt) / (full - 1));
        int step = 1;
        while (pieceLength * step / (pixel * away) < 5.0 && (full - 1) % (step * 2) == 0 && (full - 1) / (step * 2) >= 2) {
            step *= 2;
        }
        int n = (full - 1) / step + 1;
        double[] all = S.d(n * 3);
        float[] widths = S.f(n);
        double[] local = S.d(3);
        float widest = 0;
        for (int i = 0; i < n; i++) {
            bolt.dancing(i * step, age, local);
            frame.place(local, all, i * 3);
            widths[i] = bolt.widths[i * step];
            widest = Math.max(widest, widths[i]);
        }
        // the strike: only the part the bolt has raced along so far
        double grow = bolt.crawl <= 0 ? 1.0 : Math.min(1.0, age / bolt.crawl);
        double[] lengths = S.d(n);
        for (int i = 1; i < n; i++) {
            lengths[i] = lengths[i - 1] + distance(all, i - 1, i);
        }
        double reach = lengths[n - 1] * grow;
        int m = 1;
        while (m < n - 1 && lengths[m] < reach) {
            m++;
        }
        double[] p = S.d((m + 1) * 3);
        float[] w = S.f(m + 1);
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
        if (grow < 1.0) {
            // 2.2.6: the racing front is a spear point, never a cut-off square
            spear(p, w, count, reach);
        }
        double life = age / bolt.duration;
        double fade = fade(life);
        double flash = flash(bolt, age);
        double flicker = flicker(bolt, age);
        double[] dist = S.d(count);
        double[] tip = S.d(count);
        for (int i = 0; i < count; i++) {
            dist[i] = Math.max(1.0E-6, Math.sqrt(p[i * 3] * p[i * 3] + p[i * 3 + 1] * p[i * 3 + 1] + p[i * 3 + 2] * p[i * 3 + 2]));
            // the least width a stroke keeps far away (a pixel or so) gives way where the stroke itself comes to a point
            tip[i] = widest <= 0 ? 1.0 : Math.min(1.0, w[i] / (0.35 * widest));
            // 3.6.0 (the owner: "JESUS WHY IS THE LIGHTNING SO THICK"): within NEAR blocks of the eye a bolt thins, so one shooting past
            // the camera (a level bolt of the release passes a third-person camera at a block or two) never walls the screen
            w[i] *= (float) near(dist[i]);
        }
        boolean pointed = widest > 0 && w[count - 1] <= POINTED * widest;
        double[] side = sides(p, count);
        int color = bolt.color;
        switch (bolt.kind) {
            case Bolt.BODY -> {
                double scale = Math.sqrt(fade) * (1.0 + 0.15 * flash);
                double[] half = S.d(count);
                double[] ink = S.d(count);
                double[] lift = S.d(count);
                double[] inkLift = S.d(count);
                for (int i = 0; i < count; i++) {
                    half[i] = Math.max(w[i] * 0.5 * BODY * scale, 1.3 * pixel * dist[i] * fade * tip[i]);
                    ink[i] = half[i] + outline(half[i], pixel, dist[i], fade) * tip[i];
                    inkLift[i] = -push(1.0, dist[i]);
                }
                int hue = hue(color);
                double bright = Math.min(1.0, flicker * (0.85 + 0.15 * fade) + 0.4 * flash);
                inked(p, count, side, ink, inkLift, pointed, pixel, dist, sink);
                // 3.6.0: a coloured body (a Haki colour whose body is not black: HakiColour.body) is shaded in its own colour
                Shade shade = coloured(color) ? colouredBodyShade(color, bright, flash) : bodyShade(hue, bright, flash);
                tube3d(p, count, side, half, lift, shade, pixel, dist, sink);
            }
            case Bolt.EDGE -> {
                // round the black body (the same width rule as BODY's), a hot line in the colour hugging it, inked outside, and a wide
                // glow beyond the ink: only bands outside the black (nothing of them lies behind the body to show through where a
                // ribbon blocks wide twists at a kink: 2.2.5's "pink facets")
                double scale = Math.sqrt(fade) * (1.0 + 0.15 * flash);
                double bright = Math.min(1.0, flicker * (0.85 + 0.15 * fade) + 0.4 * flash);
                double[] inner = S.d(count);
                double[] rim = S.d(count);
                double[] ink = S.d(count);
                double[] halo = S.d(count);
                for (int i = 0; i < count; i++) {
                    double body = Math.max(w[i] * 0.5 * BODY * scale, 1.3 * pixel * dist[i] * fade * tip[i]);
                    inner[i] = body * 0.96;
                    rim[i] = body * 1.07 + 1.2 * pixel * dist[i] * fade * tip[i];
                    ink[i] = rim[i] + (0.05 * body + 1.6 * pixel * dist[i]) * Math.min(1.0, fade * 1.6) * tip[i];
                    halo[i] = body * 2.3 + 0.12 * tip[i];
                }
                // 3.6.0: the Haki colour's own glow (Green, Cyan and Black: cyan round a green rim)
                int glowColor = scale(0xFF000000 | HakiColour.glowFor(color), (0.6 * bright * fade + 0.35 * flash) * shine);
                glowBand(p, count, side, ink, halo, glowColor, sink);
                int hot = mix(scale(color, bright), 0xFFFFFFFF, 0.3 + 0.35 * flash);
                band(p, count, side, inner, rim, hot, scale(color, 0.75 + 0.25 * bright), -0.5, dist, sink);
                band(p, count, side, rim, ink, INK, INK, -0.5, dist, sink);
            }
            case Bolt.AURA -> {
                double strength = ((color >>> 24) & 255) / 255.0 * 0.7 * fade * flicker * (1.0 + 0.6 * flash);
                double[] half = S.d(count);
                for (int i = 0; i < count; i++) {
                    half[i] = w[i] * 0.7 * (0.6 + 0.4 * fade);
                }
                // additive light cannot darken: a dark aura glows in its own hue, brought up to full
                int top = Math.max(1, Math.max((color >>> 16) & 255, Math.max((color >>> 8) & 255, color & 255)));
                glow(p, count, side, half, scale(color, strength * shine * 255.0 / top), sink);
            }
            case Bolt.HEART -> {
                // 2.2.6: nothing of its own: the original lays a heart only inside a core, and a lit 3D core is white-hot down its
                // middle already (a flat white stripe on top of it read as a sticker on the tube)
            }
            default -> {
                double scale = fade * (1.0 + 0.3 * flash);
                double bright = Math.min(1.0, flicker * (0.82 + 0.18 * fade) + 0.4 * flash);
                double[] core = S.d(count);
                double[] ink = S.d(count);
                double[] halo = S.d(count);
                double[] lift = S.d(count);
                double[] inkLift = S.d(count);
                for (int i = 0; i < count; i++) {
                    double c = Math.max(w[i] * 0.5 * CORE * scale, 0.75 * pixel * dist[i] * fade * tip[i]);
                    core[i] = c;
                    ink[i] = c + outline(c, pixel, dist[i], fade) * tip[i];
                    halo[i] = c * (4.0 + 1.5 * flash) + 0.05 * fade * tip[i];
                    lift[i] = c * CORE_LIFT;
                    inkLift[i] = lift[i] - push(1.0, dist[i]);
                }
                int glowColor = scale(color, (0.75 * bright * fade + 0.3 * flash) * shine);
                // far away a bolt is a few pixels wide: what would not show is left out (the glow under two pixels, the crackle under
                // one), so a field of distant lightning stays cheap to draw
                int middleAt = count / 2;
                double corePx = core[middleAt] / (pixel * dist[middleAt]);
                if (halo[middleAt] / (pixel * dist[middleAt]) >= 2.0) {
                    glow(p, count, side, halo, glowColor, sink);
                }
                inked(p, count, side, ink, inkLift, pointed, pixel, dist, sink);
                tube3d(p, count, side, core, lift, coreShade(color, bright, flash), pixel, dist, sink);
                if (corePx >= 1.2) {
                    spurs(bolt, p, count, core, lift, age, fade, color, bright, flash, glowColor, pixel, sink);
                }
                if (grow < 1.0) {
                    // the spark leading the strike
                    double r = core[Math.max(0, count - 2)] * 5.0 + 0.06;
                    disc(p, count - 1, side, r, scale(mix(color, 0xFFFFFFFF, 0.55), shine), 0, true, 0.0, pixel, dist[count - 1], sink);
                }
            }
        }
    }

    /** 2.2.6: the ink's width outside a tube of half width {@code half}: about a pixel and a half at any distance, more on a fat one. */
    static double outline(double half, double pixel, double dist, double fade) {
        return (0.3 * half + 1.5 * pixel * dist) * Math.min(1.0, fade * 1.6);
    }

    /**
     * 2.2.6: the racing front of a strike drawn to a spear point: the last stretch (a few of its widths, never more than two fifths of
     * what has raced out) narrows to nothing at the front.
     */
    static void spear(double[] p, float[] w, int count, double reach) {
        double front = 0;
        for (int i = Math.max(0, count - 4); i < count; i++) {
            front = Math.max(front, w[i]);
        }
        double taper = Math.min(reach * 0.4, Math.max(front * 4.0, 0.06));
        if (taper <= 0) {
            return;
        }
        double back = 0;
        w[count - 1] = 0;
        for (int i = count - 2; i >= 0; i--) {
            back += distance(p, i, i + 1);
            if (back >= taper) {
                break;
            }
            w[i] = (float) (w[i] * Math.pow(back / taper, 0.75));
        }
    }

    /**
     * 2.2.6: a black ink line behind a tube: the ribbon {@code ink} wide each side of the path (lifted toward the eye by {@code lift}),
     * round at its sharp kinks and its root, and past a pointed end drawn on to a point of its own, so the outline wraps the tip.
     */
    static void inked(double[] p, int count, double[] side, double[] ink, double[] lift, boolean pointed, double pixel, double[] dist,
                      Sink sink) {
        if (!pointed || count < 2) {
            ribbon(p, count, side, ink, lift, INK, INK, pixel, dist, true, sink);
            return;
        }
        int last = count - 1;
        double tx = p[last * 3] - p[(last - 1) * 3];
        double ty = p[last * 3 + 1] - p[(last - 1) * 3 + 1];
        double tz = p[last * 3 + 2] - p[(last - 1) * 3 + 2];
        double tl = Math.sqrt(tx * tx + ty * ty + tz * tz);
        if (tl < 1.0E-9) {
            ribbon(p, count, side, ink, lift, INK, INK, pixel, dist, true, sink);
            return;
        }
        double reach = ink[last] * 2.2;
        double[] q = java.util.Arrays.copyOf(p, (count + 1) * 3);
        q[count * 3] = p[last * 3] + tx / tl * reach;
        q[count * 3 + 1] = p[last * 3 + 1] + ty / tl * reach;
        q[count * 3 + 2] = p[last * 3 + 2] + tz / tl * reach;
        double[] h = java.util.Arrays.copyOf(ink, count + 1);
        h[count] = 0.0;
        double[] l = java.util.Arrays.copyOf(lift, count + 1);
        l[count] = lift[last];
        double[] d = java.util.Arrays.copyOf(dist, count + 1);
        d[count] = Math.max(1.0E-6, Math.sqrt(q[count * 3] * q[count * 3] + q[count * 3 + 1] * q[count * 3 + 1] + q[count * 3 + 2] * q[count * 3 + 2]));
        ribbon(q, count + 1, sides(q, count + 1), h, l, INK, INK, pixel, d, true, sink);
    }

    /** 2.2.6: a coloured core: shadowed on the far side from the light, lit, white-hot down its middle, a hard highlight. */
    static Shade coreShade(int color, double bright, double flash) {
        int lit = scale(color, bright);
        int deep = scale(color, 0.56 * bright);
        int hot = mix(color, 0xFFFFFFFF, 0.78);
        return (facing, lambert, spec) -> {
            int c = mix(deep, lit, smooth(-0.45, 0.35, lambert));
            c = mix(c, hot, 0.62 * Math.pow(facing, 5.0));
            c = mix(c, 0xFFFFFFFF, 0.85 * spec + 0.3 * flash);
            return c;
        };
    }

    /** 3.6.0: is a body stroke's colour one of the Haki colours' own bodies (bright enough not to read black)? */
    static boolean coloured(int argb) {
        int r = (argb >>> 16) & 255;
        int g = (argb >>> 8) & 255;
        int b = argb & 255;
        return Math.max(r, Math.max(g, b)) > 45;
    }

    /** 3.6.0: within this many blocks of the eye a bolt is drawn thinner ({@link #near}), to {@link #NEAR_LEAST} of its width at the eye. */
    public static final double NEAR = 8.0;
    public static final double NEAR_LEAST = 0.1;

    /** 3.6.0: how much of its width a bolt keeps {@code dist} blocks from the eye: all of it from {@link #NEAR} out. */
    public static double near(double dist) {
        return Math.max(NEAR_LEAST, Math.min(1.0, dist / NEAR));
    }

    /** 3.6.0: a coloured body: glossy in its own colour, deep where the light leaves it, edge-lit brighter. */
    static Shade colouredBodyShade(int color, double bright, double flash) {
        int deep = mix(0xFF050407, color | 0xFF000000, 0.42);
        int lit = scale(color | 0xFF000000, 0.75 + 0.25 * bright);
        int gloss = mix(color | 0xFF000000, 0xFFFFFFFF, 0.5);
        int rim = mix(color | 0xFF000000, 0xFFFFFFFF, 0.3);
        return (facing, lambert, spec) -> {
            int c = mix(deep, lit, smooth(-0.3, 0.9, lambert));
            c = mix(c, gloss, 0.6 * spec);
            c = mix(c, rim, Math.min(1.0, 0.8 * Math.pow(1.0 - facing, 4.5) + 0.2 * flash));
            return c;
        };
    }

    /** 2.2.6: the black body: glossy black, a dark sheen of its colour where the light falls, edge-lit in its colour. */
    static Shade bodyShade(int hue, double bright, double flash) {
        int black = mix(0xFF050407, hue, 0.03);
        int sheen = mix(0xFF0A090D, hue, 0.2);
        int gloss = scale(mix(hue, 0xFFFFFFFF, 0.55), 0.55);
        int rim = scale(hue, 0.9 * bright);
        return (facing, lambert, spec) -> {
            int c = mix(black, sheen, smooth(-0.1, 0.9, lambert));
            c = mix(c, gloss, 0.7 * spec);
            c = mix(c, rim, Math.min(1.0, 0.9 * Math.pow(1.0 - facing, 4.5) + 0.2 * flash));
            return c;
        };
    }

    static double smooth(double from, double to, double x) {
        double k = Math.max(0.0, Math.min(1.0, (x - from) / (to - from)));
        return k * k * (3.0 - 2.0 * k);
    }

    /** 2.2.6: a colour's hue at full brightness (a near-black body's colour is its owner's colour, very dark). */
    static int hue(int argb) {
        int r = (argb >>> 16) & 255;
        int g = (argb >>> 8) & 255;
        int b = argb & 255;
        int top = Math.max(r, Math.max(g, b));
        if (top == 0) {
            return 0xFFFF1433;
        }
        return scale(0xFF000000 | r << 16 | g << 8 | b, 255.0 / top);
    }

    /**
     * 2.2.6: the near half of a tube round the path, {@code half} its radius at each point (mitred across the joints), its path lifted
     * {@code lift} toward the eye: rings of points from one rim through the middle (nearest the eye) to the other, each coloured by
     * {@code shade} from its normal; a dome on the ends and the sharp kinks (where the mitre gives out). Hair-thin on screen it is a flat
     * ribbon of its lit colour.
     */
    static void tube3d(double[] p, int count, double[] side, double[] half, double[] lift, Shade shade, double pixel, double[] dist,
                       Sink sink) {
        double most = 0;
        for (int i = 0; i < count; i++) {
            most = Math.max(most, half[i] / (pixel * dist[i]));
        }
        if (most <= 0) {
            return;
        }
        if (most < 1.4) {
            int c = shade.at(0.8, 0.35, 0.0);
            ribbon(p, count, side, half, lift, c, c, pixel, dist, false, sink);
            return;
        }
        int k = most < 4 ? 2 : most < 10 ? 4 : most < 26 ? 6 : 8;
        // a coarse ring spreads its rim's colour over the whole of each face: its rims are lit as a little nearer the middle
        double rough = k <= 2 ? 0.5 : k <= 4 ? 0.28 : k <= 6 ? 0.12 : 0.0;
        double[] m = mitres(p, count, side);
        double[] axis = S.d(count * 3);
        double[] toward = S.d(count * 3);
        for (int i = 0; i < count; i++) {
            double back = 1.0 - lift[i] / dist[i];
            axis[i * 3] = p[i * 3] * back;
            axis[i * 3 + 1] = p[i * 3 + 1] * back;
            axis[i * 3 + 2] = p[i * 3 + 2] * back;
            int a = Math.max(0, i - 1);
            int b = Math.min(count - 1, i + 1);
            double tx = p[b * 3] - p[a * 3];
            double ty = p[b * 3 + 1] - p[a * 3 + 1];
            double tz = p[b * 3 + 2] - p[a * 3 + 2];
            double tl = Math.sqrt(tx * tx + ty * ty + tz * tz);
            double ex = -p[i * 3] / dist[i];
            double ey = -p[i * 3 + 1] / dist[i];
            double ez = -p[i * 3 + 2] / dist[i];
            double ux = ex;
            double uy = ey;
            double uz = ez;
            if (tl > 1.0E-9) {
                tx /= tl;
                ty /= tl;
                tz /= tl;
                double along = ex * tx + ey * ty + ez * tz;
                ux = ex - along * tx;
                uy = ey - along * ty;
                uz = ez - along * tz;
            }
            double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
            if (ul < 1.0E-6) {
                // looking straight down the stroke: the eye's own direction
                ux = ex;
                uy = ey;
                uz = ez;
                ul = 1.0;
            }
            toward[i * 3] = ux / ul;
            toward[i * 3 + 1] = uy / ul;
            toward[i * 3 + 2] = uz / ul;
        }
        double[] row = S.d((k + 1) * 3);
        double[] last = S.d((k + 1) * 3);
        int[] colours = S.i(k + 1);
        int[] lastColours = S.i(k + 1);
        double[] v = S.d(12);
        int[] c = S.i(4);
        double[] n = S.d(3);
        for (int i = 0; i < count; i++) {
            for (int j = 0; j <= k; j++) {
                double theta = -Math.PI / 2 + Math.PI * j / k;
                double s = Math.sin(theta);
                double co = Math.cos(theta);
                for (int a = 0; a < 3; a++) {
                    n[a] = side[i * 3 + a] * s + toward[i * 3 + a] * co;
                    row[j * 3 + a] = axis[i * 3 + a] + side[i * 3 + a] * s * half[i] * m[i] + toward[i * 3 + a] * co * half[i] * DEPTH;
                }
                colours[j] = shadeAt(shade, n, axis, i, rough);
            }
            if (i > 0) {
                for (int j = 0; j < k; j++) {
                    System.arraycopy(last, j * 3, v, 0, 3);
                    System.arraycopy(row, j * 3, v, 3, 3);
                    System.arraycopy(row, (j + 1) * 3, v, 6, 3);
                    System.arraycopy(last, (j + 1) * 3, v, 9, 3);
                    c[0] = lastColours[j];
                    c[1] = colours[j];
                    c[2] = colours[j + 1];
                    c[3] = lastColours[j + 1];
                    emit(false, v, c, sink);
                }
            }
            double[] swap = last;
            last = row;
            row = swap;
            int[] swapColours = lastColours;
            lastColours = colours;
            colours = swapColours;
        }
        for (int i = 0; i < count; i++) {
            // the mitred tube already closes gentle bends: a dome only rounds the ends and the sharp kinks
            double px = half[i] / (pixel * dist[i]);
            if (px < 1.2 || i > 0 && i < count - 1 && (m[i] < MITRE * 0.8 || px < 1.6)) {
                continue;
            }
            dome(axis, i, side, half[i], shade, px, rough, sink);
        }
    }

    /** A colour of {@code shade} for a surface normal {@code n} at the path's point {@code i}. */
    static int shadeAt(Shade shade, double[] n, double[] axis, int i, double rough) {
        double px = axis[i * 3];
        double py = axis[i * 3 + 1];
        double pz = axis[i * 3 + 2];
        double l = Math.max(1.0E-9, Math.sqrt(px * px + py * py + pz * pz));
        double vx = -px / l;
        double vy = -py / l;
        double vz = -pz / l;
        double facing = Math.max(rough, n[0] * vx + n[1] * vy + n[2] * vz);
        double lambert = n[0] * lightX + n[1] * lightY + n[2] * lightZ;
        double hx = lightX + vx;
        double hy = lightY + vy;
        double hz = lightZ + vz;
        double hl = Math.max(1.0E-9, Math.sqrt(hx * hx + hy * hy + hz * hz));
        double spec = Math.max(0.0, (n[0] * hx + n[1] * hy + n[2] * hz) / hl);
        spec = Math.pow(spec, 22.0);
        return shade.at(facing, lambert, spec);
    }

    /**
     * 2.2.6: a dome toward the eye over point {@code i} of a tube's (lifted) path, its radius {@code r}: two rings and a crown, lit as
     * the tube is, so an end or a sharp kink is round from every side.
     */
    static void dome(double[] axis, int i, double[] side, double r, Shade shade, double px, double rough, Sink sink) {
        double cx = axis[i * 3];
        double cy = axis[i * 3 + 1];
        double cz = axis[i * 3 + 2];
        double l = Math.max(1.0E-9, Math.sqrt(cx * cx + cy * cy + cz * cz));
        double vx = -cx / l;
        double vy = -cy / l;
        double vz = -cz / l;
        double ux = side[i * 3];
        double uy = side[i * 3 + 1];
        double uz = side[i * 3 + 2];
        double wx = vy * uz - vz * uy;
        double wy = vz * ux - vx * uz;
        double wz = vx * uy - vy * ux;
        int around = px > 12 ? 12 : ROUND;
        double[] rings = {0.0, 0.55, 1.05};
        double[][] pts = new double[rings.length + 1][];
        int[][] cols = new int[rings.length + 1][];
        double[] n = S.d(3);
        double[] at = {cx, cy, cz};
        for (int ring = 0; ring <= rings.length; ring++) {
            int size = ring == rings.length ? 1 : around;
            pts[ring] = S.d(size * 3);
            cols[ring] = S.i(size);
            double phi = ring == rings.length ? Math.PI / 2 : rings[ring];
            for (int k = 0; k < size; k++) {
                double a = Math.PI * 2.0 * k / around;
                double ca = Math.cos(a) * Math.cos(phi);
                double sa = Math.sin(a) * Math.cos(phi);
                double sp = Math.sin(phi);
                n[0] = ux * ca + wx * sa + vx * sp;
                n[1] = uy * ca + wy * sa + vy * sp;
                n[2] = uz * ca + wz * sa + vz * sp;
                double deep = sp * r * (DEPTH - 1.0);
                pts[ring][k * 3] = cx + n[0] * r + vx * deep;
                pts[ring][k * 3 + 1] = cy + n[1] * r + vy * deep;
                pts[ring][k * 3 + 2] = cz + n[2] * r + vz * deep;
                cols[ring][k] = shadeAt(shade, n, at, 0, rough);
            }
        }
        double[] v = S.d(12);
        int[] c = S.i(4);
        for (int ring = 0; ring < rings.length; ring++) {
            boolean crown = ring + 1 == rings.length;
            for (int k = 0; k < around; k++) {
                int k1 = (k + 1) % around;
                System.arraycopy(pts[ring], k * 3, v, 0, 3);
                System.arraycopy(pts[ring], k1 * 3, v, 3, 3);
                System.arraycopy(pts[ring + 1], crown ? 0 : k1 * 3, v, 6, 3);
                System.arraycopy(pts[ring + 1], crown ? 0 : k * 3, v, 9, 3);
                c[0] = cols[ring][k];
                c[1] = cols[ring][k1];
                c[2] = cols[ring + 1][crown ? 0 : k1];
                c[3] = cols[ring + 1][crown ? 0 : k];
                emit(false, v, c, sink);
            }
        }
    }

    /**
     * The crackle around a live bolt: short spurs that flick out of its kinks, a new set every two ticks (most kinks bare), each a
     * tiny bolt of its own (an inked, lit tube and a glow) thinning to a point.
     */
    static void spurs(Bolt bolt, double[] p, int count, double[] core, double[] lift, double age, double fade, int color, double bright,
                      double flash, int glowColor, double pixel, Sink sink) {
        if (fade < 0.35 || count < 3) {
            return;
        }
        long frame = (long) Math.floor(Math.max(0.0, age) / 2.0);
        SplittableRandom random = new SplittableRandom(bolt.seed ^ 0x5350_5552L ^ frame * 0xD1B54A32D192ED03L);
        int points = 4;
        Shade shade = coreShade(color, bright, flash);
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
            double[] spur = S.d(points * 3);
            double[] half = S.d(points);
            double[] ink = S.d(points);
            double[] halo = S.d(points);
            double[] dist = S.d(points);
            double[] up = S.d(points);
            double[] inkUp = S.d(points);
            for (int k = 0; k < points; k++) {
                int from = k < 3 ? k : 4;
                spur[k * 3] = path[from * 3];
                spur[k * 3 + 1] = path[from * 3 + 1];
                spur[k * 3 + 2] = path[from * 3 + 2];
                dist[k] = Math.max(1.0E-6, Math.sqrt(spur[k * 3] * spur[k * 3] + spur[k * 3 + 1] * spur[k * 3 + 1] + spur[k * 3 + 2] * spur[k * 3 + 2]));
                double t = k / (double) (points - 1);
                // thinning to a point at its far end
                half[k] = Math.max(core[i] * 0.36 * (1.0 - t), 0.7 * pixel * dist[k] * fade * (1.0 - t));
                ink[k] = half[k] + (0.3 * half[k] + 0.9 * pixel * dist[k]) * Math.min(1.0, fade * 1.6) * (1.0 - 0.7 * t);
                halo[k] = half[k] * 3.0 + 0.012 * (1.0 - t);
                up[k] = lift[i];
                inkUp[k] = lift[i] - push(1.0, dist[k]);
            }
            double[] side = sides(spur, points);
            glow(spur, points, side, halo, scale(glowColor, 0.55), sink);
            inked(spur, points, side, ink, inkUp, true, pixel, dist, sink);
            tube3d(spur, points, side, half, up, shade, pixel, dist, sink);
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
        S.reset(); // 3.4.8: this drawing's arrays from the pool
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
        S.reset(); // 3.4.8: this drawing's arrays from the pool
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
        double[] rim = S.d(n * 3);
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
        double[] side = S.d(count * 3);
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
        double[] m = S.d(count);
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

    /**
     * An opaque ribbon turned to the eye (rims {@code rim}, middle {@code middle}), its path lifted {@code lift} toward the eye at each
     * point; with {@code discs}, a round disc on its ends and sharp kinks (2.2.6: the ink lines; a hair-thin tube).
     */
    static void ribbon(double[] p, int count, double[] side, double[] half, double[] lift, int rim, int middle, double pixel, double[] dist,
                       boolean discs, Sink sink) {
        double[] v = S.d(12);
        int[] c = S.i(4);
        double[] m = mitres(p, count, side);
        for (int i = 0; i + 1 < count; i++) {
            double ha = half[i] * m[i];
            double hb = half[i + 1] * m[i + 1];
            // shaded across only where it is wide enough on screen to show it
            boolean shaded = rim != middle && Math.max(half[i] / (pixel * dist[i]), half[i + 1] / (pixel * dist[i + 1])) > 2.5;
            if (!shaded) {
                corner(p, side, i, -ha, lift[i], dist[i], v, 0);
                corner(p, side, i + 1, -hb, lift[i + 1], dist[i + 1], v, 3);
                corner(p, side, i + 1, hb, lift[i + 1], dist[i + 1], v, 6);
                corner(p, side, i, ha, lift[i], dist[i], v, 9);
                c[0] = middle;
                c[1] = middle;
                c[2] = middle;
                c[3] = middle;
                emit(false, v, c, sink);
            } else {
                for (int s = -1; s <= 1; s += 2) {
                    corner(p, side, i, s * ha, lift[i], dist[i], v, 0);
                    corner(p, side, i + 1, s * hb, lift[i + 1], dist[i + 1], v, 3);
                    corner(p, side, i + 1, 0, lift[i + 1], dist[i + 1], v, 6);
                    corner(p, side, i, 0, lift[i], dist[i], v, 9);
                    c[0] = rim;
                    c[1] = rim;
                    c[2] = middle;
                    c[3] = middle;
                    emit(false, v, c, sink);
                }
            }
        }
        if (!discs) {
            return;
        }
        for (int i = 0; i < count; i++) {
            // the mitred ribbon already closes gentle bends: a disc only rounds the ends and the sharp kinks (where the mitre gives out)
            double px = half[i] / (pixel * dist[i]);
            if (px < 1.2 || i > 0 && i < count - 1 && (m[i] < MITRE * 0.8 || px < 1.6)) {
                continue;
            }
            disc(p, i, side, half[i], middle, rim, false, lift[i], pixel, dist[i], sink);
        }
    }

    /**
     * 2.2.5: two bands along the path, from {@code inner} to {@code outer} each side of it ({@code near} at the inner edge, {@code far}
     * at the outer), mitred at its joints: an outline round a body without anything behind the body.
     */
    static void band(double[] p, int count, double[] side, double[] inner, double[] outer, int near, int far, double layer, double[] dist,
                     Sink sink) {
        double[] v = S.d(12);
        int[] c = S.i(4);
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
        double[] v = S.d(12);
        int[] c = S.i(4);
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
        double[] v = S.d(12);
        int[] c = S.i(4);
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
        double[] rim = S.d(5 * 3);
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

    /** A round disc facing the eye at point {@code i} (lifted {@code lift} toward it): {@code centre} in its middle, {@code edge} at its rim. */
    static void disc(double[] p, int i, double[] side, double radius, int centre, int edge, boolean glow, double lift, double pixel,
                     double dist, Sink sink) {
        if (radius <= 0) {
            return;
        }
        double push = glow ? -0.02 : lift;
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
        double[] rim = S.d(ROUND * 3);
        for (int k = 0; k < ROUND; k++) {
            double a = Math.PI * 2.0 * k / ROUND;
            double ca = Math.cos(a) * radius;
            double sa = Math.sin(a) * radius;
            rim[k * 3] = cx + ux * ca + vx * sa;
            rim[k * 3 + 1] = cy + uy * ca + vy * sa;
            rim[k * 3 + 2] = cz + uz * ca + vz * sa;
        }
        double[] v = S.d(12);
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
            // 3.4.8: given as it is (every sink takes the quad's numbers at once: HakiBolts copies them into its batch); it cloned two
            // arrays a quad, thousands a frame through a release
            sink.quad(glow, v, c);
        } else if (first <= 0 && second <= 0) {
            double[] w = WOUND_V;
            int[] k = WOUND_C;
            w[0] = v[0];
            w[1] = v[1];
            w[2] = v[2];
            w[3] = v[9];
            w[4] = v[10];
            w[5] = v[11];
            w[6] = v[6];
            w[7] = v[7];
            w[8] = v[8];
            w[9] = v[3];
            w[10] = v[4];
            w[11] = v[5];
            k[0] = c[0];
            k[1] = c[3];
            k[2] = c[2];
            k[3] = c[1];
            sink.quad(glow, w, k);
        } else {
            triangle(glow, v, c, 0, 1, 2, first, sink);
            triangle(glow, v, c, 0, 2, 3, second, sink);
        }
    }

    /** One triangle as a quad with its last corner doubled, wound to face the eye. */
    static void triangle(boolean glow, double[] v, int[] c, int a, int b, int d, double facing, Sink sink) {
        int p = facing >= 0 ? b : d;
        int q = facing >= 0 ? d : b;
        double[] w = WOUND_V;
        int[] k = WOUND_C;
        for (int i = 0; i < 3; i++) {
            w[i] = v[a * 3 + i];
            w[3 + i] = v[p * 3 + i];
            w[6 + i] = v[q * 3 + i];
            w[9 + i] = v[q * 3 + i];
        }
        k[0] = c[a];
        k[1] = c[p];
        k[2] = c[q];
        k[3] = c[q];
        sink.quad(glow, w, k);
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
        S.reset(); // 3.4.8: this drawing's arrays from the pool
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
        double[] q = S.d(12);
        int[] c = S.i(4);
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
