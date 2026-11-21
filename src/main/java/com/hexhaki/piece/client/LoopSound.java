package com.hexhaki.piece.client;

import com.hexhaki.piece.PieceSounds;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/**
 * 2.9.0 (the owner: "the sound while it's charging ends all of a sudden"): a sound that lasts as long as its move does, on this client:
 * it follows {@code entity} ({@code up} blocks over its feet), fades in, plays (round and round when a loop) while {@code on} holds, and
 * fades out over {@link #FADE} ticks when it no longer does (or the entity is gone), never cut off. The dragon's charge, the light held
 * at Kizaru's fingertip or in his leg, the hum of his sword. Every clip is a web recording (PieceSounds' events).
 */
public final class LoopSound extends AbstractTickableSoundInstance {
    /** How long it fades in and out (ticks). */
    static final int FADE = 8;
    private final Entity entity;
    private final BooleanSupplier on;
    private final float full;
    private final double up;
    private float level;
    private boolean ending;

    private LoopSound(SoundEvent event, Entity entity, float volume, float pitch, boolean loop, BooleanSupplier on, double up, float start) {
        super(event, SoundSource.PLAYERS, SoundInstance.m_235150_());
        this.entity = entity;
        this.on = on;
        this.full = volume;
        this.up = up;
        this.level = start;
        this.f_119573_ = volume * start;
        this.f_119574_ = pitch;
        this.f_119578_ = loop;
        this.f_119579_ = 0;
        this.f_119580_ = SoundInstance.Attenuation.LINEAR;
        this.place();
    }

    /**
     * Plays {@code name} (one of PieceSounds' events) on {@code entity} while {@code on} holds: a loop when {@code loop}; {@code fadeIn}
     * whether it rises from quiet (a loop taking over from a build-up) or starts at once (a build-up's own attack).
     */
    public static LoopSound play(String name, Entity entity, float volume, float pitch, boolean loop, boolean fadeIn, double up,
                                 BooleanSupplier on) {
        SoundEvent event = PieceSounds.event(name);
        if (event == null || entity == null) {
            return null;
        }
        LoopSound sound = new LoopSound(event, entity, volume, pitch, loop, on, up, fadeIn ? 0.15F : 1.0F);
        Minecraft.m_91087_().m_91106_().m_120367_(sound);
        return sound;
    }

    void place() {
        this.f_119575_ = this.entity.m_20185_();
        this.f_119576_ = this.entity.m_20186_() + this.up;
        this.f_119577_ = this.entity.m_20189_();
    }

    /** Ends it (fading out) whatever {@code on} says. */
    public void end() {
        this.ending = true;
    }

    @Override
    public void m_7788_() {
        boolean keep = !this.ending && this.entity.m_6084_() && this.on.getAsBoolean();
        if (!keep) {
            this.ending = true;
            this.level -= 1.0F / FADE;
            if (this.level <= 0.0F) {
                this.f_119573_ = 0.0F;
                this.m_119609_();
                return;
            }
        } else if (this.level < 1.0F) {
            this.level = Math.min(1.0F, this.level + 1.0F / FADE);
        }
        this.f_119573_ = this.full * this.level;
        this.place();
    }
}
