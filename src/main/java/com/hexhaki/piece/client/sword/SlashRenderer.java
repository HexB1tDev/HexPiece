package com.hexhaki.piece.client.sword;

import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.client.haki.HakiBolts;
import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.DrawCrackle;
import com.hexhaki.piece.haki.RyouPalette;
import com.hexhaki.piece.network.S2CSlash;
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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 2.1.4: the Haki slash on every client, flown exactly as the server flies it ({@link S2CSlash}: the same start, direction, speed,
 * height and range). The owner: "The Haki projectile must use the player's saved Ryou colour, including its outline and trail. Do not
 * hardcode it to red ... use a neutral visual fallback until their personal Ryou colour exists ... Make the attack's visible shape,
 * entity hit volume and removed blocks agree."
 * <p>
 * Drawn where the server's plane is: a crescent standing in the vertical plane of its flight, as tall as the plane, its two tips at the
 * plane's top and bottom and bowed forward a little; a dark core (the cutting edge of Armament) with a bright rim of the colour round
 * it and a fainter halo, and a trail of three fading ghosts behind it; flecks of the colour shed as it flies. Additive light and the
 * dark core are depth tested: walls hide it. The colour is the owner's Ryou colour as the server sent it, or the neutral pale steel.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class SlashRenderer {
    static final int SEGMENTS = 14;
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
                Vec3 at = point(slash, d);
                int tint = color(slash) | 0xFF000000;
                for (int i = 0; i < PieceFx.count(3); i++) {
                    double up = (RANDOM.nextDouble() - 0.5) * slash.height();
                    PieceFx.at(PieceFx.MOTE, true, 8 + RANDOM.nextInt(6), at.m_82520_(0.0, up, 0.0)).colors(tint, tint & 0x00FFFFFF)
                        .size(0.16, 0.02).drag(0.9).spawn();
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

    static void draw(PoseStack pose, Vec3 cam, ClientLevel level, float partial) {
        MultiBufferSource.BufferSource buffers = Minecraft.m_91087_().m_91269_().m_110104_();
        RenderType dark = RenderType.m_110470_(SwordFx.WHITE_TEXTURE);
        RenderType light = RenderType.m_110488_(SwordFx.WHITE_TEXTURE);
        pose.m_85836_();
        pose.m_252880_((float) -cam.f_82479_, (float) -cam.f_82480_, (float) -cam.f_82481_);
        Matrix4f matrix = pose.m_85850_().m_252922_();
        Matrix3f normal = pose.m_85850_().m_252943_();
        VertexConsumer core = buffers.m_6299_(dark);
        for (S2CSlash slash : FLYING.values()) {
            double d = flown(slash, level, partial);
            if (d >= 0 && d < slash.range()) {
                crescent(core, matrix, normal, slash, d, 0.0, 0x000000, 0.86F, 0.55);
            }
        }
        buffers.m_109912_(dark);
        VertexConsumer glow = buffers.m_6299_(light);
        for (S2CSlash slash : FLYING.values()) {
            double d = flown(slash, level, partial);
            if (d < 0) {
                continue;
            }
            int color = color(slash);
            double fade = d >= slash.range() ? Math.max(0.0, 1.0 - (flown(slash, level, partial) - slash.range()) / (slash.speed() * 3.0)) : 1.0;
            if (d < slash.range()) {
                crescent(glow, matrix, normal, slash, d, 0.12, color, 1.0F, 0.85);
                crescent(glow, matrix, normal, slash, d, 0.32, color, 0.35F, 0.9);
            }
            for (int ghost = 1; ghost <= 3; ghost++) {
                double back = d - ghost * slash.speed() * 0.45;
                if (back > 0) {
                    crescent(glow, matrix, normal, slash, Math.min(back, slash.range()), 0.1, color, (float) (0.5 / ghost * fade), 0.8);
                }
            }
        }
        buffers.m_109912_(light);
        pose.m_85849_();
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
            Vec3 dir = new Vec3(slash.dx(), 0.0, slash.dz());
            dir = dir.m_82556_() < 1.0E-6 ? new Vec3(1, 0, 0) : dir.m_82541_();
            Vec3 side = new Vec3(-dir.f_82481_, 0.0, dir.f_82479_);
            Vec3 centre = point(slash, d);
            double half = slash.height() / 2.0;
            double bow = slash.height() * 0.2;
            double k = slash.height() / BLADE;
            int color = color(slash) | 0xFF000000;
            long window = (long) Math.floor(ticks / DrawCrackle.WINDOW);
            for (long w = Math.max(0, window - 2); w <= window; w++) {
                for (Bolt bolt : DrawCrackle.set(slash.id() * 0x9E3779B97F4A7C15L, w, 0.0, BLADE, Double.NaN, 1.3, color)) {
                    double age = ticks - w * DrawCrackle.WINDOW - bolt.delay;
                    if (age < 0 || age > bolt.duration) {
                        continue;
                    }
                    // the crackle's straight blade bent onto the crescent: up the blade is up the crescent, off it is ahead of it
                    double[] points = new double[bolt.points.length];
                    for (int i = 0; i < bolt.count(); i++) {
                        double a = (bolt.points[i * 3 + 1] / BLADE - 0.5) * 2.0;
                        double ahead = bow * (1.0 - a * a) + 0.275 * (1.0 - a * a * 0.85) + slash.height() * 0.03 + Math.abs(bolt.points[i * 3]) * k;
                        double across = bolt.points[i * 3 + 2] * k;
                        points[i * 3] = centre.f_82479_ + dir.f_82479_ * ahead + side.f_82479_ * across - cam.f_82479_;
                        points[i * 3 + 1] = centre.f_82480_ + a * half - cam.f_82480_;
                        points[i * 3 + 2] = centre.f_82481_ + dir.f_82481_ * ahead + side.f_82481_ * across - cam.f_82481_;
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

    /**
     * The crescent at {@code along} blocks: its chord the plane's height (centred on the path), bowed forward by a fifth of it, a band
     * {@code thickness} blocks thick plus {@code grow} each side, in {@code color} at {@code strength}; both faces.
     */
    static void crescent(VertexConsumer buffer, Matrix4f matrix, Matrix3f normal, S2CSlash slash, double along, double grow, int color, float strength,
                         double thickness) {
        Vec3 dir = new Vec3(slash.dx(), slash.dy(), slash.dz());
        Vec3 side = new Vec3(-dir.f_82481_, 0.0, dir.f_82479_);
        side = side.m_82556_() < 1.0E-6 ? new Vec3(1, 0, 0) : side.m_82541_();
        Vec3 centre = point(slash, along);
        double half = slash.height() / 2.0;
        double bow = slash.height() * 0.2;
        float r = ((color >> 16) & 255) / 255.0F * strength;
        float g = ((color >> 8) & 255) / 255.0F * strength;
        float b = (color & 255) / 255.0F * strength;
        float alpha = color == 0 ? strength : 1.0F;
        for (int i = 0; i < SEGMENTS; i++) {
            double u0 = i / (double) SEGMENTS;
            double u1 = (i + 1) / (double) SEGMENTS;
            Vec3[] q = {edge(centre, dir, side, half, bow, thickness, grow, u0, 1), edge(centre, dir, side, half, bow, thickness, grow, u1, 1),
                edge(centre, dir, side, half, bow, thickness, grow, u1, -1), edge(centre, dir, side, half, bow, thickness, grow, u0, -1)};
            for (int face = 0; face < 2; face++) {
                for (int k = 0; k < 4; k++) {
                    Vec3 p = q[face == 0 ? k : 3 - k];
                    buffer.m_252986_(matrix, (float) p.f_82479_, (float) p.f_82480_, (float) p.f_82481_).m_85950_(r, g, b, alpha).m_7421_(0.5F, 0.5F)
                        .m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(normal, (float) side.f_82479_, 0.0F,
                            (float) side.f_82481_).m_5752_();
                }
            }
        }
    }

    /** A point of the crescent's band: u 0 the bottom tip, 1 the top; s +1 the front (convex) edge, -1 the back. */
    static Vec3 edge(Vec3 centre, Vec3 dir, Vec3 side, double half, double bow, double thickness, double grow, double u, int s) {
        double a = (u - 0.5) * 2.0;
        double y = a * (half + grow);
        double forward = bow * (1.0 - a * a);
        double width = (thickness * 0.5 * (1.0 - a * a * 0.85) + grow) * s;
        Vec3 flat = new Vec3(dir.f_82479_, 0.0, dir.f_82481_);
        flat = flat.m_82556_() < 1.0E-6 ? dir : flat.m_82541_();
        return centre.m_82520_(0.0, y, 0.0).m_82549_(flat.m_82490_(forward + width));
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FLYING.clear();
    }
}
