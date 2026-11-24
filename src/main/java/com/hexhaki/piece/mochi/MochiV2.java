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
    public static final double BACK = 8.0;
    public static final double CROSS = 33.0;
    public static final double TIP = 44.0;

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
}
