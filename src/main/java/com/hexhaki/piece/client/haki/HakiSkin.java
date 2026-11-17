package com.hexhaki.piece.client.haki;

import com.hexhaki.client.RemoteHakiStates;
import com.hexhaki.piece.haki.HandCoat;
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
 * while Gear's own skin is on (it blackens the arms itself) it is left as it is. If the skin cannot be read, the player keeps their
 * skin and the hands get the coat drawn over them instead ({@link RyouHands#coating}).
 */
public final class HakiSkin {
    /** One player's blackened copy: of which skin, for which arms, under which name, the texture. */
    private record Entry(ResourceLocation source, boolean slim, ResourceLocation location, DynamicTexture texture) {
    }

    private static final Map<UUID, Entry> CACHE = new HashMap<>();
    /** When a player whose copy could not be made may be tried again (nanoseconds). */
    private static final Map<UUID, Long> RETRY = new HashMap<>();
    private static Object world;

    private HakiSkin() {
    }

    /** The skin to draw {@code player} with: {@code skin}, or its copy with the hands blackened while their Armament is on. */
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
            if (!coated(player) || gear) {
                drop(id);
                return skin;
            }
            boolean slim = "slim".equals(player.m_108564_());
            Entry entry = CACHE.get(id);
            if (entry != null && entry.source().equals(skin) && entry.slim() == slim) {
                return entry.location();
            }
            long now = System.nanoTime();
            Long retry = RETRY.get(id);
            if (retry != null && now < retry) {
                return skin;
            }
            drop(id);
            entry = create(id, skin, slim, minecraft.m_91097_());
            if (entry == null) {
                RETRY.put(id, now + 1_000_000_000L);
                return skin;
            }
            CACHE.put(id, entry);
            RETRY.remove(id);
            return entry.location();
        } catch (RuntimeException failure) {
            RETRY.put(player.m_20148_(), System.nanoTime() + 5_000_000_000L);
            return skin;
        }
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

    /** The copy: the skin's pixels read back from its texture (as GearJacketSkin.create), the hands blackened, registered. */
    private static Entry create(UUID id, ResourceLocation skin, boolean slim, TextureManager textures) {
        AbstractTexture source = textures.m_118506_(skin);
        if (source == null) {
            return null;
        }
        int previous = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        NativeImage image;
        int width;
        int height;
        try {
            GlStateManager._bindTexture(source.m_117963_());
            width = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            height = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
            if (width < 64 || width % 64 != 0 || (height != width && height * 2 != width)) {
                return null;
            }
            image = new NativeImage(width, height, false);
            image.m_85045_(0, false);
        } finally {
            GlStateManager._bindTexture(previous);
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float amount = HandCoat.amount(x, y, width, height, slim);
                if (amount > 0.0F) {
                    image.m_84988_(x, y, HandCoat.coat(image.m_84985_(x, y), amount));
                }
            }
        }
        DynamicTexture texture = new DynamicTexture(image);
        ResourceLocation location = new ResourceLocation("hexhaki", "dynamic/haki_hands/" + id);
        textures.m_118495_(location, texture);
        return new Entry(skin, slim, location, texture);
    }

    private static void drop(UUID id) {
        Entry entry = CACHE.remove(id);
        if (entry != null) {
            Minecraft.m_91087_().m_91097_().m_118513_(entry.location());
        }
    }

    private static void clear() {
        for (UUID id : new java.util.ArrayList<>(CACHE.keySet())) {
            drop(id);
        }
        RETRY.clear();
    }
}
