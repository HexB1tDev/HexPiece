package com.hexhaki.piece;

import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.BoltMesh;
import com.hexhaki.piece.haki.BoltPainter;
import com.hexhaki.piece.haki.ConquerorBolts;
import com.hexhaki.piece.haki.ConquerorStorm;
import com.hexhaki.piece.haki.DrawCrackle;
import com.hexhaki.piece.haki.Sphere;
import com.hexhaki.piece.haki.Jag;
import java.util.ArrayList;
import java.util.List;

/** 2.1.1: the Haki lightning's shapes (Jag) and the Conqueror's bolts keep the original's contract. */
public final class LightningTests {
    public static void main(String[] args) {
        jag();
        conqueror();
        bolts();
        mesh();
        painter();
        crackle();
        landing();
        storm();
        sphere();
        landings();
        tinted();
        stunFall();
        stunView();
        conquerorStun();
        conquerorMix();
        lightningSounds();
        culled();
        solid3d();
        Check.done("LightningTests");
    }

    static void jag() {
        double[] a = {0, 1, 0};
        double[] b = {6, 2, 1};
        for (int levels = 0; levels <= 3; levels++) {
            double[] path = Jag.path(a, b, levels, 0.2, 42L);
            Check.that(path.length == ((1 << levels) + 1) * 3, "a jag of " + levels + " levels has 2^levels + 1 points");
            Check.near(path[0], 0, 1e-12, "the bolt starts exactly at A");
            Check.near(path[path.length - 3], 6, 1e-12, "the bolt ends exactly at B");
            Check.near(path[path.length - 2], 2, 1e-12, "the bolt ends exactly at B (y)");
        }
        double[] one = Jag.path(a, b, 3, 0.2, 7L);
        double[] two = Jag.path(a, b, 3, 0.2, 7L);
        Check.that(java.util.Arrays.equals(one, two), "the same ends and seed give the same bolt (layers stay glued)");
        double[] other = Jag.path(a, b, 3, 0.2, 8L);
        Check.that(!java.util.Arrays.equals(one, other), "another seed gives another bolt");
        double length = Math.sqrt(36 + 1 + 1);
        for (int i = 0; i < one.length / 3; i++) {
            double t = i / (double) (one.length / 3 - 1);
            double[] onLine = {6 * t, 1 + t, t};
            double off = Math.sqrt(Math.pow(one[i * 3] - onLine[0], 2) + Math.pow(one[i * 3 + 1] - onLine[1], 2) + Math.pow(one[i * 3 + 2] - onLine[2], 2));
            Check.that(off < length * 0.45, "a crackled bolt never strays wildly from its line (point " + i + ": " + off + ")");
        }
        double[] straight = Jag.path(new double[] {0, 0, 0}, new double[] {0, 5, 0}, 2, 0.2, 3L);
        for (double value : straight) {
            Check.that(!Double.isNaN(value), "a vertical bolt is well defined");
        }
        Check.that(Jag.levels(0.2) == 0 && Jag.levels(1.0) == 1 && Jag.levels(3.0) == 2 && Jag.levels(9.0) == 3, "longer segments crackle more");
        Check.near(Jag.taper(0, 0.55), 1.0, 1e-9, "full width at the root");
        Check.near(Jag.taper(1, 0.55), 0.55, 1e-9, "thin at the tip");
        Check.that(Jag.taper(0.5, 0.55) < 1.0 && Jag.taper(0.5, 0.55) > 0.55, "the taper runs between");
        long s1 = Jag.seed(1, 2, 3, 4, 5, 6, 9);
        long s2 = Jag.seed(1.001, 2, 3, 4, 5, 6, 9);
        Check.that(s1 == s2, "ends within 1/64 of a block share a seed");
        Check.that(s1 != Jag.seed(4, 5, 6, 1, 2, 3, 9), "a reversed segment is another bolt");
    }

    /** 2.2.6: lightning laid on a player's body moves out onto the shell round it; what is outside stays where it is. */
    static void offBody() {
        double[] on = {0.1, 1.2, 0.0, 0.3, 0.6, 0.1, 5.0, 2.0, 0.0};
        double[] off = ConquerorBolts.offBody(on);
        Check.that(off != on && on[0] == 0.1, "a new array, the old one untouched");
        for (int i = 0; i < 2; i++) {
            Check.near(ConquerorBolts.through(off[i * 3], off[i * 3 + 1], off[i * 3 + 2]), 1.0, 1e-6, "on the shell");
        }
        Check.that(off[6] == 5.0 && off[7] == 2.0, "a point outside the shell stays");
        double[] free = {4, 1, 0, 6, 1, 0};
        Check.that(ConquerorBolts.offBody(free) == free, "nothing inside: the same array");
        double[] middle = ConquerorBolts.offBody(new double[] {0, ConquerorBolts.CENTRE, 0, 3, 1, 0});
        Check.that(!Double.isNaN(middle[1]) && middle[1] > ConquerorBolts.CENTRE, "the very middle goes up");
    }

    static void conqueror() {
        offBody();
        for (boolean dome : new boolean[] {false, true}) {
            for (float charge : new float[] {0.04F, 0.3F, 0.6F, 1.0F}) {
                for (int mastery : new int[] {0, 400, 1000}) {
                    double radius = 5.0 + (10.0 + 40.0 * mastery / 1000.0 - 5.0) * Math.max(0, Math.min(1, (charge - 0.04) / 0.96));
                    long seed = 1234L + mastery * 31L + (long) (charge * 100);
                    List<double[]> bolts = ConquerorBolts.generate(charge, mastery, seed, dome, radius);
                    List<double[]> again = ConquerorBolts.generate(charge, mastery, seed, dome, radius);
                    Check.that(bolts.size() == again.size(), "the bolts are a pure function of the seed (count)");
                    for (int i = 0; i < bolts.size(); i++) {
                        Check.that(java.util.Arrays.equals(bolts.get(i), again.get(i)), "the bolts are a pure function of the seed (shape)");
                    }
                    int mains = ConquerorBolts.count(charge, mastery, dome);
                    Check.that(bolts.size() >= mains, "at least the main bolts");
                    Check.that(dome ? mains >= 3 && mains <= 7 : mains >= 1 && mains <= 3, "main bolt count in range");
                    for (int i = 0; i < mains; i++) {
                        double[] bolt = bolts.get(i);
                        int points = bolt.length / 3;
                        Check.that(points >= 3 && points <= 2 * ConquerorBolts.MOST_POINTS - 1,
                            "a main bolt has 3.." + (2 * ConquerorBolts.MOST_POINTS - 1) + " points (" + points + ")");
                        double through = ConquerorBolts.through(bolt[0], bolt[1], bolt[2]);
                        boolean foot = Math.abs(bolt[1] - (ConquerorBolts.FLOOR + 0.15)) < 1e-9 && through <= 1.0 + 1e-9;
                        Check.that(through >= 0.95 && through <= 1.07 || foot,
                            "2.2.6: it starts on the shell round the body, never at the body (" + through + ")");
                        Check.that(Math.hypot(bolt[0], bolt[2]) >= 0.6 || bolt[1] >= 2.6,
                            "not in the player, nor just over the head (" + Math.hypot(bolt[0], bolt[2]) + ", " + bolt[1] + ")");
                        for (int p = 1; p < points; p++) {
                            Check.that(ConquerorBolts.through(bolt[p * 3], bolt[p * 3 + 1], bolt[p * 3 + 2]) >= 0.8,
                                "2.2.6: and never runs back through the body");
                        }
                        double tx = bolt[bolt.length - 3];
                        double ty = bolt[bolt.length - 2] - ConquerorBolts.CENTRE;
                        double tz = bolt[bolt.length - 1];
                        double tip = Math.sqrt(tx * tx + ty * ty + tz * tz);
                        boolean ground = bolt[bolt.length - 2] <= ConquerorBolts.FLOOR + 0.6;
                        Check.that(tip <= radius * 1.02 + 0.6 && (tip >= radius * 0.81 || ground && Math.hypot(tx, tz) >= radius * 0.3 - 1e-6),
                            "it reaches out to the sphere, or strikes down and runs out along the ground (" + tip + " of " + radius + ")");
                        for (int p = 0; p < points; p++) {
                            Check.that(bolt[p * 3 + 1] >= ConquerorBolts.FLOOR - 1e-9 && bolt[p * 3 + 1] <= ConquerorBolts.CENTRE + radius + 3.0,
                                "a bolt never goes under the ground (" + bolt[p * 3 + 1] + ")");
                            Check.that(!Double.isNaN(bolt[p * 3]) && !Double.isNaN(bolt[p * 3 + 1]) && !Double.isNaN(bolt[p * 3 + 2]), "no NaN");
                        }
                    }
                    for (int i = mains; i < bolts.size(); i++) {
                        double[] fork = bolts.get(i);
                        Check.that(fork.length >= 6, "a fork has at least two points");
                    }
                }
            }
        }
    }

    /** 2.1.5: a 3D bolt's shape and kinds. */
    static void bolts() {
        Check.that(Bolt.kindOf(0xEC050307) == Bolt.BODY, "the original's near-black beam is the bolt's body");
        Check.that(Bolt.kindOf(0xFFFF1433) == Bolt.CORE, "its red beam is the core");
        Check.that(Bolt.kindOf(0xFFFF7889) == Bolt.HEART, "its pale pink beam is the hot heart");
        Check.that(Bolt.kindOf(0x582A0008) == Bolt.AURA, "its faint crimson beam is an aura");
        Check.that(Bolt.kindOf(0xD23E5BFF) == Bolt.CORE && Bolt.kindOf(0xC260D9FF) == Bolt.CORE, "the Galaxy Fist's blue and cyan are cores");
        double[] a = {0, 1, 0};
        double[] b = {3, 2, 1};
        Bolt one = Bolt.crackled(a, b, 0.05F, 1.0F, 0xFFFF1433, 0, 14, 0.4F, 77L, 0.22);
        Bolt two = Bolt.crackled(a, b, 0.02F, 1.0F, 0xFFFF7889, 0, 14, 0.4F, 77L, 0.22);
        Check.that(java.util.Arrays.equals(one.points, two.points), "strokes on one segment with one seed crackle as one bolt");
        Check.near(one.points[0], 0, 1e-12, "a stroke starts exactly at A");
        Check.near(one.points[one.points.length - 3], 3, 1e-12, "a stroke ends exactly at B");
        double length = Math.sqrt(9 + 1 + 1);
        Check.that(length / (one.count() - 1) <= Bolt.PIECE + 1e-9 || one.count() - 1 == 1 << Bolt.MOST_LEVELS, "pieces no longer than PIECE");
        Check.near(one.widths[0], 0.05, 1e-6, "full width at the root");
        Check.near(one.widths[one.count() - 1], 0.05, 1e-6, "full width at the end: a chain stays joined");
        double[] here = new double[3];
        double[] there = new double[3];
        one.dancing(0, 6.0, here);
        Check.that(here[0] == one.points[0] && here[1] == one.points[1], "the ends never dance");
        one.dancing(3, 1.0, here);
        one.dancing(3, 1.9, there);
        Check.that(java.util.Arrays.equals(here, there), "the dance holds a shape for two ticks");
        one.dancing(3, 2.1, there);
        Check.that(!java.util.Arrays.equals(here, there), "and then takes a new one");
        Bolt pointed = one.pointed();
        Check.that(pointed.widths[pointed.count() - 1] < one.widths[one.count() - 1] * 0.3, "a free end comes to a point");
        Check.that(pointed.widths[pointed.count() - 1] == 0.0F, "2.2.6: a needle, not a square stub");
        Check.near(pointed.widths[0], one.widths[0], 1e-6, "but not at its root");
    }

    /** 2.1.5: the frame's geometry: every quad faces the eye, nothing out of its life, no NaN from any side. */
    static void mesh() {
        double[] a = {-1.5, 1.2, 4};
        double[] b = {1.5, 1.7, 4.4};
        List<Bolt> bolts = new ArrayList<>();
        BoltPainter.strokes(a, b, 0.176F, 0xEC050307, 14, 0, true, 9L, bolts);
        BoltPainter.strokes(a, b, 0.054F, 0xFFFF1433, 14, 0, true, 9L, bolts);
        BoltPainter.strokes(a, b, 0.014F, 0xFFFF7889, 12, 0, false, 9L, bolts);
        BoltPainter.strokes(a, b, 0.13F, 0x582A0008, 15, 0, false, 9L, bolts);
        double pixel = 2.0 * Math.tan(Math.toRadians(35)) / 1080;
        int[] counts = new int[2];
        for (Bolt bolt : bolts) {
            for (double age : new double[] {0.1, 0.5, 2, 5, 9, 13.9}) {
                BoltMesh.build(bolt, BoltMesh.Frame.at(0, 0, 0), age - bolt.delay, pixel, (glow, v, c) -> {
                    counts[glow ? 1 : 0]++;
                    for (double x : v) {
                        Check.that(!Double.isNaN(x) && !Double.isInfinite(x), "no NaN in a bolt's geometry");
                    }
                    Check.that(facing(v, 0, 1, 2) >= -1e-12 && facing(v, 0, 2, 3) >= -1e-12,
                        "both triangles of every quad face the eye (the batches cull back faces)");
                });
            }
            int[] none = {0};
            BoltMesh.build(bolt, BoltMesh.Frame.at(0, 0, 0), -0.5, pixel, (glow, v, c) -> none[0]++);
            BoltMesh.build(bolt, BoltMesh.Frame.at(0, 0, 0), bolt.duration + 0.5, pixel, (glow, v, c) -> none[0]++);
            Check.that(none[0] == 0, "nothing before a bolt strikes or after it is gone");
        }
        Check.that(counts[0] > 0 && counts[1] > 0, "a bolt has both ink and glow");
        // looking straight down a bolt
        Bolt end = Bolt.crackled(new double[] {0, 0, 2}, new double[] {0, 0, 6}, 0.05F, 1.0F, 0xFFFF1433, 0, 10, 0.2F, 3L, 0.0);
        BoltMesh.build(end, BoltMesh.Frame.at(0, 0, 0), 3, pixel, (glow, v, c) -> {
            for (double x : v) {
                Check.that(!Double.isNaN(x), "a bolt seen end-on is still well defined");
            }
        });
        // the strike: early on, only the root's part is drawn
        Bolt slow = Bolt.crackled(new double[] {-3, 0, 5}, new double[] {3, 0, 5}, 0.05F, 1.0F, 0xFFFF1433, 0, 20, 4.0F, 5L, 0.0);
        double[] reach = {-99};
        BoltMesh.build(slow, BoltMesh.Frame.at(0, 0, 0), 1.0, pixel, (glow, v, c) -> {
            if (!glow) {
                for (int k = 0; k < 4; k++) {
                    reach[0] = Math.max(reach[0], v[k * 3]);
                }
            }
        });
        Check.that(reach[0] < 0, "a quarter into its crawl the strike has not passed the middle (" + reach[0] + ")");
        // turned frames: XROT puts local +x where the entity faces
        double[] out = new double[3];
        BoltMesh.Frame.turned(0, 0, 0, Math.toRadians(-90.0 - 0.0)).getClass();
        double yaw = 90.0;
        BoltMesh.Frame frame = BoltMesh.Frame.turned(0, 0, 0, Math.toRadians(-90.0 - yaw));
        Bolt ahead = Bolt.crackled(new double[] {0, 1, 0}, new double[] {2, 1, 0}, 0.05F, 1.0F, 0xFFFF1433, 0, 10, 0.0F, 4L, 0.0);
        double[] far = {0, 0, 0};
        BoltMesh.build(ahead, frame, 2, pixel, (glow, v, c) -> {
            for (int k = 0; k < 4; k++) {
                if (Math.abs(v[k * 3]) > Math.abs(far[0])) {
                    far[0] = v[k * 3];
                    far[2] = v[k * 3 + 2];
                }
            }
        });
        Check.that(far[0] < -1.5 && Math.abs(far[2]) < 0.6, "an entity facing west (yaw 90) has its effect's +x to the west (" + far[0] + ", " + far[2] + ")");
    }

    /** 2.1.5: strokes of a beam, a chain's strike, free ends. */
    static void painter() {
        BoltPainter.Chain chain = new BoltPainter.Chain();
        double[] p0 = {0, 1, 0};
        double[] p1 = {2.6, 1.2, 0.4};
        double[] p2 = {5.1, 1.0, 1.0};
        List<Bolt> bolts = new ArrayList<>();
        float s0 = chain.start(p0, 0);
        BoltPainter.strokes(p0, p1, 0.05F, 0xFFFF1433, 14, s0, false, 1L, bolts);
        chain.land(p0, p1, s0, 14);
        float s1 = chain.start(p1, 0);
        Check.that(s1 > s0 && s1 <= s0 + 1.0F, "a piece that starts where another landed waits for it, not long (" + s1 + ")");
        BoltPainter.strokes(p1, p2, 0.05F, 0xFFFF1433, 14, s1, false, 1L, bolts);
        chain.land(p1, p2, s1, 14);
        Check.that(bolts.size() == 4, "a long-lived beam re-strikes once: two strokes each (" + bolts.size() + ")");
        Check.that(bolts.get(1).delay > bolts.get(0).delay && bolts.get(1).delay <= bolts.get(0).end(), "the re-strike lands as the first shape goes");
        BoltPainter.point(bolts);
        Check.that(bolts.get(0).widths[bolts.get(0).count() - 1] > 0.049F, "a joint inside a chain stays full");
        Bolt last = bolts.get(2);
        Check.that(last.widths[last.count() - 1] < 0.02F, "the chain's free end comes to a point");
        Check.that(BoltPainter.crawl(50, 30) <= 1.6F && BoltPainter.crawl(0.5, 30) >= 0.12F, "a strike is fast, never instant");
    }

    static double facing(double[] v, int i, int j, int k) {
        double ax = v[j * 3] - v[i * 3];
        double ay = v[j * 3 + 1] - v[i * 3 + 1];
        double az = v[j * 3 + 2] - v[i * 3 + 2];
        double bx = v[k * 3] - v[i * 3];
        double by = v[k * 3 + 1] - v[i * 3 + 1];
        double bz = v[k * 3 + 2] - v[i * 3 + 2];
        return -((ay * bz - az * by) * v[i * 3] + (az * bx - ax * bz) * v[i * 3 + 1] + (ax * by - ay * bx) * v[i * 3 + 2]);
    }

    /** 2.1.5: the Haki crackle off a katana. */
    static void crackle() {
        Check.that(DrawCrackle.set(5L, 3, 0.0, 1.28, Double.NaN, 0.0, 0xFFFF1433).isEmpty(), "no Haki strength, no crackle");
        List<Bolt> one = DrawCrackle.set(5L, 3, 0.0, 1.28, Double.NaN, 1.0, 0xFFFF1433);
        List<Bolt> two = DrawCrackle.set(5L, 3, 0.0, 1.28, Double.NaN, 1.0, 0xFFFF1433);
        Check.that(!one.isEmpty() && one.size() == two.size(), "a set is a pure function of its seed and window");
        for (int i = 0; i < one.size(); i++) {
            Check.that(java.util.Arrays.equals(one.get(i).points, two.get(i).points), "the same bolts every frame of the window");
        }
        Check.that(!DrawCrackle.set(5L, 4, 0.0, 1.28, Double.NaN, 1.0, 0xFFFF1433).isEmpty(), "every window has its set");
        boolean body = false;
        boolean edge = false;
        boolean other = false;
        for (Bolt bolt : one) {
            body |= bolt.kind == Bolt.BODY;
            edge |= bolt.kind == Bolt.EDGE;
            other |= bolt.kind != Bolt.BODY && bolt.kind != Bolt.EDGE;
            Check.that(bolt.end() <= DrawCrackle.WINDOW + DrawCrackle.LIFE + 2, "a crackle bolt burns briefly");
            for (int p = 0; p < bolt.count(); p++) {
                double y = bolt.points[p * 3 + 1];
                double off = Math.hypot(bolt.points[p * 3], bolt.points[p * 3 + 2]);
                Check.that(y > -0.2 && y < 1.5 && off < 0.4, "the crackle hugs the blade (" + y + ", " + off + ")");
            }
        }
        // 3.6.0 (the owner: "only 1 type and style of haki  model should be in use, THE CONQUEROR HAKI RELEASE"): the release's model
        Check.that(body && edge && !other, "the release's own model: a black body and its rim, nothing else");
        List<Bolt> mouth = DrawCrackle.set(9L, 1, 0.0, 0.3, 0.3, 1.0, 0xFFFF1433);
        boolean spark = false;
        for (Bolt bolt : mouth) {
            spark |= Math.abs(bolt.points[1] - 0.3) < 1e-9 && Math.hypot(bolt.points[bolt.points.length - 3], bolt.points[bolt.points.length - 1]) > 0.1;
        }
        Check.that(spark, "while the blade leaves the saya, sparks spit out of its mouth");
    }

    /** 2.1.6: a bolt striking the ground: the flash and the scorch. */
    static void landing() {
        int[] none = {0};
        BoltMesh.landing(0, -1.6, 3, -0.1, 0xFFFF1433, 1.0, 1.0, (glow, v, c) -> none[0]++);
        BoltMesh.landing(0, -1.6, 3, BoltMesh.SCORCH + 0.1, 0xFFFF1433, 1.0, 1.0, (glow, v, c) -> none[0]++);
        Check.that(none[0] == 0, "nothing before the strike or after the scorch is gone");
        int[] flash = {0, 0};
        double[] alpha = {0, 0};
        for (double age : new double[] {0.5, 30}) {
            int at = age < 1 ? 0 : 1;
            BoltMesh.landing(0.4, -1.6, 3, age, 0xFFFF1433, 1.0, 1.0, (glow, v, c) -> {
                Check.that(facing(v, 0, 1, 2) >= -1e-12 && facing(v, 0, 2, 3) >= -1e-12, "a strike's quads face the eye from above");
                if (glow) {
                    flash[at]++;
                } else {
                    alpha[at] = Math.max(alpha[at], ((c[0] >>> 24) & 255) / 255.0);
                }
            });
        }
        Check.that(flash[0] > 0 && flash[1] == 0, "the flash is brief");
        Check.that(alpha[0] > 0.6 && alpha[1] > 0 && alpha[1] < alpha[0] * 0.4, "the scorch lingers and fades (" + alpha[0] + ", " + alpha[1] + ")");
    }

    /** 2.1.7: the full-power storm: far, but a few huge bolts (the owner: "too much lightning way too much"). */
    static void storm() {
        Check.near(ConquerorStorm.reach(0), 60, 1e-9, "the storm reaches 60 blocks even at no mastery");
        Check.near(ConquerorStorm.reach(1000), 120, 1e-9, "and 120 at full mastery");
        for (int mastery : new int[] {0, 500, 1000}) {
            for (boolean supreme : new boolean[] {false, true}) {
                List<ConquerorStorm.Strand> strands = ConquerorStorm.release(mastery, 77L, supreme);
                List<ConquerorStorm.Strand> again = ConquerorStorm.release(mastery, 77L, supreme);
                Check.that(strands.size() == again.size(), "a storm is a pure function of its seed");
                double far = 0;
                int roots = 0;
                for (int s = 0; s < strands.size(); s++) {
                    ConquerorStorm.Strand strand = strands.get(s);
                    Check.that(java.util.Arrays.equals(strand.points(), again.get(s).points()), "the same seed, the same storm (the server's fissures run under it)");
                    Check.that(strand.delay() >= 0 && strand.duration() > 4 && strand.count() >= 3, "every strand strikes, and burns a while");
                    for (int i = 0; i < strand.count(); i++) {
                        Check.that(strand.points()[i * 3 + 1] >= 0.0, "every point is a height above the ground");
                        Check.that(!Double.isNaN(strand.points()[i * 3]) && !Double.isNaN(strand.points()[i * 3 + 2]), "no NaN in a storm");
                        Check.that(strand.widths()[i] > 0 && strand.widths()[i] < 6, "a ribbon, not a wall (" + strand.widths()[i] + ")");
                        far = Math.max(far, Math.hypot(Math.hypot(strand.points()[i * 3], strand.points()[i * 3 + 1]), strand.points()[i * 3 + 2]));
                    }
                    double cx = strand.points()[0];
                    double cy = strand.points()[1] - ConquerorStorm.CHEST;
                    double cz = strand.points()[2];
                    if (Math.sqrt(cx * cx + cy * cy + cz * cz) <= 2.3) {
                        roots++;
                        Check.that(strand.widths()[0] < strand.widths()[strand.count() / 3], "a bolt leaves the flash thin and swells");
                    }
                    for (Bolt bolt : ConquerorStorm.bolts(strand)) {
                        Check.that(bolt.kind == Bolt.BODY || bolt.kind == Bolt.EDGE, "a strand is a black body and its rim");
                    }
                }
                double reach = ConquerorStorm.reach(mastery);
                Check.that(far > reach * 0.65 && far < reach * 1.1, "the storm spreads out to its reach (" + far + " of " + reach + ")");
                int most = ConquerorStorm.count(mastery) + (supreme ? 1 : 0);
                Check.that(roots >= 7 && roots <= most, "seven to " + most + " huge bolts out of the flash, over the whole sphere (" + roots + ")");
            }
        }
        // 2.2.5: the fissures run under the strands that tear along the ground, open as the strike reaches them, and are a V
        for (long seed : new long[] {3L, 77L, 1234L}) {
            List<ConquerorStorm.Strand> storm = ConquerorStorm.release(1000, seed, true);
            List<com.hexhaki.piece.haki.Fissure.Cut> cuts = com.hexhaki.piece.haki.Fissure.cuts(storm, seed);
            Check.that(!cuts.isEmpty(), "a full-power storm tears the ground (seed " + seed + ")");
            int last = -1;
            for (com.hexhaki.piece.haki.Fissure.Cut cut : cuts) {
                Check.that(cut.tick() >= last, "the cuts open in order");
                last = cut.tick();
                Check.that(cut.half() > 0.1 && cut.half() <= com.hexhaki.piece.haki.Fissure.HALF_WIDTH * 1.25 + 1e-9
                    && cut.depth() > 0.3 && cut.depth() <= com.hexhaki.piece.haki.Fissure.DEPTH * 1.2 + 1e-9, "a cut is a tear, not a crater");
                Check.near(Math.hypot(cut.nx(), cut.nz()), 1.0, 1e-9, "a cut's across is a unit");
                Check.that(com.hexhaki.piece.haki.Fissure.depthAt(cut, 0) >= com.hexhaki.piece.haki.Fissure.depthAt(cut, cut.half() * 0.7)
                    && com.hexhaki.piece.haki.Fissure.depthAt(cut, cut.half() * 1.01) == 0, "deepest in its middle, nothing past its sides");
                Check.that(Math.hypot(cut.x(), cut.z()) < ConquerorStorm.reach(1000) * 1.15, "within the storm's reach");
            }
            Check.that(last < 40, "every fissure is open within two seconds (" + last + " ticks)");
        }
        // HexGhoul's repair ledger: each position remembered once, back in its own time, deferred and finished
        com.hexhaki.piece.haki.ScarLedger<String> ledger = new com.hexhaki.piece.haki.ScarLedger<>();
        Check.that(ledger.record(1L, "stone", 100) && !ledger.record(1L, "air", 50), "a second tear never overwrites the first state");
        Check.that(ledger.record(2L, "dirt", 80), "another position");
        Check.that(ledger.poll(79) == null, "nothing due before its time");
        com.hexhaki.piece.haki.ScarLedger.Scar<String> first = ledger.poll(80);
        Check.that(first != null && first.original.equals("dirt"), "the earliest comes first");
        ledger.defer(first, 120, true);
        com.hexhaki.piece.haki.ScarLedger.Scar<String> second = ledger.poll(120);
        Check.that(second != null && second.original.equals("stone"), "a deferred one waits its turn");
        ledger.finish(second);
        Check.that(ledger.size() == 1 && ledger.contains(2L) && !ledger.contains(1L), "a finished one is gone");
        Check.that(ledger.poll(120).retries() == 1, "a deferral counted");
        int[] counts = {0, 0};
        BoltMesh.ring(0, -1.6, 0, 6, 100, 0xFFFF1433, 1.0, (glow, v, c) -> {
            counts[glow ? 1 : 0]++;
            Check.that(facing(v, 0, 1, 2) >= -1e-12 && facing(v, 0, 2, 3) >= -1e-12, "the shockwave's quads face the eye");
        });
        Check.that(counts[0] > 0 && counts[1] > 0, "the shockwave has its ink and its light");
        int[] gone = {0};
        BoltMesh.ring(0, -1.6, 0, BoltMesh.WAVE + 0.5, 100, 0xFFFF1433, 1.0, (glow, v, c) -> gone[0]++);
        Check.that(gone[0] == 0, "the shockwave is gone once it has swept out");
    }

    /** 2.1.8: the lightning goes out over the whole sphere, fresh every time, no blind spots. */
    /**
     * 2.4.5 (the owner: the release's lightning "feels like the lightning is going always to the same spot"): every bolt of a release
     * that comes down strikes the ground at its own place: round the player in its own sector (never two within a third of a sector),
     * out at 30..95 % of the radius, never at the feet; the storm's strands that come down strike at least three blocks out; another
     * release, other places.
     */
    static void landings() {
        double radius = 50.0;
        java.util.Map<Long, Integer> spots = new java.util.HashMap<>();
        int[][] cells = new int[12][3];
        int strikes = 0;
        for (long seed = 1; seed <= 200; seed++) {
            int mains = ConquerorBolts.count(1.0F, 1000, true);
            List<double[]> bolts = ConquerorBolts.generate(1.0F, 1000, seed * 1000003L, true, radius);
            List<Double> angles = new java.util.ArrayList<>();
            for (int i = 0; i < mains; i++) {
                double[] p = bolts.get(i);
                for (int k = 1; k < p.length / 3; k++) {
                    double out = Math.hypot(p[k * 3], p[k * 3 + 2]);
                    if (p[k * 3 + 1] <= ConquerorBolts.FLOOR + 1e-6 && out > 3.0) {
                        Check.that(out >= radius * ConquerorBolts.LAND_NEAR - 1e-6 && out <= radius * ConquerorBolts.LAND_FAR + 1e-6,
                            "a release's bolt strikes the ground 30..95 % of the radius out (" + out + ")");
                        angles.add(Math.atan2(p[k * 3 + 2], p[k * 3]));
                        spots.merge(Math.round(p[k * 3] / 4) * 100003L + Math.round(p[k * 3 + 2] / 4), 1, Integer::sum);
                        double turn = Math.atan2(p[k * 3 + 2], p[k * 3]) + Math.PI;
                        double share = (out / radius - ConquerorBolts.LAND_NEAR) / (ConquerorBolts.LAND_FAR - ConquerorBolts.LAND_NEAR);
                        cells[Math.min(11, (int) (turn / (Math.PI * 2.0) * 12))][Math.max(0, Math.min(2, (int) (share * 3)))]++;
                        strikes++;
                        break;
                    }
                }
            }
            for (int a = 0; a < angles.size(); a++) {
                for (int b = a + 1; b < angles.size(); b++) {
                    double d = Math.abs(Math.IEEEremainder(angles.get(a) - angles.get(b), Math.PI * 2.0));
                    Check.that(d >= Math.PI * 2.0 / angles.size() * 0.39, "two strikes of one release are never in the same spot ("
                        + Math.toDegrees(d) + " degrees apart)");
                }
            }
            for (ConquerorStorm.Strand strand : ConquerorStorm.release(1000, seed * 1000003L, true)) {
                double[] p = strand.points();
                if (Math.hypot(p[0], p[2]) > 2.6 || strand.tear()[0] > 0) {
                    continue; // a branch
                }
                for (int k = 0; k < strand.count(); k++) {
                    if (strand.tear()[k] > 0) {
                        Check.that(Math.hypot(p[k * 3], p[k * 3 + 2]) >= 3.0, "a strand that comes down strikes away from the feet");
                        break;
                    }
                }
            }
        }
        Check.that(strikes > 400, "releases strike the ground often (" + strikes + ")");
        for (int sector = 0; sector < 12; sector++) {
            for (int ring = 0; ring < 3; ring++) {
                Check.that(cells[sector][ring] > 0, "over many releases every way round and every distance is struck");
            }
        }
        int most = 0;
        for (int count : spots.values()) {
            most = Math.max(most, count);
        }
        Check.that(most < strikes * 0.03, "no place is struck again and again (" + most + " of " + strikes + ")");
    }

    static void sphere() {
        for (int n : new int[] {3, 8, 13}) {
            for (long seed = 0; seed < 40; seed++) {
                double[] d = Sphere.directions(n, new java.util.SplittableRandom(seed));
                for (int i = 0; i < n; i++) {
                    Check.near(Math.sqrt(d[i * 3] * d[i * 3] + d[i * 3 + 1] * d[i * 3 + 1] + d[i * 3 + 2] * d[i * 3 + 2]), 1.0, 1e-9, "unit directions");
                }
                if (n >= 8) {
                    // no blind spot: every direction of the sphere is near one of the set
                    double worst = 1.0;
                    for (int a = 0; a < 24; a++) {
                        for (int b = 1; b < 12; b++) {
                            double y = Math.cos(Math.PI * b / 12.0);
                            double r = Math.sin(Math.PI * b / 12.0);
                            double[] probe = {Math.cos(Math.PI * a / 12.0) * r, y, Math.sin(Math.PI * a / 12.0) * r};
                            double best = -1;
                            for (int i = 0; i < n; i++) {
                                best = Math.max(best, probe[0] * d[i * 3] + probe[1] * d[i * 3 + 1] + probe[2] * d[i * 3 + 2]);
                            }
                            worst = Math.min(worst, best);
                        }
                    }
                    Check.that(worst > Math.cos(Math.toRadians(n >= 13 ? 62 : 75)),
                        "no part of the sphere is left far from a bolt (" + Math.toDegrees(Math.acos(worst)) + " degrees with " + n + ")");
                }
            }
        }
        // fresh every time, and over many releases every part of the sphere is struck
        double[] one = Sphere.directions(8, new java.util.SplittableRandom(1));
        double[] two = Sphere.directions(8, new java.util.SplittableRandom(2));
        Check.that(!java.util.Arrays.equals(one, two), "another release, another set");
        boolean[] octants = new boolean[8];
        for (long seed = 0; seed < 30; seed++) {
            double[] d = Sphere.directions(5, new java.util.SplittableRandom(seed * 7919));
            for (int i = 0; i < 5; i++) {
                octants[(d[i * 3] > 0 ? 1 : 0) + (d[i * 3 + 1] > 0 ? 2 : 0) + (d[i * 3 + 2] > 0 ? 4 : 0)] = true;
            }
        }
        for (boolean octant : octants) {
            Check.that(octant, "every part of the sphere gets struck over a few releases");
        }
        // the Conqueror's bolts: up, level and down over releases
        boolean up = false;
        boolean down = false;
        for (long seed = 0; seed < 20; seed++) {
            for (double[] bolt : ConquerorBolts.generate(1.0F, 1000, seed, true, 50)) {
                double y = bolt[bolt.length - 2];
                up |= y > 20;
                down |= y <= ConquerorBolts.FLOOR + 0.6;
            }
        }
        Check.that(up && down, "the Conqueror's lightning goes up into the sky and down onto the ground");
    }

    /** 2.2.4: every Haki lightning in its owner's Ryou colour: the black body stays black, the rest takes the colour, alpha kept. */
    static void tinted() {
        double[] points = {0, 0, 0, 1, 1, 1};
        float[] widths = {0.1F, 0.1F};
        int ryou = 0x2E9BFF;
        Bolt body = new Bolt(points, widths, 0xF0060008, Bolt.BODY, 0, 5, 0, 1L, 0);
        Bolt dark = body.tinted(ryou);
        Check.that(Bolt.kindOf(dark.color) == Bolt.BODY && (dark.color >>> 24) == 0xF0, "the black body stays black (a body, its alpha kept)");
        Check.that((dark.color & 0xFF) > ((dark.color >> 16) & 0xFF), "with a hint of the Ryou colour (its edge light)");
        Bolt core = new Bolt(points, widths, 0xFFFF1E10, Bolt.CORE, 0, 5, 0, 1L, 0).tinted(ryou);
        Check.that(core.color == (0xFF000000 | ryou), "a red core becomes the Ryou colour");
        Bolt aura = new Bolt(points, widths, 0x582A0008, Bolt.AURA, 0, 5, 0, 1L, 0).tinted(ryou);
        Check.that(aura.color == (0x58000000 | ryou), "an aura takes the colour as faint as it was");
        Bolt heart = new Bolt(points, widths, 0xFFFFE0D0, Bolt.HEART, 0, 5, 0, 1L, 0).tinted(ryou);
        Check.that((heart.color >>> 24) == 0xFF && (heart.color & 0xFF) >= (ryou & 0xFF) && ((heart.color >> 16) & 0xFF) > ((ryou >> 16) & 0xFF),
            "a hot heart is the colour whitened");
        Check.that(core.points == points && core.seed == 1L, "the bolt itself is unchanged");
    }

    /**
     * 2.6.0 (the owner: "Conqueror sounds reduced by 35% but the line evil voice line increased by a ton ... after the key is let go, add a
     * proper explosion sound (FROM THE WEB)" and "cut the voice line (evil) so that as soon as the voice ends, the sound ends"): the
     * mix's numbers, the explosion's and the roar's sounds.json entries, the voice lines' length.
     */
    static void conquerorMix() {
        Check.near(com.hexhaki.piece.haki.ConquerorMix.QUIETER, 0.65, 1e-6, "the original's Conqueror sounds 35 % quieter");
        Check.near(com.hexhaki.piece.haki.ConquerorMix.loudness(1F, false), 1.0, 1e-6, "a full release explodes at full loudness");
        Check.near(com.hexhaki.piece.haki.ConquerorMix.volume(1F, false), 4.0, 1e-6, "sent at 4: heard 64 blocks away");
        float last = 0F;
        for (int charge = 4; charge < 100; charge++) {
            float loud = com.hexhaki.piece.haki.ConquerorMix.loudness(charge / 100F, false);
            Check.that(loud >= last && loud < 1F, "a partial release explodes louder the more it was charged (" + charge + ")");
            Check.that(com.hexhaki.piece.haki.ConquerorMix.volume(charge / 100F, false) > 1F, "and is heard past 16 blocks (" + charge + ")");
            last = loud;
        }
        Check.that(com.hexhaki.piece.haki.ConquerorMix.loudness(1F, true) < com.hexhaki.piece.haki.ConquerorMix.loudness(0.04F, false),
            "a tap's explosion is the smallest");
        Check.that(com.hexhaki.piece.haki.ConquerorMix.pitch(1F, false) < com.hexhaki.piece.haki.ConquerorMix.pitch(0.5F, false)
            && com.hexhaki.piece.haki.ConquerorMix.pitch(0.5F, false) < com.hexhaki.piece.haki.ConquerorMix.pitch(0F, true),
            "the bigger the release, the deeper its explosion");
        String sounds = read("src/main/resources/assets/hexhaki/sounds.json");
        String explosion = entry(sounds, "\"" + com.hexhaki.piece.haki.ConquerorMix.EXPLOSION + "\"");
        Check.that(explosion.contains("\"hexhaki:piece/conqueror/web/boom\""), "the explosion's sound event plays its web recording");
        Check.that(explosion.contains("\"volume\": " + (1.0 / com.hexhaki.piece.haki.ConquerorMix.SCALE)),
            "its sounds.json volume is 1 / SCALE (sent at SCALE x its loudness: reach, never louder)");
        for (String gender : new String[] {"male", "female"}) {
            String roar = entry(sounds, "\"piece.conqueror.evil_release_" + gender + "\"");
            Check.that(roar.contains("\"volume\": 0.40625"), "the evil roar (sent at 1.6) plays at 0.65 (" + gender + ")");
            Check.that(roar.contains("\"attenuation_distance\": 77"), "and fades over the same 77 blocks as before (" + gender + ")");
            Check.that(!entry(sounds, "\"piece.conqueror.evil_voice_" + gender + "\"").contains("\"volume\""),
                "the voice line is not lowered (" + gender + ")");
        }
        java.io.File dir = new java.io.File("src/main/resources/assets/hexhaki/sounds/piece/conqueror");
        // 2.9.4: the booms are web recordings now (sounds/piece/conqueror/web), one a path
        for (String boom : new String[] {"boom", "boom_good", "boom_evil"}) {
            Check.that(new java.io.File(dir, "web/" + boom + ".ogg").length() > 10_000, "the " + boom + " file is there");
        }
        for (String gender : new String[] {"male", "female"}) {
            double seconds = oggSeconds(new java.io.File(dir, "evil_voice_" + gender + ".ogg"));
            Check.that(seconds > 1.1 && seconds < 1.3, "the voice line ends with the voice (" + gender + ": " + seconds + " s)");
        }
        double boom = oggSeconds(new java.io.File(dir, "web/boom.ogg"));
        Check.that(boom > 2.5 && boom < 7.0, "the boom rolls away for a few seconds (" + boom + " s)");
        // 2.9.5 (the owner: "it keeps playing like way long after you let go of the key"): the original's charge sound (played five
        // times a charge, each a second late) is silent; the charge is one held sound of ours, a build-up and then a loop
        Check.that(entry(sounds, "\"conqueror_charge\"").contains("\"sounds\": []"), "the original's charge event plays nothing");
        Check.that(entry(sounds, "\"" + com.hexhaki.piece.haki.ConquerorMix.CHARGE + "\"").contains("\"hexhaki:piece/conqueror/web/charge\""),
            "the held charge's build-up is its web recording");
        Check.that(entry(sounds, "\"" + com.hexhaki.piece.haki.ConquerorMix.CHARGE_HOLD + "\"")
            .contains("\"hexhaki:piece/conqueror/web/charge_hold\""), "and its hold the loop cut from it");
        double build = oggSeconds(new java.io.File(dir, "web/charge.ogg"));
        double hold = oggSeconds(new java.io.File(dir, "web/charge_hold.ogg"));
        int holdAt = com.hexhaki.piece.client.haki.ConquerorChargeSound.HOLD_AT;
        Check.that(holdAt / 20.0 < build - 0.4 && holdAt / 20.0 > build - 1.5,
            "the loop takes over under the build-up's last second (" + holdAt / 20.0 + " s of " + build + " s)");
        Check.that(hold > 1.5 && hold < 4.0, "the loop is a few seconds round (" + hold + " s)");
        Check.that(com.hexhaki.piece.server.ConquerorAura.CHARGE > com.hexhaki.piece.server.ConquerorAura.BLADE
            && com.hexhaki.piece.server.ConquerorAura.CHARGE_END == com.hexhaki.piece.server.ConquerorAura.CHARGE + 1,
            "the charge's messages are the Conqueror's kinds (S2CUoFx: 80 and on)");
    }

    /**
     * 3.6.0 (the owner: "all lightning sounds are to change to new more bass boosted ones (get from the web) and be lowered in volume
     * (ONLY the lightning sounds), Full COnqueror on releasing should not play all the lightning sounds at once, just one").
     */
    static void lightningSounds() {
        String sounds = read("src/main/resources/assets/hexhaki/sounds.json");
        String[][] events = {
            {"conqueror_release", "strike1", "strike2", "strike3"},
            {"armament_ambient_zap", "zap1", "zap2", "zap3"},
            {"last_stand_lightning", "thunder"},
        };
        java.io.File web = new java.io.File("src/main/resources/assets/hexhaki/sounds/piece/lightning/web");
        String credits = read(new java.io.File(web, "SOURCES.txt").getPath());
        for (String[] event : events) {
            String entry = entry(sounds, "\"" + event[0] + "\"");
            int count = entry.split("\"name\"", -1).length - 1;
            Check.that(count == event.length - 1, event[0] + " plays only the new recordings (" + count + ")");
            for (int i = 1; i < event.length; i++) {
                Check.that(entry.contains("\"hexhaki:piece/lightning/web/" + event[i] + "\""), event[0] + " plays " + event[i]);
                Check.that(credits.contains(event[i] + ".ogg: ") && credits.contains("(CC0 1.0)"), event[i] + " is a credited CC0 recording");
                double seconds = oggSeconds(new java.io.File(web, event[i] + ".ogg"));
                long rings = com.hexhaki.piece.haki.LightningGate.LENGTH[com.hexhaki.piece.haki.LightningGate.rank("hexhaki", event[0])];
                Check.that(Math.abs(seconds * 1000.0 - rings) <= 60.0, event[i] + " rings as long as the gate holds (" + seconds + " s)");
            }
            java.util.regex.Matcher volume = java.util.regex.Pattern.compile("\"volume\": ([0-9.]+)").matcher(entry);
            int volumes = 0;
            while (volume.find()) {
                double v = Double.parseDouble(volume.group(1));
                Check.that(v > 0.3 && v < 0.8, event[0] + "'s recordings are lowered (" + v + ")");
                volumes++;
            }
            Check.that(volumes == event.length - 1, event[0] + ": every recording has its own lowered volume");
        }
        // the booms and every other sound keep theirs (the owner: "ONLY the lightning sounds")
        Check.that(entry(sounds, "\"" + com.hexhaki.piece.haki.ConquerorMix.EXPLOSION + "\"").contains("\"volume\": 0.25"),
            "the release's explosion is as loud as before");
        // one lightning sound at a time
        com.hexhaki.piece.haki.LightningGate gate = new com.hexhaki.piece.haki.LightningGate();
        int zap = com.hexhaki.piece.haki.LightningGate.ZAP;
        int thunder = com.hexhaki.piece.haki.LightningGate.THUNDER;
        int strike = com.hexhaki.piece.haki.LightningGate.STRIKE;
        Check.that(com.hexhaki.piece.haki.LightningGate.rank("hexhaki", "conqueror_release") == strike
            && com.hexhaki.piece.haki.LightningGate.rank("hexhaki", "armament_ambient_zap") == zap
            && com.hexhaki.piece.haki.LightningGate.rank("hexhaki", "last_stand_lightning") == thunder, "the three lightning events ranked");
        Check.that(com.hexhaki.piece.haki.LightningGate.rank("hexhaki", "piece.conqueror.explosion") == 0
            && com.hexhaki.piece.haki.LightningGate.rank("minecraft", "conqueror_release") == 0
            && com.hexhaki.piece.haki.LightningGate.rank("hexhaki", "conqueror_supreme_blast") == 0, "nothing else is held back");
        Check.that(gate.allow(0, 0L) && gate.allow(0, 0L), "a sound that is not lightning always plays");
        Check.that(gate.allow(zap, 1000L), "a zap plays");
        Check.that(!gate.allow(zap, 1200L) && !gate.allow(zap, 1600L), "a second zap under it does not");
        Check.that(gate.allow(zap, 1700L), "once it has rung out the next one does");
        Check.that(gate.allow(strike, 1800L), "the release's strike plays over a zap");
        for (long at = 1900L; at < 1800L + 4000L; at += 100L) {
            Check.that(!gate.allow(zap, at) && !gate.allow(thunder, at) && !gate.allow(strike, at),
                "while the strike rings, no other lightning sound starts (" + at + " ms)");
        }
        Check.that(gate.allow(thunder, 5800L) && !gate.allow(zap, 6000L), "after it, a thunder; a zap not under the thunder");
        Check.that(com.hexhaki.piece.server.ConquerorRelease.FULL_STRIKE > 1F,
            "the full release's one strike is asked at least as loud as a partial release's");
    }

    static String read(String path) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path)), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException failure) {
            Check.that(false, path + " unreadable");
            return "";
        }
    }

    /** The JSON object after {@code key} in {@code text} (to its closing brace at the same depth). */
    static String entry(String text, String key) {
        int at = text.indexOf(key);
        Check.that(at >= 0, key + " is in sounds.json");
        if (at < 0) {
            return "";
        }
        int open = text.indexOf('{', at);
        int depth = 0;
        for (int i = open; i < text.length(); i++) {
            char c = text.charAt(i);
            depth += c == '{' ? 1 : c == '}' ? -1 : 0;
            if (depth == 0) {
                return text.substring(open, i + 1);
            }
        }
        return text.substring(open);
    }

    /** An Ogg Vorbis file's length in seconds: its last page's granule position over the identification header's sample rate. */
    static double oggSeconds(java.io.File file) {
        try {
            byte[] b = java.nio.file.Files.readAllBytes(file.toPath());
            int vorbis = indexOf(b, new byte[] {1, 'v', 'o', 'r', 'b', 'i', 's'}, 0);
            long rate = (b[vorbis + 12] & 0xFFL) | (b[vorbis + 13] & 0xFFL) << 8 | (b[vorbis + 14] & 0xFFL) << 16 | (b[vorbis + 15] & 0xFFL) << 24;
            int last = -1;
            for (int i = indexOf(b, new byte[] {'O', 'g', 'g', 'S'}, 0); i >= 0; i = indexOf(b, new byte[] {'O', 'g', 'g', 'S'}, i + 1)) {
                last = i;
            }
            long granule = 0;
            for (int k = 7; k >= 0; k--) {
                granule = granule << 8 | (b[last + 6 + k] & 0xFFL);
            }
            return granule / (double) rate;
        } catch (java.io.IOException | RuntimeException failure) {
            Check.that(false, file + " unreadable: " + failure);
            return -1;
        }
    }

    static int indexOf(byte[] data, byte[] what, int from) {
        outer:
        for (int i = from; i <= data.length - what.length; i++) {
            for (int j = 0; j < what.length; j++) {
                if (data[i + j] != what[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    /** 2.4.4 (the owner: a Haki stun makes any entity "actually fall down ... and stay stunned"): every body falls, lies, gets up. */
    /** 2.4.9: the Conqueror's release by alignment: who it fells, for how long (2.6.0: by the hold), whom it hurts; the storm's strikes. */
    static void conquerorStun() {
        com.hexhaki.piece.haki.ConquerorStun.class.getName();
        int good = 100;
        int evil = -100;
        int neutral = 0;
        // 2.6.0 (the owner: "tap G = 5 seconds stun, hold G (min) = 10 seconds, max hold G = 20 seconds, Max hold G good alignment = 30
        // seconds")
        Check.that(com.hexhaki.piece.haki.ConquerorStun.TAP == 100, "a tap: 5 seconds");
        int shortest = Math.max(4, (int) Math.floor((com.hexhaki.piece.haki.ConquerorStun.TAP_TICKS + 1) * 0.625)); // the original's charge
        for (int alignment : new int[] {good, evil, neutral}) {
            Check.that(com.hexhaki.piece.haki.ConquerorStun.held(alignment, shortest / 100F, 0) == 200,
                "the shortest hold: 10 seconds (alignment " + alignment + ")");
            Check.that(com.hexhaki.piece.haki.ConquerorStun.held(alignment, 0.04F, 0) == 200, "never under 10 seconds");
        }
        Check.that(com.hexhaki.piece.haki.ConquerorStun.held(evil, 1F, 100) == 400, "evil: 20 seconds at the longest hold");
        Check.that(com.hexhaki.piece.haki.ConquerorStun.held(neutral, 1F, 100) == 400, "neutral: 20 seconds at the longest hold");
        Check.that(com.hexhaki.piece.haki.ConquerorStun.held(good, 1F, 100) == 600, "good: 30 seconds at the longest hold");
        Check.that(com.hexhaki.piece.haki.ConquerorStun.held(good, 1F, 4000) == 600, "never past the most, however long it is held");
        Check.that(com.hexhaki.piece.haki.ConquerorStun.held(evil, 0.6F, 400) == com.hexhaki.piece.haki.ConquerorStun.held(evil, 0.6F, 0),
            "time held counts at full charge only");
        int last = -1;
        // every tick of the hold, from just past a tap to the charge held full RAMP ticks: never shorter, never a jump over 3 ticks
        for (int ticks = com.hexhaki.piece.haki.ConquerorStun.TAP_TICKS + 1; ticks <= 160 + 120; ticks++) {
            int charge = Math.max(4, Math.min(100, (int) Math.floor(Math.min(ticks, 160) * 0.625)));
            int stun = com.hexhaki.piece.haki.ConquerorStun.held(good, charge / 100F, Math.max(0, ticks - 160));
            Check.that(stun >= last, "the longer G is held, the longer the stun (" + ticks + " ticks)");
            Check.that(last < 0 || stun - last <= 3, "the stun grows evenly (" + ticks + " ticks: " + last + " -> " + stun + ")");
            last = stun;
        }
        Check.that(com.hexhaki.piece.haki.ConquerorStun.fellsAll(evil, true), "an evil full charge fells everything in its radius");
        Check.that(!com.hexhaki.piece.haki.ConquerorStun.fellsAll(evil, false), "an evil partial one does not");
        Check.that(!com.hexhaki.piece.haki.ConquerorStun.fellsAll(good, true) && !com.hexhaki.piece.haki.ConquerorStun.fellsAll(neutral, true),
            "good and neutral fell hostile mobs and players only");
        // 2.5.1: each alignment's charge and releases: every name it can play is a file (tools/conqueror_anims.py), upper body only
        for (int alignment : new int[] {good, evil, neutral}) {
            for (String name : new String[] {"conqueror_gear5_charge", "supreme_conqueror_release", "conqueror_gear5_release"}) {
                String chosen = com.hexhaki.piece.haki.ConquerorStun.animation(name, alignment);
                java.io.File file = new java.io.File("src/main/resources/assets/hexhaki/player_animation/" + chosen + ".json");
                Check.that(file.isFile(), chosen + " exists (" + name + ", alignment " + alignment + ")");
                try {
                    String text = new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
                    Check.that(!text.contains("Leg\""), chosen + ": the legs are left to the game");
                } catch (java.io.IOException failure) {
                    Check.that(false, chosen + " unreadable");
                }
            }
        }
        Check.that(com.hexhaki.piece.haki.ConquerorStun.animation("conqueror_gear5_charge", good).equals("conqueror_good_charge")
            && com.hexhaki.piece.haki.ConquerorStun.animation("supreme_conqueror_release", evil).equals("conqueror_evil_release")
            && com.hexhaki.piece.haki.ConquerorStun.animation("conqueror_tap_point", evil).equals("conqueror_tap_point"),
            "good and evil have their own; the tap is everyone's");
        Check.that(!com.hexhaki.piece.haki.ConquerorStun.hurts(good), "a good release hurts no one");
        Check.that(com.hexhaki.piece.haki.ConquerorStun.hurts(evil) && com.hexhaki.piece.haki.ConquerorStun.hurts(neutral),
            "evil and neutral releases hurt");
        // the owner: "half the current HP of the mob or person hit. Always half, but even if the player or entity has half a heart, then
        // it doesn't deal any damage. It's not meant to kill."
        Check.near(com.hexhaki.piece.haki.ConquerorStun.evilDamage(20F), 10F, 1e-6, "half of a full player's health");
        Check.near(com.hexhaki.piece.haki.ConquerorStun.evilDamage(7F), 3.5F, 1e-6, "half of what is left");
        Check.near(com.hexhaki.piece.haki.ConquerorStun.evilDamage(1F), 0F, 1e-6, "nothing at half a heart");
        Check.near(com.hexhaki.piece.haki.ConquerorStun.evilDamage(0.5F), 0F, 1e-6, "nothing under it");
        float health = 20F;
        for (int release = 0; release < 50; release++) {
            health -= com.hexhaki.piece.haki.ConquerorStun.evilDamage(health);
            Check.that(health > 0F, "it never kills (release " + release + ", " + health + " left)");
        }
        // the storm's strikes: a strand along the ground strikes a body on it, not one beside it nor one under it in the air
        java.util.List<com.hexhaki.piece.haki.ConquerorStorm.Strand> line = java.util.List.of(new com.hexhaki.piece.haki.ConquerorStorm.Strand(
            new double[] {0, 0.2, 0, 40, 0.2, 0}, new float[] {0.6F, 0.6F}, new double[] {1, 1}, 0F, 20, 40.0, 1L));
        Check.that(com.hexhaki.piece.haki.ConquerorStorm.strikes(line, 20, 0.3, 0, 1.8, 0.9), "a strand on the ground strikes a body on it");
        Check.that(!com.hexhaki.piece.haki.ConquerorStorm.strikes(line, 20, 4, 0, 1.8, 0.9), "not one four blocks to the side");
        Check.that(!com.hexhaki.piece.haki.ConquerorStorm.strikes(line, 50, 0, 0, 1.8, 0.9), "not one past its end");
        Check.that(!com.hexhaki.piece.haki.ConquerorStorm.strikes(line, 20, 0, 6, 7.8, 0.9), "not one flying high over it");
        int struck = 0;
        for (long seed = 1; seed <= 40; seed++) {
            java.util.List<com.hexhaki.piece.haki.ConquerorStorm.Strand> storm = com.hexhaki.piece.haki.ConquerorStorm.release(1000, seed, true);
            for (int a = 0; a < 36; a++) {
                double d = 25.0;
                struck += com.hexhaki.piece.haki.ConquerorStorm.strikes(storm, Math.cos(a * Math.PI / 18) * d, Math.sin(a * Math.PI / 18) * d, 0, 1.8,
                    0.9) ? 1 : 0;
            }
        }
        Check.that(struck > 0 && struck < 40 * 36, "the storm strikes some bodies round it and misses others (" + struck + " of " + 40 * 36 + ")");
    }

    /** 2.5.2: strokes wholly outside the view are not built; one in it, or round the eye, always is; a heart builds nothing. */
    static void culled() {
        Check.that(BoltMesh.seen(0, 0, -10, 1, 0, 0, -1), "straight ahead: seen");
        Check.that(!BoltMesh.seen(0, 0, 10, 1, 0, 0, -1), "straight behind: not built");
        Check.that(BoltMesh.seen(0, 0, 3, 5, 0, 0, -1), "behind but round the eye: seen");
        Check.that(BoltMesh.seen(10, 0, -1, 1, 0, 0, -1), "far to the side, a little ahead: seen (wider than any view)");
        Check.that(!BoltMesh.seen(10, 0, 4, 1, 0, 0, -1), "to the side and behind: not built");
        // every point a bolt is drawn at lies in its sphere, danced or not
        Bolt bolt = new Bolt(new double[] {0, 0, 0, 1, 2, 0, 3, 1, 1, 6, 0, 2}, new float[] {0.2F, 0.2F, 0.15F, 0.1F}, 0xFFFF0000, Bolt.CORE, 0F,
            12, 0F, 42L, 0.3);
        double[] b = bolt.bounds();
        double[] at = new double[3];
        for (double age = 0; age < 12; age += 0.5) {
            for (int i = 0; i < bolt.count(); i++) {
                bolt.dancing(i, age, at);
                double d = Math.sqrt(Math.pow(at[0] - b[0], 2) + Math.pow(at[1] - b[1], 2) + Math.pow(at[2] - b[2], 2));
                Check.that(d <= b[3], "a danced point stays inside the bolt's sphere");
            }
        }
        // 2.5.2: the dance made without a SplittableRandom a point a frame is exactly the one made with it
        java.util.Random draw = new java.util.Random(7);
        for (int trial = 0; trial < 2000; trial++) {
            long seed = draw.nextLong();
            int i = 1 + draw.nextInt(2);
            double age = draw.nextDouble() * 30;
            Bolt d = new Bolt(bolt.points, bolt.widths, bolt.color, bolt.kind, 0F, 12, 0F, seed, 0.3);
            d.dancing(i, age, at);
            long frame = (long) Math.floor(Math.max(0.0, age) / 2.0);
            java.util.SplittableRandom random = new java.util.SplittableRandom(seed * 0x9E3779B97F4A7C15L + i * 0x632BE59BD9B4E019L
                + frame * 0x85EBCA77C2B2AE63L);
            double px = d.points[(i + 1) * 3] - d.points[(i - 1) * 3];
            double py = d.points[(i + 1) * 3 + 1] - d.points[(i - 1) * 3 + 1];
            double pz = d.points[(i + 1) * 3 + 2] - d.points[(i - 1) * 3 + 2];
            double piece = Math.sqrt(px * px + py * py + pz * pz) * 0.5;
            double[] across = com.hexhaki.piece.haki.Jag.across(px, py, pz);
            double u = random.nextDouble(-1.0, 1.0) * 0.3 * piece;
            double v = random.nextDouble(-1.0, 1.0) * 0.3 * piece;
            Check.near(at[0], d.points[i * 3] + across[0] * u + across[3] * v, 1e-12, "the same dance (x)");
            Check.near(at[1], d.points[i * 3 + 1] + across[1] * u + across[4] * v, 1e-12, "the same dance (y)");
            Check.near(at[2], d.points[i * 3 + 2] + across[2] * u + across[5] * v, 1e-12, "the same dance (z)");
        }
        int[] quads = {0};
        BoltMesh.build(new Bolt(bolt.points, bolt.widths, 0xFFFFFFFF, Bolt.HEART, 0F, 12, 0F, 42L, 0.3), BoltMesh.Frame.at(0, 0, -5), 3, 0.001,
            (glow, xyz, argb) -> quads[0]++);
        Check.that(quads[0] == 0, "a heart builds nothing");
    }

    /** 2.5.1: a stunned player's own view goes over with them: to the sky backwards, the ground forwards, the world on its side sideways. */
    static void stunView() {
        double lie = com.hexhaki.piece.haki.StunFall.LIE;
        double deg = Math.toDegrees(lie);
        // facing +z (yaw 0): backwards is -z, forwards +z, their right is -x
        double[] back = com.hexhaki.piece.haki.StunFall.view(lie, 0, -1, 0);
        Check.near(back[1], -deg, 1e-9, "over backwards: looking up at the sky");
        Check.near(back[0], 0, 1e-9, "over backwards: not rolled");
        double[] front = com.hexhaki.piece.haki.StunFall.view(lie, 0, 1, 0);
        Check.near(front[1], deg, 1e-9, "over forwards: looking at the ground");
        double[] right = com.hexhaki.piece.haki.StunFall.view(lie, -1, 0, 0);
        Check.near(right[0], deg, 1e-9, "over to the right: rolled right");
        Check.near(right[1], 0, 1e-9, "over to the right: not tipped");
        // facing -x (yaw 90): their right is -z
        double[] turned = com.hexhaki.piece.haki.StunFall.view(lie, 0, -1, 90);
        Check.near(turned[0], deg, 1e-9, "the roll follows the way they faced as they fell");
        Check.near(com.hexhaki.piece.haki.StunFall.view(0, 0, -1, 0)[1], 0, 1e-12, "upright: the view as it is");
    }

    static void stunFall() {
        com.hexhaki.piece.haki.StunFall.Frame start = com.hexhaki.piece.haki.StunFall.at(0, 40, 1L);
        Check.that(start.angle() == 0 && start.slide() == 0, "upright the instant the stun lands");
        double last = -1;
        for (double t = 0; t <= com.hexhaki.piece.haki.StunFall.FALL_TICKS; t += 0.25) {
            double a = com.hexhaki.piece.haki.StunFall.at(t, 40, 1L).angle();
            Check.that(a >= last, "it falls over, never back, while it falls");
            last = a;
        }
        com.hexhaki.piece.haki.StunFall.Frame lying = com.hexhaki.piece.haki.StunFall.at(25, 40, 1L);
        Check.that(lying.angle() > Math.toRadians(70) && lying.angle() < Math.toRadians(95) && lying.flat() > 0.9,
            "it lies down, nearly flat, not through the ground");
        Check.that(lying.slide() > 0 && lying.slide() <= 0.5, "a small fall back: slid a little, not thrown");
        Check.that(Math.abs(lying.head()) > 0.2, "its head lolled to one side");
        for (double t = com.hexhaki.piece.haki.StunFall.FALL_TICKS + com.hexhaki.piece.haki.StunFall.SETTLE_TICKS; t < 40; t += 0.5) {
            Check.that(com.hexhaki.piece.haki.StunFall.at(t, 40, 7L).angle() > Math.toRadians(70), "it stays down while it is stunned");
        }
        Check.that(com.hexhaki.piece.haki.StunFall.at(40 + com.hexhaki.piece.haki.StunFall.GETUP_TICKS * 0.5, 40, 1L).angle() < lying.angle(),
            "it gets up once the stun is over");
        Check.that(com.hexhaki.piece.haki.StunFall.over(40 + com.hexhaki.piece.haki.StunFall.GETUP_TICKS, 40)
            && !com.hexhaki.piece.haki.StunFall.over(39, 40), "and is done once it is up");
        Check.that(com.hexhaki.piece.haki.StunFall.down(10) == com.hexhaki.piece.haki.StunFall.MIN_DOWN
            && com.hexhaki.piece.haki.StunFall.down(200) == 200, "down a while at least, longer for a longer stun");
        // the way it falls: mostly away from the user, some to a side, never toward the user; any way when no user is known
        int away = 0;
        int sideways = 0;
        for (int i = 0; i < 400; i++) {
            double r1 = (i % 20) / 20.0 + 0.01;
            double r2 = (i / 20) / 20.0 + 0.01;
            double[] d = com.hexhaki.piece.haki.StunFall.direction(0, 1, r1, r2);
            Check.near(Math.hypot(d[0], d[1]), 1.0, 1e-9, "a way on the ground");
            Check.that(d[1] > -0.35, "never back toward the one who let it out");
            if (d[1] > Math.cos(Math.toRadians(36))) {
                away++;
            } else {
                sideways++;
            }
        }
        Check.that(away > sideways * 2 && sideways > 0, "usually straight away, now and then to one side");
        double[] any = com.hexhaki.piece.haki.StunFall.direction(0, 0, 0.3, 0.6);
        Check.near(Math.hypot(any[0], any[1]), 1.0, 1e-9, "some way even when no one is known");
        for (long seed = 0; seed < 50; seed++) {
            for (double t = 0; t < 60; t += 0.5) {
                com.hexhaki.piece.haki.StunFall.Frame f = com.hexhaki.piece.haki.StunFall.at(t, 40, seed);
                Check.that(Double.isFinite(f.angle()) && f.angle() >= -1e-9 && f.angle() < Math.toRadians(95), "a fall no further than flat");
            }
        }
    }
    /**
     * 3.6.3 (the owner: "the conquerors haki is the 3d lightning, yet the AI keeps putting the 2d ... make the lightning 3d once it for
     * all, all of it", and "the tip of the lightning has to be sharp"): the storm's rim stroke is one round lit tube (its quads shaded
     * across, not bands of one colour), its body stroke adds nothing under it, and every free end comes to a point.
     */
    static void solid3d() {
        com.hexhaki.piece.haki.HakiColour[] sets = com.hexhaki.piece.haki.HakiColour.values();
        for (com.hexhaki.piece.haki.HakiColour set : sets) {
            java.util.List<com.hexhaki.piece.haki.Bolt> blast = com.hexhaki.piece.haki.ConquerorStorm.blast(4.0, 10, -1.2, 14L, 1.0);
            int solid = 0;
            java.util.Set<Integer> shades = new java.util.HashSet<>();
            int bodies = 0;
            for (com.hexhaki.piece.haki.Bolt raw : blast) {
                com.hexhaki.piece.haki.Bolt bolt = raw.tinted(set.rim);
                double[] q = bolt.points.clone();
                for (int i = 0; i < q.length; i += 3) {
                    q[i + 2] += 6.0;
                }
                com.hexhaki.piece.haki.Bolt placed = new com.hexhaki.piece.haki.Bolt(q, bolt.widths, bolt.color, bolt.kind, bolt.delay,
                    bolt.duration, bolt.crawl, bolt.seed, bolt.dance);
                int[] count = {0};
                com.hexhaki.piece.haki.BoltMesh.build(placed, com.hexhaki.piece.haki.BoltMesh.Frame.at(0, -1.0, 0), 4.0, 0.0013, (glow, xyz, argb) -> {
                    if (!glow) {
                        count[0]++;
                        for (int c : argb) {
                            shades.add(c & 0xF0F0F0);
                        }
                    }
                });
                if (bolt.kind == com.hexhaki.piece.haki.Bolt.BODY) {
                    bodies += count[0];
                } else {
                    solid += count[0];
                }
                Check.that(bolt.widths[bolt.widths.length - 1] == 0.0F, set.display + ": a blast's bolt ends in a sharp point");
                Check.that(bolt.widths[0] == 0.0F, set.display + ": a blast's bolt leaves the blast as a point");
            }
            Check.that(solid > 60, set.display + ": the blast's bolts are built as tubes (" + solid + " quads)");
            Check.that(bodies == 0, set.display + ": the storm's body stroke is drawn whole by its rim stroke (nothing under it)");
            Check.that(shades.size() > 12, set.display + ": the tubes are shaded round (" + shades.size() + " shades), not flat bands");
        }
        java.util.List<com.hexhaki.piece.haki.Bolt> strike = com.hexhaki.piece.haki.ConquerorStorm.strike(new double[] {0, 1, 0},
            new double[] {3, 2, 1}, 0.2F, 0.3F, 0xFF1A2E, 0, 6, 0, 9L);
        for (com.hexhaki.piece.haki.Bolt bolt : strike) {
            Check.that(bolt.widths[bolt.widths.length - 1] == 0.0F, "a strike (sparks, a style blow's crackle) ends in a sharp point");
        }
        java.util.List<com.hexhaki.piece.haki.Bolt> chained = com.hexhaki.piece.haki.ConquerorStorm.strike(new double[] {0, 1, 0},
            new double[] {3, 2, 1}, 0.2F, 0.3F, 0xFF1A2E, 0, 6, 0, 9L, false);
        Check.that(chained.get(0).widths[chained.get(0).widths.length - 1] > 0.0F, "a piece of a chain keeps its end full for the next");
        float[] w = com.hexhaki.piece.haki.ConquerorStorm.sharpen(new double[] {0, 0, 0, 1, 0, 0, 2, 0, 0, 3, 0, 0}, new float[] {0.4F, 0.4F, 0.4F, 0.4F},
            true, true);
        Check.that(w[0] == 0.0F && w[3] == 0.0F && w[1] > 0.0F && w[2] > 0.0F, "sharpen points both ends and keeps the middle");
    }
}
