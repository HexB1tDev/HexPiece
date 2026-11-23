import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.noro.NoroArt;
import com.hexhaki.piece.noro.NoroMirror;

/**
 * 3.3.4: the Noro Noro no Mi's photons (noro/NoroArt) for FxPreview, round the scene's middle (the ground at 0, a body standing at the
 * middle facing +z, its hands at about 1.2 up and 0.7 ahead). Shots after the camera:
 * {@code |nshot:age} a burst of photons 4 blocks ahead flying +z, {@code |nbeam:age} the beam from the hands 14 blocks along +z to a body
 * there (slowed: its rings round it), {@code |naura:age} a slowed body, {@code |npop:age} a burst ending on a body,
 * {@code |nstore:age}, {@code |nrelease:age[:size]}, {@code |nmirror:age} the mirror 2.2 ahead facing the user, {@code |ndome:age} the
 * Slow Field round the user, {@code |nsnap:age}.
 */
public final class NoroShots {
    private NoroShots() {
    }

    static boolean shot(FireMesh.Sink sink, double[] eye, String[] move) {
        double ex = eye[0];
        double ey = eye[1];
        double ez = eye[2];
        double age = move.length > 1 ? Double.parseDouble(move[1]) : 4;
        switch (move[0]) {
            case "nshot" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                NoroArt.shot(sink, -ex, 1.2 - ey, 4.0 + age * 0.4 - ez, 0, 0, 1, age);
            }
            case "nbeam" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                HieShots.body(sink, -ex, -ey, 14.4 - ez);
                NoroArt.beam(sink, -ex, 1.2 - ey, 0.7 - ez, -ex, 1.2 - ey, 14.0 - ez, age);
                NoroArt.aura(sink, -ex, 0.95 - ey, 14.4 - ez, 0.6, 1.9, age, 1.0);
            }
            case "naura" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                NoroArt.aura(sink, -ex, 0.95 - ey, -ez, 0.6, 1.9, age, 1.0);
            }
            case "npop" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                NoroArt.pop(sink, -ex, 0.95 - ey, -0.3 - ez, age, true);
            }
            case "nstore" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                NoroArt.aura(sink, -ex, 0.95 - ey, -ez, 0.6, 1.9, 40 + age, 1.0);
                NoroArt.store(sink, -ex, 1.1 - ey, -0.3 - ez, 0, 1, age);
            }
            case "nrelease" -> {
                double size = move.length > 2 ? Double.parseDouble(move[2]) : 0.8;
                HieShots.body(sink, -ex, -ey, -ez);
                NoroArt.release(sink, -ex, 0.95 - ey, -ez, size, age);
            }
            case "nmirror" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                NoroMirror g = new NoroMirror(0, 1.4, 2.2, 0, 0, -1);
                NoroArt.mirror(sink, -ex, 1.4 - ey, 2.2 - ez, g, age, 100);
            }
            case "ndome" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                HieShots.body(sink, 3.0 - ex, -ey, 2.5 - ez);
                NoroArt.aura(sink, 3.0 - ex, 0.95 - ey, 2.5 - ez, 0.6, 1.9, age, 1.0);
                NoroArt.dome(sink, -ex, -ey, -ez, 6.0, age, 160);
            }
            case "nsnap" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                NoroArt.snap(sink, -ex, 2.0 - ey, 0.2 - ez, age);
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
