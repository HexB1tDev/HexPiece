package com.hexhaki.piece.client.haki;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 2.4.7: the glow round Ryou's fists, the game's own glowing outline (the owner: "the glow around the outline is not meant to be literal
 * painted lines, it's meant to be kinda like the minecraft glow effect around the firsts, thats it").
 * <p>
 * The game outlines a glowing entity by drawing its silhouette in its colour into a framebuffer of its own, running the post chain
 * {@code shaders/post/entity_outline.json} over it (the silhouette's edges, blurred into a soft band) and laying the result over the
 * frame. This does the same for the fists only: while the hand layer draws a Ryou hand ({@link RyouHands#glow}) its box is recorded
 * here as it lands on the screen; once the level is drawn the boxes go into this chain's own target, the chain runs, and the outline is
 * blended over the frame exactly as the game blends its own. (2.4.7 copied the world's depth in first, so walls hid the glow; 2.5.2:
 * not any more, it flickered on the owner's device; the glow shows through walls as the game's own Glowing outline does.) The first-person hand is drawn after the level, in its own projection: its fists are
 * outlined just before the HUD. No painted lines, no geometry that depends on culling: the game's own shaders and blending.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class RyouGlow {
    static final ResourceLocation OUTLINE = new ResourceLocation("minecraft", "shaders/post/entity_outline.json");
    /** The fists recorded this frame: in the world (third person, every player near) and in the first-person hand. */
    private static final Boxes WORLD = new Boxes();
    private static final Boxes HAND = new Boxes();
    /** The level's entities are drawn: what is recorded from here on is the first-person hand. */
    private static boolean levelDone;
    private static PostChain chain;
    private static RenderTarget target;
    private static int width;
    private static int height;
    /** The chain could not be made or failed: no glow (the hands are still black). */
    private static boolean broken;
    /**
     * 2.5.2 (the owner: "the glow is too intense, and is flickering on and off"): the fists' silhouette is drawn this strong (the
     * outline's brightness goes about with its square: a little under half the game's own Glowing outline).
     */
    static final float INTENSITY = 0.65F;

    private RyouGlow() {
    }

    /** Faces as quads on the screen: x, y, depth (the game's normalised device coordinates) for each corner, and a colour each. */
    static final class Boxes {
        float[] xyz = new float[12 * 48];
        int[] argb = new int[48];
        int size;

        void add(float[] quad, int colour) {
            if (this.size == this.argb.length) {
                this.xyz = java.util.Arrays.copyOf(this.xyz, this.xyz.length * 2);
                this.argb = java.util.Arrays.copyOf(this.argb, this.argb.length * 2);
            }
            System.arraycopy(quad, 0, this.xyz, this.size * 12, 12);
            this.argb[this.size++] = colour;
        }
    }

    /**
     * A fist's box, its eight corners (arm space, blocks; the bottom four then the top four, each square in the same order) drawn
     * through {@code pose}, in {@code rgb} as strong as {@code strength} (0..1: the outline's brightness).
     */
    public static void box(Matrix4f pose, float[][] corners, int rgb, float strength) {
        if (broken) {
            return;
        }
        Matrix4f view = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(pose);
        Matrix4f projection = RenderSystem.getProjectionMatrix();
        float[][] screen = new float[8][];
        for (int i = 0; i < 8; i++) {
            Vector3f at = view.transformPosition(new Vector3f(corners[i][0], corners[i][1], corners[i][2]));
            if (at.z > -0.02F) {
                return; // behind the eye (or at it): not on the screen
            }
            Vector3f ndc = projection.transformProject(new Vector3f(at.x, at.y, at.z));
            screen[i] = new float[] {ndc.x, ndc.y, ndc.z};
        }
        int alpha = Math.round(255.0F * INTENSITY * Math.max(0.0F, Math.min(1.0F, strength)));
        int colour = alpha << 24 | (rgb & 0xFFFFFF);
        int[][] faces = {{0, 1, 2, 3}, {4, 5, 6, 7}, {0, 1, 5, 4}, {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7}};
        Boxes into = levelDone ? HAND : WORLD;
        for (int[] face : faces) {
            float[] quad = new float[12];
            for (int k = 0; k < 4; k++) {
                System.arraycopy(screen[face[k]], 0, quad, k * 3, 3);
            }
            into.add(quad, colour);
        }
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            // a new frame: what was left from the last (the HUD hidden, so its hand never outlined) is dropped
            levelDone = false;
            WORLD.size = 0;
            HAND.size = 0;
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            // the level's opaque world, its entities and particles are drawn: their depth is complete
            // 2.5.2: no depth copy (on the owner's device, OpenGL ES through MobileGlues, copying the depth between framebuffers every
            // frame is not reliable: the glow flickered on and off); the glow shows through walls, as the game's own Glowing does
            composite(WORLD, false, event.getPartialTick());
            levelDone = true;
        }
    }

    @SubscribeEvent
    public static void hud(RenderGuiOverlayEvent.Pre event) {
        if (HAND.size > 0) {
            composite(HAND, false, Minecraft.m_91087_().m_91296_());
        }
        levelDone = false;
    }

    /** The boxes drawn into the chain's target, outlined by the game's glowing-outline chain, blended over the frame. */
    static void composite(Boxes boxes, boolean depth, float partial) {
        if (boxes.size == 0 || broken) {
            boxes.size = 0;
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        RenderTarget main = minecraft.m_91385_();
        try {
            if (!ready(minecraft, main)) {
                return;
            }
            target.m_83931_(0.0F, 0.0F, 0.0F, 0.0F);
            target.m_83954_(Minecraft.f_91002_);
            if (depth) {
                target.m_83945_(main);
            }
            target.m_83947_(false);
            draw(boxes, depth);
            chain.m_110023_(partial);
            main.m_83947_(false);
            // laid over the frame as the game lays its own glowing outline (LevelRenderer.doEntityOutline)
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            target.m_83957_(main.f_83915_, main.f_83916_, false); // 2.5.1: m_83957_ (2.4.7 named m_83961_, clear-depth (Z)V: it crashed)
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        } catch (Exception | LinkageError failure) {
            // a glow that cannot be drawn is never drawn again this session; the hands stay black (2.5.1: a missing method too, as
            // the owner's crash was: it threw a LinkageError, which this did not catch)
            broken = true;
        } finally {
            boxes.size = 0;
            main.m_83947_(false);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
        }
    }

    /** The chain made (once) and kept the frame's size. */
    static boolean ready(Minecraft minecraft, RenderTarget main) throws java.io.IOException {
        if (chain == null) {
            chain = new PostChain(minecraft.m_91097_(), minecraft.m_91098_(), main, OUTLINE);
            chain.m_110025_(main.f_83915_, main.f_83916_);
            target = chain.m_110036_("final");
            width = main.f_83915_;
            height = main.f_83916_;
        } else if (width != main.f_83915_ || height != main.f_83916_) {
            chain.m_110025_(main.f_83915_, main.f_83916_);
            width = main.f_83915_;
            height = main.f_83916_;
        }
        if (target == null) {
            broken = true;
            return false;
        }
        return true;
    }

    /**
     * The boxes, flat in their colours, where they lie on the screen: each corner taken back through the projection and view the game
     * draws with now, so the shader puts it exactly there; tested against the copied depth in the world (never written), not in the hand.
     */
    static void draw(Boxes boxes, boolean depth) {
        Matrix4f back = new Matrix4f(RenderSystem.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix()).invert();
        if (depth) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShader(GameRenderer::m_172811_);
        BufferBuilder buffer = Tesselator.m_85913_().m_85915_();
        buffer.m_166779_(VertexFormat.Mode.QUADS, DefaultVertexFormat.f_85815_);
        for (int q = 0; q < boxes.size; q++) {
            int c = boxes.argb[q];
            float r = ((c >> 16) & 255) / 255.0F;
            float g = ((c >> 8) & 255) / 255.0F;
            float b = (c & 255) / 255.0F;
            float a = ((c >>> 24) & 255) / 255.0F;
            for (int k = 0; k < 4; k++) {
                Vector3f at = back.transformProject(new Vector3f(boxes.xyz[q * 12 + k * 3], boxes.xyz[q * 12 + k * 3 + 1],
                    boxes.xyz[q * 12 + k * 3 + 2]));
                buffer.m_5483_(at.x, at.y, at.z).m_85950_(r, g, b, a).m_5752_();
            }
        }
        BufferUploader.m_231202_(buffer.m_231175_());
    }
}
