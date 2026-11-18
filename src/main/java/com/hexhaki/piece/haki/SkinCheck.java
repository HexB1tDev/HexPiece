package com.hexhaki.piece.haki;

import java.util.function.IntBinaryOperator;

/**
 * 2.5.3 (the owner: "sometimes the player's skin vanishes when starting a world"): whether a skin read back from its texture is a real
 * skin (pure, tested in {@code RyouTests}). Armament's black hands ({@code client/haki/HakiSkin}) and Gear 5's white skin (the
 * original's {@code GearJacketSkin}) copy the player's skin from its texture; at world start (the skin not uploaded yet, or the owner's
 * OpenGL ES layer, MobileGlues, which has to emulate reading a texture back) that copy can come back empty, and an empty copy drew the
 * player invisible until the Haki went off. A real skin's base layer is opaque: the game makes it so for every downloaded skin
 * ({@code HttpTexture.processLegacySkin} clears the alpha of the head, the body, the arms and the legs) and the default skins are. So
 * the head's front face and the body's front must be (nearly) all opaque.
 */
public final class SkinCheck {
    /** At least this share of the head's front and of the body's front opaque. */
    static final float OPAQUE = 0.75F;

    private SkinCheck() {
    }

    /** True when {@code pixel} (x, y to a pixel, alpha the top byte) over a {@code width} x {@code height} skin is a real skin. */
    public static boolean real(IntBinaryOperator pixel, int width, int height) {
        int scale = width / 64;
        if (scale <= 0 || height < 32 * scale) {
            return false;
        }
        // the head's front (8..16, 8..16) and the body's front (20..28, 20..32), on a 64-wide skin
        return opaque(pixel, 8 * scale, 8 * scale, 8 * scale, 8 * scale) >= OPAQUE
            && opaque(pixel, 20 * scale, 20 * scale, 8 * scale, 12 * scale) >= OPAQUE;
    }

    /** The share of a w x h block at (x, y) whose alpha is at least half. */
    static float opaque(IntBinaryOperator pixel, int x, int y, int w, int h) {
        int solid = 0;
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                if (pixel.applyAsInt(x + i, y + j) >>> 24 >= 128) {
                    solid++;
                }
            }
        }
        return solid / (float) (w * h);
    }

    /** True when two skins of the same size hold the same pixels (every pixel of every other row and column compared). */
    public static boolean same(IntBinaryOperator a, IntBinaryOperator b, int width, int height) {
        for (int y = 0; y < height; y += 2) {
            for (int x = (y / 2) & 1; x < width; x += 2) {
                if (a.applyAsInt(x, y) != b.applyAsInt(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}
