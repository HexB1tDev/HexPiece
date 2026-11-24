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
    public static final double POWER_DONUT = 0.78;
    public static final double POWER_SIDE = 1.2;
    public static final double POWER_UP = 1.5;
    public static final double POWER_BACK = 0.2;
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
     * {@code gone} ticks since it ended (negative while it lasts): a splash of cream (frame 15), a fireball (20), the lumpy white donut
     * out of it, a black Haki fist in its hole, a soft red-orange fire glow behind it, flames licking over it (25 to 155); going, the
     * same backward into a puff of fire.
     */
    public static void powerDonut(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double age, double gone,
                                  int seed) {
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
        // the glow of fire behind it, flames licking over its top
        // (far enough behind that it rims the donut and never washes over its white)
        MochiArt.glow(sink, x - wx * 1.1, y - wy * 1.1, z - wz * 1.1, POWER_DONUT * 1.45, FIRE_GLOW, 0.32 * form);
        MochiDraw.donut(sink, x, y, z, wx, wy, wz, POWER_DONUT, form, 0.0, 2, 0.0, 0, 0, 0, true, age, seed, false);
        for (int k = 0; k < 3; k++) {
            double a = 2 * Math.PI * k / 3.0 + age * 0.05;
            double h = (age * 0.11 + k * 0.33) % 1.0;
            double[] b = B6;
            FireMesh.basis(wx, wy, wz, b);
            double ox = b[0] * Math.cos(a) + b[3] * Math.sin(a);
            double oy = b[1] * Math.cos(a) + b[4] * Math.sin(a);
            double oz = b[2] * Math.cos(a) + b[5] * Math.sin(a);
            double r = POWER_DONUT * 0.9;
            FireMesh.puff(sink, x + ox * r, y + oy * r + 0.25 * h, z + oz * r, 0.14 * (1.0 - h) * form, 0, 1, 0, 0.6, 0.6, 0.75, 0.5, false,
                0.85 * (1.0 - h), age, seed + k);
        }
    }

    private static final double[] B6 = new double[6];

    /**
     * A Power Mochi punch (frames 55 to 106): from the donut at (x, y, z) its fist shoots out onto (tx, ty, tz) as a long black square
     * Haki bar wrapped in fire (licks of flame streaming back along it, orange streaks round it), a jagged yellow-orange flash at the
     * donut as it goes, a burst of fire and red sparks where it lands; {@code age} ticks in.
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
        double out = age < POWER_OUT ? MochiDraw.elastic(age / POWER_OUT) : age < POWER_OUT + 2.0 ? 1.0
            : 1.0 - smooth((age - POWER_OUT - 2.0) / (POWER_LIFE - POWER_OUT - 2.0));
        double d = len * out;
        double r = MochiDraw.hole(POWER_DONUT) * 0.62;
        if (d > 0.2) {
            MochiDraw.arm(sink, x, y, z, x + ux * d, y + uy * d, z + uz * d, r, r * 1.35, 0.0, true, age, seed, false);
            // fire wrapped round the bar, streaming back from the fist
            double[] b = B6;
            FireMesh.basis(ux, uy, uz, b);
            int n = Math.min(9, 3 + (int) (d * 1.2));
            for (int k = 0; k < n; k++) {
                double t = (k + 0.5) / n;
                double a = t * 9.0 + age * 0.9 + k;
                double ox = b[0] * Math.cos(a) + b[3] * Math.sin(a);
                double oy = b[1] * Math.cos(a) + b[4] * Math.sin(a);
                double oz = b[2] * Math.cos(a) + b[5] * Math.sin(a);
                double rr = r * 1.25;
                FireMesh.puff(sink, x + ux * d * t + ox * rr, y + uy * d * t + oy * rr, z + uz * d * t + oz * rr, 0.16 + 0.1 * t, -ux, -uy, -uz, 0.9,
                    0.6, 0.85, 0.5, false, 0.9 * (age < POWER_OUT + 3.0 ? 1.0 : out), age, seed + k);
            }
            for (int k = 0; k < 4; k++) {
                double a = 2 * Math.PI * k / 4.0 + seed;
                double ox = (b[0] * Math.cos(a) + b[3] * Math.sin(a)) * r * 1.8;
                double oy = (b[1] * Math.cos(a) + b[4] * Math.sin(a)) * r * 1.8;
                double oz = (b[2] * Math.cos(a) + b[5] * Math.sin(a)) * r * 1.8;
                FireMesh.streak(sink, x + ux * d + ox, y + uy * d + oy, z + uz * d + oz, -ux, -uy, -uz, Math.min(d, 2.5), 0.05, k % 2 == 0 ? FIRE : 0xFFD23C,
                    0.85 * out);
            }
        }
        // the jagged flash at the donut as the fist goes (frame 66)
        if (age < 4.0) {
            double f = age / 4.0;
            MochiArt.glow(sink, x, y, z, 0.9 + 0.9 * f, 0xFFC040, 0.8 * (1.0 - f));
            for (int k = 0; k < 9; k++) {
                double a = 2 * Math.PI * (k + 0.4 * FireMesh.rand(seed, k)) / 9.0;
                double up = (FireMesh.rand(seed, k + 10) - 0.5) * 1.2;
                double ul = Math.sqrt(1.0 + up * up);
                FireMesh.ray(sink, x, y, z, Math.cos(a) / ul + ux * 0.6, up / ul + uy * 0.6, Math.sin(a) / ul + uz * 0.6, (0.6 + 0.9 * FireMesh.rand(seed, k + 20))
                    * ease(f * 2.0), 0.14, 0, 0.0, k % 2 == 0 ? 0xFFD23C : FIRE, 0.9 * (1.0 - f));
            }
        }
        // the blow: a burst of fire and red sparks where it lands
        double hit = age - POWER_OUT;
        if (hit >= 0.0 && hit < 6.0) {
            double f = hit / 6.0;
            FireMesh.puff(sink, tx, ty, tz, 0.4 + 0.6 * ease(f), 0, 1, 0, 0.2, 0.7, 0.85, 0.5, true, 1.0 - f, age, seed + 30);
            for (int k = 0; k < 8; k++) {
                double a = 2 * Math.PI * FireMesh.rand(seed, k + 40);
                double up = (FireMesh.rand(seed, k + 50) - 0.3) * 1.6;
                double ul = Math.sqrt(1.0 + up * up);
                FireMesh.ray(sink, tx, ty, tz, Math.cos(a) / ul, up / ul, Math.sin(a) / ul, 0.5 + 1.3 * ease(f), 0.06, 0, 0.0, SPARK_RED, 0.9 * (1.0 - f));
            }
        }
    }
}
