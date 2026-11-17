package com.hexhaki.piece.haki;

/**
 * 2.3.4: which pixels of a player's skin are the hand that Armament blackens, and how dark (pure, tested in {@code RyouTests}). The
 * owner: "it's not the actual skin blackening, look at Nika, how the skin turns white, why not use that for haki?": as the original
 * turns the skin white for Nika (its Gear 5 skin, recoloured pixel by pixel), Armament turns the hand's own pixels black, the arm's
 * base layer and its sleeve layer, so the model itself is drawn with black hands (client/haki/HakiSkin).
 *
 * <p>The hand is the lowest part of each arm (2.1.9, the owner: "The whole arm is black instead of just the hands"): on the arm's 12
 * rows of sides, the last three black, fading up over the three above (the wrist); the fist's end all black. The arm boxes lie where
 * the player model takes them on a 64-wide skin (scaled for a larger one): the right arm at (40, 16), the left at (32, 48), their
 * sleeves at (40, 32) and (48, 48); a box's sides run 2 x (depth + width) across from {@code u}, 12 rows down from {@code v} + depth,
 * its bottom face (the fist's end) width x depth at ({@code u} + depth + width, {@code v}); slim arms are 3 wide, wide ones 4. An old
 * 64 x 32 skin has the right arm only (the left is drawn from it).
 */
public final class HandCoat {
    /** The arm boxes' corners (u, v) on a 64-wide skin: the right arm, the left, the right sleeve, the left sleeve. */
    static final int[][] ARMS = {{40, 16}, {32, 48}, {40, 32}, {48, 48}};
    /** How black each of the arm's 12 rows of sides is, from the shoulder down. */
    static final float[] ROWS = {0, 0, 0, 0, 0, 0, 0.15F, 0.45F, 0.8F, 1, 1, 1};
    static final int DEPTH = 4;
    static final int HEIGHT = 12;

    private HandCoat() {
    }

    /** How black pixel ({@code x}, {@code y}) of a {@code width} x {@code height} skin is (0 untouched, 1 black). */
    public static float amount(int x, int y, int width, int height, boolean slim) {
        int scale = width / 64;
        if (scale <= 0 || x < 0 || y < 0 || x >= width || y >= height) {
            return 0.0F;
        }
        int px = x / scale;
        int py = y / scale;
        int arms = height * 2 == width ? 1 : ARMS.length;
        int w = slim ? 3 : 4;
        float best = 0.0F;
        for (int i = 0; i < arms; i++) {
            int u = ARMS[i][0];
            int v = ARMS[i][1];
            if (px >= u && px < u + 2 * DEPTH + 2 * w && py >= v + DEPTH && py < v + DEPTH + HEIGHT) {
                best = Math.max(best, ROWS[py - v - DEPTH]);
            }
            if (px >= u + DEPTH + w && px < u + DEPTH + 2 * w && py >= v && py < v + DEPTH) {
                best = 1.0F;
            }
        }
        return best;
    }

    /**
     * {@code pixel} (four 8-bit channels, alpha the top byte; the colour channels in either order) {@code amount} of the way to black,
     * a tenth of its own shading kept so the skin's folds still read; its alpha untouched (a sleeve's empty pixels stay empty).
     */
    public static int coat(int pixel, float amount) {
        float a = Math.max(0.0F, Math.min(1.0F, amount));
        int out = pixel & 0xFF000000;
        for (int shift = 0; shift < 24; shift += 8) {
            int c = (pixel >> shift) & 255;
            float dark = c * 0.1F + 6.0F;
            out |= Math.round(c + (dark - c) * a) << shift;
        }
        return out;
    }
}
