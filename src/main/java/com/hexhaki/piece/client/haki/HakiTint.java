package com.hexhaki.piece.client.haki;

import com.hexhaki.client.vfx.HakiVfx;
import com.hexhaki.network.msg.S2CHakiVisual;
import com.hexhaki.piece.haki.HueTint;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import net.minecraft.resources.ResourceLocation;

/**
 * 2.3.3 (the owner: "The unleashing of the conqueror still has red particles, even if my Rio is fucking like blue-ish"): the original's
 * Conqueror effects in the player's own Ryou colour. Through {@code patch/PiecePatch.java}: {@code HakiVfx.accept0} names whose
 * effect it is building ({@link #visual}), {@code HakiVfx.conquerorParticle} passes its colour through {@link #conqueror}, and the
 * Conqueror's and the last stand's own {@code particleTex} calls come here ({@link #particleTex}). A player with no Ryou colour known
 * keeps the original's colours.
 */
public final class HakiTint {
    /** 2.4.9: a hit's effect takes the colour of a player whose Haki effect was built this recently (nanoseconds). */
    static final long RECENT_NANOS = 3_000_000_000L;
    private static int owner = -1;
    private static boolean hit;
    private static int lastPlayer = -1;
    private static long lastAt;

    private HakiTint() {
    }

    /**
     * The visual the original is building now ({@code HakiVfx.accept0}): whose it is. 2.4.9 (the owner: "Partial stun, the circle around
     * is still red instead of having the ryou tint to it"): a Conqueror hit ({@code CONQUEROR_HIT}) plays on the body it struck, a mob
     * with no Ryou colour, so its circle and its lightning stayed the original's red; it takes the colour of the player whose Haki
     * effect was built last (the release that struck it goes out the same tick), within {@link #RECENT_NANOS}.
     */
    public static void visual(S2CHakiVisual message) {
        owner = message.entityId();
        hit = message.visual() == S2CHakiVisual.Visual.CONQUEROR_HIT;
        if (RyouColors.of(owner) != 0) {
            lastPlayer = owner;
            lastAt = System.nanoTime();
        }
    }

    /** The Ryou colour (RGB) the visual being built is drawn in, or 0 (the original's colours). */
    static int colour() {
        int ryou = owner < 0 ? 0 : RyouColors.of(owner);
        if (ryou == 0 && hit) {
            ryou = hitColour();
        }
        return ryou;
    }

    /** 2.4.9: while a hit is being built, the colour of the player whose release struck it (0: none known). */
    static int hitColour() {
        if (!hit || lastPlayer < 0 || System.nanoTime() - lastAt > RECENT_NANOS) {
            return 0;
        }
        return RyouColors.of(lastPlayer);
    }

    /** A Conqueror particle's colour (ARGB) in the owner's Ryou hue. */
    public static int conqueror(int argb) {
        int ryou = colour();
        return ryou == 0 ? argb : HueTint.tint(argb, ryou);
    }

    /** {@code HakiVfx.particleTex} with its colour tinted. */
    public static ParticleEmitter particleTex(int duration, int lifetime, float speed, float size, int color, boolean bloom,
                                              ResourceLocation texture) {
        return HakiVfx.particleTex(duration, lifetime, speed, size, conqueror(color), bloom, texture);
    }
}
