package com.hexhaki.piece.sword;

/**
 * 2.1.4: the owner's 25 katanas (pure), in the order of their poster ("Hex / Katana Atelier": "Twenty-five blades. Twenty-five saya."):
 * the model key (the blade {@code <key>} and its saya {@code <key>_sheath} in input/Hex_Katana_Atelier_1.20.1.zip, one atlas
 * {@code <key>.png}), the name, the hamon (the temper line the poster names under each blade), the grade and a description. Sent and
 * saved by key; new blades go at the end.
 */
public enum Katana {
    CRIMSON_DAWN("crimson_dawn", "Crimson Dawn", "Choji", Grade.RYO_WAZAMONO,
        "Forged at first light and quenched in a red tide, its clove-bud temper blooms along the edge like the sun breaking the sea."),
    SILVER_CRANE("silver_crane", "Silver Crane", "Suguha", Grade.WAZAMONO,
        "A plain straight temper, white as a crane's wing over still water. Swordsmen call it honest: it shows every mistake and forgives none."),
    OBSIDIAN_ECLIPSE("obsidian_eclipse", "Obsidian Eclipse", "Hitatsura", Grade.SAIJO_O_WAZAMONO,
        "Tempered over its whole face until the steel went dark as a moonless sky, scattered with pale islands of hard steel. It seems to drink the light around it."),
    AZURE_TIDE("azure_tide", "Azure Tide", "Notare", Grade.RYO_WAZAMONO,
        "Its temper rolls in long, slow swells, as the open sea does far from any shore. The blue saya was lacquered nine times, once for each voyage of its first owner."),
    JADE_SERPENT("jade_serpent", "Jade Serpent", "Saka Choji", Grade.RYO_WAZAMONO,
        "Its clove-bud temper leans forward along the edge like a serpent rising to strike. The green saya stays cool to the touch in any weather."),
    SAKURA_VEIL("sakura_veil", "Sakura Veil", "Gunome", Grade.WAZAMONO,
        "Rows of rounded temper waves, soft as petals drifting on a river. A gentle-looking blade; those who judged it by its colour did not judge for long."),
    AMBER_WASP("amber_wasp", "Amber Wasp", "Togari", Grade.WAZAMONO,
        "A temper of sharp peaks like a swarm's stingers, honey-coloured fittings and a black-and-gold wrap: a bite out of all proportion to its size."),
    FROSTGLASS("frostglass", "Frostglass", "Icebreak", Grade.RYO_WAZAMONO,
        "Its pale temper cracks across the edge like the first ice on a winter harbour, and the steel stays cold in the hand under any sun."),
    ROYAL_AMETHYST("royal_amethyst", "Royal Amethyst", "Toran", Grade.O_WAZAMONO,
        "Its temper rises in tall breaking waves. Made for a court that no longer stands, it outlived every crown it served."),
    COPPER_RONIN("copper_ronin", "Copper Ronin", "Midare", Grade.WAZAMONO,
        "A wild, irregular temper, no hand's width of it like another. It changed masters many times and kept the copper fittings of the first."),
    COBALT_STORM("cobalt_storm", "Cobalt Storm", "Inazuma", Grade.O_WAZAMONO,
        "Bright threads of lightning run through its temper, born in the quench the night a storm struck the forge."),
    LOTUS_BLOOM("lotus_bloom", "Lotus Bloom", "Ko Gunome", Grade.WAZAMONO,
        "A fine row of small rounded waves, close as the petals of a lotus. Many a swordsman's first true blade: light, forgiving and straight."),
    BLOOD_MOON("blood_moon", "Blood Moon", "Choji Midare", Grade.O_WAZAMONO,
        "Crowded clove buds under a lacquer the colour of an eclipsed moon. It has a long entry in the Marines' records and a short name among pirates."),
    PEARL_HERON("pearl_heron", "Pearl Heron", "Hososuguha", Grade.WAZAMONO,
        "A narrow, straight temper, a thread of light along the edge. Its pearl-white saya is said to be carved from a single whale bone."),
    SUNFIRE("sunfire", "Sunfire", "Flame", Grade.O_WAZAMONO,
        "Its temper climbs the edge in tongues like a rising blaze, under warm fittings of red and gold. Never once drawn by a calm hand."),
    BLACKWATER("blackwater", "Blackwater", "Billow", Grade.RYO_WAZAMONO,
        "Its temper heaves in dark, heavy billows, the sea before a storm. A blade for night crossings: its dark fittings throw no gleam to a lookout."),
    EMERALD_THORN("emerald_thorn", "Emerald Thorn", "Thorn", Grade.RYO_WAZAMONO,
        "Thorny peaks crowd its temper and the green saya is ridged like a briar stem. What it catches, it does not let go."),
    ROSEGOLD_WHISPER("rosegold_whisper", "Rosegold Whisper", "Sugu Notare", Grade.WAZAMONO,
        "A straight temper that loosens into a gentle wave near the point, under rose-gold fittings. Its cut is felt before it is heard."),
    ASHEN_WOLF("ashen_wolf", "Ashen Wolf", "Sambon Sugi", Grade.RYO_WAZAMONO,
        "Its temper rises in threes like cedars on a ridge. Grey as ash, made for a wandering swordsman who hunted alone."),
    STARFALL("starfall", "Starfall", "Starlight", Grade.O_WAZAMONO,
        "Specks of bright steel scatter through its temper like stars over the night sea. Navigators swear it was forged from the iron of a fallen star."),
    DESERT_MIRAGE("desert_mirage", "Desert Mirage", "Sudare", Grade.WAZAMONO,
        "Fine vertical lines hang in its temper like a bamboo blind, shimmering in strong light, under sand-coloured fittings from a far desert kingdom."),
    VENOM_ORCHID("venom_orchid", "Venom Orchid", "Poison Bloom", Grade.RYO_WAZAMONO,
        "Its temper blooms in dark flowers along the edge. The violet saya was made for a duelist who never lost and never smiled."),
    DRAGONBONE("dragonbone", "Dragonbone", "Fossil", Grade.SAIJO_O_WAZAMONO,
        "A temper like layered fossil, ancient beds of stone in the steel. Its ivory saya is carved from the bone of something no one has seen alive in a thousand years."),
    IMPERIAL_PHOENIX("imperial_phoenix", "Imperial Phoenix", "Phoenix", Grade.SAIJO_O_WAZAMONO,
        "Its temper spreads like the wings of a rising phoenix. An emperor's blade, gold at every fitting, broken and reforged three times, each time stronger."),
    MIDNIGHT_LOTUS("midnight_lotus", "Midnight Lotus", "Moonwater", Grade.O_WAZAMONO,
        "A temper like moonlight on still water, pale ripples over dark steel. Drawn only at night, its owners say, and sheathed before dawn.");

    private final String key;
    private final String display;
    private final String hamon;
    private final Grade grade;
    private final String description;

    Katana(String key, String display, String hamon, Grade grade, String description) {
        this.key = key;
        this.display = display;
        this.hamon = hamon;
        this.grade = grade;
        this.description = description;
    }

    public String key() {
        return this.key;
    }

    public String display() {
        return this.display;
    }

    public String hamon() {
        return this.hamon;
    }

    public Grade grade() {
        return this.grade;
    }

    public String description() {
        return this.description;
    }

    /** The katana with this model key, or null. */
    public static Katana byKey(String key) {
        for (Katana katana : values()) {
            if (katana.key.equals(key)) {
                return katana;
            }
        }
        return null;
    }

    public static Katana byOrdinal(int ordinal) {
        Katana[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : null;
    }
}
