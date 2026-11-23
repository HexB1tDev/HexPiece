package com.hexhaki.piece.network;

import com.hexhaki.piece.server.ItoMoves;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * 3.5.2: Sky Road's string thrown, let go or stepped on, as the runner's own client moves it (client/ito/SkyRoad) -> server/ItoMoves
 * .skyString, which checks it and passes it on to everyone near (S2CUoFx SKY_STRING) so every client draws the same string: {@code mode}
 * ItoMoves.ROPE_SWING / ROPE_HANG (x, y, z the anchor, {@code length} the string's), SKY_FLY ({@code extra} the trick: let go, leaping,
 * dropping) or SKY_STEP (x, y, z the step, {@code length} the way it faces, {@code extra} the foot).
 */
public record C2SItoSky(int mode, double x, double y, double z, float length, int extra) {
    public static void encode(C2SItoSky m, FriendlyByteBuf buffer) {
        buffer.writeByte(m.mode);
        buffer.writeDouble(m.x);
        buffer.writeDouble(m.y);
        buffer.writeDouble(m.z);
        buffer.writeFloat(m.length);
        buffer.writeByte(m.extra);
    }

    public static C2SItoSky decode(FriendlyByteBuf buffer) {
        return new C2SItoSky(buffer.readByte(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat(), buffer.readByte());
    }

    public static void handle(C2SItoSky message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> ItoMoves.skyString(player, message.mode, message.x, message.y, message.z, message.length, message.extra));
        }
        context.setPacketHandled(true);
    }
}
