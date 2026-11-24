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
        FireMesh.ring(sink, cx, cy, cz, ex, ey, ez, big * 1.1, big * 0.75, 0.0, FIRE_GLOW, alpha);
        FireMesh.ring(sink, cx, cy, cz, ex, ey, ez, big * 1.35, big * 1.1, 0.0, SPARK_RED, alpha * 0.45);
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
}
