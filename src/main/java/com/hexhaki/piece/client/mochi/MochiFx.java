package com.hexhaki.piece.client.mochi;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.mochi.MochiDraw;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.MochiMoves;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.5.5: the Mochi Mochi no Mi as every client sees it (server/MochiMoves sends S2CUoFx kinds 256 to 276; client/dragon/UoFx hands them
 * here), drawn once a frame after the particles through the game's own debugQuads type (alpha blended, depth tested), eye-centred
 * (mochi/MochiDraw, mochi/MochiArt: the house's banded solids, as the Light, Gura and Magma fruits'). The arms leave the user's
 * shoulders as they are now; with Armament on, every fist and its arm are black (the owner's rule). One's own Zan Giri wheel is carried
 * here (as Supa's dash: the user's own client moves the body), the body hidden while it rolls.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class MochiFx {
    private static long clock;
    private static boolean kitAdded;

    record Punch(int user, int which, double reach, long born, boolean coat, int seed) {
    }

    record Splat(Vec3 at, Vec3 way, double size, long born, int seed) {
    }

    record Boom(Vec3 at, double size, long born, int seed) {
    }

    /** Yaki Mochi's forearm swelling (by user). */
    record Swell(long born, long until, boolean coat) {
    }

    record Fist(Vec3 from, Vec3 step, long born, int flight, boolean coat, int seed, double charge) {
    }

    /**
     * 3.6.3: Buzz Cut Mochi (by user): the roll (its way so far, a point about every block, and when it passed each), then the cinematic
     * from the catch (where the user stood, the yaw they faced, the ones stuck to the ball).
     */
    static final class Buzz {
        final long born;
        final int ticks;
        final int seed;
        long caught = -1;
        long missed = -1;
        Vec3 feet;
        float yaw0;
        final java.util.Set<Integer> victims = new java.util.HashSet<>();
        final double[] path = new double[3 * MochiDraw.BUZZ_PATH_MOST];
        final double[] passed = new double[MochiDraw.BUZZ_PATH_MOST];
        int count;
        double rolled;
        Vec3 last;

        Buzz(long born, int ticks, int seed) {
            this.born = born;
            this.ticks = ticks;
            this.seed = seed;
        }

        boolean rolling(long clock) {
            return this.caught < 0 && this.missed < 0 || this.caught >= 0 && clock - this.caught < 2 || this.missed >= 0 && clock - this.missed < 2;
        }

        double since(long clock) {
            return this.caught < 0 ? -1.0 : clock - this.caught;
        }
    }

    /** 3.6.3: Buzz Cut Mochi's blow (the ball where it struck) and its slam's ground. */
    record BuzzMark(Vec3 at, long born, int seed, boolean ground) {
    }

    record Stuck(Vec3 at, double height, long born, int ticks) {
    }

    /** 3.6.3: the reference's moves. */
    record Muddle(Vec3 at, Vec3 way, double length, long born, int ticks, int seed) {
    }

    /** 3.6.3: Lotus Mochi Combo's donut (by its user), the one its fist hit once known (-1 until then), and where they stood. */
    static final class Lotus {
        final int user;
        final long born;
        final boolean coat;
        final int seed;
        int body = -1;
        double ground = Double.NaN;

        Lotus(int user, long born, boolean coat, int seed) {
            this.user = user;
            this.born = born;
            this.coat = coat;
            this.seed = seed;
        }
    }

    /** 3.6.3: the one the Lotus fist hit, thrown up from {@code ground} (feet) at {@code born}. */
    record LotusRise(int body, Vec3 ground, long born, int seed) {
    }

    /** 3.6.3: a Lotus blow in the air on {@code body} from {@code side}. */
    record LotusAir(int body, Vec3 side, long born, boolean coat, int seed) {
    }

    /** 3.6.3: the giant fist's donut {@code top} over {@code body}, the ground under them. */
    record LotusSlam(int body, Vec3 top, double ground, long born, boolean coat, int seed) {
    }

    /** 3.6.3: where the Lotus slam drove them into the ground. */
    record LotusGround(Vec3 at, long born, int seed) {
    }

    /** Chestnut Mochi's donut (by user): out until, its last spike. */
    static final class Chestnut {
        final long born;
        final long until;
        final int seed;
        final boolean coat;
        long ended = -1;
        long fired = -100;

        Chestnut(long born, long until, int seed, boolean coat) {
            this.born = born;
            this.until = until;
            this.seed = seed;
            this.coat = coat;
        }
    }

    record Spike(int user, Vec3 at, long born, int seed) {
    }

    /** Mochi Anemone held (by user): its area, until when; ended once let go. */
    static final class Area {
        final Vec3 at;
        final double radius;
        final long born;
        final long until;
        final boolean coat;
        final int seed;
        long ended = -1;

        Area(Vec3 at, double radius, long born, long until, boolean coat, int seed) {
            this.at = at;
            this.radius = radius;
            this.born = born;
            this.until = until;
            this.coat = coat;
            this.seed = seed;
        }
    }

    record Strike(int user, int donut, Vec3 at, long born) {
    }

    static final int MOST = 64;
    static final double FAR = 160.0;
    private static final List<Punch> PUNCHES = new ArrayList<>();
    private static final List<Splat> SPLATS = new ArrayList<>();
    private static final List<Boom> BOOMS = new ArrayList<>();
    static final Map<Integer, Swell> SWELLS = new HashMap<>();
    static final Map<Integer, Fist> FISTS = new HashMap<>();
    static final Map<Integer, Buzz> WHEELS = new HashMap<>();
    private static final List<BuzzMark> MARKS = new ArrayList<>();
    static final Map<Integer, Stuck> STUCKS = new HashMap<>();
    private static final List<Muddle> MUDDLES = new ArrayList<>();
    private static final List<Lotus> LOTUSES = new ArrayList<>();
    private static final List<LotusSlam> SLAMS = new ArrayList<>();
    private static final List<LotusRise> RISES = new ArrayList<>();
    private static final List<LotusAir> AIRS = new ArrayList<>();
    private static final List<LotusGround> GROUNDS = new ArrayList<>();
    static final Map<Integer, Chestnut> CHESTNUTS = new HashMap<>();
    private static final List<Spike> SPIKES = new ArrayList<>();
    static final Map<Integer, Area> AREAS = new HashMap<>();
    private static final List<Strike> STRIKES = new ArrayList<>();
    /** How long the Anemone's rings and the Awakening's ground take to shrink away once ended. */
    /** 3.6.3: how long a donut takes to form and to go (ticks). */
    static final double FORM = 6.0;

    private MochiFx() {
    }

    static <T> void add(List<T> list, T value) {
        if (list.size() >= MOST) {
            list.remove(0);
        }
        list.add(value);
    }

    static <T> void put(Map<Integer, T> map, int key, T value) {
        if (map.size() < MOST || map.containsKey(key)) {
            map.put(key, value);
        }
    }

    /** client/mochi/MochiClient: whether {@code user}'s Awakening is on (the panel shows the awakened moves). */
    static boolean awakened(int user) {
        return false; // 3.6.3: the awakened set (the reference's DoughV2) comes in the next version
    }

    public static void accept(S2CUoFx m) {
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        Vec3 a = new Vec3(m.ax(), m.ay(), m.az());
        boolean coat = m.bz() > 0.5F;
        int phase = Math.round(m.bx());
        switch (m.kind()) {
            case MochiMoves.PUNCH -> add(PUNCHES, new Punch(m.user(), phase, m.size(), clock, coat, m.id() + m.user()));
            case MochiMoves.YAKI -> {
                if (phase == MochiMoves.YAKI_SWELL) {
                    put(SWELLS, m.user(), new Swell(clock, clock + m.ticks() + 2, coat));
                } else if (phase == MochiMoves.YAKI_FLY) {
                    SWELLS.remove(m.user());
                    put(FISTS, m.id(), new Fist(at, a, clock, m.ticks(), coat, m.id(), m.by()));
                } else {
                    SWELLS.remove(m.user());
                    FISTS.remove(m.id());
                }
            }
            case MochiMoves.WHEEL -> buzz(m, at, phase);
            case MochiMoves.STUCK -> {
                if (m.ticks() > 0) {
                    put(STUCKS, m.id(), new Stuck(at, m.size(), clock, m.ticks()));
                } else {
                    STUCKS.remove(m.id());
                }
            }
            case MochiMoves.SPLAT -> add(SPLATS, new Splat(at, unit(a), m.size(), clock, m.id()));
            case MochiMoves.BOOM -> add(BOOMS, new Boom(at, m.size(), clock, m.id()));
            case MochiMoves.MUDDLE -> add(MUDDLES, new Muddle(at, flat(a), m.size(), clock, m.ticks(), m.id()));
            case MochiMoves.LOTUS -> lotus(m, at, a, phase, coat);
            case MochiMoves.CHESTNUT -> {
                if (phase == 1) {
                    put(CHESTNUTS, m.user(), new Chestnut(clock, clock + m.ticks(), m.id(), coat));
                } else {
                    Chestnut c = CHESTNUTS.get(m.user());
                    if (c != null && c.ended < 0) {
                        c.ended = clock;
                    }
                }
            }
            case MochiMoves.CHESTNUT_SPIKE -> {
                add(SPIKES, new Spike(m.user(), at, clock, m.id()));
                Chestnut c = CHESTNUTS.get(m.user());
                if (c != null) {
                    c.fired = clock;
                }
            }
            case MochiMoves.ANEMONE -> {
                if (phase == 1) {
                    put(AREAS, m.user(), new Area(at, m.size(), clock, clock + m.ticks(), coat, m.id()));
                } else {
                    Area n = AREAS.get(m.user());
                    if (n != null && n.ended < 0) {
                        n.ended = clock;
                    }
                }
            }
            case MochiMoves.ANEMONE_PUNCH -> add(STRIKES, new Strike(m.user(), m.id(), at, clock));
            default -> {
            }
        }
    }

    static boolean buzzDone(Buzz b) {
        return b.caught >= 0 ? clock - b.caught > MochiDraw.BUZZ_END + 2 : b.missed >= 0 ? clock - b.missed > MochiDraw.BUZZ_TILES
            : clock - b.born > b.ticks + MochiDraw.BUZZ_TILES;
    }

    /**
     * 3.6.3: each tick of Buzz Cut Mochi: the wheel's way recorded (the torn ground behind it), one's own body turned with the whirling
     * arm and kept still through the cinematic, the cutscene's third person on and off.
     */
    static void buzzTick(ClientLevel level) {
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        for (Map.Entry<Integer, Buzz> e : WHEELS.entrySet()) {
            Buzz b = e.getValue();
            Entity user = level.m_6815_(e.getKey());
            if (user == null) {
                continue;
            }
            if (b.caught < 0 && b.missed < 0) {
                Vec3 p = user.m_20182_();
                if (b.last != null) {
                    b.rolled += Math.hypot(p.f_82479_ - b.last.f_82479_, p.f_82481_ - b.last.f_82481_);
                }
                b.last = p;
                int n = b.count;
                double gap = n == 0 ? 99.0 : Math.hypot(p.f_82479_ - b.path[(n - 1) * 3], p.f_82481_ - b.path[(n - 1) * 3 + 2]);
                if (gap >= MochiDraw.BUZZ_TILE_GAP && n < b.passed.length && clock - b.born > 2) {
                    b.path[n * 3] = p.f_82479_;
                    b.path[n * 3 + 1] = p.f_82480_;
                    b.path[n * 3 + 2] = p.f_82481_;
                    b.passed[n] = clock;
                    b.count = n + 1;
                }
            }
            if (user == self && b.caught >= 0 && clock - b.caught < MochiDraw.BUZZ_END) {
                // one's own body turns with the arm (the whirl is the body's own turn), held still where it caught
                double[] arm = MochiDraw.buzzArm(clock - b.caught, 0, 0, 0, b.yaw0, new double[8]);
                self.m_146922_((float) arm[7]);
                self.m_20334_(0.0, Math.min(0.0, self.m_20184_().f_82480_), 0.0);
            }
        }
        cinema(Minecraft.m_91087_());
    }

    // ------------------------------------------------------------------------------------------------------ 3.6.3: the cutscene camera

    private static net.minecraft.client.CameraType savedCamera;

    /** The Buzz Cut cinematic one's own player is in (as its user or one caught), or null. */
    static Buzz filmed() {
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        if (self == null || WHEELS.isEmpty()) {
            return null;
        }
        for (Map.Entry<Integer, Buzz> e : WHEELS.entrySet()) {
            Buzz b = e.getValue();
            if (b.caught >= 0 && clock - b.caught < MochiDraw.BUZZ_END && (e.getKey() == self.m_19879_() || b.victims.contains(self.m_19879_()))) {
                return b;
            }
        }
        return null;
    }

    static boolean filming() {
        return filmed() != null;
    }

    /** Into the third person for the cutscene (the owner: "This move is a cutscene for both parties"), back to what it was after. */
    static void cinema(Minecraft minecraft) {
        boolean on = filming();
        if (on && savedCamera == null) {
            savedCamera = minecraft.f_91066_.m_92176_();
            minecraft.f_91066_.m_92157_(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
        } else if (!on && savedCamera != null) {
            minecraft.f_91066_.m_92157_(savedCamera);
            savedCamera = null;
        }
    }

    /**
     * Where the cutscene's camera is (client/dragon/DragonCamera.position asks), or null; cuts between shots as the reference does:
     * low at the user's left as the arm swings round into the one caught, high over them looking down through the whirl (the
     * reference's view of the vortex, frames 100 to 140), low in front looking up the arm to the raised ball (the show's frames 120 to
     * 140), far off to the side for the slam (clear of the rubble it heaves up), pulled back high over the crater after.
     */
    public static Vec3 camera(Entity entity, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        Buzz b = filmed();
        if (b == null || entity != minecraft.f_91074_ || b.feet == null) {
            return null;
        }
        double[] at = MochiDraw.buzzCamera(clock - b.caught + partial, b.feet.f_82479_, b.feet.f_82480_, b.feet.f_82481_, b.yaw0, CAM);
        return new Vec3(at[0], at[1], at[2]);
    }

    /** The camera's scratch (the render thread only). */
    private static final double[] CAM = new double[6];

    /** The cutscene's camera turned to what it films: the user and the swing, the whirl, the raised arm, the slam, the crater. */
    @SubscribeEvent
    public static void filmAngles(net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.m_91087_();
        Buzz b = filmed();
        if (b == null || minecraft.f_91074_ == null || b.feet == null) {
            return;
        }
        double t = clock - b.caught + event.getPartialTick();
        double[] at = MochiDraw.buzzCamera(t, b.feet.f_82479_, b.feet.f_82480_, b.feet.f_82481_, b.yaw0, CAM);
        double dx = at[3] - at[0];
        double dy = at[4] - at[1];
        double dz = at[5] - at[2];
        double flat = Math.sqrt(dx * dx + dz * dz);
        event.setYaw((float) Math.toDegrees(Math.atan2(-dx, dz)));
        event.setPitch((float) -Math.toDegrees(Math.atan2(dy, flat)));
    }

    /** 3.6.3: a Buzz Cut Mochi message (its phases: server/MochiMoves.BUZZ_*). */
    static void buzz(S2CUoFx m, Vec3 at, int phase) {
        Buzz b = WHEELS.get(m.user());
        switch (phase) {
            case MochiMoves.BUZZ_ROLLING -> {
                Buzz rolling = new Buzz(clock, m.ticks(), m.id());
                put(WHEELS, m.user(), rolling);
                // the wheel's rumble only while it rolls (faded out the moment it catches or stops: never outlasting the roll)
                net.minecraft.client.multiplayer.ClientLevel level = Minecraft.m_91087_().f_91073_;
                Entity roller = level == null ? null : level.m_6815_(m.user());
                com.hexhaki.piece.client.LoopSound.play(com.hexhaki.piece.mochi.MochiSounds.ROLL, roller, 1.0F, 1.2F, true, false, 0.6,
                    () -> rolling.caught < 0 && rolling.missed < 0 && clock - rolling.born < rolling.ticks + 2 && WHEELS.get(m.user()) == rolling);
                if (self(m.user())) {
                    carry = new Carry(m.ticks());
                }
            }
            case MochiMoves.BUZZ_MISSED -> {
                if (b != null) {
                    b.missed = clock;
                }
                if (self(m.user())) {
                    stopCarry();
                }
            }
            case MochiMoves.BUZZ_CAUGHT -> {
                if (b != null) {
                    b.caught = clock;
                    b.feet = at;
                    b.yaw0 = m.by();
                    b.victims.add(m.id());
                }
                if (self(m.user())) {
                    stopCarry();
                }
            }
            case MochiMoves.BUZZ_STUCK -> {
                if (b != null) {
                    b.victims.add(m.id());
                }
            }
            case MochiMoves.BUZZ_BLOW, MochiMoves.BUZZ_GROUND -> {
                add(MARKS, new BuzzMark(at, clock, m.id(), phase == MochiMoves.BUZZ_GROUND));
                // the owner: "a violent black-and-white impact frame" at the blow and at the slam, for the user and the ones caught
                net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
                if (self != null && b != null && (self.m_19879_() == m.user() || b.victims.contains(self.m_19879_()))) {
                    com.hexhaki.piece.client.fx.ClashFx.frame(at, phase == MochiMoves.BUZZ_GROUND ? 3.0F : 2.0F, 0xE0101E, m.id());
                }
            }
            default -> {
            }
        }
    }

    /** 3.6.3: a Lotus Mochi Combo message (its phases: server/MochiMoves.LOTUS). */
    static void lotus(S2CUoFx m, Vec3 at, Vec3 a, int phase, boolean coat) {
        switch (phase) {
            case MochiMoves.LOTUS_PUNCH -> add(LOTUSES, new Lotus(m.user(), clock, coat, m.id()));
            case MochiMoves.LOTUS_HIT -> {
                Vec3 feet = at;
                ClientLevel level = Minecraft.m_91087_().f_91073_;
                Entity body = level == null ? null : level.m_6815_(m.id());
                if (body != null) {
                    feet = body.m_20182_();
                }
                for (Lotus l : LOTUSES) {
                    if (l.user == m.user() && clock - l.born <= MochiDraw.LOTUS_PUNCH + 2) {
                        l.body = m.id();
                        l.ground = feet.f_82480_;
                    }
                }
                add(RISES, new LotusRise(m.id(), feet, clock + (MochiDraw.LOTUS_LAUNCH - MochiDraw.LOTUS_PUNCH), m.id() * 31 + 7));
            }
            case MochiMoves.LOTUS_AIR -> add(AIRS, new LotusAir(m.id(), unit(a), clock, coat, m.id() * 13 + Math.round(m.by())));
            case MochiMoves.LOTUS_SLAM -> {
                double ground = at.f_82480_ - 7.0 - MochiDraw.LOTUS_HIGH;
                for (LotusRise r : RISES) {
                    if (r.body == m.id()) {
                        ground = r.ground.f_82480_;
                    }
                }
                add(SLAMS, new LotusSlam(m.id(), at, ground, clock, coat, m.id() * 17 + 5));
            }
            case MochiMoves.LOTUS_GROUND -> add(GROUNDS, new LotusGround(at, clock, m.id()));
            default -> {
            }
        }
    }

    static Vec3 unit(Vec3 v) {
        double l = v.m_82553_();
        return l < 1.0E-6 ? new Vec3(0, 0, 1) : v.m_82490_(1.0 / l);
    }

    static Vec3 flat(Vec3 v) {
        Vec3 f = new Vec3(v.f_82479_, 0, v.f_82481_);
        return f.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : f.m_82541_();
    }

    static boolean self(int id) {
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        return self != null && self.m_19879_() == id;
    }

    // ------------------------------------------------------------------------------------------------------ one's own wheel

    /** Zan Giri Mochi: one's own body rolled to a place over so many ticks (the server snags whoever it meets on the way). */
    /** 3.6.3: one's own Buzz Cut wheel rolling (its own client moves it, where one looks: the reference's winding path). */
    static final class Carry {
        final int ticks;
        int age;

        Carry(int ticks) {
            this.ticks = Math.max(1, ticks);
        }
    }

    static Carry carry;

    static void stopCarry() {
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        if (carry != null && self != null) {
            Vec3 v = self.m_20184_();
            self.m_20334_(0.0, Math.min(0.0, v.f_82480_), 0.0);
        }
        carry = null;
    }

    static void roll(net.minecraft.client.player.LocalPlayer self) {
        Carry c = carry;
        if (self == null || !self.m_6084_() || self.m_20159_()) {
            carry = null;
            return;
        }
        // speeding up to its top speed, rolling where one looks, its own fall kept, never thrown up
        double speed = MochiDraw.BUZZ_SPEED * Math.min(1.0, (c.age + 2) / 12.0);
        Vec3 way = flat(self.m_20154_());
        self.m_20334_(way.f_82479_ * speed, Math.min(0.0, self.m_20184_().f_82480_), way.f_82481_ * speed);
        self.f_19789_ = 0.0F;
        if (++c.age >= c.ticks) {
            stopCarry();
        }
    }

    @SubscribeEvent
    public static void steady(net.minecraftforge.client.event.MovementInputUpdateEvent event) {
        if (carry == null && !filming() || event.getEntity() != Minecraft.m_91087_().f_91074_) {
            return;
        }
        net.minecraft.client.player.Input input = event.getInput();
        input.f_108566_ = 0.0F;
        input.f_108567_ = 0.0F;
        input.f_108572_ = false;
        input.f_108573_ = false;
    }

    /** The one rolling as a wheel is the wheel: their body is not drawn while it rolls. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void asWheel(RenderPlayerEvent.Pre event) {
        Buzz b = WHEELS.isEmpty() ? null : WHEELS.get(event.getEntity().m_19879_());
        if (b != null && b.rolling(clock)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void noHands(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ == null) {
            return;
        }
        int id = minecraft.f_91074_.m_19879_();
        Buzz b = WHEELS.isEmpty() ? null : WHEELS.get(id);
        if (b != null && b.rolling(clock)) {
            event.setCanceled(true);
            return;
        }
        // 3.6.3: one's own arm turned into mochi is not drawn as a hand too (the main hand the right arm, the off hand the left)
        int arms = mochiArms(id);
        if (arms != 0 && (arms & (event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? RIGHT : LEFT)) != 0) {
            event.setCanceled(true);
        }
    }

    /** 3.6.3: a body's right and left arm, its right leg (mochiArms). */
    static final int RIGHT = 1;
    static final int LEFT = 2;
    static final int RIGHT_LEG = 4;
    /** 3.6.3: Yanagi Mochi's leg is mochi from the clip's lift (tick 3) until it comes back (3 ticks before its end). */
    static final double LEG_FROM = 3.0;
    static final double LEG_BACK = 3.0;

    /**
     * 3.6.3 (the owner: "The mochi M1s should turn the user's arms into the mochi not create another arm above it", "X Should also turn
     * the arms into mochi not create a layer above it"): which of {@code user}'s arms are mochi now ({@link #RIGHT}, {@link #LEFT}): a
     * punch's arm while it is out, both of Kaku Mochi's.
     */
    static int mochiArms(int user) {
        Buzz b = WHEELS.isEmpty() ? null : WHEELS.get(user);
        if (b != null && b.caught >= 0 && clock - b.caught < MochiDraw.BUZZ_END) {
            return RIGHT; // 3.6.3: the right arm is the mace (MochiDraw.buzzMace)
        }
        if (PUNCHES.isEmpty()) {
            return 0;
        }
        int arms = 0;
        for (Punch p : PUNCHES) {
            if (p.user == user && clock - p.born <= MochiDraw.PUNCH_LIFE) {
                arms |= p.which == 0 ? RIGHT : LEFT;
            }
        }
        return arms;
    }

    /** 3.6.3: the parts hidden for this frame's body (restored as it is drawn), or null. */
    private static net.minecraft.client.model.geom.ModelPart[] hidden;

    /** 3.6.3: an arm turned into mochi: the body's own arm (and its sleeve) not drawn while it is. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void hideArms(RenderPlayerEvent.Pre event) {
        int arms = mochiArms(event.getEntity().m_19879_());
        if (arms == 0 || event.isCanceled() || !(event.getRenderer().m_7200_() instanceof net.minecraft.client.model.PlayerModel<?> model)) {
            return;
        }
        hidden = new net.minecraft.client.model.geom.ModelPart[] {(arms & RIGHT) != 0 ? model.f_102811_ : null,
            (arms & RIGHT) != 0 ? model.f_103375_ : null, (arms & LEFT) != 0 ? model.f_102812_ : null, (arms & LEFT) != 0 ? model.f_103374_ : null,
            (arms & RIGHT_LEG) != 0 ? model.f_102813_ : null, (arms & RIGHT_LEG) != 0 ? model.f_103377_ : null};
        for (int k = 0; k < hidden.length; k++) {
            if (hidden[k] != null && hidden[k].f_104207_) {
                hidden[k].f_104207_ = false;
            } else {
                hidden[k] = null;
            }
        }
    }

    @SubscribeEvent
    public static void showArms(RenderPlayerEvent.Post event) {
        if (hidden != null) {
            for (net.minecraft.client.model.geom.ModelPart part : hidden) {
                if (part != null) {
                    part.f_104207_ = true;
                }
            }
            hidden = null;
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
            ClientKits.KITS.put(Fruit.MOCHI, new MochiClient()); // the original has no client kit for it
        }
        if (empty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.m_91104_()) {
            return;
        }
        if (carry != null) {
            roll(minecraft.f_91074_);
        }
        PUNCHES.removeIf(p -> clock - p.born > MochiDraw.PUNCH_LIFE + 1);
        SPLATS.removeIf(s -> clock - s.born > MochiDraw.SPLAT_LIFE + 1);
        BOOMS.removeIf(b -> clock - b.born > MochiDraw.BOOM_LIFE + 1);
        SWELLS.entrySet().removeIf(e -> clock > e.getValue().until || level.m_6815_(e.getKey()) == null);
        FISTS.values().removeIf(f -> clock - f.born > f.flight + 1);
        WHEELS.entrySet().removeIf(e -> buzzDone(e.getValue()) || level.m_6815_(e.getKey()) == null);
        MARKS.removeIf(k -> clock - k.born > (k.ground ? MochiDraw.BUZZ_CRATER_LIFE : 12.0) + 1);
        buzzTick(level);
        STUCKS.values().removeIf(s -> clock - s.born > s.ticks + 1);
        MUDDLES.removeIf(u -> clock - u.born > MochiDraw.MUDDLE_LIE + 1);
        LOTUSES.removeIf(l -> clock - l.born > MochiDraw.LOTUS_LIFE + 1);
        SLAMS.removeIf(l -> clock - l.born > MochiDraw.LOTUS_SLAM_LIFE + 1);
        RISES.removeIf(r -> clock - r.born > MochiDraw.LOTUS_GROUND - MochiDraw.LOTUS_LAUNCH + 2);
        AIRS.removeIf(r -> clock - r.born > MochiDraw.LOTUS_AIR_LIFE + 1);
        GROUNDS.removeIf(g -> clock - g.born > MochiDraw.LOTUS_GROUND_LIFE + 1);
        CHESTNUTS.entrySet().removeIf(e -> e.getValue().ended >= 0 && clock - e.getValue().ended > MochiDraw.CHESTNUT_FALL + 2
            || clock > e.getValue().until + MochiDraw.CHESTNUT_FALL + 6 || level.m_6815_(e.getKey()) == null);
        SPIKES.removeIf(s -> clock - s.born > MochiDraw.SPIKE_LIFE + 1);
        AREAS.entrySet().removeIf(e -> e.getValue().ended >= 0 && clock - e.getValue().ended > MochiDraw.ANEMONE_LINGER + 1
            || clock > e.getValue().until + MochiDraw.ANEMONE_LINGER + 4);
        STRIKES.removeIf(s -> clock - s.born > MochiDraw.ANEMONE_PUNCH + 1);
    }

    static boolean empty() {
        return PUNCHES.isEmpty() && SPLATS.isEmpty() && BOOMS.isEmpty() && SWELLS.isEmpty() && FISTS.isEmpty() && WHEELS.isEmpty()
            && STUCKS.isEmpty() && MUDDLES.isEmpty() && LOTUSES.isEmpty() && SLAMS.isEmpty() && RISES.isEmpty() && AIRS.isEmpty()
            && GROUNDS.isEmpty() && CHESTNUTS.isEmpty()
            && SPIKES.isEmpty() && AREAS.isEmpty() && STRIKES.isEmpty() && MARKS.isEmpty() && carry == null;
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
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: a Mochi fruit move could not be drawn", failure);
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

    static boolean ownView(Minecraft minecraft, Entity user) {
        return user == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == net.minecraft.client.CameraType.FIRST_PERSON;
    }

    /** {@code degrees} brought within -180 to 180. */
    static float wrap(float degrees) {
        float d = degrees % 360.0F;
        return d >= 180.0F ? d - 360.0F : d < -180.0F ? d + 360.0F : d;
    }

    /** A body's turn (radians), its way (-sin, cos). */
    static double yaw(Entity user, float partial) {
        if (user instanceof LivingEntity body) {
            return Math.toRadians(body.f_20884_ + wrap(body.f_20883_ - body.f_20884_) * partial);
        }
        Vec3 look = flat(user.m_20252_(partial));
        return Math.atan2(-look.f_82479_, look.f_82481_);
    }

    /**
     * Where a mochi arm leaves {@code user} this frame: the right shoulder ({@code which} 0) or the left (1); in one's own first person
     * a little below and in front of the view on that side (the arm would fill the view from the shoulder).
     */
    static Vec3 shoulder(Minecraft minecraft, Entity user, int which, float partial) {
        double side = which == 0 ? 1.0 : -1.0;
        if (ownView(minecraft, user)) {
            Vec3 look = user.m_20252_(partial);
            Vec3 f = flat(look);
            Vec3 right = new Vec3(-f.f_82481_, 0, f.f_82479_);
            return user.m_20299_(partial).m_82549_(look.m_82490_(0.45)).m_82549_(right.m_82490_(0.4 * side)).m_82520_(0.0, -0.45, 0.0);
        }
        double y = yaw(user, partial);
        Vec3 right = new Vec3(-Math.cos(y), 0, -Math.sin(y));
        return user.m_20318_(partial).m_82520_(0.0, user.m_20206_() * 0.76, 0.0).m_82549_(right.m_82490_(0.36 * side));
    }

    /**
     * 3.5.5: where one's own held mochi sits in first person ({@code ahead} blocks along the look, low and to the right): Mochi Tsuki's
     * coils and drill and Yaki Mochi's swollen forearm, right at the eye, would fill the whole view.
     */
    static Vec3 ownHand(Entity user, double ahead, float partial) {
        Vec3 look = user.m_20252_(partial);
        Vec3 f = flat(look);
        Vec3 right = new Vec3(-f.f_82481_, 0, f.f_82479_);
        return user.m_20299_(partial).m_82549_(look.m_82490_(ahead)).m_82549_(right.m_82490_(0.55)).m_82520_(0.0, -0.6, 0.0);
    }

    static final double OWN_SWELL = 1.4;

    /** Where both shoulders' middle is (Kaku Mochi's arms rise from it). */
    static Vec3 chest(Entity user, float partial) {
        return user.m_20318_(partial).m_82520_(0.0, user.m_20206_() * 0.76, 0.0);
    }

    private static final double[] STRIKE = new double[MOST * 5];

    /** The torn ground's points made camera-relative (the render thread only: no allocation a frame). */
    private static final double[] BUZZ_PATH = new double[3 * MochiDraw.BUZZ_PATH_MOST];
    private static final double[] BUZZ_AGES = new double[MochiDraw.BUZZ_PATH_MOST];
    private static final double[] BUZZ_ARM = new double[8];
    private static final double[] BUZZ_BEND = new double[4];

    /** 3.6.3: Buzz Cut Mochi: the wheel, the torn ground, the mace and the whirl, the blows and the crater (MochiDraw.buzz*). */
    static void drawBuzz(Minecraft minecraft, ClientLevel level, Vec3 cam, float partial, double now, double cx, double cy, double cz) {
        for (Map.Entry<Integer, Buzz> e : WHEELS.entrySet()) {
            Buzz b = e.getValue();
            Entity user = level.m_6815_(e.getKey());
            if (b.count > 0) {
                double[] path = BUZZ_PATH;
                double[] ages = BUZZ_AGES;
                for (int k = 0; k < b.count; k++) {
                    path[k * 3] = b.path[k * 3] - cx;
                    path[k * 3 + 1] = b.path[k * 3 + 1] - cy;
                    path[k * 3 + 2] = b.path[k * 3 + 2] - cz;
                    ages[k] = now - b.passed[k];
                }
                MochiDraw.buzzTiles(SINK, path, ages, b.count, b.seed);
            }
            if (user == null) {
                continue;
            }
            Vec3 feet = user.m_20318_(partial);
            if (far(feet, cam)) {
                continue;
            }
            double age = now - b.born;
            double r = MochiDraw.BUZZ_WHEEL;
            if (b.rolling(clock) && !(ownView(minecraft, user) && b.caught < 0 && b.missed < 0)) {
                // the wheel (not in one's own first person: it would close round the view), unrolling into the body once it stops
                double form = Math.min(1.0, age / 4.0);
                if (b.caught >= 0) {
                    form *= 1.0 - (now - b.caught) / 3.0;
                } else if (b.missed >= 0) {
                    form *= 1.0 - (now - b.missed) / 3.0;
                }
                Vec3 way = flat(user.m_20252_(partial));
                double speed = b.caught < 0 && b.missed < 0 ? Math.min(1.0, (age + 2) / 12.0) : 0.3;
                MochiDraw.buzzWheel(SINK, feet.f_82479_ - cx, feet.f_82480_ + r - cy, feet.f_82481_ - cz, way.f_82479_, way.f_82481_, b.rolled, speed,
                    Math.max(0.0, form), age, b.seed);
            }
            if (b.caught >= 0 && b.feet != null) {
                double t = now - b.caught;
                if (t > MochiDraw.BUZZ_END) {
                    continue;
                }
                double[] arm = MochiDraw.buzzArm(t, b.feet.f_82479_, b.feet.f_82480_, b.feet.f_82481_, b.yaw0, BUZZ_ARM);
                double[] bend = MochiDraw.buzzBend(t, b.yaw0, BUZZ_BEND);
                MochiDraw.buzzMace(SINK, arm[0] - cx, arm[1] - cy, arm[2] - cz, arm[3], arm[4], arm[5], arm[6], MochiDraw.buzzSwell(t), bend[0],
                    bend[1], bend[2], bend[3], t, b.seed);
                if (t >= MochiDraw.BUZZ_HIT && t < MochiDraw.BUZZ_WHIRL + 4) {
                    double a = Math.min(1.0, (t - MochiDraw.BUZZ_HIT) / 6.0) * (1.0 - Math.max(0.0, (t - MochiDraw.BUZZ_WHIRL) / 4.0));
                    MochiDraw.buzzVortex(SINK, b.feet.f_82479_ - cx, b.feet.f_82480_ - cy, b.feet.f_82481_ - cz, Math.atan2(arm[5], arm[3]), arm[6], a);
                }
            }
        }
        for (BuzzMark k : MARKS) {
            if (far(k.at, cam)) {
                continue;
            }
            double age = now - k.born;
            if (k.ground) {
                MochiDraw.buzzCrater(SINK, k.at.f_82479_ - cx, k.at.f_82480_ - cy, k.at.f_82481_ - cz, age, k.seed);
            } else if (age < 12.0) {
                // the swing's blow: a burst of white and a ring of shock off the ball
                MochiDraw.splat(SINK, k.at.f_82479_ - cx, k.at.f_82480_ - cy, k.at.f_82481_ - cz, 0, 1, 0, 1.6, age, k.seed);
                com.hexhaki.piece.dragon.FireMesh.ring(SINK, k.at.f_82479_ - cx, k.at.f_82480_ - cy, k.at.f_82481_ - cz, 0, 1, 0, 1.0 + 5.0 * age / 12.0,
                    0.25, 0.1, 0xFFFFFF, 0.7 * (1.0 - age / 12.0));
            }
        }
    }

    /** 3.6.3: Lotus Mochi Combo, every phase of it (MochiDraw.lotus, lotusRise, lotusAir, lotusSlam, lotusGround). */
    static void drawLotus(Minecraft minecraft, ClientLevel level, Vec3 cam, float partial, double now, double cx, double cy, double cz) {
        for (Lotus l : LOTUSES) {
            Entity user = level.m_6815_(l.user);
            if (user == null) {
                continue;
            }
            Vec3 feet = user.m_20318_(partial);
            if (far(feet, cam)) {
                continue;
            }
            Vec3 look = user.m_20252_(partial);
            Vec3 way = flat(look);
            double wx = way.f_82479_;
            double wz = way.f_82481_;
            double[] d = MochiDraw.lotusAt(wx, wz, new double[3]);
            if (ownView(minecraft, user)) {
                // one's own in first person: further ahead, so it stands in the view
                d[0] += wx * 2.3 - wz * 0.6;
                d[2] += wz * 2.3 + wx * 0.6;
            }
            // the left hand the ribbon leaves from (the left of the look: (wz, -wx))
            Vec3 hand = feet.m_82520_(wz * 0.4 + wx * 0.3, 1.3, -wx * 0.4 + wz * 0.3);
            Entity body = l.body >= 0 ? level.m_6815_(l.body) : null;
            Vec3 aim = body != null ? body.m_20318_(partial).m_82520_(0, body.m_20206_() * 0.55, 0)
                : user.m_20299_(partial).m_82549_(look.m_82490_(MochiMoves.LOTUS_REACH));
            MochiDraw.lotus(SINK, hand.f_82479_ - cx, hand.f_82480_ - cy, hand.f_82481_ - cz, feet.f_82479_ + d[0] - cx, feet.f_82480_ + d[1] - cy,
                feet.f_82481_ + d[2] - cz, wx, wz, aim.f_82479_ - cx, aim.f_82480_ - cy, aim.f_82481_ - cz, feet.f_82480_ - cy, now - l.born, l.coat,
                l.seed);
        }
        for (LotusRise r : RISES) {
            Entity body = level.m_6815_(r.body);
            Vec3 at = body != null ? body.m_20318_(partial) : r.ground;
            if (!far(at, cam)) {
                MochiDraw.lotusRise(SINK, at.f_82479_ - cx, at.f_82480_ - cy, at.f_82481_ - cz, r.ground.f_82480_ - cy, now - r.born, r.seed);
            }
        }
        for (LotusAir r : AIRS) {
            Entity body = level.m_6815_(r.body);
            if (body == null) {
                continue;
            }
            Vec3 at = body.m_20318_(partial).m_82520_(0, body.m_20206_() * 0.55, 0);
            if (!far(at, cam)) {
                MochiDraw.lotusAir(SINK, at.f_82479_ - cx, at.f_82480_ - cy, at.f_82481_ - cz, r.side.f_82479_, r.side.f_82480_, r.side.f_82481_,
                    now - r.born, r.coat, r.seed);
            }
        }
        for (LotusSlam l : SLAMS) {
            Entity body = level.m_6815_(l.body);
            Vec3 at = body != null ? body.m_20318_(partial) : new Vec3(l.top.f_82479_, l.ground, l.top.f_82481_);
            if (!far(l.top, cam)) {
                MochiDraw.lotusSlam(SINK, l.top.f_82479_ - cx, l.top.f_82480_ - cy, l.top.f_82481_ - cz, at.f_82479_ - cx, at.f_82480_ - cy,
                    at.f_82481_ - cz, l.ground - cy, now - l.born, l.coat, l.seed);
            }
        }
        for (LotusGround g : GROUNDS) {
            if (!far(g.at, cam)) {
                MochiDraw.lotusGround(SINK, g.at.f_82479_ - cx, g.at.f_82480_ - cy, g.at.f_82481_ - cz, now - g.born, g.seed);
            }
        }
    }

    static void draw(Minecraft minecraft, ClientLevel level, Vec3 cam, float partial) {
        double cx = cam.f_82479_;
        double cy = cam.f_82480_;
        double cz = cam.f_82481_;
        double now = clock + partial;
        for (Muddle u : MUDDLES) {
            if (far(u.at, cam) && far(u.at.m_82549_(u.way.m_82490_(u.length)), cam)) {
                continue;
            }
            // 3.6.3: drawn from just before its splash (the dough engulfing the user), on the reference's beats
            MochiDraw.muddle(SINK, u.at.f_82479_ - cx, u.at.f_82480_ - cy, u.at.f_82481_ - cz, u.way.f_82479_, u.way.f_82481_, now - u.born, u.seed);
        }
        for (Map.Entry<Integer, Stuck> e : STUCKS.entrySet()) {
            Stuck s = e.getValue();
            Entity body = level.m_6815_(e.getKey());
            Vec3 feet = body != null ? body.m_20318_(partial) : s.at;
            if (far(feet, cam) || body != null && ownView(minecraft, body)) {
                continue;
            }
            double age = now - s.born;
            double fade = Math.min(1.0, age / 3.0) * Math.max(0.0, Math.min(1.0, (s.ticks - age) / 5.0));
            MochiDraw.stuck(SINK, feet.f_82479_ - cx, feet.f_82480_ - cy, feet.f_82481_ - cz, s.height, fade, age, e.getKey());
        }
        for (Punch p : PUNCHES) {
            Entity user = level.m_6815_(p.user);
            if (user == null) {
                continue;
            }
            Vec3 from = shoulder(minecraft, user, p.which, partial);
            Vec3 way = user.m_20252_(partial);
            if (!far(from, cam)) {
                MochiDraw.punch(SINK, from.f_82479_ - cx, from.f_82480_ - cy, from.f_82481_ - cz, way.f_82479_, way.f_82480_, way.f_82481_, p.reach,
                    now - p.born, p.coat, p.seed, p.which == 1);
            }
        }
        for (Map.Entry<Integer, Swell> e : SWELLS.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            if (user == null) {
                continue;
            }
            Swell s = e.getValue();
            Vec3 look = user.m_20252_(partial);
            Vec3 elbow = ownView(minecraft, user) ? ownHand(user, OWN_SWELL, partial) : shoulder(minecraft, user, 0, partial).m_82549_(look.m_82490_(0.25));
            if (!far(elbow, cam)) {
                double age = now - s.born;
                MochiDraw.swell(SINK, elbow.f_82479_ - cx, elbow.f_82480_ - cy, elbow.f_82481_ - cz, look.f_82479_, look.f_82480_, look.f_82481_,
                    MochiMoves.yakiCharge(age), age, s.coat, e.getKey(), user.m_20318_(partial).f_82480_ - cy);
            }
        }
        for (Fist f : FISTS.values()) {
            double age = now - f.born;
            if (age < MochiDraw.YAKI_PUFF && !far(f.from, cam)) {
                Vec3 u = unit(f.step);
                MochiDraw.yakiPuff(SINK, f.from.f_82479_ - cx, f.from.f_82480_ - cy, f.from.f_82481_ - cz, u.f_82479_, u.f_82480_, u.f_82481_, age,
                    f.seed + 11);
            }
            Vec3 p = f.from.m_82549_(f.step.m_82490_(age));
            if (!far(p, cam)) {
                Vec3 u = unit(f.step);
                MochiDraw.yaki(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, u.f_82479_, u.f_82480_, u.f_82481_, age, f.coat, f.seed,
                    f.charge);
            }
        }
        drawLotus(minecraft, level, cam, partial, now, cx, cy, cz);
        for (Map.Entry<Integer, Chestnut> e : CHESTNUTS.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            if (user == null) {
                continue;
            }
            Chestnut c = e.getValue();
            Vec3 at = MochiMoves.chestnutAt(user.m_20318_(partial), user.m_146908_(), user.m_20206_());
            if (far(at, cam)) {
                continue;
            }
            // 3.6.3: on the reference's beats (MochiDraw.CHESTNUT_*): the comet out of the right hand, the disc tilted toward the aim, its
            // end (the server's, or its time run out) the fall onto the user and the splat round their feet
            Vec3 feet = user.m_20318_(partial);
            Vec3 way = flat(user.m_20252_(partial));
            Vec3 hand = feet.m_82520_(-way.f_82481_ * 0.4 + way.f_82479_ * 0.2, 1.4, way.f_82479_ * 0.4 + way.f_82481_ * 0.2);
            double ended = c.ended >= 0 ? now - c.ended : now > c.until ? now - c.until : -1.0;
            MochiDraw.chestnut(SINK, at.f_82479_ - cx, at.f_82480_ - cy, at.f_82481_ - cz, hand.f_82479_ - cx, hand.f_82480_ - cy, hand.f_82481_ - cz,
                way.f_82479_, way.f_82481_, feet.f_82480_ - cy, now - c.born, ended, now - c.fired, c.coat, c.seed);
        }
        for (Spike s : SPIKES) {
            Entity user = level.m_6815_(s.user);
            Vec3 from = user != null ? MochiMoves.chestnutAt(user.m_20318_(partial), user.m_146908_(), user.m_20206_()) : s.at;
            if (!far(s.at, cam)) {
                MochiDraw.spike(SINK, from.f_82479_ - cx, from.f_82480_ - cy, from.f_82481_ - cz, s.at.f_82479_ - cx, s.at.f_82480_ - cy, s.at.f_82481_ - cz,
                    now - s.born);
            }
        }
        for (Map.Entry<Integer, Area> e : AREAS.entrySet()) {
            Area n = e.getValue();
            if (far(n.at, cam)) {
                continue;
            }
            double age = now - n.born;
            // 3.6.3: its own art forms each donut and, let go, bursts the dome and leaves the splats (MochiDraw.anemoneArea)
            double ended = n.ended >= 0 ? now - n.ended : now > n.until ? now - n.until : -1.0;
            double form = 1.0;
            int count = 0;
            for (Strike s : STRIKES) {
                if (s.user != e.getKey() || count >= MOST) {
                    continue;
                }
                STRIKE[count * 5] = s.donut;
                STRIKE[count * 5 + 1] = s.at.f_82479_ - cx;
                STRIKE[count * 5 + 2] = s.at.f_82480_ - cy;
                STRIKE[count * 5 + 3] = s.at.f_82481_ - cz;
                STRIKE[count * 5 + 4] = now - s.born;
                count++;
            }
            MochiDraw.anemoneArea(SINK, n.at.f_82479_ - cx, n.at.f_82480_ - cy, n.at.f_82481_ - cz, n.radius, form, STRIKE, count, n.coat, age, n.seed,
                ended);
        }
        drawBuzz(minecraft, level, cam, partial, now, cx, cy, cz);
        for (Splat s : SPLATS) {
            if (!far(s.at, cam)) {
                MochiDraw.splat(SINK, s.at.f_82479_ - cx, s.at.f_82480_ - cy, s.at.f_82481_ - cz, s.way.f_82479_, s.way.f_82480_, s.way.f_82481_, s.size,
                    now - s.born, s.seed);
            }
        }
        for (Boom b : BOOMS) {
            if (!far(b.at, cam)) {
                MochiDraw.boom(SINK, b.at.f_82479_ - cx, b.at.f_82480_ - cy, b.at.f_82481_ - cz, b.size, now - b.born, b.seed);
            }
        }
    }

    static void clear() {
        PUNCHES.clear();
        SPLATS.clear();
        BOOMS.clear();
        SWELLS.clear();
        FISTS.clear();
        WHEELS.clear();
        STUCKS.clear();
        MUDDLES.clear();
        LOTUSES.clear();
        MARKS.clear();
        SLAMS.clear();
        RISES.clear();
        AIRS.clear();
        GROUNDS.clear();
        CHESTNUTS.clear();
        SPIKES.clear();
        AREAS.clear();
        STRIKES.clear();
        carry = null;
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
