package com.hexhaki.piece.armor;

/**
 * 2.3.1: the owner's One Piece armor (input/OnePiece_Armor_1.20.1.zip, never altered; its model and client hook are translated from it by
 * build.py into {@code client/armor}): the owner, "New armor for NPCS and for the player (create a custom tab in creative as well), NPCS
 * for now only spawn with the Marine Armor (not admiral or strawhat or black coat)". Pure: the pieces, their slots, the pack's model
 * style and texture for each (its INTEGRATION.md table), and which a Marine NPC wears.
 */
public enum PieceArmor {
    MARINE_CAP("marine_cap", "Marine Cap", Slot.HEAD, "MARINE", "marine_outfit"),
    MARINE_SHIRT("marine_shirt", "Marine Shirt", Slot.CHEST, "MARINE", "marine_outfit"),
    MARINE_TROUSERS("marine_trousers", "Marine Trousers", Slot.LEGS, "MARINE", "marine_outfit"),
    MARINE_BOOTS("marine_boots", "Marine Boots", Slot.FEET, "MARINE", "marine_outfit"),
    ADMIRAL_COAT("admiral_coat", "Admiral's Coat", Slot.CHEST, "ADMIRAL", "admiral_coat"),
    STRAW_HAT("straw_hat", "Straw Hat", Slot.HEAD, "STRAW_HAT", "straw_hat"),
    BLACKBEARD_COAT("blackbeard_coat", "Blackbeard's Coat", Slot.CHEST, "BLACKBEARD", "blackbeard_coat");

    /** Where a piece is worn (the game's armor slots, head to feet). */
    public enum Slot {
        HEAD, CHEST, LEGS, FEET
    }

    private final String key;
    private final String display;
    private final Slot slot;
    private final String style;
    private final String texture;

    PieceArmor(String key, String display, Slot slot, String style, String texture) {
        this.key = key;
        this.display = display;
        this.slot = slot;
        this.style = style;
        this.texture = texture;
    }

    /** Its item id ({@code hexhaki:<key>}) and model/icon name. */
    public String key() {
        return this.key;
    }

    public String display() {
        return this.display;
    }

    public Slot slot() {
        return this.slot;
    }

    /** The pack's {@code OnePieceArmorModel.Style} constant it is drawn with. */
    public String style() {
        return this.style;
    }

    /** The texture the game draws it with (the pack's atlas, packed under hexhaki by build.py). */
    public String texture() {
        return "hexhaki:textures/models/armor/" + this.texture + ".png";
    }

    /** The texture's file name in the pack. */
    public String textureFile() {
        return this.texture + ".png";
    }

    /** True for what a Marine NPC spawns wearing (the owner: only the Marine armor; never the admiral's, the straw hat or the black coat). */
    public boolean marineIssue() {
        return this.style.equals("MARINE");
    }

    /** The Marine outfit, head to feet (what a Marine NPC spawns in). */
    public static PieceArmor[] marineOutfit() {
        return new PieceArmor[] {MARINE_CAP, MARINE_SHIRT, MARINE_TROUSERS, MARINE_BOOTS};
    }
}
