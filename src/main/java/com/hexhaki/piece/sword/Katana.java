package com.hexhaki.piece.sword;

/**
 * 2.1.4: the owner's 25 katanas (pure), in the order of their poster ("Hex / Katana Atelier": "Twenty-five blades. Twenty-five saya."):
 * the model key (the blade {@code <key>} and its saya {@code <key>_sheath} in input/Hex_Katana_Atelier_1.20.1.zip, one atlas
 * {@code <key>.png}), the name, the hamon (the temper line the poster names under each blade), the grade and a description. Sent and
 * saved by key; new blades go at the end (2.2.7: the owner's legendary Gryphon and Yoru, from their own zip).
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
        "A temper like moonlight on still water, pale ripples over dark steel. Drawn only at night, its owners say, and sheathed before dawn."),
    // 2.2.7: the owner's two legendary blades (input/Yoru_Gryphon_Models_Textures_1.20.1.zip): "these are the 2 strongest blades in the
    // game, YORU is the strongest". Held and swung as a katana is; drawn the moment combat mode comes on and put away only when it goes
    // off (or the fruit is taken up); Yoru worn on the back and longer in reach, never blackened by Haki.
    GRYPHON("gryphon", "Gryphon", "Saber", Grade.SAIJO_O_WAZAMONO,
        "A western saber under a gold bell guard and knucklebow, its scabbard lacquered blue. Its master is said to have split the sky over a "
            + "war with a single swing, and to have never needed a second.", Carry.HIP, 1.55F, 0.3),
    YORU("yoru", "Yoru", "Kokuto", Grade.SAIJO_O_WAZAMONO,
        "The black blade: a cross-hilted great sword as tall as a man, carried on the back. The finest sword in the world, gone black from a "
            + "lifetime of Haki: there is no blacker left to give it, and the lightning only runs round it.", Carry.BACK, 1.75F, 0.75);

    /** 2.2.7: where a blade is worn: in its saya at the left hip, or tied on the back with no saya (Yoru). */
    public enum Carry {
        HIP,
        BACK
    }

    private final String key;
    private final String display;
    private final String hamon;
    private final Grade grade;
    private final String description;
    private final Carry carry;
    private final float edge;
    private final double reach;

    Katana(String key, String display, String hamon, Grade grade, String description) {
        this(key, display, hamon, grade, description, Carry.HIP, grade.edge(), 0.0);
    }

    Katana(String key, String display, String hamon, Grade grade, String description, Carry carry, float edge, double reach) {
        this.key = key;
        this.display = display;
        this.hamon = hamon;
        this.grade = grade;
        this.description = description;
        this.carry = carry;
        this.edge = edge;
        this.reach = reach;
    }

    /** 2.2.7: where it is worn. */
    public Carry carry() {
        return this.carry;
    }

    /**
     * 3.8.6 (the owner's standing rule: "Different Animations for Different Weapons. Katanas Category (stays as it is) Saber Caterogy
     * (Similar to Katanas but more aggresion, the idle and unsheath animation stay as it is (Gryphon)) Mace Category ... Yoru Category
     * (Yoru is a one of a kind legendary Blade, stays as it is, and is uncontested the strongest blade that will ever be added) Gun ...
     * Sort every Katana into the category of katana Gryphon into saber and yoru into yoru"): the weapon categories, each made with its
     * own animations. Every weapon the owner gives from now on comes with one.
     */
    public enum Category {
        KATANA("Katana"), SABER("Saber"), MACE("Mace"), YORU("Yoru"), GUN("Gun");

        public final String label;

        Category(String label) {
            this.label = label;
        }
    }

    /** 3.8.6: this blade's category: Gryphon a Saber, Yoru the Yoru, every other a Katana. */
    public Category category() {
        return switch (this) {
            case GRYPHON -> Category.SABER;
            case YORU -> Category.YORU;
            default -> Category.KATANA;
        };
    }

    /** 2.2.7: one of the owner's legendary blades (above every graded katana, never carried by a sea patrol, always drawn in a fight). */
    public boolean legend() {
        return this == GRYPHON || this == YORU;
    }

    /** 2.2.7: what it multiplies a Swordsman cut by (its grade's, or a legendary blade's own). */
    public float edge() {
        return this.edge;
    }

    /**
     * 2.2.7, 2.4.5 (the owner: "Different swords should have different range (Yoru is the biggest)"): blocks its cuts reach past the
     * blade every cut's path is written with (every blade plays the katanas' one animation set, so range is the blade's, not the
     * animation's): its own length past that blade's ({@link #tip()} against {@link SwordRules#PATH_TIP}, in the player's scale), its
     * grade's reach ({@link Grade#reach()}) and a legendary blade's own (Gryphon 0.3, Yoru 0.75). Yoru reaches furthest (about 1.86),
     * then Gryphon (0.84), then the supreme, great and skillful grades; a short plain blade a little less than the path's own.
     */
    public double reach() {
        return this.reach + this.grade.reach() + (this.tip() * this.size() - SwordRules.PATH_TIP * SIZE) * SwordRules.PX;
    }

    /**
     * 2.6.2 (the owner: "increase the overall size of the katanas, and gryphon I find them too small. even on the hand they're small"):
     * every blade but Yoru is drawn SIZE times its mesh, in the hand, at the hip and in first person (the animations and the cuts' paths
     * solved for it: tools/sword_rig.py SIZE); Yoru, 2.75 blocks already, as it is.
     */
    public static final float SIZE = 1.25F;

    /** 2.6.2: how many times its mesh this blade is drawn ({@link #SIZE}; Yoru 1). */
    public float size() {
        return this.carry == Carry.BACK ? 1.0F : SIZE;
    }

    /** 2.4.5: model px from the guard to the point, measured from the owner's mesh of this blade (build.py checks them). */
    public float tip() {
        return switch (this) {
            case CRIMSON_DAWN -> 20.90F;
            case SILVER_CRANE -> 22.20F;
            case OBSIDIAN_ECLIPSE -> 20.30F;
            case AZURE_TIDE -> 22.50F;
            case JADE_SERPENT -> 23.30F;
            case SAKURA_VEIL -> 20.20F;
            case AMBER_WASP -> 19.10F;
            case FROSTGLASS -> 21.50F;
            case ROYAL_AMETHYST -> 23.60F;
            case COPPER_RONIN -> 20.60F;
            case COBALT_STORM -> 21.90F;
            case LOTUS_BLOOM -> 20.70F;
            case BLOOD_MOON -> 22.80F;
            case PEARL_HERON -> 23.10F;
            case SUNFIRE -> 21.30F;
            case BLACKWATER -> 24.00F;
            case EMERALD_THORN -> 20.50F;
            case ROSEGOLD_WHISPER -> 22.10F;
            case ASHEN_WOLF -> 20.10F;
            case STARFALL -> 22.90F;
            case DESERT_MIRAGE -> 22.60F;
            case VENOM_ORCHID -> 21.40F;
            case DRAGONBONE -> 23.50F;
            case IMPERIAL_PHOENIX -> 24.30F;
            case MIDNIGHT_LOTUS -> 22.40F;
            case GRYPHON -> 22.44F;
            case YORU -> 32.20F;
        };
    }

    /** 2.2.7: whether Armament Haki blackens it (Yoru is black already: only its lightning shows). */
    public boolean blackens() {
        return this != YORU;
    }

    /** 2.2.7: the tooltip's second line ("Hamon: ..." for a katana). */
    public String kindLine() {
        // 3.8.6: led by its category
        return switch (this) {
            case GRYPHON -> "Saber  ·  the second strongest blade";
            case YORU -> "Yoru  ·  Kokuto (black blade)  ·  the strongest blade";
            default -> "Katana  ·  Hamon: " + this.hamon;
        };
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
