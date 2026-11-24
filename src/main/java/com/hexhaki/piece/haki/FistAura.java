package com.hexhaki.piece.haki;

import java.util.ArrayList;
import java.util.List;

/**
 * 2.5.3: Ryou's fist aura as geometry (pure, tested in {@code RyouTests}, rendered off-game by {@code tools/preview/RyouPreview}).
 * The owner's target (docs/handoff/ryou-fist-target.jpg, "this is the fucking target look can you not do this?"): a glossy black fist
 * and forearm wrapped in a thick, soft aura that hugs the silhouette: a thin hot rim right at the black edge, a bright band in the
 * colour, a wide soft haze beyond it whose edge is wavy like flame and trails back along the arm; sparks streaking off it.
 *
 * <p>2.4.7 - 2.5.2 drew the glow with the game's glowing-outline post chain; on the owner's device (Android, OpenGL ES through
 * MobileGlues) it flickered, and its blit left the projection flat for everything drawn after it in the level (the release's lightning
 * vanished while Ryou was up). This is plain geometry instead, built each frame from where the eye is:
 * <ol>
 *   <li>the coated part of the arm (the forearm's lower end to the fist, the sleeve layer's size) is bent with the elbow exactly as the
 *       arm is ({@link ArmBend});</li>
 *   <li>its silhouette as the eye sees it: the edges between a face turned to the eye and one turned away, chained into a loop;</li>
 *   <li>a strip is swept outward round that loop, in the plane square to the eye's ray at each point (so on the screen it is a band
 *       round the outline), rounded at the corners; rings across the strip carry the colours: the hot rim, the band, the haze; the
 *       outer rings' reach waves with noise flowing back up the arm (the flame), longer where the outline faces back up the arm;</li>
 *   <li>everything is pulled a little toward the eye along its own ray (the same place on the screen) so the fist never hides its own
 *       rim, and every quad is wound to face the eye, so it is right with back faces culled or not.</li>
 * </ol>
 * Alpha blended, never additive (the owner's device draws additive geometry as solid boxes). Units: arm space (the arm's frame after
 * {@code ModelPart.translateAndRotate}), blocks; inside, model pixels.
 */
public final class FistAura {
    /** A quad: four corners (x, y, z each, arm space, blocks), wound to face the eye; a colour (ARGB) per corner. */
    public interface Sink {
        void quad(float[] xyz, int[] argb);
    }

    static final float PX = 1.0F / 16.0F;
    /** The aura's body (pixels): the sleeve layer's size round the arm box, from {@link #TOP} up the forearm to the fist's end. */
    static final float GROW = 0.25F;
    /**
     * Where the aura's body starts up the arm. 3.6.0 (the owner, circling where the glow stopped: "make sure the haki glow fades
     * properly, its ending too shortly all of a sudden"): the coat runs past the elbow (HandCoat), and on a straight arm so does the
     * aura ({@link #TOP_STRAIGHT}), fading out slowly up it; an arm bent at the elbow keeps it below the bend ({@link #TOP}: its outline
     * is the bent box's, which must not fold across the bend), easing between the two as the elbow bends ({@link #top}).
     */
    static final float TOP = 4.6F;
    static final float TOP_STRAIGHT = 1.2F;

    /** 3.6.0: where the aura starts on an arm bent {@code amount} radians at the elbow. */
    static float top(float amount) {
        return TOP_STRAIGHT + (TOP - TOP_STRAIGHT) * smooth(0.0, 0.3, Math.abs(amount));
    }
    static final float END = 10.0F;
    /** The rings across the strip (pixels out from the outline before the flame and the reach), their alphas, their colours. */
    static final float[] RINGS = {-0.75F, -0.22F, 0.0F, 0.42F, 1.1F, 2.0F, 3.1F, 4.3F};
    static final float[] ALPHAS = {0.0F, 0.4F, 0.95F, 0.9F, 0.64F, 0.33F, 0.12F, 0.0F};
    /** 0 the hot rim's colour, 1 the band's, 2 the haze's. */
    static final int[] HUES = {1, 1, 0, 1, 1, 2, 2, 2};
    /** The first ring the flame and the reach move (the rim and what is inside it hold still). */
    static final int FLAME = 4;
    /** How far everything is pulled toward the eye along its ray (pixels). */
    static final float PULL = 1.0F;
    /** Past this distance (pixels, arm space) the aura grows with distance, so it keeps its size on the screen. */
    static final float FAR = 120.0F;
    /** The longest a straight stretch of the strip is before it is cut (pixels), and the widest turn of a rounded corner (radians). */
    static final float STEP = 0.6F;
    static final double TURN = 0.33;
    /** The most sparks at once, idle and punching. */
    static final int SPARKS_IDLE = 6;
    static final int SPARKS_MAX = 14;

    private FistAura() {
    }

    /** One point of the strip's inner edge: where it is, which way is out, which way the inner ring's corner goes, and its arm place. */
    record Station(double[] p, double[] out, double[] inner, double y, double around) {
    }

    /**
     * Builds one arm's aura into {@code sink}.
     *
     * @param bendAxis   the elbow bend's axis and {@code bendAmount} its angle (radians), as playerAnimator gives them
     * @param slim       a slim (3-wide) arm
     * @param right      the right arm (its box lies to -x of the shoulder pivot)
     * @param view       the eye in arm space: {x, y, z, 1} a point (blocks), or {x, y, z, 0} the direction toward the viewer (an
     *                   orthographic view: the inventory)
     * @param mirrored   the arm space is drawn mirrored (the inventory's flipped z): every winding turned round
     * @param age        ticks (with the partial tick): what flows and flickers
     * @param seed       the player's own (each aura out of step with the others)
     * @param rgb        the Ryou colour
     * @param strength   0..1, how strong (the breath, the flare as Ryou comes on)
     * @param punch      0..1, how far into a swing the arm is (more sparks)
     */
    public static void build(float bendAxis, float bendAmount, boolean slim, boolean right, double[] view, boolean mirrored, float age,
                             int seed, int rgb, float strength, float punch, Sink sink) {
        build(bendAxis, bendAmount, slim, right, view, mirrored, age, seed, rgb, strength, punch, true, sink);
    }

    /** {@link #build}, the sparks left out when {@code sparks} is false (the tests check the strip's steadiness alone). */
    public static void build(float bendAxis, float bendAmount, boolean slim, boolean right, double[] view, boolean mirrored, float age,
                             int seed, int rgb, float strength, float punch, boolean sparks, Sink sink) {
        if (strength <= 0.003F) {
            return;
        }
        Frame f = new Frame(bendAxis, bendAmount, slim, right, view, mirrored);
        int[] colours = colours(rgb);
        List<Station> loop = f.outline();
        if (loop.size() >= 3) {
            strip(f, loop, age, seed, colours, strength, sink);
        }
        if (sparks) {
            sparks(f, age, seed, rgb, strength, punch, sink);
        }
    }

    /** The rim's, the band's and the haze's colours (opaque; alpha is laid on per ring) from the Ryou colour. */
    static int[] colours(int rgb) {
        RyouColour chosen = RyouColour.byBand(rgb);
        if (chosen != null) {
            // 3.6.0: a chosen Ryou colour: its band, its own haze (Red & Orange's orange, Purple & Black's black smoke), the rim the band
            // most of the way to white
            float cr = ((chosen.band >> 16) & 255) / 255.0F;
            float cg = ((chosen.band >> 8) & 255) / 255.0F;
            float cb = (chosen.band & 255) / 255.0F;
            int hot = pack(cr + (1 - cr) * 0.72F, cg + (1 - cg) * 0.72F, cb + (1 - cb) * 0.72F);
            return new int[] {hot, chosen.band, chosen.haze};
        }
        float r = ((rgb >> 16) & 255) / 255.0F;
        float g = ((rgb >> 8) & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;
        float top = Math.max(0.05F, Math.max(r, Math.max(g, b)));
        // the band: the hue at full brightness
        float br = r / top;
        float bg = g / top;
        float bb = b / top;
        int band = pack(br, bg, bb);
        // the rim: hot, most of the way to white
        int hot = pack(br + (1 - br) * 0.72F, bg + (1 - bg) * 0.72F, bb + (1 - bb) * 0.72F);
        // the haze: paler, the hue turned a little (red toward pink, as in the owner's picture)
        float[] hsv = hsv(br, bg, bb);
        float[] haze = rgb((hsv[0] - 0.045F + 1.0F) % 1.0F, hsv[1] * 0.7F, 1.0F);
        return new int[] {hot, band, pack(haze[0], haze[1], haze[2])};
    }

    // ----------------------------------------------------------------------------------------------------------------- the frame

    /** The arm's box, bent; the eye; the aura body's corners and its silhouette as the eye sees it. */
    static final class Frame {
        final float axis;
        final float amount;
        final float minX;
        final float maxX;
        final double[] view;
        final boolean mirrored;
        final double[][] unbent = new double[8][];
        final double[][] corner = new double[8][];
        final double[] centre = new double[3];
        /** Up the arm (from the fist toward the elbow), bent. */
        final double[] back;
        /** 3.6.0: where the aura starts up this arm (pixels, {@link #top}). */
        final float top;

        Frame(float axis, float amount, boolean slim, boolean right, double[] view, boolean mirrored) {
            this.axis = axis;
            this.amount = amount;
            float half = slim ? 1.5F : 2.0F;
            float middle = (slim ? 0.5F : 1.0F) * (right ? -1.0F : 1.0F);
            this.minX = middle - half;
            this.maxX = middle + half;
            this.mirrored = mirrored;
            this.top = top(amount);
            this.view = view[3] > 0.5 ? new double[] {view[0] / PX, view[1] / PX, view[2] / PX, 1} : unit(new double[] {view[0], view[1],
                view[2]}, 0);
            double x0 = this.minX - GROW;
            double x1 = this.maxX + GROW;
            double z0 = -2 - GROW;
            double z1 = 2 + GROW;
            double[][] square = {{x0, z0}, {x1, z0}, {x1, z1}, {x0, z1}};
            for (int i = 0; i < 8; i++) {
                double y = i < 4 ? this.top : END + GROW;
                this.unbent[i] = new double[] {square[i & 3][0], y, square[i & 3][1]};
                this.corner[i] = this.bend(this.unbent[i]);
                for (int k = 0; k < 3; k++) {
                    this.centre[k] += this.corner[i][k] / 8.0;
                }
            }
            double mid = (this.minX + this.maxX) / 2.0;
            this.back = norm(sub(this.bend(new double[] {mid, this.top, 0}), this.bend(new double[] {mid, END, 0})));
        }

        /** A point of the arm (pixels, unbent) where the bent arm has it (pixels). */
        double[] bend(double[] q) {
            float[] p = ArmBend.bent(this.axis, this.amount, this.minX, this.maxX, (float) (q[0] * PX), (float) (q[1] * PX),
                (float) (q[2] * PX));
            return new double[] {p[0] / PX, p[1] / PX, p[2] / PX};
        }

        /** From {@code p} toward the viewer (not normalised for an eye point). */
        double[] toEye(double[] p) {
            return this.view[3] > 0.5 ? sub(this.view, p) : new double[] {this.view[0], this.view[1], this.view[2]};
        }

        /** How far {@code p} is from the eye (pixels); 0 for an orthographic view. */
        double distance(double[] p) {
            return this.view[3] > 0.5 ? len(sub(this.view, p)) : 0.0;
        }

        /** The faces (corner indices, any order round the face): the top (up the forearm), the fist's end, the four sides. */
        static final int[][] FACES = {{0, 1, 2, 3}, {4, 5, 6, 7}, {0, 1, 5, 4}, {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7}};
        /** The edges: their corners and the two faces they join. */
        static final int[][] EDGES = {
            {0, 1, 0, 2}, {1, 2, 0, 3}, {2, 3, 0, 4}, {3, 0, 0, 5},
            {4, 5, 1, 2}, {5, 6, 1, 3}, {6, 7, 1, 4}, {7, 4, 1, 5},
            {0, 4, 5, 2}, {1, 5, 2, 3}, {2, 6, 3, 4}, {3, 7, 4, 5},
        };

        /** Which faces are turned to the eye. */
        boolean[] facing() {
            boolean[] out = new boolean[6];
            for (int f = 0; f < 6; f++) {
                int[] k = FACES[f];
                double[] a = this.corner[k[0]];
                double[] b = this.corner[k[1]];
                double[] c = this.corner[k[2]];
                double[] d = this.corner[k[3]];
                double[] n = cross(sub(c, a), sub(d, b));
                double[] mid = {(a[0] + b[0] + c[0] + d[0]) / 4, (a[1] + b[1] + c[1] + d[1]) / 4, (a[2] + b[2] + c[2] + d[2]) / 4};
                if (dot(n, sub(mid, this.centre)) < 0) {
                    n = scale(n, -1);
                }
                out[f] = dot(n, this.toEye(mid)) > 0;
            }
            return out;
        }

        /** The silhouette as a loop of corner indices, or empty (the eye inside the body, or a loop that does not close). */
        int[] loop() {
            boolean[] facing = this.facing();
            int[][] next = new int[8][2];
            int[] links = new int[8];
            int edges = 0;
            for (int[] e : EDGES) {
                if (facing[e[2]] != facing[e[3]]) {
                    if (links[e[0]] == 2 || links[e[1]] == 2) {
                        return new int[0];
                    }
                    next[e[0]][links[e[0]]++] = e[1];
                    next[e[1]][links[e[1]]++] = e[0];
                    edges++;
                }
            }
            if (edges < 3) {
                return new int[0];
            }
            int start = -1;
            for (int i = 0; i < 8; i++) {
                if (links[i] == 2) {
                    start = i;
                    break;
                }
            }
            int[] order = new int[edges];
            int previous = -1;
            int at = start;
            for (int n = 0; n < edges; n++) {
                if (links[at] != 2) {
                    return new int[0];
                }
                order[n] = at;
                int to = next[at][0] != previous ? next[at][0] : next[at][1];
                previous = at;
                at = to;
            }
            return at == start ? order : new int[0];
        }

        /** The outward direction at {@code p} of an outline running along {@code along}: square to it and to the eye's ray, away from the body. */
        double[] outward(double[] p, double[] along) {
            double[] o = cross(along, this.toEye(p));
            double length = len(o);
            if (length < 1.0E-7) {
                return null;
            }
            o = scale(o, 1.0 / length);
            return dot(o, sub(p, this.centre)) < 0 ? scale(o, -1) : o;
        }

        /** The silhouette's stations: along each edge (cut every {@link #STEP} pixels) and round each corner (every {@link #TURN}). */
        List<Station> outline() {
            int[] loop = this.loop();
            List<Station> out = new ArrayList<>();
            int m = loop.length;
            if (m == 0) {
                return out;
            }
            double cx = (this.minX + this.maxX) / 2.0;
            for (int j = 0; j < m; j++) {
                int a = loop[j];
                int b = loop[(j + 1) % m];
                int c = loop[(j + 2) % m];
                double[] along = sub(this.corner[b], this.corner[a]);
                double length = len(sub(this.unbent[b], this.unbent[a]));
                int cuts = Math.max(1, (int) Math.ceil(length / STEP));
                for (int s = 0; s <= cuts; s++) {
                    double t = s / (double) cuts;
                    double[] u = lerp(this.unbent[a], this.unbent[b], t);
                    double[] p = this.bend(u);
                    double[] o = this.outward(p, along);
                    if (o != null) {
                        out.add(new Station(p, o, o, u[1], Math.atan2(u[2], u[0] - cx)));
                    }
                }
                // round the corner at b, from this edge's outward to the next one's
                double[] p = this.corner[b];
                double[] o1 = this.outward(p, along);
                double[] o2 = this.outward(p, sub(this.corner[c], this.corner[b]));
                if (o1 == null || o2 == null) {
                    continue;
                }
                double angle = Math.atan2(len(cross(o1, o2)), dot(o1, o2));
                double[] mitre = norm(add(o1, o2));
                double reach = 1.0 / Math.max(0.4, Math.cos(angle / 2.0));
                double[] inner = scale(mitre, reach);
                int turns = (int) Math.ceil(angle / TURN);
                double[] u = this.unbent[b];
                for (int s = 1; s < turns; s++) {
                    double[] o = slerp(o1, o2, angle, s / (double) turns);
                    out.add(new Station(p, o, inner, u[1], Math.atan2(u[2], u[0] - cx)));
                }
            }
            return out;
        }

        /** {@code p} pulled {@code by} pixels toward the eye along its own ray (the same place on the screen), in blocks. */
        float[] placed(double[] p, double by) {
            double[] e = norm(this.toEye(p));
            return new float[] {(float) ((p[0] + e[0] * by) * PX), (float) ((p[1] + e[1] * by) * PX), (float) ((p[2] + e[2] * by) * PX)};
        }

        /** Winds a quad (pixel corners, blocks after {@link #placed}) to face the eye, turned round again when the frame is mirrored. */
        void emit(double[][] at, float[][] placed, int[] argb, Sink sink) {
            double[] n = cross(sub(at[2], at[0]), sub(at[3], at[1]));
            double[] mid = {(at[0][0] + at[2][0]) / 2, (at[0][1] + at[2][1]) / 2, (at[0][2] + at[2][2]) / 2};
            boolean flip = dot(n, this.toEye(mid)) < 0;
            if (this.mirrored) {
                flip = !flip;
            }
            float[] xyz = new float[12];
            int[] colours = new int[4];
            for (int k = 0; k < 4; k++) {
                int from = flip ? 3 - k : k;
                System.arraycopy(placed[from], 0, xyz, k * 3, 3);
                colours[k] = argb[from];
            }
            sink.quad(xyz, colours);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------- the strip

    /** How strongly the aura shows at arm height {@code y} (pixels): nothing up the forearm, full from the wrist down. */
    static float along(double y, float top) {
        // 3.6.0: a long, soft fade up the arm (was 2.6 pixels: it stopped short, all of a sudden)
        double fade = Math.max(2.6, (END - top) * 0.62);
        float k = smooth(top, top + fade, y);
        return k * k * (3.0F - 2.0F * k) * 0.35F + k * 0.65F;
    }

    /** How wide it is at arm height {@code y}: thinner along the forearm, thickest round the fist. */
    static float width(double y) {
        return 0.6F + 0.4F * smooth(5.5, 9.5, y);
    }

    /** 0..1: the flame's noise at a place on the arm ({@code around} radians, {@code y} pixels) at {@code age}, flowing up the arm. */
    static float flame(double around, double y, float age, int seed) {
        double u = (around / (Math.PI * 2.0) + 0.5) * 5.0;
        double v = y * 0.45 + age * 0.19;
        double n = 0.75 * noise(u, v, 5, seed) + 0.25 * noise(u * 2.0 + 3.1, v * 1.7 - age * 0.11, 10, seed + 17);
        return (float) Math.max(0.0, Math.min(1.0, n));
    }

    static void strip(Frame f, List<Station> loop, float age, int seed, int[] colours, float strength, Sink sink) {
        int n = loop.size();
        int rings = RINGS.length;
        double[][][] at = new double[n][rings][];
        float[][][] placed = new float[n][rings][];
        int[][] argb = new int[n][rings];
        // the flame per station, smoothed along the outline (neighbours a fraction of a pixel apart never jump: the haze's edge waves)
        float[] flames = new float[n];
        double[] reach = new double[n];
        for (int i = 0; i < n; i++) {
            Station s = loop.get(i);
            flames[i] = flame(s.around(), s.y(), age, seed);
            reach[i] = (0.4 + 1.7 * Math.pow(flames[i], 1.3)) * (1.0 + 0.85 * Math.max(0.0, dot(s.out(), f.back)));
        }
        for (int pass = 0; pass < 3; pass++) {
            flames = smoothed(flames);
            reach = smoothed(reach);
        }
        for (int i = 0; i < n; i++) {
            Station s = loop.get(i);
            double grow = Math.max(1.0, f.distance(s.p()) / FAR);
            float fade = along(s.y(), f.top) * strength;
            float wide = width(s.y());
            float fl = flames[i];
            double tongue = reach[i];
            for (int r = 0; r < rings; r++) {
                double out = RINGS[r];
                if (r >= 3) {
                    out *= wide;
                }
                if (r == FLAME - 1) {
                    out *= 0.85 + 0.3 * fl;
                } else if (r >= FLAME) {
                    out *= tongue;
                }
                out *= grow;
                double[] dir = out < 0 ? s.inner() : s.out();
                at[i][r] = add(s.p(), scale(dir, out));
                placed[i][r] = f.placed(at[i][r], PULL * grow);
                float alpha = ALPHAS[r] * fade;
                if (r >= FLAME - 1) {
                    alpha *= 0.75F + 0.5F * fl;
                }
                argb[i][r] = Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F) << 24 | colours[HUES[r]];
            }
        }
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            for (int r = 0; r + 1 < rings; r++) {
                if ((argb[i][r] | argb[i][r + 1] | argb[j][r] | argb[j][r + 1]) >>> 24 == 0) {
                    continue;
                }
                f.emit(new double[][] {at[i][r], at[j][r], at[j][r + 1], at[i][r + 1]},
                    new float[][] {placed[i][r], placed[j][r], placed[j][r + 1], placed[i][r + 1]},
                    new int[] {argb[i][r], argb[j][r], argb[j][r + 1], argb[i][r + 1]}, sink);
            }
        }
    }

    /** Each value blended with its two neighbours round the loop (1 2 1). */
    static float[] smoothed(float[] v) {
        int n = v.length;
        float[] out = new float[n];
        for (int i = 0; i < n; i++) {
            out[i] = (v[(i + n - 1) % n] + 2.0F * v[i] + v[(i + 1) % n]) / 4.0F;
        }
        return out;
    }

    static double[] smoothed(double[] v) {
        int n = v.length;
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            out[i] = (v[(i + n - 1) % n] + 2.0 * v[i] + v[(i + 1) % n]) / 4.0;
        }
        return out;
    }

    // ----------------------------------------------------------------------------------------------------------------- sparks

    /** Sparks: short streaks thrown off the fist and back up the arm, a few at a time, more while punching. */
    static void sparks(Frame f, float age, int seed, int rgb, float strength, float punch, Sink sink) {
        int count = Math.round(SPARKS_IDLE + (SPARKS_MAX - SPARKS_IDLE) * Math.max(0.0F, Math.min(1.0F, punch)));
        int[] cs = colours(rgb);
        int hot = mix(0xFFE6A0, cs[0], 0.3F); // gold, a little of the rim's colour (the picture's sparks)
        double cx = (f.minX + f.maxX) / 2.0;
        double hx = (f.maxX - f.minX) / 2.0 + GROW;
        double hz = 2.0 + GROW;
        for (int i = 0; i < SPARKS_MAX; i++) {
            if (i >= count) {
                break;
            }
            double period = 10.0 + 8.0 * hash01(seed, i, 0, 1);
            double phase = age / period + hash01(seed, i, 0, 2);
            long cycle = (long) Math.floor(phase);
            double t = phase - cycle;
            double r1 = hash01(seed, i, cycle, 3);
            double r2 = hash01(seed, i, cycle, 4);
            double r3 = hash01(seed, i, cycle, 5);
            double r4 = hash01(seed, i, cycle, 6);
            if (r4 < 0.25 && i >= SPARKS_IDLE / 2) {
                continue; // a gap now and then: never a steady rhythm
            }
            // from a point on the coat round the fist
            double angle = r1 * Math.PI * 2.0;
            double ca = Math.cos(angle);
            double sa = Math.sin(angle);
            double k = 1.0 / Math.max(Math.abs(ca) / hx, Math.abs(sa) / hz);
            double y = 7.4 + r2 * 3.0;
            double[] start = {cx + ca * k, y, sa * k};
            double[] out = {ca, 0, sa};
            double[] velocity = add(scale(out, 0.5 + 0.7 * r3), new double[] {0, -(1.2 + 1.6 * r4), 0});
            double travel = 3.0 + 4.0 * r3;
            double eased = 1.0 - (1.0 - t) * (1.0 - t);
            double[] head = add(start, scale(velocity, travel * eased / len(velocity)));
            double[] tail = add(start, scale(velocity, travel * Math.max(0.0, eased - 0.3) / len(velocity)));
            double[] h = f.bend(head);
            double[] tl = f.bend(tail);
            double[] d = sub(h, tl);
            if (len(d) < 1.0E-4) {
                continue;
            }
            double[] side = cross(d, f.toEye(h));
            double sl = len(side);
            if (sl < 1.0E-7) {
                continue;
            }
            double grow = Math.max(1.0, f.distance(h) / FAR);
            side = scale(side, 0.1 * grow / sl);
            float alpha = (float) (Math.pow(Math.sin(Math.PI * t), 0.7) * strength * along(y, f.top));
            int a = Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F) << 24;
            double[][] at = {sub(tl, scale(side, 0.3)), add(tl, scale(side, 0.3)), add(h, side), sub(h, side)};
            float[][] placed = new float[4][];
            for (int q = 0; q < 4; q++) {
                placed[q] = f.placed(at[q], PULL * grow);
            }
            f.emit(at, placed, new int[] {hot, hot, a | hot, a | hot}, sink);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------- numbers

    static float smooth(double from, double to, double x) {
        double t = Math.max(0.0, Math.min(1.0, (x - from) / (to - from)));
        return (float) (t * t * (3.0 - 2.0 * t));
    }

    /** Smooth value noise, 0..1, repeating every {@code period} along u. */
    static double noise(double u, double v, int period, int seed) {
        double fu = Math.floor(u);
        double fv = Math.floor(v);
        int iu = (int) fu;
        int iv = (int) fv;
        double tu = u - fu;
        double tv = v - fv;
        tu = tu * tu * (3 - 2 * tu);
        tv = tv * tv * (3 - 2 * tv);
        double a = lattice(Math.floorMod(iu, period), iv, seed);
        double b = lattice(Math.floorMod(iu + 1, period), iv, seed);
        double c = lattice(Math.floorMod(iu, period), iv + 1, seed);
        double d = lattice(Math.floorMod(iu + 1, period), iv + 1, seed);
        return (a + (b - a) * tu) * (1 - tv) + (c + (d - c) * tu) * tv;
    }

    static double lattice(int u, int v, int seed) {
        return hash01(seed, u, v, 11);
    }

    static double hash01(int seed, long a, long b, int salt) {
        long h = seed * 0x9E3779B97F4A7C15L + a * 0xC2B2AE3D27D4EB4FL + b * 0x165667B19E3779F9L + salt * 0x27D4EB2F165667C5L;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return (h >>> 11) * 0x1.0p-53;
    }

    static int pack(float r, float g, float b) {
        return Math.round(Math.max(0, Math.min(1, r)) * 255) << 16 | Math.round(Math.max(0, Math.min(1, g)) * 255) << 8
            | Math.round(Math.max(0, Math.min(1, b)) * 255);
    }

    static int mix(int a, int b, float t) {
        int out = 0;
        for (int shift = 0; shift < 24; shift += 8) {
            int ca = (a >> shift) & 255;
            int cb = (b >> shift) & 255;
            out |= Math.round(ca + (cb - ca) * t) << shift;
        }
        return out;
    }

    static float[] hsv(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;
        float h = 0;
        if (d > 1.0E-6F) {
            if (max == r) {
                h = ((g - b) / d) / 6.0F;
            } else if (max == g) {
                h = ((b - r) / d + 2) / 6.0F;
            } else {
                h = ((r - g) / d + 4) / 6.0F;
            }
        }
        return new float[] {(h + 1.0F) % 1.0F, max <= 0 ? 0 : d / max, max};
    }

    static float[] rgb(float h, float s, float v) {
        float h6 = h * 6.0F;
        int i = (int) Math.floor(h6) % 6;
        float f = h6 - (float) Math.floor(h6);
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));
        return switch (i) {
            case 0 -> new float[] {v, t, p};
            case 1 -> new float[] {q, v, p};
            case 2 -> new float[] {p, v, t};
            case 3 -> new float[] {p, q, v};
            case 4 -> new float[] {t, p, v};
            default -> new float[] {v, p, q};
        };
    }

    static double[] slerp(double[] a, double[] b, double angle, double t) {
        if (angle < 1.0E-6) {
            return a;
        }
        double s = Math.sin(angle);
        double wa = Math.sin((1 - t) * angle) / s;
        double wb = Math.sin(t * angle) / s;
        return norm(add(scale(a, wa), scale(b, wb)));
    }

    static double[] lerp(double[] a, double[] b, double t) {
        return new double[] {a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
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

    static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static double len(double[] a) {
        return Math.sqrt(dot(a, a));
    }

    static double[] norm(double[] a) {
        double l = len(a);
        return l < 1.0E-12 ? new double[] {0, 0, 0} : scale(a, 1.0 / l);
    }

    static double[] unit(double[] a, double w) {
        double[] n = norm(a);
        return new double[] {n[0], n[1], n[2], w};
    }
}
