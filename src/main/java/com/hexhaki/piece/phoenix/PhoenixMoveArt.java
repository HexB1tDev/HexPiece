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
            FireMesh.shell(sink, x, y + 0.3, z, radius * (0.5 + 1.0 * f), 0xFFFFFF, 0.5 * (1.0 - f), 0.3 * (1.0 - f));
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
}
