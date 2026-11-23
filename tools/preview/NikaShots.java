import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gear.NikaArt;
import com.hexhaki.piece.gear.NikaBend;
import com.hexhaki.piece.gear.NikaHold;
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
                        double tilt = Math.atan(RubberWave.slope(r, angle, t, radius, end)) * 0.6;
                        grass(sink, x - ex, h - ey, z - ez, cx / Math.max(r, 1e-6), cz / Math.max(r, 1e-6), tilt);
                    }
                }
                for (double[] b : new double[][] {{5.5, 3.5}, {-3.5, 9.5}}) {
                    double r = Math.hypot(b[0], b[1]);
                    double h = RubberWave.rise(r, Math.atan2(b[1], b[0]), t, radius, end);
                    HieShots.body(sink, b[0] - ex, h - ey, b[1] - ez);
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
    static void grass(FireMesh.Sink sink, double x, double y, double z, double rx, double rz, double tilt) {
        double c = Math.cos(tilt);
        double sn = Math.sin(tilt);
        double[][] corner = new double[8][3];
        for (int i = 0; i < 8; i++) {
            double vx = ((i & 1) != 0 ? 1 : 0) - 0.5;
            double vy = ((i & 2) != 0 ? 0 : -1);
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
        int[] colour = {0x7A5634, 0x7A5634, 0x6B4A2E, 0x6FB043, 0x7A5634, 0x7A5634};
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
