import com.hexhaki.piece.sword.Carcass;
import com.hexhaki.piece.sword.CutPose;
import com.hexhaki.piece.sword.Finisher;
import com.hexhaki.piece.sword.Ragdoll;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 2.2.1: the finisher off-game, on the owner's skin (art/user_skin.png): the line drawing itself round the body through the stun
 * ({@code Carcass.wound}), then the body cut in two ({@code Carcass.cut}) falling as the game's own ragdoll ({@code Ragdoll}) on flat
 * ground and on a step. The game's own quads (skin faces with vanilla's UVs, the cut cells, the line), rasterised with a depth buffer,
 * nothing culled (the game draws them in entityTranslucent, which culls nothing). Blood pools are drawn as plain discs where the game
 * pools the original mod's blood (BloodStains.pool) once a half comes to rest.
 *
 * <p>javac -cp build/classes -d build/preview tools/preview/FinisherPreview.java && java -cp build/preview:build/classes FinisherPreview out.png
 */
public final class FinisherPreview {
    static final int W = 420;
    static final int H = 380;
    static final int SS = 2;
    static BufferedImage skin;
    static boolean slim;

    public static void main(String[] args) throws Exception {
        skin = ImageIO.read(new File(args.length > 1 ? args[1] : "art/user_skin.png"));
        slim = (skin.getRGB(54, 20) >>> 24) == 0 && (skin.getRGB(55, 25) >>> 24) == 0;
        String which = args.length > 2 ? args[2] : "sheet";
        if (which.equals("inside")) {
            inside(new File(args[0]));
            return;
        }
        sheet(new File(args[0]));
    }

    record Shot(String label, double[] eye, double[] look) {
    }

    static Carcass.Layout layout() {
        return slim ? Carcass.Layout.PLAYER_SLIM : Carcass.Layout.PLAYER_WIDE;
    }

    /** A stunned body's pose: standing, arms a little out (as the game holds it). */
    static CutPose stunned() {
        return new CutPose().set("rightArm", -5, 2, 0, -0.12, 0, 0.08).set("leftArm", 5, 2, 0, 0.1, 0, -0.08).set("head", 0, 0, 0, 0.12, 0.1, 0);
    }

    static void sheet(File out) throws Exception {
        CutPose pose = stunned();
        Carcass.Plane plane = Carcass.Plane.faced();
        double scale = 0.9375;
        List<BufferedImage> tiles = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Object[][] line = {{"Stunned, 0.5 s: the line starts at his chest's middle, both ways", 10.0, new double[] {1.1, 1.5, 2.3}},
            {"1 s: up over his face, down through the crotch", 20.0, new double[] {-1.6, 1.4, 1.9}},
            {"2 s: the two ends meet at his back", 40.0, new double[] {1.4, 1.6, -2.1}}};
        for (Object[] s : line) {
            Raster r = new Raster((double[])s[2], new double[] {0, 1.0, 0});
            r.ground(null);
            r.body(pose, scale);
            Carcass.wound(pose, layout(), scale, Finisher.round((Double)s[1]), Carcass.FLAT_LAYER, plane, r.glow(new double[] {0, 0, 0}, null));
            tiles.add(r.image());
            labels.add((String)s[0]);
        }
        // The fall: flat ground, then on a step.
        Object[][] fall = {{"The cut: the halves part", 4, new double[] {2.0, 1.6, 2.5}, false, new double[] {0, 0.85, 0}},
            {"Falling, each to its side", 14, new double[] {2.8, 2.0, 3.4}, false, new double[] {0, 0.5, 0}},
            {"At rest: the insides on each cut", 80, new double[] {1.2, 3.0, 3.6}, false, new double[] {0, 0.1, 0}},
            {"At rest, from his back", 80, new double[] {-1.4, 2.8, -3.6}, false, new double[] {0, 0.1, 0}},
            {"On a step: a half lands across it", 80, new double[] {3.6, 2.6, 3.0}, true, new double[] {0.4, 0.2, 0}},
            {"From above: where each half came to rest", 80, new double[] {0.01, 5.2, 0.8}, false, new double[] {0, 0, 0}}};
        for (Object[] s : fall) {
            boolean step = (Boolean)s[3];
            List<Carcass.Piece> pieces = Carcass.cut(pose, layout(), scale, Finisher.Inside.BODY, plane);
            List<Ragdoll.Body> bodies = new ArrayList<>();
            for (Carcass.Piece piece : pieces) {
                Ragdoll.Body b = new Ragdoll.Body(piece.boxes.toArray(new double[0][]), new double[] {0, 0, 0}, new double[] {1, 0, 0, 0});
                double[][] launch = Carcass.launch(piece.kind, plane);
                System.arraycopy(launch[0], 0, b.v, 0, 3);
                System.arraycopy(launch[1], 0, b.w, 0, 3);
                bodies.add(b);
            }
            Ragdoll.Terrain terrain = terrain(step);
            for (int t = 0; t < (Integer)s[1]; t++) {
                Ragdoll.tick(bodies, terrain);
            }
            Raster r = new Raster((double[])s[2], (double[])s[4]);
            r.ground(step ? new double[] {0.7, 0, -1.5, 3.0, 0.5, 1.5} : null);
            for (int i = 0; i < pieces.size(); i++) {
                Ragdoll.Body b = bodies.get(i);
                if (b.asleep) {
                    r.pool(b.pos[0], b.pos[2], 0.55, step && b.pos[0] > 0.7 && b.pos[0] < 3.0 ? 0.5 : 0.0);
                }
            }
            for (int i = 0; i < pieces.size(); i++) {
                r.piece(pieces.get(i), bodies.get(i));
            }
            tiles.add(r.image());
            labels.add((String)s[0] + ((Integer)s[1] >= 80 ? "" : "  (" + s[1] + " ticks)"));
        }
        write(out, "The finisher, upright: the line round the cut, then the body falls apart in two halves as a ragdoll", tiles, labels, 3);
    }

    /** The cut's insides close up: the faced cut of the head and trunk, and a half seen square on. */
    static void inside(File out) throws Exception {
        CutPose pose = new CutPose();
        Carcass.Plane plane = Carcass.Plane.faced();
        List<Carcass.Piece> pieces = Carcass.cut(pose, layout(), 0.9375, Finisher.Inside.BODY, plane);
        List<BufferedImage> tiles = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        double[][] eyes = {{1.05, 1.2, 0.0}, {1.3, 1.45, 1.0}, {-1.05, 1.2, 0.0}};
        String[] names = {"The right half's cut, square on", "The right half from its front", "The left half's cut"};
        for (int k = 0; k < 3; k++) {
            Raster r = new Raster(eyes[k], new double[] {0, 1.2, 0});
            r.ground(null);
            Carcass.Piece piece = pieces.get(k == 2 ? 0 : 1); // 0 the plus half (the body's left), 1 the right
            Ragdoll.Body b = new Ragdoll.Body(piece.boxes.toArray(new double[0][]), new double[] {0, 0, 0}, new double[] {1, 0, 0, 0});
            r.piece(piece, b);
            tiles.add(r.image());
            labels.add(names[k]);
        }
        write(out, "What the cut shows: skull and brain, the spine's column, heart and lungs, the guts", tiles, labels, 3);
    }

    /** Flat ground at y = 0, and (a step) a block {x0, y0, z0, x1, y1, z1} on it. */
    static Ragdoll.Terrain terrain(boolean step) {
        List<double[]> boxes = new ArrayList<>();
        boxes.add(new double[] {-60, -1, -60, 60, 0, 60});
        if (step) {
            boxes.add(new double[] {0.7, 0, -1.5, 3.0, 0.5, 1.5});
        }
        return (a, b) -> {
            double best = Double.MAX_VALUE;
            for (double[] box : boxes) {
                double t0 = 0.0;
                double t1 = 1.0;
                boolean miss = false;
                for (int k = 0; k < 3 && !miss; k++) {
                    double d = b[k] - a[k];
                    if (Math.abs(d) < 1.0E-12) {
                        if (a[k] < box[k] || a[k] > box[k + 3]) {
                            miss = true;
                        }
                        continue;
                    }
                    double ta = (box[k] - a[k]) / d;
                    double tb = (box[k + 3] - a[k]) / d;
                    t0 = Math.max(t0, Math.min(ta, tb));
                    t1 = Math.min(t1, Math.max(ta, tb));
                    if (t0 > t1) {
                        miss = true;
                    }
                }
                if (!miss) {
                    best = Math.min(best, t0);
                }
            }
            if (best == Double.MAX_VALUE) {
                return null;
            }
            return new double[] {a[0] + (b[0] - a[0]) * best, a[1] + (b[1] - a[1]) * best, a[2] + (b[2] - a[2]) * best};
        };
    }

    static void write(File out, String title, List<BufferedImage> tiles, List<String> labels, int columns) throws Exception {
        int rows = (tiles.size() + columns - 1) / columns;
        BufferedImage sheet = new BufferedImage(W * columns, 30 + rows * (H + 22), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(22, 24, 30));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.drawString(title, 10, 21);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        for (int i = 0; i < tiles.size(); i++) {
            int x = (i % columns) * W;
            int y = 30 + (i / columns) * (H + 22);
            g.drawImage(tiles.get(i), x, y, null);
            g.setColor(new Color(22, 24, 30));
            g.fillRect(x, y + H, W, 22);
            g.setColor(new Color(230, 230, 230));
            g.drawString(labels.get(i), x + 8, y + H + 15);
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
        System.out.println(out + " written");
    }

    /** A small software rasteriser: perspective, a depth buffer, the skin sampled nearest, alpha tested, simple two-light shading. */
    static final class Raster {
        final int w = W * SS;
        final int h = H * SS;
        final float[] depth = new float[w * h];
        final int[] rgb = new int[w * h];
        final double[] eye;
        final double[] fw;
        final double[] rt;
        final double[] up;
        final double focal;

        Raster(double[] eye, double[] look) {
            this.eye = eye;
            double[] f = norm(sub(look, eye));
            double[] right = norm(cross(f, new double[] {0, 1, 0}));
            if (Double.isNaN(right[0])) {
                right = new double[] {1, 0, 0};
            }
            this.fw = f;
            this.rt = right;
            this.up = cross(right, f);
            this.focal = (this.h / 2.0) / Math.tan(Math.toRadians(50) / 2);
            for (int i = 0; i < this.w * this.h; i++) {
                int y = i / this.w;
                double k = (double)y / this.h;
                this.rgb[i] = rgb(0.25 + 0.05 * k, 0.29 + 0.05 * k, 0.36 + 0.03 * k);
                this.depth[i] = Float.MAX_VALUE;
            }
        }

        static int rgb(double r, double g, double b) {
            return (int)(Math.max(0, Math.min(1, r)) * 255) << 16 | (int)(Math.max(0, Math.min(1, g)) * 255) << 8 | (int)(Math.max(0, Math.min(1, b)) * 255);
        }

        void ground(double[] step) {
            double[] n = {0, 1, 0};
            int tile = 0x6E6A62;
            for (int x = -6; x < 6; x++) {
                for (int z = -6; z < 6; z++) {
                    int c = ((x + z) & 1) == 0 ? 0x6B675F : 0x65615A;
                    quad(new double[][] {{x, 0, z}, {x + 1, 0, z}, {x + 1, 0, z + 1}, {x, 0, z + 1}}, null, n, c, false);
                }
            }
            if (step != null) {
                double[][] v = {{step[0], step[1], step[2]}, {step[3], step[1], step[2]}, {step[3], step[4], step[2]}, {step[0], step[4], step[2]},
                    {step[0], step[1], step[5]}, {step[3], step[1], step[5]}, {step[3], step[4], step[5]}, {step[0], step[4], step[5]}};
                int[][] faces = {{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}};
                double[][] ns = {{0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}, {0, 1, 0}};
                for (int f = 0; f < faces.length; f++) {
                    quad(new double[][] {v[faces[f][0]], v[faces[f][1]], v[faces[f][2]], v[faces[f][3]]}, null, ns[f], tile, false);
                }
            }
        }

        /** A pool of blood: a dark red disc on the ground at height {@code y}. */
        void pool(double x, double z, double r, double y) {
            int n = 24;
            for (int k = 0; k < n; k++) {
                double a0 = Math.PI * 2 * k / n;
                double a1 = Math.PI * 2 * (k + 1) / n;
                double[] c = {x, y + 0.004, z};
                quad(new double[][] {c, {x + Math.cos(a0) * r, y + 0.004, z + Math.sin(a0) * r}, {x + Math.cos(a1) * r, y + 0.004, z + Math.sin(a1) * r},
                    {x + Math.cos(a1) * r, y + 0.004, z + Math.sin(a1) * r}}, null, new double[] {0, 1, 0}, 0x5A0A0E, true);
            }
        }

        /** The whole body, uncut, standing (the game draws it: the same cubes and UVs). */
        void body(CutPose pose, double scale) {
            List<Carcass.Piece> whole = Carcass.cut(pose, layout(), scale, Finisher.Inside.BODY, Carcass.Plane.from(new double[] {0, 0, 1}));
            for (Carcass.Piece piece : whole) {
                for (Carcass.Face f : piece.faces) {
                    quad(f.p(), f.uv(), f.n(), 0, false);
                }
            }
        }

        Finisher.Sink glow(double[] origin, Ragdoll.Body body) {
            return (a, b, c, d, n, ca, cb, cc, cd) -> quad(new double[][] {a, b, c, d}, null, n, ca & 0xFFFFFF, true);
        }

        void piece(Carcass.Piece piece, Ragdoll.Body body) {
            for (Carcass.Face f : piece.faces) {
                double[][] p = new double[4][];
                for (int k = 0; k < 4; k++) {
                    p[k] = place(body, f.p()[k]);
                }
                quad(p, f.uv(), Ragdoll.rotate(body.q, f.n()), 0, false);
            }
            for (Carcass.Cap c : piece.caps) {
                double[][] p = new double[4][];
                for (int k = 0; k < 4; k++) {
                    p[k] = place(body, c.p()[k]);
                }
                quad(p, null, Ragdoll.rotate(body.q, c.n()), c.argb()[0] & 0xFFFFFF, false);
            }
        }

        static double[] place(Ragdoll.Body body, double[] p) {
            return body.toWorld(new double[] {p[0] - body.offset[0], p[1] - body.offset[1], p[2] - body.offset[2]});
        }

        void quad(double[][] p, double[][] uv, double[] n, int colour, boolean bright) {
            double light = 1.0;
            if (!bright) {
                // the game's entity light: two lamps (0.2, 1, -0.7) and (-0.2, 1, 0.7), 0.6 each, 0.4 ambient
                double[] l1 = norm(new double[] {0.2, 1.0, -0.7});
                double[] l2 = norm(new double[] {-0.2, 1.0, 0.7});
                light = Math.min(1.0, 0.4 + 0.6 * (Math.max(0, dot(n, l1)) + Math.max(0, dot(n, l2))));
            }
            tri(p[0], p[1], p[2], uv == null ? null : new double[][] {uv[0], uv[1], uv[2]}, colour, light);
            tri(p[0], p[2], p[3], uv == null ? null : new double[][] {uv[0], uv[2], uv[3]}, colour, light);
        }

        void tri(double[] p0, double[] p1, double[] p2, double[][] uv, int colour, double light) {
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
            int x0 = (int)Math.max(0, Math.floor(Math.min(s[0][0], Math.min(s[1][0], s[2][0]))));
            int x1 = (int)Math.min(this.w - 1, Math.ceil(Math.max(s[0][0], Math.max(s[1][0], s[2][0]))));
            int y0 = (int)Math.max(0, Math.floor(Math.min(s[0][1], Math.min(s[1][1], s[2][1]))));
            int y1 = (int)Math.min(this.h - 1, Math.ceil(Math.max(s[0][1], Math.max(s[1][1], s[2][1]))));
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
                    float z = (float)(1.0 / iz);
                    int i = y * this.w + x;
                    if (z >= this.depth[i]) {
                        continue;
                    }
                    int c = colour;
                    if (uv != null) {
                        double k0 = l0 * s[0][2] / iz;
                        double k1 = l1 * s[1][2] / iz;
                        double k2 = l2 * s[2][2] / iz;
                        double u = uv[0][0] * k0 + uv[1][0] * k1 + uv[2][0] * k2;
                        double v = uv[0][1] * k0 + uv[1][1] * k1 + uv[2][1] * k2;
                        int tx = Math.max(0, Math.min(skin.getWidth() - 1, (int)Math.floor(u * skin.getWidth() - 1.0E-6)));
                        int ty = Math.max(0, Math.min(skin.getHeight() - 1, (int)Math.floor(v * skin.getHeight() - 1.0E-6)));
                        int t = skin.getRGB(tx, ty);
                        if ((t >>> 24) < 26) {
                            continue;
                        }
                        c = t & 0xFFFFFF;
                    }
                    this.rgb[i] = rgb(((c >> 16) & 255) / 255.0 * light, ((c >> 8) & 255) / 255.0 * light, (c & 255) / 255.0 * light);
                    this.depth[i] = z;
                }
            }
        }

        double[] project(double[] p) {
            double[] q = sub(p, this.eye);
            double x = dot(q, this.rt);
            double y = dot(q, this.up);
            double z = dot(q, this.fw);
            if (z < 0.05) {
                return null;
            }
            return new double[] {this.w / 2.0 + x / z * this.focal, this.h / 2.0 - y / z * this.focal, 1.0 / z};
        }

        BufferedImage image() {
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    for (int dy = 0; dy < SS; dy++) {
                        for (int dx = 0; dx < SS; dx++) {
                            int c = this.rgb[(y * SS + dy) * this.w + x * SS + dx];
                            r += (c >> 16) & 255;
                            g += (c >> 8) & 255;
                            b += c & 255;
                        }
                    }
                    int n = SS * SS;
                    img.setRGB(x, y, (r / n) << 16 | (g / n) << 8 | b / n);
                }
            }
            return img;
        }
    }

    static double[] sub(double[] a, double[] b) {
        return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
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
