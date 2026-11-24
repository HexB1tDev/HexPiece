package com.hexhaki.piece.mochi;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.style.StyleRig;

/**
 * 3.6.4: the Mochi fruit's Awakening (M: the reference's DoughV2 kit, the owner's "Dough base + V2 awakened"; docs/handoff/next-ai-3.6.3.md
 * P reads each of its GIFs frame by frame), drawn as the house draws every fruit (pure: client/mochi/MochiFx and the previews,
 * tools/preview/MochiShots, call it with eye-relative points). The awakened Haki is the reference's magenta-pink with black ink.
 */
public final class MochiV2 {
    private MochiV2() {
    }

    /** The awakened Haki's colours (sampled from the reference): its white-pink light, its pink, its magenta, its deep violet; the ink. */
    public static final int PINK_LIGHT = 0xFFC8F6;
    public static final int PINK = 0xFF66EE;
    public static final int MAGENTA = 0xD23CF0;
    public static final int DEEP = 0x6E1E9E;
    public static final int INK = 0x0C0A12;

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

    // ------------------------------------------------------------------------------------------------------ M: the Awakening coming on

    /** How long the Awakening's burst lasts (ticks). */
    public static final double AWAKEN_LIFE = 18.0;

    /**
     * The Awakening coming on round the user's feet (x, y, z): dough splashing up round them, a magenta ring of Haki running out over
     * the ground, a pink glow and flares out of the chest; {@code age} ticks in.
     */
    public static void awaken(FireMesh.Sink sink, double x, double y, double z, double age, int seed) {
        if (age < 0 || age > AWAKEN_LIFE) {
            return;
        }
        double f = age / AWAKEN_LIFE;
        MochiDraw.splat(sink, x, y + 0.5, z, 0, 1, 0, 2.0, age, seed);
        FireMesh.ring(sink, x, y + 0.08, z, 0, 1, 0, 0.6 + 3.6 * ease(f * 1.4), 0.32, 0.08, MAGENTA, 0.85 * (1.0 - f));
        FireMesh.ring(sink, x, y + 0.1, z, 0, 1, 0, 0.4 + 2.6 * ease(f * 1.8), 0.16, 0.05, PINK_LIGHT, 0.75 * (1.0 - f));
        if (age < 9.0) {
            double g = age / 9.0;
            MochiArt.glow(sink, x, y + 1.1, z, 1.2 + 0.8 * ease(g), PINK, 0.6 * (1.0 - smooth(g)));
            for (int s = 0; s < 10; s++) {
                double a = 2 * Math.PI * (s + 0.3 * FireMesh.rand(seed, s)) / 10.0;
                double up = (FireMesh.rand(seed, s + 10) - 0.35) * 1.4;
                double ul = Math.sqrt(1.0 + up * up);
                FireMesh.ray(sink, x, y + 1.1, z, Math.cos(a) / ul, up / ul, Math.sin(a) / ul, (0.8 + 1.4 * FireMesh.rand(seed, s + 20)) * ease(g * 2.0),
                    0.14, 0, 0.0, s % 2 == 0 ? PINK : MAGENTA, 0.9 * (1.0 - g));
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ LMB: Trident Lacerations

    /**
     * A swing (the reference: "User wields Katakuri's trident and swings it 5 times with the last hit bypassing"): the clips
     * mochi_trident_1 to _5 (tools/mochi_anims.py) run {@link #TRIDENT_SWING} ticks and strike on {@link #TRIDENT_HIT}; a swing's
     * marks last {@link #TRIDENT_LIFE}.
     */
    public static final int TRIDENT_SWINGS = 5;
    public static final int TRIDENT_SWING = 10;
    public static final int TRIDENT_HIT = 3;
    public static final double TRIDENT_LIFE = 16.0;
    /** The ticks of a swing's clip its trail is drawn over (its sweep: from its wind-up to its follow-through). */
    public static final double[][] TRIDENT_SWEEP = {{1.0, 5.0}, {1.0, 5.0}, {1.0, 7.0}, {1.0, 5.0}, {1.0, 5.5}};

    /**
     * Mogura in the right arm's frame (model px: the shoulder's pivot at 0, y down the arm, -z its front), held as the game holds a tool:
     * in the fist at ({@link #GRIP_X}, {@link #GRIP_Y}, {@link #GRIP_Z}), its shaft out of the fist's front, tipped {@link #TILT}
     * degrees down the arm, {@link #BACK} px behind the fist, the crossbar {@link #CROSS} px ahead of it, the middle prong's tip
     * {@link #TIP} px. client/mochi/MochiClient draws it on the player's own arm; the trails follow the same points.
     */
    public static final double GRIP_X = -1.0;
    public static final double GRIP_Y = 9.5;
    public static final double GRIP_Z = 0.0;
    public static final double TILT = 5.0;
    /** 3.6.7 (the owner: "should not be held at the tip of the rod it it should be in the middle of the hand"): gripped at its middle. */
    public static final double BACK = 20.0;
    public static final double CROSS = 21.0;
    public static final double TIP = 32.0;
    /**
     * 3.6.7 (the owner: "should disappear when not using M1s, and manist from a mochi only when using M1s"): Mogura forms out of mochi in
     * the hand as the swings begin ({@link #MOGURA_FORM} ticks), stays while they go on and {@link #MOGURA_LINGER} ticks after the last
     * one's clip ends, then goes back into mochi ({@link #MOGURA_GONE}).
     */
    public static final double MOGURA_FORM = 2.5;
    public static final double MOGURA_LINGER = 10.0;
    public static final double MOGURA_GONE = 5.0;

    /** How much of Mogura is out (0 to 1, past 1 a little as it springs) {@code first} ticks since the swings began, {@code last} since the last one began. */
    public static double moguraForm(double first, double last) {
        if (first < 0 || last < 0) {
            return 0.0;
        }
        double gone = 1.0 - smooth((last - TRIDENT_SWING - MOGURA_LINGER) / MOGURA_GONE);
        return MochiDraw.springy(first / MOGURA_FORM) * gone;
    }

    /**
     * The mochi Mogura forms out of and goes back into, along its shaft from the butt (bx, by, bz) to the tip (tx, ty, tz) (eye-relative,
     * as far as it is out now): a lumpy tube of dough over it, thickest while it is half formed ({@code form}), a lump in the hand.
     */
    public static void moguraMochi(FireMesh.Sink sink, double bx, double by, double bz, double tx, double ty, double tz, double hx, double hy,
                                   double hz, double form, double age, int seed) {
        double wrap = Math.sin(Math.PI * Math.max(0.0, Math.min(1.0, form)));
        if (wrap < 0.03) {
            return;
        }
        MochiArt.tubeBegin();
        for (int k = 0; k <= 6; k++) {
            double t = k / 6.0;
            double bulge = 0.5 + 0.5 * Math.sin(Math.PI * t);
            MochiArt.tubePoint(bx + (tx - bx) * t, by + (ty - by) * t, bz + (tz - bz) * t, (0.05 + 0.09 * bulge) * wrap);
        }
        MochiArt.tube(sink, 8, MochiArt.MOCHI, 0.0, age * 0.3);
        MochiArt.ball(sink, hx, hy, hz, 0.2 * wrap, 0, 1, 0, 1.1, 0.4, age, seed, MochiArt.MOCHI);
    }

    /**
     * The point {@code px} px along Mogura from the grip (negative: behind it), {@code side} px across toward the prongs' spread (the arm's
     * x), for a sampled clip {@code pose} (client/npc/StylePoser), in the player's frame (StyleRig: x to their left, y up, z ahead,
     * blocks, the feet at the origin); into out.
     */
    public static double[] mogura(float[][] pose, double px, double side, double[] out) {
        double t = Math.toRadians(TILT);
        double y = GRIP_Y + px * Math.sin(t);
        double z = GRIP_Z - px * Math.cos(t);
        // StyleRig's limb: along the box from its top (y -2) to its end (y 10); dx off the box's middle (x -1)
        double along = (y + 2.0) / 12.0;
        double[] p = StyleRig.limb(pose, StyleRig.RIGHT_ARM, GRIP_X + 1.0 + side, along, z);
        out[0] = p[0];
        out[1] = p[1];
        out[2] = p[2];
        return out;
    }

    /**
     * The trail of a swing (the reference's frames 15 to 66: magenta crescents and black ink brush strokes): a ribbon between the
     * points {@code inner} and {@code outer} (x, y, z triples, eye-relative, {@code n} of them, oldest first) Mogura's shaft swept
     * through, thickest in the middle of its sweep, clear at its old end, {@code alpha} of it; {@code ink} draws it as the reference's
     * black brush strokes edged in magenta, else a magenta crescent with a white-pink core.
     */
    public static void tridentTrail(FireMesh.Sink sink, double[] inner, double[] outer, int n, boolean ink, double alpha) {
        if (n < 2 || alpha < 0.02) {
            return;
        }
        if (ink) {
            // the reference's frames 39 and 42: one dense black crescent of ink, its outer edge ragged like a dry brush, a magenta edge
            // and a white-pink line inside it
            band(sink, inner, outer, n, 0.34, 0.42, PINK_LIGHT, 0.85 * alpha, 0.0);
            band(sink, inner, outer, n, 0.40, 0.54, MAGENTA, alpha, 0.0);
            band(sink, inner, outer, n, 0.52, 0.98, INK, alpha, 0.0);
            band(sink, inner, outer, n, 0.94, 1.07, INK, 0.9 * alpha, 0.5);
        } else {
            // the reference's frames 18, 30 and 66: a thin bold crescent of neon magenta, pink over it, a white-hot outer edge
            band(sink, inner, outer, n, 0.56, 1.05, MAGENTA, 0.95 * alpha, 0.0);
            band(sink, inner, outer, n, 0.70, 1.01, PINK, alpha, 0.0);
            band(sink, inner, outer, n, 0.86, 0.99, PINK_LIGHT, alpha, 0.0);
        }
    }

    /**
     * One band of a trail: between {@code from} and {@code to} of the way from each inner point to its outer one (narrowing to nothing at
     * both ends of the sweep), {@code rgb}, {@code alpha} at its newest end; {@code ragged} (0 to 1) breaks it into a brush's dry streaks.
     */
    static void band(FireMesh.Sink sink, double[] inner, double[] outer, int n, double from, double to, int rgb, double alpha, double ragged) {
        double mid = (from + to) * 0.5;
        double half = (to - from) * 0.5;
        for (int k = 0; k + 1 < n; k++) {
            double f0 = k / (double) (n - 1);
            double f1 = (k + 1) / (double) (n - 1);
            if (ragged > 0.0 && FireMesh.rand(n * 31 + (int) (from * 100), k) < ragged * 0.5) {
                continue;
            }
            double w0 = half * Math.sin(Math.PI * Math.min(1.0, f0 * 0.9 + 0.1));
            double w1 = half * Math.sin(Math.PI * Math.min(1.0, f1 * 0.9 + 0.1));
            int a = k * 3;
            int b = (k + 1) * 3;
            double a0x = lerp(inner[a], outer[a], mid - w0);
            double a0y = lerp(inner[a + 1], outer[a + 1], mid - w0);
            double a0z = lerp(inner[a + 2], outer[a + 2], mid - w0);
            double a1x = lerp(inner[a], outer[a], mid + w0);
            double a1y = lerp(inner[a + 1], outer[a + 1], mid + w0);
            double a1z = lerp(inner[a + 2], outer[a + 2], mid + w0);
            double b0x = lerp(inner[b], outer[b], mid - w1);
            double b0y = lerp(inner[b + 1], outer[b + 1], mid - w1);
            double b0z = lerp(inner[b + 2], outer[b + 2], mid - w1);
            double b1x = lerp(inner[b], outer[b], mid + w1);
            double b1y = lerp(inner[b + 1], outer[b + 1], mid + w1);
            double b1z = lerp(inner[b + 2], outer[b + 2], mid + w1);
            int c0 = FireMesh.argb(alpha * Math.pow(f0, 0.6), rgb);
            int c1 = FireMesh.argb(alpha * Math.pow(f1, 0.6), rgb);
            FireMesh.quad(sink, a0x, a0y, a0z, c0, a1x, a1y, a1z, c0, b1x, b1y, b1z, c1, b0x, b0y, b0z, c1);
        }
    }

    static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /**
     * Where swing {@code k}'s Mogura struck (x, y, z), {@code age} ticks after its blow (the reference): a thrust or the 4th blow a jagged
     * magenta burst with black specks (frames 15, 54); the 2nd a white star spark in a ring of magenta (frame 30); the 5th, the launch,
     * a white ring on the ground under the one thrown up and white streaks of speed rising after them (frames 66 to 78); {@code ground}
     * the ground's height there.
     */
    public static void tridentMark(FireMesh.Sink sink, double x, double y, double z, int k, double ground, double age, int seed) {
        if (age < 0 || age > TRIDENT_LIFE) {
            return;
        }
        double f = age / TRIDENT_LIFE;
        if (k == 0 || k == 3) {
            // the jagged burst: flat-ish spikes of magenta and pink flung out round the point, short black specks, a soft glow
            double grow = ease(age / 3.0);
            double fade = 1.0 - smooth((age - 3.0) / 7.0);
            if (fade > 0.01) {
                MochiArt.glow(sink, x, y, z, 1.4 * grow + 0.2, PINK, 0.55 * fade);
                for (int s = 0; s < 14; s++) {
                    double a = 2 * Math.PI * (s + 0.4 * FireMesh.rand(seed, s)) / 14.0;
                    double up = (FireMesh.rand(seed, s + 20) - 0.5) * 1.6;
                    double ul = Math.sqrt(1.0 + up * up);
                    double len = (0.7 + 0.9 * FireMesh.rand(seed, s + 40)) * grow;
                    FireMesh.ray(sink, x, y, z, Math.cos(a) / ul, up / ul, Math.sin(a) / ul, len, 0.16 + 0.12 * FireMesh.rand(seed, s + 60), 0, 0.0,
                        s % 3 == 0 ? PINK_LIGHT : s % 3 == 1 ? PINK : MAGENTA, 0.95 * fade);
                }
                for (int s = 0; s < 8; s++) {
                    double a = 2 * Math.PI * FireMesh.rand(seed, s + 80);
                    double up = (FireMesh.rand(seed, s + 90) - 0.3) * 1.2;
                    double d = (0.4 + 1.3 * FireMesh.rand(seed, s + 100)) * grow;
                    double ul = Math.sqrt(1.0 + up * up);
                    FireMesh.streak(sink, x + Math.cos(a) / ul * d, y + up / ul * d, z + Math.sin(a) / ul * d, Math.cos(a), up, Math.sin(a), 0.16, 0.05, INK,
                        0.9 * fade);
                }
            }
        }
        if (k == 1 || k == 0) {
            star(sink, x, y, z, 1.1, age, 6.0);
        }
        if (k == 4) {
            // the launch: a white ring opening on the ground, white streaks of speed rising behind the one thrown up
            FireMesh.ring(sink, x, ground + 0.08, z, 0, 1, 0, 0.6 + 2.6 * ease(f * 1.6), 0.22, 0.05, 0xFFFFFF, 0.8 * (1.0 - f));
            FireMesh.ring(sink, x, ground + 0.1, z, 0, 1, 0, 0.4 + 1.6 * ease(f * 2.2), 0.14, 0.04, PINK, 0.6 * (1.0 - f));
            double rise = 1.7 * age;
            for (int s = 0; s < 7; s++) {
                double a = 2 * Math.PI * (s + FireMesh.rand(seed, s + 120)) / 7.0;
                double r = 0.25 + 0.35 * FireMesh.rand(seed, s + 130);
                double len = 1.5 + 2.5 * FireMesh.rand(seed, s + 140);
                double h = y + rise * (0.6 + 0.4 * FireMesh.rand(seed, s + 150));
                FireMesh.streak(sink, x + Math.cos(a) * r, h - len, z + Math.sin(a) * r, 0, 1, 0, len, 0.05, 0xFFFFFF, 0.85 * (1.0 - f));
            }
            star(sink, x, y, z, 0.9, age, 5.0);
        }
    }

    /** A white star spark (the reference's flares): four thin white rays crossed and a white core, {@code size} long, gone by {@code life}. */
    static void star(FireMesh.Sink sink, double x, double y, double z, double size, double age, double life) {
        if (age > life) {
            return;
        }
        double fade = 1.0 - age / life;
        double s = size * (0.6 + 0.4 * ease(age / 2.0));
        MochiArt.glow(sink, x, y, z, 0.35 * s, 0xFFFFFF, 0.9 * fade);
        FireMesh.ray(sink, x, y, z, 1, 0, 0, s, 0.05, 0, 0.0, 0xFFFFFF, fade);
        FireMesh.ray(sink, x, y, z, -1, 0, 0, s, 0.05, 0, 0.0, 0xFFFFFF, fade);
        FireMesh.ray(sink, x, y, z, 0, 1, 0, s * 1.2, 0.05, 0, 0.0, 0xFFFFFF, fade);
        FireMesh.ray(sink, x, y, z, 0, -1, 0, s * 1.2, 0.05, 0, 0.0, 0xFFFFFF, fade);
    }

    /**
     * The 3rd swing's spin (the reference's frames 27 and 63): a spiral of black ink brush strokes on the ground round the user's feet
     * (x, y, z), a magenta ring inside it, whirled the way the spin went; {@code age} ticks from the swing's start.
     */
    public static void tridentSpin(FireMesh.Sink sink, double x, double y, double z, double yaw, double age, int seed) {
        if (age < 1.0 || age > TRIDENT_LIFE + 4.0) {
            return;
        }
        double form = ease((age - 1.0) / 5.0);
        double fade = 1.0 - smooth((age - 8.0) / (TRIDENT_LIFE - 4.0));
        if (fade < 0.02) {
            return;
        }
        for (int s = 0; s < 7; s++) {
            double r = 1.1 + 0.32 * s + 0.2 * FireMesh.rand(seed, s);
            double sweep = (3.2 + 1.4 * FireMesh.rand(seed, s + 10)) * form;
            double from = yaw + 2 * Math.PI * FireMesh.rand(seed, s + 20);
            MochiDraw.spiral(sink, x, y + 0.05 + 0.006 * s, z, r * 0.85, r * 1.1, from, sweep, 0.16 + 0.22 * FireMesh.rand(seed, s + 30), INK,
                0.92 * fade);
        }
        MochiDraw.spiral(sink, x, y + 0.12, z, 0.85, 0.95, yaw, 4.6 * form, 0.22, MAGENTA, 0.9 * fade);
        MochiDraw.spiral(sink, x, y + 0.13, z, 0.9, 1.0, yaw + 0.6, 3.6 * form, 0.1, PINK_LIGHT, 0.8 * fade);
    }

    // ------------------------------------------------------------------------------------------------------ V: Power Mochi

    /**
     * Power Mochi (the reference's Power Dough, frames 15 to 155: "two dough donuts with coated haki flaming fists for ~20 seconds that
     * will act as your new and improved M1s with increased range"): {@link #POWER_TICKS} ticks; formed by {@link #POWER_FORM} (a splash of
     * cream at each side, two fireballs, the donuts out of them); each punch's fist out in {@link #POWER_OUT}, back by
     * {@link #POWER_LIFE}; its reach.
     */
    public static final int POWER_TICKS = 400;
    public static final double POWER_FORM = 12.0;
    public static final double POWER_GONE = 8.0;
    public static final double POWER_OUT = 2.0;
    public static final double POWER_LIFE = 10.0;
    public static final double POWER_REACH = 7.5;
    /** A donut's outer radius, and where it hangs from the feet: out to the side, up, back. */
    public static final double POWER_DONUT = 0.9;
    public static final double POWER_SIDE = 1.4;
    public static final double POWER_UP = 1.75;
    public static final double POWER_BACK = 0.0;
    static final int FIRE = 0xFF7A1E;
    static final int FIRE_GLOW = 0xFF5A28;
    static final int SPARK_RED = 0xE0101E;

    /**
     * Where Power Mochi's donut {@code side} (0 the right, 1 the left) hangs from the feet of a body facing {@code yaw} (the game's yaw,
     * radians), bobbing {@code age} ticks in; into out (x, y, z).
     */
    public static double[] powerAt(int side, double yaw, double age, double[] out) {
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        // the right: (-cos, -sin) of the game's yaw
        double rx = -Math.cos(yaw);
        double rz = -Math.sin(yaw);
        double s = side == 0 ? 1.0 : -1.0;
        double bob = 0.08 * Math.sin(age * 0.16 + side * 2.1);
        out[0] = rx * POWER_SIDE * s - fx * POWER_BACK;
        out[1] = POWER_UP + bob;
        out[2] = rz * POWER_SIDE * s - fz * POWER_BACK;
        return out;
    }

    /**
     * One of Power Mochi's donuts at (x, y, z) facing (wx, wy, wz) (unit: the way its fist punches), {@code age} ticks from the press,
     * {@code gone} ticks since it ended (negative while it lasts), {@code out} its fist out of it as a punch (0 resting in its hole): a
     * splash of cream (frame 15), a fireball (20), the lumpy white donut out of it (25 to 155): a soft red-orange glow round its outline,
     * its hole filled from behind by the dough its black Haki fist grows out of (seen from behind the hole is pale, frame 30), small
     * flames licking over it; going, the same backward into a puff of fire.
     */
    public static void powerDonut(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double age, double gone,
                                  double out, int seed) {
        if (age < 0) {
            return;
        }
        if (age < 6.0) {
            MochiDraw.splat(sink, x, y - 0.3, z, 0, 1, 0, 1.3, age, seed);
        }
        // the fireball the donut comes out of (frame 20): fire swelling and bursting, arcs of fire round it
        if (age >= 3.0 && age < 11.0) {
            double f = (age - 3.0) / 8.0;
            double r = 0.5 + 0.9 * ease(f * 1.6);
            double fade = 1.0 - smooth((f - 0.45) / 0.55);
            FireMesh.puff(sink, x, y, z, r, 0, 1, 0, 0.1, 0.6, 0.85, 0.5, true, fade, age, seed);
            for (int k = 0; k < 3; k++) {
                FireMesh.arc(sink, x, y, z, Math.cos(k * 2.1 + seed) / 1.118, 0.5 / 1.118, Math.sin(k * 2.1 + seed) / 1.118, r * (1.1 + 0.25 * k), 0.12,
                    k * 2.0 + age * 0.4, 2.2, 0.2, FIRE, 0.8 * fade);
            }
        }
        double form = smooth((age - 7.0) / 5.0) * (gone < 0 ? 1.0 : 1.0 - smooth(gone / POWER_GONE));
        if (form < 0.02) {
            if (gone >= 0 && gone < POWER_GONE) {
                FireMesh.puff(sink, x, y, z, 0.5 * (1.0 - gone / POWER_GONE), 0, 1, 0, 0.2, 0.6, 0.7, 0.5, true, 1.0 - gone / POWER_GONE, age, seed + 5);
            }
            return;
        }
        double big = POWER_DONUT * MochiDraw.springy(form);
        // the soft red-orange glow round its outline (frames 25 to 155): a halo turned to the eye just behind the donut, clear inside
        // its outline (hidden behind the dough) and fading out past it; brighter as its fist goes
        halo(sink, x, y, z, big, 0.42 * form + 0.25 * Math.min(1.0, out));
        // the ring alone (its own fist drawn here, further out of the hole), the dough swelling round the hole as a punch pushes out
        MochiDraw.donut(sink, x, y, z, wx, wy, wz, POWER_DONUT, form, 0.0, 2, out > 0.0 ? Math.min(1.0, out) : 1.0, x, y, z, true, age, seed, false);
        double hole = MochiDraw.hole(big);
        double thick = big * MochiDraw.DONUT_THICK;
        if (out <= 0.0) {
            // the black Haki fist in the hole, its knuckles out of the front (the punch's bar is it while a punch is out)
            double kx = -wz;
            double kz = wx;
            double kl = Math.max(1.0E-6, Math.sqrt(kx * kx + kz * kz));
            MochiArt.fist(sink, x + wx * thick * 0.6, y + wy * thick * 0.6, z + wz * thick * 0.6, wx, wy, wz, kx / kl, 0, kz / kl, hole * 0.84, true, age,
                seed, false);
        }
        // the dough filling its hole behind the fist (the fist grows out of it): from behind the hole reads pale, never black
        double back = thick * 1.2;
        MochiArt.ball(sink, x - wx * back, y - wy * back, z - wz * back, hole * 1.06, wx, wy, wz, 0.6, 0.25, age, seed + 9, MochiArt.MOCHI);
        for (int k = 0; k < 3; k++) {
            double a = 2 * Math.PI * k / 3.0 + age * 0.05;
            double h = (age * 0.11 + k * 0.33) % 1.0;
            double[] b = B6;
            FireMesh.basis(wx, wy, wz, b);
            double ox = b[0] * Math.cos(a) + b[3] * Math.sin(a);
            double oy = b[1] * Math.cos(a) + b[4] * Math.sin(a);
            double oz = b[2] * Math.cos(a) + b[5] * Math.sin(a);
            double r = big * 0.9;
            FireMesh.puff(sink, x + ox * r, y + oy * r + 0.25 * h, z + oz * r, 0.12 * (1.0 - h) * form, 0, 1, 0, 0.6, 0.6, 0.55, 0.5, false,
                0.85 * (1.0 - h), age, seed + k);
        }
    }

    /**
     * A soft glow round the outline of something {@code big} across at (x, y, z) (eye-relative): two rings turned to the eye a little
     * behind it, clear inside (where the thing hides them) and fading out past its outline, orange inside, red further out.
     */
    static void halo(FireMesh.Sink sink, double x, double y, double z, double big, double alpha) {
        halo(sink, x, y, z, big, FIRE_GLOW, SPARK_RED, alpha);
    }

    /** As {@link #halo}, its inner and outer colours. */
    static void halo(FireMesh.Sink sink, double x, double y, double z, double big, int inner, int outer, double alpha) {
        double d = Math.sqrt(x * x + y * y + z * z);
        if (d < big * 1.5 || alpha < 0.01) {
            return;
        }
        double ex = -x / d;
        double ey = -y / d;
        double ez = -z / d;
        double back = big * 0.45;
        double cx = x - ex * back;
        double cy = y - ey * back;
        double cz = z - ez * back;
        FireMesh.ring(sink, cx, cy, cz, ex, ey, ez, big * 1.1, big * 0.75, 0.0, inner, alpha);
        FireMesh.ring(sink, cx, cy, cz, ex, ey, ez, big * 1.35, big * 1.1, 0.0, outer, alpha * 0.45);
    }

    private static final double[] B6 = new double[6];
    private static final double[] SEAM = new double[4];
    private static final double[] SEAM2 = new double[4];

    /**
     * A Power Mochi punch (frames 52 to 76): from the donut at (x, y, z) its fist shoots out onto (tx, ty, tz) as a long thick black
     * square Haki bar (as wide as the donut's hole) with seams of fire glowing orange along it and flames licking off it near the donut,
     * a fiery burst at the donut as it goes (frame 52 to 55: a starburst of yellow and orange flung along the punch, a ring of fire,
     * crimson splinters), a white splash of dough round the fist where it lands, a burst of fire and red sparks; {@code age} ticks in.
     */
    public static void powerPunch(FireMesh.Sink sink, double x, double y, double z, double tx, double ty, double tz, double age, int seed) {
        if (age < 0 || age > POWER_LIFE) {
            return;
        }
        double dx = tx - x;
        double dy = ty - y;
        double dz = tz - z;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.3) {
            return;
        }
        double ux = dx / len;
        double uy = dy / len;
        double uz = dz / len;
        double out = powerOut(age);
        double d = len * out;
        // the bar as wide as the donut's hole (frame 72), the fist a little wider
        double r = MochiDraw.hole(POWER_DONUT);
        double size = r * 1.2;
        double ex = x + ux * d;
        double ey = y + uy * d;
        double ez = z + uz * d;
        double[] b = B6;
        FireMesh.basis(ux, uy, uz, b);
        if (d > 0.2) {
            MochiDraw.arm(sink, x, y, z, ex, ey, ez, r, size, 0.0, true, age, seed, false);
            // seams of fire glowing along the bar (frames 68 to 76): one along each face by its edge, broken and kinked, white-hot to
            // orange, riding its wobble; a few cracks across it
            double glow = age < POWER_OUT + 3.0 ? 1.0 : out;
            double across = -uz;
            double acrossZ = ux;
            double al = Math.max(1.0E-6, Math.sqrt(across * across + acrossZ * acrossZ));
            across /= al;
            acrossZ /= al;
            double vx = -acrossZ * uy;
            double vy = acrossZ * ux - across * uz;
            double vz = across * uy;
            for (int k = 0; k < 7; k++) {
                int face = k % 4;
                double fa = face == 0 ? 1 : face == 2 ? -1 : 0;
                double fv = face == 1 ? 1 : face == 3 ? -1 : 0;
                boolean cross = k >= 4;
                double edge = (FireMesh.rand(seed, k + 60) < 0.5 ? -1.0 : 1.0) * (0.45 + 0.2 * FireMesh.rand(seed, k + 65));
                double t0 = cross ? 0.15 + 0.7 * FireMesh.rand(seed, k + 70) : 0.04 + 0.08 * FireMesh.rand(seed, k + 70);
                double t1 = cross ? t0 + 0.03 : 0.9 + 0.07 * FireMesh.rand(seed, k + 80);
                int dashes = cross ? 1 : 5;
                for (int j = 0; j < dashes; j++) {
                    double ta = t0 + (t1 - t0) * (j + (cross ? 0.0 : 0.08)) / dashes;
                    double tb = t0 + (t1 - t0) * (j + (cross ? 1.0 : 0.92)) / dashes;
                    MochiDraw.armAt(x, y, z, ex, ey, ez, r, size, 0.0, true, age, seed, ta, SEAM);
                    MochiDraw.armAt(x, y, z, ex, ey, ez, r, size, 0.0, true, age, seed, tb, SEAM2);
                    double oa = cross ? -0.65 : edge + (FireMesh.rand(seed, k * 7 + j) - 0.5) * 0.3;
                    double ob = cross ? 0.65 : edge + (FireMesh.rand(seed, k * 7 + j + 1) - 0.5) * 0.3;
                    // on the face (its normal n = a fa + v fv) at r, across it (t = a fv + v fa) by o r
                    double ra = SEAM[3] * 1.04;
                    double rb = SEAM2[3] * 1.04;
                    double nx = across * fa + vx * fv;
                    double ny = vy * fv;
                    double nz = acrossZ * fa + vz * fv;
                    double gx = across * fv + vx * fa;
                    double gy = vy * fa;
                    double gz = acrossZ * fv + vz * fa;
                    double px = SEAM[0] + (nx + gx * oa) * ra;
                    double py = SEAM[1] + (ny + gy * oa) * ra;
                    double pz = SEAM[2] + (nz + gz * oa) * ra;
                    double qx = SEAM2[0] + (nx + gx * ob) * rb;
                    double qy = SEAM2[1] + (ny + gy * ob) * rb;
                    double qz = SEAM2[2] + (nz + gz * ob) * rb;
                    double sl = Math.sqrt((qx - px) * (qx - px) + (qy - py) * (qy - py) + (qz - pz) * (qz - pz));
                    // a streak's head is a quarter of its length past its point: put its point there so it runs p to q
                    FireMesh.streak(sink, px + (qx - px) * 0.75, py + (qy - py) * 0.75, pz + (qz - pz) * 0.75, qx - px, qy - py, qz - pz, sl, 0.04,
                        j % 2 == 0 ? FIRE : 0xFFB020, 0.95 * glow);
                }
            }
            // flames licking off the bar near the donut (frame 68), streaming back
            int n = Math.min(8, 3 + (int) (d * 0.9));
            for (int k = 0; k < n; k++) {
                double t = 0.03 + 0.4 * (k + 0.5) / n;
                MochiDraw.armAt(x, y, z, ex, ey, ez, r, size, 0.0, true, age, seed, t, SEAM);
                double a = k * 2.3 + age * 0.7 + seed;
                double ox = b[0] * Math.cos(a) + b[3] * Math.sin(a);
                double oy = b[1] * Math.cos(a) + b[4] * Math.sin(a);
                double oz = b[2] * Math.cos(a) + b[5] * Math.sin(a);
                double rr = SEAM[3] * 1.15;
                FireMesh.puff(sink, SEAM[0] + ox * rr, SEAM[1] + oy * rr, SEAM[2] + oz * rr, 0.16 + 0.14 * (1.0 - t), -ux + ox * 0.6, -uy + oy * 0.6,
                    -uz + oz * 0.6, 1.0, 0.7, 0.42, 0.5, true, 0.85 * glow * (1.0 - t), age, seed + k);
            }
        }
        // right by the eye (one's own first person) the burst is drawn smaller, so it never buries the view on every punch
        double near = 0.45 + 0.55 * Math.max(0.0, Math.min(1.0, (Math.sqrt(x * x + y * y + z * z) - 1.5) / 4.0));
        // the fiery burst at the donut as the fist goes (frames 52 to 56, a tick a frame): a bright yellow fireball over the donut with
        // rays flung out of it (52), then wide thick crescents of fire round it on either side like brackets, opening out and fading
        // (53 to 56), crimson splinters sprayed all round
        if (age < 7.0) {
            double fireball = 1.0 - smooth((age - 0.8) / 1.4);
            if (fireball > 0.02) {
                // round the donut, engulfing it (frame 52: the donut lost in it)
                FireMesh.puff(sink, x + ux * 0.2, y + uy * 0.2, z + uz * 0.2, POWER_DONUT * (1.3 + 0.5 * ease(age / 1.5)) * near, 0, 1, 0, 0.1, 0.45, 0.86,
                    0.5,
                    false, 0.95 * fireball, age, seed + 20);
            }
            double rays = 1.0 - smooth((age - 1.0) / 3.0);
            for (int k = 0; k < 16; k++) {
                // most rays thrown forward along the punch in a cone, a few out to the sides
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 21);
                double cone = k < 10 ? 0.15 + 0.5 * FireMesh.rand(seed, k + 31) : 0.9 + 0.8 * FireMesh.rand(seed, k + 31);
                double ox = (b[0] * Math.cos(a) + b[3] * Math.sin(a)) * cone;
                double oy = (b[1] * Math.cos(a) + b[4] * Math.sin(a)) * cone;
                double oz = (b[2] * Math.cos(a) + b[5] * Math.sin(a)) * cone;
                double ol = Math.sqrt(1.0 + cone * cone);
                double reach = (k < 10 ? 1.8 + 2.4 * FireMesh.rand(seed, k + 41) : 1.0 + 1.2 * FireMesh.rand(seed, k + 41)) * (0.6 + 0.4 * ease(age / 2.0)) * near;
                FireMesh.ray(sink, x, y, z, (ux + ox) / ol, (uy + oy) / ol, (uz + oz) / ol, reach, 0.12, 0, 0.0, k % 3 == 0 ? 0xFFF29A : k % 3 == 1
                    ? 0xFFD23C : FIRE, 0.9 * rays);
            }
            // the crescents: brackets of fire either side of the donut round the punch's way, a few strands each, opening out
            double open = smooth((age - 0.6) / 1.2) * (1.0 - smooth((age - 2.5) / 4.0));
            if (open > 0.02) {
                for (int k = 0; k < 4; k++) {
                    double side = k % 2 == 0 ? 0.0 : Math.PI;
                    double rad = POWER_DONUT * (2.2 + 0.7 * (k / 2) + 0.2 * FireMesh.rand(seed, k + 110)) * (0.75 + 0.35 * ease((age - 0.6) / 4.0)) * near;
                    double sweep = 2.2 + 0.6 * FireMesh.rand(seed, k + 115);
                    double start = side - sweep * 0.5 + (FireMesh.rand(seed, k + 120) - 0.5) * 0.5 + 0.08 * age;
                    double w = (k < 2 ? 0.8 : 0.55) * near;
                    // a thick band of orange, its hot yellow heart along its middle
                    FireMesh.arc(sink, x, y, z, ux, uy, uz, rad, w, start, sweep, 0.35, 0xF5651A, 0.8 * open);
                    FireMesh.arc(sink, x, y, z, ux, uy, uz, rad + w * 0.12, w * 0.5, start + 0.12, sweep - 0.24, 0.35, 0xFFD52B, 0.9 * open);
                }
            }
            double red = 1.0 - smooth((age - 2.0) / 5.0);
            for (int k = 0; k < 18; k++) {
                double a = 2 * Math.PI * (k + 0.6 * FireMesh.rand(seed, k + 90)) / 18.0;
                double ox = b[0] * Math.cos(a) + b[3] * Math.sin(a);
                double oy = b[1] * Math.cos(a) + b[4] * Math.sin(a);
                double oz = b[2] * Math.cos(a) + b[5] * Math.sin(a);
                double s = POWER_DONUT * (0.6 + (1.0 + 1.4 * FireMesh.rand(seed, k + 95)) * ease((age + 0.5) / 2.5)) * near;
                double fw = (FireMesh.rand(seed, k + 99) - 0.3) * 0.8;
                FireMesh.streak(sink, x + ox * s + ux * fw, y + oy * s + uy * fw, z + oz * s + uz * fw, ox + ux * fw, oy + uy * fw, oz + uz * fw,
                    (0.6 + 0.8 * FireMesh.rand(seed, k + 97)) * near, 0.04 * near, SPARK_RED, 0.9 * red);
            }
        }
        // the blow: a white splash of dough round the fist where it lands (frames 70 to 76), a burst of fire and red sparks
        double hit = age - POWER_OUT;
        if (hit >= 0.0 && hit < 7.0) {
            double f = hit / 7.0;
            // the fist caught in a puff of white dough as it lands (it swells over the fist, then sinks back into it)
            double cap = Math.sin(Math.PI * Math.min(1.0, hit / 6.0));
            if (cap > 0.05) {
                MochiArt.ball(sink, ex + ux * size * 0.2, ey + uy * size * 0.2, ez + uz * size * 0.2, size * (0.85 + 0.55 * cap), ux, uy, uz, 0.8, 0.45, age,
                    seed + 34, MochiArt.MOCHI);
            }
            FireMesh.puff(sink, tx - ux * 0.3, ty - uy * 0.3, tz - uz * 0.3, 0.3 + 0.35 * ease(f), 0, 1, 0, 0.2, 0.7, 0.55, 0.5, true, 0.8 * (1.0 - f),
                age, seed + 30);
            for (int k = 0; k < 8; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 40);
                double up = (FireMesh.rand(seed, k + 50) - 0.3) * 1.6;
                double ul = Math.sqrt(1.0 + up * up);
                FireMesh.ray(sink, tx, ty, tz, Math.cos(a) / ul, up / ul, Math.sin(a) / ul, 0.5 + 1.3 * ease(f), 0.06, 0, 0.0, SPARK_RED, 0.9 * (1.0 - f));
            }
        }
    }

    /** How far a Power Mochi punch's fist is out {@code age} ticks in (0 to 1, past 1 as it springs past its reach). */
    public static double powerOut(double age) {
        if (age < 0 || age > POWER_LIFE) {
            return 0.0;
        }
        return age < POWER_OUT ? MochiDraw.elastic(age / POWER_OUT) : age < POWER_OUT + 2.0 ? 1.0 + 0.05 * Math.sin((age - POWER_OUT) * 3.0)
            * Math.exp(-(age - POWER_OUT)) : 1.0 - smooth((age - POWER_OUT - 2.0) / (POWER_LIFE - POWER_OUT - 2.0));
    }

    // ------------------------------------------------------------------------------------------------------ Z: Grilled Lance

    /**
     * Grilled Lance (the reference's 1205.gif, a tick a frame from its frame 14; docs/handoff/next-ai-3.6.3.md P): held, ticks from the
     * press: a white puff of mochi at the hand (to {@link #LANCE_SPIN}), a magenta ring of Haki spinning round the arm (to
     * {@link #LANCE_INK}), a burst of black ink with magenta star flares, the arm swollen into a great smooth ball of cream mochi with
     * magenta sparkles round it and fire at its base (full at {@link #LANCE_FULL}); held at most {@link #LANCE_MOST}. Let go: launched at
     * {@link #LANCE_SPEED} blocks a tick. Where it strikes, ticks from the strike: it melts into fire burning in them, the first blast
     * at {@link #LANCE_BOOM} (Grilled Mochi's rings of fire, rocks thrown), black Haki spikes standing in them with star flares, the
     * second blast far bigger at {@link #LANCE_BOOM2} (a white-hot flash, long rays, great swirls of fire, rocks and embers flung out).
     */
    public static final double LANCE_SPIN = 4.0;
    public static final double LANCE_INK = 11.0;
    public static final double LANCE_FULL = 16.0;
    public static final int LANCE_MOST = 60;
    public static final double LANCE_SPEED = 1.6;
    public static final double LANCE_RANGE = 18.0;
    public static final double LANCE_BALL = 0.75;
    public static final double LANCE_BOOM = 22.0;
    public static final double LANCE_BOOM2 = 46.0;
    public static final double LANCE_LIFE = LANCE_BOOM2 + 32.0;
    /** The first blast's size (MochiDraw.boom), the second's reach. */
    public static final double LANCE_BOOM_SIZE = 3.6;
    public static final double LANCE_BOOM2_SIZE = 5.5;
    static final int CHUNK = 0x4A2216;

    /**
     * Grilled Lance held, {@code age} ticks from the press: the hand at (x, y, z), the arm's way (wx, wy, wz) (unit, the look).
     */
    public static void lanceCharge(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double age, int seed) {
        if (age < 0) {
            return;
        }
        double[] b = B6;
        FireMesh.basis(wx, wy, wz, b);
        // the white puff of mochi round the hand (frame 14), swelling a little past its size and settling into the forearm
        double puff = MochiDraw.springy(age / LANCE_SPIN);
        double grow = smooth((age - LANCE_INK) / (LANCE_FULL - LANCE_INK));
        double ball = LANCE_BALL * MochiDraw.springy((age - LANCE_INK + 1.0) / (LANCE_FULL - LANCE_INK + 1.0));
        double cx = x + wx * (0.25 + 0.35 * grow);
        double cy = y + wy * (0.25 + 0.35 * grow);
        double cz = z + wz * (0.25 + 0.35 * grow);
        // breathing once it is full (the mochi alive)
        double breathe = 1.0 + 0.05 * Math.sin(age * 0.5 + seed) * grow;
        double r = Math.max(0.3 * puff, ball) * breathe;
        if (r > 0.03) {
            // the reference's ball is a smooth cream-ivory (frames 27 to 30)
            MochiArt.ball(sink, cx, cy, cz, r, wx, wy, wz, 1.0 + 0.25 * (1.0 - grow), 0.35 * (1.0 - grow) + 0.06, age, seed, grow > 0.5 ? CREAM : MochiArt.MOCHI);
            // the forearm into it: a tapered tube of mochi from behind the hand
            MochiArt.tubeBegin();
            MochiArt.tubePoint(x - wx * 0.55, y - wy * 0.55, z - wz * 0.55, 0.09);
            MochiArt.tubePoint(x - wx * 0.1, y - wy * 0.1, z - wz * 0.1, 0.12 + 0.1 * puff);
            MochiArt.tubePoint(cx, cy, cz, r * 0.7);
            MochiArt.tube(sink, 10, MochiArt.MOCHI, 0.0, age * 0.2);
        }
        // the magenta ring spinning round the arm (frames 18 to 22): two turns of a spiral, whirling
        double spin = smooth((age - 2.0) / 2.0) * (1.0 - smooth((age - LANCE_INK) / 3.0));
        if (spin > 0.02) {
            for (int k = 0; k < 3; k++) {
                double rr = 0.42 + 0.12 * k;
                FireMesh.arc(sink, x + wx * (0.1 + 0.18 * k), y + wy * (0.1 + 0.18 * k), z + wz * (0.1 + 0.18 * k), wx, wy, wz, rr, 0.09, age * 1.1
                    + k * 2.1, 4.4, 0.25, k == 1 ? PINK : MAGENTA, 0.9 * spin);
            }
            FireMesh.arc(sink, x, y, z, wx, wy, wz, 0.55, 0.05, -age * 0.9, 3.0, 0.2, PINK_LIGHT, 0.8 * spin);
        }
        // the burst of black ink with magenta star flares (frame 25): the Haki soaking into the mochi
        double ink = age - LANCE_INK;
        if (ink >= 0.0 && ink < 7.0) {
            double f = ink / 7.0;
            double fade = 1.0 - smooth((f - 0.2) / 0.8);
            for (int k = 0; k < 12; k++) {
                double a = 2 * Math.PI * (k + 0.5 * FireMesh.rand(seed, k + 200)) / 12.0;
                double tilt = (FireMesh.rand(seed, k + 210) - 0.5) * 1.2;
                double ox = (b[0] * Math.cos(a) + b[3] * Math.sin(a)) + wx * tilt;
                double oy = (b[1] * Math.cos(a) + b[4] * Math.sin(a)) + wy * tilt;
                double oz = (b[2] * Math.cos(a) + b[5] * Math.sin(a)) + wz * tilt;
                double ol = Math.sqrt(ox * ox + oy * oy + oz * oz);
                ox /= ol;
                oy /= ol;
                oz /= ol;
                double d = 0.3 + (0.9 + 0.9 * FireMesh.rand(seed, k + 220)) * ease(f * 1.6);
                double size = (0.14 + 0.16 * FireMesh.rand(seed, k + 230)) * fade;
                if (size > 0.02) {
                    MochiArt.ball(sink, cx + ox * d, cy + oy * d, cz + oz * d, size, ox, oy, oz, 2.2, 0.5, age, seed + k, MochiArt.INK);
                }
            }
            for (int k = 0; k < 10; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 240);
                double up = FireMesh.rand(seed, k + 250) - 0.5;
                double d = 0.4 + 0.8 * FireMesh.rand(seed, k + 260);
                flare(sink, cx + (b[0] * Math.cos(a) + b[3] * Math.sin(a)) * d + up * 0.6 * wx, cy + (b[1] * Math.cos(a) + b[4] * Math.sin(a)) * d
                    + up * 0.6, cz + (b[2] * Math.cos(a) + b[5] * Math.sin(a)) * d, 0.7 + 0.7 * FireMesh.rand(seed, k + 270), PINK,
                    fade * (0.6 + 0.4 * Math.sin(ink * 2.0 + k)));
            }
        }
        if (grow > 0.02) {
            // magenta sparkles drifting round the ball, a magenta rim round it, fire licking at its base (frames 27 to 30)
            halo(sink, cx, cy, cz, r, MAGENTA, PINK, 0.35 * grow);
            for (int k = 0; k < 10; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 300) + age * (0.05 + 0.05 * FireMesh.rand(seed, k + 305));
                double up = (FireMesh.rand(seed, k + 310) - 0.5) * 1.6;
                double d = r * (1.3 + 0.9 * FireMesh.rand(seed, k + 320));
                double tw = 0.5 + 0.5 * Math.sin(age * 0.9 + k * 1.7);
                flare(sink, cx + Math.cos(a) * d, cy + up * r, cz + Math.sin(a) * d, 0.12 + 0.1 * tw, PINK, 0.85 * grow * tw);
            }
            for (int k = 0; k < 3; k++) {
                double a = 2 * Math.PI * k / 3.0 + age * 0.3;
                double ox = b[0] * Math.cos(a) + b[3] * Math.sin(a);
                double oy = b[1] * Math.cos(a) + b[4] * Math.sin(a);
                double oz = b[2] * Math.cos(a) + b[5] * Math.sin(a);
                FireMesh.puff(sink, cx - wx * r * 0.6 + ox * r * 0.6, cy - wy * r * 0.6 + oy * r * 0.6 - r * 0.3, cz - wz * r * 0.6 + oz * r * 0.6,
                    0.16 * grow, -wx, -wy + 0.6, -wz, 0.9, 0.7, 0.55, 0.5, true, 0.85 * grow, age, seed + k + 40);
            }
        }
    }

    /**
     * Grilled Lance in flight at (x, y, z) going (wx, wy, wz) (unit), {@code age} ticks since it was let go: the ball of Haki mochi, its
     * magenta rim, fire streaming behind it and its sparkles.
     */
    public static void lanceFly(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double age, int seed) {
        if (age < 0) {
            return;
        }
        double r = LANCE_BALL * (1.0 + 0.08 * Math.sin(age * 0.8));
        MochiArt.ball(sink, x, y, z, r, wx, wy, wz, 1.15, 0.08, age, seed, CREAM);
        halo(sink, x, y, z, r, MAGENTA, PINK, 0.4);
        // fire licking off its back as it flies (the GIF's flight is short: a few tongues, no long jet)
        for (int k = 0; k < 3; k++) {
            double t = (k + 0.5) / 3.0;
            FireMesh.puff(sink, x - wx * (r * 0.8 + t * 0.6), y - wy * (r * 0.8 + t * 0.6) - r * 0.2, z - wz * (r * 0.8 + t * 0.6), r * (0.32 - 0.15 * t),
                -wx, -wy + 0.3, -wz, 0.6, 0.6, 0.65 - 0.2 * t, 0.5, true, 0.85 * (1.0 - 0.5 * t), age, seed + k);
        }
        for (int k = 0; k < 6; k++) {
            double a = 2 * Math.PI * k / 6.0 + age * 0.6;
            double tw = 0.5 + 0.5 * Math.sin(age * 1.3 + k);
            flare(sink, x + Math.cos(a) * r * 1.4, y + Math.sin(a) * r * 1.4, z - wz * 0.3, 0.14, PINK, 0.8 * tw);
        }
        // the launch's puff (frame 33): a ring of white mochi bursting off as it goes
        if (age < 4.0) {
            FireMesh.ring(sink, x - wx * 0.5, y - wy * 0.5, z - wz * 0.5, wx, wy, wz, r * (1.1 + 1.2 * ease(age / 4.0)), 0.35, -0.2, 0xFFFFFF,
                0.8 * (1.0 - age / 4.0));
        }
    }

    /**
     * What Grilled Lance does where it strikes (x, y, z), the ground under it at {@code ground} (eye-relative y), {@code age} ticks from
     * the strike: the ball melting into fire burning in them with orange star flares (frames 33 to 54), the first blast (56: Grilled
     * Mochi's rings, rocks thrown, a magenta ring over the ground), black Haki spikes standing in them with white slashes and star flares
     * (62 to 78), the second blast (80: a white-hot flash, long rays, swirls of fire sweeping out, rocks and embers flung far).
     */
    public static void lanceHit(FireMesh.Sink sink, double x, double y, double z, double ground, double age, int seed) {
        if (age < 0 || age > LANCE_LIFE) {
            return;
        }
        // ---- the ball melting into the fire that burns in them
        if (age < 4.0) {
            double f = age / 4.0;
            MochiArt.ball(sink, x, y, z, LANCE_BALL * (1.0 - 0.6 * f), 0, 1, 0, 1.0, 0.2, age, seed, MochiArt.MOCHI);
        }
        double burn = smooth(age / 3.0) * (1.0 - smooth((age - LANCE_BOOM) / 3.0));
        if (burn > 0.02) {
            for (int k = 0; k < 5; k++) {
                double a = 2 * Math.PI * k / 5.0 + age * 0.2;
                double up = 0.3 * Math.sin(age * 0.4 + k);
                FireMesh.puff(sink, x + Math.cos(a) * 0.35, y + up, z + Math.sin(a) * 0.35, 0.42 + 0.1 * Math.sin(age * 0.7 + k), 0, 1, 0, 0.7, 0.6, 0.6,
                    0.55, true, 0.9 * burn, age, seed + k);
            }
            if (age < 8.0) {
                // the ball bursting into fire (frames 33 to 38): a hot white-yellow heart
                FireMesh.puff(sink, x, y, z, 0.6 + 0.3 * ease(age / 3.0), 0, 1, 0, 0.2, 0.4, 0.88, 0.5, false, 0.9 * (1.0 - smooth((age - 3.0) / 5.0)), age,
                    seed + 9);
            }
            for (int k = 0; k < 3; k++) {
                double tw = Math.max(0.0, Math.sin(age * 0.8 + k * 2.2));
                double a = 2 * Math.PI * FireMesh.rand(seed, k + (int) (age / 4.0) * 7);
                flare(sink, x + Math.cos(a) * 0.6, y + 0.3 * (k - 1), z + Math.sin(a) * 0.6, 1.6 + 1.6 * tw, 0xFFB040, burn * tw);
            }
        }
        // ---- the first blast: Grilled Mochi's rings, rocks thrown, a magenta ring over the ground
        double b1 = age - LANCE_BOOM;
        if (b1 >= 0.0) {
            // the GIF's first blast is quick (its rings gone in about six frames, its fire in ten): Grilled Mochi's, played three times
            // as fast, so the spikes after it show
            MochiDraw.boom(sink, x, y, z, LANCE_BOOM_SIZE, b1 * 3.0, seed + 50);
            chunks(sink, x, y, z, ground, b1, 14, 0.3, 0.9, seed + 60);
            if (b1 < 14.0) {
                FireMesh.ring(sink, x, ground + 0.08, z, 0, 1, 0, 1.0 + 3.2 * ease(b1 / 10.0), 0.3, 0.1, MAGENTA, 0.7 * (1.0 - b1 / 14.0));
            }
        }
        // ---- black Haki spikes standing in them, white slashes and star flares (frames 62 to 78)
        double sp = age - LANCE_BOOM - 2.0;
        double spikes = smooth(sp / 3.0) * (1.0 - smooth((age - LANCE_BOOM2 + 2.0) / 3.0));
        if (spikes > 0.02) {
            for (int k = 0; k < 7; k++) {
                double a = 2 * Math.PI * (k + 0.5 * FireMesh.rand(seed, k + 400)) / 7.0;
                double d = 0.2 + 0.7 * FireMesh.rand(seed, k + 410);
                double lean = 0.05 + 0.2 * FireMesh.rand(seed, k + 420);
                double tall = (2.0 + 2.2 * FireMesh.rand(seed, k + 430)) * MochiDraw.springy(sp / 3.0);
                double bx = x + Math.cos(a) * d;
                double bz = z + Math.sin(a) * d;
                MochiArt.cone(sink, bx, ground, bz, bx + Math.cos(a) * lean * tall, ground + tall, bz + Math.sin(a) * lean * tall, 0.11 * spikes,
                    MochiArt.HAKI);
            }
            for (int k = 0; k < 2; k++) {
                double s0 = sp - 4.0 - 6.0 * k;
                if (s0 >= 0.0 && s0 < 5.0) {
                    FireMesh.arc(sink, x, y + 0.2, z, Math.cos(k * 2.0 + seed), 0.4, Math.sin(k * 2.0 + seed), 0.7, 0.12, s0 * 0.6 + k, 2.2, 0.1, 0xFFFFFF,
                        0.95 * (1.0 - s0 / 5.0));
                }
            }
            for (int k = 0; k < 5; k++) {
                double tw = Math.max(0.0, Math.sin(sp * 0.5 + k * 1.6));
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 440);
                double d = 0.5 + 1.5 * FireMesh.rand(seed, k + 450);
                flare(sink, x + Math.cos(a) * d, y + 1.8 * (FireMesh.rand(seed, k + 460) - 0.3), z + Math.sin(a) * d, 1.4 + 2.0 * tw, 0xFF9030,
                    spikes * tw);
            }
        }
        // ---- the second blast, far bigger (frames 80 to 98)
        double b2 = age - LANCE_BOOM2;
        if (b2 >= 0.0) {
            double big = LANCE_BOOM2_SIZE;
            if (b2 < 5.0) {
                double f = b2 / 5.0;
                MochiArt.glow(sink, x, y + 0.4, z, big * (0.8 + 0.6 * ease(f)), 0xFFF8E8, 0.95 * (1.0 - smooth(f)));
                MochiArt.glow(sink, x, y + 0.4, z, big * 0.45, 0xFFFFFF, 1.0 - f);
            }
            if (b2 < 10.0) {
                double f = b2 / 10.0;
                for (int k = 0; k < 22; k++) {
                    double a = 2 * Math.PI * (k + 0.4 * FireMesh.rand(seed, k + 500)) / 22.0;
                    double up = (FireMesh.rand(seed, k + 510) - 0.25) * 1.2;
                    double ul = Math.sqrt(1.0 + up * up);
                    FireMesh.ray(sink, x, y + 0.3, z, Math.cos(a) / ul, up / ul, Math.sin(a) / ul, big * (0.8 + 0.9 * FireMesh.rand(seed, k + 520)) * ease(f
                        * 3.0), 0.22, 0, 0.0, k % 3 == 0 ? 0xFFF29A : k % 3 == 1 ? 0xFFA215 : FIRE, 0.95 * (1.0 - smooth(f)));
                }
            }
            // the swirls of fire sweeping out (frames 82 to 98): wide arcs on their own tilts, opening out and fading
            double sw = smooth(b2 / 2.0) * (1.0 - smooth((b2 - 8.0) / 20.0));
            if (sw > 0.02) {
                for (int k = 0; k < 9; k++) {
                    double a = 2 * Math.PI * FireMesh.rand(seed, k + 530);
                    double tilt = 0.4 + 1.0 * FireMesh.rand(seed, k + 540);
                    double ax = Math.cos(a) * Math.sin(tilt);
                    double ay = Math.cos(tilt);
                    double az = Math.sin(a) * Math.sin(tilt);
                    double rad = big * (0.35 + 0.35 * FireMesh.rand(seed, k + 550)) * (0.5 + 0.9 * ease(b2 / 14.0));
                    FireMesh.arc(sink, x, y + 0.5, z, ax, ay, az, rad, 0.5 + 0.5 * FireMesh.rand(seed, k + 560), FireMesh.rand(seed, k + 570) * 6.3 + b2
                        * 0.12, 2.0 + 1.5 * FireMesh.rand(seed, k + 580), 0.6, k % 2 == 0 ? 0xF5651A : 0xFFA215, 0.85 * sw);
                    FireMesh.arc(sink, x, y + 0.5, z, ax, ay, az, rad * 1.04, 0.18, FireMesh.rand(seed, k + 570) * 6.3 + b2 * 0.12 + 0.2, 1.6
                        + 1.5 * FireMesh.rand(seed, k + 580), 0.6, 0xFFE88A, 0.8 * sw);
                }
                for (int k = 0; k < 7; k++) {
                    double a = 2 * Math.PI * (k + FireMesh.rand(seed, k + 590)) / 7.0;
                    double d = big * 0.35 * ease(b2 / 6.0);
                    FireMesh.puff(sink, x + Math.cos(a) * d, y + 0.4 + 0.5 * FireMesh.rand(seed, k + 595), z + Math.sin(a) * d, 0.9 * (1.0 - b2 / 30.0)
                        + 0.2, Math.cos(a), 0.4, Math.sin(a), 0.8, 0.5, 0.6 - 0.2 * Math.min(1.0, b2 / 20.0), 0.6, true, 0.9 * sw, age, seed + k + 600);
                }
            }
            chunks(sink, x, y, z, ground, b2, 20, 0.5, 1.6, seed + 700);
            // embers flung far
            if (b2 < 30.0) {
                for (int k = 0; k < 24; k++) {
                    double a = 2 * Math.PI * FireMesh.rand(seed, k + 800);
                    double up = 0.2 + 0.8 * FireMesh.rand(seed, k + 810);
                    double sp2 = 0.25 + 0.35 * FireMesh.rand(seed, k + 820);
                    double px = x + Math.cos(a) * sp2 * b2;
                    double pz = z + Math.sin(a) * sp2 * b2;
                    double py = y + up * sp2 * b2 - 0.012 * b2 * b2;
                    if (py < ground) {
                        continue;
                    }
                    FireMesh.streak(sink, px, py, pz, Math.cos(a), up - 0.024 * b2, Math.sin(a), 0.35, 0.03, FIRE, 0.9 * (1.0 - b2 / 30.0));
                }
            }
        }
    }

    /**
     * Rocks of the ground torn up and thrown out of a blast at (x, y, z) over the ground at {@code ground}, {@code age} ticks in:
     * {@code n} dark brown chunks {@code size} across flung out at {@code speed}, falling back and lying a moment.
     */
    static void chunks(FireMesh.Sink sink, double x, double y, double z, double ground, double age, int n, double size, double speed, int seed) {
        if (age < 0 || age > 36.0) {
            return;
        }
        double fade = 1.0 - smooth((age - 26.0) / 10.0);
        for (int k = 0; k < n; k++) {
            double a = 2 * Math.PI * (k + FireMesh.rand(seed, k)) / n;
            double up = 0.3 + 0.9 * FireMesh.rand(seed, k + 50);
            double v = speed * (0.5 + 0.6 * FireMesh.rand(seed, k + 100));
            double t = Math.min(age, 18.0);
            double px = x + Math.cos(a) * v * t * 0.5;
            double pz = z + Math.sin(a) * v * t * 0.5;
            double py = Math.max(ground + size * 0.4, y + up * v * t * 0.5 - 0.035 * t * t);
            FireMesh.shard(sink, px, py, pz, size * (0.6 + 0.8 * FireMesh.rand(seed, k + 150)), age * 0.3 * (py > ground + size ? 1.0 : 0.0) + k,
                seed + k, CHUNK, fade);
        }
    }

    /**
     * A four-pointed star flare at (x, y, z) (eye-relative) turned to the eye: two thin crossed streaks of light {@code size} out each
     * way, level and upright on the screen, a bright point in their middle.
     */
    static void flare(FireMesh.Sink sink, double x, double y, double z, double size, int rgb, double alpha) {
        if (alpha < 0.02 || size < 0.02) {
            return;
        }
        double d = Math.sqrt(x * x + y * y + z * z);
        if (d < size) {
            return;
        }
        double vx = x / d;
        double vy = y / d;
        double vz = z / d;
        // level across the view, and upright on it
        double rx = -vz;
        double rz = vx;
        double rl = Math.max(1.0E-6, Math.sqrt(rx * rx + rz * rz));
        rx /= rl;
        rz /= rl;
        double ux = -rz * vy;
        double uy = rz * vx - rx * vz;
        double uz = rx * vy;
        FireMesh.ray(sink, x, y, z, rx, 0, rz, size, size * 0.06, 0, 0.0, rgb, alpha);
        FireMesh.ray(sink, x, y, z, -rx, 0, -rz, size, size * 0.06, 0, 0.0, rgb, alpha);
        FireMesh.ray(sink, x, y, z, ux, uy, uz, size * 0.8, size * 0.06, 0, 0.0, rgb, alpha);
        FireMesh.ray(sink, x, y, z, -ux, -uy, -uz, size * 0.8, size * 0.06, 0, 0.0, rgb, alpha);
        MochiArt.glow(sink, x, y, z, size * 0.18, 0xFFFFFF, alpha * 0.9);
    }

    // ------------------------------------------------------------------------------------------------------ X: Elastic Lasso

    /**
     * Elastic Lasso (the reference's Elastic_Lasso.gif, a tick a frame from its frame 12): ticks from the press: a lasso of mochi whirled
     * round the user inside white wind and magenta rings (to {@link #LASSO_THROW}), thrown in a burst of pink and black; its band shoots
     * out in {@link #LASSO_SHOOT} ticks to whom it catches (a zig-zag kink by its end, as elastic), holds {@link #LASSO_HOLD}, yanks them
     * back to the user in {@link #LASSO_PULL} (a burst of pink, black and white shards as they arrive) and goes back into the hand; a
     * miss springs back.
     */
    public static final double LASSO_THROW = 8.0;
    public static final double LASSO_SHOOT = 3.0;
    public static final double LASSO_HOLD = 2.0;
    public static final double LASSO_PULL = 7.0;
    public static final double LASSO_REACH = 14.0;
    public static final double LASSO_BACK = 3.0;
    public static final double LASSO_LIFE = LASSO_THROW + LASSO_SHOOT + LASSO_HOLD + LASSO_PULL + LASSO_BACK;
    /** The bursts' life. */
    public static final double BURST_LIFE = 14.0;
    /** The awakened Haki's pink burning (coolest first), cut the way fire is (FireMesh.palette). */
    static final int[] PINK_BANDS = {0x3A0A58, 0x5E1688, 0x8A24B8, 0xC036E6, 0xE85AEE, 0xFF8AF2, 0xFFC4F6, 0xFFF0FC};

    /** When the lasso's band has its end out to where it goes (0 to 1 and past it, springing), {@code age} ticks from the throw. */
    public static double lassoOut(double age, boolean caught) {
        if (age < 0) {
            return 0.0;
        }
        if (age < LASSO_SHOOT) {
            return MochiDraw.elastic(age / LASSO_SHOOT);
        }
        if (caught) {
            return 1.0;
        }
        // a miss: held a beat, then it springs back into the hand
        return 1.0 - smooth((age - LASSO_SHOOT - 1.0) / 4.0);
    }

    /**
     * The lasso whirled (frames 12 to 19): the user's feet at (x, y, z), the hand at (hx, hy, hz), {@code age} ticks from the press: the
     * band whirled out of the hand round the user in a loop, white wind sweeping round, magenta rings whirling, black bits on the ground.
     */
    public static void lassoWhirl(FireMesh.Sink sink, double x, double y, double z, double hx, double hy, double hz, double age, int seed) {
        if (age < 0 || age > LASSO_THROW + 4.0) {
            return;
        }
        double on = smooth(age / 2.0);
        double off = 1.0 - smooth((age - LASSO_THROW) / 4.0);
        double a0 = age * 0.95;
        // the band: out of the hand and round in a loop at the chest, its reach growing as it whirls
        if (age < LASSO_THROW + 0.5) {
            double reach = 0.6 + 1.2 * on;
            MochiArt.tubeBegin();
            for (int k = 0; k <= 16; k++) {
                double t = k / 16.0;
                double a = a0 - 5.0 * t;
                double r = reach * (0.25 + 0.75 * Math.min(1.0, t * 1.6));
                double bob = 0.12 * Math.sin(t * 9.0 - age * 1.4);
                double px = x + Math.cos(a) * r;
                double py = y + 1.35 + 0.25 * t + bob;
                double pz = z + Math.sin(a) * r;
                if (k == 0) {
                    px = hx;
                    py = hy;
                    pz = hz;
                }
                MochiArt.tubePoint(px, py, pz, 0.12 * (1.0 - 0.3 * t) * on + 0.02);
            }
            MochiArt.bar(sink, MochiArt.MOCHI, 1.0);
        }
        double fx = on * off;
        if (fx < 0.02) {
            return;
        }
        // white wind sweeping round (frame 13), magenta rings whirling (15 to 19)
        for (int k = 0; k < 3; k++) {
            FireMesh.arc(sink, x, y + 0.9 + 0.35 * k, z, 0.08 * Math.cos(k), 1, 0.08 * Math.sin(k), 2.2 + 0.6 * k, 0.4, a0 * 1.2 + k * 2.0, 2.6, 0.15,
                0xFFFFFF, 0.75 * fx);
        }
        for (int k = 0; k < 4; k++) {
            double tilt = 0.12 * Math.sin(k * 1.7 + seed);
            FireMesh.arc(sink, x, y + 0.5 + 0.3 * k, z, tilt, 1, -tilt, 1.2 + 0.45 * k, 0.28, a0 * (1.4 - 0.1 * k) + k * 1.6, 3.6, 0.1,
                k % 2 == 0 ? MAGENTA : DEEP, 0.85 * fx);
        }
        for (int k = 0; k < 10; k++) {
            double a = 2 * Math.PI * FireMesh.rand(seed, k + 900) + age * 0.3;
            double d = 1.0 + 1.6 * FireMesh.rand(seed, k + 910);
            FireMesh.shard(sink, x + Math.cos(a) * d, y + 0.06, z + Math.sin(a) * d, 0.08, age * 0.4 + k, seed + k, INK, fx);
        }
    }

    /**
     * The lasso's band from the hand (hx, hy, hz) to its end (ex, ey, ez) (who it caught, or where it reached), {@code age} ticks from
     * the throw: shot out (elastic, past its reach and back), a wave running down it, a zig-zag kink near its end as elastic bunches
     * (frames 25 to 36); its end a loop round them when {@code caught}; going back into the hand after they arrive ({@code back} ticks
     * since, or negative).
     */
    public static void lassoBand(FireMesh.Sink sink, double hx, double hy, double hz, double ex, double ey, double ez, double age, boolean caught,
                                 double back, int seed) {
        if (age < 0) {
            return;
        }
        double out = lassoOut(age, caught);
        if (back >= 0) {
            out *= 1.0 - smooth(back / LASSO_BACK);
        }
        double dx = ex - hx;
        double dy = ey - hy;
        double dz = ez - hz;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.2 || out < 0.02) {
            return;
        }
        double ux = dx / len;
        double uy = dy / len;
        double uz = dz / len;
        // across the way, level, and the up square to both
        double ax = -uz;
        double az = ux;
        double al = Math.max(1.0E-6, Math.sqrt(ax * ax + az * az));
        ax /= al;
        az /= al;
        double vx = -az * uy;
        double vy = az * ux - ax * uz;
        double vz = ax * uy;
        double reach = len * out;
        double wave = 0.3 * Math.exp(-Math.max(0.0, age - LASSO_SHOOT) * 0.25) + 0.05;
        double thin = Math.min(0.4, reach / 30.0);
        MochiArt.tubeBegin();
        int n = 18;
        for (int k = 0; k <= n; k++) {
            double t = k / (double) n;
            double belly = Math.sin(Math.PI * t);
            double side = wave * belly * Math.sin(2 * Math.PI * 1.5 * t - age * 1.3 + seed);
            double rise = 0.5 * wave * belly * Math.cos(2 * Math.PI * 1.1 * t - age * 1.1 + seed);
            // the zig-zag kink by its end (the GIF's): out one way, back the other, then on to the end
            double kink = 0.0;
            if (t > 0.78 && t < 0.97) {
                double u = (t - 0.78) / 0.19;
                kink = (u < 0.5 ? u * 2.0 : 2.0 - u * 2.0) * 0.85 * Math.min(1.0, reach / 4.0);
                kink = t < 0.875 ? kink : -kink;
            }
            double along = reach * t;
            MochiArt.tubePoint(hx + ux * along + ax * (side + kink) + vx * rise, hy + uy * along + vy * rise + 0.6 * kink, hz + uz * along
                + az * (side + kink) + vz * rise, 0.2 * (1.0 - thin * belly));
        }
        MochiArt.bar(sink, MochiArt.MOCHI, 1.0);
        double tx = hx + ux * reach;
        double ty = hy + uy * reach;
        double tz = hz + uz * reach;
        if (caught && age >= LASSO_SHOOT) {
            // a loop of the band round them
            MochiArt.torus(sink, tx, ty, tz, 0, 1, 0, 0.5, 0.14, 1.0, MochiArt.MOCHI);
        } else {
            MochiArt.ball(sink, tx, ty, tz, 0.2, ux, uy, uz, 1.3, 0.3, age, seed, MochiArt.MOCHI);
        }
    }

    /**
     * A burst of the awakened Haki at (x, y, z), {@code size} across, {@code age} ticks in (Elastic Lasso's throw and its arrival, frames
     * 21 to 23 and 40 to 44): a soft jagged mass of pink fire, black splinters flung out, white shards of dough, a thin white ring.
     */
    public static void pinkBurst(FireMesh.Sink sink, double x, double y, double z, double size, double age, int seed) {
        if (age < 0 || age > BURST_LIFE) {
            return;
        }
        double f = age / BURST_LIFE;
        double grow = ease(age / 2.5);
        double fade = 1.0 - smooth((age - 2.0) / 8.0);
        if (fade > 0.02) {
            FireMesh.palette(PINK_BANDS);
            for (int k = 0; k < 5; k++) {
                double a = 2 * Math.PI * (k + FireMesh.rand(seed, k)) / 5.0;
                double up = (FireMesh.rand(seed, k + 5) - 0.4) * 0.8;
                double d = size * 0.35 * grow;
                FireMesh.puff(sink, x + Math.cos(a) * d, y + up * d, z + Math.sin(a) * d, size * (0.35 + 0.15 * FireMesh.rand(seed, k + 10)) * (0.5 + 0.5
                    * grow), Math.cos(a), up, Math.sin(a), 0.6, 1.1, 0.62, 0.55, false, 0.6 * fade, age, seed + k);
            }
            FireMesh.palette(FireMesh.BAND);
            for (int k = 0; k < 12; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 20);
                double up = (FireMesh.rand(seed, k + 30) - 0.4) * 1.4;
                double ul = Math.sqrt(1.0 + up * up);
                double d = size * (0.3 + 0.8 * FireMesh.rand(seed, k + 40)) * grow;
                FireMesh.streak(sink, x + Math.cos(a) / ul * d, y + up / ul * d, z + Math.sin(a) / ul * d, Math.cos(a), up, Math.sin(a),
                    size * (0.3 + 0.3 * FireMesh.rand(seed, k + 50)), 0.05, INK, 0.95 * fade);
            }
        }
        // white shards of dough flung out and falling (frames 44 to 58)
        for (int k = 0; k < 10; k++) {
            double a = 2 * Math.PI * FireMesh.rand(seed, k + 60);
            double up = 0.3 + 0.8 * FireMesh.rand(seed, k + 70);
            double v = size * (0.12 + 0.12 * FireMesh.rand(seed, k + 80));
            double px = x + Math.cos(a) * v * age;
            double py = y + up * v * age - 0.02 * age * age;
            double pz = z + Math.sin(a) * v * age;
            FireMesh.shard(sink, px, py, pz, 0.09 * size, age * 0.5 + k, seed + k, 0xFFFFFF, 1.0 - smooth((f - 0.5) / 0.5));
        }
        if (age < 7.0) {
            FireMesh.ring(sink, x, y, z, 0, 1, 0, size * (0.5 + 0.9 * ease(age / 7.0)), 0.06, 0.0, 0xFFFFFF, 0.8 * (1.0 - age / 7.0));
        }
    }

    // ------------------------------------------------------------------------------------------------------ C: Oblivion

    /**
     * Oblivion (the reference's Oblivion.gif, a tick a frame from its frame 14): ticks from the press: the user stomps
     * ({@link #OBL_STOMP}), a surge of cream mochi erupts along the ground from their feet to whom is aimed at (to {@link #OBL_GRAB}), a
     * burst of pink and black and the mochi rises into jagged walls all round them, a basin holding them (to {@link #OBL_BREAK}); the
     * walls sag and break, black Haki spikes and pink streaks stand in them, then a star burst of black and magenta
     * ({@link #OBL_BURST}) blows cream shards out everywhere.
     */
    public static final double OBL_STOMP = 3.0;
    public static final double OBL_SURGE = 5.0;
    public static final double OBL_GRAB = 10.0;
    public static final double OBL_BREAK = 31.0;
    public static final double OBL_BURST = 43.0;
    public static final double OBL_LIFE = OBL_BURST + 22.0;
    /** How far the surge reaches ("low range"), the walls' radius round the one held and their height. */
    public static final double OBL_RANGE = 8.0;
    public static final double OBL_WALL = 2.8;
    public static final double OBL_TALL = 1.7;
    static final int OBL_SLABS = 8;
    /** The reference's Oblivion dough: cream-ivory. */
    public static final int[] CREAM = {0xFFFCEE, 0xFBF2D6, 0xF1E3BC, 0xDCCB9C, 0xBCA77E};

    /**
     * Oblivion from the user's feet (x, y, z) to the feet of the one it holds (tx, ty, tz), {@code age} ticks from the press.
     */
    public static void oblivion(FireMesh.Sink sink, double x, double y, double z, double tx, double ty, double tz, double age, int seed) {
        if (age < 0 || age > OBL_LIFE) {
            return;
        }
        double dx = tx - x;
        double dz = tz - z;
        double len = Math.max(0.5, Math.sqrt(dx * dx + dz * dz));
        double wx = dx / len;
        double wz = dz / len;
        double px = -wz;
        double pz = wx;
        // ---- the stomp (frame 17): a ring of white over the ground round the foot, a little splash
        double st = age - OBL_STOMP;
        if (st >= 0.0 && st < 8.0) {
            FireMesh.ring(sink, x, y + 0.06, z, 0, 1, 0, 0.4 + 1.4 * ease(st / 6.0), 0.18, 0.05, 0xFFFFFF, 0.8 * (1.0 - st / 8.0));
            FireMesh.ring(sink, x, y + 0.08, z, 0, 1, 0, 0.3 + 1.0 * ease(st / 5.0), 0.12, 0.04, PINK, 0.7 * (1.0 - st / 8.0));
        }
        // ---- the surge (frames 19 to 24): cream mochi erupting along the ground from the feet to them, pink rays thrown up at its start
        double sg = age - OBL_SURGE;
        if (sg >= 0.0 && sg < 14.0) {
            double front = len * ease(sg / (OBL_GRAB - OBL_SURGE));
            int n = Math.max(4, (int) (len * 1.4));
            for (int k = 0; k < n; k++) {
                double at = len * (k + 0.5) / n;
                if (at > front) {
                    break;
                }
                double since = sg - (OBL_GRAB - OBL_SURGE) * Math.sqrt(at / len);
                double rise = MochiDraw.springy(since / 2.5) * (1.0 - smooth((since - 3.0) / 5.0));
                if (rise < 0.03) {
                    continue;
                }
                double side = (FireMesh.rand(seed, k + 1000) - 0.5) * 0.6;
                // the eruption tallest where it bursts out at the user's front (frames 19 to 21: twice their height), the surge lower on
                double h = (0.9 + 0.7 * FireMesh.rand(seed, k + 1010)) * rise * (k == 0 ? 2.6 : 1.0 + 0.3 * at / len);
                double wide = k == 0 ? 1.0 : 0.5;
                MochiArt.blob(sink, x + wx * at + px * side, y + h * 0.45, z + wz * at + pz * side, 0, 1, 0, wx, 0, wz, h * 0.6 + 0.05,
                    wide * rise + 0.05, 0.45 * rise + 0.05, 1.0, 0.55, age, seed + k, CREAM, 1.0);
                if (k == 0 && since < 8.0) {
                    // cream thrown up out of the eruption (jagged bits, not round balls)
                    for (int j = 0; j < 10; j++) {
                        double a = 2 * Math.PI * FireMesh.rand(seed, j + 1040);
                        double up = 0.6 + 0.8 * FireMesh.rand(seed, j + 1045);
                        double v = 0.18 + 0.2 * FireMesh.rand(seed, j + 1050);
                        double bx = x + wx * at + Math.cos(a) * v * since;
                        double by = y + 0.8 + up * v * since * 1.6 - 0.03 * since * since;
                        double bz = z + wz * at + Math.sin(a) * v * since;
                        double r = (0.1 + 0.12 * FireMesh.rand(seed, j + 1055)) * (1.0 - since / 8.0);
                        if (r > 0.02 && by > y) {
                            MochiArt.ball(sink, bx, by, bz, r, Math.cos(a), up, Math.sin(a), 2.4, 0.5, age, seed + j, CREAM);
                        }
                    }
                }
            }
            if (sg < 8.0) {
                double f = sg / 8.0;
                FireMesh.ring(sink, x, y + 0.08, z, 0, 1, 0, 0.6 + 2.2 * ease(f), 0.45, 0.1, PINK, 0.85 * (1.0 - f));
                for (int k = 0; k < 8; k++) {
                    double a = 2 * Math.PI * (k + FireMesh.rand(seed, k + 1020)) / 8.0;
                    FireMesh.ray(sink, x + Math.cos(a) * 0.5, y + 0.2, z + Math.sin(a) * 0.5, Math.cos(a) * 0.35, 1, Math.sin(a) * 0.35, (1.0 + 2.0
                        * FireMesh.rand(seed, k + 1030)) * ease(f * 3.0), 0.14, 0, 0.0, k % 2 == 0 ? PINK : PINK_LIGHT, 0.9 * (1.0 - f));
                }
            }
        }
        // ---- the grab (frame 24): a burst of pink and black where they stand, white swirls over the ground round them
        double gr = age - OBL_GRAB;
        if (gr >= 0.0) {
            pinkBurst(sink, tx, ty + 1.0, tz, 2.0, gr, seed + 1);
            if (gr < 16.0) {
                for (int k = 0; k < 3; k++) {
                    FireMesh.arc(sink, tx, ty + 0.06, tz, 0, 1, 0, 1.4 + 0.7 * k, 0.22, gr * 0.5 + k * 2.1, 3.6, 0.0, 0xFFFFFF, 0.7 * (1.0 - gr / 16.0));
                }
            }
        }
        // ---- the walls (frames 27 to 45): jagged cream slabs all round them, rising one after another, breathing; then sagging, tipping out
        // and breaking into splats (45 to 54)
        if (gr >= 0.0 && age < OBL_BREAK + 10.0) {
            double brk = smooth((age - OBL_BREAK) / 8.0);
            for (int k = 0; k < OBL_SLABS; k++) {
                double a = 2 * Math.PI * (k + 0.3 * FireMesh.rand(seed, k + 1100)) / OBL_SLABS;
                double ca = Math.cos(a);
                double sa = Math.sin(a);
                double rise = MochiDraw.springy((gr - k * 0.3) / 4.0);
                if (rise < 0.02) {
                    continue;
                }
                double tall = OBL_TALL * (0.75 + 0.5 * FireMesh.rand(seed, k + 1110)) * rise * (1.0 - 0.85 * brk);
                double breathe = 1.0 + 0.04 * Math.sin(age * 0.5 + k);
                double r = OBL_WALL * (1.0 + 0.35 * brk);
                double lean = 0.25 * brk;
                double cx = tx + ca * r;
                double cz = tz + sa * r;
                double cy = ty + tall * 0.5 - 0.1;
                // a slab: wide along the ring, tall, thin across it; its top torn into lumps
                // frame 40: broad slabs, smooth faced, a little torn along their tops
                MochiArt.blob(sink, cx, cy, cz, -sa, 0, ca, ca * lean, 1, sa * lean, 1.2 * breathe, tall * 0.55 + 0.05, 0.36 * breathe, 0.5, 0.3,
                    age, seed + k, CREAM, 1.0);
                for (int j = 0; j < 2; j++) {
                    double off = (j - 0.5) * 1.1;
                    double th = tall * (0.1 + 0.1 * FireMesh.rand(seed, k * 3 + j + 1120));
                    if (th > 0.04) {
                        MochiArt.ball(sink, cx - sa * off + ca * lean * tall, ty + tall + th * 0.4 - 0.1, cz + ca * off + sa * lean * tall, th, 0, 1, 0, 1.5,
                            0.5, age, seed + k * 3 + j, CREAM);
                    }
                }
                if (brk > 0.0 && brk < 1.0 && k % 2 == 0) {
                    MochiDraw.splat(sink, cx, ty + 0.3, cz, ca, 0.3, sa, 1.2, (age - OBL_BREAK) * 1.2, seed + k);
                }
            }
            // the foam inside, round their feet
            double foam = smooth(gr / 4.0) * (1.0 - brk);
            if (foam > 0.03) {
                MochiArt.blob(sink, tx, ty + 0.15, tz, 1, 0, 0, 0, 1, 0, OBL_WALL * 0.8 * foam, 0.45 * foam, OBL_WALL * 0.8 * foam, 1.0, 0.5, age, seed + 50,
                    CREAM, 1.0);
            }
        }
        // ---- the broken walls' bits lying over the ground round them (frames 48 to 56), gone in the burst
        double lie = smooth((age - OBL_BREAK - 4.0) / 4.0) * (1.0 - smooth((age - OBL_BURST) / 2.0));
        if (lie > 0.03) {
            for (int k = 0; k < 12; k++) {
                double a = 2 * Math.PI * (k + 0.6 * FireMesh.rand(seed, k + 1150)) / 12.0;
                double d = OBL_WALL * (0.6 + 0.8 * FireMesh.rand(seed, k + 1160));
                double r = (0.25 + 0.3 * FireMesh.rand(seed, k + 1170)) * lie;
                MochiArt.blob(sink, tx + Math.cos(a) * d, ty + r * 0.2, tz + Math.sin(a) * d, Math.cos(a), 0, Math.sin(a), 0, 1, 0, r * 1.3, r * 0.35,
                    r, 0.8, 0.5, age, seed + k + 1180, CREAM, 1.0);
            }
        }
        // ---- black Haki spikes and pink streaks standing in them, pink rings round them (frames 45 to 54)
        double sp = age - OBL_BREAK;
        double spikes = smooth(sp / 3.0) * (1.0 - smooth((age - OBL_BURST) / 2.0));
        if (spikes > 0.02) {
            for (int k = 0; k < 5; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 1200);
                double d = 0.3 + 1.2 * FireMesh.rand(seed, k + 1210);
                double tall = (1.4 + 1.8 * FireMesh.rand(seed, k + 1220)) * MochiDraw.springy(sp / 3.0);
                double bx = tx + Math.cos(a) * d;
                double bz = tz + Math.sin(a) * d;
                MochiArt.cone(sink, bx, ty, bz, bx, ty + tall, bz, 0.08 * spikes, MochiArt.HAKI);
                FireMesh.ray(sink, tx + Math.cos(a + 1.0) * d * 0.7, ty + 0.2, tz + Math.sin(a + 1.0) * d * 0.7, 0, 1, 0, tall * 1.3, 0.1, 0, 0.0, PINK,
                    0.85 * spikes);
            }
            for (int k = 0; k < 2; k++) {
                FireMesh.arc(sink, tx, ty + 0.5 + 0.4 * k, tz, 0.15, 1, 0.1, 2.0 + 0.5 * k, 0.2, sp * 0.4 + k * 3.0, 3.2, 0.15, k == 0 ? PINK : MAGENTA,
                    0.85 * spikes);
            }
        }
        // ---- the star burst of black and magenta (frame 57), cream shards blown out everywhere (60 to 76)
        double bu = age - OBL_BURST;
        if (bu >= 0.0) {
            if (bu < 6.0) {
                double f = bu / 6.0;
                double fade = 1.0 - smooth(f);
                // frame 57: a star of black and magenta as wide as the walls were, a white-hot heart
                MochiArt.glow(sink, tx, ty + 1.0, tz, 3.6 * (0.6 + 0.6 * ease(f * 2.0)), PINK, 0.85 * fade);
                MochiArt.glow(sink, tx, ty + 1.0, tz, 1.4, 0xFFFFFF, 0.95 * fade);
                FireMesh.palette(PINK_BANDS);
                FireMesh.puff(sink, tx, ty + 1.0, tz, 0.55 + 0.35 * ease(f * 2.0), 0, 1, 0, 0.1, 1.0, 0.78, 0.5, false, 0.8 * fade, age, seed + 1305);
                FireMesh.palette(FireMesh.BAND);
                // its black spikes: solid, thick at the heart, needle-sharp, flung out all round
                for (int k = 0; k < 12; k++) {
                    double a = 2 * Math.PI * (k + 0.5 * FireMesh.rand(seed, k + 1330)) / 12.0;
                    double up = (FireMesh.rand(seed, k + 1335) - 0.35) * 1.4;
                    double ul = Math.sqrt(1.0 + up * up);
                    double l = (2.0 + 2.6 * FireMesh.rand(seed, k + 1340)) * ease(f * 3.5) * (1.0 - 0.4 * smooth((f - 0.5) / 0.5));
                    MochiArt.cone(sink, tx, ty + 1.0, tz, tx + Math.cos(a) / ul * l, ty + 1.0 + up / ul * l, tz + Math.sin(a) / ul * l, 0.3 * fade + 0.02,
                        MochiArt.INK);
                }
                for (int k = 0; k < 16; k++) {
                    double a = 2 * Math.PI * (k + 0.4 * FireMesh.rand(seed, k + 1300)) / 16.0;
                    double up = (FireMesh.rand(seed, k + 1310) - 0.4) * 1.6;
                    double ul = Math.sqrt(1.0 + up * up);
                    double l = (2.6 + 2.8 * FireMesh.rand(seed, k + 1320)) * ease(f * 3.0);
                    FireMesh.ray(sink, tx, ty + 1.0, tz, Math.cos(a) / ul, up / ul, Math.sin(a) / ul, l, 0.75, 0, 0.0, INK, 0.95 * fade);
                    FireMesh.ray(sink, tx, ty + 1.0, tz, Math.cos(a + 0.2) / ul, up / ul, Math.sin(a + 0.2) / ul, l * 0.8, 0.35, 0, 0.0,
                        k % 2 == 0 ? MAGENTA : PINK_LIGHT, 0.95 * fade);
                }
            }
            double fly = 1.0 - smooth((bu - 12.0) / 10.0);
            for (int k = 0; k < 36; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 1400);
                double up = 0.1 + 0.9 * FireMesh.rand(seed, k + 1410);
                double v = 0.45 + 0.6 * FireMesh.rand(seed, k + 1420);
                double t = Math.min(bu, 22.0);
                double sx = tx + Math.cos(a) * v * t;
                double sz = tz + Math.sin(a) * v * t;
                double sy = Math.max(ty + 0.1, ty + 1.0 + up * v * t - 0.02 * t * t);
                double size = (0.12 + 0.22 * FireMesh.rand(seed, k + 1430)) * fly;
                if (size > 0.02) {
                    MochiArt.ball(sink, sx, sy, sz, size, Math.cos(a), up - 0.04 * t, Math.sin(a), 2.0, 0.4, age, seed + k, CREAM);
                }
            }
            if (bu < 12.0) {
                for (int k = 0; k < 8; k++) {
                    double a = 2 * Math.PI * FireMesh.rand(seed, k + 1500);
                    double up = (FireMesh.rand(seed, k + 1510) - 0.3) * 0.8;
                    double d = 0.6 * bu;
                    FireMesh.streak(sink, tx + Math.cos(a) * d, ty + 1.0 + up * d, tz + Math.sin(a) * d, Math.cos(a), up, Math.sin(a), 1.2, 0.1, PINK,
                        0.85 * (1.0 - bu / 12.0));
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ B: Blazing Storm

    /**
     * Blazing Storm (the reference's Blazing_Storm.gif, a tick a frame from its frame 16): ticks from the press: six arms of mochi sprout
     * curling out of the user and a burst of fire and dough (to {@link #STORM_FORM}); the six black Haki arms jab out of white puffs at
     * the user onto the point aimed at, over and over, wrapped in fire (to {@link #STORM_BARRAGE}); the last strike, all six fully out at
     * once ({@link #STORM_LAST}), held; an explosion of fire where they meet ({@link #STORM_BOOM}); the arms drawn back.
     */
    public static final double STORM_FORM = 8.0;
    public static final double STORM_BARRAGE = 80.0;
    public static final double STORM_LAST = 84.0;
    public static final double STORM_BOOM = 94.0;
    public static final double STORM_LIFE = STORM_BOOM + 18.0;
    /** One arm's jab (ticks), how far the barrage reaches. */
    public static final double STORM_JAB = 5.0;
    public static final double STORM_RANGE = 11.0;
    public static final int STORM_ARMS = 6;
    private static final double[] STORM_ROOT = new double[3];

    /**
     * Where Blazing Storm's arm {@code i} leaves its puff: round the user's back at the shoulders, three a side one over another,
     * from the feet (x, y, z) of a body facing (fx, fz) (flat, unit), {@code age} ticks in (they bob); into out.
     */
    public static double[] stormRoot(int i, double x, double y, double z, double fx, double fz, double age, double[] out) {
        double side = i % 2 == 0 ? 1.0 : -1.0;
        int row = i / 2;
        // the right: (-fz, fx)
        double rx = -fz * side;
        double rz = fx * side;
        double outw = 0.75 + 0.2 * row;
        double back = 0.15 + 0.15 * row;
        double bob = 0.05 * Math.sin(age * 0.3 + i * 1.3);
        out[0] = x + rx * outw - fx * back;
        out[1] = y + 1.95 - 0.42 * row + bob;
        out[2] = z + rz * outw - fz * back;
        return out;
    }

    /** How far Blazing Storm's arm {@code i} is out {@code age} ticks in (0 at its puff to 1 on the point aimed at, springing past it). */
    public static double stormOut(int i, double age) {
        if (age < STORM_FORM || age > STORM_BOOM + 8.0) {
            return 0.0;
        }
        if (age >= STORM_LAST) {
            double t = age - STORM_LAST;
            return t < 2.0 ? MochiDraw.elastic(t / 2.0) : age < STORM_BOOM + 2.0 ? 1.0 : 1.0 - smooth((age - STORM_BOOM - 2.0) / 6.0);
        }
        if (age > STORM_BARRAGE) {
            return 0.0;
        }
        double p = ((age - STORM_FORM) + i * STORM_JAB / STORM_ARMS * 1.7) % STORM_JAB / STORM_JAB;
        return p < 0.35 ? MochiDraw.elastic(p / 0.35) : 1.0 - smooth((p - 0.35) / 0.6);
    }

    /**
     * Blazing Storm from the feet (x, y, z) of a user facing (fx, fz) (flat, unit) onto the point aimed at (ax, ay, az), {@code age} ticks
     * from the press.
     */
    public static void storm(FireMesh.Sink sink, double x, double y, double z, double fx, double fz, double ax, double ay, double az, double age,
                             int seed) {
        if (age < 0 || age > STORM_LIFE) {
            return;
        }
        double[] r = STORM_ROOT;
        double gone = 1.0 - smooth((age - STORM_BOOM - 6.0) / 8.0);
        // ---- the six arms of mochi sprouting out of the user, curling (frame 16), and their puffs
        for (int i = 0; i < STORM_ARMS; i++) {
            stormRoot(i, x, y, z, fx, fz, age, r);
            double grow = MochiDraw.springy((age - i * 0.4) / 5.0) * gone;
            if (grow < 0.03) {
                continue;
            }
            if (age < STORM_FORM + 4.0) {
                // a tendril from the back out to the puff, curling as it forms
                double curl = 1.0 - smooth((age - 4.0) / 6.0);
                MochiArt.tubeBegin();
                for (int k = 0; k <= 6; k++) {
                    double t = k / 6.0;
                    double cx = x - fx * 0.25 + (r[0] - x + fx * 0.25) * t;
                    double cy = y + 1.4 + (r[1] - y - 1.4) * t + curl * 0.5 * Math.sin(Math.PI * t * 1.5 + i);
                    double cz = z - fz * 0.25 + (r[2] - z + fz * 0.25) * t;
                    MochiArt.tubePoint(cx - fx * curl * 0.6 * Math.sin(Math.PI * t), cy, cz - fz * curl * 0.6 * Math.sin(Math.PI * t), 0.08 * grow + 0.02);
                }
                MochiArt.tube(sink, 8, MochiArt.MOCHI, 0.0, age * 0.2);
            }
            // the puff: a lumpy white ball the arm comes out of, breathing, swelling as its arm drives out
            double out = stormOut(i, age);
            double pr = 0.26 * grow * (1.0 + 0.06 * Math.sin(age * 0.6 + i) + 0.15 * Math.min(1.0, out));
            MochiArt.ball(sink, r[0], r[1], r[2], pr, fx, 0, fz, 1.15, 0.45, age, seed + i, MochiArt.MOCHI);
        }
        // ---- the burst of fire and dough as they form (frame 20): fire round the user, white blobs flung out, arcs of fire
        double bf = age - 3.0;
        if (bf >= 0.0 && bf < 9.0) {
            double f = bf / 9.0;
            double fade = 1.0 - smooth((f - 0.3) / 0.7);
            FireMesh.puff(sink, x, y + 1.3, z, 0.7 + 0.5 * ease(f * 2.0), 0, 1, 0, 0.2, 0.6, 0.8, 0.5, false, 0.85 * fade, age, seed + 30);
            MochiDraw.splat(sink, x, y + 1.3, z, 0, 1, 0, 1.6, bf, seed + 31);
            for (int k = 0; k < 3; k++) {
                FireMesh.arc(sink, x, y + 1.1, z, Math.cos(k * 2.1), 0.6, Math.sin(k * 2.1), 1.3 + 0.4 * k + 0.6 * ease(f), 0.18, k * 2.0 + bf * 0.4, 3.0,
                    0.2, FIRE, 0.85 * fade);
            }
        }
        // ---- the arms: black Haki bars out of the puffs onto the point aimed at, wrapped in fire, smeared with streaks (30 to 108)
        for (int i = 0; i < STORM_ARMS; i++) {
            double out = stormOut(i, age);
            if (out < 0.04) {
                continue;
            }
            stormRoot(i, x, y, z, fx, fz, age, r);
            // each arm aims a little off the point (they converge on it, not all into one spot)
            double sx = (FireMesh.rand(seed, i + 1600) - 0.5) * 0.7;
            double sy = (FireMesh.rand(seed, i + 1610) - 0.5) * 0.6;
            double tx = ax + sx * fz;
            double ty = ay + sy;
            double tz = az - sx * fx;
            double dx = tx - r[0];
            double dy = ty - r[1];
            double dz = tz - r[2];
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 0.3) {
                continue;
            }
            double ux = dx / len;
            double uy = dy / len;
            double uz = dz / len;
            double d = len * out;
            double ex = r[0] + ux * d;
            double ey = r[1] + uy * d;
            double ez = r[2] + uz * d;
            boolean last = age >= STORM_LAST;
            double rad = last ? 0.3 : 0.2;
            MochiDraw.arm(sink, r[0], r[1], r[2], ex, ey, ez, rad, rad * 1.3, 0.0, true, age, seed + i, i % 2 == 1);
            // fire wrapped round it and streaks smeared along it the way it drives
            // licks of flame on the bars near their puffs (the last strike's, frame 104); the barrage's fire is its smear below
            int n = last ? 2 : 0;
            for (int k = 0; k < n; k++) {
                double t = 0.3 + 0.6 * (k + 0.5) / n;
                double a = k * 2.4 + age * 0.9 + i;
                double ox = Math.cos(a) * rad * 1.3;
                double oy = Math.sin(a) * rad * 1.3;
                FireMesh.puff(sink, r[0] + ux * d * t + ox * uz, r[1] + uy * d * t + oy, r[2] + uz * d * t - ox * ux, 0.14 + 0.08 * t, -ux, -uy + 0.3, -uz, 1.0,
                    0.7, 0.55, 0.5, true, 0.8 * gone, age, seed + i * 7 + k);
            }
            // the barrage's smear (frames 30 to 96): long bands of orange fire and a dark smear of speed along the arm as it drives
            double smear = Math.min(1.0, out) * (last ? 0.7 : 1.0) * gone;
            FireMesh.ray(sink, r[0], r[1], r[2], ux, uy, uz, d * 0.95, 0.55, 0, 0.0, 0xF5651A, 0.45 * smear);
            FireMesh.ray(sink, r[0], r[1], r[2], ux, uy, uz, d * 0.9, 0.3, 0, 0.0, FIRE, 0.6 * smear);
            FireMesh.ray(sink, r[0] + fz * 0.08, r[1] + 0.06, r[2] - fx * 0.08, ux, uy, uz, d * 0.8, 0.14, 0, 0.0, 0xFFD23C, 0.85 * smear);
            if (!last) {
                FireMesh.ray(sink, r[0] - fz * 0.2, r[1] - 0.1, r[2] + fx * 0.2, ux, uy, uz, d * 0.7, 0.22, 0, 0.0, INK, 0.5 * smear);
            }
            for (int k = 0; k < 2; k++) {
                double t = 0.2 + 0.6 * FireMesh.rand(seed, i * 5 + k + (int) age);
                double o = (FireMesh.rand(seed, i * 5 + k + 1700) - 0.5) * 0.5;
                FireMesh.streak(sink, r[0] + ux * d * t + fz * o, r[1] + uy * d * t + o * 0.5, r[2] + uz * d * t - fx * o, ux, uy, uz, Math.min(2.5, d * 0.5),
                    0.04, k == 0 ? FIRE : 0xFFD23C, 0.9 * gone);
            }
            if (out > 0.85 && !last) {
                // where the fist lands: a flick of fire
                FireMesh.puff(sink, ex, ey, ez, 0.25, -ux, -uy, -uz, 0.6, 0.7, 0.7, 0.5, true, 0.8, age, seed + i + 40);
            }
        }
        // ---- crescents of fire curling round the user's front while the barrage lasts (frames 30 to 96)
        double cres = smooth((age - STORM_FORM) / 3.0) * (1.0 - smooth((age - STORM_BARRAGE) / 4.0));
        if (cres > 0.02) {
            for (int k = 0; k < 2; k++) {
                FireMesh.arc(sink, x + fx * 0.9, y + 1.4, z + fz * 0.9, fx, 0.25, fz, 1.0 + 0.4 * k, 0.32, age * (0.7 + 0.2 * k) + k * 3.0, 2.4, 0.3,
                    k == 0 ? FIRE : 0xFFB020, 0.85 * cres);
            }
        }
        // ---- the explosion where they meet (frame 112): fire swelling, arcs of fire swirling, white blobs and black shards flung out
        double bo = age - STORM_BOOM;
        if (bo >= 0.0 && bo < 18.0) {
            double f = bo / 18.0;
            double fade = 1.0 - smooth((f - 0.25) / 0.75);
            if (bo < 4.0) {
                MochiArt.glow(sink, ax, ay, az, 4.5, 0xFFF4D8, 0.9 * (1.0 - bo / 4.0));
            }
            for (int k = 0; k < 6; k++) {
                double a = 2 * Math.PI * (k + FireMesh.rand(seed, k + 1800)) / 6.0;
                double dd = 0.9 * ease(bo / 5.0);
                FireMesh.puff(sink, ax + Math.cos(a) * dd * 1.5, ay + 0.4 * FireMesh.rand(seed, k + 1805), az + Math.sin(a) * dd * 1.5, 1.3 * (1.0 - 0.4 * f),
                    Math.cos(a), 0.4, Math.sin(a), 0.6, 0.6, 0.75 - 0.3 * f, 0.6, true, 0.9 * fade, age, seed + k + 1810);
            }
            for (int k = 0; k < 6; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 1820);
                double tilt = 0.3 + 1.0 * FireMesh.rand(seed, k + 1825);
                FireMesh.arc(sink, ax, ay, az, Math.cos(a) * Math.sin(tilt), Math.cos(tilt), Math.sin(a) * Math.sin(tilt), 2.0 + 2.4 * ease(bo / 8.0),
                    0.55, FireMesh.rand(seed, k + 1830) * 6.3 + bo * 0.2, 2.6, 0.4, k % 2 == 0 ? 0xF5651A : 0xFFA215, 0.85 * fade);
            }
            for (int k = 0; k < 12; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 1840);
                double up = 0.2 + 0.8 * FireMesh.rand(seed, k + 1845);
                double v = 0.25 + 0.3 * FireMesh.rand(seed, k + 1850);
                double px = ax + Math.cos(a) * v * bo;
                double py = ay + up * v * bo - 0.02 * bo * bo;
                double pz = az + Math.sin(a) * v * bo;
                if (k % 2 == 0) {
                    MochiArt.ball(sink, px, py, pz, 0.2 * (1.0 - f), Math.cos(a), up, Math.sin(a), 1.4, 0.4, age, seed + k, MochiArt.MOCHI);
                } else {
                    FireMesh.streak(sink, px, py, pz, Math.cos(a), up, Math.sin(a), 0.5, 0.08, INK, 0.95 * fade);
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ N: Scorching Buzz Cut

    /*
     * Scorching Buzz Cut (the reference's Scorching_Buzzcut.gif, read frame by frame: docs/handoff/next-ai-3.6.3.md P): Buzz Cut Mochi's
     * machinery (MochiDraw.buzz*, server MochiMoves Wheel and Whirl, client MochiFx Buzz) with these over it: the wheel spiked and on
     * fire (frames 12 to 66), the whirl's ring of fire and Haki (84 to 120), a column of fire as the arm rises (114 to 126), the ball on
     * fire, the slam's fiery ball in orbiting rings and a great block of the ground thrown up (174 to 198).
     */

    /** The spiked burning wheel over Buzz Cut's (MochiDraw.buzzWheel): its middle (x, y, z), rolling along (wx, wz), as that one. */
    public static void scorchWheel(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double rolled, double speed, double form,
                                   double age, int seed) {
        if (form < 0.02) {
            return;
        }
        double r = MochiDraw.BUZZ_WHEEL;
        double turn = rolled / r;
        // black spikes round its rim, turning with it (frames 12 to 48: poking out of its sides)
        for (int k = 0; k < 12; k++) {
            double a = turn + 2 * Math.PI * k / 12.0;
            double fx = Math.cos(a);
            double fy = Math.sin(a);
            double bx = x + wx * fx * r * 0.88;
            double by = y + fy * r * 0.88;
            double bz = z + wz * fx * r * 0.88;
            double l = r + 0.5 * form;
            MochiArt.cone(sink, bx, by, bz, x + wx * fx * l, y + fy * l, z + wz * fx * l, 0.14 * form, MochiArt.HAKI);
        }
        // wreathed in flames, a glow of fire round it
        halo(sink, x, y, z, r * form, FIRE_GLOW, SPARK_RED, 0.5 * form);
        for (int k = 0; k < 8; k++) {
            double a = turn * 0.5 + 2 * Math.PI * k / 8.0;
            double fx = Math.cos(a);
            double fy = Math.sin(a);
            FireMesh.puff(sink, x + wx * fx * r * 1.05, y + fy * r * 1.05, z + wz * fx * r * 1.05, 0.3 * form, -wx, 0.8, -wz, 1.1, 0.8, 0.62, 0.55, true,
                0.85 * form, age, seed + k);
        }
        if (speed < 0.05) {
            return;
        }
        // fire spraying off the ground behind it (frame 48), sparks flung out to the sides, a burning trail
        double gy = y - r;
        for (int k = 0; k < 10; k++) {
            double side = (FireMesh.rand(seed, k + (int) (age * 3.0)) - 0.5) * 2.0;
            double up = 0.3 + 0.7 * FireMesh.rand(seed, k + 60);
            FireMesh.streak(sink, x - wx * 0.6 + wz * side * 0.4, gy + 0.2 + up * 0.5, z - wz * 0.6 - wx * side * 0.4, -wx + wz * side * 1.2, up,
                -wz - wx * side * 1.2, 0.7 + 0.6 * up, 0.06, k % 2 == 0 ? FIRE : 0xFFD23C, 0.9 * speed * form);
        }
        for (int k = 0; k < 4; k++) {
            double back = 0.8 + 0.9 * k;
            FireMesh.puff(sink, x - wx * back, gy + 0.25, z - wz * back, (0.45 - 0.08 * k) * speed * form, 0, 1, 0, 0.8, 0.7, 0.6 - 0.08 * k, 0.5, true,
                0.8 * (1.0 - 0.2 * k) * form, age, seed + 20 + k);
        }
    }

    /**
     * The whirl's ring of fire and Haki on the ground round the feet (x, y, z) (frames 84 to 120): swirls of fire trailing the arm
     * (at {@code turn} radians, {@code reach} out), dark bands between them, a white swirl in the middle, a thick magenta ring round its
     * edge, purple orbs and black rocks whirled round; {@code alpha} its strength.
     */
    public static void scorchWhirl(FireMesh.Sink sink, double x, double y, double z, double turn, double reach, double alpha, double age, int seed) {
        if (alpha < 0.02) {
            return;
        }
        double gy = y + 0.06;
        // 3.6.8: four bands (the spiral now reaches 14 out: five cost too much at its widest)
        for (int k = 0; k < 4; k++) {
            double rad = reach * (0.32 + 0.19 * k);
            FireMesh.arc(sink, x, gy + 0.02 * k, z, 0, 1, 0, rad, reach * 0.2, turn - 5.0, 5.0, 0.0, k % 2 == 1 ? 0xFFA215 : FIRE, 0.95 * alpha);
            FireMesh.arc(sink, x, gy + 0.02 * k + 0.01, z, 0, 1, 0, rad, reach * 0.06, turn - 4.6, 4.6, 0.0, 0xFFE070, 0.9 * alpha);
            FireMesh.arc(sink, x, gy + 0.01, z, 0, 1, 0, rad + reach * 0.075, reach * 0.06, turn - 4.0, 3.8, 0.0, INK, 0.75 * alpha);
        }
        FireMesh.arc(sink, x, gy + 0.05, z, 0, 1, 0, reach * 0.22, 0.3, turn - 3.0, 3.0, 0.0, 0xFFFFFF, 0.8 * alpha);
        FireMesh.ring(sink, x, gy, z, 0, 1, 0, reach * 1.05, 0.9, 0.15, MAGENTA, 0.75 * alpha);
        FireMesh.ring(sink, x, gy + 0.02, z, 0, 1, 0, reach * 1.05, 0.35, 0.1, PINK_LIGHT, 0.6 * alpha);
        for (int k = 0; k < 14; k++) {
            double a = 2 * Math.PI * FireMesh.rand(seed, k + 2000) + turn * (0.6 + 0.3 * FireMesh.rand(seed, k + 2005));
            double d = reach * (0.35 + 0.65 * FireMesh.rand(seed, k + 2010));
            double h = 0.15 + 0.9 * FireMesh.rand(seed, k + 2015);
            MochiArt.glow(sink, x + Math.cos(a) * d, gy + h, z + Math.sin(a) * d, 0.22, PINK, 0.85 * alpha);
            MochiArt.glow(sink, x + Math.cos(a) * d, gy + h, z + Math.sin(a) * d, 0.08, 0xFFFFFF, 0.9 * alpha);
        }
        for (int k = 0; k < 10; k++) {
            double a = 2 * Math.PI * FireMesh.rand(seed, k + 2020) + turn * 0.9;
            double d = reach * (0.25 + 0.6 * FireMesh.rand(seed, k + 2025));
            FireMesh.shard(sink, x + Math.cos(a) * d, gy + 0.2 + 0.6 * FireMesh.rand(seed, k + 2030), z + Math.sin(a) * d, 0.18, age * 0.5 + k, seed + k,
                0x2A2A30, alpha);
        }
    }

    /** How strong the column of fire is {@code t} ticks from the catch: from late in the whirl to just after the arm is raised. */
    public static double scorchColumnAlpha(double t) {
        return smooth((t - MochiDraw.BUZZ_WHIRL + 10.0) / 4.0) * (1.0 - smooth((t - MochiDraw.BUZZ_RAISED - 2.0) / 4.0));
    }

    private static final MochiDraw.Scaled SCORCH_SCALED = new MochiDraw.Scaled();

    /** The column of fire up out of the whirl's middle (frames 114 to 126) from the ground at (x, y, z), {@code top} high; {@code alpha}. */
    public static void scorchColumn(FireMesh.Sink sink, double x, double y, double z, double top, double alpha, double age, int seed) {
        if (alpha < 0.02) {
            return;
        }
        int n = 7;
        for (int k = 0; k < n; k++) {
            double t = (k + 0.5) / n;
            double wob = 0.25 * Math.sin(age * 0.8 + k * 1.3);
            FireMesh.puff(sink, x + wob, y + top * t, z - wob, (0.9 - 0.45 * t) * (0.7 + 0.3 * alpha), 0, 1, 0, 1.2, 0.9, 0.75 - 0.15 * t, 0.6, true,
                0.9 * alpha, age, seed + k);
        }
    }

    /** The spiked ball on fire (the whole cinematic): flames round the ball at (x, y, z), {@code r} its radius. */
    public static void scorchBall(FireMesh.Sink sink, double x, double y, double z, double r, double age, int seed) {
        if (r < 0.05) {
            return;
        }
        // 3.6.8: five tongues (six with the wider spiral overran the phone's budget at its heaviest)
        for (int k = 0; k < 5; k++) {
            double a = 2 * Math.PI * k / 5.0 + age * 0.3;
            double up = Math.sin(a * 1.7) * 0.6;
            FireMesh.puff(sink, x + Math.cos(a) * r * 0.95, y + up * r, z + Math.sin(a) * r * 0.95, r * 0.42, Math.cos(a), 0.5, Math.sin(a), 1.0, 0.8, 0.62,
                0.55, true, 0.85, age, seed + k);
        }
        halo(sink, x, y, z, r * 1.2, FIRE_GLOW, SPARK_RED, 0.45);
    }

    /**
     * The slam's fire (frames 174 to 198) at the ground (x, y, z), {@code age} ticks from it: a fiery dark ball in pink and magenta
     * rings orbiting it, fire round the crater, a great block of the ground thrown up out of it and falling.
     */
    public static void scorchCrater(FireMesh.Sink sink, double x, double y, double z, double age, int seed) {
        if (age < 0 || age > 40.0) {
            return;
        }
        // 3.6.8 (the owner: "a much bigger crater"): drawn as big as the crater it burns in (MochiDraw.SCORCH_CRATER_SCALE)
        if (sink != SCORCH_SCALED) {
            SCORCH_SCALED.target = sink;
            SCORCH_SCALED.cx = x;
            SCORCH_SCALED.cy = y;
            SCORCH_SCALED.cz = z;
            SCORCH_SCALED.scale = MochiDraw.SCORCH_CRATER_SCALE;
            scorchCrater(SCORCH_SCALED, x, y, z, age, seed);
            SCORCH_SCALED.target = null;
            return;
        }
        if (age < 16.0) {
            double f = age / 16.0;
            double fade = 1.0 - smooth((f - 0.3) / 0.7);
            FireMesh.puff(sink, x, y + 1.2, z, 1.4 + 0.4 * ease(f * 2.0), 0, 1, 0, 0.2, 0.7, 0.4, 0.7, true, 0.95 * fade, age, seed);
            // the rings orbit outside the giant ball (frame 186), so they show round it
            for (int k = 0; k < 4; k++) {
                double a = k * 1.3 + seed;
                double tilt = 0.6 + 0.35 * k;
                FireMesh.arc(sink, x, y + 1.2, z, Math.cos(a) * Math.sin(tilt), Math.cos(tilt), Math.sin(a) * Math.sin(tilt), 3.3 + 0.45 * k, 0.6,
                    age * (0.5 + 0.1 * k) + k, 4.6, 0.2, k % 2 == 0 ? PINK : MAGENTA, 0.95 * fade);
                FireMesh.arc(sink, x, y + 1.2, z, Math.cos(a) * Math.sin(tilt), Math.cos(tilt), Math.sin(a) * Math.sin(tilt), 3.3 + 0.45 * k, 0.2,
                    age * (0.5 + 0.1 * k) + k + 0.1, 4.4, 0.2, PINK_LIGHT, 0.9 * fade);
            }
        }
        double burn = 1.0 - smooth((age - 14.0) / 20.0);
        for (int k = 0; k < 6; k++) {
            double a = 2 * Math.PI * (k + FireMesh.rand(seed, k + 2100)) / 6.0;
            double d = 2.5 + 1.5 * FireMesh.rand(seed, k + 2105);
            FireMesh.puff(sink, x + Math.cos(a) * d, y + 0.4, z + Math.sin(a) * d, 0.6 * burn, 0, 1, 0, 0.9, 0.8, 0.6, 0.5, true, 0.85 * burn, age,
                seed + k + 10);
        }
        // the great block of the ground: thrown up out of the crater past the eye's height, turning, falling back (frames 186 to 198)
        if (age >= 2.0) {
            double t = age - 2.0;
            double bx = x + 1.2 * t * 0.12;
            double by = y + 0.8 + 0.55 * t - 0.022 * t * t;
            if (by > y - 1.0) {
                FireMesh.shard(sink, bx, by, z - 0.4 * t * 0.1, 2.2, t * 0.12, seed + 7, 0x3A3A42, 1.0 - smooth((age - 32.0) / 8.0));
            }
        }
    }
}
