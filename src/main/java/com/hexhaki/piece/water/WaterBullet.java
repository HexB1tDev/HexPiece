package com.hexhaki.piece.water;

/**
 * 2.3.8: Fishman Karate's Water Bullet (pure, tested in {@code StyleTests}). The owner: "Water Bullet: The user pulls the right hand up
 * into the sky and 5 dropplets of water form on top of the first in a horizontal arch line over the fist, then the user pushes the hand
 * forward (pointing forward) and the bullets auto fire and lock on to any entity infront of the user. (damage)", and of all its water:
 * "the water actually materializes from the air particles in the air".
 *
 * <p>The right hand goes up ({@link #RAISED}); over the fist the air's moisture gathers into five drops one after another, each drawn
 * in out of a mist round its place and swelling to a full drop ({@link #grown}), along a flat arch across the fist ({@link #formation});
 * the hand is pushed forward and the drops fire one a tick from {@link #FIRE} ({@link #fireTick}), each locked onto a foe in front (the
 * nearest first, round again when there are fewer than five: {@link #target}) and turning after it as it flies ({@link #steer}). One
 * that finds nothing flies straight on along the look and falls apart into spray when its life runs out.
 */
public final class WaterBullet {
    public static final int COUNT = 5;
    /** The hand is up (ticks into the move). */
    public static final int RAISED = 4;
    /** The first drop starts gathering, the next ones this much later each, and each takes this long to form. */
    public static final int FORM_FROM = 4;
    public static final int FORM_EACH = 1;
    public static final int FORM_TICKS = 6;
    /** The first shot (the hand pushed forward just before), then one a tick. */
    public static final int FIRE = 16;
    /** A drop's radius (blocks). */
    public static final double RADIUS = 0.13;
    /** The arch: its half width across the fist, how much higher its middle stands than its ends, and how far over the fist it is. */
    public static final double ARCH = 0.5;
    public static final double ARCH_RISE = 0.2;
    public static final double OVER = 0.32;
    /** The raised fist: right of the feet's middle, above the feet, ahead (blocks, in the user's facing). */
    public static final double[] FIST = {0.34, 2.18, 0.12};
    /** Flight: blocks a tick, the most it turns toward its target a tick, ticks before it falls apart. */
    public static final double SPEED = 1.5;
    public static final double TURN = Math.toRadians(24.0);
    public static final int LIFE = 28;
    /** Targets are looked for this far ahead and this far either side of the look (degrees). */
    public static final double RANGE = 24.0;
    public static final double CONE = 45.0;
    /** How close (blocks, past the body's half width) a drop comes to strike. */
    public static final double STRIKES = 0.45;

    private WaterBullet() {
    }

    /** Drop {@code i}'s place, blocks right, up and ahead of the user's feet (its facing): along the arch over the raised fist. */
    public static double[] formation(int i) {
        double t = COUNT == 1 ? 0.0 : (i / (double) (COUNT - 1)) * 2.0 - 1.0;
        return new double[] {FIST[0] + t * ARCH, FIST[1] + OVER + ARCH_RISE * (1.0 - t * t), FIST[2]};
    }

    /** The tick drop {@code i} starts to gather. */
    public static int gathers(int i) {
        return FORM_FROM + i * FORM_EACH;
    }

    /** How formed drop {@code i} is at {@code tick} of the move: 0 (mist) to 1 (a full drop), eased; 1 from then on. */
    public static double grown(int i, double tick) {
        double t = (tick - gathers(i)) / FORM_TICKS;
        if (t <= 0) {
            return 0.0;
        }
        if (t >= 1) {
            return 1.0;
        }
        return t * t * (3 - 2 * t);
    }

    /** The tick drop {@code i} is fired. */
    public static int fireTick(int i) {
        return FIRE + i;
    }

    /** Which of {@code targets} (nearest first) drop {@code i} locks onto (-1: none, it flies straight on). */
    public static int target(int i, int targets) {
        return targets <= 0 ? -1 : i % targets;
    }

    /** True if a body {@code dx}, {@code dz} off the user is in front of a look along ({@code lookX}, {@code lookZ}) (within the cone). */
    public static boolean ahead(double lookX, double lookZ, double dx, double dz) {
        double look = Math.hypot(lookX, lookZ);
        double away = Math.hypot(dx, dz);
        if (away < 1.0E-6 || look < 1.0E-6) {
            return away < 1.0E-6;
        }
        return (lookX * dx + lookZ * dz) / (look * away) >= Math.cos(Math.toRadians(CONE));
    }

    /** A drop's velocity turned toward {@code toward} (any length) by at most {@link #TURN}, its speed {@link #SPEED}. */
    public static double[] steer(double[] velocity, double[] toward) {
        double[] v = unit(velocity);
        double[] t = unit(toward);
        double dot = Math.max(-1.0, Math.min(1.0, v[0] * t[0] + v[1] * t[1] + v[2] * t[2]));
        double angle = Math.acos(dot);
        double[] out;
        if (angle <= TURN || angle < 1.0E-6) {
            out = t;
        } else {
            // turned TURN along the great circle from v toward t
            double[] across = unit(new double[] {t[0] - v[0] * dot, t[1] - v[1] * dot, t[2] - v[2] * dot});
            double c = Math.cos(TURN);
            double s = Math.sin(TURN);
            out = new double[] {v[0] * c + across[0] * s, v[1] * c + across[1] * s, v[2] * c + across[2] * s};
        }
        return new double[] {out[0] * SPEED, out[1] * SPEED, out[2] * SPEED};
    }

    static double[] unit(double[] v) {
        double l = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        return l < 1.0E-9 ? new double[] {0, 0, 1} : new double[] {v[0] / l, v[1] / l, v[2] / l};
    }

    /** The point (right, up, ahead) of the user's own frame in the world, the user's feet at ({@code x}, {@code y}, {@code z}), yaw degrees. */
    public static double[] world(double[] local, double x, double y, double z, double yawDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double rx = -Math.cos(yaw);
        double rz = -Math.sin(yaw);
        return new double[] {x + rx * local[0] + fx * local[2], y + local[1], z + rz * local[0] + fz * local[2]};
    }
}
