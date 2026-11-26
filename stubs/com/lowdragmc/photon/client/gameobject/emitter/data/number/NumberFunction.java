// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / Photon 1.1.17). Never packaged; see verify/api-allowlist.txt.
package com.lowdragmc.photon.client.gameobject.emitter.data.number;

public interface NumberFunction {
    static NumberFunction constant(Number value) { throw new UnsupportedOperationException(); }

    /** 3.8.1: the value at {@code t} (a constant's number). */
    Number get(float t, java.util.function.Supplier<Float> lerp);
}
