package com.hexhaki.piece.client.haki;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 3.9.5 (the owner: "when holding literally anything with Ryou + Conqueror's haki the lightning around the body gets transfered to that
 * item, this applies for our weapons especially. the Mace does not have lightning right now"): where a player's held item lies in the
 * world, for the Conqueror's lightning to tear off it instead of the body (client/haki/ConquerorSparksClient). A blade or a mace drawn by
 * client/sword/SwordLayer records its own line each frame (its guard to its point, exactly); anything else held is a short line out of
 * the fist along the look.
 */
public final class HeldLine {
    /** Player id -> {seconds, base x y z, tip x y z} (world). */
    private static final Map<Integer, double[]> LINES = new HashMap<>();
    /** A recorded line this old (seconds) is no longer the item's. */
    static final double FRESH = 0.3;
    /** Anything else held: this long (blocks) out of the fist. */
    static final double ITEM = 0.7;

    private HeldLine() {
    }

    /** client/sword/SwordLayer: the weapon drawn now, guard ({@code base}) to point ({@code tip}), world. */
    public static void record(int player, double[] base, double[] tip) {
        if (LINES.size() > 128) {
            LINES.clear();
        }
        LINES.put(player, new double[] {System.nanoTime() / 1.0E9, base[0], base[1], base[2], tip[0], tip[1], tip[2]});
    }

    /** The held item's line {base, tip} (world), or null when the hand is empty. */
    public static Vec3[] of(Player player) {
        double[] r = LINES.get(player.m_19879_());
        if (r != null && System.nanoTime() / 1.0E9 - r[0] < FRESH) {
            return new Vec3[] {new Vec3(r[1], r[2], r[3]), new Vec3(r[4], r[5], r[6])};
        }
        ItemStack held = player.m_21205_();
        if (held.m_41619_()) {
            return null;
        }
        Vec3 fist = com.hexhaki.piece.client.gura.GuraFx.fist(player, 1.0F);
        Vec3 way = player.m_20252_(1.0F).m_82520_(0.0, 0.45, 0.0).m_82541_();
        return new Vec3[] {fist, fist.m_82549_(way.m_82490_(ITEM))};
    }
}
