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
}
