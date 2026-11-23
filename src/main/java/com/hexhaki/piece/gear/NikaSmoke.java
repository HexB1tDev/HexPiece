package com.hexhaki.piece.gear;

import java.util.Random;

/**
 * 3.4.9 (the owner: "the white aura around the player, it needs to be replaced with like some kind of like photon smoke. Because this
 * aura that is around the player, the white one, and I don't mean the clouds, just mean the outline that the player has, doesn't adapt
 * well to animations and it's going to generate more problems in the future"): Gear 5's aura as smoke. The original's GearAuraLayer drew
 * the whole body twice more, scaled up round the chest, in a glow texture: a stretched or swung limb poked out of it and the shells slid
 * off the moving parts. Now white wisps of steam and soft puffs rise off the body (HexPiece's Photon particles, client/gear/NikaSmokeFx):
 * free particles, so they suit every pose, stretch and size, and never need to match the model.
 *
 * <p>Pure: where each wisp starts round the body, how it moves, grows and fades ({@link #spawn}); the game and the preview
 * (tools/preview/NikaShots {@code |nsmoke}) use the very same numbers.
 */
public final class NikaSmoke {
    /** Wisps a tick off a normal-sized body at full presence (the player's particle setting scales it). */
    public static final double RATE = 2.6;
    /** At most this many times a normal body's wisps (the giant is 4 times the size, its wisps 4 times as big, but not 4 times as many). */
    public static final double MOST_RATE = 2.6;
    /** How long a wisp lives (ticks): LIFE plus up to LIFE_SPREAD. */
    public static final int LIFE = 16;
    public static final int LIFE_SPREAD = 10;
    /** The body (a normal player's, blocks): its height, half its width across the shoulders, half its depth. */
    static final double HEIGHT = 1.8;
    static final double HALF_WIDE = 0.42;
    static final double HALF_DEEP = 0.26;
    /** How it rises (blocks a tick), how much it drifts out, its sizes (blocks) and how fast it slows. */
    static final double RISE = 0.03;
    static final double RISE_SPREAD = 0.03;
    static final double OUT = 0.012;
    static final double SIZE_FROM = 0.34;
    static final double SIZE_TO = 0.95;
    public static final double DRAG = 0.95;
    public static final double WANDER = 0.006;
    /** Start and end colours (ARGB): white steam at about a third, fading to clear with a faint cool tint. */
    public static final int FROM = 0x8CFFFFFF;
    public static final int FROM_PUFF = 0x5CFFFFFF;
    public static final int TO = 0x00EEF0FF;
    /** One wisp in {@code PUFF_EVERY} is a soft round puff (the rest wispy steam). */
    static final int PUFF_EVERY = 4;

    /** A wisp's start: {@link #X} {@link #Y} {@link #Z} from the feet, its velocity, sizes, life and whether it is a puff. */
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public static final int VX = 3;
    public static final int VY = 4;
    public static final int VZ = 5;
    public static final int SIZE0 = 6;
    public static final int SIZE1 = 7;
    public static final int LIFETIME = 8;
    public static final int PUFF = 9;
    public static final int FIELDS = 10;

    private NikaSmoke() {
    }

    /** How many wisps a tick a body {@code scale} times the normal one gives off at {@code presence} 0..1 (before the particle setting). */
    public static double rate(double scale, double presence) {
        return RATE * Math.max(0.0, Math.min(1.0, presence)) * Math.min(MOST_RATE, Math.max(0.6, scale));
    }

    /**
     * Fills {@code out} ({@link #FIELDS} long) with one wisp off a body {@code scale} times the normal one, turned {@code yawDegrees}
     * (the body's own yaw, Minecraft's: 0 faces +z): it starts on the body's outline (a rounded box round the torso, the arms' and legs'
     * sides, the head's top), mostly round the chest and shoulders, and rises and drifts outward, growing.
     */
    public static void spawn(double scale, double yawDegrees, Random random, double[] out) {
        double s = Math.max(0.2, scale);
        // height: more round the chest and the head than at the shins
        double u = random.nextDouble();
        double h = HEIGHT * (0.12 + 0.88 * Math.sqrt(u));
        double a = random.nextDouble() * Math.PI * 2.0;
        double across = Math.cos(a) * HALF_WIDE;
        double deep = Math.sin(a) * HALF_DEEP;
        if (h > 1.45) {
            // the head: narrower
            across *= 0.75;
            deep *= 1.4;
        } else if (h < 0.75) {
            // the legs: narrower
            across *= 0.7;
        }
        double yaw = Math.toRadians(yawDegrees);
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        // the body's right is (-cos, 0, -sin) in Minecraft's turn, its front (-sin, 0, cos)
        double lx = -cos * across - sin * deep;
        double lz = -sin * across + cos * deep;
        double len = Math.max(1.0E-6, Math.sqrt(lx * lx + lz * lz));
        out[X] = lx * s;
        out[Y] = h * s;
        out[Z] = lz * s;
        double grow = Math.sqrt(s);
        double out1 = OUT * grow * (0.5 + random.nextDouble());
        out[VX] = lx / len * out1;
        out[VY] = (RISE + RISE_SPREAD * random.nextDouble()) * grow;
        out[VZ] = lz / len * out1;
        boolean puff = random.nextInt(PUFF_EVERY) == 0;
        double size = puff ? 1.25 : 1.0;
        out[SIZE0] = SIZE_FROM * s * size * (0.8 + 0.4 * random.nextDouble());
        out[SIZE1] = SIZE_TO * s * size * (0.8 + 0.4 * random.nextDouble());
        out[LIFETIME] = LIFE + random.nextInt(LIFE_SPREAD + 1);
        out[PUFF] = puff ? 1 : 0;
    }
}
