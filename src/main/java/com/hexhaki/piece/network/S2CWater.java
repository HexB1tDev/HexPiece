package com.hexhaki.piece.network;

import com.hexhaki.piece.client.fx.WaterFx;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 2.3.8: Fishman Karate's water as it happens ({@code server/WaterServer}): a drop fired ({@link #FIRE}: its number, its user, which of the
 * five it was, the body it is locked onto (-1: none), where it set out, its velocity), a drop bursting ({@link #BURST}: where, the way it
 * flew, the body it struck or -1), a drop falling apart at the end of its flight ({@link #FADE}), the drops still over a fist falling
 * when its move stopped short ({@link #SPILL}: {@code index} of them were fired).
 */
public record S2CWater(int kind, int id, int user, int index, int target, double x, double y, double z, float dx, float dy, float dz) {
    public static final int FIRE = 0;
    public static final int BURST = 1;
    public static final int FADE = 2;
    public static final int SPILL = 3;

    public static void encode(S2CWater message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.kind);
        buffer.writeInt(message.id);
        buffer.writeInt(message.user);
        buffer.writeByte(message.index);
        buffer.writeInt(message.target);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeFloat(message.dx);
        buffer.writeFloat(message.dy);
        buffer.writeFloat(message.dz);
    }

    public static S2CWater decode(FriendlyByteBuf buffer) {
        return new S2CWater(buffer.readByte(), buffer.readInt(), buffer.readInt(), buffer.readByte(), buffer.readInt(), buffer.readDouble(),
            buffer.readDouble(), buffer.readDouble(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(S2CWater message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> WaterFx.accept(message)));
        context.setPacketHandled(true);
    }
}
