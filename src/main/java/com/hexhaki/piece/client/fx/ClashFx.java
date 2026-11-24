package com.hexhaki.piece.client.fx;

import com.hexhaki.HexHaki;
import com.hexhaki.piece.clash.ClashRules;
import com.hexhaki.piece.clash.ImpactFrame;
import com.hexhaki.piece.clash.ImpactSprite;
import com.hexhaki.piece.client.haki.HakiBolts;
import com.hexhaki.piece.network.S2CClash;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 2.3.6: a clash seen ({@code server/Clashes} sends {@link S2CClash}). Where the blows met: a white-hot flash and a ring of shock,
 * sparks fanned out square to the line they met on (steel throws orange sparks, bare blows a burst of light and dust), the ground's dust
 * blown out when it shakes; two Ryou users' colours tearing out in a sphere of lightning ({@link HakiBolts#clash}). On the screen of
 * everyone near enough ({@link ClashRules#frameReach}): the impact frame ({@link ImpactFrame}: the view's negative, the black frame, the
 * release), drawn under the HUD. Its negative is the view itself turned over (one minus its colour, by blending) and its contrast pushed
 * (a copy of the view multiplied over itself, as {@code ScreenSplit} copies it), so it stays right whatever the device draws.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class ClashFx {
    static final ResourceLocation VIEW = HexHaki.id("piece_clash_view");
    private static final Random RANDOM = new Random();
    /** The last clashes played (a viewer near both fighters is sent each twice). */
    private static final Deque<Integer> SEEN = new ArrayDeque<>();
    private static Shown shown;
    private static TextureTarget copy;
    private static boolean registered;
    /** The field of view of the last frame drawn (degrees). */
    private static double fov = 70.0;

    /** The clash on screen: where, how hard, both Ryou colours, its seed, and when it began (client level ticks). */
    record Shown(double x, double y, double z, float power, int colorA, int colorB, long seed, long start, boolean classic) {
    }

    /**
     * 3.6.3 (the owner's Buzz Cut Mochi: "a violent black-and-white impact frame ... accompanied by red Haki lightning"): the
     * black-and-white impact frame (the classic one) at {@code at} on this screen, {@code power} 0 to 3, in {@code color} (RGB).
     */
    public static void frame(Vec3 at, float power, int color, long seed) {
        net.minecraft.client.multiplayer.ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null || at == null) {
            return;
        }
        shown = new Shown(at.f_82479_, at.f_82480_, at.f_82481_, power, color, color, seed, level.m_46467_(), true);
    }

    /** 3.5.8: the owner's impact frame (clash/ImpactSprite, tools/impact_texture.py). */
    static final ResourceLocation SPRITE = HexHaki.id("textures/piece/impact/impact_frame.png");

    private ClashFx() {
    }

    public static void accept(S2CClash message) {
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || SEEN.contains(message.id())) {
            return;
        }
        SEEN.addLast(message.id());
        while (SEEN.size() > 16) {
            SEEN.removeFirst();
        }
        Vec3 at = new Vec3(message.x(), message.y(), message.z());
        Vec3 line = new Vec3(message.dx(), 0.0, message.dz());
        long seed = message.id() * 0x9E3779B97F4A7C15L ^ level.m_46467_();
        try {
            burst(at, line, message.power(), message.steel());
            if (message.ryouA() != 0 && message.ryouB() != 0) {
                HakiBolts.clash(level, at.f_82479_, at.f_82480_, at.f_82481_, floor(level, message), message.power(), message.ryouA() & 0xFFFFFF,
                    message.ryouB() & 0xFFFFFF, seed);
            }
        } catch (RuntimeException failure) {
            // effects never stop the game
        }
        // 3.5.9: the screen's impact frame only for a clash, and only on the two who clashed (the Gura ultimate's own frame kept)
        int self = minecraft.f_91074_ == null ? -1 : minecraft.f_91074_.m_19879_();
        if (!message.framed(self)) {
            return;
        }
        Camera camera = minecraft.f_91063_ == null ? null : minecraft.f_91063_.m_109153_();
        if (!message.classic() || camera == null || camera.m_90583_().m_82554_(at) <= ClashRules.frameReach(message.power())) {
            shown = new Shown(at.f_82479_, at.f_82480_, at.f_82481_, message.power(), message.ryouA(), message.ryouB(), seed, level.m_46467_(),
                message.classic());
        }
    }

    /** How far under the point the fighters stand (blocks; the lower of the two, about a chest down if neither is known). */
    static double floor(ClientLevel level, S2CClash message) {
        double below = 1.1;
        var a = level.m_6815_(message.a());
        var b = level.m_6815_(message.b());
        if (a != null) {
            below = message.y() - a.m_20186_();
        }
        if (b != null) {
            below = Math.max(below, message.y() - b.m_20186_());
        }
        return Math.max(0.2, Math.min(3.0, below));
    }

    static double r(double spread) {
        return (RANDOM.nextDouble() - 0.5) * 2.0 * spread;
    }

    /** Where the blows met: the flash, the ring, the sparks or the burst of light, the dust thrown off the ground under a hard one. */
    static void burst(Vec3 at, Vec3 line, float power, boolean steel) {
        Vec3 across = new Vec3(-line.f_82481_, 0.0, line.f_82479_);
        PieceFx.at(PieceFx.SPARK, true, 5, at).colors(0xFFFFFFFF, 0x00FFFFFF).size(1.1 + 0.45 * power, 0.1).bloom(0).spawn();
        PieceFx.at(PieceFx.RING, true, 6, at).colors(0xC0FFFFFF, 0x00FFFFFF).size(0.3, 2.2 + 0.9 * power).bloom(0).spawn();
        PieceFx.at(PieceFx.RING, true, 9, at).colors(0x70FFFFFF, 0x00FFFFFF).size(0.2, 3.4 + 1.4 * power).bloom(0).spawn();
        int tint = steel ? 0xFFFFC870 : 0xFFF4F4FF;
        int fade = steel ? 0x00FF7020 : 0x00B0C0FF;
        int n = PieceFx.count(14 + 8 * power);
        for (int i = 0; i < n; i++) {
            // fanned out square to the line the blows met on, and up: a spray off the point of contact
            Vec3 out = across.m_82490_(r(1.0)).m_82520_(0.0, r(0.8) + 0.15, 0.0).m_82549_(line.m_82490_(r(0.35)));
            double length = Math.max(1.0E-3, out.m_82553_());
            Vec3 velocity = out.m_82490_((0.35 + RANDOM.nextDouble() * 0.45) * (0.8 + 0.25 * power) / length);
            PieceFx.at(PieceFx.SPARK, true, 6 + RANDOM.nextInt(6), at.m_82520_(r(0.15), r(0.15), r(0.15))).velocity(velocity)
                .colors(tint, fade).size(steel ? 0.16 : 0.22, 0.03).drag(0.84).fall(steel ? 0.03 : 0.0).spawn();
        }
        if (ClashRules.quakes(power)) {
            Vec3 ground = at.m_82520_(0.0, -1.1, 0.0);
            int dust = PieceFx.count(10 + 6 * power);
            for (int i = 0; i < dust; i++) {
                double angle = Math.PI * 2.0 * i / dust + r(0.2);
                Vec3 out = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
                PieceFx.at(PieceFx.PUFF, false, 16 + RANDOM.nextInt(8), ground.m_82549_(out.m_82490_(0.6)))
                    .velocity(out.m_82490_(0.25 + 0.1 * power).m_82520_(0.0, 0.04, 0.0)).colors(0xC8D2C6B4, 0x00B4A894).size(0.5, 1.6)
                    .drag(0.86).spawn();
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------- the impact frame

    /** The view's colour, copied, as a texture the GUI can draw. */
    static final class View extends AbstractTexture {
        @Override
        public void m_6704_(ResourceManager manager) {
            // nothing to load: the texture is the copy's colour attachment
        }

        @Override
        public int m_117963_() {
            return copy == null ? 0 : copy.m_83975_();
        }
    }

    static void screen(GuiGraphics graphics, int width, int height, float partial) {
        Shown clash = shown;
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (clash == null || level == null || minecraft.f_91063_ == null) {
            return;
        }
        double age = level.m_46467_() - clash.start() + partial;
        if (age >= (clash.classic() ? ImpactFrame.length(clash.power()) : ImpactSprite.length()) || age < -1.0) {
            shown = null;
            return;
        }
        Camera camera = minecraft.f_91063_.m_109153_();
        Vec3 eye = camera.m_90583_();
        Vector3f look = camera.m_253058_();
        Vector3f up = camera.m_253028_();
        Vector3f left = camera.m_252775_();
        double[] point = ImpactFrame.onScreen(new double[] {clash.x(), clash.y(), clash.z()}, new double[] {eye.f_82479_, eye.f_82480_, eye.f_82481_},
            new double[] {look.x, look.y, look.z}, new double[] {up.x, up.y, up.z}, new double[] {left.x, left.y, left.z}, fov, width, height);
        if (!clash.classic()) {
            sprite(graphics, width, height, clash, age, point);
            return;
        }
        List<ImpactFrame.Batch> batches = ImpactFrame.at(age, width, height, point[0], point[1], clash.power(), clash.colorA(), clash.colorB(),
            clash.seed());
        if (batches.isEmpty()) {
            return;
        }
        Matrix4f matrix = graphics.m_280168_().m_85850_().m_252922_();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        try {
            for (ImpactFrame.Batch batch : batches) {
                if (batch.mode() == ImpactFrame.CONTRAST) {
                    contrast(graphics, width, height);
                    continue;
                }
                if (batch.mode() == ImpactFrame.INVERT) {
                    // white, one minus what is there: the view turned over where it stands
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ZERO,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                } else {
                    RenderSystem.defaultBlendFunc();
                }
                RenderSystem.setShader(GameRenderer::m_172811_);
                BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
                buffer.m_166779_(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.f_85815_);
                float[] xy = batch.xy();
                int[] argb = batch.argb();
                for (int i = 0; i < argb.length; i++) {
                    int c = argb[i];
                    buffer.m_252986_(matrix, xy[i * 2], xy[i * 2 + 1], 0.0F).m_6122_((c >> 16) & 255, (c >> 8) & 255, c & 255, (c >>> 24) & 255)
                        .m_5752_();
                }
                BufferUploader.m_231202_(buffer.m_231175_());
            }
        } finally {
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
        }
    }

    /**
     * 3.5.8: the owner's impact frame: a dark veil falling over the view with the flash and lifting again, the burst drawn over it at
     * the art's own pixels (alpha blended: its black is clear), centred where the blow landed, bigger for a harder one.
     */
    static void sprite(GuiGraphics graphics, int width, int height, Shown clash, double age, double[] point) {
        int f = ImpactSprite.frame(age);
        if (f < 0) {
            return;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            double veil = ImpactSprite.veil(age, clash.power());
            if (veil > 0.01) {
                graphics.m_280509_(0, 0, width, height, (int) Math.round(veil * 255) << 24 | 0x02030A);
            }
            int side = ImpactSprite.side(clash.power(), width, height);
            double px = Math.max(0.0, Math.min(width, point[0]));
            double py = Math.max(0.0, Math.min(height, point[1]));
            graphics.m_280411_(SPRITE, (int) Math.round(px - side / 2.0), (int) Math.round(py - side / 2.0), side, side, ImpactSprite.u(f),
                ImpactSprite.v(f), ImpactSprite.CELL, ImpactSprite.CELL, ImpactSprite.SHEET, ImpactSprite.SHEET);
        } finally {
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
        }
    }

    /** The view's contrast pushed: a copy of it drawn over it multiplied both ways (each colour c becomes 2c times c, clamped). */
    static void contrast(GuiGraphics graphics, int width, int height) {
        Minecraft minecraft = Minecraft.m_91087_();
        RenderTarget main = minecraft.m_91385_();
        int w = main.f_83915_;
        int h = main.f_83916_;
        if (w <= 0 || h <= 0) {
            return;
        }
        if (copy == null) {
            copy = new TextureTarget(w, h, false, Minecraft.f_91002_);
        } else if (copy.f_83915_ != w || copy.f_83916_ != h) {
            copy.m_83941_(w, h, Minecraft.f_91002_);
        }
        if (!registered) {
            minecraft.m_91097_().m_118495_(VIEW, new View());
            registered = true;
        }
        // the frame so far into the copy (GL_READ_FRAMEBUFFER, GL_DRAW_FRAMEBUFFER, GL_COLOR_BUFFER_BIT, GL_NEAREST), then back
        GlStateManager._glBindFramebuffer(0x8CA8, main.f_83920_);
        GlStateManager._glBindFramebuffer(0x8CA9, copy.f_83920_);
        GlStateManager._glBlitFrameBuffer(0, 0, w, h, 0, 0, w, h, 0x4000, 0x2600);
        main.m_83947_(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        // the framebuffer's rows run bottom up: v from its height down to 0
        graphics.m_280411_(VIEW, 0, 0, width, height, 0, h, w, -h, w, h);
    }

    /** The world's field of view (the hand's is asked for too, at a fixed 70: left out). */
    @SubscribeEvent
    public static void clashFov(ViewportEvent.ComputeFov event) {
        if (!event.usedConfiguredFov()) {
            return;
        }
        double value = event.getFOV();
        if (value > 1.0 && value < 179.0) {
            fov = value;
        }
    }

    @SubscribeEvent
    public static void clashLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        shown = null;
        SEEN.clear();
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Overlay {
        private Overlay() {
        }

        /** Under the HUD: the view itself flashes, the hotbar and the bars stay readable. */
        @SubscribeEvent
        public static void registerClashOverlay(RegisterGuiOverlaysEvent event) {
            event.registerBelowAll("hexpiece_clash", (gui, graphics, partialTick, width, height) -> screen(graphics, width, height, partialTick));
        }
    }
}
