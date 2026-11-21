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

    /** 2.9.0: and whose (the laser's and the beam's leave from that player's fingertip as it is every frame). */
    record Line(Vec3 from, Vec3 to, double width, long born, int kind, int user) {
    }

    record Spot(Vec3 at, Vec3 dir, double size, int step, long born, int kind) {
    }

    record Bead(Vec3 from, Vec3 to, int flight, double size, long born, int seed) {
    }

    record Cut(Vec3 at, Vec3 axis, double start, double sweep, double radius, long born) {
    }

    record Wave(Vec3 start, Vec3 step, int life, double width, long born) {
    }

    record Clone(Vec3 origin, Vec3 spot, int target, int index, long born, int user) {
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
    /** 2.9.6: per player gone as light (Z held, or a dash): {since, until} (clock ticks). */
    static final Map<Integer, long[]> LIGHTS = new HashMap<>();
    /** 2.9.7: per player flying at them with Ama no Murakumo (a dash seen, not as light): until (clock ticks). */
    static final Map<Integer, Long> LUNGES = new HashMap<>();
    static final Map<Integer, Long> SWORDS = new HashMap<>();
    private static final Map<Integer, Trail> TRAILS = new HashMap<>();
    static final double FAR = 160.0;

    private PikaFx() {
    }

    public static void accept(S2CUoFx m) {
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        Vec3 a = new Vec3(m.ax(), m.ay(), m.az());
        switch (m.kind()) {
            case PikaMoves.LASER -> LINES.add(new Line(at, a, m.size(), clock, PikaMoves.LASER, m.user()));
            case PikaMoves.BEAM -> {
                LINES.add(new Line(at, a, m.size(), clock, PikaMoves.BEAM, m.id() == 1 ? -1 : m.user()));
                motes(a, 18, 0.5);
            }
            case PikaMoves.CHARGE_ON -> {
                CHARGES.put(m.user(), clock);
                held(m.user(), CHARGES, 1.0F, 1.4);
            }
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
                LINES.add(new Line(at, a, 1.0, clock, PikaMoves.YATA_PATH, -1));
                FORMS.remove(m.user());
                motes(a.m_82520_(0, 1.0, 0), 10, 0.3);
            }
            case PikaMoves.VOLLEY_ON -> VOLLEYS.put(m.user(), clock);
            case PikaMoves.VOLLEY_OFF -> VOLLEYS.remove(m.user());
            case PikaMoves.BEAD -> BEADS.add(new Bead(at, a, Math.max(1, m.ticks()), m.size(), clock, m.id()));
            case PikaMoves.SWORD_ON -> {
                SWORDS.put(m.user(), clock + m.ticks());
                // 2.9.0: Ama no Murakumo hums while it is out (a web recording, round and round)
                int id = m.user();
                com.hexhaki.piece.client.LoopSound.play(com.hexhaki.piece.pika.PikaSounds.HUM, entity(id), 0.35F, 1.0F, true, true, 1.0,
                    () -> sword(id));
            }
            case PikaMoves.SWORD_OFF -> SWORDS.remove(m.user());
            case PikaMoves.SLASH -> CUTS.add(new Cut(at, a.m_82541_(), m.bx(), m.by(), m.bz(), clock));
            case PikaMoves.WAVE -> WAVES.add(new Wave(at, a, m.ticks(), m.size(), clock));
            case PikaMoves.KICK_CHARGE_ON -> {
                KICK_CHARGES.put(m.user(), clock);
                held(m.user(), KICK_CHARGES, 0.8F, 0.5);
            }
            case PikaMoves.KICK_CHARGE_OFF -> KICK_CHARGES.remove(m.user());
            case PikaMoves.LIGHT_KICK -> {
                // 2.9.6: sent as the kick lands (he came as light, a dash): the kicked one's trail and the blow's flash
                if (m.id() >= 0) {
                    KICKED.add(new Kicked(m.id(), clock));
                    SPOTS.add(new Spot(a.m_82520_(0, 1.0, 0), new Vec3(m.bx(), m.by(), m.bz()), 2.5 + 2.0 * m.size(), 8, clock + 1,
                        PikaMoves.KICK));
                }
            }
            case PikaMoves.CLONE -> CLONES.add(new Clone(at, a, m.id(), m.ticks(), clock, m.user()));
            case PikaMoves.DODGE -> SPOTS.add(new Spot(at, Vec3.f_82478_, 1.2, 0, clock, PikaMoves.DODGE));
            case PikaMoves.DIVE -> {
                LINES.add(new Line(a, at, 1.2, clock, PikaMoves.YATA_PATH, -1));
                SPOTS.add(new Spot(at, Vec3.f_82478_, m.size(), 0, clock + 1, PikaMoves.BURST));
            }
            case PikaMoves.LIGHT_ON -> {
                // 2.9.6: the body gone as light (Z held): its flash, its sound for as long as it lasts, and one's own body moved as light
                LIGHTS.put(m.user(), new long[] {clock, clock + m.ticks() + 10});
                SPOTS.add(new Spot(at, a, 1.6, 0, clock, PikaMoves.LIGHT_ON));
                motes(at, 10, 0.35);
                int id = m.user();
                com.hexhaki.piece.client.LoopSound.play(com.hexhaki.piece.pika.PikaSounds.LIGHT_TRAVEL, entity(id), 0.8F, 1.0F, true, true, 0.9,
                    () -> LIGHTS.containsKey(id));
                if (self(id)) {
                    glide = Glide.free(a, m.size());
                }
            }
            case PikaMoves.LIGHT_OFF -> {
                LIGHTS.remove(m.user());
                SPOTS.add(new Spot(at, a, 1.4, 0, clock, PikaMoves.LIGHT_OFF));
                motes(at, 8, 0.3);
                if (self(m.user()) && glide != null && glide.mode == Glide.FREE) {
                    // a body again where the light is, still going with some of its speed (off the sea: up into the sky)
                    Minecraft.m_91087_().f_91074_.m_20334_(glide.dx * PikaMoves.LIGHT_CARRY, glide.dy * PikaMoves.LIGHT_CARRY,
                        glide.dz * PikaMoves.LIGHT_CARRY);
                    glide = null;
                }
            }
            case PikaMoves.REFLECT -> {
                SPOTS.add(new Spot(at, a, m.size(), 0, clock, PikaMoves.REFLECT));
                motes(at, 6, 0.3);
            }
            case PikaMoves.DASH -> {
                // 2.9.6: across as light over a few ticks; one's own body carried there by the game's own movement. 2.9.7: or seen
                // (by 1: Ama no Murakumo's lunge, the body flying at them with the sword out ahead, a trail of light behind)
                if (m.by() > 0) {
                    LUNGES.put(m.user(), clock + m.ticks() + 1);
                } else {
                    LIGHTS.put(m.user(), new long[] {clock, clock + m.ticks() + 1});
                    Vec3 from = a.m_82520_(0, 0.9, 0);
                    SPOTS.add(new Spot(from, at.m_82546_(a), 1.2, 0, clock, PikaMoves.LIGHT_ON));
                }
                if (self(m.user())) {
                    glide = Glide.dash(at, m.id(), m.ticks(), (int) m.bx());
                }
            }
            default -> {
            }
        }
    }

    static Entity entity(int id) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? null : level.m_6815_(id);
    }

    /** 2.9.0: when the light held at the fingertip or in the leg goes from gathering to humming (ticks). */
    static final int HOLD_AT = 30;

    /**
     * 2.9.0 (the owner: "The sounds are atrocious"): light gathering ({@code in}: CHARGES or KICK_CHARGES) is heard as it gathers (a web
     * recording's rise), then held, humming, until it is let go (tick, HOLD_AT), fading out then, never cut off.
     */
    static void held(int id, Map<Integer, Long> in, float pitch, double up) {
        Long since = in.get(id);
        com.hexhaki.piece.client.LoopSound.play(com.hexhaki.piece.pika.PikaSounds.CHARGE, entity(id), 0.9F, pitch, false, false, up,
            () -> since.equals(in.get(id)));
    }

    static void hum(Map<Integer, Long> in, float pitch, double up) {
        for (Map.Entry<Integer, Long> e : in.entrySet()) {
            if (clock - e.getValue() == HOLD_AT) {
                int id = e.getKey();
                Long since = e.getValue();
                com.hexhaki.piece.client.LoopSound.play(com.hexhaki.piece.pika.PikaSounds.CHARGE_HOLD, entity(id), 0.7F, pitch, true, true,
                    up, () -> since.equals(in.get(id)));
            }
        }
    }

    /** Whether {@code id}'s sword of light is out (PikaClient draws it in the hand). */
    public static boolean sword(int id) {
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
        SPOTS.removeIf(s -> clock - s.born > (s.kind == PikaMoves.BURST ? LightArt.burstLife(s.size) : s.kind == PikaMoves.REFLECT
            || s.kind == PikaMoves.LIGHT_ON || s.kind == PikaMoves.LIGHT_OFF ? LightArt.REFLECT_LIFE : 9) + 1);
        LIGHTS.entrySet().removeIf(e -> e.getValue()[1] < clock);
        LUNGES.entrySet().removeIf(e -> e.getValue() < clock);
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
        // 2.9.0 (the owner: "he can like fly like super fast ... turns into light"): flying and sprinting, the local player goes as light
        // where they look, LIGHT_FLIGHT blocks a tick (the body becomes a streak of light: lit, below)
        net.minecraft.client.player.LocalPlayer self = minecraft.f_91074_;
        if (glide != null) {
            glide(self, level);
        } else if (self != null && self.m_150110_().f_35935_ && self.m_20142_() && com.hexhaki.fruit.client.FruitClient.fruit(self.m_19879_()) == Fruit.PIKA
            && minecraft.f_91080_ == null) {
            Vec3 look = self.m_20154_();
            self.m_20334_(look.f_82479_ * LIGHT_FLIGHT, look.f_82480_ * LIGHT_FLIGHT, look.f_82481_ * LIGHT_FLIGHT);
        }
        // trails: a player flying as light, or streaming
        for (Player player : level.m_6907_()) {
            boolean streaming = lit(player);
            boolean flying = player.m_150110_().f_35935_ && com.hexhaki.fruit.client.FruitClient.fruit(player.m_19879_()) == Fruit.PIKA
                && player.m_20184_().m_82553_() > 0.35;
            Trail trail = TRAILS.get(player.m_19879_());
            if (streaming || flying || LUNGES.containsKey(player.m_19879_())) {
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
        hum(CHARGES, 1.0F, 1.4);
        hum(KICK_CHARGES, 0.8F, 0.5);
        // light gathering in a leg or at a fingertip throws motes
        for (Map.Entry<Integer, Long> e : KICK_CHARGES.entrySet()) {
            if (level.m_6815_(e.getKey()) instanceof Player player) {
                Vec3 foot = foot(player, 1.0F);
                mote(foot.m_82520_(r(0.3), r(0.2), r(0.3)), new Vec3(r(0.03), 0.06 + RANDOM.nextDouble() * 0.05, r(0.03)), 14);
            }
        }
    }

    /** 2.9.0: the light flight's speed (blocks a tick: some 40 a second), and how fast a flier must go to be drawn as light. */
    static final double LIGHT_FLIGHT = 2.0;
    static final double LIT = 0.9;

    /** Whether {@code player} is going as light: flying with the Light fruit faster than {@link #LIT} blocks a tick. */
    static boolean lit(Player player) {
        if (LIGHTS.containsKey(player.m_19879_())) {
            return true; // 2.9.6: Z held, or a dash
        }
        if (!player.m_150110_().f_35935_ || com.hexhaki.fruit.client.FruitClient.fruit(player.m_19879_()) != Fruit.PIKA) {
            return false;
        }
        double dx = player.m_20185_() - player.f_19790_;
        double dy = player.m_20186_() - player.f_19791_;
        double dz = player.m_20189_() - player.f_19792_;
        return dx * dx + dy * dy + dz * dz > LIT * LIT;
    }

    /** 2.9.0: a player going as light is not seen: only the light (its streak and its glow, drawn with the trails). */
    @SubscribeEvent
    public static void asLight(net.minecraftforge.client.event.RenderPlayerEvent.Pre event) {
        if (lit(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /** Whether {@code id} is the local player. */
    static boolean self(int id) {
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        return self != null && self.m_19879_() == id;
    }

    /**
     * 2.9.6 (the owner: "turn you into light itself, and travel in a beam where you're looking at and reflect against any object/water";
     * "smooth"): one's own body moved as light, by the game's own movement (its velocity set a tick ahead, every movement key ignored),
     * so it glides on screen from frame to frame and meets blocks as the server's movement check does. FREE (Z held): straight on at its
     * speed; where a block cut the last tick's step short along an axis, that axis is turned back (a block's faces are square to the
     * axes, so that is the reflection off it), and water or lava met from above throws it back up. DASH: to a place in so many ticks
     * (following whom it was sent after), then HOLD: hung there, facing them, a few ticks.
     */
    static final class Glide {
        static final int FREE = 0;
        static final int DASH = 1;
        static final int HOLD = 2;
        int mode;
        double dx;
        double dy;
        double dz;
        double speed;
        Vec3 to;
        int target;
        Vec3 targetFrom;
        int left;
        int hold;
        Vec3 last;
        Vec3 step = Vec3.f_82478_;
        boolean flipY;

        static Glide free(Vec3 way, double speed) {
            Glide g = new Glide();
            Vec3 unit = way.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : way.m_82541_();
            g.mode = FREE;
            g.dx = unit.f_82479_;
            g.dy = unit.f_82480_;
            g.dz = unit.f_82481_;
            g.speed = speed;
            return g;
        }

        static Glide dash(Vec3 to, int target, int ticks, int hold) {
            Glide g = new Glide();
            g.mode = DASH;
            g.to = to;
            g.target = target;
            Entity e = target >= 0 ? entity(target) : null;
            g.targetFrom = e != null ? e.m_20182_() : null;
            g.left = Math.max(1, ticks);
            g.hold = hold;
            return g;
        }
    }

    static Glide glide;

    /** A tick of one's own body as light (after the game has moved it): its next step. */
    static void glide(net.minecraft.client.player.LocalPlayer self, ClientLevel level) {
        Glide g = glide;
        if (self == null || !self.m_6084_() || self.m_20159_() || g.mode == Glide.FREE && !LIGHTS.containsKey(self.m_19879_())) {
            glide = null; // gone, riding, or its time up (should the server's word that it reformed never come)
            return;
        }
        Vec3 pos = self.m_20182_();
        Vec3 moved = g.last == null ? g.step : pos.m_82546_(g.last);
        Vec3 step;
        if (g.mode == Glide.FREE) {
            if (g.flipY) {
                g.dy = -g.dy;
                g.flipY = false;
            } else {
                net.minecraft.world.entity.LivingEntity body = self; // the flags read as the original reads them (through LivingEntity)
                if (body.f_19862_) {
                    if (Math.abs(g.step.f_82479_) > 0.01 && Math.abs(moved.f_82479_) < Math.abs(g.step.f_82479_) - 0.001) {
                        g.dx = -g.dx;
                    }
                    if (Math.abs(g.step.f_82481_) > 0.01 && Math.abs(moved.f_82481_) < Math.abs(g.step.f_82481_) - 0.001) {
                        g.dz = -g.dz;
                    }
                }
                if (body.f_19863_ && Math.abs(g.step.f_82480_) > 0.01 && Math.abs(moved.f_82480_) < Math.abs(g.step.f_82480_) - 0.001) {
                    g.dy = -g.dy;
                }
            }
            step = new Vec3(g.dx * g.speed, g.dy * g.speed, g.dz * g.speed);
            if (g.dy < -0.01) {
                // water (or lava) met from above: down onto its face this tick, back up off it the next
                Vec3 feet = pos.m_82520_(0, 0.05, 0);
                if (level.m_6425_(net.minecraft.core.BlockPos.m_274561_(feet.f_82479_, feet.f_82480_, feet.f_82481_)).m_76178_()) {
                    net.minecraft.world.phys.BlockHitResult hit = level.m_45547_(new net.minecraft.world.level.ClipContext(feet, feet.m_82549_(step),
                        net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.ANY, self));
                    if (hit.m_6662_() == net.minecraft.world.phys.HitResult.Type.BLOCK && !level.m_6425_(hit.m_82425_()).m_76178_()) {
                        step = step.m_82490_(Math.max(0.0, hit.m_82450_().m_82554_(feet) / g.speed));
                        g.flipY = true;
                    }
                }
            }
        } else if (g.mode == Glide.DASH) {
            Vec3 to = g.to;
            Entity e = g.target >= 0 ? level.m_6815_(g.target) : null;
            if (e != null && g.targetFrom != null) {
                to = to.m_82549_(e.m_20182_().m_82546_(g.targetFrom));
            }
            step = to.m_82546_(pos).m_82490_(1.0 / Math.max(1, g.left));
            if (--g.left <= 0) {
                g.mode = Glide.HOLD;
                if (e != null) {
                    face(self, to, e);
                }
            }
        } else {
            step = Vec3.f_82478_;
            if (--g.hold < 0) {
                glide = null;
            }
        }
        self.m_20334_(step.f_82479_, step.f_82480_, step.f_82481_);
        self.f_19789_ = 0.0F;
        g.last = pos;
        g.step = step;
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

    /** 2.9.6: while one's own body is light, no movement key moves it (it goes as the light goes). */
    @SubscribeEvent
    public static void steady(net.minecraftforge.client.event.MovementInputUpdateEvent event) {
        if (glide == null || event.getEntity() != Minecraft.m_91087_().f_91074_) {
            return;
        }
        net.minecraft.client.player.Input input = event.getInput();
        input.f_108566_ = 0.0F;
        input.f_108567_ = 0.0F;
        input.f_108572_ = false;
        input.f_108573_ = false;
    }

    /** 2.9.6: light has no hands: in first person none is drawn while one's own body is light. */
    @SubscribeEvent
    public static void noHands(net.minecraftforge.client.event.RenderHandEvent event) {
        net.minecraft.client.player.LocalPlayer self = Minecraft.m_91087_().f_91074_;
        if (self != null && LIGHTS.containsKey(self.m_19879_())) {
            event.setCanceled(true);
        }
    }

    static void push(double[] points, int count, Vec3 p) {
        System.arraycopy(points, 0, points, 3, Math.min(count, points.length / 3 - 1) * 3);
        points[0] = p.f_82479_;
        points[1] = p.f_82480_;
        points[2] = p.f_82481_;
    }

    // ------------------------------------------------------------------------------------------------------ where things are on a body

    /**
     * The right fingertip at {@code partial}: 2.9.0, where the pose of the laser's clip puts it this frame (client/FirstPersonLight: in
     * first person the arm drawn in the view, in third person the model's), else where the server places it.
     */
    static Vec3 fingertip(Player player, float partial) {
        Vec3 posed = com.hexhaki.piece.client.FirstPersonLight.fingertip(player, partial);
        if (posed != null) {
            return posed;
        }
        return serverTip(player, partial);
    }

    static Vec3 serverTip(Player player, float partial) {
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

    /** 2.9.7: an open palm thrust out toward the ground ahead (pika_yasakani's pose): {@code side} 1 the right hand, -1 the left. */
    static Vec3 palm(Player player, float partial, int side) {
        Vec3 look = player.m_20252_(partial);
        Vec3 flat = new Vec3(look.f_82479_, 0, look.f_82481_);
        flat = flat.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.m_82541_();
        Vec3 right = new Vec3(-flat.f_82481_, 0, flat.f_82479_);
        return player.m_20318_(partial).m_82520_(0, 0.95, 0).m_82549_(flat.m_82490_(0.62)).m_82549_(right.m_82490_(0.42 * side));
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
        SINK.matrix = pose.m_85850_().m_252922_();
        FireMesh.quality = Math.max(0.35, PieceFx.density());
        SKIN.buffers = buffers;
        com.hexhaki.piece.client.render.Bulk.read(pose.m_85850_(), SKIN.m, SKIN.n);
        Vec3 cam = event.getCamera().m_90583_();
        float partial = event.getPartialTick();
        try {
            // 2.9.0 (the owner: "The clones are not showing"): the game's buffer source has one shared builder for every type it holds no
            // buffer of its own for; asking it for a clone's skin type ended the light's (debugQuads) batch and turned the builder to
            // the skin's vertex format under the light's writer, whose next quad threw (a vertex short of the format's elements) and
            // cleared every Light effect. So the clones' skins go first, each type ended before the next is asked for, their light kept
            // aside (CLONE_LIGHT) and written with everything else's once the light's own type is the only one open.
            CLONE_LIGHT.clear();
            for (Clone c : CLONES) {
                clone(level, c, cam, partial);
                SKIN.end();
            }
            SINK.buffer = buffers.m_6299_(type);
            CLONE_LIGHT.replay(SINK);
            draw(level, cam, partial);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: a Light fruit move could not be drawn", failure);
            clear();
        }
        SKIN.end();
        if (SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        SINK.buffer = null;
    }

    /** 2.9.0: quads kept aside (a clone's light, drawn before the light's type is open) and written later in the order they came. */
    static final class Deferred implements FireMesh.Sink {
        double[] xyz = new double[12 * 256];
        int[] argb = new int[4 * 256];
        int quads;

        @Override
        public void quad(double[] q, int[] c) {
            if ((this.quads + 1) * 12 > this.xyz.length) {
                this.xyz = java.util.Arrays.copyOf(this.xyz, this.xyz.length * 2);
                this.argb = java.util.Arrays.copyOf(this.argb, this.argb.length * 2);
            }
            System.arraycopy(q, 0, this.xyz, this.quads * 12, 12);
            System.arraycopy(c, 0, this.argb, this.quads * 4, 4);
            this.quads++;
        }

        void replay(FireMesh.Sink to) {
            double[] q = new double[12];
            int[] c = new int[4];
            for (int i = 0; i < this.quads; i++) {
                System.arraycopy(this.xyz, i * 12, q, 0, 12);
                System.arraycopy(this.argb, i * 4, c, 0, 4);
                to.quad(q, c);
            }
        }

        void clear() {
            this.quads = 0;
        }
    }

    static final Deferred CLONE_LIGHT = new Deferred();

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

    /**
     * 2.8.8: a clone's skin (LightClone): the user's own skin, washed in light (LightClone.WASH), full bright, through the game's
     * translucent entity type; every type used this frame ended together.
     */
    static final class SkinWriter implements com.hexhaki.piece.pika.LightClone.Skin {
        MultiBufferSource.BufferSource buffers;
        final float[] m = new float[12];
        final float[] n = new float[9];
        RenderType type;
        VertexConsumer out;
        final List<RenderType> used = new ArrayList<>();

        void use(net.minecraft.resources.ResourceLocation skin) {
            this.type = RenderType.m_110473_(skin);
            this.out = this.buffers.m_6299_(this.type);
            if (!this.used.contains(this.type)) {
                this.used.add(this.type);
            }
        }

        @Override
        public void quad(double[] xyz, float[] uv, int argb) {
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
                    0xF000F0, 0.0F, 1.0F, 0.0F);
            }
        }

        void end() {
            for (RenderType t : this.used) {
                this.buffers.m_109912_(t);
            }
            this.used.clear();
            this.out = null;
        }
    }

    static final SkinWriter SKIN = new SkinWriter();

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
            Vec3 from = l.from;
            if (l.user >= 0 && (l.kind == PikaMoves.LASER || l.kind == PikaMoves.BEAM) && level.m_6815_(l.user) instanceof Player shooter) {
                // 2.9.0 (the owner: "It's not on the, in front of the fist. It's just moving around. Like as I move around"): out of the
                // fingertip as it is this frame, not where the server last had it
                Vec3 tip = fingertip(shooter, partial);
                from = tip;
            }
            if (from != l.from) {
                l = new Line(from, l.to, l.width, l.born, l.kind, l.user);
            }
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
                case PikaMoves.REFLECT -> LightArt.reflect(SINK, x, y, z, s.dir.f_82479_, s.dir.f_82480_, s.dir.f_82481_, s.size, age, (int) s.born);
                case PikaMoves.LIGHT_ON, PikaMoves.LIGHT_OFF -> LightArt.flare(SINK, x, y, z, s.size, age, (int) s.born);
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
            boolean streaming = lit(player);
            if (streaming) {
                LightArt.ribbon(SINK, pts, t.count, 0.8, 1.0);
            } else if (LUNGES.containsKey(e.getKey())) {
                LightArt.trail(SINK, pts, t.count, 0.6, 1.0); // 2.9.7: the lunge's trail of light
            } else {
                LightArt.trail(SINK, pts, t.count, 0.35, 0.7);
            }
            boolean inside = player == Minecraft.m_91087_().f_91074_ && Minecraft.m_91087_().f_91066_.m_92176_().m_90612_();
            if (streaming && inside && LIGHTS.containsKey(player.m_19879_())) {
                // 2.9.6: one's own body as light, seen from inside it: light rushing past round the view
                Vec3 way = glide != null && glide.mode == Glide.FREE ? new Vec3(glide.dx, glide.dy, glide.dz)
                    : new Vec3(pts[0] - pts[3], pts[1] - pts[4], pts[2] - pts[5]);
                if (way.m_82556_() > 1.0E-6) {
                    way = way.m_82541_();
                    LightArt.rush(SINK, way.f_82479_, way.f_82480_, way.f_82481_, clock + partial, e.getKey());
                }
            }
            if (streaming && !inside) {
                // 2.9.6: the body as light: a white-hot head drawn out along its way, a star of light on it, rings thrown off behind
                double wx = pts[0] - pts[3];
                double wy = pts[1] - pts[4];
                double wz = pts[2] - pts[5];
                double wl = Math.sqrt(wx * wx + wy * wy + wz * wz);
                if (wl < 1.0E-3) {
                    wx = 0;
                    wy = 1;
                    wz = 0;
                    wl = 1;
                }
                LightArt.head(SINK, pts[0], pts[1], pts[2], wx / wl, wy / wl, wz / wl, clock + partial, e.getKey());
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
                // 2.9.7: as at Egghead, the light pouring out of both open palms, a star of light on each
                double age = clock - e.getValue() + partial;
                for (int side = -1; side <= 1; side += 2) {
                    Vec3 h = palm(player, partial, side);
                    LightArt.palm(SINK, h.f_82479_ - cx, h.f_82480_ - cy, h.f_82481_ - cz, age, e.getKey() * 2 + side);
                }
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
                LightArt.bullet(CLONE_LIGHT, p.f_82479_ - cam.f_82479_, p.f_82480_ - cam.f_82480_, p.f_82481_ - cam.f_82481_, v.f_82479_ * 0.3, v.f_82480_ * 0.3,
                    v.f_82481_ * 0.3, 0.6, age, c.index);
            }
            return;
        }
        // 2.8.8 (the owner: "the light clones are bad to look it"): the user's own body of light (pika/LightClone), running at the foe
        // with afterimages behind it, two cuts of Ama no Murakumo (the Light fruit's own sword cuts), then gone in a burst of light
        Entity owner = level.m_6815_(c.user);
        if (owner instanceof net.minecraft.client.player.AbstractClientPlayer player) {
            SKIN.use(player.m_108560_());
        } else {
            SKIN.out = null;
        }
        Vec3 face = target.m_82546_(stand);
        double yaw = Math.atan2(-face.f_82479_, face.f_82481_);
        double shimmer = age * 0.4 + c.index;
        if (age < 10.0) {
            // 2.9.7 (the owner: "adapt the other abilities to your reference gifs"): as at Egghead, every clone flies at its foe through
            // the air, up and over in an arc, laid out with its sword of light thrust out ahead (Ama no Murakumo's lunge), afterimages
            // behind it
            double t = (age - 4.0) / 6.0;
            double over = 2.5 + 0.5 * (c.index % 3);
            for (int ghost = 2; ghost >= 0; ghost--) {
                double tg = Math.max(0.0, t - ghost * 0.14);
                double s = tg * tg * (3 - 2 * tg);
                Vec3 feet = rise.m_82549_(stand.m_82546_(rise).m_82490_(s)).m_82520_(0, over * Math.sin(Math.PI * s), 0);
                if (far(feet, cam)) {
                    continue;
                }
                float[][] pose = com.hexhaki.piece.client.npc.StylePoser.sample("pika_lunge", 9.0F);
                double alpha = Math.min(1.0, (age - 4.0) / 1.5) * (ghost == 0 ? 1.0 : ghost == 1 ? 0.35 : 0.15);
                com.hexhaki.piece.pika.LightClone.body(SKIN, CLONE_LIGHT, pose, place(feet, cam, yaw), alpha, ghost == 0, shimmer);
            }
            return;
        }
        if (far(stand, cam)) {
            return;
        }
        double alpha = 1.0;
        float[][] pose;
        if (age < 22.0) {
            int cut = (int) ((age - 10.0) / 6.0);
            double u = (age - 10.0) % 6.0;
            pose = com.hexhaki.piece.client.npc.StylePoser.sample(cut == 0 ? "pika_sword_1" : "pika_sword_2", (float) (u * 8.0 / 6.0));
            if (u < 3.0) {
                // the cut's arc round the clone's side axis (square to where it faces), as 2.7.8 swept it
                double fl = Math.max(1.0E-6, Math.sqrt(face.f_82479_ * face.f_82479_ + face.f_82481_ * face.f_82481_));
                double turn = Math.atan2(face.f_82479_, face.f_82481_);
                LightArt.slash(CLONE_LIGHT, stand.f_82479_ - cam.f_82479_, stand.f_82480_ + 1.0 - cam.f_82480_, stand.f_82481_ - cam.f_82481_,
                    face.f_82481_ / fl, 0.0, -face.f_82479_ / fl, 1.8, turn - 1.3, 2.6, u);
            }
        } else {
            pose = com.hexhaki.piece.client.npc.StylePoser.sample("pika_sword_2", 8.0F);
            alpha = Math.max(0.0, 1.0 - (age - 22.0) / 4.0);
            LightArt.burst(CLONE_LIGHT, stand.f_82479_ - cam.f_82479_, stand.f_82480_ + 1.0 - cam.f_82480_, stand.f_82481_ - cam.f_82481_, 1.6, age - 22.0,
                c.index * 7 + (int) c.born);
        }
        com.hexhaki.piece.pika.LightClone.body(SKIN, CLONE_LIGHT, pose, place(stand, cam, yaw), alpha, true, shimmer);
    }

    /** Feet at {@code feet}, facing {@code yaw} (radians, the game's way), eye-relative. */
    static com.hexhaki.piece.style.StyleShow.Place place(Vec3 feet, Vec3 cam, double yaw) {
        return new com.hexhaki.piece.style.StyleShow.Place(feet.f_82479_ - cam.f_82479_, feet.f_82480_ - cam.f_82480_, feet.f_82481_ - cam.f_82481_, yaw);
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
        LIGHTS.clear();
        LUNGES.clear();
        glide = null;
        SWORDS.clear();
        TRAILS.clear();
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
