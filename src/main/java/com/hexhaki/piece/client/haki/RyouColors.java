package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.haki.RyouPalette;
import com.hexhaki.piece.network.S2CRyou;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 2.1.3: every player's Ryou glow colour this client was told ({@link S2CRyou}), by entity id. */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class RyouColors {
    private static final Map<Integer, Integer> COLORS = new HashMap<>();
    /** 3.6.0: the Ryou colour (the coated arms' glow), by player, when it is not the Haki colour. */
    private static final Map<Integer, Integer> AURAS = new HashMap<>();

    private RyouColors() {
    }

    public static void accept(S2CRyou message) {
        if (message.color() == 0) {
            COLORS.remove(message.entity());
        } else {
            COLORS.put(message.entity(), message.color());
        }
        if (message.aura() == 0) {
            AURAS.remove(message.entity());
        } else {
            AURAS.put(message.entity(), message.aura());
        }
    }

    /** The player's colour (ARGB), or 0 if none is known. */
    public static int of(int entity) {
        return COLORS.getOrDefault(entity, 0);
    }

    /** The colour of a Ryou effect: the player's own, else the neutral fallback (never a colour of its own making). */
    public static int orNeutral(int entity) {
        int color = of(entity);
        return color != 0 ? color : RyouPalette.NEUTRAL | 0xFF000000;
    }

    /** 3.6.0: the colour of the player's Ryou glow round the coated arms (their Ryou colour, else their Haki colour, else neutral). */
    public static int aura(int entity) {
        Integer aura = AURAS.get(entity);
        return aura != null ? aura : orNeutral(entity);
    }

    /** This client's own player's colour, or 0. */
    public static int mine() {
        Minecraft minecraft = Minecraft.m_91087_();
        return minecraft.f_91074_ == null ? 0 : of(minecraft.f_91074_.m_19879_());
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        COLORS.clear();
        AURAS.clear();
    }
}
