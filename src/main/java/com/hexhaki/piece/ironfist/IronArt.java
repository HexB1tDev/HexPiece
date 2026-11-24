package com.hexhaki.piece.ironfist;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.mochi.MochiArt;

/**
 * 3.5.8: the Iron Fist remake's art (Garp's: docs/handoff 8zzs), eye-centred, in the house's style: the banded solids and soft shells of
 * FireMesh, the Gura fruit's shock and quake (gura/GuraArt), the Mochi fruit's rounded blocks (mochi/MochiArt) for the iron and the
 * ghost fists. Alpha blended, depth tested, never additive. Pure: client/ironfist/IronFx and tools/preview draw the very same quads.
 */
public final class IronArt {
    private IronArt() {
    }

    /** Cast iron: its lit face, its body, its shadow, its darkest, its rim (as MochiArt's bands). */
    public static final int[] IRON = {0xA7AFB9, 0x4B5058, 0x2C3036, 0x18191D, 0x707884};
    public static final int WHITE = 0xFFFFFF;
    public static final int GOLD = 0xFFD54A;
    public static final int ORANGE = 0xFF8A1E;
    public static final int CRIMSON = 0xD01830;
    public static final int INK = 0x050505;
    public static final int[] ROCKS = {0x6E6258, 0x5B5048, 0x7A6A55, 0x4E463F, 0x6B4F33};
    static final double[] B = new double[6];
    static final double[] SHEET = new double[9 * 11 * 3];
    static final int[] SHEET_C = new int[9 * 11];
    static final double[] DOME = new double[10 * 27 * 3];
    static final int[] DOME_C = new int[10 * 27];

    static double clamp(double t) {
        return Math.max(0.0, Math.min(1.0, t));
    }

    static double smooth(double t) {
        double u = clamp(t);
        return u * u * (3.0 - 2.0 * u);
    }

    // ------------------------------------------------------------------------------------------------------ Meteor Fist

    /** The black iron cannonball at (x, y, z), {@code r} round, turned {@code spin}: banded cast iron, its casting seam round it. */
    public static void cannonball(FireMesh.Sink sink, double x, double y, double z, double r, double spin, double time, int seed) {
        double ax = Math.cos(spin);
        double az = Math.sin(spin);
        MochiArt.blob(sink, x, y, z, ax, 0.0, az, 0.0, 1.0, 0.0, r, r, r, 1.0, 0.02, time, seed, IRON, 1.0);
        MochiArt.torus(sink, x, y, z, -az, 0.35, ax, r * 0.99, r * 0.07, 1.0, IRON);
    }

    /**
     * What a fast body leaves in the air behind it at (x, y, z) flying along (ux, uy, uz) (unit) at {@code speed} blocks a tick,
     * {@code r} round: white wind streaks pulled back off it and rings of shocked air left one after another, widening as they fall
     * behind.
     */
    public static void trail(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double speed, double r, double age,
                             int seed) {
        FireMesh.basis(ux, uy, uz, B);
        for (int k = 0; k < 6; k++) {
            double a = 2 * Math.PI * k / 6 + age * 0.7;
            double ox = (B[0] * Math.cos(a) + B[3] * Math.sin(a)) * r * 1.05;
            double oy = (B[1] * Math.cos(a) + B[4] * Math.sin(a)) * r * 1.05;
            double oz = (B[2] * Math.cos(a) + B[5] * Math.sin(a)) * r * 1.05;
            double len = speed * (2.2 + FireMesh.rand(seed, k) * 1.4);
            FireMesh.streak(sink, x + ox - ux * len * 0.5, y + oy - uy * len * 0.5, z + oz - uz * len * 0.5, ux, uy, uz, len, r * 0.16, WHITE, 0.75);
        }
        for (int k = 0; k < 4; k++) {
            double phase = (age * 0.5 + k) % 4.0;
            double back = r * 1.5 + phase * speed * 0.9;
            double open = r * (1.3 + phase * 0.55);
            FireMesh.ring(sink, x - ux * back, y - uy * back, z - uz * back, ux, uy, uz, open, r * 0.45, -r * 0.3, WHITE, 0.4 * (1.0 - phase / 4.0));
        }
    }

    // ------------------------------------------------------------------------------------------------------ blows

    /** A ring of pressure at (x, y, z) round (ux, uy, uz) opening to {@code radius} over {@code life} ticks, {@code age} in. */
    public static void pressure(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double radius, double age,
                                double life) {
        double t = age / life;
        if (t < 0.0 || t > 1.0) {
            return;
        }
        double open = 0.3 + (radius - 0.3) * (1.0 - (1.0 - t) * (1.0 - t));
        FireMesh.ring(sink, x, y, z, ux, uy, uz, open, Math.max(0.12, radius * 0.22 * (1.0 - t)), radius * 0.08, WHITE, 0.85 * (1.0 - t));
    }

    /**
     * The manga's thick black impact lines round a blow at (x, y, z) struck along (nx, ny, nz) (unit): rays of ink thrown out across the
     * blow, {@code size} long, gone by {@code life}.
     */
    public static void impactLines(FireMesh.Sink sink, double x, double y, double z, double nx, double ny, double nz, double size, double age,
                                   double life, int seed) {
        if (age < 0.0 || age > life) {
            return;
        }
        double fade = 1.0 - age / life;
        // the manga's lines are drawn on the page: square to the eye (at the origin), whatever way the blow went
        double el = Math.max(1.0E-6, Math.sqrt(x * x + y * y + z * z));
        FireMesh.basis(-x / el, -y / el, -z / el, B);
        int n = 12;
        for (int k = 0; k < n; k++) {
            double a = 2 * Math.PI * (k + FireMesh.rand(seed, k) * 0.6) / n;
            double dx = B[0] * Math.cos(a) + B[3] * Math.sin(a);
            double dy = B[1] * Math.cos(a) + B[4] * Math.sin(a);
            double dz = B[2] * Math.cos(a) + B[5] * Math.sin(a);
            double from = size * (0.18 + 0.12 * FireMesh.rand(seed, k + 20)) * (1.0 + age * 0.15);
            double len = size * (0.55 + 0.5 * FireMesh.rand(seed, k + 40));
            FireMesh.ray(sink, x + dx * from, y + dy * from, z + dz * from, dx, dy, dz, len, size * 0.05, 0, 0.0, INK, 0.9 * fade);
        }
    }

    /** Subtle black Haki sparks with crimson edges spitting off (x, y, z), {@code size} across. */
    public static void sparks(FireMesh.Sink sink, double x, double y, double z, double size, double age, int seed) {
        for (int k = 0; k < 7; k++) {
            int s = seed * 31 + k + (int) age;
            double a = 2 * Math.PI * FireMesh.rand(s, 1);
            double b = Math.acos(2 * FireMesh.rand(s, 2) - 1);
            double dx = Math.sin(b) * Math.cos(a);
            double dy = Math.cos(b);
            double dz = Math.sin(b) * Math.sin(a);
            double from = size * 0.25;
            double len = size * (0.35 + 0.45 * FireMesh.rand(s, 3));
            FireMesh.ray(sink, x + dx * from, y + dy * from, z + dz * from, dx, dy, dz, len, size * 0.09, 0, 0.0, CRIMSON, 0.7);
            FireMesh.ray(sink, x + dx * from, y + dy * from, z + dz * from, dx, dy, dz, len * 0.9, size * 0.045, 0, 0.0, INK, 0.95);
        }
    }

    /**
     * Haki loaded into a fist at (x, y, z), {@code power} 0 to 1: a white heart in a gold, orange and crimson glow held tight round the
     * knuckles (never a ball held in the hand), brighter and whiter as it fills.
     */
    public static void fistGlow(FireMesh.Sink sink, double x, double y, double z, double power, double time, int seed) {
        if (power < 0.02) {
            return;
        }
        double flicker = 1.0 + 0.08 * Math.sin(time * 2.3 + seed);
        FireMesh.shell(sink, x, y, z, (0.2 + 0.26 * power) * flicker, CRIMSON, 0.0, 0.22 * power);
        FireMesh.shell(sink, x, y, z, (0.16 + 0.19 * power) * flicker, ORANGE, 0.04, 0.32 * power);
        FireMesh.shell(sink, x, y, z, (0.12 + 0.13 * power) * flicker, GOLD, 0.25 * power, 0.5 * power);
        FireMesh.shell(sink, x, y, z, (0.06 + 0.09 * power) * flicker, WHITE, 0.95 * Math.min(1.0, 0.4 + power), 0.7 * power);
        // the light pricking out between the knuckles, short and sharp
        for (int k = 0; k < 6; k++) {
            double a = 2 * Math.PI * k / 6 + time * 0.3 + seed;
            double b = 0.6 + 0.9 * FireMesh.rand(seed + (int) time, k);
            double dx = Math.sin(b) * Math.cos(a);
            double dy = Math.cos(b);
            double dz = Math.sin(b) * Math.sin(a);
            FireMesh.ray(sink, x, y, z, dx, dy, dz, (0.25 + 0.5 * power) * (0.6 + 0.4 * FireMesh.rand(seed + (int) time, k + 9)), 0.05, 0, 0.0,
                power > 0.7 ? WHITE : GOLD, 0.7 * power);
        }
    }

    /** Air pulled in and pressed into a point at (x, y, z): rings closing on it one after another, {@code reach} out at their widest. */
    public static void gather(FireMesh.Sink sink, double x, double y, double z, double reach, double power, double time, int seed) {
        if (power < 0.02) {
            return;
        }
        for (int k = 0; k < 3; k++) {
            double phase = ((time / 14.0 + k / 3.0) % 1.0 + 1.0) % 1.0;
            double a = 2 * Math.PI * FireMesh.rand(seed, k);
            double b = 0.4 + 1.2 * FireMesh.rand(seed, k + 5);
            double ux = Math.sin(b) * Math.cos(a);
            double uy = Math.cos(b);
            double uz = Math.sin(b) * Math.sin(a);
            double r = 0.2 + reach * (1.0 - phase);
            FireMesh.ring(sink, x, y, z, ux, uy, uz, r, Math.max(0.06, r * 0.12), 0.0, WHITE, 0.5 * power * phase);
        }
    }

    /**
     * Ghost fists of the barrage round the chest (x, y, z) facing (ux, uz) (flat, unit): a few Haki-black fists flickering out at
     * different heights and sides, each gone in a couple of ticks (never another pair of arms).
     */
    public static void afterimages(FireMesh.Sink sink, double x, double y, double z, double ux, double uz, double speed, double time, int seed) {
        double px = -uz;
        double pz = ux;
        int n = 2 + (int) Math.round(3 * speed);
        for (int k = 0; k < n; k++) {
            int s = (int) Math.floor(time * 0.5) * 7 + k;
            double side = (FireMesh.rand(seed + s, 1) - 0.5) * 1.6;
            double up = (FireMesh.rand(seed + s, 2) - 0.5) * 0.9;
            double out = 0.7 + 0.8 * FireMesh.rand(seed + s, 3);
            double life = (time * 0.5) % 1.0;
            double alpha = 0.55 * (1.0 - life);
            double cx = x + ux * out + px * side;
            double cy = y + up;
            double cz = z + uz * out + pz * side;
            MochiArt.blob(sink, cx, cy, cz, ux, 0, uz, 0, 1, 0, 0.17, 0.15, 0.2, 0.32, 0.0, time, s, MochiArt.HAKI, alpha);
            // its forearm, short: a streak back toward the shoulder
            FireMesh.streak(sink, cx - ux * 0.45, cy, cz - uz * 0.45, ux, 0, uz, 0.6, 0.09, 0x24242F, alpha * 0.8);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Galaxy Divide

    /**
     * Galaxy Divide's shock at its front (x, y, z) (on the ground) rushing along (fx, fz) (flat, unit), {@code height} tall and
     * {@code width} wide: almost clear in its middle, its shape drawn by the bent air at its edges, white pressure lines up both sides and
     * over the top, golden flashes flickering in it.
     */
    public static void shockSheet(FireMesh.Sink sink, double x, double y, double z, double fx, double fz, double height, double width,
                                  double fade, double time, int seed) {
        if (fade < 0.02) {
            return;
        }
        double px = -fz;
        double pz = fx;
        int cols = 8;
        int rows = 10;
        double[] g = SHEET;
        int[] c = SHEET_C;
        for (int j = 0; j <= rows; j++) {
            double v = j / (double) rows;
            for (int i = 0; i <= cols; i++) {
                double s = i / (double) cols * 2.0 - 1.0;
                double bow = 1.6 * (1.0 - s * s) * Math.sin(Math.PI * v) + 0.25 * Math.sin(time * 0.9 + v * 7.0 + s * 3.0);
                double half = width * 0.5 * (0.6 + 0.4 * Math.sin(Math.PI * Math.min(1.0, v * 1.15)));
                int k = j * (cols + 1) + i;
                g[k * 3] = x + px * s * half + fx * bow;
                g[k * 3 + 1] = y + height * v;
                g[k * 3 + 2] = z + pz * s * half + fz * bow;
                double edge = Math.max(s * s, v * v * v);
                c[k] = FireMesh.argb(fade * (0.06 + 0.62 * edge), FireMesh.mix(WHITE, GOLD, 0.3 * v));
            }
        }
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                int a = j * (cols + 1) + i;
                int b = a + 1;
                int d = a + cols + 1;
                int e = d + 1;
                FireMesh.quad(sink, g[a * 3], g[a * 3 + 1], g[a * 3 + 2], c[a], g[b * 3], g[b * 3 + 1], g[b * 3 + 2], c[b], g[e * 3], g[e * 3 + 1],
                    g[e * 3 + 2], c[e], g[d * 3], g[d * 3 + 1], g[d * 3 + 2], c[d]);
            }
        }
        // the pressure lines: up both edges and arched over the top, and a few thrown back off the front
        for (int side = -1; side <= 1; side += 2) {
            for (int j = 0; j < rows; j++) {
                int a = j * (cols + 1) + (side < 0 ? 0 : cols);
                int b = a + cols + 1;
                double lx = g[b * 3] - g[a * 3];
                double ly = g[b * 3 + 1] - g[a * 3 + 1];
                double lz = g[b * 3 + 2] - g[a * 3 + 2];
                double ll = Math.sqrt(lx * lx + ly * ly + lz * lz);
                FireMesh.ray(sink, g[a * 3], g[a * 3 + 1], g[a * 3 + 2], lx / ll, ly / ll, lz / ll, ll * 1.05, 0.55, 0, 0.0, WHITE, 0.95 * fade);
            }
        }
        for (int i = 0; i < cols; i++) {
            int a = rows * (cols + 1) + i;
            int b = a + 1;
            double lx = g[b * 3] - g[a * 3];
            double ly = g[b * 3 + 1] - g[a * 3 + 1];
            double lz = g[b * 3 + 2] - g[a * 3 + 2];
            double ll = Math.max(1.0E-6, Math.sqrt(lx * lx + ly * ly + lz * lz));
            FireMesh.ray(sink, g[a * 3], g[a * 3 + 1], g[a * 3 + 2], lx / ll, ly / ll, lz / ll, ll * 1.05, 0.45, 0, 0.0, WHITE, 0.9 * fade);
        }
        for (int k = 0; k < 9; k++) {
            int s = seed + k * 13 + (int) Math.floor(time);
            double v = FireMesh.rand(s, 1);
            double sx = (FireMesh.rand(s, 2) - 0.5) * width * 0.8;
            double hx = x + px * sx + fx * 0.8;
            double hy = y + height * v;
            double hz = z + pz * sx + fz * 0.8;
            FireMesh.palette(FireMesh.LIGHT);
            FireMesh.puff(sink, hx, hy, hz, 0.6 + 0.9 * FireMesh.rand(s, 3), 0, 0, 0, 1.0, 0.3, 0.92, 0.55, false, 0.85 * fade, time, s);
            FireMesh.palette(FireMesh.BAND);
            FireMesh.ray(sink, hx, hy, hz, -fx, 0.15, -fz, 3.0 + 3.0 * FireMesh.rand(s, 4), 0.14, 0, 0.0, WHITE, 0.6 * fade);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Galaxy Impact

    /**
     * Galaxy Impact's dome at (x, y, z) (the ground) {@code radius} round: a hemisphere of golden light, almost pure white at its
     * crown, gold, orange and crimson toward its foot, its surface rolling and distorted by the pressure in it; {@code fade} 0 to 1.
     */
    public static void dome(FireMesh.Sink sink, double x, double y, double z, double radius, double fade, double time, int seed) {
        if (fade < 0.02 || radius < 0.3) {
            return;
        }
        int rings = 9;
        int sides = 26;
        double[] g = DOME;
        int[] c = DOME_C;
        for (int j = 0; j <= rings; j++) {
            double lat = Math.PI / 2 * j / rings;
            double cl = Math.cos(lat);
            double sl = Math.sin(lat);
            for (int i = 0; i <= sides; i++) {
                double lon = 2 * Math.PI * i / sides;
                double wob = 1.0 + 0.11 * FireMesh.noise(Math.cos(lon) * 2.5 + time * 0.08, sl * 2.5, Math.sin(lon) * 2.5 - time * 0.06, seed)
                    + 0.04 * Math.sin(time * 0.7 + lon * 6.0 + lat * 4.0);
                int k = j * (sides + 1) + i;
                g[k * 3] = x + Math.cos(lon) * cl * radius * wob;
                g[k * 3 + 1] = y + sl * radius * 0.92 * wob - radius * 0.06;
                g[k * 3 + 2] = z + Math.sin(lon) * cl * radius * wob;
                double heat = 0.12 + 0.88 * Math.pow(sl, 0.55);
                c[k] = FireMesh.argb(fade * (0.42 + 0.48 * sl), FireMesh.fire(heat, 1.0) & 0xFFFFFF);
            }
        }
        for (int j = 0; j < rings; j++) {
            for (int i = 0; i < sides; i++) {
                int a = j * (sides + 1) + i;
                int b = a + 1;
                int d = a + sides + 1;
                int e = d + 1;
                FireMesh.quad(sink, g[a * 3], g[a * 3 + 1], g[a * 3 + 2], c[a], g[b * 3], g[b * 3 + 1], g[b * 3 + 2], c[b], g[e * 3], g[e * 3 + 1],
                    g[e * 3 + 2], c[e], g[d * 3], g[d * 3 + 1], g[d * 3 + 2], c[d]);
            }
        }
        // the white heart of it, and the curved pressure lines over its skin
        FireMesh.shell(sink, x, y + radius * 0.15, z, radius * 0.42, WHITE, 0.85 * fade, 0.25 * fade);
        for (int k = 0; k < 10; k++) {
            double lon = 2 * Math.PI * (k + FireMesh.rand(seed, k)) / 10 + time * 0.01;
            FireMesh.arc(sink, x, y, z, -Math.sin(lon), 0, Math.cos(lon), radius * 1.02, radius * 0.03, 0.15, Math.PI * 0.35, 0.0, WHITE, 0.55 * fade);
        }
    }

    /** A ring of shock running over the ground at (x, y, z), {@code radius} out: low, white, its outer edge lifted off the ground. */
    public static void groundRing(FireMesh.Sink sink, double x, double y, double z, double radius, double alpha) {
        FireMesh.ring(sink, x, y + 0.2, z, 0, 1, 0, radius, Math.max(0.4, radius * 0.06), radius * 0.03, WHITE, alpha);
    }

    /**
     * A wall of dust and smoke rolling out at {@code radius} round (x, y, z), {@code height} tall: big soft puffs of the house's smoke,
     * fewer far off.
     */
    public static void dustWall(FireMesh.Sink sink, double x, double y, double z, double radius, double height, double alpha, double time,
                                int seed) {
        if (alpha < 0.02 || radius < 0.5) {
            return;
        }
        int n = (int) Math.max(8, Math.min(20, radius * 0.6));
        // soft dust needs no fine facets (its bands hide them): drawn a level coarser than fire of its size, so a wall around the eye
        // (Galaxy Impact's user stands in its middle) stays a few thousand quads
        double quality = FireMesh.quality;
        FireMesh.quality = quality * 0.4;
        FireMesh.palette(FireMesh.SMOKE);
        for (int k = 0; k < n; k++) {
            double a = 2 * Math.PI * (k + FireMesh.rand(seed, k)) / n;
            double r = radius * (0.9 + 0.15 * FireMesh.rand(seed, k + 50));
            double size = height * (0.45 + 0.35 * FireMesh.rand(seed, k + 90));
            FireMesh.puff(sink, x + Math.cos(a) * r, y + size * 0.6, z + Math.sin(a) * r, size, Math.cos(a) * 0.2, 0.1, Math.sin(a) * 0.2, 1.1, 0.25,
                0.8 + 0.15 * FireMesh.rand(seed, k + 130), 0.85, false, alpha * 0.6, time, seed + k);
        }
        FireMesh.quality = quality;
        FireMesh.palette(FireMesh.BAND);
    }

    /** A chunk of the ground torn loose at (x, y, z), {@code size} across, turned {@code spin}. */
    public static void chunk(FireMesh.Sink sink, double x, double y, double z, double size, double spin, int seed, double alpha) {
        FireMesh.shard(sink, x, y, z, size, spin, seed, ROCKS[Math.floorMod(seed, ROCKS.length)], alpha);
    }

    /** Where chunk {@code k} of a burst at the origin is {@code age} ticks on (thrown out and up, falling): into out (x, y, z, spin). */
    public static void thrown(int k, int seed, double reach, double lift, double age, double[] out) {
        double a = 2 * Math.PI * FireMesh.rand(seed, k);
        double sp = reach * (0.35 + 0.65 * FireMesh.rand(seed, k + 60)) / 20.0;
        double up = lift * (0.5 + 0.5 * FireMesh.rand(seed, k + 120));
        out[0] = Math.cos(a) * sp * age;
        out[1] = up * age - 0.045 * age * age;
        out[2] = Math.sin(a) * sp * age;
        out[3] = age * (0.2 + 0.3 * FireMesh.rand(seed, k + 180));
    }

    /** A bright golden-white point where the force meets the ground, the air drawn into it. */
    public static void point(FireMesh.Sink sink, double x, double y, double z, double size, double fade, double time, int seed) {
        if (fade < 0.02) {
            return;
        }
        FireMesh.shell(sink, x, y, z, size, WHITE, 0.95 * fade, 0.4 * fade);
        FireMesh.shell(sink, x, y, z, size * 1.8, GOLD, 0.2 * fade, 0.45 * fade);
        FireMesh.shell(sink, x, y, z, size * 2.6, CRIMSON, 0.0, 0.25 * fade);
        for (int k = 0; k < 8; k++) {
            double a = 2 * Math.PI * k / 8 + time * 0.2;
            FireMesh.ray(sink, x, y, z, Math.cos(a), 0.15, Math.sin(a), size * (3.0 + 2.0 * FireMesh.rand(seed, k)), size * 0.25, 0, 0.0, WHITE,
                0.7 * fade);
        }
    }
}
