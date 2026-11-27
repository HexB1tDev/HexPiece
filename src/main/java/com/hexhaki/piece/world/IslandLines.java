package com.hexhaki.piece.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * 3.9.5: the density function {@code hexhaki:island_field} (the archipelago's placed islands: {@link IslandField}), registered beside
 * {@code hexhaki:river_lines} (world/RiverLines) so data/hexhaki/worldgen/density_function/islands.json can use it. Its value is
 * {@link IslandField#field} at the block's column, seeded by the world's own seed (RiverLines reads it as the overworld loads).
 */
public final class IslandLines implements DensityFunction.SimpleFunction {
    public static final IslandLines INSTANCE = new IslandLines();
    public static final KeyDispatchDataCodec<IslandLines> CODEC = KeyDispatchDataCodec.m_216238_(MapCodec.unit(INSTANCE));

    private IslandLines() {
    }

    @Override
    public double m_207386_(DensityFunction.FunctionContext context) {
        return IslandField.field(context.m_207115_(), context.m_207113_(), RiverLines.seed);
    }

    @Override
    public double m_207402_() {
        return IslandField.SEA;
    }

    @Override
    public double m_207401_() {
        return IslandField.PEAK;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> m_214023_() {
        return CODEC;
    }
}
