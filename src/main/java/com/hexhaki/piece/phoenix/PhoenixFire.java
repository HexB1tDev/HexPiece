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

    /**
     * One tongue of fire at (x, y, z), {@code age} of its {@code life} ticks in: rising {@link PhoenixForm#TONGUE_RISE} over its life,
     * carried by the wind (wx, wy, wz: blocks a tick), swelling and shrinking away, its heart cooling as it goes; {@code size} its
     * widest (blocks); gold or cyan.
     */
    public static void tongue(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double age, double life,
                              double size, boolean gold, int seed) {
        if (age < 0 || age > life) {
            return;
        }
        double f = age / life;
        double px = x + wx * age;
        double py = y + PhoenixForm.TONGUE_RISE * f * (0.6 + 0.4 * f) + wy * age;
        double pz = z + wz * age;
        double r = size * Math.sin(Math.PI * Math.min(1.0, f * 1.25 + 0.08)) * (1.0 - 0.35 * f);
        if (r < 0.02) {
            return;
        }
        // stretched up and back along the way it streams: a tongue, not a ball
        double vx = -wx;
        double vy = -0.9;
        double vz = -wz;
        FireMesh.palette(gold ? GOLD : CYAN);
        FireMesh.puff(sink, px, py, pz, r, vx, vy - wy, vz, 1.7, 0.9, 0.78 - 0.32 * f, 0.55, false, 0.92 * (1.0 - f * f), age, seed);
        FireMesh.palette(FireMesh.BAND);
    }

    /**
     * The tongues streaming off the whole body: the skinned fire points {@code points} (x, y, z eye-relative, one per anchor), the clock
     * {@code ticks}, the wind (the body's own motion reversed, blocks a tick), the phoenix's scale (its tongues grow with it as it forms).
     */
    public static void burn(FireMesh.Sink sink, double[] points, int count, double ticks, double wx, double wy, double wz, double scale, int seed) {
        for (int k = 0; k < count; k++) {
            double life = PhoenixForm.TONGUE_LIFE + 5.0 * FireMesh.rand(seed, k);
            double age = (ticks + life * FireMesh.rand(seed, k + 100)) % life;
            boolean gold = FireMesh.rand(seed, k + 200) < 0.28;
            double size = (0.16 + 0.16 * FireMesh.rand(seed, k + 300)) * scale;
            // each life its own puff (a new seed when it is born again)
            int born = (int) Math.floor((ticks + life * FireMesh.rand(seed, k + 100)) / life);
            tongue(sink, points[k * 3], points[k * 3 + 1], points[k * 3 + 2], wx, wy, wz, age, life, size, gold, seed + k * 31 + born * 7);
        }
    }
}
