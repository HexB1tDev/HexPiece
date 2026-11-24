package com.hexhaki.piece.haki;

/**
 * 3.6.0 (the owner: "We're also changing haki entirely AGAIN, in the starter menu where you pick style, gender wtf, we'll add an option
 * to choose haki colour the new colors are the following"): the seven Haki colours a player chooses on the start menu (pure, tested in
 * {@code RyouTests}). Each is named by its colours alone (never by a character), and is three colours: the lightning's body ({@link
 * #body}: 0 is the black body every Haki lightning had, edge-lit in the rim's hue), its rim ({@link #rim}: the player's Ryou colour
 * everywhere it was used: the fists' outline, the slashes, the tinted particles) and its glow ({@link #glow}). No rim here is one of
 * the old random Ryou colours ({@link RyouPalette}), so a stroke's rim tells which set it is ({@link #byRim}).
 */
public enum HakiColour {
    /** "Black and Red. The most iconic combination used for clashes and advanced coating". */
    BLACK_RED("Black and Red", 0, 0xFF1A2E, 0xFF1A2E),
    /** "Gold and Yellow": golden lightning with a yellow rim. */
    GOLD_YELLOW("Gold and Yellow", 0xC08410, 0xFFE438, 0xFFE438),
    /** "Purple". */
    PURPLE("Purple", 0x3A0C66, 0xB44DFF, 0xB44DFF),
    /** "Green". */
    GREEN("Green", 0x0B4A1E, 0x3CFF70, 0x3CFF70),
    /** "Blue". */
    BLUE("Blue", 0x0B2470, 0x3F8DFF, 0x3F8DFF),
    /** "Green, Cyan and Black": a black body, a green rim, a cyan glow. */
    GREEN_CYAN_BLACK("Green, Cyan and Black", 0, 0x2EFF62, 0x26EEFF),
    /** "Red and Light Red": a red body with a light red rim. */
    RED_LIGHT_RED("Red and Light Red", 0xA00E1E, 0xFF8494, 0xFF8494);

    public final String display;
    public final int body;
    public final int rim;
    public final int glow;

    HakiColour(String display, int body, int rim, int glow) {
        this.display = display;
        this.body = body;
        this.rim = rim;
        this.glow = glow;
    }

    /** Saved and sent as its ordinal + 1 (0: not chosen yet). */
    public int code() {
        return this.ordinal() + 1;
    }

    public static HakiColour byCode(int code) {
        return code >= 1 && code <= values().length ? values()[code - 1] : null;
    }

    /** The set whose rim is {@code rgb} (any alpha), or null (an old random Ryou colour, the neutral steel). */
    public static HakiColour byRim(int rgb) {
        int c = rgb & 0xFFFFFF;
        for (HakiColour colour : values()) {
            if (colour.rim == c) {
                return colour;
            }
        }
        return null;
    }

    /** The lightning's body colour (RGB) for a rim of {@code rgb}: the set's own, else 0 (black, edge-lit in the rim's hue). */
    public static int bodyFor(int rgb) {
        HakiColour colour = byRim(rgb);
        return colour == null ? 0 : colour.body;
    }

    /** The lightning's glow colour (RGB) for a rim of {@code rgb}: the set's own, else the rim's. */
    public static int glowFor(int rgb) {
        HakiColour colour = byRim(rgb);
        return colour == null ? rgb & 0xFFFFFF : colour.glow;
    }

    /**
     * The set nearest an old random Ryou colour {@code rgb} (by hue; for a player who set sail before the choice existed, until they
     * choose).
     */
    public static HakiColour nearest(int rgb) {
        HakiColour exact = byRim(rgb);
        if (exact != null) {
            return exact;
        }
        double hue = hue(rgb);
        HakiColour best = BLACK_RED;
        double bestGap = Double.MAX_VALUE;
        for (HakiColour colour : new HakiColour[] {BLACK_RED, GOLD_YELLOW, GREEN, GREEN_CYAN_BLACK, BLUE, PURPLE}) {
            double gap = Math.abs(hue - hue(colour.rim));
            gap = Math.min(gap, 360.0 - gap);
            if (gap < bestGap) {
                bestGap = gap;
                best = colour;
            }
        }
        return best;
    }

    static double hue(int rgb) {
        double r = ((rgb >> 16) & 255) / 255.0;
        double g = ((rgb >> 8) & 255) / 255.0;
        double b = (rgb & 255) / 255.0;
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double d = max - min;
        if (d < 1.0E-6) {
            return 0.0;
        }
        double h;
        if (max == r) {
            h = ((g - b) / d) % 6.0;
        } else if (max == g) {
            h = (b - r) / d + 2.0;
        } else {
            h = (r - g) / d + 4.0;
        }
        h *= 60.0;
        return h < 0 ? h + 360.0 : h;
    }
}
