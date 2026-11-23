package com.hexhaki.piece.client.supa;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.client.sword.BladeCoatClient;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.SupaMoves;
import com.hexhaki.piece.supa.SupaArt;
import com.hexhaki.piece.supa.SupaSounds;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.3.5: the Supa Supa no Mi as every client sees it (server/SupaMoves sends S2CUoFx kinds 200 to 219; client/dragon/UoFx hands them
 * here): which blades each body shows (client/supa/SupaClient draws them from the owner's katana steel), the cuts in the air (flying
 * slashes, the X, claw marks, sparks, glances, wakes, Spar Break's spin: supa/SupaArt, drawn once a frame after the particles through
 * the game's own debugQuads type: alpha blended, depth tested, no culling), the skate marks Atomic Spurt leaves (each client its own
 * from the skater's feet), the skate's and the spin's loops (client/LoopSound: faded out the moment they end) and one's own dash.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class SupaFx {
    /** The blade masks (server/SupaMoves has the same). */
    static final int ARMS = 1;
    static final int CLAWS = 2;
    static final int DRILLS = 4;
    static final int SKATES = 8;
    static final int STEEL = 16;

    private static long clock;
    private static boolean kitAdded;

    /** A body's blades: which, until when, since when they came out. */
    record Parts(int mask, long until, long since) {
    }

    /** A cut flying: from, its step a tick, its flight, since when, an X or a crescent, its scale, its user's coat. */
    record Slash(Vec3 from, Vec3 step, int ticks, long born, boolean cross, double scale, double coat) {
    }

    /** One short effect in the world. */
    record Spot(int kind, Vec3 at, Vec3 a, double bx, double size, long born, int seed, double coat) {
    }

    /** Spar Break round a body: since when, how long, its reach. */
    record Spin(long born, int ticks, double radius) {
    }

    /** A skater's last steps: the feet's points (x, z, y each) one a tick, a ring. */
    static final class Marks {
        static final int MOST = 40;
        final double[] left = new double[MOST * 3];
        final double[] right = new double[MOST * 3];
        final long[] when = new long[MOST];
        int head;
        int count;
    }

    static final int MOST_SPOTS = 64;
    static final int MOST_SLASHES = 32;
    static final Map<Integer, Parts> PARTS = new HashMap<>();
    static final Map<Integer, Slash> SLASHES = new HashMap<>();
    static final Map<Integer, Spin> SPINS = new HashMap<>();
    static final Map<Integer, Marks> MARKS = new HashMap<>();
    private static final List<Spot> SPOTS = new ArrayList<>();
    static final double FAR = 160.0;
    /** How long a skate mark shows (ticks). */
    static final double MARK_LIFE = 30.0;

    private SupaFx() {
    }

    static Entity entity(int id) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? null : level.m_6815_(id);
    }

    static double coatOf(int id) {
        return entity(id) instanceof AbstractClientPlayer p ? BladeCoatClient.progress(p) : 0.0;
    }

    /** {@code id}'s blades now (0: none). */
    static int mask(int id) {
        Parts p = PARTS.get(id);
        return p == null || clock >= p.until ? 0 : p.mask;
    }

    /** Whether {@code id} is in Spar Break's spin now. */
    static boolean spinning(int id) {
        Spin s = SPINS.get(id);
        return s != null && clock - s.born < s.ticks;
    }

    /** Ticks into {@code id}'s spin. */
    static double spinAge(int id, float partial) {
        Spin s = SPINS.get(id);
        return s == null ? 0.0 : clock - s.born + partial;
    }

    /** Ticks since {@code id}'s blades came out. */
    static double age(int id, float partial) {
        Parts p = PARTS.get(id);
        return p == null ? 0.0 : clock - p.since + partial;
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
            case SupaMoves.PARTS -> parts(m.user(), (int) m.bx(), m.ticks());
            case SupaMoves.SLASH -> {
                if (SLASHES.size() < MOST_SLASHES) {
                    SLASHES.put(m.id(), new Slash(at, a, m.ticks(), clock, m.bx() > 0.5, m.size(), coatOf(m.user())));
                }
            }
            case SupaMoves.SLASH_END -> {
                Slash s = SLASHES.remove(m.id());
                add(new Spot(m.kind(), at, a, m.bx(), m.size(), clock, m.id(), s != null ? s.coat : 0.0));
            }
            case SupaMoves.SPIN -> {
                if (m.ticks() <= 0) {
                    SPINS.remove(m.user());
                } else {
                    Long born = clock;
                    SPINS.put(m.user(), new Spin(born, m.ticks(), m.size()));
                    com.hexhaki.piece.client.LoopSound.play(SupaSounds.SPIN, entity(m.user()), 1.0F, 1.0F, true, true, 1.0,
                        () -> SPINS.containsKey(m.user()) && SPINS.get(m.user()).born == born && clock - born < m.ticks());
                }
            }
            case SupaMoves.DASH -> {
                if (self(m.user())) {
                    move = Move.dash(at, m.id(), m.ticks(), (int) m.bx());
                }
            }
            case SupaMoves.CLAW, SupaMoves.SPARK, SupaMoves.GLANCE, SupaMoves.TRAIL, SupaMoves.CROSS -> add(new Spot(m.kind(), at, a, m.bx(),
                m.size(), clock, m.id(), coatOf(m.user())));
            default -> {
            }
        }
    }

    static void parts(int user, int mask, int ticks) {
        if (mask == 0 || ticks <= 0) {
            PARTS.remove(user);
            return;
        }
        Parts was = PARTS.get(user);
        boolean fresh = was == null || clock >= was.until || (was.mask & mask) != mask;
        PARTS.put(user, new Parts(mask, clock + ticks, fresh ? clock : was.since));
        if ((mask & SKATES) != 0 && (was == null || clock >= was.until || (was.mask & SKATES) == 0)) {
            com.hexhaki.piece.client.LoopSound.play(SupaSounds.SKATE, entity(user), 0.9F, 1.0F, true, true, 1.0,
                () -> (mask(user) & SKATES) != 0);
        }
    }

    static boolean self(int id) {
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        return self != null && self.m_19879_() == id;
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
            ClientKits.KITS.put(Fruit.SUPA, new SupaClient()); // the original has no client kit for it
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.m_91104_()) {
            return;
        }
        if (move != null) {
            move(minecraft.f_91074_, level);
        }
        SPOTS.removeIf(s -> clock - s.born > life(s) + 1);
        SLASHES.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().ticks + 2);
        SPINS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().ticks + 1);
        PARTS.entrySet().removeIf(e -> clock >= e.getValue().until + 40 || entity(e.getKey()) == null);
        // the skaters' feet, one point a tick; marks of those who stopped fade and go
        for (Map.Entry<Integer, Parts> e : PARTS.entrySet()) {
            if ((mask(e.getKey()) & SKATES) != 0 && entity(e.getKey()) instanceof AbstractClientPlayer p && p.m_20096_()) {
                Marks marks = MARKS.computeIfAbsent(e.getKey(), k -> new Marks());
                double yaw = Math.toRadians(p.f_20883_);
                double sx = Math.cos(yaw) * 0.15;
                double sz = Math.sin(yaw) * 0.15;
                int i = marks.head;
                marks.left[i * 3] = p.m_20185_() + sx;
                marks.left[i * 3 + 1] = p.m_20186_() + 0.02;
                marks.left[i * 3 + 2] = p.m_20189_() + sz;
                marks.right[i * 3] = p.m_20185_() - sx;
                marks.right[i * 3 + 1] = p.m_20186_() + 0.02;
                marks.right[i * 3 + 2] = p.m_20189_() - sz;
                marks.when[i] = clock;
                marks.head = (i + 1) % Marks.MOST;
                marks.count = Math.min(Marks.MOST, marks.count + 1);
            }
        }
        MARKS.entrySet().removeIf(e -> e.getValue().count == 0 || clock - e.getValue().when[(e.getValue().head + Marks.MOST - 1) % Marks.MOST]
            > MARK_LIFE);
    }

    static double life(Spot s) {
        return switch (s.kind) {
            case SupaMoves.CLAW -> SupaArt.CLAW_LIFE;
            case SupaMoves.SPARK, SupaMoves.SLASH_END -> SupaArt.SPARK_LIFE;
            case SupaMoves.GLANCE -> SupaArt.GLANCE_LIFE;
            case SupaMoves.TRAIL -> SupaArt.TRAIL_LIFE;
            case SupaMoves.CROSS -> SupaArt.CROSS_LIFE;
            default -> 20;
        };
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
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || SPOTS.isEmpty() && SLASHES.isEmpty() && SPINS.isEmpty()
            && MARKS.isEmpty()) {
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
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: a Supa fruit move could not be drawn", failure);
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

    static void draw(ClientLevel level, Vec3 cam, float partial) {
        double cx = cam.f_82479_;
        double cy = cam.f_82480_;
        double cz = cam.f_82481_;
        double now = clock + partial;
        for (Map.Entry<Integer, Marks> e : MARKS.entrySet()) {
            Marks m = e.getValue();
            for (int k = 1; k < m.count; k++) {
                int i = (m.head - k + Marks.MOST) % Marks.MOST;
                int j = (m.head - k - 1 + Marks.MOST) % Marks.MOST;
                double age = now - m.when[i];
                if (age > MARK_LIFE || m.when[i] - m.when[j] > 2) {
                    continue;
                }
                double alpha = 0.8 * (1.0 - age / MARK_LIFE);
                SupaArt.mark(SINK, m.left[j * 3] - cx, m.left[j * 3 + 1] - cy, m.left[j * 3 + 2] - cz, m.left[i * 3] - cx, m.left[i * 3 + 1] - cy,
                    m.left[i * 3 + 2] - cz, alpha);
                SupaArt.mark(SINK, m.right[j * 3] - cx, m.right[j * 3 + 1] - cy, m.right[j * 3 + 2] - cz, m.right[i * 3] - cx,
                    m.right[i * 3 + 1] - cy, m.right[i * 3 + 2] - cz, alpha);
            }
        }
        for (Map.Entry<Integer, Spin> e : SPINS.entrySet()) {
            Entity body = level.m_6815_(e.getKey());
            if (body == null) {
                continue;
            }
            double x = body.f_19790_ + (body.m_20185_() - body.f_19790_) * partial - cx;
            double y = body.f_19791_ + (body.m_20186_() - body.f_19791_) * partial - cy + body.m_20206_() * 0.55;
            double z = body.f_19792_ + (body.m_20189_() - body.f_19792_) * partial - cz;
            if (x * x + y * y + z * z < FAR * FAR) {
                SupaArt.spin(SINK, x, y, z, e.getValue().radius, now - e.getValue().born, coatOf(e.getKey()));
            }
        }
        for (Slash s : SLASHES.values()) {
            double age = now - s.born;
            Vec3 p = s.from.m_82549_(s.step.m_82490_(age));
            if (far(p, cam)) {
                continue;
            }
            double l = Math.max(1.0E-6, s.step.m_82553_());
            double ux = s.step.f_82479_ / l;
            double uy = s.step.f_82480_ / l;
            double uz = s.step.f_82481_ / l;
            if (s.cross) {
                SupaArt.cross(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, ux, uy, uz, s.scale, age, s.coat);
            } else {
                SupaArt.slash(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, ux, uy, uz, 0.0, s.scale, age, s.coat);
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
                case SupaMoves.CLAW -> SupaArt.clawMarks(SINK, x, y, z, s.a.f_82479_, s.a.f_82481_, (int) s.bx, s.size, age, s.coat);
                case SupaMoves.SPARK -> SupaArt.sparks(SINK, x, y, z, s.a.f_82479_, s.a.f_82480_, s.a.f_82481_, s.size, age, s.seed);
                case SupaMoves.SLASH_END -> SupaArt.sparks(SINK, x, y, z, -s.a.f_82479_, 0.2, -s.a.f_82481_, 0.5 * s.size, age, s.seed);
                case SupaMoves.GLANCE -> SupaArt.glance(SINK, x, y, z, s.a.f_82479_, s.a.f_82481_, age, s.seed);
                case SupaMoves.TRAIL -> {
                    if (s.bx > 0.5) {
                        SupaArt.daisyTrail(SINK, x, y, z, s.a.f_82479_ - cx, s.a.f_82480_ - cy, s.a.f_82481_ - cz, age, s.coat);
                    } else {
                        SupaArt.drillTrail(SINK, x, y, z, s.a.f_82479_ - cx, s.a.f_82480_ - cy, s.a.f_82481_ - cz, age, s.coat);
                    }
                }
                case SupaMoves.CROSS -> {
                    double l = Math.max(1.0E-6, s.a.m_82553_());
                    SupaArt.cross(SINK, x, y, z, s.a.f_82479_ / l, s.a.f_82480_ / l, s.a.f_82481_ / l, s.size, age, s.coat);
                }
                default -> {
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ one's own dash

    /** Spiral Hollow's and Sparkling Daisy's dash: one's own body carried to a place over so many ticks, then held. */
    static final class Move {
        Vec3 to;
        int target;
        Vec3 targetFrom;
        int left;
        int hold;
        boolean holding;

        static Move dash(Vec3 to, int target, int ticks, int hold) {
            Move m = new Move();
            m.to = to;
            m.target = target;
            Entity e = target >= 0 ? entity(target) : null;
            m.targetFrom = e != null ? e.m_20182_() : null;
            m.left = Math.max(1, ticks);
            m.hold = hold;
            return m;
        }
    }

    static Move move;

    static void move(net.minecraft.client.player.LocalPlayer self, ClientLevel level) {
        Move m = move;
        if (self == null || !self.m_6084_() || self.m_20159_()) {
            move = null;
            return;
        }
        Vec3 pos = self.m_20182_();
        Vec3 step;
        if (!m.holding) {
            step = m.to.m_82546_(pos).m_82490_(1.0 / Math.max(1, m.left));
            if (--m.left <= 0) {
                m.holding = true;
            }
        } else {
            step = Vec3.f_82478_;
            if (--m.hold < 0) {
                move = null;
            }
        }
        self.m_20334_(step.f_82479_, step.f_82480_, step.f_82481_);
        self.f_19789_ = 0.0F;
    }

    @SubscribeEvent
    public static void steady(net.minecraftforge.client.event.MovementInputUpdateEvent event) {
        if (move == null || event.getEntity() != Minecraft.m_91087_().f_91074_) {
            return;
        }
        net.minecraft.client.player.Input input = event.getInput();
        input.f_108566_ = 0.0F;
        input.f_108567_ = 0.0F;
        input.f_108572_ = false;
        input.f_108573_ = false;
    }

    static void clear() {
        SPOTS.clear();
        SLASHES.clear();
        SPINS.clear();
        MARKS.clear();
        PARTS.clear();
        move = null;
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
