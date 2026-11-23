package com.hexhaki.piece.server;

import com.hexhaki.devilfruit.data.GearData;
import com.hexhaki.piece.gear.NikaHold;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 3.2.8 (the owner: "Giant is not holding the creatures right. Sometimes they get off the player's hand. It's overall really junky"):
 * the Gum-Gum Giant's grab, held in the fist. The original's GiantGrab moved the body to its hand on the server only; patch/PiecePatch
 * (giantHold) has it ask here: {@link #hand} in place of its own hand(...) (the fist as the model draws it, gear/NikaHold),
 * {@link #grabbed} as it grabs, {@link #holding} each tick it holds, {@link #letGo} as it lets go (throw, drop, or the body gone).
 * Every client then keeps the body in the giant's fist itself, every tick (client/gear/NikaHoldClient): {@link #HOLD} names who holds
 * whom (user the giant, id the body, ticks how long since the grab, x, y, z where it stood), {@link #LET_GO} ends it (user).
 */
public final class GiantHold {
    /** The Nika fruit's kinds of S2CUoFx: 160 to 179 (client/dragon/UoFx sends them to client/gear/NikaHoldClient). */
    public static final int HOLD = 160;
    public static final int LET_GO = 161;
    /** The original's GiantGrab.scale for a giant (its hand() times everything by it): the giant drawn four times the size. */
    public static final double SCALE = 4.0;

    private GiantHold() {
    }

    /** Where GiantGrab.tick puts the body each tick: its middle in the fist (its feet half its height under it). */
    public static Vec3 hand(ServerPlayer player, LivingEntity held) {
        double[] fist = new double[3];
        NikaHold.fist(player.m_20185_(), player.m_20186_(), player.m_20189_(), player.f_20883_, SCALE, SCALE, fist);
        return new Vec3(fist[0], fist[1] - held.m_20206_() * 0.5, fist[2]);
    }

    /** GiantGrab.grab has just taken {@code held}: everyone near is told, the body flying in from where it stands. */
    public static void grabbed(ServerPlayer player, LivingEntity held) {
        tell(player, held, 0);
    }

    /** Each tick GiantGrab.tick holds {@code held} (after it is moved): told again now and then, for anyone come near since. */
    public static void holding(ServerPlayer player, LivingEntity held) {
        int ticks = GearData.integer(player, "grabTicks");
        if (ticks % NikaHold.RESEND == 0) {
            tell(player, held, ticks);
        }
    }

    /** At the start of GiantGrab.clear: if it held a body, everyone near lets go of it. */
    public static void letGo(ServerPlayer player) {
        if (GearData.integer(player, "grabHeld") != 0) {
            PieceNetwork.around(player, S2CUoFx.of(LET_GO, 0, player.m_19879_()));
        }
    }

    private static void tell(ServerPlayer player, LivingEntity held, int ticks) {
        PieceNetwork.around(player, new S2CUoFx(HOLD, held.m_19879_(), player.m_19879_(), held.m_20185_(), held.m_20186_(), held.m_20189_(),
            0, 0, 0, 0, 0, 0, 0, ticks));
    }
}
