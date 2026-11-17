package com.hexhaki.piece.client;

import com.hexhaki.binds.client.AbilityBook;
import com.hexhaki.client.devilfruit.screen.GearMasteryScreen;
import com.hexhaki.client.screen.HakiMasteryScreen;
import com.hexhaki.piece.ui.LogbookView;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.1: the mastery key opens the Captain's Log ({@link LogbookScreen}) where the original opened its Ability Book. 2.2.4 (the owner:
 * "there's two different types of GUI ... put them all on the same thing ... check the fruit mastery GUI. It's still old"): the log is
 * the one menu, so every one of the original's menus opens it instead, on its own page: the Ability Book on the overview, the Haki
 * Techniques on Style &amp; Haki, the Gear mastery screen (the mastery key in the Nika fruit's Devil Fruit mode) on the Devil Fruit.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class MenuSwap {
    private MenuSwap() {
    }

    @SubscribeEvent
    public static void opening(ScreenEvent.Opening event) {
        Screen next = event.getNewScreen();
        if (next instanceof AbilityBook) {
            event.setNewScreen(new LogbookScreen(LogbookView.OVERVIEW));
        } else if (next instanceof HakiMasteryScreen) {
            event.setNewScreen(new LogbookScreen(LogbookView.STYLE_HAKI));
        } else if (next instanceof GearMasteryScreen) {
            event.setNewScreen(new LogbookScreen(LogbookView.FRUIT));
        }
    }
}
