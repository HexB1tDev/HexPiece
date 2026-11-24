import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.ConquerorStorm;
import com.hexhaki.piece.haki.HakiColour;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 3.6.0: the Conqueror's lightning in each of the seven Haki colours (haki/HakiColour), as the game tints it at launch (Bolt.tinted with
 * the set's rim) and draws it (BoltMesh, through SparkPreview.render): the release's own bolts (mastery 500, 8 ahead, seen over the
 * shoulder) and a blast of a move (radius 4, close), at two moments. Usage: HakiColourShots out.png
 */
public final class HakiColourShots {
    public static void main(String[] args) throws Exception {
        HakiColour[] sets = HakiColour.values();
        int cw = 300;
        int ch = 300;
        String[] rows = {"release t6", "release t14", "blast t4"};
        BufferedImage sheet = new BufferedImage(cw * sets.length, (ch + 16) * rows.length, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(0x202020));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        DragonPreview.texture = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < sets.length; c++) {
                List<Bolt> bolts = new ArrayList<>();
                if (r < 2) {
                    for (ConquerorStorm.Strand strand : ConquerorStorm.release(500, 17L, false)) {
                        for (Bolt bolt : ConquerorStorm.bolts(strand)) {
                            double[] q = bolt.points.clone();
                            for (int i = 0; i < q.length; i += 3) {
                                q[i + 2] += 8.0;
                            }
                            bolts.add(new Bolt(q, bolt.widths, bolt.color, bolt.kind, bolt.delay, bolt.duration, bolt.crawl, bolt.seed, bolt.dance)
                                .tinted(sets[c].rim));
                        }
                    }
                } else {
                    for (Bolt bolt : ConquerorStorm.blast(4.0, 10, -1.2, 14L, 1.0)) {
                        double[] q = bolt.points.clone();
                        for (int i = 0; i < q.length; i += 3) {
                            q[i + 1] += 1.2;
                            q[i + 2] += 1.5;
                        }
                        bolts.add(new Bolt(q, bolt.widths, bolt.color, bolt.kind, bolt.delay, bolt.duration, bolt.crawl, bolt.seed, bolt.dance)
                            .tinted(sets[c].rim));
                    }
                }
                SparkPreview.far = true;
                double age = r == 0 ? 6 : r == 1 ? 14 : 4;
                g.drawImage(SparkPreview.render(bolts, age, cw * 2, ch * 2), c * cw, r * (ch + 16) + 16, cw - 2, ch - 2, null);
                g.setColor(Color.WHITE);
                g.drawString(sets[c].display + "  " + rows[r], c * cw + 4, r * (ch + 16) + 13);
            }
        }
        ImageIO.write(sheet, "png", new File(args[0]));
    }
}
