package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.fruit.Fruits;
import com.hexhaki.piece.dragon.DragonColours;
import com.hexhaki.piece.dragon.DragonForm;
import com.hexhaki.piece.dragon.UoSounds;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CDragon;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.7.0: the Uo Uo no Mi's dragon form on the server (the owner: "make Z a button to transform"; "make sure it's perfectly
 * controllable, no fuck ups, it should be as smooth as flying in creative"). Z (the kit's one move, {@link UoKit}) turns the human
 * into the dragon ({@link DragonForm#FORMING}, then {@link DragonForm#DRAGON}) and back ({@link DragonForm#UNFORMING}); a press during
 * either change is ignored.
 * <p>
 * The flight IS the game's creative flight (the player's own abilities: allowed to fly, flying from the start, at {@link #FLY_SPEED}),
 * moved by the client as the game moves it, so it handles exactly as creative does (space up, sneak down, sprint faster, a double tap
 * of space to take off again after landing); the dragon is only drawn over it. What it changes is put back as it was when the dragon
 * goes (a creative or spectator player keeps flying), and the player is never left with a flight that is not theirs: on leaving, on
 * death, when the server stops, and (a mark in the player's data) after a crash. The fall after it is never counted.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class DragonServer {
    /**
     * Creative flight's speed is 0.05 (sprinting doubles it, as in creative). 2.9.0 (the owner: "The dragon is way too fast. Like this
     * flying speed is ridiculously fast"): 0.07 before, now a little under creative's own: a body this big glides.
     */
    public static final float FLY_SPEED = 0.04F;
    /** After the dragon goes, no new one for this long (ticks), and no fall damage for this long. */
    static final int REST = 10;
    static final int SOFT_LANDING = 200;
    /** The player data's mark: the flight is the dragon's (cleared when it is given back). */
    static final String MARK = "HexPieceDragonFlight";

    static final class State {
        int phase = DragonForm.HUMAN;
        long start;
        boolean hadMayfly;
        float oldSpeed = 0.05F;
        /** 2.7.1 (the owner: "Dragons can come in different colors, other than blue. (always random)"): this dragon's palette. */
        int colour;
        /** 2.8.6: when the flying dragon's next gust of wind may sound (game ticks). */
        long nextGust;
    }

    /** 2.8.6: how fast the dragon must fly for its wind to howl (blocks a tick), and how long between gusts (ticks). */
    static final double GUST_SPEED = 0.42;
    static final int GUST_EVERY = 70;

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final Map<UUID, Long> RESTING = new HashMap<>();

    private DragonServer() {
    }

    /** The player's form now. */
    public static int phase(Player player) {
        State state = STATES.get(player.m_20148_());
        return state == null ? DragonForm.HUMAN : state.phase;
    }

    /** Z: the human becomes the dragon, the dragon the human; not in the middle of either. */
    public static void toggle(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        int phase = state == null ? DragonForm.HUMAN : state.phase;
        long now = player.m_9236_().m_46467_();
        if (phase == DragonForm.HUMAN) {
            Long rest = RESTING.get(player.m_20148_());
            if (rest != null && now < rest || player.m_5833_()) {
                return;
            }
            State fresh = new State();
            fresh.colour = colour(player);
            Abilities abilities = player.m_150110_();
            fresh.hadMayfly = abilities.f_35936_;
            fresh.oldSpeed = abilities.m_35942_();
            STATES.put(player.m_20148_(), fresh);
            player.getPersistentData().m_128379_(MARK, true);
            enter(player, fresh, DragonForm.FORMING, now);
            fly(player);
            sound(player, UoSounds.FORM, 2.5F, 1.0F);
            sound(player, UoSounds.WHOOSH, 2.0F, 0.85F);
        } else if (phase == DragonForm.DRAGON) {
            enter(player, state, DragonForm.UNFORMING, now);
            sound(player, UoSounds.UNFORM, 2.0F, 1.0F);
        }
    }

    /** The fruit's data's key for its dragon's colour. */
    static final String COLOUR = "uo_colour";

    /**
     * 2.7.5 (the owner: "Dragon keeps changing color, the color should be attributed one time only when eating the fruit"): the dragon's
     * colour lives with the fruit (the original's fruit data, which goes when the fruit goes and is kept through death and leaving):
     * chosen at random when it is eaten ({@link UoKit#granted}), the same for every transformation after; a fruit eaten before 2.7.5
     * gets its colour at its first transformation and keeps it.
     */
    public static int colour(ServerPlayer player) {
        net.minecraft.nbt.CompoundTag data = com.hexhaki.fruit.FruitData.get(player);
        if (!data.m_128441_(COLOUR)) {
            data.m_128405_(COLOUR, java.util.concurrent.ThreadLocalRandom.current().nextInt(DragonColours.PALETTES.length));
        }
        return Math.floorMod(data.m_128451_(COLOUR), DragonColours.PALETTES.length);
    }

    /** The sea (or the fruit taken away while the dragon is out): the dragon goes back as it does on Z. */
    public static void revert(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        if (state != null && state.phase == DragonForm.DRAGON) {
            enter(player, state, DragonForm.UNFORMING, player.m_9236_().m_46467_());
        }
    }

    /** At once, without the way back (the fruit gone, death, leaving): the human, the flight given back. */
    public static void restore(ServerPlayer player) {
        State state = STATES.remove(player.m_20148_());
        if (state != null) {
            giveBack(player, state);
            PieceNetwork.around(player, new S2CDragon(player.m_19879_(), DragonForm.HUMAN, 0, 0));
        } else if (player.getPersistentData().m_128471_(MARK)) {
            giveBack(player, new State());
        }
    }

    static void enter(ServerPlayer player, State state, int phase, long now) {
        state.phase = phase;
        state.start = now;
        PieceNetwork.around(player, new S2CDragon(player.m_19879_(), phase, 0, state.colour));
    }

    /** The dragon's flight: allowed, on, at its speed. */
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

    /** The flight as it was before the dragon (a creative or spectator player keeps it), and no fall counted after it. */
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
        RESTING.put(player.m_20148_(), player.m_9236_().m_46467_() + REST);
        SOFT.put(player.m_20148_(), player.m_9236_().m_46467_() + SOFT_LANDING);
    }

    private static final Map<UUID, Long> SOFT = new HashMap<>();

    static void sound(ServerPlayer player, String name, float volume, float pitch) {
        // 2.8.6: the dragon's own sounds (dragon/UoSounds) first, the original's by their plain names otherwise
        SoundEvent event = name.startsWith("piece.") ? com.hexhaki.piece.PieceSounds.event(name) : Fruits.sound(name);
        if (event != null) {
            player.m_9236_().m_6263_(null, player.m_20185_(), player.m_20186_() + DragonForm.HEAD_HEIGHT, player.m_20189_(), event,
                SoundSource.PLAYERS, volume, pitch);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        State state = STATES.get(player.m_20148_());
        if (state == null) {
            return;
        }
        long now = player.m_9236_().m_46467_();
        long ticks = now - state.start;
        if (!player.m_6084_() || FruitData.fruit(player) != Fruit.UO) {
            restore(player);
            return;
        }
        player.f_19789_ = 0.0F;
        Abilities abilities = player.m_150110_();
        if (!abilities.f_35936_ || abilities.m_35942_() != FLY_SPEED) {
            // something else took the flight (a game mode change): the dragon's again
            abilities.f_35936_ = true;
            abilities.m_35943_(FLY_SPEED);
            player.m_6885_();
        }
        if (state.phase == DragonForm.FORMING && ticks >= DragonForm.FORM_TICKS) {
            enter(player, state, DragonForm.DRAGON, now);
            sound(player, UoSounds.ROAR, 3.5F, 1.0F); // 2.8.6: fully formed, it roars
        } else if (state.phase == DragonForm.DRAGON && now >= state.nextGust) {
            // 2.8.6: flying fast, the wind howls round the body, gust after gust, louder the faster it goes
            double dx = player.m_20185_() - player.f_19790_;
            double dy = player.m_20186_() - player.f_19791_;
            double dz = player.m_20189_() - player.f_19792_;
            double speed = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (speed > GUST_SPEED) {
                sound(player, UoSounds.GUST, (float) Math.min(2.5, 0.8 + speed), 0.9F + (float) Math.min(0.2, speed * 0.1));
                state.nextGust = now + GUST_EVERY;
            }
        }
        if (state.phase == DragonForm.DRAGON && FruitServer.inSea(player)) {
            enter(player, state, DragonForm.UNFORMING, now);
        } else if (state.phase == DragonForm.UNFORMING) {
            if (ticks == DragonForm.UNFORM_TICKS - 8) {
                sound(player, UoSounds.AIR_HIT, 1.2F, 1.1F);
            }
            if (ticks >= DragonForm.UNFORM_TICKS) {
                STATES.remove(player.m_20148_());
                giveBack(player, state);
                PieceNetwork.around(player, new S2CDragon(player.m_19879_(), DragonForm.HUMAN, 0, 0));
            }
        }
    }

    /** No fall damage while the dragon is out or just after it. */
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
                // the server stopped with the dragon out (a crash): its flight is not the player's
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

    /** A player who starts seeing a dragon sees it in the middle of what it is doing. */
    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer viewer) {
            State state = STATES.get(target.m_20148_());
            if (state != null) {
                int ticks = (int) Math.min(Integer.MAX_VALUE, target.m_9236_().m_46467_() - state.start);
                PieceNetwork.sendTo(viewer, new S2CDragon(target.m_19879_(), state.phase, ticks, state.colour));
            }
        }
    }

    /** The server stopping: every player has left by then (each one's leaving gave the flight back before they were saved). */
    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        STATES.clear();
        RESTING.clear();
        SOFT.clear();
    }
}
