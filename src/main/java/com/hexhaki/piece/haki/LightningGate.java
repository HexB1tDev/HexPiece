package com.hexhaki.piece.haki;

/**
 * 3.6.0 (the owner: "Full COnqueror on releasing should not play all the lightning sounds at once, just one (currently they're all
 * playing at once when the key is released causing a ridiculously loud sound)"): one Haki lightning sound heard at a time, whoever
 * plays it (the server's release, the original's Armament zaps and Last Stand thunder, the expansion's crackles and landings).
 *
 * <p>Pure: client/haki/LightningSounds asks it for every sound about to start. A lightning sound starts only when none is still
 * ringing, or when it outranks the one that is (the release's strike over a thunder over a zap); while it rings ({@link #LENGTH}, the
 * recordings' own lengths in tools/lightning_sounds.py) every other lightning sound of its rank or below is skipped.
 */
public final class LightningGate {
    public static final int NONE = 0;
    /** The Haki zap: the original's {@code armament_ambient_zap} (piece/lightning/web/zap1..3). */
    public static final int ZAP = 1;
    /** The storm's thunder: the original's {@code last_stand_lightning} (piece/lightning/web/thunder). */
    public static final int THUNDER = 2;
    /** The release's strike: the original's {@code conqueror_release} (piece/lightning/web/strike1..3). */
    public static final int STRIKE = 3;
    /** How long each rank's recording rings (milliseconds): zap1 0.65 s, thunder 3.48 s, strike1..3 4 s. */
    public static final long[] LENGTH = {0L, 650L, 3500L, 4000L};

    private long until = Long.MIN_VALUE;
    private int rank = NONE;

    /** The rank of the sound event {@code namespace:path}, {@link #NONE} for any sound that is not a Haki lightning sound. */
    public static int rank(String namespace, String path) {
        if (!"hexhaki".equals(namespace) || path == null) {
            return NONE;
        }
        return switch (path) {
            case "armament_ambient_zap" -> ZAP;
            case "last_stand_lightning" -> THUNDER;
            case "conqueror_release" -> STRIKE;
            default -> NONE;
        };
    }

    /** True if a sound of {@code rank} may start at {@code now} (milliseconds); a lightning sound let through holds the gate. */
    public boolean allow(int rank, long now) {
        if (rank <= NONE || rank >= LENGTH.length) {
            return true;
        }
        if (now < until && rank <= this.rank) {
            return false;
        }
        until = now + LENGTH[rank];
        this.rank = rank;
        return true;
    }
}
