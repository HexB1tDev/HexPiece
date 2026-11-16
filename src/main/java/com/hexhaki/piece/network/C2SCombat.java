package com.hexhaki.piece.network;

import com.hexhaki.piece.server.StyleServer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** 2.1.2: combat mode on or off (R, as in HexGhoul): the fighting style's moves only come out while it is on. */
public record C2SCombat(boolean on) {
    public static void encode(C2SCombat message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.on);
    }

    public static C2SCombat decode(FriendlyByteBuf buffer) {
        return new C2SCombat(buffer.readBoolean());
    }

    public static void handle(C2SCombat message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> StyleServer.combat(sender, message.on));
        }
        context.setPacketHandled(true);
    }
}
