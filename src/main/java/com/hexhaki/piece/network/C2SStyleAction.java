package com.hexhaki.piece.network;

import com.hexhaki.piece.server.StyleServer;
import com.hexhaki.piece.style.Slot;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** A fighting-style input: M1 (any string slot: the server picks the blow), the right click, or a special key ({@link Slot} ordinal). */
public record C2SStyleAction(int slot) {
    public static void encode(C2SStyleAction message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.slot);
    }

    public static C2SStyleAction decode(FriendlyByteBuf buffer) {
        return new C2SStyleAction(buffer.readByte());
    }

    public static void handle(C2SStyleAction message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> StyleServer.press(sender, Slot.byOrdinal(message.slot)));
        }
        context.setPacketHandled(true);
    }
}
