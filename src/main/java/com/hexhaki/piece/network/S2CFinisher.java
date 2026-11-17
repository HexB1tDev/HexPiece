package com.hexhaki.piece.network;

import com.hexhaki.piece.client.sword.FinisherFx;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * The Ryou finisher on a body (entity id), 2.2.1 HexGhoul's: {@link #FINISH} the line starts round it ({@code ticks} how long it takes),
 * {@link #SPLIT} it meets itself and the body is cut in two; {@code yaw} the body's (degrees), ({@code dx}, {@code dz}) the blade's way
 * (the cut is the upright plane along it).
 */
public record S2CFinisher(int entity, int kind, int ticks, float yaw, float dx, float dz) {
    public static final int FINISH = 0;
    public static final int SPLIT = 1;

    public static void encode(S2CFinisher message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entity);
        buffer.writeByte(message.kind);
        buffer.writeShort(message.ticks);
        buffer.writeFloat(message.yaw);
        buffer.writeFloat(message.dx);
        buffer.writeFloat(message.dz);
    }

    public static S2CFinisher decode(FriendlyByteBuf buffer) {
        return new S2CFinisher(buffer.readInt(), buffer.readByte(), buffer.readShort(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(S2CFinisher message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FinisherFx.accept(message)));
        context.setPacketHandled(true);
    }
}
