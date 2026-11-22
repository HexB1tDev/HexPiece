package com.hexhaki.piece.client.gura;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gura.GuraArt;
import com.hexhaki.piece.gura.GuraSounds;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.GuraMoves;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.1.1: the Gura Gura no Mi's moves as every client sees them (server/GuraMoves sends S2CUoFx kinds 60 to 79; client/dragon/UoFx hands
 * them here): the quakes in 3D (gura/GuraArt), drawn once a frame after the particles through the game's own debugQuads type (alpha
 * blended, depth tested, no culling: the owner's device), eye-centred. Here too: the quake bubble on the fist (where the clip's pose has
 * it), the view shaken and tilted by Shima Yurashi, one's own leap and dash flown by the game's own movement, the rumble held while a
 * quake is held (client/LoopSound: it fades out the moment the move ends), the attack button given back to the game's punch.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class GuraFx {
    private static long clock;
    private static boolean kitAdded;

    /** One effect in the world: what, where, which way, its numbers, when. */
    record Spot(int kind, int id, int user, Vec3 at, Vec3 dir, double size, double bx, double by, long born, int seed) {
    }

    /** A quake bubble on {@code user}'s fist: since, which (0 Kaishin, 1 Gekishin, 2 Kabutowari), until (-1: until let go). */
    record Bubble(long since, int which, long until) {
    }

    /** Shima Yurashi held by its user: since, its middle (where it began), radius. */
    record Shake(long since, Vec3 at, double radius) {
    }

    /** The rift: its start, way, length, how far it opens a tick, the ground's height under each block (relative to the start). */
    record Rift(Vec3 at, Vec3 dir, double length, double[] heights, double wide, long born, int seed) {
    }

    /** The tsunami: its start, its step, its width and height, its life, its foot's height now (following the ground). */
    static final class Wave {
        final Vec3 at;
        final Vec3 step;
        final double width;
        final double height;
        final int life;
        final int hold;
        final long born;
        final int seed;
        double baseY;

        Wave(Vec3 at, Vec3 step, double width, double height, int life, int hold, long born, int seed) {
            this.at = at;
            this.hold = hold;
            this.step = step;
            this.width = width;
            this.height = height;
            this.life = life;
            this.born = born;
            this.seed = seed;
            this.baseY = at.f_82480_;
        }
    }

    static final int MOST_SPOTS = 48;
    private static final List<Spot> SPOTS = new ArrayList<>();
    private static final List<Rift> RIFTS = new ArrayList<>();
    private static final List<Wave> WAVES = new ArrayList<>();
    static final Map<Integer, Bubble> BUBBLES = new HashMap<>();
    static final Map<Integer, Shake> SHAKES = new HashMap<>();
    static final double FAR = 160.0;

    private GuraFx() {
    }

    static Entity entity(int id) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? null : level.m_6815_(id);
    }

    static boolean self(int id) {
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        return self != null && self.m_19879_() == id;
    }

    /** Whether {@code id} is the local player seen in first person. */
    static boolean ownView(int id) {
        return self(id) && Minecraft.m_91087_().f_91066_.m_92176_() == net.minecraft.client.CameraType.FIRST_PERSON;
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
            case GuraMoves.CRACK, GuraMoves.SHOCK, GuraMoves.QUAKE, GuraMoves.TREMOR -> add(new Spot(m.kind(), m.id(), m.user(), at, a, m.size(),
                m.bx(), m.by(), clock, m.ticks()));
            case GuraMoves.BUBBLE_ON -> {
                Bubble bubble = new Bubble(clock, m.id(), -1L);
                BUBBLES.put(m.user(), bubble);
                // the quake gathering: the ground's rumble, higher, held while the bubble is (faded out the moment it is let go)
                com.hexhaki.piece.client.LoopSound.play(GuraSounds.RUMBLE, entity(m.user()), 0.7F, m.id() == 1 ? 1.35F : 1.6F, true, true, 1.2,
                    () -> BUBBLES.get(m.user()) == bubble);
            }
            case GuraMoves.BUBBLE_OFF -> {
                Bubble b = BUBBLES.get(m.user());
                if (b != null && b.until < 0) {
                    BUBBLES.remove(m.user());
                }
            }
            case GuraMoves.SHAKE_ON -> {
                Shake shake = new Shake(clock, at.m_82520_(0, -0.9, 0), m.size());
                SHAKES.put(m.user(), shake);
                com.hexhaki.piece.client.LoopSound.play(GuraSounds.RUMBLE, entity(m.user()), 1.6F, 0.85F, true, true, 0.5,
                    () -> SHAKES.get(m.user()) == shake);
            }
            case GuraMoves.SHAKE_OFF -> {
                SHAKES.remove(m.user());
                // the cracks at both fists, held while it was, shatter now
                for (int i = 0; i < SPOTS.size(); i++) {
                    Spot s = SPOTS.get(i);
                    if (s.kind == GuraMoves.CRACK && s.id == 2 && s.user == m.user()) {
                        SPOTS.set(i, new Spot(s.kind, 3, s.user, s.at, s.dir, s.size, s.bx, s.by, clock - (long) GuraArt.CRACK_HOLD, s.seed));
                    }
                }
                if (m.size() > 0.0F) {
                    add(new Spot(GuraMoves.SHAKE_OFF, 0, m.user(), at.m_82520_(0, -0.9, 0), a, m.size(), m.bx(), 0, clock, m.ticks()));
                    TILTS.put(m.user(), new double[] {clock, m.bx(), at.f_82479_, at.f_82481_, m.size()});
                }
            }
            case GuraMoves.RIFT -> RIFTS.add(new Rift(at, a, m.size(), heights(at, a, m.size()), m.bx() > 0.0F ? 1.8 : 1.0, clock, m.id()));
            case GuraMoves.TSUNAMI -> WAVES.add(new Wave(at, a, m.size(), m.bx(), m.ticks(), (int) m.by(), clock, m.id()));
            case GuraMoves.LEAP -> {
                // a puff of dust where the feet leave the ground, the quake fist alight for the slam
                add(new Spot(GuraMoves.TREMOR, 0, m.user(), a, Vec3.f_82478_, 2.2, 0, 0, clock, m.user() * 7 + (int) clock));
                BUBBLES.put(m.user(), new Bubble(clock, 2, clock + m.ticks() + 4));
                if (self(m.user())) {
                    move = Move.leap(a, at, m.id(), m.ticks(), m.bx());
                }
            }
            case GuraMoves.DASH -> {
                Bubble held = BUBBLES.get(m.user());
                BUBBLES.put(m.user(), new Bubble(held != null ? held.since : clock - 20, 1, clock + m.ticks() + 3));
                if (self(m.user())) {
                    move = Move.dash(at, m.id(), m.ticks(), (int) m.bx());
                }
            }
            default -> {
            }
        }
    }

    /** The ground's height under each block of a rift (relative to its start), worked out once: it opens along the ground's own lie. */
    static double[] heights(Vec3 start, Vec3 dir, double length) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        int n = (int) Math.ceil(length) + 1;
        double[] out = new double[n];
        if (level == null) {
            return out;
        }
        double y = start.f_82480_;
        for (int k = 0; k < n; k++) {
            Vec3 p = start.m_82549_(dir.m_82490_(k));
            BlockHitResult hit = level.m_45547_(new ClipContext(new Vec3(p.f_82479_, y + 4.0, p.f_82481_), new Vec3(p.f_82479_, y - 8.0, p.f_82481_),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
            if (hit.m_6662_() == HitResult.Type.BLOCK) {
                y = hit.m_82450_().f_82480_;
            }
            out[k] = y - start.f_82480_;
        }
        return out;
    }

    // ------------------------------------------------------------------------------------------------------ the clock

    /** The island tilting under the views near it: per user {since, power, x, z, radius}. */
    static final Map<Integer, double[]> TILTS = new HashMap<>();

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clock++;
        if (!kitAdded) {
            kitAdded = true;
            ClientKits.KITS.put(Fruit.GURA, new GuraClient()); // the original has no client kit for it at all
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.m_91104_()) {
            return;
        }
        SPOTS.removeIf(s -> clock - s.born > life(s) + 1);
        RIFTS.removeIf(r -> clock - r.born > r.length / GuraMoves.RIFT_STEP + GuraArt.RIFT_AFTER + 1);
        WAVES.removeIf(w -> clock - w.born > GuraArt.tsunamiLife(w.life) + 1);
        BUBBLES.entrySet().removeIf(e -> e.getValue().until >= 0 && e.getValue().until < clock || entity(e.getKey()) == null);
        SHAKES.entrySet().removeIf(e -> clock - e.getValue().since > GuraMoves.SHAKE_MAX + 40 || entity(e.getKey()) == null);
        TILTS.entrySet().removeIf(e -> clock - e.getValue()[0] > 30);
        for (Wave w : WAVES) {
            // the wave's foot follows the ground under its middle
            double age = clock - w.born;
            Vec3 front = w.at.m_82549_(w.step.m_82490_(Math.max(0.0, Math.min(age, w.life + 6.0) - w.hold)));
            // 3.1.2: on the water's face where there is water (Fluid.ANY), the ground where there is none
            BlockHitResult hit = level.m_45547_(new ClipContext(new Vec3(front.f_82479_, w.baseY + 6.0, front.f_82481_),
                new Vec3(front.f_82479_, w.baseY - 10.0, front.f_82481_), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null));
            if (hit.m_6662_() == HitResult.Type.BLOCK) {
                w.baseY += (hit.m_82450_().f_82480_ - w.baseY) * 0.5;
            }
        }
        if (move != null) {
            move(minecraft.f_91074_, level);
        }
    }

    static double life(Spot s) {
        return switch (s.kind) {
            case GuraMoves.CRACK -> s.id == 2 ? GuraMoves.SHAKE_MAX + GuraArt.CRACK_LIFE + 40.0 : GuraArt.CRACK_LIFE;
            case GuraMoves.SHOCK -> GuraArt.shockLife(s.size, GuraMoves.SHOCK_SPEED);
            case GuraMoves.QUAKE -> GuraArt.QUAKE_LIFE;
            case GuraMoves.TREMOR -> GuraArt.TREMOR_LIFE;
            case GuraMoves.SHAKE_OFF -> GuraArt.tiltLife(s.size, GuraMoves.SHOCK_SPEED);
            default -> 20.0;
        };
    }

    // ------------------------------------------------------------------------------------------------------ the view

    /**
     * Shima Yurashi (the owner's island shaker): every view within its radius shakes and rolls while it is held (harder the longer it
     * is held, weaker further off), and lurches over when it is let go (the island tilting). ViewportEvent's roll, as client/fx/Tumbles.
     */
    @SubscribeEvent
    public static void tilt(ViewportEvent.ComputeCameraAngles event) {
        if (SHAKES.isEmpty() && TILTS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ == null) {
            return;
        }
        double t = clock + event.getPartialTick();
        Vec3 me = minecraft.f_91074_.m_20182_();
        double roll = 0.0;
        double pitch = 0.0;
        for (Map.Entry<Integer, Shake> e : SHAKES.entrySet()) {
            Shake s = e.getValue();
            Entity user = entity(e.getKey());
            Vec3 c = user != null ? user.m_20182_() : s.at;
            double d = Math.sqrt((me.f_82479_ - c.f_82479_) * (me.f_82479_ - c.f_82479_) + (me.f_82481_ - c.f_82481_) * (me.f_82481_ - c.f_82481_));
            if (d > s.radius * 1.5) {
                continue;
            }
            double grip = Math.min(1.0, (t - s.since) / 60.0);
            double near = 1.0 - 0.6 * Math.min(1.0, d / (s.radius * 1.5));
            double amp = (0.8 + 2.6 * grip) * near;
            roll += amp * (Math.sin(t * 0.95) + 0.4 * Math.sin(t * 2.3));
            pitch += amp * 0.35 * Math.sin(t * 1.7);
        }
        for (double[] tilt : TILTS.values()) {
            double d = Math.sqrt((me.f_82479_ - tilt[2]) * (me.f_82479_ - tilt[2]) + (me.f_82481_ - tilt[3]) * (me.f_82481_ - tilt[3]));
            if (d > tilt[4] * 1.5) {
                continue;
            }
            double age = t - tilt[0] - d / GuraMoves.SHOCK_SPEED;
            if (age < 0.0 || age > 24.0) {
                continue;
            }
            double env = Math.sin(Math.PI * age / 24.0) * (1.0 - age / 30.0);
            roll += 10.0 * tilt[1] * env * (1.0 - 0.5 * d / (tilt[4] * 1.5));
        }
        if (roll != 0.0 || pitch != 0.0) {
            event.setRoll(event.getRoll() + (float) roll);
            event.setPitch(Math.max(-90.0F, Math.min(90.0F, event.getPitch() + (float) pitch)));
        }
    }

    // ------------------------------------------------------------------------------------------------------ one's own leap and dash

    /**
     * Kabutowari's leap (an arc from where the feet left to where they land, following whom it was sent after) and Gekishin's dash (to
     * just in front of them, then hung there facing them): one's own body moved by the game's own movement, its velocity set a tick
     * ahead (as the Light fruit's glide), so it is smooth on screen and the server's movement check agrees.
     */
    static final class Move {
        static final int LEAP = 0;
        static final int DASH = 1;
        static final int HOLD = 2;
        int mode;
        Vec3 from;
        Vec3 to;
        int target;
        Vec3 targetFrom;
        int ticks;
        int left;
        int hold;
        double arc;

        static Move leap(Vec3 from, Vec3 to, int target, int ticks, double arc) {
            Move m = new Move();
            m.mode = LEAP;
            m.from = from;
            m.to = to;
            m.target = target;
            Entity e = target >= 0 ? entity(target) : null;
            m.targetFrom = e != null ? e.m_20182_() : null;
            m.ticks = Math.max(1, ticks);
            m.left = m.ticks;
            m.hold = 2;
            m.arc = arc;
            return m;
        }

        static Move dash(Vec3 to, int target, int ticks, int hold) {
            Move m = new Move();
            m.mode = DASH;
            m.to = to;
            m.target = target;
            Entity e = target >= 0 ? entity(target) : null;
            m.targetFrom = e != null ? e.m_20182_() : null;
            m.ticks = Math.max(1, ticks);
            m.left = m.ticks;
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
        if (m.mode == Move.LEAP) {
            int k = m.ticks - m.left + 1;
            double t = k / (double) m.ticks;
            Vec3 p = m.from.m_82549_(to.m_82546_(m.from).m_82490_(t)).m_82520_(0, m.arc * 4.0 * t * (1.0 - t), 0);
            step = p.m_82546_(pos);
            if (--m.left <= 0) {
                m.mode = Move.HOLD;
                if (e != null) {
                    face(self, to, e);
                }
            }
        } else if (m.mode == Move.DASH) {
            step = to.m_82546_(pos).m_82490_(1.0 / Math.max(1, m.left));
            if (--m.left <= 0) {
                m.mode = Move.HOLD;
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

    /** Turned (body, head and view) toward {@code e} from feet at {@code feet}. */
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

    /** While one's own leap or dash flies, no movement key moves the body. */
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

    /**
     * The attack button the game's own punch with the Gura fruit (as the Light fruit's since 3.0.4: the original's fruit mode takes it
     * from an empty hand and sends it to the kit), not while a quake is gathered or held or one's own leap or dash flies.
     */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
    public static void punch(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || !event.isCanceled()) {
            return;
        }
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        if (self == null || move != null || BUBBLES.containsKey(self.m_19879_()) || SHAKES.containsKey(self.m_19879_())
            || !com.hexhaki.fruit.client.FruitClient.active() || com.hexhaki.fruit.client.FruitClient.fruit(self.m_19879_()) != Fruit.GURA
            || !self.m_21205_().m_41619_()) {
            return;
        }
        event.setCanceled(false);
        event.setSwingHand(true);
    }

    // ------------------------------------------------------------------------------------------------------ the drawing

    /** FireMesh's quads straight into the game's buffer, through the view. */
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
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || SPOTS.isEmpty() && RIFTS.isEmpty() && WAVES.isEmpty()
            && BUBBLES.isEmpty() && SHAKES.isEmpty()) {
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
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: a Gura fruit move could not be drawn", failure);
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
        // the ground first (far under everything), then the water, then what hangs in the air
        for (Rift r : RIFTS) {
            if (far(r.at, cam) && far(r.at.m_82549_(r.dir.m_82490_(r.length)), cam)) {
                continue;
            }
            GuraArt.rift(SINK, r.at.f_82479_ - cx, r.at.f_82480_ - cy, r.at.f_82481_ - cz, r.dir.f_82479_, r.dir.f_82481_, r.length,
                GuraMoves.RIFT_STEP, r.heights, r.wide, clock - r.born + partial, r.seed);
        }
        for (Map.Entry<Integer, Shake> e : SHAKES.entrySet()) {
            Shake s = e.getValue();
            if (far(s.at, cam)) {
                continue;
            }
            GuraArt.shake(SINK, s.at.f_82479_ - cx, s.at.f_82480_ - cy, s.at.f_82481_ - cz, s.radius, clock - s.since + partial, 10,
                e.getKey() * 13 + (int) s.since);
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
                case GuraMoves.QUAKE -> GuraArt.quake(SINK, x, y, z, s.size, s.bx, age, s.seed);
                case GuraMoves.TREMOR -> GuraArt.tremor(SINK, x, y, z, s.size, age, s.seed);
                case GuraMoves.SHAKE_OFF -> GuraArt.tilt(SINK, x, y, z, s.size, s.bx, GuraMoves.SHOCK_SPEED, age, s.seed);
                default -> {
                }
            }
        }
        for (Wave w : WAVES) {
            if (far(w.at, cam) && far(w.at.m_82549_(w.step.m_82490_(w.life)), cam)) {
                continue;
            }
            GuraArt.tsunami(SINK, w.at.f_82479_ - cx, w.baseY - cy, w.at.f_82481_ - cz, w.step.f_82479_, w.step.f_82481_, w.width, w.height, w.life,
                GuraMoves.TSUNAMI_RISE, w.hold, clock - w.born + partial, w.seed);
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
                case GuraMoves.CRACK -> {
                    // the cracks at both fists of Shima Yurashi hang there as long as it is held, then shatter
                    if (s.id == 2) {
                        age = Math.min(age, GuraArt.CRACK_HOLD - 0.5);
                    }
                    if (ownView(s.user)) {
                        // one's own crack in first person: out along the look far enough that it never fills the view
                        double d = Math.sqrt(x * x + y * y + z * z);
                        double want = s.size * 1.7;
                        if (d < want) {
                            x += s.dir.f_82479_ * (want - d);
                            y += s.dir.f_82480_ * (want - d);
                            z += s.dir.f_82481_ * (want - d);
                        }
                    }
                    GuraArt.crack(SINK, x, y, z, s.dir.f_82479_, s.dir.f_82480_, s.dir.f_82481_, s.size, age, s.seed);
                }
                case GuraMoves.SHOCK -> GuraArt.shock(SINK, x, y, z, s.dir.f_82479_, s.dir.f_82480_, s.dir.f_82481_, s.size, s.bx, s.by,
                    GuraMoves.SHOCK_SPEED, age, s.seed);
                default -> {
                }
            }
        }
        for (Map.Entry<Integer, Bubble> e : BUBBLES.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof Player player)) {
                continue;
            }
            Bubble b = e.getValue();
            double age = clock - b.since + partial;
            Vec3 fist = fist(player, partial);
            if (far(fist, cam)) {
                continue;
            }
            double grow = Math.min(1.0, age / (b.which == 1 ? 30.0 : 40.0));
            double radius = b.which == 2 ? 0.42 : b.which == 1 ? 0.26 + 0.34 * grow : 0.2 + 0.26 * grow;
            if (ownView(e.getKey())) {
                radius = Math.min(radius, 0.22);
            }
            GuraArt.bubble(SINK, fist.f_82479_ - cx, fist.f_82480_ - cy, fist.f_82481_ - cz, radius, age, e.getKey());
        }
    }

    /** The right fist where the clip's pose has it this frame, else out in front of the right shoulder. */
    static Vec3 fist(Player player, float partial) {
        Vec3 posed = com.hexhaki.piece.client.FirstPersonLight.fist(player, partial);
        if (posed != null) {
            return posed;
        }
        Vec3 look = player.m_20252_(partial);
        Vec3 side = look.m_82537_(new Vec3(0, 1, 0));
        side = side.m_82556_() < 1.0E-6 ? new Vec3(1, 0, 0) : side.m_82541_();
        return player.m_20299_(partial).m_82549_(look.m_82490_(0.8)).m_82549_(side.m_82490_(0.36)).m_82520_(0.0, -0.3, 0.0);
    }

    static void clear() {
        SPOTS.clear();
        RIFTS.clear();
        WAVES.clear();
        BUBBLES.clear();
        SHAKES.clear();
        TILTS.clear();
        move = null;
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

}
