package com.hexhaki.piece.client.haki;

import com.hexhaki.client.RemoteHakiStates;
import com.hexhaki.piece.haki.HandCoat;
import com.hexhaki.piece.haki.SkinCheck;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * 2.3.4 (the owner, of Ryou's hands: "it's not the actual skin blackening, look at Nika, how the skin turns white, why not use that for
 * haki?"): while a player's Armament (or Ryou) is on, the skin the game draws them with is a copy of their own with the hands
 * blackened ({@link HandCoat}), made the way the original makes Nika's white skin ({@code GearJacketSkin}: the skin's pixels read back
 * from its texture, recoloured into a {@link DynamicTexture}, registered beside it): the patch hands every skin the original's
 * {@code GearJacketSkin.resolve} returns to {@link #resolve}. The copy is made once per player and skin, freed when the Haki goes off;
 * 2.4.7: the black spreads over the hands pixel by pixel as the Haki comes on, the fist first (about a second), and draws back off
 * them the same way as it goes off ({@link HandCoat#at}; recomposed only when its progress moves a step);
 * while Gear's own skin is on (it blackens the arms itself) it is left as it is. If the skin cannot be read, the player keeps their
 * skin and the hands get the coat drawn over them instead ({@link RyouHands#coating}).
 */
public final class HakiSkin {
    /**
     * One player's blackened copy: of which skin, for which arms, under which name, the texture, the skin's own pixels (kept to
     * recolour from), the hand's pixels (index into the image, how black each ends up, when each starts turning) and the progress step
     * it was last composed at.
     */
    private static final class Entry {
        final ResourceLocation source;
        final boolean slim;
        final ResourceLocation location;
        final DynamicTexture texture;
        final NativeImage base;
        final int[] pixels;
        final float[] amounts;
        final float[] reveals;
        /** 2.5.3: the skin's texture as it was read, and when to read it again (nanoseconds) and how many times it was. */
        final AbstractTexture origin;
        long nextCheck;
        int checks;
        int step = -1;

        Entry(ResourceLocation source, boolean slim, ResourceLocation location, DynamicTexture texture, NativeImage base, int[] pixels,
              float[] amounts, float[] reveals, AbstractTexture origin) {
            this.origin = origin;
            this.nextCheck = System.nanoTime() + CHECKS[0];
            this.source = source;
            this.slim = slim;
            this.location = location;
            this.texture = texture;
            this.base = base;
            this.pixels = pixels;
            this.amounts = amounts;
            this.reveals = reveals;
        }
    }

    /** 2.4.7: a player's coat coming on or going off: which, since when (nanoseconds), from what progress. */
    private record Fade(boolean on, long since, float from) {
        float progress(long now) {
            float spent = (now - this.since) / (float) (this.on ? ON_NANOS : OFF_NANOS);
            return Math.max(0.0F, Math.min(1.0F, this.on ? this.from + spent : this.from - spent));
        }
    }

    /** 2.4.7: how long the black takes to spread over the hands, and to draw back off them (nanoseconds). */
    static final long ON_NANOS = 1_000_000_000L;
    static final long OFF_NANOS = 600_000_000L;
    /** The coat is recomposed at most this many times on its way (the original's Gear skin: 96). */
    static final int STEPS = 48;

    private static final Map<UUID, Entry> CACHE = new HashMap<>();
    private static final Map<UUID, Fade> FADES = new HashMap<>();
    /** When a player whose copy could not be made may be tried again (nanoseconds). */
    private static final Map<UUID, Long> RETRY = new HashMap<>();
    /** 2.5.3: how many times in a row a player's copy could not be made (after a few, it is tried less often). */
    private static final Map<UUID, Integer> FAILS = new HashMap<>();
    /**
     * 2.5.3: when a copy is read again after it is made (nanoseconds after the last check): the skin's texture may be uploaded again
     * after the first read (the downloaded skin arriving over the default one at world start), and the copy's own upload may not have
     * taken; then every minute.
     */
    static final long[] CHECKS = {1_000_000_000L, 2_000_000_000L, 5_000_000_000L, 12_000_000_000L, 60_000_000_000L};
    private static Object world;

    private HakiSkin() {
    }

    /** The skin to draw {@code player} with: {@code skin}, or its copy with the hands blackening, black, or clearing again. */
    public static ResourceLocation resolve(AbstractClientPlayer player, ResourceLocation skin) {
        try {
            if (skin == null || !RenderSystem.isOnRenderThread()) {
                return skin;
            }
            Minecraft minecraft = Minecraft.m_91087_();
            if (minecraft.f_91073_ == null) {
                return skin;
            }
            if (world != minecraft.f_91073_) {
                clear();
                world = minecraft.f_91073_;
            }
            UUID id = player.m_20148_();
            boolean gear = "hexhaki".equals(skin.m_135827_()) && skin.m_135815_().startsWith("dynamic/");
            long now = System.nanoTime();
            float progress = progress(id, coated(player) && !gear, now);
            if (progress <= 0.0F || gear) {
                drop(id);
                return skin;
            }
            boolean slim = "slim".equals(player.m_108564_());
            Entry entry = CACHE.get(id);
            if (entry != null && now >= entry.nextCheck && !recheck(entry, minecraft.m_91097_(), now)) {
                drop(id); // the skin's texture changed under the copy: made again from it
                entry = null;
            }
            if (entry == null || !entry.source.equals(skin) || entry.slim != slim) {
                Long retry = RETRY.get(id);
                if (retry != null && now < retry) {
                    return skin;
                }
                drop(id);
                entry = create(id, skin, slim, minecraft.m_91097_());
                if (entry == null) {
                    // 2.5.3: the player keeps their own skin meanwhile (the coat is drawn over the hands); tried every second, then
                    // every ten after a few misses
                    int fails = FAILS.merge(id, 1, Integer::sum);
                    RETRY.put(id, now + (fails < 6 ? 1_000_000_000L : 10_000_000_000L));
                    return skin;
                }
                CACHE.put(id, entry);
                RETRY.remove(id);
                FAILS.remove(id);
            }
            int step = Math.round(progress * STEPS);
            if (step != entry.step) {
                compose(entry, step / (float) STEPS);
                entry.step = step;
            }
            return entry.location;
        } catch (RuntimeException failure) {
            RETRY.put(player.m_20148_(), System.nanoTime() + 5_000_000_000L);
            return skin;
        }
    }

    /** 2.4.7: how far {@code id}'s coat has spread (0..1), its Haki {@code on} or not, the fade turned round where it stood. */
    static float progress(UUID id, boolean on, long now) {
        Fade fade = FADES.get(id);
        if (fade == null) {
            if (!on) {
                return 0.0F;
            }
            fade = new Fade(true, now, 0.0F);
            FADES.put(id, fade);
        } else if (fade.on() != on) {
            fade = new Fade(on, now, fade.progress(now));
            FADES.put(id, fade);
        }
        float progress = fade.progress(now);
        if (!on && progress <= 0.0F) {
            FADES.remove(id);
        }
        return progress;
    }

    /** {@code player}'s Armament (or Ryou) is on, as the original's hand layer reads it. */
    public static boolean coated(AbstractClientPlayer player) {
        RemoteHakiStates.State state = RemoteHakiStates.get(player.m_19879_());
        return state != null && (state.armament() || state.ryuo());
    }

    /** True while {@code player} is drawn with a blackened copy of their skin. */
    public static boolean blackened(AbstractClientPlayer player) {
        return CACHE.containsKey(player.m_20148_());
    }

    /** The copy: the skin's pixels read back from its texture (as GearJacketSkin.create), the hand's pixels found, registered. */
    private static Entry create(UUID id, ResourceLocation skin, boolean slim, TextureManager textures) {
        AbstractTexture source = textures.m_118506_(skin);
        if (source == null) {
            return null;
        }
        NativeImage base = read(source);
        if (base == null) {
            return null;
        }
        int width = base.m_84982_();
        int height = base.m_85084_();
        java.util.List<int[]> hand = new java.util.ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (HandCoat.amount(x, y, width, height, slim) > 0.0F) {
                    hand.add(new int[] {x, y});
                }
            }
        }
        int[] pixels = new int[hand.size()];
        float[] amounts = new float[hand.size()];
        float[] reveals = new float[hand.size()];
        for (int i = 0; i < pixels.length; i++) {
            int x = hand.get(i)[0];
            int y = hand.get(i)[1];
            pixels[i] = y * width + x;
            amounts[i] = HandCoat.amount(x, y, width, height, slim);
            reveals[i] = HandCoat.reveal(x, y, width, height, slim);
        }
        NativeImage copy = new NativeImage(width, height, false);
        copy.m_85054_(base);
        DynamicTexture texture = new DynamicTexture(copy);
        ResourceLocation location = new ResourceLocation("hexhaki", "dynamic/haki_hands/" + id);
        textures.m_118495_(location, texture);
        return new Entry(skin, slim, location, texture, base, pixels, amounts, reveals, source);
    }

    /**
     * 2.5.3: the skin's pixels read back from its texture, or null when they cannot be (no texture yet, a size no skin has, or a
     * readback that is not a real skin: {@link SkinCheck#real}: at world start, or through the owner's OpenGL ES layer, it can come back
     * empty, and an empty copy drew the player invisible).
     */
    static NativeImage read(AbstractTexture source) {
        int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        try {
            GlStateManager._bindTexture(source.m_117963_());
            int width = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            int height = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
            if (width < 64 || width > 1024 || width % 64 != 0 || (height != width && height * 2 != width)) {
                return null;
            }
            NativeImage image = new NativeImage(width, height, false);
            try {
                image.m_85045_(0, false);
            } catch (RuntimeException failure) {
                image.close();
                return null;
            }
            if (!SkinCheck.real(image::m_84985_, width, height)) {
                image.close();
                return null;
            }
            return image;
        } finally {
            GlStateManager._bindTexture(previous);
        }
    }

    /**
     * 2.5.3: a copy read again ({@link #CHECKS}): false when the skin's texture is another object or holds other pixels now (made again);
     * a readback that fails keeps the copy as it is. Either way the copy is uploaded again (in case its first upload did not take).
     */
    private static boolean recheck(Entry entry, TextureManager textures, long now) {
        entry.nextCheck = now + CHECKS[Math.min(entry.checks + 1, CHECKS.length - 1)];
        entry.checks++;
        AbstractTexture source = textures.m_118506_(entry.source);
        if (source == null) {
            return true;
        }
        if (source != entry.origin) {
            return false;
        }
        NativeImage fresh = read(source);
        if (fresh != null) {
            try {
                if (fresh.m_84982_() != entry.base.m_84982_() || fresh.m_85084_() != entry.base.m_85084_()
                    || !SkinCheck.same(fresh::m_84985_, entry.base::m_84985_, fresh.m_84982_(), fresh.m_85084_())) {
                    return false;
                }
            } finally {
                fresh.close();
            }
        }
        entry.texture.m_117985_();
        return true;
    }

    /**
     * 2.5.3: the original's Gear 5 skin ({@code GearJacketSkin.create}, PiecePatch VIRTUAL_REDIRECTS) reads the skin back through here:
     * the same readback, then the same check; a readback that is not a real skin throws, so the original closes its copy and tries
     * again later (its own RETRY) instead of keeping an empty one.
     */
    public static void readBack(NativeImage image, int level, boolean opaque) {
        image.m_85045_(level, opaque);
        if (!SkinCheck.real(image::m_84985_, image.m_84982_(), image.m_85084_())) {
            throw new IllegalStateException("the skin read back empty");
        }
    }

    /** 2.4.7: the hand's pixels recoloured for {@code progress}: each as black as it has turned by then ({@link HandCoat#at}). */
    private static void compose(Entry entry, float progress) {
        NativeImage image = entry.texture.m_117991_();
        int width = entry.base.m_84982_();
        for (int i = 0; i < entry.pixels.length; i++) {
            int x = entry.pixels[i] % width;
            int y = entry.pixels[i] / width;
            float t = Math.max(0.0F, Math.min(1.0F, (progress - entry.reveals[i]) / HandCoat.SPREAD));
            float amount = entry.amounts[i] * t * t * (3.0F - 2.0F * t);
            int own = entry.base.m_84985_(x, y);
            // 2.5.3: the black's gloss (a few near-white pixels on the knuckles) set as it finishes
            image.m_84988_(x, y, amount > 0.0F ? HandCoat.finish(own, x, y, width, entry.base.m_85084_(), entry.slim, amount) : own);
        }
        entry.texture.m_117985_();
    }

    private static void drop(UUID id) {
        Entry entry = CACHE.remove(id);
        if (entry != null) {
            entry.base.close();
            Minecraft.m_91087_().m_91097_().m_118513_(entry.location);
        }
    }

    private static void clear() {
        for (UUID id : new java.util.ArrayList<>(CACHE.keySet())) {
            drop(id);
        }
        RETRY.clear();
        FAILS.clear();
        FADES.clear();
    }
}
