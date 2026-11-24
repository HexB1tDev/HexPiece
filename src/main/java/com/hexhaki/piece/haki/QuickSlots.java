package com.hexhaki.piece.haki;

/**
 * 3.5.9 (the owner: "Wifi haki becomes a Sword's man ability only (switch the key to a free quickslot not L)", "Galaxy Fist Removed
 * entirely.", "Haki Grip Removed entirely as well"): the Conqueror's quick abilities on the nine quick slots (pure, tested in
 * {@code HudTests}). The original numbers them 100 + n (com.hexhaki.binds.Binds.HAKI_BASE): the King's Awakening 100, the Haki Grip
 * 101, WiFi Haki 102, the Galaxy Fist (Haki Projection) 103; its slots are G K L H (0 to 3: the patch's letters), then the fruit's.
 * The Haki Grip and the Galaxy Fist are on no slot ever again; WiFi Haki is a Swordsman's alone, and never on L: on K (where the Grip
 * was) when that is free, else H, else the first free slot that is not L.
 */
public final class QuickSlots {
    public static final int AWAKENING = 100;
    public static final int GRIP = 101;
    public static final int WIFI = 102;
    public static final int GALAXY = 103;
    public static final int SLOT_K = 1;
    public static final int SLOT_L = 2;
    public static final int SLOT_H = 3;

    private QuickSlots() {
    }

    /** Was {@code ability} taken out of the game (the Haki Grip, the Galaxy Fist)? */
    public static boolean gone(int ability) {
        return ability == GRIP || ability == GALAXY;
    }

    /** {@code layout} (nine abilities, -1 for none) as it must be: the gone powers off it, WiFi Haki only for a Swordsman, never on L. */
    public static int[] clean(int[] layout, boolean swordsman) {
        int[] out = layout.clone();
        int wifi = -1;
        for (int i = 0; i < out.length; i++) {
            if (gone(out[i])) {
                out[i] = -1;
            } else if (out[i] == WIFI) {
                if (wifi < 0) {
                    wifi = i;
                }
                out[i] = -1;
            }
        }
        if (!swordsman) {
            return out;
        }
        int at = wifi >= 0 && wifi != SLOT_L ? wifi : -1;
        for (int prefer : new int[] {SLOT_K, SLOT_H}) {
            if (at < 0 && prefer < out.length && out[prefer] < 0) {
                at = prefer;
            }
        }
        for (int i = 0; at < 0 && i < out.length; i++) {
            if (i != SLOT_L && out[i] < 0) {
                at = i;
            }
        }
        if (at >= 0) {
            out[at] = WIFI;
        }
        return out;
    }
}
