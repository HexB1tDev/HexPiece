package com.hexhaki.piece.client.phoenix;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.phoenix.PhoenixBlast;
import com.hexhaki.piece.phoenix.PhoenixFire;
import com.hexhaki.piece.phoenix.PhoenixForm;
import com.hexhaki.piece.phoenix.PhoenixMoveArt;
import com.hexhaki.piece.server.PhoenixMoves;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 3.7.2: the Phoenix's moves on this client (server/PhoenixMoves' messages, kinds 401 to 405, handed on by PhoenixFx.accept): every fire
 * effect under way (drawn with the tongues after the particles: PhoenixFx.fire), what follows its user (the comet round the one dashing,
 * the wind and the burning leg at the top of Talon Slam, the streaks round Hawk Rush), the phoenix's pose for a move (its act: PhoenixFx
 * poses it) and the human's temporary phoenix wings (PhoenixFx draws them). The fire itself is phoenix/PhoenixMoveArt.
 */
final class PhoenixMoveFx {
    private PhoenixMoveFx() {
    }

    /** One effect: what it is, its phase, its user, when it began (PhoenixFx's clock), where, its way, how long. */
    static final class Fx {
        int kind;
        int phase;
        int user;
        int id;
        long start;
        double x;
        double y;
        double z;
        double ax;
        double ay;
        double az;
        boolean bird;
        int ticks;
        /** For an effect that follows its user: whether it is still on. */
        boolean on = true;
        /** 3.7.3: a ball's heart, a blast's radius. */
        double size;
    }

    /** A phoenix's pose for a move (server/PhoenixMoves.ACT_*), since when, for how long. */
    static final class Act {
        int act;
        long start;
        int ticks;
    }

    /** A human's temporary phoenix wings: since when, how long, their pose. */
    static final class Wings {
        long start;
        int ticks;
        int pose;
    }

    static final List<Fx> FX = new ArrayList<>();
    static final Map<Integer, Act> ACTS = new HashMap<>();
    static final Map<Integer, Wings> WINGS = new HashMap<>();

    static void accept(S2CUoFx m) {
        long now = PhoenixFx.clock;
        int phase = Math.round(m.bx());
        switch (m.kind()) {
            case PhoenixMoves.FLAME -> PhoenixBreath.accept(m.user(), phase == 1); // 3.7.7: the flamethrower
            case PhoenixMoves.ACT -> {
                if (phase == 0) {
                    ACTS.remove(m.user());
                } else {
                    Act a = new Act();
                    a.act = phase;
                    a.start = now;
                    a.ticks = Math.max(1, m.ticks());
                    ACTS.put(m.user(), a);
                }
            }
            case PhoenixMoves.WINGS -> {
                Wings had = WINGS.get(m.user());
                if (phase == 0) {
                    WINGS.remove(m.user());
                } else if (had != null && now - had.start < had.ticks) {
                    // 3.7.6: wings already out (or still burning away): kept, held on for this move's ticks (they never burn in twice)
                    had.ticks = (int) (now - had.start) + Math.max(1, m.ticks());
                    had.pose = Math.round(m.by());
                } else {
                    Wings w = new Wings();
                    w.start = now;
                    w.ticks = Math.max(1, m.ticks());
                    w.pose = Math.round(m.by());
                    WINGS.put(m.user(), w);
                }
            }
            default -> {
                // a later phase ends what followed the user before it (the dash's comet, the burning leg, the rush)
                if (m.kind() == PhoenixMoves.TALON && phase == PhoenixMoves.TALON_BLAST || m.kind() == PhoenixMoves.HAWK
                    && (phase == PhoenixMoves.HAWK_END || phase == PhoenixMoves.HAWK_GRAB) || m.kind() == PhoenixMoves.FUJI
                    && phase == PhoenixMoves.FUJI_END || m.kind() == PhoenixMoves.INFERNO && phase == PhoenixMoves.VORTEX_BLAST) {
                    for (Fx f : FX) {
                        if (f.user == m.user() && f.kind == m.kind()) {
                            f.on = false;
                        }
                    }
                }
                // 3.7.3: Bluebird: a ball fired ends the charge; its blast ends the ball (by its id)
                if (m.kind() == PhoenixMoves.BLUE) {
                    for (Fx f : FX) {
                        if (f.kind == PhoenixMoves.BLUE && (f.phase == PhoenixMoves.BLUE_CHARGE && f.user == m.user() && phase != PhoenixMoves.BLUE_BOOM
                            || f.phase == PhoenixMoves.BLUE_FIRE && phase == PhoenixMoves.BLUE_BOOM && f.id == m.id())) {
                            f.on = false;
                        }
                    }
                }
                if (m.kind() == PhoenixMoves.HAWK && phase == PhoenixMoves.HAWK_END || m.kind() == PhoenixMoves.BLUE && phase == PhoenixMoves.BLUE_OFF) {
                    return;
                }
                Fx f = new Fx();
                f.kind = m.kind();
                f.phase = phase;
                f.user = m.user();
                f.id = m.id();
                f.start = now;
                f.x = m.x();
                f.y = m.y();
                f.z = m.z();
                f.ax = m.ax();
                f.ay = m.ay();
                f.az = m.az();
                f.bird = m.by() > 0.5F;
                f.ticks = m.ticks();
                f.size = m.size();
                FX.add(f);
            }
        }
    }

    /** How long an effect is drawn (ticks) after it begins. */
    static int life(Fx f) {
        return switch (f.kind) {
            case PhoenixMoves.FUJI -> f.phase == PhoenixMoves.FUJI_END ? PhoenixMoveArt.FUJI_BURST : f.phase == PhoenixMoves.FUJI_DASH ? f.ticks + 16
                : f.phase == PhoenixMoves.FUJI_TORNADO ? f.ticks + TORNADO_OUT : f.ticks + 2;
            case PhoenixMoves.TALON -> f.phase == PhoenixMoves.TALON_BLAST ? PhoenixMoveArt.TALON_CRATER : f.phase == PhoenixMoves.TALON_TOP ? 40
                : 12;
            case PhoenixMoves.HAWK -> f.phase == PhoenixMoves.HAWK_RUSH ? PhoenixMoveArt.HAWK_MOST + 4 : f.phase == PhoenixMoves.HAWK_GRAB
                ? PhoenixMoveArt.HAWK_WHIRL + 6 : f.phase == PhoenixMoves.HAWK_THROW ? (int) PhoenixBlast.BURST_LIFE : PhoenixMoveArt.HAWK_WRAP + 4;
            case PhoenixMoves.INFERNO -> switch (f.phase) {
                case PhoenixMoves.VORTEX_ON -> PhoenixMoveArt.VORTEX + 8;
                case PhoenixMoves.VORTEX_BLAST -> PhoenixMoveArt.VORTEX_BLAST_LIFE;
                case PhoenixMoves.VORTEX_RISE, PhoenixMoves.VORTEX_DIVE -> 14;
                default -> 14;
            };
            case PhoenixMoves.BLUE -> f.phase == PhoenixMoves.BLUE_BOOM ? (int) PhoenixBlast.BURST_LIFE : f.phase == PhoenixMoves.BLUE_CHARGE
                ? PhoenixMoveArt.BLUE_MOST + 30 : f.ticks + 2;
            default -> 0;
        };
    }

    static void tick() {
        long now = PhoenixFx.clock;
        Iterator<Fx> it = FX.iterator();
        while (it.hasNext()) {
            Fx f = it.next();
            if (now - f.start > life(f) || !f.on && follows(f) && now - f.start > 2) {
                it.remove();
            }
        }
        ACTS.values().removeIf(a -> now - a.start > a.ticks + 2);
        WINGS.values().removeIf(w -> now - w.start > w.ticks + PhoenixForm.ARM_LAST + 1);
    }

    /** Whether an effect follows its user (it ends when the next phase comes). */
    static boolean follows(Fx f) {
        return f.kind == PhoenixMoves.TALON && f.phase == PhoenixMoves.TALON_TOP || f.kind == PhoenixMoves.HAWK && f.phase == PhoenixMoves.HAWK_RUSH
            || f.kind == PhoenixMoves.BLUE && f.phase != PhoenixMoves.BLUE_BOOM || f.kind == PhoenixMoves.INFERNO && f.phase == PhoenixMoves.VORTEX_DIVE;
    }

    /** 3.7.5: how long the fire tornado takes to rise and to die out (ticks). */
    static final int TORNADO_IN = 8;
    static final int TORNADO_OUT = 12;


    /**
     * A phoenix's move pose into {@code mo} (PhoenixForm.Motion's sweep, ram, reach), {@code t} its clock (ticks, with the partial):
     * returns the extra turn of its facing (degrees; 3.7.5 always none: the owner, "the phoenix should never do a 360", "the moves should
     * never make the bird spin a 360") and puts the glide's bank into {@code bank[0]}.
     */
    static float act(int user, float t, PhoenixForm.Motion mo, float[] bank) {
        mo.sweep = 0.0F;
        mo.ram = 0.0F;
        mo.reach = 0.0F;
        mo.spread = 0.0F;
        bank[0] = 0.0F;
        Act a = ACTS.get(user);
        if (a == null) {
            return 0.0F;
        }
        float age = t - a.start;
        float f = Math.max(0.0F, Math.min(1.0F, age / a.ticks));
        float in = Math.min(1.0F, age / 3.0F);
        float out = Math.min(1.0F, Math.max(0.0F, (a.ticks + 2.0F - age) / 3.0F));
        float k = Math.min(in, out);
        switch (a.act) {
            case PhoenixMoves.ACT_SWEEP -> mo.sweep = k;
            case PhoenixMoves.ACT_RAM -> mo.ram = k;
            case PhoenixMoves.ACT_REACH -> mo.reach = k;
            case PhoenixMoves.ACT_GLIDE -> {
                mo.glide = Math.max(mo.glide, k);
                bank[0] = 38.0F * (float) Math.sin(Math.PI * f);
            }
            // 3.7.5: the old whirl and spin (a server of 3.7.2 to 3.7.4) are held without turning
            case PhoenixMoves.ACT_WHIRL -> mo.reach = k;
            case PhoenixMoves.ACT_SPREAD, PhoenixMoves.ACT_SPIN -> mo.spread = k;
            default -> {
            }
        }
        return 0.0F;
    }

    /** Every effect under way, eye-relative ({@code cam} the camera), {@code partial} into the tick. */
    static void draw(FireMesh.Sink sink, ClientLevel level, Vec3 cam, float partial) {
        float now = PhoenixFx.clock + partial;
        for (Fx f : FX) {
            float age = now - f.start;
            if (age < 0.0F) {
                continue;
            }
            double x = f.x - cam.f_82479_;
            double y = f.y - cam.f_82480_;
            double z = f.z - cam.f_82481_;
            Entity user = level.m_6815_(f.user);
            Vec3 at = user == null ? null : new Vec3(user.f_19790_ + (user.m_20185_() - user.f_19790_) * partial - cam.f_82479_,
                user.f_19791_ + (user.m_20186_() - user.f_19791_) * partial - cam.f_82480_,
                user.f_19792_ + (user.m_20189_() - user.f_19792_) * partial - cam.f_82481_);
            double big = f.bird ? 2.0 : 1.0;
            switch (f.kind) {
                case PhoenixMoves.FUJI -> fuji(sink, f, age, x, y, z, at, big);
                case PhoenixMoves.TALON -> talon(sink, f, age, x, y, z, at, user, partial);
                case PhoenixMoves.HAWK -> hawk(sink, f, age, x, y, z, at, level);
                case PhoenixMoves.INFERNO -> inferno(sink, f, age, x, y, z, at);
                case PhoenixMoves.BLUE -> blue(sink, f, age, x, y, z, user, partial, cam);
                default -> {
                }
            }
        }
        PhoenixBreath.draw(sink, level, cam, partial);
    }

    static void fuji(FireMesh.Sink sink, Fx f, float age, double x, double y, double z, Vec3 at, double big) {
        if (f.phase == PhoenixMoves.FUJI_CHARGE) {
            if (f.bird) {
                // the phoenix: flames scattering off it as its wings sweep forward
                if (at != null) {
                    PhoenixMoveArt.fujiCharge(sink, at.f_82479_, at.f_82480_ + 0.6, at.f_82481_, age, 2.2, f.id);
                }
            } else {
                PhoenixMoveArt.fujiCharge(sink, x, y, z, age, 1.0, f.id);
            }
        } else if (f.phase == PhoenixMoves.FUJI_TORNADO) {
            // 3.7.5: the phoenix's fire tornado, its foot running on the way it was sent, rising in and dying out
            double st = Math.min(1.0, age / TORNADO_IN) * Math.max(0.0, Math.min(1.0, (f.ticks + TORNADO_OUT - age) / TORNADO_OUT));
            double run = Math.min(age, f.ticks);
            PhoenixBlast.tornado(sink, x + f.ax * run, y + f.ay * run, z + f.az * run, age, st, f.size > 0.5 ? f.size : 1.0, f.id);
        } else if (f.phase == PhoenixMoves.FUJI_DASH) {
            double got = Math.min(1.0, age / Math.max(1, f.ticks));
            PhoenixMoveArt.fujiTrail(sink, x, y, z, f.ax, f.ay, f.az, got, age, big, f.id);
            double l = Math.sqrt(f.ax * f.ax + f.ay * f.ay + f.az * f.az);
            if (at != null && age < f.ticks && l > 1.0E-3) {
                PhoenixMoveArt.fujiSheath(sink, at.f_82479_, at.f_82480_ + (f.bird ? 1.6 : 1.0), at.f_82481_, f.ax / l, f.ay / l, f.az / l, age, big,
                    f.id);
            }
        } else {
            double l = Math.sqrt(f.ax * f.ax + f.az * f.az);
            PhoenixMoveArt.fujiBurst(sink, x, y, z, l < 1.0E-3 ? 0.0 : f.ax / l, l < 1.0E-3 ? 1.0 : f.az / l, age, big, f.id);
        }
    }

    static void talon(FireMesh.Sink sink, Fx f, float age, double x, double y, double z, Vec3 at, Entity user, float partial) {
        switch (f.phase) {
            case PhoenixMoves.TALON_CROUCH -> PhoenixMoveArt.fujiCharge(sink, x, y, z, age * 2.0, 0.7, f.id);
            case PhoenixMoves.TALON_LEAP -> PhoenixMoveArt.talonLeap(sink, x, y, z, age);
            case PhoenixMoves.TALON_TOP -> {
                if (at == null || !f.on) {
                    return;
                }
                if (age < f.ticks + 4) {
                    PhoenixMoveArt.talonWind(sink, at.f_82479_, at.f_82480_ + 1.0, at.f_82481_, age);
                }
                // the right leg lit (the clip phoenix_talon's leg: PhoenixMoveArt.talonLeg), from the top to the blast
                float clip = PhoenixMoveArt.TALON_CROUCH + PhoenixMoveArt.TALON_RISE + age;
                double yaw = user instanceof LivingEntity body ? Math.toRadians(body.f_20884_ + PhoenixFx.wrap(body.f_20883_ - body.f_20884_) * partial)
                    : 0.0;
                double rx = -Math.cos(yaw);
                double rz = -Math.sin(yaw);
                double fx = -Math.sin(yaw);
                double fz = Math.cos(yaw);
                double a = Math.toRadians(-PhoenixMoveArt.talonLeg(clip));
                double hx = at.f_82479_ + rx * 0.12;
                double hy = at.f_82480_ + 0.75;
                double hz = at.f_82481_ + rz * 0.12;
                double dy = -Math.cos(a);
                double dh = Math.sin(a);
                PhoenixMoveArt.legFire(sink, hx, hy, hz, hx + fx * dh * 0.72, hy + dy * 0.72, hz + fz * dh * 0.72, age, Math.min(1.0, age / 4.0), f.id);
            }
            default -> PhoenixMoveArt.talonBlast(sink, x, y, z, age, PhoenixMoveArt.TALON_RADIUS, f.id);
        }
    }

    static void hawk(FireMesh.Sink sink, Fx f, float age, double x, double y, double z, Vec3 at, ClientLevel level) {
        switch (f.phase) {
            case PhoenixMoves.HAWK_WRAP -> {
                if (at != null) {
                    PhoenixMoveArt.hawkFlash(sink, at.f_82479_, at.f_82480_ + 1.4, at.f_82481_, age);
                }
            }
            case PhoenixMoves.HAWK_RUSH -> {
                if (at == null || !f.on) {
                    return;
                }
                double l = Math.sqrt(f.ax * f.ax + f.ay * f.ay + f.az * f.az);
                if (l < 1.0E-3) {
                    return;
                }
                Entity e = level.m_6815_(f.user);
                Vec3 look = e != null ? e.m_20154_() : new Vec3(f.ax / l, f.ay / l, f.az / l);
                PhoenixMoveArt.hawkStreaks(sink, at.f_82479_, at.f_82480_ + 1.2, at.f_82481_, look.f_82479_, look.f_82480_, look.f_82481_, age, f.id);
                PhoenixMoveArt.fujiSheath(sink, at.f_82479_, at.f_82480_ + 1.2, at.f_82481_, look.f_82479_, look.f_82480_, look.f_82481_, age, 1.3, f.id);
            }
            case PhoenixMoves.HAWK_THROW -> {
                // 3.7.5: the one thrown goes in a blast of fire
                PhoenixBlast.explosion(sink, x, y, z, 2.6, age, f.id);
            }
            default -> {
                if (at != null) {
                    PhoenixMoveArt.hawkWhirl(sink, at.f_82479_, at.f_82480_ + 0.8, at.f_82481_, age);
                }
                PhoenixFire.touchdown(sink, x, y - 1.0, z, age * 0.7, f.id);
            }
        }
    }

    /** 3.7.3: Phoenix Inferno's fire: the wings forming, each kick, the landing burst; the whirlpool, the rise, the dive, the blast. */
    static void inferno(FireMesh.Sink sink, Fx f, float age, double x, double y, double z, Vec3 at) {
        switch (f.phase) {
            case PhoenixMoves.INFERNO_FORM, PhoenixMoves.INFERNO_LAND -> PhoenixMoveArt.burst(sink, x, y + (f.phase == PhoenixMoves.INFERNO_LAND ? 0.8 : 0.0),
                z, age, 1.0, f.id);
            case PhoenixMoves.INFERNO_KICK -> {
                double l = Math.sqrt(f.ax * f.ax + f.az * f.az);
                // the hip (on the kicking side) and the way of the kick
                double wx = l < 1.0E-3 ? 0.0 : f.ax / l;
                double wz = l < 1.0E-3 ? 1.0 : f.az / l;
                PhoenixMoveArt.kick(sink, x - wz * 0.12, y, z + wx * 0.12, wx, wz, age, f.bird, f.id);
            }
            case PhoenixMoves.VORTEX_ON -> PhoenixMoveArt.vortex(sink, x, y, z, age, PhoenixMoveArt.VORTEX_RADIUS, f.id);
            case PhoenixMoves.VORTEX_RISE -> PhoenixMoveArt.talonLeap(sink, x, y, z, age);
            case PhoenixMoves.VORTEX_DIVE -> {
                double l = Math.sqrt(f.ax * f.ax + f.ay * f.ay + f.az * f.az);
                if (at != null && f.on && l > 1.0E-3) {
                    PhoenixMoveArt.fujiSheath(sink, at.f_82479_, at.f_82480_ + 1.4, at.f_82481_, f.ax / l, f.ay / l, f.az / l, age, 1.8, f.id);
                }
            }
            default -> PhoenixMoveArt.vortexBlast(sink, x, y, z, age, PhoenixMoveArt.VORTEX_BLAST, f.id);
        }
    }

    /** 3.7.3: Bluebird's fire: the ball growing in front of the chest (the phoenix: the flames gathering, the orb), a ball in flight, its blast. */
    static void blue(FireMesh.Sink sink, Fx f, float age, double x, double y, double z, Entity user, float partial, Vec3 cam) {
        switch (f.phase) {
            case PhoenixMoves.BLUE_CHARGE -> {
                if (user == null || !f.on) {
                    return;
                }
                Vec3 eye = user.m_20299_(partial);
                Vec3 look = user.m_20252_(partial);
                double cx = eye.f_82479_ - cam.f_82479_;
                double cy = eye.f_82480_ - 0.4 - cam.f_82480_;
                double cz = eye.f_82481_ - cam.f_82481_;
                if (f.bird) {
                    // the phoenix: the flames gathering round it, the sphere growing at its chest to the size it flies at
                    PhoenixMoveArt.fujiCharge(sink, cx, cy - 0.4, cz, age, 2.4, f.id);
                    double orb = Math.min(1.0, age / 24.0);
                    PhoenixMoveArt.blueBall(sink, cx + look.f_82479_ * 2.6, cy + 0.8, cz + look.f_82481_ * 2.6, 0.6 + 1.6 * orb, age, f.id);
                } else {
                    PhoenixMoveArt.blueBall(sink, cx + look.f_82479_ * 1.2, cy + look.f_82480_ * 1.2, cz + look.f_82481_ * 1.2,
                        PhoenixMoveArt.blueSize(age), age, f.id);
                }
            }
            case PhoenixMoves.BLUE_FIRE -> {
                if (!f.on) {
                    return;
                }
                double bx = x + f.ax * age;
                double by = y + f.ay * age;
                double bz = z + f.az * age;
                double l = Math.sqrt(f.ax * f.ax + f.ay * f.ay + f.az * f.az);
                // 3.7.6: the sphere of flames flying (no tail: "a sphere not a bird")
                PhoenixMoveArt.blueFlying(sink, bx, by, bz, l > 1.0E-3 ? f.ax / l : 0.0, l > 1.0E-3 ? f.ay / l : 0.0, l > 1.0E-3 ? f.az / l : 0.0,
                    age, f.size, f.id);
            }
            // 3.7.6: the sphere's blast, a great explosion (the bird of fire gone: "a sphere not a bird")
            default -> PhoenixMoveArt.blueBoom(sink, x, y, z, age, f.size, f.id);
        }
    }

    static void clear() {
        FX.clear();
        ACTS.clear();
        WINGS.clear();
        PhoenixBreath.clear();
    }
}
