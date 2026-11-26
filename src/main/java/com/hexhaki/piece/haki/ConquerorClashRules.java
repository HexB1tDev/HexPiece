package com.hexhaki.piece.haki;

/**
 * 3.8.2: the Conqueror's Haki clash's rules, pure (the server's {@code server/ConquerorClash} and the clients follow them; tested in
 * LightningTests). The owner, verbatim: "Conqueror's Haki Clash from now on whenever 2 players release conqueror's Haki (charged G for at
 * least 2 seconds within eachother's Haki release range) they collide creating a massive pressure wave between the 2 players lightning
 * of both parties flying outwards a massive impact in the precise point of collision like the gif shows (that ring on the point of
 * impact), Then a mini game begins, Whoever has more Conqueror's Haki Mastery has way more ease in this game, a bar appears on the
 * middle of the screen "TAP M1" whoever manages to get their bar color to the enemy's side wins, the bar has to be really well made and
 * decorated. Note: During the clash wind is blowing outwards from both players like they're in 1 single massive spherical bubble
 * anything or anyone going close to this perimeter is blasted away in a ragdoll in a very violent way (go through blocks as well in a
 * horizotal tragectory), at the end whoever wins sends the other player flying also in a horizontal tragectory, ragdoll and stunned
 * for double the winner's max stun of the conqueror."
 *
 * <p>The tug: {@code v} from -1 (B's colour all the way to A: B wins) to 1 (A's to B: A wins), 0 at the start. Each of A's taps pushes it
 * {@link #TAP} times A's {@link #weight}, each of B's back by B's; and the stronger Haki presses on its own each tick ({@link #drift}).
 */
public final class ConquerorClashRules {
    /** Ticks G must have been held (2 seconds) for a release, and the other's charge, to clash. */
    public static final int HOLD_LEAST = 40;
    /** The collision: the impact, the pressure wave, the bar filling in, before the taps count. */
    public static final int INTRO = 30;
    /** The longest the taps go on (15 s): then whoever is ahead wins (even: the greater mastery, then A). */
    public static final int GAME_MOST = 300;
    /** How far a tap at weight 1 pushes the bar (of 1 from the middle to a side). */
    public static final double TAP = 0.045;
    /** The least ticks between two taps that count (no more than 20 a second, whatever clicks faster). */
    public static final int TAP_GAP = 1;
    /** The stronger Haki's own press a tick, at the weights' whole difference. */
    public static final double DRIFT = 0.004;
    /** Mastery added to both sides in the weight (a fresh Conqueror still has some say against a master). */
    public static final double FLOOR = 150.0;
    /** The bubble: half the gap between them and this much more, at least {@link #BUBBLE_LEAST}. */
    public static final double BUBBLE_MARGIN = 5.0;
    public static final double BUBBLE_LEAST = 8.0;
    /** How near the bubble's skin a body must come to be blasted (inside it, or within this of it). */
    public static final double SKIN = 2.0;
    /** A body the wind blasts: its speed (blocks a tick, level) and how long it flies through everything. */
    public static final double BLAST_SPEED = 2.6;
    public static final int BLAST_TICKS = 14;
    /** How long a blasted body lies (ticks). */
    public static final int BLAST_DOWN = 70;
    /** The loser, sent flying: speed and flight. */
    public static final double THROW_SPEED = 3.2;
    public static final int THROW_TICKS = 20;
    /** The winner's blow, after the bar reaches the side, before the loser flies. */
    public static final int FINISH = 8;

    private ConquerorClashRules() {
    }

    /** Whether a release held {@code releaseHeld} ticks meets a charge held {@code chargeHeld} ticks {@code distance} apart. */
    public static boolean clashes(long releaseHeld, long chargeHeld, double distance, double releaseRadius, double chargeRadius) {
        return releaseHeld >= HOLD_LEAST && chargeHeld >= HOLD_LEAST && distance <= Math.min(releaseRadius, chargeRadius);
    }

    /** A side's weight in the tug (about 0.3 to 1.7; both 1 at even masteries, 0..1000). */
    public static double weight(int mine, int theirs) {
        double m = clamp(mine, 0, 1000) + FLOOR;
        double t = clamp(theirs, 0, 1000) + FLOOR;
        return 2.0 * m / (m + t);
    }

    /** The stronger Haki's press a tick toward its own side (A's positive). */
    public static double drift(int masteryA, int masteryB) {
        return DRIFT * (weight(masteryA, masteryB) - weight(masteryB, masteryA));
    }

    /** The bar after a tick of the game: {@code tapsA} and {@code tapsB} counted taps, the press of the stronger; within -1..1. */
    public static double step(double v, int tapsA, int tapsB, int masteryA, int masteryB) {
        double next = v + TAP * (tapsA * weight(masteryA, masteryB) - tapsB * weight(masteryB, masteryA)) + drift(masteryA, masteryB);
        return clamp(next, -1.0, 1.0);
    }

    /** The winner when the bar stands at {@code v}: 1 A, -1 B, 0 none yet ({@code over}: time is up, someone wins). */
    public static int winner(double v, boolean over, int masteryA, int masteryB) {
        if (v >= 1.0) {
            return 1;
        }
        if (v <= -1.0) {
            return -1;
        }
        if (!over) {
            return 0;
        }
        if (v != 0.0) {
            return v > 0.0 ? 1 : -1;
        }
        return masteryB > masteryA ? -1 : 1;
    }

    /** Where the two Haki meet along A to B (0 at A, 1 at B): the middle, pushed toward the one losing. */
    public static double meeting(double v) {
        return 0.5 + 0.3 * clamp(v, -1.0, 1.0);
    }

    /** The bubble's radius round the middle of the two, {@code gap} apart. */
    public static double bubble(double gap) {
        return Math.max(BUBBLE_LEAST, gap / 2.0 + BUBBLE_MARGIN);
    }

    /** Whether a body {@code distance} from the bubble's middle is caught by its wind. */
    public static boolean caught(double distance, double radius) {
        return distance <= radius + SKIN;
    }

    /** The loser's stun (ticks): double the most a Conqueror's release of the winner's alignment fells for. */
    public static int loserStun(int winnerAlignment) {
        return 2 * ConquerorStun.most(winnerAlignment);
    }

    static double clamp(double x, double lo, double hi) {
        return Math.max(lo, Math.min(hi, x));
    }
}
