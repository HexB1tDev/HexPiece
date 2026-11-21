import com.hexhaki.piece.client.npc.StylePoser;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.pika.LightClone;
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
 * 2.9.0: the Light fruit's animations (tools/pika_anims.py, sampled as client/npc/StylePoser samples them: the keyframes PlayerAnimator
 * plays) on the posed player (style/StyleRig, flat colours: parts a file does not key drawn at rest), with Ama no Murakumo in the right
 * fist when {@code --sword} (pika/LightClone.sword: the grip the game uses), a dummy 2.4 blocks ahead. A row per animation, a column per
 * moment. Usage: PikaPreview out.png [--sword] [--view deg] [--frames n] [--to t] ANIMATION ...
 */
public final class PikaPreview {
    public static void main(String[] args) throws Exception {
        File out = new File(args[0]);
        boolean sword = false;
        double view = 55;
        int frames = 8;
        double to = 10;
        List<String> names = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--sword" -> sword = true;
                case "--view" -> view = Double.parseDouble(args[++i]);
                case "--frames" -> frames = Integer.parseInt(args[++i]);
                case "--to" -> to = Double.parseDouble(args[++i]);
                default -> names.add(args[i]);
            }
        }
        int cw = 300;
        int ch = 260;
        BufferedImage sheet = new BufferedImage(cw * frames + 4, (ch + 16) * names.size() + 4, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(0x202020));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        DragonPreview.texture = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        for (int r = 0; r < names.size(); r++) {
            g.setColor(Color.WHITE);
            g.drawString(names.get(r), 4, r * (ch + 16) + 13);
            for (int c = 0; c < frames; c++) {
                double t = to * c / Math.max(1, frames - 1);
                BufferedImage cell = render(names.get(r), sword, view, (float) t, cw * 2, ch * 2);
                g.drawImage(cell, c * cw + 2, r * (ch + 16) + 16, cw - 2, ch - 2, null);
                g.setColor(Color.BLACK);
                g.drawString(String.format("t%.1f", t), c * cw + 6, r * (ch + 16) + 30);
            }
        }
        ImageIO.write(sheet, "png", out);
    }

    record Quad(double[] xyz, int[] argb, double far) {
    }

    static BufferedImage render(String name, boolean sword, double view, float t, int w, int h) {
        double a = Math.toRadians(view);
        double[] target = {0, 1.1, 0.8};
        double[] eye = {Math.sin(a) * 4.6, 1.8, 0.8 - Math.cos(a) * 4.6};
        if (view < -900) {
            // from straight above (--view -999), the player's front up the picture
            target = new double[] {0, 0.0, 0.6};
            eye = new double[] {0.0, 7.5, 0.55};
        }
        DragonPreview.fov = 50;
        DragonPreview.Raster raster = new DragonPreview.Raster(w, h, eye, target);
        raster.ground(0);
        raster.figure(0, 0, 2.4);
        float[][] pose = StylePoser.sample(name, Math.max(0, t));
        StylePreview.body(raster, pose, false);
        List<Quad> list = new ArrayList<>();
        FireMesh.Sink sink = (xyz, argb) -> {
            double[] q = xyz.clone();
            double mx = (q[0] + q[3] + q[6] + q[9]) / 4.0;
            double my = (q[1] + q[4] + q[7] + q[10]) / 4.0;
            double mz = (q[2] + q[5] + q[8] + q[11]) / 4.0;
            list.add(new Quad(q, argb.clone(), mx * mx + my * my + mz * mz));
        };
        if (sword) {
            LightClone.sword(sink, pose, new StyleShow.Place(-eye[0], -eye[1], -eye[2], 0.0), 1.0, 1.0);
        }
        list.sort(Comparator.comparingDouble(Quad::far).reversed());
        for (Quad q : list) {
            FxPreview.tri(raster, eye, q.xyz(), q.argb(), 0, 1, 2);
            FxPreview.tri(raster, eye, q.xyz(), q.argb(), 0, 2, 3);
        }
        // the fist (a dot): where the hand closes
        double[] fist = StyleRig.limb(pose, StyleRig.RIGHT_ARM, 0.0, 0.967, 0.0);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, w, h, raster.rgb, 0, w);
        return img;
    }
}
