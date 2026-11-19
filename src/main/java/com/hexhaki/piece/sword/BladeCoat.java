package com.hexhaki.piece.sword;

/**
 * 2.6.1: the Haki coat on a sword's blade (pure: client/sword/BladeCoatClient times it, client/sword/KatanaRenderer paints it; tested in
 * {@code SwordTests}). The owner: "Haki coating is ONLY on the blade of the swords, never on the handle or saya", then "please make sure
 * the haki coating on the blade is also progressive not instant (like the arms slowly turning black)".
 * <ul>
 *   <li>Only the steel (the mesh's 01_Blade part) turns black; the habaki, guard, handle and saya keep their colours.</li>
 *   <li>It spreads as the hands do (haki/HandCoat): from the guard up to the point, each point of the steel a little out of step with
 *       its neighbours ({@link #reveal}), over {@link #ON_NANOS}, starting {@link #DELAY_NANOS} after the Haki comes on (the fist is black
 *       first: the coat runs from the hand into the blade); it draws back the same way, the point first, over {@link #OFF_NANOS}.</li>
 * </ul>
 */
public final class BladeCoat {
    /** How long the black takes to run up the blade, how long after the Haki comes on it starts, and how long it takes to draw back. */
    public static final long ON_NANOS = 1_100_000_000L;
    public static final long DELAY_NANOS = 350_000_000L;
    public static final long OFF_NANOS = 600_000_000L;
    /** How long one point of the steel takes to turn, in progress (0..1 over the whole blade), as the hands' pixels do. */
    public static final float SPREAD = 0.18F;

    private BladeCoat() {
    }

    /**
     * The coat's progress (0..1) {@code elapsed} nanoseconds after it was turned {@code on} (or off) at progress {@code from}: coming
     * on, it waits {@link #DELAY_NANOS} from nothing (not when it was turned round part way), then runs up in {@link #ON_NANOS}; going
     * off it runs down in {@link #OFF_NANOS}.
     */
    public static float progress(boolean on, long elapsed, float from) {
        if (on) {
            long wait = from <= 0.0F ? DELAY_NANOS : 0L;
            return clamp(from + Math.max(0L, elapsed - wait) / (float) ON_NANOS);
        }
        return clamp(from - elapsed / (float) OFF_NANOS);
    }

    /**
     * The progress at which a point of the steel starts turning: {@code along} 0 at the guard to 1 at the point, {@code seed} the
     * point's own (its vertex): the guard first, the point last, each a little out of step.
     */
    public static float reveal(float along, int seed) {
        int h = seed * 0x9E3779B1;
        h ^= h >>> 15;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        float jitter = (h & 0xFFFF) / 65535.0F;
        return 0.02F + clamp(along) * 0.70F + jitter * 0.10F;
    }

    /** How black a point of the steel is (0..1) at {@code progress}: eased in over {@link #SPREAD} from its {@link #reveal}. */
    public static float at(float along, int seed, float progress) {
        if (progress <= 0.0F) {
            return 0.0F;
        }
        if (progress >= 1.0F) {
            return 1.0F;
        }
        float t = clamp((progress - reveal(along, seed)) / SPREAD);
        return t * t * (3.0F - 2.0F * t);
    }

    static float clamp(float v) {
        return Math.max(0.0F, Math.min(1.0F, v));
    }
}
