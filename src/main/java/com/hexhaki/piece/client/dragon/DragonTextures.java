package com.hexhaki.piece.client.dragon;

import com.hexhaki.piece.HexPiece;
import com.hexhaki.piece.dragon.DragonColours;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * 2.7.1: the dragon's texture in each of its colours (dragon/DragonColours): the owner's texture read once from the jar, recoloured
 * to a palette on a worker thread the first time a dragon of that colour appears (about a million pixels: never on the render
 * thread), then uploaded on the render thread as a texture of its own and kept. Until it is ready the dragon is drawn in the owner's
 * blue (it is still small then: the transformation grows it from the player's chest).
 */
final class DragonTextures {
    private static final int COUNT = DragonColours.PALETTES.length;
    private static final ResourceLocation[] READY = new ResourceLocation[COUNT];
    private static final boolean[] STARTED = new boolean[COUNT];
    private static final ConcurrentLinkedQueue<Object[]> DONE = new ConcurrentLinkedQueue<>();
    private static volatile int[] base;
    private static volatile int width;
    private static volatile int height;
    private static volatile boolean failed;

    private DragonTextures() {
    }

    /** The texture of colour {@code colour}: its own once made, the owner's until then. */
    static ResourceLocation of(int colour) {
        int palette = Math.floorMod(colour, COUNT);
        if (palette == 0 || failed) {
            return DragonRenderer.TEXTURE;
        }
        upload();
        if (READY[palette] != null) {
            return READY[palette];
        }
        if (!STARTED[palette]) {
            STARTED[palette] = true;
            Thread worker = new Thread(() -> make(palette), "HexPiece dragon colour " + palette);
            worker.setDaemon(true);
            worker.setPriority(Thread.MIN_PRIORITY);
            worker.start();
        }
        return DragonRenderer.TEXTURE;
    }

    /** On the worker: the owner's pixels (read once), recoloured. */
    static void make(int palette) {
        try {
            int[] own = pixels();
            int[] argb = own.clone();
            DragonColours.recolour(argb, width, height, DragonColours.PALETTES[palette]);
            DONE.add(new Object[] {palette, argb});
        } catch (Exception | LinkageError e) {
            failed = true;
            HexPiece.LOGGER.error("HexPiece: the dragon's colours could not be made", e);
        }
    }

    static synchronized int[] pixels() throws java.io.IOException {
        if (base == null) {
            try (InputStream in = DragonTextures.class.getResourceAsStream("/assets/hexhaki/textures/piece/dragon/seiryu.png")) {
                if (in == null) {
                    throw new java.io.IOException("the dragon's texture is missing");
                }
                NativeImage image = NativeImage.m_85058_(in);
                try {
                    int w = image.m_84982_();
                    int h = image.m_85084_();
                    int[] argb = new int[w * h];
                    for (int y = 0; y < h; y++) {
                        for (int x = 0; x < w; x++) {
                            argb[y * w + x] = swap(image.m_84985_(x, y));
                        }
                    }
                    width = w;
                    height = h;
                    base = argb;
                } finally {
                    image.close();
                }
            }
        }
        return base;
    }

    /** On the render thread: every finished colour into a texture of its own. */
    static void upload() {
        Object[] done;
        while ((done = DONE.poll()) != null) {
            int palette = (Integer) done[0];
            int[] argb = (int[]) done[1];
            NativeImage image = new NativeImage(width, height, false);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    image.m_84988_(x, y, swap(argb[y * width + x]));
                }
            }
            ResourceLocation location = ResourceLocation.m_214293_("hexhaki", "dragon/colour_" + palette);
            Minecraft.m_91087_().m_91097_().m_118495_(location, new DynamicTexture(image));
            READY[palette] = location;
        }
    }

    /** NativeImage's ABGR to ARGB and back (red and blue change places). */
    static int swap(int c) {
        return c & 0xFF00FF00 | c >> 16 & 0xFF | (c & 0xFF) << 16;
    }
}
