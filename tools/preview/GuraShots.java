import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gura.GuraArt;

/**
 * 3.1.1: the Gura Gura no Mi's effects (gura/GuraArt) for FxPreview, placed round the scene's middle (the player-sized figure stands at
 * the origin, the ground at 0). FxPreview shots (after the camera, e.g. {@code e:200:15:12:0:1:6}):
 * {@code |gcrack:age[:radius]}, {@code |gbubble:age:radius}, {@code |gshock:age:reach:strength}, {@code |gquake:age:radius},
 * {@code |gtremor:age}, {@code |gshake:held}, {@code |gtilt:age}, {@code |grift:age}, {@code |gwave:age}, {@code |gkaishin:age:charge}
 * (the whole charged punch: crack, shock, craters), {@code |gslam:age} (Kabutowari's landing: crack over the head, the quake).
 */
public final class GuraShots {
    private GuraShots() {
    }

    static boolean shot(FireMesh.Sink sink, double[] eye, String[] move) {
        double ex = eye[0];
        double ey = eye[1];
        double ez = eye[2];
        double a = move.length > 1 ? Double.parseDouble(move[1]) : 0.0;
        switch (move[0]) {
            case "gcrack" -> {
                double r = move.length > 2 ? Double.parseDouble(move[2]) : 1.6;
                GuraArt.crack(sink, 0.3 - ex, 1.4 - ey, 1.6 - ez, 0, 0, 1, r, a, 7);
            }
            case "gbubble" -> GuraArt.bubble(sink, 0.35 - ex, 1.35 - ey, 0.7 - ez, Double.parseDouble(move[2]), a, 3);
            case "gshock" -> GuraArt.shock(sink, 0.3 - ex, 1.4 - ey, 1.0 - ez, 0, 0, 1, Double.parseDouble(move[2]), 0.42,
                Double.parseDouble(move[3]), com.hexhaki.piece.server.GuraMoves.SHOCK_SPEED, a, 5);
            case "gquake" -> GuraArt.quake(sink, -ex, -ey, 6.0 - ez, Double.parseDouble(move[2]), 1.0, a, 9);
            case "gtremor" -> GuraArt.tremor(sink, -ex, -ey, -ez, 4.0, a, 4);
            case "gshake" -> GuraArt.shake(sink, -ex, -ey, -ez, com.hexhaki.piece.server.GuraMoves.SHAKE_RADIUS, a, 10, 6);
            case "gtilt" -> GuraArt.tilt(sink, -ex, -ey, -ez, com.hexhaki.piece.server.GuraMoves.SHAKE_RADIUS, 1.0,
                com.hexhaki.piece.server.GuraMoves.SHOCK_SPEED, a, 6);
            case "grift" -> {
                double[] heights = new double[40];
                GuraArt.rift(sink, 0.0 - ex, -ey, 1.4 - ez, 0, 1, com.hexhaki.piece.server.GuraMoves.RIFT_LENGTH,
                    com.hexhaki.piece.server.GuraMoves.RIFT_STEP, heights, a, 8);
            }
            case "gwave" -> GuraArt.tsunami(sink, -ex, -ey, 3.0 - ez, 0, com.hexhaki.piece.server.GuraMoves.TSUNAMI_SPEED,
                com.hexhaki.piece.server.GuraMoves.TSUNAMI_WIDTH, com.hexhaki.piece.server.GuraMoves.TSUNAMI_HEIGHT,
                com.hexhaki.piece.server.GuraMoves.TSUNAMI_LIFE, com.hexhaki.piece.server.GuraMoves.TSUNAMI_RISE, a, 11);
            case "gkaishin" -> {
                double charge = Double.parseDouble(move[2]);
                double reach = 18.0 + 22.0 * charge;
                double speed = com.hexhaki.piece.server.GuraMoves.SHOCK_SPEED;
                GuraArt.crack(sink, 0.3 - ex, 1.45 - ey, 1.7 - ez, 0, 0, 1, 2.0 + 3.0 * charge, a, 7);
                GuraArt.shock(sink, 0.3 - ex, 1.45 - ey, 1.0 - ez, 0, 0, 1, reach, 0.36 + 0.12 * charge, 0.5 + 0.5 * charge, speed, a, 5);
                for (double d = 5.0; d < reach; d += 7.0) {
                    GuraArt.quake(sink, 0.3 - ex, -ey, 1.0 + d - ez, (1.2 + 1.3 * charge) * 1.8, 0.4 + 0.4 * charge, a - d / speed, 20 + (int) d);
                }
            }
            case "gslam" -> {
                GuraArt.crack(sink, -ex, 2.3 - ey, 4.0 - ez, 0, 0, -1, 2.4, a, 12);
                GuraArt.quake(sink, -ex, -ey, 4.0 - ez, 7.0, 1.0, a, 13);
            }
            case "gfp" -> {
                // first person (FxPreview's fp:pitch camera, looking along +z): the bubble on the game's own right hand
                // (LightArt.firstPersonFingertip, as client/FirstPersonLight.fist places it), the crack and the shock out of it
                double[] v = com.hexhaki.piece.pika.LightArt.firstPersonFingertip(-0.05);
                double pr = Math.toRadians(Double.parseDouble(move[2]));
                double[] fw = {0, -Math.sin(pr), Math.cos(pr)};
                double[] up = {0, Math.cos(pr), Math.sin(pr)};
                double[] right = {-1, 0, 0};
                double fx = right[0] * v[0] + up[0] * v[1] - fw[0] * v[2];
                double fy = right[1] * v[0] + up[1] * v[1] - fw[1] * v[2];
                double fz = right[2] * v[0] + up[2] * v[1] - fw[2] * v[2];
                String what = move.length > 3 ? move[3] : "bubble";
                if (what.equals("bubble")) {
                    GuraArt.bubble(sink, fx, fy, fz, 0.22, a, 3);
                } else {
                    // as client/gura/GuraFx pushes one's own crack out in first person (1.7 times its radius from the eye)
                    GuraArt.crack(sink, fw[0] * 5.1, fw[1] * 5.1, fw[2] * 5.1, fw[0], fw[1], fw[2], 3.0, a, 7);
                    GuraArt.shock(sink, fx, fy, fz, fw[0], fw[1], fw[2], 30, 0.42, 1.0, com.hexhaki.piece.server.GuraMoves.SHOCK_SPEED, a, 5);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
