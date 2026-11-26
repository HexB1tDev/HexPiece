package com.hexhaki.piece.server;

import com.hexhaki.fx.Impact;
import com.hexhaki.piece.PieceSounds;
import com.hexhaki.piece.clash.ClashRules;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CClash;
import com.hexhaki.piece.npc.SeaFighter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 2.3.6: blows meeting in the world ({@link ClashRules}; the owner: "every move is clashable ... I want a impact frame whenever a clash
 * happens", "to clash with swords man, any form of armament haki HAS TO BE ON, otherwise the sword cant be clashed and since it has
 * more range, will come up on top", "When 2 ryou hakis clash, both lightning colors explode outwards from the clash area in a sphere").
 *
 * <p>The moment a blow lands (a style move's hit, every tick of a cut's window, the charged cut, the quick draw's run; a player's or a
 * sea fighter's), {@link #landing} looks for a foe within reach, both facing each other, whose own blow is coming at it in its last
 * ticks ({@link ClashRules#onItsWay}). Found, the two meet instead of landing: both moves stop and play their recoil, both are staggered
 * (no blow for a moment, their legs slowed), both are held a few ticks where they stand (the original's hit-stop) and thrown apart, the
 * weaker further, a hard enough clash shakes the ground, everyone near hears it ({@link ClashRules#SOUND}, and steel on steel when a
 * blade is in it) and sees it: the impact frame and the burst ({@code client/fx/ClashFx}), both Ryou colours tearing out in a sphere when
 * two Ryou users meet. A bare blow without Armament that a blade is coming at is lost, cut off where it is; the blade lands as it would.
 */
public final class Clashes {
    /** Who is staggered by a clash, by entity id: until which game tick. */
    private static final Map<Integer, Long> STAGGERED = new HashMap<>();
    /** How far past the blow's own reach others are looked for (the longest blow one could be throwing back). */
    static final double LOOK = 4.5;
    /** Bodies this far from where the blows met (blocks) are held and thrown by it; farther (a shooter far off) their move just stops. */
    static final double NEAR = 4.5;
    /** Most height between the two (blocks) for their blows to meet. */
    static final double HEIGHT = 2.6;
    private static int next;

    private Clashes() {
    }

    /** True while {@code body} is staggered by a clash: no blow, no new move. */
    public static boolean staggered(LivingEntity body, long now) {
        Long until = STAGGERED.get(body.m_19879_());
        return until != null && now < until;
    }

    /**
     * {@code user}'s blow lands {@code now} (its hit, or its cut sweeping this tick); {@code foe} is who it may hurt. True if it met a
     * foe's blow coming back at it instead (the clash played): the caller lands nothing. A bare blow without Armament meeting a blade
     * coming at it is lost: true too, the bare move cut off. A blade meeting such a bare blow cuts that move off and lands (false).
     */
    public static boolean landing(LivingEntity user, long now, Predicate<LivingEntity> foe) {
        if (!(user.m_9236_() instanceof ServerLevel level) || !user.m_6084_()) {
            return false;
        }
        ClashRules.Blow mine = blowOf(user, now);
        if (mine == null || !ClashRules.onItsWay(mine, now)) {
            return false;
        }
        Vec3 look = StyleCombat.facing(user);
        LivingEntity met = null;
        ClashRules.Blow theirs = null;
        double nearest = Double.MAX_VALUE;
        for (LivingEntity other : level.m_6443_(LivingEntity.class, user.m_20191_().m_82400_(mine.reach() + LOOK + ClashRules.SLACK),
            e -> e != user && e.m_6084_() && !e.m_5833_() && foe.test(e))) {
            ClashRules.Blow blow = blowOf(other, now);
            if (blow == null) {
                continue;
            }
            double dx = other.m_20185_() - user.m_20185_();
            double dz = other.m_20189_() - user.m_20189_();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (Math.abs(other.m_20186_() - user.m_20186_()) > HEIGHT || distance >= nearest) {
                continue;
            }
            Vec3 back = StyleCombat.facing(other);
            boolean facing = ClashRules.faces(look.f_82479_, look.f_82481_, dx, dz) && ClashRules.faces(back.f_82479_, back.f_82481_, -dx, -dz);
            if (!facing || !ClashRules.onItsWay(blow, now) || !ClashRules.reaches(mine, blow, distance)) {
                continue;
            }
            if (ClashRules.bladeWins(mine, blow)) {
                // the owner: without Armament the blade cannot be clashed, and with its reach it comes up on top
                if (mine.blade()) {
                    stop(other, now, false);
                    continue;
                }
                stop(user, now, false);
                return true;
            }
            met = other;
            theirs = blow;
            nearest = distance;
        }
        if (met == null) {
            return false;
        }
        collide(level, user, mine, met, theirs, now);
        return true;
    }

    /**
     * 2.4.2: the flying Haki slash of {@code owner} (along {@code dir}, {@code damage} hard, {@code ryou} its Ryou colour or 0) reaches
     * {@code body} at {@code at}: true if a blow of {@code body}'s coming back at it (facing it) meets it there. A slash is a blade's:
     * a bare blow meets it only with Armament on. Met: the slash stops there (the caller's), the body's move stops, it is staggered,
     * held and thrown back the way the slash flew (the harder the slash was than its blow, the further), heard and seen.
     */
    public static boolean slashMeets(LivingEntity owner, LivingEntity body, Vec3 dir, float damage, int ryou, Vec3 at) {
        if (!(body.m_9236_() instanceof ServerLevel level)) {
            return false;
        }
        long now = level.m_46467_();
        ClashRules.Blow theirs = blowOf(body, now);
        if (theirs == null || !ClashRules.onItsWay(theirs, now)) {
            return false;
        }
        Vec3 look = StyleCombat.facing(body);
        if (!ClashRules.faces(look.f_82479_, look.f_82481_, -dir.f_82479_, -dir.f_82481_)) {
            return false;
        }
        ClashRules.Blow slash = new ClashRules.Blow(ClashRules.Kind.BLADE, now, now, 0.0, damage, true, ryou);
        if (!ClashRules.clashable(slash, theirs)) {
            return false;
        }
        float power = ClashRules.power(damage, theirs.hit());
        double ratio = ClashRules.ratio(theirs.hit(), damage);
        stop(body, now, true);
        stagger(body, now, ClashRules.stagger(ratio));
        double flat = Math.max(1.0E-4, Math.hypot(dir.f_82479_, dir.f_82481_));
        Impact.hit(owner, body, spec(power, ClashRules.frameTicks(power) + 2, dir.f_82479_ / flat, dir.f_82481_ / flat,
            ClashRules.thrown(power, ratio), ClashRules.lifted(ratio)));
        heard(body, at, power, true);
        PieceNetwork.around(body, new S2CClash(++next, at.f_82479_, at.f_82480_, at.f_82481_, (float) (dir.f_82479_ / flat),
            (float) (dir.f_82481_ / flat), power, ryou, theirs.ryou(), owner.m_19879_(), body.m_19879_(),
            S2CClash.CLASH | S2CClash.BLADE_A | (theirs.blade() ? S2CClash.BLADE_B : 0) | (ClashRules.overwhelmed(ratio) ? S2CClash.OVERWHELMED : 0)));
        return true;
    }

    /**
     * 2.4.2: two flying Haki slashes met head on at {@code at} ({@code a}'s flying along {@code dir}): both stopped there (the caller's);
     * the clash heard and seen by everyone near either thrower, both Ryou colours bursting out if both were Ryou slashes.
     */
    public static void slashesMeet(LivingEntity a, LivingEntity b, Vec3 at, Vec3 dir, float hitA, float hitB, int ryouA, int ryouB) {
        if (!(a.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        float power = ClashRules.power(hitA, hitB);
        heard(a, at, power, true);
        if (ClashRules.quakes(power)) {
            Impact.quake(level, new Vec3(at.f_82479_, at.f_82480_ - 1.0, at.f_82481_), 4.0 + 2.5 * power, Math.min(1.0F, 0.35F + 0.25F * power), 10);
        }
        double flat = Math.max(1.0E-4, Math.hypot(dir.f_82479_, dir.f_82481_));
        S2CClash message = new S2CClash(++next, at.f_82479_, at.f_82480_, at.f_82481_, (float) (dir.f_82479_ / flat), (float) (dir.f_82481_ / flat),
            power, ryouA, ryouB, a.m_19879_(), b.m_19879_(), S2CClash.CLASH | S2CClash.BLADE_A | S2CClash.BLADE_B);
        PieceNetwork.around(a, message);
        PieceNetwork.around(b, message);
    }

    /** A clash heard at {@code at}: the blows meeting, and steel when a blade is in it. */
    static void heard(LivingEntity near, Vec3 at, float power, boolean steel) {
        PieceSounds.playAt(near, at.f_82479_, at.f_82480_, at.f_82481_, ClashRules.SOUND, ClashRules.volume(power), ClashRules.pitch(power));
        if (steel) {
            PieceSounds.playAt(near, at.f_82479_, at.f_82480_, at.f_82481_, com.hexhaki.piece.sword.SwordSounds.CLASH, 0.9F + 0.2F * power, 0.9F);
        }
    }

    /** What {@code body} is throwing now (a player's style move or sword, a sea fighter's), or null. */
    static ClashRules.Blow blowOf(LivingEntity body, long now) {
        if (body instanceof ServerPlayer player) {
            ClashRules.Blow blow = StyleServer.blow(player, now);
            blow = blow != null ? blow : SwordServer.blow(player, now);
            return blow != null ? blow : ClashBoard.blow(player, now); // 3.8.4: a fruit's blow on the board (HexGhoul's)
        }
        return body instanceof SeaFighter fighter ? fighter.blow(now) : null;
    }

    /**
     * 3.8.4 (the owner: "I want a proper clashing system like our other mod has HexGhoul"): how far {@code body} has come, as HexGhoul's
     * ClashPower counts it (clash/ClashPower): a player by the mastery of what strikes (the sword for a blade; the fruit for a fruit's
     * blow on the board; else their style), their Armament and their Conqueror's; a sea fighter by its rank's mastery.
     */
    static double progress(LivingEntity body, ClashRules.Blow blow, boolean board) {
        if (body instanceof ServerPlayer player) {
            int discipline;
            if (blow.blade()) {
                discipline = SwordServer.mastery(player);
            } else if (board) {
                discipline = Math.max(com.hexhaki.fruit.FruitData.mastery(player), ProfileServer.of(player).mastery(ProfileServer.of(player).style()));
            } else {
                discipline = ProfileServer.of(player).mastery(ProfileServer.of(player).style());
            }
            return com.hexhaki.piece.clash.ClashPower.progress(discipline,
                com.hexhaki.data.HakiData.mastery(player, com.hexhaki.data.HakiType.ARMAMENT),
                com.hexhaki.data.HakiData.mastery(player, com.hexhaki.data.HakiType.CONQUEROR));
        }
        if (body instanceof SeaFighter fighter) {
            int m = com.hexhaki.piece.npc.SeaRules.MASTERY[Math.max(0, Math.min(com.hexhaki.piece.npc.SeaRules.MASTERY.length - 1, fighter.rank()))];
            return com.hexhaki.piece.clash.ClashPower.progress(m, m, m / 2);
        }
        return 0.0;
    }

    /** 3.8.4: {@code body}'s blow as it counts in a clash: its heft times who swings it (clash/ClashPower). */
    static double strength(LivingEntity body, ClashRules.Blow blow, long now) {
        boolean board = body instanceof ServerPlayer player && StyleServer.blow(player, now) == null && SwordServer.blow(player, now) == null;
        try {
            return com.hexhaki.piece.clash.ClashPower.blow(progress(body, blow, board), blow.hit(), blow.armed());
        } catch (RuntimeException failure) {
            return com.hexhaki.piece.clash.ClashPower.blow(0.0, blow.hit(), blow.armed());
        }
    }

    /** {@code body}'s move cut off where it is; {@code recoil}: knocked back in a clash (its recoil plays), else simply dropped. */
    static void stop(LivingEntity body, long now, boolean recoil) {
        ClashBoard.stop(body); // 3.8.4: a fruit's blow on the board cut off too
        if (body instanceof ServerPlayer player) {
            StyleServer.clashed(player, recoil);
            SwordServer.clashed(player, now, recoil);
        } else if (body instanceof SeaFighter fighter) {
            fighter.clashed(now, recoil);
        }
    }

    /** {@code a} and {@code b} meet: both stopped and staggered, held, thrown apart, the ground shaken, heard and seen. */
    static void collide(ServerLevel level, LivingEntity a, ClashRules.Blow blowA, LivingEntity b, ClashRules.Blow blowB, long now) {
        float power = ClashRules.power(blowA.hit(), blowB.hit());
        // 3.8.4 (HexGhoul's): who is thrown is by the gap between the two as they count (the blow's heft times who swings it), not the
        // blows' damage alone: a master stands its ground against a beginner's heavy move, the weaker is the one sent flying
        double strengthA = strength(a, blowA, now);
        double strengthB = strength(b, blowB, now);
        double ratioA = ClashRules.ratio(strengthA, strengthB);
        double ratioB = ClashRules.ratio(strengthB, strengthA);
        Vec3 chestA = a.m_20182_().m_82520_(0.0, a.m_20206_() * 0.6, 0.0);
        Vec3 chestB = b.m_20182_().m_82520_(0.0, b.m_20206_() * 0.6, 0.0);
        Vec3 line = chestB.m_82546_(chestA);
        double flat = Math.sqrt(line.f_82479_ * line.f_82479_ + line.f_82481_ * line.f_82481_);
        double dx = flat < 1.0E-4 ? StyleCombat.facing(a).f_82479_ : line.f_82479_ / flat;
        double dz = flat < 1.0E-4 ? StyleCombat.facing(a).f_82481_ : line.f_82481_ / flat;
        double along = ClashRules.meeting(blowA, blowB, flat);
        double share = flat < 1.0E-4 ? 0.5 : along / flat;
        Vec3 at = chestA.m_82549_(line.m_82490_(share));
        stop(a, now, true);
        stop(b, now, true);
        stagger(a, now, ClashRules.stagger(ratioA));
        stagger(b, now, ClashRules.stagger(ratioB));
        int hold = ClashRules.frameTicks(power) + 2;
        if (a.m_20182_().m_82554_(at) <= NEAR) {
            thrown(level, b, a, power, hold, -dx, -dz, ratioA);
        }
        if (b.m_20182_().m_82554_(at) <= NEAR) {
            thrown(level, a, b, power, hold, dx, dz, ratioB);
        }
        if (ClashRules.quakes(power)) {
            Vec3 ground = new Vec3(at.f_82479_, Math.min(a.m_20186_(), b.m_20186_()), at.f_82481_);
            Impact.quake(level, ground, 4.0 + 2.5 * power, Math.min(1.0F, 0.35F + 0.25F * power), 10);
        }
        boolean steel = blowA.blade() || blowB.blade();
        PieceSounds.playAt(a, at.f_82479_, at.f_82480_, at.f_82481_, ClashRules.SOUND, ClashRules.volume(power), ClashRules.pitch(power));
        if (steel) {
            PieceSounds.playAt(a, at.f_82479_, at.f_82480_, at.f_82481_, com.hexhaki.piece.sword.SwordSounds.CLASH,
                0.9F + 0.2F * power, blowA.blade() && blowB.blade() ? 1.0F : 0.8F);
        }
        int flags = S2CClash.CLASH | (blowA.blade() ? S2CClash.BLADE_A : 0) | (blowB.blade() ? S2CClash.BLADE_B : 0)
            | (ClashRules.overwhelmed(ratioA) || ClashRules.overwhelmed(ratioB) ? S2CClash.OVERWHELMED : 0);
        S2CClash message = new S2CClash(++next, at.f_82479_, at.f_82480_, at.f_82481_, (float) dx, (float) dz, power, blowA.ryou(),
            blowB.ryou(), a.m_19879_(), b.m_19879_(), flags);
        PieceNetwork.around(a, message);
        PieceNetwork.around(b, message); // a viewer seeing both is sent it twice: the client plays one clash per id
    }

    /**
     * 3.8.4: how far and fast an overwhelmed body is flung (HexGhoul's: "pretty much the same effect entirely of the kick without the
     * animation, send them through blocks"): level, through whatever is in the way, for this many ticks, then down for {@link #FLUNG_DOWN}.
     */
    static final double FLUNG_SPEED = 1.7;
    static final int FLUNG_TICKS = 11;
    static final int FLUNG_DOWN = 50;

    /**
     * {@code who} (out-hit {@code ratio} times by {@code by}) thrown from the clash along (x, z): held and thrown back as before, the
     * weaker further; 3.8.4: overwhelmed ({@link ClashRules#overwhelmed}), flung level through everything in its way and knocked down
     * (ragdolled, stunned till it is up), as HexGhoul flings the overwhelmed.
     */
    static void thrown(ServerLevel level, LivingEntity by, LivingEntity who, float power, int hold, double x, double z, double ratio) {
        if (!ClashRules.overwhelmed(ratio)) {
            Impact.hit(by, who, spec(power, hold, x, z, ClashRules.thrown(power, ratio), ClashRules.lifted(ratio)));
            return;
        }
        Impact.hit(by, who, spec(power, 2, x, z, 0.0, 0.0));
        ConquerorClash.launch(level, who, x * FLUNG_SPEED, z * FLUNG_SPEED, FLUNG_TICKS, FLUNG_DOWN);
        PieceSounds.playAt(who, who.m_20185_(), who.m_20186_() + 1.0, who.m_20189_(), "piece.iron.boom", 1.2F, 0.9F);
    }

    /** The impact a clash gives one of the two: held {@code hold} ticks, then thrown {@code push} along (x, z) and {@code lift} up. */
    static Impact.Spec spec(float power, int hold, double x, double z, double push, double lift) {
        Vec3 launch = new Vec3(x * push, lift, z * push);
        return new Impact.Spec(hold, 0.55F + 0.25F * power, 0.5F + 0.15F * power, Impact.FLASH_NONE, 0.06F + 0.03F * power, launch);
    }

    /** {@code body} staggered for {@code ticks}: no blow meanwhile, and its legs slowed. */
    static void stagger(LivingEntity body, long now, int ticks) {
        if (STAGGERED.size() > 128) {
            STAGGERED.values().removeIf(until -> until < now);
        }
        STAGGERED.put(body.m_19879_(), now + ticks);
        body.m_7292_(new MobEffectInstance(MobEffects.f_19597_, ticks, 2, false, false, false));
    }
}
