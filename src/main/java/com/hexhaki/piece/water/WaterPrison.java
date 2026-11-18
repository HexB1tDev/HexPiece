package com.hexhaki.piece.water;

/**
 * 2.4.0: Fishman Karate's Water Prison (pure, tested in {@code StyleTests}). The owner: "Water Prison: The user pulls both hands up into
 * the sky (Needs an entity to target) the entity selected gets trapped and floats a bit off the ground in a water bubble a bit bigger than
 * the entity's or player's size for 5 seconds. (perfect stun, entity can only look around.)"
 *
 * <p>Thrown only at a body in the user's sights ({@link #sights}: within {@link #RANGE}, the look passing through its box grown by
 * {@link #AIM}). Both hands go up; from {@link #GATHER} the air's moisture gathers round the target (a mist drawn in from all round it)
 * and the bubble swells out of it ({@link #grown}), sealing on {@link #SEAL}: the body is lifted {@link #LIFT} off the ground and held
 * there in a ball of water {@link #radius} across for {@link #HOLD} ticks: it can only look around. A perfect stun needs a way out (told
 * to the owner, who has not said otherwise): the bubble bursts early once {@link #BREAKS} damage has been dealt to whoever is in it, or
 * when the one inside lets out its Conqueror's Haki.
 */
public final class WaterPrison {
    /** How far a target can be (blocks), and how much its box is grown for the aim (blocks). */
    public static final double RANGE = 16.0;
    public static final double AIM = 0.35;
    /** The water starts gathering round the target, and the bubble is sealed (ticks into the move). */
    public static final int GATHER = 4;
    public static final int SEAL = 14;
    /** The bubble swells over this many ticks before it seals. */
    public static final int SWELL = 7;
    /** How long it holds (5 seconds), how high it lifts the body (blocks), the damage that bursts it. */
    public static final int HOLD = 100;
    public static final double LIFT = 0.6;
    public static final float BREAKS = 14.0F;

    private WaterPrison() {
    }

    /** The bubble's radius round a body {@code height} tall and {@code width} wide: a bit bigger than it. */
    public static double radius(double height, double width) {
        return Math.max(height, width) * 0.5 * 1.18 + 0.3;
    }

    /** How swollen the bubble is {@code tick} ticks into the move: nothing, swelling (eased), whole once sealed. */
    public static double grown(double tick) {
        double t = (tick - (SEAL - SWELL)) / SWELL;
        if (t <= 0) {
            return 0.0;
        }
        if (t >= 1) {
            return 1.0;
        }
        return t * t * (3 - 2 * t);
    }

    /**
     * How far along the look (unit {@code dir} from {@code eye}) it first enters the box {@code min}..{@code max} grown by {@link #AIM},
     * or -1 if it misses or is beyond {@link #RANGE} (the slab test).
     */
    public static double sights(double[] eye, double[] dir, double[] min, double[] max) {
        double near = 0.0;
        double far = RANGE;
        for (int k = 0; k < 3; k++) {
            double lo = min[k] - AIM;
            double hi = max[k] + AIM;
            if (Math.abs(dir[k]) < 1.0E-9) {
                if (eye[k] < lo || eye[k] > hi) {
                    return -1;
                }
                continue;
            }
            double a = (lo - eye[k]) / dir[k];
            double b = (hi - eye[k]) / dir[k];
            near = Math.max(near, Math.min(a, b));
            far = Math.min(far, Math.max(a, b));
            if (near > far) {
                return -1;
            }
        }
        return near;
    }

    /** True once the damage dealt to whoever is inside ({@code taken}) bursts the bubble. */
    public static boolean bursts(float taken) {
        return taken >= BREAKS;
    }
}
