package com.hexhaki.piece.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;

/**
 * 3.8.3: the density function {@code hexhaki:river_lines} (the archipelago's rivers: {@link RiverField}), registered as a density
 * function type so the world generation's data (data/minecraft/worldgen/density_function/overworld/ridges.json) can use it. Its value
 * is {@link RiverField#field} at the block's column: 0 on a river, 1 away from every river. Seeded by the world's own seed (the
 * overworld's, read as it loads, before its first chunk is made).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class RiverLines implements DensityFunction.SimpleFunction {
    public static final RiverLines INSTANCE = new RiverLines();
    public static final KeyDispatchDataCodec<RiverLines> CODEC = KeyDispatchDataCodec.m_216238_(MapCodec.unit(INSTANCE));
    static final DeferredRegister<Codec<? extends DensityFunction>> TYPES = DeferredRegister.create(Registries.f_256746_, "hexhaki");
    /** The world's seed the rivers are laid by (set as the overworld loads). */
    static volatile long seed;

    static {
        TYPES.register("river_lines", () -> CODEC.f_216232_());
        // 3.9.5: the placed islands (world/IslandLines)
        TYPES.register("island_field", () -> IslandLines.CODEC.f_216232_());
    }

    private RiverLines() {
    }

    /** Registers the type (HexPiece's constructor, on the mod's event bus). */
    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }

    @Override
    public double m_207386_(DensityFunction.FunctionContext context) {
        return RiverField.field(context.m_207115_(), context.m_207113_(), seed);
    }

    @Override
    public double m_207402_() {
        return 0.0;
    }

    @Override
    public double m_207401_() {
        return 1.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> m_214023_() {
        return CODEC;
    }

    @SubscribeEvent
    public static void load(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            seed = level.m_7328_();
        }
    }
}
