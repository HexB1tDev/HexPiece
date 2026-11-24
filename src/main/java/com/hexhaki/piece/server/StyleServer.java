package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitUtil;
import com.hexhaki.fx.Impact;
import com.hexhaki.piece.Faction;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.PieceRules;
import com.hexhaki.piece.clash.ClashBlows;
import com.hexhaki.piece.clash.ClashRules;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CStance;
import com.hexhaki.piece.network.S2CStyleFx;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Hit;
import com.hexhaki.piece.style.Slot;
import com.hexhaki.piece.style.StyleCombo;
import com.hexhaki.piece.style.StyleMove;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.1: a player's fighting style on the server. Presses come from the client ({@code C2SStyleAction}); the server decides: the string
 * ({@link StyleCombo}), the right click and the specials (each on its cooldown, a special only once the style's mastery opens it), and
 * drives the move tick by tick: the animation every client plays (the original's own animation packet), the user carried
 * ({@link StyleCombat#carry}), every blow landed on its tick ({@link StyleCombat#land}), Iron Body's guard and the Shark Skin Counter.
 * Blows that land raise the style's mastery; beating someone of the other side raises a pirate's bounty or a marine's merit.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class StyleServer {
    static final class State {
        StyleMove move;
        long start;
        StyleMove lastString;
        long lastStringStart;
        long queuedAt = -1;
        /** 2.1.2: combat mode (R): the style's moves only come out while it is on; its stance is held meanwhile. */
        boolean combat;
        final Map<StyleMove, Long> ready = new EnumMap<>(StyleMove.class);
        final Set<Integer> struck = new HashSet<>();
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private StyleServer() {
    }

    static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.m_20148_(), id -> new State());
    }

    static long now(ServerPlayer player) {
        return player.m_9236_().m_46467_();
    }

    /** 2.1.4: is {@code player} in combat mode (R)? */
    public static boolean inCombat(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        return state != null && state.combat;
    }

    /** 2.3.8: the style move {@code player} is in (null: none). */
    public static StyleMove moveOf(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        return state == null ? null : state.move;
    }

    /** True while {@code player} is in a style move (the client's M1 does nothing else then). */
    public static boolean busy(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        return state != null && state.move != null;
    }

    /** 2.1.2: who holds which stance, for every client that sees {@code player} ({@code client/StanceLayer} plays it). */
    static S2CStance stance(ServerPlayer player, FightingStyle style) {
        return new S2CStance(player.m_19879_(), style == null ? -1 : style.ordinal());
    }

    /**
     * 2.1.2: combat mode on or off: the style's stance taken (or dropped) where everyone sees it, the player told on the action bar.
     * The stance is the clients' to hold ({@code client/StanceLayer}: a layer under every move), so nothing here re-plays it.
     */
    public static void combat(ServerPlayer player, boolean on) {
        PieceProfile profile = ProfileServer.of(player);
        if (on && (!profile.chosen() || !profile.style().choice() || !profile.style().armed())) {
            return;
        }
        State state = state(player);
        if (state.combat == on) {
            return;
        }
        state.combat = on;
        if (!on) {
            interrupt(player, state);
            SwordServer.stop(player);
        }
        PieceNetwork.around(player, stance(player, on ? profile.style() : null));
        player.m_5661_(Component.m_237113_(on ? "Combat stance: " + profile.style().display() : "Combat stance dropped"), true);
    }

    public static void press(ServerPlayer player, Slot slot) {
        PieceProfile profile = ProfileServer.of(player);
        FightingStyle style = profile.style();
        if (style.sword()) {
            if (slot.string()) {
                SwordServer.input(player, com.hexhaki.piece.network.C2SSword.PRESS);
            }
            return; // 2.1.4: the Swordsman's moves are the sword's (SwordServer)
        }
        if (!player.m_6084_() || player.m_5833_() || !player.m_21205_().m_41619_()) {
            return;
        }
        if (!profile.chosen() || !style.choice()) {
            return;
        }
        State state = state(player);
        if (!state.combat) {
            return;
        }
        long now = now(player);
        if (Clashes.staggered(player, now)) {
            return; // 2.3.6: knocked back by a clash, nothing thrown until it has passed
        }
        if (slot.string()) {
            if (state.move != null && !state.move.slot().string()) {
                return;
            }
            long at = StyleCombo.startFor(state.lastString, state.lastStringStart, now);
            if (at < 0) {
                return;
            }
            if (at > now) {
                state.queuedAt = at;
                return;
            }
            begin(player, state, StyleMove.of(style, StyleCombo.next(state.lastString, state.lastStringStart, now)), now);
            return;
        }
        StyleMove move = StyleMove.of(style, slot);
        if (move == null) {
            return;
        }
        if (profile.mastery(style) < move.mastery()) {
            player.m_5661_(Component.m_237113_(move.display() + " opens at " + style.display() + " mastery " + move.mastery() + " (now "
                + profile.mastery(style) + ")"), true);
            return;
        }
        Long ready = state.ready.get(move);
        if (ready != null && now < ready) {
            player.m_5661_(Component.m_237113_(move.display() + ": " + String.format(java.util.Locale.ROOT, "%.1f", (ready - now) / 20.0)
                + "s"), true);
            return;
        }
        if (state.move != null && !(state.move.slot().string() && now >= state.start + state.move.cancel())) {
            return;
        }
        begin(player, state, move, now);
    }

    static void begin(ServerPlayer player, State state, StyleMove move, long now) {
        if (move == null) {
            return;
        }
        state.move = move;
        state.start = now;
        state.queuedAt = -1;
        state.struck.clear();
        if (move.slot().string()) {
            state.lastString = move;
            state.lastStringStart = now;
        }
        if (move.cooldown() > 0) {
            state.ready.put(move, now + move.cooldown());
        }
        FruitUtil.animate(player, move.animation(), 2);
        Vec3 look = StyleCombat.facing(player);
        PieceNetwork.around(player, new S2CStyleFx(move.ordinal(), 0, player.m_19879_(), player.m_20185_(), player.m_20186_(),
            player.m_20189_(), look.f_82479_, 0.0, look.f_82481_));
        if (move.slot() != Slot.M1_1 && move.slot().string()) {
            Impact.combo(player, move.display());
        }
        switch (move.effect()) {
            case VANISH -> player.m_7292_(new MobEffectInstance(MobEffects.f_19609_, 6, 0, false, false, false));
            case GUARD -> player.m_7292_(new MobEffectInstance(MobEffects.f_19597_, move.duration(), 1, false, false, false));
            default -> {
            }
        }
    }

    static void tick(ServerPlayer player, State state) {
        long now = now(player);
        if (state.queuedAt >= 0 && now >= state.queuedAt) {
            state.queuedAt = -1;
            PieceProfile profile = ProfileServer.of(player);
            if (profile.style().choice() && !interrupted(player) && !Clashes.staggered(player, now)) {
                begin(player, state, StyleMove.of(profile.style(), StyleCombo.next(state.lastString, state.lastStringStart, now)), now);
            }
        }
        StyleMove move = state.move;
        if (move == null) {
            return;
        }
        int tick = (int) (now - state.start);
        if (tick >= move.duration()) {
            state.move = null;
            return;
        }
        if (interrupted(player)) {
            interrupt(player, state);
            return;
        }
        StyleCombat.carry(player, move, tick);
        PieceProfile profile = null;
        for (Hit hit : move.hits()) {
            if (hit.tick() == tick) {
                if (Clashes.landing(player, now, other -> FruitUtil.foe(player, other))) {
                    return; // 2.3.6: it met a blow coming back at it (or a blade came through it): nothing lands
                }
                if (profile == null) {
                    profile = ProfileServer.of(player);
                }
                int mastery = profile.mastery(move.style());
                int landed = StyleCombat.land(player, move, hit, mastery, other -> FruitUtil.foe(player, other), state.struck,
                    StyleCombat.of(player));
                if ((move == StyleMove.SP_FINGER_R || move == StyleMove.SP_FINGER_L) && armed(player)) {
                    // 2.4.1 (the owner: "finger pistol with haki shoots projectiles"): a bullet of hardened air off the finger
                    AirServer.shoot(player, mastery, other -> FruitUtil.foe(player, other), StyleCombat.of(player));
                }
                if (landed > 0) {
                    ProfileServer.addMastery(player, move.style(), PieceRules.masteryGain(mastery, 1 + hit.weight().ordinal()));
                }
            }
        }
    }

    /** 2.3.6: the style blow {@code player} has on its way {@code now} ({@code ClashBlows.style}), as a clash sees it; or null. */
    static ClashRules.Blow blow(ServerPlayer player, long now) {
        State state = STATES.get(player.m_20148_());
        if (state == null || state.move == null) {
            return null;
        }
        ClashBlows.Window window = ClashBlows.style(state.move, (int) (now - state.start));
        if (window == null) {
            return null;
        }
        int mastery = ProfileServer.of(player).mastery(state.move.style());
        return window.at(state.start, window.hit() * PieceRules.masteryDamage(mastery), armed(player), ryou(player));
    }

    /** 2.3.6: some form of Armament on (Armament, or Ryou over it): a bare blow that can meet a blade. */
    static boolean armed(ServerPlayer player) {
        return com.hexhaki.data.HakiData.flag(player, "armamentOn");
    }

    /** 2.3.6: the player's Ryou colour while Ryou is up (opaque), else 0. */
    static int ryou(ServerPlayer player) {
        return armed(player) && com.hexhaki.data.HakiData.flag(player, "acocOn") ? RyouServer.color(player) | 0xFF000000 : 0;
    }

    /**
     * 2.3.6: {@code player}'s style move cut off by a clash ({@code Clashes}): no more of its blows; {@code recoil}: knocked back (the
     * recoil plays: thrown back from the meeting, the stance found again), else (a blade came through a bare blow) simply dropped.
     */
    static void clashed(ServerPlayer player, boolean recoil) {
        State state = STATES.get(player.m_20148_());
        if (state == null || state.move == null) {
            return;
        }
        FightingStyle style = state.move.style();
        state.move = null;
        state.queuedAt = -1;
        FruitUtil.animate(player, recoil ? ClashRules.recoil(style) : "__clear__", recoil ? 2 : 3);
    }

    /**
     * 2.1.3: what cuts a style move short (the owner: "Interrupted attacks, switching styles, item use, swimming, climbing, mounting
     * and death must release their animation overrides correctly, without leaving either arm stuck"): death, something taken into the
     * main hand, an item in use, swimming, a ladder or vine, a mount.
     */
    static boolean interrupted(ServerPlayer player) {
        return !player.m_6084_() || !player.m_21205_().m_41619_() || player.m_6117_() || player.m_6069_() || player.m_6147_()
            || player.m_20159_();
    }

    /** 2.1.3: the move in hand stops where it is: no more blows, a queued blow dropped, its animation released for everyone. */
    static void interrupt(ServerPlayer player, State state) {
        if (state.move == null) {
            return;
        }
        state.move = null;
        state.queuedAt = -1;
        FruitUtil.animate(player, "__clear__", 3);
    }

    /** 2.1.3: switching styles or leaving combat mode lets go of a move in progress the same way. */
    public static void release(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        if (state != null) {
            interrupt(player, state);
        }
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            State state = STATES.get(player.m_20148_());
            if (state != null) {
                tick(player, state);
            }
        }
    }

    /** Iron Body: blows taken while it holds barely land, and whoever struck is thrown back. */
    @SubscribeEvent
    public static void hurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        State state = STATES.get(player.m_20148_());
        if (state == null || state.move == null || state.move.effect() != StyleMove.Effect.GUARD) {
            return;
        }
        event.setAmount(event.getAmount() * 0.2F);
        if (event.getSource().m_7639_() instanceof LivingEntity attacker && attacker != player && attacker.m_20280_(player) < 25.0) {
            Vec3 away = attacker.m_20182_().m_82546_(player.m_20182_());
            double length = Math.max(0.01, Math.sqrt(away.f_82479_ * away.f_82479_ + away.f_82481_ * away.f_82481_));
            Impact.hit(player, attacker, Impact.Spec.medium(new Vec3(away.f_82479_ / length * 0.9, 0.25, away.f_82481_ / length * 0.9)));
            StyleCombat.sound(player, "fx.parry", 0.9F);
        }
    }

    /**
     * 2.8.4: Paper Art: while it holds, every blow aimed at the user (struck or shot by someone) flutters past: nothing lands, nothing
     * throws them, and the body drifts aside from the one who struck. A fall or a fire still finds them.
     */
    @SubscribeEvent
    public static void dodge(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        State state = STATES.get(player.m_20148_());
        if (state == null || state.move == null || state.move.effect() != StyleMove.Effect.DODGE) {
            return;
        }
        net.minecraft.world.entity.Entity attacker = event.getSource().m_7639_();
        if (attacker == null || attacker == player) {
            return;
        }
        event.setCanceled(true);
        Vec3 away = player.m_20182_().m_82546_(attacker.m_20182_());
        double length = Math.max(0.01, Math.sqrt(away.f_82479_ * away.f_82479_ + away.f_82481_ * away.f_82481_));
        double ax = away.f_82479_ / length;
        double az = away.f_82481_ / length;
        // aside, a little away: as paper drifts off the air a hand pushes ahead of it
        double side = (player.m_217043_().m_188499_() ? 1.0 : -1.0) * 0.32;
        StyleCombat.push(player, new Vec3(-az * side + ax * 0.12, 0.04, ax * side + az * 0.12));
        StyleCombat.sound(player, "fx.whoosh_light", 0.6F);
    }

    /** A pirate who beats a marine (or the reverse) earns renown: a player counts as a captain. */
    @SubscribeEvent
    public static void died(LivingDeathEvent event) {
        if (!(event.getSource().m_7639_() instanceof ServerPlayer killer) || event.getEntity() == killer) {
            return;
        }
        Faction side = ProfileServer.of(killer).faction();
        if (!side.choice()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer victim) {
            if (ProfileServer.of(victim).faction() == side.enemy()) {
                ProfileServer.addRenown(killer, PieceRules.renownFor(side, 3));
            }
            return;
        }
        int rank = NpcSides.rankIfEnemy(event.getEntity(), side);
        if (rank >= 0) {
            ProfileServer.addRenown(killer, PieceRules.renownFor(side, rank));
        }
    }

    /** 2.1.2: someone who starts seeing a player in combat mode sees their stance too. */
    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer subject) {
            State state = STATES.get(subject.m_20148_());
            if (state != null && state.combat) {
                PieceNetwork.sendTo(viewer, stance(subject, ProfileServer.of(subject).style()));
            }
        }
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        State gone = STATES.remove(event.getEntity().m_20148_());
        if (gone != null && gone.combat && event.getEntity() instanceof ServerPlayer player) {
            PieceNetwork.around(player, stance(player, null));
        }
    }
}
