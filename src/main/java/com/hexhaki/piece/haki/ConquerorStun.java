package com.hexhaki.piece.haki;

import com.hexhaki.piece.Alignment;

/**
 * 2.4.9: who the Conqueror's release fells, for how long, and whom it hurts, by the releaser's alignment (pure, tested in
 * {@code LightningTests}). The owner: "every mob hit with the lightning from the Conqueror's release is to be fucking felled", then,
 * asked:
 * <ul>
 *   <li>"Good Aligments = Stuns to hostiles and players only ... (in the radius of the release)": a good player's release hurts no
 *       one; its full charge fells hostile mobs and players in its radius.</li>
 *   <li>"Evil alignment = Stuns everything hit in the radius of the release, entities/players hit by the lightning take the damage": an
 *       evil player's full charge fells every mob and player in its radius (and whatever its lightning strikes beyond it).</li>
 *   <li>Neutral (not asked): the original's release (it fells and hurts hostile mobs and players in its radius).</li>
 *   <li>A partial charge: "entities hit by the lightnings ALL both evil/good stun all entities caught": whatever its lightning strikes,
 *       mob or player, is felled, whatever the alignment (2.6.0: and what it takes in its radius, for as long as any held release).</li>
 *   <li>Then: "I do think that the evil one should actually deal damage, but never kill. It should always, deal, make it deal, half the
 *       current HP of the mob or person hit. Always half, but even if the player or entity has half a heart, then it doesn't deal any
 *       damage. It's not meant to kill.": an evil release takes half the health left of every body it hits (all it fells in its
 *       radius and all its lightning strikes), never killing ({@link #evilDamage}).</li>
 * </ul>
 * 2.6.0, how long (the owner: "The Conqueror release stun increased tap G = 5 seconds stun, hold G (min) = 10 seconds, max hold G = 20
 * seconds, Max hold G good alignment = 30 seconds"): a tap fells what it hits for {@link #TAP} ticks; a held release (any charge) for
 * {@link #HOLD_MIN} at the shortest hold, growing evenly with the hold (the charge filling, {@link #FILL} ticks, then held full for
 * {@link #RAMP} more) to {@link #HOLD_MAX}, a good player's to {@link #GOOD_MAX} ({@link #held}).
 */
public final class ConquerorStun {
    /** A tap (G pressed and let go at once): 5 seconds. */
    public static final int TAP = 100;
    /** A held release at the shortest hold: 10 seconds. */
    public static final int HOLD_MIN = 200;
    /** A held release at the longest hold: 20 seconds; a good player's 30. */
    public static final int HOLD_MAX = 400;
    public static final int GOOD_MAX = 600;
    /**
     * The original's charge: it fills in FILL ticks held (its {@code CONQUEROR_FULL_CHARGE_TICKS}: the charge is the ticks held times
     * 0.625), and G held up to TAP_TICKS is a tap ({@code CONQUEROR_TAP_MAX_CHARGE_TICKS}).
     */
    public static final int FILL = 160;
    public static final int TAP_TICKS = 10;
    /** Ticks held at full charge for the most stun. */
    public static final int RAMP = 100;

    private ConquerorStun() {
    }

    /** The most stun a held release of this alignment gives (ticks). */
    public static int most(int alignment) {
        return Alignment.good(alignment) ? GOOD_MAX : HOLD_MAX;
    }

    /**
     * How far along the longest hold a release is (0..1): {@code power} the original's charge (0..1, at least 0.04), {@code heldAtFull}
     * the ticks G was held after the charge filled (counted only at full charge). 0 at the shortest hold (just past a tap), 1 at the
     * full charge held {@link #RAMP} ticks.
     */
    public static double progress(float power, long heldAtFull) {
        double charge = Math.max(0.0, Math.min(1.0, power));
        double held = charge >= 0.999 ? Math.max(0L, Math.min(RAMP, heldAtFull)) : 0.0;
        double ticks = charge * FILL + held;
        return Math.max(0.0, Math.min(1.0, (ticks - TAP_TICKS) / (FILL + RAMP - TAP_TICKS)));
    }

    /** A held release's stun (ticks): {@link #HOLD_MIN} at the shortest hold to {@link #most} at the longest. */
    public static int held(int alignment, float power, long heldAtFull) {
        return (int) Math.round(HOLD_MIN + (most(alignment) - HOLD_MIN) * progress(power, heldAtFull));
    }

    /** True if this release fells every mob and player in its radius (an evil full charge), not only hostile ones and players. */
    public static boolean fellsAll(int alignment, boolean full) {
        return full && Alignment.evil(alignment);
    }

    /** True if this release may hurt what it hits: a good player's never (an evil one's by {@link #evilDamage}, a neutral one's as the original's). */
    public static boolean hurts(int alignment) {
        return !Alignment.good(alignment);
    }

    /**
     * 2.5.1 (the owner: "evil gets one animation (EVIL/Disturbing) and good gets another (Peaceful/in pain) like a hand on the face and
     * then a stare"): the Conqueror's charge's and releases' animation for this alignment (tools/conqueror_anims.py writes them all):
     * good and evil their own, neutral (and anything else, the tap, a clear) the name it was asked for.
     */
    public static String animation(String name, int alignment) {
        String side = Alignment.evil(alignment) ? "evil" : Alignment.good(alignment) ? "good" : null;
        if (side == null) {
            return name;
        }
        return switch (name) {
            case "conqueror_gear5_charge" -> "conqueror_" + side + "_charge";
            case "supreme_conqueror_release" -> "conqueror_" + side + "_release";
            case "conqueror_gear5_release" -> "conqueror_" + side + "_release_partial";
            default -> name;
        };
    }

    /** Half a heart (health points): a body this low is not hurt by an evil release. */
    public static final float SPARED = 1.0F;

    /** What an evil release takes from a body with {@code health} left: half of it, nothing at {@link #SPARED} or under. Never all. */
    public static float evilDamage(float health) {
        return health <= SPARED ? 0.0F : health / 2.0F;
    }
}
