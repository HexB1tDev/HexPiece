package com.hexhaki.piece.haki;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * 2.9.1 (the owner: "Once you unlock it, Conqueror's haki, your, like, even your Rio, every move, like, should be coating you with
 * lightning. Like, lightning should be shooting out from your body like crazy. And I don't mean too much, but like often lightning is
 * sparking out of you. Whenever you grab a sword, lightning explodes from it"): the Conqueror's lightning sparking off its user (pure;
 * client/haki/ConquerorSparksClient lets it go through client/haki/HakiBolts, the same 3D lightning as the release's). Black bodies with a
 * red rim, as the storm's ({@link ConquerorStorm#BODY}, {@link ConquerorStorm#EDGE}): short kinked arcs leaping off the body; a burst
 * tearing off a drawn blade, a few of them long enough to reach the ground. Every point is relative to the body's feet (blocks).
 */
public final class ConquerorSparks {
    /** The rim's colour (the storm's red). */
    public static final int RED = 0xFF1433;
    /** How fast an arc races out (blocks a tick). */
    static final double SPEED = 10.0;

    private ConquerorSparks() {
    }

    /** {@code count} arcs off a body {@code height} tall and {@code width} wide, from about its feet (x, y, z: blocks from them). */
    public static List<Bolt> body(int count, double height, double width, long seed) {
        SplittableRandom random = new SplittableRandom(seed ^ 0x5350_4152_4BL);
        List<Bolt> out = new ArrayList<>(count * 2);
        double r = Math.max(0.25, width * 0.5);
        for (int i = 0; i < count; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double y = height * (0.2 + 0.75 * random.nextDouble());
            double[] from = {Math.cos(a) * r, y, Math.sin(a) * r};
            double[] dir = ConquerorStorm.normalise(new double[] {Math.cos(a), random.nextDouble(-0.6, 0.9), Math.sin(a)});
            double length = 0.8 + 1.4 * random.nextDouble();
            arc(out, random, from, dir, length, 0.05 + 0.03 * random.nextDouble(), (float) (random.nextDouble() * 3.0), 5 + random.nextInt(4));
        }
        return out;
    }

    /**
     * A burst of {@code count} arcs tearing off a blade from {@code base} to {@code tip} (blocks from the feet): out of points all along
     * it, every way, the longest down to the ground ({@code floor}: the ground's height under the feet, usually 0).
     */
    public static List<Bolt> blade(double[] base, double[] tip, int count, double floor, long seed) {
        SplittableRandom random = new SplittableRandom(seed ^ 0x424C_4144_45L);
        List<Bolt> out = new ArrayList<>(count * 2);
        for (int i = 0; i < count; i++) {
            double t = random.nextDouble();
            double[] from = {base[0] + (tip[0] - base[0]) * t, base[1] + (tip[1] - base[1]) * t, base[2] + (tip[2] - base[2]) * t};
            double[] dir = Sphere.unit(random);
            double length = 1.2 + 2.4 * random.nextDouble();
            if (i % 4 == 0) {
                // down to the ground: lightning grounding off the blade
                dir = ConquerorStorm.normalise(new double[] {dir[0] * 0.5, -1.0, dir[2] * 0.5});
                length = Math.max(length, from[1] - floor + 0.6);
            }
            arc(out, random, from, dir, length, 0.07 + 0.05 * random.nextDouble(), (float) (random.nextDouble() * 2.0), 7 + random.nextInt(5));
        }
        return out;
    }

    // 3.2.5: the old thin burst of arcs (3.1.2's, unused since 3.1.8) deleted for good (the owner: the old lightning "should be gone from
    // the game entirely"): every move's lightning is the release's own (ConquerorStorm.blast).

    /**
     * One arc from {@code from} along {@code dir}: a crackled stroke (Bolt.crackled: fractal kinks, swelling and pinching), thin, black
     * with its red rim, and now and then a fork off its middle.
     */
    static void arc(List<Bolt> out, SplittableRandom random, double[] from, double[] dir, double length, double size, float delay, int duration) {
        double[] to = {from[0] + dir[0] * length, from[1] + dir[1] * length, from[2] + dir[2] * length};
        float width = (float) (size * 0.18);
        float crawl = (float) (length / SPEED);
        long seed = random.nextLong();
        out.add(Bolt.crackled(from, to, width, 0.25F, ConquerorStorm.BODY, delay, duration, crawl, seed, 0.42));
        out.add(Bolt.crackled(from, to, width, 0.25F, 0xFF000000 | RED, delay, duration, crawl, seed, 0.42));
        if (random.nextDouble() < 0.6) {
            double t = 0.35 + 0.3 * random.nextDouble();
            double[] mid = {from[0] + (to[0] - from[0]) * t, from[1] + (to[1] - from[1]) * t, from[2] + (to[2] - from[2]) * t};
            double[] fork = ConquerorStorm.normalise(new double[] {dir[0] + random.nextDouble(-1, 1), dir[1] + random.nextDouble(-1, 1),
                dir[2] + random.nextDouble(-1, 1)});
            double l = length * (0.35 + 0.3 * random.nextDouble());
            double[] end = {mid[0] + fork[0] * l, mid[1] + fork[1] * l, mid[2] + fork[2] * l};
            long forkSeed = random.nextLong();
            float d = delay + (float) (t * length / SPEED);
            out.add(Bolt.crackled(mid, end, width * 0.6F, 0.2F, ConquerorStorm.BODY, d, duration - 1, (float) (l / SPEED), forkSeed, 0.45));
            out.add(Bolt.crackled(mid, end, width * 0.6F, 0.2F, 0xFF000000 | RED, d, duration - 1, (float) (l / SPEED), forkSeed, 0.45));
        }
    }
}
