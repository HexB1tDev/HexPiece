package com.hexhaki.piece.sword;

/**
 * 2.1.4: the Swordsman's numbers (pure, tested): the owner, "Use a configurable maximum charge duration, defaulting to 40 ticks ...
 * Keep minimum and maximum size, travel distance, damage, resource costs and cooldowns configurable ... an eight-tick delay followed
 * by a four-to-six-tick sheathing animation." Each value here is the default; the server's config ({@code PieceConfig},
 * {@code hexpiece-server.toml}) may change it, and {@link #lerp} scales a charged value between its minimum and maximum.
 */
public final class SwordRules {
    /** Ticks of charge that make a full charge, and the charge under which a release counts as the minimum. */
    public static final int CHARGE_MAX = 40;
    public static final int CHARGE_MIN = 4;
    /** The physical (no Haki) charged cut: damage and how far past the blade's tip it reaches, at the least and the full charge. */
    public static final double CUT_DAMAGE_MIN = 9.0;
    public static final double CUT_DAMAGE_MAX = 20.0;
    public static final double CUT_REACH_MIN = 1.2;
    public static final double CUT_REACH_MAX = 2.6;
    public static final int CUT_COOLDOWN = 24;
    /** The Haki slash: its height (the vertical cutting plane), travel distance, damage and Haki energy cost, least to full charge. */
    public static final double SLASH_HEIGHT_MIN = 1.6;
    public static final double SLASH_HEIGHT_MAX = 6.0;
    public static final double SLASH_RANGE_MIN = 12.0;
    public static final double SLASH_RANGE_MAX = 36.0;
    public static final double SLASH_DAMAGE_MIN = 8.0;
    public static final double SLASH_DAMAGE_MAX = 24.0;
    public static final double SLASH_COST_MIN = 10.0;
    public static final double SLASH_COST_MAX = 35.0;
    /** Blocks a tick. */
    public static final double SLASH_SPEED = 1.6;
    /** The slash's thickness (blocks): it hits what this narrow plane passes through, and cuts the blocks it passes. */
    public static final double SLASH_THICKNESS = 0.6;
    public static final int SLASH_COOLDOWN = 60;
    /** The terrain cut's budget: block positions looked at and blocks removed, per slash per tick, and in all per tick. */
    public static final int CUT_CHECKS_PER_TICK = 384;
    public static final int CUT_BREAKS_PER_TICK = 64;
    /** Ticks without a cut before the sword goes back in its saya, and the sheathing animation's length. */
    public static final int SHEATHE_DELAY = 8;
    public static final int SHEATHE_TICKS = 6;
    /** Past the blade's tip, the cut's swept volume reaches this far (blocks): the air the cut moves. And the volume's radius. */
    public static final double EXTEND = 0.8;
    public static final double RADIUS = 0.35;

    private SwordRules() {
    }

    /** How charged (0 at the least, 1 at the full charge) a release after {@code ticks} of charging is. */
    public static double charge(long ticks, int max) {
        long clamped = Math.max(CHARGE_MIN, Math.min(max, ticks));
        return max <= CHARGE_MIN ? 1.0 : (clamped - CHARGE_MIN) / (double) (max - CHARGE_MIN);
    }

    public static double lerp(double min, double max, double charge) {
        return min + (max - min) * Math.max(0.0, Math.min(1.0, charge));
    }
}
