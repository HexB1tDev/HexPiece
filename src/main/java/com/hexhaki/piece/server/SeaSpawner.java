package com.hexhaki.piece.server;

import com.hexhaki.piece.Faction;
import com.hexhaki.piece.PieceEntities;
import com.hexhaki.piece.npc.SeaFighter;
import com.hexhaki.piece.npc.SeaRules;
import com.hexhaki.piece.style.FightingStyle;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.1: pirates and marines turn up in patrols around players in the overworld ({@link SeaRules}: how often, how many at most): two
 * or three grunts, sometimes led by an officer, now and then a captain's crew. A player under a flag meets the other side more often.
 * Placed as HexGhoul places its ghouls: on solid ground at the surface, out of any fluid, with room to stand.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class SeaSpawner {
    private SeaSpawner() {
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || !(player.m_9236_() instanceof ServerLevel level)
            || level.m_46472_() != Level.f_46428_ || player.m_5833_()) {
            return;
        }
        long now = level.m_46467_();
        if ((now + player.m_19879_() * 37L) % SeaRules.PATROL_EVERY != 0) {
            return;
        }
        RandomSource random = level.m_213780_();
        if (random.m_188501_() > SeaRules.PATROL_CHANCE) {
            return;
        }
        AABB around = player.m_20191_().m_82400_(SeaRules.CROWD_RANGE);
        if (level.m_6443_(SeaFighter.class, around, fighter -> fighter.m_6084_()).size() >= SeaRules.CROWD) {
            return;
        }
        Faction own = ProfileServer.of(player).faction();
        Faction side = own.choice() && random.m_188501_() < 0.6F ? own.enemy() : (random.m_188499_() ? Faction.PIRATE : Faction.MARINE);
        double angle = random.m_188500_() * Math.PI * 2;
        double distance = SeaRules.PATROL_NEAR + random.m_188500_() * (SeaRules.PATROL_FAR - SeaRules.PATROL_NEAR);
        double x = player.m_20185_() + Math.cos(angle) * distance;
        double z = player.m_20189_() + Math.sin(angle) * distance;
        patrol(level, side, x, z, SeaRules.patrol(random.m_188500_(), random.m_188500_()), random);
    }

    /** A patrol of {@code side} with these ranks around (x, z); how many stood up. */
    public static int patrol(ServerLevel level, Faction side, double x, double z, int[] ranks, RandomSource random) {
        int placed = 0;
        FightingStyle[] styles = SeaRules.styles(side);
        for (int i = 0; i < ranks.length; i++) {
            double px = x + (random.m_188500_() - 0.5) * 5.0;
            double pz = z + (random.m_188500_() - 0.5) * 5.0;
            if (place(level, side, px, pz, SeaRules.look(ranks[i], random.m_188503_(8)), styles[random.m_188503_(styles.length)], false) != null) {
                placed++;
            }
        }
        return placed;
    }

    /** One fighter on the surface at (x, z): on solid ground, out of any fluid, with room to stand (null if not). */
    public static SeaFighter place(ServerLevel level, Faction side, double x, double z, int look, FightingStyle style, boolean stays) {
        int bx = (int) Math.floor(x);
        int bz = (int) Math.floor(z);
        int y = level.m_6924_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
        BlockState ground = level.m_8055_(BlockPos.m_274561_(bx + 0.5, y - 1, bz + 0.5));
        if (ground.m_60795_() || !ground.m_60819_().m_76178_()) {
            return null;
        }
        SeaFighter fighter = (side == Faction.MARINE ? PieceEntities.MARINE : PieceEntities.PIRATE).get().m_20615_(level);
        if (fighter == null) {
            return null;
        }
        fighter.m_7678_(bx + 0.5, y, bz + 0.5, (float) (level.m_213780_().m_188500_() * 360.0), 0.0F);
        if (!level.m_45786_(fighter)) {
            return null;
        }
        fighter.setup(look, style);
        if (stays) {
            fighter.getPersistentData().m_128379_("HexPieceStays", true);
        }
        level.m_7967_(fighter);
        return fighter;
    }

    static List<SeaFighter> near(ServerLevel level, AABB area) {
        return level.m_6443_(SeaFighter.class, area, fighter -> fighter.m_6084_());
    }
}
