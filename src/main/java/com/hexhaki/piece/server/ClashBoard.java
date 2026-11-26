package com.hexhaki.piece.server;

import com.hexhaki.piece.clash.ClashRules;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * 3.8.4 (the owner: "I want a proper clashing system like our other mod has HexGhoul"): HexGhoul's clash board (its KaguneClash). Every
 * blow that is not a fighting style's or a sword's (those say so themselves: StyleServer.blow, SwordServer.blow) says here when it sets
 * out ({@link #swing}: when it lands, how far it reaches, how hard, what it strikes with) and when it is done ({@link #landed}); its move
 * asks {@link Clashes#landing} the moment it lands, as a style's does. So a fruit's blow meets a style's, a sword's, another fruit's or
 * a sea fighter's coming back at it, and the clash plays (both stopped, the weaker thrown, the overwhelmed flung: {@link Clashes}).
 * Each blow's {@code stop} cuts its move off when it is clashed.
 */
public final class ClashBoard {
    /** A blow on its way: whose, the blow, whether it is still that blow, how its move is cut off. */
    record Swing(LivingEntity who, ClashRules.Blow blow, BooleanSupplier live, Runnable stop) {
    }

    private static final Map<UUID, Swing> SWINGS = new HashMap<>();
    /** Blows long gone are let go of this many ticks after they could have landed. */
    static final long FORGET = 100L;

    private ClashBoard() {
    }

    /**
     * {@code player}'s blow sets out {@code now}, landing at {@code lands}, {@code reach} blocks long, {@code hit} hard (its damage), struck
     * with {@code kind}; {@code live} true while it is still that blow, {@code stop} cuts its move off.
     */
    public static void swing(ServerPlayer player, long now, long lands, double reach, double hit, ClashRules.Kind kind, BooleanSupplier live,
                             Runnable stop) {
        if (SWINGS.size() > 64) {
            SWINGS.values().removeIf(old -> now > old.blow().lands() + FORGET || old.who().m_213877_());
        }
        ClashRules.Blow blow = new ClashRules.Blow(kind, now, lands, reach, hit, StyleServer.armed(player), StyleServer.ryou(player));
        SWINGS.put(player.m_20148_(), new Swing(player, blow, live, stop));
    }

    /**
     * {@code player}'s blow (set out with {@link #swing}) lands now: true if it met one coming back at it (the clash played: the caller
     * lands nothing), or was cut off by one ({@code cut}: what its stop set). Off the board either way.
     */
    public static boolean lands(ServerPlayer player, boolean[] cut) {
        if (cut != null && cut[0]) {
            landed(player);
            return true;
        }
        boolean met = false;
        try {
            met = Clashes.landing(player, player.m_9236_().m_46467_(), other -> com.hexhaki.fruit.FruitUtil.foe(player, other));
        } catch (RuntimeException failure) {
            met = false; // a clash never stops the blow
        }
        landed(player);
        return met;
    }

    /** As {@link #swing}, for a blow landed later by a scheduled hit: its stop marks {@code cut} (the hit then lands nothing). */
    public static boolean[] swing(ServerPlayer player, long now, long lands, double reach, double hit, ClashRules.Kind kind) {
        boolean[] cut = {false};
        swing(player, now, lands, reach, hit, kind, () -> !cut[0], () -> cut[0] = true);
        return cut;
    }

    /** {@code who}'s blow has landed (or missed, or ended): no longer on its way. */
    public static void landed(LivingEntity who) {
        SWINGS.remove(who.m_20148_());
    }

    /** {@code who}'s blow on the board {@code now} (on its way, still that blow), or null. */
    static ClashRules.Blow blow(LivingEntity who, long now) {
        Swing swing = SWINGS.get(who.m_20148_());
        if (swing == null) {
            return null;
        }
        if (now > swing.blow().lands() + ClashRules.LATE || !alive(swing)) {
            SWINGS.remove(who.m_20148_());
            return null;
        }
        return swing.blow();
    }

    /** {@code who}'s blow on the board clashed: its move cut off. */
    static void stop(LivingEntity who) {
        Swing swing = SWINGS.remove(who.m_20148_());
        if (swing != null) {
            try {
                swing.stop().run();
            } catch (RuntimeException failure) {
                // a move that fails to stop never stops the clash
            }
        }
    }

    static boolean alive(Swing swing) {
        try {
            return !swing.who().m_213877_() && swing.who().m_6084_() && swing.live().getAsBoolean();
        } catch (RuntimeException failure) {
            return false;
        }
    }

    static void clear() {
        SWINGS.clear();
    }
}
