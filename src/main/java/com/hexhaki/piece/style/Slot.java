package com.hexhaki.piece.style;

/** Where a style move is thrown from: the four-blow M1 string, the right click (empty hand), and the two special keys. */
public enum Slot {
    M1_1, M1_2, M1_3, M1_4, HEAVY, SPECIAL_A, SPECIAL_B;

    public boolean string() {
        return this.ordinal() <= M1_4.ordinal();
    }

    public static Slot byOrdinal(int ordinal) {
        Slot[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : M1_1;
    }
}
