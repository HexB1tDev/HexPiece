package com.hexhaki.piece.client.gun;

import com.hexhaki.HexHaki;
import com.hexhaki.piece.gun.Gun;
import com.hexhaki.piece.gun.GunAnims;
import com.hexhaki.piece.gun.GunRules;
import com.hexhaki.piece.network.S2CGun;
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
 * 3.8.8: every player's gun as the server says ({@link S2CGun}): which gun is in the hand, what it is doing and since when. Its clips
 * (gun/GunAnims, tools/gun_anims.py) play on a playerAnimator layer of their own ({@link #PRIORITY}: over the stance and the sword's),
 * with no first-person view of their own (one's own gun is seen in third person: {@code client/sword/SwordCamera}). In the hold the clip
 * follows the body: standing still the idle (now and then the spin, {@link GunRules#spins}), walking the walk, sprinting the run; a new
 * clip fades in from wherever the last left the arms. While the gun is raised and fired the body turns to the look, so the shot is
 * where the server flies it.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class GunClient {
    static final ResourceLocation LAYER = HexHaki.id("piece_gun");
    static final int PRIORITY = 925;
    private static final Map<Integer, S2CGun> SEEN = new HashMap<>();
    private static final Map<Integer, Playing> PLAYING = new HashMap<>();
    /** Entity id -> the game tick it last moved; and the tick its spin began (it plays {@link GunAnims#SPIN_TICKS}). */
    private static final Map<Integer, Long> MOVED = new HashMap<>();
    private static final Map<Integer, Long> SPUN = new HashMap<>();

    private record Playing(ModifierLayer<IAnimation> layer, int anim, int start, String name) {
    }

    private GunClient() {
    }

    public static void accept(S2CGun message) {
        if (message.gun() < 0 && message.anim() == GunAnims.NONE) {
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

    /** The gun in {@code entity}'s hand (null: none). */
    public static Gun gun(int entity) {
        S2CGun seen = SEEN.get(entity);
        return seen == null ? null : Gun.byOrdinal(seen.gun());
    }

    /** What {@code entity}'s gun is doing ({@link GunAnims}). */
    public static int anim(int entity) {
        S2CGun seen = SEEN.get(entity);
        return seen == null ? GunAnims.NONE : seen.anim();
    }

    /** Ticks into what it is doing, this frame. */
    public static float ticks(Entity entity, float partial) {
        S2CGun seen = SEEN.get(entity.m_19879_());
        if (seen == null || entity.m_9236_() == null) {
            return 0.0F;
        }
        long now = entity.m_9236_().m_46467_() & 0x3FFFFFFF;
        return ((now - seen.start()) & 0x3FFFFFFF) + partial;
    }

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

    /** The layer if it was made already (for reading its item channels), or null. */
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
        S2CGun seen = SEEN.get(id);
        int anim = seen == null ? GunAnims.NONE : seen.anim();
        int start = seen == null ? 0 : seen.start();
        Playing playing = PLAYING.get(id);
        long now = level.m_46467_();
        boolean moving = moving(level, player);
        boolean sprinting = moving && player.m_20142_();
        boolean spinning = false;
        if (anim == GunAnims.HOLD && !moving) {
            Long moved = MOVED.get(id);
            long still = moved == null ? now : now - moved;
            Long spun = SPUN.get(id);
            if (spun != null && now - spun < GunAnims.SPIN_TICKS) {
                spinning = true;
            } else if (GunRules.spins(still)) {
                SPUN.put(id, now);
                spinning = true;
                net.minecraft.sounds.SoundEvent whoosh = com.hexhaki.piece.PieceSounds.event(com.hexhaki.piece.gun.GunSounds.SPIN);
                if (whoosh != null) {
                    level.m_7785_(player.m_20185_(), player.m_20186_() + 0.9, player.m_20189_(), whoosh,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.35F, 1.5F, false);
                }
            }
        }
        String name = GunAnims.name(anim, moving, sprinting, spinning);
        if (playing != null && playing.anim() == anim && (anim == GunAnims.NONE || playing.start() == start)
            && java.util.Objects.equals(playing.name(), name)) {
            return;
        }
        if (playing == null && anim == GunAnims.NONE) {
            return;
        }
        ModifierLayer<IAnimation> layer = layer(player);
        KeyframeAnimation data = name == null ? null : PlayerAnimationRegistry.getAnimation(HexHaki.id(name));
        KeyframeAnimationPlayer clip = data == null ? null : new KeyframeAnimationPlayer(data).setFirstPersonMode(FirstPersonMode.NONE);
        // a shot snaps up in a tick; the hold's clips (idle, walk, run, spin) ease into each other
        int fade = anim == GunAnims.NONE ? 4 : GunAnims.firing(anim) ? 1 : anim == GunAnims.HOLD ? 5 : 2;
        layer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fade, Ease.INOUTQUAD), clip);
        if (anim == GunAnims.NONE) {
            PLAYING.remove(id);
        } else {
            PLAYING.put(id, new Playing(layer, anim, start, name));
        }
    }

    /** Is {@code player} walking (on the ground or not, any step this tick), as the hold's clip asks? */
    static boolean moving(ClientLevel level, AbstractClientPlayer player) {
        double dx = player.m_20185_() - player.f_19854_;
        double dz = player.m_20189_() - player.f_19856_;
        if (dx * dx + dz * dz > 0.0016 || !player.m_20096_()) {
            MOVED.put(player.m_19879_(), level.m_46467_());
            return true;
        }
        Long moved = MOVED.get(player.m_19879_());
        return moved != null && level.m_46467_() - moved < 3;
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
                if (GunAnims.firing(anim(id)) && level.m_6815_(id) instanceof AbstractClientPlayer player) {
                    // the body turns to the look while the gun is raised (a player's body otherwise lags it)
                    player.f_20883_ = player.f_20885_;
                    player.f_20884_ = player.f_20886_;
                }
            } catch (RuntimeException failure) {
                PLAYING.remove(id); // a gun never stops the game
            }
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SEEN.clear();
        PLAYING.clear();
        MOVED.clear();
        SPUN.clear();
        GunBullets.clear();
    }
}
