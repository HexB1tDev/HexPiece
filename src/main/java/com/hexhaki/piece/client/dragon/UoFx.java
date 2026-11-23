package com.hexhaki.piece.client.dragon;

import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.DragonForm;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.dragon.UoFire;
import com.hexhaki.piece.dragon.UoSounds;
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
import net.minecraft.world.entity.Entity;
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
    /** 2.7.7: the lines of fire Blast Breath and the charged blast leave along their way, burning on after they burst. */
    private static final List<Line> LINES = new ArrayList<>();
    /** How long a piece of a line of fire burns (ticks): the human's Blast Breath, the dragon's blast. */
    static final double LINE_BURN = 14.0;
    static final double BLAST_LINE_BURN = 12.0;
    /** Per player: their dragon's breath (since when), its charge (since when), its bite (since when). */
    static final Map<Integer, Long> FLAMES = new HashMap<>();
    static final Map<Integer, Long> CHARGES = new HashMap<>();
    /** 2.9.0: when the charge's held loop takes over from its build-up (ticks; the build-up is 8.5 s, fading over its last 0.6 s). */
    static final int CHARGE_HOLD_AT = 156;
    static final Map<Integer, Long> BITES = new HashMap<>();
    /** 2.7.8: per player, their beam of fire (Blast Breath): when it began, its life, width, length, whether the dragon's. */
    record FireBeam(long born, int life, double width, double length, boolean dragon) {
    }

    static final Map<Integer, FireBeam> BEAMS = new HashMap<>();
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

    /** A shot's line of fire after it burst: its way, its speed, how far it flew ({@code end}, blocks), its look. */
    record Line(int id, int kind, Vec3 start, Vec3 velocity, double size, long born, double end, Vec3 offset) {
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
        if (m.kind() >= 120 && m.kind() < 160) {
            com.hexhaki.piece.client.hie.HieFx.accept(m); // 3.1.9: the Ice fruit's moves (120 to 159 since 3.2.2)
            return;
        }
        if (m.kind() >= 100 && m.kind() < 120) {
            com.hexhaki.piece.client.magu.MaguFx.accept(m); // 3.1.3: the Magma fruit's moves (100 to 119)
            return;
        }
        if (m.kind() >= 180 && m.kind() < 200) {
            com.hexhaki.piece.client.noro.NoroFx.accept(m); // 3.3.4: the Noro fruit's (180 to 199)
            return;
        }
        if (m.kind() >= 200 && m.kind() < 220) {
            com.hexhaki.piece.client.supa.SupaFx.accept(m); // 3.3.5: the Supa fruit's (200 to 219)
            return;
        }
        if (m.kind() >= 220 && m.kind() < 240) {
            com.hexhaki.piece.client.horo.HoroFx.accept(m); // 3.3.6: the Horo fruit's (220 to 239)
            return;
        }
        if (m.kind() >= 240) {
            com.hexhaki.piece.client.ito.ItoFx.accept(m); // 3.3.7: the Ito fruit's (240 to 255: the kind is one byte)
            return;
        }
        if (m.kind() == com.hexhaki.piece.server.NikaJump.HOP || m.kind() == com.hexhaki.piece.server.NikaJump.LAND) {
            com.hexhaki.piece.client.gear.RubberWorldDraw.accept(m); // 3.3.2: Gear 5's jump turning the ground to rubber
            return;
        }
        if (m.kind() == com.hexhaki.piece.server.NikaPistol.POW) {
            com.hexhaki.piece.client.gear.NikaPow.accept(m); // 3.3.9: Dawn Pistol's cartoon POW where it lands
            return;
        }
        if (m.kind() >= com.hexhaki.piece.server.NikaWhip.GOOF && m.kind() <= com.hexhaki.piece.server.GiantThrow.CRASH) {
            // 3.4.4: Goof Ball and Dawn Whip (165 to 172); 3.4.7: the Giant's throw, its flight and its crash (173, 174)
            com.hexhaki.piece.client.gear.NikaWhipClient.accept(m);
            return;
        }
        if (m.kind() >= 160 && m.kind() < 180) {
            com.hexhaki.piece.client.gear.NikaHoldClient.accept(m); // 3.2.8: the Nika fruit's (160 to 179): the Giant's hold
            return;
        }
        if (m.kind() >= com.hexhaki.piece.server.ConquerorAura.SPARKS) {
            com.hexhaki.piece.client.haki.ConquerorSparksClient.accept(m); // 2.9.1: the Conqueror's lightning off its user
            return;
        }
        if (m.kind() == com.hexhaki.piece.server.BrokenBones.XRAY) {
            com.hexhaki.piece.client.gura.BonesOverlay.accept(m); // 3.4.5: the Gura ultimate's X-ray on one's own screen
            return;
        }
        if (m.kind() >= 60 && m.kind() < 80) {
            com.hexhaki.piece.client.gura.GuraFx.accept(m); // 3.1.1: the Gura fruit's moves (60 to 79) share it too
            return;
        }
        if (m.kind() >= 32) {
            com.hexhaki.piece.client.pika.PikaFx.accept(m); // 2.7.8: the Light fruit's moves share this message
            return;
        }
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        Vec3 a = new Vec3(m.ax(), m.ay(), m.az());
        switch (m.kind()) {
            case S2CUoFx.AIR, S2CUoFx.BOLT, S2CUoFx.BLAST -> {
                Vec3 offset = null;
                ClientLevel level = Minecraft.m_91087_().f_91073_;
                if (m.kind() == S2CUoFx.BLAST && level != null && level.m_6815_(m.user()) instanceof Player user) {
                    // a dragon's blast leaves its mouth, as drawn (in first person: under the middle of the view); the server only
                    // knows near enough where that is
                    double[] mouth = mouthWay(user, 1.0F);
                    offset = new Vec3(mouth[0], mouth[1], mouth[2]).m_82546_(at);
                    if (offset.m_82556_() > 100.0) {
                        offset = null;
                    }
                }
                SHOTS.put(m.id(), new Shot(m.kind(), at, a, m.size(), m.ticks(), clock, offset));
            }
            case S2CUoFx.BURST -> {
                Shot shot = SHOTS.remove(m.id());
                int kind = (int) m.bz();
                if (shot != null && shot.kind != S2CUoFx.AIR) {
                    double speed = Math.max(1.0E-6, shot.velocity.m_82553_());
                    double end = Math.max(0.0, at.m_82546_(shot.start).m_82526_(shot.velocity) / speed);
                    LINES.add(new Line(m.id(), shot.kind, shot.start, shot.velocity, shot.size, shot.born, end, shot.offset));
                }
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
            case S2CUoFx.CHARGE_ON -> {
                CHARGES.put(m.user(), clock);
                // 2.9.0 (the owner: "the sound while it's charging ends all of a sudden"): the build-up, then (CHARGE_HOLD_AT) its
                // height held round and round until the key comes up, each fading out then, never cut off
                ClientLevel world = Minecraft.m_91087_().f_91073_;
                Entity user = world == null ? null : world.m_6815_(m.user());
                int id = m.user();
                long since = clock;
                com.hexhaki.piece.client.LoopSound.play(UoSounds.CHARGE_BUILD, user, 2.4F, 1.0F, false, false, DragonForm.HEAD_HEIGHT,
                    () -> CHARGES.get(id) != null && CHARGES.get(id) == since);
            }
            case S2CUoFx.CHARGE_OFF -> CHARGES.remove(m.user());
            case S2CUoFx.BITE -> BITES.put(m.user(), clock);
            case S2CUoFx.FIRE_BEAM -> {
                BEAMS.put(m.user(), new FireBeam(clock, m.ticks(), m.size(), m.ax(), m.id() == 1));
                AimLock.beam(m.user(), (float) m.x(), (float) m.y(), m.ticks()); // 2.8.5: one's own aim held until it ends
            }
            case S2CUoFx.FIRE_BEAM_OFF -> {
                AimLock.off(m.user());
                FireBeam beam = BEAMS.get(m.user());
                if (beam != null) {
                    // cut short: it thins away over its last 6 ticks from now
                    BEAMS.put(m.user(), new FireBeam(beam.born, (int) Math.min(beam.life, clock - beam.born + 6), beam.width, beam.length,
                        beam.dragon));
                }
            }
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
        FireBeam beam = BEAMS.get(id);
        if (beam != null && beam.dragon) {
            open = Math.max(open, 1.0F - DragonForm.smooth((clock - beam.born + partial - beam.life + 4) / 6.0F));
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
        BANGS.removeIf(bang -> clock - bang.born > (bang.kind == S2CUoFx.AIR ? 11 : bang.kind == S2CUoFx.BOMB ? UoFire.BOMB_LIFE + 1
            : UoFire.BURST_LIFE + 1));
        TORNADOES.removeIf(t -> clock - t.born > t.life);
        LINES.removeIf(l -> clock - l.born > l.end / Math.max(1.0E-6, l.velocity.m_82553_()) + LINE_BURN + 1);
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
        BEAMS.entrySet().removeIf(e -> clock - e.getValue().born > e.getValue().life + 1);
        for (Map.Entry<Integer, FireBeam> e : BEAMS.entrySet()) {
            if (level.m_6815_(e.getKey()) instanceof Player player) {
                FireBeam beam = e.getValue();
                if (beam.dragon) {
                    record(player);
                }
                // embers off the beam all along it
                double[] m = beam.dragon ? mouthWay(player, 1.0F) : humanMouth(player, 1.0F);
                double[] aim = aimed(player, m, 1.0F, beam.length);
                Vec3 from = new Vec3(m[0], m[1], m[2]);
                Vec3 way = new Vec3(aim[0], aim[1], aim[2]);
                for (int i = 0; i < PieceFx.count(beam.dragon ? 8 : 4); i++) {
                    double d = RANDOM.nextDouble() * Math.min(beam.length, aim[3]);
                    double spread = beam.width * 1.2;
                    ember(from.m_82549_(way.m_82490_(d)).m_82520_(r(spread), r(spread), r(spread)),
                        way.m_82490_(0.3).m_82520_(r(0.2), 0.12, r(0.2)), 16 + RANDOM.nextInt(12));
                }
            }
        }
        BREATHS.entrySet().removeIf(e -> e.getValue().stop >= 0 && clock - e.getValue().stop > UoFire.BREATH_LIFE + 2);
        // the dragons' aims, and their breaths' reach
        for (Map.Entry<Integer, Breath> e : BREATHS.entrySet()) {
            if (level.m_6815_(e.getKey()) instanceof Player player) {
                record(player);
                Breath breath = e.getValue();
                if (breath.stop < 0) {
                    double[] mouth = mouthWay(player, 1.0F);
                    double[] aim = aimed(player, mouth, 1.0F, 35.0);
                    double reach = aim[4] > 0.0 ? Math.max(4.0, aim[3] + 2.0) : 35.0;
                    breath.range += (reach - breath.range) * 0.5;
                    embers(player, breath);
                }
            }
        }
        for (Map.Entry<Integer, Long> charge : CHARGES.entrySet()) {
            int id = charge.getKey();
            if (level.m_6815_(id) instanceof Player player) {
                if (clock - charge.getValue() == CHARGE_HOLD_AT) {
                    long since = charge.getValue();
                    com.hexhaki.piece.client.LoopSound.play(UoSounds.CHARGE_HOLD, player, 2.4F, 1.0F, true, true, DragonForm.HEAD_HEIGHT,
                        () -> CHARGES.get(id) != null && CHARGES.get(id) == since);
                }
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
        TRACKS.keySet().removeIf(id -> !BREATHS.containsKey(id) && !CHARGES.containsKey(id) && !BITES.containsKey(id) && !BEAMS.containsKey(id));
    }

    /** This tick's aim of {@code player}'s dragon onto its track. */
    static void record(Player player) {
        Track track = TRACKS.computeIfAbsent(player.m_19879_(), k -> new Track());
        double[] m = mouthWay(player, 1.0F);
        double[] aim = aimed(player, m, 1.0F, 35.0);
        Vec3 look = new Vec3(aim[0], aim[1], aim[2]);
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
        double[] m = mouthWay(player, 1.0F);
        Vec3 mouth = new Vec3(m[0], m[1], m[2]);
        double[] aim = aimed(player, m, 1.0F, 35.0);
        Vec3 look = new Vec3(aim[0], aim[1], aim[2]);
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
        double[] m = mouthWay(player, 1.0F);
        return new Vec3(m[0], m[1], m[2]);
    }

    /** In first person the fire leaves from under the middle of the view: this far ahead of the eye and below its line (blocks). */
    static final double FIRST_AHEAD = 4.0;
    static final double FIRST_UNDER = 1.5;

    /**
     * The inside of a dragon's mouth and the mouth's way out (6 numbers). 2.7.7 (the owner: "in first person the height does not match
     * the head height, means the fire and projectiles are coming from above"): the player's own dragon seen in first person is not
     * drawn (the eye is inside it), so its fire leaves from just under the middle of the view, held to the eye as it moves.
     */
    static double[] mouthWay(Player player, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (player == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == net.minecraft.client.CameraType.FIRST_PERSON) {
            Vec3 eye = player.m_20299_(partial);
            double pitch = Math.toRadians(player.f_19860_ + (player.m_146909_() - player.f_19860_) * partial);
            double yaw = Math.toRadians(player.f_19859_ + (player.m_146908_() - player.f_19859_) * partial);
            double lx = -Math.sin(yaw) * Math.cos(pitch);
            double ly = -Math.sin(pitch);
            double lz = Math.cos(yaw) * Math.cos(pitch);
            // the view's up: the look turned a quarter up
            double ux = -Math.sin(yaw) * Math.sin(pitch);
            double uy = Math.cos(pitch);
            double uz = Math.cos(yaw) * Math.sin(pitch);
            return new double[] {eye.f_82479_ + lx * FIRST_AHEAD - ux * FIRST_UNDER, eye.f_82480_ + ly * FIRST_AHEAD - uy * FIRST_UNDER,
                eye.f_82481_ + lz * FIRST_AHEAD - uz * FIRST_UNDER, lx, ly, lz};
        }
        DragonClient.Entry entry = DragonClient.ENTRIES.get(player.m_19879_());
        if (entry != null && entry.mouthAge >= 0 && clock - entry.mouthAge < 4) {
            return entry.mouth.clone();
        }
        Vec3 look = player.m_20252_(1.0F);
        Vec3 at = player.m_20182_().m_82520_(0.0, DragonForm.HEAD_HEIGHT - 0.6, 0.0).m_82549_(look.m_82490_(2.6));
        return new double[] {at.f_82479_, at.f_82480_, at.f_82481_, look.f_82479_, look.f_82480_, look.f_82481_};
    }

    /**
     * 2.7.7: where a dragon's fire goes, as the server aims it (UoMoves.toward): from the mouth onto the first block on the eye's line
     * (the crosshair) within {@code range}, or that line's end; the look when that point is too near or behind to turn to. Returns the
     * way (unit), how far that point is from the mouth, and 1 if a block stops the line (else 0).
     */
    static double[] aimed(Player player, double[] mouth, float partial, double range) {
        Vec3 eye = player.m_20299_(partial);
        Vec3 look = player.m_20252_(partial);
        Vec3 end = eye.m_82549_(look.m_82490_(range));
        BlockHitResult wall = player.m_9236_().m_45547_(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        boolean hit = wall.m_6662_() == HitResult.Type.BLOCK;
        Vec3 to = hit ? wall.m_82450_() : end;
        double wx = to.f_82479_ - mouth[0];
        double wy = to.f_82480_ - mouth[1];
        double wz = to.f_82481_ - mouth[2];
        double length = Math.sqrt(wx * wx + wy * wy + wz * wz);
        if (length < 6.0 || (wx * look.f_82479_ + wy * look.f_82480_ + wz * look.f_82481_) / length < 0.5) {
            return new double[] {look.f_82479_, look.f_82480_, look.f_82481_, length, hit ? 1.0 : 0.0};
        }
        return new double[] {wx / length, wy / length, wz / length, length, hit ? 1.0 : 0.0};
    }

    /** The human form's mouth (UoMoves.humanMouth), in first person a little under the middle of the view. */
    static double[] humanMouth(Player player, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        Vec3 eye = player.m_20299_(partial);
        Vec3 look = player.m_20252_(partial);
        boolean first = player == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == net.minecraft.client.CameraType.FIRST_PERSON;
        Vec3 at = first ? eye.m_82549_(look.m_82490_(1.6)).m_82520_(0.0, -0.55, 0.0) : eye.m_82549_(look.m_82490_(0.5)).m_82520_(0.0, -0.12, 0.0);
        return new double[] {at.f_82479_, at.f_82480_, at.f_82481_, look.f_82479_, look.f_82480_, look.f_82481_};
    }

    static long clock() {
        return clock;
    }

    // ------------------------------------------------------------------------------------------------------ the drawing

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || SHOTS.isEmpty() && BANGS.isEmpty() && TORNADOES.isEmpty()
            && LINES.isEmpty() && BEAMS.isEmpty()
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
            LINES.clear();
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
            if (shot.kind != S2CUoFx.AIR) {
                line(shot.kind, shot.start, v, shot.size, clock - shot.born + partial, Double.MAX_VALUE, shot.offset, cam, e.getKey());
            }
            switch (shot.kind) {
                case S2CUoFx.AIR -> UoFire.airShot(SINK, x - cx, y - cy, z - cz, v.f_82479_, v.f_82480_, v.f_82481_, shot.size * 1.4, age, travelled,
                    e.getKey());
                case S2CUoFx.BOLT -> UoFire.fireball(SINK, x - cx, y - cy, z - cz, v.f_82479_, v.f_82480_, v.f_82481_, shot.size * 1.25, age, travelled,
                    false, e.getKey());
                default -> UoFire.fireball(SINK, x - cx, y - cy, z - cz, v.f_82479_, v.f_82480_, v.f_82481_, shot.size * 0.55, age, travelled, true,
                    e.getKey());
            }
        }
        for (Line l : LINES) {
            line(l.kind, l.start, l.velocity, l.size, clock - l.born + partial, l.end, l.offset, cam, l.id);
        }
        int n = 0;
        for (Bang bang : BANGS) {
            n++;
            if (bang.kind == S2CUoFx.BOMB ? bang.at.m_82557_(cam) > 300.0 * 300.0 : far(bang.at, cam)) {
                continue;
            }
            double age = clock - bang.born + partial;
            double x = bang.at.f_82479_ - cx;
            double y = bang.at.f_82480_ - cy;
            double z = bang.at.f_82481_ - cz;
            if (bang.kind == S2CUoFx.BOMB) {
                // 2.9.0: Blast Breath's end: the bomb, its cloud seen from far off (FAR is too short for it)
                UoFire.bomb(SINK, x, y, z, bang.radius, age, (int) bang.born * 31 + n);
            } else if (bang.kind == S2CUoFx.AIR) {
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
        for (Map.Entry<Integer, FireBeam> e : BEAMS.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof Player player) || far(player.m_20182_(), cam)) {
                continue;
            }
            FireBeam beam = e.getValue();
            double age = clock - beam.born + partial;
            if (beam.dragon) {
                AIM.set(player, cam, partial);
            } else {
                AIM.human(player, cam, partial, beam.length);
            }
            // as far as the first block on its way (as the server burns it)
            double reach = beam.length;
            Vec3 from = new Vec3(AIM.now[0], AIM.now[1], AIM.now[2]);
            Vec3 way = new Vec3(AIM.now[3], AIM.now[4], AIM.now[5]);
            BlockHitResult wall = level.m_45547_(new ClipContext(from, from.m_82549_(way.m_82490_(beam.length)), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
            if (wall.m_6662_() == HitResult.Type.BLOCK) {
                reach = Math.max(2.0, wall.m_82450_().m_82554_(from));
            }
            UoFire.fireBeam(SINK, AIM, age, beam.life, beam.width, reach, e.getKey());
        }
        for (Map.Entry<Integer, Long> e : BITES.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof Player player) || far(player.m_20182_(), cam)) {
                continue;
            }
            AIM.set(player, cam, partial);
            UoFire.bite(SINK, AIM, (clock - e.getValue() + partial) / BITE_TICKS, e.getKey());
        }
    }

    /** A shot's line of fire (UoFire.line), {@code age} ticks after it left, as far as {@code end} blocks. */
    static void line(int kind, Vec3 start, Vec3 velocity, double size, double age, double end, Vec3 offset, Vec3 cam, int seed) {
        if (far(start, cam) && far(start.m_82549_(velocity.m_82490_(age)), cam)) {
            return;
        }
        double speed = Math.max(1.0E-6, velocity.m_82553_());
        boolean blast = kind == S2CUoFx.BLAST;
        UoFire.line(SINK, start.f_82479_ - cam.f_82479_, start.f_82480_ - cam.f_82480_, start.f_82481_ - cam.f_82481_, velocity.f_82479_ / speed,
            velocity.f_82480_ / speed, velocity.f_82481_ / speed, speed, age, end, blast ? size * 0.42 : size * 1.5,
            blast ? BLAST_LINE_BURN : LINE_BURN, offset == null ? 0.0 : offset.f_82479_, offset == null ? 0.0 : offset.f_82480_,
            offset == null ? 0.0 : offset.f_82481_, seed);
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
            double[] m = mouthWay(player, partial);
            double[] aim = aimed(player, m, partial, 35.0);
            this.now[0] = m[0];
            this.now[1] = m[1];
            this.now[2] = m[2];
            this.now[3] = aim[0];
            this.now[4] = aim[1];
            this.now[5] = aim[2];
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

        /** The human form's beam: from its mouth (first person: under the middle of the view), onto the crosshair, straight. */
        void human(Player player, Vec3 cam, float partial, double range) {
            double[] m = humanMouth(player, partial);
            double[] aim = aimed(player, m, partial, range);
            System.arraycopy(m, 0, this.now, 0, 3);
            this.now[3] = aim[0];
            this.now[4] = aim[1];
            this.now[5] = aim[2];
            this.now[6] = aim[0];
            this.now[7] = aim[1];
            this.now[8] = aim[2];
            this.track = null;
            this.cx = cam.f_82479_;
            this.cy = cam.f_82480_;
            this.cz = cam.f_82481_;
            this.partial = partial;
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
        LINES.clear();
        BEAMS.clear();
    }
}
