package com.hexhaki.piece.gear;

/**
 * 3.3.0 (the owner: "Rubber world, we need the world to actually physically bounce like rubber ... the ground itself be manipulated so
 * that it behaves like it's made of like pure slime, sort of, as in like actually wave and all, like bend to the user's will"; the
 * reference: a tiny Gear 5 Luffy jumping over ground rolling in soft rubbery waves): how high the ground of a Rubber World field rises
 * at a place and a moment. Pure; client/gear/RubberWorldDraw draws the ground's own blocks lifted and tilted by it, and lifts the bodies
 * standing in it.
 *
 * <ul>
 *   <li>The release: three rings of ground swell out from round the user's feet one after another, each its own size and speed.</li>
 *   <li>Then, while the field lasts, a wave is born round the user every {@link #WAVE_EVERY} ticks or so and rolls slowly outward, each
 *       its own height ({@link #WAVE_LOW} to {@link #WAVE_HIGH}), width and speed: some swells much bigger than others, wobbling round
 *       the circle so the ground heaves like slime rather than a ripple tank; when the field ends they settle within {@link #FADE}
 *       ticks.</li>
 * </ul>
 * 3.3.3 (the owner: "Rubber world's rippes should be different sizes some much bigger than others, and slower, they're too fast, going
 * way too fast. they're meant to be like slow waves"): 3.3.0's rings went out at 0.75 blocks a tick and its waves at 0.42, all one
 * height; now the rings go 0.24 to 0.3 and the waves 0.12 to 0.2 (2.4 to 4 blocks a second), each its own size.
 * 3.3.8 (the owner: "Rippes from world of rubber should be higher like lift more"; asked, "About 2x"): every ring and wave twice as
 * high (the biggest swells about 3.8 blocks, the smallest 0.7), at the same widths and speeds.
 * The ground only ever rises (0 to {@link #MOST} blocks): drawn copies of its blocks lift out of it, the real ground stays.
 */
public final class RubberWave {
    /** The release's rings: when each starts, how high, half their width, how fast they go out (blocks a tick). */
    static final double[] RING_AT = {0, 14, 30};
    static final double[] RING_AMP = {3.0, 1.2, 2.2};
    static final double[] RING_HALF = {3.0, 2.0, 2.6};
    static final double[] RING_SPEED = {0.3, 0.24, 0.27};
    /** Where the rings and waves start (blocks from the middle): round the user, not under their feet. */
    static final double RING_FROM = 1.5;
    /** The waves: the first born at {@link #WAVE_FROM}, then one about every {@link #WAVE_EVERY} ticks; their heights, half widths and
     * speeds between these (each wave its own, the same on the server and every client). */
    public static final double WAVE_FROM = 40.0;
    public static final double WAVE_EVERY = 45.0;
    public static final double WAVE_LOW = 0.7;
    public static final double WAVE_HIGH = 3.8;
    static final double HALF_LOW = 1.8;
    static final double HALF_HIGH = 3.6;
    public static final double SPEED_LOW = 0.12;
    public static final double SPEED_HIGH = 0.2;
    /** How long the ground takes to settle after the field ends. */
    public static final double FADE = 20.0;
    /** The highest it ever rises (3.3.8: four blocks; the drawn top block and the one under it, stretched down to the real ground). */
    public static final double MOST = 4.0;
    /** The rim where it flattens to the field's edge. */
    static final double EDGE = 5.0;

    private RubberWave() {
    }

    /** A wave's own number in 0 to 1, the same everywhere (its {@code k}-th draw). */
    static double pick(int wave, int k) {
        double v = Math.sin(wave * 12.9898 + k * 78.233) * 43758.5453;
        return v - Math.floor(v);
    }

    /** Wave {@code i}'s height: mostly middling, every few a much bigger swell. */
    public static double waveHeight(int i) {
        double p = pick(i, 0);
        return WAVE_LOW + (WAVE_HIGH - WAVE_LOW) * p * p;
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
            double front = RING_FROM + RING_SPEED[i] * (t - RING_AT[i]);
            double d = r - front;
            if (t >= RING_AT[i] && Math.abs(d) < RING_HALF[i] && front < radius + RING_HALF[i]) {
                double c = Math.cos(Math.PI * d / (2 * RING_HALF[i]));
                h += RING_AMP[i] * c * c * fall * hollow;
            }
        }
        if (t > WAVE_FROM && r > RING_FROM) {
            double wobble = 0.72 + 0.28 * Math.sin(3 * angle + t * 0.05) * Math.cos(2 * angle - t * 0.03);
            double near = Math.min(1, (r - RING_FROM) / 3);
            int born = (int) Math.floor((Math.min(t, end) - WAVE_FROM) / WAVE_EVERY);
            for (int i = 0; i <= born; i++) {
                double at = WAVE_FROM + WAVE_EVERY * (i + 0.6 * (pick(i, 1) - 0.5));
                if (t < at) {
                    continue;
                }
                double half = HALF_LOW + (HALF_HIGH - HALF_LOW) * pick(i, 2);
                double speed = SPEED_LOW + (SPEED_HIGH - SPEED_LOW) * pick(i, 3);
                double front = RING_FROM + speed * (t - at);
                double d = r - front;
                if (Math.abs(d) < half && front < radius + half) {
                    // a swell rising out of the user's feet as it is born (over 10 ticks), then rolling out
                    double born10 = Math.min(1, (t - at) / 10);
                    double c = Math.cos(Math.PI * d / (2 * half));
                    h += waveHeight(i) * born10 * c * c * fall * wobble * near;
                }
            }
        }
        return Math.min(MOST, h * edge * Math.max(0, settle));
    }

    /**
     * 3.3.2 (the owner: Rubber World "makes it so for everyone else, it's main goal is to confuse the target, displace them and
     * overwhelm"): a body standing where the ground rises at least this much in a tick is thrown by it (server/RubberChaos).
     */
    public static final double TOSS_RISE = 0.04;
    /** Ticks between two throws of one body (it lands and the next crest comes). */
    public static final int TOSS_GAP = 6;

    /** How hard a crest rising {@code dh} blocks this tick throws what stands on it: 0 (none) to 1. */
    public static double toss(double dh) {
        return dh < TOSS_RISE ? 0 : Math.min(1, 0.35 + (dh - TOSS_RISE) * 3.0);
    }

    /**
     * A throw of strength {@code s}: up (blocks a tick), outward from the field's middle (thrown back in, {@link #BACK} times as hard,
     * one throw in {@link #BACK_CHANCE}^-1: they tumble about in the field instead of being swept out of it), and sideways at most (its
     * sign random).
     */
    public static double tossUp(double s) {
        return 0.5 + 0.45 * s;
    }

    public static double tossOut(double s) {
        return 0.1 + 0.2 * s;
    }

    public static double tossSide(double s) {
        return 0.2 + 0.3 * s;
    }

    public static final double BACK = 0.8;
    public static final double BACK_CHANCE = 0.35;

    /** The share of the slope the drawn blocks tilt by, and the most they ever tilt (3.3.8: twice as high is twice as steep; held to
     * 3.3.3's steepest, about 35 degrees, so a tall column leans, never topples). */
    static final double TILT = 0.6;
    static final double TILT_MOST = 0.61;

    /** How far a risen block tilts (radians) on a slope of {@code slope} blocks up a block out (Rubber World's and the jumps'). */
    public static double tilt(double slope) {
        double a = Math.atan(slope) * TILT;
        return Math.max(-TILT_MOST, Math.min(TILT_MOST, a));
    }

    /**
     * 3.3.8: how tall the block under a column risen {@code h} is drawn (client/gear/RubberWorldDraw: one block, or stretched like rubber
     * down to a quarter into the real ground: the top block's copy sits {@code h} over the real top, so the gap under it is {@code h - 1}).
     */
    public static double underTall(double h) {
        return Math.max(1.0, h - 0.75);
    }

    /** How steeply it rises outward there (blocks up a block out), for tilting the blocks with the wave. */
    public static double slope(double r, double angle, double t, double radius, double end) {
        return (rise(r + 0.5, angle, t, radius, end) - rise(Math.max(0, r - 0.5), angle, t, radius, end)) / (r >= 0.5 ? 1.0 : 0.5 + r);
    }
}
