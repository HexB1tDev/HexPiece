package com.hexhaki.piece.ito;

/**
 * 3.3.7: the Ito Ito no Mi's own sound events ({@code hexhaki:piece.ito.*}, the merged sounds.json), every clip a CC0 recording from
 * Freesound.org cut by tools/ito_sounds.py (sounds/piece/ito, credited in its SOURCES.txt), broadband with no pitched line (no twang:
 * a plucked string rings, and the owner will not have melodic sounds). Pure: names only (PieceSounds registers them).
 */
public final class ItoSounds {
    private ItoSounds() {
    }

    /** Strings lashing out (a whip's swish): the left click, Goshikito falling. */
    public static final String SWISH = "piece.ito.swish";
    /** A string bullet shot (a short snap). */
    public static final String SHOT = "piece.ito.shot";
    /** Strings cutting into someone or the ground. */
    public static final String SLICE = "piece.ito.slice";
    /** Overheat: a burning rope whipping out. */
    public static final String BURN = "piece.ito.burn";
    /** Parasite's strings pulling taut, Black Knight woven. */
    public static final String PULL = "piece.ito.pull";
    /** Spider's Web spreading, something caught in it. */
    public static final String WEB = "piece.ito.web";
    /** The Birdcage rising and closing. */
    public static final String CAGE = "piece.ito.cage";
    /** Break White's waves rolling, God Thread's spears. */
    public static final String WAVE = "piece.ito.wave";
    /** Sora no Michi: a step on the strings. */
    public static final String STEP = "piece.ito.step";

    /** Every event and its clips (assets/hexhaki/sounds/piece/ito/{clip}.ogg). */
    public static final String[][] EVENTS = {
        {SWISH, "swish1"}, {SHOT, "shot1"}, {SLICE, "slice1"}, {BURN, "burn1"}, {PULL, "pull1"}, {WEB, "web1"}, {CAGE, "cage1"},
        {WAVE, "wave1"}, {STEP, "step1"},
    };
}
