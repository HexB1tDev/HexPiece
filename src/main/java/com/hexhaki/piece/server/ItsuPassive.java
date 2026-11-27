package com.hexhaki.piece.server;

import com.hexhaki.data.HakiData;
import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.9.7 (the owner's Itsumade spec: "Passive: The user cannot be killed, unless the attacker has Ryou Haki activated, every time the
 * user is about to die he vanishes in a black smoke and appears somewhere safe 10 blocks away with full HP and Energy"): an Itsumade
 * eater's death is undone unless whoever dealt it has Ryou on (server/ConquerorAura.ryou); the body bursts into black smoke where it
 * stood and stands again {@link #AWAY} blocks off on safe ground (solid under the feet, two blocks of air, no liquid), with its health
 * and its energy full. The void and {@code /kill} still kill (there is nowhere to go and the command means it).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class ItsuPassive {
    /** How far it reappears (blocks), and how many ways round it looks for safe ground. */
    static final double AWAY = 10.0;
    static final int WAYS = 16;

    private ItsuPassive() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void dying(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || FruitData.fruit(player) != Fruit.ITSU) {
            return;
        }
        String id = event.getSource().m_19385_();
        if ("outOfWorld".equals(id) || "genericKill".equals(id)) {
            return;
        }
        Entity attacker = event.getSource().m_7639_();
        if (attacker instanceof ServerPlayer killer && ConquerorAura.ryou(killer)) {
            return; // only Ryou kills it
        }
        if (!(player.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        event.setCanceled(true);
        Vec3 from = player.m_20182_();
        Vec3 to = safe(level, player, from);
        ItoMoves.fx(player, ItsuServer.SMOKE, 0, from, Vec3.f_82478_, player.m_20206_(), 0, 0, 0);
        player.m_6021_(to.f_82479_, to.f_82480_, to.f_82481_);
        player.m_21153_(player.m_21233_());
        player.m_20095_();
        player.f_19789_ = 0.0F;
        try {
            HakiData.energy(player, HakiData.maxEnergy(player));
        } catch (RuntimeException failure) {
            // the energy is the original's: never stop the escape for it
        }
        ItoMoves.fx(player, ItsuServer.SMOKE, 0, to, Vec3.f_82478_, player.m_20206_(), 0, 0, 0);
        ItoMoves.sound(player, from, ItsuServer.VANISH, 1.4F, 0.55F);
        ItoMoves.sound(player, to, ItsuServer.VANISH, 1.2F, 0.7F);
    }

    /** Safe ground AWAY blocks from {@code from} (the first of WAYS directions that has some), or where it stands if none. */
    static Vec3 safe(ServerLevel level, ServerPlayer player, Vec3 from) {
        double start = player.m_217043_().m_188500_() * Math.PI * 2.0;
        for (int k = 0; k < WAYS; k++) {
            double a = start + k * Math.PI * 2.0 / WAYS;
            int x = (int) Math.floor(from.f_82479_ + Math.cos(a) * AWAY);
            int z = (int) Math.floor(from.f_82481_ + Math.sin(a) * AWAY);
            int top = (int) Math.floor(from.f_82480_) + 8;
            for (int y = top; y > top - 24 && y > level.m_141937_(); y--) {
                BlockPos feet = new BlockPos(x, y, z);
                BlockPos under = new BlockPos(x, y - 1, z);
                BlockPos head = new BlockPos(x, y + 1, z);
                if (!level.m_8055_(under).m_60795_() && level.m_8055_(feet).m_60795_() && level.m_8055_(head).m_60795_()
                    && level.m_6425_(feet).m_76178_() && level.m_6425_(under).m_76178_()) {
                    return new Vec3(x + 0.5, y, z + 0.5);
                }
            }
        }
        return from;
    }
}
