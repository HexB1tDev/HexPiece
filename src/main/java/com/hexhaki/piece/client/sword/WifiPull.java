package com.hexhaki.piece.client.sword;

import com.hexhaki.HexHaki;
import com.hexhaki.piece.sword.WifiDraw;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * 3.5.9: WiFi Haki held, on every client (sword/WifiDraw): the original plays its clip "wifi_haki" when WiFi Haki starts and clears it
 * when it ends; {@code client/gear/NikaToon.clip} hands every clip any player starts here, so the blade eases out of the saya while that
 * clip is the player's and slides home after. SwordLayer and HolsterHand ask {@link #push}; the saya's frame comes from the clip (the
 * original's Haki layer) while it plays.
 */
public final class WifiPull {
    static final String CLIP = "wifi_haki";
    static final ResourceLocation HAKI_LAYER = HexHaki.id("haki_layer");
    /** By player id: the game tick WiFi Haki began, the tick it ended (-1 while held), how far out the blade was then. */
    private static final Map<Integer, double[]> PULLS = new HashMap<>();

    private WifiPull() {
    }

    static double now() {
        var level = Minecraft.m_91087_().f_91073_;
        return level == null ? 0.0 : level.m_46467_();
    }

    /** A clip started on player {@code id} (every client, every player). */
    public static void clip(int id, String name) {
        double now = now();
        if (CLIP.equals(name)) {
            PULLS.put(id, new double[] {now, -1.0, 0.0});
            return;
        }
        double[] pull = PULLS.get(id);
        if (pull != null && pull[1] < 0.0) {
            pull[2] = WifiDraw.push(now - pull[0]);
            pull[1] = now;
        }
        if (PULLS.size() > 64) {
            PULLS.values().removeIf(p -> p[1] >= 0.0 && now - p[1] > WifiDraw.HOME + 20);
        }
    }

    /** How far (model px) {@code player}'s blade is eased out of its saya by WiFi Haki now (0: not at all). */
    static float push(AbstractClientPlayer player, float partial) {
        double[] pull = PULLS.get(player.m_19879_());
        if (pull == null) {
            return 0.0F;
        }
        double now = now() + partial;
        if (pull[1] < 0.0) {
            return WifiDraw.push(now - pull[0]);
        }
        float home = WifiDraw.home((float) pull[2], now - pull[1]);
        if (home <= 0.0F) {
            PULLS.remove(player.m_19879_());
        }
        return home;
    }

    /** Is {@code player} holding WiFi Haki (its clip playing)? */
    static boolean held(AbstractClientPlayer player) {
        double[] pull = PULLS.get(player.m_19879_());
        return pull != null && pull[1] < 0.0;
    }

    /** Ticks into WiFi Haki held (for the first-person hands), or -1. */
    static double ticks(AbstractClientPlayer player, float partial) {
        double[] pull = PULLS.get(player.m_19879_());
        return pull == null || pull[1] >= 0.0 ? -1.0 : now() + partial - pull[0];
    }

    /** The original's Haki layer (its clips, WiFi Haki's among them), whose leftItem lays the saya while WiFi Haki's clip plays. */
    @SuppressWarnings("unchecked")
    static ModifierLayer<IAnimation> layer(AbstractClientPlayer player) {
        IAnimation layer = PlayerAnimationAccess.getPlayerAssociatedData(player).get(HAKI_LAYER);
        return layer instanceof ModifierLayer<?> modifier ? (ModifierLayer<IAnimation>) modifier : null;
    }

    public static void clear() {
        PULLS.clear();
    }
}
