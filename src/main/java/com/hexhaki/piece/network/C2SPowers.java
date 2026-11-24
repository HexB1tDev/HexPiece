package com.hexhaki.piece.network;

import com.hexhaki.piece.server.Powers;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** 3.5.9 (the owner: "HOLDING R will now fully disable all powers and abilities (but fruit passives)"): R held, every power off or on. */
public record C2SPowers(boolean off) {
    public static void encode(C2SPowers message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.off);
    }

    public static C2SPowers decode(FriendlyByteBuf buffer) {
        return new C2SPowers(buffer.readBoolean());
    }

    public static void handle(C2SPowers message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> Powers.set(sender, message.off));
        }
        context.setPacketHandled(true);
    }
}
