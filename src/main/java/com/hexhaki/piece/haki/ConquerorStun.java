package com.hexhaki.piece.haki;

import com.hexhaki.piece.Alignment;

/**
 * 2.4.9: who the Conqueror's radial release fells, for how long, and whom it hurts, by the releaser's alignment (pure, tested in
 * {@code LightningTests}). The owner: "every mob hit with the lightning from the Conqueror's release is to be fucking felled ... the
 * longer you hold the G button, at max charge, I think the stun should go up to 10 seconds", then, asked:
 * <ul>
 *   <li>"Good Aligments = Stuns to hostiles and players only (15 seconds stun at max charge) (in the radius of the release)": a good
 *       player's release hurts no one; its full charge fells hostile mobs and players in its radius, for up to {@link #GOOD_MAX}.</li>
 *   <li>"Evil alignment = Stuns everything hit in the radius of the release, entities/players hit by the lightning take the damage
 *       (max stun 9 seconds)": an evil player's full charge fells every mob and player in its radius (and whatever its lightning strikes
 *       beyond it), for up to {@link #EVIL_MAX}.</li>
 *   <li>Neutral (not asked): the original's release (it fells and hurts hostile mobs and players in its radius), for up to
 *       {@link #NEUTRAL_MAX} (the owner's first "up to 10 seconds").</li>
 *   <li>A partial charge: "entities hit by the lightnings ALL both evil/good stun all entities caught. 2 seconds": whatever its
 *       lightning strikes, mob or player, is felled for {@link #LIGHTNING} ticks, whatever the alignment.</li>
 *   <li>Then: "I do think that the evil one should actually deal damage, but never kill. It should always, deal, make it deal, half the
 *       current HP of the mob or person hit. Always half, but even if the player or entity has half a heart, then it doesn't deal any
 *       damage. It's not meant to kill.": an evil release takes half the health left of every body it hits (all it fells in its
 *       radius and all its lightning strikes), never killing ({@link #evilDamage}).</li>
 * </ul>
 * A full charge's stun grows with how long G was held after the charge filled: {@link #FIRST} of the most at once, the most after
 * {@link #RAMP} ticks more.
 */
public final class ConquerorStun {
    public static final int GOOD_MAX = 300;
    public static final int EVIL_MAX = 180;
    public static final int NEUTRAL_MAX = 200;
    /** Ticks held at full charge for the most stun, and the share of it let go the moment the charge fills. */
    public static final int RAMP = 100;
    public static final double FIRST = 0.3;
    /** A partial release's lightning: what it strikes is down this long (ticks). */
    public static final int LIGHTNING = 40;

    private ConquerorStun() {
    }

    /** The most stun a full charge of this alignment gives (ticks). */
    public static int most(int alignment) {
        if (Alignment.evil(alignment)) {
            return EVIL_MAX;
        }
        return Alignment.good(alignment) ? GOOD_MAX : NEUTRAL_MAX;
    }

    /** A full charge's stun (ticks), let go {@code heldAtFull} ticks after the charge filled. */
    public static int full(int alignment, long heldAtFull) {
        double grown = Math.max(0.0, Math.min(1.0, heldAtFull / (double) RAMP));
        return (int) Math.round(most(alignment) * (FIRST + (1.0 - FIRST) * grown));
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
