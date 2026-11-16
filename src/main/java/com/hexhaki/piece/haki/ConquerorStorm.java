package com.hexhaki.piece.haki;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * 2.1.7: the Conqueror's full-power release as a storm (pure, tested in {@code LightningTests}). The owner: "the haki full power
 * explosion should be ridiculously bigger than what it currently is, lightning spreading further etc".
 *
 * <p>On top of the original's own release (its range lightning, burst and dome, which stay), a release at full charge tears the ground
 * open with lightning out to {@link #reach} (60 blocks, up to 120 at full mastery, further than the original's 50 at the very most):
 * <ul>
 *   <li><b>the web</b>: a few huge bolts (5 to 8) racing out along the ground from the player at {@link #SPEED} blocks a tick,
 *       wandering, now and then branching, thick and white-hot at the root, thinning far out (the owner, after the first try: "too
 *       much lightning way too much": few and far, not a carpet);</li>
 *   <li><b>the sky</b>: a few bolts (3 to 6) falling out of the sky onto the field while the web spreads;</li>
 *   <li><b>the crown</b>: three arches of lightning leaping off the player's shoulders, over and down to the ground;</li>
 *   <li><b>the shockwave</b>: an inked ring sweeping out to the reach ({@code client/haki/HakiBolts} draws it from {@link #reach}).</li>
 * </ul>
 * Every piece is laid as the original lays its lightning (a black body, a red core, near the player a hot heart), through
 * {@link BoltPainter}, so it crackles, re-strikes and dances like the rest. Every point is given as a height above the ground under it:
 * the client lays the storm on the real terrain when it lets it go.
 */
public final class ConquerorStorm {
    public static final double SPEED = 12.0;
    public static final int BODY = 0xEC050307;
    public static final int CORE = 0xFFFF1433;
    public static final int HEART = 0xFFFF7889;
    /** The crackle's levels for a storm's pieces (16 kinks to a five-block piece). */
    public static final int LEVELS = 4;
    static final double STEP = 5.0;

    /** One beam of the storm: from {@code a} to {@code b} (x, height above the ground, z; blocks from the player's feet). */
    public record Call(double[] a, double[] b, float width, int color, int duration, float delay) {
    }

    private ConquerorStorm() {
    }

    /** How far the storm reaches at this Conqueror mastery (0..1000). */
    public static double reach(int mastery) {
        return 60.0 + 60.0 * Math.max(0.0, Math.min(1.0, mastery / 1000.0));
    }

    /** The whole storm of one full-power release. */
    public static List<Call> release(int mastery, long seed, boolean supreme) {
        double m = Math.max(0.0, Math.min(1.0, mastery / 1000.0));
        double reach = reach(mastery);
        SplittableRandom random = new SplittableRandom(seed ^ 0x5354_4F52_4DL);
        List<Call> out = new ArrayList<>();
        // the owner, on the first try: "too much lightning way too much": a few huge bolts reaching far, not a carpet of them
        int mains = 5 + (int) Math.round(2 * m) + (supreme ? 1 : 0);
        web(out, random, mains, reach, 1.0, 0.0F, true);
        int skies = 3 + (int) Math.round(2 * m) + (supreme ? 1 : 0);
        for (int i = 0; i < skies; i++) {
            sky(out, random, reach, (float) (2.0 + random.nextDouble() * 12.0));
        }
        int arches = 3;
        double turn = random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < arches; i++) {
            arch(out, random, turn + Math.PI * 2.0 * i / arches + random.nextDouble(-0.3, 0.3), (float) (random.nextDouble() * 2.0));
        }
        return out;
    }

    /** {@code count} bolts out along the ground from the player, {@code scale} of the first wave's thickness, from {@code delay}. */
    static void web(List<Call> out, SplittableRandom random, int count, double reach, double scale, float delay, boolean heart) {
        double start = random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < count; i++) {
            double yaw = start + Math.PI * 2.0 * i / count + random.nextDouble(-0.35, 0.35) * Math.PI * 2.0 / count;
            double length = reach * (0.7 + 0.3 * random.nextDouble());
            double[] root = {Math.cos(yaw) * 0.9, 1.0, Math.sin(yaw) * 0.9};
            branch(out, random, root, yaw, length, 0.0, length, scale, delay, heart, 1);
        }
    }

    /**
     * A wandering bolt along the ground from {@code from} heading {@code yaw}, {@code length} blocks; {@code along} blocks of its trunk
     * already behind it (its thickness and its strike time run on from there); it sheds branches while {@code depth} allows.
     */
    static void branch(List<Call> out, SplittableRandom random, double[] from, double yaw, double length, double along, double trunk, double scale,
                       float delay, boolean heart, int depth) {
        double[] a = from;
        double heading = yaw;
        double walked = 0.0;
        while (walked < length - 0.5) {
            double step = Math.min(length - walked, STEP * (0.7 + 0.6 * random.nextDouble()));
            heading += random.nextDouble(-0.32, 0.32);
            double height = 0.15 + 0.7 * random.nextDouble() * random.nextDouble();
            double[] b = {a[0] + Math.cos(heading) * step, height, a[2] + Math.sin(heading) * step};
            double t = Math.min(1.0, (along + walked) / trunk);
            float at = delay + (float) ((along + walked) / SPEED);
            float body = (float) (scale * (0.46 - 0.36 * t));
            float core = (float) (scale * (0.15 - 0.115 * t));
            int life = 20 + random.nextInt(8);
            if (t < 0.5) {
                // far out the core's own ink is the outline: the black body only where the bolt is thick
                out.add(new Call(a, b, body, BODY, life, at));
            }
            out.add(new Call(a, b, core, CORE, life, at));
            if (heart && t < 0.3) {
                out.add(new Call(a, b, core * 0.3F, HEART, life - 2, at));
            }
            if (depth > 0 && walked > 8.0 && random.nextDouble() < 0.12) {
                // now and then one branch off a trunk, never a branch off a branch
                double side = random.nextBoolean() ? 1 : -1;
                double off = heading + side * random.nextDouble(0.45, 1.0);
                double rest = length - walked;
                branch(out, random, b, off, Math.max(6.0, rest * random.nextDouble(0.3, 0.55)), along + walked + step, trunk, scale * 0.6, delay,
                    false, depth - 1);
            }
            walked += step;
            a = b;
        }
    }

    /** A bolt falling out of the sky onto the field at {@code delay}. */
    static void sky(List<Call> out, SplittableRandom random, double reach, float delay) {
        double r = Math.sqrt(random.nextDouble()) * reach * 0.75 + 6.0;
        double yaw = random.nextDouble() * Math.PI * 2.0;
        double[] ground = {Math.cos(yaw) * r, 0.1, Math.sin(yaw) * r};
        double top = 38.0 + random.nextDouble() * 22.0;
        double[] a = {ground[0] + random.nextDouble(-8, 8), top, ground[2] + random.nextDouble(-8, 8)};
        int pieces = 6 + random.nextInt(3);
        int life = 10 + random.nextInt(5);
        for (int p = 0; p < pieces; p++) {
            double t = (p + 1) / (double) pieces;
            double[] b = p == pieces - 1 ? ground : new double[] {a[0] + (ground[0] - a[0]) / (pieces - p) + random.nextDouble(-2.5, 2.5),
                top * (1 - t), a[2] + (ground[2] - a[2]) / (pieces - p) + random.nextDouble(-2.5, 2.5)};
            float at = delay + (float) (p * 0.25);
            float w = (float) (0.55 - 0.2 * t);
            out.add(new Call(a, b, w * 0.42F, BODY, life, at));
            out.add(new Call(a, b, w * 0.24F, CORE, life, at));
            out.add(new Call(a, b, w * 0.08F, HEART, life - 2, at));
            a = b;
        }
    }

    /** An arch leaping off the player's shoulders up and over to the ground, heading {@code yaw}. */
    static void arch(List<Call> out, SplittableRandom random, double yaw, float delay) {
        double land = 14.0 + random.nextDouble() * 12.0;
        double rise = 8.0 + random.nextDouble() * 8.0;
        int pieces = 9;
        double[] a = {Math.cos(yaw) * 0.5, 1.6, Math.sin(yaw) * 0.5};
        double bend = random.nextDouble(-0.25, 0.25);
        for (int p = 1; p <= pieces; p++) {
            double t = p / (double) pieces;
            double d = 0.5 + (land - 0.5) * t;
            double h = 1.6 * (1 - t) + 4.0 * rise * t * (1 - t);
            double heading = yaw + bend * t;
            double[] b = {Math.cos(heading) * d, p == pieces ? 0.1 : h, Math.sin(heading) * d};
            float at = delay + (float) (p * 0.35);
            out.add(new Call(a, b, 0.3F, BODY, 18, at));
            out.add(new Call(a, b, 0.1F, CORE, 18, at));
            out.add(new Call(a, b, 0.03F, HEART, 16, at));
            a = b;
        }
    }
}
