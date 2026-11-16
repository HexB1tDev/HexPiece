package com.hexhaki.piece.style;

import com.hexhaki.piece.Faction;

/**
 * 2.1.1 (the owner: "Custom fighting styles like HexGhoul with custom movesets, extremely well choreographed. (also picked at the
 * start)"): the bare-handed styles a player picks on the start menu. Each has a four-blow M1 string, a right-click signature and two
 * specials that open with mastery ({@link StyleMove}). Any side may learn any style; {@link #leaning} only says who it is known for.
 * Sent by ordinal: new styles go at the end.
 */
public enum FightingStyle {
    NONE("none", "Bare Hands", Faction.UNSET, 0xFFB0B0B0, "No style yet: the game's own punch."),
    BLACK_LEG("black_leg", "Black Leg", Faction.PIRATE, 0xFFE8743A,
        "A cook's kicking art: the hands never touch the fight. Snapping kicks, a spinning string, a neck shot that launches and legs "
            + "that catch fire."),
    SIX_POWERS("six_powers", "Six Powers", Faction.MARINE, 0xFF7CC4FF,
        "The marines' superhuman art: finger pistols, legs that cut the air, a step so fast it vanishes and a body hard as iron."),
    FISHMAN_KARATE("fishman_karate", "Fishman Karate", Faction.UNSET, 0xFF3FC7B5,
        "Open-hand karate that strikes through the water in everything: palms, water bullets flicked from the fingers, a counter that "
            + "turns blows aside and a fist whose shock passes through."),
    BRAWLER("brawler", "Iron Fist", Faction.UNSET, 0xFFD94848,
        "Pirate street boxing: jab, cross, hook and an uppercut that launches, a haymaker, a gatling of jabs and a shoulder charge that "
            + "bowls through a crowd.");

    private final String id;
    private final String display;
    private final Faction leaning;
    private final int colour;
    private final String blurb;

    FightingStyle(String id, String display, Faction leaning, int colour, String blurb) {
        this.id = id;
        this.display = display;
        this.leaning = leaning;
        this.colour = colour;
        this.blurb = blurb;
    }

    public String id() {
        return this.id;
    }

    public String display() {
        return this.display;
    }

    public Faction leaning() {
        return this.leaning;
    }

    public int colour() {
        return this.colour;
    }

    public String blurb() {
        return this.blurb;
    }

    public boolean choice() {
        return this != NONE;
    }

    public static FightingStyle byOrdinal(int ordinal) {
        FightingStyle[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : NONE;
    }

    /** The styles a player may pick. */
    public static FightingStyle[] choices() {
        return new FightingStyle[] {BLACK_LEG, SIX_POWERS, FISHMAN_KARATE, BRAWLER};
    }
}
