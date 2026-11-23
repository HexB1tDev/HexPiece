package com.hexhaki.piece.network;

import com.hexhaki.piece.server.NikaJump;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * 3.3.2: the player has just jumped in Gear 5 (client/gear/NikaJumpClient, from the jump itself: the server cannot tell a jump from the
 * original's Gear 5 walking hops) -> server/NikaJump.jumped.
 */
public record C2SNikaJump() {
    public static void encode(C2SNikaJump m, FriendlyByteBuf buffer) {
    }

    public static C2SNikaJump decode(FriendlyByteBuf buffer) {
        return new C2SNikaJump();
    }

    public static void handle(C2SNikaJump message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> NikaJump.jumped(player));
        }
        context.setPacketHandled(true);
    }
}
