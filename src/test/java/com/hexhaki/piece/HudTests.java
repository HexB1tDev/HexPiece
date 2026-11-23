package com.hexhaki.piece;

import com.hexhaki.piece.ui.AbilityHudView;
import com.hexhaki.piece.ui.AbilityPanel;
import com.hexhaki.piece.ui.EnergyBarView;
import com.hexhaki.piece.ui.UiCanvas;
import java.util.List;
import java.util.Map;

/** 2.2.2: the one abilities panel on the left (HexGhoul's): what it lists in each mode, and its card's size. */
public final class HudTests {
    static final Map<String, String> KEYS = Map.of("attack", "LMB", "use", "RMB", "special_a", "Y", "special_b", "U", "toggle", "R", "armament",
        "Ctrl+R", "observation", "V", "menu", "P");

    public static void main(String[] args) throws Exception {
        energy();
        nika();
        ranks();
        sword();
        style();
        fruit();
        off();
        layout();
        Check.done("HudTests");
    }

    static AbilityPanel.Input input(String style, boolean combat, String sword, String fruit, boolean fruitMode, boolean armament) {
        return new AbilityPanel.Input(KEYS, style, 0xFF6F8BAE, combat, List.of("Combo", "Heavy Kick", "Flame Kick", "Sky Walk"), sword, fruit,
            0xFFFFE070, fruitMode, List.of(new AbilityPanel.Move("G", "Light Beam", "", 30, 60), new AbilityPanel.Move("K", "Yata Mirror", "400", 0, 0)),
            true, armament, false, false, List.of(new AbilityPanel.Move("G", "King's Awakening (tap / hold)", "", 0, 0)), 60, 100);
    }

    static boolean has(AbilityPanel.Panel panel, String keys, String name) {
        for (AbilityPanel.Row row : panel.rows()) {
            if (row.keys().equals(keys) && row.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    static void sword() {
        AbilityPanel.Panel sheathed = AbilityPanel.of(input("Swordsman", true, "sheathed", null, false, false));
        Check.that(sheathed.title().equals("SWORDSMAN") && sheathed.subtitle().equals("katana sheathed"), "the Swordsman's panel says the katana is in its saya");
        Check.that(has(sheathed, "LMB", "Draw (slow)"), "sheathed: the attack key draws, slowly");
        Check.that(has(sheathed, "Ctrl+LMB", "Quick draw"), "the quick draw on Ctrl and the attack key");
        Check.that(has(sheathed, "RMB hold", "Charged cut"), "held right click: the charged cut");
        AbilityPanel.Panel drawn = AbilityPanel.of(input("Swordsman", true, "drawn", null, false, true));
        Check.that(has(drawn, "LMB", "Cut string") && has(drawn, "RMB hold", "Haki slash"), "drawn with Armament on: the string and the Haki slash");
        Check.that(has(drawn, "Ctrl+R", "Armament: on"), "Armament on Ctrl + R, lit");
        Check.near(drawn.energy(), 0.6, 1e-9, "the Haki energy under the title");
    }

    static void style() {
        AbilityPanel.Panel panel = AbilityPanel.of(input("Black Leg", true, null, "Pika Pika", false, false));
        Check.that(has(panel, "LMB", "Combo") && has(panel, "RMB", "Heavy Kick") && has(panel, "Y", "Flame Kick") && has(panel, "U", "Sky Walk"),
            "a style's string, heavy and two specials on their keys");
        Check.that(has(panel, "R", "Swap to Pika Pika"), "R swaps to the fruit");
        Check.that(has(panel, "G", "King's Awakening (tap / hold)"), "the Conqueror's quick slots");
        Check.that(has(panel, "P", "Captain's Log"), "the Captain's Log key");
    }

    static void fruit() {
        AbilityPanel.Panel panel = AbilityPanel.of(input("Black Leg", false, null, "Pika Pika", true, false));
        Check.that(panel.title().equals("PIKA PIKA"), "the fruit's own mode: its name");
        AbilityPanel.Row beam = panel.rows().get(0);
        Check.that(beam.name().equals("Light Beam") && !beam.locked() && Math.abs(beam.ready() - 0.5) < 1e-9, "a recharging move, half way");
        AbilityPanel.Row mirror = panel.rows().get(1);
        Check.that(mirror.locked() && mirror.needs().equals("400") && mirror.colour() == AbilityPanel.DIM, "a move not reached yet: dim, with what it needs");
        Check.that(has(panel, "R", "Back to Black Leg and Haki"), "R back to the style and Haki");
        Check.that(!has(panel, "Ctrl+R", "Armament"), "the fruit's panel is the fruit's alone: Haki is with the style");
    }

    static void off() {
        AbilityPanel.Panel panel = AbilityPanel.of(input("Six Powers", false, null, null, false, false));
        Check.that(panel.title().equals("SIX POWERS") && has(panel, "R", "Combat mode"), "combat mode off: the style and Haki, and the key that turns it on");
        Check.that(has(panel, "Ctrl+R", "Armament") && has(panel, "G", "King's Awakening (tap / hold)"), "Haki's keys listed with the style, in any form");
        Check.that(AbilityPanel.seconds(30).equals("1.5s") && AbilityPanel.seconds(400).equals("20s"), "recharges in seconds");
    }

    static void layout() {
        UiCanvas measure = new UiCanvas() {
            public void fill(int x0, int y0, int x1, int y1, int argb) {
            }

            public void gradient(int x0, int y0, int x1, int y1, int top, int bottom) {
            }

            public void text(String text, int x, int y, int argb, boolean shadow) {
            }

            public int width(String text) {
                return text.length() * 6;
            }

            public void face(int x, int y, int size) {
            }

            public void push() {
            }

            public void translate(float x, float y) {
            }

            public void scale(float factor) {
            }

            public void pop() {
            }
        };
        AbilityPanel.Panel panel = AbilityPanel.of(input("Swordsman", true, "drawn", null, false, true));
        AbilityHudView.Layout layout = AbilityHudView.layout(measure, panel);
        Check.that(layout.height() == 3 + 9 + 4 + panel.rows().size() * 10 + 3 - 1, "one line a move under the title and the gauge");
        int longest = 0;
        for (AbilityPanel.Row row : panel.rows()) {
            longest = Math.max(longest, measure.width(row.name()));
        }
        Check.that(layout.width() >= longest + layout.keyWidth(), "wide enough for every name after the keys");
        Check.that(layout.height() * AbilityHudFits.SCALE < 140, "small on screen: it fits above the chat");
    }

    /** The game's scale (client/AbilityHud.SCALE: 0.6), kept here so the pure test does not load the client class. */
    static final class AbilityHudFits {
        static final float SCALE = 0.6F;
    }

    /**
     * 3.2.0: the fruit energy bar (ui/EnergyBarView) draws only inside its place over the food bar (the food row's width, its emblem's
     * round at the right end, the leaf over it), whatever the energy, the white chip, the time and a refused move's shake.
     */
    static void energy() {
        int right = 200;
        int top = 100;
        int[] box = {right - EnergyBarView.WIDTH - 3, top - EnergyBarView.EMBLEM, right + EnergyBarView.EMBLEM / 2 + 4, top + EnergyBarView.HEIGHT + 4};
        for (double fill : new double[] {0.0, 0.05, 0.18, 0.5, 1.0}) {
            for (double denied : new double[] {0.0, 1.0}) {
                for (double time = 0; time < 40; time += 7) {
                    boolean[] inside = {true};
                    int[] drawn = {0};
                    UiCanvas canvas = new UiCanvas() {
                        @Override
                        public void fill(int x0, int y0, int x1, int y1, int argb) {
                            drawn[0]++;
                            inside[0] &= x0 >= box[0] && x1 <= box[2] && y0 >= box[1] && y1 <= box[3];
                        }

                        @Override
                        public void gradient(int x0, int y0, int x1, int y1, int top, int bottom) {
                            this.fill(x0, y0, x1, y1, top);
                        }

                        @Override
                        public void text(String text, int x, int y, int argb, boolean shadow) {
                            inside[0] = false;
                        }

                        @Override
                        public int width(String text) {
                            return text.length() * 6;
                        }

                        @Override
                        public void face(int x, int y, int size) {
                            inside[0] = false;
                        }

                        @Override
                        public void push() {
                        }

                        @Override
                        public void translate(float x, float y) {
                        }

                        @Override
                        public void scale(float factor) {
                        }

                        @Override
                        public void pop() {
                        }
                    };
                    EnergyBarView.draw(canvas, right, top, fill, Math.min(1.0, fill + 0.2), 0x9FE6FF, time, denied);
                    Check.that(drawn[0] > 40 && inside[0], "energy bar: drawn in its place (fill " + fill + ", denied " + denied + ", t " + time + ")");
                }
            }
        }
    }

    /**
     * 3.2.0 (the owner: the fruits' rarity "in the name and everything"; asked, Mythical the Nika fruit and the Uo Uo no Mi, Legendary the
     * rest): every fruit has its rank, its name in the language file is written in that rank's colour, the Devil Fruits tab's title is
     * there, and every fruit move with a cooldown has its energy cost listed (server/FruitEnergy).
     */
    /**
     * 3.2.3 (the owner, asked: "All stretch moves" work untransformed): the stretching punch, Rocket, Whip, Gatling, Giant and Coil
     * Spring (press and let go) go ahead out of Gear 5; Rubber World, Star Gun, Sky Bounce and the transformation's own toggle are
     * left to the original's rules.
     */
    static void nika() {
        java.util.Set<String> stretch = java.util.Set.of("PISTOL", "ROCKET_START", "ROCKET_RELEASE", "WHIP", "GATLING_START", "GATLING_RELEASE",
            "GIANT_START", "GIANT_RELEASE", "COIL_START", "COIL_RELEASE", "GIANT_GRAB");
        for (com.hexhaki.network.GearAction action : com.hexhaki.network.GearAction.values()) {
            Check.that(com.hexhaki.piece.server.NikaRules.stretch(action) == stretch.contains(action.name()),
                "nika: " + action + (stretch.contains(action.name()) ? " works untransformed" : " needs Gear 5"));
        }
        for (String needed : stretch) {
            Check.that(java.util.Arrays.stream(com.hexhaki.network.GearAction.values()).anyMatch(a -> a.name().equals(needed)),
                "nika: the original has " + needed);
        }
        // 3.2.6: the Bajrang Gun's slam clip turns the body and points the arm by gear/NikaSlam's very numbers (client/gear/NikaFist
        // draws the fist where they say): at each key, the body's pitch (PlayerAnimator's: back positive) and the arm's
        String clip;
        try {
            clip = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/assets/hexhaki/player_animation/gear5_bajrang_slam.json"));
        } catch (java.io.IOException failure) {
            throw new AssertionError(failure);
        }
        for (int k = 0; k < com.hexhaki.piece.gear.NikaSlam.T.length; k++) {
            int tick = (int) com.hexhaki.piece.gear.NikaSlam.T[k];
            double body = -com.hexhaki.piece.gear.NikaSlam.PITCH[k];
            double arm = com.hexhaki.piece.gear.NikaSlam.armPitch(k);
            Check.that(keyed(clip, tick, "body", fmt(body)), "nika: the slam's body pitch at " + tick + " is NikaSlam's (" + body + ")");
            Check.that(keyed(clip, tick, "rightArm", fmt(arm)), "nika: the slam's arm pitch at " + tick + " is NikaSlam's (" + arm + ")");
        }
    }

    /** Whether {@code clip} keys {@code part}'s pitch at {@code tick} to {@code value} (whatever the indentation). */
    static boolean keyed(String clip, int tick, String part, String value) {
        return java.util.regex.Pattern.compile("\"tick\":\\s*" + tick + ",\\s*\"easing\":\\s*\"inoutsine\",\\s*\"" + part
            + "\":\\s*\\{\\s*\"pitch\":\\s*" + java.util.regex.Pattern.quote(value) + "\\s*\\}").matcher(clip).find();
    }

    /** A number as the clip writer (Python's json) writes it: 70.0, -12.0. */
    static String fmt(double v) {
        return String.valueOf(Math.round(v * 1000.0) / 1000.0);
    }

    static void ranks() throws Exception {
        String lang = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/assets/hexhaki/lang/en_us.json"));
        for (com.hexhaki.fruit.Fruit fruit : com.hexhaki.fruit.Fruit.values()) {
            com.hexhaki.piece.FruitRanks.Rank rank = com.hexhaki.piece.FruitRanks.of(fruit);
            if (fruit == com.hexhaki.fruit.Fruit.NONE) {
                Check.that(rank == null, "ranks: no fruit, no rank");
                continue;
            }
            Check.that(rank != null, "ranks: " + fruit + " has a rank");
            Check.that(lang.contains("\"item.hexhaki." + fruit.id + "_" + fruit.id + "_no_mi\": \"" + rank.code), "ranks: " + fruit + "'s name in its colour");
        }
        Check.that(com.hexhaki.piece.FruitRanks.of(com.hexhaki.fruit.Fruit.UO) == com.hexhaki.piece.FruitRanks.Rank.MYTHICAL
            && com.hexhaki.piece.FruitRanks.NIKA == com.hexhaki.piece.FruitRanks.Rank.MYTHICAL, "ranks: the Uo Uo and Nika fruits Mythical");
        Check.that(lang.contains("\"item.hexhaki.nika_fruit\": \"" + com.hexhaki.piece.FruitRanks.NIKA.code), "ranks: the Nika fruit's name in its colour");
        for (java.util.List<com.hexhaki.fruit.FruitMove> moves : java.util.List.of(com.hexhaki.piece.server.GuraKit.MOVES,
            com.hexhaki.piece.server.HieKit.MOVES, com.hexhaki.piece.server.MaguKit.MOVES, com.hexhaki.piece.server.PikaKit.MOVES,
            com.hexhaki.piece.server.UoKit.MOVES)) {
            for (com.hexhaki.fruit.FruitMove move : moves) {
                if (move.cooldown() != null) {
                    Check.that(com.hexhaki.piece.server.FruitEnergy.listed(move), "energy: " + move.name() + " has its cost listed");
                }
            }
        }
    }
}
