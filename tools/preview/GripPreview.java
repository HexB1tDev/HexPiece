import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.pika.LightArt;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 2.8.8: the first-person grip of Ama no Murakumo (LightArt.FIRST_PERSON_GRIP), as the game would draw it: the view (x right, y up,
 * looking down -z, FOV 70), the game's first-person right arm (its box through the same steps up to the arm's pivot, flat skin colour)
 * and the blade drawn through all of them. Usage: GripPreview out.png
 */
public final class GripPreview {
    public static void main(String[] args) throws Exception {
        DragonPreview.fov = 70;
        DragonPreview.texture = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        int w = 960;
        int h = 540;
        double[] eye = {0, 0, 0};
        DragonPreview.Raster r = new DragonPreview.Raster(w, h, eye, new double[] {0, 0, -1});
        // up to the arm's pivot: the steps but the last three (the fist, the turn, the guard)
        double[] arm = matrix(LightArt.FIRST_PERSON_GRIP, LightArt.FIRST_PERSON_GRIP.length - 3);
        double[][] box = new double[8][];
        for (int k = 0; k < 8; k++) {
            double x = ((k & 1) == 0 ? -3 : 1) / 16.0;
            double y = ((k & 2) == 0 ? -2 : 10) / 16.0;
            double z = ((k & 4) == 0 ? -2 : 2) / 16.0;
            box[k] = mul(arm, x, y, z);
        }
        int[][] faces = {{0, 2, 6, 4}, {1, 3, 7, 5}, {0, 1, 5, 4}, {2, 3, 7, 6}, {0, 1, 3, 2}, {4, 5, 7, 6}};
        int[] shades = {0xC8946A, 0xB98660, 0xD9A77E, 0xA8784F, 0xCF9C72, 0xBE8A63};
        for (int f = 0; f < 6; f++) {
            int[] q = faces[f];
            r.tri(box[q[0]], box[q[1]], box[q[2]], null, null, null, shades[f]);
            r.tri(box[q[0]], box[q[2]], box[q[3]], null, null, null, shades[f]);
        }
        double[] grip = matrix(LightArt.FIRST_PERSON_GRIP, LightArt.FIRST_PERSON_GRIP.length);
        List<double[]> quads = new ArrayList<>();
        List<int[]> colours = new ArrayList<>();
        FireMesh.Sink sink = (xyz, argb) -> {
            double[] q = new double[12];
            for (int k = 0; k < 4; k++) {
                double[] p = mul(grip, xyz[k * 3], xyz[k * 3 + 1], xyz[k * 3 + 2]);
                System.arraycopy(p, 0, q, k * 3, 3);
            }
            quads.add(q);
            colours.add(argb.clone());
        };
        LightArt.blade(sink, LightArt.BLADE_LENGTH, LightArt.BLADE_WIDTH, 0.5);
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < quads.size(); i++) {
            order.add(i);
        }
        order.sort(Comparator.comparingDouble((Integer i) -> {
            double[] q = quads.get(i);
            double mx = (q[0] + q[3] + q[6] + q[9]) / 4;
            double my = (q[1] + q[4] + q[7] + q[10]) / 4;
            double mz = (q[2] + q[5] + q[8] + q[11]) / 4;
            return mx * mx + my * my + mz * mz;
        }).reversed());
        for (int i : order) {
            FxPreview.tri(r, eye, quads.get(i), colours.get(i), 0, 1, 2);
            FxPreview.tri(r, eye, quads.get(i), colours.get(i), 0, 2, 3);
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, w, h, r.rgb, 0, w);
        ImageIO.write(img, "png", new File(args[0]));
    }

    /** The first {@code n} steps as one 4x4 matrix (row-major), applied as a PoseStack applies them (each on the right). */
    static double[] matrix(double[][] steps, int n) {
        double[] m = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
        for (int i = 0; i < n; i++) {
            double[] s = steps[i];
            double[] t;
            if (s[0] == 0) {
                t = new double[] {1, 0, 0, s[1], 0, 1, 0, s[2], 0, 0, 1, s[3], 0, 0, 0, 1};
            } else {
                double a = Math.toRadians(s[1]);
                double c = Math.cos(a);
                double si = Math.sin(a);
                t = s[0] == 1 ? new double[] {1, 0, 0, 0, 0, c, -si, 0, 0, si, c, 0, 0, 0, 0, 1}
                    : s[0] == 2 ? new double[] {c, 0, si, 0, 0, 1, 0, 0, -si, 0, c, 0, 0, 0, 0, 1}
                    : new double[] {c, -si, 0, 0, si, c, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
            }
            double[] out = new double[16];
            for (int r = 0; r < 4; r++) {
                for (int col = 0; col < 4; col++) {
                    double v = 0;
                    for (int k = 0; k < 4; k++) {
                        v += m[r * 4 + k] * t[k * 4 + col];
                    }
                    out[r * 4 + col] = v;
                }
            }
            m = out;
        }
        return m;
    }

    static double[] mul(double[] m, double x, double y, double z) {
        return new double[] {m[0] * x + m[1] * y + m[2] * z + m[3], m[4] * x + m[5] * y + m[6] * z + m[7], m[8] * x + m[9] * y + m[10] * z + m[11]};
    }
}
