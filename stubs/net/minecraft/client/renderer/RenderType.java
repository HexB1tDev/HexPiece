// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraft.client.renderer;

public abstract class RenderType {
    /** debugQuads() (2.1.9): position and colour, unlit, alpha blended, both faces, depth tested: every glow and trail (no additive blending, which some setups draw opaque) */ public static RenderType m_269166_() { throw new UnsupportedOperationException(); }
    /** textBackground() (2.1.5): position, colour, lightmap; unlit, alpha blended, depth tested and written, with the world's fog */ public static RenderType m_269058_() { throw new UnsupportedOperationException(); }
    /** eyes(texture): additive, full bright, no depth write (spider and enderman eyes) */ public static RenderType m_110488_(net.minecraft.resources.ResourceLocation texture) { throw new UnsupportedOperationException(); }
    /** entityTranslucentCull(texture) (2.1.3): alpha blended, back faces culled, depth tested and written */ public static RenderType m_110470_(net.minecraft.resources.ResourceLocation texture) { throw new UnsupportedOperationException(); }
    /** entityCutoutNoCull(texture) (2.1.4) */ public static RenderType m_110458_(net.minecraft.resources.ResourceLocation texture) { throw new UnsupportedOperationException(); }
    /** entityTranslucent(texture) */ public static RenderType m_110473_(net.minecraft.resources.ResourceLocation texture) { throw new UnsupportedOperationException(); }
    /** armorCutoutNoCull (4.0.0: the owner's outfits and masks) */ public static RenderType m_110431_(net.minecraft.resources.ResourceLocation texture) { throw new UnsupportedOperationException(); }
    /** entityTranslucentEmissive(texture) (2.3.4): alpha blended, full bright and unshaded, both faces, depth tested, no depth written */ public static RenderType m_234338_(net.minecraft.resources.ResourceLocation texture) { throw new UnsupportedOperationException(); }
}
