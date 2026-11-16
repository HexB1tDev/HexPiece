package com.hexhaki.piece.client.sword;

import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.network.S2CFinisher;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 2.1.4: the Ryou finisher as everyone sees it (server/Finishers): the victim held still, then "a very precise red line slowly appears
 * from bottom to top to bottom (all around the mob)": the cut's line drawn round the body where the slash's plane passes through it,
 * from the feet up the front, over the head and down the back to the feet again, a bright point leading it; when it closes, the
 * original's halving takes the body apart. The victim, if it is this client's player, also sees "a red line ... in their screen vision":
 * a line of red light rising up the middle of the view.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class FinisherFx {
    static final int RED = 0xFF1F2E;
    private static final Map<Integer, S2CFinisher> DOOMED = new HashMap<>();

    private FinisherFx() {
    }

    public static void accept(S2CFinisher message) {
        DOOMED.put(message.entity(), message);
    }

    /** 0..1 of the line drawn at this frame (0 while the victim is only held), or -1 once it is over. */
    static float progress(S2CFinisher doom, ClientLevel level, float partial) {
        long now = level.m_46467_() & 0x3FFFFFFF;
        float ticks = ((now - doom.start()) & 0x3FFFFFFF) + partial;
        if (ticks > doom.freeze() + doom.trace() + 4) {
            return -1.0F;
        }
        float t = (ticks - doom.freeze()) / doom.trace();
        return Math.max(0.0F, Math.min(1.0F, t));
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || DOOMED.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        try {
            draw(event.getPoseStack(), event.getCamera().m_90583_(), level, event.getPartialTick());
        } catch (RuntimeException failure) {
            DOOMED.clear();
        }
    }

    static void draw(PoseStack pose, Vec3 cam, ClientLevel level, float partial) {
        MultiBufferSource.BufferSource buffers = Minecraft.m_91087_().m_91269_().m_110104_();
        RenderType light = RenderType.m_110488_(SwordFx.WHITE_TEXTURE);
        VertexConsumer buffer = buffers.m_6299_(light);
        pose.m_85836_();
        pose.m_252880_((float) -cam.f_82479_, (float) -cam.f_82480_, (float) -cam.f_82481_);
        Matrix4f matrix = pose.m_85850_().m_252922_();
        Matrix3f normal = pose.m_85850_().m_252943_();
        Iterator<Map.Entry<Integer, S2CFinisher>> it = DOOMED.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, S2CFinisher> entry = it.next();
            S2CFinisher doom = entry.getValue();
            float p = progress(doom, level, partial);
            Entity victim = level.m_6815_(entry.getKey());
            if (p < 0 || victim == null) {
                if (p < 0) {
                    it.remove();
                }
                continue;
            }
            if (p <= 0.0F) {
                continue;
            }
            loop(buffer, matrix, normal, victim, doom, p, partial);
        }
        pose.m_85849_();
        buffers.m_109912_(light);
    }

    /** The cut's line round the victim's body, {@code p} of the way: up the front, over the top, down the back, along the feet. */
    static void loop(VertexConsumer buffer, Matrix4f matrix, Matrix3f normal, Entity victim, S2CFinisher doom, float p, float partial) {
        AABB box = victim.m_20191_();
        Vec3 dir = new Vec3(doom.dx(), 0.0, doom.dz());
        dir = dir.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : dir.m_82541_();
        Vec3 side = new Vec3(-dir.f_82481_, 0.0, dir.f_82479_);
        double hx = (box.f_82291_ - box.f_82288_) / 2.0;
        double hz = (box.f_82293_ - box.f_82290_) / 2.0;
        double reach = Math.min(Math.abs(dir.f_82479_) < 1.0E-4 ? 99 : hx / Math.abs(dir.f_82479_),
            Math.abs(dir.f_82481_) < 1.0E-4 ? 99 : hz / Math.abs(dir.f_82481_)) + 0.03;
        Vec3 centre = new Vec3((box.f_82288_ + box.f_82291_) / 2.0, box.f_82289_, (box.f_82290_ + box.f_82293_) / 2.0);
        double height = box.f_82292_ - box.f_82289_ + 0.03;
        Vec3 front = dir.m_82490_(-reach);
        Vec3 back = dir.m_82490_(reach);
        // the loop's corners: the front foot, the front top, the back top, the back foot, round to the front foot
        Vec3[] corners = {centre.m_82549_(front), centre.m_82549_(front).m_82520_(0, height, 0), centre.m_82549_(back).m_82520_(0, height, 0),
            centre.m_82549_(back), centre.m_82549_(front)};
        double total = 0;
        double[] legs = new double[4];
        for (int i = 0; i < 4; i++) {
            legs[i] = corners[i + 1].m_82546_(corners[i]).m_82553_();
            total += legs[i];
        }
        double left = total * p;
        Vec3 lead = corners[0];
        for (int i = 0; i < 4 && left > 0; i++) {
            double take = Math.min(left, legs[i]);
            Vec3 end = corners[i].m_82549_(corners[i + 1].m_82546_(corners[i]).m_82490_(take / legs[i]));
            segment(buffer, matrix, normal, corners[i], end, side, 0.035, 1.0F);
            segment(buffer, matrix, normal, corners[i], end, side, 0.11, 0.35F);
            lead = end;
            left -= take;
        }
        if (p < 1.0F) {
            PieceFx.at(PieceFx.SPARK, true, 3, lead).colors(0xFFFFE0E0, RED).size(0.22, 0.04).bloom(0).spawn();
        }
    }

    /** A straight piece of the line: a band {@code width} wide across the cut's plane (both faces), red light at {@code strength}. */
    static void segment(VertexConsumer buffer, Matrix4f matrix, Matrix3f normal, Vec3 a, Vec3 b, Vec3 side, double width, float strength) {
        Vec3 w = side.m_82490_(width / 2.0);
        Vec3[] q = {a.m_82549_(w), b.m_82549_(w), b.m_82546_(w), a.m_82546_(w)};
        float r = ((RED >> 16) & 255) / 255.0F * strength;
        float g = ((RED >> 8) & 255) / 255.0F * strength;
        float bl = (RED & 255) / 255.0F * strength;
        for (int face = 0; face < 2; face++) {
            for (int k = 0; k < 4; k++) {
                Vec3 v = q[face == 0 ? k : 3 - k];
                buffer.m_252986_(matrix, (float) v.f_82479_, (float) v.f_82480_, (float) v.f_82481_).m_85950_(r, g, bl, 1.0F).m_7421_(0.5F, 0.5F)
                    .m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(normal, 0.0F, 1.0F, 0.0F).m_5752_();
            }
        }
    }

    /** The victim's own view: a thin red line rising up its middle while the cut's line is drawn round them, and staying. */
    static void screen(GuiGraphics graphics, int width, int height, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ == null || minecraft.f_91073_ == null) {
            return;
        }
        S2CFinisher doom = DOOMED.get(minecraft.f_91074_.m_19879_());
        if (doom == null) {
            return;
        }
        float p = progress(doom, minecraft.f_91073_, partial);
        if (p <= 0.0F) {
            return;
        }
        int x = width / 2;
        int top = Math.round(height * (1.0F - p));
        graphics.m_280509_(x - 3, top, x + 3, height, 0x40FF1F2E);
        graphics.m_280509_(x - 1, top, x + 1, height, 0xFFFF2B3A);
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Overlay {
        private Overlay() {
        }

        @SubscribeEvent
        public static void register(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("hexpiece_finisher", (gui, graphics, partialTick, width, height) -> screen(graphics, width, height,
                partialTick));
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        DOOMED.clear();
    }
}
