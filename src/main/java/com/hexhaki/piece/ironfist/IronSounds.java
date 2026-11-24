package com.hexhaki.piece.ironfist;

/**
 * 3.5.8: Iron Fist's sound events ({@code hexhaki:piece.iron.*}, the merged sounds.json). Every clip is one of the mod's own CC0
 * recordings from Freesound.org already (their folders' SOURCES.txt credit them): heavy punches, a swing, a pitcher's throw, explosions,
 * the ground cracking and rumbling, a charge, a rush of air. Broadband, never a pitched line. Pure: names only (PieceSounds registers
 * them).
 */
public final class IronSounds {
    private IronSounds() {
    }

    /** A punch thrown through the air. */
    public static final String SWING = "piece.iron.swing";
    /** A fist landing hard. */
    public static final String PUNCH = "piece.iron.punch";
    /** The iron cannonball taken in the hand. */
    public static final String IRON = "piece.iron.iron";
    /** The cannonball hurled. */
    public static final String THROW = "piece.iron.throw";
    /** An explosion (the cannonball's, the barrage's last punch, Galaxy Impact's). */
    public static final String BOOM = "piece.iron.boom";
    /** Haki gathered into a fist. */
    public static final String CHARGE = "piece.iron.charge";
    /** A devastating blow: the ground and the air broken. */
    public static final String SMASH = "piece.iron.smash";
    /** A burst of speed, a leap. */
    public static final String DASH = "piece.iron.dash";
    /** A body thrown into the ground. */
    public static final String SLAM = "piece.iron.slam";
    /** The ground rumbling. */
    public static final String RUMBLE = "piece.iron.rumble";

    /** Every event and its clips (assets/hexhaki/sounds/piece/{clip}.ogg: a folder and a name). */
    public static final String[][] EVENTS = {
        {SWING, "sword/whoosh1", "sword/whoosh2"}, {PUNCH, "clash/impact1", "clash/impact2", "clash/impact3"}, {IRON, "sword/guard_click1"},
        {THROW, "nika/throw1"}, {BOOM, "gura/boom1", "gura/boom2"}, {CHARGE, "gura/charge"}, {SMASH, "nika/slam1", "clash/impact4"},
        {DASH, "nika/rocket1"}, {SLAM, "gura/quake1"}, {RUMBLE, "gura/rumble"},
    };
}
