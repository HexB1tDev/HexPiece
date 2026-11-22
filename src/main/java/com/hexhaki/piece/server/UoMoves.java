package com.hexhaki.piece.server;

import com.hexhaki.fruit.FruitData;
import com.hexhaki.fruit.FruitServer;
import com.hexhaki.fruit.FruitUtil;
import com.hexhaki.fx.Impact;
import com.hexhaki.gameplay.HakiServerController;
import com.hexhaki.piece.dragon.DragonForm;
import com.hexhaki.piece.dragon.UoSounds;
import com.hexhaki.piece.dragon.UoFire;
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
            if (slot == UoKit.CHARGED_BLAST.slot() && ready(player, UoKit.CHARGED_BLAST) && !CHARGING.containsKey(player.m_20148_())
                && !burning(player)) {
                if (FLAMING.remove(player.m_20148_()) != null) {
                    stopFlame(player); // the breath gives way to the blast gathering
                }
                CHARGING.put(player.m_20148_(), now);
                // 2.9.0: its sound is every client's own now (client/dragon/UoFx: the build-up, then held while the key is)
                PieceNetwork.around(player, S2CUoFx.of(S2CUoFx.CHARGE_ON, 0, player.m_19879_()));
            } else if (slot == UoKit.GIGANTIC_BITE.slot()) {
                bite(player, now);
            }
        }
    }

    /** Whether {@code player}'s Blast Breath beam is burning now. */
    static boolean burning(ServerPlayer player) {
        for (Live live : LIVE) {
            if (live instanceof Beam beam && beam.user == player) {
                return true;
            }
        }
        return false;
    }

    /** Whether {@code player} is gathering the charged blast (no other move meanwhile). */
    public static boolean charging(ServerPlayer player) {
        return CHARGING.containsKey(player.m_20148_());
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
        // 2.7.8 (the owner: "both versions of blast breath are BEAMS ... the max charge of the beam should be ridiculously bigger"): the
        // ball let go as a beam of fire, its width, length and life growing with the charge
        beam(player, true, 2.5 + 11.5 * charge, 60.0 + 100.0 * charge, 30 + (int) (40 * charge), 4.0F + 8.0F * charge, 1.2 + 2.0 * charge,
            charge);
        DragonServer.sound(player, UoSounds.ROAR, 3.0F, 0.9F);
        DragonServer.sound(player, UoSounds.BEAM, 3.0F + charge, 0.85F);
    }

    /** M1 held in dragon form (client/dragon/DragonInput, C2SDragon): the fire breath on and off. */
    public static void flame(ServerPlayer player, boolean on) {
        UUID id = player.m_20148_();
        long now = player.m_9236_().m_46467_();
        if (on) {
            // 2.9.0 (the owner: "For some reason, you can use M1s while you're charging the blast breath"): not while the blast
            // gathers in the jaws, nor while its beam burns
            if (DragonServer.phase(player) != DragonForm.DRAGON || FLAMING.containsKey(id) || CHARGING.containsKey(id) || burning(player)
                || !ready(player, UoKit.FIRE_BREATH)) {
                return;
            }
            FLAMING.put(id, new Flame(player, now));
            PieceNetwork.around(player, S2CUoFx.of(S2CUoFx.FLAME_ON, 0, player.m_19879_()));
            DragonServer.sound(player, UoSounds.FLAME_START, 2.2F, 0.9F);
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
                DragonServer.sound(player, UoSounds.AIR, 1.8F, 1.0F);
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
            // 2.7.8 (the owner: "both versions of blast breath are BEAMS, not short blasts"): a beam of fire out of the mouth
            beam(player, false, HUMAN_BEAM_WIDTH, HUMAN_BEAM_LENGTH, HUMAN_BEAM_LIFE, 3.0F, 1.0, 0.0F);
            DragonServer.sound(player, UoSounds.BEAM, 2.2F, 1.15F);
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
            DragonServer.sound(player, UoSounds.TORNADO, 2.2F, 1.0F);
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
            DragonServer.sound(player, UoSounds.FIREBALL, 2.0F, 0.95F);
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

    /**
     * 2.7.7 (the owner, in first person: "the fire and projectiles are coming from above"): what the player aims at, the first foe or
     * block on the eye's line (the crosshair), or that line's end {@code range} out. The dragon's mouth is well over the eye, so its
     * fire is aimed from the mouth onto this point ({@link #toward}), never along a line parallel to the look and over the crosshair.
     */
    static Vec3 aimed(ServerPlayer player, double range) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 eye = player.m_20299_(1.0F);
        Vec3 end = eye.m_82549_(player.m_20154_().m_82490_(range));
        BlockHitResult wall = level.m_45547_(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 to = wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_() : end;
        Predicate<LivingEntity> foe = other -> FruitUtil.foe(player, other);
        double best = 1.0;
        for (LivingEntity body : level.m_6443_(LivingEntity.class, box(eye, to).m_82400_(1.0),
            e -> e != player && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
            double along = WaterServer.closest(eye, to, StyleCombat.middle(body));
            Vec3 point = eye.m_82549_(to.m_82546_(eye).m_82490_(along));
            if (along < best && body.m_20191_().m_82400_(0.6).m_82390_(point)) {
                best = along;
            }
        }
        return eye.m_82549_(to.m_82546_(eye).m_82490_(best));
    }

    /** The way (unit) from {@code from} (the mouth) onto what the player aims at; the look when that is too near or behind to turn to. */
    static Vec3 toward(ServerPlayer player, Vec3 from, double range) {
        Vec3 look = player.m_20154_();
        Vec3 way = aimed(player, range).m_82546_(from);
        double length = way.m_82553_();
        if (length < 6.0) {
            return look;
        }
        Vec3 unit = way.m_82490_(1.0 / length);
        return unit.m_82526_(look) < 0.5 ? look : unit;
    }

    static void bite(ServerPlayer player, long now) {
        if (!ready(player, UoKit.GIGANTIC_BITE) || CHARGING.containsKey(player.m_20148_()) || burning(player)) {
            return;
        }
        cool(player, UoKit.GIGANTIC_BITE, BITE_COOL);
        Vec3 look = player.m_20154_();
        player.m_20334_(look.f_82479_ * 2.2, look.f_82480_ * 2.2 + 0.1, look.f_82481_ * 2.2);
        player.f_19864_ = true;
        PieceNetwork.around(player, S2CUoFx.of(S2CUoFx.BITE, 0, player.m_19879_()));
        DragonServer.sound(player, UoSounds.LUNGE, 2.2F, 0.9F);
        DragonServer.sound(player, UoSounds.ROAR, 2.4F, 1.15F);
        ServerLevel level = (ServerLevel) player.m_9236_();
        LIVE.add(new Later(level, now + 7, () -> {
            if (!player.m_6084_() || DragonServer.phase(player) != DragonForm.DRAGON) {
                return;
            }
            Vec3 jaws = mouth(player).m_82549_(toward(player, mouth(player), 12.0).m_82490_(3.0));
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
            DragonServer.sound(player, UoSounds.BITE, 2.6F, 0.75F);
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
        if (ticks % 5 != 0) {
            return; // it burns every fifth tick: nothing to work out between
        }
        ServerLevel level = (ServerLevel) player.m_9236_();
        Vec3 from = mouth(player);
        Vec3 look = toward(player, from, FLAME_RANGE);
        Vec3 to = from.m_82549_(look.m_82490_(FLAME_RANGE));
        BlockHitResult wall = level.m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double reach = wall.m_6662_() == HitResult.Type.BLOCK ? wall.m_82450_().m_82554_(from) : FLAME_RANGE;
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
            com.hexhaki.piece.PieceSounds.playAt(this.user, at.f_82479_, at.f_82480_, at.f_82481_,
                this.kind == S2CUoFx.AIR ? UoSounds.AIR_HIT : this.kind == S2CUoFx.BLAST ? UoSounds.BLAST : UoSounds.EXPLODE,
                this.kind == S2CUoFx.BLAST ? 3.0F : 1.8F, this.kind == S2CUoFx.BLAST ? 0.9F : 1.0F);
        }
    }

    /** {@code radius} around {@code at}: damage falling off to the edge, fire, a throw away from the middle (and on along {@code dir}). */
    static void explode(ServerPlayer user, ServerLevel level, Vec3 at, Vec3 dir, double radius, float damage, int fire, double knock) {
        ConquerorAura.exploded(user, level, at, radius);
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
            if (this.age % 80 == 0 && this.age + 20 < this.life) {
                // 2.8.6: a long-travelling tornado howls again where it is (each clip 4.5 s)
                com.hexhaki.piece.PieceSounds.playAt(this.user, this.base.f_82479_, this.base.f_82480_ + 2.0, this.base.f_82481_, UoSounds.TORNADO,
                    2.0F, 1.0F);
            }
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
            com.hexhaki.piece.PieceSounds.playAt(this.user, this.land.f_82479_, this.land.f_82480_ + 0.5, this.land.f_82481_, UoSounds.EMBER_HIT,
                1.8F, 0.9F + 0.2F * (this.id % 3) / 2.0F);
            return true;
        }
    }

    /** The human form's beam of fire (Blast Breath): its radius, length (blocks), life (ticks). */
    static final double HUMAN_BEAM_WIDTH = 1.4;
    static final double HUMAN_BEAM_LENGTH = 50.0;
    static final int HUMAN_BEAM_LIFE = 30;

    /** Where the human form's mouth is: just under the eye, out along the look. */
    static Vec3 humanMouth(ServerPlayer player) {
        return player.m_146892_().m_82549_(player.m_20154_().m_82490_(0.5)).m_82520_(0.0, -0.12, 0.0);
    }

    /** A beam of fire out of the mouth ({@code dragon}'s or the human's): sent to every client, burning on the server ({@link Beam}). */
    static void beam(ServerPlayer player, boolean dragon, double width, double length, int life, float damage, double knock, float charge) {
        ServerLevel level = (ServerLevel) player.m_9236_();
        LIVE.add(new Beam(player, level, dragon, width, length, life, damage, knock, charge));
        // 2.8.7 (the owner: the dragon held still while it burns, "it should yes"): stopped where it is (its client holds it there)
        player.m_20334_(0.0, 0.0, 0.0);
        player.f_19864_ = true;
        // 2.8.5: the aim the beam keeps (the look at release), sent with it: the user's own view is held on it until the beam ends
        PieceNetwork.around(player, new S2CUoFx(S2CUoFx.FIRE_BEAM, dragon ? 1 : 0, player.m_19879_(), player.m_146908_(), player.m_146909_(), 0,
            (float) length, 0, 0, 0, 0, 0, (float) width, life));
    }

    /**
     * A beam of fire held for {@code life} ticks, straight from the mouth onto what the player aimed at when they let go (2.8.5, the
     * owner: "whenever the blast breath is being charged, you can freely aim it, but when you let go, your aim is instantly locked into
     * that position without being able to move until it's over. It's the drawback of that move": the beam keeps that aim, and the
     * user's view is held on it, client/dragon/AimLock), as far
     * as the first block within {@code length}: every 4 ticks it burns everything within its width of that line (thin at the mouth,
     * UoFire.taper); the moment it strikes a block it blows up there and tears a crater out (2.7.8, the owner: "as soon as it hits a
     * block, it explodes", "it should also destroy blocks and self repair after 30 seconds": Craters), and goes on blowing up and
     * tearing every 6 ticks while it burns there; when it ends, a last explosion and crater as big as the charge.
     */
    static final class Beam implements Live {
        final ServerPlayer user;
        final ServerLevel level;
        final boolean dragon;
        final double width;
        final double length;
        final int life;
        final float damage;
        final double knock;
        final float charge;
        int age;
        boolean struck;
        Vec3 last;
        /** The way it burns, fixed when it was let go. */
        final Vec3 aim;

        Beam(ServerPlayer user, ServerLevel level, boolean dragon, double width, double length, int life, float damage, double knock,
             float charge) {
            this.aim = toward(user, dragon ? mouth(user) : humanMouth(user), length);
            this.user = user;
            this.level = level;
            this.dragon = dragon;
            this.width = width;
            this.length = length;
            this.life = life;
            this.damage = damage;
            this.knock = knock;
            this.charge = charge;
        }

        @Override
        public ServerLevel level() {
            return this.level;
        }

        /** The last explosion, as big as the charge: where the beam last struck (or its end in the air). */
        void last() {
            if (this.last == null) {
                return;
            }
            double radius = this.dragon ? 6.0 + 18.0 * this.charge : 4.0;
            explode(this.user, this.level, this.last, new Vec3(0, 1, 0), radius, this.damage * 4.0F, 8, this.knock * 1.5);
            blowAway(this.user, this.level, this.last, radius * 1.15, this.dragon ? 3.0 + 4.0 * this.charge : 2.2);
            // 2.9.0 (the owner: "when it explodes, it should have a big explosion, like a fucking big bomb that just went off", "the main
            // block destruction should come from the explosion and residue"): the dragon's ends in the bomb (its cloud and burning
            // ground last 9 s, UoFire.bomb), the crater the beam's own is torn out here, and the ground round it goes on blowing up
            double crater = this.dragon ? BOMB_CRATER + BOMB_CRATER_CHARGE * this.charge : 2.0;
            PieceNetwork.around(this.user, new S2CUoFx(S2CUoFx.BURST, 0, this.user.m_19879_(), this.last.f_82479_, this.last.f_82480_,
                this.last.f_82481_, 0, 1, 0, 0, 0, this.dragon ? S2CUoFx.BOMB : S2CUoFx.BLAST, (float) (this.dragon ? crater : radius), 0));
            Impact.quake(this.level, this.last, radius * 5.0 + 16.0, (float) Math.min(1.0, 0.5 + radius / 14.0), 30);
            if (this.struck) {
                Craters.open(this.level, this.user, this.last, crater);
            }
            com.hexhaki.piece.PieceSounds.playAt(this.user, this.last.f_82479_, this.last.f_82480_, this.last.f_82481_, UoSounds.BLAST,
                3.0F + 3.0F * this.charge, 0.95F - 0.15F * this.charge);
            if (this.dragon) {
                aftershocks(this.user, this.level, this.last, crater, this.damage, this.charge);
            }
        }

        @Override
        public boolean tick(long now) {
            this.age++;
            boolean gone = !this.user.m_6084_() || this.user.m_9236_() != this.level
                || this.dragon && DragonServer.phase(this.user) != DragonForm.DRAGON;
            if (gone || this.age > this.life) {
                if (gone) {
                    PieceNetwork.around(this.user, S2CUoFx.of(S2CUoFx.FIRE_BEAM_OFF, 0, this.user.m_19879_()));
                } else {
                    this.last();
                }
                return true;
            }
            // the front races out (UoFire.BEAM_SPEED blocks a tick): nothing past it is reached yet
            double front = this.age * UoFire.BEAM_SPEED + 2.0;
            Vec3 from = this.dragon ? mouth(this.user) : humanMouth(this.user);
            Vec3 way = this.aim;
            double out = Math.min(this.length, front);
            Vec3 end = from.m_82549_(way.m_82490_(out));
            BlockHitResult wall = this.level.m_45547_(new ClipContext(from, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.user));
            boolean hit = wall.m_6662_() == HitResult.Type.BLOCK;
            double reach = hit ? wall.m_82450_().m_82554_(from) : out;
            Vec3 to = from.m_82549_(way.m_82490_(reach));
            this.last = to;
            if (hit && !this.struck) {
                // the moment it strikes a block: it blows up there
                this.struck = true;
                double radius = this.width * 1.5 + 2.0;
                explode(this.user, this.level, to, way, radius, this.damage * 2.0F, 8, this.knock);
                blowAway(this.user, this.level, to, radius, this.dragon ? 2.4 + 3.0 * this.charge : 1.8);
                PieceNetwork.around(this.user, new S2CUoFx(S2CUoFx.BURST, 0, this.user.m_19879_(), to.f_82479_, to.f_82480_, to.f_82481_,
                    (float) way.f_82479_, (float) way.f_82480_, (float) way.f_82481_, 0, 0, S2CUoFx.BLAST, (float) radius, 0));
                Craters.open(this.level, this.user, to, STRIKE_CRATER + this.width * 0.1);
                Impact.quake(this.level, to, radius * 4.0 + 10.0, (float) Math.min(1.0, 0.3 + this.width / 10.0), 12);
                com.hexhaki.piece.PieceSounds.playAt(this.user, to.f_82479_, to.f_82480_, to.f_82481_, UoSounds.EXPLODE, 2.5F + this.charge,
                    0.85F);
            }
            if (this.age % 4 != 0 && this.age % 6 != 0) {
                return false;
            }
            if (this.age % 4 == 0) {
                Predicate<LivingEntity> foe = other -> FruitUtil.foe(this.user, other);
                DamageSource source = StyleCombat.of(this.user);
                for (LivingEntity body : this.level.m_6443_(LivingEntity.class, box(from, to).m_82400_(this.width + 1.0),
                    e -> e != this.user && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
                    Vec3 rel = StyleCombat.middle(body).m_82546_(from);
                    double along = rel.m_82526_(way);
                    if (along < 0.0 || along > reach + 1.0 || rel.m_82546_(way.m_82490_(along)).m_82553_()
                        > this.width * UoFire.taper(along, this.width) * 0.9 + body.m_20205_() * 0.5) {
                        continue;
                    }
                    body.f_19802_ = 0;
                    if (body.m_6469_(source, this.damage)) {
                        body.m_20254_(6);
                        Impact.hit(this.user, body, StyleCombat.spec(com.hexhaki.piece.style.Hit.Weight.MEDIUM,
                            way.m_82490_(0.5 * this.knock).m_82520_(0.0, 0.2, 0.0)));
                    }
                }
            }
            if (this.age % 6 == 0 && hit) {
                explode(this.user, this.level, to, way, this.width * 1.3 + 1.5, this.damage * 1.2F, 6, this.knock);
                // 2.9.0 (the owner: "the fucking blast breath beam is destroying blocks like way too much"): while it burns it only
                // scorches a little deeper (a 12-block ball every 6 ticks before); the crater is the bomb's at its end
                if (this.age % 18 == 0) {
                    Craters.open(this.level, this.user, to, BURN_CRATER);
                }
                if (this.age % 12 == 0) {
                    Impact.quake(this.level, to, this.width * 5.0 + 10.0, (float) Math.min(1.0, 0.3 + this.width / 10.0), 12);
                }
            }
            return false;
        }
    }

    /** 2.9.0: the craters: where the beam first strikes, what it burns deeper while it holds, the bomb's at its end (and per charge). */
    static final double STRIKE_CRATER = 1.6;
    static final double BURN_CRATER = 1.4;
    static final double BOMB_CRATER = 5.0;
    static final double BOMB_CRATER_CHARGE = 8.0;
    /** The ground round the bomb's crater going on blowing up: how many, over how long (ticks). */
    static final int AFTERSHOCKS = 6;
    static final int AFTERSHOCK_SPAN = 70;

    /**
     * 2.9.0 (the owner: "There's like no residue explosion"): after the bomb, the ground round its crater goes on blowing up, one blast
     * after another for some 3.5 s: each a smaller explosion of fire (dragon/UoFire.explosion), a burn and a throw, its own boom.
     */
    static void aftershocks(ServerPlayer user, ServerLevel level, Vec3 at, double crater, float damage, float charge) {
        long now = level.m_46467_();
        int n = AFTERSHOCKS + (int) (4 * charge);
        for (int i = 0; i < n; i++) {
            long when = now + 10 + (long) ((i + level.m_213780_().m_188500_() * 0.8) * AFTERSHOCK_SPAN / n);
            double a = level.m_213780_().m_188500_() * Math.PI * 2.0;
            double d = crater * (0.5 + 0.6 * level.m_213780_().m_188500_());
            Vec3 spot = at.m_82520_(Math.cos(a) * d, 0.5, Math.sin(a) * d);
            double size = 2.2 + 2.6 * charge * (0.6 + 0.4 * level.m_213780_().m_188500_());
            LIVE.add(new Later(level, when, () -> {
                explode(user, level, spot, new Vec3(0, 1, 0), size * 1.3, damage * 0.8F, 6, 1.0);
                PieceNetwork.around(user, new S2CUoFx(S2CUoFx.BURST, 0, user.m_19879_(), spot.f_82479_, spot.f_82480_, spot.f_82481_, 0, 1,
                    0, 0, 0, S2CUoFx.BLAST, (float) size, 0));
                com.hexhaki.piece.PieceSounds.playAt(user, spot.f_82479_, spot.f_82480_, spot.f_82481_, UoSounds.EXPLODE, 2.0F,
                    0.9F + level.m_213780_().m_188501_() * 0.25F);
            }));
        }
    }

    /** 2.7.8: how long a beam's explosion stuns (ticks): the owner's "stun for 10 seconds (ragdoll)". */
    static final int BLOWN_STUN = 200;

    /**
     * 2.7.8 (the owner: "the explosion should also cause massive knockback, and stun for 10 seconds (ragdoll)"): every foe within
     * {@code radius} of {@code at} is thrown hard away from it (a rag doll through the air: Impact's heavy throw), and as it comes
     * down knocked over and kept down, stunned, for the rest of {@link #BLOWN_STUN} ticks (HakiFalls: it falls over, lies there, gets
     * up).
     */
    static void blowAway(ServerPlayer user, ServerLevel level, Vec3 at, double radius, double speed) {
        Predicate<LivingEntity> foe = other -> FruitUtil.foe(user, other);
        long start = level.m_46467_();
        for (LivingEntity body : level.m_6443_(LivingEntity.class, box(at, at).m_82400_(radius + 1.0),
            e -> e != user && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
            Vec3 rel = StyleCombat.middle(body).m_82546_(at);
            double d = rel.m_82553_();
            if (d > radius + body.m_20205_() * 0.5) {
                continue;
            }
            double share = Math.max(0.45, 1.0 - d / (radius + 1.0));
            Vec3 away = new Vec3(rel.f_82479_, 0.0, rel.f_82481_);
            away = away.m_82556_() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : away.m_82541_();
            Vec3 thrown = away.m_82490_(speed * share).m_82520_(0.0, 0.6 + 0.35 * speed * share, 0.0);
            Impact.hit(user, body, StyleCombat.spec(com.hexhaki.piece.style.Hit.Weight.HEAVY, thrown));
            // knocked down where it lands (or after two seconds, if it never does), for the rest of the ten seconds
            LIVE.add(new Live() {
                int age;

                @Override
                public boolean tick(long now) {
                    this.age++;
                    if (!body.m_6084_()) {
                        return true;
                    }
                    if (this.age > 4 && body.m_20096_() || this.age >= 40) {
                        HakiFalls.knockDown(level, body, (int) Math.max(40L, BLOWN_STUN - (now - start)), false);
                        return true;
                    }
                    return false;
                }

                @Override
                public ServerLevel level() {
                    return level;
                }
            });
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
                if (held >= 40 && (held - 40) % 50 == 0) {
                    DragonServer.sound(player, UoSounds.FLAME_LOOP, 2.0F, 0.9F); // 2.8.6: its roar held while it burns
                }
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
