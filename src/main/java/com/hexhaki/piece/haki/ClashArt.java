package com.hexhaki.piece.haki;

/**
 * 3.8.2: the Conqueror's Haki clash in the world (the owner: "a massive impact in the precise point of collision like the gif shows
 * (that ring on the point of impact)"; the GIF: a bright white-hot sphere ringed in black where the two Haki meet, one side's colour
 * flaring off one half and the other's off the other; "wind is blowing outwards from both players like they're in 1 single massive
 * spherical bubble"). Pure: quads into a {@link BoltMesh.Sink} in the eye-centred world (the eye at the origin), drawn with the Haki
 * lightning (client/haki/HakiBolts: the ink opaque, the light alpha-blended, never additive).
 */
public final class ClashArt {
    /** The impact's radius (blocks) as the clash holds, and how much bigger it bursts at the collision. */
    public static final double CORE = 1.9;
    public static final double BURST = 4.2;
    /** Ticks the collision's burst takes to settle to the holding size. */
    public static final double SETTLE = 12.0;
    static final int SIDES = 36;
    static final int WIND = 56;

    private ClashArt() {
    }

    /** The impact's radius {@code age} ticks into the clash (bursting big at the collision, then breathing). */
    public static double radius(double age) {
        double settle = Math.max(0.0, 1.0 - age / SETTLE);
        return CORE * (1.0 + 0.08 * Math.sin(age * 0.9) + 0.04 * Math.sin(age * 2.3)) + (BURST - CORE) * settle * settle;
    }

    /**
     * The impact at ({@code x}, {@code y}, {@code z}) (eye-centred), A's side the way ({@code ax}, {@code ay}, {@code az}) from it: a
     * white-hot core, a thick black ring round it, each half flaring jagged in its fighter's colour beyond the ring.
     */
    public static void impact(double x, double y, double z, double ax, double ay, double az, double age, int colorA, int colorB,
                              double shine, BoltMesh.Sink sink) {
        double[] f = facing(x, y, z);
        if (f == null) {
            return;
        }
        double[] u = {f[0], f[1], f[2]};
        double[] v = {f[3], f[4], f[5]};
        // A's way across the face of the impact (the half that flares in A's colour)
        double su = ax * u[0] + ay * u[1] + az * u[2];
        double sv = ax * v[0] + ay * v[1] + az * v[2];
        double sl = Math.hypot(su, sv);
        double side = sl < 1.0E-6 ? 0.0 : Math.atan2(sv, su);
        double r = radius(age);
        double inner = r;
        double outer = r * 1.42;
        double[] at = {x, y, z};
        double[] q = new double[12];
        int[] c = new int[4];
        int hot = 0xFFFFFFFF;
        for (int i = 0; i < SIDES; i++) {
            double a0 = Math.PI * 2.0 * i / SIDES;
            double a1 = Math.PI * 2.0 * (i + 1) / SIDES;
            double mid = (a0 + a1) / 2.0;
            boolean aSide = Math.cos(mid - side) >= 0.0;
            int colour = (aSide ? colorA : colorB) | 0xFF000000;
            // the flare beyond the ring: jagged tongues of the side's colour, flickering
            double tongue = 1.0 + 0.55 * jag(i, age) + 0.25 * Math.sin(age * 0.7 + i);
            double flare = outer + r * 1.25 * tongue;
            point(at, u, v, a0, outer * 0.98, q, 0);
            point(at, u, v, mid, flare, q, 3);
            point(at, u, v, mid, flare, q, 6);
            point(at, u, v, a1, outer * 0.98, q, 9);
            int glow = BoltMesh.scale(colour, 0.95 * shine);
            c[0] = glow;
            c[1] = 0xFF000000;
            c[2] = 0xFF000000;
            c[3] = glow;
            sink.quad(true, q.clone(), c.clone());
            // the core: white-hot at the middle, its side's colour toward the ring
            point(at, u, v, a0, 0.0, q, 0);
            point(at, u, v, a1, 0.0, q, 3);
            point(at, u, v, a1, inner * 1.02, q, 6);
            point(at, u, v, a0, inner * 1.02, q, 9);
            int rim = BoltMesh.scale(BoltMesh.mix(colour, hot, 0.55), shine);
            c[0] = BoltMesh.scale(hot, shine);
            c[1] = c[0];
            c[2] = rim;
            c[3] = rim;
            sink.quad(true, q.clone(), c.clone());
            // the black ring (opaque ink), a hair toward the eye so it lies over the core's edge
            point(at, u, v, a0, inner, q, 0);
            point(at, u, v, a1, inner, q, 3);
            point(at, u, v, a1, outer, q, 6);
            point(at, u, v, a0, outer, q, 9);
            toward(q, 0.04);
            c[0] = BoltMesh.INK;
            c[1] = BoltMesh.INK;
            c[2] = BoltMesh.INK;
            c[3] = BoltMesh.INK;
            sink.quad(false, q.clone(), c.clone());
        }
    }

    /**
     * A fighter's Haki pouring into the impact: streaks of its colour racing from {@code from} (the fighter's chest) to {@code to} (the
     * impact), round the line between, each a flowing ribbon.
     */
    public static void stream(double[] from, double[] to, double age, int color, double shine, BoltMesh.Sink sink) {
        double dx = to[0] - from[0];
        double dy = to[1] - from[1];
        double dz = to[2] - from[2];
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 0.5) {
            return;
        }
        double[] way = {dx / length, dy / length, dz / length};
        double[] across = cross(way, new double[] {0, 1, 0});
        if (norm(across) < 1.0E-6) {
            across = new double[] {1, 0, 0};
        }
        across = unit(across);
        double[] up = unit(cross(across, way));
        int streaks = 16;
        for (int k = 0; k < streaks; k++) {
            double phase = ((age * 0.09 + k * 0.618) % 1.0);
            double spin = k * 2.399963 + age * 0.05;
            double off = 0.25 + 0.9 * ((k * 0.37) % 1.0);
            double s0 = Math.max(0.0, phase - 0.28) * length;
            double s1 = phase * length;
            double squeeze0 = 1.0 - s0 / length;
            double squeeze1 = 1.0 - s1 / length;
            double[] a = along(from, way, across, up, s0, off * squeeze0, spin);
            double[] b = along(from, way, across, up, s1, off * squeeze1, spin);
            double fade = Math.sin(Math.PI * phase);
            int glow = BoltMesh.scale(BoltMesh.mix(color | 0xFF000000, 0xFFFFFFFF, 0.3), fade * shine);
            ribbon(a, b, 0.12 + 0.1 * fade, BoltMesh.scale(glow, 0.5), glow, true, sink);
        }
    }

    /**
     * The bubble round the two: the wind blowing out of its middle ({@code middle}, eye-centred) in white streaks racing out through
     * its skin, and its skin's faint rings turning.
     */
    public static void bubble(double[] middle, double radius, double age, double shine, BoltMesh.Sink sink) {
        java.util.SplittableRandom random = new java.util.SplittableRandom(0x4255_4242L);
        for (int k = 0; k < WIND; k++) {
            double[] dir = Sphere.unit(random);
            if (dir[1] < -0.35) {
                dir[1] = -dir[1] * 0.4;
                dir = unit(dir);
            }
            double phase = (age * 0.045 + random.nextDouble()) % 1.0;
            double r0 = radius * (0.25 + 0.95 * phase);
            double r1 = r0 + radius * 0.22;
            double fade = Math.sin(Math.PI * phase);
            int white = BoltMesh.scale(0xFFE8F0FF, 0.5 * fade * shine);
            double[] a = {middle[0] + dir[0] * r0, middle[1] + dir[1] * r0, middle[2] + dir[2] * r0};
            double[] b = {middle[0] + dir[0] * r1, middle[1] + dir[1] * r1, middle[2] + dir[2] * r1};
            ribbon(a, b, 0.05 + 0.04 * fade, white, BoltMesh.scale(white, 0.2), true, sink);
        }
        // the skin: three rings round the bubble, slowly turning, faint
        for (int ring = 0; ring < 3; ring++) {
            double tilt = ring * Math.PI / 3.0 + age * 0.01;
            double lift = ring == 0 ? 0.0 : 0.9;
            int n = 40;
            for (int i = 0; i < n; i++) {
                double a0 = Math.PI * 2.0 * i / n;
                double a1 = Math.PI * 2.0 * (i + 1) / n;
                double[] p0 = circle(middle, radius, a0, tilt, lift);
                double[] p1 = circle(middle, radius, a1, tilt, lift);
                double pulse = 0.5 + 0.5 * Math.sin(age * 0.2 + i * 0.6 + ring);
                int white = BoltMesh.scale(0xFFD8E6FF, (0.07 + 0.06 * pulse) * shine);
                ribbon(p0, p1, 0.06, white, white, true, sink);
            }
        }
    }

    /** Ticks the collision's pressure wave takes to sweep out. */
    public static final double WAVE = 16.0;

    /**
     * The collision's pressure wave out of the meeting point ({@code x}, {@code y}, {@code z}, eye-centred), {@code age} ticks old: a
     * ring of white-hot pressure facing the eye, racing out to {@code reach} and thinning away, the half toward A (({@code ax},
     * {@code ay}, {@code az}) from the point) in A's colour, the other in B's; and its foot on the ground ({@code floor} under the point)
     * a thin white ring racing out over it. Nothing once it has swept out.
     */
    public static void wave(double x, double y, double z, double ax, double ay, double az, double floor, double reach, double age, int colorA,
                            int colorB, double shine, BoltMesh.Sink sink) {
        if (age < 0 || age > WAVE) {
            return;
        }
        double t = age / WAVE;
        double r = reach * (1.0 - Math.pow(1.0 - t, 2.2));
        double fade = 1.0 - t;
        double width = 0.25 + reach * 0.06 * fade;
        // the ring faces the eye (seen from anywhere it is a ring racing out, as the GIF's), split across its face by A's way
        double[] f = facing(x, y, z);
        if (f == null) {
            return;
        }
        double[] p = {f[0], f[1], f[2]};
        double[] q = {f[3], f[4], f[5]};
        double su = ax * p[0] + ay * p[1] + az * p[2];
        double sv = ax * q[0] + ay * q[1] + az * q[2];
        double side = Math.hypot(su, sv) < 1.0E-6 ? 0.0 : Math.atan2(sv, su);
        int n = 48;
        double[] quad = new double[12];
        for (int i = 0; i < n; i++) {
            double a0 = Math.PI * 2.0 * i / n;
            double a1 = Math.PI * 2.0 * (i + 1) / n;
            // the standing ring: half A's colour (toward A's side of the line's up), half B's
            int colour = (Math.cos((a0 + a1) / 2.0 - side) >= 0 ? colorA : colorB) | 0xFF000000;
            int hot = BoltMesh.scale(BoltMesh.mix(colour, 0xFFFFFFFF, 0.45), fade * shine);
            int edge = BoltMesh.scale(colour, 0.35 * fade * shine);
            ring(x, y, z, p, q, a0, a1, r - width, r, quad);
            sink.quad(true, quad.clone(), new int[] {edge, edge, hot, hot});
            ring(x, y, z, p, q, a0, a1, r, r + width * 0.6, quad);
            sink.quad(true, quad.clone(), new int[] {hot, hot, edge & 0xFF000000, edge & 0xFF000000});
            // its foot on the ground, wider, white
            double[] flatP = {1, 0, 0};
            double[] flatQ = {0, 0, 1};
            int dust = BoltMesh.scale(0xFFF2EEE6, 0.6 * fade * shine);
            ring(x, y - floor + 0.05, z, flatP, flatQ, a0, a1, r * 1.6 - width * 0.8, r * 1.6, quad);
            sink.quad(true, quad.clone(), new int[] {0xFF000000, 0xFF000000, dust, dust});
            ring(x, y - floor + 0.05, z, flatP, flatQ, a0, a1, r * 1.6, r * 1.6 + width * 0.4, quad);
            sink.quad(true, quad.clone(), new int[] {dust, dust, 0xFF000000, 0xFF000000});
        }
    }

    /** A piece of a ring in the plane (p, q) round (x, y, z): from {@code a0} to {@code a1}, between radii {@code r0} and {@code r1}. */
    static void ring(double x, double y, double z, double[] p, double[] q, double a0, double a1, double r0, double r1, double[] out) {
        double c0 = Math.cos(a0);
        double s0 = Math.sin(a0);
        double c1 = Math.cos(a1);
        double s1 = Math.sin(a1);
        double[][] pts = {{r0 * c0, r0 * s0}, {r0 * c1, r0 * s1}, {r1 * c1, r1 * s1}, {r1 * c0, r1 * s0}};
        for (int k = 0; k < 4; k++) {
            out[k * 3] = x + p[0] * pts[k][0] + q[0] * pts[k][1];
            out[k * 3 + 1] = y + p[1] * pts[k][0] + q[1] * pts[k][1];
            out[k * 3 + 2] = z + p[2] * pts[k][0] + q[2] * pts[k][1];
        }
    }

    /** A point of the ring tilted {@code tilt} about the vertical, lifted {@code lift} (0 flat) toward standing. */
    static double[] circle(double[] middle, double radius, double angle, double tilt, double lift) {
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;
        double y = z * Math.sin(lift);
        z = z * Math.cos(lift);
        double tx = x * Math.cos(tilt) - z * Math.sin(tilt);
        double tz = x * Math.sin(tilt) + z * Math.cos(tilt);
        return new double[] {middle[0] + tx, middle[1] + y, middle[2] + tz};
    }

    /** A flat ribbon from {@code a} to {@code b} facing the eye, {@code half} wide each side; colours at a and at b. */
    static void ribbon(double[] a, double[] b, double half, int ca, int cb, boolean glow, BoltMesh.Sink sink) {
        double[] d = {b[0] - a[0], b[1] - a[1], b[2] - a[2]};
        double[] eye = {-(a[0] + b[0]) / 2.0, -(a[1] + b[1]) / 2.0, -(a[2] + b[2]) / 2.0};
        double[] side = cross(d, eye);
        double n = norm(side);
        if (n < 1.0E-9) {
            return;
        }
        double k = half / n;
        double[] q = {a[0] - side[0] * k, a[1] - side[1] * k, a[2] - side[2] * k, b[0] - side[0] * k, b[1] - side[1] * k,
            b[2] - side[2] * k, b[0] + side[0] * k, b[1] + side[1] * k, b[2] + side[2] * k, a[0] + side[0] * k, a[1] + side[1] * k,
            a[2] + side[2] * k};
        sink.quad(glow, q, new int[] {ca, cb, cb, ca});
    }

    /** Along the line out of {@code from}: {@code s} blocks, {@code off} round it at {@code spin}. */
    static double[] along(double[] from, double[] way, double[] across, double[] up, double s, double off, double spin) {
        double c = Math.cos(spin) * off;
        double d = Math.sin(spin) * off;
        return new double[] {from[0] + way[0] * s + across[0] * c + up[0] * d, from[1] + way[1] * s + across[1] * c + up[1] * d,
            from[2] + way[2] * s + across[2] * c + up[2] * d};
    }

    /** The two axes of a face at (x, y, z) turned to the eye (u level, v up the face), or null at the eye. */
    static double[] facing(double x, double y, double z) {
        double d = Math.sqrt(x * x + y * y + z * z);
        if (d < 1.0E-6) {
            return null;
        }
        double[] n = {-x / d, -y / d, -z / d};
        double[] u = cross(new double[] {0, 1, 0}, n);
        if (norm(u) < 1.0E-6) {
            u = new double[] {1, 0, 0};
        }
        u = unit(u);
        double[] v = unit(cross(n, u));
        return new double[] {u[0], u[1], u[2], v[0], v[1], v[2]};
    }

    static void point(double[] at, double[] u, double[] v, double angle, double r, double[] out, int i) {
        double cu = Math.cos(angle) * r;
        double cv = Math.sin(angle) * r;
        out[i] = at[0] + u[0] * cu + v[0] * cv;
        out[i + 1] = at[1] + u[1] * cu + v[1] * cv;
        out[i + 2] = at[2] + u[2] * cu + v[2] * cv;
    }

    /** Moves the quad {@code q} a little toward the eye (so its ink lies over what is in the same place). */
    static void toward(double[] q, double by) {
        for (int i = 0; i < 4; i++) {
            double x = q[i * 3];
            double y = q[i * 3 + 1];
            double z = q[i * 3 + 2];
            double d = Math.sqrt(x * x + y * y + z * z);
            if (d > by) {
                double k = (d - by) / d;
                q[i * 3] = x * k;
                q[i * 3 + 1] = y * k;
                q[i * 3 + 2] = z * k;
            }
        }
    }

    /** A tongue's reach (0..1), jagged round the ring and flickering in time. */
    static double jag(int i, double age) {
        long h = (i * 0x9E3779B97F4A7C15L) ^ ((long) Math.floor(age * 0.5) * 0xC2B2AE3D27D4EB4FL);
        h ^= h >>> 31;
        h *= 0xBF58476D1CE4E5B9L;
        return ((h >>> 11) & 0xFFFF) / 65535.0;
    }

    static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static double norm(double[] a) {
        return Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
    }

    static double[] unit(double[] a) {
        double n = Math.max(1.0E-12, norm(a));
        return new double[] {a[0] / n, a[1] / n, a[2] / n};
    }
}
