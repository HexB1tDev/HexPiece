package com.hexhaki.piece.npc;

/**
 * 2.3.2: when a pirate or marine blinks (pure, tested), as HexGhoul's NPCs do (its EtoRules.blink: Eto, Touka, Ken, the villagers): a
 * short shut once every {@link #EVERY} ticks or so, now and then twice, each on its own clock ({@code seed}), so no two blink together.
 */
public final class Blinks {
    /** About this often (ticks), for this long. */
    public static final int EVERY = 80;
    public static final int TICKS = 3;

    private Blinks() {
    }

    /** True while the eyes are shut, {@code ticks} into its life. */
    public static boolean shut(int seed, double ticks) {
        long cycle = (long) Math.floor(ticks / EVERY);
        double into = ticks - cycle * EVERY;
        int hash = (int) ((cycle * 0x9E3779B1L + seed * 0x85EBCA6BL) >>> 7);
        int at = Math.floorMod(hash, EVERY - 2 * TICKS - 4);
        boolean twice = Math.floorMod(hash >>> 11, 5) == 0;
        return into >= at && into < at + TICKS || twice && into >= at + TICKS + 2 && into < at + 2 * TICKS + 2;
    }
}
