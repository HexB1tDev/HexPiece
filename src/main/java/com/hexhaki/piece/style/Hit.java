package com.hexhaki.piece.style;

/**
 * One blow of a style move (pure): on which tick of the move it lands, how far it reaches and how wide its arc is (degrees each side of
 * the look; 180 is all round) or, for a {@link #line} (a flicked water bullet, an air blade), how far it flies and how thick it is
 * ({@code arc} is then its radius in blocks); how hard it hits (damage before mastery), where it throws what it hits (back along the look
 * and up, blocks a tick), how long it stuns, and how it feels ({@link Weight}: the original's impact tiers, hit-stop, shake, flash).
 */
public record Hit(int tick, double reach, double arc, float damage, double back, double up, int stun, Weight weight, boolean line) {
    public enum Weight {
        LIGHT, MEDIUM, HEAVY, FINISHER
    }

    public static Hit light(int tick, double reach, float damage) {
        return new Hit(tick, reach, 50, damage, 0.25, 0.05, 6, Weight.LIGHT, false);
    }

    public static Hit medium(int tick, double reach, float damage) {
        return new Hit(tick, reach, 55, damage, 0.45, 0.1, 10, Weight.MEDIUM, false);
    }

    public static Hit heavy(int tick, double reach, float damage) {
        return new Hit(tick, reach, 55, damage, 0.9, 0.2, 14, Weight.HEAVY, false);
    }

    public static Hit finisher(int tick, double reach, float damage) {
        return new Hit(tick, reach, 50, damage, 1.2, 0.25, 18, Weight.FINISHER, false);
    }

    /** A shot along the look: {@code range} blocks long, {@code radius} thick. */
    public static Hit line(int tick, double range, double radius, float damage, Weight weight) {
        return new Hit(tick, range, radius, damage, 0.4, 0.1, 8, weight, true);
    }

    public Hit wide(double degrees) {
        return new Hit(this.tick, this.reach, degrees, this.damage, this.back, this.up, this.stun, this.weight, this.line);
    }

    public Hit throwing(double back, double up) {
        return new Hit(this.tick, this.reach, this.arc, this.damage, back, up, this.stun, this.weight, this.line);
    }

    public Hit stunning(int ticks) {
        return new Hit(this.tick, this.reach, this.arc, this.damage, this.back, this.up, ticks, this.weight, this.line);
    }

    public Hit at(int tick) {
        return new Hit(tick, this.reach, this.arc, this.damage, this.back, this.up, this.stun, this.weight, this.line);
    }
}
