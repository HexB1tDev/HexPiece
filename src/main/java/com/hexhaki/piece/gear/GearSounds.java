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

    /** Every event and its clips (assets/hexhaki/sounds/piece/gear/{clip}.ogg). */
    public static final String[][] EVENTS = {
        {DRUM, "drum1"}, {DRUM_BIG, "drum2"}, {DAWN_PISTOL, "dawn_pistol1"},
    };
}
