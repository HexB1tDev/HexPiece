package com.hexhaki.piece;

import com.hexhaki.piece.npc.SeaRules;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Slot;
import com.hexhaki.piece.style.StyleMove;
import java.nio.file.Files;
import java.nio.file.Path;

/** 2.1.1: the pirates' and marines' looks, ranks, patrols and styles. */
public final class NpcTests {
    public static void main(String[] args) {
        for (String side : new String[] {"pirate", "marine"}) {
            for (int look = 0; look < SeaRules.LOOKS.length; look++) {
                Check.that(Files.exists(Path.of("src/main/resources/assets/hexhaki/textures/piece/npc/" + side + "_" + look + ".png")),
                    "every look has its skin: " + side + " " + look);
            }
        }
        for (int rank = 0; rank <= 2; rank++) {
            for (int pick = 0; pick < 12; pick++) {
                Check.that(SeaRules.rankOf(SeaRules.look(rank, pick)) == rank, "a look picked for a rank has that rank");
            }
            Check.that(SeaRules.HEALTH[rank] > 0 && SeaRules.MASTERY[rank] <= PieceRules.MAX_MASTERY, "rank numbers in range");
        }
        Check.that(SeaRules.HEALTH[2] > SeaRules.HEALTH[1] && SeaRules.HEALTH[1] > SeaRules.HEALTH[0], "higher ranks are tougher");
        boolean both = false;
        for (int[] look : SeaRules.LOOKS) {
            both |= look[1] == 1;
        }
        Check.that(both, "there are women among them");
        for (double roll = 0; roll < 1; roll += 0.01) {
            int[] ranks = SeaRules.patrol(roll, roll);
            Check.that(ranks.length >= 2 && ranks.length <= 3, "a patrol is two or three");
            int captains = 0;
            for (int rank : ranks) {
                captains += rank == 2 ? 1 : 0;
            }
            Check.that(captains <= 1, "at most one captain");
        }
        for (Faction side : new Faction[] {Faction.PIRATE, Faction.MARINE}) {
            for (FightingStyle style : SeaRules.styles(side)) {
                if (style.sword()) {
                    Check.that(side == Faction.PIRATE, "2.1.4: only pirates carry katanas (marines unchanged)");
                    continue; // its moves are the sword's (SwordTests)
                }
                for (Slot slot : Slot.values()) {
                    Check.that(StyleMove.of(style, slot) != null, "an NPC's style has every move");
                }
            }
            Check.that(SeaRules.title(side, 2).contains("Captain"), "the captain is named so");
        }
        // 2.1.4: pirates fight with Fishman Karate, Iron Fist and the sword; marines as before
        java.util.List<FightingStyle> pirates = java.util.Arrays.asList(SeaRules.styles(Faction.PIRATE));
        Check.that(pirates.contains(FightingStyle.FISHMAN_KARATE) && pirates.contains(FightingStyle.BRAWLER)
            && pirates.contains(FightingStyle.SWORDSMAN), "pirates use Fishman Karate, Iron Fist and Swordsman");
        Check.that(java.util.Arrays.equals(SeaRules.styles(Faction.MARINE), new FightingStyle[] {FightingStyle.SIX_POWERS,
            FightingStyle.SIX_POWERS, FightingStyle.BRAWLER}), "marines unchanged");
        for (int rank = 0; rank < 3; rank++) {
            for (int pick = 0; pick < 30; pick++) {
                com.hexhaki.piece.sword.Katana k = SeaRules.katana(rank, pick);
                int grade = k.grade().rarity();
                Check.that(rank == 0 ? grade <= 1 : rank == 1 ? grade >= 1 && grade <= 2 : grade >= 2, "a pirate's katana fits its rank");
            }
        }
        Check.done("NpcTests");
    }
}
