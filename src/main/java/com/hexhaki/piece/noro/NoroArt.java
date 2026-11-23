package com.hexhaki.piece.noro;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.3.4: the Noro Noro no Mi's photons in 3D, after the owner's reference (Foxy's Noroma photons: concentric pink-violet rings of light
 * flying out of the hands, a small dark purple ball at their head). Built in the house style (POINT ONE: FireMesh's soft rings, glow
 * shells and rays, alpha blended, depth tested, never additive), eye-centred like every fruit's art (x, y, z from the eye), pure so the
 * previews draw the very same (tools/preview/NoroShots).
 */
public final class NoroArt {
    /** The photons' colours: their pink, the violet round them, the dark purple heart, a white flash. */
    public static final int PINK = 0xF4A3EC;
    public static final int VIOLET = 0xA65CDD;
    public static final int CORE = 0x4E1F66;
    public static final int WHITE = 0xFFF2FF;
    /** How long a burst's end, a held blow, a release, a reflection and the snap show (ticks). */
    public static final double POP_LIFE = 10;
    public static final double STORE_LIFE = 8;
    public static final double RELEASE_LIFE = 18;
    public static final double SNAP_LIFE = 12;
    /** The beam's rings: one every so far along it. */
    static final double BEAM_STEP = 0.7;

    private NoroArt() {
    }

    /** A burst of photons at (x, y, z) flying along (ux, uy, uz) (unit): its dark heart in a pink glow, rings swelling out behind it. */
    public static void shot(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double age) {
        FireMesh.shell(sink, x, y, z, 0.2, CORE, 0.95, 0.7);
        FireMesh.shell(sink, x, y, z, 0.42, PINK, 0.4, 0.0);
        double slide = (age * 0.9) % 1.0;
        for (int k = 0; k < 4; k++) {
            double t = k + slide;
            double back = 0.25 + t * 0.45;
            double radius = 0.26 + t * 0.2;
            double alpha = 0.95 * (1.0 - t / 4.0);
            FireMesh.ring(sink, x - ux * back, y - uy * back, z - uz * back, ux, uy, uz, radius, 0.18, 0.0, k % 2 == 0 ? PINK : VIOLET, alpha);
        }
    }

    /** A burst ending at (x, y, z): rings flashing out every way ({@code caught}: brighter, it slowed something). */
    public static void pop(FireMesh.Sink sink, double x, double y, double z, double age, boolean caught) {
        double t = age / POP_LIFE;
        if (t >= 1.0) {
            return;
        }
        double fade = (1.0 - t) * (caught ? 1.0 : 0.6);
        double radius = 0.2 + (caught ? 1.3 : 0.8) * Math.sqrt(t);
        FireMesh.ring(sink, x, y, z, 0, 1, 0, radius, 0.16, 0.0, PINK, 0.8 * fade);
        FireMesh.ring(sink, x, y, z, 1, 0, 0, radius * 0.85, 0.12, 0.0, VIOLET, 0.6 * fade);
        FireMesh.ring(sink, x, y, z, 0, 0, 1, radius * 0.7, 0.1, 0.0, PINK, 0.5 * fade);
        if (t < 0.4) {
            FireMesh.shell(sink, x, y, z, 0.25 + radius * 0.4, WHITE, 0.55 * (1.0 - t / 0.4), 0.0);
        }
    }

    /**
     * The steady beam from (ax, ay, az) to (bx, by, bz): rings one every {@link #BEAM_STEP} round the way, sliding along it, a soft pink
     * glow down its middle, a ball of light at the hands.
     */
    public static void beam(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double age) {
        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 0.05) {
            return;
        }
        double ux = dx / length;
        double uy = dy / length;
        double uz = dz / length;
        double grow = Math.min(1.0, age / 4.0);
        FireMesh.shell(sink, ax, ay, az, 0.3 * grow + 0.05, PINK, 0.55, 0.0);
        FireMesh.ray(sink, ax, ay, az, ux, uy, uz, length, 0.16, 0, 0, PINK, 0.45 * grow);
        double slide = (age * 0.35) % 1.0;
        int n = (int) Math.ceil(length / BEAM_STEP);
        for (int k = 0; k < n; k++) {
            double s = (k + slide) * BEAM_STEP;
            if (s > length) {
                break;
            }
            double end = Math.min(1.0, (length - s) / 1.5);
            // the rings swell as they go, as Foxy's do toward what they hit
            double radius = 0.24 + 0.08 * Math.sin(age * 0.5 - k * 0.9) + Math.min(0.55, s * 0.03);
            FireMesh.ring(sink, ax + ux * s, ay + uy * s, az + uz * s, ux, uy, uz, radius, 0.15, 0.0, k % 2 == 0 ? PINK : VIOLET,
                0.85 * grow * end);
        }
    }

    /**
     * 3.4.2: Noroma Spread held, a large cone of photons out of the hands (x, y, z) along (ux, uy, uz) (unit), {@code range} long,
     * {@code degrees} each side: photon rings one every {@link #CONE_STEP} along it, each as wide as the cone is there, sliding outward
     * and swelling, pink and violet by turns; a faint cone of light under them (brightest at the hands, clear at its far rim); motes of
     * light drifting out; a ball of light at the hands. {@code alpha} of it there (it fades in over 4 ticks and out when let go).
     */
    public static void cone(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double range, double degrees,
                            double age, double alpha) {
        double grow = Math.min(1.0, age / 4.0) * alpha;
        if (grow < 0.01) {
            return;
        }
        double tan = Math.tan(Math.toRadians(degrees));
        double reach = range * Math.min(1.0, age / 6.0 + 0.15);
        FireMesh.basis(ux, uy, uz, CB);
        // the cone's light: sixteen long thin triangles from the hands to the far rim, bright at the hands, clear at the rim
        int sides = 16;
        double rim = reach * tan;
        for (int k = 0; k < sides; k++) {
            double a0 = 2 * Math.PI * k / sides;
            double a1 = 2 * Math.PI * (k + 1) / sides;
            double ex = x + ux * reach;
            double ey = y + uy * reach;
            double ez = z + uz * reach;
            double p0x = ex + (CB[0] * Math.cos(a0) + CB[3] * Math.sin(a0)) * rim;
            double p0y = ey + (CB[1] * Math.cos(a0) + CB[4] * Math.sin(a0)) * rim;
            double p0z = ez + (CB[2] * Math.cos(a0) + CB[5] * Math.sin(a0)) * rim;
            double p1x = ex + (CB[0] * Math.cos(a1) + CB[3] * Math.sin(a1)) * rim;
            double p1y = ey + (CB[1] * Math.cos(a1) + CB[4] * Math.sin(a1)) * rim;
            double p1z = ez + (CB[2] * Math.cos(a1) + CB[5] * Math.sin(a1)) * rim;
            int c0 = FireMesh.argb(0.16 * grow, PINK);
            int c1 = FireMesh.argb(0.0, VIOLET);
            FireMesh.quad(sink, x, y, z, c0, x, y, z, c0, p1x, p1y, p1z, c1, p0x, p0y, p0z, c1);
        }
        FireMesh.shell(sink, x, y, z, 0.3 * grow + 0.05, PINK, 0.5, 0.0);
        double slide = (age * 0.3) % 1.0;
        int n = (int) Math.ceil(reach / CONE_STEP);
        for (int k = 0; k < n; k++) {
            double s = (k + slide) * CONE_STEP;
            if (s > reach || s < 0.4) {
                continue;
            }
            double fade = Math.min(1.0, (reach - s) / 2.0) * Math.min(1.0, s / 1.5);
            double radius = s * tan * (0.92 + 0.06 * Math.sin(age * 0.5 - k));
            FireMesh.ring(sink, x + ux * s, y + uy * s, z + uz * s, ux, uy, uz, radius, 0.16 + 0.03 * s, 0.0, k % 2 == 0 ? PINK : VIOLET,
                0.75 * grow * fade);
        }
        // motes drifting out through it
        for (int k = 0; k < 10; k++) {
            double f = ((age * 0.04 + k * 0.137) % 1.0);
            double s = f * reach;
            double a = k * 2.39996;
            double r = s * tan * (0.25 + 0.6 * ((k * 0.61) % 1.0));
            FireMesh.shell(sink, x + ux * s + (CB[0] * Math.cos(a) + CB[3] * Math.sin(a)) * r, y + uy * s + (CB[1] * Math.cos(a) + CB[4] * Math.sin(a)) * r,
                z + uz * s + (CB[2] * Math.cos(a) + CB[5] * Math.sin(a)) * r, 0.16 + 0.05 * s / range, k % 2 == 0 ? PINK : WHITE, 0.8 * grow * (1.0 - f), 0.2);
        }
    }

    /** How far apart the cone's rings are. */
    public static final double CONE_STEP = 1.6;
    static final double[] CB = new double[6];

    /**
     * A slowed body (its middle at (x, y, z), {@code wide} across, {@code tall} high): three photon rings round it turning slowly, tilted
     * every way, and motes drifting round it; {@code left} 0 to 1 of its time still to come (it fades out over the last tenth).
     */
    public static void aura(FireMesh.Sink sink, double x, double y, double z, double wide, double tall, double age, double left) {
        double alpha = 0.65 * Math.min(1.0, left * 10.0) * Math.min(1.0, age / 6.0);
        double radius = wide * 0.75 + 0.25;
        for (int k = 0; k < 3; k++) {
            double spin = age * 0.03 + k * 2.1;
            double tilt = 0.35 + 0.15 * k;
            double ax = Math.sin(spin) * tilt;
            double az = Math.cos(spin) * tilt;
            double l = Math.sqrt(ax * ax + 1 + az * az);
            double h = (k - 1) * tall * 0.28;
            FireMesh.ring(sink, x, y + h, z, ax / l, 1 / l, az / l, radius * (1.0 - 0.12 * Math.abs(k - 1)), 0.12, 0.0,
                k == 1 ? VIOLET : PINK, alpha);
        }
        for (int m = 0; m < 4; m++) {
            double a = age * 0.04 + m * Math.PI / 2;
            double mh = Math.sin(age * 0.05 + m) * tall * 0.4;
            FireMesh.shell(sink, x + Math.cos(a) * radius, y + mh, z + Math.sin(a) * radius, 0.06, WHITE, alpha * 1.4, 0.0);
        }
    }

    /** A slowed shot (arrow, fireball) at (x, y, z): one small ring round it and a faint glow. */
    public static void slowShot(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double age) {
        FireMesh.ring(sink, x, y, z, ux, uy, uz, 0.3 + 0.05 * Math.sin(age * 0.2), 0.08, 0.0, PINK, 0.55);
        FireMesh.shell(sink, x, y, z, 0.3, VIOLET, 0.25, 0.0);
    }

    /** A blow held back on a slowed body at (x, y, z), struck along (wx, wz): a small pink flash, short rays bursting off it. */
    public static void store(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double age) {
        double t = age / STORE_LIFE;
        if (t >= 1.0) {
            return;
        }
        double fade = 1.0 - t;
        FireMesh.shell(sink, x, y, z, 0.18 + 0.35 * t, PINK, 0.6 * fade, 0.0);
        FireMesh.ring(sink, x, y, z, wx, 0.0, wz, 0.15 + 0.6 * t, 0.1, 0.0, WHITE, 0.7 * fade);
    }

    /** The held blows landing at once at (x, y, z): a white flash, a shock shell and rings tearing outward, as big as {@code size}. */
    public static void release(FireMesh.Sink sink, double x, double y, double z, double size, double age) {
        double t = age / RELEASE_LIFE;
        if (t >= 1.0) {
            return;
        }
        double fade = 1.0 - t;
        double reach = 0.6 + (1.5 + 3.0 * size) * Math.sqrt(t);
        if (t < 0.3) {
            FireMesh.shell(sink, x, y, z, 0.4 + size * 0.8, WHITE, 0.8 * (1.0 - t / 0.3), 0.0);
        }
        FireMesh.shell(sink, x, y, z, reach * 0.8, PINK, 0.0, 0.45 * fade);
        FireMesh.ring(sink, x, y, z, 0, 1, 0, reach, 0.3, 0.1, PINK, 0.8 * fade);
        FireMesh.ring(sink, x, y, z, 0.6, 0.8, 0, reach * 0.8, 0.22, 0.0, VIOLET, 0.6 * fade);
        FireMesh.ring(sink, x, y, z, -0.6, 0.8, 0, reach * 0.65, 0.2, 0.0, PINK, 0.55 * fade);
    }

    /**
     * The Noroma Mirror (its middle at (x, y, z), facing g's way, g's own across and up): a glassy pale lilac square with a bright
     * streak across it and a pink frame, forming over its first 5 ticks and fading over its last 10.
     */
    public static void mirror(FireMesh.Sink sink, double x, double y, double z, NoroMirror g, double age, double life) {
        double form = Math.min(1.0, age / 5.0);
        double fade = Math.min(1.0, (life - age) / 10.0);
        if (fade <= 0.0) {
            return;
        }
        double h = NoroMirror.HALF * (0.4 + 0.6 * form);
        double a = form * fade;
        int glass = FireMesh.argb(0.22 * a, 0xE6D2FF);
        int edge = FireMesh.argb(0.32 * a, 0xF7EAFF);
        // the glass, a little brighter toward its edges
        corners(x, y, z, g, h, h);
        FireMesh.quad(sink, Q[0], Q[1], Q[2], edge, Q[3], Q[4], Q[5], glass, Q[6], Q[7], Q[8], edge, Q[9], Q[10], Q[11], glass);
        // a bright streak across it, sliding
        double s = ((age * 0.04) % 1.4) - 0.7;
        int shine = FireMesh.argb(0.5 * a, WHITE);
        int clear = FireMesh.argb(0.0, WHITE);
        double sx = s * h;
        FireMesh.quad(sink, x + g.rx * (sx - 0.15) + g.ux * h, y + g.ry * (sx - 0.15) + g.uy * h, z + g.rz * (sx - 0.15) + g.uz * h, clear,
            x + g.rx * (sx + 0.15) + g.ux * h, y + g.ry * (sx + 0.15) + g.uy * h, z + g.rz * (sx + 0.15) + g.uz * h, shine,
            x + g.rx * (sx - 0.45) - g.ux * h, y + g.ry * (sx - 0.45) - g.uy * h, z + g.rz * (sx - 0.45) - g.uz * h, shine,
            x + g.rx * (sx - 0.75) - g.ux * h, y + g.ry * (sx - 0.75) - g.uy * h, z + g.rz * (sx - 0.75) - g.uz * h, clear);
        // the frame: four bars a little in front of the glass on both sides
        int frame = FireMesh.argb(0.85 * a, PINK);
        int frameDark = FireMesh.argb(0.85 * a, VIOLET);
        double w = 0.09;
        bar(sink, x, y, z, g, -h, h, h, h, w, frame, frameDark);
        bar(sink, x, y, z, g, -h, -h, h, -h, w, frameDark, frame);
        bar(sink, x, y, z, g, -h, -h, -h, h, w, frame, frameDark);
        bar(sink, x, y, z, g, h, -h, h, h, w, frameDark, frame);
        for (int c = 0; c < 4; c++) {
            double cr = (c & 1) == 0 ? -h : h;
            double cu = (c & 2) == 0 ? -h : h;
            FireMesh.ring(sink, x + g.rx * cr + g.ux * cu, y + g.ry * cr + g.uy * cu, z + g.rz * cr + g.uz * cu, g.nx, g.ny, g.nz, 0.18, 0.08,
                0.0, PINK, 0.7 * a);
        }
    }

    private static final double[] Q = new double[12];

    static void corners(double x, double y, double z, NoroMirror g, double hr, double hu) {
        int i = 0;
        for (int k = 0; k < 4; k++) {
            double r = k == 0 || k == 3 ? -hr : hr;
            double u = k < 2 ? hu : -hu;
            Q[i++] = x + g.rx * r + g.ux * u;
            Q[i++] = y + g.ry * r + g.uy * u;
            Q[i++] = z + g.rz * r + g.uz * u;
        }
    }

    /** A bar of the mirror's frame from (r0, u0) to (r1, u1) on its face, {@code w} wide, standing out of it a little both ways. */
    static void bar(FireMesh.Sink sink, double x, double y, double z, NoroMirror g, double r0, double u0, double r1, double u1, double w,
                    int c0, int c1) {
        double dr = r1 - r0;
        double du = u1 - u0;
        double l = Math.sqrt(dr * dr + du * du);
        double pr = -du / l * w;
        double pu = dr / l * w;
        for (double off : new double[] {0.02, -0.02}) {
            double ox = g.nx * off;
            double oy = g.ny * off;
            double oz = g.nz * off;
            FireMesh.quad(sink,
                x + ox + g.rx * (r0 - pr) + g.ux * (u0 - pu), y + oy + g.ry * (r0 - pr) + g.uy * (u0 - pu), z + oz + g.rz * (r0 - pr) + g.uz * (u0 - pu), c0,
                x + ox + g.rx * (r1 - pr) + g.ux * (u1 - pu), y + oy + g.ry * (r1 - pr) + g.uy * (u1 - pu), z + oz + g.rz * (r1 - pr) + g.uz * (u1 - pu), c1,
                x + ox + g.rx * (r1 + pr) + g.ux * (u1 + pu), y + oy + g.ry * (r1 + pr) + g.uy * (u1 + pu), z + oz + g.rz * (r1 + pr) + g.uz * (u1 + pu), c1,
                x + ox + g.rx * (r0 + pr) + g.ux * (u0 + pu), y + oy + g.ry * (r0 + pr) + g.uy * (u0 + pu), z + oz + g.rz * (r0 + pr) + g.uz * (u0 + pu), c0);
        }
    }

    /**
     * The Slow Field's dome on the ground at (x, y, z), {@code radius} wide: a faint shell, rings round it at five heights drifting
     * slowly up, rising out of the ground over its first 8 ticks and fading over its last 15.
     */
    public static void dome(FireMesh.Sink sink, double x, double y, double z, double radius, double age, double life) {
        double rise = Math.min(1.0, age / 8.0);
        double fade = Math.min(1.0, (life - age) / 15.0);
        if (fade <= 0.0) {
            return;
        }
        double r = radius * (0.3 + 0.7 * rise);
        FireMesh.shell(sink, x, y, z, r, VIOLET, 0.04 * fade, 0.28 * fade);
        for (int k = 0; k < 5; k++) {
            double f = ((k + age * 0.01) % 5.0) / 5.0;
            double h = f * 0.95;
            double ring = r * Math.sqrt(Math.max(0.0, 1.0 - h * h));
            double alpha = 0.6 * fade * rise * Math.min(1.0, (1.0 - f) * 3.0) * Math.min(1.0, f * 6.0 + 0.3);
            FireMesh.ring(sink, x, y + h * r, z, 0, 1, 0, ring, 0.14, 0.0, k % 2 == 0 ? PINK : VIOLET, alpha);
        }
        FireMesh.ring(sink, x, y + 0.05, z, 0, 1, 0, r, 0.3, 0.0, PINK, 0.7 * fade * rise);
    }

    /** Time's Up: a snap of light at the hand (x, y, z): a ring flashing out and a white spark. */
    public static void snap(FireMesh.Sink sink, double x, double y, double z, double age) {
        double t = age / SNAP_LIFE;
        if (t >= 1.0) {
            return;
        }
        double fade = 1.0 - t;
        FireMesh.ring(sink, x, y, z, 0, 1, 0, 0.2 + 2.4 * Math.sqrt(t), 0.18, 0.0, PINK, 0.8 * fade);
        FireMesh.ring(sink, x, y, z, 0, 0, 1, 0.15 + 1.6 * Math.sqrt(t), 0.12, 0.0, VIOLET, 0.6 * fade);
        if (t < 0.35) {
            FireMesh.shell(sink, x, y, z, 0.25, WHITE, 0.9 * (1.0 - t / 0.35), 0.0);
        }
    }

    /** A shot turned back off the mirror at (x, y, z): a quick bright ring. */
    public static void reflect(FireMesh.Sink sink, double x, double y, double z, double age) {
        pop(sink, x, y, z, age * 1.4, true);
    }
}
