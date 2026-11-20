package com.hexhaki.piece.ui;

import com.hexhaki.piece.Alignment;
import com.hexhaki.piece.Faction;
import com.hexhaki.piece.Gender;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.PieceRules;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Slot;
import com.hexhaki.piece.style.StyleMove;
import java.util.ArrayList;
import java.util.List;

/**
 * 2.1.1 (the owner: "New mastery menu (also pirate UI, like blue, white, with a ship?)"): the Captain's Log (pure, previewed by
 * tools/preview/UiPreview). A navy sea with white crests, a white-sailed ship flying the player's flag, and a sailcloth panel with blue
 * trim.
 *
 * <p>2.2.4, the one menu (the owner: "there's two different types of GUI ... put them all on the same thing ... check the fruit mastery
 * GUI. It's still old. ... reorganize the mod as best you can", then: "every combat style should be together with the haki ... same
 * menu, same category ... the only menu that is separate is the devil fruit powers"). The original's Ability Book, Haki Techniques and
 * Gear mastery screens are gone into it (the client opens this wherever they opened). Three pages:</p>
 * <ul>
 *   <li>Overview: the wanted poster (a marine's service record), the flag, title, style and every mastery at a glance; 2.4.6: the
 *       alignment bar (good and evil).</li>
 *   <li>Style &amp; Haki: the fighting style's moves and mastery, then Armament, Observation and Conqueror's Haki, each with every
 *       technique, what it needs and its key; the Conqueror's quick powers are bound to a quick slot here.</li>
 *   <li>Devil Fruit: the fruit's moves, its mastery and its quick slots (a kit fruit), or the Nika fruit's techniques and the upgrades
 *       its points buy (with a respec).</li>
 * </ul>
 * Each list page is a scrolling list of sections on the left and the picked line in detail on the right, the nine quick slots under it
 * (click one, or press its key, to put the picked ability there).
 */
public final class LogbookView {
    public static final String[] TABS = {"Overview", "Style & Haki", "Devil Fruit"};
    public static final int OVERVIEW = 0;
    public static final int STYLE_HAKI = 1;
    public static final int FRUIT = 2;
    static final String[] HAKI = {"Armament", "Observation", "Conqueror's"};
    static final int HEADING_H = 24;
    static final int ROW_H = 13;

    /**
     * One line of a list: a move, a technique or an upgrade. {@code key}: its key cap ("G", "Ctrl+R", "M1", "-"); {@code info}: how it
     * is used ("tap or hold", "3.0s recharge"); {@code lock}: what it still needs (shown while not {@code open}); {@code ability}: the
     * quick-slot ability it is (-1: not bindable); {@code upgrade}: the upgrade it buys (-1: none), with its rank, top rank and cost.
     */
    public record Entry(String name, String key, String info, String description, boolean open, String lock, int ability, int upgrade,
                        int rank, int maxRank, int cost) {
        public static Entry of(String name, String key, String info, String description, boolean open, String lock) {
            return new Entry(name, key, info, description, open, lock, -1, -1, 0, 0, 0);
        }

        public Entry bindable(int id) {
            return new Entry(this.name, this.key, this.info, this.description, this.open, this.lock, id, this.upgrade, this.rank,
                this.maxRank, this.cost);
        }

        public static Entry upgrade(String name, String description, int index, int rank, int maxRank, int cost) {
            return new Entry(name, rank >= maxRank ? "MAX" : rank + "/" + maxRank, rank >= maxRank ? "Rank " + rank + "/" + maxRank + ": maxed"
                : "Rank " + rank + "/" + maxRank + ", the next costs " + cost + (cost == 1 ? " point" : " points"),
                description, true, "", -1, index, rank, maxRank, cost);
        }
    }

    /** A heading over entries (a style, a Haki path, a fruit, its upgrades): its status, a bar, and what it says when picked. */
    public record Section(String title, String status, int colour, float progress, List<String> about, List<Entry> entries) {
    }

    /** One row of a list page: section {@code section}'s heading ({@code entry} -1) or its entry. */
    public record Row(int section, int entry) {
        public boolean heading() {
            return this.entry < 0;
        }
    }

    /**
     * What the log shows: the player and their profile, each Haki's mastery (0..1000) and rank, their fruit and its mastery; the two list
     * pages; the nine quick slots (their keys, what each holds, its ability id or -1); the Nika fruit's free points ({@code points} -1
     * without it).
     */
    public record Data(String name, PieceProfile profile, int[] haki, String[] hakiRanks, String fruit, int fruitMastery,
                       List<Section> styleHaki, List<Section> fruitPage, String[] slotKeys, String[] slotNames, int[] slotAbilities,
                       int points) {
        public List<Section> page(int tab) {
            return tab == STYLE_HAKI ? this.styleHaki : tab == FRUIT ? this.fruitPage : List.of();
        }
    }

    /** The screen's own state: the page, the first row shown, the picked row, the slot that just took an ability. */
    public static final class State {
        public int tab;
        public int scroll;
        public int picked;
        public int flashed = -1;
        public double flashedAt = -10;

        public State(int tab) {
            this.tab = tab;
        }
    }

    public record Layout(int width, int height, int x0, int y0, int x1, int y1, int shipX) {
        public static Layout of(int width, int height) {
            boolean wide = width >= 540;
            int w = wide ? Math.min(470, width - 170) : Math.min(470, width - 12);
            int h = Math.min(296, height - 14);
            int x1 = wide ? width - Math.max(8, (width - w - 150) / 2) : (width + w) / 2;
            int x0 = x1 - w;
            int y0 = (height - h) / 2;
            return new Layout(width, height, x0, y0, x1, y0 + h, wide ? Math.max(70, x0 / 2) : -1);
        }

        int[] tab(int index) {
            int w = (this.x1 - this.x0 - 16) / TABS.length;
            int x = this.x0 + 8 + index * w;
            return new int[] {x, this.y0 + 22, x + w - 2, this.y0 + 36};
        }

        int top() {
            return this.y0 + 42;
        }

        int bottom() {
            return this.y1 - 27;
        }

        /** The list column {x0, x1} and the detail column {x0, x1}. */
        int[] list() {
            return new int[] {this.x0 + 8, this.x0 + 8 + (this.x1 - this.x0 - 16) * 45 / 100};
        }

        int[] detail() {
            return new int[] {this.list()[1] + 8, this.x1 - 8};
        }

        /** The quick slots' grid under the detail: {x0, y0, x1, y1}. */
        int[] slots() {
            int[] d = this.detail();
            return new int[] {d[0], this.bottom() - 3 * 13 - 12, d[1], this.bottom()};
        }

        int[] slot(int index) {
            int[] s = this.slots();
            int w = (s[2] - s[0] - 4) / 3;
            int x = s[0] + 2 + index % 3 * w;
            int y = s[1] + 12 + index / 3 * 13;
            return new int[] {x, y, x + w - 2, y + 12};
        }
    }

    private LogbookView() {
    }

    static boolean inside(int[] box, double x, double y) {
        return x >= box[0] && x < box[2] && y >= box[1] && y < box[3];
    }

    /** The rows of a list page, sections and their entries in order. */
    public static List<Row> rows(List<Section> page) {
        List<Row> rows = new ArrayList<>();
        for (int s = 0; s < page.size(); s++) {
            rows.add(new Row(s, -1));
            for (int e = 0; e < page.get(s).entries().size(); e++) {
                rows.add(new Row(s, e));
            }
        }
        return rows;
    }

    static int height(Row row) {
        return row.heading() ? HEADING_H : ROW_H;
    }

    /** How many rows from {@code first} fit the list. */
    static int fitting(Layout l, List<Row> rows, int first) {
        int room = l.bottom() - l.top();
        int n = 0;
        for (int i = first; i < rows.size(); i++) {
            room -= height(rows.get(i));
            if (room < 0) {
                break;
            }
            n++;
        }
        return n;
    }

    /** The largest first row that still fills the list (scrolling stops there). */
    public static int maxScroll(Layout l, List<Row> rows) {
        int room = l.bottom() - l.top();
        int first = rows.size();
        while (first > 0 && room - height(rows.get(first - 1)) >= 0) {
            room -= height(rows.get(first - 1));
            first--;
        }
        return first;
    }

    /** The entry a row is, or null for a heading. */
    public static Entry entry(List<Section> page, Row row) {
        return row == null || row.heading() ? null : page.get(row.section()).entries().get(row.entry());
    }

    /** The picked row's entry, or null (a heading, nothing picked). */
    public static Entry pickedEntry(Data data, State state) {
        List<Section> page = data.page(state.tab);
        List<Row> rows = rows(page);
        return state.picked >= 0 && state.picked < rows.size() ? entry(page, rows.get(state.picked)) : null;
    }

    /** Scrolls so the picked row is in sight. */
    public static void reveal(Layout l, List<Row> rows, State state) {
        if (state.picked < state.scroll) {
            state.scroll = state.picked;
        }
        while (state.scroll < state.picked && state.picked >= state.scroll + fitting(l, rows, state.scroll)) {
            state.scroll++;
        }
        state.scroll = Math.max(0, Math.min(maxScroll(l, rows), state.scroll));
    }

    /** The picked entry's ability when it can go on a quick slot now (open and bindable), else -1. */
    public static int bindable(Data data, State state) {
        List<Section> page = data.page(state.tab);
        List<Row> rows = rows(page);
        if (state.picked < 0 || state.picked >= rows.size()) {
            return -1;
        }
        Entry e = entry(page, rows.get(state.picked));
        return e != null && e.open() && e.ability() >= 0 ? e.ability() : -1;
    }

    /** Whether a page has anything for the quick slots (then the grid is drawn). */
    static boolean slotted(List<Section> page) {
        for (Section s : page) {
            for (Entry e : s.entries()) {
                if (e.ability() >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The buttons along the bottom for a page: {id, label}, right to left from Close. */
    static String[][] buttons(Data data, int tab) {
        List<String[]> out = new ArrayList<>();
        out.add(new String[] {"close", "Close"});
        if (tab != OVERVIEW && slotted(data.page(tab))) {
            out.add(new String[] {"reset", "Reset Slots"});
        }
        if (tab == FRUIT && data.points() >= 0) {
            out.add(new String[] {"respec", "Respec"});
        }
        return out.toArray(new String[0][]);
    }

    static int[] button(UiCanvas c, Layout l, String[][] buttons, int index) {
        int right = l.x1 - 8;
        for (int i = 0; i < index; i++) {
            right -= c.width(buttons[i][1]) + 22 + 6;
        }
        return new int[] {right - c.width(buttons[index][1]) - 22, l.y1 - 22, right, l.y1 - 7};
    }

    /** The buy button in an upgrade's detail: right of its rank, under the name, the section and the cost. */
    static int[] buy(Layout l) {
        int[] d = l.detail();
        int y = l.top() + 5 + 36;
        return new int[] {d[1] - 6 - 62, y - 3, d[1] - 6, y + 11};
    }

    /** The key caps' width on a page: the widest key, 30 to 58 pixels. */
    static int capWidth(UiCanvas c, List<Section> page) {
        int w = 30;
        for (Section s : page) {
            for (Entry e : s.entries()) {
                w = Math.max(w, c.width(e.key()) + 6);
            }
        }
        return Math.min(58, w);
    }

    /** "tab:N", "row:N" (a row of the page, counted from the first), "slot:N", "buy", "reset", "respec", "close" or null. */
    public static String at(UiCanvas c, Layout l, Data data, State state, double x, double y) {
        for (int i = 0; i < TABS.length; i++) {
            if (inside(l.tab(i), x, y)) {
                return "tab:" + i;
            }
        }
        String[][] buttons = buttons(data, state.tab);
        for (int i = 0; i < buttons.length; i++) {
            if (inside(button(c, l, buttons, i), x, y)) {
                return buttons[i][0];
            }
        }
        if (state.tab == OVERVIEW) {
            return null;
        }
        List<Section> page = data.page(state.tab);
        List<Row> rows = rows(page);
        int[] list = l.list();
        int top = l.top();
        int shown = fitting(l, rows, state.scroll);
        for (int i = state.scroll; i < state.scroll + shown; i++) {
            int h = height(rows.get(i));
            if (x >= list[0] && x < list[1] - 4 && y >= top && y < top + h) {
                return "row:" + i;
            }
            top += h;
        }
        if (slotted(page)) {
            for (int i = 0; i < 9; i++) {
                if (inside(l.slot(i), x, y)) {
                    return "slot:" + i;
                }
            }
        }
        Entry picked = state.picked >= 0 && state.picked < rows.size() ? entry(page, rows.get(state.picked)) : null;
        if (picked != null && picked.upgrade() >= 0 && inside(buy(l), x, y)) {
            return "buy";
        }
        return null;
    }

    public static void draw(UiCanvas c, Layout l, Data data, State state, String hover, double time) {
        PieceProfile p = data.profile();
        boolean marine = p.faction() == Faction.MARINE;
        // the sea at night: navy, white crests, the ship riding it
        c.gradient(0, 0, l.width, l.height / 2, 0xFF081A33, PirateUi.NAVY);
        PirateUi.sea(c, 0, l.height / 2, l.width, l.height, time, PirateUi.NAVY_LIGHT, 0xFF061224, 0xB0FFFFFF);
        for (int i = 0; i < 26; i++) {
            int sx = (i * 73) % Math.max(1, l.width);
            int sy = (i * 37) % Math.max(1, l.height / 2 - 10);
            int a = (int) (90 + 80 * Math.sin(time * 2 + i));
            c.fill(sx, sy, sx + 1, sy + 1, a << 24 | 0xFFFFFF);
        }
        PirateUi.circle(c, l.width * 0.12F, l.height * 0.12F, 9, 0xFFF4F1E0);
        PirateUi.circle(c, l.width * 0.12F + 3, l.height * 0.12F - 2, 8, 0xFF0B1F3C);
        if (l.shipX > 0) {
            PirateUi.ship(c, l.shipX, l.height / 2 + 24, Math.min(120, l.x0 - 20), time, marine ? 0xFFE9EEF5 : 0xFF2A1C10, PirateUi.SAIL,
                marine ? PirateUi.SAIL : 0xFF151515, marine);
            c.centered(data.name(), l.shipX, l.height / 2 + 34, 0xFFFFFFFF, true);
            c.centered(p.faction().choice() ? p.faction().display() + " - " + p.title() : "No flag yet", l.shipX, l.height / 2 + 45,
                marine ? PirateUi.BLUE_LIGHT : PirateUi.GOLD, true);
        }
        // the sailcloth panel
        c.fill(l.x0 + 4, l.y0 + 4, l.x1 + 4, l.y1 + 4, 0x70000000);
        c.fill(l.x0, l.y0, l.x1, l.y1, PirateUi.SAIL);
        c.gradient(l.x0, l.y1 - 40, l.x1, l.y1, 0x00DCE6F2, PirateUi.SAIL_SHADE);
        c.fill(l.x0, l.y0, l.x1, l.y0 + 18, PirateUi.BLUE);
        c.fill(l.x0, l.y0 + 18, l.x1, l.y0 + 20, PirateUi.BLUE_INK);
        PirateUi.border(c, l.x0, l.y0, l.x1, l.y1, PirateUi.BLUE_INK);
        PirateUi.border(c, l.x0 + 2, l.y0 + 20, l.x1 - 2, l.y1 - 2, PirateUi.BLUE_LIGHT);
        PirateUi.gull(c, l.x0 + 14, l.y0 + 10, 7, 0xFFFFFFFF);
        c.text(marine ? "MARINE SERVICE RECORD" : "CAPTAIN'S LOG", l.x0 + 26, l.y0 + 5, 0xFFFFFFFF, true);
        String rightTitle = marine ? "Merit " + p.renown() : PieceRules.berries(p.renown());
        c.text(rightTitle, l.x1 - 8 - c.width(rightTitle), l.y0 + 5, 0xFFDDEBFF, false);
        for (int i = 0; i < TABS.length; i++) {
            int[] b = l.tab(i);
            boolean open = state.tab == i;
            boolean over = ("tab:" + i).equals(hover);
            c.fill(b[0], b[1], b[2], b[3], open ? PirateUi.BLUE : over ? PirateUi.BLUE_LIGHT : 0xFFE4ECF6);
            PirateUi.border(c, b[0], b[1], b[2], b[3], open ? PirateUi.BLUE_INK : 0xFF9CB6D6);
            c.centered(TABS[i], (b[0] + b[2]) / 2, b[1] + 3, open ? 0xFFFFFFFF : PirateUi.BLUE_INK, false);
        }
        if (state.tab == OVERVIEW) {
            overview(c, l, data, l.top(), time);
        } else {
            page(c, l, data, state, hover, time);
        }
        String[][] buttons = buttons(data, state.tab);
        int leftmost = l.x1;
        for (int i = 0; i < buttons.length; i++) {
            int[] b = button(c, l, buttons, i);
            boolean over = buttons[i][0].equals(hover);
            c.fill(b[0], b[1], b[2], b[3], over ? PirateUi.BLUE : PirateUi.BLUE_INK);
            c.fill(b[0], b[1], b[2], b[1] + 1, 0x60FFFFFF);
            c.centered(buttons[i][1], (b[0] + b[2]) / 2, b[1] + 4, 0xFFFFFFFF, false);
            leftmost = b[0];
        }
        String hint = hint(data, state);
        if (!hint.isEmpty()) {
            c.text(c.fit(hint, leftmost - l.x0 - 18), l.x0 + 9, l.y1 - 18, 0xFF5A7090, false);
        }
    }

    /** The line along the bottom: what a click or a key does on this page now. */
    static String hint(Data data, State state) {
        if (state.tab == OVERVIEW) {
            return "Tab or the arrow keys turn the pages.";
        }
        if (bindable(data, state) >= 0) {
            return "Click a quick slot or press its key to bind it.";
        }
        return slotted(data.page(state.tab)) ? "Pick a quick power to bind it to a slot." : "Pick a line to read it.";
    }

    static void label(UiCanvas c, String text, int x, int y) {
        c.text(text, x, y, PirateUi.BLUE, false);
    }

    static void overview(UiCanvas c, Layout l, Data data, int top, double time) {
        PieceProfile p = data.profile();
        int left = l.x0 + 10;
        int posterW = 92;
        int posterX = l.x1 - 10 - posterW;
        int posterY = top;
        int posterH = Math.min(118, l.bottom() - top);
        if (p.faction() == Faction.MARINE) {
            c.fill(posterX, posterY, posterX + posterW, posterY + posterH, 0xFFEAF1FA);
            PirateUi.border(c, posterX, posterY, posterX + posterW, posterY + posterH, PirateUi.BLUE);
            c.centered("RANK", posterX + posterW / 2, posterY + 5, PirateUi.BLUE, false);
            PirateUi.gull(c, posterX + posterW / 2F, posterY + 30, 14, PirateUi.BLUE);
            c.face(posterX + posterW / 2 - 14, posterY + 40, 28);
            c.centered(c.fit(p.title(), posterW - 6), posterX + posterW / 2, posterY + 74, PirateUi.BLUE_INK, false);
            int stars = Math.min(5, (int) (p.renown() / 400));
            for (int i = 0; i < stars; i++) {
                PirateUi.circle(c, posterX + posterW / 2F - (stars - 1) * 5 + i * 10, posterY + 90, 3, PirateUi.GOLD);
            }
            c.centered("Merit " + p.renown(), posterX + posterW / 2, posterY + posterH - 12, PirateUi.BLUE, false);
        } else {
            c.fill(posterX, posterY, posterX + posterW, posterY + posterH, 0xFFE8D7AE);
            PirateUi.border(c, posterX, posterY, posterX + posterW, posterY + posterH, 0xFF8A6A3C);
            c.bigCentered("WANTED", posterX + posterW / 2, posterY + 4, PirateUi.INK, false, 1.5F);
            c.fill(posterX + 14, posterY + 19, posterX + posterW - 14, posterY + 64, 0xFFBFA77A);
            c.face(posterX + posterW / 2 - 20, posterY + 21, 40);
            c.centered("DEAD OR ALIVE", posterX + posterW / 2, posterY + 67, PirateUi.INK, false);
            c.centered(c.fit(data.name(), posterW - 6), posterX + posterW / 2, posterY + 78, PirateUi.INK, false);
            c.centered(c.fit(PieceRules.berries(p.renown()), posterW - 6), posterX + posterW / 2, posterY + 92, PirateUi.RED, false);
            c.centered("berries", posterX + posterW / 2, posterY + 102, PirateUi.INK_SOFT, false);
        }
        int y = top;
        String[][] facts = {
            {"Name", data.name()}, {"Flag", p.faction().choice() ? p.faction().display() : "None yet"}, {"Title", p.title()},
            {"Body", p.gender() == Gender.UNSET ? "-" : p.gender().display()}};
        for (String[] fact : facts) {
            label(c, fact[0], left, y);
            c.text(c.fit(fact[1], posterX - left - 60), left + 56, y, PirateUi.BLUE_INK, false);
            y += 11;
        }
        label(c, "Fruit", left, y);
        boolean none = data.fruit() == null || data.fruit().isEmpty();
        c.text(c.fit(none ? "None" : data.fruit(), posterX - left - 60), left + 56, y, none ? 0xFF7A7A7A : 0xFF6A2A8A, false);
        y += 15;
        int barRight = posterX - 10;
        FightingStyle style = p.style();
        label(c, style.choice() ? style.display() : "Style", left, y);
        String sm = p.styleMastery() + "/" + PieceRules.MAX_MASTERY;
        c.text(sm, barRight - c.width(sm), y, PirateUi.BLUE_INK, false);
        PirateUi.bar(c, left, y + 10, barRight, y + 14, p.styleMastery() / (float) PieceRules.MAX_MASTERY,
            style.choice() ? style.colour() | 0xFF000000 : PirateUi.BLUE, 0xFFD6E2F0);
        y += 18;
        for (int i = 0; i < 3; i++) {
            label(c, HAKI[i], left, y);
            String rank = data.hakiRanks()[i];
            c.text(c.fit(rank, 70), barRight - c.width(c.fit(rank, 70)), y, PirateUi.BLUE_INK, false);
            PirateUi.bar(c, left, y + 10, barRight, y + 14, data.haki()[i] / 1000F, i == 2 ? 0xFF8A2030 : 0xFF1D3F66, 0xFFD6E2F0);
            y += 18;
        }
        // 2.4.6 (the owner: "create the bar of good/evil in the mastery menu"): -100 pure evil at the left, 0 neutral in the middle,
        // 100 pure good at the right
        if (y + 14 <= l.bottom()) {
            int a = p.alignment();
            label(c, "Alignment", left, y);
            String said = Alignment.label(a) + " " + (a > 0 ? "+" + a : String.valueOf(a));
            c.text(said, barRight - c.width(said), y, Alignment.evil(a) ? PirateUi.EVIL : Alignment.good(a) ? 0xFF8A6410 : PirateUi.BLUE_INK,
                false);
            PirateUi.alignment(c, left, y + 10, barRight, y + 14, a);
            y += 18;
        }
        long next = PieceRules.nextTitle(p.faction(), p.renown());
        if (p.faction().choice() && next > 0 && y + 10 <= l.bottom()) {
            String goal = p.faction() == Faction.PIRATE ? "Next title at " + PieceRules.berries(next) + " berries" : "Next rank at " + next + " merit";
            c.text(c.fit(goal, l.x1 - l.x0 - 20), left, y + 2, PirateUi.BLUE, false);
        }
    }

    /** A list page: the rows on the left, the picked one on the right, the quick slots under it. */
    static void page(UiCanvas c, Layout l, Data data, State state, String hover, double time) {
        List<Section> page = data.page(state.tab);
        int[] list = l.list();
        int[] detail = l.detail();
        if (page.isEmpty()) {
            c.wrap(state.tab == FRUIT ? "No Devil Fruit. Eat one to gain its power; you can only ever hold one, a second tears the body apart."
                : "Nothing here yet.", l.x0 + 12, l.top() + 4, l.x1 - l.x0 - 24, PirateUi.BLUE_INK, 10, 4);
            return;
        }
        List<Row> rows = rows(page);
        int shown = fitting(l, rows, state.scroll);
        int capW = capWidth(c, page);
        int y = l.top();
        for (int i = state.scroll; i < state.scroll + shown; i++) {
            Row row = rows.get(i);
            Section section = page.get(row.section());
            boolean picked = i == state.picked;
            boolean over = ("row:" + i).equals(hover);
            if (row.heading()) {
                heading(c, list[0], y, list[1] - 5, section, picked, over);
            } else {
                line(c, list[0], y, list[1] - 5, capW, section.entries().get(row.entry()), picked, over);
            }
            y += height(row);
        }
        // the scroll bar
        int max = maxScroll(l, rows);
        if (max > 0) {
            int track = l.bottom() - l.top();
            int knob = Math.max(12, track * shown / rows.size());
            int at = l.top() + (track - knob) * state.scroll / max;
            c.fill(list[1] - 3, l.top(), list[1] - 1, l.bottom(), 0xFFDCE6F2);
            c.fill(list[1] - 3, at, list[1] - 1, at + knob, PirateUi.BLUE);
        }
        // the detail
        boolean slots = slotted(page);
        int bottom = slots ? l.slots()[1] - 4 : l.bottom();
        c.fill(detail[0], l.top(), detail[1], bottom, 0xFFF0F5FB);
        PirateUi.border(c, detail[0], l.top(), detail[1], bottom, 0xFFB8CCE4);
        Row row = state.picked >= 0 && state.picked < rows.size() ? rows.get(state.picked) : rows.get(0);
        Section section = page.get(row.section());
        if (row.heading()) {
            sectionDetail(c, detail[0] + 6, l.top() + 5, detail[1] - 6, bottom - 4, section);
        } else {
            entryDetail(c, l, detail[0] + 6, l.top() + 5, detail[1] - 6, bottom - 4, section.entries().get(row.entry()), section, data,
                "buy".equals(hover));
        }
        if (slots) {
            slots(c, l, data, state, hover, time);
        }
    }

    static void heading(UiCanvas c, int x0, int y, int x1, Section s, boolean picked, boolean over) {
        c.fill(x0, y + 1, x1, y + HEADING_H - 2, picked ? 0xFFCFE0F5 : over ? 0xFFE3ECF8 : 0xFFE9F0F8);
        c.fill(x0, y + 1, x0 + 3, y + HEADING_H - 2, s.colour() | 0xFF000000);
        c.text(c.fit(s.title(), x1 - x0 - 12 - c.width(s.status())), x0 + 7, y + 4, PirateUi.BLUE_INK, false);
        c.text(s.status(), x1 - 3 - c.width(s.status()), y + 4, PirateUi.BLUE, false);
        PirateUi.bar(c, x0 + 7, y + 15, x1 - 3, y + 19, s.progress(), s.colour() | 0xFF000000, 0xFFD6E2F0);
    }

    static void line(UiCanvas c, int x0, int y, int x1, int capW, Entry e, boolean picked, boolean over) {
        c.fill(x0, y, x1, y + ROW_H - 1, picked ? 0xFFCFE0F5 : over ? 0xFFE8F0FA : 0x00000000);
        if (picked) {
            c.fill(x0, y, x0 + 1, y + ROW_H - 1, PirateUi.BLUE);
        }
        boolean maxed = e.upgrade() >= 0 && e.rank() >= e.maxRank();
        int cap = !e.open() ? 0xFF9AA4B0 : maxed ? 0xFF2E7D4A : e.ability() >= 0 ? PirateUi.BLUE : 0xFF3F5878;
        c.fill(x0 + 4, y + 1, x0 + 4 + capW, y + ROW_H - 2, cap);
        c.centered(c.fit(e.key(), capW - 4), x0 + 4 + capW / 2, y + 2, 0xFFFFFFFF, false);
        int nameRight = x1 - 4 - (e.open() ? 0 : 9);
        c.text(c.fit(e.name(), nameRight - x0 - capW - 9), x0 + capW + 9, y + 2, e.open() ? PirateUi.BLUE_INK : 0xFF7E8894, false);
        if (!e.open()) {
            lock(c, x1 - 9, y + 2);
        }
    }

    /** A small padlock: the shackle and the body. */
    static void lock(UiCanvas c, int x, int y) {
        c.fill(x + 1, y, x + 5, y + 1, 0xFFA03030);
        c.fill(x + 1, y, x + 2, y + 4, 0xFFA03030);
        c.fill(x + 4, y, x + 5, y + 4, 0xFFA03030);
        c.fill(x, y + 3, x + 6, y + 8, 0xFFA03030);
        c.fill(x + 2, y + 5, x + 4, y + 6, 0xFFF0D0D0);
    }

    static void sectionDetail(UiCanvas c, int x0, int y, int x1, int bottom, Section s) {
        c.fill(x0 - 2, y - 1, x0, y + 9, s.colour() | 0xFF000000);
        c.text(c.fit(s.title(), x1 - x0 - 4), x0 + 3, y, PirateUi.BLUE_INK, false);
        y += 11;
        c.text(c.fit(s.status(), x1 - x0), x0, y, PirateUi.BLUE, false);
        y += 11;
        PirateUi.bar(c, x0, y, x1, y + 5, s.progress(), s.colour() | 0xFF000000, 0xFFD6E2F0);
        y += 10;
        for (String line : s.about()) {
            int lines = Math.max(1, (bottom - y) / 10);
            if (lines < 1 || y + 9 > bottom) {
                break;
            }
            y = c.wrap(line, x0, y, x1 - x0, 0xFF3A5070, 10, Math.min(lines, 4)) + 3;
        }
    }

    static void entryDetail(UiCanvas c, Layout l, int x0, int y, int x1, int bottom, Entry e, Section s, Data data, boolean overBuy) {
        int capW = Math.min(64, c.width(e.key()) + 10);
        c.fill(x1 - capW, y - 2, x1, y + 9, e.open() ? (e.ability() >= 0 ? PirateUi.BLUE : 0xFF3F5878) : 0xFF9AA4B0);
        c.centered(c.fit(e.key(), capW - 4), x1 - capW / 2, y, 0xFFFFFFFF, false);
        c.text(c.fit(e.name(), x1 - x0 - capW - 6), x0, y, PirateUi.BLUE_INK, false);
        y += 12;
        c.text(c.fit(s.title(), x1 - x0), x0, y, 0xFF7A8EAA, false);
        y += 12;
        if (!e.open()) {
            lock(c, x0, y);
            c.text(c.fit(e.lock().isEmpty() ? "Locked" : e.lock(), x1 - x0 - 10), x0 + 10, y, PirateUi.RED, false);
            y += 12;
        } else if (!e.info().isEmpty()) {
            c.text(c.fit(e.info(), x1 - x0), x0, y, 0xFF2E7D4A, false);
            y += 12;
        }
        if (e.ability() >= 0) {
            String on = "";
            for (int i = 0; i < 9; i++) {
                if (data.slotAbilities()[i] == e.ability()) {
                    on = on.isEmpty() ? data.slotKeys()[i] : on + ", " + data.slotKeys()[i];
                }
            }
            c.text(c.fit(on.isEmpty() ? "Not on a quick slot" : "Quick slot: " + on, x1 - x0), x0, y, on.isEmpty() ? 0xFF8A6A3C : PirateUi.BLUE, false);
            y += 12;
        }
        if (e.upgrade() >= 0) {
            // its rank as pips, the points left and the buy button, then what it does
            int[] b = buy(l);
            for (int i = 0; i < e.maxRank(); i++) {
                c.fill(x0 + i * 9, b[1] + 4, x0 + i * 9 + 7, b[1] + 9, i < e.rank() ? s.colour() | 0xFF000000 : 0xFFD6E2F0);
            }
            boolean can = e.rank() < e.maxRank() && data.points() >= e.cost();
            c.fill(b[0], b[1], b[2], b[3], !can ? 0xFF9AA4B0 : overBuy ? PirateUi.BLUE : PirateUi.BLUE_INK);
            c.fill(b[0], b[1], b[2], b[1] + 1, 0x60FFFFFF);
            c.centered(e.rank() >= e.maxRank() ? "Maxed" : "Buy (" + e.cost() + ")", (b[0] + b[2]) / 2, b[1] + 3, 0xFFFFFFFF, false);
            String free = data.points() + " free";
            c.text(free, b[0] - 6 - c.width(free), b[1] + 3, PirateUi.BLUE, false);
            y = b[3] + 4;
        }
        int lines = Math.max(1, (bottom - y) / 10);
        c.wrap(e.description(), x0, y + 2, x1 - x0, 0xFF3A5070, 10, lines);
    }

    static void slots(UiCanvas c, Layout l, Data data, State state, String hover, double time) {
        int[] box = l.slots();
        int ready = bindable(data, state);
        c.text("QUICK SLOTS", box[0] + 2, box[1] + 1, PirateUi.BLUE, false);
        if (ready >= 0) {
            String hint = "click to bind";
            c.text(hint, box[2] - 2 - c.width(hint), box[1] + 1, 0xFF2E7D4A, false);
        }
        for (int i = 0; i < 9; i++) {
            int[] b = l.slot(i);
            boolean holds = ready >= 0 && data.slotAbilities()[i] == ready;
            boolean flash = state.flashed == i && time - state.flashedAt < 0.6;
            boolean over = ("slot:" + i).equals(hover) && ready >= 0;
            int back = flash ? 0xFFF2D27A : over ? 0xFFCFE0F5 : holds ? 0xFFDDEAF8 : 0xFFF4F7FB;
            c.fill(b[0], b[1], b[2], b[3], back);
            PirateUi.border(c, b[0], b[1], b[2], b[3], holds ? PirateUi.BLUE : 0xFFB8CCE4);
            int capW = Math.max(12, c.width(data.slotKeys()[i]) + 4);
            c.fill(b[0], b[1], b[0] + capW, b[3], PirateUi.BLUE_INK);
            c.centered(data.slotKeys()[i], b[0] + capW / 2, b[1] + 2, 0xFFFFFFFF, false);
            String name = data.slotNames()[i];
            c.text(c.fit(name.isEmpty() ? "-" : name, b[2] - b[0] - capW - 5), b[0] + capW + 3, b[1] + 2,
                name.isEmpty() ? 0xFF9AA4B0 : PirateUi.BLUE_INK, false);
        }
    }

    // --- the style's page, built from HexPiece's own tables (the Haki and fruit sections come from the original, client side) ---

    /** The fighting style's section: its moves (or the Swordsman's strings), its mastery and damage. */
    public static Section style(PieceProfile p) {
        FightingStyle style = p.style();
        List<Entry> entries = new ArrayList<>();
        int mastery = p.styleMastery();
        String damage = "x" + String.format(java.util.Locale.ROOT, "%.2f", PieceRules.masteryDamage(mastery)) + " damage";
        if (!style.choice()) {
            return new Section("No fighting style", "", 0xFF8A8A8A, 0, List.of("Choose one when you set sail."), entries);
        }
        if (style.sword()) {
            String[][] rows = {
                {"M1", "Ground string", "Draw Cut, Kesa, Reverse Kesa, Horizontal, Return, Thrust, Rising, Heaven Splitter: one press, one cut."},
                {"M1", "Moving string", "Dashing Draw, Advancing Cut, Pivot Cut: a cut thrown on the run."},
                {"M1", "Aerial string", "Falling Draw, Aerial Cross, Wheel, Plunge: in the air."},
                {"RMB", "Charged Slash", "Hold to charge, let go: a heavy cut. With Armament Haki a slash that flies and splits the ground."},
                {"Ctrl+M1", "Quick Draw", "Five blocks down the line in a blink, everything on it cut. Recharges in 3s."}};
            for (String[] row : rows) {
                entries.add(Entry.of(row[1], row[0], "", row[2], true, ""));
            }
        } else {
            for (Slot slot : Slot.values()) {
                StyleMove move = StyleMove.of(style, slot);
                if (move == null) {
                    continue;
                }
                String key = switch (slot) {
                    case M1_1 -> "M1 1";
                    case M1_2 -> "M1 2";
                    case M1_3 -> "M1 3";
                    case M1_4 -> "M1 4";
                    case HEAVY -> "RMB";
                    case SPECIAL_A -> "Z";
                    case SPECIAL_B -> "X";
                    case EXTRA_C -> "C";
                    case EXTRA_V -> "V";
                    case EXTRA_B -> "B";
                };
                boolean open = mastery >= move.mastery();
                String info = move.cooldown() > 0 ? String.format(java.util.Locale.ROOT, "recharges in %.1fs", move.cooldown() / 20.0) : "in the string";
                entries.add(Entry.of(move.display(), key, info, move.description(), open, "Opens at style mastery " + move.mastery()));
            }
        }
        return new Section(style.display(), mastery + "/" + PieceRules.MAX_MASTERY, style.colour(), mastery / (float) PieceRules.MAX_MASTERY,
            List.of("Mastery " + mastery + "/" + PieceRules.MAX_MASTERY + ": " + damage + ".",
                "Combat mode (R) puts the style on your hands: M1 the string, the right button the signature, Z and X the specials, C, V "
                    + "and B the show's own moves.",
                "Landing blows trains it; heavier moves train it faster."), entries);
    }

    /**
     * 2.3.7 (the owner: "Galaxy fist becomes a move of Iron fist that unlocks when the user activates RYOU haki. ONLY for iron fist."): an
     * Iron Fist page gains the Galaxy Fist (the original's Haki Projection), bindable to its quick slot ({@code quick}) on {@code key},
     * {@code open} once Ryou can be raised, else {@code lock} says what opens it. Any other style's page is left as it is.
     */
    public static Section galaxy(Section style, FightingStyle fighting, boolean open, String key, String lock, int quick) {
        if (fighting != FightingStyle.BRAWLER) {
            return style;
        }
        List<Entry> entries = new ArrayList<>(style.entries());
        entries.add(Entry.of(GALAXY, key, "with Ryou up: hold its quick slot", "Ryou up, the fist wound back and the galaxy drawn into "
            + "it: a tap throws a short burst; a full hold launches you up and brings the fist down as a galaxy that detonates where it "
            + "lands, the sky's lightning striking round it. Iron Fist's alone.", open, lock).bindable(quick));
        List<String> about = new ArrayList<>(style.about());
        about.add("With Ryou up, the Galaxy Fist is Iron Fist's: put it on a quick slot.");
        return new Section(style.title(), style.status(), style.colour(), style.progress(), about, entries);
    }

    /** 2.3.7: the original's Haki Projection, as HexPiece names it. */
    public static final String GALAXY = "Galaxy Fist";

    // --- previews ---

    /** Data for the previews: a Six Powers pirate (or marine) with Haki, and the Pika fruit or the Nika fruit. */
    public static Data sample(int tab, boolean nika) {
        int[] mastery = new int[FightingStyle.values().length];
        mastery[FightingStyle.SIX_POWERS.ordinal()] = 240;
        PieceProfile profile = new PieceProfile(Gender.FEMALE, tab % 2 == 0 ? Faction.PIRATE : Faction.MARINE, FightingStyle.SIX_POWERS, true,
            mastery, tab % 2 == 0 ? 152_000_000L : 260L, tab % 2 == 0 ? -60 : 40);
        List<Section> styleHaki = new ArrayList<>();
        styleHaki.add(style(profile));
        styleHaki.add(new Section("Armament Haki", "Adept 640/1000", 0xFF1D3F66, 0.64F, List.of("Rank Adept. Next: Supreme Armament at 700.",
            "Coated hits, guarding and Haki techniques train it."), List.of(
            Entry.of("Physical Conditioning", "-", "passive", "Fight living targets to build the body before coating awakens.", true, ""),
            Entry.of("Reinforcement", "Ctrl+R", "permanent key", "Armament coating. Coated hits, guarding and Haki techniques train Armament mastery.", true, ""),
            Entry.of("Haki Blade Cut", "Swing", "permanent key", "With Armament active, sword swings launch Haki crescents.", true, ""),
            Entry.of("Haki Leap", "Shift+Space", "permanent key", "Charge a forward Haki launch.", true, ""),
            Entry.of("Ryou", "J", "permanent key", "Late-game multiplier.", false, "Needs Armament 850 and Conqueror's 850"))));
        styleHaki.add(new Section("Observation Haki", "Novice 310/1000", 0xFF2C6BB5, 0.31F, List.of("Rank Novice."), List.of(
            Entry.of("Presence", "V", "permanent key", "Sense every presence around you.", true, ""),
            Entry.of("Precognition", "-", "passive", "See the next blow before it comes.", false, "Needs Observation 500"))));
        styleHaki.add(new Section("Conqueror's Haki", "Awakened 120/1000", 0xFF8A2030, 0.12F, List.of("Rank Awakened."), List.of(
            Entry.of("King's Awakening", nika ? "G" : "B", "tap or hold", "Release the will of a king.", true, "").bindable(100),
            Entry.of("Haki Grip", nika ? "K" : "Z", "tap or hold", "Hold the key on a target in your sights.", true, "").bindable(101),
            Entry.of("WiFi Haki", nika ? "L" : "C", "hold", "A wave of will through the ground.", false, "Needs Conqueror's 400").bindable(102))));
        List<Section> fruitPage = new ArrayList<>();
        int points = -1;
        if (nika) {
            points = 3;
            fruitPage.add(new Section("Hito Hito no Mi, Model Nika", "Level 12", 0xFFE8C26A, 0.4F, List.of("Level 12, 3 points to spend.",
                "Devil Fruit mode (R) puts the fruit on your keys."), List.of(
                Entry.of("Gum-Gum Pistol", "LMB", "tap", "A stretching punch.", true, ""),
                Entry.of("Gear 5", "Ctrl+R", "toggle", "The drums of liberation.", false, "Needs mastery 600"))));
            fruitPage.add(new Section("Upgrades", "3 points free", 0xFFE8C26A, 0.3F, List.of("Spend points on the fruit's techniques."), List.of(
                Entry.upgrade("Elastic Reach", "Pistol and Whip reach further.", 0, 2, 5, 2),
                Entry.upgrade("Rubber Body", "Blunt blows hurt less.", 1, 5, 5, 0))));
        } else {
            fruitPage.add(new Section("Pika Pika no Mi", "Mastery 420", 0xFFFFE070, 0.42F, List.of("Mastery 420.",
                "R swaps your hands between the style and the fruit; its quick powers keep their slots."), List.of(
                Entry.of("Light Beam", "G", "tap", "A beam of light from the fingertips.", true, "").bindable(0),
                Entry.of("Light Speed Kick", "K", "tap", "A kick at the speed of light.", true, "").bindable(1),
                Entry.of("Amaterasu", "-", "hold", "A sphere of light that burns everything it touches.", false, "Fruit mastery 650").bindable(3))));
        }
        String[] keys = {"G", "K", "L", "M", "B", "Z", "C", "N", "H"};
        // 2.3.7: the Galaxy Fist (the original's Haki Projection) is Iron Fist's now: not this Six Powers sample's
        String[] names = nika ? new String[] {"King's Awakening", "Haki Grip", "WiFi Haki", "", "", "", "", "", ""}
            : new String[] {"Light Beam", "Light Speed Kick", "", "", "King's Awakening", "Haki Grip", "WiFi Haki", "", ""};
        int[] abilities = nika ? new int[] {100, 101, 102, -1, -1, -1, -1, -1, -1} : new int[] {0, 1, -1, -1, 100, 101, 102, -1, -1};
        return new Data("HexB1t", profile, new int[] {640, 310, 120}, new String[] {"Adept", "Novice", "Awakened"},
            nika ? "Hito Hito no Mi, Model Nika" : "Pika Pika no Mi", 420, styleHaki, fruitPage, keys, names, abilities, points);
    }
}
