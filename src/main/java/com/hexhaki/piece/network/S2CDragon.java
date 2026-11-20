package com.hexhaki.piece.network;

import com.hexhaki.piece.client.dragon.DragonClient;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** 2.7.0: a player's dragon form (dragon/DragonForm's phase) and how many ticks into it, to everyone who sees them. */
public record S2CDragon(int id, int phase, int ticks) {
    public static void encode(S2CDragon message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.id);
        buffer.writeByte(message.phase);
        buffer.writeInt(message.ticks);
    }

    public static S2CDragon decode(FriendlyByteBuf buffer) {
        return new S2CDragon(buffer.readInt(), buffer.readByte(), buffer.readInt());
    }

    public static void handle(S2CDragon message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DragonClient.accept(message)));
        context.setPacketHandled(true);
    }
}
