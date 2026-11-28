package com.hexhaki.piece.ui;

/**
 * 3.9.9: the Hellish Teleport menu (the owner: "a black/scary menu appears on the user's screen, 3 Buttons on the menu (also custom made
 * and cool looking) each button acts as a location save point ... a way to clear a button and reset the location to another spot.
 * rename a teleport area"), drawn on a {@link UiCanvas} so tools/preview/UiPreview judges it off-game: near-black, a great dark
 * pentagram turning slowly behind everything, embers rising; three gates, each a card with its own circle (lit and burning when bound,
 * dead and grey when empty), its numeral, its name and where it is, one big button (OPEN a bound gate, BIND HERE an empty one) and,
 * when bound, three small ones (NAME: rename it, MOVE: bind it again here, CLEAR). Renaming types into the card itself. The colours are the circle's own
 * (itsumade/ItsuArt): very dark purple-blue, pale lavender and cyan, a pink accent.
 */
public final class GateView {
    public static final int INK = 0xFF040212;
    public static final int PANEL = 0xF00C0A27;
    public static final int PANEL_LIT = 0xF0151040;
    public static final int EDGE = 0xFF2A1F6A;
    public static final int EDGE_LIT = 0xFF7A63F0;
    public static final int LAVENDER = 0xFFD9C8FF;
    public static final int CYAN = 0xFFA9E6FF;
    public static final int DIM = 0xFF6E6492;
    public static final int PINK = 0xFFFF4FC8;
    public static final int DISC = 0xFF17143F;
    public static final int FIRE = 0xFF3FE6F0;

    /** What the menu shows (S2CGates). */
    public static final class Data {
        public boolean[] set = new boolean[3];
        public String[] names = {"", "", ""};
        public int[] xs = new int[3];
        public int[] ys = new int[3];
        public int[] zs = new int[3];
        public String[] worlds = {"", "", ""};
        public String here = "";
        /** Ticks left before a gate opens again (counted down by the screen). */
        public int cooldown;
    }

    /** The gate being renamed (-1 none) and what has been typed. */
    public static final class State {
        public int renaming = -1;
        public String typed = "";
    }

    /** Where everything goes on a screen {@code w} x {@code h} (GUI pixels). */
    public record Layout(int w, int h, int cardW, int cardH, int gap, int top, int left) {
        public static Layout of(int w, int h) {
            int gap = Math.max(8, w / 40);
            int cardW = Math.min(132, (w - gap * 4) / 3);
            int cardH = Math.min(158, h - 92);
            int left = (w - cardW * 3 - gap * 2) / 2;
            int top = Math.max(52, (h - cardH) / 2 + 12);
            return new Layout(w, h, cardW, cardH, gap, top, left);
        }

        int x(int i) {
            return this.left + i * (this.cardW + this.gap);
        }
    }

    private GateView() {
    }

    /** The name of what the mouse is over: "open:i", "bind:i", "rename:i", "move:i", "clear:i", "close", or null. */
    public static String at(UiCanvas c, Layout l, Data d, State st, double mx, double my) {
        if (in(mx, my, l.w - 22, 6, l.w - 6, 22)) {
            return "close";
        }
        for (int i = 0; i < 3; i++) {
            int x = l.x(i);
            int[] big = bigButton(l, x);
            if (in(mx, my, big[0], big[1], big[2], big[3])) {
                return (d.set[i] ? "open:" : "bind:") + i;
            }
            if (d.set[i]) {
                String[] small = {"rename", "move", "clear"};
                for (int k = 0; k < 3; k++) {
                    int[] b = smallButton(l, x, k);
                    if (in(mx, my, b[0], b[1], b[2], b[3])) {
                        return small[k] + ":" + i;
                    }
                }
            }
        }
        return null;
    }

    static boolean in(double mx, double my, int x0, int y0, int x1, int y1) {
        return mx >= x0 && mx < x1 && my >= y0 && my < y1;
    }

    static int[] bigButton(Layout l, int x) {
        int y1 = l.top + l.cardH - 26;
        return new int[] {x + 10, y1 - 18, x + l.cardW - 10, y1};
    }

    static int[] smallButton(Layout l, int x, int k) {
        int w = (l.cardW - 20 - 8) / 3;
        int y0 = l.top + l.cardH - 22;
        int x0 = x + 10 + k * (w + 4);
        return new int[] {x0, y0, x0 + w, y0 + 14};
    }

    public static void draw(UiCanvas c, Layout l, Data d, State st, String over, double time) {
        c.batch(() -> {
            backdrop(c, l, time);
            for (int i = 0; i < 3; i++) {
                card(c, l, d, st, i, over, time);
            }
        });
        // the title, its rule, its line under
        c.bigCentered("HELLISH TELEPORT", l.w / 2 + 1, 15, 0xFF000000, false, 2.0F);
        c.bigCentered("HELLISH TELEPORT", l.w / 2, 14, LAVENDER, false, 2.0F);
        int rw = Math.min(170, l.w / 3);
        c.fill(l.w / 2 - rw, 34, l.w / 2 + rw, 35, 0xFF3A2A8A);
        c.fill(l.w / 2 - rw / 3, 34, l.w / 2 + rw / 3, 35, 0xFF8A6CFF);
        c.centered("Three gates. Bind them where you stand; open them from anywhere.", l.w / 2, 39, DIM, false);
        // the close mark
        boolean x = "close".equals(over);
        PirateUi.line(c, l.w - 18, 10, l.w - 10, 18, 1.6F, x ? PINK : DIM);
        PirateUi.line(c, l.w - 10, 10, l.w - 18, 18, 1.6F, x ? PINK : DIM);
        // the footer
        String foot = d.cooldown > 0 ? "The gates open again in " + (d.cooldown + 19) / 20 + " s."
            : "Everything standing on the pentagram goes through with you.";
        c.centered(foot, l.w / 2, l.top + l.cardH + 8, d.cooldown > 0 ? PINK : DIM, false);
    }

    /** Near-black, a great dark pentagram turning slowly behind it all, embers rising. */
    static void backdrop(UiCanvas c, Layout l, double time) {
        c.gradient(0, 0, l.w, l.h, 0xF2070418, 0xF5020108);
        float cx = l.w / 2.0F;
        float cy = l.h / 2.0F + 10.0F;
        float r = Math.min(l.w, l.h) * 0.47F;
        double spin = time * 0.06;
        PirateUi.circle(c, cx, cy, r + 2.0F, 0x30140E40);
        PirateUi.circle(c, cx, cy, r, 0xF0050312);
        ringLine(c, cx, cy, r, 1.2F, 0x803A2A8A);
        ringLine(c, cx, cy, r * 0.86F, 1.0F, 0x602A1F6A);
        for (int k = 0; k < 5; k++) {
            double a0 = -Math.PI / 2.0 + spin + k * 4.0 * Math.PI / 5.0;
            double a1 = -Math.PI / 2.0 + spin + (k + 1) * 4.0 * Math.PI / 5.0;
            PirateUi.line(c, cx + (float) Math.cos(a0) * r * 0.86F, cy + (float) Math.sin(a0) * r * 0.86F, cx + (float) Math.cos(a1) * r * 0.86F,
                cy + (float) Math.sin(a1) * r * 0.86F, 1.2F, 0x702A1F6A);
        }
        // runes round the backdrop's band, very faint
        for (int j = 0; j < 36; j++) {
            double a = spin + j * Math.PI * 2.0 / 36.0;
            float rx = cx + (float) Math.cos(a) * r * 0.93F;
            float ry = cy + (float) Math.sin(a) * r * 0.93F;
            glyph(c, rx, ry, a, r * 0.05F, j, 0x603A2A8A);
        }
        // embers drifting up
        for (int k = 0; k < 26; k++) {
            double u = (time * (0.05 + 0.03 * ((k * 7) % 5)) + k * 0.37) % 1.0;
            float ex = (float) (((k * 97) % 100) / 100.0 * l.w);
            float ey = (float) (l.h * (1.0 - u));
            int a = (int) (180 * Math.sin(u * Math.PI));
            c.fill((int) ex, (int) ey, (int) ex + 1, (int) ey + 1, (a << 24) | (k % 3 == 0 ? 0xFF4FC8 : 0x7A63F0));
        }
    }

    static void ringLine(UiCanvas c, float cx, float cy, float r, float width, int argb) {
        int n = Math.max(24, (int) (r * 0.8F));
        for (int i = 0; i < n; i++) {
            double a0 = i * Math.PI * 2.0 / n;
            double a1 = (i + 1) * Math.PI * 2.0 / n;
            PirateUi.line(c, cx + (float) Math.cos(a0) * r, cy + (float) Math.sin(a0) * r, cx + (float) Math.cos(a1) * r,
                cy + (float) Math.sin(a1) * r, width, argb);
        }
    }

    /** One rune (as the circle's: a stem and two or three strokes from the same little alphabet), {@code size} tall, turned to {@code a}. */
    static void glyph(UiCanvas c, float x, float y, double a, float size, int seed, int argb) {
        double[][] alphabet = {
            {0, -1, 0, 1}, {-1, -1, 1, 1}, {1, -1, -1, 1}, {-1, 1, 1, 1}, {-1, 0, 1, 0}, {-1, -1, 1, -1}, {0, 1, 1, 0.3},
            {-1, 1, 0, 0}, {0, 0, 1, 1}, {-1, -1, -1, 1}, {1, -1, 1, 1}, {0, -0.2, 1, -1}, {-1, 0.4, 0, -0.4}};
        java.util.Random r = new java.util.Random(seed * 2654435761L);
        double tx = -Math.sin(a);
        double ty = Math.cos(a);
        double nx = Math.cos(a);
        double ny = Math.sin(a);
        float hw = size * 0.64F;
        int strokes = 2 + r.nextInt(3);
        int first = r.nextInt(3);
        for (int s = 0; s < strokes; s++) {
            double[] st = alphabet[s == 0 ? (first == 0 ? 0 : first == 1 ? 9 : 10) : r.nextInt(alphabet.length)];
            PirateUi.line(c, (float) (x + tx * st[0] * hw + nx * st[1] * size), (float) (y + ty * st[0] * hw + ny * st[1] * size),
                (float) (x + tx * st[2] * hw + nx * st[3] * size), (float) (y + ty * st[2] * hw + ny * st[3] * size), 1.0F, argb);
        }
    }

    static final String[] NUMERALS = {"I", "II", "III"};

    static void card(UiCanvas c, Layout l, Data d, State st, int i, String over, double time) {
        int x = l.x(i);
        int y = l.top;
        boolean set = d.set[i];
        boolean hot = over != null && over.endsWith(":" + i);
        // the panel, its edge (lit when the mouse is on it), a thin inner line
        c.fill(x - 1, y - 1, x + l.cardW + 1, y + l.cardH + 1, hot ? EDGE_LIT : EDGE);
        c.gradient(x, y, x + l.cardW, y + l.cardH, set ? PANEL_LIT : PANEL, 0xF8050312);
        PirateUi.border(c, x + 3, y + 3, x + l.cardW - 3, y + l.cardH - 3, set ? 0x503A2A8A : 0x30302848);
        // its circle (everything above the buttons moved up on a shorter card, a small screen's)
        int up = Math.max(0, 158 - l.cardH);
        float ex = x + l.cardW / 2.0F;
        float ey = y + 34.0F - up * 0.4F;
        emblem(c, ex, ey, 22.0F - up * 0.3F, set, time + i * 1.7, hot);
        // its numeral, its name, where it is
        c.bigCentered(NUMERALS[i], x + l.cardW / 2 + 1, y + 62 - up * 3 / 4, 0xFF000000, false, 1.5F);
        c.bigCentered(NUMERALS[i], x + l.cardW / 2, y + 61 - up * 3 / 4, set ? LAVENDER : DIM, false, 1.5F);
        int nameY = y + 78 - up;
        if (st.renaming == i) {
            c.fill(x + 8, nameY - 2, x + l.cardW - 8, nameY + 10, 0xFF020110);
            PirateUi.border(c, x + 8, nameY - 2, x + l.cardW - 8, nameY + 10, EDGE_LIT);
            String typed = fit(c, st.typed, l.cardW - 22);
            boolean caret = ((int) (time * 2.0)) % 2 == 0;
            c.text(typed + (caret ? "_" : ""), x + 12, nameY, LAVENDER, false);
            c.centered("Enter keeps it, Esc does not", x + l.cardW / 2, nameY + 14, DIM, false);
        } else if (set) {
            c.centered(fit(c, d.names[i], l.cardW - 12), x + l.cardW / 2, nameY, LAVENDER, false);
            String where = d.xs[i] + "  " + d.ys[i] + "  " + d.zs[i];
            c.centered(fit(c, where, l.cardW - 12), x + l.cardW / 2, nameY + 12, CYAN, false);
            if (!d.worlds[i].equals(d.here)) {
                c.centered("another world", x + l.cardW / 2, nameY + 24, PINK, false);
            }
        } else {
            c.centered("unbound", x + l.cardW / 2, nameY, DIM, false);
            c.centered("bind it where you stand", x + l.cardW / 2, nameY + 12, 0xFF4E466E, false);
        }
        // the big button
        int[] b = bigButton(l, x);
        boolean overBig = over != null && over.equals((set ? "open:" : "bind:") + i);
        button(c, b, set ? "OPEN" : "BIND HERE", overBig, set && d.cooldown <= 0 && d.worlds[i].equals(d.here) || !set);
        // the small ones
        if (set) {
            String[] labels = {"NAME", "MOVE", "CLEAR"};
            String[] keys = {"rename:", "move:", "clear:"};
            for (int k = 0; k < 3; k++) {
                int[] s = smallButton(l, x, k);
                boolean on = over != null && over.equals(keys[k] + i);
                c.fill(s[0], s[1], s[2], s[3], on ? 0xFF2A1F6A : 0xFF0C0A27);
                PirateUi.border(c, s[0], s[1], s[2], s[3], on ? EDGE_LIT : 0xFF2A1F6A);
                c.centered(labels[k], (s[0] + s[2]) / 2, s[1] + 3, k == 2 ? (on ? PINK : 0xFFB04A8E) : (on ? LAVENDER : DIM), false);
            }
        }
    }

    static void button(UiCanvas c, int[] b, String label, boolean over, boolean live) {
        int fill = !live ? 0xFF0E0B22 : over ? 0xFF3A2490 : 0xFF1E1650;
        c.fill(b[0], b[1], b[2], b[3], fill);
        c.fill(b[0], b[3] - 1, b[2], b[3], over && live ? PINK : 0xFF3A2A8A);
        PirateUi.border(c, b[0], b[1], b[2], b[3], over && live ? EDGE_LIT : EDGE);
        c.centered(label, (b[0] + b[2]) / 2, b[1] + 5, live ? (over ? 0xFFFFFFFF : LAVENDER) : DIM, false);
    }

    /** A gate's own little circle: dark disc, ring, star; lit pale with cyan and pink fire licking round it when bound, dead when empty. */
    static void emblem(UiCanvas c, float x, float y, float r, boolean set, double time, boolean hot) {
        if (set) {
            // fire round it
            for (int k = 0; k < 18; k++) {
                double a = k * Math.PI * 2.0 / 18.0;
                double flick = 0.6 + 0.4 * Math.sin(time * 5.0 + k * 2.3);
                float fx = x + (float) Math.cos(a) * (r + 2.0F);
                float fy = y + (float) Math.sin(a) * (r + 2.0F);
                float tall = (float) (3.5 + 3.0 * flick) * (hot ? 1.3F : 1.0F);
                PirateUi.triangle(c, fx - 2.2F, fy, fx + 2.2F, fy, fx + (float) Math.cos(a) * 0.8F, fy - tall, k % 2 == 0 ? 0xC0FF4FC8 : 0xD03FE6F0);
            }
        }
        PirateUi.circle(c, x, y, r + 1.0F, set ? 0xFF2A1F6A : 0xFF1A1630);
        PirateUi.circle(c, x, y, r, set ? DISC : 0xFF0C0A1A);
        int line = set ? (hot ? 0xFFF2FDFF : 0xFFA9E6FF) : 0xFF3A3458;
        ringLine(c, x, y, r * 0.84F, 1.0F, line);
        for (int k = 0; k < 5; k++) {
            double a0 = -Math.PI / 2.0 + k * 4.0 * Math.PI / 5.0;
            double a1 = -Math.PI / 2.0 + (k + 1) * 4.0 * Math.PI / 5.0;
            PirateUi.line(c, x + (float) Math.cos(a0) * r * 0.84F, y + (float) Math.sin(a0) * r * 0.84F, x + (float) Math.cos(a1) * r * 0.84F,
                y + (float) Math.sin(a1) * r * 0.84F, 1.0F, line);
        }
        if (set) {
            for (int j = 0; j < 14; j++) {
                double a = j * Math.PI * 2.0 / 14.0 + time * 0.2;
                glyph(c, x + (float) Math.cos(a) * r * 0.92F, y + (float) Math.sin(a) * r * 0.92F, a, 1.6F, j, 0xFFD9C8FF);
            }
        }
    }

    /** {@code s} cut to fit {@code width} pixels, an ellipsis where it was cut. */
    static String fit(UiCanvas c, String s, int width) {
        if (c.width(s) <= width) {
            return s;
        }
        String t = s;
        while (t.length() > 1 && c.width(t + "...") > width) {
            t = t.substring(0, t.length() - 1);
        }
        return t + "...";
    }
}
