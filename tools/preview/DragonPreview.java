import com.hexhaki.piece.dragon.DragonForm;
import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.DragonPose;
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
 * 2.7.0: renders the Uo Uo no Mi's dragon off-game to judge it (the house rule): the game's own model file, posed and skinned by the
 * game's own {@link DragonPose} (and {@link com.hexhaki.piece.dragon.DragonForm} for the transformation, the flight and the way back),
 * rasterised as Minecraft draws an entity: the texture by nearest texel, the two entity lights over a 0.4 ambient, a depth buffer, the
 * fire added full bright. A ground with a block grid and a player-sized figure give the scale.
 *
 * <p>javac -cp build/classes -d build/preview tools/preview/DragonPreview.java;
 * java -cp build/preview:build/classes DragonPreview <seiryu.sry> <seiryu.png> <out.png> [shot ...]
 * a shot: {@code clip:seconds:yaw:pitch:distance} ("form:..." poses through DragonForm, see {@link #pose}).
 */
public final class DragonPreview {
    static int W = 360;
    static int H = 300;
    static final int SS = 2;
    static BufferedImage texture;
    static DragonModel model;
    /** Blocks per model unit (the game's DragonForm.SCALE). */
    static float scale = DragonForm.SCALE;

    public static void main(String[] args) throws Exception {
        try (FileInputStream in = new FileInputStream(args[0])) {
            model = DragonModel.read(in);
        }
        texture = ImageIO.read(new File(args[1]));
        List<String> shots = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            if (args[i].startsWith("--mouth=")) {
                mouth = Float.parseFloat(args[i].substring(8));
            } else if (args[i].startsWith("--bite=")) {
                bite = Float.parseFloat(args[i].substring(7));
            } else if (args[i].startsWith("--colour=")) {
                // 2.7.1: the texture recoloured to a palette, as the game recolours it (dragon/DragonColours)
                com.hexhaki.piece.dragon.DragonColours.Palette palette = com.hexhaki.piece.dragon.DragonColours.palette(
                    Integer.parseInt(args[i].substring(9)));
                int tw = texture.getWidth();
                int th = texture.getHeight();
                int[] argb = texture.getRGB(0, 0, tw, th, null, 0, tw);
                com.hexhaki.piece.dragon.DragonColours.recolour(argb, tw, th, palette);
                BufferedImage recoloured = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
                recoloured.setRGB(0, 0, tw, th, argb, 0, tw);
                texture = recoloured;
            } else if (args[i].startsWith("--scale=")) {
                scale = Float.parseFloat(args[i].substring(8));
            } else if (args[i].startsWith("--size=")) {
                String[] wh = args[i].substring(7).split("x");
                W = Integer.parseInt(wh[0]);
                H = Integer.parseInt(wh[1]);
            } else {
                shots.add(args[i]);
            }
        }
        if (shots.isEmpty()) {
            for (String c : model.clips.keySet()) {
                shots.add(c + ":0:30:15:14");
            }
        }
        if (args[2].endsWith("/")) {
            // one PNG a shot (frames for a GIF: tools/dragon_gif.py)
            new File(args[2]).mkdirs();
            for (int i = 0; i < shots.size(); i++) {
                ImageIO.write(render(shots.get(i)), "png", new File(args[2], String.format("%04d.png", i)));
            }
            return;
        }
        int cols = Math.min(4, shots.size());
        int rows = (shots.size() + cols - 1) / cols;
        BufferedImage sheet = new BufferedImage(cols * W, rows * H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        for (int i = 0; i < shots.size(); i++) {
            BufferedImage img = render(shots.get(i));
            g.drawImage(img, (i % cols) * W, (i / cols) * H, null);
        }
        g.dispose();
        ImageIO.write(sheet, "png", new File(args[2]));
        System.out.println("bones " + model.bones() + " parts " + model.parts.length + " clips " + model.clips.keySet());
    }

    /** The pose a shot names (the clip at its time, or a DragonForm state). Returns the label. */
    static String pose(DragonPose pose, String[] p) {
        pose.rest();
        float seconds = Float.parseFloat(p[1]);
        if (p[0].equals("bind")) {
            for (int b = 0; b < model.bones(); b++) {
                for (int k = 0; k < 12; k++) {
                    pose.skin[b * 12 + k] = k % 5 == 0 ? 1.0F : 0.0F;
                }
            }
            return "bind";
        }
        if (!p[0].equals("rest")) {
            DragonModel.Clip clip = model.clips.get(p[0]);
            pose.clip(clip, seconds, false, 1.0F);
        }
        pose.solve();
        return p[0] + " " + p[1] + "s";
    }

    static DragonForm.Shape shape;
    /** 2.9.0: the turn and the climb the dragon is in (degrees a block of its way; FxPreview's --turn= and --climb=). */
    static float turn;
    static float climb;

    /**
     * A shot as the game places it: {@code g:phase:seconds:flight:lookPitch:bank:camYaw:camPitch:dist[:tx:ty:tz[:groundY]]}: the
     * player's feet at the origin facing +z, the dragon posed by DragonForm.pose and placed by DragonForm.placement.
     */
    static BufferedImage game(String[] p) {
        Raster r = scene(p);
        BufferedImage img = r.image(W, H);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(Color.BLACK);
        g.drawString(label, 6, 15);
        g.dispose();
        return img;
    }

    /** 2.7.6: the last scene's label and its dragon's mouth (placed as DragonRenderer places it), for FxPreview. */
    static String label;
    static double[] lastMouth;

    /** The shot {@link #game} draws, as its raster (supersampled, its depth kept): FxPreview draws the moves over it. */
    static Raster scene(String[] p) {
        if (shape == null) {
            shape = new DragonForm.Shape(model);
        }
        int phase = Integer.parseInt(p[1]);
        float seconds = Float.parseFloat(p[2]);
        float flight = Float.parseFloat(p[3]);
        float look = Float.parseFloat(p[4]);
        float bank = Float.parseFloat(p[5]);
        DragonPose pose = new DragonPose(model);
        float[] anchor = new float[3];
        float hover = phase == DragonForm.UNFORMING ? 1.0F : seconds;
        // 2.9.0: the body following its way (DragonForm.Trail): a turn of TURN degrees and a climb of CLIMB degrees a block of the way
        DragonForm.Trail trail = null;
        if (turn != 0.0F || climb != 0.0F) {
            trail = new DragonForm.Trail();
            for (int i = 0; i < trail.yaw.length; i++) {
                trail.yaw[i] = turn * shape.behind[i];
                trail.pitch[i] = climb * shape.behind[i];
            }
        }
        DragonForm.pose(model, shape, pose, phase, seconds, hover, flight, seconds, mouth, bite, anchor, trail);
        float[] place = new float[12];
        float[] shift = new float[3];
        DragonForm.shift(shape, phase, seconds, shift);
        DragonForm.placement(anchor, shift, DragonForm.grow(phase, seconds), 0.0F, look * flight, bank, place);
        lastMouth = null;
        float[] in = new float[6];
        if (DragonForm.mouth(shape, pose, DragonForm.jawAngle(mouth, bite), in)) {
            // as DragonRenderer places it: the point through the place, the way through its turn
            lastMouth = new double[6];
            for (int k = 0; k < 3; k++) {
                lastMouth[k] = place[k * 4] * in[0] + place[k * 4 + 1] * in[1] + place[k * 4 + 2] * in[2] + place[k * 4 + 3];
                lastMouth[3 + k] = place[k * 4] * in[3] + place[k * 4 + 1] * in[4] + place[k * 4 + 2] * in[5];
            }
            double l = Math.sqrt(lastMouth[3] * lastMouth[3] + lastMouth[4] * lastMouth[4] + lastMouth[5] * lastMouth[5]);
            for (int k = 3; k < 6; k++) {
                lastMouth[k] /= l;
            }
        }
        List<float[]> skinned = new ArrayList<>();
        for (DragonModel.Part part : model.parts) {
            float[] pos = new float[part.vertices() * 3];
            float[] nrm = new float[part.vertices() * 3];
            pose.skin(part, pos, nrm);
            for (int v = 0; v < part.vertices(); v++) {
                float x = pos[v * 3];
                float y = pos[v * 3 + 1];
                float z = pos[v * 3 + 2];
                for (int k = 0; k < 3; k++) {
                    pos[v * 3 + k] = place[k * 4] * x + place[k * 4 + 1] * y + place[k * 4 + 2] * z + place[k * 4 + 3];
                }
                float nx = nrm[v * 3];
                float ny = nrm[v * 3 + 1];
                float nz = nrm[v * 3 + 2];
                float len = 0.0F;
                for (int k = 0; k < 3; k++) {
                    nrm[v * 3 + k] = place[k * 4] * nx + place[k * 4 + 1] * ny + place[k * 4 + 2] * nz;
                    len += nrm[v * 3 + k] * nrm[v * 3 + k];
                }
                len = (float) Math.sqrt(len);
                for (int k = 0; k < 3; k++) {
                    nrm[v * 3 + k] /= len;
                }
            }
            skinned.add(pos);
            skinned.add(nrm);
        }
        double yaw = Math.toRadians(Double.parseDouble(p[6]));
        double pitch = Math.toRadians(Double.parseDouble(p[7]));
        double dist = Double.parseDouble(p[8]);
        double[] target = p.length >= 12 ? new double[] {Double.parseDouble(p[9]), Double.parseDouble(p[10]), Double.parseDouble(p[11])}
            : new double[] {0, 6, -4};
        double groundY = p.length >= 13 ? Double.parseDouble(p[12]) : 0.0;
        double[] eye = fixedEye != null ? fixedEye : new double[] {target[0] + dist * Math.cos(pitch) * Math.sin(yaw),
            target[1] + dist * Math.sin(pitch), target[2] + dist * Math.cos(pitch) * Math.cos(yaw)};
        Raster r = new Raster(W * SS, H * SS, eye, target);
        r.ground(groundY);
        boolean human = !DragonForm.humanHidden(phase, seconds);
        if (human) {
            r.figure(0, 0, 0);
        }
        if (DragonForm.shown(phase, seconds) && !hideDragon) {
            for (int i = 0; i < model.parts.length; i++) {
                r.mesh(model.parts[i], skinned.get(i * 2), skinned.get(i * 2 + 1));
            }
        }
        String[] names = {"human", "forming", "dragon", "unforming"};
        label = String.format("%s %.2fs fly %.2f%s", names[phase], seconds, flight, human ? " (human shown)" : "");
        return r;
    }

    /** client/dragon/DragonCamera's place behind and over the eye (kept the same by hand). */
    static final double CAM_BACK = 26.0;
    static final double CAM_UP = 11.0;

    /**
     * What the player sees in third person: {@code v:phase:seconds:flight:lookPitch:bank[:groundY]}: the game's shot posed as
     * {@link #game}, seen from DragonCamera's place (CAM_UP over the eye, CAM_BACK behind the look) along the look, 70 degrees high.
     */
    static BufferedImage view(String[] p) {
        double pitch = Math.toRadians(Double.parseDouble(p[4]));
        double[] look = {0, -Math.sin(pitch), Math.cos(pitch)};
        double[] eye = {0, 1.62 + CAM_UP, 0};
        double[] cam = {eye[0] - look[0] * CAM_BACK, eye[1] - look[1] * CAM_BACK, eye[2] - look[2] * CAM_BACK};
        String ground = p.length > 6 ? p[6] : "0";
        String[] g = {"g", p[1], p[2], p[3], p[4], p[5], "0", "0", "1", String.valueOf(cam[0] + look[0]), String.valueOf(cam[1] + look[1]),
            String.valueOf(cam[2] + look[2]), ground};
        fixedEye = cam;
        fov = 70;
        try {
            return game(g);
        } finally {
            fixedEye = null;
            fov = 50;
        }
    }

    /** {@link #view}'s raster (FxPreview). */
    static Raster viewScene(String[] p) {
        double pitch = Math.toRadians(Double.parseDouble(p[4]));
        double[] look = {0, -Math.sin(pitch), Math.cos(pitch)};
        double[] eye = {0, 1.62 + CAM_UP, 0};
        double[] cam = {eye[0] - look[0] * CAM_BACK, eye[1] - look[1] * CAM_BACK, eye[2] - look[2] * CAM_BACK};
        String ground = p.length > 6 ? p[6] : "0";
        String[] g = {"g", p[1], p[2], p[3], p[4], p[5], "0", "0", "1", String.valueOf(cam[0] + look[0]), String.valueOf(cam[1] + look[1]),
            String.valueOf(cam[2] + look[2]), ground};
        fixedEye = cam;
        fov = 70;
        try {
            return scene(g);
        } finally {
            fixedEye = null;
            fov = 50;
        }
    }

    static double[] fixedEye;
    /** 2.7.7: the player's own dragon is not drawn in first person (FxPreview's fp: shots). */
    static boolean hideDragon;
    /** 2.7.2: the jaw and the bite for the next shots (--mouth=, --bite=). */
    static float mouth;
    static float bite = -1.0F;
    static double fov = 50;

    static BufferedImage render(String shot) {
        String[] p = shot.split(":");
        if (p[0].equals("g")) {
            return game(p);
        }
        if (p[0].equals("v")) {
            return view(p);
        }
        DragonPose pose = new DragonPose(model);
        String label = pose(pose, p);
        double yaw = Math.toRadians(Double.parseDouble(p[2]));
        double pitch = Math.toRadians(Double.parseDouble(p[3]));
        double dist = Double.parseDouble(p[4]);
        // aim at the middle of the model's bounds
        double[] lo = {1e9, 1e9, 1e9};
        double[] hi = {-1e9, -1e9, -1e9};
        List<float[]> skinned = new ArrayList<>();
        for (DragonModel.Part part : model.parts) {
            float[] pos = new float[part.vertices() * 3];
            float[] nrm = new float[part.vertices() * 3];
            pose.skin(part, pos, nrm);
            for (int k = 0; k < pos.length; k++) {
                pos[k] *= scale;
            }
            skinned.add(pos);
            skinned.add(nrm);
            for (int v = 0; v < part.vertices(); v++) {
                for (int k = 0; k < 3; k++) {
                    lo[k] = Math.min(lo[k], pos[v * 3 + k]);
                    hi[k] = Math.max(hi[k], pos[v * 3 + k]);
                }
            }
        }
        double[] target = {(lo[0] + hi[0]) / 2, (lo[1] + hi[1]) / 2, (lo[2] + hi[2]) / 2};
        if (p.length >= 8) {
            target = new double[] {Double.parseDouble(p[5]), Double.parseDouble(p[6]), Double.parseDouble(p[7])};
        }
        double[] eye = {target[0] + dist * Math.cos(pitch) * Math.sin(yaw), target[1] + dist * Math.sin(pitch),
            target[2] + dist * Math.cos(pitch) * Math.cos(yaw)};
        Raster r = new Raster(W * SS, H * SS, eye, target);
        r.ground(Math.floor(lo[1]));
        r.figure(target[0] + (hi[0] - lo[0]) * 0.5 + 1.5, Math.floor(lo[1]), target[2]);
        for (int i = 0; i < model.parts.length; i++) {
            DragonModel.Part part = model.parts[i];
            r.mesh(part, skinned.get(i * 2), skinned.get(i * 2 + 1));
        }
        BufferedImage img = r.image(W, H);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(Color.BLACK);
        g.drawString(label + String.format(" (%.1f x %.1f x %.1f)", hi[0] - lo[0], hi[1] - lo[1], hi[2] - lo[2]), 6, 15);
        g.dispose();
        return img;
    }

    /** A z-buffered rasteriser with a perspective camera. */
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
                float t = y / (float) h;
                int c = mix(0xA9C9E8, 0xE4EEF6, t);
                for (int x = 0; x < w; x++) {
                    this.rgb[y * w + x] = c;
                }
            }
            this.eye = eye;
            this.fwd = norm(sub(target, eye));
            this.right = norm(cross(this.fwd, new double[] {0, 1, 0}));
            this.up = cross(this.right, this.fwd);
            this.focal = h / 2.0 / Math.tan(Math.toRadians(fov) / 2);
        }

        /** Screen x, y and depth of a point, or null behind the eye. */
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
                    // the ray through the pixel, hit with the plane
                    double sx = (px - this.w / 2.0) / this.focal;
                    double sy = -(py - this.h / 2.0) / this.focal;
                    double[] dir = {this.fwd[0] + this.right[0] * sx + this.up[0] * sy, this.fwd[1] + this.right[1] * sx + this.up[1] * sy,
                        this.fwd[2] + this.right[2] * sx + this.up[2] * sy};
                    if (dir[1] >= -1e-6) {
                        continue;
                    }
                    double t = (y - this.eye[1]) / dir[1];
                    double gx = this.eye[0] + dir[0] * t;
                    double gz = this.eye[2] + dir[2] * t;
                    double fx = gx - Math.floor(gx);
                    double fz = gz - Math.floor(gz);
                    boolean line = fx < 0.04 || fz < 0.04;
                    int c = line ? 0x6E8A55 : 0x86A86A;
                    float zc = (float) (t * dot(dir, this.fwd));
                    int i = py * this.w + px;
                    this.rgb[i] = c;
                    this.depth[i] = zc;
                }
            }
        }

        /** A player-sized figure (1.8 blocks) standing at x, y, z. */
        void figure(double x, double y, double z) {
            box(x - 0.3, y, z - 0.15, x + 0.3, y + 0.75, z + 0.15, 0x3A4A7A);
            box(x - 0.3, y + 0.75, z - 0.15, x + 0.3, y + 1.4, z + 0.15, 0x3D8BC4);
            box(x - 0.25, y + 1.4, z - 0.25, x + 0.25, y + 1.9, z + 0.25, 0xD9A77E);
        }

        void box(double x0, double y0, double z0, double x1, double y1, double z1, int color) {
            double[][] c = {{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
            int[][] f = {{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}};
            float[] shade = {0.8F, 0.8F, 0.6F, 0.6F, 1.0F, 0.5F};
            for (int k = 0; k < 6; k++) {
                int col = scale(color, shade[k]);
                tri(c[f[k][0]], c[f[k][1]], c[f[k][2]], null, null, null, col);
                tri(c[f[k][0]], c[f[k][2]], c[f[k][3]], null, null, null, col);
            }
        }

        void mesh(DragonModel.Part part, float[] pos, float[] nrm) {
            int[] q = part.quads;
            for (int i = 0; i < q.length; i += 4) {
                int[] tri1 = {q[i], q[i + 1], q[i + 2]};
                face(part, pos, nrm, tri1);
                if (q[i + 3] != q[i + 2]) {
                    face(part, pos, nrm, new int[] {q[i], q[i + 2], q[i + 3]});
                }
            }
        }

        void face(DragonModel.Part part, float[] pos, float[] nrm, int[] idx) {
            double[][] p = new double[3][];
            double[][] uv = new double[3][];
            double[] light = new double[3];
            for (int k = 0; k < 3; k++) {
                int v = idx[k];
                p[k] = new double[] {pos[v * 3], pos[v * 3 + 1], pos[v * 3 + 2]};
                uv[k] = new double[] {part.uv[v * 2], part.uv[v * 2 + 1]};
                double nx = nrm[v * 3];
                double ny = nrm[v * 3 + 1];
                double nz = nrm[v * 3 + 2];
                light[k] = part.kind == DragonModel.GLOW ? 1.0 : entityLight(nx, ny, nz);
            }
            tri(p[0], p[1], p[2], uv, light, part, 0);
        }

        /** Minecraft's entity lighting: two lights over a 0.4 ambient. */
        static double entityLight(double nx, double ny, double nz) {
            double[] l0 = norm(new double[] {0.2, 1.0, -0.7});
            double[] l1 = norm(new double[] {-0.2, 1.0, 0.7});
            double d = Math.max(0, nx * l0[0] + ny * l0[1] + nz * l0[2]) + Math.max(0, nx * l1[0] + ny * l1[1] + nz * l1[2]);
            return Math.min(1.0, 0.4 + 0.6 * d);
        }

        void tri(double[] a, double[] b, double[] c, double[][] uv, double[] light, DragonModel.Part part, int flat) {
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
                    // perspective-correct
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
                        double u = (w0 * uv[0][0] / pa[2] + w1 * uv[1][0] / pb[2] + w2 * uv[2][0] / pc[2]) * z;
                        double v = (w0 * uv[0][1] / pa[2] + w1 * uv[1][1] / pb[2] + w2 * uv[2][1] / pc[2]) * z;
                        int tx = Math.floorMod((int) Math.floor(u * tw), tw);
                        int ty = Math.floorMod((int) Math.floor(v * th), th);
                        int texel = texture.getRGB(tx, ty);
                        if (part.kind == DragonModel.CUTOUT && (texel >>> 24) < 26) {
                            continue;
                        }
                        double l = (w0 * light[0] / pa[2] + w1 * light[1] / pb[2] + w2 * light[2] / pc[2]) * z;
                        col = scale(texel, (float) l);
                        if (part.kind == DragonModel.GLOW) {
                            // added, without writing depth
                            this.rgb[i] = add(this.rgb[i], col);
                            continue;
                        }
                    }
                    this.depth[i] = (float) z;
                    this.rgb[i] = col;
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
        int r = (int) ((a >> 16 & 255) * (1 - t) + (b >> 16 & 255) * t);
        int g = (int) ((a >> 8 & 255) * (1 - t) + (b >> 8 & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return r << 16 | g << 8 | bl;
    }

    static int scale(int c, float f) {
        int r = Math.min(255, (int) ((c >> 16 & 255) * f));
        int g = Math.min(255, (int) ((c >> 8 & 255) * f));
        int b = Math.min(255, (int) ((c & 255) * f));
        return r << 16 | g << 8 | b;
    }

    static int add(int a, int b) {
        return Math.min(255, (a >> 16 & 255) + (b >> 16 & 255)) << 16 | Math.min(255, (a >> 8 & 255) + (b >> 8 & 255)) << 8
            | Math.min(255, (a & 255) + (b & 255));
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
