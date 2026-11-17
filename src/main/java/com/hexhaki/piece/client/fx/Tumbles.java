package com.hexhaki.piece.client.fx;

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

    static double clock(float partial) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? 0.0 : level.m_46467_() + partial;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tumbleBefore(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        Knocked knocked = KNOCKED.get(entity.m_19879_());
        if (knocked == null || TURNED.contains(entity)) {
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
        if (event.phase != TickEvent.Phase.END || KNOCKED.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        double now = clock(0.0F);
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
        HEADS.clear();
        TURNED.clear();
    }
}
