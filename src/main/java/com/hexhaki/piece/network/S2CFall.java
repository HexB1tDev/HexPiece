package com.hexhaki.piece.network;

import com.hexhaki.piece.client.fx.Tumbles;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 2.4.4: a Haki stun knocked the body {@code id} down ({@code server/HakiFalls}): it falls the way ({@code dx}, {@code dz}) (on the
 * ground, unit), lies there {@code down} ticks and gets up ({@code haki/StunFall}); {@code seed} picks the way its head lolls and its
 * twitches. Sent again with a longer {@code down} when a new stun lands on a body already down (its fall is not started over).
 */
public record S2CFall(int id, float dx, float dz, int down, long seed) {
    public static void encode(S2CFall message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.id);
        buffer.writeFloat(message.dx);
        buffer.writeFloat(message.dz);
        buffer.writeInt(message.down);
        buffer.m_130103_(message.seed); // writeVarLong
    }

    public static S2CFall decode(FriendlyByteBuf buffer) {
        return new S2CFall(buffer.readInt(), buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.m_130258_()); // readVarLong
    }

    public static void handle(S2CFall message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Tumbles.fall(message)));
        context.setPacketHandled(true);
    }
}
