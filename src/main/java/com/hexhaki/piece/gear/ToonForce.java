package com.hexhaki.piece.gear;

import java.util.HashMap;
import java.util.Map;

/**
 * 3.3.1 (the owner: "just make sure that this fruit feels like a cartoon, like Bugs Bunny type of deal. Like every move should feel like
 * a Bugs Bunny movie ... because he has kind of Toon Force"): every Nika move squashes and stretches the whole body like rubber, on a
 * spring that overshoots and wobbles back. Pure: the beats of each Gear 5 clip (when, and how hard: positive squashes the body short and
 * wide, negative stretches it tall and thin), the spring, and the sizes it gives; client/gear/NikaToon plays them when a clip starts and
 * draws the body so.
 *
 * <p>The original has a squash of its own (RemoteGearStates: the transformation, Gigante, the Bajrang Gun's impact, Coil Spring, the old
 * Sky Bounce, the dodges, the bouncy walk), which only fades; this one rings like rubber and adds to it. Left alone: the transformation (the
 * owner loves it) and the lightning lance ("That was perfect how it was").
 */
public final class ToonForce {
    /** The spring: its pull back and its damping a tick (underdamped: two or three wobbles). */
    static final double PULL = 0.3;
    static final double DAMP = 0.2;
    /** The most it ever squashes or stretches (spring units). */
    public static final double MOST = 1.2;
    /** How much shorter (and wider) a unit of squash draws the body. */
    static final double SHORT = 0.3;
    static final double WIDE = 0.26;

    /**
     * A clip's beats: tick, kick pairs. A loop plays to its end and goes back to {@code loopFrom} (PlayerAnimator's returnTick), over
     * and over: the beats after {@code loopFrom} come again every {@code end - loopFrom} ticks (one on {@code loopFrom} itself is the end's).
     */
    public record Score(double[] beats, double end, double loopFrom, boolean loop) {
        /** The kicks between {@code from} (excluded) and {@code to} (included) ticks into the clip, summed. */
        public double kicks(double from, double to) {
            double sum = 0;
            for (int i = 0; i < beats.length; i += 2) {
                double t = beats[i];
                if (t > from && t <= to) {
                    sum += beats[i + 1];
                }
                if (loop && t > loopFrom) {
                    double period = end - loopFrom;
                    for (double r = t + period; r <= to; r += period) {
                        if (r > from) {
                            sum += beats[i + 1];
                        }
                    }
                }
            }
            return sum;
        }
    }

    static final Map<String, Score> SCORES = new HashMap<>();

    static {
        // the punch: crouched into it, shot out tall, a recoil
        put("gear5_pistol", 22, 0, false, 0, 0.45, 5, -0.75, 9, 0.3);
        // the barrage: the body buzzing like a plucked rubber band
        put("gear5_gatling", 12, 3, true, 0, 0.4, 3, -0.28, 7.5, 0.28, 12, -0.28);
        // anchored and hauled: squashed down, yanked long, landing squash
        put("gear5_rocket", 18, 0, false, 0, 0.6, 4, -1.0, 9, 0.5);
        put("gear5_whip", 20, 0, false, 0, 0.35, 10, -0.6, 13, 0.25);
        // 3.3.2's cartoon spin: wound up, stretched by the spin, squashed by the stop, popping tall into the skip
        put("gear5_dawn_whip", 20, 0, false, 2, 0.45, 6, -0.5, 10, -0.6, 14, 0.55, 16, -0.5, 19, 0.2);
        put("gear5_giant_charge", 30, 8, true, 0, 0.4, 15, -0.18, 30, 0.18);
        put("gear5_giant_grab", 24, 10, false, 0, 0.3, 8, -0.5);
        put("gear5_giant_throw", 18, 0, false, 0, 0.5, 6, -0.9, 10, 0.4);
        // the laugh shakes the whole body
        put("gear5_gigante_laugh", 32, 0, false, 4, 0.35, 9, -0.3, 13, 0.35, 17, -0.3, 21, 0.35, 25, -0.3, 29, 0.2);
        put("gear5_coil_charge", 40, 10, true, 0, 0.6, 10, 0.15, 20, 0.15, 30, 0.15, 40, 0.15);
        put("gear5_coil_release", 18, 0, false, 0, -0.6, 10, 0.4);
        // the old Sky Bounce's clip (the original's long running leap still plays it): the crouch, the launch, the skip, the landing
        put("gear5_bounce", 14, 0, false, 1, 0.55, 4, -0.9, 7, -0.3, 13, 0.35);
        // 3.3.2: Gear 5's jump: squashed as it pushes off the rubber, stretched tall as it shoots up, the skip popping open, then a
        // rubbery bob through the air (the loop's); the landing squashed flat and springing back
        put("gear5_jump", 13, 5, true, 0, 0.5, 2, -0.9, 5, -0.3, 9, 0.12, 13, -0.12);
        put("gear5_land", 8, 0, false, 0, 0.8, 5, -0.35, 8, 0.1);
        put("gear5_bajrang_charge", 26, 0, true, 0, 0.3, 13, -0.15, 26, 0.15);
        put("gear5_bajrang_slam", 142, 0, false, 0, -0.5, 13, 0.3);
        put("gear5_dodge_backflip", 18, 0, false, 0, 0.5, 3, -0.8, 15, 0.4);
        put("gear5_dodge_sideflip", 18, 0, false, 0, 0.5, 3, -0.8, 16, 0.4);
        put("gear5_dodge_jumpback", 15, 0, false, 0, 0.5, 3, -0.8, 12, 0.4);
        put("gear5_dodge_jumpover", 16, 0, false, 0, 0.5, 2, -0.8, 14, 0.4);
        // standing about: a slow rubbery breathing
        put("gear5_stance", 48, 12, true, 0, 0.1, 24, -0.1, 48, 0.1);
    }

    private ToonForce() {
    }

    static void put(String clip, double end, double loopFrom, boolean loop, double... beats) {
        SCORES.put(clip, new Score(beats, end, loopFrom, loop));
    }

    /** The clip's beats, or null (a clip with none: the transformation, the lance, anything not the Nika fruit's). */
    public static Score score(String clip) {
        return SCORES.get(clip);
    }

    /** One tick of the spring: {@code state} = {position, velocity}, kicked by {@code kick} first. */
    public static void step(double[] state, double kick) {
        state[0] = clamp(state[0] + kick);
        state[1] += -PULL * state[0] - DAMP * state[1];
        state[0] = clamp(state[0] + state[1]);
    }

    /** At rest (nothing left to draw). */
    public static boolean still(double[] state) {
        return Math.abs(state[0]) < 0.003 && Math.abs(state[1]) < 0.003;
    }

    /** How much taller the body draws at squash {@code x} (shorter for a squash). */
    public static double tall(double x) {
        return 1 - SHORT * x;
    }

    /** How much wider the body draws at squash {@code x} (wider for a squash, thinner for a stretch). */
    public static double wide(double x) {
        return 1 + WIDE * x;
    }

    static double clamp(double x) {
        return Math.max(-MOST, Math.min(MOST, x));
    }
}
