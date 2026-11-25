package com.hexhaki.piece.neko;

import com.hexhaki.piece.dragon.FireMesh;

/**
 * 3.7.9: the Leopard fruit's effects in the house's own 3D style (dragon/FireMesh's banded puffs, arcs, rings, streaks, shells; never
 * tongue particles, never additive): the change's black flames (the owner: "the mane and body hair IS black flames"; the reference's
 * Transform: the human slouched in black flames, a roar and a black dome bursting out as the leopard is revealed), and
 * Six Styles' strikes (the reference's SixStylesBase: white slashes and arcs round each blow, a flash where it lands, Rokuogan's white
 * shock with a dark red rim). Pure: the client, the previews and the tests share it; points eye-relative.
 */
public final class LeopardArt {
    private LeopardArt() {
    }

    /**
     * The black fire's bands, coolest to hottest (the owner: "black flames not purple"): black through charcoal, its hottest licks a dark
     * smoky grey; no purple, no red.
     */
    public static final int[] BLACK_FIRE = {0x000000, 0x030303, 0x070707, 0x0C0C0C, 0x141414, 0x1E1E1E, 0x2B2B2B, 0x3A3A3A};
    /** The dark haze round the change (was a purple glow). */
    static final int VIOLET = 0x080808;

    static double clamp(double t) {
        return Math.max(0.0, Math.min(1.0, t));
    }

    /** A puff of black fire (FireMesh.puff in {@link #BLACK_FIRE}), its palette put back after. */
    public static void puff(FireMesh.Sink sink, double x, double y, double z, double radius, double vx, double vy, double vz, double stretch,
                            double heat, double alpha, double time, int seed) {
        FireMesh.palette(BLACK_FIRE);
        FireMesh.puff(sink, x, y, z, radius, vx, vy, vz, stretch, 0.32, heat, 0.3, false, alpha, time, seed);
        FireMesh.palette(FireMesh.BAND);
    }

    // ------------------------------------------------------------------------------------------------------ the change

    /**
     * The owner: "the transformation should be a ball of burning flames with evil red eyes within": a ball of black flames {@code radius}
     * round (x, y, z), its flames flying in from all round as it gathers ({@code gather} 0 to 1: nothing grows), burning and licking up,
     * rolling slowly round it, a dark heart inside, a dark haze round it; two evil red eyes glaring out of it the way it faces
     * ({@code fx}, {@code fz}); burning away ({@code burn} 1 to 0) its flames rising and thinning out.
     */
    public static void ball(FireMesh.Sink sink, double x, double y, double z, double radius, double fx, double fz, double gather, double burn,
                            double ticks, int seed) {
        double a = gather * burn;
        if (a < 0.01) {
            return;
        }
        FireMesh.shell(sink, x, y, z, radius * 1.5, VIOLET, 0.2 * a, 0.0);
        FireMesh.shell(sink, x, y, z, radius * 0.78, 0x050208, 0.85 * a, 0.95 * a);
        int n = 16;
        double spin = ticks * 0.05;
        double quality = FireMesh.quality;
        FireMesh.quality = quality * 0.7;
        for (int i = 0; i < n; i++) {
            // a golden spiral over the ball, rolling round it
            double yy = 1.0 - 2.0 * (i + 0.5) / n;
            double rr = Math.sqrt(Math.max(0.0, 1.0 - yy * yy));
            double th = i * 2.39996 + spin;
            double dx = Math.cos(th) * rr;
            double dz = Math.sin(th) * rr;
            double arrive = clamp(gather * 1.6 - FireMesh.rand(seed, i) * 0.6);
            double out = radius * (0.82 + 0.12 * Math.sin(ticks * 0.4 + i)) * (1.0 + 1.4 * (1.0 - arrive));
            double rise = (1.0 - burn) * radius * (0.8 + FireMesh.rand(seed, 20 + i));
            double flick = 0.5 + 0.5 * Math.sin(ticks * 0.9 + i * 1.7);
            puff(sink, x + dx * out, y + yy * out + rise, z + dz * out, radius * 0.4 * (0.85 + 0.15 * flick), dx * 0.3, 1.0, dz * 0.3, 0.9,
                0.18 + 0.26 * flick * burn, arrive * burn, ticks * 1.3 + i * 5, seed + i);
        }
        for (int i = 0; i < 3; i++) {
            double ph = (ticks * 0.07 + i / 3.0) % 1.0;
            puff(sink, x + Math.sin(i * 2.1) * radius * 0.3, y + radius * (0.8 + 0.9 * ph), z + Math.cos(i * 2.1) * radius * 0.3,
                radius * 0.3 * (1.0 - 0.6 * ph), 0, 1, 0, 1.0, 0.45 * (1.0 - ph), a * (1.0 - ph * ph), ticks + i * 9, seed + 40 + i);
        }
        FireMesh.quality = quality;
        // the eyes: glaring red out of the black, slanted down toward each other (evil), flaring now and then
        double eyes = clamp((gather - 0.6) / 0.4) * burn;
        if (eyes <= 0.01) {
            return;
        }
        double fl = Math.max(1.0E-4, Math.sqrt(fx * fx + fz * fz));
        double ux = fx / fl;
        double uz = fz / fl;
        double sx = -uz;
        double sz = ux;
        double flare = 0.85 + 0.15 * Math.sin(ticks * 0.6) + (Math.sin(ticks * 0.21) > 0.97 ? 0.3 : 0.0);
        for (int k = -1; k <= 1; k += 2) {
            double ex = x + ux * radius * 0.98 + sx * radius * 0.26 * k;
            double ey = y + radius * 0.12;
            double ez = z + uz * radius * 0.98 + sz * radius * 0.26 * k;
            FireMesh.shell(sink, ex, ey, ez, radius * 0.17, 0xFF1414, 0.75 * eyes * flare, 0.0);
            // the slit: a short bright streak, its inner end lower
            FireMesh.streak(sink, ex, ey, ez, -sx * k, -0.38, -sz * k, radius * 0.3, radius * 0.05, 0xFF2020, eyes);
            FireMesh.shell(sink, ex, ey, ez, radius * 0.05, 0xFFD8D8, 0.9 * eyes, 0.0);
        }
    }

    /**
     * The owner: "the flames of the mane, tail and ribbon make sure those are real looking burning flames": a tongue of black flame rising
     * off a point of the mane, tail hair or ribbon ({@code size} blocks), born again and again ({@code k} its own beat): it leaves the
     * hair hot and full, licks up and away with the wind, thinning and cooling to black smoke as it goes.
     */
    public static void tongue(FireMesh.Sink sink, double x, double y, double z, double size, double wx, double wz, double ticks, int k) {
        double ph = (ticks * 0.085 + k * 0.618) % 1.0;
        double rise = ph * size * 2.2;
        double r = size * (0.55 - 0.35 * ph);
        double heat = 0.62 - 0.45 * ph + 0.08 * Math.sin(ticks * 1.3 + k);
        puff(sink, x + wx * rise, y + rise, z + wz * rise, r, wx, 1.0, wz, 1.1, heat, 1.0 - ph * ph, ticks * 1.5 + k * 3, k);
    }

    /**
     * The black flames rising round the human as they slouch ({@code aura} 0 to 1), before the ball closes over them, and a dark haze.
     * (x, y, z) the feet; {@code unused} kept 0.
     */
    public static void human(FireMesh.Sink sink, double x, double y, double z, double unused, double aura, double ticks, int seed) {
        if (aura <= 0.01) {
            return;
        }
        FireMesh.shell(sink, x, y + 1.0, z, 1.8 * (0.7 + 0.3 * aura), VIOLET, 0.22 * aura, 0.0);
        for (int i = 0; i < 7; i++) {
            double a = i * 2.39996 + ticks * 0.08;
            double r = 0.7 + 0.25 * Math.sin(ticks * 0.3 + i);
            double rise = (ticks * 0.11 + i * 0.37) % 1.0;
            puff(sink, x + Math.cos(a) * r, y + rise * 2.2, z + Math.sin(a) * r, 0.32 * (1.0 - rise * 0.6), 0, 1, 0, 0.7,
                0.35 + 0.4 * (1.0 - rise), aura, ticks + i * 5, seed + i);
        }
    }

    /** How long the roar's dome lasts (ticks). */
    public static final double ROAR_LIFE = 16.0;

    /**
     * The roar (the reference: a black dome bursting out as the form is revealed): a black shell racing out round the leopard with a dark
     * purple rim, a ring over the ground, black flames thrown out, white shock crescents. (x, y, z) its feet, {@code age} ticks.
     */
    public static void roar(FireMesh.Sink sink, double x, double y, double z, double age, int seed) {
        if (age < 0.0 || age > ROAR_LIFE) {
            return;
        }
        double t = age / ROAR_LIFE;
        double r = 2.0 + 6.5 * Math.sqrt(t);
        FireMesh.shell(sink, x, y + 2.0, z, r, 0x0A0410, 0.35 * (1.0 - t), 0.85 * (1.0 - t));
        FireMesh.ring(sink, x, y + 0.1, z, 0, 1, 0, r * 1.1, 0.8 * (1.0 - t) + 0.2, 0.3, 0x111111, 0.8 * (1.0 - t));
        for (int i = 0; i < 3; i++) {
            double a = FireMesh.rand(seed, i) * Math.PI * 2.0;
            FireMesh.arc(sink, x, y + 2.0, z, Math.cos(a) * 0.3, 1.0, Math.sin(a) * 0.3, r * 0.9, 0.5 * (1.0 - t), a, 3.4, 0.0, 0xFFFFFF,
                0.8 * (1.0 - t));
        }
        int n = 8;
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2.0 / n + FireMesh.rand(seed, 10 + i);
            double d = r * 0.75;
            puff(sink, x + Math.cos(a) * d, y + 0.6 + 1.2 * FireMesh.rand(seed, 20 + i) + t * 1.5, z + Math.sin(a) * d, 0.7 * (1.0 - 0.5 * t),
                Math.cos(a), 0.6, Math.sin(a), 0.8, 0.7 - 0.5 * t, 1.0 - t * t, age * 1.5, seed + i);
        }
    }

    // ------------------------------------------------------------------------------------------------------ Six Styles

    /** How long a blow's strike and its landing are seen (ticks). */
    public static final double STRIKE_LIFE = 7.0;
    public static final double LAND_LIFE = 8.0;
    public static final double ROKU_LIFE = 16.0;

    /**
     * A blow of Six Styles thrown ({@code blow} 1 to 5, {@code big} 1 the human, about 1.6 the leopard), {@code age} ticks after it lands:
     * white slashes in front of the chest (x, y, z) along the look (lx, ly, lz): a rising arc for the front kick, three claw streaks for
     * the swipe, a wide flat arc for the roundhouse, two jab streaks for the double punch; Rokuogan's is its blast ({@link #rokuogan}).
     */
    public static void strike(FireMesh.Sink sink, int blow, double x, double y, double z, double lx, double ly, double lz, double big, double age,
                              int seed) {
        if (age < 0.0 || age > STRIKE_LIFE || blow == 5) {
            return;
        }
        double t = age / STRIKE_LIFE;
        double fade = 1.0 - t * t;
        double fx = lx;
        double fz = lz;
        double fl = Math.sqrt(fx * fx + fz * fz);
        if (fl < 1.0E-4) {
            fx = 0;
            fz = 1;
            fl = 1;
        }
        fx /= fl;
        fz /= fl;
        // the side (right of the look)
        double sx = -fz;
        double sz = fx;
        double reach = 1.6 * big;
        double cx = x + fx * reach;
        double cz = z + fz * reach;
        switch (blow) {
            case 1 -> FireMesh.arc(sink, cx - fx * 0.6 * big, y - 0.3 * big, cz - fz * 0.6 * big, sx, 0, sz, 1.1 * big, 0.28 * big * fade,
                3.6 + t * 0.6, 2.2, 0.0, 0xFFFFFF, 0.9 * fade);
            case 2 -> {
                for (int k = -1; k <= 1; k++) {
                    double ox = sx * 0.22 * k * big;
                    double oz = sz * 0.22 * k * big;
                    double u = 0.3 + 0.7 * Math.min(1.0, age / 2.0);
                    FireMesh.streak(sink, cx + ox - sx * 0.6 * big * (1 - u), y - 0.5 * big + 1.0 * big * u, cz + oz - sz * 0.6 * big * (1 - u),
                        sx, 0.9, sz, 1.3 * big, 0.07 * big, 0xE8E8E8, 0.95 * fade);
                }
            }
            case 3 -> FireMesh.arc(sink, x + fx * 0.4 * big, y - 0.4 * big, z + fz * 0.4 * big, 0, 1, 0, 1.6 * big, 0.3 * big * fade,
                Math.atan2(fz, fx) - 1.6 + t * 0.5, 3.0, 0.2, 0xFFFFFF, 0.9 * fade);
            default -> {
                for (int k = 0; k < 2; k++) {
                    double side = k == 0 ? 0.25 : -0.25;
                    double u = clamp((age - k * 1.5) / 2.0);
                    if (u <= 0.0) {
                        continue;
                    }
                    FireMesh.streak(sink, x + sx * side * big + fx * reach * u, y - 0.1 * big, z + sz * side * big + fz * reach * u, fx, 0, fz,
                        1.1 * big, 0.08 * big, 0xFFFFFF, 0.9 * fade);
                }
            }
        }
    }

    /**
     * Where a blow lands on someone ({@code hit}: struck; else only a puff of air): a white flash, a ring of shock facing the blow, sparks
     * thrown on. (x, y, z) the point, (lx, ly, lz) the blow's way.
     */
    public static void land(FireMesh.Sink sink, double x, double y, double z, double lx, double ly, double lz, double big, boolean hit, double age,
                            int seed) {
        if (age < 0.0 || age > LAND_LIFE) {
            return;
        }
        double t = age / LAND_LIFE;
        double fade = 1.0 - t;
        FireMesh.ring(sink, x, y, z, lx, ly, lz, (0.3 + 1.1 * Math.sqrt(t)) * big, 0.2 * big * fade + 0.03, 0.15, 0xFFFFFF, 0.8 * fade);
        if (!hit) {
            return;
        }
        if (age < 3.0) {
            FireMesh.shell(sink, x, y, z, 0.5 * big * (1.0 + age), 0xFFFFFF, 0.5 * (1.0 - age / 3.0), 0.0);
        }
        for (int i = 0; i < 6; i++) {
            double a = FireMesh.rand(seed, i) * Math.PI * 2.0;
            double b = FireMesh.rand(seed, 10 + i) * 1.2 - 0.3;
            double vx = lx + Math.cos(a) * 0.8;
            double vy = ly + b;
            double vz = lz + Math.sin(a) * 0.8;
            double out = (0.3 + 1.6 * t) * big;
            FireMesh.streak(sink, x + vx * out, y + vy * out, z + vz * out, vx, vy, vz, 0.5 * big * fade, 0.04 * big, i % 2 == 0 ? 0xFFFFFF : 0xD8D8D8,
                fade);
        }
    }

    /**
     * Rokuogan (the reference's last blow: a white shock with a dark red rim bursting out of the fists, the one hit thrown far): a shell
     * of shock out of the fists' point, two rings racing out along the blow, white crescents round it, sparks; {@code big} its size.
     */
    public static void rokuogan(FireMesh.Sink sink, double x, double y, double z, double lx, double ly, double lz, double big, double age, int seed) {
        if (age < 0.0 || age > ROKU_LIFE) {
            return;
        }
        double t = age / ROKU_LIFE;
        double fade = 1.0 - t;
        double r = big * (0.6 + 2.6 * Math.sqrt(t));
        FireMesh.shell(sink, x, y, z, r, 0xFFFFFF, 0.3 * fade * fade, 0.9 * fade);
        if (age < 4.0) {
            FireMesh.shell(sink, x, y, z, big * 0.9 * (0.5 + age / 4.0), 0xFFF4F4, 0.7 * (1.0 - age / 4.0), 0.0);
        }
        for (int k = 0; k < 2; k++) {
            double u = clamp(t * 1.4 - k * 0.2);
            double d = big * (0.4 + 2.8 * u);
            FireMesh.ring(sink, x + lx * d, y + ly * d, z + lz * d, lx, ly, lz, big * (0.6 + 1.6 * u), 0.3 * big * (1.0 - u) + 0.05, 0.2 * big,
                k == 0 ? 0xFFFFFF : 0xB0234A, 0.85 * (1.0 - u));
        }
        for (int i = 0; i < 3; i++) {
            double a = FireMesh.rand(seed, 30 + i) * Math.PI * 2.0;
            FireMesh.arc(sink, x, y, z, lx, ly, lz, r * 0.9, 0.3 * big * fade, a, 2.6, 0.4, i == 2 ? 0xB0234A : 0xFFFFFF, 0.85 * fade);
        }
        for (int i = 0; i < 8; i++) {
            double a = FireMesh.rand(seed, i) * Math.PI * 2.0;
            double b = FireMesh.rand(seed, 10 + i) * 1.4 - 0.5;
            double vx = lx * 1.2 + Math.cos(a);
            double vy = ly + b;
            double vz = lz * 1.2 + Math.sin(a);
            double out = big * (0.5 + 3.0 * t);
            FireMesh.streak(sink, x + vx * out, y + vy * out, z + vz * out, vx, vy, vz, 0.8 * big * fade, 0.05 * big, i % 3 == 0 ? 0xB0234A : 0xFFFFFF,
                fade);
        }
    }
}
