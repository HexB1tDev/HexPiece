package com.hexhaki.piece.network;

import com.hexhaki.piece.client.PieceClient;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * A player's HexPiece profile as a client sees it: gender, side and style for everyone that player is seen by; the masteries, renown,
 * whether the start menu was done and (2.4.6) the alignment only for the player themselves ({@code full}).
 */
public record S2CProfile(int entity, int gender, int faction, int style, boolean full, boolean chosen, int[] mastery, long renown, int alignment) {
    public static void encode(S2CProfile message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entity);
        buffer.writeByte(message.gender);
        buffer.writeByte(message.faction);
        buffer.writeByte(message.style);
        buffer.writeBoolean(message.full);
        if (message.full) {
            buffer.writeBoolean(message.chosen);
            buffer.writeByte(message.mastery.length);
            for (int value : message.mastery) {
                buffer.writeShort(value);
            }
            buffer.m_130103_(message.renown);
            buffer.writeByte(message.alignment);
        }
    }

    public static S2CProfile decode(FriendlyByteBuf buffer) {
        int entity = buffer.readInt();
        int gender = buffer.readByte();
        int faction = buffer.readByte();
        int style = buffer.readByte();
        boolean full = buffer.readBoolean();
        if (!full) {
            return new S2CProfile(entity, gender, faction, style, false, false, new int[0], 0L, 0);
        }
        boolean chosen = buffer.readBoolean();
        int[] mastery = new int[Math.max(0, Math.min(32, buffer.readByte()))];
        for (int i = 0; i < mastery.length; i++) {
            mastery[i] = buffer.readShort();
        }
        long renown = buffer.m_130258_();
        return new S2CProfile(entity, gender, faction, style, true, chosen, mastery, renown, buffer.readByte());
    }

    public static void handle(S2CProfile message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PieceClient.accept(message)));
        context.setPacketHandled(true);
    }
}
