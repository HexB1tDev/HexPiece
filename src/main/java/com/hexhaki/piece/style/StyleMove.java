package com.hexhaki.piece.style;

import com.hexhaki.piece.style.Hit.Weight;
import java.util.ArrayList;
import java.util.List;

/**
 * 2.1.1: every fighting-style move (pure, tested in {@code StyleTests}): its style and slot, the animation the user plays
 * ({@code assets/hexhaki/player_animation/<animation>.json}, made by tools/style_anims.py and linted like every other), how long it
 * lasts, from which tick the next blow of the string may cut in, its cooldown, the style mastery it needs, its blows ({@link Hit}), how
 * it carries the user ({@link Motion}) and what else it does ({@link Effect}). Ticks are game ticks (20 a second). Sent by ordinal.
 *
 * <p>Choreography: every string blow is anticipated (a wind-up the animation shows before its hit tick), lands on its hit tick, and
 * recovers; the next blow may cut into the recovery ({@link #cancel}) so a string flows; the fourth is a finisher with a longer wind-up
 * and a heavier landing.
 */
public enum StyleMove {
    // --- Black Leg: kicks only, hands kept behind the back ---------------------------------------------------------------------
    BL_SNAP(FightingStyle.BLACK_LEG, Slot.M1_1, "Snap Kick", "A front kick snapped out from the knee.", 9, 6, 0, 0,
        Motion.lunge(2, 0.22), Effect.NONE, Hit.light(4, 3.3, 4.5F).throwing(0.35, 0.05)),
    BL_HOOK(FightingStyle.BLACK_LEG, Slot.M1_2, "Spinning Hook", "A full turn on the standing foot into a hooking heel.", 11, 8, 0, 0,
        Motion.lunge(1, 0.18), Effect.NONE, Hit.medium(6, 3.4, 5.0F).wide(70)),
    BL_SWEEP(FightingStyle.BLACK_LEG, Slot.M1_3, "Low Sweep", "Dropped low, the leg scythes their feet away.", 11, 8, 0, 0,
        Motion.NONE, Effect.NONE, Hit.medium(5, 3.2, 4.5F).wide(80).throwing(0.1, 0.35).stunning(16)),
    BL_MUTTON(FightingStyle.BLACK_LEG, Slot.M1_4, "Mutton Crush", "A hop and an axe kick brought down from overhead.", 16, 14, 0, 0,
        Motion.hop(2, 0.35, 0.42), Effect.QUAKE, Hit.finisher(10, 3.6, 8.0F).wide(60).throwing(0.9, 0.15)),
    BL_NECK(FightingStyle.BLACK_LEG, Slot.HEAVY, "Neck Shot", "A lunging roundhouse to the neck that launches them skyward.", 16, 16, 70, 0,
        Motion.lunge(3, 0.7), Effect.NONE, Hit.finisher(7, 3.8, 9.0F).wide(45).throwing(0.3, 1.0)),
    BL_FLAME(FightingStyle.BLACK_LEG, Slot.SPECIAL_A, "Flame Leg Whirlwind", "Spin on one leg until it burns, then three blazing sweeps "
        + "all round.", 22, 22, 180, 60, Motion.NONE, Effect.FIRE,
        Hit.medium(8, 3.6, 4.0F).wide(180).throwing(0.5, 0.15), Hit.medium(12, 3.6, 4.0F).wide(180).throwing(0.5, 0.15),
        Hit.heavy(16, 3.8, 5.0F).wide(180).throwing(0.8, 0.3)),
    BL_SKY(FightingStyle.BLACK_LEG, Slot.SPECIAL_B, "Sky Walk Drop", "Kick off the air itself, high, then drop a heel into the ground.", 26,
        26, 220, 200, Motion.leap(2, 15, 0.5, 1.05), Effect.QUAKE, Hit.finisher(19, 4.5, 12.0F).wide(180).throwing(1.0, 0.6)),

    // --- Six Powers: finger pistols, air blades, the vanishing step, the iron body ----------------------------------------------
    SP_FINGER_R(FightingStyle.SIX_POWERS, Slot.M1_1, "Finger Pistol", "A finger driven in like a bullet.", 8, 5, 0, 0,
        Motion.lunge(1, 0.15), Effect.NONE, Hit.light(3, 3.4, 4.5F).wide(30).stunning(8)),
    SP_FINGER_L(FightingStyle.SIX_POWERS, Slot.M1_2, "Finger Pistol", "The other hand, the other side.", 8, 5, 0, 0,
        Motion.lunge(1, 0.15), Effect.NONE, Hit.light(3, 3.4, 4.5F).wide(30).stunning(8)),
    SP_STORM(FightingStyle.SIX_POWERS, Slot.M1_3, "Storm Leg Slice", "A kick fast enough to cut the air in front of it.", 12, 9, 0, 0,
        Motion.NONE, Effect.NONE, Hit.medium(6, 5.0, 5.5F).wide(60)),
    SP_PALM(FightingStyle.SIX_POWERS, Slot.M1_4, "Iron Palm", "Both palms driven into the chest with the whole body behind them.", 14, 12,
        0, 0, Motion.lunge(5, 0.4), Effect.NONE, Hit.finisher(7, 3.5, 8.0F).throwing(1.3, 0.25)),
    SP_SHAVE(FightingStyle.SIX_POWERS, Slot.HEAVY, "Shave", "Kick the ground ten times in a blink: gone, and there, with a palm.", 10, 10,
        60, 0, Motion.dash(1, 4, 2.0), Effect.VANISH, Hit.medium(6, 3.0, 6.0F).wide(70).throwing(0.6, 0.15)),
    SP_BLADE(FightingStyle.SIX_POWERS, Slot.SPECIAL_A, "Air Blade", "A high kick that sends a crescent of cut air flying.", 16, 16, 120, 60,
        Motion.NONE, Effect.NONE, Hit.line(7, 22.0, 1.2, 10.0F, Weight.HEAVY).throwing(0.6, 0.2)),
    SP_IRON(FightingStyle.SIX_POWERS, Slot.SPECIAL_B, "Iron Body", "Every muscle locked: blows barely land, and those who strike are thrown "
        + "back.", 40, 40, 240, 200, Motion.NONE, Effect.GUARD),

    // --- Fishman Karate: open hands, water and shock --------------------------------------------------------------------------
    FK_PALM(FightingStyle.FISHMAN_KARATE, Slot.M1_1, "Shark Palm", "An open palm thrust from the hip.", 9, 6, 0, 0,
        Motion.lunge(2, 0.2), Effect.NONE, Hit.medium(4, 3.3, 5.0F).wide(45).throwing(0.55, 0.1)),
    FK_BACKFIST(FightingStyle.FISHMAN_KARATE, Slot.M1_2, "Back Fist", "The same arm snaps back across.", 10, 7, 0, 0,
        Motion.NONE, Effect.NONE, Hit.light(5, 3.2, 4.5F).wide(60)),
    FK_KNEE(FightingStyle.FISHMAN_KARATE, Slot.M1_3, "Rising Knee", "A knee driven up through the guard.", 10, 7, 0, 0,
        Motion.lunge(2, 0.2), Effect.NONE, Hit.medium(5, 2.8, 5.0F).wide(45).throwing(0.15, 0.45)),
    FK_BRICK(FightingStyle.FISHMAN_KARATE, Slot.M1_4, "Thousand-Brick Fist", "A fist whose shock passes through them like a wave.", 16, 14,
        0, 0, Motion.lunge(6, 0.35), Effect.NONE, Hit.finisher(9, 3.6, 10.0F).wide(40).throwing(1.6, 0.2)),
    // 2.3.8 (the owner's rework): five drops gathered out of the air over the raised fist, fired one a tick, each homing on a foe in
    // front (water/WaterBullet, server/WaterServer); its hits are the shots' ticks and each drop's damage, flown by WaterServer
    FK_BULLET(FightingStyle.FISHMAN_KARATE, Slot.HEAVY, "Water Bullet", "The hand raised to the sky: five drops of water gather over it "
        + "out of the air. Pushed forward, they fire, each locking onto a foe in front.", 28, 28, 90, 0, Motion.NONE, Effect.BULLETS,
        bullets()),
    // 2.3.9 (the owner's rework): a punch where he looks, its water shockwave striking all in its path, knocking them off their feet
    // (water/BrickFist, style/Tumble); in the Shark Skin Counter's place
    FK_BRICK_FIST(FightingStyle.FISHMAN_KARATE, Slot.SPECIAL_A, "Brick Fist", "A punch where you look: a shockwave of water rushes out of "
        + "the fist, throwing everything in its path off its feet.", 18, 18, 140, 60, Motion.lunge(6, 0.22), Effect.SHOCKWAVE,
        Hit.line(com.hexhaki.piece.water.BrickFist.STRIKE, com.hexhaki.piece.water.BrickFist.REACH, com.hexhaki.piece.water.BrickFist.RADIUS,
            9.0F, Weight.HEAVY).throwing(1.5, 0.5)),
    FK_WAVE(FightingStyle.FISHMAN_KARATE, Slot.SPECIAL_B, "Five-Thousand Brick Wave", "A fist into the ground: the shock runs out ahead "
        + "and throws everything in its path.", 22, 22, 220, 200, Motion.NONE, Effect.QUAKE,
        Hit.heavy(12, 8.0, 11.0F).wide(70).throwing(1.0, 0.7)),

    // --- Iron Fist: pirate boxing ---------------------------------------------------------------------------------------------
    IF_JAB(FightingStyle.BRAWLER, Slot.M1_1, "Jab", "A quick lead-hand jab.", 7, 4, 0, 0,
        Motion.lunge(1, 0.18), Effect.NONE, Hit.light(3, 3.1, 3.5F).wide(35)),
    IF_CROSS(FightingStyle.BRAWLER, Slot.M1_2, "Cross", "The rear hand, the hips turned through it.", 9, 6, 0, 0,
        Motion.lunge(2, 0.22), Effect.NONE, Hit.medium(4, 3.3, 5.0F).wide(40).throwing(0.4, 0.05)),
    IF_HOOK(FightingStyle.BRAWLER, Slot.M1_3, "Hook", "A tight hook round the guard.", 10, 7, 0, 0,
        Motion.NONE, Effect.NONE, Hit.medium(5, 3.1, 5.5F).wide(75).throwing(0.35, 0.1)),
    IF_UPPER(FightingStyle.BRAWLER, Slot.M1_4, "Uppercut", "Dropped low and driven up through the chin: they leave the ground.", 14, 12,
        0, 0, Motion.lunge(3, 0.2), Effect.NONE, Hit.finisher(6, 3.1, 7.5F).wide(45).throwing(0.2, 1.05)),
    IF_HAYMAKER(FightingStyle.BRAWLER, Slot.HEAVY, "Haymaker", "The arm wound all the way back, then everything behind one punch.", 20, 20,
        70, 0, Motion.lunge(10, 0.5), Effect.QUAKE, Hit.finisher(13, 3.6, 12.0F).wide(45).throwing(1.8, 0.25)),
    IF_GATLING(FightingStyle.BRAWLER, Slot.SPECIAL_A, "Gatling Jabs", "A storm of jabs too fast to count, and a straight to end it.", 26,
        26, 160, 60, Motion.NONE, Effect.NONE, gatling()),
    IF_CHARGE(FightingStyle.BRAWLER, Slot.SPECIAL_B, "Shoulder Charge", "Head down, shoulder first: through the crowd.", 18, 18, 200, 200,
        Motion.dash(3, 12, 1.15), Effect.ONCE,
        Hit.heavy(4, 2.2, 5.0F).wide(75).throwing(1.0, 0.3), Hit.heavy(6, 2.2, 5.0F).wide(75).throwing(1.0, 0.3),
        Hit.heavy(8, 2.2, 5.0F).wide(75).throwing(1.0, 0.3), Hit.heavy(10, 2.2, 5.0F).wide(75).throwing(1.0, 0.3),
        Hit.heavy(12, 2.2, 5.0F).wide(75).throwing(1.0, 0.3));

    /** What else a move does besides its blows. */
    public enum Effect {
        NONE,
        /** The ground shakes where the blow lands (the original's quake). */
        QUAKE,
        /** Its blows set what they hit on fire. */
        FIRE,
        /** Water splashes with its blows. */
        WATER,
        /** Unseen for the dash. */
        VANISH,
        /** Blows taken barely land; attackers are thrown back. */
        GUARD,
        /** Each body is hit once by the whole move, however many of its blows pass through it. */
        ONCE,
        /** 2.3.8: its blows are drops of water gathered over the fist and fired as homing shots (server/WaterServer). */
        BULLETS,
        /** 2.3.9: its blow is a shockwave of water along the look: all it strikes are knocked off their feet (server/Knockdowns). */
        SHOCKWAVE
    }

    private final FightingStyle style;
    private final Slot slot;
    private final String display;
    private final String description;
    private final int duration;
    private final int cancel;
    private final int cooldown;
    private final int mastery;
    private final Motion motion;
    private final Effect effect;
    private final Hit[] hits;

    StyleMove(FightingStyle style, Slot slot, String display, String description, int duration, int cancel, int cooldown, int mastery,
              Motion motion, Effect effect, Hit... hits) {
        this.style = style;
        this.slot = slot;
        this.display = display;
        this.description = description;
        this.duration = duration;
        this.cancel = cancel;
        this.cooldown = cooldown;
        this.mastery = mastery;
        this.motion = motion;
        this.effect = effect;
        this.hits = hits;
    }

    /** 2.3.8: the Water Bullet's five shots, one a tick from {@code WaterBullet.FIRE}: each drop flies as far as it reaches. */
    private static Hit[] bullets() {
        Hit[] hits = new Hit[com.hexhaki.piece.water.WaterBullet.COUNT];
        for (int i = 0; i < hits.length; i++) {
            hits[i] = Hit.line(com.hexhaki.piece.water.WaterBullet.fireTick(i), com.hexhaki.piece.water.WaterBullet.RANGE,
                com.hexhaki.piece.water.WaterBullet.RADIUS, 3.5F, Weight.LIGHT);
        }
        return hits;
    }

    private static Hit[] gatling() {
        List<Hit> hits = new ArrayList<>();
        for (int tick = 4; tick <= 22; tick += 2) {
            hits.add(Hit.light(tick, 3.2, 1.6F).wide(45).throwing(0.06, 0.02).stunning(5));
        }
        hits.add(Hit.medium(24, 3.3, 4.0F).wide(45).throwing(0.9, 0.15));
        return hits.toArray(new Hit[0]);
    }

    public FightingStyle style() {
        return this.style;
    }

    public Slot slot() {
        return this.slot;
    }

    public String display() {
        return this.display;
    }

    public String description() {
        return this.description;
    }

    public int duration() {
        return this.duration;
    }

    public int cancel() {
        return this.cancel;
    }

    public int cooldown() {
        return this.cooldown;
    }

    public int mastery() {
        return this.mastery;
    }

    public Motion motion() {
        return this.motion;
    }

    public Effect effect() {
        return this.effect;
    }

    public Hit[] hits() {
        return this.hits.clone();
    }

    /** The animation played for it: style_<style id>_<move name>. */
    public String animation() {
        return "style_" + this.style.id() + "_" + this.name().toLowerCase(java.util.Locale.ROOT).substring(3);
    }

    /** The move a style throws from a slot (null if none). */
    public static StyleMove of(FightingStyle style, Slot slot) {
        for (StyleMove move : values()) {
            if (move.style == style && move.slot == slot) {
                return move;
            }
        }
        return null;
    }

    public static StyleMove byOrdinal(int ordinal) {
        StyleMove[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : null;
    }

    /** The string's next slot after {@code current} (M1_4 wraps to M1_1). */
    public static Slot nextInString(Slot current) {
        return switch (current) {
            case M1_1 -> Slot.M1_2;
            case M1_2 -> Slot.M1_3;
            case M1_3 -> Slot.M1_4;
            default -> Slot.M1_1;
        };
    }
}
