package com.hexhaki.piece.client.sword;

import com.hexhaki.HexHaki;
import com.hexhaki.piece.network.S2CSword;
import com.hexhaki.piece.sword.Katana;
import com.hexhaki.piece.sword.SwordAnims;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
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
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.4: every player's sword as the server says ({@link S2CSword}): the katana at their hip, what it is doing and since when. Its
 * animations play on a playerAnimator layer of their own (priority {@link #PRIORITY}: over the stance and the original's Haki layer,
 * under the fruits), in the third-person model's first-person mode with both arms shown, so a cut seen from one's own eyes is the same
 * cut, in the same rhythm, as everyone else sees. A new cut fades in over two ticks from wherever the last left the arms; sheathed, the
 * layer fades out and the arms are the game's again. While a cut, the charge or the quick draw plays, the player's body is turned to
 * where they look, so the cut lands where the server sweeps it.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class SwordClient {
    static final ResourceLocation LAYER = HexHaki.id("piece_sword");
    static final int PRIORITY = 920;
    private static final Map<Integer, S2CSword> SEEN = new HashMap<>();
    private static final Map<Integer, Playing> PLAYING = new HashMap<>();
    /** 2.3.2: entity id -> the game tick it last moved (walked, jumped, crouched): the guard plays while moving, the idle when still. */
    private static final Map<Integer, Long> MOVED = new HashMap<>();
    /** Ticks a player stands still before the idle takes over from the guard (no flicker between steps). */
    static final int STILL_AFTER = 6;

    private record Playing(ModifierLayer<IAnimation> layer, int anim, int start, String name) {
    }

    private SwordClient() {
    }

    public static void accept(S2CSword message) {
        if (message.katana() < 0 && message.anim() == SwordAnims.NONE) {
            SEEN.remove(message.entity());
        } else {
            SEEN.put(message.entity(), message);
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level != null) {
            try {
                update(level, message.entity());
            } catch (RuntimeException failure) {
                PLAYING.remove(message.entity());
            }
        }
    }

    /** The katana at {@code entity}'s hip (null: none). */
    public static Katana katana(int entity) {
        S2CSword seen = SEEN.get(entity);
        return seen == null ? null : Katana.byOrdinal(seen.katana());
    }

    /** What {@code entity}'s sword is doing ({@link SwordAnims}). */
    public static int anim(int entity) {
        S2CSword seen = SEEN.get(entity);
        return seen == null ? SwordAnims.NONE : seen.anim();
    }

    /** Ticks into what it is doing, this frame. */
    public static float ticks(Entity entity, float partial) {
        S2CSword seen = SEEN.get(entity.m_19879_());
        if (seen == null || entity.m_9236_() == null) {
            return 0.0F;
        }
        long now = entity.m_9236_().m_46467_() & 0x3FFFFFFF;
        long elapsed = (now - seen.start()) & 0x3FFFFFFF;
        return elapsed + partial;
    }

    /** The player's sword layer, made once per player object. */
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

    /** The layer if it was made already (null otherwise), for reading its item channels. */
    @SuppressWarnings("unchecked")
    static ModifierLayer<IAnimation> existing(AbstractClientPlayer player) {
        IAnimation existing = PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER);
        return existing instanceof ModifierLayer<?> layer ? (ModifierLayer<IAnimation>) layer : null;
    }

    static void update(ClientLevel level, int id) {
        Entity entity = level.m_6815_(id);
        if (!(entity instanceof AbstractClientPlayer player)) {
            PLAYING.remove(id);
            return;
        }
        S2CSword seen = SEEN.get(id);
        int anim = seen == null ? SwordAnims.NONE : seen.anim();
        int start = seen == null ? 0 : seen.start();
        Playing playing = PLAYING.get(id);
        ModifierLayer<IAnimation> layer = layer(player);
        String name = SwordAnims.name(anim, katana(id));
        if (anim == SwordAnims.GUARD && still(level, player)) {
            name = SwordAnims.idle(katana(id));
        }
        if (playing != null && playing.layer() == layer && playing.anim() == anim && (anim == SwordAnims.NONE || playing.start() == start)
            && java.util.Objects.equals(playing.name(), name)) {
            return;
        }
        if (playing == null && anim == SwordAnims.NONE) {
            return;
        }
        KeyframeAnimation data = name == null ? null : PlayerAnimationRegistry.getAnimation(HexHaki.id(name));
        KeyframeAnimationPlayer player2 = null;
        if (data != null) {
            player2 = new KeyframeAnimationPlayer(data).setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
                .setFirstPersonConfiguration(new FirstPersonConfiguration().setShowRightArm(true).setShowLeftArm(true)
                    .setShowRightItem(true).setShowLeftItem(false));
        }
        boolean swap = playing != null && playing.anim() == SwordAnims.GUARD && anim == SwordAnims.GUARD;
        int fade = anim == SwordAnims.NONE ? 4 : swap ? 6 : anim == SwordAnims.GUARD ? 3 : 2;
        layer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fade, Ease.INOUTQUAD), player2);
        if (anim == SwordAnims.NONE) {
            PLAYING.remove(id);
        } else {
            PLAYING.put(id, new Playing(layer, anim, start, name));
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (event.phase != TickEvent.Phase.END || level == null || SEEN.isEmpty() && PLAYING.isEmpty()) {
            return;
        }
        Set<Integer> ids = new HashSet<>(SEEN.keySet());
        ids.addAll(PLAYING.keySet());
        for (int id : ids) {
            try {
                update(level, id);
                face(level, id);
            } catch (RuntimeException failure) {
                PLAYING.remove(id); // a sword never stops the game
            }
        }
    }

    /**
     * 2.3.2: has {@code player} stood still (on the ground, upright, not walking) for {@link #STILL_AFTER} ticks? The guard between cuts
     * is the right arm alone so walking and looking round stay the game's; standing still the whole body may breathe (the idle).
     */
    static boolean still(ClientLevel level, AbstractClientPlayer player) {
        long now = level.m_46467_();
        double dx = player.m_20185_() - player.f_19854_;
        double dz = player.m_20189_() - player.f_19856_;
        if (dx * dx + dz * dz > 0.0016 || !player.m_20096_() || player.m_20089_() != net.minecraft.world.entity.Pose.STANDING) {
            MOVED.put(player.m_19879_(), now);
            return false;
        }
        Long moved = MOVED.get(player.m_19879_());
        return moved == null || now - moved >= STILL_AFTER;
    }

    /** While a cut plays, the body turns to the look (a player's body otherwise lags it), so the cut is where the server sweeps it. */
    static void face(ClientLevel level, int id) {
        int anim = anim(id);
        if (anim == SwordAnims.NONE || anim == SwordAnims.GUARD || anim == SwordAnims.SHEATHE) {
            return;
        }
        if (level.m_6815_(id) instanceof AbstractClientPlayer player) {
            player.f_20883_ = player.f_20885_;
            player.f_20884_ = player.f_20886_;
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SEEN.clear();
        PLAYING.clear();
        MOVED.clear();
    }
}
