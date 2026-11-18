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
 * when its move stopped short ({@link #SPILL}: {@code index} of them were fired); 2.3.9: the Brick Fist's shockwave ({@link #SHOCK}), a body
 * knocked off its feet ({@link #KNOCKED}).
 */
public record S2CWater(int kind, int id, int user, int index, int target, double x, double y, double z, float dx, float dy, float dz) {
    public static final int FIRE = 0;
    public static final int BURST = 1;
    public static final int FADE = 2;
    public static final int SPILL = 3;
    /** 2.3.9: the Brick Fist's shockwave rushing out of the fist at (x, y, z) along (dx, dy, dz). */
    public static final int SHOCK = 4;
    /** 2.3.9: the body {@code target} knocked off its feet, launched along (dx, dy, dz): a rag doll (client/fx/Tumbles). */
    public static final int KNOCKED = 5;
    /** 2.4.0: the Water Prison: the water gathering round {@code target} (the move began {@code WaterPrison.GATHER} ticks ago), it sealed
     *  in at (x, y, z) for {@code index} ticks, the bubble bursting ({@code index} 1) or let go of (0). */
    public static final int PRISON_GATHER = 6;
    public static final int PRISON_SEAL = 7;
    public static final int PRISON_BURST = 8;
    /** 2.4.1: Six Powers: a bullet of Haki-hardened air shot from a finger (its number, from (x, y, z), its velocity), and where it struck. */
    public static final int AIR_SHOT = 9;
    public static final int AIR_HIT = 10;
    /** 2.4.4: the Brick Fist's shockwave struck the body {@code target} at (x, y, z), rushing along (dx, dy, dz): a heavy splash on it. */
    public static final int SPLASH = 11;

    public static void encode(S2CWater message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.kind);
        buffer.writeInt(message.id);
        buffer.writeInt(message.user);
        buffer.writeShort(message.index);
        buffer.writeInt(message.target);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeFloat(message.dx);
        buffer.writeFloat(message.dy);
        buffer.writeFloat(message.dz);
    }

    public static S2CWater decode(FriendlyByteBuf buffer) {
        return new S2CWater(buffer.readByte(), buffer.readInt(), buffer.readInt(), buffer.readShort(), buffer.readInt(), buffer.readDouble(),
            buffer.readDouble(), buffer.readDouble(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(S2CWater message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> WaterFx.accept(message)));
        context.setPacketHandled(true);
    }
}
