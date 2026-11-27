package com.hexhaki.piece.network;

import com.hexhaki.piece.client.gun.GunBullets;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 3.8.8: a gun's ball, flown on the server ({@code server/GunShots}, no entity) and drawn by every client near it
 * ({@code client/gun/GunBullets}). {@link #FLY}: it left the muzzle at (x, y, z) at (vx, vy, vz) blocks a tick (falling as
 * {@code gun/GunRules} says, a Haki ball straight), fired by {@code shooter}; {@link #STOP}: it stopped at (x, y, z), {@code vx} what
 * it struck ({@link #AIR}, {@link #BLOCK}, {@link #BODY}).
 */
public record S2CGunShot(int kind, int id, int shooter, double x, double y, double z, double vx, double vy, double vz, boolean haki) {
    public static final int FLY = 0;
    public static final int STOP = 1;
    public static final int AIR = 0;
    public static final int BLOCK = 1;
    public static final int BODY = 2;

    public static void encode(S2CGunShot message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.kind);
        buffer.writeInt(message.id);
        buffer.writeInt(message.shooter);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeFloat((float) message.vx);
        buffer.writeFloat((float) message.vy);
        buffer.writeFloat((float) message.vz);
        buffer.writeBoolean(message.haki);
    }

    public static S2CGunShot decode(FriendlyByteBuf buffer) {
        return new S2CGunShot(buffer.readByte(), buffer.readInt(), buffer.readInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
            buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean());
    }

    public static void handle(S2CGunShot message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> GunBullets.accept(message)));
        context.setPacketHandled(true);
    }
}
