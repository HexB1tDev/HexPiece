package com.hexhaki.piece.gear;

/**
 * 3.2.9 (the owner, on Star Gun: "the Star Gun is okay. It could push the entities model a bit further and make it more like rubber,
 * but it's good"): the shape of a body Star Gun strikes, at {@code t} ticks after the hit. The original (RubberBendDeformer, from
 * GearServerController.starHit's S2CRubberBend: strength = 1 - knockback resistance, at least 0.4; the way it flies; held 26 ticks,
 * let go over 17) stretched it up to 2.35 times along the way it flies, leaned it 0.55 and held it so, still, then eased it back with a
 * faint wobble. Now it is rubber (client/gear/NikaBendClient draws it):
 * <ul>
 *   <li>the smack ({@link #RAMP} ticks): yanked out to over three times its length along the blow, overshooting, the top left behind
 *       (leaning back toward the striker);</li>
 *   <li>the hold: still stretched but alive, the top whipping forward past the lean and swaying, the length quivering and pulsing;</li>
 *   <li>the let go: it snaps back past its own shape into a squash (short, wide and tall), springs out again and wobbles to rest
 *       ({@link #TAIL} more ticks).</li>
 * </ul>
 * Along the blow the body is also pushed back with its stretch (the model further away than it stands). Everything an affine change of
 * the whole model round its feet, in the blow's own frame: x across, y up, z along the blow.
 */
public final class NikaBend {
    /** The smack: the original's 120 ms ramp. */
    public static final double RAMP = 2.4;
    /** The wobble's last ticks after the let go. */
    public static final double TAIL = 14;
    /** How far along the blow at full strength: 1 + this times as long. */
    static final double STRETCH = 1.9;
    /** The lean (z moved this much per block up) at full strength, settled. */
    static final double LEAN = 0.55;
    /** Pushed back along the blow by this much per unit of stretch. */
    static final double PUSH = 0.38;

    /** The parts of {@link #shape}'s result. */
    public static final int ACROSS = 0;
    public static final int UP = 1;
    public static final int ALONG = 2;
    public static final int SHEAR = 3;
    public static final int BACK = 4;

    private NikaBend() {
    }

    /** The whole of it: the smack, the hold and the let go with its wobble. */
    public static double length(double hold, double release) {
        return RAMP + hold + release + TAIL;
    }

    /**
     * The shape {@code t} ticks after the hit into {@code out}: scales across, up and along the blow, the lean (z += lean * y), the push
     * back along the blow. False once it is over (and {@code out} the rest shape).
     */
    public static boolean shape(double t, double hold, double release, double strength, double[] out) {
        double s = Math.max(0, Math.min(1.25, strength));
        double stretch;
        double lean;
        if (t < 0 || t >= length(hold, release)) {
            rest(out);
            return false;
        }
        if (t < RAMP) {
            double u = t / RAMP;
            // out-back: past the mark and back
            double back = 1.0 + 2.2 * Math.pow(u - 1, 3) + 1.2 * Math.pow(u - 1, 2);
            stretch = STRETCH * s * back;
            lean = -0.45 * LEAN * s * u;
        } else if (t < RAMP + hold) {
            double tau = t - RAMP;
            stretch = held(tau, s);
            lean = leaned(tau, s);
        } else {
            double tau = t - RAMP - hold;
            stretch = held(hold, s) * Math.exp(-tau / 4.2) * Math.cos(tau * 0.62);
            lean = leaned(hold, s) * Math.exp(-tau / 3.8) * Math.cos(tau * 0.78);
        }
        double along = Math.max(0.42, 1 + stretch);
        out[ALONG] = along;
        out[ACROSS] = Math.pow(along, -0.55);
        out[UP] = Math.pow(along, -0.24);
        out[SHEAR] = lean;
        out[BACK] = PUSH * stretch;
        return true;
    }

    /** The stretch while held: quivering round its length (fast at first, dying away) and pulsing slowly the whole time. */
    static double held(double tau, double s) {
        return STRETCH * s * (1 + 0.16 * Math.exp(-tau / 6) * Math.sin(tau * 1.9) + 0.08 * Math.sin(tau * 0.8));
    }

    /** The lean while held: from behind (the top left back by the smack) whipping forward past the settled lean, then swaying. */
    static double leaned(double tau, double s) {
        return LEAN * s * (1 - 1.45 * Math.exp(-tau / 3.4) * Math.cos(tau * 0.95)) + 0.14 * s * Math.sin(tau * 0.62);
    }

    // ------------------------------------------------------------------------------------------------------ 3.3.9: Dawn Pistol

    /** Dawn Pistol's pull: the smack, the hold, the snap back and the wobble's ticks. */
    public static final double DAWN_RAMP = 2.0;
    public static final double DAWN_HOLD = 9.0;
    public static final double DAWN_RELEASE = 10.0;
    public static final double DAWN_TAIL = 10.0;
    /** How far the top is flung along the blow per block up at full strength (the body pulled out lengthways). */
    static final double DAWN_PULL = 2.8;

    /** The whole of a Dawn Pistol's pull. */
    public static double dawnLength() {
        return DAWN_RAMP + DAWN_HOLD + DAWN_RELEASE + DAWN_TAIL;
    }

    /**
     * 3.3.9 (the owner, as Dawn Pistol was built: "make sure it bends the entities a ton more and looks better than the current one",
     * with a frame of Luffy vs Lucci: Lucci's body pulled out like taffy, several times its length, along the punch): the shape of a
     * body Dawn Pistol strikes, {@code t} ticks after the hit, into {@code out} (as {@link #shape}). Not a stretch through the body's
     * thin front-to-back (Star Gun's, which barely shows on a body seen from the side): the whole body pulled out lengthways along the
     * blow, its top flung {@link #DAWN_PULL} blocks ahead per block up while its feet trail, thinned across and a little lowered (about
     * two and a half times its height long at full strength, a steep slant: 3.3.9's first try, 4.5 and lowered 0.38, lay down flat as a
     * plank and no longer read as a body); held quivering; let go, it snaps back past upright into a
     * squash (short and wide, its top swung back toward the striker) and wobbles to rest. False once it is over.
     */
    public static boolean dawn(double t, double strength, double[] out) {
        double s = Math.max(0, Math.min(1.25, strength));
        if (t < 0 || t >= dawnLength()) {
            rest(out);
            return false;
        }
        double e;
        if (t < DAWN_RAMP) {
            double u = t / DAWN_RAMP;
            // out-back: yanked past its mark and back
            e = 1.0 + 2.4 * Math.pow(u - 1, 3) + 1.3 * Math.pow(u - 1, 2);
        } else if (t < DAWN_RAMP + DAWN_HOLD) {
            double tau = t - DAWN_RAMP;
            e = 1.0 + 0.14 * Math.exp(-tau / 4.0) * Math.sin(tau * 2.1) + 0.05 * Math.sin(tau * 0.9);
        } else {
            double tau = t - DAWN_RAMP - DAWN_HOLD;
            double held = 1.0 + 0.05 * Math.sin(DAWN_HOLD * 0.9);
            e = held * Math.exp(-tau / 3.4) * Math.cos(tau * 0.72);
        }
        e *= s;
        double pull = Math.max(0, e);
        double squash = Math.max(0, -e);
        out[SHEAR] = DAWN_PULL * e;
        out[ACROSS] = pull > 0 ? 1.0 / (1.0 + 0.7 * pull) : 1.0 + 0.9 * squash;
        out[UP] = pull > 0 ? 1.0 - 0.18 * pull : 1.0 - 0.6 * squash;
        out[ALONG] = 1.0 + 0.8 * pull + 0.6 * squash;
        out[BACK] = 0.25 * pull;
        return true;
    }

    static void rest(double[] out) {
        out[ACROSS] = 1;
        out[UP] = 1;
        out[ALONG] = 1;
        out[SHEAR] = 0;
        out[BACK] = 0;
    }
}
