package com.hexhaki.piece.client;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.FruitClient;
import com.hexhaki.piece.client.ui.GuiCanvas;
import com.hexhaki.piece.network.S2CEnergy;
import com.hexhaki.piece.ui.EnergyBarView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.2.0 (the owner: "an energy bar for the fruits, appearing over the food bar with a custom good-looking bar"; asked, "Moves spend it"):
 * one's own fruit energy (server/FruitEnergy, told by S2CEnergy) drawn over the food bar (ui/EnergyBarView) while one has a fruit with
 * moves of HexPiece's, in survival or adventure (where the food bar is); up a row while air bubbles show. The bar eases to the energy;
 * what a move just cost stays white a moment, then drains; a refused move flashes it.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class EnergyBar {
    private static double energy = 100.0;
    private static double shown = 100.0;
    private static double chip = 100.0;
    private static long chipAt;
    private static long deniedAt = -1000;
    private static long clock;

    private EnergyBar() {
    }

    public static void accept(S2CEnergy message) {
        double value = message.tenths() / 10.0;
        if (value < energy - 0.5) {
            chip = Math.max(chip, shown);
            chipAt = clock;
        }
        energy = value;
        if (message.denied()) {
            deniedAt = clock;
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clock++;
        shown += (energy - shown) * 0.35;
        if (clock - chipAt > 10) {
            chip += (shown - chip) * 0.18;
        }
        if (chip < shown) {
            chip = shown;
        }
    }

    /** Whether the bar shows: one's own fruit is one of HexPiece's kits (not the Nika fruit: its moves are the original's own). */
    static boolean shows(LocalPlayer player) {
        if (player.m_7500_() || player.m_5833_()) {
            return false;
        }
        Fruit fruit = FruitClient.fruit(player.m_19879_());
        return fruit != null && fruit != Fruit.NONE;
    }

    static void draw(GuiGraphics graphics, int width, int height, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        if (player == null || !shows(player)) {
            return;
        }
        Fruit fruit = FruitClient.fruit(player.m_19879_());
        int shift = player.m_20069_() ? 10 : 0;
        GuiCanvas canvas = new GuiCanvas(graphics, minecraft.f_91062_, null);
        double time = clock + partial;
        double denied = Math.max(0.0, 1.0 - (time - deniedAt) / 12.0);
        EnergyBarView.draw(canvas, width / 2 + 91 - EnergyBarView.EMBLEM / 2, height - 49 - shift, shown / 100.0, chip / 100.0, fruit.colour & 0xFFFFFF,
            time, denied);
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Overlay {
        private Overlay() {
        }

        /** Its own handler name (Forge names the generated handler from the class, the method and the event: never two alike). */
        @SubscribeEvent
        public static void registerFruitEnergyOverlay(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("hexpiece_fruit_energy", (gui, graphics, partialTick, width, height) -> draw(graphics, width, height, partialTick));
        }
    }
}
