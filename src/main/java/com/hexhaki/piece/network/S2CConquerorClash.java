package com.hexhaki.piece.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 3.8.2: a Conqueror's Haki clash's state (server/ConquerorClash), to everyone near: its number, its {@code phase} ({@link #START} the
 * collision, {@link #TUG} the bar, {@link #WON} the bar at a side, {@link #END} the loser thrown, {@link #GONE} cut short), the two
 * players (A let the release out, B was charging), where each stands, the bubble's radius, the bar ({@code v}: 1 A's side won, -1
 * B's), their Ryou colours and Conqueror masteries, and its age (ticks).
 */
public record S2CConquerorClash(int id, int phase, int a, int b, double ax, double ay, double az, double bx, double by, double bz,
                                float radius, float v, int colorA, int colorB, int masteryA, int masteryB, int age) {
    public static final int START = 0;
    public static final int TUG = 1;
    public static final int WON = 2;
    public static final int END = 3;
    public static final int GONE = 4;

    public static void encode(S2CConquerorClash m, FriendlyByteBuf buffer) {
        buffer.writeInt(m.id);
        buffer.writeByte(m.phase);
        buffer.writeInt(m.a);
        buffer.writeInt(m.b);
        buffer.writeDouble(m.ax);
        buffer.writeDouble(m.ay);
        buffer.writeDouble(m.az);
        buffer.writeDouble(m.bx);
        buffer.writeDouble(m.by);
        buffer.writeDouble(m.bz);
        buffer.writeFloat(m.radius);
        buffer.writeFloat(m.v);
        buffer.writeInt(m.colorA);
        buffer.writeInt(m.colorB);
        buffer.writeShort(m.masteryA);
        buffer.writeShort(m.masteryB);
        buffer.writeShort(m.age);
    }

    public static S2CConquerorClash decode(FriendlyByteBuf buffer) {
        return new S2CConquerorClash(buffer.readInt(), buffer.readByte(), buffer.readInt(), buffer.readInt(), buffer.readDouble(),
            buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat(),
            buffer.readFloat(), buffer.readInt(), buffer.readInt(), buffer.readShort(), buffer.readShort(), buffer.readShort());
    }

    public static void handle(S2CConquerorClash message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> com.hexhaki.piece.client.haki.ConquerorClashFx.accept(message)));
        context.setPacketHandled(true);
    }
}
