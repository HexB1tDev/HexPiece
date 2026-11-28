package com.hexhaki.piece.network;

import com.hexhaki.piece.server.ItsuGates;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * 3.9.9: one click in the Hellish Teleport menu (client/itsumade/GateScreen -> server/ItsuGates.act): {@code action} BIND (this place
 * into the gate), CLEAR, RENAME (to {@code text}) or GO (open the gate) on gate {@code slot}.
 */
public record C2SGate(int action, int slot, String text) {
    public static final int BIND = 0;
    public static final int CLEAR = 1;
    public static final int RENAME = 2;
    public static final int GO = 3;

    public static void encode(C2SGate m, FriendlyByteBuf buffer) {
        buffer.writeByte(m.action);
        buffer.writeByte(m.slot);
        S2CGates.text(buffer, m.text);
    }

    public static C2SGate decode(FriendlyByteBuf buffer) {
        return new C2SGate(buffer.readByte(), buffer.readByte(), S2CGates.text(buffer));
    }

    public static void handle(C2SGate message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> ItsuGates.act(sender, message.action, message.slot, message.text));
        }
        context.setPacketHandled(true);
    }
}
