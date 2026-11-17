package com.hexhaki.piece.client.sword;

import com.hexhaki.piece.sword.Carcass;
import java.util.ArrayList;
import java.util.List;

/**
 * 2.3.2: the finisher's line on its victim's own view (pure: the rectangles {@link FinisherFx#screen} fills, so the preview draws the
 * very same): a vertical line of the cut's red down the middle of the view, {@code amount} of the way drawn out from the middle up and
 * down (0 to 1, as the line runs round the body), a soft glow two bands wide either side, a white-hot core; the two running ends
 * burn white over a few pixels, a spark across each; it flickers a little, as the line on the body does. {@code fade} (1 to 0) fades it all.
 */
public final class ScreenLine {
    private ScreenLine() {
    }

    /** [x0, y0, x1, y1, argb] rectangles, back to front. */
    public static List<int[]> bands(int width, int height, double amount, double fade, double time) {
        List<int[]> out = new ArrayList<>();
        if (amount <= 0.0 || fade <= 0.0) {
            return out;
        }
        int x = width / 2;
        int mid = height / 2;
        int reach = (int) Math.round(height * 0.5 * Math.min(1.0, amount * 1.08)) + (amount >= 1.0 ? 2 : 0);
        double flicker = 0.88 + 0.12 * Math.sin(time * 2.7) * Math.sin(time * 1.3 + 0.7);
        int red = Carcass.GLOW & 0xFFFFFF;
        // the glow, widest and faintest outside
        int glowWide = Math.max(4, width / 160);
        out.add(new int[] {x - glowWide * 3, mid - reach, x + glowWide * 3, mid + reach, argb(0.10 * fade * flicker, red)});
        out.add(new int[] {x - glowWide * 2, mid - reach, x + glowWide * 2, mid + reach, argb(0.16 * fade * flicker, red)});
        out.add(new int[] {x - glowWide, mid - reach, x + glowWide, mid + reach, argb(0.30 * fade * flicker, red)});
        // the line itself, and its white-hot core
        out.add(new int[] {x - 2, mid - reach, x + 2, mid + reach, argb(fade, red)});
        out.add(new int[] {x - 1, mid - reach, x + 1, mid + reach, argb(0.85 * fade, 0xFFE6E6)});
        if (amount < 1.0) {
            // the two running ends: white over a few pixels, a flare round them
            int tip = Math.max(6, height / 40);
            for (int end : new int[] {mid - reach, mid + reach}) {
                int a = Math.min(end, end + (end < mid ? tip : -tip));
                int b = Math.max(end, end + (end < mid ? tip : -tip));
                int back = end < mid ? 1 : -1;
                // a hotter, wider stretch behind the end, tapering into it, and a thin spark across it
                out.add(new int[] {x - 3, Math.min(end, end + back * tip * 3), x + 3, Math.max(end, end + back * tip * 3),
                    argb(0.55 * fade, 0xFF5A5A)});
                out.add(new int[] {x - 2, a, x + 2, b, argb(fade, 0xFFFFFF)});
                out.add(new int[] {x - glowWide * 2, end - 1, x + glowWide * 2, end + 1, argb(0.55 * fade, 0xFFE0E0)});
                out.add(new int[] {x - glowWide, end - 2, x + glowWide, end + 2, argb(0.35 * fade, 0xFFFFFF)});
            }
        }
        return out;
    }

    /** 2.3.2: how long the view's two halves take to come apart (ticks), how far apart they end (share of the width), how far they drop. */
    static final double OPEN = 14.0;
    static final double APART = 0.075;
    static final double DROP = 0.05;

    /**
     * Where each half of the split view is {@code age} ticks after the split ({@link ScreenSplit}), in shares of the screen: {left dx,
     * left dy, left turn (degrees), right dx, right dy, right turn}. They burst apart fast and ease out, then keep sliding a little and
     * settle lower, turned away from each other.
     */
    public static double[] split(double age) {
        double u = Math.max(0.0, Math.min(1.0, age / OPEN));
        double open = 1.0 - Math.pow(1.0 - u, 3.0);
        double drift = Math.max(0.0, age - OPEN) * 0.0006;
        double apart = APART * open + drift;
        double drop = DROP * open * open + drift * 0.5;
        return new double[] {-apart, drop, -3.2 * open, apart, drop * 0.8, 2.6 * open};
    }

    static int argb(double alpha, int rgb) {
        int a = (int) Math.round(255.0 * Math.max(0.0, Math.min(1.0, alpha)));
        return a << 24 | rgb & 0xFFFFFF;
    }
}
