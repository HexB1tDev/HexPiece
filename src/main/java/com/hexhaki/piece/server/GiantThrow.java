package com.hexhaki.piece.server;

import com.hexhaki.devilfruit.data.GearData;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 3.4.7 (the owner: "the giant, whenever it throws someone, it should be on a ... horizontal line, similar to the others. And it should
 * have our own custom effect whenever the entity crashes against something. Currently, it's very outdated. It's like, you gotta give it
 * your special touch"): the Gum-Gum Giant's throw. The original's {@code GiantGrab.release} threw the body along the look in an arc and
 * remembered it ("grabThrown") so its {@code tickThrown} set off a vanilla explosion (real, lasting block damage) where it hit. patch/
 * PiecePatch has release's two {@code GearData.integer(player, key, value)} calls ask {@link #integer} first: "grabThrown" is never kept
 * (the original's explosion never comes); the body is launched instead as the house's other throws are (PikaMoves.Launch, as Dawn Pistol's
 * and Dawn Whip's): flat along the giant's look, in a straight line for {@link #STRAIGHT} ticks at {@link #SPEED} blocks a tick, through
 * blocks up to obsidian (torn out through the self-repairing Scars, claims obeyed), then falling; white speed streaks behind it on every
 * client ({@link #FLY}); where it crashes into something or comes down, our crash ({@link #CRASH}: a big comic POW, earth bursting up,
 * the Gura fruit's dust, rings, rocks and cracks, a crater that grows back, the ground shaking, the Gura boom), with the Conqueror's
 * lightning out of it when the giant has the Conqueror's Haki and Ryou on. The original's damage, clip and sound at the throw stay.
 */
public final class GiantThrow {
    /** S2CUoFx kinds (the Nika fruit's; client/dragon/UoFx routes them to client/gear/NikaWhipClient): the flight, the crash. */
    public static final int FLY = 173;
    public static final int CRASH = 174;
    /** The launch: blocks a tick, straight for so many ticks (about 35 blocks), the hardest block torn through (obsidian). */
    public static final double SPEED = 3.5;
    public static final int STRAIGHT = 10;
    static final float HARDEST = 50.0F;
    /** The crater where it comes down (self-repairing). */
    static final double CRATER = 2.6;

    private GiantThrow() {
    }

    /** In GiantGrab.release, in place of {@code GearData.integer(player, key, value)}. */
    public static void integer(Player player, String key, int value) {
        if ("grabThrown".equals(key) && player instanceof ServerPlayer server) {
            GearData.integer(player, key, 0); // never kept: the original's tickThrown (its explosion) has nothing to follow
            try {
                Entity thrown = server.m_9236_().m_6815_(value);
                if (thrown instanceof LivingEntity body && body.m_6084_()) {
                    launch(server, body);
                }
            } catch (RuntimeException failure) {
                com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: the Giant's throw failed", failure);
            }
            return;
        }
        GearData.integer(player, key, value);
    }

    static void launch(ServerPlayer giant, LivingEntity body) {
        ServerLevel level = (ServerLevel) giant.m_9236_();
        Vec3 look = giant.m_20154_();
        double l = Math.sqrt(look.f_82479_ * look.f_82479_ + look.f_82481_ * look.f_82481_);
        Vec3 carry = l < 1.0E-4 ? new Vec3(0, 0, 1) : new Vec3(look.f_82479_ / l, 0, look.f_82481_ / l);
        PikaMoves.Launch launch = new PikaMoves.Launch(level, giant, body, carry, SPEED);
        launch.hardest = HARDEST;
        launch.straight = STRAIGHT;
        launch.landed = (owner, where) -> crash(owner, level, where, carry, body);
        launch.stopped = (owner, where) -> crash(owner, level, where, carry, body);
        NikaPistol.LIVE.add(launch);
        PieceNetwork.around(body, new S2CUoFx(FLY, body.m_19879_(), giant.m_19879_(), body.m_20185_(), body.m_20186_(), body.m_20189_(),
            (float) carry.f_82479_, 0, (float) carry.f_82481_, 0, 0, 0, body.m_20206_(), STRAIGHT));
    }

    /** Our crash where the thrown body hits something or comes down. */
    static void crash(ServerPlayer owner, ServerLevel level, Vec3 where, Vec3 carry, LivingEntity body) {
        double size = Math.max(1.0, Math.min(3.0, body.m_20206_()));
        Craters.open(level, owner, where, CRATER);
        GuraMoves.fx(owner, GuraMoves.QUAKE, 2, where, new Vec3(0, 1, 0), 0.9, 0, 0, 3.0, (int) (level.m_46467_() & 0xFFFF));
        ConquerorAura.exploded(owner, level, where, 3.0);
        com.hexhaki.fx.Impact.quake(level, where, 22.0, 0.7F, 12);
        com.hexhaki.piece.PieceSounds.playAt(owner, where.f_82479_, where.f_82480_, where.f_82481_, com.hexhaki.piece.gura.GuraSounds.BOOM, 1.6F,
            0.9F);
        com.hexhaki.piece.PieceSounds.playAt(owner, where.f_82479_, where.f_82480_, where.f_82481_, com.hexhaki.piece.gear.GearSounds.DAWN_PISTOL,
            1.0F, 0.8F);
        PieceNetwork.around(body, new S2CUoFx(CRASH, body.m_19879_(), owner.m_19879_(), where.f_82479_, where.f_82480_ + body.m_20206_() * 0.5,
            where.f_82481_, (float) carry.f_82479_, 0, (float) carry.f_82481_, 0, 0, 0, (float) size, (int) (level.m_46467_() & 0x7FFFFFFF)));
    }
}
