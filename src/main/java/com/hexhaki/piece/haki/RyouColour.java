package com.hexhaki.piece.haki;

/**
 * 3.6.0 (the owner: "also add a menu to pick the Ryou color"): the seven colours of Ryou's glow round the coated arms, chosen on the
 * start menu beside the Haki colour (pure, tested in {@code RyouTests}). Named by their colours alone. Each is the glow's band (its
 * main colour, {@link #band}) and its haze ({@link #haze}): Red & Orange a red band in an orange haze, Purple & Black purple in a black
 * smoke, and so on; the hot rim at the black edge is the band most of the way to white. The arms' glow draws them
 * (haki/FistAura.colours through {@link #byBand}); the lightning keeps the Haki colour (haki/HakiColour).
 */
public enum RyouColour {
    RED_ORANGE("Red & Orange", 0xFF2B24, 0xFF8C1E),
    PURPLE_BLACK("Purple & Black", 0xA64AFF, 0x1A0A24),
    GREEN("Green", 0x3BFF6C, 0x2DD65C),
    PINK("Pink", 0xFF5EC6, 0xFFA0DA),
    RED_YELLOW("Red & Yellow", 0xFF2D1F, 0xFFD43A),
    BLUE("Blue", 0x3C8CFF, 0x74B4FF),
    GOLD("Gold", 0xFFC22F, 0xFFE68C);

    public final String display;
    public final int band;
    public final int haze;

    RyouColour(String display, int band, int haze) {
        this.display = display;
        this.band = band;
        this.haze = haze;
    }

    /** Saved and sent as its ordinal + 1 (0: not chosen yet). */
    public int code() {
        return this.ordinal() + 1;
    }

    public static RyouColour byCode(int code) {
        return code >= 1 && code <= values().length ? values()[code - 1] : null;
    }

    /** The colour whose band is {@code rgb} (any alpha), or null (an older single Ryou colour). */
    public static RyouColour byBand(int rgb) {
        int c = rgb & 0xFFFFFF;
        for (RyouColour colour : values()) {
            if (colour.band == c) {
                return colour;
            }
        }
        return null;
    }
}
