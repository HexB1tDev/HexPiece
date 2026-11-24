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

    /** Mochi Tsuki held (by user). */
    record Held(long born, long until, boolean coat) {
    }

    record Thrust(int user, Vec3 from, Vec3 way, double reach, double charge, long born, boolean coat) {
    }

    record Yanagi(int user, Vec3 at, Vec3 way, long born, int seed) {
    }

    record Kaku(int user, Vec3 at, Vec3 way, long born, boolean coat, int seed) {
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

    /** Zan Giri Mochi's wheel (by user): rolling from where to where. */
    record Wheel(Vec3 from, Vec3 to, double radius, long born, int ticks) {
    }

    record Club(int user, int body, Vec3 at, long born, boolean coat, int seed) {
    }

    /** Mochi Anemone held (by user); ended once let go (its rings shrink away). */
    static final class Anemone {
        final long born;
        final long until;
        final boolean coat;
        long ended = -1;

        Anemone(long born, long until, boolean coat) {
            this.born = born;
            this.until = until;
            this.coat = coat;
        }
    }

    record Reach(int user, int ring, Vec3 at, long born) {
    }

    record Glob(Vec3 from, Vec3 step, long born, int flight) {
    }

    record Stuck(Vec3 at, double height, long born, int ticks) {
    }

    record Patch(Vec3 at, double radius, long born, int ticks, int seed) {
    }

    /** The Awakening (by user): its ground round the user's feet, the lumps worked out each tick (world: x, y, z, size, seed each). */
    static final class Awake {
        final long born;
        final int ticks;
        final double radius;
        long ended = -1;
        final double[] lumps = new double[MochiDraw.GROUND_MOST * 5];
        int count;

        Awake(long born, int ticks, double radius) {
            this.born = born;
            this.ticks = ticks;
            this.radius = radius;
        }
    }

    record Wave(Vec3 at, Vec3 way, double range, long born, int ticks, int seed) {
    }

    record Walls(Vec3 at, Vec3 way, double apart, long born, int seed) {
    }

    record Rain(Vec3 at, double radius, long born, int ticks, int seed) {
    }

    record Donuts(int user, Vec3 at, long born, int ticks, boolean coat, int seed) {
    }

    record Dome(Vec3 at, double radius, long born, int ticks, int seed) {
    }

    record Hole(int user, Vec3 at, Vec3 way, long born) {
    }

    static final int MOST = 64;
    static final double FAR = 160.0;
    private static final List<Punch> PUNCHES = new ArrayList<>();
    static final Map<Integer, Held> HELD = new HashMap<>();
    private static final List<Thrust> THRUSTS = new ArrayList<>();
    private static final List<Yanagi> YANAGIS = new ArrayList<>();
    private static final List<Kaku> KAKUS = new ArrayList<>();
    private static final List<Splat> SPLATS = new ArrayList<>();
    private static final List<Boom> BOOMS = new ArrayList<>();
    static final Map<Integer, Swell> SWELLS = new HashMap<>();
    static final Map<Integer, Fist> FISTS = new HashMap<>();
    static final Map<Integer, Wheel> WHEELS = new HashMap<>();
    private static final List<Club> CLUBS = new ArrayList<>();
    static final Map<Integer, Anemone> ANEMONES = new HashMap<>();
    private static final List<Reach> REACHES = new ArrayList<>();
    static final Map<Integer, Glob> GLOBS = new HashMap<>();
    static final Map<Integer, Stuck> STUCKS = new HashMap<>();
    private static final List<Patch> PATCHES = new ArrayList<>();
    static final Map<Integer, Awake> AWAKES = new HashMap<>();
    private static final List<Wave> WAVES = new ArrayList<>();
    private static final List<Walls> WALLS = new ArrayList<>();
    private static final List<Rain> RAINS = new ArrayList<>();
    private static final List<Donuts> DONUTS = new ArrayList<>();
    private static final List<Dome> DOMES = new ArrayList<>();
    private static final List<Hole> HOLES = new ArrayList<>();
    /** How long the Anemone's rings and the Awakening's ground take to shrink away once ended. */
    static final int ANEMONE_FADE = 6;
    static final int GROUND_FADE = 10;
    static final int SINK_TICKS = 6;

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
        Awake w = AWAKES.get(user);
        return w != null && w.ended < 0 && clock - w.born < w.ticks;
    }

    public static void accept(S2CUoFx m) {
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        Vec3 a = new Vec3(m.ax(), m.ay(), m.az());
        boolean coat = m.bz() > 0.5F;
        int phase = Math.round(m.bx());
        switch (m.kind()) {
            case MochiMoves.PUNCH -> add(PUNCHES, new Punch(m.user(), phase, m.size(), clock, coat, m.id() + m.user()));
            case MochiMoves.TSUKI -> {
                if (phase == MochiMoves.TSUKI_CHARGE) {
                    put(HELD, m.user(), new Held(clock, clock + m.ticks() + 2, coat));
                } else if (phase == MochiMoves.TSUKI_THRUST) {
                    HELD.remove(m.user());
                    add(THRUSTS, new Thrust(m.user(), at, unit(a), m.size(), m.by(), clock, coat));
                } else {
                    HELD.remove(m.user());
                }
            }
            case MochiMoves.YANAGI -> add(YANAGIS, new Yanagi(m.user(), at, unit(a), clock, m.id()));
            case MochiMoves.KAKU -> add(KAKUS, new Kaku(m.user(), at, unit(a), clock, coat, m.id()));
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
            case MochiMoves.WHEEL -> {
                if (phase == 1) {
                    put(WHEELS, m.user(), new Wheel(a, at, m.size(), clock, m.ticks()));
                    if (self(m.user())) {
                        carry = new Carry(at, m.ticks());
                    }
                } else {
                    WHEELS.remove(m.user());
                    if (self(m.user())) {
                        stopCarry();
                    }
                }
            }
            case MochiMoves.CLUB -> add(CLUBS, new Club(m.user(), m.id(), at, clock, coat, m.id() + 7));
            case MochiMoves.ANEMONE -> {
                if (phase == 1) {
                    put(ANEMONES, m.user(), new Anemone(clock, clock + m.ticks(), coat));
                } else {
                    Anemone n = ANEMONES.get(m.user());
                    if (n != null && n.ended < 0) {
                        n.ended = clock;
                    }
                }
            }
            case MochiMoves.ANEMONE_PUNCH -> add(REACHES, new Reach(m.user(), m.id(), at, clock));
            case MochiMoves.GLOB -> {
                if (m.ticks() > 0) {
                    put(GLOBS, m.id(), new Glob(at, a, clock, m.ticks()));
                } else {
                    GLOBS.remove(m.id());
                }
            }
            case MochiMoves.STUCK -> {
                if (m.ticks() > 0) {
                    put(STUCKS, m.id(), new Stuck(at, m.size(), clock, m.ticks()));
                } else {
                    STUCKS.remove(m.id());
                }
            }
            case MochiMoves.PATCH -> add(PATCHES, new Patch(at, m.size(), clock, m.ticks(), m.id()));
            case MochiMoves.SPLAT -> add(SPLATS, new Splat(at, unit(a), m.size(), clock, m.id()));
            case MochiMoves.BOOM -> add(BOOMS, new Boom(at, m.size(), clock, m.id()));
            case MochiMoves.AWAKEN -> {
                if (phase == 1) {
                    put(AWAKES, m.user(), new Awake(clock, m.ticks(), m.size()));
                } else {
                    Awake w = AWAKES.get(m.user());
                    if (w != null && w.ended < 0) {
                        w.ended = clock;
                    }
                }
            }
            case MochiMoves.NAGARE -> add(WAVES, new Wave(at, flat(a), m.size(), clock, m.ticks(), m.id()));
            case MochiMoves.KAGAMI -> add(WALLS, new Walls(at, flat(a), m.size(), clock, m.id()));
            case MochiMoves.AMADARE -> add(RAINS, new Rain(at, m.size(), clock, m.ticks(), m.id()));
            case MochiMoves.DONUTS -> add(DONUTS, new Donuts(m.user(), at, clock, m.ticks(), coat, m.id()));
            case MochiMoves.DOME -> add(DOMES, new Dome(at, m.size(), clock, m.ticks(), m.id()));
            case MochiMoves.DODGE -> {
                if (phase == MochiMoves.DODGE_HOLE) {
                    add(HOLES, new Hole(m.user(), at, unit(a), clock));
                }
            }
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
    static final class Carry {
        final Vec3 to;
        int left;

        Carry(Vec3 to, int ticks) {
            this.to = to;
            this.left = Math.max(1, ticks);
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
        Vec3 pos = self.m_20182_();
        double dx = (c.to.f_82479_ - pos.f_82479_) / Math.max(1, c.left);
        double dz = (c.to.f_82481_ - pos.f_82481_) / Math.max(1, c.left);
        // along the ground: its own fall kept (the wheel rolls over the land), never thrown up
        self.m_20334_(dx, Math.min(0.0, self.m_20184_().f_82480_), dz);
        self.f_19789_ = 0.0F;
        if (--c.left <= 0) {
            stopCarry();
        }
    }

    @SubscribeEvent
    public static void steady(net.minecraftforge.client.event.MovementInputUpdateEvent event) {
        if (carry == null || event.getEntity() != Minecraft.m_91087_().f_91074_) {
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
        if (!WHEELS.isEmpty() && WHEELS.containsKey(event.getEntity().m_19879_())) {
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
        if (!WHEELS.isEmpty() && WHEELS.containsKey(id)) {
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
        if (PUNCHES.isEmpty() && KAKUS.isEmpty() && YANAGIS.isEmpty()) {
            return 0;
        }
        int arms = 0;
        for (Punch p : PUNCHES) {
            if (p.user == user && clock - p.born <= MochiDraw.PUNCH_LIFE) {
                arms |= p.which == 0 ? RIGHT : LEFT;
            }
        }
        for (Kaku k : KAKUS) {
            if (k.user == user && clock - k.born <= MochiDraw.KAKU_LIFE) {
                arms |= RIGHT | LEFT;
            }
        }
        // 3.6.3 (the owner: "Z Move should lift the leg of the player and turn it into the many legs")
        for (Yanagi y : YANAGIS) {
            long age = clock - y.born;
            if (y.user == user && age >= LEG_FROM && age <= MochiDraw.YANAGI_LIFE - LEG_BACK) {
                arms |= RIGHT_LEG;
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
        HELD.entrySet().removeIf(e -> clock > e.getValue().until || level.m_6815_(e.getKey()) == null);
        THRUSTS.removeIf(t -> clock - t.born > MochiDraw.TSUKI_LIFE + 1);
        YANAGIS.removeIf(y -> clock - y.born > MochiDraw.YANAGI_LIFE + 1);
        KAKUS.removeIf(k -> clock - k.born > MochiDraw.KAKU_LIFE + 1);
        SPLATS.removeIf(s -> clock - s.born > MochiDraw.SPLAT_LIFE + 1);
        BOOMS.removeIf(b -> clock - b.born > MochiDraw.BOOM_LIFE + 1);
        SWELLS.entrySet().removeIf(e -> clock > e.getValue().until || level.m_6815_(e.getKey()) == null);
        FISTS.values().removeIf(f -> clock - f.born > f.flight + 1);
        WHEELS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().ticks + 2 || level.m_6815_(e.getKey()) == null);
        CLUBS.removeIf(c -> clock - c.born > MochiDraw.CLUB_LIFE + 1);
        ANEMONES.entrySet().removeIf(e -> e.getValue().ended >= 0 && clock - e.getValue().ended > ANEMONE_FADE
            || clock > e.getValue().until + ANEMONE_FADE || level.m_6815_(e.getKey()) == null);
        REACHES.removeIf(r -> clock - r.born > MochiDraw.ANEMONE_PUNCH + 1);
        GLOBS.values().removeIf(g -> clock - g.born > g.flight + 1);
        STUCKS.values().removeIf(s -> clock - s.born > s.ticks + 1);
        PATCHES.removeIf(p -> clock - p.born > p.ticks + 1);
        AWAKES.entrySet().removeIf(e -> e.getValue().ended >= 0 && clock - e.getValue().ended > GROUND_FADE
            || clock - e.getValue().born > e.getValue().ticks + GROUND_FADE || level.m_6815_(e.getKey()) == null);
        WAVES.removeIf(w -> clock - w.born > w.ticks + SINK_TICKS + 1);
        WALLS.removeIf(w -> clock - w.born > MochiDraw.WALLS_LIFE + 1);
        RAINS.removeIf(r -> clock - r.born > r.ticks + 1);
        DONUTS.removeIf(d -> clock - d.born > d.ticks + 1);
        DOMES.removeIf(d -> clock - d.born > d.ticks + SINK_TICKS + 1);
        HOLES.removeIf(h -> clock - h.born > MochiDraw.HOLE_LIFE + 1);
        for (Map.Entry<Integer, Awake> e : AWAKES.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            if (user != null) {
                lumps(level, user.m_20182_(), e.getValue());
            }
        }
    }

    private static final double[] CELLS = new double[MochiDraw.GROUND_MOST * 4];

    /**
     * The Awakening's ground round {@code feet}: the world's fixed lumps (MochiDraw.groundLumps) each on the top of its column's solid
     * block within 3 up and 6 down of the feet (none: left out).
     */
    static void lumps(ClientLevel level, Vec3 feet, Awake w) {
        int n = MochiDraw.groundLumps(feet.f_82479_, feet.f_82481_, w.radius, CELLS);
        int foot = (int) Math.floor(feet.f_82480_ - 0.01);
        int count = 0;
        for (int k = 0; k < n; k++) {
            double x = CELLS[k * 4];
            double z = CELLS[k * 4 + 1];
            int bx = (int) Math.floor(x);
            int bz = (int) Math.floor(z);
            for (int y = foot + 3; y >= foot - 6; y--) {
                net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(bx, y, bz);
                net.minecraft.world.level.block.state.BlockState state = level.m_8055_(pos);
                if (!state.m_60795_() && !state.m_60812_(level, pos).m_83281_()) {
                    double[] l = w.lumps;
                    l[count * 5] = x;
                    l[count * 5 + 1] = y + 1.0;
                    l[count * 5 + 2] = z;
                    l[count * 5 + 3] = CELLS[k * 4 + 2];
                    l[count * 5 + 4] = CELLS[k * 4 + 3];
                    count++;
                    break;
                }
            }
        }
        w.count = count;
    }

    static boolean empty() {
        return PUNCHES.isEmpty() && HELD.isEmpty() && THRUSTS.isEmpty() && YANAGIS.isEmpty() && KAKUS.isEmpty() && SPLATS.isEmpty()
            && BOOMS.isEmpty() && SWELLS.isEmpty() && FISTS.isEmpty() && WHEELS.isEmpty() && CLUBS.isEmpty() && ANEMONES.isEmpty()
            && REACHES.isEmpty() && GLOBS.isEmpty() && STUCKS.isEmpty() && PATCHES.isEmpty() && AWAKES.isEmpty() && WAVES.isEmpty()
            && WALLS.isEmpty() && RAINS.isEmpty() && DONUTS.isEmpty() && DOMES.isEmpty() && HOLES.isEmpty() && carry == null;
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

    static final double OWN_COIL = 1.7;
    static final double OWN_DRILL = 2.8;
    static final double OWN_SWELL = 1.4;
    /** How far below its usual height the Anemone's rings float round oneself in first person (else one hangs before the eye). */
    static final double OWN_RINGS_DOWN = 0.9;

    /** Where both shoulders' middle is (Kaku Mochi's arms rise from it). */
    static Vec3 chest(Entity user, float partial) {
        return user.m_20318_(partial).m_82520_(0.0, user.m_20206_() * 0.76, 0.0);
    }

    private static final double[] REACH = new double[MOST * 5];
    private static final double[] GROUND = new double[MochiDraw.GROUND_MOST * 5];

    static void draw(Minecraft minecraft, ClientLevel level, Vec3 cam, float partial) {
        double cx = cam.f_82479_;
        double cy = cam.f_82480_;
        double cz = cam.f_82481_;
        double now = clock + partial;
        // the Awakening's ground first (everything else stands on it)
        for (Map.Entry<Integer, Awake> e : AWAKES.entrySet()) {
            Awake w = e.getValue();
            double age = now - w.born;
            double grow = Math.min(1.0, age / GROUND_FADE) * (w.ended >= 0 ? Math.max(0.0, 1.0 - (now - w.ended) / GROUND_FADE) : 1.0)
                * Math.max(0.0, Math.min(1.0, (w.ticks + GROUND_FADE - age) / GROUND_FADE));
            for (int k = 0; k < w.count; k++) {
                GROUND[k * 5] = w.lumps[k * 5] - cx;
                GROUND[k * 5 + 1] = w.lumps[k * 5 + 1] - cy;
                GROUND[k * 5 + 2] = w.lumps[k * 5 + 2] - cz;
                GROUND[k * 5 + 3] = w.lumps[k * 5 + 3];
                GROUND[k * 5 + 4] = w.lumps[k * 5 + 4];
            }
            MochiDraw.ground(SINK, GROUND, w.count, grow, now);
        }
        for (Patch p : PATCHES) {
            if (!far(p.at, cam)) {
                double age = now - p.born;
                double fade = Math.min(1.0, age / 3.0) * Math.max(0.0, Math.min(1.0, (p.ticks - age) / 10.0));
                MochiDraw.patch(SINK, p.at.f_82479_ - cx, p.at.f_82480_ - cy, p.at.f_82481_ - cz, p.radius, fade, age, p.seed);
            }
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
        for (Map.Entry<Integer, Held> e : HELD.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            if (user == null) {
                continue;
            }
            Held h = e.getValue();
            Vec3 look = user.m_20252_(partial);
            Vec3 hand = ownView(minecraft, user) ? ownHand(user, OWN_COIL, partial) : shoulder(minecraft, user, 0, partial).m_82549_(look.m_82490_(0.45));
            if (!far(hand, cam)) {
                double age = now - h.born;
                MochiDraw.tsuki(SINK, hand.f_82479_ - cx, hand.f_82480_ - cy, hand.f_82481_ - cz, look.f_82479_, look.f_82480_, look.f_82481_, 0.0, -1.0,
                    MochiMoves.tsukiCharge(age), age * 0.9, h.coat);
            }
        }
        for (Thrust t : THRUSTS) {
            Entity user = level.m_6815_(t.user);
            boolean own = user != null && ownView(minecraft, user);
            Vec3 hand = user == null ? t.from : own ? ownHand(user, OWN_DRILL, partial) : shoulder(minecraft, user, 0, partial).m_82549_(t.way.m_82490_(0.45));
            if (!far(hand, cam)) {
                double age = now - t.born;
                MochiDraw.tsuki(SINK, hand.f_82479_ - cx, hand.f_82480_ - cy, hand.f_82481_ - cz, t.way.f_82479_, t.way.f_82480_, t.way.f_82481_, t.reach,
                    age, t.charge, age * 0.9, t.coat);
            }
        }
        for (Yanagi y : YANAGIS) {
            Entity user = level.m_6815_(y.user);
            Vec3 feet = user != null ? user.m_20318_(partial) : y.at.m_82520_(0.0, -0.8, 0.0);
            if (far(feet, cam)) {
                continue;
            }
            Vec3 right = new Vec3(-y.way.f_82481_, 0, y.way.f_82479_);
            Vec3 hip = feet.m_82520_(0.0, 0.8, 0.0).m_82549_(right.m_82490_(0.12));
            MochiDraw.yanagi(SINK, hip.f_82479_ - cx, hip.f_82480_ - cy, hip.f_82481_ - cz, y.way.f_82479_, y.way.f_82481_, feet.f_82480_ - cy, now - y.born,
                y.seed);
        }
        for (Kaku k : KAKUS) {
            Entity user = level.m_6815_(k.user);
            if (user == null || far(k.at, cam)) {
                continue;
            }
            Vec3 c = chest(user, partial);
            MochiDraw.kaku(SINK, c.f_82479_ - cx, c.f_82480_ - cy, c.f_82481_ - cz, k.at.f_82479_ - cx, k.at.f_82480_ - cy, k.at.f_82481_ - cz,
                k.way.f_82479_, k.way.f_82481_, now - k.born, k.coat, k.seed);
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
                    MochiMoves.yakiCharge(age), age, s.coat, e.getKey());
            }
        }
        for (Fist f : FISTS.values()) {
            double age = now - f.born;
            Vec3 p = f.from.m_82549_(f.step.m_82490_(age));
            if (!far(p, cam)) {
                Vec3 u = unit(f.step);
                MochiDraw.yaki(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, u.f_82479_, u.f_82480_, u.f_82481_, age, f.coat, f.seed,
                    f.charge);
            }
        }
        for (Map.Entry<Integer, Wheel> e : WHEELS.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            if (user == null || ownView(minecraft, user)) {
                continue; // one's own wheel would close round the view: in first person one only feels the roll
            }
            Wheel w = e.getValue();
            Vec3 feet = user.m_20318_(partial);
            if (far(feet, cam)) {
                continue;
            }
            Vec3 way = flat(w.to.m_82546_(w.from));
            double rolled = feet.m_82546_(w.from).m_82526_(way);
            MochiDraw.wheel(SINK, feet.f_82479_ - cx, feet.f_82480_ + w.radius - cy, feet.f_82481_ - cz, way.f_82479_, way.f_82481_, w.radius,
                rolled / Math.max(0.3, w.radius), now - w.born, e.getKey());
        }
        for (Club c : CLUBS) {
            Entity user = level.m_6815_(c.user);
            if (user == null) {
                continue;
            }
            Entity body = level.m_6815_(c.body);
            Vec3 at = body != null ? body.m_20318_(partial) : c.at;
            Vec3 from = shoulder(minecraft, user, 0, partial);
            if (!far(from, cam)) {
                MochiDraw.club(SINK, from.f_82479_ - cx, from.f_82480_ - cy, from.f_82481_ - cz, at.f_82479_ - cx, at.f_82480_ - cy, at.f_82481_ - cz,
                    now - c.born, c.coat, c.seed);
            }
        }
        for (Map.Entry<Integer, Anemone> e : ANEMONES.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            if (user == null) {
                continue;
            }
            Anemone n = e.getValue();
            Vec3 feet = user.m_20318_(partial);
            if (far(feet, cam)) {
                continue;
            }
            double age = now - n.born;
            double grow = Math.min(1.0, age / ANEMONE_FADE) * (n.ended >= 0 ? Math.max(0.0, 1.0 - (now - n.ended) / ANEMONE_FADE) : 1.0);
            int count = 0;
            for (Reach r : REACHES) {
                if (r.user != e.getKey() || count >= MOST) {
                    continue;
                }
                REACH[count * 5] = r.ring;
                REACH[count * 5 + 1] = r.at.f_82479_ - cx;
                REACH[count * 5 + 2] = r.at.f_82480_ - cy;
                REACH[count * 5 + 3] = r.at.f_82481_ - cz;
                REACH[count * 5 + 4] = now - r.born;
                count++;
            }
            double down = ownView(minecraft, user) ? OWN_RINGS_DOWN : 0.0;
            MochiDraw.anemone(SINK, feet.f_82479_ - cx, feet.f_82480_ - down - cy, feet.f_82481_ - cz, age, grow, REACH, count, n.coat, e.getKey());
        }
        for (Glob g : GLOBS.values()) {
            double age = now - g.born;
            Vec3 p = g.from.m_82549_(g.step.m_82490_(age));
            if (!far(p, cam)) {
                Vec3 u = unit(g.step);
                MochiDraw.glob(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, u.f_82479_, u.f_82480_, u.f_82481_, age, (int) g.born);
            }
        }
        for (Wave w : WAVES) {
            if (far(w.at, cam) && far(w.at.m_82549_(w.way.m_82490_(w.range)), cam)) {
                continue;
            }
            double age = now - w.born;
            double travelled = 2.0 + MochiMoves.NAGARE_SPEED * Math.min(age, w.ticks);
            double height = MochiMoves.NAGARE_HEIGHT * Math.min(1.0, age / 4.0) * Math.max(0.0, Math.min(1.0, 1.0 - (age - w.ticks) / SINK_TICKS));
            if (height > 0.05) {
                MochiDraw.wave(SINK, w.at.f_82479_ - cx, w.at.f_82480_ - cy, w.at.f_82481_ - cz, w.way.f_82479_, w.way.f_82481_, travelled,
                    MochiMoves.NAGARE_WIDTH, height, age, w.seed);
            }
        }
        for (Walls w : WALLS) {
            if (!far(w.at, cam)) {
                MochiDraw.walls(SINK, w.at.f_82479_ - cx, w.at.f_82480_ - cy, w.at.f_82481_ - cz, w.way.f_82479_, w.way.f_82481_, w.apart, now - w.born,
                    w.seed);
            }
        }
        for (Rain r : RAINS) {
            if (!far(r.at, cam)) {
                MochiDraw.rain(SINK, r.at.f_82479_ - cx, r.at.f_82480_ - cy, r.at.f_82481_ - cz, r.radius, MochiMoves.AMADARE_SKY, MochiMoves.AMADARE_COUNT,
                    MochiMoves.AMADARE_EVERY, now - r.born, r.seed);
            }
        }
        for (Donuts d : DONUTS) {
            Entity user = level.m_6815_(d.user);
            if (user == null) {
                continue;
            }
            Vec3 feet = user.m_20318_(partial);
            if (far(feet, cam)) {
                continue;
            }
            double age = now - d.born;
            double grow = Math.min(1.0, age / 6.0) * Math.max(0.0, Math.min(1.0, (d.ticks - age) / 6.0));
            MochiDraw.donuts(SINK, feet.f_82479_ - cx, feet.f_82480_ - cy, feet.f_82481_ - cz, d.at.f_82479_ - cx, d.at.f_82480_ - cy, d.at.f_82481_ - cz,
                age, grow, MochiMoves.DONUT_FIRST, MochiMoves.DONUT_EVERY, d.coat, d.seed);
        }
        for (Dome d : DOMES) {
            if (far(d.at, cam)) {
                continue;
            }
            double age = now - d.born;
            double rise = Math.min(1.0, age / 10.0) * Math.max(0.0, Math.min(1.0, 1.0 - (age - d.ticks) / SINK_TICKS));
            double crush = Math.max(0.0, Math.min(1.0, (age - d.ticks + 8) / 8.0));
            MochiDraw.dome(SINK, d.at.f_82479_ - cx, d.at.f_82480_ - cy, d.at.f_82481_ - cz, d.radius, rise, crush, age, d.seed);
        }
        for (Hole h : HOLES) {
            Entity user = level.m_6815_(h.user);
            if (user != null && ownView(minecraft, user) || far(h.at, cam)) {
                continue; // one's own body's holes would open round the view
            }
            MochiDraw.hole(SINK, h.at.f_82479_ - cx, h.at.f_82480_ - cy, h.at.f_82481_ - cz, h.way.f_82479_, h.way.f_82480_, h.way.f_82481_, now - h.born);
        }
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
        HELD.clear();
        THRUSTS.clear();
        YANAGIS.clear();
        KAKUS.clear();
        SPLATS.clear();
        BOOMS.clear();
        SWELLS.clear();
        FISTS.clear();
        WHEELS.clear();
        CLUBS.clear();
        ANEMONES.clear();
        REACHES.clear();
        GLOBS.clear();
        STUCKS.clear();
        PATCHES.clear();
        AWAKES.clear();
        WAVES.clear();
        WALLS.clear();
        RAINS.clear();
        DONUTS.clear();
        DOMES.clear();
        HOLES.clear();
        carry = null;
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
