package com.hexhaki.piece;

/** 2.1.1: a player's gender, picked on the start menu (as in HexGhoul). Female bodies are drawn with {@link FemaleBody}. By ordinal. */
public enum Gender {
    UNSET("Unset"),
    MALE("Male"),
    FEMALE("Female");

    private final String display;

    Gender(String display) {
        this.display = display;
    }

    public String display() {
        return this.display;
    }

    public static Gender byOrdinal(int ordinal) {
        Gender[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : UNSET;
    }

    public boolean female() {
        return this == FEMALE;
    }

    public boolean choice() {
        return this != UNSET;
    }
}
