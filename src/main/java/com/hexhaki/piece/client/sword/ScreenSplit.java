package com.hexhaki.piece.client.sword;

import com.hexhaki.HexHaki;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * 2.3.2 (the owner, of the finisher's line on its victim's view: "the bar should split the whole screen in 2"): when the body comes
 * apart, so does its view. The frame as drawn so far is copied (a framebuffer blit, as the game copies depth between its own targets),
 * and drawn again as two halves cut down the middle: they fly apart and drop, the left turning one way and the right the other, the
 * dark between them, each cut edge burning the line's red. {@link ScreenLine#split} is the motion (pure); {@link #draw} paints it.
 */
public final class ScreenSplit {
    static final ResourceLocation VIEW = HexHaki.id("piece_split_view");
    private static TextureTarget copy;
    private static boolean registered;

    private ScreenSplit() {
    }

    /** The colour of the copied frame, as a texture the GUI can blit. */
    static final class View extends AbstractTexture {
        @Override
        public void m_6704_(ResourceManager manager) {
            // nothing to load: the texture is the copy's colour attachment
        }

        @Override
        public int m_117963_() {
            return copy == null ? 0 : copy.m_83975_();
        }
    }

    /** The two halves of the view {@code age} ticks after the split, filling the screen ({@code width} x {@code height} GUI units). */
    static void draw(GuiGraphics graphics, int width, int height, double age, double fade) {
        Minecraft minecraft = Minecraft.m_91087_();
        RenderTarget main = minecraft.m_91385_();
        int w = main.f_83915_;
        int h = main.f_83916_;
        if (w <= 0 || h <= 0) {
            return;
        }
        if (copy == null) {
            copy = new TextureTarget(w, h, false, Minecraft.f_91002_);
        } else if (copy.f_83915_ != w || copy.f_83916_ != h) {
            copy.m_83941_(w, h, Minecraft.f_91002_);
        }
        if (!registered) {
            minecraft.m_91097_().m_118495_(VIEW, new View());
            registered = true;
        }
        // the frame so far into the copy (GL_READ_FRAMEBUFFER, GL_DRAW_FRAMEBUFFER, GL_COLOR_BUFFER_BIT, GL_NEAREST), then back
        GlStateManager._glBindFramebuffer(0x8CA8, main.f_83920_);
        GlStateManager._glBindFramebuffer(0x8CA9, copy.f_83920_);
        GlStateManager._glBlitFrameBuffer(0, 0, w, h, 0, 0, w, h, 0x4000, 0x2600);
        main.m_83947_(false);
        RenderSystem.disableDepthTest();
        graphics.m_280509_(0, 0, width, height, 0xFF050203);
        double[] m = ScreenLine.split(age);
        int half = width / 2;
        half(graphics, 0, half, height, 0, w / 2, w, h, m[0] * width, m[1] * height, m[2], half, fade, true);
        half(graphics, half, width - half, height, w / 2, w - w / 2, w, h, m[3] * width, m[4] * height, m[5], half, fade, false);
        RenderSystem.enableDepthTest();
    }

    /** One half: the screen strip from {@code x} {@code wide} across, from the copy's columns {@code u} {@code uw} wide; moved, turned. */
    static void half(GuiGraphics graphics, int x, int wide, int height, int u, int uw, int w, int h, double dx, double dy, double turn,
                     int cut, double fade, boolean left) {
        PoseStack pose = graphics.m_280168_();
        pose.m_85836_();
        // turned about the foot of its cut edge, as a half falling away from the other
        pose.m_252880_((float) (cut + dx), (float) (height + dy), 0.0F);
        pose.m_252781_(Axis.f_252403_.m_252977_((float) turn));
        pose.m_252880_((float) -cut, (float) -height, 0.0F);
        // the framebuffer's rows run bottom up: v from its height down to 0
        graphics.m_280411_(VIEW, x, 0, wide, height, u, h, uw, -h, w, h);
        // its cut edge burning the line's red, a glow into the dark
        int edge = left ? x + wide : x;
        int out = left ? 1 : -1;
        int red = com.hexhaki.piece.sword.Carcass.GLOW & 0xFFFFFF;
        int glow = Math.max(3, wide / 80);
        graphics.m_280509_(Math.min(edge, edge + out * glow * 3), -height / 4, Math.max(edge, edge + out * glow * 3), height + height / 4,
            ScreenLine.argb(0.22 * fade, red));
        graphics.m_280509_(Math.min(edge, edge + out * glow), -height / 4, Math.max(edge, edge + out * glow), height + height / 4,
            ScreenLine.argb(0.45 * fade, red));
        graphics.m_280509_(Math.min(edge, edge - out * 2), -height / 4, Math.max(edge, edge - out * 2), height + height / 4,
            ScreenLine.argb(fade, red));
        graphics.m_280509_(Math.min(edge, edge - out), -height / 4, Math.max(edge, edge - out), height + height / 4,
            ScreenLine.argb(0.8 * fade, 0xFFE6E6));
        pose.m_85849_();
    }
}
