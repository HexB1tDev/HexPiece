import com.hexhaki.piece.client.npc.StylePoser;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.style.StyleMove;
import com.hexhaki.piece.style.StyleRig;
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
 * 2.8.3: the fighting styles' 3D blows (style/StyleShow, the very code the game draws) on the posed player (style/StyleRig, flat
 * colours), a target dummy 2.4 blocks ahead hit by every blow, a row per move, a column per moment.
 * Usage: StylePreview out.png [--haki] [--fp] [--frames n] [--view deg] [--from t] [--to t] MOVE ...
 */
public final class StylePreview {
    static final StyleShow.Poser POSER = StylePoser::sample;

    public static void main(String[] args) throws Exception {
        File out = new File(args[0]);
        boolean haki = false;
        boolean fp = false;
        int frames = 8;
        double view = 55;
        double from = -1;
        double to = -1;
        List<StyleMove> moves = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--haki" -> haki = true;
                case "--fp" -> fp = true;
                case "--frames" -> frames = Integer.parseInt(args[++i]);
                case "--view" -> view = Double.parseDouble(args[++i]);
                case "--from" -> from = Double.parseDouble(args[++i]);
                case "--to" -> to = Double.parseDouble(args[++i]);
                default -> moves.add(StyleMove.valueOf(args[i]));
            }
        }
        int cw = 360;
        int ch = 250;
        BufferedImage sheet = new BufferedImage(cw * frames + 4, (ch + 16) * moves.size() + 4, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(0x202020));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        DragonPreview.fov = 70;
        DragonPreview.texture = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        for (int r = 0; r < moves.size(); r++) {
            StyleMove move = moves.get(r);
            StyleShow.Strike[] strikes = StyleShow.strikes(move, POSER);
            double t0 = from >= 0 ? from : Math.max(0, strikes.length > 0 ? strikes[0].tick() - 3 : 0);
            double t1 = to >= 0 ? to : (strikes.length > 0 ? strikes[strikes.length - 1].tick() : move.duration()) + 6;
            g.setColor(Color.WHITE);
            StringBuilder label = new StringBuilder(move.name() + (haki ? " +Haki" : "") + "  blows:");
            for (StyleShow.Strike s : strikes) {
                label.append(" t").append(s.tick()).append(" limb").append(s.limb()).append(s.straight() ? " straight" : " swung");
            }
            g.drawString(label.toString(), 4, r * (ch + 16) + 13);
            for (int c = 0; c < frames; c++) {
                double t = t0 + (t1 - t0) * c / Math.max(1, frames - 1);
                BufferedImage cell = render(move, haki, fp, view, (float) t, cw * 2, ch * 2);
                g.drawImage(cell, c * cw + 2, r * (ch + 16) + 16, cw - 2, ch - 2, null);
                g.setColor(Color.BLACK);
                g.drawString(String.format("t%.1f", t), c * cw + 6, r * (ch + 16) + 30);
            }
        }
        ImageIO.write(sheet, "png", out);
    }

    record Quad(double[] xyz, int[] argb, double far) {
    }

    static BufferedImage render(StyleMove move, boolean haki, boolean fp, double view, float t, int w, int h) {
        double[] eye;
        double[] target;
        if (fp) {
            eye = new double[] {0, 1.62, 0};
            target = new double[] {0, 1.62 - Math.sin(Math.toRadians(12)), Math.cos(Math.toRadians(12))};
        } else {
            // from the player's left (90: square to the side; less: from behind them), the player and the dummy both in sight
            double a = Math.toRadians(view);
            target = new double[] {0, 1.0, 1.2};
            eye = new double[] {Math.sin(a) * 4.2, 1.7, 1.2 - Math.cos(a) * 4.2};
        }
        DragonPreview.fov = fp ? 70 : 48;
        DragonPreview.Raster raster = new DragonPreview.Raster(w, h, eye, target);
        raster.ground(0);
        raster.figure(0, 0, 2.4);
        float[][] pose = StylePoser.sample(move.animation(), Math.max(0, t));
        body(raster, pose, fp);
        List<Quad> list = new ArrayList<>();
        FireMesh.Sink sink = (xyz, argb) -> {
            double[] q = xyz.clone();
            double mx = (q[0] + q[3] + q[6] + q[9]) / 4.0;
            double my = (q[1] + q[4] + q[7] + q[10]) / 4.0;
            double mz = (q[2] + q[5] + q[8] + q[11]) / 4.0;
            list.add(new Quad(q, argb.clone(), mx * mx + my * my + mz * mz));
        };
        StyleShow.Place place = new StyleShow.Place(-eye[0], -eye[1], -eye[2], 0.0);
        StyleShow.attached(sink, move, haki, t, POSER, place, 7, fp);
        StyleShow.Strike[] strikes = StyleShow.strikes(move, POSER);
        for (int i = 0; i < strikes.length; i++) {
            double age = t - strikes[i].tick();
            if (age >= 0 && age < StyleShow.landedLife(move)) {
                double[] at = {0 - eye[0], 1.0 - eye[1], 2.15 - eye[2]};
                StyleShow.landed(sink, move, haki, age, at, new double[] {0, 0, 1}, strikes[i].size(), 11 + i);
            }
        }
        // a line move's shot from the eye along the look (the server's line), 8 blocks of it
        if (StyleShow.shotKind(move) != null) {
            for (StyleShow.Strike st : strikes) {
                double age = t - st.tick();
                if (age >= 0) {
                    double[] from = {0 - eye[0], 1.45 - eye[1], 0.4 - eye[2]};
                    double[] to = {0 - eye[0], 1.2 - eye[1], 8.0 - eye[2]};
                    StyleShow.shot(sink, move, haki, age, from, to, 5);
                }
            }
        }
        // as the game lays them (client/fx/StyleVfx.Collector): far to near in 5 cm steps, each step in the order made
        List<Quad> ordered = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Quad q = list.get(i);
            ordered.add(new Quad(q.xyz(), q.argb(), Math.floor(Math.sqrt(q.far()) * 20.0) * 1.0e6 - i));
        }
        list.clear();
        list.addAll(ordered);
        list.sort(Comparator.comparingDouble(Quad::far).reversed());
        for (Quad q : list) {
            FxPreview.tri(raster, eye, q.xyz(), q.argb(), 0, 1, 2);
            FxPreview.tri(raster, eye, q.xyz(), q.argb(), 0, 2, 3);
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, w, h, raster.rgb, 0, w);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.drawString(list.size() + " quads", 8, h - 8);
        g.dispose();
        return img;
    }

    static final int[][] FACES = {{0, 2, 6, 4}, {1, 3, 7, 5}, {0, 1, 5, 4}, {2, 3, 7, 6}, {0, 1, 3, 2}, {4, 5, 7, 6}};
    static final double[] SHADE = {0.7, 0.7, 1.0, 0.55, 0.85, 0.8};

    static void body(DragonPreview.Raster r, float[][] pose, boolean fp) {
        if (!fp) {
            double[] head = new double[24];
            double[] torso = new double[24];
            for (int k = 0; k < 8; k++) {
                double x = (k & 1) == 0 ? -4 : 4;
                double[] hp = StyleRig.head(pose, x, (k & 2) == 0 ? -8 : 0, (k & 4) == 0 ? -4 : 4);
                double[] tp = StyleRig.torso(pose, x, (k & 2) == 0 ? 0 : 12, (k & 4) == 0 ? -2 : 2);
                System.arraycopy(hp, 0, head, k * 3, 3);
                System.arraycopy(tp, 0, torso, k * 3, 3);
            }
            box(r, head, 0xD9A77E);
            box(r, torso, 0x3D8BC4);
        }
        int[] colors = {0, 0, 0xCD5C46, 0xF0A05A, 0x3C468C, 0x5F6EBE};
        for (int part = 2; part <= 5; part++) {
            box(r, StyleRig.half(pose, part, false, 0), colors[part]);
            box(r, StyleRig.half(pose, part, true, 0), colors[part]);
        }
    }

    static void box(DragonPreview.Raster r, double[] c, int color) {
        for (int f = 0; f < 6; f++) {
            int[] q = FACES[f];
            double[] a = {c[q[0] * 3], c[q[0] * 3 + 1], c[q[0] * 3 + 2]};
            double[] b = {c[q[1] * 3], c[q[1] * 3 + 1], c[q[1] * 3 + 2]};
            double[] d = {c[q[2] * 3], c[q[2] * 3 + 1], c[q[2] * 3 + 2]};
            double[] e = {c[q[3] * 3], c[q[3] * 3 + 1], c[q[3] * 3 + 2]};
            int col = DragonPreview.scale(color, (float) SHADE[f]);
            r.tri(a, b, d, null, null, null, col);
            r.tri(a, d, e, null, null, null, col);
        }
    }
}
