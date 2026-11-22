package com.hexhaki.piece.server;

import com.hexhaki.devilfruit.data.GearData;
import com.hexhaki.devilfruit.data.GearTiming;
import com.hexhaki.piece.gear.GearSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 3.2.3 (the owner, on Gear 5's transformation: "if you could give the transformation just the drum sounds and just get rid of the music
 * because of copyright, that would be perfect"): the drums of liberation. The transformation's music is silenced (sounds.json); on each
 * of the chest pounding's beats ({@code GearTiming.DRUM_STRIKES}) the original's {@code GearServerController.tickTransform} played two
 * of the game's own sounds: patch/PiecePatch.gearDrums sends the first here ({@link #beat}: one CC0 drum, gear/GearSounds) and the
 * second to {@link #under} (nothing: one drum a beat).
 */
public final class GearDrums {
    /** How loud a beat is (range 48 blocks x this): everyone near hears the drums. */
    static final float VOLUME = 1.6F;

    private GearDrums() {
    }

    /** A beat of the drums: a deep drum, a little higher with each beat as it builds, the last one the heaviest. */
    public static void beat(ServerPlayer player, SoundEvent original, float volume, float pitch) {
        int tick = GearData.integer(player, "transformTick");
        int[] strikes = GearTiming.DRUM_STRIKES;
        int beat = 0;
        for (int i = 0; i < strikes.length; i++) {
            if (strikes[i] == tick) {
                beat = i;
            }
        }
        boolean last = beat == strikes.length - 1;
        SoundEvent drum = com.hexhaki.piece.PieceSounds.event(last ? GearSounds.DRUM_BIG : GearSounds.DRUM);
        if (drum == null) {
            return;
        }
        Vec3 at = player.m_20182_();
        player.m_9236_().m_6263_(null, at.f_82479_, at.f_82480_ + 1.0, at.f_82481_, drum, SoundSource.PLAYERS, last ? VOLUME * 1.15F : VOLUME,
            last ? 0.92F : 0.94F + 0.025F * beat);
    }

    /** The second sound of a beat: nothing (the drum is the beat). */
    public static void under(ServerPlayer player, SoundEvent original, float volume, float pitch) {
    }
}
