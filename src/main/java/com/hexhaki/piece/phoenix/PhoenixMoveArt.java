package com.hexhaki.piece.phoenix;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.mochi.MochiArt;

/**
 * 3.7.2 (the owner: "Proceed": the Phoenix's moves on the wiki's GIFs, docs/handoff/next-ai-3.6.3.md AD): the moves' fire, in the
 * phoenix's own style (PhoenixFire's tongues: curling tapered banded tubes of cyan and gold, the house's alpha-blended quads; white
 * shock rings and arcs as the reference's): X Fujiazami's charge, its streak along the dash, the comet of fire round the one dashing and
 * the spray where it ends; C Talon Slam's rings at the leap and round the top, the burning leg and the blast with its crater of blue
 * fire; Hawk Rush's speed streaks and the white whirl. Every timing the server's (server/PhoenixMoves) and the client's
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
     * The blue fire gathering round the feet at (x, y, z) before the dash ({@code ticks} into the charge, of {@link #FUJI_CHARGE}):
     * tongues licking up in a ring that draws in and grows, a ring of flame on the ground.
     */
    public static void fujiCharge(FireMesh.Sink sink, double x, double y, double z, double ticks, double big, int seed) {
        double f = Math.min(1.0, ticks / FUJI_CHARGE);
        double r = (1.1 - 0.6 * f) * big;
        FireMesh.ring(sink, x, y + 0.05, z, 0, 1, 0, r + 0.2, 0.35 * big, 0.1, 0x2DB8EC, 0.6 * f);
        for (int k = 0; k < 8; k++) {
            double a = 2 * Math.PI * (k + 0.3 * FireMesh.rand(seed, k)) / 8.0 + ticks * 0.15;
            double life = 6.0 + 2.0 * FireMesh.rand(seed, k + 8);
            double age = (ticks + life * FireMesh.rand(seed, k + 16)) % life;
            PhoenixFire.tongue(sink, x + Math.cos(a) * r, y, z + Math.sin(a) * r, -Math.cos(a) * 0.04, 0.0, -Math.sin(a) * 0.04, age, life,
                (0.6 + 0.8 * f) * big, k % 3 == 0, seed + k * 13 + (int) ((ticks + life * FireMesh.rand(seed, k + 16)) / life));
        }
    }

    /**
     * The streak of fire left along the dash from (x, y, z) along (dx, dy, dz) (the whole way), the one dashing {@code got} of the way
     * along it: tongues standing along the path behind them, licking up and back and burning down from the start, a white shock ring
     * on the ground where it set off; {@code ticks} since the dash began.
     */
    public static void fujiTrail(FireMesh.Sink sink, double x, double y, double z, double dx, double dy, double dz, double got, double ticks,
                                 double big, int seed) {
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.1) {
            return;
        }
        double ux = dx / len;
        double uz = dz / len;
        // the start's shock: a white ring running out over the ground and gone in 8 ticks
        if (ticks < 8.0) {
            double f = ticks / 8.0;
            FireMesh.ring(sink, x, y + 0.08, z, 0, 1, 0, (0.8 + 3.2 * f) * big, 0.55 * big, 0.25, 0xF4FBFF, 0.85 * (1.0 - f));
            FireMesh.ring(sink, x, y + 0.08, z, 0, 1, 0, (0.5 + 2.0 * f) * big, 0.3 * big, 0.1, 0xD2FBFF, 0.6 * (1.0 - f));
        }
        int n = (int) Math.max(8, Math.min(24, len * 2.0));
        for (int k = 0; k < n; k++) {
            double u = (k + 0.5) / n;
            if (u > got) {
                break;
            }
            // each tongue born as the dasher passes it, lives 14 ticks, the side ones fanned out (the reference's band of flame along
            // the dash, taller than the one dashing)
            double born = u / Math.max(1.0E-3, got) * Math.min(ticks, FUJI_DASH);
            double age = ticks - born;
            double life = 14.0 + 4.0 * FireMesh.rand(seed, k);
            if (age < 0.0 || age > life) {
                continue;
            }
            double side = (FireMesh.rand(seed, k + 30) - 0.5) * 1.2 * big;
            double px = x + dx * u - uz * side;
            double pz = z + dz * u + ux * side;
            PhoenixFire.tongue(sink, px, y + dy * u + 0.1 + 0.6 * big * FireMesh.rand(seed, k + 40), pz, -ux * 0.08, 0.0, -uz * 0.08, age, life,
                (1.4 + 1.0 * FireMesh.rand(seed, k + 50)) * big, FireMesh.rand(seed, k + 60) < 0.3, seed + k * 7);
        }
        // the band's light along it, fading from its start
        for (int k = 0; k < 4; k++) {
            double u = (k + 0.5) / 4.0;
            if (u > got) {
                break;
            }
            double fade = Math.max(0.0, 1.0 - (ticks - u * FUJI_DASH) / 14.0);
            FireMesh.shell(sink, x + dx * u, y + dy * u + 0.9 * big, z + dz * u, 1.4 * big, 0x46DDF5, 0.18 * fade, 0.0);
        }
    }

    /**
     * The comet of fire round the one dashing at (x, y, z) (their middle) along (wx, wy, wz) (unit): a white-hot core, a sheath of
     * tongues streaming back off them, cyan and gold; {@code big} 1 for the human, about 2 for the phoenix (the reference's screen-filling
     * burst as it rams).
     */
    public static void fujiSheath(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double ticks, double big,
                                  int seed) {
        double sx = -wz;
        double sz = wx;
        double sl = Math.sqrt(sx * sx + sz * sz);
        if (sl < 1.0E-4) {
            sx = 1.0;
            sz = 0.0;
            sl = 1.0;
        }
        sx /= sl;
        sz /= sl;
        int n = big > 1.5 ? 18 : 12;
        for (int k = 0; k < n; k++) {
            double a = 2 * Math.PI * k / n + ticks * 0.6;
            // a ring round it set a little ahead of it, so the flames streaming back wrap it whole (the comet's head)
            double r = (0.45 + 0.45 * FireMesh.rand(seed, k)) * big;
            double ahead = (0.3 + 0.5 * FireMesh.rand(seed, k + 40)) * big;
            double ox = sx * Math.cos(a) * r + wx * ahead;
            double oy = Math.sin(a) * r + wy * ahead;
            double oz = sz * Math.cos(a) * r + wz * ahead;
            double life = 5.0 + 2.0 * FireMesh.rand(seed, k + 10);
            double age = (ticks + life * FireMesh.rand(seed, k + 20)) % life;
            PhoenixFire.tongue(sink, x + ox, y + oy, z + oz, -wx * 0.35, -wy * 0.35, -wz * 0.35, age, life, (1.3 + 0.8 * FireMesh.rand(seed, k + 30)) * big,
                k % 3 == 1, seed + k * 11 + (int) ((ticks + life * FireMesh.rand(seed, k + 20)) / life));
        }
        PhoenixFire.glow(sink, new double[] {x, y, z, x - wx * big, y - wy * big, z - wz * big}, 2, 1.6 * big, ticks);
    }

    /**
     * The end of the dash at (x, y, z) facing (wx, wz) (flat, unit), {@code ticks} after it (the GIF's 36 to 54): a great spray of
     * blue and gold tongues flung forward and up, a dome of white smoke swelling and thinning, thin white shock arcs round it.
     */
    public static void fujiBurst(FireMesh.Sink sink, double x, double y, double z, double wx, double wz, double ticks, double big, int seed) {
        if (ticks < 0.0 || ticks > FUJI_BURST) {
            return;
        }
        double f = ticks / FUJI_BURST;
        double out = 1.0 - Math.pow(1.0 - f, 2.2);
        // the smoke dome
        FireMesh.shell(sink, x, y, z, (0.8 + 2.6 * out) * big, 0xE6EEF4, 0.32 * (1.0 - f), 0.05 * (1.0 - f));
        // the spray: tongues flung out in a fan ahead and up
        for (int k = 0; k < 22; k++) {
            double a = (FireMesh.rand(seed, k) - 0.5) * 2.6;
            double up = 0.25 + 0.9 * FireMesh.rand(seed, k + 40);
            double ca = Math.cos(a);
            double sa = Math.sin(a);
            double fx = wx * ca - wz * sa;
            double fz = wz * ca + wx * sa;
            double reach = (0.6 + 2.8 * out) * big * (0.6 + 0.6 * FireMesh.rand(seed, k + 80));
            PhoenixFire.tongue(sink, x + fx * reach, y + up * reach * 0.6, z + fz * reach, fx * 0.12, up * 0.05, fz * 0.12, ticks, FUJI_BURST,
                (1.1 + 1.0 * FireMesh.rand(seed, k + 90)) * big * (1.0 - 0.3 * f), FireMesh.rand(seed, k + 99) < 0.35, seed + k * 17);
        }
        // the shock arcs (the GIF's 45 to 54): thin white crescents round it, opening out
        if (ticks > 4.0 && ticks < 16.0) {
            double g = (ticks - 4.0) / 12.0;
            for (int k = 0; k < 3; k++) {
                double r = (1.4 + 2.4 * g + k * 0.5) * big;
                FireMesh.arc(sink, x, y + 0.6 * big, z, wx, 0.0, wz, r, 0.12 * big, k * 2.1 + ticks * 0.08, 1.9, 0.3, 0xFFFFFF, 0.7 * (1.0 - g));
            }
        }
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

    /** The burning leg from the hip (hx, hy, hz) to the foot (fx, fy, fz): a tube of blue fire, tongues licking off it. */
    public static void legFire(FireMesh.Sink sink, double hx, double hy, double hz, double fx, double fy, double fz, double ticks, double amount,
                               int seed) {
        if (amount < 0.02) {
            return;
        }
        double dx = fx - hx;
        double dy = fy - hy;
        double dz = fz - hz;
        MochiArt.tubeBegin();
        for (int i = 0; i <= 6; i++) {
            double t = i / 6.0;
            double swell = 1.0 + 0.18 * Math.sin(t * 8.0 - ticks * 1.1);
            double end = Math.sqrt(Math.max(0.0, Math.sin(Math.PI * Math.min(1.0, 0.1 + t * 0.85))));
            MochiArt.tubePoint(hx + dx * t, hy + dy * t, hz + dz * t, 0.2 * amount * Math.max(0.4, end) * swell);
        }
        MochiArt.tube(sink, 8, PhoenixFire.CYAN_TUBE, 1.6, ticks * 0.3);
        for (int k = 0; k < 4; k++) {
            double t = 0.3 + 0.2 * k;
            double life = 6.0 + 2.0 * FireMesh.rand(seed, k);
            double age = (ticks + life * FireMesh.rand(seed, k + 5)) % life;
            PhoenixFire.tongue(sink, hx + dx * t, hy + dy * t, hz + dz * t, 0.0, 0.0, 0.0, age, life, 0.6 * amount, k == 2,
                seed + k * 9 + (int) ((ticks + life * FireMesh.rand(seed, k + 5)) / life));
        }
    }

    /**
     * The slam at (x, y, z) (on the ground), {@code ticks} after it (the GIF's 57 to 84): a white flash dome, a blast of blue and gold
     * flame thrown up and out, then the crater: the ground scorched dark blue and a wall of blue flames licking up round its rim, burning
     * down over {@link #TALON_CRATER} ticks.
     */
    public static void talonBlast(FireMesh.Sink sink, double x, double y, double z, double ticks, double radius, int seed) {
        if (ticks < 0.0 || ticks > TALON_CRATER) {
            return;
        }
        // the flash (white, a few ticks)
        if (ticks < 6.0) {
            double f = ticks / 6.0;
            FireMesh.shell(sink, x, y + 0.3, z, radius * (0.5 + 1.0 * f), 0xFFFFFF, 0.35 * (1.0 - f), 0.25 * (1.0 - f));
        }
        // the blast: tongues thrown up and out
        if (ticks < 18.0) {
            double f = ticks / 18.0;
            double out = 1.0 - Math.pow(1.0 - f, 2.0);
            for (int k = 0; k < 14; k++) {
                double a = 2 * Math.PI * (k + 0.4 * FireMesh.rand(seed, k)) / 14.0;
                double r = radius * (0.2 + 0.8 * out) * (0.6 + 0.5 * FireMesh.rand(seed, k + 20));
                PhoenixFire.tongue(sink, x + Math.cos(a) * r, y + 0.2 + 1.5 * out * FireMesh.rand(seed, k + 40), z + Math.sin(a) * r, Math.cos(a) * 0.1,
                    0.08, Math.sin(a) * 0.1, ticks, 18.0, 2.2 * (1.0 - 0.4 * f), FireMesh.rand(seed, k + 60) < 0.35, seed + k * 21);
            }
        }
        // the crater: the scorch (a dark blue disc, its edge soft) and the wall of fire round its rim
        double burn = ticks < 8.0 ? ticks / 8.0 : Math.max(0.0, 1.0 - (ticks - 8.0) / (TALON_CRATER - 8.0));
        FireMesh.ring(sink, x, y + 0.04, z, 0, 1, 0, radius * 0.5, radius * 1.05, 0.0, 0x0D2A6A, 0.55 * burn);
        FireMesh.ring(sink, x, y + 0.05, z, 0, 1, 0, radius * 0.95, 0.6, 0.12, 0x2DB8EC, 0.6 * burn);
        int n = 16;
        for (int k = 0; k < n; k++) {
            double a = 2 * Math.PI * (k + 0.5 * FireMesh.rand(seed, k + 70)) / n;
            double life = 8.0 + 3.0 * FireMesh.rand(seed, k + 75);
            double start = life * FireMesh.rand(seed, k + 80);
            double age = (ticks + start) % life;
            PhoenixFire.tongue(sink, x + Math.cos(a) * radius, y + 0.05, z + Math.sin(a) * radius, -Math.cos(a) * 0.02, 0.0, -Math.sin(a) * 0.02,
                age, life, (1.5 + 1.0 * FireMesh.rand(seed, k + 85)) * burn, k % 4 == 0, seed + k * 5 + (int) ((ticks + start) / life));
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

    /** The grab's whirl round (x, y, z), {@code ticks} into it (the GIF's 27 to 42): white rings sweeping round, tilted, opening out. */
    public static void hawkWhirl(FireMesh.Sink sink, double x, double y, double z, double ticks) {
        if (ticks < 0.0 || ticks > HAWK_WHIRL + 6) {
            return;
        }
        double f = ticks / (HAWK_WHIRL + 6.0);
        double a = 1.0 - f;
        for (int k = 0; k < 3; k++) {
            double r = 2.0 + 1.8 * f + 0.6 * k;
            FireMesh.arc(sink, x, y + (k - 1) * 0.5, z, 0.15 * k, 1.0, 0.1, r, 0.18, ticks * 0.6 + k * 2.0, 3.8, 0.3, 0xFFFFFF, 0.7 * a);
        }
        FireMesh.ring(sink, x, y - 0.8, z, 0, 1, 0, 1.5 + 3.0 * f, 0.5, 0.2, 0xF4FBFF, 0.5 * a);
    }

    // ------------------------------------------------------------------------------------------------------ 3.7.3: Phoenix Inferno

    /**
     * A kick of the barrage from the hip (hx, hy, hz) out along (wx, wz) (flat, unit), {@code ticks} after it (the GIF's 20 to 56: the leg
     * a blur of fire, white arcs of air in front): the burning leg thrust out and back, a white crescent of air ahead of the foot.
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
        legFire(sink, hx, hy, hz, fx, fy, fz, ticks, 1.2 * out + 0.3, seed);
        double a = 1.0 - ticks / 6.0;
        FireMesh.arc(sink, fx + wx * 0.3, fy, fz + wz * 0.3, wx, up ? 0.8 : 0.0, wz, 0.7 + 1.3 * (ticks / 6.0), 0.16, seed * 0.7, 2.8, 0.25,
            0xFFFFFF, 0.85 * a);
        FireMesh.ring(sink, fx + wx * 0.2, fy, fz + wz * 0.2, wx, 0.0, wz, 0.4 + 1.1 * (ticks / 6.0), 0.16, 0.05, 0xF4FBFF, 0.65 * a);
        // a tongue flung off the foot along the kick
        PhoenixFire.tongue(sink, fx, fy, fz, wx * 0.2, 0.05, wz * 0.2, ticks, 6.0, 0.9, seed % 3 == 0, seed * 3);
    }

    /**
     * Blue fire bursting round the one at (x, y, z) (their middle), {@code ticks} into it, {@code big} its size: the wings and tail forming
     * (the GIF's 12 to 16), or the landing burst scattering them (76 to 84): tongues flung out all round, gold embers.
     */
    public static void burst(FireMesh.Sink sink, double x, double y, double z, double ticks, double big, int seed) {
        if (ticks < 0.0 || ticks > 14.0) {
            return;
        }
        double f = ticks / 14.0;
        double out = 1.0 - Math.pow(1.0 - f, 2.0);
        for (int k = 0; k < 12; k++) {
            double a = 2 * Math.PI * (k + 0.4 * FireMesh.rand(seed, k)) / 12.0;
            double up = FireMesh.rand(seed, k + 20) * 1.6 - 0.5;
            double r = (0.4 + 1.6 * out) * big;
            PhoenixFire.tongue(sink, x + Math.cos(a) * r, y + up * r * 0.5, z + Math.sin(a) * r, Math.cos(a) * 0.1, 0.02, Math.sin(a) * 0.1, ticks,
                14.0, (0.8 + 0.6 * FireMesh.rand(seed, k + 40)) * big, k % 3 == 0, seed + k * 13);
        }
        for (int k = 0; k < 10; k++) {
            double a = 2 * Math.PI * FireMesh.rand(seed, k + 60);
            double r = (0.6 + 2.4 * out) * big * FireMesh.rand(seed, k + 70);
            FireMesh.shard(sink, x + Math.cos(a) * r, y + 0.4 + 1.2 * out * FireMesh.rand(seed, k + 80) * big, z + Math.sin(a) * r, 0.12, ticks * 0.4,
                seed + k, 0xFFD84E, 1.0 - f);
        }
    }

    /**
     * The phoenix's whirlpool of fire round (x, y, z) (the ground under it), {@code ticks} into it (Inferno2's 12 to 96): rings of cyan and
     * white fire swirling round it at heights, tongues racing round it and up, white flames flung round; {@code radius} out.
     */
    public static void vortex(FireMesh.Sink sink, double x, double y, double z, double ticks, double radius, int seed) {
        if (ticks < 0.0 || ticks > VORTEX + 6) {
            return;
        }
        double in = Math.min(1.0, ticks / 8.0);
        double out = Math.min(1.0, Math.max(0.0, (VORTEX + 6 - ticks) / 8.0));
        double k0 = Math.min(in, out);
        // the swirling bands (cyan and white), each at its height and speed
        for (int b = 0; b < 7; b++) {
            double h = 0.3 + b * 0.85;
            double r = radius * (0.6 + 0.09 * b) * (0.6 + 0.4 * in);
            double turn = ticks * (0.28 + 0.04 * b) + b * 1.1;
            int col = b % 2 == 0 ? 0x46DDF5 : 0xF4FBFF;
            FireMesh.arc(sink, x, y + h, z, 0.06 * Math.sin(b), 1.0, 0.06 * Math.cos(b), r, 1.5 - 0.1 * b, turn, 4.4, 0.8, col, (0.6 - 0.04 * b) * k0);
        }
        FireMesh.ring(sink, x, y + 0.05, z, 0, 1, 0, radius * 0.9, radius * 0.5, 0.0, 0x2DB8EC, 0.3 * k0);
        // the tongues racing round it, climbing
        int n = 20;
        for (int k = 0; k < n; k++) {
            double a = 2 * Math.PI * k / n + ticks * 0.32;
            double r = radius * (0.55 + 0.4 * FireMesh.rand(seed, k));
            double h = (FireMesh.rand(seed, k + 20) * 4.0 + 0.12 * ((ticks + k * 3) % 20));
            double c = Math.cos(a);
            double sn = Math.sin(a);
            double life = 8.0 + 3.0 * FireMesh.rand(seed, k + 30);
            double age = (ticks + life * FireMesh.rand(seed, k + 40)) % life;
            PhoenixFire.tongue(sink, x + c * r, y + h, z + sn * r, -sn * 0.18, 0.02, c * 0.18, age, life, (1.7 + 1.1 * FireMesh.rand(seed, k + 50)) * k0,
                k % 4 == 0, seed + k * 7 + (int) ((ticks + life * FireMesh.rand(seed, k + 40)) / life));
        }
    }

    /**
     * The phoenix's blast at (x, y, z) (on the ground), {@code ticks} after it (Inferno2's 132 to 168): a flash, a great burst of blue, white
     * and gold fire thrown up and out, rubble of the ground flung up and falling, the scorched crater with its rim of flame.
     */
    public static void vortexBlast(FireMesh.Sink sink, double x, double y, double z, double ticks, double radius, int seed) {
        if (ticks < 0.0 || ticks > VORTEX_BLAST_LIFE) {
            return;
        }
        talonBlast(sink, x, y, z, ticks * (TALON_CRATER / (double) VORTEX_BLAST_LIFE), radius, seed);
        if (ticks < 12.0) {
            double f = ticks / 12.0;
            FireMesh.shell(sink, x, y + 1.0, z, radius * (0.6 + 0.8 * f), 0x8FF0FA, 0.35 * (1.0 - f), 0.15 * (1.0 - f));
            for (int k = 0; k < 10; k++) {
                double a = 2 * Math.PI * (k + 0.3 * FireMesh.rand(seed, k + 90)) / 10.0;
                double r = radius * 0.5 * f;
                PhoenixFire.tongue(sink, x + Math.cos(a) * r, y + 0.5 + 3.0 * f * FireMesh.rand(seed, k + 95), z + Math.sin(a) * r, Math.cos(a) * 0.05,
                    0.15, Math.sin(a) * 0.05, ticks, 12.0, 2.6 * (1.0 - 0.3 * f), k % 3 == 0, seed + k * 29);
            }
        }
        // the rubble: blocks of the ground flung up and falling back
        for (int k = 0; k < 14; k++) {
            double a = 2 * Math.PI * FireMesh.rand(seed, k + 110);
            double v = 0.25 + 0.25 * FireMesh.rand(seed, k + 120);
            double up = 0.45 + 0.35 * FireMesh.rand(seed, k + 130);
            double tt = Math.min(ticks, 30.0);
            double h = up * tt - 0.04 * tt * tt;
            if (h < -0.2) {
                continue;
            }
            double r = 0.5 + v * tt;
            int stone = FireMesh.rand(seed, k + 140) < 0.5 ? 0x6E6A66 : 0x8A847C;
            FireMesh.shard(sink, x + Math.cos(a) * r, y + Math.max(0.0, h), z + Math.sin(a) * r, 0.35 + 0.3 * FireMesh.rand(seed, k + 150), tt * 0.3,
                seed + k, stone, 1.0);
        }
    }

    // ------------------------------------------------------------------------------------------------------ 3.7.3: Bluebird

    /**
     * Bluebird's ball of yellow and blue fire at (x, y, z), {@code size} across its heart, {@code ticks} its clock (the GIF's 42 to 140:
     * crackling with rays, swirling gold arcs, growing): a white-hot heart, cyan and gold tongues licking out of it, rays of light
     * crackling off it, gold arcs swirling round.
     */
    public static void blueBall(FireMesh.Sink sink, double x, double y, double z, double size, double ticks, int seed) {
        blueBall(sink, x, y, z, size, ticks, seed, false);
    }

    /** As above; {@code lite}: a ball in flight (the phoenix throws four at once: fewer tongues and rays, one arc, no halo). */
    public static void blueBall(FireMesh.Sink sink, double x, double y, double z, double size, double ticks, int seed, boolean lite) {
        FireMesh.shell(sink, x, y, z, size * 0.55, 0xFFFFFF, 0.9, 0.3);
        if (!lite) {
            FireMesh.shell(sink, x, y, z, size * 1.1, 0x8FF0FA, 0.35, 0.0);
        }
        int tongues = lite ? 5 : 7;
        for (int k = 0; k < tongues; k++) {
            double a = 2 * Math.PI * (k + 0.5 * FireMesh.rand(seed, k)) / tongues + ticks * 0.2;
            double b = (FireMesh.rand(seed, k + 10) - 0.5) * 2.0;
            double ux = Math.cos(a) * Math.cos(b);
            double uy = Math.sin(b);
            double uz = Math.sin(a) * Math.cos(b);
            double life = 5.0 + 2.0 * FireMesh.rand(seed, k + 20);
            double age = (ticks + life * FireMesh.rand(seed, k + 30)) % life;
            PhoenixFire.tongue(sink, x + ux * size * 0.4, y + uy * size * 0.4, z + uz * size * 0.4, ux * 0.08, uy * 0.08, uz * 0.08, age, life,
                size * (0.6 + 0.5 * FireMesh.rand(seed, k + 40)), k % 2 == 0, seed + k * 5 + (int) ((ticks + life * FireMesh.rand(seed, k + 30)) / life));
        }
        // the crackling rays (each a flicker of a few ticks), white and gold
        for (int k = 0; k < (lite ? 4 : 8); k++) {
            int round = (int) (ticks / 3.0);
            double a = 2 * Math.PI * FireMesh.rand(seed + round, k);
            double b = (FireMesh.rand(seed + round, k + 8) - 0.5) * 2.8;
            FireMesh.ray(sink, x, y, z, Math.cos(a) * Math.cos(b), Math.sin(b), Math.sin(a) * Math.cos(b), size * (1.2 + 1.2 * FireMesh.rand(seed + round, k + 16)),
                0.06 * size + 0.03, 0, 0.0, k % 2 == 0 ? 0xFFFFFF : 0xFFD84E, 0.8);
        }
        for (int k = 0; k < (lite ? 1 : 2); k++) {
            FireMesh.arc(sink, x, y, z, Math.sin(ticks * 0.1 + k * 1.7), 1.0, Math.cos(ticks * 0.13 + k), size * (0.85 + 0.2 * k), 0.08 * size + 0.04,
                ticks * 0.5 + k * 3.0, 2.6, 0.15, 0xFFBE2E, 0.75);
        }
    }

    /** A ball in flight's trail: four tongues streaming back off it along (wx, wy, wz) (unit), cyan and gold. */
    public static void blueTrail(FireMesh.Sink sink, double x, double y, double z, double wx, double wy, double wz, double ticks, double size,
                                 int seed) {
        for (int k = 0; k < 3; k++) {
            double life = 6.0;
            double start = life * k / 3.0;
            double age = (ticks + start) % life;
            double a = 2 * Math.PI * k / 3.0 + ticks * 0.5;
            double sx = -wz * Math.cos(a) * size * 0.35;
            double sz = wx * Math.cos(a) * size * 0.35;
            double sy = Math.sin(a) * size * 0.35;
            PhoenixFire.tongue(sink, x + sx, y + sy, z + sz, -wx * 0.4, -wy * 0.4, -wz * 0.4, age, life, size * 1.3, k % 2 == 0,
                seed + k * 3 + (int) ((ticks + start) / life));
        }
    }

    /** Bluebird's blast at (x, y, z), {@code ticks} after it, {@code radius} out: a white flash, a burst of blue and gold fire, gold embers. */
    public static void blueBoom(FireMesh.Sink sink, double x, double y, double z, double ticks, double radius, int seed) {
        if (ticks < 0.0 || ticks > BLUE_BOOM) {
            return;
        }
        double f = ticks / BLUE_BOOM;
        if (ticks < 6.0) {
            FireMesh.shell(sink, x, y, z, radius * (0.5 + 0.8 * ticks / 6.0), 0xFFFFFF, 0.35 * (1.0 - ticks / 6.0), 0.15 * (1.0 - ticks / 6.0));
        }
        FireMesh.ring(sink, x, y, z, 0, 1, 0, radius * (0.5 + 1.2 * f), 0.5, 0.2, 0x46DDF5, 0.7 * (1.0 - f));
        // eight tongues flung out all round (the four balls' blasts can all burn at once beside the phoenix: kept light)
        double out = 1.0 - Math.pow(1.0 - f, 2.0);
        for (int k = 0; k < 8; k++) {
            double a = 2 * Math.PI * (k + 0.4 * FireMesh.rand(seed, k)) / 8.0;
            double up = FireMesh.rand(seed, k + 20) * 1.6 - 0.5;
            double r = (0.3 + 1.2 * out) * radius * 0.7;
            PhoenixFire.tongue(sink, x + Math.cos(a) * r, y + up * r * 0.5, z + Math.sin(a) * r, Math.cos(a) * 0.1, 0.02, Math.sin(a) * 0.1, ticks,
                BLUE_BOOM, (0.8 + 0.6 * FireMesh.rand(seed, k + 40)) * radius * 0.6, k % 3 == 0, seed + k * 13);
        }
    }
}
