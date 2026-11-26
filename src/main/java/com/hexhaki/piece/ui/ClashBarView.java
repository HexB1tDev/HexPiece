package com.hexhaki.piece.ui;

/**
 * 3.8.2: the Conqueror's Haki clash's bar (the owner: "a bar appears on the middle of the screen "TAP M1" whoever manages to get their
 * bar color to the enemy's side wins, the bar has to be really well made and decorated"), drawn on any {@link UiCanvas} (the game's
 * GuiCanvas, the UiPreview renders). One's own colour fills from the left, the foe's from the right; where they meet a white-hot
 * jagged seam throws sparks. A black iron frame with a gold inlay, spiked ends, a crown of spikes over the middle; "TAP M1" over it
 * pulsing with each tap; the time left under it; at the end VICTORY or DEFEATED. Pure: everything from {@link State}.
 */
public final class ClashBarView {
    /** What the bar shows. */
    public static final class State {
        /** -1 (all the foe's) .. 1 (all one's own), 0 at the start. */
        public double mine;
        public int myColor = 0xFFE0242C;
        public int foeColor = 0xFF2A7BFF;
        public String myName = "You";
        public String foeName = "Foe";
        /** 0..1: the bar growing in out of the collision. */
        public double shown = 1.0;
        /** Ticks since the clash began (drives the bar's life), with the partial tick. */
        public double age;
        /** 0..1: how fresh one's last tap is (1 the moment it lands). */
        public double tap;
        /** Seconds left. */
        public int left = 15;
        /** 1 won, -1 lost, 0 still on. */
        public int won;
    }

    static final int INK = 0xFF07050A;
    static final int IRON = 0xFF1C1822;
    static final int IRON_HI = 0xFF3A3442;
    static final int GOLD = 0xFFE2B04A;
    static final int GOLD_DEEP = 0xFF9A6A1E;
    static final int WHITE = 0xFFFFFFFF;

    private ClashBarView() {
    }

    /** The bar's half width on a screen {@code width} GUI pixels wide. */
    public static int halfWidth(int width) {
        return Math.max(70, Math.min(150, (int) (width * 0.34)));
    }

    public static void draw(UiCanvas c, int width, int height, State s) {
        double shown = clamp(s.shown, 0.0, 1.0);
        if (shown <= 0.0) {
            return;
        }
        int cx = width / 2;
        int cy = (int) (height * 0.72);
        int half = (int) Math.round(halfWidth(width) * ease(shown));
        int barH = 14;
        int top = cy - barH / 2;
        int bottom = top + barH;
        int left = cx - half;
        int right = cx + half;
        int alpha = (int) (255 * shown);
        c.batch(() -> {
            // a dark band behind it all, fading out toward its edges, so it reads over any sky
            for (int k = 0; k < 10; k++) {
                c.fill(left - 46 + k * 4, top - 46 + k * 2, right + 46 - k * 4, bottom + 24 - k, 14 << 24);
            }
            frame(c, left, top, right, bottom, cx, s.age, s.tap);
            fills(c, left, top, right, bottom, s);
            spikes(c, left, right, cy, barH);
            gem(c, left - 3, cy, s.myColor, s.age);
            gem(c, right + 3, cy, s.foeColor, s.age + 7);
            medallion(c, cx, top, s.age, s.tap);
        });
        // the words: TAP M1 over it (or how it ended), the time left and the two names under it
        if (s.won == 0) {
            float pulse = (float) (1.0 + 0.16 * s.tap + 0.04 * Math.sin(s.age * 0.5));
            int glow = mix(GOLD, WHITE, 0.55 * s.tap);
            int y = top - 36;
            outlined(c, "TAP M1", cx, y, withAlpha(glow, alpha), 1.6F * pulse);
            int reach = (int) (c.width("TAP M1") * 0.8F * pulse) + 8;
            double bob = 2.0 * Math.sin(s.age * 0.6);
            chevrons(c, (int) (cx - reach - bob), y + 6, 1, withAlpha(GOLD, alpha));
            chevrons(c, (int) (cx + reach + bob), y + 6, -1, withAlpha(GOLD, alpha));
            String time = s.left + "s";
            c.centered(time, cx, bottom + 8, withAlpha(0xFFD8D0C0, alpha), true);
        } else {
            boolean won = s.won > 0;
            outlined(c, won ? "VICTORY" : "DEFEATED", cx, top - 38, withAlpha(won ? GOLD : 0xFFB8B0C0, alpha), 2.0F);
        }
        String mine = c.fit(s.myName, half - 16);
        String foe = c.fit(s.foeName, half - 16);
        c.text(mine, left + 6, bottom + 8, withAlpha(mix(s.myColor, WHITE, 0.45), alpha), true);
        c.text(foe, right - 6 - c.width(foe), bottom + 8, withAlpha(mix(s.foeColor, WHITE, 0.45), alpha), true);
    }

    /** Three gold chevrons pointing at the words ({@code way} 1 rightward). */
    static void chevrons(UiCanvas c, int x, int y, int way, int argb) {
        for (int k = 0; k < 3; k++) {
            float bx = x - way * k * 5;
            int a = (int) (((argb >>> 24) & 255) * (1.0 - k * 0.3));
            c.shape(new float[] {bx - way * 3, bx + way * 1, bx - way * 3, bx - way * 1}, new float[] {y - 4, y, y + 4, y}, a << 24 | argb & 0xFFFFFF);
        }
    }

    /** A cut gem at the end of the bar in a fighter's colour, catching the light. */
    static void gem(UiCanvas c, int x, int y, int color, double age) {
        c.shape(new float[] {x - 8, x, x + 8, x}, new float[] {y, y - 9, y, y + 9}, INK);
        c.shape(new float[] {x - 6, x, x + 6, x}, new float[] {y, y - 7, y, y + 7}, GOLD);
        c.shape(new float[] {x - 4.5F, x, x + 4.5F, x}, new float[] {y, y - 5.5F, y, y + 5.5F}, scale(color, 0.7));
        c.shape(new float[] {x - 4.5F, x, x, x - 1}, new float[] {y, y - 5.5F, y, y + 1}, mix(color, WHITE, 0.35));
        double glint = 0.5 + 0.5 * Math.sin(age * 0.25);
        c.shape(new float[] {x - 2, x - 1, x, x - 1}, new float[] {y - 2, y - 3.5F, y - 2, y - 0.5F}, withAlpha(WHITE, (int) (120 + 135 * glint)));
    }

    /** The medallion over the middle of the bar: black and gold, the crown of spikes on it, white-hot as a tap lands. */
    static void medallion(UiCanvas c, int cx, int top, double age, double tap) {
        int y = top - 6;
        c.shape(new float[] {cx - 11, cx, cx + 11, cx}, new float[] {y, y - 11, y, y + 11}, INK);
        c.shape(new float[] {cx - 9, cx, cx + 9, cx}, new float[] {y, y - 9, y, y + 9}, GOLD_DEEP);
        c.shape(new float[] {cx - 7, cx, cx + 7, cx}, new float[] {y, y - 7, y, y + 7}, IRON);
        crown(c, cx, y + 3, age, tap);
        c.fill(cx - 1, top, cx + 1, top + 14, 0x60FFFFFF);
    }

    /** The black iron frame round the bar, its double gold inlay, rivets, a light running along it; brighter as a tap lands. */
    static void frame(UiCanvas c, int left, int top, int right, int bottom, int cx, double age, double tap) {
        int pad = 5;
        float mid = (top + bottom) / 2.0F;
        c.shape(new float[] {left - pad - 7, left - pad, right + pad, right + pad + 7, right + pad, left - pad},
            new float[] {mid, top - pad, top - pad, mid, bottom + pad, bottom + pad}, 0xD0000000);
        c.shape(new float[] {left - pad - 5, left - pad + 1, right + pad - 1, right + pad + 5, right + pad - 1, left - pad + 1},
            new float[] {mid, top - pad + 1, top - pad + 1, mid, bottom + pad - 1, bottom + pad - 1}, IRON);
        int gold = mix(GOLD, WHITE, 0.6 * tap);
        // the outer inlay on the iron, the inner one round the bar
        c.fill(left - pad + 2, top - pad + 2, right + pad - 2, top - pad + 3, mix(GOLD_DEEP, gold, 0.5));
        c.fill(left - pad + 2, bottom + pad - 3, right + pad - 2, bottom + pad - 2, GOLD_DEEP);
        c.fill(left - 2, top - 2, right + 2, top - 1, gold);
        c.fill(left - 2, bottom + 1, right + 2, bottom + 2, GOLD_DEEP);
        c.fill(left - 2, top - 1, left - 1, bottom + 1, GOLD_DEEP);
        c.fill(right + 1, top - 1, right + 2, bottom + 1, GOLD_DEEP);
        for (int x = left + 12; x < right - 8; x += 24) {
            if (Math.abs(x - cx) > 14) {
                c.fill(x, top - pad + 1, x + 1, top - pad + 2, IRON_HI);
                c.fill(x, bottom + pad - 2, x + 1, bottom + pad - 1, IRON_HI);
            }
        }
        double run = (age * 3.0) % Math.max(1, right - left + 40) - 20;
        int sx = (int) (left + run);
        if (sx > left && sx < right - 10) {
            c.fill(sx, top - 2, sx + 10, top - 1, 0xFFFFF1C8);
            c.fill(sx + 3, top - 2, sx + 6, top - 1, WHITE);
        }
        c.fill(left, top, right, bottom, INK);
    }

    /** The two colours: one's own from the left, the foe's from the right, a jagged white-hot seam where they meet. */
    static void fills(UiCanvas c, int left, int top, int right, int bottom, State s) {
        double t = (clamp(s.mine, -1.0, 1.0) + 1.0) / 2.0;
        int seam = (int) Math.round(left + (right - left) * t);
        int h = bottom - top;
        if (seam > left) {
            c.gradient(left, top, seam, bottom, mix(s.myColor, WHITE, 0.25), scale(s.myColor, 0.55));
            streaks(c, left, top, seam, bottom, s.age, 1, s.myColor);
        }
        if (seam < right) {
            c.gradient(seam, top, right, bottom, mix(s.foeColor, WHITE, 0.25), scale(s.foeColor, 0.55));
            streaks(c, seam, top, right, bottom, s.age, -1, s.foeColor);
        }
        // the gloss along the top third
        c.fill(left, top + 1, right, top + Math.max(2, h / 3), 0x30FFFFFF);
        c.fill(left, bottom - 1, right, bottom, 0x50000000);
        if (seam > left && seam < right) {
            // the seam: a zig-zag of white-hot light, ink either side, flickering
            int jag = (int) (Math.floor(s.age * 2.0) % 3);
            float[] xs = {seam - 2, seam + 2 + jag - 1, seam - 1, seam + 3, seam, seam - 3 + jag, seam + 1};
            float[] ys = {top - 3, top + h * 0.3F, top + h * 0.45F, top + h * 0.7F, bottom + 3, top + h * 0.6F, top + h * 0.35F};
            c.shape(new float[] {seam - 4, seam + 4, seam + 4, seam - 4}, new float[] {top, top, bottom, bottom}, 0x90FFFFFF);
            c.shape(xs, ys, WHITE);
            sparks(c, seam, (top + bottom) / 2, s.age, s.myColor, s.foeColor);
        }
    }

    /** Light moving along a fill toward the seam ({@code way} 1 rightward, -1 leftward). */
    static void streaks(UiCanvas c, int x0, int top, int x1, int bottom, double age, int way, int color) {
        int span = x1 - x0;
        if (span < 6) {
            return;
        }
        int light = withAlpha(mix(color, WHITE, 0.7), 110);
        for (int k = 0; k < 4; k++) {
            double p = ((age * 4.0 + k * 37.0) % 60.0) / 60.0;
            int x = way > 0 ? (int) (x0 + span * p) : (int) (x1 - span * p);
            int len = 6 + k * 2;
            int from = way > 0 ? x - len : x;
            int to = way > 0 ? x : x + len;
            from = Math.max(x0, from);
            to = Math.min(x1, to);
            if (to > from) {
                int row = top + 2 + (k % 3) * Math.max(1, (bottom - top - 4) / 3);
                c.fill(from, row, to, row + 1, light);
            }
        }
    }

    /** Sparks thrown out of the seam, both colours. */
    static void sparks(UiCanvas c, int x, int y, double age, int a, int b) {
        for (int k = 0; k < 8; k++) {
            double life = ((age * 0.9 + k * 0.37) % 1.0);
            double angle = k * 2.399963 + Math.floor(age * 0.9 + k * 0.37) * 1.7;
            double r = 4 + life * 16;
            float sx = (float) (x + Math.cos(angle) * r);
            float sy = (float) (y + Math.sin(angle) * r * 0.6);
            float size = (float) (1.6 * (1.0 - life) + 0.4);
            int col = withAlpha(mix(k % 2 == 0 ? a : b, WHITE, 0.5), (int) (230 * (1.0 - life)));
            c.shape(new float[] {sx - size, sx, sx + size, sx}, new float[] {sy, sy - size, sy, sy + size}, col);
        }
    }

    /** The spiked ends of the frame. */
    static void spikes(UiCanvas c, int left, int right, int cy, int barH) {
        for (int side = -1; side <= 1; side += 2) {
            int x = side < 0 ? left - 10 : right + 10;
            c.shape(new float[] {x, x + side * 16, x}, new float[] {cy - 7, cy, cy + 7}, INK);
            c.shape(new float[] {x, x + side * 12, x}, new float[] {cy - 4, cy, cy + 4}, GOLD_DEEP);
            c.shape(new float[] {x, x + side * 9, x}, new float[] {cy - 2, cy, cy}, GOLD);
        }
    }

    /** The crown of spikes over the middle of the frame: the Haki's mark, brighter with each tap. */
    static void crown(UiCanvas c, int cx, int top, double age, double tap) {
        int base = top;
        int n = 5;
        float[] xs = new float[n * 2 + 2];
        float[] ys = new float[n * 2 + 2];
        xs[0] = cx - 6;
        ys[0] = base;
        for (int i = 0; i < n; i++) {
            float x = cx - 6 + 12.0F * (i + 0.5F) / n;
            float tall = (i == n / 2 ? 8 : i % 2 == 0 ? 5 : 6) + (float) (tap * 2);
            xs[1 + i * 2] = x;
            ys[1 + i * 2] = base - tall;
            xs[2 + i * 2] = cx - 6 + 12.0F * (i + 1) / n;
            ys[2 + i * 2] = base - 1.5F;
        }
        xs[n * 2 + 1] = cx + 6;
        ys[n * 2 + 1] = base;
        c.shape(xs, ys, 0xFF000000);
        // the gold inside it, and the heart of it white-hot when a tap lands
        float[] gx = xs.clone();
        float[] gy = ys.clone();
        for (int i = 0; i < gx.length; i++) {
            gx[i] = cx + (gx[i] - cx) * 0.75F;
            gy[i] = base - 1 + (gy[i] - base) * 0.75F;
        }
        c.shape(gx, gy, mix(GOLD, WHITE, 0.6 * tap));
        c.shape(new float[] {cx - 1.5F, cx, cx + 1.5F, cx}, new float[] {base - 2.5F, base - 5, base - 2.5F, base}, mix(0xFF1A0A0A, WHITE, 0.3 + 0.7 * tap));
    }

    /** Big words with a black outline round them. */
    static void outlined(UiCanvas c, String text, int cx, int y, int argb, float factor) {
        int ink = argb & 0xFF000000 | 0x07050A;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    c.bigCentered(text, cx + dx, y + dy, ink, false, factor);
                }
            }
        }
        c.bigCentered(text, cx, y, argb, false, factor);
    }

    static double ease(double t) {
        return 1.0 - Math.pow(1.0 - t, 3.0);
    }

    static double clamp(double x, double lo, double hi) {
        return Math.max(lo, Math.min(hi, x));
    }

    static int withAlpha(int argb, int alpha) {
        int a = (int) Math.round(((argb >>> 24) & 255) * clamp(alpha, 0, 255) / 255.0);
        return a << 24 | argb & 0xFFFFFF;
    }

    static int scale(int argb, double k) {
        int r = (int) Math.min(255, ((argb >> 16) & 255) * k);
        int g = (int) Math.min(255, ((argb >> 8) & 255) * k);
        int b = (int) Math.min(255, (argb & 255) * k);
        return argb & 0xFF000000 | r << 16 | g << 8 | b;
    }

    static int mix(int a, int b, double t) {
        t = clamp(t, 0.0, 1.0);
        int ar = (a >> 16) & 255;
        int ag = (a >> 8) & 255;
        int ab = a & 255;
        int br = (b >> 16) & 255;
        int bg = (b >> 8) & 255;
        int bb = b & 255;
        int aa = (a >>> 24) & 255;
        int ba = (b >>> 24) & 255;
        return (int) (aa + (ba - aa) * t) << 24 | (int) (ar + (br - ar) * t) << 16 | (int) (ag + (bg - ag) * t) << 8 | (int) (ab + (bb - ab) * t);
    }
}
