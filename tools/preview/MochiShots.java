import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.mochi.MochiArt;

/**
 * 3.5.5: the Mochi Mochi no Mi's previews (mochi/MochiArt, the very quads client/mochi/MochiFx draws), in FxPreview:
 * {@code |mparts:coat} the pieces side by side (fists, a ball, a drill, a ring, a box).
 */
public final class MochiShots {
    private MochiShots() {
    }

    /** Targets (5 a punch: ring, x, y, z, age) made eye-relative. */
    static double[] toEye(double[] p, double ex, double ey, double ez) {
        double[] out = p.clone();
        for (int k = 0; k < out.length / 5; k++) {
            out[k * 5 + 1] -= ex;
            out[k * 5 + 2] -= ey;
            out[k * 5 + 3] -= ez;
        }
        return out;
    }

    /** The user, posed in {@code clip} {@code tick} ticks in, standing at the origin facing +z (ItoShots.posed: the game's own skin). */
    static void user(FireMesh.Sink sink, String clip, double tick, double ex, double ey, double ez) {
        ItoShots.posed(sink, clip, (float) Math.max(0.0, tick), 0.0, 0.0, 0.0, 0.0, 0.9, 0.0, ex, ey, ez, 0.0, 0.0);
    }

    /** 3.6.3: the user with the limbs in {@code hide} turned into mochi (not drawn: AstralArt.hide, as MochiFx.mochiArms in the game). */
    static void user(FireMesh.Sink sink, String clip, double tick, double ex, double ey, double ez, int hide) {
        com.hexhaki.piece.horo.AstralArt.hide = hide;
        user(sink, clip, tick, ex, ey, ez);
        com.hexhaki.piece.horo.AstralArt.hide = 0;
    }

    /** 3.6.3: the Lotus combo's body height over the ground at {@code age} (as server/MochiMoves.Juggled moves it). */
    static double lotusBody(double age) {
        return com.hexhaki.piece.mochi.MochiDraw.lotusHeight(age);
    }

    static boolean shot(FireMesh.Sink sink, double[] eye, String[] move) {
        double ex = eye[0];
        double ey = eye[1];
        double ez = eye[2];
        double age = move.length > 1 ? Double.parseDouble(move[1]) : 4;
        boolean coat = move.length > 2 && move[2].equals("c");
        switch (move[0]) {
            case "mparts" -> {
                MochiArt.fist(sink, -3 - ex, 1.5 - ey, -ez, 0, 0, 1, 1, 0, 0, 0.8, false, age, 1);
                MochiArt.fist(sink, -1 - ex, 1.5 - ey, -ez, 0, 0, 1, 1, 0, 0, 0.8, true, age, 2);
                MochiArt.ball(sink, 1.2 - ex, 1.5 - ey, -ez, 0.8, 0, 1, 0, 1.0, 0.12, age, 3, MochiArt.MOCHI);
                MochiArt.tubeBegin();
                for (int k = 0; k <= 10; k++) {
                    double t = k / 10.0;
                    MochiArt.tubePoint(3.2 - ex, 0.6 + 2.0 * t - ey, -ez, 0.5 * (1.0 - t) + 0.02);
                }
                MochiArt.tube(sink, 12, MochiArt.MOCHI, 2.5, age * 0.3);
                MochiArt.torus(sink, -2 - ex, 3.6 - ey, -ez, 0, 0, 1, 0.8, 0.32, 1.0, MochiArt.MOCHI);
                MochiArt.blob(sink, 1.0 - ex, 3.6 - ey, -ez, 1, 0, 0, 0, 1, 0, 0.8, 0.6, 0.5, 0.35, 0.0, age, 5, coat ? MochiArt.HAKI : MochiArt.MOCHI, 1.0);
            }
            case "mdonut" -> {
                // 3.6.3: the fruit's donut: lumpy, dripping, its fist filling its hole (one punching), at three sizes
                com.hexhaki.piece.mochi.MochiDraw.donut(sink, -2.6 - ex, 2.2 - ey, -ez, 0, 0, -1, 1.0, 0.0, 3, 0.0, 0, 0, 0, coat, age, 1, false);
                com.hexhaki.piece.mochi.MochiDraw.donut(sink, 0.2 - ex, 2.4 - ey, -ez, 0.3, 0.1, -0.95, 1.4, 0.0, 3, 1.0, 2.5 - ex, 2.0 - ey,
                    -6.5 - ez, coat, age, 2, false);
                com.hexhaki.piece.mochi.MochiDraw.donut(sink, 2.8 - ex, 1.6 - ey, -ez, -0.2, 0, -0.98, 0.7, 0.0, 2, 0.0, 0, 0, 0, coat, age, 3, true);
            }
            case "mmuddle" -> {
                // 3.6.3 |mmuddle:age: Mochi Muddle's trail poured along +z from 1.2 ahead
                user(sink, "mochi_nagare", age, ex, ey, ez);
                double reach = Math.min(com.hexhaki.piece.server.MochiMoves.MUDDLE_LENGTH, com.hexhaki.piece.server.MochiMoves.MUDDLE_SPREAD * (age + 1));
                double fade = Math.min(1.0, (com.hexhaki.piece.server.MochiMoves.MUDDLE_TICKS - age) / 12.0);
                HieShots.body(sink, 0.4 - ex, -ey, 7.0 - ez);
                com.hexhaki.piece.mochi.MochiDraw.muddle(sink, -ex, -ey, 1.2 - ez, 0, 1, com.hexhaki.piece.server.MochiMoves.MUDDLE_LENGTH, reach,
                    com.hexhaki.piece.server.MochiMoves.MUDDLE_WIDTH, fade, age, 3);
                if (age > 4) {
                    com.hexhaki.piece.mochi.MochiDraw.stuck(sink, 0.4 - ex, -ey, 7.0 - ez, 1.8, Math.min(1.0, (age - 4) / 3.0), age, 4);
                }
            }
            case "mlotus" -> {
                // 3.6.3 |mlotus:age[:c]: the whole combo as the reference times it (MochiDraw.LOTUS_*): the user at the origin facing +z,
                // the one hit 7 ahead, thrown up at LOTUS_LAUNCH, struck in the air, driven down by the giant fist into the ground
                user(sink, "mochi_lotus", Math.min(age, 46), ex, ey, ez);
                double[] d = com.hexhaki.piece.mochi.MochiDraw.lotusAt(0, 1, new double[3]);
                double by = lotusBody(age);
                HieShots.body(sink, -ex, by - ey, 7.0 - ez);
                com.hexhaki.piece.mochi.MochiDraw.lotusRise(sink, -ex, by - ey, 7.0 - ez, -ey, age - com.hexhaki.piece.mochi.MochiDraw.LOTUS_LAUNCH, 41);
                com.hexhaki.piece.mochi.MochiDraw.lotus(sink, 0.4 - ex, 1.3 - ey, 0.3 - ez, d[0] - ex, d[1] - ey, d[2] - ez, 0, 1, -ex, 1.0 - ey,
                    7.0 - ez, -ey, age, coat, 5);
                int[] air = com.hexhaki.piece.mochi.MochiDraw.LOTUS_AIR;
                for (int k = 0; k < air.length; k++) {
                    double side = k % 2 == 0 ? 1.0 : -1.0;
                    com.hexhaki.piece.mochi.MochiDraw.lotusAir(sink, -ex, lotusBody(air[k]) + 1.0 - ey, 7.0 - ez, side * 0.8, 0.6 * (k == 2 ? -1 : 1), 0.0,
                        age - air[k], coat, 11 + k);
                }
                double top = com.hexhaki.piece.mochi.MochiDraw.LOTUS_HIGH + 7.0;
                com.hexhaki.piece.mochi.MochiDraw.lotusSlam(sink, -ex, top - ey, 7.0 - ez, -ex, by - ey, 7.0 - ez, -ey,
                    age - com.hexhaki.piece.mochi.MochiDraw.LOTUS_DROP, coat, 21);
                com.hexhaki.piece.mochi.MochiDraw.lotusGround(sink, -ex, -ey, 7.0 - ez, age - com.hexhaki.piece.mochi.MochiDraw.LOTUS_GROUND, 31);
            }
            case "mchestnut" -> {
                // |mchestnut:age: the spiked donut over the shoulder (yaw 0: facing +z), a spike at a body 8 ahead every 4 ticks
                user(sink, "mochi_amadare", Math.min(age, 10), ex, ey, ez);
                double[] at = com.hexhaki.piece.mochi.MochiDraw.chestnutOffset(0.0, 1.8, new double[3]);
                double form = Math.min(1.0, age / 6.0);
                HieShots.body(sink, 1.0 - ex, -ey, 8.0 - ez);
                com.hexhaki.piece.mochi.MochiDraw.chestnut(sink, at[0] - ex, at[1] - ey, at[2] - ez, 0, 1, form, age * 0.08, age % 4, age, 7);
                if (age > 8) {
                    com.hexhaki.piece.mochi.MochiDraw.spike(sink, at[0] - ex, at[1] - ey, at[2] - ez, 1.0 - ex, 1.0 - ey, 8.0 - ez, age % 4);
                }
            }
            case "marea" -> {
                // |marea:age[:c]: the Anemone over a place 8 ahead (radius 3.4), fists striking into it
                user(sink, "mochi_donuts", Math.min(age, 10), ex, ey, ez);
                HieShots.body(sink, 0.6 - ex, -ey, 8.3 - ez);
                double[] strikes = new double[8 * 5];
                int n = 0;
                for (int k = 0; k < 8; k++) {
                    double pa = (age - 5 - k * 1.5 + 100) % 7.0;
                    if (age < 5) {
                        break;
                    }
                    strikes[n * 5] = k;
                    strikes[n * 5 + 1] = 0.6 + Math.cos(k) * 1.2 - ex;
                    strikes[n * 5 + 2] = 1.0 - ey;
                    strikes[n * 5 + 3] = 8.3 + Math.sin(k) * 1.2 - ez;
                    strikes[n * 5 + 4] = pa;
                    n++;
                }
                com.hexhaki.piece.mochi.MochiDraw.anemoneArea(sink, -ex, -ey, 8.0 - ez, com.hexhaki.piece.server.MochiMoves.ANEMONE_RADIUS,
                    Math.min(1.0, age / 6.0), strikes, n, coat, age, 9);
            }
            case "mpunch" -> {
                user(sink, "mochi_punch_1", age, ex, ey, ez, age <= com.hexhaki.piece.mochi.MochiDraw.PUNCH_LIFE ? 1 : 0);
                com.hexhaki.piece.mochi.MochiDraw.punch(sink, -0.36 - ex, 1.4 - ey, -ez, 0, 0, 1, 6.0, age, coat, 3);
            }
            case "mtsuki" -> {
                // |mtsuki:age[:c]: age < 0 charging (40 ticks to full), else the thrust that many ticks in (14 long at full)
                user(sink, age < 0 ? "mochi_tsuki_charge" : "mochi_tsuki_thrust", age < 0 ? 40 + age : age, ex, ey, ez);
                double charge = age < 0 ? Math.min(1.0, (age + 40) / 40.0) : 1.0;
                com.hexhaki.piece.mochi.MochiDraw.tsuki(sink, -0.36 - ex, 1.35 - ey, 0.4 - ez, 0, 0, 1, 14.0, age, charge, age * 0.9, coat);
            }
            case "myanagi" -> {
                user(sink, "mochi_yanagi", age, ex, ey, ez, age >= 3 && age <= com.hexhaki.piece.mochi.MochiDraw.YANAGI_LIFE - 3 ? 4 : 0);
                com.hexhaki.piece.mochi.MochiDraw.yanagi(sink, -0.15 - ex, 0.8 - ey, 0.2 - ez, 0, 1, -ey, age, 7);
            }
            case "mkaku" -> {
                user(sink, "mochi_kaku", age, ex, ey, ez, age <= com.hexhaki.piece.mochi.MochiDraw.KAKU_LIFE ? 3 : 0);
                HieShots.body(sink, -ex, -ey, 4.5 - ez);
                com.hexhaki.piece.mochi.MochiDraw.kaku(sink, -ex, 1.4 - ey, -ez, -ex, -ey, 4.5 - ez, 0, 1, age, coat, 4);
            }
            case "myaki" -> {
                // |myaki:t[:c[:held]]: t < 0 held that many ticks back from now (MochiMoves.YAKI_FULL to full: t = -60 a tap, 0 full), else
                // the fist flying 2.2 a tick, the blast at 14 ahead; 3.6.3: the explosion by how long it was held (held, default full)
                double held = move.length > 3 ? Double.parseDouble(move[3]) : com.hexhaki.piece.server.MochiMoves.YAKI_FULL;
                double charge = com.hexhaki.piece.server.MochiMoves.yakiCharge(age < 0 ? com.hexhaki.piece.server.MochiMoves.YAKI_FULL + age : held);
                user(sink, age < 0 ? "mochi_yaki_charge" : "mochi_yaki_fire", age < 0 ? com.hexhaki.piece.server.MochiMoves.YAKI_FULL + age : age, ex, ey, ez);
                if (age < 0) {
                    com.hexhaki.piece.mochi.MochiDraw.swell(sink, -0.36 - ex, 1.3 - ey, 0.2 - ez, 0, 0, 1, charge, age + 60, coat, 2, -ey);
                } else if (age < 6) {
                    com.hexhaki.piece.mochi.MochiDraw.yakiPuff(sink, -0.36 - ex, 1.3 - ey, 1.0 - ez, 0, 0, 1, age, 13);
                    com.hexhaki.piece.mochi.MochiDraw.yaki(sink, -0.36 - ex, 1.3 - ey, 1.0 + age * 2.2 - ez, 0, 0, 1, age, coat, 2, charge);
                } else {
                    HieShots.body(sink, -ex, -ey, 14.0 - ez);
                    com.hexhaki.piece.mochi.MochiDraw.boom(sink, -ex, 1.0 - ey, 14.0 - ez, com.hexhaki.piece.server.MochiMoves.yakiRadius(charge) * 0.65,
                        age - 6, 5);
                }
            }
            case "mwheel" -> {
                com.hexhaki.piece.mochi.MochiDraw.wheel(sink, -ex, 2.2 - ey, -ez, 0, 1, 2.2, age * 0.5, age, 6);
            }
            case "mclub" -> {
                user(sink, "mochi_club", age, ex, ey, ez);
                HieShots.body(sink, -ex, -ey, 4.0 - ez);
                com.hexhaki.piece.mochi.MochiDraw.club(sink, -0.36 - ex, 1.5 - ey, -ez, -ex, -ey, 4.0 - ez, age, coat, 6);
            }
            case "manemone" -> {
                user(sink, "mochi_anemone", age, ex, ey, ez);
                double[] p = new double[20];
                double[][] targets = {{0, 1.2, 6}, {5, 1.0, 1}, {-5, 1.0, -1}, {0, 1.0, -6}};
                for (int k = 0; k < 4; k++) {
                    HieShots.body(sink, targets[k][0] - ex, -ey, targets[k][2] - ez);
                    p[k * 5] = k;
                    p[k * 5 + 1] = targets[k][0];
                    p[k * 5 + 2] = targets[k][1];
                    p[k * 5 + 3] = targets[k][2];
                    p[k * 5 + 4] = (age + k * 2) % 7;
                }
                com.hexhaki.piece.mochi.MochiDraw.anemone(sink, -ex, -ey, -ez, age, Math.min(1.0, age / 6.0), toEye(p, ex, ey, ez), 4, coat, 8);
            }
            case "msticky" -> {
                user(sink, "mochi_throw", age, ex, ey, ez);
                com.hexhaki.piece.mochi.MochiDraw.glob(sink, -0.3 - ex, 1.4 - ey, 2.0 + age * 1.0 - ez, 0, 0, 1, age, 9);
                HieShots.body(sink, 2.5 - ex, -ey, 6 - ez);
                com.hexhaki.piece.mochi.MochiDraw.stuck(sink, 2.5 - ex, -ey, 6 - ez, 1.9, 1.0, age, 10);
                com.hexhaki.piece.mochi.MochiDraw.patch(sink, -2.5 - ex, -ey, 6 - ez, 1.5, 1.0, age, 11);
            }
            case "mhole" -> {
                user(sink, "mochi_punch_1", 0, ex, ey, ez);
                com.hexhaki.piece.mochi.MochiDraw.hole(sink, -ex, 1.1 - ey, 0.16 - ez, 0, 0, 1, age);
            }
            case "mground" -> {
                user(sink, "mochi_awaken", age, ex, ey, ez);
                double[] cells = new double[com.hexhaki.piece.mochi.MochiDraw.GROUND_MOST * 4];
                int n = com.hexhaki.piece.mochi.MochiDraw.groundLumps(0.3, 0.2, 12.0, cells);
                double[] lumps = new double[n * 5];
                for (int k = 0; k < n; k++) {
                    lumps[k * 5] = cells[k * 4] - ex;
                    lumps[k * 5 + 1] = -ey;
                    lumps[k * 5 + 2] = cells[k * 4 + 1] - ez;
                    lumps[k * 5 + 3] = cells[k * 4 + 2];
                    lumps[k * 5 + 4] = cells[k * 4 + 3];
                }
                com.hexhaki.piece.mochi.MochiDraw.ground(sink, lumps, n, Math.min(1.0, age / 10.0), age);
            }
            case "mwave" -> {
                user(sink, "mochi_wave", age, ex, ey, ez);
                com.hexhaki.piece.mochi.MochiDraw.wave(sink, -ex, -ey, -ez, 0, 1, 2.0 + age * 0.7, 8.0, 3.6, age, 13);
            }
            case "mwalls" -> {
                user(sink, "mochi_walls", age, ex, ey, ez);
                HieShots.body(sink, -ex, -ey, 6 - ez);
                com.hexhaki.piece.mochi.MochiDraw.walls(sink, -ex, -ey, 6 - ez, 0, 1, 4.0, age, 14);
            }
            case "mrain" -> {
                user(sink, "mochi_rain", age, ex, ey, ez);
                com.hexhaki.piece.mochi.MochiDraw.rain(sink, -ex, -ey, 8 - ez, 6.0, 14.0, 15, 2.0, age, 15);
            }
            case "mdonuts" -> {
                user(sink, "mochi_donuts", age, ex, ey, ez);
                HieShots.body(sink, -ex, -ey, 14 - ez);
                com.hexhaki.piece.mochi.MochiDraw.donuts(sink, -ex, -ey, -ez, -ex, 1.0 - ey, 14 - ez, age, Math.min(1.0, age / 6.0), 6.0, 4.0, coat, 16);
            }
            case "mdome" -> {
                user(sink, "mochi_dome", age, ex, ey, ez);
                HieShots.body(sink, -ex, -ey, 6 - ez);
                com.hexhaki.piece.mochi.MochiDraw.dome(sink, -ex, -ey, 6 - ez, 5.0, Math.min(1.0, age / 10.0), Math.max(0, Math.min(1, (age - 90) / 8.0)), age, 17);
            }
            case "mfirst" -> {
                // |mfirst:age:what[:c]: one's own move in first person, as client/mochi/MochiFx starts it there (the arm from below and
                // to the right of the view, 0.55 ahead); the camera at the eye (0, 1.62, 0) looking along +z
                String what = move.length > 3 ? move[3] : "punch";
                double sx = -0.4;
                double sy = 1.62 - 0.45;
                double sz = 0.45;
                switch (what) {
                    case "punch" -> com.hexhaki.piece.mochi.MochiDraw.punch(sink, sx - ex, sy - ey, sz - ez, 0, 0, 1, 6.0, age, coat, 3);
                    case "charge" -> com.hexhaki.piece.mochi.MochiDraw.tsuki(sink, -0.55 - ex, 1.02 - ey, 1.7 - ez, 0, 0, 1, 0.0, -1.0,
                        Math.min(1.0, age / 40.0), age * 0.9, coat);
                    case "thrust" -> com.hexhaki.piece.mochi.MochiDraw.tsuki(sink, -0.55 - ex, 1.02 - ey, 2.2 - ez, 0, 0, 1, 14.0, age, 1.0,
                        age * 0.9, coat);
                    case "swell" -> com.hexhaki.piece.mochi.MochiDraw.swell(sink, -0.55 - ex, 1.02 - ey, 1.4 - ez, 0, 0, 1,
                        Math.min(1.0, age / 16.0), age, coat, 2);
                    case "anemone" -> com.hexhaki.piece.mochi.MochiDraw.anemone(sink, -ex, -0.9 - ey, -ez, age, 1.0, new double[0], 0, coat, 8);
                    default -> {
                    }
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
