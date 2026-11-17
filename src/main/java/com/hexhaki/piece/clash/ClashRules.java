package com.hexhaki.piece.clash;

/**
 * 2.3.3 (in the game from 2.3.6): two blows meeting (pure, tested in {@code ClashTests}; the world's side is
 * {@code server/Clashes}). The owner: "every move is clashable (combat style wise, and yes you can take the impacts from HexGhoul's
 * latest version, however I want a impact frame whenever a clash happens.)", and "to clash with swords man, any form of armament haki
 * HAS TO BE ON, otherwise the sword cant be clashed and since it has more range, will come up on top, if haki is on, clash is
 * possible. When 2 ryou hakis clash, both lightning colors explode outwards from the clash area in a sphere (similar to conquerors but
 * smaller)."
 *
 * <p>Every fighting-style blow and every sword cut, a player's or a sea fighter's, is a {@link Blow} on its way from the tick it sets out
 * (its wind-up) to the tick it lands. One on its way that finds a foe within reach, both facing each other, whose own blow is on its
 * way too, meets it ({@link #meets}): both are stopped, an impact frame flashes, and both are thrown apart, the weaker further
 * ({@link #thrown}, HexGhoul's 3.4.3 rule). A bare blow (a fist, a kick) meets a blade only with Armament on ({@link #clashable}); without
 * it the blade comes through ({@link #bladeWins}). Two Ryou users meeting burst both colours out in a sphere ({@link #burst}).
 */
public final class ClashRules {
    /** What a blow strikes with. */
    public enum Kind {
        FIST, KICK, BLADE
    }

    /**
     * A blow on its way: what it strikes with, the tick it set out and the tick it lands, how far it reaches (blocks), how hard it is
     * (its damage), whether its user has some form of Armament on (Armament, Ryou, a Gear's), and the user's Ryou colour (0: no Ryou).
     */
    public record Blow(Kind kind, long began, long lands, double reach, double hit, boolean armed, int ryou) {
        public boolean blade() {
            return this.kind == Kind.BLADE;
        }
    }

    /** 2.3.6: how long the recoil from a clash lasts (ticks: the stagger). */
    public static final int RECOIL = 12;
    /** 2.3.6: the sound of two blows meeting (the owner's own Clashes.zip, as HexGhoul plays it). */
    public static final String SOUND = "piece.clash";
    /**
     * 2.3.6: the recoil a bare-handed fighter of {@code style} plays when its blow is met ({@code style_<style>_clash}, tools/
     * style_anims.py: knocked back, its own stance found again; a blade's is {@code sword_clash}).
     */
    public static String recoil(com.hexhaki.piece.style.FightingStyle style) {
        return "style_" + style.id() + "_clash";
    }

    /** How square two must face each other: the cosine of the most either may look off the other (60 degrees). */
    public static final double FACING = 0.5;
    /** Blocks past the longer blow's reach that the two still meet (both are swung out toward each other). */
    public static final double SLACK = 1.0;
    /** A blow still meets for this many ticks after its landing tick (the two may land a tick apart). */
    public static final int LATE = 1;
    /**
     * 2.3.6: a blow meets another only in its last ticks before it lands (its strike, not its wind-up): a fist still drawn back for a
     * haymaker is not yet coming, and a jab landing then simply lands. Two blows meet when both strike within these few ticks of each
     * other; one landing while the other is still winding up is a hit, so a fight is not all clashes.
     */
    public static final int WINDUP = 3;
    /** Ticks both are stunned after a clash (no blow, no step). */
    public static final int STAGGER = 12;
    /** Clash power: two ordinary blows' (about 5 damage each), the least and the most, and the power from which the ground shakes. */
    public static final double REFERENCE = 10.0;
    public static final float ORDINARY = 1.2F;
    public static final float LEAST = 0.7F;
    public static final float MOST = 3.0F;
    public static final float QUAKES = 1.55F;
    /** HexGhoul's 3.4.3 throw: an even clash's push (blocks a tick across) and rise, what a huge one adds, from where the stronger stands. */
    public static final double EVEN_PUSH = 0.42;
    public static final double EVEN_GROWS = 0.06;
    public static final double EVEN_LIFT = 0.15;
    public static final double STANDS = 0.5;
    /** The gap (how many times harder the other's blow was) from which the weaker is flung off its feet. */
    public static final double GAP = 2.0;

    private ClashRules() {
    }

    /**
     * A blow on its way {@code now}: set out, coming (no more than {@link #WINDUP} ticks before it lands), not landed more than
     * {@link #LATE} ticks ago.
     */
    public static boolean onItsWay(Blow blow, long now) {
        return now >= Math.max(blow.began(), blow.lands() - WINDUP) && now <= blow.lands() + LATE;
    }

    /**
     * Whether the two may clash at all: two bare blows, or two blades, always; a bare blow and a blade only with Armament on the bare
     * one's side (the owner: "any form of armament haki HAS TO BE ON, otherwise the sword cant be clashed").
     */
    public static boolean clashable(Blow a, Blow b) {
        if (a.blade() == b.blade()) {
            return true;
        }
        return a.blade() ? b.armed() : a.armed();
    }

    /** A bare blow without Armament meeting a blade: the blade comes through (its cut lands as it would), the bare blow is lost. */
    public static boolean bladeWins(Blow a, Blow b) {
        return a.blade() != b.blade() && !clashable(a, b);
    }

    /** One looking along ({@code lookX}, {@code lookZ}) faces the other, ({@code dx}, {@code dz}) off it across the ground. */
    public static boolean faces(double lookX, double lookZ, double dx, double dz) {
        double look = Math.sqrt(lookX * lookX + lookZ * lookZ);
        double away = Math.sqrt(dx * dx + dz * dz);
        if (away < 1.0E-6) {
            return true;
        }
        if (look < 1.0E-6) {
            return false;
        }
        return (lookX * dx + lookZ * dz) / (look * away) >= FACING;
    }

    /** The two, {@code distance} blocks apart, within reach of each other's blows (the longer one's, and {@link #SLACK}). */
    public static boolean reaches(Blow a, Blow b, double distance) {
        return distance <= Math.max(a.reach(), b.reach()) + SLACK;
    }

    /**
     * All of it, {@code now}: both blows on their way, either may clash the other, the two within reach, each facing the other ({@code
     * aFacesB}, {@code bFacesA}: {@link #faces} from each one's look).
     */
    public static boolean meets(Blow a, Blow b, long now, double distance, boolean aFacesB, boolean bFacesA) {
        return onItsWay(a, now) && onItsWay(b, now) && clashable(a, b) && reaches(a, b, distance) && aFacesB && bFacesA;
    }

    /**
     * 2.3.6: where the two meet, as blocks from {@code a} toward {@code b} ({@code distance} apart): as near the middle as both reaches
     * allow (a fist meeting a longer blade meets it where the fist reaches; a shot meets a fist at the fist).
     */
    public static double meeting(Blow a, Blow b, double distance) {
        double d = Math.max(0.0, distance);
        double lo = Math.max(0.0, d - b.reach());
        double hi = Math.min(d, a.reach());
        if (lo > hi) {
            return (lo + hi) * 0.5;
        }
        return Math.max(lo, Math.min(hi, d * 0.5));
    }

    /** How big a clash is from both blows' damage: {@link #ORDINARY} for two ordinary blows, growing with the root of the sum. */
    public static float power(double hitA, double hitB) {
        double sum = Math.max(0.0, hitA) + Math.max(0.0, hitB);
        return (float) Math.max(LEAST, Math.min(MOST, ORDINARY * Math.sqrt(sum / REFERENCE)));
    }

    /** True if a clash of {@code power} shakes the ground. */
    public static boolean quakes(float power) {
        return power >= QUAKES;
    }

    /** How much harder the other's blow was than one's own (1 even, above 1 the other was stronger). */
    public static double ratio(double own, double other) {
        return Math.max(0.1, other) / Math.max(0.1, own);
    }

    /** The weaker, {@code ratio} times out-hit, is flung off its feet (and stunned longer). */
    public static boolean overwhelmed(double ratio) {
        return ratio >= GAP;
    }

    /**
     * How hard one is thrown back (blocks a tick across) from a clash of {@code power}, {@code ratio} times out-hit: an even clash throws
     * both a short way apart, the stronger less (by the square of the gap; it stands its ground at twice as strong), the weaker further.
     */
    public static double thrown(float power, double ratio) {
        double r = Math.max(0.0, ratio);
        if (r <= STANDS) {
            return 0.0;
        }
        double even = EVEN_PUSH + EVEN_GROWS * (Math.max(LEAST, Math.min(MOST, power)) - ORDINARY);
        return even * (r < 1.0 ? r * r : Math.min(2.4, Math.pow(r, 1.2)));
    }

    /** How far up one is thrown with it (blocks a tick). */
    public static double lifted(double ratio) {
        double r = Math.max(0.0, ratio);
        if (r <= STANDS) {
            return 0.0;
        }
        return EVEN_LIFT * (r < 1.0 ? r * r : Math.min(2.0, r));
    }

    /** Ticks one is stunned: {@link #STAGGER}, half again for the overwhelmed. */
    public static int stagger(double ratio) {
        return overwhelmed(ratio) ? STAGGER * 3 / 2 : STAGGER;
    }

    /** Two Ryou users met: both colours burst out of the clash in a sphere. */
    public static boolean burst(Blow a, Blow b) {
        return a.ryou() != 0 && b.ryou() != 0;
    }

    /**
     * The impact frame (the owner: "I want a impact frame whenever a clash happens"): how long (client ticks) the screen holds the
     * inverted frame, 2 for an ordinary clash, to 4 for the hardest; how far off (blocks) a clash is seen that way.
     */
    public static int frameTicks(float power) {
        return power >= 2.4F ? 4 : power >= 1.6F ? 3 : 2;
    }

    public static double frameReach(float power) {
        return 24.0 + 8.0 * power;
    }

    /** The clash's sound: louder (heard further) and deeper the harder it is. */
    public static float volume(float power) {
        return 1.2F + 0.5F * power;
    }

    public static float pitch(float power) {
        return Math.max(0.65F, 1.0F + (ORDINARY - power) * 0.15F);
    }
}
