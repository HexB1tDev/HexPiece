// Compile-only API stub (Minecraft 1.20.1 / Forge 47). Never packaged; see verify/api-allowlist.txt.
package net.minecraft.client.renderer;

/** 2.4.7: a post-processing chain (the game's own glowing outline is one: shaders/post/entity_outline.json). */
public class PostChain implements AutoCloseable {
    public PostChain(net.minecraft.client.renderer.texture.TextureManager textures, net.minecraft.server.packs.resources.ResourceManager resources,
                     com.mojang.blaze3d.pipeline.RenderTarget screen, net.minecraft.resources.ResourceLocation name) throws java.io.IOException {
        throw new UnsupportedOperationException();
    }
    /** process */ public void m_110023_(float partialTick) { throw new UnsupportedOperationException(); }
    /** resize */ public void m_110025_(int width, int height) { throw new UnsupportedOperationException(); }
    /** getTempTarget */ public com.mojang.blaze3d.pipeline.RenderTarget m_110036_(String name) { throw new UnsupportedOperationException(); }
    public void close() { throw new UnsupportedOperationException(); }
}
