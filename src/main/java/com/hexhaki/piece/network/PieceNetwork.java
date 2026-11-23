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
     * (message 6), who holds a stance; S2CStyleFx stage 3. 3 (2.1.3): S2CRyou (message 7), a player's Ryou colour. 4 (2.1.4): the
     * Swordsman: C2SSword (8), S2CSword (9), S2CSwordFx (10), S2CSlash (11), S2CFinisher (12). 5 (2.3.6): S2CClash (13), two
     * blows meeting. 6 (2.3.8): S2CWater (14), Fishman Karate's water. 7 (2.4.4): S2CFall (15), the Haki stun fall. 8 (2.4.5): no
     * new message; the Conqueror's release storm is drawn from its seed on both sides (the server opens fissures under the strands
     * every client draws) and its shape changed, so the two sides must be the same version. 9 (2.4.6): S2CProfile carries the
     * player's alignment. 10 (2.7.0): S2CDragon (16), the Uo Uo no Mi's dragon form. 11 (2.7.1):
     * S2CDragon carries the dragon's colour. 12 (2.7.2): S2CUoFx (17), C2SDragon (18), the Uo Uo no Mi's moves. 13 (3.2.0): S2CEnergy
     * (19). 14 (3.3.2): C2SNikaJump (20), Gear 5's jump. 15 (3.4.4): C2SNikaWhip (21), Goof Ball let go. */
    private static final String PROTOCOL = "16";
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
        CHANNEL.registerMessage(7, S2CRyou.class, S2CRyou::encode, S2CRyou::decode, S2CRyou::handle);
        CHANNEL.registerMessage(8, C2SSword.class, C2SSword::encode, C2SSword::decode, C2SSword::handle);
        CHANNEL.registerMessage(9, S2CSword.class, S2CSword::encode, S2CSword::decode, S2CSword::handle);
        CHANNEL.registerMessage(10, S2CSwordFx.class, S2CSwordFx::encode, S2CSwordFx::decode, S2CSwordFx::handle);
        CHANNEL.registerMessage(11, S2CSlash.class, S2CSlash::encode, S2CSlash::decode, S2CSlash::handle);
        CHANNEL.registerMessage(12, S2CFinisher.class, S2CFinisher::encode, S2CFinisher::decode, S2CFinisher::handle);
        CHANNEL.registerMessage(13, S2CClash.class, S2CClash::encode, S2CClash::decode, S2CClash::handle);
        CHANNEL.registerMessage(14, S2CWater.class, S2CWater::encode, S2CWater::decode, S2CWater::handle);
        CHANNEL.registerMessage(15, S2CFall.class, S2CFall::encode, S2CFall::decode, S2CFall::handle);
        CHANNEL.registerMessage(16, S2CDragon.class, S2CDragon::encode, S2CDragon::decode, S2CDragon::handle);
        CHANNEL.registerMessage(17, S2CUoFx.class, S2CUoFx::encode, S2CUoFx::decode, S2CUoFx::handle);
        CHANNEL.registerMessage(18, C2SDragon.class, C2SDragon::encode, C2SDragon::decode, C2SDragon::handle);
        // 3.2.0: one's own fruit energy (server/FruitEnergy -> client/EnergyBar)
        CHANNEL.registerMessage(19, S2CEnergy.class, S2CEnergy::encode, S2CEnergy::decode, S2CEnergy::handle);
        // 3.3.2: Gear 5's jump (client/gear/NikaJumpClient -> server/NikaJump)
        CHANNEL.registerMessage(20, C2SNikaJump.class, C2SNikaJump::encode, C2SNikaJump::decode, C2SNikaJump::handle);
        // 3.4.4: Goof Ball's let go (client/gear/NikaKeys -> server/NikaWhip)
        CHANNEL.registerMessage(21, C2SNikaWhip.class, C2SNikaWhip::encode, C2SNikaWhip::decode, C2SNikaWhip::handle);
        // 3.5.2: Sky Road's strings and steps (client/ito/SkyRoad -> server/ItoMoves.skyString -> everyone near)
        CHANNEL.registerMessage(22, C2SItoSky.class, C2SItoSky::encode, C2SItoSky::decode, C2SItoSky::handle);
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
