package com.hexhaki.piece.gear;

/**
 * 3.2.6 (the owner, on the Bajrang Gun: "it should be coming first from above, and then the player slowly rotates it downwards and
 * slams it down. This is one of the strongest moves in the entire game"; "rotate it mid-air to like, actually aim down instead of just
 * standing still in the arm, just doing all the work"): the slam's timing, shared by the clip (tools/nika_anims.py writes
 * gear5_bajrang_slam from these very numbers) and the giant fist (client/gear/NikaFist puts the fist where the arm points). Pure.
 * <p>
 * From the moment the slam begins (ticks): the fist's way in the world, {@link #THETA} (degrees: 0 straight up, 90 straight ahead, 180
 * straight down), and the body's pitch {@link #PITCH} (forward positive: the whole body turning in the air). The fist is held overhead,
 * wound back as the body arches, swung over the front and down as the body turns head-first after it, and slammed straight down,
 * held through the impact and brought back as the arm pulls in. Between two keys both run the same in-out sine (the clip's
 * "inoutsine"), so the arm's own angle in the clip (θ - 180 - pitch) matches the fist at every moment.
 */
public final class NikaSlam {
    public static final double[] T = {0, 4, 10, 13, 16, 116, 142};
    public static final double[] THETA = {-8, -30, 165, 180, 180, 180, 180};
    public static final double[] PITCH = {-12, -22, 55, 72, 70, 70, 10};
    /** The clip's length: the slam (46 ticks), the fist lying in its crater (70) and pulled in (26), as the server's phases. */
    public static final int LENGTH = 142;
    /** While it charges, the body leans back this far, the fist straight up over it. */
    public static final double CHARGE_PITCH = -12;

    private NikaSlam() {
    }

    /** {@code values} at {@code t} ticks into the slam (an in-out sine between two keys, held past the last). */
    static double at(double[] values, double t) {
        if (t <= T[0]) {
            return values[0];
        }
        for (int k = 1; k < T.length; k++) {
            if (t <= T[k]) {
                double u = (t - T[k - 1]) / (T[k] - T[k - 1]);
                double e = 0.5 - 0.5 * Math.cos(Math.PI * u);
                return values[k - 1] + (values[k] - values[k - 1]) * e;
            }
        }
        return values[values.length - 1];
    }

    /** The fist's way in the world {@code t} ticks into the slam (degrees from straight up, over the front). */
    public static double theta(double t) {
        return at(THETA, t);
    }

    /** The body's pitch {@code t} ticks into the slam (degrees, forward positive); 0 once the clip has ended. */
    public static double pitch(double t) {
        return t > LENGTH + 3 ? 0.0 : at(PITCH, t);
    }

    /** The arm's own pitch in the clip at a key (PlayerAnimator's: 0 hanging, -180 straight over the head). */
    public static double armPitch(int key) {
        return THETA[key] - 180.0 - PITCH[key];
    }
}
