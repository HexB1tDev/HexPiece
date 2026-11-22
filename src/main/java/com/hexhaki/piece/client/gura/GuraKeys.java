package com.hexhaki.piece.client.gura;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.FruitClient;
import com.hexhaki.fruit.client.FruitKeys;
import com.hexhaki.piece.client.PieceHooks;
import com.hexhaki.piece.client.PieceKeys;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.1.6: the Gura fruit's ultimate on the End key ({@link PieceKeys#ULTIMATE}): pressed and let go in fruit mode with the Gura fruit,
 * the original's fruit message goes to the server as a quick key's does ({@code FruitKeys.send}: 0 down, 1 up) with the ultimate's own
 * ability number (server/GuraUltimate.SLOT, out of the original's 0 to 8), so the kit's press and release take it. Let go is always
 * sent once it was pressed (a screen opening, the mode or the fruit changing let it go too), so a charge never sticks.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class GuraKeys {
    /** server/GuraUltimate.SLOT. */
    static final int SLOT = 40;
    private static boolean down;

    private GuraKeys() {
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        boolean can = minecraft.f_91074_ != null && minecraft.f_91080_ == null && PieceHooks.fruitKeysActive()
            && FruitClient.fruit(minecraft.f_91074_.m_19879_()) == Fruit.GURA;
        boolean held = can && PieceKeys.ULTIMATE.m_90857_();
        while (PieceKeys.ULTIMATE.m_90859_()) {
            // the clicks are counted by the key itself: only whether it is held matters here
        }
        if (held && !down) {
            down = true;
            FruitKeys.send(0, SLOT, FruitKeys.flags(minecraft));
        } else if (!held && down) {
            down = false;
            if (minecraft.f_91074_ != null) {
                FruitKeys.send(1, SLOT, FruitKeys.flags(minecraft));
            }
        }
    }
}
