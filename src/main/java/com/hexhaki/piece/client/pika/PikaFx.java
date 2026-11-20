package com.hexhaki.piece.client.pika;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.pika.LightArt;
import com.hexhaki.piece.server.PikaMoves;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
 * 2.7.8: the Pika Pika no Mi's moves as every client sees them (server/PikaMoves sends S2CUoFx kinds from 32; client/dragon/UoFx hands
 * them here): Kizaru's light in 3D (pika/LightArt), drawn once a frame after the particles through the game's own debugQuads type
 * (alpha blended, no culling: the owner's device), eye-centred, with Photon motes of light round it. Where a bead or a clone is, a
 * cut's crescent, the light-kicked one's trail: all worked out here from what the server sent.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class PikaFx {
    static final Random RANDOM = new Random();
    private static long clock;
    private static boolean kitAdded;

    record Line(Vec3 from, Vec3 to, double width, long born, int kind) {
    }

    record Spot(Vec3 at, Vec3 dir, double size, int step, long born, int kind) {
    }

    record Bead(Vec3 from, Vec3 to, int flight, double size, long born, int seed) {
    }

    record Cut(Vec3 at, Vec3 axis, double start, double sweep, double radius, long born) {
    }

    record Wave(Vec3 start, Vec3 step, int life, double width, long born) {
    }

    record Clone(Vec3 origin, Vec3 spot, int target, int index, long born) {
    }

    /** The one light-kicked across the field: who, its trail (newest first), when. */
    static final class Kicked {
        final int id;
        final long born;
        final double[] trail = new double[24 * 3];
        int count;

        Kicked(int id, long born) {
            this.id = id;
            this.born = born;
        }
    }

    /** A flying or streaming player's trail of light (newest first). */
    static final class Trail {
        final double[] points = new double[16 * 3];
        int count;
        long last;
    }

    private static final List<Line> LINES = new ArrayList<>();
    private static final List<Spot> SPOTS = new ArrayList<>();
    private static final List<Bead> BEADS = new ArrayList<>();
    private static final List<Cut> CUTS = new ArrayList<>();
    private static final List<Wave> WAVES = new ArrayList<>();
    private static final List<Clone> CLONES = new ArrayList<>();
    private static final List<Kicked> KICKED = new ArrayList<>();
    /** Per player: since when (charging, the kick's charge, the volley, Yata forming, streaming), the sword's end. */
    static final Map<Integer, Long> CHARGES = new HashMap<>();
    static final Map<Integer, Long> KICK_CHARGES = new HashMap<>();
    static final Map<Integer, Long> VOLLEYS = new HashMap<>();
    static final Map<Integer, Long> FORMS = new HashMap<>();
    static final Map<Integer, Long> STREAMS = new HashMap<>();
    static final Map<Integer, Long> SWORDS = new HashMap<>();
    private static final Map<Integer, Trail> TRAILS = new HashMap<>();
    static final double FAR = 160.0;

    private PikaFx() {
    }

    public static void accept(S2CUoFx m) {
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        Vec3 a = new Vec3(m.ax(), m.ay(), m.az());
        switch (m.kind()) {
            case PikaMoves.LASER -> LINES.add(new Line(at, a, m.size(), clock, PikaMoves.LASER));
            case PikaMoves.BEAM -> {
                LINES.add(new Line(at, a, m.size(), clock, PikaMoves.BEAM));
                motes(a, 18, 0.5);
            }
            case PikaMoves.CHARGE_ON -> CHARGES.put(m.user(), clock);
            case PikaMoves.CHARGE_OFF -> CHARGES.remove(m.user());
            case PikaMoves.BURST -> {
                SPOTS.add(new Spot(at, a, m.size(), 0, clock, PikaMoves.BURST));
                motes(at, 14, 0.4);
            }
            case PikaMoves.KICK -> {
                SPOTS.add(new Spot(at, a, m.size(), m.ticks(), clock, PikaMoves.KICK));
                if (m.size() > 0) {
                    motes(at, 6, 0.25);
                }
            }
            case PikaMoves.YATA_FORM -> FORMS.put(m.user(), clock);
            case PikaMoves.YATA_PATH -> {
                LINES.add(new Line(at, a, 1.0, clock, PikaMoves.YATA_PATH));
                FORMS.remove(m.user());
                motes(a.m_82520_(0, 1.0, 0), 10, 0.3);
            }
            case PikaMoves.VOLLEY_ON -> VOLLEYS.put(m.user(), clock);
            case PikaMoves.VOLLEY_OFF -> VOLLEYS.remove(m.user());
            case PikaMoves.BEAD -> BEADS.add(new Bead(at, a, Math.max(1, m.ticks()), m.size(), clock, m.id()));
            case PikaMoves.SWORD_ON -> SWORDS.put(m.user(), clock + m.ticks());
            case PikaMoves.SWORD_OFF -> SWORDS.remove(m.user());
            case PikaMoves.SLASH -> CUTS.add(new Cut(at, a.m_82541_(), m.bx(), m.by(), m.bz(), clock));
            case PikaMoves.WAVE -> WAVES.add(new Wave(at, a, m.ticks(), m.size(), clock));
            case PikaMoves.KICK_CHARGE_ON -> KICK_CHARGES.put(m.user(), clock);
            case PikaMoves.KICK_CHARGE_OFF -> KICK_CHARGES.remove(m.user());
            case PikaMoves.LIGHT_KICK -> {
                LINES.add(new Line(at, a, 1.4, clock, PikaMoves.YATA_PATH));
                if (m.id() >= 0) {
                    KICKED.add(new Kicked(m.id(), clock));
                    SPOTS.add(new Spot(a.m_82520_(0, 1.0, 0), new Vec3(m.bx(), m.by(), m.bz()), 2.5 + 2.0 * m.size(), 8, clock + 1,
                        PikaMoves.KICK));
                }
            }
            case PikaMoves.CLONE -> CLONES.add(new Clone(at, a, m.id(), m.ticks(), clock));
            case PikaMoves.DODGE -> SPOTS.add(new Spot(at, Vec3.f_82478_, 1.2, 0, clock, PikaMoves.DODGE));
            case PikaMoves.DIVE -> {
                LINES.add(new Line(a, at, 1.2, clock, PikaMoves.YATA_PATH));
                SPOTS.add(new Spot(at, Vec3.f_82478_, m.size(), 0, clock + 1, PikaMoves.BURST));
            }
            case PikaMoves.STREAM_ON -> STREAMS.put(m.user(), clock);
            case PikaMoves.STREAM_OFF -> STREAMS.remove(m.user());
            default -> {
            }
        }
    }

    /** Whether {@code id}'s sword of light is out (PikaClient draws it in the hand). */
    static boolean sword(int id) {
        Long until = SWORDS.get(id);
        return until != null && until > clock;
    }

    /** Whether {@code id} is gathering light in the leg. */
    static boolean kickCharging(int id) {
        return KICK_CHARGES.containsKey(id);
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
            ClientKits.KITS.put(Fruit.PIKA, new PikaClient()); // the rebuilt kit in place of the original's LightClient
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.m_91104_()) {
            return;
        }
        LINES.removeIf(l -> clock - l.born > (l.kind == PikaMoves.BEAM ? LightArt.BEAM_LIFE : LightArt.PATH_LIFE) + 2);
        SPOTS.removeIf(s -> clock - s.born > (s.kind == PikaMoves.BURST ? LightArt.BURST_LIFE : 9) + 1);
        CUTS.removeIf(c -> clock - c.born > LightArt.SLASH_LIFE + 1);
        WAVES.removeIf(w -> clock - w.born > w.life + 2);
        CLONES.removeIf(c -> clock - c.born > CLONE_LIFE + 1);
        FORMS.entrySet().removeIf(e -> clock - e.getValue() > 8);
        SWORDS.entrySet().removeIf(e -> e.getValue() < clock);
        for (Iterator<Bead> it = BEADS.iterator(); it.hasNext(); ) {
            Bead b = it.next();
            if (clock - b.born >= b.flight) {
                it.remove();
                SPOTS.add(new Spot(b.to, Vec3.f_82478_, b.size * 2.4, 0, clock, PikaMoves.BURST));
                if (RANDOM.nextInt(3) == 0) {
                    motes(b.to, 3, 0.3);
                }
            }
        }
        for (Iterator<Kicked> it = KICKED.iterator(); it.hasNext(); ) {
            Kicked k = it.next();
            Entity e = level.m_6815_(k.id);
            if (clock - k.born > 26 || e == null) {
                it.remove();
                continue;
            }
            push(k.trail, k.count, e.m_20182_().m_82520_(0, e.m_20206_() * 0.5, 0));
            k.count = Math.min(24, k.count + 1);
            if (clock % 2 == 0) {
                motes(e.m_20182_().m_82520_(0, e.m_20206_() * 0.5, 0), 2, 0.2);
            }
        }
        // trails: a player flying as light, or streaming
        for (Player player : level.m_6907_()) {
            boolean streaming = STREAMS.containsKey(player.m_19879_());
            boolean flying = player.m_150110_().f_35935_ && com.hexhaki.fruit.client.FruitClient.fruit(player.m_19879_()) == Fruit.PIKA
                && player.m_20184_().m_82553_() > 0.35;
            Trail trail = TRAILS.get(player.m_19879_());
            if (streaming || flying) {
                if (trail == null) {
                    trail = new Trail();
                    TRAILS.put(player.m_19879_(), trail);
                }
                push(trail.points, trail.count, player.m_20182_().m_82520_(0, 1.0, 0));
                trail.count = Math.min(16, trail.count + 1);
                trail.last = clock;
            } else if (trail != null) {
                trail.count = Math.max(0, trail.count - 2);
                if (trail.count == 0) {
                    TRAILS.remove(player.m_19879_());
                }
            }
        }
        // light gathering in a leg or at a fingertip throws motes
        for (Map.Entry<Integer, Long> e : KICK_CHARGES.entrySet()) {
            if (level.m_6815_(e.getKey()) instanceof Player player) {
                Vec3 foot = foot(player, 1.0F);
                mote(foot.m_82520_(r(0.3), r(0.2), r(0.3)), new Vec3(r(0.03), 0.06 + RANDOM.nextDouble() * 0.05, r(0.03)), 14);
            }
        }
    }

    static void push(double[] points, int count, Vec3 p) {
        System.arraycopy(points, 0, points, 3, Math.min(count, points.length / 3 - 1) * 3);
        points[0] = p.f_82479_;
        points[1] = p.f_82480_;
        points[2] = p.f_82481_;
    }

    // ------------------------------------------------------------------------------------------------------ where things are on a body

    /** The right fingertip (as the server places it), at {@code partial}. */
    static Vec3 fingertip(Player player, float partial) {
        Vec3 look = player.m_20252_(partial);
        Vec3 side = look.m_82537_(new Vec3(0, 1, 0));
        side = side.m_82556_() < 1.0E-6 ? new Vec3(1, 0, 0) : side.m_82541_();
        return player.m_20299_(partial).m_82549_(look.m_82490_(0.9)).m_82549_(side.m_82490_(0.36)).m_82520_(0.0, -0.22, 0.0);
    }

    /** Before the chest, where the hands meet (Yata no Kagami, the crossed arms of Yasakani no Magatama). */
    static Vec3 hands(Player player, float partial) {
        Vec3 look = player.m_20252_(partial);
        Vec3 flat = new Vec3(look.f_82479_, 0, look.f_82481_);
        flat = flat.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.m_82541_();
        return player.m_20318_(partial).m_82520_(0, 1.25, 0).m_82549_(flat.m_82490_(0.55));
    }

    /** The kicking foot, out in front (the speed-of-light kick's charge). */
    static Vec3 foot(Player player, float partial) {
        Vec3 look = player.m_20252_(partial);
        Vec3 flat = new Vec3(look.f_82479_, 0, look.f_82481_);
        flat = flat.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.m_82541_();
        Vec3 side = flat.m_82537_(new Vec3(0, 1, 0)).m_82541_();
        return player.m_20318_(partial).m_82520_(0, 0.35, 0).m_82549_(side.m_82490_(0.15)).m_82549_(flat.m_82490_(0.1));
    }

    // ------------------------------------------------------------------------------------------------------ the drawing

    /** Clones: out of the body (4 ticks), across to their foe (6), two cuts (12), gone in light (4). */
    static final int CLONE_LIFE = 26;

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || LINES.isEmpty() && SPOTS.isEmpty() && BEADS.isEmpty()
            && CUTS.isEmpty() && WAVES.isEmpty() && CLONES.isEmpty() && KICKED.isEmpty() && CHARGES.isEmpty() && KICK_CHARGES.isEmpty()
            && VOLLEYS.isEmpty() && FORMS.isEmpty() && TRAILS.isEmpty()) {
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
        SINK.buffer = buffers.m_6299_(type);
        SINK.matrix = pose.m_85850_().m_252922_();
        FireMesh.quality = Math.max(0.35, PieceFx.density());
        try {
            draw(level, event.getCamera().m_90583_(), event.getPartialTick());
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: a Light fruit move could not be drawn", failure);
            clear();
        }
        buffers.m_109912_(type);
        SINK.buffer = null;
    }

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

    static boolean far(Vec3 at, Vec3 cam) {
        return at.m_82557_(cam) > FAR * FAR;
    }

    static void draw(ClientLevel level, Vec3 cam, float partial) {
        double cx = cam.f_82479_;
        double cy = cam.f_82480_;
        double cz = cam.f_82481_;
        for (Line l : LINES) {
            if (far(l.from, cam) && far(l.to, cam)) {
                continue;
            }
            double age = clock - l.born + partial;
            switch (l.kind) {
                case PikaMoves.LASER -> LightArt.laser(SINK, l.from.f_82479_ - cx, l.from.f_82480_ - cy, l.from.f_82481_ - cz, l.to.f_82479_ - cx,
                    l.to.f_82480_ - cy, l.to.f_82481_ - cz, l.width, age, (int) l.born);
                case PikaMoves.BEAM -> LightArt.beam(SINK, l.from.f_82479_ - cx, l.from.f_82480_ - cy, l.from.f_82481_ - cz, l.to.f_82479_ - cx,
                    l.to.f_82480_ - cy, l.to.f_82481_ - cz, l.width, age, (int) l.born);
                default -> LightArt.path(SINK, l.from.f_82479_ - cx, l.from.f_82480_ - cy + 1.0, l.from.f_82481_ - cz, l.to.f_82479_ - cx,
                    l.to.f_82480_ - cy + 1.0, l.to.f_82481_ - cz, age, (int) l.born);
            }
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
                case PikaMoves.BURST -> LightArt.burst(SINK, x, y, z, s.size, age, (int) s.born);
                case PikaMoves.DODGE -> LightArt.impact(SINK, x, y, z, s.size, age, (int) s.born);
                default -> kick(s, x, y, z, age);
            }
        }
        for (Bead b : BEADS) {
            double t = Math.min(1.0, (clock - b.born + partial) / b.flight);
            Vec3 p = b.from.m_82549_(b.to.m_82546_(b.from).m_82490_(t));
            if (far(p, cam)) {
                continue;
            }
            Vec3 v = b.to.m_82546_(b.from).m_82490_(1.0 / b.flight);
            LightArt.bullet(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, v.f_82479_, v.f_82480_, v.f_82481_, b.size, clock - b.born + partial,
                b.seed);
        }
        for (Cut c : CUTS) {
            if (far(c.at, cam)) {
                continue;
            }
            LightArt.slash(SINK, c.at.f_82479_ - cx, c.at.f_82480_ - cy, c.at.f_82481_ - cz, c.axis.f_82479_, c.axis.f_82480_, c.axis.f_82481_, c.radius,
                c.start, c.sweep, clock - c.born + partial);
        }
        for (Wave w : WAVES) {
            double age = clock - w.born + partial;
            Vec3 p = w.start.m_82549_(w.step.m_82490_(age));
            if (far(p, cam) || age > w.life) {
                continue;
            }
            Vec3 way = w.step.m_82541_();
            Vec3 side = way.m_82537_(new Vec3(0, 1, 0)).m_82541_();
            double fade = 1.0 - age / w.life;
            double yaw = Math.atan2(side.f_82479_, side.f_82481_);
            FireMesh.arc(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, way.f_82479_, way.f_82480_, way.f_82481_, w.width,
                w.width * 0.35 * fade + 0.1, yaw - 1.2, 2.4 + Math.PI * 0.0, -0.6, 0xFFD040, 0.6 * fade);
            FireMesh.arc(SINK, p.f_82479_ - cx, p.f_82480_ - cy, p.f_82481_ - cz, way.f_82479_, way.f_82480_, way.f_82481_, w.width,
                w.width * 0.15 * fade + 0.05, yaw - 1.2, 2.4, -0.6, 0xFFFFFF, 0.95 * fade);
        }
        for (Clone c : CLONES) {
            clone(level, c, cam, partial);
        }
        for (Kicked k : KICKED) {
            if (k.count < 2) {
                continue;
            }
            double[] pts = new double[k.count * 3];
            for (int i = 0; i < k.count; i++) {
                pts[i * 3] = k.trail[i * 3] - cx;
                pts[i * 3 + 1] = k.trail[i * 3 + 1] - cy;
                pts[i * 3 + 2] = k.trail[i * 3 + 2] - cz;
            }
            double fade = Math.max(0.0, 1.0 - (clock - k.born) / 26.0);
            LightArt.trail(SINK, pts, k.count, 0.9, fade);
        }
        for (Map.Entry<Integer, Trail> e : TRAILS.entrySet()) {
            Trail t = e.getValue();
            if (t.count < 2 || !(level.m_6815_(e.getKey()) instanceof Player player)) {
                continue;
            }
            double[] pts = new double[t.count * 3];
            Vec3 now = player.m_20318_(partial).m_82520_(0, 1.0, 0);
            pts[0] = now.f_82479_ - cx;
            pts[1] = now.f_82480_ - cy;
            pts[2] = now.f_82481_ - cz;
            for (int i = 1; i < t.count; i++) {
                pts[i * 3] = t.points[i * 3] - cx;
                pts[i * 3 + 1] = t.points[i * 3 + 1] - cy;
                pts[i * 3 + 2] = t.points[i * 3 + 2] - cz;
            }
            boolean streaming = STREAMS.containsKey(e.getKey());
            LightArt.trail(SINK, pts, t.count, streaming ? 0.8 : 0.35, streaming ? 1.0 : 0.7);
            if (streaming && player != Minecraft.m_91087_().f_91074_) {
                LightArt.light(SINK, pts[0], pts[1], pts[2], 0.6, 0, 1, 0, 0.0, 1.0, 1.0, clock + partial, e.getKey());
                LightArt.glow(SINK, pts[0], pts[1], pts[2], 2.0, 0xFFE070, 0.5);
            }
        }
        for (Map.Entry<Integer, Long> e : CHARGES.entrySet()) {
            if (level.m_6815_(e.getKey()) instanceof Player player) {
                Vec3 tip = fingertip(player, partial);
                double age = clock - e.getValue() + partial;
                LightArt.charge(SINK, tip.f_82479_ - cx, tip.f_82480_ - cy, tip.f_82481_ - cz, age, Math.min(1.0, age / 40.0), e.getKey());
            }
        }
        for (Map.Entry<Integer, Long> e : KICK_CHARGES.entrySet()) {
            if (level.m_6815_(e.getKey()) instanceof Player player) {
                Vec3 foot = foot(player, partial);
                double age = clock - e.getValue() + partial;
                double c = Math.min(1.0, age / 40.0);
                LightArt.glow(SINK, foot.f_82479_ - cx, foot.f_82480_ - cy, foot.f_82481_ - cz, 0.8 + 1.4 * c, 0xFFE070, 0.6);
                LightArt.light(SINK, foot.f_82479_ - cx, foot.f_82480_ - cy, foot.f_82481_ - cz, 0.15 + 0.2 * c, 0, 1, 0, 0.0, 1.0, 1.0, age,
                    e.getKey());
                FireMesh.ring(SINK, foot.f_82479_ - cx, foot.f_82480_ - cy - 0.3, foot.f_82481_ - cz, 0, 1, 0, 0.5 + 0.9 * c + 0.1 * Math.sin(age),
                    0.12, 0.05, 0xFFF0A0, 0.7);
            }
        }
        for (Map.Entry<Integer, Long> e : VOLLEYS.entrySet()) {
            if (level.m_6815_(e.getKey()) instanceof Player player) {
                Vec3 h = hands(player, partial).m_82520_(0, 0.15, 0);
                LightArt.crossed(SINK, h.f_82479_ - cx, h.f_82480_ - cy, h.f_82481_ - cz, clock - e.getValue() + partial, e.getKey());
            }
        }
        for (Map.Entry<Integer, Long> e : FORMS.entrySet()) {
            if (level.m_6815_(e.getKey()) instanceof Player player) {
                Vec3 h = hands(player, partial);
                Vec3 look = player.m_20252_(partial);
                LightArt.mirror(SINK, h.f_82479_ - cx, h.f_82480_ - cy, h.f_82481_ - cz, look.f_82479_, look.f_82480_, look.f_82481_,
                    clock - e.getValue() + partial, e.getKey());
            }
        }
    }

    /** A kick (or a cut, a parry, the light kick's blow) landing: a crescent of light at the foot's height, a flash where it struck. */
    static void kick(Spot s, double x, double y, double z, double age) {
        if (age < 0.0) {
            return;
        }
        Vec3 d = s.dir.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : s.dir.m_82541_();
        double yaw = Math.atan2(d.f_82479_, d.f_82481_);
        int step = s.step;
        // each kick its own swing: front kick rising, roundhouse across, back kick across the other way, rising kick up, barrage forward
        double ax = step == 1 || step == 4 || step == 7 ? Math.cos(yaw) : 0.15 * Math.cos(yaw);
        double ay = step == 1 || step == 4 || step == 7 ? 0.0 : step == 3 ? -1.0 : 1.0;
        double az = step == 1 || step == 4 || step == 7 ? -Math.sin(yaw) : -0.15 * Math.sin(yaw);
        double radius = step >= 6 ? 2.2 : 1.5;
        if (step != 8 && step != 9) {
            LightArt.slash(SINK, x - d.f_82479_ * 0.9, y, z - d.f_82481_ * 0.9, ax, ay, az, radius, yaw - 1.4, 2.8, age);
        }
        if (s.size > 0) {
            LightArt.impact(SINK, x, y, z, s.size >= 2 ? 3.2 : step == 9 ? 2.0 : 1.6 + (step == 8 ? s.size : 0.0), age, (int) s.born + step);
        }
    }

    /** A clone of light: a bead out of the body, a man of light running at its foe, two cuts, a burst. */
    static void clone(ClientLevel level, Clone c, Vec3 cam, float partial) {
        double age = clock - c.born + partial;
        Vec3 target = c.spot;
        Entity e = c.target >= 0 ? level.m_6815_(c.target) : null;
        if (e != null) {
            target = e.m_20318_(partial);
        }
        double out = 0.0;
        Vec3 from = c.origin;
        double angle = (c.index - 3) * 0.55;
        Vec3 toward = target.m_82546_(from);
        Vec3 flat = new Vec3(toward.f_82479_, 0, toward.f_82481_);
        flat = flat.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.m_82541_();
        Vec3 side = flat.m_82537_(new Vec3(0, 1, 0)).m_82541_();
        Vec3 rise = from.m_82549_(flat.m_82490_(1.5)).m_82549_(side.m_82490_(Math.sin(angle) * 2.5)).m_82520_(0, -0.6, 0);
        Vec3 stand = target.m_82549_(flat.m_82490_(-1.6)).m_82549_(side.m_82490_((c.index % 2 == 0 ? 1 : -1) * 0.6));
        if (age < 4.0) {
            double t = age / 4.0;
            Vec3 p = from.m_82549_(rise.m_82520_(0, 1.0, 0).m_82546_(from).m_82490_(t));
            if (!far(p, cam)) {
                Vec3 v = rise.m_82546_(from);
                LightArt.bullet(SINK, p.f_82479_ - cam.f_82479_, p.f_82480_ - cam.f_82480_, p.f_82481_ - cam.f_82481_, v.f_82479_ * 0.3, v.f_82480_ * 0.3,
                    v.f_82481_ * 0.3, 0.6, age, c.index);
            }
            return;
        }
        Vec3 feet;
        double run;
        double swing;
        double alpha = 1.0;
        if (age < 10.0) {
            double t = (age - 4.0) / 6.0;
            feet = rise.m_82549_(stand.m_82546_(rise).m_82490_(t * t * (3 - 2 * t)));
            run = age * 0.18;
            swing = -1.0;
            alpha = Math.min(1.0, (age - 4.0) / 1.5);
        } else if (age < 22.0) {
            feet = stand;
            run = 0.0;
            double u = (age - 10.0) / 6.0;
            swing = Math.sin(Math.PI * (u % 1.0) - Math.PI / 2.0);
        } else {
            feet = stand;
            run = 0.0;
            swing = 1.0;
            alpha = Math.max(0.0, 1.0 - (age - 22.0) / 4.0);
            if (!far(feet, cam)) {
                LightArt.burst(SINK, feet.f_82479_ - cam.f_82479_, feet.f_82480_ + 1.0 - cam.f_82480_, feet.f_82481_ - cam.f_82481_, 1.6, age - 22.0,
                    c.index * 7 + (int) c.born);
            }
        }
        if (far(feet, cam)) {
            return;
        }
        Vec3 face = target.m_82546_(feet);
        double yaw = Math.atan2(face.f_82479_, face.f_82481_);
        LightArt.figure(SINK, feet.f_82479_ - cam.f_82479_, feet.f_82480_ - cam.f_82480_, feet.f_82481_ - cam.f_82481_, yaw, run, swing, alpha,
            c.index);
        if (age >= 10.0 && age < 22.0 && ((int) ((age - 10.0) / 6.0)) < 2 && (age - 10.0) % 6.0 < 3.0) {
            LightArt.slash(SINK, feet.f_82479_ - cam.f_82479_, feet.f_82480_ + 1.0 - cam.f_82480_, feet.f_82481_ - cam.f_82481_, Math.cos(yaw), 0.0,
                -Math.sin(yaw), 1.8, yaw - 1.3, 2.6, (age - 10.0) % 6.0);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Photon

    /** Motes of light thrown out round {@code at} (Photon, bloomed). */
    static void motes(Vec3 at, int count, double speed) {
        for (int i = 0; i < PieceFx.count(count); i++) {
            Vec3 v = new Vec3(r(1), r(1) + 0.3, r(1)).m_82541_().m_82490_(speed * (0.4 + RANDOM.nextDouble()));
            mote(at, v, 12 + RANDOM.nextInt(12));
        }
    }

    static void mote(Vec3 at, Vec3 v, int life) {
        if (PieceFx.near(at, 64.0)) {
            PieceFx.at(PieceFx.MOTE, true, life, at).velocity(v).colors(0xFFFFF4C0, 0x00FFC830).size(0.16, 0.03).drag(0.9).fall(-0.004)
                .bloom(life).spawn();
        }
    }

    static double r(double spread) {
        return (RANDOM.nextDouble() * 2.0 - 1.0) * spread;
    }

    static void clear() {
        LINES.clear();
        SPOTS.clear();
        BEADS.clear();
        CUTS.clear();
        WAVES.clear();
        CLONES.clear();
        KICKED.clear();
        CHARGES.clear();
        KICK_CHARGES.clear();
        VOLLEYS.clear();
        FORMS.clear();
        STREAMS.clear();
        SWORDS.clear();
        TRAILS.clear();
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
