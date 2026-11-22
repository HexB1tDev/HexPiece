package com.hexhaki.piece.client.magu;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.magu.MaguArt;
import com.hexhaki.piece.magu.MaguSounds;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.MaguMoves;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.1.3: the Magu Magu no Mi's moves as every client sees them (server/MaguMoves sends S2CUoFx kinds 100 to 119; client/dragon/UoFx hands
 * them here): Akainu's magma in 3D (magu/MaguArt), drawn once a frame after the particles through the game's own debugQuads type (alpha
 * blended, depth tested, no culling: the owner's device), eye-centred. Here too: the fist swelling (on the posed fist, client/
 * FirstPersonLight.fist), one's own lunge and the stream of magma flown by the game's own movement, the body hidden while it is magma,
 * the bubbling held while magma gathers or the ground boils (client/LoopSound: faded out the moment the move ends), the attack button
 * given back to the game's punch.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class MaguFx {
    private static long clock;
    private static boolean kitAdded;

    /** One effect in the world. */
    record Spot(int kind, int id, int user, Vec3 at, Vec3 dir, double size, double bx, int ticks, long born) {
    }

    /** Inugami Guren's hound: its head's start, step a tick (3.1.5: straight along the aim), scale, life, seed. */
    record Hound(Vec3 at, Vec3 step, double scale, int life, long born, int seed) {
    }

    /** A player gone into a stream of magma: until, and the way it went (newest first). */
    static final class Stream {
        final long until;
        final double[] points = new double[14 * 3];
        int count;

        Stream(long until) {
            this.until = until;
        }
    }

    static final int MOST_SPOTS = 64;
    private static final List<Spot> SPOTS = new ArrayList<>();
    private static final List<Hound> HOUNDS = new ArrayList<>();
    static final Map<Integer, Long> SWELLS = new HashMap<>();
    static final Map<Integer, Stream> STREAMS = new HashMap<>();
    static final double FAR = 160.0;

    private MaguFx() {
    }

    static Entity entity(int id) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? null : level.m_6815_(id);
    }

    static boolean firstPerson(int id) {
        return self(id) && Minecraft.m_91087_().f_91066_.m_92176_() == net.minecraft.client.CameraType.FIRST_PERSON;
    }

    /** 3.1.8 (client/HandPass): whether {@code id} has a swelling fist to draw on the first-person fist. */
    public static boolean handWanted(int id) {
        return SWELLS.containsKey(id);
    }

    /** 3.1.8 (client/HandPass): one's own swelling fist of magma, in the hand's pass, on the fist (small: it sits close to the eye). */
    public static void handPass(FireMesh.Sink sink, int id, float partial) {
        Long since = SWELLS.get(id);
        if (since == null) {
            return;
        }
        double age = clock - since + partial;
        double[] v = com.hexhaki.piece.pika.LightArt.firstPersonFingertip(-0.05);
        MaguArt.swell(sink, v[0], v[1], v[2], Math.min(0.22, 0.22 + 0.38 * Math.min(1.0, age / 40.0)), age, id);
    }

    static boolean self(int id) {
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        return self != null && self.m_19879_() == id;
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
            case MaguMoves.BURST -> {
                int ended = (int) m.by();
                if (ended != 0) {
                    SPOTS.removeIf(s -> (s.kind == MaguMoves.FIST || s.kind == MaguMoves.METEOR) && s.id == ended);
                    HOUNDS.removeIf(h -> h.seed == ended);
                }
                add(new Spot(m.kind(), m.id(), m.user(), at, a, m.size(), m.bx(), 0, clock));
            }
            case MaguMoves.FIST, MaguMoves.POOL, MaguMoves.METEOR, MaguMoves.SKY, MaguMoves.GEYSER, MaguMoves.PIERCE, MaguMoves.TOUCH,
                MaguMoves.ERUPT -> {
                add(new Spot(m.kind(), m.id(), m.user(), at, a, m.size(), m.bx(), m.ticks(), clock));
                if (m.kind() == MaguMoves.ERUPT) {
                    long until = clock + m.ticks();
                    com.hexhaki.piece.client.LoopSound.play(MaguSounds.BUBBLE, entity(m.user()), 1.4F, 0.9F, true, true, 0.2,
                        () -> clock < until - 8);
                }
            }
            case MaguMoves.SWELL_ON -> {
                Long since = clock;
                SWELLS.put(m.user(), since);
                com.hexhaki.piece.client.LoopSound.play(MaguSounds.BUBBLE, entity(m.user()), 0.8F, 1.3F, true, true, 1.2,
                    () -> since.equals(SWELLS.get(m.user())));
            }
            case MaguMoves.SWELL_OFF -> SWELLS.remove(m.user());
            case MaguMoves.HOUND -> HOUNDS.add(new Hound(at, a, m.size(), m.ticks(), clock, m.id()));
            case MaguMoves.FLOW -> {
                STREAMS.put(m.user(), new Stream(clock + m.ticks() + 1));
                if (self(m.user())) {
                    move = Move.dash(at, -1, m.ticks(), 2);
                }
            }
            case MaguMoves.DASH -> {
                if (self(m.user())) {
                    move = Move.dash(at, m.id(), m.ticks(), (int) m.bx());
                }
            }
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
            ClientKits.KITS.put(Fruit.MAGU, new MaguClient()); // the original has no client kit for it
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.m_91104_()) {
            return;
        }
        SPOTS.removeIf(s -> clock - s.born > life(s) + 1);
        HOUNDS.removeIf(h -> clock - h.born > h.life + 2);
        SWELLS.entrySet().removeIf(e -> clock - e.getValue() > 200 || entity(e.getKey()) == null);
        STREAMS.entrySet().removeIf(e -> e.getValue().until + 6 < clock || entity(e.getKey()) == null);
        for (Map.Entry<Integer, Stream> e : STREAMS.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            Stream st = e.getValue();
            if (user != null && clock <= st.until) {
                System.arraycopy(st.points, 0, st.points, 3, Math.min(st.count, st.points.length / 3 - 1) * 3);
                Vec3 p = user.m_20182_();
                st.points[0] = p.f_82479_;
                st.points[1] = p.f_82480_;
                st.points[2] = p.f_82481_;
                st.count = Math.min(st.points.length / 3, st.count + 1);
            } else {
                st.count = Math.max(0, st.count - 2);
            }
        }
        if (move != null) {
            move(minecraft.f_91074_, level);
        }
    }

    static double life(Spot s) {
        return switch (s.kind) {
            case MaguMoves.FIST, MaguMoves.METEOR -> s.ticks + 2;
            case MaguMoves.BURST -> MaguArt.burstLife(s.size);
            case MaguMoves.POOL, MaguMoves.ERUPT -> s.ticks;
            case MaguMoves.SKY -> 12;
            case MaguMoves.GEYSER -> MaguArt.GEYSER_LIFE;
            case MaguMoves.PIERCE -> MaguArt.PIERCE_LIFE;
            case MaguMoves.TOUCH -> MaguArt.burstLife(0.6);
            default -> 20;
        };
    }

    /** Whether {@code player} is a stream of magma now (not drawn: only the magma). */
    static boolean melted(Player player) {
        Stream st = STREAMS.get(player.m_19879_());
        return st != null && clock <= st.until;
    }

    @SubscribeEvent
    public static void asMagma(net.minecraftforge.client.event.RenderPlayerEvent.Pre event) {
        if (melted(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void noHands(net.minecraftforge.client.event.RenderHandEvent event) {
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        if (self != null && melted(self)) {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------------------------------------------ one's own lunge and stream

    /** Meigo's lunge and the stream of magma: one's own body carried to a place over so many ticks (following whom it was sent after), then held. */
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
        Entity e = m.target >= 0 ? level.m_6815_(m.target) : null;
        Vec3 to = m.to;
        if (e != null && m.targetFrom != null) {
            to = to.m_82549_(e.m_20182_().m_82546_(m.targetFrom));
        }
        Vec3 step;
        if (!m.holding) {
            step = to.m_82546_(pos).m_82490_(1.0 / Math.max(1, m.left));
            if (--m.left <= 0) {
                m.holding = true;
                if (e != null) {
                    face(self, to, e);
                }
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

    static void face(net.minecraft.client.player.LocalPlayer self, Vec3 feet, Entity e) {
        Vec3 d = e.m_20182_().m_82520_(0, e.m_20206_() * 0.6, 0).m_82546_(feet.m_82520_(0, self.m_20192_(), 0));
        float yaw = (float) Math.toDegrees(Math.atan2(-d.f_82479_, d.f_82481_));
        float pitch = (float) -Math.toDegrees(Math.atan2(d.f_82480_, Math.sqrt(d.f_82479_ * d.f_82479_ + d.f_82481_ * d.f_82481_)));
        self.m_146922_(yaw);
        self.m_146926_(pitch);
        self.f_19859_ = yaw;
        self.f_19860_ = pitch;
        self.f_20885_ = yaw;
        self.f_20886_ = yaw;
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

    /** The attack button the game's own punch with the Magma fruit (not while the fist swells or one's own lunge or stream flies). */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
    public static void punch(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || !event.isCanceled()) {
            return;
        }
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        if (self == null || move != null || SWELLS.containsKey(self.m_19879_()) || !com.hexhaki.fruit.client.FruitClient.active()
            || com.hexhaki.fruit.client.FruitClient.fruit(self.m_19879_()) != Fruit.MAGU || !self.m_21205_().m_41619_()) {
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
    static final double[] TRAIL = new double[14 * 3];

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || SPOTS.isEmpty() && HOUNDS.isEmpty() && SWELLS.isEmpty()
            && STREAMS.isEmpty()) {
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
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: a Magma fruit move could not be drawn", failure);
            clear();
        }
        FireMesh.palette(FireMesh.BAND);
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
        // the burning ground first, then what stands and flies over it
        for (Spot s : SPOTS) {
            if (far(s.at, cam)) {
                continue;
            }
            double age = clock - s.born + partial;
            if (s.kind == MaguMoves.POOL) {
                MaguArt.pool(SINK, s.at.f_82479_ - cx, s.at.f_82480_ - cy, s.at.f_82481_ - cz, s.size, age, s.ticks, s.id);
            } else if (s.kind == MaguMoves.ERUPT) {
                MaguArt.pool(SINK, s.at.f_82479_ - cx, s.at.f_82480_ - cy, s.at.f_82481_ - cz, s.size, age, s.ticks, s.id);
            }
        }
        for (Map.Entry<Integer, Stream> e : STREAMS.entrySet()) {
            Stream st = e.getValue();
            if (st.count < 1) {
                continue;
            }
            for (int i = 0; i < st.count; i++) {
                TRAIL[i * 3] = st.points[i * 3] - cx;
                TRAIL[i * 3 + 1] = st.points[i * 3 + 1] - cy;
                TRAIL[i * 3 + 2] = st.points[i * 3 + 2] - cz;
            }
            MaguArt.stream(SINK, TRAIL, st.count, clock + partial, e.getKey());
        }
        for (Hound h : HOUNDS) {
            double age = clock - h.born + partial;
            Vec3 front = h.at.m_82549_(h.step.m_82490_(age));
            if (far(front, cam)) {
                continue;
            }
            MaguArt.hound(SINK, front.f_82479_ - cx, front.f_82480_ - cy, front.f_82481_ - cz, h.step.f_82479_, h.step.f_82480_, h.step.f_82481_,
                h.scale, age, h.seed);
        }
        for (Spot s : SPOTS) {
            if (far(s.at, cam)) {
                continue;
            }
            double age = clock - s.born + partial;
            double x = s.at.f_82479_ - cx;
            double y = s.at.f_82480_ - cy;
            double z = s.at.f_82481_ - cz;
            switch (s.kind) {
                case MaguMoves.FIST -> {
                    double vl = Math.max(1.0E-6, s.dir.m_82553_());
                    MaguArt.fist(SINK, x + s.dir.f_82479_ * age, y + s.dir.f_82480_ * age, z + s.dir.f_82481_ * age, s.dir.f_82479_ / vl,
                        s.dir.f_82480_ / vl, s.dir.f_82481_ / vl, s.size, age, s.id);
                }
                case MaguMoves.METEOR -> {
                    double t = Math.min(1.0, age / Math.max(1, s.ticks));
                    double vx = (s.dir.f_82479_ - s.at.f_82479_) / Math.max(1, s.ticks);
                    double vy = (s.dir.f_82480_ - s.at.f_82480_) / Math.max(1, s.ticks);
                    double vz = (s.dir.f_82481_ - s.at.f_82481_) / Math.max(1, s.ticks);
                    if (t < 1.0) {
                        MaguArt.meteor(SINK, x + vx * age, y + vy * age, z + vz * age, vx, vy, vz, s.size, age, s.id);
                    }
                }
                case MaguMoves.SKY -> MaguArt.meteor(SINK, x, y + 2.6 * age, z, 0, 2.6, 0, s.size, age, s.id);
                case MaguMoves.BURST -> MaguArt.burst(SINK, x, y, z, s.size, s.bx, age, s.id);
                case MaguMoves.TOUCH -> MaguArt.burst(SINK, x, y, z, 0.6, 0.2, age, s.id);
                case MaguMoves.GEYSER -> MaguArt.geyser(SINK, x, y, z, s.size, age, s.id);
                case MaguMoves.PIERCE -> MaguArt.pierce(SINK, x, y, z, s.dir.f_82479_, s.dir.f_82480_, s.dir.f_82481_, s.size, age, s.id);
                default -> {
                }
            }
        }
        for (Map.Entry<Integer, Long> e : SWELLS.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof Player player) || firstPerson(e.getKey())) {
                continue; // 3.1.8: one's own, in first person, is drawn on the fist in the hand's pass (client/HandPass)
            }
            double age = clock - e.getValue() + partial;
            Vec3 fist = com.hexhaki.piece.client.FirstPersonLight.fist(player, partial);
            if (fist == null) {
                Vec3 look = player.m_20252_(partial);
                fist = player.m_20299_(partial).m_82549_(look.m_82490_(0.8)).m_82520_(0, -0.3, 0);
            }
            double radius = 0.22 + 0.38 * Math.min(1.0, age / 40.0);
            if (self(e.getKey()) && Minecraft.m_91087_().f_91066_.m_92176_() == net.minecraft.client.CameraType.FIRST_PERSON) {
                radius = Math.min(radius, 0.22);
            }
            MaguArt.swell(SINK, fist.f_82479_ - cx, fist.f_82480_ - cy, fist.f_82481_ - cz, radius, age, e.getKey());
        }
    }

    static void clear() {
        SPOTS.clear();
        HOUNDS.clear();
        SWELLS.clear();
        STREAMS.clear();
        move = null;
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
