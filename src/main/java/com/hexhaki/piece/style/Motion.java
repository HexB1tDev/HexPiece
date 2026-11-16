package com.hexhaki.piece.style;

/**
 * How a style move carries its user (pure): a lunge (one push along the look on {@code tick}), a dash (held at {@code forward} blocks a
 * tick from {@code tick} to {@code until}), a leap (up and forward on {@code tick}, driven down hard on {@code until}).
 */
public record Motion(Kind kind, int tick, int until, double forward, double up) {
    public enum Kind {
        NONE, LUNGE, DASH, LEAP
    }

    public static final Motion NONE = new Motion(Kind.NONE, 0, 0, 0, 0);

    public static Motion lunge(int tick, double forward) {
        return new Motion(Kind.LUNGE, tick, tick, forward, 0.0);
    }

    public static Motion hop(int tick, double forward, double up) {
        return new Motion(Kind.LUNGE, tick, tick, forward, up);
    }

    public static Motion dash(int tick, int until, double forward) {
        return new Motion(Kind.DASH, tick, until, forward, 0.0);
    }

    public static Motion leap(int tick, int slam, double forward, double up) {
        return new Motion(Kind.LEAP, tick, slam, forward, up);
    }
}
