// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package dev.kosmx.playerAnim.minecraftApi;

public final class PlayerAnimationAccess {
    public static dev.kosmx.playerAnim.api.layered.AnimationStack getPlayerAnimLayer(net.minecraft.client.player.AbstractClientPlayer player) {
        throw new UnsupportedOperationException();
    }

    public static PlayerAssociatedAnimationData getPlayerAssociatedData(net.minecraft.client.player.AbstractClientPlayer player) {
        throw new UnsupportedOperationException();
    }

    public static class PlayerAssociatedAnimationData {
        public dev.kosmx.playerAnim.api.layered.IAnimation get(net.minecraft.resources.ResourceLocation id) { throw new UnsupportedOperationException(); }
        public dev.kosmx.playerAnim.api.layered.IAnimation set(net.minecraft.resources.ResourceLocation id,
            dev.kosmx.playerAnim.api.layered.IAnimation animation) { throw new UnsupportedOperationException(); }
    }
}
