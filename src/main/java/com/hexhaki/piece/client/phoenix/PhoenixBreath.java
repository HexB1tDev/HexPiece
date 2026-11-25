package com.hexhaki.piece.client.phoenix;

import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.dragon.UoFire;
import com.hexhaki.piece.phoenix.PhoenixBlast;
import com.hexhaki.piece.phoenix.PhoenixForm;
import com.hexhaki.piece.server.PhoenixMoves;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 3.7.7 (the owner: "Transformed M1, shall make the bird open it's beak and act as a flamethrower (blasting flames at the target like the
 * dragon does)"): the phoenix's flamethrower on this client (server/PhoenixMoves.FLAME, handed on by PhoenixMoveFx), as the dragon's breath
 * is drawn (client/dragon/UoFx): the beak opening and the head aimed down the look (PhoenixForm.Motion's beak, breath, aim), the flame
 * pouring out of the open beak (phoenix/PhoenixBlast.breath) from where the phoenix was last drawn (PhoenixForm.gape: PhoenixFx.draw
 * leaves it here), onto what the player aims at, each puff going the way the beak aimed when it left (a track of the last
 * {@link #TRACK} ticks), stopped short by a wall.
 */
final class PhoenixBreath {
    private PhoenixBreath() {
    }

    /** A flame: when it began and stopped (-1 still on), how far it reaches, the beak's last aims (9 numbers a tick, world). */
    static final class Breath {
        final long start;
        long stop = -1;
        double range = PhoenixMoves.FLAME_RANGE;
        final double[] track = new double[TRACK * 9];
        long first = -1;
        long last = -1;

        Breath(long start) {
            this.start = start;
        }
    }

    static final int TRACK = 24;
    /** The beak opens over this many ticks and shuts over {@link #SHUT}; the head comes up to aim over {@link #AIM_IN}, back over AIM_OUT. */
    static final float OPEN = 3.0F;
    static final float SHUT = 5.0F;
    static final float AIM_IN = 4.0F;
    static final float AIM_OUT = 8.0F;
    /** In first person (the phoenix not drawn) the flame leaves from under the middle of the view: this far ahead, this far under. */
    static final double FIRST_AHEAD = 2.5;
    static final double FIRST_UNDER = 0.9;

    static final Map<Integer, Breath> BREATHS = new HashMap<>();
    /** Where each phoenix's open beak was last drawn (world: x, y, z, its way x, y, z) and when (the clock). */
    static final Map<Integer, double[]> GAPES = new HashMap<>();

    static void accept(int user, boolean on) {
        long now = PhoenixFx.clock;
        Breath b = BREATHS.get(user);
        if (on) {
            if (b == null || b.stop >= 0) {
                BREATHS.put(user, new Breath(now));
            }
        } else if (b != null && b.stop < 0) {
            b.stop = now;
        }
    }

    /** PhoenixFx.draw: the gape this frame (camera-relative), {@code camera} added back. */
    static void drawn(int id, double[] gape, Vec3 camera) {
        if (!BREATHS.containsKey(id)) {
            return;
        }
        double[] g = GAPES.computeIfAbsent(id, k -> new double[7]);
        g[0] = gape[0] + camera.f_82479_;
        g[1] = gape[1] + camera.f_82480_;
        g[2] = gape[2] + camera.f_82481_;
        g[3] = gape[3];
        g[4] = gape[4];
        g[5] = gape[5];
        g[6] = PhoenixFx.clock;
    }

    /** The beak's place and way (6 numbers, world): where it was last drawn, or near enough (first person, not yet drawn). */
    static double[] beak(Player player, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        double pitch = Math.toRadians(player.f_19860_ + (player.m_146909_() - player.f_19860_) * partial);
        double yaw = Math.toRadians(player.f_19859_ + (player.m_146908_() - player.f_19859_) * partial);
        double lx = -Math.sin(yaw) * Math.cos(pitch);
        double ly = -Math.sin(pitch);
        double lz = Math.cos(yaw) * Math.cos(pitch);
        if (player == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON) {
            Vec3 eye = player.m_20299_(partial);
            double ux = -Math.sin(yaw) * Math.sin(pitch);
            double uy = Math.cos(pitch);
            double uz = Math.cos(yaw) * Math.sin(pitch);
            return new double[] {eye.f_82479_ + lx * FIRST_AHEAD - ux * FIRST_UNDER, eye.f_82480_ + ly * FIRST_AHEAD - uy * FIRST_UNDER,
                eye.f_82481_ + lz * FIRST_AHEAD - uz * FIRST_UNDER, lx, ly, lz};
        }
        double[] g = GAPES.get(player.m_19879_());
        if (g != null && PhoenixFx.clock - g[6] < 4) {
            return new double[] {g[0], g[1], g[2], g[3], g[4], g[5]};
        }
        double x = player.f_19790_ + (player.m_20185_() - player.f_19790_) * partial;
        double y = player.f_19791_ + (player.m_20186_() - player.f_19791_) * partial;
        double z = player.f_19792_ + (player.m_20189_() - player.f_19792_) * partial;
        return new double[] {x + lx * PhoenixMoves.BEAK_AHEAD, y + PhoenixMoves.BEAK_UP + ly * PhoenixMoves.BEAK_AHEAD, z + lz * PhoenixMoves.BEAK_AHEAD,
            lx, ly, lz};
    }

    /**
     * Where the flame goes, as the server aims it (server/UoMoves.toward): from the beak onto the first block on the eye's line (the
     * crosshair) within the range, or that line's end; the look when that point is too near or behind. Returns the way (unit), how far
     * that point is from the beak, and 1 if a block stops the line.
     */
    static double[] aimed(Player player, double[] beak, float partial) {
        Vec3 eye = player.m_20299_(partial);
        Vec3 look = player.m_20252_(partial);
        Vec3 end = eye.m_82549_(look.m_82490_(PhoenixMoves.FLAME_RANGE));
        BlockHitResult wall = player.m_9236_().m_45547_(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        boolean hit = wall.m_6662_() == HitResult.Type.BLOCK;
        Vec3 to = hit ? wall.m_82450_() : end;
        double wx = to.f_82479_ - beak[0];
        double wy = to.f_82480_ - beak[1];
        double wz = to.f_82481_ - beak[2];
        double length = Math.sqrt(wx * wx + wy * wy + wz * wz);
        if (length < 4.0 || (wx * look.f_82479_ + wy * look.f_82480_ + wz * look.f_82481_) / length < 0.5) {
            return new double[] {look.f_82479_, look.f_82480_, look.f_82481_, length, hit ? 1.0 : 0.0};
        }
        return new double[] {wx / length, wy / length, wz / length, length, hit ? 1.0 : 0.0};
    }

    /** Each tick: every flame's aim onto its track, its reach eased to the wall it meets; spent flames forgotten. */
    static void tick(ClientLevel level) {
        long now = PhoenixFx.clock;
        BREATHS.entrySet().removeIf(e -> e.getValue().stop >= 0 && now - e.getValue().stop > PhoenixBlast.BREATH_LIFE + SHUT + 2);
        GAPES.keySet().removeIf(id -> !BREATHS.containsKey(id));
        if (level == null) {
            return;
        }
        for (Map.Entry<Integer, Breath> e : BREATHS.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof Player player)) {
                continue;
            }
            Breath b = e.getValue();
            double[] m = beak(player, 1.0F);
            double[] aim = aimed(player, m, 1.0F);
            int o = (int) Math.floorMod(now, (long) TRACK) * 9;
            b.track[o] = m[0];
            b.track[o + 1] = m[1];
            b.track[o + 2] = m[2];
            b.track[o + 3] = aim[0];
            b.track[o + 4] = aim[1];
            b.track[o + 5] = aim[2];
            b.track[o + 6] = m[3];
            b.track[o + 7] = m[4];
            b.track[o + 8] = m[5];
            if (b.first < 0 || b.last < now - 1) {
                b.first = now;
            }
            b.last = now;
            if (b.stop < 0) {
                double reach = aim[4] > 0.0 ? Math.max(3.0, aim[3] + 1.5) : PhoenixMoves.FLAME_RANGE;
                b.range += (reach - b.range) * 0.5;
            }
        }
    }

    /** The phoenix's pose for its flame (PhoenixForm.Motion's beak, breath, aim), {@code t} the clock with the partial, {@code pitch} the look's. */
    static void pose(int user, float t, PhoenixForm.Motion mo, float pitch) {
        mo.beak = 0.0F;
        mo.breath = 0.0F;
        mo.aim = pitch;
        Breath b = BREATHS.get(user);
        if (b == null) {
            return;
        }
        float on = t - b.start;
        if (on < 0.0F) {
            return;
        }
        float shut = b.stop < 0 ? 1.0F : 1.0F - Math.min(1.0F, Math.max(0.0F, (t - b.stop) / SHUT));
        float down = b.stop < 0 ? 1.0F : 1.0F - Math.min(1.0F, Math.max(0.0F, (t - b.stop) / AIM_OUT));
        mo.beak = smooth(Math.min(1.0F, on / OPEN)) * smooth(shut);
        mo.breath = smooth(Math.min(1.0F, on / AIM_IN)) * smooth(down);
    }

    static float smooth(float t) {
        float u = Math.max(0.0F, Math.min(1.0F, t));
        return u * u * (3.0F - 2.0F * u);
    }

    private static final Aimer AIM = new Aimer();

    /** Every flame, after the particles (PhoenixFx.fire). */
    static void draw(FireMesh.Sink sink, ClientLevel level, Vec3 cam, float partial) {
        if (BREATHS.isEmpty()) {
            return;
        }
        float now = PhoenixFx.clock + partial;
        for (Map.Entry<Integer, Breath> e : BREATHS.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof Player player) || player.m_20182_().m_82557_(cam) > 160.0 * 160.0) {
                continue;
            }
            Breath b = e.getValue();
            AIM.set(player, b, cam, partial);
            double on = now - b.start;
            double off = b.stop >= 0 ? now - b.stop : -1.0;
            PhoenixBlast.breath(sink, AIM, on, off, b.range, e.getKey());
        }
    }

    /** The flame's aims as they were (eye-relative), read off its track; this frame's own for the newest. */
    static final class Aimer implements UoFire.Aim {
        final double[] now = new double[9];
        Breath breath;
        double cx;
        double cy;
        double cz;
        float partial;

        void set(Player player, Breath b, Vec3 cam, float partial) {
            double[] m = beak(player, partial);
            double[] aim = aimed(player, m, partial);
            this.now[0] = m[0];
            this.now[1] = m[1];
            this.now[2] = m[2];
            this.now[3] = aim[0];
            this.now[4] = aim[1];
            this.now[5] = aim[2];
            this.now[6] = m[3];
            this.now[7] = m[4];
            this.now[8] = m[5];
            this.breath = b;
            this.cx = cam.f_82479_;
            this.cy = cam.f_82480_;
            this.cz = cam.f_82481_;
            this.partial = partial;
        }

        @Override
        public void at(double ago, double[] out) {
            double back = ago - this.partial;
            Breath b = this.breath;
            if (b == null || back <= 0.0 || b.last < 0) {
                System.arraycopy(this.now, 0, out, 0, 9);
            } else {
                int k0 = (int) Math.floor(back);
                double f = back - k0;
                int span = (int) Math.min(TRACK - 1, b.last - b.first);
                int k1 = Math.min(k0 + 1, span);
                k0 = Math.min(k0, span);
                int o0 = (int) Math.floorMod(b.last - k0, (long) TRACK) * 9;
                int o1 = (int) Math.floorMod(b.last - k1, (long) TRACK) * 9;
                for (int i = 0; i < 9; i++) {
                    out[i] = b.track[o0 + i] + (b.track[o1 + i] - b.track[o0 + i]) * f;
                }
            }
            out[0] -= this.cx;
            out[1] -= this.cy;
            out[2] -= this.cz;
        }
    }

    static void clear() {
        BREATHS.clear();
        GAPES.clear();
    }
}
