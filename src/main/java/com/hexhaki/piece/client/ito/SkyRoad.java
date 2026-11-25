package com.hexhaki.piece.client.ito;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.ito.ItoArt;
import com.hexhaki.piece.ito.SkyPhysics;
import com.hexhaki.piece.network.C2SItoSky;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.ItoMoves;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Sky Road, Doflamingo's Sora no Michi. 3.5.2 reworked (the owner: "Sky walk needs a rework, I dont want 2 constant lines holding me up
 * I wanna climb up like stairs when I hold Space, I wanna jump from 1 string to another further away, I want to swing like spiderman,
 * come on man make sky walk insanely epic"; the keys they chose: W/A/S/D swing, a tap of Space leaps, Space held climbs, Shift drops;
 * still, one hand hangs on one string; a trick as a swing lets go). Still started by a double tap of jump in the air (client/FruitKeyN).
 *
 * <p>One string at a time, never two: hanging, one string straight up (ito_sky_hang); moving, a Spider-Man swing: one string shot ahead
 * into the sky, the body swinging under it like a pendulum (the whole body turned along the string round the hand, ito_sky_swinging),
 * let go as it rises ahead of the anchor with a kick and a backflip or a split leap (ito_sky_backflip, ito_sky_leap), flying free until
 * the next string catches it; a tap of Space a leap along the look to a string caught further away; Space held, steps of string under the
 * feet like stairs (ito_sky_climb, a step every 5 ticks); Shift, every string let go (ito_sky_drop). The numbers are ito/SkyPhysics'.
 *
 * <p>The runner's own client moves its body (as the Light fruit's glide does, so it is smooth on screen and the server's own movement
 * check agrees) and tells the server each string it throws, lets go or steps on (network/C2SItoSky); the server passes them to everyone
 * near (ItoMoves.SKY_STRING), so every client draws the same strings and poses every runner the same way. The server keeps only what it
 * must (server/ItoMoves.sky: the player may fly, no fall counted, the end on the ground or at the time limit).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class SkyRoad {
    /** The runner's states. */
    static final int HANG = 0;
    static final int SWING = 1;
    static final int FLY = 2;
    static final int LEAP = 3;
    static final int CLIMB = 4;
    static final int DROP = 5;
    /** How long a string let go fades, how many steps of string show at once, the streak's length. */
    static final double DROP_FADE = 6.0;
    static final int TREADS = 6;
    static final int FEET = 8;
    /** A dropping body falls as the game's own bodies do. */
    static final double DROP_GRAVITY = 0.08;
    /** Where the hand on the string is over the feet (to the body's right, the arm straight up): the body turns round it on a string. */
    static final double HAND_OUT = 0.42;
    /** The most the body leans along its string. */
    static final double TILT_MOST = 75.0;
    /** A remote runner's climb lasts this long after its last step (its next string or let go comes on its own). */
    static final int CLIMB_HOLD = 9;

    static final String HANG_CLIP = "ito_sky_hang";
    static final String SWING_CLIP = "ito_sky_swinging";
    static final String CLIMB_CLIP = "ito_sky_climb";
    static final String DROP_CLIP = "ito_sky_drop";
    static final String BACKFLIP_CLIP = "ito_sky_backflip";
    static final String LEAP_CLIP = "ito_sky_leap";
    static final int BACKFLIP_TICKS = 14;
    static final int LEAP_TICKS = 12;

    /** One runner as this client sees it. */
    static final class Runner {
        long born;
        long until;
        boolean confirmed;
        boolean coat;
        int mode = FLY;
        Vec3 last;
        Vec3 vel = Vec3.f_82478_;
        double heading = Double.NaN;
        double speed;
        double turn;
        /** The string held (its anchor), when it was thrown, its length; the one let go and when. */
        Vec3 rope;
        long ropeBorn;
        double ropeLength;
        Vec3 old;
        long oldDropped = -100;
        String clip;
        long clipAt;
        long trickUntil = -1;
        long lastStep = -100;
        /** The body's lean along its string (or forward and into turns), this tick and the last; the pivot's height and reach out. */
        double pitch;
        double roll;
        double pitchWas;
        double rollWas;
        double pivotY = 0.9;
        double pivotOut;
        final double[] treads = new double[TREADS * 5];
        int treadNext;
        final double[] feet = new double[FEET * 3];
        int feetCount;
    }

    static final Map<Integer, Runner> RUNNERS = new HashMap<>();
    static long clock;
    /** The keys one's own runner took from the game this tick. */
    static float forward;
    static float left;
    static boolean up;
    static boolean down;
    /** One's own runner: its velocity, the height its swings hold, its swing's flat way, ticks in its state, its leap's landing. */
    static final double[] V = new double[3];
    static double cruise;
    static double swingX;
    static double swingZ;
    static int since;
    static int stepK;
    static int foot;
    static double ropeTarget;
    static final double[] LEAP_AT = new double[3];
    /** Space: when it was last pressed, whether it was down, and the press that started it all (ignored until let go). */
    static long jumpAt;
    static boolean jumpWas;
    static boolean ignoreJump;
    static double fovSpeed;
    static final double[] H = new double[3];
    static final double[] A = new double[3];
    static final double[] OUT = new double[5];
    static final double[] PUSH = new double[3];
    static final double[] STILL = new double[3];
    /** 3.8.0: the next swing's flat way (SkyPhysics.way), and where one's own body stood last tick (NaN: not yet), to see what a wall took. */
    static final double[] WAY = new double[2];
    static final double[] LAST = {Double.NaN, 0.0, 0.0};

    private SkyRoad() {
    }

    public static boolean active(int id) {
        return RUNNERS.containsKey(id);
    }

    static boolean empty() {
        return RUNNERS.isEmpty();
    }

    /** The server's word: SKY on (bx 1) or off. */
    static void accept(S2CUoFx m) {
        if (m.bx() > 0.5F) {
            Runner r = RUNNERS.get(m.user());
            if (r == null) {
                if (RUNNERS.size() >= 64) {
                    return;
                }
                r = new Runner();
                r.born = clock;
                RUNNERS.put(m.user(), r);
            }
            r.until = clock + m.ticks();
            r.confirmed = true;
            r.coat = m.bz() > 0.5F;
        } else {
            end(m.user());
        }
    }

    /** A runner's string thrown, let go or stepped on (ItoMoves.SKY_STRING; one's own client already has its own). */
    static void string(S2CUoFx m) {
        Minecraft minecraft = Minecraft.m_91087_();
        Runner r = RUNNERS.get(m.user());
        if (r == null || minecraft.f_91074_ != null && m.user() == minecraft.f_91074_.m_19879_()) {
            return;
        }
        r.coat = m.bz() > 0.5F;
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        switch (m.id()) {
            case ItoMoves.ROPE_SWING, ItoMoves.ROPE_HANG -> {
                letGo(r);
                r.rope = at;
                r.ropeBorn = clock;
                r.ropeLength = m.by();
                r.mode = m.id() == ItoMoves.ROPE_SWING ? SWING : HANG;
                clip(m.user(), r, r.mode == SWING ? SWING_CLIP : HANG_CLIP, 4);
            }
            case ItoMoves.SKY_FLY -> {
                letGo(r);
                trick(m.user(), r, (int) m.bx());
            }
            case ItoMoves.SKY_STEP -> {
                letGo(r);
                r.mode = CLIMB;
                r.lastStep = clock;
                tread(r, at.f_82479_, at.f_82480_, at.f_82481_, m.by());
                clip(m.user(), r, CLIMB_CLIP, 3);
            }
            default -> {
            }
        }
    }

    /** One's own double tap of jump sent (client/FruitKeyN): started at once (the server's word follows; unanswered, it stops in 10 ticks). */
    public static void startLocal(LocalPlayer self) {
        if (RUNNERS.containsKey(self.m_19879_())) {
            return;
        }
        Runner r = new Runner();
        r.born = clock;
        r.until = clock + ItoMoves.SKY_MOST;
        r.mode = FLY;
        RUNNERS.put(self.m_19879_(), r);
        Vec3 v = self.m_20184_();
        V[0] = v.f_82479_;
        V[1] = Math.max(v.f_82480_, 0.0);
        V[2] = v.f_82481_;
        // the first string at once (a hang, or a swing if a key is held)
        since = SkyPhysics.NEXT_LATEST;
        cruise = self.m_20186_() + SkyPhysics.GRIP;
        LAST[0] = Double.NaN;
        // the jump press that started it is no tap and no climb: ignored until let go
        ignoreJump = true;
        jumpWas = true;
    }

    static void end(int id) {
        Runner r = RUNNERS.remove(id);
        if (r != null && r.clip != null) {
            com.hexhaki.client.anim.HakiAnimations.playRemote(id, "__clear__", 6);
        }
    }

    // ------------------------------------------------------------------------------------------------------ the keys

    /** One's own runner's keys: taken from the game (it does not walk the body) and kept for {@link #drive}. */
    @SubscribeEvent
    public static void input(MovementInputUpdateEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (event.getEntity() != minecraft.f_91074_ || !RUNNERS.containsKey(event.getEntity().m_19879_())) {
            return;
        }
        net.minecraft.client.player.Input input = event.getInput();
        forward = input.f_108567_;
        left = input.f_108566_;
        up = input.f_108572_;
        down = input.f_108573_;
        input.f_108566_ = 0.0F;
        input.f_108567_ = 0.0F;
        input.f_108572_ = false;
        input.f_108573_ = false;
    }

    // ------------------------------------------------------------------------------------------------------ the tick

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clock++;
        if (RUNNERS.isEmpty()) {
            fovSpeed *= 0.8;
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            RUNNERS.clear();
            return;
        }
        if (minecraft.m_91104_()) {
            return;
        }
        java.util.List<Integer> done = null;
        for (Map.Entry<Integer, Runner> e : RUNNERS.entrySet()) {
            Runner r = e.getValue();
            Entity body = level.m_6815_(e.getKey());
            if (body == null || !body.m_6084_() || clock > r.until + 20 || !r.confirmed && clock - r.born > 10) {
                if (done == null) {
                    done = new java.util.ArrayList<>();
                }
                done.add(e.getKey());
                continue;
            }
            if (body == minecraft.f_91074_) {
                drive(minecraft, minecraft.f_91074_, r);
            }
            follow(e.getKey(), body, r);
        }
        if (done != null) {
            for (Integer id : done) {
                end(id);
            }
        }
    }

    /** One's own runner: the state from the keys, then the next tick's velocity by ito/SkyPhysics. */
    static void drive(Minecraft minecraft, LocalPlayer self, Runner r) {
        int id = self.m_19879_();
        double yawDeg = self.m_146908_();
        double pitchDeg = self.m_146909_();
        double yaw = Math.toRadians(yawDeg);
        double wx = -Math.sin(yaw) * forward + Math.cos(yaw) * left;
        double wz = Math.cos(yaw) * forward + Math.sin(yaw) * left;
        double wl = Math.sqrt(wx * wx + wz * wz);
        boolean moving = wl > 0.01;
        if (moving) {
            wx /= wl;
            wz /= wl;
        }
        boolean sprint = minecraft.f_91066_.f_92091_.m_90857_() || self.m_20142_();
        // Space: a tap leaps, held it climbs (the press that started Sky Road is neither)
        if (up && !jumpWas) {
            jumpAt = clock;
        }
        boolean tap = !up && jumpWas && !ignoreJump && clock - jumpAt < SkyPhysics.TAP;
        boolean climb = up && !ignoreJump && clock - jumpAt >= SkyPhysics.TAP;
        if (!up) {
            ignoreJump = false;
        }
        jumpWas = up;
        Vec3 pos = self.m_20182_();
        H[0] = pos.f_82479_;
        H[1] = pos.f_82480_ + SkyPhysics.GRIP;
        H[2] = pos.f_82481_;
        // what the game stopped stays stopped (a wall, a ceiling); 3.8.0: a wall stops only the way it blocked, the body slides on
        // along it with the rest of its speed (it was all cut to 0.3: every tree brushed stopped the run)
        LivingEntity living = self;
        if (living.f_19862_ && !Double.isNaN(LAST[0])) {
            V[0] = SkyPhysics.slide(V[0], pos.f_82479_ - LAST[0]);
            V[2] = SkyPhysics.slide(V[2], pos.f_82481_ - LAST[2]);
        }
        LAST[0] = pos.f_82479_;
        LAST[1] = pos.f_82480_;
        LAST[2] = pos.f_82481_;
        if (living.f_19863_ && V[1] > 0.0) {
            V[1] = 0.0;
        }
        since++;
        if (tap) {
            leap(id, r, yawDeg, pitchDeg);
        } else if (climb) {
            if (r.mode != CLIMB) {
                letGo(r);
                r.mode = CLIMB;
                stepK = 0;
                clip(id, r, CLIMB_CLIP, 3);
            }
        } else if (down) {
            if (r.mode != DROP) {
                letGo(r);
                r.mode = DROP;
                clip(id, r, DROP_CLIP, 4);
                send(ItoMoves.SKY_FLY, pos, 0.0, ItoMoves.SKY_DROP);
            }
        } else if (r.mode == CLIMB || r.mode == DROP) {
            // Space or Shift let go: the swings hold this height now; the next string at once
            cruise = H[1];
            next(minecraft, self, r, moving, wx, wz);
        }
        switch (r.mode) {
            case CLIMB -> {
                SkyPhysics.step(stepK, moving ? wx : 0.0, moving ? wz : 0.0, V);
                if (stepK == 0) {
                    // the step the foot comes down on: where the feet will be at its end
                    double sx = pos.f_82479_ + (moving ? wx * SkyPhysics.STEP_ALONG : 0.0);
                    double sy = pos.f_82480_ + SkyPhysics.STEP_RISE - 0.02;
                    double sz = pos.f_82481_ + (moving ? wz * SkyPhysics.STEP_ALONG : 0.0);
                    tread(r, sx, sy, sz, yawDeg);
                    r.lastStep = clock;
                    send(ItoMoves.SKY_STEP, new Vec3(sx, sy, sz), yawDeg, foot);
                    foot ^= 1;
                }
                stepK = (stepK + 1) % SkyPhysics.STEP_TICKS;
            }
            case DROP -> SkyPhysics.fly(V, DROP_GRAVITY);
            case LEAP -> {
                if (since > 0) {
                    SkyPhysics.fly(V, SkyPhysics.FLY_GRAVITY);
                }
                if (since == SkyPhysics.LEAP_TICKS - (int) SkyPhysics.THROW) {
                    // the string to land on, thrown up over the landing so it holds as the body gets there
                    Vec3 hand = new Vec3(LEAP_AT[0], LEAP_AT[1], LEAP_AT[2]);
                    Vec3 at = clipped(self, hand, hand.m_82520_(V[0] * 3.0, SkyPhysics.HANG_RISE, V[2] * 3.0));
                    throwString(id, r, at, Math.max(SkyPhysics.SHORTEST, at.m_82554_(hand)), moving);
                    if (moving) {
                        swingX = wx;
                        swingZ = wz;
                    }
                }
            }
            case FLY -> {
                SkyPhysics.fly(V, SkyPhysics.FLY_GRAVITY);
                if (since >= SkyPhysics.NEXT_LATEST || since >= SkyPhysics.NEXT_SOONEST && V[1] < 0.05) {
                    next(minecraft, self, r, moving, wx, wz);
                }
            }
            default -> {
                boolean held = r.rope != null && clock - r.ropeBorn >= SkyPhysics.THROW;
                if (r.mode == HANG && moving && held) {
                    next(minecraft, self, r, true, wx, wz);
                    held = false;
                }
                if (!held) {
                    SkyPhysics.fly(V, SkyPhysics.FLY_GRAVITY);
                } else {
                    A[0] = r.rope.f_82479_;
                    A[1] = r.rope.f_82480_;
                    A[2] = r.rope.f_82481_;
                    if (r.mode == SWING) {
                        // pumped along the keys' way while one is held; 3.8.0: with none it swings on, on its own momentum, lets go at the
                        // top and the next string carries it on (it used to fall into the hang at once, the speed gone in half a second)
                        double pump = moving ? SkyPhysics.pump(V, sprint) : 0.0;
                        PUSH[0] = wx * pump;
                        PUSH[1] = 0.0;
                        PUSH[2] = wz * pump;
                        r.ropeLength = SkyPhysics.reel(r.ropeLength, ropeTarget);
                        SkyPhysics.swing(H, V, A, r.ropeLength, PUSH, SkyPhysics.SWING_DRAG, true);
                        if (moving) {
                            // looking up or down as it swings raises or lowers the height the swings hold, a little
                            cruise += Math.max(-1.0, Math.min(1.0, -Math.sin(Math.toRadians(pitchDeg)))) * 0.03;
                        }
                        if (SkyPhysics.letGo(H, A, V, swingX, swingZ)) {
                            release(id, r, pos, SkyPhysics.kicks(V, moving, sprint));
                        } else if (!moving && SkyPhysics.spent(H, A, V, swingX, swingZ)) {
                            // a swing that never got ahead and is slow: it settles under its string
                            r.mode = HANG;
                            clip(id, r, HANG_CLIP, 6);
                        }
                    } else {
                        // hanging (no key and no speed left): it settles under its string, bobbing
                        SkyPhysics.swing(H, V, A, r.ropeLength, STILL, SkyPhysics.HANG_DRAG);
                        V[1] += 0.006 * Math.sin(clock * 0.12);
                    }
                }
            }
        }
        if (self.m_20096_() && r.mode != CLIMB) {
            V[1] = Math.min(V[1], 0.0) - 0.08; // on the ground: it settles (and the server ends the run)
        }
        self.m_20334_(V[0], V[1], V[2]);
        self.f_19789_ = 0.0F;
        if (!self.m_7500_() && self.m_150110_().f_35935_) {
            self.m_150110_().f_35935_ = false; // never the game's own flight (a double tap of jump while the strings hold it)
        }
        fovSpeed += (Math.min(1.0, Math.sqrt(V[0] * V[0] + V[2] * V[2]) / 1.2) - fovSpeed) * 0.15;
    }

    /**
     * The next string: a swing's when a key is held (along the keys' way leaned toward the drift: SkyPhysics.way) or, 3.8.0, with none
     * while the body still has its momentum (on along the drift); else, slow and no key, a hang's straight up.
     */
    static void next(Minecraft minecraft, LocalPlayer self, Runner r, boolean moving, double wx, double wz) {
        int id = self.m_19879_();
        Vec3 hand = new Vec3(H[0], H[1], H[2]);
        double speed = Math.sqrt(V[0] * V[0] + V[2] * V[2]);
        if (SkyPhysics.way(moving ? wx : 0.0, moving ? wz : 0.0, V[0], V[2], WAY)) {
            double sx = WAY[0];
            double sz = WAY[1];
            // the ground under the hand and under where the swing bottoms out (ahead, under the anchor): the higher (a hill ahead)
            double r01 = FireMesh.rand(id + (int) clock, 3);
            SkyPhysics.anchor(H, sx, sz, speed, r01, cruise, Double.NaN, OUT);
            double ground = ground(self, hand);
            double ahead = ground(self, new Vec3(OUT[0], H[1] + 2.0, OUT[2]));
            if (Double.isNaN(ground) || !Double.isNaN(ahead) && ahead > ground) {
                ground = ahead;
            }
            if (!Double.isNaN(ground)) {
                // 3.8.0: the swings hold clear of the terrain (off a low start, over a hill they rise; Shift brings them back down to it)
                cruise = Math.max(cruise, SkyPhysics.floor(ground));
            }
            SkyPhysics.anchor(H, sx, sz, speed, r01, cruise, ground, OUT);
            Vec3 want = new Vec3(OUT[0], OUT[1], OUT[2]);
            Vec3 at = clipped(self, hand, want);
            double length = at == want ? OUT[4] : Math.max(SkyPhysics.SHORTEST, at.m_82554_(hand));
            swingX = sx;
            swingZ = sz;
            throwString(id, r, at, length, true);
            if (at == want) {
                ropeTarget = OUT[3];
            }
        } else {
            SkyPhysics.hangAnchor(H, V[0], V[2], OUT);
            Vec3 want = new Vec3(OUT[0], OUT[1], OUT[2]);
            Vec3 at = clipped(self, hand, want);
            throwString(id, r, at, at == want ? OUT[3] : Math.max(SkyPhysics.SHORTEST, at.m_82554_(hand)), false);
        }
    }

    /** One's own string thrown: held (and its length) once it gets there in THROW ticks; the server told. */
    static void throwString(int id, Runner r, Vec3 at, double length, boolean swing) {
        letGo(r);
        r.rope = at;
        r.ropeBorn = clock;
        r.ropeLength = length;
        ropeTarget = length;
        r.mode = swing ? SWING : HANG;
        since = 0;
        clip(id, r, swing ? SWING_CLIP : HANG_CLIP, 4);
        send(swing ? ItoMoves.ROPE_SWING : ItoMoves.ROPE_HANG, at, length, 0);
    }

    /** A swing let go: kicked on and up (with a key held, under its speed), flying free with a backflip or a split leap until the next string. */
    static void release(int id, Runner r, Vec3 pos, boolean kick) {
        if (kick) {
            SkyPhysics.kick(V);
        }
        letGo(r);
        int trick = FireMesh.rand(id * 7 + (int) clock, 9) < 0.5 ? ItoMoves.SKY_BACKFLIP : ItoMoves.SKY_LEAP;
        trick(id, r, trick);
        since = 0;
        send(ItoMoves.SKY_FLY, pos, 0.0, trick);
    }

    /** A tap of Space: every string let go, a leap along the look to a string caught further away. */
    static void leap(int id, Runner r, double yawDeg, double pitchDeg) {
        SkyPhysics.leap(H, yawDeg, pitchDeg, LEAP_AT, V);
        cruise = LEAP_AT[1];
        letGo(r);
        trick(id, r, ItoMoves.SKY_LEAP);
        r.mode = LEAP;
        since = 0;
        send(ItoMoves.SKY_FLY, new Vec3(H[0], H[1] - SkyPhysics.GRIP, H[2]), 0.0, ItoMoves.SKY_LEAP);
    }

    /** A runner's string let go (it fades where it held). */
    static void letGo(Runner r) {
        if (r.rope != null) {
            r.old = r.rope;
            r.oldDropped = clock;
            r.rope = null;
        }
    }

    /** No string held: let go (a backflip or a split leap), leaping (the split leap) or dropping (arms up). */
    static void trick(int id, Runner r, int trick) {
        if (trick == ItoMoves.SKY_DROP) {
            r.mode = DROP;
            clip(id, r, DROP_CLIP, 4);
            return;
        }
        r.mode = FLY;
        boolean flip = trick == ItoMoves.SKY_BACKFLIP;
        clip(id, r, flip ? BACKFLIP_CLIP : LEAP_CLIP, 2);
        r.trickUntil = clock + (flip ? BACKFLIP_TICKS : LEAP_TICKS);
    }

    /** A step of string under a climbing foot (x, y, z), across the way {@code yaw} faces. */
    static void tread(Runner r, double x, double y, double z, double yaw) {
        int o = r.treadNext * 5;
        r.treads[o] = x;
        r.treads[o + 1] = y;
        r.treads[o + 2] = z;
        r.treads[o + 3] = yaw;
        r.treads[o + 4] = clock;
        r.treadNext = (r.treadNext + 1) % TREADS;
    }

    /** The ground's height under the hand (NaN: none within 48 blocks). */
    static double ground(LocalPlayer self, Vec3 hand) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return Double.NaN;
        }
        BlockHitResult hit = level.m_45547_(new ClipContext(hand, hand.m_82520_(0.0, -48.0, 0.0), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.ANY, self));
        return hit.m_6662_() == HitResult.Type.BLOCK ? hit.m_82450_().f_82480_ : Double.NaN;
    }

    /** Where a string thrown from the hand toward {@code want} holds: there, or on the first block in the way (a ceiling, a wall). */
    static Vec3 clipped(LocalPlayer self, Vec3 hand, Vec3 want) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return want;
        }
        BlockHitResult hit = level.m_45547_(new ClipContext(hand, want, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, self));
        return hit.m_6662_() == HitResult.Type.BLOCK ? hit.m_82450_() : want;
    }

    static void send(int mode, Vec3 at, double length, int extra) {
        PieceNetwork.toServer(new C2SItoSky(mode, at.f_82479_, at.f_82480_, at.f_82481_, (float) length, extra));
    }

    static void clip(int id, Runner r, String name, int fade) {
        if (!name.equals(r.clip)) {
            com.hexhaki.client.anim.HakiAnimations.playRemote(id, name, fade);
            r.clip = name;
            r.clipAt = clock;
        }
    }

    /** Every runner: its momentum from its movement, the clip after a trick, its lean (along its string, or with its speed), its streak. */
    static void follow(int id, Entity body, Runner r) {
        Vec3 pos = body.m_20182_();
        if (r.last != null) {
            Vec3 d = pos.m_82546_(r.last);
            r.vel = r.vel.m_82490_(0.5).m_82549_(d.m_82490_(0.5));
        }
        r.last = pos;
        double hs = Math.sqrt(r.vel.f_82479_ * r.vel.f_82479_ + r.vel.f_82481_ * r.vel.f_82481_);
        r.speed = hs;
        r.turn = 0.0;
        if (hs > 0.05) {
            double heading = Math.toDegrees(Math.atan2(-r.vel.f_82479_, r.vel.f_82481_));
            if (!Double.isNaN(r.heading)) {
                r.turn = wrap(heading - r.heading);
            }
            r.heading = heading;
        }
        // a trick over with no string yet: arms up, falling on to the next
        if ((r.mode == FLY || r.mode == LEAP) && r.trickUntil >= 0 && clock >= r.trickUntil) {
            r.trickUntil = -1;
            clip(id, r, DROP_CLIP, 4);
        }
        // another's climb ends when its steps stop (its next string, or a let go, follows)
        Minecraft minecraft = Minecraft.m_91087_();
        if (r.mode == CLIMB && body != minecraft.f_91074_ && clock - r.lastStep > CLIMB_HOLD) {
            r.mode = FLY;
            clip(id, r, DROP_CLIP, 4);
        }
        // the clip came off (something else played): played again
        if (r.clip != null && clock - r.clipAt > 40 && !r.clip.equals(com.hexhaki.fruit.client.FruitAnimations.clip(body))) {
            com.hexhaki.client.anim.HakiAnimations.playRemote(id, r.clip, 5);
            r.clipAt = clock;
        }
        // the lean: on a held string the whole body along it, turned round the hand; else forward with the speed, banked into turns
        r.pitchWas = r.pitch;
        r.rollWas = r.roll;
        double yaw = Math.toRadians(body instanceof LivingEntity l ? l.f_20883_ : body.m_146908_());
        boolean held = r.rope != null && clock - r.ropeBorn >= SkyPhysics.THROW && (r.mode == SWING || r.mode == HANG);
        double pitchTo;
        double rollTo;
        if (held) {
            Vec3 hand = pos.m_82520_(-Math.cos(yaw) * HAND_OUT, SkyPhysics.GRIP, -Math.sin(yaw) * HAND_OUT);
            Vec3 d = r.rope.m_82546_(hand);
            double dl = Math.max(1.0E-6, d.m_82553_());
            double dx = (d.f_82479_ * Math.cos(yaw) + d.f_82481_ * Math.sin(yaw)) / dl;
            double dz = (-d.f_82479_ * Math.sin(yaw) + d.f_82481_ * Math.cos(yaw)) / dl;
            double dy = d.f_82480_ / dl;
            pitchTo = Math.max(-TILT_MOST, Math.min(TILT_MOST, Math.toDegrees(Math.atan2(dz, Math.max(0.05, dy)))));
            rollTo = Math.max(-TILT_MOST, Math.min(TILT_MOST, Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, -dx))))));
        } else if (r.mode == CLIMB) {
            pitchTo = 8.0;
            rollTo = 0.0;
        } else if (r.mode == DROP) {
            pitchTo = 0.0;
            rollTo = 0.0;
        } else {
            pitchTo = 4.0 + 22.0 * Math.min(1.0, hs / 1.2);
            rollTo = Math.max(-30.0, Math.min(30.0, r.turn * 3.0)) * Math.min(1.0, hs / 0.4);
        }
        r.pitch += (pitchTo - r.pitch) * (held ? 0.4 : 0.18);
        r.roll += (rollTo - r.roll) * (held ? 0.4 : 0.18);
        r.pivotY += ((held ? SkyPhysics.GRIP : 0.9) - r.pivotY) * 0.3;
        r.pivotOut += ((held ? HAND_OUT : 0.0) - r.pivotOut) * 0.3;
        feet(pos, hs, r);
    }

    /** The feet's streak, one place a tick. */
    static void feet(Vec3 pos, double hs, Runner r) {
        if (hs > 0.15 && r.mode != CLIMB) {
            System.arraycopy(r.feet, 0, r.feet, 3, (FEET - 1) * 3);
            r.feet[0] = pos.f_82479_;
            r.feet[1] = pos.f_82480_ + 0.12;
            r.feet[2] = pos.f_82481_;
            r.feetCount = Math.min(FEET, r.feetCount + 1);
        } else if (r.feetCount > 0) {
            r.feetCount--;
        }
    }

    static double wrap(double degrees) {
        double d = degrees % 360.0;
        if (d >= 180.0) {
            d -= 360.0;
        }
        if (d < -180.0) {
            d += 360.0;
        }
        return d;
    }

    // ------------------------------------------------------------------------------------------------------ the body

    /**
     * The body turned along its string round the hand that holds it (a swing's pendulum, the hang settling), or leaned forward with its
     * speed and banked into its turns round its hips (flying, leaping, climbing); the clip moves the limbs.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void lean(RenderPlayerEvent.Pre event) {
        Runner r = RUNNERS.isEmpty() ? null : RUNNERS.get(event.getEntity().m_19879_());
        if (r == null) {
            return;
        }
        LivingEntity body = event.getEntity();
        float partial = event.getPartialTick();
        float yaw = body.f_20884_ + (float) wrap(body.f_20883_ - body.f_20884_) * partial;
        double pitch = r.pitchWas + (r.pitch - r.pitchWas) * partial;
        double roll = r.rollWas + (r.roll - r.rollWas) * partial;
        double y = Math.toRadians(yaw);
        float px = (float) (-Math.cos(y) * r.pivotOut);
        float pz = (float) (-Math.sin(y) * r.pivotOut);
        PoseStack pose = event.getPoseStack();
        pose.m_85836_();
        pose.m_252880_(px, (float) r.pivotY, pz);
        pose.m_252781_(Axis.f_252436_.m_252977_(-yaw));
        pose.m_252781_(Axis.f_252529_.m_252977_((float) pitch));
        pose.m_252781_(Axis.f_252403_.m_252977_((float) roll));
        pose.m_252781_(Axis.f_252436_.m_252977_(yaw));
        pose.m_252880_(-px, (float) -r.pivotY, -pz);
        LEANED.add(event.getEntity().m_19879_());
    }

    static final java.util.Set<Integer> LEANED = new java.util.HashSet<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void unlean(RenderPlayerEvent.Post event) {
        if (!LEANED.isEmpty() && LEANED.remove(event.getEntity().m_19879_())) {
            event.getPoseStack().m_85849_();
        }
    }

    // ------------------------------------------------------------------------------------------------------ one's own view

    /** A slight roll as the string swings one sideways (never a shake). */
    @SubscribeEvent
    public static void roll(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.m_91087_();
        Runner r = minecraft.f_91074_ == null || RUNNERS.isEmpty() ? null : RUNNERS.get(minecraft.f_91074_.m_19879_());
        if (r != null && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON) {
            event.setRoll(event.getRoll() + (float) (r.roll * 0.15));
        }
    }

    /** The view a little wider the faster one goes. */
    @SubscribeEvent
    public static void fov(ViewportEvent.ComputeFov event) {
        if (fovSpeed > 0.01 && event.usedConfiguredFov()) {
            event.setFOV(event.getFOV() * (1.0 + 0.1 * fovSpeed));
        }
    }

    // ------------------------------------------------------------------------------------------------------ the strings

    /** Where a runner's hand on its string is: one's own in first person low right of the view; else the posed fist, or over the shoulder. */
    static Vec3 hand(Minecraft minecraft, Entity body, Runner r, float partial) {
        if (body == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON) {
            Vec3 look = body.m_20252_(partial);
            Vec3 right = look.m_82537_(new Vec3(0, 1, 0));
            right = right.m_82556_() < 1.0E-6 ? new Vec3(-1, 0, 0) : right.m_82541_();
            return body.m_20299_(partial).m_82549_(look.m_82490_(0.4)).m_82549_(right.m_82490_(0.45)).m_82520_(0.0, -0.25, 0.0);
        }
        Vec3 feet = body.m_20318_(partial);
        double yaw = Math.toRadians(body instanceof LivingEntity l ? l.f_20883_ : body.m_146908_());
        boolean held = r.mode == SWING || r.mode == HANG;
        if (held && body instanceof net.minecraft.world.entity.player.Player player && Math.abs(r.pitch) < 3.0 && Math.abs(r.roll) < 3.0) {
            // hanging upright: the hand where the clip holds it
            Vec3 fist = com.hexhaki.piece.client.FirstPersonLight.fist(player, partial);
            if (fist != null) {
                return fist;
            }
        }
        // turned along the string round the hand: the hand stays at the pivot
        return feet.m_82520_(-Math.cos(yaw) * HAND_OUT, held ? SkyPhysics.GRIP : 1.3, -Math.sin(yaw) * HAND_OUT);
    }

    /** Every runner's string (one at most), the one let go fading, its steps of string, its streak (ItoFx draws them in its batch). */
    static void draw(FireMesh.Sink sink, Minecraft minecraft, ClientLevel level, Vec3 cam, float partial) {
        double now = clock + partial;
        for (Map.Entry<Integer, Runner> e : RUNNERS.entrySet()) {
            Entity body = level.m_6815_(e.getKey());
            Runner r = e.getValue();
            if (body == null || body.m_20182_().m_82554_(cam) > 160.0) {
                continue;
            }
            Vec3 hand = hand(minecraft, body, r, partial);
            if (r.rope != null) {
                double grow = Math.min(1.0, (now - r.ropeBorn) / SkyPhysics.THROW);
                Vec3 tip = hand.m_82549_(r.rope.m_82546_(hand).m_82490_(grow));
                // taut as it catches (a flash where it holds), easing after
                double taut = grow >= 1.0 ? Math.max(0.25, 1.0 - (now - r.ropeBorn - SkyPhysics.THROW) / 6.0) : 0.0;
                ItoArt.sky(sink, hand.f_82479_ - cam.f_82479_, hand.f_82480_ - cam.f_82480_, hand.f_82481_ - cam.f_82481_, tip.f_82479_ - cam.f_82479_,
                    tip.f_82480_ - cam.f_82480_, tip.f_82481_ - cam.f_82481_, 0.75 + 0.25 * taut, taut, grow >= 1.0, r.coat);
            }
            if (r.old != null && now - r.oldDropped < DROP_FADE) {
                // let go: it slackens and fades, hanging from where it held
                double f = (now - r.oldDropped) / DROP_FADE;
                Vec3 low = hand.m_82549_(r.old.m_82546_(hand).m_82490_(0.35 + 0.65 * f));
                ItoArt.sky(sink, low.f_82479_ - cam.f_82479_, low.f_82480_ - cam.f_82480_, low.f_82481_ - cam.f_82481_, r.old.f_82479_ - cam.f_82479_,
                    r.old.f_82480_ - cam.f_82480_, r.old.f_82481_ - cam.f_82481_, 0.45 * (1.0 - f), 0.0, false, r.coat);
            }
            for (int k = 0; k < TREADS; k++) {
                int o = k * 5;
                double age = now - r.treads[o + 4];
                if (r.treads[o + 4] > 0 && age >= 0 && age <= ItoArt.TREAD_LIFE) {
                    ItoArt.tread(sink, r.treads[o] - cam.f_82479_, r.treads[o + 1] - cam.f_82480_, r.treads[o + 2] - cam.f_82481_, r.treads[o + 3], age, r.coat);
                }
            }
            if (r.feetCount >= 3 && !(body == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON)) {
                ItoArt.streak(sink, r.feet, r.feetCount, cam.f_82479_, cam.f_82480_, cam.f_82481_, Math.min(1.0, r.speed / 1.2), r.coat);
            }
        }
    }

    @SubscribeEvent
    public static void loggedOut(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        RUNNERS.clear();
        LEANED.clear();
        fovSpeed = 0.0;
    }
}
