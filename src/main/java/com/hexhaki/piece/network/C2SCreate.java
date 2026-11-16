package com.hexhaki.piece.network;

import com.hexhaki.piece.Faction;
import com.hexhaki.piece.Gender;
import com.hexhaki.piece.server.ProfileServer;
import com.hexhaki.piece.style.FightingStyle;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** The start menu's picks: gender, side and fighting style (ordinals). The server checks each is a real choice. */
public record C2SCreate(int gender, int faction, int style) {
    public static void encode(C2SCreate message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.gender);
        buffer.writeByte(message.faction);
        buffer.writeByte(message.style);
    }

    public static C2SCreate decode(FriendlyByteBuf buffer) {
        return new C2SCreate(buffer.readByte(), buffer.readByte(), buffer.readByte());
    }

    public static void handle(C2SCreate message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            context.enqueueWork(() -> ProfileServer.create(sender, Gender.byOrdinal(message.gender), Faction.byOrdinal(message.faction),
                FightingStyle.byOrdinal(message.style)));
        }
        context.setPacketHandled(true);
    }
}
