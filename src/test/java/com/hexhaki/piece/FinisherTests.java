package com.hexhaki.piece;

import com.hexhaki.piece.sword.Carcass;
import com.hexhaki.piece.sword.CutPose;
import com.hexhaki.piece.sword.Finisher;
import com.hexhaki.piece.sword.Ragdoll;
import java.util.ArrayList;
import java.util.List;

/**
 * 2.2.1: the finisher taken from HexGhoul and turned upright: the line's pace, the cut (two halves, each the body's own skin exactly,
 * each cut faced with what is inside, the whole section covered), the line round the cut, and the halves' fall coming to rest on the
 * ground, each on its own side.
 */
public final class FinisherTests {
    static final double PX = 0.9375 / 16.0;

    public static void main(String[] args) {
        pace();
        box();
        cut();
        coronal();
        section();
        line();
        fall();
        insides();
        Check.done("FinisherTests");
    }

    static void pace() {
        Check.near(Finisher.round(0), 0.0, 1e-12, "the line has not started at the stun's start");
        Check.near(Finisher.round(Finisher.STUN), 1.0, 1e-12, "the line meets itself when the stun ends");
        Check.near(Finisher.round(Finisher.STUN * 3), 1.0, 1e-12, "and stays met");
        double last = -1;
        for (int t = 0; t <= Finisher.STUN; t++) {
            double r = Finisher.round(t);
            Check.that(r >= last, "the line never goes back (" + t + ")");
            last = r;
        }
        Check.that(Finisher.STUN == 40, "two seconds' stun, as HexGhoul's");
    }

    static void box() {
        double[] start = Finisher.box(0, 0.9, 0.3);
        Check.near(start[0], 0.0, 1e-12, "the band's walk starts in the middle of the front's height");
        Check.near(start[1], 0.3, 1e-12, "at the front");
        double[] last = start;
        for (int i = 1; i <= 400; i++) {
            double[] p = Finisher.box(i / 400.0, 0.9, 0.3);
            Check.that(Math.hypot(p[0] - last[0], p[1] - last[1]) < 4.8 / 400 * 1.01 + 1e-9, "the walk is even and unbroken (" + i + ")");
            last = p;
        }
        List<double[][]> quads = new ArrayList<>();
        Finisher.line((a, b, c, d, n, ca, cb, cc, cd) -> quads.add(new double[][] {a, b, c, d}), 0.0, 0.4, 1.0, 0.005, 0xFFFF0000);
        Check.that(quads.isEmpty(), "no line round a mob's box before it starts");
        Finisher.line((a, b, c, d, n, ca, cb, cc, cd) -> quads.add(new double[][] {a, b, c, d}), 1.0, 0.4, 1.0, 0.005, 0xFFFF0000);
        Check.that(quads.size() == 96, "the whole way round a mob's box at the end");
        for (double[][] q : quads) {
            for (double[] p : q) {
                Check.that(Math.abs(p[0]) <= 0.005 + 1e-12 && p[1] >= -1e-9 && p[1] <= 1.0 + 1e-9 && Math.abs(p[2]) <= 0.4 + 1e-9,
                    "a mob's line lies on its upright middle, round its box");
            }
        }
    }

    static List<Carcass.Piece> faced(CutPose pose) {
        return Carcass.cut(pose, Carcass.Layout.PLAYER_WIDE, 0.9375, Finisher.Inside.BODY, Carcass.Plane.faced());
    }

    static void cut() {
        List<Carcass.Piece> pieces = faced(new CutPose());
        Check.that(pieces.size() == 2, "a body cut down the middle falls in two");
        Carcass.Piece left = pieces.get(0);
        Carcass.Piece right = pieces.get(1);
        Check.that(left.kind == Carcass.Kind.PLUS && right.kind == Carcass.Kind.MINUS, "its left half and its right half");
        for (Carcass.Face f : left.faces) {
            for (double[] p : f.p()) {
                Check.that(p[0] >= -0.36 * PX, "the left half's skin is all on its left (its leg whole, a hair over the middle)");
            }
        }
        for (Carcass.Face f : right.faces) {
            for (double[] p : f.p()) {
                Check.that(p[0] <= 0.36 * PX, "the right half's skin is all on its right");
            }
        }
        // the skin is all there: the two halves' faces cover exactly every cube's six faces (heads, hat, trunk, jacket, limbs, sleeves)
        double whole = (384 + 486 + 352 + 401.5 + 448 + 531 + 448 + 531) * PX * PX;
        double halves = area(left.faces) + area(right.faces);
        Check.near(halves, whole, 1e-9, "the halves' skin is the whole body's skin, nothing lost or doubled");
        double across = 0;
        for (Carcass.Piece piece : Carcass.cut(new CutPose(), Carcass.Layout.PLAYER_WIDE, 0.9375, Finisher.Inside.BODY,
            Carcass.Plane.from(new double[] {0.6, 0, 0.8}))) {
            across += area(piece.faces);
        }
        Check.near(across, whole, 1e-9, "and so it is cut at a slant");
        // the cut parts: head and trunk; each arm and leg goes whole to its own side
        Check.that(reaches(left, 7.0 * PX) && reaches(right, -7.0 * PX), "each half keeps its own arm");
        Check.that(left.boxes.size() == 4 && right.boxes.size() == 4, "each half falls as its head, trunk, arm and leg");
        for (Carcass.Piece piece : pieces) {
            Check.that(!piece.caps.isEmpty(), "each half's cut shows what is inside");
            for (Carcass.Face f : piece.faces) {
                for (double[] uv : f.uv()) {
                    Check.that(uv[0] >= -1e-9 && uv[0] <= 1 + 1e-9 && uv[1] >= -1e-9 && uv[1] <= 1 + 1e-9, "every corner takes a point of the skin");
                }
            }
        }
        // a part the plane barely grazes is not cut: the legs (0.1 px over the middle) go whole
        int legsCut = 0;
        for (Carcass.Cap cap : left.caps) {
            for (double[] p : cap.p()) {
                if (p[1] < 0.7) {
                    legsCut++;
                }
            }
        }
        Check.that(legsCut == 0, "the legs are not shaved: no cut below the crotch");
    }

    static boolean reaches(Carcass.Piece piece, double x) {
        for (Carcass.Face f : piece.faces) {
            for (double[] p : f.p()) {
                if (x > 0 ? p[0] >= x : p[0] <= x) {
                    return true;
                }
            }
        }
        return false;
    }

    static double area(List<Carcass.Face> faces) {
        double sum = 0;
        for (Carcass.Face f : faces) {
            sum += quadArea(f.p());
        }
        return sum;
    }

    static double quadArea(double[][] p) {
        return tri(p[0], p[1], p[2]) + tri(p[0], p[2], p[3]);
    }

    static double tri(double[] a, double[] b, double[] c) {
        double[] u = {b[0] - a[0], b[1] - a[1], b[2] - a[2]};
        double[] v = {c[0] - a[0], c[1] - a[1], c[2] - a[2]};
        double x = u[1] * v[2] - u[2] * v[1];
        double y = u[2] * v[0] - u[0] * v[2];
        double z = u[0] * v[1] - u[1] * v[0];
        return Math.sqrt(x * x + y * y + z * z) * 0.5;
    }

    static void coronal() {
        // the blade from the side: the plane runs front to back across the body, every part cut down its length
        Carcass.Plane plane = Carcass.Plane.from(new double[] {1, 0, 0});
        Check.near(plane.n()[2], -1.0, 1e-12, "a blade from the side cuts front from back");
        List<Carcass.Piece> pieces = Carcass.cut(new CutPose(), Carcass.Layout.PLAYER_WIDE, 0.9375, Finisher.Inside.BODY, plane);
        Check.that(pieces.size() == 2, "front and back halves");
        Check.that(pieces.get(0).boxes.size() == 6 && pieces.get(1).boxes.size() == 6, "every part cut down its length: six boxes each");
        double[][] launch = Carcass.launch(Carcass.Kind.PLUS, plane);
        Check.that(launch[0][2] < 0 && launch[0][1] > 0, "the half behind the plane slides back off the cut");
        double[][] faced = Carcass.launch(Carcass.Kind.PLUS, Carcass.Plane.faced());
        Check.that(faced[0][0] > 0 && faced[1][2] < 0, "faced, the left half moves left and tips over to its left");
        double[][] other = Carcass.launch(Carcass.Kind.MINUS, Carcass.Plane.faced());
        Check.that(other[0][0] < 0 && other[1][2] > 0, "and the right half to its right");
    }

    static void section() {
        List<Carcass.Piece> pieces = faced(new CutPose());
        // the cells' tops cover the whole section: the head's 8 x 8 and the trunk's 12 x 4 pixels, on each half
        double want = (64 + 48) * PX * PX;
        for (Carcass.Piece piece : pieces) {
            double tops = 0;
            for (Carcass.Cap cap : piece.caps) {
                if (Math.abs(Math.abs(cap.n()[0]) - 1.0) < 1e-6) {
                    tops += quadArea(cap.p());
                }
            }
            Check.near(tops, want, want * 0.005, "the cut is covered, every cell of it (" + piece.kind + ")");
            boolean out = true;
            for (Carcass.Cap cap : piece.caps) {
                for (double[] p : cap.p()) {
                    double into = piece.kind == Carcass.Kind.PLUS ? -p[0] : p[0];
                    out &= into >= -1e-9 && into <= 0.7 * PX;
                }
            }
            Check.that(out, "what is inside stands out of the cut, toward the other half, never more than 0.7 px");
        }
    }

    static void line() {
        CutPose pose = new CutPose();
        Carcass.Plane plane = Carcass.Plane.faced();
        double[] middle = Carcass.centre(pose, 0.9375, plane);
        Check.near(middle[0], 0.0, 1e-12, "the line turns about the trunk's middle, on the cut");
        double last = 0;
        for (double amount : new double[] {0.05, 0.25, 0.5, 0.75, 1.0}) {
            List<double[][]> quads = new ArrayList<>();
            Carcass.wound(pose, Carcass.Layout.PLAYER_WIDE, 0.9375, amount, Carcass.FLAT_LAYER, plane,
                (a, b, c, d, n, ca, cb, cc, cd) -> quads.add(new double[][] {a, b, c, d}));
            double length = 0;
            for (double[][] q : quads) {
                for (double[] p : q) {
                    Check.that(Math.abs(p[0]) <= Carcass.LINE * PX + 1e-9, "the line is a hair's width either side of the cut");
                    double f = Math.abs(Math.atan2(p[1] - middle[1], p[2] - middle[2])) / Math.PI;
                    Check.that(f <= amount + 0.05, "drawn only as far round as it has gone (" + amount + ")");
                }
                length += Math.hypot(q[1][1] - q[0][1], q[1][2] - q[0][2]);
            }
            Check.that(length > last, "the line grows as it goes round (" + amount + ")");
            last = length;
            if (amount == 0.05) {
                for (double[][] q : quads) {
                    Check.that(q[0][2] > 0 && Math.abs(q[0][1] - middle[1]) < 0.15, "it starts at the middle of the chest's front");
                }
            }
        }
        // the whole loop: up the front, over the head, down the back, and between the legs: about the body's outline once
        Check.that(last > 3.2 && last < 4.2, "at the end it runs round the whole body once, up the front, over the head, down the back, under the trunk (" + last + ")");
        Check.near(Finisher.round(10), 0.22, 0.02, "half a second in, a fifth of the way");
    }

    static void fall() {
        for (boolean step : new boolean[] {false, true}) {
            CutPose pose = new CutPose().set("rightArm", -5, 2, 0, -0.12, 0, 0.08).set("leftArm", 5, 2, 0, 0.1, 0, -0.08);
            Carcass.Plane plane = Carcass.Plane.faced();
            List<Carcass.Piece> pieces = Carcass.cut(pose, Carcass.Layout.PLAYER_WIDE, 0.9375, Finisher.Inside.BODY, plane);
            List<Ragdoll.Body> bodies = new ArrayList<>();
            for (Carcass.Piece piece : pieces) {
                Ragdoll.Body b = new Ragdoll.Body(piece.boxes.toArray(new double[0][]), new double[] {0.5, 64, 0.5}, new double[] {1, 0, 0, 0});
                double[][] launch = Carcass.launch(piece.kind, plane);
                System.arraycopy(launch[0], 0, b.v, 0, 3);
                System.arraycopy(launch[1], 0, b.w, 0, 3);
                bodies.add(b);
            }
            double floor = 64;
            Ragdoll.Terrain terrain = (a, b) -> {
                double best = Double.MAX_VALUE;
                double[][] boxes = step ? new double[][] {{-60, floor - 1, -60, 60, floor, 60}, {1.2, floor, -1.5, 3.5, floor + 0.5, 2.5}}
                    : new double[][] {{-60, floor - 1, -60, 60, floor, 60}};
                for (double[] box : boxes) {
                    double t0 = 0;
                    double t1 = 1;
                    boolean miss = false;
                    for (int k = 0; k < 3 && !miss; k++) {
                        double d = b[k] - a[k];
                        if (Math.abs(d) < 1e-12) {
                            miss = a[k] < box[k] || a[k] > box[k + 3];
                            continue;
                        }
                        double ta = (box[k] - a[k]) / d;
                        double tb = (box[k + 3] - a[k]) / d;
                        t0 = Math.max(t0, Math.min(ta, tb));
                        t1 = Math.min(t1, Math.max(ta, tb));
                        miss = t0 > t1;
                    }
                    if (!miss) {
                        best = Math.min(best, t0);
                    }
                }
                return best == Double.MAX_VALUE ? null : new double[] {a[0] + (b[0] - a[0]) * best, a[1] + (b[1] - a[1]) * best, a[2] + (b[2] - a[2]) * best};
            };
            int t = 0;
            while (t < 400 && !(bodies.get(0).asleep && bodies.get(1).asleep)) {
                Ragdoll.tick(bodies, terrain);
                t++;
            }
            Check.that(bodies.get(0).asleep && bodies.get(1).asleep, "both halves come to rest (" + (step ? "on a step" : "flat") + ", " + t + " ticks)");
            Check.that(bodies.get(0).pos[0] > 0.5 + 0.3 && bodies.get(1).pos[0] < 0.5 - 0.3, "each half falls to its own side");
            for (Ragdoll.Body b : bodies) {
                Check.that(b.pos[1] > floor && b.pos[1] < floor + 1.0, "a half lies on the ground, not in it nor floating");
                for (double[] box : b.boxes) {
                    for (int k = 0; k < 8; k++) {
                        double[] c = b.toWorld(new double[] {(k & 1) == 0 ? box[0] : box[3], (k & 2) == 0 ? box[1] : box[4], (k & 4) == 0 ? box[2] : box[5]});
                        Check.that(c[1] > floor - 0.03, "no corner sunk into the ground");
                    }
                }
                Check.that(Math.hypot(b.pos[0] - 0.5, b.pos[2] - 0.5) < 2.2, "it lands close by, a body's length at most");
            }
        }
    }

    static void insides() {
        double[] tf = {-4, 0, -2};
        double[] ts = {8, 12, 4};
        Finisher.Cell skin = Finisher.inside(Finisher.Inside.BODY, "torso", new double[] {0, 6, -1.9}, tf, ts, 0.5);
        Check.that(skin.height() < 0.06, "skin at the trunk's front rim, flat");
        Finisher.Cell spine = Finisher.inside(Finisher.Inside.BODY, "torso", new double[] {0, 6.8, 0.1}, tf, ts, 0.5);
        Check.that(((spine.colour() >> 16) & 255) > 200 && ((spine.colour() >> 8) & 255) > 170, "the spine is pale bone down the back's middle");
        Check.that(spine.height() > 0.45, "and stands proud of the cut");
        Finisher.Cell canal = Finisher.inside(Finisher.Inside.BODY, "torso", new double[] {0.5, 6, 0.75}, tf, ts, 0.5);
        Check.that(((canal.colour() >> 16) & 255) < 120, "the canal behind it is dark");
        Finisher.Cell brain = Finisher.inside(Finisher.Inside.BODY, "head", new double[] {0, -6, 0}, new double[] {-4, -8, -4}, new double[] {8, 8, 8}, 0.3);
        Check.that(((brain.colour() >> 16) & 255) > 120, "the head holds a brain, pink-grey");
        Finisher.Cell skull = Finisher.inside(Finisher.Inside.BODY, "head", new double[] {0, -7.4, 0}, new double[] {-4, -8, -4}, new double[] {8, 8, 8}, 0.3);
        Check.that(skull.height() > 0.4, "under the crown's skin, the skull");
        Finisher.Cell bone = Finisher.inside(Finisher.Inside.BODY, "rightArm", new double[] {-1, 4, 0.65}, new double[] {-3, -2, -2}, new double[] {4, 12, 4}, 0.2);
        Check.that(bone.height() > 0.4, "a limb's bone in its middle, whichever way it is cut");
        Finisher.Cell meat = Finisher.inside(Finisher.Inside.MEAT, "torso", new double[] {0, 6, 0}, tf, ts, 0.5);
        Check.that(((meat.colour() >> 16) & 255) > ((meat.colour() >> 8) & 255) * 2, "meat is red");
        Finisher.Cell dry = Finisher.inside(Finisher.Inside.DRY, "torso", new double[] {0, 6, 0}, tf, ts, 0.2);
        Check.that(((dry.colour() >> 8) & 255) > 150, "what doesn't bleed is dry bone inside");
    }
}
