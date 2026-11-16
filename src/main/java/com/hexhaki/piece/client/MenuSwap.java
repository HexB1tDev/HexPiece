package com.hexhaki.piece.client;

import com.hexhaki.binds.client.AbilityBook;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.1: the mastery key opens the Captain's Log ({@link LogbookScreen}) where the original opened its Ability Book; the log's own
 * "Ability Book" button lets the book through once. The Gear 5 mastery screen (Devil Fruit mode with the Nika fruit) is left as it was.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class MenuSwap {
    private static boolean pass;

    private MenuSwap() {
    }

    static void passOnce() {
        pass = true;
    }

    @SubscribeEvent
    public static void opening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof AbilityBook) {
            if (pass) {
                pass = false;
                return;
            }
            event.setNewScreen(new LogbookScreen());
        }
    }
}
