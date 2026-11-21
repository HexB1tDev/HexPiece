package com.hexhaki.piece.server;

import com.hexhaki.patch.SoundBalance;
import com.hexhaki.piece.PieceSounds;
import com.hexhaki.piece.haki.ConquerorMix;
import com.hexhaki.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 2.6.0 (the owner: "Conqueror sounds reduced by 35% ... after the key is let go, add a proper explosion sound (FROM THE WEB)"), the
 * Conqueror's sounds on the server ({@link ConquerorMix}):
 * <ul>
 *   <li>{@link #pos}: where the original's {@code delayedSound} plays a Haki sound a second after it was asked
 *       ({@code SoundBalance.pos} in its {@code lambda$delayedSound$26}, redirected by {@code patch/PiecePatch.java}): a Conqueror sound
 *       at {@link ConquerorMix#QUIETER} of the loudness the original's balance gives it (its ceilings first, the 35 % after, so it is
 *       35 % quieter whatever was asked), every other sound as it was.</li>
 *   <li>{@link #explode}: the release's explosion, the moment G is let go ({@link ConquerorRelease#visual}).</li>
 *   <li>2.9.5: the original's charge sound (its press and its 38, 58, 78 and 100 % marks) is not played at all: the charge is one sound
 *       held on each client from the press to the release ({@link com.hexhaki.piece.haki.ConquerorMix#CHARGE}).</li>
 * </ul>
 */
public final class ConquerorSounds {
    private ConquerorSounds() {
    }

    /** The original's {@code SoundBalance.pos}, a Conqueror sound 35 % quieter. */
    public static void pos(Level level, Player except, BlockPos at, SoundEvent sound, SoundSource source, float volume, float pitch) {
        if (sound != null && sound == ModSounds.CONQUEROR_CHARGE.get()) {
            return; // 2.9.5: the charge is held on each client (ConquerorMix.CHARGE), never replayed from here
        }
        float played = volume;
        try {
            if (conqueror(sound)) {
                played = SoundBalance.volume(sound, volume) * ConquerorMix.QUIETER;
            }
        } catch (RuntimeException failure) {
            played = volume; // as the original's
        }
        SoundBalance.pos(level, except, at, sound, source, played, pitch);
    }

    /** True for the original's Conqueror sounds (its {@code conqueror_*} events). */
    static boolean conqueror(SoundEvent sound) {
        return sound != null && (sound == ModSounds.CONQUEROR_CHARGE.get() || sound == ModSounds.CONQUEROR_RELEASE.get()
            || sound == ModSounds.CONQUEROR_SUPREME_BLAST.get() || sound == ModSounds.CONQUEROR_SUPREME_RUMBLE.get()
            || sound == ModSounds.CONQUEROR_SUPREME_SNAP.get());
    }

    /** The release's explosion where {@code player} lets it out: by its charge ({@code power}, 0..1), a tap's small. */
    static void explode(ServerPlayer player, float power, boolean tap) {
        try {
            PieceSounds.play(player, ConquerorMix.explosion(ProfileServer.of(player).alignment()), ConquerorMix.volume(power, tap),
                ConquerorMix.pitch(power, tap));
        } catch (RuntimeException failure) {
            // a sound never stops the release
        }
    }
}
