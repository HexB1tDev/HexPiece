package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitUtil;
import com.hexhaki.fx.Impact;
import com.hexhaki.piece.Faction;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.PieceRules;
import com.hexhaki.piece.network.PieceNetwork;
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
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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
        boolean counterUsed;
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

    /** True while {@code player} is in a style move (the client's M1 does nothing else then). */
    public static boolean busy(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        return state != null && state.move != null;
    }

    public static void press(ServerPlayer player, Slot slot) {
        if (!player.m_6084_() || player.m_5833_() || !player.m_21205_().m_41619_()) {
            return;
        }
        PieceProfile profile = ProfileServer.of(player);
        FightingStyle style = profile.style();
        if (!profile.chosen() || !style.choice()) {
            return;
        }
        State state = state(player);
        long now = now(player);
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
        state.counterUsed = false;
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
            if (profile.style().choice() && player.m_21205_().m_41619_()) {
                begin(player, state, StyleMove.of(profile.style(), StyleCombo.next(state.lastString, state.lastStringStart, now)), now);
            }
        }
        StyleMove move = state.move;
        if (move == null) {
            return;
        }
        int tick = (int) (now - state.start);
        if (tick >= move.duration() || !player.m_6084_()) {
            state.move = null;
            return;
        }
        StyleCombat.carry(player, move, tick);
        PieceProfile profile = null;
        for (Hit hit : move.hits()) {
            if (hit.tick() == tick) {
                if (profile == null) {
                    profile = ProfileServer.of(player);
                }
                int mastery = profile.mastery(move.style());
                int landed = StyleCombat.land(player, move, hit, mastery, other -> FruitUtil.foe(player, other), state.struck,
                    StyleCombat.of(player));
                if (landed > 0) {
                    ProfileServer.addMastery(player, move.style(), PieceRules.masteryGain(mastery, 1 + hit.weight().ordinal()));
                }
            }
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

    /** Shark Skin Counter: while its stance is up, the first blow taken is turned aside and paid back with a palm. */
    @SubscribeEvent
    public static void attacked(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        State state = STATES.get(player.m_20148_());
        if (state == null || state.move == null || state.move.effect() != StyleMove.Effect.COUNTER || state.counterUsed) {
            return;
        }
        int tick = (int) (now(player) - state.start);
        Entity source = event.getSource().m_7639_();
        if (tick < 2 || tick > state.move.duration() - 3 || !(source instanceof LivingEntity attacker) || attacker == player) {
            return;
        }
        event.setCanceled(true);
        state.counterUsed = true;
        int mastery = ProfileServer.of(player).mastery(state.move.style());
        Vec3 away = attacker.m_20182_().m_82546_(player.m_20182_());
        double length = Math.max(0.01, Math.sqrt(away.f_82479_ * away.f_82479_ + away.f_82481_ * away.f_82481_));
        attacker.f_19802_ = 0;
        attacker.m_6469_(StyleCombat.of(player), 9.0F * PieceRules.masteryDamage(mastery));
        Impact.hit(player, attacker, Impact.Spec.heavy(new Vec3(away.f_82479_ / length * 1.4, 0.3, away.f_82481_ / length * 1.4)));
        FruitUtil.animate(player, "style_fishman_karate_counter_strike", 1);
        StyleCombat.sound(player, "fx.counter", 1.0F);
        ProfileServer.addMastery(player, state.move.style(), 6);
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

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().m_20148_());
    }
}
