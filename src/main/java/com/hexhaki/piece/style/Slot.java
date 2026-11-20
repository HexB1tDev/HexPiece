package com.hexhaki.piece.style;

/**
 * Where a style move is thrown from: the four-blow M1 string, the right click (empty hand), the two special keys, and (2.8.4) the three
 * extra keys of combat mode (C, V and B: the show's own moves of each style).
 */
public enum Slot {
    M1_1, M1_2, M1_3, M1_4, HEAVY, SPECIAL_A, SPECIAL_B, EXTRA_C, EXTRA_V, EXTRA_B;

    /** 2.8.4: one of the three extra keys. */
    public boolean extra() {
        return this.ordinal() >= EXTRA_C.ordinal();
    }

    public boolean string() {
        return this.ordinal() <= M1_4.ordinal();
    }

    public static Slot byOrdinal(int ordinal) {
        Slot[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : M1_1;
    }
}
