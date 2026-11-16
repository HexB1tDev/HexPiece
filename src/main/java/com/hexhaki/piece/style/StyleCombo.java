package com.hexhaki.piece.style;

/**
 * 2.1.1: how M1 presses chain into a style's four-blow string (pure, tested). A press during a blow's recovery (from its {@code cancel}
 * tick, or {@link #BUFFER} ticks before it, held until then) throws the next blow as soon as it may; a press within {@link #WINDOW} ticks
 * after a blow ends continues the string; later, it starts over. After the finisher the string rests {@link #REST} ticks.
 */
public final class StyleCombo {
    public static final int BUFFER = 3;
    public static final int WINDOW = 12;
    public static final int REST = 6;

    private StyleCombo() {
    }

    /**
     * The tick the next blow may start at for a press at {@code now}, or -1 if the press is dropped. {@code last}: the string blow
     * thrown last (null if none), {@code started}: when it started.
     */
    public static long startFor(StyleMove last, long started, long now) {
        if (last == null || !last.slot().string()) {
            return now;
        }
        long cancelAt = started + last.cancel();
        if (last.slot() == Slot.M1_4) {
            cancelAt = started + last.duration() + REST;
        }
        if (now >= cancelAt) {
            return now;
        }
        return now >= cancelAt - BUFFER ? cancelAt : -1;
    }

    /** The slot the next string blow comes from. */
    public static Slot next(StyleMove last, long started, long at) {
        if (last == null || !last.slot().string() || last.slot() == Slot.M1_4) {
            return Slot.M1_1;
        }
        long end = started + last.duration();
        return at <= end + WINDOW ? StyleMove.nextInString(last.slot()) : Slot.M1_1;
    }
}
