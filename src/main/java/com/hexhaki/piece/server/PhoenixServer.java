package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.piece.phoenix.PhoenixForm;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.6.9: the Phoenix form on the server, as the dragon's (server/DragonServer): Z turns the human into the phoenix
 * (PhoenixForm.FORMING, then PHOENIX) and back (UNFORMING); a press while it changes is ignored; after turning back no new form for
 * {@link #REST} (the owner's answer: "No meter, cooldown"). The flight IS the game's creative flight (allowed, flying from the start, at
 * {@link #FLY_SPEED}: the reference's phoenix the fastest of every Zoan), handed back as it was when the phoenix goes (a creative
 * player keeps flying), never left with the player: on leaving, death, a change of world, the server stopping and (a mark in the
 * player's data) after a crash. No fall is counted while it is out or just after.
 * <p>
 * Every client is told its phase (S2CUoFx {@link #PHASE}: bx the phase, ticks how long it has been in it), again every
 * {@link #RESEND} ticks for whoever comes near later.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class PhoenixServer {
    /** The phoenix's messages (client/phoenix/PhoenixFx: 400 to 419). */
    public static final int PHASE = 400;
    /** Creative flight is 0.05 (sprinting doubles it): the phoenix a little faster. */
    public static final float FLY_SPEED = 0.065F;
    /** After the phoenix goes, no new one for this long (ticks); no fall damage for this long. */
    public static final int REST = 60;
    static final int SOFT_LANDING = 200;
    public static final int RESEND = 40;
    /** The player data's mark: the flight is the phoenix's (cleared when it is given back). */
    static final String MARK = "HexPiecePhoenixFlight";

    static final class State {
        int phase = PhoenixForm.HUMAN;
        long start;
        boolean hadMayfly;
        float oldSpeed = 0.05F;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final Map<UUID, Long> RESTING = new HashMap<>();
    private static final Map<UUID, Long> SOFT = new HashMap<>();

    private PhoenixServer() {
    }

    /** The player's form now. */
    public static int phase(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        return state == null ? PhoenixForm.HUMAN : state.phase;
    }

    /** Z: the human becomes the phoenix, the phoenix the human; not while it changes. */
    public static void toggle(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        int phase = state == null ? PhoenixForm.HUMAN : state.phase;
        long now = player.m_9236_().m_46467_();
        if (phase == PhoenixForm.HUMAN) {
            Long rest = RESTING.get(player.m_20148_());
            if (rest != null && now < rest && !OpMode.on(player) || player.m_5833_() || FruitServer.inSea(player)) {
                return;
            }
            State fresh = new State();
            Abilities abilities = player.m_150110_();
            fresh.hadMayfly = abilities.f_35936_;
            fresh.oldSpeed = abilities.m_35942_();
            STATES.put(player.m_20148_(), fresh);
            player.getPersistentData().m_128379_(MARK, true);
            enter(player, fresh, PhoenixForm.FORMING, now);
            fly(player);
            ItoMoves.sound(player, player.m_20182_(), com.hexhaki.piece.magu.MaguSounds.WHOOSH, 1.6F, 0.8F);
        } else if (phase == PhoenixForm.PHOENIX) {
            enter(player, state, PhoenixForm.UNFORMING, now);
            ItoMoves.sound(player, player.m_20182_(), com.hexhaki.piece.magu.MaguSounds.WHOOSH, 1.2F, 1.1F);
        }
    }

    /** The sea (or the fruit taken from the player while it is out): the phoenix goes back as it does on Z. */
    public static void revert(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        if (state != null && state.phase == PhoenixForm.PHOENIX) {
            enter(player, state, PhoenixForm.UNFORMING, player.m_9236_().m_46467_());
        }
    }

    /** At once, without the way back (the fruit gone, death, leaving): the human, the flight given back. */
    public static void restore(ServerPlayer player) {
        State state = STATES.remove(player.m_20148_());
        if (state != null) {
            giveBack(player, state);
            tell(player, PhoenixForm.HUMAN, 0);
        } else if (player.getPersistentData().m_128471_(MARK)) {
            giveBack(player, new State());
        }
    }

    static void enter(ServerPlayer player, State state, int phase, long now) {
        state.phase = phase;
        state.start = now;
        tell(player, phase, 0);
    }

    static void tell(ServerPlayer player, int phase, int ticks) {
        ItoMoves.fx(player, PHASE, 0, player.m_20182_(), Vec3.f_82478_, phase, 0, 0, ticks);
    }

    /** The phoenix's flight: allowed, on, at its speed. */
    static void fly(ServerPlayer player) {
        Abilities abilities = player.m_150110_();
        boolean changed = !abilities.f_35936_ || abilities.m_35942_() != FLY_SPEED;
        abilities.f_35936_ = true;
        abilities.m_35943_(FLY_SPEED);
        if (!abilities.f_35935_) {
            abilities.f_35935_ = true;
            changed = true;
        }
        if (changed) {
            player.m_6885_();
        }
    }

    /** The flight as it was before (a creative or spectator player keeps it), and no fall counted after it. */
    static void giveBack(ServerPlayer player, State state) {
        Abilities abilities = player.m_150110_();
        boolean keep = state.hadMayfly || player.m_7500_() || player.m_5833_();
        abilities.f_35936_ = keep;
        if (!keep) {
            abilities.f_35935_ = false;
        }
        abilities.m_35943_(state.oldSpeed);
        player.m_6885_();
        player.f_19789_ = 0.0F;
        player.getPersistentData().m_128473_(MARK);
        long now = player.m_9236_().m_46467_();
        RESTING.put(player.m_20148_(), now + REST);
        SOFT.put(player.m_20148_(), now + SOFT_LANDING);
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
        if (!player.m_6084_() || FruitData.fruit(player) != Fruit.TORI) {
            restore(player);
            return;
        }
        player.f_19789_ = 0.0F;
        Abilities abilities = player.m_150110_();
        if (!abilities.f_35936_ || abilities.m_35942_() != FLY_SPEED) {
            // something else took the flight (a game mode change): the phoenix's again
            abilities.f_35936_ = true;
            abilities.m_35943_(FLY_SPEED);
            player.m_6885_();
        }
        if (state.phase == PhoenixForm.FORMING && ticks >= PhoenixForm.FORM_TICKS) {
            enter(player, state, PhoenixForm.PHOENIX, now);
        } else if (state.phase == PhoenixForm.PHOENIX && FruitServer.inSea(player)) {
            enter(player, state, PhoenixForm.UNFORMING, now);
        } else if (state.phase == PhoenixForm.UNFORMING && ticks >= PhoenixForm.UNFORM_TICKS) {
            STATES.remove(player.m_20148_());
            giveBack(player, state);
            tell(player, PhoenixForm.HUMAN, 0);
        } else if (now % RESEND == 0) {
            tell(player, state.phase, (int) Math.min(Integer.MAX_VALUE, ticks));
        }
    }

    /** No fall damage while the phoenix is out or just after it. */
    @SubscribeEvent
    public static void fall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Long until = SOFT.get(player.m_20148_());
            if (STATES.containsKey(player.m_20148_()) || until != null && player.m_9236_().m_46467_() < until) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void died(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restore(player);
        }
    }

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CompoundTag data = player.getPersistentData();
            if (data.m_128471_(MARK)) {
                // the server stopped with the phoenix out (a crash): its flight is not the player's
                restore(player);
            }
        }
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restore(player);
            RESTING.remove(player.m_20148_());
            SOFT.remove(player.m_20148_());
        }
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restore(player);
        }
    }

    /** A player who starts seeing a phoenix sees it as it is. */
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
        SOFT.clear();
    }
}
