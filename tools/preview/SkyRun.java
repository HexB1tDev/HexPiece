import com.hexhaki.piece.ito.SkyPhysics;
import java.util.ArrayList;
import java.util.List;

/**
 * 3.5.4 (the owner: "When ur done I wanna see a gif of the sky walk with all variants"): a scripted Sky Road run, tick by tick, through
 * the very state machine client/ito/SkyRoad.drive runs (ito/SkyPhysics' numbers) with scripted keys instead of the player's: hang, swing
 * (W held: swings, each let go with a backflip or a split leap), a tap of Space (the leap to a string further away), Space held (the
 * climb on steps of string), Shift (the drop), each let go caught by a string; then the same swings with Haki. Flat ground at y 0, the
 * runner facing +z (yaw 0) all the way. ItoShots |iskyrun:t draws frame t; main prints the shots (a chase camera) for FxPreview and the
 * frames' captions.
 *
 * <p>Usage: java -cp build/preview:build/classes SkyRun shots.txt captions.txt
 */
public final class SkyRun {
    private SkyRun() {
    }

    static final int HANG = 0;
    static final int SWING = 1;
    static final int FLY = 2;
    static final int LEAP = 3;
    static final int CLIMB = 4;
    static final int DROP = 5;

    /** One tick of the run as drawn. */
    static final class Frame {
        double fx;
        double fy;
        double fz;
        int mode;
        String clip;
        float clipTick;
        double pitch;
        double pivotY;
        double pivotOut;
        double[] rope;
        double ropeGrow;
        double taut;
        double[] old;
        double oldFade;
        final List<double[]> treads = new ArrayList<>();
        double[] feet = new double[24];
        int feetCount;
        boolean coat;
        String caption;
    }

    static List<Frame> frames;

    /** The keys a tick: forward held, jump held, sneak held; whether Haki is on; the caption. */
    static boolean forward(int t) {
        return t >= 30 && t < 170 || t >= 330;
    }

    static boolean jump(int t) {
        return t >= 170 && t < 172 || t >= 205 && t < 240;
    }

    static boolean sneak(int t) {
        return t >= 262 && t < 280;
    }

    static boolean coat(int t) {
        return t >= 310;
    }

    static final int END = 440;

    static String caption(Frame f, int t) {
        String haki = f.coat ? "  (Haki on)" : "";
        return switch (f.mode) {
            case HANG -> "Hanging one-handed (no key)" + haki;
            case SWING -> "Swinging (hold W)" + haki;
            case FLY -> f.clip.equals("ito_sky_backflip") || f.clip.equals("ito_sky_leap") ? "Let go at the top: " + (f.clip.equals(
                "ito_sky_backflip") ? "backflip" : "split leap") + haki : "Flying to the next string" + haki;
            case LEAP -> "Leap to a string further away (tap Space)" + haki;
            case CLIMB -> "Climbing string steps (hold Space)" + haki;
            default -> "Dropping (Shift)" + haki;
        };
    }

    static List<Frame> run() {
        if (frames != null) {
            return frames;
        }
        frames = new ArrayList<>();
        double[] v = new double[3];
        double[] h = new double[3];
        double[] a = new double[3];
        double[] out = new double[5];
        double[] push = new double[3];
        double[] way = new double[2];
        double[] still = new double[3];
        double[] leapAt = new double[3];
        double fx = 0;
        double fy = 12;
        double fz = 0;
        double cruise = fy + SkyPhysics.GRIP;
        int mode = FLY;
        int since = SkyPhysics.NEXT_LATEST;
        int stepK = 0;
        boolean jumpWas = false;
        int jumpAt = -100;
        double[] rope = null;
        int ropeBorn = 0;
        double ropeLength = 0;
        double ropeTarget = 0;
        double[] old = null;
        int oldDropped = -100;
        double swingX = 0;
        double swingZ = 1;
        String clip = null;
        int clipAt = 0;
        int trickUntil = -1;
        int tricks = 0;
        double pitch = 0;
        double pivotY = 0.9;
        double pivotOut = 0;
        double[] vel = new double[3];
        double[] last = null;
        List<double[]> treads = new ArrayList<>();
        double[] feet = new double[24];
        int feetCount = 0;
        for (int t = 0; t < END; t++) {
            if (t == 300) {
                // the Haki part: begun again from a hang, a little higher
                fx = 0;
                fy = 12;
                fz = frames.get(frames.size() - 1).fz;
                v[0] = 0;
                v[1] = 0;
                v[2] = 0;
                cruise = fy + SkyPhysics.GRIP;
                mode = FLY;
                since = SkyPhysics.NEXT_LATEST;
                rope = null;
                old = null;
                treads.clear();
                feetCount = 0;
                last = null;
            }
            boolean moving = forward(t);
            boolean up = jump(t);
            boolean down = sneak(t);
            double wx = 0;
            double wz = moving ? 1 : 0;
            if (up && !jumpWas) {
                jumpAt = t;
            }
            boolean tap = !up && jumpWas && t - jumpAt < SkyPhysics.TAP;
            boolean climb = up && t - jumpAt >= SkyPhysics.TAP;
            jumpWas = up;
            h[0] = fx;
            h[1] = fy + SkyPhysics.GRIP;
            h[2] = fz;
            since++;
            String want = clip;
            if (tap) {
                SkyPhysics.leap(h, 0.0, -12.0, leapAt, v);
                cruise = leapAt[1];
                if (rope != null) {
                    old = rope;
                    oldDropped = t;
                    rope = null;
                }
                mode = LEAP;
                since = 0;
                want = "ito_sky_leap";
                trickUntil = t + 12;
            } else if (climb) {
                if (mode != CLIMB) {
                    if (rope != null) {
                        old = rope;
                        oldDropped = t;
                        rope = null;
                    }
                    mode = CLIMB;
                    stepK = 0;
                    want = "ito_sky_climb";
                }
            } else if (down) {
                if (mode != DROP) {
                    if (rope != null) {
                        old = rope;
                        oldDropped = t;
                        rope = null;
                    }
                    mode = DROP;
                    want = "ito_sky_drop";
                }
            } else if (mode == CLIMB || mode == DROP) {
                cruise = h[1];
                mode = -1; // the next string at once, below
            }
            boolean throwNext = mode == -1;
            switch (mode) {
                case CLIMB -> {
                    SkyPhysics.step(stepK, moving ? wx : 0.0, moving ? wz : 0.0, v);
                    if (stepK == 0) {
                        treads.add(new double[] {fx + (moving ? wx * SkyPhysics.STEP_ALONG : 0), fy + SkyPhysics.STEP_RISE - 0.02,
                            fz + (moving ? wz * SkyPhysics.STEP_ALONG : 0), t});
                    }
                    stepK = (stepK + 1) % SkyPhysics.STEP_TICKS;
                }
                case DROP -> SkyPhysics.fly(v, 0.08);
                case LEAP -> {
                    if (since > 0) {
                        SkyPhysics.fly(v, SkyPhysics.FLY_GRAVITY);
                    }
                    if (since == SkyPhysics.LEAP_TICKS - (int) SkyPhysics.THROW) {
                        old = null;
                        rope = new double[] {leapAt[0] + v[0] * 3.0, leapAt[1] + SkyPhysics.HANG_RISE, leapAt[2] + v[2] * 3.0};
                        ropeBorn = t;
                        ropeLength = Math.sqrt(9 * (v[0] * v[0] + v[2] * v[2]) + SkyPhysics.HANG_RISE * SkyPhysics.HANG_RISE);
                        ropeTarget = ropeLength;
                        mode = moving ? SWING : HANG;
                        since = 0;
                        want = moving ? "ito_sky_swinging" : "ito_sky_hang";
                    }
                }
                case FLY -> {
                    SkyPhysics.fly(v, SkyPhysics.FLY_GRAVITY);
                    if (since >= SkyPhysics.NEXT_LATEST || since >= SkyPhysics.NEXT_SOONEST && v[1] < 0.05) {
                        throwNext = true;
                    }
                }
                case SWING, HANG -> {
                    boolean held = rope != null && t - ropeBorn >= SkyPhysics.THROW;
                    if (mode == HANG && moving && held) {
                        throwNext = true;
                        held = false;
                    }
                    if (!held) {
                        SkyPhysics.fly(v, SkyPhysics.FLY_GRAVITY);
                    } else {
                        a[0] = rope[0];
                        a[1] = rope[1];
                        a[2] = rope[2];
                        if (mode == SWING) {
                            // 3.8.0 as client/ito/SkyRoad.drive: pumped toward its speed with a key, coasting on without one
                            double pump = moving ? SkyPhysics.pump(v, false) : 0.0;
                            push[0] = wx * pump;
                            push[1] = 0.0;
                            push[2] = wz * pump;
                            ropeLength = SkyPhysics.reel(ropeLength, ropeTarget);
                            SkyPhysics.swing(h, v, a, ropeLength, push, SkyPhysics.SWING_DRAG, true);
                            if (SkyPhysics.letGo(h, a, v, swingX, swingZ)) {
                                if (SkyPhysics.kicks(v, moving, false)) {
                                    SkyPhysics.kick(v);
                                }
                                old = rope;
                                oldDropped = t;
                                rope = null;
                                mode = FLY;
                                since = 0;
                                want = tricks++ % 2 == 0 ? "ito_sky_backflip" : "ito_sky_leap";
                                trickUntil = t + (want.equals("ito_sky_backflip") ? 14 : 12);
                            } else if (!moving && SkyPhysics.spent(h, a, v, swingX, swingZ)) {
                                mode = HANG;
                                want = "ito_sky_hang";
                            }
                        } else {
                            SkyPhysics.swing(h, v, a, ropeLength, still, SkyPhysics.HANG_DRAG);
                            v[1] += 0.006 * Math.sin(t * 0.12);
                        }
                    }
                }
                default -> {
                }
            }
            if (throwNext) {
                if (rope != null) {
                    old = rope;
                    oldDropped = t;
                }
                if (SkyPhysics.way(wx, wz, v[0], v[2], way)) {
                    cruise = Math.max(cruise, SkyPhysics.floor(0.0));
                    SkyPhysics.anchor(h, way[0], way[1], Math.hypot(v[0], v[2]), (t * 0.37) % 1.0, cruise, 0.0, out);
                    rope = new double[] {out[0], out[1], out[2]};
                    ropeLength = out[4];
                    ropeTarget = out[3];
                    swingX = way[0];
                    swingZ = way[1];
                    mode = SWING;
                    want = "ito_sky_swinging";
                } else {
                    SkyPhysics.hangAnchor(h, v[0], v[2], out);
                    rope = new double[] {out[0], out[1], out[2]};
                    ropeLength = out[3];
                    ropeTarget = out[3];
                    mode = HANG;
                    want = "ito_sky_hang";
                }
                ropeBorn = t;
                since = 0;
                if (mode == SWING) {
                    SkyPhysics.fly(v, SkyPhysics.FLY_GRAVITY);
                }
            }
            if ((mode == FLY || mode == LEAP) && trickUntil >= 0 && t >= trickUntil) {
                trickUntil = -1;
                want = "ito_sky_drop";
            }
            if (want != null && !want.equals(clip)) {
                clip = want;
                clipAt = t;
            }
            fx += v[0];
            fy += v[1];
            fz += v[2];
            if (fy < 0) {
                fy = 0;
                v[1] = 0;
            }
            // what every client works out from the movement (SkyRoad.follow): the speed, the lean along the string, the streak
            if (last != null) {
                vel[0] = vel[0] * 0.5 + (fx - last[0]) * 0.5;
                vel[1] = vel[1] * 0.5 + (fy - last[1]) * 0.5;
                vel[2] = vel[2] * 0.5 + (fz - last[2]) * 0.5;
            }
            last = new double[] {fx, fy, fz};
            double hs = Math.hypot(vel[0], vel[2]);
            boolean held = rope != null && t - ropeBorn >= SkyPhysics.THROW && (mode == SWING || mode == HANG);
            double pitchTo;
            if (held) {
                double dx = rope[0] - (fx - 0.42);
                double dy = rope[1] - (fy + SkyPhysics.GRIP);
                double dz = rope[2] - fz;
                double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
                pitchTo = Math.max(-75, Math.min(75, Math.toDegrees(Math.atan2(dz / dl, Math.max(0.05, dy / dl)))));
            } else if (mode == CLIMB) {
                pitchTo = 8.0;
            } else if (mode == DROP) {
                pitchTo = 0.0;
            } else {
                pitchTo = 4.0 + 22.0 * Math.min(1.0, hs / 1.2);
            }
            pitch += (pitchTo - pitch) * (held ? 0.4 : 0.18);
            pivotY += ((held ? SkyPhysics.GRIP : 0.9) - pivotY) * 0.3;
            pivotOut += ((held ? 0.42 : 0.0) - pivotOut) * 0.3;
            if (hs > 0.15 && mode != CLIMB) {
                System.arraycopy(feet, 0, feet, 3, 21);
                feet[0] = fx;
                feet[1] = fy + 0.12;
                feet[2] = fz;
                feetCount = Math.min(8, feetCount + 1);
            } else if (feetCount > 0) {
                feetCount--;
            }
            Frame f = new Frame();
            f.fx = fx;
            f.fy = fy;
            f.fz = fz;
            f.mode = mode;
            f.clip = clip == null ? "ito_sky_hang" : clip;
            f.clipTick = t - clipAt;
            f.pitch = pitch;
            f.pivotY = pivotY;
            f.pivotOut = pivotOut;
            if (rope != null) {
                f.rope = rope.clone();
                f.ropeGrow = Math.min(1.0, (t - ropeBorn + 1) / SkyPhysics.THROW);
                f.taut = f.ropeGrow >= 1.0 ? Math.max(0.25, 1.0 - (t - ropeBorn - SkyPhysics.THROW) / 6.0) : 0.0;
            }
            if (old != null && t - oldDropped < 6) {
                f.old = old.clone();
                f.oldFade = (t - oldDropped) / 6.0;
            }
            for (double[] tr : treads) {
                if (t - tr[3] <= com.hexhaki.piece.ito.ItoArt.TREAD_LIFE) {
                    f.treads.add(new double[] {tr[0], tr[1], tr[2], t - tr[3]});
                }
            }
            f.feet = feet.clone();
            f.feetCount = feetCount;
            f.coat = coat(t);
            f.caption = caption(f, t);
            frames.add(f);
        }
        return frames;
    }

    public static void main(String[] args) throws Exception {
        List<Frame> fs = run();
        StringBuilder shots = new StringBuilder();
        StringBuilder captions = new StringBuilder();
        // a chase camera: behind and to the right of the runner, its target eased after the body (never a jolt)
        double tx = fs.get(0).fx;
        double ty = fs.get(0).fy + 1.5;
        double tz = fs.get(0).fz;
        for (int t = 0; t < fs.size(); t++) {
            Frame f = fs.get(t);
            // still shots that cut ahead when the runner nears the frame's edge (a still background keeps the GIF small; a camera
            // following every tick changes every pixel)
            double vz = t > 0 ? f.fz - fs.get(t - 1).fz : 0.0;
            if (t == 0 || t == 300 || Math.abs(f.fz - tz) > 5.0 || Math.abs(f.fy + 2.2 - ty) > 2.6) {
                tx = f.fx;
                ty = f.fy + 2.2 + Math.max(-1.5, Math.min(1.5, (f.fy - (t > 0 ? fs.get(t - 1).fy : f.fy)) * 6.0));
                tz = f.fz + Math.max(-4.0, Math.min(4.0, vz * 5.0));
            }
            shots.append(String.format("e:98:6:11:%.3f:%.3f:%.3f|iskyrun:%d%n", tx, ty, tz, t));
            captions.append(f.caption).append('\n');
        }
        java.nio.file.Files.writeString(java.nio.file.Path.of(args[0]), shots.toString());
        java.nio.file.Files.writeString(java.nio.file.Path.of(args[1]), captions.toString());
    }
}
