package com.hexhaki.piece.ui;

import com.hexhaki.piece.Faction;
import com.hexhaki.piece.Gender;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Slot;
import com.hexhaki.piece.style.StyleMove;

/**
 * 2.1.1 (the owner: "Custom main menu with the same Male/female as HexGhoul (with a custom pirate UI)" and "decide between being a
 * pirate or a marine", the fighting style "picked at the start"): the start menu, drawn (pure, previewed by tools/preview/UiPreview).
 * Three pages on a parchment scroll over a dusk sea where a ship sails: who you are (male or female), which flag (pirate or marine), how
 * you fight (the four styles, their moves). Back and Next under it; the last page's Next is "Set Sail".
 */
public final class CreationView {
    public static final String[] STEPS = {"Who are you?", "Which flag do you sail under?", "How do you fight?"};

    /** What is picked so far and which page is open. */
    public static final class State {
        public int step;
        public Gender gender = Gender.UNSET;
        public Faction faction = Faction.UNSET;
        public FightingStyle style = FightingStyle.NONE;

        public boolean pageDone() {
            return switch (this.step) {
                case 0 -> this.gender.choice();
                case 1 -> this.faction.choice();
                default -> this.style.choice();
            };
        }
    }

    /** Where everything sits for a screen of {@code width} x {@code height}. */
    public record Layout(int width, int height, int x0, int y0, int x1, int y1) {
        public static Layout of(int width, int height) {
            int w = Math.min(440, width - 24);
            int h = Math.min(272, height - 40);
            int x0 = (width - w) / 2;
            int y0 = Math.max(26, (height - h) / 2 + 8);
            return new Layout(width, height, x0, y0, x0 + w, y0 + h);
        }

        int contentTop() {
            return this.y0 + 46;
        }

        int contentBottom() {
            return this.y1 - 30;
        }

        int[] back() {
            return new int[] {this.x0 + 12, this.y1 - 24, this.x0 + 92, this.y1 - 8};
        }

        int[] next() {
            return new int[] {this.x1 - 102, this.y1 - 24, this.x1 - 12, this.y1 - 8};
        }

        /** The cards of a page: {x0, y0, x1, y1} each. */
        int[][] cards(int count) {
            int gap = 8;
            int left = this.x0 + 14;
            int right = this.x1 - 14;
            int w = (right - left - gap * (count - 1)) / count;
            int[][] cards = new int[count][];
            for (int i = 0; i < count; i++) {
                int x = left + i * (w + gap);
                cards[i] = new int[] {x, this.contentTop(), x + w, this.contentBottom()};
            }
            return cards;
        }
    }

    private CreationView() {
    }

    static boolean inside(int[] box, double x, double y) {
        return x >= box[0] && x < box[2] && y >= box[1] && y < box[3];
    }

    /** What is under the mouse: "back", "next", "pick:N" (a card of the open page), or null. */
    public static String at(Layout layout, State state, double x, double y) {
        if (inside(layout.back(), x, y)) {
            return "back";
        }
        if (inside(layout.next(), x, y)) {
            return "next";
        }
        int[][] cards = layout.cards(state.step == 2 ? FightingStyle.choices().length : 2);
        for (int i = 0; i < cards.length; i++) {
            if (inside(cards[i], x, y)) {
                return "pick:" + i;
            }
        }
        return null;
    }

    /** Applies a click on {@code what}; returns true when the last page's "Set Sail" was pressed with every pick made. */
    public static boolean click(State state, String what) {
        if (what == null) {
            return false;
        }
        if (what.equals("back")) {
            state.step = Math.max(0, state.step - 1);
        } else if (what.equals("next")) {
            if (!state.pageDone()) {
                return false;
            }
            if (state.step == 2) {
                return true;
            }
            state.step++;
        } else if (what.startsWith("pick:")) {
            int index = Integer.parseInt(what.substring(5));
            switch (state.step) {
                case 0 -> state.gender = index == 0 ? Gender.MALE : Gender.FEMALE;
                case 1 -> state.faction = index == 0 ? Faction.PIRATE : Faction.MARINE;
                default -> state.style = FightingStyle.choices()[Math.max(0, Math.min(FightingStyle.choices().length - 1, index))];
            }
        }
        return false;
    }

    public static void draw(UiCanvas c, Layout l, State state, String hover, double time) {
        int horizon = l.height * 3 / 5;
        PirateUi.sky(c, 0, 0, l.width, horizon, time);
        PirateUi.sea(c, 0, horizon, l.width, l.height, time, PirateUi.SEA_TOP, PirateUi.SEA_LOW, PirateUi.CREST);
        float shipX = (float) ((l.width + 260) * ((time * 0.012) % 1.0)) - 130;
        PirateUi.ship(c, shipX, horizon + 18, 90, time, 0xFF3B2414, 0xFFEDE3CC,
            state.faction == Faction.MARINE ? PirateUi.SAIL : 0xFF151515, state.faction == Faction.MARINE);
        // the title over the scroll
        c.bigCentered("SET SAIL", l.width / 2, Math.max(4, l.y0 - 24), PirateUi.GOLD, true, 2.0F);
        PirateUi.parchment(c, l.x0, l.y0, l.x1, l.y1);
        // the page header: three steps along a rope
        int stepsY = l.y0 + 8;
        int span = (l.x1 - l.x0 - 60) / 3;
        for (int i = 0; i < 3; i++) {
            int cx = l.x0 + 30 + span * i + span / 2;
            boolean done = i < state.step || (i == state.step && state.pageDone());
            int colour = i == state.step ? PirateUi.RED : done ? 0xFF3C8A3C : PirateUi.INK_SOFT;
            if (i < 2) {
                c.fill(cx + 8, stepsY + 4, cx + span - 8, stepsY + 5, PirateUi.PARCHMENT_DARK);
            }
            PirateUi.circle(c, cx, stepsY + 4, 6, colour);
            c.centered(String.valueOf(i + 1), cx, stepsY + 1, 0xFFFFFFFF, false);
        }
        c.centered(STEPS[state.step], (l.x0 + l.x1) / 2, l.y0 + 24, PirateUi.INK, false);
        c.fill(l.x0 + 40, l.y0 + 36, l.x1 - 40, l.y0 + 37, PirateUi.PARCHMENT_DARK);
        switch (state.step) {
            case 0 -> genders(c, l, state, hover);
            case 1 -> flags(c, l, state, hover, time);
            default -> styles(c, l, state, hover);
        }
        int[] back = l.back();
        int[] next = l.next();
        PirateUi.ropeButton(c, back[0], back[1], back[2], back[3], "Back", "back".equals(hover), state.step > 0);
        PirateUi.ropeButton(c, next[0], next[1], next[2], next[3], state.step == 2 ? "Set Sail!" : "Next", "next".equals(hover),
            state.pageDone());
        String summary = (state.gender.choice() ? state.gender.display() : "?") + "  /  " + (state.faction.choice() ? state.faction.display() : "?")
            + "  /  " + (state.style.choice() ? state.style.display() : "?");
        c.centered(summary, (l.x0 + l.x1) / 2, l.y1 - 20, PirateUi.INK_SOFT, false);
    }

    static void genders(UiCanvas c, Layout l, State state, String hover) {
        int[][] cards = l.cards(2);
        Gender[] options = {Gender.MALE, Gender.FEMALE};
        String[] lines = {"Broad shoulders, the classic build.", "A woman's figure: slim arms, curves."};
        for (int i = 0; i < 2; i++) {
            int[] b = cards[i];
            boolean picked = state.gender == options[i];
            PirateUi.card(c, b[0], b[1], b[2], b[3], picked, ("pick:" + i).equals(hover), i == 0 ? 0xFF3C6EA8 : 0xFFB04C7A);
            int cx = (b[0] + b[2]) / 2;
            int figureTop = b[1] + 14;
            int figureHeight = Math.max(40, b[3] - b[1] - 62);
            PirateUi.figure(c, cx, figureTop, figureHeight, i == 1, 0xFFE2B48A, i == 0 ? 0xFF3C6EA8 : 0xFFB04C7A, 0xFF2E3A58);
            int head = Math.max(8, figureHeight / 4);
            c.face(cx - head / 2, figureTop, head);
            c.centered(options[i].display(), cx, b[3] - 40, PirateUi.INK, false);
            c.centered(c.fit(lines[i], b[2] - b[0] - 10), cx, b[3] - 26, PirateUi.INK_SOFT, false);
        }
    }

    static void flags(UiCanvas c, Layout l, State state, String hover, double time) {
        int[][] cards = l.cards(2);
        Faction[] options = {Faction.PIRATE, Faction.MARINE};
        for (int i = 0; i < 2; i++) {
            int[] b = cards[i];
            boolean picked = state.faction == options[i];
            PirateUi.card(c, b[0], b[1], b[2], b[3], picked, ("pick:" + i).equals(hover), i == 0 ? 0xFF1A1A1A : PirateUi.BLUE);
            int cx = (b[0] + b[2]) / 2;
            int fw = Math.min(84, b[2] - b[0] - 40);
            int fh = fw * 3 / 5;
            int fy = b[1] + 12;
            PirateUi.line(c, cx - fw / 2F - 3, fy - 4, cx - fw / 2F - 3, fy + fh + 18, 2, 0xFF5A3A20);
            if (i == 0) {
                PirateUi.jollyRoger(c, cx - fw / 2, fy, fw, fh, time);
            } else {
                PirateUi.marineFlag(c, cx - fw / 2, fy, fw, fh, time);
            }
            int y = fy + fh + 10;
            c.bigCentered(options[i].display(), cx, y, i == 0 ? PirateUi.INK : PirateUi.BLUE_INK, false, 1.4F);
            y += 16;
            y = c.wrap(options[i].blurb(), b[0] + 8, y, b[2] - b[0] - 16, PirateUi.INK_SOFT, 10, 4) + 6;
            String[] perks = i == 0
                ? new String[] {"Bounty: from Rookie to Emperor of the Sea", "Marines attack you on sight", "Your log is a wanted poster"}
                : new String[] {"Rank: from Seaman Recruit to Fleet Admiral", "Pirates attack you on sight", "Your log is a service record"};
            for (String perk : perks) {
                if (y + 10 > b[3] - 4) {
                    break;
                }
                PirateUi.circle(c, b[0] + 11, y + 3, 2, i == 0 ? PirateUi.RED : PirateUi.BLUE);
                c.text(c.fit(perk, b[2] - b[0] - 24), b[0] + 17, y, PirateUi.INK, false);
                y += 11;
            }
        }
    }

    static void styles(UiCanvas c, Layout l, State state, String hover) {
        FightingStyle[] options = FightingStyle.choices();
        int[][] cards = l.cards(options.length);
        for (int i = 0; i < options.length; i++) {
            int[] b = cards[i];
            FightingStyle style = options[i];
            boolean picked = state.style == style;
            PirateUi.card(c, b[0], b[1], b[2], b[3], picked, ("pick:" + i).equals(hover), style.colour());
            int cx = (b[0] + b[2]) / 2;
            int w = b[2] - b[0] - 8;
            c.centered(c.fit(style.display(), w), cx, b[1] + 8, PirateUi.INK, false);
            String lean = style.leaning() == Faction.PIRATE ? "Pirate's art" : style.leaning() == Faction.MARINE ? "Marine's art"
                : "Any flag";
            c.centered(lean, cx, b[1] + 19, style.colour(), false);
            int y = b[1] + 32;
            if (style.sword()) {
                // 2.1.4: the sword's own moves
                for (String[] line : new String[][] {{"M1", "15 cuts"}, {"RMB", "Charged Slash"}, {"M1", "Ctrl: Quick Draw"}}) {
                    c.fill(b[0] + 4, y - 1, b[0] + 22, y + 8, 0x30603818);
                    c.centered(line[0], b[0] + 13, y, PirateUi.INK, false);
                    c.text(c.fit(line[1], w - 22), b[0] + 25, y, PirateUi.INK, false);
                    y += 11;
                }
            }
            for (Slot slot : new Slot[] {Slot.M1_4, Slot.HEAVY, Slot.SPECIAL_A, Slot.SPECIAL_B}) {
                StyleMove move = StyleMove.of(style, slot);
                if (move == null) {
                    continue;
                }
                String key = switch (slot) {
                    case HEAVY -> "RMB";
                    case SPECIAL_A -> "Y";
                    case SPECIAL_B -> "U";
                    default -> "M1";
                };
                c.fill(b[0] + 4, y - 1, b[0] + 22, y + 8, 0x30603818);
                c.centered(key, b[0] + 13, y, PirateUi.INK, false);
                c.text(c.fit(move.display(), w - 22), b[0] + 25, y, PirateUi.INK, false);
                y += 11;
            }
            c.fill(b[0] + 6, y + 1, b[2] - 6, y + 2, PirateUi.PARCHMENT_DARK);
            c.wrap(style.blurb(), b[0] + 5, y + 5, w, PirateUi.INK_SOFT, 9, Math.max(2, (b[3] - y - 10) / 9));
        }
    }
}
