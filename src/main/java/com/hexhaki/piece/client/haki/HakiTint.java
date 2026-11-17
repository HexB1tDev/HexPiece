package com.hexhaki.piece.client.haki;

import com.hexhaki.client.vfx.HakiVfx;
import com.hexhaki.piece.haki.HueTint;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import net.minecraft.resources.ResourceLocation;

/**
 * 2.3.3 (the owner: "The unleashing of the conqueror still has red particles, even if my Rio is fucking like blue-ish"): the original's
 * Conqueror effects in the player's own Ryou colour. Through {@code patch/PiecePatch.java}: {@code HakiVfx.accept0} names whose
 * effect it is building ({@link #owner}), {@code HakiVfx.conquerorParticle} passes its colour through {@link #conqueror}, and the
 * Conqueror's and the last stand's own {@code particleTex} calls come here ({@link #particleTex}). A player with no Ryou colour known
 * keeps the original's colours.
 */
public final class HakiTint {
    private static int owner = -1;

    private HakiTint() {
    }

    /** The entity the original is building a Haki visual for now. */
    public static void owner(int entity) {
        owner = entity;
    }

    /** A Conqueror particle's colour (ARGB) in the owner's Ryou hue. */
    public static int conqueror(int argb) {
        int ryou = owner < 0 ? 0 : RyouColors.of(owner);
        return ryou == 0 ? argb : HueTint.tint(argb, ryou);
    }

    /** {@code HakiVfx.particleTex} with its colour tinted. */
    public static ParticleEmitter particleTex(int duration, int lifetime, float speed, float size, int color, boolean bloom,
                                              ResourceLocation texture) {
        return HakiVfx.particleTex(duration, lifetime, speed, size, conqueror(color), bloom, texture);
    }
}
