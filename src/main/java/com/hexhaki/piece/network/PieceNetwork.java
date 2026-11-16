package com.hexhaki.piece.network;

import com.hexhaki.HexHaki;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 2.1.1: HexPiece's own channel ({@code hexhaki:piece}); the original's channel and packets are untouched. Both sides must carry the
 * same protocol, so a client and a server from different HexPiece versions are refused at login instead of misreading each other.
 */
public final class PieceNetwork {
    /** 1 (2.1.1): profiles, the start menu, fighting styles. 2 (2.1.2): C2SCombat (message 5), combat mode; S2CStance
     * (message 6), who holds a stance; S2CStyleFx stage 3. */
    private static final String PROTOCOL = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(HexHaki.id("piece"), () -> PROTOCOL, PROTOCOL::equals,
        PROTOCOL::equals);
    private static boolean initialized;

    private PieceNetwork() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        CHANNEL.registerMessage(0, S2CProfile.class, S2CProfile::encode, S2CProfile::decode, S2CProfile::handle);
        CHANNEL.registerMessage(1, S2COpenCreation.class, S2COpenCreation::encode, S2COpenCreation::decode, S2COpenCreation::handle);
        CHANNEL.registerMessage(2, C2SCreate.class, C2SCreate::encode, C2SCreate::decode, C2SCreate::handle);
        CHANNEL.registerMessage(3, C2SStyleAction.class, C2SStyleAction::encode, C2SStyleAction::decode, C2SStyleAction::handle);
        CHANNEL.registerMessage(4, S2CStyleFx.class, S2CStyleFx::encode, S2CStyleFx::decode, S2CStyleFx::handle);
        CHANNEL.registerMessage(5, C2SCombat.class, C2SCombat::encode, C2SCombat::decode, C2SCombat::handle);
        CHANNEL.registerMessage(6, S2CStance.class, S2CStance::encode, S2CStance::decode, S2CStance::handle);
    }

    public static void sendTo(ServerPlayer receiver, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> receiver), message);
    }

    /** To every client tracking {@code subject}, and {@code subject} itself if it is a player. */
    public static void around(Entity subject, Object message) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> subject), message);
    }

    public static void toServer(Object message) {
        CHANNEL.sendToServer(message);
    }
}
