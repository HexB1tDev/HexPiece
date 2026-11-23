package com.hexhaki.piece.gear;

/**
 * 3.2.8: where the Gum-Gum Giant's right fist is, so a body the giant grabs sits in it (the owner: "Giant is not holding the creatures
 * right. Sometimes they get off the player's hand. It's overall really junky").
 *
 * <p>The original's GiantGrab put the body half a block forward and 0.31 to the right of the eye, everything times four, on the server
 * only: every client then drew it where the last position update said, three ticks behind and smoothed, so it trailed the hand when
 * the giant walked or turned and sank between updates. Now the server (server/GiantHold.hand, patched into GiantGrab.tick) and every
 * client (client/gear/NikaHoldClient, each tick) put it in the same place: the middle of the fist of the right arm as the player
 * model draws it held straight out (gear5_giant_grab from tick 8: pitch -90, no bend), the model scaled by the giant's own sizes.
 *
 * <p>The player model: the right arm's pivot 5 pixels right of the middle and 2 below the neck, its box 4 wide with its middle a pixel
 * further out, 12 long; the fist's middle 8 pixels down it. PlayerRenderer scales by 0.9375 and lifts by 1.501 (24 pixels): the
 * shoulder 0.9375 * (1.501 - 2 / 16) above the feet. The giant (GearBodyDeformer) scales all of it round the feet: across by
 * {@code wide}, up by {@code tall}.
 */
public final class NikaHold {
    /** The clip's arm straight out (gear5_giant_grab tick 8): a body grabbed flies into the fist from its own place until then. */
    public static final int REACH = 8;
    /** The fly-in starts as the arm leaves the side. */
    public static final int REACH_FROM = 2;
    /** The server tells the clients again every this many ticks (anyone come near since), and they let go of a hold not heard for 3 times as long. */
    public static final int RESEND = 10;

    static final double SHOULDER_UP = 0.9375 * (1.501 - 2 / 16.0);
    static final double FIST_RIGHT = 0.9375 * 6 / 16.0;
    static final double FIST_FORWARD = 0.9375 * 8 / 16.0;

    private NikaHold() {
    }

    /**
     * The middle of the fist into {@code out} (x, y, z): a body standing at x, y, z turned {@code yaw} degrees (Minecraft's: 0 faces +z),
     * drawn {@code wide} times across and {@code tall} times up.
     */
    public static void fist(double x, double y, double z, double yaw, double wide, double tall, double[] out) {
        double r = Math.toRadians(yaw);
        double sin = Math.sin(r);
        double cos = Math.cos(r);
        // forward (-sin, cos), the body's right (-cos, -sin)
        out[0] = x + wide * (-sin * FIST_FORWARD - cos * FIST_RIGHT);
        out[1] = y + tall * SHOULDER_UP;
        out[2] = z + wide * (cos * FIST_FORWARD - sin * FIST_RIGHT);
    }

    /** How far a grabbed body has flown into the fist {@code age} ticks after the grab (0 where it stood, 1 in the fist): fast, then easing in. */
    public static double reach(double age) {
        double t = Math.max(0, Math.min(1, (age - REACH_FROM) / (REACH - REACH_FROM)));
        double u = 1 - t;
        return 1 - u * u * u;
    }
}
