package com.hexhaki.piece.ito;

/**
 * 3.5.2: Sky Road reworked (the owner: "Sky walk needs a rework, I dont want 2 constant lines holding me up I wanna climb up like stairs
 * when I hold Space, I wanna jump from 1 string to another further away, I want to swing like spiderman, come on man make sky walk insanely
 * epic"; their keys: W/A/S/D swing, a tap of Space leaps, Space held climbs, Shift drops; still, one hand hangs on one string; a trick as a
 * swing lets go). The pure numbers client/ito/SkyRoad moves one's own body by, every tick (the game then moves it, with its collisions,
 * as the Light fruit's glide is moved), and the tests check: one string at a time, never two.
 *
 * <p>Points are the grip (the hand on the string: the feet plus {@link #GRIP} up); velocities in blocks a tick. Nothing here allocates
 * but the few arrays it is handed.
 */
public final class SkyPhysics {
    private SkyPhysics() {
    }

    /** How high over the feet the hand holds the string (the arm straight up). */
    public static final double GRIP = 2.0;
    /** Pulled down a tick: on a string swinging, and flying free (a little floatier: the strings' lightness). */
    public static final double GRAVITY = 0.085;
    public static final double FLY_GRAVITY = 0.07;
    /** The most speed there is (well inside the game's own movement check), and a fall's. */
    public static final double TOP = 1.6;
    public static final double FALL_MOST = 1.4;
    /** The flat speed kept a tick flying free. */
    public static final double DRAG = 0.995;
    /** A swing: pumped along the keys' way a tick (sprinting harder); the air's drag a tick; hanging, the swing dies away. */
    public static final double PUMP = 0.026;
    public static final double PUMP_SPRINT = 0.05;
    public static final double SWING_DRAG = 0.996;
    public static final double HANG_DRAG = 0.93;
    /** The string reels itself in as it swings (it tightens and pulls: the swing gains), down to this share of its first length. */
    public static final double REEL = 0.992;
    public static final double REEL_MOST = 0.92;
    /** A swing's string: thrown this far ahead (slow to fast) and this high over the hand, taking {@link #THROW} ticks to get there. */
    public static final double REACH_SLOW = 5.0;
    public static final double REACH_FAST = 12.0;
    public static final double RISE = 10.0;
    public static final double THROW = 3.0;
    /** Never a string shorter (it would jerk), nor one that swings the feet lower than this over the ground. */
    public static final double SHORTEST = 4.0;
    public static final double CLEAR = 1.3;
    /** Let go past this angle ahead of the anchor while rising and slowing (or past {@link #LET_GO_MOST} at once): the kick then. */
    public static final double LET_GO = Math.toRadians(30.0);
    public static final double LET_GO_MOST = Math.toRadians(60.0);
    public static final double KICK = 1.08;
    public static final double KICK_UP = 0.1;
    /** After letting go, the next string at the earliest (the trick's time), and at the latest. */
    public static final int NEXT_SOONEST = 9;
    public static final int NEXT_LATEST = 18;
    /** Hanging: the string thrown up this high, a little ahead along the drift (so the body swings under it and settles). */
    public static final double HANG_RISE = 8.0;
    /** A leap (a tap of Space): this far along the look, rising or sinking with it, landing on a string caught over the place in so many ticks. */
    public static final double LEAP = 13.0;
    public static final double LEAP_UP = 6.0;
    public static final double LEAP_DOWN = 5.0;
    public static final int LEAP_TICKS = 14;
    /** A tap of Space is let go within this many ticks; held longer it climbs. */
    public static final int TAP = 5;
    /** Climbing like stairs: a step every {@link #STEP_TICKS} ticks, this high and (with a key held) this far along. */
    public static final int STEP_TICKS = 5;
    public static final double STEP_RISE = 1.0;
    public static final double STEP_ALONG = 0.55;
    /** How a step's rise is spread over its ticks (quick up, settling on the string). */
    static final double[] STEP = {0.34, 0.28, 0.19, 0.12, 0.07};

    /**
     * One tick on a string: the velocity {@code v} (in and out, 3) of the grip at {@code h} (3) on a string to {@code a} (3) {@code length}
     * long, pulled by gravity, pumped by {@code push} (3, a tick's push), dragged by {@code drag}; where the string is taut the next place
     * is put back on its sphere (a string pulls, never pushes: slack, it falls free), the velocity what gets it there. At most {@link #TOP}.
     */
    public static void swing(double[] h, double[] v, double[] a, double length, double[] push, double drag) {
        double vx = (v[0] + push[0]) * drag;
        double vy = (v[1] + push[1] - GRAVITY) * drag;
        double vz = (v[2] + push[2]) * drag;
        double qx = h[0] + vx;
        double qy = h[1] + vy;
        double qz = h[2] + vz;
        double dx = qx - a[0];
        double dy = qy - a[1];
        double dz = qz - a[2];
        double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (d > length && d > 1.0E-9) {
            qx = a[0] + dx / d * length;
            qy = a[1] + dy / d * length;
            qz = a[2] + dz / d * length;
            vx = qx - h[0];
            vy = qy - h[1];
            vz = qz - h[2];
        }
        v[0] = vx;
        v[1] = vy;
        v[2] = vz;
        clamp(v, TOP);
    }

    /** One tick flying free (let go, leaping, dropping): gravity, the fall at most {@link #FALL_MOST}, a touch of drag. */
    public static void fly(double[] v, double gravity) {
        v[0] *= DRAG;
        v[2] *= DRAG;
        v[1] = Math.max(-FALL_MOST, v[1] - gravity);
        clamp(v, TOP);
    }

    /**
     * Where a swing's string is thrown: from the grip {@code h} along the flat way (wx, wz) (unit), further the faster ({@code speed}),
     * {@link #RISE} (and a little more by {@code r}, 0 to 1) over the cruising height {@code cruise} (the grip's height the swinging holds:
     * a swing neither climbs nor sinks on its own; Space climbs, Shift drops) and never under {@link #ABOVE} over the grip; into
     * {@code out} (x, y, z, the length it reels to, the length it catches at). The length it reels to puts the swing's bottom {@link #DIP}
     * under the cruise and, over ground at {@code groundY} (NaN: none found), never within {@link #CLEAR} of it (the anchor raised too); it
     * catches no shorter than the gap (never a snap) and reels in from there ({@link #reel}: a quick yank up when it must).
     */
    public static void anchor(double[] h, double wx, double wz, double speed, double r, double cruise, double groundY, double[] out) {
        double reach = REACH_SLOW + (REACH_FAST - REACH_SLOW) * Math.min(1.0, speed / 1.2);
        double ax = h[0] + wx * reach;
        double ay = Math.max(cruise + RISE + 2.0 * r, h[1] + ABOVE);
        double az = h[2] + wz * reach;
        double bottom = cruise - DIP;
        if (!Double.isNaN(groundY) && bottom < groundY + GRIP + CLEAR) {
            double up = groundY + GRIP + CLEAR - bottom;
            bottom += up;
            ay += up;
        }
        double gap = Math.sqrt((ax - h[0]) * (ax - h[0]) + (ay - h[1]) * (ay - h[1]) + (az - h[2]) * (az - h[2]));
        out[0] = ax;
        out[1] = ay;
        out[2] = az;
        out[3] = Math.max(SHORTEST, ay - bottom);
        out[4] = Math.max(out[3], gap);
    }

    /**
     * A swing's string a tick on, {@code length} now, reeling to {@code target}: longer than that, it yanks in {@link #PULL} a tick; then
     * it tightens slowly (it pulls: the swing gains) down to {@link #REEL_MOST} of the target.
     */
    public static double reel(double length, double target) {
        if (length > target) {
            return Math.max(target, length - PULL);
        }
        return Math.max(target * REEL_MOST, length * REEL);
    }

    /** How fast a string too long for its swing reels the body in (blocks a tick). */
    public static final double PULL = 0.45;

    /** A swing's bottom this far under the cruising height; a string never thrown lower than this over the grip. */
    public static final double DIP = 3.0;
    public static final double ABOVE = 3.0;

    /** Hanging: the string thrown up over the grip, a little ahead along the flat drift (vx, vz); into {@code out} (x, y, z, length). */
    public static void hangAnchor(double[] h, double vx, double vz, double[] out) {
        out[0] = h[0] + vx * 4.0;
        out[1] = h[1] + HANG_RISE;
        out[2] = h[2] + vz * 4.0;
        double dx = out[0] - h[0];
        double dz = out[2] - h[2];
        out[3] = Math.sqrt(dx * dx + HANG_RISE * HANG_RISE);
    }

    /**
     * Whether a swing lets go now: the grip {@code h} on a string to {@code a}, moving {@code v}, swung along the flat way (wx, wz): rising
     * past {@link #LET_GO} ahead of the anchor (flung on forward and a little up), past {@link #LET_GO_MOST} whatever, or at the top of a
     * weak swing that stopped short ahead of it (it falls on forward to the next string).
     */
    public static boolean letGo(double[] h, double[] a, double[] v, double wx, double wz) {
        double rx = h[0] - a[0];
        double ry = h[1] - a[1];
        double rz = h[2] - a[2];
        double ahead = rx * wx + rz * wz;
        if (ahead <= 0.0) {
            return false;
        }
        double angle = Math.atan2(ahead, -ry);
        return angle > LET_GO_MOST || angle > LET_GO && v[1] > 0.0 || angle > PEAK && v[1] <= 0.0 && v[0] * wx + v[2] * wz < 0.05;
    }

    /** A weak swing's top, this far ahead of the anchor at the least, lets go too. */
    public static final double PEAK = Math.toRadians(10.0);

    /** Letting go: the swing's speed kicked on and up (the release's slingshot), at most {@link #TOP}. */
    public static void kick(double[] v) {
        v[0] *= KICK;
        v[1] = v[1] * KICK + KICK_UP;
        v[2] *= KICK;
        clamp(v, TOP);
    }

    /**
     * A leap from the grip {@code h} along the look (yaw, pitch in degrees, the game's): the place it lands (into {@code at}, 3), and the
     * velocity that carries it there in {@link #LEAP_TICKS} ticks flying free (into {@code v}, 3).
     */
    public static void leap(double[] h, double yaw, double pitch, double[] at, double[] v) {
        double y = Math.toRadians(yaw);
        double p = Math.toRadians(pitch);
        double fx = -Math.sin(y);
        double fz = Math.cos(y);
        double rise = Math.max(-LEAP_DOWN, Math.min(LEAP_UP, -Math.sin(p) * LEAP));
        at[0] = h[0] + fx * LEAP;
        at[1] = h[1] + rise;
        at[2] = h[2] + fz * LEAP;
        int n = LEAP_TICKS;
        // flying free: after n ticks the drop is gravity x n (n - 1) / 2 (each tick moves by the velocity, then gravity takes it)
        // and the flat speed loses a touch of drag a tick (fly): the sum of DRAG^k over the n ticks
        double carried = (1.0 - Math.pow(DRAG, n)) / (1.0 - DRAG);
        v[0] = (at[0] - h[0]) / carried;
        v[1] = (at[1] - h[1]) / n + FLY_GRAVITY * (n - 1) / 2.0;
        v[2] = (at[2] - h[2]) / carried;
    }

    /** A climbing step's velocity at its tick {@code k} (0 to {@link #STEP_TICKS} - 1): up, and along the flat way (wx, wz) given. */
    public static void step(int k, double wx, double wz, double[] v) {
        double f = STEP[Math.max(0, Math.min(STEP.length - 1, k))];
        v[0] = wx * STEP_ALONG * f;
        v[1] = STEP_RISE * f;
        v[2] = wz * STEP_ALONG * f;
    }

    /** At most {@code most} long. */
    static void clamp(double[] v, double most) {
        double l = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (l > most) {
            v[0] *= most / l;
            v[1] *= most / l;
            v[2] *= most / l;
        }
    }
}
