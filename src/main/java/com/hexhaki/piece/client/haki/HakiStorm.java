package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.BoltPainter;
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

    /** {@code conquerorRelease}'s composition, its charge (0..1), its variant (mastery x 4 + mode) and its seed. */
    public static void release(FX fx, float charge, int variant, long seed) {
        try {
            if (fx == null || charge < 0.999F) {
                return;
            }
            int mastery = Math.max(0, variant / 4);
            boolean supreme = charge >= 0.96F && mastery >= 1000;
            HakiBolts.Pending pending = HakiBolts.pending(fx);
            List<Bolt> storm = new ArrayList<>();
            for (ConquerorStorm.Call call : ConquerorStorm.release(mastery, seed, supreme)) {
                BoltPainter.strokes(call.a(), call.b(), call.width(), call.color(), call.duration(), call.delay(), false, SALT,
                    ConquerorStorm.LEVELS, storm);
            }
            pending.bolts.addAll(storm);
            pending.ground.addAll(storm);
            pending.wave = ConquerorStorm.reach(mastery);
        } catch (RuntimeException failure) {
            // the release plays as the original made it
        }
    }
}
