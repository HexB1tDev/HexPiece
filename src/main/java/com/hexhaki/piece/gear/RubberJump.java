package com.hexhaki.piece.gear;

/**
 * 3.3.2 (the owner: "shift + space ability is removed, default jumping is much higher and always does the jump animation we're doing,
 * it always turns the ground into rubber for the player using the fruit, basically world of rubber on every jump"; asked, "Gear 5
 * only" and "About 5 blocks"): Gear 5's jump. Pure: how much higher it goes, and how the ground under it moves like rubber, a small
 * Rubber World of its own for a moment.
 *
 * <ul>
 *   <li>{@link #LIFT}: added to the jump's own upward speed (a normal jump's 0.42 becomes 0.9: about 4.9 blocks up, 23 ticks in the
 *       air; a jump boost or a honey block still counts).</li>
 *   <li>{@link #hop}: the ground at the take-off springs up under the feet (a trampoline's rebound) and two rings ripple out round it;
 *       at the landing two rings ripple out round the feet (the footing itself stays still). Only ever up, like the Rubber World's own
 *       ground (gear/RubberWave): client/gear/RubberWorldDraw draws copies of the ground's blocks lifted out of the real ground.</li>
 * </ul>
 * 3.3.8 (the owner: the ripples "should be higher like lift more"; asked, "Both" and "About 2x"): the take-off's spring and every ring
 * twice as high (a landing's first ring about 1.6 blocks), and a landing after any fall of {@link #FALL_MIN} blocks or more ripples too
 * (server/NikaJump, client/gear/NikaJumpClient: "every fall should make the floor ripple", the Bajrang Gun's fall included).
 */
public final class RubberJump {
    /** A fall from at least this high above the landing (blocks) ripples the ground round the feet. */
    public static final double FALL_MIN = 2.0;
    /** The upward speed added to a Gear 5 jump (blocks a tick). */
    public static final double LIFT = 0.48;
    /** How far the ripples reach at the take-off and at the landing (blocks). */
    public static final double HOP_RADIUS = 5.5;
    public static final double LAND_RADIUS = 4.5;
    /** How long either lasts (ticks): every ring has left its field by then. */
    public static final double LIFE = 18;
    /** The rebound under the feet at the take-off: how high, how long, how wide. */
    static final double SPRING = 1.1;
    static final double SPRING_TICKS = 3.0;
    static final double SPRING_WIDE = 1.7;
    /** The rings: where they start, how fast they go out (blocks a tick), half their width, when each starts, how high. */
    static final double RING_FROM = 0.7;
    static final double RING_SPEED = 0.55;
    static final double RING_WIDTH = 1.3;
    static final double[] HOP_AT = {0.5, 5};
    static final double[] HOP_AMP = {1.4, 0.7};
    static final double[] LAND_AT = {0, 4};
    static final double[] LAND_AMP = {1.6, 0.8};

    private RubberJump() {
    }

    /**
     * How high the ground rises {@code r} blocks from the jump's middle at {@code angle} (radians round it), {@code t} ticks after the
     * take-off (or the landing, {@code landing} true).
     */
    public static double hop(double r, double angle, double t, boolean landing) {
        return hop(r, angle, t, landing, 1.0);
    }

    /**
     * 3.4.9 (the owner, on the giant: "every jumping animation, every ground rippling, everything should just be the same except the
     * player's much bigger"): the ripples of a body {@code size} times the normal one (Giant's 4). The rings reach {@code size} times as
     * far round (a 4x giant's landing rings run out 18 blocks, its take-off's 22) and their thickness, height and pace grow by its square
     * root (twice as thick and high, half as fast: bigger waves are slower), so it reads as the normal ripple made big while a giant's
     * ripple costs at most about 900 blocks drawn a frame (all four times over would be 2,100). Size 1 is the normal ripple exactly.
     */
    public static double hop(double r, double angle, double t, boolean landing, double size) {
        double s = Math.max(1.0, size);
        double k = Math.sqrt(s);
        double radius = (landing ? LAND_RADIUS : HOP_RADIUS) * s;
        double u = t / k;
        if (u < 0 || u > LIFE || r > radius) {
            return 0;
        }
        double edge = Math.min(1, (radius - r) / (1.5 * k));
        // slime, not a ripple tank: the rings wobble round the circle
        double wobble = 0.8 + 0.2 * Math.sin(3 * angle + u * 0.3);
        double h = 0;
        double[] at = landing ? LAND_AT : HOP_AT;
        double[] amp = landing ? LAND_AMP : HOP_AMP;
        double width = RING_WIDTH * k;
        for (int i = 0; i < at.length; i++) {
            double front = (RING_FROM + RING_SPEED * (u - at[i])) * s;
            double d = r - front;
            if (u >= at[i] && Math.abs(d) < width) {
                double c = Math.cos(Math.PI * d / (2 * width));
                // lower as it spreads
                double spread = Math.max(0, 1 - front / (radius + s));
                h += amp[i] * k * c * c * spread * wobble;
            }
        }
        if (landing) {
            // the feet's own footing stays still: the rings rise round it
            h *= Math.min(1, Math.max(0, (r - 0.8 * s) / s));
        } else if (u < SPRING_TICKS && r < SPRING_WIDE * k) {
            // the take-off: the ground under the feet springs up after them
            double c = Math.cos(Math.PI * r / (2 * SPRING_WIDE * k));
            h += SPRING * k * Math.sin(Math.PI * u / SPRING_TICKS) * c * c;
        }
        return Math.max(0, h * edge);
    }

    /** How steeply it rises outward there (blocks up a block out), for tilting the blocks with it. */
    public static double slope(double r, double angle, double t, boolean landing) {
        return slope(r, angle, t, landing, 1.0);
    }

    /** {@link #slope} of a body {@code size} times the normal one. */
    public static double slope(double r, double angle, double t, boolean landing, double size) {
        return (hop(r + 0.5, angle, t, landing, size) - hop(Math.max(0, r - 0.5), angle, t, landing, size)) / (r >= 0.5 ? 1.0 : 0.5 + r);
    }

    /** How long a ripple of a body {@code size} times the normal one lasts (ticks). */
    public static double life(double size) {
        return LIFE * Math.sqrt(Math.max(1.0, size));
    }

    /** A jump's upward speed in Gear 5: the jump's own (honey, a jump boost) plus {@link #LIFT}; a jump going nowhere up is left alone. */
    public static double lifted(double vy) {
        return vy > 0 ? vy + LIFT : vy;
    }
}
