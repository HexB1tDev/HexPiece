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

    /**
     * 3.1.6: the Gura ultimate {@code age} ticks after the strike ({@code boost} 1.6 with Ryou on), the player at the origin looking along
     * +z: the heaving plates (gura/Heave) as grass-topped dirt cubes where the game draws the world's own blocks (client/gura/HeaveDraw),
     * the wave (GuraArt.heave), the five narrow fissures, the craters' quakes, the air's crack at the fist and the shock.
     */
    static void ultimate(FireMesh.Sink sink, double ex, double ey, double ez, double age, double boost) {
        double reach = com.hexhaki.piece.gura.Heave.REACH * boost;
        double cone = com.hexhaki.piece.gura.Heave.CONE;
        double speed = com.hexhaki.piece.gura.Heave.WAVE;
        for (com.hexhaki.piece.gura.Heave.Plate p : com.hexhaki.piece.gura.Heave.plates(0.0, 0.0, 0.0, 1.0, cone, reach, speed, 9)) {
            if (!com.hexhaki.piece.gura.Heave.shows(p, age)) {
                continue;
            }
            double lift = com.hexhaki.piece.gura.Heave.lift(p, age);
            double tip = com.hexhaki.piece.gura.Heave.tip(p, age);
            double cx = p.x() + p.size() * 0.5;
            double cz = p.z() + p.size() * 0.5;
            for (int i = 0; i < p.size(); i++) {
                for (int k = 0; k < p.size(); k++) {
                    cube(sink, p.x() + i - cx, -1.0, p.z() + k - cz, cx, lift, cz, p.ax(), p.az(), tip, ex, ey, ez, 0x5FA33A);
                    if (lift > 0.5) {
                        cube(sink, p.x() + i - cx, -2.0, p.z() + k - cz, cx, lift, cz, p.ax(), p.az(), tip, ex, ey, ez, 0x8A6642);
                    }
                }
            }
        }
        GuraArt.heave(sink, -ex, -ey, -ez, 0, 1, cone, reach, speed, age, 9);
        double[] heights = new double[90];
        double[] fan = {-0.8, -0.42, 0.0, 0.42, 0.8};
        for (int i = 0; i < fan.length; i++) {
            double an = Math.toRadians(cone * fan[i]);
            double dx = -Math.sin(an);
            double dz = Math.cos(an);
            double length = (reach - 3.0) * (i == 2 ? 0.95 : 0.7 + 0.08 * Math.abs(i - 2));
            GuraArt.rift(sink, dx * 3.0 - ex, -ey, dz * 3.0 - ez, dx, dz, length, com.hexhaki.piece.server.GuraMoves.RIFT_STEP, heights, 0.55,
                age, 30 + i);
        }
        for (int k = 0; k < 4; k++) {
            double d = 8.0 + reach / 4.0 * k;
            GuraArt.quake(sink, -ex, -ey, d - ez, 2.4 * (boost > 1.0 ? 1.3 : 1.0) * 1.4, 0.9, age - d / speed, 40 + k);
        }
        GuraArt.crack(sink, 0.3 - ex, 1.4 - ey, 1.8 - ez, 0, 0, 1, 3.2, age + 3.0, 7);
        GuraArt.shock(sink, 0.3 - ex, 1.4 - ey, 1.0 - ez, 0, 0, 1, reach, 0.7, 1.0, com.hexhaki.piece.server.GuraMoves.SHOCK_SPEED, age + 3.0, 5);
    }

    /** The bent ground's preview stands this high over the scene's own ground (so a sunk column is not hidden under it). */
    static final double LIFT = 10.0;

    /**
     * 3.1.8: the ground the Gura ultimate bends (gura/Heave.bend, as server/GuraUltimate.Bend applies it, a column once the wave has
     * reached it) {@code age} ticks after the strike, the player at the origin looking along +z: grass-topped dirt columns raised
     * {@link #LIFT} over the scene's ground, flat round the bent area.
     */
    static void bent(FireMesh.Sink sink, double ex, double ey, double ez, double age, double boost) {
        double length = Math.min(com.hexhaki.piece.gura.Heave.REACH * boost, com.hexhaki.piece.gura.Heave.BEND_MOST);
        int r = (int) length + 8;
        int[][] h = new int[2 * r + 1][r + 9];
        for (int x = -r; x <= r; x++) {
            for (int z = -8; z <= r; z++) {
                double d = Math.hypot(x + 0.5, z + 0.5);
                h[x + r][z + 8] = d <= age * com.hexhaki.piece.gura.Heave.WAVE
                    ? com.hexhaki.piece.gura.Heave.bend(z + 0.5, -(x + 0.5), x, z, length, com.hexhaki.piece.gura.Heave.CONE, 9) : 0;
            }
        }
        for (int x = -r; x <= r; x++) {
            for (int z = -8; z <= r; z++) {
                int o = h[x + r][z + 8];
                double top = LIFT + o;
                // shaded by height (3.2.2: the shape must read from above): darker the deeper, lighter the higher
                int grass = o == 0 ? 0xFF5FA33A : o > 0 ? 0xFF000000 | FireMesh.mix(0x5FA33A, 0xD8E8A0, Math.min(1.0, o / 4.0))
                    : 0xFF000000 | FireMesh.mix(0x5FA33A, 0x1E3A24, Math.min(1.0, -o / 8.0));
                FireMesh.quad(sink, x - ex, top - ey, z - ez, grass, x + 1 - ex, top - ey, z - ez, grass, x + 1 - ex, top - ey, z + 1 - ez, grass,
                    x - ex, top - ey, z + 1 - ez, grass);
                int[][] nb = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (int[] n : nb) {
                    int nx = x + n[0];
                    int nz = z + n[1];
                    int no = nx < -r || nx > r || nz < -8 || nz > r ? 0 : h[nx + r][nz + 8];
                    if (no >= o) {
                        continue;
                    }
                    double y0 = LIFT + no;
                    int dirt = shade(0x8A6642, n[0] != 0 ? 0.62 : 0.8);
                    double fx = n[0] > 0 ? x + 1 : n[0] < 0 ? x : x;
                    double fz = n[1] > 0 ? z + 1 : n[1] < 0 ? z : z;
                    if (n[0] != 0) {
                        FireMesh.quad(sink, fx - ex, y0 - ey, z - ez, dirt, fx - ex, top - ey, z - ez, dirt, fx - ex, top - ey, z + 1 - ez, dirt, fx - ex,
                            y0 - ey, z + 1 - ez, dirt);
                    } else {
                        FireMesh.quad(sink, x - ex, y0 - ey, fz - ez, dirt, x - ex, top - ey, fz - ez, dirt, x + 1 - ex, top - ey, fz - ez, dirt, x + 1
                            - ex, y0 - ey, fz - ez, dirt);
                    }
                }
            }
        }
        GuraArt.heave(sink, -ex, LIFT - ey, -ez, 0, 1, com.hexhaki.piece.gura.Heave.CONE, com.hexhaki.piece.gura.Heave.REACH * boost,
            com.hexhaki.piece.gura.Heave.WAVE, age, 9);
    }

    /**
     * One block (unit cube from (bx, by, bz) relative to its plate's pivot (px, 0, pz) on top of the ground), lifted {@code lift},
     * turned {@code tip} about the flat axis (ax, az): its top {@code top}, its sides dirt, each face shaded as the game shades them.
     */
    static void cube(FireMesh.Sink sink, double bx, double by, double bz, double px, double lift, double pz, double ax, double az, double tip,
                     double ex, double ey, double ez, int top) {
        double[][] c = new double[8][];
        double cos = Math.cos(tip);
        double sin = Math.sin(tip);
        for (int n = 0; n < 8; n++) {
            double x = bx + (n & 1);
            double y = by + ((n >> 1) & 1);
            double z = bz + ((n >> 2) & 1);
            // Rodrigues about (ax, 0, az)
            double dot = x * ax + z * az;
            double crx = 0 * z - az * y;
            double cry = az * x - ax * z;
            double crz = ax * y - 0 * x;
            double rx = x * cos + crx * sin + ax * dot * (1 - cos);
            double ry = y * cos + cry * sin;
            double rz = z * cos + crz * sin + az * dot * (1 - cos);
            c[n] = new double[] {rx + px - ex, ry + lift - ey, rz + pz - ez};
        }
        int dirt = 0x8A6642;
        face(sink, c, 2, 3, 7, 6, shade(top, 1.0));
        face(sink, c, 0, 1, 5, 4, shade(dirt, 0.5));
        face(sink, c, 0, 2, 6, 4, shade(dirt, 0.6));
        face(sink, c, 1, 3, 7, 5, shade(dirt, 0.6));
        face(sink, c, 0, 1, 3, 2, shade(dirt, 0.8));
        face(sink, c, 4, 5, 7, 6, shade(dirt, 0.8));
    }

    static int shade(int rgb, double f) {
        int r = (int) (((rgb >> 16) & 255) * f);
        int g = (int) (((rgb >> 8) & 255) * f);
        int b = (int) ((rgb & 255) * f);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    static void face(FireMesh.Sink sink, double[][] c, int a, int b, int d, int e, int col) {
        FireMesh.quad(sink, c[a][0], c[a][1], c[a][2], col, c[b][0], c[b][1], c[b][2], col, c[d][0], c[d][1], c[d][2], col, c[e][0], c[e][1],
            c[e][2], col);
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
            case "gwave" -> {
                // 3.1.2: the two walls, left and right of the player (at the origin, looking along +z), rising, then rolling in
                for (int side = -1; side <= 1; side += 2) {
                    GuraArt.tsunami(sink, -side * com.hexhaki.piece.server.GuraMoves.TSUNAMI_SIDE - ex, -ey,
                        com.hexhaki.piece.server.GuraMoves.TSUNAMI_AHEAD - ez, side * com.hexhaki.piece.server.GuraMoves.TSUNAMI_SPEED, 0,
                        com.hexhaki.piece.server.GuraMoves.TSUNAMI_LENGTH, com.hexhaki.piece.server.GuraMoves.TSUNAMI_HEIGHT,
                        com.hexhaki.piece.server.GuraMoves.TSUNAMI_LIFE, com.hexhaki.piece.server.GuraMoves.TSUNAMI_RISE,
                        com.hexhaki.piece.server.GuraMoves.TSUNAMI_RISE, a, 11 + side);
                }
            }
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
            case "gult" -> ultimate(sink, ex, ey, ez, a, move.length > 2 ? Double.parseDouble(move[2]) : 1.0);
            case "gbend" -> bent(sink, ex, ey, ez, a, move.length > 2 ? Double.parseDouble(move[2]) : 1.0);
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
