package com.hexhaki.piece.haki;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * 2.1.7: the Conqueror's full-power release as a storm (pure, tested in {@code LightningTests}). The owner: "the haki full power
 * explosion should be ridiculously bigger than what it currently is, lightning spreading further etc"; 2.1.8: "The lightning should
 * spread in a perfect sphere randomly every single time".
 *
 * <p>2.2.5, drawn after the two anime frames the owner sent ("this is how conqueror's is meant to be released, the circle is where the
 * player is. Like this in all directions"): from a bright flash where the player stands, a few enormous bolts tear out in every
 * direction ({@link Sphere}: a fresh set every release, still few: "too much lightning way too much"). Each is one continuous ribbon
 * (a {@link Strand}: one polyline, so it bends at mitred joints with no caps between its pieces, never a chain of blobs): long straight
 * runs joined by sharp kinks as lightning kinks, swelling as it leaves the flash and tapering far out, black with a rim of the colour
 * (drawn as a {@link Bolt#BODY} and a {@link Bolt#EDGE} on the same points: the frames' hot outline and glow); now and then a branch.
 * The ones that go down glide out low and strike the ground at their own distance (2.4.5; before, all of them near the player) and tear
 * along it outward: their points on the ground ({@link Strand#tear} above 0) are where the server opens fissures ({@code server/Fissures},
 * from the same seed). The shockwave ring and the centre's flash
 * are drawn by {@code client/haki/HakiBolts}; the client tints the strokes to the owner's Ryou colour.
 *
 * <p>Every point is given as a height above the ground under it (x, height, z; blocks from the player's feet): the client lays the
 * storm on the real terrain when it lets it go, the server reads the same terrain for the fissures.
 */
public final class ConquerorStorm {
    public static final double SPEED = 12.0;
    public static final int BODY = 0xF0050307;
    /** The rim's colour (tinted to the owner's Ryou colour at launch). */
    public static final int EDGE = 0xFFFF1433;
    static final double STEP = 6.0;
    /** Where the bolts leave the flash (blocks from the chest). */
    static final double START = 2.2;
    /** The chest, above the feet. */
    public static final double CHEST = 1.2;
    /** A point on the ground (height above it): the strands tear along the ground at about this. */
    public static final double GROUND = 0.2;
    /** 2.4.5: where a strand aimed down strikes the ground, as shares of its length (spread by area between them). */
    public static final double LAND_NEAR = 0.08;
    public static final double LAND_FAR = 0.38;

    /**
     * One bolt (or branch) of the storm: its points (x, height above the ground, z; blocks from the player's feet), its stroke width at
     * each, how far into a fissure's full width and depth the ground tears at each (0 where it is in the air), when it strikes, how long
     * it burns, its length.
     */
    public record Strand(double[] points, float[] widths, double[] tear, float delay, int duration, double length, long seed) {
        public int count() {
            return this.points.length / 3;
        }

        /** Ticks the strike takes to race to its end. */
        public float crawl() {
            return (float) (this.length / SPEED);
        }
    }

    private ConquerorStorm() {
    }

    /** How far the storm reaches at this Conqueror mastery (0..1000). */
    public static double reach(int mastery) {
        return 60.0 + 60.0 * Math.max(0.0, Math.min(1.0, mastery / 1000.0));
    }

    /** 2.9.1: how many enormous bolts tear out of a full-power release at {@code mastery} (0 to 1000): 7 to 25. */
    public static int count(int mastery) {
        double m = Math.max(0.0, Math.min(1.0, mastery / 1000.0));
        return 7 + (int) Math.round(2 * m + 16 * m * m * m);
    }

    /** The whole storm of one full-power release. */
    public static List<Strand> release(int mastery, long seed, boolean supreme) {
        double m = Math.max(0.0, Math.min(1.0, mastery / 1000.0));
        double reach = reach(mastery);
        SplittableRandom random = new SplittableRandom(seed ^ 0x5354_4F52_4DL);
        List<Strand> out = new ArrayList<>();
        // 2.9.1 (the owner: "I want the Conqueror's release to be, it's extremely destructive at max level. Like, I want it to be like
        // fucking tearing the floor. I want massive amounts of lightning shooting everywhere, up, down ... it's meant to be ridiculous"):
        // seven to ten bolts a young Conqueror, then steeply more toward mastery 1000 (some 26): those that come down tear the ground
        int count = count(mastery) + (supreme ? 1 : 0);
        double[] directions = Sphere.directions(count, random);
        for (int i = 0; i < count; i++) {
            double[] dir = {directions[i * 3], directions[i * 3 + 1], directions[i * 3 + 2]};
            bolt(out, random, dir, reach * (0.7 + 0.3 * random.nextDouble()), (float) (random.nextDouble() * 1.2), 1.0 + 0.35 * m);
        }
        return out;
    }

    /** The stroke width at {@code t} (0 at the flash, 1 at the far end), times {@code size}: swelling out of the flash, tapering far out. */
    static float width(double t, double size) {
        // thin out of the flash (the player is never walled in by their own storm), full by a third of the way, tapering far out
        double swell = Math.min(1.0, t / 0.33);
        swell = swell * swell * (3 - 2 * swell);
        return (float) (size * 3.6 * (0.12 + 0.88 * swell) * (1.0 - 0.75 * t));
    }

    /** The strands' points as they are built. */
    static final class Builder {
        final List<double[]> points = new ArrayList<>();
        final List<Double> tears = new ArrayList<>();

        void add(double[] p, double tear) {
            this.points.add(p);
            this.tears.add(tear);
        }

        double length() {
            double sum = 0;
            for (int i = 1; i < this.points.size(); i++) {
                double[] a = this.points.get(i - 1);
                double[] b = this.points.get(i);
                sum += Math.sqrt((b[0] - a[0]) * (b[0] - a[0]) + (b[1] - a[1]) * (b[1] - a[1]) + (b[2] - a[2]) * (b[2] - a[2]));
            }
            return sum;
        }

        Strand build(float delay, int duration, double size, double widthShare, long seed) {
            int n = this.points.size();
            double[] p = new double[n * 3];
            float[] w = new float[n];
            double[] tear = new double[n];
            double total = Math.max(1.0E-6, this.length());
            double walked = 0;
            for (int i = 0; i < n; i++) {
                double[] q = this.points.get(i);
                if (i > 0) {
                    double[] a = this.points.get(i - 1);
                    walked += Math.sqrt((q[0] - a[0]) * (q[0] - a[0]) + (q[1] - a[1]) * (q[1] - a[1]) + (q[2] - a[2]) * (q[2] - a[2]));
                }
                p[i * 3] = q[0];
                p[i * 3 + 1] = q[1];
                p[i * 3 + 2] = q[2];
                tear[i] = this.tears.get(i);
                // on the ground the bolt is a tear, lower and narrower than in the sky
                w[i] = width(walked / total, size) * (float) widthShare * (tear[i] > 0 ? 0.5F : 1.0F);
            }
            return new Strand(p, w, tear, delay, duration, total, seed);
        }
    }

    /**
     * One huge bolt from the flash out along {@code dir}, {@code length} blocks: straight runs of about {@link #STEP} joined by sharp
     * kinks (each run kinked once more about its middle), wandering hard round its line; one that goes down strikes the ground and tears
     * along it outward for the rest; now and then a branch (a strand of its own, from a point of this one).
     */
    static void bolt(List<Strand> out, SplittableRandom random, double[] dir, double length, float delay, double size) {
        Builder b = new Builder();
        // 2.4.5 (the owner: "feels like the lightning is going always to the same spot"): a strand aimed down used to meet the ground
        // within two blocks of the feet (every one there) and tear outward from it; now it glides out low from the flash and strikes
        // the ground at its own distance, LAND_NEAR..LAND_FAR of its length (spread by area), and tears on from there
        double land = -1.0;
        if (dir[1] < 0.0) {
            double flatDir = Math.hypot(dir[0], dir[2]);
            double turn = flatDir < 0.15 ? random.nextDouble() * Math.PI * 2.0 : Math.atan2(dir[2], dir[0]);
            double u = random.nextDouble();
            land = length * Math.sqrt(LAND_NEAR * LAND_NEAR + (LAND_FAR * LAND_FAR - LAND_NEAR * LAND_NEAR) * u);
            dir = normalise(new double[] {Math.cos(turn), -(CHEST - GROUND) / land, Math.sin(turn)});
        }
        double[] a = {dir[0] * START, Math.max(GROUND, CHEST + dir[1] * START), dir[2] * START};
        double[] h = dir.clone();
        double flat = Math.hypot(dir[0], dir[2]);
        double yaw = flat < 0.15 ? random.nextDouble() * Math.PI * 2.0 : Math.atan2(dir[2], dir[0]);
        boolean grounded = a[1] <= GROUND;
        b.add(a, grounded ? 1.0 : 0.0);
        double walked = 0.0;
        List<Object[]> branches = new ArrayList<>();
        while (walked < length - 0.5) {
            double step = Math.min(length - walked, STEP * (0.6 + 0.8 * random.nextDouble()));
            double[] next;
            if (!grounded) {
                double[] wander = Sphere.unit(random);
                h = normalise(new double[] {h[0] * 0.55 + dir[0] * 0.45 + wander[0] * 0.42, h[1] * 0.55 + dir[1] * 0.45 + wander[1] * 0.42,
                    h[2] * 0.55 + dir[2] * 0.45 + wander[2] * 0.42});
                next = new double[] {a[0] + h[0] * step, a[1] + h[1] * step, a[2] + h[2] * step};
                if (land > 0.0) {
                    // on its glide down to where it lands, wandering a little either side of it
                    double share = Math.min(1.0, (walked + step) / land);
                    next[1] = share >= 1.0 ? GROUND - 1.0 : CHEST + (GROUND - CHEST) * share + random.nextDouble(-0.3, 0.3) * (1.0 - share);
                }
                if (next[1] < GROUND) {
                    // it struck the ground: from here it tears along it, outward
                    grounded = true;
                    next[1] = GROUND;
                }
            } else {
                yaw += random.nextDouble(-0.45, 0.45);
                next = new double[] {a[0] + Math.cos(yaw) * step, GROUND, a[2] + Math.sin(yaw) * step};
            }
            double t = Math.min(1.0, (walked + step) / length);
            // a second, smaller kink about the run's middle (a kinked line, not a straight stick)
            double[] mid = {(a[0] + next[0]) / 2, (a[1] + next[1]) / 2, (a[2] + next[2]) / 2};
            double[] off = Sphere.unit(random);
            double kink = step * 0.16;
            mid = new double[] {mid[0] + off[0] * kink, grounded ? GROUND : Math.max(GROUND, mid[1] + off[1] * kink), mid[2] + off[2] * kink};
            double tear = grounded ? 1.0 - 0.8 * t : 0.0;
            b.add(mid, tear);
            b.add(next, tear);
            if (walked > 10.0 && random.nextDouble() < 0.16) {
                branches.add(new Object[] {next.clone(), h.clone(), grounded, yaw, Math.min(length - walked, 8.0 + random.nextDouble() * 14.0),
                    delay + (float) ((walked + step) / SPEED), (double) width(t, size) * 0.45});
            }
            walked += step;
            a = next;
        }
        out.add(b.build(delay, 24 + random.nextInt(6), size, 1.0, random.nextLong()));
        for (Object[] branch : branches) {
            branch(out, random, (double[]) branch[0], (double[]) branch[1], (Boolean) branch[2], (Double) branch[3], (Double) branch[4],
                (Float) branch[5], (Double) branch[6]);
        }
    }

    /** A branch off a bolt at {@code from}: two or three kinked runs peeling away from its heading, thin, never branching again. */
    static void branch(List<Strand> out, SplittableRandom random, double[] from, double[] heading, boolean grounded, double yaw, double length,
                       float delay, double width) {
        Builder b = new Builder();
        double[] a = from;
        b.add(a, grounded ? 0.35 : 0.0);
        double[] wander = Sphere.unit(random);
        double[] h = normalise(new double[] {heading[0] + wander[0] * 0.9, heading[1] + wander[1] * 0.9, heading[2] + wander[2] * 0.9});
        double turn = yaw + random.nextDouble(-1.0, 1.0);
        int pieces = 2 + random.nextInt(2);
        for (int p = 0; p < pieces; p++) {
            double step = length / pieces;
            double[] next = grounded ? new double[] {a[0] + Math.cos(turn) * step, GROUND, a[2] + Math.sin(turn) * step}
                : new double[] {a[0] + h[0] * step, Math.max(GROUND, a[1] + h[1] * step), a[2] + h[2] * step};
            turn += random.nextDouble(-0.5, 0.5);
            b.add(next, grounded ? 0.35 : 0.0);
            a = next;
        }
        // its width: the bolt's at the fork, tapering (Builder's taper on a branch's own length, scaled to the fork's width)
        Strand s = b.build(delay, 18 + random.nextInt(6), 1.0, width / 3.6, random.nextLong());
        out.add(s);
    }

    /**
     * The strokes a strand is drawn with: its black body and its rim ({@link Bolt#BODY}, {@link Bolt#EDGE}) on the very same points,
     * racing out together at {@link #SPEED}, barely dancing (a ribbon blocks wide shivers, it does not jump).
     */
    public static List<Bolt> bolts(Strand strand) {
        List<Bolt> out = new ArrayList<>(2);
        out.add(new Bolt(strand.points(), strand.widths(), BODY, Bolt.BODY, strand.delay(), strand.duration(), strand.crawl(), strand.seed(),
            0.03));
        out.add(new Bolt(strand.points(), strand.widths(), EDGE, Bolt.EDGE, strand.delay(), strand.duration(), strand.crawl(), strand.seed(),
            0.03));
        return out;
    }

    /**
     * 2.4.9: does the storm strike a body standing {@code x, z} from the releaser's feet (blocks), from {@code bottom} to {@code top}
     * above the ground under it, {@code half} blocks round (its half width and a margin)? The strands run as the clients lay them on
     * the ground (their heights are above the ground): a strand strikes the body where it passes within {@code half} and its stroke
     * of it sideways, at a height within the body's (a strand overhead passes over it).
     */
    public static boolean strikes(List<Strand> storm, double x, double z, double bottom, double top, double half) {
        for (Strand strand : storm) {
            double[] p = strand.points();
            for (int i = 1; i < strand.count(); i++) {
                double ax = p[(i - 1) * 3];
                double ay = p[(i - 1) * 3 + 1];
                double az = p[(i - 1) * 3 + 2];
                double dx = p[i * 3] - ax;
                double dy = p[i * 3 + 1] - ay;
                double dz = p[i * 3 + 2] - az;
                double len = dx * dx + dz * dz;
                double t = len < 1.0E-12 ? 0.0 : Math.max(0.0, Math.min(1.0, ((x - ax) * dx + (z - az) * dz) / len));
                double ex = ax + dx * t - x;
                double ez = az + dz * t - z;
                double reach = half + strand.widths()[Math.min(strand.widths().length - 1, i)] / 2.0;
                double y = ay + dy * t;
                if (ex * ex + ez * ez <= reach * reach && y >= bottom - 0.5 && y <= top + 0.5) {
                    return true;
                }
            }
        }
        return false;
    }

    static double[] normalise(double[] v) {
        double l = Math.max(1.0E-9, Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]));
        return new double[] {v[0] / l, v[1] / l, v[2] / l};
    }
}
