package com.hexhaki.piece.network;

import com.hexhaki.piece.client.sword.SwordClient;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 2.1.4: a player's sword as everyone near sees it: the entity, its active katana (ordinal, -1 none: nothing at the hip), what it is
 * doing ({@code sword/SwordAnims}: 0 sheathed, the guard, a cut, the charge, the release, the sheathing) and the game tick that began
 * (mod 2^30), so every client plays the same animation in step and shows the blade in the saya or in the hand at the same ticks.
 */
public record S2CSword(int entity, int katana, int anim, int start) {
    public static void encode(S2CSword message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entity);
        buffer.writeByte(message.katana);
        buffer.writeShort(message.anim);
        buffer.writeInt(message.start);
    }

    public static S2CSword decode(FriendlyByteBuf buffer) {
        return new S2CSword(buffer.readInt(), buffer.readByte(), buffer.readShort(), buffer.readInt());
    }

    public static void handle(S2CSword message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SwordClient.accept(message)));
        context.setPacketHandled(true);
    }
}
