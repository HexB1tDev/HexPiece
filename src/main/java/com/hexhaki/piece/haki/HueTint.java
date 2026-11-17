package com.hexhaki.piece.haki;

/**
 * 2.3.3: a colour turned to another's hue (pure, tested in {@code RyouTests}). The owner: "The unleashing of the conqueror still has
 * red particles, even if my Rio is fucking like blue-ish. For some reason, I'm still getting the fucking default particles": the
 * original's Conqueror particles are made red; each keeps its own brightness, its own share of colour and its alpha, and takes the
 * player's Ryou hue (a dark red ember becomes a dark blue one, a white spark stays white, black stays black).
 */
public final class HueTint {
    private HueTint() {
    }

    /**
     * {@code argb} with the hue of {@code to} (ARGB), its value and alpha kept; its saturation kept too, unless {@code to} is a pale
     * colour (under half saturated: then the particle is as much paler).
     */
    public static int tint(int argb, int to) {
        int a = argb >>> 24;
        float[] src = hsv(argb);
        float[] dst = hsv(to);
        int rgb = rgb(dst[0], src[1] * Math.min(1.0F, dst[1] * 2.0F), src[2]);
        return (a << 24) | rgb;
    }

    /** (hue 0..1, saturation 0..1, value 0..1) of the colour's red, green and blue. */
    static float[] hsv(int argb) {
        float r = ((argb >> 16) & 255) / 255.0F;
        float g = ((argb >> 8) & 255) / 255.0F;
        float b = (argb & 255) / 255.0F;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;
        float h = 0.0F;
        if (d > 1.0E-6F) {
            if (max == r) {
                h = ((g - b) / d) / 6.0F;
            } else if (max == g) {
                h = ((b - r) / d + 2.0F) / 6.0F;
            } else {
                h = ((r - g) / d + 4.0F) / 6.0F;
            }
            if (h < 0.0F) {
                h += 1.0F;
            }
        }
        return new float[] {h, max <= 0.0F ? 0.0F : d / max, max};
    }

    static int rgb(float h, float s, float v) {
        float f = (h - (float) Math.floor(h)) * 6.0F;
        int sector = (int) f % 6;
        float frac = f - (int) f;
        float p = v * (1.0F - s);
        float q = v * (1.0F - s * frac);
        float t = v * (1.0F - s * (1.0F - frac));
        float r;
        float g;
        float b;
        switch (sector) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return (Math.round(r * 255.0F) << 16) | (Math.round(g * 255.0F) << 8) | Math.round(b * 255.0F);
    }
}
