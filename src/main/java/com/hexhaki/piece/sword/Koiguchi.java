package com.hexhaki.piece.sword;

/**
 * 2.1.5: the Swordsman's readiness while standing in combat mode (pure, tested in {@code SwordTests}). From studying the One Piece
 * swordsmen the owner pointed at (Shanks, Mihawk, and Rayleigh's draw against Blackbeard): the masters stand loose and still, a hand
 * resting on the hilt, and the one tell that a cut is coming is the thumb breaking the saya's seal, <i>koiguchi o kiru</i>: the guard
 * pushed a finger's width out, a sliver of steel showing, held, then let back with a click. Rayleigh does exactly that with his Haki
 * crackling out of the gap.
 *
 * <p>So a Swordsman who has stood ready for {@link #SETTLE} ticks breaks the seal every {@link #EVERY} ticks: the blade eases
 * {@link #OUT} model pixels (a thumb's width) out of the saya, holds with a faint tremble, and slides home ({@code client/sword/SwordLayer} draws it,
 * {@code HakiDraw} makes the gap spark with Haki on).
 */
public final class Koiguchi {
    public static final int SETTLE = 40;
    public static final int EVERY = 150;
    public static final float OUT = 1.2F;
    static final double PUSH = 3.0;
    static final double HOLD = 16.0;
    static final double HOME = 4.0;

    private Koiguchi() {
    }

    /** How far (model px) the blade is pushed out of its saya {@code ticks} into the ready stance (negative: not in it). */
    public static float push(double ticks) {
        if (ticks < SETTLE) {
            return 0.0F;
        }
        double c = (ticks - SETTLE) % EVERY;
        if (c < PUSH) {
            double u = c / PUSH;
            return (float) (OUT * u * u * (3 - 2 * u));
        }
        if (c < PUSH + HOLD) {
            // a held breath: the thumb's tremble
            return (float) (OUT + 0.05 * Math.sin((c - PUSH) * 2.3));
        }
        if (c < PUSH + HOLD + HOME) {
            double u = (c - PUSH - HOLD) / HOME;
            return (float) (OUT * (1 - u * u));
        }
        return 0.0F;
    }
}
