package com.hexhaki.piece.sword;

import java.util.ArrayList;
import java.util.List;

/**
 * 2.2.1, HexGhoul 4.2.9's carcass turned upright (the owner: "the slice in half should 100% be taken from Hex Ghoul ... the cut and
 * everything about it. Just make sure instead of horizontal, it's vertical"): a body the finisher cut through (pure, tested). The cut is
 * an upright plane along the blade's path through the body's middle ({@link Plane}): faced, it runs from the crown down through the
 * face, the chest and the belly to the crotch, so the body falls apart in two halves, each with its own arm and leg (a part the plane
 * passes through is cut with it: an arm held across the body, a leg). Each half is the body's own boxes, textured as vanilla textures
 * them (the same corners, the same UV of the same skin), cut where the plane crosses them with what is inside showing on each cut
 * ({@link Finisher#inside}), and the boxes it is made of for its fall ({@link Ragdoll}).
 *
 * <p>Space: blocks, x to the body's left, y up from its feet, z its front ({@link CutPose}).
 */
public final class Carcass {
    /** The halves: the one on the plane's normal side, and the other. */
    public enum Kind {
        PLUS, MINUS
    }

    /** How a skin is laid out: a player's (wide or slim arms, with the second layer), a zombie's (64 square, a hat only), a skeleton's (64 by 32, thin limbs). */
    public enum Layout {
        PLAYER_WIDE(64, 64, 4, true), PLAYER_SLIM(64, 64, 3, true), HUMANOID(64, 64, 4, false), THIN(64, 32, 2, false);

        final int width;
        final int height;
        final int arm;
        final boolean layers;

        Layout(int width, int height, int arm, boolean layers) {
            this.width = width;
            this.height = height;
            this.arm = arm;
            this.layers = layers;
        }
    }

    /**
     * The cut: upright, through the body's middle (its feet's point), along {@code start} (horizontal, unit; the side the blade came in
     * from, where the line starts), {@code n} its normal ({@code up x start}).
     */
    public record Plane(double[] n, double[] start) {
        /** The cut along the blade, which came in from {@code start} (the finisher's space; any length, made level and unit). */
        public static Plane from(double[] start) {
            double x = start[0];
            double z = start[2];
            double l = Math.sqrt(x * x + z * z);
            double[] s = l < 1.0E-9 ? new double[] {0.0, 0.0, 1.0} : new double[] {x / l, 0.0, z / l};
            return new Plane(new double[] {s[2], 0.0, -s[0]}, s);
        }

        /** Faced: down the middle from the crown to the crotch, the line starting at the chest's front. */
        public static Plane faced() {
            return from(new double[] {0.0, 0.0, 1.0});
        }

        /** How far {@code p} lies on its normal's side (blocks). */
        public double distance(double[] p) {
            return p[0] * this.n[0] + p[1] * this.n[1] + p[2] * this.n[2];
        }
    }

    /** A textured quad of a piece: its corners (blocks), their texture coordinates (0..1) and its facing. */
    public record Face(double[][] p, double[][] uv, double[] n) {
    }

    /** A coloured quad of a cut (what is inside). */
    public record Cap(double[][] p, int[] argb, double[] n) {
    }

    /** A piece: its textured quads, its cuts, and the boxes it falls as (blocks: {min x, y, z, max x, y, z}). */
    public static final class Piece {
        public final Kind kind;
        public final List<Face> faces = new ArrayList<>();
        public final List<Cap> caps = new ArrayList<>();
        public final List<double[]> boxes = new ArrayList<>();

        Piece(Kind kind) {
            this.kind = kind;
        }
    }

    /** One cube of a part: its corner and size (part pixels), where its texture starts, how much it is blown up (the second layer), mirrored. */
    record Cube(String part, double x, double y, double z, double w, double h, double d, int u, int v, double grow, boolean mirror, boolean solid) {
        double[] from() {
            return new double[] {this.x, this.y, this.z};
        }

        double[] size() {
            return new double[] {this.w, this.h, this.d};
        }
    }

    /** A part the plane passes through by less than this (pixels) on one side goes whole to the other: no slivers. */
    static final double MARGIN = 1.0;
    /** The cut faces' cells (pixels a side). */
    static final double CELL = 1.0 / 3.0;

    private Carcass() {
    }

    /**
     * The body in {@code pose} cut through by {@code plane}: its halves, laid out as {@code layout}, {@code scale} the renderer's (a player's
     * 0.9375, most mobs 1), {@code inside} what it shows inside.
     */
    public static List<Piece> cut(CutPose pose, Layout layout, double scale, Finisher.Inside inside, Plane plane) {
        Piece plus = new Piece(Kind.PLUS);
        Piece minus = new Piece(Kind.MINUS);
        List<Cube> cubes = cubes(layout);
        for (Cube cube : cubes) {
            int side = side(pose, solid(cubes, cube.part()), scale, plane);
            add(side, plus, minus, cube, pose, layout, scale, plane, inside);
        }
        List<Piece> out = new ArrayList<>();
        for (Piece piece : List.of(plus, minus)) {
            if (!piece.faces.isEmpty()) {
                out.add(piece);
            }
        }
        return out;
    }

    /** The cubes of a body laid out as {@code layout}, vanilla's: its parts and (a player's) their second layer. */
    static List<Cube> cubes(Layout layout) {
        List<Cube> out = new ArrayList<>();
        int a = layout.arm;
        out.add(new Cube("head", -4, -8, -4, 8, 8, 8, 0, 0, 0.0, false, true));
        out.add(new Cube("head", -4, -8, -4, 8, 8, 8, 32, 0, 0.5, false, false));
        out.add(new Cube("torso", -4, 0, -2, 8, 12, 4, 16, 16, 0.0, false, true));
        if (layout == Layout.THIN) {
            out.add(new Cube("rightArm", -1, -2, -1, 2, 12, 2, 40, 16, 0.0, false, true));
            out.add(new Cube("leftArm", -1, -2, -1, 2, 12, 2, 40, 16, 0.0, true, true));
            out.add(new Cube("rightLeg", -1, 0, -1, 2, 12, 2, 0, 16, 0.0, false, true));
            out.add(new Cube("leftLeg", -1, 0, -1, 2, 12, 2, 0, 16, 0.0, true, true));
            return out;
        }
        out.add(new Cube("rightArm", a == 3 ? -2 : -3, -2, -2, a, 12, 4, 40, 16, 0.0, false, true));
        if (layout == Layout.HUMANOID) {
            // Vanilla's plain humanoid (a zombie's): its left limbs wear its right ones' texture, mirrored (only a player's skin has its own).
            out.add(new Cube("leftArm", -1, -2, -2, a, 12, 4, 40, 16, 0.0, true, true));
            out.add(new Cube("rightLeg", -2, 0, -2, 4, 12, 4, 0, 16, 0.0, false, true));
            out.add(new Cube("leftLeg", -2, 0, -2, 4, 12, 4, 0, 16, 0.0, true, true));
            return out;
        }
        out.add(new Cube("leftArm", -1, -2, -2, a, 12, 4, 32, 48, 0.0, false, true));
        out.add(new Cube("rightLeg", -2, 0, -2, 4, 12, 4, 0, 16, 0.0, false, true));
        out.add(new Cube("leftLeg", -2, 0, -2, 4, 12, 4, 16, 48, 0.0, false, true));
        if (layout.layers) {
            out.add(new Cube("torso", -4, 0, -2, 8, 12, 4, 16, 32, 0.25, false, false));
            out.add(new Cube("rightArm", a == 3 ? -2 : -3, -2, -2, a, 12, 4, 40, 32, 0.25, false, false));
            out.add(new Cube("leftArm", -1, -2, -2, a, 12, 4, 48, 48, 0.25, false, false));
            out.add(new Cube("rightLeg", -2, 0, -2, 4, 12, 4, 0, 32, 0.25, false, false));
            out.add(new Cube("leftLeg", -2, 0, -2, 4, 12, 4, 0, 48, 0.25, false, false));
        }
        return out;
    }

    static Cube solid(List<Cube> cubes, String part) {
        for (Cube cube : cubes) {
            if (cube.solid() && cube.part().equals(part)) {
                return cube;
            }
        }
        throw new IllegalArgumentException(part);
    }

    /** Which side {@code cube}'s part goes to: +1 the plane's normal side, -1 the other, 0 cut (the plane runs through it, past {@link #MARGIN}). */
    static int side(CutPose pose, Cube cube, double scale, Plane plane) {
        double lo = Double.MAX_VALUE;
        double hi = -Double.MAX_VALUE;
        for (double[] c : corners(cube, 0.0)) {
            double d = plane.distance(pose.place(cube.part(), c, scale));
            lo = Math.min(lo, d);
            hi = Math.max(hi, d);
        }
        double margin = MARGIN / 16.0 * scale;
        if (lo < -margin && hi > margin) {
            return 0;
        }
        return lo + hi >= 0.0 ? 1 : -1;
    }

    /** A cube's eight corners (part pixels), blown up by {@code grow}. */
    static double[][] corners(Cube cube, double grow) {
        double[][] out = new double[8][];
        for (int k = 0; k < 8; k++) {
            out[k] = new double[] {(k & 1) == 0 ? cube.x() - grow : cube.x() + cube.w() + grow, (k & 2) == 0 ? cube.y() - grow : cube.y() + cube.h() + grow,
                (k & 4) == 0 ? cube.z() - grow : cube.z() + cube.d() + grow};
        }
        return out;
    }

    /** Cube {@code cube} into the half {@code side} names, or (cut) its two parts into both, the cut faced with what is inside on both (its solid cube). */
    static void add(int side, Piece plus, Piece minus, Cube cube, CutPose pose, Layout layout, double scale, Plane plane, Finisher.Inside inside) {
        double g = cube.grow();
        for (double[][][] face : faces(cube, cube.x() - g, cube.y() - g, cube.z() - g, cube.x() + cube.w() + g, cube.y() + cube.h() + g,
            cube.z() + cube.d() + g, layout)) {
            double[][] p = new double[4][];
            for (int k = 0; k < 4; k++) {
                p[k] = pose.place(cube.part(), face[0][k], scale);
            }
            double[] n = unit(cross(sub(p[1], p[0]), sub(p[2], p[0])));
            if (side != 0) {
                emit(side > 0 ? plus : minus, p, face[1], n);
                continue;
            }
            emit(plus, clip(p, face[1], plane, true), n);
            emit(minus, clip(p, face[1], plane, false), n);
        }
        if (!cube.solid()) {
            return;
        }
        double[][] placed = new double[8][];
        double[][] local = corners(cube, 0.0);
        for (int k = 0; k < 8; k++) {
            placed[k] = pose.place(cube.part(), local[k], scale);
        }
        if (side != 0) {
            (side > 0 ? plus : minus).boxes.add(bounds(List.of(placed)));
            return;
        }
        // Each half's box: its own corners and where the plane crosses the cube's edges.
        List<double[]> above = new ArrayList<>();
        List<double[]> below = new ArrayList<>();
        for (double[] p : placed) {
            (plane.distance(p) >= 0.0 ? above : below).add(p);
        }
        for (double[] p : crossing(placed, plane)) {
            above.add(p);
            below.add(p);
        }
        plus.boxes.add(bounds(above));
        minus.boxes.add(bounds(below));
        section(plus, minus, pose, cube, scale, plane, inside);
    }

    /** The cube's twelve edges (corner indices). */
    static final int[][] EDGES = {{0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

    /** Where the plane crosses the edges of the box with corners {@code c} (as {@link #corners} orders them). */
    static List<double[]> crossing(double[][] c, Plane plane) {
        List<double[]> out = new ArrayList<>();
        for (int[] e : EDGES) {
            double da = plane.distance(c[e[0]]);
            double db = plane.distance(c[e[1]]);
            if ((da < 0.0) != (db < 0.0)) {
                double t = da / (da - db);
                out.add(lerp(c[e[0]], c[e[1]], t));
            }
        }
        return out;
    }

    static double[] bounds(List<double[]> points) {
        double[] b = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for (double[] p : points) {
            for (int i = 0; i < 3; i++) {
                b[i] = Math.min(b[i], p[i]);
                b[i + 3] = Math.max(b[i + 3], p[i]);
            }
        }
        return b;
    }

    /** A face (placed) into {@code piece}: a quad as it is; a triangle or a five-cornered cut face as a fan of quads, each a triangle with its last corner twice. */
    static void emit(Piece piece, double[][] p, double[][] uv, double[] n) {
        if (p == null || p.length < 3) {
            return;
        }
        if (p.length == 4) {
            piece.faces.add(new Face(p, uv, n));
            return;
        }
        for (int k = 1; k + 1 < p.length; k++) {
            piece.faces.add(new Face(new double[][] {p[0], p[k], p[k + 1], p[k + 1]}, new double[][] {uv[0], uv[k], uv[k + 1], uv[k + 1]}, n));
        }
    }

    static void emit(Piece piece, double[][][] clipped, double[] n) {
        if (clipped != null) {
            emit(piece, clipped[0], clipped[1], n);
        }
    }

    /** The part of a placed face on the plane's normal side ({@code plus}) or the other, its texture coordinates cut with it; null if none is. */
    static double[][][] clip(double[][] p, double[][] uv, Plane plane, boolean plus) {
        List<double[]> outP = new ArrayList<>();
        List<double[]> outUv = new ArrayList<>();
        int n = p.length;
        for (int k = 0; k < n; k++) {
            double[] a = p[k];
            double[] b = p[(k + 1) % n];
            double da = plane.distance(a) * (plus ? 1 : -1);
            double db = plane.distance(b) * (plus ? 1 : -1);
            if (da >= 0.0) {
                outP.add(a);
                outUv.add(uv[k]);
            }
            if ((da >= 0.0) != (db >= 0.0)) {
                double t = da / (da - db);
                outP.add(lerp(a, b, t));
                outUv.add(lerp(uv[k], uv[(k + 1) % n], t));
            }
        }
        if (outP.size() < 3) {
            return null;
        }
        return new double[][][] {outP.toArray(new double[0][]), outUv.toArray(new double[0][])};
    }

    // ------------------------------------------------------------------ the cut faces

    /**
     * The cut through a solid cube, both faces: the plane's section of the cube in cells {@link #CELL} pixels a side, each cell's colour and
     * height what is inside there ({@link Finisher#inside}), standing out of the cut (raised blocks, their sides down to whichever is lower
     * beside them, the cut itself at the rim), on each half facing the other.
     */
    static void section(Piece plus, Piece minus, CutPose pose, Cube cube, double scale, Plane plane, Finisher.Inside inside) {
        Frame f = new Frame(pose, cube.part(), scale);
        // The plane in the part's own pixels: its normal and how far it is from the part's pivot.
        double[] nl = new double[3];
        for (int i = 0; i < 3; i++) {
            nl[i] = dot(plane.n(), f.columns[i]);
        }
        double dl = -plane.distance(f.origin);
        double len = Math.sqrt(dot(nl, nl));
        if (len < 1.0E-12) {
            return;
        }
        double[] nu = scale(nl, 1.0 / len);
        double off = dl / len;
        // The section: where the plane crosses the cube's edges (part pixels), round its middle.
        double[][] c = corners(cube, 0.0);
        List<double[]> poly = new ArrayList<>();
        for (int[] e : EDGES) {
            double da = dot(nu, c[e[0]]) - off;
            double db = dot(nu, c[e[1]]) - off;
            if ((da < 0.0) != (db < 0.0)) {
                poly.add(lerp(c[e[0]], c[e[1]], da / (da - db)));
            }
        }
        if (poly.size() < 3) {
            return;
        }
        double[] o = new double[3];
        for (double[] p : poly) {
            for (int k = 0; k < 3; k++) {
                o[k] += p[k] / poly.size();
            }
        }
        // Its axes: the part's own length (down it) laid on the plane, and across.
        double[] e1 = sub(new double[] {0, 1, 0}, scale(nu, nu[1]));
        if (Math.sqrt(dot(e1, e1)) < 0.3) {
            e1 = sub(new double[] {0, 0, 1}, scale(nu, nu[2]));
        }
        e1 = unit(e1);
        double[] e2 = cross(nu, e1);
        double[][] flat = new double[poly.size()][];
        for (int i = 0; i < poly.size(); i++) {
            double[] q = sub(poly.get(i), o);
            flat[i] = new double[] {dot(q, e1), dot(q, e2)};
        }
        flat = convex(flat);
        double u0 = Double.MAX_VALUE;
        double u1 = -Double.MAX_VALUE;
        double v0 = Double.MAX_VALUE;
        double v1 = -Double.MAX_VALUE;
        for (double[] q : flat) {
            u0 = Math.min(u0, q[0]);
            u1 = Math.max(u1, q[0]);
            v0 = Math.min(v0, q[1]);
            v1 = Math.max(v1, q[1]);
        }
        int i0 = (int)Math.floor(u0 / CELL);
        int i1 = (int)Math.ceil(u1 / CELL);
        int j0 = (int)Math.floor(v0 / CELL);
        int j1 = (int)Math.ceil(v1 / CELL);
        int ni = i1 - i0;
        int nj = j1 - j0;
        double[][] tall = new double[ni][nj];
        int[][] colour = new int[ni][nj];
        Cut[][] cells = new Cut[ni][nj];
        int seed = cube.part().hashCode();
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                double a0 = (i + i0) * CELL;
                double b0 = (j + j0) * CELL;
                Cut cell = Cut.square(a0, b0, a0 + CELL, b0 + CELL).within(flat);
                if (cell == null) {
                    continue;
                }
                double[] mid = cell.middle();
                double[] at = add(o, add(scale(e1, mid[0]), scale(e2, mid[1])));
                Finisher.Cell what = Finisher.inside(inside, cube.part(), at, cube.from(), cube.size(), Finisher.noise(i + i0, j + j0, seed));
                cells[i][j] = cell;
                tall[i][j] = what.height();
                colour[i][j] = what.colour();
            }
        }
        double px = scale / 16.0;
        for (int half = 0; half < 2; half++) {
            Piece piece = half == 0 ? plus : minus;
            // Out of the cut: toward where the other half was.
            double[] out = scale(plane.n(), half == 0 ? -1.0 : 1.0);
            for (int i = 0; i < ni; i++) {
                for (int j = 0; j < nj; j++) {
                    Cut cell = cells[i][j];
                    if (cell == null) {
                        continue;
                    }
                    double h = tall[i][j];
                    int top = Finisher.argb(colour[i][j], 1.0);
                    int wall = Finisher.argb(Finisher.mixInt(colour[i][j], 0x000000, 0.35), 1.0);
                    int m = cell.points.size();
                    double[][] lifted = new double[m][];
                    for (int k = 0; k < m; k++) {
                        lifted[k] = lift(f, o, e1, e2, cell.points.get(k), out, h * px);
                    }
                    cap(piece, lifted, top, out);
                    for (int k = 0; k < m; k++) {
                        int label = cell.labels.get(k);
                        double beside = 0.0;
                        if (label >= 0) {
                            int bi = i + (label == 0 ? -1 : label == 1 ? 1 : 0);
                            int bj = j + (label == 2 ? -1 : label == 3 ? 1 : 0);
                            beside = bi >= 0 && bi < ni && bj >= 0 && bj < nj && cells[bi][bj] != null ? tall[bi][bj] : 0.0;
                        }
                        if (h <= beside + 1.0E-6) {
                            continue;
                        }
                        double[] a = cell.points.get(k);
                        double[] b = cell.points.get((k + 1) % m);
                        double[] mid = cell.middle();
                        // Its facing: away from the cell's middle, along the cut.
                        double[] edge = {b[0] - a[0], b[1] - a[1]};
                        double[] side = {edge[1], -edge[0]};
                        if (side[0] * ((a[0] + b[0]) * 0.5 - mid[0]) + side[1] * ((a[1] + b[1]) * 0.5 - mid[1]) < 0.0) {
                            side = new double[] {-side[0], -side[1]};
                        }
                        double[] sn = unit(f.vector(add(scale(e1, side[0]), scale(e2, side[1]))));
                        double[][] q = {lift(f, o, e1, e2, a, out, beside * px), lift(f, o, e1, e2, b, out, beside * px), lift(f, o, e1, e2, b, out, h * px),
                            lift(f, o, e1, e2, a, out, h * px)};
                        piece.caps.add(new Cap(q, new int[] {wall, wall, wall, wall}, sn));
                    }
                }
            }
        }
    }

    /** A point of the section (its own axes, part pixels) in the finisher's space, raised {@code by} blocks along {@code out}. */
    static double[] lift(Frame f, double[] o, double[] e1, double[] e2, double[] uv, double[] out, double by) {
        double[] p = f.point(add(o, add(scale(e1, uv[0]), scale(e2, uv[1]))));
        return add(p, scale(out, by));
    }

    /** A cell's top: a quad, or a fan of quads (each a triangle with its last corner twice), facing {@code out}. */
    static void cap(Piece piece, double[][] p, int argb, double[] out) {
        int[] c = {argb, argb, argb, argb};
        for (int k = 1; k + 1 < p.length; k += 2) {
            double[] last = k + 2 < p.length ? p[k + 2] : p[k + 1];
            piece.caps.add(new Cap(new double[][] {p[0], p[k], p[k + 1], last}, c, out));
        }
    }

    /** The section's corners in order round their middle (convex). */
    static double[][] convex(double[][] pts) {
        double cx = 0.0;
        double cy = 0.0;
        for (double[] p : pts) {
            cx += p[0] / pts.length;
            cy += p[1] / pts.length;
        }
        double fx = cx;
        double fy = cy;
        List<double[]> list = new ArrayList<>(List.of(pts));
        list.sort((a, b) -> Double.compare(Math.atan2(a[1] - fy, a[0] - fx), Math.atan2(b[1] - fy, b[0] - fx)));
        // Drop corners the cut passed through twice (an edge's end on the plane).
        List<double[]> out = new ArrayList<>();
        for (double[] p : list) {
            if (out.isEmpty() || Math.hypot(p[0] - out.get(out.size() - 1)[0], p[1] - out.get(out.size() - 1)[1]) > 1.0E-7) {
                out.add(p);
            }
        }
        if (out.size() > 1 && Math.hypot(out.get(0)[0] - out.get(out.size() - 1)[0], out.get(0)[1] - out.get(out.size() - 1)[1]) < 1.0E-7) {
            out.remove(out.size() - 1);
        }
        return out.toArray(new double[0][]);
    }

    /**
     * A cell of a cut: its corners (the section's own axes, counter-clockwise) and what lies past each edge (0..3 the cell beside it: before
     * and after along the first axis, then the second; -1 the section's rim).
     */
    static final class Cut {
        final List<double[]> points = new ArrayList<>();
        final List<Integer> labels = new ArrayList<>();

        static Cut square(double a0, double b0, double a1, double b1) {
            Cut c = new Cut();
            c.add(new double[] {a0, b0}, 2);
            c.add(new double[] {a1, b0}, 1);
            c.add(new double[] {a1, b1}, 3);
            c.add(new double[] {a0, b1}, 0);
            return c;
        }

        void add(double[] p, int label) {
            this.points.add(p);
            this.labels.add(label);
        }

        /** This cell inside the convex polygon {@code poly} (counter-clockwise): the part of it there, or null if none is. */
        Cut within(double[][] poly) {
            Cut c = this;
            for (int e = 0; e < poly.length && c != null; e++) {
                c = c.half(poly[e], poly[(e + 1) % poly.length]);
            }
            return c;
        }

        /** The part left of the line {@code a} to {@code b}; the new edge along it the rim. */
        Cut half(double[] a, double[] b) {
            Cut out = new Cut();
            int m = this.points.size();
            for (int k = 0; k < m; k++) {
                double[] p = this.points.get(k);
                double[] q = this.points.get((k + 1) % m);
                double dp = (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0]);
                double dq = (b[0] - a[0]) * (q[1] - a[1]) - (b[1] - a[1]) * (q[0] - a[0]);
                int label = this.labels.get(k);
                if (dp >= 0.0) {
                    if (dq >= 0.0) {
                        out.add(p, label);
                    } else {
                        out.add(p, label);
                        double t = dp / (dp - dq);
                        out.add(new double[] {p[0] + (q[0] - p[0]) * t, p[1] + (q[1] - p[1]) * t}, -1);
                    }
                } else if (dq >= 0.0) {
                    double t = dp / (dp - dq);
                    out.add(new double[] {p[0] + (q[0] - p[0]) * t, p[1] + (q[1] - p[1]) * t}, label);
                }
            }
            if (out.points.size() < 3 || out.area() < 1.0E-6) {
                return null;
            }
            return out;
        }

        double area() {
            double s = 0.0;
            int m = this.points.size();
            for (int k = 0; k < m; k++) {
                double[] p = this.points.get(k);
                double[] q = this.points.get((k + 1) % m);
                s += p[0] * q[1] - q[0] * p[1];
            }
            return Math.abs(s) * 0.5;
        }

        double[] middle() {
            double x = 0.0;
            double y = 0.0;
            for (double[] p : this.points) {
                x += p[0] / this.points.size();
                y += p[1] / this.points.size();
            }
            return new double[] {x, y};
        }
    }

    /** A part's frame: its pixels to the finisher's space ({@code origin} where its pivot is, {@code columns} its axes, blocks a pixel). */
    static final class Frame {
        final CutPose pose;
        final String part;
        final double scale;
        final double[] origin;
        final double[][] columns = new double[3][];

        Frame(CutPose pose, String part, double scale) {
            this.pose = pose;
            this.part = part;
            this.scale = scale;
            this.origin = pose.place(part, new double[] {0, 0, 0}, scale);
            for (int i = 0; i < 3; i++) {
                double[] e = new double[3];
                e[i] = 1.0;
                this.columns[i] = CutPose.vectorFromModel(pose.axis(part, e), scale);
            }
        }

        double[] point(double[] local) {
            return this.pose.place(this.part, local, this.scale);
        }

        double[] vector(double[] local) {
            return add(add(scale(this.columns[0], local[0]), scale(this.columns[1], local[1])), scale(this.columns[2], local[2]));
        }

        /** A point of the finisher's space in the part's own pixels (its scale taken as even). */
        double[] local(double[] p) {
            double[] d = sub(p, this.origin);
            double[] out = new double[3];
            for (int i = 0; i < 3; i++) {
                double l = dot(this.columns[i], this.columns[i]);
                out[i] = l < 1.0E-18 ? 0.0 : dot(d, this.columns[i]) / l;
            }
            return out;
        }
    }

    // ------------------------------------------------------------------ the line

    /** The line: how thick (pixels each side of the cut) and its colour, bright red, drawn glowing (lit from straight above: full bright). */
    public static final double LINE = 0.07;
    public static final int GLOW = 0xFFFF2A2A;
    /** How far out of the skin the line is drawn (pixels): over a skin's second layer, under nothing. */
    static final double LINE_OUT = 0.32;
    /** How much of the way round the line's end thins over (0..1 of the sweep). */
    static final double TIP = 0.05;
    /** How far a skin's second layer stands off the body (pixels): vanilla's flat layer, and 3D Skin Layers' voxels; the head's hat. */
    public static final double FLAT_LAYER = 0.25;
    public static final double VOXEL_LAYER = 1.15;
    static final double HAT = 0.5;

    /** Where the line starts and turns about: the middle of the trunk's section (the finisher's space). */
    public static double[] centre(CutPose pose, double scale, Plane plane) {
        double[] p = pose.place("torso", new double[] {0.0, 6.0, 0.0}, scale);
        return sub(p, scale(plane.n(), plane.distance(p)));
    }

    /**
     * The line the finisher draws round the body before it falls apart (HexGhoul's owner: "just make a really thin red (bright and glowing)
     * line around the whole area of the cut", "the line should honestly now start in the very center, and the line flows from both sides at
     * once"): one line round every part {@link #cut} will part, where the plane passes through it (where two parts meet it runs on across
     * both, unbroken), drawn in from the middle of the chest's front (the side the blade came in from) both ways at once, up over the face
     * and the crown and down the belly through the crotch, {@code amount} (0..1) of the way round to where the two ends meet in the middle
     * of the back, each end thinning to nothing; {@code layer} how far the skin's second layer stands off it (pixels), the line just past it.
     * Quads into {@code sink} (the finisher's space, blocks; lit from straight above: full bright).
     */
    public static void wound(CutPose pose, Layout layout, double scale, double amount, double layer, Plane plane, Finisher.Sink sink) {
        if (amount <= 0.0) {
            return;
        }
        List<Cube> cubes = cubes(layout);
        List<Cube> cut = new ArrayList<>();
        List<Double> outs = new ArrayList<>();
        List<Frame> frames = new ArrayList<>();
        for (Cube cube : cubes) {
            if (cube.solid() && side(pose, cube, scale, plane) == 0) {
                cut.add(cube);
                double over = cube.part().equals("head") ? Math.max(HAT, layer) : layout.layers ? Math.max(FLAT_LAYER, layer) : 0.0;
                outs.add(LINE_OUT + over);
                frames.add(new Frame(pose, cube.part(), scale));
            }
        }
        double[] middle = centre(pose, scale, plane);
        double[] up = {0.0, 1.0, 0.0};
        double hair = 0.06;
        double px = scale / 16.0;
        for (int c = 0; c < cut.size(); c++) {
            Cube cube = cut.get(c);
            double out = outs.get(c);
            double[][] blown = corners(cube, out);
            double[][] placed = new double[8][];
            for (int k = 0; k < 8; k++) {
                placed[k] = pose.place(cube.part(), blown[k], scale);
            }
            List<double[]> ring = ring(crossing(placed, plane), plane);
            if (ring.size() < 3) {
                continue;
            }
            double perimeter = 0.0;
            for (int k = 0; k < ring.size(); k++) {
                perimeter += length(sub(ring.get((k + 1) % ring.size()), ring.get(k)));
            }
            int steps = Math.max(24, (int)Math.ceil(perimeter / px * 4.0));
            int edge = 0;
            double edgeAt = 0.0;
            double[] last = ring.get(0);
            for (int s = 1; s <= steps; s++) {
                double target = perimeter * s / steps;
                double[] next = null;
                while (next == null) {
                    double[] a = ring.get(edge);
                    double[] b = ring.get((edge + 1) % ring.size());
                    double l = length(sub(b, a));
                    if (target <= edgeAt + l + 1.0E-12 || edge == ring.size() - 1) {
                        next = lerp(a, b, l < 1.0E-12 ? 0.0 : Math.min(1.0, (target - edgeAt) / l));
                    } else {
                        edgeAt += l;
                        edge++;
                    }
                }
                double[] a = last;
                double[] b = next;
                last = next;
                double[] mid = lerp(a, b, 0.5);
                double[] rel = sub(mid, middle);
                double f = Math.abs(Math.atan2(dot(rel, up), dot(rel, plane.start()))) / Math.PI;
                if (f > amount || hidden(mid, cut, outs, frames, c, hair)) {
                    continue;
                }
                double width = LINE * px * Math.min(1.0, (amount - f) / TIP + (amount >= 1.0 ? 1.0 : 0.0));
                double[] w = scale(plane.n(), width);
                sink.quad(sub(a, w), sub(b, w), add(b, w), add(a, w), up, GLOW, GLOW, GLOW, GLOW);
            }
        }
    }

    /** The section's corners (on the plane) in order round their middle. */
    static List<double[]> ring(List<double[]> points, Plane plane) {
        if (points.size() < 3) {
            return points;
        }
        double[] o = new double[3];
        for (double[] p : points) {
            for (int k = 0; k < 3; k++) {
                o[k] += p[k] / points.size();
            }
        }
        double[] up = {0.0, 1.0, 0.0};
        List<double[]> out = new ArrayList<>(points);
        out.sort((p, q) -> Double.compare(Math.atan2(dot(sub(p, o), up), dot(sub(p, o), plane.start())),
            Math.atan2(dot(sub(q, o), up), dot(sub(q, o), plane.start()))));
        return out;
    }

    /** True if {@code p} lies inside another cut part (its box blown up by its own line's distance, less {@code hair} pixels): pressed between two parts. */
    static boolean hidden(double[] p, List<Cube> cut, List<Double> outs, List<Frame> frames, int own, double hair) {
        for (int i = 0; i < cut.size(); i++) {
            if (i == own) {
                continue;
            }
            Cube cube = cut.get(i);
            double g = outs.get(i) - hair;
            double[] l = frames.get(i).local(p);
            if (l[0] > cube.x() - g && l[0] < cube.x() + cube.w() + g && l[1] > cube.y() - g && l[1] < cube.y() + cube.h() + g && l[2] > cube.z() - g
                && l[2] < cube.z() + cube.d() + g) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ the fall

    /**
     * How each half starts moving the moment the line meets itself ({speed, spin}, blocks and radians a tick, the finisher's space): each
     * slides off the cut away from the other and tips over outward, the two falling each to its own side.
     */
    public static double[][] launch(Kind kind, Plane plane) {
        double sign = kind == Kind.PLUS ? 1.0 : -1.0;
        double[] n = plane.n();
        double[] tip = cross(new double[] {0.0, 1.0, 0.0}, n);
        return new double[][] {{n[0] * 0.02 * sign, 0.03, n[2] * 0.02 * sign}, {tip[0] * 0.075 * sign, 0.0, tip[2] * 0.075 * sign}};
    }

    // ------------------------------------------------------------------ vanilla's cubes

    /**
     * The six faces of a cube as vanilla builds them ({@code ModelPart.Cube}: its corners in part pixels and their texture coordinates, 0..1),
     * each {corners, uvs}.
     */
    static List<double[][][]> faces(Cube cube, double x0, double y0, double z0, double x1, double y1, double z1, Layout layout) {
        if (cube.mirror()) {
            double t = x0;
            x0 = x1;
            x1 = t;
        }
        double[] v0 = {x0, y0, z0};
        double[] v1 = {x1, y0, z0};
        double[] v2 = {x1, y1, z0};
        double[] v3 = {x0, y1, z0};
        double[] v4 = {x0, y0, z1};
        double[] v5 = {x1, y0, z1};
        double[] v6 = {x1, y1, z1};
        double[] v7 = {x0, y1, z1};
        double u = cube.u();
        double v = cube.v();
        double w = cube.w();
        double h = cube.h();
        double d = cube.d();
        double f4 = u + d;
        double f5 = u + d + w;
        double f6 = u + d + w + w;
        double f7 = u + d + w + d;
        double f8 = u + d + w + d + w;
        double g1 = v + d;
        double g2 = v + d + h;
        List<double[][][]> out = new ArrayList<>();
        out.add(polygon(new double[][] {v5, v4, v0, v1}, f4, v, f5, g1, layout, cube.mirror()));
        out.add(polygon(new double[][] {v2, v3, v7, v6}, f5, g1, f6, v, layout, cube.mirror()));
        out.add(polygon(new double[][] {v0, v4, v7, v3}, u, g1, f4, g2, layout, cube.mirror()));
        out.add(polygon(new double[][] {v1, v0, v3, v2}, f4, g1, f5, g2, layout, cube.mirror()));
        out.add(polygon(new double[][] {v5, v1, v2, v6}, f5, g1, f7, g2, layout, cube.mirror()));
        out.add(polygon(new double[][] {v4, v5, v6, v7}, f7, g1, f8, g2, layout, cube.mirror()));
        return out;
    }

    /** One face as vanilla's {@code Polygon}: its corners take (u1, v0), (u0, v0), (u0, v1), (u1, v1); mirrored, in the other order. */
    static double[][][] polygon(double[][] corners, double u0, double v0, double u1, double v1, Layout layout, boolean mirror) {
        double tw = layout.width;
        double th = layout.height;
        double[][] uv = {{u1 / tw, v0 / th}, {u0 / tw, v0 / th}, {u0 / tw, v1 / th}, {u1 / tw, v1 / th}};
        double[][] p = corners.clone();
        if (mirror) {
            double[][] rp = new double[4][];
            double[][] ruv = new double[4][];
            for (int k = 0; k < 4; k++) {
                rp[k] = p[3 - k];
                ruv[k] = uv[3 - k];
            }
            p = rp;
            uv = ruv;
        }
        return new double[][][] {p, uv};
    }

    // ------------------------------------------------------------------ vectors

    static double[] lerp(double[] a, double[] b, double t) {
        double[] out = new double[a.length];
        for (int k = 0; k < a.length; k++) {
            out[k] = a[k] + (b[k] - a[k]) * t;
        }
        return out;
    }

    static double[] add(double[] a, double[] b) {
        return new double[] {a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    static double[] sub(double[] a, double[] b) {
        return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    static double[] scale(double[] a, double k) {
        return new double[] {a[0] * k, a[1] * k, a[2] * k};
    }

    static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    static double length(double[] a) {
        return Math.sqrt(dot(a, a));
    }

    static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static double[] unit(double[] a) {
        double l = length(a);
        return l < 1.0E-12 ? new double[] {0.0, 1.0, 0.0} : new double[] {a[0] / l, a[1] / l, a[2] / l};
    }
}
