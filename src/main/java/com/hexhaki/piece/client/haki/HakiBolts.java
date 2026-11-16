package com.hexhaki.piece.client.haki;

import com.hexhaki.piece.client.sword.SwordFx;
import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.BoltMesh;
import com.hexhaki.piece.haki.BoltPainter;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
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
 * 2.1.5: the 3D Haki lightning in the world (the owner: "We really need to rework the lightning of the Haki so it's actual 3D lightning
 * ... with a black outline ... That is the most important thing about the mod").
 *
 * <p>The original builds each Haki effect as a Photon composition, lays its lightning in it as flat textured beams, and plays it:
 * following an entity ({@code HakiVfx.play}: at the entity's feet, turned with it, Photon's XROT), or fixed in the world
 * ({@code playAtWorld}, {@code playAtFixed}). {@link HakiLightning} now also paints every beam as {@link Bolt}s and keeps them with the
 * composition ({@link #pending}); the patch has the three players ask {@link #played} first, which takes the composition's beams back
 * out, sets its bolts going from the same anchor, and lets the rest of the composition (its particles, rings, flashes) play as before.
 * A composition that never goes through those players (the Galaxy Fist's charge, which keeps its own effect) keeps its beams: nothing
 * is ever lost.
 *
 * <p>Every frame each live bolt becomes {@link BoltMesh} geometry in camera space: the outline, the body, the core and its heart in one
 * opaque, unlit, fogged batch ({@code RenderType.textBackground}), then the glow in one additive batch ({@code eyes}), so the world and
 * the bodies in front of a bolt hide it, and the glow never paints over its own ink.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class HakiBolts {
    /** Most strokes alive at once; past it a composition keeps the original's beams. */
    static final int MOST = 5000;
    /** A pixel one block from the eye (70 degrees on a 1080-line screen): the floor for the ink's width. */
    static final double PIXEL = 2.0 * Math.tan(Math.toRadians(35.0)) / 1080.0;
    /** A composition not played within this long is forgotten (its beams stay with it). */
    static final long STALE_NANOS = 5_000_000_000L;
    static final Map<FX, Pending> PENDING = new IdentityHashMap<>();
    static final List<Live> LIVE = new ArrayList<>();
    static int alive;
    private static final Quads SOLID = new Quads();
    private static final Quads GLOW = new Quads();

    /** The bolts painted into one composition before it is played, and the beams they stand in for. */
    static final class Pending {
        final List<Bolt> bolts = new ArrayList<>();
        final List<IFXObject> beams = new ArrayList<>();
        final BoltPainter.Chain chain = new BoltPainter.Chain();
        final long made = System.nanoTime();
    }

    /** Bolts set going: at a fixed point, or following {@code entity} (feet, turned with it). */
    record Live(List<Bolt> bolts, Entity entity, double x, double y, double z, long start, float end) {
    }

    private HakiBolts() {
    }

    /** The bolts being painted into {@code fx}. */
    static Pending pending(FX fx) {
        Pending pending = PENDING.get(fx);
        if (pending == null) {
            if (PENDING.size() > 64) {
                long now = System.nanoTime();
                PENDING.values().removeIf(p -> now - p.made > STALE_NANOS);
            }
            pending = new Pending();
            PENDING.put(fx, pending);
        }
        return pending;
    }

    /** {@code HakiVfx.play(entity, fx)}: true if nothing of the composition is left for Photon to play. */
    public static boolean played(FX fx, Entity entity) {
        return entity != null && launch(fx, entity, entity.m_9236_(), 0, 0, 0);
    }

    /** {@code HakiVfx.playAtWorld(entity, x, y, z, fx)}. */
    public static boolean playedAt(FX fx, Entity entity, double x, double y, double z) {
        return entity != null && launch(fx, null, entity.m_9236_(), x, y, z);
    }

    /** {@code HakiVfx.playAtFixed(level, x, y, z, fx)}. */
    public static boolean playedFixed(FX fx, ClientLevel level, double x, double y, double z) {
        return launch(fx, null, level, x, y, z);
    }

    static boolean launch(FX fx, Entity entity, Level level, double x, double y, double z) {
        try {
            Pending pending = PENDING.remove(fx);
            if (pending == null || pending.bolts.isEmpty() || level == null || alive + pending.bolts.size() > MOST) {
                return false;
            }
            List<IFXObject> objects = fx.getMainFX().objects();
            objects.removeIf(o -> containsSame(pending.beams, o));
            BoltPainter.point(pending.bolts);
            float end = 0;
            for (Bolt bolt : pending.bolts) {
                end = Math.max(end, bolt.end());
            }
            LIVE.add(new Live(pending.bolts, entity, x, y, z, level.m_46467_(), end));
            alive += pending.bolts.size();
            return objects.isEmpty();
        } catch (RuntimeException failure) {
            return false; // the original plays it as it was
        }
    }

    static boolean containsSame(List<IFXObject> list, Object o) {
        for (IFXObject item : list) {
            if (item == o) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || LIVE.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            LIVE.clear();
            alive = 0;
            return;
        }
        try {
            draw(event.getPoseStack(), event.getCamera().m_90583_(), level, event.getPartialTick());
        } catch (RuntimeException failure) {
            LIVE.clear(); // lightning never stops the game
            alive = 0;
        }
    }

    /** How much the glow shows under this sky: 1 at night, half at noon. */
    public static double shine(ClientLevel level, float partial) {
        double sky = level.m_104805_(partial);
        return 1.0 - 0.5 * Math.max(0.0, Math.min(1.0, (sky - 0.25) / 0.75));
    }

    static void draw(PoseStack pose, Vec3 cam, ClientLevel level, float partial) {
        long now = level.m_46467_();
        double shine = shine(level, partial);
        SOLID.clear();
        GLOW.clear();
        BoltMesh.Sink sink = (glow, xyz, argb) -> (glow ? GLOW : SOLID).add(xyz, argb);
        Iterator<Live> it = LIVE.iterator();
        while (it.hasNext()) {
            Live live = it.next();
            double ticks = (now - live.start) + partial;
            if (ticks > live.end + 1 || live.entity != null && (live.entity.m_213877_() || live.entity.m_9236_() != level)) {
                it.remove();
                alive -= live.bolts.size();
                continue;
            }
            BoltMesh.Frame frame;
            if (live.entity != null) {
                Vec3 at = live.entity.m_20318_(partial);
                float yaw = live.entity.f_19859_ + wrap(live.entity.m_146908_() - live.entity.f_19859_) * partial;
                frame = BoltMesh.Frame.turned(at.f_82479_ - cam.f_82479_, at.f_82480_ - cam.f_82480_, at.f_82481_ - cam.f_82481_,
                    Math.toRadians(-90.0 - yaw));
            } else {
                frame = BoltMesh.Frame.at(live.x - cam.f_82479_, live.y - cam.f_82480_, live.z - cam.f_82481_);
            }
            for (Bolt bolt : live.bolts) {
                BoltMesh.build(bolt, frame, ticks - bolt.delay, PIXEL, shine, sink);
            }
        }
        write(Minecraft.m_91087_().m_91269_().m_110104_(), pose.m_85850_().m_252922_(), pose.m_85850_().m_252943_(), 0, 0, 0, true);
    }

    /**
     * Bolts in a model's own frame (a katana's: the pose of the layer drawing it): the eye is found in that frame by undoing the pose,
     * the bolts are built round it and drawn through the pose, so they ride on the blade wherever the animation puts it. {@code ages}
     * holds each bolt's age (ticks after its delay). Used inside the entity pass: the batches are left for the game to draw.
     */
    public static void drawInFrame(PoseStack pose, MultiBufferSource buffers, List<Bolt> bolts, double[] ages, float partial) {
        if (bolts.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        double shine = level == null ? 1.0 : shine(level, partial);
        Matrix4f matrix = pose.m_85850_().m_252922_();
        Vector3f eye = new Matrix4f(matrix).invert().transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
        SOLID.clear();
        GLOW.clear();
        BoltMesh.Sink sink = (glow, xyz, argb) -> (glow ? GLOW : SOLID).add(xyz, argb);
        BoltMesh.Frame frame = BoltMesh.Frame.at(-eye.x, -eye.y, -eye.z);
        for (int i = 0; i < bolts.size(); i++) {
            BoltMesh.build(bolts.get(i), frame, ages[i], PIXEL, shine, sink);
        }
        write(buffers, matrix, pose.m_85850_().m_252943_(), eye.x, eye.y, eye.z, false);
    }

    /**
     * Bolts already placed in camera space (world minus the camera), each at its age, drawn now with the level's camera pose
     * ({@code pose} as RenderLevelStageEvent hands it: turned to the view, not moved): the flying Haki slash's crackle.
     */
    public static void drawInView(PoseStack pose, List<Bolt> bolts, double[] ages, ClientLevel level, float partial) {
        if (bolts.isEmpty()) {
            return;
        }
        double shine = shine(level, partial);
        SOLID.clear();
        GLOW.clear();
        BoltMesh.Sink sink = (glow, xyz, argb) -> (glow ? GLOW : SOLID).add(xyz, argb);
        BoltMesh.Frame frame = BoltMesh.Frame.at(0, 0, 0);
        for (int i = 0; i < bolts.size(); i++) {
            BoltMesh.build(bolts.get(i), frame, ages[i], PIXEL, shine, sink);
        }
        write(Minecraft.m_91087_().m_91269_().m_110104_(), pose.m_85850_().m_252922_(), pose.m_85850_().m_252943_(), 0, 0, 0, true);
    }

    /** The gathered quads, moved by (ox, oy, oz) and through {@code matrix}: the ink batch, then the glow batch. */
    static void write(MultiBufferSource buffers, Matrix4f matrix, Matrix3f normal, double ox, double oy, double oz, boolean end) {
        if (SOLID.size > 0) {
            RenderType ink = RenderType.m_269058_();
            VertexConsumer buffer = buffers.m_6299_(ink);
            for (int q = 0; q < SOLID.size; q++) {
                for (int k = 0; k < 4; k++) {
                    int c = SOLID.colors[q * 4 + k];
                    buffer.m_252986_(matrix, (float) (SOLID.xyz[q * 12 + k * 3] + ox), (float) (SOLID.xyz[q * 12 + k * 3 + 1] + oy),
                            (float) (SOLID.xyz[q * 12 + k * 3 + 2] + oz))
                        .m_85950_(((c >>> 16) & 255) / 255.0F, ((c >>> 8) & 255) / 255.0F, (c & 255) / 255.0F, 1.0F).m_85969_(0xF000F0).m_5752_();
                }
            }
            if (end && buffers instanceof MultiBufferSource.BufferSource source) {
                source.m_109912_(ink);
            }
        }
        if (GLOW.size > 0) {
            RenderType light = RenderType.m_110488_(SwordFx.WHITE_TEXTURE);
            VertexConsumer buffer = buffers.m_6299_(light);
            for (int q = 0; q < GLOW.size; q++) {
                for (int k = 0; k < 4; k++) {
                    int c = GLOW.colors[q * 4 + k];
                    buffer.m_252986_(matrix, (float) (GLOW.xyz[q * 12 + k * 3] + ox), (float) (GLOW.xyz[q * 12 + k * 3 + 1] + oy),
                            (float) (GLOW.xyz[q * 12 + k * 3 + 2] + oz))
                        .m_85950_(((c >>> 16) & 255) / 255.0F, ((c >>> 8) & 255) / 255.0F, (c & 255) / 255.0F, 1.0F).m_7421_(0.5F, 0.5F)
                        .m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(normal, 0.0F, 1.0F, 0.0F).m_5752_();
                }
            }
            if (end && buffers instanceof MultiBufferSource.BufferSource source) {
                source.m_109912_(light);
            }
        }
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

    /** Quads gathered for one batch (reused frame to frame). */
    static final class Quads {
        float[] xyz = new float[12 * 1024];
        int[] colors = new int[4 * 1024];
        int size;

        void clear() {
            this.size = 0;
        }

        void add(double[] v, int[] c) {
            if ((this.size + 1) * 12 > this.xyz.length) {
                this.xyz = java.util.Arrays.copyOf(this.xyz, this.xyz.length * 2);
                this.colors = java.util.Arrays.copyOf(this.colors, this.colors.length * 2);
            }
            for (int i = 0; i < 12; i++) {
                this.xyz[this.size * 12 + i] = (float) v[i];
            }
            System.arraycopy(c, 0, this.colors, this.size * 4, 4);
            this.size++;
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        PENDING.clear();
        LIVE.clear();
        alive = 0;
    }
}
