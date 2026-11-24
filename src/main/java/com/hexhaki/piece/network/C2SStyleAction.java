package com.hexhaki.piece.network;

import com.hexhaki.piece.server.StyleServer;
import com.hexhaki.piece.style.Slot;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * A fighting-style input: M1 (any string slot: the server picks the blow), the right click, or a special key ({@link Slot} ordinal).
 * 3.5.8: Iron Fist's also a key let go (its ordinal + server/IronFist.RELEASE: Meteor Fist's hold) and N (IronFist.N_KEY: Galaxy Impact).
 */
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
            context.enqueueWork(() -> {
                // 3.4.2 (the owner, on the Horo fruit's Astral Projection: "in this form the user can do everything normal from minecraft,
                // but cant use style or haki at all")
                if (!com.hexhaki.piece.server.HoroMoves.astral(sender) && !com.hexhaki.piece.server.Powers.off(sender)) { // 3.5.9: powers off
                    if (message.slot >= com.hexhaki.piece.server.IronFist.RELEASE) {
                        StyleServer.ironInput(sender, message.slot);
                    } else {
                        StyleServer.press(sender, Slot.byOrdinal(message.slot));
                    }
                }
            });
        }
        context.setPacketHandled(true);
    }
}
