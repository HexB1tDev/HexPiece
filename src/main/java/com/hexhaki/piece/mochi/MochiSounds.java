package com.hexhaki.piece.mochi;

/**
 * 3.5.5: the Mochi Mochi no Mi's own sound events ({@code hexhaki:piece.mochi.*}, the merged sounds.json). Every clip a CC0 recording
 * from Freesound.org. 3.6.8 (the owner: "literally every sound of dough sounds like shit, get new ones from the web of slime, viscous
 * stuff, dough etc"): every dough sound is real slime, goo and clay now (tools/mochi_sounds.py, sounds/piece/mochi: stretched, slapped,
 * squished, splatted, squeezed); the fire, the blast and the wheel's rumble are the mod's own as before. Broadband, never a pitched
 * line. Pure: names only (PieceSounds registers them).
 */
public final class MochiSounds {
    private MochiSounds() {
    }

    /** A mochi arm stretching out (the punches, the Anemone's arms, the donuts' fists). */
    public static final String STRETCH = "piece.mochi.stretch";
    /** A mochi fist landing. */
    public static final String PUNCH = "piece.mochi.punch";
    /** Mochi forming, sticking, closing over someone (the sticky mochi, the dodge's hole, the Awakening). */
    public static final String SQUISH = "piece.mochi.squish";
    /** A splat of mochi (a glob landing, a tendril striking). */
    public static final String SPLAT = "piece.mochi.splat";
    /** Mochi Tsuki's drill twisting and thrust. */
    public static final String DRILL = "piece.mochi.drill";
    /** Yanagi Mochi's stamps. */
    public static final String STOMP = "piece.mochi.stomp";
    /** A heavy slam (Kaku Mochi, the club, the walls, the dome crushing). */
    public static final String SLAM = "piece.mochi.slam";
    /** Yaki Mochi's forearm swelling with pressure. */
    public static final String INFLATE = "piece.mochi.inflate";
    /** Yaki Mochi's burning fist launched. */
    public static final String FIRE = "piece.mochi.fire";
    /** Yaki Mochi's blast. */
    public static final String BOOM = "piece.mochi.boom";
    /** Zan Giri Mochi's wheel rolling; the Awakening's ground and wave heaving. */
    public static final String ROLL = "piece.mochi.roll";
    /** Muso Donuts appearing. */
    public static final String POP = "piece.mochi.pop";

    /** Every event and its clips (assets/hexhaki/sounds/piece/{clip}.ogg: a folder and a name). */
    public static final String[][] EVENTS = {
        {STRETCH, "mochi/stretch1", "mochi/stretch2"}, {PUNCH, "mochi/slap1", "mochi/slap2", "mochi/slap3"},
        {SQUISH, "mochi/squish1", "mochi/squish2"}, {SPLAT, "mochi/splat1", "mochi/splat2"}, {DRILL, "supa/spin"}, {STOMP, "mochi/stamp1"},
        {SLAM, "mochi/slam1"}, {INFLATE, "mochi/swell1"}, {FIRE, "magu/whoosh1"}, {BOOM, "magu/boom1"}, {ROLL, "gura/rumble"},
        {POP, "horo/pop1"},
    };
}
