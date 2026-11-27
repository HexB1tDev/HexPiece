package com.hexhaki.piece.gun;

/**
 * 3.8.8: what a gun is doing (the server says it, {@code network/S2CGun}) and the clip each plays (tools/gun_anims.py writes them under
 * player_animation/; one set for every gun, the owner: "these animations will serve for every gun from now on"). Pure: tested.
 * <ul>
 *   <li>{@link #NONE}: no gun in the hand.</li>
 *   <li>{@link #DRAW}: the gun just taken in hand, spun once on the finger into the relaxed hold ({@link #DRAW_TICKS}).</li>
 *   <li>{@link #HOLD}: the relaxed hold (the owner: "guns are lowered in a really relaxed way, pointing down at the floor sometimes playing
 *       with them by spinning them on the hand, running animation"): the client picks the clip by what the body does: standing still
 *       {@link #IDLE} (now and then {@link #SPIN}), walking {@link #WALK}, sprinting {@link #RUN}.</li>
 *   <li>{@link #FIRE} + a bucket: M1, the arm snapped up to where they look ({@link #bucket}: one clip every {@link #STEP} degrees of
 *       the look's pitch, so the barrel is on the crosshair), the shot at {@link GunRules#AIM}, the recoil, held to the reload.</li>
 *   <li>{@link #RELOAD}: "simple reload animation, put 1 bullet in the hole of the barrel": the gun brought up, the muzzle tipped up,
 *       the left hand takes a ball from the belt ({@link #BALL_FROM}), presses it into the muzzle ({@link #BALL_AT} to
 *       {@link #BALL_HOME}), the gun back down into the hold.</li>
 * </ul>
 */
public final class GunAnims {
    public static final int NONE = 0;
    public static final int HOLD = 1;
    public static final int DRAW = 2;
    public static final int RELOAD = 3;
    /** FIRE + bucket (0 looking {@link #HIGHEST} up ... {@link #BUCKETS} - 1 as far down). */
    public static final int FIRE = 10;
    public static final int BUCKETS = 7;
    /** Degrees of look pitch a fire clip covers. */
    public static final int STEP = 20;
    /** The look pitch of bucket 0 (the game's: negative is up). */
    public static final int HIGHEST = -60;
    public static final int DRAW_TICKS = 16;
    /** The fire clip's length: the aim, the shot, the recoil settling until the reload begins. */
    public static final int FIRE_TICKS = GunRules.AIM + GunRules.RELOAD_FROM;
    /** The reload: the ball in the left hand from BALL_FROM, at the muzzle at BALL_AT, pressed home (gone) by BALL_HOME. */
    public static final int BALL_FROM = 4;
    public static final int BALL_AT = 12;
    public static final int BALL_HOME = 15;

    public static final String IDLE = "gun_idle";
    public static final String WALK = "gun_walk";
    public static final String RUN = "gun_run";
    public static final String SPIN = "gun_spin";
    public static final String SPIN_DRAW = "gun_draw";
    public static final String RELOAD_CLIP = "gun_reload";
    /** The spin's length (the client plays it once over the idle). */
    public static final int SPIN_TICKS = 30;

    private GunAnims() {
    }

    /** The fire bucket of a look pitch (degrees, the game's: negative up). */
    public static int bucket(float pitch) {
        int b = Math.round((pitch - HIGHEST) / (float) STEP);
        return Math.max(0, Math.min(BUCKETS - 1, b));
    }

    /** The look pitch a fire bucket's clip aims at. */
    public static int pitchOf(int bucket) {
        return HIGHEST + bucket * STEP;
    }

    public static boolean firing(int anim) {
        return anim >= FIRE && anim < FIRE + BUCKETS;
    }

    /** The clip of a fire bucket. */
    public static String fireClip(int bucket) {
        return "gun_fire_" + bucket;
    }

    /**
     * The clip {@code anim} plays: for the hold, by what the body does ({@code moving} walking, {@code sprinting}, {@code spinning}
     * standing still and the spin due); null for none.
     */
    public static String name(int anim, boolean moving, boolean sprinting, boolean spinning) {
        if (firing(anim)) {
            return fireClip(anim - FIRE);
        }
        return switch (anim) {
            case DRAW -> SPIN_DRAW;
            case RELOAD -> RELOAD_CLIP;
            case HOLD -> sprinting ? RUN : moving ? WALK : spinning ? SPIN : IDLE;
            default -> null;
        };
    }

    /** Whether the lead ball is in the left hand {@code ticks} into the reload (its way from the belt to the muzzle). */
    public static boolean ballInHand(int anim, float ticks) {
        return anim == RELOAD && ticks >= BALL_FROM && ticks < BALL_AT;
    }

    /** How far the ball has gone into the muzzle (0 at its mouth, 1 home and out of sight); -1 when it is not there. */
    public static float ballInMuzzle(int anim, float ticks) {
        if (anim != RELOAD || ticks < BALL_AT || ticks >= BALL_HOME) {
            return -1.0F;
        }
        return (ticks - BALL_AT) / (float) (BALL_HOME - BALL_AT);
    }
}
