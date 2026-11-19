package com.hexhaki.piece.server;

import com.hexhaki.data.HakiData;
import com.hexhaki.piece.Alignment;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.PieceSounds;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.4.6: an evil player's full-charge Conqueror's Haki has the owner's own sounds ({@link Alignment}). The owner: "Evil gets the sound I
 * sent, when it's full charge (holding G) the voice line plays " SAWAGASHII" and when released the other sound plays ... make it more
 * female if the player is a woman, male is this but more bass boosted."
 * <ul>
 *   <li>The voice line: the moment the original's charge (its {@code conquerorCharge}, 0..100, climbing while G is held) reaches 100,
 *       once a charge, heard by everyone near.</li>
 *   <li>The roar: when a full charge is let out (the release's visual, {@link ConquerorRelease#visual}, at power 1), over the
 *       original's own blast (2.4.7: its snap, rumble and crackle are gone: {@link ConquerorRelease#sound}).</li>
 * </ul>
 * A woman's versions for a female player, a man's otherwise. Good's own sounds are to come.
 *
 * <p>2.6.0 (the owner: "Conqueror sounds reduced by 35% but the line evil voice line increased by a ton"): the voice line plays at
 * full loudness (remade louder, and only the voice: tools/evil_conqueror_sounds.py); the roar at 0.65 of it (its sounds.json volume,
 * 0.40625 x {@link #VOLUME}; its attenuation 77 blocks keeps the reach it had).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class AlignmentSounds {
    /** Heard this far: the sounds' own reach (48 blocks in sounds.json) times this. */
    static final float VOLUME = 1.6F;
    /** The players whose voice line has played in the charge they are holding. */
    private static final Set<UUID> VOICED = new HashSet<>();

    private AlignmentSounds() {
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        try {
            boolean charging = HakiData.flag(player, "conquerorCharging");
            ConquerorRelease.charging(player, charging, charging ? HakiData.integer(player, "conquerorCharge") : 0); // 2.4.9: the stun's hold
            if (!charging) {
                VOICED.remove(player.m_20148_());
                return;
            }
            if (HakiData.integer(player, "conquerorCharge") >= 100 && VOICED.add(player.m_20148_())) {
                PieceProfile profile = ProfileServer.of(player);
                if (Alignment.evil(profile.alignment())) {
                    PieceSounds.voice(player, Alignment.evilVoice(profile.gender()), VOLUME);
                }
            }
        } catch (RuntimeException failure) {
            // a sound never stops the game
        }
    }

    /** The Conqueror's release let out at {@code power} (0..1; 1: the full charge): an evil player's roar. */
    static void released(ServerPlayer player, float power) {
        if (power < 0.999F) {
            return;
        }
        PieceProfile profile = ProfileServer.of(player);
        if (Alignment.evil(profile.alignment())) {
            PieceSounds.voice(player, Alignment.evilRelease(profile.gender()), VOLUME);
        }
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        VOICED.remove(event.getEntity().m_20148_());
        ConquerorRelease.loggedOut(event.getEntity().m_20148_());
    }
}
