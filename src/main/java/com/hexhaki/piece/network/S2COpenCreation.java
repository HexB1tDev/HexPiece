package com.hexhaki.piece.network;

import com.hexhaki.piece.client.PieceClient;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** The server asks a player who has not set sail yet to open the start menu. */
public record S2COpenCreation(boolean again) {
    public static void encode(S2COpenCreation message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.again);
    }

    public static S2COpenCreation decode(FriendlyByteBuf buffer) {
        return new S2COpenCreation(buffer.readBoolean());
    }

    public static void handle(S2COpenCreation message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PieceClient.openCreation(message.again)));
        context.setPacketHandled(true);
    }
}
