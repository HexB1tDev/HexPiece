package com.hexhaki.piece.server;

import com.hexhaki.data.HakiData;
import com.hexhaki.data.HakiType;
import com.hexhaki.gameplay.HakiProgression;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.9.1 (the owner: "the higher your mastery on haki, like the faster the Conqueror release will be. Like, I want Conqueror's haki to be
 * fucking crazy. Once you unlock it, Conqueror's haki, your, like, even your Rio, every move, like, should be coating you with
 * lightning. Like, lightning should be shooting out from your body like crazy. And I don't mean too much, but like often lightning is
 * sparking out of you. Whenever you grab a sword, lightning explodes from it ... All of this while avoiding as much lag as possible"):
 * <ul>
 *   <li>The release's charge fills faster the higher the Conqueror's mastery ({@link #rate}: the original's 0.625 a tick, 8 s to full,
 *       times {@link #speed}: 2.5 s at mastery 1000).</li>
 *   <li>Once the Conqueror's Haki has awakened, its lightning sparks off the body: on most moves (every clip the server has a player
 *       play, Ryou's and every fruit's and style's: PiecePatch hooks HakiServerController.animate), at most one burst every
 *       {@link #GAP} ticks, more arcs the higher the mastery; and now and then on its own.</li>
 *   <li>A blade drawn by a Conqueror bursts with it (server/SwordServer).</li>
 * </ul>
 * Each is one small message (S2CUoFx kinds {@link #SPARKS}, {@link #BLADE}) to those near; every client builds the arcs itself
 * (haki/ConquerorSparks) and draws them as the release's 3D lightning, capped by HakiBolts.MOST.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class ConquerorAura {
    /** S2CUoFx kinds: arcs off a body (size: how many, ticks: the seed), a burst off a drawn blade (x, y, z the hand, a its way, size its length). */
    public static final int SPARKS = 80;
    public static final int BLADE = 81;
    /** At most one burst a player this often (ticks), and the share of moves that spark. */
    static final int GAP = 8;
    static final double MOVE_CHANCE = 0.6;
    /** On its own: a look every this many ticks, and the chance it sparks then. */
    static final int IDLE_EVERY = 20;
    static final double IDLE_CHANCE = 0.5;
    private static final Map<UUID, Long> NEXT = new HashMap<>();

    private ConquerorAura() {
    }

    /** How much faster than the original the charge fills at {@code mastery} (0 to 1000): 1 at none, 3.2 at the most. */
    public static double speed(int mastery) {
        double t = Math.max(0.0, Math.min(1.0, mastery / 1000.0));
        return 1.0 + 2.2 * Math.pow(t, 0.85);
    }

    /** The release's charge a tick (the original's 0.625) for {@code player} (PiecePatch: HakiServerController.tickConquerorCharge). */
    public static double rate(ServerPlayer player) {
        try {
            return 0.625 * speed(HakiData.mastery(player, HakiType.CONQUEROR));
        } catch (RuntimeException failure) {
            return 0.625;
        }
    }

    /**
     * Whether {@code player}'s Conqueror's lightning sparks off them now. 2.9.4 (the owner: "I told you to keep lightning around the
     * player whenever Rio was activated ... hockey's not activated yet, fucking lightning is shooting for me"): only while their Ryou is
     * on (Armament with Ryou: the {@code acocOn} flag), the Conqueror's Haki awakened.
     */
    static boolean awakened(ServerPlayer player) {
        try {
            return HakiData.flag(player, "armamentOn") && HakiData.flag(player, "acocOn") && HakiProgression.conquerorAwakened(player);
        } catch (RuntimeException failure) {
            return false;
        }
    }

    /** How many arcs a burst off the body throws at the player's Conqueror mastery: 2 to 6. */
    static int arcs(ServerPlayer player) {
        return 2 + Math.min(4, HakiData.mastery(player, HakiType.CONQUEROR) / 220);
    }

    /** PiecePatch: {@code player} was set playing the clip {@code name} (HakiServerController.animate): most moves spark. */
    public static void moved(ServerPlayer player, String name) {
        try {
            if (name == null || name.startsWith("__") || !awakened(player)) {
                return;
            }
            long now = player.m_9236_().m_46467_();
            Long next = NEXT.get(player.m_20148_());
            if (next != null && now < next || player.m_217043_().m_188500_() > MOVE_CHANCE) {
                return;
            }
            NEXT.put(player.m_20148_(), now + GAP);
            send(player, SPARKS, Vec3.f_82478_, Vec3.f_82478_, arcs(player));
        } catch (RuntimeException failure) {
            // a spark never stops a move
        }
    }

    /** server/SwordServer: {@code player} drew a blade: if they have the Conqueror's Haki, lightning bursts off it. */
    public static void drew(ServerPlayer player) {
        try {
            if (!awakened(player)) {
                return;
            }
            Vec3 look = player.m_20154_();
            Vec3 side = look.m_82537_(new Vec3(0, 1, 0));
            side = side.m_82556_() < 1.0E-6 ? new Vec3(1, 0, 0) : side.m_82541_();
            Vec3 hand = player.m_146892_().m_82549_(look.m_82490_(0.45)).m_82549_(side.m_82490_(0.35)).m_82520_(0, -0.45, 0);
            Vec3 way = look.m_82520_(0, 0.35, 0).m_82541_();
            send(player, BLADE, hand, way, 2.2);
            NEXT.put(player.m_20148_(), player.m_9236_().m_46467_() + GAP);
        } catch (RuntimeException failure) {
            // a spark never stops a draw
        }
    }

    static void send(ServerPlayer player, int kind, Vec3 at, Vec3 way, double size) {
        int seed = (int) (player.m_217043_().m_188500_() * Integer.MAX_VALUE);
        PieceNetwork.around(player, new S2CUoFx(kind, 0, player.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_, (float) way.f_82479_,
            (float) way.f_82480_, (float) way.f_82481_, 0, 0, 0, (float) size, seed));
    }

    /** Now and then, on its own: a Conqueror's lightning sparks off them even standing still. */
    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
            || (player.f_19797_ + (player.m_19879_() & 31)) % IDLE_EVERY != 0) {
            return;
        }
        if (player.m_217043_().m_188500_() < IDLE_CHANCE && awakened(player)) {
            send(player, SPARKS, Vec3.f_82478_, Vec3.f_82478_, 2);
        }
    }

    static void forget(UUID id) {
        NEXT.remove(id);
    }
}
