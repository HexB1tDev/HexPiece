package com.hexhaki.piece.itsumade;

import com.hexhaki.piece.dragon.FireMesh;
import java.util.Random;

/**
 * 3.9.8: the Itsumade's fire and its Haki's light, drawn as the house draws fire (dragon/FireMesh: flat cel bands cut sharp, never a
 * smear), pure so the previews draw exactly what the game draws (tools/preview/FxPreview: iball, ibeam, iring, iclouds, iburst). Every
 * point is camera-relative; every quad goes to a {@link FireMesh.Sink}. The callers set nothing: each draw sets the palette it needs and
 * puts {@link FireMesh#BAND} back.
 */
public final class ItsuArt {
    /** Its black flames (soot black to a deep violet). */
    public static final int[] YOKAI_BLACK = {0x040205, 0x0A050D, 0x120917, 0x1B0D22, 0x26112E, 0x33163B, 0x431B4A, 0x56225C};
    /** Its Haki's light (dark to light): near-black indigo, indigo, violet, pale violet, green-white (server/RyouServer.ITSU_COLOR). */
    public static final int[] HAKI = {0x07051A, 0x120C3A, 0x1E1A66, 0x2B3FD8, 0x4A52F0, 0x7E6CF5, 0xA9F5D2, 0xEFFFF7};
    /** Thy Death's clouds: soot black with a violet cast. */
    public static final int[] STORM = {0x050408, 0x09070E, 0x0E0B15, 0x14101D, 0x1A1526, 0x221B30, 0x2B233B, 0x352C47};
    public static final int INDIGO = 0x2B3FD8;
    public static final int GREEN = 0x22E08A;
    public static final int NIGHT = 0x07051A;

    private ItsuArt() {
    }

    /**
     * A ball of white-hot fire (the Yokai Beam's in the jaws, the Yokai Blast's in the hand): its heart white-hot through yellow to an
     * orange skin with a deep-red line round it, and black flames licking up round its edge (tongues, never a sheath over it: the fire
     * must show).
     */
    public static void ball(FireMesh.Sink sink, double x, double y, double z, double radius, double time, int seed) {
        try {
            FireMesh.palette(FireMesh.BAND);
            // the white-hot heart, seen through the orange skin round it
            FireMesh.puff(sink, x, y, z, radius * 0.62, 0.0, 0.02, 0.0, 0.0, 0.35, 0.97, 0.2, false, 1.0, time, seed + 5);
            FireMesh.puff(sink, x, y, z, radius, 0.0, 0.03, 0.0, 0.0, 0.55, 0.42, 0.25, true, 0.5, time, seed);
            com.hexhaki.piece.dragon.UoFire.glow(sink, x, y, z, radius * 2.4, 0xFFB84A, 0.35);
            FireMesh.palette(YOKAI_BLACK);
            for (int k = 0; k < 5; k++) {
                double a = time * 0.23 + k * 1.2566;
                double ox = Math.cos(a) * radius * 0.95;
                double oz = Math.sin(a) * radius * 0.95;
                double oy = Math.sin(time * 0.31 + k * 2.1) * radius * 0.35;
                FireMesh.puff(sink, x + ox, y + oy, z + oz, radius * 0.36, ox * 0.15, 0.12, oz * 0.15, 0.9, 0.9, 0.55, 0.4, false, 0.85,
                    time, seed * 31 + k);
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /**
     * A beam of its fire from ({@code x, y, z}) along {@code w} (unit) for {@code reach} blocks, {@code width} across (thin for its first
     * {@code flare} blocks), at {@code fade} (1 burning, to 0 gone): a heart of bright orange fire, white-hot down its middle, black
     * flames twisting round it in tongues (the owner: "a beam of bright orange/black flames", "super clean"): the fire always shows between
     * them.
     */
    public static void beam(FireMesh.Sink sink, double x, double y, double z, double[] w, double reach, double width, double fade, double age,
                            int seed, double flare) {
        try {
            // two ways square to the beam, for the tongues twisting round it
            double ux = -w[2];
            double uy = 0.0;
            double uz = w[0];
            double ul = Math.sqrt(ux * ux + uz * uz);
            if (ul < 1.0E-4) {
                ux = 1.0;
                uz = 0.0;
                ul = 1.0;
            }
            ux /= ul;
            uz /= ul;
            double vx = w[1] * uz - w[2] * uy;
            double vy = w[2] * ux - w[0] * uz;
            double vz = w[0] * uy - w[1] * ux;
            // the white-hot core down its middle
            double core = Math.min(reach, 1.5);
            FireMesh.tube(sink, x, y, z, x + w[0] * core, y + w[1] * core, z + w[2] * core, width * 0.12, width * 0.3, 0xFFFDF2, 0xFFF29A,
                0xFFD52B, 0.3, fade, 0.1, age, seed);
            if (reach > core) {
                FireMesh.tube(sink, x + w[0] * core, y + w[1] * core, z + w[2] * core, x + w[0] * reach, y + w[1] * reach, z + w[2] * reach,
                    width * 0.3, width * 0.32, 0xFFFDF2, 0xFFF29A, 0xFFD52B, 0.3, fade, 0.1, age, seed + 1);
            }
            double step = Math.max(0.3, width * 0.45);
            int n = (int) Math.min(220, reach / step);
            for (int i = 0; i <= n; i++) {
                double d = i * step;
                double taper = Math.min(1.0, 0.35 + d / flare);
                double px = x + w[0] * d;
                double py = y + w[1] * d;
                double pz = z + w[2] * d;
                // the orange fire round it, see-through enough that the white-hot core shows in it
                double heat = 0.44 + 0.22 * FireMesh.rand(seed * 613 + i, 4);
                FireMesh.palette(FireMesh.BAND);
                FireMesh.puff(sink, px, py, pz, width * 0.66 * taper, w[0] * 0.8, w[1] * 0.8, w[2] * 0.8, 0.9, 0.55, heat, 0.25, false,
                    0.7 * fade, age + i * 0.29, seed * 613 + i);
                // a black tongue twisting round it, from side to side, turning as the beam pours
                FireMesh.palette(YOKAI_BLACK);
                double a = age * 0.45 + i * 2.3;
                double off = width * 0.64 * taper;
                double ox = (ux * Math.cos(a) + vx * Math.sin(a)) * off;
                double oy = (uy * Math.cos(a) + vy * Math.sin(a)) * off;
                double oz = (uz * Math.cos(a) + vz * Math.sin(a)) * off;
                FireMesh.puff(sink, px + ox, py + oy, pz + oz, width * 0.34 * taper, w[0] * 0.7 + ox * 0.2, w[1] * 0.7 + oy * 0.2 + 0.03,
                    w[2] * 0.7 + oz * 0.2, 1.2, 0.9, 0.5, 0.35, false, 0.9 * fade, age + i * 0.37, seed * 977 + i);
            }
            // its light
            for (int k = 0; k < 4; k++) {
                double d = reach * (k + 0.5) / 4.0;
                com.hexhaki.piece.dragon.UoFire.glow(sink, x + w[0] * d, y + w[1] * d, z + w[2] * d, width * 2.2, 0xFFB84A, 0.22 * fade);
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /**
     * A flat ring ({@code x, y, z} its middle, {@code n} its normal), {@code radius} out to its edge and {@code width} wide: {@code inner}
     * on its inside edge (a third of {@code alpha}), {@code outer} at its middle ({@code alpha} three quarters), fading out to nothing at
     * its outside edge.
     */
    public static void ring(FireMesh.Sink sink, double x, double y, double z, double nx, double ny, double nz, double radius, double width,
                            int inner, int outer, double alpha) {
        if (alpha <= 0.01 || radius <= 0.01) {
            return;
        }
        double l = Math.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= l;
        ny /= l;
        nz /= l;
        // two ways across the ring's plane
        double ax = Math.abs(ny) < 0.9 ? 0 : 1;
        double ay = Math.abs(ny) < 0.9 ? 1 : 0;
        double ux = ay * nz;
        double uy = -ax * nz;
        double uz = ax * ny - ay * nx;
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul;
        uy /= ul;
        uz /= ul;
        double vx = ny * uz - nz * uy;
        double vy = nz * ux - nx * uz;
        double vz = nx * uy - ny * ux;
        int a = (int) (Math.min(1.0, alpha) * 255.0);
        int ci = ((a / 3) << 24) | (inner & 0xFFFFFF);
        int cm = ((a * 3 / 4) << 24) | (outer & 0xFFFFFF);
        int co = outer & 0xFFFFFF;
        int segments = (int) Math.max(24, Math.min(96, radius * 10));
        double r0 = Math.max(0.0, radius - width);
        double rm = r0 + (radius - r0) * 0.5;
        double[] xyz = new double[12];
        int[] argb = new int[4];
        for (int i = 0; i < segments; i++) {
            double t0 = i * Math.PI * 2.0 / segments;
            double t1 = (i + 1) * Math.PI * 2.0 / segments;
            double c0 = Math.cos(t0);
            double s0 = Math.sin(t0);
            double c1 = Math.cos(t1);
            double s1 = Math.sin(t1);
            put(xyz, 0, x, y, z, ux, uy, uz, vx, vy, vz, c0 * r0, s0 * r0);
            put(xyz, 1, x, y, z, ux, uy, uz, vx, vy, vz, c0 * rm, s0 * rm);
            put(xyz, 2, x, y, z, ux, uy, uz, vx, vy, vz, c1 * rm, s1 * rm);
            put(xyz, 3, x, y, z, ux, uy, uz, vx, vy, vz, c1 * r0, s1 * r0);
            argb[0] = ci;
            argb[1] = cm;
            argb[2] = cm;
            argb[3] = ci;
            sink.quad(xyz, argb);
            put(xyz, 0, x, y, z, ux, uy, uz, vx, vy, vz, c0 * rm, s0 * rm);
            put(xyz, 1, x, y, z, ux, uy, uz, vx, vy, vz, c0 * radius, s0 * radius);
            put(xyz, 2, x, y, z, ux, uy, uz, vx, vy, vz, c1 * radius, s1 * radius);
            put(xyz, 3, x, y, z, ux, uy, uz, vx, vy, vz, c1 * rm, s1 * rm);
            argb[0] = cm;
            argb[1] = co;
            argb[2] = co;
            argb[3] = cm;
            sink.quad(xyz, argb);
        }
    }

    static void put(double[] xyz, int k, double x, double y, double z, double ux, double uy, double uz, double vx, double vy, double vz,
                    double a, double b) {
        xyz[k * 3] = x + ux * a + vx * b;
        xyz[k * 3 + 1] = y + uy * a + vy * b;
        xyz[k * 3 + 2] = z + uz * a + vz * b;
    }

    /**
     * Thy Death's clouds over ({@code x, z}) at the height {@code y}: a slow, rolling roof of black cloud {@code radius} round, at
     * {@code alpha}; {@code grow} (0 to 1) how far they have gathered.
     */
    public static void clouds(FireMesh.Sink sink, double x, double y, double z, double radius, double grow, double alpha, double time, int seed) {
        if (alpha <= 0.01) {
            return;
        }
        try {
            FireMesh.palette(STORM);
            Random r = new Random(seed * 7919L);
            int puffs = 46;
            for (int i = 0; i < puffs; i++) {
                double a = r.nextDouble() * Math.PI * 2.0 + time * 0.004 * (i % 2 == 0 ? 1 : -1);
                double d = radius * Math.sqrt(r.nextDouble());
                double size = 5.0 + r.nextDouble() * 4.0;
                FireMesh.puff(sink, x + Math.cos(a) * d, y + (r.nextDouble() - 0.5) * 3.0, z + Math.sin(a) * d, size * (0.6 + 0.4 * grow), 0.02,
                    0.0, 0.01, 0.2, 0.5, 0.35 + 0.3 * r.nextDouble(), 0.3, false, alpha, time * 0.25 + i * 3.1, seed * 131 + i);
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /**
     * Thy Death's bolt bursting where it strikes the ground ({@code x, y, z}): a ball of its Haki's light {@code radius} round, swelling
     * in its first ticks, a ring of it running out over the ground; {@code k} 0 the strike to 1 gone.
     */
    public static void burst(FireMesh.Sink sink, double x, double y, double z, double radius, double k, double time, int seed) {
        if (k >= 1.0) {
            return;
        }
        double swell = radius * Math.min(1.0, 0.35 + k * 4.0);
        try {
            FireMesh.palette(HAKI);
            FireMesh.puff(sink, x, y + swell * 0.6, z, swell * 0.55, 0.0, 0.05, 0.0, 0.0, 0.4, 0.95 - 0.3 * k, 0.1, false, 1.0 - k, time, seed + 3);
            FireMesh.puff(sink, x, y + swell * 0.6, z, swell, 0.0, 0.05, 0.0, 0.0, 0.7, 0.42 - 0.2 * k, 0.15, true, 0.6 * (1.0 - k * k), time, seed);
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
        ring(sink, x, y + 0.1, z, 0, 1, 0, radius * (1.0 + 1.2 * k), 0.3, INDIGO, GREEN, (1.0 - k) * 0.6);
    }

    /** How thick Thy Death's bolts are where they leave the clouds (blocks): a storm's, not a spark's. */
    public static final float SKY_WIDTH = 0.34F;

    /**
     * One bolt of Thy Death ({@code dx, dy, dz}: blocks from the cloud down to where it strikes): the one Haki lightning's stroke
     * (haki/ConquerorStorm.strike, its rim tinted to its owner's Haki when it is let go), thick, racing down in 3 ticks, with two or three
     * forks breaking off it on the way down.
     */
    public static java.util.List<com.hexhaki.piece.haki.Bolt> skyStroke(double dx, double dy, double dz, long seed) {
        Random r = new Random(seed);
        java.util.List<com.hexhaki.piece.haki.Bolt> out = new java.util.ArrayList<>();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        out.addAll(com.hexhaki.piece.haki.ConquerorStorm.strike(new double[] {0, 0, 0}, new double[] {dx, dy, dz}, SKY_WIDTH, 0.25F,
            com.hexhaki.piece.haki.ConquerorSparks.RED, 0.0F, 12, 3.0F, r.nextLong()));
        int forks = 2 + r.nextInt(2);
        for (int i = 0; i < forks; i++) {
            double t = 0.15 + 0.6 * r.nextDouble();
            double[] mid = {dx * t, dy * t, dz * t};
            double l = length * (0.12 + 0.15 * r.nextDouble());
            double a = r.nextDouble() * Math.PI * 2.0;
            double[] end = {mid[0] + Math.cos(a) * l * 0.7, mid[1] - l * 0.7, mid[2] + Math.sin(a) * l * 0.7};
            out.addAll(com.hexhaki.piece.haki.ConquerorStorm.strike(mid, end, SKY_WIDTH * 0.45F, 0.2F, com.hexhaki.piece.haki.ConquerorSparks.RED,
                (float) (3.0 * t), 10, 1.5F, r.nextLong()));
        }
        return out;
    }
}
