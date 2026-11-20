import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.dragon.UoFire;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 2.7.6: renders the Uo Uo no Mi's moves (dragon/UoFire, dragon/FireMesh) off-game over DragonPreview's scene, as the game draws them:
 * every quad sorted far to near by its middle's distance from the eye (the game's sort for a translucent type), blended over what is
 * there, tested against and writing the depth (RenderType debugQuads). Judge the frames against docs/handoff/uo-fire-target-*.jpg.
 *
 * <p>javac -cp build/classes -d build/preview tools/preview/DragonPreview.java tools/preview/FxPreview.java;
 * java -cp build/preview:build/classes FxPreview <seiryu.sry> <seiryu.png> <out.png or dir/> [--night] [--size=WxH] shot ...
 * a shot: {@code <DragonPreview v: or g: shot>|<move>:<args>}, e.g. {@code v:2:1.0:0:10:0|breath:8:-1}.
 */
public final class FxPreview {
    static boolean night;
    static int quads;

    public static void main(String[] args) throws Exception {
        try (FileInputStream in = new FileInputStream(args[0])) {
            DragonPreview.model = DragonModel.read(in);
        }
        DragonPreview.texture = ImageIO.read(new File(args[1]));
        List<String> shots = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            if (args[i].equals("--night")) {
                night = true;
            } else if (args[i].startsWith("--size=")) {
                String[] wh = args[i].substring(7).split("x");
                DragonPreview.W = Integer.parseInt(wh[0]);
                DragonPreview.H = Integer.parseInt(wh[1]);
            } else {
                shots.add(args[i]);
            }
        }
        if (args[2].endsWith("/")) {
            new File(args[2]).mkdirs();
            for (int i = 0; i < shots.size(); i++) {
                ImageIO.write(render(shots.get(i)), "png", new File(args[2], String.format("%04d.png", i)));
            }
            return;
        }
        int cols = Math.min(3, shots.size());
        int rows = (shots.size() + cols - 1) / cols;
        BufferedImage sheet = new BufferedImage(cols * DragonPreview.W, rows * DragonPreview.H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        for (int i = 0; i < shots.size(); i++) {
            g.drawImage(render(shots.get(i)), (i % cols) * DragonPreview.W, (i / cols) * DragonPreview.H, null);
        }
        g.dispose();
        ImageIO.write(sheet, "png", new File(args[2]));
    }

    /** One quad as the game gets it: 4 corners, 4 colours, its middle's distance from the eye. */
    record Quad(double[] xyz, int[] argb, double far) {
    }

    static BufferedImage render(String shot) {
        String[] halves = shot.split("\\|");
        String[] p = halves[0].split(":");
        String[] move = halves.length > 1 ? halves[1].split(":") : new String[] {"none"};
        DragonPreview.mouth = move[0].equals("breath") || move[0].equals("orb") || move[0].equals("roar") ? 1.0F : 0.0F;
        DragonPreview.bite = move[0].equals("bite") ? Float.parseFloat(move[1]) : -1.0F;
        if (p[0].equals("m")) {
            // a close-up of the mouth: m:phase:seconds:flight:look:bank:camYaw:camPitch:dist (the camera aimed at the mouth)
            String[] g = {"g", p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8]};
            DragonPreview.scene(g);
            double[] m = DragonPreview.lastMouth;
            p = new String[] {"g", p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], String.valueOf(m[0]), String.valueOf(m[1]), String.valueOf(m[2])};
            DragonModel md = DragonPreview.model;
            int jaw = md.bone("jaw");
            System.out.printf("mouth %.2f %.2f %.2f way %.2f %.2f %.2f (jaw's parent %s)%n", m[0], m[1], m[2], m[3], m[4], m[5],
                md.names[md.parent[jaw]]);
        }
        DragonPreview.Raster r = p[0].equals("v") ? DragonPreview.viewScene(p) : DragonPreview.scene(p);
        if (night) {
            for (int i = 0; i < r.rgb.length; i++) {
                if (r.depth[i] == Float.MAX_VALUE) {
                    int y = i / r.w;
                    r.rgb[i] = DragonPreview.mix(0x1A1030, 0x3B2A5E, y / (float) r.h);
                }
            }
        }
        double pitch = Math.toRadians(Double.parseDouble(p[4]));
        double[] look = {0, -Math.sin(pitch), Math.cos(pitch)};
        double[] mouth = DragonPreview.lastMouth != null ? DragonPreview.lastMouth : new double[] {0, 6, 4};
        double[] eye = r.eye;
        List<Quad> list = new ArrayList<>();
        FireMesh.Sink sink = (xyz, argb) -> {
            double[] q = xyz.clone();
            double mx = (q[0] + q[3] + q[6] + q[9]) / 4.0;
            double my = (q[1] + q[4] + q[7] + q[10]) / 4.0;
            double mz = (q[2] + q[5] + q[8] + q[11]) / 4.0;
            list.add(new Quad(q, argb.clone(), mx * mx + my * my + mz * mz));
        };
        double mx = mouth[0] - eye[0];
        double my = mouth[1] - eye[1];
        double mz = mouth[2] - eye[2];
        double[] way = mouth.length >= 6 ? new double[] {mouth[3], mouth[4], mouth[5]} : look;
        UoFire.Aim aim = (ago, out) -> {
            out[0] = mx;
            out[1] = my;
            out[2] = mz;
            out[3] = look[0];
            out[4] = look[1];
            out[5] = look[2];
            out[6] = way[0];
            out[7] = way[1];
            out[8] = way[2];
        };
        List<double[]> dots = new ArrayList<>();
        switch (move[0]) {
            case "breath" -> {
                double on = Double.parseDouble(move[1]);
                double off = move.length > 2 ? Double.parseDouble(move[2]) : -1.0;
                UoFire.breath(sink, aim, on, off, 35.0, 7);
                for (int i = 0; i < 70; i++) {
                    double d = FireMesh.rand(99, i) * 28.0 + 5.0;
                    double s = d * 0.3 + 1.5;
                    dots.add(new double[] {mx + look[0] * d + (FireMesh.rand(98, i) - 0.5) * s * 2, my + look[1] * d + (FireMesh.rand(97, i) - 0.5) * s * 2,
                        mz + look[2] * d + (FireMesh.rand(96, i) - 0.5) * s * 2, 0.06 + 0.06 * FireMesh.rand(95, i)});
                }
            }
            case "orb" -> UoFire.orb(sink, aim, Double.parseDouble(move[1]), Double.parseDouble(move[2]), 7);
            case "blast", "bolt" -> {
                // a fireball out of the mouth along the look, {age} ticks after it left (3.6 / 3.2 blocks a tick, as UoMoves throws them)
                boolean big = move[0].equals("blast");
                double speed = big ? 3.6 : 3.2;
                double age = Double.parseDouble(move[1]);
                double size = big ? 1.6 + 2.6 * (move.length > 2 ? Double.parseDouble(move[2]) : 1.0) : 0.7;
                double d = speed * age;
                UoFire.fireball(sink, mx + look[0] * d, my + look[1] * d, mz + look[2] * d, look[0] * speed, look[1] * speed, look[2] * speed,
                    big ? size * 0.55 : size, age, d, big, 11);
            }
            case "boom" -> {
                // an explosion {radius} on the ground {at} blocks ahead of the dragon, {age} ticks old
                double age = Double.parseDouble(move[1]);
                double radius = Double.parseDouble(move[2]);
                double at = move.length > 3 ? Double.parseDouble(move[3]) : 22.0;
                UoFire.explosion(sink, -eye[0], radius * 0.3 - eye[1], at - eye[2], radius, age, 5);
            }
            case "air" -> {
                double age = Double.parseDouble(move[1]);
                double d = 4.2 * age;
                UoFire.airShot(sink, mx + look[0] * d, my + look[1] * d, mz + look[2] * d, look[0] * 4.2, look[1] * 4.2, look[2] * 4.2, 1.3, age, d, 3);
            }
            case "airburst" -> UoFire.airBurst(sink, -eye[0], 1.5 - eye[1], 16.0 - eye[2], 0, 0, -1, 2.0, Double.parseDouble(move[1]), 3);
            case "tornado" -> UoFire.tornado(sink, -eye[0], -eye[1], 14.0 - eye[2], Double.parseDouble(move[1]),
                move.length > 2 ? Double.parseDouble(move[2]) : 1.0, 9);
            case "bite" -> UoFire.bite(sink, aim, Double.parseDouble(move[1]), 4);
            default -> {
            }
        }
        quads = list.size();
        list.sort((a, b) -> Double.compare(b.far, a.far));
        for (Quad q : list) {
            double[] v = q.xyz;
            tri(r, eye, v, q.argb, 0, 1, 2);
            tri(r, eye, v, q.argb, 0, 2, 3);
        }
        for (double[] d : dots) {
            glow(r, eye, d);
        }
        BufferedImage img = r.image(DragonPreview.W, DragonPreview.H);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(night ? Color.WHITE : Color.BLACK);
        g.drawString(halves.length > 1 ? halves[1] + "  (" + quads + " quads)" : DragonPreview.label, 6, 15);
        g.dispose();
        return img;
    }

    /** An ember as Photon draws one: a glowing dot (added), {@code d[3]} blocks across. */
    static void glow(DragonPreview.Raster r, double[] eye, double[] d) {
        double[] c = r.project(d[0] + eye[0], d[1] + eye[1], d[2] + eye[2]);
        if (c == null || c[2] >= r.depth[(int) Math.max(0, Math.min(r.h - 1, c[1])) * r.w + (int) Math.max(0, Math.min(r.w - 1, c[0]))]) {
            return;
        }
        double rad = Math.max(1.5, d[3] / c[2] * r.focal);
        for (int y = (int) (c[1] - rad * 3); y <= c[1] + rad * 3; y++) {
            for (int x = (int) (c[0] - rad * 3); x <= c[0] + rad * 3; x++) {
                if (x < 0 || y < 0 || x >= r.w || y >= r.h) {
                    continue;
                }
                double e = Math.hypot(x - c[0], y - c[1]) / rad;
                double a = e < 1.0 ? 1.0 : Math.exp(-(e - 1.0) * 2.2) * 0.7;
                int i = y * r.w + x;
                int core = e < 0.6 ? 0xFFF4C0 : 0xFF8A2A;
                r.rgb[i] = blend(r.rgb[i], core, a);
            }
        }
    }

    static int blend(int dst, int src, double a) {
        double u = Math.max(0.0, Math.min(1.0, a));
        int rr = (int) (((src >> 16) & 255) * u + ((dst >> 16) & 255) * (1 - u));
        int gg = (int) (((src >> 8) & 255) * u + ((dst >> 8) & 255) * (1 - u));
        int bb = (int) ((src & 255) * u + (dst & 255) * (1 - u));
        return rr << 16 | gg << 8 | bb;
    }

    /** One triangle of a quad: blended, depth tested, depth written (debugQuads). */
    static void tri(DragonPreview.Raster r, double[] eye, double[] v, int[] c, int ia, int ib, int ic) {
        double[] pa = r.project(v[ia * 3] + eye[0], v[ia * 3 + 1] + eye[1], v[ia * 3 + 2] + eye[2]);
        double[] pb = r.project(v[ib * 3] + eye[0], v[ib * 3 + 1] + eye[1], v[ib * 3 + 2] + eye[2]);
        double[] pc = r.project(v[ic * 3] + eye[0], v[ic * 3 + 1] + eye[1], v[ic * 3 + 2] + eye[2]);
        if (pa == null || pb == null || pc == null) {
            return;
        }
        double area = (pb[0] - pa[0]) * (pc[1] - pa[1]) - (pb[1] - pa[1]) * (pc[0] - pa[0]);
        if (Math.abs(area) < 1e-9) {
            return;
        }
        int x0 = (int) Math.max(0, Math.floor(Math.min(pa[0], Math.min(pb[0], pc[0]))));
        int x1 = (int) Math.min(r.w - 1, Math.ceil(Math.max(pa[0], Math.max(pb[0], pc[0]))));
        int y0 = (int) Math.max(0, Math.floor(Math.min(pa[1], Math.min(pb[1], pc[1]))));
        int y1 = (int) Math.min(r.h - 1, Math.ceil(Math.max(pa[1], Math.max(pb[1], pc[1]))));
        int ca = c[ia];
        int cb = c[ib];
        int cc = c[ic];
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                double px = x + 0.5;
                double py = y + 0.5;
                double w0 = ((pb[0] - px) * (pc[1] - py) - (pb[1] - py) * (pc[0] - px)) / area;
                double w1 = ((pc[0] - px) * (pa[1] - py) - (pc[1] - py) * (pa[0] - px)) / area;
                double w2 = 1 - w0 - w1;
                if (w0 < 0 || w1 < 0 || w2 < 0) {
                    continue;
                }
                double iz = w0 / pa[2] + w1 / pb[2] + w2 / pc[2];
                double z = 1 / iz;
                int i = y * r.w + x;
                if (z > r.depth[i]) {
                    continue;
                }
                // the colour across the triangle as GL interpolates it (perspective-correct)
                double a0 = w0 / pa[2] * z;
                double a1 = w1 / pb[2] * z;
                double a2 = w2 / pc[2] * z;
                double al = (((ca >>> 24) & 255) * a0 + ((cb >>> 24) & 255) * a1 + ((cc >>> 24) & 255) * a2) / 255.0;
                double rr = ((ca >> 16) & 255) * a0 + ((cb >> 16) & 255) * a1 + ((cc >> 16) & 255) * a2;
                double gg = ((ca >> 8) & 255) * a0 + ((cb >> 8) & 255) * a1 + ((cc >> 8) & 255) * a2;
                double bb = (ca & 255) * a0 + (cb & 255) * a1 + (cc & 255) * a2;
                int src = (int) rr << 16 | (int) gg << 8 | (int) bb;
                r.rgb[i] = blend(r.rgb[i], src, al);
                r.depth[i] = (float) z;
            }
        }
    }
}
