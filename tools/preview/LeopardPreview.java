import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.DragonPose;
import com.hexhaki.piece.neko.LeopardForm;
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
 * 3.7.9: renders the owner's leopard (assets/hexhaki/neko/lucci.sry, tools/leopard_bake.py) off-game to judge it (the house rule), posed
 * by the game's own {@link LeopardForm} and skinned by {@link DragonPose}, coloured as the game colours it ({@link LeopardForm#tint}: the
 * texture lit by one light, the black flames as LeopardForm.flameColour), on PhoenixPreview's rasteriser (a depth buffer, the ground's
 * block grid, a player-sized figure for the scale).
 *
 * <p>java -cp prev:build/classes LeopardPreview <lucci.sry> <lucci.png> <out.png | dir/> [--size=WxH] shot ...
 * a shot: {@code state:seconds:yaw:pitch:dist[:tx:ty:tz]}; state: "bind" (the file's rest), "take=<name>" (one of the file's takes
 * as it is), or LeopardForm's states (idle, run, jump, fall, land, walk, form, unform; face=<0..3>; hands=<0..3>).
 */
public final class LeopardPreview {
    static DragonModel model;
    static LeopardForm.Shape shape;
    static final List<double[]> FLAMES = new ArrayList<>();
    static final java.util.Map<Integer, float[]> ANCHOR_POS = new java.util.HashMap<>();

    public static void main(String[] args) throws Exception {
        try (FileInputStream in = new FileInputStream(args[0])) {
            model = DragonModel.read(in);
        }
        shape = new LeopardForm.Shape(model);
        PhoenixPreview.texture = ImageIO.read(new File(args[1]));
        List<String> shots = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            if (args[i].startsWith("--size=")) {
                String[] wh = args[i].substring(7).split("x");
                PhoenixPreview.W = Integer.parseInt(wh[0]);
                PhoenixPreview.H = Integer.parseInt(wh[1]);
            } else {
                shots.add(args[i]);
            }
        }
        int w = PhoenixPreview.W;
        int h = PhoenixPreview.H;
        if (args[2].endsWith("/")) {
            new File(args[2]).mkdirs();
            for (int i = 0; i < shots.size(); i++) {
                ImageIO.write(render(shots.get(i)), "png", new File(args[2], String.format("%04d.png", i)));
            }
            return;
        }
        int cols = Math.min(3, shots.size());
        int rows = (shots.size() + cols - 1) / cols;
        BufferedImage sheet = new BufferedImage(cols * w, rows * h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        for (int i = 0; i < shots.size(); i++) {
            g.drawImage(render(shots.get(i)), (i % cols) * w, (i / cols) * h, null);
        }
        g.dispose();
        ImageIO.write(sheet, "png", new File(args[2]));
    }

    static BufferedImage render(String shot) {
        String[] p = shot.split(":");
        String state = p[0];
        double t = Double.parseDouble(p[1]);
        DragonPose pose = new DragonPose(model);
        LeopardForm.Motion mo = new LeopardForm.Motion();
        float scale = LeopardForm.SCALE;
        // 3.7.9: the change ("form", "unform": seconds into it), a blow with its effects ("fx1" to "fx5" as the leopard, "hfx1" to "hfx5"
        // the human: seconds after it was thrown)
        int phase = LeopardForm.LEOPARD;
        float ticks = (float) t * 20.0F;
        boolean human = false;
        int fxBlow = 0;
        if (state.startsWith("form") || state.startsWith("unform")) {
            phase = state.startsWith("form") ? LeopardForm.FORMING : LeopardForm.UNFORMING;
            state = "idle";
        } else if (state.startsWith("fx") || state.startsWith("hfx")) {
            human = state.startsWith("hfx");
            fxBlow = state.charAt(state.length() - 1) - '0';
            state = "blow" + fxBlow;
        }
        float there = LeopardForm.revealed(phase, ticks);
        if (human) {
            there = 0.0F;
        }
        if (state.equals("bind")) {
            pose.rest();
            pose.solve();
        } else if (state.startsWith("take=")) {
            pose.rest();
            pose.clip(model.clips.get(state.substring(5)), (float) t, true, 1.0F);
            pose.solve();
        } else {
            LeopardForm.preview(model, shape, pose, state, (float) t, mo);
        }
        double yaw = Math.toRadians(Double.parseDouble(p[2]));
        double pitch = Math.toRadians(Double.parseDouble(p[3]));
        double dist = Double.parseDouble(p[4]);
        int W = PhoenixPreview.W * 2;
        int H = PhoenixPreview.H * 2;
        double[] lo = {1e9, 1e9, 1e9};
        double[] hi = {-1e9, -1e9, -1e9};
        List<float[]> skinned = new ArrayList<>();
        List<DragonModel.Part> shown = new ArrayList<>();
        int faces = 0;
        for (int i = 0; i < model.parts.length; i++) {
            DragonModel.Part part = model.parts[i];
            if (!LeopardForm.shown(shape, i, mo) || there < 0.01F) {
                continue;
            }
            float[] pos = new float[part.vertices() * 3];
            float[] nrm = new float[part.vertices() * 3];
            pose.skin(part, pos, nrm);
            LeopardForm.flame(shape, i, part, pos, nrm, (float) t);
            for (int v = 0; v < part.vertices(); v++) {
                for (int r = 0; r < 3; r++) {
                    pos[v * 3 + r] *= scale;
                    lo[r] = Math.min(lo[r], pos[v * 3 + r]);
                    hi[r] = Math.max(hi[r], pos[v * 3 + r]);
                }
            }
            faces += part.quads.length / 4;
            ANCHOR_POS.put(i, pos);
            if (shape.kind[i] >= LeopardForm.HAIR) {
                // the black flames: alpha-blended quads after the body, far to near (as the game draws them after the particles)
                int[] q = part.quads;
                for (int k = 0; k < q.length; k += 4) {
                    double[] quad = new double[17];
                    for (int c = 0; c < 4; c++) {
                        int v = q[k + c];
                        quad[c * 3] = pos[v * 3];
                        quad[c * 3 + 1] = pos[v * 3 + 1];
                        quad[c * 3 + 2] = pos[v * 3 + 2];
                        quad[12 + c] = LeopardForm.flameColour(shape, i, v, pos[v * 3] / scale, pos[v * 3 + 2] / scale, (float) t) & 0xFFFFFFFFL;
                    }
                    FLAMES.add(quad);
                }
                continue;
            }
            float[] col = new float[part.vertices() * 4];
            float[] tint = new float[4];
            for (int v = 0; v < part.vertices(); v++) {
                LeopardForm.tint(shape, i, part, v, nrm[v * 3], nrm[v * 3 + 1], nrm[v * 3 + 2], pos[v * 3 + 1] / scale, (float) t, tint);
                col[v * 4] = tint[0];
                col[v * 4 + 1] = tint[1];
                col[v * 4 + 2] = tint[2];
                col[v * 4 + 3] = tint[3];
            }
            skinned.add(pos);
            skinned.add(col);
            shown.add(part);
        }
        if (there <= 0.0F) {
            lo = new double[] {-1, 0, -1};
            hi = new double[] {1, 2, 1};
        }
        double[] target = {(lo[0] + hi[0]) / 2, (lo[1] + hi[1]) / 2, (lo[2] + hi[2]) / 2};
        if (p.length >= 8) {
            target = new double[] {Double.parseDouble(p[5]), Double.parseDouble(p[6]), Double.parseDouble(p[7])};
        }
        double[] eye = {target[0] + dist * Math.cos(pitch) * Math.sin(yaw), target[1] + dist * Math.sin(pitch),
            target[2] + dist * Math.cos(pitch) * Math.cos(yaw)};
        PhoenixPreview.Raster ra = new PhoenixPreview.Raster(W, H, eye, target);
        ra.ground(0.0);
        ra.figure(2.2, 0.0, 0.0);
        boolean humanShown = human || phase != LeopardForm.LEOPARD && !LeopardForm.humanHidden(phase, ticks);
        if (humanShown) {
            ra.figure(0.0, 0.0, 0.0);
        }
        // the burning line: nothing of the leopard above it (its quads cut as client/neko/NekoFx cuts them)
        float cut = there >= 1.0F ? Float.MAX_VALUE : (float) (shape.height * there * LeopardForm.SCALE);
        List<double[]> fire = new ArrayList<>();
        com.hexhaki.piece.dragon.FireMesh.Sink sink = (xyz, argb) -> {
            double[] q = new double[17];
            for (int k = 0; k < 12; k++) {
                q[k] = xyz[k] + eye[k % 3];
            }
            for (int k = 0; k < 4; k++) {
                q[12 + k] = argb[k] & 0xFFFFFFFFL;
            }
            fire.add(q);
        };
        double ex = -eye[0];
        double ey = -eye[1];
        double ez = -eye[2];
        if (phase == LeopardForm.FORMING || phase == LeopardForm.UNFORMING) {
            float[] ball = new float[3];
            LeopardForm.ball(phase, ticks, ball);
            boolean big = ball[2] > 0.5F;
            com.hexhaki.piece.neko.LeopardArt.ball(sink, ex, ey + (big ? LeopardForm.BALL_BIG_Y : LeopardForm.BALL_SMALL_Y), ez,
                big ? LeopardForm.BALL_BIG : LeopardForm.BALL_SMALL, Math.sin(yaw), Math.cos(yaw), ball[0], ball[1], ticks, 3);
            if (phase == LeopardForm.FORMING && ticks < LeopardForm.BALL_IN + 2.0F) {
                com.hexhaki.piece.neko.LeopardArt.human(sink, ex, ey, ez, 0.0, Math.min(1.0F, ticks / 3.0F) * (1.0F - ball[0] * 0.7F), ticks, 3);
            }
            if (phase == LeopardForm.FORMING) {
                com.hexhaki.piece.neko.LeopardArt.roar(sink, ex, ey, ez, ticks - LeopardForm.ROAR, 3);
            }
        }
        if (there >= 1.0F && !human) {
            // the tongues of black flame off the mane, the tail's tuft and the ribbon
            for (int k = 0; k < shape.anchors.length / 2; k++) {
                float[] ap = ANCHOR_POS.get(shape.anchors[k * 2]);
                if (ap == null) {
                    continue;
                }
                int v = shape.anchors[k * 2 + 1];
                com.hexhaki.piece.neko.LeopardArt.tongue(sink, ap[v * 3] - eye[0], ap[v * 3 + 1] - eye[1], ap[v * 3 + 2] - eye[2], shape.anchorSize[k],
                    0.0, 0.0, ticks, k);
            }
        }
        if (fxBlow > 0) {
            double big = human ? 1.0 : 1.7;
            double chest = human ? 1.2 : 3.0;
            double reach = human ? 3.4 * 0.6 : 5.2 * 0.6;
            double age = ticks - (fxBlow == 5 ? 5 : 3);
            if (fxBlow == 5) {
                com.hexhaki.piece.neko.LeopardArt.rokuogan(sink, ex, ey + chest, ez - reach, 0, 0, -1, big, age, 4);
            } else {
                com.hexhaki.piece.neko.LeopardArt.strike(sink, fxBlow, ex, ey + chest, ez, 0, 0, -1, big, ticks - 1.0, 4);
                com.hexhaki.piece.neko.LeopardArt.land(sink, ex, ey + chest, ez - reach, 0, 0, -1, big, true, age, 4);
            }
            ra.figure(0.0, 0.0, -reach - 0.3);
        }
        for (int k = 0; k < shown.size(); k++) {
            DragonModel.Part part = shown.get(k);
            float[] pos = skinned.get(k * 2);
            if (cut < Float.MAX_VALUE) {
                int[] q = part.quads;
                int[] keep = new int[q.length];
                int n = 0;
                for (int i = 0; i < q.length; i += 4) {
                    double top = Math.max(Math.max(pos[q[i] * 3 + 1], pos[q[i + 1] * 3 + 1]), Math.max(pos[q[i + 2] * 3 + 1], pos[q[i + 3] * 3 + 1]));
                    if (top <= cut) {
                        System.arraycopy(q, i, keep, n, 4);
                        n += 4;
                    }
                }
                for (int i = 0; i < n; i += 4) {
                    ra.face(part, pos, skinned.get(k * 2 + 1), keep[i], keep[i + 1], keep[i + 2]);
                    if (keep[i + 3] != keep[i + 2]) {
                        ra.face(part, pos, skinned.get(k * 2 + 1), keep[i], keep[i + 2], keep[i + 3]);
                    }
                }
                continue;
            }
            ra.mesh(part, pos, skinned.get(k * 2 + 1));
        }
        if (cut < Float.MAX_VALUE) {
            FLAMES.removeIf(q -> Math.max(Math.max(q[1], q[4]), Math.max(q[7], q[10])) > cut);
        }
        FLAMES.addAll(fire);
        for (double[] q : FLAMES) {
            double mx = (q[0] + q[3] + q[6] + q[9]) / 4 - eye[0];
            double my = (q[1] + q[4] + q[7] + q[10]) / 4 - eye[1];
            double mz = (q[2] + q[5] + q[8] + q[11]) / 4 - eye[2];
            q[16] = mx * mx + my * my + mz * mz;
        }
        FLAMES.sort((x, y) -> Double.compare(y[16], x[16]));
        for (double[] q : FLAMES) {
            ra.blend(q);
        }
        FLAMES.clear();
        BufferedImage img = ra.image(PhoenixPreview.W, PhoenixPreview.H);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(Color.BLACK);
        g.drawString(shot + String.format("  (%.1f x %.1f x %.1f blocks, %d faces)", hi[0] - lo[0], hi[1] - lo[1], hi[2] - lo[2], faces), 6, 15);
        g.dispose();
        return img;
    }
}
