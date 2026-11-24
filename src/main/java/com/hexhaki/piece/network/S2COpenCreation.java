package com.hexhaki.piece.network;

import com.hexhaki.piece.client.PieceClient;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * The server asks a player who has not set sail yet to open the start menu; 3.6.0: or one who set sail before the Haki colour could be
 * chosen to open its page alone ({@link #colourOnly}).
 */
public record S2COpenCreation(boolean again, boolean colourOnly) {
    public S2COpenCreation(boolean again) {
        this(again, false);
    }

    public static void encode(S2COpenCreation message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.again);
        buffer.writeBoolean(message.colourOnly);
    }

    public static S2COpenCreation decode(FriendlyByteBuf buffer) {
        return new S2COpenCreation(buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(S2COpenCreation message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PieceClient.openCreation(message.again, message.colourOnly)));
        context.setPacketHandled(true);
    }
}
