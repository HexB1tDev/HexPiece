package com.hexhaki.piece.client.horo;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.client.fx.Tumbles;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.horo.AstralArt;
import com.hexhaki.piece.horo.HoroArt;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.HoroMoves;
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
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.3.6: the Horo Horo no Mi as every client sees it (server/HoroMoves sends S2CUoFx kinds 220 to 239; client/dragon/UoFx hands them
 * here), drawn once a frame after the particles through the game's own debugQuads type (alpha blended, depth tested, no culling: the
 * owner's device), eye-centred (horo/HoroArt, horo/AstralArt): Negative Hollows flying, despair over whoever they passed through
 * (following the body, over it when it lies down), Mini Hollows flying to their foes and clinging, the Toku Hollow drifting, bursts,
 * Ghost Network's watchers circling their user; Astral Projection's spirit (the user's own body, ghost-white and see-through: the
 * player is not drawn the game's way meanwhile) and the body it left standing. The attack button is given back to the game's punch.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class HoroFx {
    private static long clock;
    private static boolean kitAdded;

    /** A Negative Hollow flying: from, its step a tick, its head, how long, since when. */
    record Flight(Vec3 from, Vec3 step, double size, int ticks, long born) {
    }

    /** Despair over a body: since when, how long. */
    record Gloom(long born, int ticks) {
    }

    /** A Mini Hollow: whose, from, where it waits with no one to cling to, whom it clings to (-1 no one), its head, life, since when. */
    record Mini(int user, Vec3 from, Vec3 rest, int target, double size, int life, long born) {
    }

    /** The Toku Hollow drifting: from, its drift a tick, its head, its longest life, since when. */
    record Toku(Vec3 from, Vec3 step, double size, int life, long born) {
    }

    /** A burst: where, how big, since when. */
    record Burst(Vec3 at, double size, int seed, long born) {
    }

    /** Ghost Network round a user: since when, how long. */
    record Watch(long born, int ticks) {
    }

    /** Astral Projection: where the body stands (its feet), its facing (degrees), since when, how long. */
    record Astral(Vec3 feet, float yaw, long born, int ticks) {
    }

    static final int MOST = 64;
    static final Map<Integer, Flight> FLIGHTS = new HashMap<>();
    static final Map<Integer, Gloom> GLOOMS = new HashMap<>();
    static final Map<Integer, Mini> MINIS = new HashMap<>();
    static final Map<Integer, Toku> TOKUS = new HashMap<>();
    private static final List<Burst> BURSTS = new ArrayList<>();
    static final Map<Integer, Watch> WATCHES = new HashMap<>();
    static final Map<Integer, Astral> ASTRALS = new HashMap<>();
    static final double FAR = 160.0;

    private HoroFx() {
    }

    static <T> void put(Map<Integer, T> map, int key, T value) {
        if (map.size() < MOST || map.containsKey(key)) {
            map.put(key, value);
        }
    }

    public static void accept(S2CUoFx m) {
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        Vec3 a = new Vec3(m.ax(), m.ay(), m.az());
        switch (m.kind()) {
            case HoroMoves.GHOST -> put(FLIGHTS, m.id(), new Flight(at, a, m.size(), m.ticks(), clock));
            case HoroMoves.DESPAIR -> put(GLOOMS, m.id(), new Gloom(clock, m.ticks()));
            case HoroMoves.MINI -> put(MINIS, m.id(), new Mini(m.user(), at, a, (int) m.bx(), m.size(), m.ticks(), clock));
            case HoroMoves.MINI_POP -> {
                MINIS.remove(m.id());
                if (m.bx() < 0.5) {
                    burst(at, 1.0, m.id());
                }
            }
            case HoroMoves.TOKU -> put(TOKUS, m.id(), new Toku(at, a, m.size(), m.ticks(), clock));
            case HoroMoves.TOKU_BOOM -> {
                TOKUS.remove(m.id());
                if (m.bx() < 0.5) {
                    burst(at, m.size(), m.id());
                }
            }
            case HoroMoves.NETWORK -> {
                if (m.ticks() > 0) {
                    put(WATCHES, m.user(), new Watch(clock, m.ticks()));
                } else {
                    WATCHES.remove(m.user());
                }
            }
            case HoroMoves.ASTRAL -> {
                if (m.ticks() > 0) {
                    put(ASTRALS, m.user(), new Astral(at, m.ax(), clock, m.ticks()));
                } else if (ASTRALS.remove(m.user()) != null) {
                    // back in the body: a soft puff of ghost smoke where the spirit goes into it
                    burst(at.m_82520_(0.0, 1.1, 0.0), 0.8, m.user());
                }
            }
            default -> {
            }
        }
    }

    static void burst(Vec3 at, double size, int seed) {
        if (BURSTS.size() >= MOST) {
            BURSTS.remove(0);
        }
        BURSTS.add(new Burst(at, size, seed, clock));
    }

    /** Whether {@code id} is out of their body as a spirit now (the game does not draw them: the spirit is drawn here). */
    static boolean spirit(int id) {
        return ASTRALS.containsKey(id);
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
            ClientKits.KITS.put(Fruit.HORO, new HoroClient()); // the original has no client kit for it
        }
        if (FLIGHTS.isEmpty() && GLOOMS.isEmpty() && MINIS.isEmpty() && TOKUS.isEmpty() && BURSTS.isEmpty() && WATCHES.isEmpty()
            && ASTRALS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.m_91104_()) {
            return;
        }
        FLIGHTS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().ticks + 1);
        GLOOMS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().ticks + 1 || entity(level, e.getKey()) == null);
        MINIS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().life + 20);
        TOKUS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().life + 20);
        BURSTS.removeIf(b -> clock - b.born > (b.size > 2.5 ? HoroArt.BOOM_LIFE : HoroArt.POP_LIFE) + 1);
        WATCHES.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().ticks + 1 || entity(level, e.getKey()) == null);
        ASTRALS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().ticks + 40);
    }

    static Entity entity(ClientLevel level, int id) {
        return level.m_6815_(id);
    }

    /** The attack button the game's own punch with the Horo fruit (the original's fruit keys take it). */
    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
    public static void punch(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || !event.isCanceled()) {
            return;
        }
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        if (self == null || spirit(self.m_19879_()) || !com.hexhaki.fruit.client.FruitClient.active()
            || com.hexhaki.fruit.client.FruitClient.fruit(self.m_19879_()) != Fruit.HORO || !self.m_21205_().m_41619_()) {
            return;
        }
        event.setCanceled(false);
        event.setSwingHand(true);
    }

    /** A spirit is not drawn the game's way (no armour floating about): {@link #spirits} draws it. */
    @SubscribeEvent
    public static void asSpirit(RenderPlayerEvent.Pre event) {
        if (!ASTRALS.isEmpty() && spirit(event.getEntity().m_19879_())) {
            event.setCanceled(true);
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

    /**
     * A body's skin (the spirit's and the one left behind) through the game's translucent entity type: the spirit full bright, the body
     * lit by the world where it stands and shaded by its faces' normals, as the game lights a player.
     */
    static final class SkinWriter implements AstralArt.Skin {
        MultiBufferSource.BufferSource buffers;
        final float[] m = new float[12];
        final float[] n = new float[9];
        RenderType type;
        VertexConsumer out;
        int light = 0xF000F0;
        boolean shaded;

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
            float x = this.shaded ? (float) nx : 0.0F;
            float y = this.shaded ? (float) ny : 1.0F;
            float z = this.shaded ? (float) nz : 0.0F;
            for (int k = 0; k < 4; k++) {
                com.hexhaki.piece.client.render.Bulk.vertex(this.out, this.m, this.n, (float) xyz[k * 3], (float) xyz[k * 3 + 1],
                    (float) xyz[k * 3 + 2], r, g, b, a, uv[k * 2], uv[k * 2 + 1], net.minecraft.client.renderer.texture.OverlayTexture.f_118083_,
                    this.light, x, y, z);
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
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || FLIGHTS.isEmpty() && GLOOMS.isEmpty() && MINIS.isEmpty()
            && TOKUS.isEmpty() && BURSTS.isEmpty() && WATCHES.isEmpty() && ASTRALS.isEmpty()) {
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
            // the bodies' skins first, each type ended before the next is asked for (the game's shared builder must not change its
            // vertex format under the light's writer: 2.9.0's lesson with the Light clones), then the light in one batch
            if (!ASTRALS.isEmpty()) {
                SKIN.buffers = buffers;
                com.hexhaki.piece.client.render.Bulk.read(pose.m_85850_(), SKIN.m, SKIN.n);
                for (Map.Entry<Integer, Astral> e : ASTRALS.entrySet()) {
                    skins(minecraft, level, e.getKey(), e.getValue(), cam, partial);
                    SKIN.end();
                }
            }
            SINK.buffer = buffers.m_6299_(type);
            draw(minecraft, level, cam, partial);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: a Horo fruit move could not be drawn", failure);
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

    private static final double[] OUT = new double[6];
    private static final double[] WAY = new double[4];
    private static final float[][] POSE = new float[8][8];

    static double smooth(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return t * t * (3.0 - 2.0 * t);
    }

    static void draw(Minecraft minecraft, ClientLevel level, Vec3 cam, float partial) {
        double cx = cam.f_82479_;
        double cy = cam.f_82480_;
        double cz = cam.f_82481_;
        double now = clock + partial;
        // Negative Hollows: off along their way, weaving a little, fading in and out
        for (Map.Entry<Integer, Flight> e : FLIGHTS.entrySet()) {
            Flight f = e.getValue();
            double age = now - f.born;
            Vec3 p = f.from.m_82549_(f.step.m_82490_(age));
            if (far(p, cam)) {
                continue;
            }
            double l = Math.max(1.0E-6, f.step.m_82553_());
            double sx = -f.step.f_82481_ / l;
            double sz = f.step.f_82479_ / l;
            double weave = 0.18 * Math.sin(age * 0.3 + e.getKey());
            double alpha = Math.min(1.0, age / 3.0) * Math.min(1.0, (f.ticks - age) / 6.0);
            HoroArt.ghost(SINK, p.f_82479_ + sx * weave - cx, p.f_82480_ + 0.1 * Math.sin(age * 0.23 + e.getKey()) - cy,
                p.f_82481_ + sz * weave - cz, f.step.f_82479_, f.step.f_82480_, f.step.f_82481_, f.size, HoroArt.NEGATIVE, age, alpha, e.getKey());
        }
        // despair over each body passed through, following it (over it as it lies down)
        for (Map.Entry<Integer, Gloom> e : GLOOMS.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof LivingEntity body)) {
                continue;
            }
            Vec3 feet = body.m_20318_(partial);
            if (far(feet, cam)) {
                continue;
            }
            Gloom g = e.getValue();
            double age = now - g.born;
            double h = body.m_20206_();
            double w = body.m_20205_();
            double over = Tumbles.lying(e.getKey(), partial, WAY);
            double along = over > 0.0 ? h * (1.0 - 0.4 * Math.sin(over)) : h;
            double slide = over > 0.0 ? WAY[2] : 0.0;
            double lift = over > 0.0 ? w * 0.5 * WAY[3] : 0.0;
            double ox = over > 0.0 ? WAY[0] * (slide + along * Math.sin(over)) : 0.0;
            double oz = over > 0.0 ? WAY[1] * (slide + along * Math.sin(over)) : 0.0;
            double top = Math.max(along * Math.cos(over) + lift, 0.45);
            HoroArt.gloom(SINK, feet.f_82479_ + ox - cx, feet.f_82480_ + top - cy, feet.f_82481_ + oz - cz, w, h, age, (g.ticks - age) / g.ticks);
        }
        // Mini Hollows: flying from the user's chest to whom they cling (or to their waiting place), then holding on, bobbing
        for (Map.Entry<Integer, Mini> e : MINIS.entrySet()) {
            Mini mini = e.getValue();
            int seed = e.getKey();
            double age = now - mini.born;
            Entity target = mini.target >= 0 ? level.m_6815_(mini.target) : null;
            double ex;
            double ey;
            double ez;
            double fx;
            double fy;
            double fz;
            if (target != null) {
                Vec3 feet = target.m_20318_(partial);
                HoroArt.cling(seed, age, target.m_20205_(), target.m_20206_(), OUT);
                ex = feet.f_82479_ + OUT[0];
                ey = feet.f_82480_ + OUT[1];
                ez = feet.f_82481_ + OUT[2];
                fx = OUT[3];
                fy = OUT[4];
                fz = OUT[5];
            } else {
                ex = mini.rest.f_82479_;
                ey = mini.rest.f_82480_ + 0.08 * Math.sin(age * 0.2 + seed);
                ez = mini.rest.f_82481_;
                fx = mini.rest.f_82479_ - mini.from.f_82479_;
                fy = 0.0;
                fz = mini.rest.f_82481_ - mini.from.f_82481_;
            }
            double t = Math.min(1.0, age / HoroArt.MINI_FLY);
            double s = smooth(t);
            double x = mini.from.f_82479_ + (ex - mini.from.f_82479_) * s;
            double y = mini.from.f_82480_ + (ey - mini.from.f_82480_) * s + 0.8 * Math.sin(Math.PI * t);
            double z = mini.from.f_82481_ + (ez - mini.from.f_82481_) * s;
            if (t < 1.0) {
                // in flight: facing the way it flies
                fx = ex - mini.from.f_82479_;
                fy = 0.0;
                fz = ez - mini.from.f_82481_;
            }
            if (x - cx > FAR || cx - x > FAR || z - cz > FAR || cz - z > FAR) {
                continue;
            }
            HoroArt.ghost(SINK, x - cx, y - cy, z - cz, fx, fy, fz, mini.size, HoroArt.MINI, age, Math.min(1.0, age / 3.0), seed);
        }
        // the Toku Hollow drifting along, bobbing
        for (Map.Entry<Integer, Toku> e : TOKUS.entrySet()) {
            Toku t = e.getValue();
            double age = now - t.born;
            Vec3 p = t.from.m_82549_(t.step.m_82490_(Math.min(age, t.life)));
            if (far(p, cam)) {
                continue;
            }
            HoroArt.ghost(SINK, p.f_82479_ - cx, p.f_82480_ + 0.15 * Math.sin(age * 0.12) - cy, p.f_82481_ - cz, t.step.f_82479_, t.step.f_82480_,
                t.step.f_82481_, t.size, HoroArt.TOKU, age, Math.min(1.0, age / 5.0), e.getKey());
        }
        // Ghost Network: the watchers circling their user
        for (Map.Entry<Integer, Watch> e : WATCHES.entrySet()) {
            Entity user = level.m_6815_(e.getKey());
            if (user == null) {
                continue;
            }
            Vec3 feet = user.m_20318_(partial);
            if (far(feet, cam)) {
                continue;
            }
            Watch w = e.getValue();
            double age = now - w.born;
            double alpha = Math.min(1.0, age / 8.0) * Math.min(1.0, (w.ticks - age) / 10.0) * 0.9;
            for (int k = 0; k < HoroArt.WATCHERS; k++) {
                HoroArt.watch(k, age, OUT);
                HoroArt.ghost(SINK, feet.f_82479_ + OUT[0] - cx, feet.f_82480_ + OUT[1] - cy, feet.f_82481_ + OUT[2] - cz, OUT[3], OUT[4], OUT[5],
                    HoroArt.WATCHER_SIZE, HoroArt.MINI, age, alpha, k);
            }
        }
        // the spirits' light (their skins went first)
        for (Map.Entry<Integer, Astral> e : ASTRALS.entrySet()) {
            if (level.m_6815_(e.getKey()) instanceof AbstractClientPlayer player && !ownView(minecraft, player)) {
                double age = now - e.getValue().born;
                Vec3 feet = player.m_20318_(partial);
                if (!far(feet, cam)) {
                    spiritPose(player, age, partial);
                    AstralArt.spirit(null, SINK, POSE, place(feet, cam, bodyYaw(player, partial)), Math.min(1.0, age / 6.0));
                }
            }
        }
        for (Burst b : BURSTS) {
            if (!far(b.at, cam)) {
                HoroArt.burst(SINK, b.at.f_82479_ - cx, b.at.f_82480_ - cy, b.at.f_82481_ - cz, b.size, now - b.born, b.seed);
            }
        }
    }

    /** One spirit's skin and the body it left behind, in the player's own skin. */
    static void skins(Minecraft minecraft, ClientLevel level, int id, Astral astral, Vec3 cam, float partial) {
        if (!(level.m_6815_(id) instanceof AbstractClientPlayer player)) {
            return;
        }
        SKIN.use(player.m_108560_());
        if (!far(astral.feet, cam)) {
            SKIN.shaded = true;
            SKIN.light = LevelRenderer.m_109541_(level, BlockPos.m_274561_(astral.feet.f_82479_, astral.feet.f_82480_ + 1.0, astral.feet.f_82481_));
            AstralArt.asleep(POSE);
            AstralArt.left(SKIN, POSE, place(astral.feet, cam, Math.toRadians(astral.yaw)));
        }
        Vec3 feet = player.m_20318_(partial);
        if (!ownView(minecraft, player) && !far(feet, cam)) {
            SKIN.shaded = false;
            SKIN.light = 0xF000F0;
            spiritPose(player, clock + partial - astral.born, partial);
            AstralArt.spirit(SKIN, null, POSE, place(feet, cam, bodyYaw(player, partial)), Math.min(1.0, (clock + partial - astral.born) / 6.0));
        }
    }

    /** Whether this is one's own spirit seen from inside its head (first person: not drawn, as the game draws no body of one's own). */
    static boolean ownView(Minecraft minecraft, AbstractClientPlayer player) {
        return player == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == net.minecraft.client.CameraType.FIRST_PERSON;
    }

    static double bodyYaw(AbstractClientPlayer player, float partial) {
        return Math.toRadians(player.f_20884_ + wrap(player.f_20883_ - player.f_20884_) * partial);
    }

    /** The spirit's pose: floating, its head turned as the player looks. */
    static void spiritPose(AbstractClientPlayer player, double age, float partial) {
        float body = player.f_20884_ + wrap(player.f_20883_ - player.f_20884_) * partial;
        float head = player.f_20886_ + wrap(player.f_20885_ - player.f_20886_) * partial;
        float pitch = player.f_19860_ + (player.m_146909_() - player.f_19860_) * partial;
        // 3.4.2: its arms the game's own (hanging, swinging as it moves, the right one swung by its punch), not the ghost's raised arms
        double dx = player.m_20185_() - player.f_19854_;
        double dz = player.m_20189_() - player.f_19856_;
        double speed = Math.min(1.0, Math.sqrt(dx * dx + dz * dz) / 0.25);
        AstralArt.floating(age, pitch, wrap(head - body), speed, player.m_21324_(partial), POSE);
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

    /** Feet at {@code feet}, facing {@code yaw} (radians, the game's way), eye-relative. */
    static StyleShow.Place place(Vec3 feet, Vec3 cam, double yaw) {
        return new StyleShow.Place(feet.f_82479_ - cam.f_82479_, feet.f_82480_ - cam.f_82480_, feet.f_82481_ - cam.f_82481_, yaw);
    }

    static void clear() {
        FLIGHTS.clear();
        GLOOMS.clear();
        MINIS.clear();
        TOKUS.clear();
        BURSTS.clear();
        WATCHES.clear();
        ASTRALS.clear();
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
