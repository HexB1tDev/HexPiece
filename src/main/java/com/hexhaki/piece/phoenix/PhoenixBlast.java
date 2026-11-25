package com.hexhaki.piece.phoenix;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.7.5 (the owner: "I dont like the fire particles from the moves"; "Explosions and slams from moves lack real explosion, it looks overall
 * bad and goofy"; "X transformed becomes a massive tornado like the dragon fruit but bigger and made of beautiful flames (blue and
 * yellow)"; "the B move should be 1 projectile that makes a massive explosion in the shape of a bird"): the Phoenix's moves' fire in the
 * house's own fire (FireMesh's banded fire puffs, the dragon fruit's explosions, fireballs and tornado: dragon/UoFire), in the phoenix's
 * blue and gold (PhoenixFire.CYAN and GOLD), never the curling tongues. Pure: the client, the previews and the tests share it; points
 * eye-relative.
 */
public final class PhoenixBlast {
    private PhoenixBlast() {
    }

    static final double[] DIR = new double[3];
    static final double[] BB = new double[6];

    static double clamp(double t) {
        return Math.max(0.0, Math.min(1.0, t));
    }

    static double ease(double t) {
        double u = clamp(t);
        return 1.0 - (1.0 - u) * (1.0 - u) * (1.0 - u);
    }

    /** A way from {@code seed}, {@code i}: spread over the sphere, lifted by {@code up} (0 any, 1 straight up). */
    static void direction(int seed, int i, double up, double[] out) {
        double a = FireMesh.rand(seed, i * 3) * Math.PI * 2.0;
        double y = FireMesh.rand(seed, i * 3 + 1) * 2.0 - 1.0;
        y = y + (1.0 - y) * up * FireMesh.rand(seed, i * 3 + 2);
        double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
        out[0] = Math.cos(a) * r;
        out[1] = y;
        out[2] = Math.sin(a) * r;
    }

    /** A puff of the phoenix's fire: cyan (or gold), as FireMesh.puff, its palette put back after. */
    static void puff(FireMesh.Sink sink, double x, double y, double z, double radius, double vx, double vy, double vz, double stretch,
                     double tongues, double heat, double rim, double alpha, double time, int seed, boolean gold) {
        FireMesh.palette(gold ? PhoenixFire.GOLD : PhoenixFire.CYAN);
        FireMesh.puff(sink, x, y, z, radius, vx, vy, vz, stretch, tongues, heat, rim, false, alpha, time, seed);
        FireMesh.palette(FireMesh.BAND);
    }

    /** Smoke (the house's grey puff), its palette put back after. */
    static void smoke(FireMesh.Sink sink, double x, double y, double z, double radius, double alpha, double time, int seed) {
        FireMesh.palette(FireMesh.SMOKE);
        FireMesh.puff(sink, x, y, z, radius, 0.0, 1.0, 0.0, 0.4, 0.1, 0.62, 0.45, false, alpha, time, seed);
        FireMesh.palette(FireMesh.BAND);
    }

    // ------------------------------------------------------------------------------------------------------ the explosion

    /** How long an explosion burns (ticks). */
    public static final double BURST_LIFE = 32.0;

    /**
     * An explosion of {@code radius} at (x, y, z), {@code age} ticks old (as the dragon fruit's: dragon/UoFire.explosion): a white-hot
     * flash, a ball of fire puffs thrown out and up that burn from white through cyan (a third of them gold) and go to smoke, a shock shell
     * and a ring racing over the ground, white shock crescents, sparks of cyan and gold and rock shards thrown out on arcs, its light.
     */
    public static void explosion(FireMesh.Sink sink, double x, double y, double z, double radius, double age, int seed) {
        explosion(sink, x, y, z, radius, age, seed, 1.0);
    }

    /** As above, {@code share} of its fire puffs, sparks and rocks (the bird of fire's blast: the bird is the most of its fire). */
    public static void explosion(FireMesh.Sink sink, double x, double y, double z, double radius, double age, int seed, double share) {
        if (age < 0.0 || age > BURST_LIFE) {
            return;
        }
        double rr = Math.max(0.8, radius);
        double k = Math.sqrt(rr);
        double quality = FireMesh.quality;
        FireMesh.quality = quality * BLAST_QUALITY;
        // 3.7.5: its glow and its shock one shell (two before): the glow while the shock races out, then the glow fading
        if (age < 9.0) {
            double t = age / 9.0;
            FireMesh.shell(sink, x, y, z, rr * (0.5 + 2.0 * Math.sqrt(t)), 0xFFFFFF, 0.25 * (1.0 - t), 0.8 * (1.0 - t));
        } else if (age < 14.0) {
            FireMesh.shell(sink, x, y, z, rr * 2.4, 0x8FF0FA, 0.5 * (14.0 - age) / 14.0, 0.0);
        }
        if (age < 5.0) {
            double f = Math.sin(Math.PI * Math.min(1.0, age / 5.0 + 0.15));
            puff(sink, x, y, z, rr * 0.8 * f, 0.0, 1.0, 0.0, 0.2, 0.1, 1.0, 0.0, 1.0, age, seed + 1, false);
        }
        int n = (int) Math.round(share * Math.min(14, 6 + (int) (rr * 1.5)));
        for (int i = 0; i < n; i++) {
            int s = seed * 13 + i;
            direction(s, 0, 0.5, DIR);
            double r0 = FireMesh.rand(s, 5);
            double burn = 12.0 + 10.0 * FireMesh.rand(s, 8);
            double t = clamp((age - burn) / (BURST_LIFE - burn));
            double out = rr * (0.35 + 0.75 * r0) * ease(age / 6.0) * (1.0 + 0.5 * t);
            double rise = age * 0.03 * rr * (0.5 + FireMesh.rand(s, 6)) + t * t * rr * 1.4;
            double px = x + DIR[0] * out;
            double py = y + DIR[1] * out + rise;
            double pz = z + DIR[2] * out;
            double size = rr * (0.5 + 0.3 * FireMesh.rand(s, 7)) * (0.5 + 0.5 * ease(age / 4.0));
            double heat = 1.1 - 0.85 * age / burn;
            if (age < burn) {
                puff(sink, px, py, pz, size, DIR[0], DIR[1] + 0.3, DIR[2], 0.6, 0.2, heat, 0.65, 1.0, age * 1.5, s, i % 3 == 0);
            } else {
                smoke(sink, px, py, pz, size * (0.95 + 0.45 * t), 0.6 * (1.0 - t) * (1.0 - t), age, s);
            }
        }
        if (age < 9.0) {
            double t = age / 9.0;
            for (int i = 0; i < 3; i++) {
                direction(seed + 40, i, 0.2, DIR);
                FireMesh.arc(sink, x, y, z, DIR[0], DIR[1], DIR[2], rr * (0.9 + 2.2 * Math.sqrt(t)), 0.25 + 0.25 * rr * (1.0 - t),
                    FireMesh.rand(seed, 50 + i) * 6.28, 3.8, 0.0, 0xFFFFFF, 0.9 * (1.0 - t));
            }
        }
        if (age < 14.0) {
            double t = age / 14.0;
            FireMesh.ring(sink, x, y - rr * 0.3, z, 0.0, 1.0, 0.0, rr * (0.7 + 3.2 * Math.sqrt(t)), rr * 0.7 * (1.0 - t) + 0.4, rr * 0.25,
                0xD2FBFF, 0.75 * (1.0 - t));
        }
        for (int i = 0; i < (int) (24 * share); i++) {
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
                i % 3 == 0 ? 0xFFBE2E : 0x46DDF5, 1.0 - age / life);
        }
        for (int i = 0; i < (int) (share * (8 + (int) (rr * 1.5))); i++) {
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
        FireMesh.quality = quality;
    }

    /**
     * 3.7.5: the detail the blasts and the tornado are drawn at (FireMesh.quality's share): their puffs a step coarser than a lone puff's
     * (a dozen big ones burn at once, beside the phoenix's own model, in a phone's budget); their cel bands hide the facets.
     */
    static final double BLAST_QUALITY = 0.7;

    // ------------------------------------------------------------------------------------------------------ fire that burns on

    /**
     * Blue fire burning on the ground at (x, y, z) (a crater's rim, a dash's path), {@code amount} 0 to 1, {@code age} its clock: a few
     * tall flame puffs licking up, cyan with gold among them.
     */
    public static void flames(FireMesh.Sink sink, double x, double y, double z, double size, double amount, double age, int seed) {
        if (amount < 0.02) {
            return;
        }
        for (int k = 0; k < 2; k++) {
            double life = 9.0 + 3.0 * FireMesh.rand(seed, k);
            double t = ((age + life * FireMesh.rand(seed, k + 5)) % life) / life;
            double h = t * size * 1.6;
            double r = size * 0.55 * (1.0 - 0.6 * t) * amount;
            puff(sink, x + (FireMesh.rand(seed, k + 9) - 0.5) * size * 0.4, y + h + r * 0.6, z + (FireMesh.rand(seed, k + 13) - 0.5) * size * 0.4, r,
                0.0, 1.0, 0.0, 1.0, 0.25, 1.0 - 0.7 * t, 0.7, 1.0, age * 1.4 + k, seed + k * 7, k == 1 && (seed & 1) == 0);
        }
    }

    // ------------------------------------------------------------------------------------------------------ the fireball

    /**
     * A ball of the phoenix's fire at (x, y, z) flying (vx, vy, vz), {@code size} round, {@code travelled} blocks from where it left (as
     * the dragon fruit's fireball): a white-hot heart in a cyan blaze, a tail of fire burning down behind it from cyan to gold, sparks shed,
     * white crescents spinning round it.
     */
    public static void fireball(FireMesh.Sink sink, double x, double y, double z, double vx, double vy, double vz, double size, double age,
                                double travelled, int seed) {
        double vl = Math.max(1.0E-6, Math.sqrt(vx * vx + vy * vy + vz * vz));
        double ux = vx / vl;
        double uy = vy / vl;
        double uz = vz / vl;
        FireMesh.basis(ux, uy, uz, BB);
        double quality = FireMesh.quality;
        FireMesh.quality = quality * BLAST_QUALITY;
        FireMesh.shell(sink, x, y, z, size * 2.4, 0x8FF0FA, 0.4, 0.0);
        FireMesh.shell(sink, x, y, z, size * 1.25, 0xFFD84E, 0.0, 0.6);
        puff(sink, x, y, z, size, ux, uy, uz, 0.7, 0.16, 1.0, 0.55, 1.0, age * 2.0, seed, false);
        int tail = 6;
        double spacing = size * 0.9;
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
            puff(sink, px, py, pz, radius, ux, uy, uz, 1.1, 0.18, 0.95 - 0.6 * t, 0.7, 1.0, age * 1.6 + j, s, j % 3 == 2);
        }
        for (int k = 0; k < 6; k++) {
            double life = 5.0;
            double t = ((age + k * 0.6) % life) / life;
            direction(seed + 30, k + (int) ((age + k * 0.6) / life) * 8, 0.0, DIR);
            double back = t * size * 4.0;
            double out = size * (0.6 + 1.2 * t);
            FireMesh.streak(sink, x - ux * back + DIR[0] * out, y - uy * back + DIR[1] * out + 0.5 * t, z - uz * back + DIR[2] * out,
                DIR[0] - ux, DIR[1] - uy + 0.3, DIR[2] - uz, size * 0.7, 0.04 + 0.02 * size, k % 2 == 0 ? 0xFFD84E : 0x8FF0FA, 1.0 - t);
        }
        for (int k = 0; k < 2; k++) {
            double life = 6.0;
            double t = ((age + k * 3.0) % life) / life;
            FireMesh.arc(sink, x - ux * t * size * 3.0, y - uy * t * size * 3.0, z - uz * t * size * 3.0, ux, uy, uz, size * (1.3 + 1.6 * t),
                0.3 + 0.2 * size, age * 0.5 + k * Math.PI, 3.4, -size * t, 0xFFFFFF, 0.85 * (1.0 - t));
        }
        FireMesh.quality = quality;
    }

    // ------------------------------------------------------------------------------------------------------ the fire tornado

    /** The fire tornado's height, its radius at its foot and at its top (blocks; 1.4 times with the Conqueror's Haki and Ryou on). */
    public static final double TORNADO_TALL = 18.0;
    public static final double TORNADO_LOW = 2.0;
    public static final double TORNADO_HIGH = 6.5;

    /**
     * The Phoenix's fire tornado with its foot at (x, y, z), {@code age} ticks old, {@code strength} 0 to 1 (it grows in and dies out),
     * {@code big} its size (1, or 1.4 with the Conqueror's Haki and Ryou): as the dragon fruit's tornado (dragon/UoFire.tornado), bands of
     * blue and gold flame wound up the funnel round a cyan core, flame puffs racing round and up it, gold embers carried round, white
     * streaks whipping round, a ring of fire round its foot.
     */
    public static void tornado(FireMesh.Sink sink, double x, double y, double z, double age, double strength, double big, int seed) {
        double st = clamp(strength);
        if (st <= 0.0) {
            return;
        }
        double quality = FireMesh.quality;
        FireMesh.quality = quality * BLAST_QUALITY;
        double tall = TORNADO_TALL * big * (0.4 + 0.6 * st);
        double low = TORNADO_LOW * big;
        double high = TORNADO_HIGH * big;
        FireMesh.tube(sink, x, y, z, x, y + tall, z, low * 0.7 * st, high * 0.7 * st, 0x2DB8EC, 0x46DDF5, 0x8FF0FA, 0.3, 0.3 * st, 0.15, age, seed);
        // the bands of flame wound up the funnel: cyan and gold, bright at their middle, clear at their edges
        for (int k = 0; k < 6; k++) {
            int steps = 22;
            double prevIx = 0;
            double prevIy = 0;
            double prevIz = 0;
            double prevOx = 0;
            double prevOy = 0;
            double prevOz = 0;
            for (int i = 0; i <= steps; i++) {
                double h = tall * i / steps;
                double a = age * 0.55 + h * 0.5 + k * Math.PI * 2.0 / 6.0;
                double rad = (low + (high - low) * h / tall) * st * (1.0 + 0.08 * Math.sin(age * 0.3 + k));
                double band = (1.4 + 0.9 * h / tall) * big;
                double ix = x + Math.cos(a) * rad;
                double iy = y + h;
                double iz = z + Math.sin(a) * rad;
                double ox = x + Math.cos(a - 0.35) * rad * 1.04;
                double oy = y + h + band;
                double oz = z + Math.sin(a - 0.35) * rad * 1.04;
                if (i > 0) {
                    double fade = Math.sin(Math.PI * i / steps);
                    int colour = k % 3 == 2 ? 0xFFBE2E : k % 3 == 1 ? 0x46DDF5 : 0x8FF0FA;
                    int edge = FireMesh.argb(0.0, colour);
                    int mid = FireMesh.argb(0.88 * st * fade, colour);
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
        // the flames racing round and up it
        for (int k = 0; k < 8; k++) {
            double h = tall * ((FireMesh.rand(seed, 20 + k) + age * 0.015) % 1.0);
            double a = age * 0.5 + k * 1.57 + h * 0.5;
            double rad = (low + (high - low) * h / tall) * st;
            puff(sink, x + Math.cos(a) * rad, y + h, z + Math.sin(a) * rad, (0.55 + 0.6 * h / tall) * st * big, -Math.sin(a), 0.3, Math.cos(a), 1.4,
                0.3, 0.72, 0.75, 0.9 * st, age * 1.5, seed + 30 + k, k % 2 == 0);
        }
        for (int k = 0; k < 8; k++) {
            double h = tall * FireMesh.rand(seed, k);
            double a = age * 0.75 + k * 0.8 + h * 0.5;
            double rad = (low + (high - low) * h / tall) * st * 1.1;
            FireMesh.streak(sink, x + Math.cos(a) * rad, y + h, z + Math.sin(a) * rad, -Math.sin(a), 0.25, Math.cos(a), 3.0 * big, 0.05, 0xFFFFFF,
                0.8 * st);
        }
        for (int k = 0; k < 8; k++) {
            double h = tall * ((FireMesh.rand(seed, 40 + k) + age * 0.02) % 1.0);
            double a = age * 0.5 + k * 1.3 + h * 0.5;
            double rad = (low + (high - low) * h / tall) * st * 0.9;
            FireMesh.shard(sink, x + Math.cos(a) * rad, y + h, z + Math.sin(a) * rad, 0.2 + 0.15 * FireMesh.rand(seed, 50 + k), age * 0.5, seed + k,
                0xFFD84E, 1.0);
        }
        // flames licking up off its lip and out of its wall, turning with it
        for (int k = 0; k < 18; k++) {
            boolean lip = k < 10;
            double h = lip ? tall * (0.96 + 0.04 * FireMesh.rand(seed, 100 + k)) : tall * (0.25 + 0.6 * ((FireMesh.rand(seed, 100 + k) + age * 0.02) % 1.0));
            double a = age * 0.55 + h * 0.5 + (lip ? k * 0.628 : k * 2.4);
            double rad = (low + (high - low) * h / tall) * st;
            double ox = Math.cos(a);
            double oz = Math.sin(a);
            double len = (lip ? 2.6 + 1.6 * FireMesh.rand(seed, 120 + k) : 1.6 + 1.0 * FireMesh.rand(seed, 120 + k)) * big * st;
            double up = lip ? 0.9 : 0.35;
            double[] a0 = {x + ox * rad, y + h, z + oz * rad};
            double[] a1 = {a0[0] + (ox * 0.5 - oz * 0.6) * len, a0[1] + up * len, a0[2] + (oz * 0.5 + ox * 0.6) * len};
            ribbon(sink, a0, a1, -oz, 0.0, ox, 0.0, 1.0, 0.0, (lip ? 1.9 : 1.3) * big, 0.0, k % 3 == 0 ? 0xFFF0B0 : 0xD2FBFF,
                k % 3 == 0 ? 0xFFBE2E : 0x2DB8EC, 0.8 * st, age, seed + 130 + k, 3);
        }
        FireMesh.ring(sink, x, y + 0.15, z, 0.0, 1.0, 0.0, low * 1.6 * st + 0.3 * Math.sin(age * 0.4), 1.6 * big, 0.6, 0x46DDF5, 0.6 * st);
        for (int k = 0; k < 3; k++) {
            double a = 2 * Math.PI * k / 3.0 + age * 0.3;
            flames(sink, x + Math.cos(a) * low * 1.5 * st, y, z + Math.sin(a) * low * 1.5 * st, 1.3 * big, st, age, seed + 60 + k);
        }
        FireMesh.quality = quality;
    }

    // ------------------------------------------------------------------------------------------------------ the sphere of flames

    /**
     * 3.7.6, Bluebird's sphere (the owner: "The Blue bird move should shoot and charge a sphere not a bird (made of flames)", "make blue
     * bird the sphere I asked for"): a ball of fire {@code radius} round at (x, y, z), {@code age} its clock, going (vx, vy, vz) (zero:
     * held): a white-hot heart in a round cyan blaze, its whole skin flames licking out and up all round it and turning with it, a gold
     * rim and a soft cyan glow, sparks shed behind it as it flies. No tail: it stays a sphere.
     */
    public static void sphere(FireMesh.Sink sink, double x, double y, double z, double radius, double vx, double vy, double vz, double age,
                              int seed) {
        if (radius < 0.05) {
            return;
        }
        FireMesh.shell(sink, x, y, z, radius * 1.6, 0x46DDF5, 0.35, 0.25);
        // a cyan blaze, ragged with flame, round a white-hot heart (at the full detail: the sphere is the whole move)
        puff(sink, x, y, z, radius, 0.0, 1.0, 0.0, 0.0, 0.42, 0.85, 0.75, 0.9, age * 2.4, seed, false);
        puff(sink, x, y, z, radius * 0.55, 0.0, 1.0, 0.0, 0.0, 0.2, 1.2, 0.3, 1.0, age * 2.0, seed + 1, false);
        // its skin of flames: points spread evenly over it (a golden-angle spiral), turning, each a flame licking out and a little up
        int n = 22;
        double spin = age * 0.12;
        for (int k = 0; k < n; k++) {
            double v = 1.0 - 2.0 * (k + 0.5) / n;
            double ring = Math.sqrt(Math.max(0.0, 1.0 - v * v));
            double a = k * 2.39996 + spin;
            double dx = Math.cos(a) * ring;
            double dy = v;
            double dz = Math.sin(a) * ring;
            double flick = 0.5 + 0.5 * Math.sin(age * 0.9 + k * 1.7);
            double len = radius * (0.8 + 0.6 * flick) * (0.8 + 0.4 * FireMesh.rand(seed, k));
            double[] a0 = {x + dx * radius * 0.6, y + dy * radius * 0.6, z + dz * radius * 0.6};
            double[] a1 = {a0[0] + (dx * 0.85) * len, a0[1] + (dy * 0.85 + 0.45) * len, a0[2] + (dz * 0.85) * len};
            boolean gold = k % 4 == 1;
            ribbon(sink, a0, a1, -dz, 0.0, dx, 0.0, 1.0, 0.0, radius * 1.1, 0.0, gold ? 0xFFE27A : 0x8FF0FA, gold ? 0xFFBE2E : 0x2DB8EC,
                0.95, age, seed + 10 + k, 3);
        }
        // sparks shed off it: behind it as it flies, all round it held
        double vl = Math.sqrt(vx * vx + vy * vy + vz * vz);
        for (int k = 0; k < 8; k++) {
            double life = 6.0;
            double t = ((age + k * 0.75) % life) / life;
            direction(seed + 40, k + (int) ((age + k * 0.75) / life) * 8, 0.2, DIR);
            double out = radius * (1.0 + 1.4 * t);
            double bx = vl > 1.0E-3 ? -vx / vl * t * radius * 2.5 : 0.0;
            double by = vl > 1.0E-3 ? -vy / vl * t * radius * 2.5 : 0.0;
            double bz = vl > 1.0E-3 ? -vz / vl * t * radius * 2.5 : 0.0;
            FireMesh.streak(sink, x + DIR[0] * out + bx, y + DIR[1] * out + by + 0.4 * t, z + DIR[2] * out + bz, DIR[0], DIR[1] + 0.3, DIR[2],
                radius * 0.5, 0.04 + 0.02 * radius, k % 3 == 0 ? 0xFFD84E : 0x8FF0FA, 1.0 - t);
        }
    }

    /**
     * A ribbon of flame from {@code a} to {@code b} in {@code n} pieces, {@code wide} across at its root narrowing to {@code end}, laid
     * along (wx, wy, wz) (or, where that runs along the ribbon, along (vx, vy, vz)): bright {@code root} colour to {@code tip}, clear at
     * its edges and its end, licking side to side as it burns.
     */
    static void ribbon(FireMesh.Sink sink, double[] a, double[] b, double wx, double wy, double wz, double vx, double vy, double vz, double wide,
                       double end, int root, int tip, double alpha, double age, int seed, int n) {
        double lx = b[0] - a[0];
        double ly = b[1] - a[1];
        double lz = b[2] - a[2];
        double len = Math.sqrt(lx * lx + ly * ly + lz * lz);
        if (len < 1.0E-4 || alpha <= 0.0) {
            return;
        }
        lx /= len;
        ly /= len;
        lz /= len;
        // the side: the given way with its share along the ribbon taken out
        double d = wx * lx + wy * ly + wz * lz;
        double sx = wx - lx * d;
        double sy = wy - ly * d;
        double sz = wz - lz * d;
        double sl = Math.sqrt(sx * sx + sy * sy + sz * sz);
        if (sl < 0.2) {
            d = vx * lx + vy * ly + vz * lz;
            sx = vx - lx * d;
            sy = vy - ly * d;
            sz = vz - lz * d;
            sl = Math.sqrt(sx * sx + sy * sy + sz * sz);
        }
        if (sl < 1.0E-4) {
            return;
        }
        sx /= sl;
        sy /= sl;
        sz /= sl;
        // the lick: across the ribbon's face (its side crossed with its length)
        double nx = sy * lz - sz * ly;
        double ny = sz * lx - sx * lz;
        double nz = sx * ly - sy * lx;
        double phase = FireMesh.rand(seed, 3) * 6.28;
        double[] prev = null;
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            double lick = Math.sin(age * 0.55 + phase + t * 3.2) * 0.18 * len * t * t;
            double px = a[0] + lx * len * t + nx * lick;
            double py = a[1] + ly * len * t + ny * lick;
            double pz = a[2] + lz * len * t + nz * lick;
            double half = 0.5 * (wide + (end - wide) * t);
            int mid = FireMesh.argb(alpha * (1.0 - 0.85 * t * t), FireMesh.mix(root, tip, t));
            int edge = FireMesh.argb(0.0, FireMesh.mix(root, tip, t));
            double[] cur = {px, py, pz, px + sx * half, py + sy * half, pz + sz * half, px - sx * half, py - sy * half, pz - sz * half, mid, edge};
            if (prev != null) {
                int pm = (int) prev[9];
                int pe = (int) prev[10];
                FireMesh.quad(sink, prev[0], prev[1], prev[2], pm, prev[3], prev[4], prev[5], pe, cur[3], cur[4], cur[5], edge, px, py, pz, mid);
                FireMesh.quad(sink, prev[0], prev[1], prev[2], pm, prev[6], prev[7], prev[8], pe, cur[6], cur[7], cur[8], edge, px, py, pz, mid);
            }
            prev = cur;
        }
    }
    // ------------------------------------------------------------------------------------------------------ the flamethrower (M1 held)

    /**
     * 3.7.7 (the owner: "Transformed M1, shall make the bird open it's beak and act as a flamethrower (blasting flames at the target like
     * the dragon does)"): how many puffs a tick leave the beak, how long each lives to reach the end (ticks), how far the flame runs out
     * of the beak's own way before it has turned onto the aim (blocks), its detail (the phoenix's own model beside it: the phone's budget).
     */
    public static final double BREATH_RATE = 1.0;
    public static final double BREATH_LIFE = 12.0;
    public static final double BREATH_TURN = 3.0;
    static final double BREATH_QUALITY = 0.65;
    static final double[] AIM = new double[9];
    static final double[] AT = new double[3];

    /** How far a puff born {@code age} ticks ago has gone of {@code range}: fast out of the beak, slowing to a stop at the end. */
    public static double reach(double age, double range) {
        double u = clamp(age / BREATH_LIFE);
        return range * (1.0 - Math.pow(1.0 - u, 1.6));
    }

    /** The point {@code d} blocks along the flame leaving the beak in {@link #AIM}: straight out of it, turned onto the aim. */
    static void along(double d, double[] out) {
        double s = BREATH_TURN * (1.0 - Math.exp(-d / BREATH_TURN));
        out[0] = AIM[0] + AIM[6] * s + AIM[3] * (d - s);
        out[1] = AIM[1] + AIM[7] * s + AIM[4] * (d - s);
        out[2] = AIM[2] + AIM[8] * s + AIM[5] * (d - s);
    }

    /**
     * The phoenix's flamethrower, on for {@code on} ticks (off for {@code off}, -1 while on), as the dragon's breath (dragon/UoFire.breath)
     * in the phoenix's fire: a white-hot jet out of the open beak turning to pale cyan, a stream of fire puffs that bloom as they go out to
     * {@code range} and burn from white through cyan (one in three gold) to the deep blue of its edge, white shock rings racing out round
     * it, and its blue light. {@code aim} gives the beak (eye-relative), the aim and the beak's own way, as they were {@code ago} ticks back
     * (so the stream bends as the phoenix turns its head).
     */
    public static void breath(FireMesh.Sink sink, com.hexhaki.piece.dragon.UoFire.Aim aim, double on, double off, double range, int seed) {
        double quality = FireMesh.quality;
        FireMesh.quality = quality * BREATH_QUALITY;
        double now = on;
        int last = (int) Math.floor(now * BREATH_RATE);
        int first = Math.max(0, (int) Math.ceil((now - BREATH_LIFE) * BREATH_RATE));
        double stopped = off < 0.0 ? Double.MAX_VALUE : now - off;
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
            FireMesh.basis(AIM[3], AIM[4], AIM[5], BB);
            double clear = Math.max(0.0, d - 1.2); // out of the beak: from here it may bloom
            double spread = d * 0.03 + clear * 0.09;
            along(d, AT);
            double px = AT[0] + (BB[0] * r1 + BB[3] * r2) * spread;
            double py = AT[1] + (BB[1] * r1 + BB[4] * r2) * spread;
            double pz = AT[2] + (BB[2] * r1 + BB[5] * r2) * spread;
            // a narrow jet at the beak, a wall of blue fire at the end
            double radius = (0.12 + d * 0.06 + clear * 0.13) * (0.75 + 0.5 * r0);
            double end = Math.max(0.0, (u - 0.78) / 0.22);
            radius *= 1.0 - end * end;
            // white out of the beak, burning to cyan and the deep blue of its edge as it goes (the phoenix's flame is blue)
            double heat = 1.0 - 0.62 * Math.pow(u, 0.6) + 0.06 * r0;
            double turn = Math.exp(-d / BREATH_TURN);
            double vx = AIM[3] + (AIM[6] - AIM[3]) * turn;
            double vy = AIM[4] + (AIM[7] - AIM[4]) * turn + 0.08 * u;
            double vz = AIM[5] + (AIM[8] - AIM[5]) * turn;
            double stretch = (1.0 - 0.5 * u) * Math.min(1.0, clear / 2.5);
            // torn into tongues, a dark blue line round each spent flame (the anime's cel outline)
            FireMesh.palette(k % 4 == 1 ? PhoenixFire.GOLD : PhoenixFire.CYAN);
            FireMesh.puff(sink, px, py, pz, radius, vx, vy, vz, stretch, 0.3, heat, 0.45, true, 1.0, now + k * 3.7, s);
            FireMesh.palette(FireMesh.BAND);
        }
        // the jet: white-hot out of the beak, as long as the flame has had time to throw it (gone a few ticks after it stops)
        double fade = off < 0.0 ? 1.0 : Math.max(0.0, 1.0 - off / 4.0);
        if (fade > 0.0) {
            aim.at(0.0, AIM);
            double length = Math.min(range * 0.3, now * 4.0) * fade;
            double grow = Math.min(1.0, now / 3.0);
            double[] cut = {0.0, 0.8, 2.0, 4.0, 7.0, length};
            double ax = AIM[0];
            double ay = AIM[1];
            double az = AIM[2];
            for (int i = 1; i < cut.length && cut[i - 1] < length; i++) {
                double d = Math.min(cut[i], length);
                along(d, AT);
                // narrow: the flame is its puffs; the jet only the white-hot rush out of the beak, cooling to cyan
                double ra = (0.08 + 0.6 * cut[i - 1] / (range * 0.3)) * grow;
                double rb = (0.08 + 0.6 * d / (range * 0.3)) * grow;
                FireMesh.tube(sink, ax, ay, az, AT[0], AT[1], AT[2], ra, rb, FireMesh.mix(0xFFFFFF, 0x8FF0FA, cut[i - 1] / length),
                    FireMesh.mix(0xFFFFFF, 0x8FF0FA, d / length), 0x2DB8EC, 0.35, 0.9, 0.22, now, seed + i);
                ax = AT[0];
                ay = AT[1];
                az = AT[2];
            }
            // the white heart in the beak, its light filling the gape
            puff(sink, AIM[0], AIM[1], AIM[2], 0.22 * grow * fade, AIM[6], AIM[7], AIM[8], 0.6, 0.12, 1.1, 0.0, 1.0, now, seed + 5, false);
            FireMesh.shell(sink, AIM[0] + AIM[6] * 0.3, AIM[1] + AIM[7] * 0.3, AIM[2] + AIM[8] * 0.3,
                Math.min(0.8 * grow * fade, 0.9 * Math.sqrt(AIM[0] * AIM[0] + AIM[1] * AIM[1] + AIM[2] * AIM[2])), 0xD2FBFF, 0.55, 0.0);
        }
        // the light round the stream: thin and cool (light, not smoke)
        double front = reach(Math.min(now, BREATH_LIFE), range);
        double back = off < 0.0 ? 0.0 : reach(Math.min(off, BREATH_LIFE), range);
        if (front - back >= 0.5) {
            aim.at(0.0, AIM);
            for (int j = 0; j < 2; j++) {
                double d = back + (front - back) * (j + 0.5) / 2.0;
                double radius = (0.25 + d * 0.06 + Math.max(0.0, d - 1.2) * 0.15) * 2.0;
                along(d, AT);
                double eye = Math.sqrt(AT[0] * AT[0] + AT[1] * AT[1] + AT[2] * AT[2]);
                FireMesh.shell(sink, AT[0], AT[1], AT[2], Math.min(radius, eye * 0.9), 0x46DDF5, 0.26, 0.0);
            }
        }
        // the shock rings: one every 4 ticks, out along the flame, widening as they go
        int arcs = (int) Math.floor(now / 4.0);
        for (int k = arcs; k >= 0 && k > arcs - 3; k--) {
            double born = k * 4.0 + 1.0;
            double age = now - born;
            if (age < 0.0 || born > stopped || age > 9.0) {
                continue;
            }
            aim.at(age, AIM);
            double t = age / 9.0;
            double d = reach(age * 1.4, range) * 0.75 + 1.5;
            along(d, AT);
            double radius = 1.0 + 4.0 * Math.sqrt(t);
            double start = FireMesh.rand(seed * 17 + k, 0) * Math.PI * 2.0;
            FireMesh.arc(sink, AT[0], AT[1], AT[2], AIM[3], AIM[4], AIM[5], radius, 0.25 + 0.3 * (1.0 - t), start, 3.2, -2.0 * t - 0.6,
                0xE8FFFF, 0.75 * (1.0 - t * t));
        }
        FireMesh.quality = quality;
    }
}
