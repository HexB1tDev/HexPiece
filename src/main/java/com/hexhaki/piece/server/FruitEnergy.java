package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitMove;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CEnergy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.2.0 (the owner: "we're going to need to have an energy bar for the fruits, appearing over the food bar with a custom good-looking
 * bar"; asked what it does: "Moves spend it": "Every fruit move spends energy (big moves and ultimates more), it refills over time; a move
 * you can't afford doesn't go off"): each player's fruit energy, 0 to {@link #MOST}. Every fruit kit of HexPiece's (the Light, Gura, Magma,
 * Ice fruits and the dragon) asks {@link #afford} where its moves start (their {@code ready}) and {@link #spend}s where they begin their
 * cooldown (their {@code cool}): {@link #cost} per move, a right-click move's quick tap about a third of it. It refills
 * {@link #REFILL} a tick, after {@link #PAUSE} ticks of rest from the last move. Kept in memory (full again after a relog); its own
 * client's bar is told (S2CEnergy) whenever its whole number changes.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class FruitEnergy {
    public static final double MOST = 100.0;
    static final double REFILL = 0.16;
    static final int PAUSE = 20;
    /** A quick tap's share of its move's cost (a right-click move whose cooldown is this short or shorter is a tap). */
    static final double TAP_SHARE = 0.35;
    static final int TAP_COOLDOWN = 20;

    /** The energy each move costs (by its cooldown key); one not listed costs {@link #USUAL}. */
    static final Map<String, Integer> COSTS = new HashMap<>();
    static final int USUAL = 20;

    static {
        String[][] costs = {
            {"gura_kaishin", "25"}, {"gura_shima", "30"}, {"gura_kabutowari", "25"}, {"gura_tsunami", "35"}, {"gura_gekishin", "30"},
            {"gura_split", "30"}, {"gura_ultimate", "70"},
            {"hie_partisan", "25"}, {"hie_ice_time", "20"}, {"hie_ice_ball", "30"}, {"hie_pheasant", "30"}, {"hie_ice_age", "40"},
            {"hie_ice_road", "25"}, {"hie_saber", "20"},
            {"magu_dai_funka", "25"}, {"magu_meigo", "20"}, {"magu_ryusei", "35"}, {"magu_inugami", "30"}, {"magu_bakuretsu", "35"},
            {"magu_flow", "20"},
            {"noro_photon", "20"}, {"noro_spread", "25"}, {"noro_mirror", "25"}, {"noro_timesup", "20"}, {"noro_field", "40"},
            {"supa_slash", "20"}, {"supa_atomic", "30"}, {"supa_spiral", "30"}, {"supa_spurt", "30"}, {"supa_spider", "35"},
            {"supa_daisy", "30"}, {"supa_break", "40"},
            {"horo_negative", "25"}, {"horo_mini", "30"}, {"horo_toku", "40"}, {"horo_astral", "30"}, {"horo_network", "35"},
            {"ito_bullet", "15"}, {"ito_goshiki", "30"}, {"ito_overheat", "30"}, {"ito_parasite", "35"}, {"ito_web", "25"},
            {"ito_knight", "40"}, {"ito_cage", "60"}, {"ito_awaken", "50"},
            {"mochi_tsuki", "25"}, {"mochi_yanagi", "20"}, {"mochi_kaku", "25"}, {"mochi_yaki", "30"}, {"mochi_zangiri", "35"},
            {"mochi_anemone", "35"}, {"mochi_sticky", "15"}, {"mochi_awaken", "50"}, {"mochi_dodge", "20"}, {"mochi_nagare", "30"},
            {"mochi_kagami", "35"}, {"mochi_amadare", "35"}, {"mochi_donuts", "40"}, {"mochi_ginkaku", "45"},
            {"pika_laser", "20"}, {"pika_yata", "15"}, {"pika_yasakani", "30"}, {"pika_lunge", "20"}, {"pika_light_kick", "30"},
            {"pika_clones", "35"},
            {"uo_air", "15"}, {"uo_breath", "30"}, {"uo_wind", "25"}, {"uo_embers", "20"}, {"uo_flame", "15"}, {"uo_blast", "40"},
            {"uo_bite", "20"},
        };
        for (String[] c : costs) {
            COSTS.put(c[0], Integer.parseInt(c[1]));
        }
    }

    /** A player's energy and when it last spent. */
    static final class Pool {
        double energy = MOST;
        long spentAt = Long.MIN_VALUE;
        int sent = -1;
        long deniedAt = Long.MIN_VALUE;
    }

    private static final Map<UUID, Pool> POOLS = new HashMap<>();

    private FruitEnergy() {
    }

    static Pool pool(ServerPlayer player) {
        return POOLS.computeIfAbsent(player.m_20148_(), k -> new Pool());
    }

    /** The full energy {@code move} costs. */
    public static int cost(FruitMove move) {
        return move == null || move.cooldown() == null ? 0 : COSTS.getOrDefault(move.cooldown(), USUAL);
    }

    /** Whether {@code move}'s cost is listed (HudTests: every move of HexPiece's kits). */
    public static boolean listed(FruitMove move) {
        return move.cooldown() != null && COSTS.containsKey(move.cooldown());
    }

    /** The least {@code move} can cost (a right-click move's tap). */
    static double least(FruitMove move) {
        int full = cost(move);
        return move != null && "[RMB]".equals(move.key()) ? Math.max(4.0, full * TAP_SHARE) : full;
    }

    /**
     * Whether {@code player} has the energy to start {@code move} (creative and spectators always do); if not, the action bar says so
     * and its bar flashes (at most every 10 ticks).
     */
    public static boolean afford(ServerPlayer player, FruitMove move) {
        if (player.m_7500_() || player.m_5833_()) {
            return true;
        }
        Pool pool = pool(player);
        if (pool.energy + 1.0E-6 >= least(move)) {
            return true;
        }
        long now = player.m_9236_().m_46467_();
        if (now - pool.deniedAt >= 10) {
            pool.deniedAt = now;
            player.m_5661_(Component.m_237113_("§bNot enough fruit energy"), true);
            PieceNetwork.sendTo(player, new S2CEnergy((int) Math.round(pool.energy * 10.0), true));
        }
        return false;
    }

    /** {@code move} began with a {@code cooldown}-tick cooldown: its energy is spent (a tap's share for a quick right-click). */
    public static void spend(ServerPlayer player, FruitMove move, int cooldown) {
        if (player.m_7500_() || player.m_5833_()) {
            return;
        }
        int full = cost(move);
        if (full <= 0) {
            return;
        }
        double amount = "[RMB]".equals(move.key()) && cooldown <= TAP_COOLDOWN ? Math.max(4.0, full * TAP_SHARE) : full;
        Pool pool = pool(player);
        pool.energy = Math.max(0.0, pool.energy - amount);
        pool.spentAt = player.m_9236_().m_46467_();
        sync(player, pool);
    }

    static void sync(ServerPlayer player, Pool pool) {
        int whole = (int) Math.floor(pool.energy);
        if (whole != pool.sent) {
            pool.sent = whole;
            PieceNetwork.sendTo(player, new S2CEnergy((int) Math.round(pool.energy * 10.0), false));
        }
    }

    /** Refilling, after a short rest from the last move. */
    @SubscribeEvent
    public static void refill(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Pool pool = POOLS.get(player.m_20148_());
        if (pool == null) {
            if (player.f_19797_ % 20 == 0) {
                sync(player, pool(player)); // a new player's bar is told it is full
            }
            return;
        }
        if (pool.energy >= MOST) {
            if (pool.sent != (int) MOST) {
                sync(player, pool);
            }
            return;
        }
        if (player.m_9236_().m_46467_() - pool.spentAt < PAUSE) {
            return;
        }
        pool.energy = Math.min(MOST, pool.energy + REFILL);
        sync(player, pool);
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        POOLS.remove(event.getEntity().m_20148_());
    }
}
