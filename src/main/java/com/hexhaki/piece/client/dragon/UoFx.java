package com.hexhaki.piece.client.dragon;

import com.hexhaki.piece.client.fx.PieceFx;
import com.hexhaki.piece.dragon.DragonForm;
import com.hexhaki.piece.network.S2CUoFx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.7.2: the Uo Uo no Mi's moves as every client sees them (server/UoMoves sends {@link S2CUoFx}). The owner: "Use minecraft particles
 * and some Photon (Only for sparks etc) fire is made of a colored Cloud particles and smoke": fire is the game's cloud particles
 * coloured from white-hot through yellow and orange to red, with smoke behind; Photon (client/fx/PieceFx) only for the sparks. Air is
 * white clouds and the game's poof; the tornado pale green clouds. Everything a move draws is worked out here from what the server sent
 * (where a shot is each tick, where the tornado is, each ember's arc), so it costs no traffic while it flies. Counts follow the
 * player's particle setting (PieceFx.count).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class UoFx {
    static final Random RANDOM = new Random();
    private static final Map<Integer, Shot> SHOTS = new HashMap<>();
    private static final List<Tornado> TORNADOES = new ArrayList<>();
    private static final List<Ember> EMBERS = new ArrayList<>();
    private static final List<Bang> BANGS = new ArrayList<>();
    /** Per player: their dragon's breath (since when), its charge (since when), its bite (since when). */
    static final Map<Integer, Long> FLAMES = new HashMap<>();
    static final Map<Integer, Long> CHARGES = new HashMap<>();
    static final Map<Integer, Long> BITES = new HashMap<>();
    private static long clock;

    record Shot(int kind, Vec3 velocity, double size, int life, long born, Vec3[] at) {
    }

    record Tornado(Vec3 start, Vec3 step, int life, long born) {
    }

    record Ember(Vec3 mouth, Vec3 apex, Vec3 land, int delay, int rise, int fall, long born) {
    }

    /** A burst still going off for a few ticks. */
    record Bang(Vec3 at, Vec3 dir, double radius, int kind, long born) {
    }

    private UoFx() {
    }

    public static void accept(S2CUoFx m) {
        Vec3 at = new Vec3(m.x(), m.y(), m.z());
        Vec3 a = new Vec3(m.ax(), m.ay(), m.az());
        switch (m.kind()) {
            case S2CUoFx.AIR, S2CUoFx.BOLT, S2CUoFx.BLAST -> {
                SHOTS.put(m.id(), new Shot(m.kind(), a, m.size(), m.ticks(), clock, new Vec3[] {at}));
                if (m.kind() == S2CUoFx.AIR) {
                    ring(at, a.m_82541_(), 0.6, 16, 0.18, 0xFFFFFF);
                } else {
                    flash(at, m.kind() == S2CUoFx.BLAST ? 1.6 : 0.8);
                }
            }
            case S2CUoFx.BURST -> {
                Shot shot = SHOTS.remove(m.id());
                int kind = (int) m.bz();
                BANGS.add(new Bang(at, new Vec3(m.ax(), m.ay(), m.az()), m.size(), shot == null ? kind : shot.kind(), clock));
            }
            case S2CUoFx.TORNADO -> TORNADOES.add(new Tornado(at, a, m.ticks(), clock));
            case S2CUoFx.EMBER -> EMBERS.add(new Ember(at, a, new Vec3(m.bx(), m.by(), m.bz()), m.ticks() >> 16 & 255, m.ticks() >> 8 & 255,
                m.ticks() & 255, clock));
            case S2CUoFx.FLAME_ON -> FLAMES.put(m.user(), clock);
            case S2CUoFx.FLAME_OFF -> FLAMES.remove(m.user());
            case S2CUoFx.CHARGE_ON -> CHARGES.put(m.user(), clock);
            case S2CUoFx.CHARGE_OFF -> CHARGES.remove(m.user());
            case S2CUoFx.BITE -> BITES.put(m.user(), clock);
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ for the dragon's pose

    /** How far a dragon's jaw is open now (its breath, its charge). */
    static float mouth(int id, float partial) {
        float open = 0.0F;
        Long flame = FLAMES.get(id);
        if (flame != null) {
            open = Math.max(open, DragonForm.smooth((clock - flame + partial) / 5.0F));
        }
        Long charge = CHARGES.get(id);
        if (charge != null) {
            open = Math.max(open, 0.55F + 0.3F * DragonForm.smooth((clock - charge + partial) / 40.0F));
        }
        return open;
    }

    /** How far into its bite a dragon is (0 to 1), or -1. */
    static float bite(int id, float partial) {
        Long bite = BITES.get(id);
        if (bite == null) {
            return -1.0F;
        }
        float t = (clock - bite + partial) / BITE_TICKS;
        return t > 1.0F ? -1.0F : t;
    }

    /** The bite's length (ticks): the jaws shut on the server's tick 7 (0.45 to 0.6 of it). */
    static final float BITE_TICKS = 14.0F;

    // ------------------------------------------------------------------------------------------------------ the clock

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clock++;
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.m_91104_()) {
            return;
        }
        for (Iterator<Map.Entry<Integer, Shot>> it = SHOTS.entrySet().iterator(); it.hasNext(); ) {
            Shot shot = it.next().getValue();
            long age = clock - shot.born;
            if (age > shot.life + 2) {
                it.remove();
                continue;
            }
            Vec3 from = shot.at[0];
            Vec3 to = from.m_82549_(shot.velocity);
            shot.at[0] = to;
            trail(level, shot, from, to, age);
        }
        for (Iterator<Bang> it = BANGS.iterator(); it.hasNext(); ) {
            Bang bang = it.next();
            long age = clock - bang.born;
            if (age > 3) {
                it.remove();
                continue;
            }
            burst(level, bang, (int) age);
        }
        for (Iterator<Tornado> it = TORNADOES.iterator(); it.hasNext(); ) {
            Tornado t = it.next();
            long age = clock - t.born;
            if (age > t.life) {
                it.remove();
                continue;
            }
            tornado(level, t.start.m_82549_(t.step.m_82490_(age)), age, Math.min(1.0, Math.min(age / 6.0, (t.life - age) / 8.0)));
        }
        for (Iterator<Ember> it = EMBERS.iterator(); it.hasNext(); ) {
            Ember e = it.next();
            long age = clock - e.born - e.delay;
            if (age < 0) {
                continue;
            }
            if (age > e.rise + e.fall) {
                it.remove();
                BANGS.add(new Bang(e.land.m_82520_(0.0, 0.4, 0.0), new Vec3(0, -1, 0), 2.6, S2CUoFx.BOLT, clock));
                continue;
            }
            Vec3 now = ember(e, age);
            Vec3 before = ember(e, Math.max(0, age - 1));
            fireball(level, now, now.m_82546_(before), 0.45, 3);
        }
        breaths(level);
        BITES.entrySet().removeIf(e -> clock - e.getValue() > BITE_TICKS + 2);
    }

    /** Where an ember is {@code age} ticks after it left the mouth: up to its top easing out, down onto its mark easing in. */
    static Vec3 ember(Ember e, long age) {
        if (age <= e.rise) {
            double t = age / (double) e.rise;
            double u = 1.0 - (1.0 - t) * (1.0 - t);
            Vec3 bend = e.mouth.m_82549_(e.apex).m_82490_(0.5).m_82520_(0.0, (e.apex.f_82480_ - e.mouth.f_82480_) * 0.35, 0.0);
            return bezier(e.mouth, bend, e.apex, u);
        }
        double t = (age - e.rise) / (double) e.fall;
        double u = t * t;
        Vec3 bend = new Vec3(e.land.f_82479_, e.apex.f_82480_, e.land.f_82481_);
        return bezier(e.apex, bend, e.land, u);
    }

    static Vec3 bezier(Vec3 a, Vec3 b, Vec3 c, double t) {
        Vec3 ab = a.m_82549_(b.m_82546_(a).m_82490_(t));
        Vec3 bc = b.m_82549_(c.m_82546_(b).m_82490_(t));
        return ab.m_82549_(bc.m_82546_(ab).m_82490_(t));
    }

    // ------------------------------------------------------------------------------------------------------ the dragons' mouths

    static void breaths(ClientLevel level) {
        for (Map.Entry<Integer, Long> e : FLAMES.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof Player player)) {
                continue;
            }
            Vec3 mouth = mouthOf(player);
            Vec3 look = player.m_20252_(1.0F);
            long age = clock - e.getValue();
            int n = PieceFx.count(age < 4 ? 14 : 28);
            for (int i = 0; i < n; i++) {
                double speed = 1.05 + RANDOM.nextDouble() * 0.85;
                Vec3 spread = new Vec3(r(0.16), r(0.16), r(0.16));
                Vec3 v = look.m_82549_(spread).m_82541_().m_82490_(speed);
                double start = RANDOM.nextDouble() * 1.5;
                Vec3 p = mouth.m_82549_(look.m_82490_(start));
                float heat = (float) (0.55 + RANDOM.nextDouble() * 0.45);
                fire(level, p, v, heat);
            }
            if (age % 2 == 0) {
                for (int i = 0; i < PieceFx.count(4); i++) {
                    Vec3 v = look.m_82490_(0.6 + RANDOM.nextDouble() * 0.5).m_82520_(r(0.08), 0.04, r(0.08));
                    add(level, ParticleTypes.f_123755_, mouth.m_82549_(look.m_82490_(4.0 + RANDOM.nextDouble() * 6.0)), v);
                }
                spark(mouth.m_82549_(look.m_82490_(2.0)), look.m_82490_(0.9).m_82520_(r(0.3), r(0.3), r(0.3)), 0xFFFFD27A, 8);
            }
        }
        for (Map.Entry<Integer, Long> e : CHARGES.entrySet()) {
            if (!(level.m_6815_(e.getKey()) instanceof Player player)) {
                continue;
            }
            long age = clock - e.getValue();
            double size = 0.5 + 2.4 * Math.min(1.0, age / 60.0);
            Vec3 core = mouthOf(player).m_82549_(player.m_20252_(1.0F).m_82490_(size * 0.8));
            fireball(level, core, Vec3.f_82478_, size, 6 + (int) (size * 6));
            // fire drawn in: puffs from round about rushing to the ball
            for (int i = 0; i < PieceFx.count(6); i++) {
                Vec3 out = new Vec3(r(1), r(1), r(1)).m_82541_().m_82490_(size * 2.6 + 1.5);
                fire(level, core.m_82549_(out), out.m_82490_(-0.22), 0.75F);
            }
            if (age % 3 == 0) {
                spark(core, new Vec3(r(0.4), r(0.4), r(0.4)), 0xFFFFE7A0, 10);
            }
        }
    }

    /** A dragon's mouth: where the renderer last drew it, or near enough (over the feet, out along the look) before it has been drawn. */
    static Vec3 mouthOf(Player player) {
        DragonClient.Entry entry = DragonClient.ENTRIES.get(player.m_19879_());
        if (entry != null && entry.mouthAge >= 0 && clock - entry.mouthAge < 4) {
            return new Vec3(entry.mouth[0], entry.mouth[1], entry.mouth[2]);
        }
        Vec3 look = player.m_20252_(1.0F);
        return player.m_20182_().m_82520_(0.0, DragonForm.HEAD_HEIGHT - 0.6, 0.0).m_82549_(look.m_82490_(2.6));
    }

    static long clock() {
        return clock;
    }

    // ------------------------------------------------------------------------------------------------------ the pieces

    static void trail(ClientLevel level, Shot shot, Vec3 from, Vec3 to, long age) {
        Vec3 way = to.m_82546_(from);
        double length = way.m_82553_();
        Vec3 dir = length < 1.0E-6 ? new Vec3(0, 0, 1) : way.m_82490_(1.0 / length);
        if (shot.kind == S2CUoFx.AIR) {
            int n = PieceFx.count(6);
            for (int i = 0; i < n; i++) {
                Vec3 p = from.m_82549_(way.m_82490_(RANDOM.nextDouble()));
                cloud(level, p.m_82520_(r(shot.size * 0.5), r(shot.size * 0.5), r(shot.size * 0.5)), dir.m_82490_(0.15), 1.0F, 1.0F, 1.0F);
            }
            if (age % 2 == 0) {
                ring(to, dir, shot.size * 0.8, 10, 0.12, 0xFFFFFF);
            }
            add(level, ParticleTypes.f_123759_, to, Vec3.f_82478_);
            return;
        }
        double size = shot.size;
        int n = PieceFx.count(shot.kind == S2CUoFx.BLAST ? 18 : 10);
        for (int i = 0; i < n; i++) {
            Vec3 p = from.m_82549_(way.m_82490_(RANDOM.nextDouble())).m_82520_(r(size * 0.4), r(size * 0.4), r(size * 0.4));
            fire(level, p, dir.m_82490_(0.08).m_82520_(r(0.05), r(0.05), r(0.05)), (float) (0.6 + RANDOM.nextDouble() * 0.4));
        }
        fireball(level, to, way, size, shot.kind == S2CUoFx.BLAST ? 14 : 6);
        for (int i = 0; i < PieceFx.count(shot.kind == S2CUoFx.BLAST ? 4 : 2); i++) {
            add(level, ParticleTypes.f_123762_, from.m_82549_(way.m_82490_(RANDOM.nextDouble())), new Vec3(r(0.03), 0.03, r(0.03)));
        }
    }

    /** A ball of fire at {@code at} (moving along {@code way}): a hot core, an orange skin, its sparks. */
    static void fireball(ClientLevel level, Vec3 at, Vec3 way, double size, int count) {
        int n = PieceFx.count(count);
        for (int i = 0; i < n; i++) {
            Vec3 off = new Vec3(r(1), r(1), r(1)).m_82541_().m_82490_(size * Math.sqrt(RANDOM.nextDouble()));
            double depth = off.m_82553_() / Math.max(0.05, size);
            fire(level, at.m_82549_(off), way.m_82490_(0.05).m_82549_(off.m_82490_(0.03)), (float) (1.0 - 0.6 * depth));
        }
        if (RANDOM.nextInt(3) == 0) {
            spark(at, new Vec3(r(0.3), r(0.3), r(0.3)), 0xFFFFC266, 7);
        }
    }

    static void burst(ClientLevel level, Bang bang, int age) {
        double radius = bang.radius;
        if (bang.kind == S2CUoFx.AIR) {
            if (age == 0) {
                ring(bang.at, bang.dir.m_82556_() < 1.0E-6 ? new Vec3(0, 1, 0) : bang.dir.m_82541_(), 0.4, 22, 0.35, 0xFFFFFF);
                for (int i = 0; i < PieceFx.count(12); i++) {
                    add(level, ParticleTypes.f_123759_, bang.at.m_82520_(r(radius * 0.5), r(radius * 0.5), r(radius * 0.5)), Vec3.f_82478_);
                }
            }
            return;
        }
        int n = PieceFx.count((int) Math.min(120, 18 + radius * radius * 2.6) / (age + 1));
        for (int i = 0; i < n; i++) {
            Vec3 out = new Vec3(r(1), r(0.8) + 0.25, r(1)).m_82541_();
            double speed = (0.12 + RANDOM.nextDouble() * 0.28) * Math.sqrt(radius);
            Vec3 p = bang.at.m_82549_(out.m_82490_(RANDOM.nextDouble() * radius * 0.35));
            fire(level, p, out.m_82490_(speed), (float) (age == 0 ? 0.75 + RANDOM.nextDouble() * 0.25 : 0.45 + RANDOM.nextDouble() * 0.35));
        }
        if (age <= 1) {
            for (int i = 0; i < PieceFx.count((int) (6 + radius * 3)); i++) {
                Vec3 out = new Vec3(r(1), r(0.6) + 0.3, r(1)).m_82541_();
                add(level, ParticleTypes.f_123755_, bang.at.m_82549_(out.m_82490_(radius * 0.5)), out.m_82490_(0.08 * Math.sqrt(radius)));
                spark(bang.at, out.m_82490_(0.35 + RANDOM.nextDouble() * 0.5 * Math.sqrt(radius)), 0xFFFFB24A, 12);
            }
            for (int i = 0; i < PieceFx.count(4); i++) {
                add(level, ParticleTypes.f_175834_, bang.at, new Vec3(r(0.3), 0.3, r(0.3)));
            }
        }
    }

    /** The tornado at {@code base}: pale green clouds wound up a funnel, turning, dust kicked up at its foot. */
    static void tornado(ClientLevel level, Vec3 base, long age, double strength) {
        int n = PieceFx.count((int) (34 * strength));
        double low = 2.2;
        double high = 4.2;
        double tall = 9.0;
        for (int i = 0; i < n; i++) {
            double h = RANDOM.nextDouble() * tall;
            double radius = (low + (high - low) * h / tall) * (0.75 + RANDOM.nextDouble() * 0.3);
            double a = age * 0.55 + h * 0.9 + RANDOM.nextDouble() * Math.PI * 2.0;
            Vec3 p = base.m_82520_(Math.cos(a) * radius, h, Math.sin(a) * radius);
            double spin = 0.35 + h * 0.03;
            Vec3 v = new Vec3(-Math.sin(a) * spin, 0.08, Math.cos(a) * spin);
            float g = (float) (0.78 + RANDOM.nextDouble() * 0.2);
            cloud(level, p, v, 0.55F * g + 0.1F, 0.95F * g, 0.6F * g + 0.05F);
        }
        for (int i = 0; i < PieceFx.count(3); i++) {
            double a = RANDOM.nextDouble() * Math.PI * 2.0;
            add(level, ParticleTypes.f_123759_, base.m_82520_(Math.cos(a) * low, 0.1, Math.sin(a) * low), new Vec3(-Math.sin(a) * 0.2, 0.1, Math.cos(a) * 0.2));
        }
    }

    /** A ring of white cloud about {@code dir} (a shockwave of air). */
    static void ring(Vec3 at, Vec3 dir, double radius, int count, double speed, int rgb) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        Vec3 side = Math.abs(dir.f_82480_) > 0.9 ? new Vec3(1, 0, 0) : dir.m_82537_(new Vec3(0, 1, 0)).m_82541_();
        Vec3 up = side.m_82537_(dir).m_82541_();
        int n = PieceFx.count(count);
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2.0 / Math.max(1, n);
            Vec3 out = side.m_82490_(Math.cos(a)).m_82549_(up.m_82490_(Math.sin(a)));
            cloud(level, at.m_82549_(out.m_82490_(radius)), out.m_82490_(speed).m_82549_(dir.m_82490_(0.05)), (rgb >> 16 & 255) / 255.0F,
                (rgb >> 8 & 255) / 255.0F, (rgb & 255) / 255.0F);
        }
    }

    /** A flash of heat where a breath leaves the mouth. */
    static void flash(Vec3 at, double size) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level != null) {
            fireball(level, at, Vec3.f_82478_, size, (int) (10 * size));
        }
    }

    /**
     * One puff of fire: the game's cloud coloured by {@code heat} (1 white-hot, 0.8 yellow, 0.6 orange, 0.35 red), the hotter the
     * fewer.
     */
    static void fire(ClientLevel level, Vec3 at, Vec3 v, float heat) {
        float h = Math.max(0.0F, Math.min(1.0F, heat));
        float r;
        float g;
        float b;
        if (h > 0.8F) {
            float t = (h - 0.8F) / 0.2F;
            r = 1.0F;
            g = 0.82F + 0.16F * t;
            b = 0.38F + 0.42F * t;
        } else if (h > 0.55F) {
            float t = (h - 0.55F) / 0.25F;
            r = 1.0F;
            g = 0.5F + 0.32F * t;
            b = 0.1F + 0.28F * t;
        } else {
            float t = h / 0.55F;
            r = 0.62F + 0.38F * t;
            g = 0.12F + 0.38F * t;
            b = 0.05F + 0.05F * t;
        }
        cloud(level, at, v, r, g, b);
    }

    static void cloud(ClientLevel level, Vec3 at, Vec3 v, float r, float g, float b) {
        if (!PieceFx.near(at, 96.0)) {
            return;
        }
        Particle particle = Minecraft.m_91087_().f_91061_.m_107370_(ParticleTypes.f_123796_, at.f_82479_, at.f_82480_, at.f_82481_,
            v.f_82479_, v.f_82480_, v.f_82481_);
        if (particle != null) {
            particle.m_107253_(r, g, b);
        }
    }

    static void add(ClientLevel level, ParticleOptions type, Vec3 at, Vec3 v) {
        if (PieceFx.near(at, 96.0)) {
            level.m_7106_(type, at.f_82479_, at.f_82480_, at.f_82481_, v.f_82479_, v.f_82480_, v.f_82481_);
        }
    }

    /** A glowing spark (Photon: the only thing it is used for here). */
    static void spark(Vec3 at, Vec3 v, int argb, int life) {
        if (PieceFx.near(at, 64.0)) {
            PieceFx.at(PieceFx.SPARK, true, life, at).velocity(v).colors(argb, argb & 0x00FFFFFF).size(0.14, 0.02).drag(0.86).fall(0.02).spawn();
        }
    }

    static double r(double spread) {
        return (RANDOM.nextDouble() * 2.0 - 1.0) * spread;
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SHOTS.clear();
        TORNADOES.clear();
        EMBERS.clear();
        BANGS.clear();
        FLAMES.clear();
        CHARGES.clear();
        BITES.clear();
    }
}
