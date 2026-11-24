package com.hexhaki.piece.server;

import com.hexhaki.data.HakiData;
import net.minecraft.world.entity.player.Player;

/**
 * 3.6.2 (the owner: "All new fruits and every style have to obey /hp op on/off"): the original's op switch ({@code /hp op on|off}: its
 * HakiData "opMode" flag, server side). The original's own moves already obey it (FruitData.ready and cooldown, FruitData.unlocked,
 * FruitServer.pay, HakiData.consume, the Haki and Gear cooldown keys): no cooldowns, no energy spent, every move open whatever the
 * mastery. HexPiece's fruits go through the original's cooldowns (FruitData) already; this is for what HexPiece keeps itself: the
 * fruit energy ({@link FruitEnergy}), the styles' moves and their mastery ({@link StyleServer}), Iron Fist ({@link IronFist}), the sword
 * ({@link SwordServer}) and the dragon's rest after it turns back ({@link DragonServer}).
 */
public final class OpMode {
    private OpMode() {
    }

    /** True while {@code player} has op mode on. */
    public static boolean on(Player player) {
        try {
            return player != null && HakiData.op(player);
        } catch (RuntimeException failure) {
            return false;
        }
    }
}
