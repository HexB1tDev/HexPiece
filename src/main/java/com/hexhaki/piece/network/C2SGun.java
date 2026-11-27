package com.hexhaki.piece.network;

import com.hexhaki.piece.server.GunServer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

/**
 * 3.8.8: M1 with a gun in the main hand (client/gun/GunInput): fire, at the point under the crosshair (what the third-person camera's
 * centre line meets, up to the gun's range), so the ball goes where the player aims although the camera is over the shoulder. The
 * server decides everything ({@link GunServer}: loaded, the aim near the look).
 */
public record C2SGun(double x, double y, double z) {
    public static void encode(C2SGun message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
    }

    public static C2SGun decode(FriendlyByteBuf buffer) {
        return new C2SGun(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(C2SGun message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> {
                if (!com.hexhaki.piece.server.HoroMoves.astral(sender)) {
                    GunServer.fire(sender, new Vec3(message.x, message.y, message.z));
                }
            });
        }
        context.setPacketHandled(true);
    }
}
