package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.fx.Impact;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.horo.HoroSounds;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.3.6: the Horo Horo no Mi's moves (server/HoroKit; the keys the owner accepted, docs/handoff 8zw). What everyone sees is one small
 * {@link S2CUoFx} (kinds {@link #GHOST} to {@link #NETWORK}, 220 to 239) and every client draws the ghosts from it (client/horo/HoroFx,
 * horo/HoroArt): a Negative Hollow's flight from where and which way, a Mini Hollow's target (each client flies it there and keeps it
 * on), the Toku Hollow's drift, the astral spirit's body, the network. The animations are the mod's own (the owner: "simple reused
 * animations already in the mod"): hie_partisan_throw, pika_laser_charge/end, hie_pheasant, hie_ball_gather/throw, hie_partisan_raise,
 * magu_rise.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class HoroMoves {
    // ---- what the clients draw (S2CUoFx kinds 220 to 239 are the Horo fruit's)
    /** A Negative Hollow flying: id its seed, x, y, z from, a its step a tick, size its head, ticks its flight. */
    public static final int GHOST = 220;
    /** Despair: id the body, x, y, z the top of its head, size its width, ticks how long. */
    public static final int DESPAIR = 222;
    /** A Mini Hollow: id its seed, user, x, y, z from, a where it waits with no one to cling to, bx the body it clings to (-1 none),
     *  ticks its life. */
    public static final int MINI = 223;
    /** A Mini Hollow bursting: id its seed, x, y, z where. */
    public static final int MINI_POP = 224;
    /** The Toku Hollow: id its seed, x, y, z from, a its drift a tick, size its head, ticks its longest life. */
    public static final int TOKU = 225;
    /** The Toku Hollow exploding: id its seed, x, y, z where, size how big. */
    public static final int TOKU_BOOM = 226;
    /** Astral Projection: user, x, y, z the body's feet, a.x its facing (degrees), ticks how long (0: back in the body). */
    public static final int ASTRAL = 227;
    /** Ghost Network: user, ticks how long. */
    public static final int NETWORK = 228;

    // ---- numbers
    static final int TAP = 5;
    /** Negative Hollows: at most this many in a stream, one every so many ticks, how fast, how long they fly, how close they pass. */
    public static final int STREAM_MOST = 5;
    static final int STREAM_EVERY = 6;
    public static final double GHOST_SPEED = 0.6;
    public static final int GHOST_FLIGHT = 45;
    public static final double GHOST_SIZE = 0.75;
    static final double GHOST_REACH = 1.1;
    /** Despair: how long. */
    public static final int DESPAIR_TICKS = 120;
    /** Mini Hollows: how many, how far they look for foes, their life, their burst. */
    static final int MINIS = 4;
    static final double MINI_RANGE = 16.0;
    public static final int MINI_LIFE = 120;
    static final double MINI_RADIUS = 2.5;
    /** The Toku Hollow: its size, drift, life, how close it must come, its blast. */
    public static final double TOKU_SIZE = 2.6;
    static final double TOKU_SPEED = 0.25;
    public static final int TOKU_LIFE = 200;
    static final double TOKU_TOUCH = 2.0;
    public static final double TOKU_RADIUS = 6.0;
    /** Astral Projection: how long. */
    public static final int ASTRAL_TICKS = 400;
    /** Ghost Network: how long, how far it sees. */
    public static final int NETWORK_TICKS = 600;
    static final double NETWORK_RANGE = 48.0;

    static final class Mini {
        final int seed;
        final LivingEntity target;
        final Vec3 point;

        Mini(int seed, LivingEntity target, Vec3 point) {
            this.seed = seed;
            this.target = target;
            this.point = point;
        }
    }

    static final class Toku {
        final int seed;
        Vec3 at;
        final Vec3 step;
        final long until;

        Toku(int seed, Vec3 at, Vec3 step, long until) {
            this.seed = seed;
            this.at = at;
            this.step = step;
            this.until = until;
        }
    }

    static final class State {
        long busyUntil;
        long useSince = -1;
        int sent;
        long nextGhost;
        final List<Mini> minis = new ArrayList<>();
        long minisUntil;
        Toku toku;
        long astralUntil = -1;
        ServerPlayer spirit;
        Vec3 astralFrom;
        float astralYaw;
        float astralPitch;
        boolean hadMayfly;
        boolean hadFlying;
        long networkUntil = -1;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final List<UoMoves.Live> LIVE = new ArrayList<>();
    private static final Map<String, long[]> HEARD = new HashMap<>();
    private static final Map<UUID, Long> BOLTS = new HashMap<>();
    private static int seeds;

    private HoroMoves() {
    }

    static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.m_20148_(), k -> new State());
    }

    static long now(ServerPlayer player) {
        return player.m_9236_().m_46467_();
    }

    static void fx(ServerPlayer player, int kind, int id, Vec3 at, Vec3 a, double bx, double size, int ticks) {
        PieceNetwork.around(player, new S2CUoFx(kind, id, player.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_, (float) a.f_82479_,
            (float) a.f_82480_, (float) a.f_82481_, (float) bx, 0, 0, (float) size, ticks));
    }

    static void sound(ServerPlayer player, Vec3 at, String name, float volume, float pitch) {
        SoundEvent event = com.hexhaki.piece.PieceSounds.event(name);
        if (event != null) {
            player.m_9236_().m_6263_(null, at.f_82479_, at.f_82480_, at.f_82481_, event, SoundSource.PLAYERS, volume, pitch);
        }
    }

    /** At most {@code most} of one event of one user's in any {@code gap} ticks (a crowd in despair at once is one or two sounds). */
    static void sounds(ServerPlayer player, Vec3 at, String name, float volume, float pitch, int most, int gap) {
        long now = now(player);
        long[] heard = HEARD.computeIfAbsent(player.m_20148_() + name, k -> new long[] {Long.MIN_VALUE / 2, 0});
        if (now - heard[0] >= gap || now < heard[0]) {
            heard[0] = now;
            heard[1] = 0;
        }
        if (heard[1]++ < most) {
            sound(player, at, name, volume, pitch);
        }
    }

    /** The Conqueror's lightning where a ghost strikes (POINT ONE; visual only), at most once every 4 ticks a user. */
    static void lightning(ServerPlayer player, Vec3 at) {
        long now = now(player);
        Long next = BOLTS.get(player.m_20148_());
        if (next != null && now < next && now >= next - 4) {
            return;
        }
        if (ConquerorAura.mixed(player, at, 2.0, 5, -1.0)) {
            BOLTS.put(player.m_20148_(), now + 4);
        }
    }

    static boolean ready(ServerPlayer player, FruitMove move) {
        return move.cooldown() == null || FruitData.ready(player, move.cooldown()) && FruitEnergy.afford(player, move);
    }

    static void cool(ServerPlayer player, FruitMove move, int ticks) {
        FruitEnergy.spend(player, move, ticks);
        FruitData.cooldown(player, move.cooldown(), ticks);
        FruitServer.sync(player);
    }

    static void animate(ServerPlayer player, String name) {
        HakiServerController.animate(player, name, 1);
    }

    static void later(ServerPlayer player, int ticks, Runnable action) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        LIVE.add(new UoMoves.Later(level, now(player) + Math.max(0, ticks), () -> {
            if (player.m_6084_()) {
                action.run();
            }
        }));
    }

    static boolean busy(State s, long now) {
        return s.useSince >= 0 || now < s.busyUntil;
    }

    static Vec3 chest(Entity player) {
        return player.m_146892_().m_82520_(0.0, -0.35, 0.0);
    }

    static boolean astral(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        return s != null && s.astralUntil >= 0;
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    static void press(ServerPlayer player, int slot) {
        State s = state(player);
        long now = now(player);
        if (slot == HoroKit.ASTRAL.slot() && s.astralUntil >= 0) {
            astralEnd(player, s); // back in the body, whatever else is going on
            return;
        }
        if (slot == HoroKit.MINI.slot() && !s.minis.isEmpty()) {
            burstMinis(player, s); // the second press: they burst
            return;
        }
        if (slot == HoroKit.TOKU.slot() && s.toku != null) {
            boom(player, s); // the second press: it explodes
            return;
        }
        if (busy(s, now) || player.m_20159_() || s.astralUntil >= 0) {
            return;
        }
        if (slot == HoroKit.MINI.slot()) {
            minis(player, s);
        } else if (slot == HoroKit.TOKU.slot()) {
            toku(player, s);
        } else if (slot == HoroKit.ASTRAL.slot()) {
            astral(player, s);
        } else if (slot == HoroKit.NETWORK.slot()) {
            network(player, s);
        }
    }

    // ------------------------------------------------------------------------------------------------------ RMB: Negative Hollow

    static void use(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || player.m_20159_() || s.astralUntil >= 0 || !ready(player, HoroKit.NEGATIVE)) {
            return;
        }
        s.useSince = now;
        s.sent = 0;
        s.nextGhost = now + STREAM_EVERY;
        animate(player, "hie_partisan_throw");
        send(player, s);
    }

    static void useUp(ServerPlayer player) {
        State s = state(player);
        if (s.useSince < 0) {
            return;
        }
        boolean stream = s.sent > 1;
        cool(player, HoroKit.NEGATIVE, stream ? 40 + 10 * s.sent : 24);
        s.useSince = -1;
        s.busyUntil = now(player) + 6;
        if (stream) {
            animate(player, "pika_laser_end");
        }
    }

    /** One Negative Hollow off the hand, along the look. */
    static void send(ServerPlayer player, State s) {
        s.sent++;
        Vec3 look = player.m_20154_();
        Vec3 from = chest(player).m_82549_(look.m_82490_(0.8));
        Vec3 step = look.m_82490_(GHOST_SPEED);
        int seed = ++seeds;
        fx(player, GHOST, seed, from, step, 0, GHOST_SIZE, GHOST_FLIGHT);
        sounds(player, from, HoroSounds.GHOST, 0.8F, 0.9F + (seed % 4) * 0.06F, 2, 6);
        LIVE.add(new Ghost(player, (ServerLevel) player.m_9236_(), from, step, GHOST_FLIGHT));
    }

    /** A Negative Hollow flying: through walls and bodies, everyone it passes sinking into despair. */
    static final class Ghost implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        Vec3 at;
        final Vec3 step;
        int left;
        final Set<Integer> passed = new HashSet<>();

        Ghost(ServerPlayer user, ServerLevel level, Vec3 at, Vec3 step, int left) {
            this.user = user;
            this.level = level;
            this.at = at;
            this.step = step;
            this.left = left;
        }

        @Override
        public ServerLevel level() {
            return level;
        }

        @Override
        public boolean tick(long now) {
            if (left-- <= 0 || !user.m_6084_()) {
                return true;
            }
            Vec3 to = at.m_82549_(step);
            for (LivingEntity body : PikaMoves.crossed(user, at, to, GHOST_REACH)) {
                if (passed.add(body.m_19879_())) {
                    despair(user, body);
                }
            }
            at = to;
            return false;
        }
    }

    /** {@code body} passed through by a Negative Hollow: down in despair, unable to fight, for {@link #DESPAIR_TICKS}. */
    static void despair(ServerPlayer user, LivingEntity body) {
        ServerLevel level = (ServerLevel) user.m_9236_();
        HakiFalls.knockDown(level, body, DESPAIR_TICKS, false);
        body.m_7292_(new MobEffectInstance(MobEffects.f_19613_, DESPAIR_TICKS, 4, false, false, false));
        body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, DESPAIR_TICKS, 3, false, false, false));
        if (body instanceof Mob mob) {
            mob.m_6710_(null);
        }
        Vec3 top = body.m_20182_().m_82520_(0.0, body.m_20206_(), 0.0);
        PieceNetwork.around(body, new S2CUoFx(DESPAIR, body.m_19879_(), user.m_19879_(), top.f_82479_, top.f_82480_, top.f_82481_, 0, 0, 0, 0,
            (float) body.m_20206_(), 0, body.m_20205_(), DESPAIR_TICKS));
        sounds(user, top, HoroSounds.DESPAIR, 0.9F, 1.0F, 2, 6);
        lightning(user, StyleCombat.middle(body));
    }

    // ------------------------------------------------------------------------------------------------------ Z: Mini Hollow

    /** Four little ghosts off to the nearest foes (or out ahead, with no one near), clinging to them until they burst. */
    static void minis(ServerPlayer player, State s) {
        if (!ready(player, HoroKit.MINI)) {
            return;
        }
        cool(player, HoroKit.MINI, 200);
        long now = now(player);
        s.busyUntil = now + 10;
        s.minisUntil = now + MINI_LIFE;
        animate(player, "hie_pheasant");
        Vec3 eye = player.m_146892_();
        List<LivingEntity> near = PikaMoves.foes(player, eye, MINI_RANGE);
        near.sort((a, b) -> Double.compare(a.m_20280_(player), b.m_20280_(player)));
        Vec3 look = player.m_20154_();
        Vec3 right = new Vec3(-look.f_82481_, 0, look.f_82479_);
        right = right.m_82556_() < 1.0E-6 ? new Vec3(1, 0, 0) : right.m_82541_();
        for (int k = 0; k < MINIS; k++) {
            LivingEntity target = near.isEmpty() ? null : near.get(k % near.size());
            Vec3 point = eye.m_82549_(look.m_82490_(3.5)).m_82549_(right.m_82490_((k - 1.5) * 1.1)).m_82520_(0, 0.4 * (k % 2), 0);
            int seed = ++seeds;
            s.minis.add(new Mini(seed, target, point));
            fx(player, MINI, seed, chest(player), point, target != null ? target.m_19879_() : -1, 0.32, MINI_LIFE);
        }
        sound(player, chest(player), HoroSounds.GHOST, 1.0F, 1.3F);
    }

    /** Every Mini Hollow bursts: a small blast each where it clings (or waits), the Conqueror's lightning out of each (POINT ONE). */
    static void burstMinis(ServerPlayer player, State s) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        for (Mini m : s.minis) {
            Vec3 at = m.target != null && m.target.m_6084_() && m.target.m_9236_() == level ? StyleCombat.middle(m.target) : m.point;
            UoMoves.explode(player, level, at, player.m_20154_(), MINI_RADIUS, 6.0F, 0, 0.6);
            fx(player, MINI_POP, m.seed, at, Vec3.f_82478_, 0, 1.0, 0);
            sounds(player, at, HoroSounds.POP, 1.0F, 1.0F + (m.seed % 3) * 0.08F, 2, 2);
        }
        s.minis.clear();
    }

    // ------------------------------------------------------------------------------------------------------ X: Toku Hollow

    /** The giant ghost floating out ahead, until it touches someone or a wall, the key again, or its time is up. */
    static void toku(ServerPlayer player, State s) {
        if (!ready(player, HoroKit.TOKU)) {
            return;
        }
        cool(player, HoroKit.TOKU, 300);
        long now = now(player);
        s.busyUntil = now + 14;
        animate(player, "hie_ball_gather");
        later(player, 8, () -> {
            animate(player, "hie_ball_throw");
            Vec3 look = player.m_20154_();
            Vec3 flat = new Vec3(look.f_82479_, Math.max(-0.3, Math.min(0.3, look.f_82480_)), look.f_82481_);
            flat = flat.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.m_82541_();
            Vec3 at = player.m_146892_().m_82549_(flat.m_82490_(3.0)).m_82520_(0, 0.3, 0);
            int seed = ++seeds;
            s.toku = new Toku(seed, at, flat.m_82490_(TOKU_SPEED), now(player) + TOKU_LIFE);
            fx(player, TOKU, seed, at, s.toku.step, 0, TOKU_SIZE, TOKU_LIFE);
            sound(player, at, HoroSounds.GHOST, 1.2F, 0.6F);
        });
    }

    static void tokuTick(ServerPlayer player, State s, long now) {
        Toku t = s.toku;
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 to = t.at.m_82549_(t.step);
        BlockHitResult block = level.m_45547_(new ClipContext(t.at, to.m_82549_(t.step.m_82541_().m_82490_(TOKU_SIZE * 0.5)),
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        boolean wall = block.m_6662_() == HitResult.Type.BLOCK;
        boolean touch = !PikaMoves.foes(player, t.at, TOKU_TOUCH).isEmpty();
        if (wall || touch || now >= t.until) {
            boom(player, s);
            return;
        }
        t.at = to;
    }

    /** The Toku Hollow explodes: a huge blast (the Conqueror's lightning out of it, POINT ONE), a shake. */
    static void boom(ServerPlayer player, State s) {
        Toku t = s.toku;
        s.toku = null;
        if (t == null) {
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        UoMoves.explode(player, level, t.at, t.step.m_82541_(), TOKU_RADIUS, 14.0F, 0, 1.4);
        fx(player, TOKU_BOOM, t.seed, t.at, Vec3.f_82478_, 0, TOKU_RADIUS, 0);
        sound(player, t.at, HoroSounds.BOOM, 2.0F, 0.9F);
        Impact.quake(level, t.at, 24.0, 0.5F, 10);
    }

    // ------------------------------------------------------------------------------------------------------ C: Astral Projection

    /** The spirit leaves the body: it flies, untouchable and unseen by mobs, the body left standing where it was. */
    static void astral(ServerPlayer player, State s) {
        if (!ready(player, HoroKit.ASTRAL)) {
            return;
        }
        cool(player, HoroKit.ASTRAL, ASTRAL_TICKS + 200);
        long now = now(player);
        s.astralUntil = now + ASTRAL_TICKS;
        s.spirit = player;
        s.astralFrom = player.m_20182_();
        s.astralYaw = player.m_146908_();
        s.astralPitch = player.m_146909_();
        Abilities abilities = player.m_150110_();
        s.hadMayfly = abilities.f_35936_;
        s.hadFlying = abilities.f_35935_;
        abilities.f_35936_ = true;
        abilities.f_35935_ = true;
        player.m_6885_();
        player.m_7292_(new MobEffectInstance(MobEffects.f_19609_, ASTRAL_TICKS + 20, 0, false, false, false));
        animate(player, "hie_partisan_raise");
        fx(player, ASTRAL, 0, s.astralFrom, new Vec3(s.astralYaw, 0, 0), 0, 0, ASTRAL_TICKS);
        sound(player, chest(player), HoroSounds.SPIRIT, 1.0F, 1.0F);
        player.m_20256_(new Vec3(0, 0.35, 0));
        player.f_19864_ = true;
    }

    /** Back in the body: the spirit returns to where the body stands, flying and unseen no more. */
    static void astralEnd(ServerPlayer player, State s) {
        if (s.astralUntil < 0) {
            return;
        }
        s.astralUntil = -1;
        Abilities abilities = player.m_150110_();
        boolean creative = player.m_7500_() || player.m_5833_();
        abilities.f_35936_ = creative || s.hadMayfly;
        abilities.f_35935_ = creative && s.hadFlying;
        player.m_6885_();
        player.m_21195_(MobEffects.f_19609_);
        if (s.astralFrom != null) {
            player.m_6021_(s.astralFrom.f_82479_, s.astralFrom.f_82480_, s.astralFrom.f_82481_);
        }
        player.m_20256_(Vec3.f_82478_);
        player.f_19789_ = 0.0F;
        fx(player, ASTRAL, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0);
        sound(player, chest(player), HoroSounds.SPIRIT, 1.0F, 0.8F);
        s.busyUntil = now(player) + 10;
    }

    /** The spirit is untouchable (only the void reaches it), and it cannot strike anything either. */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void spirit(LivingAttackEvent event) {
        if (STATES.isEmpty()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer target && astral(target)
            && !event.getSource().m_269533_(net.minecraft.tags.DamageTypeTags.f_268738_)) {
            event.setCanceled(true);
            return;
        }
        if (event.getSource().m_7639_() instanceof ServerPlayer striker && astral(striker)) {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------------------------------------------ V: Ghost Network

    /** Six ghosts spread out round the user: every living thing within {@link #NETWORK_RANGE} glows, seen through walls. */
    static void network(ServerPlayer player, State s) {
        if (!ready(player, HoroKit.NETWORK)) {
            return;
        }
        cool(player, HoroKit.NETWORK, 900);
        long now = now(player);
        s.networkUntil = now + NETWORK_TICKS;
        s.busyUntil = now + 12;
        animate(player, "magu_rise");
        fx(player, NETWORK, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, NETWORK_TICKS);
        sound(player, chest(player), HoroSounds.NETWORK, 1.0F, 1.0F);
    }

    static void networkTick(ServerPlayer player, State s, long now) {
        if ((now - s.networkUntil) % 20 != 0) {
            return;
        }
        Vec3 at = player.m_20182_();
        for (LivingEntity body : player.m_9236_().m_6443_(LivingEntity.class, UoMoves.box(at, at).m_82400_(NETWORK_RANGE),
            e -> e != player && e.m_6084_() && !e.m_5833_())) {
            if (body.m_20182_().m_82554_(at) <= NETWORK_RANGE) {
                body.m_7292_(new MobEffectInstance(MobEffects.f_19619_, 40, 0, false, false, false));
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ the ticks

    /** Each tick of a player with the kit: the stream, the Mini Hollows' time, the Toku Hollow's drift, the spirit, the network. */
    static void tick(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        long now = now(player);
        if (s.useSince >= 0 && now - s.useSince >= TAP && s.sent < STREAM_MOST && now >= s.nextGhost) {
            if (s.sent == 1) {
                animate(player, "pika_laser_charge");
            }
            s.nextGhost = now + STREAM_EVERY;
            send(player, s);
        }
        if (!s.minis.isEmpty() && now >= s.minisUntil) {
            burstMinis(player, s);
        }
        if (s.toku != null) {
            tokuTick(player, s, now);
        }
        if (s.astralUntil >= 0) {
            if (now >= s.astralUntil || !player.m_6084_()) {
                astralEnd(player, s);
            } else if (now % 10 == 0) {
                // mobs lose sight of the spirit
                Vec3 at = player.m_20182_();
                for (Mob mob : player.m_9236_().m_6443_(Mob.class, UoMoves.box(at, at).m_82400_(32.0), e -> e.m_5448_() == player)) {
                    mob.m_6710_(null);
                }
            }
        }
        if (s.networkUntil >= 0) {
            if (now >= s.networkUntil) {
                s.networkUntil = -1;
            } else {
                networkTick(player, s, now);
            }
        }
    }

    static void forget(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        astralEnd(player, s);
        if (s.networkUntil >= 0) {
            fx(player, NETWORK, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0);
        }
        for (Mini m : s.minis) {
            fx(player, MINI_POP, m.seed, m.point, Vec3.f_82478_, 1, 0, 0);
        }
        if (s.toku != null) {
            fx(player, TOKU_BOOM, s.toku.seed, s.toku.at, Vec3.f_82478_, 1, 0, 0);
        }
        STATES.remove(player.m_20148_());
    }

    /** The server stopping (before the save): every spirit put back in its body, everything else dropped. */
    static void clear() {
        for (State s : new ArrayList<>(STATES.values())) {
            if (s.astralUntil >= 0 && s.spirit != null) {
                astralEnd(s.spirit, s);
            }
        }
        STATES.clear();
        LIVE.clear();
        HEARD.clear();
        BOLTS.clear();
    }

    /** The world's tick: the ghosts in flight and everything timed. */
    @SubscribeEvent
    public static void horoTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || LIVE.isEmpty()) {
            return;
        }
        long now = level.m_46467_();
        List<UoMoves.Live> due = new ArrayList<>();
        for (UoMoves.Live live : LIVE) {
            if (live.level() == level) {
                due.add(live);
            }
        }
        for (UoMoves.Live live : due) {
            boolean done;
            try {
                done = live.tick(now);
            } catch (RuntimeException failure) {
                done = true;
            }
            if (done) {
                LIVE.remove(live);
            }
        }
    }
}
