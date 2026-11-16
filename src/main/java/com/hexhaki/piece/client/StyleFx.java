package com.hexhaki.piece.client;

import com.hexhaki.piece.network.S2CStyleFx;
import com.hexhaki.piece.style.StyleMove;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import java.util.Random;

/**
 * 2.1.1: what a fighting-style move looks like besides its animation and the original's impact (hit-stop, shake, flash): sparks where a
 * blow lands, flames for the Flame Leg, water for Fishman Karate's bullets, cut air along the Air Blade, a puff where Shave vanishes.
 */
public final class StyleFx {
    private StyleFx() {
    }

    public static void accept(S2CStyleFx message) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        StyleMove move = StyleMove.byOrdinal(message.move());
        if (level == null || move == null) {
            return;
        }
        Random random = new Random();
        switch (message.stage()) {
            case 0 -> started(level, move, message, random);
            case 1 -> landed(level, move, message, random);
            case 2 -> shot(level, move, message, random);
            default -> {
            }
        }
    }

    static void puff(ClientLevel level, ParticleOptions type, double x, double y, double z, int count, double spread, double speed,
                     Random random) {
        for (int i = 0; i < count; i++) {
            level.m_7106_(type, x + (random.nextDouble() - 0.5) * spread, y + (random.nextDouble() - 0.5) * spread,
                z + (random.nextDouble() - 0.5) * spread, (random.nextDouble() - 0.5) * speed, (random.nextDouble() - 0.2) * speed,
                (random.nextDouble() - 0.5) * speed);
        }
    }

    static void started(ClientLevel level, StyleMove move, S2CStyleFx m, Random random) {
        switch (move.effect()) {
            case VANISH -> puff(level, ParticleTypes.f_123759_, m.x(), m.y() + 1.0, m.z(), 14, 0.8, 0.12, random);
            case FIRE -> {
                for (int i = 0; i < 24; i++) {
                    double a = i / 24.0 * Math.PI * 2;
                    level.m_7106_(ParticleTypes.f_175834_, m.x() + Math.cos(a) * 0.6, m.y() + 0.3, m.z() + Math.sin(a) * 0.6,
                        Math.cos(a) * 0.05, 0.08, Math.sin(a) * 0.05);
                }
            }
            case GUARD -> puff(level, ParticleTypes.f_123797_, m.x(), m.y() + 1.0, m.z(), 18, 1.0, 0.3, random);
            case COUNTER -> {
                for (int i = 0; i < 16; i++) {
                    double a = i / 16.0 * Math.PI * 2;
                    level.m_7106_(ParticleTypes.f_123769_, m.x() + Math.cos(a) * 0.8, m.y() + 1.0, m.z() + Math.sin(a) * 0.8, 0.0, 0.05, 0.0);
                }
            }
            default -> {
            }
        }
    }

    static void landed(ClientLevel level, StyleMove move, S2CStyleFx m, Random random) {
        int weight = 0;
        for (var hit : move.hits()) {
            weight = Math.max(weight, hit.weight().ordinal());
        }
        puff(level, ParticleTypes.f_123797_, m.x(), m.y(), m.z(), 6 + weight * 4, 0.5, 0.5 + weight * 0.15, random);
        switch (move.effect()) {
            case FIRE -> puff(level, ParticleTypes.f_175834_, m.x(), m.y(), m.z(), 12, 0.6, 0.12, random);
            case WATER -> puff(level, ParticleTypes.f_123769_, m.x(), m.y(), m.z(), 14, 0.5, 0.3, random);
            default -> {
                if (weight >= 2) {
                    puff(level, ParticleTypes.f_123759_, m.x(), m.y(), m.z(), 6, 0.4, 0.15, random);
                }
            }
        }
    }

    static void shot(ClientLevel level, StyleMove move, S2CStyleFx m, Random random) {
        double dx = m.x() - m.dx();
        double dy = m.y() - m.dy();
        double dz = m.z() - m.dz();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = (int) Math.max(4, length * 3);
        boolean water = move.effect() == StyleMove.Effect.WATER;
        for (int i = 1; i <= steps; i++) {
            double t = i / (double) steps;
            double x = m.dx() + dx * t;
            double y = m.dy() + dy * t;
            double z = m.dz() + dz * t;
            if (water) {
                level.m_7106_(ParticleTypes.f_123804_, x, y, z, 0.0, 0.0, 0.0);
                if (i % 3 == 0) {
                    level.m_7106_(ParticleTypes.f_123769_, x, y, z, 0.0, 0.02, 0.0);
                }
            } else {
                level.m_7106_(ParticleTypes.f_123797_, x, y, z, (random.nextDouble() - 0.5) * 0.1, (random.nextDouble() - 0.5) * 0.1,
                    (random.nextDouble() - 0.5) * 0.1);
                if (i % 2 == 0) {
                    level.m_7106_(ParticleTypes.f_123759_, x, y, z, 0.0, 0.0, 0.0);
                }
            }
        }
    }
}
