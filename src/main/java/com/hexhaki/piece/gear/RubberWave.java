package com.hexhaki.piece.gear;

/**
 * 3.3.0 (the owner: "Rubber world, we need the world to actually physically bounce like rubber ... the ground itself be manipulated so
 * that it behaves like it's made of like pure slime, sort of, as in like actually wave and all, like bend to the user's will"; the
 * reference: a tiny Gear 5 Luffy jumping over ground rolling in soft rubbery waves): how high the ground of a Rubber World field rises
 * at a place and a moment. Pure; client/gear/RubberWorldDraw draws the ground's own blocks lifted and tilted by it, and lifts the bodies
 * standing in it.
 *
 * <ul>
 *   <li>The release: three rings of ground swell out from round the user's feet one after another ({@link #RING_SPEED} blocks a tick),
 *       each lower than the one before, fading toward the field's edge.</li>
 *   <li>Then, while the field lasts, rounded waves keep rolling outward ({@link #ROLL_LENGTH} apart), wobbling round the circle so the
 *       ground heaves like slime rather than a ripple tank; when the field ends they settle within {@link #FADE} ticks.</li>
 * </ul>
 * The ground only ever rises (0 to {@link #MOST} blocks): drawn copies of its blocks lift out of it, the real ground stays.
 */
public final class RubberWave {
    /** The rings: how fast they go out, half their crest's width, when each starts, how high. */
    public static final double RING_SPEED = 0.75;
    public static final double RING_WIDTH = 2.3;
    static final double[] RING_AT = {0, 9, 18};
    static final double[] RING_AMP = {1.3, 0.9, 0.6};
    /** Where the rings start (blocks from the middle): round the user, not under their feet. */
    static final double RING_FROM = 1.5;
    /** The rolling waves: their length, speed (blocks a tick), height; they grow in from {@link #ROLL_FROM} over {@link #ROLL_IN} ticks. */
    public static final double ROLL_LENGTH = 9.0;
    public static final double ROLL_SPEED = 0.42;
    public static final double ROLL_AMP = 0.6;
    static final double ROLL_FROM = 8.0;
    static final double ROLL_IN = 24.0;
    /** How long the ground takes to settle after the field ends. */
    public static final double FADE = 20.0;
    /** The highest it ever rises. */
    public static final double MOST = 1.6;
    /** The rim where it flattens to the field's edge. */
    static final double EDGE = 5.0;

    private RubberWave() {
    }

    /**
     * How high the ground rises {@code r} blocks from the field's middle at {@code angle} (radians round it), {@code t} ticks after the
     * release, in a field of {@code radius} blocks that ended {@code end} ticks after the release (or still lasting: infinity).
     */
    public static double rise(double r, double angle, double t, double radius, double end) {
        if (t < 0 || r > radius || t > end + FADE) {
            return 0;
        }
        double edge = Math.min(1, (radius - r) / EDGE);
        if (edge <= 0) {
            return 0;
        }
        double settle = t <= end ? 1 : 1 - (t - end) / FADE;
        double h = 0;
        double fall = Math.pow(Math.max(0, 1 - r / (radius + 4)), 0.6);
        // the user's own footing stays still: the rings rise round it
        double hollow = Math.min(1, Math.max(0, (r - 0.9) / 1.4));
        for (int i = 0; i < RING_AT.length; i++) {
            double front = RING_FROM + RING_SPEED * (t - RING_AT[i]);
            double d = r - front;
            if (t >= RING_AT[i] && Math.abs(d) < RING_WIDTH && front < radius) {
                double c = Math.cos(Math.PI * d / (2 * RING_WIDTH));
                h += RING_AMP[i] * c * c * fall * hollow;
            }
        }
        double grow = Math.max(0, Math.min(1, (t - ROLL_FROM) / ROLL_IN));
        if (grow > 0 && r > RING_FROM) {
            double u = r / ROLL_LENGTH - (t - ROLL_FROM) * ROLL_SPEED / ROLL_LENGTH;
            double s = Math.sin(2 * Math.PI * u);
            if (s > 0) {
                double wobble = 0.72 + 0.28 * Math.sin(3 * angle + t * 0.11) * Math.cos(2 * angle - t * 0.07);
                double near = Math.min(1, (r - RING_FROM) / 3);
                h += ROLL_AMP * grow * Math.pow(s, 1.6) * wobble * near;
            }
        }
        return Math.min(MOST, h * edge * Math.max(0, settle));
    }

    /** How steeply it rises outward there (blocks up a block out), for tilting the blocks with the wave. */
    public static double slope(double r, double angle, double t, double radius, double end) {
        return (rise(r + 0.5, angle, t, radius, end) - rise(Math.max(0, r - 0.5), angle, t, radius, end)) / (r >= 0.5 ? 1.0 : 0.5 + r);
    }
}
