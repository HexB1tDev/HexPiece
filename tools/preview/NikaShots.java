import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gear.NikaArt;
import com.hexhaki.piece.gear.NikaHold;

/**
 * 3.2.6: the Nika fruit's drawn shapes (gear/NikaArt) for FxPreview, round the scene's middle (the ground at 0, a body standing at the
 * middle facing +z). Shots after the camera: {@code |nfist:radius[:way]} the Bajrang Gun's giant fist over a body for scale (way: "down"
 * knuckles to the ground, "ahead" toward +z, "up" over its head).
 *
 * <p>3.2.8: {@code |ngrab:yaw:age[:arm]} the Gum-Gum Giant (the player model's own boxes, placed and scaled as PlayerRenderer and
 * GearBodyDeformer do, four times the size) turned {@code yaw} degrees, its right arm out at {@code arm} degrees of pitch (-90: the
 * hold), a body grabbed {@code age} ticks ago standing 6 blocks off at the start, flown into the fist as gear/NikaHold places it.
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
}
