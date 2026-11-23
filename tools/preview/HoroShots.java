import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.horo.HoroArt;

/**
 * 3.3.6: the Horo Horo no Mi's ghosts (horo/HoroArt) for FxPreview, round the scene's middle (a body standing there facing +z). Shots
 * after the camera: {@code |hnegative:age} a Negative Hollow 2 blocks ahead at head height flying +z, {@code |hnegatives:age} a stream of
 * five, {@code |hmini:age} Mini Hollows round a body, {@code |htoku:age} the Toku Hollow 4 ahead, {@code |hpop:age},
 * {@code |hboom:age}, {@code |hgloom:age} despair over a body, {@code |hnetwork:age} six ghosts round the user.
 */
public final class HoroShots {
    private HoroShots() {
    }

    static boolean shot(FireMesh.Sink sink, double[] eye, String[] move) {
        double ex = eye[0];
        double ey = eye[1];
        double ez = eye[2];
        double age = move.length > 1 ? Double.parseDouble(move[1]) : 4;
        switch (move[0]) {
            case "hnegative" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                HoroArt.ghost(sink, -ex, 1.5 - ey, 2.0 - ez, 0, 0, -1, 0.7, HoroArt.NEGATIVE, age, 1.0, 1);
            }
            case "hnegatives" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                for (int k = 0; k < 5; k++) {
                    HoroArt.ghost(sink, -0.8 + 0.4 * k - ex, 1.3 + 0.2 * Math.sin(k * 1.7) - ey, 2.0 + 1.6 * k - ez, 0.1 * (k - 2), 0, 1, 0.7,
                        HoroArt.NEGATIVE, age + k * 5, 1.0, k);
                }
            }
            case "hmini" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                for (int k = 0; k < 4; k++) {
                    double a = k * Math.PI / 2 + 0.4;
                    HoroArt.ghost(sink, Math.cos(a) * 0.55 - ex, 1.2 + 0.25 * (k % 2) - ey, Math.sin(a) * 0.55 - ez, Math.cos(a), 0, Math.sin(a), 0.32,
                        HoroArt.MINI, age, 1.0, k);
                }
            }
            case "htoku" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                HoroArt.ghost(sink, -ex, 2.2 - ey, 4.0 - ez, 0, 0, -1, 2.6, HoroArt.TOKU, age, 1.0, 3);
            }
            case "hpop" -> HoroArt.burst(sink, -ex, 1.2 - ey, -ez, 1.0, age, 5);
            case "hboom" -> HoroArt.burst(sink, -ex, 1.5 - ey, -ez, 6.0, age, 9);
            case "hgloom" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                HoroArt.gloom(sink, -ex, 1.95 - ey, -ez, 0.6, 1.9, age, 1.0);
            }
            case "hnetwork" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                for (int k = 0; k < 6; k++) {
                    double a = k * Math.PI / 3 + age * 0.03;
                    double gx = Math.cos(a) * 3.2;
                    double gz = Math.sin(a) * 3.2;
                    HoroArt.ghost(sink, gx - ex, 2.2 + 0.3 * Math.sin(age * 0.1 + k) - ey, gz - ez, -Math.sin(a), 0, Math.cos(a), 0.6, HoroArt.MINI,
                        age, 0.9, k);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
