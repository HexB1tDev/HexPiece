package com.hexhaki.piece.horo;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.3.6: the Horo Horo no Mi's ghosts in 3D, after the owner's references (Perona's Negative Hollows: round white see-through ghosts,
 * tongues out, wispy tails; the Toku Hollow: a giant round grinning ghost). Built in the house style (POINT ONE: alpha blended, depth
 * tested, never additive; the explosions banded smoke as the Magma fruit's fire is, in a ghost's lavender to white), eye-centred (x, y, z
 * from the eye) as every fruit's art, pure so the previews draw the very same (tools/preview/HoroShots).
 *
 * <p>A ghost is one smooth body (no boxes, no flat cards: the owner's word on Supa's first flat blades was "really poor and poorly
 * made"): a round head flowing back into a tail that sways and curls to a point, see-through at its middle and bright at its rim (as a
 * ghost seen through), its tail fading out; on the front of its head a face: the Negative Hollow's droopy half-shut eyes, open drooping
 * mouth and pink tongue lolling out; the Mini Hollow's round eyes and small smile; the Toku Hollow's wide grin full of teeth.
 */
public final class HoroArt {
    /** A ghost's body, its rim, the inside of a mouth, eyes, the tongue, teeth. */
    public static final int BODY = 0xF2EEFF;
    public static final int CORE = 0xD6CCF2;
    public static final int RIM = 0xFFFFFF;
    public static final int MOUTH = 0x3A1832;
    public static final int EYE = 0x241C2E;
    public static final int TONGUE = 0xF2919F;
    public static final int TEETH = 0xFFFFF4;
    /** The banded smoke of a ghost's burst (FireMesh.puff's palette while it is drawn): deep violet to white. */
    public static final int[] SMOKE = {0x2E2048, 0x47336C, 0x654E92, 0x8670B6, 0xA897D4, 0xC9BEEA, 0xE6E0F8, 0xFFFFFF};
    /** Despair: the gloom lines' blue, the cloud's grey. */
    public static final int GLOOM = 0x343A78;
    /** The gloom's raincloud: a storm's blue-grey, dark to light (FireMesh.puff's palette while it is drawn). */
    public static final int[] RAINCLOUD = {0x1C1E2C, 0x272A3C, 0x34384E, 0x434860, 0x545A74, 0x686E88, 0x7E849C, 0x969BB0};
    public static final int CLOUD = 0x4A4660;

    /** The kinds of ghost. */
    public static final int NEGATIVE = 0;
    public static final int MINI = 1;
    public static final int TOKU = 2;

    /** How long a burst shows (ticks): a Mini Hollow's, the Toku Hollow's. */
    public static final double POP_LIFE = 16;
    public static final double BOOM_LIFE = 30;

    private HoroArt() {
    }

    static final int SIDES = 14;
    static final int HEAD_RINGS = 6;
    static final int TAIL_RINGS = 9;
    static final double[] RING = new double[(HEAD_RINGS + TAIL_RINGS + 1) * (SIDES + 1) * 3];
    static final int[] SHADE = new int[(HEAD_RINGS + TAIL_RINGS + 1) * (SIDES + 1)];
    static final double[] F = new double[9];

    /**
     * The ghost's frame into {@link #F}: its way forward (fx, fy, fz) made unit, its right and its up square to it (the up kept near
     * the world's up).
     */
    static void frame(double fx, double fy, double fz) {
        double l = Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (l < 1.0E-6) {
            fx = 0;
            fy = 0;
            fz = 1;
            l = 1;
        }
        fx /= l;
        fy /= l;
        fz /= l;
        // right = f x up(0,1,0); if f is nearly up, any right
        double rx = -fz;
        double ry = 0.0;
        double rz = fx;
        double rl = Math.sqrt(rx * rx + rz * rz);
        if (rl < 1.0E-4) {
            rx = 1;
            rz = 0;
            rl = 1;
        }
        rx /= rl;
        rz /= rl;
        double ux = ry * fz - rz * fy;
        double uy = rz * fx - rx * fz;
        double uz = rx * fy - ry * fx;
        F[0] = fx;
        F[1] = fy;
        F[2] = fz;
        F[3] = rx;
        F[4] = ry;
        F[5] = rz;
        F[6] = ux;
        F[7] = uy;
        F[8] = uz;
    }

    /**
     * A ghost of {@code kind}, its head's middle at (x, y, z), facing (fx, fy, fz), its head {@code size} across (blocks), {@code age}
     * ticks old (its tail sways with it), {@code alpha} of it there (fading in or out).
     */
    public static void ghost(FireMesh.Sink sink, double x, double y, double z, double fx, double fy, double fz, double size, int kind,
                             double age, double alpha, int seed) {
        if (alpha < 0.01 || size < 0.02) {
            return;
        }
        frame(fx, fy, fz);
        double r = size * 0.5;
        double tail = r * (kind == TOKU ? 2.0 : 2.6);
        double sway = age * 0.18 + seed * 1.7;
        int rings = HEAD_RINGS + TAIL_RINGS;
        // the rings: the head's front half (a pole at its face to its widest), then the tail narrowing to its curled point
        for (int i = 0; i <= rings; i++) {
            double cx;
            double cy;
            double cz;
            double rad;
            double fade;
            if (i <= HEAD_RINGS) {
                double th = Math.PI / 2 * i / HEAD_RINGS;
                double along = r * Math.cos(th);
                cx = x + F[0] * along;
                cy = y + F[1] * along;
                cz = z + F[2] * along;
                rad = r * Math.sin(th);
                fade = 1.0;
            } else {
                double t = (i - HEAD_RINGS) / (double) TAIL_RINGS;
                double back = tail * t;
                double down = r * (0.55 * t * t + 0.25 * Math.sin(Math.PI * 1.3 * t + sway) * t);
                double side = r * 0.4 * Math.sin(2 * Math.PI * t + sway * 0.8) * t;
                cx = x - F[0] * back + F[3] * side - F[6] * down;
                cy = y - F[1] * back + F[4] * side - F[7] * down;
                cz = z - F[2] * back + F[5] * side - F[8] * down;
                rad = r * Math.pow(1.0 - t, 1.25) * (1.0 + 0.08 * Math.sin(3 * Math.PI * t + sway)) + 0.004;
                fade = 1.0 - 0.75 * t;
            }
            for (int k = 0; k <= SIDES; k++) {
                double a = 2 * Math.PI * k / SIDES;
                double ca = Math.cos(a);
                double sa = Math.sin(a);
                double nx = F[3] * ca + F[6] * sa;
                double ny = F[4] * ca + F[7] * sa;
                double nz = F[5] * ca + F[8] * sa;
                double px = cx + nx * rad;
                double py = cy + ny * rad;
                double pz = cz + nz * rad;
                int v = i * (SIDES + 1) + k;
                RING[v * 3] = px;
                RING[v * 3 + 1] = py;
                RING[v * 3 + 2] = pz;
                // the head's normal from its middle; the tail's from its ring's middle
                double qx = i <= HEAD_RINGS ? px - x : px - cx;
                double qy = i <= HEAD_RINGS ? py - y : py - cy;
                double qz = i <= HEAD_RINGS ? pz - z : pz - cz;
                double ql = Math.max(1.0E-6, Math.sqrt(qx * qx + qy * qy + qz * qz));
                double el = Math.max(1.0E-6, Math.sqrt(px * px + py * py + pz * pz));
                double facing = Math.abs((qx * px + qy * py + qz * pz) / (ql * el));
                double edge = 1.0 - facing;
                double a2 = alpha * fade * (0.16 + 0.66 * Math.pow(edge, 1.5));
                int rgb = edge > 0.55 ? RIM : FireMesh.mix(CORE, BODY, Math.min(1.0, edge * 1.8));
                SHADE[v] = FireMesh.argb(a2, rgb);
            }
        }
        for (int i = 0; i < rings; i++) {
            for (int k = 0; k < SIDES; k++) {
                int a = i * (SIDES + 1) + k;
                int b = a + 1;
                int c = a + SIDES + 2;
                int d = a + SIDES + 1;
                FireMesh.quad(sink, RING[a * 3], RING[a * 3 + 1], RING[a * 3 + 2], SHADE[a], RING[b * 3], RING[b * 3 + 1], RING[b * 3 + 2], SHADE[b],
                    RING[c * 3], RING[c * 3 + 1], RING[c * 3 + 2], SHADE[c], RING[d * 3], RING[d * 3 + 1], RING[d * 3 + 2], SHADE[d]);
            }
        }
        face(sink, x, y, z, r, kind, age, alpha);
        // a soft glow round it (a ghost lights the dark a little)
        FireMesh.shell(sink, x - F[0] * r * 0.4, y - F[1] * r * 0.4, z - F[2] * r * 0.4, r * 2.0, BODY, 0.12 * alpha, 0.0);
    }

    static final double[] P = new double[3];

    /** A point on the head (its middle x, y, z, its radius r) by angles right {@code h} and up {@code v} of its face, out {@code out}. */
    static void on(double x, double y, double z, double r, double h, double v, double out) {
        double dx = F[0] + F[3] * Math.tan(h) + F[6] * Math.tan(v);
        double dy = F[1] + F[4] * Math.tan(h) + F[7] * Math.tan(v);
        double dz = F[2] + F[5] * Math.tan(h) + F[8] * Math.tan(v);
        double l = Math.sqrt(dx * dx + dy * dy + dz * dz);
        P[0] = x + dx / l * r * out;
        P[1] = y + dy / l * r * out;
        P[2] = z + dz / l * r * out;
    }

    static final int FAN = 10;
    static final double[] FAN_XYZ = new double[(FAN + 1) * 3];

    /**
     * A flat shape on the head's face round angles ({@code h}, {@code v}): an ellipse {@code w} by {@code t} (radians) from {@code from}
     * to {@code to} of its turn (0 its right, PI/2 its top), in {@code rgb}: eyes, mouths.
     */
    static void patch(FireMesh.Sink sink, double x, double y, double z, double r, double h, double v, double w, double t, double from,
                      double to, int argb) {
        on(x, y, z, r, h, v, 1.015);
        double mx = P[0];
        double my = P[1];
        double mz = P[2];
        for (int k = 0; k <= FAN; k++) {
            double a = from + (to - from) * k / FAN;
            on(x, y, z, r, h + w * Math.cos(a), v + t * Math.sin(a), 1.015);
            FAN_XYZ[k * 3] = P[0];
            FAN_XYZ[k * 3 + 1] = P[1];
            FAN_XYZ[k * 3 + 2] = P[2];
        }
        for (int k = 0; k < FAN; k++) {
            FireMesh.quad(sink, mx, my, mz, argb, FAN_XYZ[k * 3], FAN_XYZ[k * 3 + 1], FAN_XYZ[k * 3 + 2], argb, FAN_XYZ[k * 3 + 3],
                FAN_XYZ[k * 3 + 4], FAN_XYZ[k * 3 + 5], argb, mx, my, mz, argb);
        }
    }

    /** The face on the head's front, by kind. */
    static void face(FireMesh.Sink sink, double x, double y, double z, double r, int kind, double age, double alpha) {
        int eye = FireMesh.argb(0.95 * alpha, EYE);
        int mouth = FireMesh.argb(0.92 * alpha, MOUTH);
        if (kind == NEGATIVE) {
            // droopy, half-shut eyes: the lower halves of two wide ellipses, a heavy lid line over each
            for (int side = -1; side <= 1; side += 2) {
                patch(sink, x, y, z, r, side * 0.38, 0.2, 0.2, 0.15, Math.PI, 2 * Math.PI, eye);
                patch(sink, x, y, z, r, side * 0.38, 0.215, 0.23, 0.03, 0.0, 2 * Math.PI, eye);
            }
            // a wide drooping mouth, open
            patch(sink, x, y, z, r, 0.0, -0.17, 0.3, 0.16, Math.PI, 2 * Math.PI, mouth);
            patch(sink, x, y, z, r, 0.0, -0.17, 0.3, 0.04, 0.0, Math.PI, mouth);
            // the tongue lolling out of it, swinging a little
            on(x, y, z, r, 0.0, -0.27, 0.98);
            double tx = P[0];
            double ty = P[1];
            double tz = P[2];
            double swing = 0.15 * Math.sin(age * 0.25);
            double wx = F[0] * 0.55 - F[6] * 0.8 + F[3] * swing;
            double wy = F[1] * 0.55 - F[7] * 0.8 + F[4] * swing;
            double wz = F[2] * 0.55 - F[8] * 0.8 + F[5] * swing;
            tongue(sink, tx, ty, tz, wx, wy, wz, r * 0.5, r * 0.13, alpha);
        } else if (kind == MINI) {
            for (int side = -1; side <= 1; side += 2) {
                patch(sink, x, y, z, r, side * 0.32, 0.16, 0.11, 0.15, 0.0, 2 * Math.PI, eye);
            }
            patch(sink, x, y, z, r, 0.0, -0.2, 0.22, 0.09, Math.PI, 2 * Math.PI, mouth);
        } else {
            // the Toku Hollow: round staring eyes under heavy brows, a grin from cheek to cheek, a row of teeth along each lip
            for (int side = -1; side <= 1; side += 2) {
                patch(sink, x, y, z, r, side * 0.3, 0.2, 0.13, 0.16, 0.0, 2 * Math.PI, eye);
                patch(sink, x, y, z, r, side * 0.32, 0.4, 0.17, 0.035, 0.0, 2 * Math.PI, eye);
            }
            patch(sink, x, y, z, r, 0.0, -0.12, 0.62, 0.28, Math.PI, 2 * Math.PI, mouth);
            patch(sink, x, y, z, r, 0.0, -0.12, 0.62, 0.035, 0.0, Math.PI, mouth);
            int teeth = FireMesh.argb(0.95 * alpha, TEETH);
            for (int k = -4; k <= 4; k++) {
                double h = k * 0.12;
                double drop = 0.28 * Math.sqrt(Math.max(0.0, 1.0 - (h / 0.62) * (h / 0.62)));
                patch(sink, x, y, z, r, h, -0.14, 0.045, 0.06, Math.PI, 2 * Math.PI, teeth);
                if (Math.abs(k) < 4) {
                    patch(sink, x, y, z, r, h, -0.12 - drop + 0.03, 0.04, 0.05, 0.0, Math.PI, teeth);
                }
            }
        }
    }

    static final int TONGUE_SIDES = 8;
    static final double[] T_RING = new double[4 * (TONGUE_SIDES + 1) * 3];
    static final double[] T_BASIS = new double[6];
    static final double[] T_ALONG = {0.0, 0.45, 0.85, 1.0};
    static final double[] T_ROUND = {0.75, 1.0, 0.85, 0.25};

    /** The tongue: a soft pink lobe from (x, y, z) along (wx, wy, wz), {@code length} long, {@code radius} round, its tip rounded. */
    static void tongue(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double length, double radius,
                       double alpha) {
        double wl = Math.sqrt(wx * wx + wy * wy + wz * wz);
        wx /= wl;
        wy /= wl;
        wz /= wl;
        double[] b = T_BASIS;
        FireMesh.basis(wx, wy, wz, b);
        double[] along = T_ALONG;
        double[] round = T_ROUND;
        for (int i = 0; i < 4; i++) {
            double cx = x + wx * length * along[i];
            double cy = y + wy * length * along[i];
            double cz = z + wz * length * along[i];
            for (int k = 0; k <= TONGUE_SIDES; k++) {
                double a = 2 * Math.PI * k / TONGUE_SIDES;
                // flattened: wide across, thin through
                double ox = b[0] * Math.cos(a) * 1.3 + b[3] * Math.sin(a) * 0.55;
                double oy = b[1] * Math.cos(a) * 1.3 + b[4] * Math.sin(a) * 0.55;
                double oz = b[2] * Math.cos(a) * 1.3 + b[5] * Math.sin(a) * 0.55;
                int v = i * (TONGUE_SIDES + 1) + k;
                T_RING[v * 3] = cx + ox * radius * round[i];
                T_RING[v * 3 + 1] = cy + oy * radius * round[i];
                T_RING[v * 3 + 2] = cz + oz * radius * round[i];
            }
        }
        int light = FireMesh.argb(0.95 * alpha, TONGUE);
        int dark = FireMesh.argb(0.95 * alpha, FireMesh.mix(TONGUE, 0x9A3A5A, 0.45));
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < TONGUE_SIDES; k++) {
                int a = i * (TONGUE_SIDES + 1) + k;
                int c1 = k < TONGUE_SIDES / 2 ? light : dark;
                FireMesh.quad(sink, T_RING[a * 3], T_RING[a * 3 + 1], T_RING[a * 3 + 2], c1, T_RING[(a + 1) * 3], T_RING[(a + 1) * 3 + 1],
                    T_RING[(a + 1) * 3 + 2], c1, T_RING[(a + TONGUE_SIDES + 2) * 3], T_RING[(a + TONGUE_SIDES + 2) * 3 + 1],
                    T_RING[(a + TONGUE_SIDES + 2) * 3 + 2], c1, T_RING[(a + TONGUE_SIDES + 1) * 3], T_RING[(a + TONGUE_SIDES + 1) * 3 + 1],
                    T_RING[(a + TONGUE_SIDES + 1) * 3 + 2], c1);
            }
        }
    }

    /**
     * A ghost bursting at (x, y, z) ({@code size}: a Mini Hollow's pop about 1, the Toku Hollow's blast about 6), {@code age} ticks in:
     * a white flash, a shock shell bright at its rim, two rings rolling out flat, banded ghost smoke (deep violet to white) billowing up and
     * out, and a few wisps flying off.
     */
    public static void burst(FireMesh.Sink sink, double x, double y, double z, double size, double age, int seed) {
        double life = size > 2.5 ? BOOM_LIFE : POP_LIFE;
        double t = age / life;
        if (t >= 1.0 || t < 0.0) {
            return;
        }
        double grow = 1.0 - (1.0 - t) * (1.0 - t);
        if (t < 0.25) {
            FireMesh.shell(sink, x, y, z, size * (0.25 + 0.5 * t / 0.25), RIM, 0.75 * (1.0 - t / 0.25), 0.0);
        }
        FireMesh.shell(sink, x, y, z, size * (0.35 + 0.75 * grow), BODY, 0.04 * (1.0 - t), 0.5 * (1.0 - t));
        FireMesh.ring(sink, x, y, z, 0, 1, 0, size * (0.3 + 1.1 * grow), size * 0.18, size * 0.05, BODY, 0.6 * (1.0 - t));
        FireMesh.ring(sink, x, y - size * 0.1, z, 0, 1, 0, size * (0.2 + 0.8 * grow), size * 0.1, 0.0, CORE, 0.5 * (1.0 - t));
        FireMesh.palette(SMOKE);
        int puffs = size > 2.5 ? 14 : 6;
        for (int i = 0; i < puffs; i++) {
            double a = FireMesh.rand(seed, i) * 2 * Math.PI;
            double up = FireMesh.rand(seed, i + 30) * 0.8 - 0.1;
            double out = size * (0.25 + 0.75 * grow) * (0.6 + 0.4 * FireMesh.rand(seed, i + 60));
            double px = x + Math.cos(a) * out;
            double py = y + up * out * 0.8 + size * 0.25 * t;
            double pz = z + Math.sin(a) * out;
            double heat = 0.8 - 0.65 * t;
            FireMesh.puff(sink, px, py, pz, size * (0.22 + 0.18 * grow), Math.cos(a), up, Math.sin(a), 0.3, 0.4, heat, 0.3, false,
                0.85 * (1.0 - t * t), age, seed + i);
        }
        FireMesh.palette(FireMesh.BAND);
        // wisps flying off: little ghost tails streaking out
        for (int i = 0; i < (size > 2.5 ? 8 : 4); i++) {
            double a = FireMesh.rand(seed, i + 90) * 2 * Math.PI;
            double out = size * (0.4 + 1.2 * grow);
            FireMesh.streak(sink, x + Math.cos(a) * out, y + size * 0.3 * grow, z + Math.sin(a) * out, Math.cos(a), 0.3, Math.sin(a), size * 0.35,
                size * 0.05, BODY, 0.7 * (1.0 - t));
        }
    }

    /**
     * Despair over a body (the top of its head at x, y, z, {@code width} across): a small dark cloud hanging over it, the anime's gloom
     * lines streaming down round it (wavering), a dim blue shadow round the body; {@code age} ticks in, {@code left} 0 to 1 of it left.
     */
    public static void gloom(FireMesh.Sink sink, double x, double y, double z, double width, double height, double age, double left) {
        double a = Math.min(1.0, age / 6.0) * Math.min(1.0, left * 6.0);
        if (a < 0.01) {
            return;
        }
        double w = Math.max(0.5, width);
        // a dark blue-grey raincloud of gloom over the head
        FireMesh.palette(RAINCLOUD);
        for (int i = 0; i < 4; i++) {
            double ox = (i - 1.5) * w * 0.26;
            double bob = 0.05 * Math.sin(age * 0.15 + i);
            FireMesh.puff(sink, x + ox, y + 0.6 + bob + 0.06 * (i % 2), z + 0.08 * Math.sin(i * 2.1), w * 0.3, 0, 0, 0, 0.0, 0.25, 0.22, 0.2, false,
                0.9 * a, age, 7 + i);
        }
        FireMesh.palette(FireMesh.BAND);
        // the anime's gloom lines: long wavering blue strokes hanging round the head and shoulders, drifting down
        for (int i = 0; i < 9; i++) {
            double ang = i * 2 * Math.PI / 9 + 0.4;
            double rr = w * (0.5 + 0.12 * (i % 3));
            double rx = Math.cos(ang) * rr;
            double rz = Math.sin(ang) * rr;
            double drift = (age * 0.03 + i * 0.29) % 1.0;
            double length = height * (0.4 + 0.18 * Math.sin(i * 1.7));
            double top = y + 0.45 - drift * 0.2;
            double fade = Math.sin(Math.PI * drift);
            FireMesh.ray(sink, x + rx, top, z + rz, 0.05 * Math.sin(age * 0.2 + i), -1, 0.05 * Math.cos(age * 0.17 + i), length, 0.05, 0, 0,
                GLOOM, 0.75 * a * (0.4 + 0.6 * fade));
        }
        FireMesh.shell(sink, x, y - height * 0.5, z, height * 0.75, GLOOM, 0.12 * a, 0.0);
    }

    /** Ghost Network's link: a faint line of white light from (ax..) to (bx..), shimmering along. */
    public static void link(FireMesh.Sink sink, double ax, double ay, double az, double bx, double by, double bz, double age, double alpha) {
        double dx = bx - ax;
        double dy = by - ay;
        double dz = bz - az;
        double l = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (l < 0.1 || alpha < 0.01) {
            return;
        }
        FireMesh.ray(sink, ax, ay, az, dx / l, dy / l, dz / l, l, 0.05, 5, (age * 0.05) % 1.0, BODY, 0.35 * alpha);
    }
}
