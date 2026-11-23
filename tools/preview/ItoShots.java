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
                // 3.4.2: the hand raised (ito_goshiki's beats: up by 3) then raked down on 5; the strip from the feet along +z
                if (!fp) {
                    HieShots.body(sink, -ex, -ey, -ez);
                }
                HieShots.body(sink, 0.6 - ex, -ey, 9 - ez);
                HieShots.body(sink, -1.2 - ex, -ey, 15 - ez);
                double up = Math.min(1.0, age / 3.0);
                double down = Math.max(0.0, Math.min(1.0, (age - 4.0) / 1.0));
                double hy = 1.15 + 1.3 * up - 1.5 * down;
                double hz = 0.35 + 0.2 * up + 0.4 * down;
                ItoArt.goshiki(sink, HX - ex, hy - ey, hz - ez, -ex, -ey, -ez, 0, 1, ItoMoves.GOSHIKI_LENGTH, age, ItoMoves.GOSHIKI_LIFE, coat, 5);
            }
            case "iknot" -> {
                // Overheat held: the arm pointed ahead, the fist at about (-0.36, 1.38, 0.95)
                double charge = move.length > 3 ? Double.parseDouble(move[3]) : Math.min(1.0, age / ItoMoves.HEAT_FULL);
                if (!fp) {
                    HieShots.body(sink, -ex, -ey, -ez);
                }
                double kz = fp ? 0.9 : 0.95;
                ItoArt.knot(sink, HX - ex, (fp ? 1.32 : 1.38) - ey, kz - ez, 0, 0, 1, charge, age, coat, 7);
            }
            case "ibeam" -> {
                double charge = move.length > 3 ? Double.parseDouble(move[3]) : 1.0;
                double length = 24.0;
                double flight = Math.ceil(length / ItoMoves.HEAT_SPEED);
                if (!fp) {
                    HieShots.body(sink, -ex, -ey, -ez);
                }
                HieShots.body(sink, -ex, -ey, 25 - ez);
                // in first person one's own beam starts ItoFx.OWN_BEAM_GAP ahead
                double gap = fp ? 1.2 : 0.0;
                ItoArt.beam(sink, HX - ex, 1.38 - ey, 1.0 + gap - ez, 0, 0.0, 1, length - gap, charge, age, flight, coat, 7, fp);
                if (age >= flight) {
                    ItoArt.scatter(sink, HX - ex, 1.38 - ey, 1.0 + length - ez, (2.2 + 2.6 * charge) * 1.6, true, age - flight, coat, 7, false);
                }
            }
            case "iknothand" -> {
                // one's own knot in first person as client/ito/ItoFx.handPass draws it: at the game's fingertip (about 0.75 right, 0.43
                // down, 1.2 ahead of the eye: LightArt.firstPersonFingertip), HAND_KNOT its size
                double charge = move.length > 3 ? Double.parseDouble(move[3]) : 1.0;
                ItoArt.knot(sink, -0.75, -0.43, 1.2, 0, 0, 1, charge, age, coat, 7, 0.45);
            }
            case "iscatter" -> {
                boolean heat = move.length > 3 && move[3].equals("h");
                ItoArt.scatter(sink, -ex, 1.2 - ey, 6 - ez, heat ? 7.6 : 6.3, heat, age, coat, 11, false);
            }
            case "istream" -> {
                double radius = move.length > 3 ? Double.parseDouble(move[3]) : ItoMoves.cageSize(age);
                if (!fp) {
                    HieShots.body(sink, -ex, -ey, -ez);
                }
                ItoArt.stream(sink, HX - ex, (fp ? 1.32 + 3.0 : 2.35) - ey, (fp ? 0.7 : 0.1) - ez, -ex, radius - ey, -ez, radius, age, coat, 3);
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
                // 3.4.2: |icage:age:c-or-b:radius:phase (phase s: standing, spreading from the crown at age 0; c: closing, age into it)
                HieShots.body(sink, -ex, -ey, -ez);
                HieShots.body(sink, 4 - ex, -ey, 6 - ez);
                double radius = move.length > 3 ? Double.parseDouble(move[3]) : ItoMoves.CAGE_RADIUS;
                boolean closing = move.length > 4 && move[4].equals("c");
                double r = closing ? ItoMoves.cageRadius(radius, age, ItoMoves.CAGE_CLOSE) : radius;
                double spread = closing ? 1.0 : Math.min(1.0, age / ItoArt.CAGE_SPREAD);
                double tension = closing ? Math.min(1.0, age / ItoMoves.CAGE_CLOSE) : 0.0;
                ItoArt.cage(sink, -ex, -ey, -ez, r, spread, tension, 1.0, coat);
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
                // 3.4.3: a Sky Road runner 4 up, running along +z: a string from each hand to its anchor high ahead (the right one
                // tightening with the age), one let go behind, the feet's streak
                HieShots.body(sink, -ex, 4 - ey, -ez);
                double taut = Math.max(0.0, 1.0 - (age % 8) / 4.0);
                ItoArt.sky(sink, -0.75 - ex, 5.05 - ey, 0.1 - ez, -3.4 - ex, 20 - ey, 13 - ez, 0.75, taut, true, coat);
                ItoArt.sky(sink, 0.75 - ex, 5.05 - ey, 0.1 - ez, 2.8 - ex, 19 - ey, 11 - ez, 0.75, 0.0, true, coat);
                ItoArt.sky(sink, 0.4 - ex, 9.0 - ey, -2 - ez, 2.4 - ex, 18 - ey, -5 - ez, 0.3, 0.0, false, coat);
                double[] feet = new double[24];
                for (int k = 0; k < 8; k++) {
                    feet[k * 3] = 0.1 * Math.sin(k * 0.8);
                    feet[k * 3 + 1] = 4.12;
                    feet[k * 3 + 2] = -0.9 * k;
                }
                ItoArt.streak(sink, feet, 8, ex, ey, ez, 1.0, coat);
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
