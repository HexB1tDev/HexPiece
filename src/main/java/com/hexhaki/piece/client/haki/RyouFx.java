package com.hexhaki.piece.client.haki;

import com.hexhaki.client.RemoteHakiStates;
import com.hexhaki.client.vfx.HakiVfx;
import com.hexhaki.piece.client.fx.PieceFx;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.3: what Ryou looks like around a player besides the hands ({@link RyouHands}), always in that player's own colour, never fire (the
 * owner: "Remove the fire particles from every Ryou activation, idle and movement effect"):
 * <ul>
 *   <li>coming on: the rim flares and settles ({@link #flare}), a burst of sparks off both fists and a ring of light round each;</li>
 *   <li>held: now and then a glint off a knuckle; moving fast, a short streak of light trailing each fist.</li>
 * </ul>
 * The original's ambient Armament effects (black arcs, its embers) are kept for Armament without Ryou: {@link #ambient} and
 * {@link #groundStorm} are where its client tick asked for them (through {@code patch/PiecePatch.java}).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class RyouFx {
    /** Ticks the rim's flare lasts when Ryou comes on. */
    static final float FLARE_TICKS = 14.0F;
    static final Random RANDOM = new Random();
    private static final Map<Integer, Long> LIT_AT = new HashMap<>();
    private static final Set<Integer> UP = new HashSet<>();

    private RyouFx() {
    }

    /** The original's ambient Armament effect, or Ryou's for a player with Ryou up. */
    public static void ambient(Entity entity, float mastery, boolean acoc, long seed, boolean slim) {
        if (!acoc) {
            HakiVfx.armamentAmbient(entity, mastery, acoc, seed, slim);
        } else {
            glints(entity);
        }
    }

    /** The original's ground storm under Armament; nothing for Ryou (its embers were fire). */
    public static void groundStorm(Entity entity, float mastery, boolean acoc, long seed) {
        if (!acoc) {
            HakiVfx.armamentGroundStorm(entity, mastery, acoc, seed);
        }
    }

    /** Extra light on the rim just after Ryou came on: 1 at once, gone after {@link #FLARE_TICKS}. */
    static float flare(int entity) {
        Long at = LIT_AT.get(entity);
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (at == null || level == null) {
            return 0.0F;
        }
        float t = (level.m_46467_() - at) / FLARE_TICKS;
        return t < 0.0F || t >= 1.0F ? 0.0F : (1.0F - t) * (1.0F - t);
    }

    /** Where a player's fist is (world), near enough for light around it. */
    static Vec3 fist(Entity entity, boolean right) {
        float yaw = (entity instanceof LivingEntity living ? living.f_20883_ : entity.m_146908_()) * (float) (Math.PI / 180.0);
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        Vec3 side = new Vec3(-Math.cos(yaw), 0.0, -Math.sin(yaw));
        return entity.m_20182_().m_82520_(0.0, 0.72, 0.0).m_82549_(side.m_82490_(right ? 0.38 : -0.38)).m_82549_(forward.m_82490_(0.08));
    }

    static void glints(Entity entity) {
        if (!PieceFx.near(entity.m_20182_(), 48.0)) {
            return;
        }
        int color = RyouColors.aura(entity.m_19879_());
        int faded = color & 0x00FFFFFF;
        for (boolean right : new boolean[] {true, false}) {
            if (RANDOM.nextFloat() < 0.55F) {
                Vec3 at = fist(entity, right).m_82520_((RANDOM.nextDouble() - 0.5) * 0.18, (RANDOM.nextDouble() - 0.5) * 0.2,
                    (RANDOM.nextDouble() - 0.5) * 0.18);
                PieceFx.at(PieceFx.SPARK, true, 7, at).velocity(new Vec3(0, 0.01, 0)).colors(color, faded).size(0.16, 0.03).bloom(0).spawn();
            }
        }
    }

    static void comeOn(AbstractClientPlayer player, long now) {
        LIT_AT.put(player.m_19879_(), now);
        if (!PieceFx.near(player.m_20182_(), 64.0)) {
            return;
        }
        int color = RyouColors.aura(player.m_19879_());
        int faded = color & 0x00FFFFFF;
        for (boolean right : new boolean[] {true, false}) {
            Vec3 at = fist(player, right);
            int n = PieceFx.count(10);
            for (int i = 0; i < n; i++) {
                Vec3 out = new Vec3(RANDOM.nextDouble() - 0.5, RANDOM.nextDouble() * 0.6 - 0.1, RANDOM.nextDouble() - 0.5).m_82541_();
                PieceFx.at(PieceFx.SPARK, true, 6 + RANDOM.nextInt(4), at).velocity(out.m_82490_(0.12 + RANDOM.nextDouble() * 0.1))
                    .colors(color, faded).size(0.2, 0.03).drag(0.84).bloom(0).spawn();
            }
            PieceFx.at(PieceFx.RING, true, 7, at).colors(color, faded).size(0.15, 1.1).bloom(0).spawn();
        }
    }

    static void streaks(AbstractClientPlayer player) {
        Vec3 moved = player.m_20182_().m_82546_(new Vec3(player.f_19854_, player.f_19855_, player.f_19856_));
        if (moved.m_82556_() < 0.06 || !PieceFx.near(player.m_20182_(), 40.0)) {
            return;
        }
        int color = RyouColors.aura(player.m_19879_());
        int faded = color & 0x00FFFFFF;
        for (boolean right : new boolean[] {true, false}) {
            PieceFx.at(PieceFx.STREAK, true, 5, fist(player, right)).velocity(moved.m_82490_(-0.35)).colors((color & 0x00FFFFFF) | 0xA0000000,
                faded).size(0.22, 0.05).bloom(0).spawn();
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (event.phase != TickEvent.Phase.END || level == null) {
            return;
        }
        long now = level.m_46467_();
        Set<Integer> seen = new HashSet<>();
        for (AbstractClientPlayer player : level.m_6907_()) {
            RemoteHakiStates.State state = RemoteHakiStates.get(player.m_19879_());
            boolean up = state != null && state.armament() && state.acoc() && player.m_6084_();
            int id = player.m_19879_();
            if (up) {
                seen.add(id);
                if (UP.add(id)) {
                    comeOn(player, now);
                } else if ((now + id) % 3 == 0) {
                    streaks(player);
                }
            }
        }
        UP.retainAll(seen);
        LIT_AT.keySet().removeIf(id -> !seen.contains(id) && now - LIT_AT.get(id) > FLARE_TICKS);
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        LIT_AT.clear();
        UP.clear();
    }
}
