package com.hexhaki.piece.server;

import com.hexhaki.data.HakiData;
import com.hexhaki.fruit.FruitUtil;
import com.hexhaki.gameplay.HakiProgression;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.haki.RyouPalette;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CRyou;
import com.hexhaki.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.3: Ryou, the original's Advanced Haki (CONQ_ACOC) renamed, on the server. Its unlock, mastery and power are the original's
 * ({@code HakiProgression.advancedHakiUnlocked}, the {@code acocOn} flag every technique reads); only how it comes on changed:
 * <ul>
 *   <li>the owner: "whenever Ryou is unlcked it fully replaces regular haki, and we empty out the J key for now": once unlocked,
 *       switching Armament on brings Ryou with it ({@link #afterArmament}, run at the end of the original's {@code toggleArmament}; off
 *       takes it off as before) and J does nothing ({@link #acocKey} is the original's {@code toggleAcoc} now);</li>
 *   <li>the owner: "When a player first unlocks Ryou, assign them one random, clearly visible glow colour on the server and save it
 *       permanently with their player data ... Reconnecting, dying, changing dimensions, toggling Haki or restarting the server must not
 *       reroll it. Existing players who already possess the unlock must receive a colour once during migration": {@link #ensure} gives it
 *       once (at the unlock, or the first check after this update for a player who had it already), kept in Forge's persisted player
 *       data next to the HexPiece profile ({@code PlayerPersisted.HexPiece.RyouColor}), never given again; every client that sees the
 *       player is told ({@link S2CRyou}), and everyone who starts seeing them later.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class RyouServer {
    static final String COLOR = "RyouColor";
    /** Ticks between the checks that give a colour to a player who unlocked Ryou. */
    static final int CHECK_EVERY = 40;

    private RyouServer() {
    }

    /** The player's Ryou colour (ARGB), or 0 while they have none. */
    public static int color(ServerPlayer player) {
        return ProfileServer.root(player).m_128451_(COLOR);
    }

    /**
     * The colour, given now if none was given yet. 2.2.4 (the owner: "make sure it matches the color of RYOU always, same for the
     * conquerors haki releases, all lightnings in the game are to reflect the same color as RYOU"): every player has it from their first
     * login, Ryou unlocked or not (it was given at the unlock), since all their Haki lightning and their Haki slash wear it; it is still
     * given once and kept for good.
     */
    static int ensure(ServerPlayer player) {
        CompoundTag root = ProfileServer.root(player);
        int color = root.m_128451_(COLOR);
        if (color == 0) {
            color = RyouPalette.pick(player.m_217043_().m_188503_(RyouPalette.COLORS.length));
            root.m_128405_(COLOR, color);
            PieceNetwork.around(player, new S2CRyou(player.m_19879_(), color));
        }
        return color;
    }

    /** End of the original's toggleArmament: Armament just came on and Ryou is unlocked, so Ryou comes on with it. */
    public static void afterArmament(ServerPlayer player) {
        if (!HakiData.flag(player, "armamentOn") || HakiData.flag(player, "acocOn") || !HakiProgression.advancedHakiUnlocked(player)) {
            return;
        }
        ensure(player);
        HakiData.flag(player, "acocOn", true);
        HakiServerController.sync(player);
        // 2.9.4 (the owner: "remove this fuck ass sound for the Rio sound"): Ryou comes on silently (its acoc_on and acoc_off events
        // play nothing either, sounds.json)
    }

    /** J (the original's toggleAcoc): empty for now. */
    public static void acocKey(ServerPlayer player) {
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player && player.f_19797_ % CHECK_EVERY == 0) {
            ensure(player);
        }
    }

    static void tell(ServerPlayer player) {
        int color = color(player);
        if (color != 0) {
            PieceNetwork.around(player, new S2CRyou(player.m_19879_(), color));
        }
    }

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ensure(player);
            tell(player);
        }
    }

    @SubscribeEvent
    public static void respawned(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            tell(player);
        }
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            tell(player);
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer subject) {
            int color = color(subject);
            if (color != 0) {
                PieceNetwork.sendTo(viewer, new S2CRyou(subject.m_19879_(), color));
            }
        }
    }

    /**
     * 2.2.4: the server half of {@code PieceHooks.hakiKeysOff}: {@code HakiServerController.onAction} asked the original's "powers off"
     * switch ({@code Binds.off}) and dropped every Haki action while it was on. Haki answers whatever the fruit's switch says.
     */
    public static boolean hakiOff(Player player) {
        // 2.7.3 (the owner: "Uo Uo, you cannot switch from fruit mode to any mode or use any form of haki, ONLY use what the
        // transformation abilities allow"): no Haki at all while the dragon is out (or coming or going)
        // 3.4.2 (the owner, on the Horo fruit's Astral Projection: "cant use style or haki at all"): none while out of the body either
        return DragonServer.phase(player) != com.hexhaki.piece.dragon.DragonForm.HUMAN
            || player instanceof net.minecraft.server.level.ServerPlayer server && HoroMoves.astral(server);
    }
}
