package com.hexhaki.piece.dragon;

/**
 * 2.7.6: the Uo Uo no Mi's moves as 3D shapes ({@link FireMesh}'s pieces put together), each worked out from where and when it
 * started, so the client keeps no particle state for them and every viewer sees the same fire. Positions are eye-centred.
 */
public final class UoFire {
    private UoFire() {
    }

    /**
     * Where a breath's mouth was and where it aimed {@code ago} ticks before now: {@code out} gets the inside of the mouth (x, y, z),
     * the aim (a unit direction: where the move strikes), then the mouth's own way out (unit: DragonForm.mouth).
     */
    public interface Aim {
        void at(double ago, double[] out);
    }

    static final double[] AIM = new double[9];
    static final double[] AT = new double[3];

    /** How far (blocks) the fire runs out of the mouth along the mouth's own way before it has turned onto the aim. */
    public static final double TURN = 4.0;

    /**
     * The point {@code d} blocks along a breath leaving the mouth in {@link #AIM}: straight out of the gape, turned onto the aim within
     * {@link #TURN} blocks (so it leaves the mouth, and strikes where the player aims).
     */
    static void along(double d, double[] out) {
        double s = TURN * (1.0 - Math.exp(-d / TURN));
        out[0] = AIM[0] + AIM[6] * s + AIM[3] * (d - s);
        out[1] = AIM[1] + AIM[7] * s + AIM[4] * (d - s);
        out[2] = AIM[2] + AIM[8] * s + AIM[5] * (d - s);
    }

    // ------------------------------------------------------------------------------------------------------ the breath (M1)

    /** The breath's puffs: born this many a tick, each living this long (ticks) to reach the end of its range. */
    public static final double BREATH_RATE = 2.0;
    public static final double BREATH_LIFE = 13.0;

    /** How far a puff born {@code age} ticks ago has gone of {@code range}: fast out of the mouth, slowing to a stop at the end. */
    public static double reach(double age, double range) {
        double u = Math.max(0.0, Math.min(1.0, age / BREATH_LIFE));
        return range * (1.0 - Math.pow(1.0 - u, 1.6));
    }

    /**
     * The dragon's breath, on for {@code on} ticks (and off for {@code off} ticks, -1 while it is on): a white-hot core out of the
     * mouth, a stream of fire puffs that grow and burn from white through yellow and orange to red as they go out to {@code range},
     * and white shock crescents racing out round it.
     */
    public static void breath(FireMesh.Sink sink, Aim aim, double on, double off, double range, int seed) {
        double now = on;
        // the puffs: index k was born at tick k / RATE after the breath began
        int last = (int) Math.floor(now * BREATH_RATE);
        int first = Math.max(0, (int) Math.ceil((now - BREATH_LIFE) * BREATH_RATE));
        double stopped = off < 0.0 ? Double.MAX_VALUE : now - off; // puffs born after the breath stopped are not there
        for (int k = last; k >= first; k--) {
            double born = k / BREATH_RATE;
            if (born > stopped) {
                continue;
            }
            double age = now - born;
            if (age < 0.0) {
                continue;
            }
            aim.at(age, AIM);
            double u = age / BREATH_LIFE;
            double d = reach(age, range);
            int s = seed * 31 + k;
            double r0 = FireMesh.rand(s, 0);
            double r1 = FireMesh.rand(s, 1) * 2.0 - 1.0;
            double r2 = FireMesh.rand(s, 2) * 2.0 - 1.0;
            FireMesh.basis(AIM[3], AIM[4], AIM[5], FireMesh.B);
            double clear = Math.max(0.0, d - 2.5); // out of the jaws: from here it may bloom
            double spread = d * 0.03 + clear * 0.1;
            along(d, AT);
            double px = AT[0] + (FireMesh.B[0] * r1 + FireMesh.B[3] * r2) * spread;
            double py = AT[1] + (FireMesh.B[1] * r1 + FireMesh.B[4] * r2) * spread;
            double pz = AT[2] + (FireMesh.B[2] * r1 + FireMesh.B[5] * r2) * spread;
            double radius = (0.2 + d * 0.07 + clear * 0.15) * (0.75 + 0.5 * r0); // narrower than the gape in it, a wall of fire at the end
            double end = Math.max(0.0, (u - 0.78) / 0.22);
            radius *= 1.0 - end * end;
            double heat = 1.0 - 0.62 * Math.pow(u, 0.7) + 0.06 * r0; // white-hot to amber: bright, red only in its depths
            double turn = Math.exp(-d / TURN);
            double vx = AIM[3] + (AIM[6] - AIM[3]) * turn;
            double vy = AIM[4] + (AIM[7] - AIM[4]) * turn + 0.08 * u;
            double vz = AIM[5] + (AIM[8] - AIM[5]) * turn;
            double stretch = (1.0 - 0.5 * u) * Math.min(1.0, clear / 3.0); // no tail back into the head
            FireMesh.puff(sink, px, py, pz, radius, vx, vy, vz, stretch, 0.16, heat, 0.75, true, 1.0, now + k * 3.7, s);
        }
        // the core: white-hot from the mouth, as long as the breath has had time to throw it (gone a few ticks after it stops)
        double fade = off < 0.0 ? 1.0 : Math.max(0.0, 1.0 - off / 4.0);
        if (fade > 0.0) {
            aim.at(0.0, AIM);
            double length = Math.min(range * 0.42, now * 4.0) * fade;
            double grow = Math.min(1.0, now / 3.0);
            // along the bend: short pieces near the mouth, longer after
            double[] cut = {0.0, 1.2, 2.6, 4.5, 7.5, length};
            double ax = AIM[0];
            double ay = AIM[1];
            double az = AIM[2];
            for (int i = 1; i < cut.length && cut[i - 1] < length; i++) {
                double d = Math.min(cut[i], length);
                along(d, AT);
                double r0 = (0.22 + 1.4 * cut[i - 1] / (range * 0.42)) * grow;
                double r1 = (0.22 + 1.4 * d / (range * 0.42)) * grow;
                FireMesh.tube(sink, ax, ay, az, AT[0], AT[1], AT[2], r0, r1, FireMesh.mix(0xFFFDF2, 0xFFF29A, cut[i - 1] / length),
                    FireMesh.mix(0xFFFDF2, 0xFFF29A, d / length), 0xFFD52B, 0.28, 1.0, 0.18, now, seed + i);
                ax = AT[0];
                ay = AT[1];
                az = AT[2];
            }
            // the white-hot heart deep in the mouth, and its light filling the gape
            FireMesh.puff(sink, AIM[0], AIM[1], AIM[2], 0.42 * grow * fade, AIM[6], AIM[7], AIM[8], 0.6, 0.12, 1.0, 0.0, false, 1.0, now, seed + 5);
            glow(sink, AIM[0] + AIM[6] * 0.5, AIM[1] + AIM[7] * 0.5, AIM[2] + AIM[8] * 0.5, 1.3 * grow * fade, 0xFFE68A, 0.6);
        }
        // the glow round the stream (the owner: "bright and glowy"): light, not smoke, so thin and warm
        double front = reach(Math.min(now, BREATH_LIFE), range);
        double back = off < 0.0 ? 0.0 : reach(Math.min(off, BREATH_LIFE), range);
        for (int j = 0; j < 6; j++) {
            double d = back + (front - back) * (j + 0.5) / 6.0;
            if (front - back < 0.5) {
                break;
            }
            aim.at(0.0, AIM);
            double radius = (0.3 + d * 0.07 + Math.max(0.0, d - 2.5) * 0.17) * 1.7;
            along(d, AT);
            glow(sink, AT[0], AT[1], AT[2], radius, j < 2 ? 0xFFE27A : 0xFFB84A, 0.32);
        }
        // the shock crescents: one every 3 ticks, out along the breath, widening as they go
        int arcs = (int) Math.floor(now / 3.0);
        for (int k = arcs; k >= 0 && k > arcs - 5; k--) {
            double born = k * 3.0 + 1.0;
            double age = now - born;
            if (age < 0.0 || born > stopped) {
                continue;
            }
            double life = 9.0;
            if (age > life) {
                continue;
            }
            aim.at(age, AIM);
            double t = age / life;
            double d = reach(age * 1.4, range) * 0.75 + 2.0;
            int s = seed * 17 + k;
            along(d, AT);
            double cx = AT[0];
            double cy = AT[1];
            double cz = AT[2];
            double radius = 2.5 + 9.0 * Math.sqrt(t);
            double start = FireMesh.rand(s, 0) * Math.PI * 2.0;
            FireMesh.arc(sink, cx, cy, cz, AIM[3], AIM[4], AIM[5], radius, 0.55 + 0.5 * (1.0 - t), start, 3.6, -2.5 * t - 0.8, 0xFFFFFF,
                0.92 * (1.0 - t * t));
        }
    }

    static final double[] BB = new double[6];

    static double ease(double t) {
        double u = clamp(t);
        return 1.0 - (1.0 - u) * (1.0 - u) * (1.0 - u);
    }

    /** A unit direction for {@code i} under {@code seed}, leaning up by {@code up} (0 any way, 1 well up). */
    static void direction(int seed, int i, double up, double[] out) {
        double a = FireMesh.rand(seed, i * 3) * Math.PI * 2.0;
        double y = FireMesh.rand(seed, i * 3 + 1) * 2.0 - 1.0;
        y = y + (1.0 - y) * up * 0.5;
        double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
        out[0] = Math.cos(a) * r;
        out[1] = y;
        out[2] = Math.sin(a) * r;
    }

    static final double[] DIR = new double[3];

    // ------------------------------------------------------------------------------------------------------ the charged ball (X held)

    /**
     * The ball gathering in the open mouth ({@code charge} 0 to 1, {@code age} ticks since the charge began; the reference's first
     * frame): a yellow ball with a pale heart, a red corona round it, dashed beams of light out of it, fire licking up round the jaws,
     * sparks rushing into it, and its light.
     */
    public static void orb(FireMesh.Sink sink, Aim aim, double age, double charge, int seed) {
        aim.at(0.0, AIM);
        double c = clamp(charge);
        double r = 0.3 + 0.75 * c;
        double pulse = 1.0 + 0.07 * Math.sin(age * 1.9);
        double sit = 0.2 + r * 0.6;
        double x = AIM[0] + AIM[6] * sit;
        double y = AIM[1] + AIM[7] * sit;
        double z = AIM[2] + AIM[8] * sit;
        FireMesh.basis(AIM[6], AIM[7], AIM[8], BB);
        double bx0 = BB[0];
        double by0 = BB[1];
        double bz0 = BB[2];
        double bx1 = BB[3];
        double by1 = BB[4];
        double bz1 = BB[5];
        glow(sink, x, y, z, r * (2.4 + 0.8 * c) * pulse, 0xFFC850, 0.4 + 0.15 * c);
        // the red corona, a little to one side (the reference's red crescent)
        FireMesh.shell(sink, x + bx0 * r * 0.12, y + by0 * r * 0.12, z + bz0 * r * 0.12, r * 1.3 * pulse, 0xE8301A, 0.0, 0.95);
        FireMesh.puff(sink, x, y, z, r * pulse, 0.0, 1.0, 0.0, 0.1, 0.06, 0.84, 0.7, false, 1.0, age * 2.2, seed);
        // the beams: dashed, turning slowly, longer with the charge
        for (int k = 0; k < 6; k++) {
            double a = k * Math.PI / 3.0 + age * 0.035 + FireMesh.rand(seed, 40 + k) * 0.5;
            double out = 0.25 + 0.3 * FireMesh.rand(seed, 50 + k);
            double dx = AIM[6] * out + bx0 * Math.cos(a) + bx1 * Math.sin(a);
            double dy = AIM[7] * out + by0 * Math.cos(a) + by1 * Math.sin(a);
            double dz = AIM[8] * out + bz0 * Math.cos(a) + bz1 * Math.sin(a);
            double l = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double flicker = 0.75 + 0.25 * Math.sin(age * 2.3 + k * 1.7);
            FireMesh.ray(sink, x, y, z, dx / l, dy / l, dz / l, (2.0 + 9.0 * c) * (0.7 + 0.3 * FireMesh.rand(seed, 60 + k)), 0.07 + 0.09 * c, 5,
                age * 0.12 + k * 0.3, 0xFFF6C0, flicker);
        }
        // fire licking up round the jaws (the reference's yellow flames at the sides)
        for (int k = 0; k < 5; k++) {
            double life = 9.0;
            double t = ((age + k * life / 5.0) % life) / life;
            double a = k * Math.PI * 2.0 / 5.0 + FireMesh.rand(seed, 70 + k);
            double fx = x + (bx0 * Math.cos(a) + bx1 * Math.sin(a)) * r * 1.15;
            double fy = y + (by0 * Math.cos(a) + by1 * Math.sin(a)) * r * 1.15 + t * (0.8 + 1.4 * c);
            double fz = z + (bz0 * Math.cos(a) + bz1 * Math.sin(a)) * r * 1.15;
            double size = r * (0.26 + 0.12 * c) * Math.sin(Math.PI * t);
            FireMesh.puff(sink, fx, fy, fz, size, 0.0, -1.0, 0.0, 1.6, 0.1, 0.8 - 0.3 * t, 0.6, true, 1.0, age * 2.0, seed + 80 + k);
        }
        // sparks rushing in
        for (int k = 0; k < 10; k++) {
            double life = 7.0;
            double t = ((age + k * 0.7) % life) / life;
            direction(seed + 90, k + (int) ((age + k * 0.7) / life) * 10, 0.0, DIR);
            double from = (2.0 + 3.0 * c) * (1.0 - t);
            double sx = x + DIR[0] * from;
            double sy = y + DIR[1] * from;
            double sz = z + DIR[2] * from;
            FireMesh.streak(sink, sx, sy, sz, -DIR[0], -DIR[1], -DIR[2], 0.4 + 0.6 * c, 0.035, 0xFFB43A, Math.sin(Math.PI * t));
        }
    }

    // ------------------------------------------------------------------------------------------------------ a fireball in flight

    /**
     * A fireball at ({@code x}, {@code y}, {@code z}) flying ({@code vx}, {@code vy}, {@code vz}) blocks a tick, {@code size} round,
     * {@code travelled} blocks from where it left (its tail never reaches back past that): a white-hot head, a tail of fire burning down
     * behind it, sparks shed, and for a {@code big} one (the charged blast) a red corona and shock crescents spinning round it.
     */
    public static void fireball(FireMesh.Sink sink, double x, double y, double z, double vx, double vy, double vz, double size, double age,
                                double travelled, boolean big, int seed) {
        double vl = Math.max(1.0E-6, Math.sqrt(vx * vx + vy * vy + vz * vz));
        double ux = vx / vl;
        double uy = vy / vl;
        double uz = vz / vl;
        FireMesh.basis(ux, uy, uz, BB);
        glow(sink, x, y, z, size * 2.6, 0xFFC850, 0.45);
        if (big) {
            FireMesh.shell(sink, x, y, z, size * 1.28, 0xE8301A, 0.0, 0.8);
        }
        FireMesh.puff(sink, x, y, z, size, ux, uy, uz, 0.7, 0.14, 0.98, 0.55, false, 1.0, age * 2.0, seed);
        int tail = big ? 11 : 7;
        double spacing = size * 0.75;
        for (int j = 1; j <= tail; j++) {
            double back = j * spacing;
            if (back > travelled + size) {
                break;
            }
            int s = seed * 7 + j;
            double jit = size * 0.35 * (0.3 + 0.7 * j / (double) tail);
            double a = FireMesh.rand(s, 0) * Math.PI * 2.0 + age * 0.3;
            double px = x - ux * back + (BB[0] * Math.cos(a) + BB[3] * Math.sin(a)) * jit;
            double py = y - uy * back + (BB[1] * Math.cos(a) + BB[4] * Math.sin(a)) * jit + j * 0.04 * size;
            double pz = z - uz * back + (BB[2] * Math.cos(a) + BB[5] * Math.sin(a)) * jit;
            double t = j / (double) tail;
            double radius = size * (1.0 - 0.6 * t) * (0.8 + 0.4 * FireMesh.rand(s, 1));
            FireMesh.puff(sink, px, py, pz, radius, ux, uy, uz, 1.1, 0.16, 0.9 - 0.62 * t, 0.7, t > 0.4, 1.0, age * 1.6 + j, s);
        }
        for (int k = 0; k < (big ? 8 : 4); k++) {
            double life = 5.0;
            double t = ((age + k * 0.6) % life) / life;
            direction(seed + 30, k + (int) ((age + k * 0.6) / life) * 8, 0.0, DIR);
            double back = t * size * 4.0;
            double out = size * (0.6 + 1.2 * t);
            FireMesh.streak(sink, x - ux * back + DIR[0] * out, y - uy * back + DIR[1] * out + 0.5 * t, z - uz * back + DIR[2] * out,
                DIR[0] - ux, DIR[1] - uy + 0.3, DIR[2] - uz, size * 0.7, 0.04 + 0.02 * size, 0xFFB43A, 1.0 - t);
        }
        if (big) {
            for (int k = 0; k < 2; k++) {
                double life = 6.0;
                double t = ((age + k * 3.0) % life) / life;
                FireMesh.arc(sink, x - ux * t * size * 3.0, y - uy * t * size * 3.0, z - uz * t * size * 3.0, ux, uy, uz, size * (1.3 + 1.6 * t),
                    0.3 + 0.2 * size, age * 0.5 + k * Math.PI, 3.4, -size * t, 0xFFFFFF, 0.85 * (1.0 - t));
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ the line of fire

    /**
     * 2.7.7 (the owner: "the Blast Breath Move should be a longer line of fire, like wayyyy longer"): the fire a shot leaves all along
     * its way, from where it left ({@code sx}, {@code sy}, {@code sz}) along ({@code ux}, {@code uy}, {@code uz}) (unit) at
     * {@code speed} blocks a tick, {@code age} ticks after it left, as far as {@code end} blocks (where it burst; the head's place
     * while it flies). Each piece flares up as the head passes it and burns down over {@code burn} ticks, so the whole way is a
     * line of fire at once. A dragon's shot left its mouth: ({@code ox}, {@code oy}, {@code oz}), gone over the first 5 ticks, is
     * how far the mouth was off the server's line.
     */
    public static void line(FireMesh.Sink sink, double sx, double sy, double sz, double ux, double uy, double uz, double speed, double age,
                            double end, double size, double burn, double ox, double oy, double oz, int seed) {
        double head = Math.min(end, speed * age);
        double spacing = size * 1.25;
        int count = (int) Math.floor(head / spacing);
        FireMesh.basis(ux, uy, uz, BB);
        for (int j = 0; j <= count; j++) {
            double d = j * spacing;
            double a = age - d / speed;
            if (a < 0.0 || a > burn) {
                continue;
            }
            int s = seed * 29 + j;
            double u = a / burn;
            double fade = Math.max(0.0, 1.0 - d / speed / 5.0);
            double side = (FireMesh.rand(s, 0) * 2.0 - 1.0) * size * 0.3;
            double lift = (FireMesh.rand(s, 1) * 2.0 - 1.0) * size * 0.3 + u * u * size * 1.6;
            double px = sx + ux * d + ox * fade + BB[0] * side + BB[3] * lift * 0.3;
            double py = sy + uy * d + oy * fade + BB[1] * side + lift;
            double pz = sz + uz * d + oz * fade + BB[2] * side + BB[5] * lift * 0.3;
            double flare = Math.min(1.0, a / 1.5);
            double radius = size * (0.7 + 0.45 * FireMesh.rand(s, 2)) * (0.55 + 0.45 * flare) * (1.0 - u * u);
            double heat = 1.0 - 0.75 * u + 0.06 * FireMesh.rand(s, 3);
            FireMesh.puff(sink, px, py, pz, radius, ux, uy + 0.25 * u, uz, 1.1 - 0.5 * u, 0.16, heat, 0.7, u > 0.35, 1.0, age * 1.4 + j, s);
        }
    }

    // ------------------------------------------------------------------------------------------------------ an explosion

    /** How long an explosion lasts (ticks). */
    public static final double BURST_LIFE = 30.0;

    /**
     * An explosion of {@code radius} at ({@code x}, {@code y}, {@code z}), {@code age} ticks old: a white flash, a ball of fire puffs
     * thrown out and up that burn from white to red and go to smoke, a shock shell and a ring racing over the ground, shock crescents,
     * 3D sparks and rock shards thrown out on arcs, and its light.
     */
    public static void explosion(FireMesh.Sink sink, double x, double y, double z, double radius, double age, int seed) {
        if (age < 0.0 || age > BURST_LIFE) {
            return;
        }
        double rr = Math.max(0.8, radius);
        double k = Math.sqrt(rr);
        // the light and the flash
        glow(sink, x, y, z, rr * 2.6 * (0.7 + 0.3 * ease(age / 3.0)), 0xFFD470, 0.75 * Math.max(0.0, 1.0 - age / 12.0));
        if (age < 5.0) {
            double f = Math.sin(Math.PI * Math.min(1.0, age / 5.0 + 0.15));
            FireMesh.puff(sink, x, y, z, rr * 0.75 * f, 0.0, 1.0, 0.0, 0.2, 0.1, 1.0, 0.0, false, 1.0, age, seed + 1);
        }
        // the fire, then the smoke: each puff burns down at its own pace, so fire and smoke mix
        int n = Math.min(18, 9 + (int) rr); // fewer, bigger puffs as it grows: a phone draws it
        for (int i = 0; i < n; i++) {
            int s = seed * 13 + i;
            direction(s, 0, 0.5, DIR);
            double r0 = FireMesh.rand(s, 5);
            double burn = 12.0 + 10.0 * FireMesh.rand(s, 8); // ticks to burn out
            double t = clamp((age - burn) / (BURST_LIFE - burn));
            double out = rr * (0.35 + 0.7 * r0) * ease(age / 6.0) * (1.0 + 0.5 * t);
            double rise = age * 0.03 * rr * (0.5 + FireMesh.rand(s, 6)) + t * t * rr * 1.4;
            double px = x + DIR[0] * out;
            double py = y + DIR[1] * out + rise;
            double pz = z + DIR[2] * out;
            double size = rr * (0.48 + 0.28 * FireMesh.rand(s, 7)) * (0.5 + 0.5 * ease(age / 4.0));
            double heat = 1.1 - 0.85 * age / burn;
            if (age < burn) {
                FireMesh.puff(sink, px, py, pz, size, DIR[0], DIR[1] + 0.3, DIR[2], 0.6, 0.18, heat, 0.65, heat < 0.6, 1.0, age * 1.5, s);
            } else {
                FireMesh.palette(FireMesh.SMOKE);
                FireMesh.puff(sink, px, py, pz, size * (0.95 + 0.45 * t), 0.0, 1.0, 0.0, 0.4, 0.1, 0.62 - 0.3 * t, 0.45, false,
                    0.7 * (1.0 - t) * (1.0 - t), age, s);
                FireMesh.palette(FireMesh.BAND);
            }
        }
        // the shock: a shell and a ring over the ground, and crescents
        if (age < 9.0) {
            double t = age / 9.0;
            FireMesh.shell(sink, x, y, z, rr * (0.5 + 2.0 * Math.sqrt(t)), 0xFFFFFF, 0.0, 0.8 * (1.0 - t));
            for (int i = 0; i < 3; i++) {
                direction(seed + 40, i, 0.2, DIR);
                FireMesh.arc(sink, x, y, z, DIR[0], DIR[1], DIR[2], rr * (0.9 + 2.2 * Math.sqrt(t)), 0.25 + 0.25 * rr * (1.0 - t),
                    FireMesh.rand(seed, 50 + i) * 6.28, 3.8, 0.0, 0xFFFFFF, 0.9 * (1.0 - t));
            }
        }
        if (age < 14.0) {
            double t = age / 14.0;
            FireMesh.ring(sink, x, y - rr * 0.3, z, 0.0, 1.0, 0.0, rr * (0.7 + 3.2 * Math.sqrt(t)), rr * 0.7 * (1.0 - t) + 0.4, rr * 0.25,
                0xFFF2D8, 0.75 * (1.0 - t));
        }
        // sparks and rocks on arcs
        for (int i = 0; i < 28; i++) {
            double life = 10.0 + 6.0 * FireMesh.rand(seed + 60, i);
            if (age > life) {
                continue;
            }
            direction(seed + 61, i, 0.6, DIR);
            double speed = (0.6 + 1.0 * FireMesh.rand(seed + 62, i)) * k * 0.6;
            double px = x + DIR[0] * speed * age;
            double py = y + DIR[1] * speed * age - 0.025 * age * age;
            double pz = z + DIR[2] * speed * age;
            FireMesh.streak(sink, px, py, pz, DIR[0] * speed, DIR[1] * speed - 0.05 * age, DIR[2] * speed, 1.4 + rr * 0.3, 0.1 + 0.04 * k,
                0xFFA72E, 1.0 - age / life);
        }
        for (int i = 0; i < 8 + (int) (rr * 1.5); i++) {
            double life = 18.0 + 6.0 * FireMesh.rand(seed + 70, i);
            if (age > life) {
                continue;
            }
            direction(seed + 71, i, 0.75, DIR);
            double speed = (0.35 + 0.5 * FireMesh.rand(seed + 72, i)) * k * 0.5;
            double px = x + DIR[0] * speed * age;
            double py = y + (DIR[1] * 1.6 + 0.4) * speed * age - 0.03 * age * age;
            double pz = z + DIR[2] * speed * age;
            double fade = clamp((life - age) / 4.0);
            FireMesh.shard(sink, px, py, pz, (0.35 + 0.45 * FireMesh.rand(seed + 73, i)) * Math.min(2.2, k) * fade, age * 0.35, seed + i,
                0x5A463A, 1.0);
        }
    }

    // ------------------------------------------------------------------------------------------------------ air (Air Pressure)

    /** A blast of compressed air in flight: a clear white tear, rings of shock left behind it, lines of wind along it. */
    public static void airShot(FireMesh.Sink sink, double x, double y, double z, double vx, double vy, double vz, double size, double age,
                               double travelled, int seed) {
        double vl = Math.max(1.0E-6, Math.sqrt(vx * vx + vy * vy + vz * vz));
        double ux = vx / vl;
        double uy = vy / vl;
        double uz = vz / vl;
        FireMesh.palette(FireMesh.AIR);
        FireMesh.puff(sink, x, y, z, size * 0.55, ux, uy, uz, 2.2, 0.08, 0.92, 0.5, false, 0.5, age * 2.0, seed);
        FireMesh.palette(FireMesh.BAND);
        FireMesh.shell(sink, x, y, z, size * 0.95, 0xFFFFFF, 0.0, 0.55);
        for (int j = 1; j <= 4; j++) {
            double back = j * 1.7 + (age * vl) % 1.7;
            if (back > travelled) {
                break;
            }
            double t = back / 8.5;
            FireMesh.ring(sink, x - ux * back, y - uy * back, z - uz * back, ux, uy, uz, size * (0.6 + 1.6 * t), 0.3 + 0.2 * size, -0.4,
                0xFFFFFF, 0.75 * (1.0 - t));
        }
        FireMesh.basis(ux, uy, uz, BB);
        for (int k = 0; k < 6; k++) {
            double a = k * Math.PI / 3.0 + FireMesh.rand(seed, k) * 0.6;
            double out = size * (0.8 + 0.5 * FireMesh.rand(seed, 10 + k));
            double back = 1.5 + 2.5 * FireMesh.rand(seed, 20 + k);
            FireMesh.streak(sink, x - ux * back + (BB[0] * Math.cos(a) + BB[3] * Math.sin(a)) * out,
                y - uy * back + (BB[1] * Math.cos(a) + BB[4] * Math.sin(a)) * out, z - uz * back + (BB[2] * Math.cos(a) + BB[5] * Math.sin(a)) * out,
                ux, uy, uz, Math.min(travelled, 3.5), 0.03, 0xE8F2FF, 0.8);
        }
    }

    /** Where a blast of air struck: a shell and rings of shock thrown out, puffs of air. Lasts 10 ticks. */
    public static void airBurst(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double radius, double age,
                                int seed) {
        if (age < 0.0 || age > 10.0) {
            return;
        }
        double t = age / 10.0;
        double rr = Math.max(0.8, radius);
        FireMesh.shell(sink, x, y, z, rr * (0.4 + 1.5 * Math.sqrt(t)), 0xFFFFFF, 0.0, 0.85 * (1.0 - t));
        FireMesh.ring(sink, x, y, z, ux, uy, uz, rr * (0.5 + 2.2 * Math.sqrt(t)), 0.5 * (1.0 - t) + 0.2, 0.3, 0xFFFFFF, 0.8 * (1.0 - t));
        FireMesh.ring(sink, x, y, z, 0.0, 1.0, 0.0, rr * (0.3 + 1.6 * Math.sqrt(t)), 0.4, 0.15, 0xFFFFFF, 0.6 * (1.0 - t));
        FireMesh.palette(FireMesh.AIR);
        for (int i = 0; i < 7; i++) {
            direction(seed, i, 0.3, DIR);
            double out = rr * (0.3 + 1.2 * ease(t));
            FireMesh.puff(sink, x + DIR[0] * out, y + DIR[1] * out, z + DIR[2] * out, rr * 0.35 * (1.0 - t * 0.6), DIR[0], DIR[1], DIR[2], 0.5, 0.1,
                0.85, 0.4, false, 0.55 * (1.0 - t), age, seed + i);
        }
        FireMesh.palette(FireMesh.BAND);
    }

    // ------------------------------------------------------------------------------------------------------ the tornado (Wind Force)

    /** The tornado's height and its radius at the foot and at the top (blocks). */
    public static final double TORNADO_TALL = 10.0;
    public static final double TORNADO_LOW = 1.6;
    public static final double TORNADO_HIGH = 4.6;

    /**
     * Wind Force's green tornado with its foot at ({@code x}, {@code y}, {@code z}), {@code age} ticks old, {@code strength} 0 to 1 (it
     * grows in and dies out): bands of green wind wound up a funnel round a faint green core, white streaks whipping round, puffs of
     * wind, rocks carried round and up, dust thrown round its foot.
     */
    public static void tornado(FireMesh.Sink sink, double x, double y, double z, double age, double strength, int seed) {
        double st = clamp(strength);
        if (st <= 0.0) {
            return;
        }
        double tall = TORNADO_TALL * (0.4 + 0.6 * st);
        FireMesh.tube(sink, x, y, z, x, y + tall, z, TORNADO_LOW * 0.7 * st, TORNADO_HIGH * 0.75 * st, 0xB8EEC4, 0xDDF8E0, 0xF4FFF4, 0.3, 0.22 * st,
            0.15, age, seed);
        for (int k = 0; k < 5; k++) {
            int steps = 22;
            double prevIx = 0;
            double prevIy = 0;
            double prevIz = 0;
            double prevOx = 0;
            double prevOy = 0;
            double prevOz = 0;
            for (int i = 0; i <= steps; i++) {
                double h = tall * i / steps;
                double a = age * 0.6 + h * 0.7 + k * Math.PI * 2.0 / 5.0;
                double rad = (TORNADO_LOW + (TORNADO_HIGH - TORNADO_LOW) * h / tall) * st * (1.0 + 0.08 * Math.sin(age * 0.3 + k));
                double band = 0.7 + 0.5 * h / tall;
                double ix = x + Math.cos(a) * rad;
                double iy = y + h;
                double iz = z + Math.sin(a) * rad;
                double ox = x + Math.cos(a - 0.35) * rad * 1.04;
                double oy = y + h + band;
                double oz = z + Math.sin(a - 0.35) * rad * 1.04;
                if (i > 0) {
                    double fade = Math.sin(Math.PI * i / steps);
                    int edge = FireMesh.argb(0.0, 0xDDF8E0);
                    int mid = FireMesh.argb(0.6 * st * fade, k % 2 == 0 ? 0x95DCA2 : 0xDDF8E0);
                    FireMesh.quad(sink, prevIx, prevIy, prevIz, mid, prevOx, prevOy, prevOz, edge, ox, oy, oz, edge, ix, iy, iz, mid);
                }
                prevIx = ix;
                prevIy = iy;
                prevIz = iz;
                prevOx = ox;
                prevOy = oy;
                prevOz = oz;
            }
        }
        for (int k = 0; k < 8; k++) {
            double h = tall * FireMesh.rand(seed, k);
            double a = age * 0.75 + k * 0.8 + h * 0.7;
            double rad = (TORNADO_LOW + (TORNADO_HIGH - TORNADO_LOW) * h / tall) * st * 1.1;
            FireMesh.streak(sink, x + Math.cos(a) * rad, y + h, z + Math.sin(a) * rad, -Math.sin(a), 0.25, Math.cos(a), 2.6, 0.04, 0xFFFFFF, 0.85 * st);
        }
        FireMesh.palette(FireMesh.WIND);
        for (int k = 0; k < 6; k++) {
            double h = tall * (0.15 + 0.8 * FireMesh.rand(seed, 20 + k));
            double a = age * 0.55 + k * 1.05 + h * 0.7;
            double rad = (TORNADO_LOW + (TORNADO_HIGH - TORNADO_LOW) * h / tall) * st;
            FireMesh.puff(sink, x + Math.cos(a) * rad, y + h, z + Math.sin(a) * rad, (0.6 + 0.5 * h / tall) * st, -Math.sin(a), 0.2, Math.cos(a), 1.2,
                0.12, 0.72, 0.4, false, 0.7 * st, age * 1.5, seed + 30 + k);
        }
        FireMesh.palette(FireMesh.BAND);
        for (int k = 0; k < 6; k++) {
            double h = tall * ((FireMesh.rand(seed, 40 + k) + age * 0.02) % 1.0);
            double a = age * 0.5 + k * 1.3 + h * 0.7;
            double rad = (TORNADO_LOW + (TORNADO_HIGH - TORNADO_LOW) * h / tall) * st * 0.9;
            FireMesh.shard(sink, x + Math.cos(a) * rad, y + h, z + Math.sin(a) * rad, 0.3 + 0.25 * FireMesh.rand(seed, 50 + k), age * 0.5, seed + k,
                0x5A4A3A, 1.0);
        }
        FireMesh.ring(sink, x, y + 0.15, z, 0.0, 1.0, 0.0, TORNADO_LOW * 1.6 * st + 0.3 * Math.sin(age * 0.4), 1.4, 0.5, 0xD8F0D0, 0.5 * st);
    }

    // ------------------------------------------------------------------------------------------------------ the bite

    /** The bite's snap ({@code t} 0 to 1 of the bite): white crescents slamming shut round the jaws and a ring of shock out of them. */
    public static void bite(FireMesh.Sink sink, Aim aim, double t, int seed) {
        if (t < 0.42 || t > 0.8) {
            return;
        }
        aim.at(0.0, AIM);
        double u = (t - 0.42) / 0.38;
        double x = AIM[0] + AIM[6] * 1.5;
        double y = AIM[1] + AIM[7] * 1.5;
        double z = AIM[2] + AIM[8] * 1.5;
        FireMesh.arc(sink, x, y, z, AIM[6], AIM[7], AIM[8], 2.2 + 3.0 * u, 0.5 * (1.0 - u) + 0.15, 0.3, 2.6, -1.0 * u, 0xFFFFFF, 0.95 * (1.0 - u));
        FireMesh.arc(sink, x, y, z, AIM[6], AIM[7], AIM[8], 2.2 + 3.0 * u, 0.5 * (1.0 - u) + 0.15, 0.3 + Math.PI, 2.6, -1.0 * u, 0xFFFFFF,
            0.95 * (1.0 - u));
        FireMesh.ring(sink, x + AIM[6] * u * 2.0, y + AIM[7] * u * 2.0, z + AIM[8] * u * 2.0, AIM[6], AIM[7], AIM[8], 1.5 + 4.0 * u, 0.6, 0.6,
            0xFFFFFF, 0.8 * (1.0 - u));
        for (int i = 0; i < 10; i++) {
            direction(seed, i, 0.0, DIR);
            double out = 1.0 + 5.0 * u;
            FireMesh.streak(sink, x + DIR[0] * out, y + DIR[1] * out, z + DIR[2] * out, DIR[0], DIR[1], DIR[2], 1.2, 0.04, 0xFFF2C0, 1.0 - u);
        }
    }

    /** A ball of light: warm, densest in its middle, gone at its edge (alpha blended: the owner's device). */
    public static void glow(FireMesh.Sink sink, double x, double y, double z, double radius, int rgb, double alpha) {
        // never round the eye (a glow the eye is in would tint the whole view): at most just short of it
        double eye = Math.sqrt(x * x + y * y + z * z);
        FireMesh.shell(sink, x, y, z, Math.min(radius, eye * 0.9), rgb, alpha, 0.0);
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
