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

    /**
     * 2.9.6: Z held, {@code tt} ticks in: the light off where it began, down onto the ground (its middle 0.9 over it, as the body's),
     * reflected up off it, at PikaMoves.LIGHT_SPEED blocks a tick; its ribbon (16 ticks, as PikaFx keeps it), its head, the flash where
     * the body became light, the reflection where it met the ground.
     */
    static void zpath(FireMesh.Sink sink, double[] eye, double tt) {
        double[][] corners = {{-9, 3.4, 2}, {-5, 0.9, 8}, {7, 8.4, 26}};
        double speed = com.hexhaki.piece.server.PikaMoves.LIGHT_SPEED;
        double[] pts = new double[16 * 3];
        int count = 0;
        for (int i = 0; i < 16 && tt - i >= 0; i++) {
            double[] q = along(corners, (tt - i) * speed);
            pts[i * 3] = q[0] - eye[0];
            pts[i * 3 + 1] = q[1] - eye[1];
            pts[i * 3 + 2] = q[2] - eye[2];
            count++;
        }
        if (count >= 2) {
            com.hexhaki.piece.pika.LightArt.ribbon(sink, pts, count, 0.8, 1.0);
        }
        double[] h = along(corners, tt * speed);
        double[] h0 = along(corners, Math.max(0.0, tt * speed - 0.5));
        double wx = h[0] - h0[0];
        double wy = h[1] - h0[1];
        double wz = h[2] - h0[2];
        double wl = Math.max(1.0E-6, Math.sqrt(wx * wx + wy * wy + wz * wz));
        com.hexhaki.piece.pika.LightArt.head(sink, h[0] - eye[0], h[1] - eye[1], h[2] - eye[2], wx / wl, wy / wl, wz / wl, tt, 3);
        com.hexhaki.piece.pika.LightArt.flare(sink, corners[0][0] - eye[0], corners[0][1] - eye[1], corners[0][2] - eye[2], 1.6, tt, 4);
        double travelled = 0.0;
        for (int c = 1; c + 1 < corners.length; c++) {
            travelled += dist(corners[c - 1], corners[c]);
            double age = tt - travelled / speed;
            double[] in = unit(corners[c - 1], corners[c]);
            double[] out = unit(corners[c], corners[c + 1]);
            double[] n = {out[0] - in[0], out[1] - in[1], out[2] - in[2]};
            double nl = Math.sqrt(n[0] * n[0] + n[1] * n[1] + n[2] * n[2]);
            // where the light met the ground: under the body's middle
            com.hexhaki.piece.pika.LightArt.reflect(sink, corners[c][0] - eye[0], corners[c][1] - 0.88 - eye[1], corners[c][2] - eye[2], n[0] / nl,
                n[1] / nl, n[2] / nl, 1.0, age, 5 + c);
        }
    }

    static double dist(double[] a, double[] b) {
        return Math.sqrt((a[0] - b[0]) * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1]) + (a[2] - b[2]) * (a[2] - b[2]));
    }

    static double[] unit(double[] a, double[] b) {
        double d = dist(a, b);
        return new double[] {(b[0] - a[0]) / d, (b[1] - a[1]) / d, (b[2] - a[2]) / d};
    }

    static double[] along(double[][] corners, double s) {
        for (int c = 0; c + 1 < corners.length; c++) {
            double d = dist(corners[c], corners[c + 1]);
            if (s <= d || c + 2 == corners.length) {
                double f = Math.min(1.0, s / d);
                return new double[] {corners[c][0] + (corners[c + 1][0] - corners[c][0]) * f, corners[c][1] + (corners[c + 1][1] - corners[c][1]) * f,
                    corners[c][2] + (corners[c + 1][2] - corners[c][2]) * f};
            }
            s -= d;
        }
        return corners[corners.length - 1];
    }

    static BufferedImage render(String shot) {
        String[] halves = shot.split("\\|");
        String[] p = halves[0].split(":");
        String[] move = halves.length > 1 ? halves[1].split(":") : new String[] {"none"};
        DragonPreview.mouth = move[0].equals("breath") || move[0].equals("orb") || move[0].equals("roar") || move[0].equals("fbeam") ? 1.0F : 0.0F;
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
        // fp:pitch[:aimAhead]: first person, as the game draws it: the eye at 1.62 over the feet looking along +z, the own dragon not
        // drawn, the fire from under the middle of the view (UoFx.mouthWay) aimed onto the crosshair's point aimAhead blocks out
        // 2.9.6: e:yaw:pitch:dist:tx:ty:tz: the camera round a point, no dragon (the Light fruit's shots)
        boolean bare = p[0].equals("e");
        if (bare) {
            p = new String[] {"g", "2", "1.0", "0", "0", "0", p[1], p[2], p[3], p[4], p[5], p[6]};
            DragonPreview.hideDragon = true;
        }
        boolean fp = p[0].equals("fp");
        double fpAhead = fp && p.length > 2 ? Double.parseDouble(p[2]) : 30.0;
        if (fp) {
            double pitchFp = Double.parseDouble(p[1]);
            double pr = Math.toRadians(pitchFp);
            p = new String[] {"g", "2", "1.0", "0", p[1], "0", "180", p[1], "1", "0", String.valueOf(1.62 - Math.sin(pr)), String.valueOf(Math.cos(pr))};
            DragonPreview.hideDragon = true;
        }
        DragonPreview.Raster r = p[0].equals("v") ? DragonPreview.viewScene(p) : DragonPreview.scene(p);
        DragonPreview.hideDragon = false;
        if (fp) {
            double pr = Math.toRadians(Double.parseDouble(p[4]));
            double[] lk = {0, -Math.sin(pr), Math.cos(pr)};
            double[] up = {0, Math.cos(pr), Math.sin(pr)};
            DragonPreview.lastMouth = new double[] {lk[0] * 4.0 - up[0] * 1.5, 1.62 + lk[1] * 4.0 - up[1] * 1.5, lk[2] * 4.0 - up[2] * 1.5,
                lk[0], lk[1], lk[2]};
        }
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
        if (fp) {
            // the fire aimed from the mouth onto the crosshair's point (UoFx.aimed)
            double[] m0 = DragonPreview.lastMouth;
            double[] target = {r.eye[0] + look[0] * fpAhead, r.eye[1] + look[1] * fpAhead, r.eye[2] + look[2] * fpAhead};
            double[] w = {target[0] - m0[0], target[1] - m0[1], target[2] - m0[2]};
            double wl = Math.sqrt(w[0] * w[0] + w[1] * w[1] + w[2] * w[2]);
            look[0] = w[0] / wl;
            look[1] = w[1] / wl;
            look[2] = w[2] / wl;
        }
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
            // 2.7.8: the beams: fbeam:age:charge (the dragon's, UoFx's numbers), hbeam:age (the human's)
            case "fbeam" -> {
                double c = Double.parseDouble(move[2]);
                UoFire.fireBeam(sink, aim, Double.parseDouble(move[1]), 30 + 40 * c, 2.5 + 11.5 * c, 60 + 100 * c, 7);
            }
            case "hbeam" -> UoFire.fireBeam(sink, aim, Double.parseDouble(move[1]), 30, 1.4, 50, 7);
            case "blast", "bolt" -> {
                // a fireball out of the mouth along the look, {age} ticks after it left (3.6 / 3.2 blocks a tick, as UoMoves throws them)
                boolean big = move[0].equals("blast");
                double speed = big ? 3.6 : 3.2;
                double age = Double.parseDouble(move[1]);
                double size = big ? 1.6 + 2.6 * (move.length > 2 ? Double.parseDouble(move[2]) : 1.0) : 0.7;
                double end = move.length > 3 ? Double.parseDouble(move[3]) : Double.MAX_VALUE; // where it burst (blocks), for the line after
                double d = Math.min(speed * age, end);
                // 2.7.7: the line of fire along its way (UoFx: size x0.42 for the blast, x1 for Blast Breath; burns 12 / 14 ticks)
                UoFire.line(sink, mx, my, mz, look[0], look[1], look[2], speed, age, end, big ? size * 0.42 : size * 1.5, big ? 12.0 : 14.0, 0, 0, 0, 11);
                if (speed * age < end) {
                    UoFire.fireball(sink, mx + look[0] * d, my + look[1] * d, mz + look[2] * d, look[0] * speed, look[1] * speed, look[2] * speed,
                        big ? size * 0.55 : size * 1.25, age, d, big, 11);
                }
            }
            case "boom" -> {
                // an explosion {radius} on the ground {at} blocks ahead of the dragon, {age} ticks old
                double age = Double.parseDouble(move[1]);
                double radius = Double.parseDouble(move[2]);
                double at = move.length > 3 ? Double.parseDouble(move[3]) : 22.0;
                UoFire.explosion(sink, -eye[0], radius * 0.3 - eye[1], at - eye[2], radius, age, 5);
            }
            case "bomb" -> {
                // 2.9.0: Blast Breath's last explosion {radius} on the ground {at} blocks ahead, {age} ticks old
                double age = Double.parseDouble(move[1]);
                double radius = Double.parseDouble(move[2]);
                double at = move.length > 3 ? Double.parseDouble(move[3]) : 40.0;
                UoFire.bomb(sink, -eye[0], -eye[1], at - eye[2], radius, age, 5);
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
            // 2.7.8: the Light fruit (pika/LightArt), the player at the origin facing +z, the right hand at about (-0.35, 1.4, 0.5)
            case "laser" -> com.hexhaki.piece.pika.LightArt.laser(sink, -0.35 - eye[0], 1.45 - eye[1], 0.6 - eye[2], -eye[0] + 1.0, 1.0 - eye[1],
                Double.parseDouble(move[2]) - eye[2], 0.25, Double.parseDouble(move[1]), 3);
            case "charge" -> com.hexhaki.piece.pika.LightArt.charge(sink, -0.35 - eye[0], 1.45 - eye[1], 0.6 - eye[2], Double.parseDouble(move[1]),
                Double.parseDouble(move[2]), 3);
            case "beam" -> com.hexhaki.piece.pika.LightArt.beam(sink, -0.35 - eye[0], 1.45 - eye[1], 0.6 - eye[2], -eye[0], 1.0 - eye[1],
                Double.parseDouble(move[2]) - eye[2], 1.2, Double.parseDouble(move[1]), 3);
            case "lburst" -> com.hexhaki.piece.pika.LightArt.burst(sink, -eye[0], 1.0 - eye[1], 16.0 - eye[2], Double.parseDouble(move[2]),
                Double.parseDouble(move[1]), 3);
            case "bullets" -> {
                // a volley: beads from the crossed hands up in the air (y 6) raining onto the ground ahead
                double age = Double.parseDouble(move[1]);
                com.hexhaki.piece.pika.LightArt.crossed(sink, -eye[0], 6.4 - eye[1], 0.6 - eye[2], age, 3);
                for (int i = 0; i < 40; i++) {
                    double born = i * 0.5;
                    double a = age - born;
                    if (a < 0 || a > 6) {
                        continue;
                    }
                    double tx = (FireMesh.rand(5, i) - 0.5) * 22.0;
                    double tz = 8.0 + FireMesh.rand(6, i) * 18.0;
                    double f = a / 6.0;
                    double x = tx * f;
                    double y = 6.4 + (0.0 - 6.4) * f;
                    double z = 0.6 + (tz - 0.6) * f;
                    com.hexhaki.piece.pika.LightArt.bullet(sink, x - eye[0], y - eye[1], z - eye[2], tx / 6.0, -6.4 / 6.0, (tz - 0.6) / 6.0, 0.5, a, i);
                }
                for (int i = 0; i < 40; i++) {
                    double hit = i * 0.5 + 6.0;
                    double a = age - hit;
                    if (a >= 0 && a < 22) {
                        com.hexhaki.piece.pika.LightArt.burst(sink, (FireMesh.rand(5, i) - 0.5) * 22.0 - eye[0], 0.4 - eye[1],
                            8.0 + FireMesh.rand(6, i) * 18.0 - eye[2], 1.6, a, i);
                    }
                }
            }
            case "mirror" -> com.hexhaki.piece.pika.LightArt.mirror(sink, -eye[0], 1.3 - eye[1], 0.55 - eye[2], 0, 0, 1, Double.parseDouble(move[1]), 3);
            case "path" -> com.hexhaki.piece.pika.LightArt.path(sink, -eye[0], 1.3 - eye[1], 0.5 - eye[2], 4.0 - eye[0], 1.0 - eye[1], 24.0 - eye[2],
                Double.parseDouble(move[1]), 3);
            case "slash" -> com.hexhaki.piece.pika.LightArt.slash(sink, -eye[0], 1.2 - eye[1], 0.4 - eye[2], 0.3, 0.9, -0.3, 2.4, -2.2, 3.2,
                Double.parseDouble(move[1]));
            case "figure" -> {
                double t = Double.parseDouble(move[1]);
                for (int i = 0; i < 5; i++) {
                    com.hexhaki.piece.pika.LightArt.figure(sink, (i - 2) * 2.5 - eye[0], -eye[1], 6.0 + i % 2 * 2.0 - eye[2], Math.PI * (0.1 * (i - 2)),
                        t + i * 0.2, Math.sin(t * 3.0 + i), 1.0, i);
                }
            }
            case "blade" -> {
                // the sword held out: the hand's frame turned so the blade points ahead and up (as the arm holds it)
                double tilt = Math.toRadians(Double.parseDouble(move[1]));
                FireMesh.Sink turned = (xyz, argb) -> {
                    double[] q = new double[12];
                    for (int k = 0; k < 4; k++) {
                        double lx = xyz[k * 3];
                        double ly = xyz[k * 3 + 1];
                        double lz = xyz[k * 3 + 2];
                        // -Y (the blade) onto +z tilted up by tilt
                        double fy = -ly;
                        double wy = fy * Math.sin(tilt) + lz * Math.cos(tilt);
                        double wz = fy * Math.cos(tilt) - lz * Math.sin(tilt);
                        q[k * 3] = lx - 0.35 - eye[0];
                        q[k * 3 + 1] = wy + 1.1 - eye[1];
                        q[k * 3 + 2] = wz + 0.5 - eye[2];
                    }
                    sink.quad(q, argb);
                };
                com.hexhaki.piece.pika.LightArt.blade(turned, 2.6, 0.032, 0.5);
            }
            case "zpath" -> zpath(sink, eye, Double.parseDouble(move[1]));
            case "wave" -> {
                double age = Double.parseDouble(move[1]);
                com.hexhaki.piece.pika.LightArt.wave(sink, -eye[0], 1.0 - eye[1], 1.2 + 2.4 * age - eye[2], 0, 1, 3.0, 1.0 - age / 10.0);
            }
            case "rush" -> com.hexhaki.piece.pika.LightArt.rush(sink, look[0], look[1], look[2], Double.parseDouble(move[1]), 9);
            case "palm" -> {
                com.hexhaki.piece.pika.LightArt.palm(sink, -0.42 - eye[0], 1.6 - eye[1], 4.0 - eye[2], Double.parseDouble(move[1]), 1);
                com.hexhaki.piece.pika.LightArt.palm(sink, 0.42 - eye[0], 1.6 - eye[1], 4.0 - eye[2], Double.parseDouble(move[1]), 2);
            }
            case "star" -> com.hexhaki.piece.pika.LightArt.star(sink, -eye[0], 1.5 - eye[1], 6.0 - eye[2], Double.parseDouble(move[1]), 0.0, 1.0);
            case "reflect" -> com.hexhaki.piece.pika.LightArt.reflect(sink, -eye[0], 0.02 - eye[1], 10.0 - eye[2], 0, 1, 0, 1.0,
                Double.parseDouble(move[1]), 5);
            case "wallreflect" -> com.hexhaki.piece.pika.LightArt.reflect(sink, -eye[0], 1.5 - eye[1], 10.0 - eye[2], 0, 0, -1, 1.0,
                Double.parseDouble(move[1]), 5);
            case "flare" -> com.hexhaki.piece.pika.LightArt.flare(sink, -eye[0], 1.0 - eye[1], 8.0 - eye[2], 1.6, Double.parseDouble(move[1]), 5);
            case "head" -> {
                double tt = Double.parseDouble(move[1]);
                double[] pts = new double[16 * 3];
                for (int i = 0; i < 16; i++) {
                    pts[i * 3] = -3.0 + tt * 0.3 - i * 3.0 - eye[0];
                    pts[i * 3 + 1] = 1.5 - eye[1];
                    pts[i * 3 + 2] = 8.0 - eye[2];
                }
                com.hexhaki.piece.pika.LightArt.ribbon(sink, pts, 16, 0.8, 1.0);
                com.hexhaki.piece.pika.LightArt.head(sink, pts[0], pts[1], pts[2], 1, 0, 0, tt, 3);
            }
            case "trail" -> {
                double[] pts = new double[20 * 3];
                for (int i = 0; i < 20; i++) {
                    pts[i * 3] = Math.sin(i * 0.3) * 2.0 - eye[0];
                    pts[i * 3 + 1] = 2.0 + i * 0.15 - eye[1];
                    pts[i * 3 + 2] = 14.0 - i * 1.2 - eye[2];
                }
                com.hexhaki.piece.pika.LightArt.trail(sink, pts, 20, 0.5, 1.0);
            }
            default -> {
                if (!GuraShots.shot(sink, eye, move)) { // 3.1.1: the Gura fruit's (tools/preview/GuraShots)
                    if (!MaguShots.shot(sink, eye, move)) { // 3.1.3: the Magma fruit's (tools/preview/MaguShots)
                        if (!HieShots.shot(sink, eye, move)) { // 3.1.9: the Ice fruit's (tools/preview/HieShots)
                            NikaShots.shot(sink, eye, move); // 3.2.6: the Nika fruit's (tools/preview/NikaShots)
                        }
                    }
                }
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
