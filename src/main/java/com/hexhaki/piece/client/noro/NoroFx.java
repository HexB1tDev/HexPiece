package com.hexhaki.piece.client.noro;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.noro.NoroArt;
import com.hexhaki.piece.noro.NoroMirror;
import com.hexhaki.piece.noro.NoroSounds;
import com.hexhaki.piece.server.NoroMoves;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.3.4: the Noro Noro no Mi's photons as every client sees them (server/NoroMoves sends S2CUoFx kinds 180 to 199; client/dragon/UoFx
 * hands them here), drawn once a frame after the particles through the game's own debugQuads type (alpha blended, depth tested, no
 * culling: the owner's device), eye-centred (noro/NoroArt). The beam each client traces itself from the user's own look, off the same
 * mirrors (noro/NoroMirror) and up to the same blocks; a slowed shot's crawl each client keeps itself (no stream of positions); the
 * beam's hum held through client/LoopSound (faded out the moment it ends); the attack button given back to the game's punch.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class NoroFx {
    private static long clock;
    private static boolean kitAdded;

    /** One short effect in the world (a burst's end, a held blow, a release, a reflection, the snap). */
    record Spot(int kind, int id, Vec3 at, Vec3 dir, double size, double bx, long born) {
    }

    /** A burst flying: from, its step a tick, its flight, since when. */
    record Shot(Vec3 from, Vec3 step, int ticks, long born) {
    }

    /** A slowed thing: until when, since when, a shot's crawl (null for a body). */
    record Slowed(long until, long born, Vec3 crawl) {
    }

    /** A mirror: its glass, since when, its life. */
    record Glass(NoroMirror glass, long born, int life) {
    }

    /** A dome: its foot, radius, since when, life. */
    record Dome(Vec3 at, double radius, long born, int life) {
    }

    static final int MOST_SPOTS = 64;
    static final int MOST_SLOWED = 96;
    private static final List<Spot> SPOTS = new ArrayList<>();
    static final Map<Integer, Shot> SHOTS = new HashMap<>();
    static final Map<Integer, Long> BEAMS = new HashMap<>();
    static final Map<Integer, Slowed> SLOWED = new HashMap<>();
    static final Map<Integer, Glass> MIRRORS = new HashMap<>();
    static final Map<Integer, Dome> DOMES = new HashMap<>();
    static final double FAR = 160.0;

    private NoroFx() {
    }

    static Entity entity(int id) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? null : level.m_6815_(id);
    }

    static void add(Spot spot) {
        if (SPOTS.size() >= MOST_SPOTS) {
            SPOTS.remove(0);
        }
        SPOTS.add(spot);
    }

    public static void accept(S2CUoFx m) {
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        Vec3 a = new Vec3(m.ax(), m.ay(), m.az());
        switch (m.kind()) {
            case NoroMoves.SHOT -> SHOTS.put(m.id(), new Shot(at, a, m.ticks(), clock));
            case NoroMoves.POP -> {
                SHOTS.remove(m.id());
                add(new Spot(m.kind(), m.id(), at, a, m.size(), m.bx(), clock));
            }
            case NoroMoves.BEAM_ON -> {
                Long since = clock;
                BEAMS.put(m.user(), since);
                com.hexhaki.piece.client.LoopSound.play(NoroSounds.BEAM, entity(m.user()), 1.0F, 1.0F, true, true, 1.0,
                    () -> since.equals(BEAMS.get(m.user())));
            }
            case NoroMoves.BEAM_OFF -> BEAMS.remove(m.user());
            case NoroMoves.SLOW -> {
                if (SLOWED.size() >= MOST_SLOWED && !SLOWED.containsKey(m.id())) {
                    return;
                }
                Slowed was = SLOWED.get(m.id());
                SLOWED.put(m.id(), new Slowed(clock + m.ticks(), was != null ? was.born : clock, m.bx() > 0.5 ? a : null));
            }
            case NoroMoves.UNSLOW -> SLOWED.remove(m.id());
            case NoroMoves.MIRROR -> MIRRORS.put(m.id(), new Glass(new NoroMirror(m.x(), m.y(), m.z(), m.ax(), m.ay(), m.az()), clock,
                m.ticks()));
            case NoroMoves.MIRROR_OFF -> MIRRORS.remove(m.id());
            case NoroMoves.FIELD -> DOMES.put(m.id(), new Dome(at, m.size(), clock, m.ticks()));
            case NoroMoves.STORE, NoroMoves.RELEASE, NoroMoves.REFLECT, NoroMoves.SNAP -> add(new Spot(m.kind(), m.id(), at, a, m.size(), m.bx(),
                clock));
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ the clock

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clock++;
        if (!kitAdded) {
            kitAdded = true;
            ClientKits.KITS.put(Fruit.NORO, new NoroClient()); // the original has no client kit for it
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.m_91104_()) {
            return;
        }
        SPOTS.removeIf(s -> clock - s.born > life(s) + 1);
        SHOTS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().ticks + 2);
        BEAMS.entrySet().removeIf(e -> clock - e.getValue() > NoroMoves.BEAM_MOST + 20 || entity(e.getKey()) == null);
        MIRRORS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().life + 1);
        DOMES.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().life + 1);
        SLOWED.entrySet().removeIf(e -> clock > e.getValue().until + 20);
        // a slowed shot crawls on every client as on the server (its gravity is off, its speed kept down to the crawl)
        for (Map.Entry<Integer, Slowed> e : SLOWED.entrySet()) {
            Vec3 crawl = e.getValue().crawl;
            if (crawl != null) {
                Entity shot = level.m_6815_(e.getKey());
                if (shot != null) {
                    shot.m_20256_(crawl);
                }
            }
        }
    }

    static double life(Spot s) {
        return switch (s.kind) {
            case NoroMoves.POP, NoroMoves.REFLECT -> NoroArt.POP_LIFE;
            case NoroMoves.STORE -> NoroArt.STORE_LIFE;
            case NoroMoves.RELEASE -> NoroArt.RELEASE_LIFE;
            case NoroMoves.SNAP -> NoroArt.SNAP_LIFE;
            default -> 20;
        };
    }

    /** The attack button the game's own punch with the Noro fruit (the original's fruit keys take it). */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
    public static void punch(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || !event.isCanceled()) {
            return;
        }
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        if (self == null || BEAMS.containsKey(self.m_19879_()) || !com.hexhaki.fruit.client.FruitClient.active()
            || com.hexhaki.fruit.client.FruitClient.fruit(self.m_19879_()) != Fruit.NORO || !self.m_21205_().m_41619_()) {
            return;
        }
        event.setCanceled(false);
        event.setSwingHand(true);
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

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || SPOTS.isEmpty() && SHOTS.isEmpty() && BEAMS.isEmpty()
            && SLOWED.isEmpty() && MIRRORS.isEmpty() && DOMES.isEmpty()) {
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
        SINK.matrix = pose.m_85850_().m_252922_();
        FireMesh.quality = Math.max(0.35, PieceFx.density());
        Vec3 cam = event.getCamera().m_90583_();
        float partial = event.getPartialTick();
        try {
            SINK.buffer = buffers.m_6299_(type);
            draw(level, cam, partial);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: a Noro fruit move could not be drawn", failure);
            clear();
        }
        if (SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        SINK.buffer = null;
    }

    static boolean far(Vec3 at, Vec3 cam) {
        return at.m_82557_(cam) > FAR * FAR;
    }

    private static final double[] WAY = new double[3];

    static void draw(ClientLevel level, Vec3 cam, float partial) {
        double cx = cam.f_82479_;
        double cy = cam.f_82480_;
        double cz = cam.f_82481_;
        double now = clock + partial;
        for (Dome d : DOMES.values()) {
            if (!far(d.at, cam)) {
                NoroArt.dome(SINK, d.at.f_82479_ - cx, d.at.f_82480_ - cy, d.at.f_82481_ - cz, d.radius, now - d.born, d.life);
            }
        }
        for (Glass g : MIRRORS.values()) {
            NoroMirror m = g.glass;
            NoroArt.mirror(SINK, m.cx - cx, m.cy - cy, m.cz - cz, m, now - g.born, g.life);
        }
        for (Map.Entry<Integer, Slowed> e : SLOWED.entrySet()) {
            Entity body = level.m_6815_(e.getKey());
            Slowed s = e.getValue();
            if (body == null) {
                continue;
            }
            double x = body.f_19790_ + (body.m_20185_() - body.f_19790_) * partial - cx;
            double y = body.f_19791_ + (body.m_20186_() - body.f_19791_) * partial - cy;
            double z = body.f_19792_ + (body.m_20189_() - body.f_19792_) * partial - cz;
            if (x * x + y * y + z * z > FAR * FAR) {
                continue;
            }
            double left = Math.max(0.0, Math.min(1.0, (s.until - now) / (double) com.hexhaki.piece.server.NoroSlow.LENGTH));
            if (s.crawl != null) {
                double l = Math.max(1.0E-6, s.crawl.m_82553_());
                NoroArt.slowShot(SINK, x, y + body.m_20206_() * 0.5, z, s.crawl.f_82479_ / l, s.crawl.f_82480_ / l, s.crawl.f_82481_ / l, now - s.born);
            } else if (body instanceof LivingEntity) {
                NoroArt.aura(SINK, x, y + body.m_20206_() * 0.5, z, body.m_20205_(), body.m_20206_(), now - s.born, left);
            }
        }
        for (Shot s : SHOTS.values()) {
            double age = now - s.born;
            Vec3 p = s.from.m_82549_(s.step.m_82490_(age));
            if (far(p, cam)) {
                continue;
            }
            double l = Math.max(1.0E-6, s.step.m_82553_());
            NoroArt.shot(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, s.step.f_82479_ / l, s.step.f_82480_ / l, s.step.f_82481_ / l, age);
        }
        for (Map.Entry<Integer, Long> e : BEAMS.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            if (user != null) {
                beam(level, user, cam, partial, now - e.getValue());
            }
        }
        for (Spot s : SPOTS) {
            if (far(s.at, cam)) {
                continue;
            }
            double age = now - s.born;
            double x = s.at.f_82479_ - cx;
            double y = s.at.f_82480_ - cy;
            double z = s.at.f_82481_ - cz;
            switch (s.kind) {
                case NoroMoves.POP -> NoroArt.pop(SINK, x, y, z, age, s.bx > 0.5);
                case NoroMoves.REFLECT -> NoroArt.reflect(SINK, x, y, z, age);
                case NoroMoves.STORE -> NoroArt.store(SINK, x, y, z, s.dir.f_82479_, s.dir.f_82481_, age);
                case NoroMoves.RELEASE -> NoroArt.release(SINK, x, y, z, s.size, age);
                case NoroMoves.SNAP -> NoroArt.snap(SINK, x, y, z, age);
                default -> {
                }
            }
        }
    }

    /** One user's beam this frame: from the hands along the look, up to the blocks, off the mirrors (as the server traces it). */
    static void beam(ClientLevel level, Entity user, Vec3 cam, float partial, double age) {
        Vec3 look = user.m_20252_(partial);
        Vec3 from = user.m_20299_(partial).m_82549_(look.m_82490_(0.7)).m_82520_(0.0, -0.35, 0.0);
        WAY[0] = look.f_82479_;
        WAY[1] = look.f_82480_;
        WAY[2] = look.f_82481_;
        double left = NoroMoves.BEAM_RANGE;
        for (int bounce = 0; bounce < 3 && left > 0.1; bounce++) {
            Vec3 way = new Vec3(WAY[0], WAY[1], WAY[2]);
            Vec3 to = from.m_82549_(way.m_82490_(left));
            BlockHitResult block = level.m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));
            double at = block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_().m_82554_(from) : left;
            NoroMirror hit = null;
            for (Glass g : MIRRORS.values()) {
                double t = g.glass.hit(from.f_82479_, from.f_82480_, from.f_82481_, WAY[0], WAY[1], WAY[2], at);
                if (t > 0) {
                    at = t;
                    hit = g.glass;
                }
            }
            Vec3 end = from.m_82549_(way.m_82490_(at));
            NoroArt.beam(SINK, from.f_82479_ - cam.f_82479_, from.f_82480_ - cam.f_82480_, from.f_82481_ - cam.f_82481_, end.f_82479_ - cam.f_82479_,
                end.f_82480_ - cam.f_82480_, end.f_82481_ - cam.f_82481_, age);
            if (hit == null) {
                return;
            }
            hit.reflect(WAY);
            left -= at;
            from = end;
        }
    }

    static void clear() {
        SPOTS.clear();
        SHOTS.clear();
        BEAMS.clear();
        SLOWED.clear();
        MIRRORS.clear();
        DOMES.clear();
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
