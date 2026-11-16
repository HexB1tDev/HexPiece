package com.hexhaki.piece.haki;

import java.util.List;
import java.util.SplittableRandom;

/**
 * 2.1.5: the strokes one of the original's lightning beams becomes (pure: the game and {@code tools/preview/BoltPreview} share it). A
 * beam from A to B is a crackled {@link Bolt}, thinning a little toward B, racing out from A; a long-lived one re-strikes halfway into
 * a new shape (as real lightning does, the old shape gone as the new one lands); a strong one may shed a fork that lands later and dies
 * first. Every choice comes from the seed of the beam's ends, so the black body, the red core and the hot heart the original lays on
 * one segment crackle, re-strike and fork as one bolt.
 */
public final class BoltPainter {
    static final long RESTRIKE = 0x5245_5354L;
    static final long FORK = 0x464F_524BL;
    static final double ROUGH = 0.22;

    private BoltPainter() {
    }

    /** How fast a strike races along its bolt, blocks a tick (fast enough to cross a Conqueror's field in a few ticks, slow enough to see). */
    public static final double SPEED = 9.0;

    /** Ticks a stroke of this length takes to race out. */
    public static float crawl(double length, int duration) {
        double ticks = Math.max(0.12, Math.min(1.6, length / SPEED));
        return (float) Math.min(ticks, Math.max(0.12, duration / 3.0));
    }

    /**
     * One effect's chains: the original lays a long bolt as a string of beams, each starting where the last ended. A beam that starts
     * where another landed waits for it, so the strike races along the whole bolt instead of every piece growing at once.
     */
    public static final class Chain {
        private final java.util.Map<Long, Float> landed = new java.util.HashMap<>();

        public static long key(double[] p) {
            return Math.round(p[0] * 32.0) * 0x9E3779B1L ^ Math.round(p[1] * 32.0) * 0x85EBCA77L ^ Math.round(p[2] * 32.0) * 0xC2B2AE3DL;
        }

        /** When a beam from {@code a} with the original's {@code delay} strikes. */
        public float start(double[] a, int delay) {
            Float before = this.landed.get(key(a));
            return before == null ? delay : Math.max(delay, before);
        }

        /** The beam that struck at {@code start} reaches {@code b} after its crawl. */
        public void land(double[] a, double[] b, float start, int duration) {
            double dx = b[0] - a[0];
            double dy = b[1] - a[1];
            double dz = b[2] - a[2];
            float at = start + crawl(Math.sqrt(dx * dx + dy * dy + dz * dz), duration);
            this.landed.merge(key(b), at, Math::min);
        }
    }

    /**
     * Every stroke of one effect whose end no other stroke carries on from, drawn to a point: the free ends of the bolts taper as
     * lightning does, while the joints inside a chain stay full.
     */
    public static void point(List<Bolt> bolts) {
        java.util.Set<Long> roots = new java.util.HashSet<>();
        for (Bolt bolt : bolts) {
            roots.add(Chain.key(new double[] {bolt.points[0], bolt.points[1], bolt.points[2]}));
        }
        for (int i = 0; i < bolts.size(); i++) {
            Bolt bolt = bolts.get(i);
            int last = bolt.points.length - 3;
            if (!roots.contains(Chain.key(new double[] {bolt.points[last], bolt.points[last + 1], bolt.points[last + 2]}))) {
                bolts.set(i, bolt.pointed());
            }
        }
    }

    /** The strokes of one beam, added to {@code out}. */
    public static void strokes(double[] a, double[] b, float width, int color, int duration, float delay, boolean forks, long salt,
                               List<Bolt> out) {
        strokes(a, b, width, color, duration, delay, forks, salt, -1, out);
    }

    /** As above; {@code levels} (if not negative) caps the crackle's levels. */
    public static void strokes(double[] a, double[] b, float width, int color, int duration, float delay, boolean forks, long salt,
                               int levels, List<Bolt> out) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = b[2] - a[2];
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-4) {
            return;
        }
        int crackle = levels < 0 ? Bolt.levels(length) : Math.min(levels, Bolt.levels(length));
        long seed = Jag.seed(a[0], a[1], a[2], b[0], b[1], b[2], salt);
        float crawl = crawl(length, duration);
        if (duration >= 10 && length >= 1.0) {
            int half = duration / 2;
            out.add(Bolt.crackled(a, b, width, 1.0F, color, delay, half + 1, crawl, seed, ROUGH, crackle));
            out.add(Bolt.crackled(a, b, width * 0.9F, 1.0F, color, delay + half, duration - half, Math.min(crawl, 0.6F), seed ^ RESTRIKE,
                ROUGH, crackle));
        } else {
            out.add(Bolt.crackled(a, b, width, 1.0F, color, delay, duration, crawl, seed, ROUGH, crackle));
        }
        if (!forks || length < 1.2) {
            return;
        }
        SplittableRandom random = new SplittableRandom(seed ^ FORK);
        if (random.nextDouble() >= 0.42) {
            return;
        }
        double at = 0.25 + random.nextDouble() * 0.5;
        double[] main = Bolt.path(a, b, crackle, ROUGH, seed);
        int points = main.length / 3;
        int index = Math.max(1, Math.min(points - 2, (int) Math.round(at * (points - 1))));
        double[] from = {main[index * 3], main[index * 3 + 1], main[index * 3 + 2]};
        double[] across = Jag.across(dx, dy, dz);
        double side = random.nextDouble() < 0.5 ? -1 : 1;
        double turn = random.nextDouble() * Math.PI * 2.0;
        double reach = length * (0.24 + random.nextDouble() * 0.2);
        double ox = across[0] * Math.cos(turn) + across[3] * Math.sin(turn);
        double oy = across[1] * Math.cos(turn) + across[4] * Math.sin(turn);
        double oz = across[2] * Math.cos(turn) + across[5] * Math.sin(turn);
        double[] tip = {
            from[0] + (dx / length * 0.72 + ox * 0.7 * side) * reach,
            from[1] + (dy / length * 0.72 + oy * 0.7 * side) * reach - reach * 0.1,
            from[2] + (dz / length * 0.72 + oz * 0.7 * side) * reach};
        float start = delay + crawl * index / (float) (points - 1);
        int life = Math.max(2, (int) (duration * 0.6));
        out.add(Bolt.crackled(from, tip, width * 0.55F, 0.3F, color, start, life, crawl(reach, life), random.nextLong(), 0.28));
    }
}
