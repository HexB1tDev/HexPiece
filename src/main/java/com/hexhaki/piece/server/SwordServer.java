package com.hexhaki.piece.server;

import com.hexhaki.data.HakiData;
import com.hexhaki.fx.Impact;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.fruit.FruitUtil;
import com.hexhaki.piece.PieceConfig;
import com.hexhaki.piece.PieceProfile;
import com.hexhaki.piece.PieceRules;
import com.hexhaki.piece.network.C2SSword;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CSword;
import com.hexhaki.piece.network.S2CSwordFx;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Hit;
import com.hexhaki.piece.style.Motion;
import com.hexhaki.piece.sword.BladePaths;
import com.hexhaki.piece.sword.Katana;
import com.hexhaki.piece.sword.KatanaItem;
import com.hexhaki.piece.sword.SwordAnims;
import com.hexhaki.piece.sword.SwordCombo;
import com.hexhaki.piece.sword.SwordMove;
import com.hexhaki.piece.sword.SwordRules;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.4: the Swordsman on the server, which decides everything (the owner: "keep combo progression server-authoritative ... Validate
 * the selected sword, reach, charge, cooldowns and resources on the server. Synchronise the active sword, combo step, draw state,
 * charge state and Ryou colour so remote observers see the same actions").
 * <ul>
 *   <li><b>The active katana</b> ({@link #scan}): of the katanas in the player's own inventory (the hotbar, the main inventory and
 *       the offhand; never a container inside it or the ender chest), the one in the main hand; else the one used last while it is
 *       still carried (each katana carries an id in its NBT, so moving it round the inventory keeps it the same sword); else the first
 *       by slot. Everyone sees it at the hip ({@link S2CSword}), whatever their style.</li>
 *   <li><b>Drawing</b>: only a Swordsman in combat mode (R) with nothing but the katana (or nothing) in the main hand draws it, and
 *       only by attacking: a cut, the charge or the quick draw. Sheathed, the first cut is its string's draw ({@link SwordCombo});
 *       {@link PieceConfig#SHEATHE_DELAY} ticks after the last action it is sheathed (a six-tick animation); a cut asked for while it
 *       is being sheathed carries on from where the blade is. Drawing never touches the inventory.</li>
 *   <li><b>Cuts</b>: one press, one cut; one press kept from {@link SwordCombo#BUFFER} ticks before the cut's cancel tick, never more.
 *       A cut hurts only inside its windows, along the blade's path swept from tick to tick ({@link BladePaths}, the clients' own
 *       keyframes) in four steps a tick and lengthened by {@link SwordRules#EXTEND}, each body once per window.</li>
 *   <li><b>The charge</b> (the right button held): from the server's own clock, clamped to {@link PieceConfig#CHARGE_MAX}; never
 *       released by itself; one release per charge. Without Armament Haki the release is a heavy cut with a longer reach; with it, the
 *       Haki slash ({@link HakiSlashes}), its size, reach, damage and Haki cost by the charge, in the player's Ryou colour.</li>
 *   <li><b>The quick draw</b> (Ctrl + M1): five blocks down the line in four ticks, everything along it cut once.</li>
 *   <li>Anything that makes a sword fight impossible (death, the katana gone, another style, combat mode off, an item in the hand or in
 *       use, swimming, climbing, riding) drops every action at once and the katana is back in its saya.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class SwordServer {
    static final String SWORD_ID = "HexPieceSword";
    /** Ticks between inventory looks for the active katana (and at once on any action). */
    static final int SCAN_EVERY = 5;
    static final int QUICK_COOLDOWN = 60;
    static final double QUICK_DISTANCE = 5.0;
    static final float QUICK_DAMAGE = 9.0F;

    static final class State {
        long swordId;
        Katana katana;
        boolean drawn;
        long sheathingUntil = -1;
        long idleSince;
        SwordMove move;
        long moveStart;
        SwordMove last;
        long lastEnd = -100000;
        long bufferedAt = -1;
        final List<Set<Integer>> struck = new ArrayList<>();
        long chargeStart = -1;
        boolean chargeDrew;
        long releaseStart = -1;
        double releaseCharge;
        boolean releaseHaki;
        boolean releaseRyou;
        Vec3 aim = Vec3.f_82478_;
        int releaseColor;
        final Set<Integer> releaseStruck = new HashSet<>();
        long quickStart = -1;
        boolean quickDrew;
        Vec3 quickLast;
        final Set<Integer> quickStruck = new HashSet<>();
        long cutReady;
        long slashReady;
        long quickReady;
        double lastX;
        double lastZ;
        double speed;
        int sentKatana = -2;
        int sentAnim = -1;
        long sentStart;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private SwordServer() {
    }

    static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.m_20148_(), id -> new State());
    }

    static long now(ServerPlayer player) {
        return player.m_9236_().m_46467_();
    }

    // --- the active katana ---------------------------------------------------------------------------------------------------

    /** The id a katana stack carries (given now if it has none: it stays with the stack wherever it is moved). */
    static long idOf(ServerPlayer player, ItemStack stack) {
        CompoundTag tag = stack.m_41783_();
        if (tag == null) {
            tag = new CompoundTag();
            stack.m_41751_(tag);
        }
        long id = tag.m_128454_(SWORD_ID);
        if (id == 0L) {
            id = player.m_217043_().m_188505_() | 1L;
            tag.m_128356_(SWORD_ID, id);
        }
        return id;
    }

    /** Finds the active katana (see the class comment); returns true if it changed. */
    static boolean scan(ServerPlayer player, State state) {
        Inventory inventory = player.m_150109_();
        ItemStack main = player.m_21205_();
        long chosen = 0L;
        Katana kind = null;
        long first = 0L;
        Katana firstKind = null;
        boolean keep = false;
        int size = Math.min(41, inventory.m_6643_());
        for (int slot = 0; slot < size; slot++) {
            if (slot >= 36 && slot < 40) {
                continue; // armour
            }
            ItemStack stack = inventory.m_8020_(slot);
            Katana katana = KatanaItem.of(stack);
            if (katana == null) {
                continue;
            }
            long id = idOf(player, stack);
            if (stack == main) {
                chosen = id;
                kind = katana;
            }
            if (id == state.swordId) {
                keep = true;
            }
            if (firstKind == null) {
                first = id;
                firstKind = katana;
            }
        }
        if (kind == null && keep) {
            chosen = state.swordId;
            kind = state.katana;
        }
        if (kind == null) {
            chosen = first;
            kind = firstKind;
        }
        boolean changed = chosen != state.swordId || kind != state.katana;
        state.swordId = chosen;
        state.katana = kind;
        return changed;
    }

    /** A Swordsman in combat mode. */
    static boolean swordsman(ServerPlayer player) {
        PieceProfile profile = ProfileServer.of(player);
        return profile.chosen() && profile.style().sword() && StyleServer.inCombat(player);
    }

    /** Free to fight with the sword: alive, nothing else in the hand or in use, not swimming, climbing or riding. */
    static boolean free(ServerPlayer player, State state) {
        if (!player.m_6084_() || player.m_5833_() || player.m_6117_() || player.m_6069_() || player.m_6147_() || player.m_20159_()) {
            return false;
        }
        ItemStack main = player.m_21205_();
        return main.m_41619_() || KatanaItem.of(main) != null;
    }

    static boolean ready(ServerPlayer player, State state) {
        return state.katana != null && swordsman(player) && free(player, state);
    }

    // --- what everyone sees ----------------------------------------------------------------------------------------------------

    static int anim(State state) {
        if (state.move != null) {
            return SwordAnims.of(state.move);
        }
        if (state.quickStart >= 0) {
            return state.quickDrew ? SwordAnims.QUICK_DRAW : SwordAnims.QUICK_CUT;
        }
        if (state.releaseStart >= 0) {
            return SwordAnims.RELEASE;
        }
        if (state.chargeStart >= 0) {
            return state.chargeDrew ? SwordAnims.CHARGE_DRAW : SwordAnims.CHARGE;
        }
        if (state.sheathingUntil >= 0) {
            return SwordAnims.SHEATHE;
        }
        return state.drawn ? SwordAnims.GUARD : SwordAnims.NONE;
    }

    static long animStart(State state) {
        if (state.move != null) {
            return state.moveStart;
        }
        if (state.quickStart >= 0) {
            return state.quickStart;
        }
        if (state.releaseStart >= 0) {
            return state.releaseStart;
        }
        if (state.chargeStart >= 0) {
            return state.chargeStart;
        }
        if (state.sheathingUntil >= 0) {
            return state.sheathingUntil - SwordRules.SHEATHE_TICKS;
        }
        return state.idleSince;
    }

    static S2CSword message(ServerPlayer player, State state) {
        return new S2CSword(player.m_19879_(), state.katana == null ? -1 : state.katana.ordinal(), anim(state),
            (int) (animStart(state) & 0x3FFFFFFF));
    }

    /** Tells everyone near (and the player) when what they see changed. */
    static void sync(ServerPlayer player, State state) {
        int katana = state.katana == null ? -1 : state.katana.ordinal();
        int anim = anim(state);
        long start = animStart(state);
        if (katana != state.sentKatana || anim != state.sentAnim || (anim != SwordAnims.NONE && start != state.sentStart)) {
            state.sentKatana = katana;
            state.sentAnim = anim;
            state.sentStart = start;
            PieceNetwork.around(player, message(player, state));
        }
    }

    // --- input -------------------------------------------------------------------------------------------------------------------

    /** One input from the client ({@link C2SSword}). */
    public static void input(ServerPlayer player, int action) {
        State state = state(player);
        scan(player, state);
        long now = now(player);
        if (action == C2SSword.CANCEL) {
            if (state.chargeStart >= 0) {
                state.chargeStart = -1;
                state.idleSince = now;
                sync(player, state);
            }
            return;
        }
        if (!ready(player, state)) {
            if (state.katana == null && swordsman(player) && action != C2SSword.RELEASE) {
                player.m_5661_(Component.m_237113_("No katana: a Swordsman needs one in the inventory"), true);
            }
            return;
        }
        switch (action) {
            case C2SSword.PRESS -> press(player, state, now);
            case C2SSword.CHARGE -> charge(player, state, now);
            case C2SSword.RELEASE -> release(player, state, now);
            case C2SSword.QUICK_DRAW -> quick(player, state, now);
            default -> {
            }
        }
        sync(player, state);
    }

    /** Sheathed now? (or near enough: the sheathing's last tick, the blade home). */
    static boolean sheathed(State state, long now) {
        return !state.drawn || (state.sheathingUntil >= 0 && now >= state.sheathingUntil - 1);
    }

    static void press(ServerPlayer player, State state, long now) {
        if (state.chargeStart >= 0 || state.releaseStart >= 0 || state.quickStart >= 0) {
            return;
        }
        if (state.move != null) {
            if (state.bufferedAt < 0) {
                long at = SwordCombo.buffered(state.move, state.moveStart, now);
                if (at > now) {
                    state.bufferedAt = at; // one kept, thrown at the cancel tick
                } else if (at == now) {
                    begin(player, state, now);
                }
            }
            return;
        }
        begin(player, state, now);
    }

    /** The next cut of the string, from the player's movement now. */
    static void begin(ServerPlayer player, State state, long now) {
        boolean sheathed = sheathed(state, now);
        SwordMove.Branch branch = SwordCombo.branch(player.m_20096_(), state.speed);
        if (state.move != null) {
            state.last = state.move;
            state.lastEnd = now;
        }
        SwordMove move = SwordCombo.next(branch, sheathed, state.last, state.lastEnd, now);
        state.move = move;
        state.moveStart = now;
        state.bufferedAt = -1;
        state.sheathingUntil = -1;
        state.drawn = true;
        state.struck.clear();
        for (int i = 0; i < move.cuts().length; i++) {
            state.struck.add(new HashSet<>());
        }
        if (!move.draw() && move.branch() == branch && state.last != null) {
            Impact.combo(player, move.display());
        }
    }

    static void charge(ServerPlayer player, State state, long now) {
        if (state.chargeStart >= 0 || state.releaseStart >= 0 || state.quickStart >= 0 || state.move != null) {
            return;
        }
        boolean haki = HakiData.flag(player, "armamentOn");
        long ready = haki ? state.slashReady : state.cutReady;
        if (now < ready) {
            player.m_5661_(Component.m_237113_((haki ? "Haki slash" : "Charged cut") + ": " + String.format(java.util.Locale.ROOT, "%.1f",
                (ready - now) / 20.0) + "s"), true);
            return;
        }
        state.chargeDrew = sheathed(state, now);
        state.chargeStart = now;
        state.drawn = true;
        state.sheathingUntil = -1;
    }

    static void release(ServerPlayer player, State state, long now) {
        if (state.chargeStart < 0) {
            return;
        }
        long held = now - state.chargeStart;
        state.chargeStart = -1;
        double charge = SwordRules.charge(held, PieceConfig.chargeMax());
        boolean haki = HakiData.flag(player, "armamentOn");
        if (haki) {
            double cost = SwordRules.lerp(PieceConfig.get(PieceConfig.SLASH_COST_MIN, SwordRules.SLASH_COST_MIN),
                PieceConfig.get(PieceConfig.SLASH_COST_MAX, SwordRules.SLASH_COST_MAX), charge);
            if (now < state.slashReady || !HakiData.consume(player, (float) cost)) {
                haki = false;
                player.m_5661_(Component.m_237113_("Not enough Haki: the cut is steel alone"), true);
            } else {
                HakiServerController.sync(player);
            }
        }
        state.releaseStart = now;
        state.releaseCharge = charge;
        state.releaseHaki = haki;
        state.releaseRyou = haki && HakiData.flag(player, "acocOn");
        state.releaseColor = haki ? RyouServer.color(player) : 0;
        state.aim = player.m_20154_();
        state.releaseStruck.clear();
        if (haki) {
            state.slashReady = now + Math.round(PieceConfig.get(PieceConfig.SLASH_COOLDOWN, SwordRules.SLASH_COOLDOWN));
        } else {
            state.cutReady = now + Math.round(PieceConfig.get(PieceConfig.CUT_COOLDOWN, SwordRules.CUT_COOLDOWN));
        }
    }

    static void quick(ServerPlayer player, State state, long now) {
        if (state.chargeStart >= 0 || state.releaseStart >= 0 || state.quickStart >= 0 || state.move != null) {
            return;
        }
        if (now < state.quickReady) {
            player.m_5661_(Component.m_237113_("Quick draw: " + String.format(java.util.Locale.ROOT, "%.1f", (state.quickReady - now) / 20.0)
                + "s"), true);
            return;
        }
        state.quickDrew = sheathed(state, now);
        state.quickStart = now;
        state.quickReady = now + QUICK_COOLDOWN;
        state.quickLast = player.m_20182_();
        state.quickStruck.clear();
        state.drawn = true;
        state.sheathingUntil = -1;
    }

    /** Every sword action dropped at once, the katana back in its saya (combat mode off, the style changed, death...). */
    public static void stop(ServerPlayer player) {
        State state = STATES.get(player.m_20148_());
        if (state != null) {
            reset(state);
            sync(player, state);
        }
    }

    static void reset(State state) {
        state.move = null;
        state.bufferedAt = -1;
        state.chargeStart = -1;
        state.releaseStart = -1;
        state.quickStart = -1;
        state.sheathingUntil = -1;
        state.drawn = false;
    }

    // --- the tick ---------------------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        State state = state(player);
        double dx = player.m_20185_() - state.lastX;
        double dz = player.m_20189_() - state.lastZ;
        state.speed = state.speed * 0.4 + Math.sqrt(dx * dx + dz * dz) * 0.6;
        state.lastX = player.m_20185_();
        state.lastZ = player.m_20189_();
        if (player.f_19797_ % SCAN_EVERY == 0) {
            scan(player, state);
        }
        try {
            tick(player, state, now(player));
        } finally {
            sync(player, state);
        }
    }

    static void tick(ServerPlayer player, State state, long now) {
        boolean busy = state.drawn || state.move != null || state.chargeStart >= 0 || state.releaseStart >= 0 || state.quickStart >= 0;
        if (!busy) {
            return;
        }
        if (!ready(player, state)) {
            reset(state);
            return;
        }
        if (state.move != null) {
            tickMove(player, state, now);
        }
        if (state.releaseStart >= 0) {
            tickRelease(player, state, now);
        }
        if (state.quickStart >= 0) {
            tickQuick(player, state, now);
        }
        boolean acting = state.move != null || state.chargeStart >= 0 || state.releaseStart >= 0 || state.quickStart >= 0;
        if (state.drawn && !acting && state.sheathingUntil < 0
            && now - state.idleSince >= Math.round(PieceConfig.get(PieceConfig.SHEATHE_DELAY, SwordRules.SHEATHE_DELAY))) {
            state.sheathingUntil = now + SwordRules.SHEATHE_TICKS;
        }
        if (state.sheathingUntil >= 0 && now >= state.sheathingUntil) {
            state.sheathingUntil = -1;
            state.drawn = false;
        }
    }

    static void tickMove(ServerPlayer player, State state, long now) {
        SwordMove move = state.move;
        int tick = (int) (now - state.moveStart);
        if (state.bufferedAt >= 0 && now >= state.bufferedAt) {
            begin(player, state, now);
            return;
        }
        if (tick >= move.duration()) {
            state.last = move;
            state.lastEnd = now;
            state.move = null;
            state.idleSince = now;
            return;
        }
        StyleCombat.carry(player, move.motion(), tick);
        SwordMove.Cut[] cuts = move.cuts();
        for (int i = 0; i < cuts.length; i++) {
            SwordMove.Cut cut = cuts[i];
            if (tick == cut.from()) {
                StyleCombat.sound(player, cut.weight().ordinal() >= Hit.Weight.HEAVY.ordinal() ? "fx.whoosh_heavy" : "fx.whoosh_light", 0.8F);
            }
            if (tick >= cut.from() && tick <= cut.to()) {
                float damage = cut.damage() * edge(state) * PieceRules.masteryDamage(mastery(player));
                int landed = sweep(player, move.animation(), tick, SwordRules.EXTEND, state.struck.get(i), damage, cut.weight(), cut.back(),
                    cut.up(), cut.stun(), other -> FruitUtil.foe(player, other), StyleCombat.of(player));
                if (landed > 0) {
                    gain(player, cut.weight(), landed);
                }
            }
        }
    }

    static void tickRelease(ServerPlayer player, State state, long now) {
        int tick = (int) (now - state.releaseStart);
        if (tick >= 15) {
            state.releaseStart = -1;
            state.idleSince = now;
            return;
        }
        StyleCombat.carry(player, Motion.lunge(1, 0.45), tick);
        double charge = state.releaseCharge;
        if (state.releaseHaki) {
            if (tick == 2) {
                double height = SwordRules.lerp(PieceConfig.get(PieceConfig.SLASH_HEIGHT_MIN, SwordRules.SLASH_HEIGHT_MIN),
                    PieceConfig.get(PieceConfig.SLASH_HEIGHT_MAX, SwordRules.SLASH_HEIGHT_MAX), charge);
                double range = SwordRules.lerp(PieceConfig.get(PieceConfig.SLASH_RANGE_MIN, SwordRules.SLASH_RANGE_MIN),
                    PieceConfig.get(PieceConfig.SLASH_RANGE_MAX, SwordRules.SLASH_RANGE_MAX), charge);
                double damage = SwordRules.lerp(PieceConfig.get(PieceConfig.SLASH_DAMAGE_MIN, SwordRules.SLASH_DAMAGE_MIN),
                    PieceConfig.get(PieceConfig.SLASH_DAMAGE_MAX, SwordRules.SLASH_DAMAGE_MAX), charge)
                    * edge(state) * PieceRules.masteryDamage(mastery(player));
                Vec3 from = player.m_146892_().m_82549_(state.aim.m_82490_(0.6));
                HakiSlashes.launch(player, from, state.aim, height, range, (float) damage, state.releaseColor, state.releaseRyou,
                    PieceConfig.get(PieceConfig.TERRAIN, true), StyleCombat.of(player), other -> FruitUtil.foe(player, other));
                StyleCombat.sound(player, "fx.whoosh_heavy", 1.0F);
                gain(player, Hit.Weight.FINISHER, 1);
            }
            return;
        }
        if (tick >= 1 && tick <= 3) {
            if (tick == 1) {
                StyleCombat.sound(player, "fx.whoosh_heavy", 1.0F);
                Vec3 look = StyleCombat.facing(player);
                PieceNetwork.around(player, new S2CSwordFx(S2CSwordFx.RELEASE, player.m_20185_(), player.m_20186_(), player.m_20189_(),
                    look.f_82479_, charge, look.f_82481_, 0));
            }
            double extend = SwordRules.lerp(PieceConfig.get(PieceConfig.CUT_REACH_MIN, SwordRules.CUT_REACH_MIN),
                PieceConfig.get(PieceConfig.CUT_REACH_MAX, SwordRules.CUT_REACH_MAX), charge);
            float damage = (float) SwordRules.lerp(PieceConfig.get(PieceConfig.CUT_DAMAGE_MIN, SwordRules.CUT_DAMAGE_MIN),
                PieceConfig.get(PieceConfig.CUT_DAMAGE_MAX, SwordRules.CUT_DAMAGE_MAX), charge) * edge(state)
                * PieceRules.masteryDamage(mastery(player));
            int landed = sweep(player, "sword_release", tick, extend, state.releaseStruck, damage, Hit.Weight.FINISHER, 1.1, 0.25, 16,
                other -> FruitUtil.foe(player, other), StyleCombat.of(player));
            if (landed > 0) {
                gain(player, Hit.Weight.FINISHER, landed);
            }
        }
    }

    static void tickQuick(ServerPlayer player, State state, long now) {
        int tick = (int) (now - state.quickStart);
        if (tick >= 16) {
            state.quickStart = -1;
            state.idleSince = now;
            return;
        }
        Vec3 look = StyleCombat.facing(player);
        if (tick >= 3 && tick <= 6) {
            double step = QUICK_DISTANCE / 4.0;
            StyleCombat.push(player, new Vec3(look.f_82479_ * step, Math.min(player.m_20184_().f_82480_, 0.0), look.f_82481_ * step));
            player.f_19789_ = 0.0F;
            if (tick == 3) {
                StyleCombat.sound(player, "fx.whoosh_heavy", 1.0F);
                PieceNetwork.around(player, new S2CSwordFx(S2CSwordFx.QUICK, player.m_20185_(), player.m_20186_(), player.m_20189_(),
                    look.f_82479_, 0.0, look.f_82481_, 0));
            }
        } else if (tick == 7) {
            StyleCombat.push(player, new Vec3(look.f_82479_ * 0.1, player.m_20184_().f_82480_, look.f_82481_ * 0.1));
        }
        if (tick >= 3 && tick <= 8) {
            // everything along the line run since last tick, within the flung blade's reach of it, is cut once
            Vec3 from = state.quickLast == null ? player.m_20182_() : state.quickLast;
            Vec3 to = player.m_20182_();
            float damage = QUICK_DAMAGE * edge(state) * PieceRules.masteryDamage(mastery(player));
            int landed = along(player, from, to, 1.7, state.quickStruck, damage, other -> FruitUtil.foe(player, other), StyleCombat.of(player));
            if (landed > 0) {
                gain(player, Hit.Weight.HEAVY, landed);
            }
        }
        state.quickLast = player.m_20182_();
    }

    static float edge(State state) {
        return state.katana == null ? 1.0F : state.katana.grade().edge();
    }

    static int mastery(ServerPlayer player) {
        return ProfileServer.of(player).mastery(FightingStyle.SWORDSMAN);
    }

    static void gain(ServerPlayer player, Hit.Weight weight, int landed) {
        int mastery = mastery(player);
        ProfileServer.addMastery(player, FightingStyle.SWORDSMAN, PieceRules.masteryGain(mastery, (1 + weight.ordinal()) * Math.min(3, landed)));
    }

    // --- cutting -------------------------------------------------------------------------------------------------------------------

    /** The blade's point {@code along} (0 base, 1 tip) at {@code time} ticks of {@code path} (6 floats a half tick), player frame. */
    static double[] blade(float[] path, double time, double along, double extend) {
        int samples = path.length / 6;
        double h = Math.max(0.0, Math.min(samples - 1, time * 2.0));
        int a = (int) Math.floor(h);
        int b = Math.min(samples - 1, a + 1);
        double u = h - a;
        double[] base = new double[3];
        double[] tip = new double[3];
        for (int k = 0; k < 3; k++) {
            base[k] = path[a * 6 + k] + (path[b * 6 + k] - path[a * 6 + k]) * u;
            tip[k] = path[a * 6 + 3 + k] + (path[b * 6 + 3 + k] - path[a * 6 + 3 + k]) * u;
        }
        double len = Math.sqrt((tip[0] - base[0]) * (tip[0] - base[0]) + (tip[1] - base[1]) * (tip[1] - base[1])
            + (tip[2] - base[2]) * (tip[2] - base[2]));
        double reach = along * (len + extend);
        double s = len < 1.0E-6 ? 0.0 : reach / len;
        return new double[] {base[0] + (tip[0] - base[0]) * s, base[1] + (tip[1] - base[1]) * s, base[2] + (tip[2] - base[2]) * s};
    }

    /**
     * The cut of {@code animation} from tick {@code tick - 1} to {@code tick}: the blade's swept path (four steps, seven points along
     * the blade and {@code extend} past its tip) placed on the user (its feet and its facing now); every foe it passes through that
     * this window has not hit yet is hit. Returns how many were.
     */
    public static int sweep(LivingEntity user, String animation, int tick, double extend, Set<Integer> struck, float damage,
                            Hit.Weight weight, double back, double up, int stun, Predicate<LivingEntity> foe, DamageSource source) {
        float[] path = BladePaths.of(animation);
        if (path == null) {
            return 0;
        }
        double yaw = Math.toRadians(user.m_146908_());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double rx = -Math.cos(yaw);
        double rz = -Math.sin(yaw);
        Vec3 feet = user.m_20182_();
        List<Vec3> points = new ArrayList<>();
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        for (int step = 0; step <= 4; step++) {
            double time = tick - 1 + step / 4.0;
            for (int j = 0; j <= 7; j++) {
                double[] p = blade(path, time, j / 7.0, extend);
                double x = feet.f_82479_ + rx * p[0] + fx * p[2];
                double y = feet.f_82480_ + p[1];
                double z = feet.f_82481_ + rz * p[0] + fz * p[2];
                points.add(new Vec3(x, y, z));
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                minZ = Math.min(minZ, z);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
                maxZ = Math.max(maxZ, z);
            }
        }
        AABB around = new AABB(minX, minY, minZ, maxX, maxY, maxZ).m_82400_(SwordRules.RADIUS + 0.5);
        int landed = 0;
        Vec3 look = StyleCombat.facing(user);
        for (LivingEntity other : user.m_9236_().m_6443_(LivingEntity.class, around,
            e -> e != user && e.m_6084_() && !e.m_5833_() && !struck.contains(e.m_19879_()) && foe.test(e))) {
            AABB body = other.m_20191_().m_82400_(SwordRules.RADIUS);
            Vec3 hitAt = null;
            for (Vec3 p : points) {
                if (body.m_82390_(p)) {
                    hitAt = p;
                    break;
                }
            }
            if (hitAt == null) {
                continue;
            }
            struck.add(other.m_19879_());
            if (strike(user, other, damage, weight, look.m_82490_(back).m_82520_(0.0, up, 0.0), stun, source, hitAt, look)) {
                landed++;
            }
        }
        return landed;
    }

    /** Everything within {@code radius} of the segment from..to (the quick draw's line), not struck yet, is hit. */
    static int along(LivingEntity user, Vec3 from, Vec3 to, double radius, Set<Integer> struck, float damage, Predicate<LivingEntity> foe,
                     DamageSource source) {
        AABB around = new AABB(Math.min(from.f_82479_, to.f_82479_), Math.min(from.f_82480_, to.f_82480_), Math.min(from.f_82481_, to.f_82481_),
            Math.max(from.f_82479_, to.f_82479_), Math.max(from.f_82480_, to.f_82480_) + 2.0, Math.max(from.f_82481_, to.f_82481_))
            .m_82400_(radius + 0.5);
        Vec3 run = to.m_82546_(from);
        double length = run.m_82553_();
        Vec3 look = length < 1.0E-6 ? StyleCombat.facing(user) : run.m_82490_(1.0 / length);
        int landed = 0;
        for (LivingEntity other : user.m_9236_().m_6443_(LivingEntity.class, around,
            e -> e != user && e.m_6084_() && !e.m_5833_() && !struck.contains(e.m_19879_()) && foe.test(e))) {
            Vec3 c = StyleCombat.middle(other);
            Vec3 rel = c.m_82546_(from);
            double t = length < 1.0E-6 ? 0.0 : Math.max(0.0, Math.min(length, rel.f_82479_ * look.f_82479_ + rel.f_82481_ * look.f_82481_));
            Vec3 closest = from.m_82549_(look.m_82490_(t)).m_82520_(0.0, 1.0, 0.0);
            double dx = c.f_82479_ - closest.f_82479_;
            double dz = c.f_82481_ - closest.f_82481_;
            if (Math.sqrt(dx * dx + dz * dz) > radius + other.m_20205_() * 0.5 || Math.abs(c.f_82480_ - closest.f_82480_) > 2.0) {
                continue;
            }
            struck.add(other.m_19879_());
            if (strike(user, other, damage, Hit.Weight.HEAVY, look.m_82490_(0.6).m_82520_(0.0, 0.2, 0.0), 14, source, c, look)) {
                landed++;
            }
        }
        return landed;
    }

    /** One cut lands on {@code target}: hurt, thrown through the original's impact, stunned, seen (a glint and sparks where it hit). */
    static boolean strike(LivingEntity user, LivingEntity target, float damage, Hit.Weight weight, Vec3 launch, int stun, DamageSource source,
                          Vec3 at, Vec3 dir) {
        target.f_19802_ = 0;
        if (!target.m_6469_(source, damage)) {
            return false;
        }
        Impact.hit(user, target, StyleCombat.spec(weight, launch));
        if (stun > 0) {
            target.m_7292_(new MobEffectInstance(MobEffects.f_19597_, stun, 3, false, false, false));
        }
        PieceNetwork.around(target, new S2CSwordFx(S2CSwordFx.HIT, at.f_82479_, at.f_82480_, at.f_82481_, dir.f_82479_, weight.ordinal(),
            dir.f_82481_, 0));
        StyleCombat.sound(user, weight.ordinal() >= Hit.Weight.HEAVY.ordinal() ? "fx.impact_heavy" : "fx.impact_light", 1.0F);
        return true;
    }

    // --- who sees what ---------------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer subject) {
            State state = STATES.get(subject.m_20148_());
            if (state != null && state.katana != null) {
                PieceNetwork.sendTo(viewer, message(subject, state));
            }
        }
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().m_20148_());
    }

    @SubscribeEvent
    public static void respawned(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            State state = state(player);
            reset(state);
            state.sentKatana = -2;
            scan(player, state);
            sync(player, state);
        }
    }
}
