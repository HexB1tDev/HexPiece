package com.hexhaki.piece.style;

import com.hexhaki.piece.dragon.FireMesh;
import java.util.EnumMap;
import java.util.Map;

/**
 * 2.8.3: what a fighting-style move looks like in 3D, moment by moment (pure: the game's client/fx/StyleVfx and
 * tools/preview/StylePreview call the very same code). Everything follows the move's own animation (StyleRig on its keyframes):
 * <ul>
 *   <li>the fist or foot that strikes each blow is found from how fast each limb's end moves into the blow's tick; while it moves fast
 *       it leaves a smear ({@link StyleArt#smear}) in its style's look (Black Leg hot, Fishman Karate water, Iron Fist shocked air, Six
 *       Powers pressed air; the Flame Leg fire), nothing while it winds up slowly;</li>
 *   <li>a straight blow (the limb's end driven along the limb, as a punch, a palm, a snapped kick) throws the push of air off it
 *       ({@link StyleArt#thrust}); Fishman Karate's arms wind water round them into the blow ({@link StyleArt#helix}) and burst it out of
 *       the hand ({@link StyleArt#splash});</li>
 *   <li>with Armament Haki the very same blow is black edged in red, and crackles ({@link StyleArt#crackle});</li>
 *   <li>where a blow lands ({@link #landed}): the impact star, Fishman Karate's water thrown open with ripples, Haki's bolts.</li>
 * </ul>
 */
public final class StyleShow {
    private StyleShow() {
    }

    /** A move's pose at a moment (client/npc/StylePoser.sample). */
    public interface Poser {
        float[][] sample(String animation, float tick);
    }

    /** Where the user stands, eye-relative (their feet), and the way their body faces (radians, the game's yaw). */
    public record Place(double x, double y, double z, double yaw) {
        /** A point of the player's frame (x to their left, y up, z ahead) where it is, eye-relative. */
        public void put(double[] f, double[] out) {
            double c = Math.cos(this.yaw);
            double s = Math.sin(this.yaw);
            out[0] = this.x + f[0] * c - f[2] * s;
            out[1] = this.y + f[1];
            out[2] = this.z + f[0] * s + f[2] * c;
        }

        /** A direction of the player's frame, turned as they face. */
        public void turn(double[] f, double[] out) {
            double c = Math.cos(this.yaw);
            double s = Math.sin(this.yaw);
            out[0] = f[0] * c - f[2] * s;
            out[1] = f[1];
            out[2] = f[0] * s + f[2] * c;
        }
    }

    /** One blow of a move: its tick, the limb that strikes it, whether it is driven straight, its way then (the player's frame), size. */
    public record Strike(int tick, int limb, boolean straight, double[] dir, double size, double along) {
    }

    private static final Map<StyleMove, Strike[]> STRIKES = new EnumMap<>(StyleMove.class);

    /** How far behind the limb a smear reaches (ticks) and how finely it is cut (moments a tick). */
    static final double TRAIL = 2.6;
    static final int STEPS = 4;
    /** A limb's end slower than this (blocks a tick) leaves no smear; this much faster, its full smear. */
    static final double SLOW = 0.1;
    static final double FAST = 0.18;

    /** Within this of the eye (blocks) an effect fades out, gone at {@link #NEAR}: one's own blows never fill the first-person view. */
    static final double NEAR = 0.35;
    static final double CLEAR = 1.15;
    private static final int[] FADED = new int[4];

    /** {@code sink} with every corner faded by its nearness to the eye (eye-relative points). */
    static FireMesh.Sink faded(FireMesh.Sink sink) {
        return (xyz, argb) -> {
            boolean near = false;
            for (int k = 0; k < 4; k++) {
                double d = Math.sqrt(xyz[k * 3] * xyz[k * 3] + xyz[k * 3 + 1] * xyz[k * 3 + 1] + xyz[k * 3 + 2] * xyz[k * 3 + 2]);
                double f = StyleArt.smooth((d - NEAR) / (CLEAR - NEAR));
                near |= f < 1.0;
                FADED[k] = (int) Math.round(((argb[k] >>> 24) & 255) * f) << 24 | argb[k] & 0xFFFFFF;
            }
            if (!near) {
                sink.quad(xyz, argb);
            } else if ((FADED[0] | FADED[1] | FADED[2] | FADED[3]) >>> 24 != 0) {
                sink.quad(xyz, FADED);
            }
        };
    }

    /** The size of a blow by its weight: a light blow 0.8, a finisher 1.6. */
    public static double size(Hit.Weight weight) {
        return switch (weight) {
            case LIGHT -> 0.8;
            case MEDIUM -> 1.0;
            case HEAVY -> 1.25;
            default -> 1.6;
        };
    }

    /** Whether the 3D show draws this move (the moves with their own effects keep them: the Air Blade, Iron Body, Shave). */
    public static boolean shown(StyleMove move) {
        return switch (move) {
            case SP_BLADE, SP_IRON, SP_SHAVE -> false;
            case SP_MOON, SP_PAPER -> true; // 2.8.4: no blows, their own show (special)
            default -> move.style() != FightingStyle.SWORDSMAN && move.style() != FightingStyle.NONE && move.hits().length > 0;
        };
    }

    /** A move's look (the Flame Leg's fire is its own). */
    public static StyleArt.Look look(StyleMove move, boolean haki) {
        if ((move == StyleMove.BL_FLAME || move == StyleMove.BL_HACHIS) && !haki) {
            return StyleArt.FIRE;
        }
        return StyleArt.of(move.style(), haki);
    }

    /** The end of a limb (the hand or the foot) in the player's frame. */
    static double[] tip(float[][] pose, int limb) {
        return StyleRig.limb(pose, limb, 0.0, 1.0, 0.0);
    }

    /** A move's blows: which limb strikes each, straight or swung, which way (cached per move). */
    public static Strike[] strikes(StyleMove move, Poser poser) {
        Strike[] cached = STRIKES.get(move);
        if (cached != null) {
            return cached;
        }
        Hit[] hits = move.hits();
        Strike[] out = new Strike[hits.length];
        for (int h = 0; h < hits.length; h++) {
            int tick = hits[h].tick();
            float[][] now = poser.sample(move.animation(), tick);
            float[][] before = poser.sample(move.animation(), Math.max(0, tick - 2));
            int best = StyleRig.RIGHT_ARM;
            double fastest = -1;
            double[] way = {0, 0, 1};
            for (int limb = StyleRig.RIGHT_ARM; limb <= StyleRig.LEFT_LEG; limb++) {
                double[] a = tip(before, limb);
                double[] b = tip(now, limb);
                double[] v = {b[0] - a[0], b[1] - a[1], b[2] - a[2]};
                double speed = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
                if (speed > fastest) {
                    fastest = speed;
                    best = limb;
                    way = speed > 1.0e-6 ? new double[] {v[0] / speed, v[1] / speed, v[2] / speed} : new double[] {0, 0, 1};
                }
            }
            // straight: the end driven along the limb (from the joint to the end)
            double[] j = StyleRig.limb(now, best, 0.0, 0.5, 0.0);
            double[] e = tip(now, best);
            double[] axis = {e[0] - j[0], e[1] - j[1], e[2] - j[2]};
            double al = Math.sqrt(axis[0] * axis[0] + axis[1] * axis[1] + axis[2] * axis[2]);
            double along = al < 1.0e-6 ? 0.0 : (axis[0] * way[0] + axis[1] * way[1] + axis[2] * way[2]) / al;
            // and not flung round by the body's own turn (a spinning kick sweeps, whatever its leg does)
            double spin = Math.abs(now[StyleRig.BODY][4] * now[StyleRig.BODY][7] - before[StyleRig.BODY][4] * before[StyleRig.BODY][7]);
            boolean straight = along > 0.6 && spin < 30.0;
            double[] dir = straight ? new double[] {axis[0] / al, axis[1] / al, axis[2] / al} : way;
            out[h] = new Strike(tick, best, straight, dir, size(hits[h].weight()), 1.0);
        }
        STRIKES.put(move, out);
        return out;
    }

    /** The limbs a move strikes with (a bit each). */
    static int limbs(Strike[] strikes) {
        int bits = 0;
        for (Strike s : strikes) {
            bits |= 1 << s.limb();
        }
        return bits;
    }

    /**
     * Everything a move shows on its user at {@code ticks} (from its start, fractional): the smears of the limbs it strikes with, the
     * pushes of its straight blows, Fishman Karate's water round the arm and out of the hand, Armament's crackle. {@code seed}: the user.
     */
    public static void attached(FireMesh.Sink out, StyleMove move, boolean haki, float ticks, Poser poser, Place place, int seed) {
        attached(out, move, haki, ticks, poser, place, seed, false);
    }

    /** In one's own first person ({@code own}) the pushes and the water off the hand are smaller: the target stays in sight. */
    static final double OWN = 0.55;

    public static void attached(FireMesh.Sink out, StyleMove move, boolean haki, float ticks, Poser poser, Place place, int seed, boolean own) {
        if (!shown(move)) {
            return;
        }
        FireMesh.Sink sink = faded(out);
        Strike[] strikes = strikes(move, poser);
        StyleArt.Look look = look(move, haki);
        int bits = limbs(strikes);
        String name = move.animation();
        for (int limb = StyleRig.RIGHT_ARM; limb <= StyleRig.LEFT_LEG; limb++) {
            if ((bits & 1 << limb) != 0) {
                smear(sink, name, limb, ticks, poser, place, look);
            }
        }
        double[] at = new double[3];
        double[] dir = new double[3];
        for (int i = 0; i < strikes.length; i++) {
            Strike s = strikes[i];
            double age = ticks - s.tick();
            if (age < -4.0 || age > StyleArt.SPLASH_LIFE) {
                continue;
            }
            float[][] hitPose = poser.sample(name, s.tick());
            place.put(StyleRig.limb(hitPose, s.limb(), 0.0, s.along(), 0.0), at);
            place.turn(s.dir(), dir);
            // Six Powers' pushes are its own rings of air (client/fx/AirFx), but for the Six King Gun's
            if (s.straight() && age >= 0.0 && (move.style() != FightingStyle.SIX_POWERS || move == StyleMove.SP_KING)) {
                StyleArt.thrust(sink, at, dir, age, s.size() * (own ? OWN : 1.0), look, seed * 17 + i);
            }
            if (haki && age > -2.0 && age < 4.0) {
                float[][] pose = poser.sample(name, Math.max(0.0F, ticks));
                double[] end = new double[3];
                place.put(StyleRig.limb(pose, s.limb(), 0.0, s.along(), 0.0), end);
                double fade = age < 0.0 ? (age + 2.0) / 2.0 : 1.0 - age / 4.0;
                StyleArt.crackle(sink, end, dir, (int) Math.floor(ticks), 3, 0.42 * s.size(), fade, seed + i);
            }
        }
        special(sink, move, haki, ticks, poser, place, seed, look);
    }

    /**
     * 2.8.4: the extras' own: Moon Walk's air stamped down under the feet, Paper Art's sheets drifting round the body, the Six King
     * Gun's sphere of shock out of both fists.
     */
    static void special(FireMesh.Sink sink, StyleMove move, boolean haki, float ticks, Poser poser, Place place, int seed, StyleArt.Look look) {
        double[] at = new double[3];
        switch (move) {
            case SP_MOON -> {
                float[][] kick = poser.sample(move.animation(), 1.0F);
                double[] foot = tip(kick, StyleRig.RIGHT_LEG);
                place.put(foot, at);
                StyleArt.step(sink, at, ticks - 1.0, look, seed + 41);
            }
            case SP_PAPER -> {
                place.put(new double[] {0.0, 0.0, 0.0}, at);
                StyleArt.papers(sink, at, ticks, move.duration(), haki, seed);
            }
            case SP_KING -> {
                int hit = move.hits()[0].tick();
                if (ticks >= hit) {
                    float[][] pose = poser.sample(move.animation(), hit);
                    double[] r = tip(pose, StyleRig.RIGHT_ARM);
                    double[] l = tip(pose, StyleRig.LEFT_ARM);
                    place.put(new double[] {(r[0] + l[0]) * 0.5, (r[1] + l[1]) * 0.5, (r[2] + l[2]) * 0.5 + 0.2}, at);
                    StyleArt.sphere(sink, at, ticks - hit, 2.6, look);
                }
            }
            default -> {
            }
        }
    }

    /** 2.8.4: what a line move throws (StyleArt.shot): "spear", "drill", "ball", "tunnel"; null: its shot is drawn elsewhere. */
    public static String shotKind(StyleMove move) {
        // 3.5.7: none now (Fishman Karate's spear and drill went with it, Iron Fist's cannonball and tunnel with its old moves)
        return null;
    }

    /** How long a thrown shot shows ({@code length} blocks out), ticks. */
    public static double shotLife(StyleMove move, double length) {
        String kind = shotKind(move);
        return kind == null ? 0.0 : length / StyleArt.speed(kind) + StyleArt.SPLASH_LIFE;
    }

    /** A line move's shot from {@code from} to {@code to} (eye-relative), {@code age} ticks on, in its user's look. */
    public static void shot(FireMesh.Sink out, StyleMove move, boolean haki, double age, double[] from, double[] to, int seed) {
        String kind = shotKind(move);
        if (kind != null) {
            StyleArt.shot(faded(out), kind, from, to, age, look(move, haki), seed);
        }
    }

    /** One limb's smear at {@code ticks}: the sheet between near its joint and just past its end over the last {@link #TRAIL} ticks. */
    static void smear(FireMesh.Sink sink, String name, int limb, float ticks, Poser poser, Place place, StyleArt.Look look) {
        int n = (int) Math.round(TRAIL * STEPS) + 1;
        double[] inner = new double[n * 3];
        double[] outer = new double[n * 3];
        double[] strength = new double[n];
        double[] f = new double[3];
        double[] prevTip = null;
        boolean any = false;
        double innerAlong = limb >= StyleRig.RIGHT_LEG ? 0.3 : 0.35;
        for (int k = 0; k < n; k++) {
            float t = (float) (ticks - TRAIL + k / (double) STEPS);
            float[][] pose = poser.sample(name, Math.max(0.0F, t));
            double[] tip = tip(pose, limb);
            place.put(StyleRig.limb(pose, limb, 0.0, innerAlong, 0.0), f);
            System.arraycopy(f, 0, inner, k * 3, 3);
            place.put(StyleRig.limb(pose, limb, 0.0, 1.12, 0.0), f);
            System.arraycopy(f, 0, outer, k * 3, 3);
            double speed = 0.0;
            if (prevTip != null) {
                double dx = tip[0] - prevTip[0];
                double dy = tip[1] - prevTip[1];
                double dz = tip[2] - prevTip[2];
                speed = Math.sqrt(dx * dx + dy * dy + dz * dz) * STEPS;
            }
            strength[k] = t < 0.0F ? 0.0 : StyleArt.smooth((speed - SLOW) / (FAST - SLOW));
            if (k == 1) {
                strength[0] = strength[1];
            }
            any |= strength[k] > 0.01;
            prevTip = tip;
        }
        if (any) {
            StyleArt.smear(sink, inner, outer, strength, n, look);
        }
    }

    /** How long a landed blow shows (ticks). */
    public static double landedLife(StyleMove move) {
        return StyleArt.IMPACT_LIFE;
    }

    /**
     * Where a blow of {@code move} landed ({@code at}, eye-relative, struck along {@code dir}), {@code age} ticks on: the impact star
     * in the style's look, Armament's bolts crackling out of it.
     */
    public static void landed(FireMesh.Sink out, StyleMove move, boolean haki, double age, double[] at, double[] dir, double size, int seed) {
        if (!shown(move)) {
            return;
        }
        FireMesh.Sink sink = faded(out);
        StyleArt.Look look = look(move, haki);
        StyleArt.impact(sink, at, dir, age, size, look, seed);
        if (haki && age < 6.0) {
            StyleArt.crackle(sink, at, dir, (int) Math.floor(age), 5, 0.9 * size, 1.0 - age / 6.0, seed + 3);
        }
    }

    /** The biggest weight of a move's blows. */
    public static Hit.Weight weight(StyleMove move) {
        Hit.Weight best = Hit.Weight.LIGHT;
        for (Hit hit : move.hits()) {
            if (hit.weight().ordinal() > best.ordinal()) {
                best = hit.weight();
            }
        }
        return best;
    }
}
