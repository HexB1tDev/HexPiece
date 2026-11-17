package com.hexhaki.piece.server;

import com.hexhaki.data.HakiData;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.PieceRules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 2.3.7: the Galaxy Fist made Iron Fist's (the owner: "Galaxy fist becomes a move of Iron fist that unlocks when the user activates RYOU
 * haki. ONLY for iron fist."). The original starts it from its Haki action (its quick slot held: {@code HakiServerController.onAction},
 * CONVERGENCE_START); the patch sends that start here ({@link #begin}), which lets it go on only for an Iron Fist fighter with Ryou up
 * ({@link PieceRules#galaxyFist}) and tells anyone else why not. The patch also drops the original's Armament mastery 520 for it: Ryou is
 * what opens it. Its charge, its leap, its blow and its lightning are the original's as they were.
 */
public final class GalaxyFist {
    private GalaxyFist() {
    }

    /** Ryou up: Armament on with Ryou over it. */
    static boolean ryou(ServerPlayer player) {
        return HakiData.flag(player, "armamentOn") && HakiData.flag(player, "acocOn");
    }

    /** The original's Galaxy Fist start, for an Iron Fist fighter with Ryou up only. */
    public static void begin(ServerPlayer player) {
        PieceProfile profile = ProfileServer.of(player);
        boolean ryou = ryou(player);
        if (!PieceRules.galaxyFist(profile.chosen() ? profile.style() : null, ryou)) {
            boolean iron = profile.chosen() && profile.style() == com.hexhaki.piece.style.FightingStyle.BRAWLER;
            player.m_5661_(Component.m_237113_(iron ? "Galaxy Fist: Ryou must be up (Armament, then Ryou)"
                : "Galaxy Fist: Iron Fist's alone, with Ryou up"), true);
            return;
        }
        HakiServerController.beginConvergence(player);
    }
}
