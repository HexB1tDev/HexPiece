package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitData;
import com.hexhaki.piece.hie.HieSounds;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * A body frozen solid by the Hie Hie no Mi. 3.2.2 (the owner: "the ice sometimes melts" and "I think like the entity should be like in a
 * crystal like you had before that then shatters"): no block of the world changes any more (3.1.9's real ice blocks melted into water by
 * any light); every client draws the body held in hie/HieArt's cut crystal (client/hie/HieFx), cracking over its last
 * {@link #CRACK_TICKS} ticks and shattering into crystal shards. It is held exactly where it froze (put back if it drifts, no gravity, its
 * legs slowed to nothing), it strikes nothing (its blows and shots cancelled, as a Water Prison's). When its time is up, or once it has
 * taken {@link #BREAKS} damage, or the next time the one who froze it strikes it ("when you hit them again, you can shatter them"), it
 * shatters with a last bite of damage.
 * <p>
 * The frost (the owner: "imagine whenever an entity punches you that doesn't have Haki on, their limbs that they hit with like turn blue"
 * and "whenever you left-click someone. You should be able to, like, freeze them slowly, and when we punch them a certain number of times,
 * they get completely frozen, and when you hit them again, you can shatter them"): every cold hit ({@link #frost}) leaves the body bluer
 * and slower; the {@link #FROST_FULL}th within {@link #FROST_KEEP} ticks of the last freezes it solid. The Ice fruit's own punches in fruit
 * mode frost what they hit ({@link #punch}); a bare-handed blow on the body of ice frosts the attacker from the arm (server/HieMoves.dodged).
 */
@Mod.EventBusSubscriber(modid = "hexhaki")
public final class HieFreeze {
    /** A body frozen: whose ice, where it is held, until when, inside an Ice Ball (that client draws the ball, not a crystal), damage taken. */
    static final class Hold {
        final ServerLevel level;
        final ServerPlayer owner;
        final LivingEntity body;
        final Vec3 at;
        final long until;
        final boolean ball;
        /** The tick it froze (its freezer's next blow smashes it only a few ticks after: never the blow that froze it). */
        final long since;
        float taken;
        /** Broken by the damage it took: it shatters on the next tick (never inside the blow's own damage). */
        boolean broken;
        /** Struck by the one who froze it: it shatters on the next tick with {@link #SMASH_DAMAGE}. */
        boolean smashed;

        Hold(ServerLevel level, ServerPlayer owner, LivingEntity body, Vec3 at, long until, boolean ball) {
            this.level = level;
            this.owner = owner;
            this.body = body;
            this.at = at;
            this.until = until;
            this.ball = ball;
            this.since = level.m_46467_();
        }
    }

    /** Damage taken inside the ice that breaks it early. */
    static final float BREAKS = 14.0F;
    /** The shatter's last bite, and the bite when its freezer smashes it. */
    static final float SHATTER_DAMAGE = 4.0F;
    static final float SMASH_DAMAGE = 8.0F;
    /** A held body that drifts this far (blocks) is put back. */
    static final double DRIFT = 0.12;
    /** The most bodies frozen at once (the oldest thaws first). */
    static final int MOST = 48;
    /** The frost: how many cold hits freeze a body solid, for how long (ticks), how long the frost stays without another hit. */
    public static final int FROST_FULL = 4;
    public static final int FROST_FREEZE_TICKS = 80;
    public static final int FROST_KEEP = 100;
    /** One punch's frost on one body at most this often (ticks): clicking fast does not freeze anyone in a second. */
    static final int PUNCH_EVERY = 8;
    /** Its freezer's blow smashes a frozen body only this many ticks after it froze. */
    static final int SMASH_AFTER = 4;
    /** The crystal cracks over a frozen body's last this many ticks (hie/HieArt.CRACK_TIME draws it). */
    public static final int CRACK_TICKS = 24;
    /** After it thaws, a body is not frozen again by a lasting field (Ice Age's) for this many ticks. */
    public static final int GRACE = 20;

    private static final Map<Integer, Hold> HELD = new HashMap<>();
    /** Each frosted body's frost: {hits, the tick of the last, the tick of the last punch}. */
    private static final Map<Integer, long[]> FROST = new HashMap<>();
    /** The tick each body last thawed. */
    private static final Map<Integer, Long> THAWED = new HashMap<>();

    private HieFreeze() {
    }

    /** Whether {@code body} is frozen solid now. */
    public static boolean frozen(Entity body) {
        return body != null && HELD.containsKey(body.m_19879_());
    }

    /** Whether {@code body} thawed within the last {@link #GRACE} ticks. */
    static boolean graced(Entity body, long now) {
        Long at = THAWED.get(body.m_19879_());
        return at != null && now - at < GRACE;
    }

    /**
     * {@code owner} freezes {@code body} solid for {@code ticks} where it is ({@code ball}: inside an Ice Ball, which the clients draw in
     * its place). A body already frozen is held longer (never shorter). Returns whether it froze.
     */
    public static boolean freeze(ServerPlayer owner, LivingEntity body, int ticks, boolean ball) {
        if (!(body.m_9236_() instanceof ServerLevel level) || !body.m_6084_() || ticks <= 0) {
            return false;
        }
        long now = level.m_46467_();
        Hold old = HELD.get(body.m_19879_());
        if (old != null && old.until >= now + ticks) {
            return false;
        }
        if (old == null && HELD.size() >= MOST) {
            Hold oldest = null;
            for (Hold h : HELD.values()) {
                if (oldest == null || h.until < oldest.until) {
                    oldest = h;
                }
            }
            if (oldest != null) {
                HELD.remove(oldest.body.m_19879_());
                thaw(oldest, false);
            }
        }
        Vec3 at = old != null ? old.at : body.m_20182_();
        body.m_20242_(true);
        body.m_20256_(Vec3.f_82478_);
        boolean inBall = ball || old != null && old.ball;
        HELD.put(body.m_19879_(), new Hold(level, owner, body, at, now + ticks, inBall));
        FROST.remove(body.m_19879_());
        PieceNetwork.around(body, new S2CUoFx(HieMoves.FREEZE, body.m_19879_(), owner.m_19879_(), at.f_82479_, at.f_82480_, at.f_82481_, 0, 0, 0,
            body.m_20206_(), inBall ? 1.0F : 0.0F, 0.0F, (float) (body.m_20205_() * 0.5), ticks));
        if (old == null) {
            HieMoves.sound(owner, at.m_82520_(0, body.m_20206_() * 0.5, 0), HieSounds.FREEZE, 1.1F, 0.9F + level.m_213780_().m_188501_() * 0.2F);
        }
        return true;
    }

    /**
     * {@code hits} cold hits on {@code body} by {@code owner}: the body bluer and slower with each (every client draws hie/HieArt.frost
     * over it, from the arm up when {@code fromArm}: an attacker's own blow); the {@link #FROST_FULL}th within {@link #FROST_KEEP} ticks
     * of the last freezes it solid for {@link #FROST_FREEZE_TICKS}. Returns whether it froze.
     */
    public static boolean frost(ServerPlayer owner, LivingEntity body, int hits, boolean fromArm) {
        if (!(body.m_9236_() instanceof ServerLevel level) || !body.m_6084_() || frozen(body) || hits <= 0) {
            return false;
        }
        long now = level.m_46467_();
        long[] cold = FROST.get(body.m_19879_());
        if (cold == null || now - cold[1] > FROST_KEEP) {
            cold = new long[] {0, now, now - PUNCH_EVERY};
            if (FROST.size() > 256) {
                FROST.values().removeIf(c -> now - c[1] > FROST_KEEP);
            }
            FROST.put(body.m_19879_(), cold);
        }
        cold[0] += hits;
        cold[1] = now;
        if (cold[0] >= FROST_FULL) {
            return freeze(owner, body, FROST_FREEZE_TICKS, false);
        }
        body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, FROST_KEEP, (int) cold[0] - 1, false, false, false));
        Vec3 feet = body.m_20182_();
        PieceNetwork.around(body, new S2CUoFx(HieMoves.CHILL, body.m_19879_(), owner.m_19879_(), feet.f_82479_, feet.f_82480_, feet.f_82481_, 0, 0, 0,
            fromArm ? 1.0F : 0.0F, body.m_20206_(), 0.0F, cold[0] / (float) FROST_FULL, FROST_KEEP));
        HieMoves.sound(owner, feet.m_82520_(0, body.m_20206_() * 0.6, 0), HieSounds.CRACK, 0.45F, 1.25F + 0.1F * cold[0]);
        return false;
    }

    /** The ice round a held body goes ({@code shatter}: it shatters, with its last bite and its sound; else it only fades away). */
    static void thaw(Hold hold, boolean shatter) {
        LivingEntity body = hold.body;
        body.m_20242_(false);
        THAWED.put(body.m_19879_(), hold.level.m_46467_());
        if (THAWED.size() > 256) {
            long now = hold.level.m_46467_();
            THAWED.values().removeIf(t -> now - t > GRACE);
        }
        Vec3 middle = body.m_20182_().m_82520_(0, body.m_20206_() * 0.5, 0);
        PieceNetwork.around(body, new S2CUoFx(HieMoves.THAW, body.m_19879_(), hold.owner.m_19879_(), middle.f_82479_, middle.f_82480_,
            middle.f_82481_, 0, 0, 0, 0, shatter ? 1.0F : 0.0F, hold.ball && !hold.smashed ? 1.0F : 0.0F, (float) Math.max(0.9, body.m_20206_() * 0.7), 0));
        if (shatter && body.m_6084_()) {
            if (hold.owner.m_6084_()) {
                HieMoves.hurt(hold.owner, body, hold.smashed ? SMASH_DAMAGE : SHATTER_DAMAGE);
            }
            if (!hold.ball || hold.smashed) {
                HieMoves.sound(hold.owner, middle, HieSounds.SHATTER, hold.smashed ? 1.2F : 0.9F, 0.95F + hold.level.m_213780_().m_188501_() * 0.2F);
            }
        }
    }

    /** Every frozen body let go at once (the server stopping). */
    static void clear() {
        for (Hold h : new ArrayList<>(HELD.values())) {
            h.body.m_20242_(false);
        }
        HELD.clear();
        FROST.clear();
        THAWED.clear();
    }

    @SubscribeEvent
    public static void freezeTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || HELD.isEmpty()) {
            return;
        }
        long now = level.m_46467_();
        List<Hold> done = new ArrayList<>();
        for (Hold hold : HELD.values()) {
            if (hold.level != level) {
                continue;
            }
            LivingEntity body = hold.body;
            if (!body.m_6084_() || body.m_213877_() || body.m_9236_() != level || now >= hold.until || hold.broken || hold.smashed) {
                done.add(hold);
                continue;
            }
            // frozen where it stands: put back if it drifts, no motion, its legs slowed to nothing, nothing it strikes lands
            if (body.m_20182_().m_82554_(hold.at) > DRIFT) {
                body.m_6021_(hold.at.f_82479_, hold.at.f_82480_, hold.at.f_82481_);
            }
            body.m_20256_(Vec3.f_82478_);
            body.f_19789_ = 0.0F;
            body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 3, 9, false, false, false));
            Clashes.stagger(body, now, 3);
            // the crystal starts to crack (every client draws the cracks running over it)
            if (!hold.ball && hold.until - now == CRACK_TICKS) {
                HieMoves.sound(hold.owner, hold.at.m_82520_(0, body.m_20206_() * 0.5, 0), HieSounds.CRACK, 0.9F, 1.0F);
            }
        }
        for (Hold hold : done) {
            HELD.remove(hold.body.m_19879_());
            thaw(hold, hold.body.m_6084_());
        }
    }

    /** Damage dealt to a frozen body adds up; enough breaks the ice early (it shatters). */
    @SubscribeEvent
    public static void freezeHurt(LivingHurtEvent event) {
        if (HELD.isEmpty()) {
            return;
        }
        Hold hold = HELD.get(event.getEntity().m_19879_());
        if (hold == null) {
            return;
        }
        hold.taken += event.getAmount();
        if (hold.taken >= BREAKS) {
            hold.broken = true;
        }
    }

    /**
     * {@code owner} strikes {@code body} on purpose (a punch, a spear, a cut of the saber): if it froze that body at least
     * {@link #SMASH_AFTER} ticks ago, the crystal shatters on the next tick with {@link #SMASH_DAMAGE} ("when you hit them again, you can
     * shatter them"). Returns whether it will.
     */
    public static boolean smash(ServerPlayer owner, LivingEntity body) {
        Hold hold = HELD.get(body.m_19879_());
        if (hold == null || hold.owner != owner || hold.level.m_46467_() < hold.since + SMASH_AFTER) {
            return false;
        }
        hold.smashed = true;
        return true;
    }

    /** A frozen body strikes nothing (its blows, and what it shoots). */
    @SubscribeEvent
    public static void freezeAttack(LivingAttackEvent event) {
        if (HELD.isEmpty()) {
            return;
        }
        Entity attacker = event.getSource().m_7639_();
        if (attacker != null && attacker != event.getEntity() && HELD.containsKey(attacker.m_19879_())) {
            event.setCanceled(true);
        }
    }

    /**
     * The Ice fruit's punch in fruit mode (a real left click on a body: the game's own punch, client/hie/HieFx gives it back): what it hits
     * is frosted one hit (at most every {@link #PUNCH_EVERY} ticks a body); a body this player froze is smashed instead.
     */
    @SubscribeEvent
    public static void punch(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getTarget() instanceof LivingEntity body) || !body.m_6084_()
            || FruitData.fruit(player) != Fruit.HIE || !FruitData.active(player) || !player.m_21205_().m_41619_() || HieMoves.saberOut(player)) {
            return;
        }
        if (HELD.containsKey(body.m_19879_())) {
            smash(player, body); // a frozen body: its freezer's punch smashes it (anyone else's damage adds up: freezeHurt)
            return;
        }
        long now = player.m_9236_().m_46467_();
        long[] cold = FROST.get(body.m_19879_());
        if (cold != null && now - cold[1] <= FROST_KEEP && now - cold[2] < PUNCH_EVERY) {
            return;
        }
        frost(player, body, 1, false);
        cold = FROST.get(body.m_19879_());
        if (cold != null) {
            cold[2] = now;
        }
    }
}
