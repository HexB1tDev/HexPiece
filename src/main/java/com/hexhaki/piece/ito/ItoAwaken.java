package com.hexhaki.piece.ito;

/**
 * 3.5.0 (the owner: "the awakening in hockey mode should be different and same for the default awakening ... without hockey, the floor
 * itself should be turned into string-like material and just like spike up and create like little spikes on the floor whenever someone
 * steps in it, like sharp spikes from the floor that like rise up high whenever something steps on it, causing bleed. When hockey's on
 * ... massive tentacles like rise from the floor and from trees, like entire trees. Whenever the player has awakening activated, like it
 * becoming a toggle while hockey's on, and whenever the player like has an hostile entity nearby, the environment will automatically
 * generate massive tentacles of strings with the tip coded in hockey that will like impale, lift the targets up into the sky, and just
 * like throw them afterwards. This should be a visual spectacle"): the Ito fruit's Awakening (M), the numbers the server and every client
 * share. Pure.
 *
 * <ul>
 *   <li>Without Haki: the ground round the user turns to string ({@link #FIELD_RADIUS} blocks, {@link #FIELD_TICKS}); whoever stands on
 *       it is impaled by a spike of string shooting up under them ({@link #SPIKE_HEIGHT} blocks, at most once in {@link #SPIKE_REST}
 *       ticks a body): cut and left bleeding, thrown up.</li>
 *   <li>With Armament (Haki): a toggle (M again: off). While on, every hostile body within {@link #TENTACLE_RANGE} blocks (a monster,
 *       a mob after the user, whoever last hurt the user) draws a huge tentacle of string out of the ground near it, or out of a tree
 *       trunk near it; the tentacle's Haki-black tip impales it ({@link #RISE} ticks), lifts it {@link #LIFT_HEIGHT} blocks into the sky
 *       over {@link #LIFT} ticks, and throws it away from the user; then the tentacle sinks back ({@link #WITHDRAW} ticks). At most
 *       {@link #TENTACLES} at once, a new one every {@link #TENTACLE_GAP} ticks.</li>
 * </ul>
 */
public final class ItoAwaken {
    public static final double FIELD_RADIUS = 14.0;
    public static final int FIELD_TICKS = 300;
    /** How long the string ground takes to spread out and to fade at its end (ticks). */
    public static final double FIELD_SPREAD = 12.0;
    public static final double FIELD_FADE = 20.0;
    public static final int SPIKE_EVERY = 4;
    public static final int SPIKE_REST = 24;
    public static final double SPIKE_HEIGHT = 4.5;
    public static final int SPIKE_LIFE = 16;
    public static final float SPIKE_DAMAGE = 5.0F;

    public static final double TENTACLE_RANGE = 22.0;
    public static final int TENTACLES = 3;
    public static final int TENTACLE_GAP = 14;
    public static final int TENTACLE_REST = 80;
    public static final int RISE = 8;
    public static final int LIFT = 22;
    public static final int WITHDRAW = 12;
    public static final double LIFT_HEIGHT = 12.0;
    public static final float TENTACLE_DAMAGE = 10.0F;
    /** How far from a body (blocks) a tree trunk may be to throw the tentacle; the ground's root otherwise this far from it. */
    public static final int TREE_REACH = 6;
    public static final double GROUND_ROOT = 3.5;
    /**
     * 3.5.1: the tentacle is massive now (ItoArt.tentacle: a giant rope 1.6 blocks across at its foot): out of the ground toward the user
     * only while the user is at least this far (flat) from the body; nearer, it bursts out beside the body instead, never under the user.
     */
    public static final double ROOT_CLEAR = 7.0;
    /** The longest the Haki Awakening stays on (a safety: M again ends it). */
    public static final int TOGGLE_MOST = 6000;

    private ItoAwaken() {
    }

    /**
     * 3.5.1: where out of the ground a tentacle bursts, from the body it takes: (ux, uz) the flat way from the body to the user and
     * {@code far} how far the user is (flat); {@code side} 1 or -1. {@link #GROUND_ROOT} toward the user while they are {@link #ROOT_CLEAR}
     * or more away, else as far to the side (square to that way): {x, z} from the body.
     */
    public static double[] root(double ux, double uz, double far, int side) {
        if (far >= ROOT_CLEAR) {
            return new double[] {ux * GROUND_ROOT, uz * GROUND_ROOT};
        }
        return new double[] {-uz * side * GROUND_ROOT, ux * side * GROUND_ROOT};
    }

    /** A tentacle's whole life (ticks). */
    public static int tentacleLife() {
        return RISE + LIFT + WITHDRAW;
    }

    /** How far a spike stands up {@code age} ticks after it shot up (0 to 1): up in 3 with an overshoot, held, down from 8 to the end. */
    public static double spike(double age) {
        if (age < 0 || age > SPIKE_LIFE) {
            return 0.0;
        }
        if (age < 3.0) {
            double u = age / 3.0;
            double c = 1.7;
            return 1.0 + (c + 1) * Math.pow(u - 1, 3) + c * Math.pow(u - 1, 2);
        }
        if (age < 8.0) {
            return 1.0;
        }
        double u = (age - 8.0) / (SPIKE_LIFE - 8.0);
        return 1.0 - u * u * (3 - 2 * u);
    }

    /** How far the tentacle has grown toward the body {@code age} ticks after it began (0 to 1, eased in fast). */
    public static double grow(double age) {
        double u = Math.max(0.0, Math.min(1.0, age / RISE));
        return 1.0 - (1.0 - u) * (1.0 - u) * (1.0 - u);
    }

    /** How high the caught body has been lifted (0 to 1 of {@link #LIFT_HEIGHT}) {@code age} ticks after the tentacle began. */
    public static double lift(double age) {
        double u = Math.max(0.0, Math.min(1.0, (age - RISE) / LIFT));
        return u * u * (3 - 2 * u);
    }

    /** How much of the tentacle is left as it sinks back after the throw (1 to 0). */
    public static double withdraw(double age) {
        double u = Math.max(0.0, Math.min(1.0, (age - RISE - LIFT) / WITHDRAW));
        return 1.0 - u;
    }

    /** Whether a block's name (its registry path) is a tree's trunk: a log, wood, or a nether stem or hyphae (stripped ones too). */
    public static boolean trunk(String path) {
        return path.endsWith("_log") || path.endsWith("_wood") || path.endsWith("_stem") || path.endsWith("_hyphae");
    }
}
