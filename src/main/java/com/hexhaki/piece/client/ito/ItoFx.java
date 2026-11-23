package com.hexhaki.piece.client.ito;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.ito.ItoArt;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.ItoMoves;
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
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.3.7: the Ito Ito no Mi as every client sees it (server/ItoMoves sends S2CUoFx kinds 240 to 252; client/dragon/UoFx hands them
 * here), drawn once a frame after the particles through the game's own debugQuads type (alpha blended, depth tested, no culling: the
 * owner's device), eye-centred (ito/ItoArt): the lashes, bullets, hits, Goshikito, Overheat (from the user's hand as it is now),
 * Parasite's strings to the held body, Spider's Web, the Birdcage closing in (each client works out its radius from its age), Break
 * White, God Thread, the sky steps and the stitches. 3.4.2: Goshikito's rake from the posed hand, Overheat's knot on the fist while held
 * (one's own in first person in the hand's own pass, client/HandPass) and its beam, the bursts of strings, the Birdcage's stream to the
 * sky, its strings spreading from the crown and closing; the Black Knight is a real body now (client/ito/KnightRenderer draws it in
 * its weaver's skin), the strings it is woven of drawn over it here.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class ItoFx {
    private static long clock;
    private static boolean kitAdded;

    record Lash(int user, Vec3 at, Vec3 way, int which, long born, boolean coat) {
    }

    record Bullet(Vec3 from, Vec3 step, int ticks, long born, boolean coat) {
    }

    record Hit(Vec3 at, Vec3 way, double size, long born, boolean coat, int seed) {
    }

    record Goshiki(int user, Vec3 at, Vec3 way, double length, long born, int life, boolean coat, int seed) {
    }

    /** Overheat held (by user): since when, coated, its seed. */
    record Knot(long born, boolean coat, int seed, long until) {
    }

    /** Overheat fired: from where, its way (unit), how far, its charge, its flight; the rope's start follows the hand while it flies. */
    record Beam(int user, Vec3 from, Vec3 way, double length, double charge, long born, int flight, boolean coat, int seed) {
    }

    record Scatter(Vec3 at, double size, boolean heat, long born, boolean coat, int seed) {
    }

    record Held(int user, long born, long until, boolean coat) {
    }

    record Web(Vec3 at, Vec3 facing, double radius, long born, int life, boolean coat) {
    }

    /** The Birdcage standing ({@code closing} < 0) or closing (since then, over {@code life}), faded once ended. */
    record Cage(Vec3 at, double radius, long born, int life, boolean coat, long closing, long ended) {
    }

    /** The Birdcage held (by user): since when, coated. */
    record Stream(long born, boolean coat, long until) {
    }

    /** 3.5.0: the Awakening's string ground (its heights worked out once), or the Haki Awakening's round its user's feet (follow). */
    record Field(Vec3 at, double radius, long born, int life, boolean coat, boolean follow, double[] heights) {
    }

    /** 3.5.0: a spike of string out of the string ground. */
    record Spike(Vec3 at, double height, long born, int seed, boolean coat) {
    }

    /** 3.5.0: a tentacle of string at the body it takes (its tip where it last was, for the sinking back). */
    static final class Tentacle {
        final int body;
        final Vec3 root;
        final boolean tree;
        final long born;
        final int life;
        Vec3 tip;

        Tentacle(int body, Vec3 root, boolean tree, long born, int life) {
            this.body = body;
            this.root = root;
            this.tree = tree;
            this.born = born;
            this.life = life;
        }
    }

    /** A Black Knight drawn this frame (KnightRenderer tells): its feet, its height, coated; the strings it is woven of go over it. */
    record Seen(int id, Vec3 feet, double height, boolean coat) {
    }

    static final int MOST = 64;
    private static final List<Lash> LASHES = new ArrayList<>();
    static final Map<Integer, Bullet> BULLETS = new HashMap<>();
    private static final List<Hit> HITS = new ArrayList<>();
    private static final List<Goshiki> GOSHIKIS = new ArrayList<>();
    static final Map<Integer, Knot> KNOTS = new HashMap<>();
    private static final List<Beam> BEAMS = new ArrayList<>();
    private static final List<Scatter> SCATTERS = new ArrayList<>();
    static final Map<Integer, Stream> STREAMS = new HashMap<>();
    private static final List<Seen> SEEN = new ArrayList<>();
    static final Map<Integer, Held> HELD = new HashMap<>();
    static final Map<Integer, Web> WEBS = new HashMap<>();
    static final Map<Integer, Cage> CAGES = new HashMap<>();
    static final Map<Integer, Field> FIELDS = new HashMap<>();
    private static final List<Spike> SPIKES = new ArrayList<>();
    private static final List<Tentacle> TENTACLES = new ArrayList<>();
    static final Map<Integer, long[]> STITCHES = new HashMap<>();
    static final double FAR = 160.0;
    static final int CAGE_FADE = 12;

    private ItoFx() {
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

    public static void accept(S2CUoFx m) {
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        Vec3 a = new Vec3(m.ax(), m.ay(), m.az());
        boolean coat = m.bz() > 0.5F;
        switch (m.kind()) {
            case ItoMoves.SLASH -> add(LASHES, new Lash(m.user(), at, a, (int) m.bx(), clock, coat));
            case ItoMoves.BULLET -> put(BULLETS, m.id(), new Bullet(at, a, m.ticks(), clock, coat));
            case ItoMoves.HIT -> {
                if (m.by() > 0.5F) {
                    BULLETS.remove(m.id()); // a bullet stopped by a wall
                }
                add(HITS, new Hit(at, a, m.size(), clock, coat, m.id()));
            }
            case ItoMoves.GOSHIKI -> add(GOSHIKIS, new Goshiki(m.user(), at, a, m.size(), clock, m.ticks(), coat, m.id()));
            case ItoMoves.OVERHEAT -> heat(m, at, a, coat);
            case ItoMoves.SCATTER -> add(SCATTERS, new Scatter(at, m.size(), m.bx() > 0.5F, clock, coat, m.id()));
            case ItoMoves.PARASITE -> {
                if (m.ticks() > 0) {
                    put(HELD, m.id(), new Held(m.user(), clock, clock + m.ticks(), coat));
                } else {
                    HELD.remove(m.id());
                }
            }
            case ItoMoves.WEB -> {
                if (m.ticks() > 0) {
                    put(WEBS, m.user(), new Web(at, a, m.size(), clock, m.ticks(), coat));
                } else {
                    WEBS.remove(m.user());
                }
            }
            case ItoMoves.CAGE -> cage(m, at, coat);
            case ItoMoves.FIELD -> {
                if (m.ticks() <= 0) {
                    FIELDS.remove(m.user());
                } else {
                    boolean follow = m.bx() > 0.5F;
                    put(FIELDS, m.user(), new Field(at, m.size(), clock, m.ticks(), coat, follow, follow ? null : heights(at, m.size())));
                }
            }
            case ItoMoves.SPIKE -> add(SPIKES, new Spike(at, m.size(), clock, m.id(), coat));
            case ItoMoves.TENTACLE -> add(TENTACLES, new Tentacle(m.id(), at, m.bx() > 0.5F, clock, m.ticks()));
            case ItoMoves.SKY -> SkyRoad.accept(m); // 3.4.3: Sky Road on or off
            case ItoMoves.STITCH -> put(STITCHES, m.user(), new long[] {clock, m.ticks()});
            default -> {
            }
        }
    }

    /**
     * 3.5.0: the ground's height over {@code at} under each ray and ring of the string ground (ItoArt.stringGround), worked out once when
     * it begins: each column's top solid block within 5 blocks up or 7 down of the middle (none: the middle's height).
     */
    static double[] heights(Vec3 at, double radius) {
        net.minecraft.client.multiplayer.ClientLevel level = Minecraft.m_91087_().f_91073_;
        double[] out = new double[ItoArt.FIELD_RAYS * ItoArt.FIELD_RINGS];
        if (level == null) {
            return out;
        }
        int foot = (int) Math.floor(at.f_82480_ - 0.01);
        for (int k = 0; k < ItoArt.FIELD_RAYS; k++) {
            double a = 2 * Math.PI * k / ItoArt.FIELD_RAYS + 0.13;
            for (int j = 0; j < ItoArt.FIELD_RINGS; j++) {
                double r = radius * (j + 1) / ItoArt.FIELD_RINGS;
                int x = (int) Math.floor(at.f_82479_ + Math.cos(a) * r);
                int z = (int) Math.floor(at.f_82481_ + Math.sin(a) * r);
                double h = 0.0;
                for (int y = foot + 5; y >= foot - 7; y--) {
                    net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x, y, z);
                    net.minecraft.world.level.block.state.BlockState state = level.m_8055_(pos);
                    if (!state.m_60795_() && !state.m_60812_(level, pos).m_83281_()) {
                        h = y + 1 - at.f_82480_;
                        break;
                    }
                }
                out[k * ItoArt.FIELD_RINGS + j] = h;
            }
        }
        return out;
    }

    static void heat(S2CUoFx m, Vec3 at, Vec3 a, boolean coat) {
        int phase = Math.round(m.bx());
        if (phase == ItoMoves.HEAT_CHARGE) {
            put(KNOTS, m.user(), new Knot(clock, coat, m.id(), clock + m.ticks() + 2));
        } else if (phase == ItoMoves.HEAT_FIRE) {
            KNOTS.remove(m.user());
            double l = a.m_82553_();
            add(BEAMS, new Beam(m.user(), at, l < 1.0E-6 ? new Vec3(0, 0, 1) : a.m_82490_(1.0 / l), m.size(), m.by(), clock, m.ticks(), coat, m.id()));
        } else {
            KNOTS.remove(m.user());
        }
    }

    static void cage(S2CUoFx m, Vec3 at, boolean coat) {
        int phase = Math.round(m.bx());
        if (m.ticks() <= 0) {
            STREAMS.remove(m.user());
            Cage c = CAGES.get(m.user());
            if (c != null && c.ended < 0) {
                CAGES.put(m.user(), new Cage(c.at, c.radius, c.born, c.life, c.coat, c.closing, clock));
            }
        } else if (phase == ItoMoves.CAGE_CHARGING) {
            put(STREAMS, m.user(), new Stream(clock, coat, clock + m.ticks() + 2));
        } else if (phase == ItoMoves.CAGE_STANDING) {
            STREAMS.remove(m.user());
            put(CAGES, m.user(), new Cage(at, m.size(), clock, m.ticks(), coat, -1, -1));
        } else {
            Cage c = CAGES.get(m.user());
            put(CAGES, m.user(), new Cage(at, m.size(), c != null ? c.born : clock - (long) ItoArt.CAGE_SPREAD, m.ticks(), coat, clock, -1));
        }
    }

    /** client/ito/KnightRenderer: a Black Knight was drawn this frame (its woven strings are drawn over it after the particles). */
    static void knightDrawn(com.hexhaki.piece.ito.StringKnight knight, float partial) {
        if (SEEN.size() < MOST) {
            SEEN.add(new Seen(knight.m_19879_(), knight.m_20318_(partial), knight.m_20206_(), knight.coated()));
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
            ClientKits.KITS.put(Fruit.ITO, new ItoClient()); // the original has no client kit for it
        }
        if (empty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.m_91104_()) {
            return;
        }
        LASHES.removeIf(l -> clock - l.born > ItoArt.LASH_LIFE + 1);
        BULLETS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().ticks + 1);
        HITS.removeIf(h -> clock - h.born > ItoArt.HIT_LIFE + 1);
        GOSHIKIS.removeIf(g -> clock - g.born > g.life + 1);
        KNOTS.entrySet().removeIf(e -> clock > e.getValue().until || level.m_6815_(e.getKey()) == null);
        BEAMS.removeIf(b -> clock - b.born > b.flight + ItoArt.BREAK_LIFE + 1);
        SCATTERS.removeIf(c -> clock - c.born > ItoArt.SCATTER_LIFE + 1);
        STREAMS.entrySet().removeIf(e -> clock > e.getValue().until || level.m_6815_(e.getKey()) == null);
        HELD.entrySet().removeIf(e -> clock > e.getValue().until + 2 || level.m_6815_(e.getKey()) == null);
        WEBS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().life + 1);
        CAGES.entrySet().removeIf(e -> clock - (e.getValue().closing >= 0 ? e.getValue().closing : e.getValue().born) > e.getValue().life + 2
            || e.getValue().ended >= 0 && clock - e.getValue().ended > CAGE_FADE);
        FIELDS.values().removeIf(f -> clock - f.born > f.life + 1);
        SPIKES.removeIf(k -> clock - k.born > com.hexhaki.piece.ito.ItoAwaken.SPIKE_LIFE + 1);
        TENTACLES.removeIf(t -> clock - t.born > t.life + 1);
        STITCHES.entrySet().removeIf(e -> clock - e.getValue()[0] > e.getValue()[1] + 1);
    }

    static boolean empty() {
        return LASHES.isEmpty() && BULLETS.isEmpty() && HITS.isEmpty() && GOSHIKIS.isEmpty() && KNOTS.isEmpty() && BEAMS.isEmpty()
            && SCATTERS.isEmpty() && STREAMS.isEmpty() && SEEN.isEmpty() && HELD.isEmpty() && WEBS.isEmpty() && CAGES.isEmpty()
            && FIELDS.isEmpty() && SPIKES.isEmpty() && TENTACLES.isEmpty() && SkyRoad.empty() && STITCHES.isEmpty();
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
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: an Ito fruit move could not be drawn", failure);
            clear();
        }
        SEEN.clear();
        if (SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        SINK.buffer = null;
    }

    static boolean far(Vec3 at, Vec3 cam) {
        return at.m_82557_(cam) > FAR * FAR;
    }

    private static final double[] POINTS = new double[15];

    /** Where strings leave a user's hand this frame: in front of the view in one's own first person, else the right hand. */
    static Vec3 hand(Minecraft minecraft, Entity user, float partial) {
        Vec3 look = user.m_20252_(partial);
        Vec3 flat = new Vec3(look.f_82479_, 0, look.f_82481_);
        flat = flat.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.m_82541_();
        Vec3 right = new Vec3(-flat.f_82481_, 0, flat.f_82479_);
        if (user == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == net.minecraft.client.CameraType.FIRST_PERSON) {
            return user.m_20299_(partial).m_82549_(look.m_82490_(0.7)).m_82549_(right.m_82490_(0.32)).m_82520_(0.0, -0.3, 0.0);
        }
        return user.m_20318_(partial).m_82520_(0.0, user.m_20206_() * 0.62, 0.0).m_82549_(right.m_82490_(0.36)).m_82549_(flat.m_82490_(0.35));
    }

    /**
     * 3.4.2: where a user's strings leave the fist this frame: a player playing one of the String fruit's clips, the clip's own posed
     * fist (client/FirstPersonLight.fist: the raised hand of Goshikito and the Birdcage, the pointed fist of Overheat); else the hand.
     */
    static Vec3 fist(Minecraft minecraft, Entity user, float partial) {
        if (user instanceof net.minecraft.world.entity.player.Player player) {
            Vec3 at = com.hexhaki.piece.client.FirstPersonLight.fist(player, partial);
            if (at != null) {
                return at;
            }
        }
        return hand(minecraft, user, partial);
    }

    /** How far above one's own hand the Birdcage's stream starts in first person. */
    static final double OWN_STREAM_GAP = 3.0;

    /** How far ahead of one's own view Overheat's beam starts in first person. */
    static final double OWN_BEAM_GAP = 1.2;

    /** How big one's own Overheat knot is in first person (in the hand's pass, right by the eye). */
    static final double HAND_KNOT = 0.45;

    /** client/HandPass: one's own Overheat knot, drawn on the fist in the hand's own pass. */
    public static boolean handWanted(int id) {
        return KNOTS.containsKey(id);
    }

    public static void handPass(FireMesh.Sink sink, int id, float partial) {
        Knot k = KNOTS.get(id);
        if (k == null) {
            return;
        }
        double[] v = com.hexhaki.piece.pika.LightArt.firstPersonFingertip(-0.05);
        double age = clock - k.born + partial;
        ItoArt.knot(sink, v[0], v[1], v[2], 0, 0, -1, ItoMoves.heatCharge(age), age, k.coat, k.seed, HAND_KNOT);
    }

    static void draw(Minecraft minecraft, ClientLevel level, Vec3 cam, float partial) {
        double cx = cam.f_82479_;
        double cy = cam.f_82480_;
        double cz = cam.f_82481_;
        double now = clock + partial;
        for (Map.Entry<Integer, Cage> e : CAGES.entrySet()) {
            Cage c = e.getValue();
            double age = now - c.born;
            boolean closing = c.closing >= 0;
            double r = closing ? ItoMoves.cageRadius(c.radius, now - c.closing, c.life) : c.radius;
            double tension = closing ? Math.min(1.0, (now - c.closing) / Math.max(1.0, c.life)) : 0.0;
            double alpha = c.ended >= 0 ? Math.max(0.0, 1.0 - (now - c.ended) / CAGE_FADE) : 1.0;
            if (c.at.m_82554_(cam) < FAR + r) {
                ItoArt.cage(SINK, c.at.f_82479_ - cx, c.at.f_82480_ - cy, c.at.f_82481_ - cz, r, Math.min(1.0, age / ItoArt.CAGE_SPREAD), tension,
                    alpha, c.coat);
            }
        }
        for (Map.Entry<Integer, Stream> e : STREAMS.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            if (user == null) {
                continue;
            }
            Stream st = e.getValue();
            double age = now - st.born;
            double radius = ItoMoves.cageSize(age);
            Vec3 feet = user.m_20318_(partial);
            Vec3 from = fist(minecraft, user, partial);
            if (ownView(minecraft, user)) {
                from = from.m_82520_(0.0, OWN_STREAM_GAP, 0.0); // right by one's own eye the strings would be blades across the view
            }
            if (feet.m_82554_(cam) < FAR + radius) {
                ItoArt.stream(SINK, from.f_82479_ - cx, from.f_82480_ - cy, from.f_82481_ - cz, feet.f_82479_ - cx, feet.f_82480_ + radius - cy,
                    feet.f_82481_ - cz, radius, age, st.coat, e.getKey());
            }
        }
        for (Map.Entry<Integer, Field> e : FIELDS.entrySet()) {
            Field f = e.getValue();
            Vec3 at = f.at;
            if (f.follow) {
                Entity user = level.m_6815_(e.getKey());
                if (user == null || !user.m_20096_()) {
                    continue; // round the feet only while they stand on the ground
                }
                at = user.m_20318_(partial);
            }
            if (!far(at, cam)) {
                ItoArt.stringGround(SINK, at.f_82479_ - cx, at.f_82480_ - cy, at.f_82481_ - cz, f.radius, f.follow ? 8 : ItoArt.FIELD_RAYS,
                    f.follow ? 2 : ItoArt.FIELD_RINGS, f.heights, now - f.born, f.life, f.coat);
            }
        }
        for (Spike k : SPIKES) {
            if (!far(k.at, cam)) {
                ItoArt.spike(SINK, k.at.f_82479_ - cx, k.at.f_82480_ - cy, k.at.f_82481_ - cz, k.height, now - k.born, k.coat, k.seed);
            }
        }
        for (Tentacle t : TENTACLES) {
            if (far(t.root, cam)) {
                continue;
            }
            double age = now - t.born;
            Entity body = level.m_6815_(t.body);
            Vec3 mid = body == null ? t.tip : body.m_20318_(partial).m_82520_(0.0, body.m_20206_() * 0.55, 0.0);
            Vec3 tip;
            if (age < com.hexhaki.piece.ito.ItoAwaken.RISE + com.hexhaki.piece.ito.ItoAwaken.LIFT) {
                if (mid == null) {
                    continue;
                }
                double g = com.hexhaki.piece.ito.ItoAwaken.grow(age);
                tip = t.root.m_82549_(mid.m_82546_(t.root).m_82490_(g));
                t.tip = tip;
            } else {
                if (t.tip == null) {
                    continue;
                }
                double w = com.hexhaki.piece.ito.ItoAwaken.withdraw(age);
                tip = t.root.m_82549_(t.tip.m_82546_(t.root).m_82490_(w));
            }
            ItoArt.tentacle(SINK, t.root.f_82479_ - cx, t.root.f_82480_ - cy, t.root.f_82481_ - cz, tip.f_82479_ - cx, tip.f_82480_ - cy,
                tip.f_82481_ - cz, age, t.tree, t.body);
        }
        for (Goshiki g : GOSHIKIS) {
            if (!far(g.at, cam)) {
                Entity user = level.m_6815_(g.user);
                Vec3 from = user != null ? fist(minecraft, user, partial) : g.at.m_82520_(0.0, 1.4, 0.0);
                ItoArt.goshiki(SINK, from.f_82479_ - cx, from.f_82480_ - cy, from.f_82481_ - cz, g.at.f_82479_ - cx, g.at.f_82480_ - cy,
                    g.at.f_82481_ - cz, g.way.f_82479_, g.way.f_82481_, g.length, now - g.born, g.life, g.coat, g.seed);
            }
        }
        for (Map.Entry<Integer, Web> e : WEBS.entrySet()) {
            Web w = e.getValue();
            if (!far(w.at, cam)) {
                // one's own web in first person, right in front of the view: fainter, so the fight can still be seen through it
                Entity user = level.m_6815_(e.getKey());
                double alpha = user != null && ownView(minecraft, user) ? 0.45 : 1.0;
                ItoArt.web(SINK, w.at.f_82479_ - cx, w.at.f_82480_ - cy, w.at.f_82481_ - cz, w.facing.f_82479_, w.facing.f_82480_, w.facing.f_82481_,
                    w.radius, now - w.born, w.life, w.coat, alpha);
            }
        }
        for (Map.Entry<Integer, Knot> e : KNOTS.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            if (user == null || ownView(minecraft, user)) {
                continue; // one's own in first person is drawn on the fist in the hand's own pass (handPass)
            }
            Knot k = e.getValue();
            Vec3 from = fist(minecraft, user, partial);
            Vec3 look = user.m_20252_(partial);
            if (!far(from, cam)) {
                ItoArt.knot(SINK, from.f_82479_ - cx, from.f_82480_ - cy, from.f_82481_ - cz, look.f_82479_, look.f_82480_, look.f_82481_,
                    ItoMoves.heatCharge(now - k.born), now - k.born, k.coat, k.seed);
            }
        }
        for (Beam b : BEAMS) {
            double age = now - b.born;
            Entity user = level.m_6815_(b.user);
            // flying, the rope leaves the hand as it is now; once it has blown up it lies where it was fired from
            Vec3 from = user != null && age <= b.flight ? fist(minecraft, user, partial) : b.from;
            Vec3 end = b.from.m_82549_(b.way.m_82490_(b.length));
            Vec3 way = end.m_82546_(from);
            double length = way.m_82553_();
            if (length < 1.0E-3 || far(from, cam) && far(end, cam)) {
                continue;
            }
            way = way.m_82490_(1.0 / length);
            boolean own = user != null && ownView(minecraft, user);
            if (own && length > OWN_BEAM_GAP + 1.0) {
                // one's own beam in first person starts a little ahead of the view (a thick rope seen from right behind the fist is a
                // flat red wedge across the screen)
                from = from.m_82549_(way.m_82490_(OWN_BEAM_GAP));
                length -= OWN_BEAM_GAP;
            }
            ItoArt.beam(SINK, from.f_82479_ - cx, from.f_82480_ - cy, from.f_82481_ - cz, way.f_82479_, way.f_82480_, way.f_82481_, length, b.charge,
                age, b.flight, b.coat, b.seed, own);
        }
        for (Scatter c : SCATTERS) {
            if (!far(c.at, cam)) {
                ItoArt.scatter(SINK, c.at.f_82479_ - cx, c.at.f_82480_ - cy, c.at.f_82481_ - cz, c.size, c.heat, now - c.born, c.coat, c.seed,
                    SCATTERS.size() > 4 || c.at.m_82554_(cam) > 40.0);
            }
        }
        for (Seen k : SEEN) {
            if (!far(k.feet, cam)) {
                ItoArt.woven(SINK, k.feet.f_82479_ - cx, k.feet.f_82480_ - cy, k.feet.f_82481_ - cz, k.height, now, k.coat, k.id);
            }
        }
        for (Lash l : LASHES) {
            Entity user = level.m_6815_(l.user);
            Vec3 from = user != null ? hand(minecraft, user, partial) : l.at;
            Vec3 way = user != null ? user.m_20252_(partial) : l.way;
            if (!far(from, cam)) {
                ItoArt.lash(SINK, from.f_82479_ - cx, from.f_82480_ - cy, from.f_82481_ - cz, way.f_82479_, way.f_82481_, ItoMoves.SLASH_REACH, l.which,
                    now - l.born, l.coat);
            }
        }
        for (Map.Entry<Integer, Bullet> e : BULLETS.entrySet()) {
            Bullet b = e.getValue();
            double age = now - b.born;
            Vec3 p = b.from.m_82549_(b.step.m_82490_(age));
            if (far(p, cam)) {
                continue;
            }
            double l = Math.max(1.0E-6, b.step.m_82553_());
            ItoArt.bullet(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, b.step.f_82479_ / l, b.step.f_82480_ / l, b.step.f_82481_ / l, age,
                b.coat);
        }
        for (Map.Entry<Integer, Held> e : HELD.entrySet()) {
            Held h = e.getValue();
            Entity user = level.m_6815_(h.user);
            if (user == null || !(level.m_6815_(e.getKey()) instanceof LivingEntity body)) {
                continue;
            }
            Vec3 feet = body.m_20318_(partial);
            if (far(feet, cam)) {
                continue;
            }
            Vec3 from = hand(minecraft, user, partial);
            double yaw = Math.toRadians(body.f_20884_ + (body.f_20883_ - body.f_20884_) * partial);
            double sx = Math.cos(yaw);
            double sz = Math.sin(yaw);
            double height = body.m_20206_();
            double w = body.m_20205_() * 0.5;
            double[] p = POINTS;
            put(p, 0, feet.f_82479_, feet.f_82480_ + height * 0.95, feet.f_82481_, cam);
            put(p, 1, feet.f_82479_ + sx * w * 1.1, feet.f_82480_ + height * 0.7, feet.f_82481_ + sz * w * 1.1, cam);
            put(p, 2, feet.f_82479_ - sx * w * 1.1, feet.f_82480_ + height * 0.7, feet.f_82481_ - sz * w * 1.1, cam);
            put(p, 3, feet.f_82479_ + sx * w * 0.5, feet.f_82480_ + height * 0.12, feet.f_82481_ + sz * w * 0.5, cam);
            put(p, 4, feet.f_82479_ - sx * w * 0.5, feet.f_82480_ + height * 0.12, feet.f_82481_ - sz * w * 0.5, cam);
            double alpha = Math.min(1.0, (h.until - now) / 6.0);
            ItoArt.parasite(SINK, from.f_82479_ - cx, from.f_82480_ - cy, from.f_82481_ - cz, p, now - h.born, Math.max(0.0, alpha), h.coat);
        }
        SkyRoad.draw(SINK, minecraft, level, cam, partial);
        for (Map.Entry<Integer, long[]> e : STITCHES.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            if (user == null || ownView(minecraft, user)) {
                continue;
            }
            Vec3 feet = user.m_20318_(partial);
            if (!far(feet, cam)) {
                ItoArt.stitch(SINK, feet.f_82479_ - cx, feet.f_82480_ + user.m_20206_() * 0.55 - cy, feet.f_82481_ - cz, user.m_20206_(),
                    now - e.getValue()[0], e.getValue()[1], e.getKey());
            }
        }
        for (Hit h : HITS) {
            if (!far(h.at, cam)) {
                ItoArt.hit(SINK, h.at.f_82479_ - cx, h.at.f_82480_ - cy, h.at.f_82481_ - cz, h.way.f_82479_, h.way.f_82480_, h.way.f_82481_, h.size,
                    now - h.born, h.coat, h.seed);
            }
        }
    }

    static void put(double[] p, int k, double x, double y, double z, Vec3 cam) {
        p[k * 3] = x - cam.f_82479_;
        p[k * 3 + 1] = y - cam.f_82480_;
        p[k * 3 + 2] = z - cam.f_82481_;
    }

    static boolean ownView(Minecraft minecraft, Entity user) {
        return user == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == net.minecraft.client.CameraType.FIRST_PERSON;
    }

    static void clear() {
        LASHES.clear();
        BULLETS.clear();
        HITS.clear();
        GOSHIKIS.clear();
        KNOTS.clear();
        BEAMS.clear();
        SCATTERS.clear();
        STREAMS.clear();
        SEEN.clear();
        HELD.clear();
        WEBS.clear();
        CAGES.clear();
        FIELDS.clear();
        SPIKES.clear();
        TENTACLES.clear();
        STITCHES.clear();
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
