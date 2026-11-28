package com.hexhaki.piece.client.itsumade;

import com.hexhaki.piece.HexPiece;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.itsumade.ItsuArt;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.ItsuGates;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.9.9: Hellish Teleport as every client near either end sees it (server/ItsuGates; S2CUoFx 460 to 464): the pentagram forming on the
 * ground (itsumade/ItsuArt.pentagram, each piece on the block under it: the heights found once and kept, again where the ground was not
 * loaded yet), whatever stands on it swallowed by black flames drawn to its middle, the pillar of black flames blasting up into the sky
 * from it, the beam of them falling out of the sky onto the far one, black flames peeling off what stands there, then both fading.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class ItsuGateFx {
    /** One end of one gate opening. */
    static final class End {
        final double x;
        final double y;
        final double z;
        long start;
        long pillarFrom = -1;
        int pillarLife;
        boolean down;
        long endFrom = -1;
        final Map<Long, Double> ground = new HashMap<>();

        End(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /** A body in black flames: swallowed (drawn to {@code to}) or, arriving, the flames peeling off it. */
    record Burn(int entity, double x, double y, double z, double height, long start, int life, double[] to) {
    }

    static final Map<Long, End> ENDS = new HashMap<>();
    static final List<Burn> BURNS = new ArrayList<>();
    /** How long an end takes to fade once it is over (ticks), how far off it is still drawn (blocks). */
    static final int FADE = 20;
    static final double SEEN = 192.0;

    private ItsuGateFx() {
    }

    static long clock() {
        return ItsuFx.clock;
    }

    static long key(int user, int end) {
        return ((long) user << 1) | (end & 1);
    }

    static void accept(S2CUoFx m) {
        long now = clock();
        switch (m.kind()) {
            case ItsuGates.FORM -> {
                End e = new End(m.x(), m.y(), m.z());
                e.start = now - m.ticks();
                ENDS.put(key(m.user(), m.id()), e);
            }
            case ItsuGates.PILLAR -> {
                End e = ENDS.get(key(m.user(), m.id()));
                if (e != null) {
                    e.pillarFrom = now;
                    e.pillarLife = m.ticks();
                    e.down = m.id() == 1;
                }
            }
            // 4.0.2: the flames stay on the body the whole climb up the near pillar or down the far beam (it is carried: the server moves
            // it; drawn where the body is each frame)
            case ItsuGates.ENGULF -> BURNS.add(new Burn(m.id(), m.x(), m.y(), m.z(), m.bx(), now, m.ticks() + 2, new double[] {m.ax(), m.ay(), m.az()}));
            case ItsuGates.ARRIVE -> BURNS.add(new Burn(m.id(), m.x(), m.y(), m.z(), m.bx(), now, m.ticks(), null));
            case ItsuGates.END -> {
                End e = ENDS.get(key(m.user(), m.id()));
                if (e != null && e.endFrom < 0) {
                    e.endFrom = now;
                }
            }
            default -> {
            }
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ENDS.isEmpty() && BURNS.isEmpty()) {
            return;
        }
        long now = clock();
        Iterator<Map.Entry<Long, End>> it = ENDS.entrySet().iterator();
        while (it.hasNext()) {
            End e = it.next().getValue();
            // over (faded), or left behind long ago (a lost END: never drawn forever)
            if (e.endFrom >= 0 && now - e.endFrom > FADE || now - e.start > ItsuArt.GATE_FORM + 400) {
                it.remove();
            }
        }
        BURNS.removeIf(b -> now - b.start() > b.life());
    }

    /** The height of the ground under ({@code wx, wz}) near {@code e} (its top face, or water's), kept once found. */
    static double ground(ClientLevel level, End e, double wx, double wz) {
        long k = (Math.round(wx * 4.0) << 32) ^ (Math.round(wz * 4.0) & 0xFFFFFFFFL);
        Double kept = e.ground.get(k);
        if (kept != null) {
            return kept;
        }
        BlockHitResult hit = level.m_45547_(new ClipContext(new Vec3(wx, e.y + 2.5, wz), new Vec3(wx, e.y - 5.0, wz), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.ANY, null));
        if (hit.m_6662_() != HitResult.Type.BLOCK) {
            return e.y; // not loaded yet, or a drop: the gate's own height (found again next frame)
        }
        double h = hit.m_82450_().f_82480_;
        if (e.ground.size() < 6000) {
            e.ground.put(k, h);
        }
        return h;
    }

    @SubscribeEvent
    public static void effects(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ENDS.isEmpty() && BURNS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            return;
        }
        float partial = event.getPartialTick();
        Vec3 camera = event.getCamera().m_90583_();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        RenderType type = RenderType.m_269166_();
        ItsuFx.SINK.matrix = event.getPoseStack().m_85850_().m_252922_();
        try {
            ItsuFx.SINK.buffer = buffers.m_6299_(type);
            double now = clock() + partial;
            for (Map.Entry<Long, End> entry : ENDS.entrySet()) {
                End e = entry.getValue();
                if (Math.hypot(e.x - camera.f_82479_, e.z - camera.f_82481_) > SEEN) {
                    continue;
                }
                int seed = (int) (entry.getKey() & 0x7FFFFFFF);
                double fade = e.endFrom >= 0 ? Math.max(0.0, 1.0 - (now - e.endFrom) / FADE) : 1.0;
                double cx = camera.f_82479_;
                double cy = camera.f_82480_;
                double cz = camera.f_82481_;
                ItsuArt.Ground on = (gx, gz) -> ground(level, e, gx + cx, gz + cz) - cy;
                ItsuArt.pentagram(ItsuFx.SINK, on, e.x - cx, e.z - cz, now - e.start, fade, seed);
                if (e.pillarFrom >= 0) {
                    double p = now - e.pillarFrom;
                    double life = e.pillarLife;
                    double alpha = p > life - 16 ? Math.max(0.0, (life - p) / 16.0) : 1.0;
                    double base = e.y - cy;
                    double top = base + ItsuGates.SKY;
                    if (e.down) {
                        // out of the sky onto it, its front falling the whole way in the time things are swallowed at the other end
                        double front = top - ItsuGates.SKY * Math.min(1.0, p / ItsuGates.SWALLOW);
                        ItsuArt.pillar(ItsuFx.SINK, e.x - cx, front, top, e.z - cz, 1.5, p, alpha, true, seed);
                    } else {
                        // blasting up out of it
                        double reach = base + ItsuGates.SKY * Math.min(1.0, p / 5.0);
                        ItsuArt.pillar(ItsuFx.SINK, e.x - cx, base, reach, e.z - cz, 1.5, p, alpha, false, seed);
                    }
                }
            }
            for (Burn b : BURNS) {
                double age = now - b.start();
                double k = age / b.life();
                net.minecraft.world.entity.Entity body = level.m_6815_(b.entity());
                Vec3 at = body != null ? body.m_20318_(partial) : new Vec3(b.x(), b.y(), b.z());
                double x = at.f_82479_ - camera.f_82479_;
                double y = at.f_82480_ - camera.f_82480_;
                double z = at.f_82481_ - camera.f_82481_;
                if (b.to() != null) {
                    // swallowed: the flames rising over it in the first ticks, then held over it as it is carried
                    ItsuArt.engulf(ItsuFx.SINK, x, y, z, b.height(), 0.34 * Math.min(1.0, age / ItsuGates.SWALLOW), null, now, b.entity());
                } else {
                    // down: the flames peeling off it
                    ItsuArt.engulf(ItsuFx.SINK, x, y, z, b.height(), 0.35 + 0.65 * k, null, now, b.entity());
                }
            }
        } catch (RuntimeException failure) {
            HexPiece.LOGGER.error("HexPiece: a Hellish Teleport could not be drawn", failure);
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
        if (ItsuFx.SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        ItsuFx.SINK.buffer = null;
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ENDS.clear();
        BURNS.clear();
    }
}
