package com.hexhaki.piece;

/**
 * 2.1.1 (the owner: "decide between being a pirate or a marine"): which side of the sea a player sails for, picked on the start menu.
 * Pirates earn a bounty (berries) for every marine they beat; marines earn merit (and rank) for every pirate. NPCs of the other side
 * attack on sight. By ordinal.
 */
public enum Faction {
    UNSET("Unset", 0xFFB0B0B0, "", ""),
    PIRATE("Pirate", 0xFFE0B040, "Bounty", "Sail under your own flag. Every marine you beat raises the price on your head."),
    MARINE("Marine", 0xFF5AA0E6, "Merit", "Uphold justice. Every pirate you bring down earns merit and a higher rank.");

    private final String display;
    private final int colour;
    private final String renownName;
    private final String blurb;

    Faction(String display, int colour, String renownName, String blurb) {
        this.display = display;
        this.colour = colour;
        this.renownName = renownName;
        this.blurb = blurb;
    }

    public String display() {
        return this.display;
    }

    public int colour() {
        return this.colour;
    }

    public String renownName() {
        return this.renownName;
    }

    public String blurb() {
        return this.blurb;
    }

    public boolean choice() {
        return this != UNSET;
    }

    /** The side that hunts this one (none for unset). */
    public Faction enemy() {
        return this == PIRATE ? MARINE : this == MARINE ? PIRATE : UNSET;
    }

    public static Faction byOrdinal(int ordinal) {
        Faction[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : UNSET;
    }
}
