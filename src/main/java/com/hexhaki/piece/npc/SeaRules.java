package com.hexhaki.piece.npc;

import com.hexhaki.piece.Faction;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.sword.Katana;

/**
 * 2.1.1: the pirates' and marines' numbers (pure, tested): their looks (tools/npc_skins.py paints <side>_<look>.png in this order), ranks,
 * styles, toughness, and how often patrols come.
 */
public final class SeaRules {
    /** Each look: {rank, female (1) or not (0)}; the same eight for both sides. */
    public static final int[][] LOOKS = {{0, 0}, {0, 0}, {0, 1}, {0, 1}, {1, 0}, {1, 1}, {2, 0}, {2, 1}};
    public static final String[][] TITLES = {{"Pirate", "Pirate Officer", "Pirate Captain"}, {"Marine", "Marine Lieutenant", "Marine Captain"}};
    /** Health, speed and style mastery by rank (grunt, officer, captain). */
    public static final double[] HEALTH = {26.0, 44.0, 80.0};
    public static final double[] SPEED = {0.3, 0.31, 0.32};
    public static final int[] MASTERY = {150, 450, 800};
    /** Patrols: one is tried for each player every PATROL_EVERY ticks with PATROL_CHANCE, never past CROWD fighters within CROWD_RANGE. */
    public static final int PATROL_EVERY = 600;
    public static final float PATROL_CHANCE = 0.22F;
    public static final int CROWD = 6;
    public static final double CROWD_RANGE = 64.0;
    public static final double PATROL_NEAR = 26.0;
    public static final double PATROL_FAR = 40.0;
    /** How far a fighter sees a foe, and where it gives up the chase. */
    public static final double SIGHT = 18.0;
    public static final double CHASE = 32.0;
    /** How close it closes in before throwing a blow. */
    public static final double STRIKE = 2.7;

    private SeaRules() {
    }

    public static boolean female(int look) {
        return LOOKS[Math.floorMod(look, LOOKS.length)][1] == 1;
    }

    public static int rankOf(int look) {
        return LOOKS[Math.floorMod(look, LOOKS.length)][0];
    }

    /** A look of {@code rank}, the {@code pick}-th of those that have it (wrapping). */
    public static int look(int rank, int pick) {
        int count = 0;
        for (int[] look : LOOKS) {
            if (look[0] == rank) {
                count++;
            }
        }
        int wanted = Math.floorMod(pick, Math.max(1, count));
        for (int i = 0; i < LOOKS.length; i++) {
            if (LOOKS[i][0] == rank && wanted-- == 0) {
                return i;
            }
        }
        return 0;
    }

    /**
     * The styles a side fights with: pirates kick, box, do karate and (2.1.4) carry katanas (two in five); marines use the Six Powers
     * and box (unchanged).
     */
    public static FightingStyle[] styles(Faction side) {
        return side == Faction.MARINE ? new FightingStyle[] {FightingStyle.SIX_POWERS, FightingStyle.SIX_POWERS, FightingStyle.BRAWLER}
            : new FightingStyle[] {FightingStyle.BLACK_LEG, FightingStyle.BRAWLER, FightingStyle.FISHMAN_KARATE, FightingStyle.SWORDSMAN,
                FightingStyle.SWORDSMAN};
    }

    /** 2.1.4: a pirate swordsman's katana: the grade by rank (grunts the lower grades, captains the great ones), the blade by pick. */
    public static Katana katana(int rank, int pick) {
        java.util.List<Katana> fit = new java.util.ArrayList<>();
        for (Katana katana : Katana.values()) {
            int grade = katana.grade().rarity();
            if (rank == 0 ? grade <= 1 : rank == 1 ? grade >= 1 && grade <= 2 : grade >= 2) {
                fit.add(katana);
            }
        }
        return fit.get(Math.floorMod(pick, fit.size()));
    }

    public static String title(Faction side, int rank) {
        return TITLES[side == Faction.MARINE ? 1 : 0][Math.max(0, Math.min(2, rank))];
    }

    /** A patrol's ranks: two or three grunts, sometimes an officer, now and then a captain. */
    public static int[] patrol(double roll, double size) {
        int grunts = size < 0.5 ? 2 : 3;
        if (roll < 0.08) {
            return new int[] {2, 1, 0};
        }
        if (roll < 0.35) {
            int[] ranks = new int[grunts];
            ranks[0] = 1;
            return ranks;
        }
        return new int[grunts];
    }
}
