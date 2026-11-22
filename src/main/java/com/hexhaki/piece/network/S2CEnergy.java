package com.hexhaki.piece.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 3.2.0 (the owner: "we're going to need to have an energy bar for the fruits, appearing over the food bar"; asked, "Moves spend it"): a
 * player's own fruit energy (tenths, 0 to 1000) for its own client's bar (client/EnergyBar); {@code denied}: a move was just refused for
 * want of energy (the bar flashes).
 */
public record S2CEnergy(int tenths, boolean denied) {
    public static void encode(S2CEnergy message, FriendlyByteBuf buffer) {
        buffer.writeShort(message.tenths);
        buffer.writeBoolean(message.denied);
    }

    public static S2CEnergy decode(FriendlyByteBuf buffer) {
        return new S2CEnergy(buffer.readShort(), buffer.readBoolean());
    }

    public static void handle(S2CEnergy message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.hexhaki.piece.client.EnergyBar.accept(message)));
        context.setPacketHandled(true);
    }
}
