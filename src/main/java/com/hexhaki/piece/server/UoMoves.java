package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.fruit.FruitUtil;
import com.hexhaki.fx.Impact;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.dragon.DragonForm;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
 * 2.7.2: the Uo Uo no Mi's moves on the server (the owner, verbatim in docs/handoff/next-ai-2.7.0.md). Human form (fruit mode):
 * X Air Pressure, C Blast Breath, V Wind Force, B Dragon Embers; dragon form: M1 held fire breath, X charged Blast Breath, C Gigantic
 * Bite. Every move is simulated here (where it flies, what it strikes, its damage, fire and throw); the clients draw it from
 * {@link S2CUoFx} (client/dragon/UoFx). Cooldowns are the original's fruit cooldowns (FruitData, shown on the abilities panel).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class UoMoves {
    // ---- the human form's beats (ticks from the key), kept equal to tools/uo_anims.py's
    static final int[] AIR_BEATS = {4, 7, 10};
    static final int BREATH_AT = 7;
    static final int SWIPE_AT = 6;
    static final int EMBERS_AT = 8;
    // ---- ranges (blocks), the owner's
    public static final double AIR_RANGE = 25.0;
    public static final double BREATH_RANGE = 35.0;
    public static final double WIND_RANGE = 25.0;
    public static final double EMBER_RANGE = 15.0;
    public static final double FLAME_RANGE = 35.0;
    public static final double BLAST_MIN = 45.0;
    public static final double BLAST_MAX = 100.0;
    // ---- cooldowns (ticks)
    static final int AIR_COOL = 60;
    static final int BREATH_COOL = 100;
    static final int WIND_COOL = 200;
    static final int EMBERS_COOL = 240;
    static final int FLAME_COOL = 80;
    static final int BLAST_COOL = 160;
    static final int BITE_COOL = 100;
    /** The dragon's breath: how long it can be held (ticks), its cone's half-angle (degrees). */
    static final int FLAME_MAX = 100;
    static final double FLAME_CONE = 9.0;
    /** The charged blast: full after this long (ticks), the shortest charge that fires. */
    static final int BLAST_FULL = 60;
    static final int BLAST_LEAST = 6;
    /** The tornado: its speed (blocks a tick), its radius at the ground and at the top, its height. */
    static final double WIND_SPEED = 0.55;
    static final double WIND_LOW = 2.2;
    static final double WIND_HIGH = 4.2;
    static final double WIND_TALL = 9.0;

    /** Something of a move in the world, ticked until it is done. */
    interface Live {
        /** One tick; true when it is over. */
        boolean tick(long now);

        ServerLevel level();
    }

    private static final List<Live> LIVE = new ArrayList<>();
    /** Who is breathing fire, since when. */
    record Flame(ServerPlayer player, long start) {
    }

    private static final Map<UUID, Flame> FLAMING = new HashMap<>();
    private static final Map<UUID, Long> CHARGING = new HashMap<>();
    private static int next;

    private UoMoves() {
    }

    /** A key of the Uo kit going down ({@code slot}: the quick slot, the move's own by default). */
    public static void press(ServerPlayer player, int slot) {
        int phase = DragonServer.phase(player);
        long now = player.m_9236_().m_46467_();
        if (phase == DragonForm.HUMAN) {
            if (slot == UoKit.AIR_PRESSURE.slot()) {
                airPressure(player, now);
            } else if (slot == UoKit.BLAST_BREATH.slot()) {
                blastBreath(player, now);
            } else if (slot == UoKit.WIND_FORCE.slot()) {
                windForce(player, now);
            } else if (slot == UoKit.DRAGON_EMBERS.slot()) {
                embers(player, now);
            }
        } else if (phase == DragonForm.DRAGON) {
            if (slot == UoKit.CHARGED_BLAST.slot() && ready(player, UoKit.CHARGED_BLAST) && !CHARGING.containsKey(player.m_20148_())) {
                CHARGING.put(player.m_20148_(), now);
                PieceNetwork.around(player, S2CUoFx.of(S2CUoFx.CHARGE_ON, 0, player.m_19879_()));
                DragonServer.sound(player, "stream", 1.2F, 0.6F);
            } else if (slot == UoKit.GIGANTIC_BITE.slot()) {
                bite(player, now);
            }
        }
    }

    /** A key of the Uo kit coming up: the charged blast is let go. */
    public static void release(ServerPlayer player, int slot) {
        if (slot != UoKit.CHARGED_BLAST.slot()) {
            return;
        }
        Long from = CHARGING.remove(player.m_20148_());
        if (from == null) {
            return;
        }
        PieceNetwork.around(player, S2CUoFx.of(S2CUoFx.CHARGE_OFF, 0, player.m_19879_()));
        long held = player.m_9236_().m_46467_() - from;
        if (held < BLAST_LEAST || DragonServer.phase(player) != DragonForm.DRAGON) {
            return;
        }
        float charge = (float) Math.min(1.0, held / (double) BLAST_FULL);
        cool(player, UoKit.CHARGED_BLAST, BLAST_COOL);
        Vec3 look = player.m_20154_();
        Vec3 from2 = mouth(player);
        double range = BLAST_MIN + (BLAST_MAX - BLAST_MIN) * charge;
        double size = 1.6 + 2.6 * charge;
        // 2.7.6 (the owner: "more aggressive more violent"): a wider, harder, hotter blast
        launch(player, from2, look.m_82490_(3.6), range, size, 4.5 + 7.5 * charge, 16.0F + 30.0F * charge, 10, 1.8 + 2.4 * charge, S2CUoFx.BLAST);
        DragonServer.sound(player, "whoosh_heavy", 2.0F, 0.5F);
    }

    /** M1 held in dragon form (client/dragon/DragonInput, C2SDragon): the fire breath on and off. */
    public static void flame(ServerPlayer player, boolean on) {
        UUID id = player.m_20148_();
        long now = player.m_9236_().m_46467_();
        if (on) {
            if (DragonServer.phase(player) != DragonForm.DRAGON || FLAMING.containsKey(id) || !ready(player, UoKit.FIRE_BREATH)) {
                return;
            }
            FLAMING.put(id, new Flame(player, now));
            PieceNetwork.around(player, S2CUoFx.of(S2CUoFx.FLAME_ON, 0, player.m_19879_()));
            DragonServer.sound(player, "flame", 1.6F, 0.7F);
        } else if (FLAMING.remove(id) != null) {
            stopFlame(player);
        }
    }

    static void stopFlame(ServerPlayer player) {
        PieceNetwork.around(player, S2CUoFx.of(S2CUoFx.FLAME_OFF, 0, player.m_19879_()));
        cool(player, UoKit.FIRE_BREATH, FLAME_COOL);
    }

    /** The dragon gone (or the player): nothing of it keeps going. */
    public static void forget(ServerPlayer player) {
        if (FLAMING.remove(player.m_20148_()) != null) {
            PieceNetwork.around(player, S2CUoFx.of(S2CUoFx.FLAME_OFF, 0, player.m_19879_()));
        }
        if (CHARGING.remove(player.m_20148_()) != null) {
            PieceNetwork.around(player, S2CUoFx.of(S2CUoFx.CHARGE_OFF, 0, player.m_19879_()));
        }
    }

    // ------------------------------------------------------------------------------------------------------ the human form

    static void airPressure(ServerPlayer player, long now) {
        if (!ready(player, UoKit.AIR_PRESSURE)) {
            return;
        }
        cool(player, UoKit.AIR_PRESSURE, AIR_COOL);
        HakiServerController.animate(player, "uo_air_pressure", 2);
        ServerLevel level = (ServerLevel) player.m_9236_();
        for (int beat : AIR_BEATS) {
            LIVE.add(new Later(level, now + beat, () -> {
                if (!player.m_6084_()) {
                    return;
                }
                Vec3 look = player.m_20154_();
                Vec3 hand = player.m_146892_().m_82549_(look.m_82490_(0.9)).m_82549_(right(player).m_82490_(0.35)).m_82520_(0.0, -0.35, 0.0);
                launch(player, hand, look.m_82490_(4.2), AIR_RANGE, 0.9, 2.0, 8.0F, 0, 1.8, S2CUoFx.AIR);
                StyleCombat.sound(player, "fx.whoosh_heavy", 1.0F);
            }));
        }
    }

    static void blastBreath(ServerPlayer player, long now) {
        if (!ready(player, UoKit.BLAST_BREATH)) {
            return;
        }
        cool(player, UoKit.BLAST_BREATH, BREATH_COOL);
        HakiServerController.animate(player, "uo_blast_breath", 2);
        ServerLevel level = (ServerLevel) player.m_9236_();
        LIVE.add(new Later(level, now + BREATH_AT, () -> {
            if (!player.m_6084_()) {
                return;
            }
            Vec3 look = player.m_20154_();
            Vec3 mouth = player.m_146892_().m_82549_(look.m_82490_(0.5)).m_82520_(0.0, -0.12, 0.0);
            launch(player, mouth, look.m_82490_(3.2), BREATH_RANGE, 0.7, 4.5, 11.0F, 6, 1.6, S2CUoFx.BOLT);
            DragonServer.sound(player, "flame", 1.4F, 0.9F);
        }));
    }

    static void windForce(ServerPlayer player, long now) {
        if (!ready(player, UoKit.WIND_FORCE)) {
            return;
        }
        cool(player, UoKit.WIND_FORCE, WIND_COOL);
        HakiServerController.animate(player, "uo_wind_force", 2);
        ServerLevel level = (ServerLevel) player.m_9236_();
        LIVE.add(new Later(level, now + SWIPE_AT, () -> {
            if (!player.m_6084_()) {
                return;
            }
            Vec3 look = player.m_20154_();
            Vec3 flat = new Vec3(look.f_82479_, 0.0, look.f_82481_);
            flat = flat.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.m_82541_();
            Vec3 base = player.m_20182_().m_82549_(flat.m_82490_(2.5));
            Tornado tornado = new Tornado(player, level, ++next, base, flat.m_82490_(WIND_SPEED), (int) Math.round(WIND_RANGE / WIND_SPEED));
            LIVE.add(tornado);
            PieceNetwork.around(player, new S2CUoFx(S2CUoFx.TORNADO, tornado.id, player.m_19879_(), base.f_82479_, base.f_82480_, base.f_82481_,
                (float) tornado.step.f_82479_, (float) tornado.step.f_82480_, (float) tornado.step.f_82481_, 0, 0, 0, 1.0F, tornado.life));
            StyleCombat.sound(player, "fx.whoosh_heavy", 1.4F);
        }));
    }

    static void embers(ServerPlayer player, long now) {
        if (!ready(player, UoKit.DRAGON_EMBERS)) {
            return;
        }
        cool(player, UoKit.DRAGON_EMBERS, EMBERS_COOL);
        HakiServerController.animate(player, "uo_embers", 2);
        ServerLevel level = (ServerLevel) player.m_9236_();
        LIVE.add(new Later(level, now + EMBERS_AT, () -> {
            if (!player.m_6084_()) {
                return;
            }
            Predicate<LivingEntity> foe = other -> FruitUtil.foe(player, other);
            List<LivingEntity> near = level.m_6443_(LivingEntity.class, player.m_20191_().m_82400_(EMBER_RANGE),
                e -> e != player && e.m_6084_() && !e.m_5833_() && foe.test(e) && e.m_20280_(player) <= EMBER_RANGE * EMBER_RANGE);
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int count = Math.max(8, Math.min(14, near.size() * 2 + 4));
            Vec3 mouth = player.m_146892_().m_82520_(0.0, 0.1, 0.0);
            for (int i = 0; i < count; i++) {
                Vec3 land;
                if (!near.isEmpty() && i < near.size() * 2) {
                    LivingEntity target = near.get(i % near.size());
                    land = target.m_20182_().m_82520_(random.nextDouble(-0.6, 0.6), 0.0, random.nextDouble(-0.6, 0.6));
                } else {
                    double a = random.nextDouble() * Math.PI * 2.0;
                    double r = 3.0 + random.nextDouble() * (EMBER_RANGE - 4.0);
                    land = ground(level, player.m_20182_().m_82520_(Math.cos(a) * r, 0.0, Math.sin(a) * r));
                }
                double a = random.nextDouble() * Math.PI * 2.0;
                Vec3 apex = mouth.m_82520_(Math.cos(a) * random.nextDouble(1.0, 4.5), random.nextDouble(9.0, 13.0), Math.sin(a) * random.nextDouble(1.0, 4.5));
                int delay = i / 2;
                int rise = 12 + random.nextInt(5);
                int fall = 9 + random.nextInt(4);
                Ember ember = new Ember(player, level, ++next, land, now + delay + rise + fall);
                LIVE.add(ember);
                PieceNetwork.around(player, new S2CUoFx(S2CUoFx.EMBER, ember.id, player.m_19879_(), mouth.f_82479_, mouth.f_82480_, mouth.f_82481_,
                    (float) apex.f_82479_, (float) apex.f_82480_, (float) apex.f_82481_, (float) land.f_82479_, (float) land.f_82480_,
                    (float) land.f_82481_, 0.55F, delay << 16 | rise << 8 | fall));
            }
            DragonServer.sound(player, "flame", 1.5F, 1.2F);
        }));
    }

    /** The ground under a point (or the point, when there is none near). */
    static Vec3 ground(ServerLevel level, Vec3 at) {
        BlockHitResult hit = level.m_45547_(new ClipContext(at.m_82520_(0.0, 6.0, 0.0), at.m_82520_(0.0, -12.0, 0.0), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, null));
        return hit.m_6662_() == HitResult.Type.BLOCK ? hit.m_82450_() : at;
    }

    // ------------------------------------------------------------------------------------------------------ the dragon form

    /** Where the dragon's mouth is, near enough for what it strikes: over the player's feet, out along the look. */
    static Vec3 mouth(ServerPlayer player) {
        Vec3 look = player.m_20154_();
        return player.m_20182_().m_82520_(0.0, DragonForm.HEAD_HEIGHT - 0.6, 0.0).m_82549_(look.m_82490_(2.6));
    }

    static void bite(ServerPlayer player, long now) {
        if (!ready(player, UoKit.GIGANTIC_BITE)) {
            return;
        }
        cool(player, UoKit.GIGANTIC_BITE, BITE_COOL);
        Vec3 look = player.m_20154_();
        player.m_20334_(look.f_82479_ * 2.2, look.f_82480_ * 2.2 + 0.1, look.f_82481_ * 2.2);
        player.f_19864_ = true;
        PieceNetwork.around(player, S2CUoFx.of(S2CUoFx.BITE, 0, player.m_19879_()));
        DragonServer.sound(player, "whoosh_heavy", 1.8F, 0.7F);
        ServerLevel level = (ServerLevel) player.m_9236_();
        LIVE.add(new Later(level, now + 7, () -> {
            if (!player.m_6084_() || DragonServer.phase(player) != DragonForm.DRAGON) {
                return;
            }
            Vec3 jaws = mouth(player).m_82549_(player.m_20154_().m_82490_(3.0));
            Predicate<LivingEntity> foe = other -> FruitUtil.foe(player, other);
            DamageSource source = StyleCombat.of(player);
            for (LivingEntity body : level.m_6443_(LivingEntity.class, box(jaws, jaws).m_82400_(5.0),
                e -> e != player && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
                if (body.m_20191_().m_82400_(1.0).m_82390_(nearest(body.m_20191_(), jaws)) && body.m_20182_().m_82554_(jaws) < 5.5) {
                    body.f_19802_ = 0;
                    if (body.m_6469_(source, 20.0F)) {
                        Vec3 throwAway = player.m_20154_().m_82490_(1.4).m_82520_(0.0, 0.5, 0.0);
                        Impact.hit(player, body, StyleCombat.spec(com.hexhaki.piece.style.Hit.Weight.HEAVY, throwAway));
                    }
                }
            }
            DragonServer.sound(player, "slam", 1.6F, 0.6F);
        }));
    }

    /** The box between two points. */
    static AABB box(Vec3 a, Vec3 b) {
        return new AABB(Math.min(a.f_82479_, b.f_82479_), Math.min(a.f_82480_, b.f_82480_), Math.min(a.f_82481_, b.f_82481_),
            Math.max(a.f_82479_, b.f_82479_), Math.max(a.f_82480_, b.f_82480_), Math.max(a.f_82481_, b.f_82481_));
    }

    static Vec3 nearest(AABB box, Vec3 p) {
        return new Vec3(Math.max(box.f_82288_, Math.min(box.f_82291_, p.f_82479_)), Math.max(box.f_82289_, Math.min(box.f_82292_, p.f_82480_)),
            Math.max(box.f_82290_, Math.min(box.f_82293_, p.f_82481_)));
    }

    /** One tick of a held breath: a cone of fire from the mouth along the look, stopped by the first block. */
    static void breathe(ServerPlayer player, long ticks) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 from = mouth(player);
        Vec3 look = player.m_20154_();
        Vec3 to = from.m_82549_(look.m_82490_(FLAME_RANGE));
        BlockHitResult wall = level.m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double reach = wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_().m_82554_(from) : FLAME_RANGE;
        if (ticks % 5 != 0) {
            return;
        }
        Predicate<LivingEntity> foe = other -> FruitUtil.foe(player, other);
        DamageSource source = StyleCombat.of(player);
        double tan = Math.tan(Math.toRadians(FLAME_CONE));
        AABB box = box(from, from.m_82549_(look.m_82490_(reach))).m_82400_(reach * tan + 1.0);
        for (LivingEntity body : level.m_6443_(LivingEntity.class, box, e -> e != player && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
            Vec3 middle = StyleCombat.middle(body);
            Vec3 rel = middle.m_82546_(from);
            double along = rel.m_82526_(look);
            if (along < 0.0 || along > reach) {
                continue;
            }
            double off = rel.m_82546_(look.m_82490_(along)).m_82553_();
            if (off <= along * tan + 0.8 + body.m_20205_() * 0.5) {
                body.f_19802_ = 0;
                if (body.m_6469_(source, 4.0F)) {
                    body.m_20254_(5);
                    Impact.hit(player, body, StyleCombat.spec(com.hexhaki.piece.style.Hit.Weight.LIGHT, look.m_82490_(0.25)));
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ what flies

    /**
     * A shot along {@code velocity} (blocks a tick) for {@code range}: it strikes the first foe it passes within {@code size} of, or the
     * first block, or ends there; and bursts: {@code radius} around (damage, {@code fire} seconds of fire, a throw out of
     * {@code knock}).
     */
    static void launch(ServerPlayer player, Vec3 from, Vec3 velocity, double range, double size, double radius, float damage, int fire,
                       double knock, int kind) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Shot shot = new Shot(player, level, ++next, from, velocity, range, size, radius, damage, fire, knock, kind);
        LIVE.add(shot);
        PieceNetwork.around(player, new S2CUoFx(kind, shot.id, player.m_19879_(), from.f_82479_, from.f_82480_, from.f_82481_,
            (float) velocity.f_82479_, (float) velocity.f_82480_, (float) velocity.f_82481_, 0, 0, 0, (float) size,
            (int) Math.ceil(range / velocity.m_82553_())));
    }

    static final class Shot implements Live {
        final ServerPlayer user;
        final ServerLevel level;
        final int id;
        final Vec3 velocity;
        final double range;
        final double size;
        final double radius;
        final float damage;
        final int fire;
        final double knock;
        final int kind;
        Vec3 at;
        double flown;

        Shot(ServerPlayer user, ServerLevel level, int id, Vec3 at, Vec3 velocity, double range, double size, double radius, float damage, int fire,
             double knock, int kind) {
            this.user = user;
            this.level = level;
            this.id = id;
            this.at = at;
            this.velocity = velocity;
            this.range = range;
            this.size = size;
            this.radius = radius;
            this.damage = damage;
            this.fire = fire;
            this.knock = knock;
            this.kind = kind;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            Vec3 from = this.at;
            double step = Math.min(this.velocity.m_82553_(), this.range - this.flown);
            Vec3 dir = this.velocity.m_82541_();
            Vec3 to = from.m_82549_(dir.m_82490_(step));
            Predicate<LivingEntity> foe = other -> FruitUtil.foe(this.user, other);
            AABB around = box(from, to).m_82400_(this.size + 1.0);
            double first = Double.MAX_VALUE;
            for (LivingEntity other : this.level.m_6443_(LivingEntity.class, around,
                e -> e != this.user && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
                double along = WaterServer.closest(from, to, StyleCombat.middle(other));
                Vec3 point = from.m_82549_(to.m_82546_(from).m_82490_(along));
                if (other.m_20191_().m_82400_(this.size).m_82390_(point) && along < first) {
                    first = along;
                }
            }
            BlockHitResult wall = this.level.m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.user));
            double wallAt = wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_().m_82554_(from) / Math.max(1.0E-6, step) : Double.MAX_VALUE;
            double end = Math.min(first, wallAt);
            if (end <= 1.0) {
                burst(from.m_82549_(to.m_82546_(from).m_82490_(Math.max(0.0, end - 0.05))), dir);
                return true;
            }
            this.at = to;
            this.flown += step;
            if (this.flown >= this.range - 1.0E-6) {
                burst(to, dir);
                return true;
            }
            return false;
        }

        void burst(Vec3 at, Vec3 dir) {
            explode(this.user, this.level, at, dir, this.radius, this.damage, this.fire, this.knock);
            PieceNetwork.around(this.user, new S2CUoFx(S2CUoFx.BURST, this.id, this.user.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_,
                (float) dir.f_82479_, (float) dir.f_82480_, (float) dir.f_82481_, 0, 0, this.kind, (float) this.radius, 0));
            if (this.kind != S2CUoFx.AIR) {
                Impact.quake(this.level, at, this.radius * 5.0, (float) Math.min(1.0, this.radius / 6.0), 14);
            }
            DragonServer.sound(this.user, this.kind == S2CUoFx.AIR ? "impact_heavy" : "slam", this.kind == S2CUoFx.BLAST ? 2.4F : 1.2F,
                this.kind == S2CUoFx.BLAST ? 0.55F : 0.9F);
        }
    }

    /** {@code radius} around {@code at}: damage falling off to the edge, fire, a throw away from the middle (and on along {@code dir}). */
    static void explode(ServerPlayer user, ServerLevel level, Vec3 at, Vec3 dir, double radius, float damage, int fire, double knock) {
        Predicate<LivingEntity> foe = other -> FruitUtil.foe(user, other);
        DamageSource source = StyleCombat.of(user);
        for (LivingEntity body : level.m_6443_(LivingEntity.class, box(at, at).m_82400_(radius + 1.0),
            e -> e != user && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
            Vec3 middle = StyleCombat.middle(body);
            double d = middle.m_82554_(at);
            if (d > radius + body.m_20205_() * 0.5) {
                continue;
            }
            float share = (float) Math.max(0.35, 1.0 - d / (radius + 0.5));
            body.f_19802_ = 0;
            if (body.m_6469_(source, damage * share)) {
                if (fire > 0) {
                    body.m_20254_(fire);
                }
                Vec3 away = middle.m_82546_(at);
                away = away.m_82556_() < 1.0E-6 ? dir : away.m_82541_();
                Vec3 launch = away.m_82490_(knock * share).m_82549_(dir.m_82490_(0.3 * knock)).m_82520_(0.0, 0.35 * knock * share, 0.0);
                Impact.hit(user, body, StyleCombat.spec(knock > 1.5 ? com.hexhaki.piece.style.Hit.Weight.HEAVY
                    : com.hexhaki.piece.style.Hit.Weight.MEDIUM, launch));
            }
        }
    }

    /** Wind Force: a funnel of wind going forward, dragging everything it catches round and up along with it, flinging it at the end. */
    static final class Tornado implements Live {
        final ServerPlayer user;
        final ServerLevel level;
        final int id;
        final Vec3 step;
        final int life;
        final List<LivingEntity> caught = new ArrayList<>();
        Vec3 base;
        int age;

        Tornado(ServerPlayer user, ServerLevel level, int id, Vec3 base, Vec3 step, int life) {
            this.user = user;
            this.level = level;
            this.id = id;
            this.base = base;
            this.step = step;
            this.life = life;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            this.age++;
            this.base = this.base.m_82549_(this.step);
            Predicate<LivingEntity> foe = other -> FruitUtil.foe(this.user, other);
            AABB funnel = box(this.base.m_82520_(-WIND_HIGH, -1.0, -WIND_HIGH), this.base.m_82520_(WIND_HIGH, WIND_TALL, WIND_HIGH));
            DamageSource source = StyleCombat.of(this.user);
            for (LivingEntity body : this.level.m_6443_(LivingEntity.class, funnel, e -> e != this.user && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
                double h = Math.max(0.0, Math.min(WIND_TALL, body.m_20186_() - this.base.f_82480_));
                double radius = WIND_LOW + (WIND_HIGH - WIND_LOW) * h / WIND_TALL;
                double dx = body.m_20185_() - this.base.f_82479_;
                double dz = body.m_20189_() - this.base.f_82481_;
                double r = Math.sqrt(dx * dx + dz * dz);
                if (r > radius + 0.6) {
                    continue;
                }
                if (!this.caught.contains(body)) {
                    this.caught.add(body);
                }
                // round the axis (counter-clockwise from above), pulled to a ring inside it, rising, carried forward with the funnel
                double ring = Math.max(0.8, radius * 0.55);
                double ux = r < 1.0E-3 ? 1.0 : dx / r;
                double uz = r < 1.0E-3 ? 0.0 : dz / r;
                double spin = 0.55;
                double pull = (ring - r) * 0.35;
                double vx = -uz * spin + ux * pull + this.step.f_82479_;
                double vz = ux * spin + uz * pull + this.step.f_82481_;
                double vy = h < WIND_TALL - 1.5 ? 0.22 : 0.02;
                body.m_20334_(vx, vy, vz);
                body.f_19864_ = true;
                body.f_19789_ = 0.0F;
                if (this.age % 10 == 0) {
                    body.f_19802_ = 0;
                    body.m_6469_(source, 2.0F);
                }
            }
            if (this.age >= this.life) {
                Vec3 forward = this.step.m_82541_();
                for (LivingEntity body : this.caught) {
                    if (body.m_6084_()) {
                        body.m_20334_(forward.f_82479_ * 1.3, 0.7, forward.f_82481_ * 1.3);
                        body.f_19864_ = true;
                    }
                }
                return true;
            }
            return false;
        }
    }

    /** One of Dragon Embers' fireballs: up into the sky and down where it was aimed, bursting there. */
    static final class Ember implements Live {
        final ServerPlayer user;
        final ServerLevel level;
        final int id;
        final Vec3 land;
        final long when;

        Ember(ServerPlayer user, ServerLevel level, int id, Vec3 land, long when) {
            this.user = user;
            this.level = level;
            this.id = id;
            this.land = land;
            this.when = when;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (now < this.when) {
                return false;
            }
            explode(this.user, this.level, this.land.m_82520_(0.0, 0.5, 0.0), new Vec3(0, -1, 0), 3.2, 7.0F, 5, 1.0);
            Impact.quake(this.level, this.land, 12.0, 0.35F, 8);
            return true;
        }
    }

    /** Something done {@code when}. */
    static final class Later implements Live {
        final ServerLevel level;
        final long when;
        final Runnable action;

        Later(ServerLevel level, long when, Runnable action) {
            this.level = level;
            this.when = when;
            this.action = action;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        @Override
        public boolean tick(long now) {
            if (now < this.when) {
                return false;
            }
            this.action.run();
            return true;
        }
    }

    // ------------------------------------------------------------------------------------------------------ the clock

    @SubscribeEvent
    public static void uoTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        long now = level.m_46467_();
        if (!LIVE.isEmpty()) {
            List<Live> due = new ArrayList<>();
            for (Live live : LIVE) {
                if (live.level() == level) {
                    due.add(live);
                }
            }
            for (Live live : due) {
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
        if (!FLAMING.isEmpty()) {
            for (Iterator<Map.Entry<UUID, Flame>> it = FLAMING.entrySet().iterator(); it.hasNext(); ) {
                Flame flame = it.next().getValue();
                ServerPlayer player = flame.player();
                if (player.m_9236_() != level) {
                    continue;
                }
                long held = now - flame.start();
                if (DragonServer.phase(player) != DragonForm.DRAGON || held > FLAME_MAX || !player.m_6084_()) {
                    it.remove();
                    stopFlame(player);
                    continue;
                }
                breathe(player, held);
            }
        }
    }

    static boolean ready(ServerPlayer player, com.hexhaki.fruit.FruitMove move) {
        return FruitData.ready(player, move.cooldown());
    }

    static void cool(ServerPlayer player, com.hexhaki.fruit.FruitMove move, int ticks) {
        FruitData.cooldown(player, move.cooldown(), ticks);
        FruitServer.sync(player);
    }

    /** The player's right (flat). */
    static Vec3 right(ServerPlayer player) {
        Vec3 look = player.m_20154_();
        Vec3 r = new Vec3(-look.f_82481_, 0.0, look.f_82479_);
        return r.m_82556_() < 1.0E-6 ? new Vec3(1, 0, 0) : r.m_82541_();
    }
}
