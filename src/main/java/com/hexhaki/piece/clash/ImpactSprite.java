package com.hexhaki.piece.clash;

/**
 * 3.5.8 (the owner, with their own impact frame, art/impact-frame.gif: "this will replace every impact frame in the game (but the Quake N
 * ultimate move) ... feel free to edit it ... change color, speed or whatever is needed for it to look good"): every impact frame but
 * the Gura ultimate's (S2CClash.CLASSIC: the old {@link ImpactFrame}) is the owner's burst, laid out by tools/impact_texture.py
 * (assets/hexhaki/textures/piece/impact/impact_frame.png: {@link #FRAMES} frames of {@link #CELL} pixels, {@link #COLUMNS} across),
 * played at {@link #PER_SECOND} frames a second over the view, centred where the blow landed, under a dark veil that falls and lifts
 * with the flash (the anime's impact frame), bigger for a harder blow. Pure: client/fx/ClashFx draws it.
 */
public final class ImpactSprite {
    public static final int FRAMES = 58;
    public static final int CELL = 252;
    public static final int COLUMNS = 8;
    public static final int SHEET = CELL * COLUMNS;
    public static final double PER_SECOND = 50.0;
    /** The flash's first frame (the GIF's 31st: the white burst, after one empty beat). */
    public static final int FLASH = 5;

    private ImpactSprite() {
    }

    /** How long it plays (game ticks). */
    public static double length() {
        return FRAMES / PER_SECOND * 20.0;
    }

    /** Its frame {@code age} ticks in, or -1 once it has played. */
    public static int frame(double age) {
        if (age < 0.0) {
            return 0;
        }
        int f = (int) Math.floor(age / 20.0 * PER_SECOND);
        return f < FRAMES ? f : -1;
    }

    /** Where frame {@code f} sits on the sheet: its left and top pixel. */
    public static int u(int f) {
        return (f % COLUMNS) * CELL;
    }

    public static int v(int f) {
        return (f / COLUMNS) * CELL;
    }

    /** How big it is on a screen {@code width} by {@code height} for a blow of {@code power} (about 1 to 5): its side, pixels. */
    public static int side(float power, int width, int height) {
        double share = Math.max(0.7, Math.min(1.45, 0.62 + 0.16 * power));
        return (int) Math.round(Math.min(width, height) * share);
    }

    /** The dark veil's strength {@code age} ticks in (0 to 1): down at once with the flash, lifted again over a third of a second. */
    public static double veil(double age, float power) {
        double flash = FLASH / PER_SECOND * 20.0;
        if (age < flash) {
            return 0.0;
        }
        double t = (age - flash) / 8.0;
        double most = Math.max(0.35, Math.min(0.62, 0.3 + 0.07 * power));
        return t >= 1.0 ? 0.0 : most * (1.0 - t * t);
    }
}
