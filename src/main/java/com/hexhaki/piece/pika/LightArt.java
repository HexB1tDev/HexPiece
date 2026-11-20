package com.hexhaki.piece.pika;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 2.7.8 (the owner: "better the Lightfruit in every way, make it cooler, more dramatic ... copy his moves and how he uses them
 * exactly"): Kizaru's light as 3D shapes, worked out from where and when each move began (no game class: tools/preview/FxPreview
 * renders it off-game, the tests check it). His light is gold round a white core: every shape here is a white heart, a pale-gold to
 * gold body and a soft gold glow, cut into flat bands like the Uo fire ({@link FireMesh}, palette {@link FireMesh#LIGHT}).
 * Positions are eye-centred; directions unit.
 */
public final class LightArt {
    private LightArt() {
    }

    static final double[] B = new double[6];
    static final double[] D = new double[3];

    static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    static double ease(double t) {
        double u = clamp(t);
        return 1.0 - (1.0 - u) * (1.0 - u) * (1.0 - u);
    }

    /** A soft ball of light (never round the eye). */
    public static void glow(FireMesh.Sink sink, double x, double y, double z, double radius, int rgb, double alpha) {
        double eye = Math.sqrt(x * x + y * y + z * z);
        FireMesh.shell(sink, x, y, z, Math.min(radius, eye * 0.9), rgb, alpha, 0.0);
    }

    /** A puff of light: {@code heat} 1 white-hot, 0.7 gold. */
    public static void light(FireMesh.Sink sink, double x, double y, double z, double radius, double vx, double vy, double vz, double stretch,
                             double heat, double alpha, double time, int seed) {
        FireMesh.palette(FireMesh.LIGHT);
        FireMesh.puff(sink, x, y, z, radius, vx, vy, vz, stretch, 0.08, heat, 0.5, false, alpha, time, seed);
        FireMesh.palette(FireMesh.BAND);
    }

    static void direction(int seed, int i, double up, double[] out) {
        double a = FireMesh.rand(seed, i * 3) * Math.PI * 2.0;
        double y = FireMesh.rand(seed, i * 3 + 1) * 2.0 - 1.0;
        y = y + (1.0 - y) * up * 0.5;
        double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
        out[0] = Math.cos(a) * r;
        out[1] = y;
        out[2] = Math.sin(a) * r;
    }

    // ------------------------------------------------------------------------------------------------------ the finger laser (RMB)

    /** How long a quick laser shows (ticks). */
    public static final double LASER_LIFE = 6.0;

    /**
     * A laser from the fingertip ({@code ax}..) to where it struck ({@code bx}..), {@code age} ticks old: a thin white-hot line with a
     * gold glow round it, a flare at the finger, a flash where it hit; it thins out and is gone in {@link #LASER_LIFE} ticks.
     */
    public static void laser(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double width, double age,
                             int seed) {
        if (age < 0.0 || age > LASER_LIFE) {
            return;
        }
        double t = age / LASER_LIFE;
        double thin = Math.sqrt(1.0 - t);
        FireMesh.tube(sink, ax, ay, az, bx, by, bz, width * 0.32 * thin, width * 0.28 * thin, 0xFFFFFF, 0xFFF9D6, 0xFFEF9A, 0.3, 1.0, 0.0, age,
            seed);
        FireMesh.tube(sink, ax, ay, az, bx, by, bz, width * 1.1 * thin, width * 1.0 * thin, 0xFFE05E, 0xFFD040, 0xFFC020, 0.0,
            0.35 * (1.0 - t), 0.0, age, seed + 1);
        glow(sink, ax, ay, az, width * 3.0 * (1.0 - t), 0xFFF0A0, 0.6 * (1.0 - t));
        impact(sink, bx, by, bz, width * 4.0, age, seed + 2);
    }

    /** A flash where light struck: a white-hot puff, its glow, a ring and short rays, gone in 7 ticks. */
    public static void impact(FireMesh.Sink sink, double x, double y, double z, double size, double age, int seed) {
        if (age < 0.0 || age > 7.0) {
            return;
        }
        double t = age / 7.0;
        light(sink, x, y, z, size * 0.4 * (1.0 - t) * (0.6 + 0.4 * ease(age / 1.5)), 0, 1, 0, 0.1, 1.0 - 0.3 * t, 1.0, age, seed);
        glow(sink, x, y, z, size * (0.8 + 0.8 * ease(t)), 0xFFE680, 0.7 * (1.0 - t));
        FireMesh.ring(sink, x, y, z, 0, 1, 0, size * (0.3 + 1.2 * ease(t)), size * 0.25 * (1.0 - t) + 0.05, 0.0, 0xFFFFFF, 0.8 * (1.0 - t));
        for (int i = 0; i < 6; i++) {
            direction(seed, i, 0.0, D);
            FireMesh.ray(sink, x, y, z, D[0], D[1], D[2], size * (0.6 + 1.2 * ease(t)) * (0.6 + 0.4 * FireMesh.rand(seed, 30 + i)), size * 0.06, 0,
                0.0, 0xFFF6C0, 0.95 * (1.0 - t));
        }
    }

    // ------------------------------------------------------------------------------------------------------ the beam (RMB held)

    /**
     * Light gathered at the fingertip ({@code charge} 0 to 1, {@code age} ticks since it began): a growing white-hot point with gold
     * round it, beams of light turning out of it, sparks drawn in.
     */
    public static void charge(FireMesh.Sink sink, double x, double y, double z, double age, double charge, int seed) {
        double c = clamp(charge);
        double r = 0.08 + 0.3 * c;
        double pulse = 1.0 + 0.12 * Math.sin(age * 2.4);
        glow(sink, x, y, z, (0.6 + 1.8 * c) * pulse, 0xFFE070, 0.5 + 0.2 * c);
        light(sink, x, y, z, r * pulse, 0, 1, 0, 0.0, 1.0, 1.0, age * 2.0, seed);
        for (int k = 0; k < 6; k++) {
            direction(seed + (int) (age / 4.0), k, 0.0, D);
            double flick = 0.7 + 0.3 * Math.sin(age * 3.1 + k);
            FireMesh.ray(sink, x, y, z, D[0], D[1], D[2], (0.5 + 2.5 * c) * flick, 0.03 + 0.04 * c, 0, 0.0, 0xFFF8D0, 0.9 * flick);
        }
        for (int k = 0; k < 8; k++) {
            double life = 6.0;
            double t = ((age + k * 0.75) % life) / life;
            direction(seed + 70, k + (int) ((age + k * 0.75) / life) * 8, 0.0, D);
            double from = (1.5 + 1.5 * c) * (1.0 - t);
            FireMesh.streak(sink, x + D[0] * from, y + D[1] * from, z + D[2] * from, -D[0], -D[1], -D[2], 0.3 + 0.4 * c, 0.025, 0xFFD040,
                Math.sin(Math.PI * t));
        }
    }

    /** How long a charged beam burns (ticks). */
    public static final double BEAM_LIFE = 12.0;

    /**
     * The charged beam from ({@code ax}..) to ({@code bx}..), {@code width} across, {@code age} ticks old: a thick white-hot core in a
     * gold sheath, rings of light racing along it, a flare at the hand; it narrows and is gone in {@link #BEAM_LIFE} ticks.
     */
    public static void beam(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double width, double age,
                            int seed) {
        if (age < 0.0 || age > BEAM_LIFE) {
            return;
        }
        double t = age / BEAM_LIFE;
        double grow = ease(age / 1.5);
        double thin = (1.0 - t * t) * grow;
        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        double len = Math.max(1.0E-6, Math.sqrt(dx * dx + dy * dy + dz * dz));
        double ux = dx / len;
        double uy = dy / len;
        double uz = dz / len;
        FireMesh.tube(sink, ax, ay, az, bx, by, bz, width * 0.4 * thin, width * 0.36 * thin, 0xFFFFFF, 0xFFF9D6, 0xFFEF9A, 0.35, 1.0, 0.12, age,
            seed);
        FireMesh.tube(sink, ax, ay, az, bx, by, bz, width * 1.05 * thin, width * 0.95 * thin, 0xFFE05E, 0xFFC830, 0xFFB020, 0.0, 0.4, 0.18, age,
            seed + 1);
        glow(sink, ax, ay, az, width * 3.5 * thin, 0xFFF0A0, 0.7);
        // rings racing out along it
        for (int k = 0; k < 6; k++) {
            double along = ((age * 6.0 + k * len / 6.0) % len);
            double fade = (1.0 - t) * Math.sin(Math.PI * along / len);
            FireMesh.ring(sink, ax + ux * along, ay + uy * along, az + uz * along, ux, uy, uz, width * (1.0 + 0.6 * FireMesh.rand(seed, k)), width * 0.3,
                -width * 0.6, 0xFFFFFF, 0.75 * fade);
        }
        burst(sink, bx, by, bz, width * 2.5, age, seed + 9);
    }

    // ------------------------------------------------------------------------------------------------------ his explosions

    /** How long a burst of light lasts (ticks). */
    public static final double BURST_LIFE = 22.0;

    /**
     * Light blowing up ({@code radius}, {@code age} ticks old), as his lasers and bullets do: a white dome of light, a gold ball
     * of puffs, beams thrown out all round, a ring over the ground, sparks, then dust.
     */
    public static void burst(FireMesh.Sink sink, double x, double y, double z, double radius, double age, int seed) {
        if (age < 0.0 || age > BURST_LIFE) {
            return;
        }
        double rr = Math.max(0.5, radius);
        double t = age / BURST_LIFE;
        glow(sink, x, y, z, rr * 2.4 * (0.6 + 0.4 * ease(age / 3.0)), 0xFFE680, 0.8 * Math.max(0.0, 1.0 - age / 10.0));
        if (age < 6.0) {
            double f = Math.sin(Math.PI * Math.min(1.0, age / 6.0 + 0.1));
            light(sink, x, y, z, rr * 0.8 * f, 0, 1, 0, 0.1, 1.05, 1.0, age, seed);
        }
        int n = Math.min(12, 6 + (int) rr);
        for (int i = 0; i < n; i++) {
            int s = seed * 11 + i;
            direction(s, 0, 0.45, D);
            double out = rr * (0.3 + 0.6 * FireMesh.rand(s, 5)) * ease(age / 5.0);
            double size = rr * (0.35 + 0.2 * FireMesh.rand(s, 6)) * (1.0 - t * t) * (0.6 + 0.4 * ease(age / 3.0));
            double heat = 1.0 - 0.6 * t;
            light(sink, x + D[0] * out, y + D[1] * out + t * rr * 0.4, z + D[2] * out, size, D[0], D[1], D[2], 0.4, heat, 1.0, age, s);
        }
        if (age < 10.0) {
            double u = age / 10.0;
            FireMesh.shell(sink, x, y, z, rr * (0.6 + 1.8 * Math.sqrt(u)), 0xFFFBE0, 0.0, 0.85 * (1.0 - u));
            FireMesh.ring(sink, x, y - rr * 0.25, z, 0, 1, 0, rr * (0.6 + 2.8 * Math.sqrt(u)), rr * 0.5 * (1.0 - u) + 0.2, rr * 0.2, 0xFFF4C0,
                0.8 * (1.0 - u));
            for (int i = 0; i < 10; i++) {
                direction(seed + 20, i, 0.3, D);
                FireMesh.ray(sink, x, y, z, D[0], D[1], D[2], rr * (1.2 + 3.0 * ease(u)) * (0.6 + 0.4 * FireMesh.rand(seed, 40 + i)),
                    rr * 0.07 * (1.0 - u) + 0.03, 0, 0.0, 0xFFF8D0, 0.95 * (1.0 - u));
            }
        }
        for (int i = 0; i < 14; i++) {
            double life = 8.0 + 5.0 * FireMesh.rand(seed + 50, i);
            if (age > life) {
                continue;
            }
            direction(seed + 51, i, 0.5, D);
            double speed = (0.5 + 0.8 * FireMesh.rand(seed + 52, i)) * Math.sqrt(rr) * 0.6;
            FireMesh.streak(sink, x + D[0] * speed * age, y + D[1] * speed * age - 0.02 * age * age, z + D[2] * speed * age, D[0], D[1] - 0.04 * age,
                D[2], 0.8 + rr * 0.2, 0.06 + 0.02 * rr, 0xFFC030, 1.0 - age / life);
        }
        if (age > 8.0) {
            double u = (age - 8.0) / (BURST_LIFE - 8.0);
            FireMesh.palette(FireMesh.SMOKE);
            for (int i = 0; i < 5; i++) {
                direction(seed + 60, i, 0.6, D);
                double out = rr * (0.4 + 0.5 * FireMesh.rand(seed + 61, i)) * (1.0 + 0.5 * u);
                FireMesh.puff(sink, x + D[0] * out, y + D[1] * out * 0.5 + u * rr * 0.8, z + D[2] * out, rr * 0.45 * (0.9 + 0.5 * u), 0, 1, 0, 0.3,
                    0.1, 0.8, 0.3, false, 0.55 * (1.0 - u) * (1.0 - u), age, seed + 70 + i);
            }
            FireMesh.palette(FireMesh.BAND);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Yasakani no Magatama

    /**
     * One of Yasakani no Magatama's light bullets at ({@code x}..), flying ({@code vx}..) a tick: a white-hot bead in gold, its glow,
     * a short streak of light behind it.
     */
    public static void bullet(FireMesh.Sink sink, double x, double y, double z, double vx, double vy, double vz, double size, double age,
                              int seed) {
        double vl = Math.max(1.0E-6, Math.sqrt(vx * vx + vy * vy + vz * vz));
        light(sink, x, y, z, size * 0.45, vx, vy, vz, 1.2, 1.0, 1.0, age * 2.0, seed);
        glow(sink, x, y, z, size * 1.6, 0xFFE070, 0.5);
        FireMesh.streak(sink, x, y, z, vx, vy, vz, Math.min(vl * 1.2, 4.0), size * 0.22, 0xFFD040, 0.9);
    }

    /**
     * Yasakani no Magatama gathering ({@code age} ticks, {@code at} the crossed hands): light piling up in the crossed hands, its
     * glow, beads already spilling from it.
     */
    public static void crossed(FireMesh.Sink sink, double x, double y, double z, double age, int seed) {
        double g = ease(age / 6.0);
        double pulse = 1.0 + 0.1 * Math.sin(age * 2.0);
        glow(sink, x, y, z, (0.8 + 1.6 * g) * pulse, 0xFFE070, 0.65);
        light(sink, x, y, z, (0.15 + 0.3 * g) * pulse, 0, 1, 0, 0.0, 1.0, 1.0, age * 2.0, seed);
        for (int k = 0; k < 8; k++) {
            direction(seed + (int) (age / 3.0), k, 0.0, D);
            FireMesh.ray(sink, x, y, z, D[0], D[1], D[2], (0.6 + 1.6 * g) * (0.7 + 0.3 * Math.sin(age * 2.7 + k)), 0.04, 0, 0.0, 0xFFF8D0, 0.85);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Yata no Kagami

    /**
     * Yata no Kagami being formed: light between the hands ({@code at}, facing {@code (ux..)}) as a small mirror of light: a white-hot
     * disc in a gold ring, its glow, beams out of it; {@code age} ticks into it.
     */
    public static void mirror(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double age, int seed) {
        double g = ease(age / 4.0);
        glow(sink, x, y, z, 0.6 + 1.0 * g, 0xFFE680, 0.7);
        light(sink, x, y, z, 0.12 + 0.16 * g, ux, uy, uz, 0.0, 1.0, 1.0, age, seed);
        FireMesh.ring(sink, x, y, z, ux, uy, uz, 0.25 + 0.3 * g, 0.12, 0.0, 0xFFFFFF, 0.95);
        FireMesh.ring(sink, x, y, z, ux, uy, uz, 0.45 + 0.4 * g, 0.08, 0.05, 0xFFD040, 0.8);
        FireMesh.basis(ux, uy, uz, B);
        for (int k = 0; k < 4; k++) {
            double a = k * Math.PI / 2.0 + age * 0.2;
            double dx = B[0] * Math.cos(a) + B[3] * Math.sin(a);
            double dy = B[1] * Math.cos(a) + B[4] * Math.sin(a);
            double dz = B[2] * Math.cos(a) + B[5] * Math.sin(a);
            FireMesh.ray(sink, x, y, z, dx, dy, dz, 0.8 + 1.2 * g, 0.035, 0, 0.0, 0xFFF8D0, 0.9);
        }
        FireMesh.ray(sink, x, y, z, ux, uy, uz, 2.0 + 4.0 * g, 0.05, 0, 0.0, 0xFFFFFF, 0.8 * g);
    }

    /** How long Yata no Kagami's way of light shows (ticks). */
    public static final double PATH_LIFE = 8.0;

    /**
     * The way Yata no Kagami's light went from ({@code ax}..) to ({@code bx}..), {@code age} ticks ago: a white-hot line in gold that
     * thins away, the light that he was still streaming along it, a flash where he came out of it.
     */
    public static void path(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double age, int seed) {
        if (age < 0.0 || age > PATH_LIFE) {
            return;
        }
        double t = age / PATH_LIFE;
        double w = (1.0 - t) * (1.0 - t);
        FireMesh.tube(sink, ax, ay, az, bx, by, bz, 0.1 * w, 0.14 * w, 0xFFFFFF, 0xFFF9D6, 0xFFEF9A, 0.3, 1.0, 0.0, age, seed);
        FireMesh.tube(sink, ax, ay, az, bx, by, bz, 0.45 * w, 0.6 * w, 0xFFE05E, 0xFFD040, 0xFFC020, 0.0, 0.4 * (1.0 - t), 0.1, age, seed + 1);
        glow(sink, ax, ay, az, 1.2 * (1.0 - t), 0xFFF0A0, 0.6 * (1.0 - t));
        impact(sink, bx, by + 1.0, bz, 2.2, age, seed + 3);
    }

    // ------------------------------------------------------------------------------------------------------ Ama no Murakumo

    /**
     * Ama no Murakumo in the hand's own frame (the fist at the origin, the sword out along -Y as an item is held). 2.7.9 (the owner,
     * with two anime frames, docs/handoff/murakumo-ref-*.png: "its meant to be a full light sword no handle"): no hilt at all, the
     * sword is light from end to end: a long, very thin rod, white-hot down its core in a pale-gold sheath and a soft gold glow,
     * pointed at its tip and short behind the fist; at the fist a ring of light with a cross through it (the first frame) and a
     * starburst of rays (the second), that breathe with {@code shimmer} (0 to 1). {@code length} from the fist to the point,
     * {@code width} the rod's radius. Independent of the eye, so it is drawn through the arm's own pose.
     */
    public static void blade(FireMesh.Sink sink, double length, double width, double shimmer) {
        double back = 0.8; // the grip: the rod runs well back past the fist (the owner: "the grip handle rod has to be longer")
        double tipAt = -length + width * 14.0;
        // the rod: a white core in a pale-gold sheath, eight-sided, pointed at both ends
        rod(sink, back, tipAt, -length, width, 0xFFFFFF, 0xFFF6C0);
        rod(sink, back + 0.02, tipAt - width * 2.0, -length - width * 3.0, width * 1.9, 0xFFF29A, 0xFFE05E);
        // its glow: two crossed sheets along it, bright at the rod, clear at their rims
        double g = width * (5.0 + 1.5 * shimmer);
        int on = FireMesh.argb(0.55, 0xFFE070);
        int off = FireMesh.argb(0.0, 0xFFE070);
        FireMesh.quad(sink, 0, back, 0, on, 0, -length, 0, on, 0, -length, g * 0.4, off, 0, back, g, off);
        FireMesh.quad(sink, 0, back, 0, on, 0, back, -g, off, 0, -length, -g * 0.4, off, 0, -length, 0, on);
        FireMesh.quad(sink, 0, back, 0, on, 0, -length, 0, on, g * 0.4, -length, 0, off, g, back, 0, off);
        FireMesh.quad(sink, 0, back, 0, on, -g, back, 0, off, -g * 0.4, -length, 0, off, 0, -length, 0, on);
        // at the fist: a ring of light round the rod in its own plane, a bar across it (the cross), the starburst
        double ring = 0.2 + 0.02 * shimmer;
        FireMesh.ring(sink, 0, 0, 0, 1, 0, 0, ring, width * 1.6, 0.0, 0xFFFFFF, 1.0);
        FireMesh.ring(sink, 0, 0, 0, 1, 0, 0, ring, width * 4.0, 0.0, 0xFFE070, 0.45);
        rodZ(sink, ring * 1.45, width * 0.9);
        double[][] rays = {{0, 0, 1, 0.55}, {0, 0, -1, 0.55}, {0, 1, 0, 0.35}, {0.6, 0.6, 0.5, 0.42}, {-0.6, 0.5, -0.6, 0.38},
            {0.5, -0.4, -0.75, 0.46}, {-0.55, -0.45, 0.7, 0.4}, {0.9, 0.0, -0.3, 0.3}, {-0.9, 0.1, 0.35, 0.33}};
        for (int i = 0; i < rays.length; i++) {
            double[] r = rays[i];
            double l = Math.sqrt(r[0] * r[0] + r[1] * r[1] + r[2] * r[2]);
            double reach = r[3] * (0.8 + 0.4 * Math.abs(Math.sin(shimmer * 3.0 + i * 1.7)));
            FireMesh.ray(sink, 0, 0, 0, r[0] / l, r[1] / l, r[2] / l, reach, width * 1.4, 0, 0.0, 0xFFFDE8, 0.95);
        }
        glowLocal(sink, 0.0, 0.0, 0.0, 0.32 + 0.06 * shimmer, 0xFFF0A0, 0.65);
    }

    /** A rod of light along Y from {@code top} to {@code from} at full {@code radius}, then to a point at {@code tip}. */
    static void rod(FireMesh.Sink sink, double top, double from, double tip, double radius, int core, int edge) {
        int c = FireMesh.argb(1.0, core);
        int e = FireMesh.argb(1.0, edge);
        for (int k = 0; k < 8; k++) {
            double a0 = k * Math.PI / 4.0;
            double a1 = (k + 1) * Math.PI / 4.0;
            double x0 = Math.cos(a0) * radius;
            double z0 = Math.sin(a0) * radius;
            double x1 = Math.cos(a1) * radius;
            double z1 = Math.sin(a1) * radius;
            int c0 = k % 2 == 0 ? c : e;
            int c1 = k % 2 == 0 ? e : c;
            // the short point behind the fist, the long body, the point at the tip
            FireMesh.quad(sink, 0, top, 0, c, 0, top, 0, c, x1, top - radius * 4.0, z1, c1, x0, top - radius * 4.0, z0, c0);
            FireMesh.quad(sink, x0, top - radius * 4.0, z0, c0, x1, top - radius * 4.0, z1, c1, x1, from, z1, c1, x0, from, z0, c0);
            FireMesh.quad(sink, x0, from, z0, c0, x1, from, z1, c1, 0, tip, 0, c, 0, tip, 0, c);
        }
    }

    /** The cross's bar: a thin rod of light along Z through the fist, {@code half} out each way. */
    static void rodZ(FireMesh.Sink sink, double half, double radius) {
        int c = FireMesh.argb(1.0, 0xFFFFFF);
        int e = FireMesh.argb(1.0, 0xFFE05E);
        for (int k = 0; k < 6; k++) {
            double a0 = k * Math.PI / 3.0;
            double a1 = (k + 1) * Math.PI / 3.0;
            double x0 = Math.cos(a0) * radius;
            double y0 = Math.sin(a0) * radius;
            double x1 = Math.cos(a1) * radius;
            double y1 = Math.sin(a1) * radius;
            FireMesh.quad(sink, 0, 0, half, c, x0, y0, half * 0.8, e, x1, y1, half * 0.8, e, 0, 0, half, c);
            FireMesh.quad(sink, x0, y0, half * 0.8, e, x0, y0, -half * 0.8, e, x1, y1, -half * 0.8, e, x1, y1, half * 0.8, e);
            FireMesh.quad(sink, 0, 0, -half, c, x1, y1, -half * 0.8, e, x0, y0, -half * 0.8, e, 0, 0, -half, c);
        }
    }

    /** A ball of light in a local frame (no eye to keep it from): two crossed discs of light and a third across them. */
    static void glowLocal(FireMesh.Sink sink, double x, double y, double z, double radius, int rgb, double alpha) {
        int on = FireMesh.argb(alpha, rgb);
        int off = FireMesh.argb(0.0, rgb);
        for (int axis = 0; axis < 3; axis++) {
            for (int k = 0; k < 12; k++) {
                double a0 = k * Math.PI / 6.0;
                double a1 = (k + 1) * Math.PI / 6.0;
                double c0 = Math.cos(a0) * radius;
                double s0 = Math.sin(a0) * radius;
                double c1 = Math.cos(a1) * radius;
                double s1 = Math.sin(a1) * radius;
                double[] p0 = axis == 0 ? new double[] {0, c0, s0} : axis == 1 ? new double[] {c0, 0, s0} : new double[] {c0, s0, 0};
                double[] p1 = axis == 0 ? new double[] {0, c1, s1} : axis == 1 ? new double[] {c1, 0, s1} : new double[] {c1, s1, 0};
                FireMesh.quad(sink, x, y, z, on, x + p0[0], y + p0[1], z + p0[2], off, x + p1[0], y + p1[1], z + p1[2], off, x, y, z, on);
            }
        }
    }

    /** A box of light (centre, half sizes): its sides {@code side}, its ends {@code end}. */
    static void box(FireMesh.Sink sink, double cx, double cy, double cz, double hx, double hy, double hz, int side, int end) {
        double x0 = cx - hx;
        double x1 = cx + hx;
        double y0 = cy - hy;
        double y1 = cy + hy;
        double z0 = cz - hz;
        double z1 = cz + hz;
        FireMesh.quad(sink, x0, y0, z0, side, x1, y0, z0, side, x1, y1, z0, side, x0, y1, z0, side);
        FireMesh.quad(sink, x0, y0, z1, side, x0, y1, z1, side, x1, y1, z1, side, x1, y0, z1, side);
        FireMesh.quad(sink, x0, y0, z0, side, x0, y1, z0, side, x0, y1, z1, side, x0, y0, z1, side);
        FireMesh.quad(sink, x1, y0, z0, side, x1, y0, z1, side, x1, y1, z1, side, x1, y1, z0, side);
        FireMesh.quad(sink, x0, y1, z0, end, x1, y1, z0, end, x1, y1, z1, end, x0, y1, z1, end);
        FireMesh.quad(sink, x0, y0, z0, end, x0, y0, z1, end, x1, y0, z1, end, x1, y0, z0, end);
    }

    /** How long a cut of light shows (ticks). */
    public static final double SLASH_LIFE = 6.0;

    /**
     * A cut of light round ({@code ux}..) (the swing's axis) through {@code sweep} radians from {@code start}, {@code radius} out from
     * ({@code x}..), {@code age} ticks old: a white crescent over a wider gold one, gone in {@link #SLASH_LIFE} ticks.
     */
    public static void slash(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double radius, double start,
                             double sweep, double age) {
        if (age < 0.0 || age > SLASH_LIFE) {
            return;
        }
        double t = age / SLASH_LIFE;
        double open = ease(age / 1.5);
        FireMesh.arc(sink, x, y, z, ux, uy, uz, radius * (1.0 + 0.15 * t), radius * 0.5 * (1.0 - t * 0.6), start, sweep * open, 0.0, 0xFFD040,
            0.55 * (1.0 - t));
        FireMesh.arc(sink, x, y, z, ux, uy, uz, radius * (1.0 + 0.1 * t), radius * 0.22 * (1.0 - t), start, sweep * open, 0.0, 0xFFFFFF,
            0.95 * (1.0 - t));
    }

    // ------------------------------------------------------------------------------------------------------ trails

    /**
     * A trail of light through {@code points} (x, y, z each, newest first, eye-centred), {@code count} of them: a white line in a gold
     * glow, thinning to nothing at its oldest end ({@code width} at its newest).
     */
    public static void trail(FireMesh.Sink sink, double[] points, int count, double width, double alpha) {
        for (int i = 0; i + 1 < count; i++) {
            double t0 = i / (double) (count - 1);
            double t1 = (i + 1) / (double) (count - 1);
            double ax = points[i * 3];
            double ay = points[i * 3 + 1];
            double az = points[i * 3 + 2];
            double bx = points[i * 3 + 3];
            double by = points[i * 3 + 4];
            double bz = points[i * 3 + 5];
            if ((ax - bx) * (ax - bx) + (ay - by) * (ay - by) + (az - bz) * (az - bz) < 1.0E-6) {
                continue;
            }
            FireMesh.tube(sink, ax, ay, az, bx, by, bz, width * 0.3 * (1.0 - t0), width * 0.3 * (1.0 - t1), 0xFFFFFF, 0xFFF9D6, 0xFFEF9A, 0.3,
                alpha * (1.0 - t0 * 0.5), 0.0, 0.0, 7);
            FireMesh.tube(sink, ax, ay, az, bx, by, bz, width * (1.0 - t0), width * (1.0 - t1), 0xFFE05E, 0xFFC830, 0xFFB020, 0.0,
                alpha * 0.4 * (1.0 - t0), 0.0, 0.0, 8);
        }
    }

    // ------------------------------------------------------------------------------------------------------ the clones (Egghead)

    /**
     * A clone of light (Yasakani no Magatama, as at Egghead: every bead a Kizaru) standing at ({@code x}, {@code y}, {@code z}) (its
     * feet), facing {@code yaw} radians (0 = +z), {@code run} 0..1 into its stride, its sword arm {@code swing} (-1 raised behind, 1 cut
     * through), {@code alpha} how much of it is there: a man of light, white-hot at the heart, gold at every edge, in a gold glow,
     * a sword of light in the right hand.
     */
    public static void figure(FireMesh.Sink sink, double x, double y, double z, double yaw, double run, double swing, double alpha, int seed) {
        if (alpha < 0.01) {
            return;
        }
        double cy = Math.cos(yaw);
        double sy = Math.sin(yaw);
        double stride = Math.sin(run * Math.PI * 2.0) * 0.6;
        double lean = 0.25;
        // parts: centre (local x, y, z), half sizes, pitch (about local x) from the joint at its top
        limb(sink, x, y, z, cy, sy, 0.0, 1.5, 0.0, 0.26, 0.26, 0.26, lean, 0.0, alpha); // head (pivot at the neck)
        limb(sink, x, y, z, cy, sy, 0.0, 1.5, 0.0, 0.26, 0.38, 0.14, lean, 0.38, alpha); // body hangs from the neck
        limb(sink, x, y, z, cy, sy, 0.13, 0.75, 0.0, 0.12, 0.38, 0.12, stride, 0.38, alpha); // right leg
        limb(sink, x, y, z, cy, sy, -0.13, 0.75, 0.0, 0.12, 0.38, 0.12, -stride, 0.38, alpha); // left leg
        limb(sink, x, y, z, cy, sy, -0.38, 1.38, 0.0, 0.11, 0.36, 0.11, -stride * 0.8, 0.36, alpha); // left arm
        double arm = -1.6 - 1.2 * swing; // raised up and back (-1) to cut down and through (1)
        limb(sink, x, y, z, cy, sy, 0.38, 1.38, 0.0, 0.11, 0.36, 0.11, arm, 0.36, alpha); // right arm
        // the sword along the right arm, out of the hand
        double hx = 0.38;
        double hyL = 1.38 - Math.cos(arm) * 0.72;
        double hzL = Math.sin(arm) * 0.72;
        double tx = 0.38;
        double tyL = 1.38 - Math.cos(arm) * 2.9;
        double tzL = Math.sin(arm) * 2.9;
        double h0 = x + hx * cy + hzL * sy;
        double h2 = z - hx * sy + hzL * cy;
        double t0 = x + tx * cy + tzL * sy;
        double t2 = z - tx * sy + tzL * cy;
        FireMesh.tube(sink, h0, y + hyL, h2, t0, y + tyL, t2, 0.06, 0.02, 0xFFFFFF, 0xFFF9D6, 0xFFE05E, 0.35, alpha, 0.0, 0.0, seed);
        glow(sink, x, y + 1.0, z, 1.5, 0xFFE070, 0.35 * alpha);
    }

    /**
     * One box of a figure: its joint at local ({@code jx}, {@code jy}, {@code jz}) (x right, y up, z ahead before the yaw), the box
     * hanging {@code hang} below it turned {@code pitch} about local x, half sizes ({@code hx}, {@code hy}, {@code hz}); faces white
     * where they face the eye, gold where they turn from it.
     */
    static void limb(FireMesh.Sink sink, double x, double y, double z, double cy, double sy, double jx, double jy, double jz, double hx, double hy,
                     double hz, double pitch, double hang, double alpha) {
        double cp = Math.cos(pitch);
        double sp = Math.sin(pitch);
        double[] c = new double[24];
        for (int k = 0; k < 8; k++) {
            double lx = (k & 1) == 0 ? -hx : hx;
            double ly = ((k & 2) == 0 ? -hy : hy) + (hang > 0.0 ? -hy : hy); // hanging below the joint, or (the head) standing on it
            double lz = (k & 4) == 0 ? -hz : hz;
            // pitch about x at the joint
            double py = ly * cp - lz * sp;
            double pz = ly * sp + lz * cp;
            double ax = jx + lx;
            double ay = jy + py;
            double az = jz + pz;
            c[k * 3] = x + ax * cy + az * sy;
            c[k * 3 + 1] = y + ay;
            c[k * 3 + 2] = z - ax * sy + az * cy;
        }
        int[][] sides = {{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};
        for (int[] s : sides) {
            double mx = 0;
            double my = 0;
            double mz = 0;
            for (int i : s) {
                mx += c[i * 3] / 4.0;
                my += c[i * 3 + 1] / 4.0;
                mz += c[i * 3 + 2] / 4.0;
            }
            double ex = c[s[1] * 3] - c[s[0] * 3];
            double ey = c[s[1] * 3 + 1] - c[s[0] * 3 + 1];
            double ez = c[s[1] * 3 + 2] - c[s[0] * 3 + 2];
            double fx = c[s[3] * 3] - c[s[0] * 3];
            double fy = c[s[3] * 3 + 1] - c[s[0] * 3 + 1];
            double fz = c[s[3] * 3 + 2] - c[s[0] * 3 + 2];
            double nx = ey * fz - ez * fy;
            double ny = ez * fx - ex * fz;
            double nz = ex * fy - ey * fx;
            double nl = Math.max(1.0E-9, Math.sqrt(nx * nx + ny * ny + nz * nz));
            double ml = Math.max(1.0E-9, Math.sqrt(mx * mx + my * my + mz * mz));
            double facing = Math.abs((nx * mx + ny * my + nz * mz) / (nl * ml));
            int col = FireMesh.argb(alpha, facing > 0.55 ? 0xFFFFFF : facing > 0.25 ? 0xFFF09A : 0xFFC832);
            FireMesh.quad(sink, c[s[0] * 3], c[s[0] * 3 + 1], c[s[0] * 3 + 2], col, c[s[1] * 3], c[s[1] * 3 + 1], c[s[1] * 3 + 2], col, c[s[2] * 3],
                c[s[2] * 3 + 1], c[s[2] * 3 + 2], col, c[s[3] * 3], c[s[3] * 3 + 1], c[s[3] * 3 + 2], col);
        }
    }
}
