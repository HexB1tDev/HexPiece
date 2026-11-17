package com.hexhaki.piece;

import com.hexhaki.piece.ui.AbilityHudView;
import com.hexhaki.piece.ui.AbilityPanel;
import com.hexhaki.piece.ui.UiCanvas;
import java.util.List;
import java.util.Map;

/** 2.2.2: the one abilities panel on the left (HexGhoul's): what it lists in each mode, and its card's size. */
public final class HudTests {
    static final Map<String, String> KEYS = Map.of("attack", "LMB", "use", "RMB", "special_a", "Y", "special_b", "U", "toggle", "R", "armament",
        "Ctrl+R", "observation", "V", "menu", "P");

    public static void main(String[] args) {
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
        Check.that(has(panel, "R", "Back to Black Leg"), "R back to the style");
    }

    static void off() {
        AbilityPanel.Panel panel = AbilityPanel.of(input("Six Powers", false, null, null, false, false));
        Check.that(panel.title().equals("HAKI") && has(panel, "R", "Combat mode (Six Powers)"), "combat mode off: Haki and the key that turns it on");
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
}
