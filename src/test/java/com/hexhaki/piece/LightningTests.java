package com.hexhaki.piece;

import com.hexhaki.piece.haki.ConquerorBolts;
import com.hexhaki.piece.haki.Jag;
import java.util.List;

/** 2.1.1: the Haki lightning's shapes (Jag) and the Conqueror's bolts keep the original's contract. */
public final class LightningTests {
    public static void main(String[] args) {
        jag();
        conqueror();
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
}
