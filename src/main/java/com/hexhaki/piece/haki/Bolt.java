package com.hexhaki.piece.haki;

import java.util.SplittableRandom;

/**
 * 2.1.5: one stroke of 3D Haki lightning (pure, tested in {@code LightningTests}). The owner: "We really need to rework the lightning of
 * the Haki so it's actual 3D lightning. It actually looks good and it feels good to look at, like with a black outline to it ... That is
 * the most important thing about the mod."
 *
 * <p>A stroke is a crackled polyline in its effect's own frame (the original's beam ends, or a sword's, or a fist's), a width at every
 * point, a colour and a life: it waits {@link #delay} ticks, races out from its root over {@link #crawl} ticks, burns for the rest of
 * its {@link #duration} and thins away. {@link BoltMesh} turns it into geometry every frame: a tube facing the eye with round joints, a
 * black outline, a coloured core, a white-hot heart and an additive glow, all in 3D (it bends with the path, the world hides it, it
 * keeps its thickness from every side).
 *
 * <p>Three kinds, read from the original's palette (its lightning is always a thick near-black beam with a thin red one on the same
 * segment, sometimes a pale hot one inside, sometimes a faint wide aura): {@link #BODY} (near-black: the bolt's black body),
 * {@link #CORE} (bright: a coloured core with its own black outline, a hot heart and a glow) and {@link #AURA} (faint: glow only).
 * Strokes on the same segment get the same seed from their ends, so they crackle and dance as one bolt.
 */
public final class Bolt {
    public static final int BODY = 0;
    public static final int CORE = 1;
    public static final int AURA = 2;
    public static final int HEART = 3;
    /** Longest piece of a crackled stroke, in blocks: fine enough that a bolt reads as lightning, not as a bent stick. */
    public static final double PIECE = 0.15;
    public static final int MOST_LEVELS = 5;
    /** How hard the finer levels kink, as a share of their piece. */
    public static final double KINK = 0.3;

    /** Points (xyz, flattened) in the effect's frame. */
    public final double[] points;
    /** Full width at each point, in blocks. */
    public final float[] widths;
    public final int color;
    public final int kind;
    /** Ticks from the effect's start to the strike (fractional: a chain's pieces land one after another). */
    public final float delay;
    public final int duration;
    public final float crawl;
    public final long seed;
    /** How far the bolt dances every two ticks, as a share of a piece's length (0: never). */
    public final double dance;

    public Bolt(double[] points, float[] widths, int color, int kind, float delay, int duration, float crawl, long seed, double dance) {
        if (points.length < 6 || points.length % 3 != 0 || widths.length != points.length / 3) {
            throw new IllegalArgumentException("a bolt needs two points or more and a width for each");
        }
        this.points = points;
        this.widths = widths;
        this.color = color;
        this.kind = kind;
        this.delay = Math.max(0.0F, delay);
        this.duration = Math.max(1, duration);
        this.crawl = Math.max(0.0F, crawl);
        this.seed = seed;
        this.dance = dance;
    }

    public int count() {
        return this.points.length / 3;
    }

    /** The tick (from the effect's start) the bolt is gone. */
    public float end() {
        return this.delay + this.duration;
    }

    /** The kind the original meant by a colour: faint is an aura, near-black is the body, pale is a hot heart, the rest is a core. */
    public static int kindOf(int argb) {
        int a = (argb >>> 24) & 255;
        int r = (argb >>> 16) & 255;
        int g = (argb >>> 8) & 255;
        int b = argb & 255;
        if (a < 150) {
            return AURA;
        }
        if (Math.max(r, Math.max(g, b)) < 48) {
            return BODY;
        }
        return Math.min(r, Math.min(g, b)) >= 100 ? HEART : CORE;
    }

    /** Crackle levels for a stroke of this length: pieces no longer than {@link #PIECE}, and never fewer than eight. */
    public static int levels(double length) {
        // even a short spark zigzags: at least eight pieces
        int levels = Math.min(MOST_LEVELS, 3);
        while (levels < MOST_LEVELS && length / (1 << levels) > PIECE) {
            levels++;
        }
        return levels;
    }

    /**
     * A stroke from {@code a} to {@code b} crackled by its own seed ({@link Jag#path}): {@code width} at the root narrowing to
     * {@code width * tip} at the end ({@code tip} near 1 for a piece of a longer bolt, small for a fork).
     */
    public static Bolt crackled(double[] a, double[] b, float width, float tip, int color, float delay, int duration, float crawl, long seed,
                                double rough) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = b[2] - a[2];
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double[] path = path(a, b, levels(length), rough, seed);
        int n = path.length / 3;
        float[] widths = new float[n];
        // lightning is never an even tube: it swells and pinches over its length (a new thickness every few kinks, eased between;
        // the ends stay true, so a chain stays joined)
        SplittableRandom random = new SplittableRandom(seed ^ 0x5749_4454L);
        int every = 4;
        double[] knots = new double[(n - 1) / every + 2];
        for (int k = 0; k < knots.length; k++) {
            knots[k] = 0.7 + 0.55 * random.nextDouble();
        }
        knots[0] = 1.0;
        for (int i = 0; i < n; i++) {
            double t = n == 1 ? 0 : i / (double) (n - 1);
            int k = i / every;
            double f = (i % every) / (double) every;
            f = f * f * (3 - 2 * f);
            double swell = i == n - 1 ? 1.0 : knots[k] + (knots[Math.min(knots.length - 1, k + 1)] - knots[k]) * f;
            if (i > n - 1 - every) {
                // ease back to the true width at the end
                double e = (i - (n - 1 - every)) / (double) every;
                swell = swell + (1.0 - swell) * e;
            }
            widths[i] = (float) (width * (1.0 - (1.0 - tip) * t) * swell);
        }
        return new Bolt(path, widths, color, kindOf(color), delay, duration, crawl, seed, 0.16);
    }

    /** This stroke with its last stretch drawn to a point (a bolt that ends here, not one that hands over to the next piece). */
    public Bolt pointed() {
        int n = this.count();
        float[] narrowed = this.widths.clone();
        for (int i = 0; i < n; i++) {
            double t = n == 1 ? 1 : i / (double) (n - 1);
            if (t > 0.45) {
                double k = (t - 0.45) / 0.55;
                narrowed[i] = (float) (narrowed[i] * (1.0 - 0.85 * k * k));
            }
        }
        return new Bolt(this.points, narrowed, this.color, this.kind, this.delay, this.duration, this.crawl, this.seed, this.dance);
    }

    /**
     * The crackle of a stroke: midpoint displacement as {@link Jag#path}, but self-similar (every finer level kinks as hard, for its
     * size, as the one above), so a bolt zigzags sharply at every scale instead of bending like a wire. The first split strays by
     * {@code rough} of the whole length; finer ones by {@link #KINK} of their piece. The same ends, levels and seed: the same bolt.
     */
    public static double[] path(double[] a, double[] b, int levels, double rough, long seed) {
        int pieces = 1 << Math.max(0, levels);
        double[] points = new double[(pieces + 1) * 3];
        System.arraycopy(a, 0, points, 0, 3);
        System.arraycopy(b, 0, points, pieces * 3, 3);
        SplittableRandom random = new SplittableRandom(seed);
        double amplitude = rough;
        for (int step = pieces; step > 1; step /= 2) {
            for (int start = 0; start < pieces; start += step) {
                int end = start + step;
                int mid = start + step / 2;
                double sx = points[start * 3];
                double sy = points[start * 3 + 1];
                double sz = points[start * 3 + 2];
                double dx = points[end * 3] - sx;
                double dy = points[end * 3 + 1] - sy;
                double dz = points[end * 3 + 2] - sz;
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double[] across = Jag.across(dx, dy, dz);
                // a kink, not a wobble: the middle pushed firmly to one side (at least half the reach), in a random direction
                double angle = random.nextDouble() * Math.PI * 2.0;
                double reach = (0.5 + 0.5 * random.nextDouble()) * amplitude * length;
                double u = Math.cos(angle) * reach;
                double v = Math.sin(angle) * reach;
                double along = 0.5 + random.nextDouble(-0.14, 0.14);
                points[mid * 3] = sx + dx * along + across[0] * u + across[3] * v;
                points[mid * 3 + 1] = sy + dy * along + across[1] * u + across[4] * v;
                points[mid * 3 + 2] = sz + dz * along + across[2] * u + across[5] * v;
            }
            amplitude = KINK;
        }
        return points;
    }

    /**
     * Where point {@code i} is this frame: its place on the path, shifted across it by the dance (a new shape every two ticks; the
     * ends never move, so a bolt made of several strokes stays one chain). {@code out} gets xyz.
     */
    public void dancing(int i, double age, double[] out) {
        out[0] = this.points[i * 3];
        out[1] = this.points[i * 3 + 1];
        out[2] = this.points[i * 3 + 2];
        int n = this.count();
        if (this.dance <= 0 || i == 0 || i == n - 1) {
            return;
        }
        long frame = (long) Math.floor(Math.max(0.0, age) / 2.0);
        double px = this.points[(i + 1) * 3] - this.points[(i - 1) * 3];
        double py = this.points[(i + 1) * 3 + 1] - this.points[(i - 1) * 3 + 1];
        double pz = this.points[(i + 1) * 3 + 2] - this.points[(i - 1) * 3 + 2];
        double piece = Math.sqrt(px * px + py * py + pz * pz) * 0.5;
        double[] across = Jag.across(px, py, pz);
        SplittableRandom random = new SplittableRandom(this.seed * 0x9E3779B97F4A7C15L + i * 0x632BE59BD9B4E019L + frame * 0x85EBCA77C2B2AE63L);
        double u = random.nextDouble(-1.0, 1.0) * this.dance * piece;
        double v = random.nextDouble(-1.0, 1.0) * this.dance * piece;
        out[0] += across[0] * u + across[3] * v;
        out[1] += across[1] * u + across[4] * v;
        out[2] += across[2] * u + across[5] * v;
    }
}
