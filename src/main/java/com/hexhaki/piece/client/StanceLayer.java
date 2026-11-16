package com.hexhaki.piece.client;

import com.hexhaki.HexHaki;
import com.hexhaki.piece.network.S2CStance;
import com.hexhaki.piece.style.FightingStyle;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.2: the combat stances (R, {@link CombatMode}) on every client. A player in combat mode holds their style's stance, an arms-only
 * loop ({@code tools/style_anims.py}: the legs, body and head stay the game's, so walking and looking round are untouched), on a layer
 * of its own UNDER the original's (priority 850; the original's Haki layer is 900, the Don Gun 905, the fruits 1100). So a style move,
 * Armament Haki or anything else the original plays goes over the stance, eases in from it and back into it by itself when it ends:
 * the stance is never re-sent, it survives a respawn or a change of dimension (the layer is made again for the new player object) and
 * reaches players who walk up later ({@link S2CStance} on tracking). It eases out while the player holds something in the main hand,
 * rides, swims, flies, sleeps or lies dead, and eases back in after (2.1.3: a kit fruit no longer hides it: R swaps
 * the kit and the style, and the server says who is in combat mode). In first person the stance leaves the
 * game's own hand alone (its first-person mode is playerAnimator's default, NONE).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class StanceLayer {
    private static final ResourceLocation LAYER = HexHaki.id("piece_stance");
    private static final int PRIORITY = 850;
    private static final int FADE_TICKS = 5;
    /** Entity id -> the style whose stance that player holds (combat mode on). */
    private static final Map<Integer, FightingStyle> HELD = new HashMap<>();
    /** Entity id -> what this client last put on that player's layer. */
    private static final Map<Integer, Playing> PLAYING = new HashMap<>();

    private record Playing(ModifierLayer<IAnimation> layer, String animation) {
    }

    private StanceLayer() {
    }

    public static void accept(S2CStance message) {
        FightingStyle style = FightingStyle.byOrdinal(message.style());
        if (message.style() < 0 || !style.choice()) {
            HELD.remove(message.entity());
        } else {
            HELD.put(message.entity(), style);
        }
    }

    /** The stance {@code player} should show now, or null. */
    static String wanted(AbstractClientPlayer player, FightingStyle style) {
        if (style == null || style.stance() == null || !player.m_6084_() || player.m_5833_() || player.m_20159_()
            || !player.m_21205_().m_41619_() || player.m_6117_() || player.m_6147_()) {
            return null; // 2.1.3: also while using an item (a shield, food in the off hand) or on a ladder or vine
        }
        Pose pose = player.m_20089_();
        return pose == Pose.STANDING || pose == Pose.CROUCHING ? style.stance() : null;
    }

    /** {@code player}'s stance layer, made (once per player object) under the original's layers. */
    @SuppressWarnings("unchecked")
    static ModifierLayer<IAnimation> layer(AbstractClientPlayer player) {
        PlayerAnimationAccess.PlayerAssociatedAnimationData data = PlayerAnimationAccess.getPlayerAssociatedData(player);
        IAnimation existing = data.get(LAYER);
        if (existing instanceof ModifierLayer<?> layer) {
            return (ModifierLayer<IAnimation>) layer;
        }
        ModifierLayer<IAnimation> layer = new ModifierLayer<>();
        PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(PRIORITY, layer);
        data.set(LAYER, layer);
        return layer;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (event.phase != TickEvent.Phase.END || level == null || HELD.isEmpty() && PLAYING.isEmpty()) {
            return;
        }
        Set<Integer> ids = new HashSet<>(HELD.keySet());
        ids.addAll(PLAYING.keySet());
        for (int id : ids) {
            try {
                update(level, id);
            } catch (RuntimeException failure) {
                PLAYING.remove(id); // a stance never stops the game
            }
        }
    }

    static void update(ClientLevel level, int id) {
        Entity entity = level.m_6815_(id);
        if (!(entity instanceof AbstractClientPlayer player)) {
            PLAYING.remove(id); // gone from view: its layer went with it; HELD stays for when it comes back
            return;
        }
        String want = wanted(player, HELD.get(id));
        Playing playing = PLAYING.get(id);
        if (want == null && playing == null) {
            return;
        }
        ModifierLayer<IAnimation> layer = layer(player);
        String current = playing != null && playing.layer() == layer ? playing.animation() : null;
        if (Objects.equals(want, current)) {
            if (want == null) {
                PLAYING.remove(id); // what it held was on a player object that is gone
            }
            return;
        }
        KeyframeAnimation data = want == null ? null : PlayerAnimationRegistry.getAnimation(HexHaki.id(want));
        layer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(FADE_TICKS, Ease.INOUTQUAD),
            data == null ? null : new KeyframeAnimationPlayer(data));
        if (want == null) {
            PLAYING.remove(id);
        } else {
            PLAYING.put(id, new Playing(layer, want));
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        HELD.clear();
        PLAYING.clear();
    }
}
