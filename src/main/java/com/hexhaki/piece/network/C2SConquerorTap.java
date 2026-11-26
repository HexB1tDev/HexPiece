package com.hexhaki.piece.network;

import com.hexhaki.piece.server.ConquerorClash;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** 3.8.2: M1 tapped in a Conqueror's Haki clash (client/haki/ConquerorClashFx -> server/ConquerorClash.tap). */
public record C2SConquerorTap() {
    public static void encode(C2SConquerorTap m, FriendlyByteBuf buffer) {
    }

    public static C2SConquerorTap decode(FriendlyByteBuf buffer) {
        return new C2SConquerorTap();
    }

    public static void handle(C2SConquerorTap message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> ConquerorClash.tap(player));
        }
        context.setPacketHandled(true);
    }
}
