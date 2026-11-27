package com.hexhaki.piece.client;

import com.hexhaki.client.HakiClientState;
import com.hexhaki.client.HakiKeys;
import com.hexhaki.client.devilfruit.GearClientState;
import com.hexhaki.client.devilfruit.GearKeys;
import com.hexhaki.fruit.client.FruitClient;
import com.hexhaki.network.HakiAction;
import com.hexhaki.network.HakiNetwork;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.network.C2SCombat;
import com.hexhaki.piece.network.C2SPowers;
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
 *       GearKeys.switchMode}, the original's X key does the same). Devil Fruit mode had Gear 5 on R: Ctrl + R until 3.2.2, Z since 3.2.3
 *       (client/gear/NikaKeys); Ctrl + R is Armament in every mode.</li>
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
    /**
     * 3.5.9 (the owner: "HOLDING R will now fully disable all powers and abilities (but fruit passives)"): R held this long (ticks)
     * turns every power off, or back on; a shorter press is the tap it always was, acted on when R is let go.
     */
    public static final int HOLD = 12;
    private static boolean on;
    private static long switchingUntil;
    private static long pressedAt = -1;
    private static boolean pressCtrl;
    private static boolean fired;

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
        pressedAt = -1;
        fired = false;
    }

    /**
     * 3.7.7: one's own player in a Zoan form (the dragon, the phoenix: from the change to the way back; Gear 5): the fruit's mode only.
     */
    static boolean zoan() {
        return com.hexhaki.piece.client.dragon.DragonClient.selfOut() || com.hexhaki.piece.client.phoenix.PhoenixFx.selfOut() || GearClientState.gearFive;
    }

    /** 3.5.9: is every power off (the original's bit, server/Powers)? */
    static boolean powersOff() {
        return com.hexhaki.binds.client.BindsClient.off();
    }

    /** 3.5.9: how far R has been held toward the switch (0 to 1), or -1 when it is not held. */
    static double holding(float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (pressedAt < 0 || fired || minecraft.f_91073_ == null) {
            return -1.0;
        }
        return Math.min(1.0, (now(minecraft) - pressedAt + partial) / HOLD);
    }

    /** The player has set sail with a fighting style that has moves (3.5.7: Iron Fist has none yet): R is HexPiece's. */
    static boolean styled() {
        PieceProfile self = PieceClient.self();
        return self.chosen() && self.style().choice() && self.style().armed();
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
        if (on && powersOff()) {
            set(false); // 3.5.9: every power off: no combat mode either
        }
        if (on && zoan()) {
            set(false); // 3.7.7: transformed: only the fruit
        }
        if (minecraft.f_91080_ != null) {
            pressedAt = -1; // the original drains R under a screen
            return;
        }
        // 3.5.9: a tap of R (or Ctrl + R) acts when it is let go; held HOLD ticks it switches every power off or on instead
        boolean down = HakiKeys.ARMAMENT.m_90857_();
        int clicks = 0;
        while (HakiKeys.ARMAMENT.m_90859_()) {
            clicks++;
        }
        long now = now(minecraft);
        if (down && pressedAt < 0) {
            pressedAt = now;
            pressCtrl = Screen.m_96637_();
            fired = false;
        }
        if (down && !fired && now - pressedAt >= HOLD) {
            fired = true;
            boolean off = !powersOff();
            PieceNetwork.toServer(new C2SPowers(off));
            if (off) {
                set(false);
            }
        }
        if (!down && pressedAt >= 0) {
            if (!fired) {
                tap(minecraft, pressCtrl);
            }
            pressedAt = -1;
        } else if (!down && clicks > 0) {
            tap(minecraft, Screen.m_96637_()); // pressed and let go within one tick
        }
    }

    /** R tapped (Ctrl: Armament), as R always was; nothing but a reminder while every power is off. */
    static void tap(Minecraft minecraft, boolean ctrl) {
        if (com.hexhaki.piece.client.dragon.DragonClient.selfOut()) {
            return; // 2.7.3: the dragon keeps fruit mode and has no Haki (R and Ctrl + R do nothing)
        }
        if (!ctrl && zoan()) {
            // 3.7.7 (the owner: "In no ZOAN transformed state can you switch to Style or any other mode aside from fruit"): R does not
            // leave the fruit while transformed (Ctrl + R, Armament Haki, is not a mode: it stays)
            minecraft.f_91074_.m_5661_(net.minecraft.network.chat.Component.m_237113_(com.hexhaki.piece.server.Transformed.FRUIT_ONLY), true);
            return;
        }
        if (powersOff()) {
            minecraft.f_91074_.m_5661_(net.minecraft.network.chat.Component.m_237113_("Powers off: hold "
                + AbilityHud.name(HakiKeys.ARMAMENT) + " to turn them on"), true);
            return;
        }
        if (ctrl) {
            power();
        } else {
            swap(minecraft);
        }
    }

    /**
     * Ctrl + R: Armament Haki. (Until 3.2.2 it was Gear 5 in the Gomu fruit's Devil Fruit mode; 3.2.3 put Gear 5 on Z with the fruit's
     * other moves, client/gear/NikaKeys, so Ctrl + R is Armament there too: the Haki that turns its moves into their Dawn versions.)
     */
    static void power() {
        if (HakiClientState.enabled) {
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

    /**
     * 3.5.9: over the hotbar's middle: while R is held, a bar filling toward the switch ("Powers off" or "Powers on" when it fills).
     * 3.6.3: nothing else while every power is off (the whole HUD is gone until they are on again).
     */
    static void powers(GuiGraphics graphics, int width, int height, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ == null || minecraft.f_91066_ == null || minecraft.f_91080_ != null) {
            return;
        }
        Font font = minecraft.f_91062_;
        double fill = holding(partial);
        boolean off = powersOff();
        if (fill < 0) {
            // 3.6.3 (the owner: "when powers are turned off, the hud on the screen is to dissappear until the power is once again
            // activated"): no badge while off, as no panel and no energy bar: only the fill while R is held toward the switch
            return;
        }
        String key = AbilityHud.name(HakiKeys.ARMAMENT);
        String text = off ? "Hold " + key + ": powers on" : "Hold " + key + ": powers off";
        int w = font.m_92895_(text) + 16;
        int x = width / 2 - w / 2;
        int y = height - 74;
        graphics.m_280509_(x, y, x + w, y + 16, 0xB0101014);
        graphics.m_280509_(x, y + 14, x + w, y + 16, 0xFF2A2A30);
        graphics.m_280509_(x, y + 14, x + (int) Math.round(w * fill), y + 16, off ? 0xFF8CE07A : 0xFFE05050);
        graphics.m_280056_(font, text, x + 8, y + 4, 0xFFF4E8D0, true);
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
        String text = style.display() + "  [" + AbilityHud.name(PieceKeys.SPECIAL_A) + "] [" + AbilityHud.name(PieceKeys.SPECIAL_B) + "]";
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
            // 3.5.9: R held toward the powers switch, and every power off
            event.registerAboveAll("hexpiece_powers", (gui, graphics, partialTick, width, height) -> powers(graphics, width, height, partialTick));
        }
    }
}
