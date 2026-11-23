package com.hexhaki.piece.gear;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.horo.AstralArt;
import com.hexhaki.piece.style.SkinBox;

/**
 * 3.4.4 (the owner: "Without Haki the leg whip move, becomes "Goof Ball" the user starts spinning super fast in a cartoon way and then
 * becomes a spinning ball of smoke and the player's skin mixed, punching in all directions, this is a held move, when it ends the user is
 * temporarily dizzy (with a dizzy symbol on top)"): Goof Ball as everyone sees it (pure; client/gear/NikaWhipClient and
 * tools/preview/NikaShots draw the very same quads).
 * <ul>
 *   <li>{@link #ball}: a cartoon fight cloud: banded grey-white puffs churning round a spinning middle, bulging and swirling, speed arcs
 *       whipping round it, dust kicked up under it, little POW stars where the punches land;</li>
 *   <li>{@link #limbs}: the user's own arms and legs (and now and then the head) in their own skin, shooting out of the cloud every which
 *       way, stretched like rubber, and snapping back in (a punch every {@link #PUNCH} ticks each);</li>
 *   <li>{@link #pop}: the ball bursting when it ends (its puffs flung out, a ring, a flash);</li>
 *   <li>{@link #dizzy}: cartoon stars circling over the head while the user is dizzy after it.</li>
 * </ul>
 * Everything alpha-blended, never additive (the owner's phone); no allocation per frame (static scratch arrays, the render thread only).
 */
public final class NikaGoof {
    private NikaGoof() {
    }

    /** The spin-up before the ball (gear5_goof_spin's, tools/nika_anims.py GOOF_SPIN), the ball's least and most life (ticks). */
    public static final int SPIN = 8;
    public static final int LEAST = 12;
    public static final int MOST = 100;
    /** The pop's life and the daze after it (ticks). */
    public static final double POP_LIFE = 10.0;
    public static final int DIZZY = 60;
    /** The ball: its radius and its middle over the feet (blocks); how fast it churns (radians a tick). */
    public static final double RADIUS = 1.05;
    public static final double MIDDLE = 0.95;
    static final double TURN = 0.55;
    static final int PUFFS = 16;
    /** Each limb's punch: out and back in this many ticks; how many limbs at once. */
    public static final double PUNCH = 4.0;
    static final int LIMBS = 6;
    /** A skin pixel in blocks (the player model's 1/16 at its 0.9375), and how much bigger the ball's limbs are drawn. */
    static final double PX = 0.9375 / 16.0;
    static final double LIMB_SCALE = 1.15;

    /** The fight cloud's bands (FireMesh.puff's palette while it is drawn): a cool grey shadow to white. */
    public static final int[] CLOUD = {0x55515E, 0x6C6876, 0x85818E, 0x9F9CA8, 0xBAB8C1, 0xD4D2D9, 0xEAE9ED, 0xFFFFFF};
    /** The dizzy stars: an ink outline, cartoon yellow, a pale heart. */
    static final int STAR_INK = 0x2A1E0C;
    static final int STAR_YELLOW = 0xFFD52B;
    static final int STAR_CORE = 0xFFF6B8;

    static final double[] B = new double[6];
    static final double[] DIR = new double[3];
    static final double[] CORNERS = new double[24];
    static final double[] XYZ = new double[12];
    static final float[] UV = new float[8];

    /** The cloud's puffs' directions (a golden spiral round the sphere), made once. */
    static final double[] SPIRAL = new double[PUFFS * 3];

    static {
        double golden = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int i = 0; i < PUFFS; i++) {
            double y = 1.0 - 2.0 * (i + 0.5) / PUFFS;
            double r = Math.sqrt(1.0 - y * y);
            SPIRAL[i * 3] = Math.cos(golden * i) * r;
            SPIRAL[i * 3 + 1] = y * 0.9;
            SPIRAL[i * 3 + 2] = Math.sin(golden * i) * r;
        }
    }

    /** How big the ball is {@code age} ticks after it formed: puffed out over three ticks. */
    public static double grow(double age) {
        double t = Math.max(0.0, Math.min(1.0, age / 3.0));
        return 1.0 - (1.0 - t) * (1.0 - t) * (1.0 - t);
    }

    /**
     * The fight cloud at ({@code x}, {@code y}, {@code z}) (its middle, eye-relative), {@code age} ticks after it formed: the puffs
     * churning round the middle, the speed arcs, the dust under it, the POW stars. About 4,500 quads up close.
     */
    public static void ball(FireMesh.Sink sink, double x, double y, double z, double age, int seed) {
        double g = grow(age);
        if (g < 0.01) {
            return;
        }
        double spin = age * TURN;
        FireMesh.palette(CLOUD);
        try {
            for (int i = 0; i < PUFFS; i++) {
                double a = spin * (0.8 + 0.4 * FireMesh.rand(seed, i)) + i * 0.3;
                double cos = Math.cos(a);
                double sin = Math.sin(a);
                double dx = SPIRAL[i * 3] * cos - SPIRAL[i * 3 + 2] * sin;
                double dy = SPIRAL[i * 3 + 1];
                double dz = SPIRAL[i * 3] * sin + SPIRAL[i * 3 + 2] * cos;
                double bulge = 0.56 + 0.14 * Math.sin(age * 1.7 + i * 2.3);
                double out = RADIUS * bulge * g;
                double r = RADIUS * (0.46 + 0.1 * Math.sin(age * 2.3 + i * 1.3)) * g;
                // lit from above: the top puffs white, the underside a grey shadow
                double heat = 0.56 + 0.32 * dy;
                FireMesh.puff(sink, x + dx * out, y + dy * out, z + dz * out, r, -dz, 0.0, dx, 0.25, 0.18, heat, 0.35, false, 0.97,
                    age * 1.5 + i * 7, seed + i * 31);
            }
            // dust kicked up round the bottom
            for (int i = 0; i < 4; i++) {
                double a = -spin * 1.3 + i * Math.PI * 0.5;
                double r = RADIUS * (0.85 + 0.1 * Math.sin(age + i));
                FireMesh.puff(sink, x + Math.cos(a) * r, y - MIDDLE * 0.82, z + Math.sin(a) * r, 0.26 * g, -Math.sin(a), 0.2, Math.cos(a),
                    0.6, 0.25, 0.42, 0.3, false, 0.75, age * 2 + i * 5, seed + 101 + i);
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
        // speed arcs whipping round it, each on its own tilt
        for (int k = 0; k < 4; k++) {
            double tilt = 0.35 + 0.25 * k;
            double turn = k * 1.7 + seed * 0.01;
            double ux = Math.sin(tilt) * Math.cos(turn);
            double uy = Math.cos(tilt);
            double uz = Math.sin(tilt) * Math.sin(turn);
            FireMesh.arc(sink, x, y, z, ux, uy, uz, RADIUS * (1.12 + 0.08 * k) * g, 0.09, spin * 1.6 + k * 1.9, 2.1, 0.0, 0xFFFFFF, 0.85);
        }
        // a little comic burst where a punch lands: a new one every two ticks somewhere on the surface, each alive six
        for (int k = 0; k < 3; k++) {
            double born = Math.floor(age / 2.0) * 2.0 - k * 2.0;
            double local = age - born;
            if (born < 2.0 || local > BURST_LIFE) {
                continue;
            }
            int s = seed + (int) born * 977;
            direction(s, 0.55);
            double out = RADIUS * 1.05;
            burst(sink, x + DIR[0] * out, y + DIR[1] * out, z + DIR[2] * out, local, 0.26, s);
        }
    }

    static final double BURST_LIFE = 6.0;
    static final double[] SPIKES = new double[16 * 3];

    /**
     * A small comic burst (a punch landing) at x y z facing the eye, {@code age} ticks old: an eight-spiked star in Dawn Pistol's POW
     * bands (the ink outline, white, a warm heart), popping out past its size and shrinking away, four impact lines off it.
     */
    public static void burst(FireMesh.Sink sink, double x, double y, double z, double age, double size, int seed) {
        if (age < 0.0 || age > BURST_LIFE) {
            return;
        }
        double dist = Math.sqrt(x * x + y * y + z * z);
        if (dist < 0.3) {
            return;
        }
        double vx = x / dist;
        double vy = y / dist;
        double vz = z / dist;
        double scale = age < 1.5 ? 1.25 * Math.sin(Math.PI * 0.5 * age / 1.5) : 1.25 - 0.25 * Math.min(1.0, (age - 1.5) / 1.5)
            - 0.6 * Math.max(0.0, (age - 3.5) / (BURST_LIFE - 3.5));
        double r = size * Math.max(0.05, scale);
        FireMesh.basis(vx, vy, vz, B);
        double turn = FireMesh.rand(seed, 1) * Math.PI;
        for (int k = 0; k < 16; k++) {
            double a = turn + k * Math.PI / 8.0;
            double rr = k % 2 == 0 ? 0.9 + 0.25 * FireMesh.rand(seed, k + 2) : 0.45;
            SPIKES[k * 3] = (B[0] * Math.cos(a) + B[3] * Math.sin(a)) * rr;
            SPIKES[k * 3 + 1] = (B[1] * Math.cos(a) + B[4] * Math.sin(a)) * rr;
            SPIKES[k * 3 + 2] = (B[2] * Math.cos(a) + B[5] * Math.sin(a)) * rr;
        }
        double alpha = age < 3.5 ? 1.0 : Math.max(0.0, 1.0 - (age - 3.5) / (BURST_LIFE - 3.5));
        spikes(sink, x, y, z, vx, vy, vz, 0.0, r * 1.2, FireMesh.argb(0.95 * alpha, NikaArt.POW_INK));
        spikes(sink, x, y, z, vx, vy, vz, dist * 0.004, r, FireMesh.argb(0.95 * alpha, NikaArt.POW_WHITE));
        spikes(sink, x, y, z, vx, vy, vz, dist * 0.008, r * 0.5, FireMesh.argb(0.9 * alpha, NikaArt.POW_CORE));
        for (int k = 0; k < 4; k++) {
            double a = turn + (k + 0.5) * Math.PI * 0.5;
            double ox = B[0] * Math.cos(a) + B[3] * Math.sin(a);
            double oy = B[1] * Math.cos(a) + B[4] * Math.sin(a);
            double oz = B[2] * Math.cos(a) + B[5] * Math.sin(a);
            FireMesh.ray(sink, x + ox * r * 1.2, y + oy * r * 1.2, z + oz * r * 1.2, ox, oy, oz, r * (0.8 + 0.6 * Math.min(1.0, age / 2.0)),
                r * 0.08, 0, 0.0, NikaArt.POW_WHITE, 0.85 * alpha);
        }
    }

    static void spikes(FireMesh.Sink sink, double x, double y, double z, double vx, double vy, double vz, double near, double size, int argb) {
        double cx = x - vx * near;
        double cy = y - vy * near;
        double cz = z - vz * near;
        for (int k = 0; k < 16; k++) {
            int j = (k + 1) % 16;
            FireMesh.quad(sink, cx, cy, cz, argb, cx + SPIKES[k * 3] * size, cy + SPIKES[k * 3 + 1] * size, cz + SPIKES[k * 3 + 2] * size,
                argb, cx + SPIKES[j * 3] * size, cy + SPIKES[j * 3 + 1] * size, cz + SPIKES[j * 3 + 2] * size, argb, cx + SPIKES[j * 3] * size,
                cy + SPIKES[j * 3 + 1] * size, cz + SPIKES[j * 3 + 2] * size, argb);
        }
    }

    /** A random direction under {@code seed} into {@link #DIR}, {@code flat}: how far it keeps to the level (0 any, 1 level). */
    static void direction(int seed, double flat) {
        double a = FireMesh.rand(seed, 1) * Math.PI * 2.0;
        double y = (FireMesh.rand(seed, 2) * 2.0 - 1.0) * (1.0 - flat) + 0.1;
        double r = Math.sqrt(Math.max(0.0, 1.0 - y * y));
        DIR[0] = Math.cos(a) * r;
        DIR[1] = y;
        DIR[2] = Math.sin(a) * r;
    }

    /** The skin's boxes: the arms (wide and slim), the legs, the head (SkinBox's faces, built once). */
    static final int[][] ARM_RIGHT = SkinBox.faces(SkinBox.RIGHT_ARM, false, 0, 12);
    static final int[][] ARM_LEFT = SkinBox.faces(SkinBox.LEFT_ARM, false, 0, 12);
    static final int[][] SLIM_RIGHT = SkinBox.faces(new SkinBox.Part(40, 16, 3, 12, 4, 40, 32), false, 0, 12);
    static final int[][] SLIM_LEFT = SkinBox.faces(new SkinBox.Part(32, 48, 3, 12, 4, 48, 48), false, 0, 12);
    static final int[][] LEG_RIGHT = SkinBox.faces(SkinBox.RIGHT_LEG, false, 0, 12);
    static final int[][] LEG_LEFT = SkinBox.faces(SkinBox.LEFT_LEG, false, 0, 12);
    static final int[][] HEAD = SkinBox.faces(SkinBox.HEAD, false, 0, 8);

    /** How far a limb is out ({@code u} 0 to 1 through its punch): shot out fast, held a moment, pulled back in. */
    public static double reach(double u) {
        if (u < 0.3) {
            double t = u / 0.3;
            return 1.0 - (1.0 - t) * (1.0 - t);
        }
        if (u < 0.5) {
            return 1.0;
        }
        double t = (u - 0.5) / 0.5;
        return 1.0 - t * t * (3.0 - 2.0 * t);
    }

    /**
     * The user's arms and legs punching out of the ball (middle at {@code x y z}, eye-relative), {@code age} ticks after it formed, in
     * their own skin ({@code slim}: three-pixel arms), each face's normal given for the light; only the faces turned to the eye (the
     * device culls nothing). Six limbs, each out and back every {@link #PUNCH} ticks in a new direction, stretched like rubber as they
     * go out; the head pops out of the top now and then.
     */
    public static void limbs(AstralArt.Skin skin, double x, double y, double z, double age, int seed, boolean slim) {
        double g = grow(age);
        if (g < 0.3) {
            return;
        }
        for (int k = 0; k < LIMBS; k++) {
            double phase = age / PUNCH + k * (1.0 / LIMBS) * 2.3;
            int cycle = (int) Math.floor(phase);
            double e = reach(phase - cycle);
            if (e < 0.05) {
                continue;
            }
            direction(seed + k * 7919 + cycle * 104729, 0.45);
            boolean arm = k % 2 == 0;
            int[][] faces = arm ? (k % 4 == 0 ? (slim ? SLIM_RIGHT : ARM_RIGHT) : (slim ? SLIM_LEFT : ARM_LEFT))
                : (k % 4 == 1 ? LEG_RIGHT : LEG_LEFT);
            double width = (arm && slim ? 3.0 : 4.0) * PX * LIMB_SCALE;
            double depth = 4.0 * PX * LIMB_SCALE;
            double length = 12.0 * PX * LIMB_SCALE * (1.0 + 0.55 * e);
            double tip = RADIUS * (0.5 + 1.25 * e) * g;
            double roll = FireMesh.rand(seed + cycle, k + 11) * Math.PI * 2.0;
            box(skin, faces, x, y, z, DIR[0], DIR[1], DIR[2], tip - length, tip, width, depth, roll);
        }
        // the head, popping out of the top for five ticks in every fourteen, looking about
        double h = (age + seed % 7) % 14.0;
        if (h < 5.0) {
            double e = Math.sin(Math.PI * h / 5.0);
            double size = 8.0 * PX * LIMB_SCALE;
            int cycle = (int) Math.floor((age + seed % 7) / 14.0);
            double yaw = FireMesh.rand(seed, cycle + 300) * Math.PI * 2.0;
            double top = RADIUS * (0.5 + 0.62 * e) * g;
            // the head's "down" is the box's along axis: from its crown to its chin, straight down; the face turned to yaw
            double fx = Math.cos(yaw);
            double fz = Math.sin(yaw);
            headBox(skin, x, y + top, z, fx, fz, size);
        }
    }

    /**
     * One skin box: from {@code from} to {@code to} out of the middle along ({@code ax ay az}) (its top at {@code from}, its bottom (the
     * hand or the foot) at {@code to}), {@code width} across, {@code depth} deep, turned {@code roll} about its length. Corner k: bit 0
     * the side, bit 1 along (0 the top), bit 2 the back (SkinBox's numbering: the model's x, its y down, its z), placed by a proper
     * rotation (side, along, front = along x side) so the skin is never mirrored.
     */
    static void box(AstralArt.Skin skin, int[][] faces, double x, double y, double z, double ax, double ay, double az, double from, double to,
                    double width, double depth, double roll) {
        FireMesh.basis(ax, ay, az, B);
        double c = Math.cos(roll);
        double s = Math.sin(roll);
        double sx = B[0] * c + B[3] * s;
        double sy = B[1] * c + B[4] * s;
        double sz = B[2] * c + B[5] * s;
        // front = along x side
        double fx = ay * sz - az * sy;
        double fy = az * sx - ax * sz;
        double fz = ax * sy - ay * sx;
        for (int k = 0; k < 8; k++) {
            double side = (k & 1) == 0 ? -width * 0.5 : width * 0.5;
            double along = (k & 2) == 0 ? from : to;
            double front = (k & 4) == 0 ? depth * 0.5 : -depth * 0.5;
            CORNERS[k * 3] = x + ax * along + sx * side + fx * front;
            CORNERS[k * 3 + 1] = y + ay * along + sy * side + fy * front;
            CORNERS[k * 3 + 2] = z + az * along + sz * side + fz * front;
        }
        faces(skin, faces, 64.0F);
    }

    /** The head: a cube {@code size} across, its crown up, its face turned to ({@code fx}, 0, {@code fz}), its middle at x y z. */
    static void headBox(AstralArt.Skin skin, double x, double y, double z, double fx, double fz, double size) {
        // side = along x front with along straight down: (0,-1,0) x (fx,0,fz) = (-fz, 0, fx)... chosen so front = along x side holds
        double sx = fz;
        double sz = -fx;
        for (int k = 0; k < 8; k++) {
            double side = (k & 1) == 0 ? -size * 0.5 : size * 0.5;
            double down = (k & 2) == 0 ? -size * 0.5 : size * 0.5;
            double front = (k & 4) == 0 ? size * 0.5 : -size * 0.5;
            CORNERS[k * 3] = x + sx * side + fx * front;
            CORNERS[k * 3 + 1] = y - down;
            CORNERS[k * 3 + 2] = z + sz * side + fz * front;
        }
        faces(skin, HEAD, 64.0F);
    }

    /** The faces of the box in {@link #CORNERS} turned to the eye (the origin), with their skin rectangles and outward normals. */
    static void faces(AstralArt.Skin skin, int[][] faces, float texture) {
        double mx = 0;
        double my = 0;
        double mz = 0;
        for (int k = 0; k < 8; k++) {
            mx += CORNERS[k * 3] / 8;
            my += CORNERS[k * 3 + 1] / 8;
            mz += CORNERS[k * 3 + 2] / 8;
        }
        int argb = 0xFFFFFFFF;
        for (int[] face : faces) {
            for (int k = 0; k < 4; k++) {
                System.arraycopy(CORNERS, face[k] * 3, XYZ, k * 3, 3);
            }
            double ax = XYZ[3] - XYZ[0];
            double ay = XYZ[4] - XYZ[1];
            double az = XYZ[5] - XYZ[2];
            double bx = XYZ[9] - XYZ[0];
            double by = XYZ[10] - XYZ[1];
            double bz = XYZ[11] - XYZ[2];
            double nx = ay * bz - az * by;
            double ny = az * bx - ax * bz;
            double nz = ax * by - ay * bx;
            double nl = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (nl < 1.0E-12) {
                continue;
            }
            double cx = (XYZ[0] + XYZ[6]) * 0.5;
            double cy = (XYZ[1] + XYZ[7]) * 0.5;
            double cz = (XYZ[2] + XYZ[8]) * 0.5;
            double out = nx * (cx - mx) + ny * (cy - my) + nz * (cz - mz) < 0 ? -1.0 / nl : 1.0 / nl;
            if ((nx * cx + ny * cy + nz * cz) * out > 0.0) {
                continue; // turned away from the eye
            }
            UV[0] = face[4] / texture;
            UV[1] = face[5] / texture;
            UV[2] = face[6] / texture;
            UV[3] = face[5] / texture;
            UV[4] = face[6] / texture;
            UV[5] = face[7] / texture;
            UV[6] = face[4] / texture;
            UV[7] = face[7] / texture;
            skin.quad(XYZ, UV, argb, nx * out, ny * out, nz * out);
        }
    }

    /**
     * The ball bursting ({@code age} ticks ago, middle at x y z): its puffs flung out and thinning, a white ring racing out, a flash. About
     * 2,500 quads at its start.
     */
    public static void pop(FireMesh.Sink sink, double x, double y, double z, double age, int seed) {
        if (age < 0.0 || age > POP_LIFE) {
            return;
        }
        double t = age / POP_LIFE;
        double fade = 1.0 - t * t;
        FireMesh.palette(CLOUD);
        try {
            for (int i = 0; i < 9; i++) {
                direction(seed + i * 613, 0.2);
                double out = RADIUS * (0.4 + 1.6 * (1.0 - (1.0 - t) * (1.0 - t)));
                double r = RADIUS * 0.42 * (1.0 + 0.5 * t) * (1.0 - 0.5 * t);
                FireMesh.puff(sink, x + DIR[0] * out, y + DIR[1] * out, z + DIR[2] * out, r, DIR[0], DIR[1], DIR[2], 0.5, 0.25,
                    0.6 + 0.25 * DIR[1], 0.35, false, 0.95 * fade, age * 2, seed + i);
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
        FireMesh.ring(sink, x, y - MIDDLE * 0.6, z, 0, 1, 0, RADIUS * (0.6 + 2.4 * t), 0.35, 0.12, 0xFFFFFF, 0.8 * fade);
        if (age < 3.0) {
            FireMesh.shell(sink, x, y, z, RADIUS * (0.8 + 0.4 * age), 0xFFFFFF, 0.25 * (1.0 - age / 3.0), 0.0);
        }
    }

    /**
     * The dizzy stars over the head ({@code x y z}: just over its top, eye-relative), {@code age} ticks into the daze of {@code life}:
     * five cartoon stars circling round in a tilted ring, bobbing, each turned to the eye, popping in at the start and out at the end.
     */
    public static void dizzy(FireMesh.Sink sink, double x, double y, double z, double age, double life, int seed) {
        if (age < 0.0 || age > life) {
            return;
        }
        double in = Math.min(1.0, age / 4.0);
        double out = Math.min(1.0, (life - age) / 6.0);
        double size = 0.11 * in * out;
        if (size < 0.01) {
            return;
        }
        for (int k = 0; k < 5; k++) {
            double a = age * 0.32 + k * Math.PI * 2.0 / 5.0;
            double px = x + Math.cos(a) * 0.42;
            double pz = z + Math.sin(a) * 0.42;
            double py = y + 0.18 + 0.06 * Math.sin(a) + 0.03 * Math.sin(age * 0.9 + k);
            star(sink, px, py, pz, size * (0.85 + 0.3 * FireMesh.rand(seed, k)), age * 0.2 + k);
        }
    }

    static final double[] STAR = new double[10 * 3];

    /** A five-pointed cartoon star at x y z facing the eye, {@code size} its radius, turned {@code spin}: ink, yellow, a pale heart. */
    static void star(FireMesh.Sink sink, double x, double y, double z, double size, double spin) {
        double dist = Math.sqrt(x * x + y * y + z * z);
        if (dist < 0.2) {
            return;
        }
        double vx = x / dist;
        double vy = y / dist;
        double vz = z / dist;
        FireMesh.basis(vx, vy, vz, B);
        for (int k = 0; k < 10; k++) {
            double a = spin + k * Math.PI / 5.0;
            double r = k % 2 == 0 ? 1.0 : 0.45;
            STAR[k * 3] = (B[0] * Math.cos(a) + B[3] * Math.sin(a)) * r;
            STAR[k * 3 + 1] = (B[1] * Math.cos(a) + B[4] * Math.sin(a)) * r;
            STAR[k * 3 + 2] = (B[2] * Math.cos(a) + B[5] * Math.sin(a)) * r;
        }
        // far to near, each band a hair nearer the eye (one plane would speckle)
        starBand(sink, x, y, z, vx, vy, vz, dist * 0.0, size * 1.3, FireMesh.argb(0.95, STAR_INK));
        starBand(sink, x, y, z, vx, vy, vz, dist * 0.004, size, FireMesh.argb(1.0, STAR_YELLOW));
        starBand(sink, x, y, z, vx, vy, vz, dist * 0.008, size * 0.45, FireMesh.argb(0.95, STAR_CORE));
    }

    static void starBand(FireMesh.Sink sink, double x, double y, double z, double vx, double vy, double vz, double near, double size, int argb) {
        double cx = x - vx * near;
        double cy = y - vy * near;
        double cz = z - vz * near;
        for (int k = 0; k < 10; k++) {
            int j = (k + 1) % 10;
            FireMesh.quad(sink, cx, cy, cz, argb, cx + STAR[k * 3] * size, cy + STAR[k * 3 + 1] * size, cz + STAR[k * 3 + 2] * size, argb,
                cx + STAR[j * 3] * size, cy + STAR[j * 3 + 1] * size, cz + STAR[j * 3 + 2] * size, argb, cx + STAR[j * 3] * size,
                cy + STAR[j * 3 + 1] * size, cz + STAR[j * 3 + 2] * size, argb);
        }
    }

    /**
     * One's own first person inside the ball ({@code age} ticks after it formed): churning puffs framing the view (left, right, below,
     * the corners), turning round the look, and the user's own fists jabbing in from the lower corners in turn. The view's frame:
     * forward ({@code f}), up ({@code u}) and right ({@code r}), unit, eye-relative.
     */
    public static void firstPerson(FireMesh.Sink sink, AstralArt.Skin skin, double age, int seed, boolean slim, double fx, double fy, double fz,
                                   double ux, double uy, double uz, double rx, double ry, double rz) {
        double g = grow(age);
        if (sink != null) {
            FireMesh.palette(CLOUD);
            try {
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI * 2.0 / 8.0 + age * 0.35;
                    double ca = Math.cos(a);
                    double sa = Math.sin(a);
                    double d = 1.5;
                    double spread = 0.95 + 0.12 * Math.sin(age * 1.3 + i);
                    double px = fx * d + (rx * ca + ux * sa) * spread;
                    double py = fy * d + (ry * ca + uy * sa) * spread;
                    double pz = fz * d + (rz * ca + uz * sa) * spread;
                    FireMesh.puff(sink, px, py, pz, 0.42 * g, -sa, 0, ca, 0.3, 0.2, 0.58 + 0.3 * sa, 0.35, false, 0.9, age * 1.5 + i * 3,
                        seed + i);
                }
            } finally {
                FireMesh.palette(FireMesh.BAND);
            }
        }
        if (skin != null) {
            for (int k = 0; k < 2; k++) {
                double phase = age / (PUNCH * 0.5) + k * 0.5;
                double e = reach(phase - Math.floor(phase));
                double side = k == 0 ? 1.0 : -1.0;
                // from below the view's lower corner toward a little off the middle
                double bx = rx * 0.36 * side - ux * 0.42 + fx * 0.15;
                double by = ry * 0.36 * side - uy * 0.42 + fy * 0.15;
                double bz = rz * 0.36 * side - uz * 0.42 + fz * 0.15;
                double tx = fx * 1.0 + rx * 0.12 * side - ux * 0.08;
                double ty = fy * 1.0 + ry * 0.12 * side - uy * 0.08;
                double tz = fz * 1.0 + rz * 0.12 * side - uz * 0.08;
                double ax = tx - bx;
                double ay = ty - by;
                double az = tz - bz;
                double l = Math.sqrt(ax * ax + ay * ay + az * az);
                ax /= l;
                ay /= l;
                az /= l;
                double length = 12.0 * PX;
                double out = 0.25 + 0.6 * e;
                int[][] faces = k == 0 ? (slim ? SLIM_RIGHT : ARM_RIGHT) : (slim ? SLIM_LEFT : ARM_LEFT);
                box(skin, faces, bx, by, bz, ax, ay, az, out - length, out, (slim ? 3.0 : 4.0) * PX, 4.0 * PX, side > 0 ? 0.0 : Math.PI);
            }
        }
    }
}
