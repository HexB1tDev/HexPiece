package com.hexhaki.piece.haki;

/**
 * 2.6.0: how loud the Conqueror's Haki sounds (pure, tested in {@code LightningTests}). The owner, testing 2.5.9: "Conqueror sounds
 * reduced by 35% but the line evil voice line increased by a ton, its barely audible, after the key is let go, add a proper explosion
 * sound (FROM THE WEB)".
 * <ul>
 *   <li>The original's Conqueror sounds (its {@code conqueror_*} events: the charge, the release, the full release's blast, the
 *       awakening's snap and rumble), played through its {@code delayedSound}, at {@link #QUIETER} of their loudness
 *       ({@code server/ConquerorSounds.pos}); the evil roar too (its sounds.json volume, 0.65 of 2.4.8's, its reach kept).</li>
 *   <li>The evil voice line is not lowered: remade far louder and only the voice (tools/evil_conqueror_sounds.py).</li>
 *   <li>The explosion ({@link #EXPLOSION}: 2.9.4 tools/conqueror_sounds.py, CC0 web recordings) the moment G is let go: a full charge's at
 *       full loudness, a partial one's by its charge, a tap's small ({@link #loudness}, {@link #pitch}).</li>
 * </ul>
 */
public final class ConquerorMix {
    /** The original's Conqueror sounds: 35 % quieter. */
    public static final float QUIETER = 0.65F;
    /**
     * The release's boom, by the releaser's path. 2.9.4 (the owner: "Give Conqueror's Haiku release a new proper sound, like even on like
     * all paths, good, bad, normal ... Go search around the web"): each a CC0 recording from Freesound.org (tools/conqueror_sounds.py,
     * sounds/piece/conqueror/web): a neutral Conqueror's a deep bass boom, a good one's a dramatic trailer boom, an evil one's a dark
     * trailer impact. The original's charge, release and full-release events play the same tool's thunder, charge and blast now.
     */
    public static final String EXPLOSION = "piece.conqueror.explosion";
    public static final String EXPLOSION_GOOD = "piece.conqueror.explosion_good";
    public static final String EXPLOSION_EVIL = "piece.conqueror.explosion_evil";

    /** The boom for an alignment (Alignment's: at or under -25 evil, at or over 25 good). */
    public static String explosion(int alignment) {
        return alignment <= -25 ? EXPLOSION_EVIL : alignment >= 25 ? EXPLOSION_GOOD : EXPLOSION;
    }
    /**
     * The explosion's sounds.json volume is 1 / SCALE: it is sent at SCALE times its loudness, so the server sends it as far as a sound
     * that loud (16 blocks times the volume sent: 64 for a full release) while the game plays it at its loudness (a volume over 1 is
     * only reach, never louder).
     */
    public static final float SCALE = 4.0F;
    /** A tap's explosion, and a partial charge's from its least to its most (a full charge: 1). */
    public static final float TAP = 0.4F;
    public static final float PARTIAL_LEAST = 0.5F;
    public static final float PARTIAL_MOST = 0.85F;

    private ConquerorMix() {
    }

    /** How loud the explosion of a release of {@code power} (0..1) is (0..1). */
    public static float loudness(float power, boolean tap) {
        if (tap) {
            return TAP;
        }
        if (power >= 0.999F) {
            return 1.0F;
        }
        float p = Math.max(0.0F, Math.min(1.0F, power));
        return PARTIAL_LEAST + (PARTIAL_MOST - PARTIAL_LEAST) * p;
    }

    /** The volume the explosion is sent at (its loudness times {@link #SCALE}). */
    public static float volume(float power, boolean tap) {
        return loudness(power, tap) * SCALE;
    }

    /** Its pitch: a full charge's the deepest, a tap's the highest (smaller sounds higher). */
    public static float pitch(float power, boolean tap) {
        if (tap) {
            return 1.15F;
        }
        float p = Math.max(0.0F, Math.min(1.0F, power));
        return 1.06F - 0.14F * p;
    }
}
