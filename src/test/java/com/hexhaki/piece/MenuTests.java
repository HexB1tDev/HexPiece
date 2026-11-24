package com.hexhaki.piece;

import com.hexhaki.piece.ui.LogbookView;
import com.hexhaki.piece.ui.Silhouette;
import com.hexhaki.piece.ui.UiCanvas;
import java.util.List;

/**
 * 2.2.4: the one menu (the Captain's Log: Overview, Style &amp; Haki, Devil Fruit) and the start menu's silhouettes: the pages' rows,
 * scrolling, picking, binding and hit tests; the figures' outlines and the shape fill that draws them.
 */
public final class MenuTests {
    /**
     * 2.4.6 (the owner: "100 is pure good, 0 is neutal, -100 pure evil"): the alignment's rules, the sounds evil plays by gender, and the
     * log's bar (pure evil at its left end, neutral in the middle, pure good at its right end).
     */
    static void alignment() {
        Check.that(com.hexhaki.piece.Alignment.clamp(-250) == -100 && com.hexhaki.piece.Alignment.clamp(250) == 100
            && com.hexhaki.piece.Alignment.clamp(7) == 7, "alignment runs -100..100");
        Check.that("Pure Evil".equals(com.hexhaki.piece.Alignment.label(-100)) && "Evil".equals(com.hexhaki.piece.Alignment.label(-60))
            && "Neutral".equals(com.hexhaki.piece.Alignment.label(0)) && "Good".equals(com.hexhaki.piece.Alignment.label(40))
            && "Pure Good".equals(com.hexhaki.piece.Alignment.label(100)), "its names along the bar");
        Check.that(com.hexhaki.piece.Alignment.evil(-25) && !com.hexhaki.piece.Alignment.evil(-24) && com.hexhaki.piece.Alignment.good(25)
            && !com.hexhaki.piece.Alignment.good(24) && !com.hexhaki.piece.Alignment.evil(0) && !com.hexhaki.piece.Alignment.good(0),
            "evil from -25 down, good from 25 up, neutral between");
        Check.near(com.hexhaki.piece.Alignment.along(-100), 0.0, 1e-9, "pure evil at the bar's left end");
        Check.near(com.hexhaki.piece.Alignment.along(0), 0.5, 1e-9, "neutral in its middle");
        Check.near(com.hexhaki.piece.Alignment.along(100), 1.0, 1e-9, "pure good at its right end");
        Check.that(com.hexhaki.piece.Alignment.evilVoice(Gender.FEMALE).endsWith("_female")
            && com.hexhaki.piece.Alignment.evilVoice(Gender.MALE).endsWith("_male")
            && com.hexhaki.piece.Alignment.evilVoice(Gender.UNSET).endsWith("_male")
            && com.hexhaki.piece.Alignment.evilRelease(Gender.FEMALE).endsWith("_female"), "a woman's voice for a woman, a man's otherwise");
        for (String sound : com.hexhaki.piece.Alignment.SOUNDS) {
            Check.that(java.util.Arrays.asList(com.hexhaki.piece.Alignment.SOUNDS).contains(com.hexhaki.piece.Alignment.evilVoice(Gender.FEMALE))
                && sound.startsWith("piece.conqueror.evil_"), "every alignment sound is registered (" + sound + ")");
        }
        // the log draws it on the overview: the pointer where the value is, red filled towards evil, gold towards good
        LogbookView.Data evil = LogbookView.sample(0, false);
        Check.that(evil.profile().alignment() == -60, "the preview's pirate is evil");
    }

    public static void main(String[] args) {
        rows();
        scrolling();
        binding();
        hits();
        nika();
        silhouettes();
        fill();
        alignment();
        Check.done("MenuTests");
    }

    /** Measures text at 6 px a letter and adds up the area it is asked to fill (in unscaled pixels). */
    static final class Measure implements UiCanvas {
        double area;
        float scale = 1;
        final java.util.ArrayDeque<Float> stack = new java.util.ArrayDeque<>();

        public void fill(int x0, int y0, int x1, int y1, int argb) {
            this.area += Math.abs((double) (x1 - x0) * (y1 - y0)) * this.scale * this.scale;
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
            this.stack.push(this.scale);
        }

        public void translate(float x, float y) {
        }

        public void scale(float factor) {
            this.scale *= factor;
        }

        public void pop() {
            this.scale = this.stack.pop();
        }
    }

    static void rows() {
        LogbookView.Data data = LogbookView.sample(0, false);
        List<LogbookView.Row> rows = LogbookView.rows(data.page(LogbookView.STYLE_HAKI));
        int headings = 0;
        for (LogbookView.Row row : rows) {
            headings += row.heading() ? 1 : 0;
        }
        Check.that(headings == 4, "Style & Haki: the style and the three Haki paths, each a heading");
        Check.that(rows.get(0).heading() && data.styleHaki().get(0).title().equals("Six Powers"), "the style comes first, above the Haki");
        Check.that(data.page(LogbookView.OVERVIEW).isEmpty(), "the overview has no list");
        int entries = 0;
        for (LogbookView.Section s : data.styleHaki()) {
            entries += s.entries().size();
        }
        Check.that(rows.size() == headings + entries, "every entry is one row under its heading");
        LogbookView.Section style = LogbookView.style(data.profile());
        Check.that(style.entries().size() == 10, "Six Powers lists its four string hits, the signature, both specials and its three extras (2.8.4)");
        Check.that(style.entries().get(7).key().equals("C") && style.entries().get(9).key().equals("B"), "the extras on C, V and B");
        Check.that(style.entries().get(4).key().equals("RMB") && style.entries().get(5).key().equals("Z"), "with their keys (2.7.1: the specials on Z and X)");
    }

    static void scrolling() {
        LogbookView.Data data = LogbookView.sample(0, false);
        for (int[] size : new int[][] {{427, 240}, {640, 360}, {960, 540}}) {
            LogbookView.Layout l = LogbookView.Layout.of(size[0], size[1]);
            Check.that(l.x0() >= 0 && l.x1() <= size[0] && l.y0() >= 0 && l.y1() <= size[1], "the panel fits a " + size[0] + "x" + size[1] + " screen");
            List<LogbookView.Row> rows = LogbookView.rows(data.page(LogbookView.STYLE_HAKI));
            int max = LogbookView.maxScroll(l, rows);
            Check.that(max >= 0 && max < rows.size(), "scrolling stops with the last rows filling the list");
            LogbookView.State state = new LogbookView.State(LogbookView.STYLE_HAKI);
            state.picked = rows.size() - 1;
            LogbookView.reveal(l, rows, state);
            Check.that(state.scroll == max, "picking the last row scrolls to the end");
            state.picked = 0;
            LogbookView.reveal(l, rows, state);
            Check.that(state.scroll == 0, "picking the first row scrolls back to the top");
        }
    }

    static void binding() {
        LogbookView.Data data = LogbookView.sample(0, false);
        List<LogbookView.Row> rows = LogbookView.rows(data.page(LogbookView.STYLE_HAKI));
        LogbookView.State state = new LogbookView.State(LogbookView.STYLE_HAKI);
        int open = -1;
        int locked = -1;
        for (int i = 0; i < rows.size(); i++) {
            LogbookView.Entry e = LogbookView.entry(data.page(state.tab), rows.get(i));
            if (e != null && e.ability() >= 0 && e.open() && open < 0) {
                open = i;
            }
            if (e != null && e.ability() >= 0 && !e.open()) {
                locked = i;
            }
        }
        state.picked = open;
        Check.that(LogbookView.bindable(data, state) == 100, "a quick power picked can go on a slot (King's Awakening, 100)");
        // 2.3.7: the Galaxy Fist (the original's Haki Projection, 103) is no longer Armament's; 3.5.7: nor Iron Fist's (its moves are
        // gone until it is remade): no page offers it
        for (LogbookView.Section section : data.styleHaki()) {
            for (LogbookView.Entry e : section.entries()) {
                Check.that(e.ability() != 103, "no page offers the Galaxy Fist (" + section.title() + ")");
            }
        }
        int[] mastery = new int[com.hexhaki.piece.style.FightingStyle.values().length];
        com.hexhaki.piece.PieceProfile iron = new com.hexhaki.piece.PieceProfile(Gender.MALE, Faction.PIRATE,
            com.hexhaki.piece.style.FightingStyle.BRAWLER, true, mastery, 0L, 0);
        LogbookView.Section ironPage = LogbookView.style(iron);
        Check.that(ironPage.entries().size() == 8 && "Galaxy Impact".equals(ironPage.entries().get(7).name()),
            "Iron Fist's page: Garp's eight, Galaxy Impact last (3.5.8)");
        state.picked = locked;
        Check.that(LogbookView.bindable(data, state) == -1, "a locked one cannot");
        state.picked = 0;
        Check.that(LogbookView.bindable(data, state) == -1, "nor a heading");
        state.tab = LogbookView.FRUIT;
        state.picked = 1;
        Check.that(LogbookView.bindable(data, state) == 0, "a kit fruit's quick move can (its slot number)");
    }

    static void hits() {
        Measure c = new Measure();
        LogbookView.Data data = LogbookView.sample(0, false);
        LogbookView.Layout l = LogbookView.Layout.of(640, 360);
        LogbookView.State state = new LogbookView.State(LogbookView.STYLE_HAKI);
        int listX = l.x0() + 20;
        Check.that(("row:0").equals(LogbookView.at(c, l, data, state, listX, l.y0() + 46)), "the list's first row is under the pointer at its top");
        state.scroll = 3;
        Check.that(("row:3").equals(LogbookView.at(c, l, data, state, listX, l.y0() + 46)), "scrolled, the top row is the first shown");
        int[] tab = {l.x0() + 8 + (l.x1() - l.x0() - 16) / 3 * 2 + 10, l.y0() + 28};
        Check.that(("tab:2").equals(LogbookView.at(c, l, data, state, tab[0], tab[1])), "the third tab is the Devil Fruit");
        Check.that(("close").equals(LogbookView.at(c, l, data, state, l.x1() - 14, l.y1() - 14)), "Close sits at the bottom right");
        Check.that(("reset").equals(LogbookView.at(c, l, data, state, l.x1() - 14 - 30 - 6 - 30, l.y1() - 14)),
            "Reset Slots beside it on a page with quick powers");
        // the grid: three columns, three rows, under the detail
        int right = l.x1() - 8;
        int bottom = l.y1() - 27;
        Check.that(("slot:8").equals(LogbookView.at(c, l, data, state, right - 10, bottom - 5)), "the last slot is the grid's bottom right");
        Check.that(LogbookView.at(c, l, data, new LogbookView.State(LogbookView.OVERVIEW), right - 10, bottom - 5) == null,
            "the overview has no slots");
    }

    static void nika() {
        Measure c = new Measure();
        LogbookView.Data data = LogbookView.sample(2, true);
        LogbookView.Layout l = LogbookView.Layout.of(640, 360);
        LogbookView.State state = new LogbookView.State(LogbookView.FRUIT);
        Check.that(data.points() == 3 && data.fruitPage().size() == 2, "the Nika fruit: its techniques and its upgrades, with free points");
        int right = l.x1() - 8;
        int bottom = l.y1() - 27;
        Check.that(LogbookView.at(c, l, data, state, right - 10, bottom - 5) == null, "no quick slots without a quick power on the page");
        Check.that(("respec").equals(LogbookView.at(c, l, data, state, l.x1() - 14 - 30 - 6 - 20, l.y1() - 14)), "Respec beside Close");
        state.picked = 4;
        LogbookView.Entry upgrade = LogbookView.pickedEntry(data, state);
        Check.that(upgrade != null && upgrade.upgrade() == 0 && upgrade.cost() == 2, "an upgrade picked: its index and the next rank's cost");
        Check.that(("buy").equals(LogbookView.at(c, l, data, state, right - 30, l.y0() + 42 + 5 + 36 + 4)), "its Buy button under the name");
    }

    /** The figures: closed, mirrored, eight heads tall, the man broad in the shoulders, the woman narrow in the waist. */
    static void silhouettes() {
        for (boolean woman : new boolean[] {false, true}) {
            float[][] o = Silhouette.outline(woman);
            float[] xs = o[0];
            float[] ys = o[1];
            float top = Float.MAX_VALUE;
            float bottom = -Float.MAX_VALUE;
            float wide = 0;
            for (int i = 0; i < xs.length; i++) {
                top = Math.min(top, ys[i]);
                bottom = Math.max(bottom, ys[i]);
                wide = Math.max(wide, Math.abs(xs[i]));
            }
            String who = woman ? "the woman" : "the man";
            Check.near(top, 0, 0.02, who + "'s crown at the top");
            Check.near(bottom, Silhouette.HEADS, 0.02, who + "'s soles eight heads down");
            Check.that(wide < 1.45F, who + " fits a card (arms out at most 1.45 heads)");
            Check.near(area(xs, ys, 1), area(xs, ys, -1), 0.02, who + " is mirrored left to right");
            for (int i = 0; i < xs.length; i++) {
                int j = (i + 1) % xs.length;
                Check.that(Math.hypot(xs[j] - xs[i], ys[j] - ys[i]) < 0.12, who + "'s outline is smooth (no long straight jumps)");
            }
        }
        Check.that(halfWidth(false, 1.62F) > halfWidth(true, 1.62F) + 0.15F, "the man's shoulders are broader");
        Check.that(halfWidth(true, 3.95F) > halfWidth(true, 3.1F) * 1.3F, "the woman's hips are well wider than her waist");
        Check.that(halfWidth(true, 3.1F) < halfWidth(false, 3.1F), "her waist is narrower than his");
    }

    /** Twice the area of the outline's half on one side of the middle line ({@code side} 1 right, -1 left), clipped at x = 0. */
    static double area(float[] xs, float[] ys, int side) {
        double sum = 0;
        for (int i = 0; i < xs.length; i++) {
            int j = (i + 1) % xs.length;
            double x0 = Math.max(0, side * xs[i]);
            double x1 = Math.max(0, side * xs[j]);
            sum += (x0 + x1) * (ys[j] - ys[i]) / 2;
        }
        return Math.abs(sum);
    }

    /** The body's half width at height y (its outermost edge left of the arms: the first crossing out from the middle). */
    static float halfWidth(boolean woman, float y) {
        float[][] o = Silhouette.outline(woman);
        float best = Float.MAX_VALUE;
        for (int i = 0; i < o[0].length; i++) {
            int j = (i + 1) % o[0].length;
            float y0 = o[1][i];
            float y1 = o[1][j];
            if ((y0 <= y && y1 > y || y1 <= y && y0 > y)) {
                float x = o[0][i] + (y - y0) / (y1 - y0) * (o[0][j] - o[0][i]);
                if (x > 0.01F) {
                    best = Math.min(best, x);
                }
            }
        }
        return best;
    }

    /** The shape fill covers what the outline encloses (to within its quarter-pixel rows), concave parts and gaps left empty. */
    static void fill() {
        for (boolean woman : new boolean[] {false, true}) {
            float[][] o = Silhouette.outline(woman);
            float height = 120;
            float unit = height / Silhouette.HEADS;
            float[] xs = new float[o[0].length];
            float[] ys = new float[o[0].length];
            for (int i = 0; i < xs.length; i++) {
                xs[i] = 100 + o[0][i] * unit;
                ys[i] = 10 + o[1][i] * unit;
            }
            double exact = 0;
            for (int i = 0; i < xs.length; i++) {
                int j = (i + 1) % xs.length;
                exact += (xs[i] * ys[j] - xs[j] * ys[i]) / 2.0;
            }
            Measure c = new Measure();
            c.shape(xs, ys, 0xFF000000);
            Check.near(c.area / Math.abs(exact), 1.0, 0.03, (woman ? "the woman" : "the man") + " is filled as drawn");
            Check.that(c.scale == 1, "the fill leaves the canvas's scale as it found it");
        }
        // an L: the notch stays empty (a convex fill would close it)
        Measure c = new Measure();
        c.shape(new float[] {0, 10, 10, 4, 4, 0}, new float[] {0, 0, 4, 4, 10, 10}, 0xFF000000);
        Check.near(c.area, 10 * 4 + 4 * 6, 0.5, "a concave shape keeps its notch");
    }
}
