package com.hexhaki.piece.style;

/**
 * 3.1.4 (the owner: "We need to give them better first-person simple animations. Like, just make them simple for every move of the two
 * fruits we made. Quick. Better light. And better quake"; asked which fruits: "All three"): every move of the Light, Gura and Magma
 * fruits moves the game's own first-person right hand, simply and quickly: a jab out toward the crosshair, a fist pulled back trembling
 * while it gathers, a big punch out of it, the arm thrown out to the side, raised overhead, brought down, swept across. Pure: for a clip's
 * name and its age, how far the hand is moved (blocks: x right, y up, z toward the eye, the hand's view space) and turned (degrees, about
 * the hand: pitch, yaw, roll); client/FirstPersonHand applies it to the hand the game draws (never a hand of its own: the owner's device
 * did not show one in 3.0.5), and the fist's effects (the bubble, the swelling magma, the laser's fingertip) follow it ({@link #apply}).
 */
public final class HandMoves {
    private HandMoves() {
    }

    /** Where the hand turns about (its middle, in the view: right, down, ahead of the eye). */
    public static final double[] PIVOT = {0.45, -0.55, -0.7};

    // each track: rows of {tick, dx, dy, dz, pitch, yaw, roll}; eased between rows (smoothstep); held at its last row
    // pitch + tips the forearm up (toward the sky), - down; yaw + swings the fist left (toward the crosshair), - right; kept in view
    static final double[][] ZERO = {{0, 0, 0, 0, 0, 0, 0}};
    static final double[][] JAB = {{0, 0, 0, 0, 0, 0, 0}, {1, 0.05, -0.05, 0.08, -8, 0, 0}, {2, -0.16, 0.08, -0.28, 6, 10, 0},
        {4, -0.15, 0.07, -0.26, 5, 8, 0}, {8, 0, 0, 0, 0, 0, 0}};
    static final double[][] DRAWN = {{0, 0, 0, 0, 0, 0, 0}, {5, 0.1, 0.02, 0.08, -6, -12, -10}};
    static final double[][] DEEP = {{0, 0, 0, 0, 0, 0, 0}, {6, 0.14, 0.04, 0.1, -6, -16, -14}};
    static final double[][] BIG = {{0, 0.1, 0.02, 0.08, -6, -12, -10}, {2, -0.2, 0.1, -0.42, 8, 14, 0}, {5, -0.18, 0.09, -0.4, 7, 12, 0},
        {12, 0, 0, 0, 0, 0, 0}};
    static final double[][] SIDE = {{0, 0, 0, 0, 0, 0, 0}, {2, -0.08, 0.03, -0.08, 0, 10, 0}, {4, 0.26, 0.06, -0.04, 0, -38, -24}};
    static final double[][] SIDE_QUICK = {{0, 0, 0, 0, 0, 0, 0}, {2, -0.08, 0.03, -0.08, 0, 10, 0}, {4, 0.28, 0.02, -0.04, -4, -40, -26},
        {8, 0.26, 0.02, -0.04, -4, -38, -24}, {16, 0, 0, 0, 0, 0, 0}};
    static final double[][] TILT = {{0, 0.26, 0.06, -0.04, 0, -38, -24}, {2, 0.04, -0.05, -0.12, -21, 0, 0}, {6, 0.04, -0.04, -0.1, -20, 0, 0},
        {14, 0, 0, 0, 0, 0, 0}};
    static final double[][] UP = {{0, 0, 0, 0, 0, 0, 0}, {2, 0.04, -0.06, 0.06, -10, 0, 0}, {4, 0.04, 0.3, -0.02, 45, 0, 0}};
    /**
     * 3.1.5: Ryusei Kazan held: the fist punched up at tick 3 and every 6 ticks after (15 in all, MaguMoves.THROW_EVERY), drawn back down
     * between the punches, held up after.
     */
    static final double[][] PUMP = pump();

    static double[][] pump() {
        double[][] up = {{0.04, 0.3, -0.02, 45, 0, 0}};
        double[][] back = {{0.06, 0.14, 0.02, 22, -4, -6}};
        java.util.List<double[]> rows = new java.util.ArrayList<>();
        rows.add(new double[] {0, 0, 0, 0, 0, 0, 0});
        rows.add(new double[] {2, 0.04, -0.06, 0.06, -10, 0, 0});
        for (int k = 0; k < 15; k++) {
            int t = 3 + 6 * k;
            if (k > 0) {
                rows.add(row(t - 3, back[0]));
            }
            rows.add(row(t, up[0]));
        }
        return rows.toArray(new double[0][]);
    }

    static double[] row(double tick, double[] m) {
        return new double[] {tick, m[0], m[1], m[2], m[3], m[4], m[5]};
    }
    static final double[][] UP_END = {{0, 0.04, 0.3, -0.02, 45, 0, 0}, {10, 0, 0, 0, 0, 0, 0}};
    /**
     * 3.6.3: Lotus Mochi Combo on its beats (mochi/MochiDraw.LOTUS_*): the hand flings the dough out to the left (4), held out at the
     * donut while it forms, drawn back with its flash (31), thrust with its punch (34), swept up with the launch (39), let down.
     */
    /** 3.6.3: Mochi Muddle: the hand out to the side gathering, then thrown down and ahead into the dive (27), held low, back. */
    static final double[][] MUDDLE = {{0, 0, 0, 0, 0, 0, 0}, {4, 0.26, 0.06, -0.04, 0, -38, -24}, {22, 0.27, 0.07, -0.04, 2, -40, -26},
        {24, 0.04, 0.2, -0.06, 30, 0, 0}, {27, -0.04, -0.22, -0.26, -34, 0, 0}, {40, -0.04, -0.2, -0.24, -30, 0, 0}, {50, 0, 0, 0, 0, 0, 0}};
    /** 3.6.3: Chestnut Mochi: the hand flung straight up (4), held while the disc forms (to 12), let down. */
    static final double[][] UP_END_HELD = {{0, 0, 0, 0, 0, 0, 0}, {2, 0.04, -0.08, 0.06, -12, 0, 0}, {4, 0.04, 0.3, -0.02, 45, 0, 0},
        {12, 0.04, 0.29, -0.02, 43, 0, 0}, {22, 0, 0, 0, 0, 0, 0}};
    /** 3.6.3: Buzz Cut Mochi from the catch: back to the right (3), swung round (7), held out in the whirl, raised (60), slammed (66). */
    static final double[][] BUZZ = {{0, 0, 0, 0, 0, 0, 0}, {3, 0.26, 0.06, -0.04, 0, -38, -24}, {7, -0.16, 0.08, -0.28, 6, 10, 0},
        {52, -0.15, 0.07, -0.26, 5, 8, 0}, {60, 0.04, 0.3, -0.02, 45, 0, 0}, {63, 0.04, 0.32, 0.0, 48, 0, 0}, {66, -0.04, -0.22, -0.26, -34, 0, 0},
        {76, -0.04, -0.2, -0.24, -30, 0, 0}, {86, 0, 0, 0, 0, 0, 0}};
    static final double[][] LOTUS = {{0, 0, 0, 0, 0, 0, 0}, {2, 0.05, -0.04, 0.06, -6, -10, 0}, {4, -0.2, 0.12, -0.12, 10, 28, 10},
        {8, -0.16, 0.06, -0.1, 4, 22, 6}, {28, -0.15, 0.06, -0.1, 4, 20, 6}, {31, 0.1, 0.02, 0.08, -6, -8, -6}, {34, -0.16, 0.08, -0.3, 6, 10, 0},
        {39, 0.04, 0.3, -0.02, 45, 0, 0}, {46, 0, 0, 0, 0, 0, 0}};
    static final double[][] SLAM = {{0, 0.04, 0.3, -0.02, 45, 0, 0}, {2, 0.04, 0.34, 0.0, 52, 0, 0}, {5, -0.04, -0.22, -0.26, -34, 0, 0},
        {8, -0.04, -0.2, -0.24, -30, 0, 0}, {14, 0, 0, 0, 0, 0, 0}};
    static final double[][] DOWN = {{0, 0, 0, 0, 0, 0, 0}, {2, 0.04, 0.28, -0.02, 45, 0, 0}, {4, -0.04, -0.22, -0.26, -34, 0, 0},
        {8, -0.04, -0.2, -0.24, -30, 0, 0}, {16, 0, 0, 0, 0, 0, 0}};
    /** 3.5.5: Kaku Mochi's and the club's: the fist raised overhead from the hand's place, slammed down on 7 to 8, then let down. */
    static final double[][] SLAM_UP = {{0, 0, 0, 0, 0, 0, 0}, {4, 0.04, 0.3, -0.02, 45, 0, 0}, {6, 0.04, 0.34, 0.0, 52, 0, 0},
        {8, -0.04, -0.22, -0.26, -34, 0, 0}, {12, -0.04, -0.2, -0.24, -30, 0, 0}, {18, 0, 0, 0, 0, 0, 0}};
    /** 3.5.5: Amadare Mochi's and Mochi Ginkaku's: the fist raised to the sky, then brought down pointing at the place. */
    static final double[][] UP_DOWN = {{0, 0, 0, 0, 0, 0, 0}, {3, 0.04, 0.3, -0.02, 45, 0, 0}, {7, 0.04, 0.32, -0.02, 48, 0, 0},
        {10, -0.12, 0.06, -0.24, 6, 8, 0}, {14, -0.12, 0.06, -0.24, 6, 8, 0}, {18, 0, 0, 0, 0, 0, 0}};
    static final double[][] SWEEP = {{0, 0, 0, 0, 0, 0, 0}, {1, 0.22, 0.06, 0.0, 0, -30, 0}, {3, -0.4, 0.0, -0.18, 0, 40, 12},
        {8, 0, 0, 0, 0, 0, 0}};
    static final double[][] SWEEP_BACK = {{0, 0, 0, 0, 0, 0, 0}, {1, -0.36, 0.04, -0.14, 0, 34, 10}, {3, 0.26, 0.0, -0.08, 0, -34, -6},
        {8, 0, 0, 0, 0, 0, 0}};
    static final double[][] POINT = {{0, 0, 0, 0, 0, 0, 0}, {2, -0.12, 0.06, -0.24, 6, 8, 0}};
    static final double[][] POINT_TAP = {{0, 0, 0, 0, 0, 0, 0}, {2, -0.12, 0.06, -0.24, 6, 8, 0}, {5, -0.12, 0.06, -0.24, 6, 8, 0},
        {8, 0, 0, 0, 0, 0, 0}};
    static final double[][] RECOIL = {{0, -0.12, 0.06, -0.24, 6, 8, 0}, {1, -0.1, 0.16, -0.1, 25, 8, 0}, {3, -0.12, 0.1, -0.18, 12, 8, 0},
        {10, 0, 0, 0, 0, 0, 0}};
    static final double[][] POINT_END = {{0, -0.12, 0.06, -0.24, 6, 8, 0}, {6, 0, 0, 0, 0, 0, 0}};
    /** 3.9.8: Gorosei Stare: the fist clenched and drawn in, shaking as the glare fixes (to 8), jolted out as the target flies. */
    static final double[][] GLARE = {{0, 0, 0, 0, 0, 0, 0}, {3, 0.07, -0.05, 0.1, -9, -5, -6}, {5, 0.075, -0.045, 0.1, -8, -6, -7},
        {7, 0.07, -0.05, 0.1, -9, -5, -6}, {9, -0.06, 0.03, -0.12, 4, 6, 0}, {12, -0.05, 0.02, -0.1, 3, 5, 0}, {20, 0, 0, 0, 0, 0, 0}};
    /** 3.9.8: Yokai Blast let go: the pointed hand kicked up by the beam leaving it, then held out pointing while it pours. */
    static final double[][] POINT_HELD = {{0, -0.12, 0.06, -0.24, 6, 8, 0}, {1, -0.1, 0.13, -0.14, 20, 8, 0}, {4, -0.12, 0.07, -0.22, 8, 8, 0}};
    /** 3.9.8: Thy Death: the hand raised to the sky (3), held while the clouds gather (to 26), let down. */
    static final double[][] RAISED = {{0, 0, 0, 0, 0, 0, 0}, {3, 0.04, 0.3, -0.02, 45, 0, 0}, {26, 0.04, 0.31, -0.02, 46, 0, 0},
        {36, 0, 0, 0, 0, 0, 0}};
    static final double[][] PUSH = {{0, 0, 0, 0, 0, 0, 0}, {6, -0.1, -0.04, -0.28, -22, 8, 0}};
    static final double[][] PUSH_END = {{0, -0.1, -0.04, -0.28, -22, 8, 0}, {3, 0.2, 0.16, -0.1, 20, -20, -20}, {12, 0, 0, 0, 0, 0, 0}};
    static final double[][] THRUST = {{0, 0, 0, 0, 0, 0, 0}, {3, 0.05, -0.04, 0.06, 0, 0, 0}, {5, -0.15, 0.05, -0.4, 8, 10, 0}};
    static final double[][] GATHER_LEG = {{0, 0, 0, 0, 0, 0, 0}, {6, 0.1, 0.01, 0.04, -7, 0, 0}};
    static final double[][] KICK = {{0, 0, 0, 0, 0, 0, 0}, {1, 0.05, 0.01, 0.05, -6, 0, 0}, {6, 0.04, 0.0, 0.04, -5, 0, 0},
        {14, 0, 0, 0, 0, 0, 0}};
    static final double[][] THROW = {{0, 0, 0, 0, 0, 0, 0}, {2, 0.05, -0.06, 0.1, -8, 0, 0}, {4, -0.14, 0.1, -0.4, 10, 10, 0},
        {8, -0.05, 0.1, -0.36, 10, -20, 0}, {13, 0, 0, 0, 0, 0, 0}};
    static final double[][] COCKED = {{0, 0, 0, 0, 0, 0, 0}, {3, 0.12, 0.03, 0.08, -7, -12, -10}};
    /** 3.1.6, the Gura ultimate: the fist pulled up and back to the side of the view, held; let go, driven out and down; given up. */
    static final double[][] ULT_COCK = {{0, 0, 0, 0, 0, 0, 0}, {8, 0.04, 0.19, 0.08, 28, -6, -12}};
    static final double[][] ULT_RELEASE = {{0, 0.04, 0.19, 0.08, 28, -6, -12}, {2, -0.18, 0.04, -0.44, 2, 14, 0},
        {6, -0.16, -0.02, -0.42, -4, 12, 0}, {14, -0.12, -0.02, -0.34, -4, 10, 0}, {24, 0, 0, 0, 0, 0, 0}};
    static final double[][] ULT_DROP = {{0, 0.04, 0.19, 0.08, 28, -6, -12}, {8, 0, 0, 0, 0, 0, 0}};
    static final double[][] MELT = {{0, 0, 0, 0, 0, 0, 0}, {4, 0.04, -0.01, 0.01, -8, 0, 0}};
    static final double[][] RISE = {{0, 0.04, -0.01, 0.01, -8, 0, 0}, {3, 0.1, 0.22, -0.08, 36, 0, -16}, {14, 0, 0, 0, 0, 0, 0}};

    /** A clip's track and how hard the hand trembles once it has got there (0 not at all). */
    record Move(double[][] track, double tremble) {
    }

    static Move move(String clip) {
        if (clip == null) {
            return null;
        }
        return switch (clip) {
            // the Light fruit
            case "pika_laser" -> new Move(POINT_TAP, 0);
            case "pika_laser_charge" -> new Move(POINT, 0.4);
            case "pika_laser_fire" -> new Move(RECOIL, 0);
            case "pika_laser_end" -> new Move(POINT_END, 0);
            case "pika_yasakani" -> new Move(PUSH, 1.0);
            case "pika_yasakani_end" -> new Move(PUSH_END, 0);
            case "pika_lunge" -> new Move(THRUST, 0);
            case "pika_sword_1" -> new Move(SWEEP, 0);
            case "pika_sword_2" -> new Move(SWEEP_BACK, 0);
            case "pika_light_kick_charge" -> new Move(GATHER_LEG, 0.5);
            case "pika_light_kick", "pika_z_kick", "pika_z_slam" -> new Move(KICK, 0);
            case "pika_clones", "pika_murakumo", "pika_yata" -> new Move(THROW, 0);
            // the Gura fruit
            case "gura_punch", "magu_punch" -> new Move(JAB, 0);
            case "gura_charge", "magu_charge" -> new Move(DRAWN, 1.0);
            case "gura_fist_charge" -> new Move(DEEP, 1.2);
            case "gura_release", "gura_gekishin", "magu_release" -> new Move(BIG, 0);
            case "gura_shake" -> new Move(SIDE, 1.4);
            case "gura_tilt" -> new Move(TILT, 0);
            case "gura_leap" -> new Move(UP, 0.3);
            case "magu_sky" -> new Move(PUMP, 0.3);
            case "gura_slam" -> new Move(SLAM, 0);
            case "gura_tsunami" -> new Move(SIDE_QUICK, 0);
            case "gura_split", "magu_erupt" -> new Move(DOWN, 0);
            case "gura_ult_charge" -> new Move(ULT_COCK, 1.6);
            case "gura_ult_release" -> new Move(ULT_RELEASE, 0);
            case "gura_ult_drop" -> new Move(ULT_DROP, 0);
            // the Magma fruit
            case "magu_meigo_wind" -> new Move(COCKED, 0.3);
            case "magu_meigo" -> new Move(BIG, 0);
            case "magu_sky_end" -> new Move(UP_END, 0);
            case "magu_hound" -> new Move(THROW, 0);
            case "magu_melt" -> new Move(MELT, 0);
            case "magu_rise" -> new Move(RISE, 0);
            // 3.1.9: the Ice fruit
            case "hie_partisan_raise" -> new Move(SIDE, 0.3);
            case "hie_partisan_throw" -> new Move(THROW, 0);
            case "hie_partisan_launch", "hie_ball_throw" -> new Move(PUSH_END, 0);
            case "hie_grab_reach" -> new Move(POINT, 0.2);
            case "hie_grab", "hie_road_punch" -> new Move(BIG, 0);
            case "hie_ball_gather" -> new Move(DRAWN, 0.6);
            case "hie_pheasant", "hie_saber_cut_1" -> new Move(SWEEP, 0);
            case "hie_saber_cut_2" -> new Move(SWEEP_BACK, 0);
            case "hie_ice_age" -> new Move(DOWN, 0);
            case "hie_road_slide" -> new Move(COCKED, 0.3);
            case "hie_saber_draw" -> new Move(SIDE_QUICK, 0);
            // 3.4.2: the String fruit's own clips (tools/ito_anims.py)
            case "ito_slash_1" -> new Move(SWEEP, 0);
            case "ito_slash_2" -> new Move(SWEEP_BACK, 0);
            case "ito_goshiki" -> new Move(DOWN, 0);
            case "ito_heat_charge" -> new Move(POINT, 0.4);
            case "ito_heat_fire" -> new Move(RECOIL, 0);
            case "ito_cage_charge" -> new Move(UP, 0.3);
            case "ito_cage_cast" -> new Move(UP_END, 0);
            case "ito_cage_close" -> new Move(DOWN, 0);
            case "ito_knight" -> new Move(POINT_TAP, 0);
            // 3.5.5: the Mochi fruit's own clips (tools/mochi_anims.py)
            case "mochi_punch_1", "mochi_punch_2", "mochi_yaki_fire", "mochi_lance_fire" -> new Move(JAB, 0);
            case "mochi_lotus" -> new Move(LOTUS, 0);
            case "mochi_muddle" -> new Move(MUDDLE, 0);
            case "mochi_chestnut" -> new Move(UP_END_HELD, 0);
            case "mochi_buzz" -> new Move(BUZZ, 0);
            case "mochi_yaki_charge", "mochi_lance_charge" -> new Move(DEEP, 1.2);
            case "mochi_awaken" -> new Move(DOWN, 0);
            case "mochi_donuts" -> new Move(POINT, 0.4);
            // 3.6.4: the awakened left click, Mogura's five swings (client/mochi/MochiClient holds Mogura on this hand)
            case "mochi_trident_1" -> new Move(THRUST, 0);
            case "mochi_trident_2", "mochi_trident_3" -> new Move(SWEEP, 0);
            case "mochi_trident_4" -> new Move(SWEEP_BACK, 0);
            case "mochi_trident_5" -> new Move(UP, 0);
            // 3.6.6: Power Mochi: the arms flung out as the donuts burst out; each punch a point of the arm on its side
            case "mochi_power" -> new Move(UP_DOWN, 0);
            case "mochi_lasso" -> new Move(THROW, 0);
            case "mochi_oblivion" -> new Move(DOWN, 0);
            case "mochi_storm" -> new Move(PUSH, 0.6);
            case "mochi_power_1", "mochi_power_2" -> new Move(POINT, 0);
            // 3.7.2: the Phoenix's: Fujiazami's dash (the hand swept back with the arms, flung out with the throw), Talon Slam's leap
            // and its slam down (tools/phoenix_anims.py)
            case "phoenix_fuji" -> new Move(PUSH, 0.8);
            case "phoenix_talon" -> new Move(UP_DOWN, 0);
            // 3.7.3: Phoenix Inferno (the hands braced out for the kicks), Bluebird (the hands cupped round the ball, then thrust out)
            case "phoenix_inferno" -> new Move(DOWN, 0);
            case "phoenix_bluebird" -> new Move(DEEP, 1.2);
            case "phoenix_bluebird_throw" -> new Move(PUSH, 0.4);
            // 3.9.8: the Itsumade's moves as a human (tools/itsu_anims.py): the glare (the fist clenched and drawn in, trembling, then
            // jolted out as the target is thrown), the presence (the arms thrown open), Yokai Blast (pointed, trembling as it gathers;
            // held out against the beam's push; let down), Thy Death (raised to the sky, held, let down)
            case "itsu_stare" -> new Move(GLARE, 0);
            case "itsu_presence" -> new Move(SIDE_QUICK, 0);
            case "itsu_blast_charge" -> new Move(POINT, 0.5);
            case "itsu_blast_fire" -> new Move(POINT_HELD, 0.35);
            case "itsu_blast_end" -> new Move(POINT_END, 0);
            case "itsu_death" -> new Move(RAISED, 0);
            default -> null;
        };
    }

    /** Whether {@code clip} moves the first-person hand. */
    public static boolean moves(String clip) {
        return move(clip) != null;
    }

    static double smooth(double t) {
        double u = t < 0.0 ? 0.0 : t > 1.0 ? 1.0 : t;
        return u * u * (3.0 - 2.0 * u);
    }

    /**
     * The hand's move {@code ticks} into {@code clip} into {@code out} (6: dx, dy, dz, pitch, yaw, roll); all zero for a clip that does
     * not move it. A held pose trembles ({@code ticks} drives it).
     */
    public static void at(String clip, double ticks, double[] out) {
        java.util.Arrays.fill(out, 0.0);
        Move m = move(clip);
        if (m == null) {
            return;
        }
        double[][] tr = m.track;
        int k = 0;
        while (k + 1 < tr.length && ticks >= tr[k + 1][0]) {
            k++;
        }
        double[] a = tr[k];
        double[] b = k + 1 < tr.length ? tr[k + 1] : tr[k];
        double t = b == a ? 1.0 : smooth((ticks - a[0]) / Math.max(1.0E-6, b[0] - a[0]));
        if (ticks < tr[0][0]) {
            t = 0.0;
        }
        for (int i = 0; i < 6; i++) {
            out[i] = a[i + 1] + (b[i + 1] - a[i + 1]) * t;
        }
        double last = tr[tr.length - 1][0];
        if (m.tremble > 0.0 && ticks > last) {
            double w = m.tremble;
            out[0] += 0.006 * w * Math.sin(ticks * 2.9);
            out[1] += 0.006 * w * Math.sin(ticks * 3.7 + 1.0);
            out[3] += 1.6 * w * Math.sin(ticks * 3.3 + 2.0);
            out[5] += 1.2 * w * Math.sin(ticks * 2.6);
        }
    }

    /** {@code from} blended to {@code to} by {@code t} (0 to 1, eased) into {@code out}. */
    public static void blend(double[] from, double[] to, double t, double[] out) {
        double s = smooth(t);
        for (int i = 0; i < 6; i++) {
            out[i] = from[i] + (to[i] - from[i]) * s;
        }
    }

    /**
     * A point of the hand's view space {@code v} (x right, y up, z toward the eye) moved as the hand is by {@code m}: turned about
     * {@link #PIVOT} (yaw, then pitch, then roll, as PoseStack's turns stack) and shifted, into {@code out}.
     */
    public static void apply(double[] m, double[] v, double[] out) {
        double x = v[0] - PIVOT[0];
        double y = v[1] - PIVOT[1];
        double z = v[2] - PIVOT[2];
        double rz = Math.toRadians(m[5]);
        double nx = x * Math.cos(rz) - y * Math.sin(rz);
        double ny = x * Math.sin(rz) + y * Math.cos(rz);
        x = nx;
        y = ny;
        double rx = Math.toRadians(m[3]);
        ny = y * Math.cos(rx) - z * Math.sin(rx);
        double nz = y * Math.sin(rx) + z * Math.cos(rx);
        y = ny;
        z = nz;
        double ry = Math.toRadians(m[4]);
        nx = x * Math.cos(ry) + z * Math.sin(ry);
        nz = -x * Math.sin(ry) + z * Math.cos(ry);
        out[0] = nx + PIVOT[0] + m[0];
        out[1] = y + PIVOT[1] + m[1];
        out[2] = nz + PIVOT[2] + m[2];
    }
}
