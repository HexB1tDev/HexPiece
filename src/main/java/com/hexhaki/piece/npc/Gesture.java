package com.hexhaki.piece.npc;

import com.hexhaki.piece.Faction;

/**
 * 2.3.3 (the owner: "the NPCS ... can have the same ambient animations as HexGhoul, Ken, touka, eto, Moro, etc etc etc"): what a pirate or
 * a marine does with its body while nothing is happening (pure; sent by ordinal, new ones at the end). The shapes are HexGhoul's
 * (control/npc/EtoGesture, its client EtoPose: the lookout's hand over the eyes, the folded arms, the stretch, the yawn, the knuckles
 * cracked, the shrug, the hand at the chin, the laugh, the foot tapping), with two of the Marines' own: the salute and standing at
 * ease. {@link GesturePose} poses them; the server picks one now and then for a fighter standing idle ({@link #pick}).
 * <p>2.4.3 (the handoff's open item, after HexGhoul's Ken, Touka and Eto, each with ways of its own): a persona to each rank. A captain
 * swaggers ({@link #SWAGGER}, {@link #BRUSH}), an officer gives orders ({@link #ORDER}), a recruit fidgets ({@link #FIDGET},
 * {@link #SCUFF}); each only by its own rank, over its side's own.
 */
public enum Gesture {
    NONE(0),
    /** A hand flat over the eyes against the glare, looking out to sea one way, then the other. */
    LOOKOUT(96),
    /** Arms folded, weight on one leg, chin turned a little away; a glance back now and then. */
    CROSS(120),
    /** Both arms up high, head back, up on the toes. */
    STRETCH(56),
    /** A hand to the mouth, head back; then the other arm stretched up. */
    YAWN(52),
    /** Both hands together in front, pushed out until the knuckles crack; a roll of the neck. */
    CRACK(60),
    /** Shoulders up, arms out, head rolled to one side: whatever. */
    SHRUG(40),
    /** A hand to the chin, the other arm across under it, head tipped: thinking. */
    CHIN(96),
    /** Head back, hands on the belly, shaking with it. */
    LAUGH(48),
    /** Arms folded, a foot tapping, head tipped: well? */
    TAP(80),
    /** A Marine's salute: the right hand snapped to the brow, held, brought down. */
    SALUTE(52),
    /** A Marine at ease: feet apart, hands behind the back, a slow sway. */
    EASE(140),
    /** 2.4.3, a captain's: dust brushed off the shoulder with the back of the hand, flicked away, the chin up after it. */
    BRUSH(64),
    /** 2.4.3, a captain's: chest out, chin up, elbows out, feet planted wide, a slow look over its crew from one side to the other. */
    SWAGGER(110),
    /** 2.4.3, an officer's: the arm thrown out to point the crew one way, a jab, swept to point another, a firm nod. */
    ORDER(72),
    /** 2.4.3, a recruit's: the weight from foot to foot, hands wrung in front, quick glances over each shoulder. */
    FIDGET(90),
    /** 2.4.3, a recruit's: head hung, a toe scuffed at the deck, then something kicked away, a sigh. */
    SCUFF(80);

    /** How long it lasts (ticks). */
    public final int ticks;

    Gesture(int ticks) {
        this.ticks = ticks;
    }

    public static Gesture byOrdinal(int ordinal) {
        Gesture[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : NONE;
    }

    /** Each side's gestures, with how often each comes (weights, in the order of {@link #values()}). */
    private static final int[] PIRATE = {0, 3, 3, 2, 2, 3, 2, 2, 3, 2, 0, 0, 0, 0, 0, 0, 0};
    private static final int[] MARINE = {0, 3, 2, 1, 1, 1, 0, 1, 0, 1, 3, 4, 0, 0, 0, 0, 0};
    /**
     * 2.4.3: what each rank adds over its side's (recruit, officer, captain; the same order): a recruit fidgets and stretches and yawns
     * more, an officer points and thinks, a captain swaggers, brushes itself off and laughs (a Marine captain salutes less: it is the
     * one saluted).
     */
    private static final int[][] RANKS = {
        {0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 3},
        {0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 4, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 2, 0, -2, 0, 3, 4, 0, 0, 0}};

    /** How often {@code gesture} comes for a fighter of {@code side} and {@code rank} (0 recruit, 1 officer, 2 captain): never below 0. */
    public static int weight(Faction side, int rank, Gesture gesture) {
        int[] base = side == Faction.MARINE ? MARINE : PIRATE;
        int[] extra = RANKS[Math.max(0, Math.min(RANKS.length - 1, rank))];
        int i = gesture.ordinal();
        return Math.max(0, base[i] + extra[i]);
    }

    /**
     * The gesture a fighter of {@code side} and {@code rank} starts for a random draw {@code roll} (0 inclusive to 1 exclusive); never
     * NONE.
     */
    public static Gesture pick(Faction side, int rank, double roll) {
        Gesture[] all = values();
        int total = 0;
        for (Gesture g : all) {
            total += weight(side, rank, g);
        }
        double at = Math.max(0.0, Math.min(0.999999, roll)) * total;
        for (Gesture g : all) {
            at -= weight(side, rank, g);
            if (at < 0.0) {
                return g;
            }
        }
        return LOOKOUT;
    }

    /** Ticks between one gesture's end and the next one's start, for a random draw {@code roll} (0 to 1): 6 to 18 seconds. */
    public static int pause(double roll) {
        return 120 + (int) Math.round(Math.max(0.0, Math.min(1.0, roll)) * 240.0);
    }

    /** True if it is over {@code age} ticks in. */
    public boolean over(double age) {
        return this == NONE || age >= this.ticks;
    }
}
