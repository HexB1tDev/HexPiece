package com.hexhaki.piece.supa;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.3.5: the Supa Supa no Mi's steel in 3D (the owner: "Turns any body part—such as fingers, arms, legs, or the entire body—into sharp
 * steel blades"; "haki blackens the blades as in makes them purpleish like the katanas"), after the owner's references (Daz Bones' arm
 * blade, steel with a wavy edge out of the forearm; his finger blades; Spiral Hollow's white drills round the forearms). Built in the house
 * style (POINT ONE: flat banded colours, alpha blended, depth tested, never additive; FireMesh's arcs, rings, rays and streaks for what
 * flies), pure so the previews draw the very same (tools/preview/SupaShots).
 *
 * <p>The blades on the body are the owner's katana steel (supa/SupaBlades, client/supa/SupaClient: the owner called this file's first
 * flat blades "really poor and poorly made" and chose katana steel). Here is what flies and flashes, eye-centred (x, y, z from the eye)
 * as every fruit's art: the flying slash ({@link #slash}), the X ({@link #cross}), the claw marks ({@link #clawMarks}), sparks
 * ({@link #sparks}), a blow glancing off ({@link #glance}), the drill's and the lunge's trails, Spar Break's spinning blades
 * ({@link #spin}) and the skate marks ({@link #mark}).
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
    public static final int CUT_GLOW = 0x7FAEEA;
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

    // ------------------------------------------------------------------------------------------------------ in the world

    static final double[] B = new double[6];

    /**
     * A crescent of cutting force flying along (ux, uy, uz) (unit), its middle at (x, y, z), {@code roll} radians round its way (0 level,
     * as the owner wanted the Light fruit's wave: "it's going vertical its very strange"), {@code scale} its size (1 a tapped slash, 2.4
     * one charged full), {@code coat} the user's Armament: built as the Light fruit's wave is (LightArt.wave, approved): a crescent bowed
     * out ahead the way it flies, its ends trailing, five bands deep so it reads from behind, each a wide pale-blue glow under a white
     * core (with Armament a very dark purple core in a violet glow).
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
        double width = 1.5 * scale * Math.min(1.0, 0.55 + age * 0.2);
        double cx = x - ux * width;
        double cy = y - uy * width;
        double cz = z - uz * width;
        double start = startOf(nx, ny, nz, ux, uy, uz) - 1.05;
        int core = coat(CUT_CORE, coat);
        int glow = coat > 0 ? FireMesh.mix(CUT_GLOW, COAT_GLINT, Math.min(1.0, coat)) : CUT_GLOW;
        for (int k = -2; k <= 2; k++) {
            double off = k * 0.08 * Math.min(1.6, scale);
            double depth = k == 0 ? 1.0 : Math.abs(k) == 1 ? 0.7 : 0.4;
            FireMesh.arc(sink, cx + nx * off, cy + ny * off, cz + nz * off, nx, ny, nz, width, width * 0.42 + 0.12, start, 2.1, 0.0, glow,
                0.7 * depth);
            FireMesh.arc(sink, cx + nx * off, cy + ny * off, cz + nz * off, nx, ny, nz, width, width * 0.18 + 0.06, start, 2.1, 0.0, core,
                depth);
        }
    }

    /** The angle    }

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
     * Spar Claw's marks in the air before the user (x, y, z its chest, facing fx, fz flat): four parallel claw cuts raked across, one way
     * or the other by {@code which}, {@code reach} out round the chest: each a white core in a pale-blue glow, two bands deep so they read
     * edge-on, opening as the swipe goes and fading over {@link #CLAW_LIFE}.
     */
    public static void clawMarks(FireMesh.Sink sink, double x, double y, double z, double fx, double fz, int which, double reach, double age,
                                 double coat) {
        double t = age / CLAW_LIFE;
        if (t >= 1.0) {
            return;
        }
        double alpha = t < 0.15 ? t / 0.15 : 1.0 - (t - 0.15) / 0.85;
        // tilted planes: the first swipe falls from the upper right, the second rises from the lower left
        double tilt = which == 0 ? 0.45 : -0.45;
        double sweep = Math.toRadians(95) * Math.min(1.0, 0.35 + age / 1.5);
        double rx = -fz;
        double rz = fx;
        double nx = rx * Math.sin(tilt);
        double ny = Math.cos(tilt);
        double nz = rz * Math.sin(tilt);
        int core = coat(CUT_CORE, coat);
        int glow = coat > 0 ? FireMesh.mix(CUT_GLOW, COAT_GLINT, Math.min(1.0, coat)) : CUT_GLOW;
        double mid = startOf(nx, ny, nz, fx, 0, fz);
        double start = which == 0 ? mid + Math.toRadians(48) - sweep : mid - Math.toRadians(48);
        for (int k = 0; k < 4; k++) {
            double off = (k - 1.5) * 0.17;
            for (int band = -1; band <= 1; band += 2) {
                double o = off + band * 0.035;
                double cx = x + nx * o;
                double cy = y + ny * o;
                double cz = z + nz * o;
                FireMesh.arc(sink, cx, cy, cz, nx, ny, nz, reach, 0.3, start, sweep, 0.0, glow, 0.45 * alpha);
                FireMesh.arc(sink, cx, cy, cz, nx, ny, nz, reach, 0.11, start, sweep, 0.0, core, alpha);
            }
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
     * Spiral Hollow's wake from A to B: rings of white cut round the way the drills went, each open where the thread runs and turned on
     * from the last (a drill's wake), a bright core and a pale-blue glow down its middle, tightest at B, fading over {@link #TRAIL_LIFE}.
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
        int core = coat(CUT_CORE, coat);
        int glow = coat > 0 ? FireMesh.mix(CUT_GLOW, COAT_GLINT, Math.min(1.0, coat)) : CUT_GLOW;
        int n = Math.min(28, (int) (length / 0.6));
        for (int i = 0; i < n; i++) {
            double s = (i + (age * 0.4) % 1.0) / (double) Math.max(1, n);
            double px = ax + dx * s;
            double py = ay + dy * s;
            double pz = az + dz * s;
            double radius = 0.45 + 0.25 * (1.0 - s) + 0.4 * t;
            // a ring cut open where the thread runs, turning along the way (a drill's wake)
            double start = s * 10.0 + age * 0.5;
            FireMesh.arc(sink, px, py, pz, ux, uy, uz, radius, 0.26, start, 4.4, 0.25, glow, 0.4 * alpha);
            FireMesh.arc(sink, px, py, pz, ux, uy, uz, radius, 0.09, start, 4.4, 0.25, core, 0.85 * alpha);
        }
        FireMesh.ray(sink, ax, ay, az, ux, uy, uz, length, 0.9 * (1.0 - 0.5 * t), 0, 0, glow, 0.35 * alpha);
        FireMesh.ray(sink, ax, ay, az, ux, uy, uz, length, 0.22 * (1.0 - 0.5 * t), 0, 0, core, 0.7 * alpha);
    }

    /**
     * Sparkling Daisy's cut from A to B: a bright cut of steel drawn straight through the air, thick at first and thinning as it fades, a
     * wide pale-blue glow round it, a second fainter cut crossing it at a slant (both blades out), over {@link #TRAIL_LIFE}.
     */
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
        double thin = 1.0 - 0.7 * t;
        int core = coat(CUT_CORE, coat);
        int glow = coat > 0 ? FireMesh.mix(CUT_GLOW, COAT_GLINT, Math.min(1.0, coat)) : CUT_GLOW;
        FireMesh.ray(sink, ax, ay + 0.2, az, ux, uy, uz, length, 1.3 * thin, 0, 0, glow, 0.35 * alpha);
        FireMesh.ray(sink, ax, ay + 0.2, az, ux, uy, uz, length, 0.32 * thin, 0, 0, core, alpha);
        FireMesh.ray(sink, ax, ay - 0.25, az, ux, uy - 0.03, uz, length, 0.7 * thin, 0, 0, glow, 0.25 * alpha);
        FireMesh.ray(sink, ax, ay - 0.25, az, ux, uy - 0.03, uz, length, 0.16 * thin, 0, 0, core, 0.7 * alpha);
    }

    /**
     * Spar Break's blur round a body (its middle at x, y, z): the white smear of the blades' cuts as they whirl (the blades themselves
     * are the owner's katana steel, client/supa/SupaClient), two rings of arcs chasing round and a pale glow, {@code radius} out; fades
     * in over its first ticks.
     */
    public static void spin(FireMesh.Sink sink, double x, double y, double z, double radius, double age, double coat) {
        double grow = Math.min(1.0, age / 3.0);
        double turn = age * 1.4;
        int core = coat(CUT_CORE, coat);
        int glow = coat > 0 ? FireMesh.mix(CUT_GLOW, COAT_GLINT, Math.min(1.0, coat)) : CUT_GLOW;
        for (int k = -1; k <= 1; k++) {
            double h = y + k * 0.16;
            double depth = k == 0 ? 1.0 : 0.6;
            for (int r = 0; r < 2; r++) {
                double start = turn + r * Math.PI;
                FireMesh.arc(sink, x, h, z, 0, 1, 0, radius * grow * 0.9, 0.7, start, Math.PI * 0.9, 0.0, glow, 0.45 * depth * grow);
                FireMesh.arc(sink, x, h, z, 0, 1, 0, radius * grow * 0.9, 0.22, start, Math.PI * 0.9, 0.0, core, 0.9 * depth * grow);
            }
        }
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
