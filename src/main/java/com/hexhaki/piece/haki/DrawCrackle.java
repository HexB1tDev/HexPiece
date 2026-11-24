package com.hexhaki.piece.haki;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * 2.1.5: Haki lightning crackling off a katana (pure; {@code client/sword/HakiDraw} draws it in the blade's own frame, the preview draws
 * it the same way). The owner's idea: "if you have Haki activated ... when you unsheath the blade, like lightning comes out of it like
 * slowly when you're unsheathing it ... Rayleigh does against Blackbeard, like the way he pulls out the sword and like lightning comes
 * out".
 *
 * <p>The frame is the blade's: its back runs up the y axis from the guard (y 0) to the tip, blocks. A <i>set</i> of bolts is struck
 * every {@link #WINDOW} ticks and burns for up to {@link #LIFE}, so the crackle never holds still: short arcs that leap along the bared
 * steel between two points of it, bowing off the blade, and (while it leaves the saya) sparks spitting out of the saya's mouth. Each
 * bolt is the original's three strokes: a black body, a coloured core and a pale hot heart, so it is the same inked lightning as the
 * Conqueror's.
 */
public final class DrawCrackle {
    public static final int WINDOW = 2;
    public static final int LIFE = 4;
    static final long SALT = 0x4452_4157L;

    private DrawCrackle() {
    }

    /**
     * One set, struck at the window {@code window}: arcs along the blade between {@code from} and {@code to} (y, blocks) and, if
     * {@code mouth} is not NaN, sparks out of the saya's mouth at that y. {@code strength} 0..2 sets how many and how big;
     * {@code color} is the core's.
     */
    public static List<Bolt> set(long seed, long window, double from, double to, double mouth, double strength, int color) {
        List<Bolt> out = new ArrayList<>();
        if (strength <= 0.0 || to - from < 0.02 && Double.isNaN(mouth)) {
            return out;
        }
        SplittableRandom random = new SplittableRandom(seed * 0x9E3779B97F4A7C15L ^ window * 0xC2B2AE3D27D4EB4FL ^ SALT);
        int heart = BoltMesh.mix(color, 0xFFFFFFFF, 0.62);
        double size = 0.7 + 0.3 * Math.min(1.5, strength);
        int arcs = to - from < 0.02 ? 0 : (int) Math.round(1 + 2.2 * strength * random.nextDouble() + 1.0 * strength);
        for (int i = 0; i < arcs; i++) {
            double length = (0.12 + 0.3 * random.nextDouble()) * size;
            double y1 = from + random.nextDouble() * (to - from);
            double y2 = Math.max(from, Math.min(to, y1 + (random.nextBoolean() ? length : -length)));
            if (Math.abs(y2 - y1) < 0.05) {
                y2 = Math.min(to, y1 + 0.08);
            }
            double angle = random.nextDouble() * Math.PI * 2.0;
            double bow = (0.035 + 0.08 * random.nextDouble()) * size;
            // on the blade (its back and its flat, a little off the steel), bowed off it in the middle
            double[] a = {Math.cos(angle) * 0.02, y1, Math.sin(angle) * 0.012};
            double[] c = {Math.cos(angle) * 0.02, y2, Math.sin(angle) * 0.012};
            double[] mid = {Math.cos(angle) * (0.02 + bow), (y1 + y2) / 2.0, Math.sin(angle) * (0.012 + bow)};
            float delay = (float) (random.nextDouble() * 1.2);
            int life = 2 + random.nextInt(LIFE - 1);
            stroke(out, a, mid, c, 0.017F * (float) size, color, heart, delay, life, random.nextLong());
        }
        if (!Double.isNaN(mouth)) {
            int sparks = (int) Math.round(1 + 2.2 * strength * random.nextDouble() + 0.5 * strength);
            for (int i = 0; i < sparks; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                double reach = (0.14 + 0.32 * random.nextDouble()) * size;
                double tilt = random.nextDouble(-0.35, 0.6);
                double[] a = {Math.cos(angle) * 0.03, mouth, Math.sin(angle) * 0.03};
                double[] c = {Math.cos(angle) * reach, mouth + tilt * reach, Math.sin(angle) * reach};
                double[] mid = {(a[0] + c[0]) / 2 + Math.cos(angle + 1.4) * reach * 0.18, (a[1] + c[1]) / 2,
                    (a[2] + c[2]) / 2 + Math.sin(angle + 1.4) * reach * 0.18};
                float delay = (float) (random.nextDouble() * 1.0);
                int life = 2 + random.nextInt(LIFE - 1);
                stroke(out, a, mid, c, 0.022F * (float) size, color, heart, delay, life, random.nextLong());
            }
        }
        BoltPainter.point(out);
        return out;
    }

    /** One two-piece bolt (A, the bend, B) as the original's three strokes, the second piece struck as the first lands. */
    static void stroke(List<Bolt> out, double[] a, double[] mid, double[] b, float width, int color, int heart, float delay, int life,
                       long salt) {
        BoltPainter.Chain chain = new BoltPainter.Chain();
        double[][] pieces = {a, mid, b};
        for (int p = 0; p < 2; p++) {
            float start = Math.max(delay, chain.start(pieces[p], 0));
            float w = width * (p == 0 ? 1.0F : 0.8F);
            // 3.6.0 (the owner: "only 1 type and style of haki  model should be in use, THE CONQUEROR HAKI RELEASE"): the release's
            // model (its black body and rim, its crooked runs: ConquerorStorm.strike), as wide as the old glow
            out.addAll(ConquerorStorm.strike(pieces[p], pieces[p + 1], w * 2.6F, 0.6F, color & 0xFFFFFF, start, life, 0.0F,
                salt ^ p * 0x9E3779B97F4A7C15L, false));
            chain.land(pieces[p], pieces[p + 1], start, life);
        }
    }
}
