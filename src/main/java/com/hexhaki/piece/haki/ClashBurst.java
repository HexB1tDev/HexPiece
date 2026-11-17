package com.hexhaki.piece.haki;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * 2.3.6: two Ryou users' blows meeting (the owner: "When 2 ryou hakis clash, both lightning colors explode outwards from the clash area
 * in a sphere (similar to conquerors but smaller)"), pure, tested in {@code ClashTests}. As the Conqueror's storm
 * ({@link ConquerorStorm}) but a fraction of its size: a few bolts tear out of the point the blows met on, spread over the whole sphere
 * ({@link Sphere}: a fresh set every clash), each a kinked ribbon swelling out of the flash and tapering, black with a rim of its colour
 * ({@link Bolt#BODY} and {@link Bolt#EDGE} on the same points), the two fighters' colours by turns. A bolt that comes down to the ground
 * runs along it outward. Points are blocks from the clash point.
 */
public final class ClashBurst {
    /** Where the bolts leave the flash (blocks from the point). */
    public static final double START = 0.5;
    /** How fast a strike races out (blocks a tick). */
    public static final double SPEED = 14.0;
    static final double STEP = 1.7;

    private ClashBurst() {
    }

    /** How far the bolts reach for a clash of {@code power} (ClashRules.power: 0.7 to 3). */
    public static double reach(float power) {
        return 6.0 + 3.0 * Math.max(0.0, Math.min(3.0, power));
    }

    /** How many bolts. */
    public static int count(float power) {
        return 8 + (int) Math.round(3.0 * Math.max(0.0, Math.min(3.0, power)));
    }

    /**
     * The bolts of one clash of {@code power} between Ryou colours {@code colorA} and {@code colorB} (rgb), the ground {@code floor}
     * blocks under the point, from {@code seed}: for each strand its black body and its rim, the first colour on even strands.
     */
    public static List<Bolt> bolts(float power, int colorA, int colorB, double floor, long seed) {
        SplittableRandom random = new SplittableRandom(seed ^ 0x434C_4153_4842L);
        int count = count(power);
        double reach = reach(power);
        double size = 0.6 + 0.25 * Math.max(0.0, Math.min(3.0, power));
        double ground = -Math.max(0.2, floor) + 0.08;
        double[] directions = Sphere.directions(count, random);
        List<Bolt> out = new ArrayList<>(count * 2);
        for (int i = 0; i < count; i++) {
            double[] dir = {directions[i * 3], directions[i * 3 + 1], directions[i * 3 + 2]};
            int rgb = (i % 2 == 0 ? colorA : colorB) & 0xFFFFFF;
            double length = reach * (0.65 + 0.35 * random.nextDouble());
            strand(out, random, dir, length, ground, size, rgb, (float) (random.nextDouble() * 0.6));
        }
        return out;
    }

    /** One bolt from the flash out along {@code dir}: kinked runs of about {@link #STEP}, wandering round its line; on the ground, along it. */
    static void strand(List<Bolt> out, SplittableRandom random, double[] dir, double length, double ground, double size, int rgb, float delay) {
        List<double[]> points = new ArrayList<>();
        double[] a = {dir[0] * START, Math.max(ground, dir[1] * START), dir[2] * START};
        points.add(a);
        double[] h = dir.clone();
        double walked = 0.0;
        boolean grounded = a[1] <= ground;
        double yaw = Math.atan2(dir[2], dir[0]);
        while (walked < length - 0.2) {
            double step = Math.min(length - walked, STEP * (0.6 + 0.8 * random.nextDouble()));
            double[] next;
            if (!grounded) {
                double[] wander = Sphere.unit(random);
                h = ConquerorStorm.normalise(new double[] {h[0] * 0.55 + dir[0] * 0.45 + wander[0] * 0.5, h[1] * 0.55 + dir[1] * 0.45
                    + wander[1] * 0.5, h[2] * 0.55 + dir[2] * 0.45 + wander[2] * 0.5});
                next = new double[] {a[0] + h[0] * step, a[1] + h[1] * step, a[2] + h[2] * step};
                if (next[1] <= ground) {
                    grounded = true;
                    next[1] = ground;
                    yaw = Math.atan2(h[2], h[0]);
                }
            } else {
                yaw += random.nextDouble(-0.5, 0.5);
                next = new double[] {a[0] + Math.cos(yaw) * step, ground, a[2] + Math.sin(yaw) * step};
            }
            double[] mid = {(a[0] + next[0]) / 2, (a[1] + next[1]) / 2, (a[2] + next[2]) / 2};
            double[] off = Sphere.unit(random);
            double kink = step * 0.18;
            mid = new double[] {mid[0] + off[0] * kink, grounded ? ground : Math.max(ground, mid[1] + off[1] * kink), mid[2] + off[2] * kink};
            points.add(mid);
            points.add(next);
            walked += step;
            a = next;
        }
        int n = points.size();
        double[] p = new double[n * 3];
        float[] w = new float[n];
        for (int i = 0; i < n; i++) {
            double[] q = points.get(i);
            p[i * 3] = q[0];
            p[i * 3 + 1] = q[1];
            p[i * 3 + 2] = q[2];
            double t = n == 1 ? 0.0 : i / (double) (n - 1);
            double swell = Math.min(1.0, t / 0.25);
            swell = swell * swell * (3 - 2 * swell);
            // thin for their length (the storm's are a twentieth of theirs): one of the two stands a block from the flash
            w[i] = (float) (size * 0.17 * (0.25 + 0.75 * swell) * (1.0 - 0.75 * t));
        }
        int duration = 12 + random.nextInt(5);
        float crawl = (float) (length / SPEED);
        long seed = random.nextLong();
        out.add(new Bolt(p, w, 0xF0000000 | Bolt.body(rgb), Bolt.BODY, delay, duration, crawl, seed, 0.04));
        out.add(new Bolt(p, w, 0xFF000000 | rgb, Bolt.EDGE, delay, duration, crawl, seed, 0.04));
    }
}
