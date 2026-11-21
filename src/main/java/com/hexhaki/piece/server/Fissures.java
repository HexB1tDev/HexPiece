package com.hexhaki.piece.server;

import com.hexhaki.piece.PieceConfig;
import com.hexhaki.piece.haki.ConquerorStorm;
import com.hexhaki.piece.haki.Fissure;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.2.5 (the owner: "At full charge, it should cause severe tears on the floor like fissures where the lightnings hit, then repair
 * themselves automaticly (same as HexGhoul)"): the Conqueror's full-power release tears the ground open under its storm. The storm is
 * rebuilt here from the seed the clients were sent ({@link ConquerorRelease}), so the fissures run exactly under the bolts the players
 * see tearing along the ground ({@link Fissure#cuts}); each cut opens the tick the strike reaches it, found on the ground the way the
 * clients lay the storm on it (the first solid block under air, looking down from 14 above the player); its blocks are torn out by
 * {@link Scars} (HexGhoul's: nothing dropped, nothing with a block entity, protected blocks left alone, everything grown back 15 to 25
 * seconds later). Server config: {@code conqueror.fullChargeFissures}.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class Fissures {
    /** One release's fissures being torn open. */
    static final class Opening {
        ServerLevel level;
        ServerPlayer owner;
        double x;
        double y;
        double z;
        long start;
        List<Fissure.Cut> cuts;
        int next;
        final Map<Long, Double> heights = new HashMap<>();
        final List<double[]> supports = new ArrayList<>();
    }

    private static final List<Opening> OPEN = new ArrayList<>();

    private Fissures() {
    }

    /** The full-power release of {@code owner}, with the storm's seed and their Conqueror mastery: its fissures start opening. */
    static void open(ServerPlayer owner, long seed, int mastery) {
        if (!PieceConfig.get(PieceConfig.FISSURES, true) || !(owner.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Opening opening = new Opening();
        opening.level = level;
        opening.owner = owner;
        Vec3 at = owner.m_20182_();
        opening.x = at.f_82479_;
        opening.y = at.f_82480_;
        opening.z = at.f_82481_;
        opening.start = level.m_46467_();
        opening.cuts = Fissure.cuts(ConquerorStorm.release(mastery, seed, true), seed);
        // paintings and item frames anywhere the fissures can reach: their walls stay
        double reach = ConquerorStorm.reach(mastery) + 4.0;
        for (HangingEntity hanging : level.m_6443_(HangingEntity.class, new AABB(at.f_82479_ - reach, at.f_82480_ - 20, at.f_82481_ - reach,
            at.f_82479_ + reach, at.f_82480_ + 20, at.f_82481_ + reach), Entity::m_6084_)) {
            Vec3 p = hanging.m_20182_();
            opening.supports.add(new double[] {p.f_82479_, p.f_82480_, p.f_82481_});
        }
        OPEN.add(opening);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Iterator<Opening> it = OPEN.iterator();
        while (it.hasNext()) {
            Opening opening = it.next();
            try {
                if (step(opening)) {
                    it.remove();
                }
            } catch (RuntimeException failure) {
                it.remove(); // a fissure that fails is dropped, never the server
            }
        }
        Scars.tickAll();
    }

    /** Opens the cuts whose time has come, as far as this tick's budget goes; true once every cut is open. */
    static boolean step(Opening opening) {
        if (opening.owner.m_213877_() || opening.owner.m_9236_() != opening.level) {
            return true;
        }
        Scars scars = Scars.of(opening.level);
        long age = opening.level.m_46467_() - opening.start;
        while (opening.next < opening.cuts.size()) {
            Fissure.Cut cut = opening.cuts.get(opening.next);
            if (cut.tick() > age || !scars.room()) {
                return false;
            }
            cut(opening, scars, cut);
            opening.next++;
        }
        return true;
    }

    /** One slot: across the line, half a block at a time, each column torn down from the ground's top as deep as the V goes there. */
    static void cut(Opening opening, Scars scars, Fissure.Cut cut) {
        double cx = opening.x + cut.x();
        double cz = opening.z + cut.z();
        int across = Math.max(1, (int) Math.ceil(cut.half() * 2 / 0.5));
        for (int k = 0; k <= across; k++) {
            double o = -cut.half() + 2 * cut.half() * k / across;
            double depth = Fissure.depthAt(cut, o);
            if (depth < 0.5) {
                continue;
            }
            double wx = cx + cut.nx() * o;
            double wz = cz + cut.nz() * o;
            double top = ground(opening, wx, wz);
            int bx = (int) Math.floor(wx);
            int bz = (int) Math.floor(wz);
            int from = (int) Math.floor(top) - 1;
            for (int y = from; y > from - Math.ceil(depth); y--) {
                scars.carve(opening.owner, new BlockPos(bx, y, bz), opening.supports);
            }
        }
    }

    /**
     * The top of the ground in the column at (wx, wz) as the clients lay the storm on it ({@code client/haki/HakiBolts.ground}): the first
     * solid block under air, looking down from 14 above the release; remembered (the first answer stands while the fissure opens).
     */
    static double ground(Opening opening, double wx, double wz) {
        int bx = (int) Math.floor(wx);
        int bz = (int) Math.floor(wz);
        long key = (long) bx << 32 ^ (bz & 0xFFFFFFFFL);
        Double known = opening.heights.get(key);
        if (known != null) {
            return known;
        }
        int from = (int) Math.floor(opening.y) + 14;
        double found = opening.y;
        boolean air = false;
        for (int by = from; by >= from - 40; by--) {
            boolean solid = !opening.level.m_8055_(BlockPos.m_274561_(bx + 0.5, by + 0.5, bz + 0.5)).m_60795_();
            if (solid && air) {
                found = by + 1.0;
                break;
            }
            air |= !solid;
        }
        opening.heights.put(key, found);
        return found;
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        OPEN.clear();
        Scars.restoreAll();
    }
}
