package com.hexhaki.piece.network;

import com.hexhaki.piece.server.UoMoves;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** 2.7.2: M1 held down (or let go) in dragon form: the dragon's fire breath (client/dragon/DragonInput -> server/UoMoves.flame). */
public record C2SDragon(boolean flame) {
    public static void encode(C2SDragon m, FriendlyByteBuf buffer) {
        buffer.writeBoolean(m.flame);
    }

    public static C2SDragon decode(FriendlyByteBuf buffer) {
        return new C2SDragon(buffer.readBoolean());
    }

    public static void handle(C2SDragon message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> UoMoves.flame(player, message.flame() && !com.hexhaki.piece.server.Powers.off(player))); // 3.5.9
        }
        context.setPacketHandled(true);
    }
}
