package com.hexhaki.piece.noro;

/**
 * 3.3.4: the Noro Noro no Mi's own sound events ({@code hexhaki:piece.noro.*}, the merged sounds.json), every clip a CC0 recording from
 * Freesound.org cut by tools/noro_sounds.py (sounds/piece/noro, credited in its SOURCES.txt), broadband with no pitched line. (The first
 * 3.3.4 push borrowed the mod's own clips while Freesound refused that session: replaced.) Pure: names only (PieceSounds registers
 * them; the beam is a loop the client plays while it shines).
 */
public final class NoroSounds {
    private NoroSounds() {
    }

    /** A burst of Noroma photons let go (the tap, each of the spread's). */
    public static final String PHOTON = "piece.noro.photon";
    /** The steady beam: a loop while RMB is held. */
    public static final String BEAM = "piece.noro.beam";
    /** Something caught by the photons, slowing down. */
    public static final String SLOW = "piece.noro.slow";
    /** A blow held back on a slowed body (a soft muffled thud). */
    public static final String STORE = "piece.noro.store";
    /** The stored blows landing all at once. */
    public static final String RELEASE = "piece.noro.release";
    /** The Noroma Mirror forming, and a shot turned back off it. */
    public static final String MIRROR = "piece.noro.mirror";
    /** The Slow Field's dome rising. */
    public static final String FIELD = "piece.noro.field";

    /** Every event and its clips (assets/hexhaki/sounds/piece/noro/{clip}.ogg). */
    public static final String[][] EVENTS = {
        {PHOTON, "photon1", "photon2"}, {BEAM, "beam"}, {SLOW, "slow1", "slow2"}, {STORE, "store1", "store2"},
        {RELEASE, "release1"}, {MIRROR, "mirror1", "mirror2"}, {FIELD, "field1"},
    };
}
