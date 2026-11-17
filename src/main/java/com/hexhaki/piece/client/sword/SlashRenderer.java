package com.hexhaki.piece.client.sword;

import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.client.haki.HakiBolts;
import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.DrawCrackle;
import com.hexhaki.piece.haki.RyouPalette;
import com.hexhaki.piece.network.S2CSlash;
import com.hexhaki.piece.sword.SlashMesh;
import com.hexhaki.piece.sword.SwordRules;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 2.1.4: the Haki slash on every client, flown exactly as the server flies it ({@link S2CSlash}: the same start, direction, speed,
 * height and range). The owner: "The Haki projectile must use the player's saved Ryou colour, including its outline and trail. Do not
 * hardcode it to red ... use a neutral visual fallback until their personal Ryou colour exists ... Make the attack's visible shape,
 * entity hit volume and removed blocks agree."
 * <p>
 * 2.2.4: drawn where the server's plane is as the energy wave of the manga's panel the owner showed ({@link SlashMesh}): rising out of
 * its trench, a sharp peak as tall as the plane, its back torn into tongues of light, thick across its flight, inked and glowing, its
 * Haki crackle running up its leading edge, flecks of the colour shed off its back. Depth tested: walls hide it. The colour is the
 * owner's Ryou colour as the server sent it (every player has one from their first login), or the neutral pale steel (a pirate captain).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class SlashRenderer {
    /** The crackle's own blade length (DrawCrackle works along a katana's blade; the slash scales it to its height). */
    static final double BLADE = 1.28;
    private static final Map<Integer, S2CSlash> FLYING = new HashMap<>();
    private static final Random RANDOM = new Random();

    private SlashRenderer() {
    }

    public static void accept(S2CSlash message) {
        FLYING.put(message.id(), message);
    }

    static int color(S2CSlash slash) {
        return (slash.color() == 0 ? RyouPalette.NEUTRAL : slash.color()) & 0xFFFFFF;
    }

    /** How far it has flown at this frame (blocks), or -1 once it is gone. */
    static double flown(S2CSlash slash, ClientLevel level, float partial) {
        long now = level.m_46467_() & 0x3FFFFFFF;
        double ticks = ((now - slash.start()) & 0x3FFFFFFF) + partial;
        double distance = ticks * slash.speed();
        return distance > slash.range() + slash.speed() * 3.0 ? -1.0 : Math.min(slash.range(), distance);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (event.phase != TickEvent.Phase.END || level == null || FLYING.isEmpty()) {
            return;
        }
        Iterator<S2CSlash> it = FLYING.values().iterator();
        while (it.hasNext()) {
            S2CSlash slash = it.next();
            double d = flown(slash, level, 0.0F);
            if (d < 0) {
                it.remove();
                continue;
            }
            if (d < slash.range()) {
                // flecks of its colour shed along the way
                // flecks of its colour shed off its back slope as it goes (2.2.4: the wave's body, trench to peak)
                Vec3 foot = point(slash, d).m_82520_(0.0, SwordRules.slashCentre(slash.height()) - slash.height() / 2.0, 0.0);
                Vec3 flat = new Vec3(slash.dx(), 0.0, slash.dz());
                flat = flat.m_82556_() < 1.0E-6 ? new Vec3(1, 0, 0) : flat.m_82541_();
                int tint = color(slash) | 0xFF000000;
                for (int i = 0; i < PieceFx.count(4); i++) {
                    double v = RANDOM.nextDouble() * 0.85;
                    double back = -SwordRules.SLASH_BACK * slash.height() * Math.pow(1.0 - v, 1.25) * RANDOM.nextDouble();
                    Vec3 at = foot.m_82549_(flat.m_82490_(back)).m_82520_(0.0, v * slash.height(), 0.0);
                    PieceFx.at(PieceFx.MOTE, true, 8 + RANDOM.nextInt(6), at).colors(tint, tint & 0x00FFFFFF)
                        .velocity(flat.m_82490_(-0.15).m_82520_(0.0, 0.12, 0.0)).size(0.16 + slash.height() * 0.004, 0.02).drag(0.9).spawn();
                }
            }
        }
    }

    static Vec3 point(S2CSlash slash, double along) {
        return new Vec3(slash.x() + slash.dx() * along, slash.y() + slash.dy() * along, slash.z() + slash.dz() * along);
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || FLYING.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        try {
            draw(event.getPoseStack(), event.getCamera().m_90583_(), level, event.getPartialTick());
        } catch (RuntimeException failure) {
            FLYING.clear();
        }
    }

    /** 2.2.4: the wave's quads, rebuilt every frame: the solid body, streaks and outline, and the glow. */
    private static final SlashMesh.Quads SOLID = new SlashMesh.Quads();
    private static final SlashMesh.Quads GLOW = new SlashMesh.Quads();

    /** The wave's frame at this frame: the foot of its line (relative to the eye), its flight, its height, how far it has grown out. */
    static SlashMesh.Frame frame(S2CSlash slash, double along, Vec3 cam) {
        Vec3 foot = point(slash, Math.min(along, slash.range())).m_82520_(0.0, SwordRules.slashCentre(slash.height()) - slash.height() / 2.0, 0.0);
        double grown = along / Math.max(0.5, slash.height() * SwordRules.SLASH_BACK);
        return SlashMesh.Frame.of(new double[] {foot.f_82479_ - cam.f_82479_, foot.f_82480_ - cam.f_82480_, foot.f_82481_ - cam.f_82481_},
            slash.dx(), slash.dz(), slash.height(), grown);
    }

    /**
     * 2.2.4: the wave ({@link SlashMesh}): its body, streaks and ink outline in a batch of their own (unlit, depth written, back faces
     * culled: the Haki lightning's ink), then its glow (alpha blended, sorted far to near). As it reaches its range it sinks and fades
     * over three ticks.
     */
    static void draw(PoseStack pose, Vec3 cam, ClientLevel level, float partial) {
        MultiBufferSource.BufferSource buffers = Minecraft.m_91087_().m_91269_().m_110104_();
        SOLID.clear();
        GLOW.clear();
        long now = level.m_46467_() & 0x3FFFFFFF;
        for (S2CSlash slash : FLYING.values()) {
            double d = flown(slash, level, partial);
            if (d < 0) {
                continue;
            }
            double fade = d >= slash.range() ? Math.max(0.0, 1.0 - (d - slash.range()) / (slash.speed() * 3.0)) : 1.0;
            double over = ((now - slash.start()) & 0x3FFFFFFF) + partial - slash.range() / slash.speed();
            if (over > 0) {
                fade = Math.max(0.0, 1.0 - over / 3.0);
            }
            if (fade <= 0.01) {
                continue;
            }
            double ticks = ((now - slash.start()) & 0x3FFFFFFF) + partial;
            SlashMesh.wave(SOLID, GLOW, frame(slash, d, cam), slash.id(), color(slash), ticks, fade);
        }
        Matrix4f matrix = pose.m_85850_().m_252922_();
        if (SOLID.n > 0) {
            RenderType ink = RenderType.m_269058_();
            VertexConsumer solid = buffers.m_6299_(ink);
            for (int q = 0; q < SOLID.n; q++) {
                for (int k = 0; k < 4; k++) {
                    int c = SOLID.c[q * 4 + k];
                    solid.m_252986_(matrix, (float) SOLID.v[q * 12 + k * 3], (float) SOLID.v[q * 12 + k * 3 + 1], (float) SOLID.v[q * 12 + k * 3 + 2])
                        .m_85950_(((c >>> 16) & 255) / 255.0F, ((c >>> 8) & 255) / 255.0F, (c & 255) / 255.0F, ((c >>> 24) & 255) / 255.0F)
                        .m_85969_(0xF000F0).m_5752_();
                }
            }
            buffers.m_109912_(ink);
        }
        if (GLOW.n > 0) {
            GLOW.sortBackToFront();
            RenderType light = RenderType.m_269166_();
            VertexConsumer glow = buffers.m_6299_(light);
            for (int q = 0; q < GLOW.n; q++) {
                for (int k = 0; k < 4; k++) {
                    int c = GLOW.c[q * 4 + k];
                    glow.m_252986_(matrix, (float) GLOW.v[q * 12 + k * 3], (float) GLOW.v[q * 12 + k * 3 + 1], (float) GLOW.v[q * 12 + k * 3 + 2])
                        .m_85950_(((c >>> 16) & 255) / 255.0F, ((c >>> 8) & 255) / 255.0F, (c & 255) / 255.0F, ((c >>> 24) & 255) / 255.0F)
                        .m_5752_();
                }
            }
            buffers.m_109912_(light);
        }
        crackle(pose, cam, level, partial);
    }

    /**
     * 2.1.5: the slash crackles with Haki lightning (the same inked bolts as the Conqueror's and the drawn blade's): arcs leaping along
     * its edge and sparks shed behind it, a new set every two ticks, in its colour, as big as the slash is tall.
     */
    static void crackle(PoseStack pose, Vec3 cam, ClientLevel level, float partial) {
        List<Bolt> bolts = new ArrayList<>();
        List<Double> ages = new ArrayList<>();
        long now = level.m_46467_() & 0x3FFFFFFF;
        for (S2CSlash slash : FLYING.values()) {
            double d = flown(slash, level, partial);
            if (d < 0 || d >= slash.range()) {
                continue;
            }
            double ticks = ((now - slash.start()) & 0x3FFFFFFF) + partial;
            SlashMesh.Frame frame = frame(slash, d, cam);
            double k = slash.height() / BLADE;
            int color = color(slash) | 0xFF000000;
            long window = (long) Math.floor(ticks / DrawCrackle.WINDOW);
            for (long w = Math.max(0, window - 2); w <= window; w++) {
                for (Bolt bolt : DrawCrackle.set(slash.id() * 0x9E3779B97F4A7C15L, w, 0.0, BLADE, Double.NaN, 1.3, color)) {
                    double age = ticks - w * DrawCrackle.WINDOW - bolt.delay;
                    if (age < 0 || age > bolt.duration) {
                        continue;
                    }
                    // 2.2.4: the crackle's straight blade laid along the wave's leading edge: up the blade is up the edge (trench to
                    // peak), off it is ahead of it
                    double[] points = new double[bolt.points.length];
                    for (int i = 0; i < bolt.count(); i++) {
                        double v = Math.max(0.0, Math.min(0.97, bolt.points[i * 3 + 1] / BLADE * 0.97));
                        double[] p = SlashMesh.edge(frame, v, slash.height() * 0.01 + Math.abs(bolt.points[i * 3]) * k, bolt.points[i * 3 + 2] * k);
                        points[i * 3] = p[0];
                        points[i * 3 + 1] = p[1];
                        points[i * 3 + 2] = p[2];
                    }
                    float[] widths = bolt.widths.clone();
                    float grow = (float) Math.min(3.0, Math.pow(k, 0.75));
                    for (int i = 0; i < widths.length; i++) {
                        widths[i] *= grow;
                    }
                    bolts.add(new Bolt(points, widths, bolt.color, bolt.kind, bolt.delay, bolt.duration, bolt.crawl, bolt.seed, bolt.dance));
                    ages.add(age);
                }
            }
        }
        double[] at = new double[ages.size()];
        for (int i = 0; i < at.length; i++) {
            at[i] = ages.get(i);
        }
        HakiBolts.drawInView(pose, bolts, at, level, partial);
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FLYING.clear();
    }
}
