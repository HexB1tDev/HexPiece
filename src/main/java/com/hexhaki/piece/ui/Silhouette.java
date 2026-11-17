package com.hexhaki.piece.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * 2.2.4 (the owner, of the start menu's figures: "the models of the female and the player are completely fucking horrible ... you can
 * tell that the female body is, like, broken in the image ... Just make them, like, black silhouettes. You don't have to be adding
 * details to it"): a man and a woman standing, front on, as one smooth outline each, filled plain near-black. No face, no clothes.
 *
 * <p>Each body is authored as its right half in head units (8 heads from the crown to the soles; x from the middle line), a closed
 * loop of control points mirrored to the left, joined by a centripetal Catmull-Rom spline (never loops or overshoots between close
 * points) that bends smoothly everywhere except at the points marked sharp (armpits, the crotch, fingertips, the soles). The arms hang
 * a little away from the body so the shape reads; the woman's long hair falls to the shoulders, her waist is narrow and her hips wider
 * than her shoulders, her arms slimmer and her legs closer. Pure: the outline is plain numbers, filled by {@link UiCanvas#shape}.</p>
 */
public final class Silhouette {
    /** Heads from the crown to the soles. */
    public static final float HEADS = 8.0F;
    public static final int INK = 0xFF15110E;

    /** {x, y, sharp}: the right half, crown to crotch, in head units. */
    static final float[][] MAN = {
        {0.00F, 0.00F, 0}, {0.16F, 0.042F, 0}, {0.28F, 0.146F, 0}, {0.355F, 0.29F, 0}, {0.38F, 0.45F, 0}, {0.37F, 0.58F, 0},
        {0.33F, 0.70F, 0}, {0.25F, 0.85F, 0}, {0.21F, 0.93F, 1},
        // neck, the trapezius, the shoulder
        {0.225F, 1.10F, 0}, {0.40F, 1.27F, 0}, {0.70F, 1.38F, 0}, {0.95F, 1.49F, 0}, {1.10F, 1.64F, 0}, {1.18F, 1.90F, 0},
        // the arm's outside, the hand
        {1.21F, 2.30F, 0}, {1.23F, 2.70F, 0}, {1.24F, 3.00F, 0}, {1.31F, 3.30F, 0}, {1.33F, 3.70F, 0}, {1.33F, 3.96F, 0},
        {1.38F, 4.22F, 0}, {1.38F, 4.52F, 0}, {1.31F, 4.78F, 1}, {1.20F, 4.81F, 0}, {1.11F, 4.66F, 0}, {1.10F, 4.36F, 0},
        {1.12F, 4.06F, 0},
        // the arm's inside up to the armpit
        {1.13F, 3.92F, 0}, {1.05F, 3.40F, 0}, {0.98F, 3.02F, 0}, {0.91F, 2.62F, 0}, {0.86F, 2.32F, 1},
        // the torso: lats, waist, hip
        {0.80F, 2.50F, 0}, {0.73F, 2.80F, 0}, {0.68F, 3.08F, 0}, {0.69F, 3.42F, 0}, {0.75F, 3.78F, 0},
        // the leg's outside, the foot
        {0.77F, 4.04F, 0}, {0.72F, 4.62F, 0}, {0.61F, 5.22F, 0}, {0.49F, 5.76F, 0}, {0.51F, 6.22F, 0}, {0.45F, 6.90F, 0},
        {0.31F, 7.56F, 0}, {0.38F, 7.80F, 0}, {0.41F, 8.00F, 1}, {0.09F, 8.00F, 1},
        // the leg's inside up to the crotch
        {0.08F, 7.80F, 0}, {0.13F, 7.54F, 0}, {0.16F, 6.60F, 0}, {0.14F, 6.10F, 0}, {0.11F, 5.74F, 0}, {0.10F, 5.00F, 0},
        {0.07F, 4.42F, 0}, {0.00F, 4.16F, 1},
    };

    /** The woman: long hair to the shoulders (her face inside it), narrow waist, wide hips, slim arms, legs close. */
    static final float[][] WOMAN = {
        {0.00F, 0.00F, 0}, {0.18F, 0.035F, 0}, {0.32F, 0.12F, 0}, {0.42F, 0.26F, 0}, {0.46F, 0.42F, 0}, {0.47F, 0.62F, 0},
        {0.46F, 0.86F, 0}, {0.48F, 1.08F, 0}, {0.53F, 1.28F, 0}, {0.58F, 1.40F, 1},
        // where the hair's ends meet the shoulder
        {0.55F, 1.43F, 1}, {0.68F, 1.48F, 0}, {0.80F, 1.55F, 0}, {0.88F, 1.72F, 0},
        // the arm's outside, the hand
        {0.92F, 2.05F, 0}, {0.95F, 2.55F, 0}, {0.98F, 2.98F, 0}, {1.03F, 3.36F, 0}, {1.07F, 3.74F, 0}, {1.09F, 3.96F, 0},
        {1.14F, 4.20F, 0}, {1.13F, 4.48F, 0}, {1.07F, 4.70F, 1}, {0.99F, 4.72F, 0}, {0.93F, 4.58F, 0}, {0.92F, 4.30F, 0},
        {0.94F, 4.04F, 0},
        // the arm's inside up to the armpit
        {0.94F, 3.90F, 0}, {0.87F, 3.40F, 0}, {0.81F, 3.00F, 0}, {0.75F, 2.58F, 0}, {0.70F, 2.26F, 1},
        // bust, waist, hips
        {0.70F, 2.40F, 0}, {0.66F, 2.66F, 0}, {0.57F, 3.00F, 0}, {0.57F, 3.22F, 0}, {0.66F, 3.52F, 0}, {0.75F, 3.80F, 0},
        // the leg's outside, the foot
        {0.785F, 4.02F, 0}, {0.765F, 4.28F, 0}, {0.70F, 4.70F, 0}, {0.59F, 5.24F, 0}, {0.43F, 5.78F, 0}, {0.45F, 6.26F, 0}, {0.38F, 6.92F, 0},
        {0.23F, 7.60F, 0}, {0.28F, 7.82F, 0}, {0.30F, 8.00F, 1}, {0.06F, 8.00F, 1},
        // the leg's inside up to the crotch
        {0.05F, 7.84F, 0}, {0.08F, 7.58F, 0}, {0.11F, 6.62F, 0}, {0.09F, 6.10F, 0}, {0.07F, 5.80F, 0}, {0.08F, 5.04F, 0},
        {0.06F, 4.62F, 0}, {0.00F, 4.38F, 1},
    };

    private Silhouette() {
    }

    /** The whole outline, both halves, as {xs, ys} in head units (x from the middle line, y down from the crown). */
    public static float[][] outline(boolean woman) {
        float[][] half = woman ? WOMAN : MAN;
        // the closed loop: the right half crown to crotch, then the left half back up (the crown and crotch not doubled)
        List<float[]> loop = new ArrayList<>();
        for (float[] p : half) {
            loop.add(p);
        }
        for (int i = half.length - 2; i >= 1; i--) {
            loop.add(new float[] {-half[i][0], half[i][1], half[i][2]});
        }
        List<float[]> out = new ArrayList<>();
        int n = loop.size();
        // start the walk at a sharp point so every smooth run between two sharp points is one open spline
        int start = 0;
        while (loop.get(start)[2] == 0) {
            start++;
        }
        int i = start;
        do {
            int j = i + 1;
            while (loop.get(j % n)[2] == 0) {
                j++;
            }
            List<float[]> run = new ArrayList<>();
            for (int k = i; k <= j; k++) {
                run.add(loop.get(k % n));
            }
            spline(run, out);
            i = j % n;
        } while (i != start);
        float[] xs = new float[out.size()];
        float[] ys = new float[out.size()];
        for (int k = 0; k < out.size(); k++) {
            xs[k] = out.get(k)[0];
            ys[k] = out.get(k)[1];
        }
        return new float[][] {xs, ys};
    }

    /** An open centripetal Catmull-Rom spline through {@code run} (its ends extended straight), every point but the last added. */
    static void spline(List<float[]> run, List<float[]> out) {
        int m = run.size();
        for (int s = 0; s < m - 1; s++) {
            float[] p1 = run.get(s);
            float[] p2 = run.get(s + 1);
            float[] p0 = s > 0 ? run.get(s - 1) : new float[] {2 * p1[0] - p2[0], 2 * p1[1] - p2[1]};
            float[] p3 = s + 2 < m ? run.get(s + 2) : new float[] {2 * p2[0] - p1[0], 2 * p2[1] - p1[1]};
            float length = (float) Math.hypot(p2[0] - p1[0], p2[1] - p1[1]);
            int steps = Math.max(2, (int) Math.ceil(length / 0.04F));
            for (int k = 0; k < steps; k++) {
                out.add(centripetal(p0, p1, p2, p3, k / (float) steps));
            }
        }
    }

    /** The point at {@code u} (0..1) between p1 and p2 (Barry and Goldman's pyramid, knots spaced by the square root of distance). */
    static float[] centripetal(float[] p0, float[] p1, float[] p2, float[] p3, float u) {
        double t0 = 0;
        double t1 = t0 + Math.max(1e-4, Math.sqrt(Math.hypot(p1[0] - p0[0], p1[1] - p0[1])));
        double t2 = t1 + Math.max(1e-4, Math.sqrt(Math.hypot(p2[0] - p1[0], p2[1] - p1[1])));
        double t3 = t2 + Math.max(1e-4, Math.sqrt(Math.hypot(p3[0] - p2[0], p3[1] - p2[1])));
        double t = t1 + (t2 - t1) * u;
        float[] r = new float[2];
        for (int a = 0; a < 2; a++) {
            double a1 = (t1 - t) / (t1 - t0) * p0[a] + (t - t0) / (t1 - t0) * p1[a];
            double a2 = (t2 - t) / (t2 - t1) * p1[a] + (t - t1) / (t2 - t1) * p2[a];
            double a3 = (t3 - t) / (t3 - t2) * p2[a] + (t - t2) / (t3 - t2) * p3[a];
            double b1 = (t2 - t) / (t2 - t0) * a1 + (t - t0) / (t2 - t0) * a2;
            double b2 = (t3 - t) / (t3 - t1) * a2 + (t - t1) / (t3 - t1) * a3;
            r[a] = (float) ((t2 - t) / (t2 - t1) * b1 + (t - t1) / (t2 - t1) * b2);
        }
        return r;
    }

    /** The figure {@code height} pixels tall, its crown at ({@code cx}, {@code top}), with a soft shadow under the feet. */
    public static void draw(UiCanvas c, float cx, float top, float height, boolean woman) {
        float unit = height / HEADS;
        float[][] o = outline(woman);
        float[] xs = new float[o[0].length];
        float[] ys = new float[o[0].length];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = cx + o[0][i] * unit;
            ys[i] = top + o[1][i] * unit;
        }
        c.batch(() -> {
            // the ground shadow: three flat ellipses, each fainter and wider
            for (int ring = 0; ring < 3; ring++) {
                float rx = unit * (0.95F + 0.35F * ring);
                float ry = unit * (0.13F + 0.05F * ring);
                c.shape(ellipseX(cx, rx), ellipseY(top + height - unit * 0.02F, ry), 0x22000000);
            }
            c.shape(xs, ys, INK);
        });
    }

    static float[] ellipseX(float cx, float rx) {
        float[] xs = new float[40];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = cx + rx * (float) Math.cos(i * Math.PI * 2 / xs.length);
        }
        return xs;
    }

    static float[] ellipseY(float cy, float ry) {
        float[] ys = new float[40];
        for (int i = 0; i < ys.length; i++) {
            ys[i] = cy + ry * (float) Math.sin(i * Math.PI * 2 / ys.length);
        }
        return ys;
    }
}
