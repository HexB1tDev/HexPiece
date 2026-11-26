// Compile-only API stub (Minecraft 1.20.1 / Forge 47). Never packaged; see verify/api-allowlist.txt (3.8.3).
package net.minecraft.world.level.levelgen;

/** 3.8.3: a density function (yarn DensityFunction, intermediary class_6910, obf dhd); SRG names from joined.tsrg. */
public interface DensityFunction {
    /** compute */ double m_207386_(FunctionContext context);

    /** fillArray */ void m_207362_(double[] array, ContextProvider provider);

    /** mapAll */ DensityFunction m_207456_(Visitor visitor);

    /** minValue */ double m_207402_();

    /** maxValue */ double m_207401_();

    /** codec */ net.minecraft.util.KeyDispatchDataCodec<? extends DensityFunction> m_214023_();

    /** dhd$b */ interface FunctionContext {
        /** blockX */ int m_207115_();

        /** blockY */ int m_207114_();

        /** blockZ */ int m_207113_();
    }

    /** dhd$a */ interface ContextProvider {
        /** fillAllDirectly */ void m_207207_(double[] array, DensityFunction function);
    }

    /** dhd$f */ interface Visitor {
    }

    /** dhd$d: SimpleFunction, its fillArray and mapAll given (defaults in the real interface) */
    interface SimpleFunction extends DensityFunction {
        @Override
        default void m_207362_(double[] array, ContextProvider provider) { throw new UnsupportedOperationException(); }

        @Override
        default DensityFunction m_207456_(Visitor visitor) { throw new UnsupportedOperationException(); }
    }
}
