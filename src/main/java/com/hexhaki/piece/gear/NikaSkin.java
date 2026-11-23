package com.hexhaki.piece.gear;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.horo.AstralArt;

/**
 * 3.4.0: Dawn Pistol's fist through the body (the owner, on 3.3.9's pull: "it looks cool but please get a gif of Gear 5 luffy vs luccy the
 * final blow, its not that... the fist goes through the body, stretching the skin to insane amounts and then causes a catastrophic
 * knockback that destroys all blocks in it's way"; their frames from Egghead: Luffy's arm driven through Lucci, Lucci's own spotted skin
 * stretched out along the arm to the fist, red-hot then white-hot at its end). Pure, previewable (FxPreview {@code |nskin:t}).
 * <ul>
 *   <li>{@link #out}: how far the skin is stretched out of the body, {@code t} ticks after the hit: shot out past its length in
 *       {@link #GROW} ticks, held quivering while the server pins the body ({@link #PIN}), then snapping back into it in {@link #SNAP}
 *       as the body is launched.</li>
 *   <li>{@link #skin}: the body's own skin (its texture's region, stretched along: the client's texture) pulled out of the far side of
 *       the body along the blow into a long funnel, thinning to a neck and swelling round the fist at its end.</li>
 *   <li>{@link #heat}: in flat bands over it, see-through: nothing at the body, red, orange, then yellow-white at the fist, the fist a
 *       white-hot ball in an orange glow (alpha-blended, never additive).</li>
 * </ul>
 * Everything about a base (the body's middle) along a unit way, eye-centred.
 */
public final class NikaSkin {
    /** Ticks to stretch out, the ticks the body is pinned (the server launches it then), ticks to snap back. */
    public static final double GROW = 2.0;
    public static final int PIN = 5;
    public static final double SNAP = 2.5;
    /** How far past the body's middle the fist stretches the skin at full stretch (blocks). */
    public static final double LENGTH = 7.0;
    /** The funnel's neck and the swelling round the fist (blocks). */
    static final double NECK = 0.27;
    public static final double BULB = 0.42;
    static final int SIDES = 12;
    static final int RINGS = 16;
    /** Heat bands along the skin (from the body to the fist): where each starts, its colour, how opaque. */
    static final double[] HEAT_FROM = {0.38, 0.62, 0.82, 0.93};
    static final int[] HEAT_RGB = {0xC81E1E, 0xF0501E, 0xFFA02A, 0xFFF0C0};
    static final double[] HEAT_ALPHA = {0.35, 0.55, 0.75, 0.9};

    private NikaSkin() {
    }

    /** The whole of it (ticks). */
    public static double life() {
        return PIN + SNAP;
    }

    /** How far the skin is stretched out (0 to about 1.15) {@code t} ticks after the hit. */
    public static double out(double t) {
        if (t < 0 || t >= life()) {
            return 0;
        }
        if (t < GROW) {
            double u = t / GROW;
            // out-back: past full and back
            return 1.0 + 2.4 * Math.pow(u - 1, 3) + 1.3 * Math.pow(u - 1, 2);
        }
        if (t < PIN) {
            double tau = t - GROW;
            return 1.0 + 0.05 * Math.sin(tau * 3.1) * Math.exp(-tau / 3.0);
        }
        // snapped back into the body as it is launched
        double u = (t - PIN) / SNAP;
        return Math.max(0, (1 - u) * (1 - u)) * Math.cos(u * 1.2);
    }

    static final double[] B = new double[6];
    static final double[] XYZ = new double[12];
    static final float[] UV = new float[8];
    static final int[] II = new int[4];
    static final int[] JJ = new int[4];
    static final double[] RAD = new double[RINGS + 1];
    static final double[] ALONG = new double[RINGS + 1];

    /** The funnel's radius at {@code s} (0 the body's middle, 1 the tip) for length {@code len}, base radius {@code rb}, fist {@code rt}. */
    static double radius(double s, double len, double rb, double rt) {
        // a cone, body-wide where it leaves the body, narrowing evenly to the fist (the owner's frames: a funnel, not a stick)
        double funnel = NECK + (rb - NECK) * Math.pow(Math.max(0, 1 - s), 1.15);
        double d = len - s * len; // blocks back from the tip
        double c = rt; // the bulb's middle, rt back from the tip
        double bulb = Math.abs(d - c) < rt ? Math.sqrt(rt * rt - (d - c) * (d - c)) : 0;
        return d < rt ? bulb : Math.max(funnel, bulb);
    }

    /** Lays out the rings (where along, how wide) for length {@code len}: more of them round the fist. */
    static void rings(double len, double rb, double rt) {
        for (int i = 0; i <= RINGS; i++) {
            double f = i / (double) RINGS;
            // denser at the tip: the bulb's roundness
            double s = 1 - Math.pow(1 - f, 1.6);
            ALONG[i] = s;
            RAD[i] = radius(s, len, rb, rt);
        }
        RAD[RINGS] = 0;
    }

    /**
     * The body's skin stretched out of it: from the base (bx, by, bz) along (wx, wy, wz) (unit), {@code len} long, {@code rb} round at the
     * body, swelling to {@code rt} round the fist; the texture's region u0..u1 round it and v0..v1 along it (stretched), {@code argb} its
     * tint; quivering with {@code time}. {@code len} under 0.05: nothing.
     */
    public static void skin(AstralArt.Skin sink, double bx, double by, double bz, double wx, double wy, double wz, double len, double rb,
                            double rt, float u0, float u1, float v0, float v1, int argb, double time) {
        if (len < 0.05) {
            return;
        }
        rings(len, rb, Math.min(rt, len * 0.5));
        FireMesh.basis(wx, wy, wz, B);
        double[] xyz = XYZ;
        float[] uv = UV;
        for (int i = 0; i < RINGS; i++) {
            for (int j = 0; j < SIDES; j++) {
                int[] ii = II;
                int[] jj = JJ;
                ii[0] = i;
                ii[1] = i;
                ii[2] = i + 1;
                ii[3] = i + 1;
                jj[0] = j;
                jj[1] = j + 1;
                jj[2] = j + 1;
                jj[3] = j;
                double nx = 0;
                double ny = 0;
                double nz = 0;
                for (int k = 0; k < 4; k++) {
                    double a = 2 * Math.PI * jj[k] / SIDES;
                    double ca = Math.cos(a);
                    double sa = Math.sin(a);
                    double ox = B[0] * ca + B[3] * sa;
                    double oy = B[1] * ca + B[4] * sa;
                    double oz = B[2] * ca + B[5] * sa;
                    double r = RAD[ii[k]] * (1 + 0.04 * Math.sin(time * 2.3 + ii[k] * 0.9));
                    double s = ALONG[ii[k]] * len;
                    xyz[k * 3] = bx + wx * s + ox * r;
                    xyz[k * 3 + 1] = by + wy * s + oy * r;
                    xyz[k * 3 + 2] = bz + wz * s + oz * r;
                    uv[k * 2] = u0 + (u1 - u0) * (jj[k] / (float) SIDES);
                    uv[k * 2 + 1] = v0 + (v1 - v0) * (float) ALONG[ii[k]];
                    nx += ox;
                    ny += oy;
                    nz += oz;
                }
                double nl = Math.max(1.0E-6, Math.sqrt(nx * nx + ny * ny + nz * nz));
                sink.quad(xyz, uv, argb, nx / nl, ny / nl, nz / nl);
            }
        }
    }

    /**
     * The heat over the stretched skin (a hair outside it), in flat bands from nothing at the body to white-hot at the fist, and the fist
     * itself a white-hot ball in an orange glow; {@code glow} 0 to 1 (it cools as the skin snaps back).
     */
    public static void heat(FireMesh.Sink sink, double bx, double by, double bz, double wx, double wy, double wz, double len, double rb, double rt,
                            double glow, double time) {
        if (len < 0.05 || glow < 0.01) {
            return;
        }
        double tip = Math.min(rt, len * 0.5);
        rings(len, rb, tip);
        FireMesh.basis(wx, wy, wz, B);
        for (int i = 0; i < RINGS; i++) {
            double s = (ALONG[i] + ALONG[i + 1]) * 0.5;
            int band = -1;
            for (int k = 0; k < HEAT_FROM.length; k++) {
                if (s >= HEAT_FROM[k]) {
                    band = k;
                }
            }
            if (band < 0) {
                continue;
            }
            int col = FireMesh.argb(HEAT_ALPHA[band] * glow, HEAT_RGB[band]);
            for (int j = 0; j < SIDES; j++) {
                double a0 = 2 * Math.PI * j / SIDES;
                double a1 = 2 * Math.PI * (j + 1) / SIDES;
                double r0 = RAD[i] * 1.08 + 0.01;
                double r1 = RAD[i + 1] * 1.08 + 0.01;
                double s0 = ALONG[i] * len;
                double s1 = ALONG[i + 1] * len;
                FireMesh.quad(sink, px(bx, wx, s0, a0, r0), py(by, wy, s0, a0, r0), pz(bz, wz, s0, a0, r0), col,
                    px(bx, wx, s0, a1, r0), py(by, wy, s0, a1, r0), pz(bz, wz, s0, a1, r0), col,
                    px(bx, wx, s1, a1, r1), py(by, wy, s1, a1, r1), pz(bz, wz, s1, a1, r1), col,
                    px(bx, wx, s1, a0, r1), py(by, wy, s1, a0, r1), pz(bz, wz, s1, a0, r1), col);
            }
        }
        // the fist: a white-hot ball in an orange glow, pulsing
        double cx = bx + wx * (len - tip);
        double cy = by + wy * (len - tip);
        double cz = bz + wz * (len - tip);
        double pulse = 1 + 0.08 * Math.sin(time * 2.7);
        FireMesh.shell(sink, cx, cy, cz, tip * 1.2 * pulse, 0xFFF6D8, 0.95 * glow, 0.55 * glow);
        FireMesh.shell(sink, cx, cy, cz, tip * 2.3 * pulse, 0xFF8A2A, 0.45 * glow, 0.0);
    }

    static double px(double b, double w, double s, double a, double r) {
        return b + w * s + (B[0] * Math.cos(a) + B[3] * Math.sin(a)) * r;
    }

    static double py(double b, double w, double s, double a, double r) {
        return b + w * s + (B[1] * Math.cos(a) + B[4] * Math.sin(a)) * r;
    }

    static double pz(double b, double w, double s, double a, double r) {
        return b + w * s + (B[2] * Math.cos(a) + B[5] * Math.sin(a)) * r;
    }
}
