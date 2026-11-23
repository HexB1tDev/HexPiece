package com.hexhaki.piece.horo;

/**
 * 3.3.6: the Horo Horo no Mi's own sound events ({@code hexhaki:piece.horo.*}, the merged sounds.json), every clip a CC0 recording from
 * Freesound.org cut by tools/horo_sounds.py (sounds/piece/horo, credited in its SOURCES.txt), broadband with no pitched line (no ghostly
 * wail or laugh: a voice or a moan is pitched, and the owner will not have melodic sounds). Pure: names only (PieceSounds registers them).
 */
public final class HoroSounds {
    private HoroSounds() {
    }

    /** A ghost sent off (a soft breathy whoosh). */
    public static final String GHOST = "piece.horo.ghost";
    /** Someone passed through by a Negative Hollow, sinking into despair. */
    public static final String DESPAIR = "piece.horo.despair";
    /** A Mini Hollow bursting. */
    public static final String POP = "piece.horo.pop";
    /** The Toku Hollow exploding. */
    public static final String BOOM = "piece.horo.boom";
    /** The spirit leaving the body, and going back. */
    public static final String SPIRIT = "piece.horo.spirit";
    /** Ghost Network's ghosts spreading out. */
    public static final String NETWORK = "piece.horo.network";

    /** Every event and its clips (assets/hexhaki/sounds/piece/horo/{clip}.ogg). */
    public static final String[][] EVENTS = {
        {GHOST, "ghost1"}, {DESPAIR, "despair1"}, {POP, "pop1"}, {BOOM, "boom1"}, {SPIRIT, "spirit1"}, {NETWORK, "network1"},
    };
}
