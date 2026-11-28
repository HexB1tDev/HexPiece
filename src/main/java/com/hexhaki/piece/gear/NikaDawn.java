package com.hexhaki.piece.gear;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.4.4 (the owner's six phases, docs/handoff 8zzf verbatim: "Phase 1 — Winding up the body ... Phase 2 — Extending the attacking leg ...
 * Phase 3 — Explosive rotational release ... Phase 4 — The whip-like impact ... Phase 5 — Uncontrolled spinning after impact ... The
 * spinning eventually carries him downward, where he drills into the ground ... Phase 6 — The exaggerated Gear 5 recovery"): Dawn Whip's
 * timeline (server/NikaWhip runs it; gear5_dawn_whip and gear5_dawn_pop, tools/nika_anims.py, are keyed to the same ticks) and its look
 * (pure; client/gear/NikaWhipClient and tools/preview/NikaShots draw the very same quads):
 * <ul>
 *   <li>{@link #coil}: the wind-up's tension: white spiral lines wound round the body, tightening and turning the wrong way;</li>
 *   <li>{@link #smear}: the release: a cartoon smear of the stretched leg sweeping round, bands of white arcs out to its reach, the
 *       Haki-dark tip leading;</li>
 *   <li>{@link #crack}: the whip's crack on a body struck: a sharp burst, shock rings and streaks along the way it is thrown;</li>
 *   <li>{@link #whirl}: the overspin: arcs whipping round the body like a top's, dust spun off the feet;</li>
 *   <li>{@link #drill}: drilling into the ground: earth and stones sprayed up out of the hole in a spiral;</li>
 *   <li>{@link #pop}: popping back out: a burst of earth and a dust ring round the hole.</li>
 * </ul>
 * Alpha-blended, never additive (the owner's phone); no allocation per frame.
 */
public final class NikaDawn {
    private NikaDawn() {
    }

    /** The wind-up ends. */
    public static final int WIND = 12;
    /** The leg sweeps across the look (gear5_dawn_whip's yaw 360) and all round: the hit. */
    public static final int IMPACT = 16;
    /** The overspin ends and the drill begins; the drill is under the ground; popped back out. */
    public static final int OVERSPIN = 44;
    public static final int DRILL = 56;
    public static final int POP = 66;
    /** The pop's clip (gear5_dawn_pop) and how long the move waits for the landing after it at most. */
    public static final int POP_CLIP = 16;
    public static final int LANDING = 40;
    /** The shaft drilled under the feet (blocks deep), torn a layer every {@link #DRILL_STEP} ticks from {@link #OVERSPIN}. */
    public static final int DEPTH = 3;
    public static final int DRILL_STEP = 3;
    /** The leg's reach (blocks: the original's whip reach with Dawn, 8.5 x 1.2) and the height either side of the hips it sweeps. */
    public static final double REACH = 10.2;
    public static final double SWEEP_HEIGHT = 2.4;

    /** Earth's bands (FireMesh.puff's palette while drawn): dark loam to pale dust. */
    public static final int[] EARTH = {0x2E2419, 0x433526, 0x5A4733, 0x725B42, 0x8C7457, 0xA88F70, 0xC4AE91, 0xDDCFB8};
    /** The Haki's dark violet on the leg's leading edge, and the smear's white. */
    public static final int HAKI = 0x2A1838;
    public static final int WHITE = 0xFFFFFF;

    /**
     * gear5_dawn_whip's body yaw (degrees: positive swings the forward leg to the user's left) at its keys from the end of the wind-up
     * through the leg's retraction (tools/nika_anims.py writes the same; HudTests checks them against the clip): linear between them.
     */
    public static final double[] YAW_TICKS = {10, 11, 12, 13, 14, 15, 16, 18, 20, 22, 24, 26, 28};
    public static final double[] YAW = {-265, -276, -284, -160, -10, 170, 360, 502, 644, 786, 928, 1070, 1212};

    /** The clip's body yaw at {@code t} ticks into the move (from tick 10 on; held at its ends). */
    public static double yaw(double t) {
        if (t <= YAW_TICKS[0]) {
            return YAW[0];
        }
        for (int k = 0; k + 1 < YAW_TICKS.length; k++) {
            if (t <= YAW_TICKS[k + 1]) {
                double u = (t - YAW_TICKS[k]) / (YAW_TICKS[k + 1] - YAW_TICKS[k]);
                return YAW[k] + (YAW[k + 1] - YAW[k]) * u;
            }
        }
        return YAW[YAW.length - 1];
    }

    /** The rubber leg: shot out from {@link #LEG_OUT}, at its full reach from {@link #IMPACT} to {@link #LEG_HOLD}, back in by {@link #LEG_BACK}. */
    public static final double LEG_OUT = 11;
    public static final double LEG_HOLD = 18;
    public static final double LEG_BACK = 25;
    /** The leg's own length (blocks: the model's 12 px at 0.9375) and how much thicker the rubber leg is drawn (to read at 10 blocks). */
    static final double LEG_REST = 12.0 / 16.0 * 0.9375;
    static final double LEG_THICK = 1.3;
    /** The Haki coat over the striking leg (as Dawn Pistol's fist: the skin's texel times this). */
    public static final int COAT = 0x3A3248;

    /** How far the leg reaches {@code t} ticks into the move (0: not out). */
    public static double legLength(double t) {
        if (t < LEG_OUT || t > LEG_BACK) {
            return 0.0;
        }
        double e;
        if (t < IMPACT) {
            double u = (t - LEG_OUT) / (IMPACT - LEG_OUT);
            e = 1.0 - (1.0 - u) * (1.0 - u) * (1.0 - u);
        } else if (t <= LEG_HOLD) {
            e = 1.0;
        } else {
            double u = (t - LEG_HOLD) / (LEG_BACK - LEG_HOLD);
            e = 1.0 - u * u;
        }
        return LEG_REST + (REACH - LEG_REST) * e;
    }

    static final int LEG_SEGMENTS = 12;
    static final double[] LEG = new double[(LEG_SEGMENTS + 1) * 3];
    static final double[] F = new double[3];
    static final double[] O = new double[3];
    static final double[] Q = new double[12];
    static final float[] QUV = new float[8];

    /**
     * 3.4.6 (the owner: "I don't see the Dawn Whip. Like, I don't see the leg in your render ... I needed to see an accurate render"):
     * the striking leg, drawn here (client/gear/NikaWhipClient and the preview draw these very quads; the original's own stretched leg is
     * no longer sent): the user's right leg in their own skin, coated by Haki ({@code tint}), stretched from the hip out to its reach as a
     * rubber whip, turning with the body as gear5_dawn_whip turns it ({@link #yaw}), its outer part trailing behind the turn (bent back
     * like a whip: more the faster it turns), sagging a little at the end. {@code place}: the user's feet and facing (StyleShow.Place:
     * x to the left, z ahead); {@code t} ticks into the move. The leg's front faces up (the leg is thrust out level, as the clip's own
     * thigh), its foot at the end. Only the faces turned to the eye; about 50 quads.
     */
    public static void leg(com.hexhaki.piece.horo.AstralArt.Skin skin, com.hexhaki.piece.style.StyleShow.Place place, double t, boolean slim,
                           int tint) {
        if (!line(place, t)) {
            return;
        }
        double half = 2.0 / 16.0 * 0.9375 * LEG_THICK;
        int argb = 0xFF000000 | tint;
        for (int k = 0; k < LEG_SEGMENTS; k++) {
            double v0 = 20.0 + 12.0 * k / LEG_SEGMENTS;
            double v1 = 20.0 + 12.0 * (k + 1) / LEG_SEGMENTS;
            segment(skin, k, half, half, v0, v1, argb, k == LEG_SEGMENTS - 1);
        }
    }

    /** The leg's middle line {@code t} ticks in ({@link #LEG}, eye-relative, hip to foot); false when it is not out. */
    static boolean line(com.hexhaki.piece.style.StyleShow.Place place, double t) {
        double length = legLength(t);
        if (length <= 0.0) {
            return false;
        }
        double y = Math.toRadians(yaw(t));
        double speed = Math.abs(yaw(t + 0.5) - yaw(t - 0.5));
        double lag = Math.min(0.9, speed * 0.0062);
        double sag = 0.35 * (length / REACH);
        // the hip (the model's right hip, 1.9 px to the right, 12 px up), turned with the body
        double hx = -1.9 / 16.0 * 0.9375;
        double hipX = hx * Math.cos(y);
        double hipZ = -hx * Math.sin(y);
        double hipY = 12.0 / 16.0 * 0.9375;
        for (int k = 0; k <= LEG_SEGMENTS; k++) {
            double s = k / (double) LEG_SEGMENTS;
            double a = y - lag * Math.pow(s, 1.6);
            double d = length * s;
            F[0] = hipX + Math.sin(a) * d;
            F[1] = hipY - sag * s * s;
            F[2] = hipZ + Math.cos(a) * d;
            place.put(F, O);
            LEG[k * 3] = O[0];
            LEG[k * 3 + 1] = O[1];
            LEG[k * 3 + 2] = O[2];
        }
        return true;
    }

    /** The Haki's glow round the striking leg (the house's Haki look: a dark coat outlined in light), violet. */
    public static final int LEG_GLOW = 0xB08CFF;
    static final int GLOW_SIDES = 8;
    static final double[] RING_A = new double[GLOW_SIDES * 3];
    static final double[] RING_B = new double[GLOW_SIDES * 3];
    static final double[] NA = new double[GLOW_SIDES * 3];
    static final double[] NB = new double[GLOW_SIDES * 3];

    /**
     * The glow outlining the striking leg (into the light's batch, after its skin): a shell a little wider than the leg, clear where it
     * faces the eye and bright along its outline, so the coated leg reads as a Haki-hardened limb and not a black rope. About 100 quads.
     */
    public static void legGlow(FireMesh.Sink sink, com.hexhaki.piece.style.StyleShow.Place place, double t, int rgb) {
        if (!line(place, t)) {
            return;
        }
        double r = 2.0 / 16.0 * 0.9375 * LEG_THICK * 1.9;
        for (int k = 0; k <= LEG_SEGMENTS; k++) {
            int a = Math.max(0, k - 1);
            int b = Math.min(LEG_SEGMENTS, k + 1);
            double tx = LEG[b * 3] - LEG[a * 3];
            double ty = LEG[b * 3 + 1] - LEG[a * 3 + 1];
            double tz = LEG[b * 3 + 2] - LEG[a * 3 + 2];
            double tl = Math.max(1.0E-9, Math.sqrt(tx * tx + ty * ty + tz * tz));
            FireMesh.basis(tx / tl, ty / tl, tz / tl, BASIS);
            double[] ring = (k & 1) == 0 ? RING_A : RING_B;
            double[] normals = (k & 1) == 0 ? NA : NB;
            // thinner toward the foot, a rounded tip
            double rr = r * (k == LEG_SEGMENTS ? 0.6 : 1.0 - 0.25 * k / LEG_SEGMENTS);
            for (int i = 0; i < GLOW_SIDES; i++) {
                double ang = i * Math.PI * 2.0 / GLOW_SIDES;
                double ox = BASIS[0] * Math.cos(ang) + BASIS[3] * Math.sin(ang);
                double oy = BASIS[1] * Math.cos(ang) + BASIS[4] * Math.sin(ang);
                double oz = BASIS[2] * Math.cos(ang) + BASIS[5] * Math.sin(ang);
                ring[i * 3] = LEG[k * 3] + ox * rr;
                ring[i * 3 + 1] = LEG[k * 3 + 1] + oy * rr;
                ring[i * 3 + 2] = LEG[k * 3 + 2] + oz * rr;
                normals[i * 3] = ox;
                normals[i * 3 + 1] = oy;
                normals[i * 3 + 2] = oz;
            }
            if (k == 0) {
                continue;
            }
            double[] prev = (k & 1) == 0 ? RING_B : RING_A;
            double[] prevN = (k & 1) == 0 ? NB : NA;
            for (int i = 0; i < GLOW_SIDES; i++) {
                int j = (i + 1) % GLOW_SIDES;
                FireMesh.quad(sink, prev[i * 3], prev[i * 3 + 1], prev[i * 3 + 2], glow(prev, prevN, i, rgb),
                    prev[j * 3], prev[j * 3 + 1], prev[j * 3 + 2], glow(prev, prevN, j, rgb),
                    ring[j * 3], ring[j * 3 + 1], ring[j * 3 + 2], glow(ring, normals, j, rgb),
                    ring[i * 3], ring[i * 3 + 1], ring[i * 3 + 2], glow(ring, normals, i, rgb));
            }
        }
    }

    static final double[] BASIS = new double[6];

    /** A shell point's colour: bright where the shell is seen edge-on (the outline), clear where it faces the eye. */
    static int glow(double[] ring, double[] normals, int i, int rgb) {
        double px = ring[i * 3];
        double py = ring[i * 3 + 1];
        double pz = ring[i * 3 + 2];
        double el = Math.max(1.0E-6, Math.sqrt(px * px + py * py + pz * pz));
        double facing = Math.abs((normals[i * 3] * px + normals[i * 3 + 1] * py + normals[i * 3 + 2] * pz) / el);
        double edge = 1.0 - facing;
        return FireMesh.argb(0.04 + 0.7 * edge * edge * edge, rgb);
    }

    static final double[] A = new double[24];

    /** One stretch of the leg (rings k and k + 1): its four sides in the leg's skin (rows v0 to v1), and the foot's sole at the end. */
    static void segment(com.hexhaki.piece.horo.AstralArt.Skin skin, int k, double half, double halfSide, double v0, double v1, int argb,
                        boolean last) {
        for (int end = 0; end < 2; end++) {
            int i = k + end;
            // the leg's way here (the tangent), its front up and its side (front x way keeps the skin unmirrored)
            int a = Math.max(0, i - 1);
            int b = Math.min(LEG_SEGMENTS, i + 1);
            double tx = LEG[b * 3] - LEG[a * 3];
            double ty = LEG[b * 3 + 1] - LEG[a * 3 + 1];
            double tz = LEG[b * 3 + 2] - LEG[a * 3 + 2];
            double tl = Math.max(1.0E-9, Math.sqrt(tx * tx + ty * ty + tz * tz));
            tx /= tl;
            ty /= tl;
            tz /= tl;
            // up, made square to the way
            double ux = -ty * tx;
            double uy = 1.0 - ty * ty;
            double uz = -ty * tz;
            double ul = Math.max(1.0E-9, Math.sqrt(ux * ux + uy * uy + uz * uz));
            ux /= ul;
            uy /= ul;
            uz /= ul;
            // the model's +x (the side): front (up) x way
            double wx = uy * tz - uz * ty;
            double wy = uz * tx - ux * tz;
            double wz = ux * ty - uy * tx;
            for (int c = 0; c < 4; c++) {
                double side = (c & 1) == 0 ? -halfSide : halfSide;
                double front = (c & 2) == 0 ? half : -half;
                int at = (end * 4 + c) * 3;
                A[at] = LEG[i * 3] + wx * side + ux * front;
                A[at + 1] = LEG[i * 3 + 1] + wy * side + uy * front;
                A[at + 2] = LEG[i * 3 + 2] + wz * side + uz * front;
            }
        }
        // corners: end (0 the hip's side, 1 the foot's) x {0 -x front, 1 +x front, 2 -x back, 3 +x back}
        // the front (skin u 4 to 8), +x side (8 to 12), back (12 to 16), -x side (0 to 4), as SkinBox lays the right leg out
        face(skin, argb, 0, 1, 5, 4, 4, 8, v0, v1);
        face(skin, argb, 1, 3, 7, 5, 8, 12, v0, v1);
        face(skin, argb, 3, 2, 6, 7, 12, 16, v0, v1);
        face(skin, argb, 2, 0, 4, 6, 0, 4, v0, v1);
        if (last) {
            // the sole (the leg's bottom: u 8 to 12, v 16 to 20)
            faceCap(skin, argb, 4, 5, 7, 6);
        }
    }

    /** A side: corners p0 p1 (the hip's end) then p2 p3 (the foot's end), skin u0 to u1 across, v0 to v1 along. */
    static void face(com.hexhaki.piece.horo.AstralArt.Skin skin, int argb, int p0, int p1, int p2, int p3, double u0, double u1, double v0,
                     double v1) {
        put(0, p0);
        put(1, p1);
        put(2, p2);
        put(3, p3);
        QUV[0] = (float) (u0 / 64.0);
        QUV[1] = (float) (v0 / 64.0);
        QUV[2] = (float) (u1 / 64.0);
        QUV[3] = (float) (v0 / 64.0);
        QUV[4] = (float) (u1 / 64.0);
        QUV[5] = (float) (v1 / 64.0);
        QUV[6] = (float) (u0 / 64.0);
        QUV[7] = (float) (v1 / 64.0);
        emit(skin, argb);
    }

    static void faceCap(com.hexhaki.piece.horo.AstralArt.Skin skin, int argb, int p0, int p1, int p2, int p3) {
        put(0, p0);
        put(1, p1);
        put(2, p3);
        put(3, p2);
        QUV[0] = 8.0F / 64.0F;
        QUV[1] = 16.0F / 64.0F;
        QUV[2] = 12.0F / 64.0F;
        QUV[3] = 16.0F / 64.0F;
        QUV[4] = 12.0F / 64.0F;
        QUV[5] = 20.0F / 64.0F;
        QUV[6] = 8.0F / 64.0F;
        QUV[7] = 20.0F / 64.0F;
        emit(skin, argb);
    }

    static void put(int k, int corner) {
        Q[k * 3] = A[corner * 3];
        Q[k * 3 + 1] = A[corner * 3 + 1];
        Q[k * 3 + 2] = A[corner * 3 + 2];
    }

    /** The quad in {@link #Q} if it is turned to the eye (the origin), with its normal out of the leg. */
    static void emit(com.hexhaki.piece.horo.AstralArt.Skin skin, int argb) {
        double ax = Q[3] - Q[0];
        double ay = Q[4] - Q[1];
        double az = Q[5] - Q[2];
        double bx = Q[9] - Q[0];
        double by = Q[10] - Q[1];
        double bz = Q[11] - Q[2];
        double nx = ay * bz - az * by;
        double ny = az * bx - ax * bz;
        double nz = ax * by - ay * bx;
        double nl = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (nl < 1.0E-12) {
            return;
        }
        double cx = (Q[0] + Q[6]) * 0.5;
        double cy = (Q[1] + Q[7]) * 0.5;
        double cz = (Q[2] + Q[8]) * 0.5;
        // out of the leg: away from the leg's middle line nearest this face (the ring's middle)
        double mx = (A[0] + A[3] + A[6] + A[9] + A[12] + A[15] + A[18] + A[21]) / 8.0;
        double my = (A[1] + A[4] + A[7] + A[10] + A[13] + A[16] + A[19] + A[22]) / 8.0;
        double mz = (A[2] + A[5] + A[8] + A[11] + A[14] + A[17] + A[20] + A[23]) / 8.0;
        double out = nx * (cx - mx) + ny * (cy - my) + nz * (cz - mz) < 0 ? -1.0 / nl : 1.0 / nl;
        if ((nx * cx + ny * cy + nz * cz) * out > 0.0) {
            return; // turned away from the eye
        }
        skin.quad(Q, QUV, argb, nx * out, ny * out, nz * out);
    }

    /** The share of the swing done {@code t} ticks after the wind-up's end (the release: gear5_dawn_whip's unwinding), 0 to 1. */
    public static double release(double t) {
        double u = Math.max(0.0, Math.min(1.0, t / (IMPACT - WIND)));
        return u * u * (3.0 - 2.0 * u);
    }

    /**
     * The wind-up's tension round the body (feet at x y z, eye-relative), {@code age} ticks in (0 to {@link #WIND}): three white spiral
     * lines wound round it at the knees, the waist and the chest, closing in and turning the wrong way as it tightens, trembling at the
     * end; dust drawn in round the feet. About 300 quads.
     */
    public static void coil(FireMesh.Sink sink, double x, double y, double z, double age, int seed) {
        if (age < 0.0 || age > WIND + 1.0) {
            return;
        }
        double t = Math.min(1.0, age / WIND);
        double fade = age > WIND ? 1.0 - (age - WIND) : Math.min(1.0, age / 2.0);
        double shake = age > WIND - 3.0 ? 0.03 * Math.sin(age * 9.0) : 0.0;
        for (int k = 0; k < 3; k++) {
            double h = 0.45 + 0.5 * k;
            double r = (1.0 - 0.42 * t) * (0.95 - 0.08 * k) + shake;
            double start = -t * 4.2 - k * 2.1;
            double lean = 0.32 + 0.2 * t;
            FireMesh.arc(sink, x, y + h, z, 0, 1, 0, r, 0.13 + 0.07 * t, start, -3.6, lean, WHITE, 0.95 * fade);
            FireMesh.arc(sink, x, y + h + 0.05, z, 0, 1, 0, r * 1.08, 0.05, start - 0.3, -3.0, lean, HAKI, 0.75 * fade);
        }
        // dust pulled in round the feet as the body twists
        FireMesh.palette(EARTH);
        try {
            for (int i = 0; i < 4; i++) {
                double a = -t * 3.0 + i * Math.PI * 0.5;
                double r = 1.3 - 0.6 * t;
                FireMesh.puff(sink, x + Math.cos(a) * r, y + 0.12, z + Math.sin(a) * r, 0.16 + 0.08 * t, Math.sin(a), 0, -Math.cos(a), 0.5,
                    0.2, 0.6, 0.3, false, 0.55 * fade, age * 2 + i, seed + i);
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /**
     * The whip's smear behind the leg ({@code place}: the user's feet and facing, eye-relative), {@code t} ticks into the move: a
     * translucent fan from the hips out to the leg's reach ({@link #legLength}), trailing the leg round as gear5_dawn_whip turns it
     * ({@link #yaw}: toward the user's left), a white and Haki-violet leading edge along the leg, speed lines flung off its tip; fading
     * out after the impact. About 100 quads.
     */
    public static void smear(FireMesh.Sink sink, com.hexhaki.piece.style.StyleShow.Place place, double t, int seed) {
        if (t < LEG_OUT || t > IMPACT + 8.0) {
            return;
        }
        double after = Math.max(0.0, t - IMPACT);
        double fade = after <= 0.0 ? Math.min(1.0, (t - LEG_OUT) / 2.0 + 0.3) : Math.max(0.0, 1.0 - after / 8.0);
        double out = Math.max(legLength(t), LEG_REST);
        if (t > LEG_BACK) {
            out = LEG_REST;
        }
        double y = Math.toRadians(yaw(t));
        F[0] = Math.sin(y);
        F[1] = 0.0;
        F[2] = Math.cos(y);
        place.turn(F, O);
        // the leg's angle round the hips in the world (the angle of (cos a, 0, sin a)); turning left, it falls
        double tip = Math.atan2(O[2], O[0]);
        double x = place.x();
        double yy = place.y() + 12.0 / 16.0 * 0.9375;
        double z = place.z();
        double sweep = Math.toRadians(150.0);
        fan(sink, x, yy, z, out, tip, sweep, 0.0, 0.62 * fade);
        // the leading edge along the leg: white, the Haki's dark violet; a bright rim at the reach
        for (int k = 0; k < 2; k++) {
            ray(sink, x, yy + 0.02 * k, z, tip - 0.03 * k, out * 0.15, out, k == 0 ? 0.2 : 0.09, k == 0 ? WHITE : HAKI, (k == 0 ? 0.9 : 0.85) * fade);
        }
        flat(sink, x, yy, z, out, 0.3, tip, sweep * 0.55, WHITE, 0.9 * fade);
        flat(sink, x, yy, z, out + 0.18, 0.12, tip, sweep * 0.35, HAKI, 0.8 * fade);
        // speed lines flung off the tip
        for (int k = 0; k < 5; k++) {
            double a = tip + 0.12 * k;
            double r = out * (0.7 + 0.3 * FireMesh.rand(seed, k));
            FireMesh.ray(sink, x + Math.cos(a) * r, yy + 0.25 * (FireMesh.rand(seed, k + 9) - 0.5), z + Math.sin(a) * r, Math.sin(a), 0,
                -Math.cos(a), 1.4, 0.05, 0, 0.0, WHITE, 0.7 * fade);
        }
    }

    /**
     * A level arc round x y z from the angle {@code from} through {@code sweep} (radians; the angle of (cos a, 0, sin a), the x-z
     * plane's): FireMesh.arc round the up axis measures its angles the other way round and a quarter turn off (its 0 is -z).
     */
    static void flat(FireMesh.Sink sink, double x, double y, double z, double radius, double width, double from, double sweep, int rgb,
                     double alpha) {
        FireMesh.arc(sink, x, y, z, 0, 1, 0, radius, width, -from - Math.PI * 0.5, -sweep, 0.0, rgb, alpha);
    }

    static final int FAN = 14;

    /**
     * A smear fan round x y z (level): from the angle {@code from} (the leading edge, brightest) back through {@code sweep}, from a
     * fifth of {@code reach} out to it, its outer edge {@code rise} higher (a shallow cone, never a flat line edge-on), fading behind.
     */
    static void fan(FireMesh.Sink sink, double x, double y, double z, double reach, double from, double sweep, double rise, double alpha) {
        double inner = reach * 0.2;
        for (int i = 0; i < FAN; i++) {
            double t0 = i / (double) FAN;
            double t1 = (i + 1) / (double) FAN;
            double a0 = from + sweep * t0;
            double a1 = from + sweep * t1;
            int c0 = FireMesh.argb(alpha * Math.pow(1.0 - t0, 1.6), WHITE);
            int c1 = FireMesh.argb(alpha * Math.pow(1.0 - t1, 1.6), WHITE);
            int i0 = FireMesh.argb(alpha * 0.25 * (1.0 - t0), WHITE);
            int i1 = FireMesh.argb(alpha * 0.25 * (1.0 - t1), WHITE);
            FireMesh.quad(sink, x + Math.cos(a0) * inner, y, z + Math.sin(a0) * inner, i0, x + Math.cos(a0) * reach, y + rise,
                z + Math.sin(a0) * reach, c0, x + Math.cos(a1) * reach, y + rise, z + Math.sin(a1) * reach, c1, x + Math.cos(a1) * inner, y,
                z + Math.sin(a1) * inner, i1);
        }
    }

    /** A flat level band at the angle {@code a} from {@code from} to {@code to} out of x y z, {@code width} wide, turned up to the sky. */
    static void ray(FireMesh.Sink sink, double x, double y, double z, double a, double from, double to, double width, int rgb, double alpha) {
        double cx = Math.cos(a);
        double cz = Math.sin(a);
        double sx = -cz * width * 0.5;
        double sz = cx * width * 0.5;
        int c = FireMesh.argb(alpha, rgb);
        int tail = FireMesh.argb(alpha * 0.2, rgb);
        FireMesh.quad(sink, x + cx * from - sx, y, z + cz * from - sz, tail, x + cx * to - sx, y, z + cz * to - sz, c, x + cx * to + sx, y,
            z + cz * to + sz, c, x + cx * from + sx, y, z + cz * from + sz, tail);
    }

    /** The crack's life (ticks). */
    public static final double CRACK_LIFE = 10.0;

    /**
     * The whip's crack on a body struck (its middle at x y z, eye-relative), {@code age} ticks ago, thrown along ({@code wx}, {@code wz}):
     * a sharp comic burst, two shock rings round the way it flies, streaks shooting off along it. About 500 quads.
     */
    public static void crack(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double age, double size, int seed) {
        if (age < 0.0 || age > CRACK_LIFE) {
            return;
        }
        NikaArt.pow(sink, x, y, z, 0, 0, 0, age * 1.2, Math.max(0.6, size * 0.7), seed);
        double wl = Math.sqrt(wx * wx + wz * wz);
        if (wl < 1.0E-6) {
            return;
        }
        double ux = wx / wl;
        double uz = wz / wl;
        for (int k = 0; k < 2; k++) {
            double t = age - k * 1.5;
            if (t < 0.0 || t > 7.0) {
                continue;
            }
            FireMesh.ring(sink, x + ux * t * 0.4, y, z + uz * t * 0.4, ux, 0, uz, 0.4 + 0.45 * t, 0.4 - 0.12 * k, 0.35, WHITE,
                (0.85 - 0.3 * k) * (1.0 - t / 7.0));
        }
        for (int k = 0; k < 6; k++) {
            double s = FireMesh.rand(seed, k + 20);
            double ox = (FireMesh.rand(seed, k + 30) - 0.5) * 0.9;
            double oy = (FireMesh.rand(seed, k + 40) - 0.5) * size;
            double along = age * (0.9 + 0.6 * s);
            FireMesh.streak(sink, x + ux * along - uz * ox, y + oy, z + uz * along + ux * ox, ux, 0, uz, 1.2 + s, 0.05, WHITE,
                0.8 * (1.0 - age / CRACK_LIFE));
        }
    }

    /**
     * The overspin (feet at x y z, eye-relative), {@code age} ticks after the impact: white arcs whipping round the body at three heights
     * like a top's, faster as it goes, dust spun off round the feet; {@code drill} 0 to 1 as it turns into the drill (the arcs drawn in
     * tight and low). About 900 quads.
     */
    public static void whirl(FireMesh.Sink sink, double x, double y, double z, double age, double drill, int seed) {
        double spin = age * 1.1 + age * age * 0.01;
        for (int k = 0; k < 3; k++) {
            double h = (0.3 + 0.62 * k) * (1.0 - 0.35 * drill);
            double r = (0.85 + 0.12 * Math.sin(age * 0.7 + k)) * (1.0 - 0.45 * drill);
            // turning on the release's way (to the user's left: FireMesh.arc's angle rising), each arc trailing behind its head
            FireMesh.arc(sink, x, y + h, z, 0, 1, 0, r, 0.17, spin * 1.6 + k * 2.0, -2.6, 0.1, WHITE, 0.9);
            FireMesh.arc(sink, x, y + h + 0.12, z, 0, 1, 0, r * 1.18, 0.07, spin * 1.6 + k * 2.0 - 0.5, -1.8, 0.05, WHITE, 0.55);
        }
        FireMesh.palette(EARTH);
        try {
            for (int i = 0; i < 5; i++) {
                double a = -spin + i * Math.PI * 0.4;
                double r = 0.7 + 0.5 * FireMesh.rand(seed, i + (int) (age / 3));
                FireMesh.puff(sink, x + Math.cos(a) * r, y + 0.1 + 0.15 * drill, z + Math.sin(a) * r, 0.18 + 0.1 * drill, Math.sin(a), 0.3,
                    -Math.cos(a), 0.7, 0.2, 0.62, 0.3, false, 0.6, age * 2 + i, seed + i * 13);
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /**
     * Drilling into the ground (the hole's top middle at x y z, eye-relative), {@code age} ticks into the drill: earth sprayed up out of
     * the hole in a spiral, stones flung out, a ring of dust round its mouth; {@code torn} whether the ground opened (else only the dust
     * of a body spinning on it). About 2,500 quads at its height.
     */
    public static void drill(FireMesh.Sink sink, double x, double y, double z, double age, boolean torn, int seed) {
        if (age < 0.0) {
            return;
        }
        double life = DRILL - OVERSPIN + 6.0;
        if (age > life) {
            return;
        }
        double fade = Math.min(1.0, (life - age) / 6.0);
        FireMesh.palette(EARTH);
        try {
            int n = torn ? 9 : 5;
            for (int i = 0; i < n; i++) {
                double born = (i * 1.3) % 6.0;
                double t = ((age + born) % 6.0) / 6.0;
                double a = age * 0.8 + i * 2.4;
                double r = 0.4 + 1.6 * t;
                double up = (torn ? 2.2 : 0.8) * Math.sin(Math.PI * Math.min(1.0, t * 1.2)) * (0.6 + 0.4 * FireMesh.rand(seed, i));
                FireMesh.puff(sink, x + Math.cos(a) * r, y + up, z + Math.sin(a) * r, 0.22 + 0.25 * t, Math.cos(a), 1.0 - t, Math.sin(a),
                    0.7, 0.25, 0.48 + 0.25 * (1.0 - t), 0.3, false, 0.85 * fade * (1.0 - t * t), age * 2 + i, seed + i * 7);
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
        if (torn) {
            for (int i = 0; i < 8; i++) {
                double t = ((age + i * 0.75) % 6.0) / 6.0;
                double a = i * 0.785 + age * 0.5;
                double r = 0.3 + 2.4 * t;
                double up = 2.6 * t - 3.2 * t * t + 0.2;
                FireMesh.shard(sink, x + Math.cos(a) * r, y + up, z + Math.sin(a) * r, 0.12 + 0.06 * FireMesh.rand(seed, i + 50), age * 0.6 + i,
                    seed + i, i % 2 == 0 ? 0x6E5A44 : 0x8A8279, fade);
            }
        }
        // 4.0.2 (the owner: "Gear 5 also has these rings showing up around the player when I don't ask for them"): no ring on the ground
    }

    /** The pop's life (ticks). */
    public static final double POP_LIFE = 12.0;

    /** Popping back out of the hole (its top middle at x y z, eye-relative), {@code age} ticks ago: earth bursting up and a dust ring. */
    public static void pop(FireMesh.Sink sink, double x, double y, double z, double age, int seed) {
        if (age < 0.0 || age > POP_LIFE) {
            return;
        }
        double t = age / POP_LIFE;
        double fade = 1.0 - t * t;
        FireMesh.palette(EARTH);
        try {
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4.0 + FireMesh.rand(seed, i) * 0.5;
                double r = 0.3 + 1.8 * (1.0 - (1.0 - t) * (1.0 - t));
                double up = 0.3 + 1.2 * Math.sin(Math.PI * Math.min(1.0, t * 1.5));
                FireMesh.puff(sink, x + Math.cos(a) * r, y + up, z + Math.sin(a) * r, 0.3 + 0.2 * t, Math.cos(a), 0.6, Math.sin(a), 0.6, 0.25,
                    0.55 + 0.2 * (1.0 - t), 0.3, false, 0.85 * fade, age * 2 + i, seed + i * 5);
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
        // 4.0.2: no dust ring on the ground (the owner: rings round the player they did not ask for)
    }

    /**
     * One's own first person through the release and the overspin, {@code age} ticks after the wind-up's end: speed streaks rushing
     * sideways across the view (the world whirling past the other way round), thinning out as the spin slows into the drill. The view's
     * frame: forward ({@code f}), up ({@code u}), right ({@code r}), unit, eye-relative.
     */
    public static void firstPerson(FireMesh.Sink sink, double age, int seed, double fx, double fy, double fz, double ux, double uy, double uz,
                                   double rx, double ry, double rz) {
        double life = DRILL - WIND;
        if (age < 0.0 || age > life) {
            return;
        }
        double strength = age < IMPACT - WIND ? release(age) : Math.max(0.2, 1.0 - (age - (IMPACT - WIND)) / life);
        for (int k = 0; k < 10; k++) {
            double s = FireMesh.rand(seed, k);
            double lane = (k / 9.0 - 0.5) * 1.3;
            double slide = ((age * 1.9 + s * 3.0) % 2.0) - 1.0;
            // the body turns to its left, so the world sweeps across the view from left to right
            double px = fx * 2.0 + ux * lane + rx * slide * 1.8;
            double py = fy * 2.0 + uy * lane + ry * slide * 1.8;
            double pz = fz * 2.0 + uz * lane + rz * slide * 1.8;
            FireMesh.streak(sink, px, py, pz, rx, ry, rz, 1.0 + 0.8 * s, 0.012, WHITE, 0.55 * strength);
        }
    }
}
