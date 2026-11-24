package com.hexhaki.piece.style;

import com.hexhaki.piece.Faction;

/**
 * 2.1.1 (the owner: "Custom fighting styles like HexGhoul with custom movesets, extremely well choreographed. (also picked at the
 * start)"): the bare-handed styles a player picks on the start menu. Each has a four-blow M1 string, a right-click signature and two
 * specials that open with mastery ({@link StyleMove}). Any side may learn any style; {@link #leaning} only says who it is known for.
 * Sent by ordinal (both sides run the same version), saved by {@link #code} (fixed for good: a style removed never shifts the others'
 * saves). 3.5.7 (the owner: "Remove FIshman karate from the game entirely"): Fishman Karate is gone (its save code 3 reads as no style);
 * (the owner: "Remove all abilities from Iron Fist, we're making this shit anew"): Iron Fist has no moves until it is remade.
 */
public enum FightingStyle {
    NONE(0, "none", "Bare Hands", Faction.UNSET, 0xFFB0B0B0, "No style yet: the game's own punch."),
    BLACK_LEG(1, "black_leg", "Black Leg", Faction.PIRATE, 0xFFE8743A,
        "A cook's kicking art: the hands never touch the fight. Snapping kicks, a spinning string, a neck shot that launches and legs "
            + "that catch fire."),
    SIX_POWERS(2, "six_powers", "Six Powers", Faction.MARINE, 0xFF7CC4FF,
        "The marines' superhuman art: finger pistols, legs that cut the air, a step so fast it vanishes and a body hard as iron."),
    // code 3 was Fishman Karate (removed in 3.5.7): never give it to another style
    BRAWLER(4, "brawler", "Iron Fist", Faction.UNSET, 0xFFD94848,
        "Pirate street boxing, being made anew: no moves yet, the game's own punch until then."),
    /** 2.1.4: the sword, one-handed (the owner: "the entire moveset is now right handed only"); its moves are {@code sword/SwordMove}. */
    SWORDSMAN(5, "swordsman", "Swordsman", Faction.UNSET, 0xFF6F8BAE,
        "One katana, the right hand: a draw that cuts before the eye sees it, a string of heavy cuts on the ground, on the run and in "
            + "the air, and a charged slash that, with Haki, flies and splits the ground.");

    private final int code;
    private final String id;
    private final String display;
    private final Faction leaning;
    private final int colour;
    private final String blurb;

    FightingStyle(int code, String id, String display, Faction leaning, int colour, String blurb) {
        this.code = code;
        this.id = id;
        this.display = display;
        this.leaning = leaning;
        this.colour = colour;
        this.blurb = blurb;
    }

    public String id() {
        return this.id;
    }

    /** 3.5.7: its number in saves (a player's profile, a sea fighter): fixed for good. */
    public int code() {
        return this.code;
    }

    /** The style a save's number names (a removed style's, or none: no style). */
    public static FightingStyle byCode(int code) {
        for (FightingStyle style : values()) {
            if (style.code == code) {
                return style;
            }
        }
        return NONE;
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

    /**
     * 2.1.2: the stance it holds in combat mode (R): an arms-only loop, {@code tools/style_anims.py}. 2.1.3: only the relaxed ones
     * (Black Leg's and Six Powers' hands behind the back); Iron Fist holds none (null), its arms hang and swing as
     * the game's do until a blow raises them (the owner: "Selecting a combat style must not permanently activate an attacking pose").
     */
    public String stance() {
        return this == BLACK_LEG || this == SIX_POWERS || this == SWORDSMAN ? "style_" + this.id + "_stance" : null;
    }

    /**
     * 3.5.7: whether it has moves to fight with (Iron Fist has none until it is remade): a style without them leaves the hands, combat
     * mode and R to the game, as no style does.
     */
    public boolean armed() {
        if (this.sword()) {
            return true;
        }
        for (StyleMove move : StyleMove.values()) {
            if (move.style() == this) {
                return true;
            }
        }
        return false;
    }

    /** 2.1.4: a style that fights with a katana (its moves are SwordMove's, not StyleMove's). */
    public boolean sword() {
        return this == SWORDSMAN;
    }

    public static FightingStyle byOrdinal(int ordinal) {
        FightingStyle[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : NONE;
    }

    /** The styles a player may pick. */
    public static FightingStyle[] choices() {
        return new FightingStyle[] {BLACK_LEG, SIX_POWERS, BRAWLER, SWORDSMAN};
    }
}
