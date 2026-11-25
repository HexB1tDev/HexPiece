import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.horo.AstralArt;
import com.hexhaki.piece.horo.HoroArt;
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
 * 3.3.6: Astral Projection (horo/AstralArt, the very quads client/horo/HoroFx draws) in a skin: the spirit floating with its head turned
 * as the player looks, beside the body it left standing (lit as the game lights an entity: an ambient 0.4 and two lights from above,
 * 0.6 each). Usage: AstralPreview out.png skin.png [--night]
 */
public final class AstralPreview {
    record Quad(double[] xyz, int[] argb, double far) {
    }

    public static void main(String[] args) throws Exception {
        BufferedImage skin = ImageIO.read(new File(args[1]));
        boolean night = java.util.Arrays.asList(args).contains("--night");
        // 3.7.5: a slim skin (three pixel arms) drawn as the game draws it
        AstralArt.slim = java.util.Arrays.asList(args).contains("--slim");
        // views: eye (x, y, z) and the point looked at; the spirit floats 1.2 up at (0, 0, 0) facing +z (yaw 0), the body stands at
        // (1.6, 0, -1.2) facing +z
        double[][] eyes = System.getenv("ASTRAL_SIDE") != null ? new double[][] {{3.2, 2.2, 0.0}, {-3.2, 2.2, 0.3}, {0.3, 2.2, 3.2}, {0.3, 2.2, -3.2}, {2.4, 3.4, 2.4}, {2.3, 1.2, -2.3}} : new double[][] {{3.4, 2.4, 4.2}, {-3.6, 2.2, 3.4}, {4.2, 1.8, -1.0}, {0.4, 2.0, -4.6}, {0.2, 2.6, 4.6}, {4.0, 2.2, 3.0}};
        double[][] heads = {{0, 0}, {0, -40}, {30, 0}, {0, 0}, {-30, 50}, {0, 0}};
        double[] ages = {0, 12, 25, 40, 60, 3};
        int cw = 420;
        int ch = 380;
        BufferedImage sheet = new BufferedImage(cw * 3 + 3, ch * 2 + 3, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        DragonPreview.fov = 50;
        DragonPreview.texture = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < eyes.length; i++) {
            BufferedImage cell = render(skin, eyes[i], heads[i][0], heads[i][1], ages[i], night, cw * 2, ch * 2);
            g.drawImage(cell, (i % 3) * cw + 1, (i / 3) * ch + 1, cw - 1, ch - 1, null);
            g.setColor(Color.WHITE);
            g.drawString(String.format("t%.0f head pitch %.0f yaw %.0f", ages[i], heads[i][0], heads[i][1]), (i % 3) * cw + 8, (i / 3) * ch + 16);
        }
        ImageIO.write(sheet, "png", new File(args[0]));
    }

    static final double[] L0 = unit(0.2, 1.0, -0.7);
    static final double[] L1 = unit(-0.2, 1.0, 0.7);

    static double[] unit(double x, double y, double z) {
        double l = Math.sqrt(x * x + y * y + z * z);
        return new double[] {x / l, y / l, z / l};
    }

    static BufferedImage render(BufferedImage skin, double[] eye, double headPitch, double headYaw, double age, boolean night, int w, int h) {
        DragonPreview.Raster r = new DragonPreview.Raster(w, h, eye, System.getenv("ASTRAL_SIDE") != null ? new double[] {0.0, 2.1, 0.0} : new double[] {0.6, 1.3, -0.4});
        r.ground(0);
        if (night) {
            for (int i = 0; i < r.rgb.length; i++) {
                r.rgb[i] = DragonPreview.scale(r.rgb[i], 0.35F);
            }
        }
        List<Quad> list = new ArrayList<>();
        FireMesh.Sink glow = (xyz, argb) -> add(list, xyz.clone(), argb.clone());
        boolean[] lit = {false};
        AstralArt.Skin skinSink = (xyz, uv, argb, nx, ny, nz) -> {
            double shade = 1.0;
            if (lit[0]) {
                shade = Math.min(1.0, 0.4 + 0.6 * (Math.max(0, nx * L0[0] + ny * L0[1] + nz * L0[2]) + Math.max(0, nx * L1[0] + ny * L1[1] + nz * L1[2])));
            }
            texels(list, skin, xyz, uv, argb, shade);
        };
        float[][] pose = new float[8][8];
        lit[0] = true;
        AstralArt.asleep(pose);
        AstralArt.left(skinSink, pose, new StyleShow.Place(1.6 - eye[0], -eye[1], -1.2 - eye[2], 0.0));
        lit[0] = false;
        if (System.getenv("ASTRAL_KNIGHT") != null) {
            // 3.3.7: the Ito fruit's Black Knight instead of the spirit: walking (ages under 30) or still, lit, coated when the head yaw is 50
            boolean coat = headYaw == 50;
            lit[0] = true;
            AstralArt.walking(age, age < 30 ? 1.0 : 0.0, pose);
            StyleShow.Place knight = new StyleShow.Place(-eye[0], -eye[1], -eye[2], 0.0);
            AstralArt.knight(skinSink, null, pose, knight, 1.0, coat);
            AstralArt.knight(null, glow, pose, knight, 1.0, coat);
        } else {
            AstralArt.floating(age, headPitch, headYaw, pose);
            StyleShow.Place spirit = new StyleShow.Place(-eye[0], 1.2 - eye[1], -eye[2], 0.0);
            AstralArt.spirit(skinSink, null, pose, spirit, Math.min(1.0, age / 6.0 + 0.0001 + (age == 0 ? 1 : 0)));
            AstralArt.spirit(null, glow, pose, spirit, Math.min(1.0, age / 6.0 + (age == 0 ? 1 : 0)));
        }
        // a Negative Hollow beside, for scale and style
        HoroArt.ghost(glow, -1.6 - eye[0], 1.7 - eye[1], 0.8 - eye[2], 0, 0, 1, 0.75, HoroArt.NEGATIVE, age, 1.0, 1);
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

    /** A skin quad texel by texel: each texel the skin's colour times the wash and the shade, its opacity the wash's times the texel's. */
    static void texels(List<Quad> list, BufferedImage skin, double[] xyz, float[] uv, int wash, double shade) {
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
                if ((texel >>> 24) < 26) {
                    continue;
                }
                double ta = (texel >>> 24) / 255.0;
                int rr = (int) (((texel >> 16) & 255) * ((wash >> 16) & 255) / 255 * shade);
                int gg = (int) (((texel >> 8) & 255) * ((wash >> 8) & 255) / 255 * shade);
                int bb = (int) ((texel & 255) * (wash & 255) / 255 * shade);
                int c = (int) Math.round(wa * ta * 255) << 24 | rr << 16 | gg << 8 | bb;
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
