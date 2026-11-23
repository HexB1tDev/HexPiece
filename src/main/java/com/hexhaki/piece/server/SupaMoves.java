package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.fx.Impact;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.supa.SupaSounds;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.3.5: the Supa Supa no Mi's moves (server/SupaKit; the keys the owner accepted, docs/handoff 8zw). What everyone sees is one small
 * {@link S2CUoFx} (kinds {@link #PARTS} to {@link #SPIN}, 200 to 219): which blades a body shows and for how long (the clients draw them
 * from the owner's katana steel: client/supa/SupaClient), and the cuts in the air (client/supa/SupaFx, supa/SupaArt). The animations are
 * the mod's own (the owner: "simple reused animations already in the mod"), chosen by render: pika_sword_1/2, hie_partisan_raise/throw,
 * pika_murakumo, hie_road_slide/punch, hie_partisan_launch, pika_lunge, gura_tilt.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class SupaMoves {
    // ---- what the clients draw (S2CUoFx kinds 200 to 219 are the Supa fruit's)
    /** Which blades a body shows: user, bx the mask (client/supa/SupaFx: ARMS, CLAWS, DRILLS, SKATES, STEEL, SPIN), ticks how long. */
    public static final int PARTS = 200;
    /** A flying slash: id its seed, x, y, z from, a its step a tick, bx 1 an X (Atomic Spar), size its scale, ticks its flight. */
    public static final int SLASH = 201;
    /** A flying slash ending: id its seed, x, y, z where, bx 1 against a wall. */
    public static final int SLASH_END = 202;
    /** A claw swipe's marks: user, x, y, z the chest, a the facing, bx which swipe, size its reach. */
    public static final int CLAW = 203;
    /** Sparks off steel: x, y, z where, a their way, size how many. */
    public static final int SPARK = 204;
    /** A blow glancing off the steel body: x, y, z where, a its way. */
    public static final int GLANCE = 205;
    /** One's own dash (Spiral Hollow, Sparkling Daisy): id whom it goes for (-1 none), x, y, z to, a from, bx the hold, ticks. */
    public static final int DASH = 206;
    /** A dash's wake: x, y, z from, a to, bx 0 the drills', 1 Sparkling Daisy's. */
    public static final int TRAIL = 207;
    /** A cut's flash where it struck: x, y, z, a the way, size: an X flashing there (Sparkling Daisy's). */
    public static final int CROSS = 208;
    /** Spar Break round its user: user, size its reach, ticks how long. */
    public static final int SPIN = 209;

    /** The blade masks (client/supa/SupaFx has the same). */
    static final int ARMS = 1;
    static final int CLAWS = 2;
    static final int DRILLS = 4;
    static final int SKATES = 8;
    static final int STEEL = 16;

    // ---- numbers
    /** The N key's ability number (client/FruitKeyN). */
    static final int N_SLOT = 40;
    /** RMB held longer than this (ticks) charges the slash, fully in {@link #CHARGE_FULL} more. */
    static final int TAP = 5;
    static final int CHARGE_FULL = 30;
    /** A flying slash: speed (blocks a tick), how far, its reach round its middle (times its scale). */
    public static final double SLASH_SPEED = 1.5;
    static final double SLASH_RANGE = 24.0;
    static final double SLASH_REACH = 1.1;
    /** Spar Claw: one swipe every so many ticks, its reach and half its arc (degrees). */
    static final int CLAW_EVERY = 7;
    static final double CLAW_REACH = 3.2;
    static final double CLAW_ARC = 75.0;
    /** Spiral Hollow: how far, how fast. */
    static final double SPIRAL_LENGTH = 12.0;
    static final double SPIRAL_SPEED = 1.5;
    /** Atomic Spurt: how long, its speed, its cut's reach. */
    public static final int SPURT_TICKS = 70;
    static final double SPURT_REACH = 1.6;
    /** Spider: how long. */
    public static final int SPIDER_TICKS = 160;
    /** Sparkling Daisy: how far it looks for its target, how far past it, how fast. */
    static final double DAISY_RANGE = 16.0;
    static final double DAISY_PAST = 2.5;
    static final double DAISY_SPEED = 2.2;
    /** Spar Break: how long, its reach, a cut every so many ticks. */
    public static final int BREAK_TICKS = 36;
    public static final double BREAK_REACH = 3.6;
    static final int BREAK_EVERY = 4;

    static final class State {
        long busyUntil;
        long useSince = -1;
        boolean charging;
        long clawNext;
        int claws;
        int mask;
        long maskUntil;
        long spurtUntil = -1;
        long spiderUntil = -1;
        long breakUntil = -1;
        long breakFrom;
        final Map<Integer, Long> cut = new HashMap<>();
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final List<UoMoves.Live> LIVE = new ArrayList<>();
    private static final Map<String, long[]> HEARD = new HashMap<>();
    private static int seeds;

    private SupaMoves() {
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

    /** At most {@code most} of one event of one user's in any {@code gap} ticks (a crowd cut at once is one or two sounds). */
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
        return s.useSince >= 0 || now < s.busyUntil || now < s.breakUntil;
    }

    /** {@code mask} shown on the user for {@code ticks} (added to what Spider and Atomic Spurt keep out), told to everyone near. */
    static void parts(ServerPlayer player, State s, int mask, int ticks) {
        long now = now(player);
        s.mask = mask;
        s.maskUntil = now + ticks;
        send(player, s);
    }

    /** What the user's body shows now: the move's blades, Spider's steel, Atomic Spurt's skates. */
    static void send(ServerPlayer player, State s) {
        long now = now(player);
        int mask = now < s.maskUntil ? s.mask : 0;
        long until = now < s.maskUntil ? s.maskUntil : now;
        if (now < s.spiderUntil) {
            mask |= STEEL;
            until = Math.max(until, s.spiderUntil);
        }
        if (now < s.spurtUntil) {
            mask |= SKATES;
            until = Math.max(until, s.spurtUntil);
        }
        fx(player, PARTS, 0, player.m_20182_(), Vec3.f_82478_, mask, 0, (int) Math.max(0, until - now));
    }

    static Vec3 flat(ServerPlayer player) {
        return MaguMoves.flat(player);
    }

    static Vec3 chest(ServerPlayer player) {
        return player.m_146892_().m_82520_(0.0, -0.4, 0.0);
    }

    /** A cut lands on {@code body}: damage, a push, sparks (the Conqueror's lightning at the strike, POINT ONE). */
    static void cut(ServerPlayer player, LivingEntity body, float damage, Vec3 push) {
        Vec3 at = StyleCombat.middle(body);
        if (MaguMoves.hurt(player, body, damage)) {
            StyleCombat.push(body, body.m_20184_().m_82490_(0.3).m_82549_(push));
        }
        Vec3 way = push.m_82556_() < 1.0E-6 ? Vec3.f_82478_ : push.m_82541_();
        fx(player, SPARK, ++seeds, at, way, Math.min(1.0, damage / 10.0), 0, 0);
        sounds(player, at, SupaSounds.CUT, 0.9F, 0.95F + (seeds % 5) * 0.04F, 2, 3);
        ConquerorAura.mixed(player, at, 2.0, 5, -1.0);
    }

    /** Living foes in front of {@code player} within {@code reach} and {@code arc} degrees each side of the look. */
    static List<LivingEntity> ahead(ServerPlayer player, double reach, double arc) {
        List<LivingEntity> out = new ArrayList<>();
        Vec3 eye = chest(player);
        Vec3 look = flat(player);
        double cos = Math.cos(Math.toRadians(arc));
        for (LivingEntity body : PikaMoves.foes(player, eye, reach)) {
            Vec3 d = StyleCombat.middle(body).m_82546_(eye);
            Vec3 f = new Vec3(d.f_82479_, 0, d.f_82481_);
            double l = f.m_82553_();
            if (l < 0.6 || f.m_82526_(look) / l >= cos) {
                out.add(body);
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    static void press(ServerPlayer player, int slot) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || player.m_20159_()) {
            return;
        }
        if (slot == N_SLOT) {
            sparBreak(player, s);
        } else if (slot == SupaKit.ATOMIC.slot()) {
            atomicSpar(player, s);
        } else if (slot == SupaKit.SPIRAL.slot()) {
            spiralHollow(player, s);
        } else if (slot == SupaKit.SPURT.slot()) {
            atomicSpurt(player, s);
        } else if (slot == SupaKit.SPIDER.slot()) {
            spider(player, s);
        } else if (slot == SupaKit.DAISY.slot()) {
            sparklingDaisy(player, s);
        }
    }

    // ------------------------------------------------------------------------------------------------------ LMB: Spar Claw

    /** The attack button: a swipe of the finger blades, every {@link #CLAW_EVERY} ticks, one way then the other. */
    static void claw(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || now < s.clawNext || player.m_20159_()) {
            return;
        }
        s.clawNext = now + CLAW_EVERY;
        s.claws++;
        int which = s.claws % 2;
        animate(player, which == 0 ? "pika_sword_2" : "pika_sword_1");
        parts(player, s, CLAWS, 18);
        sound(player, chest(player), SupaSounds.SWISH, 0.8F, 1.15F + which * 0.1F);
        later(player, 2, () -> {
            Vec3 f = flat(player);
            fx(player, CLAW, which, chest(player), f, which, CLAW_REACH * 0.55, 0);
            for (LivingEntity body : ahead(player, CLAW_REACH, CLAW_ARC)) {
                Vec3 side = new Vec3(-f.f_82481_, 0, f.f_82479_).m_82490_(which == 0 ? 0.15 : -0.15);
                cut(player, body, 4.0F, f.m_82490_(0.25).m_82549_(side).m_82520_(0, 0.1, 0));
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ RMB: Flying Slash

    static void use(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || player.m_20159_() || !ready(player, SupaKit.SLASH)) {
            return;
        }
        s.useSince = now;
        s.charging = false;
    }

    static void useUp(ServerPlayer player) {
        State s = state(player);
        if (s.useSince < 0) {
            return;
        }
        long held = now(player) - s.useSince;
        double charge = s.charging ? Math.max(0.0, Math.min(1.0, (held - TAP) / (double) CHARGE_FULL)) : 0.0;
        s.useSince = -1;
        s.charging = false;
        throwSlash(player, s, charge);
    }

    /** The slash let go: its size and damage by {@code charge} (0 a tap, 1 full). */
    static void throwSlash(ServerPlayer player, State s, double charge) {
        cool(player, SupaKit.SLASH, charge <= 0.0 ? 16 : 30 + (int) (40 * charge));
        s.busyUntil = now(player) + 8;
        animate(player, "hie_partisan_throw");
        parts(player, s, ARMS, 16);
        sound(player, chest(player), SupaSounds.SWISH, 1.0F + (float) charge * 0.3F, 0.95F - (float) charge * 0.2F);
        double scale = 1.0 + 1.4 * charge;
        float damage = (float) (6.0 + 8.0 * charge);
        double range = SLASH_RANGE + 12.0 * charge;
        later(player, 3, () -> {
            Vec3 look = player.m_20154_();
            shoot(player, chest(player).m_82549_(look.m_82490_(0.8)), look, scale, damage, range, false);
        });
    }

    /** A crescent (or Atomic Spar's X, {@code cross}) flying from {@code from} along {@code way}, cutting through everyone it meets. */
    static void shoot(ServerPlayer player, Vec3 from, Vec3 way, double scale, float damage, double range, boolean cross) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 step = way.m_82541_().m_82490_(SLASH_SPEED);
        int flight = (int) Math.ceil(range / SLASH_SPEED);
        int seed = ++seeds;
        fx(player, SLASH, seed, from, step, cross ? 1 : 0, scale, flight);
        LIVE.add(new Slash(player, level, from, step, flight, seed, scale, damage));
    }

    /** A flying cut. */
    static final class Slash implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        Vec3 at;
        final Vec3 step;
        int left;
        final int seed;
        final double scale;
        final float damage;
        final Set<Integer> struck = new HashSet<>();

        Slash(ServerPlayer user, ServerLevel level, Vec3 at, Vec3 step, int left, int seed, double scale, float damage) {
            this.user = user;
            this.level = level;
            this.at = at;
            this.step = step;
            this.left = left;
            this.seed = seed;
            this.scale = scale;
            this.damage = damage;
        }

        @Override
        public ServerLevel level() {
            return level;
        }

        @Override
        public boolean tick(long now) {
            if (left-- <= 0 || !user.m_6084_()) {
                fx(user, SLASH_END, seed, at, step, 0, scale, 0);
                return true;
            }
            Vec3 to = at.m_82549_(step);
            BlockHitResult block = level.m_45547_(new ClipContext(at, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));
            Vec3 end = block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_() : to;
            for (LivingEntity body : PikaMoves.crossed(user, at, end, SLASH_REACH * scale)) {
                if (struck.add(body.m_19879_())) {
                    cut(user, body, damage, step.m_82541_().m_82490_(0.5 + 0.2 * scale).m_82520_(0, 0.15, 0));
                }
            }
            if (end != to) {
                fx(user, SLASH_END, seed, end, step, 1, scale, 0);
                fx(user, SPARK, ++seeds, end, step.m_82490_(-1.0).m_82541_(), 1.0, 0, 0);
                sounds(user, end, SupaSounds.RIP, 0.9F, 1.0F, 1, 4);
                return true;
            }
            at = to;
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ Z: Atomic Spar

    /** Both arm blades cross and cut out an X that flies and cuts through everyone in its way. */
    static void atomicSpar(ServerPlayer player, State s) {
        if (!ready(player, SupaKit.ATOMIC)) {
            return;
        }
        cool(player, SupaKit.ATOMIC, 120);
        s.busyUntil = now(player) + 12;
        animate(player, "pika_murakumo");
        parts(player, s, ARMS, 20);
        sound(player, chest(player), SupaSounds.DRAW, 0.9F, 1.0F);
        later(player, 4, () -> {
            Vec3 look = player.m_20154_();
            sound(player, chest(player), SupaSounds.SWISH, 1.2F, 0.85F);
            shoot(player, chest(player).m_82549_(look.m_82490_(0.6)), look, 1.6, 11.0F, 16.0, true);
        });
    }

    // ------------------------------------------------------------------------------------------------------ X: Spiral Hollow

    /** Drills of blades round both forearms, a charge forward drilling through everything in a line. */
    static void spiralHollow(ServerPlayer player, State s) {
        if (!ready(player, SupaKit.SPIRAL)) {
            return;
        }
        cool(player, SupaKit.SPIRAL, 160);
        Vec3 from = player.m_20182_();
        Vec3 f = flat(player);
        Vec3 to = PikaMoves.room(player, from, from.m_82549_(f.m_82490_(SPIRAL_LENGTH)));
        int ticks = Math.max(3, (int) Math.ceil(to.m_82554_(from) / SPIRAL_SPEED));
        dash(player, s, from, to, f, ticks, -1, DRILLS, "hie_road_slide", "hie_road_punch", 9.0F, 1.8, 0);
    }

    /**
     * One's own dash from {@code from} to {@code to} over {@code ticks} (the player's own client carries the body, client/supa/SupaFx,
     * as the Ice fruit's slide), blades {@code mask} out, cutting everyone it passes ({@code damage}, within {@code reach}); a wake
     * ({@code trail} 0 the drills', 1 Sparkling Daisy's) and the last clip at the end.
     */
    static void dash(ServerPlayer player, State s, Vec3 from, Vec3 to, Vec3 way, int ticks, int target, int mask, String clip, String end,
                     float damage, double reach, int trail) {
        long now = now(player);
        int hold = 6;
        s.busyUntil = now + ticks + hold + 2;
        player.f_19789_ = 0.0F;
        parts(player, s, mask, ticks + hold + 8);
        fx(player, DASH, target, to, from, hold, 0, ticks);
        animate(player, clip);
        sound(player, from, trail == 0 ? SupaSounds.RIP : SupaSounds.DRAW, 1.0F, 1.0F);
        Set<Integer> struck = new HashSet<>();
        for (int k = 1; k <= ticks; k++) {
            double t = k / (double) ticks;
            Vec3 at = from.m_82549_(to.m_82546_(from).m_82490_(t)).m_82520_(0, 1.0, 0);
            later(player, k, () -> {
                for (LivingEntity body : PikaMoves.foes(player, at, reach)) {
                    if (struck.add(body.m_19879_())) {
                        Vec3 side = body.m_20182_().m_82546_(at);
                        Vec3 out = new Vec3(side.f_82479_, 0, side.f_82481_);
                        out = out.m_82556_() < 1.0E-4 ? Vec3.f_82478_ : out.m_82541_();
                        cut(player, body, damage, way.m_82490_(0.6).m_82549_(out.m_82490_(0.5)).m_82520_(0, 0.3, 0));
                    }
                }
            });
        }
        later(player, ticks, () -> {
            if (end != null) {
                animate(player, end);
            }
            Vec3 at = to.m_82520_(0, 1.0, 0);
            fx(player, TRAIL, 0, from.m_82520_(0, 1.0, 0), at, trail, 0, 0);
            fx(player, SPARK, ++seeds, at.m_82549_(way.m_82490_(0.8)), way, 1.0, 0, 0);
            sound(player, at, SupaSounds.RIP, 1.0F, 0.9F);
            ConquerorAura.mixed(player, at.m_82549_(way.m_82490_(0.8)), 2.5, 7, -1.0);
        });
    }

    // ------------------------------------------------------------------------------------------------------ C: Atomic Spurt

    /** Blades from the feet: skating at great speed for {@link #SPURT_TICKS}, cutting whoever is passed. */
    static void atomicSpurt(ServerPlayer player, State s) {
        if (!ready(player, SupaKit.SPURT)) {
            return;
        }
        cool(player, SupaKit.SPURT, 220);
        long now = now(player);
        s.spurtUntil = now + SPURT_TICKS;
        s.busyUntil = now + 6;
        s.cut.clear();
        animate(player, "hie_road_slide");
        player.m_7292_(new MobEffectInstance(MobEffects.f_19596_, SPURT_TICKS, 3, false, false, true));
        sound(player, player.m_20182_(), SupaSounds.DRAW, 0.9F, 1.2F);
        send(player, s);
    }

    // ------------------------------------------------------------------------------------------------------ V: Spider

    /** The whole body steel for {@link #SPIDER_TICKS}: blows glance off (SupaKit.blocks), the user a little slower. */
    static void spider(ServerPlayer player, State s) {
        if (!ready(player, SupaKit.SPIDER)) {
            return;
        }
        cool(player, SupaKit.SPIDER, 360);
        long now = now(player);
        s.spiderUntil = now + SPIDER_TICKS;
        s.busyUntil = now + 10;
        animate(player, "hie_partisan_launch");
        player.m_7292_(new MobEffectInstance(MobEffects.f_19597_, SPIDER_TICKS, 0, false, false, true));
        sound(player, player.m_20182_(), SupaSounds.DRAW, 1.1F, 0.8F);
        send(player, s);
    }

    /** Whether {@code source} names a blow struck with a blade in hand: a sword, an axe, one of the mod's katanas. */
    static boolean bladed(DamageSource source) {
        if (!(source.m_7639_() instanceof LivingEntity attacker) || source.m_7640_() != attacker) {
            return false;
        }
        Item held = attacker.m_21205_().m_41720_();
        return held instanceof SwordItem || held instanceof AxeItem || held instanceof com.hexhaki.piece.sword.KatanaItem;
    }

    /** SupaKit.blocks: Slashing Immunity and Spider (see there). */
    static boolean blocks(ServerPlayer player, DamageSource source) {
        if (FruitServer.hakiInfused(source)) {
            return false; // Armament Haki cuts steel
        }
        Entity attacker = source.m_7639_();
        State s = STATES.get(player.m_20148_());
        boolean spider = s != null && now(player) < s.spiderUntil;
        boolean blade = bladed(source);
        if (!blade && !(spider && attacker != null)) {
            return false;
        }
        Vec3 at = StyleCombat.middle(player);
        Vec3 from = attacker != null ? attacker.m_20182_() : at;
        Vec3 way = new Vec3(from.f_82479_ - at.f_82479_, 0, from.f_82481_ - at.f_82481_);
        way = way.m_82556_() < 1.0E-6 ? flat(player) : way.m_82541_();
        Vec3 hit = at.m_82549_(way.m_82490_(0.35));
        fx(player, GLANCE, ++seeds, hit, way, 0, 0, 0);
        sounds(player, hit, SupaSounds.GLANCE, 0.9F, 1.0F, 1, 4);
        // Spider: a bare fist that strikes the steel is cut
        if (spider && attacker instanceof LivingEntity fist && source.m_7640_() == fist && fist.m_21205_().m_41619_() && fist != player
            && fist.m_20182_().m_82554_(player.m_20182_()) < 5.0) {
            long now = now(player);
            Long last = s.cut.get(fist.m_19879_());
            if (last == null || now - last >= 10) {
                s.cut.put(fist.m_19879_(), now);
                cut(player, fist, 3.0F, way.m_82490_(0.3));
            }
        }
        return true;
    }

    // ------------------------------------------------------------------------------------------------------ B: Sparkling Daisy

    /** A lunge straight through the one looked at, both arm blades out. */
    static void sparklingDaisy(ServerPlayer player, State s) {
        if (!ready(player, SupaKit.DAISY)) {
            return;
        }
        cool(player, SupaKit.DAISY, 180);
        LivingEntity target = PikaMoves.aimed(player, DAISY_RANGE, 0.06);
        Vec3 from = player.m_20182_();
        Vec3 way;
        Vec3 to;
        if (target != null) {
            Vec3 t = target.m_20182_();
            way = new Vec3(t.f_82479_ - from.f_82479_, 0, t.f_82481_ - from.f_82481_);
            way = way.m_82556_() < 1.0E-6 ? flat(player) : way.m_82541_();
            to = PikaMoves.room(player, from, t.m_82549_(way.m_82490_(target.m_20205_() * 0.5 + DAISY_PAST)));
        } else {
            way = flat(player);
            to = PikaMoves.room(player, from, from.m_82549_(way.m_82490_(8.0)));
        }
        int ticks = Math.max(3, (int) Math.ceil(to.m_82554_(from) / DAISY_SPEED));
        dash(player, s, from, to, way, ticks, target != null ? target.m_19879_() : -1, ARMS, "pika_lunge", null, 6.0F, 1.6, 1);
        if (target != null) {
            // the one looked at: the full cut as the lunge passes through them, an X flashing where it struck
            int through = Math.max(1, (int) Math.round(ticks * Math.min(1.0, target.m_20182_().m_82554_(from) / Math.max(0.1, to.m_82554_(from)))));
            Vec3 aim = way;
            later(player, through, () -> {
                if (target.m_6084_() && target.m_20182_().m_82554_(player.m_20182_()) < 6.0) {
                    Vec3 at = StyleCombat.middle(target);
                    cut(player, target, 14.0F, aim.m_82490_(0.9).m_82520_(0, 0.35, 0));
                    fx(player, CROSS, ++seeds, at, aim, 0, 1.3, 0);
                    Impact.quake((ServerLevel) player.m_9236_(), at, 14.0, 0.3F, 6);
                }
            });
        }
    }

    // ------------------------------------------------------------------------------------------------------ N: Spar Break

    /** Blades all round the body, spinning: cuts all round every {@link #BREAK_EVERY} ticks, a last burst throwing everyone away. */
    static void sparBreak(ServerPlayer player, State s) {
        if (!ready(player, SupaKit.BREAK)) {
            return;
        }
        cool(player, SupaKit.BREAK, 220);
        long now = now(player);
        s.breakFrom = now;
        s.breakUntil = now + BREAK_TICKS;
        animate(player, "gura_tilt");
        parts(player, s, ARMS, BREAK_TICKS + 4);
        fx(player, SPIN, 0, player.m_20182_(), Vec3.f_82478_, 0, BREAK_REACH, BREAK_TICKS);
        sound(player, player.m_20182_(), SupaSounds.DRAW, 1.0F, 0.9F);
    }

    static void breakTick(ServerPlayer player, State s, long now) {
        long age = now - s.breakFrom;
        Vec3 middle = StyleCombat.middle(player);
        if (age > 0 && age % BREAK_EVERY == 0 && now < s.breakUntil) {
            for (LivingEntity body : PikaMoves.foes(player, middle, BREAK_REACH)) {
                Vec3 out = body.m_20182_().m_82546_(player.m_20182_());
                out = new Vec3(out.f_82479_, 0, out.f_82481_);
                out = out.m_82556_() < 1.0E-4 ? flat(player) : out.m_82541_();
                // swept round: pushed along the spin as much as out
                Vec3 round = new Vec3(-out.f_82481_, 0, out.f_82479_);
                cut(player, body, 3.5F, out.m_82490_(0.25).m_82549_(round.m_82490_(0.3)).m_82520_(0, 0.12, 0));
            }
        }
        if (now >= s.breakUntil) {
            s.breakUntil = -1;
            // the last burst (an explosion of blades: the Conqueror's lightning out of it, POINT ONE)
            for (LivingEntity body : PikaMoves.foes(player, middle, BREAK_REACH + 0.6)) {
                Vec3 out = body.m_20182_().m_82546_(player.m_20182_());
                out = new Vec3(out.f_82479_, 0, out.f_82481_);
                out = out.m_82556_() < 1.0E-4 ? flat(player) : out.m_82541_();
                cut(player, body, 5.0F, out.m_82490_(0.9).m_82520_(0, 0.4, 0));
            }
            fx(player, SPARK, ++seeds, middle, Vec3.f_82478_, 1.0, 0, 0);
            sound(player, middle, SupaSounds.RIP, 1.2F, 0.85F);
            ConquerorAura.exploded(player, (ServerLevel) player.m_9236_(), middle, BREAK_REACH + 1.0);
            Impact.quake((ServerLevel) player.m_9236_(), middle, 16.0, 0.35F, 8);
        }
    }

    // ------------------------------------------------------------------------------------------------------ the ticks

    /** Each tick of a player with the kit: a held RMB turning into a charge, Atomic Spurt's cuts, Spar Break's spin, steel ending. */
    static void tick(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        long now = now(player);
        if (s.useSince >= 0 && !s.charging && now - s.useSince >= TAP) {
            s.charging = true;
            animate(player, "hie_partisan_raise");
            parts(player, s, ARMS, CHARGE_FULL + 120);
            sound(player, chest(player), SupaSounds.DRAW, 0.9F, 1.1F);
        }
        if (s.useSince >= 0 && s.charging && now - s.useSince >= TAP + CHARGE_FULL + 100) {
            useUp(player); // held far past full: let go of itself
        }
        if (s.breakUntil >= 0) {
            breakTick(player, s, now);
        }
        if (now < s.spurtUntil) {
            Vec3 middle = StyleCombat.middle(player);
            for (LivingEntity body : PikaMoves.foes(player, middle, SPURT_REACH)) {
                Long last = s.cut.get(body.m_19879_());
                if (last == null || now - last >= 10) {
                    s.cut.put(body.m_19879_(), now);
                    Vec3 v = player.m_20184_();
                    Vec3 side = body.m_20182_().m_82546_(player.m_20182_());
                    side = new Vec3(side.f_82479_, 0, side.f_82481_);
                    side = side.m_82556_() < 1.0E-4 ? flat(player) : side.m_82541_();
                    cut(player, body, 5.0F, side.m_82490_(0.5).m_82549_(new Vec3(v.f_82479_, 0, v.f_82481_).m_82490_(0.5)).m_82520_(0, 0.25, 0));
                }
            }
        } else if (s.spurtUntil >= 0) {
            s.spurtUntil = -1;
            send(player, s);
        }
        if (s.spiderUntil >= 0 && now >= s.spiderUntil) {
            s.spiderUntil = -1;
            send(player, s);
        }
    }

    static void forget(ServerPlayer player) {
        State s = STATES.remove(player.m_20148_());
        if (s != null) {
            fx(player, PARTS, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0);
            fx(player, SPIN, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0);
        }
    }

    static void clear() {
        STATES.clear();
        LIVE.clear();
        HEARD.clear();
    }

    /** The world's tick: the cuts in flight and everything timed. */
    @SubscribeEvent
    public static void supaTick(TickEvent.LevelTickEvent event) {
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
