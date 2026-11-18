package com.hexhaki.piece.client.fx;

import com.hexhaki.piece.network.S2CWater;
import com.hexhaki.piece.water.WaterBullet;
import com.hexhaki.piece.water.WaterMesh;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 2.3.8: Fishman Karate's water seen (the owner: "make sure the water abilities are super realistic, like the water actually materializes
 * from the air particles in the air, and forms realistic water moves"). Water is drawn as bodies ({@link WaterMesh}: clear in the
 * middle, bright at the rim, glinting), alpha blended and laid back to front: never additive, right without culling.
 *
 * <p>The Water Bullet ({@link WaterBullet}): from the move's start, over the user's raised fist, the air's moisture is drawn in from all
 * round each drop's place as a fine mist (each mote racing in and fading as it arrives) while the drop swells out of nothing, wobbling,
 * one after another along the arch; fired ({@link S2CWater#FIRE}), each drop flies drawn out into a teardrop along its flight, turning
 * after the body it is locked onto, shedding spray; where it bursts ({@link S2CWater#BURST}) a splash: spray, a ring, droplets flung out
 * and falling; at the end of its flight it falls apart into spray; drops still over a fist whose move stopped fall and splash.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class WaterFx {
    static final Random RANDOM = new Random();
    static final int MIST = 0x70D8F2FF;
    static final int MIST_FADE = 0x00E8F8FF;
    /** Fighters gathering drops: entity id -> the game tick their move began, and which drops are gone from over the fist. */
    private static final Map<Integer, Gathering> GATHERING = new HashMap<>();
    /** Drops in flight, by their number. */
    private static final Map<Integer, Flying> FLYING = new HashMap<>();
    /** Droplets thrown off a burst (and fallen drops): small bodies under gravity. */
    private static final List<Bit> BITS = new ArrayList<>();
    /** 2.4.0: Water Prisons: the body held (its id), when its move began, when it was sealed (-1: still gathering) and until when. */
    private static final Map<Integer, Prison> PRISONS = new HashMap<>();

    static final class Prison {
        final long start;
        long sealed = -1;
        long until;

        Prison(long start) {
            this.start = start;
            this.until = start + com.hexhaki.piece.water.WaterPrison.SEAL + 4;
        }
    }

    /** 2.3.9: the Brick Fist's shockwaves rushing out (where from, the way, when: game ticks). */
    private static final List<Shock> SHOCKS = new ArrayList<>();
    private static final int MOST_BITS = 160;

    static final class Gathering {
        final long start;
        final boolean[] gone = new boolean[WaterBullet.COUNT];

        Gathering(long start) {
            this.start = start;
        }
    }

    static final class Flying {
        final int target;
        final long seed;
        Vec3 at;
        Vec3 last;
        Vec3 velocity;
        int age;

        Flying(int target, Vec3 at, Vec3 velocity, long seed) {
            this.target = target;
            this.at = at;
            this.last = at;
            this.velocity = velocity;
            this.seed = seed;
        }
    }

    /** 2.3.9: a Brick Fist's shockwave: from the fist along the look, from game tick {@code start}. */
    record Shock(Vec3 from, Vec3 dir, long start, long seed) {
    }

    static final class Bit {
        Vec3 at;
        Vec3 last;
        Vec3 velocity;
        final double radius;
        final int life;
        final long seed;
        int age;

        Bit(Vec3 at, Vec3 velocity, double radius, int life, long seed) {
            this.at = at;
            this.last = at;
            this.velocity = velocity;
            this.radius = radius;
            this.life = life;
            this.seed = seed;
        }
    }

    private WaterFx() {
    }

    /** A fighter began its Water Bullet at game tick {@code start}: the drops gather over its fist. */
    public static void gather(int user, long start) {
        GATHERING.put(user, new Gathering(start));
    }

    public static void accept(S2CWater message) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        Vec3 at = new Vec3(message.x(), message.y(), message.z());
        Vec3 dir = new Vec3(message.dx(), message.dy(), message.dz());
        switch (message.kind()) {
            case S2CWater.FIRE -> {
                Gathering gathering = GATHERING.get(message.user());
                if (gathering != null && message.index() >= 0 && message.index() < WaterBullet.COUNT) {
                    gathering.gone[message.index()] = true;
                }
                FLYING.put(message.id(), new Flying(message.target(), at, dir, message.id() * 0x9E3779B97F4A7C15L));
                spray(at, dir.m_82490_(0.2), 4, 0.08);
            }
            case S2CWater.BURST -> {
                FLYING.remove(message.id());
                splash(at, dir.m_82541_(), message.target() >= 0 ? 1.0 : 0.8);
            }
            case S2CWater.FADE -> {
                FLYING.remove(message.id());
                spray(at, dir.m_82490_(0.3), 10, 0.18);
            }
            case S2CWater.SPILL -> spill(level, message.user());
            case S2CWater.SHOCK -> shock(at, dir.m_82541_(), level.m_46467_());
            case S2CWater.KNOCKED -> Tumbles.knocked(message.target(), dir);
            case S2CWater.AIR_SHOT, S2CWater.AIR_HIT -> AirFx.accept(message);
            case S2CWater.PRISON_GATHER -> PRISONS.put(message.target(), new Prison(level.m_46467_() - com.hexhaki.piece.water.WaterPrison.GATHER));
            case S2CWater.PRISON_SEAL -> {
                Prison prison = PRISONS.computeIfAbsent(message.target(), id -> new Prison(level.m_46467_()
                    - com.hexhaki.piece.water.WaterPrison.SEAL));
                prison.sealed = level.m_46467_();
                prison.until = prison.sealed + message.index() + 10;
                spray(at.m_82520_(0.0, 0.9, 0.0), Vec3.f_82478_, 14, 0.8);
            }
            case S2CWater.PRISON_BURST -> {
                Prison prison = PRISONS.remove(message.target());
                Entity body = level.m_6815_(message.target());
                double radius = body == null ? 1.2 : com.hexhaki.piece.water.WaterPrison.radius(body.m_20206_(), body.m_20205_());
                if (prison != null) {
                    burstPrison(at, radius, message.index() == 1);
                }
            }
            default -> {
            }
        }
    }

    /** 2.3.9: a Brick Fist's shockwave sets out from the fist: a burst of water off it, then its rings (drawn, shedding spray each tick). */
    static void shock(Vec3 fist, Vec3 dir, long now) {
        SHOCKS.add(new Shock(fist, dir, now, RANDOM.nextLong()));
        spray(fist, dir.m_82490_(0.35), 18, 0.25);
        PieceFx.at(PieceFx.RING, false, 6, fist).colors(0xB0D8F2FF, 0x00D8F2FF).size(0.2, 1.6).bloom(0).spawn();
    }

    /** 2.4.0: a Water Prison bursts (or lets go): the ball of water flung apart and falling, spray every way, a ring. */
    static void burstPrison(Vec3 middle, double radius, boolean burst) {
        spray(middle, Vec3.f_82478_, burst ? 40 : 24, radius * 0.8);
        PieceFx.at(PieceFx.RING, false, 9, middle).colors(0xB0C8EEFF, 0x00C8EEFF).size(0.4, radius * 2.6).bloom(0).spawn();
        int n = PieceFx.count(burst ? 22 : 12);
        for (int i = 0; i < n; i++) {
            Vec3 out = new Vec3(r(1), r(1), r(1)).m_82541_();
            bit(middle.m_82549_(out.m_82490_(radius * 0.9)), out.m_82490_((burst ? 0.16 : 0.06) + RANDOM.nextDouble() * 0.1).m_82520_(0.0, 0.05, 0.0),
                0.04 + RANDOM.nextDouble() * 0.05, 14 + RANDOM.nextInt(8));
        }
    }

    /** The middle of a held body's bubble now (its body's middle). */
    static Vec3 middle(Entity body, float partial) {
        return body.m_20318_(partial).m_82520_(0.0, body.m_20206_() * 0.5, 0.0);
    }

    /** The drops still over a fist fall from it. */
    static void spill(ClientLevel level, int user) {
        Gathering gathering = GATHERING.remove(user);
        Entity entity = level.m_6815_(user);
        if (gathering == null || entity == null) {
            return;
        }
        double age = level.m_46467_() - gathering.start;
        for (int i = 0; i < WaterBullet.COUNT; i++) {
            if (!gathering.gone[i] && WaterBullet.grown(i, age) > 0.3) {
                Vec3 at = place(entity, i, 1.0F);
                bit(at, new Vec3(r(0.03), 0.02, r(0.03)), WaterBullet.RADIUS * WaterBullet.grown(i, age) * 0.9, 18);
            }
        }
    }

    static double r(double spread) {
        return (RANDOM.nextDouble() - 0.5) * 2.0 * spread;
    }

    /** Drop {@code i}'s place over {@code entity}'s raised fist, now. */
    static Vec3 place(Entity entity, int i, float partial) {
        Vec3 feet = entity.m_20318_(partial);
        float yaw = entity.f_19859_ + wrap(entity.m_146908_() - entity.f_19859_) * partial;
        double[] p = WaterBullet.world(WaterBullet.formation(i), feet.f_82479_, feet.f_82480_, feet.f_82481_, yaw);
        return new Vec3(p[0], p[1], p[2]);
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

    /** Fine spray off {@code at}, drifting along {@code drift}. */
    static void spray(Vec3 at, Vec3 drift, int wanted, double spread) {
        int n = PieceFx.count(wanted);
        for (int i = 0; i < n; i++) {
            PieceFx.at(PieceFx.DROP, false, 8 + RANDOM.nextInt(6), at.m_82520_(r(spread), r(spread), r(spread)))
                .velocity(drift.m_82520_(r(0.05), r(0.04) + 0.01, r(0.05))).colors(MIST, MIST_FADE).size(0.06, 0.03).fall(0.02).drag(0.9)
                .bloom(0).spawn();
        }
    }

    /** A drop strikes: spray, a ring along the way it flew, droplets flung out and back and falling. */
    static void splash(Vec3 at, Vec3 dir, double strength) {
        spray(at, dir.m_82490_(-0.12), (int) (16 * strength), 0.12);
        PieceFx.at(PieceFx.RING, false, 7, at).colors(0xA0C8EEFF, 0x00C8EEFF).size(0.15, 1.3 * strength).bloom(0).spawn();
        int n = PieceFx.count((float) (7 * strength));
        for (int i = 0; i < n; i++) {
            Vec3 out = new Vec3(r(1), r(0.6) + 0.5, r(1)).m_82541_();
            // thrown back off what it struck and out round it
            Vec3 velocity = out.m_82490_(0.12 + RANDOM.nextDouble() * 0.14).m_82549_(dir.m_82490_(-0.1));
            bit(at, velocity, 0.028 + RANDOM.nextDouble() * 0.03, 12 + RANDOM.nextInt(8));
        }
    }

    static void bit(Vec3 at, Vec3 velocity, double radius, int life) {
        if (BITS.size() >= MOST_BITS) {
            BITS.remove(0);
        }
        BITS.add(new Bit(at, velocity, radius, life, RANDOM.nextLong()));
    }

    // ------------------------------------------------------------------------------------------------------------- each tick

    @SubscribeEvent
    public static void waterClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (event.phase != TickEvent.Phase.END || level == null || minecraft.m_91104_()) {
            if (level == null) {
                clear();
            }
            return;
        }
        long now = level.m_46467_();
        for (Iterator<Map.Entry<Integer, Gathering>> it = GATHERING.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Gathering> entry = it.next();
            Gathering gathering = entry.getValue();
            Entity user = level.m_6815_(entry.getKey());
            double age = now - gathering.start;
            if (user == null || age > WaterBullet.fireTick(WaterBullet.COUNT - 1) + 6) {
                it.remove();
                continue;
            }
            for (int i = 0; i < WaterBullet.COUNT; i++) {
                double grown = WaterBullet.grown(i, age);
                if (gathering.gone[i] || age < WaterBullet.gathers(i) || grown >= 1.0) {
                    continue;
                }
                // the air's moisture drawn in from all round the drop's place, each mote arriving as it fades
                Vec3 place = place(user, i, 1.0F);
                int n = PieceFx.count(3);
                for (int k = 0; k < n; k++) {
                    Vec3 off = new Vec3(r(1), r(1), r(1)).m_82541_().m_82490_(0.45 + RANDOM.nextDouble() * 0.5);
                    int life = 4 + RANDOM.nextInt(3);
                    PieceFx.at(PieceFx.DROP, false, life, place.m_82549_(off)).velocity(off.m_82490_(-1.0 / life)).colors(0x00D8F2FF, 0xA0E0F6FF)
                        .size(0.035, 0.05).drag(1.0).bloom(0).spawn();
                }
            }
        }
        for (Iterator<Flying> it = FLYING.values().iterator(); it.hasNext(); ) {
            Flying drop = it.next();
            drop.age++;
            if (drop.age > WaterBullet.LIFE + 4) {
                it.remove();
                continue;
            }
            Entity target = drop.target >= 0 ? level.m_6815_(drop.target) : null;
            if (target != null && target.m_6084_()) {
                Vec3 to = target.m_20182_().m_82520_(0.0, target.m_20206_() * 0.5, 0.0).m_82546_(drop.at);
                double[] v = WaterBullet.steer(new double[] {drop.velocity.f_82479_, drop.velocity.f_82480_, drop.velocity.f_82481_},
                    new double[] {to.f_82479_, to.f_82480_, to.f_82481_});
                drop.velocity = new Vec3(v[0], v[1], v[2]);
            }
            drop.last = drop.at;
            drop.at = drop.at.m_82549_(drop.velocity);
            // a thin trail of spray where it passed
            if (RANDOM.nextFloat() < 0.8F * PieceFx.density()) {
                PieceFx.at(PieceFx.DROP, false, 7, drop.last.m_82549_(drop.velocity.m_82490_(RANDOM.nextDouble()))).velocity(new Vec3(r(0.02),
                    0.0, r(0.02))).colors(0x80D8F2FF, 0x00E8F8FF).size(0.045, 0.02).fall(0.015).drag(0.92).bloom(0).spawn();
            }
        }
        for (Iterator<Map.Entry<Integer, Prison>> it = PRISONS.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Prison> entry = it.next();
            Prison prison = entry.getValue();
            Entity body = level.m_6815_(entry.getKey());
            if (body == null || now > prison.until) {
                it.remove();
                continue;
            }
            double radius = com.hexhaki.piece.water.WaterPrison.radius(body.m_20206_(), body.m_20205_());
            Vec3 middle = middle(body, 1.0F);
            if (prison.sealed < 0) {
                // the air's moisture drawn in from all round it, onto the swelling ball
                int n = PieceFx.count(10);
                for (int k = 0; k < n; k++) {
                    Vec3 dirOut = new Vec3(r(1), r(1), r(1)).m_82541_();
                    int life = 4 + RANDOM.nextInt(4);
                    double from = radius * (1.6 + RANDOM.nextDouble() * 0.9);
                    PieceFx.at(PieceFx.DROP, false, life, middle.m_82549_(dirOut.m_82490_(from)))
                        .velocity(dirOut.m_82490_(-(from - radius) / life)).colors(0x00D8F2FF, 0xA0E0F6FF).size(0.05, 0.07).drag(1.0).bloom(0)
                        .spawn();
                }
            } else if (RANDOM.nextFloat() < 0.6F * PieceFx.density()) {
                // air rising inside it, small bright bubbles
                Vec3 in = new Vec3(r(radius * 0.6), -radius * 0.5 + RANDOM.nextDouble() * radius * 0.4, r(radius * 0.6));
                PieceFx.at(PieceFx.MOTE, false, 12 + RANDOM.nextInt(8), middle.m_82549_(in)).velocity(new Vec3(r(0.01), 0.05, r(0.01)))
                    .colors(0xC0E8FAFF, 0x00FFFFFF).size(0.05, 0.03).wander(0.01).bloom(0).spawn();
            }
        }
        for (Iterator<Shock> it = SHOCKS.iterator(); it.hasNext(); ) {
            Shock shock = it.next();
            double age = now - shock.start();
            if (age > com.hexhaki.piece.water.BrickFist.length() + 1) {
                it.remove();
                continue;
            }
            for (int k = 0; k < com.hexhaki.piece.water.BrickFist.rings(); k++) {
                double d = com.hexhaki.piece.water.BrickFist.travelled(k, age);
                if (d < 0) {
                    continue;
                }
                // spray torn off the ring's edge, and a droplet or two flung out of it
                Vec3 middle = shock.from().m_82549_(shock.dir().m_82490_(d));
                double open = com.hexhaki.piece.water.BrickFist.major(k, d);
                Vec3[] across = across(shock.dir());
                int n = PieceFx.count((float) (5 * com.hexhaki.piece.water.BrickFist.left(d)));
                for (int i = 0; i < n; i++) {
                    double a = RANDOM.nextDouble() * Math.PI * 2.0;
                    Vec3 out = across[0].m_82490_(Math.cos(a)).m_82549_(across[1].m_82490_(Math.sin(a)));
                    PieceFx.at(PieceFx.DROP, false, 9 + RANDOM.nextInt(5), middle.m_82549_(out.m_82490_(open)))
                        .velocity(out.m_82490_(0.06).m_82549_(shock.dir().m_82490_(0.25))).colors(MIST, MIST_FADE).size(0.07, 0.03).fall(0.02)
                        .drag(0.88).bloom(0).spawn();
                    if (k == 0 && RANDOM.nextFloat() < 0.35F) {
                        bit(middle.m_82549_(out.m_82490_(open)), out.m_82490_(0.12).m_82549_(shock.dir().m_82490_(0.3)).m_82520_(0.0, 0.06, 0.0),
                            0.03 + RANDOM.nextDouble() * 0.025, 12 + RANDOM.nextInt(6));
                    }
                }
            }
        }
        for (Iterator<Bit> it = BITS.iterator(); it.hasNext(); ) {
            Bit bit = it.next();
            bit.age++;
            if (bit.age > bit.life) {
                it.remove();
                if (RANDOM.nextFloat() < 0.5F) {
                    spray(bit.at, Vec3.f_82478_, 2, 0.03);
                }
                continue;
            }
            bit.last = bit.at;
            bit.velocity = new Vec3(bit.velocity.f_82479_ * 0.96, bit.velocity.f_82480_ * 0.96 - 0.035, bit.velocity.f_82481_ * 0.96);
            bit.at = bit.at.m_82549_(bit.velocity);
        }
    }

    /** Two unit vectors square to {@code dir} and to each other. */
    static Vec3[] across(Vec3 dir) {
        Vec3 side = Math.abs(dir.f_82480_) < 0.9 ? dir.m_82537_(new Vec3(0, 1, 0)) : dir.m_82537_(new Vec3(1, 0, 0));
        side = side.m_82541_();
        return new Vec3[] {side, side.m_82537_(dir).m_82541_()};
    }

    // ------------------------------------------------------------------------------------------------------------- drawn

    @SubscribeEvent
    public static void waterStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || GATHERING.isEmpty() && FLYING.isEmpty() && BITS.isEmpty()
            && SHOCKS.isEmpty() && PRISONS.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        try {
            draw(event.getPoseStack(), event.getCamera().m_90583_(), level, event.getPartialTick());
        } catch (RuntimeException failure) {
            clear(); // water never stops the game
        }
    }

    static void draw(PoseStack pose, Vec3 cam, ClientLevel level, float partial) {
        List<WaterMesh.Body> bodies = new ArrayList<>();
        long now = level.m_46467_();
        for (Map.Entry<Integer, Gathering> entry : GATHERING.entrySet()) {
            Entity user = level.m_6815_(entry.getKey());
            if (user == null) {
                continue;
            }
            double age = now - entry.getValue().start + partial;
            for (int i = 0; i < WaterBullet.COUNT; i++) {
                double grown = WaterBullet.grown(i, age);
                if (entry.getValue().gone[i] || grown <= 0.02) {
                    continue;
                }
                Vec3 at = place(user, i, partial).m_82546_(cam);
                bodies.add(new WaterMesh.Body(at.f_82479_, at.f_82480_, at.f_82481_, WaterBullet.RADIUS * grown, null, 1.0,
                    0.12 * (1.2 - grown), age, entry.getKey() * 31L + i, WaterMesh.DROP));
            }
        }
        for (Map.Entry<Integer, Flying> entry : FLYING.entrySet()) {
            Flying drop = entry.getValue();
            Vec3 at = drop.last.m_82549_(drop.at.m_82546_(drop.last).m_82490_(partial)).m_82546_(cam);
            bodies.add(new WaterMesh.Body(at.f_82479_, at.f_82480_, at.f_82481_, WaterBullet.RADIUS,
                new double[] {drop.velocity.f_82479_, drop.velocity.f_82480_, drop.velocity.f_82481_}, 2.3, 0.05, drop.age + partial, drop.seed,
                WaterMesh.DROP));
        }
        for (Bit bit : BITS) {
            Vec3 at = bit.last.m_82549_(bit.at.m_82546_(bit.last).m_82490_(partial)).m_82546_(cam);
            double speed = bit.velocity.m_82553_();
            bodies.add(new WaterMesh.Body(at.f_82479_, at.f_82480_, at.f_82481_, bit.radius * (1.0 - 0.4 * bit.age / (double) bit.life),
                new double[] {bit.velocity.f_82479_, bit.velocity.f_82480_, bit.velocity.f_82481_}, 1.0 + Math.min(1.5, speed * 4.0), 0.06,
                bit.age + partial, bit.seed, WaterMesh.DROP));
        }
        for (Map.Entry<Integer, Prison> entry : PRISONS.entrySet()) {
            Entity body = level.m_6815_(entry.getKey());
            if (body == null) {
                continue;
            }
            Prison prison = entry.getValue();
            double age = now - prison.start + partial;
            double grown = prison.sealed >= 0 ? 1.0 : com.hexhaki.piece.water.WaterPrison.grown(age);
            if (grown <= 0.02) {
                continue;
            }
            Vec3 at = middle(body, partial).m_82546_(cam);
            double radius = com.hexhaki.piece.water.WaterPrison.radius(body.m_20206_(), body.m_20205_()) * grown;
            // swelling, it shivers hard; held, it barely moves
            bodies.add(new WaterMesh.Body(at.f_82479_, at.f_82480_, at.f_82481_, radius, null, 1.0, prison.sealed >= 0 ? 0.035 : 0.09 * (1.2 - grown),
                age, entry.getKey() * 977L, WaterMesh.PRISON * (0.6 + 0.4 * grown)));
        }
        List<WaterMesh.Ring> rings = new ArrayList<>();
        for (Shock shock : SHOCKS) {
            double age = now - shock.start() + partial;
            for (int k = 0; k < com.hexhaki.piece.water.BrickFist.rings(); k++) {
                double d = com.hexhaki.piece.water.BrickFist.travelled(k, age);
                if (d < 0) {
                    continue;
                }
                Vec3 at = shock.from().m_82549_(shock.dir().m_82490_(d)).m_82546_(cam);
                rings.add(new WaterMesh.Ring(at.f_82479_, at.f_82480_, at.f_82481_, new double[] {shock.dir().f_82479_, shock.dir().f_82480_,
                    shock.dir().f_82481_}, com.hexhaki.piece.water.BrickFist.major(k, d), com.hexhaki.piece.water.BrickFist.minor(k, d), 0.12,
                    age, shock.seed() + k, WaterMesh.PRISON * com.hexhaki.piece.water.BrickFist.left(d)));
            }
        }
        if (bodies.isEmpty() && rings.isEmpty()) {
            return;
        }
        rings.sort((p, q) -> Double.compare(q.x() * q.x() + q.y() * q.y() + q.z() * q.z(), p.x() * p.x() + p.y() * p.y() + p.z() * p.z()));
        bodies.sort((p, q) -> Double.compare(q.x() * q.x() + q.y() * q.y() + q.z() * q.z(), p.x() * p.x() + p.y() * p.y() + p.z() * p.z()));
        // the water lit as the sky is: dim at night, full by day
        double day = Math.max(0.0, Math.min(1.0, level.m_104805_(partial)));
        float light = (float) (0.32 + 0.68 * day);
        Matrix4f matrix = pose.m_85850_().m_252922_();
        MultiBufferSource.BufferSource buffers = Minecraft.m_91087_().m_91269_().m_110104_();
        RenderType type = RenderType.m_269166_();
        VertexConsumer buffer = buffers.m_6299_(type);
        WaterMesh.Sink sink = (xyz, argb) -> {
            for (int k = 0; k < 4; k++) {
                int v = Math.min(k, 2);
                int c = argb[v];
                buffer.m_252986_(matrix, (float) xyz[v * 3], (float) xyz[v * 3 + 1], (float) xyz[v * 3 + 2])
                    .m_85950_(((c >>> 16) & 255) / 255.0F * light, ((c >>> 8) & 255) / 255.0F * light, (c & 255) / 255.0F * light,
                        ((c >>> 24) & 255) / 255.0F)
                    .m_5752_();
            }
        };
        for (WaterMesh.Ring ring : rings) {
            double distance = Math.sqrt(ring.x() * ring.x() + ring.y() * ring.y() + ring.z() * ring.z());
            int around = distance < 6 ? 40 : distance < 16 ? 28 : 16;
            WaterMesh.ring(ring, around, distance < 6 ? 10 : 7, sink);
        }
        for (WaterMesh.Body body : bodies) {
            WaterMesh.body(body, sink);
        }
        buffers.m_109912_(type);
    }

    static void clear() {
        PRISONS.clear();
        SHOCKS.clear();
        GATHERING.clear();
        FLYING.clear();
        BITS.clear();
    }

    @SubscribeEvent
    public static void waterLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
