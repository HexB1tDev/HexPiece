package com.hexhaki.piece.client.fx;

import com.lowdragmc.photon.client.gameobject.emitter.data.material.TextureMaterial;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.NumberFunction;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.NumberFunction3;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleConfig;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import com.lowdragmc.photon.client.gameobject.particle.TileParticle;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * 2.1.2 (the owner: "since we use photon, add water particles to fish man, air particles for swings of moves and air blade"): HexPiece's
 * particles on Photon, ported from HexGhoul's {@code RcFx} (itself from Hex God of Death's {@code DeathFx}): no vanilla particle types.
 * A particle is a {@link Mote}: a white sprite ({@code textures/piece/particle}, HexGhoul's, plus a water drop) tinted per particle,
 * with drag and weight, wandering a little, fading between two colours as it grows or shrinks. Emitters are pooled, capped and batched
 * per tick and sprite. {@link #later} runs a piece of an effect a few ticks on (a bullet's splash when it arrives). Client-only, visual.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class PieceFx {
    public static final ResourceLocation MOTE = sprite("mote");
    public static final ResourceLocation PUFF = sprite("puff");
    public static final ResourceLocation STREAK = sprite("streak");
    public static final ResourceLocation RING = sprite("ring");
    public static final ResourceLocation SLASH = sprite("slash");
    public static final ResourceLocation SPARK = sprite("spark");
    public static final ResourceLocation STEAM = sprite("steam");
    public static final ResourceLocation FLAME = sprite("flame");
    public static final ResourceLocation EMBER = sprite("ember");
    public static final ResourceLocation DROP = sprite("drop");

    private static final List<ParticleEmitter> ACTIVE = new ArrayList<>();
    private static final Map<ParticleEmitter, Vec3> ORIGINS = new IdentityHashMap<>();
    private static final int MAX_EMITTERS = 192;
    private static final Map<String, ParticleEmitter> BATCH = new HashMap<>();
    private static long batchTick = Long.MIN_VALUE;
    private static final List<Object[]> LATER = new ArrayList<>();
    private static long clock;

    private PieceFx() {
    }

    static ResourceLocation sprite(String name) {
        return ResourceLocation.m_214293_("hexhaki", "textures/piece/particle/" + name + ".png");
    }

    /** 1 with all particles, 0.5 decreased, 0.15 minimal (the client's own particle setting). */
    public static float density() {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91066_ == null) {
            return 1.0F;
        }
        Object status = minecraft.f_91066_.m_231929_().m_231551_();
        int level = status instanceof Enum<?> mode ? mode.ordinal() : 0;
        return level == 0 ? 1.0F : level == 1 ? 0.5F : 0.15F;
    }

    /** How many of {@code wanted} to spawn, randomly rounded so low densities still show something. */
    public static int count(float wanted) {
        float scaled = wanted * density();
        int whole = (int) scaled;
        return whole + (Math.random() < scaled - whole ? 1 : 0);
    }

    /** True if {@code at} is within {@code range} blocks of the camera. */
    public static boolean near(Vec3 at, double range) {
        Minecraft minecraft = Minecraft.m_91087_();
        return minecraft.f_91063_ != null && minecraft.f_91063_.m_109153_().m_90583_().m_82557_(at) < range * range;
    }

    private static ParticleEmitter emitter(Vec3 at, int lifetime, ResourceLocation texture, boolean glow) {
        ParticleEmitter emitter = new ParticleEmitter();
        ParticleConfig config = emitter.config;
        config.setDuration(1);
        config.setLooping(false);
        config.setMaxParticles(2048);
        config.setParallelUpdate(false);
        config.setParallelRendering(false);
        config.emission.setEmissionRate(NumberFunction.constant(0));
        config.setStartLifetime(NumberFunction.constant(lifetime));
        config.setStartSpeed(NumberFunction.constant(0));
        config.setStartSize(new NumberFunction3(NumberFunction.constant(1), NumberFunction.constant(1), NumberFunction.constant(1)));
        config.physics.setEnable(false);
        config.material.setMaterial(new TextureMaterial(texture));
        config.material.setCull(false);
        config.material.setDepthTest(true);
        config.material.setDepthMask(false);
        if (glow) {
            config.material.getBlendMode().setDstColorFactor(GlStateManager.DestFactor.ONE);
        }
        config.renderer.setBloomEffect(glow);
        config.renderer.getCull().setEnable(false);
        register(emitter, at);
        return emitter;
    }

    /** This tick's shared emitter for one sprite. */
    public static ParticleEmitter batch(Vec3 near, int lifetime, ResourceLocation texture, boolean glow) {
        Minecraft minecraft = Minecraft.m_91087_();
        long now = minecraft.f_91073_ == null ? 0L : minecraft.f_91073_.m_46467_();
        if (now != batchTick) {
            BATCH.clear();
            batchTick = now;
        }
        String key = texture + (glow ? "+" : "-");
        ParticleEmitter emitter = BATCH.get(key);
        if (emitter == null || !emitter.m_107276_()) {
            emitter = emitter(near, lifetime, texture, glow);
            BATCH.put(key, emitter);
        }
        return emitter;
    }

    private static void register(ParticleEmitter emitter, Vec3 at) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null) {
            return;
        }
        ACTIVE.removeIf(each -> {
            boolean dead = !each.m_107276_();
            if (dead) {
                ORIGINS.remove(each);
            }
            return dead;
        });
        while (ACTIVE.size() >= MAX_EMITTERS) {
            ParticleEmitter oldest = ACTIVE.remove(0);
            ORIGINS.remove(oldest);
            oldest.remove(true);
        }
        emitter.setLevel(minecraft.f_91073_);
        emitter.m_107264_(at.f_82479_, at.f_82480_, at.f_82481_);
        ORIGINS.put(emitter, at);
        ACTIVE.add(emitter);
        minecraft.f_91061_.m_107344_(emitter);
    }

    /** One particle of {@code texture} at {@code at} in this tick's batch (glowing = additive). */
    public static Mote at(ResourceLocation texture, boolean glow, int lifetime, Vec3 at) {
        return new Mote(batch(at, lifetime, texture, glow)).world(at).life(lifetime);
    }

    /** Runs {@code piece} {@code ticks} client ticks from now (0: this tick's end). */
    public static void later(int ticks, Runnable piece) {
        LATER.add(new Object[] {clock + Math.max(0, ticks), piece});
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clock++;
        if (LATER.isEmpty()) {
            return;
        }
        List<Runnable> due = new ArrayList<>();
        for (Iterator<Object[]> it = LATER.iterator(); it.hasNext();) {
            Object[] entry = it.next();
            if ((Long) entry[0] <= clock) {
                due.add((Runnable) entry[1]);
                it.remove();
            }
        }
        for (Runnable piece : due) {
            try {
                piece.run();
            } catch (RuntimeException failure) {
                // an effect never stops the game
            }
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        for (ParticleEmitter emitter : ACTIVE) {
            emitter.remove(true);
        }
        ACTIVE.clear();
        ORIGINS.clear();
        BATCH.clear();
        LATER.clear();
    }

    /** A particle as a small body: drag, weight, wander, a colour fade, a size change, an opening over its first ticks. */
    public static final class Mote extends TileParticle {
        private final ParticleEmitter owner;
        private float drag = 0.96F;
        private float gravity;
        private float jitter;
        private int from = 0xFFFFFFFF;
        private int to = 0x00FFFFFF;
        private float size0 = 0.2F;
        private float size1 = 0.1F;
        private float bloom = 2.0F;

        Mote(ParticleEmitter emitter) {
            super(emitter, emitter.config, emitter.getRandomSource());
            this.owner = emitter;
        }

        public Mote world(Vec3 world) {
            Vec3 origin = ORIGINS.getOrDefault(this.owner, Vec3.f_82478_);
            Vec3 local = world.m_82546_(origin);
            this.setLocalPos(new Vector3f((float) local.f_82479_, (float) local.f_82480_, (float) local.f_82481_), true);
            return this;
        }

        /** Velocity in blocks per tick. */
        public Mote velocity(Vec3 velocity) {
            this.setInternalVelocity(new Vector3f((float) velocity.f_82479_, (float) velocity.f_82480_, (float) velocity.f_82481_));
            return this;
        }

        public Mote colors(int from, int to) {
            this.from = from;
            this.to = to;
            this.setARGBColor(from);
            return this;
        }

        public Mote size(double from, double to) {
            this.size0 = (float) from;
            this.size1 = (float) to;
            this.setSize(new Vector3f(this.size0, this.size0, this.size0));
            return this;
        }

        public Mote life(int ticks) {
            this.setLifetime(Math.max(1, ticks));
            return this;
        }

        public Mote drag(double drag) {
            this.drag = (float) drag;
            return this;
        }

        /** Weight in blocks per tick per tick, positive down. */
        public Mote fall(double gravity) {
            this.gravity = (float) gravity;
            return this;
        }

        public Mote wander(double jitter) {
            this.jitter = (float) jitter;
            return this;
        }

        /** Ticks to open in (0: simply there). */
        public Mote bloom(double ticks) {
            this.bloom = (float) ticks;
            return this;
        }

        public void spawn() {
            this.owner.emitParticle(this);
        }

        @Override
        protected void update() {
            this.velocityY -= this.gravity;
            if (this.jitter != 0.0F) {
                this.velocityX += (this.randomSource.m_188501_() - 0.5F) * this.jitter;
                this.velocityY += (this.randomSource.m_188501_() - 0.5F) * this.jitter * 0.5F;
                this.velocityZ += (this.randomSource.m_188501_() - 0.5F) * this.jitter;
            }
            this.velocityX *= this.drag;
            this.velocityY *= this.drag;
            this.velocityZ *= this.drag;
            float t = this.lifetime > 0 ? Math.min(1.0F, this.age / (float) this.lifetime) : 0.0F;
            float a0 = ((this.from >>> 24) & 255) / 255.0F;
            float a1 = ((this.to >>> 24) & 255) / 255.0F;
            float open = this.bloom <= 0.0F ? 1.0F : Math.min(1.0F, this.age / this.bloom);
            this.setColor(new Vector4f(
                lerp(((this.from >> 16) & 255) / 255.0F, ((this.to >> 16) & 255) / 255.0F, t),
                lerp(((this.from >> 8) & 255) / 255.0F, ((this.to >> 8) & 255) / 255.0F, t),
                lerp((this.from & 255) / 255.0F, (this.to & 255) / 255.0F, t),
                lerp(a0, a1, t) * open * open * (3.0F - 2.0F * open)));
            float size = lerp(this.size0, this.size1, t);
            this.setSize(new Vector3f(size, size, size));
            super.update();
        }

        private static float lerp(float a, float b, float t) {
            return a + (b - a) * t;
        }
    }
}
