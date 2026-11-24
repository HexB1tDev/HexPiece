package com.hexhaki.piece.ironfist;

/**
 * 3.5.8 (the owner's Iron Fist remake, verbatim in docs/handoff 8zzs: Garp's fighting style, seven moves, the keys they accepted): the
 * moves as the panel, the logbook and both sides know them, with their beats (ticks, 20 a second). Pure: server/IronFist throws them,
 * client/ironfist/IronFx draws them on these very beats.
 */
public enum IronMove {
    PUNCH("Punches", "LMB", Need.NONE, 0,
        "A plain left punch, then the right, and back again."),
    LOVE("Fist of Love", "RMB", Need.NONE, 30,
        "The right fist raised overhead and brought straight down on their head with almost no wind-up: stunned and driven down. Cuts"
            + " into an attack, carries on a combo."),
    METEOR("Meteor Fist", "Z hold", Need.NONE, 100,
        "Hold: a black iron cannonball in the right hand, pulled back behind the head like a pitcher's wind-up; let go to hurl it, faster"
            + " than any shot, exploding on what it meets. The longer the hold, the bigger, faster and harder."),
    CRUSHER("Mountain Crusher", "X", Need.ARMAMENT, 160,
        "Low and wide, the coated fist clenched at the waist, then one tremendous step into an uppercut: launched up and away through"
            + " the blocks in their path. Breaks any guard."),
    BLUE_HOLE("Blue Hole", "C", Need.RYOU, 300,
        "Gone in a burst of speed to the one in your sights, grabbed by the face, lifted, and thrown straight down into the ground: a hole"
            + " fifteen blocks deep, stunned at its bottom."),
    DIVIDE("Galaxy Divide", "V", Need.RYOU, 400,
        "A leap up at them, the fist pulled back and loaded with Haki, then a punch into the air: a towering shockwave that splits the"
            + " world in front of you, forty blocks long."),
    INFINITUM("Infinitum Explosion", "B", Need.RYOU, 500,
        "A barrage of punches faster and faster until the arms blur, advancing and tearing the ground ahead, ended by one devastating"
            + " punch that blows everything away."),
    IMPACT("Galaxy Impact", "N", Need.CONQUEROR_RYOU, 3600,
        "The ultimate: a leap thirty-five blocks into the sky, the fist charged until it blinds, then one punch at the world below: a"
            + " dome of golden light fifty blocks wide, a crater torn out of the ground. Uses most of your Haki.");

    /** What a move needs on to be thrown. */
    public enum Need {
        NONE(""),
        ARMAMENT("Armament"),
        RYOU("Ryou"),
        CONQUEROR_RYOU("Conqueror's Haki and Ryou");

        public final String text;

        Need(String text) {
            this.text = text;
        }
    }

    private final String display;
    private final String key;
    private final Need need;
    private final int cooldown;
    private final String description;

    IronMove(String display, String key, Need need, int cooldown, String description) {
        this.display = display;
        this.key = key;
        this.need = need;
        this.cooldown = cooldown;
        this.description = description;
    }

    public String display() {
        return this.display;
    }

    public String key() {
        return this.key;
    }

    public Need need() {
        return this.need;
    }

    public int cooldown() {
        return this.cooldown;
    }

    public String description() {
        return this.description;
    }

    /** The panel's rows, in the keys' order (LMB, RMB, Z, X, C, V, B, N). */
    public static final IronMove[] PANEL = {PUNCH, LOVE, METEOR, CRUSHER, BLUE_HOLE, DIVIDE, INFINITUM, IMPACT};

    // --- the beats ------------------------------------------------------------------------------------------------------

    /** Punches: one every so many ticks, landing on this tick of it, so far. */
    public static final int PUNCH_EVERY = 6;
    public static final int PUNCH_HIT = 2;
    public static final double PUNCH_REACH = 3.2;
    /** Fist of Love: its whole length and its blow (almost no wind-up). */
    public static final int LOVE_TICKS = 8;
    public static final int LOVE_HIT = 2;
    public static final double LOVE_REACH = 3.4;
    /** Meteor Fist: a full hold, the longest; the throw lets go on this tick of its clip. */
    public static final int METEOR_FULL = 40;
    public static final int METEOR_MOST = 80;
    public static final int METEOR_THROW = 3;
    public static final int METEOR_FLIGHT = 40;

    public static double meteorRadius(double charge) {
        return 0.45 + 0.4 * charge;
    }

    public static double meteorSpeed(double charge) {
        return 2.4 + 1.8 * charge;
    }

    public static double meteorBlast(double charge) {
        return 3.0 + 3.0 * charge;
    }

    /** How full Meteor Fist is after {@code held} ticks (a tap is a third). */
    public static double meteorCharge(double held) {
        return Math.max(0.33, Math.min(1.0, held / METEOR_FULL));
    }

    /** Mountain Crusher: the coil, the step and uppercut, its whole length, how far it reaches, how long the launched body tears blocks. */
    public static final int CRUSHER_COIL = 10;
    public static final int CRUSHER_HIT = 13;
    public static final int CRUSHER_TICKS = 24;
    public static final double CRUSHER_REACH = 3.8;
    public static final int CRUSHER_TEAR = 14;
    /** Blue Hole: the crouch, the dash, the grab held (lifted), the slam, the hole's depth, the stun at its bottom, the reach. */
    public static final int HOLE_CROUCH = 4;
    public static final int HOLE_DASH = 6;
    public static final int HOLE_LIFT = 14;
    public static final int HOLE_DEPTH = 15;
    public static final int HOLE_STUN = 80;
    public static final double HOLE_RANGE = 20.0;
    /** Galaxy Divide: the crouch, the leap, the punch, the shock's speed, length, height and width. */
    public static final int DIVIDE_CROUCH = 6;
    public static final int DIVIDE_LEAP = 10;
    public static final int DIVIDE_PUNCH = 20;
    public static final double DIVIDE_SPEED = 2.0;
    public static final double DIVIDE_LENGTH = 40.0;
    public static final double DIVIDE_HEIGHT = 20.0;
    public static final double DIVIDE_WIDTH = 5.0;
    /** Infinitum Explosion: the stance, the barrage, the finishing punch's wind-up and blow, its whole length. */
    public static final int INF_STANCE = 10;
    public static final int INF_END = 70;
    public static final int INF_FINISH = 80;
    public static final int INF_TICKS = 94;

    /** The barrage's punch {@code k}'s tick: every 4 ticks at first, faster and faster to every tick. */
    public static int barrageTick(int k) {
        double t = INF_STANCE;
        for (int i = 0; i < k; i++) {
            t += Math.max(1.0, 4.0 - i * 0.12);
        }
        return (int) Math.round(t);
    }

    /** How many punches the barrage throws before its end. */
    public static int barrageCount() {
        int k = 0;
        while (barrageTick(k) < INF_END) {
            k++;
        }
        return k;
    }

    /** Galaxy Impact: the crouch, the leap and how high, the top, the silence, the punch, the force's speed down, the dome, the descent. */
    public static final int IMPACT_JUMP = 30;
    public static final int IMPACT_TOP = 50;
    public static final double IMPACT_HEIGHT = 35.0;
    public static final int IMPACT_SILENCE = 100;
    public static final int IMPACT_PUNCH = 104;
    public static final double IMPACT_FALL = 5.0;
    public static final double IMPACT_DOME = 50.0;
    public static final int IMPACT_DOME_TICKS = 30;
    public static final double IMPACT_CRATER = 15.0;
    public static final int IMPACT_DESCEND = 150;
    public static final int IMPACT_TICKS = 180;
    /** The share of the Haki reserves it takes. */
    public static final double IMPACT_ENERGY = 0.6;

    /** How high over its start the body is {@code t} ticks into Galaxy Impact (blocks): it shoots up and slows, hangs, comes down. */
    public static double impactRise(double t) {
        if (t <= IMPACT_JUMP) {
            return 0.0;
        }
        if (t < IMPACT_TOP) {
            double u = (t - IMPACT_JUMP) / (IMPACT_TOP - IMPACT_JUMP);
            return IMPACT_HEIGHT * (1.0 - (1.0 - u) * (1.0 - u) * (1.0 - u));
        }
        if (t < IMPACT_DESCEND) {
            return IMPACT_HEIGHT;
        }
        double u = Math.min(1.0, (t - IMPACT_DESCEND) / (IMPACT_TICKS - 4 - IMPACT_DESCEND));
        return IMPACT_HEIGHT * (1.0 - u * u * (3.0 - 2.0 * u));
    }

    /** The fist's charge 0 to 1 while it hangs in the sky (it pulses three times at the end). */
    public static double impactCharge(double t) {
        double u = Math.max(0.0, Math.min(1.0, (t - IMPACT_TOP) / (IMPACT_SILENCE - IMPACT_TOP)));
        double pulse = t > IMPACT_SILENCE - 15 && t < IMPACT_SILENCE ? 0.12 * Math.abs(Math.sin((t - (IMPACT_SILENCE - 15)) * Math.PI / 5.0)) : 0.0;
        return Math.min(1.0, u * u + pulse);
    }

    /** When the force reaches the ground {@code drop} blocks under the fist. */
    public static double impactLanding(double drop) {
        return IMPACT_PUNCH + Math.max(1.0, drop / IMPACT_FALL);
    }
}
