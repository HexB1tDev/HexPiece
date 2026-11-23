package com.hexhaki.piece.server;

import com.hexhaki.devilfruit.data.GearData;
import com.hexhaki.network.GearAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 3.2.3 (the owner: "The stretchy moves like M1s and whatsoever, they should all be used in the normal like non-transformed state";
 * asked, "All stretch moves"): which of the Nika fruit's moves work without Gear 5. The original refused every move but the
 * transformation unless Gear 5 was up ({@code GearServerController.onAction}) and checked it again in Gatling, Giant and Coil Spring;
 * patch/PiecePatch.gearUntransformed asks here instead. Untransformed: the stretching punch (Pistol), Rocket, Gatling and Coil Spring
 * (3.4.6: no longer the Whip or Giant, the owner's word). Gear 5 only: Goof Ball, Giant, Rubber World, Star Gun, and the Haki versions
 * (Dawn Pistol, Dawn Whip, the
 * Bajrang Gun, the lightning lance), which the original only ever gives in Gear 5 ({@code GearServerController.dawn}).
 */
public final class NikaRules {
    private NikaRules() {
    }

    /** The original's M1 lock: GearServerController.setM1Cooldown sets "cd_pistol" to the throw's tick + 20, the stretched arm's whole
     * life (StretchStyle.PISTOL: wind-up 6, out 4, held 3, back 7). */
    static final String PISTOL_LOCK = "cd_pistol";

    /**
     * 3.4.9 (the owner: the M1s "should have a cooldown, otherwise you can just spam it and cancel the animation, which isn't should
     * not be possible. You shouldn't be able to punch until the arm is fully returned to your body to avoid lag"): in place of the
     * Pistol's {@code onCooldown(player, "pistol")} in GearServerController.pistol: true while the last arm thrown is still out (until
     * the tick the original's own lock ends), in every mode; the original's no-cooldown mode (HakiServerController.noCooldowns) skipped
     * it, so every click restarted the punch and threw another arm.
     */
    public static boolean armOut(ServerPlayer player, String key) {
        return player.m_9236_().m_46467_() < GearData.longValue(player, PISTOL_LOCK);
    }

    /** Whether {@code action} goes ahead for {@code player}: in Gear 5 every one, out of it the stretch moves only. */
    public static boolean allowed(ServerPlayer player, GearAction action) {
        // 3.4.4: nothing else while Goof Ball, its daze or Dawn Whip runs (server/NikaWhip)
        return (GearData.flag(player, "gear5") || stretch(action)) && !NikaWhip.busy(player);
    }

    /**
     * The stretch moves that work untransformed (their press and their let go). 3.4.6 (the owner: "the goofy ball, that ability that
     * we just made, it should be only usable in Gear 5 mode, which is the transformation. Same for Giant. Giant is not usable without
     * the transformation"): the Whip (Goof Ball, Dawn Whip) and Giant (Gigante, its grab, the Bajrang Gun) are Gear 5's only now.
     */
    public static boolean stretch(GearAction action) {
        return switch (action.name()) {
            case "PISTOL", "ROCKET_START", "ROCKET_RELEASE", "GATLING_START", "GATLING_RELEASE", "COIL_START", "COIL_RELEASE" -> true;
            default -> false;
        };
    }

    /**
     * 3.4.4: in Gear 5 the original's whip never gets this far (server/NikaWhip runs Goof Ball and Dawn Whip), so its clip is its own.
     * 3.2.7 (the owner: "Dawn Whip needs a complete rework. That move has to be entirely scrapped and redone ... he actually rotates
     * like a cartoon character. I think that the player's feet should also rotate like a cartoon character"): the whip's clip, in place of
     * the original's {@code animate(player, "gear5_whip", priority)} in {@code GearServerController.whip}: with Haki on in Gear 5 (the
     * original's {@code dawn}: Gear 5 with Armament or Ryou on) the cartoon spin {@code gear5_dawn_whip} (tools/nika_anims.py), else the
     * whip as it was.
     */
    public static void whipClip(ServerPlayer player, String name, int priority) {
        // 3.4.4: the original's whip only runs untransformed now (server/NikaWhip has Gear 5's C): its own clip
        com.hexhaki.gameplay.HakiServerController.animate(player, name, priority);
    }

    /**
     * 3.3.2 (the owner: "shift + space ability is removed"): in GearServerController's mechanicsAction (Shift + Space's press) and
     * skyBounce, in place of {@code GearData.unlocked(player, ability)}: Sky Bounce is never unlocked; anything else as it was.
     */
    public static boolean unlocked(Player player, com.hexhaki.devilfruit.data.GearAbility ability) {
        return ability != com.hexhaki.devilfruit.data.GearAbility.SKY_BOUNCE && GearData.unlocked(player, ability);
    }

    /**
     * In Gatling's tick, Giant's and Coil Spring's start and Coil Spring's let go, in place of their "Gear 5 is up" check: they go ahead
     * for anyone with the fruit (transformed or not).
     */
    public static boolean stretched(Player player, String flag) {
        return GearData.hasFruit(player);
    }
}
