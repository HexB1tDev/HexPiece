import com.hexhaki.piece.clash.ClashRules;
import com.hexhaki.piece.clash.ImpactFrame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 2.3.6: renders a clash's impact frame off-game to judge it (the house rule), from the game's own {@link ImpactFrame} batches over a
 * picture of the view, blended as the game blends them: {@link ImpactFrame#INVERT} turns the colour under it over (one minus it), the
 * rest is alpha blended. Triangles are filled with their points' colours mixed across them, as the game's position-colour shader does.
 *
 * <p>javac -cp build/classes -d build/preview tools/preview/ImpactPreview.java; java -cp build/preview:build/classes ImpactPreview
 * BACKGROUND.png OUT_DIR POWER COLOR_A COLOR_B PX PY (colours as hex, 0 for none; writes frame_NNN.png at 60 frames a second)
 */
public final class ImpactPreview {
    public static void main(String[] args) throws Exception {
        BufferedImage background = ImageIO.read(new File(args[0]));
        File out = new File(args[1]);
        out.mkdirs();
        float power = Float.parseFloat(args[2]);
        int colorA = (int) Long.parseLong(args[3], 16);
        int colorB = (int) Long.parseLong(args[4], 16);
        double px = Double.parseDouble(args[5]);
        double py = Double.parseDouble(args[6]);
        int w = background.getWidth();
        int h = background.getHeight();
        double length = ImpactFrame.length(power);
        int frames = (int) Math.ceil(length * 3.0) + 3;
        for (int f = 0; f < frames; f++) {
            double age = f / 3.0 - 1.0 / 3.0;
            float[][] rgb = new float[3][w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int c = background.getRGB(x, y);
                    rgb[0][y * w + x] = ((c >> 16) & 255) / 255.0F;
                    rgb[1][y * w + x] = ((c >> 8) & 255) / 255.0F;
                    rgb[2][y * w + x] = (c & 255) / 255.0F;
                }
            }
            List<ImpactFrame.Batch> batches = ImpactFrame.at(age, w, h, px, py, power, colorA, colorB, 1234L);
            for (ImpactFrame.Batch batch : batches) {
                if (batch.mode() == ImpactFrame.CONTRAST) {
                    // the game: a copy of the view drawn over it, multiplied both ways (src * dst + dst * src)
                    for (int k = 0; k < 3; k++) {
                        for (int i = 0; i < w * h; i++) {
                            rgb[k][i] = Math.min(1.0F, 2.0F * rgb[k][i] * rgb[k][i]);
                        }
                    }
                    continue;
                }
                for (int t = 0; t + 2 < batch.count(); t += 3) {
                    tri(rgb, w, h, batch, t, batch.mode() == ImpactFrame.INVERT);
                }
            }
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < w * h; i++) {
                int r = Math.round(Math.max(0, Math.min(1, rgb[0][i])) * 255);
                int g = Math.round(Math.max(0, Math.min(1, rgb[1][i])) * 255);
                int b = Math.round(Math.max(0, Math.min(1, rgb[2][i])) * 255);
                image.setRGB(i % w, i / w, r << 16 | g << 8 | b);
            }
            ImageIO.write(image, "png", new File(out, String.format("frame_%03d.png", f)));
        }
        System.out.println("wrote " + frames + " frames, power " + power + ", frame " + ClashRules.frameTicks(power) + " ticks");
    }

    /** One triangle, its points' colours mixed across it; inverting (white over: one minus the colour) or alpha blended. */
    static void tri(float[][] rgb, int w, int h, ImpactFrame.Batch batch, int t, boolean invert) {
        float[] p = batch.xy();
        double x0 = p[t * 2];
        double y0 = p[t * 2 + 1];
        double x1 = p[t * 2 + 2];
        double y1 = p[t * 2 + 3];
        double x2 = p[t * 2 + 4];
        double y2 = p[t * 2 + 5];
        double area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0);
        if (Math.abs(area) < 1.0E-9) {
            return;
        }
        int minX = (int) Math.max(0, Math.floor(Math.min(x0, Math.min(x1, x2))));
        int maxX = (int) Math.min(w - 1, Math.ceil(Math.max(x0, Math.max(x1, x2))));
        int minY = (int) Math.max(0, Math.floor(Math.min(y0, Math.min(y1, y2))));
        int maxY = (int) Math.min(h - 1, Math.ceil(Math.max(y0, Math.max(y1, y2))));
        int[] c = {batch.argb()[t], batch.argb()[t + 1], batch.argb()[t + 2]};
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double sx = x + 0.5;
                double sy = y + 0.5;
                double a = ((x1 - sx) * (y2 - sy) - (x2 - sx) * (y1 - sy)) / area;
                double b = ((x2 - sx) * (y0 - sy) - (x0 - sx) * (y2 - sy)) / area;
                double g = 1.0 - a - b;
                if (a < 0 || b < 0 || g < 0) {
                    continue;
                }
                int i = y * w + x;
                if (invert) {
                    for (int k = 0; k < 3; k++) {
                        rgb[k][i] = 1.0F - rgb[k][i];
                    }
                    continue;
                }
                double alpha = (a * ((c[0] >>> 24) & 255) + b * ((c[1] >>> 24) & 255) + g * ((c[2] >>> 24) & 255)) / 255.0;
                for (int k = 0; k < 3; k++) {
                    int shift = 16 - 8 * k;
                    double v = (a * ((c[0] >> shift) & 255) + b * ((c[1] >> shift) & 255) + g * ((c[2] >> shift) & 255)) / 255.0;
                    rgb[k][i] = (float) (v * alpha + rgb[k][i] * (1.0 - alpha));
                }
            }
        }
    }
}
