package com.hexhaki.piece.gura;

/**
 * 3.1.1 (the owner: "Now do Gura Gura, get reference gifs from white beard and black beard both"): the Gura Gura no Mi's own sound
 * events ({@code hexhaki:piece.gura.*}, the merged sounds.json; the clips in assets/hexhaki/sounds/piece/gura, every one a CC0
 * recording from Freesound.org, cut by tools/gura_sounds.py, credited in its SOURCES.txt). Pure: names only (PieceSounds registers
 * them; the rumble is a loop the client plays while a quake is held).
 */
public final class GuraSounds {
    private GuraSounds() {
    }

    /** The quake let out of the fist: a deep boom with its rumble (Kaishin, Gekishin, Kabutowari). */
    public static final String BOOM = "piece.gura.boom";
    /** The air cracking like glass where the fist strikes. */
    public static final String CRACK = "piece.gura.crack";
    /** The ground quaking: a loop, held while Shima Yurashi is held or the bubble gathers on the fist. */
    public static final String RUMBLE = "piece.gura.rumble";
    /** The island tilted, a slam into the ground: an earthquake's crack and roar. */
    public static final String QUAKE = "piece.gura.quake";
    /** Rock torn apart and falling (the rift, a crater). */
    public static final String CRUMBLE = "piece.gura.crumble";
    /** The tsunami breaking. */
    public static final String WAVE = "piece.gura.wave";
    /** The leap, the dash. */
    public static final String WHOOSH = "piece.gura.whoosh";
    /** A blow landing hard. */
    public static final String HIT = "piece.gura.hit";

    /** Every event and its clips (assets/hexhaki/sounds/piece/gura/{clip}.ogg). */
    public static final String[][] EVENTS = {
        {BOOM, "boom1", "boom2"}, {CRACK, "crack1", "crack2"}, {RUMBLE, "rumble"}, {QUAKE, "quake1"}, {CRUMBLE, "crumble1", "crumble2"},
        {WAVE, "wave1"}, {WHOOSH, "whoosh1"}, {HIT, "hit1"},
    };
}
