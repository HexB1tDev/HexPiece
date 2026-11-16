package com.hexhaki.piece.client.render;

import com.hexhaki.piece.client.PieceClient;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.1: a female player (picked on the start menu) is drawn with the female body: her legs wider and deeper, a wide-armed skin's arms
 * slim; her bust is a layer ({@link FigureLayer}). Shaped just before she is drawn and put back just after, each scale divided back.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class FemalePlayers {
    private static final Map<AbstractClientPlayer, Boolean> SHAPED = new IdentityHashMap<>();

    private FemalePlayers() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void pre(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player) || !PieceClient.gender(player.m_19879_()).female()
            || SHAPED.containsKey(player) || !(event.getRenderer().m_7200_() instanceof PlayerModel<?> model)) {
            return;
        }
        boolean wide = !"slim".equals(player.m_108564_());
        FemaleFigure.shape(model, wide);
        SHAPED.put(player, wide);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void post(RenderPlayerEvent.Post event) {
        Boolean wide = event.getEntity() instanceof AbstractClientPlayer player ? SHAPED.remove(player) : null;
        if (wide != null && event.getRenderer().m_7200_() instanceof PlayerModel<?> model) {
            FemaleFigure.unshape(model, wide);
        }
    }
}
