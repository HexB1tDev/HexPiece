package com.hexhaki.piece.server;

import com.hexhaki.command.HakiCommands;
import com.hexhaki.data.HakiData;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.data.HakiType;
import com.hexhaki.network.HakiNetwork;
import com.hexhaki.network.msg.S2CHakiVisual;
import com.hexhaki.registry.ModSounds;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

/**
 * 2.2.5: where the original's Conqueror's release asks HexPiece (calls redirected by {@code patch/PiecePatch.java} in
 * {@code HakiServerController.releaseConqueror}).
 * <ul>
 *   <li>The owner: "the hexpiece joyboy on/off, is not a thing anymore, anyone by default at max charge shall be able to do that. (keep
 *       and override the default max conqueror with the joyboy one since it's better.)": the release's JoyBoy switch reads true
 *       ({@link #joyBoy}; its mastery 1000 is 0 by the patch), so every full-charge radial release is the supreme one, for everyone;
 *       {@code /hp joyboy} says so ({@link #joyBoyCommand}).</li>
 *   <li>The owner: "At full charge, it should cause severe tears on the floor like fissures where the lightnings hit, then repair
 *       themselves automaticly (same as HexGhoul)": the release's visual goes out with a seed chosen here ({@link #visual}, as the
 *       original chose it: the player's random), kept for the moment; the supreme release's permanent destruction (the original's
 *       {@code SupremeHakiDestruction}) is the self-repairing fissures under the storm that seed draws ({@link #fissures},
 *       {@link Fissures}).</li>
 *   <li>2.4.7, the owner: "the crackle lightning sound at the end is to be removed along with all the old sounds of the letting go of G
 *       button max charge, keep the explosion sound and the new one we just added": the full release keeps only the original's blast
 *       (the explosion) and the evil roar ({@link AlignmentSounds}); its snap ({@link #sound}) and its rumble ({@link #rumble}) are
 *       gone, and its lightning lands silently (client/haki/HakiStorm, HakiBolts). A partial release keeps its own sound.</li>
 * </ul>
 */
public final class ConquerorRelease {
    /** The seed of each player's release just sent, by entity id (taken by the fissures in the same tick). */
    private static final Map<Integer, Long> SEEDS = new HashMap<>();

    private ConquerorRelease() {
    }

    /** The release's JoyBoy switch: always on. */
    public static boolean joyBoy(Player player) {
        return true;
    }

    /** The original's {@code HakiServerController.visual}, with the seed kept. */
    public static void visual(ServerPlayer player, S2CHakiVisual.Visual visual, float power, int variant) {
        WaterPrisons.conqueror(player); // 2.4.0: a Water Prison round the one letting it out bursts
        HakiFalls.released(player); // 2.4.4: the bodies it knocks down fall away from it
        long seed = player.m_217043_().m_188505_();
        if (visual == S2CHakiVisual.Visual.CONQUEROR_RELEASE) {
            SEEDS.put(player.m_19879_(), seed);
            AlignmentSounds.released(player, power); // 2.4.6: an evil player's full release roars
        }
        HakiNetwork.tracking(player, new S2CHakiVisual(player.m_19879_(), visual, power, variant, seed));
    }

    /** In place of the supreme release's permanent destruction: fissures under the storm, that heal. */
    public static void fissures(ServerPlayer player, double radius) {
        Long seed = SEEDS.remove(player.m_19879_());
        if (seed != null) {
            Fissures.open(player, seed, HakiData.mastery(player, HakiType.CONQUEROR));
        }
    }

    /**
     * The release's sounds ({@code HakiServerController.sound}, which plays each a second later, as the original's {@code delayedSound}
     * does): all but the full release's snap.
     */
    public static void sound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        if (sound == ModSounds.CONQUEROR_SUPREME_SNAP.get()) {
            return;
        }
        HakiServerController.delayedSound(player, sound, volume, pitch);
    }

    /** The full release's rumble ({@code scheduleSupremeAudio}): left out. */
    public static void rumble(ServerPlayer player) {
    }

    /** {@code /hp joyboy on|off}: there is no switch any more. */
    public static int joyBoyCommand(CommandSourceStack source, ServerPlayer player, boolean on) {
        HakiCommands.reply(source, "§eJoyBoy is not a switch any more: every full-charge Conqueror's release is the JoyBoy one, for everyone.");
        return 1;
    }
}
