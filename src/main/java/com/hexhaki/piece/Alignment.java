package com.hexhaki.piece;

/**
 * 2.4.6: a player's alignment, good or evil (pure, tested in {@code MenuTests}). The owner: "New Good and Evil Alignment bar, killing
 * (coming soon humans) will result in the player being evil, when good or evil the full conqueror release gets a custom sound, for early
 * testing, create the bar of good/evil in the mastery menu, and a command to set your alignment, 100 is pure good, 0 is neutal, -100 pure
 * evil."
 * <p>
 * A number from {@link #MIN} (pure evil) through 0 (neutral) to {@link #MAX} (pure good), kept with the player's HexPiece profile
 * ({@code server/ProfileServer}), shown as a bar on the Captain's Log's overview ({@code ui/LogbookView}) and set by
 * {@code /hexpiece alignment <value>}. A player at or below {@link #EVIL} is evil, at or above {@link #GOOD} good, neutral between.
 * Evil's full-charge Conqueror's Haki has the owner's own sounds ({@code server/AlignmentSounds}): the voice line at full charge and the
 * roar when it is let out, a woman's or a man's by the player's gender. Good's sounds are to come; killing humans (to come) will move a
 * player towards evil.
 */
public final class Alignment {
    public static final int MIN = -100;
    public static final int MAX = 100;
    /** At or below: evil. */
    public static final int EVIL = -25;
    /** At or above: good. */
    public static final int GOOD = 25;

    private Alignment() {
    }

    public static int clamp(int value) {
        return Math.max(MIN, Math.min(MAX, value));
    }

    public static boolean evil(int value) {
        return value <= EVIL;
    }

    public static boolean good(int value) {
        return value >= GOOD;
    }

    /** What the log calls it: Pure Evil, Evil, Neutral, Good, Pure Good. */
    public static String label(int value) {
        int v = clamp(value);
        if (v == MIN) {
            return "Pure Evil";
        }
        if (v == MAX) {
            return "Pure Good";
        }
        return evil(v) ? "Evil" : good(v) ? "Good" : "Neutral";
    }

    /** Where {@code value} sits along the bar, 0 (pure evil, left) to 1 (pure good, right). */
    public static float along(int value) {
        return (clamp(value) - MIN) / (float) (MAX - MIN);
    }

    /** The evil full-charge voice line ("Sawagashii") for a player of {@code gender}: a woman's, or a man's (unset: a man's). */
    public static String evilVoice(Gender gender) {
        return "piece.conqueror.evil_voice_" + (gender.female() ? "female" : "male");
    }

    /** The evil full-charge release's roar for a player of {@code gender}. */
    public static String evilRelease(Gender gender) {
        return "piece.conqueror.evil_release_" + (gender.female() ? "female" : "male");
    }

    /** Every sound event an alignment plays (registered by {@code PieceSounds}). */
    public static final String[] SOUNDS = {"piece.conqueror.evil_voice_male", "piece.conqueror.evil_voice_female",
        "piece.conqueror.evil_release_male", "piece.conqueror.evil_release_female"};
}
