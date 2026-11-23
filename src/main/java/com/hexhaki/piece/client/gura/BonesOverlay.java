package com.hexhaki.piece.client.gura;

import com.hexhaki.piece.network.S2CUoFx;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.4.5 (the owner: "whenever the Gura Gura No Mi hits the ultimate move N, this gif I sent fills the screen (in a way that looks good)
 * and plays till the end only once"; asked: on the screen of the player who is hit, "X-ray overlay"): the owner's GIF (art/broken-bones.gif,
 * laid out by tools/bones_texture.py as textures/gui/broken_bones.png, its black made see-through) played once over one's own screen
 * when the Gura ultimate strikes (server/BrokenBones.XRAY, sent to the one struck only): the view dimmed behind it as an X-ray, the
 * skeleton big in the middle, each of its {@link #FRAMES} frames {@link #FRAME_MS} ms as the GIF has them, a quick fade in, a fade out
 * after its last frame. Drawn above every other overlay (the game's own GUI: a flat blit, no framebuffer copy).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class BonesOverlay {
    static final ResourceLocation SHEET = new ResourceLocation("hexhaki", "textures/gui/broken_bones.png");
    /** The GIF: its frames, each one's time, their size on the sheet, the sheet's layout. */
    public static final int FRAMES = 12;
    public static final long FRAME_MS = 240L;
    static final int FRAME_W = 356;
    static final int FRAME_H = 358;
    static final int COLUMNS = 4;
    static final int SHEET_W = FRAME_W * COLUMNS;
    static final int SHEET_H = FRAME_H * 3;
    /** The fade in, the fade out after the last frame (ms), how dark the view goes behind it, how tall the skeleton is on the screen. */
    public static final long FADE_IN = 150L;
    public static final long FADE_OUT = 350L;
    static final double DIM = 0.62;
    static final double TALL = 0.86;

    /** When it began (System.nanoTime), or -1. */
    static long start = -1L;

    private BonesOverlay() {
    }

    /** From client/dragon/UoFx: one's own bones broken by the Gura ultimate. */
    public static void accept(S2CUoFx m) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ != null && m.id() == minecraft.f_91074_.m_19879_()) {
            start = System.nanoTime();
        }
    }

    /** The frame shown {@code ms} after it began (the last held through the fade out), and how much of it shows (0 to 1); -1: over. */
    public static int frame(long ms) {
        if (ms < 0 || ms > FRAMES * FRAME_MS + FADE_OUT) {
            return -1;
        }
        return (int) Math.min(FRAMES - 1, ms / FRAME_MS);
    }

    public static double alpha(long ms) {
        if (ms < 0 || ms > FRAMES * FRAME_MS + FADE_OUT) {
            return 0.0;
        }
        if (ms < FADE_IN) {
            return ms / (double) FADE_IN;
        }
        long end = FRAMES * FRAME_MS;
        return ms <= end ? 1.0 : 1.0 - (ms - end) / (double) FADE_OUT;
    }

    static void draw(GuiGraphics graphics, int width, int height) {
        if (start < 0) {
            return;
        }
        long ms = (System.nanoTime() - start) / 1_000_000L;
        int frame = frame(ms);
        if (frame < 0) {
            start = -1;
            return;
        }
        double a = Math.max(0.0, Math.min(1.0, alpha(ms)));
        // the view dimmed behind the X-ray
        int dim = (int) Math.round(255 * DIM * a);
        graphics.m_280509_(0, 0, width, height, dim << 24 | 0x05030A);
        // the skeleton, big in the middle
        int h = (int) Math.round(height * TALL);
        int w = (int) Math.round(h * FRAME_W / (double) FRAME_H);
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float) a);
        graphics.m_280411_(SHEET, x, y, w, h, (frame % COLUMNS) * FRAME_W, (frame / COLUMNS) * FRAME_H, FRAME_W, FRAME_H, SHEET_W, SHEET_H);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        start = -1;
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Overlay {
        private Overlay() {
        }

        @SubscribeEvent
        // a handler name of its own (Forge names its generated handler from the package, the class, the method and the event)
        public static void registerBonesOverlay(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("hexpiece_broken_bones", (gui, graphics, partialTick, width, height) -> draw(graphics, width, height));
        }
    }
}
