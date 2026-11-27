import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.BoltMesh;
import com.hexhaki.piece.haki.ConquerorSparks;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 2.9.1: the Conqueror's lightning off its user (haki/ConquerorSparks, built by haki/BoltMesh as client/haki/HakiBolts builds it) beside
 * a player figure: a burst off the body at mastery 0 (2 arcs) and 1000 (6), a burst off a drawn blade, each over its life.
 * Usage: SparkPreview out.png
 */
public final class SparkPreview {
    record Quad(double[] xyz, int[] argb, boolean glow, double far) {
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 1 && args[1].equals("itsu")) {
            itsu(new File(args[0]));
            return;
        }
        String[] rows = {"body 2", "body 6", "blade", "gura burst", "blast 9", "ultimate thick", "release"};
        double[] ages = {1, 3, 6, 10, 16, 22};
        int cw = 300;
        int ch = 300;
        BufferedImage sheet = new BufferedImage(cw * ages.length + 4, (ch + 16) * rows.length + 4, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(0x202020));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        DragonPreview.texture = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        for (int r = 0; r < rows.length; r++) {
            List<Bolt> bolts = switch (r) {
                case 0 -> ConquerorSparks.body(2, 1.8, 0.6, 11L);
                case 1 -> ConquerorSparks.body(6, 1.8, 0.6, 12L);
                case 2 -> ConquerorSparks.blade(new double[] {-0.35, 1.15, 0.45}, new double[] {-0.35, 1.9, 2.5}, 11, 0.0, 13L);
                case 6 -> {
                    // 3.1.8: the Conqueror's release itself (mastery 500), its points from the feet as heights over flat ground, 8 ahead
                    List<Bolt> moved = new ArrayList<>();
                    for (com.hexhaki.piece.haki.ConquerorStorm.Strand strand : com.hexhaki.piece.haki.ConquerorStorm.release(500, 17L, false)) {
                        for (Bolt bolt : com.hexhaki.piece.haki.ConquerorStorm.bolts(strand)) {
                            double[] q = bolt.points.clone();
                            for (int i = 0; i < q.length; i += 3) {
                                q[i + 2] += 8.0;
                            }
                            moved.add(new Bolt(q, bolt.widths, bolt.color, bolt.kind, bolt.delay, bolt.duration, bolt.crawl, bolt.seed, bolt.dance));
                        }
                    }
                    yield moved;
                }
                case 5 -> {
                    // 3.1.6: the Gura ultimate's thick lightning out of the breaking ground (GuraUltimate.Front: 5 to 7 blocks, 9 arcs, x 2.2)
                    List<Bolt> moved = new ArrayList<>();
                    for (Bolt bolt : com.hexhaki.piece.haki.ConquerorStorm.blast(6.0, 9, -0.4, 16L, 1.35)) {
                        double[] q = bolt.points.clone();
                        for (int i = 0; i < q.length; i += 3) {
                            q[i + 1] += 0.4;
                            q[i + 2] += 8.0;
                        }
                        moved.add(new Bolt(q, bolt.widths, bolt.color, bolt.kind, bolt.delay, bolt.duration, bolt.crawl, bolt.seed, bolt.dance));
                    }
                    yield moved;
                }
                case 4 -> {
                    // 3.1.5: ConquerorAura.exploded out of a radius 9 blast (the Light fruit's full beam): 0.8 of it, 11 arcs, 8 ahead
                    List<Bolt> moved = new ArrayList<>();
                    for (Bolt bolt : com.hexhaki.piece.haki.ConquerorStorm.blast(7.2, 11, -0.5, 15L, 1.0)) {
                        double[] q = bolt.points.clone();
                        for (int i = 0; i < q.length; i += 3) {
                            q[i + 1] += 0.5;
                            q[i + 2] += 8.0;
                        }
                        moved.add(new Bolt(q, bolt.widths, bolt.color, bolt.kind, bolt.delay, bolt.duration, bolt.crawl, bolt.seed, bolt.dance));
                    }
                    yield moved;
                }
                default -> {
                    // 3.1.2: the Conqueror's lightning mixed into a Gura quake (the fist's crack 1.5 ahead, 1.2 over the ground)
                    List<Bolt> moved = new ArrayList<>();
                    for (Bolt bolt : com.hexhaki.piece.haki.ConquerorStorm.blast(4.0, 10, -1.2, 14L, 1.0)) {
                        double[] q = bolt.points.clone();
                        for (int i = 0; i < q.length; i += 3) {
                            q[i + 1] += 1.2;
                            q[i + 2] += 1.5;
                        }
                        moved.add(new Bolt(q, bolt.widths, bolt.color, bolt.kind, bolt.delay, bolt.duration, bolt.crawl, bolt.seed, bolt.dance));
                    }
                    yield moved;
                }
            };
            g.setColor(Color.WHITE);
            g.drawString(rows[r], 4, r * (ch + 16) + 13);
            for (int c = 0; c < ages.length; c++) {
                far = r >= 4;
                g.drawImage(render(bolts, ages[c], cw * 2, ch * 2), c * cw + 2, r * (ch + 16) + 16, cw - 2, ch - 2, null);
                g.setColor(Color.BLACK);
                g.drawString(String.format("t%.1f", ages[c]), c * cw + 6, r * (ch + 16) + 30);
            }
        }
        ImageIO.write(sheet, "png", new File(args[0]));
    }

    /** 3.1.5: the blast row seen from further back, over the caster's shoulder. */
    static boolean far;

    /**
     * 3.6.0: a glow quad's colours as the game draws them (client/haki/HakiBolts.write): the hue of its brightest corner at full
     * brightness, each corner as opaque as it is bright (x 0.9), so a glow fades out to nothing. (Drawn opaque before, the preview showed
     * every glow band as solid dark red fading to black: thick black outlines the game never draws.)
     */
    static int[] glowed(int[] argb) {
        int top = 0;
        int hue = 0xFFFFFF;
        for (int c : argb) {
            int bright = Math.max((c >>> 16) & 255, Math.max((c >>> 8) & 255, c & 255));
            if (bright > top) {
                top = bright;
                hue = c;
            }
        }
        double scale = top > 0 ? 255.0 / top : 0.0;
        int rgb = (int) Math.min(255, ((hue >>> 16) & 255) * scale) << 16 | (int) Math.min(255, ((hue >>> 8) & 255) * scale) << 8
            | (int) Math.min(255, (hue & 255) * scale);
        int[] out = new int[argb.length];
        for (int k = 0; k < argb.length; k++) {
            int c = argb[k];
            int alpha = (int) Math.round(Math.max((c >>> 16) & 255, Math.max((c >>> 8) & 255, c & 255)) * 0.9);
            out[k] = alpha << 24 | rgb;
        }
        return out;
    }

    /** 3.9.8: a camera of its own (eye, target) when set: the Itsumade's storm seen whole. */
    static double[] eyeAt;
    static double[] targetAt;

    /**
     * 3.9.8: Thy Death (itsumade/ItsuArt.skyStroke) as the game lets it go: five bolts out of the clouds 34 blocks up onto the ground round
     * the figure, struck 3 ticks apart, their rims in the Itsumade's Haki (server/RyouServer.ITSU_COLOR); and Enormous Presence's bolts
     * off the body (ConquerorSparks.wifi, 0.8) onto foes 3 to 8 blocks off.
     */
    static void itsu(File out) throws Exception {
        double[][] spots = {{-6, 0, 12}, {5, 0, 18}, {-14, 0, 22}, {10, 0, 9}, {1, 0, 26}};
        List<Bolt> storm = new ArrayList<>();
        for (int i = 0; i < spots.length; i++) {
            double sx = spots[i][0] + (i % 2 == 0 ? 2.0 : -2.5);
            double sz = spots[i][2] + (i % 3 == 0 ? -1.5 : 2.0);
            for (Bolt b : com.hexhaki.piece.itsumade.ItsuArt.skyStroke(spots[i][0] - sx, -34.0, spots[i][2] - sz, 100L + i)) {
                double[] q = b.points.clone();
                for (int k = 0; k < q.length; k += 3) {
                    q[k] += sx;
                    q[k + 1] += 34.0;
                    q[k + 2] += sz;
                }
                storm.add(new Bolt(q, b.widths, 0x2B3FD8, b.kind, b.delay + i * 3.0F, b.duration, b.crawl, b.seed, b.dance));
            }
        }
        List<Bolt> presence = new ArrayList<>();
        double[][] foes = {{3, 1.0, 4}, {-4, 0.9, 6}, {1, 1.2, 8}};
        for (int i = 0; i < foes.length; i++) {
            double[] from = {foes[i][0] > 0 ? 0.4 : -0.4, 1.3, 0.1};
            for (Bolt b : ConquerorSparks.wifi(new double[3], new double[] {foes[i][0] - from[0], foes[i][1] - from[1], foes[i][2] - from[2]}, 0.8,
                3, 40L + i)) {
                double[] q = b.points.clone();
                for (int k = 0; k < q.length; k += 3) {
                    q[k] += from[0];
                    q[k + 1] += from[1];
                    q[k + 2] += from[2];
                }
                presence.add(new Bolt(q, b.widths, 0x2B3FD8, b.kind, b.delay + i * 4.0F, b.duration, b.crawl, b.seed, b.dance));
            }
        }
        double[] ages = {1, 2.5, 4, 7, 10, 14};
        int cw = 300;
        int ch = 300;
        BufferedImage sheet = new BufferedImage(cw * ages.length + 4, (ch + 16) * 2 + 4, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(0x202020));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        DragonPreview.texture = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        for (int r = 0; r < 2; r++) {
            eyeAt = r == 0 ? new double[] {14, 9, -22} : new double[] {4.0, 3.0, -4.5};
            targetAt = r == 0 ? new double[] {0, 14, 14} : new double[] {0, 1.2, 4.0};
            g.setColor(Color.WHITE);
            g.drawString(r == 0 ? "Thy Death" : "Enormous Presence", 4, r * (ch + 16) + 13);
            for (int c = 0; c < ages.length; c++) {
                g.drawImage(render(r == 0 ? storm : presence, ages[c] + (r == 0 ? ages[c] : 0), cw * 2, ch * 2), c * cw + 2, r * (ch + 16) + 16,
                    cw - 2, ch - 2, null);
                g.setColor(Color.BLACK);
                g.drawString(String.format("t%.1f", ages[c] * (r == 0 ? 2 : 1)), c * cw + 6, r * (ch + 16) + 30);
            }
        }
        eyeAt = null;
        ImageIO.write(sheet, "png", out);
    }

    static BufferedImage render(List<Bolt> bolts, double age, int w, int h) {
        double[] eye = eyeAt != null ? eyeAt : far ? new double[] {4.5, 3.4, -5.0} : new double[] {3.2, 1.6, -2.4};
        double[] target = eyeAt != null ? targetAt : far ? new double[] {0, 1.5, 7.0} : new double[] {0, 1.0, 0.6};
        DragonPreview.fov = 55;
        DragonPreview.Raster raster = new DragonPreview.Raster(w, h, eye, target);
        raster.ground(0);
        raster.figure(0, 0, 0);
        List<Quad> list = new ArrayList<>();
        BoltMesh.Sink sink = (glow, xyz, argb) -> {
            double[] q = xyz.clone();
            double mx = (q[0] + q[3] + q[6] + q[9]) / 4.0;
            double my = (q[1] + q[4] + q[7] + q[10]) / 4.0;
            double mz = (q[2] + q[5] + q[8] + q[11]) / 4.0;
            list.add(new Quad(q, argb.clone(), glow, mx * mx + my * my + mz * mz));
        };
        BoltMesh.light(0.4, 0.8, 0.3);
        BoltMesh.Frame frame = BoltMesh.Frame.at(-eye[0], -eye[1], -eye[2]);
        for (Bolt bolt : bolts) {
            BoltMesh.build(bolt, frame, age, 0.002, sink);
        }
        // solid first (depth), then the glow far to near
        for (Quad q : list) {
            if (!q.glow()) {
                FxPreview.tri(raster, eye, q.xyz(), q.argb(), 0, 1, 2);
                FxPreview.tri(raster, eye, q.xyz(), q.argb(), 0, 2, 3);
            }
        }
        list.removeIf(q -> !q.glow());
        list.sort(Comparator.comparingDouble(Quad::far).reversed());
        for (Quad q : list) {
            int[] c = glowed(q.argb());
            FxPreview.tri(raster, eye, q.xyz(), c, 0, 1, 2);
            FxPreview.tri(raster, eye, q.xyz(), c, 0, 2, 3);
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, w, h, raster.rgb, 0, w);
        return img;
    }
}
