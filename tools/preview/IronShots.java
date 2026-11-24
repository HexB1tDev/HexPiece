import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gura.GuraArt;
import com.hexhaki.piece.ironfist.IronArt;
import com.hexhaki.piece.ironfist.IronMove;

/**
 * 3.5.8: Iron Fist's previews (ironfist/IronArt and gura/GuraArt, the very quads client/ironfist/IronFx draws), in FxPreview:
 * {@code |iball:age} the cannonball flying, {@code |iboom:age[:c]} its blast, {@code |icrush:age} Mountain Crusher's shock,
 * {@code |ihole:age} Blue Hole's slam, {@code |idivide:age} Galaxy Divide's shock, {@code |iinf:age} the barrage, {@code |iglow:power}
 * Galaxy Impact's fist, {@code |idome:age} its blast (age from the force landing).
 */
public final class IronShots {
    private IronShots() {
    }

    static boolean shot(FireMesh.Sink sink, double[] eye, String[] move) {
        double ex = eye[0];
        double ey = eye[1];
        double ez = eye[2];
        double age = move.length > 1 ? Double.parseDouble(move[1]) : 4;
        boolean c = move.length > 2 && move[2].equals("c");
        switch (move[0]) {
            case "iball" -> {
                user(sink, "style_brawler_meteor_throw", 6, ex, ey, ez);
                double speed = IronMove.meteorSpeed(1.0);
                double r = IronMove.meteorRadius(1.0);
                IronArt.cannonball(sink, -ex, 1.8 - ey, 2.0 + age * speed - ez, r, age * 0.9, age, 3);
                IronArt.trail(sink, -ex, 1.8 - ey, 2.0 + age * speed - ez, 0, 0, 1, speed, r, age, 3);
            }
            case "iboom" -> {
                double radius = IronMove.meteorBlast(c ? 1.0 : 0.4);
                GuraArt.quake(sink, -ex, -ey, -ez, radius * 1.3, 0.8, age, 5);
                IronArt.pressure(sink, -ex, 0.5 - ey, -ez, 0, 1, 0, radius * 1.8, age, 10.0);
                IronArt.pressure(sink, -ex, 0.5 - ey, -ez, 0, 0, 1, radius * 1.2, age - 1.0, 8.0);
                if (age < 6.0) {
                    FireMesh.shell(sink, -ex, 0.5 - ey, -ez, radius * (0.5 + age * 0.15), IronArt.WHITE, 0.8 * (1.0 - age / 6.0), 0.3 * (1.0 - age / 6.0));
                }
                debris(sink, -ex, 0.2 - ey, -ez, radius * 1.6, 0.8, 16, age, 7);
            }
            case "icrush" -> {
                user(sink, "style_brawler_crusher", 13 + age, ex, ey, ez);
                HieShots.body(sink, -ex, -ey, 2.5 - ez);
                double ux = 0.0;
                double uy = 0.75;
                double uz = 0.66;
                GuraArt.shock(sink, -ex, 1.5 - ey, 1.1 - ez, ux, uy, uz, 11.0, 0.42, 1.0, 3.2, age, 9);
                IronArt.impactLines(sink, -ex, 1.5 - ey, 1.1 - ez, ux, uy, uz, 4.5, age, 9.0, 9);
                IronArt.pressure(sink, -ex, 1.5 - ey, 1.1 - ez, ux, uy, uz, 2.4, age, 8.0);
            }
            case "ihole" -> {
                user(sink, "style_brawler_blue_hole", 30, ex, ey, ez);
                GuraArt.quake(sink, -ex, -ey, 1.6 - ez, 10.0, 1.0, age, 11);
                double u = Math.min(1.0, age / 16.0);
                IronArt.groundRing(sink, -ex, -ey, 1.6 - ez, 1.0 + 15.0 * (1.0 - (1.0 - u) * (1.0 - u)), 0.8 * (1.0 - u));
                IronArt.dustWall(sink, -ex, -ey, 1.6 - ez, 1.2 + 3.0 * u, 2.6, 0.7 * (1.0 - age / GuraArt.QUAKE_LIFE), age, 11);
                debris(sink, -ex, -ey, 1.6 - ez, 7.0, 0.8, 16, age, 12);
            }
            case "idivide" -> {
                double front = Math.min(IronMove.DIVIDE_LENGTH, age * IronMove.DIVIDE_SPEED);
                IronArt.shockSheet(sink, -ex, -0.5 - ey, 3.0 + front - ez, 0, 1, IronMove.DIVIDE_HEIGHT, IronMove.DIVIDE_WIDTH, 1.0, age, 13);
                FireMesh.palette(FireMesh.SMOKE);
                for (double s = 2.0; s < front; s += 4.0) {
                    double since = age - s / IronMove.DIVIDE_SPEED;
                    FireMesh.puff(sink, -ex, 1.0 + since * 0.05 - ey, 3.0 + s - ez, 2.2 + since * 0.03, 0, 0.1, 0, 1.1, 0.25, 0.7, 0.7, false,
                        0.5 * Math.max(0.0, 1.0 - since / 50.0), age, 13 + (int) s);
                }
                FireMesh.palette(FireMesh.BAND);
            }
            case "iinf" -> {
                user(sink, "style_brawler_infinitum", age, ex, ey, ez);
                double speed = Math.max(0.0, Math.min(1.0, (age - IronMove.INF_STANCE) / (IronMove.INF_END - IronMove.INF_STANCE)));
                IronArt.afterimages(sink, -ex, 1.3 - ey, -ez, 0, 1, speed, age, 15);
                for (int k = 0; k < 3; k++) {
                    double a = (age + k * 2.0) % 6.0;
                    IronArt.pressure(sink, (k - 1) * 0.5 - ex, 1.3 - ey, 2.2 - ez, 0, 0, 1, 1.5, a, 6.0);
                    if (a < 3.0) {
                        FireMesh.shell(sink, (k - 1) * 0.5 - ex, 1.3 - ey, 2.2 - ez, 0.25 + a * 0.12, IronArt.GOLD, 0.45 * (1.0 - a / 3.0), 0.5 * (1.0 - a / 3.0));
                    }
                }
                GuraArt.shock(sink, -ex, 1.3 - ey, 2.0 - ez, 0, 0, 1, 5.0, 0.3, 0.5, 2.5, age % 4.0, 16);
            }
            case "iglow" -> {
                user(sink, "style_brawler_impact", 80, ex, ey, ez);
                double power = age;
                IronArt.fistGlow(sink, -0.45 - ex, 2.0 - ey, -0.2 - ez, power, 3.0, 17);
                IronArt.gather(sink, -0.45 - ex, 2.0 - ey, -0.2 - ez, 3.0 + 6.0 * power, Math.min(1.0, 0.4 + power), 3.0, 17);
            }
            case "idome" -> {
                double b = age - 4.0;
                if (age < 8.0) {
                    IronArt.point(sink, -ex, 0.5 - ey, -ez, 1.2 + age * 0.3, 1.0 - Math.max(0.0, age - 4.0) / 4.0, age, 19);
                }
                GuraArt.quake(sink, -ex, -ey, -ez, IronMove.IMPACT_DOME, 1.0, age * 0.8, 19);
                if (b >= 0.0) {
                    double u = Math.min(1.0, b / IronMove.IMPACT_DOME_TICKS);
                    double grow = 1.0 - (1.0 - u) * (1.0 - u);
                    IronArt.dome(sink, -ex, -ey, -ez, IronMove.IMPACT_DOME * grow, 1.0, age, 19);
                    double[] reach = {IronMove.IMPACT_DOME * 1.05, 90.0, 140.0};
                    double[] ticks = {IronMove.IMPACT_DOME_TICKS, 26.0, 22.0};
                    for (int k = 0; k < 3; k++) {
                        double v = b / ticks[k];
                        if (v < 1.0) {
                            IronArt.groundRing(sink, -ex, -ey, -ez, reach[k] * (1.0 - (1.0 - v) * (1.0 - v)), 0.8 * (1.0 - v));
                        }
                    }
                    IronArt.dustWall(sink, -ex, -ey, -ez, IronMove.IMPACT_DOME * grow * 0.96, 6.0 + 8.0 * grow, Math.min(0.75, b / 6.0), age, 19);
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    static final double[] T = new double[4];

    static void debris(FireMesh.Sink sink, double x, double y, double z, double reach, double lift, int count, double age, int seed) {
        for (int k = 0; k < count; k++) {
            IronArt.thrown(k, seed, reach, lift, age, T);
            if (T[1] < -3.0) {
                continue;
            }
            IronArt.chunk(sink, x + T[0], y + T[1], z + T[2], 0.3 + 0.55 * FireMesh.rand(seed, k + 240), T[3], seed + k, 1.0);
        }
    }

    static void user(FireMesh.Sink sink, String clip, double tick, double ex, double ey, double ez) {
        ItoShots.posed(sink, clip, (float) Math.max(0.0, tick), 0.0, 0.0, 0.0, 0.0, 0.9, 0.0, ex, ey, ez, 0.0, 0.0);
    }
}
