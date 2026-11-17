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

    /** What {@code body} is throwing now (a player's style move or sword, a sea fighter's), or null. */
    static ClashRules.Blow blowOf(LivingEntity body, long now) {
        if (body instanceof ServerPlayer player) {
            ClashRules.Blow blow = StyleServer.blow(player, now);
            return blow != null ? blow : SwordServer.blow(player, now);
        }
        return body instanceof SeaFighter fighter ? fighter.blow(now) : null;
    }

    /** {@code body}'s move cut off where it is; {@code recoil}: knocked back in a clash (its recoil plays), else simply dropped. */
    static void stop(LivingEntity body, long now, boolean recoil) {
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
        double ratioA = ClashRules.ratio(blowA.hit(), blowB.hit());
        double ratioB = ClashRules.ratio(blowB.hit(), blowA.hit());
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
            Impact.hit(b, a, spec(power, hold, -dx, -dz, ClashRules.thrown(power, ratioA), ClashRules.lifted(ratioA)));
        }
        if (b.m_20182_().m_82554_(at) <= NEAR) {
            Impact.hit(a, b, spec(power, hold, dx, dz, ClashRules.thrown(power, ratioB), ClashRules.lifted(ratioB)));
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
        int flags = (blowA.blade() ? S2CClash.BLADE_A : 0) | (blowB.blade() ? S2CClash.BLADE_B : 0)
            | (ClashRules.overwhelmed(ratioA) || ClashRules.overwhelmed(ratioB) ? S2CClash.OVERWHELMED : 0);
        S2CClash message = new S2CClash(++next, at.f_82479_, at.f_82480_, at.f_82481_, (float) dx, (float) dz, power, blowA.ryou(),
            blowB.ryou(), a.m_19879_(), b.m_19879_(), flags);
        PieceNetwork.around(a, message);
        PieceNetwork.around(b, message); // a viewer seeing both is sent it twice: the client plays one clash per id
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
