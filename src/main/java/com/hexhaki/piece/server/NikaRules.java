package com.hexhaki.piece.server;

import com.hexhaki.devilfruit.data.GearData;
import com.hexhaki.network.GearAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 3.2.3 (the owner: "The stretchy moves like M1s and whatsoever, they should all be used in the normal like non-transformed state";
 * asked, "All stretch moves"): which of the Nika fruit's moves work without Gear 5. The original refused every move but the
 * transformation unless Gear 5 was up ({@code GearServerController.onAction}) and checked it again in Gatling, Giant and Coil Spring;
 * patch/PiecePatch.gearUntransformed asks here instead. Untransformed: the stretching punch (Pistol), Rocket, Whip, Gatling, Giant
 * (Gigante) and Coil Spring. Gear 5 only: Rubber World, Sky Bounce, Star Gun, and the Haki versions (Dawn Pistol, Dawn Whip, the
 * Bajrang Gun, the lightning lance), which the original only ever gives in Gear 5 ({@code GearServerController.dawn}).
 */
public final class NikaRules {
    private NikaRules() {
    }

    /** Whether {@code action} goes ahead for {@code player}: in Gear 5 every one, out of it the stretch moves only. */
    public static boolean allowed(ServerPlayer player, GearAction action) {
        return GearData.flag(player, "gear5") || stretch(action);
    }

    /** The stretch moves (their press and their let go). */
    public static boolean stretch(GearAction action) {
        return switch (action.name()) {
            case "PISTOL", "ROCKET_START", "ROCKET_RELEASE", "WHIP", "GATLING_START", "GATLING_RELEASE", "GIANT_START", "GIANT_RELEASE",
                "COIL_START", "COIL_RELEASE", "GIANT_GRAB" -> true;
            default -> false;
        };
    }

    /**
     * 3.2.7 (the owner: "Dawn Whip needs a complete rework. That move has to be entirely scrapped and redone ... he actually rotates
     * like a cartoon character. I think that the player's feet should also rotate like a cartoon character"): the whip's clip, in place of
     * the original's {@code animate(player, "gear5_whip", priority)} in {@code GearServerController.whip}: with Haki on in Gear 5 (the
     * original's {@code dawn}: Gear 5 with Armament or Ryou on) the cartoon spin {@code gear5_dawn_whip} (tools/nika_anims.py), else the
     * whip as it was.
     */
    public static void whipClip(ServerPlayer player, String name, int priority) {
        boolean dawn = GearData.flag(player, "gear5")
            && (com.hexhaki.data.HakiData.flag(player, "armamentOn") || com.hexhaki.data.HakiData.flag(player, "acocOn"));
        com.hexhaki.gameplay.HakiServerController.animate(player, dawn ? "gear5_dawn_whip" : name, priority);
    }

    /**
     * In Gatling's tick, Giant's and Coil Spring's start and Coil Spring's let go, in place of their "Gear 5 is up" check: they go ahead
     * for anyone with the fruit (transformed or not).
     */
    public static boolean stretched(Player player, String flag) {
        return GearData.hasFruit(player);
    }
}
