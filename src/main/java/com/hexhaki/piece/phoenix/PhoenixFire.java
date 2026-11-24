package com.hexhaki.piece.phoenix;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.mochi.MochiArt;

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
        MochiArt.tubeBegin();
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
            MochiArt.tubePoint(px, py, pz, Math.max(0.004, r));
        }
        MochiArt.tube(sink, 6, gold ? GOLD_TUBE : CYAN_TUBE, 0.0, 0.0);
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
     * 3.7.0 (the owner: "Transformation animation looks bad improve it so the players body slowly becomes the phoenix, arms become the
     * wings and etc."): the change round the player standing at (x, y, z) (their feet, eye-relative), their body turned to
     * {@code bodyYaw} (the game's degrees), {@code ticks} into {@code phase}: the human turned to a figure of blue fire
     * ({@link PhoenixForm#burning}: the fire running up the arms from the hands, raised out as wings ({@link PhoenixForm#armRaise}),
     * then over the body from the shoulders down, the legs, the head), tongues licking up off every burning part, and a burst of
     * tongues out of the chest as the phoenix opens out of the figure (and as it closes back into it going). Banded fire tubes, as the
     * house's tongues: alpha blended quads.
     */
    public static void change(FireMesh.Sink sink, double x, double y, double z, float bodyYaw, int phase, double ticks, int seed) {
        PhoenixForm.burning(phase, (float) ticks, BURN);
        double arms = BURN[0];
        double body = BURN[1];
        double left = BURN[2];
        if (left > 0.01 && (arms > 0.01 || body > 0.01)) {
            human(sink, x, y, z, bodyYaw, PhoenixForm.armRaise(phase, (float) ticks), arms, body, left, ticks, seed);
        }
        // the phoenix opening out of the figure (forming), closing back into it (going): a burst of tongues out of the chest
        double at = phase == PhoenixForm.FORMING ? PhoenixForm.BIRD_IN - 1.0 : phase == PhoenixForm.UNFORMING ? PhoenixForm.BIRD_GONE - 4.0 : -99.0;
        double f = (ticks - at) / 9.0;
        if (f > 0.0 && f < 1.0) {
            flare(sink, x, y + 1.15, z, Math.sin(Math.PI * f), ticks, seed + 9);
        }
    }

    private static final float[] BURN = new float[3];

    /** The figure of fire's limbs' size (blocks): the arm, the leg and the torso's halves (round, a little over the skin's). */
    static final double ARM_R = 0.2;
    static final double LEG_R = 0.18;
    static final double TORSO_R = 0.2;
    static final double HEAD_R = 0.34;

    /**
     * The human as a figure of fire: {@code arms} of the arms burning from the hands up, {@code body} of the rest from the shoulders
     * down (the torso, then the legs, the head), {@code left} of it left (its limbs thinning away to nothing as it burns away), the arms
     * raised {@code raise} degrees out to the sides (the player model's: the shoulders 22 pixels up, 5 out; the arms 10 long below).
     */
    static void human(FireMesh.Sink sink, double x, double y, double z, float bodyYaw, double raise, double arms, double body, double left,
                      double ticks, int seed) {
        double yaw = Math.toRadians(bodyYaw);
        // the player's right (their right hand's side) and forward
        double rx = -Math.cos(yaw);
        double rz = -Math.sin(yaw);
        double r = Math.toRadians(raise);
        int k = 0;
        for (int side = 0; side < 2; side++) {
            double sgn = side == 0 ? 1.0 : -1.0;
            double sx = x + rx * sgn * 0.33;
            double sy = y + 1.375;
            double sz = z + rz * sgn * 0.33;
            // the arm's way: down turned out to its side by the raise
            double dx = rx * sgn * Math.sin(r);
            double dy = -Math.cos(r);
            double dz = rz * sgn * Math.sin(r);
            if (arms > 0.01) {
                // burning from the hand (0.65 out) back to over the shoulder's top (0.12 behind the pivot)
                double hand = 0.65;
                double reach = hand - (hand + 0.12) * arms;
                limb(sink, sx + dx * hand, sy + dy * hand, sz + dz * hand, sx + dx * reach, sy + dy * reach, sz + dz * reach, ARM_R * left,
                    ticks, seed + side);
                for (int t = 0; t < 4; t++) {
                    double u = hand - (hand - reach) * (t + 0.5) / 4.0;
                    lick(sink, sx + dx * u, sy + dy * u + ARM_R * 0.6, sz + dz * u, 0.7 * left, ticks, seed + 40 + k++);
                }
            }
            if (body > 0.45) {
                // the legs, from the hips down
                double legs = (body - 0.45) / 0.55;
                double lx = x + rx * sgn * 0.12;
                double lz = z + rz * sgn * 0.12;
                limb(sink, lx, y + 0.75, lz, lx, y + 0.75 - 0.75 * legs, lz, LEG_R * left, ticks, seed + 4 + side);
                lick(sink, lx, y + 0.75 - 0.6 * legs, lz, 0.45 * left, ticks, seed + 60 + side);
            }
            if (body > 0.01) {
                // the torso's half, from the shoulders down
                double tx = x + rx * sgn * 0.12;
                double tz = z + rz * sgn * 0.12;
                limb(sink, tx, y + 1.5, tz, tx, y + 1.5 - 0.75 * Math.min(1.0, body / 0.6), tz, TORSO_R * left, ticks, seed + 8 + side);
                lick(sink, tx, y + 1.45 - 0.5 * Math.min(1.0, body / 0.6), tz, 0.6 * left, ticks, seed + 70 + side);
                lick(sink, tx + rx * sgn * 0.12, y + 1.5, tz + rz * sgn * 0.12, 0.65 * left, ticks, seed + 74 + side);
            }
        }
        if (body > 0.3) {
            // the head, last
            double head = Math.min(1.0, (body - 0.3) / 0.4);
            double hr = HEAD_R * head * left;
            MochiArt.tubeBegin();
            MochiArt.tubePoint(x, y + 1.75 - hr, z, hr * 0.5);
            MochiArt.tubePoint(x, y + 1.75 - hr * 0.5, z, hr * 0.9);
            MochiArt.tubePoint(x, y + 1.75, z, hr * (1.0 + 0.08 * Math.sin(ticks * 1.1 + seed)));
            MochiArt.tubePoint(x, y + 1.75 + hr * 0.5, z, hr * 0.85);
            MochiArt.tubePoint(x, y + 1.75 + hr * 1.1, z, hr * 0.2);
            MochiArt.tube(sink, 8, CYAN_TUBE, 1.2, ticks * 0.25);
            lick(sink, x, y + 1.75 + hr * 0.6, z, 0.7 * head * left, ticks, seed + 80);
        }
    }

    /** One burning limb from (ax, ay, az) to (bx, by, bz): a banded tube of fire, its surface swelling and licking up as it burns. */
    static void limb(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double r, double ticks, int seed) {
        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        if (r < 0.01 || dx * dx + dy * dy + dz * dz < 1.0E-4) {
            return;
        }
        MochiArt.tubeBegin();
        int n = 7;
        for (int i = 0; i <= n; i++) {
            double t = i / (double) n;
            // the ends rounded, a swell running along it (the fire licking up it)
            double end = Math.sqrt(Math.max(0.0, Math.sin(Math.PI * Math.min(1.0, 0.08 + t * 0.92))));
            double swell = 1.0 + 0.16 * Math.sin(t * 7.0 - ticks * 0.9 + seed) + 0.08 * Math.sin(t * 13.0 + ticks * 1.7 + seed * 2);
            MochiArt.tubePoint(ax + dx * t, ay + dy * t, az + dz * t, r * Math.max(0.35, end) * swell);
        }
        // the flames swirling along it (stripes of deep blue winding round it, turning)
        MochiArt.tube(sink, 8, CYAN_TUBE, 1.6, ticks * 0.3 + seed);
    }

    /** A tongue licking up off a burning part at (x, y, z), {@code size} long, its life running on with the clock. */
    static void lick(FireMesh.Sink sink, double x, double y, double z, double size, double ticks, int seed) {
        if (size < 0.05) {
            return;
        }
        double life = 7.0 + 3.0 * FireMesh.rand(seed, 1);
        double start = life * FireMesh.rand(seed, 2);
        tongue(sink, x, y, z, 0.0, 0.0, 0.0, (ticks + start) % life, life, size, FireMesh.rand(seed, 3) < 0.25,
            seed * 7 + (int) ((ticks + start) / life));
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
     * 3.7.0 (the owner: "it should also glow"): the phoenix's glow, soft cyan light round its body and along its wings ({@code points}:
     * eye-relative, {@code count} of them, each {@code size} across), clear at its edge: it never hides the body, only haloes it.
     */
    public static void glow(FireMesh.Sink sink, double[] points, int count, double size, double ticks) {
        for (int k = 0; k < count; k++) {
            double pulse = 1.0 + 0.12 * Math.sin(ticks * 0.35 + k * 1.7);
            FireMesh.shell(sink, points[k * 3], points[k * 3 + 1], points[k * 3 + 2], size * pulse, 0x46DDF5, 0.16, 0.0);
        }
    }
}
