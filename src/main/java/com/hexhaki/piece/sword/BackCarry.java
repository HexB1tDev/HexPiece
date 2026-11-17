package com.hexhaki.piece.sword;

import java.util.ArrayList;
import java.util.List;

/**
 * 2.2.7: Yoru worn on the back (pure: the game's {@code client/sword/SwordLayer} draws it, tools/sword_rig.py has the same frame for the
 * animations, StyleTests checks the two agree). The owner: "Yoru is big so it's on the back of the player, it's roughly the size of an
 * entire player (make it tied with a rope to the player's back".
 *
 * <p>Body-part space, model px (x the player's left, y down from the neck, z back). Yoru is 2.75 blocks long (the owner: "Yoru IS way
 * bigger than that"), half as tall again as the player: upright it would stand in the ground or in the head, its grip out of an arm's
 * reach, so it is slung across the back (the placement found by search: the pommel over the head, the tip clear of the ground and of
 * every part of the body, the grip in reach of the right hand): hilt up over the right shoulder, the cross guard behind it, the blade
 * down across the back past the left leg, standing off the back behind the head ({@link #GUARD}: the blade's origin; {@link #TILT}
 * off the vertical; {@link #LEAN} its point in toward the legs). Two ropes hold it: a baldric from over the left shoulder down
 * across the chest to the right hip and back up across the back, and a cord round the waist, both lifted over the blade where they
 * cross it. Each rope is a three-strand hemp tube.
 */
public final class BackCarry {
    /** Where the blade's origin (just under its cross guard) sits on the back, and its tilt (degrees; the point toward the left). */
    public static final double[] GUARD = {-4.0, -1.0, 5.6};
    public static final double TILT = 33.0;
    /** Degrees the point leans in toward the legs (the hilt out behind the head). */
    public static final double LEAN = 8.0;
    /** The rope's radius and its colours (weathered hemp, the shadowed strand). */
    public static final double ROPE = 0.42;
    static final int HEMP = 0xC9A774;
    static final int HEMP_DARK = 0x7E643F;
    static final int SIDES = 6;
    /** The torso with its jacket layer (px): half width, half depth, and the rope's centre this far out of it. */
    static final double HALF_X = 4.25;
    static final double HALF_Z = 2.25;
    static final double OFF = 0.5;
    /** The blade's half width where a rope crosses it (a rope tents over it from this far on), and how far over its depth it lies. */
    static final double BLADE_HALF = 1.5;
    static final double TENT = 3.5;
    static final double OVER = 0.4 + ROPE + 0.05;

    private BackCarry() {
    }

    /** A rope quad: four corners (x, y, z), their normals, their colours (RGB). */
    public record Quad(float[] xyz, float[] normals, int[] rgb) {
    }

    /** The blade's direction down the back (unit, body px). */
    public static double[] axis() {
        double t = Math.toRadians(TILT);
        double l = Math.toRadians(LEAN);
        return new double[] {Math.sin(t) * Math.cos(l), Math.cos(t) * Math.cos(l), -Math.sin(l)};
    }

    /** How far a point of the back (x, y) is from the blade's line seen from behind (px), and the blade's depth (z) there. */
    public static double[] fromBlade(double x, double y) {
        double[] a = axis();
        double flat = Math.hypot(a[0], a[1]);
        double ux = a[0] / flat;
        double uy = a[1] / flat;
        double dx = x - GUARD[0];
        double dy = y - GUARD[1];
        double along = dx * ux + dy * uy;
        return new double[] {Math.abs(dx * uy - dy * ux), GUARD[2] + along / flat * a[2]};
    }

    /** The baldric: over the left shoulder, down the chest to the right hip, round the right side and back up across the back. */
    public static List<double[]> baldric() {
        double x = HALF_X + OFF;
        double z = HALF_Z + OFF;
        double[][] corners = {
            {2.6, -OFF, -z}, {-x, 10.6, -z}, {-x, 10.8, 0.0}, {-x, 10.6, z}, {2.6, -OFF, z}, {2.9, -OFF - 0.25, 0.0}};
        return over(smooth(loop(corners, 0.45), 2));
    }

    /** The cord round the waist, just above the belt. */
    public static List<double[]> waist() {
        double x = HALF_X + OFF;
        double z = HALF_Z + OFF;
        double y = 10.0;
        double[][] corners = {{x, y, -z}, {-x, y, -z}, {-x, y, z}, {x, y, z}};
        return over(smooth(loop(corners, 0.45), 2));
    }

    /** A closed polyline through {@code corners}, a point every {@code step} px. */
    static List<double[]> loop(double[][] corners, double step) {
        List<double[]> out = new ArrayList<>();
        for (int i = 0; i < corners.length; i++) {
            double[] a = corners[i];
            double[] b = corners[(i + 1) % corners.length];
            double length = Math.sqrt(sq(b[0] - a[0]) + sq(b[1] - a[1]) + sq(b[2] - a[2]));
            int n = Math.max(1, (int) Math.ceil(length / step));
            for (int k = 0; k < n; k++) {
                double t = k / (double) n;
                out.add(new double[] {a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t});
            }
        }
        return out;
    }

    /** Chaikin's corner cutting on a closed polyline, {@code times} over: the rope bends round the body's edges, never kinks. */
    static List<double[]> smooth(List<double[]> loop, int times) {
        List<double[]> out = loop;
        for (int pass = 0; pass < times; pass++) {
            List<double[]> next = new ArrayList<>(out.size() * 2);
            for (int i = 0; i < out.size(); i++) {
                double[] a = out.get(i);
                double[] b = out.get((i + 1) % out.size());
                next.add(new double[] {a[0] * 0.75 + b[0] * 0.25, a[1] * 0.75 + b[1] * 0.25, a[2] * 0.75 + b[2] * 0.25});
                next.add(new double[] {a[0] * 0.25 + b[0] * 0.75, a[1] * 0.25 + b[1] * 0.75, a[2] * 0.25 + b[2] * 0.75});
            }
            out = next;
        }
        return out;
    }

    /** Where a rope crosses the blade on the back, it rises over it (smoothly, so it reads as pulled tight over the steel). */
    static List<double[]> over(List<double[]> loop) {
        for (double[] p : loop) {
            if (p[2] <= 0.0) {
                continue;
            }
            double[] blade = fromBlade(p[0], p[1]);
            double k = 1.0 - smoothstep(BLADE_HALF, BLADE_HALF + TENT, blade[0]);
            double over = blade[1] + OVER;
            p[2] = Math.max(p[2], p[2] + (over - p[2]) * k);
        }
        return loop;
    }

    static double smoothstep(double a, double b, double x) {
        double t = Math.max(0.0, Math.min(1.0, (x - a) / (b - a)));
        return t * t * (3.0 - 2.0 * t);
    }

    static double sq(double v) {
        return v * v;
    }

    /** Both ropes as quads (body px), ready to be drawn through the body part's pose. */
    public static List<Quad> ropes() {
        List<Quad> out = new ArrayList<>();
        tube(baldric(), out);
        tube(waist(), out);
        return out;
    }

    /**
     * A closed rope along {@code loop}: a {@link #SIDES}-sided tube of radius {@link #ROPE}, frames carried along the loop without twist
     * (parallel transport), coloured as three strands laid in a helix (the dark seam between strands turning round it as it runs).
     */
    static void tube(List<double[]> loop, List<Quad> out) {
        int n = loop.size();
        double[][] tangent = new double[n][];
        for (int i = 0; i < n; i++) {
            double[] a = loop.get((i - 1 + n) % n);
            double[] b = loop.get((i + 1) % n);
            tangent[i] = unit(b[0] - a[0], b[1] - a[1], b[2] - a[2]);
        }
        double[][] normal = new double[n][];
        double[] start = Math.abs(tangent[0][1]) < 0.9 ? new double[] {0, 1, 0} : new double[] {1, 0, 0};
        normal[0] = unit(cross(cross(tangent[0], start), tangent[0]));
        for (int i = 1; i < n; i++) {
            double[] prev = normal[i - 1];
            double along = dot(prev, tangent[i]);
            normal[i] = unit(prev[0] - along * tangent[i][0], prev[1] - along * tangent[i][1], prev[2] - along * tangent[i][2]);
        }
        double run = 0.0;
        double[] runs = new double[n + 1];
        for (int i = 1; i <= n; i++) {
            double[] a = loop.get(i - 1);
            double[] b = loop.get(i % n);
            run += Math.sqrt(sq(b[0] - a[0]) + sq(b[1] - a[1]) + sq(b[2] - a[2]));
            runs[i] = run;
        }
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            for (int s = 0; s < SIDES; s++) {
                float[] xyz = new float[12];
                float[] nrm = new float[12];
                int[] rgb = new int[4];
                int[][] corners = {{i, s}, {j, s}, {j, s + 1}, {i, s + 1}};
                for (int c = 0; c < 4; c++) {
                    int at = corners[c][0];
                    double angle = Math.PI * 2.0 * corners[c][1] / SIDES;
                    double[] t = tangent[at];
                    double[] u = normal[at];
                    double[] v = cross(t, u);
                    double[] dir = {u[0] * Math.cos(angle) + v[0] * Math.sin(angle), u[1] * Math.cos(angle) + v[1] * Math.sin(angle),
                        u[2] * Math.cos(angle) + v[2] * Math.sin(angle)};
                    double[] p = loop.get(at);
                    for (int k = 0; k < 3; k++) {
                        xyz[c * 3 + k] = (float) (p[k] + dir[k] * ROPE);
                        nrm[c * 3 + k] = (float) dir[k];
                    }
                    double along = at == 0 && c > 0 && i == n - 1 ? runs[n] : runs[at];
                    // three strands: a seam every third of a turn, the helix climbing one strand width per 1.1 px
                    double strand = 0.5 + 0.5 * Math.cos(3.0 * angle - along / 1.1 * Math.PI * 2.0);
                    rgb[c] = mix(HEMP_DARK, HEMP, 0.35 + 0.65 * Math.pow(strand, 0.6));
                }
                out.add(new Quad(xyz, nrm, rgb));
            }
        }
    }

    static int mix(int a, int b, double t) {
        int r = (int) Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return r << 16 | g << 8 | bl;
    }

    static double[] unit(double x, double y, double z) {
        double l = Math.sqrt(x * x + y * y + z * z);
        return l < 1.0E-9 ? new double[] {0, 0, 1} : new double[] {x / l, y / l, z / l};
    }

    static double[] unit(double[] v) {
        return unit(v[0], v[1], v[2]);
    }

    static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }
}
