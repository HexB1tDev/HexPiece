package com.hexhaki.piece.client.ironfist;

import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gura.GuraArt;
import com.hexhaki.piece.ironfist.IronArt;
import com.hexhaki.piece.ironfist.IronMove;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.IronFist;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.5.8: Garp's Iron Fist as every client sees it (server/IronFist sends S2CUoFx kinds 320 to 339; client/dragon/UoFx hands them here),
 * drawn once a frame after the particles through the game's own debugQuads type (alpha blended, depth tested), eye-centred
 * (ironfist/IronArt, gura/GuraArt), on the beats of ironfist/IronMove. One's own body is carried here (Blue Hole's dash, Galaxy Divide's
 * leap, Galaxy Impact's ascent, hang and descent: the server lets the player hang in the air meanwhile); Galaxy Impact's hang is filmed
 * from a low angle round the body, the world darkens under its charge and lights up white when it lands, seen from far off.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class IronFx {
    private static long clock;

    record Pop(Vec3 at, Vec3 way, boolean landed, long born) {
    }

    record Love(Vec3 at, boolean landed, long born, int seed) {
    }

    record Ball(Vec3 from, Vec3 step, double radius, double charge, long born, int flight, int seed) {
    }

    record Boom(Vec3 at, Vec3 way, double radius, double charge, long born, int seed) {
    }

    record Crush(Vec3 at, Vec3 way, double size, long born, int seed) {
    }

    record Ring(Vec3 at, double radius, long born, int life, int seed) {
    }

    record Hole(Vec3 at, double depth, long born, int seed) {
    }

    record Divide(Vec3 base, Vec3 way, double length, long born, int run, int seed) {
    }

    record Blast(Vec3 at, Vec3 way, int k, long born) {
    }

    record Finish(Vec3 at, Vec3 way, long born, int seed) {
    }

    record Strike(Vec3 at, Vec3 down, double drop, long born, int seed) {
    }

    record Debris(Vec3 at, double reach, double lift, int count, long born, int seed) {
    }

    /** A move held or playing on a user (by user): since when, how long, its seed, where it began. */
    record Doing(int kind, long born, int ticks, int seed, Vec3 at) {
    }

    /** One's own body carried: from, to, over so many ticks (an arc's height), or Galaxy Impact's ride from its feet. */
    static final class Carry {
        final Vec3 from;
        final Vec3 to;
        final long born;
        final int ticks;
        final double arc;
        final boolean impact;
        long holdUntil;

        Carry(Vec3 from, Vec3 to, long born, int ticks, double arc, boolean impact) {
            this.from = from;
            this.to = to;
            this.born = born;
            this.ticks = ticks;
            this.arc = arc;
            this.impact = impact;
        }
    }

    static final int MOST = 48;
    static final double FAR = 160.0;
    /** Galaxy Impact is seen from much further. */
    static final double FAR_IMPACT = 600.0;
    private static final List<Pop> POPS = new ArrayList<>();
    private static final List<Love> LOVES = new ArrayList<>();
    static final Map<Integer, Ball> BALLS = new HashMap<>();
    private static final List<Boom> BOOMS = new ArrayList<>();
    private static final List<Crush> CRUSHES = new ArrayList<>();
    private static final List<Ring> RINGS = new ArrayList<>();
    private static final List<Hole> HOLES = new ArrayList<>();
    private static final List<Divide> DIVIDES = new ArrayList<>();
    private static final List<Blast> BLASTS = new ArrayList<>();
    private static final List<Finish> FINISHES = new ArrayList<>();
    private static final List<Strike> STRIKES = new ArrayList<>();
    private static final List<Debris> DEBRIS = new ArrayList<>();
    /** What each user is doing (Meteor held, Crusher's coil, the grab, the barrage, Galaxy Impact). */
    static final Map<Integer, Doing> DOING = new HashMap<>();
    static Carry carry;
    private static CameraType savedCamera;

    private IronFx() {
    }

    static <T> void add(List<T> list, T value) {
        if (list.size() >= MOST) {
            list.remove(0);
        }
        list.add(value);
    }

    static boolean self(int id) {
        var self = Minecraft.m_91087_().f_91074_;
        return self != null && self.m_19879_() == id;
    }

    static Vec3 unit(Vec3 v) {
        double l = v.m_82553_();
        return l < 1.0E-6 ? new Vec3(0, 0, 1) : v.m_82490_(1.0 / l);
    }

    public static void accept(S2CUoFx m) {
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        Vec3 a = new Vec3(m.ax(), m.ay(), m.az());
        int phase = Math.round(m.bx());
        switch (m.kind()) {
            case IronFist.PUNCH -> add(POPS, new Pop(at, unit(a), m.by() > 0.5F, clock));
            case IronFist.LOVE -> add(LOVES, new Love(at, m.by() > 0.5F, clock, m.id()));
            case IronFist.METEOR -> {
                if (phase == 1) {
                    DOING.put(m.user(), new Doing(IronFist.METEOR, clock, m.ticks(), m.id(), at));
                } else {
                    DOING.remove(m.user());
                }
            }
            case IronFist.BALL -> {
                DOING.remove(m.user());
                BALLS.put(m.id(), new Ball(at, a, m.size(), m.by(), clock, m.ticks(), m.id()));
            }
            case IronFist.BALL_END -> {
                BALLS.remove(m.id());
                add(BOOMS, new Boom(at, unit(a), m.size(), m.by(), clock, m.id()));
            }
            case IronFist.CRUSHER -> {
                if (phase == 0) {
                    DOING.put(m.user(), new Doing(IronFist.CRUSHER, clock, m.ticks(), m.id(), at));
                } else {
                    DOING.remove(m.user());
                    add(CRUSHES, new Crush(at, unit(a), m.size(), clock, m.id()));
                }
            }
            case IronFist.HOLE -> hole(m, at, a, phase);
            case IronFist.DIVIDE -> {
                if (phase == 1) {
                    add(RINGS, new Ring(a, 6.0, clock, 12, m.id()));
                    if (self(m.user())) {
                        carry = new Carry(a, at, clock, m.ticks(), 2.5, false);
                        carry.holdUntil = clock + m.ticks() + (IronMove.DIVIDE_PUNCH - IronMove.DIVIDE_CROUCH - IronMove.DIVIDE_LEAP);
                    }
                } else if (phase == 2) {
                    add(DIVIDES, new Divide(at, unit(a), m.size(), clock, m.ticks(), m.id()));
                    if (self(m.user())) {
                        carry = null;
                    }
                }
            }
            case IronFist.INF -> {
                if (phase == 1) {
                    DOING.put(m.user(), new Doing(IronFist.INF, clock, m.ticks(), m.id(), at));
                } else if (phase == 2) {
                    add(BLASTS, new Blast(at, unit(a), Math.round(m.by()), clock));
                } else if (phase == 3) {
                    add(FINISHES, new Finish(at, unit(a), clock, m.id()));
                } else {
                    DOING.remove(m.user());
                }
            }
            case IronFist.IMPACT -> {
                if (phase == 1) {
                    DOING.put(m.user(), new Doing(IronFist.IMPACT, clock, m.ticks(), m.id(), at));
                    if (self(m.user())) {
                        carry = new Carry(at, at, clock, m.ticks(), 0.0, true);
                        com.hexhaki.piece.client.LoopSound.play(com.hexhaki.piece.ironfist.IronSounds.RUMBLE, Minecraft.m_91087_().f_91074_, 0.8F,
                            0.6F, true, true, 1.0, () -> impacting(m.user()) < IronMove.IMPACT_SILENCE);
                    }
                } else if (phase == 2) {
                    add(STRIKES, new Strike(at, unit(a), m.by(), clock, m.id()));
                } else {
                    DOING.remove(m.user());
                }
            }
            case IronFist.DEBRIS -> add(DEBRIS, new Debris(at, m.size(), m.by(), Math.min(24, m.ticks()), clock, m.id()));
            default -> {
            }
        }
    }

    static void hole(S2CUoFx m, Vec3 at, Vec3 a, int phase) {
        switch (phase) {
            case 1 -> {
                add(RINGS, new Ring(a, 6.0, clock, 12, m.id()));
                if (self(m.user())) {
                    carry = new Carry(a, at, clock, m.ticks(), 0.25, false);
                    carry.holdUntil = clock + m.ticks() + IronMove.HOLE_LIFT + 2;
                }
            }
            case 2 -> DOING.put(m.user(), new Doing(IronFist.HOLE, clock, m.ticks(), m.id(), at));
            case 3 -> {
                DOING.remove(m.user());
                add(HOLES, new Hole(at, m.size(), clock, m.id()));
            }
            case 4 -> {
                if (self(m.user())) {
                    carry = null;
                }
            }
            default -> {
            }
        }
    }

    /** How far into Galaxy Impact {@code user} is (ticks), or a large number when not in it. */
    static double impacting(int user) {
        Doing d = DOING.get(user);
        return d == null || d.kind != IronFist.IMPACT ? 1.0E9 : clock - d.born;
    }

    // ------------------------------------------------------------------------------------------------------ the clock

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clock++;
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.m_91104_()) {
            return;
        }
        if (carry != null) {
            ride(minecraft.f_91074_);
        }
        cinema(minecraft);
        POPS.removeIf(p -> clock - p.born > 8);
        LOVES.removeIf(l -> clock - l.born > 14);
        BALLS.values().removeIf(b -> clock - b.born > b.flight + 1);
        BOOMS.removeIf(b -> clock - b.born > GuraArt.QUAKE_LIFE);
        CRUSHES.removeIf(c -> clock - c.born > 20);
        RINGS.removeIf(r -> clock - r.born > r.life);
        HOLES.removeIf(h -> clock - h.born > GuraArt.QUAKE_LIFE);
        DIVIDES.removeIf(d -> clock - d.born > d.run + 60);
        BLASTS.removeIf(b -> clock - b.born > 8);
        FINISHES.removeIf(f -> clock - f.born > GuraArt.QUAKE_LIFE);
        STRIKES.removeIf(s -> clock - s.born > 200);
        DEBRIS.removeIf(d -> clock - d.born > 50);
        DOING.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().ticks + 2 || level.m_6815_(e.getKey()) == null);
    }

    /** One's own body carried along its path this tick (its own fall forgotten, its keys ignored). */
    static void ride(net.minecraft.client.player.LocalPlayer self) {
        Carry c = carry;
        if (self == null || !self.m_6084_() || self.m_20159_()) {
            carry = null;
            return;
        }
        double t = clock - c.born;
        Vec3 pos = self.m_20182_();
        Vec3 want;
        if (c.impact) {
            if (t >= IronMove.IMPACT_TICKS - 2) {
                carry = null;
                return;
            }
            want = c.from.m_82520_(0, IronMove.impactRise(t + 1), 0);
        } else if (t < c.ticks) {
            double u = Math.min(1.0, (t + 1) / c.ticks);
            double e = 1.0 - (1.0 - u) * (1.0 - u);
            want = c.from.m_82549_(c.to.m_82546_(c.from).m_82490_(e)).m_82520_(0, c.arc * 4.0 * e * (1.0 - e), 0);
        } else if (clock < c.holdUntil) {
            want = pos;
        } else {
            carry = null;
            return;
        }
        Vec3 step = want.m_82546_(pos);
        self.m_20334_(step.f_82479_, step.f_82480_, step.f_82481_);
        self.f_19789_ = 0.0F;
    }

    @SubscribeEvent
    public static void steady(net.minecraftforge.client.event.MovementInputUpdateEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (carry == null || event.getEntity() != minecraft.f_91074_) {
            return;
        }
        net.minecraft.client.player.Input input = event.getInput();
        input.f_108566_ = 0.0F;
        input.f_108567_ = 0.0F;
        input.f_108572_ = false;
        input.f_108573_ = false;
    }

    // ------------------------------------------------------------------------------------------------------ Galaxy Impact's camera

    static final int CAMERA_FROM = IronMove.IMPACT_TOP + 2;
    static final int CAMERA_TO = IronMove.IMPACT_DESCEND;

    static boolean filming(Minecraft minecraft) {
        if (minecraft.f_91074_ == null) {
            return false;
        }
        double t = impacting(minecraft.f_91074_.m_19879_());
        return t >= CAMERA_FROM && t < CAMERA_TO;
    }

    /** Into the third person for the shot, and back to what it was after. */
    static void cinema(Minecraft minecraft) {
        boolean on = filming(minecraft);
        if (on && savedCamera == null) {
            savedCamera = minecraft.f_91066_.m_92176_();
            minecraft.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);
        } else if (!on && savedCamera != null) {
            minecraft.f_91066_.m_92157_(savedCamera);
            savedCamera = null;
        }
    }

    /**
     * client/dragon/DragonCamera: the camera's place during Galaxy Impact's hang: low under the body and off to its side, looking up at
     * it, slowly going round it; null otherwise.
     */
    public static Vec3 camera(Entity entity, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (entity != minecraft.f_91074_ || !filming(minecraft)) {
            return null;
        }
        double t = impacting(entity.m_19879_()) + partial;
        Vec3 body = entity.m_20318_(partial).m_82520_(0, 1.0, 0);
        double a = Math.toRadians(entity.m_146908_()) + 1.1 + (t - CAMERA_FROM) * 0.011;
        double near = Math.min(1.0, (t - CAMERA_FROM) / 10.0);
        double dist = 5.0 + 5.0 * near;
        return body.m_82520_(-Math.sin(a) * dist, -3.8 * near - 0.5, Math.cos(a) * dist);
    }

    @SubscribeEvent
    public static void filmAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (!filming(minecraft)) {
            return;
        }
        float partial = (float) event.getPartialTick();
        Vec3 cam = camera(minecraft.f_91074_, partial);
        if (cam == null) {
            return;
        }
        Vec3 to = minecraft.f_91074_.m_20318_(partial).m_82520_(0, 1.2, 0).m_82546_(cam);
        double flat = Math.sqrt(to.f_82479_ * to.f_82479_ + to.f_82481_ * to.f_82481_);
        event.setYaw((float) Math.toDegrees(Math.atan2(-to.f_82479_, to.f_82481_)));
        event.setPitch((float) -Math.toDegrees(Math.atan2(to.f_82480_, flat)));
    }

    // ------------------------------------------------------------------------------------------------------ the screen: dark, then white

    /** How dark the world goes round a Galaxy Impact charging near, and how white its blast lights it (0 to 1 each). */
    static double[] screen(Minecraft minecraft, float partial) {
        double dark = 0.0;
        double white = 0.0;
        if (minecraft.f_91074_ == null || minecraft.f_91073_ == null) {
            return new double[] {0, 0};
        }
        Vec3 eye = minecraft.f_91074_.m_20299_(partial);
        for (Map.Entry<Integer, Doing> e : DOING.entrySet()) {
            Doing d = e.getValue();
            if (d.kind != IronFist.IMPACT || d.at.m_82554_(eye) > 200.0) {
                continue;
            }
            double t = clock - d.born + partial;
            if (t > IronMove.IMPACT_TOP && t < IronMove.IMPACT_PUNCH + 2) {
                dark = Math.max(dark, 0.42 * IronMove.impactCharge(t));
            }
        }
        for (Strike s : STRIKES) {
            double a = clock - s.born + partial - (s.drop / IronMove.IMPACT_FALL + 4.0);
            double d = s.at.m_82554_(eye);
            if (a >= 0.0 && a < 14.0 && d < FAR_IMPACT) {
                white = Math.max(white, 0.9 * (1.0 - a / 14.0) * Math.max(0.2, 1.0 - d / FAR_IMPACT));
            }
        }
        return new double[] {dark, white};
    }

    static void overlay(net.minecraft.client.gui.GuiGraphics graphics, int width, int height, float partial) {
        if (DOING.isEmpty() && STRIKES.isEmpty()) {
            return;
        }
        double[] s = screen(Minecraft.m_91087_(), partial);
        if (s[0] > 0.01) {
            graphics.m_280509_(0, 0, width, height, (int) (s[0] * 255) << 24 | 0x0A0204);
        }
        if (s[1] > 0.01) {
            graphics.m_280509_(0, 0, width, height, (int) (s[1] * 255) << 24 | 0xFFF6DC);
        }
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Overlay {
        private Overlay() {
        }

        /** Under the HUD: the world dims and flashes, the hotbar stays readable. */
        @SubscribeEvent
        public static void registerIronOverlay(RegisterGuiOverlaysEvent event) {
            event.registerBelowAll("hexpiece_iron", (gui, graphics, partialTick, width, height) -> overlay(graphics, width, height, partialTick));
        }
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

    static boolean empty() {
        return POPS.isEmpty() && LOVES.isEmpty() && BALLS.isEmpty() && BOOMS.isEmpty() && CRUSHES.isEmpty() && RINGS.isEmpty() && HOLES.isEmpty()
            && DIVIDES.isEmpty() && BLASTS.isEmpty() && FINISHES.isEmpty() && STRIKES.isEmpty() && DEBRIS.isEmpty() && DOING.isEmpty();
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || empty()) {
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
            draw(minecraft, level, cam, partial);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: an Iron Fist move could not be drawn", failure);
            clear();
        }
        if (SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        SINK.buffer = null;
    }

    static boolean far(Vec3 at, Vec3 cam, double far) {
        return at.m_82557_(cam) > far * far;
    }

    static boolean ownView(Minecraft minecraft, Entity user) {
        return user == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON;
    }

    /** Where {@code user}'s right fist is this frame (the clip's own posed fist; in one's own first person low and right of the view). */
    static Vec3 fist(Minecraft minecraft, Entity user, float partial) {
        if (user instanceof net.minecraft.world.entity.player.Player player) {
            Vec3 at = com.hexhaki.piece.client.FirstPersonLight.fist(player, partial);
            if (at != null) {
                return at;
            }
        }
        Vec3 look = user.m_20252_(partial);
        Vec3 f = new Vec3(look.f_82479_, 0, look.f_82481_);
        f = f.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : f.m_82541_();
        Vec3 right = new Vec3(-f.f_82481_, 0, f.f_82479_);
        if (ownView(minecraft, user)) {
            return user.m_20299_(partial).m_82549_(look.m_82490_(1.2)).m_82549_(right.m_82490_(0.5)).m_82520_(0, -0.45, 0);
        }
        return user.m_20318_(partial).m_82520_(0, user.m_20206_() * 0.62, 0).m_82549_(right.m_82490_(0.4)).m_82549_(f.m_82490_(0.45));
    }

    private static final double[] T = new double[4];

    static void draw(Minecraft minecraft, ClientLevel level, Vec3 cam, float partial) {
        double cx = cam.f_82479_;
        double cy = cam.f_82480_;
        double cz = cam.f_82481_;
        double now = clock + partial;
        for (Pop p : POPS) {
            if (!far(p.at, cam, FAR)) {
                double age = now - p.born;
                IronArt.pressure(SINK, p.at.f_82479_ - cx, p.at.f_82480_ - cy, p.at.f_82481_ - cz, p.way.f_82479_, p.way.f_82480_, p.way.f_82481_,
                    p.landed ? 0.9 : 0.5, age, 6.0);
                if (p.landed) {
                    IronArt.impactLines(SINK, p.at.f_82479_ - cx, p.at.f_82480_ - cy, p.at.f_82481_ - cz, p.way.f_82479_, p.way.f_82480_,
                        p.way.f_82481_, 1.2, age, 5.0, (int) p.born);
                }
            }
        }
        for (Love l : LOVES) {
            if (far(l.at, cam, FAR)) {
                continue;
            }
            double age = now - l.born;
            double x = l.at.f_82479_ - cx;
            double y = l.at.f_82480_ - cy;
            double z = l.at.f_82481_ - cz;
            IronArt.pressure(SINK, x, y, z, 0, 1, 0, l.landed ? 1.7 : 1.0, age, 10.0);
            if (l.landed) {
                IronArt.impactLines(SINK, x, y, z, 0, 1, 0, 2.0, age, 8.0, l.seed);
                if (age < 6.0) {
                    IronArt.sparks(SINK, x, y, z, 0.8, age, l.seed);
                }
            }
        }
        for (Map.Entry<Integer, Doing> e : DOING.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            if (user == null || far(user.m_20182_(), cam, FAR_IMPACT)) {
                continue;
            }
            doing(minecraft, user, e.getValue(), now, partial, cx, cy, cz);
        }
        for (Ball b : BALLS.values()) {
            double age = now - b.born;
            Vec3 p = b.from.m_82549_(b.step.m_82490_(age));
            if (far(p, cam, FAR)) {
                continue;
            }
            Vec3 u = unit(b.step);
            IronArt.cannonball(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, b.radius, age * 0.9, age, b.seed);
            IronArt.trail(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, u.f_82479_, u.f_82480_, u.f_82481_, b.step.m_82553_(), b.radius, age,
                b.seed);
        }
        for (Boom b : BOOMS) {
            if (far(b.at, cam, FAR)) {
                continue;
            }
            double age = now - b.born;
            double x = b.at.f_82479_ - cx;
            double y = b.at.f_82480_ - cy;
            double z = b.at.f_82481_ - cz;
            GuraArt.quake(SINK, x, y, z, b.radius * 1.3, 0.6 + 0.4 * b.charge, age, b.seed);
            IronArt.pressure(SINK, x, y, z, 0, 1, 0, b.radius * 1.8, age, 10.0);
            IronArt.pressure(SINK, x, y, z, b.way.f_82479_, b.way.f_82480_, b.way.f_82481_, b.radius * 1.2, age - 1.0, 8.0);
            if (age < 6.0) {
                FireMesh.shell(SINK, x, y, z, b.radius * (0.5 + age * 0.15), IronArt.WHITE, 0.8 * (1.0 - age / 6.0), 0.3 * (1.0 - age / 6.0));
            }
        }
        for (Crush c : CRUSHES) {
            if (far(c.at, cam, FAR)) {
                continue;
            }
            double age = now - c.born;
            double x = c.at.f_82479_ - cx;
            double y = c.at.f_82480_ - cy;
            double z = c.at.f_82481_ - cz;
            GuraArt.shock(SINK, x, y, z, c.way.f_82479_, c.way.f_82480_, c.way.f_82481_, 11.0 * c.size, 0.42, 1.0, 3.2, age, c.seed);
            IronArt.impactLines(SINK, x, y, z, c.way.f_82479_, c.way.f_82480_, c.way.f_82481_, 4.5, age, 9.0, c.seed);
            IronArt.pressure(SINK, x, y, z, c.way.f_82479_, c.way.f_82480_, c.way.f_82481_, 2.4, age, 8.0);
        }
        for (Ring r : RINGS) {
            if (far(r.at, cam, FAR)) {
                continue;
            }
            double age = now - r.born;
            double u = age / r.life;
            double x = r.at.f_82479_ - cx;
            double y = r.at.f_82480_ - cy;
            double z = r.at.f_82481_ - cz;
            IronArt.groundRing(SINK, x, y, z, 0.5 + r.radius * (1.0 - (1.0 - u) * (1.0 - u)), 0.75 * (1.0 - u));
            IronArt.dustWall(SINK, x, y, z, 0.6 + r.radius * 0.6 * u, 1.3, 0.55 * (1.0 - u), now, r.seed);
        }
        for (Hole h : HOLES) {
            if (far(h.at, cam, FAR)) {
                continue;
            }
            double age = now - h.born;
            double x = h.at.f_82479_ - cx;
            double y = h.at.f_82480_ - cy;
            double z = h.at.f_82481_ - cz;
            GuraArt.quake(SINK, x, y, z, 10.0, 1.0, age, h.seed);
            double u = Math.min(1.0, age / 16.0);
            IronArt.groundRing(SINK, x, y, z, 1.0 + 15.0 * (1.0 - (1.0 - u) * (1.0 - u)), 0.8 * (1.0 - u));
            IronArt.dustWall(SINK, x, y, z, 1.2 + 3.0 * u, 2.6, 0.7 * (1.0 - age / GuraArt.QUAKE_LIFE), now, h.seed);
        }
        for (Divide d : DIVIDES) {
            divide(d, now, cam, cx, cy, cz);
        }
        for (Blast b : BLASTS) {
            if (far(b.at, cam, FAR)) {
                continue;
            }
            double age = now - b.born;
            double x = b.at.f_82479_ - cx;
            double y = b.at.f_82480_ - cy;
            double z = b.at.f_82481_ - cz;
            IronArt.pressure(SINK, x, y, z, b.way.f_82479_, b.way.f_82480_, b.way.f_82481_, 1.5, age, 6.0);
            if (age < 3.0) {
                FireMesh.shell(SINK, x, y, z, 0.25 + age * 0.12, IronArt.GOLD, 0.45 * (1.0 - age / 3.0), 0.5 * (1.0 - age / 3.0));
            }
            if (b.k % 3 == 0) {
                GuraArt.shock(SINK, x, y, z, b.way.f_82479_, b.way.f_82480_, b.way.f_82481_, 5.0, 0.3, 0.5, 2.5, age, b.k);
            }
        }
        for (Finish f : FINISHES) {
            if (far(f.at, cam, FAR)) {
                continue;
            }
            double age = now - f.born;
            double x = f.at.f_82479_ - cx;
            double y = f.at.f_82480_ - cy;
            double z = f.at.f_82481_ - cz;
            GuraArt.shock(SINK, x, y, z, f.way.f_82479_, f.way.f_82480_, f.way.f_82481_, 15.0, 0.5, 1.0, 2.8, age, f.seed);
            IronArt.impactLines(SINK, x, y, z, f.way.f_82479_, f.way.f_82480_, f.way.f_82481_, 5.0, age, 10.0, f.seed);
            if (age < 8.0) {
                IronArt.fistGlow(SINK, x, y, z, 1.0 - age / 8.0, now, f.seed);
            }
            Vec3 g = f.at.m_82549_(f.way.m_82490_(4.5)).m_82520_(0, -1.2, 0);
            GuraArt.quake(SINK, g.f_82479_ - cx, g.f_82480_ - cy, g.f_82481_ - cz, 9.0, 1.0, age, f.seed + 3);
        }
        for (Strike s : STRIKES) {
            strike(s, now, cam, cx, cy, cz);
        }
        for (Debris d : DEBRIS) {
            if (far(d.at, cam, FAR_IMPACT)) {
                continue;
            }
            double age = now - d.born;
            double alpha = Math.min(1.0, (50.0 - age) / 8.0);
            for (int k = 0; k < d.count; k++) {
                IronArt.thrown(k, d.seed, d.reach, d.lift, age, T);
                if (T[1] < -3.0) {
                    continue;
                }
                IronArt.chunk(SINK, d.at.f_82479_ + T[0] - cx, d.at.f_82480_ + T[1] - cy, d.at.f_82481_ + T[2] - cz,
                    0.3 + 0.55 * FireMesh.rand(d.seed, k + 240), T[3], d.seed + k, alpha);
            }
        }
    }

    /** A user in the middle of a held or long move: the cannonball in hand, the coil, the grab, the barrage, Galaxy Impact. */
    static void doing(Minecraft minecraft, Entity user, Doing d, double now, float partial, double cx, double cy, double cz) {
        double age = now - d.born;
        Vec3 fist = fist(minecraft, user, partial);
        double fx = fist.f_82479_ - cx;
        double fy = fist.f_82480_ - cy;
        double fz = fist.f_82481_ - cz;
        switch (d.kind) {
            case IronFist.METEOR -> {
                double charge = IronMove.meteorCharge(age);
                double r = IronMove.meteorRadius(charge);
                IronArt.cannonball(SINK, fx, fy + r * 0.6, fz, r, age * 0.05, age, d.seed);
            }
            case IronFist.CRUSHER -> {
                double power = Math.min(1.0, age / Math.max(1, d.ticks));
                IronArt.gather(SINK, fx, fy, fz, 1.6, power, now, d.seed);
                IronArt.sparks(SINK, fx, fy, fz, 0.6 + 0.3 * power, age, d.seed);
            }
            case IronFist.HOLE -> {
                IronArt.sparks(SINK, fx, fy, fz, 0.9, age, d.seed);
                IronArt.fistGlow(SINK, fx, fy, fz, 0.25, now, d.seed);
            }
            case IronFist.INF -> {
                if (age > IronMove.INF_STANCE && age < IronMove.INF_END) {
                    double speed = (age - IronMove.INF_STANCE) / (IronMove.INF_END - IronMove.INF_STANCE);
                    Vec3 look = user.m_20252_(partial);
                    Vec3 f = new Vec3(look.f_82479_, 0, look.f_82481_);
                    f = f.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : f.m_82541_();
                    Vec3 chest = user.m_20318_(partial).m_82520_(0, user.m_20206_() * 0.7, 0);
                    IronArt.afterimages(SINK, chest.f_82479_ - cx, chest.f_82480_ - cy, chest.f_82481_ - cz, f.f_82479_, f.f_82481_, speed, now, d.seed);
                } else if (age >= IronMove.INF_END && age < IronMove.INF_FINISH) {
                    IronArt.fistGlow(SINK, fx, fy, fz, (age - IronMove.INF_END) / (IronMove.INF_FINISH - IronMove.INF_END), now, d.seed);
                    IronArt.gather(SINK, fx, fy, fz, 2.4, 1.0, now, d.seed);
                }
            }
            case IronFist.IMPACT -> impact(minecraft, user, d, age, now, partial, fx, fy, fz, cx, cy, cz);
            default -> {
            }
        }
    }

    /** Galaxy Impact on its user: the crouch on the cracking ground, the leap, the trails up, the hang with the fist charging. */
    static void impact(Minecraft minecraft, Entity user, Doing d, double t, double now, float partial, double fx, double fy, double fz, double cx,
                       double cy, double cz) {
        double x = d.at.f_82479_ - cx;
        double y = d.at.f_82480_ - cy;
        double z = d.at.f_82481_ - cz;
        if (t < IronMove.IMPACT_JUMP) {
            GuraArt.tremor(SINK, x, y, z, 2.5 + t * 0.05, t, d.seed);
            IronArt.dustWall(SINK, x, y, z, 1.2 + t * 0.04, 0.6 + t * 0.03, 0.35, now, d.seed);
            for (int k = 0; k < 8; k++) {
                double a = 2 * Math.PI * k / 8 + FireMesh.rand(d.seed, k);
                double r = 1.5 + 2.5 * FireMesh.rand(d.seed, k + 9);
                double jitter = 0.05 * Math.sin(now * 3.0 + k) * t / IronMove.IMPACT_JUMP;
                IronArt.chunk(SINK, x + Math.cos(a) * r + jitter, y + 0.15 + Math.abs(jitter) * 2.0, z + Math.sin(a) * r, 0.18, now * 0.1, d.seed + k, 1.0);
            }
        } else if (t < IronMove.IMPACT_JUMP + 24) {
            double a = t - IronMove.IMPACT_JUMP;
            double u = Math.min(1.0, a / 18.0);
            IronArt.groundRing(SINK, x, y, z, 1.0 + 30.0 * (1.0 - (1.0 - u) * (1.0 - u)), 0.85 * (1.0 - u));
            IronArt.dustWall(SINK, x, y, z, 2.0 + 18.0 * u, 3.0, 0.6 * (1.0 - u), now, d.seed);
            GuraArt.quake(SINK, x, y, z, 9.0, 1.0, a, d.seed);
        }
        if (t >= IronMove.IMPACT_JUMP && t < IronMove.IMPACT_TOP + 4) {
            Vec3 body = user.m_20318_(partial).m_82520_(0, 1.0, 0);
            double speed = Math.max(0.3, IronMove.impactRise(t + 1) - IronMove.impactRise(t));
            IronArt.trail(SINK, body.f_82479_ - cx, body.f_82480_ - cy, body.f_82481_ - cz, 0, 1, 0, speed, 0.55, t, d.seed);
        }
        if (t >= IronMove.IMPACT_TOP && t < IronMove.IMPACT_PUNCH + 2) {
            double power = IronMove.impactCharge(t);
            IronArt.fistGlow(SINK, fx, fy, fz, power, now, d.seed);
            IronArt.gather(SINK, fx, fy, fz, 3.0 + 6.0 * power, Math.min(1.0, 0.4 + power), now, d.seed);
            // below: dust rising off the ground, small rocks lifting and trembling
            double rise = Math.min(1.0, (t - IronMove.IMPACT_TOP) / 30.0);
            IronArt.dustWall(SINK, x, y, z, 6.0 + 10.0 * rise, 1.0 + 2.5 * rise, 0.3 * rise, now, d.seed + 1);
            for (int k = 0; k < 14; k++) {
                double a = 2 * Math.PI * FireMesh.rand(d.seed, k + 30);
                double r = 3.0 + 16.0 * FireMesh.rand(d.seed, k + 60);
                double lift = rise * (0.6 + 1.6 * FireMesh.rand(d.seed, k + 90)) + 0.08 * Math.sin(now * 2.7 + k);
                IronArt.chunk(SINK, x + Math.cos(a) * r, y + 0.3 + lift, z + Math.sin(a) * r, 0.25 + 0.25 * FireMesh.rand(d.seed, k), now * 0.05 + k,
                    d.seed + k, 1.0);
            }
        }
    }

    static void divide(Divide d, double now, Vec3 cam, double cx, double cy, double cz) {
        double age = now - d.born;
        double front = Math.min(d.length, age * IronMove.DIVIDE_SPEED);
        Vec3 at = d.base.m_82549_(d.way.m_82490_(front));
        if (far(at, cam, FAR * 1.5)) {
            return;
        }
        double fade = age <= d.run ? 1.0 : Math.max(0.0, 1.0 - (age - d.run) / 6.0);
        IronArt.shockSheet(SINK, at.f_82479_ - cx, at.f_82480_ - 0.5 - cy, at.f_82481_ - cz, d.way.f_82479_, d.way.f_82481_, IronMove.DIVIDE_HEIGHT,
            IronMove.DIVIDE_WIDTH, fade, now, d.seed);
        // the fracture's dust along the line behind the front, rising and thinning
        FireMesh.palette(FireMesh.SMOKE);
        for (double s = 2.0; s < front; s += 4.0) {
            double since = age - s / IronMove.DIVIDE_SPEED;
            double alpha = 0.5 * Math.max(0.0, 1.0 - since / 50.0);
            if (alpha < 0.02) {
                continue;
            }
            Vec3 p = d.base.m_82549_(d.way.m_82490_(s));
            int seed = d.seed + (int) s;
            FireMesh.puff(SINK, p.f_82479_ - cx, p.f_82480_ + 1.0 + since * 0.05 - cy, p.f_82481_ - cz, 2.2 + since * 0.03, 0, 0.1, 0, 1.1, 0.25,
                0.7, 0.7, false, alpha, now, seed);
        }
        FireMesh.palette(FireMesh.BAND);
    }

    /** Galaxy Impact's punch: the force driven down from the fist, the point where it lands, the fractures, the dome, the rings, the dust. */
    static void strike(Strike s, double now, Vec3 cam, double cx, double cy, double cz) {
        if (far(s.at, cam, FAR_IMPACT + IronMove.IMPACT_DOME)) {
            return;
        }
        double age = now - s.born;
        double land = s.drop / IronMove.IMPACT_FALL;
        double x = s.at.f_82479_ - cx;
        double y = s.at.f_82480_ - cy;
        double z = s.at.f_82481_ - cz;
        Vec3 fist = s.at.m_82546_(s.down.m_82490_(s.drop));
        if (age < land + 6.0) {
            GuraArt.shock(SINK, fist.f_82479_ - cx, fist.f_82480_ - cy, fist.f_82481_ - cz, s.down.f_82479_, s.down.f_82480_, s.down.f_82481_, s.drop + 2.0,
                0.4, 1.0, IronMove.IMPACT_FALL, age, s.seed);
            double head = Math.min(s.drop, age * IronMove.IMPACT_FALL);
            Vec3 p = fist.m_82549_(s.down.m_82490_(head));
            FireMesh.palette(FireMesh.LIGHT);
            FireMesh.puff(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, 2.2, s.down.f_82479_, s.down.f_82480_, s.down.f_82481_, 1.6, 0.3,
                0.95, 0.6, false, 0.8, now, s.seed);
            FireMesh.palette(FireMesh.BAND);
        }
        double a = age - land;
        if (a >= 0.0 && a < 8.0) {
            IronArt.point(SINK, x, y + 0.5, z, 1.2 + a * 0.3, 1.0 - Math.max(0.0, a - 4.0) / 4.0, now, s.seed);
            // the ground drawn in toward the point for a moment
            double pull = Math.min(1.0, a / 4.0);
            IronArt.groundRing(SINK, x, y, z, 10.0 * (1.0 - pull) + 0.5, 0.6 * (1.0 - pull));
        }
        if (a >= 0.0) {
            GuraArt.quake(SINK, x, y, z, IronMove.IMPACT_DOME, 1.0, a * 0.8, s.seed);
        }
        double b = a - 4.0;
        if (b < 0.0) {
            return;
        }
        double u = Math.min(1.0, b / IronMove.IMPACT_DOME_TICKS);
        double grow = 1.0 - (1.0 - u) * (1.0 - u);
        double domeFade = b <= IronMove.IMPACT_DOME_TICKS ? 1.0 : Math.max(0.0, 1.0 - (b - IronMove.IMPACT_DOME_TICKS) / 18.0);
        IronArt.dome(SINK, x, y, z, IronMove.IMPACT_DOME * grow, domeFade, now, s.seed);
        // the rings over the ground at their own speeds, the fastest far beyond the dome
        double[] reach = {IronMove.IMPACT_DOME * 1.05, 90.0, 140.0};
        double[] ticks = {IronMove.IMPACT_DOME_TICKS, 26.0, 22.0};
        for (int k = 0; k < 3; k++) {
            double v = b / ticks[k];
            if (v < 1.0) {
                IronArt.groundRing(SINK, x, y, z, reach[k] * (1.0 - (1.0 - v) * (1.0 - v)), 0.8 * (1.0 - v));
            }
        }
        // the wall of dust following the dome out, then lingering over the battlefield
        double dustAlpha = Math.min(0.75, b / 6.0) * Math.max(0.0, 1.0 - (b - 60.0) / 90.0);
        IronArt.dustWall(SINK, x, y, z, IronMove.IMPACT_DOME * grow * 0.96, 6.0 + 8.0 * grow, dustAlpha, now, s.seed);
        if (b > 20.0) {
            IronArt.dustWall(SINK, x, y, z, IronMove.IMPACT_DOME * 0.55, 10.0, dustAlpha * 0.8, now, s.seed + 5);
            IronArt.dustWall(SINK, x, y, z, IronMove.IMPACT_CRATER * 0.8, 8.0, dustAlpha * 0.7, now, s.seed + 9);
        }
    }

    static void clear() {
        POPS.clear();
        LOVES.clear();
        BALLS.clear();
        BOOMS.clear();
        CRUSHES.clear();
        RINGS.clear();
        HOLES.clear();
        DIVIDES.clear();
        BLASTS.clear();
        FINISHES.clear();
        STRIKES.clear();
        DEBRIS.clear();
        DOING.clear();
        carry = null;
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        if (savedCamera != null) {
            Minecraft.m_91087_().f_91066_.m_92157_(savedCamera);
            savedCamera = null;
        }
        clear();
    }
}
