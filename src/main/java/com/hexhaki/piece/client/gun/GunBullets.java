package com.hexhaki.piece.client.gun;

import com.hexhaki.piece.gun.Gun;
import com.hexhaki.piece.gun.GunRules;
import com.hexhaki.piece.network.S2CGunShot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.8.8: the guns' balls in flight, as the server flies them ({@link S2CGunShot}; server/GunShots): the owner's lead ball (every gun's
 * bullet, drawn well over its size so it is seen), a pale streak of air behind it; with Haki pure black, a long black streak. Where it
 * stops: a puff and sparks off a block, sparks off a body. The muzzle's smoke on the shot (GunLayer) is the game's own cloud.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class GunBullets {
    private static final Map<Integer, Flight> FLIGHTS = new HashMap<>();
    private static final Random RANDOM = new Random();
    static final int FULL_BRIGHT = 0xF000F0;
    /** Blocks of streak behind a ball, and its half-width. */
    static final double STREAK = 1.4;
    static final double HAKI_STREAK = 4.5;
    static final float WIDTH = 0.035F;

    static final class Flight {
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;
        boolean haki;
        /** The game tick it left the muzzle (the client's), and where and when (ticks of flight) it stops, once the server says. */
        long born;
        Vec3 stop;
        double stopTicks = Double.NaN;
        int struck;
    }

    private GunBullets() {
    }

    public static void accept(S2CGunShot message) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        if (message.kind() == S2CGunShot.FLY) {
            Flight f = new Flight();
            f.x = message.x();
            f.y = message.y();
            f.z = message.z();
            f.vx = message.vx();
            f.vy = message.vy();
            f.vz = message.vz();
            f.haki = message.haki();
            f.born = level.m_46467_();
            FLIGHTS.put(message.id(), f);
            return;
        }
        Flight f = FLIGHTS.get(message.id());
        Vec3 at = new Vec3(message.x(), message.y(), message.z());
        if (f == null) {
            impact(level, at, (int) message.vx(), message.haki(), null);
            return;
        }
        // it is drawn on until it gets there (the stop's own tick of flight, from how far along its line the point is)
        double speed = Math.sqrt(f.vx * f.vx + f.vy * f.vy + f.vz * f.vz);
        double along = ((at.f_82479_ - f.x) * f.vx + (at.f_82480_ - f.y) * f.vy + (at.f_82481_ - f.z) * f.vz) / Math.max(1.0E-6, speed * speed);
        f.stop = at;
        f.stopTicks = Math.max(0.0, along);
        f.struck = (int) message.vx();
    }

    /** The ball {@code ticks} into its flight. */
    static Vec3 at(Flight f, double ticks) {
        return new Vec3(GunRules.at(f.x, f.vx, ticks, f.haki, false), GunRules.at(f.y, f.vy, ticks, f.haki, true),
            GunRules.at(f.z, f.vz, ticks, f.haki, false));
    }

    /** Smoke out of a muzzle at {@code at}, the barrel's way {@code way} (GunLayer, once a shot). */
    static void muzzleSmoke(double[] at, double[] way) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        double len = Math.sqrt(way[0] * way[0] + way[1] * way[1] + way[2] * way[2]);
        if (len < 1.0E-6) {
            return;
        }
        for (int i = 0; i < 6; i++) {
            double s = 0.02 + 0.05 * RANDOM.nextDouble();
            level.m_7106_(ParticleTypes.f_123796_, at[0], at[1], at[2], way[0] / len * s + (RANDOM.nextDouble() - 0.5) * 0.02,
                way[1] / len * s + 0.01 + RANDOM.nextDouble() * 0.015, way[2] / len * s + (RANDOM.nextDouble() - 0.5) * 0.02);
        }
    }

    static void impact(ClientLevel level, Vec3 at, int struck, boolean haki, Flight f) {
        if (struck == S2CGunShot.AIR) {
            return;
        }
        int sparks = struck == S2CGunShot.BODY ? 10 : 7;
        for (int i = 0; i < sparks; i++) {
            level.m_7106_(ParticleTypes.f_123797_, at.f_82479_, at.f_82480_, at.f_82481_, (RANDOM.nextDouble() - 0.5) * 0.5,
                RANDOM.nextDouble() * 0.35, (RANDOM.nextDouble() - 0.5) * 0.5);
        }
        if (struck == S2CGunShot.BLOCK) {
            for (int i = 0; i < (haki ? 7 : 4); i++) {
                level.m_7106_(ParticleTypes.f_123796_, at.f_82479_, at.f_82480_, at.f_82481_, (RANDOM.nextDouble() - 0.5) * 0.06,
                    0.02 + RANDOM.nextDouble() * 0.04, (RANDOM.nextDouble() - 0.5) * 0.06);
            }
        }
    }

    static void clear() {
        FLIGHTS.clear();
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || FLIGHTS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            FLIGHTS.clear();
            return;
        }
        PoseStack pose = event.getPoseStack();
        Vec3 cam = event.getCamera().m_90583_();
        float partial = event.getPartialTick();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        long now = level.m_46467_();
        try {
            VertexConsumer streaks = buffers.m_6299_(RenderType.m_269166_());
            Matrix4f view = pose.m_85850_().m_252922_();
            Iterator<Map.Entry<Integer, Flight>> it = FLIGHTS.entrySet().iterator();
            while (it.hasNext()) {
                Flight f = it.next().getValue();
                double t = now - f.born + partial;
                boolean done = !Double.isNaN(f.stopTicks) && t >= f.stopTicks;
                if (done || t > GunRules.life(f.haki) + 4) {
                    if (done) {
                        impact(level, f.stop, f.struck, f.haki, f);
                    }
                    it.remove();
                    continue;
                }
                Vec3 p = at(f, t);
                streak(streaks, view, cam, f, t);
                pose.m_85836_();
                pose.m_85837_(p.f_82479_ - cam.f_82479_, p.f_82480_ - cam.f_82480_, p.f_82481_ - cam.f_82481_);
                GunRenderer.ball(pose, buffers, FULL_BRIGHT, Gun.BULLET_SCALE, f.haki);
                pose.m_85849_();
            }
            buffers.m_109912_(RenderType.m_269166_());
            buffers.m_109912_(RenderType.m_110458_(GunRenderer.TEXTURE));
        } catch (RuntimeException failure) {
            FLIGHTS.clear(); // a ball never stops the frame
        }
    }

    /** The streak behind a ball: a ribbon turned to the eye, from the ball back along its way, fading. */
    static void streak(VertexConsumer out, Matrix4f view, Vec3 cam, Flight f, double t) {
        double length = f.haki ? HAKI_STREAK : STREAK;
        Vec3 head = at(f, t);
        double back = Math.min(t * Math.sqrt(f.vx * f.vx + f.vy * f.vy + f.vz * f.vz), length);
        if (back < 0.05) {
            return;
        }
        Vec3 way = new Vec3(f.vx, f.vy, f.vz).m_82541_();
        Vec3 tail = head.m_82546_(way.m_82490_(back));
        Vec3 toEye = cam.m_82546_(head);
        Vec3 side = way.m_82537_(toEye);
        if (side.m_82553_() < 1.0E-6) {
            return;
        }
        side = side.m_82541_().m_82490_(f.haki ? WIDTH * 1.6 : WIDTH);
        float hr = f.haki ? 0.02F : 0.92F;
        float hg = f.haki ? 0.02F : 0.9F;
        float hb = f.haki ? 0.03F : 0.86F;
        float ha = f.haki ? 0.9F : 0.45F;
        Vec3[] corners = {head.m_82549_(side), head.m_82546_(side), tail.m_82546_(side.m_82490_(0.3)), tail.m_82549_(side.m_82490_(0.3))};
        float[] alpha = {ha, ha, 0.0F, 0.0F};
        for (int pass = 0; pass < 2; pass++) {
            // both faces (the ribbon is seen from either side as the eye moves round it)
            for (int k = 0; k < 4; k++) {
                int v = pass == 0 ? k : 3 - k;
                vertex(out, view, corners[v], cam, hr, hg, hb, alpha[v]);
            }
        }
    }

    static void vertex(VertexConsumer out, Matrix4f view, Vec3 p, Vec3 cam, float r, float g, float b, float a) {
        out.m_252986_(view, (float) (p.f_82479_ - cam.f_82479_), (float) (p.f_82480_ - cam.f_82480_), (float) (p.f_82481_ - cam.f_82481_))
            .m_85950_(r, g, b, a).m_5752_();
    }
}
