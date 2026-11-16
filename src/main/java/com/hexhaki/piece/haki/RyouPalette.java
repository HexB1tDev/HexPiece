package com.hexhaki.piece.haki;

/**
 * 2.1.3: the glow colours a player's Ryou can be given (pure). The owner: "When a player first unlocks Ryou, assign them one random,
 * clearly visible glow colour on the server and save it permanently with their player data". Every colour here is saturated and bright
 * (no greys, no darks): it has to read as a rim of light around blackened hands, in daylight and at night.
 */
public final class RyouPalette {
    /** RGB, opaque. */
    public static final int[] COLORS = {
        0xFF2B3D, // crimson
        0xFF5A1F, // vermilion
        0xFFA51F, // amber
        0xFFD93A, // gold
        0xB6FF3A, // lime
        0x34FF7C, // emerald
        0x2CF6D2, // jade
        0x2AE6FF, // cyan
        0x2E9BFF, // azure
        0x5C6BFF, // cobalt
        0x9B4BFF, // violet
        0xD94BFF, // orchid
        0xFF3AC8, // magenta
        0xFF5E8E, // rose
        0x9FE8FF, // ice
        0xFFF2B0, // pale gold
    };
    /** Shown for a Ryou effect whose owner's colour has not arrived yet (or a technique used before Ryou was unlocked): pale steel. */
    public static final int NEUTRAL = 0xDCE6F2;

    private RyouPalette() {
    }

    /** The colour for a random number (any int: its value modulo the palette). */
    public static int pick(int random) {
        return COLORS[Math.floorMod(random, COLORS.length)] | 0xFF000000;
    }

    /** True for a colour this palette gives (opaque, one of {@link #COLORS}). */
    public static boolean given(int argb) {
        for (int color : COLORS) {
            if ((color | 0xFF000000) == argb) {
                return true;
            }
        }
        return false;
    }
}
