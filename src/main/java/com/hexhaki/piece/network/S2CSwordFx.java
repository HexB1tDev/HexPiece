package com.hexhaki.piece.network;

import com.hexhaki.piece.client.sword.SwordFx;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 2.1.4: a moment of a sword fight for every client near it: a cut landing (an edge glint and sparks at the point, the cut's line),
 * the charged cut's release, a slash's end. {@code kind} ({@code SwordFx}), a point, a direction and a colour (ARGB; 0 steel).
 */
public record S2CSwordFx(int kind, double x, double y, double z, double dx, double dy, double dz, int color) {
    public static final int HIT = 0;
    public static final int RELEASE = 1;
    public static final int QUICK = 2;

    public static void encode(S2CSwordFx message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.kind);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeFloat((float) message.dx);
        buffer.writeFloat((float) message.dy);
        buffer.writeFloat((float) message.dz);
        buffer.writeInt(message.color);
    }

    public static S2CSwordFx decode(FriendlyByteBuf buffer) {
        return new S2CSwordFx(buffer.readByte(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat(),
            buffer.readFloat(), buffer.readFloat(), buffer.readInt());
    }

    public static void handle(S2CSwordFx message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SwordFx.accept(message)));
        context.setPacketHandled(true);
    }
}
