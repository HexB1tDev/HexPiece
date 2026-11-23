package com.hexhaki.piece.client.ito;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.horo.AstralArt;
import com.hexhaki.piece.ito.ItoArt;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.ItoMoves;
import com.hexhaki.piece.style.StyleShow;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
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
 * White, God Thread, the sky steps and the stitches; the Black Knight (the user's own skin, pale, through the game's translucent entity
 * type before the light's batch) walking where the server last put it and cutting.
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

    record Goshiki(Vec3 at, Vec3 way, double spread, long born, int life, boolean coat, int seed) {
    }

    record Heat(int user, Vec3 at, Vec3 way, double length, long born, int life, boolean coat, int seed) {
    }

    record Held(int user, long born, long until, boolean coat) {
    }

    record Web(Vec3 at, Vec3 facing, double radius, long born, int life, boolean coat) {
    }

    record Cage(Vec3 at, double from, double to, long born, int life, boolean coat, long ended) {
    }

    record Wave(Vec3 at, double reach, long born, int life, boolean coat) {
    }

    record Thread(Vec3 target, Vec3 origin, long born, int flight) {
    }

    record Sky(Vec3 at, long born, boolean coat, int seed) {
    }

    /** The Black Knight: where it was and is going (the server's last two places), its facing, what it does, since when. */
    static final class Knight {
        Vec3 from;
        Vec3 to;
        float yaw;
        int action;
        long actionAt;
        long movedAt;
        long until;
        long born;
        boolean coat;
    }

    static final int MOST = 64;
    private static final List<Lash> LASHES = new ArrayList<>();
    static final Map<Integer, Bullet> BULLETS = new HashMap<>();
    private static final List<Hit> HITS = new ArrayList<>();
    private static final List<Goshiki> GOSHIKIS = new ArrayList<>();
    private static final List<Heat> HEATS = new ArrayList<>();
    static final Map<Integer, Held> HELD = new HashMap<>();
    static final Map<Integer, Web> WEBS = new HashMap<>();
    static final Map<Integer, Knight> KNIGHTS = new HashMap<>();
    static final Map<Integer, Cage> CAGES = new HashMap<>();
    private static final List<Wave> WAVES = new ArrayList<>();
    private static final List<Thread> THREADS = new ArrayList<>();
    private static final List<Sky> SKIES = new ArrayList<>();
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
            case ItoMoves.GOSHIKI -> add(GOSHIKIS, new Goshiki(at, a, m.size(), clock, m.ticks(), coat, m.id()));
            case ItoMoves.OVERHEAT -> add(HEATS, new Heat(m.user(), at, a, m.size(), clock, m.ticks(), coat, m.id()));
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
            case ItoMoves.KNIGHT -> knight(m, at, coat);
            case ItoMoves.CAGE -> {
                if (m.ticks() > 0) {
                    put(CAGES, m.user(), new Cage(at, m.size(), m.by(), clock, m.ticks(), coat, -1));
                } else {
                    Cage c = CAGES.get(m.user());
                    if (c != null && c.ended < 0) {
                        CAGES.put(m.user(), new Cage(c.at, c.from, c.to, c.born, c.life, c.coat, clock));
                    }
                }
            }
            case ItoMoves.WAVE -> add(WAVES, new Wave(at, m.size(), clock, m.ticks(), coat));
            case ItoMoves.THREAD -> add(THREADS, new Thread(at, a, clock, m.ticks()));
            case ItoMoves.SKY -> add(SKIES, new Sky(at, clock, coat, m.user() * 7 + (int) m.bx()));
            case ItoMoves.STITCH -> put(STITCHES, m.user(), new long[] {clock, m.ticks()});
            default -> {
            }
        }
    }

    static void knight(S2CUoFx m, Vec3 at, boolean coat) {
        if (m.ticks() <= 0) {
            KNIGHTS.remove(m.user());
            return;
        }
        Knight k = KNIGHTS.get(m.user());
        if (k == null) {
            if (KNIGHTS.size() >= MOST) {
                return;
            }
            k = new Knight();
            k.from = at;
            k.to = at;
            k.born = clock;
            KNIGHTS.put(m.user(), k);
        } else {
            k.from = position(k, 0.0F);
            k.to = at;
        }
        k.movedAt = clock;
        k.yaw = m.ax();
        k.until = clock + m.ticks();
        k.coat = coat;
        int action = (int) m.ay();
        if (action != 0) {
            k.action = action;
            k.actionAt = clock;
        }
    }

    /** The knight's feet this frame: between the server's last two places. */
    static Vec3 position(Knight k, float partial) {
        double t = Math.max(0.0, Math.min(1.0, (clock + partial - k.movedAt) / (double) ItoMoves.KNIGHT_SEND));
        return k.from.m_82549_(k.to.m_82546_(k.from).m_82490_(t));
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
        HEATS.removeIf(h -> clock - h.born > h.life + 1);
        HELD.entrySet().removeIf(e -> clock > e.getValue().until + 2 || level.m_6815_(e.getKey()) == null);
        WEBS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().life + 1);
        KNIGHTS.entrySet().removeIf(e -> clock > e.getValue().until + 20);
        CAGES.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().life + 1
            || e.getValue().ended >= 0 && clock - e.getValue().ended > CAGE_FADE);
        WAVES.removeIf(w -> clock - w.born > w.life + 1);
        THREADS.removeIf(t -> clock - t.born > t.flight + 5);
        SKIES.removeIf(s -> clock - s.born > ItoArt.SKY_LIFE + 1);
        STITCHES.entrySet().removeIf(e -> clock - e.getValue()[0] > e.getValue()[1] + 1);
    }

    static boolean empty() {
        return LASHES.isEmpty() && BULLETS.isEmpty() && HITS.isEmpty() && GOSHIKIS.isEmpty() && HEATS.isEmpty() && HELD.isEmpty()
            && WEBS.isEmpty() && KNIGHTS.isEmpty() && CAGES.isEmpty() && WAVES.isEmpty() && THREADS.isEmpty() && SKIES.isEmpty()
            && STITCHES.isEmpty();
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

    /** The knights' skins through the game's translucent entity type, lit by the world where they stand. */
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
            float r = ((argb >>> 16) & 255) / 255.0F;
            float g = ((argb >>> 8) & 255) / 255.0F;
            float b = (argb & 255) / 255.0F;
            float a = ((argb >>> 24) & 255) / 255.0F;
            for (int k = 0; k < 4; k++) {
                com.hexhaki.piece.client.render.Bulk.vertex(this.out, this.m, this.n, (float) xyz[k * 3], (float) xyz[k * 3 + 1],
                    (float) xyz[k * 3 + 2], r, g, b, a, uv[k * 2], uv[k * 2 + 1], net.minecraft.client.renderer.texture.OverlayTexture.f_118083_,
                    this.light, (float) nx, (float) ny, (float) nz);
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
            // the knights' skins first, each type ended before the next is asked for (2.9.0's lesson with the Light clones), then the
            // light in one batch
            if (!KNIGHTS.isEmpty()) {
                SKIN.buffers = buffers;
                com.hexhaki.piece.client.render.Bulk.read(pose.m_85850_(), SKIN.m, SKIN.n);
                for (Map.Entry<Integer, Knight> e : KNIGHTS.entrySet()) {
                    knightSkin(level, e.getKey(), e.getValue(), cam, partial);
                    SKIN.end();
                }
            }
            SINK.buffer = buffers.m_6299_(type);
            draw(minecraft, level, cam, partial);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: an Ito fruit move could not be drawn", failure);
            clear();
        }
        SKIN.end();
        if (SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        SINK.buffer = null;
    }

    static boolean far(Vec3 at, Vec3 cam) {
        return at.m_82557_(cam) > FAR * FAR;
    }

    private static final float[][] POSE = new float[8][8];
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

    static void draw(Minecraft minecraft, ClientLevel level, Vec3 cam, float partial) {
        double cx = cam.f_82479_;
        double cy = cam.f_82480_;
        double cz = cam.f_82481_;
        double now = clock + partial;
        for (Map.Entry<Integer, Cage> e : CAGES.entrySet()) {
            Cage c = e.getValue();
            double age = now - c.born;
            double r = ItoMoves.cageRadius(c.from, c.to, age, c.life);
            double alpha = c.ended >= 0 ? Math.max(0.0, 1.0 - (now - c.ended) / CAGE_FADE) : 1.0;
            if (c.at.m_82554_(cam) < FAR + r) {
                ItoArt.cage(SINK, c.at.f_82479_ - cx, c.at.f_82480_ - cy, c.at.f_82481_ - cz, r, age, alpha, c.coat);
            }
        }
        for (Wave w : WAVES) {
            if (!far(w.at, cam)) {
                ItoArt.waves(SINK, w.at.f_82479_ - cx, w.at.f_82480_ - cy, w.at.f_82481_ - cz, w.reach, ItoMoves.WAVES, ItoMoves.WAVE_EVERY,
                    ItoMoves.WAVE_SPEED, now - w.born, w.life, w.coat);
            }
        }
        for (Goshiki g : GOSHIKIS) {
            if (!far(g.at, cam)) {
                ItoArt.goshiki(SINK, g.at.f_82479_ - cx, g.at.f_82480_ - cy, g.at.f_82481_ - cz, g.way.f_82479_, g.way.f_82481_, g.spread,
                    ItoMoves.GOSHIKI_FALL, now - g.born, g.life, g.coat, g.seed);
            }
        }
        for (Map.Entry<Integer, Web> e : WEBS.entrySet()) {
            Web w = e.getValue();
            if (!far(w.at, cam)) {
                ItoArt.web(SINK, w.at.f_82479_ - cx, w.at.f_82480_ - cy, w.at.f_82481_ - cz, w.facing.f_82479_, w.facing.f_82480_, w.facing.f_82481_,
                    w.radius, now - w.born, w.life, w.coat);
            }
        }
        for (Heat h : HEATS) {
            Entity user = level.m_6815_(h.user);
            Vec3 from = user != null ? hand(minecraft, user, partial) : h.at;
            if (!far(from, cam)) {
                ItoArt.overheat(SINK, from.f_82479_ - cx, from.f_82480_ - cy, from.f_82481_ - cz, h.way.f_82479_, h.way.f_82481_, h.length,
                    now - h.born, h.life, h.coat, h.seed);
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
        for (Map.Entry<Integer, Knight> e : KNIGHTS.entrySet()) {
            Knight k = e.getValue();
            Vec3 feet = position(k, partial);
            if (!far(feet, cam)) {
                knightPose(k, feet, partial);
                AstralArt.knight(null, SINK, POSE, place(feet, cam, k.yaw), knightAlpha(k, now), k.coat);
            }
        }
        for (Thread t : THREADS) {
            if (!far(t.target, cam)) {
                ItoArt.thread(SINK, t.target.f_82479_ - cx, t.target.f_82480_ - cy, t.target.f_82481_ - cz, t.origin.f_82479_ - cx, t.origin.f_82480_ - cy,
                    t.origin.f_82481_ - cz, ItoMoves.SPEARS, now - t.born, t.flight);
            }
        }
        for (Sky s : SKIES) {
            if (!far(s.at, cam)) {
                ItoArt.sky(SINK, s.at.f_82479_ - cx, s.at.f_82480_ - cy, s.at.f_82481_ - cz, now - s.born, s.coat, s.seed);
            }
        }
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

    static double knightAlpha(Knight k, double now) {
        return Math.min(1.0, (now - k.born) / 8.0) * Math.max(0.0, Math.min(1.0, (k.until - now) / 8.0));
    }

    /** The knight's pose: a cut (the Light fruit's sword cuts) for 8 ticks after it strikes, else walking as fast as it moves. */
    static void knightPose(Knight k, Vec3 feet, float partial) {
        double since = clock + partial - k.actionAt;
        if (k.action != 0 && since < 8.0) {
            float[][] cut = com.hexhaki.piece.client.npc.StylePoser.sample(k.action == 1 ? "pika_sword_1" : "pika_sword_2", (float) since);
            for (int i = 0; i < POSE.length; i++) {
                System.arraycopy(cut[i], 0, POSE[i], 0, POSE[i].length);
            }
            return;
        }
        double speed = k.to.m_82554_(k.from) / ItoMoves.KNIGHT_SEND;
        AstralArt.walking(clock + partial, Math.min(1.0, speed / 0.3), POSE);
    }

    static void knightSkin(ClientLevel level, int user, Knight k, Vec3 cam, float partial) {
        if (!(level.m_6815_(user) instanceof AbstractClientPlayer player)) {
            return;
        }
        Vec3 feet = position(k, partial);
        if (far(feet, cam)) {
            return;
        }
        SKIN.use(player.m_108560_());
        SKIN.light = LevelRenderer.m_109541_(level, BlockPos.m_274561_(feet.f_82479_, feet.f_82480_ + 1.0, feet.f_82481_));
        knightPose(k, feet, partial);
        AstralArt.knight(SKIN, null, POSE, place(feet, cam, k.yaw), knightAlpha(k, clock + partial), k.coat);
    }

    /** Feet at {@code feet}, facing {@code yaw} (radians, the game's way), eye-relative. */
    static StyleShow.Place place(Vec3 feet, Vec3 cam, double yaw) {
        return new StyleShow.Place(feet.f_82479_ - cam.f_82479_, feet.f_82480_ - cam.f_82480_, feet.f_82481_ - cam.f_82481_, yaw);
    }

    static void clear() {
        LASHES.clear();
        BULLETS.clear();
        HITS.clear();
        GOSHIKIS.clear();
        HEATS.clear();
        HELD.clear();
        WEBS.clear();
        KNIGHTS.clear();
        CAGES.clear();
        WAVES.clear();
        THREADS.clear();
        SKIES.clear();
        STITCHES.clear();
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
