package com.hexhaki.piece.server;

import com.hexhaki.devilfruit.data.GearData;
import com.hexhaki.piece.PieceEffects;
import com.hexhaki.piece.dragon.DragonForm;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.7.3: the Transformed effect (PieceEffects) on whoever is transformed: the Uo Uo no Mi's dragon (from the transformation to the way
 * back) and Nika's Gear 5. Coming on, the health is doubled with the maximum (the same share of it); going, it is kept within the
 * maximum again. Resistance II with it, a few seconds at a time renewed while it lasts (so it never takes away a Resistance the player
 * has from elsewhere).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class Transformed {
    static final int EVERY = 10;
    static final int RESISTANCE = 1;

    private Transformed() {
    }

    /** Whether {@code player} is transformed now. */
    public static boolean transformed(ServerPlayer player) {
        if (DragonServer.phase(player) != DragonForm.HUMAN) {
            return true;
        }
        try {
            return GearData.hasFruit(player) && GearData.flag(player, "gear5");
        } catch (RuntimeException e) {
            return false;
        }
    }

    @SubscribeEvent
    public static void transformedTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.f_19797_ % EVERY != 0) {
            return;
        }
        MobEffect effect = PieceEffects.TRANSFORMED.get();
        boolean want = player.m_6084_() && transformed(player);
        boolean has = player.m_21023_(effect);
        if (want) {
            if (!has) {
                float share = player.m_21223_() / Math.max(1.0F, player.m_21233_());
                player.m_7292_(new MobEffectInstance(effect, -1, 0, false, false, true));
                player.m_21153_(Math.min(player.m_21233_(), share * player.m_21233_()));
            }
            player.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 40, RESISTANCE, false, false, true));
        } else if (has) {
            player.m_21195_(effect);
            if (player.m_21223_() > player.m_21233_()) {
                player.m_21153_(player.m_21233_());
            }
        }
    }
}
