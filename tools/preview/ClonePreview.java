import com.hexhaki.piece.client.npc.StylePoser;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.pika.LightClone;
import com.hexhaki.piece.style.StyleShow;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 2.8.8: the Light fruit's clones (pika/LightClone, the very quads the game draws) in a skin, from beside: running with their
 * afterimages, cutting, fading. Usage: ClonePreview out.png skin.png [--night]
 */
public final class ClonePreview {
    record Quad(double[] xyz, int[] argb, double far) {
    }

    public static void main(String[] args) throws Exception {
        BufferedImage skin = ImageIO.read(new File(args[1]));
        boolean night = args.length > 2 && args[2].equals("--night");
        String[][] shots = {{"pika_sword_walk", "4"}, {"pika_sword_walk", "12"}, {"pika_sword_1", "3"}, {"pika_sword_1", "6"},
            {"pika_sword_2", "3"}, {"pika_sword_2", "7"}, {"pika_sword_idle", "0"}, {"pika_sword_walk", "16"}};
        int cw = 420;
        int ch = 360;
        BufferedImage sheet = new BufferedImage(cw * 4 + 4, ch * 2 + 4, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        DragonPreview.fov = 50;
        DragonPreview.texture = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < shots.length; i++) {
            boolean running = shots[i][0].equals("pika_sword_walk") && i != 6;
            BufferedImage cell = render(skin, shots[i][0], Float.parseFloat(shots[i][1]), running, night, cw * 2, ch * 2, i);
            g.drawImage(cell, (i % 4) * cw + 2, (i / 4) * ch + 2, cw - 2, ch - 2, null);
            g.setColor(Color.WHITE);
            g.drawString(shots[i][0] + " t" + shots[i][1] + (running ? " (running, afterimages)" : ""), (i % 4) * cw + 8, (i / 4) * ch + 16);
        }
        ImageIO.write(sheet, "png", new File(args[0]));
    }

    static BufferedImage render(BufferedImage skin, String anim, float t, boolean running, boolean night, int w, int h, int seed) {
        double[] eye = {3.2, 1.7, 2.6};
        DragonPreview.Raster r = new DragonPreview.Raster(w, h, eye, new double[] {0, 1.0, 0.2});
        r.ground(0);
        if (night) {
            for (int i = 0; i < r.rgb.length; i++) {
                r.rgb[i] = DragonPreview.scale(r.rgb[i], 0.35F);
            }
        }
        List<Quad> list = new ArrayList<>();
        FireMesh.Sink glow = (xyz, argb) -> add(list, xyz.clone(), argb.clone());
        LightClone.Skin skinSink = (xyz, uv, argb) -> texels(list, skin, xyz, uv, argb);
        double yaw = Math.toRadians(-20.0);
        int ghosts = running ? 2 : 0;
        for (int ghost = ghosts; ghost >= 0; ghost--) {
            double back = ghost * 0.9;
            StyleShow.Place place = new StyleShow.Place(Math.sin(yaw) * back - eye[0], -eye[1], -Math.cos(yaw) * back - eye[2], yaw);
            float[][] pose = StylePoser.sample(anim, Math.max(0, t - ghost * 0.8F));
            double alpha = ghost == 0 ? 1.0 : ghost == 1 ? 0.35 : 0.15;
            LightClone.body(skinSink, glow, pose, place, alpha, ghost == 0, t * 0.4 + seed);
        }
        list.sort(Comparator.comparingDouble(Quad::far).reversed());
        for (Quad q : list) {
            FxPreview.tri(r, eye, q.xyz(), q.argb(), 0, 1, 2);
            FxPreview.tri(r, eye, q.xyz(), q.argb(), 0, 2, 3);
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, w, h, r.rgb, 0, w);
        return img;
    }

    static void add(List<Quad> list, double[] q, int[] c) {
        double mx = (q[0] + q[3] + q[6] + q[9]) / 4.0;
        double my = (q[1] + q[4] + q[7] + q[10]) / 4.0;
        double mz = (q[2] + q[5] + q[8] + q[11]) / 4.0;
        list.add(new Quad(q, c, mx * mx + my * my + mz * mz));
    }

    /** A skin quad texel by texel: each texel the skin's colour times the wash, its opacity the wash's. */
    static void texels(List<Quad> list, BufferedImage skin, double[] xyz, float[] uv, int wash) {
        int u0 = Math.round(uv[0] * 64);
        int v0 = Math.round(uv[1] * 64);
        int u1 = Math.round(uv[2] * 64);
        int v1 = Math.round(uv[5] * 64);
        int tw = u1 - u0;
        int th = v1 - v0;
        if (tw <= 0 || th <= 0) {
            return;
        }
        double wa = ((wash >>> 24) & 255) / 255.0;
        for (int i = 0; i < tw; i++) {
            for (int j = 0; j < th; j++) {
                int texel = skin.getRGB(u0 + i, v0 + j);
                if ((texel >>> 24) < 128) {
                    continue;
                }
                int rr = ((texel >> 16) & 255) * ((wash >> 16) & 255) / 255;
                int gg = ((texel >> 8) & 255) * ((wash >> 8) & 255) / 255;
                int bb = (texel & 255) * (wash & 255) / 255;
                int c = (int) Math.round(wa * 255) << 24 | rr << 16 | gg << 8 | bb;
                double s0 = i / (double) tw;
                double s1 = (i + 1) / (double) tw;
                double t0 = j / (double) th;
                double t1 = (j + 1) / (double) th;
                double[] q = new double[12];
                put(q, 0, xyz, s0, t0);
                put(q, 1, xyz, s1, t0);
                put(q, 2, xyz, s1, t1);
                put(q, 3, xyz, s0, t1);
                add(list, q, new int[] {c, c, c, c});
            }
        }
    }

    static void put(double[] q, int k, double[] xyz, double s, double t) {
        for (int a = 0; a < 3; a++) {
            double top = xyz[a] + (xyz[3 + a] - xyz[a]) * s;
            double bottom = xyz[9 + a] + (xyz[6 + a] - xyz[9 + a]) * s;
            q[k * 3 + a] = top + (bottom - top) * t;
        }
    }
}
