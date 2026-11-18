package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.ConquerorStorm;
import com.lowdragmc.photon.client.fx.FX;
import java.util.ArrayList;
import java.util.List;

/**
 * 2.1.7: the Conqueror's full-power release, made ridiculously bigger (the owner: "the haki full power explosion should be ridiculously
 * bigger than what it currently is, lightning spreading further etc", then "too much lightning way too much": a few huge bolts reaching
 * far, not more of them).
 *
 * <p>The patch calls {@link #release} as the original's {@code HakiVfx.conquerorRelease(charge, variant, seed)} hands back its
 * composition: at full charge the {@link ConquerorStorm} is painted into the same composition ({@link HakiBolts#pending}), so it is
 * let go with it, from the same point, when the original plays it; {@link HakiBolts} lays it on the terrain, sweeps its shockwave out
 * and lands its strikes. The original's own release (its range lightning, burst and dome, its sounds) is untouched.
 */
public final class HakiStorm {
    static final long SALT = 0x5354_4F52L;

    private HakiStorm() {
    }

    /**
     * 2.4.5 (the owner: "Conqueror Haki full release lags a ton when releasing the full charge"): {@code conquerorRelease}'s range
     * lightning (the patch sends both its calls here). The first pass is drawn as the original draws it; the supreme release's second
     * (another seed, {@code baseDelay} 7) is left out: it doubled the 3D lightning near the player (up to 80,000 quads a frame for a
     * second, built on the CPU every frame), and the storm's strands already reach far past it.
     */
    public static void range(FX fx, float charge, int mastery, long seed, boolean release, int baseDelay) {
        if (baseDelay > 0) {
            return;
        }
        com.hexhaki.client.vfx.HakiVfx.addConquerorRangeLightning(fx, charge, mastery, seed, release, baseDelay);
    }

    /** {@code conquerorRelease}'s composition, its charge (0..1), its variant (mastery x 4 + mode) and its seed. */
    public static void release(FX fx, float charge, int variant, long seed) {
        try {
            if (fx == null || charge < 0.96F) {
                return;
            }
            int mastery = Math.max(0, variant / 4);
            // 2.2.5 (the owner: "anyone by default at max charge shall be able to do that"): every full-charge release is the
            // supreme (JoyBoy) one, whatever the mastery (the server's releaseConqueror reads the same since this version)
            boolean supreme = true;
            HakiBolts.Pending pending = HakiBolts.pending(fx);
            List<Bolt> storm = new ArrayList<>();
            for (ConquerorStorm.Strand strand : ConquerorStorm.release(mastery, seed, supreme)) {
                storm.addAll(ConquerorStorm.bolts(strand));
            }
            pending.bolts.addAll(storm);
            pending.ground.addAll(storm);
            pending.wave = ConquerorStorm.reach(mastery);
            pending.burst = true;
        } catch (RuntimeException failure) {
            // the release plays as the original made it
        }
    }
}
