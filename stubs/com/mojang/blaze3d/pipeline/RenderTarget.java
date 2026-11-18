// Compile-only API stub (Minecraft 1.20.1 / Forge 47). Never packaged; see verify/api-allowlist.txt.
package com.mojang.blaze3d.pipeline;

public abstract class RenderTarget {
    /** width (2.3.2) */ public int f_83915_;
    /** height */ public int f_83916_;
    /** frameBufferId */ public int f_83920_;
    /** bindWrite */ public void m_83947_(boolean setViewport) { throw new UnsupportedOperationException(); }
    /** getColorTextureId */ public int m_83975_() { throw new UnsupportedOperationException(); }
    /** resize */ public void m_83941_(int width, int height, boolean onOsx) { throw new UnsupportedOperationException(); }
    /** copyDepthFrom (2.4.7) */ public void m_83945_(RenderTarget other) { throw new UnsupportedOperationException(); }
    /** setClearColor (2.4.7) */ public void m_83931_(float red, float green, float blue, float alpha) { throw new UnsupportedOperationException(); }
    /** clear (2.4.7) */ public void m_83954_(boolean onOsx) { throw new UnsupportedOperationException(); }
    /** blitToScreen (2.4.7; 2.5.1: its name is m_83957_, not m_83961_) */ public void m_83957_(int width, int height, boolean disableBlend) { throw new UnsupportedOperationException(); }
}
