package com.hexhaki.piece.network;

import com.hexhaki.piece.client.StanceLayer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 2.1.2: a player's combat mode as everyone near sees it: the player (entity id) and the style whose stance they hold, or -1 when they
 * leave combat mode. Sent when it changes and to everyone who starts seeing a player in combat mode.
 */
public record S2CStance(int entity, int style) {
    public static void encode(S2CStance message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entity);
        buffer.writeByte(message.style);
    }

    public static S2CStance decode(FriendlyByteBuf buffer) {
        return new S2CStance(buffer.readInt(), buffer.readByte());
    }

    public static void handle(S2CStance message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> StanceLayer.accept(message)));
        context.setPacketHandled(true);
    }
}
