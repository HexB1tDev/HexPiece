package com.hexhaki.piece.gun;

/**
 * 3.8.8: the guns' rules, pure (tested: GunTests). The owner's category Gun: "Pistols, usually held with one hand (right)
 * in a badass pose, all weapons only shoot 1 bullet at a time, and auto reloads after, bullets are also the same model from now one ...
 * when haki is on, bullets come out black (pure black), those bullets are much much faster and go through blocks (10 blocks max), simple
 * reload animation, put 1 bullet in the hole of the barrel" and "Make sure guns are lowered in a really relaxed way, pointing down at the
 * floor sometimes playing with them by spinning them on the hand, running animation, etc (these animations will serve for every gun
 * from now on)".
 *
 * <p>A shot: M1 raises the gun from its relaxed hold and fires {@link #AIM} ticks later (the arm level, the muzzle on the crosshair),
 * one ball; the gun is empty until it is reloaded ({@link #RELOAD} ticks: the ball pressed into the muzzle), then it can fire again. A
 * ball flies {@link #SPEED} blocks a tick (falling {@link #GRAVITY} a tick each tick) up to {@link #RANGE} blocks, stopped by the first
 * block it meets; with Armament Haki on it flies pure black {@link #HAKI_SPEED} blocks a tick, straight, through up to {@link #PIERCE}
 * blocks.
 */
public final class GunRules {
    public static final int AIM = 3;
    public static final int RELOAD = 26;
    /** The ticks after the shot the reload begins (the recoil settles first). */
    public static final int RELOAD_FROM = 6;
    public static final double SPEED = 3.2;
    public static final double HAKI_SPEED = 9.5;
    public static final double GRAVITY = 0.012;
    public static final double RANGE = 110.0;
    public static final int PIERCE = 10;
    public static final float DAMAGE = 7.0F;
    public static final float HAKI_DAMAGE = 10.5F;
    /** How much a body is grown when the ball's line is tested against it (blocks): the ball's own size. */
    public static final double BALL = 0.12;
    /**
     * Where the ball leaves the muzzle, from the eye along the look (blocks): {@link #MUZZLE_AHEAD} ahead, {@link #MUZZLE_RIGHT} to the
     * right, {@link #MUZZLE_DOWN} below (tools/gun_anims.py's aims put the muzzle there, 1.1 ahead and 0.37 right at every pitch).
     */
    public static final double MUZZLE_AHEAD = 1.1;
    public static final double MUZZLE_RIGHT = 0.37;
    public static final double MUZZLE_DOWN = 0.17;
    /** How far (degrees) the aim the client sends may be off the look before the server takes the look itself. */
    public static final double AIM_SLACK = 25.0;
    /** The relaxed hold's spin of the gun on the hand: after this many ticks standing still with it, now and then. */
    public static final int SPIN_AFTER = 120;
    public static final int SPIN_EVERY = 260;

    private GunRules() {
    }

    /** A ball's speed (blocks a tick): a Haki ball much faster. */
    public static double speed(boolean haki) {
        return haki ? HAKI_SPEED : SPEED;
    }

    /** How far down a ball has fallen after {@code ticks} (blocks; none for a Haki ball, which flies straight). */
    public static double drop(boolean haki, double ticks) {
        return haki ? 0.0 : 0.5 * GRAVITY * ticks * ticks;
    }

    /** One coordinate of a ball {@code ticks} into its flight: from {@code start} at {@code velocity} a tick ({@code down}: its height). */
    public static double at(double start, double velocity, double ticks, boolean haki, boolean down) {
        return start + velocity * ticks - (down ? drop(haki, ticks) : 0.0);
    }

    /** The ticks a ball flies before it is gone (out of range). */
    public static int life(boolean haki) {
        return (int) Math.ceil(RANGE / speed(haki));
    }

    /** Whether a ball that has gone through {@code blocks} solid blocks goes on through another: a Haki ball through {@link #PIERCE}. */
    public static boolean through(boolean haki, int blocks) {
        return haki && blocks < PIERCE;
    }

    /** The ball's damage. */
    public static float damage(boolean haki) {
        return haki ? HAKI_DAMAGE : DAMAGE;
    }

    /** Whether the gun can fire {@code now}: loaded (its reload done). */
    public static boolean ready(long now, long loadedAt) {
        return now >= loadedAt;
    }

    /** When a gun fired at {@code firedAt} is loaded again. */
    public static long loadedAt(long firedAt) {
        return firedAt + AIM + RELOAD_FROM + RELOAD;
    }

    /** Whether the relaxed gun spins on the hand now (standing still {@code still} ticks). */
    public static boolean spins(long still) {
        return still >= SPIN_AFTER && (still - SPIN_AFTER) % SPIN_EVERY == 0;
    }
}
