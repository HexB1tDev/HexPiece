package com.hexhaki.piece.client.fx;

import com.hexhaki.piece.network.S2CWater;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * 2.3.8: Fishman Karate's water seen. 3.5.7 (the owner: "Remove FIshman karate from the game entirely"): its Water Bullet, Water Prison
 * and Brick Fist are gone with it. What the message still carries: a body knocked off its feet (a rag doll: client/fx/Tumbles, its fine
 * spray as it flies and lands: {@link #spray}) and the Six Powers' air bullets (client/fx/AirFx).
 */
public final class WaterFx {
    static final Random RANDOM = new Random();
    static final int MIST = 0x70D8F2FF;
    static final int MIST_FADE = 0x00E8F8FF;

    private WaterFx() {
    }

    public static void accept(S2CWater message) {
        if (Minecraft.m_91087_().f_91073_ == null) {
            return;
        }
        Vec3 dir = new Vec3(message.dx(), message.dy(), message.dz());
        switch (message.kind()) {
            case S2CWater.KNOCKED -> Tumbles.knocked(message.target(), dir);
            case S2CWater.AIR_SHOT, S2CWater.AIR_HIT -> AirFx.accept(message);
            default -> {
            }
        }
    }

    static double r(double spread) {
        return (RANDOM.nextDouble() - 0.5) * 2.0 * spread;
    }

    /** Fine spray off {@code at}, drifting along {@code drift}. */
    static void spray(Vec3 at, Vec3 drift, int wanted, double spread) {
        int n = PieceFx.count(wanted);
        for (int i = 0; i < n; i++) {
            PieceFx.at(PieceFx.DROP, false, 8 + RANDOM.nextInt(6), at.m_82520_(r(spread), r(spread), r(spread)))
                .velocity(drift.m_82520_(r(0.05), r(0.04) + 0.01, r(0.05))).colors(MIST, MIST_FADE).size(0.06, 0.03).fall(0.02).drag(0.9)
                .bloom(0).spawn();
        }
    }
}
