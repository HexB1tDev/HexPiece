package com.hexhaki.piece;

import com.hexhaki.HexHaki;
import com.hexhaki.piece.sword.SwordSounds;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 2.2.9: HexPiece's own sound events ({@code hexhaki:piece.*}, defined in the merged {@code sounds.json}): the Swordsman's, from the
 * web (CC0, OpenGameArt: {@link SwordSounds}); the clashes' and (2.4.6) the evil Conqueror's, the owner's own; (2.6.0) the Conqueror's
 * release explosion, from the web (CC0). Played on the server for everyone near.
 */
public final class PieceSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HexPiece.MODID);
    private static final Map<String, RegistryObject<SoundEvent>> BY_NAME = new HashMap<>();

    static {
        for (String name : SwordSounds.ALL) {
            BY_NAME.put(name, SOUNDS.register(name, () -> SoundEvent.m_262824_(HexHaki.id(name))));
        }
        // 2.3.6: two blows meeting (the owner's own clash sounds, from HexGhoul)
        String clash = com.hexhaki.piece.clash.ClashRules.SOUND;
        BY_NAME.put(clash, SOUNDS.register(clash, () -> SoundEvent.m_262824_(HexHaki.id(clash))));
        // 2.4.6: the evil Conqueror's voice line and roar (the owner's own clip, Alignment)
        for (String name : Alignment.SOUNDS) {
            BY_NAME.put(name, SOUNDS.register(name, () -> SoundEvent.m_262824_(HexHaki.id(name))));
        }
        // 2.6.0: the Conqueror's release explosion (CC0 recordings from the web, haki/ConquerorMix); 2.9.5: its charge, held
        for (String explosion : new String[] {com.hexhaki.piece.haki.ConquerorMix.EXPLOSION, com.hexhaki.piece.haki.ConquerorMix.EXPLOSION_GOOD,
            com.hexhaki.piece.haki.ConquerorMix.EXPLOSION_EVIL, com.hexhaki.piece.haki.ConquerorMix.CHARGE,
            com.hexhaki.piece.haki.ConquerorMix.CHARGE_HOLD}) {
            BY_NAME.put(explosion, SOUNDS.register(explosion, () -> SoundEvent.m_262824_(HexHaki.id(explosion))));
        }
        // 2.8.6: the Uo Uo no Mi's sounds (CC0 recordings from Freesound.org, dragon/UoSounds)
        for (String[] event : com.hexhaki.piece.dragon.UoSounds.EVENTS) {
            String name = event[0];
            BY_NAME.put(name, SOUNDS.register(name, () -> SoundEvent.m_262824_(HexHaki.id(name))));
        }
        // 2.9.0: the Pika Pika no Mi's (CC0 recordings from Freesound.org, pika/PikaSounds)
        for (String[] event : com.hexhaki.piece.pika.PikaSounds.EVENTS) {
            String name = event[0];
            BY_NAME.put(name, SOUNDS.register(name, () -> SoundEvent.m_262824_(HexHaki.id(name))));
        }
        // 3.1.1: the Gura Gura no Mi's (CC0 recordings from Freesound.org, gura/GuraSounds)
        for (String[] event : com.hexhaki.piece.gura.GuraSounds.EVENTS) {
            String name = event[0];
            BY_NAME.put(name, SOUNDS.register(name, () -> SoundEvent.m_262824_(HexHaki.id(name))));
        }
        // 3.1.3: the Magu Magu no Mi's (CC0 recordings from Freesound.org, magu/MaguSounds)
        for (String[] event : com.hexhaki.piece.magu.MaguSounds.EVENTS) {
            String name = event[0];
            BY_NAME.put(name, SOUNDS.register(name, () -> SoundEvent.m_262824_(HexHaki.id(name))));
        }
        // 3.1.9: the Hie Hie no Mi's (CC0 recordings from Freesound.org, hie/HieSounds)
        for (String[] event : com.hexhaki.piece.hie.HieSounds.EVENTS) {
            String name = event[0];
            BY_NAME.put(name, SOUNDS.register(name, () -> SoundEvent.m_262824_(HexHaki.id(name))));
        }
        // 3.2.3: the Nika fruit's transformation drums (CC0 recordings from Freesound.org, gear/GearSounds)
        for (String[] event : com.hexhaki.piece.gear.GearSounds.EVENTS) {
            String name = event[0];
            BY_NAME.put(name, SOUNDS.register(name, () -> SoundEvent.m_262824_(HexHaki.id(name))));
        }
    }

    /** 2.8.6: one of HexPiece's own sound events by name, or null (not one of ours, or not registered yet). */
    public static SoundEvent event(String name) {
        RegistryObject<SoundEvent> sound = BY_NAME.get(name);
        return sound == null ? null : sound.get();
    }

    private PieceSounds() {
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }

    /** One beat of a sword ({@link SwordSounds.Beat}) heard where {@code user} is. */
    public static void play(LivingEntity user, SwordSounds.Beat beat) {
        play(user, beat.sound(), beat.volume(), beat.pitch());
    }

    /** 2.3.6: {@code name} heard at a point ({@code near}'s world), by everyone near, at {@code volume} and about {@code pitch}. */
    public static void playAt(LivingEntity near, double x, double y, double z, String name, float volume, float pitch) {
        RegistryObject<SoundEvent> sound = BY_NAME.get(name);
        if (sound == null || near == null) {
            return;
        }
        float vary = 0.95F + near.m_217043_().m_188501_() * 0.1F;
        near.m_9236_().m_6263_(null, x, y, z, sound.get(), SoundSource.PLAYERS, volume, pitch * vary);
    }

    /**
     * 2.4.6: {@code name} heard where {@code user} is, by everyone near, at {@code volume} and its own pitch (a voice line is never
     * varied: a varied pitch is another voice).
     */
    public static void voice(LivingEntity user, String name, float volume) {
        RegistryObject<SoundEvent> sound = BY_NAME.get(name);
        if (sound == null || user == null) {
            return;
        }
        user.m_9236_().m_6263_(null, user.m_20185_(), user.m_20186_() + 1.4, user.m_20189_(), sound.get(), SoundSource.PLAYERS, volume, 1.0F);
    }

    /** {@code name} heard where {@code user} is, by everyone near, at {@code volume} and about {@code pitch} (a little varied). */
    public static void play(LivingEntity user, String name, float volume, float pitch) {
        RegistryObject<SoundEvent> sound = BY_NAME.get(name);
        if (sound == null || user == null) {
            return;
        }
        float vary = 0.95F + user.m_217043_().m_188501_() * 0.1F;
        user.m_9236_().m_6263_(null, user.m_20185_(), user.m_20186_() + 1.0, user.m_20189_(), sound.get(), SoundSource.PLAYERS, volume,
            pitch * vary);
    }
}
