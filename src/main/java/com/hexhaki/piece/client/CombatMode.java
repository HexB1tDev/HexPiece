package com.hexhaki.piece.client;

import com.hexhaki.client.HakiClientState;
import com.hexhaki.client.HakiKeys;
import com.hexhaki.client.devilfruit.GearClientState;
import com.hexhaki.client.devilfruit.GearKeys;
import com.hexhaki.fruit.client.FruitClient;
import com.hexhaki.network.GearAction;
import com.hexhaki.network.HakiAction;
import com.hexhaki.network.HakiNetwork;
import com.hexhaki.network.msg.C2SGearAction;
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
 * 2.1.2 (the owner: "same as HexGhoul tap R to enable combat"): combat mode, as HexGhoul's R puts a ghoul into ghoul mode; R was the
 * original's Armament Haki key, so Armament moved to Ctrl + R. 2.1.3 (the owner: "tapping R switches from Combat style and fruit (if
 * any is eaten)"): with a fruit eaten, R swaps between the fighting style and the fruit.
 * <ul>
 *   <li>No fruit: R toggles combat mode.</li>
 *   <li>The Gomu fruit (Devil Fruit mode, {@code GearKeys}): R leaves combat mode for Devil Fruit mode and back ({@code
 *       GearKeys.switchMode}, the original's X key does the same). Devil Fruit mode had Gear 5 on R: it is on Ctrl + R there now, as
 *       Armament is in combat mode (Ctrl + R is the mode's own power).</li>
 *   <li>A kit fruit (the Pika fruit, binds on): R swaps M1 and the right click between the style (combat mode) and the kit ({@link
 *       PieceHooks#fruitKeysActive}); the kit's bound abilities keep their keys. (The original's "powers off" is not used: it turns
 *       Haki off too.)</li>
 * </ul>
 * A player who has not set sail with a style keeps R as the original had it. The presses are taken before the original's key handling
 * (a higher priority on the same tick, both use consumeClick), so it never sees the ones that were R's here. While combat mode is on,
 * the fighting style throws its moves ({@link StyleInput}), a relaxed stance may be held ({@link StanceLayer}) and a badge sits over
 * the hotbar.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class CombatMode {
    /** Ticks a switch into combat mode waits for the server to end Devil Fruit mode before deciding it was refused. */
    private static final int SWITCH_GRACE = 40;
    private static boolean on;
    private static long switchingUntil;

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
        switchingUntil = 0;
    }

    /** The player has set sail with a fighting style: R is HexPiece's. */
    static boolean styled() {
        PieceProfile self = PieceClient.self();
        return self.chosen() && self.style().choice();
    }

    /** Combat mode is there for a styled player outside Devil Fruit mode. */
    static boolean allowed() {
        return styled() && !GearClientState.devilFruitMode();
    }

    static long now(Minecraft minecraft) {
        return minecraft.f_91073_ == null ? 0L : minecraft.f_91073_.m_46467_();
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
        if (!styled()) {
            if (on) {
                set(false); // no style any more (a reset): R is the original's again
            }
            return;
        }
        if (on && GearClientState.devilFruitMode() && now(minecraft) > switchingUntil) {
            set(false); // Devil Fruit mode came on another way (the X key): the fruit has the keys
        }
        if (minecraft.f_91080_ != null) {
            return; // the original drains R under a screen
        }
        while (HakiKeys.ARMAMENT.m_90859_()) {
            if (Screen.m_96637_()) {
                power();
            } else {
                swap(minecraft);
            }
        }
    }

    /** Ctrl + R: the mode's own power, Gear 5 in Devil Fruit mode, Armament Haki otherwise. */
    static void power() {
        if (GearClientState.hasFruit && GearClientState.devilFruitMode()) {
            HakiNetwork.CHANNEL.sendToServer(new C2SGearAction(GearAction.GEAR_FIVE_TOGGLE));
        } else if (HakiClientState.enabled) {
            HakiKeys.send(HakiAction.ARMAMENT_TOGGLE);
        }
    }

    /** R: combat mode on or off, or, with a fruit eaten, from the style to the fruit and back. */
    static void swap(Minecraft minecraft) {
        boolean gear = GearClientState.hasFruit;
        if (!gear && !FruitClient.owned()) {
            set(!on);
            return;
        }
        if (on) {
            set(false);
            if (gear && !GearClientState.devilFruitMode()) {
                GearKeys.switchMode();
            }
        } else {
            if (gear && GearClientState.devilFruitMode()) {
                GearKeys.switchMode();
                switchingUntil = now(minecraft) + SWITCH_GRACE;
            }
            set(true);
        }
    }

    /** The badge over the hotbar's left end while combat mode is on: the style's colour and name, and its special keys. */
    static void draw(GuiGraphics graphics, int width, int height) {
        if (AbilityHud.replaces()) {
            return; // 2.2.2: the abilities panel on the left shows the style (and everything else)
        }
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
        // 2.2.9: a name of its own: Forge's event bus names the handler it generates from the package, "Overlay", the method and the
        // event, so two Overlay.register in one package shared one, and the game crashed on boot ("Overlay already registered")
        public static void registerCombatOverlay(RegisterGuiOverlaysEvent event) {
            event.registerBelowAll("hexpiece_combat", (gui, graphics, partialTick, width, height) -> draw(graphics, width, height));
        }
    }
}
