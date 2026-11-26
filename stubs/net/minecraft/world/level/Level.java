// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraft.world.level;

public abstract class Level implements LevelAccessor {
    /** getFluidState (2.9.0) */ public net.minecraft.world.level.material.FluidState m_6425_(net.minecraft.core.BlockPos pos) { throw new UnsupportedOperationException(); }
    /** getGameRules (2.1.4) */ public net.minecraft.world.level.GameRules m_46469_() { throw new UnsupportedOperationException(); }
    /** destroyBlock(pos, drop, entity) (2.1.4) */ public boolean m_46953_(net.minecraft.core.BlockPos pos, boolean drop, net.minecraft.world.entity.Entity entity) { throw new UnsupportedOperationException(); }
    /** isClientSide (1.2.1) */ public boolean f_46443_;
    /** OVERWORLD */ public static final net.minecraft.resources.ResourceKey<Level> f_46428_ = null;
    /** dimension */ public net.minecraft.resources.ResourceKey<Level> m_46472_() { throw new UnsupportedOperationException(); }
    /** getBlockEntity(pos) */ public net.minecraft.world.level.block.entity.BlockEntity m_7702_(net.minecraft.core.BlockPos pos) { throw new UnsupportedOperationException(); }
    /** destroyBlockProgress(breakerId, pos, progress 0-9, else gone) (3.1.9: frozen bodies' ice cracking; verify/api-allowlist.txt) */ public void m_6801_(int breaker, net.minecraft.core.BlockPos pos, int progress) { throw new UnsupportedOperationException(); }
    /** setBlock(pos, state, flags) */ public boolean m_7731_(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, int flags) { throw new UnsupportedOperationException(); }
    /** mayInteract(player, pos): not spawn-protected for them */ public boolean m_7966_(net.minecraft.world.entity.player.Player player, net.minecraft.core.BlockPos pos) { throw new UnsupportedOperationException(); }
    /** playSound */ public void m_6263_(net.minecraft.world.entity.player.Player player, double x, double y, double z, net.minecraft.sounds.SoundEvent sound, net.minecraft.sounds.SoundSource source, float volume, float pitch) { throw new UnsupportedOperationException(); }
    /** getGameTime */ public long m_46467_() { throw new UnsupportedOperationException(); }
    /** getDayTime */ public long m_46468_() { throw new UnsupportedOperationException(); }
    /** getEntity(int) */ public net.minecraft.world.entity.Entity m_6815_(int id) { throw new UnsupportedOperationException(); }
    /** clip */ public net.minecraft.world.phys.BlockHitResult m_45547_(ClipContext context) { throw new UnsupportedOperationException(); }
    /** isRaining */ public boolean m_46471_() { throw new UnsupportedOperationException(); }
    /** isThundering (1.3.29) */ public boolean m_46470_() { throw new UnsupportedOperationException(); }
    /** isRainingAt(pos): rain falling on that block (open sky, a rainy biome) (4.1.15: a kiss in the rain) */ public boolean m_46758_(net.minecraft.core.BlockPos pos) { throw new UnsupportedOperationException(); }
    /** getRandom (the original HexHaki jar calls it on ServerLevel) */ public net.minecraft.util.RandomSource m_213780_() { throw new UnsupportedOperationException(); }
    /** addFreshEntity */ public boolean m_7967_(net.minecraft.world.entity.Entity entity) { throw new UnsupportedOperationException(); }
    /** 3.8.2: getEntities(except, box) (EntityGetter's; the original calls it on ServerLevel) */ public java.util.List<net.minecraft.world.entity.Entity> m_45933_(net.minecraft.world.entity.Entity except, net.minecraft.world.phys.AABB box) { throw new UnsupportedOperationException(); }

    /** getEntitiesOfClass */ public <T extends net.minecraft.world.entity.Entity> java.util.List<T> m_6443_(Class<T> type, net.minecraft.world.phys.AABB box, java.util.function.Predicate<? super T> filter) { throw new UnsupportedOperationException(); }
    /** blockEvent(pos, block, type, data) (1.3.29: a chest's lid up while it is tidied) */ public void m_7696_(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.Block block, int type, int data) { throw new UnsupportedOperationException(); }
}
