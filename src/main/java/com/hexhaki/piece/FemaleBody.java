package com.hexhaki.piece;

/**
 * 2.1.1: the female body (pure), ported from HexGhoul's (the owner: "the same Male/female as HexGhoul"): slim arms, wider hips, the bust
 * on the torso bouncing as she moves. Unlike HexGhoul's, a woman here keeps a player's full size (no hitbox change).
 */
public final class FemaleBody {
    public static final float ARMS = 0.75F;
    public static final float HIPS = 1.25F;
    public static final float THIGHS = 1.1F;
    public static final float BUST_TOP = 1.5F;
    public static final float BUST_TIP = 4.5F;
    public static final float BUST_BOTTOM = 6.0F;
    public static final float BUST_FRONT = -2.0F;
    public static final float BUST_OUT = -3.6F;
    public static final double JIGGLE_STIFF = 0.9;
    public static final double JIGGLE_DAMP = 0.35;
    public static final double JIGGLE_MOST = 0.075;

    private FemaleBody() {
    }

    /** The bust's lag carried on over {@code ticks} as the body moved {@code moved} blocks up (state: lag, its speed). */
    public static void jiggle(double[] state, double moved, double ticks) {
        if (Math.abs(moved) > 1.0 || !(ticks > 0.0)) {
            if (Math.abs(moved) > 1.0) {
                state[0] = 0.0;
                state[1] = 0.0;
            }
            return;
        }
        state[0] = Math.max(-JIGGLE_MOST, Math.min(JIGGLE_MOST, state[0] - moved));
        int steps = (int) Math.ceil(Math.min(ticks, 4.0) / 0.25);
        double h = Math.min(ticks, 4.0) / steps;
        for (int i = 0; i < steps; i++) {
            state[1] += (-JIGGLE_STIFF * state[0] - JIGGLE_DAMP * state[1]) * h;
            state[0] = Math.max(-JIGGLE_MOST, Math.min(JIGGLE_MOST, state[0] + state[1] * h));
        }
    }
}
