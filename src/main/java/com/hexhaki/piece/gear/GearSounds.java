package com.hexhaki.piece.gear;

/**
 * 3.2.3 (the owner, on Gear 5's transformation: "if you could give the transformation just the drum sounds and just get rid of the music
 * because of copyright, that would be perfect"): the Nika fruit's own sound events ({@code hexhaki:piece.gear.*}, the merged sounds.json;
 * the clips in assets/hexhaki/sounds/piece/gear, every one a CC0 recording from Freesound.org, cut by tools/gear_sounds.py, credited in
 * its SOURCES.txt). Pure: names only (PieceSounds registers them; server/GearDrums plays them on the chest pounding's beats).
 */
public final class GearSounds {
    private GearSounds() {
    }

    /** One beat of the drums of liberation (a deep tribal floor tom). */
    public static final String DRUM = "piece.gear.drum";
    /** The last, heaviest beat (a heavy cinematic drum hit). */
    public static final String DRUM_BIG = "piece.gear.drum_big";

    /** 3.3.9: Dawn Pistol's cartoon punch where it lands (server/NikaPistol; "Cartoon Punch Cassette F" by modusmogulus). */
    public static final String DAWN_PISTOL = "piece.gear.dawn_pistol";

    /** 3.4.4: Goof Ball's ball of smoke whirling (a loop while it lasts: client/gear/NikaWhipClient), its punches landing (Dawn Pistol's
     *  cartoon punch, quiet), the ball popping ("Poof of Smoke" by Planman). */
    public static final String GOOF = "piece.gear.goof";
    public static final String GOOF_PUNCH = "piece.gear.goof_punch";
    public static final String GOOF_POP = "piece.gear.goof_pop";
    /** 3.4.4: Dawn Whip: the body wound up like rubber ("Rubber Stretch Rip 1" by mickdow), the unwinding whoosh ("Whoosh Swirl Spinning"
     *  by Qvantor), the whip's crack ("Mixed Whip Crack 1" by peterbullmusic); drilling into the ground is the Gura fruit's crumble. */
    public static final String WIND = "piece.gear.whip_wind";
    public static final String SPIN = "piece.gear.whip_spin";
    public static final String CRACK = "piece.gear.whip_crack";

    /** Every event and its clips (assets/hexhaki/sounds/piece/gear/{clip}.ogg). */
    public static final String[][] EVENTS = {
        {DRUM, "drum1"}, {DRUM_BIG, "drum2"}, {DAWN_PISTOL, "dawn_pistol1"}, {GOOF, "goof_spin"}, {GOOF_PUNCH, "dawn_pistol1"},
        {GOOF_POP, "goof_pop1"}, {WIND, "whip_wind1"}, {SPIN, "whip_spin1"}, {CRACK, "whip_crack1"},
    };
}
