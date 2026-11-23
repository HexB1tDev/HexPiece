import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.supa.SupaArt;

/**
 * 3.3.5: the Supa Supa no Mi's steel (supa/SupaArt) for FxPreview. A stand-in player built as the game builds one (its feet at the
 * scene's middle facing +z, every limb at the player model's own pivot, turned Z then Y then X as ModelPart.translateAndRotate turns it,
 * model y down), so the blades drawn in a limb's frame sit where client/supa/SupaClient puts them. Shots after the camera:
 * {@code |sblade:age[:coat]} both arm blades (the right arm forward, the left hanging), {@code |sclaw:age[:coat]} the finger blades and a
 * swipe's marks, {@code |sdrill:age} both drills (arms forward), {@code |sskate:age} the foot blades and skate marks,
 * {@code |sspider:age[:coat]} the steel body, {@code |sslash:age:scale[:coat]} a flying slash 3 blocks ahead, {@code |scross:age} the X,
 * {@code |sspark:age}, {@code |sglance:age}, {@code |strail:age} Spiral Hollow's wake, {@code |sdaisy:age} Sparkling Daisy's cut,
 * {@code |sspin:age[:coat]} Spar Break.
 */
public final class SupaShots {
    private SupaShots() {
    }

    /** A sink drawing a limb's frame into the scene (eye-centred). */
    static final class Framed implements FireMesh.Sink {
        FireMesh.Sink out;
        final double[] m = new double[12];
        final double[] q = new double[12];

        @Override
        public void quad(double[] xyz, int[] argb) {
            for (int k = 0; k < 4; k++) {
                double x = xyz[k * 3];
                double y = xyz[k * 3 + 1];
                double z = xyz[k * 3 + 2];
                q[k * 3] = m[0] * x + m[1] * y + m[2] * z + m[3];
                q[k * 3 + 1] = m[4] * x + m[5] * y + m[6] * z + m[7];
                q[k * 3 + 2] = m[8] * x + m[9] * y + m[10] * z + m[11];
            }
            out.quad(q, argb);
        }

        /**
         * The frame of a part pivoted at (px, py, pz) model pixels and turned xRot, yRot, zRot degrees, for a player standing at the
         * scene's middle facing +z (the renderer: model (x, y, z) -> world (x, 1.501 - y, -z)), seen from {@code eye}; {@code mirror}
         * flips the part's x (the left arm's blade drawn from the right's).
         */
        Framed at(double[] eye, double px, double py, double pz, double xRot, double yRot, double zRot, boolean mirror) {
            double cx = Math.cos(Math.toRadians(xRot));
            double sx = Math.sin(Math.toRadians(xRot));
            double cy = Math.cos(Math.toRadians(yRot));
            double sy = Math.sin(Math.toRadians(yRot));
            double cz = Math.cos(Math.toRadians(zRot));
            double sz = Math.sin(Math.toRadians(zRot));
            // R = Rz * Ry * Rx
            double[] rx = {1, 0, 0, 0, cx, -sx, 0, sx, cx};
            double[] ry = {cy, 0, sy, 0, 1, 0, -sy, 0, cy};
            double[] rz = {cz, -sz, 0, sz, cz, 0, 0, 0, 1};
            double[] r = mul(rz, mul(ry, rx));
            double f = mirror ? -1 : 1;
            // world = S * (R * (f x, y, z) + t) + (0, 1.501, 0) - eye, S = diag(1, -1, -1)
            double[] s = {1, -1, -1};
            double[] t = {px / 16.0, py / 16.0, pz / 16.0};
            for (int row = 0; row < 3; row++) {
                m[row * 4] = s[row] * r[row * 3] * f;
                m[row * 4 + 1] = s[row] * r[row * 3 + 1];
                m[row * 4 + 2] = s[row] * r[row * 3 + 2];
                m[row * 4 + 3] = s[row] * t[row] - eye[row];
            }
            m[7] += 1.501;
            return this;
        }

        static double[] mul(double[] a, double[] b) {
            double[] c = new double[9];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    c[i * 3 + j] = a[i * 3] * b[j] + a[i * 3 + 1] * b[3 + j] + a[i * 3 + 2] * b[6 + j];
                }
            }
            return c;
        }
    }

    static final Framed FRAME = new Framed();

    /** A limb's box (model pixels) in the frame. */
    static void part(Framed f, double x0, double y0, double z0, double x1, double y1, double z1, int rgb) {
        HieShots.box(f, x0 / 16, y0 / 16, z0 / 16, x1 / 16, y1 / 16, z1 / 16, rgb);
    }

    /** The stand-in player, its arms and legs turned (degrees, xRot then zRot each); its parts' frames kept for the blades. */
    static void player(FireMesh.Sink sink, double[] eye, double rArmX, double rArmZ, double lArmX, double lArmZ, double rLegX, double lLegX) {
        FRAME.out = sink;
        part(FRAME.at(eye, 0, 0, 0, 0, 0, 0, false), -4, -8, -4, 4, 0, 4, 0xD9A77E);
        part(FRAME, -3, -5, -4.3, -1, -4, -4, 0x202020); // the eyes: the face is the model's -z side (the world's +z)
        part(FRAME, 1, -5, -4.3, 3, -4, -4, 0x202020);
        part(FRAME.at(eye, 0, 0, 0, 0, 0, 0, false), -4, 0, -2, 4, 12, 2, 0x3C6FB4);
        part(FRAME.at(eye, -5, 2, 0, rArmX, 0, rArmZ, false), -3, -2, -2, 1, 10, 2, 0xD9A77E);
        part(FRAME.at(eye, 5, 2, 0, lArmX, 0, lArmZ, false), -1, -2, -2, 3, 10, 2, 0xD9A77E);
        part(FRAME.at(eye, -1.9, 12, 0, rLegX, 0, 0, false), -2, 0, -2, 2, 12, 2, 0x34407A);
        part(FRAME.at(eye, 1.9, 12, 0, lLegX, 0, 0, false), -2, 0, -2, 2, 12, 2, 0x34407A);
    }

    static boolean shot(FireMesh.Sink sink, double[] eye, String[] move) {
        double ex = eye[0];
        double ey = eye[1];
        double ez = eye[2];
        double age = move.length > 1 ? Double.parseDouble(move[1]) : 4;
        double coat = 0;
        switch (move[0]) {
            case "sblade" -> {
                coat = move.length > 2 ? Double.parseDouble(move[2]) : 0;
                player(sink, eye, -90, 0, -10, -8, 0, 0);
                SupaArt.armBlade(FRAME.at(eye, -5, 2, 0, -90, 0, 0, false), SupaArt.BLADE_LENGTH, coat, (age * 0.05) % 1.0);
                SupaArt.armBlade(FRAME.at(eye, 5, 2, 0, -10, 0, -8, true), SupaArt.BLADE_LENGTH, coat, (age * 0.05) % 1.0);
            }
            case "sclaw" -> {
                coat = move.length > 2 ? Double.parseDouble(move[2]) : 0;
                player(sink, eye, -80, 25, 0, -5, 0, 0);
                SupaArt.claws(FRAME.at(eye, -5, 2, 0, -80, 0, 25, false), coat, Math.min(1.0, age / 2.0));
                SupaArt.clawMarks(sink, -ex, 1.25 - ey, -ez, 0, 1, 0, 1.5, age, coat);
            }
            case "sdrill" -> {
                player(sink, eye, -90, 0, -90, 0, 0, 0);
                SupaArt.drill(FRAME.at(eye, -5, 2, 0, -90, 0, 0, false), 0, age * 0.9);
                SupaArt.drill(FRAME.at(eye, 5, 2, 0, -90, 0, 0, true), 0, -age * 0.9);
            }
            case "sskate" -> {
                player(sink, eye, -20, 10, 20, -10, -25, 20);
                SupaArt.footBlade(FRAME.at(eye, -1.9, 12, 0, -25, 0, 0, false), 0);
                SupaArt.footBlade(FRAME.at(eye, 1.9, 12, 0, 20, 0, 0, false), 0);
                for (int i = 0; i < 12; i++) {
                    double a = 1.0 - i / 12.0;
                    SupaArt.mark(sink, -0.12 - ex, 0.01 - ey, -i * 0.5 - ez, -0.12 - ex, 0.01 - ey, -(i + 1) * 0.5 - ez, a);
                    SupaArt.mark(sink, 0.12 - ex, 0.01 - ey, -i * 0.5 - ez, 0.12 - ex, 0.01 - ey, -(i + 1) * 0.5 - ez, a);
                }
            }
            case "sspider" -> {
                coat = move.length > 2 ? Double.parseDouble(move[2]) : 0;
                player(sink, eye, -15, 20, -15, -20, 0, 0);
                double sh = (age * 0.04) % 1.0;
                double al = 0.85;
                SupaArt.steelBox(FRAME.at(eye, 0, 0, 0, 0, 0, 0, false), -0.25, -0.5, -0.25, 0.25, 0, 0.25, coat, sh, al);
                SupaArt.steelBox(FRAME.at(eye, 0, 0, 0, 0, 0, 0, false), -0.25, 0, -0.125, 0.25, 0.75, 0.125, coat, sh, al);
                SupaArt.steelBox(FRAME.at(eye, -5, 2, 0, -15, 0, 20, false), -0.1875, -0.125, -0.125, 0.0625, 0.625, 0.125, coat, sh, al);
                SupaArt.steelBox(FRAME.at(eye, 5, 2, 0, -15, 0, -20, false), -0.0625, -0.125, -0.125, 0.1875, 0.625, 0.125, coat, sh, al);
                SupaArt.steelBox(FRAME.at(eye, -1.9, 12, 0, 0, 0, 0, false), -0.125, 0, -0.125, 0.125, 0.75, 0.125, coat, sh, al);
                SupaArt.steelBox(FRAME.at(eye, 1.9, 12, 0, 0, 0, 0, false), -0.125, 0, -0.125, 0.125, 0.75, 0.125, coat, sh, al);
            }
            case "sslash" -> {
                double scale = move.length > 2 ? Double.parseDouble(move[2]) : 1.0;
                coat = move.length > 3 ? Double.parseDouble(move[3]) : 0;
                player(sink, eye, -100, 0, 0, 0, 0, 0);
                SupaArt.slash(sink, -ex, 1.3 - ey, 3.0 + age * 0.5 - ez, 0, 0, 1, 0.0, scale, age, coat);
            }
            case "scross" -> {
                player(sink, eye, -60, 40, -60, -40, 0, 0);
                SupaArt.cross(sink, -ex, 1.3 - ey, 2.5 + age * 0.4 - ez, 0, 0, 1, 1.6, age, 0);
            }
            case "sspark" -> {
                player(sink, eye, 0, 0, 0, 0, 0, 0);
                SupaArt.sparks(sink, -ex, 1.1 - ey, -0.3 - ez, 0, 0, -1, 1.0, age, 7);
            }
            case "sglance" -> {
                player(sink, eye, 0, 0, 0, 0, 0, 0);
                SupaArt.glance(sink, -ex, 1.1 - ey, -0.3 - ez, 0, -1, age, 9);
            }
            case "strail" -> {
                player(sink, eye, -90, 0, -90, 0, 0, 0);
                SupaArt.drillTrail(sink, -ex, 1.2 - ey, -10 - ez, -ex, 1.2 - ey, 0.5 - ez, age, 0);
            }
            case "sdaisy" -> {
                player(sink, eye, -90, 0, -90, 0, 0, 0);
                SupaArt.daisyTrail(sink, -ex, 1.2 - ey, -12 - ez, -ex, 1.2 - ey, 0.3 - ez, age, 0);
            }
            case "sspin" -> {
                coat = move.length > 2 ? Double.parseDouble(move[2]) : 0;
                player(sink, eye, 0, 80, 0, -80, 0, 0);
                SupaArt.spin(sink, -ex, 1.1 - ey, -ez, 3.4, age, coat);
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
