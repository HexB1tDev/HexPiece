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
        if (m.kind() == ConquerorAura.CHARGE || m.kind() == ConquerorAura.CHARGE_END) {
            ConquerorChargeSound.accept(m.kind(), m.user()); // 2.9.5: the charge's sound, held while G is
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
                    HakiBolts.sparks(level, user, feet.f_82479_, feet.f_82480_, feet.f_82481_,
                        ConquerorSparks.body((int) m.size(), user.m_20206_(), user.m_20205_(), m.ticks()), 0.0, m.size() >= 5 ? 0.25F : 0.0F);
                }
                case ConquerorAura.BLADE -> {
                    // x, y, z the hand; a the blade's way; size its length; ticks the seed
                    Vec3 hand = new Vec3(m.x(), m.y(), m.z());
                    Vec3 feet = user != null ? user.m_20182_() : hand.m_82520_(0, -1.2, 0);
                    double[] base = {hand.f_82479_ - feet.f_82479_, hand.f_82480_ - feet.f_82480_, hand.f_82481_ - feet.f_82481_};
                    double[] tip = {base[0] + m.ax() * m.size(), base[1] + m.ay() * m.size(), base[2] + m.az() * m.size()};
                    HakiBolts.sparks(level, user, feet.f_82479_, feet.f_82480_, feet.f_82481_, ConquerorSparks.blade(base, tip, 11, 0.0, m.ticks()), 1.1,
                        0.9F);
                }
                case ConquerorAura.MIXED -> {
                    // 3.1.2: the Conqueror's lightning mixed into a Gura quake at a point (silent: the quake has its own sounds)
                    // 3.1.4: no flash of its own (a white orb in the air, the owner's screenshot), and on the fist where it is drawn when
                    // it gathers there
                    double x = m.x();
                    double y = m.y();
                    double z = m.z();
                    if (m.bz() > 0.0F && user instanceof net.minecraft.world.entity.player.Player player) {
                        Vec3 fist = com.hexhaki.piece.client.gura.GuraFx.fist(player, 1.0F);
                        x = fist.f_82479_;
                        y = fist.f_82480_;
                        z = fist.f_82481_;
                    }
                    // 3.1.6: ax carries how thick (the Gura ultimate's "thick lightning"; 0 is the usual)
                    double thick = m.ax() > 0.0F ? Math.min(3.0, m.ax()) : 1.0;
                    HakiBolts.sparks(level, user, x, y, z, ConquerorSparks.burst(m.size(), Math.max(2, Math.min(14, m.id())), m.bx(), m.ticks(),
                        thick), 0.0, 0.0F);
                }
                default -> {
                }
            }
        } catch (RuntimeException failure) {
            // lightning never stops the game
        }
    }
}
