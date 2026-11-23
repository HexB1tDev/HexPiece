package com.hexhaki.piece.client.gear;

import com.hexhaki.piece.client.LoopSound;
import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gear.GearSounds;
import com.hexhaki.piece.gear.NikaDawn;
import com.hexhaki.piece.gear.NikaGoof;
import com.hexhaki.piece.horo.AstralArt;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.NikaWhip;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.4.4: Goof Ball and Dawn Whip on every client (server/NikaWhip's S2CUoFx kinds 165 to 172; client/dragon/UoFx routes them here):
 * <ul>
 *   <li>Goof Ball: from {@link NikaGoof#SPIN} ticks after it starts the body is not drawn (RenderPlayerEvent.Pre cancelled, as the Horo
 *       spirit's) and the ball is (gear/NikaGoof: the cloud, then the limbs in the user's Gear 5 skin, the original's
 *       GearJacketSkin.resolve, the white skin the body wears); its spin loops through LoopSound while it lasts; the bursts where its
 *       punches land; the pop; the dizzy stars over the head after it (and one's own view swaying a little). One's own first person:
 *       no hands, the cloud round the view and one's own Gear 5 fists jabbing in;</li>
 *   <li>Dawn Whip: the wind-up's tension lines, the smear of the leg sweeping round, the crack on each body struck, the overspin's
 *       arcs, the drill's spray of earth, the pop out of the hole (gear/NikaDawn). One's own first person: the smear, the cracks and the
 *       earth, the speed streaks across the view instead of the arcs round one's own body.</li>
 * </ul>
 * Everything at AFTER_PARTICLES through the game's debugQuads (alpha blended, depth tested: the owner's phone), skins first in their own
 * batch (a shared builder must not change its format mid-batch: 2.9.0's lesson), capped counts.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class NikaWhipClient {
    static final int MOST = 24;
    static final double FAR = 96.0;

    static final class Goof {
        double start;
        int seed;
        double popped = -1;
        LoopSound sound;
    }

    static final class Dawn {
        double start;
        double wx;
        double wz;
        double reach;
        int seed;
        double drill = -1;
        double dx;
        double dy;
        double dz;
        boolean torn;
    }

    /** A thing left in the world for a while: a burst, a crack, a pop, the hole's pop. */
    static final class Mark {
        int kind;
        double x;
        double y;
        double z;
        double wx;
        double wz;
        double size;
        double born;
        int seed;
    }

    static final Map<Integer, Goof> GOOFS = new HashMap<>();
    static final Map<Integer, Dawn> DAWNS = new HashMap<>();
    /** The dazed: user id -> {since, how long}. */
    static final Map<Integer, double[]> DIZZY = new HashMap<>();
    static final List<Mark> MARKS = new ArrayList<>();
    static double clock;

    private NikaWhipClient() {
    }

    static double now() {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? 0 : level.m_46467_();
    }

    /** From client/dragon/UoFx: one of server/NikaWhip's kinds. */
    public static void accept(S2CUoFx m) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        double now = level.m_46467_();
        switch (m.kind()) {
            case NikaWhip.GOOF -> {
                Goof goof = new Goof();
                goof.start = now;
                goof.seed = m.ticks();
                stopSound(GOOFS.put(m.user(), goof));
                DIZZY.remove(m.user());
                Entity user = level.m_6815_(m.user());
                if (user != null) {
                    int id = m.user();
                    goof.sound = LoopSound.play(GearSounds.GOOF, user, 0.8F, 1.0F, true, true, NikaGoof.MIDDLE, () -> {
                        Goof g = GOOFS.get(id);
                        return g == goof && g.popped < 0;
                    });
                }
            }
            case NikaWhip.GOOF_HIT -> mark(m, now, 0.0);
            case NikaWhip.GOOF_POP -> {
                Goof goof = GOOFS.remove(m.user());
                stopSound(goof);
                if (m.bx() > 0.5F) {
                    mark(m, now, 0.0);
                }
                if (m.ticks() > 0) {
                    DIZZY.put(m.user(), new double[] {now, m.ticks()});
                }
            }
            case NikaWhip.DAWN -> {
                Dawn dawn = new Dawn();
                dawn.start = now;
                dawn.wx = m.ax();
                dawn.wz = m.az();
                dawn.reach = m.size();
                dawn.seed = m.ticks();
                DAWNS.put(m.user(), dawn);
            }
            case NikaWhip.DAWN_HIT -> mark(m, now, m.size());
            case NikaWhip.DAWN_DRILL -> {
                Dawn dawn = DAWNS.get(m.user());
                if (dawn != null) {
                    dawn.drill = now;
                    dawn.dx = m.x();
                    dawn.dy = m.y();
                    dawn.dz = m.z();
                    dawn.torn = m.bx() > 0.5F;
                }
            }
            case NikaWhip.DAWN_POP -> {
                DAWNS.remove(m.user());
                mark(m, now, 0.0);
            }
            case NikaWhip.STOP -> {
                stopSound(GOOFS.remove(m.user()));
                DAWNS.remove(m.user());
                DIZZY.remove(m.user());
            }
            default -> {
            }
        }
    }

    static void mark(S2CUoFx m, double now, double size) {
        if (MARKS.size() >= MOST) {
            MARKS.remove(0);
        }
        Mark k = new Mark();
        k.kind = m.kind();
        k.x = m.x();
        k.y = m.y();
        k.z = m.z();
        k.wx = m.ax();
        k.wz = m.az();
        k.size = size;
        k.born = now;
        k.seed = m.ticks();
        MARKS.add(k);
    }

    static void stopSound(Goof goof) {
        if (goof != null) {
            goof.popped = 0;
        }
    }

    /** Whether {@code id} is inside its ball now (its body not drawn). */
    static boolean balled(int id, double now) {
        Goof goof = GOOFS.get(id);
        return goof != null && now - goof.start >= NikaGoof.SPIN;
    }

    @SubscribeEvent
    public static void hide(RenderPlayerEvent.Pre event) {
        if (!GOOFS.isEmpty() && balled(event.getEntity().m_19879_(), now() + event.getPartialTick())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void hands(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (!GOOFS.isEmpty() && minecraft.f_91074_ != null && balled(minecraft.f_91074_.m_19879_(), now() + event.getPartialTick())) {
            event.setCanceled(true);
        }
    }

    /** One's own view sways a little while dizzy (never a shake). */
    @SubscribeEvent
    public static void sway(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (DIZZY.isEmpty() || minecraft.f_91074_ == null || minecraft.f_91066_.m_92176_() != CameraType.FIRST_PERSON) {
            return;
        }
        double[] d = DIZZY.get(minecraft.f_91074_.m_19879_());
        if (d == null) {
            return;
        }
        double age = now() + event.getPartialTick() - d[0];
        double fade = Math.max(0.0, Math.min(1.0, Math.min(age / 6.0, (d[1] - age) / 12.0)));
        event.setRoll(event.getRoll() + (float) (4.0 * Math.sin(age * 0.22) * fade));
        event.setYaw(event.getYaw() + (float) (2.0 * Math.sin(age * 0.15 + 1.0) * fade));
    }

    // ------------------------------------------------------------------------------------------------------ the drawing

    static final class Writer implements FireMesh.Sink {
        VertexConsumer buffer;
        Matrix4f matrix;

        @Override
        public void quad(double[] xyz, int[] argb) {
            for (int k = 0; k < 4; k++) {
                int c = argb[k];
                this.buffer.m_252986_(this.matrix, (float) xyz[k * 3], (float) xyz[k * 3 + 1], (float) xyz[k * 3 + 2])
                    .m_85950_(((c >>> 16) & 255) / 255.0F, ((c >>> 8) & 255) / 255.0F, (c & 255) / 255.0F, ((c >>> 24) & 255) / 255.0F).m_5752_();
            }
        }
    }

    static final Writer SINK = new Writer();

    /** The limbs' skin through the game's translucent entity type, lit by the world where the ball is and shaded by its faces. */
    static final class SkinWriter implements AstralArt.Skin {
        MultiBufferSource.BufferSource buffers;
        final float[] m = new float[12];
        final float[] n = new float[9];
        RenderType type;
        VertexConsumer out;
        int light = 0xF000F0;

        void use(net.minecraft.resources.ResourceLocation skin) {
            this.type = RenderType.m_110473_(skin);
            this.out = this.buffers.m_6299_(this.type);
        }

        @Override
        public void quad(double[] xyz, float[] uv, int argb, double nx, double ny, double nz) {
            if (this.out == null) {
                return;
            }
            for (int k = 0; k < 4; k++) {
                com.hexhaki.piece.client.render.Bulk.vertex(this.out, this.m, this.n, (float) xyz[k * 3], (float) xyz[k * 3 + 1],
                    (float) xyz[k * 3 + 2], 1.0F, 1.0F, 1.0F, 1.0F, uv[k * 2], uv[k * 2 + 1],
                    net.minecraft.client.renderer.texture.OverlayTexture.f_118083_, this.light, (float) nx, (float) ny, (float) nz);
            }
        }

        void end() {
            if (this.out != null) {
                this.buffers.m_109912_(this.type);
            }
            this.out = null;
        }
    }

    static final SkinWriter SKIN = new SkinWriter();

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || GOOFS.isEmpty() && DAWNS.isEmpty() && DIZZY.isEmpty()
            && MARKS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            clear();
            return;
        }
        float partial = event.getPartialTick();
        double now = level.m_46467_() + partial;
        prune(level, now);
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        RenderType type = RenderType.m_269166_();
        SINK.matrix = pose.m_85850_().m_252922_();
        FireMesh.quality = Math.max(0.35, PieceFx.density());
        Vec3 cam = event.getCamera().m_90583_();
        boolean firstPerson = minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON;
        int own = minecraft.f_91074_ == null ? Integer.MIN_VALUE : minecraft.f_91074_.m_19879_();
        try {
            // the balls' limbs first, each skin its own batch, ended before the light's is asked for
            if (!GOOFS.isEmpty()) {
                SKIN.buffers = buffers;
                com.hexhaki.piece.client.render.Bulk.read(pose.m_85850_(), SKIN.m, SKIN.n);
                for (Map.Entry<Integer, Goof> e : GOOFS.entrySet()) {
                    double age = now - e.getValue().start - NikaGoof.SPIN;
                    if (age < 0 || !(level.m_6815_(e.getKey()) instanceof AbstractClientPlayer player)) {
                        continue;
                    }
                    Vec3 feet = player.m_20318_(partial);
                    if (feet.m_82557_(cam) > FAR * FAR) {
                        continue;
                    }
                    SKIN.use(gearSkin(player));
                    SKIN.light = LevelRenderer.m_109541_(level, BlockPos.m_274561_(feet.f_82479_, feet.f_82480_ + 1.0, feet.f_82481_));
                    boolean slim = "slim".equals(player.m_108564_());
                    if (e.getKey() == own && firstPerson) {
                        look(player, partial);
                        NikaGoof.firstPerson(null, SKIN, age, e.getValue().seed, slim, F[0], F[1], F[2], U[0], U[1], U[2], R[0], R[1], R[2]);
                    } else {
                        NikaGoof.limbs(SKIN, feet.f_82479_ - cam.f_82479_, feet.f_82480_ + NikaGoof.MIDDLE - cam.f_82480_,
                            feet.f_82481_ - cam.f_82481_, age, e.getValue().seed, slim);
                    }
                    SKIN.end();
                }
            }
            SINK.buffer = buffers.m_6299_(type);
            draw(minecraft, level, cam, now, partial, firstPerson, own);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: Goof Ball / Dawn Whip could not be drawn", failure);
            clear();
        }
        SKIN.end();
        if (SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        SINK.buffer = null;
    }

    /** The player's Gear 5 skin as the original draws the body in it (its white skin; Armament's black through HakiSkin). */
    static net.minecraft.resources.ResourceLocation gearSkin(AbstractClientPlayer player) {
        net.minecraft.resources.ResourceLocation skin = player.m_108560_();
        try {
            net.minecraft.resources.ResourceLocation gear = com.hexhaki.client.devilfruit.render.GearJacketSkin.resolve(player, skin);
            return gear != null ? gear : skin;
        } catch (RuntimeException failure) {
            return skin;
        }
    }

    static final double[] F = new double[3];
    static final double[] U = new double[3];
    static final double[] R = new double[3];

    /** One's own view's frame (forward, up, right) from the look. */
    static void look(AbstractClientPlayer player, float partial) {
        double yaw = Math.toRadians(player.f_19859_ + (player.m_146908_() - player.f_19859_) * partial);
        double pitch = Math.toRadians(player.f_19860_ + (player.m_146909_() - player.f_19860_) * partial);
        F[0] = -Math.sin(yaw) * Math.cos(pitch);
        F[1] = -Math.sin(pitch);
        F[2] = Math.cos(yaw) * Math.cos(pitch);
        U[0] = -Math.sin(yaw) * Math.sin(pitch);
        U[1] = Math.cos(pitch);
        U[2] = Math.cos(yaw) * Math.sin(pitch);
        // right = forward x up
        R[0] = F[1] * U[2] - F[2] * U[1];
        R[1] = F[2] * U[0] - F[0] * U[2];
        R[2] = F[0] * U[1] - F[1] * U[0];
    }

    static void draw(Minecraft minecraft, ClientLevel level, Vec3 cam, double now, float partial, boolean firstPerson, int own) {
        double cx = cam.f_82479_;
        double cy = cam.f_82480_;
        double cz = cam.f_82481_;
        for (Map.Entry<Integer, Goof> e : GOOFS.entrySet()) {
            double age = now - e.getValue().start - NikaGoof.SPIN;
            if (age < 0 || !(level.m_6815_(e.getKey()) instanceof AbstractClientPlayer player)) {
                continue;
            }
            if (e.getKey() == own && firstPerson) {
                look(player, partial);
                NikaGoof.firstPerson(SINK, null, age, e.getValue().seed, false, F[0], F[1], F[2], U[0], U[1], U[2], R[0], R[1], R[2]);
                continue;
            }
            Vec3 feet = player.m_20318_(partial);
            if (feet.m_82557_(cam) > FAR * FAR) {
                continue;
            }
            NikaGoof.ball(SINK, feet.f_82479_ - cx, feet.f_82480_ + NikaGoof.MIDDLE - cy, feet.f_82481_ - cz, age, e.getValue().seed);
        }
        for (Map.Entry<Integer, double[]> e : DIZZY.entrySet()) {
            if (e.getKey() == own && firstPerson || !(level.m_6815_(e.getKey()) instanceof AbstractClientPlayer player)) {
                continue;
            }
            Vec3 feet = player.m_20318_(partial);
            if (feet.m_82557_(cam) > FAR * FAR) {
                continue;
            }
            double top = player.m_20206_() + 0.1;
            NikaGoof.dizzy(SINK, feet.f_82479_ - cx, feet.f_82480_ + top - cy, feet.f_82481_ - cz, now - e.getValue()[0], e.getValue()[1],
                e.getKey());
        }
        for (Map.Entry<Integer, Dawn> e : DAWNS.entrySet()) {
            Dawn dawn = e.getValue();
            if (!(level.m_6815_(e.getKey()) instanceof AbstractClientPlayer player)) {
                continue;
            }
            Vec3 feet = player.m_20318_(partial);
            if (feet.m_82557_(cam) > FAR * FAR) {
                continue;
            }
            boolean mine = e.getKey() == own && firstPerson;
            double age = now - dawn.start;
            double x = feet.f_82479_ - cx;
            double y = feet.f_82480_ - cy;
            double z = feet.f_82481_ - cz;
            if (!mine && age <= NikaDawn.WIND + 1) {
                NikaDawn.coil(SINK, x, y, z, age, dawn.seed);
            }
            if (age >= NikaDawn.WIND) {
                NikaDawn.smear(SINK, x, y + 0.75, z, age - NikaDawn.WIND, dawn.wx, dawn.wz, dawn.reach, dawn.seed);
            }
            if (age > NikaDawn.IMPACT && age < NikaDawn.DRILL) {
                double drill = Math.max(0.0, Math.min(1.0, (age - NikaDawn.OVERSPIN) / 4.0));
                if (!mine) {
                    NikaDawn.whirl(SINK, x, y, z, age - NikaDawn.IMPACT, drill, dawn.seed);
                }
            }
            if (mine) {
                look(player, partial);
                NikaDawn.firstPerson(SINK, age - NikaDawn.WIND, dawn.seed, F[0], F[1], F[2], U[0], U[1], U[2], R[0], R[1], R[2]);
            }
            if (dawn.drill >= 0) {
                NikaDawn.drill(SINK, dawn.dx - cx, dawn.dy - cy, dawn.dz - cz, now - dawn.drill, dawn.torn, dawn.seed);
            }
        }
        for (Mark k : MARKS) {
            double x = k.x - cx;
            double y = k.y - cy;
            double z = k.z - cz;
            if (x * x + y * y + z * z > FAR * FAR) {
                continue;
            }
            double age = now - k.born;
            switch (k.kind) {
                case NikaWhip.GOOF_HIT -> NikaGoof.burst(SINK, x, y, z, age, 0.32, k.seed);
                case NikaWhip.GOOF_POP -> NikaGoof.pop(SINK, x, y, z, age, k.seed);
                case NikaWhip.DAWN_HIT -> NikaDawn.crack(SINK, x, y, z, k.wx, k.wz, age, k.size, k.seed);
                case NikaWhip.DAWN_POP -> NikaDawn.pop(SINK, x, y, z, age, k.seed);
                default -> {
                }
            }
        }
    }

    static void prune(ClientLevel level, double now) {
        for (Iterator<Mark> it = MARKS.iterator(); it.hasNext(); ) {
            Mark k = it.next();
            double life = k.kind == NikaWhip.DAWN_HIT ? NikaDawn.CRACK_LIFE : k.kind == NikaWhip.DAWN_POP ? NikaDawn.POP_LIFE
                : k.kind == NikaWhip.GOOF_POP ? NikaGoof.POP_LIFE : 6.0;
            if (now - k.born > life || now < k.born - 40) {
                it.remove();
            }
        }
        // a move never heard ending (out of range, a lost message) is let go well after its longest
        GOOFS.values().removeIf(g -> now - g.start > NikaGoof.SPIN + NikaGoof.MOST + 40 || now < g.start - 40);
        DAWNS.values().removeIf(d -> now - d.start > NikaDawn.POP + 20 || now < d.start - 40);
        DIZZY.values().removeIf(d -> now - d[0] > d[1] || now < d[0] - 40);
    }

    static void clear() {
        for (Goof goof : GOOFS.values()) {
            stopSound(goof);
        }
        GOOFS.clear();
        DAWNS.clear();
        DIZZY.clear();
        MARKS.clear();
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
