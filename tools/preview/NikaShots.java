import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gear.NikaArt;

/**
 * 3.2.6: the Nika fruit's drawn shapes (gear/NikaArt) for FxPreview, round the scene's middle (the ground at 0, a body standing at the
 * middle facing +z). Shots after the camera: {@code |nfist:radius[:way]} the Bajrang Gun's giant fist over a body for scale (way: "down"
 * knuckles to the ground, "ahead" toward +z, "up" over its head).
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
            default -> {
                return false;
            }
        }
        return true;
    }
}
