package com.hexhaki.piece.sword;

/**
 * 2.1.4: a katana's grade (pure), the swordsmiths' ranking of named blades (the owner: "give the sword beautiful description, tiers,
 * and rarities"). Its rarity colours the name (Minecraft's own rarities); its edge scales every Swordsman cut made with it.
 */
public enum Grade {
    WAZAMONO("Wazamono", "Graded Blade", 0, 1.0F, 0xC9D2DC),
    RYO_WAZAMONO("Ryo Wazamono", "Skillful Grade", 1, 1.1F, 0xF0D264),
    O_WAZAMONO("O Wazamono", "Great Grade", 2, 1.22F, 0x6BD8EC),
    SAIJO_O_WAZAMONO("Saijo O Wazamono", "Supreme Grade", 3, 1.36F, 0xD07CFF);

    private final String title;
    private final String english;
    private final int rarity;
    private final float edge;
    private final int colour;

    Grade(String title, String english, int rarity, float edge, int colour) {
        this.title = title;
        this.english = english;
        this.rarity = rarity;
        this.edge = edge;
        this.colour = colour;
    }

    /** The tooltip's grade line colour (RGB), a softer shade of the rarity's own. */
    public int colour() {
        return this.colour;
    }

    public String title() {
        return this.title;
    }

    public String english() {
        return this.english;
    }

    /** Minecraft's rarity: 0 common (white), 1 uncommon (yellow), 2 rare (aqua), 3 epic (magenta). */
    public int rarity() {
        return this.rarity;
    }

    /** What the grade multiplies a Swordsman cut's damage by. */
    public float edge() {
        return this.edge;
    }
}
