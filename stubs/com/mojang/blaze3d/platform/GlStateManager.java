// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / Photon 1.1.17). Never packaged; see verify/api-allowlist.txt.
package com.mojang.blaze3d.platform;

public class GlStateManager {
    /** glBindFramebuffer (2.3.2) */ public static void _glBindFramebuffer(int target, int framebuffer) { throw new UnsupportedOperationException(); }
    /** glBlitFramebuffer (2.3.2) */ public static void _glBlitFrameBuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1,
        int dstY1, int mask, int filter) { throw new UnsupportedOperationException(); }
    /** glGetTexLevelParameteri on the bound texture */ public static int _getTexLevelParameter(int target, int level, int parameter) { throw new UnsupportedOperationException(); }
    public enum DestFactor {
        ONE, ONE_MINUS_SRC_ALPHA
    }
}
