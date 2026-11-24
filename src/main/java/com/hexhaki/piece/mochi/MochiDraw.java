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
    /** Mochi Tsuki's thrust: out in {@link #TSUKI_OUT}, gone by {@link #TSUKI_LIFE}. */
    public static final double TSUKI_OUT = 3.0;
    public static final double TSUKI_LIFE = 14.0;
    /** Yanagi Mochi: how long the stamping lasts, how many tendrils. */
    public static final double YANAGI_LIFE = 22.0;
    public static final int YANAGI_TENDRILS = 6;
    /** Kaku Mochi: the fists rise, then slam at {@link #KAKU_SLAM}; gone by {@link #KAKU_LIFE}. */
    public static final double KAKU_SLAM = 8.0;
    public static final double KAKU_LIFE = 20.0;
    /** A splat of mochi (an impact), a boom of fire. */
    public static final double SPLAT_LIFE = 12.0;
    public static final double BOOM_LIFE = 18.0;
    /** Zan Giri Mochi's club: raised, slammed at {@link #CLUB_SLAM}, gone by {@link #CLUB_LIFE}. */
    public static final double CLUB_SLAM = 7.0;
    public static final double CLUB_LIFE = 18.0;
    /** The Anemone's punch: out and back. */
    public static final double ANEMONE_PUNCH = 7.0;
    /** A dodge's hole: open and closed again. */
    public static final double HOLE_LIFE = 10.0;
    /** Kagami Mochi: the walls rise, close at {@link #WALLS_SHUT}, gone by {@link #WALLS_LIFE}. */
    public static final double WALLS_RISE = 6.0;
    public static final double WALLS_SHUT = 14.0;
    public static final double WALLS_LIFE = 34.0;
    /** A Chikara Mochi fist's flight from its donut. */
    public static final double DONUT_FIST = 6.0;

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
        // the bar runs into the back of the hand (the fist is its very end)
        double reach = Math.max(0.05, len - size * 0.5);
        MochiArt.tubeBegin();
        int n = sag > 0.0 ? 6 : 1;
        for (int k = 0; k <= n; k++) {
            double t = k / (double) n;
            double drop = sag * 4.0 * t * (1.0 - t);
            MochiArt.tubePoint(sx + ux * reach * t, sy + uy * reach * t - drop, sz + uz * reach * t, r);
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
        double out = age < PUNCH_OUT ? ease(age / PUNCH_OUT) : age < PUNCH_OUT + 2 ? 1.0 : 1.0 - smooth((age - PUNCH_OUT - 2) / (PUNCH_LIFE - PUNCH_OUT - 2));
        double d = PUNCH_REST + (reach - PUNCH_REST) * out;
        arm(sink, x, y, z, x + wx * d, y + wy * d, z + wz * d, PUNCH_ARM, PUNCH_FIST, 0.0, coat, age, seed, left);
    }

    // ------------------------------------------------------------------------------------------------------ Mochi Tsuki

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

    /**
     * Mochi Tsuki (RMB): the arm from the hand (x, y, z) twisted into a drill of mochi round Mogura, thrust along (wx, wy, wz) (unit)
     * {@code length} out at its furthest; {@code age} ticks into the thrust (out in {@link #TSUKI_OUT}, held spinning, pulled back by
     * {@link #TSUKI_LIFE}); its prongs out of the drill's point. Held ({@code age} < 0: charging, {@code charge} 0 to 1): the drill coils
     * at the hand, fatter the fuller.
     */
    public static void tsuki(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double length, double age,
                             double charge, double spin, boolean coat) {
        double[] b = B;
        FireMesh.basis(wx, wy, wz, b);
        double sx = b[0];
        double sy = b[1];
        double sz = b[2];
        double out = 0.0;
        if (age < 0) {
            out = 0.0;
        } else if (age > TSUKI_LIFE) {
            return;
        } else {
            double back = clamp((age - TSUKI_OUT - 4) / (TSUKI_LIFE - TSUKI_OUT - 4));
            out = (0.9 + (length - 0.9) * ease(age / TSUKI_OUT)) * (1.0 - smooth(back) * 0.9);
        }
        if (age < 0) {
            // held: the arm twisting itself up round Mogura, coils of mochi winding tighter and fatter round the forearm, the drill's
            // point just starting at the hand
            for (int k = 0; k < 4; k++) {
                double along = -0.35 + k * 0.3;
                double big = (0.2 + 0.22 * charge) * (1.0 - k * 0.12);
                double cx = x + wx * along + sx * 0.05 * Math.sin(spin + k);
                double cy = y + wy * along + sy * 0.05 * Math.sin(spin + k);
                double cz = z + wz * along + sz * 0.05 * Math.sin(spin + k);
                double tilt = 0.25 * Math.sin(spin * 0.7 + k * 1.3);
                MochiArt.torus(sink, cx, cy, cz, wx + sx * tilt, wy + sy * tilt, wz + sz * tilt, big, 0.09 + 0.07 * charge, 1.0,
                    coat ? MochiArt.HAKI : MochiArt.MOCHI);
            }
            double cl = 0.3 + 0.9 * charge;
            MochiArt.tubeBegin();
            for (int k = 0; k <= 6; k++) {
                double t = k / 6.0;
                MochiArt.tubePoint(x + wx * cl * t, y + wy * cl * t, z + wz * cl * t, (0.26 + 0.12 * charge) * (1.0 - t * 0.9) + 0.02);
            }
            MochiArt.tube(sink, 14, coat ? MochiArt.HAKI : MochiArt.MOCHI, 1.5, spin);
            trident(sink, x - wx * 1.6, y - wy * 1.6, z - wz * 1.6, x + wx * (cl + 0.15), y + wy * (cl + 0.15), z + wz * (cl + 0.15), sx, sy, sz);
            return;
        }
        double r0 = 0.5;
        // the drill: a long cone of mochi round the shaft, ridged like a screw, its thread spinning
        MochiArt.tubeBegin();
        int n = Math.max(8, Math.min(28, (int) (out * 2.0)));
        for (int k = 0; k <= n; k++) {
            double t = k / (double) n;
            double ridge = 1.0 + 0.1 * Math.sin(t * out * 5.0 - spin * 2.0);
            MochiArt.tubePoint(x + wx * out * t, y + wy * out * t, z + wz * out * t, (r0 * (1.0 - t * 0.94) + 0.02) * ridge);
        }
        MochiArt.tube(sink, 14, coat ? MochiArt.HAKI : MochiArt.MOCHI, Math.max(2.0, out * 0.9), spin);
        // Mogura: its shaft out behind the hand, its head out of the drill's point
        trident(sink, x - wx * 1.6, y - wy * 1.6, z - wz * 1.6, x + wx * (out + 0.1), y + wy * (out + 0.1), z + wz * (out + 0.1), sx, sy, sz);
    }

    // ------------------------------------------------------------------------------------------------------ Yanagi Mochi

    /**
     * Yanagi Mochi (Z): the leg from the hip (x, y, z) split into {@link #YANAGI_TENDRILS} tendrils stamping on the ground ahead along
     * (wx, wz) (flat, unit), the ground {@code ground} under the hip; {@code age} ticks in, each tendril lifting and stamping at its own
     * beat (a stamp every 4 ticks), spread 1.5 to 6 ahead and 2.5 to each side.
     */
    public static void yanagi(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double ground, double age, int seed) {
        if (age < 0 || age > YANAGI_LIFE) {
            return;
        }
        // 3.6.3: the tendrils grow out of the lifted leg as it turns into them (the clip lifts it by tick 3)
        double grow = ease(Math.max(0.0, age - 2.0) / 3.0) * (1.0 - smooth((age - YANAGI_LIFE + 5) / 5.0));
        if (grow < 0.02) {
            return;
        }
        double px = -wz;
        double pz = wx;
        for (int k = 0; k < YANAGI_TENDRILS; k++) {
            double a = FireMesh.rand(seed, k);
            double ahead = 1.5 + 4.5 * FireMesh.rand(seed, k + 10);
            double side = (a - 0.5) * 5.0;
            double gx = x + wx * ahead + px * side;
            double gz = z + wz * ahead + pz * side;
            // the beat: up, then stamped down (a stamp every 4 ticks, each tendril half a beat apart)
            double beat = ((age + k * 0.7) % 4.0) / 4.0;
            double lift = beat < 0.6 ? Math.sin(Math.PI * beat / 0.6) * 1.6 * grow : 0.0;
            double gy = ground + 0.15 + lift;
            // 3.6.3 (the owner: "Mochi appearing and disappearing should have more realism not just vanish"): each tendril grows out of
            // the hip along its own arc and pulls back into it the same way (its end receding), never squeezed whole
            double reach = Math.min(1.0, grow * (1.0 + 0.15 * k / YANAGI_TENDRILS));
            MochiArt.tubeBegin();
            double ex = x;
            double ey = y;
            double ez = z;
            for (int j = 0; j <= 6; j++) {
                double t = reach * j / 6.0;
                // arcing out from the hip, down onto the foot
                double hump = Math.sin(Math.PI * t) * (0.9 + 0.5 * lift);
                ex = x + (gx - x) * t;
                ey = y + (gy - y) * t + hump;
                ez = z + (gz - z) * t;
                MochiArt.tubePoint(ex, ey, ez, (0.2 - 0.07 * t) * (0.6 + 0.4 * grow));
            }
            MochiArt.tube(sink, 8, MochiArt.MOCHI, 0.0, 0.0);
            // the foot: a flattened blob at its end, squashed as it lands (a rounded tip while it is still growing out)
            double foot = smooth((reach - 0.75) / 0.25);
            double squash = lift < 0.05 ? 0.6 : 1.0;
            double fr = 0.14 + 0.2 * foot;
            MochiArt.blob(sink, ex, ey, ez, wx, 0, wz, 0, 1, 0, fr, fr * (0.65 * squash + 0.35 * (1.0 - foot)), fr * 0.82, 0.7, 0.0, age, seed + k,
                MochiArt.MOCHI, 1.0);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Kaku Mochi

    /**
     * Kaku Mochi (X): two square fists of mochi (black with Armament) on arms raised from the shoulders (x, y, z) high over, then slammed
     * down on (tx, ty, tz) at {@link #KAKU_SLAM}; {@code age} ticks in. The arms' way across the body: (wx, wz) flat (unit) the look.
     */
    public static void kaku(FireMesh.Sink sink, double x, double y, double z, double tx, double ty, double tz, double wx, double wz, double age,
                            boolean coat, int seed) {
        if (age < 0 || age > KAKU_LIFE) {
            return;
        }
        double px = -wz;
        double pz = wx;
        double rise = ease(age / (KAKU_SLAM * 0.6));
        double slam = smooth((age - KAKU_SLAM * 0.6) / (KAKU_SLAM * 0.4));
        double back = smooth((age - KAKU_LIFE + 6) / 6.0);
        for (int k = -1; k <= 1; k += 2) {
            double sx = x + px * 0.35 * k;
            double sz = z + pz * 0.35 * k;
            // raised: over and behind the head; slammed: down onto the target, a block either side of it
            double hx = x + px * 1.4 * k - wx * 0.5;
            double hy = y + 4.2 * rise;
            double hz = z + pz * 1.4 * k - wz * 0.5;
            double lx = tx + px * 0.9 * k;
            double ly = ty + 0.75;
            double lz = tz + pz * 0.9 * k;
            double fx = hx + (lx - hx) * slam;
            double fy = hy + (ly - hy) * slam;
            double fz = hz + (lz - hz) * slam;
            fx += (sx - fx) * back;
            fy += (y - fy) * back;
            fz += (sz - fz) * back;
            if (back > 0.97) {
                continue; // 3.6.3: drawn all the way back into the shoulder: the arm is the arm again
            }
            MochiArt.tubeBegin();
            for (int j = 0; j <= 8; j++) {
                double t = j / 8.0;
                double arc = Math.sin(Math.PI * t) * 1.2 * (1.0 - back);
                MochiArt.tubePoint(sx + (fx - sx) * t, y + (fy - y) * t + arc, sz + (fz - sz) * t, 0.2 * (1.0 - 0.4 * back));
            }
            MochiArt.bar(sink, coat ? MochiArt.HAKI : MochiArt.MOCHI, 1.0);
            // the square block: flat-edged, squashed a little as it lands; 3.6.3: pulled back in it shrinks into the arm's end
            double s = 0.75 * (1.0 - back) + 0.2 * back;
            double squash = age > KAKU_SLAM && age < KAKU_SLAM + 3 ? 0.8 : 1.0;
            MochiArt.blob(sink, fx, fy, fz, wx, 0, wz, 0, 1, 0, s, s * squash, s, 0.28, 0.0, age, seed + k, coat ? MochiArt.HAKI : MochiArt.MOCHI, 1.0);
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
     * Yaki Mochi's blast where the burning fist lands (x, y, z), {@code size} its scale, {@code age} ticks in: the house's banded fire
     * (FireMesh.puff, the Magma and Dragon fire's) bursting out, white mochi flung, a ring of shock round it.
     */
    public static void boom(FireMesh.Sink sink, double x, double y, double z, double size, double age, int seed) {
        if (age < 0 || age > BOOM_LIFE) {
            return;
        }
        double t = age / BOOM_LIFE;
        int n = 9;
        for (int k = 0; k < n; k++) {
            double a = 2 * Math.PI * (k + FireMesh.rand(seed, k)) / n;
            double up = 0.25 + 0.75 * FireMesh.rand(seed, k + 30);
            double d = size * (0.25 + 1.1 * ease(t * 1.4)) * (0.6 + 0.6 * FireMesh.rand(seed, k + 50));
            double r = size * (0.55 - 0.3 * t) * (0.7 + 0.5 * FireMesh.rand(seed, k + 70));
            double heat = Math.max(0.0, 1.0 - t * 1.25);
            FireMesh.puff(sink, x + Math.cos(a) * d, y + up * d * 0.9 + size * 0.4 * t, z + Math.sin(a) * d, r, Math.cos(a), up, Math.sin(a), 0.4,
                0.5, heat, 0.6, true, Math.max(0.0, 1.0 - t * t), age, seed + k);
        }
        if (age < 8) {
            FireMesh.ring(sink, x, y + 0.1, z, 0, 1, 0, size * (0.6 + 2.4 * ease(age / 8.0)), size * 0.25, 0.0, 0xFFE2B0, 0.7 * (1.0 - age / 8.0));
        }
        // 3.6.3 (the owner's reference: the fist bursts in a white-hot star of long spiky rays, then rings of fire spreading round it)
        if (age < 6) {
            double f = 1.0 - age / 6.0;
            MochiArt.glow(sink, x, y + size * 0.3, z, size * (0.9 + 0.6 * ease(age / 3.0)), 0xFFF6DC, 0.85 * f);
            for (int k = 0; k < BOOM_RAYS; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 90);
                double up = FireMesh.rand(seed, k + 110) * 1.6 - 0.5;
                double ul = Math.sqrt(1.0 + up * up);
                double len = size * (1.6 + 1.8 * FireMesh.rand(seed, k + 130)) * ease(Math.min(1.0, age / 2.5 + 0.3));
                FireMesh.ray(sink, x, y + size * 0.3, z, Math.cos(a) / ul, up / ul, Math.sin(a) / ul, len, size * 0.18, 0, 0.0,
                    k % 3 == 0 ? 0xFFFFFF : 0xFFE9A8, 0.9 * f);
            }
        }
        if (age < 12) {
            double u = ease(age / 12.0);
            for (int k = 0; k < 3; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 150);
                double tilt = 0.35 + 0.5 * FireMesh.rand(seed, k + 160);
                double nx = Math.cos(a) * tilt;
                double nz = Math.sin(a) * tilt;
                double nl = Math.sqrt(nx * nx + 1.0 + nz * nz);
                FireMesh.ring(sink, x, y + size * 0.35, z, nx / nl, 1.0 / nl, nz / nl, size * (0.5 + (1.5 + 0.4 * k) * u), size * (0.3 - 0.06 * k),
                    size * 0.15, k == 1 ? 0xFFF0C0 : 0xFF8C2E, 0.75 * (1.0 - age / 12.0));
            }
        }
        splat(sink, x, y + size * 0.3, z, 0, 1, 0, size * 1.2, age * 0.9, seed + 9);
    }

    /** 3.6.3: the explosion's star of rays. */
    static final int BOOM_RAYS = 14;

    // ------------------------------------------------------------------------------------------------------ Yaki Mochi

    /**
     * Yaki Mochi (C) held: the forearm from the elbow (x, y, z) along (wx, wy, wz) swelling with pressure, {@code charge} 0 to 1: a ball of
     * mochi growing round the forearm, trembling as it fills, the fist at its end.
     */
    public static void swell(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double charge, double age,
                             boolean coat, int seed) {
        // 3.6.3 (the owner's reference: a huge puffy mass of dough swelling round the pointed arm, splattering, its fist at the end): the
        // forearm's mochi grows with the hold, lumps welling up on it one by one, drips hanging off it and drops falling away
        int[] bands = coat ? MochiArt.HAKI : MochiArt.MOCHI;
        double g = ease(charge);
        double r = 0.18 + 0.62 * g;
        double shake = charge > 0.6 ? 0.03 * Math.sin(age * 3.1) * charge : 0.0;
        double along = 0.45 + r * 1.1;
        double cx = x + wx * along + shake;
        double cy = y + wy * along + shake * 0.5;
        double cz = z + wz * along;
        double[] b = B;
        MochiArt.axes(wx, wy, wz, b);
        MochiArt.blob(sink, cx, cy, cz, b[0], b[1], b[2], b[3], b[4], b[5], r * 1.35, r, r, 0.95, 0.12, age, seed, bands, 1.0);
        // its side: across the way, level
        double sx = -wz;
        double sz = wx;
        double sl = Math.max(1.0E-6, Math.hypot(sx, sz));
        sx /= sl;
        sz /= sl;
        // the up across it
        double ux = wy * sz;
        double uy = wz * sx - wx * sz;
        double uz = -wy * sx;
        double ul = Math.max(1.0E-6, Math.sqrt(ux * ux + uy * uy + uz * uz));
        ux /= ul;
        uy /= ul;
        uz /= ul;
        // lumps welling up round it, one after another as it fills
        for (int k = 0; k < SWELL_LUMPS; k++) {
            double grow = Math.max(0.0, Math.min(1.0, (charge - 0.12 - k * 0.14) / 0.25));
            if (grow <= 0.0) {
                continue;
            }
            double a = 2 * Math.PI * (k * 0.382 + FireMesh.rand(seed, k) * 0.2);
            double ca = Math.cos(a);
            double sa = Math.sin(a);
            double on = (FireMesh.rand(seed, k + 9) - 0.5) * 1.4 * r;
            double lr = r * (0.42 + 0.18 * FireMesh.rand(seed, k + 17)) * ease(grow);
            MochiArt.ball(sink, cx + wx * on + (sx * ca + ux * sa) * r * 0.8, cy + wy * on + uy * sa * r * 0.8, cz + wz * on + (sz * ca + uz * sa) * r * 0.8,
                lr, sx * ca + ux * sa, uy * sa, sz * ca + uz * sa, 1.2, 0.08, age, seed + k, bands);
        }
        // drips hanging off its underside, and drops falling from them to the ground
        for (int k = 0; k < 2; k++) {
            if (charge < 0.25 + 0.2 * k) {
                break;
            }
            double on = (k == 0 ? -0.3 : 0.45) * r;
            double hx = cx + wx * on;
            double hy = cy + wy * on - r * 0.85;
            double hz = cz + wz * on;
            double len = r * (0.45 + 0.2 * Math.sin(age * 0.13 + k));
            MochiArt.tubeBegin();
            for (int s = 0; s <= 3; s++) {
                double f = s / 3.0;
                MochiArt.tubePoint(hx, hy - len * f, hz, r * 0.22 * (1.0 - 0.6 * f));
            }
            MochiArt.tube(sink, 8, bands, 0.0, 0.0);
            double fall = (age * 0.06 + k * 0.5) % 1.0;
            MochiArt.ball(sink, hx, hy - len - 1.4 * fall * fall, hz, r * 0.11, 0, 1, 0, 1.3, 0.0, age, seed + 20 + k, bands);
        }
        double tip = along + r * 1.35 + 0.1;
        MochiArt.fist(sink, x + wx * tip + shake, y + wy * tip, z + wz * tip, wx, wy, wz, sx, 0, sz, 0.26 + 0.3 * charge, coat, age, seed + 3);
    }

    /** 3.6.3: how many lumps well up on Yaki Mochi's swelling forearm. */
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

    // ------------------------------------------------------------------------------------------------------ the donut

    /** 3.6.3: a donut's tube (its radius) to the donut's outer radius (the awakened Muso Donuts': every donut of the fruit is one of those). */
    public static final double DONUT_THICK = 0.28;
    /** 3.6.3: how lumpy a donut is (a share of its tube). */
    public static final double DONUT_LUMP = 0.34;
    /** 3.6.3: the awakened Muso Donuts' outer radius. */
    public static final double MUSO = 1.3;

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
        double s = springy(f);
        double thick = big * DONUT_THICK * s * (1.0 + 0.8 * (1.0 - f));
        double ring = (big - big * DONUT_THICK) * s * Math.pow(f, 0.7);
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

    // ------------------------------------------------------------------------------------------------------ Zan Giri Mochi

    /**
     * Zan Giri Mochi (V): the user become a giant wheel of mochi rolling along (wx, wz) (flat, unit), its middle (x, y, z) (on the
     * ground: its bottom at y - {@code radius}), turned {@code spin} radians; its rim lumpy, speed lines behind it.
     */
    public static void wheel(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double radius, double spin, double age,
                             int seed) {
        double ax = -wz;
        double az = wx;
        // 3.6.3 (the owner: "the rolling donut the player turns into it should look like the donuts in the awakening v move, look at it 1
        // of those"): one of the fruit's donuts standing on its rim, rolling (its lumps and its fist turned with it), no drips (flung off)
        donut(sink, x, y, z, ax, 0, az, radius, -spin, 0, 0.0, 0, 0, 0, false, age, seed, false);
        // speed: streaks of white behind it
        for (int k = 0; k < 5; k++) {
            double h = (k - 2) * radius * 0.35;
            FireMesh.streak(sink, x - wx * radius * 1.1, y + h, z - wz * radius * 1.1, -wx, 0, -wz, radius * (1.6 + 0.4 * (k % 2)), 0.05, 0xFFFFFF,
                0.55);
        }
        // 3.6.3 (the owner's reference: the rolling donut tears the ground up behind it): chunks of the ground it passed flipped up in two
        // rows either side of its track, falling back behind it, and dust kicked up at its foot
        double rolled = spin * radius;
        double ground = y - radius;
        for (int j = 0; j < WHEEL_CHUNKS; j++) {
            double behind = (rolled % WHEEL_GAP + WHEEL_GAP) % WHEEL_GAP + j * WHEEL_GAP;
            double t = behind / WHEEL_ROLL;
            double h = 0.32 * t - 0.04 * t * t;
            if (h <= 0.0 || behind > rolled) {
                continue;
            }
            int spot = (int) Math.floor((rolled - behind) / WHEEL_GAP + 0.5);
            for (int side = -1; side <= 1; side += 2) {
                double off = side * radius * (0.45 + 0.25 * FireMesh.rand(seed + spot, side + 3)) + side * t * 0.05;
                com.hexhaki.piece.ironfist.IronArt.chunk(sink, x - wx * behind + ax * off, ground + h, z - wz * behind + az * off,
                    0.16 + 0.08 * FireMesh.rand(seed + spot, side + 7), t * 0.5 + spot, seed + spot * 2 + side, 1.0);
            }
        }
        for (int k = 0; k < 3; k++) {
            double d = radius * (0.5 + 0.7 * k);
            FireMesh.shell(sink, x - wx * d, ground + 0.3 + 0.2 * k, z - wz * d, radius * (0.4 + 0.18 * k), 0xE9E5DE, 0.42 - 0.1 * k, 0.0);
        }
    }

    /** 3.6.3: the ground torn up behind the wheel: how many spots back, a spot every so many blocks, the wheel's roll a tick (blocks). */
    static final int WHEEL_CHUNKS = 8;
    static final double WHEEL_GAP = 1.1;
    static final double WHEEL_ROLL = 0.9;

    /**
     * Zan Giri Mochi's club: from the shoulder (x, y, z) an arm swung up overhead and down onto (tx, ty, tz) at {@link #CLUB_SLAM}, ending
     * in a giant spiked ball (black with Armament); {@code age} ticks in.
     */
    public static void club(FireMesh.Sink sink, double x, double y, double z, double tx, double ty, double tz, double age, boolean coat, int seed) {
        if (age < 0 || age > CLUB_LIFE) {
            return;
        }
        double up = ease(age / (CLUB_SLAM * 0.55));
        double down = smooth((age - CLUB_SLAM * 0.55) / (CLUB_SLAM * 0.45));
        double back = smooth((age - CLUB_LIFE + 6) / 6.0);
        double hx = x + (tx - x) * 0.15;
        double hy = y + 6.0 * up;
        double hz = z + (tz - z) * 0.15;
        double bx = hx + (tx - hx) * down;
        double by = hy + (ty + 1.3 - hy) * down;
        double bz = hz + (tz - hz) * down;
        bx += (x - bx) * back;
        by += (y - by) * back;
        bz += (z - bz) * back;
        double r = 1.25 * (1.0 - back * 0.7);
        MochiArt.tubeBegin();
        for (int j = 0; j <= 8; j++) {
            double t = j / 8.0;
            MochiArt.tubePoint(x + (bx - x) * t, y + (by - y) * t + Math.sin(Math.PI * t) * 1.5 * (1.0 - back), z + (bz - z) * t, 0.26);
        }
        MochiArt.bar(sink, coat ? MochiArt.HAKI : MochiArt.MOCHI, 1.0);
        spiked(sink, bx, by, bz, r, coat, age, seed);
    }

    /** A spiked ball at (x, y, z), {@code r} round: a ball and fourteen spikes all round it (black with Armament). */
    public static void spiked(FireMesh.Sink sink, double x, double y, double z, double r, boolean coat, double age, int seed) {
        int[] bands = coat ? MochiArt.HAKI : MochiArt.MOCHI;
        MochiArt.ball(sink, x, y, z, r, 0, 1, 0, 1.0, 0.0, age, seed, bands);
        for (int k = 0; k < SPIKES.length / 3; k++) {
            double ox = SPIKES[k * 3];
            double oy = SPIKES[k * 3 + 1];
            double oz = SPIKES[k * 3 + 2];
            MochiArt.cone(sink, x + ox * r * 0.85, y + oy * r * 0.85, z + oz * r * 0.85, x + ox * r * 1.55, y + oy * r * 1.55, z + oz * r * 1.55,
                r * 0.24, bands);
        }
    }

    /** Fourteen ways round a ball (a cube's six faces and eight corners). */
    static final double[] SPIKES;

    static {
        double c = 1.0 / Math.sqrt(3.0);
        SPIKES = new double[] {1, 0, 0, -1, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, 1, 0, 0, -1, c, c, c, -c, c, c, c, -c, c, -c, -c, c, c, c, -c, -c, c,
            -c, c, -c, -c, -c, -c, -c};
    }

    // ------------------------------------------------------------------------------------------------------ Mochi Anemone

    /** The Anemone's rings: how many, how far round the user, how high over the feet. */
    public static final int ANEMONE_RINGS = 4;
    /** 3.6.3: the Anemone's donuts, their outer radius (the awakened donuts' look, smaller). */
    public static final double ANEMONE_DONUT = 1.0;
    public static final double ANEMONE_OUT = 2.0;
    public static final double ANEMONE_UP = 1.7;

    /**
     * Where the Anemone's ring {@code k} is round the user standing at (x, y, z), {@code age} ticks in (they float round slowly, bobbing):
     * into out (x, y, z, then the way it faces out).
     */
    public static void ring(double x, double y, double z, int k, double age, double[] out) {
        double a = 2 * Math.PI * k / ANEMONE_RINGS + age * 0.04;
        double ox = Math.cos(a);
        double oz = Math.sin(a);
        out[0] = x + ox * ANEMONE_OUT;
        out[1] = y + ANEMONE_UP + 0.25 * Math.sin(age * 0.15 + k * 1.7) + (k % 2) * 0.5;
        out[2] = z + oz * ANEMONE_OUT;
        out[3] = ox;
        out[4] = 0.15;
        out[5] = oz;
    }

    static final double[] R = new double[6];

    /**
     * Mochi Anemone (B) held: its rings round the user (x, y, z) floating, grown in over 6 ticks ({@code grow} 0 to 1, shrinking away
     * again); {@code punches} (ring, target x, y, z, the punch's age: 5 a punch) the arms out of the rings punching (black fists with
     * Armament).
     */
    public static void anemone(FireMesh.Sink sink, double x, double y, double z, double age, double grow, double[] punches, int count,
                               boolean coat, int seed) {
        if (grow < 0.02) {
            return;
        }
        // 3.6.3 (the owner: "Mochi anemoni, the fists should fill the entire hole and adapt to the size of the donuts, just like the V move
        // in the awakening"): each ring one of the fruit's donuts, its fist resting in its hole, punching out of it as wide as the hole
        for (int k = 0; k < ANEMONE_RINGS; k++) {
            ring(x, y, z, k, age, R);
            double out = 0.0;
            double fx = 0;
            double fy = 0;
            double fz = 0;
            for (int p = 0; p < count; p++) {
                double pa = punches[p * 5 + 4];
                if ((int) punches[p * 5] != k || pa < 0 || pa > ANEMONE_PUNCH) {
                    continue;
                }
                double o = pa < 2.5 ? ease(pa / 2.5) : 1.0 - smooth((pa - 3.5) / (ANEMONE_PUNCH - 3.5));
                if (o > out) {
                    out = o;
                    fx = R[0] + (punches[p * 5 + 1] - R[0]) * o;
                    fy = R[1] + (punches[p * 5 + 2] - R[1]) * o;
                    fz = R[2] + (punches[p * 5 + 3] - R[2]) * o;
                }
            }
            // a punch turns the donut to face where it goes
            double ax = R[3];
            double ay = R[4];
            double az = R[5];
            if (out > 0.02) {
                double dx = fx - R[0];
                double dy = fy - R[1];
                double dz = fz - R[2];
                double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dl > 1.0E-3) {
                    ax = dx / dl;
                    ay = dy / dl;
                    az = dz / dl;
                }
            }
            donut(sink, R[0], R[1], R[2], ax, ay, az, ANEMONE_DONUT, grow, 0.0, 2, out > 0.02 ? out : 0.0, fx, fy, fz, coat, age, seed + k,
                k % 2 == 1);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Sticky Mochi

    /** A glob of sticky mochi flying at (x, y, z) along (wx, wy, wz), wobbling as it goes. */
    public static void glob(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double age, int seed) {
        double wob = 1.0 + 0.18 * Math.sin(age * 1.9);
        MochiArt.ball(sink, x, y, z, 0.42, wx, wy, wz, 1.35 * wob, 0.18, age, seed, MochiArt.MOCHI);
        // its tail: smaller drops stretched behind
        for (int k = 1; k <= 2; k++) {
            MochiArt.ball(sink, x - wx * 0.55 * k, y - wy * 0.55 * k, z - wz * 0.55 * k, 0.2 / k, wx, wy, wz, 1.5, 0.0, age, seed + k, MochiArt.MOCHI);
        }
    }

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

    /** A patch of sticky mochi on the ground at (x, y, z), {@code radius} across, its edge lumpy; {@code fade} 0 to 1. */
    public static void patch(FireMesh.Sink sink, double x, double y, double z, double radius, double fade, double age, int seed) {
        if (fade < 0.02) {
            return;
        }
        MochiArt.blob(sink, x, y + 0.08, z, 1, 0, 0, 0, 1, 0, radius * fade, 0.18 * fade, radius * fade, 0.85, 0.2, age, seed, MochiArt.MOCHI, 1.0);
    }

    // ------------------------------------------------------------------------------------------------------ the dodge

    /**
     * Pseudo-Intangibility: a hole opening in the mochi body where a blow passes, at (x, y, z) facing (wx, wy, wz): a ring of mochi
     * swelling open and closing back by {@link #HOLE_LIFE}.
     */
    public static void hole(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double age) {
        if (age < 0 || age > HOLE_LIFE) {
            return;
        }
        double open = age < 3 ? ease(age / 3.0) : 1.0 - smooth((age - 4) / (HOLE_LIFE - 4));
        if (open < 0.02) {
            return;
        }
        double big = 0.12 + 0.3 * open;
        MochiArt.torus(sink, x, y, z, wx, wy, wz, big, 0.09 + 0.05 * open, 1.0, MochiArt.MOCHI);
        // its inside: a shadowed lip a little in and behind (the hole goes in)
        MochiArt.torus(sink, x - wx * 0.08, y - wy * 0.08, z - wz * 0.08, wx, wy, wz, big * 0.82, 0.06 + 0.03 * open, 1.0, HOLE_IN);
    }

    /** A hole's shadowed inside. */
    static final int[] HOLE_IN = {0xCFC3D0, 0xB7A5B4, 0x9C8AA0, 0x857491, 0x6C5C7A};

    // ------------------------------------------------------------------------------------------------------ the Awakening

    /** The mochi ground: its lumps sit on a grid of the world this far apart (so they stay put as the user walks), at most this many. */
    public static final double GROUND_STEP = 2.6;
    public static final int GROUND_MOST = 64;

    /**
     * The Awakening's mochi ground: the lumps round (cx, cz) (world) within {@code radius}, each on its cell of a grid of the world
     * {@link #GROUND_STEP} apart (nudged a little inside its cell by its own numbers), into {@code out} (x, z, size, seed each: world x
     * and z, its size shrinking toward the field's edge); how many. The client gives each its ground's height; every lump is a fixed place,
     * so walking through the field the ground stays and the field spreads under the feet.
     */
    public static int groundLumps(double cx, double cz, double radius, double[] out) {
        int n = 0;
        int r = (int) Math.ceil(radius / GROUND_STEP) + 1;
        int ix = (int) Math.floor(cx / GROUND_STEP);
        int iz = (int) Math.floor(cz / GROUND_STEP);
        for (int i = ix - r; i <= ix + r && n < GROUND_MOST; i++) {
            for (int j = iz - r; j <= iz + r && n < GROUND_MOST; j++) {
                int seed = i * 73856093 ^ j * 19349663;
                double x = (i + 0.2 + 0.6 * FireMesh.rand(seed, 1)) * GROUND_STEP;
                double z = (j + 0.2 + 0.6 * FireMesh.rand(seed, 2)) * GROUND_STEP;
                double d = Math.hypot(x - cx, z - cz);
                if (d > radius) {
                    continue;
                }
                double edge = Math.min(1.0, (radius - d) / 2.5);
                out[n * 4] = x;
                out[n * 4 + 1] = z;
                out[n * 4 + 2] = (0.75 + 0.35 * FireMesh.rand(seed, 3)) * edge;
                out[n * 4 + 3] = seed;
                n++;
            }
        }
        return n;
    }

    /**
     * The Awakening's mochi ground: {@code count} lumps from {@link #groundLumps} at (x, y, z) from the eye (5 each: x, y the ground's
     * height there, z, size, seed), {@code grow} 0 to 1 how much is there: wide flat swells of mochi, overlapping into one bubbly floor of
     * it, heaving slowly. Drawn coarse (many of them).
     */
    public static void ground(FireMesh.Sink sink, double[] lumps, int count, double grow, double age) {
        if (grow < 0.02) {
            return;
        }
        MochiArt.low = true;
        for (int k = 0; k < count; k++) {
            double size = lumps[k * 5 + 3] * grow;
            if (size < 0.05) {
                continue;
            }
            int seed = (int) lumps[k * 5 + 4];
            double heave = 0.85 + 0.15 * Math.sin(age * 0.07 + (seed & 255) * 0.1);
            double a = FireMesh.rand(seed, 4) * Math.PI;
            MochiArt.blob(sink, lumps[k * 5], lumps[k * 5 + 1], lumps[k * 5 + 2], Math.cos(a), 0, Math.sin(a), 0, 1, 0, size * 1.95, size * 0.6 * heave,
                size * 1.65, 0.95, 0.1, age, seed, MochiArt.MOCHI, 1.0);
        }
        MochiArt.low = false;
    }

    /** Nagare Mochi's wave: its profile (back to front: how far behind the crest, how high a share of the wave), the curl at its lip. */
    static final double[] WAVE_BACK = {-4.6, -3.3, -2.2, -1.3, -0.6, 0.0, 0.6, 1.1, 1.4, 1.35, 1.0, 0.6};
    static final double[] WAVE_UP = {0.0, 0.2, 0.45, 0.7, 0.9, 1.0, 1.05, 0.98, 0.8, 0.6, 0.46, 0.42};

    /**
     * Nagare Mochi (awakened Z): a wave of mochi rolling over the ground (y) from (x, z) along (wx, wz) (flat, unit), its crest
     * {@code travelled} out, {@code width} across, {@code height} high: a sheet rising from the ground behind to its crest and curling
     * over at its front, its lip a round roll, lumps of mochi tumbling on it; lower at its two ends.
     */
    public static void wave(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double travelled, double width, double height,
                            double age, int seed) {
        double px = -wz;
        double pz = wx;
        double cx = x + wx * travelled;
        double cz = z + wz * travelled;
        int cols = 12;
        int rows = WAVE_BACK.length - 1;
        for (int i = 0; i <= cols; i++) {
            double t = i / (double) cols * 2.0 - 1.0;
            double edge = Math.sqrt(Math.max(0.0, 1.0 - t * t * t * t));
            double h = height * (0.15 + 0.85 * edge) * (1.0 + 0.06 * Math.sin(age * 0.35 + i * 0.9));
            for (int j = 0; j <= rows; j++) {
                double b = WAVE_BACK[j] * (0.6 + 0.4 * edge) * height / 3.0;
                double px2 = cx + wx * b + px * width * 0.5 * t;
                double py2 = y + h * WAVE_UP[j];
                double pz2 = cz + wz * b + pz * width * 0.5 * t;
                int v = j * (cols + 1) + i;
                MochiArt.GRID[v * 3] = px2;
                MochiArt.GRID[v * 3 + 1] = py2;
                MochiArt.GRID[v * 3 + 2] = pz2;
            }
        }
        // each point shaded by the sheet's slope there (its normal from its neighbours)
        for (int j = 0; j <= rows; j++) {
            for (int i = 0; i <= cols; i++) {
                int v = j * (cols + 1) + i;
                int a = j * (cols + 1) + Math.max(0, i - 1);
                int b = j * (cols + 1) + Math.min(cols, i + 1);
                int c = Math.max(0, j - 1) * (cols + 1) + i;
                int d = Math.min(rows, j + 1) * (cols + 1) + i;
                double ux = MochiArt.GRID[b * 3] - MochiArt.GRID[a * 3];
                double uy = MochiArt.GRID[b * 3 + 1] - MochiArt.GRID[a * 3 + 1];
                double uz = MochiArt.GRID[b * 3 + 2] - MochiArt.GRID[a * 3 + 2];
                double vx = MochiArt.GRID[d * 3] - MochiArt.GRID[c * 3];
                double vy = MochiArt.GRID[d * 3 + 1] - MochiArt.GRID[c * 3 + 1];
                double vz = MochiArt.GRID[d * 3 + 2] - MochiArt.GRID[c * 3 + 2];
                double nx = uy * vz - uz * vy;
                double ny = uz * vx - ux * vz;
                double nz = ux * vy - uy * vx;
                double nl = Math.max(1.0E-9, Math.sqrt(nx * nx + ny * ny + nz * nz));
                MochiArt.GRID_C[v] = MochiArt.shade(MochiArt.GRID[v * 3], MochiArt.GRID[v * 3 + 1], MochiArt.GRID[v * 3 + 2], nx / nl, ny / nl, nz / nl,
                    MochiArt.MOCHI, 1.0);
            }
        }
        MochiArt.grid(sink, rows, cols);
        // the lip: a round roll along the curl's edge
        MochiArt.tubeBegin();
        for (int i = 0; i <= cols; i++) {
            double t = i / (double) cols * 2.0 - 1.0;
            double edge = Math.sqrt(Math.max(0.0, 1.0 - t * t * t * t));
            double h = height * (0.15 + 0.85 * edge) * (1.0 + 0.06 * Math.sin(age * 0.35 + i * 0.9));
            double b = WAVE_BACK[rows] * (0.6 + 0.4 * edge) * height / 3.0;
            MochiArt.tubePoint(cx + wx * b + px * width * 0.5 * t, y + h * WAVE_UP[rows], cz + wz * b + pz * width * 0.5 * t, 0.18 + 0.2 * edge);
        }
        MochiArt.tube(sink, 8, MochiArt.MOCHI, 0.0, 0.0);
        for (int k = 0; k < 5; k++) {
            double t = (k + 0.5) / 5.0 * 2.0 - 1.0;
            double edge = Math.sqrt(Math.max(0.0, 1.0 - t * t * t * t));
            double h = height * (0.15 + 0.85 * edge);
            double roll = (age * 0.25 + k * 1.3) % 1.0;
            double b = (1.3 - roll * 0.6) * (0.6 + 0.4 * edge) * height / 3.0;
            MochiArt.ball(sink, cx + wx * b + px * width * 0.5 * t, y + h * (1.0 - roll * 0.4), cz + wz * b + pz * width * 0.5 * t, 0.3 * edge + 0.1, wx,
                -0.5, wz, 1.3, 0.12, age, seed + k, MochiArt.MOCHI);
        }
    }

    /**
     * Kagami Mochi (awakened X): two walls of mochi rising out of the ground either side of (x, y, z) across (wx, wz) (flat, unit: the
     * walls face along it), {@code apart} each side at first, closing to crush at {@link #WALLS_SHUT}; {@code age} ticks in.
     */
    public static void walls(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double apart, double age, int seed) {
        if (age < 0 || age > WALLS_LIFE) {
            return;
        }
        double rise = ease(age / WALLS_RISE);
        double close = smooth((age - WALLS_RISE) / (WALLS_SHUT - WALLS_RISE));
        double sink2 = smooth((age - WALLS_LIFE + 8) / 8.0);
        double px = -wz;
        double pz = wx;
        double h = 5.0 * rise * (1.0 - sink2);
        if (h < 0.05) {
            return;
        }
        for (int k = -1; k <= 1; k += 2) {
            double off = (apart + (0.7 - apart) * close) * k;
            double cx = x + px * off;
            double cz = z + pz * off;
            MochiArt.blob(sink, cx, y + h * 0.5, cz, wx, 0, wz, 0, 1, 0, 3.2, h * 0.5 + 0.05, 0.75, 0.32, 0.07, age, seed + k, MochiArt.MOCHI, 1.0);
            // its top lumpy, mochi bulging over the edge
            for (int j = 0; j < 4; j++) {
                double t = (j + 0.5) / 4.0 * 2.0 - 1.0;
                MochiArt.ball(sink, cx + wx * 2.6 * t, y + h + 0.05, cz + wz * 2.6 * t, 0.55 + 0.15 * FireMesh.rand(seed, j + k * 4), wx, 0, wz, 1.5,
                    0.12, age, seed + j + k * 4, MochiArt.MOCHI);
            }
        }
    }

    /**
     * Amadare Mochi (awakened C): tendrils of mochi raining down from the sky ({@code sky} blocks over (x, y, z)) onto points round it
     * within {@code radius}; tendril {@code k} falls at {@code k * every} ticks, strikes 4 ticks later, lingers and pulls back up.
     * {@code age} ticks in.
     */
    public static void rain(FireMesh.Sink sink, double x, double y, double z, double radius, double sky, int count, double every, double age,
                            int seed) {
        // the mass of mochi overhead the tendrils pour out of, gathering in its first 6 ticks, gone when the last has struck
        double gather = ease(age / 6.0) * (1.0 - smooth((age - count * every - 10) / 6.0));
        if (gather > 0.02) {
            for (int k = 0; k < 6; k++) {
                double a = 2 * Math.PI * k / 6 + 0.3;
                double d = radius * 0.55 * (k == 0 ? 0.0 : 1.0);
                MochiArt.blob(sink, x + Math.cos(a) * d, y + sky + 0.6 * FireMesh.rand(seed, k + 200), z + Math.sin(a) * d, Math.cos(a), 0, Math.sin(a), 0,
                    1, 0, radius * 0.55 * gather, 1.1 * gather, radius * 0.5 * gather, 0.9, 0.15, age, seed + k, MochiArt.MOCHI, 1.0);
            }
        }
        for (int k = 0; k < count; k++) {
            double a = age - k * every;
            if (a < 0 || a > 14) {
                continue;
            }
            double ang = 2 * Math.PI * FireMesh.rand(seed, k);
            double d = radius * Math.sqrt(FireMesh.rand(seed, k + 50));
            double gx = x + Math.cos(ang) * d;
            double gz = z + Math.sin(ang) * d;
            double down = ease(a / 4.0) * (1.0 - smooth((a - 9.0) / 5.0));
            double top = y + sky;
            double tipY = top + (y + 0.3 - top) * down;
            MochiArt.tubeBegin();
            for (int j = 0; j <= 6; j++) {
                double t = j / 6.0;
                double wob = 0.25 * Math.sin(t * 5.0 + k) * (1.0 - t);
                MochiArt.tubePoint(gx + wob, top + (tipY - top) * t, gz + wob * 0.5, 0.34 + 0.18 * t);
            }
            MochiArt.tube(sink, 8, MochiArt.MOCHI, 0.0, 0.0);
            MochiArt.ball(sink, gx, tipY, gz, 0.62, 0, -1, 0, 1.4, 0.1, a, seed + k, MochiArt.MOCHI);
            if (a > 4.0) {
                splat(sink, gx, y + 0.2, gz, 0, 1, 0, 1.0, (a - 4.0) * 1.2, seed + k * 7);
            }
        }
    }

    /** Muso Donuts: how many donut rings, how far round behind the user, how high. */
    public static final int DONUTS = 5;

    /** Where donut {@code k} floats round the user (x, y, z) facing (wx, wz) (flat, unit): into out (x, y, z). */
    public static void donut(double x, double y, double z, double wx, double wz, int k, double age, double[] out) {
        double spread = (k - (DONUTS - 1) / 2.0) / ((DONUTS - 1) / 2.0);
        double px = -wz;
        double pz = wx;
        out[0] = x + px * spread * 3.2 - wx * (1.2 - Math.abs(spread) * 0.8);
        out[1] = y + 2.6 + (1.0 - spread * spread) * 1.4 + 0.15 * Math.sin(age * 0.12 + k);
        out[2] = z + pz * spread * 3.2 - wz * (1.2 - Math.abs(spread) * 0.8);
    }

    static final double[] D = new double[3];

    /**
     * Muso Donuts and Chikara Mochi (awakened V): {@link #DONUTS} rings of mochi floating in an arc behind and over the user (x, y, z),
     * facing the target (tx, ty, tz); from each in turn ({@code every} ticks apart, from {@code first}) a giant fist (black with Armament)
     * on a mochi arm punches out to the target in {@link #DONUT_FIST} ticks and pulls back; {@code grow} 0 to 1 the rings grown in.
     */
    public static void donuts(FireMesh.Sink sink, double x, double y, double z, double tx, double ty, double tz, double age, double grow,
                              double first, double every, boolean coat, int seed) {
        if (grow < 0.02) {
            return;
        }
        double fx = tx - x;
        double fz = tz - z;
        double fl = Math.max(1.0E-6, Math.hypot(fx, fz));
        fx /= fl;
        fz /= fl;
        for (int k = 0; k < DONUTS; k++) {
            donut(x, y, z, fx, fz, k, age, D);
            double ax = tx - D[0];
            double ay = ty - D[1];
            double az = tz - D[2];
            double al = Math.max(1.0E-6, Math.sqrt(ax * ax + ay * ay + az * az));
            // 3.6.3: each one of the fruit's donuts (lumpy, dripping), its fist resting in its hole until it punches out of it
            double pa = age - first - k * every;
            double out = pa < 0 || pa > DONUT_FIST + 6 ? 0.0 : pa < DONUT_FIST ? ease(pa / DONUT_FIST) : 1.0 - smooth((pa - DONUT_FIST - 1) / 5.0);
            double reach = Math.max(0.5, al - 0.8) * out;
            donut(sink, D[0], D[1], D[2], ax / al, ay / al, az / al, MUSO, grow, 0.0, 3, out > 0.02 ? out : 0.0, D[0] + ax / al * reach,
                D[1] + ay / al * reach, D[2] + az / al * reach, coat, age, seed + k, k % 2 == 1);
        }
    }

    /**
     * Mochi Ginkaku (awakened B): a dome of mochi over (x, y, z) (the ground), {@code radius} round, rising out of the ground over its
     * first 10 ticks ({@code rise} 0 to 1), its surface lumpy and heaving; crushed in at its end ({@code crush} 0 to 1: it caves in).
     */
    public static void dome(FireMesh.Sink sink, double x, double y, double z, double radius, double rise, double crush, double age, int seed) {
        if (rise < 0.02) {
            return;
        }
        double h = radius * 0.9 * rise * (1.0 - 0.7 * crush);
        double r = radius * (1.0 - 0.35 * crush);
        // a whole blob, its lower half under the ground (the world hides it)
        MochiArt.blob(sink, x, y, z, 1, 0, 0, 0, 1, 0, r, h, r, 0.9, 0.05 + 0.08 * crush, age, seed, MochiArt.MOCHI, 1.0);
        // the pavilion: ridges of mochi from its foot over its top, a knob crowning it
        for (int k = 0; k < 8; k++) {
            double a = 2 * Math.PI * k / 8;
            MochiArt.tubeBegin();
            for (int j = 0; j <= 8; j++) {
                double up = Math.PI / 2 * j / 8;
                double rr = r * 1.03 * Math.cos(up);
                MochiArt.tubePoint(x + Math.cos(a) * rr, y + h * 1.03 * Math.sin(up), z + Math.sin(a) * rr, 0.22 * (1.0 - 0.5 * j / 8.0) * rise);
            }
            MochiArt.tube(sink, 8, MochiArt.MOCHI, 0.0, 0.0);
        }
        MochiArt.ball(sink, x, y + h + 0.35 * rise, z, 0.55 * rise, 0, 1, 0, 1.3, 0.0, age, seed, MochiArt.MOCHI);
    }

    static final double[] B = new double[6];
}
