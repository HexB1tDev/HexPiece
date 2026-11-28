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

    // ------------------------------------------------------------------------------------------------------ 3.9.9: Hellish Teleport

    /**
     * The gate's fire (the owner's reference, Haki_and_Portalfire.gif): cyan flames, white-cyan in their hearts, their edges hot pink:
     * FireMesh pulls a puff's edge toward its RIM band, so pink sits just under the cyan.
     */
    public static final int[] GATE_FIRE = {0x2A0630, 0x5A0C55, 0x9A1478, 0xD8249A, 0xFF4FB8, 0xFF6FC8, 0x3FE6F0, 0xC8FFFF};
    /**
     * 3.9.9 (the owner: "the summoning circle lacks runes. the color also isn't it, the original is a very dark shade of purple/blue";
     * input/itsumade/pictures/pentagram_frame.png): the circle's own colours: a very dark purple-blue disc, a darker band round it full of
     * pale lavender runes, the star's points a deep blue with a pink mark in each, every line thin and pale cyan-white.
     */
    public static final int DISC = 0x17143F;
    public static final int RING_BAND = 0x0C0A27;
    public static final int STAR_FILL = 0x1C2A74;
    public static final int LINE_CORE = 0xF2FDFF;
    public static final int LINE = 0xA9E6FF;
    public static final int LINE_GLOW = 0x4A3FD0;
    public static final int RUNE = 0xD9C8FF;
    public static final int RUNE_GLOW = 0x6A3FD8;
    public static final int MARK = 0xFF4FC8;
    public static final int MARK_GLOW = 0x8A1A78;

    /** The ground's height under a point ({@code x, z} camera-relative, as every point here), for the pentagram to lie on. */
    public interface Ground {
        double at(double x, double z);
    }

    /** How long the pentagram takes to form (ticks): its circles traced, then the star drawn point to point, then it burns. */
    public static final int GATE_FORM = 200;
    public static final double GATE_RADIUS = 3.0;
    /** How many runes run round its band. */
    static final int RUNES = 40;

    /**
     * The pentagram ({@code GATE_RADIUS} round its middle {@code x, z}; six blocks across) at {@code age} ticks of its forming, laid on
     * the ground as {@code ground} has it (every piece of it on the top of the block under it, its lines climbing the face of a step), at
     * {@code alpha}: its dark disc welling up (to 100), the outer circle traced round (to 60), the inner circle (40 to 90), the runes
     * written one after another round the band (50 to 130), the star's five lines drawn point to point (80 to 160) and its marks (150 to
     * 175), then all of it burning brighter to {@link #GATE_FORM}; cyan fire with pink edges licking up round the outer circle as it forms.
     */
    public static void pentagram(FireMesh.Sink sink, Ground ground, double x, double z, double age, double alpha, int seed) {
        if (alpha <= 0.01) {
            return;
        }
        double r = GATE_RADIUS;
        double ri = r * 0.84;
        double burn = clamp01((age - 150.0) / 50.0);
        double width = 0.035 + 0.015 * burn;
        double a = alpha * (0.8 + 0.2 * burn);
        disc(sink, ground, x, z, r, ri, alpha * clamp01(age / 100.0), clamp01((age - 120.0) / 50.0));
        // the circles: the outer edge and both edges of the band
        arcOnGround(sink, ground, x, z, r, 0.0, clamp01(age / 60.0), width * 1.3, a);
        arcOnGround(sink, ground, x, z, ri, Math.PI, clamp01((age - 40.0) / 50.0), width, a);
        // the runes round the band, written one after another
        double rb = (r + ri) * 0.5;
        for (int j = 0; j < RUNES; j++) {
            double shown = clamp01((age - 50.0 - j * 80.0 / RUNES) / 6.0);
            if (shown <= 0.0) {
                break;
            }
            double th = j * Math.PI * 2.0 / RUNES + 0.05;
            rune(sink, ground, x, z, th, rb, (r - ri) * 0.68, seed * 31 + j, a * shown);
        }
        // the star: five points on the inner circle, each joined to the one two along, drawn one after another
        for (int k = 0; k < 5; k++) {
            double t = clamp01((age - 80.0 - k * 16.0) / 16.0);
            if (t <= 0.0) {
                continue;
            }
            double a0 = Math.PI / 2.0 + k * 2.0 * Math.PI * 2.0 / 5.0;
            double a1 = Math.PI / 2.0 + (k + 1) * 2.0 * Math.PI * 2.0 / 5.0;
            double x0 = x + Math.cos(a0) * ri;
            double z0 = z + Math.sin(a0) * ri;
            double x1 = x + Math.cos(a1) * ri;
            double z1 = z + Math.sin(a1) * ri;
            lineOnGround(sink, ground, x0, z0, x0 + (x1 - x0) * t, z0 + (z1 - z0) * t, width, a);
        }
        // a pink mark in each point of the star, pointing in to its middle
        for (int k = 0; k < 5; k++) {
            double shown = clamp01((age - 150.0 - k * 4.0) / 6.0);
            if (shown > 0.0) {
                mark(sink, ground, x, z, Math.PI / 2.0 + k * Math.PI * 2.0 / 5.0, ri, a * shown);
            }
        }
        // the fire round it, as far round as the circle has gone, taller once it burns: tongues of cyan with pink edges licking up
        double round = clamp01(age / 60.0);
        int flames = 56;
        FireMesh.palette(GATE_FIRE);
        for (int k = 0; k < flames; k++) {
            double u = (k + 0.5) / flames;
            if (u > round) {
                break;
            }
            double ang = u * Math.PI * 2.0;
            double rr = r + 0.22 + 0.12 * Math.sin(k * 1.9);
            double fx = x + Math.cos(ang) * rr;
            double fz = z + Math.sin(ang) * rr;
            double fy = ground.at(fx, fz);
            double flick = 0.7 + 0.3 * Math.sin(age * 0.9 + k * 2.3);
            double size = (0.12 + 0.09 * burn) * flick * Math.min(1.0, (round - u) * 12.0);
            // moving down so FireMesh draws its tongue out behind it, upward: a flame's tip
            FireMesh.puff(sink, fx, fy + size * 0.8, fz, size, 0.0, -1.0, 0.0, 1.1 + 0.5 * burn, 0.55, 0.86, 0.7, false, alpha,
                age + k * 1.7, seed * 53 + k);
        }
        FireMesh.palette(FireMesh.BAND);
    }

    /**
     * The circle's dark disc on the ground: cut into small pieces round its middle (rings of 0.375, pieces of about 0.4 round), each flat
     * on the block under it, coloured by where it lies (the band, a point of the star, the rest); the star's points fill in at
     * {@code star}.
     */
    static void disc(FireMesh.Sink sink, Ground ground, double x, double z, double r, double ri, double alpha, double star) {
        if (alpha <= 0.01) {
            return;
        }
        int rings = 8;
        double[] xyz = new double[12];
        int[] argb = new int[4];
        for (int i = 0; i < rings; i++) {
            double r0 = r * i / rings;
            double r1 = r * (i + 1) / rings;
            int segs = (int) Math.max(10, Math.ceil(r1 * Math.PI * 2.0 / 0.4));
            for (int j = 0; j < segs; j++) {
                double a0 = j * Math.PI * 2.0 / segs;
                double a1 = (j + 1) * Math.PI * 2.0 / segs;
                double rm = (r0 + r1) * 0.5;
                double am = (a0 + a1) * 0.5;
                double cx = Math.cos(am) * rm;
                double cz = Math.sin(am) * rm;
                double h = ground.at(x + cx, z + cz) + 0.012;
                int c;
                if (rm > ri) {
                    c = argb(RING_BAND, alpha);
                } else if (star > 0.0 && inStar(cx, cz, ri)) {
                    c = argb(mix(DISC, STAR_FILL, star), alpha);
                } else {
                    c = argb(DISC, alpha);
                }
                // each piece a little over its neighbours (the rings' pieces do not meet corner to corner: no hairline between them)
                double o0 = Math.max(0.0, r0 - 0.03);
                double o1 = r1 + 0.03;
                double b0 = a0 - 0.012;
                double b1 = a1 + 0.012;
                xyz[0] = x + Math.cos(b0) * o0;
                xyz[1] = h;
                xyz[2] = z + Math.sin(b0) * o0;
                xyz[3] = x + Math.cos(b0) * o1;
                xyz[4] = h;
                xyz[5] = z + Math.sin(b0) * o1;
                xyz[6] = x + Math.cos(b1) * o1;
                xyz[7] = h;
                xyz[8] = z + Math.sin(b1) * o1;
                xyz[9] = x + Math.cos(b1) * o0;
                xyz[10] = h;
                xyz[11] = z + Math.sin(b1) * o0;
                argb[0] = c;
                argb[1] = c;
                argb[2] = c;
                argb[3] = c;
                sink.quad(xyz, argb);
            }
        }
    }

    /** Whether ({@code px, pz}) lies in the star drawn on the circle {@code ri} round (its ten corners, the points' and the inner ones). */
    static boolean inStar(double px, double pz, double ri) {
        double inner = ri * Math.cos(Math.PI * 2.0 / 5.0) / Math.cos(Math.PI / 5.0);
        boolean in = false;
        for (int i = 0, j = 9; i < 10; j = i++) {
            double ai = Math.PI / 2.0 + i * Math.PI / 5.0;
            double aj = Math.PI / 2.0 + j * Math.PI / 5.0;
            double rI = i % 2 == 0 ? ri : inner;
            double rJ = j % 2 == 0 ? ri : inner;
            double xi = Math.cos(ai) * rI;
            double zi = Math.sin(ai) * rI;
            double xj = Math.cos(aj) * rJ;
            double zj = Math.sin(aj) * rJ;
            if ((zi > pz) != (zj > pz) && px < (xj - xi) * (pz - zi) / (zj - zi) + xi) {
                in = !in;
            }
        }
        return in;
    }

    static int mix(int a, int b, double t) {
        int r = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        int g = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        int bl = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
        return (r << 16) | (g << 8) | bl;
    }

    /**
     * One rune in the band, its foot toward the middle, read round the circle: two to four strokes out of a little alphabet of bars,
     * slants, hooks and forks (seeded, so each rune is its own and always the same), pale lavender over a violet glow.
     */
    static void rune(FireMesh.Sink sink, Ground ground, double x, double z, double th, double rb, double tall, int seed, double alpha) {
        // across (along the circle) and up (out from the middle) the rune
        double tx = -Math.sin(th);
        double tz = Math.cos(th);
        double nx = Math.cos(th);
        double nz = Math.sin(th);
        double cx = x + nx * rb;
        double cz = z + nz * rb;
        double hh = tall * 0.5;
        double hw = tall * 0.32;
        // each stroke: from (u0, v0) to (u1, v1), in halves of the rune's width and height
        double[][] alphabet = {
            {0, -1, 0, 1}, {-1, -1, 1, 1}, {1, -1, -1, 1}, {-1, 1, 1, 1}, {-1, 0, 1, 0}, {-1, -1, 1, -1}, {0, 1, 1, 0.3},
            {-1, 1, 0, 0}, {0, 0, 1, 1}, {-1, -1, -1, 1}, {1, -1, 1, 1}, {0, -0.2, 1, -1}, {-1, 0.4, 0, -0.4}};
        Random r = new Random(seed * 2654435761L);
        int strokes = 2 + r.nextInt(3);
        int first = r.nextInt(3); // most runes stand on a stem
        for (int s = 0; s < strokes; s++) {
            double[] st = alphabet[s == 0 ? (first == 0 ? 0 : first == 1 ? 9 : 10) : r.nextInt(alphabet.length)];
            double x0 = cx + tx * st[0] * hw + nx * st[1] * hh;
            double z0 = cz + tz * st[0] * hw + nz * st[1] * hh;
            double x1 = cx + tx * st[2] * hw + nx * st[3] * hh;
            double z1 = cz + tz * st[2] * hw + nz * st[3] * hh;
            stroke(sink, ground, x0, z0, x1, z1, tall * 0.075, RUNE, RUNE_GLOW, alpha);
        }
    }

    /** A pink mark in the point of the star at {@code th}: a chevron pointing in to the middle, a stroke through it. */
    static void mark(FireMesh.Sink sink, Ground ground, double x, double z, double th, double ri, double alpha) {
        double nx = Math.cos(th);
        double nz = Math.sin(th);
        double tx = -nz;
        double tz = nx;
        double tip = ri * 0.42;
        double back = ri * 0.62;
        double half = ri * 0.08;
        stroke(sink, ground, x + nx * back + tx * half, z + nz * back + tz * half, x + nx * tip, z + nz * tip, 0.04, MARK, MARK_GLOW, alpha);
        stroke(sink, ground, x + nx * back - tx * half, z + nz * back - tz * half, x + nx * tip, z + nz * tip, 0.04, MARK, MARK_GLOW, alpha);
        stroke(sink, ground, x + nx * (back + ri * 0.08), z + nz * (back + ri * 0.08), x + nx * (tip + ri * 0.04), z + nz * (tip + ri * 0.04),
            0.03, MARK, MARK_GLOW, alpha);
    }

    /** A short stroke flat on the ground (the height under its middle): its colour over a soft glow of {@code glow}. */
    static void stroke(FireMesh.Sink sink, Ground ground, double x0, double z0, double x1, double z1, double width, int colour, int glow,
                       double alpha) {
        double dx = x1 - x0;
        double dz = z1 - z0;
        double l = Math.hypot(dx, dz);
        if (l < 1.0E-6 || alpha <= 0.01) {
            return;
        }
        double sx = -dz / l;
        double sz = dx / l;
        // the stroke runs a hair past its ends so strokes meet
        double ex = dx / l * width * 0.5;
        double ez = dz / l * width * 0.5;
        double y = ground.at((x0 + x1) * 0.5, (z0 + z1) * 0.5) + 0.03;
        int g = argb(glow, alpha);
        double gw = width * 0.95;
        sink.quad(new double[] {x0 - ex * 2 + sx * gw, y, z0 - ez * 2 + sz * gw, x1 + ex * 2 + sx * gw, y, z1 + ez * 2 + sz * gw,
            x1 + ex * 2 - sx * gw, y, z1 + ez * 2 - sz * gw, x0 - ex * 2 - sx * gw, y, z0 - ez * 2 - sz * gw}, new int[] {g, g, g, g});
        int c = argb(colour, alpha);
        double w = width * 0.5;
        sink.quad(new double[] {x0 - ex + sx * w, y + 0.004, z0 - ez + sz * w, x1 + ex + sx * w, y + 0.004, z1 + ez + sz * w,
            x1 + ex - sx * w, y + 0.004, z1 + ez - sz * w, x0 - ex - sx * w, y + 0.004, z0 - ez - sz * w}, new int[] {c, c, c, c});
    }

    static double clamp01(double v) {
        return v < 0.0 ? 0.0 : v > 1.0 ? 1.0 : v;
    }

    /** An arc of a circle on the ground from {@code start} (radians) round {@code part} of the way. */
    static void arcOnGround(FireMesh.Sink sink, Ground ground, double x, double z, double radius, double start, double part, double width,
                            double alpha) {
        if (part <= 0.0) {
            return;
        }
        int n = (int) Math.max(8, Math.ceil(radius * 2.0 * Math.PI * part / 0.35));
        double px = x + Math.cos(start) * radius;
        double pz = z + Math.sin(start) * radius;
        for (int i = 1; i <= n; i++) {
            double ang = start + part * Math.PI * 2.0 * i / n;
            double qx = x + Math.cos(ang) * radius;
            double qz = z + Math.sin(ang) * radius;
            segment(sink, ground, px, pz, qx, qz, width, alpha);
            px = qx;
            pz = qz;
        }
    }

    /** A straight line on the ground, cut into short pieces so each lies on its own block. */
    static void lineOnGround(FireMesh.Sink sink, Ground ground, double x0, double z0, double x1, double z1, double width, double alpha) {
        double l = Math.hypot(x1 - x0, z1 - z0);
        int n = (int) Math.max(1, Math.ceil(l / 0.3));
        for (int i = 0; i < n; i++) {
            double t0 = (double) i / n;
            double t1 = (double) (i + 1) / n;
            segment(sink, ground, x0 + (x1 - x0) * t0, z0 + (z1 - z0) * t0, x0 + (x1 - x0) * t1, z0 + (z1 - z0) * t1, width, alpha);
        }
    }

    /**
     * One short piece of a glowing line flat on the ground (a violet glow, the pink line, its pink-white heart), a hair over the block's
     * top; where the ground steps between its ends, a piece up (or down) the step's face joins them.
     */
    static void segment(FireMesh.Sink sink, Ground ground, double x0, double z0, double x1, double z1, double width, double alpha) {
        double dx = x1 - x0;
        double dz = z1 - z0;
        double l = Math.hypot(dx, dz);
        if (l < 1.0E-6) {
            return;
        }
        double sx = -dz / l;
        double sz = dx / l;
        double y0 = ground.at(x0, z0) + 0.03;
        double y1 = ground.at(x1, z1) + 0.03;
        double mx = (x0 + x1) * 0.5;
        double mz = (z0 + z1) * 0.5;
        if (Math.abs(y1 - y0) > 0.3) {
            // a step: each half on its own block, a riser up the face between them
            flat(sink, x0, y0, z0, mx, mz, sx, sz, width, alpha);
            flat(sink, mx, y1, mz, x1, z1, sx, sz, width, alpha);
            riser(sink, mx, mz, y0, y1, sx, sz, width, alpha);
            return;
        }
        segmentAt(sink, x0, y0, z0, x1, y1, z1, sx, sz, width, alpha);
    }

    static void flat(FireMesh.Sink sink, double x0, double y, double z0, double x1, double z1, double sx, double sz, double width,
                     double alpha) {
        segmentAt(sink, x0, y, z0, x1, y, z1, sx, sz, width, alpha);
    }

    /**
     * One piece of line from ({@code x0, y0, z0}) to ({@code x1, y1, z1}), {@code sx, sz} across it: a solid violet halo, the pale line,
     * its white heart, each a hair over the last. Solid, never see-through at its edges: a see-through glow still hides what is drawn
     * after it under it (the disc showed the grass through in teeth round every line).
     */
    static void segmentAt(FireMesh.Sink sink, double x0, double y0, double z0, double x1, double y1, double z1, double sx, double sz,
                          double width, double alpha) {
        double[] widths = {width * 2.3, width, width * 0.4};
        int[] colours = {LINE_GLOW, LINE, LINE_CORE};
        double[] xyz = new double[12];
        int[] argb = new int[4];
        for (int k = 0; k < 3; k++) {
            double w = widths[k];
            double lift = k * 0.004;
            int c = argb(colours[k], alpha);
            xyz[0] = x0 + sx * w;
            xyz[1] = y0 + lift;
            xyz[2] = z0 + sz * w;
            xyz[3] = x1 + sx * w;
            xyz[4] = y1 + lift;
            xyz[5] = z1 + sz * w;
            xyz[6] = x1 - sx * w;
            xyz[7] = y1 + lift;
            xyz[8] = z1 - sz * w;
            xyz[9] = x0 - sx * w;
            xyz[10] = y0 + lift;
            xyz[11] = z0 - sz * w;
            argb[0] = c;
            argb[1] = c;
            argb[2] = c;
            argb[3] = c;
            sink.quad(xyz, argb);
        }
    }

    /** The glow's half strip: {@code inner} along the line, {@code outer} (clear) {@code w} out to the side. */
    static void half(FireMesh.Sink sink, double x0, double y0, double z0, double x1, double y1, double z1, double sx, double sz, double w,
                     int inner, int outer) {
        double[] xyz = {x0, y0, z0, x1, y1, z1, x1 + sx * w, y1, z1 + sz * w, x0 + sx * w, y0, z0 + sz * w};
        sink.quad(xyz, new int[] {inner, inner, outer, outer});
    }

    /** The line up a step's face (vertical, square to the line), from {@code y0} to {@code y1}. */
    static void riser(FireMesh.Sink sink, double x, double z, double y0, double y1, double sx, double sz, double width, double alpha) {
        int g = argb(LINE_GLOW, alpha);
        int c = argb(LINE, alpha);
        double gw = width * 2.3;
        sink.quad(new double[] {x + sx * gw, y0, z + sz * gw, x + sx * gw, y1, z + sz * gw, x - sx * gw, y1, z - sz * gw, x - sx * gw, y0,
            z - sz * gw}, new int[] {g, g, g, g});
        double w = width;
        // the line a hair out from its halo on both faces of the step's face
        sink.quad(new double[] {x + sx * w, y0, z + sz * w, x + sx * w, y1, z + sz * w, x - sx * w, y1, z - sz * w, x - sx * w, y0, z - sz * w},
            new int[] {c, c, c, c});
    }

    static int argb(int rgb, double alpha) {
        return ((int) (Math.max(0.0, Math.min(1.0, alpha)) * 255.0) << 24) | (rgb & 0xFFFFFF);
    }

    /**
     * A pillar of black flames from {@code y0} to {@code y1} over ({@code x, z}): {@code radius} round, rolling up (or down when
     * {@code down}) as {@code age} runs, a violet-pink heart showing through, at {@code alpha}.
     */
    public static void pillar(FireMesh.Sink sink, double x, double y0, double y1, double z, double radius, double age, double alpha,
                              boolean down, int seed) {
        if (alpha <= 0.01 || y1 <= y0) {
            return;
        }
        double h = y1 - y0;
        double step = Math.max(0.6, radius * 0.7);
        int n = (int) Math.min(160, h / step);
        double flow = (age * 0.9) % step;
        try {
            for (int i = 0; i <= n; i++) {
                double d = i * step + (down ? step - flow : flow);
                if (d > h) {
                    continue;
                }
                double y = y0 + d;
                double up = d / h;
                double rr = radius * (0.85 + 0.25 * Math.sin(age * 0.5 + i * 1.7)) * (1.0 + 0.6 * up);
                double ox = Math.sin(age * 0.3 + i * 2.1) * rr * 0.2;
                double oz = Math.cos(age * 0.37 + i * 1.3) * rr * 0.2;
                FireMesh.palette(YOKAI_BLACK);
                FireMesh.puff(sink, x + ox, y, z + oz, rr, ox * 0.2, down ? 1.0 : -1.0, oz * 0.2, 1.8, 1.3, 0.5, 0.35, false, alpha * 0.85,
                    age + i * 0.37, seed * 977 + i);
                if (i % 3 == 0) {
                    com.hexhaki.piece.dragon.UoFire.glow(sink, x, y, z, rr * 2.4, LINE_GLOW, 0.22 * alpha);
                }
                // pink and blue tongues twisting up (or down) round it: the pentagram's fire carried with it
                if (i % 2 == 0) {
                    double ta = age * 0.35 + i * 2.4;
                    double tr = rr * 1.02;
                    FireMesh.palette(GATE_FIRE);
                    FireMesh.puff(sink, x + Math.cos(ta) * tr, y, z + Math.sin(ta) * tr, rr * 0.3, 0.0, down ? 1.0 : -1.0, 0.0, 2.6, 1.0, 0.86,
                        0.7, false, alpha * 0.9, age + i * 0.53, seed * 397 + i);
                }
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }

    /**
     * A body swallowed by black flames ({@code x, y, z} its feet, {@code height} tall), {@code k} 0 to 1: the flames rising over it
     * and then (when {@code toward} is given, camera-relative) drawn off it to that point, the pentagram's middle.
     */
    public static void engulf(FireMesh.Sink sink, double x, double y, double z, double height, double k, double[] toward, double age, int seed) {
        if (k >= 1.0) {
            return;
        }
        double over = clamp01(k * 3.0);
        double gone = clamp01((k - 0.35) / 0.65);
        double cx = x;
        double cz = z;
        double cy = y;
        if (toward != null) {
            cx = x + (toward[0] - x) * gone;
            cy = y + (toward[1] - y) * gone;
            cz = z + (toward[2] - z) * gone;
        }
        try {
            FireMesh.palette(YOKAI_BLACK);
            int n = 5;
            for (int i = 0; i < n; i++) {
                double u = (i + 0.5) / n;
                double hy = cy + height * u * over * (1.0 - gone * 0.8);
                double rr = (0.45 + 0.2 * Math.sin(age * 0.6 + i)) * (1.0 - gone * 0.5);
                FireMesh.puff(sink, cx, hy, cz, rr, 0.0, 0.3, 0.0, 1.2, 0.9, 0.5, 0.35, false, 0.92 * (1.0 - gone * gone), age + i, seed * 71 + i);
            }
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
    }
}
