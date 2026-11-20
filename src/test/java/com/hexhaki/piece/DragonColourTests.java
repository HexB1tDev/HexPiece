package com.hexhaki.piece;

import com.hexhaki.piece.dragon.DragonColours;

/**
 * 2.7.1: the dragon's colours (dragon/DragonColours): the owner's blue untouched, every other palette turning the scales, the cape and
 * fur and the belly to its own hues while the black mane, the white teeth and the fire's oranges stay as the owner made them.
 */
public final class DragonColourTests {
    public static void main(String[] args) {
        Check.that(DragonColours.PALETTES.length >= 8, "at least eight colours");
        Check.that(DragonColours.palette(0) == DragonColours.PALETTES[0] && DragonColours.palette(-1) != null, "any number names a palette");
        int scales = 0xFF2FA6D8;  // a scale's blue
        int fur = 0xFF5B3C8C;     // the cape's purple
        int belly = 0xFFD8B04A;   // the belly's gold
        int mane = 0xFF101014;    // the black mane
        int teeth = 0xFFF0F0F0;   // white
        int fire = 0xFFF7A133;    // the fire's orange
        int[] own = {scales, fur, belly, mane, teeth, fire, scales, fur, belly, mane, teeth, fire};
        // 6 x 2: the upper row is the texture's upper half (the belly's golds count there), the lower its lower half (the fire)
        int[] same = own.clone();
        DragonColours.recolour(same, 6, 2, DragonColours.PALETTES[0]);
        Check.that(java.util.Arrays.equals(same, own), "the owner's blue is left exactly as it is");
        for (int p = 1; p < DragonColours.PALETTES.length; p++) {
            DragonColours.Palette palette = DragonColours.PALETTES[p];
            int[] px = own.clone();
            DragonColours.recolour(px, 6, 2, palette);
            Check.that(hueNear(px[0], palette.scales().hue(), 14.0F) || saturation(px[0]) < 0.3F, palette.name() + ": the scales take its hue");
            Check.that(hueNear(px[1], palette.fur().hue(), 14.0F) || saturation(px[1]) < 0.3F, palette.name() + ": the cape and fur take its hue");
            Check.that(px[3] == mane && px[4] == teeth, palette.name() + ": the black mane and the white teeth stay");
            Check.that(px[11] == fire, palette.name() + ": the fire stays");
            Check.that((px[0] >>> 24) == 0xFF && (px[1] >>> 24) == 0xFF, palette.name() + ": nothing turns see-through");
        }
        Check.done("DragonColourTests");
    }

    static float[] hsv(int c) {
        float r = (c >> 16 & 255) / 255.0F;
        float g = (c >> 8 & 255) / 255.0F;
        float b = (c & 255) / 255.0F;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;
        float h = d < 1e-6F ? 0 : max == r ? 60 * (((g - b) / d) % 6) : max == g ? 60 * ((b - r) / d + 2) : 60 * ((r - g) / d + 4);
        return new float[] {(h + 360) % 360, max <= 0 ? 0 : d / max, max};
    }

    static float saturation(int c) {
        return hsv(c)[1];
    }

    static boolean hueNear(int c, float hue, float within) {
        float d = Math.abs(((hsv(c)[0] - hue) % 360 + 540) % 360 - 180);
        return d <= within;
    }
}
