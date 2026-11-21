package com.hexhaki.piece.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.7.8 (the owner, on the Blast Breath beam: "it should also destroy blocks and self repair after 30 seconds"): a crater torn out of
 * the ground through {@link Scars} (nothing dropped, nothing with a block entity touched, no claimed or protected block, paintings'
 * walls left, the world's mobGriefing rule obeyed by the owner's own checks), opening from the middle outward a few ticks long within
 * Scars' budgets, every block grown back {@link #REPAIR} ticks later.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class Craters {
    /** 30 seconds. */
    public static final long REPAIR = 600L;
    /** The biggest crater (blocks of radius), and at most this many blocks a crater looks at a tick. */
    static final double MOST = 13.0;
    static final int LOOK_PER_TICK = 2500;

    /** One crater opening: its middle, its cells nearest first (packed x, y, z offsets), how far it has got. */
    static final class Crater {
        final ServerLevel level;
        final LivingEntity owner;
        final int cx;
        final int cy;
        final int cz;
        final int[] cells;
        final List<double[]> supports;
        int next;

        Crater(ServerLevel level, LivingEntity owner, int cx, int cy, int cz, int[] cells, List<double[]> supports) {
            this.level = level;
            this.owner = owner;
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
            this.cells = cells;
            this.supports = supports;
        }
    }

    private static final List<Crater> OPENING = new ArrayList<>();

    private Craters() {
    }

    /** Opens a rough ball of {@code radius} blocks round {@code at}, torn out by {@code owner}. */
    public static void open(ServerLevel level, LivingEntity owner, Vec3 at, double radius) {
        double rr = Math.min(MOST, radius);
        if (rr < 0.8 || !level.m_46469_().m_46207_(net.minecraft.world.level.GameRules.f_46132_)) {
            return;
        }
        int r = (int) Math.ceil(rr + 1.0);
        int cx = (int) Math.floor(at.f_82479_);
        int cy = (int) Math.floor(at.f_82480_);
        int cz = (int) Math.floor(at.f_82481_);
        long[] keyed = new long[(2 * r + 1) * (2 * r + 1) * (2 * r + 1)];
        int n = 0;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    double rough = rr * (0.8 + 0.3 * noise(cx + x, cy + y, cz + z));
                    int d2 = x * x + y * y + z * z;
                    if (d2 > rough * rough) {
                        continue;
                    }
                    // nearest first: the distance in the high bits, the offset (each 8 bits, +128) below
                    keyed[n++] = (long) d2 << 24 | (long) (x + 128) << 16 | (long) (y + 128) << 8 | (long) (z + 128);
                }
            }
        }
        long[] sorted = Arrays.copyOf(keyed, n);
        Arrays.sort(sorted);
        int[] cells = new int[n];
        for (int i = 0; i < n; i++) {
            cells[i] = (int) (sorted[i] & 0xFFFFFFL);
        }
        List<double[]> supports = new ArrayList<>();
        double c = Scars.HANGING_CLEARANCE + r;
        for (HangingEntity hanging : level.m_6443_(HangingEntity.class, new AABB(cx - c, cy - c, cz - c, cx + 1 + c, cy + 1 + c, cz + 1 + c),
            entity -> true)) {
            Vec3 p = hanging.m_20182_();
            supports.add(new double[] {p.f_82479_, p.f_82480_, p.f_82481_});
        }
        OPENING.add(new Crater(level, owner, cx, cy, cz, cells, supports));
    }

    static double noise(int x, int y, int z) {
        int h = x * 374761393 + y * 668265263 + z * 1274126177;
        h = (h ^ (h >>> 13)) * 1274126177;
        return ((h ^ (h >>> 16)) & 0xFFFF) / 65535.0;
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || OPENING.isEmpty()) {
            return;
        }
        Scars scars = Scars.of(level);
        for (Iterator<Crater> it = OPENING.iterator(); it.hasNext(); ) {
            Crater crater = it.next();
            if (crater.level != level) {
                continue;
            }
            int look = LOOK_PER_TICK;
            while (crater.next < crater.cells.length && look-- > 0 && scars.room()) {
                int cell = crater.cells[crater.next++];
                int x = (cell >> 16 & 255) - 128;
                int y = (cell >> 8 & 255) - 128;
                int z = (cell & 255) - 128;
                scars.tear(crater.owner, new BlockPos(crater.cx + x, crater.cy + y, crater.cz + z), crater.supports, REPAIR);
            }
            if (crater.next >= crater.cells.length) {
                it.remove();
            }
        }
    }
}
