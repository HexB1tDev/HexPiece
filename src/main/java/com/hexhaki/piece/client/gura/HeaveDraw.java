package com.hexhaki.piece.client.gura;

import com.hexhaki.piece.gura.Heave;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 3.1.6: the Gura ultimate's ground heaving (gura/Heave says which squares break and how they move): each breaking plate's top blocks
 * are drawn with the world's own block models ({@code BlockRenderDispatcher.renderSingleBlock}, the way a falling block is drawn: solid,
 * lit as where it stands), lifted, tipped about the plate's middle and bounced as Heave has it, over the real ground (which stays, and
 * shows through where a plate sinks back). The blocks are found once, when the heave arrives (the top solid block of each column, not
 * under water, no block entity); at most {@link Heave#MOST_DRAWN} blocks a frame, the nearest plates first.
 */
final class HeaveDraw {
    /** One column of a plate: its top block and the one under it, where, how it is lit. */
    record Column(BlockState top, BlockState under, int x, int y, int z, int light) {
    }

    /** One plate and its columns; its middle on top of the ground. */
    record Slab(Heave.Plate plate, List<Column> columns, double cx, double cy, double cz) {
    }

    /** The one rotation every plate is turned by (set afresh for each; no allocation a frame). */
    private static final org.joml.Quaternionf TURN = new org.joml.Quaternionf();

    final List<Slab> slabs = new ArrayList<>();
    final long born;
    final Vec3 at;
    final double reach;

    HeaveDraw(ClientLevel level, Vec3 at, Vec3 dir, double cone, double reach, double wave, int seed, long born) {
        this.born = born;
        this.at = at;
        this.reach = reach;
        int foot = (int) Math.floor(at.f_82480_ - 0.01);
        for (Heave.Plate p : Heave.plates(at.f_82479_, at.f_82481_, dir.f_82479_, dir.f_82481_, cone, reach, wave, seed)) {
            List<Column> columns = new ArrayList<>(p.size() * p.size());
            double top = 0.0;
            for (int i = 0; i < p.size(); i++) {
                for (int k = 0; k < p.size(); k++) {
                    Column c = column(level, p.x() + i, foot, p.z() + k);
                    if (c != null) {
                        columns.add(c);
                        top += c.y + 1;
                    }
                }
            }
            if (!columns.isEmpty()) {
                this.slabs.add(new Slab(p, columns, p.x() + p.size() * 0.5, top / columns.size(), p.z() + p.size() * 0.5));
            }
        }
    }

    /** The top solid block of the column at (x, z) near the height {@code foot}, or null (none, under water, a block entity). */
    static Column column(ClientLevel level, int x, int foot, int z) {
        for (int y = foot + 7; y >= foot - 10; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.m_6425_(pos).m_76178_()) {
                return null;
            }
            BlockState state = level.m_8055_(pos);
            if (state.m_60795_() || state.m_60812_(level, pos).m_83281_()) {
                continue;
            }
            if (level.m_7702_(pos) != null) {
                return null;
            }
            BlockPos below = new BlockPos(x, y - 1, z);
            BlockState under = level.m_8055_(below);
            if (under.m_60795_() || level.m_7702_(below) != null) {
                under = null;
            }
            return new Column(state, under, x, y, z, LevelRenderer.m_109541_(level, new BlockPos(x, y + 1, z)));
        }
        return null;
    }

    boolean over(double age) {
        return age > this.reach / Heave.WAVE + 2.0 + Heave.LIFE;
    }

    /** Draws the moving plates at {@code age} ticks; returns how many blocks it drew (at most {@code budget}). */
    int draw(PoseStack pose, MultiBufferSource buffers, BlockRenderDispatcher blocks, Vec3 cam, double age, int budget) {
        int drawn = 0;
        for (Slab s : this.slabs) {
            Heave.Plate p = s.plate;
            if (age < p.delay() || !Heave.shows(p, age)) {
                continue;
            }
            double lift = Heave.lift(p, age);
            double tip = Heave.tip(p, age);
            boolean deep = lift > 0.5;
            int cost = s.columns.size() * (deep ? 2 : 1);
            if (drawn + cost > budget) {
                break;
            }
            drawn += cost;
            pose.m_85836_();
            pose.m_85837_(s.cx - cam.f_82479_, s.cy - cam.f_82480_ + lift, s.cz - cam.f_82481_);
            pose.m_252781_(TURN.rotationAxis((float) tip, (float) p.ax(), 0.0F, (float) p.az()));
            for (Column c : s.columns) {
                pose.m_85836_();
                pose.m_85837_(c.x - s.cx, c.y - s.cy, c.z - s.cz);
                blocks.m_110912_(c.top, pose, buffers, c.light, OverlayTexture.f_118083_);
                if (deep && c.under != null) {
                    pose.m_85837_(0.0, -1.0, 0.0);
                    blocks.m_110912_(c.under, pose, buffers, c.light, OverlayTexture.f_118083_);
                }
                pose.m_85849_();
            }
            pose.m_85849_();
        }
        return drawn;
    }
}
