package com.hexhaki.piece.dragon;

/**
 * 2.8.6 (the owner: "giving every move of the dragon custom sounds from the web ... pick something epic for everything. Wind sound
 * effects, fire sound effects, fire like blasts, fire explosions, charging, everything comes from the web"): the Uo Uo no Mi's own sound
 * events ({@code hexhaki:piece.uo.*}, the merged sounds.json; the clips in assets/hexhaki/sounds/piece/uo, every one a CC0 recording from
 * Freesound.org, cut by tools/dragon_sounds.py, credited in its SOURCES.txt). Pure: names only (server/PieceSounds registers them).
 */
public final class UoSounds {
    private UoSounds() {
    }

    /** The dragon's roar: fully formed, the bite, the blast let go. */
    public static final String ROAR = "piece.uo.roar";
    /** The body bursting out of the player (a vortex boom) and the rush of it. */
    public static final String FORM = "piece.uo.form";
    public static final String WHOOSH = "piece.uo.whoosh";
    /** The body going back into the player, and its landing. */
    public static final String UNFORM = "piece.uo.unform";
    /** The flying dragon's wind, gust after gust while it goes fast. */
    public static final String GUST = "piece.uo.gust";
    /** Air Pressure: the shot of air, and where it strikes. */
    public static final String AIR = "piece.uo.air";
    public static final String AIR_HIT = "piece.uo.air_hit";
    /** Wind Force's tornado. */
    public static final String TORNADO = "piece.uo.tornado";
    /** The Fire Breath: its ignition, and its roar held while it burns. */
    public static final String FLAME_START = "piece.uo.flame_start";
    public static final String FLAME_LOOP = "piece.uo.flame_loop";
    /** Blast Breath: the charge gathering, the beam of fire. */
    public static final String CHARGE = "piece.uo.charge";
    public static final String BEAM = "piece.uo.beam";
    /** Dragon Embers: the fireballs thrown up, and each one landing. */
    public static final String FIREBALL = "piece.uo.fireball";
    public static final String EMBER_HIT = "piece.uo.ember_hit";
    /** Fire blowing up (the beam striking, the blasts), and the last and biggest blast. */
    public static final String EXPLODE = "piece.uo.explode";
    public static final String BLAST = "piece.uo.blast";
    /** The Gigantic Bite: the lunge and the jaws. */
    public static final String LUNGE = "piece.uo.lunge";
    public static final String BITE = "piece.uo.bite";

    /** Every event and its clips (assets/hexhaki/sounds/piece/uo/{clip}.ogg). */
    public static final String[][] EVENTS = {
        {ROAR, "roar1", "roar2", "roar3"}, {FORM, "form_boom"}, {WHOOSH, "whoosh1", "whoosh2", "whoosh3"}, {UNFORM, "unform"},
        {GUST, "gust1", "gust2", "gust3"}, {AIR, "air1", "air2"}, {AIR_HIT, "air_hit"}, {TORNADO, "tornado1", "tornado2"},
        {FLAME_START, "flame_start"}, {FLAME_LOOP, "flame_loop1", "flame_loop2"}, {CHARGE, "charge"}, {BEAM, "beam1", "beam2"},
        {FIREBALL, "fireball1"}, {EMBER_HIT, "ember_hit1", "ember_hit2"}, {EXPLODE, "explode1", "explode2"}, {BLAST, "blast1"},
        {LUNGE, "lunge"}, {BITE, "bite1"},
    };
}
