package com.hexhaki.piece.sword;

import com.hexhaki.piece.sword.SwordMove.Branch;

/**
 * 2.1.4: how M1 presses chain into the Swordsman's strings (pure, tested; the server decides, the client only asks). The owner:
 * "Connect these motions into a flowing grounded combo with movement and aerial branches using the existing movement inputs. Choose
 * branches predictably from the player's movement state, and keep combo progression server-authoritative. Clearly define the
 * continuation window and reset the sequence after it expires ... Each M1 press should request one attack, with a small input buffer
 * for the next legal combo step. Do not let repeated packets bypass recovery or generate unlimited queued attacks."
 * <ul>
 *   <li>The branch is the player's state at the press: in the air, on the move (faster than {@link #MOVING} blocks a tick), or
 *       standing ({@link #branch}).</li>
 *   <li>Sheathed, the first press is its branch's draw ({@link #DRAWS}); drawn, the next cut of the string.</li>
 *   <li>A press within {@link #WINDOW} ticks of the last cut's end, in the same branch, continues its string (after the last cut it
 *       starts the string again); otherwise the string starts over. A different branch starts its own string.</li>
 *   <li>During a cut, a press from {@link #BUFFER} ticks before its cancel tick is kept (one, never more) and thrown at the cancel
 *       tick; an earlier press is dropped.</li>
 * </ul>
 */
public final class SwordCombo {
    public static final int BUFFER = 4;
    public static final int WINDOW = 12;
    /** Horizontal speed (blocks a tick) over which a player is on the move (walking is about 0.21, sneaking 0.065). */
    public static final double MOVING = 0.12;
    private static final SwordMove[] GROUND = {SwordMove.KESA, SwordMove.GYAKU_KESA, SwordMove.YOKO, SwordMove.GYAKU_YOKO, SwordMove.TSUKI,
        SwordMove.KIRIAGE, SwordMove.MAKKO};
    private static final SwordMove[] MOVE = {SwordMove.ADVANCING_CUT, SwordMove.PIVOT_CUT};
    private static final SwordMove[] AIR = {SwordMove.AERIAL_CROSS, SwordMove.AERIAL_WHEEL, SwordMove.PLUNGE};
    /** Each branch's draw, by Branch ordinal. */
    public static final SwordMove[] DRAWS = {SwordMove.DRAW_CUT, SwordMove.ADVANCING_DRAW, SwordMove.AERIAL_DRAW};

    private SwordCombo() {
    }

    public static Branch branch(boolean onGround, double horizontalSpeed) {
        if (!onGround) {
            return Branch.AIR;
        }
        return horizontalSpeed > MOVING ? Branch.MOVING : Branch.GROUND;
    }

    /** A branch's string of drawn cuts. */
    public static SwordMove[] string(Branch branch) {
        return switch (branch) {
            case GROUND -> GROUND.clone();
            case MOVING -> MOVE.clone();
            case AIR -> AIR.clone();
        };
    }

    /**
     * The cut a press starts. {@code sheathed}: the sword is in its saya; {@code last}: the cut before (null if none), {@code lastEnd}:
     * the tick it ended (or ends).
     */
    public static SwordMove next(Branch branch, boolean sheathed, SwordMove last, long lastEnd, long now) {
        if (sheathed) {
            return DRAWS[branch.ordinal()];
        }
        SwordMove[] string = string(branch);
        if (last == null || last.branch() != branch || now > lastEnd + WINDOW) {
            return string[0];
        }
        for (int i = 0; i < string.length; i++) {
            if (string[i] == last) {
                return string[(i + 1) % string.length];
            }
        }
        return string[0]; // after the branch's draw: its first cut
    }

    /**
     * What a press during a cut does: the tick the next cut may start (its cancel tick) if the press is kept, or -1 if it is dropped
     * (too early). {@code started}: when the cut in hand began.
     */
    public static long buffered(SwordMove current, long started, long now) {
        long cancelAt = started + current.cancel();
        if (now >= cancelAt) {
            return now;
        }
        return now >= cancelAt - BUFFER ? cancelAt : -1;
    }
}
