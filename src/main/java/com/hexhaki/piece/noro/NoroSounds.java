package com.hexhaki.piece.noro;

/**
 * 3.3.4: the Noro Noro no Mi's own sound events ({@code hexhaki:piece.noro.*}, the merged sounds.json). Freesound.org refused this
 * session's connection (403, and its pages unreachable for the web reader too), so for now every event plays CC0 Freesound recordings
 * the mod already carries (each credited in its own folder's SOURCES.txt), at its own pitch in sounds.json: nothing made or mixed by
 * hand. Give the fruit recordings of its own (a tools/noro_sounds.py as tools/hie_sounds.py) once Freesound answers. Pure: names only
 * (PieceSounds registers them; the beam is a loop the client plays while it shines).
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

    /** Every event and its clips (assets/hexhaki/sounds/piece/{folder/clip}.ogg: recordings the mod already carries, for now). */
    public static final String[][] EVENTS = {
        {PHOTON, "pika/zap1", "pika/zap2"}, {BEAM, "gura/charge"}, {SLOW, "uo/air2"}, {STORE, "pika/hit1", "pika/hit2"},
        {RELEASE, "pika/pop1"}, {MIRROR, "pika/sparkle1"}, {FIELD, "pika/travel1"},
    };
}
