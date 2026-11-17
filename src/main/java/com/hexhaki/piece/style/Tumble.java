package com.hexhaki.piece.style;

/**
 * 2.3.9: a body knocked off its feet, as every client draws it (pure, tested in {@code StyleTests}): the owner's rag doll, taken from
 * HexGhoul (the owner: "causes knockback and ragdoll (Ragdoll mechanic can be taken from HexGhoul)"; HexGhoul 6.2.0's
 * {@code KickTumble}, the Speed Kick's). Where it flies is the server's launch; this is only how it goes: the head snaps the way the
 * blow drove it on the very frame of it, the body follows a beat later, twisting round and tipping over head first the way it flies,
 * tumbling; it comes down flat with a thump, lies there a moment ({@link #DOWN_TICKS}) and gets up ({@link #GETUP_TICKS}).
 *
 * <p>Angles in radians: {@code head} the head's turn the way it was driven, {@code pitch} its chin thrown up, {@code twist} the whole
 * body's turn about its own length the same way, {@code tumble} its tip over about the level line square to its flight (+ the top goes
 * the way it flies; a quarter turn lies it flat).
 */
public final class Tumble {
    /** How long it lies there once down, and how long it takes to get up (ticks). */
    public static final int DOWN_TICKS = 28;
    public static final int GETUP_TICKS = 12;
    /** How far the head snaps round, and the chin up; how quickly (ticks): it is the first thing to go. */
    public static final double HEAD_SNAP = 1.15;
    static final double HEAD_PITCH = 0.5;
    static final double SNAP_TICKS = 0.7;
    /** How far the body turns after it, how long after, and how quickly. */
    static final double BODY_TURN = 0.95;
    static final double BODY_LAG = 0.35;
    static final double BODY_TICKS = 2.2;
    /** Then on round in the air (radians a tick), and over head first. */
    static final double SPIN = 0.24;
    static final double TUMBLE = 0.42;
    /** The tumble starts after the body has begun to turn, and takes this long (ticks) to get going. */
    static final double TUMBLE_LAG = 0.7;
    static final double TUMBLE_RAMP = 1.4;
    /** Ticks to come down flat once it lands, the bounce in it. */
    public static final double THUMP = 3.0;
    static final double BOUNCE = 0.18;
    /** In the air at most this long (ticks): then it is down wherever it is. */
    public static final double AIRBORNE_MAX = 80.0;

    private Tumble() {
    }

    /** One frame: the head's turn and pitch, the body's twist and tumble, and how flat it lies (0 upright, 1 flat). */
    public record Frame(double head, double pitch, double twist, double tumble, double flat) {
        public static final Frame NONE = new Frame(0.0, 0.0, 0.0, 0.0, 0.0);
    }

    /** {@code t} ticks after the blow; it landed {@code landed} ticks after the blow (NaN: still in the air). */
    public static Frame at(double t, double landed) {
        if (t < 0.0) {
            return Frame.NONE;
        }
        boolean down = !Double.isNaN(landed) && t >= landed;
        double air = down ? landed : t;
        double head = HEAD_SNAP * snap(air / SNAP_TICKS);
        double pitch = HEAD_PITCH * snap(air / SNAP_TICKS);
        double twist = twist(air);
        double tumble = tumble(air);
        if (!down) {
            return new Frame(head, pitch, twist, tumble, Math.abs(Math.sin(tumble)));
        }
        // Down: from however it was turned over, flat (head first the way it flew, or the other way, whichever is nearer), with a
        // thump; lying there; then up onto its feet.
        double since = t - landed;
        double lie = lying(tumble);
        double up = upright(lie);
        double spun = Math.round(twist / (Math.PI * 2.0)) * Math.PI * 2.0;
        double relax = 1.0 - smooth(since / 4.0);
        if (since < THUMP) {
            double u = since / THUMP;
            double eased = 1.0 - Math.pow(1.0 - u, 3.0) + BOUNCE * Math.sin(Math.PI * u) * (1.0 - u);
            double angle = tumble + (lie - tumble) * eased;
            return new Frame(head * relax, pitch * relax, twist, angle, Math.abs(Math.sin(angle)));
        }
        double rising = (since - DOWN_TICKS) / GETUP_TICKS;
        if (rising <= 0.0) {
            return new Frame(head * relax, pitch * relax, twist, lie, 1.0);
        }
        double u = smooth(Math.min(1.0, rising));
        double angle = lie + (up - lie) * u;
        return new Frame(head * relax * (1.0 - u), pitch * relax * (1.0 - u), twist + (spun - twist) * u, angle, Math.abs(Math.sin(angle)));
    }

    /** True once it is back on its feet, {@code t} ticks after the blow, having landed {@code landed} after it. */
    public static boolean over(double t, double landed) {
        return !Double.isNaN(landed) && t - landed >= DOWN_TICKS + GETUP_TICKS;
    }

    /** The body's turn about its own length {@code t} ticks into its flight: after the head, then on round. */
    static double twist(double t) {
        double follow = BODY_TURN * smooth((t - BODY_LAG) / BODY_TICKS);
        double on = t - BODY_LAG - BODY_TICKS;
        return follow + (on > 0.0 ? SPIN * on : 0.0);
    }

    /** Its tip over {@code t} ticks into its flight: getting going, then over and over. */
    static double tumble(double t) {
        double x = t - TUMBLE_LAG;
        if (x <= 0.0) {
            return 0.0;
        }
        return TUMBLE * (x < TUMBLE_RAMP ? x * x / (2.0 * TUMBLE_RAMP) : x - TUMBLE_RAMP / 2.0);
    }

    /** The flat angle nearest {@code tumble} (a quarter turn, three quarters, and so on round); at least a quarter turn over. */
    static double lying(double tumble) {
        double k = Math.round((tumble - Math.PI / 2.0) / Math.PI);
        return Math.max(Math.PI / 2.0, Math.PI / 2.0 + k * Math.PI);
    }

    /** Up from lying at {@code lie}: the nearer of a quarter turn back or on that is a whole number of turns. */
    static double upright(double lie) {
        double back = lie - Math.PI / 2.0;
        double turns = back / (Math.PI * 2.0);
        return Math.abs(turns - Math.round(turns)) < 1.0E-6 ? back : lie + Math.PI / 2.0;
    }

    static double snap(double u) {
        double c = Math.max(0.0, Math.min(1.0, u));
        return 1.0 - (1.0 - c) * (1.0 - c) * (1.0 - c);
    }

    static double smooth(double u) {
        double c = Math.max(0.0, Math.min(1.0, u));
        return c * c * (3.0 - 2.0 * c);
    }
}
