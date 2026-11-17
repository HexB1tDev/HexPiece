import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.BoltMesh;
import com.hexhaki.piece.haki.BoltPainter;
import com.hexhaki.piece.haki.ConquerorBolts;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 2.1.5: renders the 3D Haki lightning off-game to judge it (the house rule). The bolts are made by the game's own BoltPainter and
 * turned into the game's own quads by BoltMesh; this only rasterises them as Minecraft draws them: the opaque layers with a depth
 * buffer, then the glow added without depth writes. A ground and a player-sized figure give the scale and hide what is behind them.
 *
 * <p>javac -cp build/classes -d build/preview tools/preview/BoltPreview.java; java -cp build/preview:build/classes BoltPreview <dir>
 */
public final class BoltPreview {
    static int W = 300;
    static int H = 240;
    static final int SS = 2;
    static final double FOV = Math.toRadians(70);

    record Call(double[] a, double[] b, float width, int color, int duration, int delay, boolean forks) {
    }

    record Shot(String label, double[] eye, double[] look, boolean night) {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 2 && args[0].equals("gifs")) {
            gifs(new File(args[1]));
            return;
        }
        if (args.length == 2 && args[0].equals("clash")) {
            clash(new File(args[1]));
            return;
        }
        if (args.length == 2 && args[0].equals("slashgif")) {
            slashGif(new File(args[1]));
            return;
        }
        File dir = new File(args[0]);
        dir.mkdirs();
        double[] ages = {0.5, 1.2, 2.2, 4, 7, 10, 13};
        sheet(new File(dir, "bolt-closeup.png"), conqueror(0.9F, 500, true, 23L), ages, new Shot[] {
            new Shot("night, side", new double[] {0.6, 2.0, -6.5}, new double[] {0.0, 1.6, 0}, true),
            new Shot("day, side", new double[] {0.6, 2.0, -6.5}, new double[] {0.0, 1.6, 0}, false),
            new Shot("night, along", new double[] {-5.0, 2.6, -3.5}, new double[] {0.5, 1.4, 0.1}, true)});
        sheet(new File(dir, "bolt-conqueror.png"), conqueror(1.0F, 1000, true, 8L), new double[] {0.6, 1.5, 3, 5, 8, 11, 14}, new Shot[] {
            new Shot("night, third person", new double[] {-4.5, 3.4, -7.5}, new double[] {0, 1.0, 0}, true),
            new Shot("day, third person", new double[] {-4.5, 3.4, -7.5}, new double[] {0, 1.0, 0}, false),
            new Shot("night, from above", new double[] {0.5, 13, -4}, new double[] {0, 0, 0.5}, true)});
        sheet(new File(dir, "bolt-segment.png"), segmentStyle(), ages, new Shot[] {
            new Shot("night", new double[] {0.0, 1.6, -3.0}, new double[] {0.0, 1.3, 0}, true),
            new Shot("day", new double[] {0.0, 1.6, -3.0}, new double[] {0.0, 1.3, 0}, false)});
        W = 360;
        H = 330;
        sheet(new File(dir, "bolt-strike.png"), strike(), new double[] {0.4, 1.2, 2.2, 4, 7, 15, 30}, new Shot[] {
            new Shot("night", new double[] {-1.2, 1.7, -0.6}, new double[] {1.4, 0.6, 3.6}, true),
            new Shot("day", new double[] {-1.2, 1.7, -0.6}, new double[] {1.4, 0.6, 3.6}, false)});
        stormSheet(new File(dir, "bolt-storm.png"));
        drawSheet(new File(dir, "bolt-draw.png"));
        slashSheet(new File(dir, "bolt-slash.png"));
        bigSlashSheet(new File(dir, "bolt-bigslash.png"));
        W = 300;
        H = 240;
        // large single frames, to judge the tube, the outline and the glow up close
        W = 900;
        H = 560;
        sheet(new File(dir, "bolt-hero-night.png"), conqueror(0.9F, 500, true, 23L), new double[] {4}, new Shot[] {
            new Shot("night, side", new double[] {0.6, 2.0, -6.5}, new double[] {0.0, 1.6, 0}, true)});
        sheet(new File(dir, "bolt-hero-day.png"), conqueror(0.9F, 500, true, 23L), new double[] {2.2}, new Shot[] {
            new Shot("day, side", new double[] {0.6, 2.0, -6.5}, new double[] {0.0, 1.6, 0}, false)});
        sheet(new File(dir, "bolt-hero-tube.png"), conqueror(0.9F, 500, true, 23L), new double[] {3}, new Shot[] {
            new Shot("day, close", new double[] {-1.2, 2.3, -3.6}, new double[] {1.6, 1.7, 0.4}, false)});
        sheet(new File(dir, "bolt-hero-burst.png"), conqueror(1.0F, 1000, true, 8L), new double[] {3}, new Shot[] {
            new Shot("night, third person", new double[] {-3.0, 2.6, -5.0}, new double[] {0, 1.1, 0}, true)});
        W = 300;
        H = 240;
        System.out.println("wrote bolt previews to " + dir);
    }

    /**
     * The Haki draw: a katana (a dark saya, a black blade: Armament blackens it) standing up, the blade 0.25, 0.6 and 1.0 of the way
     * out with sparks at the saya's mouth, then drawn with the crackle along it, at three moments each (sets strike every two ticks).
     */
    static void drawSheet(File out) throws Exception {
        double[] outs = {0.25, 0.6, 1.0};
        double blade = 20.5 / 16.0;
        double[] times = {0.6, 1.6, 2.6};
        double x = 0.9;
        Shot[] shots = {new Shot("night", new double[] {x + 0.75, 1.75, -1.25}, new double[] {x, 1.45, 0}, true),
            new Shot("day", new double[] {x + 0.75, 1.75, -1.25}, new double[] {x, 1.45, 0}, false)};
        BufferedImage sheet = new BufferedImage(W * outs.length * times.length, (H + 18) * shots.length, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(236, 234, 228));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        for (int r = 0; r < shots.length; r++) {
            int col = 0;
            for (double share : outs) {
                for (double t : times) {
                    Raster raster = new Raster(shots[r]);
                    raster.scene();
                    // the blade's frame: guard at (0, 0.9, 0), the blade up; the saya's mouth `share` of the blade above the guard
                    double guard = 0.9;
                    double exposed = blade * share;
                    double mouth = share < 1.0 ? exposed : Double.NaN;
                    raster.box(x - 0.04, guard - 0.25, -0.015, x + 0.04, guard, 0.015, new float[] {0.1F, 0.08F, 0.07F});
                    raster.box(x - 0.03, guard, -0.006, x + 0.035, guard + exposed, 0.006, new float[] {0.05F, 0.05F, 0.06F});
                    if (share < 1.0) {
                        raster.box(x - 0.05, guard + exposed, -0.022, x + 0.05, guard + exposed + 1.35, 0.022, new float[] {0.2F, 0.06F, 0.06F});
                    }
                    long window = (long) Math.floor(t / com.hexhaki.piece.haki.DrawCrackle.WINDOW);
                    List<Bolt> bolts = new ArrayList<>();
                    for (long w = window - 1; w <= window; w++) {
                        for (Bolt bolt : com.hexhaki.piece.haki.DrawCrackle.set(7L, w, 0.0, exposed, mouth, 1.0, 0xFFFF1433)) {
                            bolts.add(new Bolt(bolt.points, bolt.widths, bolt.color, bolt.kind, bolt.delay + w * com.hexhaki.piece.haki.DrawCrackle.WINDOW,
                                bolt.duration, bolt.crawl, bolt.seed, bolt.dance));
                        }
                    }
                    double[] eye = shots[r].eye;
                    raster.boltsAt(bolts, t, new double[] {x, guard, 0});
                    g.drawImage(raster.image(), col * W, r * (H + 18) + 18, null);
                    g.setColor(new Color(30, 30, 30));
                    g.drawString(shots[r].label + "  " + Math.round(share * 100) + "% out  t" + t, col * W + 4, r * (H + 18) + 13);
                    col++;
                }
            }
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
    }

    /**
     * 2.2.4: the Haki slash as an energy wave (SlashMesh, the game's own quads, drawn as the game's two batches draw them): the full
     * charge (64 tall, flying 220) from the side, from behind its owner and three-quarter, and a small one (6 tall) up close; in the
     * manga panel's turquoise the owner showed, and a red Ryou colour.
     */
    static void bigSlashSheet(File out) throws Exception {
        int oldW = W;
        int oldH = H;
        W = 520;
        H = 360;
        Object[][] rows = {
            {64.0, new double[] {6, 40, 110, 200}, new Shot("day, from the side", new double[] {60, 26, -95}, new double[] {80, 22, 0}, false), 0xFF3FF2D2},
            {64.0, new double[] {6, 40, 110, 200}, new Shot("night, from the side", new double[] {60, 26, -95}, new double[] {80, 22, 0}, true), 0xFF3FF2D2},
            {64.0, new double[] {6, 40, 110, 200}, new Shot("day, behind the owner", new double[] {-14, 6, -6}, new double[] {40, 18, 0}, false), 0xFF3FF2D2},
            {64.0, new double[] {6, 40, 110, 200}, new Shot("night, three-quarter", new double[] {-40, 30, -90}, new double[] {60, 20, 0}, true), 0xFFFF2A3A},
            {6.0, new double[] {3, 6, 9, 12}, new Shot("day, a small one", new double[] {4, 3.2, -9}, new double[] {7, 2.4, 0}, false), 0xFF3FF2D2}};
        BufferedImage sheet = new BufferedImage(W * 4, (H + 18) * rows.length, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(236, 234, 228));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        for (int r = 0; r < rows.length; r++) {
            double height = (Double) rows[r][0];
            double[] alongs = (double[]) rows[r][1];
            Shot shot = (Shot) rows[r][2];
            int color = (Integer) rows[r][3];
            double foot = 1.6 + com.hexhaki.piece.sword.SwordRules.slashCentre(height) - height / 2.0;
            for (int c = 0; c < alongs.length; c++) {
                Raster raster = new Raster(shot);
                raster.scene();
                raster.minAlpha = 0.002F;
                com.hexhaki.piece.sword.SlashMesh.Quads glow = new com.hexhaki.piece.sword.SlashMesh.Quads();
                com.hexhaki.piece.sword.SlashMesh.Quads solid = new com.hexhaki.piece.sword.SlashMesh.Quads();
                double grown = Math.min(1.0, alongs[c] / (height * com.hexhaki.piece.sword.SwordRules.SLASH_BACK));
                com.hexhaki.piece.sword.SlashMesh.Frame frame = com.hexhaki.piece.sword.SlashMesh.Frame.of(sub(new double[] {alongs[c], foot, 0}, shot.eye),
                    1, 0, height, grown);
                com.hexhaki.piece.sword.SlashMesh.wave(solid, glow, frame, 7L + r, color, alongs[c] / 4.0, 1.0);
                // the solid batch first (opaque, back faces culled), then the glow's (sorted far to near, both faces)
                for (int q = 0; q < solid.n; q++) {
                    raster.quad(java.util.Arrays.copyOfRange(solid.v, q * 12, q * 12 + 12), java.util.Arrays.copyOfRange(solid.c, q * 4, q * 4 + 4), false);
                }
                glow.sortBackToFront();
                for (int q = 0; q < glow.n; q++) {
                    raster.quadNoCull(java.util.Arrays.copyOfRange(glow.v, q * 12, q * 12 + 12), java.util.Arrays.copyOfRange(glow.c, q * 4, q * 4 + 4));
                }
                g.drawImage(raster.image(), c * W, r * (H + 18) + 18, null);
                g.setColor(new Color(30, 30, 30));
                g.drawString(shot.label + "  " + Math.round(height) + " tall, " + Math.round(alongs[c]) + " blocks out (" + (solid.n + glow.n) + " quads)",
                    c * W + 4, r * (H + 18) + 13);
            }
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
        W = oldW;
        H = oldH;
    }

    /** The flying Haki slash (a crescent 4 blocks tall in a Ryou colour) with its crackle, as SlashRenderer bends it on. */
    static void slashSheet(File out) throws Exception {
        double[] times = {0.5, 1.5, 2.5, 3.5};
        int[] colors = {0xFF2BFF7A, 0xFFB04BFF};
        Shot[] shots = {new Shot("night", new double[] {2.5, 2.2, -5.5}, new double[] {2.5, 2.0, 0}, true),
            new Shot("day", new double[] {2.5, 2.2, -5.5}, new double[] {2.5, 2.0, 0}, false)};
        BufferedImage sheet = new BufferedImage(W * times.length * colors.length, (H + 18) * shots.length, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(236, 234, 228));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        double height = 4.0;
        double blade = 1.28;
        double[] centre = {2.5, 2.2, 0};
        double[] dir = {1, 0, 0};
        double[] side = {0, 0, 1};
        for (int r = 0; r < shots.length; r++) {
            int col = 0;
            for (int color : colors) {
                for (double t : times) {
                    Raster raster = new Raster(shots[r]);
                    raster.scene();
                    double half = height / 2.0;
                    double bow = height * 0.2;
                    // the crescent: a black core band and an additive rim of the colour, as SlashRenderer draws it
                    for (int i = 0; i < 14; i++) {
                        for (int pass = 0; pass < 2; pass++) {
                            double grow = pass == 0 ? 0.0 : 0.12;
                            double[][] q = new double[4][];
                            for (int k = 0; k < 4; k++) {
                                double u = (i + (k == 1 || k == 2 ? 1 : 0)) / 14.0;
                                int sgn = k < 2 ? 1 : -1;
                                double a = (u - 0.5) * 2.0;
                                double y = a * (half + grow);
                                double fwd = bow * (1 - a * a) + ((pass == 0 ? 0.55 : 0.85) * 0.5 * (1 - a * a * 0.85) + grow) * sgn;
                                q[k] = sub(new double[] {centre[0] + fwd, centre[1] + y, centre[2]}, shots[r].eye);
                            }
                            float[] c = pass == 0 ? new float[] {0.02F, 0.02F, 0.02F} : Raster.rgb(BoltMesh.scale(color, shots[r].night ? 0.8 : 0.4));
                            for (int face = 0; face < 2; face++) {
                                double[] a0 = q[0], a1 = face == 0 ? q[1] : q[3], a2 = q[2], a3 = face == 0 ? q[3] : q[1];
                                if (pass == 0) {
                                    raster.tri(a0, a1, a2, c, c, c, false);
                                    raster.tri(a0, a2, a3, c, c, c, false);
                                } else if (face == 0) {
                                    raster.tri(a0, a1, a2, c, c, c, true);
                                    raster.tri(a0, a2, a3, c, c, c, true);
                                }
                            }
                        }
                    }
                    long window = (long) Math.floor(t / com.hexhaki.piece.haki.DrawCrackle.WINDOW);
                    List<Bolt> bolts = new ArrayList<>();
                    double k = height / blade;
                    for (long w = Math.max(0, window - 2); w <= window; w++) {
                        for (Bolt bolt : com.hexhaki.piece.haki.DrawCrackle.set(11L, w, 0.0, blade, Double.NaN, 1.3, color)) {
                            double[] pts = new double[bolt.points.length];
                            for (int i = 0; i < bolt.count(); i++) {
                                double a = (bolt.points[i * 3 + 1] / blade - 0.5) * 2.0;
                                double ahead = bow * (1 - a * a) + 0.275 * (1 - a * a * 0.85) + height * 0.03 + Math.abs(bolt.points[i * 3]) * k;
                                double across = bolt.points[i * 3 + 2] * k;
                                pts[i * 3] = centre[0] + dir[0] * ahead + side[0] * across;
                                pts[i * 3 + 1] = centre[1] + a * half;
                                pts[i * 3 + 2] = centre[2] + dir[2] * ahead + side[2] * across;
                            }
                            float[] widths = bolt.widths.clone();
                            for (int i = 0; i < widths.length; i++) {
                                widths[i] *= (float) Math.min(3.0, Math.pow(k, 0.75));
                            }
                            bolts.add(new Bolt(pts, widths, bolt.color, bolt.kind, bolt.delay + w * com.hexhaki.piece.haki.DrawCrackle.WINDOW,
                                bolt.duration, bolt.crawl, bolt.seed, bolt.dance));
                        }
                    }
                    raster.bolts(bolts, t);
                    g.drawImage(raster.image(), col * W, r * (H + 18) + 18, null);
                    g.setColor(new Color(30, 30, 30));
                    g.drawString(shots[r].label + "  t" + t, col * W + 4, r * (H + 18) + 13);
                    col++;
                }
            }
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
    }

    /** One bolt the way the original lays its Conqueror lightning: a black body, a red core and a hot heart on the same segment. */
    static List<Call> closeUp() {
        List<Call> calls = new ArrayList<>();
        double[][] chain = {{-1.6, 1.1, 0.0}, {-0.4, 1.55, 0.2}, {0.7, 1.25, -0.1}, {1.8, 1.7, 0.1}};
        for (int i = 0; i + 1 < chain.length; i++) {
            double t = i / (double) (chain.length - 1);
            calls.add(new Call(chain[i], chain[i + 1], (float) lerp(0.176, 0.06, t), 0xEC050307, 14, i, true));
            calls.add(new Call(chain[i], chain[i + 1], (float) lerp(0.054, 0.02, t), 0xFFFF1433, 14, i, true));
            calls.add(new Call(chain[i], chain[i + 1], (float) lerp(0.014, 0.006, t), 0xFFFF7889, 14, i, false));
        }
        return calls;
    }

    /** The full-power release: the original's range lightning (as before) and the storm on top, on flat ground, with its shockwave. */
    static void stormSheet(File out) throws Exception {
        int oldW = W;
        int oldH = H;
        W = 420;
        H = 300;
        double[] ages = {1, 3, 6, 10, 16, 24};
        // 2.2.5: the owner's frames looked up at the release from below and afar, at night; the colours are two Ryou colours
        Object[][] shots = {
            {new Shot("night, from below and afar", new double[] {-30, 3, -38}, new double[] {0, 12, 0}, true), 0xFFFF2B3D},
            {new Shot("night, third person", new double[] {-5, 4.5, -9}, new double[] {0, 3.5, 6}, true), 0xFFFF2B3D},
            {new Shot("night, from below, cyan Ryou", new double[] {-30, 3, -38}, new double[] {0, 12, 0}, true), 0xFF2AE6FF},
            {new Shot("day, from afar", new double[] {-60, 12, -95}, new double[] {0, 4, 0}, false), 0xFFFF2B3D},
            {new Shot("night, high above", new double[] {0.5, 95, -40}, new double[] {0, 0, 10}, true), 0xFFFF2B3D}};
        List<Bolt> base = new ArrayList<>();
        BoltPainter.Chain chain = new BoltPainter.Chain();
        for (Call call : conqueror(1.0F, 1000, false, 8L)) {
            float start = chain.start(call.a, call.delay);
            BoltPainter.strokes(call.a, call.b, call.width, call.color, call.duration, start, call.forks, 0x4841_4B49L, base);
            chain.land(call.a, call.b, start, call.duration);
        }
        for (com.hexhaki.piece.haki.ConquerorStorm.Strand strand : com.hexhaki.piece.haki.ConquerorStorm.release(1000, 8L, true)) {
            base.addAll(com.hexhaki.piece.haki.ConquerorStorm.bolts(strand));
        }
        BoltPainter.point(base);
        double reach = com.hexhaki.piece.haki.ConquerorStorm.reach(1000);
        BufferedImage sheet = new BufferedImage(W * ages.length, (H + 18) * shots.length, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(236, 234, 228));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        int most = 0;
        for (int r = 0; r < shots.length; r++) {
            Shot shot = (Shot) shots[r][0];
            int colour = (Integer) shots[r][1];
            // as the game does at launch: every stroke in the owner's Ryou colour, the black body kept
            List<Bolt> bolts = new ArrayList<>();
            for (Bolt bolt : base) {
                bolts.add(bolt.tinted(colour & 0xFFFFFF));
            }
            List<double[]> landings = landings(bolts);
            for (int c = 0; c < ages.length; c++) {
                Raster raster = new Raster(shot);
                raster.scene();
                raster.landings = landings;
                raster.wave = reach;
                raster.burst = true;
                raster.stormColor = colour;
                int quads = raster.bolts(bolts, ages[c]);
                most = Math.max(most, quads);
                g.drawImage(raster.image(), c * W, r * (H + 18) + 18, null);
                g.setColor(new Color(30, 30, 30));
                g.drawString(shot.label + "  t" + ages[c] + "  " + quads + " quads", c * W + 4, r * (H + 18) + 13);
            }
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
        System.out.println(out.getName() + ": " + base.size() + " strokes, at most " + most + " quads a frame");
        W = oldW;
        H = oldH;
    }

    /** 2.2.5: one large frame of the full-power release, to judge its ribbons, rims and flash up close. */
    /**
     * 2.3.6: two Ryou users' blows meeting (HakiBolts.clash, ClashBurst): both colours tearing out of the point in a sphere, each its
     * own flash and shock ring, as the game lays them (the anchor a chest under the point, the bolts lifted to it). Two fighters, the
     * point between them; a sheet of moments by day and by night (java -cp build/preview:build/classes BoltPreview clash DIR).
     */
    static void clash(File dir) throws Exception {
        dir.mkdirs();
        float power = 2.2F;
        int colorA = 0xFF2244;
        int colorB = 0x22AAFF;
        List<Bolt> all = com.hexhaki.piece.haki.ClashBurst.bolts(power, colorA, colorB, 1.2, 77L);
        List<Bolt> first = new ArrayList<>();
        List<Bolt> second = new ArrayList<>();
        double chest = com.hexhaki.piece.haki.ConquerorStorm.CHEST;
        for (int i = 0; i < all.size(); i++) {
            Bolt bolt = all.get(i);
            double[] p = bolt.points.clone();
            for (int k = 1; k < p.length; k += 3) {
                p[k] += chest;
            }
            Bolt lifted = new Bolt(p, bolt.widths, bolt.color, bolt.kind, bolt.delay, bolt.duration, bolt.crawl, bolt.seed, bolt.dance);
            ((i / 2) % 2 == 0 ? first : second).add(lifted);
        }
        double flash = 1.6 + 0.7 * power;
        double[] ages = {0.5, 1.5, 3.0, 6.0, 10.0};
        for (boolean night : new boolean[] {false, true}) {
            List<BufferedImage> frames = new ArrayList<>();
            for (double age : ages) {
                Raster raster = new Raster(new Shot("clash", new double[] {-7.5, 2.6, -6.0}, new double[] {0, 1.2, 0}, night));
                raster.scene();
                // the other fighter, facing the first across the point
                float[] skin = {0.78F, 0.6F, 0.48F};
                float[] shirt = {0.62F, 0.2F, 0.16F};
                raster.box(-0.25, 0, 1.475, 0.25, 1.5, 1.725, shirt);
                raster.box(-0.25, 1.5, 1.35, 0.25, 2.0, 1.85, skin);
                double[] at = {0, 0, 0.8};
                raster.wave = 0;
                raster.burst = true;
                raster.flash = flash;
                raster.stormColor = 0xFF000000 | colorA;
                raster.boltsAt(first, age, at);
                raster.flash = flash * 0.7;
                raster.stormColor = 0xFF000000 | colorB;
                raster.boltsAt(second, age, at);
                frames.add(raster.image());
            }
            int w = frames.get(0).getWidth();
            int h = frames.get(0).getHeight();
            BufferedImage sheet = new BufferedImage(w * frames.size(), h, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = sheet.createGraphics();
            for (int i = 0; i < frames.size(); i++) {
                g.drawImage(frames.get(i), i * w, 0, null);
                g.setColor(java.awt.Color.WHITE);
                g.drawString("t" + ages[i], i * w + 6, 14);
            }
            g.dispose();
            ImageIO.write(sheet, "png", new File(dir, night ? "clash-night.png" : "clash-day.png"));
        }
    }

    static void stormHero(File out) throws Exception {
        List<Bolt> base = new ArrayList<>();
        for (com.hexhaki.piece.haki.ConquerorStorm.Strand strand : com.hexhaki.piece.haki.ConquerorStorm.release(1000, 8L, true)) {
            base.addAll(com.hexhaki.piece.haki.ConquerorStorm.bolts(strand));
        }
        BoltPainter.point(base);
        List<Bolt> bolts = new ArrayList<>();
        for (Bolt bolt : base) {
            bolts.add(bolt.tinted(0xFF2B3D));
        }
        Raster raster = new Raster(new Shot("night, from below and afar", new double[] {-30, 3, -38}, new double[] {0, 14, 0}, true));
        raster.scene();
        raster.wave = com.hexhaki.piece.haki.ConquerorStorm.reach(1000);
        raster.burst = true;
        raster.stormColor = 0xFFFF2B3D;
        raster.bolts(bolts, 7.0);
        ImageIO.write(raster.image(), "png", out);
    }

    /**
     * 2.2.5: frames for the owner's GIFs (java -cp build/preview:build/classes BoltPreview gifs &lt;dir&gt;): the Conqueror's full-power release
     * seen from far away (the original's own release lightning, the storm, its flash and shockwave), and the full-charge Haki slash flying
     * its whole range, seen from the side.
     */
    static void gifs(File dir) throws Exception {
        int oldW = W;
        int oldH = H;
        W = 640;
        H = 400;
        File release = new File(dir, "release");
        release.mkdirs();
        List<Bolt> base = new ArrayList<>();
        BoltPainter.Chain chain = new BoltPainter.Chain();
        for (Call call : conqueror(1.0F, 1000, false, 8L)) {
            float start = chain.start(call.a, call.delay);
            BoltPainter.strokes(call.a, call.b, call.width, call.color, call.duration, start, call.forks, 0x4841_4B49L, base);
            chain.land(call.a, call.b, start, call.duration);
        }
        for (com.hexhaki.piece.haki.ConquerorStorm.Strand strand : com.hexhaki.piece.haki.ConquerorStorm.release(1000, 8L, true)) {
            base.addAll(com.hexhaki.piece.haki.ConquerorStorm.bolts(strand));
        }
        BoltPainter.point(base);
        int colour = 0xFFFF2B3D;
        List<Bolt> bolts = new ArrayList<>();
        for (Bolt bolt : base) {
            bolts.add(bolt.tinted(colour & 0xFFFFFF));
        }
        List<double[]> landings = landings(bolts);
        Shot far = new Shot("night, from far away", new double[] {-95, 34, -130}, new double[] {0, 20, 0}, true);
        int frame = 0;
        for (double age = 0; age <= 32; age += 0.5) {
            Raster raster = new Raster(far);
            raster.plain = 20;
            raster.scene();
            raster.landings = landings;
            raster.wave = com.hexhaki.piece.haki.ConquerorStorm.reach(1000);
            raster.burst = true;
            raster.stormColor = colour;
            raster.bolts(bolts, age);
            ImageIO.write(raster.image(), "png", new File(release, String.format("f%03d.png", frame++)));
        }
        File slash = new File(dir, "slash");
        slash.mkdirs();
        double height = com.hexhaki.piece.sword.SwordRules.SLASH_HEIGHT_MAX;
        double range = com.hexhaki.piece.sword.SwordRules.SLASH_RANGE_MAX;
        double speed = com.hexhaki.piece.sword.SwordRules.SLASH_SPEED_MAX;
        double foot = 1.6 + com.hexhaki.piece.sword.SwordRules.slashCentre(height) - height / 2.0;
        Shot side = new Shot("day, from the side", new double[] {100, 30, -160}, new double[] {100, 26, 0}, false);
        frame = 0;
        for (double ticks = 0; ticks <= range / speed + 4; ticks += 1.0) {
            Raster raster = new Raster(side);
            raster.plain = 20;
            raster.scene();
            raster.minAlpha = 0.002F;
            double along = Math.min(range, ticks * speed);
            double fade = ticks * speed <= range ? 1.0 : Math.max(0.0, 1.0 - (ticks - range / speed) / 3.0);
            if (fade > 0.01) {
                com.hexhaki.piece.sword.SlashMesh.Quads glow = new com.hexhaki.piece.sword.SlashMesh.Quads();
                com.hexhaki.piece.sword.SlashMesh.Quads solid = new com.hexhaki.piece.sword.SlashMesh.Quads();
                com.hexhaki.piece.sword.SlashMesh.Frame f = com.hexhaki.piece.sword.SlashMesh.Frame.of(sub(new double[] {along, foot, 0}, side.eye), 1, 0,
                    height, along / (height * com.hexhaki.piece.sword.SwordRules.SLASH_BACK));
                com.hexhaki.piece.sword.SlashMesh.wave(solid, glow, f, 7L, 0xFF2AE6FF, ticks, fade);
                for (int q = 0; q < solid.n; q++) {
                    raster.quad(java.util.Arrays.copyOfRange(solid.v, q * 12, q * 12 + 12), java.util.Arrays.copyOfRange(solid.c, q * 4, q * 4 + 4), false);
                }
                glow.sortBackToFront();
                for (int q = 0; q < glow.n; q++) {
                    raster.quadNoCull(java.util.Arrays.copyOfRange(glow.v, q * 12, q * 12 + 12), java.util.Arrays.copyOfRange(glow.c, q * 4, q * 4 + 4));
                }
            }
            ImageIO.write(raster.image(), "png", new File(slash, String.format("f%03d.png", frame++)));
        }
        W = oldW;
        H = oldH;
    }

    /**
     * 2.2.6: frames of a Haki slash flowing, the camera keeping pace with it (java -cp build/preview:build/classes BoltPreview slashgif
     * &lt;dir&gt;): a 24-tall wave at night and by day, three-quarter from the side, its crackle on, in a cyan Ryou colour.
     */
    static void slashGif(File dir) throws Exception {
        int oldW = W;
        int oldH = H;
        W = 560;
        H = 420;
        dir.mkdirs();
        double height = 24.0;
        double speed = 2.4;
        double foot = 1.6 + com.hexhaki.piece.sword.SwordRules.slashCentre(height) - height / 2.0;
        int frame = 0;
        for (boolean night : new boolean[] {true, false}) {
            for (double ticks = 4; ticks <= 16; ticks += 0.5) {
                double along = 10 + ticks * speed;
                Shot shot = new Shot(night ? "night" : "day", new double[] {along - 14, 12, -34}, new double[] {along - 2, 11, 0}, night);
                Raster raster = new Raster(shot);
                raster.plain = 12;
                raster.scene();
                raster.minAlpha = 0.002F;
                com.hexhaki.piece.sword.SlashMesh.Quads glow = new com.hexhaki.piece.sword.SlashMesh.Quads();
                com.hexhaki.piece.sword.SlashMesh.Quads solid = new com.hexhaki.piece.sword.SlashMesh.Quads();
                com.hexhaki.piece.sword.SlashMesh.Frame f = com.hexhaki.piece.sword.SlashMesh.Frame.of(sub(new double[] {along, foot, 0}, shot.eye), 1, 0,
                    height, along / (height * com.hexhaki.piece.sword.SwordRules.SLASH_BACK));
                com.hexhaki.piece.sword.SlashMesh.wave(solid, glow, f, 7L, 0xFF2AE6FF, ticks, 1.0);
                for (int q = 0; q < solid.n; q++) {
                    raster.quad(java.util.Arrays.copyOfRange(solid.v, q * 12, q * 12 + 12), java.util.Arrays.copyOfRange(solid.c, q * 4, q * 4 + 4), false);
                }
                // the crackle up its leading edge, as SlashRenderer lays it
                List<Bolt> bolts = new ArrayList<>();
                List<Double> ages = new ArrayList<>();
                double k = height / 1.28;
                long window = (long) Math.floor(ticks / com.hexhaki.piece.haki.DrawCrackle.WINDOW);
                for (long w = Math.max(0, window - 2); w <= window; w++) {
                    for (Bolt bolt : com.hexhaki.piece.haki.DrawCrackle.set(7L * 0x9E3779B97F4A7C15L, w, 0.0, 1.28, Double.NaN, 1.3, 0xFF2AE6FF)) {
                        double age = ticks - w * com.hexhaki.piece.haki.DrawCrackle.WINDOW - bolt.delay;
                        if (age < 0 || age > bolt.duration) {
                            continue;
                        }
                        double[] pts = new double[bolt.points.length];
                        for (int i = 0; i < bolt.count(); i++) {
                            double v = Math.max(0.0, Math.min(0.97, bolt.points[i * 3 + 1] / 1.28 * 0.97));
                            double[] p = com.hexhaki.piece.sword.SlashMesh.edge(f, v, height * 0.01 + Math.abs(bolt.points[i * 3]) * k, bolt.points[i * 3 + 2] * k);
                            pts[i * 3] = p[0];
                            pts[i * 3 + 1] = p[1];
                            pts[i * 3 + 2] = p[2];
                        }
                        float[] widths = bolt.widths.clone();
                        for (int i = 0; i < widths.length; i++) {
                            widths[i] *= (float) Math.min(3.0, Math.pow(k, 0.75));
                        }
                        bolts.add(new Bolt(pts, widths, bolt.color, bolt.kind, 0, bolt.duration, bolt.crawl, bolt.seed, bolt.dance));
                        ages.add(age);
                    }
                }
                raster.light();
                double pixel = 2.0 * Math.tan(FOV / 2) / H;
                List<double[]> gv = new ArrayList<>();
                List<int[]> gc = new ArrayList<>();
                for (int i = 0; i < bolts.size(); i++) {
                    BoltMesh.build(bolts.get(i), BoltMesh.Frame.at(0, 0, 0), ages.get(i), pixel, night ? 1.0 : 0.5, (g, xyz, argb) -> {
                        if (g) {
                            gv.add(xyz);
                            gc.add(argb);
                        } else {
                            raster.quad(xyz, argb, false);
                        }
                    });
                }
                for (int i = 0; i < gv.size(); i++) {
                    raster.glowQuad(gv.get(i), gc.get(i));
                }
                glow.sortBackToFront();
                for (int q = 0; q < glow.n; q++) {
                    raster.quadNoCull(java.util.Arrays.copyOfRange(glow.v, q * 12, q * 12 + 12), java.util.Arrays.copyOfRange(glow.c, q * 4, q * 4 + 4));
                }
                ImageIO.write(raster.image(), "png", new File(dir, String.format("f%03d.png", frame++)));
            }
        }
        W = oldW;
        H = oldH;
    }

    /** One bolt coming down to strike the ground in front of the camera (two pieces, the original's three strokes each). */
    static List<Call> strike() {
        List<Call> calls = new ArrayList<>();
        double[][] chain = {{0.2, 2.6, 3.2}, {1.0, 1.3, 3.9}, {1.6, 0.02, 4.2}};
        for (int i = 0; i + 1 < chain.length; i++) {
            double t = i / (double) (chain.length - 1);
            calls.add(new Call(chain[i], chain[i + 1], (float) lerp(0.176, 0.1, t), 0xEC050307, 14, 0, true));
            calls.add(new Call(chain[i], chain[i + 1], (float) lerp(0.054, 0.035, t), 0xFFFF1433, 14, 0, true));
            calls.add(new Call(chain[i], chain[i + 1], (float) lerp(0.014, 0.01, t), 0xFFFF7889, 12, 0, false));
        }
        return calls;
    }

    /** The original's addHakiLightningSegment layering: a faint crimson aura, the black sheath and the red filament. */
    static List<Call> segmentStyle() {
        List<Call> calls = new ArrayList<>();
        double[][] chain = {{-1.4, 0.9, 0.0}, {-0.2, 1.6, 0.0}, {1.2, 1.2, 0.1}};
        for (int i = 0; i + 1 < chain.length; i++) {
            calls.add(new Call(chain[i], chain[i + 1], 0.13F, 0x582A0008, 15, i, false));
            calls.add(new Call(chain[i], chain[i + 1], 0.072F, 0xF0060008, 14, i, true));
            calls.add(new Call(chain[i], chain[i + 1], 0.022F, 0xFFFF1E10, 12, i, true));
        }
        return calls;
    }

    /** The Conqueror's charge bolts (the shapes the server stuns with), laid as the original's range lightning lays them. */
    static List<Call> conqueror(float charge, int mastery, boolean dome, long seed) {
        double radius = 5.0 + (10.0 + 40.0 * mastery / 1000.0 - 5.0) * Math.max(0, Math.min(1, (charge - 0.04) / 0.96));
        List<double[]> bolts = ConquerorBolts.generate(charge, mastery, seed, dome, radius);
        int mains = ConquerorBolts.count(charge, mastery, dome);
        List<Call> calls = new ArrayList<>();
        for (int b = 0; b < bolts.size(); b++) {
            double[] bolt = bolts.get(b);
            int n = bolt.length / 3 - 1;
            boolean main = b < mains;
            for (int i = 0; i < n; i++) {
                double t = i / (double) Math.max(1, n - 1);
                double[] a = {bolt[i * 3], bolt[i * 3 + 1], bolt[i * 3 + 2]};
                double[] c = {bolt[i * 3 + 3], bolt[i * 3 + 4], bolt[i * 3 + 5]};
                int delay = (int) (t * 2) + (main ? 0 : 2);
                float k = main ? 1.0F : 0.6F;
                calls.add(new Call(a, c, (float) lerp(0.176, 0.03, t) * k, 0xEC050307, 14, delay, main));
                calls.add(new Call(a, c, (float) lerp(0.054, 0.015, t) * k, 0xFFFF1433, 14, delay, main));
                calls.add(new Call(a, c, (float) lerp(0.014, 0.005, t) * k, 0xFFFF7889, 12, delay, false));
            }
        }
        return calls;
    }

    /** As HakiBolts finds them: free ends of coloured strokes that reach the ground (here the ground is y 0), one per half block. */
    static List<double[]> landings(List<Bolt> bolts) {
        java.util.Set<Long> roots = new java.util.HashSet<>();
        for (Bolt bolt : bolts) {
            roots.add(Math.round(bolt.points[0] * 32) * 31L + Math.round(bolt.points[1] * 32) * 131L + Math.round(bolt.points[2] * 32) * 1313L);
        }
        List<double[]> hits = new ArrayList<>();
        java.util.Set<Long> cells = new java.util.HashSet<>();
        for (Bolt bolt : bolts) {
            int last = bolt.points.length - 3;
            double x = bolt.points[last];
            double y = bolt.points[last + 1];
            double z = bolt.points[last + 2];
            long key = Math.round(x * 32) * 31L + Math.round(y * 32) * 131L + Math.round(z * 32) * 1313L;
            if (bolt.kind != Bolt.CORE && bolt.kind != Bolt.EDGE || roots.contains(key) || y > 0.35 || hits.size() >= 16) {
                continue;
            }
            if (cells.add(Math.round(x * 2) * 7919L + Math.round(z * 2))) {
                hits.add(new double[] {x, y, z, bolt.delay + bolt.crawl});
            }
        }
        return hits;
    }

    static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    static void sheet(File out, List<Call> calls, double[] ages, Shot[] shots) throws Exception {
        List<Bolt> bolts = new ArrayList<>();
        BoltPainter.Chain chain = new BoltPainter.Chain();
        for (Call call : calls) {
            float start = chain.start(call.a, call.delay);
            BoltPainter.strokes(call.a, call.b, call.width, call.color, call.duration, start, call.forks, 0x4841_4B49L, bolts);
            chain.land(call.a, call.b, start, call.duration);
        }
        // as the game does at launch: lightning off the body (2.2.6), every stroke in the owner's Ryou colour (here the original's red),
        // free ends pointed
        bolts.replaceAll(bolt -> new Bolt(com.hexhaki.piece.haki.ConquerorBolts.offBody(bolt.points), bolt.widths, bolt.color, bolt.kind,
            bolt.delay, bolt.duration, bolt.crawl, bolt.seed, bolt.dance));
        bolts.replaceAll(bolt -> bolt.tinted(0xFF1433));
        BoltPainter.point(bolts);
        List<double[]> landings = landings(bolts);
        BufferedImage sheet = new BufferedImage(W * ages.length, (H + 18) * shots.length, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(236, 234, 228));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        int quads = 0;
        for (int r = 0; r < shots.length; r++) {
            for (int c = 0; c < ages.length; c++) {
                Raster raster = new Raster(shots[r]);
                raster.scene();
                raster.landings = landings;
                quads = raster.bolts(bolts, ages[c]);
                g.drawImage(raster.image(), c * W, r * (H + 18) + 18, null);
                g.setColor(new Color(30, 30, 30));
                g.drawString(shots[r].label + "  t" + ages[c] + (c == 0 ? "" : ""), c * W + 4, r * (H + 18) + 13);
            }
        }
        g.dispose();
        ImageIO.write(sheet, "png", out);
        System.out.println(out.getName() + ": " + bolts.size() + " strokes, " + quads + " quads at the last frame");
    }

    /** A tiny software rasteriser: perspective, a depth buffer, opaque and additive triangles. */
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
        final boolean night;
        float[] alpha;
        float[] triAlpha;
        /** Below this a blended pixel is skipped (2.2.4: the slash's faint halo is drawn). */
        float minAlpha = 0.1F;
        /** Big tiles each way from the middle (16 blocks each). */
        int plain = 8;
        List<double[]> landings;
        double wave;
        /** 2.2.5: the centre's flash, and the colour the storm was tinted to (the shockwave, flash and strikes take it). */
        boolean burst;
        /** 2.3.6: the flash's radius when not the storm's (a clash's). */
        double flash;
        int stormColor = 0xFFFF1433;

        Raster(Shot shot) {
            this.eye = shot.eye;
            this.night = shot.night;
            double[] f = norm(sub(shot.look, shot.eye));
            double[] right = norm(cross(f, new double[] {0, 1, 0}));
            this.fw = f;
            this.rt = right;
            this.up = cross(right, f);
            this.focal = (this.h / 2.0) / Math.tan(FOV / 2);
            float[] sky = this.night ? new float[] {0.07F, 0.08F, 0.13F} : new float[] {0.62F, 0.76F, 0.95F};
            for (int i = 0; i < this.w * this.h; i++) {
                int y = i / this.w;
                float k = 0.75F + 0.25F * y / this.h;
                this.r[i] = sky[0] * k;
                this.g[i] = sky[1] * k;
                this.b[i] = sky[2] * k;
                this.depth[i] = Float.MAX_VALUE;
            }
        }

        /** The ground (one-block tiles) and a player-sized figure at the origin, facing +z. */
        void scene() {
            float[] grassA = this.night ? new float[] {0.11F, 0.16F, 0.10F} : new float[] {0.42F, 0.62F, 0.30F};
            float[] grassB = this.night ? new float[] {0.10F, 0.14F, 0.09F} : new float[] {0.38F, 0.57F, 0.27F};
            for (int x = -16; x < 16; x++) {
                for (int z = -16; z < 16; z++) {
                    float[] c = ((x + z) & 1) == 0 ? grassA : grassB;
                    box(x, -0.2, z, x + 1, 0, z + 1, c);
                }
            }
            // beyond, the plain (big tiles; 2.2.5: out to {@link #plain} tiles for the long shots)
            for (int x = -this.plain; x < this.plain; x++) {
                for (int z = -this.plain; z < this.plain; z++) {
                    if (x >= -1 && x < 1 && z >= -1 && z < 1) {
                        continue;
                    }
                    float[] c = ((x + z) & 1) == 0 ? grassA : grassB;
                    box(x * 16, -0.25, z * 16, x * 16 + 16, -0.01, z * 16 + 16, c);
                }
            }
            float[] skin = {0.78F, 0.6F, 0.48F};
            float[] shirt = {0.16F, 0.32F, 0.62F};
            float[] pants = {0.18F, 0.2F, 0.42F};
            float k = this.night ? 0.45F : 1.0F;
            box(-0.25, 0, -0.125, 0, 0.75, 0.125, dim(pants, k));
            box(0, 0, -0.125, 0.25, 0.75, 0.125, dim(pants, k));
            box(-0.25, 0.75, -0.125, 0.25, 1.5, 0.125, dim(shirt, k));
            box(-0.5, 0.75, -0.125, -0.25, 1.5, 0.125, dim(skin, k));
            box(0.25, 0.75, -0.125, 0.5, 1.5, 0.125, dim(skin, k));
            box(-0.25, 1.5, -0.25, 0.25, 2.0, 0.25, dim(skin, k));
        }

        static float[] dim(float[] c, float k) {
            return new float[] {c[0] * k, c[1] * k, c[2] * k};
        }

        void box(double x0, double y0, double z0, double x1, double y1, double z1, float[] c) {
            double[][] v = {{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
            int[][] faces = {{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}};
            float[] shade = {0.8F, 0.8F, 0.6F, 0.6F, 1.0F, 0.5F};
            for (int f = 0; f < 6; f++) {
                float[] fc = {c[0] * shade[f], c[1] * shade[f], c[2] * shade[f]};
                double[][] q = new double[4][];
                for (int k = 0; k < 4; k++) {
                    q[k] = sub(v[faces[f][k]], this.eye);
                }
                tri(q[0], q[1], q[2], fc, fc, fc, false);
                tri(q[0], q[2], q[3], fc, fc, fc, false);
            }
        }

        int bolts(List<Bolt> bolts, double age) {
            return boltsAt(bolts, age, new double[] {0, 0, 0});
        }

        /** 2.2.6: the key light as the game sets it: BoltMesh.VIEW_LIGHT turned from the view into the camera-space frame. */
        void light() {
            double[] l = BoltMesh.VIEW_LIGHT;
            BoltMesh.light(this.rt[0] * l[0] + this.up[0] * l[1] - this.fw[0] * l[2], this.rt[1] * l[0] + this.up[1] * l[1] - this.fw[1] * l[2],
                this.rt[2] * l[0] + this.up[2] * l[1] - this.fw[2] * l[2]);
        }

        int boltsAt(List<Bolt> bolts, double age, double[] origin) {
            light();
            double pixel = 2.0 * Math.tan(FOV / 2) / H;
            List<double[]> glowV = new ArrayList<>();
            List<int[]> glowC = new ArrayList<>();
            int[] quads = {0};
            for (Bolt bolt : bolts) {
                BoltMesh.Frame frame = BoltMesh.Frame.at(origin[0] - this.eye[0], origin[1] - this.eye[1], origin[2] - this.eye[2]);
                BoltMesh.build(bolt, frame, age - bolt.delay, pixel, this.night ? 1.0 : 0.5, (glow, xyz, argb) -> {
                    quads[0]++;
                    if (glow) {
                        glowV.add(xyz);
                        glowC.add(argb);
                    } else {
                        quad(xyz, argb, false);
                    }
                });
            }
            if (this.burst) {
                BoltMesh.burst(origin[0] - this.eye[0], origin[1] + com.hexhaki.piece.haki.ConquerorStorm.CHEST - this.eye[1], origin[2] - this.eye[2], age,
                    this.flash > 0 ? this.flash : 10.0 + this.wave * 0.1,
                    this.stormColor, this.night ? 1.0 : 0.5, (glow, xyz, argb) -> {
                        quads[0]++;
                        glowV.add(xyz);
                        glowC.add(argb);
                    });
            }
            if (this.wave > 0) {
                BoltMesh.ring(origin[0] - this.eye[0], origin[1] - this.eye[1], origin[2] - this.eye[2], age, this.wave, this.stormColor,
                    this.night ? 1.0 : 0.5, (glow, xyz, argb) -> {
                    quads[0]++;
                    if (glow) {
                        glowV.add(xyz);
                        glowC.add(argb);
                    } else {
                        quad(xyz, argb, false);
                    }
                });
            }
            if (this.landings != null) {
                for (double[] hit : this.landings) {
                    BoltMesh.landing(hit[0] - this.eye[0], 0.0 - this.eye[1], hit[2] - this.eye[2], age - hit[3], this.stormColor, 1.0,
                        this.night ? 1.0 : 0.5, (glow, xyz, argb) -> {
                            if (glow) {
                                glowV.add(xyz);
                                glowC.add(argb);
                            } else {
                                quad(xyz, argb, false);
                            }
                        });
                }
            }
            for (int i = 0; i < glowV.size(); i++) {
                glowQuad(glowV.get(i), glowC.get(i));
            }
            return quads[0];
        }

        /**
         * 2.2.6: a glow quad as HakiBolts.write hands it to the game (2.1.9: alpha blended, not added): every corner the hue of the quad's
         * brightest at full brightness, as opaque as it was bright (times 0.9); depth tested, not written here (glow over glow blends).
         */
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
            double[] a = {v[0], v[1], v[2]};
            double[] b2 = {v[3], v[4], v[5]};
            double[] c2 = {v[6], v[7], v[8]};
            double[] d = {v[9], v[10], v[11]};
            float old = this.minAlpha;
            this.minAlpha = 0.004F;
            this.noDepthWrite = true;
            this.triAlpha = new float[] {al[0], al[1], al[2]};
            tri(a, b2, c2, col, col, col, false);
            this.triAlpha = new float[] {al[0], al[2], al[3]};
            tri(a, c2, d, col, col, col, false);
            this.triAlpha = null;
            this.noDepthWrite = false;
            this.minAlpha = old;
        }

        boolean noDepthWrite;

        /** 2.2.4: a quad as the game's debug-quads batch draws it: alpha blended, depth tested and written, both faces. */
        void quadNoCull(double[] v, int[] c) {
            float[] al = {((c[0] >>> 24) & 255) / 255F, ((c[1] >>> 24) & 255) / 255F, ((c[2] >>> 24) & 255) / 255F, ((c[3] >>> 24) & 255) / 255F};
            double[] a = {v[0], v[1], v[2]};
            double[] b2 = {v[3], v[4], v[5]};
            double[] c2 = {v[6], v[7], v[8]};
            double[] d = {v[9], v[10], v[11]};
            this.triAlpha = new float[] {al[0], al[1], al[2]};
            tri(a, b2, c2, rgb(c[0]), rgb(c[1]), rgb(c[2]), false);
            this.triAlpha = new float[] {al[0], al[2], al[3]};
            tri(a, c2, d, rgb(c[0]), rgb(c[2]), rgb(c[3]), false);
            this.triAlpha = null;
        }

        void quad(double[] v, int[] c, boolean add) {
            this.alpha = add ? null : new float[] {((c[0] >>> 24) & 255) / 255F, ((c[1] >>> 24) & 255) / 255F, ((c[2] >>> 24) & 255) / 255F,
                ((c[3] >>> 24) & 255) / 255F};
            double[] a = {v[0], v[1], v[2]};
            double[] b2 = {v[3], v[4], v[5]};
            double[] c2 = {v[6], v[7], v[8]};
            double[] d = {v[9], v[10], v[11]};
            // the game's quads are front faces seen from the eye: check it, a back face would be culled in game
            float[] ca = rgb(c[0]);
            float[] cb = rgb(c[1]);
            float[] cc = rgb(c[2]);
            float[] cd = rgb(c[3]);
            // the game's batches cull back faces: so does the preview, so a hole would show
            if (front(a, b2, c2)) {
                this.triAlpha = this.alpha == null ? null : new float[] {this.alpha[0], this.alpha[1], this.alpha[2]};
                tri(a, b2, c2, ca, cb, cc, add);
            }
            if (front(a, c2, d)) {
                this.triAlpha = this.alpha == null ? null : new float[] {this.alpha[0], this.alpha[2], this.alpha[3]};
                tri(a, c2, d, ca, cc, cd, add);
            }
            this.alpha = null;
            this.triAlpha = null;
        }

        static boolean front(double[] a, double[] b, double[] c) {
            double[] n = cross(sub(b, a), sub(c, a));
            return -dot(n, a) >= -1e-12;
        }

        static float[] rgb(int argb) {
            return new float[] {((argb >>> 16) & 255) / 255F, ((argb >>> 8) & 255) / 255F, (argb & 255) / 255F};
        }

        /** Camera-relative world points -> screen, and the triangle filled. */
        void tri(double[] p0, double[] p1, double[] p2, float[] c0, float[] c1, float[] c2, boolean add) {
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
                    if (add) {
                        this.r[i] += cr;
                        this.g[i] += cg;
                        this.b[i] += cb;
                    } else if (this.triAlpha != null && (this.triAlpha[0] < 1F || this.triAlpha[1] < 1F || this.triAlpha[2] < 1F)) {
                        float a = (float) (this.triAlpha[0] * k0 + this.triAlpha[1] * k1 + this.triAlpha[2] * k2);
                        if (a < this.minAlpha) {
                            continue;
                        }
                        this.r[i] = cr * a + this.r[i] * (1 - a);
                        this.g[i] = cg * a + this.g[i] * (1 - a);
                        this.b[i] = cb * a + this.b[i] * (1 - a);
                        if (!this.noDepthWrite) {
                            this.depth[i] = z;
                        }
                    } else {
                        this.r[i] = cr;
                        this.g[i] = cg;
                        this.b[i] = cb;
                        this.depth[i] = z;
                    }
                }
            }
        }

        double[] project(double[] p) {
            double x = dot(p, this.rt);
            double y = dot(p, this.up);
            double z = dot(p, this.fw);
            if (z < 0.05) {
                return null;
            }
            return new double[] {this.w / 2.0 + x / z * this.focal, this.h / 2.0 - y / z * this.focal, 1.0 / z};
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
