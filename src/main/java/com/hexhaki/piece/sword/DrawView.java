package com.hexhaki.piece.sword;

/**
 * 2.5.6 (the owner: "the pull out animation is horrid, if you can even call it that, the pull out animation needs to be really
 * gorgeous to look at, sheath and unsheath both", "I mean the first person"): the first-person draw and sheathing, and the drawn
 * blade held between cuts (pure: client/sword/HolsterHand plays it, tools/draw_preview.py renders it from these same frames). Until
 * 2.5.5 first person had no draw at all: the left hand lay on the hilt, then the bare blade simply appeared in the hand.
 *
 * <p>The draw (iai's nukitsuke, slow and deliberate as the owner asked in 2.2.1): the left hand slides from the end of the handle down
 * to the saya's mouth as the right hand comes up from below and closes on the handle; the left thumb breaks the seal (koiguchi wo
 * kiru), the blade jumping a finger's width out with a glint; a held breath; then the long draw, the blade sliding out along the
 * saya's own curve (sword/SayaSlide) while the left hand draws the saya back (saya-biki); the point leaves the mouth and the blade
 * whips out across the view in one fast arc, edge leading, and settles with its weight into the hand. The sheathing (noto): the blade
 * flicked clean (chiburi), held a beat, swung back and its back laid on the left hand at the mouth, slid home slowly along the curve,
 * the guard clicking home; the right hand lets go and the left goes back to rest on the end of the handle.
 *
 * <p>Every part is a rigid frame (a place in the view, blocks; a turn, a quaternion x, y, z, w) keyed in time with an easing; the blade
 * in the saya is the saya's frame slid along its curve; the right hand gripping is the blade's frame times one fixed grip. The game
 * applies each frame as steps on the hand's pose ({@link HolsterView#MOVE}, {@link #ROTATE}): the arms through the game's own
 * {@code renderLeftHand}/{@code renderRightHand} (their part then moves to its pivot and sways), the katana through its renderer.
 */
public final class DrawView {
    /** A step: rotate by an angle (degrees) about an axis: {4, angle, x, y, z}. */
    public static final int ROTATE = 4;

    /** What is drawn at a moment: the saya's steps, the blade's, the right arm's and the left arm's (null: not drawn), the blade's gleam. */
    public record Pose(float[][] saya, float[][] blade, float[][] right, float[][] left, float shine) {
    }

    /** A rigid frame: where (view, blocks) and its turn (unit quaternion x, y, z, w). */
    public record Frame(double[] p, double[] q) {
    }

    /** The draw's moments for a katana's 28 ticks, scaled to the blade's own length (Gryphon's 32), the blade free at {@link #FREE}. */
    static final float GRIP_AT = 6.0F;
    static final float PUSH_AT = 9.0F;
    static final float HOLD_AT = 11.0F;
    static final float CLEAR_AT = 19.5F;
    static final float FREE = 20.0F;
    static final float ARC_AT = 22.5F;
    static final float DRAW_END = 28.0F;
    /** 2.6.1: how long the empty saya takes to sink out of the view after the draw, or to rise into it before the sheathing (ticks). */
    static final float SINK_TICKS = 5.0F;
    /** Model px out of the saya: the seal broken. */
    static final float PUSH = 1.4F;
    /** The sheathing's moments for a katana's 24 ticks (the guard home at 21). */
    static final float FLICK_AT = 4.0F;
    static final float FLICK_HOLD = 7.0F;
    static final float LAY_AT = 10.0F;
    static final float REST_AT = 12.0F;
    static final float DEEP_AT = 18.0F;
    static final float HOME_AT = 21.0F;
    static final float SHEATHE_END = 24.0F;

    // ------------------------------------------------------------------------------------------------------- the poses, by intent

    /** The drawn blade held between cuts: the grip, the blade's way, its edge (view). The blade forward and up from the right hand. */
    static final double[] HELD_GRIP = {0.42, -0.48, -0.92};
    static final double[] HELD_BLADE = {-0.32, 0.86, -0.40};
    static final double[] HELD_EDGE = {-0.6, 0.0, -0.8};
    /** 2.6.1: how much the view's up steadies the right fist's roll against the blade's way ({@link #gripped}). */
    static final double ROLL_STEADY = 0.6;
    /** The right shoulder, below the lower right of the view: every right arm runs from here to its fist. */
    static final double[] R_SHOULDER = {0.50, -1.0, -0.38};
    /** Where the right arm waits below the view (its fist), and the left arm's way out to the lower left. */
    static final double[] RIGHT_AWAY = {0.62, -1.15, -0.85};
    static final double[] LEFT_AWAY = {-0.62, -1.15, -0.8};
    /** The left fist at the saya's mouth (model px from the guard down the saya, and above it), the thumb on the guard. */
    static final double MOUTH_DOWN = 3.2;
    static final double MOUTH_OVER = 1.2;
    /** Saya-biki: how far the left hand turns the saya back as the blade comes out (degrees, about the mouth). */
    static final double SAYA_BIKI = 9.0;
    /** ... and how far it draws the saya back along its own line meanwhile (model px), so the right hand need not reach so far. */
    static final double SAYA_PULL = 7.0;
    /**
     * 2.6.1 (the owner, on 2.6.0's draw: "wtf is this sequence", the blade flipping up, right, up again in a tick and a half): once the
     * point leaves the mouth the blade rises in ONE arc, its point turning one way only (clockwise in the view) from the saya's line to
     * the held blade, never pointing at the camera; this is what the arc is drawn toward (a curve, {@link #curve}), the hand pushing it
     * forward and up as the point clears. Its edge turns from up (as in the saya) to the held blade's in the fastest tick: the blade
     * edge-on for a moment, a glint along it.
     */
    static final double[] SPRING_GRIP = {0.50, -0.36, -1.24};
    static final double[] SPRING_BLADE = {-0.96, 0.22, -0.16};
    static final double[] SPRING_EDGE = {0.0, 0.25, -1.0};
    /**
     * 2.6.1: the sheathing's snap (chiburi), the same way the noto goes on: the blade brought down from the held blade to the left with a
     * snap, turned edge up as it comes (as it lies in the saya), held a beat with its flat to the eye; then laid at the mouth.
     */
    static final double[] FLICK_GRIP = {0.52, -0.44, -1.06};
    static final double[] FLICK_BLADE = {-0.994, -0.105, 0.05};
    static final double[] FLICK_EDGE = {0.1, 1.0, 0.15};
    /** 2.6.1: how far the empty saya sinks out of the view once the blade is out (and rises back before the sheathing), blocks. */
    static final double SAYA_SINK = 0.55;

    private DrawView() {
    }

    // ------------------------------------------------------------------------------------------------------------ the moments

    /** The sheathed view (the left hand resting on the end of the handle): {@link HolsterView}. */
    public static Pose holster(Katana katana, boolean slim, float equip, float age) {
        return new Pose(HolsterView.katana(equip, age), HolsterView.katana(equip, age), null, HolsterView.arm(slim, equip, age), 0.0F);
    }

    /** The drawn blade held between cuts, the right hand on its handle. */
    public static Pose held(Katana katana, boolean slim, float equip, float age) {
        double sz = katana.size();
        Frame blade = lifted(heldBlade(sz), equip, age);
        return new Pose(null, steps(blade), rightArm(gripped(blade, sz), slim), null, katana.blackens() ? 1.0F : 0.45F);
    }

    /**
     * The draw, {@code t} ticks in, of a draw {@code length} ticks long whose blade the server counts out of the saya at {@code free}
     * (BladePaths.drawTick): the slide ends, the point at the mouth, exactly then.
     */
    public static Pose draw(Katana katana, boolean slim, float t, float length, float free, float equip, float age) {
        double sz = katana.size();
        float k = warp(t, length, free, CLEAR_AT, DRAW_END);
        Frame saya = saya(sayaBiki(k));
        Frame blade;
        float shine;
        if (k < CLEAR_AT) {
            blade = slid(katana, saya, slideOut(katana, k));
            shine = k < PUSH_AT - 1.5F ? 0.0F : 1.0F;
        } else {
            // 2.6.1: the point free, the blade springs out to the left and rises in one arc into the hand, fastest as it leaves the
            // mouth and settling slowly (the exit taken with the saya as it was at the mouth: the start never moves)
            Frame exit = slid(katana, saya(sayaBiki(CLEAR_AT)), clear(katana));
            blade = curve(ease("outquart", (k - CLEAR_AT) / (DRAW_END - CLEAR_AT)), exit, spring(sz), heldBlade(sz));
            shine = 1.0F;
        }
        blade = lifted(blade, equip, age);
        // the right hand: up from below to the handle, then on it
        Frame grip = gripped(blade, sz);
        Frame right = k < GRIP_AT ? between(away(RIGHT_AWAY, grip), grip, ease("outcubic", k / GRIP_AT)) : grip;
        // the left hand: from the end of the handle to the mouth; away once the point has left
        Frame rest = restLeft();
        Frame mouth = lifted(mouth(saya), equip, age);
        Frame left;
        if (k < 4.0F) {
            left = between(lifted(rest, equip, age), mouth, ease("inoutsine", k / 4.0F));
        } else if (k < FREE) {
            left = mouth;
        } else {
            left = between(mouth, away(LEFT_AWAY, mouth), ease("insine", Math.min(1.0F, (k - FREE) / 4.0F)));
        }
        // 2.6.1: the empty saya sinks out of the view (until 2.6.0 it vanished at once four ticks after the point left)
        double sink = k < FREE ? 0.0 : SAYA_SINK * ease("insine", (k - FREE) / SINK_TICKS);
        boolean sayaShown = k < FREE + SINK_TICKS;
        return new Pose(sayaShown ? steps(lifted(sunk(saya, sink), equip, age)) : null, steps(blade), rightArm(right, slim),
            k < FREE + 4.0F ? leftArm(left, slim) : null, shine);
    }

    /** The sheathing, {@code t} ticks in, of one {@code length} ticks long, the guard home at {@code home} (BladePaths.sheathTick). */
    public static Pose sheathe(Katana katana, boolean slim, float t, float length, float home, float equip, float age) {
        double sz = katana.size();
        float k = warp(t, length, home, HOME_AT, SHEATHE_END);
        Frame saya = saya(k < REST_AT ? SAYA_BIKI : k < HOME_AT ? SAYA_BIKI * (1.0 - ease("inoutsine", (k - REST_AT) / (HOME_AT - REST_AT)))
            : 0.0);
        Frame blade;
        if (k < LAY_AT) {
            // 2.6.1: the blade snapped down to the left (its point one way only) edge up, held a beat with its flat to the eye, then
            // carried to the mouth (until 2.6.0: flicked right, then swung back up and over to the left, the fist twisting)
            Frame laid = lean(slid(katana, saya(SAYA_BIKI), clear(katana)), 0.35, sz);
            blade = keyed(k, new float[] {0.0F, FLICK_AT, FLICK_HOLD, LAY_AT},
                new Frame[] {heldBlade(sz), flick(sz), settled(flick(sz), sz), laid},
                new String[] {"outquad", "inoutsine", "inoutsine"});
        } else if (k < REST_AT) {
            blade = between(lean(slid(katana, saya, clear(katana)), 0.35, sz), slid(katana, saya, clear(katana)), ease("outsine", (k - LAY_AT) / (REST_AT - LAY_AT)));
        } else {
            blade = slid(katana, saya, sheatheOut(katana, k));
        }
        blade = lifted(blade, equip, age);
        Frame grip = gripped(blade, sz);
        Frame right = k < HOME_AT ? grip : between(grip, away(RIGHT_AWAY, grip), ease("insine", Math.min(1.0F, (k - HOME_AT) / 3.0F)));
        Frame mouth = lifted(mouth(saya), equip, age);
        Frame rest = lifted(restLeft(), equip, age);
        Frame left;
        if (k < FLICK_HOLD) {
            left = null;
        } else if (k < LAY_AT) {
            left = between(away(LEFT_AWAY, mouth), mouth, ease("outsine", (k - FLICK_HOLD) / (LAY_AT - FLICK_HOLD)));
        } else if (k < HOME_AT) {
            left = mouth;
        } else {
            left = between(mouth, rest, ease("inoutsine", Math.min(1.0F, (k - HOME_AT) / 3.0F)));
        }
        // 2.6.1: the saya rises back into the view as the left hand comes to its mouth (until 2.6.0 it appeared at once)
        double sink = k >= LAY_AT ? 0.0 : SAYA_SINK * (1.0 - ease("outsine", (k - (LAY_AT - SINK_TICKS)) / SINK_TICKS));
        boolean sayaShown = k >= LAY_AT - SINK_TICKS;
        return new Pose(sayaShown ? steps(lifted(sunk(saya, sink), equip, age)) : null, steps(blade),
            k < SHEATHE_END ? rightArm(right, slim) : null, left == null ? null : leftArm(left, slim),
            k >= REST_AT && k < HOME_AT ? 1.0F : 0.45F);
    }

    /**
     * 2.6.1 (the owner: "the swords man right click has no first person animation"): the right click's charge (the Haki slash held until
     * it is let go; third person: "the blade raised high behind the right shoulder, the body drawn back like a bow"), seen from the
     * eye: the fist up at the right of the view, the blade drawn back over the right shoulder (its point past the top of the view, behind
     * the eye), trembling harder as the charge builds. The release (a swing) is playerAnimator's, from this same place.
     */
    static final double[] CHARGE_GRIP = {0.62, 0.0, -0.86};
    static final double[] CHARGE_BLADE = {-0.24, 0.76, 0.60};
    static final double[] CHARGE_EDGE = {0.0, 0.3, -1.0};
    /** How long the blade takes to be drawn back into the charge from the held blade (ticks; the third person's 5). */
    static final float CHARGE_RAISE = 5.0F;
    /** The charge's tremble: the least and most (degrees), how long it takes to reach the most (ticks), its two rates (per tick). */
    static final double TREMBLE_LEAST = 0.35;
    static final double TREMBLE_MOST = 1.6;
    static final float TREMBLE_BUILD = 40.0F;
    static final double TREMBLE_SLOW = 0.155;
    static final double TREMBLE_FAST = 0.37;
    /** The charge drawn straight from the saya (third person: out by its second tick, in the charge by its fifth): its moments. */
    static final float QUICK_GRIP = 1.0F;
    static final float QUICK_CLEAR = 2.2F;
    static final float QUICK_UP = 6.5F;
    /** Where the blade springs to as it leaves the saya on its way up into the charge. */
    static final double[] QUICK_GRIP_AT = {0.56, -0.30, -1.10};
    static final double[] QUICK_BLADE = {-0.85, 0.50, -0.10};
    static final double[] QUICK_EDGE = {0.0, 0.25, -1.0};

    /** The charge from the blade held between cuts: drawn back over the shoulder, then held trembling ({@code t} ticks in). */
    public static Pose charge(Katana katana, boolean slim, float t, float equip, float age) {
        double sz = katana.size();
        Frame blade = t < CHARGE_RAISE ? between(heldBlade(sz), chargeBlade(sz), ease("inoutsine", t / CHARGE_RAISE)) : chargeBlade(sz);
        blade = lifted(trembled(blade, t - CHARGE_RAISE, sz), equip, age);
        return new Pose(null, steps(blade), rightArm(gripped(blade, sz), slim), null, 1.0F);
    }

    /**
     * The charge drawn from the saya ({@code t} ticks in): the left hand to the mouth as the right closes on the handle, the blade
     * slid out fast and swept up and back over the shoulder in one arc, then held trembling; the empty saya sinks out of the view.
     */
    public static Pose chargeDraw(Katana katana, boolean slim, float t, float equip, float age) {
        double sz = katana.size();
        Frame saya = saya(0.0);
        Frame blade;
        if (t < QUICK_CLEAR) {
            float out = t < 0.4F ? 0.0F : (float) (clear(katana) * ease("insine", (t - 0.4F) / (QUICK_CLEAR - 0.4F)));
            blade = slid(katana, saya, out);
        } else {
            Frame exit = slid(katana, saya, clear(katana));
            blade = curve(ease("outquad", (t - QUICK_CLEAR) / (QUICK_UP - QUICK_CLEAR)), exit, byIntent(QUICK_GRIP_AT, QUICK_BLADE, QUICK_EDGE, sz),
                chargeBlade(sz));
        }
        blade = lifted(trembled(blade, t - QUICK_UP, sz), equip, age);
        Frame grip = gripped(blade, sz);
        Frame right = t < QUICK_GRIP ? between(away(RIGHT_AWAY, grip), grip, ease("outcubic", t / QUICK_GRIP)) : grip;
        Frame mouth = lifted(mouth(saya), equip, age);
        Frame left;
        if (t < QUICK_GRIP) {
            left = between(lifted(restLeft(), equip, age), mouth, ease("inoutsine", t / QUICK_GRIP));
        } else if (t < QUICK_CLEAR) {
            left = mouth;
        } else {
            left = between(mouth, away(LEFT_AWAY, mouth), ease("insine", Math.min(1.0F, (t - QUICK_CLEAR) / 3.0F)));
        }
        double sink = t < QUICK_CLEAR ? 0.0 : SAYA_SINK * ease("insine", (t - QUICK_CLEAR) / SINK_TICKS);
        return new Pose(t < QUICK_CLEAR + SINK_TICKS ? steps(lifted(sunk(saya, sink), equip, age)) : null, steps(blade),
            rightArm(right, slim), t < QUICK_CLEAR + 3.0F ? leftArm(left, slim) : null, 1.0F);
    }

    /** Yoru's charge off its back ({@code t} ticks in, out of its ropes at {@code free}): down over the shoulder into the charge. */
    public static Pose backCharge(Katana katana, boolean slim, float t, float free, float equip, float age) {
        double sz = katana.size();
        if (t < free) {
            return new Pose(null, null, null, null, 0.0F);
        }
        Frame over = byIntent(OVER_GRIP, OVER_BLADE, OVER_EDGE, sz);
        Frame blade = keyed(t, new float[] {free, free + BACK_SWING}, new Frame[] {over, chargeBlade(sz)}, new String[] {"inoutsine"});
        blade = lifted(trembled(blade, t - free - BACK_SWING, sz), equip, age);
        return new Pose(null, steps(blade), rightArm(gripped(blade, sz), slim), null, 1.0F);
    }

    static Frame chargeBlade(double sz) {
        return byIntent(CHARGE_GRIP, CHARGE_BLADE, CHARGE_EDGE, sz);
    }

    /**
     * {@code f} trembling {@code held} ticks into the hold (nothing before): two slow, smooth swayings about the grip (never a jolt
     * from one frame to the next), growing from {@link #TREMBLE_LEAST} to {@link #TREMBLE_MOST} degrees as the charge builds.
     */
    static Frame trembled(Frame f, float held, double sz) {
        if (held <= 0.0F) {
            return f;
        }
        double grow = Math.min(1.0, held / TREMBLE_BUILD);
        double in = Math.min(1.0, held / 3.0); // eased in over the first ticks of the hold
        double amount = in * (TREMBLE_LEAST + (TREMBLE_MOST - TREMBLE_LEAST) * grow);
        double a = amount * (Math.sin(2 * Math.PI * TREMBLE_SLOW * held) + 0.5 * Math.sin(2 * Math.PI * TREMBLE_FAST * held + 1.3)) / 1.5;
        double b = amount * 0.7 * Math.sin(2 * Math.PI * TREMBLE_FAST * 0.83 * held + 0.4);
        double[] grip = point(f, 0, KatanaGrip.GRIP * sz, 0);
        double[] q = mul(axisAngle(new double[] {0, 0, 1}, Math.toRadians(a)), mul(axisAngle(new double[] {1, 0, 0}, Math.toRadians(b)), f.q()));
        Frame turned = new Frame(f.p(), q);
        double[] moved = point(turned, 0, KatanaGrip.GRIP * sz, 0);
        return new Frame(HolsterView.add(f.p(), HolsterView.sub(grip, moved)), q);
    }

    /** Yoru's moves (off its back, onto it): above the view, over the right shoulder, the blade pointing back. */
    static final double[] OVER_GRIP = {0.62, 0.62, -0.55};
    static final double[] OVER_BLADE = {-0.25, 0.55, 0.80};
    static final double[] OVER_EDGE = {0.0, 1.0, 0.0};
    /** How long Yoru takes to come down into the hand once it is out of its ropes, and to rise out of the view going back (ticks). */
    static final float BACK_SWING = 6.0F;

    /**
     * Yoru's draw ({@code t} ticks in, out of its ropes at {@code free}): the hands empty while the right hand reaches back over the
     * shoulder (out of the view); then the great sword swung down over the right shoulder into the hand, settling with its weight.
     */
    public static Pose backDraw(Katana katana, boolean slim, float t, float free, float equip, float age) {
        double sz = katana.size();
        if (t < free) {
            return new Pose(null, null, null, null, 0.0F);
        }
        Frame over = byIntent(OVER_GRIP, OVER_BLADE, OVER_EDGE, sz);
        Frame blade = lifted(keyed(t, new float[] {free, free + BACK_SWING}, new Frame[] {over, heldBlade(sz)}, new String[] {"outback"}),
            equip, age);
        return new Pose(null, steps(blade), rightArm(gripped(blade, sz), slim), null, 1.0F);
    }

    /** Yoru's sheathing: swung up and back over the right shoulder, out of the view by {@code home}; then the hands are empty. */
    public static Pose backSheathe(Katana katana, boolean slim, float t, float home, float equip, float age) {
        double sz = katana.size();
        float from = Math.max(0.0F, home - BACK_SWING);
        if (t >= home) {
            return new Pose(null, null, null, null, 0.0F);
        }
        Frame over = byIntent(OVER_GRIP, OVER_BLADE, OVER_EDGE, sz);
        Frame blade = lifted(keyed(t, new float[] {from, home}, new Frame[] {heldBlade(sz), over}, new String[] {"insine"}), equip, age);
        return new Pose(null, steps(blade), rightArm(gripped(blade, sz), slim), null, 0.45F);
    }

    /**
     * A moment {@code t} of a move {@code length} ticks long, as the moment of the katana's own timeline (which ends at {@code end}) so
     * that its key moment {@code key} falls on the move's {@code at}: the draw's point at the mouth on the server's draw tick, the
     * sheathing's guard home on its sheath tick, whatever the blade's lengths.
     */
    static float warp(float t, float length, float at, float key, float end) {
        if (at <= 0.0F || at >= length) {
            return t * end / Math.max(1.0F, length);
        }
        return t <= at ? t * key / at : key + (t - at) * (end - key) / (length - at);
    }

    /** Model px out of the saya with the point at the mouth: the blade's own length past the guard, and a little more. */
    static float clear(Katana katana) {
        return katana.tip() + 0.6F;
    }

    /** How far out of the saya the blade is in the draw (model px), {@code k} ticks in (a katana's time). */
    static float slideOut(Katana katana, float k) {
        if (k < PUSH_AT - 1.5F) {
            return 0.0F;
        }
        if (k < PUSH_AT) {
            return (float) (PUSH * ease("outback", (k - (PUSH_AT - 1.5F)) / 1.5F));
        }
        if (k < HOLD_AT) {
            return PUSH;
        }
        return (float) (PUSH + (clear(katana) - PUSH) * ease("inoutsine", (k - HOLD_AT) / (CLEAR_AT - HOLD_AT)));
    }

    /** How far out the blade is in the sheathing (model px), from the point at the mouth to home. */
    static float sheatheOut(Katana katana, float k) {
        if (k < DEEP_AT) {
            return (float) (clear(katana) + (3.0 - clear(katana)) * ease("inoutsine", (k - REST_AT) / (DEEP_AT - REST_AT)));
        }
        if (k < HOME_AT) {
            return (float) (3.0 * (1.0 - ease("outsine", (k - DEEP_AT) / (HOME_AT - DEEP_AT))));
        }
        return 0.0F;
    }

    /** Saya-biki through the draw (degrees): drawn back with the blade, eased home as the blade swings free. */
    static double sayaBiki(float k) {
        if (k < HOLD_AT) {
            return 0.0;
        }
        if (k < CLEAR_AT) {
            return SAYA_BIKI * ease("inoutsine", (k - HOLD_AT) / (CLEAR_AT - HOLD_AT));
        }
        return SAYA_BIKI * (1.0 - ease("outsine", Math.min(1.0F, (k - CLEAR_AT) / 4.0F)));
    }

    // ------------------------------------------------------------------------------------------------------------ the frames

    /** The saya at the hip (the holster view's frame), turned back {@code biki} degrees about its mouth (the guard's place). */
    static Frame saya(double biki) {
        double[][] r = HolsterView.katanaFrame();
        Frame base = new Frame(HolsterView.GUARD.clone(), quat(r));
        if (biki == 0.0) {
            return base;
        }
        // drawn back along its line, and turned about the view's up-ish axis through the mouth: the handle swung left and back
        double[] axis = HolsterView.norm(new double[] {0.15, 1.0, 0.25});
        double[] q = mul(axisAngle(axis, Math.toRadians(-biki)), base.q());
        double[] back = rotate(q, new double[] {0, SAYA_PULL * Katana.SIZE * (biki / SAYA_BIKI) / 16.0, 0});
        return new Frame(HolsterView.add(base.p(), back), q);
    }

    /** The blade slid {@code out} model px out of the saya along its curve ({@link SayaSlide}). */
    static Frame slid(Katana katana, Frame saya, float out) {
        float[] r = SayaSlide.at(katana, out);
        double[] local = {r[1] * katana.size() / 16.0, r[2] * katana.size() / 16.0, 0.0}; // 2.6.2: the blade's own size
        double[] q = mul(saya.q(), axisAngle(new double[] {0, 0, 1}, Math.toRadians(r[0])));
        return new Frame(HolsterView.add(saya.p(), rotate(saya.q(), local)), q);
    }

    /** {@code f} turned about its grip a little outward ({@code k} of a quarter turn about the view's up): the point leaving the line. */
    static Frame lean(Frame f, double k, double sz) {
        double[] grip = point(f, 0, KatanaGrip.GRIP * sz, 0);
        double[] q = mul(axisAngle(new double[] {0, 1, 0}, -k * Math.PI / 4.0), f.q());
        Frame turned = new Frame(f.p(), q);
        double[] moved = point(turned, 0, KatanaGrip.GRIP * sz, 0);
        return new Frame(HolsterView.add(f.p(), HolsterView.sub(grip, moved)), q);
    }

    static Frame heldBlade(double sz) {
        return byIntent(HELD_GRIP, HELD_BLADE, HELD_EDGE, sz);
    }

    static Frame spring(double sz) {
        return byIntent(SPRING_GRIP, SPRING_BLADE, SPRING_EDGE, sz);
    }

    static Frame flick(double sz) {
        return byIntent(FLICK_GRIP, FLICK_BLADE, FLICK_EDGE, sz);
    }

    /** The snap's end held a beat: drifted a hair back toward the body and the point a hair lower (a held pose that still breathes). */
    static Frame settled(Frame f, double sz) {
        double[] grip = point(f, 0, KatanaGrip.GRIP * sz, 0);
        double[] q = mul(axisAngle(new double[] {0, 0, 1}, Math.toRadians(2.5)), f.q());
        Frame turned = new Frame(f.p(), q);
        double[] moved = point(turned, 0, KatanaGrip.GRIP * sz, 0);
        return new Frame(HolsterView.add(HolsterView.add(f.p(), HolsterView.sub(grip, moved)), new double[] {-0.02, -0.01, 0.03}), q);
    }

    /** {@code f} lowered {@code by} blocks (the view's down). */
    static Frame sunk(Frame f, double by) {
        return by == 0.0 ? f : new Frame(HolsterView.add(f.p(), new double[] {0, -by, 0}), f.q());
    }

    /** A blade's frame with its grip (the fist's centre on the handle) at {@code grip}, pointing {@code blade}, its edge toward {@code edge}. */
    static Frame byIntent(double[] grip, double[] blade, double[] edge, double sz) {
        double[] y = HolsterView.norm(blade);
        double[] e = HolsterView.norm(HolsterView.sub(edge, HolsterView.scale(y, HolsterView.dot(edge, y))));
        double[] x = HolsterView.scale(e, -1);
        double[] z = HolsterView.cross(x, y);
        double[] q = quat(HolsterView.columns(x, y, z));
        double[] origin = HolsterView.sub(grip, rotate(q, new double[] {0, KatanaGrip.GRIP * sz / 16.0, 0}));
        return new Frame(origin, q);
    }

    /**
     * The right arm's frame gripping the blade in {@code blade}: the fist's centre on the handle ({@code KatanaGrip.GRIP}), the arm
     * running to it from the shoulder below the lower right of the view ({@link #R_SHOULDER}), the blade standing out of the fist's
     * front as the game holds an item (the arm's -z), so the wrist turns with the blade and the arm never swings through the view.
     */
    static Frame gripped(Frame blade, double sz) {
        double[] fist = point(blade, 0, KatanaGrip.GRIP * sz, 0);
        double[] y = HolsterView.norm(HolsterView.sub(fist, R_SHOULDER));
        double[] b = rotate(blade.q(), new double[] {0, 1, 0});
        // 2.6.1: the roll from the blade's way across the arm, steadied by the view's up across it: when the blade nearly continues
        // the arm (the held blade is 23 degrees off it) its way across is a tiny vector that swung the fist round by up to 190 degrees
        // a tick (2.6.0's draw settling, and its sheathing's start); the view's up never lies along the arm
        double[] across = HolsterView.sub(b, HolsterView.scale(y, HolsterView.dot(b, y)));
        double[] up = {0, 1, 0};
        double[] steady = HolsterView.sub(up, HolsterView.scale(y, HolsterView.dot(up, y)));
        double[] front = HolsterView.add(across, HolsterView.scale(steady, ROLL_STEADY));
        if (HolsterView.dot(front, front) < 1.0E-6) {
            front = steady;
        }
        double[] z = HolsterView.scale(HolsterView.norm(front), -1);
        double[] x = HolsterView.cross(y, z);
        return new Frame(fist, quat(HolsterView.columns(x, y, z)));
    }

    /** The left fist at the saya's mouth: a little down the saya from the guard and over it, the forearm along the holster's. */
    static Frame mouth(Frame saya) {
        double[] at = point(saya, 0, MOUTH_DOWN * Katana.SIZE, 0); // 2.6.2: the saya drawn Katana.SIZE times its mesh
        double[] up = rotate(saya.q(), new double[] {-1, 0, 0});
        double[] fist = HolsterView.add(at, HolsterView.scale(up, MOUTH_OVER * Katana.SIZE / 16.0));
        return new Frame(fist, restLeft().q());
    }

    /** The left arm resting on the end of the handle (the holster view's). */
    static Frame restLeft() {
        return new Frame(HolsterView.fist(), quat(HolsterView.armFrame()));
    }

    /** {@code f} moved to {@code where} (its fist; the turn kept): off the view. */
    static Frame away(double[] where, Frame f) {
        return new Frame(where.clone(), f.q());
    }

    /** {@code f} sinking as the sword is taken up, and breathing. */
    static Frame lifted(Frame f, float equip, float age) {
        if (f == null) {
            return null;
        }
        double[] up = HolsterView.lift(equip, age);
        return new Frame(HolsterView.add(f.p(), up), f.q());
    }

    // ------------------------------------------------------------------------------------------------------------ to steps

    /** A frame as steps: move, then rotate. */
    public static float[][] steps(Frame f) {
        double[] aa = axisAngleOf(f.q());
        return new float[][] {{HolsterView.MOVE, (float) f.p()[0], (float) f.p()[1], (float) f.p()[2]},
            {ROTATE, (float) Math.toDegrees(aa[0]), (float) aa[1], (float) aa[2], (float) aa[3]}};
    }

    /** The right arm's steps for renderRightHand: its fist's centre at the frame, the part's pivot and sway undone. */
    static float[][] rightArm(Frame f, boolean slim) {
        float[] pivot = slim ? new float[] {-5.0F, 2.5F, 0.0F} : new float[] {-5.0F, 2.0F, 0.0F};
        float[] centre = slim ? new float[] {-0.5F, 8.5F, 0.0F} : new float[] {-1.0F, 8.5F, 0.0F};
        return arm(f, pivot, centre, 0.1F);
    }

    /** The left arm's steps for renderLeftHand (as {@link HolsterView#arm}). */
    static float[][] leftArm(Frame f, boolean slim) {
        float[] pivot = slim ? HolsterView.PIVOT_SLIM : HolsterView.PIVOT_WIDE;
        float[] centre = slim ? HolsterView.FIST_SLIM : HolsterView.FIST_WIDE;
        return arm(f, pivot, centre, HolsterView.IDLE_ROLL);
    }

    static float[][] arm(Frame f, float[] pivot, float[] centre, float sway) {
        float[][] s = steps(f);
        return new float[][] {s[0], s[1], {HolsterView.MOVE, -centre[0] / 16.0F, -centre[1] / 16.0F, -centre[2] / 16.0F},
            {HolsterView.TURN_Z, (float) Math.toDegrees(-sway)}, {HolsterView.MOVE, -pivot[0] / 16.0F, -pivot[1] / 16.0F, -pivot[2] / 16.0F}};
    }

    // ------------------------------------------------------------------------------------------------------------ keys and easing

    /** The frame at {@code k} among keys at {@code times} (frames), each span eased its own way. */
    static Frame keyed(float k, float[] times, Frame[] frames, String[] easings) {
        if (k <= times[0]) {
            return frames[0];
        }
        for (int i = 0; i + 1 < times.length; i++) {
            if (k <= times[i + 1]) {
                double u = (k - times[i]) / Math.max(1.0E-6, times[i + 1] - times[i]);
                return between(frames[i], frames[i + 1], ease(easings[i], u));
            }
        }
        return frames[frames.length - 1];
    }

    /**
     * The frame {@code s} (0..1) of the way along a path through {@code frames}, each stretch as long as the blade turns over it
     * (plus how far its grip moves): an even pace through every key, never a stop.
     */
    static Frame along(double s, Frame[] frames) {
        double[] lengths = new double[frames.length - 1];
        double total = 0.0;
        for (int i = 0; i + 1 < frames.length; i++) {
            double dot = Math.abs(frames[i].q()[0] * frames[i + 1].q()[0] + frames[i].q()[1] * frames[i + 1].q()[1]
                + frames[i].q()[2] * frames[i + 1].q()[2] + frames[i].q()[3] * frames[i + 1].q()[3]);
            double turn = 2.0 * Math.acos(Math.min(1.0, dot));
            lengths[i] = turn + 2.0 * HolsterView.len(HolsterView.sub(frames[i + 1].p(), frames[i].p()));
            total += lengths[i];
        }
        double at = Math.max(0.0, Math.min(1.0, s)) * total;
        for (int i = 0; i < lengths.length; i++) {
            if (at <= lengths[i] || i == lengths.length - 1) {
                return between(frames[i], frames[i + 1], lengths[i] <= 0 ? 1.0 : Math.min(1.0, at / lengths[i]));
            }
            at -= lengths[i];
        }
        return frames[frames.length - 1];
    }

    /**
     * 2.6.1: the frame {@code s} (0..1) along a smooth curve from {@code a} to {@code c} drawn toward {@code b} (a quadratic Bezier, its
     * turns by slerp, de Casteljau): no corner at a middle key, so the blade's turn speeds and slows without a hitch.
     */
    static Frame curve(double s, Frame a, Frame b, Frame c) {
        double u = Math.max(0.0, Math.min(1.0, s));
        return between(between(a, b, u), between(b, c, u), u);
    }

    static Frame between(Frame a, Frame b, double u) {
        double[] p = {a.p()[0] + (b.p()[0] - a.p()[0]) * u, a.p()[1] + (b.p()[1] - a.p()[1]) * u, a.p()[2] + (b.p()[2] - a.p()[2]) * u};
        return new Frame(p, slerp(a.q(), b.q(), u));
    }

    /** 0..1 eased (outback overshoots past 1 a little and comes back). */
    static double ease(String kind, double u) {
        double t = Math.max(0.0, Math.min(1.0, u));
        return switch (kind) {
            case "insine" -> 1 - Math.cos(t * Math.PI / 2);
            case "outsine" -> Math.sin(t * Math.PI / 2);
            case "inoutsine" -> -(Math.cos(Math.PI * t) - 1) / 2;
            case "outcubic" -> 1 - Math.pow(1 - t, 3);
            case "outquart" -> 1 - Math.pow(1 - t, 4);
            case "outquad" -> 1 - (1 - t) * (1 - t);
            case "outexpo" -> t >= 1 ? 1 : 1 - Math.pow(2, -10 * t);
            case "outback" -> {
                double c1 = 1.70158;
                double c3 = c1 + 1;
                yield 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
            }
            default -> t;
        };
    }

    // ------------------------------------------------------------------------------------------------------------ quaternions

    static double[] point(Frame f, double x, double yPx, double z) {
        return HolsterView.add(f.p(), rotate(f.q(), new double[] {x, yPx / 16.0, z}));
    }

    /** {@code b} expressed in {@code a}'s frame. */
    static Frame relative(Frame a, Frame b) {
        double[] inv = conj(a.q());
        return new Frame(rotate(inv, HolsterView.sub(b.p(), a.p())), mul(inv, b.q()));
    }

    static Frame compose(Frame a, Frame b) {
        return new Frame(HolsterView.add(a.p(), rotate(a.q(), b.p())), mul(a.q(), b.q()));
    }

    static double[] quat(double[][] m) {
        double tr = m[0][0] + m[1][1] + m[2][2];
        double x;
        double y;
        double z;
        double w;
        if (tr > 0) {
            double s = Math.sqrt(tr + 1.0) * 2;
            w = 0.25 * s;
            x = (m[2][1] - m[1][2]) / s;
            y = (m[0][2] - m[2][0]) / s;
            z = (m[1][0] - m[0][1]) / s;
        } else if (m[0][0] > m[1][1] && m[0][0] > m[2][2]) {
            double s = Math.sqrt(1.0 + m[0][0] - m[1][1] - m[2][2]) * 2;
            w = (m[2][1] - m[1][2]) / s;
            x = 0.25 * s;
            y = (m[0][1] + m[1][0]) / s;
            z = (m[0][2] + m[2][0]) / s;
        } else if (m[1][1] > m[2][2]) {
            double s = Math.sqrt(1.0 + m[1][1] - m[0][0] - m[2][2]) * 2;
            w = (m[0][2] - m[2][0]) / s;
            x = (m[0][1] + m[1][0]) / s;
            y = 0.25 * s;
            z = (m[1][2] + m[2][1]) / s;
        } else {
            double s = Math.sqrt(1.0 + m[2][2] - m[0][0] - m[1][1]) * 2;
            w = (m[1][0] - m[0][1]) / s;
            x = (m[0][2] + m[2][0]) / s;
            y = (m[1][2] + m[2][1]) / s;
            z = 0.25 * s;
        }
        double n = Math.sqrt(x * x + y * y + z * z + w * w);
        return new double[] {x / n, y / n, z / n, w / n};
    }

    static double[] axisAngle(double[] axis, double angle) {
        double s = Math.sin(angle / 2);
        return new double[] {axis[0] * s, axis[1] * s, axis[2] * s, Math.cos(angle / 2)};
    }

    /** {angle (radians), x, y, z} of a unit quaternion. */
    static double[] axisAngleOf(double[] q) {
        double w = Math.max(-1.0, Math.min(1.0, q[3]));
        double angle = 2 * Math.acos(w);
        double s = Math.sqrt(Math.max(0.0, 1 - w * w));
        if (s < 1.0E-8) {
            return new double[] {0.0, 0.0, 1.0, 0.0};
        }
        return new double[] {angle, q[0] / s, q[1] / s, q[2] / s};
    }

    static double[] mul(double[] a, double[] b) {
        return new double[] {
            a[3] * b[0] + a[0] * b[3] + a[1] * b[2] - a[2] * b[1],
            a[3] * b[1] - a[0] * b[2] + a[1] * b[3] + a[2] * b[0],
            a[3] * b[2] + a[0] * b[1] - a[1] * b[0] + a[2] * b[3],
            a[3] * b[3] - a[0] * b[0] - a[1] * b[1] - a[2] * b[2]};
    }

    static double[] conj(double[] q) {
        return new double[] {-q[0], -q[1], -q[2], q[3]};
    }

    static double[] rotate(double[] q, double[] v) {
        double[] p = {v[0], v[1], v[2], 0};
        double[] r = mul(mul(q, p), conj(q));
        return new double[] {r[0], r[1], r[2]};
    }

    static double[] slerp(double[] a, double[] b, double u) {
        double d = a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3];
        double[] bb = b;
        if (d < 0) {
            bb = new double[] {-b[0], -b[1], -b[2], -b[3]};
            d = -d;
        }
        if (d > 0.9995) {
            double[] r = new double[4];
            for (int i = 0; i < 4; i++) {
                r[i] = a[i] + (bb[i] - a[i]) * u;
            }
            double n = Math.sqrt(r[0] * r[0] + r[1] * r[1] + r[2] * r[2] + r[3] * r[3]);
            return new double[] {r[0] / n, r[1] / n, r[2] / n, r[3] / n};
        }
        double th = Math.acos(d);
        double s = Math.sin(th);
        double wa = Math.sin((1 - u) * th) / s;
        double wb = Math.sin(u * th) / s;
        return new double[] {a[0] * wa + bb[0] * wb, a[1] * wa + bb[1] * wb, a[2] * wa + bb[2] * wb, a[3] * wa + bb[3] * wb};
    }

    /** Where the right fist's centre grips a katana's handle (model px along it from the guard): KatanaRenderer.GRIP. */
    static final class KatanaGrip {
        static final double GRIP = -2.4;

        private KatanaGrip() {
        }
    }
}
