package com.hexhaki.piece.client.ui;

import com.hexhaki.piece.ui.UiCanvas;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** The game's GUI as a {@link UiCanvas} (ported from HexGhoul). {@code skin}: the viewing player's skin, for {@link #face}. */
public final class GuiCanvas implements UiCanvas {
    private final GuiGraphics graphics;
    private final Font font;
    private final ResourceLocation skin;

    public GuiCanvas(GuiGraphics graphics, Font font, ResourceLocation skin) {
        this.graphics = graphics;
        this.font = font;
        this.skin = skin;
    }

    @Override
    public void fill(int x0, int y0, int x1, int y1, int argb) {
        this.graphics.m_280509_(x0, y0, x1, y1, argb);
    }

    @Override
    public void gradient(int x0, int y0, int x1, int y1, int top, int bottom) {
        this.graphics.m_280024_(x0, y0, x1, y1, top, bottom);
    }

    @Override
    public void text(String text, int x, int y, int argb, boolean shadow) {
        this.graphics.m_280056_(this.font, text, x, y, argb, shadow);
    }

    @Override
    public int width(String text) {
        return this.font.m_92895_(text);
    }

    @Override
    public void face(int x, int y, int size) {
        if (this.skin != null && size > 0) {
            this.graphics.m_280411_(this.skin, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
            this.graphics.m_280411_(this.skin, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
        }
    }

    @Override
    public void batch(Runnable draws) {
        this.graphics.m_286007_(draws);
    }

    @Override
    public void push() {
        this.graphics.m_280168_().m_85836_();
    }

    @Override
    public void translate(float x, float y) {
        this.graphics.m_280168_().m_85837_(x, y, 0.0);
    }

    @Override
    public void scale(float factor) {
        this.graphics.m_280168_().m_85841_(factor, factor, 1.0F);
    }

    @Override
    public void pop() {
        PoseStack pose = this.graphics.m_280168_();
        pose.m_85849_();
    }

    @Override
    public void texture(String id, int x, int y, int w, int h, int u, int v, int uw, int vh, int texW, int texH) {
        int colon = id.indexOf(':');
        if (colon > 0) {
            this.graphics.m_280411_(ResourceLocation.m_214293_(id.substring(0, colon), id.substring(colon + 1)), x, y, w, h, u, v, uw, vh,
                texW, texH);
        }
    }
}
