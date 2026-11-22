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
        String[] rows = {"body 2", "body 6", "blade", "gura burst"};
        double[] ages = {0.5, 2, 4, 6, 9, 12};
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
                default -> {
                    // 3.1.2: the Conqueror's lightning mixed into a Gura quake (the fist's crack 1.5 ahead, 1.2 over the ground)
                    List<Bolt> moved = new ArrayList<>();
                    for (Bolt bolt : ConquerorSparks.burst(4.0, 10, -1.2, 14L)) {
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
                g.drawImage(render(bolts, ages[c], cw * 2, ch * 2), c * cw + 2, r * (ch + 16) + 16, cw - 2, ch - 2, null);
                g.setColor(Color.BLACK);
                g.drawString(String.format("t%.1f", ages[c]), c * cw + 6, r * (ch + 16) + 30);
            }
        }
        ImageIO.write(sheet, "png", new File(args[0]));
    }

    static BufferedImage render(List<Bolt> bolts, double age, int w, int h) {
        double[] eye = {3.2, 1.6, -2.4};
        double[] target = {0, 1.0, 0.6};
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
            FxPreview.tri(raster, eye, q.xyz(), q.argb(), 0, 1, 2);
            FxPreview.tri(raster, eye, q.xyz(), q.argb(), 0, 2, 3);
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, w, h, raster.rgb, 0, w);
        return img;
    }
}
