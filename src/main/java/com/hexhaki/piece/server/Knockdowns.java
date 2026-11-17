package com.hexhaki.piece.server;

import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CWater;
import com.hexhaki.piece.style.Tumble;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.3.9: bodies knocked off their feet (the owner's rag doll, HexGhoul 6.2.0's {@code PaleKnockdown}): every client draws them tumbling
 * ({@link Tumble}, {@code client/fx/Tumbles}, told by {@link S2CWater#KNOCKED}); here, from the tick one touches the ground it is held
 * down, slowed to nothing, for {@link Tumble#DOWN_TICKS} and while it gets up ({@link Tumble#GETUP_TICKS}), and throws nothing all the
 * while ({@code Clashes.staggered}).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class Knockdowns {
    /** A launched body: its world, when it was launched and when it came down (MIN_VALUE: still in the air). */
    private static final class Down {
        final ServerLevel level;
        final long at;
        long landed = Long.MIN_VALUE;

        Down(ServerLevel level, long at) {
            this.level = level;
            this.at = at;
        }
    }

    private static final Map<Integer, Down> DOWN = new HashMap<>();

    private Knockdowns() {
    }

    /** {@code body} knocked off its feet now, sent along {@code launch} (the throw itself is the caller's): every client near sees it. */
    public static void knocked(LivingEntity body, Vec3 launch) {
        if (!(body.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        long now = level.m_46467_();
        DOWN.put(body.m_19879_(), new Down(level, now));
        Clashes.stagger(body, now, (int) Tumble.AIRBORNE_MAX);
        PieceNetwork.around(body, new S2CWater(S2CWater.KNOCKED, 0, body.m_19879_(), 0, body.m_19879_(), body.m_20185_(), body.m_20186_(),
            body.m_20189_(), (float) launch.f_82479_, (float) launch.f_82480_, (float) launch.f_82481_));
    }

    /** True while {@code body} is knocked down (in the air off its feet, lying there or getting up). */
    public static boolean down(LivingEntity body) {
        return DOWN.containsKey(body.m_19879_());
    }

    @SubscribeEvent
    public static void knockdownTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || DOWN.isEmpty()) {
            return;
        }
        long now = level.m_46467_();
        for (Iterator<Map.Entry<Integer, Down>> it = DOWN.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Down> entry = it.next();
            Down down = entry.getValue();
            if (down.level != level) {
                continue;
            }
            Entity entity = level.m_6815_(entry.getKey());
            if (!(entity instanceof LivingEntity body) || !body.m_6084_() || now < down.at) {
                it.remove();
                continue;
            }
            long since = now - down.at;
            if (down.landed == Long.MIN_VALUE) {
                if ((since >= 3 && body.m_20096_()) || since >= Tumble.AIRBORNE_MAX) {
                    down.landed = now;
                } else {
                    continue;
                }
            }
            long left = down.landed + Tumble.DOWN_TICKS + Tumble.GETUP_TICKS - now;
            if (left <= 0) {
                it.remove();
                continue;
            }
            // down: it skids to a stop and stays there until it is up, throwing nothing
            body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 3, 6, false, false, false));
            Clashes.stagger(body, now, (int) left);
        }
    }
}
