package com.hexhaki.piece.supa;

/**
 * 3.3.5: the Supa Supa no Mi's own sound events ({@code hexhaki:piece.supa.*}, the merged sounds.json), every clip a CC0 recording from
 * Freesound.org cut by tools/supa_sounds.py (sounds/piece/supa, credited in its SOURCES.txt), broadband with no pitched line (a steel
 * "shing" rings like a bell: the owner will not have melodic sounds, so blades are heard as swishes, scrapes and cuts). Pure: names only
 * (PieceSounds registers them; the skate and the spin are loops the client plays while they last).
 */
public final class SupaSounds {
    private SupaSounds() {
    }

    /** A blade swung through the air (a claw swipe, a slash thrown, the X). */
    public static final String SWISH = "piece.supa.swish";
    /** Blades sliding out of the body (steel scraped). */
    public static final String DRAW = "piece.supa.draw";
    /** A blade cutting into someone. */
    public static final String CUT = "piece.supa.cut";
    /** A blow glancing off the steel body (Spider, Slashing Immunity). */
    public static final String GLANCE = "piece.supa.glance";
    /** A flying slash or the drills tearing through, ending against a wall. */
    public static final String RIP = "piece.supa.rip";
    /** Skating on the foot blades: a loop while Atomic Spurt lasts. */
    public static final String SKATE = "piece.supa.skate";
    /** Spar Break's spinning blades: a loop while it spins. */
    public static final String SPIN = "piece.supa.spin";

    /** Every event and its clips (assets/hexhaki/sounds/piece/supa/{clip}.ogg). */
    public static final String[][] EVENTS = {
        {SWISH, "swish1", "swish2"}, {DRAW, "draw1"}, {CUT, "cut1", "cut2"}, {GLANCE, "glance1"}, {RIP, "rip1"}, {SKATE, "skate"},
        {SPIN, "spin"},
    };
}
