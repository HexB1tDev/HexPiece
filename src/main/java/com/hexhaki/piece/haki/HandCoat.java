package com.hexhaki.piece.haki;

/**
 * 2.3.4: which pixels of a player's skin are the hand that Armament blackens, and how dark (pure, tested in {@code RyouTests}). The
 * owner: "it's not the actual skin blackening, look at Nika, how the skin turns white, why not use that for haki?": as the original
 * turns the skin white for Nika (its Gear 5 skin, recoloured pixel by pixel), Armament turns the hand's own pixels black, the arm's
 * base layer and its sleeve layer, so the model itself is drawn with black hands (client/haki/HakiSkin).
 * 2.4.7: it spreads pixel by pixel as the Haki comes on, the fist first ({@link #reveal}, {@link #at}), and draws back as it goes off.
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
    /** How black each of the arm's 12 rows of sides was, from the shoulder down (until 3.6.0: the hand and the wrist only). */
    static final float[] ROWS = {0, 0, 0, 0, 0, 0, 0.15F, 0.45F, 0.8F, 1, 1, 1};
    /**
     * 3.6.0 (the owner: "Haki coating on the arm should be covering even higher up in the arm unevenly", with a picture of both forearms
     * black past the elbow, the black's edge ragged): the coat reaches past the elbow (row 6), its edge a different row in every column
     * round the arm (between {@link #EDGE_HIGH} and {@link #EDGE_LOW}), a pixel of fade above it and now and then a tongue of black
     * reaching one row higher.
     */
    static final int EDGE_HIGH = 2;
    static final int EDGE_LOW = 5;

    /** 3.6.0: how black row {@code row} of column {@code column} round the arm is (the ragged edge: the same on every skin). */
    static float coat(int column, int row) {
        int h = column * 0x9E3779B1 ^ 0x5BD1E995;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        h ^= h >>> 12;
        int n = column + 1;
        int g = n * 0x27D4EB2D;
        g ^= g >>> 15;
        // the edge wanders round the arm: a column's edge near its neighbour's, now and then a step
        double wave = 0.5 + 0.35 * Math.sin(column * 1.9 + 0.7) + 0.15 * ((h & 255) / 255.0 - 0.5) * 2.0;
        int edge = EDGE_HIGH + (int) Math.round(Math.max(0.0, Math.min(1.0, wave)) * (EDGE_LOW - EDGE_HIGH));
        if (row > edge) {
            return 1.0F;
        }
        if (row == edge) {
            return 0.7F;
        }
        if (row == edge - 1 && (g & 3) == 0) {
            return 0.45F; // a tongue of black above the edge
        }
        return 0.0F;
    }
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
                best = Math.max(best, coat(px - u, py - v - DEPTH));
            }
            if (px >= u + DEPTH + w && px < u + DEPTH + 2 * w && py >= v && py < v + DEPTH) {
                best = 1.0F;
            }
        }
        return best;
    }

    /** 2.4.7: how long a pixel takes to turn, in progress (0..1 over the whole coat). */
    public static final float SPREAD = 0.18F;

    /**
     * 2.4.7 (the owner: "just turn the player's hands black the skin itself like the Nika fruit changes the skin (slowly pixel by pixel,
     * exactly how the skin turns white)"): the progress (0..1) at which pixel ({@code x}, {@code y}) starts turning black, as the original
     * turns Nika's skin white ({@code GearJacketPattern}: each pixel its own moment, by its row, a little out of step with its
     * neighbours): the fist's end first, then up the hand from the knuckles to the wrist, every pixel jittered by its own hash. Past the
     * coat (where {@link #amount} is 0) it is 1.
     */
    public static float reveal(int x, int y, int width, int height, boolean slim) {
        int scale = width / 64;
        if (amount(x, y, width, height, slim) <= 0.0F || scale <= 0) {
            return 1.0F;
        }
        int px = x / scale;
        int py = y / scale;
        int arms = height * 2 == width ? 1 : ARMS.length;
        int w = slim ? 3 : 4;
        float row = 0.0F;
        for (int i = 0; i < arms; i++) {
            int u = ARMS[i][0];
            int v = ARMS[i][1];
            if (px >= u && px < u + 2 * DEPTH + 2 * w && py >= v + DEPTH && py < v + DEPTH + HEIGHT) {
                // the sides: the lowest row (11) first, up past the elbow (3.6.0: to its ragged edge, row 2 at the highest)
                row = Math.max(row, (HEIGHT - 1 - (py - v - DEPTH)) / 9.0F);
            }
        }
        int h = (x * 73856093) ^ (y * 19349663);
        h ^= h >>> 13;
        h *= 0x5BD1E995;
        h ^= h >>> 15;
        float jitter = (h & 0xFFFF) / 65535.0F;
        return 0.04F + Math.min(1.0F, row) * 0.58F + jitter * 0.14F;
    }

    /** 2.4.7: how black pixel ({@code x}, {@code y}) is at {@code progress} (0 untouched, 1 its full {@link #amount}). */
    public static float at(int x, int y, int width, int height, boolean slim, float progress) {
        float amount = amount(x, y, width, height, slim);
        if (amount <= 0.0F) {
            return 0.0F;
        }
        float t = Math.max(0.0F, Math.min(1.0F, (progress - reveal(x, y, width, height, slim)) / SPREAD));
        return amount * t * t * (3.0F - 2.0F * t);
    }

    /** 2.5.3: the gloss's colour (near white, a little cold) and how far toward it each highlight goes: strong, mid, soft. */
    public static final int GLOSS = 0xDCE2EC;
    static final float STRONG = 0.86F;
    static final float MID = 0.55F;
    static final float SOFT = 0.3F;

    /**
     * 2.5.3 (the owner's target picture: the coated fist is glossy jet black, a white highlight on each knuckle and along the top of the
     * fingers): how much of the gloss pixel ({@code x}, {@code y}) carries (0 none). A few pixels per arm and sleeve: on the fist's end
     * (the knuckles: two strong, two mid, along its front edge, which is the top of a punching fist), a short streak on the arm's front
     * face just above the end (the top of the fist), a soft one on the outer side. Scaled for a larger skin (every pixel of a block).
     */
    public static float shine(int x, int y, int width, int height, boolean slim) {
        int scale = width / 64;
        if (scale <= 0 || amount(x, y, width, height, slim) <= 0.0F) {
            return 0.0F;
        }
        int px = x / scale;
        int py = y / scale;
        int arms = height * 2 == width ? 1 : ARMS.length;
        int w = slim ? 3 : 4;
        for (int i = 0; i < arms; i++) {
            int u = ARMS[i][0];
            int v = ARMS[i][1];
            boolean left = i == 1 || i == 3;
            // the fist's end: columns minX..maxX, rows from the back (0) to the front (DEPTH - 1)
            int ex = px - (u + DEPTH + w);
            int ez = py - v;
            if (ex >= 0 && ex < w && ez >= 0 && ez < DEPTH) {
                // the knuckles: the front edge catches the light all along, one bright spot just behind it
                int k = left ? w - 1 - ex : ex;
                if (ez == DEPTH - 1) {
                    return k == 1 ? STRONG : MID * 0.75F;
                }
                if (ez == DEPTH - 2 && k == 2) {
                    return SOFT;
                }
                return 0.0F;
            }
            int column = px - u;
            int row = py - v - DEPTH;
            if (column < 0 || column >= 2 * DEPTH + 2 * w || row < 0 || row >= HEIGHT) {
                continue;
            }
            // the front face (the top of a punching fist): a short streak running down to the knuckles
            int front = column - DEPTH;
            if (front >= 0 && front < w) {
                int k = left ? w - 1 - front : front;
                if (row == 10 && k == 1) {
                    return STRONG * 0.8F;
                }
                if (row == 9 && k == 1 || row == 11 && k == 2) {
                    return MID * 0.7F;
                }
                return 0.0F;
            }
            // the outer side (the right arm's -x face, the left arm's +x face)
            int outer = left ? column - DEPTH - w : column;
            if (outer >= 0 && outer < DEPTH) {
                if (row == 10 && outer == 1) {
                    return MID * 0.8F;
                }
                if (row == 9 && outer == 2 || row == 11 && outer == 1) {
                    return SOFT;
                }
            }
            return 0.0F;
        }
        return 0.0F;
    }

    /**
     * 2.5.3: {@code pixel} as the coat finishes it at {@code amount} (0..1 of its full black): black ({@link #coat}), then its gloss
     * ({@link #shine}) laid on as the black sets (none until the pixel is three quarters black); its alpha untouched.
     */
    public static int finish(int pixel, int x, int y, int width, int height, boolean slim, float amount) {
        int black = coat(pixel, amount);
        float shine = shine(x, y, width, height, slim);
        float set = Math.max(0.0F, Math.min(1.0F, (amount - 0.75F) / 0.25F));
        return shine > 0.0F && set > 0.0F ? gloss(black, shine * set) : black;
    }

    /** {@code pixel} {@code t} of the way to {@link #GLOSS}, its alpha kept (channels in either order: the gloss is near grey). */
    static int gloss(int pixel, float t) {
        int out = pixel & 0xFF000000;
        for (int shift = 0; shift < 24; shift += 8) {
            int c = (pixel >> shift) & 255;
            int g = (GLOSS >> shift) & 255;
            out |= Math.round(c + (g - c) * t) << shift;
        }
        return out;
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
