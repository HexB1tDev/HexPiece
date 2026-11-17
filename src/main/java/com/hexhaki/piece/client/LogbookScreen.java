package com.hexhaki.piece.client;

import com.hexhaki.binds.client.BindsClient;
import com.hexhaki.client.HakiKeys;
import com.hexhaki.network.HakiNetwork;
import com.hexhaki.network.msg.C2SGearUpgrade;
import com.hexhaki.piece.client.ui.GuiCanvas;
import com.hexhaki.piece.ui.LogbookView;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * 2.1.1: the Captain's Log ({@link LogbookView}). 2.2.4: the one menu, everywhere the original opened its Ability Book, its Haki
 * Techniques or its Gear mastery screen ({@link MenuSwap}): Overview, Style &amp; Haki, Devil Fruit. A line picked on the left reads on
 * the right; a quick power picked goes onto a quick slot by a click on the slot or a press of its key ({@code BindsClient.bind}, as the
 * Ability Book did); Reset Slots puts the original's layout back ({@code BindsClient.reset}); the Nika fruit's upgrades are bought and
 * given back with the original's {@code C2SGearUpgrade}. Tab and the arrow keys turn the pages, up and down move the pick, the wheel
 * scrolls, the menu key closes it.
 */
public final class LogbookScreen extends Screen {
    private final long openedAt = System.currentTimeMillis();
    private final LogbookView.State state;
    private LogbookView.Data data;
    private long dataAt;

    public LogbookScreen() {
        this(LogbookView.OVERVIEW);
    }

    public LogbookScreen(int tab) {
        super(Component.m_237113_("Captain's Log"));
        this.state = new LogbookView.State(tab);
    }

    private LogbookView.Layout layout() {
        return LogbookView.Layout.of(this.f_96543_, this.f_96544_);
    }

    private double time() {
        return (System.currentTimeMillis() - this.openedAt) / 1000.0;
    }

    /** The log's contents, read again four times a second (the server's syncs land while it is open) or right after an action. */
    private LogbookView.Data data() {
        long now = System.currentTimeMillis();
        if (this.data == null || now - this.dataAt > 250) {
            this.data = LogbookData.build();
            this.dataAt = now;
        }
        return this.data;
    }

    private void stale() {
        this.data = null;
    }

    private GuiCanvas canvas(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        return new GuiCanvas(graphics, minecraft.f_91062_, player == null ? null : player.m_108560_());
    }

    @Override
    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        GuiCanvas canvas = this.canvas(graphics);
        LogbookView.Layout layout = this.layout();
        LogbookView.Data data = this.data();
        LogbookView.draw(canvas, layout, data, this.state, LogbookView.at(canvas, layout, data, this.state, mouseX, mouseY), this.time());
    }

    static void click() {
        Minecraft.m_91087_().m_91106_().m_120367_(SimpleSoundInstance.m_263171_(SoundEvents.f_12490_, 1.0F));
    }

    private void tab(int tab) {
        this.state.tab = (tab + LogbookView.TABS.length) % LogbookView.TABS.length;
        this.state.scroll = 0;
        this.state.picked = 0;
    }

    private void bind(int slot) {
        int ability = LogbookView.bindable(this.data(), this.state);
        if (ability >= 0) {
            BindsClient.bind(slot, ability);
            this.state.flashed = slot;
            this.state.flashedAt = this.time();
            this.stale();
            click();
        }
    }

    @Override
    public boolean m_6375_(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        GuiCanvas canvas = new GuiCanvas(null, minecraft.f_91062_, null);
        LogbookView.Data data = this.data();
        String at = LogbookView.at(canvas, this.layout(), data, this.state, mouseX, mouseY);
        if (at == null) {
            return false;
        }
        if (at.startsWith("tab:")) {
            this.tab(Integer.parseInt(at.substring(4)));
        } else if (at.startsWith("row:")) {
            this.state.picked = Integer.parseInt(at.substring(4));
        } else if (at.startsWith("slot:")) {
            this.bind(Integer.parseInt(at.substring(5)));
            return true;
        } else if (at.equals("buy")) {
            LogbookView.Entry entry = LogbookView.pickedEntry(data, this.state);
            if (entry == null || entry.upgrade() < 0 || entry.rank() >= entry.maxRank() || data.points() < entry.cost()) {
                return true;
            }
            HakiNetwork.CHANNEL.sendToServer(new C2SGearUpgrade(entry.upgrade(), false));
            this.stale();
        } else if (at.equals("respec")) {
            HakiNetwork.CHANNEL.sendToServer(new C2SGearUpgrade(0, true));
            this.stale();
        } else if (at.equals("reset")) {
            BindsClient.reset();
            this.stale();
        } else if (at.equals("close")) {
            minecraft.m_91152_(null);
        }
        click();
        return true;
    }

    @Override
    public boolean m_6050_(double mouseX, double mouseY, double delta) {
        if (this.state.tab == LogbookView.OVERVIEW || delta == 0) {
            return false;
        }
        List<LogbookView.Row> rows = LogbookView.rows(this.data().page(this.state.tab));
        int max = LogbookView.maxScroll(this.layout(), rows);
        this.state.scroll = Math.max(0, Math.min(max, this.state.scroll - (int) Math.signum(delta) * 2));
        return true;
    }

    @Override
    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        // a quick power picked: a quick slot's own key puts it there (as the Ability Book did)
        if (LogbookView.bindable(this.data(), this.state) >= 0) {
            KeyMapping[] keys = BindsClient.keys();
            for (int slot = 0; slot < keys.length && slot < 9; slot++) {
                if (keys[slot].m_90832_(keyCode, scanCode)) {
                    this.bind(slot);
                    return true;
                }
            }
        }
        if (HakiKeys.MASTERY.m_90832_(keyCode, scanCode)) {
            this.m_7379_();
            return true;
        }
        switch (keyCode) {
            case 258, 262 -> this.tab(this.state.tab + 1); // tab, right
            case 263 -> this.tab(this.state.tab - 1); // left
            case 264, 265 -> { // down, up
                List<LogbookView.Row> rows = LogbookView.rows(this.data().page(this.state.tab));
                if (rows.isEmpty()) {
                    return true;
                }
                this.state.picked = Math.max(0, Math.min(rows.size() - 1, this.state.picked + (keyCode == 264 ? 1 : -1)));
                LogbookView.reveal(this.layout(), rows, this.state);
            }
            default -> {
                return super.m_7933_(keyCode, scanCode, modifiers);
            }
        }
        return true;
    }

    @Override
    public boolean m_7043_() {
        return false;
    }
}
