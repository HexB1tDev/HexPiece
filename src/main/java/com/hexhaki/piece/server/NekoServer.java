package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.piece.neko.LeopardForm;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.7.9 (the owner: "Neko Neko No Mi ... Zoan ... We need ... transforming and de-transforming animations"; the reference's Transform:
 * "slouches down, taps into the awakened power and transforms, a roar that sends nearby players back before the form is revealed;
 * transformed: ... speed up DRASTICALLY"): the leopard form on the server, as the phoenix's (server/PhoenixServer) without the flight: Z
 * turns the human into the leopard (LeopardForm.FORMING, then LEOPARD) and back (UNFORMING); a press while it changes is ignored; after
 * turning back no new form for {@link #REST}. At the roar (LeopardForm.ROAR) everyone within {@link #ROAR_REACH} is thrown back. While it
 * is the leopard it runs fast (Speed {@link #SPEED} + 1, renewed while it lasts, never shown). The sea takes it away.
 * <p>
 * Every client is told its phase (S2CUoFx {@link #PHASE}: bx the phase, ticks how long it has been in it), again every {@link #RESEND}
 * ticks for whoever comes near later.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class NekoServer {
    /** The Leopard fruit's messages (client/neko/NekoFx: 420 to 439). */
    public static final int PHASE = 420;
    public static final int REST = 60;
    public static final int RESEND = 40;
    /** The roar: how far it throws (blocks), how hard (blocks a tick), how high. */
    public static final double ROAR_REACH = 7.0;
    static final double ROAR_PUSH = 1.4;
    static final double ROAR_LIFT = 0.45;
    /** The leopard's speed (the Speed effect's amplifier: 4 is twice the walking speed), renewed every so often. */
    public static final int SPEED = 4;
    static final int SPEED_EVERY = 10;

    static final class State {
        int phase = LeopardForm.HUMAN;
        long start;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final Map<UUID, Long> RESTING = new HashMap<>();

    private NekoServer() {
    }

    /** The player's form now. */
    public static int phase(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        return state == null ? LeopardForm.HUMAN : state.phase;
    }

    /** Whether the player is the leopard now (out: not changing). */
    public static boolean leopard(ServerPlayer player) {
        return phase(player) == LeopardForm.LEOPARD;
    }

    /** Z: the human becomes the leopard, the leopard the human; not while it changes. */
    public static void toggle(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        int phase = state == null ? LeopardForm.HUMAN : state.phase;
        long now = player.m_9236_().m_46467_();
        if (phase == LeopardForm.HUMAN) {
            Long rest = RESTING.get(player.m_20148_());
            if (rest != null && now < rest && !OpMode.on(player) || player.m_5833_() || FruitServer.inSea(player)) {
                return;
            }
            State fresh = new State();
            STATES.put(player.m_20148_(), fresh);
            enter(player, fresh, LeopardForm.FORMING, now);
            ItoMoves.sound(player, player.m_20182_(), com.hexhaki.piece.magu.MaguSounds.WHOOSH, 1.4F, 0.6F);
        } else if (phase == LeopardForm.LEOPARD) {
            enter(player, state, LeopardForm.UNFORMING, now);
            ItoMoves.sound(player, player.m_20182_(), com.hexhaki.piece.magu.MaguSounds.WHOOSH, 1.2F, 0.8F);
        }
    }

    /** The sea (or the fruit taken from the player while it is out): the leopard goes back as it does on Z. */
    public static void revert(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        if (state != null && state.phase == LeopardForm.LEOPARD) {
            enter(player, state, LeopardForm.UNFORMING, player.m_9236_().m_46467_());
        }
    }

    /** At once, without the way back (the fruit gone, death, leaving): the human. */
    public static void restore(ServerPlayer player) {
        if (STATES.remove(player.m_20148_()) != null) {
            player.m_21195_(MobEffects.f_19596_);
            RESTING.put(player.m_20148_(), player.m_9236_().m_46467_() + REST);
            tell(player, LeopardForm.HUMAN, 0);
        }
    }

    static void enter(ServerPlayer player, State state, int phase, long now) {
        state.phase = phase;
        state.start = now;
        tell(player, phase, 0);
        if (phase == LeopardForm.FORMING) {
            ItoMoves.animate(player, "neko_form");
        } else if (phase == LeopardForm.UNFORMING) {
            ItoMoves.animate(player, "neko_unform");
            player.m_21195_(MobEffects.f_19596_);
        }
    }

    static void tell(ServerPlayer player, int phase, int ticks) {
        ItoMoves.fx(player, PHASE, 0, player.m_20182_(), Vec3.f_82478_, phase, 0, 0, ticks);
    }

    /** The roar: everyone near thrown back from the player, the Conqueror's lightning out of it (POINT ONE). */
    static void roar(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 at = player.m_20182_();
        ItoMoves.sound(player, at, com.hexhaki.piece.dragon.UoSounds.ROAR, 2.6F, 1.35F);
        for (LivingEntity body : level.m_6443_(LivingEntity.class, player.m_20191_().m_82400_(ROAR_REACH),
            e -> e != player && e.m_6084_() && !e.m_5833_() && com.hexhaki.fruit.FruitUtil.foe(player, e))) {
            Vec3 away = body.m_20182_().m_82546_(at);
            double d = Math.sqrt(away.f_82479_ * away.f_82479_ + away.f_82481_ * away.f_82481_);
            if (d > ROAR_REACH) {
                continue;
            }
            Vec3 flat = d < 1.0E-3 ? player.m_20154_() : new Vec3(away.f_82479_ / d, 0.0, away.f_82481_ / d);
            double k = 1.0 - 0.5 * d / ROAR_REACH;
            StyleCombat.push(body, new Vec3(flat.f_82479_ * ROAR_PUSH * k, ROAR_LIFT, flat.f_82481_ * ROAR_PUSH * k));
        }
        ConquerorAura.exploded(player, level, at.m_82520_(0.0, 1.0, 0.0), 4.0);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || STATES.isEmpty() || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        State state = STATES.get(player.m_20148_());
        if (state == null) {
            return;
        }
        long now = player.m_9236_().m_46467_();
        long ticks = now - state.start;
        if (!player.m_6084_() || FruitData.fruit(player) != Fruit.NEKO) {
            restore(player);
            return;
        }
        if (state.phase == LeopardForm.FORMING && ticks == LeopardForm.ROAR) {
            roar(player);
        }
        if (state.phase == LeopardForm.LEOPARD && ticks % SPEED_EVERY == 0) {
            player.m_7292_(new MobEffectInstance(MobEffects.f_19596_, SPEED_EVERY + 30, SPEED, false, false, false));
        }
        if (state.phase == LeopardForm.FORMING && ticks >= LeopardForm.FORM_TICKS) {
            enter(player, state, LeopardForm.LEOPARD, now);
        } else if (state.phase == LeopardForm.LEOPARD && FruitServer.inSea(player)) {
            enter(player, state, LeopardForm.UNFORMING, now);
        } else if (state.phase == LeopardForm.UNFORMING && ticks >= LeopardForm.UNFORM_TICKS) {
            STATES.remove(player.m_20148_());
            RESTING.put(player.m_20148_(), now + REST);
            tell(player, LeopardForm.HUMAN, 0);
        } else if (now % RESEND == 0) {
            tell(player, state.phase, (int) Math.min(Integer.MAX_VALUE, ticks));
        }
    }

    @SubscribeEvent
    public static void died(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restore(player);
        }
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restore(player);
            RESTING.remove(player.m_20148_());
        }
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restore(player);
        }
    }

    /** A player who starts seeing a leopard sees it as it is. */
    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer viewer) {
            State state = STATES.get(target.m_20148_());
            if (state != null) {
                int ticks = (int) Math.min(Integer.MAX_VALUE, target.m_9236_().m_46467_() - state.start);
                com.hexhaki.piece.network.PieceNetwork.sendTo(viewer, new com.hexhaki.piece.network.S2CUoFx(PHASE, 0, target.m_19879_(),
                    target.m_20185_(), target.m_20186_(), target.m_20189_(), 0, 0, 0, state.phase, 0, 0, 0, ticks));
            }
        }
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        STATES.clear();
        RESTING.clear();
    }
}
