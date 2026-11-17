package com.hexhaki.piece.ui;

/**
 * 2.1.1: HexPiece's menu kit (pure, drawn on any {@link UiCanvas}): the sea and sky, ships, the Jolly Roger and the marines' gull,
 * parchment and wood for the pirate start menu, white sailcloth and blue trim for the mastery log, rope buttons and cards.
 */
public final class PirateUi {
    // The start menu's palette: dusk over the sea, parchment and wood.
    public static final int SKY_TOP = 0xFF1A2442;
    public static final int SKY_LOW = 0xFFD9845A;
    public static final int SEA_TOP = 0xFF1C4A70;
    public static final int SEA_LOW = 0xFF0A1C30;
    public static final int CREST = 0x9AC8E4F4;
    public static final int PARCHMENT = 0xFFEBDDBA;
    public static final int PARCHMENT_DARK = 0xFFC9B07E;
    public static final int WOOD = 0xFF5B3A21;
    public static final int WOOD_DARK = 0xFF35210F;
    public static final int GOLD = 0xFFE3B341;
    public static final int INK = 0xFF35261A;
    public static final int INK_SOFT = 0xFF6A5640;
    public static final int RED = 0xFFB8322A;
    // The mastery log's palette: navy, white sailcloth, marine blue.
    public static final int NAVY = 0xFF0D2647;
    public static final int NAVY_LIGHT = 0xFF173C6B;
    public static final int SAIL = 0xFFF5F8FC;
    public static final int SAIL_SHADE = 0xFFDCE6F2;
    public static final int BLUE = 0xFF2C6BB5;
    public static final int BLUE_LIGHT = 0xFFBBD3EE;
    public static final int BLUE_INK = 0xFF10294B;

    private PirateUi() {
    }

    public static void circle(UiCanvas c, float cx, float cy, float r, int argb) {
        int n = Math.max(10, (int) (r * 2.5F));
        float[] xs = new float[n];
        float[] ys = new float[n];
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            xs[i] = cx + (float) Math.cos(a) * r;
            ys[i] = cy + (float) Math.sin(a) * r;
        }
        c.polygon(xs, ys, argb);
    }

    public static void quad(UiCanvas c, float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, int argb) {
        c.polygon(new float[] {x0, x1, x2, x3}, new float[] {y0, y1, y2, y3}, argb);
    }

    public static void triangle(UiCanvas c, float x0, float y0, float x1, float y1, float x2, float y2, int argb) {
        c.polygon(new float[] {x0, x1, x2}, new float[] {y0, y1, y2}, argb);
    }

    /** A thick line as a quad. */
    public static void line(UiCanvas c, float x0, float y0, float x1, float y1, float width, int argb) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.01F) {
            return;
        }
        float nx = -dy / length * width / 2;
        float ny = dx / length * width / 2;
        quad(c, x0 + nx, y0 + ny, x1 + nx, y1 + ny, x1 - nx, y1 - ny, x0 - nx, y0 - ny, argb);
    }

    public static void border(UiCanvas c, int x0, int y0, int x1, int y1, int argb) {
        c.fill(x0, y0, x1, y0 + 1, argb);
        c.fill(x0, y1 - 1, x1, y1, argb);
        c.fill(x0, y0, x0 + 1, y1, argb);
        c.fill(x1 - 1, y0, x1, y1, argb);
    }

    /** Dusk: the sky graded to the horizon, a low sun. */
    public static void sky(UiCanvas c, int x0, int y0, int x1, int horizon, double time) {
        int mid = y0 + (horizon - y0) * 3 / 5;
        c.gradient(x0, y0, x1, mid, SKY_TOP, 0xFF5B4A6A);
        c.gradient(x0, mid, x1, horizon, 0xFF5B4A6A, SKY_LOW);
        float sunX = x0 + (x1 - x0) * 0.72F;
        circle(c, sunX, horizon - 2, 26, 0x40FFC48A);
        circle(c, sunX, horizon - 2, 18, 0x70FFD39A);
        circle(c, sunX, horizon - 2, 12, 0xFFFFE2A8);
        for (int i = 0; i < 5; i++) {
            float cloudX = (float) ((x0 + ((i * 97 + time * (4 + i)) % (x1 - x0 + 80))) - 40);
            float cloudY = y0 + 16 + i * 11;
            c.fill((int) cloudX, (int) cloudY, (int) cloudX + 38 + i * 6, (int) cloudY + 3, 0x30FFFFFF);
            c.fill((int) cloudX + 8, (int) cloudY - 2, (int) cloudX + 26, (int) cloudY, 0x24FFFFFF);
        }
    }

    /** The sea from the horizon down, its crests rolling with {@code time} (seconds). */
    public static void sea(UiCanvas c, int x0, int horizon, int x1, int y1, double time, int top, int low, int crest) {
        c.gradient(x0, horizon, x1, y1, top, low);
        int rows = Math.max(1, (y1 - horizon) / 7);
        for (int row = 0; row < rows; row++) {
            int y = horizon + 3 + row * 7 + row * row / 6;
            if (y >= y1) {
                break;
            }
            float speed = 6 + row * 3;
            float spacing = 26 + row * 6;
            float offset = (float) ((time * speed + row * 13) % spacing);
            int length = 8 + row * 2;
            int alpha = Math.min(255, ((crest >>> 24) & 255) * (row + 3) / (rows + 3));
            for (float x = x0 - spacing + offset; x < x1; x += spacing) {
                int wobble = (int) Math.round(Math.sin(time * 1.7 + x * 0.05 + row) * 1.0);
                c.fill((int) x, y + wobble, (int) x + length, y + wobble + 1, alpha << 24 | (crest & 0xFFFFFF));
            }
        }
        c.fill(x0, horizon, x1, horizon + 1, 0x60FFE0B0);
    }

    /** A ship sailing at {@code cx} with its keel at {@code baseY}, {@code size} pixels long, rocking with {@code time}. */
    public static void ship(UiCanvas c, float cx, float baseY, float size, double time, int hull, int sail, int flagColour,
                            boolean marineFlag) {
        float s = size / 100F;
        float rock = (float) Math.sin(time * 1.3) * 1.5F * s;
        float bob = (float) Math.sin(time * 1.9) * 1.2F;
        float y = baseY + bob;
        // hull with a raised stern
        c.polygon(new float[] {cx - 50 * s, cx + 44 * s, cx + 36 * s, cx - 40 * s, cx - 50 * s},
            new float[] {y - 14 * s + rock, y - 14 * s - rock, y, y, y - 22 * s + rock}, hull);
        c.fill((int) (cx - 46 * s), (int) (y - 10 * s), (int) (cx + 40 * s), (int) (y - 9 * s), 0x50FFFFFF);
        for (int port = -3; port <= 3; port++) {
            c.fill((int) (cx + port * 10 * s - 1.5F * s), (int) (y - 7 * s), (int) (cx + port * 10 * s + 1.5F * s), (int) (y - 5 * s), 0xC0201010);
        }
        // masts and sails
        float[] masts = {-22, 6, 30};
        float[] heights = {62, 74, 48};
        for (int m = 0; m < masts.length; m++) {
            float mx = cx + masts[m] * s;
            float top = y - 14 * s - heights[m] * s;
            line(c, mx, y - 12 * s, mx, top, Math.max(1.5F, 2.2F * s), 0xFF3A2414);
            float billow = (float) Math.sin(time * 2.1 + m) * 1.2F * s;
            float w = (m == 1 ? 18 : 14) * s;
            quad(c, mx - w, top + 6 * s, mx + w, top + 6 * s, mx + w + billow + 2 * s, top + 26 * s, mx - w + billow - 2 * s, top + 26 * s, sail);
            quad(c, mx - w * 1.15F, top + 29 * s, mx + w * 1.15F, top + 29 * s, mx + w * 1.15F + billow + 3 * s, top + (heights[m] - 4) * s,
                mx - w * 1.15F + billow - 3 * s, top + (heights[m] - 4) * s, sail);
            c.fill((int) (mx - w), (int) (top + 6 * s), (int) (mx + w), (int) (top + 7 * s), 0x30000000);
        }
        // the flag on the main mast
        float fx = cx + masts[1] * s;
        float fy = y - 14 * s - heights[1] * s;
        float wave = (float) Math.sin(time * 4) * 1.5F * s;
        quad(c, fx, fy - 10 * s, fx + 14 * s, fy - 9 * s + wave, fx + 14 * s, fy - 1 * s + wave, fx, fy, flagColour);
        if (marineFlag) {
            gull(c, fx + 7 * s, fy - 5 * s + wave * 0.5F, 6 * s, BLUE);
        } else {
            circle(c, fx + 7 * s, fy - 5.5F * s + wave * 0.5F, 2.2F * s, 0xFFF0F0F0);
        }
        // its wake
        for (int i = 0; i < 4; i++) {
            float wx = cx - 52 * s - i * 9 * s - (float) ((time * 10) % (9 * s));
            c.fill((int) wx, (int) (y + 1), (int) (wx + 6 * s), (int) (y + 2), 0x80E8F4FF);
        }
    }

    /** The Jolly Roger: a black flag, a white skull over crossed bones. */
    public static void jollyRoger(UiCanvas c, int x, int y, int w, int h, double time) {
        float wave = (float) Math.sin(time * 3) * 2;
        quad(c, x, y, x + w, y + wave, x + w, y + h + wave, x, y + h, 0xFF111111);
        float cx = x + w / 2F;
        float cy = y + h / 2F + wave / 2;
        float r = Math.min(w, h) * 0.2F;
        line(c, cx - r * 2.2F, cy - r * 1.4F, cx + r * 2.2F, cy + r * 2.0F, r * 0.55F, 0xFFF2EFE6);
        line(c, cx + r * 2.2F, cy - r * 1.4F, cx - r * 2.2F, cy + r * 2.0F, r * 0.55F, 0xFFF2EFE6);
        circle(c, cx, cy - r * 0.3F, r * 1.15F, 0xFFF2EFE6);
        c.fill((int) (cx - r * 0.7F), (int) (cy + r * 0.5F), (int) (cx + r * 0.7F), (int) (cy + r * 1.2F), 0xFFF2EFE6);
        circle(c, cx - r * 0.45F, cy - r * 0.35F, r * 0.32F, 0xFF111111);
        circle(c, cx + r * 0.45F, cy - r * 0.35F, r * 0.32F, 0xFF111111);
        // the straw hat band
        c.fill((int) (cx - r * 1.6F), (int) (cy - r * 1.25F), (int) (cx + r * 1.6F), (int) (cy - r * 0.95F), 0xFFE0B040);
        c.fill((int) (cx - r * 1.0F), (int) (cy - r * 1.75F), (int) (cx + r * 1.0F), (int) (cy - r * 1.25F), 0xFFE0B040);
        c.fill((int) (cx - r * 1.0F), (int) (cy - r * 1.35F), (int) (cx + r * 1.0F), (int) (cy - r * 1.2F), RED);
    }

    /** The marines' gull (a seagull's wings in a shallow M). */
    public static void gull(UiCanvas c, float cx, float cy, float size, int argb) {
        float t = Math.max(1.0F, size * 0.18F);
        line(c, cx - size, cy - size * 0.15F, cx - size * 0.45F, cy - size * 0.45F, t, argb);
        line(c, cx - size * 0.45F, cy - size * 0.45F, cx, cy + size * 0.1F, t, argb);
        line(c, cx, cy + size * 0.1F, cx + size * 0.45F, cy - size * 0.45F, t, argb);
        line(c, cx + size * 0.45F, cy - size * 0.45F, cx + size, cy - size * 0.15F, t, argb);
    }

    /** The marines' flag: white, a blue gull, "MARINE" under it. */
    public static void marineFlag(UiCanvas c, int x, int y, int w, int h, double time) {
        float wave = (float) Math.sin(time * 3) * 2;
        quad(c, x, y, x + w, y + wave, x + w, y + h + wave, x, y + h, SAIL);
        c.fill(x, y + 2, x + w, y + 4, BLUE);
        gull(c, x + w / 2F, y + h * 0.42F + wave / 2, Math.min(w, h) * 0.3F, BLUE);
        c.centered("MARINE", x + w / 2, (int) (y + h * 0.66F + wave / 2), BLUE, false);
    }

    /** A scroll of parchment: rolled ends, a darker edge, a few stains. */
    public static void parchment(UiCanvas c, int x0, int y0, int x1, int y1) {
        c.fill(x0 + 3, y0 + 3, x1 + 3, y1 + 3, 0x50000000);
        c.fill(x0, y0, x1, y1, PARCHMENT);
        c.gradient(x0, y0, x1, y0 + 10, 0x30603818, 0x00603818);
        c.gradient(x0, y1 - 10, x1, y1, 0x00603818, 0x30603818);
        border(c, x0, y0, x1, y1, PARCHMENT_DARK);
        border(c, x0 + 1, y0 + 1, x1 - 1, y1 - 1, 0x40B08850);
        circle(c, x0 + (x1 - x0) * 0.8F, y0 + (y1 - y0) * 0.75F, 14, 0x12603818);
        circle(c, x0 + (x1 - x0) * 0.15F, y0 + (y1 - y0) * 0.3F, 9, 0x10603818);
        // rolled ends
        c.fill(x0 - 4, y0 - 3, x0 + 2, y1 + 3, 0xFFD4BF8E);
        c.fill(x1 - 2, y0 - 3, x1 + 4, y1 + 3, 0xFFD4BF8E);
        c.fill(x0 - 4, y0 - 3, x0 - 3, y1 + 3, PARCHMENT_DARK);
        c.fill(x1 + 3, y0 - 3, x1 + 4, y1 + 3, PARCHMENT_DARK);
        c.fill(x0 - 5, y0 - 5, x0 + 3, y0 - 2, WOOD);
        c.fill(x0 - 5, y1 + 2, x0 + 3, y1 + 5, WOOD);
        c.fill(x1 - 3, y0 - 5, x1 + 5, y0 - 2, WOOD);
        c.fill(x1 - 3, y1 + 2, x1 + 5, y1 + 5, WOOD);
    }

    /** A wooden board with nail heads. */
    public static void board(UiCanvas c, int x0, int y0, int x1, int y1) {
        c.fill(x0, y0, x1, y1, WOOD);
        for (int y = y0 + 6; y < y1; y += 7) {
            c.fill(x0 + 1, y, x1 - 1, y + 1, 0x40000000);
        }
        border(c, x0, y0, x1, y1, WOOD_DARK);
        for (int[] nail : new int[][] {{x0 + 3, y0 + 3}, {x1 - 5, y0 + 3}, {x0 + 3, y1 - 5}, {x1 - 5, y1 - 5}}) {
            c.fill(nail[0], nail[1], nail[0] + 2, nail[1] + 2, 0xFFB8A070);
        }
    }

    /** A rope-edged button. */
    public static void ropeButton(UiCanvas c, int x0, int y0, int x1, int y1, String label, boolean hover, boolean enabled) {
        int face = !enabled ? 0xFF6E5A46 : hover ? 0xFF8A5A30 : 0xFF6B4423;
        c.fill(x0 + 1, y0 + 2, x1 + 1, y1 + 2, 0x60000000);
        c.fill(x0, y0, x1, y1, face);
        c.gradient(x0, y0, x1, y0 + (y1 - y0) / 2, 0x30FFFFFF, 0x00FFFFFF);
        for (int x = x0; x < x1; x += 4) {
            c.fill(x, y0, Math.min(x1, x + 2), y0 + 1, 0xFFC8A46A);
            c.fill(x + 2, y1 - 1, Math.min(x1, x + 4), y1, 0xFFC8A46A);
        }
        c.fill(x0, y0, x0 + 1, y1, 0xFFC8A46A);
        c.fill(x1 - 1, y0, x1, y1, 0xFFC8A46A);
        c.centered(label, (x0 + x1) / 2, (y0 + y1) / 2 - 4, enabled ? (hover ? 0xFFFFE9B0 : 0xFFF4E2C0) : 0xFFB0A090, true);
    }

    /** A choice card on parchment: lifted when hovered, gold-edged and ticked when picked. */
    public static void card(UiCanvas c, int x0, int y0, int x1, int y1, boolean picked, boolean hover, int accent) {
        int lift = hover && !picked ? 1 : 0;
        c.fill(x0 + 2, y0 + 3, x1 + 2, y1 + 3, 0x40000000);
        c.fill(x0, y0 - lift, x1, y1 - lift, picked ? 0xFFF7EBCB : hover ? 0xFFF3E6C6 : 0xFFE6D5AE);
        c.fill(x0, y0 - lift, x1, y0 + 3 - lift, accent);
        border(c, x0, y0 - lift, x1, y1 - lift, picked ? GOLD : 0xFFB59A68);
        if (picked) {
            border(c, x0 + 1, y0 + 1 - lift, x1 - 1, y1 - 1 - lift, GOLD);
            line(c, x1 - 13, y0 + 10 - lift, x1 - 9, y0 + 14 - lift, 2, 0xFF3C8A3C);
            line(c, x1 - 9, y0 + 14 - lift, x1 - 3, y0 + 6 - lift, 2, 0xFF3C8A3C);
        }
    }

    /** A filled progress bar. */
    public static void bar(UiCanvas c, int x0, int y0, int x1, int y1, float fraction, int fill, int back) {
        c.fill(x0, y0, x1, y1, back);
        int w = Math.round((x1 - x0) * Math.max(0F, Math.min(1F, fraction)));
        if (w > 0) {
            c.fill(x0, y0, x0 + w, y1, fill);
            c.fill(x0, y0, x0 + w, y0 + 1, 0x50FFFFFF);
        }
        border(c, x0, y0, x1, y1, 0x60000000);
    }
}
