package com.hexhaki.piece.pika;

/**
 * 2.9.0 (the owner: "The sounds are atrocious ... I keep telling you to get the sounds from the web ... you cannot fucking create
 * sounds"): the Pika Pika no Mi's own sound events ({@code hexhaki:piece.pika.*}, the merged sounds.json; the clips in
 * assets/hexhaki/sounds/piece/pika, every one a CC0 recording from Freesound.org, cut by tools/pika_sounds.py, credited in its
 * SOURCES.txt). Pure: names only (PieceSounds registers them; the two loops are played on the client while their move lasts).
 */
public final class PikaSounds {
    private PikaSounds() {
    }

    /** The laser out of the fingertip; the light gathering there and held (a loop); the beam let go. */
    public static final String LASER = "piece.pika.laser";
    public static final String CHARGE = "piece.pika.charge";
    public static final String CHARGE_HOLD = "piece.pika.charge_hold";
    public static final String BEAM = "piece.pika.beam";
    /** Light blowing up: the laser's, the beam's, the kick's beam; each of Yasakani's bullets bursting. */
    public static final String EXPLODE = "piece.pika.explode";
    public static final String POP = "piece.pika.pop";
    /** Yasakani no Magatama's bullets of light, loosed. */
    public static final String ZAP = "piece.pika.zap";
    /** Going as light (Yata no Kagami, the speed-of-light kick), and the zip of a dash. */
    public static final String TRAVEL = "piece.pika.travel";
    public static final String ZIP = "piece.pika.zip";
    /** The kicks: the swish, the blow landing, the hardest blows, the speed-of-light kick landing. */
    public static final String WHOOSH = "piece.pika.whoosh";
    public static final String HIT = "piece.pika.hit";
    public static final String HIT_HEAVY = "piece.pika.hit_heavy";
    public static final String KICK_BLAST = "piece.pika.kick_blast";
    /** Ama no Murakumo drawn, its hum (a loop), its cuts, put away, a blow met with it. */
    public static final String SWORD_ON = "piece.pika.sword_on";
    public static final String HUM = "piece.pika.hum";
    public static final String SWING = "piece.pika.swing";
    public static final String SWORD_OFF = "piece.pika.sword_off";
    public static final String PARRY = "piece.pika.parry";
    /** The clones of light, a blow passing through the body of light, the mirror forming. */
    public static final String SPARKLE = "piece.pika.sparkle";

    /** Every event and its clips (assets/hexhaki/sounds/piece/pika/{clip}.ogg). */
    public static final String[][] EVENTS = {
        {LASER, "laser1", "laser2"}, {CHARGE, "charge"}, {CHARGE_HOLD, "charge_hold"}, {BEAM, "beam1", "beam2", "beam3"},
        {EXPLODE, "explode1", "explode2"}, {POP, "pop1"}, {ZAP, "zap1", "zap2", "zap3", "zap4"}, {TRAVEL, "travel1"}, {ZIP, "travel2"},
        {WHOOSH, "whoosh1", "whoosh2"}, {HIT, "hit1", "hit2", "hit3", "hit4"}, {HIT_HEAVY, "hit_heavy1", "hit_heavy2"},
        {KICK_BLAST, "kick_blast"}, {SWORD_ON, "sword_on"}, {HUM, "hum"},
        {SWING, "swing1", "swing2", "swing3", "swing4", "swing5"}, {SWORD_OFF, "sword_off"}, {PARRY, "parry1"},
        {SPARKLE, "sparkle1", "sparkle2"},
    };
}
