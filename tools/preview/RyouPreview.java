import com.hexhaki.piece.haki.ArmBend;
import com.hexhaki.piece.haki.FistAura;
import com.hexhaki.piece.haki.HandCoat;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 2.5.3: renders Ryou's fist off-game to judge it against the owner's target (docs/handoff/ryou-fist-target.jpg). The arm is the
 * player model's own (the arm box and its sleeve layer, every skin texel a quad, bent at the elbow by the game's ArmBend, lit as the
 * game lights entities), its skin blackened by the game's HandCoat (with its gloss); the aura is the game's own FistAura, rasterised as
 * the game draws it (RenderType.entityShadow: alpha blended per vertex colour, depth tested against the arm and body, no depth written).
 *
 * <p>javac -cp build/classes -d build/preview tools/preview/RyouPreview.java; java -cp build/preview:build/classes RyouPreview out-dir
 */
public final class RyouPreview {
    static final int SS = 2;
    static final double K = 0.9375 / 16.0;

    /** A shot: the arm's pose, the camera, the background. */
    record Shot(String label, double xRot, double yRot, double zRot, float bend, double[] eye, double[] look, double fov, boolean night,
                boolean slim, int colour, float age, float punch, boolean ortho) {
    }

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "build/ryou-preview");
        dir.mkdirs();
        int red = 0xFF2B3D;
        int blue = 0x2E7BFF;
        // the shoulder (right arm pivot) in the world: x -5 px (flipped: +), y 22 px up from the neck line
        double[] hand = handWorld(-Math.PI / 2, 0, 0, 0);
        List<Shot> shots = new ArrayList<>();
        shots.add(new Shot("picture angle: punch toward the eye, 3/4", -Math.PI / 2, 0.15, 0.0, -0.1F,
            add(hand, new double[] {-0.55, 0.12, -0.75}), hand, 60, true, false, red, 12.0F, 0.0F, false));
        shots.add(new Shot("punch, side", -Math.PI / 2, 0.0, 0.0, 0.0F, add(hand, new double[] {-1.4, 0.05, 0.25}),
            add(hand, new double[] {0, 0, 0.3}), 60, true, false, red, 17.0F, 0.0F, false));
        double[] up = handWorld(-1.25, 0.2, 0.1, -1.4F);
        shots.add(new Shot("elbow bent 80, fist up", -1.25, 0.2, 0.1, -1.4F, add(up, new double[] {-1.2, 0.2, -1.2}), up, 60, true, false,
            blue, 23.0F, 0.0F, false));
        double[] fp = handWorld(-1.45, -0.15, 0.0, -0.25F);
        double[] shoulder = model(new double[] {-5, 2, 0});
        shots.add(new Shot("first person (behind the shoulder)", -1.45, -0.15, 0.0, -0.25F, add(shoulder, new double[] {0.25, 0.32, 0.42}),
            add(fp, new double[] {0.1, 0.1, -0.6}), 70, false, false, red, 31.0F, 0.6F, false));
        double[] hang = handWorld(0, 0, 0.05, 0);
        shots.add(new Shot("hanging, daylight, 3 blocks", 0, 0, 0.05, 0.0F, add(hang, new double[] {-1.8, 0.6, -2.4}), add(hang,
            new double[] {0.2, 0.2, 0}), 70, false, true, blue, 40.0F, 0.0F, false));
        shots.add(new Shot("8 blocks away", -Math.PI / 2, 0.15, 0.0, -0.1F, add(hand, new double[] {-4.5, 1.0, -6.6}), add(hand,
            new double[] {0.3, -0.3, 0.4}), 70, true, false, red, 50.0F, 0.0F, false));
        shots.add(new Shot("inventory (orthographic)", 0.2, 0, 0.05, 0.0F, new double[] {0, 1.0, -5}, new double[] {0, 1.0, 0}, 0, false,
            false, red, 60.0F, 0.0F, true));
        int w = 420;
        int h = 320;
        BufferedImage sheet = new BufferedImage(w * 4, (h + 18) * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(16, 16, 20));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        for (int i = 0; i < shots.size(); i++) {
            BufferedImage image = render(shots.get(i), w, h);
            int x = (i % 4) * w;
            int y = (i / 4) * (h + 18);
            g.drawImage(image, x, y + 18, null);
            g.setColor(new Color(225, 225, 225));
            g.drawString(shots.get(i).label(), x + 6, y + 13);
            ImageIO.write(image, "png", new File(dir, "shot-" + i + ".png"));
        }
        // the target, scaled into the last cell, to compare
        File target = new File("docs/handoff/ryou-fist-target.jpg");
        if (target.isFile()) {
            BufferedImage t = ImageIO.read(target);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(t, 3 * w, h + 18 + 18, w, w * t.getHeight() / t.getWidth(), null);
            g.setColor(new Color(225, 225, 225));
            g.drawString("the owner's target", 3 * w + 6, h + 18 + 13);
        }
        ImageIO.write(sheet, "png", new File(dir, "sheet.png"));
        // the flame over time, the picture's angle: a strip of frames a tick apart
        Shot base = shots.get(0);
        BufferedImage strip = new BufferedImage(260 * 6, 220, BufferedImage.TYPE_INT_RGB);
        Graphics2D sg = strip.createGraphics();
        for (int f = 0; f < 6; f++) {
            Shot s = new Shot(base.label(), base.xRot(), base.yRot(), base.zRot(), base.bend(), base.eye(), base.look(), base.fov(), true,
                false, red, 12.0F + f * 1.0F, 0.0F, false);
            sg.drawImage(render(s, 260, 220), f * 260, 0, null);
        }
        ImageIO.write(strip, "png", new File(dir, "flow.png"));
        // a big close-up next to the target
        BufferedImage big = render(new Shot("close", base.xRot(), base.yRot(), base.zRot(), base.bend(), add(hand, new double[] {-0.42, 0.1,
            -0.55}), add(hand, new double[] {0.02, 0.0, 0.08}), 60, true, false, red, 12.0F, 0.0F, false), 700, 394);
        ImageIO.write(big, "png", new File(dir, "close.png"));
        BufferedImage day = render(new Shot("close, day", base.xRot(), base.yRot(), base.zRot(), base.bend(), add(hand, new double[] {-0.42,
            0.1, -0.55}), add(hand, new double[] {0.02, 0.0, 0.08}), 60, false, false, blue, 20.0F, 1.0F, false), 700, 394);
        ImageIO.write(day, "png", new File(dir, "close-day.png"));
        System.out.println("wrote " + dir);
    }

    // --------------------------------------------------------------------------------------------------------------- the player

    /** Arm space (blocks) to the world, for the right arm in this pose. */
    static double[] world(double[][] r, double[] armBlocks) {
        double[] px = {armBlocks[0] * 16, armBlocks[1] * 16, armBlocks[2] * 16};
        double[] model = add(new double[] {-5, 2, 0}, mul(r, px));
        return model(model);
    }

    /** Model space (pixels, y down) to the world (blocks, feet at the origin, facing -z... the model's front, flipped as the renderer does). */
    static double[] model(double[] m) {
        return new double[] {-m[0] * K, 24 * K - m[1] * K + 1.501 * 0.9375 - 24 * K, m[2] * K};
    }

    static double[] handWorld(double xr, double yr, double zr, float bend) {
        double[][] r = rot(xr, yr, zr);
        float[] p = ArmBend.bent(0, bend, -3, 1, -1.0F / 16, 8.5F / 16, 0);
        return world(r, new double[] {p[0], p[1], p[2]});
    }

    /** ModelPart's rotation: Rz Ry Rx. */
    static double[][] rot(double x, double y, double z) {
        double[][] rx = {{1, 0, 0}, {0, Math.cos(x), -Math.sin(x)}, {0, Math.sin(x), Math.cos(x)}};
        double[][] ry = {{Math.cos(y), 0, Math.sin(y)}, {0, 1, 0}, {-Math.sin(y), 0, Math.cos(y)}};
        double[][] rz = {{Math.cos(z), -Math.sin(z), 0}, {Math.sin(z), Math.cos(z), 0}, {0, 0, 1}};
        return matmul(rz, matmul(ry, rx));
    }

    /** A Steve-like 64 x 64 skin: a teal shirt down to the elbow, the hand bare; then blackened by HandCoat at full Haki. */
    static int[] skin(boolean slim) {
        int[] px = new int[64 * 64];
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int base;
                boolean armRight = x >= 40 && x < 56 && y >= 16 && y < 32;
                boolean armLeft = x >= 32 && x < 48 && y >= 48;
                boolean sleeve = x >= 40 && x < 56 && y >= 32 && y < 48 || x >= 48 && x < 64 && y >= 48;
                if (sleeve) {
                    base = 0; // no sleeve layer
                } else if (armRight || armLeft) {
                    int row = y - (armRight ? 16 : 48) - 4;
                    boolean shirt = row >= 0 && row < 4 || row < 0;
                    int n = ((x * 7 + y * 13) % 5) * 3;
                    base = shirt ? rgb(0x00A8A8 - n * 0x000101) : rgb(0xC08A6E - n * 0x010101);
                } else {
                    base = rgb(0x00A8A8);
                }
                px[y * 64 + x] = base;
            }
        }
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                float amount = HandCoat.at(x, y, 64, 64, slim, 1.0F);
                if (amount > 0) {
                    px[y * 64 + x] = HandCoat.finish(px[y * 64 + x], x, y, 64, 64, slim, amount);
                }
            }
        }
        return px;
    }

    static int rgb(int c) {
        return 0xFF000000 | c;
    }

    // --------------------------------------------------------------------------------------------------------------- the render

    static BufferedImage render(Shot s, int w, int h) {
        Raster r = new Raster(w * SS, h * SS, s);
        double[][] rot = rot(s.xRot(), s.yRot(), s.zRot());
        boolean slim = s.slim();
        float half = slim ? 1.5F : 2.0F;
        float middle = slim ? -0.5F : -1.0F;
        float minX = middle - half;
        float maxX = middle + half;
        int[] skin = skin(slim);
        // the body (torso and head), flat: it hides what is behind it (not in first person: the game draws only the arm)
        if (!s.label().startsWith("first")) r.box(new double[] {-4, 0, -2}, new double[] {4, 12, 2}, 0xFF00A0A0);
        if (!s.label().startsWith("first")) {
            r.box(new double[] {-4, -8, -4}, new double[] {4, 0, 4}, 0xFFB58466);
            r.box(new double[] {-4, 12, -2}, new double[] {4, 24, 2}, 0xFF3A3AA0);
        }
        // the arm: the base box and its sleeve layer, each texel a quad
        r.cube(rot, s.bend(), minX, maxX, 40, 16, slim ? 3 : 4, 0.0, skin);
        r.cube(rot, s.bend(), minX, maxX, 40, 32, slim ? 3 : 4, 0.25, skin);
        // the aura, as RyouAura sends it
        double[] view;
        if (s.ortho()) {
            double[] toViewer = sub(s.eye(), s.look());
            double[] d = armDirection(rot, norm(toViewer));
            view = new double[] {d[0], d[1], d[2], 0};
        } else {
            double[] e = armPoint(rot, s.eye());
            view = new double[] {e[0], e[1], e[2], 1};
        }
        FistAura.build(0.0F, s.bend(), slim, true, view, false, s.age(), 7 * 31 + 1, s.colour(), 1.0F, s.punch(),
            (q, c) -> {
                double[][] p = new double[4][];
                for (int k = 0; k < 4; k++) {
                    p[k] = world(rot, new double[] {q[k * 3], q[k * 3 + 1], q[k * 3 + 2]});
                }
                r.quad(p, c);
            });
        return r.image(w, h);
    }

    /** A world point in arm space (blocks). */
    static double[] armPoint(double[][] r, double[] world) {
        double[] m = {-world[0] / K, -(world[1] - 1.501 * 0.9375) / K, world[2] / K};
        double[] local = mul(transpose(r), sub(m, new double[] {-5, 2, 0}));
        return new double[] {local[0] / 16, local[1] / 16, local[2] / 16};
    }

    static double[] armDirection(double[][] r, double[] world) {
        double[] m = {-world[0], -world[1], world[2]};
        return norm(mul(transpose(r), m));
    }

    /** A z-buffered rasteriser (supersampled), lit as the game lights entities. */
    static final class Raster {
        final int w;
        final int h;
        final float[] cr;
        final float[] cg;
        final float[] cb;
        final float[] depth;
        final Shot shot;
        final double[] eye;
        final double[] fw;
        final double[] rt;
        final double[] up;
        final double focal;

        Raster(int w, int h, Shot shot) {
            this.w = w;
            this.h = h;
            this.shot = shot;
            this.cr = new float[w * h];
            this.cg = new float[w * h];
            this.cb = new float[w * h];
            this.depth = new float[w * h];
            this.eye = shot.eye();
            this.fw = norm(sub(shot.look(), shot.eye()));
            this.rt = norm(cross(this.fw, new double[] {0, 1, 0}));
            this.up = cross(this.rt, this.fw);
            this.focal = shot.ortho() ? h / 1.6 : (h / 2.0) / Math.tan(Math.toRadians(shot.fov()) / 2);
            java.util.Random noise = new java.util.Random(5);
            for (int i = 0; i < w * h; i++) {
                int y = i / w;
                int x = i % w;
                float t = y / (float) h;
                if (shot.night()) {
                    // a deep blue sky with a lighter streak, as behind the picture's fist
                    float glow = (float) Math.exp(-Math.pow((x - w * 0.7) / (w * 0.35), 2) - Math.pow((y - h * 0.4) / (h * 0.5), 2));
                    float n = noise.nextFloat() * 0.03F;
                    this.cr[i] = 0.06F + 0.10F * glow + n;
                    this.cg[i] = 0.08F + 0.16F * glow + n;
                    this.cb[i] = 0.22F + 0.35F * glow + n;
                } else {
                    this.cr[i] = 0.55F + 0.15F * t;
                    this.cg[i] = 0.70F + 0.10F * t;
                    this.cb[i] = 0.92F;
                }
                this.depth[i] = Float.MAX_VALUE;
            }
        }

        /** World to screen (x, y, depth along the view). */
        double[] project(double[] p) {
            double[] v = sub(p, this.eye);
            double x = dot(v, this.rt);
            double y = dot(v, this.up);
            double z = dot(v, this.fw);
            if (this.shot.ortho()) {
                return new double[] {this.w / 2.0 + x * this.focal, this.h / 2.0 - y * this.focal, z};
            }
            if (z < 0.01) {
                return null;
            }
            return new double[] {this.w / 2.0 + this.focal * x / z, this.h / 2.0 - this.focal * y / z, z};
        }

        float light(double[] n) {
            double[] l0 = norm(new double[] {0.2, 1.0, -0.7});
            double[] l1 = norm(new double[] {-0.2, 1.0, 0.7});
            return (float) Math.min(1.0, (Math.max(0, dot(n, l0)) + Math.max(0, dot(n, l1))) * 0.6 + 0.4);
        }

        /** A body box (model pixels), one flat colour, lit. */
        void box(double[] a, double[] b, int argb) {
            double[][] c = new double[8][];
            for (int i = 0; i < 8; i++) {
                c[i] = model(new double[] {(i & 1) == 0 ? a[0] : b[0], (i & 2) == 0 ? a[1] : b[1], (i & 4) == 0 ? a[2] : b[2]});
            }
            int[][] faces = {{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 1, 5, 4}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}};
            for (int[] f : faces) {
                double[][] p = {c[f[0]], c[f[1]], c[f[2]], c[f[3]]};
                double[] n = norm(cross(sub(p[1], p[0]), sub(p[3], p[0])));
                double[] mid = scale(add(add(p[0], p[1]), add(p[2], p[3])), 0.25);
                double[] centre = model(new double[] {(a[0] + b[0]) / 2, (a[1] + b[1]) / 2, (a[2] + b[2]) / 2});
                if (dot(n, sub(mid, centre)) < 0) {
                    n = scale(n, -1);
                }
                float k = this.light(n);
                int lit = shade(argb, k);
                this.solid(p, lit);
            }
        }

        /** An arm cube (the player model's UV layout at u, v; grown by {@code grow} pixels), every texel a lit quad. */
        void cube(double[][] rot, float bend, float minX, float maxX, int u, int v, int wide, double grow, int[] skin) {
            double x0 = minX - grow;
            double x1 = maxX + grow;
            double y0 = -2 - grow;
            double y1 = 10 + grow;
            double z0 = -2 - grow;
            double z1 = 2 + grow;
            double[] vx = {x0, y0, z0};
            double[] v1 = {x1, y0, z0};
            double[] v2 = {x1, y1, z0};
            double[] v3 = {x0, y1, z0};
            double[] v4 = {x0, y0, z1};
            double[] v5 = {x1, y0, z1};
            double[] v6 = {x1, y1, z1};
            double[] v7 = {x0, y1, z1};
            int d = 4;
            int hgt = 12;
            double f4 = u;
            double f5 = u + d;
            double f6 = u + d + wide;
            double f7 = u + d + wide + wide;
            double f8 = u + d + wide + d;
            double f9 = u + d + wide + d + wide;
            double f10 = v;
            double f11 = v + d;
            double f12 = v + d + hgt;
            // ModelPart.Cube's polygons (1.20.1): vertices, then u1, v1, u2, v2
            Object[][] polys = {
                {new double[][] {v5, v4, vx, v1}, f5, f10, f6, f11},
                {new double[][] {v2, v3, v7, v6}, f6, f11, f7, f10},
                {new double[][] {vx, v4, v7, v3}, f4, f11, f5, f12},
                {new double[][] {v1, vx, v3, v2}, f5, f11, f6, f12},
                {new double[][] {v5, v1, v2, v6}, f6, f11, f8, f12},
                {new double[][] {v4, v5, v6, v7}, f8, f11, f9, f12},
            };
            for (Object[] poly : polys) {
                double[][] vs = (double[][]) poly[0];
                double u1 = (Double) poly[1];
                double va = (Double) poly[2];
                double u2 = (Double) poly[3];
                double vb = (Double) poly[4];
                int ua = (int) Math.min(u1, u2);
                int ub = (int) Math.max(u1, u2);
                int vlo = (int) Math.min(va, vb);
                int vhi = (int) Math.max(va, vb);
                for (int ty = vlo; ty < vhi; ty++) {
                    for (int tx = ua; tx < ub; tx++) {
                        int texel = skin[ty * 64 + tx];
                        if ((texel >>> 24) < 128) {
                            continue;
                        }
                        double[][] q = new double[4][];
                        double[][] uv = {{tx, ty}, {tx + 1, ty}, {tx + 1, ty + 1}, {tx, ty + 1}};
                        for (int k = 0; k < 4; k++) {
                            double s = (uv[k][0] - u1) / (u2 - u1);
                            double t = (uv[k][1] - va) / (vb - va);
                            double[] p = add(add(scale(vs[1], (1 - s) * (1 - t)), scale(vs[0], s * (1 - t))),
                                add(scale(vs[2], (1 - s) * t), scale(vs[3], s * t)));
                            float[] b = ArmBend.bent(0, bend, minX, maxX, (float) (p[0] / 16), (float) (p[1] / 16), (float) (p[2] / 16));
                            q[k] = world(rot, new double[] {b[0], b[1], b[2]});
                        }
                        double[] n = norm(cross(sub(q[1], q[0]), sub(q[3], q[0])));
                        double[] centre = world(rot, scale(new double[] {(minX + maxX) / 2.0, 4, 0}, 1.0 / 16));
                        double[] mid = scale(add(add(q[0], q[1]), add(q[2], q[3])), 0.25);
                        if (dot(n, sub(mid, centre)) < 0) {
                            n = scale(n, -1);
                        }
                        this.solid(q, shade(texel, this.light(n)));
                    }
                }
            }
        }

        void solid(double[][] p, int argb) {
            double[][] s = new double[4][];
            for (int k = 0; k < 4; k++) {
                s[k] = this.project(p[k]);
                if (s[k] == null) {
                    return;
                }
            }
            float r = ((argb >> 16) & 255) / 255.0F;
            float g = ((argb >> 8) & 255) / 255.0F;
            float b = (argb & 255) / 255.0F;
            this.tri(s[0], s[1], s[2], new float[][] {{r, g, b, 1}, {r, g, b, 1}, {r, g, b, 1}}, true);
            this.tri(s[0], s[2], s[3], new float[][] {{r, g, b, 1}, {r, g, b, 1}, {r, g, b, 1}}, true);
        }

        /** An aura quad: per-corner colours, alpha blended, depth tested, not written. Split 0-1-2, 0-2-3 as GL splits a quad. */
        void quad(double[][] p, int[] argb) {
            double[][] s = new double[4][];
            for (int k = 0; k < 4; k++) {
                s[k] = this.project(p[k]);
                if (s[k] == null) {
                    return;
                }
            }
            float[][] c = new float[4][];
            for (int k = 0; k < 4; k++) {
                c[k] = new float[] {((argb[k] >> 16) & 255) / 255.0F, ((argb[k] >> 8) & 255) / 255.0F, (argb[k] & 255) / 255.0F,
                    (argb[k] >>> 24) / 255.0F};
            }
            this.tri(s[0], s[1], s[2], new float[][] {c[0], c[1], c[2]}, false);
            this.tri(s[0], s[2], s[3], new float[][] {c[0], c[2], c[3]}, false);
        }

        void tri(double[] a, double[] b, double[] c, float[][] col, boolean solid) {
            double area = (b[0] - a[0]) * (c[1] - a[1]) - (c[0] - a[0]) * (b[1] - a[1]);
            if (Math.abs(area) < 1.0E-9) {
                return;
            }
            int x0 = Math.max(0, (int) Math.floor(Math.min(a[0], Math.min(b[0], c[0]))));
            int x1 = Math.min(this.w - 1, (int) Math.ceil(Math.max(a[0], Math.max(b[0], c[0]))));
            int y0 = Math.max(0, (int) Math.floor(Math.min(a[1], Math.min(b[1], c[1]))));
            int y1 = Math.min(this.h - 1, (int) Math.ceil(Math.max(a[1], Math.max(b[1], c[1]))));
            for (int y = y0; y <= y1; y++) {
                for (int x = x0; x <= x1; x++) {
                    double px = x + 0.5;
                    double py = y + 0.5;
                    double wa = ((b[0] - px) * (c[1] - py) - (c[0] - px) * (b[1] - py)) / area;
                    double wb = ((c[0] - px) * (a[1] - py) - (a[0] - px) * (c[1] - py)) / area;
                    double wc = 1 - wa - wb;
                    if (wa < 0 || wb < 0 || wc < 0) {
                        continue;
                    }
                    // perspective-correct depth (1/z interpolates linearly on the screen)
                    double inv = this.shot.ortho() ? wa * a[2] + wb * b[2] + wc * c[2] : wa / a[2] + wb / b[2] + wc / c[2];
                    float z = (float) (this.shot.ortho() ? inv : 1.0 / inv);
                    int i = y * this.w + x;
                    if (z > this.depth[i] + 1.0E-5F) {
                        continue;
                    }
                    float r = (float) (wa * col[0][0] + wb * col[1][0] + wc * col[2][0]);
                    float g = (float) (wa * col[0][1] + wb * col[1][1] + wc * col[2][1]);
                    float bl = (float) (wa * col[0][2] + wb * col[1][2] + wc * col[2][2]);
                    float al = (float) (wa * col[0][3] + wb * col[1][3] + wc * col[2][3]);
                    if (solid) {
                        this.cr[i] = r;
                        this.cg[i] = g;
                        this.cb[i] = bl;
                        this.depth[i] = z;
                    } else {
                        this.cr[i] = this.cr[i] * (1 - al) + r * al;
                        this.cg[i] = this.cg[i] * (1 - al) + g * al;
                        this.cb[i] = this.cb[i] * (1 - al) + bl * al;
                    }
                }
            }
        }

        BufferedImage image(int ow, int oh) {
            BufferedImage out = new BufferedImage(ow, oh, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < oh; y++) {
                for (int x = 0; x < ow; x++) {
                    float r = 0;
                    float g = 0;
                    float b = 0;
                    for (int dy = 0; dy < SS; dy++) {
                        for (int dx = 0; dx < SS; dx++) {
                            int i = (y * SS + dy) * this.w + x * SS + dx;
                            r += this.cr[i];
                            g += this.cg[i];
                            b += this.cb[i];
                        }
                    }
                    int n = SS * SS;
                    out.setRGB(x, y, (clamp(r / n) << 16) | (clamp(g / n) << 8) | clamp(b / n));
                }
            }
            return out;
        }
    }

    static int clamp(float v) {
        return Math.max(0, Math.min(255, Math.round(v * 255)));
    }

    static int shade(int argb, float k) {
        int r = Math.round(((argb >> 16) & 255) * k);
        int g = Math.round(((argb >> 8) & 255) * k);
        int b = Math.round((argb & 255) * k);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    // --------------------------------------------------------------------------------------------------------------- vectors

    static double[] add(double[] a, double[] b) {
        return new double[] {a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    static double[] sub(double[] a, double[] b) {
        return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    static double[] scale(double[] a, double k) {
        return new double[] {a[0] * k, a[1] * k, a[2] * k};
    }

    static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static double[] norm(double[] a) {
        double l = Math.sqrt(dot(a, a));
        return scale(a, 1 / l);
    }

    static double[] mul(double[][] m, double[] v) {
        return new double[] {m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2], m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2],
            m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2]};
    }

    static double[][] matmul(double[][] a, double[][] b) {
        double[][] out = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    out[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return out;
    }

    static double[][] transpose(double[][] m) {
        return new double[][] {{m[0][0], m[1][0], m[2][0]}, {m[0][1], m[1][1], m[2][1]}, {m[0][2], m[1][2], m[2][2]}};
    }
}
