package com.hexhaki.piece.client.fx;

import com.hexhaki.client.RemoteHakiStates;
import com.hexhaki.piece.client.npc.StylePoser;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.style.StyleMove;
import com.hexhaki.piece.style.StyleShow;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 2.8.3: the fighting styles' blows in 3D in the game (the owner: "better VFX on the M1s (m1 vfx on water kungfu suck ass), add
 * variatations with the haki on/off"): style/StyleShow's smears, pushes, water and Haki on every user of a move (from the server's
 * S2CStyleFx: stage 0 its start; for a user whose start never came, as an NPC's, its first blow thrown), and its landed blows' bursts
 * (stage 1), each frame after the particles, through the game's own position-colour quads, eye-centred. Armament Haki (or Ryou) on the
 * user, as every client sees it (the original's RemoteHakiStates), turns them black and red.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class StyleVfx {
    private StyleVfx() {
    }

    /** A move under way on someone. */
    static final class Active {
        final int entity;
        final StyleMove move;
        long start;

        Active(int entity, StyleMove move, long start) {
            this.entity = entity;
            this.move = move;
            this.start = start;
        }
    }

    /** 2.8.4: a line move's shot in flight (a spear of water, a drill, a cannonball, a punch's shock). */
    record Shot(int entity, StyleMove move, Vec3 from, Vec3 to, long start, int seed) {
    }

    private static final List<Shot> SHOTS = new ArrayList<>();

    /** A line move's shot thrown from {@code from} to {@code to} (stage 2): drawn here if it has a 3D shot (StyleShow.shotKind). */
    public static boolean shot(int entity, StyleMove move, Vec3 from, Vec3 to) {
        if (StyleShow.shotKind(move) == null) {
            return false;
        }
        if (SHOTS.size() >= MOST) {
            SHOTS.remove(0);
        }
        SHOTS.add(new Shot(entity, move, from, to, now(), entity * 7 + (int) now()));
        return true;
    }

    /** A blow that landed. */
    record Landed(int entity, StyleMove move, Vec3 at, double[] dir, double size, long start, int seed) {
    }

    private static final List<Active> ACTIVE = new ArrayList<>();
    private static final List<Landed> LANDED = new ArrayList<>();
    static final StyleShow.Poser POSER = StylePoser::sample;
    /** Never more than this many at once (a crowd of NPCs). */
    static final int MOST = 24;
    /** Drawn no further than this from the eye (blocks). */
    static final double FAR = 64.0;

    static long now() {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? 0L : level.m_46467_();
    }

    /** {@code entity} began {@code move} (stage 0). */
    public static void started(int entity, StyleMove move) {
        if (!StyleShow.shown(move)) {
            return;
        }
        ACTIVE.removeIf(a -> a.entity == entity);
        if (ACTIVE.size() >= MOST) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Active(entity, move, now()));
    }

    /** A blow of {@code move} thrown by {@code entity} (stage 3): its start, if it never came (an NPC), taken from its first blow. */
    public static void thrown(int entity, StyleMove move) {
        if (!StyleShow.shown(move) || move.hits().length == 0) {
            return;
        }
        long t = now();
        for (Active a : ACTIVE) {
            if (a.entity == entity && a.move == move && t - a.start <= move.duration()) {
                return;
            }
        }
        started(entity, move);
        ACTIVE.get(ACTIVE.size() - 1).start = t - move.hits()[0].tick();
    }

    /** A blow of {@code move} by {@code entity} landed at {@code at}, struck along {@code facing} (stage 1). */
    public static void landed(int entity, StyleMove move, Vec3 at, Vec3 facing) {
        if (!StyleShow.shown(move)) {
            return;
        }
        if (LANDED.size() >= MOST) {
            LANDED.remove(0);
        }
        double size = StyleShow.size(StyleShow.weight(move));
        LANDED.add(new Landed(entity, move, at, new double[] {facing.f_82479_, facing.f_82480_, facing.f_82481_}, size, now(),
            (int) (at.f_82479_ * 31 + at.f_82481_ * 17 + now())));
    }

    static boolean haki(int entity) {
        RemoteHakiStates.State state = RemoteHakiStates.get(entity);
        return state != null && (state.armament() || state.ryuo());
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty() && LANDED.isEmpty() && SHOTS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        RenderType type = RenderType.m_269166_();
        SINK.buffer = buffers.m_6299_(type);
        SINK.matrix = pose.m_85850_().m_252922_();
        FireMesh.quality = Math.max(0.35, PieceFx.density());
        try {
            COLLECT.count = 0;
            draw(level, event.getCamera().m_90583_(), event.getPartialTick());
            COLLECT.flush(SINK);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: a style move's effect could not be drawn", failure);
            ACTIVE.clear();
            LANDED.clear();
            SHOTS.clear();
        }
        buffers.m_109912_(type);
        SINK.buffer = null;
    }

    static void draw(ClientLevel level, Vec3 cam, float partial) {
        long t = now();
        for (Iterator<Active> it = ACTIVE.iterator(); it.hasNext(); ) {
            Active a = it.next();
            double ticks = t - a.start + partial;
            Entity user = level.m_6815_(a.entity);
            if (user == null || user.m_213877_() || ticks > a.move.duration() + 20) {
                it.remove();
                continue;
            }
            Vec3 feet = user.m_20318_(partial);
            if (feet.m_82557_(cam) > FAR * FAR) {
                continue;
            }
            double yaw = user.m_146908_();
            if (user instanceof LivingEntity living) {
                yaw = living.f_20884_ + wrap(living.f_20883_ - living.f_20884_) * partial;
            }
            StyleShow.Place place = new StyleShow.Place(feet.f_82479_ - cam.f_82479_, feet.f_82480_ - cam.f_82480_, feet.f_82481_ - cam.f_82481_,
                Math.toRadians(yaw));
            boolean own = user == Minecraft.m_91087_().f_91074_ && Minecraft.m_91087_().f_91066_.m_92176_() == net.minecraft.client.CameraType.FIRST_PERSON;
            StyleShow.attached(COLLECT, a.move, haki(a.entity), (float) ticks, POSER, place, a.entity, own);
        }
        for (Iterator<Landed> it = LANDED.iterator(); it.hasNext(); ) {
            Landed l = it.next();
            double age = t - l.start() + partial;
            if (age > StyleShow.landedLife(l.move())) {
                it.remove();
                continue;
            }
            if (l.at().m_82557_(cam) > FAR * FAR) {
                continue;
            }
            double[] at = {l.at().f_82479_ - cam.f_82479_, l.at().f_82480_ - cam.f_82480_, l.at().f_82481_ - cam.f_82481_};
            StyleShow.landed(COLLECT, l.move(), haki(l.entity()), age, at, l.dir(), l.size(), l.seed());
        }
        shots(cam, t, partial);
    }

    static void shots(Vec3 cam, long t, float partial) {
        for (Iterator<Shot> it = SHOTS.iterator(); it.hasNext(); ) {
            Shot s = it.next();
            double age = t - s.start() + partial;
            if (age > StyleShow.shotLife(s.move(), s.from().m_82554_(s.to()))) {
                it.remove();
                continue;
            }
            if (s.from().m_82557_(cam) > FAR * FAR && s.to().m_82557_(cam) > FAR * FAR) {
                continue;
            }
            double[] from = {s.from().f_82479_ - cam.f_82479_, s.from().f_82480_ - cam.f_82480_, s.from().f_82481_ - cam.f_82481_};
            double[] to = {s.to().f_82479_ - cam.f_82479_, s.to().f_82480_ - cam.f_82480_, s.to().f_82481_ - cam.f_82481_};
            StyleShow.shot(COLLECT, s.move(), haki(s.entity()), age, from, to, s.seed());
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

    /** The quads straight into the game's buffer, through the view. */
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

    /**
     * Every quad of the frame, then laid far to near (alpha blended and depth written: a near one first would hide a far one behind it),
     * those within 5 cm of each other in the order they were made (a look's bands, laid outer to inner).
     */
    static final class Collector implements FireMesh.Sink {
        double[] xyz = new double[12 * 1024];
        int[] argb = new int[4 * 1024];
        int count;

        @Override
        public void quad(double[] q, int[] c) {
            if ((this.count + 1) * 12 > this.xyz.length) {
                this.xyz = java.util.Arrays.copyOf(this.xyz, this.xyz.length * 2);
                this.argb = java.util.Arrays.copyOf(this.argb, this.argb.length * 2);
            }
            System.arraycopy(q, 0, this.xyz, this.count * 12, 12);
            System.arraycopy(c, 0, this.argb, this.count * 4, 4);
            this.count++;
        }

        void flush(FireMesh.Sink out) {
            long[] keys = new long[this.count];
            double[] q = new double[12];
            int[] c = new int[4];
            for (int i = 0; i < this.count; i++) {
                int o = i * 12;
                double mx = (this.xyz[o] + this.xyz[o + 3] + this.xyz[o + 6] + this.xyz[o + 9]) * 0.25;
                double my = (this.xyz[o + 1] + this.xyz[o + 4] + this.xyz[o + 7] + this.xyz[o + 10]) * 0.25;
                double mz = (this.xyz[o + 2] + this.xyz[o + 5] + this.xyz[o + 8] + this.xyz[o + 11]) * 0.25;
                long bucket = Math.min(1_000_000L, (long) (Math.sqrt(mx * mx + my * my + mz * mz) * 20.0));
                keys[i] = (1_000_000L - bucket) << 22 | i;
            }
            java.util.Arrays.sort(keys);
            for (long key : keys) {
                int i = (int) (key & ((1 << 22) - 1));
                System.arraycopy(this.xyz, i * 12, q, 0, 12);
                System.arraycopy(this.argb, i * 4, c, 0, 4);
                out.quad(q, c);
            }
            this.count = 0;
        }
    }

    static final Collector COLLECT = new Collector();

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ACTIVE.clear();
        LANDED.clear();
        SHOTS.clear();
    }
}
