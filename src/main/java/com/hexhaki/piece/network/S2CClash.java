package com.hexhaki.piece.network;

import com.hexhaki.piece.client.fx.ClashFx;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 2.3.6: two blows met ({@code server/Clashes}): a number for the clash (a viewer sent it twice plays it once), where they met, the line
 * they met on (across the ground, from the first toward the second), how hard ({@code ClashRules.power}), each side's Ryou colour (0:
 * none), the two bodies, and what struck ({@link #BLADE_A}, {@link #BLADE_B}) and whether one was overwhelmed.
 */
public record S2CClash(int id, double x, double y, double z, float dx, float dz, float power, int ryouA, int ryouB, int a, int b,
                       int flags) {
    public static final int BLADE_A = 1;
    public static final int BLADE_B = 2;
    public static final int OVERWHELMED = 4;
    /** 3.5.8: the old impact frame (clash/ImpactFrame), the Gura ultimate's alone; every other is the owner's (clash/ImpactSprite). */
    public static final int CLASSIC = 8;

    public boolean classic() {
        return (this.flags & CLASSIC) != 0;
    }

    public boolean blades() {
        return (this.flags & (BLADE_A | BLADE_B)) == (BLADE_A | BLADE_B);
    }

    public boolean steel() {
        return (this.flags & (BLADE_A | BLADE_B)) != 0;
    }

    public static void encode(S2CClash message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.id);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeFloat(message.dx);
        buffer.writeFloat(message.dz);
        buffer.writeFloat(message.power);
        buffer.writeInt(message.ryouA);
        buffer.writeInt(message.ryouB);
        buffer.writeInt(message.a);
        buffer.writeInt(message.b);
        buffer.writeByte(message.flags);
    }

    public static S2CClash decode(FriendlyByteBuf buffer) {
        return new S2CClash(buffer.readInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat(),
            buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readByte());
    }

    public static void handle(S2CClash message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClashFx.accept(message)));
        context.setPacketHandled(true);
    }
}
