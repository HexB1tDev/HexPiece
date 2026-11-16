package com.hexhaki.piece.network;

import com.hexhaki.piece.client.sword.FinisherFx;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** 2.1.4: the Ryou finisher on a victim (entity id), from a game tick (mod 2^30): held still, then the red line, then halved. */
public record S2CFinisher(int entity, int start, int freeze, int trace, float dx, float dz) {
    public static void encode(S2CFinisher message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entity);
        buffer.writeInt(message.start);
        buffer.writeShort(message.freeze);
        buffer.writeShort(message.trace);
        buffer.writeFloat(message.dx);
        buffer.writeFloat(message.dz);
    }

    public static S2CFinisher decode(FriendlyByteBuf buffer) {
        return new S2CFinisher(buffer.readInt(), buffer.readInt(), buffer.readShort(), buffer.readShort(), buffer.readFloat(),
            buffer.readFloat());
    }

    public static void handle(S2CFinisher message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FinisherFx.accept(message)));
        context.setPacketHandled(true);
    }
}
