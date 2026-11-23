import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gear.NikaArt;
import com.hexhaki.piece.gear.NikaSkin;
import com.hexhaki.piece.gear.NikaBend;
import com.hexhaki.piece.gear.NikaHold;
import com.hexhaki.piece.gear.RubberJump;
import com.hexhaki.piece.gear.RubberWave;

/**
 * 3.2.6: the Nika fruit's drawn shapes (gear/NikaArt) for FxPreview, round the scene's middle (the ground at 0, a body standing at the
 * middle facing +z). Shots after the camera: {@code |nfist:radius[:way]} the Bajrang Gun's giant fist over a body for scale (way: "down"
 * knuckles to the ground, "ahead" toward +z, "up" over its head).
 *
 * <p>3.2.8: {@code |ngrab:yaw:age[:arm]} the Gum-Gum Giant (the player model's own boxes, placed and scaled as PlayerRenderer and
 * GearBodyDeformer do, four times the size) turned {@code yaw} degrees, its right arm out at {@code arm} degrees of pitch (-90: the
 * hold), a body grabbed {@code age} ticks ago standing 6 blocks off at the start, flown into the fist as gear/NikaHold places it.
 *
 * <p>3.2.9: {@code |nbend:t[:strength]} a body Star Gun struck {@code t} ticks ago (gear/NikaBend: held 26, let go over 17, as the
 * original's starHit sends), the blow toward +x; a faint outline of the body at rest beside it.
 *
 * <p>3.3.0: {@code |nworld:t[:radius[:end]]} Rubber World's ground {@code t} ticks after the release (gear/RubberWave), the field's
 * middle at the scene's middle: each grass block whose ground rises drawn lifted and tilted with the wave (as client/gear/RubberWorldDraw
 * draws the world's own blocks), within 22 blocks; two bodies standing in it ride the waves.
 *
 * <p>3.3.2: {@code |nhop:t[:land]} Gear 5's jump {@code t} ticks after the take-off (gear/RubberJump.hop; {@code land} 1: after the
 * landing): the ground springing up and rippling round the middle, the jumper (rising as the lifted jump does, or standing there when
 * landed) and a bystander 2.6 blocks off riding the ripple.
 *
 * <p>3.3.2: {@code |nchaos:t[:radius]} Rubber World on everyone else {@code t} ticks after the release: the ground as {@code |nworld},
 * the user standing untouched at the middle, and eight bodies run tick by tick as server/RubberChaos throws them (gear/RubberWave.toss
 * where a crest rises under one standing on the real, flat ground; the game's gravity and drag; a fixed seed for the sideways throw).
 */
public final class NikaShots {
    private NikaShots() {
    }

    static boolean shot(FireMesh.Sink sink, double[] eye, String[] move) {
        double ex = eye[0];
        double ey = eye[1];
        double ez = eye[2];
        switch (move[0]) {
            case "nfist" -> {
                double r = move.length > 1 ? Double.parseDouble(move[1]) : 1.5;
                String way = move.length > 2 ? move[2] : "ahead";
                HieShots.body(sink, -ex, -ey, -ez);
                NikaArt.tint = 0xE7B48F; // a skin tone (the game uses the player's own skin texel)
                NikaArt.light(-0.33, 0.79, -0.51);
                switch (way) {
                    case "down" -> NikaArt.fist(sink, -ex, 2.6 + r - ey, 1.0 - ez, 0, -1, 0, 0, 0, -1, r, true);
                    case "up" -> NikaArt.fist(sink, -0.3 - ex, 2.4 + r - ey, -ez, 0, 1, 0, 0, 0, -1, r, true);
                    default -> NikaArt.fist(sink, -ex, 1.4 - ey, 1.2 + r - ez, 0, 0, 1, 0, 1, 0, r, true);
                }
                NikaArt.tint = 0xFFFFFF;
            }
            case "npow" -> {
                // 3.3.9: Dawn Pistol landing: |npow:age[:size] (the struck body 5 ahead, its POW, the thrower at the middle)
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 2;
                double size = move.length > 2 ? Double.parseDouble(move[2]) : 1.9;
                double back = 0.9 * Math.max(0, age);
                HieShots.body(sink, -ex, -ey, -ez);
                HieShots.body(sink, -ex, -ey, 5.0 + back - ez);
                NikaArt.pow(sink, -ex, 0.95 - ey, 5.0 + back - ez, 0, 0, 1, age, size, 7);
            }
            case "nskin" -> {
                // 3.4.0: Dawn Pistol through the body, |nskin:t (t ticks after the hit; the body 4.5 ahead, the blow along +z): the
                // body's skin stretched out of its far side to the fist (here the shirt's red where the game uses the body's own texture),
                // the heat over it, the POW where the fist went in; launched at NikaSkin.PIN
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 3;
                double flown = 0;
                double v = 3.6;
                for (int k = NikaSkin.PIN; k < (int) t; k++) {
                    flown += v;
                }
                if (t > NikaSkin.PIN) {
                    flown += v * Math.min(1, t - Math.max(NikaSkin.PIN, (int) t));
                }
                double bz = 4.5 + flown;
                HieShots.body(sink, -ex, -ey, -ez);
                HieShots.body(sink, -ex, -ey, bz - ez);
                double len = NikaSkin.LENGTH * NikaSkin.out(t);
                double glow = Math.min(1.0, NikaSkin.out(t) * 1.2);
                com.hexhaki.piece.horo.AstralArt.Skin flat = (xyz, uv, argb, nx, ny, nz) -> {
                    double lit = nx * -0.33 + ny * 0.79 + nz * -0.51;
                    double band = lit > 0.5 ? 1.0 : lit > 0.1 ? 0.86 : lit > -0.3 ? 0.72 : 0.6;
                    int c = FireMesh.argb(1.0, FireMesh.mix(0, 0xB03A2E, band));
                    sink.quad(xyz, new int[] {c, c, c, c});
                };
                NikaSkin.skin(flat, -ex, 1.05 - ey, bz - ez, 0, 0, 1, len, 0.42, NikaSkin.BULB, 0, 1, 0, 1, 0xFFFFFFFF, t);
                NikaSkin.heat(sink, -ex, 1.05 - ey, bz - ez, 0, 0, 1, len, 0.42, NikaSkin.BULB, glow, t);
                NikaArt.pow(sink, -ex, 0.95 - ey, 4.5 - ez, 0, 0, 1, t, 1.9, 7);
            }
            case "ndawn" -> {
                // 3.4.0: Dawn Pistol over time, |ndawn:t (t ticks after the arm leaves; this scene's arm lands on tick 4): the Haki-black
                // arm and ballooned fist reaching into the body, the POW, its skin stretched out through it to the white-hot fist (the
                // shirt's red here; the body's own texture in game), held while pinned, then snapping back as it is launched (3.6 a tick)
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 5;
                double land = 4.0;
                double tl = t - land;
                double bz0 = 4.6;
                // 3.4.1: fast out; then through the body to the fist at the skin's end while it stretches; gone as the body is launched
                double reach = t < land ? 0.6 + (bz0 - 0.6) * (1 - Math.pow(1 - t / land, 3))
                    : tl < NikaSkin.PIN ? bz0 + Math.max(0, NikaSkin.LENGTH * NikaSkin.out(tl) - NikaSkin.BULB) : 0;
                double flown = tl > NikaSkin.PIN ? 5.0 * (tl - NikaSkin.PIN) : 0;
                double bz = bz0 + flown;
                HieShots.body(sink, -ex, -ey, -ez);
                HieShots.body(sink, -ex, -ey, bz - ez);
                int skin = 0xE7B48F;
                int coat = FireMesh.mix(0, skin, 0.12);
                double sx = -0.37;
                double sy = 1.3;
                if (reach > 0.3) {
                    // through the body: the loop laid out to the body, the arm straight on to the fist (client/gear/NikaPistolArm)
                    double to = Math.min(reach, bz0);
                    curvedArm(sink, ex, ey, ez, sx, sy, 0, 0, 1.15, to, coat, reach > bz0 + 0.05 ? -1 : mulRgb(skin, 0x3A3248));
                    if (reach > bz0 + 0.05) {
                        FireMesh.tube(sink, -ex, 1.15 - ey, to - ez, -ex, 1.15 - ey, reach - ez, 0.13, 0.13, coat, coat, coat, 0.0, 1.0, 0.0, 0.0, 1);
                        NikaArt.tint = mulRgb(skin, 0x3A3248);
                        NikaArt.light(-0.33, 0.79, -0.51);
                        double r = 0.375;
                        NikaArt.fist(sink, -ex, 1.15 - ey, reach + r - ez, 0, 0, 1, 0, 1, 0, r, true, true);
                        NikaArt.tint = 0xFFFFFF;
                    }
                }
                if (tl >= 0) {
                    double len = NikaSkin.LENGTH * NikaSkin.out(tl);
                    com.hexhaki.piece.horo.AstralArt.Skin flat = (xyz, uv, argb, nx, ny, nz) -> {
                        double lit = nx * -0.33 + ny * 0.79 + nz * -0.51;
                        double band = lit > 0.5 ? 1.0 : lit > 0.1 ? 0.86 : lit > -0.3 ? 0.72 : 0.6;
                        int c = FireMesh.argb(1.0, FireMesh.mix(0, 0xB03A2E, band));
                        sink.quad(xyz, new int[] {c, c, c, c});
                    };
                    NikaSkin.skin(flat, -ex, 1.05 - ey, bz - ez, 0, 0, 1, len, 0.42, NikaSkin.BULB, 0, 1, 0, 1, 0xFFFFFFFF, tl);
                    NikaSkin.heat(sink, -ex, 1.05 - ey, bz - ez, 0, 0, 1, len, 0.42, NikaSkin.BULB, Math.min(1.0, NikaSkin.out(tl) * 1.2), tl);
                    NikaArt.pow(sink, -ex, 0.95 - ey, bz0 - ez, 0, 0, 1, tl, 1.9, 7);
                }
            }
            case "ndland" -> {
                // 3.4.1: where a Dawn Pistol's body comes down: |ndland:age (the crater's dust explosion: the Gura fruit's quake, 3.2)
                double age = move.length > 1 ? Double.parseDouble(move[1]) : 3;
                HieShots.body(sink, 0.8 - ex, -ey, -ez);
                com.hexhaki.piece.gura.GuraArt.quake(sink, -ex, -ey, -ez, 3.2, 1.0, age, 7);
            }
            case "npistol" -> {
                // 3.3.9: the Dawn Pistol's arm at its reach: |npistol:reach[:pow age] (the arm Haki-black as the original draws it, the
                // ballooned fist round its end as client/gear/NikaPistolArm draws it, the struck body and the POW when it has landed)
                double reach = move.length > 1 ? Double.parseDouble(move[1]) : 4.5;
                double age = move.length > 2 ? Double.parseDouble(move[2]) : -1;
                HieShots.body(sink, -ex, -ey, -ez);
                int skin = 0xE7B48F;
                int coat = FireMesh.mix(0, skin, 0.12); // the original's dawn arm: the skin times 0.11, 0.11, 0.14
                double sx = -0.37;
                double sy = 1.3;
                double tx = 0.0;
                double ty = 1.15;
                double tz = reach;
                FireMesh.tube(sink, sx - ex, sy - ey, -ez, tx - ex, ty - ey, tz - ez, 0.13, 0.13, coat, coat, coat, 0.0, 1.0, 0.0, 0.0, 1);
                double grow = 0.4 + 0.6 * Math.min(1.0, reach / 2.5);
                double r = 0.125 * (move.length > 3 ? Double.parseDouble(move[3]) : 3.0) * grow;
                double dx = tx - sx;
                double dy = ty - sy;
                double dz = tz;
                double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
                NikaArt.tint = mulRgb(skin, 0x3A3248);
                NikaArt.light(-0.33, 0.79, -0.51);
                // the thrower's right arm (x -0.37: facing +z, the right is -x): a right hand, its thumb on the inside
                NikaArt.fist(sink, tx + dx / dl * r - ex, ty + dy / dl * r - ey, tz + dz / dl * r - ez, dx, dy, dz, 0, 1, 0, r, true, true);
                NikaArt.tint = 0xFFFFFF;
                if (age >= 0) {
                    HieShots.body(sink, -ex, -ey, tz + 2 * r + 0.3 - ez);
                    NikaArt.pow(sink, -ex, 0.95 - ey, tz + 2 * r + 0.3 - ez, 0, 0, 1, age, 1.9, 7);
                }
            }
            case "ngrab" -> {
                double yaw = move.length > 1 ? Double.parseDouble(move[1]) : 0;
                double age = move.length > 2 ? Double.parseDouble(move[2]) : 20;
                double arm = move.length > 3 ? Double.parseDouble(move[3]) : -90;
                double scale = 4.0;
                giant(sink, -ex, -ey, -ez, yaw, scale, arm);
                double[] fist = new double[3];
                NikaHold.fist(0, 0, 0, yaw, scale, scale, fist);
                double k = NikaHold.reach(age);
                double fromX = 2.5;
                double fromZ = 6.0;
                double bx = fromX + (fist[0] - fromX) * k;
                double by = (fist[1] - 0.95) * k;
                double bz = fromZ + (fist[2] - fromZ) * k;
                HieShots.body(sink, bx - ex, by - ey, bz - ez);
            }
            case "nbend" -> {
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 0;
                double strength = move.length > 2 ? Double.parseDouble(move[2]) : 1;
                double[] shape = new double[5];
                NikaBend.shape(t, 26, 17, strength, shape);
                bent(sink, -ex, -ey, -ez, Math.PI / 2, shape);
                double[] rest = {1, 1, 1, 0, 0};
                bent(sink, -ex, -ey, -ez - 2.2, Math.PI / 2, rest);
            }
            case "nworld" -> {
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 20;
                double radius = move.length > 2 ? Double.parseDouble(move[2]) : 40;
                double end = move.length > 3 ? Double.parseDouble(move[3]) : Double.POSITIVE_INFINITY;
                for (int x = -22; x < 22; x++) {
                    for (int z = -22; z < 22; z++) {
                        double cx = x + 0.5;
                        double cz = z + 0.5;
                        double r = Math.sqrt(cx * cx + cz * cz);
                        if (r > 22) {
                            continue;
                        }
                        double angle = Math.atan2(cz, cx);
                        double h = RubberWave.rise(r, angle, t, radius, end);
                        if (h < 0.03) {
                            continue;
                        }
                        double tilt = RubberWave.tilt(RubberWave.slope(r, angle, t, radius, end));
                        column(sink, x - ex, h - ey, z - ez, cx / Math.max(r, 1e-6), cz / Math.max(r, 1e-6), tilt, h);
                    }
                }
                for (double[] b : new double[][] {{5.5, 3.5}, {-3.5, 9.5}}) {
                    double r = Math.hypot(b[0], b[1]);
                    double h = RubberWave.rise(r, Math.atan2(b[1], b[0]), t, radius, end);
                    HieShots.body(sink, b[0] - ex, h - ey, b[1] - ez);
                }
            }
            case "nhop" -> {
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 1;
                boolean landing = move.length > 2 && move[2].equals("1");
                for (int x = -7; x < 7; x++) {
                    for (int z = -7; z < 7; z++) {
                        double cx = x + 0.5;
                        double cz = z + 0.5;
                        double r = Math.sqrt(cx * cx + cz * cz);
                        double angle = Math.atan2(cz, cx);
                        double h = RubberJump.hop(r, angle, t, landing);
                        if (h < 0.03) {
                            continue;
                        }
                        double tilt = RubberWave.tilt(RubberJump.slope(r, angle, t, landing));
                        column(sink, x - ex, h - ey, z - ez, cx / Math.max(r, 1e-6), cz / Math.max(r, 1e-6), tilt, h);
                    }
                }
                // the jumper: up as the lifted jump goes (0.9 a tick, the game's gravity and drag), or standing after the landing
                double y = 0;
                if (!landing) {
                    double v = RubberJump.lifted(0.42);
                    for (int k = 0; k < (int) t; k++) {
                        y += v;
                        v = (v - 0.08) * 0.98;
                    }
                    y += v * (t - (int) t);
                }
                HieShots.body(sink, -ex, y - ey, -ez);
                double bx = 2.6;
                double bz = 0.9;
                double bh = RubberJump.hop(Math.hypot(bx, bz), Math.atan2(bz, bx), t, landing);
                HieShots.body(sink, bx - ex, bh - ey, bz - ez);
            }
            case "nchaos" -> {
                double t = move.length > 1 ? Double.parseDouble(move[1]) : 30;
                double radius = move.length > 2 ? Double.parseDouble(move[2]) : 16;
                double inf = Double.POSITIVE_INFINITY;
                for (int x = -18; x < 18; x++) {
                    for (int z = -18; z < 18; z++) {
                        double cx = x + 0.5;
                        double cz = z + 0.5;
                        double r = Math.sqrt(cx * cx + cz * cz);
                        double angle = Math.atan2(cz, cx);
                        double h = RubberWave.rise(r, angle, t, radius, inf);
                        if (h < 0.03) {
                            continue;
                        }
                        double tilt = RubberWave.tilt(RubberWave.slope(r, angle, t, radius, inf));
                        column(sink, x - ex, h - ey, z - ez, cx / Math.max(r, 1e-6), cz / Math.max(r, 1e-6), tilt, h);
                    }
                }
                HieShots.body(sink, -ex, -ey, -ez);
                java.util.Random random = new java.util.Random(7);
                for (int b = 0; b < 8; b++) {
                    double a0 = b * Math.PI / 4 + 0.3;
                    double r0 = 3.2 + 1.3 * (b % 3);
                    double[] q = {Math.cos(a0) * r0, 0, Math.sin(a0) * r0};
                    double[] v = {0, 0, 0};
                    long next = 0;
                    for (int k = 1; k <= (int) t; k++) {
                        boolean ground = q[1] <= 0;
                        double r = Math.hypot(q[0], q[2]);
                        double angle = Math.atan2(q[2], q[0]);
                        double s = r <= radius ? RubberWave.toss(RubberWave.rise(r, angle, k, radius, inf) - RubberWave.rise(r, angle, k - 1, radius, inf)) : 0;
                        if (ground && s > 0 && k >= next) {
                            next = k + RubberWave.TOSS_GAP;
                            double ox = q[0] / Math.max(r, 1e-6);
                            double oz = q[2] / Math.max(r, 1e-6);
                            double side = RubberWave.tossSide(s) * (random.nextDouble() < 0.5 ? -1 : 1) * (0.5 + random.nextDouble());
                            double out = RubberWave.tossOut(s) * (random.nextDouble() < RubberWave.BACK_CHANCE ? -RubberWave.BACK : 1);
                            v[0] = v[0] * 0.3 + ox * out - oz * side;
                            v[1] = RubberWave.tossUp(s);
                            v[2] = v[2] * 0.3 + oz * out + ox * side;
                        }
                        q[0] += v[0];
                        q[1] += v[1];
                        q[2] += v[2];
                        if (q[1] <= 0) {
                            q[1] = 0;
                            v[0] *= 0.55;
                            v[2] *= 0.55;
                            v[1] = 0;
                        } else {
                            v[1] = (v[1] - 0.08) * 0.98;
                            v[0] *= 0.91;
                            v[2] *= 0.91;
                        }
                    }
                    double r = Math.hypot(q[0], q[2]);
                    double lift = q[1] <= 0 ? RubberWave.rise(r, Math.atan2(q[2], q[0]), t, radius, inf) : 0;
                    HieShots.body(sink, q[0] - ex, q[1] + lift - ey, q[2] - ez);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * The player model's boxes (wide arms) as PlayerRenderer draws them at the feet x, y, z turned {@code yaw} (0 faces +z), scaled
     * {@code scale} times round the feet, the right arm pitched {@code arm} degrees (the rest standing).
     */
    static void giant(FireMesh.Sink sink, double x, double y, double z, double yaw, double scale, double arm) {
        // pivot (pixels), box from, box size, pitch, colour
        double[][] parts = {
            {0, 0, 0, -4, -8, -4, 8, 8, 8, 0, 0xD9A77E},
            {0, 0, 0, -4, 0, -2, 8, 12, 4, 0, 0xF2F0E6},
            {-5, 2, 0, -3, -2, -2, 4, 12, 4, arm, 0xD9A77E},
            {5, 2, 0, -1, -2, -2, 4, 12, 4, 0, 0xD9A77E},
            {-1.9, 12, 0, -2, 0, -2, 4, 12, 4, 0, 0x3A3A44},
            {1.9, 12, 0, -2, 0, -2, 4, 12, 4, 0, 0x3A3A44},
        };
        double r = Math.toRadians(180 - yaw);
        double cr = Math.cos(r);
        double sr = Math.sin(r);
        for (double[] p : parts) {
            double pitch = Math.toRadians(p[9]);
            double cp = Math.cos(pitch);
            double sp = Math.sin(pitch);
            double[][] c = new double[8][3];
            for (int i = 0; i < 8; i++) {
                double vx = p[3] + ((i & 1) != 0 ? p[6] : 0);
                double vy = p[4] + ((i & 2) != 0 ? p[7] : 0);
                double vz = p[5] + ((i & 4) != 0 ? p[8] : 0);
                // ModelPart: about X by its pitch, then to its pivot (pixels)
                double ry = vy * cp - vz * sp;
                double rz = vy * sp + vz * cp;
                double mx = (p[0] + vx) / 16.0;
                double my = (p[1] + ry) / 16.0 - 1.501;
                double mz = (p[2] + rz) / 16.0;
                // PlayerRenderer: 0.9375, then scale(-1, -1, 1), then about Y by 180 - yaw; the giant round the feet
                double fx = -0.9375 * mx;
                double fy = -0.9375 * my;
                double fz = 0.9375 * mz;
                c[i][0] = x + scale * (fx * cr + fz * sr);
                c[i][1] = y + scale * fy;
                c[i][2] = z + scale * (-fx * sr + fz * cr);
            }
            int[][] order = {{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};
            double[] shade = {0.62, 0.62, 0.95, 0.45, 0.78, 0.78};
            for (int k = 0; k < 6; k++) {
                int col = FireMesh.argb(1.0, FireMesh.mix(0, (int) p[10], shade[k]));
                double[] q = new double[12];
                for (int i = 0; i < 4; i++) {
                    System.arraycopy(c[order[k][i]], 0, q, i * 3, 3);
                }
                sink.quad(q, new int[] {col, col, col, col});
            }
        }
    }

    /** HieShots.body's boxes changed as client/gear/NikaBendClient changes a drawing: in the blow's frame (yaw) scaled, leaned, pushed. */
    static void bent(FireMesh.Sink sink, double x, double y, double z, double yaw, double[] shape) {
        double[][] boxes = {
            {-0.3, 0, -0.15, 0.3, 0.75, 0.15, 0x3A4A7A},
            {-0.3, 0.75, -0.15, 0.3, 1.4, 0.15, 0xB03A2E},
            {-0.25, 1.4, -0.25, 0.25, 1.9, 0.25, 0xD9A77E},
        };
        double c = Math.cos(yaw);
        double sn = Math.sin(yaw);
        int[][] order = {{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};
        double[] shade = {0.62, 0.62, 0.95, 0.45, 0.78, 0.78};
        for (double[] b : boxes) {
            double[][] corner = new double[8][3];
            for (int i = 0; i < 8; i++) {
                double vx = (i & 1) != 0 ? b[3] : b[0];
                double vy = (i & 2) != 0 ? b[4] : b[1];
                double vz = (i & 4) != 0 ? b[5] : b[2];
                // into the blow's frame: about Y by -yaw (JOML: x' = x cos + z sin, z' = -x sin + z cos)
                double lx = vx * Math.cos(-yaw) + vz * Math.sin(-yaw);
                double lz = -vx * Math.sin(-yaw) + vz * Math.cos(-yaw);
                lx *= shape[NikaBend.ACROSS];
                double ly = vy * shape[NikaBend.UP];
                lz *= shape[NikaBend.ALONG];
                lz += shape[NikaBend.SHEAR] * ly + shape[NikaBend.BACK];
                corner[i][0] = x + lx * c + lz * sn;
                corner[i][1] = y + ly;
                corner[i][2] = z - lx * sn + lz * c;
            }
            for (int k = 0; k < 6; k++) {
                int col = FireMesh.argb(1.0, FireMesh.mix(0, (int) b[6], shade[k]));
                double[] q = new double[12];
                for (int i = 0; i < 4; i++) {
                    System.arraycopy(corner[order[k][i]], 0, q, i * 3, 3);
                }
                sink.quad(q, new int[] {col, col, col, col});
            }
        }
    }

    /**
     * One grass block (its top at y), from x, z, turned about the flat axis across the way out (rx, rz) by {@code tilt} radians round
     * its top's middle (a positive tilt faces its top inward, as the client turns it).
     */
    /**
     * 3.4.1: Dawn Pistol's arm as client/gear/NikaPistolArm draws it: gear/NikaArc's looping path (up and out to the right: the thrower's
     * right is -x facing +z) as a quadratic B-spline, each piece 4 tube lengths, and the ballooned right fist on its end facing along the
     * path's last stretch.
     */
    static void curvedArm(FireMesh.Sink sink, double ex, double ey, double ez, double sx, double sy, double sz, double tx, double ty, double tz,
                          int coat, int fistTint) {
        double[] h = new double[3];
        double[] k = new double[com.hexhaki.piece.gear.NikaArc.KNOTS * 3];
        if (!com.hexhaki.piece.gear.NikaArc.plane(tx - sx, ty - sy, tz - sz, 0, 1, 0, -1, 0, 0, h)) {
            return;
        }
        com.hexhaki.piece.gear.NikaArc.path(sx, sy, sz, tx, ty, tz, h[0], h[1], h[2], k);
        int n = com.hexhaki.piece.gear.NikaArc.KNOTS;
        double px = sx;
        double py = sy;
        double pz = sz;
        for (int p = 0; p <= n - 2; p++) {
            double[] a0 = p == 0 ? new double[] {k[0], k[1], k[2]} : mid(k, p, p + 1);
            double[] c0 = {k[(p + 1) * 3], k[(p + 1) * 3 + 1], k[(p + 1) * 3 + 2]};
            double[] b0 = p == n - 2 ? new double[] {k[(n - 1) * 3], k[(n - 1) * 3 + 1], k[(n - 1) * 3 + 2]} : mid(k, p + 1, p + 2);
            for (int i = 1; i <= 4; i++) {
                double u = i / 4.0;
                double qa = (1 - u) * (1 - u);
                double qb = 2 * u * (1 - u);
                double qc = u * u;
                double qx = qa * a0[0] + qb * c0[0] + qc * b0[0];
                double qy = qa * a0[1] + qb * c0[1] + qc * b0[1];
                double qz = qa * a0[2] + qb * c0[2] + qc * b0[2];
                FireMesh.tube(sink, px - ex, py - ey, pz - ez, qx - ex, qy - ey, qz - ez, 0.13, 0.13, coat, coat, coat, 0.0, 1.0, 0.0, 0.0, 1);
                px = qx;
                py = qy;
                pz = qz;
            }
        }
        if (fistTint < 0) {
            return; // the fist is further on (through the body)
        }
        double len = Math.sqrt((tx - sx) * (tx - sx) + (ty - sy) * (ty - sy) + (tz - sz) * (tz - sz));
        double r = 0.125 * 3.0 * (0.4 + 0.6 * Math.min(1.0, len / 2.5));
        double fx = tx - k[(n - 2) * 3];
        double fy = ty - k[(n - 2) * 3 + 1];
        double fz = tz - k[(n - 2) * 3 + 2];
        double fl = Math.max(1.0E-6, Math.sqrt(fx * fx + fy * fy + fz * fz));
        NikaArt.tint = fistTint;
        NikaArt.light(-0.33, 0.79, -0.51);
        NikaArt.fist(sink, tx + fx / fl * r - ex, ty + fy / fl * r - ey, tz + fz / fl * r - ez, fx, fy, fz, 0, 1, 0, r, true, true);
        NikaArt.tint = 0xFFFFFF;
    }

    static double[] mid(double[] k, int i, int j) {
        return new double[] {(k[i * 3] + k[j * 3]) * 0.5, (k[i * 3 + 1] + k[j * 3 + 1]) * 0.5, (k[i * 3 + 2] + k[j * 3 + 2]) * 0.5};
    }

    /** Two colours multiplied (a texel times a vertex colour, as the game draws it). */
    static int mulRgb(int a, int b) {
        int r = ((a >> 16) & 255) * ((b >> 16) & 255) / 255;
        int g = ((a >> 8) & 255) * ((b >> 8) & 255) / 255;
        int bl = (a & 255) * (b & 255) / 255;
        return (r << 16) | (g << 8) | bl;
    }

    static void grass(FireMesh.Sink sink, double x, double y, double z, double rx, double rz, double tilt) {
        box(sink, x, y, z, rx, rz, tilt, 0, -1, true);
    }

    /**
     * 3.3.8: a risen column as client/gear/RubberWorldDraw draws it: the grass block lifted {@code h} (its top at {@code y}) and, past 0.6,
     * the block under it stretched down to the real ground (gear/RubberWave.underTall).
     */
    static void column(FireMesh.Sink sink, double x, double y, double z, double rx, double rz, double tilt, double h) {
        box(sink, x, y, z, rx, rz, tilt, 0, -1, true);
        if (h > 0.6) {
            box(sink, x, y, z, rx, rz, tilt, -1, -1 - RubberWave.underTall(h), false);
        }
    }

    /** A block from {@code top} to {@code bottom} below the lifted top middle (x + 0.5, y, z + 0.5), tilted about the flat axis across. */
    static void box(FireMesh.Sink sink, double x, double y, double z, double rx, double rz, double tilt, double top, double bottom, boolean grassy) {
        double c = Math.cos(tilt);
        double sn = Math.sin(tilt);
        double[][] corner = new double[8][3];
        for (int i = 0; i < 8; i++) {
            double vx = ((i & 1) != 0 ? 1 : 0) - 0.5;
            double vy = ((i & 2) != 0 ? top : bottom);
            double vz = ((i & 4) != 0 ? 1 : 0) - 0.5;
            // about the tangent (-rz, 0, rx): the radial part and the height turn, the tangent part stays
            double radial = vx * rx + vz * rz;
            double tangent = -vx * rz + vz * rx;
            double radial2 = radial * c - vy * sn;
            double up2 = radial * sn + vy * c;
            corner[i][0] = x + 0.5 + radial2 * rx - tangent * rz;
            corner[i][1] = y + up2;
            corner[i][2] = z + 0.5 + radial2 * rz + tangent * rx;
        }
        int[][] order = {{0, 1, 3, 2}, {4, 6, 7, 5}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 5, 7, 3}};
        // faces: the two across z, the bottom, the top (grass), the two across x (dirt)
        int[] colour = {0x7A5634, 0x7A5634, 0x6B4A2E, grassy ? 0x6FB043 : 0x7A5634, 0x7A5634, 0x7A5634};
        double[] shade = {0.8, 0.8, 0.5, 1.0, 0.62, 0.62};
        for (int k = 0; k < 6; k++) {
            int col = FireMesh.argb(1.0, FireMesh.mix(0, colour[k], shade[k]));
            double[] q = new double[12];
            for (int i = 0; i < 4; i++) {
                System.arraycopy(corner[order[k][i]], 0, q, i * 3, 3);
            }
            sink.quad(q, new int[] {col, col, col, col});
        }
    }
}
