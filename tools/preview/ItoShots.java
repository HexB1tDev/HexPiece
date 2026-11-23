import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.ito.ItoArt;
import com.hexhaki.piece.server.ItoMoves;

/**
 * 3.3.7: the Ito Ito no Mi's strings (ito/ItoArt) for FxPreview, round the scene's middle (the user standing there facing +z, their
 * right hand at (-0.36, 1.15, 0.35) as client/ito/ItoFx.hand puts it). Shots after the camera, an age, and "c" for Armament on:
 * {@code |islash:age} the lash, {@code |ibullet:age} a burst of bullets, {@code |ihit:age}, {@code |igoshiki:age} onto (0, 0, 7),
 * {@code |iheat:age} Overheat, {@code |iparasite:age} strings to a body at (0, 0, 5), {@code |iweb:age}, {@code |icage:age:radius}, 3.5.0 {@code |ifield:age}, {@code |ispike:age}, {@code |itentacle:age[:tree]}
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
                // 3.5.0: |icage:age:c-or-b:radius:phase:hill the cage cast on a hilltop hill blocks over the ground round it (its strings
                // go on down to the ground: the owner's gaps)
                double hill = move.length > 5 ? Double.parseDouble(move[5]) : 0.0;
                ItoArt.cage(sink, -ex, hill - ey, -ez, r, spread, tension, 1.0, coat);
            }
            case "ifield" -> {
                // 3.5.0: |ifield:age the Awakening's string ground (flat here) with three bodies on it, one spiked (its spike age - 30)
                HieShots.body(sink, -ex, -ey, -ez);
                double[] bodies = {5, 3, -4, 7, 1, -8};
                for (int k = 0; k < 3; k++) {
                    HieShots.body(sink, bodies[k * 2] - ex, -ey, bodies[k * 2 + 1] - ez);
                }
                ItoArt.stringGround(sink, -ex, -ey, -ez, com.hexhaki.piece.ito.ItoAwaken.FIELD_RADIUS, ItoArt.FIELD_RAYS, ItoArt.FIELD_RINGS, null, age,
                    com.hexhaki.piece.ito.ItoAwaken.FIELD_TICKS, coat);
                ItoArt.spike(sink, 5 - ex, -ey, 3 - ez, com.hexhaki.piece.ito.ItoAwaken.SPIKE_HEIGHT, age - 30, coat, 7);
            }
            case "ispike" -> {
                // 3.5.0: |ispike:age one spike under a body, close
                HieShots.body(sink, -ex, -ey + Math.max(0, com.hexhaki.piece.ito.ItoAwaken.spike(age) - 0.8) * 1.2, -ez);
                ItoArt.spike(sink, -ex, -ey, -ez, com.hexhaki.piece.ito.ItoAwaken.SPIKE_HEIGHT, age, coat, 3);
            }
            case "itentacle" -> {
                // 3.5.0: |itentacle:age[:tree] a tentacle from the ground 3.5 off (or a tree's top, 5 up, 6 off) taking a body at (0, 0, 8):
                // grown to it, lifting it 12 up, throwing it on along +z, sinking back (|itentaclefp: the user's own body not drawn, for
                // a first-person camera fp:pitch)
                boolean tree = move.length > 2 && move[2].equals("tree");
                double rx = tree ? 5.0 : 3.5;
                double ry = tree ? 5.5 : 0.0;
                double rz = tree ? 8.0 : 6.0;
                double up = com.hexhaki.piece.ito.ItoAwaken.LIFT_HEIGHT * com.hexhaki.piece.ito.ItoAwaken.lift(age);
                double flung = Math.max(0, age - com.hexhaki.piece.ito.ItoAwaken.RISE - com.hexhaki.piece.ito.ItoAwaken.LIFT);
                double by = up + (flung > 0 ? 0.7 * flung - 0.04 * flung * flung : 0);
                double bz = 8 + 2.2 * flung;
                if (!fp) {
                    HieShots.body(sink, -ex, -ey, -ez);
                }
                if (tree) {
                    for (int k = 0; k < 6; k++) {
                        FireMesh.tube(sink, rx - ex, k - ey, rz - ez, rx - ex, k + 1 - ey, rz - ez, 0.5, 0.5, 0x6B4A2B, 0x6B4A2B, 0x6B4A2B, 0, 1, 0, 0, 1);
                    }
                }
                HieShots.body(sink, -ex, by - ey, bz - ez);
                double mx = 0;
                double my = by + 0.95;
                double mz = bz;
                double rise = com.hexhaki.piece.ito.ItoAwaken.RISE + com.hexhaki.piece.ito.ItoAwaken.LIFT;
                double g = age < rise ? com.hexhaki.piece.ito.ItoAwaken.grow(age) : com.hexhaki.piece.ito.ItoAwaken.withdraw(age);
                double tx0 = age < rise ? mx : 0;
                double ty0 = age < rise ? my : up + 0.95;
                double tz0 = age < rise ? mz : 8;
                ItoArt.tentacle(sink, rx - ex, ry - ey, rz - ez, rx + (tx0 - rx) * g - ex, ry + (ty0 - ry) * g - ey, rz + (tz0 - rz) * g - ez, age,
                    tree, 5);
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
            // 3.5.2 Sky Road reworked (the owner: "swing like spiderman", "climb up like stairs", "jump from 1 string to another"): the
            // user (art/user_skin.png) posed by the clip client/ito/SkyRoad plays, facing +z, turned along its string round the hand as
            // SkyRoad.lean turns it. |iswing:angle[:tick]: on one string to an anchor at (0, 16, 0), the hand on it 10 away at the angle
            // (degrees from straight down, + ahead along +z); |ihang:tick one string straight up; |iclimb:tick steps of string under the
            // feet like stairs; |ileap:tick the split leap, the string let go fading; |iflip:tick the backflip after a swing lets go;
            // |iswingfp:angle one's own first person on a swing (camera fp:pitch)
            case "iswing", "ihang", "iclimb", "ileap", "iflip" -> sky(sink, ex, ey, ez, move, fp, coat);
            // |iarc: one swing seen whole: the body at five points of its arc under one anchor (the string drawn to the last, faint to
            // the others), then flung on in a backflip past the top
            case "iarc" -> {
                double[] angles = {-40, -20, 0, 20, 38};
                for (int k = 0; k < angles.length; k++) {
                    double a = Math.toRadians(angles[k]);
                    double hy = 16.0 - Math.cos(a) * 10.0;
                    double hz = Math.sin(a) * 10.0;
                    posed(sink, "ito_sky_swinging", k * 5.0F, 0.0, hy - com.hexhaki.piece.ito.SkyPhysics.GRIP, hz, -0.42, hy, hz, ex, ey, ez,
                        -angles[k], 0.0);
                    ItoArt.sky(sink, -0.42 - ex, hy - ey, hz - ez, -ex, 16.0 - ey, -ez, k == angles.length - 1 ? 1.0 : 0.3, k == angles.length - 1 ? 0.5 : 0.0,
                        true, coat);
                }
                posed(sink, "ito_sky_backflip", 5.0F, 0.0, 11.5, 10.5, 0.0, 12.4, 10.5, ex, ey, ez, 0.0, 0.0);
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

    /** A Sky Road shot (see the cases above). */
    static void sky(FireMesh.Sink sink, double ex, double ey, double ez, String[] move, boolean fp, boolean coat) {
        String kind = move[0];
        double arg = move.length > 1 ? Double.parseDouble(move[1]) : 0;
        double grip = com.hexhaki.piece.ito.SkyPhysics.GRIP;
        double out = 0.42;
        if (kind.equals("iswing")) {
            double angle = Math.toRadians(arg);
            float tick = move.length > 2 ? Float.parseFloat(move[2]) : 0.0F;
            double ay = 16.0;
            double length = 10.0;
            // the hand on the string; the body's feet before it is turned (the hand over its right shoulder: x is to the left)
            double hy = ay - Math.cos(angle) * length;
            double hz = Math.sin(angle) * length;
            if (fp) {
                // one's own view (the eye at 1.62 over the feet at the origin, looking along +z): the hand low right of the view
                // (SkyRoad.hand), the string up to its anchor ahead and above, 10 away at the angle (behind the hand: - ahead)
                double hx0 = -0.45;
                double hy0 = 1.62 - 0.25;
                double hz0 = 0.4;
                ItoArt.sky(sink, hx0 - ex, hy0 - ey, hz0 - ez, -ex, hy0 + Math.cos(angle) * length - ey, hz0 - Math.sin(angle) * length - ez, 1.0, 0.6,
                    true, coat);
                return;
            }
            double pitch = -arg; // the top turned toward the anchor (SkyRoad.follow: atan2 of the string's way)
            posed(sink, "ito_sky_swinging", tick, 0.0, hy - grip, hz, -out, hy, hz, ex, ey, ez, pitch, 0.0);
            ItoArt.sky(sink, -out - ex, hy - ey, hz - ez, -ex, ay - ey, -ez, 1.0, 0.4, true, coat);
            // its streak: where the feet were along the arc
            double[] feet = new double[24];
            for (int k = 0; k < 8; k++) {
                double a = angle - Math.toRadians(5.0 * k) * Math.signum(arg == 0 ? 1 : arg);
                feet[k * 3] = 0.0;
                feet[k * 3 + 1] = ay - Math.cos(a) * (length + grip);
                feet[k * 3 + 2] = Math.sin(a) * (length + grip);
            }
            ItoArt.streak(sink, feet, 8, ex, ey, ez, 1.0, coat);
        } else if (kind.equals("ihang")) {
            posed(sink, "ito_sky_hang", (float) arg, 0.0, 0.0, 0.0, 0, 0.9, 0, ex, ey, ez, 0, 0);
            ItoArt.sky(sink, -out - ex, grip + 0.05 - ey, -ez, -out - ex, grip + 8.0 - ey, -ez, 1.0, 0.3, true, coat);
        } else if (kind.equals("iclimb")) {
            // climbing along +z high in the air: a step every 5 ticks, each 1 up and 0.55 along; the feet now at (0, 6, 0)
            float tick = (float) arg;
            posed(sink, "ito_sky_climb", tick % 10.0F, 0.0, 6.0, 0.0, 0, 6.9, 0, ex, ey, ez, 8.0, 0);
            double into = tick % 5.0;
            for (int k = 0; k < 4; k++) {
                double age = into + 5.0 * k;
                double y = 6.0 - k * com.hexhaki.piece.ito.SkyPhysics.STEP_RISE - 0.02;
                double z = -k * com.hexhaki.piece.ito.SkyPhysics.STEP_ALONG;
                ItoArt.tread(sink, -ex, y - ey, z - ez, 0.0, age, coat);
            }
        } else if (kind.equals("ileap") || kind.equals("iflip")) {
            String clip = kind.equals("ileap") ? "ito_sky_leap" : "ito_sky_backflip";
            posed(sink, clip, (float) arg, 0.0, 0.0, 0.0, 0, 0.9, 0, ex, ey, ez, 14.0, 0);
            // the string just let go: slack and fading where it held, behind and up
            double f = Math.min(1.0, arg / 6.0);
            ItoArt.sky(sink, -out - ex, grip + 1.0 + 4.0 * f - ey, -3.0 * f - ez, -ex, 11.0 - ey, -6.0 - ez, 0.45 * (1.0 - f), 0.0, false, coat);
        }
    }

    /**
     * The user posed by {@code clip} at {@code tick} with its feet at (fx, fy, fz) facing +z (x is its left: the hand over the right
     * shoulder is at -x), turned by {@code pitch} (+ the top forward) and {@code roll} round the pivot (px, py, pz), as SkyRoad.lean turns
     * it: v' = X(pitch) Z(roll) v about the pivot.
     */
    static void posed(FireMesh.Sink sink, String clip, float tick, double fx, double fy, double fz, double px, double py, double pz, double ex,
                      double ey, double ez, double pitch, double roll) {
        float[][] pose = com.hexhaki.piece.client.npc.StylePoser.sample(clip, tick);
        com.hexhaki.piece.horo.AstralArt.Skin skin = NikaShots.skin(sink);
        double p = Math.toRadians(pitch);
        double r = Math.toRadians(roll);
        double cp = Math.cos(p);
        double sp = Math.sin(p);
        double cr = Math.cos(r);
        double sr = Math.sin(r);
        double pivx = px - ex;
        double pivy = py - ey;
        double pivz = pz - ez;
        com.hexhaki.piece.horo.AstralArt.Skin turned = (xyz, uv, argb, nx, ny, nz) -> {
            double[] q = new double[12];
            for (int i = 0; i < 4; i++) {
                double[] v = turn(xyz[i * 3] - pivx, xyz[i * 3 + 1] - pivy, xyz[i * 3 + 2] - pivz, cp, sp, cr, sr);
                q[i * 3] = v[0] + pivx;
                q[i * 3 + 1] = v[1] + pivy;
                q[i * 3 + 2] = v[2] + pivz;
            }
            double[] n = turn(nx, ny, nz, cp, sp, cr, sr);
            skin.quad(q, uv, argb, n[0], n[1], n[2]);
        };
        com.hexhaki.piece.horo.AstralArt.left(turned, pose, new com.hexhaki.piece.style.StyleShow.Place(fx - ex, fy - ey, fz - ez, 0.0));
    }

    /** Z(roll) then X(pitch) (the game's: the top toward -x, the body's right, for + roll; toward +z for + pitch). */
    static double[] turn(double x, double y, double z, double cp, double sp, double cr, double sr) {
        double x1 = x * cr - y * sr;
        double y1 = x * sr + y * cr;
        double y2 = y1 * cp - z * sp;
        double z2 = y1 * sp + z * cp;
        return new double[] {x1, y2, z2};
    }
}
