package com.hexhaki.piece.client;

import com.hexhaki.piece.Faction;
import com.hexhaki.piece.Gender;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.network.S2CProfile;
import com.hexhaki.piece.style.FightingStyle;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.1: what this client knows of HexPiece profiles: its own in full, everyone else's gender, side and style (by entity id). Opens the
 * start menu when the server asks, as soon as no other screen is up.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class PieceClient {
    private static final Map<Integer, S2CProfile> SEEN = new HashMap<>();
    private static PieceProfile self = PieceProfile.EMPTY;
    private static boolean pendingCreation;

    private PieceClient() {
    }

    public static void accept(S2CProfile message) {
        SEEN.put(message.entity(), message);
        Minecraft minecraft = Minecraft.m_91087_();
        if (message.full() && minecraft.f_91074_ != null && minecraft.f_91074_.m_19879_() == message.entity()) {
            self = new PieceProfile(Gender.byOrdinal(message.gender()), Faction.byOrdinal(message.faction()),
                FightingStyle.byOrdinal(message.style()), message.chosen(), message.mastery(), message.renown());
        }
    }

    public static PieceProfile self() {
        return self;
    }

    public static Gender gender(int entity) {
        S2CProfile seen = SEEN.get(entity);
        return seen == null ? Gender.UNSET : Gender.byOrdinal(seen.gender());
    }

    public static Faction faction(int entity) {
        S2CProfile seen = SEEN.get(entity);
        return seen == null ? Faction.UNSET : Faction.byOrdinal(seen.faction());
    }

    public static void openCreation(boolean again) {
        pendingCreation = true;
        tryOpen();
    }

    static void tryOpen() {
        Minecraft minecraft = Minecraft.m_91087_();
        if (pendingCreation && minecraft.f_91074_ != null && minecraft.f_91080_ == null) {
            pendingCreation = false;
            minecraft.m_91152_(new CreationScreen());
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && pendingCreation) {
            tryOpen();
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SEEN.clear();
        self = PieceProfile.EMPTY;
        CombatMode.reset();
        pendingCreation = false;
    }
}
