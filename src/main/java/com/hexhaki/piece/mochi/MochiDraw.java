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
    public static final double BOOM_LIFE = 70.0;
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
    /** The blows in the air, the giant fist over the body, the fall, the ground. */
    public static final int[] LOTUS_AIR = {57, 67, 78};
    public static final int LOTUS_DROP = 87;
    public static final int LOTUS_FALL = 96;
    public static final int LOTUS_GROUND = 106;
    /** How high the one hit is thrown (blocks over the ground it stood on), how long the rise takes. */
    public static final double LOTUS_HIGH = 13.0;
    public static final double LOTUS_RISE = 10.0;
    /** The donut's outer radius (the reference's is about twice the user's height across) and where it stands from the user. */
    public static final double LOTUS_DONUT = 1.95;
    public static final double LOTUS_LEFT = 2.8;
    public static final double LOTUS_AHEAD = 0.9;
    /** An air blow's donut and its life, the giant fist's donut and its life (from the drop), the ground's life. */
    public static final double LOTUS_AIR_DONUT = 0.95;
    public static final double LOTUS_AIR_LIFE = 11.0;
    public static final double LOTUS_SLAM_DONUT = 2.3;
    public static final double LOTUS_SLAM_LIFE = 30.0;
    public static final double LOTUS_GROUND_LIFE = 40.0;

    /**
     * How high over the ground it stood on the one the Lotus combo hit is at {@code age} ticks into the combo (the server moves the body
     * so: server/MochiMoves.Juggled): on the ground to the launch, up {@link #LOTUS_HIGH} in {@link #LOTUS_RISE} ticks (fast, slowing),
     * hanging there bobbing a little, then driven down from {@link #LOTUS_FALL} to the ground at {@link #LOTUS_GROUND} (faster and faster).
     */
    public static double lotusHeight(double age) {
        if (age <= LOTUS_LAUNCH || age >= LOTUS_GROUND) {
            return 0.0;
        }
        if (age < LOTUS_LAUNCH + LOTUS_RISE) {
            return LOTUS_HIGH * ease((age - LOTUS_LAUNCH) / LOTUS_RISE);
        }
        if (age < LOTUS_FALL) {
            return LOTUS_HIGH + 0.25 * Math.sin((age - LOTUS_LAUNCH - LOTUS_RISE) * 0.25);
        }
        double f = (age - LOTUS_FALL) / (LOTUS_GROUND - LOTUS_FALL);
        double top = LOTUS_HIGH + 0.25 * Math.sin((LOTUS_FALL - LOTUS_LAUNCH - LOTUS_RISE) * 0.25);
        return top * (1.0 - f * f);
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
     * A Lotus blow in the air (the reference's frames 78 to 102): a small donut puffing up beside the body thrown high at (x, y, z),
     * {@code (sx, sy, sz)} (unit) the side it comes from, its fist jabbing into the body, a white burst with rings; {@code age} ticks in.
     */
    public static void lotusAir(FireMesh.Sink sink, double x, double y, double z, double sx, double sy, double sz, double age, boolean coat,
                                int seed) {
        if (age < 0 || age > LOTUS_AIR_LIFE) {
            return;
        }
        double d = 1.9;
        double cx = x + sx * d;
        double cy = y + sy * d;
        double cz = z + sz * d;
        double form = smooth((age + 1.0) / 2.5) * (1.0 - smooth((age - LOTUS_AIR_LIFE + 4.0) / 4.0));
        double out = age < 2.0 ? 0.0 : age < 3.0 ? ease(age - 2.0) : age < 5.0 ? 1.0 : 1.0 - smooth((age - 5.0) / 3.0);
        donut(sink, cx, cy, cz, -sx, -sy, -sz, LOTUS_AIR_DONUT, form, 0.0, 0, out > 0.02 ? out : 0.0, cx + (x - cx) * out * 0.85,
            cy + (y - cy) * out * 0.85, cz + (z - cz) * out * 0.85, coat, age, seed, false);
        if (age >= 2.5 && age < 9.0) {
            double f = (age - 2.5) / 6.5;
            MochiArt.glow(sink, x, y, z, 0.9 + 1.0 * ease(f), 0xFFFFFF, 0.85 * (1.0 - f));
            shockRings(sink, x, y, z, sx, sz, 0.7 + 1.6 * ease(f), 0.6 * (1.0 - f), seed + 3);
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
        if (age < CHESTNUT_FORM - 2.0) {
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
