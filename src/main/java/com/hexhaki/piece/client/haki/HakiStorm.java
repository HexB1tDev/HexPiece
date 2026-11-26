package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.ConquerorStorm;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import com.lowdragmc.photon.client.gameobject.emitter.data.EmissionSetting;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.NumberFunction;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
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
 * and lands its strikes. The original's own release (its range lightning, burst and dome) is untouched. 2.4.7: the full release's
 * lightning lands silently (the owner: "the crackle lightning sound at the end is to be removed along with all the old sounds").
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
        // 2.9.5 (the owner: "after you let go, the all lightning sounds all at once, it's ridiculously loud"): a partial release's
        // lightning lands silently too, as a full one's has since 2.4.7: its crackles (one for the whole and one for each of its first
        // three strikes, each 3.4 s of the original's Armament zap, at once) were heard over the release's own thunder and explosion
        HakiBolts.silence(fx);
    }

    /**
     * 2.9.5: the charge's range lightning ({@code HakiVfx.conquerorCharge}'s call, its visual sent every few ticks while G is held), drawn as
     * the original draws it, landing silently: the charge is heard as its own held sound (ConquerorChargeSound), and these crackles piled
     * up under it and rang on past the release.
     */
    public static void charge(FX fx, float charge, int mastery, long seed, boolean release, int baseDelay) {
        com.hexhaki.client.vfx.HakiVfx.addConquerorRangeLightning(fx, charge, mastery, seed, release, baseDelay);
        HakiBolts.silence(fx);
    }

    /**
     * 2.4.7: the full release's aftershocks ({@code HakiVfx.aftershock}, three of them, to 74 ticks after it), as the original makes
     * them, but silent: the owner had their crackle taken out with the rest of the full release's old sounds.
     */
    public static FX aftershock(float power, long seed) {
        FX fx = com.hexhaki.client.vfx.HakiVfx.aftershock(power, seed);
        HakiBolts.silence(fx);
        if (power >= 0.96F) {
            thin(fx); // 3.8.1: as the full release's own
        }
        return fx;
    }

    /**
     * 3.8.1 (the owner: "Reduce Conqueror's Haki Release (g) end lag, when releasing the full charge of haki it causes a ton of lag for a
     * bit then stops"): every burst of {@code fx}'s particle emitters thinned ({@link ConquerorStorm#thinned}): the original's full
     * release (its range glow, its burst, its dome of five shells, its pressure fronts and horizon) and each of its three aftershocks
     * threw some 7,000 large see-through puffs within its first two seconds, each ticked and drawn by Photon on the CPU and the screen
     * painted over and over. What the composition is otherwise is untouched.
     */
    static void thin(FX fx) {
        try {
            for (IFXObject object : fx.getMainFX().objects()) {
                if (!(object instanceof ParticleEmitter emitter)) {
                    continue;
                }
                for (EmissionSetting.Burst burst : emitter.config.emission.getBursts()) {
                    Number count = burst.getCount().get(0.0F, () -> 0.0F);
                    int n = count == null ? 0 : count.intValue();
                    if (ConquerorStorm.thinned(n) != n) {
                        burst.setCount(NumberFunction.constant(ConquerorStorm.thinned(n)));
                    }
                }
            }
        } catch (RuntimeException failure) {
            // the composition plays as the original made it
        }
    }

    /** {@code conquerorRelease}'s composition, its charge (0..1), its variant (mastery x 4 + mode) and its seed. */
    public static void release(FX fx, float charge, int variant, long seed) {
        try {
            if (fx == null || charge < 0.96F) {
                return;
            }
            thin(fx);
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
            pending.silent = true;
        } catch (RuntimeException failure) {
            // the release plays as the original made it
        }
    }
}
