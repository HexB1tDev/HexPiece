package com.hexhaki.piece.sword;

/**
 * 2.1.4: what a player's sword is doing, as the server tells every client ({@code S2CSword}) (pure): sheathed (nothing playing), the
 * guard between cuts, the sheathing, the charge (from a drawn or a sheathed sword), the release, the quick draw (from the saya, or with
 * the blade already out), or one of the {@link SwordMove}s ({@link #MOVES} + its ordinal). Each has its animation
 * ({@code assets/hexhaki/player_animation/<name>.json}) and, in {@link BladePaths}, the ticks the blade leaves and enters the saya.
 */
public final class SwordAnims {
    public static final int NONE = 0;
    public static final int GUARD = 1;
    public static final int SHEATHE = 2;
    public static final int CHARGE = 3;
    public static final int CHARGE_DRAW = 4;
    public static final int RELEASE = 5;
    public static final int QUICK_DRAW = 6;
    public static final int QUICK_CUT = 7;
    /** 2.2.1: the slow draw from the saya into the guard (nothing can be done with the blade until it is out). */
    public static final int DRAW = 8;
    public static final int MOVES = 10;
    private static final String[] NAMES = {null, "sword_guard", "sword_sheathe", "sword_charge", "sword_charge_draw", "sword_release",
        "sword_quick_draw", "sword_quick_cut", "sword_draw"};

    private SwordAnims() {
    }

    public static int of(SwordMove move) {
        return MOVES + move.ordinal();
    }

    /** The animation's name, or null (sheathed: nothing plays). */
    public static String name(int anim) {
        if (anim >= MOVES) {
            SwordMove move = SwordMove.byOrdinal(anim - MOVES);
            return move == null ? null : move.animation();
        }
        return anim > 0 && anim < NAMES.length ? NAMES[anim] : null;
    }

    /**
     * 2.2.7, 2.3.2: the animation a player with {@code katana} plays for {@code anim}. Gryphon and Yoru have every animation of their
     * own ({@code <name>_gryphon}, {@code <name>_yoru}: their own draw, sheathing, guard and idle, and every cut written again for the
     * blade, kept off the floor; the owner: "yoru clips through the floor", "Gyphon and Yoru's pull outs are broken"). A legendary
     * blade is drawn the moment combat begins and never drawn mid-fight (the owner, of Yoru's draw cut: "HERE IT IS the bug", Yoru on
     * the back and in the hand at once): what would draw it from a saya plays its drawn counterpart (a draw cut: its branch's first
     * cut; the quick draw: the quick cut; the charge from the saya: the charge).
     */
    public static String name(int anim, Katana katana) {
        String suffix = suffix(katana);
        if (suffix.isEmpty()) {
            return name(anim);
        }
        int own = anim;
        if (anim == QUICK_DRAW) {
            own = QUICK_CUT;
        } else if (anim == CHARGE_DRAW && katana != Katana.YORU) {
            own = CHARGE;
        } else if (anim >= MOVES) {
            SwordMove move = SwordMove.byOrdinal(anim - MOVES);
            if (move != null && move.draw()) {
                own = of(SwordCombo.string(move.branch())[0]);
            }
        }
        String base = name(own);
        return base == null ? null : base + suffix;
    }

    /** 2.3.2: the suffix of a blade's own animations ("" for a katana). */
    public static String suffix(Katana katana) {
        if (katana == Katana.GRYPHON) {
            return "_gryphon";
        }
        return katana == Katana.YORU ? "_yoru" : "";
    }

    /**
     * 2.3.2 (the owner: "using sword makes the player's body too stiff, the head doesnt move ... more ambient animation with all
     * swords", "the left arm is not even moving... why is it frozen"): the guard between cuts is the right arm alone (the walk, the
     * look and the free arm stay the game's); standing still, the client plays this instead: the whole body breathing and shifting,
     * the blade alive in the hand, the head still free.
     */
    public static String idle(Katana katana) {
        return "sword_idle" + suffix(katana);
    }

    /** 2.2.7: {@link #drawn(int, float)} for the animation a player with {@code katana} plays. */
    public static boolean drawn(int anim, float ticks, Katana katana) {
        return drawnIn(name(anim, katana), ticks);
    }

    /** 2.3.2: how long {@code anim} lasts with {@code katana} (the draws and sheathings are each blade's own: Yoru slowest). */
    public static int duration(int anim, Katana katana) {
        if (anim == DRAW) {
            return SwordRules.drawTicks(katana);
        }
        if (anim == SHEATHE) {
            return SwordRules.sheatheTicks(katana);
        }
        return duration(anim);
    }

    /** How long it lasts (ticks; a loop: its whole length, looping from {@link #loopFrom}). */
    public static int duration(int anim) {
        if (anim >= MOVES) {
            SwordMove move = SwordMove.byOrdinal(anim - MOVES);
            return move == null ? 0 : move.duration();
        }
        return switch (anim) {
            case GUARD -> 40;
            case SHEATHE -> SwordRules.SHEATHE_TICKS;
            case DRAW -> SwordRules.DRAW_TICKS;
            case CHARGE, CHARGE_DRAW -> 26;
            case RELEASE -> 15;
            case QUICK_DRAW, QUICK_CUT -> 16;
            default -> 0;
        };
    }

    /** Where a loop starts again (the guard from its start, the charges from their held pose). */
    public static int loopFrom(int anim) {
        return anim == CHARGE || anim == CHARGE_DRAW ? 6 : 0;
    }

    /** The animation's own tick for a moment {@code ticks} after it began (loops wrapped). */
    public static float frame(int anim, float ticks) {
        if (!loops(anim)) {
            return ticks;
        }
        int end = duration(anim);
        int from = loopFrom(anim);
        if (ticks <= end || end <= from) {
            return ticks;
        }
        return from + (ticks - from) % (end - from);
    }

    /** Loops (held until the server says otherwise): the guard and the charges. */
    public static boolean loops(int anim) {
        return anim == GUARD || anim == CHARGE || anim == CHARGE_DRAW;
    }

    /** Is the blade in the hand {@code ticks} into {@code anim} (else in its saya)? */
    public static boolean drawn(int anim, float ticks) {
        return drawnIn(name(anim), ticks);
    }

    static boolean drawnIn(String name, float ticks) {
        if (name == null) {
            return false;
        }
        int draw = BladePaths.drawTick(name);
        int sheath = BladePaths.sheathTick(name);
        if (draw >= 0 && ticks < draw) {
            return false;
        }
        return sheath < 0 || ticks < sheath;
    }
}
