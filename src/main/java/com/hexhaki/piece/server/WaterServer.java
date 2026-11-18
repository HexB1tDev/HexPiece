package com.hexhaki.piece.server;

import com.hexhaki.fx.Impact;
import com.hexhaki.piece.PieceRules;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CWater;
import com.hexhaki.piece.style.Hit;
import com.hexhaki.piece.style.StyleMove;
import com.hexhaki.piece.water.WaterBullet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.3.8: Fishman Karate's water in the world (the server; {@code client/fx/WaterFx} draws it). The Water Bullet ({@link WaterBullet}):
 * while its move plays, {@link #bullets} fires drop {@code i} on its tick from its place over the raised fist, locked onto a foe in
 * front (picked at the first shot: the nearest first); each tick every drop in flight turns after its target (still alive, still in this
 * world) and flies on; it strikes the first body of a foe it comes within reach of (hurt, a light throw, the original's light impact)
 * or the first block in its way, and bursts there; one that runs out of life falls apart into spray. Every client near sees each drop
 * fired, and where it burst ({@link S2CWater}); a fighter who stops before every drop is fired lets the rest fall ({@link S2CWater#SPILL}).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class WaterServer {
    /** A drop in flight. */
    static final class Drop {
        final LivingEntity user;
        final ServerLevel level;
        final int id;
        final LivingEntity target;
        final float damage;
        final Predicate<LivingEntity> foe;
        final DamageSource source;
        Vec3 at;
        Vec3 velocity;
        int age;

        Drop(LivingEntity user, ServerLevel level, int id, LivingEntity target, float damage, Predicate<LivingEntity> foe, DamageSource source,
             Vec3 at, Vec3 velocity) {
            this.user = user;
            this.level = level;
            this.id = id;
            this.target = target;
            this.damage = damage;
            this.foe = foe;
            this.source = source;
            this.at = at;
            this.velocity = velocity;
        }
    }

    /** A fighter gathering drops: when its move began, the foes its drops lock onto (picked at the first shot), how many it fired. */
    static final class Gathering {
        final long start;
        List<LivingEntity> targets;
        int fired;

        Gathering(long start) {
            this.start = start;
        }
    }

    private static final List<Drop> FLYING = new ArrayList<>();
    private static final Map<Integer, Gathering> GATHERING = new HashMap<>();
    private static int next;

    private WaterServer() {
    }

    /**
     * One tick of {@code user}'s Water Bullet ({@code move}, begun at game tick {@code start}, {@code tick} ticks in): the drops gather
     * (the clients draw them from the move's start), each fired on its tick. {@code mastery} scales the damage.
     */
    public static void bullets(LivingEntity user, StyleMove move, int tick, long start, int mastery, Predicate<LivingEntity> foe,
                               DamageSource source) {
        if (!(user.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        Gathering gathering = GATHERING.get(user.m_19879_());
        if (gathering == null || gathering.start != start) {
            gathering = new Gathering(start);
            GATHERING.put(user.m_19879_(), gathering);
            // the air's moisture drawn together over the fist
            vanilla(level, user.m_20182_().m_82520_(0.0, 2.4, 0.0), "block.bubble_column.upwards_inside", 0.6F, 1.4F);
        }
        Hit[] hits = move.hits();
        for (int i = 0; i < hits.length; i++) {
            if (hits[i].tick() != tick) {
                continue;
            }
            if (gathering.targets == null) {
                gathering.targets = lockOn(user, foe);
            }
            int which = WaterBullet.target(i, gathering.targets.size());
            LivingEntity target = which < 0 ? null : gathering.targets.get(which);
            double[] from = WaterBullet.world(WaterBullet.formation(i), user.m_20185_(), user.m_20186_(), user.m_20189_(), user.m_146908_());
            Vec3 at = new Vec3(from[0], from[1], from[2]);
            Vec3 aim = target != null ? StyleCombat.middle(target).m_82546_(at) : user.m_20154_();
            double[] v = WaterBullet.steer(new double[] {aim.f_82479_, aim.f_82480_, aim.f_82481_}, new double[] {aim.f_82479_, aim.f_82480_,
                aim.f_82481_});
            Drop drop = new Drop(user, level, ++next, target, hits[i].damage() * PieceRules.masteryDamage(mastery), foe, source, at,
                new Vec3(v[0], v[1], v[2]));
            FLYING.add(drop);
            gathering.fired = i + 1;
            PieceNetwork.around(user, new S2CWater(S2CWater.FIRE, drop.id, user.m_19879_(), i, target == null ? -1 : target.m_19879_(), at.f_82479_,
                at.f_82480_, at.f_82481_, (float) v[0], (float) v[1], (float) v[2]));
            StyleCombat.sound(user, "fx.whoosh_light", 0.6F);
        }
        if (gathering.fired >= hits.length) {
            GATHERING.remove(user.m_19879_());
        }
    }

    /** The foes in front of {@code user} its drops lock onto: within reach and the cone, nearest first. */
    static List<LivingEntity> lockOn(LivingEntity user, Predicate<LivingEntity> foe) {
        Vec3 look = StyleCombat.facing(user);
        List<LivingEntity> found = new ArrayList<>();
        for (LivingEntity other : user.m_9236_().m_6443_(LivingEntity.class, user.m_20191_().m_82400_(WaterBullet.RANGE),
            e -> e != user && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
            double dx = other.m_20185_() - user.m_20185_();
            double dz = other.m_20189_() - user.m_20189_();
            if (Math.hypot(dx, dz) <= WaterBullet.RANGE && WaterBullet.ahead(look.f_82479_, look.f_82481_, dx, dz)) {
                found.add(other);
            }
        }
        found.sort(Comparator.comparingDouble(other -> other.m_20280_(user)));
        return found;
    }

    /** A fighter whose Water Bullet stopped before every drop was fired: the drops still over its fist fall. */
    static void spill(LivingEntity user) {
        spill(user, GATHERING.remove(user.m_19879_()));
    }

    /** The drops of {@code gathering} still over {@code user}'s fist fall (the gathering already forgotten by the caller). */
    static void spill(LivingEntity user, Gathering gathering) {
        if (gathering != null && gathering.fired < WaterBullet.COUNT) {
            PieceNetwork.around(user, new S2CWater(S2CWater.SPILL, 0, user.m_19879_(), gathering.fired, -1, user.m_20185_(), user.m_20186_(),
                user.m_20189_(), 0, 0, 0));
        }
    }

    @SubscribeEvent
    public static void waterTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        if (!GATHERING.isEmpty()) {
            // 2.5.9 (the owner's server crash, ConcurrentModificationException here): until now this was a removeIf over the map
            // whose check spilled a stopped Water Bullet's drops through spill(user), which removes from the same map mid-iteration:
            // every Water Bullet stopped before its last drop (its user hit, dead or out of the move) crashed the server. Now a
            // snapshot is walked and each finished gathering forgotten by its own id, and a failure forgets it instead of stopping
            // the game
            long now = level.m_46467_();
            for (Map.Entry<Integer, Gathering> entry : new ArrayList<>(GATHERING.entrySet())) {
                int id = entry.getKey();
                Gathering gathering = entry.getValue();
                try {
                    var body = level.m_6815_(id);
                    if (!(body instanceof LivingEntity user)) {
                        // another world's, or gone: forgotten once its move would have ended
                        if (now - gathering.start > 120) {
                            GATHERING.remove(id, gathering);
                        }
                    } else if (!user.m_6084_() || !gathering(user)) {
                        GATHERING.remove(id, gathering);
                        spill(user, gathering);
                    }
                } catch (RuntimeException failure) {
                    GATHERING.remove(id, gathering); // water never stops the game
                }
            }
        }
        if (FLYING.isEmpty()) {
            return;
        }
        // a snapshot as well: a drop's strike runs the game's damage (and every mod's hurt events), which must never be able to
        // disturb this walk
        for (Drop drop : new ArrayList<>(FLYING)) {
            if (drop.level != level) {
                continue;
            }
            boolean gone;
            try {
                gone = fly(drop);
            } catch (RuntimeException failure) {
                gone = true; // water never stops the game
            }
            if (gone) {
                FLYING.remove(drop);
            }
        }
    }

    /** True while {@code user} is still in its Water Bullet (a player's move, or a sea fighter's). */
    static boolean gathering(LivingEntity user) {
        StyleMove move = user instanceof net.minecraft.server.level.ServerPlayer player ? StyleServer.moveOf(player)
            : user instanceof com.hexhaki.piece.npc.SeaFighter fighter ? fighter.currentMove() : null;
        return move != null && move.effect() == StyleMove.Effect.BULLETS;
    }

    /** One tick of a drop's flight; true when it is gone (it struck, or fell apart). */
    static boolean fly(Drop drop) {
        drop.age++;
        if (drop.target != null && drop.target.m_6084_() && drop.target.m_9236_() == drop.level) {
            Vec3 to = StyleCombat.middle(drop.target).m_82546_(drop.at);
            double[] v = WaterBullet.steer(new double[] {drop.velocity.f_82479_, drop.velocity.f_82480_, drop.velocity.f_82481_},
                new double[] {to.f_82479_, to.f_82480_, to.f_82481_});
            drop.velocity = new Vec3(v[0], v[1], v[2]);
        }
        Vec3 from = drop.at;
        Vec3 to = from.m_82549_(drop.velocity);
        // the first foe along this tick's path
        LivingEntity struck = null;
        double first = Double.MAX_VALUE;
        AABB around = new AABB(Math.min(from.f_82479_, to.f_82479_), Math.min(from.f_82480_, to.f_82480_), Math.min(from.f_82481_, to.f_82481_),
            Math.max(from.f_82479_, to.f_82479_), Math.max(from.f_82480_, to.f_82480_), Math.max(from.f_82481_, to.f_82481_))
            .m_82400_(WaterBullet.STRIKES + 1.0);
        for (LivingEntity other : drop.level.m_6443_(LivingEntity.class, around,
            e -> e != drop.user && e.m_6084_() && !e.m_5833_() && drop.foe.test(e))) {
            double along = closest(from, to, StyleCombat.middle(other));
            Vec3 point = from.m_82549_(to.m_82546_(from).m_82490_(along));
            AABB body = other.m_20191_().m_82400_(WaterBullet.STRIKES);
            if (body.m_82390_(point) && along < first) {
                first = along;
                struck = other;
            }
        }
        BlockHitResult wall = drop.level.m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, drop.user));
        double wallAt = wall != null && wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_().m_82554_(from) / Math.max(1.0E-6, to.m_82554_(from))
            : Double.MAX_VALUE;
        Vec3 dir = drop.velocity.m_82541_();
        if (struck != null && first <= wallAt) {
            Vec3 at = from.m_82549_(to.m_82546_(from).m_82490_(first));
            struck.f_19802_ = 0;
            if (struck.m_6469_(drop.source, drop.damage)) {
                Impact.hit(drop.user, struck, StyleCombat.spec(Hit.Weight.LIGHT, new Vec3(dir.f_82479_ * 0.35, 0.08, dir.f_82481_ * 0.35)));
            }
            burst(drop, at, dir, struck.m_19879_());
            return true;
        }
        if (wallAt <= 1.0) {
            burst(drop, wall.m_82450_(), dir, -1);
            return true;
        }
        drop.at = to;
        if (drop.age >= WaterBullet.LIFE) {
            PieceNetwork.around(drop.user, new S2CWater(S2CWater.FADE, drop.id, drop.user.m_19879_(), 0, -1, to.f_82479_, to.f_82480_, to.f_82481_,
                (float) drop.velocity.f_82479_, (float) drop.velocity.f_82480_, (float) drop.velocity.f_82481_));
            return true;
        }
        return false;
    }

    /** Where along from..to (0..1) the point nearest {@code p} lies. */
    static double closest(Vec3 from, Vec3 to, Vec3 p) {
        Vec3 d = to.m_82546_(from);
        Vec3 q = p.m_82546_(from);
        double l = d.f_82479_ * d.f_82479_ + d.f_82480_ * d.f_82480_ + d.f_82481_ * d.f_82481_;
        return l < 1.0E-9 ? 0.0 : Math.max(0.0, Math.min(1.0, (q.f_82479_ * d.f_82479_ + q.f_82480_ * d.f_82480_ + q.f_82481_ * d.f_82481_) / l));
    }

    /** A drop bursts at {@code at}: everyone near sees the splash, hears it. */
    static void burst(Drop drop, Vec3 at, Vec3 dir, int struck) {
        PieceNetwork.around(drop.user, new S2CWater(S2CWater.BURST, drop.id, drop.user.m_19879_(), 0, struck, at.f_82479_, at.f_82480_, at.f_82481_,
            (float) dir.f_82479_, (float) dir.f_82480_, (float) dir.f_82481_));
        float vary = drop.user.m_217043_().m_188501_();
        vanilla(drop.level, at, "entity.fishing_bobber.splash", 0.8F, 1.15F + vary * 0.3F);
        vanilla(drop.level, at, "entity.generic.splash", 0.35F, 1.6F + vary * 0.3F);
    }

    /** One of the game's own sounds (by its name: the water's are Minecraft's own splashes), heard by everyone near {@code at}. */
    public static void vanilla(ServerLevel level, Vec3 at, String name, float volume, float pitch) {
        net.minecraft.sounds.SoundEvent sound = net.minecraftforge.registries.ForgeRegistries.SOUND_EVENTS.getValue(
            net.minecraft.resources.ResourceLocation.m_214293_("minecraft", name));
        if (sound != null) {
            level.m_6263_(null, at.f_82479_, at.f_82480_, at.f_82481_, sound, net.minecraft.sounds.SoundSource.PLAYERS, volume, pitch);
        }
    }
}
