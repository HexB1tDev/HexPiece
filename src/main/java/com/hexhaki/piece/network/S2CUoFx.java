package com.hexhaki.piece.network;

import com.hexhaki.piece.client.dragon.UoFx;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 2.7.2: one moment of a Uo Uo no Mi move for every client that sees it (server/UoMoves -> client/dragon/UoFx): what ({@code kind}),
 * which ({@code id}, the move's own number), whose ({@code user}), where ({@code x, y, z}), and per kind two more points or
 * directions ({@code a}, {@code b}), a size and a count of ticks.
 */
public record S2CUoFx(int kind, int id, int user, double x, double y, double z, float ax, float ay, float az, float bx, float by, float bz,
                      float size, int ticks) {
    /** A shot of air (Air Pressure): a = its velocity (blocks a tick), size its width, ticks its flight. */
    public static final int AIR = 1;
    /** A line of fire (Blast Breath, human): as AIR. */
    public static final int BOLT = 2;
    /** The dragon's charged fireball (Blast Breath, dragon): as AIR. */
    public static final int BLAST = 3;
    /** A shot bursting: a = the way it was going, bz = the shot's kind, size its radius. */
    public static final int BURST = 4;
    /** Wind Force's tornado: x, y, z its base, a its step a tick, ticks its life. */
    public static final int TORNADO = 5;
    /** One of Dragon Embers' fireballs: x, y, z the mouth, a the top of its flight, b where it lands, ticks delay << 16 | rise << 8 | fall. */
    public static final int EMBER = 6;
    /** The dragon's breath on and off (user). */
    public static final int FLAME_ON = 7;
    public static final int FLAME_OFF = 8;
    /** The charged blast gathering in the dragon's mouth, and let go (user). */
    public static final int CHARGE_ON = 9;
    public static final int CHARGE_OFF = 10;
    /** The dragon's bite (user). */
    public static final int BITE = 11;
    /**
     * 2.7.8: a beam of fire (Blast Breath, both forms) on: user, id 1 the dragon's (0 the human's), size its width (radius), ax its
     * length, ticks its life; and cut short (user).
     */
    public static final int FIRE_BEAM = 12;
    public static final int FIRE_BEAM_OFF = 13;
    /**
     * 2.9.0: a BURST's kind (bz) for Blast Breath's last explosion: the bomb (dragon/UoFire.bomb), its mushroom cloud and the burning
     * ground it leaves for {@code UoFire.BOMB_LIFE} ticks; size its radius.
     */
    public static final int BOMB = 14;

    /** A kind with no place (the user's own dragon). */
    public static S2CUoFx of(int kind, int id, int user) {
        return new S2CUoFx(kind, id, user, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public static void encode(S2CUoFx m, FriendlyByteBuf buffer) {
        buffer.writeByte(m.kind);
        buffer.writeInt(m.id);
        buffer.writeInt(m.user);
        buffer.writeDouble(m.x);
        buffer.writeDouble(m.y);
        buffer.writeDouble(m.z);
        buffer.writeFloat(m.ax);
        buffer.writeFloat(m.ay);
        buffer.writeFloat(m.az);
        buffer.writeFloat(m.bx);
        buffer.writeFloat(m.by);
        buffer.writeFloat(m.bz);
        buffer.writeFloat(m.size);
        buffer.writeInt(m.ticks);
    }

    public static S2CUoFx decode(FriendlyByteBuf buffer) {
        // 3.1.9: the kind read back unsigned (the Ice fruit's kinds run to 139, past a signed byte)
        return new S2CUoFx(buffer.readByte() & 0xFF, buffer.readInt(), buffer.readInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
            buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
            buffer.readFloat(), buffer.readInt());
    }

    public static void handle(S2CUoFx message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> UoFx.accept(message)));
        context.setPacketHandled(true);
    }
}
