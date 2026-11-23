package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.fx.Impact;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.ito.ItoSounds;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.3.7: the Ito Ito no Mi's moves (server/ItoKit; the keys the owner accepted "As proposed" and their Birdcage, "40 blocks, 45 s":
 * docs/handoff 8zzc). What everyone sees is one small {@link S2CUoFx} (kinds {@link #SLASH} to {@link #STITCH}, 240 to 252: the kind is
 * one byte) and every client draws the strings from it (client/ito/ItoFx, ito/ItoArt); with Armament on (bz 1) every string is coated
 * black-purple. The animations are the mod's own (the owner: "reuse already in the game first person and third person animations"):
 * pika_sword_1/2, hie_partisan_launch, pika_laser_charge/end, hie_partisan_throw, hie_pheasant, hie_grab_reach, hie_ball_gather,
 * hie_partisan_raise, magu_rise, hie_ice_age, hie_ball_throw. With the Conqueror's Haki and Ryou on, every string that strikes throws the
 * Conqueror's lightning and every blast throws it out of itself (POINT ONE).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class ItoMoves {
    // ---- what the clients draw (S2CUoFx kinds 240 to 255 are the Ito fruit's; bz 1: Armament on, the strings coated)
    /** The attack button's lash: id which way, user, x, y, z the chest, a the facing, size its reach. */
    public static final int SLASH = 240;
    /** A string bullet flying: id its seed, x, y, z from, a its step a tick, ticks its flight. */
    public static final int BULLET = 241;
    /** Strings striking: id its seed, x, y, z where, a the way, bx what (HIT_*), size how big. */
    public static final int HIT = 242;
    /** Goshikito: id its seed, user, x, y, z the ground struck, a the way along the cuts, size their spread, ticks how long they show. */
    public static final int GOSHIKI = 243;
    /** Overheat: id its seed, user, x, y, z the hand, a the way, size its length, ticks how long. */
    public static final int OVERHEAT = 244;
    /** Parasite: id the one held, user, ticks how long (0: let go). */
    public static final int PARASITE = 245;
    /** Spider's Web: user, x, y, z its middle, a its facing (out, away from its user), size its radius, ticks how long (0: down). */
    public static final int WEB = 246;
    /** Black Knight: user, x, y, z its feet, ax its facing (radians, the game's way), ay what it does (0 walks, 1 and 2 cuts), ticks
     *  how long it has left (0: gone). */
    public static final int KNIGHT = 247;
    /** The Birdcage: user, x, y, z its middle on the ground, size its first radius, by its last, ticks how long (0: down). */
    public static final int CAGE = 248;
    /** Break White: id its seed, user, x, y, z its middle, size how far its waves roll, ticks how long. */
    public static final int WAVE = 249;
    /** God Thread: id its seed, user, x, y, z where the spears meet, a where they are fired from, ticks their flight. */
    public static final int THREAD = 250;
    /** Sora no Michi: user, x, y, z the feet. */
    public static final int SKY = 251;
    /** Self-Surgery: user, ticks how long the stitching shows. */
    public static final int STITCH = 252;

    /** What a HIT is. */
    public static final int HIT_STRING = 0;
    public static final int HIT_CAGE = 1;
    public static final int HIT_WEB = 2;
    public static final int HIT_KNIGHT = 3;

    // ---- numbers
    /** The N and M keys' ability numbers (client/FruitKeyN), the air step's. */
    static final int N_SLOT = 40;
    static final int M_SLOT = 41;
    static final int SKY_SLOT = 42;
    /** String Slash: one lash every so many ticks, its reach, half its arc (degrees), its damage. */
    static final int SLASH_EVERY = 8;
    public static final double SLASH_REACH = 4.5;
    static final double SLASH_ARC = 70.0;
    /** Tamaito: held longer than this (ticks) a burst, one every so many ticks, at most so many; speed, range, damage. */
    static final int TAP = 5;
    static final int BURST_EVERY = 4;
    public static final int BURST_MOST = 6;
    public static final double BULLET_SPEED = 2.4;
    static final double BULLET_RANGE = 40.0;
    static final double BULLET_REACH = 0.6;
    /** Goshikito: how far it reaches, the strings' fall (ticks), its damage, the half length and half width of its cuts. */
    static final double GOSHIKI_RANGE = 30.0;
    public static final int GOSHIKI_FALL = 6;
    public static final int GOSHIKI_LIFE = 40;
    static final double GOSHIKI_HALF = 4.0;
    public static final double GOSHIKI_SPREAD = 1.4;
    /** Overheat: its length, reach round it, damage, seconds burning, how long it shows. */
    public static final double OVERHEAT_LENGTH = 16.0;
    static final double OVERHEAT_REACH = 1.4;
    public static final int OVERHEAT_LIFE = 18;
    /** Parasite: how far it reaches, how long it holds, how near and far the held body is kept. */
    static final double PARASITE_RANGE = 24.0;
    public static final int PARASITE_TICKS = 100;
    /** Spider's Web: how far ahead, its radius, how long. */
    static final double WEB_AHEAD = 2.2;
    public static final double WEB_RADIUS = 2.4;
    public static final int WEB_TICKS = 120;
    /** Black Knight: how long, its pace, its reach, a cut every so many ticks, how far it looks for foes. */
    public static final int KNIGHT_TICKS = 300;
    static final double KNIGHT_SPEED = 0.3;
    static final double KNIGHT_REACH = 2.0;
    static final int KNIGHT_EVERY = 14;
    static final double KNIGHT_SEEK = 16.0;
    public static final int KNIGHT_SEND = 4;
    /** The Birdcage (the owner: "40 blocks, 45 s"): its first and last radius, how long. */
    public static final double CAGE_RADIUS = 40.0;
    public static final double CAGE_END = 10.0;
    public static final int CAGE_TICKS = 900;
    /** Break White: three waves, born so many ticks apart, rolling so fast, so far. */
    public static final int WAVES = 3;
    public static final int WAVE_EVERY = 8;
    public static final double WAVE_SPEED = 0.5;
    public static final double WAVE_REACH = 18.0;
    /** God Thread: sixteen spears, their flight, how far, the blast where they meet. */
    public static final int SPEARS = 16;
    public static final int THREAD_FLIGHT = 8;
    static final double THREAD_RANGE = 40.0;
    /** Sora no Michi: steps before landing, the lift a step gives. */
    public static final int SKY_STEPS = 3;
    static final double SKY_LIFT = 0.5;
    /** Self-Surgery: below this share of the health, once in so many ticks, healing so long. */
    static final double SURGERY_BELOW = 1.0 / 3.0;
    static final int SURGERY_EVERY = 1200;
    static final int SURGERY_TICKS = 120;

    /** Cage time: how far it has closed in at {@code age} ticks of {@code life} (its radius). */
    public static double cageRadius(double radius, double end, double age, double life) {
        double t = Math.max(0.0, Math.min(1.0, age / Math.max(1.0, life)));
        return radius + (end - radius) * t;
    }

    static final class State {
        long busyUntil;
        long useSince = -1;
        int sent;
        long nextBullet;
        long slashNext;
        int slashes;
        LivingEntity held;
        long heldUntil;
        double heldDistance;
        long webUntil = -1;
        Vec3 webAt;
        Vec3 webFacing;
        final List<Projectile> caught = new ArrayList<>();
        long knightUntil = -1;
        Vec3 knight;
        float knightYaw;
        long knightNext;
        int knightCuts;
        long cageUntil = -1;
        long cageFrom;
        Vec3 cageAt;
        final Map<Integer, Long> cut = new HashMap<>();
        int steps;
        long surgeryNext;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final List<UoMoves.Live> LIVE = new ArrayList<>();
    private static final Map<String, long[]> HEARD = new HashMap<>();
    private static final Map<UUID, Long> BOLTS = new HashMap<>();
    static final int BOLT_GAP = 4;
    private static int seeds;

    private ItoMoves() {
    }

    static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.m_20148_(), k -> new State());
    }

    static long now(ServerPlayer player) {
        return player.m_9236_().m_46467_();
    }

    /** Armament on: the strings coated black-purple (and cutting deeper). */
    static boolean coated(ServerPlayer player) {
        try {
            return com.hexhaki.data.HakiData.flag(player, "armamentOn");
        } catch (RuntimeException e) {
            return false;
        }
    }

    static void fx(ServerPlayer player, int kind, int id, Vec3 at, Vec3 a, double bx, double by, double size, int ticks) {
        PieceNetwork.around(player, new S2CUoFx(kind, id, player.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_, (float) a.f_82479_,
            (float) a.f_82480_, (float) a.f_82481_, (float) bx, (float) by, coated(player) ? 1 : 0, (float) size, ticks));
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

    /** The Conqueror's lightning where a string strikes (POINT ONE; visual only), at most once every {@link #BOLT_GAP} ticks a user. */
    static void lightning(ServerPlayer player, Vec3 at) {
        long now = now(player);
        Long next = BOLTS.get(player.m_20148_());
        if (next != null && now < next && now >= next - BOLT_GAP) {
            return;
        }
        if (ConquerorAura.mixed(player, at, 2.0, 5, -1.0)) {
            BOLTS.put(player.m_20148_(), now + BOLT_GAP);
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

    static Vec3 flat(ServerPlayer player) {
        return MaguMoves.flat(player);
    }

    static Vec3 chest(Entity player) {
        return player.m_146892_().m_82520_(0.0, -0.4, 0.0);
    }

    /** Strings cut {@code body}: damage (more with Armament), a push, the Conqueror's lightning at the strike. */
    static void cut(ServerPlayer player, LivingEntity body, float damage, Vec3 push) {
        Vec3 at = StyleCombat.middle(body);
        if (MaguMoves.hurt(player, body, coated(player) ? damage * 1.25F : damage)) {
            StyleCombat.push(body, body.m_20184_().m_82490_(0.3).m_82549_(push));
        }
        Vec3 way = push.m_82556_() < 1.0E-6 ? Vec3.f_82478_ : push.m_82541_();
        fx(player, HIT, ++seeds, at, way, HIT_STRING, 0, Math.min(1.5, 0.5 + damage / 10.0), 0);
        sounds(player, at, ItoSounds.SLICE, 0.9F, 0.95F + (seeds % 5) * 0.04F, 2, 3);
        lightning(player, at);
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

    /** The point looked at: the first block along the look within {@code range}, or the end of it. */
    static Vec3 looked(ServerPlayer player, double range) {
        Vec3 eye = player.m_146892_();
        Vec3 to = eye.m_82549_(player.m_20154_().m_82490_(range));
        BlockHitResult block = player.m_9236_().m_45547_(new ClipContext(eye, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_() : to;
    }

    /** The ground under {@code at} within {@code down} blocks (null: none). */
    static Vec3 ground(ServerLevel level, Vec3 at, double up, double down) {
        Vec3 from = at.m_82520_(0.0, up, 0.0);
        BlockHitResult block = level.m_45547_(new ClipContext(from, at.m_82520_(0.0, -down, 0.0), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, null));
        return block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_() : null;
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    static void press(ServerPlayer player, int slot) {
        State s = state(player);
        long now = now(player);
        if (slot == SKY_SLOT) {
            sky(player, s);
            return;
        }
        if (slot == ItoKit.PARASITE.slot() && s.held != null) {
            release(player, s); // the second press: let go
            return;
        }
        if (slot == N_SLOT && s.cageUntil >= 0) {
            cageEnd(player, s); // the second press: the cage comes down
            return;
        }
        if (busy(s, now) || player.m_20159_()) {
            return;
        }
        if (slot == N_SLOT) {
            birdcage(player, s);
        } else if (slot == M_SLOT) {
            awaken(player, s);
        } else if (slot == ItoKit.GOSHIKI.slot()) {
            goshikito(player, s);
        } else if (slot == ItoKit.OVERHEAT.slot()) {
            overheat(player, s);
        } else if (slot == ItoKit.PARASITE.slot()) {
            parasite(player, s);
        } else if (slot == ItoKit.WEB.slot()) {
            web(player, s);
        } else if (slot == ItoKit.KNIGHT.slot()) {
            blackKnight(player, s);
        }
    }

    // ------------------------------------------------------------------------------------------------------ LMB: String Slash

    /** The attack button: strings lashing out of the fingers every {@link #SLASH_EVERY} ticks, one way then the other. */
    static void slash(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || now < s.slashNext || player.m_20159_()) {
            return;
        }
        s.slashNext = now + SLASH_EVERY;
        s.slashes++;
        int which = s.slashes % 2;
        animate(player, which == 0 ? "pika_sword_2" : "pika_sword_1");
        sound(player, chest(player), ItoSounds.SWISH, 0.8F, 1.1F + which * 0.1F);
        later(player, 2, () -> {
            Vec3 f = flat(player);
            fx(player, SLASH, which, chest(player), f, which, 0, SLASH_REACH, 0);
            for (LivingEntity body : ahead(player, SLASH_REACH, SLASH_ARC)) {
                Vec3 side = new Vec3(-f.f_82481_, 0, f.f_82479_).m_82490_(which == 0 ? 0.15 : -0.15);
                cut(player, body, 4.5F, f.m_82490_(0.25).m_82549_(side).m_82520_(0, 0.1, 0));
            }
        });
    }

    // ------------------------------------------------------------------------------------------------------ RMB: Tamaito

    static void use(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (busy(s, now) || player.m_20159_() || !ready(player, ItoKit.BULLET)) {
            return;
        }
        s.useSince = now;
        s.sent = 0;
        s.nextBullet = now + TAP;
        animate(player, "hie_partisan_launch");
        shoot(player, s);
    }

    static void useUp(ServerPlayer player) {
        State s = state(player);
        if (s.useSince < 0) {
            return;
        }
        boolean burst = s.sent > 1;
        cool(player, ItoKit.BULLET, burst ? 30 + 5 * s.sent : 18);
        s.useSince = -1;
        s.busyUntil = now(player) + 5;
        if (burst) {
            animate(player, "pika_laser_end");
        }
    }

    /** One string bullet off the fingertip, along the look. */
    static void shoot(ServerPlayer player, State s) {
        s.sent++;
        Vec3 look = player.m_20154_();
        Vec3 from = chest(player).m_82549_(look.m_82490_(0.8));
        Vec3 step = look.m_82490_(BULLET_SPEED);
        int flight = (int) Math.ceil(BULLET_RANGE / BULLET_SPEED);
        int seed = ++seeds;
        fx(player, BULLET, seed, from, step, 0, 0, 1.0, flight);
        sounds(player, from, ItoSounds.SHOT, 0.9F, 0.95F + (seed % 4) * 0.05F, 2, 3);
        LIVE.add(new Bullet(player, (ServerLevel) player.m_9236_(), from, step, flight, seed));
    }

    /** A string bullet: through every body in its way (each struck once), stopped by a wall. */
    static final class Bullet implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        Vec3 at;
        final Vec3 step;
        int left;
        final int seed;
        final Set<Integer> struck = new HashSet<>();

        Bullet(ServerPlayer user, ServerLevel level, Vec3 at, Vec3 step, int left, int seed) {
            this.user = user;
            this.level = level;
            this.at = at;
            this.step = step;
            this.left = left;
            this.seed = seed;
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
            BlockHitResult block = level.m_45547_(new ClipContext(at, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));
            Vec3 end = block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_() : to;
            for (LivingEntity body : PikaMoves.crossed(user, at, end, BULLET_REACH)) {
                if (struck.add(body.m_19879_())) {
                    cut(user, body, 4.0F, step.m_82541_().m_82490_(0.35).m_82520_(0, 0.05, 0));
                }
            }
            if (end != to) {
                fx(user, HIT, seed, end, step.m_82541_().m_82490_(-1.0), HIT_STRING, 1, 0.6, 0);
                return true;
            }
            at = to;
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ Z: Goshikito

    /** Five strings slash down from the sky onto the spot looked at: the ground cut in five lines, everyone standing on them cut. */
    static void goshikito(ServerPlayer player, State s) {
        if (!ready(player, ItoKit.GOSHIKI)) {
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 at = looked(player, GOSHIKI_RANGE);
        Vec3 g = ground(level, at, 1.0, 8.0);
        Vec3 spot = g != null ? g : at;
        cool(player, ItoKit.GOSHIKI, 160);
        s.busyUntil = now(player) + 10;
        animate(player, "hie_partisan_throw");
        Vec3 way = flat(player);
        int seed = ++seeds;
        fx(player, GOSHIKI, seed, spot, way, 0, 0, GOSHIKI_SPREAD, GOSHIKI_LIFE);
        sound(player, spot, ItoSounds.SWISH, 1.2F, 0.75F);
        later(player, GOSHIKI_FALL, () -> {
            Vec3 side = new Vec3(-way.f_82481_, 0, way.f_82479_);
            for (LivingEntity body : PikaMoves.foes(player, spot, GOSHIKI_HALF + 1.0)) {
                Vec3 d = body.m_20182_().m_82546_(spot);
                double along = d.f_82479_ * way.f_82479_ + d.f_82481_ * way.f_82481_;
                double across = d.f_82479_ * side.f_82479_ + d.f_82481_ * side.f_82481_;
                if (Math.abs(along) <= GOSHIKI_HALF + body.m_20205_() * 0.5 && Math.abs(across) <= GOSHIKI_SPREAD + body.m_20205_() * 0.5
                    && Math.abs(d.f_82480_) < 3.5) {
                    cut(player, body, 12.0F, new Vec3(0, -0.2, 0).m_82549_(side.m_82490_(across >= 0 ? 0.2 : -0.2)));
                }
            }
            sound(player, spot, ItoSounds.SLICE, 1.4F, 0.8F);
            ConquerorAura.exploded(player, level, spot.m_82520_(0, 0.5, 0), GOSHIKI_SPREAD + 1.5);
            Impact.quake(level, spot, 16.0, 0.3F, 6);
        });
    }

    // ------------------------------------------------------------------------------------------------------ X: Overheat

    /** A red-hot rope of string lashing out along the look: everyone along it struck, set burning and thrown aside. */
    static void overheat(ServerPlayer player, State s) {
        if (!ready(player, ItoKit.OVERHEAT)) {
            return;
        }
        cool(player, ItoKit.OVERHEAT, 140);
        s.busyUntil = now(player) + 12;
        animate(player, "hie_pheasant");
        Vec3 way = flat(player);
        int seed = ++seeds;
        fx(player, OVERHEAT, seed, chest(player), way, 0, 0, OVERHEAT_LENGTH, OVERHEAT_LIFE);
        sound(player, chest(player), ItoSounds.BURN, 1.3F, 0.95F);
        later(player, 4, () -> {
            ServerLevel level = (ServerLevel) player.m_9236_();
            Vec3 from = chest(player);
            Vec3 look = player.m_20154_();
            Vec3 aim = new Vec3(look.f_82479_, Math.max(-0.35, Math.min(0.35, look.f_82480_)), look.f_82481_).m_82541_();
            Vec3 to = from.m_82549_(aim.m_82490_(OVERHEAT_LENGTH));
            BlockHitResult block = level.m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            Vec3 end = block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_() : to;
            Vec3 side = new Vec3(aim.f_82481_, 0, -aim.f_82479_);
            for (LivingEntity body : PikaMoves.crossed(player, from, end, OVERHEAT_REACH)) {
                cut(player, body, 10.0F, side.m_82490_(0.7).m_82549_(aim.m_82490_(0.3)).m_82520_(0, 0.3, 0));
                body.m_20254_(5);
            }
            fx(player, HIT, ++seeds, end, aim.m_82490_(-1.0), HIT_STRING, 1, 1.2, 0);
            ConquerorAura.exploded(player, level, end, 2.0);
        });
    }

    // ------------------------------------------------------------------------------------------------------ C: Parasite

    /** Threads into the one looked at: for {@link #PARASITE_TICKS} it is moved where the user looks, not by itself. */
    static void parasite(ServerPlayer player, State s) {
        if (!ready(player, ItoKit.PARASITE)) {
            return;
        }
        LivingEntity target = PikaMoves.aimed(player, PARASITE_RANGE, 0.08);
        if (target == null) {
            sound(player, chest(player), ItoSounds.SWISH, 0.6F, 1.4F); // the threads find no one
            return;
        }
        cool(player, ItoKit.PARASITE, 300);
        s.busyUntil = now(player) + 8;
        s.held = target;
        s.heldUntil = now(player) + PARASITE_TICKS;
        s.heldDistance = Math.max(3.0, Math.min(10.0, target.m_20182_().m_82554_(player.m_20182_())));
        animate(player, "hie_grab_reach");
        fx(player, PARASITE, target.m_19879_(), StyleCombat.middle(target), Vec3.f_82478_, 0, 0, 0, PARASITE_TICKS);
        sound(player, StyleCombat.middle(target), ItoSounds.PULL, 1.0F, 1.0F);
        lightning(player, StyleCombat.middle(target));
    }

    static void parasiteTick(ServerPlayer player, State s, long now) {
        LivingEntity body = s.held;
        if (now >= s.heldUntil || !body.m_6084_() || body.m_9236_() != player.m_9236_() || body.m_20182_().m_82554_(player.m_20182_()) > 48.0) {
            release(player, s);
            return;
        }
        Vec3 want = player.m_146892_().m_82549_(player.m_20154_().m_82490_(s.heldDistance)).m_82520_(0.0, -body.m_20206_() * 0.5, 0.0);
        Vec3 pull = want.m_82546_(body.m_20182_()).m_82490_(0.35);
        double l = pull.m_82553_();
        if (l > 1.0) {
            pull = pull.m_82490_(1.0 / l);
        }
        body.m_20256_(pull);
        body.f_19864_ = true;
        body.f_19789_ = 0.0F;
        if (body instanceof Mob mob) {
            mob.m_6710_(null);
            mob.m_21573_().m_26573_();
        }
    }

    static void release(ServerPlayer player, State s) {
        if (s.held != null) {
            fx(player, PARASITE, s.held.m_19879_(), StyleCombat.middle(s.held), Vec3.f_82478_, 0, 0, 0, 0);
        }
        s.held = null;
    }

    // ------------------------------------------------------------------------------------------------------ V: Spider's Web

    /** A web of string in front for {@link #WEB_TICKS}: shots stop in it, blows from the front are caught. */
    static void web(ServerPlayer player, State s) {
        if (!ready(player, ItoKit.WEB)) {
            return;
        }
        cool(player, ItoKit.WEB, 240);
        long now = now(player);
        s.busyUntil = now + 8;
        s.webUntil = now + WEB_TICKS;
        s.webFacing = flat(player);
        s.webAt = chest(player).m_82549_(s.webFacing.m_82490_(WEB_AHEAD));
        animate(player, "hie_ball_gather");
        fx(player, WEB, 0, s.webAt, s.webFacing, 0, 0, WEB_RADIUS, WEB_TICKS);
        sound(player, s.webAt, ItoSounds.WEB, 1.0F, 1.0F);
    }

    static void webTick(ServerPlayer player, State s, long now) {
        if (now >= s.webUntil) {
            webEnd(player, s);
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 c = s.webAt;
        Vec3 n = s.webFacing;
        for (Projectile shot : level.m_6443_(Projectile.class, UoMoves.box(c, c).m_82400_(WEB_RADIUS + 3.0), e -> e.m_6084_())) {
            if (shot.m_19749_() == player || s.caught.contains(shot)) {
                continue;
            }
            Vec3 d = shot.m_20182_().m_82546_(c);
            double depth = d.m_82526_(n);
            Vec3 flatPart = d.m_82546_(n.m_82490_(depth));
            if (depth > -0.4 && depth < 1.2 && flatPart.m_82553_() < WEB_RADIUS) {
                // caught: held fast in the strings (given back its fall when the web goes)
                shot.m_20256_(Vec3.f_82478_);
                shot.m_20242_(true);
                s.caught.add(shot);
                fx(player, HIT, ++seeds, shot.m_20182_(), n.m_82490_(-1.0), HIT_WEB, 0, 0.6, 0);
                sounds(player, shot.m_20182_(), ItoSounds.WEB, 0.7F, 1.3F, 1, 4);
            }
        }
        for (Projectile shot : s.caught) {
            if (shot.m_6084_()) {
                shot.m_20256_(Vec3.f_82478_);
            }
        }
    }

    static void webEnd(ServerPlayer player, State s) {
        if (s.webUntil < 0) {
            return;
        }
        s.webUntil = -1;
        for (Projectile shot : s.caught) {
            if (shot.m_6084_()) {
                shot.m_20242_(false);
            }
        }
        s.caught.clear();
        fx(player, WEB, 0, s.webAt != null ? s.webAt : player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
    }

    /** A blow from in front of a user behind the web is caught in it (true: stopped). */
    static boolean blocks(ServerPlayer player, DamageSource source) {
        State s = STATES.get(player.m_20148_());
        if (s == null || s.webUntil < 0 || s.webAt == null) {
            return false;
        }
        Entity from = source.m_7640_() != null ? source.m_7640_() : source.m_7639_();
        if (from == null || from == player) {
            return false;
        }
        Vec3 d = from.m_20182_().m_82546_(player.m_20182_());
        Vec3 f = new Vec3(d.f_82479_, 0, d.f_82481_);
        double l = f.m_82553_();
        if (l < 0.3 || f.m_82526_(s.webFacing) / l < 0.25) {
            return false;
        }
        fx(player, HIT, ++seeds, s.webAt, s.webFacing.m_82490_(-1.0), HIT_WEB, 0, 0.9, 0);
        sounds(player, s.webAt, ItoSounds.WEB, 0.8F, 1.2F, 1, 4);
        return true;
    }

    // ------------------------------------------------------------------------------------------------------ B: Black Knight

    /** A copy of the user woven of string fights beside them for {@link #KNIGHT_TICKS}. */
    static void blackKnight(ServerPlayer player, State s) {
        if (!ready(player, ItoKit.KNIGHT)) {
            return;
        }
        cool(player, ItoKit.KNIGHT, KNIGHT_TICKS + 100);
        long now = now(player);
        s.busyUntil = now + 10;
        s.knightUntil = now + KNIGHT_TICKS;
        Vec3 f = flat(player);
        Vec3 right = new Vec3(-f.f_82481_, 0, f.f_82479_);
        Vec3 at = player.m_20182_().m_82549_(right.m_82490_(1.3)).m_82549_(f.m_82490_(0.4));
        Vec3 g = ground((ServerLevel) player.m_9236_(), at, 1.5, 4.0);
        s.knight = g != null ? g : at;
        s.knightYaw = (float) Math.atan2(-f.f_82479_, f.f_82481_);
        s.knightNext = now + 10;
        animate(player, "hie_partisan_raise");
        knight(player, s, 0, now);
        sound(player, s.knight.m_82520_(0, 1, 0), ItoSounds.PULL, 1.0F, 0.8F);
    }

    static void knight(ServerPlayer player, State s, int action, long now) {
        fx(player, KNIGHT, 0, s.knight, new Vec3(s.knightYaw, action, 0), 0, 0, 0, (int) Math.max(0, s.knightUntil - now));
    }

    static void knightTick(ServerPlayer player, State s, long now) {
        if (now >= s.knightUntil || !player.m_6084_()) {
            s.knightUntil = -1;
            fx(player, KNIGHT, 0, s.knight, Vec3.f_82478_, 0, 0, 0, 0);
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity body : PikaMoves.foes(player, s.knight, KNIGHT_SEEK)) {
            double d = body.m_20182_().m_82557_(s.knight);
            if (d < best) {
                best = d;
                target = body;
            }
        }
        Vec3 goal;
        double stop;
        if (target != null) {
            goal = target.m_20182_();
            stop = KNIGHT_REACH * 0.8 + target.m_20205_() * 0.5;
        } else {
            Vec3 f = flat(player);
            goal = player.m_20182_().m_82549_(new Vec3(-f.f_82481_, 0, f.f_82479_).m_82490_(1.4)).m_82549_(f.m_82490_(-0.5));
            stop = 0.8;
        }
        Vec3 d = new Vec3(goal.f_82479_ - s.knight.f_82479_, 0, goal.f_82481_ - s.knight.f_82481_);
        double dist = d.m_82553_();
        int action = 0;
        if (dist > 24.0) {
            s.knight = goal; // left far behind: it catches up at once
        } else if (dist > stop) {
            double pace = target != null ? KNIGHT_SPEED : Math.min(0.6, Math.max(KNIGHT_SPEED, dist * 0.15));
            Vec3 step = d.m_82490_(Math.min(pace, dist - stop) / dist);
            Vec3 next = s.knight.m_82549_(step);
            Vec3 g = ground(level, next, 1.2, 3.0);
            s.knight = g != null ? g : next;
        }
        if (dist > 0.05) {
            s.knightYaw = (float) Math.atan2(-d.f_82479_, d.f_82481_);
        }
        if (target != null && dist <= KNIGHT_REACH + target.m_20205_() * 0.5 && now >= s.knightNext) {
            s.knightNext = now + KNIGHT_EVERY;
            s.knightCuts++;
            action = 1 + s.knightCuts % 2;
            Vec3 way = dist > 0.05 ? d.m_82490_(1.0 / dist) : flat(player);
            cut(player, target, 5.0F, way.m_82490_(0.4).m_82520_(0, 0.15, 0));
            fx(player, HIT, ++seeds, StyleCombat.middle(target), way, HIT_KNIGHT, 0, 1.0, 0);
            sounds(player, s.knight, ItoSounds.SWISH, 0.8F, 1.15F, 1, 4);
        }
        if (action != 0 || (now - s.knightUntil) % KNIGHT_SEND == 0) {
            knight(player, s, action, now);
        }
    }

    // ------------------------------------------------------------------------------------------------------ N: Birdcage

    /** The Birdcage (the owner: "40 blocks, 45 s"): a dome of strings round the user, closing in; touching it cuts; no one gets out. */
    static void birdcage(ServerPlayer player, State s) {
        if (!ready(player, ItoKit.CAGE)) {
            return;
        }
        cool(player, ItoKit.CAGE, CAGE_TICKS * 2);
        long now = now(player);
        s.busyUntil = now + 16;
        s.cageUntil = now + CAGE_TICKS;
        s.cageFrom = now;
        Vec3 g = ground((ServerLevel) player.m_9236_(), player.m_20182_(), 0.5, 6.0);
        s.cageAt = g != null ? g : player.m_20182_();
        s.cut.clear();
        animate(player, "magu_rise");
        fx(player, CAGE, 0, s.cageAt, Vec3.f_82478_, 0, CAGE_END, CAGE_RADIUS, CAGE_TICKS);
        sound(player, s.cageAt.m_82520_(0, 2, 0), ItoSounds.CAGE, 2.5F, 1.0F);
    }

    static void cageTick(ServerPlayer player, State s, long now) {
        if (now >= s.cageUntil) {
            cageEnd(player, s);
            return;
        }
        if ((now - s.cageFrom) % 2 != 0) {
            return;
        }
        double r = cageRadius(CAGE_RADIUS, CAGE_END, now - s.cageFrom, CAGE_TICKS);
        Vec3 c = s.cageAt;
        for (LivingEntity body : PikaMoves.foes(player, c, r + 2.0)) {
            Vec3 m = StyleCombat.middle(body);
            Vec3 d = m.m_82546_(c);
            // the dome: a hemisphere on the ground (below its middle, the wall goes straight down)
            Vec3 dd = d.f_82480_ < 0 ? new Vec3(d.f_82479_, 0, d.f_82481_) : d;
            double dist = dd.m_82553_();
            double gap = dist - r;
            if (Math.abs(gap) > 1.2 || dist < 1.0E-4) {
                continue;
            }
            Vec3 out = dd.m_82490_(1.0 / dist);
            // inside it is pushed back in, outside it is kept out
            Vec3 push = out.m_82490_(gap < 0 ? -0.5 : 0.5);
            body.m_20256_(new Vec3(push.f_82479_, Math.max(body.m_20184_().f_82480_, 0.05), push.f_82481_));
            body.f_19864_ = true;
            Long last = s.cut.get(body.m_19879_());
            if (last == null || now - last >= 10) {
                s.cut.put(body.m_19879_(), now);
                cut(player, body, 4.0F, Vec3.f_82478_);
                fx(player, HIT, ++seeds, c.m_82549_(out.m_82490_(r)), out, HIT_CAGE, 0, 0.8, 0);
            }
        }
    }

    static void cageEnd(ServerPlayer player, State s) {
        if (s.cageUntil < 0) {
            return;
        }
        s.cageUntil = -1;
        s.cut.clear();
        fx(player, CAGE, 0, s.cageAt != null ? s.cageAt : player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
    }

    // ------------------------------------------------------------------------------------------------------ M: Awakening

    /** Break White (waves of string rolling out over the ground), or with Armament God Thread (sixteen Haki spears at once). */
    static void awaken(ServerPlayer player, State s) {
        if (!ready(player, ItoKit.AWAKEN)) {
            return;
        }
        cool(player, ItoKit.AWAKEN, 600);
        s.busyUntil = now(player) + 14;
        if (coated(player)) {
            godThread(player);
        } else {
            breakWhite(player);
        }
    }

    static void breakWhite(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        animate(player, "hie_ice_age");
        Vec3 g = ground(level, player.m_20182_(), 0.5, 6.0);
        Vec3 c = g != null ? g : player.m_20182_();
        int seed = ++seeds;
        int life = WAVE_EVERY * (WAVES - 1) + (int) Math.ceil(WAVE_REACH / WAVE_SPEED) + 10;
        fx(player, WAVE, seed, c, Vec3.f_82478_, 0, 0, WAVE_REACH, life);
        sound(player, c, ItoSounds.WAVE, 2.0F, 0.9F);
        LIVE.add(new Waves(player, level, c, now(player), life));
    }

    /** Break White's waves: each rolling outward, striking whoever its front passes once. */
    static final class Waves implements UoMoves.Live {
        final ServerPlayer user;
        final ServerLevel level;
        final Vec3 at;
        final long born;
        final int life;
        final List<Set<Integer>> struck = new ArrayList<>();

        Waves(ServerPlayer user, ServerLevel level, Vec3 at, long born, int life) {
            this.user = user;
            this.level = level;
            this.at = at;
            this.born = born;
            this.life = life;
            for (int k = 0; k < WAVES; k++) {
                struck.add(new HashSet<>());
            }
        }

        @Override
        public ServerLevel level() {
            return level;
        }

        @Override
        public boolean tick(long now) {
            double age = now - born;
            if (age > life || !user.m_6084_()) {
                return true;
            }
            List<LivingEntity> near = PikaMoves.foes(user, at, Math.min(WAVE_REACH, age * WAVE_SPEED) + 1.5);
            for (int k = 0; k < WAVES; k++) {
                double r = (age - k * WAVE_EVERY) * WAVE_SPEED;
                if (r <= 0.0 || r > WAVE_REACH) {
                    continue;
                }
                for (LivingEntity body : near) {
                    Vec3 d = body.m_20182_().m_82546_(at);
                    double dist = Math.hypot(d.f_82479_, d.f_82481_);
                    if (dist > r - 1.2 && dist < r + 0.6 && Math.abs(d.f_82480_) < 4.0 && struck.get(k).add(body.m_19879_())) {
                        Vec3 out = dist < 1.0E-4 ? Vec3.f_82478_ : new Vec3(d.f_82479_ / dist, 0, d.f_82481_ / dist);
                        cut(user, body, 7.0F, out.m_82490_(0.6).m_82520_(0, 0.8, 0));
                    }
                }
            }
            return false;
        }
    }

    static void godThread(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        animate(player, "hie_ball_throw");
        LivingEntity aimed = PikaMoves.aimed(player, THREAD_RANGE, 0.08);
        Vec3 target = aimed != null ? StyleCombat.middle(aimed) : looked(player, THREAD_RANGE);
        Vec3 from = chest(player);
        int seed = ++seeds;
        fx(player, THREAD, seed, target, from, 0, 0, 0, THREAD_FLIGHT);
        sound(player, from, ItoSounds.WAVE, 1.6F, 1.25F);
        later(player, THREAD_FLIGHT, () -> {
            UoMoves.explode(player, level, target, target.m_82546_(from).m_82541_(), 3.5, 14.0F, 0, 1.0);
            fx(player, HIT, ++seeds, target, target.m_82546_(from).m_82541_(), HIT_STRING, 2, 2.2, 0);
            sound(player, target, ItoSounds.SLICE, 1.6F, 0.7F);
            Impact.quake(level, target, 20.0, 0.4F, 8);
        });
    }

    // ------------------------------------------------------------------------------------------------------ the passives

    /** Sora no Michi: a step up on strings in the air ({@link #SKY_STEPS} before landing); the fall so far forgotten. */
    static void sky(ServerPlayer player, State s) {
        if (player.m_20096_() || player.m_150110_().f_35935_ || player.m_20069_() || player.m_20159_() || s.steps >= SKY_STEPS) {
            return;
        }
        s.steps++;
        Vec3 v = player.m_20184_();
        player.m_20256_(new Vec3(v.f_82479_ * 1.05, SKY_LIFT, v.f_82481_ * 1.05));
        player.f_19864_ = true;
        player.f_19789_ = 0.0F;
        fx(player, SKY, 0, player.m_20182_(), Vec3.f_82478_, s.steps, 0, 0, 0);
        sound(player, player.m_20182_(), ItoSounds.STEP, 0.7F, 1.0F + s.steps * 0.08F);
    }

    /** Self-Surgery: below a third of the health, once a minute, strings stitch the wounds (healing a few seconds). */
    static void surgery(ServerPlayer player, State s, long now) {
        if (now < s.surgeryNext || player.m_21223_() >= player.m_21233_() * SURGERY_BELOW || !player.m_6084_()) {
            return;
        }
        s.surgeryNext = now + SURGERY_EVERY;
        player.m_7292_(new MobEffectInstance(MobEffects.f_19605_, SURGERY_TICKS, 1, false, false, true));
        fx(player, STITCH, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 60);
        sound(player, chest(player), ItoSounds.PULL, 0.8F, 1.3F);
    }

    // ------------------------------------------------------------------------------------------------------ the ticks

    /** Each tick of a player with the kit: the burst, Parasite, the web, the knight, the cage, the sky steps, Self-Surgery. */
    static void tick(ServerPlayer player) {
        State s = state(player);
        long now = now(player);
        if (s.useSince >= 0 && now - s.useSince >= TAP && s.sent < BURST_MOST && now >= s.nextBullet) {
            if (s.sent == 1) {
                animate(player, "pika_laser_charge");
            }
            s.nextBullet = now + BURST_EVERY;
            shoot(player, s);
        }
        if (s.held != null) {
            parasiteTick(player, s, now);
        }
        if (s.webUntil >= 0) {
            webTick(player, s, now);
        }
        if (s.knightUntil >= 0) {
            knightTick(player, s, now);
        }
        if (s.cageUntil >= 0) {
            cageTick(player, s, now);
        }
        if (s.steps > 0 && player.m_20096_()) {
            s.steps = 0;
        }
        surgery(player, s, now);
    }

    static void forget(ServerPlayer player) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        release(player, s);
        webEnd(player, s);
        if (s.knightUntil >= 0) {
            s.knightUntil = -1;
            fx(player, KNIGHT, 0, s.knight != null ? s.knight : player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
        }
        cageEnd(player, s);
        STATES.remove(player.m_20148_());
    }

    /** The server stopping (before the save): every caught shot given back its fall, everything dropped. */
    static void clear() {
        for (State s : STATES.values()) {
            for (Projectile shot : s.caught) {
                if (shot.m_6084_()) {
                    shot.m_20242_(false);
                }
            }
            s.caught.clear();
        }
        STATES.clear();
        LIVE.clear();
        HEARD.clear();
        BOLTS.clear();
    }

    /** The world's tick: the bullets in flight, the waves and everything timed. */
    @SubscribeEvent
    public static void itoTick(TickEvent.LevelTickEvent event) {
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
