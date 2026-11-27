package com.hexhaki.piece.gun;

/**
 * 3.8.8: the guns' sound events ({@code hexhaki:piece.gun.*}, the merged sounds.json; PieceSounds registers them). Every clip is a CC0
 * recording from the web (their folders' SOURCES.txt credit each). Pure: names only.
 */
public final class GunSounds {
    private GunSounds() {
    }

    /** The shot: a black-powder musket's bang. */
    public static final String SHOT = "piece.gun.shot";
    /** Under a Haki shot: a deeper boom (the ball leaving pure black). */
    public static final String HAKI_SHOT = "piece.gun.haki_shot";
    /** The ball pressed home into the muzzle (a ramrod's slide and knock, played as the ball reaches the muzzle). */
    public static final String LOAD = "piece.gun.load";
    /** The hammer drawn back at the end of the reload: ready. */
    public static final String COCK = "piece.gun.cock";
    /** The gun spun round the finger. */
    public static final String SPIN = "piece.gun.spin";
    /** The ball striking a block or a body. */
    public static final String HIT = "piece.gun.hit";

    /** Every event and its clips (assets/hexhaki/sounds/piece/{clip}.ogg: a folder and a name; gun/ from tools/gun_sounds.py). */
    public static final String[][] EVENTS = {
        {SHOT, "gun/shot1", "gun/shot2"}, {HAKI_SHOT, "gun/boom1"}, {LOAD, "gun/load1"}, {COCK, "gun/cock1"}, {SPIN, "gun/spin1"},
        {HIT, "clash/impact2", "clash/impact3"},
    };
}
