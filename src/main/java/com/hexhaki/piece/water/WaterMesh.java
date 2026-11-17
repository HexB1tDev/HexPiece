package com.hexhaki.piece.water;

import java.util.ArrayList;
import java.util.List;

/**
 * 2.3.8: water as a body (pure: the game's {@code client/fx/WaterFx} and {@code tools/preview/WaterPreview} draw the very same triangles).
 * The owner: "make sure the water abilities are super realistic, like the water actually materializes from the air particles in the air,
 * and forms realistic water moves". A drop, a bullet or a prison's bubble is a closed surface of water: a sphere that wobbles as liquid
 * does ({@link #wobble}), drawn out along its flight into a teardrop ({@code stretch}), shaded as water is seen: clear where you look
 * straight through it, bright and more opaque toward its rim (the fresnel of a curved surface), a hard white highlight where the sky
 * glints off it, and a pale glow on its far underside where the light that passes through it gathers. Alpha blended, its triangles laid
 * back to front from the eye (the far inner wall before the near one): right whatever the device does with culling, never additive.
 */
public final class WaterMesh {
    /** Deep water (looked straight through), the shallow tint toward the rim, the light gathered on the far side. */
    public static final int DEEP = 0x1A6FC4;
    public static final int SHALLOW = 0x9ADCFF;
    public static final int GATHERED = 0xD6F6FF;
    /** How much of it there is to see through: a drop (dense, small), a prison's bubble (a body of water round someone). */
    public static final double DROP = 1.6;
    public static final double PRISON = 1.15;
    /** Where the sky's light comes from (unit vector, world: up, a little to the side). */
    static final double[] SKY = normalise(new double[] {0.35, 1.0, 0.22});

    /** Takes each triangle: three points (x, y, z, eye-relative) and their colours (argb). */
    public interface Sink {
        void tri(double[] xyz, int[] argb);
    }

    /** How a body is shaped and seen. */
    public record Body(double x, double y, double z, double radius, double[] along, double stretch, double wobble, double time, long seed,
                       double opacity) {
    }

    private WaterMesh() {
    }

    /** Rings and sides for a body this many blocks across seen from {@code distance} away: fine up close, few far off. */
    public static int[] resolution(double radius, double distance) {
        double share = radius / Math.max(0.5, distance);
        int rings = share > 0.25 ? 14 : share > 0.08 ? 10 : share > 0.03 ? 7 : 5;
        return new int[] {rings, rings * 2};
    }

    /**
     * How far the surface stands out at a point of the unit sphere ({@code u}) at {@code time} (ticks): a few slow, low lobes as a
     * liquid surface shivers, amplitude {@code amount} of the radius, each body its own from its seed.
     */
    public static double wobble(double[] u, double time, long seed, double amount) {
        if (amount <= 0) {
            return 1.0;
        }
        double s = (seed & 1023) / 1023.0 * Math.PI * 2.0;
        double a = Math.sin(3.0 * u[0] + 2.0 * u[1] + time * 0.55 + s);
        double b = Math.sin(2.0 * u[2] - 3.0 * u[1] + time * 0.41 + s * 1.7);
        double c = Math.sin(4.0 * u[1] + u[0] - time * 0.73 + s * 2.3);
        return 1.0 + amount * (0.5 * a + 0.35 * b + 0.25 * c) / 1.1;
    }

    /** The body's triangles, back to front from the eye at the origin, to {@code sink}. */
    public static void body(Body body, Sink sink) {
        int[] res = resolution(body.radius(), Math.sqrt(body.x() * body.x() + body.y() * body.y() + body.z() * body.z()));
        body(body, res[0], res[1], sink);
    }

    /** As above with {@code rings} rings of latitude and {@code sides} around. */
    public static void body(Body body, int rings, int sides, Sink sink) {
        double[] along = body.along() == null ? new double[] {0, 1, 0} : normalise(body.along());
        double k = Math.max(1.0, body.stretch());
        int count = (rings + 1) * sides;
        double[][] points = new double[count][];
        double[][] normals = new double[count][];
        for (int i = 0; i <= rings; i++) {
            double phi = Math.PI * i / rings;
            for (int j = 0; j < sides; j++) {
                double theta = Math.PI * 2.0 * j / sides;
                // a sphere round the line it flies along (its poles on the line): u in the body's own frame
                double[] u = frame(along, Math.sin(phi) * Math.cos(theta), Math.cos(phi), Math.sin(phi) * Math.sin(theta));
                double r = body.radius() * wobble(u, body.time(), body.seed(), body.wobble());
                double ahead = u[0] * along[0] + u[1] * along[1] + u[2] * along[2];
                // drawn out along its flight: the front rounded, the back pulled into a tail (a teardrop)
                double pull = ahead >= 0 ? (k - 1.0) * 0.35 : (k - 1.0) * 1.0;
                double[] p = {u[0] * r + along[0] * ahead * r * pull, u[1] * r + along[1] * ahead * r * pull,
                    u[2] * r + along[2] * ahead * r * pull};
                // the normal of the drawn-out surface (its stretch undone across the line)
                double squash = 1.0 / (1.0 + pull);
                double[] n = normalise(new double[] {u[0] - along[0] * ahead * (1.0 - squash), u[1] - along[1] * ahead * (1.0 - squash),
                    u[2] - along[2] * ahead * (1.0 - squash)});
                points[i * sides + j] = new double[] {body.x() + p[0], body.y() + p[1], body.z() + p[2]};
                normals[i * sides + j] = n;
            }
        }
        List<double[]> tris = new ArrayList<>();
        for (int i = 0; i < rings; i++) {
            for (int j = 0; j < sides; j++) {
                int a = i * sides + j;
                int b = i * sides + (j + 1) % sides;
                int c = (i + 1) * sides + (j + 1) % sides;
                int d = (i + 1) * sides + j;
                if (i > 0) {
                    tris.add(new double[] {a, b, c, depth(points, a, b, c)});
                }
                if (i < rings - 1) {
                    tris.add(new double[] {a, c, d, depth(points, a, c, d)});
                }
            }
        }
        tris.sort((p, q) -> Double.compare(q[3], p[3]));
        int[] colours = new int[count];
        for (int v = 0; v < count; v++) {
            colours[v] = shade(points[v], normals[v], body.opacity());
        }
        double[] xyz = new double[9];
        int[] argb = new int[3];
        for (double[] t : tris) {
            for (int k2 = 0; k2 < 3; k2++) {
                int v = (int) t[k2];
                xyz[k2 * 3] = points[v][0];
                xyz[k2 * 3 + 1] = points[v][1];
                xyz[k2 * 3 + 2] = points[v][2];
                argb[k2] = colours[v];
            }
            sink.tri(xyz.clone(), argb.clone());
        }
    }

    /**
     * 2.3.9: a ring of water (a torus) round {@code axis} through ({@code x}, {@code y}, {@code z}), eye-relative: {@code major} out from
     * the axis, its tube {@code minor} thick, wobbling, shaded and laid back to front as a body is (the Brick Fist's shockwave).
     */
    public record Ring(double x, double y, double z, double[] axis, double major, double minor, double wobble, double time, long seed,
                       double opacity) {
    }

    /** The ring's triangles, back to front from the eye at the origin, to {@code sink}. */
    public static void ring(Ring ring, int around, int tube, Sink sink) {
        double[] axis = normalise(ring.axis());
        double[] e1 = Math.abs(axis[1]) < 0.9 ? normalise(cross(axis, new double[] {0, 1, 0})) : normalise(cross(axis, new double[] {1, 0, 0}));
        double[] e2 = cross(axis, e1);
        int count = around * tube;
        double[][] points = new double[count][];
        double[][] normals = new double[count][];
        for (int i = 0; i < around; i++) {
            double u = Math.PI * 2.0 * i / around;
            double[] out = {e1[0] * Math.cos(u) + e2[0] * Math.sin(u), e1[1] * Math.cos(u) + e2[1] * Math.sin(u), e1[2] * Math.cos(u) + e2[2] * Math.sin(u)};
            for (int j = 0; j < tube; j++) {
                double v = Math.PI * 2.0 * j / tube;
                double[] n = {out[0] * Math.cos(v) + axis[0] * Math.sin(v), out[1] * Math.cos(v) + axis[1] * Math.sin(v),
                    out[2] * Math.cos(v) + axis[2] * Math.sin(v)};
                double r = ring.minor() * wobble(new double[] {out[0] * 2, out[1] * 2 + v, out[2] * 2}, ring.time(), ring.seed(), ring.wobble());
                points[i * tube + j] = new double[] {ring.x() + out[0] * ring.major() + n[0] * r, ring.y() + out[1] * ring.major() + n[1] * r,
                    ring.z() + out[2] * ring.major() + n[2] * r};
                normals[i * tube + j] = n;
            }
        }
        List<double[]> tris = new ArrayList<>();
        for (int i = 0; i < around; i++) {
            for (int j = 0; j < tube; j++) {
                int a = i * tube + j;
                int b = ((i + 1) % around) * tube + j;
                int c = ((i + 1) % around) * tube + (j + 1) % tube;
                int d = i * tube + (j + 1) % tube;
                tris.add(new double[] {a, b, c, depth(points, a, b, c)});
                tris.add(new double[] {a, c, d, depth(points, a, c, d)});
            }
        }
        tris.sort((p, q) -> Double.compare(q[3], p[3]));
        int[] colours = new int[count];
        for (int v = 0; v < count; v++) {
            colours[v] = shade(points[v], normals[v], ring.opacity());
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

    /** How far a triangle is from the eye (squared, its middle). */
    static double depth(double[][] points, int a, int b, int c) {
        double x = (points[a][0] + points[b][0] + points[c][0]) / 3.0;
        double y = (points[a][1] + points[b][1] + points[c][1]) / 3.0;
        double z = (points[a][2] + points[b][2] + points[c][2]) / 3.0;
        return x * x + y * y + z * z;
    }

    /**
     * Water's colour at a point of its surface seen from the eye at the origin: clear and deep where seen square on, the shallow tint
     * and more of it toward the rim, the sky's glint, the gathered light on the side away from the sky; {@code opacity} scales it all.
     */
    public static int shade(double[] p, double[] n, double opacity) {
        double[] v = normalise(new double[] {-p[0], -p[1], -p[2]});
        double facing = Math.abs(n[0] * v[0] + n[1] * v[1] + n[2] * v[2]);
        double fresnel = Math.pow(1.0 - facing, 2.2);
        double[] h = normalise(new double[] {SKY[0] + v[0], SKY[1] + v[1], SKY[2] + v[2]});
        double spec = Math.pow(Math.max(0.0, n[0] * h[0] + n[1] * h[1] + n[2] * h[2]), 48.0);
        double gathered = Math.pow(Math.max(0.0, -(n[0] * SKY[0] + n[1] * SKY[1] + n[2] * SKY[2])), 2.0) * 0.45;
        double[] colour = mix(rgb(DEEP), rgb(SHALLOW), Math.min(1.0, 0.25 + fresnel * 1.2));
        colour = mix(colour, rgb(GATHERED), gathered);
        colour = mix(colour, new double[] {1, 1, 1}, Math.min(1.0, spec * 1.4));
        double alpha = (0.2 + 0.62 * fresnel + 0.18 * gathered + 0.75 * spec) * opacity;
        return argb(Math.max(0.0, Math.min(1.0, alpha)), colour);
    }

    /** The point (x, y, z) of the body's own frame (y along {@code along}) in the world's. */
    static double[] frame(double[] along, double x, double y, double z) {
        double[] side = Math.abs(along[1]) < 0.9 ? normalise(cross(along, new double[] {0, 1, 0})) : normalise(cross(along, new double[] {1, 0, 0}));
        double[] up = cross(side, along);
        return new double[] {side[0] * x + along[0] * y + up[0] * z, side[1] * x + along[1] * y + up[1] * z, side[2] * x + along[2] * y + up[2] * z};
    }

    static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static double[] normalise(double[] v) {
        double l = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        return l < 1.0E-9 ? new double[] {0, 1, 0} : new double[] {v[0] / l, v[1] / l, v[2] / l};
    }

    static double[] rgb(int c) {
        return new double[] {((c >> 16) & 255) / 255.0, ((c >> 8) & 255) / 255.0, (c & 255) / 255.0};
    }

    static double[] mix(double[] a, double[] b, double t) {
        double k = Math.max(0.0, Math.min(1.0, t));
        return new double[] {a[0] + (b[0] - a[0]) * k, a[1] + (b[1] - a[1]) * k, a[2] + (b[2] - a[2]) * k};
    }

    static int argb(double alpha, double[] c) {
        return (int) Math.round(alpha * 255) << 24 | (int) Math.round(Math.max(0, Math.min(1, c[0])) * 255) << 16
            | (int) Math.round(Math.max(0, Math.min(1, c[1])) * 255) << 8 | (int) Math.round(Math.max(0, Math.min(1, c[2])) * 255);
    }
}
