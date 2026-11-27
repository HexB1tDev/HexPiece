import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.DragonPose;
import com.hexhaki.piece.itsumade.ItsuForm;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 3.9.4: renders the owner's Itsumade off-game to judge it (the house rule): the game's own baked model (assets/hexhaki/itsumade/
 * itsumade.sry, tools/itsumade_bake.py), posed by the game's own {@link ItsuForm}, skinned by {@link DragonPose}, its flowing parts alive
 * (ItsuForm.flow, ItsuForm.heat), coloured as the game colours it (the vertex colour times the atlas, lit by a sun, the fire glowing),
 * over a ground with a block grid and a player-sized figure for the scale.
 *
 * <p>java -cp prev:build/classes ItsuPreview itsumade.sry itsumade.png out.png|dir/ [--size=WxH] shot ...
 * a shot: {@code state:seconds:yaw:pitch:dist[:tx:ty:tz]}; states: rest, hover, fly, fast, glide, dive, flare, land, stand, walk, bite,
 * shut, gape.
 */
public final class ItsuPreview {
    static int W = 480;
    static int H = 360;
    static final int SS = 2;
    static BufferedImage texture;
    static DragonModel model;
    static ItsuForm.Shape shape;
    static double OX;
    static double OY;
    static double OZ;
    static final double[] BEAK = new double[7];
    static final float[] LIGHT = norm3(0.35F, 0.8F, -0.45F);

    public static void main(String[] args) throws Exception {
        try (FileInputStream in = new FileInputStream(args[0])) {
            model = DragonModel.read(in);
        }
        shape = new ItsuForm.Shape(model);
        texture = ImageIO.read(new File(args[1]));
        List<String> shots = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            if (args[i].startsWith("--f=")) {
                // a tuning: --f=NAME=value sets ItsuForm's static NAME
                String[] kv = args[i].substring(4).split("=");
                java.lang.reflect.Field f = ItsuForm.class.getDeclaredField(kv[0]);
                f.setAccessible(true);
                f.setFloat(null, Float.parseFloat(kv[1]));
            } else if (args[i].startsWith("--size=")) {
                String[] wh = args[i].substring(7).split("x");
                W = Integer.parseInt(wh[0]);
                H = Integer.parseInt(wh[1]);
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
        BufferedImage sheet = new BufferedImage(cols * W, rows * H, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = sheet.createGraphics();
        for (int i = 0; i < shots.size(); i++) {
            BufferedImage one = render(shots.get(i));
            g.drawImage(one, (i % cols) * W, (i / cols) * H, null);
            g.setColor(java.awt.Color.WHITE);
            g.drawString(shots.get(i), (i % cols) * W + 6, (i / cols) * H + 14);
        }
        g.dispose();
        ImageIO.write(sheet, "png", new File(args[2]));
    }

    static float[] norm3(float x, float y, float z) {
        float l = (float) Math.sqrt(x * x + y * y + z * z);
        return new float[] {x / l, y / l, z / l};
    }

    /** The motion of a preview state at t seconds. */
    static ItsuForm.Motion motion(String state, float t) {
        ItsuForm.Motion mo = new ItsuForm.Motion();
        switch (state) {
            case "hover" -> mo.speed = 0.0F;
            case "fly" -> mo.speed = 0.45F;
            case "fast" -> mo.speed = 1.0F;
            case "glide" -> {
                mo.speed = 0.8F;
                mo.glide = 1.0F;
            }
            case "dive" -> {
                mo.speed = 1.0F;
                mo.dive = 1.0F;
            }
            case "flare" -> {
                mo.speed = 0.2F;
                mo.flare = 1.0F;
            }
            case "land" -> {
                mo.perch = 1.0F;
                mo.land = (float) Math.max(0.0, Math.sin(Math.min(1.0, t / 0.7) * Math.PI));
            }
            case "stand" -> mo.perch = 1.0F;
            case "walk" -> {
                mo.perch = 1.0F;
                mo.walk = 1.0F;
                mo.stride = t * 4.2F;
            }
            case "bite" -> {
                mo.perch = 1.0F;
                mo.bite = Math.min(0.999F, (t % 1.0F));
            }
            case "shut" -> {
                mo.perch = 1.0F;
                mo.mouth = 0.0F;
            }
            case "gape" -> {
                mo.perch = 1.0F;
                mo.mouth = 1.0F;
            }
            case "form" -> {
                mo.perch = 1.0F;
                ItsuForm.change(ItsuForm.FORMING, t * 20.0F, mo);
            }
            case "unform" -> {
                mo.perch = 1.0F;
                ItsuForm.change(ItsuForm.UNFORMING, t * 20.0F, mo);
            }
            default -> {
            }
        }
        return mo;
    }

    static BufferedImage render(String shot) {
        String[] p = shot.split(":");
        String state = p[0];
        float t = (float) Double.parseDouble(p[1]);
        DragonPose pose = new DragonPose(model);
        ItsuForm.Motion mo = motion(state, t);
        boolean rest = state.equals("rest");
        if (rest) {
            pose.rest();
            pose.solve();
        } else {
            ItsuForm.pose(model, shape, pose, mo, t);
        }
        boolean change = state.equals("form") || state.equals("unform");
        int phase = state.equals("form") ? ItsuForm.FORMING : ItsuForm.UNFORMING;
        float s = ItsuForm.SCALE * (change ? ItsuForm.size(phase, t * 20.0F) : 1.0F);
        // the skinned parts, the flowing ones alive; then placed: on the ground its feet on it, in the air its chest 7 blocks up
        List<float[]> skinned = new ArrayList<>();
        float low = Float.MAX_VALUE;
        for (int i = 0; i < model.parts.length; i++) {
            DragonModel.Part part = model.parts[i];
            float[] pos = new float[part.vertices() * 3];
            float[] nrm = new float[part.vertices() * 3];
            pose.skin(part, pos, nrm);
            if (!rest) {
                float wind = (1.0F - mo.perch) * (0.3F + 0.7F * mo.speed);
                ItsuForm.flow(shape, i, pos, nrm, t, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, wind, mo.grown[ItsuForm.G_FIRE]);
                ItsuForm.ribbonGrown(shape, pose, i, pos, mo.grown[ItsuForm.G_RIBBON]);
            }
            skinned.add(pos);
            skinned.add(nrm);
            if (part.name.contains("digit") || part.name.contains("talon") || part.name.contains("hallux") || part.name.contains("toe pad")) {
                for (int v = 0; v < part.vertices(); v++) {
                    low = Math.min(low, pos[v * 3 + 1]);
                }
            }
        }
        boolean ground = mo.perch > 0.5F || rest;
        if (s < 0.001F) {
            s = 0.001F;
        }
        float chestY = pose.global[shape.chest * 12 + 7];
        double lift = ground ? -low * s : 7.0 - chestY * s;
        double[] lo = {1e9, 1e9, 1e9};
        double[] hi = {-1e9, -1e9, -1e9};
        List<float[]> placed = new ArrayList<>();
        for (int i = 0; i < model.parts.length; i++) {
            float[] pos = skinned.get(i * 2);
            float[] wp = new float[pos.length];
            for (int v = 0; v < pos.length / 3; v++) {
                wp[v * 3] = pos[v * 3] * s;
                wp[v * 3 + 1] = (float) (pos[v * 3 + 1] * s + lift);
                wp[v * 3 + 2] = pos[v * 3 + 2] * s;
                for (int r = 0; r < 3; r++) {
                    lo[r] = Math.min(lo[r], wp[v * 3 + r]);
                    hi[r] = Math.max(hi[r], wp[v * 3 + r]);
                }
            }
            placed.add(wp);
        }
        double yaw = Math.toRadians(Double.parseDouble(p[2]));
        double pitch = Math.toRadians(Double.parseDouble(p[3]));
        double dist = Double.parseDouble(p[4]);
        double[] target = {(lo[0] + hi[0]) / 2, (lo[1] + hi[1]) / 2, (lo[2] + hi[2]) / 2};
        if (p.length >= 8) {
            target = new double[] {Double.parseDouble(p[5]), Double.parseDouble(p[6]), Double.parseDouble(p[7])};
        }
        double[] eye = {target[0] + dist * Math.cos(pitch) * Math.sin(yaw), target[1] + dist * Math.sin(pitch),
            target[2] + dist * Math.cos(pitch) * Math.cos(yaw)};
        Raster ra = new Raster(W * SS, H * SS, eye, target);
        ra.ground(0.0);
        if (change) {
            if (!ItsuForm.humanHidden(phase, t * 20.0F)) {
                // 3.9.5: the human swelling as it convulses (ItsuForm.humanScale), its arms thrown wide before the swap
                double k = ItsuForm.humanScale(phase, t * 20.0F);
                double raise = phase == ItsuForm.FORMING ? (t * 20.0F >= 16.0F ? 85.0 : 20.0) : 15.0;
                ra.humanK(k, raise);
            }
        } else {
            ra.figure(lo[0] - 1.5, 0.0, 0.0);
        }
        for (int i = 0; i < model.parts.length; i++) {
            DragonModel.Part part = model.parts[i];
            float[] wp = placed.get(i);
            float[] nrm = skinned.get(i * 2 + 1);
            float[] col = new float[part.vertices() * 4];
            int kind = shape.kind[i];
            for (int v = 0; v < part.vertices(); v++) {
                float cr = part.col == null ? 1.0F : part.col[v * 3];
                float cg = part.col == null ? 1.0F : part.col[v * 3 + 1];
                float cb = part.col == null ? 1.0F : part.col[v * 3 + 2];
                float lit;
                float heat = 0.0F;
                if (kind == ItsuForm.FIRE) {
                    lit = 1.15F;
                    heat = rest ? 0.0F : 0.55F * ItsuForm.heat(shape, i, v, t);
                } else if (kind == ItsuForm.RIBBON) {
                    lit = 1.0F;
                } else {
                    float d = nrm[v * 3] * LIGHT[0] + nrm[v * 3 + 1] * LIGHT[1] + nrm[v * 3 + 2] * LIGHT[2];
                    lit = 0.55F + 0.5F * Math.max(0.0F, d);
                }
                col[v * 4] = cr * lit;
                col[v * 4 + 1] = cg * lit;
                col[v * 4 + 2] = cb * lit;
                col[v * 4 + 3] = heat;
            }
            ra.mesh(part, wp, col);
        }
        return ra.image(W, H);
    }

    static final class Raster {
        final int w;
        final int h;
        final int[] rgb;
        final float[] depth;
        final double[] eye;
        final double[] fwd;
        final double[] right;
        final double[] up;
        final double focal;

        Raster(int w, int h, double[] eye, double[] target) {
            this.w = w;
            this.h = h;
            this.rgb = new int[w * h];
            this.depth = new float[w * h];
            java.util.Arrays.fill(this.depth, Float.MAX_VALUE);
            for (int y = 0; y < h; y++) {
                float k = y / (float) h;
                int c = mix(0x2B3F6B, 0x7A9CC8, k);
                for (int x = 0; x < w; x++) {
                    this.rgb[y * w + x] = c;
                }
            }
            this.eye = eye;
            this.fwd = norm(sub(target, eye));
            this.right = norm(cross(this.fwd, new double[] {0, 1, 0}));
            this.up = cross(this.right, this.fwd);
            this.focal = h / 2.0 / Math.tan(Math.toRadians(70) / 2);
        }

        double[] project(double x, double y, double z) {
            double[] d = {x - this.eye[0], y - this.eye[1], z - this.eye[2]};
            double zc = dot(d, this.fwd);
            if (zc < 0.05) {
                return null;
            }
            return new double[] {this.w / 2.0 + dot(d, this.right) / zc * this.focal, this.h / 2.0 - dot(d, this.up) / zc * this.focal, zc};
        }

        void ground(double y) {
            for (int px = 0; px < this.w; px++) {
                for (int py = 0; py < this.h; py++) {
                    double sx = (px - this.w / 2.0) / this.focal;
                    double sy = -(py - this.h / 2.0) / this.focal;
                    double[] dir = {this.fwd[0] + this.right[0] * sx + this.up[0] * sy, this.fwd[1] + this.right[1] * sx + this.up[1] * sy,
                        this.fwd[2] + this.right[2] * sx + this.up[2] * sy};
                    if (dir[1] >= -1e-6) {
                        continue;
                    }
                    double k = (y - this.eye[1]) / dir[1];
                    double gx = this.eye[0] + dir[0] * k;
                    double gz = this.eye[2] + dir[2] * k;
                    double fx = gx - Math.floor(gx);
                    double fz = gz - Math.floor(gz);
                    boolean line = fx < 0.04 || fz < 0.04;
                    int i = py * this.w + px;
                    this.rgb[i] = line ? 0x55684A : 0x6E8A5E;
                    this.depth[i] = (float) (k * dot(dir, this.fwd));
                }
            }
        }

        void figure(double x, double y, double z) {
            box(x - 0.3, y, z - 0.15, x + 0.3, y + 0.75, z + 0.15, 0x3A4A7A);
            box(x - 0.3, y + 0.75, z - 0.15, x + 0.3, y + 1.4, z + 0.15, 0x3D8BC4);
            box(x - 0.25, y + 1.4, z - 0.25, x + 0.25, y + 1.9, z + 0.25, 0xD9A77E);
        }

        /**
         * 3.7.0: a player at the origin facing -Z (their right toward +X), the arms raised {@code raise} degrees out to the sides (the
         * player model's: the shoulders 22 pixels up, the arms 4 square, from 2 over the pivot to 10 under it).
         */
        /** A player swollen {@code k} (taller more than wider), the arms raised {@code raise} degrees out to the sides. */
        void humanK(double k, double raise) {
            double w = Math.pow(k, 0.85);
            box(-0.25 * w, 0, -0.125 * w, 0.25 * w, 0.75 * k, 0.125 * w, 0x3A4A7A);
            box(-0.25 * w, 0.75 * k, -0.125 * w, 0.25 * w, 1.5 * k, 0.125 * w, 0x3D8BC4);
            box(-0.25 * w, 1.5 * k, -0.25 * w, 0.25 * w, 2.0 * k, 0.25 * w, 0xD9A77E);
            double r = Math.toRadians(raise);
            for (int side = 0; side < 2; side++) {
                double sgn = side == 0 ? 1 : -1;
                double[] d = {sgn * Math.sin(r), -Math.cos(r), 0};
                double[] q = {sgn * Math.cos(r), Math.sin(r), 0};
                double[] o = {sgn * 0.375 * w, 1.375 * k, 0};
                double[][] c = new double[8][];
                for (int n = 0; n < 8; n++) {
                    double a = ((n & 1) == 0 ? -0.125 : 0.625) * k;
                    double b = ((n & 2) == 0 ? -0.125 : 0.125) * w;
                    double e = ((n & 4) == 0 ? -0.125 : 0.125) * w;
                    c[n] = new double[] {o[0] + d[0] * a + q[0] * b, o[1] + d[1] * a + q[1] * b, o[2] + e};
                }
                int[][] f = {{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 1, 5, 4}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}};
                for (int[] face : f) {
                    tri(c[face[0]], c[face[1]], c[face[2]], null, null, 0xD9A77E);
                    tri(c[face[0]], c[face[2]], c[face[3]], null, null, 0xD9A77E);
                }
            }
        }

        void human(double raise, boolean noArms, boolean noBody, boolean noHead, boolean noLegs) {
            if (!noLegs) {
                box(OX - 0.25, OY, OZ - 0.125, OX + 0.25, OY + 0.75, OZ + 0.125, 0x3A4A7A);
            }
            if (!noBody) {
                box(OX - 0.25, OY + 0.75, OZ - 0.125, OX + 0.25, OY + 1.5, OZ + 0.125, 0x3D8BC4);
            }
            if (!noHead) {
                box(OX - 0.25, OY + 1.5, OZ - 0.25, OX + 0.25, OY + 2.0, OZ + 0.25, 0xD9A77E);
            }
            double r = Math.toRadians(raise);
            for (int side = 0; side < 2 && !noArms; side++) {
                double sgn = side == 0 ? 1 : -1;
                double[] d = {sgn * Math.sin(r), -Math.cos(r), 0};
                double[] q = {sgn * Math.cos(r), Math.sin(r), 0};
                double[] w = {0, 0, 1};
                double[] o = {OX + sgn * 0.375, OY + 1.375, OZ};
                double[][] c = new double[8][];
                for (int k = 0; k < 8; k++) {
                    double a = (k & 1) == 0 ? -0.125 : 0.625;
                    double b = (k & 2) == 0 ? -0.125 : 0.125;
                    double e = (k & 4) == 0 ? -0.125 : 0.125;
                    c[k] = new double[] {o[0] + d[0] * a + q[0] * b + w[0] * e, o[1] + d[1] * a + q[1] * b + w[1] * e, o[2] + d[2] * a + q[2] * b + w[2] * e};
                }
                int[][] f = {{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 1, 5, 4}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}};
                for (int[] face : f) {
                    tri(c[face[0]], c[face[1]], c[face[2]], null, null, 0xD9A77E);
                    tri(c[face[0]], c[face[2]], c[face[3]], null, null, 0xD9A77E);
                }
            }
        }

        void box(double x0, double y0, double z0, double x1, double y1, double z1, int color) {
            double[][] c = {{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
            int[][] f = {{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}};
            float[] shade = {0.8F, 0.8F, 0.6F, 0.6F, 1.0F, 0.5F};
            for (int k = 0; k < 6; k++) {
                int col = scale(color, shade[k]);
                tri(c[f[k][0]], c[f[k][1]], c[f[k][2]], null, null, col);
                tri(c[f[k][0]], c[f[k][2]], c[f[k][3]], null, null, col);
            }
        }

        /** 3.7.6: only the quads within {@code shown} of the way out along the span (as client/phoenix/PhoenixFx.emit). */
        void meshShown(DragonModel.Part part, float[] pos, float[] col, float[] modelX, float rootX, float span, float length, float shown) {
            int[] q = part.quads;
            for (int i = 0; i < q.length; i += 4) {
                float far = -Float.MAX_VALUE;
                for (int j = 0; j < 4; j++) {
                    far = Math.max(far, (modelX[q[i + j]] - rootX) * span / length);
                }
                if (far > shown) {
                    continue;
                }
                face(part, pos, col, q[i], q[i + 1], q[i + 2]);
                if (q[i + 3] != q[i + 2]) {
                    face(part, pos, col, q[i], q[i + 2], q[i + 3]);
                }
            }
        }

        void mesh(DragonModel.Part part, float[] pos, float[] col) {
            int[] q = part.quads;
            for (int i = 0; i < q.length; i += 4) {
                face(part, pos, col, q[i], q[i + 1], q[i + 2]);
                if (q[i + 3] != q[i + 2]) {
                    face(part, pos, col, q[i], q[i + 2], q[i + 3]);
                }
            }
        }

        void face(DragonModel.Part part, float[] pos, float[] col, int a, int b, int c) {
            int[] idx = {a, b, c};
            double[][] p = new double[3][];
            double[][] uvc = new double[3][];
            for (int k = 0; k < 3; k++) {
                int v = idx[k];
                p[k] = new double[] {pos[v * 3], pos[v * 3 + 1], pos[v * 3 + 2]};
                uvc[k] = new double[] {part.uv[v * 2], part.uv[v * 2 + 1], col[v * 4], col[v * 4 + 1], col[v * 4 + 2], col[v * 4 + 3]};
            }
            tri(p[0], p[1], p[2], uvc, part, 0);
        }

        void tri(double[] a, double[] b, double[] c, double[][] uvc, DragonModel.Part part, int flat) {
            double[] pa = project(a[0], a[1], a[2]);
            double[] pb = project(b[0], b[1], b[2]);
            double[] pc = project(c[0], c[1], c[2]);
            if (pa == null || pb == null || pc == null) {
                return;
            }
            double area = (pb[0] - pa[0]) * (pc[1] - pa[1]) - (pb[1] - pa[1]) * (pc[0] - pa[0]);
            if (Math.abs(area) < 1e-9) {
                return;
            }
            int x0 = (int) Math.max(0, Math.floor(Math.min(pa[0], Math.min(pb[0], pc[0]))));
            int x1 = (int) Math.min(this.w - 1, Math.ceil(Math.max(pa[0], Math.max(pb[0], pc[0]))));
            int y0 = (int) Math.max(0, Math.floor(Math.min(pa[1], Math.min(pb[1], pc[1]))));
            int y1 = (int) Math.min(this.h - 1, Math.ceil(Math.max(pa[1], Math.max(pb[1], pc[1]))));
            int tw = texture.getWidth();
            int th = texture.getHeight();
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
                    int i = y * this.w + x;
                    if (z >= this.depth[i]) {
                        continue;
                    }
                    int col;
                    if (part == null) {
                        col = flat;
                    } else {
                        double[] m = new double[6];
                        for (int k = 0; k < 6; k++) {
                            m[k] = (w0 * uvc[0][k] / pa[2] + w1 * uvc[1][k] / pb[2] + w2 * uvc[2][k] / pc[2]) * z;
                        }
                        int tx = Math.floorMod((int) Math.floor(m[0] * tw), tw);
                        int ty = Math.floorMod((int) Math.floor(m[1] * th), th);
                        int texel = texture.getRGB(tx, ty);
                        // the game's overlay: toward white by the heat (OverlayTexture's white rows)
                        double wh = m[5];
                        int r = (int) Math.min(255, ((texel >> 16) & 255) * m[2] * (1 - wh) + 255 * wh);
                        int g = (int) Math.min(255, ((texel >> 8) & 255) * m[3] * (1 - wh) + 255 * wh);
                        int bl = (int) Math.min(255, (texel & 255) * m[4] * (1 - wh) + 255 * wh);
                        col = r << 16 | g << 8 | bl;
                    }
                    this.depth[i] = (float) z;
                    this.rgb[i] = col;
                }
            }
        }

        /** A quad of fire (12 coordinates, 4 ARGB corners) blended over what is there, depth tested, not written. */
        void blend(double[] q) {
            int[] c = {(int) (long) q[12], (int) (long) q[13], (int) (long) q[14], (int) (long) q[15]};
            blendTri(q, 0, 1, 2, c);
            blendTri(q, 0, 2, 3, c);
        }

        void blendTri(double[] q, int a, int b, int c, int[] col) {
            double[] pa = project(q[a * 3], q[a * 3 + 1], q[a * 3 + 2]);
            double[] pb = project(q[b * 3], q[b * 3 + 1], q[b * 3 + 2]);
            double[] pc = project(q[c * 3], q[c * 3 + 1], q[c * 3 + 2]);
            if (pa == null || pb == null || pc == null) {
                return;
            }
            double area = (pb[0] - pa[0]) * (pc[1] - pa[1]) - (pb[1] - pa[1]) * (pc[0] - pa[0]);
            if (Math.abs(area) < 1e-9) {
                return;
            }
            int x0 = (int) Math.max(0, Math.floor(Math.min(pa[0], Math.min(pb[0], pc[0]))));
            int x1 = (int) Math.min(this.w - 1, Math.ceil(Math.max(pa[0], Math.max(pb[0], pc[0]))));
            int y0 = (int) Math.max(0, Math.floor(Math.min(pa[1], Math.min(pb[1], pc[1]))));
            int y1 = (int) Math.min(this.h - 1, Math.ceil(Math.max(pa[1], Math.max(pb[1], pc[1]))));
            int[] cc = {col[a], col[b], col[c]};
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
                    double z = 1 / (w0 / pa[2] + w1 / pb[2] + w2 / pc[2]);
                    int i = y * this.w + x;
                    if (z >= this.depth[i]) {
                        continue;
                    }
                    double al = 0;
                    double r = 0;
                    double g = 0;
                    double bl = 0;
                    double[] ws = {w0, w1, w2};
                    for (int k = 0; k < 3; k++) {
                        al += ws[k] * ((cc[k] >>> 24) & 255) / 255.0;
                        r += ws[k] * ((cc[k] >> 16) & 255);
                        g += ws[k] * ((cc[k] >> 8) & 255);
                        bl += ws[k] * (cc[k] & 255);
                    }
                    int d = this.rgb[i];
                    int nr = (int) (((d >> 16) & 255) * (1 - al) + r * al);
                    int ng = (int) (((d >> 8) & 255) * (1 - al) + g * al);
                    int nb = (int) ((d & 255) * (1 - al) + bl * al);
                    this.rgb[i] = nr << 16 | ng << 8 | nb;
                }
            }
        }

        BufferedImage image(int ow, int oh) {
            BufferedImage out = new BufferedImage(ow, oh, BufferedImage.TYPE_INT_RGB);
            int ss = this.w / ow;
            for (int y = 0; y < oh; y++) {
                for (int x = 0; x < ow; x++) {
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    for (int dy = 0; dy < ss; dy++) {
                        for (int dx = 0; dx < ss; dx++) {
                            int c = this.rgb[(y * ss + dy) * this.w + x * ss + dx];
                            r += c >> 16 & 255;
                            g += c >> 8 & 255;
                            b += c & 255;
                        }
                    }
                    int n = ss * ss;
                    out.setRGB(x, y, (r / n) << 16 | (g / n) << 8 | b / n);
                }
            }
            return out;
        }
    }

    static int mix(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return r << 16 | g << 8 | bl;
    }

    static int scale(int c, float f) {
        return mix(0, c, f);
    }

    static double[] sub(double[] a, double[] b) {
        return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }

    static double[] norm(double[] a) {
        double l = Math.sqrt(dot(a, a));
        return new double[] {a[0] / l, a[1] / l, a[2] / l};
    }
}
