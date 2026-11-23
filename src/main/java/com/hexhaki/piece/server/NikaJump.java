package com.hexhaki.piece.server;

import com.hexhaki.devilfruit.data.GearData;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.gear.RubberJump;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.3.2 (the owner: "shift + space ability is removed, default jumping is much higher and always does the jump animation we're doing,
 * it always turns the ground into rubber for the player using the fruit, basically world of rubber on every jump"; asked, "Gear 5
 * only" and "About 5 blocks"): Gear 5's jump on the server. The player's own client lifts the jump (client/gear/NikaJumpClient: the
 * body is the client's to move) and says so here ({@link #jumped}); then:
 * <ul>
 *   <li>everyone near sees the ground under the take-off spring up and ripple like rubber ({@link #HOP}: client/gear/RubberWorldDraw),
 *       and again round the landing ({@link #LAND});</li>
 *   <li>the body plays gear5_jump (Gear 5's laughing skip, held through the air) unless a move of the fruit is busy, then at the landing
 *       gear5_land and Gear 5's stance after it (or straight back to its run when it bounds on, as the original's walk does);</li>
 *   <li>no fall damage from the jump.</li>
 * </ul>
 * The original's walk switches between its run and its stance as the body leaves and touches the ground (GearServerController
 * .tickBouncyWalk); patch/PiecePatch has those two clips ask {@link #walkClip} first, which holds them back while the jump's clip plays
 * (the landing puts the right one back). Any other clip (a move thrown in the air) takes over: the head of HakiServerController.animate
 * tells {@link #played}, and the landing then leaves it alone.
 *
 * <p>3.3.8 (the owner: "falling from Bajarang gun doesnt make the body ripple when the player falls (every fall should make the floor
 * ripple)"): every landing in Gear 5 from {@link RubberJump#FALL_MIN} blocks or more over the feet ripples the ground round them
 * ({@link #LAND}), however the body got up there: the Bajrang Gun's fall, the original's bounding run, a ledge, a throw ({@link #FALLS}:
 * each airborne Gear 5 player's highest point). A jump's own landing still ripples once, from its {@link Jump}.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class NikaJump {
    /** S2CUoFx kinds (the Nika fruit's 160 to 179): the ground springing and rippling round a take-off and round a landing (user, x y z
     * the feet). */
    public static final int HOP = 162;
    public static final int LAND = 163;
    static final String JUMP_CLIP = "gear5_jump";
    static final String LAND_CLIP = "gear5_land";
    /** How long the landing clip plays before the stance comes back. */
    static final int LAND_TICKS = 8;
    /** At most one jump in this many ticks; a jump not landed in {@link #LONGEST} ticks is forgotten. */
    static final int GAP = 5;
    static final int LONGEST = 200;
    /** The giant's size (the original's GearServerController.bodyScale100 is 400 with "gigante"). */
    static final float GIANT = 4.0F;

    static final class Jump {
        long at;
        /** Whether the jump's (or the landing's) clip is still the one playing. */
        boolean clip;
        boolean landed;
        long until;
    }

    static final Map<UUID, Jump> JUMPS = new HashMap<>();
    /** 3.3.8: each Gear 5 player in the air, the highest their feet have been since they left the ground. */
    static final Map<UUID, Double> FALLS = new HashMap<>();

    private NikaJump() {
    }

    /** C2SNikaJump: the player's client has just lifted a Gear 5 jump. */
    public static void jumped(ServerPlayer player) {
        if (!player.m_6084_() || player.m_5833_() || !GearData.flag(player, "gear5") || GearData.integer(player, "transformTick") > 0
            || player.m_150110_().f_35935_ || player.m_20159_() || NikaWhip.busy(player)) {
            return;
        }
        long now = player.m_9236_().m_46467_();
        Jump last = JUMPS.get(player.m_20148_());
        if (last != null && !last.landed && now - last.at < GAP) {
            return;
        }
        Jump jump = new Jump();
        jump.at = now;
        ripple(player, HOP, RubberJump.HOP_RADIUS);
        if (!busy(player)) {
            HakiServerController.animate(player, JUMP_CLIP, 1);
            jump.clip = true;
        }
        JUMPS.put(player.m_20148_(), jump);
    }

    /** A move of the fruit that has its own clip going (the jump's would cut it). */
    static boolean busy(ServerPlayer player) {
        return GearData.flag(player, "gatling") || GearData.flag(player, "rocketing") || GearData.flag(player, "coilHeld")
            || GearData.flag(player, "lanceHolding") || GearData.flag(player, "giantHeld")
            || GearData.flag(player, "rubberWorldHeld") || GearData.integer(player, "bajrangPhase") != 0
            || GearData.integer(player, "grabHeld") != 0;
    }

    /** 3.4.9: bx the body's size (Giant's 4: its ripples are that big; the original's GearServerController.bodyScale100). */
    static void ripple(ServerPlayer player, int kind, double radius) {
        float scale = GearData.flag(player, "gigante") ? GIANT : 1.0F;
        PieceNetwork.around(player, new S2CUoFx(kind, 0, player.m_19879_(), player.m_20185_(), player.m_20186_(), player.m_20189_(), 0, 0, 0,
            scale, 0, 0, (float) (radius * scale), 0));
    }

    /** The clip Gear 5 stands or runs in (the original's own GearServerController.idleTrack). */
    static String idle(ServerPlayer player) {
        if (!GearData.flag(player, "gear5")) {
            return "__clear__";
        }
        return GearData.flag(player, "bounding") ? "gear5_run" : "gear5_stance";
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        boolean ground = player.m_20096_();
        if (ground && FALLS.isEmpty() && JUMPS.isEmpty()) {
            return;
        }
        falls(player, ground);
        if (JUMPS.isEmpty()) {
            return;
        }
        Jump jump = JUMPS.get(player.m_20148_());
        if (jump == null) {
            return;
        }
        long now = player.m_9236_().m_46467_();
        if (!jump.landed) {
            if (now - jump.at > LONGEST || !player.m_6084_()) {
                JUMPS.remove(player.m_20148_());
                return;
            }
            if (now - jump.at < 3 || !player.m_20096_()) {
                return;
            }
            jump.landed = true;
            jump.until = now + LAND_TICKS;
            ripple(player, LAND, RubberJump.LAND_RADIUS);
            if (!jump.clip || !GearData.flag(player, "gear5")) {
                JUMPS.remove(player.m_20148_());
            } else if (GearData.flag(player, "bounding")) {
                // still on the move: the original's walk bounds straight on, in its run
                JUMPS.remove(player.m_20148_());
                HakiServerController.animate(player, "gear5_run", 3);
            } else {
                HakiServerController.animate(player, LAND_CLIP, 1);
            }
            return;
        }
        if (now >= jump.until) {
            JUMPS.remove(player.m_20148_());
            if (jump.clip) {
                HakiServerController.animate(player, idle(player), 4);
            }
        }
    }

    /**
     * 3.3.8: a Gear 5 player in the air is followed to their highest point; landing {@link RubberJump#FALL_MIN} or more under it, the
     * ground ripples round the feet (unless a jump of theirs is landing now: it ripples on its own). Flying, riding or water forget it.
     */
    static void falls(ServerPlayer player, boolean ground) {
        UUID id = player.m_20148_();
        double y = player.m_20186_();
        if (!ground) {
            Double peak = FALLS.get(id);
            if (player.m_150110_().f_35935_ || player.m_20159_() || player.m_20069_() || !player.m_6084_()) {
                if (peak != null) {
                    FALLS.remove(id);
                }
            } else if (peak != null) {
                if (y > peak) {
                    FALLS.put(id, y);
                }
            } else if (!player.m_5833_() && GearData.flag(player, "gear5")) {
                FALLS.put(id, y);
            }
            return;
        }
        Double peak = FALLS.remove(id);
        if (peak == null || peak - y < RubberJump.FALL_MIN || !GearData.flag(player, "gear5")) {
            return;
        }
        Jump jump = JUMPS.get(id);
        if (jump != null && !jump.landed) {
            return;
        }
        ripple(player, LAND, RubberJump.LAND_RADIUS);
    }

    /** No fall damage from a Gear 5 jump (the body is rubber; the landing ripples instead). */
    @SubscribeEvent
    public static void fall(LivingFallEvent event) {
        if (!JUMPS.isEmpty() && event.getEntity() instanceof ServerPlayer player && JUMPS.containsKey(player.m_20148_())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        JUMPS.remove(event.getEntity().m_20148_());
        FALLS.remove(event.getEntity().m_20148_());
    }

    /** 3.3.8: a death or another dimension forgets the height (no ripple where they come back). */
    @SubscribeEvent
    public static void respawned(PlayerEvent.PlayerRespawnEvent event) {
        FALLS.remove(event.getEntity().m_20148_());
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        FALLS.remove(event.getEntity().m_20148_());
    }

    /** patch/PiecePatch, at the head of HakiServerController.animate: {@code player} was set playing {@code name}. */
    public static void played(ServerPlayer player, String name) {
        if (JUMPS.isEmpty() || JUMP_CLIP.equals(name) || LAND_CLIP.equals(name)) {
            return;
        }
        Jump jump = JUMPS.get(player.m_20148_());
        if (jump != null) {
            jump.clip = false;
        }
    }

    /**
     * patch/PiecePatch: the original's walk (GearServerController.tickBouncyWalk) plays its run, its stance or nothing as the body leaves
     * and touches the ground: held back while the jump's clip plays (the landing puts back the one the walk is in by then); anything else
     * as it was.
     */
    public static void walkClip(ServerPlayer player, String name, int fade) {
        if (NikaWhip.holds(player, name)) {
            return; // 3.4.4: Goof Ball, its daze or Dawn Whip is playing its own clip
        }
        if (!JUMPS.isEmpty() && ("gear5_run".equals(name) || "gear5_stance".equals(name) || "__clear__".equals(name))) {
            Jump jump = JUMPS.get(player.m_20148_());
            if (jump != null && jump.clip) {
                return;
            }
        }
        HakiServerController.animate(player, name, fade);
    }
}
