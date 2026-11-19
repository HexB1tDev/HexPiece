package com.hexhaki.piece.client.sword;

import com.hexhaki.piece.sword.BladeCoat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * 2.6.1: how far the Haki coat has run up each player's blade this frame ({@link BladeCoat}): turned on and off with their Armament
 * ({@link SwordLayer#blackened}), timed on the client as the hands' coat is (client/haki/HakiSkin), turned round where it stands.
 */
public final class BladeCoatClient {
    /** A player's coat coming on or going off: which, since when (nanoseconds), from what progress. */
    private record Fade(boolean on, long since, float from) {
    }

    private static final Map<Integer, Fade> FADES = new HashMap<>();
    /** The level the fades belong to (entity ids are a level's own). */
    private static Object level;

    private BladeCoatClient() {
    }

    /** {@code player}'s blade coat now (0: bare steel, 1: black to the point). */
    public static float progress(AbstractClientPlayer player) {
        if (player == null) {
            return 0.0F;
        }
        Object current = player.m_9236_();
        if (current != level) {
            FADES.clear();
            level = current;
        }
        boolean on = SwordLayer.blackened(player);
        long now = System.nanoTime();
        int id = player.m_19879_();
        Fade fade = FADES.get(id);
        if (fade == null) {
            if (!on) {
                return 0.0F;
            }
            fade = new Fade(true, now, 0.0F);
            FADES.put(id, fade);
        } else if (fade.on() != on) {
            fade = new Fade(on, now, BladeCoat.progress(fade.on(), now - fade.since(), fade.from()));
            FADES.put(id, fade);
        }
        float progress = BladeCoat.progress(fade.on(), now - fade.since(), fade.from());
        if (!on && progress <= 0.0F) {
            FADES.remove(id);
        }
        return progress;
    }
}
