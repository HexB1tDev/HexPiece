package com.hexhaki.piece.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 2.2.2 (the owner: "all the abilities, everything has to be in the same menu, like HUD on the left. Like, I don't care about mastery
 * in the bottom. I want you to copy the one from Hex Ghoul and put it here, basically in our way"): what the one abilities panel on the
 * left shows (pure, tested), HexGhoul's {@code AbilityGuide} in our way: a title in the mode's colour (the fighting style in combat
 * mode, the fruit in its own mode, Haki otherwise), what the body is doing, the Haki energy as a thin gauge under the title, then one
 * line a move: the keys the player bound, the move's name in its colour, dim when it is locked (with the mastery it needs) or
 * recharging (with the seconds left). The style's moves, the fruit's moves, Gear 5's, every Haki power and its quick slots, the swap
 * key and the Captain's Log: everything in one place; the original's mastery bars at the bottom are gone.
 */
public final class AbilityPanel {
    public static final int TEXT = 0xFFEDE3D6;
    public static final int DIM = 0xFF7D7480;
    public static final int HAKI = 0xFFE0464E;
    public static final int OBSERVATION = 0xFF6FB8FF;
    public static final int ENERGY = 0xFFB77BFF;

    /** One line: its keys, the move, its colour, what it needs if locked ("" if not), how long it still recharges and out of how long (ticks). */
    public record Row(String keys, String name, int colour, String needs, long left, long of) {
        public boolean locked() {
            return !this.needs.isEmpty();
        }

        /** How far its recharge has come (0..1; 1 ready). */
        public double ready() {
            return this.left <= 0 || this.of <= 0 ? 1.0 : Math.max(0.0, Math.min(1.0, 1.0 - this.left / (double)this.of));
        }

        public static Row of(String keys, String name, int colour) {
            return new Row(keys, name, colour, "", 0, 0);
        }
    }

    /** The panel: its title, subtitle and colour, the energy gauge (0..1, or -1 none), its lines. */
    public record Panel(String title, String subtitle, int accent, double energy, List<Row> rows) {
    }

    /** A fruit's or Gear 5's move as the client knows it: its keys, its name, what it needs ("" nothing), its recharge (ticks left, of). */
    public record Move(String keys, String name, String needs, long left, long of) {
    }

    /**
     * Everything the panel is made from (the client gathers it): {@code keys} the player's own key names ({@code attack}, {@code use},
     * {@code special_a}, {@code special_b}, {@code toggle}, {@code armament}, {@code observation}, {@code menu}); the style (its name,
     * colour, whether combat mode is on, its four moves' names: the string, the heavy, the two specials), the Swordsman's katana
     * ({@code sword}: null without one, else "sheathed", "drawing" or "drawn"); the fruit (null none; its colour; whether its own mode is
     * on, its moves); Haki (unlocked, Armament, Observation and Ryou on), the Haki quick slots; the energy (and its most).
     */
    public record Input(Map<String, String> keys, String style, int styleColour, boolean combat, List<String> styleMoves, String sword,
                        String fruit, int fruitColour, boolean fruitMode, List<Move> fruitMoves, boolean haki, boolean armament,
                        boolean observation, boolean ryou, List<Move> hakiSlots, double energy, double maxEnergy) {
    }

    private AbilityPanel() {
    }

    static String key(Input in, String name) {
        String k = in.keys().get(name);
        return k == null || k.isEmpty() ? "-" : k;
    }

    /** The panel for {@code in}. */
    public static Panel of(Input in) {
        List<Row> rows = new ArrayList<>();
        String title;
        String subtitle;
        int accent;
        if (in.combat()) {
            title = in.style().toUpperCase(java.util.Locale.ROOT);
            accent = in.styleColour();
            if (in.sword() != null) {
                subtitle = "katana " + in.sword();
                sword(in, rows);
            } else {
                subtitle = in.style().equals("Bare Hands") ? "no style yet" : "combat";
                style(in, rows);
            }
        } else if (in.fruitMode() && in.fruit() != null) {
            title = in.fruit().toUpperCase(java.util.Locale.ROOT);
            accent = in.fruitColour();
            subtitle = "devil fruit";
            for (Move move : in.fruitMoves()) {
                rows.add(move(move, in.fruitColour()));
            }
        } else {
            title = "HAKI";
            accent = HAKI;
            subtitle = "combat mode off";
            rows.add(Row.of(key(in, "toggle"), "Combat mode (" + in.style() + ")", TEXT));
        }
        haki(in, rows);
        if (in.fruit() != null) {
            rows.add(Row.of(key(in, "toggle"), in.fruitMode() ? "Back to " + in.style() : in.combat() ? "Swap to " + in.fruit() : "Fruit mode", DIM));
        }
        rows.add(Row.of(key(in, "menu"), "Captain's Log", DIM));
        double energy = in.maxEnergy() > 0 ? Math.max(0.0, Math.min(1.0, in.energy() / in.maxEnergy())) : -1.0;
        return new Panel(title, subtitle, accent, in.haki() ? energy : -1.0, rows);
    }

    /** The Swordsman's lines: the draw (slow) while sheathed, the string once drawn, the charge, the quick draw. */
    static void sword(Input in, List<Row> rows) {
        boolean out = in.sword().equals("drawn");
        int colour = in.styleColour();
        String attack = key(in, "attack");
        rows.add(Row.of(attack, out ? "Cut string" : "Draw (slow)", out ? colour : TEXT));
        rows.add(Row.of(key(in, "use") + " hold", in.armament() ? "Haki slash" : "Charged cut", in.armament() ? HAKI : colour));
        rows.add(Row.of("Ctrl+" + attack, "Quick draw", colour));
    }

    /** A fighting style's lines: its string, its heavy, its two specials. */
    static void style(Input in, List<Row> rows) {
        String[] slots = {"attack", "use", "special_a", "special_b"};
        for (int i = 0; i < Math.min(slots.length, in.styleMoves().size()); i++) {
            String name = in.styleMoves().get(i);
            if (name != null && !name.isEmpty()) {
                rows.add(Row.of(key(in, slots[i]), name, i == 0 ? TEXT : in.styleColour()));
            }
        }
    }

    static Row move(Move move, int colour) {
        boolean locked = !move.needs().isEmpty();
        return new Row(move.keys(), move.name(), locked ? DIM : colour, move.needs(), locked ? 0 : move.left(), move.of());
    }

    /** Haki: Armament (and Ryou with it), Observation, the Conqueror's quick slots. */
    static void haki(Input in, List<Row> rows) {
        if (!in.haki()) {
            return;
        }
        rows.add(Row.of(key(in, "armament"), in.armament() ? (in.ryou() ? "Armament + Ryou: on" : "Armament: on") : "Armament", in.armament() ? HAKI : TEXT));
        rows.add(Row.of(key(in, "observation"), in.observation() ? "Observation: on" : "Observation", in.observation() ? OBSERVATION : TEXT));
        for (Move move : in.hakiSlots()) {
            rows.add(move(move, HAKI));
        }
    }

    /** {@code ticks} as seconds, one decimal under ten. */
    public static String seconds(long ticks) {
        double s = ticks / 20.0;
        return s < 10.0 ? String.format(java.util.Locale.ROOT, "%.1fs", s) : Math.round(s) + "s";
    }
}
