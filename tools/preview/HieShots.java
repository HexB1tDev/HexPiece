import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.hie.HieArt;

/**
 * 3.1.9: the Hie Hie no Mi's drawn ice (hie/HieArt, the smooth glassy look the owner chose) for FxPreview, round the scene's middle (the
 * ground at 0, a body standing at the middle facing +z). The frozen bodies and the frozen ground are real blocks in the game (they cannot
 * be drawn here). Shots after the camera: {@code |hshatter:age}, {@code |hburst:age:radius}, {@code |hspear:grow}, {@code |hspears:age}
 * (Partisan's spears in the air round the body), {@code |hflight:age}, {@code |horb:age}, {@code |hgather:age},
 * {@code |hpheasant:age[:scale]}, {@code |hcoat:age}, {@code |hsaber}, {@code |hslash:age}, {@code |hscatter:age}, {@code |hmist:age}.
 */
public final class HieShots {
    private HieShots() {
    }

    /** A player-sized body (opaque boxes) standing at x, y, z (relative to the eye). */
    static void body(FireMesh.Sink sink, double x, double y, double z) {
        box(sink, x - 0.3, y, z - 0.15, x + 0.3, y + 0.75, z + 0.15, 0x3A4A7A);
        box(sink, x - 0.3, y + 0.75, z - 0.15, x + 0.3, y + 1.4, z + 0.15, 0xB03A2E);
        box(sink, x - 0.25, y + 1.4, z - 0.25, x + 0.25, y + 1.9, z + 0.25, 0xD9A77E);
    }

    static void box(FireMesh.Sink sink, double x0, double y0, double z0, double x1, double y1, double z1, int rgb) {
        double[][] c = {{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
        int[][] f = {{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}};
        double[] shade = {0.8, 0.8, 0.6, 0.6, 1.0, 0.5};
        for (int k = 0; k < 6; k++) {
            int col = FireMesh.argb(1.0, FireMesh.mix(0, rgb, shade[k]));
            double[] q = new double[12];
            for (int i = 0; i < 4; i++) {
                System.arraycopy(c[f[k][i]], 0, q, i * 3, 3);
            }
            sink.quad(q, new int[] {col, col, col, col});
        }
    }

    static boolean shot(FireMesh.Sink sink, double[] eye, String[] move) {
        double ex = eye[0];
        double ey = eye[1];
        double ez = eye[2];
        double a = move.length > 1 ? Double.parseDouble(move[1]) : 0.0;
        switch (move[0]) {
            case "hshatter" -> HieArt.shatter(sink, -ex, 1.0 - ey, -ez, 1.2, a, 6);
            case "hburst" -> HieArt.burst(sink, -ex, 0.6 - ey, 4.0 - ez, move.length > 2 ? Double.parseDouble(move[2]) : 3.0, 1.0, a, 7);
            case "hspear" -> HieArt.spear(sink, -ex, 1.6 - ey, 3.0 - ez, 1, 0, 0, HieArt.SPEAR_LENGTH, a, 1.0, 8);
            case "hspears" -> {
                body(sink, -ex, -ey, -ez);
                for (int k = 0; k < 8; k++) {
                    double[] s = HieArt.slot(k);
                    // facing +z: his right is -x, behind him -z
                    double x = -s[0];
                    double y = s[1];
                    double z = -s[2];
                    double grow = Math.max(0.0, Math.min(1.0, (a - k * 5.0) / 4.0));
                    double fx = -x * 0.06;
                    double fy = 0.1;
                    double fz = 1.0;
                    double fl = Math.sqrt(fx * fx + fy * fy + fz * fz);
                    HieArt.spear(sink, x - ex, y - ey, z - ez, fx / fl, fy / fl, fz / fl, HieArt.SPEAR_LENGTH, grow, 1.0, 40 + k);
                }
            }
            case "hflight" -> {
                double z = 2.0 + a * 2.4;
                HieArt.spear(sink, -ex, 1.6 - ey, z - ez, 0, 0, 1, HieArt.SPEAR_LENGTH, 1.0, 1.0, 9);
                HieArt.trail(sink, -ex, 1.6 - ey, z - 1.2 - ez, 0, 0, 2.4, 0.18, a, 9);
            }
            case "horb" -> HieArt.orb(sink, -ex, 1.4 - ey, 2.5 - ez, 0.35, a, 10);
            case "hgather" -> HieArt.gather(sink, -ex, 1.25 - ey, 0.6 - ez, 0.35, a, 11);
            case "hpheasant" -> HieArt.pheasant(sink, -ex, 2.6 - ey, 6.0 + a * 0.5 - ez, 0, 0, 1,
                move.length > 2 ? Double.parseDouble(move[2]) : 2.4, a, 13);
            case "hcoat" -> {
                body(sink, -ex, -ey, -ez);
                HieArt.coat(sink, -0.35 - ex, 1.3 - ey, 0.7 - ez, 0, 0, 1, 1.0, a, 16);
            }
            case "hsaber" -> {
                // the saber held out level ahead of the body's right hand: its grip frame's -y onto +z
                body(sink, -ex, -ey, -ez);
                FireMesh.Sink turned = (xyz, argb) -> {
                    double[] q = new double[12];
                    for (int k = 0; k < 4; k++) {
                        q[k * 3] = xyz[k * 3] - 0.35 - ex;
                        q[k * 3 + 1] = xyz[k * 3 + 2] + 1.1 - ey;
                        q[k * 3 + 2] = -xyz[k * 3 + 1] + 0.5 - ez;
                    }
                    sink.quad(q, argb);
                };
                HieArt.saber(turned, HieArt.SABER_LENGTH, 0.5);
            }
            case "hslash" -> HieArt.slash(sink, -ex, 1.2 - ey, 0.6 - ez, 0, 1, 0, Math.PI * 0.2, 2.4, a, 17);
            case "hscatter" -> HieArt.scatter(sink, -ex, 0.95 - ey, -ez, 1.8, a, 19);
            case "hmist" -> {
                double[] pts = new double[9 * 3];
                for (int i = 0; i < 9; i++) {
                    double ang = Math.toRadians(-45 + 11.25 * i);
                    pts[i * 3] = Math.sin(ang) * a * 1.5 - ex;
                    pts[i * 3 + 1] = 0.6 - ey;
                    pts[i * 3 + 2] = Math.cos(ang) * a * 1.5 - ez;
                }
                HieArt.mistRow(sink, pts, 9, 1.3, 0.45, a, 20);
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
