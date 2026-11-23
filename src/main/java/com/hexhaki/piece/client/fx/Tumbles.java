package com.hexhaki.piece.client.fx;

import com.hexhaki.piece.haki.StunFall;
import com.hexhaki.piece.network.S2CFall;
import com.hexhaki.piece.style.Tumble;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

/**
 * 2.3.9: bodies knocked off their feet, drawn as rag dolls ({@link Tumble}; HexGhoul 6.2.0's {@code PaleReactions}, the owner's): the
 * head snapped round the way it was driven (its own head turn, so every model's head goes), the whole body twisted about its own length
 * and tipped over the way it flies, about its middle in the air and, lying, so that it lies on the ground; down from the tick it
 * touches the ground (as the server finds it) and done once it is up again. Water still runs off it while it flies.
 * <p>2.4.4: and every body a Haki stun knocks down ({@link #fall}, {@link StunFall}; any model, players, mobs, modded ones: the whole of
 * it is turned): tipped over from its feet the way it falls, slid back a little, lifted by half its width as it lies so it lies on the
 * ground and not in it, its head lolled; up again when the stun is over.
 * <p>2.5.1: and a player's own first-person view goes over with them ({@link #fallView}).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class Tumbles {
    /** A rag doll's head is never pitched further than this (degrees) up or down. */
    static final float HEAD_PITCH_MAX = 70.0F;

    /** A knocked body: which way it flies (level, unit), when it was struck and when it came down (game ticks; NaN: in the air). */
    static final class Knocked {
        final double x;
        final double z;
        final double at;
        double landed = Double.NaN;
        /** Which way round it goes (+1: to its right), fixed on the first frame it is drawn. */
        float side;

        Knocked(double x, double z, double at) {
            this.x = x;
            this.z = z;
            this.at = at;
        }
    }

    private static final Map<Integer, Knocked> KNOCKED = new HashMap<>();

    /** 2.4.4: a body a Haki stun knocked down: the way it falls (unit, on the ground), when (game tick), how long it is down, its seed. */
    record Fallen(double x, double z, double at, int down, long seed) {
    }

    private static final Map<Integer, Fallen> FALLEN = new HashMap<>();
    /** 2.5.1: the way this client's own player faced (yaw, degrees) when they were knocked down. */
    private static float ownYaw;
    /** The heads turned this frame (each body's own head turn and pitch, put back after it is drawn), the bodies turned. */
    private static final Map<LivingEntity, float[]> HEADS = new IdentityHashMap<>();
    private static final Set<LivingEntity> TURNED = Collections.newSetFromMap(new IdentityHashMap<>());

    private Tumbles() {
    }

    /** The body {@code id} was knocked off its feet, launched along {@code launch}. */
    public static void knocked(int id, Vec3 launch) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        double length = Math.sqrt(launch.f_82479_ * launch.f_82479_ + launch.f_82481_ * launch.f_82481_);
        if (level == null || length < 1.0E-4) {
            return;
        }
        KNOCKED.put(id, new Knocked(launch.f_82479_ / length, launch.f_82481_ / length, level.m_46467_()));
    }

    /** 2.4.4: a Haki stun knocked a body down (or keeps one down longer: its fall is not started over). */
    public static void fall(S2CFall message) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        double length = Math.hypot(message.dx(), message.dz());
        if (level == null || length < 1.0E-4) {
            return;
        }
        Fallen was = FALLEN.get(message.id());
        double now = level.m_46467_();
        if (was != null && now - was.at() < was.down() + StunFall.GETUP_TICKS) {
            FALLEN.put(message.id(), new Fallen(was.x(), was.z(), was.at(), Math.max(was.down(), message.down()), was.seed()));
            return;
        }
        // a fall puts any tumble still going to rest (the stun's fall is what shows now)
        KNOCKED.remove(message.id());
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ != null && minecraft.f_91074_.m_19879_() == message.id()) {
            ownYaw = minecraft.f_91074_.m_146908_();
        }
        FALLEN.put(message.id(), new Fallen(message.dx() / length, message.dz() / length, now, message.down(), message.seed()));
    }

    /**
     * 3.3.6: how far over the body {@code id} is from a Haki stun's fall this frame (radians from upright, as {@link #fallBefore} turns
     * it; 0 when it is not down), the way it fell into {@code way} (x, z, unit), how far it slid that way and how far it is lifted (blocks,
     * way[2] and way[3]): client/horo/HoroFx hangs despair's cloud over a body lying down.
     */
    public static double lying(int id, float partial, double[] way) {
        Fallen fallen = FALLEN.get(id);
        if (fallen == null) {
            return 0.0;
        }
        StunFall.Frame frame = StunFall.at(clock(partial) - fallen.at(), fallen.down(), fallen.seed());
        way[0] = fallen.x();
        way[1] = fallen.z();
        way[2] = frame.slide();
        way[3] = frame.flat();
        return frame.angle();
    }

    static double clock(float partial) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? 0.0 : level.m_46467_() + partial;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tumbleBefore(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (TURNED.contains(entity)) {
            return;
        }
        Fallen fallen = FALLEN.get(entity.m_19879_());
        if (fallen != null) {
            fallBefore(event, entity, fallen);
            return;
        }
        Knocked knocked = KNOCKED.get(entity.m_19879_());
        if (knocked == null) {
            return;
        }
        double t = clock(event.getPartialTick()) - knocked.at;
        double landed = Double.isNaN(knocked.landed) ? Double.NaN : knocked.landed - knocked.at;
        Tumble.Frame frame = Tumble.at(t, landed);
        float bodyYaw = entity.f_20884_ + wrap(entity.f_20883_ - entity.f_20884_) * event.getPartialTick();
        if (knocked.side == 0.0F) {
            float flying = (float) Math.toDegrees(Math.atan2(-knocked.x, knocked.z));
            knocked.side = wrap(flying - bodyYaw) >= 0.0F ? 1.0F : -1.0F;
        }
        float side = knocked.side;
        if (!HEADS.containsKey(entity)) {
            HEADS.put(entity, new float[] {entity.f_20885_, entity.f_20886_, entity.m_146909_(), entity.f_19860_});
        }
        // a rag doll's head is its body's: square on it, turned and thrown only by the blow
        float yaw = bodyYaw + side * (float) Math.toDegrees(frame.head());
        float pitch = Math.max(-HEAD_PITCH_MAX, Math.min(HEAD_PITCH_MAX, -(float) Math.toDegrees(frame.pitch())));
        entity.f_20885_ = yaw;
        entity.f_20886_ = yaw;
        entity.f_19860_ = pitch;
        entity.m_146926_(pitch);
        float pivot = (float) (entity.m_20206_() * 0.5 * (1.0 - frame.flat()) + entity.m_20205_() * 0.5 * frame.flat());
        PoseStack stack = event.getPoseStack();
        stack.m_85836_();
        stack.m_252880_(0.0F, pivot, 0.0F);
        stack.m_252781_(new Quaternionf().rotateAxis((float) frame.tumble(), (float) knocked.z, 0.0F, (float) -knocked.x));
        // (the model's own turn to its facing comes after this: a turn here turns it the opposite way to its yaw)
        stack.m_252781_(new Quaternionf().rotateAxis((float) (-side * frame.twist()), 0.0F, 1.0F, 0.0F));
        stack.m_252880_(0.0F, -pivot, 0.0F);
        TURNED.add(entity);
    }

    /**
     * 2.4.4: a body down from a Haki stun, turned from its feet: slid back the way it falls, lifted by half its width the flatter it lies,
     * tipped over the way it falls; its head square on its body, lolled to one side.
     */
    static void fallBefore(RenderLivingEvent.Pre<?, ?> event, LivingEntity entity, Fallen fallen) {
        double t = clock(event.getPartialTick()) - fallen.at();
        StunFall.Frame frame = StunFall.at(t, fallen.down(), fallen.seed());
        if (frame.angle() <= 1.0E-4 && t > StunFall.FALL_TICKS) {
            return;
        }
        float bodyYaw = entity.f_20884_ + wrap(entity.f_20883_ - entity.f_20884_) * event.getPartialTick();
        if (!HEADS.containsKey(entity)) {
            HEADS.put(entity, new float[] {entity.f_20885_, entity.f_20886_, entity.m_146909_(), entity.f_19860_});
        }
        float yaw = bodyYaw + (float) Math.toDegrees(frame.head());
        entity.f_20885_ = yaw;
        entity.f_20886_ = yaw;
        entity.f_19860_ = 0.0F;
        entity.m_146926_(0.0F);
        float lift = (float) (entity.m_20205_() * 0.5 * frame.flat());
        PoseStack stack = event.getPoseStack();
        stack.m_85836_();
        stack.m_252880_((float) (fallen.x() * frame.slide()), lift, (float) (fallen.z() * frame.slide()));
        stack.m_252781_(new Quaternionf().rotateAxis((float) frame.angle(), (float) fallen.z(), 0.0F, (float) -fallen.x()));
        TURNED.add(entity);
    }

    /**
     * 2.5.1: this client's own player knocked down, seen in first person: the view goes over with them ({@link StunFall#view}: rolled
     * the way they fall, tipped to the sky or the ground), lies there, and comes back up as they get up. In third person their body is
     * drawn falling as everyone else's is ({@link #fallBefore}).
     */
    @SubscribeEvent
    public static void fallView(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ == null || minecraft.f_91066_ == null || !minecraft.f_91066_.m_92176_().m_90612_()) {
            return;
        }
        Fallen fallen = FALLEN.get(minecraft.f_91074_.m_19879_());
        if (fallen == null) {
            return;
        }
        StunFall.Frame frame = StunFall.at(clock((float) event.getPartialTick()) - fallen.at(), fallen.down(), fallen.seed());
        if (frame.angle() <= 1.0E-4) {
            return;
        }
        double[] view = StunFall.view(frame.angle(), fallen.x(), fallen.z(), ownYaw);
        event.setRoll(event.getRoll() + (float) view[0]);
        event.setPitch(Math.max(-90.0F, Math.min(90.0F, event.getPitch() + (float) view[1])));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tumbleAfter(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        restore(entity, HEADS.remove(entity));
        if (TURNED.remove(entity)) {
            event.getPoseStack().m_85849_();
        }
    }

    /** A body's own head turn and pitch put back as they were before it was drawn turned. */
    static void restore(LivingEntity entity, float[] head) {
        if (head != null) {
            entity.f_20885_ = head[0];
            entity.f_20886_ = head[1];
            entity.m_146926_(head[2]);
            entity.f_19860_ = head[3];
        }
    }

    static float wrap(float degrees) {
        return degrees - 360.0F * (float) Math.floor((degrees + 180.0F) / 360.0F);
    }

    @SubscribeEvent
    public static void tumbleFrame(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            TURNED.clear();
            // (a body whose drawing was cut short last frame gets its own head back)
            HEADS.forEach(Tumbles::restore);
            HEADS.clear();
        }
    }

    @SubscribeEvent
    public static void tumbleTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || KNOCKED.isEmpty() && FALLEN.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        double now = clock(0.0F);
        // 2.4.4: the bodies down from a Haki stun, gone once they are up (or gone, or the clock went back)
        FALLEN.entrySet().removeIf(entry -> {
            Fallen fallen = entry.getValue();
            Entity body = level == null ? null : level.m_6815_(entry.getKey());
            double since = now - fallen.at();
            return !(body instanceof LivingEntity living) || !living.m_6084_() || since < -40.0 || StunFall.over(since, fallen.down());
        });
        KNOCKED.entrySet().removeIf(entry -> {
            Knocked knocked = entry.getValue();
            Entity body = level == null ? null : level.m_6815_(entry.getKey());
            if (!(body instanceof LivingEntity living) || !living.m_6084_() || now < knocked.at - 40.0) {
                return true;
            }
            double since = now - knocked.at;
            if (Double.isNaN(knocked.landed) && ((since >= 3.0 && living.m_20096_()) || since >= Tumble.AIRBORNE_MAX)) {
                knocked.landed = now;
                WaterFx.spray(living.m_20182_().m_82520_(0.0, 0.2, 0.0), Vec3.f_82478_, 10, 0.4);
            }
            if (Double.isNaN(knocked.landed) && since < 14.0 && since > 1.0) {
                // the water it was struck with running off it as it flies
                WaterFx.spray(living.m_20182_().m_82520_(0.0, living.m_20206_() * 0.5, 0.0), new Vec3(-knocked.x * 0.05, -0.02, -knocked.z * 0.05),
                    2, living.m_20205_() * 0.5);
            }
            return Tumble.over(since, Double.isNaN(knocked.landed) ? Double.NaN : knocked.landed - knocked.at);
        });
    }

    @SubscribeEvent
    public static void tumbleLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        KNOCKED.clear();
        FALLEN.clear();
        HEADS.clear();
        TURNED.clear();
    }
}
