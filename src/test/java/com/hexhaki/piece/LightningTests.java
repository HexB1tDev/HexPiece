package com.hexhaki.piece;

import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.BoltMesh;
import com.hexhaki.piece.haki.BoltPainter;
import com.hexhaki.piece.haki.ConquerorBolts;
import com.hexhaki.piece.haki.DrawCrackle;
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

    static void conqueror() {
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
                        Check.that(points >= 3 && points <= ConquerorBolts.MOST_POINTS, "a main bolt has 3.." + ConquerorBolts.MOST_POINTS + " points (" + points + ")");
                        double rootOut = Math.hypot(bolt[0], bolt[2]);
                        Check.that(rootOut >= 0.87 && rootOut <= 1.17, "it starts at the body (" + rootOut + ")");
                        Check.that(bolt[1] >= 0.29 && bolt[1] <= 1.49, "waist to shoulder high");
                        double tipOut = Math.hypot(bolt[bolt.length - 3], bolt[bolt.length - 1]);
                        Check.that(tipOut >= radius * 0.81 && tipOut <= radius * 1.001, "it reaches the Conqueror radius (" + tipOut + " of " + radius + ")");
                        Check.that(bolt[bolt.length - 2] >= 0.15 && bolt[bolt.length - 2] <= 2.55, "its tip stays near the ground");
                        for (int p = 0; p < points; p++) {
                            Check.that(bolt[p * 3 + 1] >= ConquerorBolts.FLOOR - 1e-9 && bolt[p * 3 + 1] <= 2.55 + ConquerorBolts.MOST_LIFT + 1.0,
                                "a bolt skims the ground: never below it, never far above (" + bolt[p * 3 + 1] + ")");
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
        boolean core = false;
        boolean heart = false;
        for (Bolt bolt : one) {
            body |= bolt.kind == Bolt.BODY;
            core |= bolt.kind == Bolt.CORE;
            heart |= bolt.kind == Bolt.HEART;
            Check.that(bolt.end() <= DrawCrackle.WINDOW + DrawCrackle.LIFE + 2, "a crackle bolt burns briefly");
            for (int p = 0; p < bolt.count(); p++) {
                double y = bolt.points[p * 3 + 1];
                double off = Math.hypot(bolt.points[p * 3], bolt.points[p * 3 + 2]);
                Check.that(y > -0.2 && y < 1.5 && off < 0.4, "the crackle hugs the blade (" + y + ", " + off + ")");
            }
        }
        Check.that(body && core && heart, "inked like the Conqueror's: black body, coloured core, hot heart");
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
}
