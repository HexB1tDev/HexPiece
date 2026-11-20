package com.hexhaki.piece.dragon;

/**
 * 2.7.1 (the owner: "Dragons can come in different colors, other than blue. (always random)"; "make sure the cape also changes colors,
 * the fur as well (as long as it matches and blends well to look beautiful with the scales color)"): the dragon's colours, as whole
 * palettes recolouring the owner's own texture pixel by pixel (every scale, shade and highlight kept): its scales (the blues), its
 * cloud cape and fur (the purples) and its belly and horns (the golds of the upper half) each turned to the palette's hue, saturation
 * and brightness; the black mane, the eyes, the teeth and claws and the fire as the owner made them. Each palette is matched (the cape
 * a near hue to the scales, the belly a warm accent), so they blend. Pure: the game recolours its texture with it once a palette
 * (client/dragon/DragonTextures), tools/preview/DragonPreview the same.
 */
public final class DragonColours {
    /** The owner's texture's own hues for the three parts (degrees). */
    static final float SCALES = 198.0F;
    static final float FUR = 268.0F;
    static final float BELLY = 45.0F;

    /** One part's new look: its hue (degrees), saturation and brightness multipliers. */
    public record Tone(float hue, float saturation, float value) {
    }

    /** A dragon's colours. */
    public record Palette(String name, Tone scales, Tone fur, Tone belly) {
    }

    /** The palettes; the first is the owner's own blue (left as it is). */
    public static final Palette[] PALETTES = {
        new Palette("Azure", new Tone(SCALES, 1.0F, 1.0F), new Tone(FUR, 1.0F, 1.0F), new Tone(BELLY, 1.0F, 1.0F)),
        new Palette("Crimson", new Tone(354.0F, 1.05F, 0.88F), new Tone(326.0F, 0.85F, 0.72F), new Tone(42.0F, 1.0F, 1.0F)),
        new Palette("Emerald", new Tone(142.0F, 0.95F, 0.85F), new Tone(178.0F, 0.85F, 0.90F), new Tone(48.0F, 0.95F, 1.0F)),
        new Palette("Gold", new Tone(44.0F, 0.95F, 1.0F), new Tone(22.0F, 1.0F, 0.92F), new Tone(56.0F, 0.45F, 1.12F)),
        new Palette("Violet", new Tone(272.0F, 0.95F, 0.92F), new Tone(318.0F, 0.85F, 0.95F), new Tone(45.0F, 1.0F, 1.0F)),
        new Palette("Obsidian", new Tone(222.0F, 0.22F, 0.52F), new Tone(352.0F, 0.95F, 0.78F), new Tone(30.0F, 0.55F, 0.85F)),
        new Palette("Pearl", new Tone(205.0F, 0.18F, 1.12F), new Tone(214.0F, 0.45F, 1.10F), new Tone(45.0F, 0.55F, 1.08F)),
        new Palette("Inferno", new Tone(18.0F, 1.05F, 0.95F), new Tone(350.0F, 0.95F, 0.80F), new Tone(50.0F, 1.0F, 1.05F)),
        new Palette("Jade", new Tone(168.0F, 0.90F, 0.82F), new Tone(232.0F, 0.80F, 0.85F), new Tone(45.0F, 1.0F, 1.0F)),
        new Palette("Rose", new Tone(332.0F, 0.80F, 0.98F), new Tone(270.0F, 0.75F, 0.92F), new Tone(40.0F, 0.9F, 1.05F)),
    };

    private DragonColours() {
    }

    /** The palette a dragon's colour number names. */
    public static Palette palette(int colour) {
        return PALETTES[Math.floorMod(colour, PALETTES.length)];
    }

    /**
     * Recolours {@code argb} (the owner's texture, row by row, {@code width} x {@code height}) in place to {@code palette}: each pixel
     * as much of each part as its hue and saturation say (soft bands, so no edge shows), the golds only in the texture's upper half (the
     * fire's oranges are in the lower).
     */
    public static void recolour(int[] argb, int width, int height, Palette palette) {
        if (palette == PALETTES[0]) {
            return;
        }
        float[] hsv = new float[3];
        float[] out = new float[3];
        for (int y = 0; y < height; y++) {
            boolean upper = y < height / 2;
            for (int x = 0; x < width; x++) {
                int i = y * width + x;
                int c = argb[i];
                float r = (c >> 16 & 255) / 255.0F;
                float g = (c >> 8 & 255) / 255.0F;
                float b = (c & 255) / 255.0F;
                toHsv(r, g, b, hsv);
                // how much colour the pixel carries at all: the black mane's near-blacks and the greys keep theirs
                float chroma = ramp(hsv[1] * hsv[2], 0.03F, 0.08F);
                float ws = band(hsv[0], SCALES, 34.0F, 22.0F) * ramp(hsv[1], 0.10F, 0.24F) * chroma;
                float wf = band(hsv[0], FUR, 30.0F, 22.0F) * ramp(hsv[1], 0.08F, 0.20F) * chroma;
                float wb = upper ? band(hsv[0], BELLY, 15.0F, 12.0F) * ramp(hsv[1], 0.28F, 0.42F) * chroma : 0.0F;
                if (ws + wf + wb <= 0.0F) {
                    continue;
                }
                float nr = r * (1.0F - ws - wf - wb);
                float ng = g * (1.0F - ws - wf - wb);
                float nb = b * (1.0F - ws - wf - wb);
                if (ws > 0.0F) {
                    tone(hsv, SCALES, palette.scales, out);
                    nr += out[0] * ws;
                    ng += out[1] * ws;
                    nb += out[2] * ws;
                }
                if (wf > 0.0F) {
                    tone(hsv, FUR, palette.fur, out);
                    nr += out[0] * wf;
                    ng += out[1] * wf;
                    nb += out[2] * wf;
                }
                if (wb > 0.0F) {
                    tone(hsv, BELLY, palette.belly, out);
                    nr += out[0] * wb;
                    ng += out[1] * wb;
                    nb += out[2] * wb;
                }
                argb[i] = c & 0xFF000000 | channel(nr) << 16 | channel(ng) << 8 | channel(nb);
            }
        }
    }

    /** A pixel of a part (its own hue {@code from}) turned to {@code tone}: the hue moved as the part's is, saturation and value scaled. */
    static void tone(float[] hsv, float from, Tone tone, float[] rgb) {
        float h = hsv[0] + (tone.hue() - from);
        float s = Math.min(1.0F, hsv[1] * tone.saturation());
        float v = Math.min(1.0F, hsv[2] * tone.value());
        toRgb(h, s, v, rgb);
    }

    static int channel(float x) {
        return Math.max(0, Math.min(255, Math.round(x * 255.0F)));
    }

    /** 1 within {@code full} degrees of {@code centre}, falling to 0 over {@code fade} more (round the hue circle). */
    static float band(float hue, float centre, float full, float fade) {
        float d = Math.abs(((hue - centre) % 360.0F + 540.0F) % 360.0F - 180.0F);
        return d <= full ? 1.0F : d >= full + fade ? 0.0F : 1.0F - (d - full) / fade;
    }

    static float ramp(float x, float from, float to) {
        return x <= from ? 0.0F : x >= to ? 1.0F : (x - from) / (to - from);
    }

    static void toHsv(float r, float g, float b, float[] out) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;
        float h;
        if (d < 1e-6F) {
            h = 0.0F;
        } else if (max == r) {
            h = 60.0F * (((g - b) / d) % 6.0F);
        } else if (max == g) {
            h = 60.0F * ((b - r) / d + 2.0F);
        } else {
            h = 60.0F * ((r - g) / d + 4.0F);
        }
        out[0] = (h + 360.0F) % 360.0F;
        out[1] = max <= 0.0F ? 0.0F : d / max;
        out[2] = max;
    }

    static void toRgb(float h, float s, float v, float[] out) {
        float hh = ((h % 360.0F) + 360.0F) % 360.0F / 60.0F;
        float c = v * s;
        float x = c * (1.0F - Math.abs(hh % 2.0F - 1.0F));
        float m = v - c;
        float r;
        float g;
        float b;
        switch ((int) hh) {
            case 0 -> { r = c; g = x; b = 0; }
            case 1 -> { r = x; g = c; b = 0; }
            case 2 -> { r = 0; g = c; b = x; }
            case 3 -> { r = 0; g = x; b = c; }
            case 4 -> { r = x; g = 0; b = c; }
            default -> { r = c; g = 0; b = x; }
        }
        out[0] = r + m;
        out[1] = g + m;
        out[2] = b + m;
    }
}
