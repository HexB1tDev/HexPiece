package com.hexhaki.piece.server;

import com.hexhaki.piece.Faction;
import com.hexhaki.piece.Gender;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.PieceRules;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2COpenCreation;
import com.hexhaki.piece.network.S2CProfile;
import com.hexhaki.piece.style.FightingStyle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.1: every player's HexPiece profile ({@link PieceProfile}), kept in Forge's persisted player data (it goes with them through death),
 * sent to them in full and to everyone who sees them in part (their gender changes how they are drawn, their side and style how NPCs
 * and the menus treat them). A player who has not been through the start menu is asked to on every login until they have.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class ProfileServer {
    /** Forge's persisted player data (copied over on respawn) and HexPiece's tag in it. */
    static final String PERSISTED = "PlayerPersisted";
    static final String ROOT = "HexPiece";

    private ProfileServer() {
    }

    static CompoundTag root(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.m_128441_(PERSISTED)) {
            data.m_128365_(PERSISTED, new CompoundTag());
        }
        CompoundTag persisted = data.m_128469_(PERSISTED);
        if (!persisted.m_128441_(ROOT)) {
            persisted.m_128365_(ROOT, new CompoundTag());
        }
        return persisted.m_128469_(ROOT);
    }

    public static PieceProfile of(ServerPlayer player) {
        CompoundTag root = root(player);
        int[] mastery = new int[FightingStyle.values().length];
        for (FightingStyle style : FightingStyle.values()) {
            mastery[style.ordinal()] = PieceRules.clampMastery(root.m_128451_("Mastery_" + style.id()));
        }
        return new PieceProfile(Gender.byOrdinal(root.m_128451_("Gender")), Faction.byOrdinal(root.m_128451_("Faction")),
            FightingStyle.byCode(root.m_128451_("Style")), root.m_128471_("Chosen"), mastery, root.m_128454_("Renown"),
            com.hexhaki.piece.Alignment.clamp(root.m_128451_("Alignment")));
    }

    /** The start menu's picks: kept, the player told, everyone who sees them told. Refused once set sail (an operator can reset). */
    public static void create(ServerPlayer player, Gender gender, Faction faction, FightingStyle style) {
        CompoundTag root = root(player);
        if (root.m_128471_("Chosen")) {
            player.m_5661_(Component.m_237113_("You have already set sail. An operator can let you choose again with /hexpiece reset."), false);
            return;
        }
        if (!gender.choice() || !faction.choice() || !style.choice()) {
            PieceNetwork.sendTo(player, new S2COpenCreation(true));
            return;
        }
        root.m_128405_("Gender", gender.ordinal());
        root.m_128405_("Faction", faction.ordinal());
        root.m_128405_("Style", style.code());
        root.m_128379_("Chosen", true);
        sync(player);
        String side = faction == Faction.PIRATE ? "a pirate" : "a marine";
        player.m_5661_(Component.m_237113_("You set sail as " + side + ", fighting with " + style.display() + "."), false);
    }

    public static void reset(ServerPlayer player) {
        StyleServer.combat(player, false); // 2.1.2: no style, no combat stance
        CompoundTag root = root(player);
        root.m_128379_("Chosen", false);
        sync(player);
        PieceNetwork.sendTo(player, new S2COpenCreation(true));
    }

    /** Style mastery for landing blows; the player is told when it opens a special. */
    public static void addMastery(ServerPlayer player, FightingStyle style, int amount) {
        if (!style.choice() || amount <= 0) {
            return;
        }
        CompoundTag root = root(player);
        String key = "Mastery_" + style.id();
        int before = PieceRules.clampMastery(root.m_128451_(key));
        int after = PieceRules.clampMastery(before + amount);
        if (after == before) {
            return;
        }
        root.m_128405_(key, after);
        for (com.hexhaki.piece.style.StyleMove move : com.hexhaki.piece.style.StyleMove.values()) {
            if (move.style() == style && move.mastery() > before && move.mastery() <= after) {
                player.m_5661_(Component.m_237113_(style.display() + ": " + move.display() + " learned!"), true);
            }
        }
        if (after / 50 != before / 50) {
            sync(player);
        }
    }

    /**
     * 3.4.8 (the owner: "we need a mastery command for the styles so I can test the moves ... hex piece space style space mastery space
     * then the number"): {@code style}'s mastery set to {@code value} (0 to PieceRules.MAX_MASTERY), every special it opens on the way up
     * announced as when it is earned; returns the value set.
     */
    public static int setMastery(ServerPlayer player, FightingStyle style, int value) {
        CompoundTag root = root(player);
        String key = "Mastery_" + style.id();
        int before = PieceRules.clampMastery(root.m_128451_(key));
        int after = PieceRules.clampMastery(value);
        root.m_128405_(key, after);
        for (com.hexhaki.piece.style.StyleMove move : com.hexhaki.piece.style.StyleMove.values()) {
            if (move.style() == style && move.mastery() > before && move.mastery() <= after) {
                player.m_5661_(Component.m_237113_(style.display() + ": " + move.display() + " learned!"), false);
            }
        }
        sync(player);
        return after;
    }

    /** Renown for beating someone of the other side; a new title or rank is announced. */
    public static void addRenown(ServerPlayer player, long amount) {
        PieceProfile profile = of(player);
        if (!profile.faction().choice() || amount <= 0) {
            return;
        }
        long after = profile.renown() + amount;
        root(player).m_128356_("Renown", after);
        String before = profile.title();
        String now = PieceRules.title(profile.faction(), after);
        if (profile.faction() == Faction.PIRATE) {
            player.m_5661_(Component.m_237113_("Bounty: " + PieceRules.berries(after) + " berries"), true);
        } else {
            player.m_5661_(Component.m_237113_("Merit: " + after), true);
        }
        if (!now.equals(before)) {
            player.m_5661_(Component.m_237113_(profile.faction() == Faction.PIRATE ? "The world now calls you: " + now : "Promoted to " + now + "!"),
                false);
        }
        sync(player);
    }

    /** 2.4.6: sets {@code player}'s alignment ({@link com.hexhaki.piece.Alignment}: -100 pure evil, 0 neutral, 100 pure good); returns it. */
    public static int setAlignment(ServerPlayer player, int value) {
        int alignment = com.hexhaki.piece.Alignment.clamp(value);
        root(player).m_128405_("Alignment", alignment);
        sync(player);
        return alignment;
    }

    static S2CProfile full(ServerPlayer player, PieceProfile profile) {
        return new S2CProfile(player.m_19879_(), profile.gender().ordinal(), profile.faction().ordinal(), profile.style().ordinal(), true,
            profile.chosen(), profile.mastery().clone(), profile.renown(), profile.alignment());
    }

    static S2CProfile seen(ServerPlayer player, PieceProfile profile) {
        return new S2CProfile(player.m_19879_(), profile.gender().ordinal(), profile.faction().ordinal(), profile.style().ordinal(), false,
            false, new int[0], 0L, 0);
    }

    /** The player told everything, everyone who sees them what shows. */
    public static void sync(ServerPlayer player) {
        PieceProfile profile = of(player);
        PieceNetwork.sendTo(player, full(player, profile));
        PieceNetwork.around(player, seen(player, profile));
    }

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CompoundTag root = root(player);
            if (root.m_128471_("Chosen") && !FightingStyle.byCode(root.m_128451_("Style")).choice()) {
                // 3.5.7: their style is gone (Fishman Karate, the owner: "Remove FIshman karate from the game entirely"): they set
                // sail again and pick another
                root.m_128379_("Chosen", false);
                player.m_5661_(Component.m_237113_("Fishman Karate is no more: choose a new fighting style."), false);
            }
            sync(player);
            if (!of(player).chosen()) {
                PieceNetwork.sendTo(player, new S2COpenCreation(false));
            }
        }
    }

    @SubscribeEvent
    public static void respawned(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer subject) {
            PieceNetwork.sendTo(viewer, seen(subject, of(subject)));
        }
    }
}
