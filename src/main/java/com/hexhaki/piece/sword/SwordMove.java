package com.hexhaki.piece.sword;

import com.hexhaki.piece.style.Hit.Weight;
import com.hexhaki.piece.style.Motion;
import java.util.Locale;

/**
 * 2.1.4: every Swordsman move (pure, tested in {@code SwordTests}). The owner: "Implement at least twelve genuinely distinct attack
 * motions across connected combo sequences and contextual variations. Include a fast draw cut, opposing diagonal cuts, horizontal
 * cuts in both directions, a rising cut, a controlled thrust, a two-handed overhead cut [made one-handed when the owner asked for
 * "the entire moveset is now right handed only"], a pivoting cut, an advancing cut and aerial variations."
 * <p>
 * Fifteen attack motions in three strings ({@link SwordCombo}): on the ground, on the move and in the air, each opening with its own
 * draw when the sword is sheathed. Each move has the animation every client plays ({@code sword_<name>}, tools/sword_anims.py, keyed
 * every tick along its blade's arc), how long it lasts, from which tick the next press may start the next cut ({@link #cancel}), its
 * cuts (explicit hit windows; a move with two cuts has two windows), how it carries the player, and whether it opens from the saya.
 * Damage lands only inside a cut's window, along the blade's swept path ({@link BladePaths}). Sent by ordinal.
 */
public enum SwordMove {
    // --- the draws (from the saya) -------------------------------------------------------------------------------------------
    DRAW_CUT(Branch.GROUND, true, "Draw Cut", "Stillness, the hand on the hilt, then the blade leaves the saya straight into a cut across.",
        13, 9, Motion.lunge(3, 0.25), false, new Cut(4, 5, 7.0F, Weight.HEAVY, 0.5, 0.1, 10)),
    ADVANCING_DRAW(Branch.MOVING, true, "Dashing Draw", "Low and far forward, the blade drawn on the run into a rising cut.",
        14, 10, Motion.dash(2, 5, 0.6), false, new Cut(4, 6, 7.5F, Weight.HEAVY, 0.4, 0.35, 10)),
    AERIAL_DRAW(Branch.AIR, true, "Falling Draw", "In the air: drawn up and across as the body turns.",
        12, 8, Motion.hop(1, 0.15, 0.22), false, new Cut(3, 5, 6.5F, Weight.MEDIUM, 0.3, 0.3, 8)),

    // --- the ground string ---------------------------------------------------------------------------------------------------
    KESA(Branch.GROUND, false, "Kesa Cut", "From high over the right shoulder down through the left hip, the whole body behind it.",
        14, 9, Motion.lunge(4, 0.22), false, new Cut(5, 6, 6.0F, Weight.MEDIUM, 0.45, 0.05, 10)),
    GYAKU_KESA(Branch.GROUND, false, "Reverse Kesa", "From high over the left shoulder down through the right hip.",
        14, 9, Motion.lunge(4, 0.22), false, new Cut(5, 6, 6.0F, Weight.MEDIUM, 0.45, 0.05, 10)),
    YOKO(Branch.GROUND, false, "Horizontal Cut", "Right to left at the waist, the hips wound back and unwound through it.",
        13, 9, Motion.lunge(4, 0.18), false, new Cut(5, 6, 5.5F, Weight.MEDIUM, 0.55, 0.05, 8)),
    GYAKU_YOKO(Branch.GROUND, false, "Return Cut", "A backhand from the left hip across to the right.",
        13, 9, Motion.lunge(4, 0.18), false, new Cut(5, 6, 5.5F, Weight.MEDIUM, 0.55, 0.05, 8)),
    TSUKI(Branch.GROUND, false, "Thrust", "Drawn back to the hip and driven in with a long lunge, the left hand sighting along it.",
        14, 10, Motion.lunge(4, 0.5), false, new Cut(5, 7, 6.5F, Weight.HEAVY, 0.8, 0.1, 12)),
    KIRIAGE(Branch.GROUND, false, "Rising Cut", "From the left knee up past the right shoulder: they leave the ground.",
        14, 10, Motion.lunge(4, 0.15), false, new Cut(5, 6, 6.0F, Weight.HEAVY, 0.2, 0.75, 12)),
    MAKKO(Branch.GROUND, false, "Heaven Splitter", "The finisher: raised high behind the shoulder, then straight down through the centre "
        + "with a step in.", 17, 15, Motion.lunge(6, 0.35), true, new Cut(7, 8, 10.0F, Weight.FINISHER, 1.0, 0.15, 18)),

    // --- the moving string ---------------------------------------------------------------------------------------------------
    ADVANCING_CUT(Branch.MOVING, false, "Advancing Cut", "A long low step and a cut across at knee height at the end of it.",
        13, 9, Motion.dash(2, 5, 0.7), false, new Cut(5, 6, 6.5F, Weight.HEAVY, 0.6, 0.2, 12)),
    PIVOT_CUT(Branch.MOVING, false, "Pivot Cut", "A full turn on the front foot with the arm flung out flat: everything round is cut.",
        15, 12, Motion.NONE, true, new Cut(5, 10, 6.0F, Weight.FINISHER, 0.9, 0.2, 14)),

    // --- the aerial string ---------------------------------------------------------------------------------------------------
    AERIAL_CROSS(Branch.AIR, false, "Aerial Cross", "Two diagonals crossing in the air, each its own cut.",
        14, 11, Motion.hop(1, 0.1, 0.18), false, new Cut(3, 4, 4.5F, Weight.MEDIUM, 0.3, 0.2, 8),
        new Cut(8, 9, 5.0F, Weight.MEDIUM, 0.45, 0.25, 8)),
    AERIAL_WHEEL(Branch.AIR, false, "Wheel", "A forward somersault, the blade carried round a full vertical circle.",
        15, 11, Motion.hop(1, 0.25, 0.3), false, new Cut(4, 9, 6.0F, Weight.HEAVY, 0.5, 0.3, 10)),
    PLUNGE(Branch.AIR, false, "Plunge", "The aerial finisher: raised high, then driven point-down as the body drops onto it.",
        17, 15, Motion.leap(1, 6, 0.1, 0.25), true, new Cut(6, 9, 9.0F, Weight.FINISHER, 0.8, 0.45, 18));

    /** Where a string is thrown: standing, on the move, in the air (the player's movement picks it, {@link SwordCombo#branch}). */
    public enum Branch {
        GROUND, MOVING, AIR
    }

    /**
     * One cut of a move: the window of ticks its blade hurts in (inclusive: the blade swept from {@code from - 1} to {@code to}), its
     * damage (before the grade's edge and the mastery), its weight (the original's impact tiers), where it throws what it hits (along
     * the look and up, blocks a tick) and how long it stuns.
     */
    public record Cut(int from, int to, float damage, Weight weight, double back, double up, int stun) {
    }

    private final Branch branch;
    private final boolean draw;
    private final String display;
    private final String description;
    private final int duration;
    private final int cancel;
    private final Motion motion;
    private final boolean finisher;
    private final Cut[] cuts;

    SwordMove(Branch branch, boolean draw, String display, String description, int duration, int cancel, Motion motion, boolean finisher,
              Cut... cuts) {
        this.branch = branch;
        this.draw = draw;
        this.display = display;
        this.description = description;
        this.duration = duration;
        this.cancel = cancel;
        this.motion = motion;
        this.finisher = finisher;
        this.cuts = cuts;
    }

    public Branch branch() {
        return this.branch;
    }

    /** Opens from the saya: the hand finds the hilt and the blade leaves the saya inside the move. */
    public boolean draw() {
        return this.draw;
    }

    public String display() {
        return this.display;
    }

    public String description() {
        return this.description;
    }

    public int duration() {
        return this.duration;
    }

    public int cancel() {
        return this.cancel;
    }

    public Motion motion() {
        return this.motion;
    }

    public boolean finisher() {
        return this.finisher;
    }

    public Cut[] cuts() {
        return this.cuts.clone();
    }

    /** The animation every client plays: {@code assets/hexhaki/player_animation/sword_<name>.json}. */
    public String animation() {
        return "sword_" + this.name().toLowerCase(Locale.ROOT);
    }

    public static SwordMove byOrdinal(int ordinal) {
        SwordMove[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : null;
    }

    /** The cut whose window holds {@code tick}, or -1. */
    public int cutAt(int tick) {
        for (int i = 0; i < this.cuts.length; i++) {
            if (tick >= this.cuts[i].from() && tick <= this.cuts[i].to()) {
                return i;
            }
        }
        return -1;
    }
}
