// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraft.server.level;

public class ServerPlayer extends net.minecraft.world.entity.player.Player {
    /** connection */ public net.minecraft.server.network.ServerGamePacketListenerImpl f_8906_;
    /** getAdvancements */ public net.minecraft.server.PlayerAdvancements m_8960_() { throw new UnsupportedOperationException(); }
    /** serverLevel */ public ServerLevel m_284548_() { throw new UnsupportedOperationException(); }
    /** teleportTo(level, x, y, z, yaw, pitch) (2.7.8) */ public void m_8999_(ServerLevel level, double x, double y, double z, float yaw, float pitch) { throw new UnsupportedOperationException(); }
}
