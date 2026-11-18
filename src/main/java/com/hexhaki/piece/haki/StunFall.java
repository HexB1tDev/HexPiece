package com.hexhaki.piece.haki;

/**
 * 2.4.4: a body a Haki stun lands on falls down (pure, tested in {@code LightningTests.stunFall}; {@code server/HakiFalls} starts it,
 * {@code client/fx/Tumbles} draws it). The owner: "whenever haki stun lands on the entity or player, no matter if it's modded or not, I
 * want that entity to ragdoll and like fall to a random side, usually like opposite where the lightning hit the entity, like just a
 * small ... just a very small like fall backwards type thing and like keep them stunned. Like instead of the current animation, that
 * makes them like just like look up weird. Make them like actually fall down, like, and stay stunned, like for a little while" and "this
 * should affect literally all entities".
 *
 * <p>The original knocked a stunned body down by forcing the swimming pose on it, which on anything but a player only tips its head back
 * (the "look up weird"), and played a player its knockdown animations. Now every body goes over the same way, whatever its model: the
 * whole of it tips over from its feet away from the Haki user ({@link #direction}: mostly straight away, now and then to one side),
 * falling faster as it goes ({@link #FALL_TICKS}) and sliding back a little ({@link #SLIDE}), thumps down nearly flat ({@link #LIE}) with
 * a small bounce, lies there while it is stunned (at least {@link #MIN_DOWN} ticks: {@link #down}) with its head lolled to one side and
 * the odd twitch, then gets back up ({@link #GETUP_TICKS}).
 */
public final class StunFall {
    /** How long the fall takes, the bounce after it, getting up (ticks). */
    public static final int FALL_TICKS = 7;
    public static final int SETTLE_TICKS = 5;
    public static final int GETUP_TICKS = 12;
    /** It stays down at least this long (ticks), however short the stun that knocked it down. */
    public static final int MIN_DOWN = 40;
    /** How far over it lies (radians from upright), how far it bounces back up on landing, how far it slides back (blocks). */
    public static final double LIE = Math.toRadians(84.0);
    public static final double BOUNCE = 0.16;
    public static final double SLIDE = 0.25;
    /** How far its head lolls to one side lying there (radians), how far a twitch moves it (radians). */
    public static final double HEAD_LOLL = 0.5;
    public static final double TWITCH = 0.05;
    /** Most falls go this far either side of straight away from the Haki user (degrees); one in {@link #SIDEWAYS_ONE_IN} goes to a side. */
    public static final double SPREAD = 35.0;
    public static final int SIDEWAYS_ONE_IN = 4;

    /** One moment of a fall: how far over (radians), how far slid back (blocks), the head's loll (radians, + to the right), how flat. */
    public record Frame(double angle, double slide, double head, double flat) {
        public static final Frame NONE = new Frame(0.0, 0.0, 0.0, 0.0);
    }

    private StunFall() {
    }

    /** How long a body knocked down by a stun of {@code ticks} stays down (ticks). */
    public static int down(int ticks) {
        return Math.max(MIN_DOWN, ticks);
    }

    /**
     * The way a body falls (a unit vector on the ground, x and z) given the way away from the Haki user ({@code awayX}, {@code awayZ}; zero
     * for none known: a random way) and two random draws (0 to 1): mostly within {@link #SPREAD} of straight away, one in
     * {@link #SIDEWAYS_ONE_IN} to one side.
     */
    public static double[] direction(double awayX, double awayZ, double roll, double roll2) {
        double length = Math.hypot(awayX, awayZ);
        double base = length < 1.0E-6 ? roll2 * Math.PI * 2.0 : Math.atan2(awayZ, awayX);
        double turn;
        if (length >= 1.0E-6 && (int) Math.floor(roll * SIDEWAYS_ONE_IN) == 0) {
            // to one side: a quarter turn either way, give or take 20 degrees
            turn = Math.toRadians((roll2 < 0.5 ? -1.0 : 1.0) * (70.0 + 40.0 * Math.abs(roll2 - 0.5) * 2.0));
        } else {
            turn = Math.toRadians((roll2 * 2.0 - 1.0) * SPREAD);
        }
        double a = base + turn;
        return new double[] {Math.cos(a), Math.sin(a)};
    }

    /** {@code t} ticks after the stun landed, the body down for {@code down} ticks, {@code seed} its own (which way the head lolls, twitches). */
    public static Frame at(double t, int down, long seed) {
        if (t < 0.0 || over(t, down)) {
            return Frame.NONE;
        }
        double side = (seed & 1L) == 0 ? 1.0 : -1.0;
        double angle;
        double slide;
        if (t < FALL_TICKS) {
            // over faster and faster, as a body falls
            double u = t / FALL_TICKS;
            angle = LIE * u * u;
            slide = SLIDE * smooth(u);
        } else if (t < FALL_TICKS + SETTLE_TICKS) {
            // the thump: a small bounce back up off the ground, settling
            double s = (t - FALL_TICKS) / SETTLE_TICKS;
            angle = LIE - BOUNCE * Math.sin(Math.PI * s) * (1.0 - s);
            slide = SLIDE;
        } else {
            angle = LIE + twitch(t, seed);
            slide = SLIDE;
        }
        double up = t > down ? smooth((t - down) / GETUP_TICKS) : 0.0;
        angle *= 1.0 - up;
        slide *= 1.0 - up;
        double head = HEAD_LOLL * side * smooth(Math.min(1.0, t / FALL_TICKS)) * (1.0 - up);
        return new Frame(angle, slide, head, Math.abs(Math.sin(angle)));
    }

    /** True once it is back on its feet. */
    public static boolean over(double t, int down) {
        return t >= down + GETUP_TICKS;
    }

    /** A twitch now and then while it lies there: a short jerk every 14 to 22 ticks, its own. */
    static double twitch(double t, long seed) {
        double every = 14.0 + (Math.floorMod(seed >> 3, 9L));
        double phase = Math.floorMod(seed >> 7, 13L);
        double local = (t + phase) % every;
        return local < 3.0 ? TWITCH * Math.sin(Math.PI * local / 3.0) : 0.0;
    }

    static double smooth(double t) {
        double u = Math.max(0.0, Math.min(1.0, t));
        return u * u * (3.0 - 2.0 * u);
    }
}
