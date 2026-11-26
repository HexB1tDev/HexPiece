import com.hexhaki.piece.haki.Bolt;
import com.hexhaki.piece.haki.BoltMesh;
import com.hexhaki.piece.haki.ClashArt;
import com.hexhaki.piece.haki.ConquerorClashRules;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 3.8.2: the Conqueror's Haki clash in the world (haki/ClashArt and the clash's lightning, ClashBurst) as the game draws them: two
 * fighters 14 blocks apart (A red at the origin, the preview's own figure; B blue 14 blocks along x), the impact ringed in black at the meeting point, each one's Haki pouring into it,
 * the bubble's wind, both colours' lightning tearing out. A sheet of moments (ticks) at night and by day, the bar at {@code v}.
 * Usage: java -cp build/preview:build/classes ConquerorClashPreview out-dir
 */
public class ConquerorClashPreview {
    public static void main(String[] args) throws Exception {
        File dir = new File(args[0]);
        dir.mkdirs();
        double[][] moments = {{2, 0}, {6, 0}, {12, 0}, {30, 0.0}, {60, 0.45}, {94, -0.55}};
        int colorA = 0xE0242C;
        int colorB = 0x2A7BFF;
        double[] a = {0, 0, 0};
        double[] b = {14, 0, 0};
        double radius = ConquerorClashRules.bubble(14);
        for (boolean night : new boolean[] {true, false}) {
            List<BufferedImage> images = new ArrayList<>();
            for (double[] moment : moments) {
                double age = moment[0];
                double v = moment[1];
                BoltPreview.Raster raster = new BoltPreview.Raster(new BoltPreview.Shot("clash", new double[] {4, 5.5, -19},
                    new double[] {7, 2.2, 0}, night));
                raster.scene();
                figure(raster, b, 0.16F, 0.26F, 0.62F);
                double t = ConquerorClashRules.meeting(v);
                double[] meet = {a[0] + (b[0] - a[0]) * t, 1.3, a[2] + (b[2] - a[2]) * t};
                double[] e = raster.eye;
                List<double[]> glowV = new ArrayList<>();
                List<int[]> glowC = new ArrayList<>();
                BoltMesh.Sink sink = (glow, xyz, argb) -> {
                    if (glow) {
                        glowV.add(xyz.clone());
                        glowC.add(argb.clone());
                    } else {
                        raster.quad(xyz, argb, false);
                    }
                };
                double shine = night ? 1.0 : 0.5;
                double[] m = {meet[0] - e[0], meet[1] - e[1], meet[2] - e[2]};
                double ax = a[0] - meet[0];
                double ay = a[1] + 1.3 - meet[1];
                double az = a[2] - meet[2];
                double al = Math.sqrt(ax * ax + ay * ay + az * az);
                ClashArt.impact(m[0], m[1], m[2], ax / al, ay / al, az / al, age, colorA, colorB, shine, sink);
                ClashArt.stream(new double[] {a[0] - e[0], 1.3 - e[1], a[2] - e[2]}, m, age, colorA, shine, sink);
                ClashArt.stream(new double[] {b[0] - e[0], 1.3 - e[1], b[2] - e[2]}, m, age, colorB, shine, sink);
                ClashArt.bubble(new double[] {7 - e[0], 1.0 - e[1], -e[2]}, radius, age, shine, sink);
                ClashArt.wave(m[0], m[1], m[2], ax / al, ay / al, az / al, 1.3, radius * 1.6, age, colorA, colorB, shine, sink);
                // the lightning: the collision's burst (power 3) and the latest of those torn out every 9 ticks (power 2)
                double since = age < 9 ? age : age % 9;
                float power = age < 9 ? 3.0F : 2.0F;
                List<Bolt> burst = com.hexhaki.piece.haki.ClashBurst.bolts(power, colorA, colorB, 1.3, 77L + (long) (age / 9));
                for (int i = 0; i < burst.size(); i++) {
                    Bolt bolt = burst.get(i);
                    BoltMesh.Frame frame = BoltMesh.Frame.at(m[0], m[1], m[2]);
                    BoltMesh.build(bolt, frame, since - bolt.delay, 2.0 * Math.tan(Math.toRadians(70) / 2) / 300, shine, sink);
                }
                for (int i = 0; i < glowV.size(); i++) {
                    raster.glowQuad(glowV.get(i), glowC.get(i));
                }
                images.add(raster.image());
            }
            int w = images.get(0).getWidth();
            int h = images.get(0).getHeight();
            BufferedImage sheet = new BufferedImage(w * 3, h * 2, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = sheet.createGraphics();
            for (int i = 0; i < images.size(); i++) {
                g.drawImage(images.get(i), (i % 3) * w, (i / 3) * h, null);
                g.setColor(java.awt.Color.WHITE);
                g.drawString("t" + moments[i][0] + "  bar " + moments[i][1], (i % 3) * w + 6, (i / 3) * h + 14);
            }
            g.dispose();
            ImageIO.write(sheet, "png", new File(dir, night ? "conqueror-clash-night.png" : "conqueror-clash-day.png"));
        }
    }

    /** A player-sized figure standing at {@code at}. */
    static void figure(BoltPreview.Raster raster, double[] at, float r, float g, float b) {
        float[] skin = {0.78F, 0.6F, 0.48F};
        float[] shirt = {r, g, b};
        float[] legs = {0.2F, 0.22F, 0.4F};
        raster.box(at[0] - 0.25, 0, at[2] - 0.125, at[0] + 0.25, 0.75, at[2] + 0.125, legs);
        raster.box(at[0] - 0.25, 0.75, at[2] - 0.125, at[0] + 0.25, 1.5, at[2] + 0.125, shirt);
        raster.box(at[0] - 0.25, 1.5, at[2] - 0.25, at[0] + 0.25, 2.0, at[2] + 0.25, skin);
    }
}
