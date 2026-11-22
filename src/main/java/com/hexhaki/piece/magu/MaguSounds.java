package com.hexhaki.piece.magu;

/**
 * 3.1.3 (the owner: "Now do the same as you did with the Gurunomi for the Magunomi ... make his abilities"): the Magu Magu no Mi's own
 * sound events ({@code hexhaki:piece.magu.*}, the merged sounds.json; the clips in assets/hexhaki/sounds/piece/magu, every one a CC0
 * recording from Freesound.org, cut by tools/magu_sounds.py, credited in its SOURCES.txt). Pure: names only (PieceSounds registers them;
 * the bubbling is a loop the client plays while magma is gathered or the ground boils).
 */
public final class MaguSounds {
    private MaguSounds() {
    }

    /** Magma bubbling: a loop (the fist swelling, the ground boiling). */
    public static final String BUBBLE = "piece.magu.bubble";
    /** A magma fist or a stream of magma thrown: a fire whoosh. */
    public static final String WHOOSH = "piece.magu.whoosh";
    /** Magma bursting where it lands. */
    public static final String BURST = "piece.magu.burst";
    /** The giant fist exploding, the hound's flood: a big fiery explosion. */
    public static final String BOOM = "piece.magu.boom";
    /** Magma meeting flesh or the ground: a hiss. */
    public static final String SIZZLE = "piece.magu.sizzle";
    /** The hound and the geysers: a roar of fire. */
    public static final String ROAR = "piece.magu.roar";

    /** Every event and its clips (assets/hexhaki/sounds/piece/magu/{clip}.ogg). */
    public static final String[][] EVENTS = {
        {BUBBLE, "bubble"}, {WHOOSH, "whoosh1", "whoosh2"}, {BURST, "burst1", "burst2"}, {BOOM, "boom1"}, {SIZZLE, "sizzle1", "sizzle2"},
        {ROAR, "roar1"},
    };
}
