package com.hexhaki.piece.client.gear;

import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gear.NikaArt;
import com.hexhaki.piece.network.S2CUoFx;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.3.9: Dawn Pistol's cartoon POW where it lands (server/NikaPistol.POW; gear/NikaArt.pow draws it), on every client near: each one kept
 * {@link NikaArt#POW_LIFE} ticks (at most {@link #MOST}), drawn once a frame at AFTER_PARTICLES through the game's debugQuads (alpha
 * blended, depth tested: never additive, the owner's phone), in one batch.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class NikaPow {
    /** The most kept at once (a crowd struck in turn: the oldest goes first), and how far away one is still drawn. */
    static final int MOST = 16;
    static final double FAR = 96.0;

    static final class Pow {
        double x;
        double y;
        double z;
        float wx;
        float wy;
        float wz;
        float size;
        int seed;
        double born;
    }

    static final List<Pow> POWS = new ArrayList<>();

    private NikaPow() {
    }

    /** From client/dragon/UoFx: a Dawn Pistol has landed. */
    public static void accept(S2CUoFx m) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        if (POWS.size() >= MOST) {
            POWS.remove(0);
        }
        Pow p = new Pow();
        p.x = m.x();
        p.y = m.y();
        p.z = m.z();
        p.wx = m.ax();
        p.wy = m.ay();
        p.wz = m.az();
        p.size = m.size();
        p.seed = m.ticks();
        p.born = level.m_46467_();
        POWS.add(p);
        // 3.4.0: the fist through the body: its skin stretched out to the fist, then snapping back as it is launched
        NikaSkinClient.start(m.id(), m.ax(), m.ay(), m.az(), p.born);
    }

    static final class Writer implements FireMesh.Sink {
        VertexConsumer buffer;
        Matrix4f matrix;

        @Override
        public void quad(double[] xyz, int[] argb) {
            for (int k = 0; k < 4; k++) {
                int c = argb[k];
                this.buffer.m_252986_(this.matrix, (float) xyz[k * 3], (float) xyz[k * 3 + 1], (float) xyz[k * 3 + 2])
                    .m_85950_(((c >>> 16) & 255) / 255.0F, ((c >>> 8) & 255) / 255.0F, (c & 255) / 255.0F, ((c >>> 24) & 255) / 255.0F).m_5752_();
            }
        }
    }

    static final Writer SINK = new Writer();

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || POWS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            POWS.clear();
            return;
        }
        double now = level.m_46467_() + event.getPartialTick();
        POWS.removeIf(p -> now - p.born > NikaArt.POW_LIFE || now < p.born - 40);
        if (POWS.isEmpty()) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        RenderType type = RenderType.m_269166_();
        SINK.matrix = pose.m_85850_().m_252922_();
        FireMesh.quality = Math.max(0.35, PieceFx.density());
        Vec3 cam = event.getCamera().m_90583_();
        try {
            SINK.buffer = buffers.m_6299_(type);
            for (Pow p : POWS) {
                double x = p.x - cam.f_82479_;
                double y = p.y - cam.f_82480_;
                double z = p.z - cam.f_82481_;
                if (x * x + y * y + z * z > FAR * FAR) {
                    continue;
                }
                NikaArt.pow(SINK, x, y, z, p.wx, p.wy, p.wz, now - p.born, p.size, p.seed);
            }
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: Dawn Pistol's POW could not be drawn", failure);
            POWS.clear();
        }
        if (SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        SINK.buffer = null;
    }
}
