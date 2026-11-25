package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.fx.Impact;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.ito.ItoAwaken;
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
import net.minecraft.world.entity.player.Abilities;
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
    /**
     * Goshikito (3.4.2): id its seed, user, x, y, z the ground at the user's feet, a the way the strip runs (flat), size its length,
     * ticks how long it shows: five strings hang from the raised hand, then rake down over the strip on {@link #GOSHIKI_RAKE}.
     */
    public static final int GOSHIKI = 243;
    /**
     * Overheat (3.4.2): id its seed, user; bx {@link #HEAT_CHARGE} (the rope winding in front of the fist; ticks the longest hold),
     * {@link #HEAT_FIRE} (x, y, z where it leaves the fist, a its step a tick, by the charge 0 to 1, size how far it flies before it
     * explodes, ticks its flight) or {@link #HEAT_DROP} (the charge let go of with nothing fired).
     */
    public static final int OVERHEAT = 244;
    public static final int HEAT_CHARGE = 0;
    public static final int HEAT_FIRE = 1;
    public static final int HEAT_DROP = 2;
    /** Parasite: id the one held, user, ticks how long (0: let go). */
    public static final int PARASITE = 245;
    /** Spider's Web: user, x, y, z its middle, a its facing (out, away from its user), size its radius, ticks how long (0: down). */
    public static final int WEB = 246;
    /**
     * 3.4.2: strings bursting out in every direction: id its seed, x, y, z where, a the way it came (or zero), bx what (SCATTER_*), size
     * how far they fly. The Black Knight's burst (it is a real body now: ito/StringKnight) and Overheat's explosion.
     */
    public static final int SCATTER = 247;
    public static final int SCATTER_WHITE = 0;
    public static final int SCATTER_HEAT = 1;
    /**
     * The Birdcage (3.4.2): user; bx {@link #CAGE_CHARGING} (the stream of strings poured up out of the hand, growing the cage; ticks
     * the longest hold), {@link #CAGE_STANDING} (x, y, z its middle on the ground, size its radius, ticks how long it stands) or
     * {@link #CAGE_CLOSING} (as standing, ticks how long it takes to shut); ticks 0: down.
     */
    public static final int CAGE = 248;
    public static final int CAGE_STANDING = 0;
    public static final int CAGE_CLOSING = 1;
    public static final int CAGE_CHARGING = 2;
    /**
     * 3.5.0: the Awakening's string ground (ito/ItoAwaken): id its seed, user, x, y, z its middle, size its radius, ticks how long (0:
     * gone); bx 1: the Haki Awakening's, round the user's feet wherever they go, while it is on.
     */
    public static final int FIELD = 249;
    /** 3.5.0: a tentacle of string (the Haki Awakening): id the body it takes, user, x, y, z its root, bx 1 out of a tree, ticks its life. */
    public static final int TENTACLE = 250;
    /**
     * Sky Road (3.4.3, the owner: "Ito Ito No mi double jump tap now has a new move: Sky Road movement ability"; was 3.3.7's three air
     * steps): user; bx 1 on (ticks how long at the most, x, y, z the feet), 0 off. The runner's own client moves it (client/ito/SkyRoad),
     * every client draws its strings and poses it.
     */
    public static final int SKY = 251;
    /** Self-Surgery: user, ticks how long the stitching shows. */
    public static final int STITCH = 252;
    /** 3.5.0: a spike of string shooting up out of the string ground: id its seed, x, y, z its foot, size its height, ticks its life. */
    public static final int SPIKE = 253;
    /**
     * 3.5.2: Sky Road's string or step (the runner's own client throws it: network/C2SItoSky): user; id ROPE_SWING or ROPE_HANG (x, y, z the
     * anchor, by the string's length), SKY_FLY (no string held: bx the trick, SKY_LET_GO, SKY_LEAP, SKY_DROP) or SKY_STEP (x, y, z the
     * step, by the way it faces, bx the foot). Its own client already has it.
     */
    public static final int SKY_STRING = 254;
    public static final int ROPE_SWING = 1;
    public static final int ROPE_HANG = 2;
    public static final int SKY_FLY = 3;
    public static final int SKY_STEP = 4;
    public static final int SKY_LET_GO = 0;
    public static final int SKY_LEAP = 1;
    public static final int SKY_DROP = 2;
    public static final int SKY_BACKFLIP = 3;
    /** How far from the runner a string's anchor or a step may be; at most this many a runner a tick (anything past it is dropped). */
    static final double SKY_REACH = 48.0;
    static final int SKY_PER_TICK = 2;

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
    /**
     * Goshikito (3.4.2, the owner: "Z Often doesnt work and looks bad overall (redo entirely)": it struck where the look met a block,
     * behind whoever was aimed at): the strip the five strings rake (from the feet along the way, centred on whoever is aimed at), its
     * length, the strings' spacing, its half width; the tick of ito_goshiki they land on; how long the cuts show; the damage.
     */
    public static final double GOSHIKI_LENGTH = 22.0;
    public static final double GOSHIKI_SPREAD = 0.9;
    static final double GOSHIKI_WIDTH = 2.4;
    public static final int GOSHIKI_RAKE = 5;
    public static final int GOSHIKI_LIFE = 44;
    static final float GOSHIKI_DAMAGE = 12.0F;
    /**
     * Overheat (3.4.2, the owner: "Should be a charge beam that DOES NOT COME BACK (explodes on landing) when it ends the strings scatter
     * into every direction (The charging should be one arm pointed forward and the strings gathering infront of the fist"): full in
     * {@link #HEAT_FULL} ticks, let go by itself at {@link #HEAT_MOST}; the beam's speed, its reach at no charge and at full, the blast.
     */
    public static final int HEAT_FULL = 40;
    public static final int HEAT_MOST = 96;
    public static final double HEAT_SPEED = 5.0;
    public static final double HEAT_REACH_LEAST = 18.0;
    public static final double HEAT_REACH_MOST = 40.0;
    /** Parasite: how far it reaches, how long it holds, how near and far the held body is kept. */
    static final double PARASITE_RANGE = 24.0;
    public static final int PARASITE_TICKS = 100;
    /** Spider's Web: how far ahead, its radius, how long. */
    static final double WEB_AHEAD = 2.2;
    public static final double WEB_RADIUS = 2.4;
    public static final int WEB_TICKS = 120;
    /**
     * Black Knight (3.4.2, the owner: "it should be a clone that roams on it's own / follows the player, when killed it explodes into
     * tons of strings, if the key is recast while it's alive it explodes into strings too"): a real body now (ito/StringKnight): how
     * long it lasts, its reach, a cut every so many ticks, how far it looks for foes, how far it strays from its weaver before it walks
     * back, the burst's reach and damage, the rest after it bursts.
     */
    public static final int KNIGHT_TICKS = 600;
    static final double KNIGHT_REACH = 2.2;
    static final int KNIGHT_EVERY = 14;
    static final double KNIGHT_SEEK = 16.0;
    static final double KNIGHT_STRAY = 6.0;
    public static final double KNIGHT_BURST = 4.5;
    static final float KNIGHT_BURST_DAMAGE = 8.0F;
    static final int KNIGHT_REST = 200;
    /**
     * The Birdcage (the owner: "40 blocks, 45 s"; 3.4.2: "Bird cage should be also a charge, the user points at the sky and shoots a
     * steady stream of strings that spread into the cage when it reaches the right height. (the longer you hold it the bigger it gets
     * (max is the current size), when recasting it, the bird cage slowly closes, cutting down anything caught in the cage, all entities
     * are pulled towards the center of the cage and when the cage fully closes, they take damage + bleed"): the smallest and largest
     * radius, the hold that reaches the largest, the longest hold, how long it stands, how long it takes to shut, the shut cage's blow.
     */
    public static final double CAGE_LEAST = 8.0;
    public static final double CAGE_RADIUS = 40.0;
    public static final int CAGE_FULL = 80;
    public static final int CAGE_HOLD_MOST = 96;
    public static final int CAGE_TICKS = 900;
    /** 3.5.0 (the owner: "the birdcage should close way slower ... it should come slowly, very slowly back. Like I mean, very slow"):
     *  30 s to shut (was 5 s). */
    public static final int CAGE_CLOSE = 600;
    public static final double CAGE_SHUT = 1.2;
    static final float CAGE_DAMAGE = 18.0F;
    /** Break White: three waves, born so many ticks apart, rolling so fast, so far. */
    /**
     * Sky Road: no time limit (3.8.0, the owner: "not stop swinging": it ended after 30 seconds, wherever the runner was): it lasts until
     * the ground (SKY_MOST only keeps the clients' count of it far away); the rest after it, and how long on the ground ends it.
     */
    public static final int SKY_MOST = Integer.MAX_VALUE / 4;
    static final int SKY_REST = 30;
    static final int SKY_GROUNDED = 3;
    /** Self-Surgery: below this share of the health, once in so many ticks, healing so long. */
    static final double SURGERY_BELOW = 1.0 / 3.0;
    static final int SURGERY_EVERY = 1200;
    static final int SURGERY_TICKS = 120;


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
        long heatSince = -1;
        int heatSeed;
        com.hexhaki.piece.ito.StringKnight knight;
        /** 3.5.0: the Awakening: the string ground's end and middle (no Haki), the bodies it spiked lately; the Haki toggle, the next
         *  tentacle's earliest tick, the bodies taken lately. */
        long fieldUntil = -1;
        Vec3 fieldAt;
        int fieldSeed;
        final Map<Integer, Long> spiked = new HashMap<>();
        boolean awakened;
        long awakenedUntil;
        long nextTentacle;
        final Map<Integer, Long> taken = new HashMap<>();
        int tentacles;
        long cageSince = -1;
        long cageUntil = -1;
        long cageFrom;
        Vec3 cageAt;
        double cageR;
        long closeFrom = -1;
        final Map<Integer, Long> cut = new HashMap<>();
        long skyUntil = -1;
        long skyNext;
        int skyGrounded;
        boolean skyMayfly;
        boolean skyFall;
        long skyHeardAt;
        int skyHeard;
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
        fxAs(player, player.m_19879_(), kind, id, at, a, bx, by, size, ticks);
    }

    /** As {@link #fx}, its user another body (the Black Knight's own lashes). */
    static void fxAs(ServerPlayer player, int user, int kind, int id, Vec3 at, Vec3 a, double bx, double by, double size, int ticks) {
        PieceNetwork.around(player, new S2CUoFx(kind, id, user, at.f_82479_, at.f_82480_, at.f_82481_, (float) a.f_82479_,
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
        return s.useSince >= 0 || s.heatSince >= 0 || s.cageSince >= 0 || now < s.busyUntil;
    }

    /** The user's foes near {@code at}, never their own Black Knight. */
    static List<LivingEntity> foes(ServerPlayer player, Vec3 at, double radius) {
        List<LivingEntity> out = PikaMoves.foes(player, at, radius);
        out.removeIf(body -> body instanceof com.hexhaki.piece.ito.StringKnight knight && knight.weaver() == player);
        return out;
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
            cageClose(player, s); // the second press: the cage closes (3.4.2)
            return;
        }
        if (slot == ItoKit.KNIGHT.slot() && s.knight != null && s.knight.m_6084_()) {
            knightBurst(player, s.knight); // the second press: the knight bursts into strings (3.4.2)
            return;
        }
        if (busy(s, now) || player.m_20159_()) {
            return;
        }
        if (slot == N_SLOT) {
            cageCharge(player, s);
        } else if (slot == M_SLOT) {
            awaken(player, s);
        } else if (slot == ItoKit.GOSHIKI.slot()) {
            goshikito(player, s);
        } else if (slot == ItoKit.OVERHEAT.slot()) {
            heatCharge(player, s);
        } else if (slot == ItoKit.PARASITE.slot()) {
            parasite(player, s);
        } else if (slot == ItoKit.WEB.slot()) {
            web(player, s);
        } else if (slot == ItoKit.KNIGHT.slot()) {
            blackKnight(player, s);
        }
    }

    /** 3.4.2: a held key let go (Overheat on X, the Birdcage on N). */
    static void pressUp(ServerPlayer player, int slot) {
        State s = STATES.get(player.m_20148_());
        if (s == null) {
            return;
        }
        if (slot == ItoKit.OVERHEAT.slot() && s.heatSince >= 0) {
            heatFire(player, s);
        } else if (slot == N_SLOT && s.cageSince >= 0) {
            cageCast(player, s);
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
        // 3.4.2 (the owner: "I dont like how M1 looks animationwise"): the String fruit's own whip of the hand (tools/ito_anims.py),
        // forehand (the strings swept right to left: ItoArt.lash which 0) then backhand
        animate(player, which == 0 ? "ito_slash_1" : "ito_slash_2");
        sound(player, chest(player), ItoSounds.SWISH, 0.8F, 1.1F + which * 0.1F);
        later(player, 2, () -> {
            Vec3 f = flat(player);
            fx(player, SLASH, which, chest(player), f, which, 0, SLASH_REACH, 0);
            for (LivingEntity body : ahead(player, SLASH_REACH, SLASH_ARC)) {
                if (body instanceof com.hexhaki.piece.ito.StringKnight knight && knight.weaver() == player) {
                    continue;
                }
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

    /**
     * Goshikito (3.4.2, redone): the hand raised high with five strings hanging from its fingers (ito_goshiki), then raked down over the
     * strip in front of the user, {@link #GOSHIKI_LENGTH} long and {@link #GOSHIKI_WIDTH} each side, centred on whoever is aimed at:
     * everyone on it cut (and their mobs turned on the user) on {@link #GOSHIKI_RAKE}; the ground cut in five lines; the Conqueror's
     * lightning where the strings strike. It never misses for want of a block under the crosshair (3.3.7's did).
     */
    static void goshikito(ServerPlayer player, State s) {
        if (!ready(player, ItoKit.GOSHIKI)) {
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 way = flat(player);
        LivingEntity aimed = PikaMoves.aimed(player, GOSHIKI_LENGTH + 2.0, 0.15);
        if (aimed != null && !(aimed instanceof com.hexhaki.piece.ito.StringKnight)) {
            Vec3 d = aimed.m_20182_().m_82546_(player.m_20182_());
            Vec3 f = new Vec3(d.f_82479_, 0.0, d.f_82481_);
            if (f.m_82553_() > 1.0) {
                way = f.m_82541_();
            }
        }
        Vec3 g = ground(level, player.m_20182_(), 0.5, 4.0);
        Vec3 start = g != null ? g : player.m_20182_();
        cool(player, ItoKit.GOSHIKI, 160);
        s.busyUntil = now(player) + 12;
        animate(player, "ito_goshiki");
        int seed = ++seeds;
        fx(player, GOSHIKI, seed, start, way, 0, 0, GOSHIKI_LENGTH, GOSHIKI_LIFE);
        sound(player, chest(player), ItoSounds.SWISH, 1.0F, 0.8F);
        Vec3 along = way;
        later(player, GOSHIKI_RAKE, () -> rake(player, level, start, along));
    }

    /** The five strings landing: everyone on the strip cut, the ground's lightning, a quake. */
    static void rake(ServerPlayer player, ServerLevel level, Vec3 start, Vec3 way) {
        Vec3 side = new Vec3(-way.f_82481_, 0, way.f_82479_);
        Vec3 middle = start.m_82549_(way.m_82490_(GOSHIKI_LENGTH * 0.5));
        for (LivingEntity body : foes(player, middle, GOSHIKI_LENGTH * 0.5 + GOSHIKI_WIDTH + 1.0)) {
            Vec3 d = body.m_20182_().m_82546_(start);
            double on = d.f_82479_ * way.f_82479_ + d.f_82481_ * way.f_82481_;
            double across = d.f_82479_ * side.f_82479_ + d.f_82481_ * side.f_82481_;
            double half = body.m_20205_() * 0.5;
            if (on >= 0.3 - half && on <= GOSHIKI_LENGTH + half && Math.abs(across) <= GOSHIKI_WIDTH + half && d.f_82480_ > -4.0
                && d.f_82480_ < 10.0) {
                cut(player, body, GOSHIKI_DAMAGE, way.m_82490_(0.25).m_82520_(0.0, -0.3, 0.0));
            }
        }
        sound(player, middle, ItoSounds.SLICE, 1.6F, 0.75F);
        sound(player, start.m_82549_(way.m_82490_(3.0)), ItoSounds.SWISH, 1.2F, 0.6F);
        ConquerorAura.exploded(player, level, middle.m_82520_(0, 0.5, 0), 4.0);
        Impact.quake(level, middle, 20.0, 0.35F, 6);
    }

    // ------------------------------------------------------------------------------------------------------ X: Overheat

    /** Overheat held: one arm pointed ahead (ito_heat_charge) while the red-hot rope winds itself in front of the fist. */
    static void heatCharge(ServerPlayer player, State s) {
        if (!ready(player, ItoKit.OVERHEAT)) {
            return;
        }
        s.heatSince = now(player);
        s.heatSeed = ++seeds;
        animate(player, "ito_heat_charge");
        fx(player, OVERHEAT, s.heatSeed, chest(player), player.m_20154_(), HEAT_CHARGE, 0, 0, HEAT_MOST);
        sound(player, chest(player), ItoSounds.BURN, 0.7F, 0.8F);
    }

    /** How full Overheat's charge is after {@code held} ticks (a tap still fires a short one). */
    public static double heatCharge(double held) {
        return Math.max(0.15, Math.min(1.0, held / HEAT_FULL));
    }

    /**
     * Overheat let go: the beam fired along the look from the fist, {@link #HEAT_SPEED} a tick, never drawn back: it explodes where it
     * lands (the first body or block in its way, or the end of its reach), the blast burning and throwing, its strings scattering every
     * way; the Conqueror's lightning out of the blast (UoMoves.explode).
     */
    static void heatFire(ServerPlayer player, State s) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        long now = now(player);
        double c = heatCharge(now - s.heatSince);
        int seed = s.heatSeed;
        s.heatSince = -1;
        cool(player, ItoKit.OVERHEAT, (int) (100 + 80 * c));
        s.busyUntil = now + 8;
        animate(player, "ito_heat_fire");
        Vec3 look = player.m_20154_();
        Vec3 from = chest(player).m_82549_(look.m_82490_(0.8));
        double reach = HEAT_REACH_LEAST + (HEAT_REACH_MOST - HEAT_REACH_LEAST) * c;
        Vec3 to = from.m_82549_(look.m_82490_(reach));
        BlockHitResult block = level.m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 end = block.m_6662_() == HitResult.Type.BLOCK ? block.m_82450_() : to;
        double best = from.m_82554_(end);
        for (LivingEntity body : PikaMoves.crossed(player, from, end, 0.8)) {
            if (body instanceof com.hexhaki.piece.ito.StringKnight knight && knight.weaver() == player) {
                continue;
            }
            Vec3 m = StyleCombat.middle(body);
            double d = m.m_82546_(from).m_82526_(look);
            if (d > 0 && d < best) {
                best = d;
                end = from.m_82549_(look.m_82490_(d));
            }
        }
        double length = from.m_82554_(end);
        int flight = Math.max(1, (int) Math.ceil(length / HEAT_SPEED));
        fx(player, OVERHEAT, seed, from, look.m_82490_(HEAT_SPEED), HEAT_FIRE, c, length, flight);
        sound(player, from, ItoSounds.BURN, 1.4F, 1.1F - 0.2F * (float) c);
        Vec3 at = end;
        later(player, flight, () -> {
            double radius = 2.2 + 2.6 * c;
            UoMoves.explode(player, level, at, look, radius, (float) (8.0 + 10.0 * c), (int) (3 + 4 * c), 1.0 + 0.8 * c);
            fx(player, SCATTER, seed, at, look, SCATTER_HEAT, 0, radius * 1.6, 0);
            sound(player, at, ItoSounds.BURN, 2.0F, 0.7F);
            sound(player, at, ItoSounds.SLICE, 1.4F, 0.6F);
            Impact.quake(level, at, 18.0 + 10.0 * c, 0.3F + 0.2F * (float) c, 8);
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

    /**
     * Black Knight (3.4.2): a clone of the user woven of string, a real body ({@link com.hexhaki.piece.ito.StringKnight}, the user's own
     * skin on every client): it follows its weaver, roams round them, fights their foes, can be struck and killed; killed, pressed again
     * or out of time it bursts into strings ({@link #knightBurst}).
     */
    static void blackKnight(ServerPlayer player, State s) {
        if (!ready(player, ItoKit.KNIGHT)) {
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        com.hexhaki.piece.ito.StringKnight knight = com.hexhaki.piece.PieceEntities.KNIGHT.get().m_20615_(level);
        if (knight == null) {
            return;
        }
        cool(player, ItoKit.KNIGHT, KNIGHT_TICKS + KNIGHT_REST);
        long now = now(player);
        s.busyUntil = now + 10;
        Vec3 f = flat(player);
        Vec3 right = new Vec3(-f.f_82481_, 0, f.f_82479_);
        Vec3 at = player.m_20182_().m_82549_(right.m_82490_(1.4)).m_82549_(f.m_82490_(0.6));
        Vec3 g = ground(level, at, 1.5, 4.0);
        Vec3 feet = g != null ? g : player.m_20182_();
        knight.m_7678_(feet.f_82479_, feet.f_82480_, feet.f_82481_, player.m_146908_(), 0.0F);
        knight.weave(player, coated(player), now + KNIGHT_TICKS);
        level.m_7967_(knight);
        s.knight = knight;
        animate(player, "ito_knight");
        fx(player, SCATTER, ++seeds, feet.m_82520_(0, 1.0, 0), Vec3.f_82478_, SCATTER_WHITE, 1, 1.4, 0);
        sound(player, feet.m_82520_(0, 1, 0), ItoSounds.PULL, 1.0F, 0.8F);
    }

    /**
     * Each tick of a knight on the server (StringKnight's own AI step): its weaver gone (a reload, the fruit set aside) it unravels at
     * once; out of time it bursts; else it fights the nearest foe of its weaver within {@link #KNIGHT_SEEK}, or follows its weaver and
     * roams round them.
     */
    public static void knightTick(com.hexhaki.piece.ito.StringKnight knight, ServerLevel level) {
        ServerPlayer owner = knight.weaver();
        State s = owner == null ? null : STATES.get(owner.m_20148_());
        if (owner == null || s == null || s.knight != knight || !owner.m_6084_() || owner.m_9236_() != level) {
            knight.m_146870_();
            return;
        }
        long now = level.m_46467_();
        if (now >= knight.until()) {
            knightBurst(owner, knight);
            return;
        }
        LivingEntity foe = knight.foe();
        if (foe != null && (!foe.m_6084_() || foe.m_9236_() != level || foe.m_20280_(knight) > 24 * 24 || foe == owner)) {
            foe = null;
        }
        if (foe == null || now % 10 == 0) {
            LivingEntity nearest = null;
            double best = Double.MAX_VALUE;
            for (LivingEntity body : foes(owner, knight.m_20182_(), KNIGHT_SEEK)) {
                double d = body.m_20280_(knight);
                if (body != knight && d < best) {
                    best = d;
                    nearest = body;
                }
            }
            if (nearest != null && (foe == null || best < foe.m_20280_(knight) * 0.6)) {
                foe = nearest;
            }
        }
        knight.foe(foe);
        double fromOwner = Math.sqrt(knight.m_20280_(owner));
        if (fromOwner > 28.0) {
            // left far behind: woven again beside its weaver
            Vec3 f = flat(owner);
            Vec3 at = owner.m_20182_().m_82549_(new Vec3(-f.f_82481_, 0, f.f_82479_).m_82490_(1.4));
            knight.m_6021_(at.f_82479_, at.f_82480_, at.f_82481_);
            knight.m_21573_().m_26573_();
            return;
        }
        if (foe != null) {
            knight.m_21563_().m_24960_(foe, 30.0F, 30.0F);
            double d = Math.sqrt(foe.m_20280_(knight));
            if (d > KNIGHT_REACH + foe.m_20205_() * 0.5) {
                if (now % 5 == 0) {
                    knight.m_21573_().m_5624_(foe, 1.35);
                }
                return;
            }
            knight.m_21573_().m_26573_();
            knight.face(foe);
            if (now >= knight.nextCut()) {
                knightCut(owner, knight, foe, now);
            }
            return;
        }
        if (fromOwner > KNIGHT_STRAY) {
            if (now % 10 == 0) {
                knight.m_21573_().m_5624_(owner, 1.25); // follows its weaver
            }
        } else if (now >= knight.nextWander() && knight.m_21573_().m_26571_()) {
            // roams round its weaver
            knight.nextWander(now + 50 + knight.m_217043_().m_188503_(70));
            double angle = knight.m_217043_().m_188500_() * Math.PI * 2;
            double reach = 2.0 + knight.m_217043_().m_188500_() * 3.5;
            knight.m_21573_().m_26519_(owner.m_20185_() + Math.cos(angle) * reach, owner.m_20186_(), owner.m_20189_() + Math.sin(angle) * reach, 0.9);
        }
    }

    /** The knight's lash: the same strings as its weaver's String Slash, out of its own hand; the mob it cuts turns on it. */
    static void knightCut(ServerPlayer owner, com.hexhaki.piece.ito.StringKnight knight, LivingEntity foe, long now) {
        int which = knight.cut(now, KNIGHT_EVERY);
        Vec3 way = foe.m_20182_().m_82546_(knight.m_20182_());
        way = new Vec3(way.f_82479_, 0, way.f_82481_);
        way = way.m_82556_() < 1.0E-6 ? flat(owner) : way.m_82541_();
        fxAs(owner, knight.m_19879_(), SLASH, which, knight.m_146892_().m_82520_(0.0, -0.4, 0.0), way, which, 0, SLASH_REACH, 0);
        Vec3 side = new Vec3(-way.f_82481_, 0, way.f_82479_).m_82490_(which == 0 ? 0.15 : -0.15);
        cut(owner, foe, 5.0F, way.m_82490_(0.35).m_82549_(side).m_82520_(0, 0.12, 0));
        if (foe instanceof Mob mob && mob.m_6084_()) {
            mob.m_6710_(knight); // it fights back: the knight can be killed
        }
        sounds(owner, knight.m_20182_(), ItoSounds.SWISH, 0.8F, 1.15F, 1, 4);
    }

    /**
     * The knight bursts into strings (killed, its key pressed again, or out of time): white strings fly out every way, cutting
     * everyone of its weaver's foes within {@link #KNIGHT_BURST}; the Conqueror's lightning out of the burst; then it is gone and the
     * move rests {@link #KNIGHT_REST} ticks.
     */
    public static void knightBurst(ServerPlayer owner, com.hexhaki.piece.ito.StringKnight knight) {
        if (knight.burst()) {
            return;
        }
        ServerLevel level = (ServerLevel) knight.m_9236_();
        Vec3 at = StyleCombat.middle(knight);
        if (owner != null) {
            fx(owner, SCATTER, ++seeds, at, Vec3.f_82478_, SCATTER_WHITE, 0, KNIGHT_BURST * 1.4, 0);
            for (LivingEntity body : foes(owner, at, KNIGHT_BURST)) {
                if (body == knight || body.m_20280_(knight) > KNIGHT_BURST * KNIGHT_BURST) {
                    continue;
                }
                Vec3 out = StyleCombat.middle(body).m_82546_(at);
                out = out.m_82556_() < 1.0E-6 ? Vec3.f_82478_ : out.m_82541_();
                cut(owner, body, KNIGHT_BURST_DAMAGE, out.m_82490_(0.7).m_82520_(0, 0.35, 0));
            }
            ConquerorAura.exploded(owner, level, at, KNIGHT_BURST);
            sound(owner, at, ItoSounds.SLICE, 1.5F, 0.85F);
            sound(owner, at, ItoSounds.PULL, 1.2F, 0.6F);
            State s = STATES.get(owner.m_20148_());
            if (s != null && s.knight == knight) {
                s.knight = null;
                FruitData.cooldown(owner, ItoKit.KNIGHT.cooldown(), KNIGHT_REST);
                FruitServer.sync(owner);
            }
        }
        knight.m_146870_();
    }

    // ------------------------------------------------------------------------------------------------------ N: Birdcage

    /** The Birdcage's radius after {@code held} ticks of the stream (the owner: "the longer you hold it the bigger it gets"). */
    public static double cageSize(double held) {
        double t = Math.max(0.0, Math.min(1.0, held / CAGE_FULL));
        return CAGE_LEAST + (CAGE_RADIUS - CAGE_LEAST) * t;
    }

    /** N held: the arm pointed at the sky (ito_cage_charge), a steady stream of strings pouring up out of the hand. */
    static void cageCharge(ServerPlayer player, State s) {
        if (!ready(player, ItoKit.CAGE)) {
            return;
        }
        s.cageSince = now(player);
        animate(player, "ito_cage_charge");
        fx(player, CAGE, 0, player.m_20182_(), Vec3.f_82478_, CAGE_CHARGING, 0, 0, CAGE_HOLD_MOST);
        sound(player, chest(player), ItoSounds.CAGE, 1.2F, 1.2F);
    }

    /** N let go (or held its longest): the strings spread down from the height they reached into the cage, round the user. */
    static void cageCast(ServerPlayer player, State s) {
        long now = now(player);
        double radius = cageSize(now - s.cageSince);
        s.cageSince = -1;
        cool(player, ItoKit.CAGE, CAGE_TICKS + 300);
        s.busyUntil = now + 10;
        s.cageUntil = now + CAGE_TICKS;
        s.cageFrom = now;
        s.closeFrom = -1;
        s.cageR = radius;
        Vec3 g = ground((ServerLevel) player.m_9236_(), player.m_20182_(), 0.5, 6.0);
        s.cageAt = g != null ? g : player.m_20182_();
        s.cut.clear();
        animate(player, "ito_cage_cast");
        fx(player, CAGE, 0, s.cageAt, Vec3.f_82478_, CAGE_STANDING, 0, radius, CAGE_TICKS);
        sound(player, s.cageAt.m_82520_(0, 2, 0), ItoSounds.CAGE, 2.5F, 1.0F);
    }

    /**
     * N pressed again: the cage slowly closes ({@link #CAGE_CLOSE} ticks). 3.5.4 (the owner: "Bird cage is freezing and forcing entities to
     * the middle, regarless if the cage pulls them or Not, thats wrong, they are free to move around at all times, if they touch the cage
     * they get damage and get pushed along with it, only at the end are they free after taking the full damage"): no pull to the middle;
     * only its wall sweeps in whoever it reaches ({@link #cageShove}).
     */
    static void cageClose(ServerPlayer player, State s) {
        if (s.closeFrom >= 0) {
            return;
        }
        long now = now(player);
        s.closeFrom = now;
        s.cageUntil = now + CAGE_CLOSE;
        animate(player, "ito_cage_close");
        fx(player, CAGE, 0, s.cageAt, Vec3.f_82478_, CAGE_CLOSING, 0, s.cageR, CAGE_CLOSE);
        sound(player, s.cageAt.m_82520_(0, 2, 0), ItoSounds.CAGE, 2.5F, 0.7F);
    }

    /** The cage's radius now: standing, its own; closing, shutting in to {@link #CAGE_SHUT} (every client works it out the same). */
    public static double cageRadius(double radius, double closing, double close) {
        return com.hexhaki.piece.ito.ItoArt.closing(radius, CAGE_SHUT, closing, close);
    }

    static void cageTick(ServerPlayer player, State s, long now) {
        boolean closing = s.closeFrom >= 0;
        if (now >= s.cageUntil) {
            if (closing) {
                cageShut(player, s);
            } else {
                cageEnd(player, s);
            }
            return;
        }
        if ((now - s.cageFrom) % 2 != 0) {
            return;
        }
        double r = closing ? cageRadius(s.cageR, now - s.closeFrom, CAGE_CLOSE) : s.cageR;
        // how fast the wall comes in (blocks a tick), closing
        double sweep = closing ? Math.max(0.0, r - cageRadius(s.cageR, now - s.closeFrom + 1, CAGE_CLOSE)) : 0.0;
        Vec3 c = s.cageAt;
        for (LivingEntity body : foes(player, c, Math.max(r, s.cageR) + 2.0)) {
            Vec3 m = StyleCombat.middle(body);
            Vec3 d = m.m_82546_(c);
            // 3.5.0: the cage is a whole sphere round its middle (as drawn: its strings go on down into the ground)
            Vec3 dd = d;
            double dist = dd.m_82553_();
            if (dist < 1.0E-4) {
                continue;
            }
            double gap = dist - r;
            Vec3 out = dd.m_82490_(1.0 / dist);
            // inside it they move freely; only its wall moves them: closing, it sweeps in whoever it reaches (caught inside when it
            // began to close); standing, it pushes back in whoever is inside and keeps out whoever is outside
            boolean inside = closing ? dist < s.cageR + 0.5 : gap < 0.0;
            Vec3 v = body.m_20184_();
            double[] shove = cageShove(out.f_82479_, out.f_82480_, out.f_82481_, gap, v.f_82479_, v.f_82480_, v.f_82481_, inside, sweep);
            if (shove == null) {
                continue;
            }
            body.m_20256_(new Vec3(shove[0], shove[1], shove[2]));
            body.f_19864_ = true;
            body.f_19789_ = 0.0F;
            // touching its strings cuts (once every half second)
            Long last = s.cut.get(body.m_19879_());
            if (last == null || now - last >= 10) {
                s.cut.put(body.m_19879_(), now);
                cut(player, body, 4.0F, Vec3.f_82478_);
                fx(player, HIT, ++seeds, c.m_82549_(out.m_82490_(r)), out, HIT_CAGE, 0, 0.8, 0);
            }
        }
    }

    /** How near its wall (blocks, either side) the cage's strings reach a body. */
    public static final double CAGE_TOUCH = 1.2;
    /** How hard the wall pushes a body it touches (blocks a tick, on top of the wall's own speed in). */
    public static final double CAGE_PUSH = 0.3;

    /**
     * 3.5.4: what the cage's wall does to a body: (ox, oy, oz) the way from the cage's middle to it (unit), {@code gap} how far it is
     * outside the wall (negative: inside), (vx, vy, vz) its velocity, {@code inside} on the cage's inner side, {@code sweep} how fast the
     * wall comes in (0 standing). Away from the wall: null, left alone, free to move. Touching it: its velocity with the part going
     * through the wall taken away (it slides along the wall as it likes) and pushed {@link #CAGE_PUSH} plus the wall's own speed back to
     * its side (inside: in toward the middle, swept along as the wall closes; outside: out, kept out).
     */
    public static double[] cageShove(double ox, double oy, double oz, double gap, double vx, double vy, double vz, boolean inside, double sweep) {
        if (Math.abs(gap) > CAGE_TOUCH && !(inside && gap > 0.0)) {
            return null;
        }
        // the way back to its side of the wall: in for the inside, out for the outside
        double sx = inside ? -ox : ox;
        double sy = inside ? -oy : oy;
        double sz = inside ? -oz : oz;
        double into = vx * sx + vy * sy + vz * sz;
        double push = CAGE_PUSH + (inside ? sweep : 0.0);
        // only the part of its own velocity heading through the wall is taken away
        double through = Math.min(0.0, into);
        double nx = vx - sx * through + sx * push;
        double ny = vy - sy * through + sy * push;
        double nz = vz - sz * through + sz * push;
        return new double[] {nx, Math.max(ny, Math.min(vy, 0.0)), nz};
    }

    /**
     * The cage shut: everyone swept into its middle is cut deeply and left bleeding (the original's own wounds, gore/Gore.bleed), the
     * strings burst out of it, the Conqueror's lightning out of the burst.
     */
    static void cageShut(ServerPlayer player, State s) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 c = s.cageAt;
        for (LivingEntity body : foes(player, c, s.cageR + 1.0)) {
            Vec3 d = StyleCombat.middle(body).m_82546_(c);
            if (d.m_82553_() > s.cageR + 0.5) {
                continue;
            }
            cut(player, body, CAGE_DAMAGE, new Vec3(0, 0.25, 0));
            com.hexhaki.gore.Gore.bleed(player, body, 5, 200);
        }
        fx(player, SCATTER, ++seeds, c.m_82520_(0, 1.0, 0), Vec3.f_82478_, SCATTER_WHITE, 0, 7.0, 0);
        ConquerorAura.exploded(player, level, c.m_82520_(0, 1.0, 0), 5.0);
        sound(player, c.m_82520_(0, 1, 0), ItoSounds.SLICE, 2.0F, 0.6F);
        Impact.quake(level, c, 24.0, 0.5F, 10);
        cageEnd(player, s);
    }

    static void cageEnd(ServerPlayer player, State s) {
        if (s.cageUntil < 0) {
            return;
        }
        s.cageUntil = -1;
        s.closeFrom = -1;
        s.cut.clear();
        fx(player, CAGE, 0, s.cageAt != null ? s.cageAt : player.m_20182_(), Vec3.f_82478_, CAGE_STANDING, 0, 0, 0);
    }

    // ------------------------------------------------------------------------------------------------------ M: Awakening

    /**
     * 3.5.0: the Awakening (ito/ItoAwaken): without Haki the ground round the user turns to string for a while, spiking whoever stands
     * on it; with Armament a toggle: tentacles of string out of the ground and the trees at hostile bodies near, while it is on. M again
     * (or Armament off) ends the toggle.
     */
    static void awaken(ServerPlayer player, State s) {
        if (s.awakened) {
            awakenOff(player, s);
            return;
        }
        if (!ready(player, ItoKit.AWAKEN)) {
            return;
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        long now = now(player);
        s.busyUntil = now + 14;
        animate(player, "hie_ice_age");
        if (coated(player)) {
            cool(player, ItoKit.AWAKEN, 20);
            s.awakened = true;
            s.awakenedUntil = now + ItoAwaken.TOGGLE_MOST;
            s.nextTentacle = now + 6;
            fx(player, FIELD, ++seeds, player.m_20182_(), Vec3.f_82478_, 1, 0, 6.0, ItoAwaken.TOGGLE_MOST);
            sound(player, player.m_20182_(), ItoSounds.WAVE, 2.0F, 0.7F);
            return;
        }
        cool(player, ItoKit.AWAKEN, 600);
        Vec3 g = ground(level, player.m_20182_(), 0.5, 6.0);
        s.fieldAt = g != null ? g : player.m_20182_();
        s.fieldUntil = now + ItoAwaken.FIELD_TICKS;
        s.fieldSeed = ++seeds;
        s.spiked.clear();
        fx(player, FIELD, s.fieldSeed, s.fieldAt, Vec3.f_82478_, 0, 0, ItoAwaken.FIELD_RADIUS, ItoAwaken.FIELD_TICKS);
        sound(player, s.fieldAt, ItoSounds.WAVE, 2.0F, 0.9F);
    }

    static void awakenOff(ServerPlayer player, State s) {
        if (!s.awakened) {
            return;
        }
        s.awakened = false;
        s.taken.clear();
        fx(player, FIELD, ++seeds, player.m_20182_(), Vec3.f_82478_, 1, 0, 0, 0);
        cool(player, ItoKit.AWAKEN, 300);
    }

    static void awakenTick(ServerPlayer player, State s, long now) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        if (s.fieldUntil >= 0) {
            if (now >= s.fieldUntil || !player.m_6084_()) {
                s.fieldUntil = -1;
                s.spiked.clear();
            } else if (now % ItoAwaken.SPIKE_EVERY == 0) {
                Vec3 c = s.fieldAt;
                for (LivingEntity body : PikaMoves.foes(player, c, ItoAwaken.FIELD_RADIUS + 1.0)) {
                    Vec3 d = body.m_20182_().m_82546_(c);
                    if (!body.m_20096_() || Math.hypot(d.f_82479_, d.f_82481_) > ItoAwaken.FIELD_RADIUS || Math.abs(d.f_82480_) > 4.0) {
                        continue;
                    }
                    Long last = s.spiked.get(body.m_19879_());
                    if (last != null && now - last < ItoAwaken.SPIKE_REST) {
                        continue;
                    }
                    s.spiked.put(body.m_19879_(), now);
                    // a sharp spike of string out of the ground under it: cut, bleeding, thrown up
                    fx(player, SPIKE, ++seeds, body.m_20182_(), Vec3.f_82478_, 0, 0, ItoAwaken.SPIKE_HEIGHT, ItoAwaken.SPIKE_LIFE);
                    cut(player, body, ItoAwaken.SPIKE_DAMAGE, new Vec3(0, 0.9, 0));
                    com.hexhaki.gore.Gore.bleed(player, body, 3, 120);
                }
            }
        }
        if (!s.awakened) {
            return;
        }
        if (now >= s.awakenedUntil || !player.m_6084_() || !coated(player)) {
            awakenOff(player, s);
            return;
        }
        if (now < s.nextTentacle || s.tentacles >= ItoAwaken.TENTACLES) {
            return;
        }
        s.taken.values().removeIf(t -> now - t > ItoAwaken.TENTACLE_REST);
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity body : PikaMoves.foes(player, player.m_20182_(), ItoAwaken.TENTACLE_RANGE)) {
            if (s.taken.containsKey(body.m_19879_()) || !hostile(player, body)) {
                continue;
            }
            double d = body.m_20280_(player);
            if (d < bestDistance) {
                bestDistance = d;
                best = body;
            }
        }
        if (best == null) {
            s.nextTentacle = now + 4;
            return;
        }
        s.nextTentacle = now + ItoAwaken.TENTACLE_GAP;
        s.taken.put(best.m_19879_(), now);
        Vec3 tree = tree(level, best);
        Vec3 root;
        if (tree != null) {
            root = tree;
        } else {
            Vec3 toward = player.m_20182_().m_82546_(best.m_20182_());
            Vec3 flat = new Vec3(toward.f_82479_, 0, toward.f_82481_);
            double far = Math.sqrt(flat.m_82556_());
            flat = flat.m_82556_() < 1.0E-6 ? new Vec3(1, 0, 0) : flat.m_82541_();
            // 3.5.1: a near body's tentacle bursts out beside it, never under the user (it is massive now)
            double[] off = ItoAwaken.root(flat.f_82479_, flat.f_82481_, far, (best.m_19879_() & 1) == 0 ? 1 : -1);
            Vec3 at = best.m_20182_().m_82520_(off[0], 0.0, off[1]);
            Vec3 g = ground(level, at, 3.0, 8.0);
            root = g != null ? g : at;
        }
        s.tentacles++;
        fx(player, TENTACLE, best.m_19879_(), root, Vec3.f_82478_, tree != null ? 1 : 0, 0, ItoAwaken.LIFT_HEIGHT, ItoAwaken.tentacleLife());
        sound(player, root, ItoSounds.PULL, 1.8F, 0.6F);
        LIVE.add(new Tentacle(player, s, level, best, root, now));
    }

    /** A body the Haki Awakening goes for: a monster, a mob after the user, or whoever last hurt the user. */
    static boolean hostile(ServerPlayer player, LivingEntity body) {
        if (body.m_6095_().m_20674_() == net.minecraft.world.entity.MobCategory.MONSTER) {
            return true;
        }
        if (body instanceof net.minecraft.world.entity.Mob mob && mob.m_5448_() == player) {
            return true;
        }
        return player.m_21188_() == body;
    }

    /**
     * The top of a tree trunk within {@link ItoAwaken#TREE_REACH} blocks round {@code body} (a log, wood, stem or hyphae: its name), the
     * nearest; null: none (the tentacle comes out of the ground).
     */
    static Vec3 tree(ServerLevel level, LivingEntity body) {
        int fx = (int) Math.floor(body.m_20185_());
        int fy = (int) Math.floor(body.m_20186_());
        int fz = (int) Math.floor(body.m_20189_());
        int reach = ItoAwaken.TREE_REACH;
        double best = Double.MAX_VALUE;
        int[] found = null;
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > reach * reach || d2 >= best) {
                    continue;
                }
                for (int dy = -2; dy <= 6; dy++) {
                    if (trunk(level, new net.minecraft.core.BlockPos(fx + dx, fy + dy, fz + dz))) {
                        // up the trunk to its top
                        int top = fy + dy;
                        for (int k = 0; k < 12 && trunk(level, new net.minecraft.core.BlockPos(fx + dx, top + 1, fz + dz)); k++) {
                            top++;
                        }
                        best = d2;
                        found = new int[] {fx + dx, top, fz + dz};
                        break;
                    }
                }
            }
        }
        return found == null ? null : new Vec3(found[0] + 0.5, found[1] + 0.5, found[2] + 0.5);
    }

    static boolean trunk(ServerLevel level, net.minecraft.core.BlockPos pos) {
        net.minecraft.resources.ResourceLocation name = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(level.m_8055_(pos).m_60734_());
        return name != null && ItoAwaken.trunk(name.m_135815_());
    }

    /** A tentacle at work: it grows to the body, impales it, lifts it into the sky, throws it away from the user and sinks back. */
    static final class Tentacle implements UoMoves.Live {
        final ServerPlayer user;
        final State state;
        final ServerLevel level;
        final LivingEntity body;
        final Vec3 root;
        final long born;
        Vec3 caught;
        boolean thrown;

        Tentacle(ServerPlayer user, State state, ServerLevel level, LivingEntity body, Vec3 root, long born) {
            this.user = user;
            this.state = state;
            this.level = level;
            this.body = body;
            this.root = root;
            this.born = born;
        }

        @Override
        public ServerLevel level() {
            return level;
        }

        @Override
        public boolean tick(long now) {
            long age = now - born;
            boolean done = age >= ItoAwaken.tentacleLife() || !user.m_6084_();
            if (done) {
                state.tentacles = Math.max(0, state.tentacles - 1);
                return true;
            }
            if (!body.m_6084_() || body.m_213877_()) {
                return false; // the tentacle finishes (sinks back) on its own
            }
            if (age == ItoAwaken.RISE) {
                // the Haki-black tip impales it
                caught = body.m_20182_();
                cut(user, body, ItoAwaken.TENTACLE_DAMAGE, Vec3.f_82478_);
                com.hexhaki.gore.Gore.bleed(user, body, 4, 160);
                sound(user, StyleCombat.middle(body), ItoSounds.SLICE, 1.6F, 0.6F);
            }
            if (caught != null && age > ItoAwaken.RISE && age <= ItoAwaken.RISE + ItoAwaken.LIFT) {
                // lifted into the sky on the tentacle's tip
                double up = ItoAwaken.LIFT_HEIGHT * ItoAwaken.lift(age);
                body.m_6021_(caught.f_82479_, caught.f_82480_ + up, caught.f_82481_);
                body.m_20256_(Vec3.f_82478_);
                body.f_19789_ = 0.0F;
                body.f_19864_ = true;
            }
            if (caught != null && !thrown && age >= ItoAwaken.RISE + ItoAwaken.LIFT) {
                // and thrown away from the user
                thrown = true;
                Vec3 away = body.m_20182_().m_82546_(user.m_20182_());
                Vec3 flat = new Vec3(away.f_82479_, 0, away.f_82481_);
                flat = flat.m_82556_() < 1.0E-6 ? new Vec3(1, 0, 0) : flat.m_82541_();
                body.m_20256_(flat.m_82490_(2.2).m_82520_(0, 0.7, 0));
                body.f_19864_ = true;
                sound(user, StyleCombat.middle(body), ItoSounds.SWISH, 1.4F, 0.7F);
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------------------------------------ the passives

    /**
     * Sky Road (3.4.3): the jump pressed again in the air: the user is held up by strings to anchors in the sky and runs through it,
     * moved by their own client (client/ito/SkyRoad: momentum, arcs, the slingshot, the hover) as the Light fruit's glide is; here only
     * what the server must keep: the player may fly while it lasts (so no server kicks a floating runner; flying itself is kept off), no
     * fall is counted, and it ends on the ground ({@link #SKY_GROUNDED} ticks), in water, or on riding or dying (3.8.0: never on a
     * timer). A second air jump while it runs is the runner going up (the client), never a toggle.
     */
    static void sky(ServerPlayer player, State s) {
        long now = now(player);
        if (s.skyUntil >= 0 || now < s.skyNext || player.m_20096_() || player.m_150110_().f_35935_ || player.m_20069_() || player.m_20159_()) {
            return;
        }
        s.skyUntil = now + SKY_MOST;
        s.skyGrounded = 0;
        Abilities abilities = player.m_150110_();
        s.skyMayfly = abilities.f_35936_;
        abilities.f_35936_ = true;
        abilities.f_35935_ = false;
        player.m_6885_();
        player.f_19789_ = 0.0F;
        fx(player, SKY, 0, player.m_20182_(), Vec3.f_82478_, 1, 0, 0, SKY_MOST);
        sound(player, player.m_20182_(), ItoSounds.STEP, 0.9F, 0.9F);
    }

    static void skyTick(ServerPlayer player, State s, long now) {
        Abilities abilities = player.m_150110_();
        if (abilities.f_35935_ && !player.m_7500_() && !player.m_5833_()) {
            abilities.f_35935_ = false; // never the game's own flight (a double tap of the jump key): the strings carry them
            player.m_6885_();
        }
        player.f_19789_ = 0.0F;
        s.skyGrounded = player.m_20096_() ? s.skyGrounded + 1 : 0;
        if (s.skyGrounded >= SKY_GROUNDED || player.m_20159_() || !player.m_6084_() || player.m_20069_()) {
            skyEnd(player, s);
        }
    }

    /**
     * 3.5.2: a runner's own client threw a string, let one go or stepped on one (network/C2SItoSky): only while its Sky Road runs, near it,
     * at most {@link #SKY_PER_TICK} a tick; passed to everyone near (SKY_STRING), with a soft sound: a string's flick as one is thrown, a
     * swish as a swing lets go or a leap leaves, a faint swish under a step.
     */
    public static void skyString(ServerPlayer player, int mode, double x, double y, double z, float length, int extra) {
        State s = STATES.get(player.m_20148_());
        if (s == null || s.skyUntil < 0 || mode < ROPE_SWING || mode > SKY_STEP || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
            || !Float.isFinite(length)) {
            return;
        }
        long now = now(player);
        if (s.skyHeardAt != now) {
            s.skyHeardAt = now;
            s.skyHeard = 0;
        }
        Vec3 at = new Vec3(x, y, z);
        if (++s.skyHeard > SKY_PER_TICK || at.m_82554_(player.m_20182_()) > SKY_REACH) {
            return;
        }
        fx(player, SKY_STRING, mode, at, Vec3.f_82478_, Math.max(0, Math.min(3, extra)), Math.max(-360.0F, Math.min(360.0F, length)), 0, 0);
        Vec3 by = player.m_20182_();
        if (mode == ROPE_SWING || mode == ROPE_HANG) {
            sounds(player, by, STEP_SOUND, 0.7F, 1.25F, 1, 4);
        } else if (mode == SKY_FLY && extra != SKY_DROP) {
            sounds(player, by, ItoSounds.SWISH, 0.55F, 1.1F, 1, 6);
        } else if (mode == SKY_STEP) {
            sounds(player, by, STEP_SOUND, 0.3F, 1.5F, 1, 4);
        }
    }

    static final String STEP_SOUND = ItoSounds.STEP;

    /** Sky Road over: the player's own flight given back as it was; the fall from here never hurts (until they land). */
    static void skyEnd(ServerPlayer player, State s) {
        if (s.skyUntil < 0) {
            return;
        }
        s.skyUntil = -1;
        s.skyNext = now(player) + SKY_REST;
        s.skyFall = !player.m_20096_();
        Abilities abilities = player.m_150110_();
        boolean creative = player.m_7500_() || player.m_5833_();
        abilities.f_35936_ = creative || s.skyMayfly;
        if (!creative) {
            abilities.f_35935_ = false;
        }
        player.m_6885_();
        fx(player, SKY, 0, player.m_20182_(), Vec3.f_82478_, 0, 0, 0, 0);
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
        if (s.heatSince >= 0 && now - s.heatSince >= HEAT_MOST) {
            heatFire(player, s); // held its longest: it goes by itself
        }
        if (s.cageSince >= 0 && now - s.cageSince >= CAGE_HOLD_MOST) {
            cageCast(player, s);
        }
        if (s.knight != null && !s.knight.m_6084_()) {
            s.knight = null;
        }
        if (s.cageUntil >= 0) {
            cageTick(player, s, now);
        }
        if (s.fieldUntil >= 0 || s.awakened) {
            awakenTick(player, s, now);
        }
        if (s.skyUntil >= 0) {
            skyTick(player, s, now);
        } else if (s.skyFall) {
            player.f_19789_ = 0.0F; // the strings let go in the air: no fall damage for that drop
            s.skyFall = !player.m_20096_() && !player.m_20069_();
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
        skyEnd(player, s);
        if (s.heatSince >= 0) {
            s.heatSince = -1;
            fx(player, OVERHEAT, s.heatSeed, chest(player), Vec3.f_82478_, HEAT_DROP, 0, 0, 0);
        }
        if (s.cageSince >= 0) {
            s.cageSince = -1;
            fx(player, CAGE, 0, player.m_20182_(), Vec3.f_82478_, CAGE_STANDING, 0, 0, 0);
        }
        if (s.knight != null && s.knight.m_6084_()) {
            knightBurst(player, s.knight);
        }
        cageEnd(player, s);
        STATES.remove(player.m_20148_());
    }

    /** The server stopping (before the save): every caught shot given back its fall, everything dropped. */
    static void clear() {
        for (State s : STATES.values()) {
            if (s.knight != null && s.knight.m_6084_()) {
                s.knight.m_146870_(); // never saved: a knight without its weaver unravels
            }
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
