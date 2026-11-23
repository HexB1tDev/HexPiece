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

    /** The wind-up ends; the leg's stretch is sent this many ticks before the impact (StretchStyle WHIP: windup 5 + extend 5). */
    public static final int WIND = 12;
    public static final int STRETCH_LEAD = 10;
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
     * The whip's smear (the user's hips at x y z, eye-relative), {@code age} ticks after the wind-up's end, the look's way ({@code fx},
     * {@code fz}, flat, unit), {@code reach} the leg's: bands of white arcs from the hips out to the reach following the leg round (it
     * turns as gear5_dawn_whip does: toward the user's left), the Haki-dark tip leading, fading out after the impact. About 600 quads.
     */
    public static void smear(FireMesh.Sink sink, double x, double y, double z, double age, double fx, double fz, double reach, int seed) {
        if (age < 0.0 || age > IMPACT - WIND + 8.0) {
            return;
        }
        double after = Math.max(0.0, age - (IMPACT - WIND));
        double fade = after <= 0.0 ? Math.min(1.0, age + 0.3) : Math.max(0.0, 1.0 - after / 8.0);
        double out = reach * (0.35 + 0.65 * release(age));
        // the leg's angle round the hips: the look's at the impact, coming from 640 degrees back (gear5_dawn_whip's yaw -280 to 360);
        // a turn toward the user's left is the angle going down here (the look at angle a, its left at a - 90 degrees)
        double look = Math.atan2(fz, fx);
        double yaw = Math.toRadians(-280.0 + 640.0 * release(age) + 70.0 * after);
        double tip = look - yaw;
        // the trail behind the tip (the tip's angle falls as it turns left: the trail lies at the angles above it): a cartoon smear, a
        // fan from the hips out to the leg's reach, bright at the leg and thinning out behind it (one layer: two translucent fans
        // over each other block each other in stripes)
        double sweep = Math.toRadians(150.0);
        fan(sink, x, y, z, out, tip, sweep, 0.0, 0.62 * fade);
        // the leg itself as the smear's leading edge: white, the Haki's dark violet along its front, a bright rim at the reach
        for (int k = 0; k < 2; k++) {
            double a = tip - 0.03 * k;
            ray(sink, x, y + 0.02 * k, z, a, out * 0.15, out, k == 0 ? 0.2 : 0.09, k == 0 ? WHITE : HAKI, (k == 0 ? 0.9 : 0.85) * fade);
        }
        flat(sink, x, y, z, out, 0.3, tip, sweep * 0.55, WHITE, 0.9 * fade);
        flat(sink, x, y, z, out + 0.18, 0.12, tip, sweep * 0.35, HAKI, 0.8 * fade);
        // speed lines flung off the tip
        for (int k = 0; k < 5; k++) {
            double a = tip + 0.12 * k;
            double r = out * (0.7 + 0.3 * FireMesh.rand(seed, k));
            FireMesh.ray(sink, x + Math.cos(a) * r, y + 0.25 * (FireMesh.rand(seed, k + 9) - 0.5), z + Math.sin(a) * r, Math.sin(a), 0,
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
        FireMesh.ring(sink, x, y + 0.05, z, 0, 1, 0, 0.8 + 0.25 * Math.sin(age), 0.5, 0.1, EARTH[6], 0.55 * fade);
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
        FireMesh.ring(sink, x, y + 0.08, z, 0, 1, 0, 0.6 + 2.8 * t, 0.5, 0.15, EARTH[7], 0.7 * fade);
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
