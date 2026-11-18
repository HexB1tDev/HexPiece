package com.hexhaki.piece;

import com.hexhaki.piece.style.FightingStyle;

/**
 * 2.1.1: what HexPiece keeps about a player (pure value): gender, side, fighting style, whether the start menu was completed, the
 * mastery of every style (by ordinal) and their renown (a pirate's bounty in berries, a marine's merit); 2.4.6: their alignment
 * ({@link Alignment}, -100 pure evil to 100 pure good).
 */
public record PieceProfile(Gender gender, Faction faction, FightingStyle style, boolean chosen, int[] mastery, long renown, int alignment) {
    public static final PieceProfile EMPTY = new PieceProfile(Gender.UNSET, Faction.UNSET, FightingStyle.NONE, false,
        new int[FightingStyle.values().length], 0L, 0);

    public int styleMastery() {
        return this.mastery(this.style);
    }

    public int mastery(FightingStyle of) {
        int index = of.ordinal();
        return index < this.mastery.length ? this.mastery[index] : 0;
    }

    public String title() {
        return PieceRules.title(this.faction, this.renown);
    }
}
