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
    /**
     * 3.6.0 (the owner: "every lightning should be more croocked, its going too straight Look at haki references man"): the straight runs
     * between kinks, about this long (was 6: long sticks), each zig-zagging sharply off the bolt's way ({@link #ZIG_LEAST} to {@link #ZIG_MOST} degrees) and kinked again inside.
     */
    static final double STEP = 3.0;
    /**
     * 3.6.0 (the owner's reference, "this is how the lightnign should shoot"): how sharply each run zig-zags off the bolt's own way out
     * of the player, in degrees, least and most (it was a free wander of 0.85 a run, which could turn a bolt back on itself: tangles).
     */
    static final double ZIG_LEAST = 25.0;
    static final double ZIG_MOST = 55.0;
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

    /**
     * 3.6.0 (the owner: "the green lines are always empty, you are never using those spaces ... its only shooting down and up, the
     * middle is empty", and their reference of the bolts shooting out of the player every way): the ways {@code count} bolts leave the
     * player, all round them (a golden-angle turn each), by thirds: one out level at the chest (rising {@link #LEVEL_LEAST} to
     * {@link #LEVEL_MOST} degrees), one up into the sky (20 to 72), one down to strike the ground out where it lands (15 to 55 under).
     */
    static double[] spread(int count, SplittableRandom random) {
        int n = Math.max(1, count);
        double[] out = new double[n * 3];
        double yaw = random.nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < n; i++) {
            yaw += 2.399963229728653 + random.nextDouble(-0.25, 0.25);
            double up = switch (i % 3) {
                case 0 -> LEVEL_LEAST + (LEVEL_MOST - LEVEL_LEAST) * random.nextDouble();
                case 1 -> 20.0 + 52.0 * random.nextDouble();
                default -> -15.0 - 40.0 * random.nextDouble();
            };
            double r = Math.toRadians(up);
            out[i * 3] = Math.cos(yaw) * Math.cos(r);
            out[i * 3 + 1] = Math.sin(r);
            out[i * 3 + 2] = Math.sin(yaw) * Math.cos(r);
        }
        return out;
    }

    /** 3.6.0: how steeply a level bolt rises as it shoots out from the chest (degrees): it stays out in the middle, off the ground. */
    static final double LEVEL_LEAST = 3.0;
    static final double LEVEL_MOST = 14.0;
    /** 3.6.0: the least a bolt not sent down stays over the ground (blocks): it never drops out of the middle into the ground. */
    static final double AIR = 0.7;

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
        double[] directions = spread(count, random);
        for (int i = 0; i < count; i++) {
            double[] dir = {directions[i * 3], directions[i * 3 + 1], directions[i * 3 + 2]};
            bolt(out, random, dir, reach * (0.7 + 0.3 * random.nextDouble()), (float) (random.nextDouble() * 1.2), 1.0 + 0.35 * m);
        }
        return out;
    }

    /**
     * 3.1.8 (the owner: "the lightning from the explosions and all when Haki is activated is not the same as the one in Conqueror's
     * Release. If you check, the one in Conqueror's Release is way thicker ... It should be the same size like as the one in Conquer"):
     * the Conqueror's lightning out of a fruit's blast or strike (server/ConquerorAura.mixed, client/haki/ConquerorSparksClient) is now
     * the release's very bolts ({@link #bolt}: the same ribbons, as wide, kinked and branching the same way, the same body and rim),
     * out of the blast's point instead of the chest: {@code count} of them (the burst's 2 to 14 arcs make 1 to 6 bolts), each 8 to 40
     * blocks long (2.5 times the blast's {@code radius}), two in three sent up and out of the ground, those aimed down striking the
     * ground {@code floor} under it (relative, negative) and tearing along it; {@code size} the release's (1, its thickest 1.35). Points
     * relative to the blast's point.
     */
    /**
     * 3.2.5 (the owner, after 3.2.2 made them a fifth as wide: "The lightning is to be exactly the same as the one in Conqueror's Haki
     * release. The one that you added for the fruits is the old lightning effects. They are not meant to be in use anymore"; before, after
     * 3.1.8: "ridiculously fat compared to the other one"): a blast's bolt is a release bolt cut short. Its width is the release's own at
     * the same distance from where it starts, as along a release bolt {@link #RELEASE_SPAN} blocks long (0.43 blocks where it leaves,
     * swelling as it goes), tapering to a quarter at its own end as the release's do; its kinks, branches, black body and rim the
     * release's. (3.1.8 squeezed the release's whole width curve into the short bolt: it swelled to its widest within a few blocks of the
     * player, fat tubes; 3.2.2 drew it a fifth as wide: thin black lines, the old look. Both are gone: never scale a blast's width on its
     * own again.)
     */
    public static final double RELEASE_SPAN = 80.0;

    public static List<Bolt> blast(double radius, int count, double floor, long seed, double size) {
        SplittableRandom random = new SplittableRandom(seed ^ 0x424C_4153_54L);
        double height = Math.max(GROUND + 0.3, -floor);
        int n = Math.max(1, Math.min(6, 1 + count / 3));
        double[] directions = spread(n, random);
        List<Strand> strands = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double[] dir = {directions[i * 3], directions[i * 3 + 1], directions[i * 3 + 2]};
            double length = Math.max(8.0, Math.min(40.0, radius * 2.5)) * (0.75 + 0.25 * random.nextDouble());
            // the release's very bolt, cut short: its width by the distance from the blast as along a release bolt, its kinks the release's
            bolt(strands, random, dir, length, (float) (random.nextDouble() * 0.8), size, height, 0.4, STEP, RELEASE_SPAN);
        }
        List<Bolt> out = new ArrayList<>(strands.size() * 2);
        for (Strand strand : strands) {
            double[] p = strand.points().clone();
            for (int i = 1; i < p.length; i += 3) {
                p[i] -= height;
            }
            // a blast's strike snaps (about 11 ticks, the release's burn 27): as many on screen at once stay within a phone's budget
            out.addAll(bolts(new Strand(p, strand.widths(), strand.tear(), strand.delay(), Math.max(8, (int) (strand.duration() * 0.42)),
                strand.length(), strand.seed())));
        }
        return out;
    }

    /**
     * The width {@code walked} blocks along a bolt {@code total} long: the release's at that share of its length ({@code span} 0), or (a
     * blast's) the release's at that distance along a release bolt {@code span} long, tapering to a quarter over its own last third.
     */
    static float spanWidth(double walked, double total, double span, double size) {
        if (span <= 0.0) {
            return width(walked / Math.max(1.0E-6, total), size);
        }
        double left = Math.max(0.0, total - walked) / Math.max(1.0E-6, total * 0.35);
        return (float) (width(Math.min(1.0, walked / span), size) * (0.25 + 0.75 * Math.min(1.0, left)));
    }

    /**
     * 3.6.0 (the owner: "JESUS WHY IS THE LIGHTNING SO THICK", with their reference: bolts shooting out of the player, the widest about
     * half a block): the widest a bolt is drawn, in blocks, at size 1, before its taper ({@link #width}: 0.68 blocks at its widest; it was
     * 3.6, 2.7 blocks: folded ribbons). The one Haki lightning's width: the release's and every blast's with it.
     */
    public static final double WIDEST = 0.9;
    /** The width a bolt had before 3.6.0, over its width now: the storm still strikes bodies as far from its line as before. */
    static final double HIT_WIDTH = 3.6 / WIDEST;

    /** The stroke width at {@code t} (0 at the flash, 1 at the far end), times {@code size}: swelling out of the flash, tapering far out. */
    static float width(double t, double size) {
        // thin out of the flash (the player is never walled in by their own storm), full by a third of the way, tapering far out
        double swell = Math.min(1.0, t / 0.33);
        swell = swell * swell * (3 - 2 * swell);
        return (float) (size * WIDEST * (0.12 + 0.88 * swell) * (1.0 - 0.75 * t));
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
            return build(delay, duration, size, widthShare, seed, 0.0);
        }

        /** As above; {@code span} over 0: the width by the distance walked as along a release bolt that long (a blast's). */
        Strand build(float delay, int duration, double size, double widthShare, long seed, double span) {
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
                w[i] = spanWidth(walked, total, span, size) * (float) widthShare * (tear[i] > 0 ? 0.5F : 1.0F);
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
        bolt(out, random, dir, length, delay, size, CHEST, START, STEP, 0.0);
    }

    /** As above, out of a flash {@code chest} over the ground, the strands leaving it {@code start} blocks out (3.1.8: a blast's). */
    static void bolt(List<Strand> out, SplittableRandom random, double[] dir, double length, float delay, double size, double chest,
                     double start, double runs, double span) {
        final double CHEST = chest;
        final double START = start;
        final double STEP = runs;
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
        // 3.6.0 (the owner's reference: "this is how the lightnign should shoot": every bolt shooting straight out of the player, kinked
        // sharply): the side the runs zig-zag to (across the bolt's way, turning slowly), and which side the next run goes
        double[] side = across(dir, Sphere.unit(random));
        double zig = random.nextBoolean() ? 1.0 : -1.0;
        List<Object[]> branches = new ArrayList<>();
        while (walked < length - 0.5) {
            double step = Math.min(length - walked, STEP * (0.6 + 0.8 * random.nextDouble()));
            double[] next;
            boolean wasGrounded = grounded;
            if (!grounded) {
                double[] wander = Sphere.unit(random);
                boolean gliding = land > 0.0 && walked < land;
                if (gliding) {
                    // on its glide down to where it lands as before (it must land out where it was sent, never back at the feet)
                    h = normalise(new double[] {h[0] * 0.55 + dir[0] * 0.45 + wander[0] * 0.42, h[1] * 0.55 + dir[1] * 0.45 + wander[1] * 0.42,
                        h[2] * 0.55 + dir[2] * 0.45 + wander[2] * 0.42});
                } else {
                    // 3.6.0 (the owner's reference): each run a sharp zig-zag off the bolt's own straight way out (25 to 55 degrees), now
                    // one side, now the other, never turning back on itself; one that has strayed far off its line zags back toward it
                    side = across(dir, new double[] {side[0] + wander[0] * 0.5, side[1] + wander[1] * 0.5, side[2] + wander[2] * 0.5});
                    zig = random.nextDouble() < 0.8 ? -zig : zig;
                    double[] line = {a[0], a[1] - CHEST, a[2]};
                    double along = line[0] * dir[0] + line[1] * dir[1] + line[2] * dir[2];
                    double[] off = {line[0] - dir[0] * along, line[1] - dir[1] * along, line[2] - dir[2] * along};
                    double stray = Math.sqrt(off[0] * off[0] + off[1] * off[1] + off[2] * off[2]);
                    if (stray > 1.0 + 0.12 * walked) {
                        zig = (off[0] * side[0] + off[1] * side[1] + off[2] * side[2]) > 0.0 ? -1.0 : 1.0;
                    }
                    double k = Math.tan(Math.toRadians(ZIG_LEAST + (ZIG_MOST - ZIG_LEAST) * random.nextDouble()));
                    h = normalise(new double[] {dir[0] + side[0] * zig * k, dir[1] + side[1] * zig * k, dir[2] + side[2] * zig * k});
                }
                next = new double[] {a[0] + h[0] * step, a[1] + h[1] * step, a[2] + h[2] * step};
                if (land > 0.0) {
                    // on its glide down to where it lands, wandering a little either side of it
                    double share = Math.min(1.0, (walked + step) / land);
                    next[1] = share >= 1.0 ? GROUND - 1.0 : CHEST + (GROUND - CHEST) * share + random.nextDouble(-0.3, 0.3) * (1.0 - share);
                }
                if (land <= 0.0 && next[1] < GROUND + AIR) {
                    // 3.6.0: its zig-zags never bring a bolt sent level or up down into the ground (that is a downward strand's, far out)
                    next[1] = GROUND + AIR + random.nextDouble() * 0.3;
                }
                if (next[1] < GROUND) {
                    // it struck the ground: from here it tears along it, outward
                    grounded = true;
                    next[1] = GROUND;
                }
            } else {
                yaw += random.nextDouble(-0.6, 0.6);
                next = new double[] {a[0] + Math.cos(yaw) * step, GROUND, a[2] + Math.sin(yaw) * step};
            }
            double t = Math.min(1.0, (walked + step) / length);
            // a second kink inside the run, off its middle (3.6.0: sharper, and not always halfway: a kinked line, not a straight stick)
            double at = 0.35 + 0.3 * random.nextDouble();
            double[] mid = {a[0] + (next[0] - a[0]) * at, a[1] + (next[1] - a[1]) * at, a[2] + (next[2] - a[2]) * at};
            double[] off = Sphere.unit(random);
            double kink = step * 0.3;
            // 3.6.0: the run that strikes the ground keeps its inner kink in the air (only what is on the ground tears)
            mid = new double[] {mid[0] + off[0] * kink, wasGrounded ? GROUND : Math.max(GROUND + 0.15, mid[1] + off[1] * kink), mid[2] + off[2] * kink};
            double tear = grounded ? 1.0 - 0.8 * t : 0.0;
            b.add(mid, wasGrounded ? tear : 0.0);
            b.add(next, tear);
            if (walked > Math.min(10.0, length * 0.35) && random.nextDouble() < (length < 40.0 ? 0.3 : 0.16)) {
                branches.add(new Object[] {next.clone(), h.clone(), grounded, yaw, Math.min(length - walked, 8.0 + random.nextDouble() * 14.0),
                    delay + (float) ((walked + step) / SPEED), (double) spanWidth(walked + step, length, span, size) * 0.45});
            }
            walked += step;
            a = next;
        }
        out.add(b.build(delay, 24 + random.nextInt(6), size, 1.0, random.nextLong(), span));
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
        // 3.6.0: more, shorter runs, each turned hard (the owner: "more croocked")
        int pieces = 3 + random.nextInt(3);
        for (int p = 0; p < pieces; p++) {
            double step = length / pieces;
            double[] next = grounded ? new double[] {a[0] + Math.cos(turn) * step, GROUND, a[2] + Math.sin(turn) * step}
                : new double[] {a[0] + h[0] * step, Math.max(GROUND, a[1] + h[1] * step), a[2] + h[2] * step};
            turn += random.nextDouble(-0.8, 0.8);
            if (!grounded) {
                double[] w2 = Sphere.unit(random);
                h = normalise(new double[] {heading[0] * 0.5 + h[0] * 0.2 + w2[0] * 0.8, heading[1] * 0.5 + h[1] * 0.2 + w2[1] * 0.8,
                    heading[2] * 0.5 + h[2] * 0.2 + w2[2] * 0.8});
            }
            b.add(next, grounded ? 0.35 : 0.0);
            a = next;
        }
        // its width: the bolt's at the fork, tapering (Builder's taper on a branch's own length, scaled to the fork's width; 3.6.0: over
        // WIDEST, it was over the old 3.6)
        Strand s = b.build(delay, 18 + random.nextInt(6), 1.0, width / WIDEST, random.nextLong());
        out.add(s);
    }

    /**
     * 3.6.0 (the owner: "Just make sure ALL haki is shared, only 1 type and style of haki  model should be in use, THE CONQUEROR HAKI
     * RELEASE (BUTTON G)", and "every lightning should be more croocked"): the one Haki lightning's path from {@code a} to {@code b} at
     * any size: the release's straight runs kinked hard off each other (a run of about {@link #STEP}, or a quarter of a short bolt),
     * wandering off the line and pulled back to it, each run kinked again inside. The original's painted effects (a coated hit, the tap
     * release, the ground storm), the sparks off a body and a blade, the sword's crackle and the lance all draw with it ({@link #strike}).
     */
    public static double[] jagged(double[] a, double[] b, long seed) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double dz = b[2] - a[2];
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-6) {
            return new double[] {a[0], a[1], a[2], b[0], b[1], b[2]};
        }
        SplittableRandom random = new SplittableRandom(seed ^ 0x4A41_4747_4544L);
        double run = Math.min(STEP, Math.max(0.05, length / 4.0));
        int runs = (int) Math.max(3, Math.min(24, Math.round(length / run)));
        double[] across = Jag.across(dx, dy, dz);
        double amp = length / runs * 0.55;
        double[] out = new double[(runs * 2 + 1) * 3];
        out[0] = a[0];
        out[1] = a[1];
        out[2] = a[2];
        double ou = 0.0;
        double ov = 0.0;
        double[] prev = a.clone();
        int k = 1;
        for (int i = 1; i <= runs; i++) {
            double t = i / (double) runs;
            double[] end;
            if (i < runs) {
                t += random.nextDouble(-0.25, 0.25) / runs;
                ou = ou * 0.35 + random.nextDouble(-1.0, 1.0) * amp;
                ov = ov * 0.35 + random.nextDouble(-1.0, 1.0) * amp;
                end = new double[] {a[0] + dx * t + across[0] * ou + across[3] * ov, a[1] + dy * t + across[1] * ou + across[4] * ov,
                    a[2] + dz * t + across[2] * ou + across[5] * ov};
            } else {
                end = b.clone();
            }
            double at = 0.35 + 0.3 * random.nextDouble();
            double piece = Math.sqrt((end[0] - prev[0]) * (end[0] - prev[0]) + (end[1] - prev[1]) * (end[1] - prev[1])
                + (end[2] - prev[2]) * (end[2] - prev[2]));
            double[] off = Sphere.unit(random);
            for (int c = 0; c < 3; c++) {
                out[k * 3 + c] = prev[c] + (end[c] - prev[c]) * at + off[c] * piece * 0.3;
                out[(k + 1) * 3 + c] = end[c];
            }
            k += 2;
            prev = end;
        }
        return out;
    }

    /**
     * 3.6.0: one stroke of the one Haki lightning (the release's model: its black body and its rim on the same points, {@link #jagged}),
     * {@code width} where it leaves (thin, swelling a little, tapering to {@code tip} of it at its end, as a release bolt does), its rim
     * {@code rim} (tinted to its owner's Haki colour when it is let go).
     */
    public static List<Bolt> strike(double[] a, double[] b, float width, float tip, int rim, float delay, int duration, float crawl,
                                    long seed) {
        double[] p = jagged(a, b, seed);
        int n = p.length / 3;
        double[] walked = new double[n];
        for (int i = 1; i < n; i++) {
            double ddx = p[i * 3] - p[i * 3 - 3];
            double ddy = p[i * 3 + 1] - p[i * 3 - 2];
            double ddz = p[i * 3 + 2] - p[i * 3 - 1];
            walked[i] = walked[i - 1] + Math.sqrt(ddx * ddx + ddy * ddy + ddz * ddz);
        }
        double total = Math.max(1.0E-6, walked[n - 1]);
        float[] w = new float[n];
        for (int i = 0; i < n; i++) {
            double t = walked[i] / total;
            double swell = Math.min(1.0, t / 0.3);
            swell = swell * swell * (3 - 2 * swell);
            w[i] = (float) (width * (0.6 + 0.4 * swell) * (1.0 - (1.0 - tip) * t));
        }
        List<Bolt> out = new ArrayList<>(2);
        out.add(new Bolt(p, w, BODY, Bolt.BODY, delay, duration, crawl, seed, 0.05));
        out.add(new Bolt(p, w, 0xFF000000 | rim, Bolt.EDGE, delay, duration, crawl, seed, 0.05));
        return out;
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
                double reach = half + strand.widths()[Math.min(strand.widths().length - 1, i)] * HIT_WIDTH / 2.0;
                double y = ay + dy * t;
                if (ex * ex + ez * ez <= reach * reach && y >= bottom - 0.5 && y <= top + 0.5) {
                    return true;
                }
            }
        }
        return false;
    }

    /** {@code v} with its part along {@code way} taken out, made a unit (any unit across {@code way} when nothing is left). */
    static double[] across(double[] way, double[] v) {
        double d = v[0] * way[0] + v[1] * way[1] + v[2] * way[2];
        double[] o = {v[0] - way[0] * d, v[1] - way[1] * d, v[2] - way[2] * d};
        if (o[0] * o[0] + o[1] * o[1] + o[2] * o[2] < 1.0E-8) {
            o = Math.abs(way[1]) < 0.9 ? new double[] {-way[2], 0.0, way[0]} : new double[] {1.0, 0.0, 0.0};
        }
        return normalise(o);
    }

    static double[] normalise(double[] v) {
        double l = Math.max(1.0E-9, Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]));
        return new double[] {v[0] / l, v[1] / l, v[2] / l};
    }
}
