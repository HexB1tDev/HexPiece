package com.hexhaki.piece.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 3.9.9: an Itsumade eater's three Hellish Teleport gates (server/ItsuGates) told to their own client, and the menu opened
 * ({@code open}) or kept up to date (client/itsumade/GateScreen): each gate's name, whether it is bound, where (block x, y, z) and in
 * which world; the world the player is in now; how many ticks are left before a gate opens again.
 */
public record S2CGates(boolean open, boolean[] set, String[] names, int[] xs, int[] ys, int[] zs, String[] worlds, String here, int cooldown) {
    public static final int SLOTS = 3;

    public static void encode(S2CGates m, FriendlyByteBuf buffer) {
        buffer.writeBoolean(m.open);
        for (int i = 0; i < SLOTS; i++) {
            buffer.writeBoolean(m.set[i]);
            text(buffer, m.names[i]);
            buffer.writeInt(m.xs[i]);
            buffer.writeInt(m.ys[i]);
            buffer.writeInt(m.zs[i]);
            text(buffer, m.worlds[i]);
        }
        text(buffer, m.here);
        buffer.writeInt(m.cooldown);
    }

    public static S2CGates decode(FriendlyByteBuf buffer) {
        boolean open = buffer.readBoolean();
        boolean[] set = new boolean[SLOTS];
        String[] names = new String[SLOTS];
        int[] xs = new int[SLOTS];
        int[] ys = new int[SLOTS];
        int[] zs = new int[SLOTS];
        String[] worlds = new String[SLOTS];
        for (int i = 0; i < SLOTS; i++) {
            set[i] = buffer.readBoolean();
            names[i] = text(buffer);
            xs[i] = buffer.readInt();
            ys[i] = buffer.readInt();
            zs[i] = buffer.readInt();
            worlds[i] = text(buffer);
        }
        String here = text(buffer);
        return new S2CGates(open, set, names, xs, ys, zs, worlds, here, buffer.readInt());
    }

    /** A short text (at most 64 characters): its length, then each character. */
    static void text(FriendlyByteBuf buffer, String s) {
        String t = s == null ? "" : s.length() > 64 ? s.substring(0, 64) : s;
        buffer.writeByte(t.length());
        for (int i = 0; i < t.length(); i++) {
            buffer.writeShort(t.charAt(i));
        }
    }

    static String text(FriendlyByteBuf buffer) {
        int n = Math.min(64, buffer.readByte() & 0xFF);
        StringBuilder out = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            out.append((char) buffer.readShort());
        }
        return out.toString();
    }

    public static void handle(S2CGates message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.hexhaki.piece.client.itsumade.GateScreen.told(message)));
        context.setPacketHandled(true);
    }
}
