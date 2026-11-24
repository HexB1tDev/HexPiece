package com.hexhaki.piece;

/** 2.1.1: the numbers of HexPiece's own progression (pure, tested in {@code PieceTests}). */
public final class PieceRules {
    /** A fighting style's mastery runs 0..MAX (as the Haki paths do). */
    public static final int MAX_MASTERY = 1000;
    /** How much harder a style hits at full mastery than at none. */
    public static final float MASTERY_DAMAGE = 0.6F;

    /** A pirate's titles by bounty (berries) and a marine's ranks by merit: {threshold, name}. */
    static final Object[][] PIRATE_TITLES = {
        {0L, "Nobody"}, {1_000_000L, "Rookie"}, {30_000_000L, "Wanted Pirate"}, {100_000_000L, "Supernova"},
        {300_000_000L, "Notorious Captain"}, {1_000_000_000L, "Warlord of the Sea"}, {3_000_000_000L, "Emperor of the Sea"}};
    static final Object[][] MARINE_RANKS = {
        {0L, "Seaman Recruit"}, {10L, "Seaman"}, {40L, "Petty Officer"}, {100L, "Ensign"}, {220L, "Lieutenant"},
        {400L, "Commander"}, {700L, "Captain"}, {1100L, "Commodore"}, {1700L, "Rear Admiral"}, {2500L, "Vice Admiral"},
        {4000L, "Admiral"}, {7000L, "Fleet Admiral"}};

    private PieceRules() {
    }

    public static int clampMastery(int mastery) {
        return Math.max(0, Math.min(MAX_MASTERY, mastery));
    }

    /** The damage multiplier a style's mastery gives (1 at none, 1.6 at full). */
    public static float masteryDamage(int mastery) {
        return 1.0F + MASTERY_DAMAGE * clampMastery(mastery) / (float) MAX_MASTERY;
    }

    /** Style mastery gained by landing a blow: more for heavier moves, less as the style nears its peak. */
    public static int masteryGain(int mastery, int weight) {
        int base = Math.max(1, weight);
        return mastery >= 800 ? Math.max(1, base / 2) : base;
    }

    /** The title or rank for a side's renown. */
    public static String title(Faction faction, long renown) {
        Object[][] table = faction == Faction.MARINE ? MARINE_RANKS : faction == Faction.PIRATE ? PIRATE_TITLES : null;
        if (table == null) {
            return "Drifter";
        }
        String best = (String) table[0][1];
        for (Object[] row : table) {
            if (renown >= (Long) row[0]) {
                best = (String) row[1];
            }
        }
        return best;
    }

    /** The next title's threshold above {@code renown}, or -1 at the top. */
    public static long nextTitle(Faction faction, long renown) {
        Object[][] table = faction == Faction.MARINE ? MARINE_RANKS : PIRATE_TITLES;
        for (Object[] row : table) {
            if ((Long) row[0] > renown) {
                return (Long) row[0];
            }
        }
        return -1;
    }

    /** Renown for beating someone of the other side: their rank 0 (grunt), 1 (officer), 2 (captain), 3 a player. */
    public static long renownFor(Faction faction, int rank) {
        int r = Math.max(0, Math.min(3, rank));
        if (faction == Faction.PIRATE) {
            return new long[] {2_500_000L, 9_000_000L, 35_000_000L, 25_000_000L}[r];
        }
        if (faction == Faction.MARINE) {
            return new long[] {3L, 9L, 30L, 20L}[r];
        }
        return 0L;
    }

    /** Berries as the wanted posters print them: 1,234,000. */
    public static String berries(long amount) {
        return String.format(java.util.Locale.ROOT, "%,d", amount);
    }

    /**
     * 2.3.7: the Galaxy Fist (the original's Armament technique, its "convergence") is Iron Fist's alone, and opens with Ryou up (the
     * owner: "Galaxy fist becomes a move of Iron fist that unlocks when the user activates RYOU haki. ONLY for iron fist."): no other
     * style, nor Iron Fist without Ryou, can throw it; with Ryou up no Armament mastery is asked for it. 3.5.7 (the owner: "Remove all
     * abilities from Iron Fist, we're making this shit anew"): gone with Iron Fist's other moves: no one throws it until it is remade.
     */
    public static boolean galaxyFist(com.hexhaki.piece.style.FightingStyle style, boolean ryou) {
        return false;
    }
}
