package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.haki.LightningGate;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.6.0 (the owner: "Full COnqueror on releasing should not play all the lightning sounds at once, just one"): every sound about to
 * start on this client is asked of {@link LightningGate}; a Haki lightning sound while another still rings is not played. One place
 * for every source: the server's (the release, Last Stand), the original's client zaps and the expansion's (HakiBolts, HakiDraw).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class LightningSounds {
    private static final LightningGate GATE = new LightningGate();

    private LightningSounds() {
    }

    @SubscribeEvent
    public static void play(PlaySoundEvent event) {
        try {
            SoundInstance sound = event.getSound();
            ResourceLocation id = sound == null ? null : sound.m_7904_();
            if (id != null && !GATE.allow(LightningGate.rank(id.m_135827_(), id.m_135815_()), System.currentTimeMillis())) {
                event.setSound(null);
            }
        } catch (RuntimeException failure) {
            // a sound is played as it was
        }
    }
}
