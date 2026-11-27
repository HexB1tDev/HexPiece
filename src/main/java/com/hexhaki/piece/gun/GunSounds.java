package com.hexhaki.piece.gun;

/**
 * 3.8.8: the guns' sound events ({@code hexhaki:piece.gun.*}, the merged sounds.json; PieceSounds registers them). Every clip is a CC0
 * recording from the web (their folders' SOURCES.txt credit each). Pure: names only.
 */
public final class GunSounds {
    private GunSounds() {
    }

    /** The shot: the flint's crack and the powder's bang. */
    public static final String SHOT = "piece.gun.shot";
    /** Under a Haki shot: a deeper boom (the ball leaving pure black). */
    public static final String HAKI_SHOT = "piece.gun.haki_shot";
    /** The ball pressed home into the muzzle. */
    public static final String LOAD = "piece.gun.load";
    /** The hammer drawn back at the end of the reload: ready. */
    public static final String COCK = "piece.gun.cock";
    /** The gun spun round the finger. */
    public static final String SPIN = "piece.gun.spin";
    /** The ball striking a block or a body. */
    public static final String HIT = "piece.gun.hit";

    /** Every event and its clips (assets/hexhaki/sounds/piece/{clip}.ogg: a folder and a name). */
    public static final String[][] EVENTS = {
        {SHOT, "gear/whip_crack1"}, {HAKI_SHOT, "gura/boom1", "gura/boom2"}, {LOAD, "sword/guard_click2"}, {COCK, "sword/guard_click4"},
        {SPIN, "sword/whoosh1", "sword/whoosh2"}, {HIT, "clash/impact2", "clash/impact3"},
    };
}
