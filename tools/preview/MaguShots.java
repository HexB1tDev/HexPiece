import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.magu.MaguArt;

/**
 * 3.1.3: the Magu Magu no Mi's magma (magu/MaguArt) for FxPreview, round the scene's middle (the ground at 0). Shots after the camera:
 * {@code |mfist:age:size}, {@code |mburst:age:radius}, {@code |mpool:age:radius}, {@code |mmeteor:age}, {@code |mhound:age},
 * {@code |mgeyser:age}, {@code |mstream:age}, {@code |mpierce:age}, {@code |mswell:age}, {@code |mfp:age} (first person, the swelling fist).
 */
public final class MaguShots {
    private MaguShots() {
    }

    static boolean shot(FireMesh.Sink sink, double[] eye, String[] move) {
        double ex = eye[0];
        double ey = eye[1];
        double ez = eye[2];
        double a = move.length > 1 ? Double.parseDouble(move[1]) : 0.0;
        switch (move[0]) {
            case "mfist" -> MaguArt.fist(sink, -ex, 1.6 - ey, 6.0 + a * 1.7 - ez, 0, 0, 1, Double.parseDouble(move[2]), a, 7);
            case "mburst" -> MaguArt.burst(sink, -ex, 0.5 - ey, 8.0 - ez, Double.parseDouble(move[2]), 1.0, a, 9);
            case "mpool" -> MaguArt.pool(sink, -ex, -ey, 5.0 - ez, Double.parseDouble(move[2]), a, 100, 4);
            case "mmeteor" -> {
                double t = a / com.hexhaki.piece.server.MaguMoves.METEOR_FALL;
                MaguArt.meteor(sink, 4.0 * (1 - t) - ex, 26.0 * (1 - t) + 0.5 - ey, 14.0 - ez, -4.0 / 12, -26.0 / 12, 0, 0.9, a, 5);
            }
            case "mhound" -> MaguArt.hound(sink, -ex, 2.0 - ey, 4.0 + a * com.hexhaki.piece.server.MaguMoves.HOUND_SPEED - ez, 0,
                move.length > 2 ? Double.parseDouble(move[2]) : 0.0, 1, com.hexhaki.piece.server.MaguMoves.HOUND_SCALE, a, 11);
            case "mgeyser" -> MaguArt.geyser(sink, -ex, -ey, 8.0 - ez, com.hexhaki.piece.server.MaguMoves.GEYSER_HEIGHT, a, 3);
            case "mstream" -> {
                double[] pts = new double[12 * 3];
                for (int i = 0; i < 12; i++) {
                    pts[i * 3] = Math.sin(i * 0.3) * 0.6 - ex;
                    pts[i * 3 + 1] = -ey;
                    pts[i * 3 + 2] = 8.0 - i * 1.6 - ez;
                }
                MaguArt.stream(sink, pts, 12, a, 6);
            }
            case "mpierce" -> MaguArt.pierce(sink, -ex, 1.1 - ey, 5.0 - ez, 0, 0, 1, 1.0, a, 8);
            case "mswell" -> MaguArt.swell(sink, 0.35 - ex, 1.35 - ey, 0.8 - ez, 0.25 + 0.35 * Math.min(1.0, a / 40.0), a, 3);
            case "mfp" -> {
                double[] v = com.hexhaki.piece.pika.LightArt.firstPersonFingertip(-0.05);
                MaguArt.swell(sink, -v[0], v[1] + 0.05, -v[2] + 0.1, 0.22, a, 3);
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
