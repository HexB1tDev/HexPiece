package com.hexhaki.piece.sword;

/**
 * 3.5.9 (the owner: "animation of the Wifi haki becomes: The user (while holding the key) slowly unsheaths the sword JUST A LITTLE BIT,
 * the lightning emergers from the opening in the sheath", with a picture of the guard pushed out of the saya's mouth and red-black
 * lightning bursting from the gap): WiFi Haki held, as time goes (pure, tested in {@code SwordTests}). The right hand settles on the
 * hilt ({@link #GRIP}), the left thumb breaks the seal, the guard a thumb's width out ({@link #PUSH}, {@link #THUMB} model px, as
 * {@link Koiguchi}), then the blade eases slowly on to {@link #MOST} px ({@link #OUT}) and is held there trembling (a loop of
 * {@link #LOOP} ticks) while the key is; let go, it slides home in {@link #HOME} ticks. The clip (tools/wifi_anims.py, wifi_haki.json)
 * is the sword draw's own solved poses (sword_draw: the fist on the hilt, the saya brought forward by the left hand) at the draw's
 * tick where its blade is as far out ({@link #drawTick}); client/sword/WifiPull slides the blade by {@link #push}.
 */
public final class WifiDraw {
    public static final int GRIP = 9;
    public static final int PUSH = 14;
    public static final int OUT = 40;
    public static final int LOOP = 8;
    public static final int HOME = 5;
    public static final float THUMB = 1.2F;
    public static final float MOST = 2.9F;
    public static final float TREMBLE = 0.08F;
    /** The sword draw's beats (tools/sword_anims.py, katana): the hand on the hilt, the thumb's push, its hold, the blade half out. */
    static final double DRAW_GRIP = 5.0;
    static final double DRAW_PUSH = 9.0;
    static final double DRAW_HOLD = 11.0;
    static final double DRAW_HALF = 17.0;
    static final double DRAW_HALF_OUT = 8.0;

    private WifiDraw() {
    }

    static double inOutSine(double u) {
        return -(Math.cos(Math.PI * u) - 1.0) / 2.0;
    }

    static double outSine(double u) {
        return Math.sin(u * Math.PI / 2.0);
    }

    /** How far (model px) the blade is out of its saya {@code t} ticks into WiFi Haki held. */
    public static float push(double t) {
        if (t < GRIP) {
            return 0.0F;
        }
        if (t < PUSH) {
            return (float) (THUMB * outSine((t - GRIP) / (PUSH - GRIP)));
        }
        if (t < OUT) {
            return (float) (THUMB + (MOST - THUMB) * inOutSine((t - PUSH) / (OUT - PUSH)));
        }
        double c = ((t - OUT) % LOOP) / LOOP;
        return (float) (MOST - TREMBLE * Math.sin(Math.PI * c));
    }

    /** Let go {@code since} ticks ago with the blade {@code from} px out: on its way home. */
    public static float home(float from, double since) {
        if (since >= HOME) {
            return 0.0F;
        }
        double u = Math.max(0.0, since) / HOME;
        return (float) (from * (1.0 - u * u));
    }

    /** The sword draw's tick (sword_draw.json) whose pose WiFi Haki's clip holds {@code t} ticks in: its blade as far out. */
    public static double drawTick(double t) {
        if (t < GRIP) {
            return DRAW_GRIP * Math.max(0.0, t) / GRIP;
        }
        if (t < PUSH) {
            return DRAW_GRIP + (DRAW_PUSH - DRAW_GRIP) * (t - GRIP) / (PUSH - GRIP);
        }
        // the draw holds the thumb's push from DRAW_PUSH to DRAW_HOLD, then slides on (in and out sine) to DRAW_HALF_OUT at DRAW_HALF
        double y = (push(t) - THUMB) / (DRAW_HALF_OUT - THUMB);
        double x = Math.acos(Math.max(-1.0, Math.min(1.0, 1.0 - 2.0 * Math.max(0.0, y)))) / Math.PI;
        return DRAW_HOLD + (DRAW_HALF - DRAW_HOLD) * x;
    }

    /** How far the sword draw's blade is out at its tick {@code d} (its noto beats, as BladePaths.notoOut has them). */
    public static double drawOut(double d) {
        if (d < DRAW_GRIP) {
            return 0.0;
        }
        if (d < DRAW_PUSH) {
            return THUMB * outSine((d - DRAW_GRIP) / (DRAW_PUSH - DRAW_GRIP));
        }
        if (d < DRAW_HOLD) {
            return THUMB;
        }
        return THUMB + (DRAW_HALF_OUT - THUMB) * inOutSine(Math.min(1.0, (d - DRAW_HOLD) / (DRAW_HALF - DRAW_HOLD)));
    }
}
