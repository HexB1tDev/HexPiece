package com.hexhaki.piece.client;

import com.hexhaki.binds.client.AbilityBook;
import com.hexhaki.client.devilfruit.GearClientState;
import com.hexhaki.client.screen.HakiMasteryScreen;
import com.hexhaki.data.HakiRank;
import com.hexhaki.data.HakiType;
import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.FruitClient;
import com.hexhaki.piece.client.ui.GuiCanvas;
import com.hexhaki.piece.ui.LogbookView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * 2.1.1: the Captain's Log ({@link LogbookView}), the new mastery menu. The original's mastery key opens it instead of the Ability
 * Book ({@link MenuSwap}); its buttons open the Ability Book (to bind abilities) and the Haki techniques, as before.
 */
public final class LogbookScreen extends Screen {
    private final long openedAt = System.currentTimeMillis();
    private int tab;

    public LogbookScreen() {
        super(Component.m_237113_("Captain's Log"));
    }

    private LogbookView.Layout layout() {
        return LogbookView.Layout.of(this.f_96543_, this.f_96544_);
    }

    static String pretty(String constant) {
        String lower = constant.toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return lower.isEmpty() ? lower : Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private LogbookView.Data data() {
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        HakiType[] types = {HakiType.ARMAMENT, HakiType.OBSERVATION, HakiType.CONQUEROR};
        int[] haki = new int[3];
        String[] ranks = new String[3];
        for (int i = 0; i < 3; i++) {
            haki[i] = Math.max(0, Math.min(1000, HakiMasteryScreen.mastery(types[i])));
            ranks[i] = pretty(HakiRank.of(haki[i]).name());
        }
        String fruit = "";
        int fruitMastery = 0;
        Fruit kit = FruitClient.fruit();
        if (kit != null && kit != Fruit.NONE) {
            fruit = kit.display;
            fruitMastery = FruitClient.mastery();
        } else if (GearClientState.hasFruit) {
            fruit = "Hito Hito no Mi, Model Nika";
            fruitMastery = GearClientState.mastery;
        }
        String name = player == null ? "Captain" : player.m_36316_().getName();
        return new LogbookView.Data(name, PieceClient.self(), haki, ranks, fruit, fruitMastery, this.tab);
    }

    @Override
    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        GuiCanvas canvas = new GuiCanvas(graphics, minecraft.f_91062_, player == null ? null : player.m_108560_());
        LogbookView.Layout layout = this.layout();
        LogbookView.draw(canvas, layout, this.data(), LogbookView.at(layout, mouseX, mouseY), (System.currentTimeMillis() - this.openedAt) / 1000.0);
    }

    @Override
    public boolean m_6375_(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        String at = LogbookView.at(this.layout(), mouseX, mouseY);
        if (at == null) {
            return false;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (at.startsWith("tab:")) {
            this.tab = Integer.parseInt(at.substring(4));
        } else if (at.equals("book")) {
            MenuSwap.passOnce();
            minecraft.m_91152_(new AbilityBook());
        } else if (at.equals("haki")) {
            minecraft.m_91152_(new HakiMasteryScreen());
        } else if (at.equals("close")) {
            minecraft.m_91152_(null);
        }
        return true;
    }

    @Override
    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 258 || keyCode == 262) {
            this.tab = (this.tab + 1) % LogbookView.TABS.length;
            return true;
        }
        if (keyCode == 263) {
            this.tab = (this.tab + LogbookView.TABS.length - 1) % LogbookView.TABS.length;
            return true;
        }
        return super.m_7933_(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean m_7043_() {
        return false;
    }
}
