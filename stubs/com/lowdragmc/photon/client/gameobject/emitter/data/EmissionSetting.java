// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / Photon 1.1.17). Never packaged; see verify/api-allowlist.txt.
package com.lowdragmc.photon.client.gameobject.emitter.data;

public class EmissionSetting {
    public void setEmissionRate(com.lowdragmc.photon.client.gameobject.emitter.data.number.NumberFunction rate) { throw new UnsupportedOperationException(); }

    /** 3.8.1: the bursts the original's HakiVfx.burst / burstCycles add (Lombok @Getter on the class). */
    public java.util.List<Burst> getBursts() { throw new UnsupportedOperationException(); }

    public static class Burst {
        public int time;
        public int cycles;
        public int interval;

        public Burst() { throw new UnsupportedOperationException(); }

        public com.lowdragmc.photon.client.gameobject.emitter.data.number.NumberFunction getCount() { throw new UnsupportedOperationException(); }

        public void setCount(com.lowdragmc.photon.client.gameobject.emitter.data.number.NumberFunction count) { throw new UnsupportedOperationException(); }
    }
}
