package com.hexhaki.piece.supa;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.3.5: the Supa Supa no Mi's steel in 3D (the owner: "Turns any body part—such as fingers, arms, legs, or the entire body—into sharp
 * steel blades"; "haki blackens the blades as in makes them purpleish like the katanas"), after the owner's references (Daz Bones' arm
 * blade, steel with a wavy edge out of the forearm; his finger blades; Spiral Hollow's white drills round the forearms). Built in the house
 * style (POINT ONE: flat banded colours, alpha blended, depth tested, never additive; FireMesh's arcs, rings, rays and streaks for what
 * flies), pure so the previews draw the very same (tools/preview/SupaShots).
 *
 * <p>Two kinds of drawing:
 * <ul>
 *   <li><b>On the body</b>, in a limb's own model frame (client/supa/SupaClient puts the pose on the arm, the leg or the body; 1 a block,
 *       y down the limb from its joint, the limb's front at -z): the arm blade ({@link #armBlade}), the finger blades ({@link #claws}), the
 *       drill ({@link #drill}), the foot blade ({@link #footBlade}) and Spider's steel skin ({@link #steelBox}).</li>
 *   <li><b>In the world</b>, eye-centred (x, y, z from the eye) as every fruit's art: the flying slash ({@link #slash}), the X
 *       ({@link #cross}), the claw marks ({@link #clawMarks}), sparks ({@link #sparks}), a blow glancing off ({@link #glance}), the drill's
 *       and the lunge's trails, the spinning blades of Spar Break ({@link #spin}) and the skate marks ({@link #mark}).</li>
 * </ul>
 * With Armament Haki on the steel is multiplied toward the katanas' own very dark purple ({@link #coat}: client/sword/KatanaRenderer's
 * COAT_R/G/B 62, 34, 88), its bright edge kept as a violet glint so the blade still reads.
 */
public final class SupaArt {
    /** The steel, dark to bright: its spine, its flat, its bevel, its shine, the very edge. */
    public static final int DARK = 0x4C5A6C;
    public static final int FLAT = 0x8A98AB;
    public static final int BEVEL = 0xC6D1DE;
    public static final int SHINE = 0xEEF3F9;
    public static final int EDGE = 0xFFFFFF;
    /** The cutting force (the flying slash, the X): white at its heart, the fruit's pale blue round it. */
    public static final int CUT_CORE = 0xF4F9FF;
    public static final int CUT_GLOW = 0x9CC3F0;
    /** Sparks off steel. */
    public static final int SPARK = 0xFFD27A;
    /** The katanas' coat (client/sword/KatanaRenderer: COAT_R/G/B), and the glint a coated edge keeps. */
    static final int COAT_R = 62;
    static final int COAT_G = 34;
    static final int COAT_B = 88;
    public static final int COAT_GLINT = 0x8A5CC8;
    /** How long each world effect shows (ticks). */
    public static final double CLAW_LIFE = 6;
    public static final double SPARK_LIFE = 10;
    public static final double GLANCE_LIFE = 8;
    public static final double TRAIL_LIFE = 12;
    public static final double CROSS_LIFE = 10;
    /** The arm blade's length past its root on the forearm, and how far up the forearm it grows from (blocks, the arm's frame). */
    public static final double BLADE_LENGTH = 1.05;
    public static final double BLADE_ROOT = 0.28;

    private SupaArt() {
    }

    /** {@code rgb} coated {@code coat} (0 bare steel, 1 full Armament) toward the katanas' very dark purple. */
    public static int coat(int rgb, double coat) {
        if (coat <= 0.0) {
            return rgb;
        }
        int r = (rgb >> 16) & 255;
        int g = (rgb >> 8) & 255;
        int b = rgb & 255;
        double k = Math.min(1.0, coat);
        r = (int) Math.round(r * (1.0 - k + k * COAT_R / 255.0));
        g = (int) Math.round(g * (1.0 - k + k * COAT_G / 255.0));
        b = (int) Math.round(b * (1.0 - k + k * COAT_B / 255.0));
        return r << 16 | g << 8 | b;
    }

    /** The bright edge line: white on bare steel, a violet glint on coated steel. */
    static int edge(double coat) {
        return coat <= 0.0 ? EDGE : FireMesh.mix(EDGE, COAT_GLINT, Math.min(1.0, coat));
    }

    static int solid(int rgb) {
        return 0xFF000000 | rgb;
    }

    // ------------------------------------------------------------------------------------------------------ on the body

    /** The blade's outline: the edge's distance out from the spine at {@code t} (0 root, 1 tip) along it. */
    static double width(double t) {
        double body = t < 0.12 ? 0.07 + t / 0.12 * 0.15 : t < 0.74 ? 0.22 - (t - 0.12) * 0.05 : 0.19 * (1.0 - (t - 0.74) / 0.26);
        return Math.max(0.0, body);
    }

    static final int BLADE_STEPS = 12;
    /** The blade's cross-section turned this far round the arm (radians): never quite edge-on from the side or the front. */
    static final double BLADE_TURN = Math.toRadians(35);
    static final double TURN_C = Math.cos(BLADE_TURN);
    static final double TURN_S = Math.sin(BLADE_TURN);
    static final double[] P4 = new double[12];
    static final int[] C4 = new int[4];

    /** A quad given in the blade's own section (u across toward the edge, y along the arm, v through its thickness), turned. */
    static void bq(FireMesh.Sink sink, double cu, double u0, double y0, double v0, int c0, double u1, double y1, double v1, int c1, double u2,
                   double y2, double v2, int c2, double u3, double y3, double v3, int c3) {
        double[] u = {u0, u1, u2, u3};
        double[] y = {y0, y1, y2, y3};
        double[] v = {v0, v1, v2, v3};
        for (int k = 0; k < 4; k++) {
            double a = u[k] - cu;
            P4[k * 3] = cu + a * TURN_C - v[k] * TURN_S;
            P4[k * 3 + 1] = y[k];
            P4[k * 3 + 2] = a * TURN_S + v[k] * TURN_C;
        }
        C4[0] = c0;
        C4[1] = c1;
        C4[2] = c2;
        C4[3] = c3;
        sink.quad(P4, C4);
    }

    /**
     * Daz Bones' arm blade in the right arm's frame (the left arm: the caller mirrors x): a broad blade grown out of the outside of the
     * forearm from {@link #BLADE_ROOT} and running {@code length} down past the fist, its back along the arm, its cutting edge toward the
     * thumb side (+x, where a swipe leads), its section turned {@link #BLADE_TURN} round the arm; a thick spine of darker steel, a flat,
     * a bright bevel cut by a wavy temper line (the reference's waves), a white edge. {@code shimmer} (0..1) slides a glint along it.
     */
    public static void armBlade(FireMesh.Sink sink, double length, double coat, double shimmer) {
        double spine = -0.19;
        double half = 0.032; // half its thickness at the spine
        int dark = solid(coat(DARK, coat));
        int flat = solid(coat(FLAT, coat));
        int bevel = solid(coat(BEVEL, coat));
        int shine = solid(coat(SHINE, coat));
        int line = solid(edge(coat));
        for (int i = 0; i < BLADE_STEPS; i++) {
            double t0 = i / (double) BLADE_STEPS;
            double t1 = (i + 1) / (double) BLADE_STEPS;
            double x0 = spine + t0 * 0.05;
            double x1 = spine + t1 * 0.05;
            double y0 = BLADE_ROOT + t0 * length;
            double y1 = BLADE_ROOT + t1 * length;
            double w0 = width(t0);
            double w1 = width(t1);
            // the wavy temper: where the flat turns to the bevel, rolling as the reference's edge does
            double h0 = w0 * (0.5 + 0.13 * Math.sin(t0 * 21.0));
            double h1 = w1 * (0.5 + 0.13 * Math.sin(t1 * 21.0));
            int b0 = Math.abs(t0 - shimmer) < 0.09 ? shine : bevel;
            int b1 = Math.abs(t1 - shimmer) < 0.09 ? shine : bevel;
            double th0 = half * (1.0 - 0.6 * t0);
            double th1 = half * (1.0 - 0.6 * t1);
            for (int side = -1; side <= 1; side += 2) {
                // the flat (spine to temper), then the bevel (temper to edge) thinning to nothing
                bq(sink, spine, x0, y0, side * th0, dark, x0 + h0, y0, side * th0 * 0.8, flat, x1 + h1, y1, side * th1 * 0.8, flat, x1, y1,
                    side * th1, dark);
                bq(sink, spine, x0 + h0, y0, side * th0 * 0.8, b0, x0 + w0, y0, 0.0, b0, x1 + w1, y1, 0.0, b1, x1 + h1, y1, side * th1 * 0.8, b1);
            }
            // the back of the blade (its spine, square) and the white edge line along the front
            bq(sink, spine, x0, y0, -th0, dark, x0, y0, th0, dark, x1, y1, th1, dark, x1, y1, -th1, dark);
            bq(sink, spine, x0 + w0 - 0.016, y0, 0.004, line, x0 + w0 + 0.008, y0, 0.0, line, x1 + w1 + 0.008, y1, 0.0, line, x1 + w1 - 0.016,
                y1, 0.004, line);
            bq(sink, spine, x0 + w0 - 0.016, y0, -0.004, line, x0 + w0 + 0.008, y0, 0.0, line, x1 + w1 + 0.008, y1, 0.0, line,
                x1 + w1 - 0.016, y1, -0.004, line);
        }
    }

    /**
     * The finger blades (Spar Claw) in the right arm's frame: four thin curved blades out of the fingertips, side by side across the hand,
     * {@code out} (0..1) of their length grown, curling toward the palm (+x).
     */
    public static void claws(FireMesh.Sink sink, double coat, double out) {
        if (out <= 0.01) {
            return;
        }
        int dark = solid(coat(FLAT, coat));
        int bright = solid(coat(SHINE, coat));
        int line = solid(edge(coat));
        double hand = 0.6;
        for (int f = 0; f < 4; f++) {
            double z = -0.09 + f * 0.06;
            double length = (f == 0 || f == 3 ? 0.4 : 0.5) * out;
            double px = -0.07;
            double py = hand;
            int steps = 4;
            for (int i = 0; i < steps; i++) {
                double t0 = i / (double) steps;
                double t1 = (i + 1) / (double) steps;
                double ax = -0.07 + 0.09 * t0 * t0;
                double bx = -0.07 + 0.09 * t1 * t1;
                double ay = hand + length * t0;
                double by = hand + length * t1;
                double w0 = 0.03 * (1.0 - t0) + 0.006;
                double w1 = 0.03 * (1.0 - t1) + 0.006;
                FireMesh.quad(sink, ax - w0, ay, z, dark, ax + w0, ay, z, bright, bx + w1, by, z, bright, bx - w1, by, z, dark);
                FireMesh.quad(sink, ax + w0, ay, z - 0.008, line, ax + w0, ay, z + 0.008, line, bx + w1, by, z + 0.008, line, bx + w1, by,
                    z - 0.008, line);
                px = bx;
                py = by;
            }
            FireMesh.quad(sink, px - 0.004, py, z - 0.004, line, px + 0.004, py, z - 0.004, line, px + 0.004, py, z + 0.004, line, px - 0.004, py,
                z + 0.004, line);
        }
    }

    static final int DRILL_SIDES = 10;
    static final int DRILL_RINGS = 6;

    /**
     * Spiral Hollow's drill round the right forearm (the reference: white drill blades round the forearms): a cone from round the
     * forearm to a point past the fist, banded in a spiral that turns with {@code spin} (radians), white steel edged in grey.
     */
    public static void drill(FireMesh.Sink sink, double coat, double spin) {
        double top = 0.22;
        double tip = 1.2;
        double base = 0.19;
        int white = solid(coat(SHINE, coat));
        int grey = solid(coat(FLAT, coat));
        int dark = solid(coat(DARK, coat));
        for (int r = 0; r < DRILL_RINGS; r++) {
            double t0 = r / (double) DRILL_RINGS;
            double t1 = (r + 1) / (double) DRILL_RINGS;
            double y0 = top + (tip - top) * t0;
            double y1 = top + (tip - top) * t1;
            double r0 = base * (1.0 - t0 * t0 * 0.15 - t0 * 0.85);
            double r1 = base * (1.0 - t1 * t1 * 0.15 - t1 * 0.85);
            for (int k = 0; k < DRILL_SIDES; k++) {
                double a0 = k * 2.0 * Math.PI / DRILL_SIDES;
                double a1 = (k + 1) * 2.0 * Math.PI / DRILL_SIDES;
                // the spiral: a band's twist grows down the cone, the whole turning with the spin
                double tw0 = spin + t0 * 4.0;
                double tw1 = spin + t1 * 4.0;
                int band = ((k + r) % 2 == 0) ? white : grey;
                int edgeColour = (k + r) % 2 == 0 ? grey : dark;
                FireMesh.quad(sink, -0.06 + Math.cos(a0 + tw0) * r0, y0, Math.sin(a0 + tw0) * r0, band,
                    -0.06 + Math.cos(a1 + tw0) * r0, y0, Math.sin(a1 + tw0) * r0, edgeColour,
                    -0.06 + Math.cos(a1 + tw1) * r1, y1, Math.sin(a1 + tw1) * r1, edgeColour,
                    -0.06 + Math.cos(a0 + tw1) * r1, y1, Math.sin(a0 + tw1) * r1, band);
            }
        }
        // the flange where it meets the arm
        for (int k = 0; k < DRILL_SIDES; k++) {
            double a0 = k * 2.0 * Math.PI / DRILL_SIDES + spin;
            double a1 = (k + 1) * 2.0 * Math.PI / DRILL_SIDES + spin;
            FireMesh.quad(sink, -0.06 + Math.cos(a0) * 0.12, top - 0.02, Math.sin(a0) * 0.12, dark, -0.06 + Math.cos(a1) * 0.12, top - 0.02,
                Math.sin(a1) * 0.12, dark, -0.06 + Math.cos(a1) * base, top, Math.sin(a1) * base, grey, -0.06 + Math.cos(a0) * base, top,
                Math.sin(a0) * base, grey);
        }
    }

    /**
     * Atomic Spurt's skate blade under a foot, in the leg's frame (the sole at 0.75 down the leg): a thin steel runner from the heel to
     * past the toes, its front curled up, its bottom edge white.
     */
    public static void footBlade(FireMesh.Sink sink, double coat) {
        int flat = solid(coat(FLAT, coat));
        int bevel = solid(coat(BEVEL, coat));
        int line = solid(edge(coat));
        double sole = 0.75;
        double depth = 0.11;
        int steps = 6;
        for (int i = 0; i < steps; i++) {
            double t0 = i / (double) steps;
            double t1 = (i + 1) / (double) steps;
            double z0 = 0.16 - 0.5 * t0;
            double z1 = 0.16 - 0.5 * t1;
            // the runner's bottom curls up at the toes (z most negative: the front)
            double lift0 = t0 > 0.72 ? (t0 - 0.72) / 0.28 * depth * 0.9 : 0.0;
            double lift1 = t1 > 0.72 ? (t1 - 0.72) / 0.28 * depth * 0.9 : 0.0;
            double b0 = sole + depth - lift0;
            double b1 = sole + depth - lift1;
            for (int side = -1; side <= 1; side += 2) {
                double x = side * 0.012;
                FireMesh.quad(sink, x, sole, z0, flat, x, b0, z0, bevel, x, b1, z1, bevel, x, sole, z1, flat);
            }
            FireMesh.quad(sink, -0.012, b0, z0, line, 0.012, b0, z0, line, 0.012, b1, z1, line, -0.012, b1, z1, line);
        }
        // the mount under the sole
        FireMesh.quad(sink, -0.1, sole, 0.14, flat, 0.1, sole, 0.14, flat, 0.1, sole, -0.3, flat, -0.1, sole, -0.3, flat);
    }

    /**
     * Spider's steel skin over one body part's box (its model frame, blocks): the box a little larger than the part, its faces steel with
     * a bright rim, {@code shimmer} (0..1) a glint sliding down it; see-through enough that the skin's shape shows, as the body turned to
     * steel.
     */
    public static void steelBox(FireMesh.Sink sink, double x0, double y0, double z0, double x1, double y1, double z1, double coat,
                                double shimmer, double alpha) {
        double grow = 0.035;
        x0 -= grow;
        y0 -= grow;
        z0 -= grow;
        x1 += grow;
        y1 += grow;
        z1 += grow;
        int top = FireMesh.argb(alpha, coat(BEVEL, coat));
        int mid = FireMesh.argb(alpha, coat(SHINE, coat));
        int low = FireMesh.argb(alpha, coat(DARK, coat));
        int side = FireMesh.argb(alpha, coat(FireMesh.mix(DARK, 0x1C232C, 0.4), coat));
        double g = y0 + (y1 - y0) * shimmer;
        // each face banded from bright at its top (y0, the joint) to dark at its bottom, a glint where the shimmer is
        face(sink, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, top, low, g, mid);
        face(sink, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, mid, side, g, top);
        face(sink, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, mid, side, g, top);
        face(sink, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, top, low, g, mid);
        FireMesh.quad(sink, x0, y0, z0, top, x1, y0, z0, top, x1, y0, z1, top, x0, y0, z1, top);
        FireMesh.quad(sink, x0, y1, z1, side, x1, y1, z1, side, x1, y1, z0, side, x0, y1, z0, side);
    }

    /** A box face a, b (top), c, d (bottom): {@code topColour} to {@code lowColour}, cut at {@code glintY} by a band of {@code glint}. */
    static void face(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double cx, double cy, double cz,
                     double dx, double dy, double dz, int topColour, int lowColour, double glintY, int glint) {
        double span = cy - by;
        double t = span == 0 ? 0 : Math.max(0.0, Math.min(1.0, (glintY - by) / span));
        double mx0 = ax + (dx - ax) * t;
        double my0 = ay + (dy - ay) * t;
        double mz0 = az + (dz - az) * t;
        double mx1 = bx + (cx - bx) * t;
        double my1 = by + (cy - by) * t;
        double mz1 = bz + (cz - bz) * t;
        FireMesh.quad(sink, ax, ay, az, topColour, bx, by, bz, topColour, mx1, my1, mz1, glint, mx0, my0, mz0, glint);
        FireMesh.quad(sink, mx0, my0, mz0, glint, mx1, my1, mz1, glint, cx, cy, cz, lowColour, dx, dy, dz, lowColour);
    }

    // ------------------------------------------------------------------------------------------------------ in the world

    static final double[] B = new double[6];

    /**
     * A crescent of cutting force flying along (ux, uy, uz) (unit), its middle at (x, y, z), {@code roll} radians round its way (0 flat,
     * PI / 2 upright), {@code scale} its size (1 a tapped slash, 2.4 one charged full), {@code coat} the user's Armament (its glow dark
     * purple then): a white crescent bulging forward with a pale blue glow, two fading ghosts of it behind (the speed it carries).
     */
    public static void slash(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double roll, double scale,
                             double age, double coat) {
        FireMesh.basis(ux, uy, uz, B);
        double c = Math.cos(roll);
        double s = Math.sin(roll);
        // the crescent's own side (rolled round the way) and its plane's normal
        double sx = B[0] * c + B[3] * s;
        double sy = B[1] * c + B[4] * s;
        double sz = B[2] * c + B[5] * s;
        double nx = uy * sz - uz * sy;
        double ny = uz * sx - ux * sz;
        double nz = ux * sy - uy * sx;
        double radius = 0.95 * scale;
        double sweep = Math.toRadians(150);
        double grow = Math.min(1.0, 0.45 + age * 0.3);
        int core = coat(CUT_CORE, coat);
        int glow = coat > 0 ? FireMesh.mix(CUT_GLOW, COAT_GLINT, Math.min(1.0, coat)) : CUT_GLOW;
        for (int ghost = 0; ghost < 3; ghost++) {
            double back = ghost * 0.38 * scale;
            double fade = ghost == 0 ? 1.0 : ghost == 1 ? 0.45 : 0.2;
            double cx = x - ux * (radius + back);
            double cy = y - uy * (radius + back);
            double cz = z - uz * (radius + back);
            double start = startOf(nx, ny, nz, ux, uy, uz) - sweep / 2;
            FireMesh.arc(sink, cx, cy, cz, nx, ny, nz, radius * grow, 0.34 * scale, start, sweep, 0.0, glow, 0.5 * fade);
            FireMesh.arc(sink, cx, cy, cz, nx, ny, nz, radius * grow, 0.13 * scale, start, sweep, 0.0, core, 0.95 * fade);
        }
    }

    /** The angle (in FireMesh.arc's frame round the normal n) at which the arc points along u. */
    static double startOf(double nx, double ny, double nz, double ux, double uy, double uz) {
        FireMesh.basis(nx, ny, nz, B);
        return Math.atan2(ux * B[3] + uy * B[4] + uz * B[5], ux * B[0] + uy * B[1] + uz * B[2]);
    }

    /** Atomic Spar's X flying along (ux, uy, uz): two crescents crossed, one rolled each way. */
    public static void cross(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double scale, double age,
                             double coat) {
        slash(sink, x, y, z, ux, uy, uz, Math.PI / 4, scale, age, coat);
        slash(sink, x, y, z, ux, uy, uz, -Math.PI / 4, scale, age, coat);
        if (age < 3) {
            FireMesh.shell(sink, x, y, z, 0.5 * scale, coat(CUT_CORE, coat), 0.5 * (1.0 - age / 3.0), 0.0);
        }
    }

    /**
     * Spar Claw's marks in the air before the user (x, y, z, facing fx, fz flat): four thin parallel arcs slashed across, one way or the
     * other by {@code which}, {@code reach} out, fading over {@link #CLAW_LIFE}.
     */
    public static void clawMarks(FireMesh.Sink sink, double x, double y, double z, double fx, double fz, int which, double reach, double age,
                                 double coat) {
        double t = age / CLAW_LIFE;
        if (t >= 1.0) {
            return;
        }
        double alpha = t < 0.2 ? t / 0.2 : 1.0 - (t - 0.2) / 0.8;
        // tilted planes: the first swipe falls from the upper right, the second rises from the lower left
        double tilt = which == 0 ? 0.45 : -0.45;
        double sweep = Math.toRadians(80) * Math.min(1.0, age / 2.0);
        double rx = -fz;
        double rz = fx;
        double nx = rx * Math.sin(tilt);
        double ny = Math.cos(tilt);
        double nz = rz * Math.sin(tilt);
        int line = coat(CUT_CORE, coat);
        for (int k = 0; k < 4; k++) {
            double off = (k - 1.5) * 0.13;
            double cx = x + nx * off;
            double cy = y + ny * off;
            double cz = z + nz * off;
            double mid = startOf(nx, ny, nz, fx, 0, fz);
            double start = which == 0 ? mid + Math.toRadians(40) - sweep : mid - Math.toRadians(40);
            FireMesh.arc(sink, cx, cy, cz, nx, ny, nz, reach, 0.08, start, sweep, 0.0, line, 0.95 * alpha);
            FireMesh.arc(sink, cx, cy, cz, nx, ny, nz, reach, 0.22, start, sweep, 0.0, CUT_GLOW, 0.35 * alpha);
        }
    }

    /** Sparks off steel at (x, y, z), thrown round (dx, dy, dz) (unit, or 0 every way): a white flash, a dozen orange streaks falling. */
    public static void sparks(FireMesh.Sink sink, double x, double y, double z, double dx, double dy, double dz, double size, double age,
                              int seed) {
        double t = age / SPARK_LIFE;
        if (t >= 1.0) {
            return;
        }
        if (t < 0.3) {
            FireMesh.shell(sink, x, y, z, 0.18 + 0.3 * size, SHINE, 0.7 * (1.0 - t / 0.3), 0.0);
        }
        int n = 6 + (int) (6 * Math.min(1.0, size));
        for (int i = 0; i < n; i++) {
            double ax = FireMesh.rand(seed, i * 3) * 2.0 - 1.0;
            double ay = FireMesh.rand(seed, i * 3 + 1) * 1.6 - 0.3;
            double az = FireMesh.rand(seed, i * 3 + 2) * 2.0 - 1.0;
            double vx = ax + dx * 1.5;
            double vy = ay + dy * 1.5;
            double vz = az + dz * 1.5;
            double speed = (0.12 + 0.1 * FireMesh.rand(seed, i + 40)) * (0.6 + size);
            double fall = 0.035 * age * age;
            double px = x + vx * speed * age;
            double py = y + vy * speed * age - fall;
            double pz = z + vz * speed * age;
            FireMesh.streak(sink, px, py, pz, vx, vy - 0.07 * age, vz, 0.22 + 0.1 * size, 0.025, SPARK, 1.0 - t);
        }
    }

    /** A blow glancing off the steel body at (x, y, z): a hard white ring flashing out, a few sparks. */
    public static void glance(FireMesh.Sink sink, double x, double y, double z, double dx, double dz, double age, int seed) {
        double t = age / GLANCE_LIFE;
        if (t >= 1.0) {
            return;
        }
        double r = 0.15 + 0.6 * Math.sqrt(t);
        FireMesh.ring(sink, x, y, z, dx, 0.0, dz, r, 0.08, 0.02, SHINE, 0.9 * (1.0 - t));
        FireMesh.ring(sink, x, y, z, dx, 0.0, dz, r * 0.6, 0.05, 0.0, CUT_GLOW, 0.6 * (1.0 - t));
        sparks(sink, x, y, z, dx, 0.2, dz, 0.4, age, seed);
    }

    /**
     * Spiral Hollow's wake from A to B: a corkscrew of white cuts round the way the drills went, tightest at B, fading over
     * {@link #TRAIL_LIFE}.
     */
    public static void drillTrail(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double age,
                                  double coat) {
        double t = age / TRAIL_LIFE;
        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (t >= 1.0 || length < 0.1) {
            return;
        }
        double ux = dx / length;
        double uy = dy / length;
        double uz = dz / length;
        double alpha = 1.0 - t;
        int line = coat(CUT_CORE, coat);
        int n = Math.min(40, (int) (length / 0.35));
        for (int i = 0; i < n; i++) {
            double s = i / (double) Math.max(1, n - 1);
            double px = ax + dx * s;
            double py = ay + dy * s;
            double pz = az + dz * s;
            double radius = 0.55 + 0.25 * (1.0 - s) + 0.4 * t;
            double start = s * 9.0 + age * 0.6;
            FireMesh.arc(sink, px, py, pz, ux, uy, uz, radius, 0.09, start, 2.0, 0.25, line, 0.75 * alpha);
        }
        FireMesh.ray(sink, ax, ay, az, ux, uy, uz, length, 0.25, 0, 0, CUT_GLOW, 0.35 * alpha);
    }

    /** Sparkling Daisy's cut from A to B: one bright straight line of steel drawn through the air, with a glow round it, fading. */
    public static void daisyTrail(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double age,
                                  double coat) {
        double t = age / TRAIL_LIFE;
        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (t >= 1.0 || length < 0.1) {
            return;
        }
        double ux = dx / length;
        double uy = dy / length;
        double uz = dz / length;
        double alpha = 1.0 - t * t;
        double thin = 1.0 - t;
        FireMesh.ray(sink, ax, ay + 0.15, az, ux, uy, uz, length, 0.09 * thin + 0.02, 0, 0, coat(CUT_CORE, coat), alpha);
        FireMesh.ray(sink, ax, ay - 0.15, az, ux, uy, uz, length, 0.09 * thin + 0.02, 0, 0, coat(CUT_CORE, coat), alpha);
        FireMesh.ray(sink, ax, ay, az, ux, uy, uz, length, 0.5 * thin + 0.1, 0, 0, CUT_GLOW, 0.3 * alpha);
    }

    /**
     * Spar Break round a body (its middle at x, y, z): eight blades standing out of it, whirling round, and the white blur of their cuts
     * (two rings of arcs), {@code radius} out; fades in over its first ticks.
     */
    public static void spin(FireMesh.Sink sink, double x, double y, double z, double radius, double age, double coat) {
        double grow = Math.min(1.0, age / 3.0);
        double turn = age * 1.1;
        int flat = solid(coat(FLAT, coat));
        int bright = solid(coat(SHINE, coat));
        int line = solid(edge(coat));
        for (int k = 0; k < 8; k++) {
            double a = turn + k * Math.PI / 4.0;
            double ca = Math.cos(a);
            double sa = Math.sin(a);
            double ta = -sa;
            double tb = ca;
            double inner = 0.35;
            double outer = inner + (radius - inner) * grow * 0.75;
            double h = k % 2 == 0 ? 0.25 : -0.2;
            // a blade: wide at its root, a point at its tip, leaning back against the spin
            double rx = x + ca * inner;
            double rz = z + sa * inner;
            double tx = x + ca * outer - ta * 0.35;
            double tz = z + sa * outer - tb * 0.35;
            FireMesh.quad(sink, rx + ta * 0.12, y + h - 0.06, rz + tb * 0.12, flat, rx - ta * 0.12, y + h - 0.06, rz - tb * 0.12, bright,
                tx, y + h, tz, line, tx, y + h, tz, line);
            FireMesh.quad(sink, rx + ta * 0.12, y + h + 0.06, rz + tb * 0.12, flat, rx - ta * 0.12, y + h + 0.06, rz - tb * 0.12, bright,
                tx, y + h, tz, line, tx, y + h, tz, line);
        }
        int blur = coat(CUT_CORE, coat);
        FireMesh.arc(sink, x, y + 0.25, z, 0, 1, 0, radius * grow * 0.85, 0.3, turn * 1.3, Math.PI * 1.3, 0.0, blur, 0.5 * grow);
        FireMesh.arc(sink, x, y - 0.2, z, 0, 1, 0, radius * grow * 0.8, 0.25, turn * 1.3 + Math.PI, Math.PI * 1.3, 0.0, blur, 0.45 * grow);
        FireMesh.arc(sink, x, y, z, 0, 1, 0, radius * grow, 0.6, turn * 1.3 + 0.8, Math.PI * 1.6, 0.0, CUT_GLOW, 0.25 * grow);
    }

    /** One stretch of a skate mark on the ground from A to B: a thin bright line lying flat, {@code alpha} its fade. */
    public static void mark(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double alpha) {
        double dx = bx - ax;
        double dz = bz - az;
        double l = Math.sqrt(dx * dx + dz * dz);
        if (l < 1.0E-4 || alpha < 0.01) {
            return;
        }
        double sx = -dz / l * 0.03;
        double sz = dx / l * 0.03;
        int c = FireMesh.argb(alpha, SHINE);
        int soft = FireMesh.argb(alpha * 0.3, CUT_GLOW);
        FireMesh.quad(sink, ax - sx, ay, az - sz, c, ax + sx, ay, az + sz, c, bx + sx, by, bz + sz, c, bx - sx, by, bz - sz, c);
        FireMesh.quad(sink, ax - sx * 4, ay, az - sz * 4, soft, ax + sx * 4, ay, az + sz * 4, soft, bx + sx * 4, by, bz + sz * 4, soft,
            bx - sx * 4, by, bz - sz * 4, soft);
    }
}
