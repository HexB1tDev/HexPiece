package com.hexhaki.piece.client.sword;

import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.network.S2CSwordFx;
import com.hexhaki.piece.sword.SwordAnims;
import com.hexhaki.piece.sword.SwordMove;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 2.1.4: what a sword fight looks like besides the blade (the owner: "Use restrained blade trails, brief edge glints and sharply timed
 * impact effects ... Trails must follow the actual blade and fade quickly ... Ordinary sword attacks should not emit fire or unrelated
 * elemental effects"):
 * <ul>
 *   <li>the trail: while a cut's window is open, the blade as it is drawn this frame ({@link #record}, from the sword layer itself:
 *       the very blade on screen, first person included) is kept for {@link #TRAIL_LIFE} seconds and drawn as a thin ribbon from the
 *       middle of the blade to its point, pale steel, fading out as it ages;</li>
 *   <li>a cut that lands ({@link S2CSwordFx#HIT}): a glint on the edge, a short fan of sparks along the cut and a ring, timed on the
 *       server's hit tick;</li>
 *   <li>the charged cut's release and the quick draw: dust and a line of cut air.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class SwordFx {
    public static final ResourceLocation WHITE_TEXTURE = ResourceLocation.m_214293_("hexhaki", "textures/piece/white.png");
    static final double TRAIL_LIFE = 0.16;
    static final int STEEL = 0xFFE6EEF8;
    private static final Random RANDOM = new Random();
    private static final Map<Integer, ArrayDeque<double[]>> TRAILS = new HashMap<>();
    /** The view rotation of this frame (columns of the camera's turn, from the level's first stage), to take a layer's points to the world. */
    private static final float[][] VIEW = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

    private SwordFx() {
    }

    public static void accept(S2CSwordFx message) {
        Vec3 at = new Vec3(message.x(), message.y(), message.z());
        if (!PieceFx.near(at, 64.0)) {
            return;
        }
        Vec3 dir = new Vec3(message.dx(), 0.0, message.dz());
        dir = dir.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : dir.m_82541_();
        switch (message.kind()) {
            case S2CSwordFx.HIT -> hit(at, dir, (int) message.dy(), message.color());
            case S2CSwordFx.RELEASE -> release(at, dir, message.dy());
            case S2CSwordFx.QUICK -> quick(at, dir);
            default -> {
            }
        }
    }

    static double r(double spread) {
        return (RANDOM.nextDouble() - 0.5) * 2.0 * spread;
    }

    /** A cut landed: an edge glint, sparks fanned along the cut, a ring. */
    static void hit(Vec3 at, Vec3 dir, int weight, int color) {
        int tint = color == 0 ? 0xFFFFF6E0 : color | 0xFF000000;
        Vec3 side = new Vec3(-dir.f_82481_, 0, dir.f_82479_);
        PieceFx.at(PieceFx.SPARK, true, 4, at).colors(0xFFFFFFFF, tint & 0x00FFFFFF).size(0.7 + weight * 0.12, 0.05).bloom(0).spawn();
        int n = PieceFx.count(8 + weight * 4);
        for (int i = 0; i < n; i++) {
            Vec3 velocity = dir.m_82490_(0.25 + RANDOM.nextDouble() * 0.35).m_82549_(side.m_82490_(r(0.35))).m_82520_(0, r(0.25) + 0.05, 0);
            PieceFx.at(PieceFx.SPARK, true, 5 + RANDOM.nextInt(4), at.m_82520_(r(0.2), r(0.3), r(0.2))).velocity(velocity)
                .colors(tint, 0x00FF9040).size(0.14, 0.03).drag(0.82).fall(0.02).spawn();
        }
        PieceFx.at(PieceFx.RING, true, 5, at).colors(0x80FFFFFF, 0x00FFFFFF).size(0.2, 1.2 + 0.3 * weight).bloom(0).spawn();
    }

    /** The charged cut released without Haki: the ground's dust thrown forward and a wide cut of air. */
    static void release(Vec3 feet, Vec3 dir, double charge) {
        Vec3 point = feet.m_82549_(dir.m_82490_(1.6)).m_82520_(0, 1.0, 0);
        PieceFx.at(PieceFx.SLASH, false, 6, point).velocity(dir.m_82490_(0.25)).colors(0xB0FFFFFF, 0x00FFFFFF)
            .size(1.6 + charge, 2.8 + charge * 1.5).drag(0.8).bloom(0).spawn();
        int n = PieceFx.count(10 + (int) (charge * 10));
        for (int i = 0; i < n; i++) {
            PieceFx.at(PieceFx.PUFF, false, 14 + RANDOM.nextInt(8), feet.m_82549_(dir.m_82490_(0.8 + RANDOM.nextDouble() * 1.5))
                .m_82520_(r(0.6), 0.1, r(0.6))).velocity(dir.m_82490_(0.12).m_82520_(0, 0.03, 0)).colors(0x90B8AC98, 0x00B8AC98)
                .size(0.5, 1.4).drag(0.86).spawn();
        }
    }

    /** The quick draw: a line of cut air down the dash. */
    static void quick(Vec3 feet, Vec3 dir) {
        for (int i = 0; i < PieceFx.count(14); i++) {
            double along = RANDOM.nextDouble() * 5.0;
            PieceFx.at(PieceFx.STREAK, false, 5 + RANDOM.nextInt(4), feet.m_82549_(dir.m_82490_(along)).m_82520_(r(0.3), 0.9 + r(0.5), r(0.3)))
                .velocity(dir.m_82490_(0.6)).colors(0xD0FFFFFF, 0x00FFFFFF).size(0.5, 0.06).drag(0.8).bloom(0).spawn();
        }
    }

    // --- trails -----------------------------------------------------------------------------------------------------------------

    /** Is a cut's window open {@code ticks} into {@code anim} (or about to: the trail leads the hit by a tick)? */
    static boolean cutting(int anim, float ticks) {
        if (anim >= SwordAnims.MOVES) {
            SwordMove move = SwordMove.byOrdinal(anim - SwordAnims.MOVES);
            if (move != null) {
                for (SwordMove.Cut cut : move.cuts()) {
                    if (ticks >= cut.from() - 1.5F && ticks <= cut.to() + 0.5F) {
                        return true;
                    }
                }
            }
            return false;
        }
        return anim == SwordAnims.RELEASE && ticks >= 0.5F && ticks <= 3.5F
            || (anim == SwordAnims.QUICK_DRAW || anim == SwordAnims.QUICK_CUT) && ticks >= 3.0F && ticks <= 7.0F;
    }

    /** From the sword layer, the blade drawn now: its middle and its point in the pose's frame (model px along the blade). */
    static void record(AbstractClientPlayer player, PoseStack pose, float ticks) {
        if (!cutting(SwordClient.anim(player.m_19879_()), ticks)) {
            return;
        }
        Matrix4f matrix = pose.m_85850_().m_252922_();
        float px = KatanaRenderer.px(SwordClient.katana(player.m_19879_())); // 2.6.2: the blade's own size
        double[] a = world(matrix, 0.0F, 9.0F * px, 0.0F);
        double[] b = world(matrix, 0.0F, 21.0F * px, 0.0F);
        ArrayDeque<double[]> trail = TRAILS.computeIfAbsent(player.m_19879_(), id -> new ArrayDeque<>());
        trail.add(new double[] {System.nanoTime() / 1.0E9, a[0], a[1], a[2], b[0], b[1], b[2]});
        while (trail.size() > 48) {
            trail.poll();
        }
    }

    /** A point of the pose's frame in the world (the camera's position and turn undone). */
    static double[] world(Matrix4f matrix, float x, float y, float z) {
        Vector3f v = new Vector3f(x, y, z);
        matrix.transformPosition(v);
        Camera camera = Minecraft.m_91087_().f_91063_.m_109153_();
        Vec3 cam = camera.m_90583_();
        double wx = VIEW[0][0] * v.x + VIEW[0][1] * v.y + VIEW[0][2] * v.z;
        double wy = VIEW[1][0] * v.x + VIEW[1][1] * v.y + VIEW[1][2] * v.z;
        double wz = VIEW[2][0] * v.x + VIEW[2][1] * v.y + VIEW[2][2] * v.z;
        return new double[] {cam.f_82479_ + wx, cam.f_82480_ + wy, cam.f_82481_ + wz};
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            // the view's turn this frame: each world axis as the level's pose stack turns it; its transpose takes view to world
            Matrix4f matrix = event.getPoseStack().m_85850_().m_252922_();
            for (int i = 0; i < 3; i++) {
                Vector3f axis = new Vector3f(i == 0 ? 1 : 0, i == 1 ? 1 : 0, i == 2 ? 1 : 0);
                matrix.transformPosition(axis);
                VIEW[i][0] = axis.x;
                VIEW[i][1] = axis.y;
                VIEW[i][2] = axis.z;
            }
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES && !TRAILS.isEmpty()) {
            try {
                drawTrails(event.getPoseStack(), event.getCamera().m_90583_());
            } catch (RuntimeException failure) {
                TRAILS.clear();
            }
        }
    }

    static void drawTrails(PoseStack pose, Vec3 cam) {
        double now = System.nanoTime() / 1.0E9;
        MultiBufferSource.BufferSource buffers = Minecraft.m_91087_().m_91269_().m_110104_();
        // 2.1.9 (the owner: "sword swings are black"): alpha blended, not additive: some setups draw additive "eyes" geometry opaque,
        // and the trail's dim additive steel came out as a dark sheet; a pale streak whose opacity is its strength reads the same anywhere
        RenderType type = RenderType.m_269166_();
        VertexConsumer buffer = buffers.m_6299_(type);
        pose.m_85836_();
        pose.m_252880_((float) -cam.f_82479_, (float) -cam.f_82480_, (float) -cam.f_82481_);
        Matrix4f matrix = pose.m_85850_().m_252922_();
        Matrix3f normal = pose.m_85850_().m_252943_();
        Iterator<Map.Entry<Integer, ArrayDeque<double[]>>> it = TRAILS.entrySet().iterator();
        while (it.hasNext()) {
            ArrayDeque<double[]> trail = it.next().getValue();
            while (!trail.isEmpty() && now - trail.peek()[0] > TRAIL_LIFE) {
                trail.poll();
            }
            if (trail.size() < 2) {
                if (trail.isEmpty()) {
                    it.remove();
                }
                continue;
            }
            double[] previous = null;
            for (double[] s : trail) {
                if (previous != null) {
                    float a0 = (float) Math.max(0.0, 1.0 - (now - previous[0]) / TRAIL_LIFE) * 0.42F;
                    float a1 = (float) Math.max(0.0, 1.0 - (now - s[0]) / TRAIL_LIFE) * 0.42F;
                    ribbon(buffer, matrix, normal, previous, s, a0, a1);
                }
                previous = s;
            }
        }
        pose.m_85849_();
        buffers.m_109912_(type);
    }

    /** One stretch of the ribbon, both faces: pale steel, as opaque as its strength (strongest along the edge, fading behind). */
    static void ribbon(VertexConsumer buffer, Matrix4f matrix, Matrix3f normal, double[] p, double[] q, float a0, float a1) {
        float r = ((STEEL >> 16) & 255) / 255.0F;
        float g = ((STEEL >> 8) & 255) / 255.0F;
        float bl = (STEEL & 255) / 255.0F;
        double[][] corners = {{p[1], p[2], p[3], a0 * 0.35}, {p[4], p[5], p[6], a0}, {q[4], q[5], q[6], a1}, {q[1], q[2], q[3], a1 * 0.35}};
        for (int side = 0; side < 2; side++) {
            for (int k = 0; k < 4; k++) {
                double[] c = corners[side == 0 ? k : 3 - k];
                buffer.m_252986_(matrix, (float) c[0], (float) c[1], (float) c[2]).m_85950_(r, g, bl, (float) Math.min(0.8, c[3] * 1.3)).m_5752_();
            }
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        TRAILS.clear();
    }
}
