package com.hexhaki.piece.ui;

/**
 * 3.2.0 (the owner: "we're going to need to have an energy bar for the fruits, appearing over the food bar with a custom good-looking bar,
 * like not just a normal bar ... It should be as creative as the one in Tokyo Ghoul mod and the Hex Kagune mod. I'm not saying copy
 * that one, absolutely not. Just make your own"): the fruit energy bar, drawn over the food bar (client/EnergyBar). Its own look: a
 * slanted bar in the eaten fruit's own colour, filling out leftward from a round devil-fruit emblem at its right end (swirled like the
 * fruits' icons, a stem and a leaf), the hunger bar's way; the fill cel-lit (a light band on top, a dark one under), slow swirl stripes
 * sliding through it, notches every fifth; what a move just cost shown in white a moment before it drains away; low, it blinks; a move
 * refused for want of energy flashes the frame red and shakes it. Pure: GUI pixels into a {@link UiCanvas}, previewed off-game by
 * tools/preview/UiPreview exactly as the game draws it.
 */
public final class EnergyBarView {
    private EnergyBarView() {
    }

    /** The bar's size (GUI pixels): as wide as the food row, the emblem's round. */
    public static final int WIDTH = 81;
    public static final int HEIGHT = 7;
    public static final int SLANT = 3;
    public static final int EMBLEM = 11;

    static int argb(int alpha, int rgb) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    static int mix(int a, int b, double t) {
        double u = Math.max(0.0, Math.min(1.0, t));
        int r = (int) Math.round(((a >> 16) & 255) * (1 - u) + ((b >> 16) & 255) * u);
        int g = (int) Math.round(((a >> 8) & 255) * (1 - u) + ((b >> 8) & 255) * u);
        int bl = (int) Math.round((a & 255) * (1 - u) + (b & 255) * u);
        return r << 16 | g << 8 | bl;
    }

    /** A slanted band from x0 to x1 (the bar's own slant), rows y0 to y1, its left edge leaning {@code SLANT} over its height. */
    static void band(UiCanvas c, float x0, float x1, float y0, float y1, float top, int argb) {
        if (x1 - x0 <= 0.01F || y1 <= y0) {
            return;
        }
        float k = SLANT / (float) HEIGHT;
        float lt = x0 + (y0 - top) * -k + SLANT;
        float lb = x0 + (y1 - top) * -k + SLANT;
        float rt = x1 + (y0 - top) * -k + SLANT;
        float rb = x1 + (y1 - top) * -k + SLANT;
        c.polygon(new float[] {lt, rt, rb, lb}, new float[] {y0, y0, y1, y1}, argb);
    }

    /**
     * The bar with its right end (the emblem's middle) at ({@code right}, {@code top}): {@code fill} 0 to 1 the energy, {@code chip} 0 to 1
     * what it was a moment ago (shown in white past the fill), {@code colour} the fruit's (RGB), {@code time} in ticks (the stripes'
     * slide, the blink), {@code denied} 0 to 1 a refused move's flash (1 just refused).
     */
    public static void draw(UiCanvas c, int right, int top, double fill, double chip, int colour, double time, double denied) {
        int shake = denied > 0.5 ? (((int) (time * 2.0)) % 2 == 0 ? 1 : -1) : 0;
        float x0 = right - WIDTH + shake;
        float x1 = right - EMBLEM / 2.0F + shake;
        float y0 = top;
        float y1 = top + HEIGHT;
        boolean low = fill < 0.25;
        boolean blink = low && ((int) (time / 6.0)) % 2 == 0;
        int frame = denied > 0.0 ? mix(0x101522, 0xE02A2A, denied) : blink ? 0x3A1218 : 0x101522;
        // the frame and the dark track inside it
        band(c, x0 - 1, x1 + 1, y0 - 1, y1 + 1, y0, argb(235, frame));
        band(c, x0, x1, y0, y1, y0, argb(220, 0x1C2333));
        band(c, x0, x1, y0, y0 + 1, y0, argb(220, 0x262E42));
        float span = x1 - x0;
        float filled = (float) (span * Math.max(0.0, Math.min(1.0, fill)));
        float chipped = (float) (span * Math.max(0.0, Math.min(1.0, chip)));
        // what a move just cost: white, draining away
        if (chipped > filled + 0.5F) {
            band(c, x1 - chipped, x1 - filled, y0, y1, y0, argb(230, 0xF4F7FF));
        }
        if (filled > 0.5F) {
            int base = blink ? mix(colour, 0xFF4040, 0.45) : colour;
            float fx = x1 - filled;
            band(c, fx, x1, y0, y1, y0, argb(255, mix(base, 0x000000, 0.12)));
            band(c, fx, x1, y0, y0 + 2, y0, argb(255, mix(base, 0xFFFFFF, 0.45)));
            band(c, fx, x1, y1 - 2, y1, y0, argb(255, mix(base, 0x000000, 0.38)));
            // swirl stripes sliding through the fill (thin leaning bands, light)
            float slide = (float) ((time * 0.35) % 9.0);
            for (float sx = fx - 9 + slide; sx < x1; sx += 9.0F) {
                float a = Math.max(fx, sx);
                float b = Math.min(x1, sx + 2.0F);
                band(c, a, b, y0 + 2, y1 - 2, y0, argb(110, 0xFFFFFF));
            }
            // its leading edge: a bright line
            band(c, fx, Math.min(x1, fx + 1.0F), y0, y1, y0, argb(230, mix(base, 0xFFFFFF, 0.7)));
        }
        // notches every fifth
        for (int k = 1; k < 5; k++) {
            float nx = x1 - span * k / 5.0F;
            band(c, nx, nx + 1.0F, y0 + 1, y1, y0, argb(140, 0x0A0D14));
        }
        emblem(c, right + shake, top + HEIGHT / 2, colour, time, frame);
    }

    /** The round devil-fruit emblem ({@link #EMBLEM} across) at ({@code cx}, {@code cy}): the fruit's colour swirled lighter, a stem and a leaf. */
    static void emblem(UiCanvas c, int cx, int cy, int colour, double time, int frame) {
        int r = EMBLEM / 2;
        for (int dy = -r - 1; dy <= r + 1; dy++) {
            for (int dx = -r - 1; dx <= r + 1; dx++) {
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d > r + 0.6) {
                    if (d <= r + 1.5) {
                        c.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, argb(235, frame));
                    }
                    continue;
                }
                // light from the upper left, in two cel steps; a swirl wound round the middle
                double lit = (-dx - dy) / (double) r;
                int col = lit > 0.35 ? mix(colour, 0xFFFFFF, 0.35) : lit < -0.45 ? mix(colour, 0x000000, 0.35) : colour;
                double a = Math.atan2(dy, dx);
                double wave = Math.sin(a - d * 1.25 + time * 0.05);
                if (wave > 0.75 && d > 1.0 && d < r - 0.5) {
                    col = mix(col, 0xFFFFFF, 0.6);
                }
                c.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, argb(255, col));
            }
        }
        // the stem and a leaf
        c.fill(cx, cy - r - 2, cx + 1, cy - r, argb(255, 0x5A4026));
        c.fill(cx + 1, cy - r - 3, cx + 4, cy - r - 1, argb(255, 0x4FA35A));
        c.fill(cx + 1, cy - r - 2, cx + 3, cy - r - 1, argb(255, 0x2F6E3A));
    }
}
