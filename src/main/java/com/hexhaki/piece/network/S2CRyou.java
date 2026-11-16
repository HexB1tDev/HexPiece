package com.hexhaki.piece.network;

import com.hexhaki.piece.client.haki.RyouColors;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 2.1.3: a player's Ryou glow colour (ARGB; 0 none yet) for every client that sees them: sent when it is given, at login, respawn and
 * a change of dimension, and to everyone who starts seeing them. Every Ryou effect of that player is drawn in it.
 */
public record S2CRyou(int entity, int color) {
    public static void encode(S2CRyou message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entity);
        buffer.writeInt(message.color);
    }

    public static S2CRyou decode(FriendlyByteBuf buffer) {
        return new S2CRyou(buffer.readInt(), buffer.readInt());
    }

    public static void handle(S2CRyou message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RyouColors.accept(message)));
        context.setPacketHandled(true);
    }
}
