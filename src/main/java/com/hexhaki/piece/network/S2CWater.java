package com.hexhaki.piece.network;

import com.hexhaki.piece.client.fx.WaterFx;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 2.3.8: Fishman Karate's water as it happens. 3.5.7 (the owner: "Remove FIshman karate from the game entirely"): its drops, prisons and
 * shockwaves are gone; left: a body knocked off its feet ({@link #KNOCKED}: server/Knockdowns, client/fx/Tumbles) and the Six Powers'
 * air bullets ({@link #AIR_SHOT}, {@link #AIR_HIT}).
 */
public record S2CWater(int kind, int id, int user, int index, int target, double x, double y, double z, float dx, float dy, float dz) {
    /** 2.3.9: the body {@code target} knocked off its feet, launched along (dx, dy, dz): a rag doll (client/fx/Tumbles). */
    public static final int KNOCKED = 5;
    /** 2.4.1: Six Powers: a bullet of Haki-hardened air shot from a finger (its number, from (x, y, z), its velocity), and where it struck. */
    public static final int AIR_SHOT = 9;
    public static final int AIR_HIT = 10;

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
