package com.hexhaki.piece.client.gear;

import com.hexhaki.client.devilfruit.RemoteGearStates;
import com.hexhaki.piece.gear.NikaHold;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.GiantHold;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.2.8: a body the Gum-Gum Giant holds, kept in its fist on every client (server/GiantHold tells who holds whom). The original only
 * moved it on the server, so here it trailed the hand by the position updates' three-tick smoothing and the trip from the server,
 * and sank between them. Now, every tick after everything has moved, the body is put in the giant's right fist as drawn this tick and
 * last tick (gear/NikaHold.fist: the model's arm held out, scaled by the giant's own sizes as GearBodyDeformer draws them), both its
 * places so the drawing in between follows the fist exactly, its fall stopped, turned to face the giant. Grabbed, it flies in from where
 * it stood as the arm comes up (NikaHold.reach). One's own body when held is left to the server (the camera is the player's own). In
 * first person the giant's own grab follows the view's turn (the arm drawn there turns with the view, the body never seen).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class NikaHoldClient {
    static final class Hold {
        int held;
        double fromX;
        double fromY;
        double fromZ;
        /** Ticks since the grab, and since the server last named it. */
        int age;
        int heard;
    }

    /** By the giant's id. */
    static final Map<Integer, Hold> HOLDS = new HashMap<>();
    private static final double[] NOW = new double[3];
    private static final double[] BEFORE = new double[3];
    private static double wide;
    private static double tall;

    private NikaHoldClient() {
    }

    /** GiantHold.HOLD and LET_GO (from client/dragon/UoFx). */
    public static void accept(S2CUoFx m) {
        if (m.kind() == GiantHold.LET_GO) {
            HOLDS.remove(m.user());
            return;
        }
        if (m.kind() != GiantHold.HOLD) {
            return;
        }
        Hold h = HOLDS.get(m.user());
        if (h == null || h.held != m.id()) {
            h = new Hold();
            h.held = m.id();
            h.age = m.ticks();
            ClientLevel level = Minecraft.m_91087_().f_91073_;
            Entity body = level == null ? null : level.m_6815_(m.id());
            // flies in from where it is drawn now (the server's place for it may be a little ahead)
            h.fromX = body != null ? body.m_20185_() : m.x();
            h.fromY = body != null ? body.m_20186_() : m.y();
            h.fromZ = body != null ? body.m_20189_() : m.z();
            HOLDS.put(m.user(), h);
        }
        h.heard = 0;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || HOLDS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            HOLDS.clear();
            return;
        }
        if (minecraft.m_91104_()) {
            return;
        }
        Iterator<Map.Entry<Integer, Hold>> it = HOLDS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Hold> e = it.next();
            Hold h = e.getValue();
            h.age++;
            h.heard++;
            Entity giant = level.m_6815_(e.getKey());
            Entity body = level.m_6815_(h.held);
            if (h.heard > 3 * NikaHold.RESEND || !(giant instanceof Player holder) || !(body instanceof LivingEntity held) || !held.m_6084_()) {
                it.remove();
                continue;
            }
            if (held != minecraft.f_91074_) {
                place(minecraft, holder, held, h);
            }
        }
    }

    private static void place(Minecraft minecraft, Player holder, LivingEntity held, Hold h) {
        RemoteGearStates.State state = RemoteGearStates.get(holder.m_19879_());
        boolean ownView = holder == minecraft.f_91074_ && minecraft.f_91066_.m_92176_().m_90612_();
        float yaw = ownView ? holder.m_146908_() : holder.f_20883_;
        float yawBefore = ownView ? holder.f_19859_ : holder.f_20884_;
        size(state, holder, 1.0F);
        NikaHold.fist(holder.m_20185_(), holder.m_20186_(), holder.m_20189_(), yaw, wide, tall, NOW);
        size(state, holder, 0.0F);
        NikaHold.fist(holder.f_19790_, holder.f_19791_, holder.f_19792_, yawBefore, wide, tall, BEFORE);
        double half = held.m_20206_() * 0.5;
        double k = NikaHold.reach(h.age);
        double kBefore = NikaHold.reach(h.age - 1);
        double x = h.fromX + (NOW[0] - h.fromX) * k;
        double y = h.fromY + (NOW[1] - half - h.fromY) * k;
        double z = h.fromZ + (NOW[2] - h.fromZ) * k;
        double xBefore = h.fromX + (BEFORE[0] - h.fromX) * kBefore;
        double yBefore = h.fromY + (BEFORE[1] - half - h.fromY) * kBefore;
        double zBefore = h.fromZ + (BEFORE[2] - h.fromZ) * kBefore;
        held.m_6034_(x, y, z);
        held.f_19790_ = xBefore;
        held.f_19791_ = yBefore;
        held.f_19792_ = zBefore;
        held.f_19854_ = xBefore;
        held.f_19855_ = yBefore;
        held.f_19856_ = zBefore;
        held.m_20334_(0, 0, 0);
        held.f_19789_ = 0;
        // facing the giant
        float face = yaw + 180.0F;
        float faceBefore = yawBefore + 180.0F;
        held.m_146922_(face);
        held.f_19859_ = faceBefore;
        held.f_20883_ = face;
        held.f_20884_ = faceBefore;
        held.f_20885_ = face;
        held.f_20886_ = faceBefore;
    }

    /** The giant's drawn sizes at {@code partial} (0 last tick, 1 this tick), as GearBodyDeformer.applyDeformation scales the body. */
    private static void size(RemoteGearStates.State state, Player holder, float partial) {
        if (state == null) {
            wide = 1;
            tall = 1;
            return;
        }
        float presence = state.presence(partial);
        float squash = Math.max(-1.0F, Math.min(1.0F, state.squash(partial))) * 0.3F;
        float wobble = presence > 0.01F ? presence * 0.03F * (float) Math.sin((state.flow(partial) + holder.m_19879_() * 7.0F) * 0.1F) : 0.0F;
        float scale = Math.max(0.2F, Math.min(6.0F, state.scale(partial)));
        tall = (1.0F - squash + wobble) * scale;
        wide = (1.0F + squash * 0.92F - wobble * 0.55F) * scale;
    }
}
