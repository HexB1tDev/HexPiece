package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.haki.ConquerorSparks;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.ConquerorAura;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * 2.9.1: the Conqueror's lightning off its user, as every client near sees it (server/ConquerorAura sends it as S2CUoFx kinds from
 * {@link ConquerorAura#SPARKS}; client/dragon/UoFx hands them here): arcs off the body as they move, a burst off a blade as it is drawn.
 */
public final class ConquerorSparksClient {
    private ConquerorSparksClient() {
    }

    public static void accept(S2CUoFx m) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        Entity user = level.m_6815_(m.user());
        try {
            switch (m.kind()) {
                case ConquerorAura.SPARKS -> {
                    if (user == null) {
                        return;
                    }
                    Vec3 feet = user.m_20182_();
                    HakiBolts.sparks(level, feet.f_82479_, feet.f_82480_, feet.f_82481_,
                        ConquerorSparks.body((int) m.size(), user.m_20206_(), user.m_20205_(), m.ticks()), 0.0, m.size() >= 5 ? 0.25F : 0.0F);
                }
                case ConquerorAura.BLADE -> {
                    // x, y, z the hand; a the blade's way; size its length; ticks the seed
                    Vec3 hand = new Vec3(m.x(), m.y(), m.z());
                    Vec3 feet = user != null ? user.m_20182_() : hand.m_82520_(0, -1.2, 0);
                    double[] base = {hand.f_82479_ - feet.f_82479_, hand.f_82480_ - feet.f_82480_, hand.f_82481_ - feet.f_82481_};
                    double[] tip = {base[0] + m.ax() * m.size(), base[1] + m.ay() * m.size(), base[2] + m.az() * m.size()};
                    HakiBolts.sparks(level, feet.f_82479_, feet.f_82480_, feet.f_82481_, ConquerorSparks.blade(base, tip, 11, 0.0, m.ticks()), 1.1,
                        0.9F);
                }
                default -> {
                }
            }
        } catch (RuntimeException failure) {
            // lightning never stops the game
        }
    }
}
