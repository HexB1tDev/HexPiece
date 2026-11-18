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
        gestures();
        gestureClearance();
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

    /**
     * 2.4.3: no gesture puts a straight arm through the head, in the vanilla model's own geometry (model pixels, y down, the face at -z;
     * each part turned Rz(roll) Ry(yaw) Rx(pitch) about its pivot: the head's (0, 0, 0), the arms' (-5, 2, 0) and (5, 2, 0), each arm's
     * middle line 1 px out from its pivot, from 2 px above it to 10 below), each part at its weight over a fighter standing still: an
     * arm's middle line, past the shoulder joint, may not be inside the head's box drawn in by {@value #HEAD_IN} px (the arm is 4 thick:
     * 2.4 px into the head). The lint of tools/gesture_preview.py; the old LOOKOUT had its hand 1.3 px deep in the face.
     */
    static final double HEAD_IN = 0.4;

    static void gestureClearance() {
        for (com.hexhaki.piece.npc.Gesture g : com.hexhaki.piece.npc.Gesture.values()) {
            if (g == com.hexhaki.piece.npc.Gesture.NONE) {
                continue;
            }
            double worst = -99.0;
            for (float t = 0.0F; t <= g.ticks; t += 0.5F) {
                float[][] p = com.hexhaki.piece.npc.GesturePose.pose(g, t);
                double[][] head = turn(p[1]);
                for (int side = 0; side < 2; side++) {
                    float[] arm = p[2 + side];
                    double[][] r = turn(arm);
                    double out = side == 0 ? -1.0 : 1.0;
                    for (int k = 5; k <= 10; k++) {
                        double[] local = {out, -2.0 + 12.0 * k / 10.0, 0.0};
                        double[] at = times(r, local);
                        at[0] += side == 0 ? -5.0 : 5.0;
                        at[1] += 2.0;
                        // into the head's own frame (its turn undone), measured from its box's middle (0, -4, 0)
                        double[] h = {head[0][0] * at[0] + head[1][0] * at[1] + head[2][0] * at[2],
                            head[0][1] * at[0] + head[1][1] * at[1] + head[2][1] * at[2],
                            head[0][2] * at[0] + head[1][2] * at[1] + head[2][2] * at[2]};
                        double depth = Math.min(4.0 - Math.abs(h[0]), Math.min(4.0 - Math.abs(h[1] + 4.0), 4.0 - Math.abs(h[2])));
                        worst = Math.max(worst, depth);
                    }
                }
            }
            Check.that(worst < HEAD_IN, g + ": no arm through the head (deepest " + String.format("%.2f", worst) + " px)");
        }
    }

    /** A part's turn at its weight (degrees, Rz(roll) Ry(yaw) Rx(pitch)), as a matrix. */
    static double[][] turn(float[] part) {
        double w = part[com.hexhaki.piece.npc.GesturePose.WEIGHT];
        double x = Math.toRadians(part[3] * w);
        double y = Math.toRadians(part[4] * w);
        double z = Math.toRadians(part[5] * w);
        double[][] rx = {{1, 0, 0}, {0, Math.cos(x), -Math.sin(x)}, {0, Math.sin(x), Math.cos(x)}};
        double[][] ry = {{Math.cos(y), 0, Math.sin(y)}, {0, 1, 0}, {-Math.sin(y), 0, Math.cos(y)}};
        double[][] rz = {{Math.cos(z), -Math.sin(z), 0}, {Math.sin(z), Math.cos(z), 0}, {0, 0, 1}};
        return product(rz, product(ry, rx));
    }

    static double[][] product(double[][] a, double[][] b) {
        double[][] c = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    c[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return c;
    }

    static double[] times(double[][] m, double[] v) {
        return new double[] {m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2], m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2],
            m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2]};
    }

    /** 2.3.3: the NPCs' idle gestures (HexGhoul's ambient animations, the owner's request). */
    static void gestures() {
        java.util.Set<com.hexhaki.piece.npc.Gesture> seen = new java.util.HashSet<>();
        for (com.hexhaki.piece.Faction side : new com.hexhaki.piece.Faction[] {com.hexhaki.piece.Faction.PIRATE, com.hexhaki.piece.Faction.MARINE}) {
            for (int rank = 0; rank < 3; rank++) {
                java.util.Set<com.hexhaki.piece.npc.Gesture> ranked = new java.util.HashSet<>();
                for (double roll = 0.0; roll < 1.0; roll += 0.002) {
                    com.hexhaki.piece.npc.Gesture g = com.hexhaki.piece.npc.Gesture.pick(side, rank, roll);
                    Check.that(g != com.hexhaki.piece.npc.Gesture.NONE, "an idle fighter always picks a real gesture");
                    Check.that(side == com.hexhaki.piece.Faction.MARINE || g != com.hexhaki.piece.npc.Gesture.SALUTE
                        && g != com.hexhaki.piece.npc.Gesture.EASE, "pirates neither salute nor stand at ease");
                    seen.add(g);
                    ranked.add(g);
                }
                // 2.4.3: a persona to each rank, each only its own
                Check.that(ranked.contains(com.hexhaki.piece.npc.Gesture.FIDGET) == (rank == 0)
                    && ranked.contains(com.hexhaki.piece.npc.Gesture.SCUFF) == (rank == 0), side + " rank " + rank + ": only a recruit fidgets");
                Check.that(ranked.contains(com.hexhaki.piece.npc.Gesture.ORDER) == (rank == 1), side + " rank " + rank + ": only an officer orders");
                Check.that(ranked.contains(com.hexhaki.piece.npc.Gesture.SWAGGER) == (rank == 2)
                    && ranked.contains(com.hexhaki.piece.npc.Gesture.BRUSH) == (rank == 2), side + " rank " + rank + ": only a captain swaggers");
                for (com.hexhaki.piece.npc.Gesture g : com.hexhaki.piece.npc.Gesture.values()) {
                    Check.that(com.hexhaki.piece.npc.Gesture.weight(side, rank, g) >= 0, g + ": no weight below nothing");
                }
            }
            Check.that(com.hexhaki.piece.npc.Gesture.weight(com.hexhaki.piece.Faction.MARINE, 2, com.hexhaki.piece.npc.Gesture.SALUTE)
                < com.hexhaki.piece.npc.Gesture.weight(com.hexhaki.piece.Faction.MARINE, 0, com.hexhaki.piece.npc.Gesture.SALUTE),
                "a Marine captain salutes less than its recruits");
            Check.that(com.hexhaki.piece.npc.Gesture.pause(0.0) >= 100 && com.hexhaki.piece.npc.Gesture.pause(1.0) <= 400,
                "a pause of seconds between gestures");
        }
        Check.that(seen.size() == com.hexhaki.piece.npc.Gesture.values().length - 1, "every gesture comes up");
        for (com.hexhaki.piece.npc.Gesture g : com.hexhaki.piece.npc.Gesture.values()) {
            if (g == com.hexhaki.piece.npc.Gesture.NONE) {
                continue;
            }
            Check.that(g.ticks >= 30, g + ": long enough to be seen");
            float[][] start = com.hexhaki.piece.npc.GesturePose.pose(g, 0.0F);
            float[][] end = com.hexhaki.piece.npc.GesturePose.pose(g, g.ticks);
            float[][] after = com.hexhaki.piece.npc.GesturePose.pose(g, g.ticks + 1.0F);
            for (int part = 0; part < com.hexhaki.piece.npc.GesturePose.PARTS; part++) {
                Check.that(start[part][com.hexhaki.piece.npc.GesturePose.WEIGHT] < 1.0E-4F
                    && end[part][com.hexhaki.piece.npc.GesturePose.WEIGHT] < 1.0E-4F
                    && after[part][com.hexhaki.piece.npc.GesturePose.WEIGHT] == 0.0F, g + ": eases in from and out to the fighter's own pose");
            }
            for (float t = 0.0F; t <= g.ticks; t += 0.5F) {
                float[][] p = com.hexhaki.piece.npc.GesturePose.pose(g, t);
                for (float[] part : p) {
                    for (float v : part) {
                        Check.that(Float.isFinite(v), g + ": every value a number");
                    }
                }
                Check.that(Math.abs(p[2][3]) <= 175.0F && Math.abs(p[3][3]) <= 175.0F, g + ": no arm swung over the top");
                Check.that(Math.abs(p[1][3]) <= 30.0F && Math.abs(p[1][4]) <= 45.0F && Math.abs(p[1][5]) <= 20.0F, g + ": a head a neck can turn");
            }
            float[][] mid = com.hexhaki.piece.npc.GesturePose.pose(g, g.ticks * 0.5F);
            Check.that(mid[2][com.hexhaki.piece.npc.GesturePose.WEIGHT] > 0.99F, g + ": fully shown in its middle");
        }
    }
}
