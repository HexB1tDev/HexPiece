package com.hexhaki.piece.network;

import com.hexhaki.piece.client.sword.SlashRenderer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 2.1.4: a Haki slash for every client near it: where it starts, which way it flies (a unit vector), how tall its cutting plane is,
 * how far and fast it goes, its colour (the owner's saved Ryou colour, 0 for the neutral fallback) and when it set off (game tick mod
 * 2^30). Every client flies it the same way the server does; {@code range} sent again shorter ends it early.
 */
public record S2CSlash(int id, double x, double y, double z, float dx, float dy, float dz, float height, float range, float speed,
                       int color, int start) {
    public static void encode(S2CSlash message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.id);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeFloat(message.dx);
        buffer.writeFloat(message.dy);
        buffer.writeFloat(message.dz);
        buffer.writeFloat(message.height);
        buffer.writeFloat(message.range);
        buffer.writeFloat(message.speed);
        buffer.writeInt(message.color);
        buffer.writeInt(message.start);
    }

    public static S2CSlash decode(FriendlyByteBuf buffer) {
        return new S2CSlash(buffer.readInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat(),
            buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readInt(),
            buffer.readInt());
    }

    public static void handle(S2CSlash message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SlashRenderer.accept(message)));
        context.setPacketHandled(true);
    }
}
