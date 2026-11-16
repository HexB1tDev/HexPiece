package com.hexhaki.piece.sword;

/**
 * 2.1.4: what a player's sword is doing, as the server tells every client ({@code S2CSword}) (pure): sheathed (nothing playing), the
 * guard between cuts, the sheathing, the charge (from a drawn or a sheathed sword), the release, the quick draw (from the saya, or with
 * the blade already out), or one of the {@link SwordMove}s ({@link #MOVES} + its ordinal). Each has its animation
 * ({@code assets/hexhaki/player_animation/<name>.json}) and, in {@link BladePaths}, the ticks the blade leaves and enters the saya.
 */
public final class SwordAnims {
    public static final int NONE = 0;
    public static final int GUARD = 1;
    public static final int SHEATHE = 2;
    public static final int CHARGE = 3;
    public static final int CHARGE_DRAW = 4;
    public static final int RELEASE = 5;
    public static final int QUICK_DRAW = 6;
    public static final int QUICK_CUT = 7;
    public static final int MOVES = 10;
    private static final String[] NAMES = {null, "sword_guard", "sword_sheathe", "sword_charge", "sword_charge_draw", "sword_release",
        "sword_quick_draw", "sword_quick_cut"};

    private SwordAnims() {
    }

    public static int of(SwordMove move) {
        return MOVES + move.ordinal();
    }

    /** The animation's name, or null (sheathed: nothing plays). */
    public static String name(int anim) {
        if (anim >= MOVES) {
            SwordMove move = SwordMove.byOrdinal(anim - MOVES);
            return move == null ? null : move.animation();
        }
        return anim > 0 && anim < NAMES.length ? NAMES[anim] : null;
    }

    /** How long it lasts (ticks; a loop: its whole length, looping from {@link #loopFrom}). */
    public static int duration(int anim) {
        if (anim >= MOVES) {
            SwordMove move = SwordMove.byOrdinal(anim - MOVES);
            return move == null ? 0 : move.duration();
        }
        return switch (anim) {
            case GUARD -> 24;
            case SHEATHE -> 6;
            case CHARGE, CHARGE_DRAW -> 26;
            case RELEASE -> 15;
            case QUICK_DRAW, QUICK_CUT -> 16;
            default -> 0;
        };
    }

    /** Where a loop starts again (the guard from its start, the charges from their held pose). */
    public static int loopFrom(int anim) {
        return anim == CHARGE || anim == CHARGE_DRAW ? 6 : 0;
    }

    /** The animation's own tick for a moment {@code ticks} after it began (loops wrapped). */
    public static float frame(int anim, float ticks) {
        if (!loops(anim)) {
            return ticks;
        }
        int end = duration(anim);
        int from = loopFrom(anim);
        if (ticks <= end || end <= from) {
            return ticks;
        }
        return from + (ticks - from) % (end - from);
    }

    /** Loops (held until the server says otherwise): the guard and the charges. */
    public static boolean loops(int anim) {
        return anim == GUARD || anim == CHARGE || anim == CHARGE_DRAW;
    }

    /** Is the blade in the hand {@code ticks} into {@code anim} (else in its saya)? */
    public static boolean drawn(int anim, float ticks) {
        String name = name(anim);
        if (name == null) {
            return false;
        }
        int draw = BladePaths.drawTick(name);
        int sheath = BladePaths.sheathTick(name);
        if (draw >= 0 && ticks < draw) {
            return false;
        }
        return sheath < 0 || ticks < sheath;
    }
}
