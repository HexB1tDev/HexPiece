package com.hexhaki.piece.server;

import com.hexhaki.binds.Binds;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.haki.QuickSlots;
import com.hexhaki.piece.sword.Katana;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 3.5.9: the Conqueror's quick abilities after the owner's word (haki/QuickSlots): the Haki Grip and the Galaxy Fist taken out
 * entirely (the patch sends their starts and ends here, where nothing happens, and keeps them off every player's slots), WiFi Haki a
 * Swordsman's alone (the patch sends its start and end here), on K, its lightning leaving the saya's mouth at the hip (the blade eased a
 * finger's width out of it while the key is held: client/sword/WifiPull draws it).
 */
public final class HakiQuick {
    private static boolean tidying;

    private HakiQuick() {
    }

    static boolean swordsman(ServerPlayer player) {
        PieceProfile profile = ProfileServer.of(player);
        return profile.chosen() && profile.style() != null && profile.style().sword();
    }

    /** The Haki Grip's start and end, and the Galaxy Fist's: gone (the owner: "Removed entirely"). */
    public static void gone(ServerPlayer player) {
    }

    /** WiFi Haki's key pressed: a Swordsman's alone. */
    public static void wifi(ServerPlayer player) {
        if (!swordsman(player)) {
            player.m_5661_(Component.m_237113_("WiFi Haki is a Swordsman's"), true);
            return;
        }
        HakiServerController.sovereignStrike(player);
    }

    /** WiFi Haki's key let go. */
    public static void wifiRelease(ServerPlayer player) {
        HakiServerController.releaseSovereignStrike(player);
    }

    /**
     * Where WiFi Haki's lightning leaves the player: the saya's mouth at the left hip when the blade is sheathed there (the gap the blade
     * is eased out of), else the original's point by the hand.
     */
    public static Vec3 mouth(ServerPlayer player) {
        Katana katana = SwordServer.sheathedAtHip(player);
        if (katana == null) {
            return HakiServerController.wifiHakiHandPoint(player);
        }
        // client/sword/SwordLayer.KOIGUCHI (body px: 3.4 to the left, 10 down from the neck, 4.2 forward), turned with the body
        double yaw = Math.toRadians(player.f_20883_);
        double left = 3.4 / 16.0;
        double ahead = 4.2 / 16.0;
        double x = Math.cos(yaw) * left - Math.sin(yaw) * ahead;
        double z = Math.sin(yaw) * left + Math.cos(yaw) * ahead;
        return player.m_20182_().m_82520_(x, (24.0 - 10.0) / 16.0, z);
    }

    /**
     * The head of the original's {@code Binds.sync(player)} (every time the slots are sent: at login, after a bind or a reset): the
     * player's slots set right (haki/QuickSlots) first, through the original's own {@code Binds.set} (which sends them again).
     */
    public static void tidy(ServerPlayer player) {
        if (tidying) {
            return;
        }
        tidying = true;
        try {
            int[] now = Binds.get(player);
            int[] want = QuickSlots.clean(now, swordsman(player));
            for (int i = 0; i < want.length; i++) {
                if (want[i] < 0 && now[i] >= 0) {
                    Binds.set(player, i, -1);
                }
            }
            for (int i = 0; i < want.length; i++) {
                if (want[i] >= 0 && Binds.get(player)[i] != want[i]) {
                    Binds.set(player, i, want[i]);
                }
            }
        } catch (RuntimeException failure) {
            // the slots stay as they were
        } finally {
            tidying = false;
        }
    }

    /** The style changed (the start menu): the slots set right for it and sent. */
    public static void styleChanged(ServerPlayer player) {
        try {
            Binds.sync(player);
        } catch (RuntimeException failure) {
            // sent at the next login
        }
    }
}
