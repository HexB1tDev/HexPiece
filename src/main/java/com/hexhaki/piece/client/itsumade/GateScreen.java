package com.hexhaki.piece.client.itsumade;

import com.hexhaki.piece.client.ui.GuiCanvas;
import com.hexhaki.piece.network.C2SGate;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CGates;
import com.hexhaki.piece.ui.GateView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 3.9.9: the Hellish Teleport menu in game (ui/GateView draws it; server/ItsuGates keeps the gates): a click on an empty gate binds it
 * where the player stands; on a bound one OPEN opens it (and the menu closes: the pentagram forms), RENAME types its new name into the
 * card (letters, digits, spaces and dashes; Enter keeps it, Esc does not), MOVE HERE binds it again where the player stands, CLEAR
 * empties it. It does not pause the game: the gates are called in the middle of a fight.
 */
public final class GateScreen extends Screen {
    private final long openedAt = System.currentTimeMillis();
    private final GateView.State state = new GateView.State();
    GateView.Data data;
    private long toldAt = System.currentTimeMillis();

    GateScreen(GateView.Data data) {
        super(Component.m_237113_("Hellish Teleport"));
        this.data = data;
    }

    /** S2CGates: the menu opened, or what it shows brought up to date. */
    public static void told(S2CGates m) {
        GateView.Data d = new GateView.Data();
        d.set = m.set().clone();
        d.names = m.names().clone();
        d.xs = m.xs().clone();
        d.ys = m.ys().clone();
        d.zs = m.zs().clone();
        d.worlds = m.worlds().clone();
        d.here = m.here();
        d.cooldown = m.cooldown();
        Minecraft minecraft = Minecraft.m_91087_();
        if (m.open()) {
            minecraft.m_91152_(new GateScreen(d));
        } else if (minecraft.f_91080_ instanceof GateScreen screen) {
            screen.data = d;
            screen.toldAt = System.currentTimeMillis();
        }
    }

    private double time() {
        return (System.currentTimeMillis() - this.openedAt) / 1000.0;
    }

    private GateView.Layout layout() {
        return GateView.Layout.of(this.f_96543_, this.f_96544_);
    }

    /** The cooldown as it stands now (counted down from when it was told). */
    private GateView.Data shown() {
        long gone = (System.currentTimeMillis() - this.toldAt) / 50L;
        this.data.cooldown = (int) Math.max(0L, this.data.cooldown - gone);
        this.toldAt += gone * 50L;
        return this.data;
    }

    @Override
    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.m_91087_();
        GuiCanvas canvas = new GuiCanvas(graphics, minecraft.f_91062_, null);
        GateView.Layout layout = this.layout();
        GateView.Data d = this.shown();
        GateView.draw(canvas, layout, d, this.state, GateView.at(canvas, layout, d, this.state, mouseX, mouseY), this.time());
    }

    @Override
    public boolean m_7043_() {
        return false;
    }

    static void send(int action, int slot, String text) {
        PieceNetwork.toServer(new C2SGate(action, slot, text));
    }

    @Override
    public boolean m_6375_(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        GuiCanvas canvas = new GuiCanvas(null, minecraft.f_91062_, null);
        String at = GateView.at(canvas, this.layout(), this.data, this.state, mouseX, mouseY);
        if (this.state.renaming >= 0 && (at == null || !at.equals("rename:" + this.state.renaming))) {
            this.keepName(); // a click elsewhere keeps what was typed
        }
        if (at == null) {
            return false;
        }
        if (at.equals("close")) {
            this.m_7379_();
            return true;
        }
        int slot = at.charAt(at.length() - 1) - '0';
        switch (at.substring(0, at.indexOf(':'))) {
            case "bind", "move" -> send(C2SGate.BIND, slot, "");
            case "clear" -> send(C2SGate.CLEAR, slot, "");
            case "rename" -> {
                this.state.renaming = slot;
                this.state.typed = this.data.names[slot];
            }
            case "open" -> {
                send(C2SGate.GO, slot, "");
                this.m_7379_();
            }
            default -> {
            }
        }
        return true;
    }

    private void keepName() {
        int slot = this.state.renaming;
        this.state.renaming = -1;
        if (slot >= 0) {
            this.data.names[slot] = this.state.typed.trim().isEmpty() ? this.data.names[slot] : this.state.typed.trim();
            send(C2SGate.RENAME, slot, this.state.typed);
        }
    }

    @Override
    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        if (this.state.renaming < 0) {
            return super.m_7933_(keyCode, scanCode, modifiers);
        }
        // typing the name: the keys themselves (GLFW codes), shift for capitals
        switch (keyCode) {
            case 256 -> this.state.renaming = -1; // Esc: not kept
            case 257, 335 -> this.keepName(); // Enter
            case 259 -> {
                if (!this.state.typed.isEmpty()) {
                    this.state.typed = this.state.typed.substring(0, this.state.typed.length() - 1);
                }
            }
            default -> {
                char ch = typed(keyCode, (modifiers & 1) != 0);
                if (ch != 0 && this.state.typed.length() < 20) {
                    this.state.typed += ch;
                }
            }
        }
        return true;
    }

    /** The character a key types (0: none of the ones a name takes). */
    static char typed(int keyCode, boolean shift) {
        if (keyCode >= 65 && keyCode <= 90) {
            return (char) (shift ? keyCode : keyCode + 32);
        }
        if (keyCode >= 48 && keyCode <= 57 && !shift) {
            return (char) keyCode;
        }
        if (keyCode >= 320 && keyCode <= 329) {
            return (char) ('0' + keyCode - 320); // the keypad's digits
        }
        if (keyCode == 32) {
            return ' ';
        }
        if (keyCode == 45) {
            return shift ? '_' : '-';
        }
        if (keyCode == 39 && !shift) {
            return '\'';
        }
        return 0;
    }
}
