package com.hexhaki.piece.clash;

/**
 * 3.8.4 (the owner: "I want a proper clashing system like our other mod has HexGhoul"): how much a fighter carries into a clash, as
 * HexGhoul's ClashPower measures it (pure, tested in ClashTests). A clash threw the weaker blow further by the gap in the two blows'
 * damage alone ({@link ClashRules#ratio}): a fresh player's heavy move out-weighed a master's jab. Now both stand on one scale, HexGhoul's:
 * an ordinary blow ({@link #BLOW}) times the fighter's power, times the root of how heavy this blow is next to an ordinary one (a
 * finisher, a fruit's heavy blow: it counts, but less than who swings it). A player's power runs from a fresh fighter's ({@link #LOW})
 * to a master's ({@link #HIGH}) with how far they have come ({@link #progress}: the mastery of what they strike with (the style, the
 * sword, the fruit), their Armament and their Conqueror's Haki), times {@link #ARMED} with Armament on. A sea fighter's by its rank's
 * mastery. Against a much weaker one, the strong stands its ground and the weak is the one sent flying.
 */
public final class ClashPower {
    /** An ordinary blow's damage (a style's jab at no mastery and a little over). */
    public static final double BLOW = 5.0;
    /** How the three count toward a player's progress (they add to 1). */
    public static final double DISCIPLINE_SHARE = 0.5;
    public static final double ARMAMENT_SHARE = 0.3;
    public static final double CONQUEROR_SHARE = 0.2;
    /** A fresh fighter's power and a master's (times an ordinary blow): at the top three times a beginner, so a master stands its ground. */
    public static final float LOW = 1.0F;
    public static final float HIGH = 3.0F;
    /** What Armament on adds (times): Haki-coated fists and blades hit harder in a clash. */
    public static final double ARMED = 1.25;

    private ClashPower() {
    }

    /** How far a fighter has come, 0 to 1: the mastery of what strikes, their Armament's, their Conqueror's (each of 1000). */
    public static double progress(int discipline, int armament, int conqueror) {
        return clamp(DISCIPLINE_SHARE * clamp(discipline / 1000.0) + ARMAMENT_SHARE * clamp(armament / 1000.0)
            + CONQUEROR_SHARE * clamp(conqueror / 1000.0));
    }

    /** A fighter's power at {@code progress}: {@link #LOW} at 0 to {@link #HIGH} at 1. */
    public static float power(double progress) {
        return (float) (LOW + (HIGH - LOW) * clamp(progress));
    }

    /** How much a blow of {@code hit} damage counts next to an ordinary one: the root of its weight (0.55 to 1.7). */
    public static double heft(double hit) {
        return Math.sqrt(Math.max(0.3, Math.min(3.0, Math.max(0.0, hit) / BLOW)));
    }

    /** A blow in a clash: an ordinary blow times the fighter's power at {@code progress}, its heft, and Armament ({@code armed}). */
    public static double blow(double progress, double hit, boolean armed) {
        return BLOW * power(progress) * heft(hit) * (armed ? ARMED : 1.0);
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
