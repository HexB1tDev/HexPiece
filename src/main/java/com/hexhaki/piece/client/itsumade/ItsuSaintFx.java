package com.hexhaki.piece.client.itsumade;

import com.hexhaki.piece.HexPiece;
import com.hexhaki.piece.client.haki.HakiBolts;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.haki.ConquerorSparks;
import com.hexhaki.piece.itsumade.ItsuArt;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.ItsuSaint;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.9.8: the Itsumade eater's moves as a human, as every client sees them (server/ItsuSaint):
 * <ul>
 *   <li>Gorosei Stare: the air round the target drawn in tight while the glare fixes on it (a dark ring closing on it, black wisps
 *       pulled in); then a shockwave bursting off it the way it is thrown (rings racing out, its Haki's sparks).</li>
 *   <li>Enormous Presence: its body crackling with the Conqueror's lightning the whole ten seconds, a dark haze rising off it, each wave
 *       of its Haki a ring sweeping out over the ground; its bolts leap from the body onto what they strike.</li>
 *   <li>Yokai Blast: a ball of white-hot fire gathering in the pointed hand, black flames licking round it; let go, the beam (the Yokai
 *       Beam's fire, ItsuFx.beamLine) pours out of the hand along the way it was let go on, embers and smoke where it burns into the
 *       ground.</li>
 *   <li>Thy Death: black storm clouds rolling in over the circle, the Conqueror's lightning falling out of them onto the ground, every
 *       bolt bursting in a ball of its Haki's light 3 blocks across.</li>
 * </ul>
 * Every colour is the Itsumade's Haki's (server/RyouServer.ITSU_COLOR, ITSU_AURA): deep indigo, violet rims, green sparks.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class ItsuSaintFx {
    /** Its Haki's palette (dark to light) for the flashes: near-black indigo, indigo, violet, pale violet, green-white. */
    static final int[] HAKI = {0x07051A, 0x120C3A, 0x1E1A66, 0x2B3FD8, 0x4A52F0, 0x7E6CF5, 0xA9F5D2, 0xEFFFF7};
    /** The storm's clouds: soot black with a violet cast. */
    static final int[] STORM = {0x050408, 0x09070E, 0x0E0B15, 0x14101D, 0x1A1526, 0x221B30, 0x2B233B, 0x352C47};
    static final int INDIGO = 0x2B3FD8;
    static final int GREEN = 0x22E08A;
    private static final Random RANDOM = new Random();

    /** One player's moves on this client. */
    static final class Saint {
        long stareFrom = -1000;
        int stareTarget;
        long stareHit = -1000;
        double[] stareAt = new double[3];
        double[] stareWay = new double[3];
        long presenceUntil = -1;
        long pulseFrom = -1000;
        double pulseRadius;
        double[] pulseAt = new double[3];
        long chargeFrom = -1;
        long blastFrom = -1;
        int blastLife;
        double blastWidth;
        double blastLength;
        double blastCharge;
        double[] blastWay = new double[3];
        double blastReach;
        long stormFrom = -1;
        long stormUntil = -1;
        double cloudY;
        double stormRadius;
        /** Its bolts' bursts still glowing: x, y, z, the tick it struck. */
        final java.util.List<double[]> bursts = new java.util.ArrayList<>();
    }

    static final Map<Integer, Saint> SAINTS = new HashMap<>();

    private ItsuSaintFx() {
    }

    static long clock() {
        return ItsuFx.clock;
    }

    /** client/itsumade/ItsuFx hands on the human's messages (server/ItsuSaint: 450 to 458). */
    static void accept(S2CUoFx m) {
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            return;
        }
        Saint s = SAINTS.computeIfAbsent(m.user(), id -> new Saint());
        long now = clock();
        switch (m.kind()) {
            case ItsuSaint.STARE -> {
                s.stareTarget = m.id();
                s.stareAt[0] = m.x();
                s.stareAt[1] = m.y();
                s.stareAt[2] = m.z();
                if (m.bx() > 0.5F) {
                    s.stareHit = now;
                    s.stareWay[0] = m.ax();
                    s.stareWay[1] = m.ay();
                    s.stareWay[2] = m.az();
                    thrown(level, m.x(), m.y(), m.z(), m.ax(), m.ay(), m.az());
                } else {
                    s.stareFrom = now;
                }
            }
            case ItsuSaint.PRESENCE -> s.presenceUntil = m.id() == 1 ? now + m.ticks() : -1;
            case ItsuSaint.PULSE -> {
                s.pulseFrom = now;
                s.pulseRadius = m.bx();
                s.pulseAt[0] = m.x();
                s.pulseAt[1] = m.y();
                s.pulseAt[2] = m.z();
            }
            case ItsuSaint.BODY_BOLT -> bodyBolt(level, m.user(), m.x(), m.y(), m.z(), m.id());
            case ItsuSaint.CHARGE -> s.chargeFrom = m.id() == 1 ? now : -1;
            case ItsuSaint.BLAST -> {
                s.chargeFrom = -1;
                s.blastFrom = now;
                s.blastLife = m.ticks();
                s.blastWidth = m.bx();
                s.blastLength = m.by();
                s.blastCharge = m.size();
                double l = Math.sqrt(m.ax() * m.ax() + m.ay() * m.ay() + m.az() * m.az());
                s.blastWay[0] = m.ax() / Math.max(1.0E-6, l);
                s.blastWay[1] = m.ay() / Math.max(1.0E-6, l);
                s.blastWay[2] = m.az() / Math.max(1.0E-6, l);
            }
            case ItsuSaint.BLAST_OFF -> {
                if (s.blastFrom >= 0) {
                    s.blastLife = (int) Math.min(s.blastLife, now - s.blastFrom); // it fades from now
                }
            }
            case ItsuSaint.STORM -> {
                if (m.id() == 1) {
                    s.stormFrom = now;
                    s.stormUntil = now + m.ticks();
                    s.cloudY = m.bx();
                    s.stormRadius = m.by();
                } else {
                    s.stormUntil = Math.min(s.stormUntil, now);
                }
            }
            case ItsuSaint.SKY_BOLT -> skyBolt(level, s, m.user(), m.x(), m.y(), m.z(), m.bx(), m.id());
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ the bolts

    /** Points on a body the Conqueror's lightning leaps from (its shoulders, its hands, its chest), the one nearest {@code to}. */
    static double[] nearestOnBody(Entity body, double tx, double ty, double tz) {
        double yaw = Math.toRadians(body.m_146908_());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double rx = -fz;
        double rz = fx;
        double x = body.m_20185_();
        double y = body.m_20186_();
        double z = body.m_20189_();
        double[][] points = {
            {x, y + 1.15, z}, {x + rx * 0.4, y + 1.4, z + rz * 0.4}, {x - rx * 0.4, y + 1.4, z - rz * 0.4},
            {x + rx * 0.45 + fx * 0.15, y + 0.75, z + rz * 0.45 + fz * 0.15}, {x - rx * 0.45 + fx * 0.15, y + 0.75, z - rz * 0.45 + fz * 0.15},
            {x, y + 0.7, z}};
        double[] best = points[0];
        double bestD = Double.MAX_VALUE;
        for (double[] p : points) {
            double d = (p[0] - tx) * (p[0] - tx) + (p[1] - ty) * (p[1] - ty) + (p[2] - tz) * (p[2] - tz);
            if (d < bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }

    /** Enormous Presence: one bolt out of the body (the point of it nearest the target) onto what it strikes. */
    static void bodyBolt(ClientLevel level, int user, double tx, double ty, double tz, long seed) {
        Entity owner = level.m_6815_(user);
        if (owner == null) {
            return;
        }
        double[] b = nearestOnBody(owner, tx, ty, tz);
        HakiBolts.sparks(level, owner, b[0], b[1], b[2], ConquerorSparks.wifi(new double[3], new double[] {tx - b[0], ty - b[1], tz - b[2]}, 0.8,
            3, seed), 0.0, 0.0F);
        HakiBolts.zap(level, tx, ty, tz, 0.6F);
    }

    /** Thy Death: a bolt out of the clouds straight down onto the ground, a little crooked, its burst where it lands. */
    static void skyBolt(ClientLevel level, Saint s, int user, double x, double y, double z, double cloudY, long seed) {
        Entity owner = level.m_6815_(user);
        Random r = new Random(seed);
        double top = Math.max(cloudY, y + 12.0);
        double sx = x + (r.nextDouble() - 0.5) * 6.0;
        double sz = z + (r.nextDouble() - 0.5) * 6.0;
        HakiBolts.sparks(level, owner, sx, top, sz, ItsuArt.skyStroke(x - sx, y - top, z - sz, r.nextLong()), 0.0, 0.0F);
        s.bursts.add(new double[] {x, y, z, clock(), seed});
        for (int i = 0; i < 14; i++) {
            double a = r.nextDouble() * Math.PI * 2.0;
            level.m_7106_(ParticleTypes.f_123796_, x + Math.cos(a) * 0.6, y + 0.2, z + Math.sin(a) * 0.6, Math.cos(a) * 0.14,
                0.06 + r.nextDouble() * 0.08, Math.sin(a) * 0.14);
        }
    }

    /** Gorosei Stare's throw: dust and black wisps blown off the target the way it flies. */
    static void thrown(ClientLevel level, double x, double y, double z, double wx, double wy, double wz) {
        for (int i = 0; i < 24; i++) {
            double a = RANDOM.nextDouble() * Math.PI * 2.0;
            double spread = 0.25;
            level.m_7106_(ParticleTypes.f_123796_, x, y, z, wx * 0.5 + Math.cos(a) * spread, wy * 0.5 + (RANDOM.nextDouble() - 0.3) * spread,
                wz * 0.5 + Math.sin(a) * spread);
        }
    }

    // ------------------------------------------------------------------------------------------------------ each tick

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || SAINTS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            SAINTS.clear();
            return;
        }
        long now = clock();
        Iterator<Map.Entry<Integer, Saint>> it = SAINTS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Saint> e = it.next();
            Saint s = e.getValue();
            Entity body = level.m_6815_(e.getKey());
            s.bursts.removeIf(b -> now - (long) b[3] > BURST_LIFE);
            boolean busy = s.presenceUntil > now || s.chargeFrom >= 0 || s.blastFrom >= 0 && now - s.blastFrom < s.blastLife + 8
                || s.stormUntil + STORM_FADE > now || now - s.stareFrom < 30 || now - s.stareHit < 20 || now - s.pulseFrom < 20
                || !s.bursts.isEmpty();
            if (body == null || !busy) {
                if (!busy) {
                    it.remove();
                }
                continue;
            }
            if (s.presenceUntil > now && body instanceof Player player) {
                crackle(level, player, now);
            }
            Entity target = level.m_6815_(s.stareTarget);
            if (now - s.stareFrom < ItsuSaint.STARE_AT && target != null) {
                // the air drawn in round the target
                for (int i = 0; i < 3; i++) {
                    double a = RANDOM.nextDouble() * Math.PI * 2.0;
                    double r = 1.6 + RANDOM.nextDouble() * 0.8;
                    double ty = target.m_20186_() + RANDOM.nextDouble() * target.m_20206_();
                    level.m_7106_(ParticleTypes.f_123762_, target.m_20185_() + Math.cos(a) * r, ty, target.m_20189_() + Math.sin(a) * r,
                        -Math.cos(a) * 0.18, 0.0, -Math.sin(a) * 0.18);
                }
            }
            if (s.blastFrom >= 0 && now - s.blastFrom < s.blastLife && s.blastReach > 0.0 && body instanceof Player player) {
                embers(level, player, s);
            }
            if (s.stormUntil > now && now % 2 == 0 && body instanceof Player player) {
                // a little rain of soot under the clouds
                double a = RANDOM.nextDouble() * Math.PI * 2.0;
                double r = s.stormRadius * Math.sqrt(RANDOM.nextDouble());
                level.m_7106_(ParticleTypes.f_123762_, player.m_20185_() + Math.cos(a) * r, s.cloudY - 2.0, player.m_20189_() + Math.sin(a) * r,
                    0.0, -0.05, 0.0);
            }
        }
    }

    /** Enormous Presence: short arcs of the Conqueror's lightning crawling off the body, a dark haze rising off it. */
    static void crackle(ClientLevel level, Player player, long now) {
        if (now % 3 == 0) {
            double h = 0.3 + RANDOM.nextDouble() * 1.4;
            double a = RANDOM.nextDouble() * Math.PI * 2.0;
            double bx = player.m_20185_() + Math.cos(a) * 0.3;
            double by = player.m_20186_() + h;
            double bz = player.m_20189_() + Math.sin(a) * 0.3;
            double out = 0.7 + RANDOM.nextDouble() * 0.9;
            HakiBolts.sparks(level, player, bx, by, bz, ConquerorSparks.wifi(new double[3], new double[] {Math.cos(a) * out,
                (RANDOM.nextDouble() - 0.4) * out, Math.sin(a) * out}, 0.35, 2, RANDOM.nextLong()), 0.0, 0.0F);
        }
        if (now % 2 == 0) {
            double a = RANDOM.nextDouble() * Math.PI * 2.0;
            level.m_7106_(ParticleTypes.f_123762_, player.m_20185_() + Math.cos(a) * 0.4, player.m_20186_() + RANDOM.nextDouble() * 1.6,
                player.m_20189_() + Math.sin(a) * 0.4, Math.cos(a) * 0.02, 0.04, Math.sin(a) * 0.02);
        }
    }

    /** Yokai Blast: embers and smoke thrown off where the beam burns into what it meets. */
    static void embers(ClientLevel level, Player player, Saint s) {
        Vec3 from = hand(player, 1.0F);
        double[] w = s.blastWay;
        double x = from.f_82479_ + w[0] * s.blastReach;
        double y = from.f_82480_ + w[1] * s.blastReach;
        double z = from.f_82481_ + w[2] * s.blastReach;
        for (int i = 0; i < 3; i++) {
            level.m_7106_(ParticleTypes.f_123796_, x, y, z, -w[0] * 0.15 + (RANDOM.nextDouble() - 0.5) * 0.2, 0.08 + RANDOM.nextDouble() * 0.1,
                -w[2] * 0.15 + (RANDOM.nextDouble() - 0.5) * 0.2);
        }
    }

    /** Where the right hand is: the pose's own fist (client/FirstPersonLight: the view's in first person), else near enough. */
    static Vec3 hand(Player player, float partial) {
        Vec3 posed = com.hexhaki.piece.client.FirstPersonLight.fist(player, partial);
        if (posed != null) {
            return posed;
        }
        Vec3 look = player.m_20252_(partial);
        Vec3 flat = new Vec3(look.f_82479_, 0.0, look.f_82481_);
        flat = flat.m_82556_() < 1.0E-6 ? new Vec3(0, 0, 1) : flat.m_82541_();
        Vec3 right = new Vec3(-flat.f_82481_, 0.0, flat.f_82479_);
        return player.m_20318_(partial).m_82520_(0.0, 1.38, 0.0).m_82549_(right.m_82490_(0.36)).m_82549_(look.m_82490_(0.8));
    }

    // ------------------------------------------------------------------------------------------------------ the drawing

    /** How long a bolt's burst glows (ticks), how long the storm takes to clear. */
    static final int BURST_LIFE = 10;
    static final int STORM_FADE = 30;

    @SubscribeEvent
    public static void effects(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || SAINTS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            return;
        }
        float partial = event.getPartialTick();
        Vec3 camera = event.getCamera().m_90583_();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        RenderType type = RenderType.m_269166_();
        ItsuFx.SINK.matrix = event.getPoseStack().m_85850_().m_252922_();
        try {
            ItsuFx.SINK.buffer = buffers.m_6299_(type);
            float now = clock() + partial;
            for (Map.Entry<Integer, Saint> e : SAINTS.entrySet()) {
                Saint s = e.getValue();
                int seed = e.getKey();
                Entity body = level.m_6815_(e.getKey());
                if (body instanceof Player player) {
                    if (s.chargeFrom >= 0) {
                        Vec3 h = hand(player, partial).m_82546_(camera);
                        double c = Math.min(1.0, (now - s.chargeFrom) / ItsuSaint.BLAST_FULL);
                        ItsuFx.ball(h.f_82479_, h.f_82480_, h.f_82481_, 0.12 + 0.3 * c, now, seed);
                    }
                    if (s.blastFrom >= 0 && now - s.blastFrom < s.blastLife + 6) {
                        s.blastReach = ItsuFx.beamLine(level, camera, hand(player, partial), s.blastWay, s.blastLength, s.blastWidth, s.blastLife,
                            now - s.blastFrom, seed, ItsuSaint.BLAST_SPEED, 2.0);
                    }
                    if (s.stormFrom >= 0 && now < s.stormUntil + STORM_FADE) {
                        clouds(player, s, camera, now, partial, seed);
                    }
                }
                Entity target = level.m_6815_(s.stareTarget);
                if (now - s.stareFrom < ItsuSaint.STARE_AT + 1 && target != null) {
                    Vec3 t = target.m_20318_(partial).m_82520_(0.0, target.m_20206_() * 0.5, 0.0).m_82546_(camera);
                    double k = (now - s.stareFrom) / ItsuSaint.STARE_AT;
                    // a dark ring closing in round it, level with its middle
                    ring(t.f_82479_, t.f_82480_, t.f_82481_, 0, 1, 0, 2.4 - 1.6 * k, 0.18 + 0.1 * k, 0x07051A, INDIGO, 0.25 + 0.55 * k);
                }
                if (now - s.stareHit < 12) {
                    double k = (now - s.stareHit) / 12.0;
                    double[] w = s.stareWay;
                    double x = s.stareAt[0] - camera.f_82479_;
                    double y = s.stareAt[1] - camera.f_82480_;
                    double z = s.stareAt[2] - camera.f_82481_;
                    // the shockwave: rings racing out off it, square to the way it is thrown
                    for (int i = 0; i < 3; i++) {
                        double kk = Math.max(0.0, k - i * 0.12);
                        if (kk <= 0.0) {
                            continue;
                        }
                        double r = 0.6 + 4.5 * Math.sqrt(kk);
                        ring(x + w[0] * i * 0.7, y + w[1] * i * 0.7, z + w[2] * i * 0.7, w[0], w[1], w[2], r, 0.35 * (1.0 - kk) + 0.05,
                            INDIGO, GREEN, (1.0 - kk) * 0.8);
                    }
                }
                if (now - s.pulseFrom < 14) {
                    double k = (now - s.pulseFrom) / 14.0;
                    double r = s.pulseRadius * Math.sqrt(k);
                    ring(s.pulseAt[0] - camera.f_82479_, s.pulseAt[1] + 0.15 - camera.f_82480_, s.pulseAt[2] - camera.f_82481_, 0, 1, 0, r,
                        0.6 + 1.6 * (1.0 - k), 0x07051A, INDIGO, (1.0 - k) * 0.7);
                }
                for (double[] b : s.bursts) {
                    double age = now - b[3];
                    if (age < 0.0 || age > BURST_LIFE) {
                        continue;
                    }
                    ItsuArt.burst(ItsuFx.SINK, b[0] - camera.f_82479_, b[1] - camera.f_82480_, b[2] - camera.f_82481_, ItsuSaint.DEATH_BLAST,
                        age / BURST_LIFE, now, (int) b[4]);
                }
            }
        } catch (RuntimeException failure) {
            HexPiece.LOGGER.error("HexPiece: an Itsumade's move could not be drawn", failure);
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
        if (ItsuFx.SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        ItsuFx.SINK.buffer = null;
    }

    /** Thy Death's clouds over the player at the storm's height, gathering, then clearing once it is over (itsumade/ItsuArt.clouds). */
    static void clouds(Player player, Saint s, Vec3 camera, float now, float partial, int seed) {
        double gather = Math.min(1.0, (now - s.stormFrom) / ItsuSaint.DEATH_GATHER);
        double clear = now > s.stormUntil ? Math.max(0.0, 1.0 - (now - s.stormUntil) / STORM_FADE) : 1.0;
        Vec3 at = player.m_20318_(partial);
        ItsuArt.clouds(ItsuFx.SINK, at.f_82479_ - camera.f_82479_, s.cloudY - camera.f_82480_, at.f_82481_ - camera.f_82481_,
            s.stormRadius + 6.0, gather, 0.85 * gather * clear, now, seed);
    }

    static void ring(double x, double y, double z, double nx, double ny, double nz, double radius, double width, int inner, int outer,
                     double alpha) {
        ItsuArt.ring(ItsuFx.SINK, x, y, z, nx, ny, nz, radius, width, inner, outer, alpha);
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SAINTS.clear();
    }
}
