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
    /**
     * The Haki slash: its height (the vertical cutting plane), travel distance, damage and Haki energy cost, least to full charge. 2.2.0
     * (the owner: "the right click slash is ridiculously small it's meant to be at max charge like 100 times this"): at full charge a
     * plane 64 blocks tall flying 220 blocks (it was 6 and 36).
     */
    public static final double SLASH_HEIGHT_MIN = 2.0;
    public static final double SLASH_HEIGHT_MAX = 64.0;
    public static final double SLASH_RANGE_MIN = 16.0;
    public static final double SLASH_RANGE_MAX = 220.0;
    public static final double SLASH_DAMAGE_MIN = 8.0;
    public static final double SLASH_DAMAGE_MAX = 40.0;
    public static final double SLASH_COST_MIN = 10.0;
    public static final double SLASH_COST_MAX = 35.0;
    /** Blocks a tick, least to full charge. */
    public static final double SLASH_SPEED = 1.8;
    public static final double SLASH_SPEED_MAX = 4.0;
    /**
     * The slash's thickness across its flight (blocks). 2.2.4 gave the wave a body {@code 0.07} of its height thick each side (9 blocks
     * across at full charge). 2.4.9 (the owner: "the sword slash width on all charges should always be 1 block wide, 1x1, never more",
     * and asked, the hit, the cut and the wave drawn alike): at every charge the slash hits what a wall {@link #SLASH_WIDTH} thick
     * passes through, cuts a trench one block wide, and is drawn no wider: its body {@link #SLASH_BODY} across in the ground, thinning
     * to its peak ({@link #slashThicknessAt}), its glow and tongues squeezed into the rest ({@code SlashMesh.squeeze}). Its height and
     * its shape along its flight are as they were.
     */
    public static final double SLASH_WIDTH = 1.0;
    public static final double SLASH_BODY = 0.7;
    /** The wave's shape, in its height: how far its leading edge stands ahead of the cutting line and its back slope runs behind it. */
    public static final double SLASH_FRONT = 0.18;
    public static final double SLASH_BACK = 0.48;
    /**
     * Ticks before the next Haki slash. 2.5.4 (the owner: "vastly increase the cooldown of the sword slash projection move"): 30 seconds
     * (it was 3); while it cools, the charged cut is steel alone (its own cooldown).
     */
    public static final int SLASH_COOLDOWN = 600;
    /** A captain's slash stays the size it was (a pirate captain is not to carve the world). */
    public static final double NPC_SLASH_HEIGHT_MAX = 6.0;
    public static final double NPC_SLASH_RANGE_MAX = 36.0;
    /** The terrain cut's budget: block positions looked at and blocks removed, per slash per tick (four times that for all slashes). */
    public static final int CUT_CHECKS_PER_TICK = 2400;
    public static final int CUT_BREAKS_PER_TICK = 240;
    /**
     * Ticks without a cut before the sword goes back in its saya, and the sheathing animation's length; 2.2.1 the draw's (the owner:
     * "the sheath and unsheath is too fast. Slow those animations down. Don't make the blade usable until it's fully unsheathed ... It
     * should be the only drawback from having the swordsman ... Unless it's a quick draw"): from the saya, a cut or the charge waits
     * for the whole draw; the quick draw does not.
     */
    public static final int SHEATHE_DELAY = 8;
    public static final int SHEATHE_TICKS = 24;
    public static final int DRAW_TICKS = 28;
    /**
     * 2.3.2 (the owner: "The pull out animation is way too fast on some swords, yoru should be slower than all"): a katana's draw 28
     * ticks (it was 22) and its sheathing 24 (20), Gryphon's the same (every blade shares the katanas' animations); Yoru, the great
     * sword pulled off the back, 44 and 36.
     */
    public static final int YORU_DRAW_TICKS = 44;
    public static final int YORU_SHEATHE_TICKS = 36;
    /** Past the blade's tip, the cut's swept volume reaches this far (blocks): the air the cut moves. And the volume's radius. */
    public static final double EXTEND = 0.8;
    public static final double RADIUS = 0.35;
    /**
     * 2.4.5: model px from the guard to the point of the blade every cut's path is written with (tools/sword_rig.py BLADE_TIP), of its
     * mesh (2.6.2: drawn, and its path written, {@link Katana#SIZE} times as long).
     */
    public static final float PATH_TIP = 21.0F;
    /** 2.4.5: blocks a model px of a blade in a player's hand comes to (a pixel, at the player model's 0.9375 scale). */
    public static final double PX = 0.9375 / 16.0;

    private SwordRules() {
    }

    /** 2.3.2: how long {@code katana}'s draw takes (null: a katana's). */
    public static int drawTicks(Katana katana) {
        if (katana != null && katana.carry() == Katana.Carry.BACK) {
            return YORU_DRAW_TICKS; // 3.8.7: every weapon off the back (the mace's pull-out is slow too)
        }
        return DRAW_TICKS;
    }

    /** 2.3.2: how long {@code katana}'s sheathing takes (null: a katana's). */
    public static int sheatheTicks(Katana katana) {
        if (katana != null && katana.carry() == Katana.Carry.BACK) {
            return YORU_SHEATHE_TICKS;
        }
        return SHEATHE_TICKS;
    }

    /**
     * Where the slash's plane is centred, above the point it flies from (the eye): it stands up from a little below the feet (a trench as
     * deep as a tenth of its height, at most two and a half blocks) instead of being centred on the eye, so a tall one towers over the
     * land rather than digging a canyon under its owner.
     */
    public static double slashCentre(double height) {
        return height / 2.0 - 1.6 - Math.min(2.5, height * 0.1);
    }

    /** How thick across its flight the wall a slash hits with is: {@link #SLASH_WIDTH}, whatever its height (2.4.9). */
    public static double slashThickness(double height) {
        return SLASH_WIDTH;
    }

    /** The share of that thickness the wave has {@code v} of the way up (0 the trench, 1 the peak). */
    public static double slashProfile(double v) {
        return Math.pow(Math.max(0.0, 1.0 - Math.max(0.0, v)), 0.6);
    }

    /** How thick the wave's body is {@code v} of the way up: {@link #SLASH_BODY} in the ground, thinning, never under a block's tenth. */
    public static double slashThicknessAt(double height, double v) {
        return Math.max(0.1, SLASH_BODY * slashProfile(v));
    }

    /**
     * 2.3.3 (the owner: "your right click is destroying a ridiculous amount of terrain. It should be like maximum like 2 blocks wide.
     * It's destroying buildings and they're not repairing themselves. Like, it should repair itself afterwards and should not drop
     * blocks"): the trench the slash cuts is at most this wide (blocks), however wide the wave's body is drawn; what it cuts drops
     * nothing and grows back (server/Scars). 2.4.9: one block (one cell a row, on the slash's line).
     */
    public static final double TRENCH_MAX = 1.0;

    /** How wide the trench is {@code v} of the way up the slash: the body's width there, never over {@link #TRENCH_MAX}. */
    public static double trenchAt(double height, double v) {
        return Math.min(TRENCH_MAX, slashThicknessAt(height, v));
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
