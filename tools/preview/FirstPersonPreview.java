import com.hexhaki.piece.client.npc.StylePoser;
import com.hexhaki.piece.style.KickLeg;
import com.hexhaki.piece.style.StyleMove;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 2.8.2: the first-person view of the style moves that kick (client/FirstPersonLegs): the legs as the game draws them there (style/KickLeg
 * from the move's own keyframes, the player's skin), seen from the eye looking ahead (or down, {@code --pitch 30}), a row per move, a
 * column per moment. Usage: FirstPersonPreview out.png skin.png [--pitch deg] [--frames n] [move ...]
 */
public final class FirstPersonPreview {
    public static void main(String[] args) throws Exception {
        File out = new File(args[0]);
        BufferedImage skin = ImageIO.read(new File(args[1]));
        double pitch = 0;
        int frames = 8;
        List<StyleMove> moves = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("--pitch")) {
                pitch = Double.parseDouble(args[++i]);
            } else if (args[i].equals("--frames")) {
                frames = Integer.parseInt(args[++i]);
            } else {
                moves.add(StyleMove.valueOf(args[i]));
            }
        }
        if (moves.isEmpty()) {
            for (StyleMove m : StyleMove.values()) {
                if (kicks(m)) {
                    moves.add(m);
                }
            }
        }
        int cw = 256;
        int ch = 144;
        BufferedImage sheet = new BufferedImage(cw * frames + 4, (ch + 16) * moves.size() + 4, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(0x202020));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        DragonPreview.fov = 70;
        DragonPreview.texture = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        for (int r = 0; r < moves.size(); r++) {
            StyleMove move = moves.get(r);
            float length = length(move);
            g.setColor(Color.WHITE);
            g.drawString(move.name() + "  (" + move.animation() + ", " + length + " t)", 4, r * (ch + 16) + 13);
            for (int c = 0; c < frames; c++) {
                float t = length * c / (frames - 1);
                double[] eye = {0, 1.62, 0};
                double[] target = {0, 1.62 - Math.sin(Math.toRadians(pitch)), Math.cos(Math.toRadians(pitch))};
                DragonPreview.Raster raster = new DragonPreview.Raster(cw * 2, ch * 2, eye, target);
                raster.ground(0);
                raster.figure(0.4, 0, 3.0);
                legs(raster, StylePoser.sample(move.animation(), t), skin, pitch);
                BufferedImage cell = new BufferedImage(cw * 2, ch * 2, BufferedImage.TYPE_INT_RGB);
                cell.setRGB(0, 0, cw * 2, ch * 2, raster.rgb, 0, cw * 2);
                g.drawImage(cell, c * cw + 2, r * (ch + 16) + 16, cw - 2, ch - 2, null);
                g.setColor(Color.BLACK);
                g.drawString("t" + Math.round(t * 10) / 10.0, c * cw + 6, r * (ch + 16) + 30);
            }
        }
        ImageIO.write(sheet, "png", out);
    }

    static boolean kicks(StyleMove m) {
        for (float t = 0.0F; t <= 30.0F; t += 0.5F) {
            float[][] pose = StylePoser.sample(m.animation(), t);
            for (int part = 4; part <= 5; part++) {
                if (pose[part][StylePoser.WEIGHT] > 0.5F && (Math.abs(pose[part][3]) > 35.0F || Math.abs(pose[part][5]) > 35.0F)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The last moment a leg still moves (ticks). */
    static float length(StyleMove m) {
        float last = 1;
        float[][] prev = StylePoser.sample(m.animation(), 0);
        for (float t = 0.5F; t <= 40.0F; t += 0.5F) {
            float[][] pose = StylePoser.sample(m.animation(), t);
            for (int part : new int[] {0, 4, 5}) {
                for (int ch = 0; ch < 8; ch++) {
                    if (Math.abs(pose[part][ch] - prev[part][ch]) > 1.0E-3) {
                        last = t;
                    }
                }
            }
            prev = pose;
        }
        return last;
    }

    static void legs(DragonPreview.Raster raster, float[][] pose, BufferedImage skin, double pitchDeg) {
        double cp = Math.cos(Math.toRadians(pitchDeg));
        double sp = Math.sin(Math.toRadians(pitchDeg));
        for (int part = 4; part <= 5; part++) {
            if (pose[part][StylePoser.WEIGHT] < 0.05F) {
                continue;
            }
            for (int half = 0; half < 2; half++) {
                for (int layer = 0; layer < 2; layer++) {
                    double[] c = KickLeg.onBody(part, half == 1, pose, layer == 1 ? 0.25 : 0.0);
                    int[] uv = KickLeg.skin(part == 4, layer == 1);
                    for (int[] f : KickLeg.faces(half == 1, uv[0], uv[1])) {
                        face(raster, c, f, skin);
                    }
                }
            }
        }
    }

    /** A face texel by texel (the skin's pixels as small flat quads), lit by its normal. */
    static void face(DragonPreview.Raster raster, double[] p, int[] f, BufferedImage skin) {
        double[] a = corner(p, f[0]);
        double[] b = corner(p, f[1]);
        double[] c = corner(p, f[2]);
        double[] d = corner(p, f[3]);
        double[] u = sub(b, a);
        double[] v = sub(d, a);
        double[] n = cross(u, v);
        double nl = Math.sqrt(dot(n, n));
        if (nl < 1e-9) {
            return;
        }
        double light = DragonPreview.Raster.entityLight(n[0] / nl, n[1] / nl, n[2] / nl);
        int tw = f[6] - f[4];
        int th = f[7] - f[5];
        for (int i = 0; i < tw; i++) {
            for (int j = 0; j < th; j++) {
                int argb = skin.getRGB(f[4] + i, f[5] + j);
                if ((argb >>> 24) < 128) {
                    continue;
                }
                double s0 = i / (double) tw;
                double s1 = (i + 1) / (double) tw;
                double t0 = j / (double) th;
                double t1 = (j + 1) / (double) th;
                double[] q00 = bilerp(a, b, c, d, s0, t0);
                double[] q10 = bilerp(a, b, c, d, s1, t0);
                double[] q11 = bilerp(a, b, c, d, s1, t1);
                double[] q01 = bilerp(a, b, c, d, s0, t1);
                int col = shade(argb & 0xFFFFFF, light);
                raster.tri(q00, q10, q11, null, null, null, col);
                raster.tri(q00, q11, q01, null, null, null, col);
            }
        }
    }

    static double[] bilerp(double[] a, double[] b, double[] c, double[] d, double s, double t) {
        double[] out = new double[3];
        for (int k = 0; k < 3; k++) {
            double top = a[k] + (b[k] - a[k]) * s;
            double bottom = d[k] + (c[k] - d[k]) * s;
            out[k] = top + (bottom - top) * t;
        }
        return out;
    }

    static int shade(int rgb, double light) {
        int r = (int) Math.min(255, ((rgb >> 16) & 255) * light);
        int g = (int) Math.min(255, ((rgb >> 8) & 255) * light);
        int b = (int) Math.min(255, (rgb & 255) * light);
        return (r << 16) | (g << 8) | b;
    }

    static double[] corner(double[] p, int i) {
        return new double[] {p[i * 3], p[i * 3 + 1], p[i * 3 + 2]};
    }

    static double[] sub(double[] a, double[] b) {
        return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }
}
