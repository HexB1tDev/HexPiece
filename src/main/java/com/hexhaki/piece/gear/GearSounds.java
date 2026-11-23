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

    /**
     * 3.4.9 (the owner: "we also need a sound redesign for most of the abilities. The like mostly like cartoony sounds, so go get them
     * from the web"): the original's own Gear 5 events, played where the original plays them, now on cartoon recordings from Freesound
     * (CC0, cut by tools/gear_sounds.py into assets/hexhaki/sounds/piece/nika, credited in its SOURCES.txt; sounds.json overrides each
     * event, its subtitle and range kept): the stretching punch thrown (a quick cartoon zip), a stretched fist landing (a cartoony slap),
     * the Gatling's fists (two kung-fu punches), Rocket's haul and a dodge (short swooshes), Gigante and the Bajrang Gun's fist swelling (a
     * party balloon inflating), the Bajrang Gun's slam and the Giant's throw (a long low whoosh), its impact (a cassette cartoon
     * explosion), Coil Spring (a rubber stretch), the bounding walk's bounds (a cartoon pop), Rubber World (a wet splat). The lance's
     * lightning (gear5_lightning_throw) is left as it is. Event, then its clips.
     */
    public static final String[][] REDONE = {
        {"gear5_pistol", "pistol1"}, {"gear5_impact", "impact1"}, {"gear5_gatling", "gatling1", "gatling2"}, {"gear5_rocket", "rocket1"},
        {"gear5_dodge", "dodge1"}, {"gear5_giant", "giant1"}, {"gear5_whip", "throw1"}, {"gear5_bajrang_charge", "charge1"},
        {"gear5_bajrang_slam", "slam1"}, {"gear5_bajrang_impact", "boom1"}, {"gear5_coil", "coil1"}, {"gear5_sky_bounce", "bounce1"},
        {"gear5_rubber_world", "world1"},
    };

    /** Every event and its clips (assets/hexhaki/sounds/piece/gear/{clip}.ogg). */
    public static final String[][] EVENTS = {
        {DRUM, "drum1"}, {DRUM_BIG, "drum2"}, {DAWN_PISTOL, "dawn_pistol1"}, {GOOF, "goof_spin"}, {GOOF_PUNCH, "dawn_pistol1"},
        {GOOF_POP, "goof_pop1"}, {WIND, "whip_wind1"}, {SPIN, "whip_spin1"}, {CRACK, "whip_crack1"},
    };
}
