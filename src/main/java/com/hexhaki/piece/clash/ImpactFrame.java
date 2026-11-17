package com.hexhaki.piece.clash;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * 2.3.6: the impact frame of a clash (the owner: "I want a impact frame whenever a clash happens"), as screen geometry (pure: the game,
 * {@code client/fx/ClashFx}, and {@code tools/preview/ImpactPreview} draw the very same batches). The anime's frame in three beats, over
 * {@link ClashRules#frameTicks} ticks (2 for a small clash, 4 for the hardest):
 * <ol>
 *   <li><b>the negative</b>: the whole view inverted where it stands ({@link #INVERT}), focus lines of ink rushing in on the point the
 *   blows met, a black star burst there;</li>
 *   <li><b>the black frame</b>: the view gone to ink, white rays torn out of the point, a white star burst on it (two Ryou users: the rays
 *   in both their colours, by turns);</li>
 *   <li><b>the release</b>: the view back, a white flash going off it and the last lines fading ({@link #TAIL} ticks more).</li>
 * </ol>
 * Every beat's lines are drawn afresh every half tick (they flicker, as hand-drawn frames do), from the clash's own seed.
 */
public final class ImpactFrame {
    /** A batch drawn with the view's colour turned over under it (white, one minus the colour already there). */
    public static final int INVERT = 0;
    /** A batch alpha blended over the view. */
    public static final int BLEND = 1;
    /**
     * The whole view's contrast pushed (each colour doubled times itself, 2c<sup>2</sup>, clamped): the game draws a copy of the view
     * over itself multiplied (its triangles are the whole view).
     */
    public static final int CONTRAST = 2;
    /** Ticks the release fades over after the frame itself. */
    public static final double TAIL = 2.0;
    /** Where the beats change, as a share of the frame's ticks. */
    public static final double NEGATIVE = 0.42;
    public static final double BLACK = 0.78;
    static final int INK = 0xFF000000;
    /** How many times the negative's contrast is pushed. */
    public static final int CONTRAST_PASSES = 1;
    static final int WHITE = 0xFFFFFFFF;

    /** One batch: how it is blended, its triangles (x, y in screen units, three points a triangle) and each point's colour (argb). */
    public record Batch(int mode, float[] xy, int[] argb) {
        public int count() {
            return this.argb.length;
        }
    }

    private ImpactFrame() {
    }

    /** How long a clash of {@code power} shows on screen, the release included (ticks). */
    public static double length(float power) {
        return ClashRules.frameTicks(power) + TAIL;
    }

    /**
     * What to draw {@code age} ticks into a clash's frame on a {@code width} x {@code height} view whose blows met at ({@code px},
     * {@code py}) on it (off it, the lines still come from there), {@code power} hard, the two Ryou colours ({@code colorA},
     * {@code colorB}; 0: none), from {@code seed}. Empty before it starts and once it is over.
     */
    public static List<Batch> at(double age, double width, double height, double px, double py, float power, int colorA, int colorB,
                                 long seed) {
        List<Batch> out = new ArrayList<>();
        int frame = ClashRules.frameTicks(power);
        if (age < 0.0 || age >= frame + TAIL || width <= 0 || height <= 0) {
            return out;
        }
        double u = age / frame;
        double small = Math.min(width, height);
        double reach = Math.hypot(Math.max(px, width - px), Math.max(py, height - py)) * 1.08 + small * 0.1;
        double size = Math.max(0.6, Math.min(1.6, 0.55 + 0.35 * power));
        boolean ryou = colorA != 0 && colorB != 0;
        SplittableRandom random = new SplittableRandom(seed * 0x9E3779B97F4A7C15L + (long) Math.floor(age * 2.0) * 0xBF58476D1CE4E5B9L);
        if (u < NEGATIVE) {
            out.add(screen(INVERT, width, height, WHITE));
            for (int pass = 0; pass < CONTRAST_PASSES; pass++) {
                out.add(screen(CONTRAST, width, height, WHITE));
            }
            Shapes ink = new Shapes();
            focusLines(ink, random, px, py, reach, small, Math.round(60 + 16 * power), 0.1, 0.32, 0.006, 0.02, INK, 0xF0);
            // small: the blows' point, not a blot over the two who threw them
            star(ink, random, px, py, small * 0.022 * size, 11 + (int) (3 * power), 2.0, 4.2, INK, INK);
            out.add(ink.batch(BLEND));
            return out;
        }
        if (u < BLACK) {
            out.add(screen(BLEND, width, height, 0xF4040404));
            Shapes light = new Shapes();
            int rays = Math.round(15 + 6 * power);
            double core = small * 0.06 * size;
            for (int i = 0; i < rays; i++) {
                double angle = Math.PI * 2.0 * (i + random.nextDouble(-0.35, 0.35)) / rays;
                int tint = ryou ? (i % 2 == 0 ? colorA : colorB) | 0xFF000000 : WHITE;
                double wide = small * random.nextDouble(0.018, 0.05) * size;
                double from = core * random.nextDouble(0.7, 1.4);
                ray(light, px, py, angle, from, reach, core * 0.5, wide, WHITE, alpha(tint, ryou ? 0xD0 : 0x60));
            }
            focusLines(light, random, px, py, reach, small, Math.round(26 + 8 * power), 0.3, 0.6, 0.002, 0.006, WHITE, 0x90);
            star(light, random, px, py, core, 14 + (int) (3 * power), 1.8, 3.2, WHITE, WHITE);
            out.add(light.batch(BLEND));
            return out;
        }
        double v = Math.min(1.0, (age - BLACK * frame) / ((1.0 - BLACK) * frame + TAIL));
        double fade = (1.0 - v) * (1.0 - v);
        out.add(screen(BLEND, width, height, alpha(WHITE, (int) Math.round(150 * fade))));
        Shapes lines = new Shapes();
        focusLines(lines, random, px, py, reach, small, Math.round(36 + 10 * power), 0.24 + 0.3 * v, 0.55, 0.004, 0.012, INK,
            (int) Math.round(200 * (1.0 - v)));
        out.add(lines.batch(BLEND));
        return out;
    }

    /** Triangles as they are laid. */
    static final class Shapes {
        final List<float[]> points = new ArrayList<>();
        final List<Integer> colors = new ArrayList<>();

        void tri(double x0, double y0, int c0, double x1, double y1, int c1, double x2, double y2, int c2) {
            this.points.add(new float[] {(float) x0, (float) y0});
            this.points.add(new float[] {(float) x1, (float) y1});
            this.points.add(new float[] {(float) x2, (float) y2});
            this.colors.add(c0);
            this.colors.add(c1);
            this.colors.add(c2);
        }

        Batch batch(int mode) {
            float[] xy = new float[this.points.size() * 2];
            int[] argb = new int[this.colors.size()];
            for (int i = 0; i < argb.length; i++) {
                xy[i * 2] = this.points.get(i)[0];
                xy[i * 2 + 1] = this.points.get(i)[1];
                argb[i] = this.colors.get(i);
            }
            return new Batch(mode, xy, argb);
        }
    }

    /** The whole view in one colour. */
    static Batch screen(int mode, double width, double height, int argb) {
        Shapes s = new Shapes();
        s.tri(0, 0, argb, 0, height, argb, width, height, argb);
        s.tri(0, 0, argb, width, height, argb, width, 0, argb);
        return s.batch(mode);
    }

    /**
     * Thin lines rushing in on the point (the manga's focus lines): {@code count} wedges round it, each from a little way out
     * ({@code in0}..{@code in1} of the view's short side) to past the view's edge, a hair at its inner end and {@code w0}..{@code w1}
     * of the view's diagonal at its outer one, {@code color} at {@code alpha} outside fading to nothing toward the point.
     */
    static void focusLines(Shapes s, SplittableRandom random, double px, double py, double reach, double small, int count, double in0,
                           double in1, double w0, double w1, int color, int alpha) {
        if (alpha <= 0) {
            return;
        }
        double diagonal = reach;
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0 * (i + random.nextDouble()) / count;
            double from = small * random.nextDouble(in0, in1);
            double wide = diagonal * random.nextDouble(w0, w1);
            ray(s, px, py, angle, from, reach, 0.0, wide, alpha(color, 0), alpha(color, alpha));
        }
    }

    /** One wedge out from the point along {@code angle}, {@code from}..{@code to} out, {@code w0} wide there and {@code w1} at its end. */
    static void ray(Shapes s, double px, double py, double angle, double from, double to, double w0, double w1, int inner, int outer) {
        double cx = Math.cos(angle);
        double cy = Math.sin(angle);
        double ax = px + cx * from;
        double ay = py + cy * from;
        double bx = px + cx * to;
        double by = py + cy * to;
        double nx = -cy;
        double ny = cx;
        if (w0 <= 0.0) {
            s.tri(ax, ay, inner, bx + nx * w1 * 0.5, by + ny * w1 * 0.5, outer, bx - nx * w1 * 0.5, by - ny * w1 * 0.5, outer);
            return;
        }
        double a1x = ax + nx * w0 * 0.5;
        double a1y = ay + ny * w0 * 0.5;
        double a2x = ax - nx * w0 * 0.5;
        double a2y = ay - ny * w0 * 0.5;
        double b1x = bx + nx * w1 * 0.5;
        double b1y = by + ny * w1 * 0.5;
        double b2x = bx - nx * w1 * 0.5;
        double b2y = by - ny * w1 * 0.5;
        s.tri(a1x, a1y, inner, b1x, b1y, outer, b2x, b2y, outer);
        s.tri(a1x, a1y, inner, b2x, b2y, outer, a2x, a2y, inner);
    }

    /**
     * A star burst on the point: {@code spikes} points from {@code lo}..{@code hi} times {@code radius} out, their roots on a ring of
     * {@code radius}; {@code middle} at the centre, {@code edge} out on the spikes.
     */
    static void star(Shapes s, SplittableRandom random, double px, double py, double radius, int spikes, double lo, double hi, int middle,
                     int edge) {
        int n = spikes * 2;
        double[] xs = new double[n];
        double[] ys = new double[n];
        double turn = random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < n; i++) {
            double angle = turn + Math.PI * 2.0 * (i + (i % 2 == 0 ? random.nextDouble(-0.25, 0.25) : 0.0)) / n;
            double r = i % 2 == 0 ? radius * random.nextDouble(lo, hi) : radius * random.nextDouble(0.85, 1.1);
            xs[i] = px + Math.cos(angle) * r;
            ys[i] = py + Math.sin(angle) * r;
        }
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            s.tri(px, py, middle, xs[i], ys[i], i % 2 == 0 ? edge : middle, xs[j], ys[j], j % 2 == 0 ? edge : middle);
        }
    }

    static int alpha(int rgb, int alpha) {
        return Math.max(0, Math.min(255, alpha)) << 24 | rgb & 0xFFFFFF;
    }

    /** The two colours half and half (opaque). */
    static int mix(int a, int b) {
        int r = (((a >> 16) & 255) + ((b >> 16) & 255)) / 2;
        int g = (((a >> 8) & 255) + ((b >> 8) & 255)) / 2;
        int bl = ((a & 255) + (b & 255)) / 2;
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }

    static int mixWhite(int rgb, double t) {
        int r = (int) Math.round(((rgb >> 16) & 255) * (1 - t) + 255 * t);
        int g = (int) Math.round(((rgb >> 8) & 255) * (1 - t) + 255 * t);
        int b = (int) Math.round((rgb & 255) * (1 - t) + 255 * t);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    /**
     * Where a point in the world falls on the view (pure: the camera's position, its look, up and left as unit vectors, the vertical
     * field of view, the view's size): {x, y, in front (1) or behind (0)}. Behind the eye, the point is taken to the view's middle.
     */
    public static double[] onScreen(double[] point, double[] eye, double[] look, double[] up, double[] left, double fovDegrees, double width,
                                    double height) {
        double rx = point[0] - eye[0];
        double ry = point[1] - eye[1];
        double rz = point[2] - eye[2];
        double z = rx * look[0] + ry * look[1] + rz * look[2];
        if (z < 0.05) {
            return new double[] {width * 0.5, height * 0.5, 0.0};
        }
        double x = rx * left[0] + ry * left[1] + rz * left[2];
        double y = rx * up[0] + ry * up[1] + rz * up[2];
        double t = Math.tan(Math.toRadians(fovDegrees) * 0.5);
        double sx = width * 0.5 - (x / z) / t * height * 0.5;
        double sy = height * 0.5 - (y / z) / t * height * 0.5;
        return new double[] {sx, sy, 1.0};
    }
}
