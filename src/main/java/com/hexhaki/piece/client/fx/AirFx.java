package com.hexhaki.piece.client.fx;

import com.hexhaki.piece.air.AirMesh;
import com.hexhaki.piece.network.S2CWater;
import com.hexhaki.piece.style.StyleMove;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 2.4.1: Six Powers' air in 3D (the owner: "Improve air particles, speed them up and make them 3d. (finger pistol with haki shoots
 * projectiles)"). Cut air is a body ({@link AirMesh}: a crescent, clear, its rim bright), a blow's shock a ring of air
 * ({@link WaterMesh#ring} in {@link WaterMesh#AIR}), alpha blended and laid back to front like the water, never additive:
 * <ul>
 *   <li>the Storm Leg: a crescent of air cut off the kick, tilted the way the leg swept, flying out fast and gone at its reach;</li>
 *   <li>the Air Blade: a big crescent racing out at {@link #BLADE_SPEED} blocks a tick (it was 2.2), two fainter after it;</li>
 *   <li>every other blow (the finger pistols, the palms, Shave's): a ring of air bursting off where it struck, opening fast;</li>
 *   <li>the finger pistol with Armament Haki: a black, glossy bullet of hardened air shot from the finger
 *   ({@link S2CWater#AIR_SHOT}, {@link S2CWater#AIR_HIT}), a ring of air off the finger.</li>
 * </ul>
 * The air's streaks fly faster than before ({@link #STREAK_SPEED}).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class AirFx {
    static final Random RANDOM = new Random();
    /** How fast the Air Blade's crescent races out (blocks a tick), and the air's streaks (times as fast as they were). */
    public static final double BLADE_SPEED = 4.5;
    public static final double STREAK_SPEED = 1.8;
    private static final List<Cut> CUTS = new ArrayList<>();
    private static final List<Burst> RINGS = new ArrayList<>();
    private static final Map<Integer, Shot> SHOTS = new HashMap<>();

    /** A crescent flying: from, the way, its tips' line, radius, speed, how long it lasts, when (game ticks), how strong. */
    record Cut(Vec3 from, Vec3 dir, double[] across, double radius, double speed, double life, long start, double strength) {
    }

    /** A ring of air opening where a blow struck. */
    record Burst(Vec3 at, Vec3 dir, double from, double to, double life, long start) {
    }

    static final class Shot {
        Vec3 at;
        Vec3 last;
        final Vec3 velocity;
        int age;

        Shot(Vec3 at, Vec3 velocity) {
            this.at = at;
            this.last = at;
            this.velocity = velocity;
        }
    }

    private AirFx() {
    }

    static double r(double spread) {
        return (RANDOM.nextDouble() - 0.5) * 2.0 * spread;
    }

    static long now() {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? 0L : level.m_46467_();
    }

    /** A Six Powers blow thrown (its weight, {@code round}: all round): its air cut or its ring, and fast streaks. */
    public static void swing(StyleMove move, Vec3 feet, Vec3 facing, double height, int weight, boolean round) {
        Vec3 point = feet.m_82549_(facing.m_82490_(1.2)).m_82520_(0.0, height, 0.0);
        double[] side = {-facing.f_82481_, 0.0, facing.f_82479_};
        if (move == StyleMove.SP_STORM) {
            // the leg swept up and across: its cut tilted that way, flying out to the blow's reach
            CUTS.add(new Cut(point, facing, AirMesh.tilted(new double[] {facing.f_82479_, 0, facing.f_82481_}, side, -30), 1.0, 2.0, 2.6, now(), 1.0));
        } else if (!round) {
            RINGS.add(new Burst(point, facing, 0.12, 0.7 + 0.25 * weight, 5.0, now()));
        } else {
            RINGS.add(new Burst(feet.m_82520_(0.0, height, 0.0), new Vec3(0, 1, 0), 0.6, 3.2 + 0.4 * weight, 6.0, now()));
        }
        Vec3 sideways = new Vec3(side[0], 0.0, side[2]);
        int n = PieceFx.count(6 + weight * 3);
        for (int i = 0; i < n; i++) {
            Vec3 velocity = facing.m_82490_((0.5 + RANDOM.nextDouble() * 0.45) * STREAK_SPEED).m_82549_(sideways.m_82490_(r(0.2)))
                .m_82520_(0, r(0.12) + 0.02, 0);
            PieceFx.at(PieceFx.STREAK, false, 3 + RANDOM.nextInt(2), point.m_82520_(r(0.3), r(0.3), r(0.3))).velocity(velocity)
                .colors(0xC0FFFFFF, 0x00FFFFFF).size(0.42, 0.05).drag(0.8).bloom(0).spawn();
        }
    }

    /** The Air Blade fired from {@code from} to {@code to}: a big crescent racing out, two fainter after it; at its end, a ring and dust. */
    public static void blade(Vec3 from, Vec3 to) {
        Vec3 path = to.m_82546_(from);
        double distance = Math.max(0.5, path.m_82553_());
        Vec3 dir = path.m_82490_(1.0 / distance);
        double[] side = {-dir.f_82481_, 0.0, dir.f_82479_};
        double[] across = AirMesh.tilted(new double[] {dir.f_82479_, dir.f_82480_, dir.f_82481_}, side, 35);
        double life = distance / BLADE_SPEED;
        long now = now();
        Vec3 start = from.m_82549_(dir.m_82490_(1.2));
        CUTS.add(new Cut(start, dir, across, 1.6, BLADE_SPEED, life, now, 1.0));
        CUTS.add(new Cut(start, dir, across, 1.45, BLADE_SPEED, life, now + 1, 0.45));
        CUTS.add(new Cut(start, dir, across, 1.3, BLADE_SPEED, life, now + 2, 0.22));
        PieceFx.later((int) Math.ceil(life), () -> {
            RINGS.add(new Burst(to, dir, 0.3, 2.6, 6.0, now()));
            int n = PieceFx.count(10);
            for (int i = 0; i < n; i++) {
                PieceFx.at(PieceFx.PUFF, false, 14 + RANDOM.nextInt(8), to.m_82520_(r(0.6), -0.4 + r(0.3), r(0.6)))
                    .velocity(new Vec3(r(0.15), 0.04, r(0.15))).colors(0xA0E4DED4, 0x00C8C0B4).size(0.4, 1.2).drag(0.88).spawn();
            }
        });
    }

    public static void accept(S2CWater message) {
        Vec3 at = new Vec3(message.x(), message.y(), message.z());
        Vec3 dir = new Vec3(message.dx(), message.dy(), message.dz());
        if (message.kind() == S2CWater.AIR_SHOT) {
            SHOTS.put(message.id(), new Shot(at, dir));
            Vec3 unit = dir.m_82556_() < 1.0E-9 ? new Vec3(0, 0, 1) : dir.m_82541_();
            RINGS.add(new Burst(at, unit, 0.06, 0.45, 4.0, now()));
        } else if (message.kind() == S2CWater.AIR_HIT) {
            SHOTS.remove(message.id());
            Vec3 unit = dir.m_82556_() < 1.0E-9 ? new Vec3(0, 0, 1) : dir.m_82541_();
            RINGS.add(new Burst(at, unit, 0.1, 0.9, 5.0, now()));
            int n = PieceFx.count(6);
            for (int i = 0; i < n; i++) {
                PieceFx.at(PieceFx.STREAK, false, 4, at).velocity(new Vec3(r(1), r(1), r(1)).m_82541_().m_82490_(0.4))
                    .colors(0xC0FFFFFF, 0x00FFFFFF).size(0.3, 0.04).drag(0.8).bloom(0).spawn();
            }
        }
    }

    @SubscribeEvent
    public static void airTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long now = now();
        CUTS.removeIf(cut -> now - cut.start() > cut.life() + 1);
        RINGS.removeIf(ring -> now - ring.start() > ring.life());
        for (Iterator<Shot> it = SHOTS.values().iterator(); it.hasNext(); ) {
            Shot shot = it.next();
            shot.age++;
            if (shot.age > 12) {
                it.remove();
                continue;
            }
            shot.last = shot.at;
            shot.at = shot.at.m_82549_(shot.velocity);
        }
    }

    @SubscribeEvent
    public static void airStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || CUTS.isEmpty() && RINGS.isEmpty() && SHOTS.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        try {
            draw(event.getPoseStack(), event.getCamera().m_90583_(), level, event.getPartialTick());
        } catch (RuntimeException failure) {
            clear(); // air never stops the game
        }
    }

    static void draw(PoseStack pose, Vec3 cam, ClientLevel level, float partial) {
        double now = level.m_46467_() + partial;
        double day = Math.max(0.0, Math.min(1.0, level.m_104805_(partial)));
        float light = (float) (0.4 + 0.6 * day);
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
        for (Burst ring : RINGS) {
            double t = (now - ring.start()) / ring.life();
            if (t < 0 || t > 1) {
                continue;
            }
            double open = ring.from() + (ring.to() - ring.from()) * (1.0 - (1.0 - t) * (1.0 - t));
            Vec3 at = ring.at().m_82546_(cam);
            WaterMesh.ring(new WaterMesh.Ring(at.f_82479_, at.f_82480_, at.f_82481_, new double[] {ring.dir().f_82479_, ring.dir().f_82480_,
                ring.dir().f_82481_}, open, Math.max(0.02, open * 0.13 * (1.0 - t)), 0.0, now, 0, 1.0 - t), 28, 6, WaterMesh.AIR, sink);
        }
        for (Cut cut : CUTS) {
            double age = now - cut.start();
            if (age < 0 || age > cut.life()) {
                continue;
            }
            double fade = age < cut.life() - 1 ? 1.0 : Math.max(0.0, cut.life() - age);
            Vec3 at = cut.from().m_82549_(cut.dir().m_82490_(age * cut.speed())).m_82546_(cam);
            double grow = Math.min(1.0, 0.6 + age * 0.4);
            AirMesh.crescent(new AirMesh.Crescent(at.f_82479_, at.f_82480_, at.f_82481_, new double[] {cut.dir().f_82479_, cut.dir().f_82480_,
                cut.dir().f_82481_}, cut.across(), cut.radius() * grow, Math.toRadians(70), 0.11 * cut.radius() / 1.6, 0.3 * cut.radius() / 1.6,
                cut.strength() * fade), 24, 8, sink);
        }
        for (Shot shot : SHOTS.values()) {
            Vec3 at = shot.last.m_82549_(shot.at.m_82546_(shot.last).m_82490_(partial)).m_82546_(cam);
            WaterMesh.body(new WaterMesh.Body(at.f_82479_, at.f_82480_, at.f_82481_, 0.09, new double[] {shot.velocity.f_82479_,
                shot.velocity.f_82480_, shot.velocity.f_82481_}, 3.0, 0.0, now, 7, 1.0), 6, 12, WaterMesh.HAKI, sink);
        }
        buffers.m_109912_(type);
    }

    static void clear() {
        CUTS.clear();
        RINGS.clear();
        SHOTS.clear();
    }

    @SubscribeEvent
    public static void airLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }
}
