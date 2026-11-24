package com.hexhaki.piece.server;

import com.hexhaki.binds.Binds;
import com.hexhaki.devilfruit.data.GearData;
import com.hexhaki.devilfruit.gameplay.GearServerController;
import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.fruit.FruitUtil;
import com.hexhaki.gameplay.HakiServerController;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 3.5.9 (the owner: "HOLDING R will now fully disable all powers and abilities (but fruit passives)"): every power off, or back on.
 * It is the original's own "powers off" bit (com.hexhaki.binds.Binds.off, synced to the client, BindsClient.off), which already holds
 * back every fruit ability (FruitData.active) and the fruit keys; HexPiece makes it hold back Haki too (RyouServer.hakiOff, the Haki
 * keys: PieceHooks.hakiKeysOff), the Gomu fruit (its actions: the patch's guard at the head of GearServerController.onAction), the
 * fighting styles and their moves, the sword, Iron Fist, the dragon's flame, Gear 5's jump and Sky Road (the C2S handlers ask
 * {@link #off}). The fruit's passives stay: its kit is cancelled (its moves and forms end), never removed (the original's switch,
 * FruitServer.togglePowers, removed the kit and so its passives: its callers come here instead), and its passive protections still ask
 * whether the fruit is owned ({@link #passives}).
 */
public final class Powers {
    private Powers() {
    }

    /** Is every power of {@code player}'s off? */
    public static boolean off(Player player) {
        try {
            return Binds.off(player);
        } catch (RuntimeException failure) {
            return false;
        }
    }

    /** The original's switch (FruitServer.togglePowers, from its "powers on/off" key and the Gomu fruit's): the same as holding R. */
    public static void toggle(ServerPlayer player) {
        set(player, !off(player));
    }

    /** FruitServer.attacked's question (was FruitData.active): the fruit's passives (its immunities, the Logia's dodge) stay on. */
    public static boolean passives(ServerPlayer player) {
        return FruitData.owns(player);
    }

    /**
     * HakiServerController.onAction's {@code GearData.hasFruit(player)} (it let a Gomu fruit eater's Haki through the powers-off bit):
     * not while every power is off.
     */
    public static boolean gomuThrough(Player player) {
        return !off(player) && GearData.hasFruit(player);
    }

    /** The head of the Gomu fruit's GearServerController.onAction: nothing while powers are off. */
    public static boolean refuse(ServerPlayer player) {
        return off(player);
    }

    public static void set(ServerPlayer player, boolean off) {
        if (off == off(player)) {
            return;
        }
        Binds.off(player, off);
        HakiServerController.cancelAll(player);
        if (off) {
            if (FruitData.owns(player)) {
                FruitKit kit = FruitServer.kit(player);
                if (kit != null) {
                    kit.cancel(player);
                }
            }
            if (GearData.hasFruit(player)) {
                GearServerController.cancelAll(player);
                if (GearData.flag(player, "gear5")) {
                    GearServerController.endGearFive(player, false);
                }
            }
            StyleServer.combat(player, false);
            SwordServer.stop(player);
            IronFist.forget(player);
            FruitUtil.animate(player, "__clear__", 4);
        }
        player.m_5661_(Component.m_237113_(off ? "Powers off (hold R to turn them on)" : "Powers on"), true);
        HakiServerController.sync(player);
        FruitServer.sync(player);
        ProfileServer.sync(player);
    }
}
