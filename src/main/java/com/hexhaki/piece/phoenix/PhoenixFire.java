package com.hexhaki.piece.phoenix;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.6.7 (the owner: "the body is made of flames, my model did not burn, which is what I want. moving flames"; the reference: Marco's
 * phoenix, cyan fire with gold through it, flowing off the wings and tail all the time): the tongues of fire that stream off the
 * phoenix's body continuously, drawn as the house draws fire (FireMesh's banded puffs, in the phoenix's own cyan and gold), each born
 * at one of the body's fire points (PhoenixForm.Shape.anchors), rising, streaming back with the wind of its flight, shrinking away.
 * Pure: the game's renderer and the previews share it; eye-relative points.
 */
public final class PhoenixFire {
    private PhoenixFire() {
    }

    /** The phoenix's fire, coolest first (as FireMesh.BAND): deep blue, blue, azure, cyan, bright cyan, pale cyan, ice, white. */
    public static final int[] CYAN = {0x0D3F8A, 0x1660B8, 0x1F8AD8, 0x2DB8EC, 0x46DDF5, 0x8FF0FA, 0xD2FBFF, 0xFFFFFF};
    /** Its gold (the crest, the tail's streamers, the flames' cores in the reference): amber to pale yellow. */
    public static final int[] GOLD = {0x8A4A06, 0xC0700C, 0xE89A16, 0xFFBE2E, 0xFFD84E, 0xFFEA8A, 0xFFF7CF, 0xFFFFFF};

    /** The tongues' shading, heart (lit, facing the eye) to rim, as the house's tubes are banded (mochi/MochiArt.tube). */
    public static final int[] CYAN_TUBE = {0xEAFFFF, 0xA4F4FB, 0x48DEF0, 0x22AEE0, 0x1172C4};
    public static final int[] GOLD_TUBE = {0xFFFCE6, 0xFFEC96, 0xFFCC40, 0xEE9C16, 0xBC660C};

    /**
     * One tongue of fire licking up off the body at (x, y, z), {@code age} of its {@code life} ticks in (the reference: Marco's flames
     * flow like liquid, never still): a tapering ribbon of flame, thick at its root, needle thin at its tip, curling: a wave runs up it
     * from the root to the tip as it grows out, it streams back with the wind (wx, wy, wz: blocks a tick) and rises; past the middle of
     * its life its root lets go of the body and it lifts away, thinning out to nothing. {@code size} its length (blocks); gold or cyan.
     */
    public static void tongue(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double age, double life,
                              double size, boolean gold, int seed) {
        if (age < 0 || age > life) {
            return;
        }
        double f = age / life;
        double grow = Math.min(1.0, f * 2.2);
        double go = Math.max(0.0, (f - 0.5) / 0.5);
        double len = size * (0.35 + 0.65 * grow) * (1.0 - 0.4 * go);
        double thick = size * 0.16 * (1.0 - go * go);
        if (thick < 0.008 || len < 0.03) {
            return;
        }
        // the way it streams: up and back with the wind
        double ux = wx * 6.0;
        double uy = 1.0;
        double uz = wz * 6.0;
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul;
        uy /= ul;
        uz /= ul;
        // a sideways axis for its curl (level, square to its way)
        double sx = -uz;
        double sz = ux;
        double sl = Math.sqrt(sx * sx + sz * sz);
        if (sl < 1.0E-4) {
            sx = 1;
            sz = 0;
            sl = 1;
        }
        sx /= sl;
        sz /= sl;
        double phase = FireMesh.rand(seed, 3) * 6.283;
        double lift = PhoenixForm.TONGUE_RISE * go * go + 0.15 * size * f;
        double bx = x + wx * age * 0.5;
        double by = y + lift;
        double bz = z + wz * age * 0.5;
        com.hexhaki.piece.mochi.MochiArt.tubeBegin();
        int n = 7;
        for (int k = 0; k <= n; k++) {
            double t = k / (double) n;
            // the curl: a wave running up from the root, stronger toward the tip
            double wave = Math.sin(t * 4.2 - age * 0.9 + phase) * t * size * 0.32;
            double wave2 = Math.cos(t * 3.1 - age * 0.7 + phase) * t * size * 0.18;
            double px = bx + ux * len * t + sx * wave;
            double py = by + uy * len * t + wave2 * 0.5;
            double pz = bz + uz * len * t + sz * wave;
            // thick at the root (or where it let go), drawn to a point at its tip
            double r = thick * Math.pow(1.0 - t, 1.3) * (0.75 + 0.25 * Math.sin(Math.PI * Math.min(1.0, t * 2.5)));
            com.hexhaki.piece.mochi.MochiArt.tubePoint(px, py, pz, Math.max(0.004, r));
        }
        com.hexhaki.piece.mochi.MochiArt.tube(sink, 6, gold ? GOLD_TUBE : CYAN_TUBE, 0.0, 0.0);
    }

    /**
     * The tongues streaming off the whole body: the skinned fire points {@code points} (x, y, z eye-relative, one per anchor), the clock
     * {@code ticks}, the wind (the body's own motion reversed, blocks a tick), the phoenix's scale (its tongues grow with it as it forms).
     */
    public static void burn(FireMesh.Sink sink, double[] points, int count, double ticks, double wx, double wy, double wz, double scale, int seed) {
        burn(sink, points, null, count, ticks, wx, wy, wz, scale, seed);
    }

    /** As above, each point's tongues sized by {@code sizes} (PhoenixForm.Shape.anchorSize; null: all alike). */
    public static void burn(FireMesh.Sink sink, double[] points, float[] sizes, int count, double ticks, double wx, double wy, double wz, double scale,
                            int seed) {
        for (int k = 0; k < count; k++) {
            double life = PhoenixForm.TONGUE_LIFE + 5.0 * FireMesh.rand(seed, k);
            double age = (ticks + life * FireMesh.rand(seed, k + 100)) % life;
            boolean gold = FireMesh.rand(seed, k + 200) < 0.28;
            double size = (0.45 + 0.45 * FireMesh.rand(seed, k + 300)) * scale * (sizes != null ? sizes[k] : 1.0);
            // each life its own puff (a new seed when it is born again)
            int born = (int) Math.floor((ticks + life * FireMesh.rand(seed, k + 100)) / life);
            tongue(sink, points[k * 3], points[k * 3 + 1], points[k * 3 + 2], wx, wy, wz, age, life, size, gold, seed + k * 31 + born * 7);
        }
    }

    /**
     * 3.6.9: the phoenix forming or going round the player's chest (x, y, z: eye-relative), {@code ticks} into it (the reference: Marco
     * bursting into blue fire, the flames swirling up round him and opening out into the wings). Forming: a column of fire spiralling up
     * round the player, its tongues streaming up and out as it widens, a ring of flame running out over the ground from their feet, a hot
     * cyan and gold heart at the chest that the phoenix opens out of; going: the flames spiral back in to the chest and a last flare
     * goes out as the human comes back. Fire only (tongues and puffs, never a shell of glass).
     */
    public static void change(FireMesh.Sink sink, double x, double y, double z, boolean forming, double ticks, int seed) {
        if (forming) {
            if (ticks > 24.0) {
                return;
            }
            double grow = 1.0 - Math.pow(1.0 - Math.min(1.0, ticks / 13.0), 3.0);
            double fade = 1.0 - Math.max(0.0, Math.min(1.0, (ticks - 12.0) / 12.0));
            column(sink, x, y, z, ticks, 0.35 + 2.3 * grow, (0.45 + 0.75 * grow) * fade + 0.1 * fade, 1.0, seed);
            // the ring of flame running out over the ground, tongues licking up off it
            double ring = 0.6 + 3.6 * grow;
            FireMesh.ring(sink, x, y - 0.95, z, 0, 1, 0, ring, 0.55, 0.2, 0x2DB8EC, 0.7 * fade);
            for (int k = 0; k < 8; k++) {
                double a = 2 * Math.PI * (k + 0.5 * FireMesh.rand(seed, k + 40)) / 8.0;
                double age = (ticks + 7.0 * FireMesh.rand(seed, k + 50)) % 9.0;
                tongue(sink, x + Math.cos(a) * ring, y - 0.95, z + Math.sin(a) * ring, Math.cos(a) * 0.05, 0.0, Math.sin(a) * 0.05, age,
                    9.0, (0.7 + 0.4 * FireMesh.rand(seed, k + 60)) * fade, false, seed + k * 13 + (int) ((ticks + 7.0 * FireMesh.rand(seed, k + 50)) / 9.0));
            }
            // the heart: a burst of cyan and gold tongues flaring out of the chest as it opens, burning down as the body takes over
            flare(sink, x, y, z, Math.sin(Math.PI * Math.min(1.0, ticks / 16.0)), ticks, seed);
            return;
        }
        // going: the column spiralling back in round the chest, the phoenix's last flare at the end
        double pull = Math.min(1.0, ticks / 14.0);
        column(sink, x, y, z, ticks, 0.25 + 2.2 * (1.0 - pull) * (1.0 - pull), 0.25 + 0.75 * (1.0 - pull), -1.0, seed);
        if (ticks > 10.0 && ticks < 20.0) {
            flare(sink, x, y, z, 0.8 * Math.sin(Math.PI * (ticks - 10.0) / 10.0), ticks, seed + 9);
        }
    }

    /** A burst of 10 tongues flung out all round the chest (x, y, z), {@code amount} (0 to 1) of their full length, a third of them gold. */
    private static void flare(FireMesh.Sink sink, double x, double y, double z, double amount, double ticks, int seed) {
        if (amount < 0.05) {
            return;
        }
        for (int k = 0; k < 10; k++) {
            double a = 2 * Math.PI * (k + 0.4 * FireMesh.rand(seed, k + 110)) / 10.0;
            double up = FireMesh.rand(seed, k + 120) * 2.0 - 0.7;
            double c = Math.cos(a);
            double s = Math.sin(a);
            double life = 7.0 + 3.0 * FireMesh.rand(seed, k + 125);
            double start = life * FireMesh.rand(seed, k + 130);
            double r = 0.25 * amount;
            tongue(sink, x + c * r, y + up * 0.3 * amount, z + s * r, c * 0.09, 0.0, s * 0.09, (ticks + start) % life, life,
                (0.5 + 0.5 * FireMesh.rand(seed, k + 135)) * (0.4 + 0.9 * amount), k % 3 == 0, seed + k * 23 + (int) ((ticks + start) / life));
        }
    }

    /**
     * The spiral column of the change: 16 tongues on a turning spiral round the chest (x, y, z), from the feet to over the head,
     * {@code radius} out, {@code size} long, each streaming up and round with the swirl, and out (way 1) or in (way -1).
     */
    private static void column(FireMesh.Sink sink, double x, double y, double z, double ticks, double radius, double size, double way, int seed) {
        int n = 16;
        for (int k = 0; k < n; k++) {
            double h = (k % 4) / 3.0;
            double a = 2 * Math.PI * (k / (double) n + 0.25 * h) + ticks * 0.28 + 0.4 * FireMesh.rand(seed, k + 70);
            double r = radius * (0.55 + 0.25 * h + 0.3 * FireMesh.rand(seed, k + 80));
            double life = 9.0 + 4.0 * FireMesh.rand(seed, k + 85);
            double start = life * FireMesh.rand(seed, k + 90);
            double age = (ticks + start) % life;
            int born = (int) Math.floor((ticks + start) / life);
            double c = Math.cos(a);
            double s = Math.sin(a);
            // round with the swirl and out (or in)
            double wx = -s * 0.07 + c * 0.05 * way;
            double wz = c * 0.07 + s * 0.05 * way;
            tongue(sink, x + c * r, y - 0.9 + 2.6 * h, z + s * r, wx, 0.0, wz, age, life,
                Math.min(1.5, size * (1.0 + 0.6 * FireMesh.rand(seed, k + 95))), FireMesh.rand(seed, k + 99) < 0.3, seed + k * 17 + born * 5);
        }
    }
}
