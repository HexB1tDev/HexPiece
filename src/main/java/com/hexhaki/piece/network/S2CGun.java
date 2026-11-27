package com.hexhaki.piece.network;

import com.hexhaki.piece.client.gun.GunClient;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 3.8.8: a player's gun as everyone near sees it: the entity, the gun in the hand (ordinal, -1 none), what it is doing
 * ({@code gun/GunAnims}) and the game tick that began (mod 2^30), so every client plays the same clip in step and shows the ball
 * going into the muzzle at the same ticks.
 */
public record S2CGun(int entity, int gun, int anim, int start) {
    public static void encode(S2CGun message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entity);
        buffer.writeByte(message.gun);
        buffer.writeByte(message.anim);
        buffer.writeInt(message.start);
    }

    public static S2CGun decode(FriendlyByteBuf buffer) {
        return new S2CGun(buffer.readInt(), buffer.readByte(), buffer.readByte(), buffer.readInt());
    }

    public static void handle(S2CGun message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> GunClient.accept(message)));
        context.setPacketHandled(true);
    }
}
