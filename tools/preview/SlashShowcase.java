import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.BoltMesh;
import com.hexhaki.piece.haki.DrawCrackle;
import com.hexhaki.piece.sword.Carcass;
import com.hexhaki.piece.sword.CutPose;
import com.hexhaki.piece.sword.Finisher;
import com.hexhaki.piece.sword.Ragdoll;
import com.hexhaki.piece.sword.SlashMesh;
import com.hexhaki.piece.sword.SwordRules;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * 2.3.0 (the owner: "first thing I want is a full showcase of the sword beam hitting a player and aftermath finisher"): one continuous
 * shot, off-game, of a Ryou Haki slash flying into a player and the finisher that follows, built from the game's own code wherever the
 * game has it:
 * <ul>
 *   <li>the wave and its crackle exactly as {@code client/sword/SlashRenderer} lays them ({@link SlashMesh#wave}, {@link DrawCrackle} along
 *       {@link SlashMesh#edge}, {@link BoltMesh}), its motes as {@code SlashRenderer.tick} sheds them;</li>
 *   <li>the trench: the cells {@code server/HakiSlashes.queueCells} picks (rows {@code ceil(height)} tall from {@link SwordRules#slashCentre}
 *       down, each {@link SwordRules#slashThicknessAt} wide, sampled every half block), cut the tick the plane reaches them, with break
 *       debris;</li>
 *   <li>the victim (the owner's skin, art/user_skin.png): hit when the plane reaches its box ({@code HakiSlashes.hitBodies}), held by the
 *       finisher ({@code server/Finishers}: its motion stopped, gravity still acting unless {@code hover}), the line drawing round it for
 *       {@link Finisher#STUN} ticks ({@link Carcass#wound}, {@link Carcass#GLOW}), then cut ({@link Carcass#cut}, {@link Carcass#launch}) and
 *       falling apart as the game's ragdoll ({@link Ragdoll}) on the cut blocks, bleeding from each cut and pooling where each half rests
 *       ({@code client/sword/FinisherFx.tick}).</li>
 * </ul>
 * The rest is the preview's: blocks with procedural 16 px textures and vanilla's face shading, distance fog, the original mod's blood as
 * plain drops and discs.
 *
 * <p>javac -cp build/classes -d build/preview tools/preview/SlashShowcase.java
 * && java -cp build/preview:build/classes SlashShowcase out-dir [skin.png] [hover] [fps]
 */
public final class SlashShowcase {
    static int W = 640;
    static int H = 360;
    static final int SS = 2;
    static final double FOV = Math.toRadians(70);
    static BufferedImage skin;
    static boolean slim;

    // --- the slash: a 0.6 charge (lethal on a full-health player: 27 damage), Ryou in the owner's mint ----------------------------------
    static final double CHARGE = 0.6;
    static final double HEIGHT = SwordRules.lerp(SwordRules.SLASH_HEIGHT_MIN, SwordRules.SLASH_HEIGHT_MAX, CHARGE * CHARGE);
    static final double SPEED = SwordRules.lerp(SwordRules.SLASH_SPEED, SwordRules.SLASH_SPEED_MAX, CHARGE);
    static final double RANGE = SwordRules.lerp(SwordRules.SLASH_RANGE_MIN, SwordRules.SLASH_RANGE_MAX, CHARGE * CHARGE);
    static final int COLOR = 0xFF2AE6FF;
    static final long ID = 7L;
    /** The slash sets off from the swordsman's eye, START blocks up the +z axis, flying -z (into the victim's face). */
    static final double START = 70.0;
    static final double EYE = 1.62;
    /** The victim stands at the origin facing +z. */
    static final double HIT_TICK;

    static {
        // the plane reaches the victim's front (its box +0.3, plus half the wave's thickness) this many ticks after the release
        HIT_TICK = (START - 0.3 - SwordRules.slashThickness(HEIGHT) / 2.0) / SPEED;
    }

    /** How far the plane has flown at release tick t (it stops at its range). */
    static double along(double t) {
        return Math.max(0.0, Math.min(RANGE, t * SPEED));
    }

    /** The line's z at release tick t. */
    static double lineZ(double t) {
        return START - along(t);
    }

    // --- the world: grass on dirt, the ground's top at y 0 -----------------------------------------------------------------------------
    static final int X0 = -20;
    static final int X1 = 20;
    static final int Y0 = -7;
    static final int Z0 = -48;
    static final int Z1 = 76;
    static final boolean[][][] SOLID = new boolean[X1 - X0][-Y0][Z1 - Z0];
    /** When each cut cell goes (release ticks), and the cells in the order the plane reached them. */
    static final List<int[]> CUTS = new ArrayList<>();
    static final double[][][] CUT_AT = new double[X1 - X0][-Y0][Z1 - Z0];

    static boolean solid(int x, int y, int z, double t) {
        if (x < X0 || x >= X1 || z < Z0 || z >= Z1) {
            return y < 0;
        }
        if (y >= 0 || y < Y0) {
            return y < Y0;
        }
        int i = x - X0;
        int j = y - Y0;
        int k = z - Z0;
        return SOLID[i][j][k] && !(CUT_AT[i][j][k] <= t);
    }

    /** The cells the plane reaches, as HakiSlashes.queueCells picks them (the slash's start at the swordsman's eye height). */
    static void trench() {
        for (boolean[][] a : SOLID) {
            for (boolean[] b : a) {
                Arrays.fill(b, true);
            }
        }
        for (double[][] a : CUT_AT) {
            for (double[] b : a) {
                Arrays.fill(b, Double.MAX_VALUE);
            }
        }
        int rows = Math.max(1, (int) Math.ceil(HEIGHT));
        double bottom = SwordRules.slashCentre(HEIGHT) - HEIGHT / 2.0;
        double rowHeight = HEIGHT / rows;
        double[] widths = new double[rows];
        int[] acrosses = new int[rows];
        for (int row = 0; row < rows; row++) {
            widths[row] = SwordRules.slashThicknessAt(HEIGHT, (row + 0.5) / rows);
            acrosses[row] = Math.max(1, (int) Math.ceil(widths[row] / 0.5));
        }
        Set<Long> seen = new HashSet<>();
        for (int tick = 1; along(tick - 1) < RANGE; tick++) {
            double a = along(tick - 1);
            double b = along(tick);
            int samples = Math.max(1, (int) Math.ceil((b - a) / 0.5));
            for (int s = 1; s <= samples; s++) {
                double cz = START - (a + (b - a) * s / samples);
                for (int row = 0; row < rows; row++) {
                    double y = EYE + bottom + (row + 0.5) * rowHeight;
                    int across = acrosses[row];
                    for (int w = 0; w < across; w++) {
                        double off = across == 1 ? 0.0 : (w / (double) (across - 1) - 0.5) * widths[row];
                        // the flight is -z: its side (-dir.z, dir.x) is +x
                        int bx = (int) Math.floor(off);
                        int by = (int) Math.floor(y);
                        int bz = (int) Math.floor(cz);
                        long key = ((long) bx & 0xFFFFF) | (((long) by & 0xFFFFF) << 20) | (((long) bz & 0xFFFFF) << 40);
                        if (!seen.add(key) || by >= 0 || by < Y0 || bx < X0 || bx >= X1 || bz < Z0 || bz >= Z1) {
                            continue;
                        }
                        CUT_AT[bx - X0][by - Y0][bz - Z0] = tick;
                        CUTS.add(new int[] {bx, by, bz, tick});
                    }
                }
            }
        }
    }

    /** The first solid block the segment a..b meets (the game: a block clip), the point where it does. */
    static double[] clip(double[] a, double[] b, double t) {
        double best = Double.MAX_VALUE;
        int x0 = (int) Math.floor(Math.min(a[0], b[0])) - 1;
        int x1 = (int) Math.floor(Math.max(a[0], b[0])) + 1;
        int y0 = (int) Math.floor(Math.min(a[1], b[1])) - 1;
        int y1 = (int) Math.floor(Math.max(a[1], b[1])) + 1;
        int z0 = (int) Math.floor(Math.min(a[2], b[2])) - 1;
        int z1 = (int) Math.floor(Math.max(a[2], b[2])) + 1;
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    if (!solid(x, y, z, t)) {
                        continue;
                    }
                    double[] box = {x, y, z, x + 1, y + 1, z + 1};
                    double t0 = 0.0;
                    double t1 = 1.0;
                    boolean miss = false;
                    for (int k = 0; k < 3 && !miss; k++) {
                        double d = b[k] - a[k];
                        if (Math.abs(d) < 1.0E-12) {
                            miss = a[k] < box[k] || a[k] > box[k + 3];
                            continue;
                        }
                        double ta = (box[k] - a[k]) / d;
                        double tb = (box[k + 3] - a[k]) / d;
                        t0 = Math.max(t0, Math.min(ta, tb));
                        t1 = Math.min(t1, Math.max(ta, tb));
                        miss = t0 > t1;
                    }
                    if (!miss) {
                        best = Math.min(best, t0);
                    }
                }
            }
        }
        if (best == Double.MAX_VALUE) {
            return null;
        }
        return new double[] {a[0] + (b[0] - a[0]) * best, a[1] + (b[1] - a[1]) * best, a[2] + (b[2] - a[2]) * best};
    }

    /** The ground's top under (x, z) at release tick t (the trench's floor where it is cut). */
    static double floor(double x, double z, double above, double t) {
        int bx = (int) Math.floor(x);
        int bz = (int) Math.floor(z);
        for (int y = (int) Math.floor(above); y >= Y0 - 1; y--) {
            if (solid(bx, y, bz, t)) {
                return y + 1;
            }
        }
        return Y0;
    }

    // --- textures (16 px, procedural) --------------------------------------------------------------------------------------------------
    static final int[] GRASS_TOP = new int[256];
    static final int[] GRASS_SIDE = new int[256];
    static final int[] DIRT = new int[256];
    static final float[][] MEAN = new float[3][];

    static double hash(long n) {
        n = (n ^ (n >>> 33)) * 0xff51afd7ed558ccdL;
        n = (n ^ (n >>> 33)) * 0xc4ceb9fe1a85ec53L;
        n ^= n >>> 33;
        return (n >>> 11) * 0x1.0p-53;
    }

    static int rgb(double r, double g, double b) {
        return (int) (Math.max(0, Math.min(1, r)) * 255) << 16 | (int) (Math.max(0, Math.min(1, g)) * 255) << 8 | (int) (Math.max(0, Math.min(1, b)) * 255);
    }

    static void textures() {
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                double n = hash(x * 31 + y * 977 + 11);
                double m = hash(x * 131 + y * 37 + 5);
                double g = 0.82 + 0.3 * (n - 0.5) + (m > 0.86 ? 0.12 : m < 0.1 ? -0.12 : 0.0);
                GRASS_TOP[y * 16 + x] = rgb(0.36 * g, 0.6 * g, 0.24 * g);
                double d = 0.85 + 0.28 * (n - 0.5) + (m > 0.88 ? -0.22 : m < 0.08 ? 0.14 : 0.0);
                DIRT[y * 16 + x] = rgb(0.53 * d, 0.38 * d, 0.26 * d);
                int fringe = 3 + (int) (hash(x * 7 + 3) * 2.2) - (hash(x * 13 + 1) > 0.8 ? 1 : 0);
                GRASS_SIDE[y * 16 + x] = y < fringe ? GRASS_TOP[y * 16 + x] : DIRT[y * 16 + x];
            }
        }
        int[][] all = {GRASS_TOP, GRASS_SIDE, DIRT};
        for (int t = 0; t < 3; t++) {
            float[] m = new float[3];
            for (int c : all[t]) {
                m[0] += ((c >> 16) & 255) / 255F / 256F;
                m[1] += ((c >> 8) & 255) / 255F / 256F;
                m[2] += (c & 255) / 255F / 256F;
            }
            MEAN[t] = m;
        }
    }

    // --- the victim -------------------------------------------------------------------------------------------------------------------

    static Carcass.Layout layout() {
        return slim ? Carcass.Layout.PLAYER_SLIM : Carcass.Layout.PLAYER_WIDE;
    }

    /** Standing, arms a little out, as the finisher holds a body that was standing (FinisherPreview's pose). */
    static CutPose stunned() {
        return new CutPose().set("rightArm", -5, 2, 0, -0.12, 0, 0.08).set("leftArm", 5, 2, 0, 0.1, 0, -0.08).set("head", 0, 0, 0, 0.12, 0.1, 0);
    }

    /** A drop of blood or a crumb of earth: where, how fast, how big, its colour, when it was born and how long it lives. */
    static final class Mote {
        final double[] p;
        final double[] v;
        final double size;
        final int argb;
        final double born;
        final double life;
        final double gravity;
        final double drag;
        final boolean glow;
        boolean landed;

        Mote(double[] p, double[] v, double size, int argb, double born, double life, double gravity, double drag, boolean glow) {
            this.p = p;
            this.v = v;
            this.size = size;
            this.argb = argb;
            this.born = born;
            this.life = life;
            this.gravity = gravity;
            this.drag = drag;
            this.glow = glow;
        }
    }

    /** The whole run of the scene, ticked once a tick from the release; frames are drawn between ticks. */
    static final class Sim {
        final boolean hover;
        final Random random = new Random(42);
        double tick;
        // the victim
        double feetY;
        double vy;
        boolean hit;
        double hitAt = -1;
        boolean split;
        List<Carcass.Piece> pieces;
        List<Ragdoll.Body> bodies;
        boolean[] pooled;
        final List<double[]> pools = new ArrayList<>();
        final List<Mote> motes = new ArrayList<>();
        int cutIndex;
        final CutPose pose = stunned();
        final Carcass.Plane plane = Carcass.Plane.from(new double[] {0.0, 0.0, 1.0});

        Sim(boolean hover) {
            this.hover = hover;
        }

        /** One game tick: from tick to tick + 1. */
        void step() {
            double t = this.tick + 1;
            // the plane reaches the victim
            if (!this.hit && t >= HIT_TICK) {
                this.hit = true;
                this.hitAt = t;
            }
            // the cells the plane reached this tick go (with their break debris)
            while (this.cutIndex < CUTS.size() && CUTS.get(this.cutIndex)[3] <= t) {
                int[] c = CUTS.get(this.cutIndex++);
                // vanilla's break particles (TerrainParticle: 0.1..0.2 across, 4..40 ticks), far fewer than the game's 64 a block
                int pieces = c[2] > -24 && c[2] < 30 ? 3 : 1;
                for (int i = 0; i < pieces; i++) {
                    double[] at = {c[0] + this.random.nextDouble(), c[1] + this.random.nextDouble(), c[2] + this.random.nextDouble()};
                    double[] v = {(this.random.nextDouble() - 0.5) * 0.2, 0.05 + this.random.nextDouble() * 0.18, (this.random.nextDouble() - 0.5) * 0.2};
                    int tex = c[1] == -1 ? (this.random.nextDouble() < 0.5 ? GRASS_TOP : GRASS_SIDE)[this.random.nextInt(256)] : DIRT[this.random.nextInt(256)];
                    this.motes.add(new Mote(at, v, 0.08 + this.random.nextDouble() * 0.08, 0xFF000000 | tex, t, 8 + this.random.nextInt(22), 0.04, 0.98, false));
                }
            }
            // the slash's flecks of its colour, shed off its back slope (SlashRenderer.tick)
            if (t * SPEED < RANGE) {
                double foot = EYE + SwordRules.slashCentre(HEIGHT) - HEIGHT / 2.0;
                for (int i = 0; i < 4; i++) {
                    double v = this.random.nextDouble() * 0.85;
                    double back = -SwordRules.SLASH_BACK * HEIGHT * Math.pow(1.0 - v, 1.25) * this.random.nextDouble();
                    double[] at = {0.0, foot + v * HEIGHT, lineZ(t) - back};
                    this.motes.add(new Mote(at, new double[] {0.0, 0.12, 0.15}, 0.16 + HEIGHT * 0.004, COLOR, t, 8 + this.random.nextInt(6), 0.0, 0.9, true));
                }
            }
            // the victim: held where it stands (no step, no knockback); without hover it falls into the trench cut under it
            if (!this.split) {
                if (this.hit && !this.hover) {
                    this.vy = (this.vy - 0.08) * 0.98;
                    double ground = floor(0.0, 0.0, this.feetY + 0.01, t);
                    this.feetY = Math.max(ground, this.feetY + this.vy);
                    if (this.feetY <= ground) {
                        this.vy = 0.0;
                    }
                }
                if (this.hit && t - this.hitAt >= Finisher.STUN) {
                    this.split = true;
                    this.pieces = Carcass.cut(this.pose, layout(), 0.9375, Finisher.Inside.BODY, this.plane);
                    this.bodies = new ArrayList<>();
                    for (Carcass.Piece piece : this.pieces) {
                        Ragdoll.Body b = new Ragdoll.Body(piece.boxes.toArray(new double[0][]), new double[] {0.0, this.feetY, 0.0}, new double[] {1, 0, 0, 0});
                        double[][] launch = Carcass.launch(piece.kind, this.plane);
                        System.arraycopy(launch[0], 0, b.v, 0, 3);
                        System.arraycopy(launch[1], 0, b.w, 0, 3);
                        this.bodies.add(b);
                    }
                    this.pooled = new boolean[this.pieces.size()];
                }
            } else {
                double age = t - this.hitAt - Finisher.STUN;
                Ragdoll.tick(this.bodies, (a, b) -> clip(a, b, t));
                for (int i = 0; i < this.pieces.size(); i++) {
                    Ragdoll.Body b = this.bodies.get(i);
                    double[] cut = cutAt(this.pieces.get(i), b);
                    if (cut != null && age < 40.0 && this.random.nextDouble() < 0.7) {
                        double[] v = {(this.random.nextDouble() - 0.5) * 0.1, 0.02 + this.random.nextDouble() * 0.04, (this.random.nextDouble() - 0.5) * 0.1};
                        this.motes.add(new Mote(cut, v, 0.07 + this.random.nextDouble() * 0.05, 0xFF7A0A0E, t, 60, 0.04, 0.98, false));
                    }
                    if (b.asleep && !this.pooled[i]) {
                        this.pooled[i] = true;
                        double y = floor(b.pos[0], b.pos[2], b.pos[1] + 0.2, t);
                        this.pools.add(new double[] {b.pos[0], y, b.pos[2], t, this.random.nextLong()});
                    }
                }
            }
            // the motes
            for (Mote m : this.motes) {
                if (m.landed) {
                    continue;
                }
                m.v[1] -= m.gravity;
                for (int k = 0; k < 3; k++) {
                    m.v[k] *= m.drag;
                }
                double[] next = {m.p[0] + m.v[0], m.p[1] + m.v[1], m.p[2] + m.v[2]};
                double[] stop = m.gravity > 0 ? clip(m.p, next, t) : null;
                if (stop != null) {
                    System.arraycopy(stop, 0, m.p, 0, 3);
                    m.landed = true;
                } else {
                    System.arraycopy(next, 0, m.p, 0, 3);
                }
            }
            this.motes.removeIf(m -> t - m.born > m.life);
            this.tick = t;
        }
    }

    /** Where a half's cut is now (FinisherFx.cutAt): the middle of what is inside it, in the world. */
    static double[] cutAt(Carcass.Piece piece, Ragdoll.Body body) {
        if (piece.caps.isEmpty()) {
            return null;
        }
        double[] sum = new double[3];
        int step = Math.max(1, piece.caps.size() / 24);
        int counted = 0;
        for (int i = 0; i < piece.caps.size(); i += step) {
            double[] p = piece.caps.get(i).p()[0];
            for (int k = 0; k < 3; k++) {
                sum[k] += p[k];
            }
            counted++;
        }
        for (int k = 0; k < 3; k++) {
            sum[k] = sum[k] / counted - body.offset[k];
        }
        return body.toWorld(sum);
    }

    // --- the camera -------------------------------------------------------------------------------------------------------------------

    /** A key: at release tick t the eye and what it looks at. */
    record Key(double t, double[] eye, double[] look) {
    }

    static double[] lerp3(double[] a, double[] b, double u) {
        return new double[] {a[0] + (b[0] - a[0]) * u, a[1] + (b[1] - a[1]) * u, a[2] + (b[2] - a[2]) * u};
    }

    static double smooth(double u) {
        u = Math.max(0, Math.min(1, u));
        return u * u * (3 - 2 * u);
    }

    static double[][] camera(List<Key> keys, double t) {
        if (t <= keys.get(0).t()) {
            return new double[][] {keys.get(0).eye(), keys.get(0).look()};
        }
        for (int i = 0; i + 1 < keys.size(); i++) {
            Key a = keys.get(i);
            Key b = keys.get(i + 1);
            if (t <= b.t()) {
                double u = smooth((t - a.t()) / (b.t() - a.t()));
                return new double[][] {lerp3(a.eye(), b.eye(), u), lerp3(a.look(), b.look(), u)};
            }
        }
        Key last = keys.get(keys.size() - 1);
        return new double[][] {last.eye(), last.look()};
    }

    // --- drawing a frame --------------------------------------------------------------------------------------------------------------

    static BufferedImage frame(Sim sim, double t, double[] eye, double[] look) {
        Raster r = new Raster(eye, look);
        // the world: every block face open to the air, near; big flat tiles beyond
        world(r, t);
        // the victim, standing, or its halves
        if (!sim.split) {
            double y = sim.feetY;
            List<Carcass.Piece> whole = Carcass.cut(sim.pose, layout(), 0.9375, Finisher.Inside.BODY, Carcass.Plane.from(new double[] {0, 0, 1}));
            for (Carcass.Piece piece : whole) {
                for (Carcass.Face f : piece.faces) {
                    double[][] p = new double[4][];
                    for (int k = 0; k < 4; k++) {
                        p[k] = new double[] {f.p()[k][0], f.p()[k][1] + y, f.p()[k][2]};
                    }
                    r.skinQuad(p, f.uv(), f.n());
                }
            }
            if (sim.hit) {
                double amount = Finisher.round(t - sim.hitAt);
                if (amount > 0.0) {
                    Carcass.wound(sim.pose, layout(), 0.9375, amount, Carcass.FLAT_LAYER, sim.plane, (a, b, c, d, n, ca, cb, cc, cd) -> {
                        double[][] p = {a, b, c, d};
                        double[][] q = new double[4][];
                        for (int k = 0; k < 4; k++) {
                            q[k] = new double[] {p[k][0], p[k][1] + y, p[k][2]};
                        }
                        r.flatQuad(q, n, Carcass.GLOW, true);
                    });
                }
            }
        } else {
            for (int i = 0; i < sim.pieces.size(); i++) {
                Carcass.Piece piece = sim.pieces.get(i);
                Ragdoll.Body b = sim.bodies.get(i);
                for (Carcass.Face f : piece.faces) {
                    double[][] p = new double[4][];
                    for (int k = 0; k < 4; k++) {
                        p[k] = place(b, f.p()[k]);
                    }
                    r.skinQuad(p, f.uv(), Ragdoll.rotate(b.q, f.n()));
                }
                for (Carcass.Cap c : piece.caps) {
                    double[][] p = new double[4][];
                    for (int k = 0; k < 4; k++) {
                        p[k] = place(b, c.p()[k]);
                    }
                    r.flatQuad(p, Ragdoll.rotate(b.q, c.n()), c.argb()[0], false);
                }
            }
        }
        // the pools, growing over 60 ticks (BloodStains.pool, 1.1 across)
        for (double[] pool : sim.pools) {
            double grow = Math.min(1.0, (t - pool[3]) / 60.0);
            if (grow > 0) {
                r.pool(pool[0], pool[1], pool[2], 0.55 * Math.sqrt(grow), (long) pool[4]);
            }
        }
        // drops and debris (opaque), the slash's motes (glow) later
        for (Mote m : sim.motes) {
            if (!m.glow) {
                r.billboard(m.p, m.size, m.argb, false, 1.0F);
            }
        }
        // the wave and its crackle, as SlashRenderer lays them
        // SlashRenderer: past its range it fades over three ticks
        double d = along(t);
        double over = t - RANGE / SPEED;
        double fade = over > 0 ? Math.max(0.0, 1.0 - over / 3.0) : 1.0;
        if (fade > 0.01) {
            double footY = EYE + SwordRules.slashCentre(HEIGHT) - HEIGHT / 2.0;
            double[] foot = {0.0 - eye[0], footY - eye[1], lineZ(t) - eye[2]};
            SlashMesh.Frame f = SlashMesh.Frame.of(foot, 0.0, -1.0, HEIGHT, d / Math.max(0.5, HEIGHT * SwordRules.SLASH_BACK));
            SlashMesh.Quads solid = new SlashMesh.Quads();
            SlashMesh.Quads glow = new SlashMesh.Quads();
            SlashMesh.wave(solid, glow, f, ID, COLOR, t, fade);
            for (int q = 0; q < solid.n; q++) {
                r.gameQuad(Arrays.copyOfRange(solid.v, q * 12, q * 12 + 12), Arrays.copyOfRange(solid.c, q * 4, q * 4 + 4));
            }
            if (fade >= 1.0) {
                crackle(r, f, t);
            }
            glow.sortBackToFront();
            for (int q = 0; q < glow.n; q++) {
                r.gameQuadNoCull(Arrays.copyOfRange(glow.v, q * 12, q * 12 + 12), Arrays.copyOfRange(glow.c, q * 4, q * 4 + 4));
            }
        }
        for (Mote m : sim.motes) {
            if (m.glow) {
                float alpha = (float) Math.max(0.0, 1.0 - (t - m.born) / m.life);
                r.billboard(m.p, m.size * (0.5 + 0.5 * alpha), m.argb, true, alpha);
            }
        }
        return r.image();
    }

    static double[] place(Ragdoll.Body body, double[] p) {
        return body.toWorld(new double[] {p[0] - body.offset[0], p[1] - body.offset[1], p[2] - body.offset[2]});
    }

    static final double BLADE = 1.28;

    static void crackle(Raster r, SlashMesh.Frame frame, double ticks) {
        double k = HEIGHT / BLADE;
        long window = (long) Math.floor(ticks / DrawCrackle.WINDOW);
        List<Bolt> bolts = new ArrayList<>();
        List<Double> ages = new ArrayList<>();
        for (long w = Math.max(0, window - 2); w <= window; w++) {
            for (Bolt bolt : DrawCrackle.set(ID * 0x9E3779B97F4A7C15L, w, 0.0, BLADE, Double.NaN, 1.3, COLOR)) {
                double age = ticks - w * DrawCrackle.WINDOW - bolt.delay;
                if (age < 0 || age > bolt.duration) {
                    continue;
                }
                double[] points = new double[bolt.points.length];
                for (int i = 0; i < bolt.count(); i++) {
                    double v = Math.max(0.0, Math.min(0.97, bolt.points[i * 3 + 1] / BLADE * 0.97));
                    double[] p = SlashMesh.edge(frame, v, HEIGHT * 0.01 + Math.abs(bolt.points[i * 3]) * k, bolt.points[i * 3 + 2] * k);
                    points[i * 3] = p[0];
                    points[i * 3 + 1] = p[1];
                    points[i * 3 + 2] = p[2];
                }
                float[] widths = bolt.widths.clone();
                float grow = (float) Math.min(3.0, Math.pow(k, 0.75));
                for (int i = 0; i < widths.length; i++) {
                    widths[i] *= grow;
                }
                bolts.add(new Bolt(points, widths, bolt.color, bolt.kind, 0, bolt.duration, bolt.crawl, bolt.seed, bolt.dance));
                ages.add(age);
            }
        }
        r.light();
        double pixel = 2.0 * Math.tan(FOV / 2) / H;
        List<double[]> gv = new ArrayList<>();
        List<int[]> gc = new ArrayList<>();
        for (int i = 0; i < bolts.size(); i++) {
            BoltMesh.build(bolts.get(i), BoltMesh.Frame.at(0, 0, 0), ages.get(i), pixel, 0.5, (g, xyz, argb) -> {
                if (g) {
                    gv.add(xyz);
                    gc.add(argb);
                } else {
                    r.gameQuad(xyz, argb);
                }
            });
        }
        for (int i = 0; i < gv.size(); i++) {
            r.glowQuad(gv.get(i), gc.get(i));
        }
    }

    /** Block faces open to the air within reach; beyond, the plain as big tiles. */
    static void world(Raster r, double t) {
        int reach = 72;
        int cx = (int) Math.floor(r.eye[0]);
        int cz = (int) Math.floor(r.eye[2]);
        for (int x = Math.max(X0, cx - reach); x < Math.min(X1, cx + reach); x++) {
            for (int z = Math.max(Z0, cz - reach); z < Math.min(Z1, cz + reach); z++) {
                for (int y = -1; y >= -4; y--) {
                    if (!solid(x, y, z, t)) {
                        continue;
                    }
                    // the grass layer's sides are grass-fringed dirt; deeper, dirt; a cut floor is bare dirt
                    int[] side = y == -1 ? GRASS_SIDE : DIRT;
                    int sideId = y == -1 ? 1 : 2;
                    if (!solid(x, y + 1, z, t)) {
                        r.blockFace(x, y, z, 4, y == -1 ? GRASS_TOP : DIRT, y == -1 ? 0 : 2);
                    }
                    if (!solid(x - 1, y, z, t)) {
                        r.blockFace(x, y, z, 0, side, sideId);
                    }
                    if (!solid(x + 1, y, z, t)) {
                        r.blockFace(x, y, z, 1, side, sideId);
                    }
                    if (!solid(x, y, z - 1, t)) {
                        r.blockFace(x, y, z, 2, side, sideId);
                    }
                    if (!solid(x, y, z + 1, t)) {
                        r.blockFace(x, y, z, 3, side, sideId);
                    }
                }
            }
        }
        // the plain beyond the blocks: 16-block tiles, the grass's mean colour
        for (int tx = -14; tx < 14; tx++) {
            for (int tz = -14; tz < 14; tz++) {
                double x0 = tx * 16;
                double z0 = tz * 16;
                if (x0 + 16 > cx - reach && x0 < cx + reach && z0 + 16 > cz - reach && z0 < cz + reach && x0 + 16 > X0 && x0 < X1 && z0 + 16 > Z0
                    && z0 < Z1) {
                    // partly covered by blocks: draw only its parts outside them, as four strips
                    double bx0 = Math.max(X0, cx - reach);
                    double bx1 = Math.min(X1, cx + reach);
                    double bz0 = Math.max(Z0, cz - reach);
                    double bz1 = Math.min(Z1, cz + reach);
                    r.plain(x0, z0, Math.min(x0 + 16, bx0), z0 + 16);
                    r.plain(Math.max(x0, bx1), z0, x0 + 16, z0 + 16);
                    r.plain(Math.max(x0, bx0), z0, Math.min(x0 + 16, bx1), Math.min(z0 + 16, bz0));
                    r.plain(Math.max(x0, bx0), Math.max(z0, bz1), Math.min(x0 + 16, bx1), z0 + 16);
                } else {
                    r.plain(x0, z0, x0 + 16, z0 + 16);
                }
            }
        }
    }

    // --- the rasteriser ---------------------------------------------------------------------------------------------------------------

    static final class Raster {
        final int w = W * SS;
        final int h = H * SS;
        final float[] depth = new float[w * h];
        final float[] r = new float[w * h];
        final float[] g = new float[w * h];
        final float[] b = new float[w * h];
        final double[] eye;
        final double[] fw;
        final double[] rt;
        final double[] up;
        final double focal;
        static final float[] FOG = {0.66F, 0.78F, 0.95F};

        Raster(double[] eye, double[] look) {
            this.eye = eye;
            double[] f = norm(sub(look, eye));
            double[] right = norm(cross(f, new double[] {0, 1, 0}));
            this.fw = f;
            this.rt = right;
            this.up = cross(right, f);
            this.focal = (this.h / 2.0) / Math.tan(FOV / 2);
            for (int i = 0; i < this.w * this.h; i++) {
                int y = i / this.w;
                // the sky: deeper blue overhead, paler to the horizon (where the view's ray is level)
                double[] ray = norm(add(this.fw, add(scale(this.up, (this.h / 2.0 - y) / this.focal), new double[3])));
                double k = Math.max(0.0, Math.min(1.0, ray[1] * 2.2));
                this.r[i] = (float) (FOG[0] + (0.42 - FOG[0]) * k);
                this.g[i] = (float) (FOG[1] + (0.6 - FOG[1]) * k);
                this.b[i] = (float) (FOG[2] + (0.92 - FOG[2]) * k);
                this.depth[i] = Float.MAX_VALUE;
            }
        }

        /** 2.2.6: the bolts' key light as the game sets it (BoltMesh.VIEW_LIGHT turned into the camera-relative frame). */
        void light() {
            double[] l = BoltMesh.VIEW_LIGHT;
            BoltMesh.light(this.rt[0] * l[0] + this.up[0] * l[1] - this.fw[0] * l[2], this.rt[1] * l[0] + this.up[1] * l[1] - this.fw[1] * l[2],
                this.rt[2] * l[0] + this.up[2] * l[1] - this.fw[2] * l[2]);
        }

        // vanilla's block faces: -x, +x, -z, +z, top; shading 0.6, 0.6, 0.8, 0.8, 1.0
        void blockFace(int x, int y, int z, int face, int[] tex, int texId) {
            double[][] p;
            double[] n;
            float shade;
            switch (face) {
                case 0 -> {
                    p = new double[][] {{x, y + 1, z}, {x, y + 1, z + 1}, {x, y, z + 1}, {x, y, z}};
                    n = new double[] {-1, 0, 0};
                    shade = 0.6F;
                }
                case 1 -> {
                    p = new double[][] {{x + 1, y + 1, z + 1}, {x + 1, y + 1, z}, {x + 1, y, z}, {x + 1, y, z + 1}};
                    n = new double[] {1, 0, 0};
                    shade = 0.6F;
                }
                case 2 -> {
                    p = new double[][] {{x + 1, y + 1, z}, {x, y + 1, z}, {x, y, z}, {x + 1, y, z}};
                    n = new double[] {0, 0, -1};
                    shade = 0.8F;
                }
                case 3 -> {
                    p = new double[][] {{x, y + 1, z + 1}, {x + 1, y + 1, z + 1}, {x + 1, y, z + 1}, {x, y, z + 1}};
                    n = new double[] {0, 0, 1};
                    shade = 0.8F;
                }
                default -> {
                    p = new double[][] {{x, y + 1, z}, {x + 1, y + 1, z}, {x + 1, y + 1, z + 1}, {x, y + 1, z + 1}};
                    n = new double[] {0, 1, 0};
                    shade = 1.0F;
                }
            }
            // a back face is never seen
            if (dot(n, sub(p[0], this.eye)) >= 0) {
                return;
            }
            // a cut floor deep in the trench is in shadow (its sky light cut down by the walls)
            if (y < -1 && face == 4) {
                shade = 0.72F;
            } else if (y < -1) {
                shade *= 0.85F;
            }
            double[][] uv = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
            texTri(p[0], p[1], p[2], uv[0], uv[1], uv[2], tex, texId, shade);
            texTri(p[0], p[2], p[3], uv[0], uv[2], uv[3], tex, texId, shade);
        }

        void plain(double x0, double z0, double x1, double z1) {
            if (x1 - x0 < 1.0E-6 || z1 - z0 < 1.0E-6) {
                return;
            }
            float[] m = MEAN[0];
            float[] c = {m[0], m[1], m[2]};
            colTri(new double[] {x0, 0, z0}, new double[] {x1, 0, z0}, new double[] {x1, 0, z1}, c, c, c, null, true);
            colTri(new double[] {x0, 0, z0}, new double[] {x1, 0, z1}, new double[] {x0, 0, z1}, c, c, c, null, true);
        }

        /** The skin on a face of the body (world points), lit as the game lights entities (two lamps, 0.4 ambient). */
        void skinQuad(double[][] p, double[][] uv, double[] n) {
            double[] l1 = norm(new double[] {0.2, 1.0, -0.7});
            double[] l2 = norm(new double[] {-0.2, 1.0, 0.7});
            float light = (float) Math.min(1.0, 0.4 + 0.6 * (Math.max(0, dot(n, l1)) + Math.max(0, dot(n, l2))));
            texTri(p[0], p[1], p[2], uv[0], uv[1], uv[2], null, -1, light);
            texTri(p[0], p[2], p[3], uv[0], uv[2], uv[3], null, -1, light);
        }

        /** A plain coloured face (the cut's insides, entity-lit; the line, full bright). */
        void flatQuad(double[][] p, double[] n, int argb, boolean bright) {
            float light = 1.0F;
            if (!bright) {
                double[] l1 = norm(new double[] {0.2, 1.0, -0.7});
                double[] l2 = norm(new double[] {-0.2, 1.0, 0.7});
                light = (float) Math.min(1.0, 0.4 + 0.6 * (Math.max(0, dot(n, l1)) + Math.max(0, dot(n, l2))));
            }
            float[] c = {((argb >> 16) & 255) / 255F * light, ((argb >> 8) & 255) / 255F * light, (argb & 255) / 255F * light};
            colTri(p[0], p[1], p[2], c, c, c, null, true);
            colTri(p[0], p[2], p[3], c, c, c, null, true);
        }

        /** A pool of blood on the ground: a dark, slightly ragged disc. */
        void pool(double x, double y, double z, double radius, long seed) {
            int n = 28;
            float[] c = {0.42F, 0.02F, 0.04F};
            float[] edge = {0.55F, 0.05F, 0.06F};
            double[] centre = {x, y + 0.006, z};
            for (int k = 0; k < n; k++) {
                double r0 = radius * (0.85 + 0.25 * hash(seed + k % n));
                double r1 = radius * (0.85 + 0.25 * hash(seed + (k + 1) % n));
                double a0 = Math.PI * 2 * k / n;
                double a1 = Math.PI * 2 * (k + 1) / n;
                double[] p0 = {x + Math.cos(a0) * r0, y + 0.006, z + Math.sin(a0) * r0};
                double[] p1 = {x + Math.cos(a1) * r1, y + 0.006, z + Math.sin(a1) * r1};
                colTri(centre, p1, p0, c, edge, edge, null, true);
                colTri(centre, p0, p1, c, edge, edge, null, true);
            }
        }

        /** A small square facing the eye (a drop, a crumb; glow: the slash's motes, blended). */
        void billboard(double[] p, double size, int argb, boolean glow, float alpha) {
            double s = size / 2;
            double[] a = add(add(p, scale(this.rt, -s)), scale(this.up, -s));
            double[] b2 = add(add(p, scale(this.rt, s)), scale(this.up, -s));
            double[] c = add(add(p, scale(this.rt, s)), scale(this.up, s));
            double[] d = add(add(p, scale(this.rt, -s)), scale(this.up, s));
            float[] col = {((argb >> 16) & 255) / 255F, ((argb >> 8) & 255) / 255F, (argb & 255) / 255F};
            if (glow) {
                float[] al = {alpha * 0.8F, alpha * 0.8F, alpha * 0.8F};
                colTri(a, b2, c, col, col, col, al, false);
                colTri(a, c, d, col, col, col, al, false);
            } else {
                colTri(a, b2, c, col, col, col, null, true);
                colTri(a, c, d, col, col, col, null, true);
            }
        }

        // --- the game's quads (camera-relative, as SlashMesh and BoltMesh build them) ---

        /** BoltPreview's quad: alpha blended, depth written, back faces culled (the game's batches cull). */
        void gameQuad(double[] v, int[] c) {
            double[] a = {v[0] + this.eye[0], v[1] + this.eye[1], v[2] + this.eye[2]};
            double[] b2 = {v[3] + this.eye[0], v[4] + this.eye[1], v[5] + this.eye[2]};
            double[] c2 = {v[6] + this.eye[0], v[7] + this.eye[1], v[8] + this.eye[2]};
            double[] d = {v[9] + this.eye[0], v[10] + this.eye[1], v[11] + this.eye[2]};
            float[] al = {((c[0] >>> 24) & 255) / 255F, ((c[1] >>> 24) & 255) / 255F, ((c[2] >>> 24) & 255) / 255F, ((c[3] >>> 24) & 255) / 255F};
            if (front(v, 0, 3, 6)) {
                colTri(a, b2, c2, rgbf(c[0]), rgbf(c[1]), rgbf(c[2]), opaque(al[0], al[1], al[2]) ? null : new float[] {al[0], al[1], al[2]}, true);
            }
            if (front(v, 0, 6, 9)) {
                colTri(a, c2, d, rgbf(c[0]), rgbf(c[2]), rgbf(c[3]), opaque(al[0], al[2], al[3]) ? null : new float[] {al[0], al[2], al[3]}, true);
            }
        }

        static boolean opaque(float a, float b, float c) {
            return a >= 1F && b >= 1F && c >= 1F;
        }

        static boolean front(double[] v, int i, int j, int k) {
            double[] a = {v[i], v[i + 1], v[i + 2]};
            double[] b = {v[j], v[j + 1], v[j + 2]};
            double[] c = {v[k], v[k + 1], v[k + 2]};
            double[] n = cross(sub(b, a), sub(c, a));
            return -dot(n, a) >= -1e-12;
        }

        /** BoltPreview's quadNoCull: the slash's glow batch, alpha blended, depth written, both faces. */
        void gameQuadNoCull(double[] v, int[] c) {
            double[] a = {v[0] + this.eye[0], v[1] + this.eye[1], v[2] + this.eye[2]};
            double[] b2 = {v[3] + this.eye[0], v[4] + this.eye[1], v[5] + this.eye[2]};
            double[] c2 = {v[6] + this.eye[0], v[7] + this.eye[1], v[8] + this.eye[2]};
            double[] d = {v[9] + this.eye[0], v[10] + this.eye[1], v[11] + this.eye[2]};
            float[] al = {((c[0] >>> 24) & 255) / 255F, ((c[1] >>> 24) & 255) / 255F, ((c[2] >>> 24) & 255) / 255F, ((c[3] >>> 24) & 255) / 255F};
            float old = this.minAlpha;
            this.minAlpha = 0.002F;
            colTri(a, b2, c2, rgbf(c[0]), rgbf(c[1]), rgbf(c[2]), new float[] {al[0], al[1], al[2]}, true);
            colTri(a, c2, d, rgbf(c[0]), rgbf(c[2]), rgbf(c[3]), new float[] {al[0], al[2], al[3]}, true);
            this.minAlpha = old;
        }

        /** BoltPreview's glowQuad (HakiBolts' glow: the hue at full brightness, as opaque as bright x 0.9, depth tested, not written). */
        void glowQuad(double[] v, int[] c) {
            int top = 0;
            int hue = 0xFFFFFF;
            for (int k = 0; k < 4; k++) {
                int bright = Math.max((c[k] >>> 16) & 255, Math.max((c[k] >>> 8) & 255, c[k] & 255));
                if (bright > top) {
                    top = bright;
                    hue = c[k];
                }
            }
            if (top == 0) {
                return;
            }
            float k = 1.0F / top;
            float[] col = {((hue >>> 16) & 255) * k, ((hue >>> 8) & 255) * k, (hue & 255) * k};
            float[] al = new float[4];
            for (int i = 0; i < 4; i++) {
                al[i] = Math.max((c[i] >>> 16) & 255, Math.max((c[i] >>> 8) & 255, c[i] & 255)) / 255F * 0.9F;
            }
            double[] a = {v[0] + this.eye[0], v[1] + this.eye[1], v[2] + this.eye[2]};
            double[] b2 = {v[3] + this.eye[0], v[4] + this.eye[1], v[5] + this.eye[2]};
            double[] c2 = {v[6] + this.eye[0], v[7] + this.eye[1], v[8] + this.eye[2]};
            double[] d = {v[9] + this.eye[0], v[10] + this.eye[1], v[11] + this.eye[2]};
            float old = this.minAlpha;
            this.minAlpha = 0.004F;
            colTri(a, b2, c2, col, col, col, new float[] {al[0], al[1], al[2]}, false);
            colTri(a, c2, d, col, col, col, new float[] {al[0], al[2], al[3]}, false);
            this.minAlpha = old;
        }

        float minAlpha = 0.1F;

        static float[] rgbf(int argb) {
            return new float[] {((argb >>> 16) & 255) / 255F, ((argb >>> 8) & 255) / 255F, (argb & 255) / 255F};
        }

        double[] project(double[] world) {
            double[] q = sub(world, this.eye);
            double x = dot(q, this.rt);
            double y = dot(q, this.up);
            double z = dot(q, this.fw);
            if (z < 0.05) {
                return null;
            }
            return new double[] {this.w / 2.0 + x / z * this.focal, this.h / 2.0 - y / z * this.focal, 1.0 / z};
        }

        /** Fog as the game thins the far world into the sky (opaque surfaces only). */
        static float fog(float z) {
            double u = (z - 48.0) / 90.0;
            return (float) Math.max(0.0, Math.min(1.0, u));
        }

        /** A triangle of one texture (tex null: the skin), sampled nearest, alpha tested; far texels give the texture's mean colour. */
        static final double NEAR = 0.06;

        /** The polygon {@code verts} ({x, y, z, attributes...}) cut at the near plane (the part in front of the eye), or null if none. */
        List<double[]> clipNear(double[][] verts) {
            boolean all = true;
            boolean none = true;
            double[] zs = new double[verts.length];
            for (int i = 0; i < verts.length; i++) {
                zs[i] = (verts[i][0] - this.eye[0]) * this.fw[0] + (verts[i][1] - this.eye[1]) * this.fw[1] + (verts[i][2] - this.eye[2]) * this.fw[2];
                all &= zs[i] >= NEAR;
                none &= zs[i] < NEAR;
            }
            if (none) {
                return null;
            }
            List<double[]> out = new ArrayList<>();
            if (all) {
                out.addAll(Arrays.asList(verts));
                return out;
            }
            for (int i = 0; i < verts.length; i++) {
                int j = (i + 1) % verts.length;
                boolean in = zs[i] >= NEAR;
                if (in) {
                    out.add(verts[i]);
                }
                if (in != zs[j] >= NEAR) {
                    double t = (NEAR - zs[i]) / (zs[j] - zs[i]);
                    double[] m = new double[verts[i].length];
                    for (int k = 0; k < m.length; k++) {
                        m[k] = verts[i][k] + (verts[j][k] - verts[i][k]) * t;
                    }
                    out.add(m);
                }
            }
            return out.size() >= 3 ? out : null;
        }

        /** A textured triangle, cut at the near plane first (a big tile or a wall reaching behind the eye keeps its part in front). */
        void texTri(double[] p0, double[] p1, double[] p2, double[] t0, double[] t1, double[] t2, int[] tex, int texId, float shade) {
            List<double[]> poly = clipNear(new double[][] {{p0[0], p0[1], p0[2], t0[0], t0[1]}, {p1[0], p1[1], p1[2], t1[0], t1[1]},
                {p2[0], p2[1], p2[2], t2[0], t2[1]}});
            if (poly == null) {
                return;
            }
            for (int i = 1; i + 1 < poly.size(); i++) {
                double[] a = poly.get(0);
                double[] b = poly.get(i);
                double[] c = poly.get(i + 1);
                texTriRaw(new double[] {a[0], a[1], a[2]}, new double[] {b[0], b[1], b[2]}, new double[] {c[0], c[1], c[2]}, new double[] {a[3], a[4]},
                    new double[] {b[3], b[4]}, new double[] {c[3], c[4]}, tex, texId, shade);
            }
        }

        /** A coloured triangle, cut at the near plane first. */
        void colTri(double[] p0, double[] p1, double[] p2, float[] c0, float[] c1, float[] c2, float[] alpha, boolean write) {
            double a0 = alpha == null ? 1 : alpha[0];
            double a1 = alpha == null ? 1 : alpha[1];
            double a2 = alpha == null ? 1 : alpha[2];
            List<double[]> poly = clipNear(new double[][] {{p0[0], p0[1], p0[2], c0[0], c0[1], c0[2], a0}, {p1[0], p1[1], p1[2], c1[0], c1[1], c1[2], a1},
                {p2[0], p2[1], p2[2], c2[0], c2[1], c2[2], a2}});
            if (poly == null) {
                return;
            }
            for (int i = 1; i + 1 < poly.size(); i++) {
                double[] a = poly.get(0);
                double[] b = poly.get(i);
                double[] c = poly.get(i + 1);
                colTriRaw(new double[] {a[0], a[1], a[2]}, new double[] {b[0], b[1], b[2]}, new double[] {c[0], c[1], c[2]},
                    new float[] {(float) a[3], (float) a[4], (float) a[5]}, new float[] {(float) b[3], (float) b[4], (float) b[5]},
                    new float[] {(float) c[3], (float) c[4], (float) c[5]}, alpha == null ? null : new float[] {(float) a[6], (float) b[6], (float) c[6]},
                    write);
            }
        }

        void texTriRaw(double[] p0, double[] p1, double[] p2, double[] t0, double[] t1, double[] t2, int[] tex, int texId, float shade) {
            double[][] s = {project(p0), project(p1), project(p2)};
            for (double[] q : s) {
                if (q == null) {
                    return;
                }
            }
            double area = (s[1][0] - s[0][0]) * (s[2][1] - s[0][1]) - (s[2][0] - s[0][0]) * (s[1][1] - s[0][1]);
            if (Math.abs(area) < 1.0E-12) {
                return;
            }
            int x0 = (int) Math.max(0, Math.floor(Math.min(s[0][0], Math.min(s[1][0], s[2][0]))));
            int x1 = (int) Math.min(this.w - 1, Math.ceil(Math.max(s[0][0], Math.max(s[1][0], s[2][0]))));
            int y0 = (int) Math.max(0, Math.floor(Math.min(s[0][1], Math.min(s[1][1], s[2][1]))));
            int y1 = (int) Math.min(this.h - 1, Math.ceil(Math.max(s[0][1], Math.max(s[1][1], s[2][1]))));
            for (int y = y0; y <= y1; y++) {
                for (int x = x0; x <= x1; x++) {
                    double px = x + 0.5;
                    double py = y + 0.5;
                    double l0 = ((s[1][0] - px) * (s[2][1] - py) - (s[2][0] - px) * (s[1][1] - py)) / area;
                    double l1 = ((s[2][0] - px) * (s[0][1] - py) - (s[0][0] - px) * (s[2][1] - py)) / area;
                    double l2 = 1 - l0 - l1;
                    if (l0 < 0 || l1 < 0 || l2 < 0) {
                        continue;
                    }
                    double iz = l0 * s[0][2] + l1 * s[1][2] + l2 * s[2][2];
                    float z = (float) (1.0 / iz);
                    int i = y * this.w + x;
                    if (z >= this.depth[i]) {
                        continue;
                    }
                    double k0 = l0 * s[0][2] / iz;
                    double k1 = l1 * s[1][2] / iz;
                    double k2 = l2 * s[2][2] / iz;
                    double u = t0[0] * k0 + t1[0] * k1 + t2[0] * k2;
                    double v = t0[1] * k0 + t1[1] * k1 + t2[1] * k2;
                    float cr;
                    float cg;
                    float cb;
                    if (tex == null) {
                        int tx = Math.max(0, Math.min(skin.getWidth() - 1, (int) Math.floor(u * skin.getWidth() - 1.0E-6)));
                        int ty = Math.max(0, Math.min(skin.getHeight() - 1, (int) Math.floor(v * skin.getHeight() - 1.0E-6)));
                        int c = skin.getRGB(tx, ty);
                        if ((c >>> 24) < 26) {
                            continue;
                        }
                        cr = ((c >> 16) & 255) / 255F;
                        cg = ((c >> 8) & 255) / 255F;
                        cb = (c & 255) / 255F;
                    } else if (z / this.focal * 16.0 > 0.9) {
                        float[] m = MEAN[texId];
                        cr = m[0];
                        cg = m[1];
                        cb = m[2];
                    } else {
                        int tx = Math.floorMod((int) Math.floor(u * 16), 16);
                        int ty = Math.floorMod((int) Math.floor(v * 16), 16);
                        int c = tex[ty * 16 + tx];
                        cr = ((c >> 16) & 255) / 255F;
                        cg = ((c >> 8) & 255) / 255F;
                        cb = (c & 255) / 255F;
                    }
                    float f = fog(z);
                    this.r[i] = (cr * shade) * (1 - f) + FOG[0] * f;
                    this.g[i] = (cg * shade) * (1 - f) + FOG[1] * f;
                    this.b[i] = (cb * shade) * (1 - f) + FOG[2] * f;
                    this.depth[i] = z;
                }
            }
        }

        /** A triangle of per-corner colours: opaque (alpha null) or blended; depth tested, written if asked. */
        void colTriRaw(double[] p0, double[] p1, double[] p2, float[] c0, float[] c1, float[] c2, float[] alpha, boolean write) {
            double[][] s = {project(p0), project(p1), project(p2)};
            for (double[] q : s) {
                if (q == null) {
                    return;
                }
            }
            double area = (s[1][0] - s[0][0]) * (s[2][1] - s[0][1]) - (s[2][0] - s[0][0]) * (s[1][1] - s[0][1]);
            if (Math.abs(area) < 1.0E-12) {
                return;
            }
            int x0 = (int) Math.max(0, Math.floor(Math.min(s[0][0], Math.min(s[1][0], s[2][0]))));
            int x1 = (int) Math.min(this.w - 1, Math.ceil(Math.max(s[0][0], Math.max(s[1][0], s[2][0]))));
            int y0 = (int) Math.max(0, Math.floor(Math.min(s[0][1], Math.min(s[1][1], s[2][1]))));
            int y1 = (int) Math.min(this.h - 1, Math.ceil(Math.max(s[0][1], Math.max(s[1][1], s[2][1]))));
            for (int y = y0; y <= y1; y++) {
                for (int x = x0; x <= x1; x++) {
                    double px = x + 0.5;
                    double py = y + 0.5;
                    double l0 = ((s[1][0] - px) * (s[2][1] - py) - (s[2][0] - px) * (s[1][1] - py)) / area;
                    double l1 = ((s[2][0] - px) * (s[0][1] - py) - (s[0][0] - px) * (s[2][1] - py)) / area;
                    double l2 = 1 - l0 - l1;
                    if (l0 < 0 || l1 < 0 || l2 < 0) {
                        continue;
                    }
                    double iz = l0 * s[0][2] + l1 * s[1][2] + l2 * s[2][2];
                    float z = (float) (1.0 / iz);
                    int i = y * this.w + x;
                    if (z >= this.depth[i]) {
                        continue;
                    }
                    double k0 = l0 * s[0][2] / iz;
                    double k1 = l1 * s[1][2] / iz;
                    double k2 = l2 * s[2][2] / iz;
                    float cr = (float) (c0[0] * k0 + c1[0] * k1 + c2[0] * k2);
                    float cg = (float) (c0[1] * k0 + c1[1] * k1 + c2[1] * k2);
                    float cb = (float) (c0[2] * k0 + c1[2] * k1 + c2[2] * k2);
                    if (alpha == null) {
                        float f = fog(z);
                        this.r[i] = cr * (1 - f) + FOG[0] * f;
                        this.g[i] = cg * (1 - f) + FOG[1] * f;
                        this.b[i] = cb * (1 - f) + FOG[2] * f;
                        this.depth[i] = z;
                        continue;
                    }
                    float a = (float) (alpha[0] * k0 + alpha[1] * k1 + alpha[2] * k2);
                    if (a < this.minAlpha) {
                        continue;
                    }
                    this.r[i] = cr * a + this.r[i] * (1 - a);
                    this.g[i] = cg * a + this.g[i] * (1 - a);
                    this.b[i] = cb * a + this.b[i] * (1 - a);
                    if (write) {
                        this.depth[i] = z;
                    }
                }
            }
        }

        BufferedImage image() {
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    float sr = 0;
                    float sg = 0;
                    float sb = 0;
                    for (int dy = 0; dy < SS; dy++) {
                        for (int dx = 0; dx < SS; dx++) {
                            int i = (y * SS + dy) * this.w + x * SS + dx;
                            sr += Math.min(1F, this.r[i]);
                            sg += Math.min(1F, this.g[i]);
                            sb += Math.min(1F, this.b[i]);
                        }
                    }
                    float n = SS * SS;
                    img.setRGB(x, y, new Color(sr / n, sg / n, sb / n).getRGB());
                }
            }
            return img;
        }
    }

    // --- the run ----------------------------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        File dir = new File(args[0]);
        dir.mkdirs();
        skin = ImageIO.read(new File(args.length > 1 ? args[1] : "art/user_skin.png"));
        slim = (skin.getRGB(54, 20) >>> 24) == 0 && (skin.getRGB(55, 25) >>> 24) == 0;
        boolean hover = args.length > 2 && args[2].equals("hover");
        double fps = args.length > 3 ? Double.parseDouble(args[3]) : 30.0;
        Set<Integer> only = new HashSet<>();
        if (args.length > 4) {
            for (String n : args[4].split(",")) {
                if (!n.isBlank()) {
                    only.add(Integer.parseInt(n.trim()));
                }
            }
        }
        textures();
        trench();
        double hit = HIT_TICK;
        // the shot, in release ticks (the plane reaches the victim at `hit`)
        List<Key> keys = new ArrayList<>();
        // wide, from the side and behind: the wave's flank as it comes in, the victim small before it
        keys.add(new Key(hit - 15, new double[] {12.0, 3.0, -8.0}, new double[] {0.0, 5.0, 16.0}));
        keys.add(new Key(hit - 3, new double[] {8.5, 2.2, -5.0}, new double[] {0.0, 4.0, 6.0}));
        // it passes through him, seen from outside its glow
        keys.add(new Key(hit + 1, new double[] {9.0, 2.8, -2.0}, new double[] {0.0, 2.0, 2.0}));
        keys.add(new Key(hit + 5, new double[] {6.0, 2.5, 3.0}, new double[] {0.0, -0.5, 0.0}));
        // down into the trench, in front of him: the line drawing round him, close
        keys.add(new Key(hit + 13, new double[] {1.3, -0.2, 3.2}, new double[] {0.0, -0.7, 0.0}));
        keys.add(new Key(hit + 40, new double[] {0.85, -0.45, 2.0}, new double[] {0.0, -0.65, 0.0}));
        // the cut, and up out of the trench over the halves
        keys.add(new Key(hit + 44, new double[] {1.6, 0.0, 3.2}, new double[] {0.0, -1.2, 0.0}));
        keys.add(new Key(hit + 60, new double[] {2.6, 1.5, 3.2}, new double[] {0.0, -1.7, 0.1}));
        keys.add(new Key(hit + 85, new double[] {1.9, 1.3, 1.9}, new double[] {0.0, -1.85, 0.1}));
        keys.add(new Key(hit + 105, new double[] {0.2, 1.9, 2.3}, new double[] {0.0, -1.85, 0.1}));
        keys.add(new Key(hit + 125, new double[] {-1.9, 1.5, 1.8}, new double[] {0.0, -1.85, 0.1}));
        double from = hit - 15;
        double to = hit + 125;
        if (args.length > 5) {
            W = Integer.parseInt(args[5]);
            H = W * 9 / 16;
        }
        Sim sim = new Sim(hover);
        while (sim.tick + 1 <= from) {
            sim.step();
        }
        int frame = 0;
        double dt = 20.0 / fps;
        for (double t = from; t <= to; t += dt) {
            while (sim.tick + 1 <= t) {
                sim.step();
            }
            if (!only.isEmpty() && !only.contains(frame)) {
                frame++;
                continue;
            }
            double[][] cam = camera(keys, t);
            BufferedImage img = frame(sim, t, cam[0], cam[1]);
            ImageIO.write(img, "png", new File(dir, String.format("f%04d.png", frame)));
            frame++;
        }
        System.out.println("hit at release tick " + String.format("%.2f", hit) + ", height " + String.format("%.1f", HEIGHT) + ", speed "
            + String.format("%.2f", SPEED) + ", " + CUTS.size() + " cells cut, " + frame + " frames");
    }

    static double[] sub(double[] a, double[] b) {
        return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    static double[] add(double[] a, double[] b) {
        return new double[] {a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    static double[] scale(double[] a, double k) {
        return new double[] {a[0] * k, a[1] * k, a[2] * k};
    }

    static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static double[] norm(double[] a) {
        double l = Math.sqrt(dot(a, a));
        return new double[] {a[0] / l, a[1] / l, a[2] / l};
    }
}
