package com.hexhaki.piece.client.haki;

import com.hexhaki.network.msg.S2CWifiHaki;
import com.hexhaki.piece.haki.ConquerorSparks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * 3.6.5 (the owner: "I also found traces of the old Haki in the wifi haki, it should use our latest one"): WiFi Haki's strike. The patch
 * sends the original's {@code HakiVfx.acceptWifiHaki} here instead of its own {@code acceptWifiHaki0}, which drew the arc as the original's
 * flat Photon beams ({@code wifiHakiArc}): now it is the one 3D Haki lightning (haki/ConquerorSparks.wifi through HakiBolts, the
 * release's own bolts: round lit tubes, sharp tips, in the user's Haki colour), from where it leaves the blade to the one it strikes.
 */
public final class WifiBolts {
    private WifiBolts() {
    }

    public static void accept(S2CWifiHaki message) {
        try {
            Minecraft minecraft = Minecraft.m_91087_();
            ClientLevel level = minecraft.f_91073_;
            if (level == null) {
                return;
            }
            Entity source = level.m_6815_(message.sourceId());
            if (source == null) {
                return;
            }
            Vec3 feet = source.m_20182_();
            double[] from = {message.startX() - feet.f_82479_, message.startY() - feet.f_82480_, message.startZ() - feet.f_82481_};
            double[] to = {message.endX() - feet.f_82479_, message.endY() - feet.f_82480_, message.endZ() - feet.f_82481_};
            HakiBolts.sparks(level, source, feet.f_82479_, feet.f_82480_, feet.f_82481_,
                ConquerorSparks.wifi(from, to, message.power(), message.travelTicks(), message.seed() + message.pulseIndex()), 0.0, 0.0F);
        } catch (RuntimeException failure) {
            // lightning never stops the game
        }
    }
}
