import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.ito.ItoArt;
import com.hexhaki.piece.server.ItoMoves;

/**
 * 3.3.7: the Ito Ito no Mi's strings (ito/ItoArt) for FxPreview, round the scene's middle (the user standing there facing +z, their
 * right hand at (-0.36, 1.15, 0.35) as client/ito/ItoFx.hand puts it). Shots after the camera, an age, and "c" for Armament on:
 * {@code |islash:age} the lash, {@code |ibullet:age} a burst of bullets, {@code |ihit:age}, {@code |igoshiki:age} onto (0, 0, 7),
 * {@code |iheat:age} Overheat, {@code |iparasite:age} strings to a body at (0, 0, 5), {@code |iweb:age}, {@code |icage:age:radius}
 * (default the owner's 40 closing in), {@code |iwave:age}, {@code |ithread:age} at (0, 1, 12), {@code |isky:age}, {@code |istitch:age}.
 */
public final class ItoShots {
    private ItoShots() {
    }

    static double HX = -0.36;
    static double HY = 1.15;
    static double HZ = 0.35;

    static boolean shot(FireMesh.Sink sink, double[] eye, String[] move) {
        // a shot ending "fp": the hand where ItoFx.hand puts one's own in first person (the eye at 1.62 looking along +z)
        boolean fp = move[0].endsWith("fp");
        if (fp) {
            move = move.clone();
            move[0] = move[0].substring(0, move[0].length() - 2);
        }
        HX = fp ? -0.32 : -0.36;
        HY = fp ? 1.32 : 1.15;
        HZ = fp ? 0.7 : 0.35;
        double ex = eye[0];
        double ey = eye[1];
        double ez = eye[2];
        double age = move.length > 1 ? Double.parseDouble(move[1]) : 4;
        boolean coat = move.length > 2 && move[2].equals("c");
        switch (move[0]) {
            case "islash" -> {
                if (!fp) {
                    HieShots.body(sink, -ex, -ey, -ez);
                }
                ItoArt.lash(sink, HX - ex, HY - ey, HZ - ez, 0, 1, ItoMoves.SLASH_REACH, 1, age, coat);
            }
            case "ibullet" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                for (int k = 0; k < 3; k++) {
                    double a = age - k * 4;
                    if (a >= 0) {
                        ItoArt.bullet(sink, HX - ex + 0.1 * k, HY + 0.1 - ey, HZ + 0.6 + ItoMoves.BULLET_SPEED * a - ez, 0, 0, 1, a, coat);
                    }
                }
            }
            case "ihit" -> ItoArt.hit(sink, -ex, 1.2 - ey, 3 - ez, 0, 0, -1, 1.2, age, coat, 3);
            case "igoshiki" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                HieShots.body(sink, 0.6 - ex, -ey, 7.5 - ez);
                ItoArt.goshiki(sink, -ex, -ey, 7 - ez, 0, 1, ItoMoves.GOSHIKI_SPREAD, ItoMoves.GOSHIKI_FALL, age, ItoMoves.GOSHIKI_LIFE, coat, 5);
            }
            case "iheat" -> {
                if (!fp) {
                    HieShots.body(sink, -ex, -ey, -ez);
                }
                ItoArt.overheat(sink, HX - ex, HY - ey, HZ - ez, 0, 1, ItoMoves.OVERHEAT_LENGTH, age, ItoMoves.OVERHEAT_LIFE, coat, 7);
            }
            case "iparasite" -> {
                if (!fp) {
                    HieShots.body(sink, -ex, -ey, -ez);
                }
                HieShots.body(sink, -ex, 0.4 - ey, 5 - ez);
                double[] p = {-ex, 0.4 + 1.8 - ey, 5 - ez, 0.33 - ex, 0.4 + 1.3 - ey, 5 - ez, -0.33 - ex, 0.4 + 1.3 - ey, 5 - ez, 0.15 - ex, 0.4 + 0.2 - ey,
                    5 - ez, -0.15 - ex, 0.4 + 0.2 - ey, 5 - ez};
                ItoArt.parasite(sink, HX - ex, HY - ey, HZ - ez, p, age, 1.0, coat);
            }
            case "iweb" -> {
                if (!fp) {
                    HieShots.body(sink, -ex, -ey, -ez);
                }
                ItoArt.web(sink, -ex, 1.1 - ey, 2.2 - ez, 0, 0, 1, ItoMoves.WEB_RADIUS, age, ItoMoves.WEB_TICKS, coat, fp ? 0.45 : 1.0);
            }
            case "icage" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                double radius = move.length > 3 ? Double.parseDouble(move[3])
                    : ItoArt.cageRadius(ItoMoves.CAGE_RADIUS, ItoMoves.CAGE_END, age, ItoMoves.CAGE_TICKS);
                ItoArt.cage(sink, -ex, -ey, -ez, radius, age, 1.0, coat);
            }
            case "iwave" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                for (int k = 0; k < 5; k++) {
                    HieShots.body(sink, -6 + 3 * k - ex, -ey, 9 - ez);
                }
                ItoArt.waves(sink, -ex, -ey, -ez, ItoMoves.WAVE_REACH, ItoMoves.WAVES, ItoMoves.WAVE_EVERY, ItoMoves.WAVE_SPEED, age, 60, coat);
            }
            case "ithread" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                HieShots.body(sink, -ex, -ey, 12 - ez);
                ItoArt.thread(sink, -ex, 1 - ey, 12 - ez, -ex, 1.25 - ey, -ez, ItoMoves.SPEARS, age, ItoMoves.THREAD_FLIGHT);
            }
            case "isky" -> {
                HieShots.body(sink, -ex, 3 - ey, -ez);
                ItoArt.sky(sink, -ex, 3 - ey, -ez, age, coat, 4);
            }
            case "istitch" -> {
                HieShots.body(sink, -ex, -ey, -ez);
                ItoArt.stitch(sink, -ex, 1.0 - ey, -ez, 1.8, age, 60, 9);
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
