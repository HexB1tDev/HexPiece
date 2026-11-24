package com.hexhaki.piece.network;

import com.hexhaki.piece.server.SwordServer;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * 2.1.4: a Swordsman input: one M1 press (one cut asked for), the right button held (charge) and let go (release) or the charge given
 * up (a screen opened), or Ctrl + M1 (the quick draw). The server decides everything ({@link SwordServer}).
 */
public record C2SSword(int action) {
    public static final int PRESS = 0;
    public static final int CHARGE = 1;
    public static final int RELEASE = 2;
    public static final int CANCEL = 3;
    public static final int QUICK_DRAW = 4;

    public static void encode(C2SSword message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.action);
    }

    public static C2SSword decode(FriendlyByteBuf buffer) {
        return new C2SSword(buffer.readByte());
    }

    public static void handle(C2SSword message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> {
                if (!com.hexhaki.piece.server.HoroMoves.astral(sender) && !com.hexhaki.piece.server.Powers.off(sender)) { // 3.4.2, 3.5.9
                    SwordServer.input(sender, message.action);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
