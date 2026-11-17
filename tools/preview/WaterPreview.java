import com.hexhaki.piece.water.WaterMesh;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 2.3.8: renders the game's water bodies ({@link WaterMesh}) off-game to judge them (the house rule), over a picture of the view, as the
 * game draws them: each body's triangles back to front, alpha blended, their points' colours mixed across them; bodies far to near.
 * The camera looks down -z from the origin with the game's 70 degree field of view.
 *
 * <p>javac -cp build/classes -d build/preview tools/preview/WaterPreview.java; java -cp build/preview:build/classes WaterPreview
 * BACKGROUND.png OUT.png SCENE (drops | flight | bubble | close) [time]
 */
public final class WaterPreview {
    public static void main(String[] args) throws Exception {
        BufferedImage background = ImageIO.read(new File(args[0]));
        String scene = args[2];
        double time = args.length > 3 ? Double.parseDouble(args[3]) : 0.0;
        List<WaterMesh.Body> bodies = new ArrayList<>();
        List<WaterMesh.Ring> rings = new ArrayList<>();
        switch (scene) {
            case "drops" -> {
                // five drops in a flat arch over a raised fist, the fist 0.55 above the eye, 2.6 ahead
                for (int i = 0; i < 5; i++) {
                    double a = Math.toRadians(-50 + 25 * i);
                    bodies.add(new WaterMesh.Body(Math.sin(a) * 0.55, 0.7 + Math.cos(a) * 0.32, -2.6, 0.13, null, 1.0, 0.07, time + i * 3, i,
                        WaterMesh.DROP));
                }
            }
            case "flight" -> {
                for (int i = 0; i < 5; i++) {
                    double[] along = {-0.35 + 0.17 * i, 0.05, 1.0};
                    bodies.add(new WaterMesh.Body(-0.9 + 0.45 * i, 0.1 * (i % 2), -3.5 - 0.6 * i, 0.13, along, 2.4, 0.05, time + i, i,
                        WaterMesh.DROP));
                }
            }
            case "bullet" -> bullet(bodies, time);
            case "shock", "shockside" -> {
                shock(rings, time, scene.equals("shockside"));
            }
            case "bubble" -> bodies.add(new WaterMesh.Body(0.0, -0.1, -4.2, 1.3, null, 1.0, 0.03, time, 3, WaterMesh.PRISON));
            default -> bodies.add(new WaterMesh.Body(0.0, 0.0, -0.75, 0.16, null, 1.0, 0.07, time, 5, WaterMesh.DROP));
        }
        bodies.sort((p, q) -> Double.compare(q.z() * q.z() + q.x() * q.x(), p.z() * p.z() + p.x() * p.x()));
        int w = background.getWidth();
        int h = background.getHeight();
        float[][] rgb = new float[3][w * h];
        for (int i = 0; i < w * h; i++) {
            int c = background.getRGB(i % w, i / w);
            rgb[0][i] = ((c >> 16) & 255) / 255.0F;
            rgb[1][i] = ((c >> 8) & 255) / 255.0F;
            rgb[2][i] = (c & 255) / 255.0F;
        }
        double focal = h * 0.5 / Math.tan(Math.toRadians(35.0));
        int[] count = {0};
        WaterMesh.Sink draw = (xyz, argb) -> {
            double[] sx = new double[3];
            double[] sy = new double[3];
            for (int k = 0; k < 3; k++) {
                double z = -xyz[k * 3 + 2];
                if (z < 0.05) {
                    return;
                }
                sx[k] = w * 0.5 + xyz[k * 3] / z * focal;
                sy[k] = h * 0.5 - xyz[k * 3 + 1] / z * focal;
            }
            count[0]++;
            tri(rgb, w, h, sx, sy, argb);
        };
        rings.sort((p, q) -> Double.compare(q.z() * q.z() + q.x() * q.x(), p.z() * p.z() + p.x() * p.x()));
        for (WaterMesh.Ring ring : rings) {
            WaterMesh.ring(ring, 40, 10, draw);
        }
        for (WaterMesh.Body body : bodies) {
            WaterMesh.body(body, (xyz, argb) -> {
                double[] sx = new double[3];
                double[] sy = new double[3];
                for (int k = 0; k < 3; k++) {
                    double z = -xyz[k * 3 + 2];
                    if (z < 0.05) {
                        return;
                    }
                    sx[k] = w * 0.5 + xyz[k * 3] / z * focal;
                    sy[k] = h * 0.5 - xyz[k * 3 + 1] / z * focal;
                }
                count[0]++;
                tri(rgb, w, h, sx, sy, argb);
            });
        }
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < w * h; i++) {
            int r = Math.round(Math.max(0, Math.min(1, rgb[0][i])) * 255);
            int g = Math.round(Math.max(0, Math.min(1, rgb[1][i])) * 255);
            int b = Math.round(Math.max(0, Math.min(1, rgb[2][i])) * 255);
            image.setRGB(i % w, i / w, r << 16 | g << 8 | b);
        }
        ImageIO.write(image, "png", new File(args[1]));
        System.out.println(scene + ": " + count[0] + " triangles");
    }

    /**
     * The Water Bullet as the game lays it ({@code com.hexhaki.piece.water.WaterBullet}) on the figure in the backdrop (its feet 2.43
     * blocks ahead of the eye and 1.25 below it, facing the eye: its right is the picture's left): the drops gathering over its raised
     * fist out of a mist drawn in round them, then fired at a foe just left of the eye, turning after it.
     */
    static void bullet(List<WaterMesh.Body> bodies, double age) {
        double[] feet = {0.0, -1.25, -2.43};
        java.util.function.Function<double[], double[]> place = local -> new double[] {feet[0] - local[0], feet[1] + local[1], feet[2] + local[2]};
        java.util.Random random = new java.util.Random(7);
        for (int i = 0; i < com.hexhaki.piece.water.WaterBullet.COUNT; i++) {
            double grown = com.hexhaki.piece.water.WaterBullet.grown(i, age);
            int fire = com.hexhaki.piece.water.WaterBullet.fireTick(i);
            double[] p = place.apply(com.hexhaki.piece.water.WaterBullet.formation(i));
            if (age < fire) {
                if (grown > 0.02) {
                    bodies.add(new WaterMesh.Body(p[0], p[1], p[2], com.hexhaki.piece.water.WaterBullet.RADIUS * grown, null, 1.0,
                        0.12 * (1.2 - grown), age, i, WaterMesh.DROP));
                }
                if (age >= com.hexhaki.piece.water.WaterBullet.gathers(i) && grown < 1.0) {
                    // the mist drawn in: motes on their way in, nearer the more of their life is gone
                    for (int k = 0; k < 9; k++) {
                        double[] off = {random.nextGaussian(), random.nextGaussian(), random.nextGaussian()};
                        double l = Math.sqrt(off[0] * off[0] + off[1] * off[1] + off[2] * off[2]);
                        double out = (0.45 + random.nextDouble() * 0.5) * (1.0 - random.nextDouble() * 0.8);
                        bodies.add(new WaterMesh.Body(p[0] + off[0] / l * out, p[1] + off[1] / l * out, p[2] + off[2] / l * out, 0.016, null,
                            1.0, 0.0, 0, k, 0.9));
                    }
                }
                continue;
            }
            // in flight: from its place, fired at a foe just left of the eye and turning after it
            double[] at = p.clone();
            double[] target = {-0.8, -0.3, 0.6};
            double[] v = com.hexhaki.piece.water.WaterBullet.steer(new double[] {0, 0.1, 1}, new double[] {0, 0.1, 1});
            double flown = age - fire;
            for (int t = 0; t < (int) flown; t++) {
                v = com.hexhaki.piece.water.WaterBullet.steer(v, new double[] {target[0] - at[0], target[1] - at[1], target[2] - at[2]});
                at = new double[] {at[0] + v[0], at[1] + v[1], at[2] + v[2]};
            }
            if (at[2] > 0.3) {
                continue; // past the eye
            }
            bodies.add(new WaterMesh.Body(at[0], at[1], at[2], com.hexhaki.piece.water.WaterBullet.RADIUS, v, 2.3, 0.05, age, i,
                WaterMesh.DROP));
        }
    }

    /**
     * The Brick Fist's shockwave ({@code com.hexhaki.piece.water.BrickFist}) {@code age} ticks after the strike: from behind the figure
     * in the backdrop (it punches away from the eye, its fist 1.4 above its feet), or from the side (racing left to right across).
     */
    static void shock(List<WaterMesh.Ring> rings, double age, boolean side) {
        double[] from = side ? new double[] {-3.5, -0.3, -6.0} : new double[] {0.25, 0.25, -2.43 - com.hexhaki.piece.water.BrickFist.FIST};
        double[] axis = side ? new double[] {1, 0, 0} : new double[] {0, 0.05, -1};
        for (int k = 0; k < com.hexhaki.piece.water.BrickFist.rings(); k++) {
            double d = com.hexhaki.piece.water.BrickFist.travelled(k, age);
            if (d < 0) {
                continue;
            }
            double left = com.hexhaki.piece.water.BrickFist.left(d);
            rings.add(new WaterMesh.Ring(from[0] + axis[0] * d, from[1] + axis[1] * d, from[2] + axis[2] * d, axis,
                com.hexhaki.piece.water.BrickFist.major(k, d), com.hexhaki.piece.water.BrickFist.minor(k, d), 0.12, age, k,
                WaterMesh.PRISON * left));
        }
    }

    static void tri(float[][] rgb, int w, int h, double[] x, double[] y, int[] c) {
        double area = (x[1] - x[0]) * (y[2] - y[0]) - (x[2] - x[0]) * (y[1] - y[0]);
        if (Math.abs(area) < 1.0E-9) {
            return;
        }
        int minX = (int) Math.max(0, Math.floor(Math.min(x[0], Math.min(x[1], x[2]))));
        int maxX = (int) Math.min(w - 1, Math.ceil(Math.max(x[0], Math.max(x[1], x[2]))));
        int minY = (int) Math.max(0, Math.floor(Math.min(y[0], Math.min(y[1], y[2]))));
        int maxY = (int) Math.min(h - 1, Math.ceil(Math.max(y[0], Math.max(y[1], y[2]))));
        for (int py = minY; py <= maxY; py++) {
            for (int px = minX; px <= maxX; px++) {
                double sx = px + 0.5;
                double sy = py + 0.5;
                double a = ((x[1] - sx) * (y[2] - sy) - (x[2] - sx) * (y[1] - sy)) / area;
                double b = ((x[2] - sx) * (y[0] - sy) - (x[0] - sx) * (y[2] - sy)) / area;
                double g = 1.0 - a - b;
                if (a < 0 || b < 0 || g < 0) {
                    continue;
                }
                int i = py * w + px;
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
