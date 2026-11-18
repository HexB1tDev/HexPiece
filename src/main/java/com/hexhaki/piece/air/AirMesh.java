package com.hexhaki.piece.air;

import com.hexhaki.piece.water.WaterMesh;
import java.util.ArrayList;
import java.util.List;

/**
 * 2.4.1: Six Powers' cut air as a body (pure: the game's {@code client/fx/AirFx} and {@code tools/preview/WaterPreview} draw the same
 * triangles). The owner: "Improve air particles, speed them up and make them 3d". A cut of air (the Storm Leg's, the Air Blade's) is a
 * crescent: an arc bulging the way it flies, thick in its middle and drawn to a point at both tips, its cross-section a flat lens (thin
 * across its plane, deeper along its bulge); shaded as air pressed hard is seen ({@link WaterMesh#AIR}: all but clear straight on, a
 * bright rim, the sky's glint), laid back to front from the eye, alpha blended: right without culling, never additive.
 */
public final class AirMesh {
    /**
     * A crescent: its apex (eye-relative), the way it flies (unit), the line its tips lie along (unit, square to the way), its radius,
     * half the angle it spans (radians), its thickness across its plane and its depth along its bulge at the apex, how much of it shows.
     */
    public record Crescent(double x, double y, double z, double[] forward, double[] across, double radius, double span, double thickness,
                           double depth, double opacity) {
    }

    private AirMesh() {
    }

    /** How full the crescent is at {@code u} (-1 tip, 0 apex, 1 tip): whole in the middle, drawn to nothing at the tips. */
    public static double taper(double u) {
        double c = Math.cos(Math.max(-1.0, Math.min(1.0, u)) * Math.PI * 0.5);
        return Math.pow(Math.max(0.0, c), 0.7);
    }

    /** The crescent's triangles, back to front from the eye at the origin, to {@code sink}, {@code steps} along it, {@code round} round. */
    public static void crescent(Crescent c, int steps, int round, WaterMesh.Sink sink) {
        double[] f = unit(c.forward());
        double[] a = unit(c.across());
        double[] b = unit(cross(a, f));
        int count = (steps + 1) * round;
        double[][] points = new double[count][];
        double[][] normals = new double[count][];
        for (int i = 0; i <= steps; i++) {
            double u = -1.0 + 2.0 * i / steps;
            double angle = u * c.span();
            // the arc bulges the way it flies: its apex at (x, y, z), its tips swept back
            double[] radial = {a[0] * Math.sin(angle) + f[0] * Math.cos(angle), a[1] * Math.sin(angle) + f[1] * Math.cos(angle),
                a[2] * Math.sin(angle) + f[2] * Math.cos(angle)};
            double[] on = {c.x() + radial[0] * c.radius() - f[0] * c.radius(), c.y() + radial[1] * c.radius() - f[1] * c.radius(),
                c.z() + radial[2] * c.radius() - f[2] * c.radius()};
            double k = taper(u);
            double thick = c.thickness() * k + 1.0E-4;
            double deep = c.depth() * k + 1.0E-4;
            for (int j = 0; j < round; j++) {
                double v = Math.PI * 2.0 * j / round;
                double cv = Math.cos(v);
                double sv = Math.sin(v);
                points[i * round + j] = new double[] {on[0] + radial[0] * deep * cv + b[0] * thick * sv, on[1] + radial[1] * deep * cv
                    + b[1] * thick * sv, on[2] + radial[2] * deep * cv + b[2] * thick * sv};
                normals[i * round + j] = unit(new double[] {radial[0] * cv / deep + b[0] * sv / thick, radial[1] * cv / deep + b[1] * sv / thick,
                    radial[2] * cv / deep + b[2] * sv / thick});
            }
        }
        List<double[]> tris = new ArrayList<>();
        for (int i = 0; i < steps; i++) {
            for (int j = 0; j < round; j++) {
                int p0 = i * round + j;
                int p1 = i * round + (j + 1) % round;
                int p2 = (i + 1) * round + (j + 1) % round;
                int p3 = (i + 1) * round + j;
                tris.add(new double[] {p0, p1, p2, depth(points, p0, p1, p2)});
                tris.add(new double[] {p0, p2, p3, depth(points, p0, p2, p3)});
            }
        }
        tris.sort((p, q) -> Double.compare(q[3], p[3]));
        int[] colours = new int[count];
        for (int v = 0; v < count; v++) {
            colours[v] = WaterMesh.shade(points[v], normals[v], c.opacity(), WaterMesh.AIR);
        }
        for (double[] t : tris) {
            double[] xyz = new double[9];
            int[] argb = new int[3];
            for (int k = 0; k < 3; k++) {
                int v = (int) t[k];
                xyz[k * 3] = points[v][0];
                xyz[k * 3 + 1] = points[v][1];
                xyz[k * 3 + 2] = points[v][2];
                argb[k] = colours[v];
            }
            sink.tri(xyz, argb);
        }
    }

    static double depth(double[][] points, int a, int b, int c) {
        double x = (points[a][0] + points[b][0] + points[c][0]) / 3.0;
        double y = (points[a][1] + points[b][1] + points[c][1]) / 3.0;
        double z = (points[a][2] + points[b][2] + points[c][2]) / 3.0;
        return x * x + y * y + z * z;
    }

    static double[] cross(double[] p, double[] q) {
        return new double[] {p[1] * q[2] - p[2] * q[1], p[2] * q[0] - p[0] * q[2], p[0] * q[1] - p[1] * q[0]};
    }

    static double[] unit(double[] v) {
        double l = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        return l < 1.0E-9 ? new double[] {0, 1, 0} : new double[] {v[0] / l, v[1] / l, v[2] / l};
    }

    /** {@code across} turned {@code degrees} about {@code forward} (a kick's crescent tilted the way the leg swept). */
    public static double[] tilted(double[] forward, double[] across, double degrees) {
        double[] f = unit(forward);
        double[] a = unit(across);
        double[] b = cross(a, f);
        double r = Math.toRadians(degrees);
        return unit(new double[] {a[0] * Math.cos(r) + b[0] * Math.sin(r), a[1] * Math.cos(r) + b[1] * Math.sin(r),
            a[2] * Math.cos(r) + b[2] * Math.sin(r)});
    }
}
