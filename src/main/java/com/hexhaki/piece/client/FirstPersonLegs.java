package com.hexhaki.piece.client;

import com.hexhaki.piece.client.npc.StylePoser;
import com.hexhaki.piece.client.render.Bulk;
import com.hexhaki.piece.style.KickLeg;
import com.hexhaki.piece.style.StyleMove;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.8.2 (the owner: "give the current M1s a first person (some are missing, like 6 powers, black leg, water kungfu and iron first)"):
 * in first person PlayerAnimator draws the arms of a move and never the legs, so a kick showed nothing. While the local player's style
 * move kicks (its animation moves a leg: Black Leg's every blow, Six Powers' Storm Leg and Air Blade, Fishman Karate's knee ...), its
 * legs are drawn here in the player's own skin and trousers, posed by the same keyframes (client/npc/StylePoser, style/KickLeg: the
 * knee bending), lifted and brought forward into the view as the game's first-person arms are (a kick at the hip would be under the
 * bottom of the screen), turned with the body's own twist. Through the game's entity type, lit as the player stands.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class FirstPersonLegs {
    private static final Map<StyleMove, Boolean> KICKS = new EnumMap<>(StyleMove.class);
    private static StyleMove move;
    private static long start;
    private static final float[] M = new float[12];
    private static final float[] N = new float[9];

    private FirstPersonLegs() {
    }

    /** The local player began {@code started} on game tick {@code tick} (StyleFx, the server's stage 0). */
    static void started(StyleMove started, long tick) {
        move = kicks(started) ? started : null;
        start = tick;
    }

    /** Whether a move's animation kicks: a leg raised or swung well away from standing at some moment of it. */
    static boolean kicks(StyleMove m) {
        return KICKS.computeIfAbsent(m, k -> {
            for (float t = 0.0F; t <= 30.0F; t += 0.5F) {
                float[][] pose = StylePoser.sample(k.animation(), t);
                for (int part = 4; part <= 5; part++) {
                    if (pose[part][StylePoser.WEIGHT] > 0.5F && (Math.abs(pose[part][3]) > 35.0F || Math.abs(pose[part][5]) > 35.0F)) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || move == null) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        if (player == null || minecraft.f_91073_ == null || minecraft.f_91066_.m_92176_() != CameraType.FIRST_PERSON) {
            return;
        }
        float partial = event.getPartialTick();
        float ticks = (float) (minecraft.f_91073_.m_46467_() - start) + partial;
        if (ticks > 40.0F) {
            move = null;
            return;
        }
        try {
            draw(event, player, StylePoser.sample(move.animation(), ticks), partial);
        } catch (RuntimeException failure) {
            move = null; // a leg never stops the game
        }
    }

    static void draw(RenderLevelStageEvent event, LocalPlayer player, float[][] pose, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        Vec3 cam = event.getCamera().m_90583_();
        Vec3 feet = player.m_20318_(partial);
        // the body's own facing (as third person turns the model), the legs hung from its hips
        double yaw = Math.toRadians(player.f_20884_ + wrap(player.f_20883_ - player.f_20884_) * partial);
        ResourceLocation skin = player.m_108560_();
        RenderType type = RenderType.m_110458_(skin);
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        VertexConsumer out = buffers.m_6299_(type);
        Bulk.read(event.getPoseStack().m_85850_(), M, N);
        int light = LevelRenderer.m_109541_(minecraft.f_91073_, BlockPos.m_274561_(feet.f_82479_, feet.f_82480_ + 1.0, feet.f_82481_));
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        double[] world = new double[24];
        for (int part = 4; part <= 5; part++) {
            if (pose[part][StylePoser.WEIGHT] < 0.05F) {
                continue;
            }
            for (int half = 0; half < 2; half++) {
                for (int layer = 0; layer < 2; layer++) {
                    double[] c = KickLeg.onBody(part, half == 1, pose, layer == 1 ? 0.25 : 0.0);
                    // the player's frame (x to their left, z ahead) turned by the body's facing, eye-centred
                    for (int k = 0; k < 8; k++) {
                        double ex = c[k * 3];
                        double ez = c[k * 3 + 2];
                        world[k * 3] = feet.f_82479_ + ex * cos - ez * sin - cam.f_82479_;
                        world[k * 3 + 1] = feet.f_82480_ + c[k * 3 + 1] - cam.f_82480_;
                        world[k * 3 + 2] = feet.f_82481_ + ex * sin + ez * cos - cam.f_82481_;
                    }
                    int[] uv = KickLeg.skin(part == 4, layer == 1);
                    for (int[] f : KickLeg.faces(half == 1, uv[0], uv[1])) {
                        face(out, world, f, light);
                    }
                }
            }
        }
        buffers.m_109912_(type);
    }

    static float wrap(float degrees) {
        float d = degrees % 360.0F;
        if (d >= 180.0F) {
            d -= 360.0F;
        }
        if (d < -180.0F) {
            d += 360.0F;
        }
        return d;
    }

    /** One face: its corners, the skin's rectangle (pixels of 64), lit by its own normal. */
    static void face(VertexConsumer out, double[] p, int[] f, int light) {
        int a = f[0];
        int b = f[1];
        int c = f[2];
        int d = f[3];
        double ux = p[b * 3] - p[a * 3];
        double uy = p[b * 3 + 1] - p[a * 3 + 1];
        double uz = p[b * 3 + 2] - p[a * 3 + 2];
        double vx = p[d * 3] - p[a * 3];
        double vy = p[d * 3 + 1] - p[a * 3 + 1];
        double vz = p[d * 3 + 2] - p[a * 3 + 2];
        float nx = (float) (uy * vz - uz * vy);
        float ny = (float) (uz * vx - ux * vz);
        float nz = (float) (ux * vy - uy * vx);
        float nl = (float) Math.max(1.0E-9, Math.sqrt(nx * nx + ny * ny + nz * nz));
        nx /= nl;
        ny /= nl;
        nz /= nl;
        float u0 = f[4] / 64.0F;
        float v0 = f[5] / 64.0F;
        float u1 = f[6] / 64.0F;
        float v1 = f[7] / 64.0F;
        int[] corner = {a, b, c, d};
        float[][] uv = {{u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}};
        for (int k = 0; k < 4; k++) {
            int i = corner[k];
            Bulk.vertex(out, M, N, (float) p[i * 3], (float) p[i * 3 + 1], (float) p[i * 3 + 2], 1.0F, 1.0F, 1.0F, 1.0F, uv[k][0], uv[k][1],
                OverlayTexture.f_118083_, light, nx, ny, nz);
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        move = null;
    }
}
