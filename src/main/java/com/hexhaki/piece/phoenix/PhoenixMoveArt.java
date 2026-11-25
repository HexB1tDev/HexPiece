package com.hexhaki.piece.phoenix;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.7.2 (the owner: "Proceed": the Phoenix's moves on the wiki's GIFs, docs/handoff/next-ai-3.6.3.md AD): the moves' timings and fire.
 * 3.7.5 (the owner: "I dont like the fire particles from the moves"; "Explosions and slams from moves lack real explosion"): the fire is
 * the house's (phoenix/PhoenixBlast: FireMesh's banded fire puffs, the dragon fruit's explosions and fireballs, in the phoenix's blue and
 * gold), the white shock rings and arcs as the reference's. Every timing the server's (server/PhoenixMoves) and the client's
 * (client/phoenix/PhoenixFx) are these constants. Pure: the game's renderer, the previews and the tests share it; points eye-relative.
 */
public final class PhoenixMoveArt {
    private PhoenixMoveArt() {
    }

    // ------------------------------------------------------------------------------------------------------ the timings (ticks)

    /** Fujiazami, human (the GIF's frames 15 to 57): the fire gathering at the feet, the dash, how far it goes (blocks). */
    public static final int FUJI_CHARGE = 8;
    public static final int FUJI_DASH = 6;
    public static final double FUJI_REACH = 12.0;
    /** As the phoenix (Fujiazami2: 32 to 72): the wings swept forward, the ram, how far, the glide round after it. */
    public static final int FUJI_WIND = 6;
    public static final int FUJI_RAM = 8;
    public static final double FUJI_RAM_REACH = 16.0;
    public static final int FUJI_GLIDE = 14;
    /** How long the spray at the end of the dash burns. */
    public static final int FUJI_BURST = 22;

    /** Talon Slam (the GIF's 15 to 84): the crouch, the leap up, the hang at the top (the leg lighting), the longest dive. */
    public static final int TALON_CROUCH = 4;
    public static final int TALON_RISE = 12;
    public static final int TALON_HANG = 8;
    public static final int TALON_DIVE = 10;
    public static final double TALON_HEIGHT = 7.0;
    /** How far in front it slams down at most (blocks), the blast's radius, how long the crater burns. */
    public static final double TALON_REACH = 6.0;
    public static final double TALON_RADIUS = 4.0;
    public static final int TALON_CRATER = 46;

    /** Hawk Rush (the GIF's 15 to 51): the wings wrapped forward, the shortest and longest rush (held), the grab's whirl. */
    public static final int HAWK_WRAP = 5;
    public static final int HAWK_LEAST = 10;
    public static final int HAWK_MOST = 100;
    public static final int HAWK_WHIRL = 14;

    /**
     * 3.7.3: Phoenix Inferno, human (the GIF's 12 to 88): the wings and tail forming in a burst of fire, the barrage of kicks (one every
     * {@link #INFERNO_EVERY}), the last kick up, the backflip, the landing burst that scatters the wings and tail.
     */
    public static final int INFERNO_FORM = 8;
    public static final int INFERNO_KICKS = 12;
    public static final int INFERNO_EVERY = 3;
    public static final int INFERNO_LAST = INFERNO_FORM + INFERNO_KICKS * INFERNO_EVERY;
    public static final int INFERNO_FLIP = INFERNO_LAST + 6;
    public static final int INFERNO_LAND = INFERNO_FLIP + 12;
    public static final double INFERNO_REACH = 3.5;
    /** As the phoenix (Inferno2: 12 to 168): the whirlpool of fire (its radius), the rise, the dive, the blast (its radius). */
    public static final int VORTEX = 60;
    public static final double VORTEX_RADIUS = 6.0;
    public static final int VORTEX_RISE = 8;
    public static final double VORTEX_HEIGHT = 9.0;
    public static final int VORTEX_DIVE = 6;
    public static final double VORTEX_BLAST = 6.0;
    public static final int VORTEX_BLAST_LIFE = 40;

    /** 3.7.3: Bluebird: the longest hold (the ball's growth), how fast it flies, how far, the four balls' gap (the phoenix). */
    public static final int BLUE_MOST = 60;
    public static final int BLUE_LEAST = 8;
    public static final double BLUE_SPEED = 1.2;
    public static final double BLUE_BIRD_SPEED = 0.9;
    public static final double BLUE_RANGE = 40.0;
    public static final int BLUE_GAP = 4;
    public static final int BLUE_BOOM = 24;

    /** Bluebird's ball's size (blocks across its heart) after {@code held} ticks of the hold. */
    public static double blueSize(double held) {
        return 0.6 + 1.0 * Math.min(1.0, Math.max(0.0, held) / BLUE_MOST);
    }

    /** Talon Slam's right leg (degrees of pitch, the clip phoenix_talon's: 0 hanging, -120 raised high in front, 20 driven down past). */
    public static float talonLeg(float ticks) {
        float top = TALON_CROUCH + TALON_RISE;
        if (ticks < top - 4) {
            return -20.0F * smooth((ticks - TALON_CROUCH) / 6.0F);
        }
        if (ticks < top + TALON_HANG) {
            return -20.0F - 100.0F * smooth((ticks - top + 4) / (TALON_HANG + 2.0F));
        }
        return -120.0F + 140.0F * smooth((ticks - top - TALON_HANG) / 4.0F);
    }

    static float smooth(float t) {
        t = Math.max(0.0F, Math.min(1.0F, t));
        return t * t * (3.0F - 2.0F * t);
    }

    // ------------------------------------------------------------------------------------------------------ Fujiazami

    /**
     * 3.7.5 (the owner: "I dont like the fire particles from the moves"): the blue fire gathering round the feet at (x, y, z) before the
     * dash ({@code ticks} into the charge): the house's fire (PhoenixBlast's flame puffs) licking up round the feet, drawing in, a ring.
     */
    public static void fujiCharge(FireMesh.Sink sink, double x, double y, double z, double ticks, double big, int seed) {
        double f = Math.min(1.0, ticks / FUJI_CHARGE);
        double r = (1.0 - 0.5 * f) * big;
        FireMesh.ring(sink, x, y + 0.05, z, 0, 1, 0, r + 0.2, 0.35 * big, 0.1, 0x2DB8EC, 0.6 * f);
        // (a step coarser: round the phoenix they burn beside its own model, in a phone's budget)
        double quality = FireMesh.quality;
        FireMesh.quality = quality * 0.7;
        for (int k = 0; k < 5; k++) {
            double a = 2 * Math.PI * (k + 0.3 * FireMesh.rand(seed, k)) / 5.0 + ticks * 0.15;
            PhoenixBlast.flames(sink, x + Math.cos(a) * r, y, z + Math.sin(a) * r, (0.6 + 0.5 * f) * big, f, ticks, seed + k * 5);
        }
        FireMesh.quality = quality;
    }

    /**
     * The fire left along the dash from (x, y, z) along (dx, dy, dz) (the whole way), the one dashing {@code got} of the way along it:
     * flames standing along the path behind them, burning down from the start; a white shock ring where it set off; {@code ticks} since
     * the dash began.
     */
    public static void fujiTrail(FireMesh.Sink sink, double x, double y, double z, double dx, double dy, double dz, double got, double ticks,
                                 double big, int seed) {
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.1) {
            return;
        }
        if (ticks < 8.0) {
            double f = ticks / 8.0;
            FireMesh.ring(sink, x, y + 0.08, z, 0, 1, 0, (0.8 + 3.2 * f) * big, 0.55 * big, 0.25, 0xF4FBFF, 0.85 * (1.0 - f));
        }
        int n = (int) Math.max(5, Math.min(12, len * 0.9));
        for (int k = 0; k < n; k++) {
            double u = (k + 0.5) / n;
            if (u > got) {
                break;
            }
            double born = u / Math.max(1.0E-3, got) * Math.min(ticks, FUJI_DASH);
            double age = ticks - born;
            double burn = 1.0 - Math.max(0.0, Math.min(1.0, (age - 4.0) / 14.0));
            PhoenixBlast.flames(sink, x + dx * u, y + dy * u, z + dz * u, 1.2 * big, burn, ticks, seed + k * 11);
        }
    }

    /**
     * The one dashing at (x, y, z) (their middle) wrapped in fire, going (wx, wy, wz) (unit): the house's fireball round them, its tail
     * of fire burning down behind; {@code big} 1 for the human, more for the phoenix.
     */
    public static void fujiSheath(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double ticks, double big,
                                  int seed) {
        PhoenixBlast.fireball(sink, x + wx * 0.4 * big, y + wy * 0.4 * big, z + wz * 0.4 * big, wx, wy, wz, 0.9 * big, ticks, 4.0 * big, seed);
    }

    /** The end of the dash at (x, y, z) facing (wx, wz), {@code ticks} after it: an explosion of the phoenix's fire (PhoenixBlast). */
    public static void fujiBurst(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double ticks, double big, int seed) {
        if (ticks < 0.0 || ticks > FUJI_BURST + 10) {
            return;
        }
        PhoenixBlast.explosion(sink, x + wx * 0.5, y, z + wz * 0.5, 2.4 * big, ticks, seed);
    }

    // ------------------------------------------------------------------------------------------------------ Talon Slam

    /** The leap (the GIF's 15 to 18): white crescents on the ground where it jumped, opening out; {@code ticks} since the leap. */
    public static void talonLeap(FireMesh.Sink sink, double x, double y, double z, double ticks) {
        if (ticks < 0.0 || ticks > 10.0) {
            return;
        }
        double f = ticks / 10.0;
        double a = 1.0 - f;
        FireMesh.ring(sink, x, y + 0.06, z, 0, 1, 0, 0.6 + 2.6 * f, 0.45, 0.35, 0xFFFFFF, 0.85 * a);
        FireMesh.arc(sink, x, y + 0.1, z, 0, 1, 0, 1.2 + 2.0 * f, 0.3, 0.3, 2.4, 0.4, 0xF4FBFF, 0.8 * a);
        FireMesh.arc(sink, x, y + 0.1, z, 0, 1, 0, 1.2 + 2.0 * f, 0.3, 3.44, 2.4, 0.4, 0xF4FBFF, 0.8 * a);
    }

    /**
     * Round the one at the top of the leap at (x, y, z) (their middle), {@code ticks} into the hang (the GIF's 42 to 54): white rings of
     * wind swirling round them at tilts, turning.
     */
    public static void talonWind(FireMesh.Sink sink, double x, double y, double z, double ticks) {
        double f = Math.min(1.0, ticks / 4.0);
        for (int k = 0; k < 3; k++) {
            double tilt = 0.35 + 0.25 * k;
            double turn = ticks * (0.4 + 0.1 * k) + k * 2.1;
            double ux = Math.sin(tilt) * Math.cos(turn);
            double uz = Math.sin(tilt) * Math.sin(turn);
            double uy = Math.cos(tilt);
            FireMesh.arc(sink, x, y + (k - 1) * 0.45, z, ux, uy, uz, 1.3 + 0.35 * k, 0.1, ticks * 0.5 + k, 3.6, 0.2, 0xFFFFFF, 0.65 * f);
        }
    }

    /** The burning leg from the hip (hx, hy, hz) to the foot (fx, fy, fz): flame puffs along it, the most at the foot. */
    public static void legFire(FireMesh.Sink sink, double hx, double hy, double hz, double fx, double fy, double fz, double ticks, double amount,
                               int seed) {
        if (amount < 0.02) {
            return;
        }
        for (int k = 0; k < 3; k++) {
            double t = 0.3 + 0.35 * k;
            double r = (0.22 + 0.08 * k) * amount;
            PhoenixBlast.puff(sink, hx + (fx - hx) * t, hy + (fy - hy) * t, hz + (fz - hz) * t, r, 0.0, 1.0, 0.0, 0.9, 0.25, 0.95, 0.6, 1.0,
                ticks * 1.6 + k, seed + k, k == 2);
        }
    }

    /**
     * The slam at (x, y, z) (on the ground), {@code ticks} after it: a real explosion (PhoenixBlast.explosion: the flash, the fire ball
     * going to smoke, the shock shell and ring, sparks and rocks), then the crater: the ground scorched dark blue and blue fire burning
     * round its rim, burning down over {@link #TALON_CRATER} ticks.
     */
    public static void talonBlast(FireMesh.Sink sink, double x, double y, double z, double ticks, double radius, int seed) {
        talonBlast(sink, x, y, z, ticks, radius, seed, 1.0, 8);
    }

    /** As above, {@code share} of the blast's fire puffs, {@code rim} flames burning round the crater. */
    static void talonBlast(FireMesh.Sink sink, double x, double y, double z, double ticks, double radius, int seed, double share, int rim) {
        if (ticks < 0.0 || ticks > TALON_CRATER) {
            return;
        }
        PhoenixBlast.explosion(sink, x, y + 0.6, z, radius * 0.9, ticks, seed, share);
        double burn = ticks < 8.0 ? ticks / 8.0 : Math.max(0.0, 1.0 - (ticks - 8.0) / (TALON_CRATER - 8.0));
        FireMesh.ring(sink, x, y + 0.04, z, 0, 1, 0, radius * 0.5, radius * 1.05, 0.0, 0x0D2A6A, 0.55 * burn);
        FireMesh.ring(sink, x, y + 0.05, z, 0, 1, 0, radius * 0.95, 0.6, 0.12, 0x2DB8EC, 0.6 * burn);
        for (int k = 0; k < rim; k++) {
            double a = 2 * Math.PI * (k + 0.5 * FireMesh.rand(seed, k + 70)) / rim;
            PhoenixBlast.flames(sink, x + Math.cos(a) * radius, y, z + Math.sin(a) * radius, 1.3, burn, ticks, seed + k * 13);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Hawk Rush

    /** The wings wrapped forward (the GIF's 15): a flash of white light at the chest (x, y, z), {@code ticks} into it. */
    public static void hawkFlash(FireMesh.Sink sink, double x, double y, double z, double ticks) {
        if (ticks < 0.0 || ticks > HAWK_WRAP + 3) {
            return;
        }
        double f = Math.sin(Math.PI * Math.min(1.0, ticks / (HAWK_WRAP + 3.0)));
        FireMesh.shell(sink, x, y, z, 1.6 * f + 0.2, 0xFFFFFF, 0.7 * f, 0.1 * f);
        FireMesh.ring(sink, x, y, z, 0, 1, 0, 1.0 + 1.2 * f, 0.25, 0.0, 0xFFFFFF, 0.6 * f);
    }

    /** The rush's white speed streaks round the one rushing at (x, y, z) along (wx, wy, wz) (unit), {@code ticks} into it. */
    public static void hawkStreaks(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double ticks, int seed) {
        for (int k = 0; k < 10; k++) {
            double life = 5.0;
            double age = (ticks + life * FireMesh.rand(seed, k)) % life;
            int round = (int) ((ticks + life * FireMesh.rand(seed, k)) / life);
            double a = 2 * Math.PI * FireMesh.rand(seed + round, k + 10);
            double r = 1.6 + 1.6 * FireMesh.rand(seed + round, k + 20);
            double ox = Math.cos(a) * r;
            double oy = Math.sin(a) * r * 0.6;
            // across the way: a level axis and up
            double sx = -wz;
            double sz = wx;
            double back = (age / life) * 6.0 - 2.0;
            FireMesh.streak(sink, x + sx * ox - wx * back, y + oy - wy * back, z + sz * ox - wz * back, -wx, -wy, -wz, 2.2, 0.06, 0xFFFFFF,
                0.7 * (1.0 - age / life));
        }
    }

    /**
     * 3.7.5 (the owner: "the phoenix should never do a 360"): the seize at (x, y, z), {@code ticks} into it: a white flash ring where the
     * claws strike (no whirl round: the phoenix carries them a moment and throws them).
     */
    public static void hawkWhirl(FireMesh.Sink sink, double x, double y, double z, double ticks) {
        if (ticks < 0.0 || ticks > 10.0) {
            return;
        }
        double f = ticks / 10.0;
        FireMesh.ring(sink, x, y, z, 0, 1, 0, 0.8 + 2.4 * f, 0.4, 0.2, 0xFFFFFF, 0.8 * (1.0 - f));
        FireMesh.shell(sink, x, y, z, 0.6 + 1.6 * f, 0xFFFFFF, 0.0, 0.7 * (1.0 - f));
    }

    // ------------------------------------------------------------------------------------------------------ 3.7.3: Phoenix Inferno

    /**
     * A kick of the barrage from the hip (hx, hy, hz) out along (wx, wz), {@code ticks} after it: the foot a puff of fire thrust out and
     * back, a white crescent and ring of air ahead of it; {@code up} the last kick.
     */
    public static void kick(FireMesh.Sink sink, double hx, double hy, double hz, double wx, double wz, double ticks, boolean up, int seed) {
        if (ticks < 0.0 || ticks > 6.0) {
            return;
        }
        double out = Math.sin(Math.PI * Math.min(1.0, ticks / 4.0));
        double rise = up ? 0.9 : 0.15;
        double fx = hx + wx * 0.75 * out;
        double fy = hy + rise * out - 0.1;
        double fz = hz + wz * 0.75 * out;
        legFire(sink, hx, hy, hz, fx, fy, fz, ticks, 0.8 * out + 0.3, seed);
        double a = 1.0 - ticks / 6.0;
        FireMesh.arc(sink, fx + wx * 0.3, fy, fz + wz * 0.3, wx, up ? 0.8 : 0.0, wz, 0.7 + 1.3 * (ticks / 6.0), 0.16, seed * 0.7, 2.8, 0.25,
            0xFFFFFF, 0.85 * a);
        FireMesh.ring(sink, fx + wx * 0.2, fy, fz + wz * 0.2, wx, 0.0, wz, 0.4 + 1.1 * (ticks / 6.0), 0.16, 0.05, 0xF4FBFF, 0.65 * a);
    }

    /** Blue fire bursting round (x, y, z), {@code ticks} into it, {@code big} its size: a small explosion of the phoenix's fire. */
    public static void burst(FireMesh.Sink sink, double x, double y, double z, double ticks, double big, int seed) {
        PhoenixBlast.explosion(sink, x, y, z, 1.4 * big, ticks, seed);
    }

    /**
     * The phoenix's whirlpool of fire round (x, y, z) (the ground under it), {@code ticks} into it: bands of cyan, white and gold fire
     * swirling round it at heights, the house's flame puffs racing round it and climbing; {@code radius} out.
     */
    public static void vortex(FireMesh.Sink sink, double x, double y, double z, double ticks, double radius, int seed) {
        if (ticks < 0.0 || ticks > VORTEX + 6) {
            return;
        }
        double in = Math.min(1.0, ticks / 8.0);
        double out = Math.min(1.0, Math.max(0.0, (VORTEX + 6 - ticks) / 8.0));
        double k0 = Math.min(in, out);
        for (int b = 0; b < 7; b++) {
            double h = 0.3 + b * 0.85;
            double r = radius * (0.6 + 0.09 * b) * (0.6 + 0.4 * in);
            double turn = ticks * (0.28 + 0.04 * b) + b * 1.1;
            int col = b % 3 == 2 ? 0xFFBE2E : b % 2 == 0 ? 0x46DDF5 : 0xF4FBFF;
            FireMesh.arc(sink, x, y + h, z, 0.06 * Math.sin(b), 1.0, 0.06 * Math.cos(b), r, 1.5 - 0.1 * b, turn, 4.4, 0.8, col, (0.6 - 0.04 * b) * k0);
        }
        FireMesh.ring(sink, x, y + 0.05, z, 0, 1, 0, radius * 0.9, radius * 0.5, 0.0, 0x2DB8EC, 0.3 * k0);
        // (nine flame puffs round it, a step coarser, beside the phoenix's own model: a phone's budget)
        double quality = FireMesh.quality;
        FireMesh.quality = quality * 0.7;
        for (int k = 0; k < 9; k++) {
            double a = 2 * Math.PI * k / 9.0 + ticks * 0.32;
            double r = radius * (0.55 + 0.4 * FireMesh.rand(seed, k));
            double h = 0.4 + 4.0 * ((FireMesh.rand(seed, k + 20) + ticks * 0.012) % 1.0);
            double c = Math.cos(a);
            double sn = Math.sin(a);
            PhoenixBlast.puff(sink, x + c * r, y + h, z + sn * r, (1.0 + 0.6 * FireMesh.rand(seed, k + 50)) * k0, -sn, 0.3, c, 1.3, 0.3, 0.8, 0.7,
                0.9 * k0, ticks * 1.5 + k, seed + k * 7, k % 3 == 0);
        }
        FireMesh.quality = quality;
    }

    /** The phoenix's blast at (x, y, z) (on the ground), {@code ticks} after it: a great explosion and the burning crater (as the slam's). */
    public static void vortexBlast(FireMesh.Sink sink, double x, double y, double z, double ticks, double radius, int seed) {
        if (ticks < 0.0 || ticks > VORTEX_BLAST_LIFE) {
            return;
        }
        // 3.7.5: beside the diving phoenix (its own model most of a phone's budget): the blast's fire puffs a step coarser and fewer, four
        // flames on its rim
        double quality = FireMesh.quality;
        FireMesh.quality = quality * 0.5;
        talonBlast(sink, x, y, z, ticks * (TALON_CRATER / (double) VORTEX_BLAST_LIFE), radius, seed, 0.5, 4);
        FireMesh.quality = quality;
    }

    // ------------------------------------------------------------------------------------------------------ 3.7.3: Bluebird

    /**
     * Bluebird's sphere of flames at (x, y, z), {@code size} across its heart, {@code ticks} its clock (3.7.6: PhoenixBlast.sphere, held
     * still: the charge).
     */
    public static void blueBall(FireMesh.Sink sink, double x, double y, double z, double size, double ticks, int seed) {
        PhoenixBlast.sphere(sink, x, y, z, size * 0.6, 0.0, 0.0, 0.0, ticks, seed);
    }

    /** The sphere in flight going (wx, wy, wz) (unit): sparks shed behind it, no tail (3.7.6: it stays a sphere). */
    public static void blueFlying(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double ticks, double size,
                                  int seed) {
        PhoenixBlast.sphere(sink, x, y, z, size * 0.6, wx, wy, wz, ticks, seed);
    }

    /**
     * Bluebird's blast at (x, y, z), {@code ticks} after it, {@code radius} the server's: the house's explosion (its fire puffs a step
     * coarser and fewer: the phoenix's is beside its own model).
     */
    public static void blueBoom(FireMesh.Sink sink, double x, double y, double z, double ticks, double radius, int seed) {
        double quality = FireMesh.quality;
        FireMesh.quality = quality * 0.5;
        PhoenixBlast.explosion(sink, x, y, z, radius * 0.7, ticks, seed, 0.7);
        FireMesh.quality = quality;
    }

}
