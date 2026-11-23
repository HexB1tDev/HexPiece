package com.hexhaki.piece.network;

import com.hexhaki.piece.server.NikaWhip;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * 3.4.4: C let go in Devil Fruit mode (client/gear/NikaKeys; the original only ever sends C's press) -> server/NikaWhip.letGo: Goof Ball
 * is held and ends when C is let go.
 */
public record C2SNikaWhip() {
    public static void encode(C2SNikaWhip m, FriendlyByteBuf buffer) {
    }

    public static C2SNikaWhip decode(FriendlyByteBuf buffer) {
        return new C2SNikaWhip();
    }

    public static void handle(C2SNikaWhip message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> NikaWhip.letGo(player));
        }
        context.setPacketHandled(true);
    }
}
