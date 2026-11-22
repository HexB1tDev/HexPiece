package com.hexhaki.piece.magu;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.1.3: the Magu Magu no Mi's magma in 3D, as Akainu's looks in the references (docs/handoff/magu-ref/sheets): magma drawn flat and
 * cel-shaded, orange-red with yellow-white hot spots, black lines of crust over it and a black outline (FireMesh.lump in the
 * {@link #MAGMA} palette), thick, dripping; fire in the game's own fire bands where it flies; black-brown smoke billowing off it; the
 * ground it lands on glowing and burning. Pure: every point relative to the eye, every quad into a {@link FireMesh.Sink}, so
 * tools/preview/FxPreview renders it off-game exactly as the game draws it; alpha blended and depth tested (client/magu/MaguFx), never
 * additive; no allocation per frame (FireMesh's own scratch).
 */
public final class MaguArt {
    private MaguArt() {
    }

    /** Magma by heat: black crust, dark red-brown crust, deep red, red, red-orange, orange, yellow-orange, yellow-white. */
    public static final int[] MAGMA = {0x120604, 0x2E0C06, 0x6A1406, 0xB0220A, 0xE04A10, 0xFF7A1A, 0xFFB43A, 0xFFF0A8};
    public static final int GLOW = 0xFF6A14;
    static final double[] B = new double[6];

    static double clamp(double v) {
        return v < 0.0 ? 0.0 : v > 1.0 ? 1.0 : v;
    }

    static double ease(double t) {
        double u = clamp(t);
        return 1.0 - (1.0 - u) * (1.0 - u);
    }

    /** One lump of magma (FireMesh.lump in {@link #MAGMA}). */
    public static void lump(FireMesh.Sink sink, double x, double y, double z, double r, double vx, double vy, double vz, double stretch, double heat,
                            double alpha, double time, int seed) {
        FireMesh.palette(MAGMA);
        FireMesh.lump(sink, x, y, z, r, vx, vy, vz, stretch, heat, 1.0, alpha, time, seed);
        FireMesh.palette(FireMesh.BAND);
    }

    /** A puff of black-brown smoke (FireMesh.SMOKE, its dark end). */
    static void smoke(FireMesh.Sink sink, double x, double y, double z, double r, double alpha, double time, int seed) {
        FireMesh.palette(FireMesh.SMOKE);
        FireMesh.puff(sink, x, y, z, r, 0, 1, 0, 0.2, 0.12, 0.08 + 0.16 * FireMesh.rand(seed, 9), 0.0, false, alpha, time, seed);
        FireMesh.palette(FireMesh.BAND);
    }

    /** A tongue of fire (the game's fire bands). */
    static void fire(FireMesh.Sink sink, double x, double y, double z, double r, double vx, double vy, double vz, double heat, double alpha, double time,
                     int seed) {
        FireMesh.puff(sink, x, y, z, r, vx, vy, vz, 0.8, 0.35, heat, 0.6, true, alpha, time, seed);
    }

    // ------------------------------------------------------------------------------------------------------ the fist

    /**
     * A fist of magma at ({@code x}, {@code y}, {@code z}) going ({@code ux}, {@code uy}, {@code uz}) (unit), {@code size} across (refs 07,
     * 14, 19: the giant fist, four knuckles and a thumb, black crust lines over it, a glow round it), fire streaming off it behind and
     * black smoke behind that.
     */
    public static void fist(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double size, double time, int seed) {
        FireMesh.basis(ux, uy, uz, B);
        // B[0..2] across the knuckles (level when the fist flies level), B[3..5] up out of the back of the hand
        double ax = B[0];
        double ay = B[1];
        double az = B[2];
        double bx = B[3];
        double by = B[4];
        double bz = B[5];
        FireMesh.shell(sink, x, y, z, size * 1.9, GLOW, 0.22, 0.0);
        lump(sink, x, y, z, size, ux, uy, uz, 0.45, 0.8, 1.0, time, seed);
        for (int k = 0; k < 4; k++) {
            double s = (k - 1.5) * 0.42 * size;
            double f = size * (0.72 - 0.05 * Math.abs(k - 1.5));
            lump(sink, x + ux * f + ax * s + bx * size * 0.18, y + uy * f + ay * s + by * size * 0.18, z + uz * f + az * s + bz * size * 0.18,
                size * 0.3, ux, uy, uz, 0.0, 0.9, 1.0, time, seed + 3 + k);
        }
        lump(sink, x + ux * size * 0.4 - ax * size * 0.75 - bx * size * 0.25, y + uy * size * 0.4 - ay * size * 0.75 - by * size * 0.25,
            z + uz * size * 0.4 - az * size * 0.75 - bz * size * 0.25, size * 0.3, ux, uy, uz, 0.3, 0.85, 1.0, time, seed + 9);
        // fire streaming off behind it, smoke behind the fire
        for (int k = 0; k < 3; k++) {
            double d = size * (1.0 + 0.6 * k);
            double r = size * (0.5 - 0.1 * k);
            fire(sink, x - ux * d, y - uy * d, z - uz * d, r, -ux, -uy, -uz, 0.82 - 0.14 * k, 0.9, time * 2.0 + k, seed + 20 + k);
        }
        for (int k = 0; k < 3; k++) {
            double d = size * (2.6 + 1.0 * k);
            smoke(sink, x - ux * d + bx * size * 0.3, y - uy * d + size * 0.3 * (1 + k), z - uz * d + bz * size * 0.3, size * (0.7 + 0.25 * k),
                0.75 - 0.18 * k, time, seed + 30 + k);
        }
    }

    // ------------------------------------------------------------------------------------------------------ magma bursting

    /** How long a burst of {@code radius} lasts (its smoke settling). */
    public static double burstLife(double radius) {
        return 34.0 + radius * 3.0;
    }

    /**
     * Magma bursting at ({@code x}, {@code y}, {@code z}) ({@code radius} blocks, {@code strength} 0 to 1; refs 12, 18, 26): a flash of fire,
     * an orange ring racing out, lumps of magma flung out on arcs and falling, a cloud of black-brown smoke billowing up and spreading.
     */
    public static void burst(FireMesh.Sink sink, double x, double y, double z, double radius, double strength, double age, int seed) {
        double life = burstLife(radius);
        if (age < 0.0 || age > life) {
            return;
        }
        double rr = Math.max(0.6, radius);
        if (age < 8.0) {
            double u = age / 8.0;
            FireMesh.shell(sink, x, y, z, rr * (0.8 + 1.4 * ease(u)), GLOW, 0.55 * (1.0 - u), 0.0);
            int n = rr > 2.5 ? 4 : 2;
            for (int i = 0; i < n; i++) {
                double a = Math.PI * 2.0 * (i + FireMesh.rand(seed, i)) / n;
                double out = rr * 0.5 * ease(u * 1.5);
                fire(sink, x + Math.cos(a) * out, y + rr * 0.25 * u, z + Math.sin(a) * out, Math.min(2.4, rr * (0.4 + 0.2 * u)) * (1.0 - 0.4 * u), Math.cos(a), 0.6,
                    Math.sin(a), 0.86 - 0.4 * u, 1.0 - u * u, age * 2.0, seed + i);
            }
            FireMesh.ring(sink, x, y + 0.1, z, 0, 1, 0, rr * 1.8 * ease(u), 0.3 + rr * 0.15, rr * 0.25, GLOW, 0.7 * (1.0 - u));
        }
        // lumps of magma thrown out, falling, landing
        int globs = (int) Math.max(3, Math.min(6, 2 + rr * 0.5) * FireMesh.quality);
        for (int i = 0; i < globs; i++) {
            int s = seed * 13 + i;
            double a = Math.PI * 2.0 * FireMesh.rand(s, 1);
            double out = (0.15 + 0.2 * FireMesh.rand(s, 2)) * (0.5 + rr * 0.25);
            double up = 0.3 + 0.35 * FireMesh.rand(s, 3) + 0.1 * strength;
            double t = Math.min(age, up / 0.04 * 2.0);
            double h = Math.max(0.0, up * t - 0.04 * t * t);
            double size = (0.15 + 0.18 * FireMesh.rand(s, 4)) * Math.min(2.0, 0.6 + rr * 0.2);
            double fade = clamp((life * 0.7 - age) / 8.0);
            lump(sink, x + Math.cos(a) * out * t, y + h + size * 0.4, z + Math.sin(a) * out * t, size, Math.cos(a) * out, up - 0.08 * t,
                Math.sin(a) * out, 0.4, 0.8 - 0.3 * clamp(age / 30.0), fade, age, s);
        }
        // the smoke cloud
        int clouds = (int) Math.max(2, Math.min(5, 2 + rr * 0.4) * FireMesh.quality);
        double u = age / life;
        for (int i = 0; i < clouds; i++) {
            int s = seed * 7 + i;
            double a = Math.PI * 2.0 * (i + FireMesh.rand(s, 1)) / clouds;
            double out = rr * (0.3 + 0.5 * FireMesh.rand(s, 2)) * ease(age / 12.0);
            double rise = rr * (0.3 + 0.9 * u) + 0.6;
            double size = rr * (0.35 + 0.2 * FireMesh.rand(s, 3)) * (0.5 + 0.8 * ease(age / 14.0));
            smoke(sink, x + Math.cos(a) * out, y + rise, z + Math.sin(a) * out, Math.min(size, 3.0), 0.8 * (1.0 - u * u), age, s);
        }
    }

    // ------------------------------------------------------------------------------------------------------ burning ground

    static final double[] RIM_A = new double[16 * 3];

    /**
     * Burning ground at ({@code x}, {@code y}, {@code z}) (refs 05, 11, 26, 38: the ground and the sea turned to glowing red magma): an
     * uneven patch {@code radius} across, yellow-orange at its heart through orange to red, a black crust at its edge, bubbles swelling and
     * bursting on it, smoke wisps rising; it cools darker and fades by {@code life}.
     */
    public static void pool(FireMesh.Sink sink, double x, double y, double z, double radius, double age, double life, int seed) {
        if (age < 0.0 || age > life) {
            return;
        }
        double grow = ease(age / 4.0);
        double cool = clamp((age - life * 0.55) / (life * 0.45));
        double alpha = 0.95 * clamp((life - age) / 12.0);
        double r = radius * grow;
        int n = 16;
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2.0 * i / n;
            double rr = r * (0.78 + 0.32 * FireMesh.rand(seed, i));
            RIM_A[i * 3] = Math.cos(a) * rr;
            RIM_A[i * 3 + 2] = Math.sin(a) * rr;
        }
        double yy = y + 0.04;
        int heart = FireMesh.argb(alpha, FireMesh.mix(0xFFC44A, 0x8A1C06, cool));
        int mid = FireMesh.argb(alpha, FireMesh.mix(0xFF6A14, 0x5A1206, cool));
        int edge = FireMesh.argb(alpha, FireMesh.mix(0xB01E08, 0x2A0A05, cool));
        int crust = FireMesh.argb(alpha, 0x140705);
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            double ax = RIM_A[i * 3];
            double az = RIM_A[i * 3 + 2];
            double bx = RIM_A[j * 3];
            double bz = RIM_A[j * 3 + 2];
            // heart to middle, middle to edge, edge to the crust outside
            FireMesh.quad(sink, x, yy, z, heart, x + ax * 0.45, yy, z + az * 0.45, mid, x + bx * 0.45, yy, z + bz * 0.45, mid, x, yy, z, heart);
            FireMesh.quad(sink, x + ax * 0.45, yy, z + az * 0.45, mid, x + ax * 0.85, yy, z + az * 0.85, edge, x + bx * 0.85, yy, z + bz * 0.85, edge,
                x + bx * 0.45, yy, z + bz * 0.45, mid);
            FireMesh.quad(sink, x + ax * 0.85, yy + 0.005, z + az * 0.85, crust, x + ax, yy + 0.005, z + az, crust, x + bx, yy + 0.005, z + bz, crust,
                x + bx * 0.85, yy + 0.005, z + bz * 0.85, crust);
        }
        // bubbles swelling and bursting
        if (cool < 0.9) {
            for (int i = 0; i < 3; i++) {
                double phase = (age * 0.12 + FireMesh.rand(seed, 40 + i)) % 1.0;
                int round = (int) (age * 0.12 + FireMesh.rand(seed, 40 + i));
                int s = seed * 31 + i * 7 + round;
                double a = Math.PI * 2.0 * FireMesh.rand(s, 1);
                double d = r * 0.6 * Math.sqrt(FireMesh.rand(s, 2));
                double size = (0.12 + 0.12 * FireMesh.rand(s, 3)) * Math.min(1.5, radius * 0.5) * Math.sin(Math.PI * phase);
                lump(sink, x + Math.cos(a) * d, yy, z + Math.sin(a) * d, size, 0, 1, 0, 0.0, 0.85 - 0.4 * cool, alpha, age, s);
            }
        }
        // smoke wisps
        for (int i = 0; i < 2; i++) {
            double phase = (age * 0.03 + FireMesh.rand(seed, 60 + i)) % 1.0;
            smoke(sink, x + (FireMesh.rand(seed, 62 + i) - 0.5) * r, yy + 0.5 + phase * 2.5, z + (FireMesh.rand(seed, 64 + i) - 0.5) * r,
                (0.3 + 0.5 * phase) * Math.min(1.5, radius * 0.4), 0.5 * (1.0 - phase) * alpha, age, seed + 70 + i);
        }
    }

    // ------------------------------------------------------------------------------------------------------ meteors

    /** A meteor of magma at ({@code x}, {@code y}, {@code z}) falling ({@code vx}, {@code vy}, {@code vz}) a tick: a fist of magma, a long trail. */
    public static void meteor(FireMesh.Sink sink, double x, double y, double z, double vx, double vy, double vz, double size, double age, int seed) {
        double vl = Math.max(1.0E-6, Math.sqrt(vx * vx + vy * vy + vz * vz));
        double ux = vx / vl;
        double uy = vy / vl;
        double uz = vz / vl;
        FireMesh.shell(sink, x, y, z, size * 1.8, GLOW, 0.22, 0.0);
        lump(sink, x, y, z, size, ux, uy, uz, 0.9, 0.8, 1.0, age, seed);
        fire(sink, x - ux * size * 1.4, y - uy * size * 1.4, z - uz * size * 1.4, size * 0.6, -ux, -uy, -uz, 0.8, 0.9, age * 2.0, seed + 1);
        smoke(sink, x - ux * size * 3.2, y - uy * size * 3.2 + size * 0.4, z - uz * size * 3.2, size * 0.9, 0.6, age, seed + 2);
        for (int k = 0; k < 6; k++) {
            double d = size * 2.0 + k * 1.1;
            FireMesh.streak(sink, x - ux * d + (FireMesh.rand(seed, k) - 0.5) * size, y - uy * d, z - uz * d + (FireMesh.rand(seed, 10 + k) - 0.5) * size,
                vx, vy, vz, 1.8, 0.08, 0xFF8A1A, 0.8 - 0.1 * k);
        }
    }

    // ------------------------------------------------------------------------------------------------------ the hound

    /**
     * Inugami Guren's hound (ref 01: hounds of magma blooming out of him): a hound's head of magma {@code scale} big surging along the ground
     * from ({@code x}, {@code y}, {@code z}) (its front, on the ground) along ({@code fx}, {@code fz}) (flat, unit), {@code age} ticks old:
     * a skull, a long snout, the lower jaw snapping open and shut, two ears laid back, glowing eyes, fangs, its neck of magma pouring
     * along the ground behind it, smoke boiling off it.
     */
    public static void hound(FireMesh.Sink sink, double x, double y, double z, double fx, double fz, double scale, double age, int seed) {
        double s = scale * ease(age / 4.0);
        if (s < 0.05) {
            return;
        }
        double sx = -fz;
        double sz = fx;
        double hy = y + 1.15 * s;
        double bite = 0.5 + 0.5 * Math.sin(age * 0.9);
        FireMesh.shell(sink, x, hy, z, s * 2.6, GLOW, 0.2, 0.0);
        // the neck pouring along the ground behind it
        for (int k = 1; k >= 1; k--) {
            double d = k * 0.9 * s;
            double r = s * (0.75 - 0.12 * k);
            double wob = Math.sin(age * 0.5 + k) * 0.12 * s;
            lump(sink, x - fx * d + sx * wob, y + r * 0.35, z - fz * d + sz * wob, r, fx, 0, fz, 0.6, 0.72, 1.0, age, seed + 40 + k);
        }
        // the skull
        lump(sink, x, hy, z, s * 0.85, fx, 0, fz, 0.2, 0.8, 1.0, age, seed);
        // the snout, long, a little down
        lump(sink, x + fx * 0.85 * s, hy - 0.05 * s, z + fz * 0.85 * s, s * 0.5, -fx, 0, -fz, 0.6, 0.85, 1.0, age, seed + 1);
        lump(sink, x + fx * 1.25 * s, hy - 0.1 * s, z + fz * 1.25 * s, s * 0.36, -fx, 0, -fz, 0.4, 0.88, 1.0, age, seed + 3);
        // the mouth's dark inside, and the lower jaw snapping under it
        double drop = (0.3 + 0.35 * bite) * s;
        lump(sink, x + fx * 0.95 * s, hy - 0.2 * s - drop * 0.4, z + fz * 0.95 * s, s * 0.34, -fx, 0, -fz, 0.6, 0.18, 1.0, age, seed + 4);
        lump(sink, x + fx * 0.9 * s, hy - 0.2 * s - drop, z + fz * 0.9 * s, s * 0.36, -fx, 0, -fz, 0.9, 0.75, 1.0, age, seed + 2);
        for (int side = -1; side <= 1; side += 2) {
            // ears pricked up and back
            lump(sink, x - fx * 0.3 * s + sx * side * 0.42 * s, hy + 0.8 * s, z - fz * 0.3 * s + sz * side * 0.42 * s, s * 0.3, -fx * 0.4, -1.0, -fz * 0.4,
                1.3, 0.72, 1.0, age, seed + 6 + side);
            // a glowing eye on the brow
            FireMesh.shell(sink, x + fx * 0.86 * s + sx * side * 0.34 * s, hy + 0.34 * s, z + fz * 0.86 * s + sz * side * 0.34 * s, s * 0.15, 0xFFF4B0,
                1.0, 0.7);
        }
        // fangs down from the snout, up from the jaw
        for (int k = 0; k < 4; k++) {
            double off = (k - 1.5) * 0.16 * s;
            double fxp = x + fx * 1.3 * s + sx * off;
            double fzp = z + fz * 1.3 * s + sz * off;
            FireMesh.streak(sink, fxp, hy - 0.35 * s, fzp, 0, -1, 0, 0.35 * s, 0.05 * s, 0xFFE9A0, 0.95);
            FireMesh.streak(sink, fxp - fx * 0.15 * s, hy - 0.1 * s - drop, fzp - fz * 0.15 * s, 0, 1, 0, 0.25 * s, 0.04 * s, 0xFFE9A0, 0.9);
        }
        // smoke boiling off it
        for (int k = 0; k < 2; k++) {
            double phase = (age * 0.08 + k / 2.0) % 1.0;
            smoke(sink, x - fx * (0.6 + 1.6 * phase) * s, hy + (0.8 + 1.4 * phase) * s, z - fz * (0.6 + 1.6 * phase) * s, s * (0.4 + 0.5 * phase),
                0.7 * (1.0 - phase), age, seed + 60 + k);
        }
    }

    // ------------------------------------------------------------------------------------------------------ geysers, the stream, the spike

    public static final double GEYSER_LIFE = 28.0;

    /** A geyser of magma out of the ground at ({@code x}, {@code y}, {@code z}), {@code height} high: up in 4 ticks, a crown of globs, falling back. */
    public static void geyser(FireMesh.Sink sink, double x, double y, double z, double height, double age, int seed) {
        if (age < 0.0 || age > GEYSER_LIFE) {
            return;
        }
        double up = ease(age / 4.0);
        double down = clamp((age - 10.0) / 12.0);
        double top = height * up * (1.0 - down);
        FireMesh.shell(sink, x, y + top * 0.5, z, 1.2 + top * 0.3, GLOW, 0.3 * (1.0 - down), 0.0);
        int n = 5;
        for (int k = 0; k < n; k++) {
            double t = (k + 0.5) / n;
            double r = 1.0 - 0.45 * t;
            lump(sink, x + Math.sin(age * 0.7 + k) * 0.12, y + top * t, z + Math.cos(age * 0.6 + k) * 0.12, r * (1.0 - 0.5 * down), 0, -1, 0, 0.8, 0.8,
                1.0 - down * down, age, seed + k);
        }
        // the crown thrown out at the top, falling back
        for (int i = 0; i < 4; i++) {
            int s = seed * 11 + i;
            double a = Math.PI * 2.0 * (i + FireMesh.rand(s, 1)) / 4.0;
            double t = Math.max(0.0, age - 3.0);
            double out = (0.08 + 0.06 * FireMesh.rand(s, 2)) * t;
            double h = height + 0.25 * t - 0.035 * t * t;
            if (age < 3.0 || h < 0.0) {
                continue;
            }
            lump(sink, x + Math.cos(a) * out, y + h, z + Math.sin(a) * out, 0.22, Math.cos(a), 0.25 - 0.07 * t, Math.sin(a), 0.4, 0.75,
                clamp((GEYSER_LIFE - age) / 6.0), age, s);
        }
        // the splash at its foot
        lump(sink, x, y, z, 1.0 * (1.0 - 0.6 * down), 0, 1, 0, 0.0, 0.85, 1.0 - down, age, seed + 20);
    }

    /** One's body as a stream of magma along the ground: its way ({@code points}: x, y, z of the feet, newest first), a lump at its head. */
    public static void stream(FireMesh.Sink sink, double[] points, int count, double time, int seed) {
        // at most seven lumps along it (every other tick's place), big enough to run into each other: one stream, at a phone's cost
        for (int i = count - 1; i >= 0; i -= 2) {
            double t = i / (double) Math.max(1, count - 1);
            double r = 1.05 - 0.45 * t;
            lump(sink, points[i * 3], points[i * 3 + 1] - 0.15, points[i * 3 + 2], r, 0, 1, 0, 0.0, 0.85 - 0.3 * t, 1.0 - 0.6 * t, time, seed + i);
        }
        if (count > 0) {
            FireMesh.shell(sink, points[0], points[1] + 0.4, points[2], 1.6, GLOW, 0.3, 0.0);
            smoke(sink, points[0], points[1] + 1.4, points[2], 0.6, 0.5, time, seed + 99);
        }
    }

    public static final double PIERCE_LIFE = 16.0;

    /**
     * Meigo's fist through them (refs 00, 44): a spike of magma thrusting out of their back along ({@code ux}, {@code uy}, {@code uz})
     * (unit) from ({@code x}, {@code y}, {@code z}), a flash of yellow-white heat, magma splashing on ahead, dripping.
     */
    public static void pierce(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double strength, double age, int seed) {
        if (age < 0.0 || age > PIERCE_LIFE) {
            return;
        }
        double out = 2.2 * strength * ease(age / 2.5);
        double fade = clamp((PIERCE_LIFE - age) / 6.0);
        if (age < 5.0) {
            FireMesh.shell(sink, x, y, z, 1.4 * strength, GLOW, 0.5 * (1.0 - age / 5.0), 0.0);
        }
        lump(sink, x + ux * out * 0.35, y + uy * out * 0.35, z + uz * out * 0.35, 0.5 * strength, ux, uy, uz, 1.4, 0.85, fade, age, seed);
        lump(sink, x + ux * out * 0.75, y + uy * out * 0.75, z + uz * out * 0.75, 0.45 * strength, ux, uy, uz, 1.0, 0.9, fade, age, seed + 2);
        lump(sink, x + ux * out, y + uy * out, z + uz * out, 0.42 * strength, ux, uy, uz, 0.5, 0.95, fade, age, seed + 1);
        for (int i = 0; i < 4; i++) {
            int s = seed * 5 + i;
            double t = age;
            double spread = 0.35;
            double vx = ux + (FireMesh.rand(s, 1) - 0.5) * spread * 2;
            double vy = uy + (FireMesh.rand(s, 2) - 0.2) * spread;
            double vz = uz + (FireMesh.rand(s, 3) - 0.5) * spread * 2;
            double speed = 0.25 + 0.2 * FireMesh.rand(s, 4);
            lump(sink, x + vx * speed * t, y + vy * speed * t - 0.03 * t * t, z + vz * speed * t, 0.13 * strength, vx, vy, vz, 0.6, 0.85, fade, age, s);
        }
        fire(sink, x + ux * out, y + uy * out, z + uz * out, 0.6 * strength, ux, uy, uz, 0.95, 0.9 * fade * (1.0 - clamp(age / 8.0)), age * 2.0, seed + 30);
    }

    /** Dai Funka gathering: the fist swelling into magma ({@code radius}), bubbling, dripping, a little smoke. */
    public static void swell(FireMesh.Sink sink, double x, double y, double z, double radius, double age, int seed) {
        FireMesh.shell(sink, x, y, z, radius * 2.0, GLOW, 0.25, 0.0);
        lump(sink, x, y, z, radius * (1.0 + 0.05 * Math.sin(age * 1.7)), 0, -1, 0, 0.15, 0.85, 1.0, age * 2.0, seed);
        for (int i = 0; i < 2; i++) {
            double phase = (age * 0.09 + i * 0.5) % 1.0;
            lump(sink, x + (FireMesh.rand(seed + (int) (age * 0.09 + i * 0.5), i) - 0.5) * radius, y - radius - phase * 1.2, z, radius * 0.18 * (1.0 - phase),
                0, -1, 0, 0.8, 0.8, 1.0 - phase, age, seed + 10 + i);
        }
        double rise = (age * 0.05) % 1.0;
        smoke(sink, x, y + radius + rise * 1.4, z, radius * (0.5 + 0.6 * rise), 0.45 * (1.0 - rise), age, seed + 20);
    }
}
