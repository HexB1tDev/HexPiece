package com.hexhaki.piece.client.ito;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.ito.ItoArt;
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
 * 3.4.3 (the owner: "Ito Ito No mi double jump tap now has a new move: Sky Road movement ability ... It should NOT resemble ordinary
 * creative-mode flight, elytra flight, or a player simply floating through the air ... they begin running through the sky ... Directional
 * movement must be completely fluid ... Sudden acceleration should produce a brief, powerful forward slingshot ... stop dramatically in
 * midair ... Create a dedicated aerial locomotion system with proper animations, procedural string rendering, momentum-aware body
 * positioning, and smooth transitions. All movement must remain responsive and multiplayer-safe"): Doflamingo's Sora no Michi.
 *
 * <p>Movement: the runner's own client moves its body (as the Light fruit's glide does, so it is smooth on screen and the server's own
 * movement check agrees): every tick, after the game has moved it, the velocity for the next is set from the keys it took from the game
 * ({@link #input}): toward where the keys point (the camera's way; looking up or down climbs or sinks as it runs; jump and sneak go
 * straight up and down), its heading turned toward it at most a few degrees a tick (sharp turns sweep in arcs), its speed easing up to
 * the run's or the sprint's; from a standstill, or breaking into a sprint, a slingshot (the strings taut: a burst forward); no key: it
 * glides to a stop and hangs, bobbing. The server keeps only what it must (server/ItoMoves.sky: the player may fly, no fall counted, the
 * end on the ground or at the time limit).
 *
 * <p>Every client, for every runner it sees: its momentum from its own movement (the speed, the heading, how fast it turns) leans the
 * body forward with the speed and banks it into turns round its hips ({@link #lean}), picks its clip (tools/ito_anims.py: ito_sky_hover,
 * ito_sky_run, ito_sky_sprint, through the original's HakiAnimations.playRemote) and draws its strings ({@link #draw}): one from each
 * hand to an anchor high in the sky ahead, let go and thrown again further ahead as it passes under it, a tiny flash where a string
 * catches and on every stride as it tightens, faint streaks behind the feet. One's own view: a slight roll into turns, the view a
 * little wider at speed; no shaking.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class SkyRoad {
    /** Speeds (blocks a tick): running, sprinting; climbing and sinking; the slingshot's burst (times the speed). */
    static final double RUN = 0.62;
    static final double SPRINT = 1.15;
    static final double CLIMB = 0.42;
    static final double SLING = 1.5;
    /** The most the heading turns a tick (degrees): running, sprinting. */
    static final double TURN_RUN = 11.0;
    static final double TURN_SPRINT = 7.0;
    /** Below this speed a runner hangs (the hover), above this it sprints. */
    static final double HOVER_BELOW = 0.07;
    static final double SPRINT_ABOVE = 0.82;
    /** How long a string takes to shoot up to its anchor, how long one let go fades, how long a flash shows. */
    static final double THROW = 3.0;
    static final double DROP = 5.0;
    static final double FLASH = 4.0;
    static final int FEET = 8;

    /** 3.5.0 (the owner: "If the player is just, like, hanging in midair, then we just make the player, like, grabbing onto a string with
     *  one hand"): hanging still is one hand up on one string overhead (ito_sky_hang); 3.4.3's two-string hover is no longer played. */
    static final String HOVER = "ito_sky_hang";
    /**
     * 3.5.0 (the owner: "I want it to like do break dances in the fucking sky. Like I want it to like jump from rope to rope, like just
     * without like any requirement from the player, just animation wise, like backflip to another string, just skip to another one,
     * jump from one to the other, and like swing, just like grab a string from the sky and swing to the next line"): while it runs, every
     * TRICK_GAP to TRICK_GAP + TRICK_SPREAD ticks a runner lets go of its strings and does a trick on its own (tools/ito_anims.tricks):
     * a backflip, a flying split leap or a skip from string to string (no string held, both thrown again as it lands the trick), or a
     * swing: one string caught overhead ahead, the body swinging under it like a pendulum, let go at the top. Animation only: the body
     * moves as the keys move it. Every client picks them for itself (each runner's own random numbers).
     */
    static final String[] TRICKS = {"ito_sky_backflip", "ito_sky_leap", "ito_sky_skip", "ito_sky_swing"};
    static final int[] TRICK_TICKS = {14, 12, 10, 20};
    static final int SWING = 3;
    static final int TRICK_GAP = 36;
    static final int TRICK_SPREAD = 44;
    static final String RUN_CLIP = "ito_sky_run";
    static final String SPRINT_CLIP = "ito_sky_sprint";

    static final class Anchor {
        final Vec3 at;
        final long born;
        long dropped = -1;

        Anchor(Vec3 at, long born) {
            this.at = at;
            this.born = born;
        }
    }

    /** One runner as this client sees it. */
    static final class Runner {
        long born;
        long until;
        boolean confirmed;
        boolean coat;
        Vec3 last;
        Vec3 vel = Vec3.f_82478_;
        double heading = Double.NaN;
        double bank;
        double lean;
        double speed;
        int sling;
        String clip;
        long clipAt;
        /** 3.5.0: the trick playing (an index into TRICKS, or -1), until when, the next one's earliest tick, how many so far. */
        int trick = -1;
        long trickUntil;
        long nextTrick = -1;
        int tricks;
        double phase;
        final Anchor[] anchors = new Anchor[2];
        final Anchor[] old = new Anchor[2];
        final long[] flash = {-100, -100};
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
    static Vec3 own = Vec3.f_82478_;
    static boolean wasSprint;
    static double fovSpeed;

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

    /** One's own air jump sent (client/FruitKeyN): started at once (the server's word follows; unanswered, it stops in 10 ticks). */
    public static void startLocal(LocalPlayer self) {
        if (RUNNERS.containsKey(self.m_19879_())) {
            return;
        }
        Runner r = new Runner();
        r.born = clock;
        r.until = clock + ItoMoves.SKY_MOST;
        RUNNERS.put(self.m_19879_(), r);
        own = self.m_20184_();
        wasSprint = false;
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

    /** One's own runner: the next tick's velocity from the keys (momentum, arcs, the slingshot, the hover). */
    static void drive(Minecraft minecraft, LocalPlayer self, Runner r) {
        double yaw = Math.toRadians(self.m_146908_());
        double pitch = Math.toRadians(self.m_146909_());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double lx = Math.cos(yaw);
        double lz = Math.sin(yaw);
        double wx = fx * forward + lx * left;
        double wz = fz * forward + lz * left;
        double wl = Math.sqrt(wx * wx + wz * wz);
        boolean sprint = minecraft.f_91066_.f_92091_.m_90857_() || self.m_20142_();
        double top = sprint ? SPRINT : RUN;
        double vx = own.f_82479_;
        double vy = own.f_82480_;
        double vz = own.f_82481_;
        double speed = Math.sqrt(vx * vx + vz * vz);
        if (wl > 0.01) {
            double dx = wx / wl;
            double dz = wz / wl;
            double want = top * Math.min(1.0, wl);
            if (speed < 0.12) {
                // from a standstill: the strings snap taut and fling the body forward
                vx = dx * want * SLING;
                vz = dz * want * SLING;
                r.sling = 8;
                r.flash[0] = clock;
                r.flash[1] = clock;
            } else {
                double cur = Math.atan2(vz, vx);
                double turn = Math.atan2(dz, dx) - cur;
                while (turn > Math.PI) {
                    turn -= 2 * Math.PI;
                }
                while (turn < -Math.PI) {
                    turn += 2 * Math.PI;
                }
                double most = Math.toRadians(sprint ? TURN_SPRINT : TURN_RUN);
                cur += Math.max(-most, Math.min(most, turn));
                double next = speed + Math.max(-0.1, Math.min(0.07, want - speed));
                if (sprint && !wasSprint && speed > 0.2) {
                    next = Math.max(next, top * 1.35); // breaking into a sprint: another slingshot
                    r.sling = 6;
                    r.flash[0] = clock;
                    r.flash[1] = clock;
                }
                vx = Math.cos(cur) * next;
                vz = Math.sin(cur) * next;
            }
        } else {
            vx *= 0.84;
            vz *= 0.84;
        }
        double wantY = ((up ? 1.0 : 0.0) - (down ? 1.0 : 0.0)) * CLIMB;
        if (forward > 0.0F) {
            wantY += -Math.sin(pitch) * forward * Math.min(top, Math.max(speed, 0.2)) * 0.6; // looking up as it runs climbs
        }
        vy += (wantY - vy) * 0.25;
        if (wantY == 0.0 && wl < 0.01) {
            vy = vy * 0.8 + 0.012 * Math.sin(clock * 0.12); // hanging: a gentle bob on the strings
        }
        if (self.m_20096_() && !up) {
            vy = Math.min(vy, 0.0) - 0.08; // on the ground: it settles (and the server ends the run)
        }
        if (r.sling > 0) {
            r.sling--;
        }
        self.m_20334_(vx, vy, vz);
        self.f_19789_ = 0.0F;
        if (!self.m_7500_() && self.m_150110_().f_35935_) {
            self.m_150110_().f_35935_ = false; // never the game's own flight (a double tap of jump while the strings hold it)
        }
        own = new Vec3(vx, vy, vz);
        wasSprint = sprint;
        fovSpeed += (Math.min(1.0, Math.sqrt(vx * vx + vz * vz) / SPRINT) - fovSpeed) * 0.15;
    }

    /** Every runner: its momentum from its movement, its lean and bank, its clip, its strings' anchors, its feet's streak. */
    static void follow(int id, Entity body, Runner r) {
        Vec3 pos = body.m_20182_();
        if (r.last != null) {
            Vec3 d = pos.m_82546_(r.last);
            r.vel = r.vel.m_82490_(0.5).m_82549_(d.m_82490_(0.5));
        }
        r.last = pos;
        double hs = Math.sqrt(r.vel.f_82479_ * r.vel.f_82479_ + r.vel.f_82481_ * r.vel.f_82481_);
        r.speed = hs;
        double turn = 0.0;
        if (hs > 0.05) {
            double heading = Math.toDegrees(Math.atan2(-r.vel.f_82479_, r.vel.f_82481_));
            if (!Double.isNaN(r.heading)) {
                turn = wrap(heading - r.heading);
            }
            r.heading = heading;
        }
        // momentum: banked into the turn (turning right leans right), leaned forward the faster it goes, harder in a slingshot
        double bankTo = Math.max(-35.0, Math.min(35.0, turn * 3.0)) * Math.min(1.0, hs / 0.4);
        r.bank += (bankTo - r.bank) * 0.18;
        double leanTo = 5.0 + 26.0 * Math.min(1.0, hs / SPRINT) + (r.sling > 0 ? 10.0 : 0.0);
        r.lean += (leanTo - r.lean) * 0.15;
        // 3.5.0: a trick on its own now and then while it runs (its strings let go), the swing on one string caught overhead
        if (r.trick >= 0 && clock >= r.trickUntil) {
            r.trick = -1;
            r.clip = null; // back to running: the clip and both strings come back below
            for (int k = 0; k < 2; k++) {
                if (r.anchors[k] != null) {
                    r.anchors[k].dropped = clock;
                    r.old[k] = r.anchors[k];
                    r.anchors[k] = null;
                }
            }
        }
        if (r.nextTrick < 0) {
            r.nextTrick = clock + TRICK_GAP + (long) (TRICK_SPREAD * FireMesh.rand(id * 31, 17));
        }
        if (r.trick < 0 && hs >= HOVER_BELOW * 1.6 && clock >= r.nextTrick) {
            int t = (int) Math.min(TRICKS.length - 1, FireMesh.rand(id * 13 + r.tricks, 5) * TRICKS.length);
            r.trick = t;
            r.tricks++;
            r.trickUntil = clock + TRICK_TICKS[t];
            r.nextTrick = r.trickUntil + TRICK_GAP + (long) (TRICK_SPREAD * FireMesh.rand(id * 31 + r.tricks, 17));
            com.hexhaki.client.anim.HakiAnimations.playRemote(id, TRICKS[t], 3);
            r.clip = TRICKS[t];
            r.clipAt = clock;
            double dir0 = Double.isNaN(r.heading) ? Math.toRadians(-body.m_146908_()) : Math.toRadians(r.heading);
            for (int k = 0; k < 2; k++) {
                if (r.anchors[k] != null) {
                    r.anchors[k].dropped = clock;
                    r.old[k] = r.anchors[k];
                    r.anchors[k] = null;
                }
            }
            if (t == SWING) {
                // the string caught for the swing: high over the way ahead, so the body swings under it and on
                r.anchors[0] = new Anchor(pos.m_82520_(-Math.sin(dir0) * (2.0 + hs * 10.0), 9.0, Math.cos(dir0) * (2.0 + hs * 10.0)), clock);
                r.flash[0] = clock + (long) THROW;
            }
        }
        if (r.trick >= 0) {
            feet(pos, hs, r);
            return;
        }
        // its clip: hanging, running, sprinting (a little hysteresis so it never flickers between two)
        String want;
        if (hs < HOVER_BELOW || HOVER.equals(r.clip) && hs < HOVER_BELOW * 1.6) {
            want = HOVER;
        } else if (hs > SPRINT_ABOVE || SPRINT_CLIP.equals(r.clip) && hs > SPRINT_ABOVE * 0.85) {
            want = SPRINT_CLIP;
        } else {
            want = RUN_CLIP;
        }
        if (!want.equals(r.clip) || clock - r.clipAt > 40 && !want.equals(com.hexhaki.fruit.client.FruitAnimations.clip(body))) {
            com.hexhaki.client.anim.HakiAnimations.playRemote(id, want, 5);
            r.clip = want;
            r.clipAt = clock;
            r.phase = 0.0;
        }
        // every stride the strings tighten (a flash where they hold)
        double before = r.phase;
        r.phase += RUN_CLIP.equals(r.clip) ? 1.0 / 16.0 : SPRINT_CLIP.equals(r.clip) ? 1.0 / 10.0 : 0.0;
        if (Math.floor(before * 2.0) != Math.floor(r.phase * 2.0)) {
            r.flash[(int) Math.floor(r.phase * 2.0) % 2] = clock;
        }
        // the strings' anchors: high above, ahead along the way it goes; passed under (or too far), let go and thrown again ahead
        double dir = Double.isNaN(r.heading) ? Math.toRadians(-body.m_146908_()) : Math.toRadians(r.heading);
        double hx = -Math.sin(dir);
        double hz = Math.cos(dir);
        boolean hanging = HOVER.equals(r.clip);
        for (int k = 0; k < 2; k++) {
            Anchor a = r.anchors[k];
            if (hanging && k == 1) {
                // 3.5.0: one hand on one string while it hangs: the left lets go
                if (a != null) {
                    a.dropped = clock;
                    r.old[k] = a;
                    r.anchors[k] = null;
                }
                continue;
            }
            boolean again;
            if (a == null) {
                again = true;
            } else {
                Vec3 to = a.at.m_82546_(pos);
                double ahead = to.f_82479_ * hx + to.f_82481_ * hz;
                double flat = Math.sqrt(to.f_82479_ * to.f_82479_ + to.f_82481_ * to.f_82481_);
                again = hs >= HOVER_BELOW ? ahead < 1.5 || to.m_82553_() > 36.0 : flat > 9.0 || clock - a.born > 200;
            }
            if (again) {
                if (a != null) {
                    a.dropped = clock;
                    r.old[k] = a;
                }
                double side = hanging ? -0.25 : k == 0 ? -1.0 : 1.0; // the right hand's anchor to the right of the way, the left's to the left
                double reach = hs >= HOVER_BELOW ? 6.0 + hs * 16.0 : 0.0;
                double rise = 14.0 + 4.0 * FireMesh.rand(id * 7 + (int) clock, k);
                Vec3 at = pos.m_82520_(hx * reach + hz * side * 2.6, rise, hz * reach - hx * side * 2.6);
                r.anchors[k] = new Anchor(at, clock);
                r.flash[k] = clock + (long) THROW;
            }
        }
        feet(pos, hs, r);
    }

    /** The feet's streak, one place a tick. */
    static void feet(Vec3 pos, double hs, Runner r) {
        if (hs > 0.15) {
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

    /** The body leaned forward with its speed and banked into its turns round its hips (the clip moves the limbs). */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void lean(RenderPlayerEvent.Pre event) {
        Runner r = RUNNERS.isEmpty() ? null : RUNNERS.get(event.getEntity().m_19879_());
        if (r == null) {
            return;
        }
        LivingEntity body = event.getEntity();
        float partial = event.getPartialTick();
        float yaw = body.f_20884_ + (float) wrap(body.f_20883_ - body.f_20884_) * partial;
        double sway = r.speed < HOVER_BELOW ? 1.5 * Math.sin((clock + partial) * 0.09) : 0.0;
        PoseStack pose = event.getPoseStack();
        pose.m_85836_();
        pose.m_252880_(0.0F, 0.9F, 0.0F);
        pose.m_252781_(Axis.f_252436_.m_252977_(-yaw));
        pose.m_252781_(Axis.f_252529_.m_252977_((float) r.lean));
        pose.m_252781_(Axis.f_252403_.m_252977_((float) (r.bank + sway)));
        pose.m_252781_(Axis.f_252436_.m_252977_(yaw));
        pose.m_252880_(0.0F, -0.9F, 0.0F);
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

    /** A slight roll into turns (never a shake). */
    @SubscribeEvent
    public static void roll(ViewportEvent.ComputeCameraAngles event) {
        Minecraft minecraft = Minecraft.m_91087_();
        Runner r = minecraft.f_91074_ == null || RUNNERS.isEmpty() ? null : RUNNERS.get(minecraft.f_91074_.m_19879_());
        if (r != null && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON) {
            event.setRoll(event.getRoll() + (float) (r.bank * 0.22));
        }
    }

    /** The view a little wider the faster one runs. */
    @SubscribeEvent
    public static void fov(ViewportEvent.ComputeFov event) {
        if (fovSpeed > 0.01 && event.usedConfiguredFov()) {
            event.setFOV(event.getFOV() * (1.0 + 0.09 * fovSpeed));
        }
    }

    // ------------------------------------------------------------------------------------------------------ the strings

    /** Where a runner's hand is (0 the right, 1 the left), the arms spread out: at its sides, a little forward. */
    static Vec3 hand(Minecraft minecraft, Entity body, Runner r, int k, float partial) {
        Vec3 feet = body.m_20318_(partial);
        double side = k == 0 ? -1.0 : 1.0;
        if (body == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON) {
            Vec3 look = body.m_20252_(partial);
            Vec3 right = look.m_82537_(new Vec3(0, 1, 0));
            right = right.m_82556_() < 1.0E-6 ? new Vec3(-1, 0, 0) : right.m_82541_();
            return body.m_20299_(partial).m_82549_(look.m_82490_(0.4)).m_82549_(right.m_82490_(-side * 0.55)).m_82520_(0.0, -0.3, 0.0);
        }
        if (k == 0 && (HOVER.equals(r.clip) || r.trick == SWING) && body instanceof net.minecraft.world.entity.player.Player player) {
            // 3.5.0: the hand up on the string: where the clip holds it (the hang, the swing)
            Vec3 fist = com.hexhaki.piece.client.FirstPersonLight.fist(player, partial);
            if (fist != null) {
                return fist;
            }
        }
        double yaw = Math.toRadians(body instanceof LivingEntity l ? l.f_20883_ : body.m_146908_());
        double rx = -Math.cos(yaw);
        double rz = -Math.sin(yaw);
        double out = r.speed < HOVER_BELOW ? 0.45 : 0.75;
        return feet.m_82520_(-rx * side * out - Math.sin(yaw) * 0.1, r.speed < HOVER_BELOW ? 0.85 : 1.05, -rz * side * out + Math.cos(yaw) * 0.1);
    }

    /** Every runner's strings, flashes and streaks (ItoFx draws them in its batch). */
    static void draw(FireMesh.Sink sink, Minecraft minecraft, ClientLevel level, Vec3 cam, float partial) {
        double now = clock + partial;
        for (Map.Entry<Integer, Runner> e : RUNNERS.entrySet()) {
            Entity body = level.m_6815_(e.getKey());
            Runner r = e.getValue();
            if (body == null || body.m_20182_().m_82554_(cam) > 160.0) {
                continue;
            }
            for (int k = 0; k < 2; k++) {
                Vec3 hand = hand(minecraft, body, r, k, partial);
                Anchor a = r.anchors[k];
                if (a != null) {
                    double grow = Math.min(1.0, (now - a.born) / THROW);
                    Vec3 tip = hand.m_82549_(a.at.m_82546_(hand).m_82490_(grow));
                    double taut = Math.max(0.0, 1.0 - (now - r.flash[k]) / FLASH);
                    ItoArt.sky(sink, hand.f_82479_ - cam.f_82479_, hand.f_82480_ - cam.f_82480_, hand.f_82481_ - cam.f_82481_, tip.f_82479_ - cam.f_82479_,
                        tip.f_82480_ - cam.f_82480_, tip.f_82481_ - cam.f_82481_, 0.55 + 0.35 * taut, taut, grow >= 1.0, r.coat);
                }
                Anchor o = r.old[k];
                if (o != null && o.dropped >= 0 && now - o.dropped < DROP) {
                    // let go: it slackens and fades, hanging from where it held
                    double f = (now - o.dropped) / DROP;
                    Vec3 low = hand.m_82549_(o.at.m_82546_(hand).m_82490_(0.35 + 0.65 * f));
                    ItoArt.sky(sink, low.f_82479_ - cam.f_82479_, low.f_82480_ - cam.f_82480_, low.f_82481_ - cam.f_82481_, o.at.f_82479_ - cam.f_82479_,
                        o.at.f_82480_ - cam.f_82480_, o.at.f_82481_ - cam.f_82481_, 0.45 * (1.0 - f), 0.0, false, r.coat);
                }
            }
            if (r.feetCount >= 3 && !(body == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON)) {
                ItoArt.streak(sink, r.feet, r.feetCount, cam.f_82479_, cam.f_82480_, cam.f_82481_, Math.min(1.0, r.speed / SPRINT), r.coat);
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
