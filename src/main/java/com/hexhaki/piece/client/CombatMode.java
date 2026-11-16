package com.hexhaki.piece.client;

import com.hexhaki.client.HakiClientState;
import com.hexhaki.client.HakiKeys;
import com.hexhaki.client.devilfruit.GearClientState;
import com.hexhaki.fruit.client.FruitClient;
import com.hexhaki.network.HakiAction;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.network.C2SCombat;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.style.FightingStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.2 (the owner: "same as HexGhoul tap R to enable combat"): combat mode, as HexGhoul's R puts a ghoul into ghoul mode. R was the
 * original's Armament Haki key, so, as HexGhoul moved its kagune to Ctrl + R: tap R for combat mode, Ctrl + R for Armament Haki. In Devil
 * Fruit mode or with a fruit kit on, R stays the original's (Gear 5's transformation, Armament), and a player who has not set sail yet
 * keeps R as Armament; combat mode switches itself off when any of those starts. The presses are taken before the original's key
 * handling (a higher priority on the same tick, both use consumeClick), so it never sees the ones that were combat mode's. While combat
 * mode is on, the fighting style throws its moves ({@link StyleInput}), the style's stance is held ({@link StanceLayer}) and a badge
 * sits over the hotbar.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class CombatMode {
    private static boolean on;

    private CombatMode() {
    }

    public static boolean on() {
        return on;
    }

    static void set(boolean value) {
        if (on != value) {
            on = value;
            PieceNetwork.toServer(new C2SCombat(value));
        }
    }

    static void reset() {
        on = false;
    }

    /** Combat mode is there for a player who has set sail with a style, outside Devil Fruit mode and with no fruit kit on. */
    static boolean allowed() {
        PieceProfile self = PieceClient.self();
        return self.chosen() && self.style().choice() && !GearClientState.devilFruitMode() && !FruitClient.active();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void keys(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ == null) {
            return;
        }
        if (!allowed()) {
            if (on) {
                set(false); // Devil Fruit mode, a fruit kit on or no style any more: R is the original's again
            }
            return;
        }
        if (minecraft.f_91080_ != null) {
            return; // the original drains R under a screen
        }
        while (HakiKeys.ARMAMENT.m_90859_()) {
            if (Screen.m_96637_()) {
                if (HakiClientState.enabled) {
                    HakiKeys.send(HakiAction.ARMAMENT_TOGGLE);
                }
            } else {
                set(!on);
            }
        }
    }

    /** The badge over the hotbar's left end while combat mode is on: the style's colour and name, and its special keys. */
    static void draw(GuiGraphics graphics, int width, int height) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (!on || minecraft.f_91074_ == null || minecraft.f_91066_ == null) {
            return;
        }
        FightingStyle style = PieceClient.self().style();
        Font font = minecraft.f_91062_;
        String text = style.display() + "  [Y] [U]";
        int w = font.m_92895_(text) + 18;
        int x = width / 2 - 91 - w - 6;
        int y = height - 22;
        if (x < 4) {
            x = 4;
            y = height - 52;
        }
        graphics.m_280509_(x, y, x + w, y + 18, 0xA0101828);
        graphics.m_280509_(x, y, x + 3, y + 18, style.colour() | 0xFF000000);
        graphics.m_280509_(x + 7, y + 7, x + 11, y + 11, 0xFFE8D7AE);
        graphics.m_280056_(font, text, x + 14, y + 5, 0xFFF4E8D0, true);
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Overlay {
        private Overlay() {
        }

        @SubscribeEvent
        public static void register(RegisterGuiOverlaysEvent event) {
            event.registerBelowAll("hexpiece_combat", (gui, graphics, partialTick, width, height) -> draw(graphics, width, height));
        }
    }
}
