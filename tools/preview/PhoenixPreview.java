import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.DragonPose;
import com.hexhaki.piece.phoenix.PhoenixForm;
import com.hexhaki.piece.phoenix.PhoenixLook;
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
 * 3.6.7: renders the owner's phoenix off-game to judge it (the house rule): the game's own baked model (assets/hexhaki/phoenix/marco.sry,
 * tools/phoenix_bake.py), posed by the game's own {@link PhoenixForm}, skinned by {@link DragonPose}, coloured as the game colours it
 * ({@link PhoenixLook}: the atlas's colour times the file's cel ramp, full bright), with a depth buffer, a ground with a block grid and a
 * player-sized figure for the scale.
 *
 * <p>java -cp prev:build/classes PhoenixPreview <marco.sry> <marco.png> <out.png | dir/> [--size=WxH] shot ...
 * a shot: {@code state:seconds:yaw:pitch:dist[:tx:ty:tz]}; state one of rest, soar (the owner's take), or PhoenixForm's states (hover,
 * fly, glide, dive, human, form, unform): see {@link PhoenixForm#pose}.
 */
public final class PhoenixPreview {
    static int W = 480;
    static int H = 360;
    static final int SS = 2;
    static BufferedImage texture;
    static DragonModel model;
    static PhoenixForm.Shape shape;
    static int tongueQuads;

    public static void main(String[] args) throws Exception {
        try (FileInputStream in = new FileInputStream(args[0])) {
            model = DragonModel.read(in);
        }
        shape = new PhoenixForm.Shape(model);
        texture = ImageIO.read(new File(args[1]));
        List<String> shots = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            if (args[i].startsWith("--size=")) {
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
        Graphics2D g = sheet.createGraphics();
        for (int i = 0; i < shots.size(); i++) {
            g.drawImage(render(shots.get(i)), (i % cols) * W, (i / cols) * H, null);
        }
        g.dispose();
        ImageIO.write(sheet, "png", new File(args[2]));
    }

    /** 3.7.2: where the human stand-in stands (a move scene moves it). */
    static double OX;
    static double OY;
    static double OZ;

    /**
     * 3.7.2: a move scene (the state "m-fujih", "m-fujip", "m-talon", "m-hawk" at {@code ticks}): the phoenix's pose and place (or none),
     * the human's place, their temporary wings, a dummy to seize, and the move's fire (phoenix/PhoenixMoveArt), as client/phoenix/PhoenixFx
     * and PhoenixMoveFx draw them from server/PhoenixMoves' timings.
     */
    static final class Scene {
        boolean bird;
        final PhoenixForm.Motion mo = new PhoenixForm.Motion();
        double bx;
        double by;
        double bz;
        float yaw;
        float bank;
        float beat;
        boolean human;
        double hx;
        double hy;
        double hz;
        int wings = -1;
        float wingsSize;
        boolean dummy;
        double dx;
        double dy;
        double dz;
        java.util.function.BiConsumer<com.hexhaki.piece.dragon.FireMesh.Sink, double[]> fire;
    }

    static double smooth(double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        return t * t * (3.0 - 2.0 * t);
    }

    static Scene scene(String state, double t) {
        if (!state.startsWith("m-")) {
            return null;
        }
        Scene sc = new Scene();
        sc.beat = (float) (t / 20.0 * PhoenixForm.beatRate(sc.mo));
        switch (state) {
            case "m-fujih" -> {
                int c = com.hexhaki.piece.phoenix.PhoenixMoveArt.FUJI_CHARGE;
                int d = com.hexhaki.piece.phoenix.PhoenixMoveArt.FUJI_DASH;
                double reach = com.hexhaki.piece.phoenix.PhoenixMoveArt.FUJI_REACH;
                sc.human = true;
                sc.hz = -reach * Math.max(0.0, Math.min(1.0, (t - c) / d));
                sc.wings = 0;
                sc.wingsSize = (float) Math.min(Math.min(1.0, t / 4.0), Math.max(0.0, (c + d + 14 - t) / 4.0));
                double hz = sc.hz;
                sc.fire = (sink, e) -> {
                    if (t < c) {
                        com.hexhaki.piece.phoenix.PhoenixMoveArt.fujiCharge(sink, -e[0], -e[1], -e[2], t, 1.0, 3);
                    } else {
                        com.hexhaki.piece.phoenix.PhoenixMoveArt.fujiTrail(sink, -e[0], -e[1], -e[2], 0, 0, -reach, Math.min(1.0, (t - c) / d), t - c, 1.0, 3);
                        if (t < c + d) {
                            com.hexhaki.piece.phoenix.PhoenixMoveArt.fujiSheath(sink, -e[0], 1.0 - e[1], hz - e[2], 0, 0, -1, t - c, 1.0, 3);
                        }
                    }
                    if (t >= c + d) {
                        com.hexhaki.piece.phoenix.PhoenixMoveArt.fujiBurst(sink, -e[0], 1.0 - e[1], -reach - 1.4 - e[2], 0, -1, t - c - d, 1.0, 3);
                    }
                };
            }
            case "m-fujip" -> {
                int w = com.hexhaki.piece.phoenix.PhoenixMoveArt.FUJI_WIND;
                int r = com.hexhaki.piece.phoenix.PhoenixMoveArt.FUJI_RAM;
                double reach = com.hexhaki.piece.phoenix.PhoenixMoveArt.FUJI_RAM_REACH;
                sc.bird = true;
                double got = Math.max(0.0, Math.min(1.0, (t - w) / r));
                sc.by = 2.0;
                sc.bz = -reach * got;
                sc.mo.sweep = (float) Math.min(1.0, Math.min(t / 3.0, Math.max(0.0, (w + 1 - t) / 2.0)));
                sc.mo.ram = (float) (t >= w - 1 && t < w + r + 2 ? Math.min(1.0, Math.min((t - w + 1) / 2.0, (w + r + 2 - t) / 2.0)) : 0.0);
                sc.mo.speed = (float) (t >= w ? Math.max(0.0, 1.0 - Math.max(0.0, t - w - r) / 10.0) : 0.0);
                double g = (t - w - r) / com.hexhaki.piece.phoenix.PhoenixMoveArt.FUJI_GLIDE;
                if (g > 0.0 && g < 1.0) {
                    sc.mo.glide = 1.0F;
                    sc.bank = (float) (38.0 * Math.sin(Math.PI * g));
                    sc.yaw = (float) (-140.0 * smooth(g));
                }
                double bz = sc.bz;
                sc.fire = (sink, e) -> {
                    if (t < w + 2) {
                        com.hexhaki.piece.phoenix.PhoenixMoveArt.fujiCharge(sink, -e[0], 2.6 - e[1], -e[2], t, 2.2, 5);
                    }
                    if (t >= w) {
                        com.hexhaki.piece.phoenix.PhoenixMoveArt.fujiTrail(sink, -e[0], 2.0 - e[1], -e[2], 0, 0, -reach, got, t - w, 2.0, 5);
                        if (t < w + r) {
                            com.hexhaki.piece.phoenix.PhoenixMoveArt.fujiSheath(sink, -e[0], 3.6 - e[1], bz - e[2], 0, 0, -1, t - w, 2.0, 5);
                        }
                    }
                    if (t >= w + r) {
                        com.hexhaki.piece.phoenix.PhoenixMoveArt.fujiBurst(sink, -e[0], 3.0 - e[1], -reach - 2.4 - e[2], 0, -1, t - w - r, 2.0, 5);
                    }
                };
            }
            case "m-talon" -> {
                int c = com.hexhaki.piece.phoenix.PhoenixMoveArt.TALON_CROUCH;
                int rise = com.hexhaki.piece.phoenix.PhoenixMoveArt.TALON_RISE;
                int hang = com.hexhaki.piece.phoenix.PhoenixMoveArt.TALON_HANG;
                double height = com.hexhaki.piece.phoenix.PhoenixMoveArt.TALON_HEIGHT;
                double land = c + rise + hang + 4;
                double tz = -com.hexhaki.piece.phoenix.PhoenixMoveArt.TALON_REACH * 0.6;
                sc.human = true;
                if (t < c) {
                    sc.hy = 0.0;
                } else if (t < c + rise) {
                    double u = (t - c) / rise;
                    sc.hy = height * (1.0 - (1.0 - u) * (1.0 - u));
                } else if (t < c + rise + hang) {
                    sc.hy = height;
                } else {
                    double u = Math.min(1.0, (t - c - rise - hang) / 4.0);
                    sc.hy = height * (1.0 - u * u);
                    sc.hz = tz * u;
                }
                double hy = sc.hy;
                double hz = sc.hz;
                sc.fire = (sink, e) -> {
                    if (t < c) {
                        com.hexhaki.piece.phoenix.PhoenixMoveArt.fujiCharge(sink, -e[0], -e[1], -e[2], t * 2.0, 0.7, 4);
                    }
                    com.hexhaki.piece.phoenix.PhoenixMoveArt.talonLeap(sink, -e[0], -e[1], -e[2], t - c);
                    double top = t - c - rise;
                    if (top >= 0.0 && t < land) {
                        if (top < hang + 4) {
                            com.hexhaki.piece.phoenix.PhoenixMoveArt.talonWind(sink, -e[0], hy + 1.0 - e[1], hz - e[2], top);
                        }
                        double a = Math.toRadians(-com.hexhaki.piece.phoenix.PhoenixMoveArt.talonLeg((float) t));
                        double hx = 0.12;
                        double hipY = hy + 0.75;
                        com.hexhaki.piece.phoenix.PhoenixMoveArt.legFire(sink, hx - e[0], hipY - e[1], hz - e[2], hx - e[0], hipY - Math.cos(a) * 0.72 - e[1],
                            hz - Math.sin(a) * 0.72 - e[2], top, Math.min(1.0, top / 4.0), 4);
                    }
                    if (t >= land) {
                        com.hexhaki.piece.phoenix.PhoenixMoveArt.talonBlast(sink, -e[0], -e[1], tz - e[2], t - land,
                            com.hexhaki.piece.phoenix.PhoenixMoveArt.TALON_RADIUS, 4);
                    }
                };
            }
            case "m-hawk" -> {
                int w = com.hexhaki.piece.phoenix.PhoenixMoveArt.HAWK_WRAP;
                int rush = 14;
                int whirl = com.hexhaki.piece.phoenix.PhoenixMoveArt.HAWK_WHIRL;
                sc.bird = true;
                sc.by = 1.6;
                double got = Math.max(0.0, Math.min(rush, t - w));
                sc.bz = -1.5 * got;
                sc.mo.sweep = (float) Math.min(1.0, Math.min(t / 2.0, Math.max(0.0, (w + 1 - t) / 2.0)));
                sc.mo.reach = (float) (t >= w ? Math.min(1.0, (t - w) / 3.0) * Math.min(1.0, Math.max(0.0, (w + rush + whirl + 2 - t) / 3.0)) : 0.0);
                sc.mo.speed = (float) (t >= w && t < w + rush ? 0.8 : 0.0);
                double wh = (t - w - rush) / whirl;
                sc.dummy = true;
                sc.dz = -1.5 * rush - 1.2;
                if (wh > 0.0) {
                    double f = Math.min(1.0, wh);
                    sc.yaw = (float) (360.0 * smooth(f));
                    double ang = 2 * Math.PI * smooth(f);
                    sc.dx = -Math.sin(ang) * 2.2;
                    sc.dz = sc.bz - Math.cos(ang) * 2.2;
                    sc.dy = sc.by - 0.6 - 0.9;
                    if (wh >= 1.0) {
                        double fly = (t - w - rush - whirl) * 1.2;
                        sc.dz = sc.bz - 2.2 - fly;
                        sc.dy = Math.max(0.0, sc.by - 1.5 + 0.6 * fly - 0.08 * fly * fly);
                    }
                }
                double bz = sc.bz;
                double by = sc.by;
                sc.fire = (sink, e) -> {
                    com.hexhaki.piece.phoenix.PhoenixMoveArt.hawkFlash(sink, -e[0], by + 1.4 - e[1], -e[2], t);
                    if (t >= w && t < w + rush) {
                        com.hexhaki.piece.phoenix.PhoenixMoveArt.hawkStreaks(sink, -e[0], by + 1.2 - e[1], bz - e[2], 0, 0, -1, t - w, 6);
                        com.hexhaki.piece.phoenix.PhoenixMoveArt.fujiSheath(sink, -e[0], by + 1.2 - e[1], bz - e[2], 0, 0, -1, t - w, 1.3, 6);
                    }
                    if (t >= w + rush) {
                        com.hexhaki.piece.phoenix.PhoenixMoveArt.hawkWhirl(sink, -e[0], by + 0.8 - e[1], bz - e[2], t - w - rush);
                        com.hexhaki.piece.phoenix.PhoenixFire.touchdown(sink, -e[0], by - 1.0 - e[1], bz - 1.2 - e[2], (t - w - rush) * 0.7, 6);
                    }
                };
            }
            default -> {
                return null;
            }
        }
        return sc;
    }

    static BufferedImage render(String shot) {
        String[] p = shot.split(":");
        String state = p[0];
        double t = Double.parseDouble(p[1]);
        DragonPose pose = new DragonPose(model);
        float[] place = new float[12];
        // 3.6.9 "form" / "unform": t seconds into the change, standing on the ground as in game (client/phoenix/PhoenixFx); 3.7.0 the
        // human turned to fire, the phoenix opening out of it shaped as the human (its wings' roots at the shoulders), then its own
        boolean change = state.equals("form") || state.equals("unform");
        int phase = state.equals("form") ? PhoenixForm.FORMING : PhoenixForm.UNFORMING;
        float ticks = (float) t * 20.0F;
        float grow = 1.0F;
        Scene sc = scene(state, ticks);
        OX = 0.0;
        OY = 0.0;
        OZ = 0.0;
        if (sc != null) {
            grow = sc.bird ? 1.0F : 0.0F;
            PhoenixForm.pose(model, shape, pose, sc.mo, sc.beat, ticks / 20.0F);
            float sca = PhoenixLook.SCALE;
            double ry = Math.toRadians(sc.yaw);
            double rz = Math.toRadians(sc.bank);
            double cy = Math.cos(ry);
            double sy = Math.sin(ry);
            double cz = Math.cos(rz);
            double sz = Math.sin(rz);
            // Ry * Rz, its chest (in the air) at (bx, by + 0.9, bz) as in game
            double[] r = {cy * cz, -cy * sz, sy, sz, cz, 0, -sy * cz, sy * sz, cy};
            float anchor = shape.chest;
            double[] at = {sc.bx, sc.by + 0.9, sc.bz};
            for (int i = 0; i < 3; i++) {
                place[i * 4] = (float) (r[i * 3] * sca);
                place[i * 4 + 1] = (float) (r[i * 3 + 1] * sca);
                place[i * 4 + 2] = (float) (r[i * 3 + 2] * sca);
                place[i * 4 + 3] = (float) (at[i] - r[i * 3 + 1] * sca * anchor);
            }
            OX = sc.hx;
            OY = sc.hy;
            OZ = sc.hz;
        } else if (change) {
            PhoenixForm.Motion mo = new PhoenixForm.Motion();
            mo.perch = 1.0F;
            float shaped = PhoenixForm.shaped(phase, ticks);
            mo.form = shaped;
            float beat = phase == PhoenixForm.FORMING ? (float) (-Math.PI * 0.5 + Math.max(0.0F, ticks - PhoenixForm.HUMAN_GONE) / 16.0F * Math.PI)
                : ticks / 20.0F * PhoenixForm.beatRate(mo);
            PhoenixForm.pose(model, shape, pose, mo, beat, ticks / 20.0F);
            grow = Math.max(0.0F, PhoenixForm.grow(phase, ticks));
            float g = grow * PhoenixLook.SCALE;
            float anchor = shape.shoulder + (shape.feet - shape.shoulder) * shaped;
            float rise = 1.4F * (1.0F - shaped);
            float[] p0 = {g, 0, 0, 0, 0, g, 0, rise - anchor * g, 0, 0, g, 0};
            System.arraycopy(p0, 0, place, 0, 12);
        } else {
            PhoenixForm.preview(model, shape, pose, state, (float) t, place);
        }
        double yaw = Math.toRadians(Double.parseDouble(p[2]));
        double pitch = Math.toRadians(Double.parseDouble(p[3]));
        double dist = Double.parseDouble(p[4]);
        double[] lo = {1e9, 1e9, 1e9};
        double[] hi = {-1e9, -1e9, -1e9};
        List<float[]> skinned = new ArrayList<>();
        float[] light = PhoenixLook.LIGHT;
        for (DragonModel.Part part : model.parts) {
            float[] pos = new float[part.vertices() * 3];
            float[] nrm = new float[part.vertices() * 3];
            // 3.7.1: in the air the tail's streamers laid out straight (PhoenixForm.skin), on the ground as modelled
            boolean ground = change || state.equals("perch") || state.equals("walk") || state.equals("land")
                || state.equals("landseq") && t >= 0.6;
            if (state.equals("rest") || state.equals("soar") || state.startsWith("t")) {
                pose.skin(part, pos, nrm);
            } else {
                PhoenixForm.skin(shape, pose, skinned.size() / 3, part, pos, nrm, ground ? 1.0F : 0.0F, (float) t);
            }
            if (!state.equals("rest") && !state.startsWith("t")) {
                PhoenixForm.flame(shape, model, part, pos, nrm, (float) t);
            }
            int index = skinned.size() / 3;
            float[] heat = new float[part.vertices()];
            if (shape.fire[index] && !state.equals("rest") && !state.startsWith("t")) {
                for (int v = 0; v < part.vertices(); v++) {
                    heat[v] = Math.min(PhoenixLook.WHITEST, PhoenixLook.HEAT * PhoenixForm.heat(index, shape.reach[index][v], pos[v * 3], pos[v * 3 + 2], (float) t));
                }
            }
            float[] wp = new float[pos.length];
            float[] wn = new float[nrm.length];
            for (int v = 0; v < part.vertices(); v++) {
                for (int r = 0; r < 3; r++) {
                    wp[v * 3 + r] = place[r * 4] * pos[v * 3] + place[r * 4 + 1] * pos[v * 3 + 1] + place[r * 4 + 2] * pos[v * 3 + 2] + place[r * 4 + 3];
                    wn[v * 3 + r] = place[r * 4] * nrm[v * 3] + place[r * 4 + 1] * nrm[v * 3 + 1] + place[r * 4 + 2] * nrm[v * 3 + 2];
                }
                double l = Math.sqrt(wn[v * 3] * wn[v * 3] + wn[v * 3 + 1] * wn[v * 3 + 1] + wn[v * 3 + 2] * wn[v * 3 + 2]);
                for (int r = 0; r < 3; r++) {
                    wn[v * 3 + r] /= (float) Math.max(1.0E-6, l);
                    lo[r] = Math.min(lo[r], wp[v * 3 + r]);
                    hi[r] = Math.max(hi[r], wp[v * 3 + r]);
                }
            }
            skinned.add(wp);
            skinned.add(wn);
            skinned.add(heat);
        }
        double[] target = {(lo[0] + hi[0]) / 2, (lo[1] + hi[1]) / 2, (lo[2] + hi[2]) / 2};
        if (p.length >= 8) {
            target = new double[] {Double.parseDouble(p[5]), Double.parseDouble(p[6]), Double.parseDouble(p[7])};
        }
        double[] eye = {target[0] + dist * Math.cos(pitch) * Math.sin(yaw), target[1] + dist * Math.sin(pitch),
            target[2] + dist * Math.cos(pitch) * Math.cos(yaw)};
        Raster ra = new Raster(W * SS, H * SS, eye, target);
        ra.ground(0.0);
        if (sc != null) {
            if (sc.human) {
                ra.human(0.0, false, false, false, false);
            }
            if (sc.dummy) {
                ra.figure(sc.dx, sc.dy, sc.dz);
            }
        } else if (change && !PhoenixForm.humanHidden(phase, ticks)) {
            float[] burnt = new float[3];
            PhoenixForm.burning(phase, ticks, burnt);
            boolean fig = burnt[2] >= 0.5F;
            ra.human(PhoenixForm.armRaise(phase, ticks), fig && burnt[0] > PhoenixForm.ARMS_TAKEN, fig && burnt[1] > PhoenixForm.BODY_TAKEN,
                fig && burnt[1] > PhoenixForm.HEAD_TAKEN, fig && burnt[1] > PhoenixForm.LEGS_TAKEN);
        } else if (!change) {
            ra.figure(2.6, 0.0, 0.0);
        }
        float[] tint = new float[3];
        for (int i = 0; i < model.parts.length && grow > 0.02F; i++) {
            DragonModel.Part part = model.parts[i];
            float[] wp = skinned.get(i * 3);
            float[] wn = skinned.get(i * 3 + 1);
            float[] heat = skinned.get(i * 3 + 2);
            float[] col = new float[part.vertices() * 4];
            // 3.7.0 as the game: the fire parts at the ramp's lit end (no shadow), their heat flowing toward white; the rest by the ramp
            for (int v = 0; v < part.vertices(); v++) {
                if (shape.fire[i]) {
                    PhoenixLook.glowing(part.kind, tint);
                } else {
                    PhoenixLook.tint(part.kind, wn[v * 3], wn[v * 3 + 1], wn[v * 3 + 2], light, tint);
                }
                col[v * 4] = tint[0];
                col[v * 4 + 1] = tint[1];
                col[v * 4 + 2] = tint[2];
                col[v * 4 + 3] = heat[v];
            }
            ra.mesh(part, wp, col);
        }
        // 3.7.2: the human's temporary phoenix wings and tail (client/phoenix/PhoenixFx.wings): the model's wing and tail parts small on the
        // back, the roots at the shoulders, swept back (a dash) or spread
        if (sc != null && sc.wings >= 0 && sc.wingsSize > 0.02F) {
            DragonPose wp = new DragonPose(model);
            PhoenixForm.Motion wm = new PhoenixForm.Motion();
            wm.ram = sc.wings == 0 ? 1.0F : 0.0F;
            PhoenixForm.pose(model, shape, wp, wm, ticks / 20.0F * PhoenixForm.beatRate(wm), ticks / 20.0F);
            float ws = 0.13F * sc.wingsSize;
            double[] root = {OX, OY + 1.38, OZ + 0.22};
            for (int i = 0; i < model.parts.length; i++) {
                if (!shape.wings[i]) {
                    continue;
                }
                DragonModel.Part part = model.parts[i];
                float[] pos = new float[part.vertices() * 3];
                float[] nrm = new float[part.vertices() * 3];
                PhoenixForm.skin(shape, wp, i, part, pos, nrm, 0.0F, ticks / 20.0F);
                PhoenixForm.flame(shape, i, part, pos, nrm, ticks / 20.0F);
                float[] col = new float[part.vertices() * 4];
                PhoenixLook.glowing(part.kind, tint);
                for (int v = 0; v < part.vertices(); v++) {
                    pos[v * 3] = (float) (root[0] + pos[v * 3] * ws);
                    pos[v * 3 + 1] = (float) (root[1] + (pos[v * 3 + 1] - shape.shoulder) * ws);
                    pos[v * 3 + 2] = (float) (root[2] + pos[v * 3 + 2] * ws);
                    col[v * 4] = tint[0];
                    col[v * 4 + 1] = tint[1];
                    col[v * 4 + 2] = tint[2];
                }
                ra.mesh(part, pos, col);
            }
        }
        // the tongues of fire streaming off the body (PhoenixFire.burn), alpha blended over, far to near
        if (!state.equals("rest") && !state.startsWith("t") && !state.equals("soar")) {
            int[] an = shape.anchors;
            int count = an.length / 2;
            double[] pts = new double[count * 3];
            for (int k = 0; k < count; k++) {
                float[] wp = skinned.get(an[k * 2] * 3);
                int v = an[k * 2 + 1];
                pts[k * 3] = wp[v * 3] - eye[0];
                pts[k * 3 + 1] = wp[v * 3 + 1] - eye[1];
                pts[k * 3 + 2] = wp[v * 3 + 2] - eye[2];
            }
            double wind = state.equals("hover") ? 0.0 : state.equals("fly") || state.equals("glide") ? 0.25 : 0.45;
            List<double[]> quads = new ArrayList<>();
            com.hexhaki.piece.dragon.FireMesh.Sink sink = (xyz, argb) -> {
                double[] q = new double[17];
                for (int k = 0; k < 12; k++) {
                    q[k] = xyz[k] + eye[k % 3];
                }
                q[12] = argb[0];
                q[13] = argb[1];
                q[14] = argb[2];
                q[15] = argb[3];
                double mx = (q[0] + q[3] + q[6] + q[9]) / 4 - eye[0];
                double my = (q[1] + q[4] + q[7] + q[10]) / 4 - eye[1];
                double mz = (q[2] + q[5] + q[8] + q[11]) / 4 - eye[2];
                q[16] = mx * mx + my * my + mz * mz;
                quads.add(q);
            };
            if (grow > 0.05F) {
                com.hexhaki.piece.phoenix.PhoenixFire.burn(sink, pts, shape.anchorSize, count, t * 20.0, 0.0, 0.0, wind, Math.min(1.0, grow), 7);
                // the glow (as client/phoenix/PhoenixFx: the chest, the neck, the tail's root, along each wing)
                int[] bones = {shape.body, shape.neck, shape.tailRoot, shape.wing[0][1], shape.wing[0][2], shape.wing[0][3], shape.wing[1][1],
                    shape.wing[1][2], shape.wing[1][3]};
                double[] glow = new double[bones.length * 3];
                for (int k = 0; k < bones.length; k++) {
                    float gx = pose.global[bones[k] * 12 + 3];
                    float gy = pose.global[bones[k] * 12 + 7];
                    float gz = pose.global[bones[k] * 12 + 11];
                    for (int r = 0; r < 3; r++) {
                        glow[k * 3 + r] = place[r * 4] * gx + place[r * 4 + 1] * gy + place[r * 4 + 2] * gz + place[r * 4 + 3] - eye[r];
                    }
                }
                com.hexhaki.piece.phoenix.PhoenixFire.glow(sink, glow, bones.length, 3.2 * place[0], t * 20.0);
            }
            if (change) {
                com.hexhaki.piece.phoenix.PhoenixFire.change(sink, -eye[0], -eye[1], -eye[2], 180.0F, phase, ticks, 7);
            }
            if (sc != null && sc.fire != null) {
                sc.fire.accept(sink, eye);
            }
            quads.sort((x, y) -> Double.compare(y[16], x[16]));
            for (double[] q : quads) {
                ra.blend(q);
            }
            tongueQuads = quads.size();
        }
        BufferedImage img = ra.image(W, H);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.setColor(Color.BLACK);
        g.drawString(shot + String.format("  (%.1f x %.1f x %.1f blocks, %d fire quads)", hi[0] - lo[0], hi[1] - lo[1], hi[2] - lo[2], tongueQuads), 6, 15);
        tongueQuads = 0;
        g.dispose();
        return img;
    }

    /** A z-buffered rasteriser with a perspective camera, the texture by nearest texel times a per-vertex tint. */
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
