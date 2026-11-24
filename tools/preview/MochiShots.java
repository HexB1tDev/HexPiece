import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.mochi.MochiArt;

/**
 * 3.5.5: the Mochi Mochi no Mi's previews (mochi/MochiArt, the very quads client/mochi/MochiFx draws), in FxPreview:
 * {@code |mparts:coat} the pieces side by side (fists, a ball, a drill, a ring, a box).
 */
public final class MochiShots {
    private MochiShots() {
    }

    /** Targets (5 a punch: ring, x, y, z, age) made eye-relative. */
    static double[] toEye(double[] p, double ex, double ey, double ez) {
        double[] out = p.clone();
        for (int k = 0; k < out.length / 5; k++) {
            out[k * 5 + 1] -= ex;
            out[k * 5 + 2] -= ey;
            out[k * 5 + 3] -= ez;
        }
        return out;
    }

    /** The user, posed in {@code clip} {@code tick} ticks in, standing at the origin facing +z (ItoShots.posed: the game's own skin). */
    static void user(FireMesh.Sink sink, String clip, double tick, double ex, double ey, double ez) {
        ItoShots.posed(sink, clip, (float) Math.max(0.0, tick), 0.0, 0.0, 0.0, 0.0, 0.9, 0.0, ex, ey, ez, 0.0, 0.0);
    }

    /** 3.6.3: the user with the limbs in {@code hide} turned into mochi (not drawn: AstralArt.hide, as MochiFx.mochiArms in the game). */
    static void user(FireMesh.Sink sink, String clip, double tick, double ex, double ey, double ez, int hide) {
        com.hexhaki.piece.horo.AstralArt.hide = hide;
        user(sink, clip, tick, ex, ey, ez);
        com.hexhaki.piece.horo.AstralArt.hide = 0;
    }

    /**
     * 3.6.4: the Lotus combo's body's tumble at {@code age} as pitch and roll (degrees; client/mochi/MochiFx.lotusTumble turns it about
     * each blow's own axis: here summed into the two, near enough for a preview).
     */
    static double[] lotusTurn(double age) {
        double pitch = 0;
        double roll = 0;
        if (age <= com.hexhaki.piece.mochi.MochiDraw.LOTUS_LAUNCH || age >= com.hexhaki.piece.mochi.MochiDraw.LOTUS_GROUND) {
            return new double[] {0, 0};
        }
        double up = Math.min(1.0, (age - com.hexhaki.piece.mochi.MochiDraw.LOTUS_LAUNCH) / com.hexhaki.piece.mochi.MochiDraw.LOTUS_RISE);
        up = 1.0 - (1.0 - up) * (1.0 - up);
        pitch -= Math.toDegrees(1.3 * up);
        int[] air = com.hexhaki.piece.mochi.MochiDraw.LOTUS_AIR;
        double[] dn = new double[6];
        for (int i = 0; i < air.length && age > air[i]; i++) {
            com.hexhaki.piece.mochi.MochiDraw.lotusDonut(i, 0, 1, dn);
            double ax = dn[5];
            double az = -dn[3];
            double al = Math.max(1.0E-6, Math.hypot(ax, az));
            double u = Math.min(1.0, (age - air[i]) / (com.hexhaki.piece.mochi.MochiDraw.LOTUS_CARRY + 2.0));
            double spin = Math.toDegrees(3.4 * (1.0 - (1.0 - u) * (1.0 - u)));
            pitch += spin * ax / al;
            roll += spin * az / al;
        }
        return new double[] {pitch, roll};
    }

    static boolean shot(FireMesh.Sink sink, double[] eye, String[] move) {
        double ex = eye[0];
        double ey = eye[1];
        double ez = eye[2];
        double age = move.length > 1 ? Double.parseDouble(move[1]) : 4;
        boolean coat = move.length > 2 && move[2].equals("c");
        switch (move[0]) {
            case "mparts" -> {
                MochiArt.fist(sink, -3 - ex, 1.5 - ey, -ez, 0, 0, 1, 1, 0, 0, 0.8, false, age, 1);
                MochiArt.fist(sink, -1 - ex, 1.5 - ey, -ez, 0, 0, 1, 1, 0, 0, 0.8, true, age, 2);
                MochiArt.ball(sink, 1.2 - ex, 1.5 - ey, -ez, 0.8, 0, 1, 0, 1.0, 0.12, age, 3, MochiArt.MOCHI);
                MochiArt.tubeBegin();
                for (int k = 0; k <= 10; k++) {
                    double t = k / 10.0;
                    MochiArt.tubePoint(3.2 - ex, 0.6 + 2.0 * t - ey, -ez, 0.5 * (1.0 - t) + 0.02);
                }
                MochiArt.tube(sink, 12, MochiArt.MOCHI, 2.5, age * 0.3);
                MochiArt.torus(sink, -2 - ex, 3.6 - ey, -ez, 0, 0, 1, 0.8, 0.32, 1.0, MochiArt.MOCHI);
                MochiArt.blob(sink, 1.0 - ex, 3.6 - ey, -ez, 1, 0, 0, 0, 1, 0, 0.8, 0.6, 0.5, 0.35, 0.0, age, 5, coat ? MochiArt.HAKI : MochiArt.MOCHI, 1.0);
            }
            case "mdonut" -> {
                // 3.6.3: the fruit's donut: lumpy, dripping, its fist filling its hole (one punching), at three sizes
                com.hexhaki.piece.mochi.MochiDraw.donut(sink, -2.6 - ex, 2.2 - ey, -ez, 0, 0, -1, 1.0, 0.0, 3, 0.0, 0, 0, 0, coat, age, 1, false);
                com.hexhaki.piece.mochi.MochiDraw.donut(sink, 0.2 - ex, 2.4 - ey, -ez, 0.3, 0.1, -0.95, 1.4, 0.0, 3, 1.0, 2.5 - ex, 2.0 - ey,
                    -6.5 - ez, coat, age, 2, false);
                com.hexhaki.piece.mochi.MochiDraw.donut(sink, 2.8 - ex, 1.6 - ey, -ez, -0.2, 0, -0.98, 0.7, 0.0, 2, 0.0, 0, 0, 0, coat, age, 3, true);
            }
            case "mmuddle" -> {
                // 3.6.3 |mmuddle:age: Mochi Muddle from the press (the reference's beats: MochiDraw.MUDDLE_*), the user at the origin
                // facing +z, a body 6 ahead stuck once the puddle reaches it
                user(sink, "mochi_muddle", Math.min(age, 60), ex, ey, ez);
                double a = age - com.hexhaki.piece.mochi.MochiDraw.MUDDLE_START;
                HieShots.body(sink, 0.4 - ex, -ey, 6.0 - ez);
                com.hexhaki.piece.mochi.MochiDraw.muddle(sink, -ex, -ey, -ez, 0, 1, a, 3);
                double reached = com.hexhaki.piece.mochi.MochiDraw.muddleReach(a);
                if (reached >= 6.0) {
                    double since = a - (com.hexhaki.piece.mochi.MochiDraw.MUDDLE_RUN + 6.0 / com.hexhaki.piece.mochi.MochiDraw.MUDDLE_SPEED);
                    com.hexhaki.piece.mochi.MochiDraw.stuck(sink, 0.4 - ex, -ey, 6.0 - ez, 1.8, Math.min(1.0, since / 3.0), since, 4);
                }
            }
            case "mlotus" -> {
                // 3.6.3 |mlotus:age[:c]: the whole combo as the reference times it (MochiDraw.LOTUS_*): the user at the origin facing +z,
                // the one hit 7 ahead, thrown up at LOTUS_LAUNCH; 3.6.4: knocked from donut to donut in the air, tumbling as client/mochi/
                // MochiFx.lotusTumble turns it (here as pitch and roll), driven down by the giant fist into the ground
                user(sink, "mochi_lotus", Math.min(age, 46), ex, ey, ez);
                double[] d = com.hexhaki.piece.mochi.MochiDraw.lotusAt(0, 1, new double[3]);
                double[] b = com.hexhaki.piece.mochi.MochiDraw.lotusBody(age, 0, 1, new double[3]);
                double[] turn = lotusTurn(age);
                ItoShots.posed(sink, "mochi_punch_1", 0.0F, b[0], b[1], b[2] + 7.0, b[0], b[1] + 0.9, b[2] + 7.0, ex, ey, ez, turn[0], turn[1]);
                com.hexhaki.piece.mochi.MochiDraw.lotusRise(sink, b[0] - ex, b[1] - ey, b[2] + 7.0 - ez, -ey,
                    age - com.hexhaki.piece.mochi.MochiDraw.LOTUS_LAUNCH, 41);
                com.hexhaki.piece.mochi.MochiDraw.lotus(sink, 0.4 - ex, 1.3 - ey, 0.3 - ez, d[0] - ex, d[1] - ey, d[2] - ez, 0, 1, -ex, 1.0 - ey,
                    7.0 - ez, -ey, age, coat, 5);
                int[] air = com.hexhaki.piece.mochi.MochiDraw.LOTUS_AIR;
                double[] dn = new double[6];
                for (int k = 0; k < air.length; k++) {
                    com.hexhaki.piece.mochi.MochiDraw.lotusDonut(k, 0, 1, dn);
                    com.hexhaki.piece.mochi.MochiDraw.lotusChain(sink, dn[0] - ex, dn[1] - ey, dn[2] + 7.0 - ez, dn[3], dn[4], dn[5], b[0] - ex,
                        b[1] + 1.0 - ey, b[2] + 7.0 - ez, age - air[k], coat, 11 + 13 * k);
                }
                double[] last = com.hexhaki.piece.mochi.MochiDraw.lotusBody(com.hexhaki.piece.mochi.MochiDraw.LOTUS_FALL, 0, 1, new double[3]);
                com.hexhaki.piece.mochi.MochiDraw.lotusSlam(sink, last[0] - ex, last[1] + 7.0 - ey, last[2] + 7.0 - ez, b[0] - ex, b[1] - ey,
                    b[2] + 7.0 - ez, -ey, age - com.hexhaki.piece.mochi.MochiDraw.LOTUS_DROP, coat, 21);
                com.hexhaki.piece.mochi.MochiDraw.lotusGround(sink, last[0] - ex, -ey, last[2] + 7.0 - ez,
                    age - com.hexhaki.piece.mochi.MochiDraw.LOTUS_GROUND, 31);
            }
            case "mchestnut" -> {
                // 3.6.3 |mchestnut:age[:c]: the whole move on its beats (MochiDraw.CHESTNUT_*), the user at the origin facing +z, a body 8
                // ahead (1 to the side) struck every 4 ticks while it fires; past the firing its fall and splat
                user(sink, "mochi_chestnut", Math.min(age, 22), ex, ey, ez);
                double[] at = com.hexhaki.piece.mochi.MochiDraw.chestnutOffset(0.0, 1.8, new double[3]);
                HieShots.body(sink, 1.0 - ex, -ey, 8.0 - ez);
                double fire = com.hexhaki.piece.mochi.MochiDraw.CHESTNUT_FORM + com.hexhaki.piece.mochi.MochiDraw.CHESTNUT_FIRE;
                double ended = age > fire ? age - fire : -1.0;
                double fired = (age - com.hexhaki.piece.mochi.MochiDraw.CHESTNUT_FORM) % 4.0;
                com.hexhaki.piece.mochi.MochiDraw.chestnut(sink, at[0] - ex, at[1] - ey, at[2] - ez, -0.4 - ex, 1.4 - ey, 0.2 - ez, 0, 1, -ey, age,
                    ended, fired, coat, 7);
                if (age > com.hexhaki.piece.mochi.MochiDraw.CHESTNUT_FORM && ended < 0) {
                    com.hexhaki.piece.mochi.MochiDraw.spike(sink, at[0] - ex, at[1] - ey, at[2] - ez, 1.0 - ex, 1.0 - ey, 8.0 - ez, fired);
                }
            }
            case "marea" -> {
                // 3.6.3 |marea:age[:c[:ended]]: the Anemone over a place 8 ahead (radius 3.4) on its GIF's beats, two bars every 3 ticks
                // into it; a 4th field: ticks since let go (the burst and the splats)
                user(sink, "mochi_donuts", Math.min(age, 10), ex, ey, ez);
                double ended = move.length > 3 ? Double.parseDouble(move[3]) : -1.0;
                HieShots.body(sink, 0.6 - ex, -ey, 8.3 - ez);
                double[] strikes = new double[12 * 5];
                int n = 0;
                for (int k = 0; k < 12 && age >= 5 && ended < 0; k++) {
                    double start = 5 + (k / 2) * 3 + (k % 2);
                    double pa = age - start;
                    double cycle = 18.0;
                    pa = ((pa % cycle) + cycle) % cycle;
                    strikes[n * 5] = (k * 7) % com.hexhaki.piece.mochi.MochiDraw.ANEMONE_DONUTS;
                    strikes[n * 5 + 1] = 0.6 + Math.cos(k * 1.3) * 1.6 - ex;
                    strikes[n * 5 + 2] = 0.3 - ey;
                    strikes[n * 5 + 3] = 8.0 + Math.sin(k * 1.3) * 1.6 - ez;
                    strikes[n * 5 + 4] = pa;
                    n++;
                }
                com.hexhaki.piece.mochi.MochiDraw.anemoneArea(sink, -ex, -ey, 8.0 - ez, com.hexhaki.piece.server.MochiMoves.ANEMONE_RADIUS, 1.0, strikes, n,
                    coat, ended >= 0 ? 60 + ended : age, 9, ended);
            }
            case "mtrident" -> {
                // 3.6.4 |mtrident:k:age: the awakened left click's swing k (0 to 4) age ticks in, the user at the origin facing +z, a body 2.6
                // ahead: the posed user, Mogura in their right hand (as client/mochi/MochiClient holds it), the swing's trail, its marks
                int k = (int) Double.parseDouble(move[1]);
                double t = move.length > 2 ? Double.parseDouble(move[2]) : 3;
                String clip = "mochi_trident_" + (k + 1);
                float[][] pose = com.hexhaki.piece.client.npc.StylePoser.sample(clip, (float) Math.min(t, com.hexhaki.piece.mochi.MochiV2.TRIDENT_SWING));
                user(sink, clip, Math.min(t, com.hexhaki.piece.mochi.MochiV2.TRIDENT_SWING), ex, ey, ez);
                double by = k == 4 && t > com.hexhaki.piece.mochi.MochiV2.TRIDENT_HIT ? Math.min(9.0, 1.7 * (t - com.hexhaki.piece.mochi.MochiV2.TRIDENT_HIT)) : 0.0;
                HieShots.body(sink, -ex, by - ey, 2.6 - ez);
                double[] a = com.hexhaki.piece.mochi.MochiV2.mogura(pose, -com.hexhaki.piece.mochi.MochiV2.BACK, 0, new double[3]);
                double[] h = com.hexhaki.piece.mochi.MochiV2.mogura(pose, com.hexhaki.piece.mochi.MochiV2.CROSS, 0, new double[3]);
                double[] sd = com.hexhaki.piece.mochi.MochiV2.mogura(pose, com.hexhaki.piece.mochi.MochiV2.CROSS, 4, new double[3]);
                double sl = Math.sqrt((sd[0] - h[0]) * (sd[0] - h[0]) + (sd[1] - h[1]) * (sd[1] - h[1]) + (sd[2] - h[2]) * (sd[2] - h[2]));
                com.hexhaki.piece.mochi.MochiDraw.trident(sink, a[0] - ex, a[1] - ey, a[2] - ez, h[0] - ex, h[1] - ey, h[2] - ez, (sd[0] - h[0]) / sl,
                    (sd[1] - h[1]) / sl, (sd[2] - h[2]) / sl);
                double[] w = com.hexhaki.piece.mochi.MochiV2.TRIDENT_SWEEP[k];
                int n = 0;
                double[] in = new double[3 * 32];
                double[] out = new double[3 * 32];
                for (double u = w[0]; u <= Math.min(t, w[1]) + 1.0E-6 && n < 32; u += 0.4) {
                    float[][] q = com.hexhaki.piece.client.npc.StylePoser.sample(clip, (float) u);
                    double[] i0 = com.hexhaki.piece.mochi.MochiV2.mogura(q, com.hexhaki.piece.mochi.MochiV2.CROSS * 0.3, 0, new double[3]);
                    double[] o0 = com.hexhaki.piece.mochi.MochiV2.mogura(q, com.hexhaki.piece.mochi.MochiV2.TIP, 0, new double[3]);
                    in[n * 3] = i0[0] - ex;
                    in[n * 3 + 1] = i0[1] - ey;
                    in[n * 3 + 2] = i0[2] - ez;
                    out[n * 3] = o0[0] - ex;
                    out[n * 3 + 1] = o0[1] - ey;
                    out[n * 3 + 2] = o0[2] - ez;
                    n++;
                }
                double fade = 1.0 - Math.max(0.0, Math.min(1.0, (t - w[1]) / (com.hexhaki.piece.mochi.MochiV2.TRIDENT_LIFE - w[1])));
                com.hexhaki.piece.mochi.MochiV2.tridentTrail(sink, in, out, n, k == 2 || k == 3, fade);
                if (k == 2) {
                    com.hexhaki.piece.mochi.MochiV2.tridentSpin(sink, -ex, 0.02 - ey, -ez, 0.0, t, 5);
                }
                com.hexhaki.piece.mochi.MochiV2.tridentMark(sink, -ex, by + 1.1 - ey, 2.6 - ez, k, -ey, t - com.hexhaki.piece.mochi.MochiV2.TRIDENT_HIT, 9);
            }
            case "mpunch" -> {
                user(sink, "mochi_punch_1", age, ex, ey, ez, age <= com.hexhaki.piece.mochi.MochiDraw.PUNCH_LIFE ? 1 : 0);
                com.hexhaki.piece.mochi.MochiDraw.punch(sink, -0.36 - ex, 1.4 - ey, -ez, 0, 0, 1, 6.0, age, coat, 3);
            }
            case "myaki" -> {
                // |myaki:t[:c[:held]]: t < 0 held that many ticks back from now (MochiMoves.YAKI_FULL to full: t = -60 a tap, 0 full), else
                // the fist flying 2.2 a tick, the blast at 14 ahead; 3.6.3: the explosion by how long it was held (held, default full)
                double held = move.length > 3 ? Double.parseDouble(move[3]) : com.hexhaki.piece.server.MochiMoves.YAKI_FULL;
                double charge = com.hexhaki.piece.server.MochiMoves.yakiCharge(age < 0 ? com.hexhaki.piece.server.MochiMoves.YAKI_FULL + age : held);
                user(sink, age < 0 ? "mochi_yaki_charge" : "mochi_yaki_fire", age < 0 ? com.hexhaki.piece.server.MochiMoves.YAKI_FULL + age : age, ex, ey, ez);
                if (age < 0) {
                    com.hexhaki.piece.mochi.MochiDraw.swell(sink, -0.36 - ex, 1.3 - ey, 0.2 - ez, 0, 0, 1, charge, age + 60, coat, 2, -ey);
                } else if (age < 6) {
                    com.hexhaki.piece.mochi.MochiDraw.yakiPuff(sink, -0.36 - ex, 1.3 - ey, 1.0 - ez, 0, 0, 1, age, 13);
                    com.hexhaki.piece.mochi.MochiDraw.yaki(sink, -0.36 - ex, 1.3 - ey, 1.0 + age * 2.2 - ez, 0, 0, 1, age, coat, 2, charge);
                } else {
                    HieShots.body(sink, -ex, -ey, 14.0 - ez);
                    com.hexhaki.piece.mochi.MochiDraw.boom(sink, -ex, 1.0 - ey, 14.0 - ez, com.hexhaki.piece.server.MochiMoves.yakiRadius(charge) * 0.65,
                        age - 6, 5);
                }
            }
            case "mbuzz" -> {
                // 3.6.3 |mbuzz:age: Buzz Cut Mochi from the press: the wheel rolling along +z from the origin (speeding up), the body 10
                // ahead caught when the wheel reaches it, then the cinematic (MochiDraw.BUZZ_*): unroll, swing, whirl, raise, slam
                double rolled = 0.0;
                double speed = 0.0;
                double caught = -1.0;
                int most = com.hexhaki.piece.mochi.MochiDraw.BUZZ_PATH_MOST;
                double[] path = new double[3 * most];
                double[] ages = new double[most];
                int count = 0;
                double next = 0.0;
                for (int t = 0; t <= Math.min(age, 200); t++) {
                    speed = Math.min(1.0, (t + 2) / 12.0);
                    if (caught < 0 && rolled >= 9.0) {
                        caught = t;
                    }
                    if (caught >= 0) {
                        break;
                    }
                    rolled += com.hexhaki.piece.mochi.MochiDraw.BUZZ_SPEED * speed;
                    while (next <= rolled && count < most) {
                        path[count * 3] = 0;
                        path[count * 3 + 1] = 0;
                        path[count * 3 + 2] = next;
                        ages[count] = t;
                        count++;
                        next += com.hexhaki.piece.mochi.MochiDraw.BUZZ_TILE_GAP;
                    }
                }
                for (int k = 0; k < count; k++) {
                    ages[k] = age - ages[k];
                    path[k * 3] -= ex;
                    path[k * 3 + 1] -= ey;
                    path[k * 3 + 2] -= ez;
                }
                com.hexhaki.piece.mochi.MochiDraw.buzzTiles(sink, path, ages, count, 5);
                double r = com.hexhaki.piece.mochi.MochiDraw.BUZZ_WHEEL;
                if (caught < 0 || age < caught + 3) {
                    double form = Math.min(1.0, age / 4.0) * (caught < 0 ? 1.0 : 1.0 - (age - caught) / 3.0);
                    com.hexhaki.piece.mochi.MochiDraw.buzzWheel(sink, -ex, r - ey, rolled - ez, 0, 1, rolled, caught < 0 ? speed : 0.3, form, age, 3);
                }
                if (caught < 0) {
                    HieShots.body(sink, -ex, -ey, 10.5 - ez);
                    return true;
                }
                double t = age - caught;
                double[] arm = com.hexhaki.piece.mochi.MochiDraw.buzzArm(t, 0, 0, rolled, 0.0, new double[8]);
                boolean armOut = t <= com.hexhaki.piece.mochi.MochiDraw.BUZZ_END;
                if (t >= 2) {
                    // the right arm is the mace until the move ends (MochiFx.mochiArms), then the player's own again
                    com.hexhaki.piece.horo.AstralArt.hide = t < com.hexhaki.piece.mochi.MochiDraw.BUZZ_END ? 1 : 0;
                    ItoShots.posed(sink, "mochi_buzz", (float) t, 0.0, 0.0, rolled, 0.0, 0.9, rolled, ex, ey, ez, 0.0, 0.0);
                    com.hexhaki.piece.horo.AstralArt.hide = 0;
                }
                double[] bend = com.hexhaki.piece.mochi.MochiDraw.buzzBend(t, 0.0, new double[4]);
                if (armOut) com.hexhaki.piece.mochi.MochiDraw.buzzMace(sink, arm[0] - ex, arm[1] - ey, arm[2] - ez, arm[3], arm[4], arm[5], arm[6],
                    com.hexhaki.piece.mochi.MochiDraw.buzzSwell(t), bend[0], bend[1], bend[2], bend[3], t, 7);
                // the one caught: before the blow where the wheel met them, after it stuck at the mace's end, in the crater after the slam
                double bx = 0;
                double by = 0;
                double bz = 10.5;
                if (t >= com.hexhaki.piece.mochi.MochiDraw.BUZZ_HIT) {
                    double[] st = com.hexhaki.piece.mochi.MochiDraw.buzzStuck(t, 0, 0, 0, rolled, 0.0, new double[3]);
                    bx = st[0];
                    by = st[1];
                    bz = st[2];
                }
                HieShots.body(sink, bx - ex, by - ey, bz - ez);
                if (t >= com.hexhaki.piece.mochi.MochiDraw.BUZZ_HIT && t < com.hexhaki.piece.mochi.MochiDraw.BUZZ_WHIRL + 4) {
                    double turn = Math.atan2(arm[5], arm[3]);
                    double a = Math.min(1.0, (t - com.hexhaki.piece.mochi.MochiDraw.BUZZ_HIT) / 6.0)
                        * (1.0 - Math.max(0.0, (t - com.hexhaki.piece.mochi.MochiDraw.BUZZ_WHIRL) / 4.0));
                    com.hexhaki.piece.mochi.MochiDraw.buzzVortex(sink, -ex, -ey, rolled - ez, turn, arm[6], a);
                }
                if (t >= com.hexhaki.piece.mochi.MochiDraw.BUZZ_SLAM) {
                    com.hexhaki.piece.mochi.MochiDraw.buzzCrater(sink, bx - ex, -ey, bz - ez, t - com.hexhaki.piece.mochi.MochiDraw.BUZZ_SLAM, 11);
                }
            }
            case "mfirst" -> {
                // |mfirst:age:what[:c]: one's own move in first person, as client/mochi/MochiFx starts it there (the arm from below and
                // to the right of the view, 0.55 ahead); the camera at the eye (0, 1.62, 0) looking along +z
                String what = move.length > 3 ? move[3] : "punch";
                double sx = -0.4;
                double sy = 1.62 - 0.45;
                double sz = 0.45;
                switch (what) {
                    case "punch" -> com.hexhaki.piece.mochi.MochiDraw.punch(sink, sx - ex, sy - ey, sz - ez, 0, 0, 1, 6.0, age, coat, 3);
                    case "swell" -> com.hexhaki.piece.mochi.MochiDraw.swell(sink, -0.55 - ex, 1.02 - ey, 1.4 - ez, 0, 0, 1,
                        Math.min(1.0, age / 16.0), age, coat, 2);
                    default -> {
                    }
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
