package com.hexhaki.piece.client;

import com.hexhaki.piece.client.ui.GuiCanvas;
import com.hexhaki.piece.network.C2SCreate;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.ui.CreationView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/** 2.1.1: the start menu ({@link CreationView}): male or female, pirate or marine, the fighting style; Set Sail sends the picks. */
public final class CreationScreen extends Screen {
    private final long openedAt = System.currentTimeMillis();
    private final CreationView.State state;

    public CreationScreen(boolean colourOnly) {
        super(Component.m_237113_("Set Sail"));
        this.state = colourOnly ? CreationView.State.colourOnly() : new CreationView.State();
    }

    void send() {
        PieceNetwork.toServer(new C2SCreate(this.state.gender.ordinal(), this.state.faction.ordinal(), this.state.style.ordinal(),
            this.state.colour == null ? 0 : this.state.colour.code()));
    }

    private CreationView.Layout layout() {
        return CreationView.Layout.of(this.f_96543_, this.f_96544_);
    }

    @Override
    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        GuiCanvas canvas = new GuiCanvas(graphics, minecraft.f_91062_, player == null ? null : player.m_108560_());
        CreationView.Layout layout = this.layout();
        CreationView.draw(canvas, layout, this.state, CreationView.at(layout, this.state, mouseX, mouseY),
            (System.currentTimeMillis() - this.openedAt) / 1000.0);
    }

    @Override
    public boolean m_6375_(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        String at = CreationView.at(this.layout(), this.state, mouseX, mouseY);
        if (at == null) {
            return false;
        }
        if (CreationView.click(this.state, at)) {
            this.send();
            Minecraft.m_91087_().m_91152_(null);
        }
        return true;
    }

    @Override
    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 257, 335 -> {
                if (CreationView.click(this.state, "next")) {
                    this.send();
                    Minecraft.m_91087_().m_91152_(null);
                }
                return true;
            }
            case 259 -> {
                CreationView.click(this.state, "back");
                return true;
            }
            case 256 -> {
                return true; // the menu is not skipped: setting sail comes first
            }
            default -> {
                return super.m_7933_(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public boolean m_7043_() {
        return false;
    }
}
