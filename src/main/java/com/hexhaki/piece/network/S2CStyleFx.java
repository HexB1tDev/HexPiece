package com.hexhaki.piece.network;

import com.hexhaki.piece.client.StyleFx;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * What a style move looks like as it happens, for every client near it: the move (ordinal), its stage (0 started, 1 a blow landed, 2 a
 * shot fired, 3 a blow thrown: dy carries its weight, + 10 all round), the user, and a point (where the blow landed, a shot's end, the
 * thrower's feet) and direction (a shot's start).
 */
public record S2CStyleFx(int move, int stage, int entity, double x, double y, double z, double dx, double dy, double dz) {
    public static void encode(S2CStyleFx message, FriendlyByteBuf buffer) {
        buffer.writeShort(message.move);
        buffer.writeByte(message.stage);
        buffer.writeInt(message.entity);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeFloat((float) message.dx);
        buffer.writeFloat((float) message.dy);
        buffer.writeFloat((float) message.dz);
    }

    public static S2CStyleFx decode(FriendlyByteBuf buffer) {
        return new S2CStyleFx(buffer.readShort(), buffer.readByte(), buffer.readInt(), buffer.readDouble(), buffer.readDouble(),
            buffer.readDouble(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(S2CStyleFx message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> StyleFx.accept(message)));
        context.setPacketHandled(true);
    }
}
