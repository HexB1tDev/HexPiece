package com.hexhaki.piece;

import com.hexhaki.fruit.Fruit;

/**
 * 3.2.0 (the owner: "We're going to have to evaluate them in rarity, like in the name and everything. It's going to have like a rank, like
 * between common, uncommon, rare, legendary, and mythical"; asked, he took the proposal: Mythical the Nika fruit and the Uo Uo no Mi,
 * Legendary the Gura Gura, Pika Pika, Magu Magu and Hie Hie no Mi, the lower ranks kept for fruits to come): every fruit's rank, its
 * colour (the fruit's name is written in it: the language file's names carry the colour code) and its line in the tooltip
 * (client/FruitTooltip). Pure.
 */
public final class FruitRanks {
    private FruitRanks() {
    }

    /** A rank: its name, the colour code its fruits' names are written in, its stars. */
    public enum Rank {
        COMMON("Common", "§f", 1),
        UNCOMMON("Uncommon", "§a", 2),
        RARE("Rare", "§9", 3),
        LEGENDARY("Legendary", "§6", 4),
        MYTHICAL("Mythical", "§d", 5);

        public final String label;
        public final String code;
        public final int stars;

        Rank(String label, String code, int stars) {
            this.label = label;
            this.code = code;
            this.stars = stars;
        }

        /** Its tooltip line: its stars and its name, in its colour. */
        public String line() {
            return this.code + "★".repeat(this.stars) + "§8" + "★".repeat(5 - this.stars) + " " + this.code + "§l" + this.label;
        }
    }

    /** The Nika fruit's rank (it is the original's own item, not one of the enum's fruits). */
    public static final Rank NIKA = Rank.MYTHICAL;

    /** {@code fruit}'s rank (null: no fruit). */
    public static Rank of(Fruit fruit) {
        if (fruit == null || fruit == Fruit.NONE) {
            return null;
        }
        if (fruit == Fruit.UO) {
            return Rank.MYTHICAL;
        }
        if (fruit == Fruit.NORO) {
            return Rank.COMMON; // 3.3.4 (the owner: "New common Fruits ... Noro Noro No Mi (Common)")
        }
        if (fruit == Fruit.HORO) {
            return Rank.COMMON; // 3.3.6 (the owner: "Horo Horo No Mi (common)")
        }
        if (fruit == Fruit.ITO) {
            return Rank.RARE; // 3.3.7 (the owner: "New Rare Fruit: Ito Ito No Mi (Rare)")
        }
        if (fruit == Fruit.TORI) {
            return Rank.MYTHICAL; // 3.6.9 (the owner: "Mythical Fruit: Tori Tori no Mi, Model: Phoenix")
        }
        if (fruit == Fruit.NEKO) {
            return Rank.LEGENDARY; // 3.7.9 (the owner: "New Fruit: Legendary Leopard Fruit, Name: Neko Neko No Mi")
        }
        if (fruit == Fruit.MOCHI) {
            return Rank.LEGENDARY; // 3.5.5 (the owner: "research katakuris mochi fruit and add it ... Legendary fruit")
        }
        if (fruit == Fruit.SUPA) {
            return Rank.UNCOMMON; // 3.3.5 (the owner: "Supa Supa No Mi (UnCommon)")
        }
        return Rank.LEGENDARY; // Gura, Pika, Magu, Hie
    }
}
