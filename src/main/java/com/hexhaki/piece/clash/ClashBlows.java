package com.hexhaki.piece.clash;

import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Hit;
import com.hexhaki.piece.style.StyleMove;
import com.hexhaki.piece.sword.BladePaths;
import com.hexhaki.piece.sword.SwordMove;

/**
 * 2.3.6: which blow a move has on its way at each of its ticks (pure, tested in {@code ClashTests}), for {@code server/Clashes}. A
 * style move's blows are its {@link Hit}s: each sets out the tick after the one before it landed (the first at the move's start) and
 * lands on its own tick; a shot (a water bullet, an air blade) flies the instant it is fired, so it is on its way only then. A sword
 * move's blows are its cuts: each sets out after the one before and is on its way to the end of its window. The charged cut and the
 * quick draw are one blow each. How far a cut reaches is read off the blade's own swept path ({@link BladePaths}), the same path that
 * hurts.
 */
public final class ClashBlows {
    /** The charged cut (not the Haki slash, which flies off): its blade hurts from tick 1 to 3. */
    public static final int RELEASE_LANDS = 3;
    /** The quick draw: its blade cuts along the run from tick 3 to 8. */
    public static final int QUICK_LANDS = 8;
    /** How far the quick draw's run and flung blade reach (blocks: the dash still to go and the blade past it). */
    public static final double QUICK_REACH = 4.5;

    private ClashBlows() {
    }

    /**
     * A blow's window in its move's own ticks: on its way from {@code began} to {@code lands}, how far it reaches (blocks), how hard
     * (damage before mastery and edge), what it strikes with, and whether it is a shot.
     */
    public record Window(int began, int lands, double reach, double hit, ClashRules.Kind kind, boolean shot) {
        /** This window as a blow in the world, its move having begun at game tick {@code start}. */
        public ClashRules.Blow at(long start, double hit, boolean armed, int ryou) {
            return new ClashRules.Blow(this.kind, start + this.began, start + this.lands, this.reach, hit, armed, ryou);
        }
    }

    /** What a style throws its blows with: Black Leg kicks, the rest strike with the hands (Six Powers' legs cut like a blade's air). */
    public static ClashRules.Kind kind(StyleMove move) {
        return move.style() == FightingStyle.BLACK_LEG || move == StyleMove.SP_STORM || move == StyleMove.SP_BLADE ? ClashRules.Kind.KICK
            : ClashRules.Kind.FIST;
    }

    /** The blow {@code move} has on its way {@code tick} ticks in (the next one to land, or one landed a tick ago), or null. */
    public static Window style(StyleMove move, int tick) {
        if (move == null) {
            return null;
        }
        int previous = -1;
        for (Hit hit : move.hits()) {
            if (tick <= hit.tick() + ClashRules.LATE) {
                int began = hit.line() ? hit.tick() : previous + 1;
                return new Window(began, hit.tick(), hit.reach(), hit.damage(), kind(move), hit.line());
            }
            previous = hit.tick();
        }
        return null;
    }

    /** The cut {@code move} has on its way {@code tick} ticks in, reaching {@code reach} blocks, or null. */
    public static Window sword(SwordMove move, int tick, double reach) {
        if (move == null) {
            return null;
        }
        int previous = -1;
        for (SwordMove.Cut cut : move.cuts()) {
            if (tick <= cut.to() + ClashRules.LATE) {
                return new Window(previous + 1, cut.to(), reach, cut.damage(), ClashRules.Kind.BLADE, false);
            }
            previous = cut.to();
        }
        return null;
    }

    /** The charged cut: one blow from the release to its last cutting tick. */
    public static Window release(int tick, double reach, double hit) {
        return tick <= RELEASE_LANDS + ClashRules.LATE ? new Window(0, RELEASE_LANDS, reach, hit, ClashRules.Kind.BLADE, false) : null;
    }

    /** The quick draw: one blow from the press to the end of the run. */
    public static Window quick(int tick, double extra, double hit) {
        return tick <= QUICK_LANDS + ClashRules.LATE ? new Window(0, QUICK_LANDS, QUICK_REACH + extra, hit, ClashRules.Kind.BLADE, false)
            : null;
    }

    /**
     * How far a cut of {@code animation} reaches across the ground (blocks from the body's middle to the farthest the blade's tip goes
     * between ticks {@code from} and {@code to}, and {@code extend} past it, as the sweep cuts), or {@code fallback} for an animation
     * with no path.
     */
    public static double bladeReach(String animation, int from, int to, double extend, double fallback) {
        float[] path = animation == null ? null : BladePaths.of(animation);
        if (path == null) {
            return fallback;
        }
        int samples = path.length / 6;
        double most = 0.0;
        for (int h = Math.max(0, from * 2); h <= Math.min(samples - 1, to * 2); h++) {
            double bx = path[h * 6];
            double bz = path[h * 6 + 2];
            double tx = path[h * 6 + 3];
            double tz = path[h * 6 + 5];
            double base = Math.hypot(bx, bz);
            double tip = Math.hypot(tx, tz);
            double len = Math.sqrt((tx - bx) * (tx - bx) + (path[h * 6 + 4] - path[h * 6 + 1]) * (path[h * 6 + 4] - path[h * 6 + 1])
                + (tz - bz) * (tz - bz));
            // the sweep reaches extend past the tip along the blade: as far across as the tip goes, and that much more of the line
            double out = len < 1.0E-6 ? tip : tip + extend * Math.max(0.0, (tip - base) / len);
            most = Math.max(most, out);
        }
        return most > 0.0 ? most : fallback;
    }
}
