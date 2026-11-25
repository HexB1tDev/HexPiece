package com.hexhaki.piece.network;

import com.hexhaki.piece.server.UoMoves;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * 2.7.2: M1 held down (or let go) in dragon form: the dragon's fire breath (client/dragon/DragonInput -> server/UoMoves.flame). 3.7.7: as the
 * phoenix, its flamethrower (client/phoenix/PhoenixFx -> server/PhoenixMoves.flame).
 */
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
            context.enqueueWork(() -> {
                boolean on = message.flame() && !com.hexhaki.piece.server.Powers.off(player); // 3.5.9
                UoMoves.flame(player, on);
                // 3.7.7: the phoenix's M1 held, its flamethrower (each returns at once for the other's form)
                com.hexhaki.piece.server.PhoenixMoves.flame(player, on);
            });
        }
        context.setPacketHandled(true);
    }
}
