package com.hexhaki.piece.client.dragon;

import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.DragonForm;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.dragon.UoFire;
import com.hexhaki.piece.network.S2CUoFx;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 2.7.2: the Uo Uo no Mi's moves as every client sees them (server/UoMoves sends {@link S2CUoFx}). 2.7.6 (the owner: "the attacks
 * have to be more aggressive more violent, perhaps time to switch over to Photon, go crazy with it ... sparks, explosions, GO CRAZY
 * all of it must be 3d", "the fire has to be beautiful I mean look at the reference", "make sure it's bright and glowy, and it's
 * coming from inside the mouth not from anywhere else"): every move is real 3D geometry now (dragon/UoFire, dragon/FireMesh: the
 * anime's flat cel bands), drawn once a frame after the particles through the game's own debugQuads type (alpha blended, no culling,
 * sorted far to near: the owner's device), eye-centred; Photon (client/fx/PieceFx) adds glowing embers and sparks with bloom. Where a
 * shot is, where the tornado is, each ember's arc are worked out from what the server sent, so nothing costs traffic while it flies.
 * The dragon's fire leaves the inside of its open mouth (DragonForm.mouth, as DragonRenderer last drew it), its breath sweeps after
 * the aim as a stream does (the last ticks' aims are kept), and stops at a wall.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class UoFx {
    static final Random RANDOM = new Random();
    private static final Map<Integer, Shot> SHOTS = new HashMap<>();
    private static final List<Tornado> TORNADOES = new ArrayList<>();
    private static final List<Ember> EMBERS = new ArrayList<>();
    private static final List<Bang> BANGS = new ArrayList<>();
    /** Per player: their dragon's breath (since when), its charge (since when), its bite (since when). */
    static final Map<Integer, Long> FLAMES = new HashMap<>();
    static final Map<Integer, Long> CHARGES = new HashMap<>();
    static final Map<Integer, Long> BITES = new HashMap<>();
    /** Per player: their breath, kept after it stops until its last fire has flown out. */
    private static final Map<Integer, Breath> BREATHS = new HashMap<>();
    /** Per player: their dragon's last aims (for the breath's sweep). */
    private static final Map<Integer, Track> TRACKS = new HashMap<>();
    private static long clock;
    /** Beyond this (blocks) from the eye a move is not drawn. */
    static final double FAR = 160.0;

    /**
     * A shot in flight: from {@code start} at {@code velocity} a tick for {@code life} ticks; a dragon's starts at its mouth
     * ({@code offset}: the mouth less where the server put it, gone over its first ticks).
     */
    record Shot(int kind, Vec3 start, Vec3 velocity, double size, int life, long born, Vec3 offset) {
    }

    record Tornado(Vec3 start, Vec3 step, int life, long born) {
    }

    record Ember(Vec3 mouth, Vec3 apex, Vec3 land, int delay, int rise, int fall, long born) {
    }

    /** A burst going off. */
    record Bang(Vec3 at, Vec3 dir, double radius, int kind, long born) {
    }

    /** A breath: when it began and stopped (-1 still on), and how far it reaches before a wall. */
    static final class Breath {
        final long start;
        long stop = -1;
        double range = 35.0;

        Breath(long start) {
            this.start = start;
        }
    }

    /** A dragon's last {@link #TRACK} aims, one a tick: the inside of the mouth, the look, the mouth's way (9 numbers each). */
    static final int TRACK = 24;

    static final class Track {
        final double[] at = new double[TRACK * 9];
        long first = -1;
        long last = -1;
    }

    private UoFx() {
    }

    public static void accept(S2CUoFx m) {
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        Vec3 a = new Vec3(m.ax(), m.ay(), m.az());
        switch (m.kind()) {
            case S2CUoFx.AIR, S2CUoFx.BOLT, S2CUoFx.BLAST -> {
                Vec3 offset = null;
                if (m.kind() == S2CUoFx.BLAST) {
                    // a dragon's blast leaves its mouth (the server only knows near enough where that is)
                    DragonClient.Entry entry = DragonClient.ENTRIES.get(m.user());
                    if (entry != null && entry.mouthAge >= 0 && clock - entry.mouthAge < 4) {
                        offset = new Vec3(entry.mouth[0], entry.mouth[1], entry.mouth[2]).m_82546_(at);
                        if (offset.m_82556_() > 100.0) {
                            offset = null;
                        }
                    }
                }
                SHOTS.put(m.id(), new Shot(m.kind(), at, a, m.size(), m.ticks(), clock, offset));
            }
            case S2CUoFx.BURST -> {
                Shot shot = SHOTS.remove(m.id());
                int kind = (int) m.bz();
                BANGS.add(new Bang(at, new Vec3(m.ax(), m.ay(), m.az()), m.size(), shot == null ? kind : shot.kind(), clock));
                sparks(at, m.size(), shot == null ? kind : shot.kind());
            }
            case S2CUoFx.TORNADO -> TORNADOES.add(new Tornado(at, a, m.ticks(), clock));
            case S2CUoFx.EMBER -> EMBERS.add(new Ember(at, a, new Vec3(m.bx(), m.by(), m.bz()), m.ticks() >> 16 & 255, m.ticks() >> 8 & 255,
                m.ticks() & 255, clock));
            case S2CUoFx.FLAME_ON -> {
                FLAMES.put(m.user(), clock);
                BREATHS.put(m.user(), new Breath(clock));
            }
            case S2CUoFx.FLAME_OFF -> {
                FLAMES.remove(m.user());
                Breath breath = BREATHS.get(m.user());
                if (breath != null && breath.stop < 0) {
                    breath.stop = clock;
                }
            }
            case S2CUoFx.CHARGE_ON -> CHARGES.put(m.user(), clock);
            case S2CUoFx.CHARGE_OFF -> CHARGES.remove(m.user());
            case S2CUoFx.BITE -> BITES.put(m.user(), clock);
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ for the dragon's pose

    /** How far a dragon's jaw is open now (its breath, its charge): thrown wide (DragonForm's roar). */
    static float mouth(int id, float partial) {
        float open = 0.0F;
        Long flame = FLAMES.get(id);
        if (flame != null) {
            open = Math.max(open, DragonForm.smooth((clock - flame + partial) / 4.0F));
        }
        Breath breath = BREATHS.get(id);
        if (flame == null && breath != null && breath.stop >= 0) {
            open = Math.max(open, 1.0F - DragonForm.smooth((clock - breath.stop + partial) / 6.0F));
        }
        Long charge = CHARGES.get(id);
        if (charge != null) {
            open = Math.max(open, 0.6F + 0.4F * DragonForm.smooth((clock - charge + partial) / 30.0F));
        }
        return open;
    }

    /** How far into its bite a dragon is (0 to 1), or -1. */
    static float bite(int id, float partial) {
        Long bite = BITES.get(id);
        if (bite == null) {
            return -1.0F;
        }
        float t = (clock - bite + partial) / BITE_TICKS;
        return t > 1.0F ? -1.0F : t;
    }

    /** The bite's length (ticks): the jaws shut on the server's tick 7 (0.45 to 0.6 of it). */
    static final float BITE_TICKS = 14.0F;

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
        SHOTS.values().removeIf(shot -> clock - shot.born > shot.life + 3);
        BANGS.removeIf(bang -> clock - bang.born > (bang.kind == S2CUoFx.AIR ? 11 : UoFire.BURST_LIFE + 1));
        TORNADOES.removeIf(t -> clock - t.born > t.life);
        for (Iterator<Ember> it = EMBERS.iterator(); it.hasNext(); ) {
            Ember e = it.next();
            long age = clock - e.born - e.delay;
            if (age > e.rise + e.fall) {
                it.remove();
                BANGS.add(new Bang(e.land.m_82520_(0.0, 0.4, 0.0), new Vec3(0, -1, 0), 3.2, S2CUoFx.BOLT, clock));
                sparks(e.land, 3.2, S2CUoFx.BOLT);
            } else if (age >= 0 && RANDOM.nextInt(2) == 0) {
                Vec3 now = ember(e, age);
                ember(now, new Vec3(r(0.08), 0.06, r(0.08)), 10);
            }
        }
        BITES.entrySet().removeIf(e -> clock - e.getValue() > BITE_TICKS + 2);
        BREATHS.entrySet().removeIf(e -> e.getValue().stop >= 0 && clock - e.getValue().stop > UoFire.BREATH_LIFE + 2);
        // the dragons' aims, and their breaths' reach
        for (Map.Entry<Integer, Breath> e : BREATHS.entrySet()) {
            if (level.m_6815_(e.getKey()) instanceof Player player) {
                record(player);
                Breath breath = e.getValue();
                if (breath.stop < 0) {
                    Vec3 mouth = mouthOf(player);
                    Vec3 look = player.m_20252_(1.0F);
                    BlockHitResult wall = level.m_45547_(new ClipContext(mouth, mouth.m_82549_(look.m_82490_(35.0)), ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, player));
                    double reach = wall.m_6662_() == HitResult.Type.BLOCK ? Math.max(4.0, wall.m_82450_().m_82554_(mouth) + 2.0) : 35.0;
                    breath.range += (reach - breath.range) * 0.5;
                    embers(player, breath);
                }
            }
        }
        for (Integer id : CHARGES.keySet()) {
            if (level.m_6815_(id) instanceof Player player) {
                record(player);
                if (clock % 2 == 0) {
                    Vec3 mouth = mouthOf(player);
                    spark(mouth.m_82520_(r(2.5), r(2.5), r(2.5)), new Vec3(r(0.1), 0.08, r(0.1)), 0xFFFFD27A, 12);
                }
            }
        }
        for (Integer id : BITES.keySet()) {
            if (level.m_6815_(id) instanceof Player player) {
                record(player);
            }
        }
        TRACKS.keySet().removeIf(id -> !BREATHS.containsKey(id) && !CHARGES.containsKey(id) && !BITES.containsKey(id));
    }

    /** This tick's aim of {@code player}'s dragon onto its track. */
    static void record(Player player) {
        Track track = TRACKS.computeIfAbsent(player.m_19879_(), k -> new Track());
        Vec3 look = player.m_20252_(1.0F);
        double[] m = mouthWay(player);
        int o = Math.floorMod(clock, TRACK) * 9;
        track.at[o] = m[0];
        track.at[o + 1] = m[1];
        track.at[o + 2] = m[2];
        track.at[o + 3] = look.f_82479_;
        track.at[o + 4] = look.f_82480_;
        track.at[o + 5] = look.f_82481_;
        track.at[o + 6] = m[3];
        track.at[o + 7] = m[4];
        track.at[o + 8] = m[5];
        if (track.first < 0 || track.last < clock - 1) {
            track.first = clock; // a fresh track (or one left a while): nothing older is kept
        }
        track.last = clock;
    }

    /** Glowing embers thrown off a breath (Photon, with bloom), all along it. */
    static void embers(Player player, Breath breath) {
        Vec3 mouth = mouthOf(player);
        Vec3 look = player.m_20252_(1.0F);
        int n = PieceFx.count(5);
        for (int i = 0; i < n; i++) {
            double d = 3.0 + RANDOM.nextDouble() * Math.min(breath.range, UoFire.reach(clock - breath.start, breath.range));
            double spread = 0.4 + d * 0.16;
            Vec3 p = mouth.m_82549_(look.m_82490_(d)).m_82520_(r(spread), r(spread), r(spread));
            ember(p, look.m_82490_(0.3 + RANDOM.nextDouble() * 0.4).m_82520_(r(0.15), 0.08 + RANDOM.nextDouble() * 0.1, r(0.15)),
                14 + RANDOM.nextInt(10));
        }
    }

    /** A burst's sparks and embers (Photon, with bloom: a light the geometry alone has not). */
    static void sparks(Vec3 at, double radius, int kind) {
        int n = PieceFx.count(kind == S2CUoFx.AIR ? 6 : (float) Math.min(40, 10 + radius * 3));
        for (int i = 0; i < n; i++) {
            Vec3 out = new Vec3(r(1), r(0.6) + 0.45, r(1)).m_82541_();
            double speed = (0.3 + RANDOM.nextDouble() * 0.6) * Math.sqrt(Math.max(1.0, radius));
            if (kind == S2CUoFx.AIR) {
                spark(at, out.m_82490_(speed * 0.6), 0xFFFFFFFF, 8);
            } else {
                spark(at, out.m_82490_(speed), RANDOM.nextBoolean() ? 0xFFFFD27A : 0xFFFF9A3A, 14 + RANDOM.nextInt(10));
                ember(at.m_82549_(out.m_82490_(radius * 0.4)), out.m_82490_(speed * 0.35).m_82520_(0.0, 0.1, 0.0), 22 + RANDOM.nextInt(16));
            }
        }
    }

    /** Where an ember is {@code age} ticks after it left the mouth: up to its top easing out, down onto its mark easing in. */
    static Vec3 ember(Ember e, double age) {
        if (age <= e.rise) {
            double t = age / (double) e.rise;
            double u = 1.0 - (1.0 - t) * (1.0 - t);
            Vec3 bend = e.mouth.m_82549_(e.apex).m_82490_(0.5).m_82520_(0.0, (e.apex.f_82480_ - e.mouth.f_82480_) * 0.35, 0.0);
            return bezier(e.mouth, bend, e.apex, u);
        }
        double t = (age - e.rise) / (double) e.fall;
        double u = t * t;
        Vec3 bend = new Vec3(e.land.f_82479_, e.apex.f_82480_, e.land.f_82481_);
        return bezier(e.apex, bend, e.land, u);
    }

    static Vec3 bezier(Vec3 a, Vec3 b, Vec3 c, double t) {
        Vec3 ab = a.m_82549_(b.m_82546_(a).m_82490_(t));
        Vec3 bc = b.m_82549_(c.m_82546_(b).m_82490_(t));
        return ab.m_82549_(bc.m_82546_(ab).m_82490_(t));
    }

    // ------------------------------------------------------------------------------------------------------ the dragons' mouths

    /** A dragon's mouth: the inside of its gape where the renderer last drew it, or near enough before it has been drawn. */
    static Vec3 mouthOf(Player player) {
        double[] m = mouthWay(player);
        return new Vec3(m[0], m[1], m[2]);
    }

    /** The inside of a dragon's mouth and the mouth's way out (6 numbers). */
    static double[] mouthWay(Player player) {
        DragonClient.Entry entry = DragonClient.ENTRIES.get(player.m_19879_());
        if (entry != null && entry.mouthAge >= 0 && clock - entry.mouthAge < 4) {
            return entry.mouth.clone();
        }
        Vec3 look = player.m_20252_(1.0F);
        Vec3 at = player.m_20182_().m_82520_(0.0, DragonForm.HEAD_HEIGHT - 0.6, 0.0).m_82549_(look.m_82490_(2.6));
        return new double[] {at.f_82479_, at.f_82480_, at.f_82481_, look.f_82479_, look.f_82480_, look.f_82481_};
    }

    static long clock() {
        return clock;
    }

    // ------------------------------------------------------------------------------------------------------ the drawing

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || SHOTS.isEmpty() && BANGS.isEmpty() && TORNADOES.isEmpty()
            && EMBERS.isEmpty() && BREATHS.isEmpty() && CHARGES.isEmpty() && BITES.isEmpty()) {
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
        FireMesh.quality = Math.max(0.35, PieceFx.density()); // fewer faces with fewer particles
        try {
            draw(level, event.getCamera().m_90583_(), event.getPartialTick());
        } catch (RuntimeException failure) {
            // a move's look never stops the game
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: a Uo move could not be drawn", failure);
            SHOTS.clear();
            BANGS.clear();
            TORNADOES.clear();
            EMBERS.clear();
            BREATHS.clear();
        }
        buffers.m_109912_(type);
        SINK.buffer = null;
    }

    /** Writes FireMesh's quads straight into the game's buffer, through the view. */
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
        for (Map.Entry<Integer, Shot> e : SHOTS.entrySet()) {
            Shot shot = e.getValue();
            double age = Math.min(shot.life, clock - shot.born + partial);
            Vec3 v = shot.velocity;
            double fade = shot.offset == null ? 0.0 : Math.max(0.0, 1.0 - age / 5.0);
            double x = shot.start.f_82479_ + v.f_82479_ * age + (fade > 0.0 ? shot.offset.f_82479_ * fade : 0.0);
            double y = shot.start.f_82480_ + v.f_82480_ * age + (fade > 0.0 ? shot.offset.f_82480_ * fade : 0.0);
            double z = shot.start.f_82481_ + v.f_82481_ * age + (fade > 0.0 ? shot.offset.f_82481_ * fade : 0.0);
            if ((x - cx) * (x - cx) + (y - cy) * (y - cy) + (z - cz) * (z - cz) > FAR * FAR) {
                continue;
            }
            double travelled = v.m_82553_() * age;
            switch (shot.kind) {
                case S2CUoFx.AIR -> UoFire.airShot(SINK, x - cx, y - cy, z - cz, v.f_82479_, v.f_82480_, v.f_82481_, shot.size * 1.4, age, travelled,
                    e.getKey());
                case S2CUoFx.BOLT -> UoFire.fireball(SINK, x - cx, y - cy, z - cz, v.f_82479_, v.f_82480_, v.f_82481_, shot.size * 1.25, age, travelled,
                    false, e.getKey());
                default -> UoFire.fireball(SINK, x - cx, y - cy, z - cz, v.f_82479_, v.f_82480_, v.f_82481_, shot.size * 0.55, age, travelled, true,
                    e.getKey());
            }
        }
        int n = 0;
        for (Bang bang : BANGS) {
            n++;
            if (far(bang.at, cam)) {
                continue;
            }
            double age = clock - bang.born + partial;
            double x = bang.at.f_82479_ - cx;
            double y = bang.at.f_82480_ - cy;
            double z = bang.at.f_82481_ - cz;
            if (bang.kind == S2CUoFx.AIR) {
                Vec3 d = bang.dir.m_82556_() < 1.0E-6 ? new Vec3(0, 1, 0) : bang.dir.m_82541_();
                UoFire.airBurst(SINK, x, y, z, d.f_82479_, d.f_82480_, d.f_82481_, bang.radius * 0.8, age, (int) bang.born + n);
            } else {
                UoFire.explosion(SINK, x, y, z, bang.radius * 0.8, age, (int) bang.born * 31 + n);
            }
        }
        for (Tornado t : TORNADOES) {
            double age = clock - t.born + partial;
            Vec3 base = t.start.m_82549_(t.step.m_82490_(age));
            if (far(base, cam)) {
                continue;
            }
            double strength = Math.min(1.0, Math.min(age / 6.0, (t.life - age) / 8.0));
            UoFire.tornado(SINK, base.f_82479_ - cx, base.f_82480_ - cy, base.f_82481_ - cz, age, strength, (int) t.born);
        }
        for (Ember e : EMBERS) {
            double age = clock - e.born - e.delay + partial;
            if (age < 0.0) {
                continue;
            }
            Vec3 now = ember(e, age);
            if (far(now, cam)) {
                continue;
            }
            Vec3 before = ember(e, Math.max(0.0, age - 1.0));
            Vec3 v = now.m_82546_(before);
            if (v.m_82556_() < 1.0E-6) {
                v = new Vec3(0, 0.1, 0);
            }
            UoFire.fireball(SINK, now.f_82479_ - cx, now.f_82480_ - cy, now.f_82481_ - cz, v.f_82479_, v.f_82480_, v.f_82481_, 0.55, age,
                Math.min(age * 1.2, 5.0), false, (int) e.born + e.delay);
        }
        for (Map.Entry<Integer, Breath> e : BREATHS.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof Player player) || far(player.m_20182_(), cam)) {
                continue;
            }
            Breath breath = e.getValue();
            AIM.set(player, cam, partial);
            double on = clock - breath.start + partial;
            double off = breath.stop >= 0 ? clock - breath.stop + partial : -1.0;
            UoFire.breath(SINK, AIM, on, off, breath.range, e.getKey());
        }
        for (Map.Entry<Integer, Long> e : CHARGES.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof Player player) || far(player.m_20182_(), cam)) {
                continue;
            }
            AIM.set(player, cam, partial);
            double age = clock - e.getValue() + partial;
            UoFire.orb(SINK, AIM, age, Math.min(1.0, age / 60.0), e.getKey());
        }
        for (Map.Entry<Integer, Long> e : BITES.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof Player player) || far(player.m_20182_(), cam)) {
                continue;
            }
            AIM.set(player, cam, partial);
            UoFire.bite(SINK, AIM, (clock - e.getValue() + partial) / BITE_TICKS, e.getKey());
        }
    }

    /** A dragon's aim for UoFire, eye-centred: now (as the renderer last drew the mouth), or off its track for a while ago. */
    static final class Aimer implements UoFire.Aim {
        double[] now = new double[9];
        Track track;
        double cx;
        double cy;
        double cz;
        float partial;

        void set(Player player, Vec3 cam, float partial) {
            double[] m = mouthWay(player);
            Vec3 look = player.m_20252_(partial);
            this.now[0] = m[0];
            this.now[1] = m[1];
            this.now[2] = m[2];
            this.now[3] = look.f_82479_;
            this.now[4] = look.f_82480_;
            this.now[5] = look.f_82481_;
            this.now[6] = m[3];
            this.now[7] = m[4];
            this.now[8] = m[5];
            this.track = TRACKS.get(player.m_19879_());
            this.cx = cam.f_82479_;
            this.cy = cam.f_82480_;
            this.cz = cam.f_82481_;
            this.partial = partial;
        }

        @Override
        public void at(double ago, double[] out) {
            // ago, counted from this frame: the track's sample k ticks back is (k + partial) ago
            double back = ago - this.partial;
            if (this.track == null || back <= 0.0 || this.track.last < 0) {
                System.arraycopy(this.now, 0, out, 0, 9);
            } else {
                int k0 = (int) Math.floor(back);
                double f = back - k0;
                int k1 = k0 + 1;
                int span = (int) Math.min(TRACK - 1, this.track.last - this.track.first);
                k0 = Math.min(k0, span);
                k1 = Math.min(k1, span);
                int o0 = Math.floorMod(this.track.last - k0, TRACK) * 9;
                int o1 = Math.floorMod(this.track.last - k1, TRACK) * 9;
                for (int i = 0; i < 9; i++) {
                    out[i] = this.track.at[o0 + i] + (this.track.at[o1 + i] - this.track.at[o0 + i]) * f;
                }
                // the newest sample blends into now, so the stream never jumps at the mouth
                if (k0 == 0) {
                    for (int i = 0; i < 9; i++) {
                        out[i] = this.now[i] + (out[i] - this.now[i]) * Math.min(1.0, back);
                    }
                }
            }
            out[0] -= this.cx;
            out[1] -= this.cy;
            out[2] -= this.cz;
            normal(out, 3);
            normal(out, 6);
        }

        static void normal(double[] v, int o) {
            double l = Math.sqrt(v[o] * v[o] + v[o + 1] * v[o + 1] + v[o + 2] * v[o + 2]);
            if (l > 1.0E-9) {
                v[o] /= l;
                v[o + 1] /= l;
                v[o + 2] /= l;
            }
        }
    }

    static final Aimer AIM = new Aimer();

    // ------------------------------------------------------------------------------------------------------ Photon

    /** A glowing spark (Photon, bloomed). */
    static void spark(Vec3 at, Vec3 v, int argb, int life) {
        if (PieceFx.near(at, 64.0)) {
            PieceFx.at(PieceFx.SPARK, true, life, at).velocity(v).colors(argb, argb & 0x00FFFFFF).size(0.16, 0.02).drag(0.86).fall(0.02)
                .bloom(life).spawn();
        }
    }

    /** A glowing ember drifting up and out (Photon, bloomed): yellow-white going to red. */
    static void ember(Vec3 at, Vec3 v, int life) {
        if (PieceFx.near(at, 64.0)) {
            PieceFx.at(PieceFx.EMBER, true, life, at).velocity(v).colors(0xFFFFE9A0, 0x00FF4A14).size(0.18, 0.04).drag(0.93).fall(-0.006)
                .wander(0.02).bloom(life).spawn();
        }
    }

    static double r(double spread) {
        return (RANDOM.nextDouble() * 2.0 - 1.0) * spread;
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SHOTS.clear();
        TORNADOES.clear();
        EMBERS.clear();
        BANGS.clear();
        FLAMES.clear();
        CHARGES.clear();
        BITES.clear();
        BREATHS.clear();
        TRACKS.clear();
    }
}
