import com.hexhaki.piece.style.HandMoves;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * 3.1.4: the first-person hand's moves (style/HandMoves, as client/FirstPersonHand puts them on the game's own hand) seen from the eye: a
 * stand-in for the game's right arm (a box from the lower right of the view up toward the crosshair, where the game draws it) moved by
 * each clip, a row per clip, a column per moment; the crosshair drawn. Usage: HandPreview out.png [--to ticks] clip ...
 */
public final class HandPreview {
    public static void main(String[] args) throws Exception {
        double to = 12;
        java.util.List<String> clips = new java.util.ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("--to")) {
                to = Double.parseDouble(args[++i]);
            } else {
                clips.add(args[i]);
            }
        }
        int cols = 7;
        int w = 220;
        int h = 140;
        BufferedImage img = new BufferedImage(cols * w, clips.size() * (h + 14), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double[] m = new double[6];
        for (int r = 0; r < clips.size(); r++) {
            g.setColor(Color.WHITE);
            g.drawString(clips.get(r), 4, r * (h + 14) + 11);
            for (int c = 0; c < cols; c++) {
                double t = to * c / (cols - 1);
                HandMoves.at(clips.get(r), t, m);
                int ox = c * w;
                int oy = r * (h + 14) + 14;
                g.setColor(new Color(0x8FB8DE));
                g.fillRect(ox, oy, w - 2, h / 2);
                g.setColor(new Color(0x7EA35A));
                g.fillRect(ox, oy + h / 2, w - 2, h / 2);
                g.setColor(Color.BLACK);
                g.drawString(String.format("t%.1f", t), ox + 4, oy + 12);
                g.setColor(Color.WHITE);
                g.drawLine(ox + w / 2 - 5, oy + h / 2, ox + w / 2 + 5, oy + h / 2);
                g.drawLine(ox + w / 2, oy + h / 2 - 5, ox + w / 2, oy + h / 2 + 5);
                g.setClip(ox, oy, w - 2, h);
                arm(g, m, ox, oy, w, h);
                g.setClip(null);
            }
        }
        g.dispose();
        ImageIO.write(img, "png", new File(args[0]));
    }

    /** The stand-in arm: a box from below the right edge of the view up to the fist, moved by {@code m}, projected (70 degrees tall). */
    static void arm(Graphics2D g, double[] m, int ox, int oy, int w, int h) {
        double[] fist = {0.42, -0.42, -0.85};
        double[] elbow = {0.62, -0.95, -0.55};
        double r = 0.07;
        double[][] corners = new double[8][];
        for (int k = 0; k < 8; k++) {
            double[] base = k < 4 ? elbow : fist;
            double sx = (k & 1) == 0 ? -r : r;
            double sy = (k & 2) == 0 ? -r : r;
            double[] p = {base[0] + sx, base[1] + sy, base[2]};
            double[] q = new double[3];
            HandMoves.apply(m, p, q);
            corners[k] = q;
        }
        int[][] faces = {{0, 1, 5, 4}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}, {4, 5, 7, 6}};
        Color[] shade = {new Color(0xC08A60), new Color(0xD8A070), new Color(0xB07A50), new Color(0xE0B080), new Color(0xF0C090)};
        double f = (h / 2.0) / Math.tan(Math.toRadians(35));
        for (int fi = 0; fi < faces.length; fi++) {
            Polygon poly = new Polygon();
            boolean ok = true;
            for (int k : faces[fi]) {
                double[] p = corners[k];
                if (p[2] > -0.05) {
                    ok = false;
                    break;
                }
                poly.addPoint(ox + (int) (w / 2.0 + p[0] / -p[2] * f), oy + (int) (h / 2.0 - p[1] / -p[2] * f));
            }
            if (ok) {
                g.setColor(shade[fi]);
                g.fillPolygon(poly);
                g.setColor(new Color(0x704830));
                g.setStroke(new BasicStroke(1));
                g.drawPolygon(poly);
            }
        }
    }
}
