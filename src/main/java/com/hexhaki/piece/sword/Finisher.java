package com.hexhaki.piece.sword;

/**
 * 2.2.1: the finisher's rules (pure, tested), taken from HexGhoul 4.2.9's Steel Blade finisher (the owner: "the slice in half should 100% be
 * taken from Hex Ghoul ... the cut and everything about it. Just make sure instead of horizontal, it's vertical"). How long the line takes
 * ({@link #STUN}, {@link #round}), the line round a body not built like a person ({@link #line}), and what a cut shows inside, wherever it
 * passes through a part ({@link #inside}: a person's head, trunk and limbs, meat, or dry bone). The body's pieces and the line round a person
 * are {@link Carcass}'s; their fall is {@link Ragdoll}'s.
 */
public final class Finisher {
    /** How long the line takes to go round (ticks): the two seconds the body stands stunned. */
    public static final int STUN = 40;

    private Finisher() {
    }

    /** How far round the line has gone {@code age} ticks into the stun (0..1): it creeps in, runs steady, and meets itself at {@link #STUN}. */
    public static double round(double age) {
        double t = clamp(age / STUN);
        return t * t * (3.0 - 2.0 * t) * 0.35 + t * 0.65;
    }

    /** A quad sink: four corners, a facing, a colour each (ARGB). */
    public interface Sink {
        void quad(double[] a, double[] b, double[] c, double[] d, double[] n, int ca, int cb, int cc, int cd);
    }

    /**
     * The point of a square-cornered band {@code u} (0..1) of the way round, walked at an even pace from its front middle toward its top, over
     * its back and under its bottom, home: {up, front}, {@code halfHeight} by {@code halfDepth} from its middle.
     */
    public static double[] box(double u, double halfHeight, double halfDepth) {
        double h = halfHeight;
        double d = halfDepth;
        double[] legs = {h, 2.0 * d, 2.0 * h, 2.0 * d, h};
        double total = 4.0 * (h + d);
        double left = (u - Math.floor(u)) * total;
        for (int k = 0; k < legs.length; k++) {
            if (left <= legs[k] || k == legs.length - 1) {
                double t = legs[k] < 1.0E-12 ? 0.0 : Math.min(1.0, left / legs[k]);
                return switch (k) {
                    case 0 -> new double[] {h * t, d};
                    case 1 -> new double[] {h, d - 2.0 * d * t};
                    case 2 -> new double[] {h - 2.0 * h * t, -d};
                    case 3 -> new double[] {-h, -d + 2.0 * d * t};
                    default -> new double[] {-h + h * t, d};
                };
            }
            left -= legs[k];
        }
        return new double[] {0.0, d};
    }

    /**
     * The finisher's line round a body that isn't built like a person (the same thin glowing line, upright): round its box {@code halfDepth}
     * deep and {@code height} tall (blocks; y up from its feet, z its front), in its middle, drawn in from the middle of its front both ways at
     * once, up over its top and down under its belly, {@code amount} (0..1) of the way to the middle of its back; {@code thick} its half
     * thickness (blocks). Lit from straight above (full bright).
     */
    public static void line(Sink sink, double amount, double halfDepth, double height, double thick, int argb) {
        if (amount <= 0.0) {
            return;
        }
        int steps = 96;
        double half = height * 0.5;
        double[] up = {0.0, 1.0, 0.0};
        for (int i = 0; i < steps; i++) {
            double[] a = box(i / (double)steps, half, halfDepth);
            double[] b = box((i + 1) / (double)steps, half, halfDepth);
            double f = Math.abs(Math.atan2((a[0] + b[0]) * 0.5, (a[1] + b[1]) * 0.5)) / Math.PI;
            if (f > amount) {
                continue;
            }
            double w = thick * Math.min(1.0, (amount - f) / 0.05 + (amount >= 1.0 ? 1.0 : 0.0));
            sink.quad(new double[] {-w, half + a[0], a[1]}, new double[] {-w, half + b[0], b[1]}, new double[] {w, half + b[0], b[1]},
                new double[] {w, half + a[0], a[1]}, up, argb, argb, argb, argb);
        }
    }

    /** What a cut shows inside: a person (their head, trunk and limbs as they are), meat (every other mob), or dry bone (what doesn't bleed). */
    public enum Inside {
        BODY, MEAT, DRY
    }

    /** The colours of what is inside (HexGhoul's {@code WoundShape}'s and the meat's, and a head's). */
    static final int SKIN = 0xC9897C;
    static final int FAT = 0xE4C47C;
    static final int MUSCLE = 0x8E1822;
    static final int MUSCLE_LIGHT = 0xD9525A;
    static final int MUSCLE_DARK = 0x44060C;
    static final int BONE = 0xEADFC6;
    static final int BONE_GREY = 0xCBD3D6;
    static final int SPONGE = 0xE8C9A8;
    static final int DISC = 0xB9C8CC;
    static final int CANAL = 0x3A0A10;
    static final int CORD = 0xE6CDB5;
    static final int GUT = 0xD27884;
    static final int GUT_DARK = 0x8A3646;
    static final int FLESH = 0xB2222E;
    static final int FLESH_DEEP = 0x4A0810;
    static final int LUNG = 0xC9707A;
    static final int LIVER = 0x5A1418;
    static final int BRAIN = 0xD8A8A8;
    static final int BRAIN_FOLD = 0x9C5A64;
    static final int TOOTH = 0xF2EEE0;
    static final int TONGUE = 0xC0505E;

    /** One cell of a cut: its colour and how far it stands out of the cut (pixels of the body). */
    public record Cell(int colour, double height) {
    }

    /**
     * What a cut shows where it passes through {@code part} (head, torso, an arm or a leg) at the part's own point {@code at} (pixels, as its
     * cube has them: x its left, y down from its top, z toward its back), the cube {@code size} pixels from {@code from}: skin at its rim,
     * then (a person) fat or muscle, the skull round the brain, the spine down the back of the trunk (vertebrae and discs, the canal with the
     * cord behind them, their knobs behind that), the breastbone, the heart and lungs above the midriff and the guts' coils below it; a
     * limb's bone in its middle; meat red and uneven; dry bone pale. {@code grain} (0..1) the cell's own unevenness.
     */
    public static Cell inside(Inside kind, String part, double[] at, double[] from, double[] size, double grain) {
        double[] n = new double[3];
        double[] edge = new double[3];
        for (int k = 0; k < 3; k++) {
            double half = size[k] * 0.5;
            double off = at[k] - (from[k] + half);
            n[k] = half < 1.0E-9 ? 0.0 : off / half;
            edge[k] = half - Math.abs(off);
        }
        boolean head = part.equals("head");
        // How deep under the skin (pixels): a head is skin all round; a trunk or a limb round its sides (its ends are joined to the rest).
        double depth = head ? Math.min(edge[0], Math.min(edge[1], edge[2])) : Math.min(edge[0], edge[2]);
        double down = at[1] - from[1];
        double front = -n[2]; // +1 at its front
        if (kind == Inside.MEAT) {
            double lump = noise((int)Math.floor(at[0]), (int)Math.floor(at[1]) * 31 + (int)Math.floor(at[2]), 17) * 0.6 + grain * 0.4;
            int colour = depth < 0.34 ? mixInt(FLESH_DEEP, MUSCLE, grain)
                : lump > 0.7 ? mixInt(MUSCLE_LIGHT, FLESH, (lump - 0.7) / 0.3)
                : lump > 0.35 ? mixInt(FLESH, MUSCLE, (0.7 - lump) / 0.35) : mixInt(MUSCLE, FLESH_DEEP, (0.35 - lump) / 0.35);
            return new Cell(colour, depth < 0.34 ? 0.05 : 0.08 + 0.42 * lump * lump);
        }
        if (kind == Inside.DRY) {
            boolean pore = grain > 0.78;
            return new Cell(depth < 0.34 ? mixInt(BONE, BONE_GREY, grain) : pore ? mixInt(0x8C8478, BONE_GREY, grain) : mixInt(BONE, 0xD8CDB0, grain),
                depth < 0.34 ? 0.06 : pore ? 0.1 : 0.22 + 0.18 * grain);
        }
        if (depth < 0.34) {
            return new Cell(mixInt(SKIN, MUSCLE_LIGHT, grain * 0.25), 0.04);
        }
        if (head) {
            return head(down, front, depth, grain, at);
        }
        if (!part.equals("torso")) {
            // A limb: muscle round the bone down its middle (a hard white ring, the marrow red in it), whichever way the cut runs.
            double bone = n[0] * n[0] / 0.1444 + n[2] * n[2] / 0.1444;
            if (bone < 0.45) {
                return new Cell(mixInt(0xA8263A, 0xC45A4A, grain), 0.4);
            }
            if (bone < 1.0) {
                return new Cell(mixInt(BONE, BONE_GREY, grain * 0.3), 0.48);
            }
            return new Cell(mixInt(MUSCLE, MUSCLE_LIGHT, grain * 0.6), 0.14 + 0.08 * grain);
        }
        double back = from[2] + size[2] - at[2];
        double ahead = at[2] - from[2];
        return trunk(n[0], down * 12.0 / Math.max(1.0E-6, size[1]), back * 4.0 / Math.max(1.0E-6, size[2]), ahead * 4.0 / Math.max(1.0E-6, size[2]),
            edge[0], grain);
    }

    /** A head's inside: the skull under the skin, the brain's folds in it (the cerebellum's finer ones low at its back, the stem below), the mouth. */
    static Cell head(double down, double front, double depth, double grain, double[] at) {
        if (depth < 1.0) {
            return new Cell(mixInt(BONE, BONE_GREY, grain * 0.3), 0.48);
        }
        if (depth < 1.34) {
            return new Cell(mixInt(0x6E0812, MUSCLE_DARK, grain), 0.12);
        }
        // the face: the nose's hollow, the teeth, the tongue, the jaw
        if (front > 0.2 && down > 4.3) {
            if (down < 5.6) {
                return front > 0.35 ? new Cell(CANAL, 0.05) : new Cell(mixInt(MUSCLE, MUSCLE_DARK, grain), 0.16);
            }
            if (down < 6.2 && front > 0.5) {
                return new Cell(mixInt(TOOTH, BONE, grain * 0.4), 0.42);
            }
            if (down < 6.9) {
                return new Cell(mixInt(TONGUE, MUSCLE_LIGHT, grain * 0.5), 0.3);
            }
            return new Cell(mixInt(BONE, BONE_GREY, grain * 0.3), 0.45);
        }
        // the stem, down the middle to the neck
        if (down > 5.2 && Math.abs(front + 0.15) < 0.22) {
            return new Cell(mixInt(0xE0C0B0, SPONGE, grain * 0.5), 0.22);
        }
        // the cerebellum, low at the back: finer folds
        if (down > 4.6 && front < -0.1) {
            boolean fold = Math.sin(down * 6.2 + front * 3.0) > 0.35;
            return new Cell(fold ? mixInt(0x9A5A62, 0x7A3A44, grain) : mixInt(0xC89098, BRAIN, grain * 0.4), fold ? 0.14 : 0.24);
        }
        if (down > 4.6) {
            return new Cell(mixInt(MUSCLE, MUSCLE_DARK, grain * 0.6), 0.16);
        }
        // the brain: its folds winding over it
        double wind = Math.sin(down * 2.4 + front * 5.0 + Math.sin(at[0] * 0.9 + down * 1.3) * 1.6);
        boolean fold = wind > 0.55;
        return new Cell(fold ? mixInt(BRAIN_FOLD, 0x7E3E48, grain) : mixInt(BRAIN, 0xE6BDBA, grain * 0.6), fold ? 0.14 : 0.26 + 0.08 * grain);
    }

    /**
     * A trunk's inside, {@code y} pixels down it (of 12), {@code back} and {@code ahead} pixels from its back and its front (of 4),
     * {@code side} from its nearer side: fat under the front's skin and muscle under the back's; the spine down the back (the knobs, the
     * canal with the cord, the vertebrae and their discs); the breastbone; the ribs at its sides; the heart and lungs above the midriff,
     * the liver and the guts' coils below it.
     */
    static Cell trunk(double nx, double y, double back, double ahead, double side, double grain) {
        if (ahead < 0.67 && ahead < back) {
            return new Cell(mixInt(FAT, 0xC49A55, grain * 0.6), 0.08);
        }
        if (back < 0.67) {
            return new Cell(mixInt(MUSCLE_DARK, MUSCLE, grain), 0.1);
        }
        double ring = y - Math.floor(y / 1.5) * 1.5;
        double ax = Math.abs(nx);
        if (back < 1.0) {
            // the knobs of the spine
            return ax < 0.14 && ring > 0.3 && ring < 1.1 ? new Cell(mixInt(BONE, 0xD8C8A8, grain * 0.4), 0.38)
                : new Cell(mixInt(MUSCLE, MUSCLE_DARK, grain), 0.16);
        }
        if (back < 1.4 && ax < 0.2) {
            // the canal, the cord in it
            return ax < 0.1 && back > 1.07 && back < 1.33 ? new Cell(mixInt(CORD, SPONGE, grain * 0.4), 0.16) : new Cell(CANAL, 0.05);
        }
        if (back < 2.4 && ax < 0.32) {
            // the vertebrae, stacked down the back with discs between
            if (ring < 0.3) {
                return new Cell(mixInt(DISC, BONE_GREY, grain * 0.4), 0.3);
            }
            boolean rim = back < 1.55 || back > 2.25 || ax > 0.24 || ring < 0.45 || ring > 1.35;
            return new Cell(rim ? mixInt(BONE, BONE_GREY, grain * 0.3) : mixInt(SPONGE, 0xD49A86, grain * 0.5), rim ? 0.62 : 0.52);
        }
        if (back < 2.4) {
            return new Cell(mixInt(MUSCLE, MUSCLE_DARK, grain * 0.7), 0.16 + 0.06 * grain);
        }
        // the breastbone, under the front's fat
        if (ahead < 1.0 && y > 0.3 && y < 6.6 && ax < 0.24) {
            return new Cell(mixInt(BONE, 0xD8C8A8, grain * 0.4), 0.45);
        }
        // the ribs, at the chest's sides
        if (y < 7.2 && ax > 0.42 && side < 1.7 && (y - Math.floor(y / 1.25) * 1.25) < 0.45) {
            return new Cell(mixInt(BONE, BONE_GREY, grain * 0.3), 0.4);
        }
        if (y < 6.5) {
            // the heart, a little to the left of the middle; the lungs round it
            double heart = Math.pow((nx - 0.12) / 0.36, 2) + Math.pow((y - 4.2) / 1.7, 2) + Math.pow((ahead - 1.35) / 0.6, 2);
            if (heart < 1.0) {
                return heart < 0.3 ? new Cell(mixInt(MUSCLE_DARK, 0x2A0408, grain), 0.18) : new Cell(mixInt(MUSCLE, MUSCLE_LIGHT, grain * 0.4), 0.36);
            }
            boolean spot = grain > 0.72;
            return new Cell(spot ? mixInt(0x9A4A56, LUNG, grain * 0.3) : mixInt(LUNG, 0xE0A0A8, grain * 0.5), spot ? 0.14 : 0.2 + 0.12 * grain);
        }
        if (y < 7.0) {
            return new Cell(mixInt(MUSCLE, MUSCLE_LIGHT, grain * 0.5), 0.18); // the midriff
        }
        if (y < 8.6 && nx < -0.12) {
            return new Cell(mixInt(LIVER, 0x7A2028, grain * 0.6), 0.26);
        }
        // the guts: tubes coiled side by side, each a ring of gut round a dark middle, bulging out of the cut
        double ux = y * 0.75 + (Math.floor(ahead * 1.5) % 2) * 0.5;
        double fx = ux - Math.floor(ux) - 0.5;
        double fz = ahead * 1.5 + nx * 1.3;
        fz = fz - Math.floor(fz) - 0.5;
        double coil = Math.sqrt(fx * fx + fz * fz * 1.6);
        if (coil < 0.17) {
            return new Cell(GUT_DARK, 0.22);
        }
        if (coil < 0.5) {
            return new Cell(mixInt(GUT, 0xF0A0AA, (0.5 - coil) * 1.2 * (0.6 + 0.4 * grain)), 0.3 + 0.35 * (0.5 - coil) / 0.33);
        }
        return new Cell(mixInt(0x6E0812, GUT_DARK, grain), 0.12);
    }

    /** A steady hash (0..1): the same cut every frame. */
    public static double noise(int c, int r, int seed) {
        long h = c * 73856093L ^ r * 19349663L ^ seed * 83492791L;
        h = (h ^ (h >>> 13)) * 0x5bd1e995L;
        h ^= h >>> 15;
        return (h & 0xFFFF) / 65535.0;
    }

    /** RGB {@code from} mixed {@code amount} (0..1) of the way to {@code to}. */
    public static int mixInt(int from, int to, double amount) {
        double t = clamp(amount);
        int r = (int)Math.round(((from >> 16) & 255) * (1 - t) + ((to >> 16) & 255) * t);
        int g = (int)Math.round(((from >> 8) & 255) * (1 - t) + ((to >> 8) & 255) * t);
        int b = (int)Math.round((from & 255) * (1 - t) + (to & 255) * t);
        return r << 16 | g << 8 | b;
    }

    /** RGB {@code rgb} with alpha {@code alpha} (0..1). */
    public static int argb(int rgb, double alpha) {
        return (int)Math.round(clamp(alpha) * 255.0) << 24 | rgb & 0xFFFFFF;
    }

    static double clamp(double x) {
        return x < 0.0 ? 0.0 : x > 1.0 ? 1.0 : x;
    }
}
