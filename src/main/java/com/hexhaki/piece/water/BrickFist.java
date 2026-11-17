package com.hexhaki.piece.water;

/**
 * 2.3.9: Fishman Karate's Brick Fist (pure, tested in {@code StyleTests}). The owner: "Brick Fist: The user punches where he's looking
 * creating a water shockwave, causes knockback and ragdoll (Ragdoll mechanic can be taken from HexGhoul)".
 *
 * <p>The fist driven out along the look on {@link #STRIKE}: from it a shockwave of water rushes out the way the user looks (up and down
 * too): rings of water ({@link #rings}, one a tick, the first the biggest) racing out at {@link #SPEED}, opening wider ({@link #major})
 * and thinning ({@link #minor}) as they go, gone at {@link #REACH}; spray torn off them. Everything in its path ({@link #RADIUS} round
 * the line, {@link #REACH} long) is struck: hurt, thrown along it and knocked off its feet (a rag doll, {@code style/Tumble}).
 */
public final class BrickFist {
    /** The tick the fist lands (its hit). */
    public static final int STRIKE = 8;
    /** How far the shockwave reaches, how far round its line it strikes (blocks), how fast it rushes out (blocks a tick). */
    public static final double REACH = 8.5;
    public static final double RADIUS = 1.4;
    public static final double SPEED = 1.7;
    /** Its rings: how many (one a tick), how wide they open from and to, how thick their water is from and to. */
    public static final int RINGS = 3;
    public static final double OPEN_FROM = 0.3;
    public static final double OPEN_TO = 1.9;
    public static final double THICK_FROM = 0.2;
    public static final double THICK_TO = 0.05;
    /** Where the fist is, ahead of the eyes along the look (blocks). */
    public static final double FIST = 0.9;

    private BrickFist() {
    }

    /** How far ring {@code k} has come ({@code age} ticks after the strike), or -1 before it sets out or once it has gone. */
    public static double travelled(int k, double age) {
        double own = age - k;
        if (own < 0) {
            return -1;
        }
        double d = own * SPEED * (1.0 - 0.12 * k);
        return d > REACH ? -1 : d;
    }

    /** Ring {@code k}'s width (out from its line) when it has come {@code d} of {@link #REACH}: opening as it goes, the later rings less. */
    public static double major(int k, double d) {
        double t = Math.max(0.0, Math.min(1.0, d / REACH));
        double open = OPEN_FROM + (OPEN_TO - OPEN_FROM) * Math.sqrt(t);
        return open * (1.0 - 0.18 * k);
    }

    /** Its water's thickness then: thinning as it opens. */
    public static double minor(int k, double d) {
        double t = Math.max(0.0, Math.min(1.0, d / REACH));
        return (THICK_FROM + (THICK_TO - THICK_FROM) * t) * (1.0 - 0.2 * k);
    }

    /** How much of it is still there (1 whole, fading out over its last stretch). */
    public static double left(double d) {
        double t = d / REACH;
        return t < 0.7 ? 1.0 : Math.max(0.0, 1.0 - (t - 0.7) / 0.3);
    }

    /** How many rings. */
    public static int rings() {
        return RINGS;
    }

    /** How long (ticks after the strike) the shockwave is seen at all. */
    public static double length() {
        return RINGS - 1 + REACH / (SPEED * (1.0 - 0.12 * (RINGS - 1)));
    }
}
