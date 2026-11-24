package com.hexhaki.piece.mochi;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.5.5: the Mochi Mochi no Mi's moves drawn (pure; MochiArt's pieces put together, eye-centred; client/mochi/MochiFx and
 * tools/preview/MochiShots draw the very same quads), after the owner's references: the stretched white arm ending in a big fist (black
 * with Armament), Mochi Tsuki's drill of mochi spiralling round the trident Mogura, Yanagi Mochi's tendrils stamping, Kaku Mochi's square
 * fists, Yaki Mochi's swelling forearm and its fist flying on a jet of fire, Zan Giri Mochi's wheel and spiked club, the Anemone's
 * floating rings, the sticky mochi, and the Awakening's mochi ground, wave, walls, rain, Muso Donuts and dome.
 */
public final class MochiDraw {
    private MochiDraw() {
    }

    // ------------------------------------------------------------------------------------------------------ timing (shared with the server)

    /** A punch: out in {@link #PUNCH_OUT} ticks, held, back by {@link #PUNCH_LIFE}. */
    public static final double PUNCH_OUT = 3.0;
    public static final double PUNCH_LIFE = 9.0;
    /** 3.5.5: a punch's arm, half its width (a bar), and its fist's half width (a little wider: the block at its end). */
    public static final double PUNCH_ARM = 0.21;
    public static final double PUNCH_FIST = 0.29;
    /** 3.6.3: the punch's arm at rest, an arm's own length (the arm itself is the mochi: client/mochi/MochiFx.mochiArms hides the real one). */
    public static final double PUNCH_REST = 0.7;
    /** A splat of mochi (an impact), a boom of fire. */
    public static final double SPLAT_LIFE = 12.0;
    public static final double BOOM_LIFE = 70.0;
    /** The Anemone's punch: out and back. */
    public static final double ANEMONE_PUNCH = 7.0;

    static double clamp(double t) {
        return Math.max(0.0, Math.min(1.0, t));
    }

    static double ease(double t) {
        t = clamp(t);
        return 1.0 - (1.0 - t) * (1.0 - t);
    }

    static double smooth(double t) {
        t = clamp(t);
        return t * t * (3.0 - 2.0 * t);
    }

    // ------------------------------------------------------------------------------------------------------ the arm and fist

    /**
     * A stretched mochi arm from the shoulder (sx, sy, sz) to the fist at (fx, fy, fz): 3.5.5 (the owner: "the arms are like squares.
     * They're like basically rectangles that with the fist at the very edge"): a straight bar of rounded square section {@code r} half
     * wide all along (one flat face up), ending in a block of a fist ({@code size} its half width, a little wider than the bar, punching
     * along it; {@code left} a left fist). It sags a little in the middle as mochi does ({@code sag} blocks).
     */
    public static void arm(FireMesh.Sink sink, double sx, double sy, double sz, double fx, double fy, double fz, double r, double size,
                           double sag, boolean coat, double time, int seed, boolean left) {
        double dx = fx - sx;
        double dy = fy - sy;
        double dz = fz - sz;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-4) {
            return;
        }
        double ux = dx / len;
        double uy = dy / len;
        double uz = dz / len;
        double[] at = ARM_AT;
        MochiArt.tubeBegin();
        int n = len > 1.2 ? 10 : 3;
        for (int k = 0; k <= n; k++) {
            armAt(sx, sy, sz, fx, fy, fz, r, size, sag, coat, time, seed, k / (double) n, at);
            MochiArt.tubePoint(at[0], at[1], at[2], at[3]);
        }
        // the owner: "The black fist is only with haki on, without haki on its fully white, and with haki on the entire fist and arm
        // (thing connecting to the fist) as well are black"
        MochiArt.bar(sink, coat ? MochiArt.HAKI : MochiArt.MOCHI, 1.0);
        // the fist, its knuckles across the bar (level across the way: the bar's own side)
        double kx = -uz;
        double kz = ux;
        double kl = Math.sqrt(kx * kx + kz * kz);
        if (kl < 1.0E-4) {
            kx = 1;
            kz = 0;
            kl = 1;
        }
        MochiArt.fist(sink, fx, fy, fz, ux, uy, uz, kx / kl, 0, kz / kl, size, coat, time, seed, left);
    }

    private static final double[] ARM_AT = new double[4];

    /**
     * 3.6.6: the point of {@link #arm}'s bar {@code t} of the way from the shoulder (0) to the back of the fist (1), as it is drawn (its
     * wobble, its sag, its thinning) into out (x, y, z, its half width): what rides on the bar (Power Mochi's seams of fire) sits on it.
     */
    public static double[] armAt(double sx, double sy, double sz, double fx, double fy, double fz, double r, double size, double sag, boolean coat,
                                 double time, int seed, double t, double[] out) {
        double dx = fx - sx;
        double dy = fy - sy;
        double dz = fz - sz;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-4) {
            out[0] = sx;
            out[1] = sy;
            out[2] = sz;
            out[3] = r;
            return out;
        }
        double ux = dx / len;
        double uy = dy / len;
        double uz = dz / len;
        // the bar runs into the back of the hand (the fist is its very end)
        double reach = Math.max(0.05, len - size * 0.5);
        // 3.6.5 (the owner: "feels like mochi is dead, it should be more stretchy, more animated, more squishy ... the arms should have
        // more elasticity"): drawn out, the bar thins in its middle the further it stretches (dough keeps its volume), a wave of wobble
        // runs along it side to side and up and down, and it sags a little under its own weight; hardened by Haki it wobbles less
        double stretch = clamp((len - 1.0) / 5.0);
        double thin = (coat ? 0.18 : 0.34) * stretch;
        double amp = Math.min(0.32, 0.04 + 0.035 * len) * (coat ? 0.4 : 1.0) * Math.min(1.0, len / 1.2);
        double hang = sag + 0.025 * len;
        // across the way, level, and the up square to both
        double ax = -uz;
        double az = ux;
        double al = Math.sqrt(ax * ax + az * az);
        if (al < 1.0E-4) {
            ax = 1;
            az = 0;
            al = 1;
        }
        ax /= al;
        az /= al;
        double vx = -az * uy;
        double vy = az * ux - ax * uz;
        double vz = ax * uy;
        double phase = seed * 1.7;
        double belly = Math.sin(Math.PI * t);
        // the wobble and the sag eased in and out at both ends: the bar leaves the shoulder and enters the fist square on its way
        double held = smooth((t - 0.1) / 0.2) * smooth((0.9 - t) / 0.2);
        double drop = hang * 4.0 * t * (1.0 - t) * held;
        double side = amp * belly * held * Math.sin(2 * Math.PI * 1.3 * t - time * 1.1 + phase);
        double rise = 0.6 * amp * belly * held * Math.cos(2 * Math.PI * 0.9 * t - time * 0.8 + phase * 0.6);
        out[0] = sx + ux * reach * t + ax * side + vx * rise;
        out[1] = sy + uy * reach * t + vy * rise - drop;
        out[2] = sz + uz * reach * t + az * side + vz * rise;
        out[3] = r * (1.0 - thin * belly);
        return out;
    }

    /** As {@link #arm} with a right fist. */
    public static void arm(FireMesh.Sink sink, double sx, double sy, double sz, double fx, double fy, double fz, double r, double size,
                           double sag, boolean coat, double time, int seed) {
        arm(sink, sx, sy, sz, fx, fy, fz, r, size, sag, coat, time, seed, false);
    }

    /**
     * The attack button's mochi punch (LMB): from the shoulder (x, y, z) along (wx, wy, wz) (unit), {@code reach} out at its furthest,
     * {@code age} ticks in: shot out in {@link #PUNCH_OUT}, a beat held, snapped back by {@link #PUNCH_LIFE}.
     */
    public static void punch(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double reach, double age,
                             boolean coat, int seed) {
        punch(sink, x, y, z, wx, wy, wz, reach, age, coat, seed, false);
    }

    /** As {@link #punch}, {@code left} the left arm's (its fist a left fist). */
    public static void punch(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double reach, double age,
                             boolean coat, int seed, boolean left) {
        if (age < 0 || age > PUNCH_LIFE) {
            return;
        }
        // 3.6.5 (the owner: "more stretchy ... the arms should have more elasticity"): shot out past its reach and springing back to
        // it, wobbling there, snapped back; the fist squashing out a little as it lands
        double out = age < PUNCH_OUT ? elastic(age / PUNCH_OUT) : age < PUNCH_OUT + 2 ? 1.0 + 0.07 * Math.sin((age - PUNCH_OUT) * 3.0)
            * Math.exp(-(age - PUNCH_OUT)) : 1.0 - smooth((age - PUNCH_OUT - 2) / (PUNCH_LIFE - PUNCH_OUT - 2));
        double d = PUNCH_REST + (reach - PUNCH_REST) * out;
        double land = age - PUNCH_OUT * 0.8;
        double squash = 1.0 + 0.22 * Math.exp(-land * land * 0.8);
        arm(sink, x, y, z, x + wx * d, y + wy * d, z + wz * d, PUNCH_ARM, PUNCH_FIST * squash, 0.0, coat, age, seed, left);
    }

    /**
     * Mogura, Katakuri's trident: its shaft from (bx, by, bz) to its head at (hx, hy, hz), the head's prongs spread along the side
     * (sx, sy, sz) (unit): a long dark iron shaft, a crossbar, three prongs, the middle one longest.
     */
    public static void trident(FireMesh.Sink sink, double bx, double by, double bz, double hx, double hy, double hz, double sx, double sy,
                               double sz) {
        double dx = hx - bx;
        double dy = hy - by;
        double dz = hz - bz;
        double len = Math.max(1.0E-4, Math.sqrt(dx * dx + dy * dy + dz * dz));
        double ux = dx / len;
        double uy = dy / len;
        double uz = dz / len;
        MochiArt.tubeBegin();
        MochiArt.tubePoint(bx, by, bz, 0.07);
        MochiArt.tubePoint(hx, hy, hz, 0.07);
        MochiArt.tube(sink, 6, MochiArt.IRON, 0.0, 0.0);
        // the crossbar and the prongs
        double w = 0.42;
        MochiArt.tubeBegin();
        MochiArt.tubePoint(hx - sx * w, hy - sy * w, hz - sz * w, 0.06);
        MochiArt.tubePoint(hx + sx * w, hy + sy * w, hz + sz * w, 0.06);
        MochiArt.tube(sink, 6, MochiArt.IRON, 0.0, 0.0);
        for (int k = -1; k <= 1; k++) {
            double ox = hx + sx * w * k;
            double oy = hy + sy * w * k;
            double oz = hz + sz * w * k;
            double l = k == 0 ? 0.95 : 0.7;
            MochiArt.cone(sink, ox, oy, oz, ox + ux * l, oy + uy * l, oz + uz * l, 0.08, MochiArt.IRON);
        }
    }

    // ------------------------------------------------------------------------------------------------------ splats and booms

    /**
     * A splat of mochi where something lands at (x, y, z): blobs of mochi flung out (they shrink away, never fade: they stay solid),
     * {@code size} its scale, {@code age} ticks in; facing (wx, wy, wz) (the way the blow went).
     */
    public static void splat(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double size, double age, int seed) {
        if (age < 0 || age > SPLAT_LIFE) {
            return;
        }
        double t = age / SPLAT_LIFE;
        double shrink = 1.0 - t * t;
        int n = 7;
        for (int k = 0; k < n; k++) {
            double a = 2 * Math.PI * (k + FireMesh.rand(seed, k)) / n;
            double up = 0.3 + 0.7 * FireMesh.rand(seed, k + 20);
            double ox = Math.cos(a) + wx * 0.6;
            double oy = up + wy * 0.6;
            double oz = Math.sin(a) + wz * 0.6;
            double ol = Math.max(1.0E-6, Math.sqrt(ox * ox + oy * oy + oz * oz));
            double d = size * (0.4 + 1.4 * ease(t * 1.6)) * (0.7 + 0.5 * FireMesh.rand(seed, k + 40));
            double fall = 1.8 * size * t * t;
            double r = size * 0.22 * (0.6 + 0.6 * FireMesh.rand(seed, k + 60)) * shrink;
            if (r > 0.02) {
                MochiArt.ball(sink, x + ox / ol * d, y + oy / ol * d - fall, z + oz / ol * d, r, ox, oy, oz, 1.0 + 0.8 * (1.0 - t), 0.1, age, seed + k,
                    MochiArt.MOCHI);
            }
        }
        // the middle: a soft puff of mochi squashing out
        double c = size * 0.45 * (1.0 - smooth(t * 1.5));
        if (c > 0.03) {
            MochiArt.blob(sink, x, y, z, 1, 0, 0, 0, 1, 0, c * 1.4, c * 0.8, c * 1.4, 0.9, 0.15, age, seed, MochiArt.MOCHI, 1.0);
        }
    }

    /**
     * Grilled Mochi's explosion where the burning fist lands (x, y, z), {@code size} its scale, {@code age} ticks in. 3.6.3, after the
     * reference's frames 36 to 110 (next-ai-3.6.3.md G): a white-hot flash, a ball of the house's banded fire filling a dome, and round
     * it CONCENTRIC RINGS turned to the eye (bright orange fire rings with dark scorched rings between them: from any side the dome reads
     * as the reference's tunnel of rings, the ground hiding its lower half), black smoke puffs thrown off its rim, white dough flung; the
     * rings fade into a pale grey dome of smoke that lingers while the fire in it dies (about three seconds in all).
     */
    public static void boom(FireMesh.Sink sink, double x, double y, double z, double size, double age, int seed) {
        if (age < 0 || age > BOOM_LIFE) {
            return;
        }
        double t = age / BOOM_LIFE;
        // toward the eye (every point here is eye-relative): the rings face the one looking
        double el = Math.max(1.0E-6, Math.sqrt(x * x + y * y + z * z));
        double nx = -x / el;
        double ny = -y / el;
        double nz = -z / el;
        if (age < 4.0) {
            double f = age / 4.0;
            MochiArt.glow(sink, x, y + size * 0.3, z, size * (1.0 + 0.5 * ease(f)), 0xFFF6DC, 0.95 * (1.0 - f));
        }
        // the fire filling the dome: hot and swelling, then dying down inside the smoke
        double burn = 1.0 - smooth((age - 22.0) / 30.0);
        if (burn > 0.02) {
            int n = 9;
            for (int k = 0; k < n; k++) {
                double a = 2 * Math.PI * (k + FireMesh.rand(seed, k)) / n;
                double up = 0.2 + 0.7 * FireMesh.rand(seed, k + 30);
                double d = size * (0.1 + 0.32 * ease(age / 6.0)) * (0.6 + 0.6 * FireMesh.rand(seed, k + 50));
                double r = size * (0.34 - 0.1 * t) * (0.7 + 0.5 * FireMesh.rand(seed, k + 70));
                double heat = Math.max(0.0, 1.0 - age / 30.0);
                FireMesh.puff(sink, x + Math.cos(a) * d, y + up * d * 0.8 + size * 0.1, z + Math.sin(a) * d, r, Math.cos(a), up, Math.sin(a), 0.4, 0.5,
                    heat, 0.6, true, 0.95 * burn, age, seed + k);
            }
        }
        // the rings: bright fire and dark scorch by turns, nested, spreading a little as they fade
        double rings = 1.0 - smooth((age - 16.0) / 24.0);
        if (rings > 0.02) {
            for (int k = 0; k < BOOM_RINGS; k++) {
                double r = size * (0.5 + 0.24 * k) * (0.55 + 0.45 * ease(age / 5.0)) * (1.0 + 0.12 * t);
                double w = size * (k % 2 == 0 ? 0.3 : 0.14);
                if (k % 2 == 0) {
                    // a fire ring: orange, its inner edge yellow-white hot
                    FireMesh.ring(sink, x, y, z, nx, ny, nz, r, w, 0.0, 0xFF6A12, 0.92 * rings);
                    FireMesh.ring(sink, x, y, z, nx, ny, nz, r - w * 0.28, w * 0.4, 0.0, 0xFFD25A, 0.95 * rings);
                    FireMesh.ring(sink, x, y, z, nx, ny, nz, r - w * 0.42, w * 0.14, 0.0, 0xFFF6D6, 0.9 * rings);
                } else {
                    // the scorched ring between
                    FireMesh.ring(sink, x, y, z, nx, ny, nz, r, w, 0.0, 0x2B1408, 0.85 * rings);
                }
            }
            // the outermost: a white-hot edge
            double ro = size * (0.5 + 0.24 * BOOM_RINGS) * (0.55 + 0.45 * ease(age / 5.0)) * (1.0 + 0.12 * t);
            FireMesh.ring(sink, x, y, z, nx, ny, nz, ro, size * 0.06, 0.0, 0xFFF4D8, 0.7 * rings);
        }
        // black smoke thrown off its rim, rising
        double smoke = 1.0 - smooth((age - 10.0) / 30.0);
        if (age > 2.0 && smoke > 0.02) {
            for (int k = 0; k < 4; k++) {
                double a = 2 * Math.PI * (k + 0.5 * FireMesh.rand(seed, k + 200)) / 4.0;
                double d = size * (1.1 + 0.35 * ease((age - 2.0) / 10.0));
                double up = 0.2 + 0.5 * FireMesh.rand(seed, k + 210);
                // the house's smoke (the Magma fruit's black-brown puffs: FireMesh.SMOKE, its dark end)
                FireMesh.palette(FireMesh.SMOKE);
                FireMesh.puff(sink, x + Math.cos(a) * d, y + up * d + 0.04 * age, z + Math.sin(a) * d, size * 0.12 * (1.0 + 0.03 * age), 0, 1, 0, 0.2,
                    0.12, 0.08 + 0.16 * FireMesh.rand(seed, k + 230), 0.0, false, 0.85 * smoke, age, seed + 220 + k);
                FireMesh.palette(FireMesh.BAND);
            }
        }
        // the dome of smoke left: pale grey, see-through, lingering
        double dome = smooth((age - 12.0) / 10.0) * (1.0 - smooth((age - BOOM_LIFE + 20.0) / 20.0));
        if (dome > 0.02) {
            double r = size * (1.25 + 0.15 * t);
            MochiArt.glow(sink, x, y, z, r, 0xC9C6C2, 0.28 * dome);
            FireMesh.ring(sink, x, y, z, nx, ny, nz, r * 0.92, r * 0.22, 0.0, 0xDCDAD6, 0.38 * dome);
            FireMesh.ring(sink, x, y, z, nx, ny, nz, r * 0.7, r * 0.12, 0.0, 0xBDB9B4, 0.22 * dome);
        }
        // embers
        if (age < 20.0) {
            for (int k = 0; k < 8; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 300);
                double up = FireMesh.rand(seed, k + 310) * 1.2 - 0.2;
                double ul = Math.sqrt(1.0 + up * up);
                double d = size * (0.4 + 1.4 * ease(age / 12.0));
                FireMesh.ray(sink, x + Math.cos(a) / ul * d, y + up / ul * d - 0.004 * age * age, z + Math.sin(a) / ul * d, Math.cos(a) / ul, up / ul,
                    Math.sin(a) / ul, 0.25, 0.05, 0, 0.0, 0xFFB347, 0.9 * (1.0 - age / 20.0));
            }
        }
        // white droplets of dough flung out of it
        splat(sink, x, y + size * 0.3, z, 0, 1, 0, Math.min(1.4, size * 0.45), age * 0.9, seed + 9);
    }

    /** 3.6.3: how many nested rings the explosion's dome shows (bright and dark by turns). */
    static final int BOOM_RINGS = 4;

    // ------------------------------------------------------------------------------------------------------ Yaki Mochi

    /**
     * Yaki Mochi (C) held: the forearm from the elbow (x, y, z) along (wx, wy, wz) swelling with pressure, {@code charge} 0 to 1: a ball of
     * mochi growing round the forearm, trembling as it fills, the fist at its end.
     */
    public static void swell(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double charge, double age,
                             boolean coat, int seed) {
        swell(sink, x, y, z, wx, wy, wz, charge, age, coat, seed, Double.NaN);
    }

    /**
     * 3.6.3 (the reference's Grilled Dough, frames 6 to 33: next-ai-3.6.3.md G): held, the pointed arm bursts into a huge white puff of
     * dough round it (bigger than the user, splattering: {@link #SWELL_BURST} ticks), which settles into a long thick arm of lumpy
     * dough (as thick as the body) out from the elbow (x, y, z) along (wx, wy, wz), a block of a fist at its end (black with Armament:
     * the dough itself stays white, as the reference's), drips hanging off its underside and dropping onto the ground ({@code ground}
     * high; NaN: none) where they splat; longer, thicker and trembling the fuller {@code charge} (0 to 1) is.
     */
    public static void swell(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double charge, double age,
                             boolean coat, int seed, double ground) {
        double shake = charge > 0.6 ? 0.03 * Math.sin(age * 3.1) * charge : 0.0;
        double[] b = B;
        MochiArt.axes(wx, wy, wz, b);
        // its side (across the way, level) and its up
        double sx = -wz;
        double sz = wx;
        double sl = Math.max(1.0E-6, Math.hypot(sx, sz));
        sx /= sl;
        sz /= sl;
        double ux = wy * sz;
        double uy = wz * sx - wx * sz;
        double uz = -wy * sx;
        double ul = Math.max(1.0E-6, Math.sqrt(ux * ux + uy * uy + uz * uz));
        ux /= ul;
        uy /= ul;
        uz /= ul;
        // the arm it settles into: length and thickness by the hold
        double length = 1.5 + 0.9 * ease(charge);
        double thick = 0.38 + 0.2 * ease(charge);
        // the burst: up past its size and settling (0 before, 1 at its biggest, 0 once settled)
        double burst = age < SWELL_BURST ? Math.sin(Math.PI * Math.min(1.0, age / SWELL_BURST)) * (1.0 - 0.3 * age / SWELL_BURST) : 0.0;
        double settle = smooth((age - 2.0) / (SWELL_BURST - 2.0));
        double cxm = x + wx * length * 0.5;
        double cym = y + wy * length * 0.5;
        double czm = z + wz * length * 0.5;
        int n = SWELL_LUMPS;
        for (int k = 0; k < n; k++) {
            double t = (k + 0.5) / n;
            // settled: along the arm, lumpy (a little up, down and to the sides), a little smaller toward the fist
            double ra = 2 * Math.PI * FireMesh.rand(seed, k + 5);
            double off = thick * 0.25 * FireMesh.rand(seed, k + 9);
            double px = x + wx * length * t + (sx * Math.cos(ra) + ux * Math.sin(ra)) * off + shake;
            double py = y + wy * length * t + uy * Math.sin(ra) * off;
            double pz = z + wz * length * t + (sz * Math.cos(ra) + uz * Math.sin(ra)) * off;
            double pr = thick * (0.95 + 0.3 * FireMesh.rand(seed, k + 17)) * (1.0 - 0.15 * t);
            // in the burst: thrown out round the arm's middle, swollen
            double ba = 2 * Math.PI * (k + FireMesh.rand(seed, k + 21)) / n;
            double bu = FireMesh.rand(seed, k + 23) * 1.4 - 0.3;
            double bd = 1.25 * burst;
            double qx = cxm + (sx * Math.cos(ba) + ux * bu * 0.6) * bd;
            double qy = cym + Math.abs(bu) * bd + 0.4 * burst;
            double qz = czm + (sz * Math.cos(ba) + uz * bu * 0.6) * bd;
            double qr = 1.0 + 0.6 * FireMesh.rand(seed, k + 29);
            double gx = qx + (px - qx) * settle;
            double gy = qy + (py - qy) * settle;
            double gz = qz + (pz - qz) * settle;
            double gr = qr * burst * (1.0 - settle) + pr * settle + 0.05;
            // grows out of the arm in the first ticks
            gr *= smooth(age / 2.5);
            if (gr > 0.03) {
                MochiArt.blob(sink, gx, gy, gz, b[0], b[1], b[2], b[3], b[4], b[5], gr * 1.15, gr, gr, 0.9, 0.16, age, seed + k, MochiArt.MOCHI, 1.0);
            }
        }
        // splatters thrown off the burst
        if (age < SWELL_BURST + 4) {
            for (int k = 0; k < 8; k++) {
                double t = Math.min(1.0, age / (SWELL_BURST + 4));
                double a = 2 * Math.PI * (k + FireMesh.rand(seed, k + 40)) / 8.0;
                double up = 0.3 + 0.9 * FireMesh.rand(seed, k + 41);
                double d = 0.6 + 2.2 * ease(t);
                double fall = 2.2 * t * t;
                MochiArt.ball(sink, cxm + sx * Math.cos(a) * d + ux * up * d * 0.3, cym + up * d * 0.6 - fall, czm + sz * Math.cos(a) * d + uz * up * d * 0.3,
                    0.1 * (1.0 - t) + 0.04, sx * Math.cos(a), up, sz * Math.cos(a), 1.4, 0.0, age, seed + 40 + k, MochiArt.MOCHI);
            }
        }
        // drips hanging off its underside, drops falling from them onto the ground and splatting there
        if (settle > 0.5) {
            for (int k = 0; k < 3; k++) {
                double on = (0.25 + 0.3 * k) * length;
                double hx = x + wx * on;
                double hy = y + wy * on - thick * 0.85;
                double hz = z + wz * on;
                double len = thick * (0.5 + 0.25 * Math.sin(age * 0.13 + k));
                MochiArt.tubeBegin();
                for (int q = 0; q <= 3; q++) {
                    double f = q / 3.0;
                    MochiArt.tubePoint(hx, hy - len * f, hz, thick * 0.22 * (1.0 - 0.6 * f));
                }
                MochiArt.tube(sink, 8, MochiArt.MOCHI, 0.0, 0.0);
                double bottom = Double.isNaN(ground) ? hy - len - 1.4 : ground;
                double fall = (age * 0.07 + k * 0.37) % 1.0;
                double dy = hy - len - (hy - len - bottom) * fall * fall;
                MochiArt.ball(sink, hx, dy, hz, thick * 0.12, 0, 1, 0, 1.3, 0.0, age, seed + 20 + k, MochiArt.MOCHI);
                if (!Double.isNaN(ground)) {
                    double sp = 0.28 + 0.1 * k;
                    MochiArt.blob(sink, hx + sx * 0.2 * k, ground + 0.03, hz + sz * 0.2 * k, 1, 0, 0, 0, 1, 0, sp, 0.04, sp * 0.8, 0.7, 0.3, age,
                        seed + 60 + k, MochiArt.MOCHI, 1.0);
                }
            }
        }
        double tip = length + thick * 0.55;
        MochiArt.fist(sink, x + wx * tip + shake, y + wy * tip, z + wz * tip, wx, wy, wz, sx, 0, sz, (0.34 + 0.22 * charge) * smooth(age / 3.0) + 0.02,
            coat, age, seed + 3);
    }

    /** 3.6.3: the swell's burst (ticks): up to its biggest and settled into the arm of dough. */
    public static final double SWELL_BURST = 10.0;

    /** 3.6.3: how many lumps of dough make the swollen arm. */
    static final int SWELL_LUMPS = 6;

    /**
     * Yaki Mochi fired: the burning fist at (x, y, z) flying along (wx, wy, wz) (unit), {@code age} ticks into its flight: a big fist
     * (black with Armament) on a jet of the house's banded fire trailing behind it.
     */
    public static void yaki(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double age, boolean coat, int seed) {
        yaki(sink, x, y, z, wx, wy, wz, age, coat, seed, 1.0);
    }

    /** 3.6.3: as above, {@code charge} (0 to 1) how long it was held: the fist and its jet of fire the bigger. */
    public static void yaki(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double age, boolean coat, int seed,
                            double charge) {
        double kx = -wz;
        double kz = wx;
        double kl = Math.max(1.0E-6, Math.hypot(kx, kz));
        double s = 0.55 + 0.45 * charge;
        MochiArt.fist(sink, x, y, z, wx, wy, wz, kx / kl, 0, kz / kl, 0.7 * s, coat, age, seed);
        for (int k = 0; k < 7; k++) {
            double d = (0.7 + k * 0.6) * s;
            double r = (0.55 - k * 0.055) * s;
            double wob = 0.12 * Math.sin(age * 1.7 + k * 1.3);
            FireMesh.puff(sink, x - wx * d + kx / kl * wob, y - wy * d + wob * 0.6, z - wz * d + kz / kl * wob, r, -wx, -wy, -wz, 0.8, 0.6,
                Math.max(0.15, 1.0 - k * 0.14), 0.55, true, 1.0 - k * 0.1, age, seed + k);
        }
    }

    /**
     * 3.6.3 (the reference's Grilled Dough, frames 31 to 36): the swollen arm of dough bursting into a big white puff the moment its fist
     * is thrown from (x, y, z) along (wx, wy, wz): the puff swelling and blowing apart, dough flung, a spark of fire; {@code age} ticks in.
     */
    public static void yakiPuff(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double age, int seed) {
        if (age < 0 || age > YAKI_PUFF) {
            return;
        }
        double f = age / YAKI_PUFF;
        double r = 1.1 * Math.sin(Math.PI * Math.min(1.0, f * 1.6)) + 0.05;
        double cx = x - wx * 0.8;
        double cy = y - wy * 0.8 + 0.2;
        double cz = z - wz * 0.8;
        if (r > 0.06) {
            MochiArt.blob(sink, cx, cy, cz, 1, 0, 0, 0, 1, 0, r * 1.2, r, r * 1.1, 0.9, 0.3, age, seed, MochiArt.MOCHI, 1.0);
        }
        splat(sink, cx, cy, cz, wx, wy + 0.3, wz, 1.3, age * 1.3, seed + 3);
        if (age < 4.0) {
            MochiArt.glow(sink, x, y, z, 0.6 + 0.6 * age / 4.0, 0xFFB347, 0.8 * (1.0 - age / 4.0));
        }
    }

    /** 3.6.3: how long the throw's puff lasts (ticks). */
    public static final double YAKI_PUFF = 8.0;

    // ------------------------------------------------------------------------------------------------------ the donut

    /** 3.6.3: a donut's tube (its radius) to the donut's outer radius (the awakened Muso Donuts': every donut of the fruit is one of those). */
    public static final double DONUT_THICK = 0.28;
    /** 3.6.3: how lumpy a donut is (a share of its tube). */
    public static final double DONUT_LUMP = 0.34;

    /** 3.6.5: an elastic shot out (0 to 1): fast, past its end by about a seventh, springing back onto it. */
    public static double elastic(double t) {
        double u = clamp(t);
        return 1.0 - Math.pow(1.0 - u, 3.0) + 0.3 * Math.sin(Math.PI * u) * u * u;
    }

    /** 3.6.3: soft dough's ease: up past its size a little and settling back (0 to 1). */
    public static double springy(double t) {
        double u = Math.max(0.0, Math.min(1.0, t));
        return 1.0 - Math.pow(1.0 - u, 3.0) + 0.12 * Math.sin(Math.PI * u) * u;
    }

    /** 3.6.3: a donut's hole, its radius ({@code big} the donut's outer radius): what a fist out of it fills. */
    public static double hole(double big) {
        return big * (1.0 - 2.0 * DONUT_THICK);
    }

    /**
     * 3.6.3 (the owner: "Mochi anemoni, the fists should fill the entire hole and adapt to the size of the donuts, just like the V move in
     * the awakening"; the references: puffy lumpy rings of dough with drips, a fist in each hole): one donut of the fruit at (x, y, z)
     * facing (ax, ay, az) (unit), {@code big} its radius; its fist resting in its hole (filling it, knuckles out) unless {@code punch} is
     * over 0: then the arm (as wide as the hole) is out of the hole to (fx, fy, fz). Black with Armament ({@code coat}), as every fist and
     * arm of the fruit; the ring always white. {@code turn}: the ring turned round its axis (a rolling wheel's).
     */
    public static void donut(FireMesh.Sink sink, double x, double y, double z, double ax, double ay, double az, double big, double turn, int drips,
                             double punch, double fx, double fy, double fz, boolean coat, double age, int seed, boolean left) {
        donut(sink, x, y, z, ax, ay, az, big, 1.0, turn, drips, punch, fx, fy, fz, coat, age, seed, left);
    }

    /**
     * 3.6.3 (the owner: "Mochi appearing and disappearing should have more realism not just vanish"): as above, {@code form} (0 to 1)
     * how far it has formed: a lump of dough puffing up (overshooting a little, as soft dough does) whose hole opens as it swells into
     * the ring; going, the same backward: the hole closes, the ring slumps into a lump that shrinks away.
     */
    public static void donut(FireMesh.Sink sink, double x, double y, double z, double ax, double ay, double az, double big, double form, double turn,
                             int drips, double punch, double fx, double fy, double fz, boolean coat, double age, int seed, boolean left) {
        if (big < 0.02 || form < 0.02) {
            return;
        }
        double f = Math.min(1.0, form);
        // 3.6.5 (the owner: "more animated, more squishy"): the dough breathes (the ring and its tube swelling and easing, out of
        // step), and the tube swells as its arm pushes out of the hole
        double breathe = 1.0 + 0.04 * Math.sin(age * 0.42 + seed * 1.3);
        double puff = 1.0 + 0.06 * Math.sin(age * 0.57 + seed * 0.7) + (punch > 0.0 ? 0.14 * Math.min(1.0, punch) : 0.0);
        double s = springy(f);
        double thick = big * DONUT_THICK * s * (1.0 + 0.8 * (1.0 - f)) * puff;
        double ring = (big - big * DONUT_THICK) * s * Math.pow(f, 0.7) * breathe;
        // a forming lump slumps a little (it is soft until it has its shape)
        y -= (1.0 - f) * big * 0.25;
        MochiArt.donut(sink, x, y, z, ax, ay, az, ring, thick, DONUT_LUMP, turn, f > 0.8 ? drips : 0, age, seed, MochiArt.MOCHI);
        big *= s * f;
        double hole = hole(big);
        if (punch > 0.0) {
            // the arm out of the hole, as wide as it (its square bar's corners touching the dough round it)
            arm(sink, x, y, z, fx, fy, fz, hole * 0.66, hole * 0.88, 0.0, coat, age, seed, left);
        } else {
            // the fist resting in the hole, its knuckles just out of it, filling it
            double kx = -az;
            double kz = ax;
            double kl = Math.sqrt(kx * kx + kz * kz);
            if (kl < 1.0E-4) {
                kx = 1;
                kz = 0;
                kl = 1;
            }
            kx /= kl;
            kz /= kl;
            // its knuckles' row turned with the ring (k across the way, u = a x k)
            double ux = ay * kz;
            double uy = az * kx - ax * kz;
            double uz = -ay * kx;
            double ul = Math.max(1.0E-9, Math.sqrt(ux * ux + uy * uy + uz * uz));
            double ct = Math.cos(turn);
            double st = Math.sin(turn);
            double rx = kx * ct + ux / ul * st;
            double ry = uy / ul * st;
            double rz = kz * ct + uz / ul * st;
            MochiArt.fist(sink, x + ax * thick * 0.15, y + ay * thick * 0.15, z + az * thick * 0.15, ax, ay, az, rx, ry, rz, hole * 0.98, coat,
                age, seed, left);
        }
    }

    // ------------------------------------------------------------------------------------------------------ 3.6.3: the reference's moves

    // ------------------------------------------------------------------------------------------------------ Mochi Muddle
    // 3.6.3, after the reference's Dough Muddle read frame by frame (next-ai-3.6.3.md G), in ticks from the press: a charge with the
    // arms out (to MUDDLE_START - 6), a dive, the user engulfed in a block of dough, a splash bursting up out of it (MUDDLE_START), a
    // thick pool round them (sunk to the legs) that flattens, and from MUDDLE_RUN the puddle running out ahead past the one aimed at,
    // spreading wider with spiky splashed edges, splats round it and faint swirls of wind over it; it lies, then sinks away.

    /** The splash (ticks from the press), when the puddle starts to run out ahead (from the splash), how long it lies (from the splash). */
    public static final int MUDDLE_START = 28;
    public static final double MUDDLE_RUN = 14.0;
    public static final double MUDDLE_LIE = 100.0;
    /** The pool round the user, the puddle's length ahead of it, how fast it runs (blocks a tick), its width near and far. */
    public static final double MUDDLE_POOL = 2.3;
    public static final double MUDDLE_LENGTH = 10.0;
    public static final double MUDDLE_SPEED = 0.6;
    public static final double MUDDLE_NEAR = 3.0;
    public static final double MUDDLE_FAR = 4.6;

    /** How far ahead of the splash the Muddle puddle has run {@code age} ticks after the splash. */
    public static double muddleReach(double age) {
        return Math.max(0.0, Math.min(MUDDLE_LENGTH, (age - MUDDLE_RUN) * MUDDLE_SPEED));
    }

    /** The Muddle puddle's width {@code along} blocks ahead of the splash. */
    public static double muddleWidth(double along) {
        return MUDDLE_NEAR + (MUDDLE_FAR - MUDDLE_NEAR) * clamp(along / MUDDLE_LENGTH);
    }

    /**
     * Mochi Muddle (X): its splash at (x, y, z) (the ground at the user's feet) and the puddle running from it along (wx, wz) (flat,
     * unit); {@code age} ticks from the splash (from -4: the dough engulfing the user just before it).
     */
    public static void muddle(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double age, int seed) {
        if (age < -4.0 || age > MUDDLE_LIE) {
            return;
        }
        double px = -wz;
        double pz = wx;
        double fade = 1.0 - smooth((age - MUDDLE_LIE + 14.0) / 14.0);
        // ---- the user engulfed in a block of dough (frame 48), bursting into the splash
        if (age < 3.0) {
            double g = smooth((age + 4.0) / 3.0) * (1.0 - smooth((age - 0.5) / 2.5));
            if (g > 0.03) {
                MochiArt.blob(sink, x, y + 1.0, z, wx, 0, wz, 0, 1, 0, 0.85 * g + 0.1, 1.15 * g + 0.1, 0.85 * g + 0.1, 0.45, 0.25, age, seed,
                    MochiArt.MOCHI, 1.0);
            }
        }
        if (age < 0) {
            return;
        }
        // ---- the splash (frames 50 to 58): a column of dough thrown up and falling back, a crown of splashes, droplets flung everywhere
        if (age < 16.0) {
            double up = Math.sin(Math.PI * Math.min(1.0, age / 14.0));
            double top = 3.8 * up;
            if (top > 0.2) {
                MochiArt.tubeBegin();
                for (int k = 0; k <= 8; k++) {
                    double t = k / 8.0;
                    double wob = 0.18 * Math.sin(t * 5.0 + age * 0.6) * t;
                    MochiArt.tubePoint(x + px * wob - wx * 0.4 * t * t, y + top * t, z + pz * wob - wz * 0.4 * t * t, (0.75 - 0.45 * t) * (0.5 + 0.5 * up));
                }
                MochiArt.tube(sink, 12, MochiArt.MOCHI, 0.0, 0.0);
                MochiArt.ball(sink, x - wx * 0.4, y + top, z - wz * 0.4, 0.45 * up + 0.1, 0, 1, 0, 1.1, 0.25, age, seed + 3, MochiArt.MOCHI);
            }
            for (int k = 0; k < 12; k++) {
                double a = 2 * Math.PI * (k + FireMesh.rand(seed, k + 10)) / 12.0;
                double t = age / 16.0;
                double sp = 2.0 + 3.0 * FireMesh.rand(seed, k + 20);
                double h = (1.5 + 2.5 * FireMesh.rand(seed, k + 30)) * t * 4.0 - 7.0 * t * t * 4.0 * 0.6;
                if (h < -0.1 && t > 0.3) {
                    continue;
                }
                MochiArt.ball(sink, x + Math.cos(a) * sp * t * 1.6, y + 0.3 + Math.max(0.0, h), z + Math.sin(a) * sp * t * 1.6,
                    0.18 * (1.0 - 0.5 * t) + 0.05, Math.cos(a), 0.5, Math.sin(a), 1.5, 0.1, age, seed + 40 + k, MochiArt.MOCHI);
            }
            // the crown: splashes standing up round the column's foot, leaning out
            double crown = Math.sin(Math.PI * Math.min(1.0, age / 10.0));
            for (int k = 0; k < 9 && crown > 0.05; k++) {
                double a = 2 * Math.PI * (k + 0.4 * FireMesh.rand(seed, k + 60)) / 9.0;
                double r = 0.9 + 0.5 * crown;
                double bx = x + Math.cos(a) * r;
                double bz = z + Math.sin(a) * r;
                double h = (0.7 + 0.6 * FireMesh.rand(seed, k + 70)) * crown;
                MochiArt.cone(sink, bx, y + 0.05, bz, bx + Math.cos(a) * h * 0.6, y + h, bz + Math.sin(a) * h * 0.6, 0.16, MochiArt.MOCHI);
            }
        }
        // ---- the pool round the user (frames 56 to 66): a thick mound round their legs at first, flattening into the puddle
        double pool = ease(age / 5.0) * fade;
        double mound = 1.0 - smooth((age - 12.0) / 10.0);
        if (pool > 0.02) {
            MochiArt.blob(sink, x, y + 0.06, z, wx, 0, wz, 0, 1, 0, MUDDLE_POOL * pool, (0.14 + 0.45 * mound) * pool, MUDDLE_POOL * 0.9 * pool, 0.85,
                0.35, age, seed + 5, MochiArt.MOCHI, 1.0);
            if (mound > 0.05) {
                MochiArt.blob(sink, x, y + 0.3 * mound, z, wx, 0, wz, 0, 1, 0, 0.9, 0.6 * mound, 0.8, 0.7, 0.4, age, seed + 6, MochiArt.MOCHI, 1.0);
            }
        }
        // ---- the puddle running out ahead (frames 70 to 82), spreading wider (to 128)
        double reach = muddleReach(age);
        int n = (int) Math.ceil(reach / 1.1);
        for (int k = 1; k <= n; k++) {
            double d = Math.min(reach, k * 1.1);
            double front = Math.max(0.0, 1.0 - (reach - d) / 1.8);
            double w = muddleWidth(d) * (0.75 + 0.25 * smooth((age - MUDDLE_RUN - d / MUDDLE_SPEED) / 12.0));
            double wob = (FireMesh.rand(seed, k + 80) - 0.5) * w * 0.25;
            double h = (0.1 + 0.16 * front) * fade;
            MochiArt.blob(sink, x + wx * d + px * wob, y + 0.05, z + wz * d + pz * wob, wx, 0, wz, 0, 1, 0, 0.8, h, w * 0.5, 0.85, 0.3, age,
                seed + 100 + k, MochiArt.MOCHI, 1.0);
        }
        // ---- the spiky splashed edges (frames 100 to 128): flat spikes of dough radiating out from the puddle's rim, like spilled paint
        double spread = smooth((age - 4.0) / 10.0) * fade;
        int spikes = 36;
        for (int k = 0; k < spikes && spread > 0.02; k++) {
            double t = FireMesh.rand(seed, k + 200);
            double along = -MUDDLE_POOL * 0.6 + t * (MUDDLE_POOL * 0.6 + Math.max(0.5, reach));
            if (along > MUDDLE_POOL * 0.6 && along > reach) {
                continue;
            }
            double side = k % 2 == 0 ? 1.0 : -1.0;
            double half = along < MUDDLE_POOL * 0.6 ? MUDDLE_POOL * 0.85 : muddleWidth(along) * 0.45;
            double out = (0.5 + 1.3 * FireMesh.rand(seed, k + 210)) * spread;
            double ox = x + wx * along + px * side * (half + out * 0.5);
            double oz = z + wz * along + pz * side * (half + out * 0.5);
            double ang = (FireMesh.rand(seed, k + 220) - 0.5) * 0.9;
            double dx = px * side * Math.cos(ang) + wx * Math.sin(ang);
            double dz = pz * side * Math.cos(ang) + wz * Math.sin(ang);
            MochiArt.blob(sink, ox, y + 0.04, oz, dx, 0, dz, 0, 1, 0, out * 0.75 + 0.1, 0.035, 0.09 + 0.06 * FireMesh.rand(seed, k + 230), 0.9, 0.1,
                age, seed + 240 + k, MochiArt.MOCHI, 1.0);
        }
        // ---- splats thrown round it
        for (int k = 0; k < 8 && spread > 0.02; k++) {
            double a = 2 * Math.PI * FireMesh.rand(seed, k + 300);
            double d = 2.4 + 2.4 * FireMesh.rand(seed, k + 310);
            double r = (0.18 + 0.22 * FireMesh.rand(seed, k + 320)) * spread;
            MochiArt.blob(sink, x + Math.cos(a) * d + wx * reach * 0.4, y + 0.04, z + Math.sin(a) * d + wz * reach * 0.4, Math.cos(a), 0, Math.sin(a), 0, 1,
                0, r * 1.5, 0.04, r, 0.8, 0.3, age, seed + 330 + k, MochiArt.MOCHI, 1.0);
        }
        // ---- faint swirls of wind over the pool (frames 60 to 96)
        if (age > 2.0 && age < 50.0) {
            double sw = Math.sin(Math.PI * (age - 2.0) / 48.0);
            for (int k = 0; k < 3; k++) {
                double r = 2.4 + 0.9 * k + 0.03 * age;
                FireMesh.ring(sink, x, y + 0.25 + 0.15 * k, z, 0, 1, 0, r, 0.06, 0.04, 0xFFFFFF, 0.22 * sw);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ Lotus Mochi Combo
    // 3.6.3 (the owner: "do not give up until the moves look exactly like the reference GIFs ... to very minute detail"): the
    // reference's Lotus Dough Combo read frame by frame and timed in ticks (next-ai-3.6.3.md G): a ribbon of dough swoops out of the
    // user and curls into a swirl beside them (tick 0 to 4), the swirl balls up (to 12) and flashes white in rings (12), a big donut
    // stands out of the flash (15) ringed in thin shock rings, dough dripping off it, a second flash (31) and its fist punches out into
    // the one aimed at (34), who is thrown high (39), struck in the air three times (57, 67, 78), driven down by a giant fist out of a
    // donut above (87, falling from 96) into the ground (106): a white flash, a flat splat of dough, rocks thrown, a ring rising.

    /** The ribbon lands and curls; the swirl is a ball and flashes; the donut is out of the flash; the flash before the punch. */
    public static final double LOTUS_CURL = 4.0;
    public static final double LOTUS_BALL = 12.0;
    public static final double LOTUS_OPEN = 15.0;
    public static final double LOTUS_CHARGE = 31.0;
    /** The fist out of the donut (the server's blow lands then), the one hit thrown up, the donut gone. */
    public static final int LOTUS_PUNCH = 34;
    public static final int LOTUS_LAUNCH = 39;
    public static final double LOTUS_LIFE = 48.0;
    /**
     * The blows in the air, the giant fist over the body, the fall, the ground. 3.6.4 (the owner: "pushed in ragdoll from side to side
     * depending where he was punched from basically from donut to donut carrying basically being glued with each punch to the next,
     * fast"): a blow every 6 ticks, each carrying the body {@link #LOTUS_CARRY} ticks across to the next donut.
     */
    public static final int[] LOTUS_AIR = {50, 56, 62, 68};
    public static final int LOTUS_DROP = 69;
    public static final int LOTUS_FALL = 73;
    public static final int LOTUS_GROUND = 82;
    /** How long a blow in the air carries the body, glued to its fist, across to the next donut (ticks). */
    public static final double LOTUS_CARRY = 3.0;
    /**
     * Where the body is after the launch and after each blow in the air (the owner's sketch: knocked from donut to donut, side to side):
     * across to the user's right, up, ahead, from the ground it stood on (blocks).
     */
    public static final double[][] LOTUS_STATIONS = {
        {0.0, 13.0, 0.0},
        {-3.4, 11.2, 0.5},
        {0.5, 14.4, 0.1},
        {-3.1, 13.6, 0.7},
        {0.4, 14.8, 0.0},
    };
    /** How high the one hit is thrown (blocks over the ground it stood on), how long the rise takes. */
    public static final double LOTUS_HIGH = 13.0;
    public static final double LOTUS_RISE = 10.0;
    /** The donut's outer radius (the reference's is about twice the user's height across) and where it stands from the user. */
    public static final double LOTUS_DONUT = 1.95;
    public static final double LOTUS_LEFT = 2.8;
    public static final double LOTUS_AHEAD = 0.9;
    /** An air blow's donut, how long before its blow it forms and how long after it it goes; the giant fist's donut and its life (from the
     * drop), the ground's life. */
    public static final double LOTUS_AIR_DONUT = 1.15;
    public static final double LOTUS_AIR_EARLY = 6.0;
    public static final double LOTUS_AIR_LIFE = 11.0;
    public static final double LOTUS_SLAM_DONUT = 2.3;
    public static final double LOTUS_SLAM_LIFE = 30.0;
    public static final double LOTUS_GROUND_LIFE = 40.0;

    /**
     * Where the one the Lotus combo hit is {@code age} ticks into the combo, from the ground it stood on, the user having faced (wx, wz)
     * (flat, unit) (the server moves the body so: server/MochiMoves.Juggled): on the ground to the launch, thrown up to the first station
     * in {@link #LOTUS_RISE} ticks; each blow in the air carries it, glued to the fist, fast across to the next station
     * ({@link #LOTUS_STATIONS}, in {@link #LOTUS_CARRY} ticks), where it hangs, sinking a little, until the next; driven down from
     * {@link #LOTUS_FALL} into the ground at {@link #LOTUS_GROUND} (faster and faster); into out (x, y, z).
     */
    public static double[] lotusBody(double age, double wx, double wz, double[] out) {
        double a;
        double y;
        double f;
        if (age <= LOTUS_LAUNCH || age >= LOTUS_GROUND) {
            a = 0.0;
            y = 0.0;
            f = 0.0;
        } else if (age < LOTUS_AIR[0]) {
            double[] s0 = LOTUS_STATIONS[0];
            double u = ease((age - LOTUS_LAUNCH) / LOTUS_RISE);
            a = s0[0] * u;
            y = s0[1] * u;
            f = s0[2] * u;
        } else {
            int i = LOTUS_AIR.length - 1;
            while (i > 0 && age < LOTUS_AIR[i]) {
                i--;
            }
            double[] from = LOTUS_STATIONS[i];
            double[] to = LOTUS_STATIONS[i + 1];
            double u = ease((age - LOTUS_AIR[i]) / LOTUS_CARRY);
            double hang = Math.max(0.0, age - LOTUS_AIR[i] - LOTUS_CARRY);
            a = from[0] + (to[0] - from[0]) * u;
            y = from[1] + (to[1] - from[1]) * u - 0.04 * hang * hang;
            f = from[2] + (to[2] - from[2]) * u;
            if (age > LOTUS_FALL) {
                double fall = (age - LOTUS_FALL) / (LOTUS_GROUND - LOTUS_FALL);
                y = y * (1.0 - fall * fall);
            }
        }
        // across (the right of the way faced: (-wz, wx)), up, ahead
        out[0] = -wz * a + wx * f;
        out[1] = y;
        out[2] = wx * a + wz * f;
        return out;
    }

    /** How high over the ground it stood on the one the Lotus combo hit is at {@code age} ticks in (as {@link #lotusBody}). */
    public static double lotusHeight(double age) {
        return lotusBody(age, 0, 1, new double[3])[1];
    }

    /**
     * Where the donut of the Lotus combo's blow {@code i} in the air stands, from the ground the body stood on, the user having faced
     * (wx, wz): beyond the body's station, on the side it is struck from (its fist drives the body on to the next station); into out (x,
     * y, z, then the way its fist strikes, unit).
     */
    public static double[] lotusDonut(int i, double wx, double wz, double[] out) {
        double[] from = LOTUS_STATIONS[i];
        double[] to = LOTUS_STATIONS[i + 1];
        double da = to[0] - from[0];
        double dy = to[1] - from[1];
        double df = to[2] - from[2];
        double dl = Math.max(1.0E-6, Math.sqrt(da * da + dy * dy + df * df));
        da /= dl;
        dy /= dl;
        df /= dl;
        double back = 2.3;
        double a = from[0] - da * back;
        double y = from[1] + 0.9 - dy * back;
        double f = from[2] - df * back;
        out[0] = -wz * a + wx * f;
        out[1] = y;
        out[2] = wx * a + wz * f;
        out[3] = -wz * da + wx * df;
        out[4] = dy;
        out[5] = wx * da + wz * df;
        return out;
    }

    /**
     * Where the Lotus donut stands from its user's feet, the user facing (wx, wz) (flat, unit): to their left, a little ahead, its
     * bottom on the ground; into out (x, y, z).
     */
    public static double[] lotusAt(double wx, double wz, double[] out) {
        // the left of (wx, wz) seen from above: (wz, -wx)
        out[0] = wz * LOTUS_LEFT + wx * LOTUS_AHEAD;
        out[1] = LOTUS_DONUT + 0.05;
        out[2] = -wx * LOTUS_LEFT + wz * LOTUS_AHEAD;
        return out;
    }

    /**
     * Lotus Mochi Combo (C), the user's part: from the user's hand (hx, hy, hz) the ribbon, the swirl, the flashes and the donut at
     * (dx, dy, dz) facing (wx, wz) (flat, unit; the user's look), its fist punching onto (tx, ty, tz) at {@link #LOTUS_PUNCH};
     * {@code ground} the height of the ground under the donut; {@code age} ticks in.
     */
    public static void lotus(FireMesh.Sink sink, double hx, double hy, double hz, double dx, double dy, double dz, double wx, double wz,
                             double tx, double ty, double tz, double ground, double age, boolean coat, int seed) {
        if (age < 0 || age > LOTUS_LIFE) {
            return;
        }
        // the plane the donut stands in: across (sx, 0, sz) and up
        double sx = -wz;
        double sz = wx;
        // ---- the ribbon: out of the hand in an arc over to the donut's place, its head racing ahead of its tail, then winding round
        // that place into a swirl that tightens and fattens into a ball (the reference's frames 18 to 28)
        if (age < LOTUS_BALL + 1) {
            ribbon(sink, hx, hy, hz, dx, dy, dz, sx, sz, wx, wz, age, seed);
        }
        // ---- the flash out of the ball: a white burst, thin rings of shock round it (frame 30)
        if (age >= LOTUS_BALL - 1 && age < LOTUS_OPEN + 5) {
            double f = (age - LOTUS_BALL + 1) / 9.0;
            double fade = 1.0 - smooth(f);
            MochiArt.glow(sink, dx, dy, dz, LOTUS_DONUT * (1.1 + 0.6 * ease(f * 2.0)), 0xFFFFFF, 0.95 * fade);
            shockRings(sink, dx, dy, dz, wx, wz, LOTUS_DONUT * (1.0 + 1.6 * ease(f)), 0.75 * fade, seed);
        }
        // ---- the donut (frames 33 to 57): out of the flash with a spring, dripping, rings of shock pulsing out round it; it slumps
        // away after the punch
        double form = smooth((age - LOTUS_OPEN + 2.0) / 3.0) * (1.0 - smooth((age - LOTUS_LIFE + 6.0) / 6.0));
        if (form > 0.02) {
            double out = 0.0;
            double fx = dx;
            double fy = dy;
            double fz = dz;
            if (age >= LOTUS_PUNCH - 1.0 && age < LOTUS_LIFE - 4.0) {
                // the punch: out in a tick and a half, held, drawn back (frames 53 to 58)
                double pa = age - (LOTUS_PUNCH - 1.0);
                out = pa < 1.5 ? ease(pa / 1.5) : pa < 5.0 ? 1.0 : 1.0 - smooth((pa - 5.0) / 4.0);
                fx = dx + (tx - dx) * out;
                fy = dy + (ty - dy) * out;
                fz = dz + (tz - dz) * out;
            }
            double ax = wx;
            double ay = 0.0;
            double az = wz;
            if (out > 0.02) {
                // the donut turns to throw the punch where it goes
                double ux = tx - dx;
                double uy = ty - dy;
                double uz = tz - dz;
                double ul = Math.max(1.0E-6, Math.sqrt(ux * ux + uy * uy + uz * uz));
                double turn = Math.min(1.0, out * 1.5);
                ax = wx + (ux / ul - wx) * turn;
                ay = uy / ul * turn;
                az = wz + (uz / ul - wz) * turn;
                double al = Math.max(1.0E-6, Math.sqrt(ax * ax + ay * ay + az * az));
                ax /= al;
                ay /= al;
                az /= al;
            }
            donut(sink, dx, dy, dz, ax, ay, az, LOTUS_DONUT, form, 0.0, 3, out > 0.02 ? out : 0.0, fx, fy, fz, coat, age, seed, false);
            // dough dripping off its underside and splatting on the ground under it (frames 36 to 48)
            drops(sink, dx, dy, dz, sx, sz, ground, form, age, seed);
            if (age > LOTUS_OPEN && age < LOTUS_CHARGE + 2) {
                // rings of shock pulsing out of it, one every five ticks
                for (int k = 0; k < 3; k++) {
                    double ra = (age - LOTUS_OPEN + k * 5.0) % 15.0 / 15.0;
                    shockRings(sink, dx, dy, dz, wx, wz, LOTUS_DONUT * (1.15 + 1.1 * ra), 0.5 * (1.0 - ra) * form, seed + k * 17);
                }
            }
        }
        // ---- the flash before the punch, the blow's own burst at the fist (frames 51 and 53)
        if (age >= LOTUS_CHARGE && age < LOTUS_PUNCH + 4) {
            double f = (age - LOTUS_CHARGE) / (LOTUS_PUNCH + 4 - LOTUS_CHARGE);
            double fade = Math.sin(Math.PI * Math.min(1.0, f * 1.4));
            MochiArt.glow(sink, dx, dy, dz, LOTUS_DONUT * (1.5 + 0.8 * f), 0xFFFFFF, 0.9 * fade);
            shockRings(sink, dx, dy, dz, wx, wz, LOTUS_DONUT * (1.3 + 1.4 * f), 0.7 * fade, seed + 91);
        }
        if (age >= LOTUS_PUNCH && age < LOTUS_PUNCH + 6) {
            double f = (age - LOTUS_PUNCH) / 6.0;
            MochiArt.glow(sink, tx, ty, tz, 1.1 + 1.2 * ease(f), 0xFFFFFF, 0.9 * (1.0 - f));
            shockRings(sink, tx, ty, tz, wx, wz, 0.8 + 2.2 * ease(f), 0.6 * (1.0 - f), seed + 7);
        }
    }

    /**
     * The Lotus ribbon (the reference's frames 18 to 28): a comet of dough out of the hand, thrown high and swooping down onto the donut's
     * place (its head thick, its tail thinning out behind), then winding round that place into a swirl that tightens and fattens into a
     * ball.
     */
    static void ribbon(FireMesh.Sink sink, double hx, double hy, double hz, double dx, double dy, double dz, double sx, double sz, double wx,
                       double wz, double age, int seed) {
        int n = 26;
        double fly = ease(age / LOTUS_CURL);
        double wind = clamp((age - LOTUS_CURL * 0.6) / (LOTUS_BALL - LOTUS_CURL * 0.6));
        // the throw's curve: up out of the hand, high over, down onto the place
        double p1x = hx + (dx - hx) * 0.15;
        double p1y = hy + 3.6;
        double p1z = hz + (dz - hz) * 0.15;
        double p2x = dx + (dx - hx) * 0.25;
        double p2y = dy + 3.2;
        double p2z = dz + (dz - hz) * 0.25;
        MochiArt.tubeBegin();
        for (int k = 0; k <= n; k++) {
            double t = k / (double) n;
            double u = clamp(fly - (1.0 - t) * 0.5 * (1.0 - wind));
            double a = 1.0 - u;
            double ax = a * a * a * hx + 3 * a * a * u * p1x + 3 * a * u * u * p2x + u * u * u * dx;
            double ay = a * a * a * hy + 3 * a * a * u * p1y + 3 * a * u * u * p2y + u * u * u * dy;
            double az = a * a * a * hz + 3 * a * a * u * p1z + 3 * a * u * u * p2z + u * u * u * dz;
            // round the donut's place in its plane: the swirl tightening as it winds (the head the innermost)
            double turn = 2 * Math.PI * (1.4 * t + 1.3 * wind) + 0.3 * FireMesh.rand(seed, 3);
            double r = LOTUS_DONUT * (1.25 - 0.6 * t) * (1.0 - 0.5 * wind);
            double cx = dx + sx * Math.cos(turn) * r + wx * 0.15 * Math.sin(turn * 1.3);
            double cy = dy + Math.sin(turn) * r;
            double cz = dz + sz * Math.cos(turn) * r + wz * 0.15 * Math.sin(turn * 1.3);
            double curl = smooth((fly - 0.7) / 0.3) * smooth(t * 1.4 + wind);
            double thick = (0.035 + 0.27 * Math.pow(t, 1.3)) * (1.0 + 1.4 * wind);
            MochiArt.tubePoint(ax + (cx - ax) * curl, ay + (cy - ay) * curl, az + (cz - az) * curl, thick);
        }
        MochiArt.tube(sink, 12, MochiArt.MOCHI, 0.0, 0.0);
        // the ball swelling in the swirl's heart as it winds tight
        double ball = smooth((age - LOTUS_CURL) / (LOTUS_BALL - LOTUS_CURL));
        if (ball > 0.02) {
            MochiArt.blob(sink, dx, dy, dz, wx, 0, wz, 0, 1, 0, LOTUS_DONUT * 0.85 * ball, LOTUS_DONUT * 0.8 * ball, LOTUS_DONUT * 0.85 * ball,
                0.9, 0.2, age, seed, MochiArt.MOCHI, 1.0);
        }
        // spatters flung off the ribbon's head as it whips round
        if (age > 2 && age < LOTUS_BALL) {
            for (int k = 0; k < 4; k++) {
                double t = (age * 0.3 + k * 0.25) % 1.0;
                double a = 2 * Math.PI * FireMesh.rand(seed + (int) age, k);
                double d = LOTUS_DONUT * (1.0 + 1.4 * t);
                MochiArt.ball(sink, dx + sx * Math.cos(a) * d, dy + Math.sin(a) * d - 1.2 * t * t, dz + sz * Math.cos(a) * d, 0.09 * (1.0 - t) + 0.03,
                    sx * Math.cos(a), Math.sin(a), sz * Math.cos(a), 1.5, 0.0, age, seed + k, MochiArt.MOCHI);
            }
        }
    }

    /**
     * The one the Lotus fist hit thrown up (the reference's frames 59 to 75): a white burst and splat where they left the ground
     * ({@code ground}), white streaks under them as they rise from (x, y, z) (their feet now); {@code age} ticks from the launch.
     */
    public static void lotusRise(FireMesh.Sink sink, double x, double y, double z, double ground, double age, int seed) {
        if (age < 0 || age > LOTUS_RISE + 8) {
            return;
        }
        if (age < 7) {
            double f = age / 7.0;
            MochiArt.glow(sink, x, ground + 0.6, z, 1.2 + 1.6 * f, 0xFFFFFF, 0.8 * (1.0 - f));
            FireMesh.ring(sink, x, ground + 0.15, z, 0, 1, 0, 0.8 + 3.0 * ease(f), 0.15, 0.1, 0xFFFFFF, 0.7 * (1.0 - f));
        }
        if (age < LOTUS_RISE + 2) {
            double fade = 1.0 - smooth((age - LOTUS_RISE + 2.0) / 4.0);
            for (int k = 0; k < 6; k++) {
                double a = 2 * Math.PI * (k + FireMesh.rand(seed, k + 80)) / 6.0;
                double r = 0.45 + 0.35 * FireMesh.rand(seed, k + 90);
                double len = Math.min(y - ground + 0.5, 3.0 + 2.5 * FireMesh.rand(seed, k + 100));
                FireMesh.streak(sink, x + Math.cos(a) * r, y - 0.2, z + Math.sin(a) * r, 0, -1, 0, len, 0.05, 0xFFFFFF, 0.75 * fade);
            }
        }
        // dough left on the ground where they stood, and a few rocks
        double stay = 1.0 - smooth((age - LOTUS_RISE - 2.0) / 6.0);
        MochiArt.blob(sink, x, ground + 0.05, z, 1, 0, 0, 0, 1, 0, 1.1 * ease(age / 3.0), 0.08 * stay, 0.9 * ease(age / 3.0), 0.8, 0.35, age, seed,
            MochiArt.MOCHI, 1.0);
        double[] t = new double[4];
        for (int k = 0; k < 6; k++) {
            com.hexhaki.piece.ironfist.IronArt.thrown(k, seed + 500, 3.0, 0.35, age, t);
            if (t[1] < -0.2) {
                continue;
            }
            com.hexhaki.piece.ironfist.IronArt.chunk(sink, x + t[0], ground + 0.2 + t[1], z + t[2], 0.25 + 0.25 * FireMesh.rand(seed, k + 260), t[3],
                seed + k, stay);
        }
    }

    /**
     * Thin translucent rings of shock round (x, y, z) at {@code radius} (the reference's spherical arcs of light round its donut): three
     * great circles at their own tilts, facing about the way (wx, wz).
     */
    static void shockRings(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double radius, double alpha, int seed) {
        if (alpha < 0.02) {
            return;
        }
        for (int k = 0; k < 3; k++) {
            double a = 2 * Math.PI * FireMesh.rand(seed, k + 40);
            double tilt = 0.35 + 0.5 * FireMesh.rand(seed, k + 50);
            double nx = wx + Math.cos(a) * tilt;
            double ny = Math.sin(a) * tilt;
            double nz = wz + Math.sin(a) * tilt * 0.6;
            double nl = Math.max(1.0E-6, Math.sqrt(nx * nx + ny * ny + nz * nz));
            FireMesh.ring(sink, x, y, z, nx / nl, ny / nl, nz / nl, radius * (0.92 + 0.12 * k), 0.035 * radius, 0.0, 0xFFFFFF, alpha * (1.0 - 0.2 * k));
        }
    }

    /** Dough dripping off a donut at (x, y, z) and splatting on the ground ({@code ground} high) under it. */
    static void drops(FireMesh.Sink sink, double x, double y, double z, double sx, double sz, double ground, double form, double age, int seed) {
        if (form < 0.6) {
            return;
        }
        for (int k = 0; k < 3; k++) {
            double side = (FireMesh.rand(seed, k + 70) - 0.5) * LOTUS_DONUT * 1.2;
            double t = (age * 0.09 + k * 0.33) % 1.0;
            double top = y - LOTUS_DONUT * 0.8;
            double h = top - (top - ground) * t * t;
            MochiArt.ball(sink, x + sx * side, h, z + sz * side, 0.11, 0, 1, 0, 1.0 + 1.2 * t, 0.0, age, seed + k, MochiArt.MOCHI);
            // its splat below, flattened, spreading a little
            double spread = 0.35 + 0.15 * k;
            MochiArt.blob(sink, x + sx * side * 1.2, ground + 0.03, z + sz * side * 1.2, 1, 0, 0, 0, 1, 0, spread, 0.04, spread * 0.8, 0.7, 0.3, age,
                seed + 20 + k, MochiArt.MOCHI, 1.0);
        }
    }

    /**
     * 3.6.4: a Lotus blow in the air (the owner: "from donut to donut carrying basically being glued with each punch to the next, fast"):
     * a donut puffing up at (x, y, z) beyond the body, its hole to the way its fist strikes (wx, wy, wz) (unit); its fist punches out
     * onto the body's middle (bx, by, bz) at the blow ({@code age} 0) and stays glued to it as it carries it on, its arm stretching,
     * then snaps back; a white burst and shock rings where it strikes; {@code age} ticks from the blow (from -{@link #LOTUS_AIR_EARLY}).
     */
    public static void lotusChain(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double bx, double by, double bz,
                                  double age, boolean coat, int seed) {
        if (age < -LOTUS_AIR_EARLY || age > LOTUS_AIR_LIFE) {
            return;
        }
        double form = smooth((age + LOTUS_AIR_EARLY) / 3.0) * (1.0 - smooth((age - LOTUS_AIR_LIFE + 4.0) / 4.0));
        // out of the hole onto the body in the tick before the blow, glued to it through the carry, back into the hole after
        double out = age < -1.0 ? 0.0 : age < 0.0 ? ease(age + 1.0) : age < LOTUS_CARRY + 0.5 ? 1.0 : 1.0 - smooth((age - LOTUS_CARRY - 0.5) / 2.5);
        donut(sink, x, y, z, wx, wy, wz, LOTUS_AIR_DONUT, form, 0.0, 0, out > 0.02 ? out : 0.0, x + (bx - x) * out, y + (by - y) * out, z + (bz - z) * out,
            coat, age, seed, false);
        if (age >= 0.0 && age < 7.0) {
            double f = age / 7.0;
            MochiArt.glow(sink, bx, by, bz, 0.9 + 1.0 * ease(f), 0xFFFFFF, 0.85 * (1.0 - f));
            shockRings(sink, bx, by, bz, wx, wz, 0.7 + 1.6 * ease(f), 0.6 * (1.0 - f), seed + 3);
        }
        // white streaks of the body's flight behind it while it is carried
        if (age >= 0.0 && age < LOTUS_CARRY + 1.0) {
            double fade = 1.0 - age / (LOTUS_CARRY + 1.0);
            for (int k = 0; k < 4; k++) {
                double ox = (FireMesh.rand(seed, k + 30) - 0.5) * 0.8;
                double oy = (FireMesh.rand(seed, k + 40) - 0.5) * 1.0;
                FireMesh.streak(sink, bx + ox, by + oy, bz + ox * 0.5, -wx, -wy, -wz, 1.6 + 1.2 * FireMesh.rand(seed, k + 50), 0.04, 0xFFFFFF, 0.7 * fade);
            }
        }
    }

    /**
     * The Lotus slam (the reference's frames 111 to 128): a big donut forming {@code top} over the body (fixed where it formed: (x, top,
     * z)), its giant fist driving down onto the body at (bx, by, bz) as it falls, white streaks of the fall; {@code age} ticks from the
     * drop ({@link #LOTUS_DROP}); {@code ground} the ground's height under it.
     */
    public static void lotusSlam(FireMesh.Sink sink, double x, double top, double z, double bx, double by, double bz, double ground, double age,
                                 boolean coat, int seed) {
        if (age < 0 || age > LOTUS_SLAM_LIFE) {
            return;
        }
        double form = smooth((age + 1.0) / 4.0) * (1.0 - smooth((age - LOTUS_SLAM_LIFE + 6.0) / 6.0));
        double fall = LOTUS_FALL - LOTUS_DROP;
        double hit = LOTUS_GROUND - LOTUS_DROP;
        // the fist: down onto the body's head before the fall, riding it down, buried in the ground at the slam, drawn back after
        double reach;
        double headY = by + 1.9;
        if (age < 4.0) {
            reach = (top - headY) * ease(age / 4.0);
        } else if (age < hit + 1.0) {
            reach = top - Math.max(ground + 0.2, headY);
        } else {
            // the reference shows no arm once the ground is struck: drawn back up fast
            reach = (top - ground - 0.2) * (1.0 - smooth((age - hit - 1.0) / 4.0));
        }
        double end = top - Math.max(0.3, reach);
        donut(sink, x, top, z, 0, -1, 0, LOTUS_SLAM_DONUT, form, 0.0, 2, reach > 0.3 ? 1.0 : 0.0, x + (bx - x) * Math.min(1.0, age / 4.0), end,
            z + (bz - z) * Math.min(1.0, age / 4.0), coat, age, seed, false);
        // the fall's white streaks, up from the body (the reference's frames 126 to 128)
        if (age >= fall && age < hit + 1.0) {
            for (int k = 0; k < 6; k++) {
                double a = 2 * Math.PI * (k + FireMesh.rand(seed, k + 80)) / 6.0;
                double r = 0.5 + 0.4 * FireMesh.rand(seed, k + 90);
                FireMesh.streak(sink, bx + Math.cos(a) * r, by + 2.0, bz + Math.sin(a) * r, 0, 1, 0, 2.5 + 2.0 * FireMesh.rand(seed, k + 100), 0.04,
                    0xFFFFFF, 0.7);
            }
        }
    }

    /**
     * The Lotus slam's ground (the reference's frames 130 to 161): where the body lands (x, y, z) (on the ground): a white flash, a flat
     * splat of dough spreading, rocks of the ground thrown up and falling round it, a white ring rising and widening; {@code age} ticks in.
     */
    public static void lotusGround(FireMesh.Sink sink, double x, double y, double z, double age, int seed) {
        if (age < 0 || age > LOTUS_GROUND_LIFE) {
            return;
        }
        if (age < 5.0) {
            double f = age / 5.0;
            MochiArt.glow(sink, x, y + 0.8, z, 2.6 + 2.0 * f, 0xFFFFFF, 0.95 * (1.0 - f));
        }
        // the splat: flat, wide, lumpy, spreading fast and sinking away at its end
        double spread = ease(age / 4.0);
        double sink0 = 1.0 - smooth((age - LOTUS_GROUND_LIFE + 10.0) / 10.0);
        if (sink0 > 0.02) {
            MochiArt.blob(sink, x, y + 0.08, z, 1, 0, 0, 0, 1, 0, 2.3 * spread, 0.28 * sink0, 2.0 * spread, 0.85, 0.35, age, seed, MochiArt.MOCHI, 1.0);
            for (int k = 0; k < 7; k++) {
                double a = 2 * Math.PI * (k + FireMesh.rand(seed, k)) / 7.0;
                double d = (2.0 + 1.2 * FireMesh.rand(seed, k + 10)) * spread;
                double r = (0.35 + 0.3 * FireMesh.rand(seed, k + 20)) * sink0;
                MochiArt.blob(sink, x + Math.cos(a) * d, y + 0.05, z + Math.sin(a) * d, Math.cos(a), 0, Math.sin(a), 0, 1, 0, r * 1.6, 0.1 * sink0, r, 0.8,
                    0.3, age, seed + k, MochiArt.MOCHI, 1.0);
            }
        }
        // rocks of the ground thrown up round it, tumbling and falling back
        double alpha = Math.min(1.0, (LOTUS_GROUND_LIFE - age) / 8.0);
        double[] t = new double[4];
        for (int k = 0; k < 14; k++) {
            com.hexhaki.piece.ironfist.IronArt.thrown(k, seed + 300, 5.5, 0.45, age, t);
            if (t[1] < -0.2) {
                continue;
            }
            com.hexhaki.piece.ironfist.IronArt.chunk(sink, x + t[0] * 1.0 + Math.cos(k) * 1.6, y + 0.3 + t[1], z + t[2] + Math.sin(k) * 1.6,
                0.35 + 0.45 * FireMesh.rand(seed, k + 240), t[3], seed + k, alpha);
        }
        // the ring rising and widening over it (frames 138 to 146)
        if (age >= 2.0 && age < 18.0) {
            double f = (age - 2.0) / 16.0;
            FireMesh.ring(sink, x, y + 0.4 + 2.2 * ease(f), z, 0, 1, 0, 2.4 + 3.4 * ease(f), 0.18, 0.12, 0xFFFFFF, 0.75 * (1.0 - f));
        }
    }

    // ------------------------------------------------------------------------------------------------------ Chestnut Mochi
    // 3.6.3, after the reference's Chestnut read frame by frame (next-ai-3.6.3.md G): a comet of dough flung straight up out of the
    // hand curls into a swirl high over the user, balls up and flashes (to CHESTNUT_FORM), and a dark spiked disc (Haki black with
    // Armament, white without) hangs there tilted toward the aim, ringed in thin white light, firing a spike down a white tracer at the
    // nearest foe every CHESTNUT_EVERY ticks (a white-blue spark star where it strikes) for CHESTNUT_FIRE ticks; then it turns white,
    // falls flat onto the user and splats into a puddle round their feet (CHESTNUT_FALL).

    /** The forming, the firing, the fall and splat (ticks). */
    public static final double CHESTNUT_FORM = 15.0;
    public static final double CHESTNUT_FIRE = 120.0;
    public static final double CHESTNUT_FALL = 30.0;
    /** How high over the user's head the disc hangs. */
    public static final double CHESTNUT_HIGH = 4.2;

    /**
     * Chestnut Mochi (V): the disc at (x, y, z) over its user, the hand (hx, hy, hz) the comet leaves from, the aim (ax, az) (flat) it
     * tilts toward, {@code ground} the height of the ground under the user; {@code age} ticks from the cast, {@code ended} ticks since it
     * stopped firing (negative: still firing), {@code fired} ticks since its last spike.
     */
    public static void chestnut(FireMesh.Sink sink, double x, double y, double z, double hx, double hy, double hz, double ax, double az,
                                double ground, double age, double ended, double fired, boolean coat, int seed) {
        if (age < 0) {
            return;
        }
        double al = Math.max(1.0E-6, Math.hypot(ax, az));
        ax /= al;
        az /= al;
        double px = -az;
        double pz = ax;
        // ---- the comet up out of the hand (frames 17 to 21), curling into a swirl at the top, balling up (24 to 28)
        // (not on its very first frame: its points all at the hand, a tube with no length)
        if (age < CHESTNUT_FORM - 2.0 && age > 0.2) {
            double fly = ease(age / 5.0);
            double wind = clamp((age - 3.0) / 7.0);
            MochiArt.tubeBegin();
            int n = 20;
            for (int k = 0; k <= n; k++) {
                double t = k / (double) n;
                double u = clamp(fly - (1.0 - t) * 0.5 * (1.0 - wind));
                double cx = hx + (x - hx) * u + px * Math.sin(Math.PI * u) * 0.8;
                double cy = hy + (y - hy) * u;
                double cz = hz + (z - hz) * u + pz * Math.sin(Math.PI * u) * 0.8;
                double turn = 2 * Math.PI * (1.3 * t + 1.4 * wind);
                double r = 1.0 * (1.2 - 0.6 * t) * (1.0 - 0.6 * wind);
                double sx = x + Math.cos(turn) * r * px + Math.sin(turn) * r * ax * 0.4;
                double sy = y + Math.sin(turn) * r * 0.8;
                double sz = z + Math.cos(turn) * r * pz + Math.sin(turn) * r * az * 0.4;
                double curl = smooth((fly - 0.75) / 0.25) * smooth(t * 1.5 + wind);
                MochiArt.tubePoint(cx + (sx - cx) * curl, cy + (sy - cy) * curl, cz + (sz - cz) * curl, (0.03 + 0.22 * Math.pow(t, 1.3)) * (1.0 + wind));
            }
            MochiArt.tube(sink, 10, MochiArt.MOCHI, 0.0, 0.0);
            double ball = smooth((age - 5.0) / 6.0);
            if (ball > 0.02) {
                MochiArt.blob(sink, x, y, z, ax, 0, az, 0, 1, 0, 0.85 * ball, 0.8 * ball, 0.85 * ball, 0.9, 0.2, age, seed, MochiArt.MOCHI, 1.0);
            }
        }
        // ---- the flash out of the ball (frame 32)
        if (age >= CHESTNUT_FORM - 4.0 && age < CHESTNUT_FORM + 5.0) {
            double f = (age - CHESTNUT_FORM + 4.0) / 9.0;
            MochiArt.glow(sink, x, y, z, 1.2 + 1.0 * ease(f), 0xFFFFFF, 0.95 * (1.0 - smooth(f)));
            shockRings(sink, x, y, z, ax, az, 1.0 + 1.6 * ease(f), 0.7 * (1.0 - smooth(f)), seed + 5);
        }
        // ---- the disc (frames 36 to 186) and its end (190 to 212)
        double form = smooth((age - CHESTNUT_FORM + 3.0) / 3.0);
        if (form < 0.02) {
            return;
        }
        boolean over = ended >= 0;
        double white = over ? smooth(ended / 4.0) : 0.0;
        double flat = over ? smooth((ended - 2.0) / 4.0) : 0.0;
        double fall = over ? clamp((ended - 4.0) / 8.0) : 0.0;
        double cy = y - (y - ground - 0.3) * fall * fall;
        if (fall >= 1.0) {
            // ---- splatted round the user's feet: a puddle, droplets, a faint ring; sinking away
            double sp = ended - 12.0;
            double stay = 1.0 - smooth((sp - CHESTNUT_FALL + 22.0) / 10.0);
            if (stay > 0.02) {
                MochiArt.blob(sink, x, ground + 0.05, z, 1, 0, 0, 0, 1, 0, 1.6 * ease(sp / 3.0), 0.12 * stay, 1.4 * ease(sp / 3.0), 0.85, 0.35, age,
                    seed + 7, MochiArt.MOCHI, 1.0);
                for (int k = 0; k < 10; k++) {
                    double a = 2 * Math.PI * (k + FireMesh.rand(seed, k + 400)) / 10.0;
                    double d = (1.6 + 0.9 * FireMesh.rand(seed, k + 410)) * ease(sp / 3.0);
                    MochiArt.blob(sink, x + Math.cos(a) * d, ground + 0.04, z + Math.sin(a) * d, Math.cos(a), 0, Math.sin(a), 0, 1, 0,
                        0.45 * stay, 0.035, 0.1, 0.9, 0.1, age, seed + 420 + k, MochiArt.MOCHI, 1.0);
                }
            }
            splat(sink, x, ground + 0.3, z, 0, 1, 0, 1.1, sp, seed + 9);
            if (sp < 10.0) {
                FireMesh.ring(sink, x, ground + 0.1, z, 0, 1, 0, 1.2 + 1.8 * ease(sp / 10.0), 0.08, 0.05, 0xFFFFFF, 0.5 * (1.0 - sp / 10.0));
            }
            return;
        }
        // its axis: tilted half way between the aim and straight down (the reference's disc, seen tilted), laid flat as it falls
        double tx = ax * 0.7 * (1.0 - flat);
        double ty = -0.7 - 0.3 * flat;
        double tz = az * 0.7 * (1.0 - flat);
        double tl = Math.sqrt(tx * tx + ty * ty + tz * tz);
        tx /= tl;
        ty /= tl;
        tz /= tl;
        double kick = !over && fired < 3 ? 0.12 * (1.0 - fired / 3.0) : 0.0;
        double dx = x - tx * kick;
        double dy = cy - ty * kick;
        double dz = z - tz * kick;
        int[] bands = coat && white < 0.5 ? MochiArt.HAKI : MochiArt.MOCHI;
        double s = springy(form) * form;
        double big = CHESTNUT_DONUT * s;
        double ring = big * (1.0 - DONUT_THICK);
        double thick = big * DONUT_THICK;
        double spin = age * 0.05;
        MochiArt.donut(sink, dx, dy, dz, tx, ty, tz, ring, thick, DONUT_LUMP * 0.6, spin, 0, age, seed, bands);
        // the spikes: all round its rim and out of both its faces, turning with it
        double[] b = B;
        FireMesh.basis(tx, ty, tz, b);
        for (int k = 0; k < CHESTNUT_SPIKES; k++) {
            double a = spin + 2 * Math.PI * k / CHESTNUT_SPIKES;
            double ox = b[0] * Math.cos(a) + b[3] * Math.sin(a);
            double oy = b[1] * Math.cos(a) + b[4] * Math.sin(a);
            double oz = b[2] * Math.cos(a) + b[5] * Math.sin(a);
            double rb = ring + thick * 0.7;
            double len = thick * (1.2 + 0.5 * FireMesh.rand(seed, k)) * s;
            MochiArt.cone(sink, dx + ox * rb, dy + oy * rb, dz + oz * rb, dx + ox * (rb + len), dy + oy * (rb + len), dz + oz * (rb + len), thick * 0.3,
                bands);
            if (k % 2 == 0) {
                for (int f = -1; f <= 1; f += 2) {
                    double fx = dx + ox * ring + tx * thick * 0.6 * f;
                    double fy = dy + oy * ring + ty * thick * 0.6 * f;
                    double fz = dz + oz * ring + tz * thick * 0.6 * f;
                    double l = thick * 0.95 * s;
                    MochiArt.cone(sink, fx, fy, fz, fx + (tx * f + ox * 0.35) * l, fy + (ty * f + oy * 0.35) * l, fz + (tz * f + oz * 0.35) * l,
                        thick * 0.24, bands);
                }
            }
        }
        // the thin white ring of light round it (frames 36 to 50), turning, fainter later
        double glowRing = (0.55 - 0.3 * clamp((age - CHESTNUT_FORM) / 30.0)) * form * (1.0 - white);
        FireMesh.ring(sink, dx, dy, dz, tx, ty, tz, big * (1.45 + 0.08 * Math.sin(age * 0.3)), 0.05, 0.0, 0xFFFFFF, glowRing);
        FireMesh.ring(sink, dx, dy, dz, tx * 0.8 + px * 0.6, ty, tz * 0.8 + pz * 0.6, big * 1.7, 0.035, 0.0, 0xFFFFFF, glowRing * 0.6);
        if (!over && fired < 2.5) {
            MochiArt.glow(sink, dx + tx * big * 0.2, dy + ty * big * 0.2, dz + tz * big * 0.2, 0.6, 0xFFFFFF, 0.5 * (1.0 - fired / 2.5));
        }
    }

    /**
     * Where Chestnut Mochi's disc hangs from its user's feet, facing {@code yawDegrees} (the game's yaw), {@code height} tall: high over
     * their head, a little behind; into out (x, y, z).
     */
    public static double[] chestnutOffset(double yawDegrees, double height, double[] out) {
        double y = Math.toRadians(yawDegrees);
        double fx = -Math.sin(y);
        double fz = Math.cos(y);
        out[0] = -fx * 0.4;
        out[1] = height + CHESTNUT_HIGH;
        out[2] = -fz * 0.4;
        return out;
    }

    /** Chestnut Mochi's disc and its spikes round the rim. */
    public static final double CHESTNUT_DONUT = 1.15;
    static final int CHESTNUT_SPIKES = 14;

    /** A Chestnut spike's flight (ticks), then its spark. */
    public static final double SPIKE_LIFE = 7.0;

    /**
     * A spike shot from (fx, fy, fz) onto (tx, ty, tz) (the reference's frames 46 to 62): a thin white tracer drawn the whole way at
     * once, a small black spike racing down it in two ticks, a white-blue star of sparks where it strikes; {@code age} ticks in.
     */
    public static void spike(FireMesh.Sink sink, double fx, double fy, double fz, double tx, double ty, double tz, double age) {
        if (age < 0 || age > SPIKE_LIFE) {
            return;
        }
        double dx = tx - fx;
        double dy = ty - fy;
        double dz = tz - fz;
        double dl = Math.max(1.0E-6, Math.sqrt(dx * dx + dy * dy + dz * dz));
        double ux = dx / dl;
        double uy = dy / dl;
        double uz = dz / dl;
        // the tracer: the whole way, thin, fading
        if (age < 4.0) {
            FireMesh.ray(sink, fx, fy, fz, ux, uy, uz, dl, 0.03, 0, 0.0, 0xFFFFFF, 0.7 * (1.0 - age / 4.0));
        }
        // the spike racing down it
        double t = Math.min(1.0, age / 2.0);
        if (age < 2.5) {
            double hx = fx + dx * t;
            double hy = fy + dy * t;
            double hz = fz + dz * t;
            double len = 0.5;
            MochiArt.cone(sink, hx - ux * len, hy - uy * len, hz - uz * len, hx, hy, hz, 0.08, MochiArt.HAKI);
        }
        // the strike: a white-blue star of sparks
        if (age >= 1.8) {
            double f = (age - 1.8) / (SPIKE_LIFE - 1.8);
            MochiArt.glow(sink, tx, ty, tz, 0.45 + 0.4 * f, 0xDDEBFF, 0.85 * (1.0 - f));
            for (int k = 0; k < 8; k++) {
                double a = 2 * Math.PI * k / 8.0 + 0.4;
                double up = (k % 3 - 1) * 0.6;
                double ul = Math.sqrt(1.0 + up * up);
                FireMesh.ray(sink, tx, ty, tz, Math.cos(a) / ul, up / ul, Math.sin(a) / ul, 0.5 + 0.6 * ease(f * 2.0), 0.03, 0, 0.0,
                    k % 2 == 0 ? 0xFFFFFF : 0x9CC8FF, 0.9 * (1.0 - f));
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ Mochi Anemone
    // 3.6.3, after the reference's Anemone read frame by frame (next-ai-3.6.3.md G): about twenty small donuts pop up scattered over
    // the area at every height, more stand half sunk in the ground round it like arches; long Haki bars shoot out of them into the area,
    // several at once, a white burst where each leaves its donut and where it lands; a see-through white dome glows over it, swirls of
    // white and pale blue wind round it, rocks lie torn up round a dark crater; let go, a last flash and the dome bursts into splashes,
    // the donuts puff away, and white splats lie over the ground fading (ANEMONE_LINGER).

    /** Mochi Anemone: how many donuts hang over its area (and how many stand in the ground round it), the highest, how long it lingers. */
    public static final int ANEMONE_DONUTS = 20;
    public static final int ANEMONE_ARCHES = 8;
    public static final double ANEMONE_HIGH = 5.5;
    public static final double ANEMONE_LINGER = 50.0;
    /** 3.6.3: the reference's Anemone donuts are small (their outer radius). */
    public static final double ANEMONE_SMALL = 0.6;

    /** Where Anemone donut {@code k} hangs from the area's middle on the ground, {@code radius} round: into out (x, y, z). */
    public static double[] anemoneDonut(int k, double radius, int seed, double[] out) {
        double a = 2 * Math.PI * (k * 0.618 + 0.25 * FireMesh.rand(seed, k));
        double r = radius * (0.35 + 1.25 * FireMesh.rand(seed, k + 20));
        out[0] = Math.cos(a) * r;
        out[1] = 1.4 + (ANEMONE_HIGH - 1.4) * FireMesh.rand(seed, k + 40);
        out[2] = Math.sin(a) * r;
        return out;
    }

    /**
     * Mochi Anemone (B) held: its donuts over the area round (x, y, z) (the ground in its middle), {@code radius} round, {@code form} 0
     * to 1 as it comes; {@code strikes} (donut, x, y, z, the punch's age: 5 a strike) the Haki bars shooting out of them;
     * {@code ended} ticks since it was let go (negative: still held).
     */
    public static void anemoneArea(FireMesh.Sink sink, double x, double y, double z, double radius, double form, double[] strikes, int count,
                                   boolean coat, double age, int seed, double ended) {
        boolean over = ended >= 0;
        double going = over ? smooth(ended / 6.0) : 0.0;
        double[] d = new double[3];
        // its many small donuts drawn coarse (the budget: about 16k quads at most)
        boolean wasLow = MochiArt.low;
        MochiArt.low = true;
        // ---- the donuts in the air, popping up one after another (frames 12 to 18), each facing the area's middle
        for (int k = 0; k < ANEMONE_DONUTS; k++) {
            anemoneDonut(k, radius, seed, d);
            double cx = x + d[0];
            double cy = y + d[1] + 0.15 * Math.sin(age * 0.2 + k);
            double cz = z + d[2];
            double f = smooth((age - k * 0.25) / 3.0) * form * (1.0 - going);
            if (f < 0.02) {
                if (over && ended < 8.0 && smooth((age - k * 0.25) / 3.0) > 0.5) {
                    splat(sink, cx, cy, cz, 0, -1, 0, 0.6, ended * 1.6, seed + k);
                }
                continue;
            }
            double out = 0.0;
            double tx = x;
            double ty = y + 0.4;
            double tz = z;
            double pa = -1.0;
            for (int s = 0; s < count; s++) {
                double sa = strikes[s * 5 + 4];
                if ((int) strikes[s * 5] != k || sa < 0 || sa > ANEMONE_PUNCH) {
                    continue;
                }
                double o = sa < 2.0 ? ease(sa / 2.0) : 1.0 - smooth((sa - 3.5) / (ANEMONE_PUNCH - 3.5));
                if (o > out) {
                    out = o;
                    pa = sa;
                    tx = strikes[s * 5 + 1];
                    ty = strikes[s * 5 + 2];
                    tz = strikes[s * 5 + 3];
                }
            }
            double dx = tx - cx;
            double dy = ty - cy;
            double dz = tz - cz;
            double dl = Math.max(1.0E-6, Math.sqrt(dx * dx + dy * dy + dz * dz));
            double big = ANEMONE_SMALL * (0.8 + 0.4 * FireMesh.rand(seed, k + 60));
            donut(sink, cx, cy, cz, dx / dl, dy / dl, dz / dl, big, f, 0.0, 1, out > 0.02 ? out : 0.0, cx + dx * out, cy + dy * out, cz + dz * out,
                coat, age, seed + k, k % 2 == 1);
            if (pa >= 0 && pa < 2.5) {
                // the white burst as the bar leaves its donut (frame 22)
                MochiArt.glow(sink, cx, cy, cz, big * 1.6, 0xFFFFFF, 0.7 * (1.0 - pa / 2.5));
                FireMesh.ring(sink, cx, cy, cz, dx / dl, dy / dl, dz / dl, big * (1.3 + 0.8 * pa / 2.5), 0.05, 0.0, 0xFFFFFF, 0.6 * (1.0 - pa / 2.5));
            }
            if (pa >= 1.5 && pa < 5.0) {
                // and where it lands
                double g = (pa - 1.5) / 3.5;
                MochiArt.glow(sink, tx, ty, tz, 0.6 + 0.8 * g, 0xFFFFFF, 0.75 * (1.0 - g));
            }
        }
        // ---- the donuts standing half sunk in the ground round the area, like arches (frame 14 on)
        for (int k = 0; k < ANEMONE_ARCHES; k++) {
            double a = 2 * Math.PI * (k + 0.4 * FireMesh.rand(seed, k + 80)) / ANEMONE_ARCHES;
            double r = radius * (1.25 + 0.35 * FireMesh.rand(seed, k + 90));
            double ax = Math.cos(a);
            double az = Math.sin(a);
            double f = smooth((age - 2.0 - k * 0.3) / 3.0) * form * (1.0 - going);
            if (f > 0.02) {
                donut(sink, x + ax * r, y + 0.05, z + az * r, -az, 0, ax, 0.55, f, 0.0, 0, 0.0, 0, 0, 0, coat, age, seed + 100 + k, false);
            }
        }
        MochiArt.low = wasLow;
        // toward the eye (eye-relative points): the dome's rim faces the one looking
        double el = Math.max(1.0E-6, Math.sqrt(x * x + (y + 1.0) * (y + 1.0) + z * z));
        double nx = -x / el;
        double ny = -(y + 1.0) / el;
        double nz = -z / el;
        // ---- the see-through white dome over it (frames 30 to 196), brighter as the bars strike, its last flash at the end
        double dome = smooth((age - 6.0) / 6.0) * form * (over ? 1.0 - smooth((ended - 2.0) / 5.0) : 1.0);
        int live = 0;
        for (int s = 0; s < count; s++) {
            if (strikes[s * 5 + 4] >= 1.5 && strikes[s * 5 + 4] < 4.0) {
                live++;
            }
        }
        double flash = over ? Math.max(0.0, 1.0 - ended / 4.0) : 0.0;
        if (dome > 0.02) {
            double r = radius * 1.05;
            // the reference's dome glows bright white (frames 30 to 196)
            MochiArt.glow(sink, x, y + 0.4, z, r * (1.1 + 0.15 * flash), 0xFFFFFF, Math.min(0.9, (0.5 + 0.07 * live + 0.4 * flash) * dome));
            MochiArt.glow(sink, x, y + 0.6, z, r * 0.6, 0xFFFFFF, Math.min(0.9, (0.35 + 0.08 * live) * dome));
            FireMesh.ring(sink, x, y + 0.2, z, nx, ny, nz, r, 0.12, 0.0, 0xFFFFFF, 0.55 * dome);
            FireMesh.ring(sink, x, y + 0.2, z, nx, ny, nz, r * 0.82, 0.06, 0.0, 0xFFFFFF, 0.3 * dome);
            // swirls of wind round it, white low and pale blue high (frames 30 to 196)
            for (int k = 0; k < 3; k++) {
                double spin = age * (0.05 + 0.02 * k) + k * 2.1;
                double tilt = 0.5 + 0.25 * k;
                double ux = Math.cos(spin) * tilt;
                double uz = Math.sin(spin) * tilt;
                double ul = Math.sqrt(ux * ux + 1.0 + uz * uz);
                FireMesh.ring(sink, x, y + 1.0 + 0.6 * k, z, ux / ul, 1.0 / ul, uz / ul, radius * (1.15 + 0.2 * k), 0.07, 0.0, 0xFFFFFF, 0.28 * dome);
            }
            for (int k = 0; k < 2; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 140) + age * 0.04;
                FireMesh.ring(sink, x + Math.cos(a) * radius * 0.6, y + ANEMONE_HIGH + 1.0, z + Math.sin(a) * radius * 0.6, Math.cos(a) * 0.4, 1.0,
                    Math.sin(a) * 0.4, radius * 0.9, 0.08, 0.0, 0x9FE9FF, 0.3 * dome);
            }
        }
        // ---- the ground torn: a dark crater in the middle, rocks round it (frames 48 on)
        double torn = smooth((age - 8.0) / 8.0) * (over ? 1.0 - smooth((ended - ANEMONE_LINGER + 10.0) / 10.0) : 1.0);
        if (torn > 0.02) {
            MochiArt.blob(sink, x, y + 0.02, z, 1, 0, 0, 0, 1, 0, radius * 0.4 * torn, 0.015, radius * 0.34 * torn, 0.8, 0.3, 0, seed + 160, MochiArt.HAKI,
                1.0);
            for (int k = 0; k < 10; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 170);
                double r = radius * (0.45 + 0.8 * FireMesh.rand(seed, k + 180));
                com.hexhaki.piece.ironfist.IronArt.chunk(sink, x + Math.cos(a) * r, y + 0.12, z + Math.sin(a) * r, 0.25 + 0.25 * FireMesh.rand(seed, k + 190),
                    k * 1.7, seed + 200 + k, torn);
            }
        }
        // ---- let go: the dome bursts into splashes (frame 204), then white splats lie over the ground fading (to 270)
        if (over) {
            if (ended < 12.0) {
                for (int k = 0; k < 5; k++) {
                    double a = 2 * Math.PI * k / 5.0 + 0.3;
                    splat(sink, x + Math.cos(a) * radius * 0.5, y + 0.6, z + Math.sin(a) * radius * 0.5, Math.cos(a), 0.6, Math.sin(a), 1.2, ended * 1.2,
                        seed + 220 + k);
                }
            }
            double lie = smooth(ended / 4.0) * (1.0 - smooth((ended - ANEMONE_LINGER + 18.0) / 18.0));
            if (lie > 0.02) {
                for (int k = 0; k < 14; k++) {
                    double a = 2 * Math.PI * FireMesh.rand(seed, k + 240);
                    double r = radius * 1.6 * Math.sqrt(FireMesh.rand(seed, k + 250));
                    double sp = (0.3 + 0.5 * FireMesh.rand(seed, k + 260)) * lie;
                    MochiArt.blob(sink, x + Math.cos(a) * r, y + 0.03, z + Math.sin(a) * r, Math.cos(a), 0, Math.sin(a), 0, 1, 0, sp * 1.6, 0.03, sp,
                        0.85, 0.3, 0, seed + 270 + k, MochiArt.MOCHI, 1.0);
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ Buzz Cut Mochi
    // 3.6.3, after the reference's Buzzcut read frame by frame (next-ai-3.6.3.md G) and the owner's own cinematic (next-ai-3.2.2.md 8zzv
    // 10; the owner chose the GIF's horizontal whirl: next-ai-3.6.3.md F 8): the user becomes an upright wheel of white dough rolling
    // where they look (a double row of the ground torn up behind it); meeting a foe it unrolls fluidly into the user, whose right forearm
    // swells into a long spiked mace of glossy Haki black flickering red; a heavy horizontal swing sticks whoever it meets to it (anyone
    // near too), they are whirled round the user (a white vortex of motion on the ground), lifted overhead, a beat, slammed down under it
    // into a black star of a crater; the arm shrinks back. Ticks from the catch: BUZZ_*.

    /** The wheel's radius, the longest roll (ticks), its top speed (blocks a tick). */
    public static final double BUZZ_WHEEL = 1.3;
    public static final int BUZZ_ROLL = 60;
    public static final double BUZZ_SPEED = 0.95;
    /** From the catch: unrolled, the swing's blow, the whirl's end, overhead, the slam begins, the ground, the arm back, all done. */
    public static final double BUZZ_UNROLL = 4.0;
    public static final int BUZZ_HIT = 7;
    public static final double BUZZ_WHIRL = 52.0;
    public static final double BUZZ_RAISED = 60.0;
    public static final double BUZZ_DOWN = 63.0;
    public static final int BUZZ_SLAM = 66;
    public static final double BUZZ_BACK = 76.0;
    public static final double BUZZ_END = 86.0;
    public static final double BUZZ_CRATER_LIFE = 110.0;
    /** The whirl's turns, the mace's reach: unrolled, whirling at its longest. */
    public static final double BUZZ_TURNS = 5.0;
    public static final double BUZZ_REACH = 3.4;
    public static final double BUZZ_REACH_MOST = 6.5;
    /** 3.6.3 (the owner: "the arm increases to insane ammounts and then slams into the ground"): raised overhead, then slammed this far. */
    public static final double BUZZ_REACH_UP = 16.0;
    public static final double BUZZ_REACH_SLAM = 13.0;
    /** How long a slab torn up behind the wheel lies (ticks), and how far apart they are torn along its way (blocks). */
    public static final double BUZZ_TILES = 100.0;
    public static final double BUZZ_TILE_GAP = 0.6;
    /** The most points of the wheel's way kept (its longest roll at its top speed, a slab pair each). */
    public static final int BUZZ_PATH_MOST = 112;

    /** The mace arm's swing round the user, radians from the way they faced when they caught (counter-clockwise seen from above). */
    public static double buzzTurn(double t) {
        if (t < BUZZ_UNROLL) {
            return -1.75;
        }
        if (t < BUZZ_HIT) {
            // the heavy swing: from back over the right shoulder round into the one caught ahead
            double u = (t - BUZZ_UNROLL) / (BUZZ_HIT - BUZZ_UNROLL);
            return -1.75 * (1.0 - u * u);
        }
        if (t < BUZZ_WHIRL) {
            // whirled round BUZZ_TURNS times, speeding up over its first third, ending facing the way it began
            double u = (t - BUZZ_HIT) / (BUZZ_WHIRL - BUZZ_HIT);
            double k = 0.3;
            double g = u < k ? u * u / (2 * k) : k / 2 + (u - k);
            return 2 * Math.PI * BUZZ_TURNS * g / (1.0 - k / 2);
        }
        return 2 * Math.PI * BUZZ_TURNS;
    }

    /**
     * The mace arm's lift (radians over level): level in the whirl, raised straight up overhead, a beat, slammed down until its ball
     * lies on the ground {@link #BUZZ_REACH_SLAM} ahead (the shoulder 1.4 over it).
     */
    public static double buzzLift(double t) {
        // its giant ball half buried in the ground at the slam (the shoulder 1.4 over the ground)
        double down = Math.asin(Math.max(-0.9, Math.min(0.9, (buzzBall(BUZZ_SLAM) * 0.5 - 1.4) / BUZZ_REACH_SLAM)));
        if (t < BUZZ_WHIRL) {
            // high enough that the giant ball clears the ground as it whirls
            return t < BUZZ_HIT ? 0.12 : 0.3;
        }
        if (t < BUZZ_RAISED) {
            return 0.3 + (1.62 - 0.3) * smooth((t - BUZZ_WHIRL) / (BUZZ_RAISED - BUZZ_WHIRL));
        }
        if (t < BUZZ_DOWN) {
            return 1.62 + 0.03 * Math.sin((t - BUZZ_RAISED) * 3.0);
        }
        if (t < BUZZ_SLAM) {
            double u = (t - BUZZ_DOWN) / (BUZZ_SLAM - BUZZ_DOWN);
            return 1.62 - (1.62 - down) * u * u;
        }
        return down;
    }

    /**
     * The mace arm's reach from the shoulder: out of the hand as it unrolls, stretching in the whirl, shot up to an insane length as it is
     * raised ({@link #BUZZ_REACH_UP}), slammed down {@link #BUZZ_REACH_SLAM} ahead, drawn back at the end.
     */
    public static double buzzReach(double t) {
        if (t < BUZZ_UNROLL + 2) {
            return 0.7 + (BUZZ_REACH - 0.7) * ease(t / (BUZZ_UNROLL + 2));
        }
        double r;
        if (t < BUZZ_WHIRL) {
            r = BUZZ_REACH + (BUZZ_REACH_MOST - BUZZ_REACH) * clamp((t - BUZZ_HIT) / (BUZZ_WHIRL - BUZZ_HIT));
        } else if (t < BUZZ_DOWN) {
            r = BUZZ_REACH_MOST + (BUZZ_REACH_UP - BUZZ_REACH_MOST) * ease((t - BUZZ_WHIRL) / (BUZZ_RAISED - BUZZ_WHIRL));
        } else if (t < BUZZ_SLAM) {
            r = BUZZ_REACH_UP + (BUZZ_REACH_SLAM - BUZZ_REACH_UP) * (t - BUZZ_DOWN) / (BUZZ_SLAM - BUZZ_DOWN);
        } else {
            r = BUZZ_REACH_SLAM;
        }
        return 0.7 + (r - 0.7) * (1.0 - smooth((t - BUZZ_BACK) / (BUZZ_END - BUZZ_BACK)));
    }

    /**
     * 3.6.3 (the owner's clip from the show: the mace ball ENORMOUS, the one caught a tiny figure stuck among its spikes): the spiked
     * ball's radius {@code t} ticks after the catch: growing with the arm's reach to {@link #BUZZ_BALL} at the top of the raise.
     */
    public static double buzzBall(double t) {
        return 0.25 + (BUZZ_BALL - 0.25) * buzzSwell(t) * Math.min(1.0, 0.45 + buzzReach(t) * 0.06);
    }

    /**
     * Where the {@code k}-th one caught by Buzz Cut Mochi has their feet {@code t} ticks after the catch, for a user standing at (fx, fy,
     * fz) who faced {@code yaw0} (the game's yaw): stuck on the giant ball's surface (on its top as it whirls and is raised, side by
     * side), crushed under it on the ground from the slam (the owner: "The opponent must be underneath the forearm when it connects with
     * the ground"); into out (x, y, z).
     */
    public static double[] buzzStuck(double t, int k, double fx, double fy, double fz, double yaw0, double[] out) {
        double[] arm = buzzArm(Math.min(t, BUZZ_SLAM), fx, fy, fz, yaw0, new double[8]);
        double ball = buzzBall(Math.min(t, BUZZ_SLAM));
        double cx = arm[0] + arm[3] * arm[6];
        double cy = arm[1] + arm[4] * arm[6];
        double cz = arm[2] + arm[5] * arm[6];
        // across the arm, level: (-dz, dx)
        double ax = -arm[5];
        double az = arm[3];
        double al = Math.max(1.0E-6, Math.hypot(ax, az));
        double side = (k % 2 == 0 ? 1 : -1) * ((k + 1) / 2) * 0.55 * ball / Math.max(1, k + 1) * Math.min(1, k);
        if (t >= BUZZ_SLAM) {
            out[0] = cx + ax / al * side;
            out[1] = fy;
            out[2] = cz + az / al * side;
            return out;
        }
        out[0] = cx + ax / al * side;
        out[1] = cy + ball * 0.75;
        out[2] = cz + az / al * side;
        return out;
    }

    /** 3.6.3: the spiked ball's biggest radius. */
    public static final double BUZZ_BALL = 2.5;

    /** How swollen the forearm is (0 an arm, 1 the mace): swelling as it unrolls, shrinking back at the end. */
    public static double buzzSwell(double t) {
        return smooth(t / (BUZZ_UNROLL + 2)) * (1.0 - smooth((t - BUZZ_BACK) / (BUZZ_END - BUZZ_BACK)));
    }

    /**
     * Where the mace arm's shoulder is and which way it reaches {@code t} ticks after the catch, for a user standing at (fx, fy, fz) who
     * faced {@code yaw0} degrees (the game's yaw) when they caught: into out (shoulder x, y, z, the arm's way x, y, z (unit), its reach,
     * the user's own yaw now in degrees).
     */
    public static double[] buzzArm(double t, double fx, double fy, double fz, double yaw0, double[] out) {
        double yaw = yaw0 - Math.toDegrees(buzzTurn(t));
        double r = Math.toRadians(yaw);
        double fwx = -Math.sin(r);
        double fwz = Math.cos(r);
        // the right of the user: (-cos, -sin) of the game's yaw
        double rx = -Math.cos(r);
        double rz = -Math.sin(r);
        double lift = buzzLift(t);
        out[0] = fx + rx * 0.36;
        out[1] = fy + 1.4;
        out[2] = fz + rz * 0.36;
        out[3] = fwx * Math.cos(lift);
        out[4] = Math.sin(lift);
        out[5] = fwz * Math.cos(lift);
        out[6] = buzzReach(t);
        out[7] = yaw;
        return out;
    }

    /**
     * How the mace's elastic band bows {@code t} ticks after the catch: into out (the way it bows x, y, z (unit), how far in blocks).
     * 3.6.4 (the owner: "the end of the arm is heavier so ... the heavy end should always be after the mochi elastic part"): the band
     * leads the motion and the heavy ball trails after it, so it bows toward where the arm is going (the faster and longer, the more),
     * and it always sags a little under the ball's weight; never back against its swing (3.6.3 bowed it backward: an arch over the
     * slam where the owner drew it hanging under).
     */
    public static double[] buzzBend(double t, double yaw0, double[] out) {
        double[] now = buzzArm(t, 0, 0, 0, yaw0, BEND_NOW);
        double[] before = buzzArm(Math.max(0.0, t - 1.5), 0, 0, 0, yaw0, BEND_BEFORE);
        double mx = now[3] - before[3];
        double my = now[4] - before[4];
        double mz = now[5] - before[5];
        double ml = Math.sqrt(mx * mx + my * my + mz * mz);
        double lead = Math.min(3.0, ml * now[6] * 0.9);
        double sag = BUZZ_SAG * now[6];
        double vx = (ml > 1.0E-6 ? mx / ml * lead : 0.0);
        double vy = (ml > 1.0E-6 ? my / ml * lead : 0.0) - sag;
        double vz = (ml > 1.0E-6 ? mz / ml * lead : 0.0);
        // only across the arm (along it, a bow would only slide its points)
        double d = vx * now[3] + vy * now[4] + vz * now[5];
        vx -= d * now[3];
        vy -= d * now[4];
        vz -= d * now[5];
        double l = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (l < 1.0E-6) {
            out[0] = 0;
            out[1] = -1;
            out[2] = 0;
            out[3] = 0.0;
            return out;
        }
        out[0] = vx / l;
        out[1] = vy / l;
        out[2] = vz / l;
        // its middle kept over the ground (the shoulder 1.4 over the feet: buzzArm)
        double middle = 1.4 + now[4] * now[6] * 0.5 - 0.35;
        out[3] = out[1] < -1.0E-6 ? Math.min(l, Math.max(0.0, middle) / -out[1]) : l;
        return out;
    }

    /** 3.6.4: how far the band sags under the ball's weight, a share of its length. */
    public static final double BUZZ_SAG = 0.07;

    /** buzzBend's scratch (the render thread only). */
    private static final double[] BEND_NOW = new double[8];
    private static final double[] BEND_BEFORE = new double[8];

    /**
     * The wheel the user has become, its middle at (x, y, z) rolling along (wx, wz) (flat, unit), {@code rolled} blocks so far at
     * {@code speed} (0 to 1 of its top), {@code form} (0 to 1: forming out of the body, or unrolling back into it); {@code age} ticks.
     */
    public static void buzzWheel(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double rolled, double speed, double form,
                                 double age, int seed) {
        if (form < 0.02) {
            return;
        }
        double ax = -wz;
        double az = wx;
        double r = BUZZ_WHEEL;
        // pressed against the ground as it rolls (the owner: "slightly deform and compress against the ground")
        double press = 0.07 * speed * form;
        donut(sink, x, y - press, z, ax, 0, az, r, form, -rolled / r, 0, 0.0, 0, 0, 0, false, age, seed, false);
        MochiArt.blob(sink, x, y - r * form + 0.12, z, wx, 0, wz, 0, 1, 0, r * 0.5 * form, 0.14 * form, r * 0.32 * form, 0.8, 0.2, age, seed + 1,
            MochiArt.MOCHI, 1.0);
        if (speed < 0.05) {
            return;
        }
        // the reference's wheel at speed is a bright white blur: a glow over it and blurred copies of its ring trailing behind
        MochiArt.glow(sink, x, y, z, r * 1.2, 0xFFFFFF, 0.6 * speed * form);
        for (int k = 1; k <= 2; k++) {
            FireMesh.ring(sink, x - wx * 0.35 * k * speed, y, z - wz * 0.35 * k * speed, ax, 0, az, r * (1.0 - DONUT_THICK), r * DONUT_THICK * 1.8, 0.0,
                0xFFFFFF, (0.45 - 0.15 * k) * speed * form);
        }
        // white arcs of motion round its rim, trailing the turn (the reference's motion blur)
        for (int k = 0; k < 3; k++) {
            arc(sink, x, y, z, ax, 0, az, wx, 0, wz, r * (1.02 + 0.08 * k), -rolled / r - 0.6 - k * 0.9, 1.6 + 0.5 * speed, 0.07, 0xFFFFFF,
                0.55 * speed * form);
        }
        // black lines of wind streaking past it (frames 20 to 80)
        for (int k = 0; k < 4; k++) {
            double h = (FireMesh.rand(seed + (int) age, k) - 0.3) * r * 1.6;
            double side = (FireMesh.rand(seed + (int) age, k + 9) - 0.5) * r * 1.4;
            FireMesh.streak(sink, x - wx * r * 0.5 + ax * side, y + h, z - wz * r * 0.5 + az * side, -wx, 0, -wz, 1.2 + 1.8 * speed, 0.025, 0x141414,
                0.6 * speed);
        }
        // white droplets of dough flung off its rim backward and up, again and again (frames 55 to 70: white ovals flying off it), and long
        // white slashes of wind past it
        for (int k = 0; k < 5; k++) {
            double clockK = age + k * 2.3;
            double ph = (clockK % 6.0) / 6.0;
            int s = seed + 7 * (int) (clockK / 6.0) + 31 * k;
            double up = 0.5 + 0.9 * FireMesh.rand(s, 1);
            double side = (FireMesh.rand(s, 2) - 0.5) * 1.8;
            double back = 0.8 + 1.8 * FireMesh.rand(s, 3);
            double vx = -wx * back + ax * side;
            double vy = up;
            double vz = -wz * back + az * side;
            double px = x - wx * r * 0.4 + vx * ph * 2.2;
            double py = y + r * 0.6 + vy * ph * 2.2 - 2.4 * ph * ph;
            double pz = z - wz * r * 0.4 + vz * ph * 2.2;
            MochiArt.ball(sink, px, py, pz, (0.1 + 0.07 * FireMesh.rand(s, 4)) * (1.0 - 0.5 * ph) * speed * form, vx, vy - 4.8 * ph, vz, 1.9, 0.0, age,
                s, MochiArt.MOCHI);
        }
        for (int k = 0; k < 3; k++) {
            double h = (FireMesh.rand(seed + (int) age, k + 20) - 0.2) * r * 1.4;
            double side = (FireMesh.rand(seed + (int) age, k + 29) - 0.5) * r * 2.4;
            FireMesh.streak(sink, x + wx * r * 0.3 + ax * side, y + h, z + wz * r * 0.3 + az * side, -wx, 0.15, -wz, 2.2 + 1.6 * speed, 0.05, 0xFFFFFF,
                0.75 * speed * form);
        }
        // white smoke puffed behind it
        for (int k = 0; k < 3; k++) {
            double d = r * (0.6 + 0.8 * k);
            FireMesh.shell(sink, x - wx * d, y - r + 0.35 + 0.2 * k, z - wz * d, r * (0.35 + 0.18 * k) * speed, 0xEDEAE4, (0.4 - 0.1 * k) * speed, 0.0);
        }
    }

    /**
     * An arc of light round (cx, cy, cz) in the plane across (nx, ny, nz) (unit), from (ux, uy, uz) (unit, in the plane) at {@code from}
     * radians on for {@code sweep}, {@code radius} round, {@code width} wide, fading to nothing at its start.
     */
    static void arc(FireMesh.Sink sink, double cx, double cy, double cz, double nx, double ny, double nz, double ux, double uy, double uz,
                    double radius, double from, double sweep, double width, int rgb, double alpha) {
        if (alpha < 0.01 || sweep <= 0.0) {
            return;
        }
        // v = n x u
        double vx = ny * uz - nz * uy;
        double vy = nz * ux - nx * uz;
        double vz = nx * uy - ny * ux;
        int n = Math.max(4, (int) (sweep * 6.0));
        for (int k = 0; k < n; k++) {
            double[] q = new double[12];
            for (int e = 0; e < 2; e++) {
                double a = from + sweep * (k + e) / n;
                double ca = Math.cos(a);
                double sa = Math.sin(a);
                double ox = ux * ca + vx * sa;
                double oy = uy * ca + vy * sa;
                double oz = uz * ca + vz * sa;
                double ri = radius - width * 0.5;
                double ro = radius + width * 0.5;
                int i0 = e == 0 ? 0 : 3;
                int i1 = e == 0 ? 1 : 2;
                q[i0 * 3] = cx + ox * ri;
                q[i0 * 3 + 1] = cy + oy * ri;
                q[i0 * 3 + 2] = cz + oz * ri;
                q[i1 * 3] = cx + ox * ro;
                q[i1 * 3 + 1] = cy + oy * ro;
                q[i1 * 3 + 2] = cz + oz * ro;
            }
            int c0 = FireMesh.argb(alpha * k / n, rgb);
            int c1 = FireMesh.argb(alpha * (k + 1) / n, rgb);
            sink.quad(q, new int[] {c0, c0, c1, c1});
        }
    }

    /** The torn slabs' colours (sampled from the reference's frames 55 to 70: a pale khaki, lit and shaded). */
    static final int[] SLAB = {0xC9C79F, 0xB9B98C, 0xD3CFA6, 0xAFAE84};

    /**
     * The ground torn up behind the wheel (frames 20 to 140): at each point of its way (x, y, z triples in {@code path}, {@code count}
     * of them, {@link #BUZZ_TILE_GAP} apart, the ground there), a square slab of the ground either side flipped up as it passes and
     * dropped back tipped at its own angle: the reference's long winding DOUBLE ROW of pale khaki tiles lying close together, side by
     * side, for seconds; {@code ages} ticks since the wheel passed each.
     */
    public static void buzzTiles(FireMesh.Sink sink, double[] path, double[] ages, int count, int seed) {
        for (int i = 0; i < count; i++) {
            double age = ages[i];
            if (age < 0 || age > BUZZ_TILES) {
                continue;
            }
            int a = Math.max(0, i - 1);
            int b = Math.min(count - 1, i + 1);
            double dx = path[b * 3] - path[a * 3];
            double dz = path[b * 3 + 2] - path[a * 3 + 2];
            double dl = Math.max(1.0E-6, Math.hypot(dx, dz));
            double ux = dx / dl;
            double uz = dz / dl;
            double sx = -uz;
            double sz = ux;
            // sinking back into the ground at the end rather than fading out
            double sink0 = smooth((age - BUZZ_TILES + 18.0) / 18.0);
            for (int side = -1; side <= 1; side += 2) {
                int key = i * 2 + (side + 1) / 2;
                double r1 = FireMesh.rand(seed, key + 5);
                double r2 = FireMesh.rand(seed, key + 40);
                double r3 = FireMesh.rand(seed, key + 90);
                double size = 0.22 + 0.08 * r1;
                // thrown up out of the ground as the wheel passes, falling back by 6 ticks
                double hop = age < 6.0 ? Math.sin(Math.PI * age / 6.0) * (0.35 + 0.3 * r2) : 0.0;
                double off = 0.62 + 0.22 * r2;
                // tipped up on its outer edge (some almost upright), turned a little about the up; tumbling while in the air
                double tip = age < 6.0 ? (0.4 + 0.9 * r3) * Math.min(1.0, age / 2.0) + 0.6 * Math.sin(age * 1.3) * (1.0 - age / 6.0)
                    : 0.4 + 0.9 * r3;
                double turn = (r1 - 0.5) * 1.4;
                double tx = ux * Math.cos(turn) + sx * Math.sin(turn);
                double tz = uz * Math.cos(turn) + sz * Math.sin(turn);
                // across the slab, level: (-tz, tx) toward the outside
                double ox = -tz * side;
                double oz = tx * side;
                double ct = Math.cos(tip);
                double st = Math.sin(tip);
                double nx = ox * st;
                double ny = ct;
                double nz = oz * st;
                // resting on its lower edge: its middle as high as half its tipped width, plus the hop, less what has sunk
                double rest = size * st + 0.06 * ct;
                double y = path[i * 3 + 1] + rest + hop - sink0 * (rest + 0.2);
                int rgb = SLAB[(int) (r3 * SLAB.length) % SLAB.length];
                plank(sink, path[i * 3] + sx * side * off, y, path[i * 3 + 2] + sz * side * off, tx, 0, tz, nx, ny, nz, size, size, 0.06, rgb,
                    1.0);
            }
        }
    }

    /**
     * A flat plank of the ground (a box): its middle (x, y, z), its length along (ux, uy, uz) and its face toward (nx, ny, nz) (both
     * unit, square to each other), {@code length} and {@code width} half sizes, {@code thick} half thick; its top {@code rgb}, its sides
     * darker, lit from above.
     */
    static void plank(FireMesh.Sink sink, double x, double y, double z, double ux, double uy, double uz, double nx, double ny, double nz,
                      double length, double width, double thick, int rgb, double alpha) {
        // w = n x u (across)
        double wx = ny * uz - nz * uy;
        double wy = nz * ux - nx * uz;
        double wz = nx * uy - ny * ux;
        double[][] axes = {{ux, uy, uz, length}, {wx, wy, wz, width}, {nx, ny, nz, thick}};
        for (int a = 0; a < 3; a++) {
            for (int sgn = -1; sgn <= 1; sgn += 2) {
                double[] f = axes[a];
                double[] p = axes[(a + 1) % 3];
                double[] q = axes[(a + 2) % 3];
                double cx = x + f[0] * f[3] * sgn;
                double cy = y + f[1] * f[3] * sgn;
                double cz = z + f[2] * f[3] * sgn;
                double light = Math.max(0.35, 0.55 + 0.45 * f[1] * sgn);
                int c = FireMesh.argb(alpha, FireMesh.mix(0x2A2218, rgb, light));
                double[] v = new double[12];
                double[][] corner = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
                for (int k = 0; k < 4; k++) {
                    double a1 = corner[k][0] * p[3];
                    double a2 = corner[k][1] * q[3];
                    v[k * 3] = cx + p[0] * a1 + q[0] * a2;
                    v[k * 3 + 1] = cy + p[1] * a1 + q[1] * a2;
                    v[k * 3 + 2] = cz + p[2] * a1 + q[2] * a2;
                }
                sink.quad(v, new int[] {c, c, c, c});
            }
        }
    }

    /**
     * The user's right arm become the mace (the owner's anime frame: the arm a long thin WHITE band of stretched mochi, bowed like
     * elastic, flaring at its end into a splash of dough that holds a dark spiked ball; and "coated in glossy, jet-black Armament Haki,
     * with deep crimson highlights and violent red Haki energy flickering across its surface"): from the shoulder (sx, sy, sz) along
     * (dx, dy, dz) (unit) {@code reach} long, bowed {@code bow} blocks toward (bx, by, bz) (unit: the way it lags behind its swing),
     * {@code swell} 0 (an arm) to 1; {@code age} ticks.
     */
    public static void buzzMace(FireMesh.Sink sink, double sx, double sy, double sz, double dx, double dy, double dz, double reach, double swell,
                                double bx, double by, double bz, double bow, double age, int seed) {
        if (reach < 0.1) {
            return;
        }
        // the band: thin white dough, a little thicker at the shoulder, bowed in the middle, flaring into a splash round the ball (the
        // ball enormous: buzzBall, from the reach and the swell)
        double ball = 0.25 + (BUZZ_BALL - 0.25) * swell * Math.min(1.0, 0.45 + reach * 0.06);
        double end = Math.max(0.2, reach - ball * 0.6);
        // 3.6.5 (the owner: "more stretchy, more animated ... the arms should have more elasticity"): a wave of wobble runs along the
        // band across its bow, and it thins in its middle the longer it is drawn out
        double[] wb = new double[6];
        FireMesh.basis(dx, dy, dz, wb);
        double wave = Math.min(0.5, 0.05 + 0.03 * reach);
        double thin = 0.3 * clamp((reach - 2.0) / 10.0);
        MochiArt.tubeBegin();
        int n = Math.min(16, 6 + (int) (reach * 0.8));
        for (int k = 0; k <= n; k++) {
            double t = k / (double) n;
            double along = end * t;
            double sag = bow * 4.0 * t * (1.0 - t);
            double w = wave * Math.sin(Math.PI * t) * Math.sin(2 * Math.PI * 1.2 * t - age * 1.0 + seed);
            double r = (0.19 - 0.06 * t) * (1.0 - thin * Math.sin(Math.PI * t)) + (0.1 + 0.5 * swell) * Math.pow(t, 6.0);
            MochiArt.tubePoint(sx + dx * along + bx * sag + wb[3] * w, sy + dy * along + by * sag + wb[4] * w, sz + dz * along + bz * sag + wb[5] * w, r);
        }
        MochiArt.tube(sink, 10, MochiArt.MOCHI, 0.0, 0.0);
        double cx = sx + dx * reach;
        double cy = sy + dy * reach;
        double cz = sz + dz * reach;
        // the splash of dough cupping the ball (white tongues reaching round it)
        double[] b = new double[6];
        FireMesh.basis(dx, dy, dz, b);
        for (int k = 0; k < 5 && swell > 0.1; k++) {
            double a = 2 * Math.PI * (k + 0.3 * FireMesh.rand(seed, k + 50)) / 5.0;
            double ox = b[0] * Math.cos(a) + b[3] * Math.sin(a);
            double oy = b[1] * Math.cos(a) + b[4] * Math.sin(a);
            double oz = b[2] * Math.cos(a) + b[5] * Math.sin(a);
            double px = cx - dx * ball * 0.75 + ox * ball * 0.55;
            double py = cy - dy * ball * 0.75 + oy * ball * 0.55;
            double pz = cz - dz * ball * 0.75 + oz * ball * 0.55;
            MochiArt.cone(sink, cx - dx * ball * 1.2, cy - dy * ball * 1.2, cz - dz * ball * 1.2, px + dx * ball * 0.4, py + dy * ball * 0.4,
                pz + dz * ball * 0.4, ball * 0.22 * swell, MochiArt.MOCHI);
        }
        // the ball: glossy Haki black lit crimson, spikes all round it (shrinking away into the arm as it contracts back)
        if (swell < 0.1) {
            return;
        }
        ball *= smooth((swell - 0.1) / 0.4);
        MochiArt.ball(sink, cx, cy, cz, ball, dx, dy, dz, 1.0, 0.04, age, seed, MochiArt.HAKI_CRIMSON);
        for (int k = 0; k < SPIKES.length / 3; k++) {
            double ox = SPIKES[k * 3];
            double oy = SPIKES[k * 3 + 1];
            double oz = SPIKES[k * 3 + 2];
            MochiArt.cone(sink, cx + ox * ball * 0.85, cy + oy * ball * 0.85, cz + oz * ball * 0.85, cx + ox * ball * 1.6, cy + oy * ball * 1.6,
                cz + oz * ball * 1.6, ball * 0.22, MochiArt.HAKI_CRIMSON);
        }
        // crimson Haki flickering over it
        for (int k = 0; k < 5; k++) {
            double a = 2 * Math.PI * FireMesh.rand(seed + (int) age, k + 20);
            double up = FireMesh.rand(seed + (int) age, k + 30) * 2.0 - 1.0;
            double ul = Math.sqrt(1.0 + up * up);
            double ox = Math.cos(a) / ul;
            double oy = up / ul;
            double oz = Math.sin(a) / ul;
            FireMesh.streak(sink, cx + ox * ball * 1.02, cy + oy * ball * 1.02, cz + oz * ball * 1.02, -oz, 0, ox, 0.5 * ball, 0.04, 0xFF1E2E, 0.85 * swell);
        }
    }

    /**
     * The whirl's vortex round the user at (x, y, z) (their feet) (frames 98 to 142): white rings of motion on the ground spiralling out
     * behind the arm, at {@code turn} radians (its way round from +x toward +z), {@code reach} long; and a thick curved trail behind the
     * mace's end; {@code alpha} its strength.
     */
    public static void buzzVortex(FireMesh.Sink sink, double x, double y, double z, double turn, double reach, double alpha) {
        if (alpha < 0.02) {
            return;
        }
        // the reference's frames 125 to 140: a whole disc of soft white swirl streaks on the ground, as wide as the arm reaches and more,
        // spiralling in toward the user and whirling with the arm (each streak trails behind its bright head), densest in a ring round
        // the middle, a bright white core round the user's feet
        double outer = reach + 1.8;
        for (int k = 0; k < BUZZ_SWIRLS; k++) {
            double r1 = FireMesh.rand(BUZZ_SWIRLS, k);
            double r2 = FireMesh.rand(BUZZ_SWIRLS, k + 40);
            double r3 = FireMesh.rand(BUZZ_SWIRLS, k + 80);
            double rad = 0.9 + (outer - 0.9) * Math.sqrt((k + r1) / BUZZ_SWIRLS);
            // the inner ones run faster round (they are whirled by the same arm, closer in)
            double head = turn * (1.0 + 0.6 * (1.0 - rad / outer)) - 2 * Math.PI * r2;
            double sweep = 1.3 + 2.2 * r3;
            double ring = Math.exp(-Math.pow((rad - outer * 0.55) / (outer * 0.4), 2.0));
            spiral(sink, x, y + 0.06 + 0.004 * k, z, rad * 1.18, rad, head - sweep, sweep, 0.14 + 0.45 * r1 * (rad / outer), 0xFFFFFF,
                (0.3 + 0.45 * ring) * alpha);
        }
        // the bright core: tight fast rings round the feet
        for (int k = 0; k < 3; k++) {
            spiral(sink, x, y + 0.2 + 0.03 * k, z, 1.1 + 0.25 * k, 0.6 + 0.25 * k, turn * 1.8 - 2.6 - 2.1 * k, 2.6, 0.32, 0xFFFFFF, 0.75 * alpha);
        }
        MochiArt.glow(sink, x, y + 0.3, z, 1.9, 0xFFFFFF, 0.6 * alpha);
        // the thick curved trail of the mace's end, at its height (the owner: "thick curved motion trails")
        spiral(sink, x, y + 1.6, z, reach + 0.4, reach + 0.4, turn - 2.4, 2.4, 1.6, 0xFFFFFF, 0.45 * alpha);
        spiral(sink, x, y + 1.6, z, reach + 0.4, reach + 0.4, turn - 1.2, 1.2, 0.7, 0xFFFFFF, 0.65 * alpha);
    }

    /** The whirl's swirl streaks on the ground. */
    static final int BUZZ_SWIRLS = 40;

    /**
     * A flat streak of light on the level round (cx, cy, cz): from {@code from} radians (x toward z) on for {@code sweep}, its radius
     * going from {@code r0} at its tail to {@code r1} at its head, {@code width} wide (thinning at both ends), clear at its tail,
     * brightest just behind its head.
     */
    static void spiral(FireMesh.Sink sink, double cx, double cy, double cz, double r0, double r1, double from, double sweep, double width, int rgb,
                       double alpha) {
        if (alpha < 0.01 || sweep <= 0.0) {
            return;
        }
        int n = Math.max(4, Math.min(20, (int) (sweep * 6.0)));
        double[] q = SPIRAL_Q;
        int[] c = SPIRAL_C;
        for (int k = 0; k < n; k++) {
            for (int e = 0; e < 2; e++) {
                double f = (k + e) / (double) n;
                double a = from + sweep * f;
                double r = r0 + (r1 - r0) * f;
                double w = width * 0.5 * Math.sin(Math.PI * Math.min(1.0, f * 0.85 + 0.08));
                double ca = Math.cos(a);
                double sa = Math.sin(a);
                int i0 = e == 0 ? 0 : 3;
                int i1 = e == 0 ? 1 : 2;
                q[i0 * 3] = cx + ca * (r - w);
                q[i0 * 3 + 1] = cy;
                q[i0 * 3 + 2] = cz + sa * (r - w);
                q[i1 * 3] = cx + ca * (r + w);
                q[i1 * 3 + 1] = cy;
                q[i1 * 3 + 2] = cz + sa * (r + w);
                double strength = f < 0.8 ? f / 0.8 : 1.0 - (f - 0.8) / 0.2 * 0.6;
                int col = FireMesh.argb(alpha * strength, rgb);
                c[i0] = col;
                c[i1] = col;
            }
            sink.quad(q, c);
        }
    }

    static final double[] SPIRAL_Q = new double[12];
    static final int[] SPIRAL_C = new int[4];

    /**
     * The slam's ground (frames 166 to 195; the owner: "an expanding circular shockwave, violent crimson Haki lightning, massive ground
     * fractures, flying block debris, dense dust clouds and a large impact crater"): a white flash, a black star of splattered dough
     * and cracks spreading out of it, red sparks, a ring of shock, dust, rocks thrown; it lies, then fades; {@code age} ticks in.
     */
    public static void buzzCrater(FireMesh.Sink sink, double x, double y, double z, double age, int seed) {
        if (age < 0 || age > BUZZ_CRATER_LIFE) {
            return;
        }
        if (age < 5.0) {
            MochiArt.glow(sink, x, y + 1.0, z, 3.0 + 2.5 * age / 5.0, 0xFFFFFF, 0.95 * (1.0 - age / 5.0));
        }
        double stay = 1.0 - smooth((age - BUZZ_CRATER_LIFE + 25.0) / 25.0);
        double spread = ease(age / 4.0);
        // the black star: splats radiating, and cracks running out of it
        MochiArt.blob(sink, x, y + 0.03, z, 1, 0, 0, 0, 1, 0, 2.3 * spread + 0.05, 0.03, 2.1 * spread + 0.05, 0.8, 0.45, 0, seed, MochiArt.INK,
            stay);
        // its long thin splattered rays (the reference's frame 190: a black star twice the user's height across and more)
        for (int k = 0; k < 16; k++) {
            double a = 2 * Math.PI * (k + 0.4 * FireMesh.rand(seed, k)) / 16.0;
            double len = (2.2 + 3.2 * FireMesh.rand(seed, k + 10)) * spread + 0.05;
            MochiArt.blob(sink, x + Math.cos(a) * len * 0.6, y + 0.025, z + Math.sin(a) * len * 0.6, Math.cos(a), 0, Math.sin(a), 0, 1, 0, len * 0.55,
                0.02, 0.14 + 0.12 * FireMesh.rand(seed, k + 20), 0.9, 0.25, 0, seed + k, MochiArt.INK, stay);
            if (k % 2 == 0) {
                double d = len * (1.15 + 0.2 * FireMesh.rand(seed, k + 30));
                MochiArt.blob(sink, x + Math.cos(a) * d, y + 0.025, z + Math.sin(a) * d, 1, 0, 0, 0, 1, 0, 0.18, 0.02, 0.16, 0.8, 0.4, 0, seed + 40 + k,
                    MochiArt.INK, stay);
            }
        }
        double crack = ease(age / 6.0) * stay;
        for (int k = 0; k < 9; k++) {
            double a = 2 * Math.PI * FireMesh.rand(seed, k + 40);
            double px = x + Math.cos(a) * 1.2;
            double pz = z + Math.sin(a) * 1.2;
            double len = (2.5 + 3.0 * FireMesh.rand(seed, k + 50)) * crack;
            double bend = (FireMesh.rand(seed, k + 60) - 0.5) * 0.8;
            FireMesh.streak(sink, px + Math.cos(a + bend) * len, y + 0.04, pz + Math.sin(a + bend) * len, -Math.cos(a + bend), 0, -Math.sin(a + bend), len,
                0.07, 0x0B0A0E, 0.9 * stay);
        }
        // red streaks of Haki sprayed up out of the blow (frames 165 to 182: a fan of thin crimson spikes, a few white), and red specks
        // spattered over the black that fade (frames 175 to 190)
        if (age < 16.0) {
            double f = age / 16.0;
            for (int k = 0; k < 32; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 70);
                double up = 0.6 + 2.6 * FireMesh.rand(seed, k + 80);
                double ul = Math.sqrt(1.0 + up * up);
                double len = (2.0 + 4.5 * FireMesh.rand(seed, k + 90)) * ease(Math.min(1.0, f * 3.0));
                // out of the rim of the giant ball lying in it
                double rim = BUZZ_BALL * (1.0 + 0.15 * FireMesh.rand(seed, k + 99));
                double h = 0.2 + 1.6 * FireMesh.rand(seed, k + 98);
                FireMesh.ray(sink, x + Math.cos(a) * rim, y + h, z + Math.sin(a) * rim, Math.cos(a) / ul, up / ul, Math.sin(a) / ul, len, 0.14 + 0.12 * FireMesh.rand(seed, k + 95), 0, 0.0,
                    k % 5 == 0 ? 0xFFFFFF : k % 2 == 0 ? 0xE0101E : 0xB00818, 1.0 - f * f);
            }
            FireMesh.ring(sink, x, y + 0.2, z, 0, 1, 0, 1.0 + 8.0 * ease(f), 0.35, 0.15, 0xFFFFFF, 0.75 * (1.0 - f));
        }
        if (age < 30.0) {
            double f = 1.0 - age / 30.0;
            for (int k = 0; k < 12; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 150);
                double d = (0.6 + 3.2 * FireMesh.rand(seed, k + 160)) * spread;
                MochiArt.blob(sink, x + Math.cos(a) * d, y + 0.05, z + Math.sin(a) * d, Math.cos(a), 0, Math.sin(a), 0, 1, 0,
                    0.12 + 0.2 * FireMesh.rand(seed, k + 170), 0.02, 0.07 + 0.08 * FireMesh.rand(seed, k + 180), 0.9, 0.3, 0, seed + 150 + k,
                    MochiArt.HAKI_CRIMSON, f);
            }
        }
        // pale dust rolling out
        if (age < 40.0) {
            for (int k = 0; k < 7; k++) {
                double a = 2 * Math.PI * k / 7.0 + 0.3;
                double d = 1.8 + 3.5 * ease(age / 20.0);
                FireMesh.shell(sink, x + Math.cos(a) * d, y + 0.5 + 0.02 * age, z + Math.sin(a) * d, 0.8 + 0.035 * age, 0xDDD6CA, 0.5 * (1.0 - age / 40.0),
                    0.0);
            }
        }
        // rocks of the ground thrown up and falling round it, and (the owner's clip from the show) a vast field of grey rubble blocks
        // heaved up round the blow, tilted every way, lying there
        double[] t = new double[4];
        double fade = Math.min(1.0, (BUZZ_CRATER_LIFE - age) / 10.0);
        for (int k = 0; k < 24; k++) {
            com.hexhaki.piece.ironfist.IronArt.thrown(k, seed + 600, 9.0, 0.75, age, t);
            if (t[1] < -0.3) {
                continue;
            }
            com.hexhaki.piece.ironfist.IronArt.chunk(sink, x + t[0], y + 0.3 + t[1], z + t[2], 0.4 + 0.6 * FireMesh.rand(seed, k + 120), t[3], seed + k, fade);
        }
        double heave = ease(age / 3.0);
        for (int k = 0; k < 40; k++) {
            double a = 2 * Math.PI * FireMesh.rand(seed, k + 700);
            double d = 2.0 + 7.5 * Math.sqrt(FireMesh.rand(seed, k + 710));
            double size = (0.45 + 0.65 * FireMesh.rand(seed, k + 720)) * (1.0 - 0.35 * d / 9.5);
            double tilt = 0.4 + 0.8 * FireMesh.rand(seed, k + 730);
            double nx = Math.cos(a) * Math.sin(tilt);
            double nz = Math.sin(a) * Math.sin(tilt);
            double ny = Math.cos(tilt);
            double ux = -Math.sin(a);
            double uz = Math.cos(a);
            plank(sink, x + Math.cos(a) * d, y + size * 0.6 * heave - 0.1, z + Math.sin(a) * d, ux, 0, uz, nx, ny, nz, size, size * 0.8, size * 0.45,
                RUBBLE[k % RUBBLE.length], fade);
        }
    }

    /** The rubble field's greys (the show's slam: grey blocks, lighter and darker). */
    static final int[] RUBBLE = {0x9C9A9E, 0x8A888F, 0xA9A7AA, 0x7D7B83};

    // ------------------------------------------------------------------------------------------------------ spikes

    /** Fourteen ways round a ball (a cube's six faces and eight corners). */
    static final double[] SPIKES;

    static {
        double c = 1.0 / Math.sqrt(3.0);
        SPIKES = new double[] {1, 0, 0, -1, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, 1, 0, 0, -1, c, c, c, -c, c, c, c, -c, c, -c, -c, c, c, c, -c, -c, c,
            -c, c, -c, -c, -c, -c, -c};
    }

    // ------------------------------------------------------------------------------------------------------ a body stuck in mochi

    /**
     * A body stuck in mochi: a mass of it round the feet (x, y, z) up to the knees and over the hips ({@code height} the body's), its
     * surface heaving a little; {@code fade} 0 to 1 how much is there (it shrinks away when it lets go).
     */
    public static void stuck(FireMesh.Sink sink, double x, double y, double z, double height, double fade, double age, int seed) {
        if (fade < 0.02) {
            return;
        }
        double h = height * 0.55 * fade;
        MochiArt.blob(sink, x, y + h * 0.35, z, 1, 0, 0, 0, 1, 0, 0.75 * fade + 0.1, h * 0.55, 0.75 * fade + 0.1, 0.9, 0.12, age, seed,
            MochiArt.MOCHI, 1.0);
        MochiArt.blob(sink, x, y + h * 0.85, z, 1, 0, 0, 0, 1, 0, 0.5 * fade + 0.05, h * 0.35, 0.5 * fade + 0.05, 0.9, 0.15, age, seed + 1,
            MochiArt.MOCHI, 1.0);
    }

    static final double[] B = new double[6];
}
