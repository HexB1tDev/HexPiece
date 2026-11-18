package com.hexhaki.piece.water;

/**
 * 2.3.9: Fishman Karate's Brick Fist (pure, tested in {@code StyleTests}). The owner: "Brick Fist: The user punches where he's looking
 * creating a water shockwave, causes knockback and ragdoll (Ragdoll mechanic can be taken from HexGhoul)".
 *
 * <p>The fist driven out along the look on {@link #STRIKE}: from it a shockwave of water rushes out the way the user looks (up and down
 * too): rings of water ({@link #rings}, one a tick, the first the biggest) racing out at {@link #SPEED}, opening wider ({@link #major})
 * and thinning ({@link #minor}) as they go, gone at {@link #REACH}; spray torn off them. Everything in its path ({@link #RADIUS} round
 * the line, {@link #REACH} long) is struck: hurt, thrown along it and knocked off its feet (a rag doll, {@code style/Tumble}).
 * <p>2.4.4 (the owner: "the brick punch ... seems ... very lackluster ... it doesn't look very strong"): five heavy rings, faster, riding
 * a torrent of water ({@link #torrent}); a big burst off the fist; a splash where it strikes; a finisher's hit-stop and shake; the
 * punch itself planted and stepped into.
 */
public final class BrickFist {
    /** The tick the fist lands (its hit). */
    public static final int STRIKE = 8;
    /** How far the shockwave reaches, how far round its line it strikes (blocks), how fast it rushes out (blocks a tick). */
    public static final double REACH = 8.5;
    public static final double RADIUS = 1.4;
    /** 2.4.4 (the owner: "it doesn't look very strong"): faster, was 1.7. */
    public static final double SPEED = 2.2;
    /**
     * Its rings: how many (one a tick), how wide they open from and to, how thick their water is from and to. 2.4.4: five heavy rings
     * of water (were three thin ones: 3, 0.3 to 1.9 wide, 0.2 to 0.05 thick), each later one a little smaller and slower.
     */
    public static final int RINGS = 5;
    public static final double OPEN_FROM = 0.55;
    public static final double OPEN_TO = 3.0;
    public static final double THICK_FROM = 0.42;
    public static final double THICK_TO = 0.12;
    /** How much smaller, thinner and slower each later ring is than the one before (of the first's). */
    static final double SMALLER = 0.12;
    static final double THINNER = 0.14;
    static final double SLOWER = 0.08;
    /**
     * 2.4.4: the torrent the rings ride on: a jet of water out of the fist along the look, its head racing a little ahead of the first
     * ring, its body {@link #TORRENT_LENGTH} long, {@link #TORRENT_RADIUS} thick at the head (thinner toward the fist), in
     * {@link #TORRENT_BLOBS} stretched bodies of water.
     */
    public static final double TORRENT_LENGTH = 3.6;
    public static final double TORRENT_RADIUS = 0.42;
    public static final int TORRENT_BLOBS = 5;
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
        double d = own * SPEED * (1.0 - SLOWER * k);
        return d > REACH ? -1 : d;
    }

    /** Ring {@code k}'s width (out from its line) when it has come {@code d} of {@link #REACH}: opening as it goes, the later rings less. */
    public static double major(int k, double d) {
        double t = Math.max(0.0, Math.min(1.0, d / REACH));
        double open = OPEN_FROM + (OPEN_TO - OPEN_FROM) * Math.sqrt(t);
        return open * (1.0 - SMALLER * k);
    }

    /** Its water's thickness then: thinning as it opens. */
    public static double minor(int k, double d) {
        double t = Math.max(0.0, Math.min(1.0, d / REACH));
        return (THICK_FROM + (THICK_TO - THICK_FROM) * t) * (1.0 - THINNER * k);
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
        return RINGS - 1 + REACH / (SPEED * (1.0 - SLOWER * (RINGS - 1)));
    }

    /**
     * 2.4.4: the torrent {@code age} ticks after the strike: {head, tail} distances along the look (the head a little ahead of the first
     * ring, up to {@link #REACH}), or null once it is gone. Its tail leaves the fist after {@link #TORRENT_LENGTH} and follows the head
     * out, so the jet drains off the fist and rushes away.
     */
    public static double[] torrent(double age) {
        if (age < 0) {
            return null;
        }
        double head = Math.min(REACH + 0.5, 0.4 + age * SPEED * 1.1);
        double tail = Math.max(0.0, head - TORRENT_LENGTH);
        if (age * SPEED * 1.1 > TORRENT_LENGTH) {
            // once out, the tail catches up: the jet shortens as it spends itself
            tail = Math.max(tail, (age * SPEED * 1.1 - TORRENT_LENGTH) * 1.4);
        }
        return tail >= head - 0.2 ? null : new double[] {head, tail};
    }

    /** How thick the torrent is {@code u} of the way from its tail (0) to its head (1): thin at the fist, swelling to the head. */
    public static double torrentRadius(double u) {
        double v = Math.max(0.0, Math.min(1.0, u));
        return TORRENT_RADIUS * (0.45 + 0.55 * Math.sqrt(v));
    }
}
