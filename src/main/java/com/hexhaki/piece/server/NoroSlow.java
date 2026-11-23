package com.hexhaki.piece.server;

import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.noro.NoroSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.3.4 (the owner: "Targets hit by Noroma photons—including people, objects, or projectiles like cannonballs—move in extreme slow motion
 * for 30 seconds before returning to normal speed"; "While a target is slowed, kinetic energy from strikes accumulates without
 * immediately taking effect, then releases all at once when the 30-second timer ends"): the Noro Noro no Mi's slowdown.
 *
 * <ul>
 *   <li>A living body: every tick its motion is cut to {@link #KEEP} of itself (it crawls, falls and jumps in slow motion), and it
 *       carries slowness, weakness and slow falling for its own client (a player moves themself).</li>
 *   <li>A projectile (an arrow, a fireball, a trident...): it hangs in the air, crawling along its way at {@link #CRAWL} of its speed
 *       without falling, until the slowdown ends and it flies on at its full speed again.</li>
 *   <li>Delayed Kinetic Force: every blow a slowed body takes from someone (any attacker's: blades, fists, arrows, moves) does nothing
 *       yet: its damage and its push add up (each shown as a small pink flash), and all land at once when the slowdown ends or the
 *       user calls Time's Up, as a burst of photons (the Conqueror's lightning out of it with the user's Haki: POINT ONE). At most
 *       {@link #MOST_SHARE} of its health and {@link #MOST_DAMAGE} (never a one-hit kill from full health).</li>
 * </ul>
 * Everyone sees it: one S2CUoFx (SLOW) names the body and how long; the clients draw its photon rings and keep a slowed projectile's
 * crawl in step themselves.
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class NoroSlow {
    /** The slowdown's length (ticks: 30 s). */
    public static final int LENGTH = 600;
    /** A slowed body keeps this much of its motion each tick; a slowed projectile crawls at this much of its speed. */
    public static final double KEEP = 0.2;
    public static final double CRAWL = 0.04;
    /** The stored blows' cap: this share of the body's most health, and this much. */
    static final double MOST_SHARE = 0.6;
    static final float MOST_DAMAGE = 40.0F;
    /** The stored push: so much a blow, at most so much (blocks a tick). */
    static final double PUSH_EACH = 0.35;
    static final double PUSH_MOST = 2.6;
    /** At most this many slowed things at once (the oldest let go first). */
    static final int MOST = 64;

    /** One slowed thing. */
    static final class Slow {
        final Entity body;
        final ServerPlayer user;
        final ServerLevel level;
        long until;
        /** A projectile's own velocity, to give back. */
        Vec3 velocity;
        float stored;
        int blows;
        double pushX;
        double pushY;
        double pushZ;

        Slow(Entity body, ServerPlayer user, ServerLevel level) {
            this.body = body;
            this.user = user;
            this.level = level;
        }
    }

    static final Map<Integer, Slow> SLOWED = new HashMap<>();
    private static boolean releasing;

    private NoroSlow() {
    }

    /** Whether {@code body} is slowed now. */
    public static boolean slowed(Entity body) {
        return !SLOWED.isEmpty() && SLOWED.containsKey(body.m_19879_());
    }

    /**
     * {@code body} hit by the user's photons: slowed for {@link #LENGTH} ticks (again: the clock starts over, the stored blows kept).
     * Returns whether it is newly slowed.
     */
    public static boolean slow(ServerPlayer user, Entity body) {
        if (body == user || !body.m_6084_() || body instanceof Player p && (p.m_5833_() || p.m_7500_())) {
            return false;
        }
        ServerLevel level = (ServerLevel) user.m_9236_();
        long now = level.m_46467_();
        Slow s = SLOWED.get(body.m_19879_());
        boolean fresh = s == null;
        if (fresh) {
            if (SLOWED.size() >= MOST) {
                Integer oldest = null;
                long when = Long.MAX_VALUE;
                for (Map.Entry<Integer, Slow> e : SLOWED.entrySet()) {
                    if (e.getValue().until < when) {
                        when = e.getValue().until;
                        oldest = e.getKey();
                    }
                }
                if (oldest != null) {
                    end(SLOWED.get(oldest), true);
                }
            }
            s = new Slow(body, user, level);
            if (body instanceof Projectile shot) {
                s.velocity = shot.m_20184_();
                shot.m_20242_(true);
            }
            SLOWED.put(body.m_19879_(), s);
            NoroMoves.sounds(user, body.m_20182_(), NoroSounds.SLOW, 0.9F, 1.0F, 2, 4);
        }
        s.until = now + LENGTH;
        Vec3 crawl = s.velocity != null ? s.velocity.m_82490_(CRAWL) : Vec3.f_82478_;
        if (body instanceof Projectile shot) {
            shot.m_20256_(crawl);
            shot.f_19812_ = true;
        }
        effects(body);
        PieceNetwork.around(body, new S2CUoFx(NoroMoves.SLOW, body.m_19879_(), user.m_19879_(), body.m_20185_(), body.m_20186_(), body.m_20189_(),
            (float) crawl.f_82479_, (float) crawl.f_82480_, (float) crawl.f_82481_, body instanceof Projectile ? 1 : 0, 0, 0, 0, LENGTH));
        return fresh;
    }

    /** What a slowed body's own client feels (refreshed every second while it lasts): it crawls, strikes slowly and floats down. */
    static void effects(Entity body) {
        if (body instanceof LivingEntity living) {
            living.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 30, 5, false, false, false));
            living.m_7292_(new MobEffectInstance(MobEffects.f_19613_, 30, 1, false, false, false));
            living.m_7292_(new MobEffectInstance(MobEffects.f_19591_, 30, 0, false, false, false));
            if (body instanceof Mob mob) {
                mob.m_21573_().m_26573_();
            }
        }
    }

    /** Ends a slowdown: the projectile flies on, the stored blows land at once ({@code release}), everyone is told. */
    static void end(Slow s, boolean release) {
        SLOWED.remove(s.body.m_19879_());
        Entity body = s.body;
        if (body instanceof Projectile shot && body.m_6084_()) {
            shot.m_20242_(false);
            if (s.velocity != null) {
                shot.m_20256_(s.velocity);
            }
            shot.f_19812_ = true;
        }
        PieceNetwork.around(body, S2CUoFx.of(NoroMoves.UNSLOW, body.m_19879_(), 0));
        if (release && s.blows > 0 && body instanceof LivingEntity living && living.m_6084_()) {
            unleash(s, living);
        }
    }

    /** Delayed Kinetic Force: every stored blow at once: the damage (capped), the push, a burst of photons, the Conqueror's lightning. */
    static void unleash(Slow s, LivingEntity body) {
        ServerPlayer user = s.user.m_213877_() ? null : s.user;
        float damage = (float) Math.min(Math.min(s.stored, MOST_DAMAGE), body.m_21233_() * MOST_SHARE);
        double push = Math.sqrt(s.pushX * s.pushX + s.pushY * s.pushY + s.pushZ * s.pushZ);
        double k = push > PUSH_MOST ? PUSH_MOST / push : 1.0;
        Vec3 middle = StyleCombat.middle(body);
        releasing = true;
        try {
            body.f_19802_ = 0;
            if (user != null) {
                body.m_6469_(StyleCombat.of(user), damage);
            }
        } finally {
            releasing = false;
        }
        body.m_20334_(s.pushX * k, Math.max(0.25, s.pushY * k + 0.3), s.pushZ * k);
        body.f_19864_ = true;
        float size = (float) Math.min(1.0, 0.25 + s.blows * 0.12 + damage / 30.0);
        PieceNetwork.around(body, new S2CUoFx(NoroMoves.RELEASE, body.m_19879_(), user != null ? user.m_19879_() : 0, middle.f_82479_,
            middle.f_82480_, middle.f_82481_, 0, 0, 0, s.blows, 0, 0, size, 0));
        if (user != null) {
            NoroMoves.sounds(user, middle, NoroSounds.RELEASE, 0.8F + size * 0.6F, 1.05F - size * 0.15F, 2, 6);
            ConquerorAura.exploded(user, s.level, middle, 2.0 + 3.0 * size);
        }
    }

    /** Time's Up: every slowdown {@code user} put on ends now, its blows landing at once. Returns how many. */
    static int timesUp(ServerPlayer user) {
        List<Slow> mine = new ArrayList<>();
        for (Slow s : SLOWED.values()) {
            if (s.user == user) {
                mine.add(s);
            }
        }
        for (Slow s : mine) {
            end(s, true);
        }
        return mine.size();
    }

    /**
     * The server stopping (NoroKit.reset, before the world is saved): every slowed shot given back its fall and its speed, so none is
     * saved hanging in the air for good; nothing is told or released (no one is left to see it).
     */
    static void clear() {
        for (Slow s : SLOWED.values()) {
            if (s.body instanceof Projectile shot && s.velocity != null) {
                shot.m_20242_(false);
                shot.m_20256_(s.velocity);
            }
        }
        SLOWED.clear();
        releasing = false;
    }

    /** Delayed Kinetic Force: a blow on a slowed body from someone is held back, added to the rest (a small pink flash shows it). */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void blow(LivingAttackEvent event) {
        if (SLOWED.isEmpty() || releasing) {
            return;
        }
        LivingEntity body = event.getEntity();
        Slow s = SLOWED.get(body.m_19879_());
        Entity attacker = event.getSource().m_7639_();
        if (s == null || attacker == null || attacker == body || event.getAmount() <= 0.0F) {
            return;
        }
        event.setCanceled(true);
        s.stored += event.getAmount();
        s.blows++;
        Vec3 way = body.m_20182_().m_82546_(attacker.m_20182_());
        Vec3 flat = new Vec3(way.f_82479_, 0, way.f_82481_);
        flat = flat.m_82556_() < 1.0E-6 ? Vec3.f_82478_ : flat.m_82541_();
        s.pushX += flat.f_82479_ * PUSH_EACH;
        s.pushY += PUSH_EACH * 0.4;
        s.pushZ += flat.f_82481_ * PUSH_EACH;
        Vec3 at = StyleCombat.middle(body);
        PieceNetwork.around(body, new S2CUoFx(NoroMoves.STORE, body.m_19879_(), attacker.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_,
            (float) flat.f_82479_, 0, (float) flat.f_82481_, s.blows, 0, 0, 0, 0));
        s.level.m_6263_(null, at.f_82479_, at.f_82480_, at.f_82481_, com.hexhaki.piece.PieceSounds.event(NoroSounds.STORE),
            net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.4F + Math.min(0.5F, s.blows * 0.05F));
    }

    /** A slowed body crawls: its motion cut at the start of its own tick (gravity and its legs then add only a little). */
    @SubscribeEvent
    public static void crawl(LivingEvent.LivingTickEvent event) {
        if (SLOWED.isEmpty()) {
            return;
        }
        LivingEntity body = event.getEntity();
        if (!(body.m_9236_() instanceof ServerLevel) || !SLOWED.containsKey(body.m_19879_())) {
            return;
        }
        Vec3 v = body.m_20184_();
        body.m_20334_(v.f_82479_ * KEEP, v.f_82480_ * KEEP, v.f_82481_ * KEEP);
    }

    /** The world's tick: projectiles kept crawling, effects refreshed, slowdowns that are over ended (their blows landing). */
    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || SLOWED.isEmpty() || !(event.level instanceof ServerLevel level)) {
            return;
        }
        long now = level.m_46467_();
        List<Slow> over = new ArrayList<>();
        for (Slow s : SLOWED.values()) {
            if (s.level != level) {
                continue;
            }
            if (!s.body.m_6084_() || s.body.m_9236_() != level) {
                over.add(s);
                continue;
            }
            if (now >= s.until) {
                over.add(s);
                continue;
            }
            if (s.body instanceof Projectile shot && s.velocity != null) {
                shot.m_20256_(s.velocity.m_82490_(CRAWL));
            } else if (now % 20 == 0) {
                effects(s.body);
            }
        }
        for (Slow s : over) {
            end(s, s.body.m_6084_());
        }
    }
}
