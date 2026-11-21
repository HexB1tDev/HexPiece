package com.hexhaki.piece.client.dragon;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.dragon.DragonForm;
import com.hexhaki.piece.network.C2SDragon;
import com.hexhaki.piece.network.PieceNetwork;
import com.hexhaki.piece.network.S2CDragon;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.7.0: every dragon the client sees (the server's S2CDragon: a player's form and how far into it), drawn over the player's own
 * movement: the human hidden while the dragon is out (and the first-person hands), the dragon drawn after the entities (never culled
 * with the player's small box: it is 14 blocks tall and 25 long), facing where its player looks, its flight blended in by how fast
 * they move, banked into turns. The local player's camera goes behind it ({@link DragonCamera}) and to third person while it is out
 * (back to first person after, if it was), and the transformation lifts them off the ground into the creative flight the server gives.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class DragonClient {
    /** The client's own clock (ticks): never jumps with the server's time. */
    private static long clock;
    private static boolean kitAdded;
    /** The local player was in first person when their dragon came out. */
    private static boolean fromFirstPerson;
    /** How long (ticks from the start) the transformation lifts the local player, and how fast. */
    static final int LIFT_TICKS = 12;
    static final double LIFT = 0.42;
    /** The flight blend's approach a tick. */
    static final float FLIGHT_EASE = 0.07F;

    /** One player's dragon on this client. */
    static final class Entry {
        int phase;
        long start;
        long hoverStart;
        float flight;
        float flightO;
        float yaw;
        float yawO;
        boolean turned;
        float bank;
        float bankO;
        double x;
        double y;
        double z;
        boolean placed;
        int colour;
        /**
         * 2.7.2: where the renderer last drew its mouth (world), and when (UoFx's clock; -1 never). 2.7.6: the inside of the gape, then
         * the mouth's own way out (unit).
         */
        final double[] mouth = new double[6];
        long mouthAge = -1;
        final float[] mouthOut = new float[6];
        /**
         * 2.9.0: where the head has been: per tick (newest at {@code newest}, a ring of {@link #HISTORY}) how far it had gone (blocks,
         * never less than {@link #CREEP} a tick) and its facing and pitch; the body is bent along it (DragonForm.Trail).
         */
        final double[] travel = new double[HISTORY];
        final float[] yawAt = new float[HISTORY];
        final float[] pitchAt = new float[HISTORY];
        int newest = -1;
        int count;
        final DragonForm.Trail trail = new DragonForm.Trail();
    }

    /** 2.9.0: how many ticks of the head's way are kept, and how far it is taken to creep a tick even when still (blocks). */
    static final int HISTORY = 160;
    static final double CREEP = 0.3;

    static final Map<Integer, Entry> ENTRIES = new HashMap<>();

    private DragonClient() {
    }

    public static void accept(S2CDragon message) {
        Minecraft minecraft = Minecraft.m_91087_();
        boolean self = minecraft.f_91074_ != null && minecraft.f_91074_.m_19879_() == message.id();
        Entry entry = ENTRIES.get(message.id());
        if (message.phase() == DragonForm.HUMAN) {
            if (entry != null) {
                ENTRIES.remove(message.id());
                DragonRenderer.forget(message.id());
                puff(minecraft.f_91073_, message.id(), 60);
            }
            if (self) {
                back(minecraft);
            }
            return;
        }
        if (entry == null) {
            entry = new Entry();
            ENTRIES.put(message.id(), entry);
            entry.hoverStart = clock - message.ticks() - 20;
        }
        entry.phase = message.phase();
        entry.start = clock - message.ticks();
        entry.colour = message.colour();
        if (message.phase() == DragonForm.DRAGON) {
            entry.hoverStart = entry.start;
        }
        if (message.phase() == DragonForm.FORMING && message.ticks() == 0) {
            puff(minecraft.f_91073_, message.id(), 70);
            if (self) {
                out(minecraft);
            }
        }
    }

    /** The local player's dragon came out: third person (if it was first), flying from the start. */
    static void out(Minecraft minecraft) {
        if (minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON) {
            minecraft.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);
            fromFirstPerson = true;
        }
    }

    /** It went back: first person again if that is where it came from (and the player has not changed the view since). */
    static void back(Minecraft minecraft) {
        if (fromFirstPerson && minecraft.f_91066_.m_92176_() == CameraType.THIRD_PERSON_BACK) {
            minecraft.f_91066_.m_92157_(CameraType.FIRST_PERSON);
        }
        fromFirstPerson = false;
    }

    /** A burst of cloud where the player's chest is: the dragon coming out of them or going back in. */
    static void puff(ClientLevel level, int id, int count) {
        if (level == null || !(level.m_6815_(id) instanceof Player player)) {
            return;
        }
        double cx = player.m_20185_();
        double cy = player.m_20186_() + 1.0;
        double cz = player.m_20189_();
        for (int i = 0; i < count; i++) {
            double a = i * 2.39996;
            double r = 0.3 + (i % 7) * 0.12;
            double vx = Math.cos(a) * 0.18;
            double vz = Math.sin(a) * 0.18;
            level.m_7106_(ParticleTypes.f_123796_, cx + Math.cos(a) * r, cy + (i % 5) * 0.25 - 0.4, cz + Math.sin(a) * r, vx, 0.05 + (i % 3) * 0.03, vz);
        }
    }

    /** How long the cloud field gathers as the dragon forms, and before it goes (ticks), how far out it reaches (blocks). */
    static final int CLOUD_TICKS = 40;
    static final int CLOUD_BACK = 26;
    static final double CLOUD_NEAR = 3.0;
    static final double CLOUD_FAR = 14.0;
    static final double CLOUD_HIGH = 9.0;
    private static final java.util.Random CLOUD_RANDOM = new java.util.Random();

    /**
     * A wide field of cloud round the player: {@code 3 + 9 x strength} puffs this tick, anywhere from {@link #CLOUD_NEAR} to
     * {@link #CLOUD_FAR} out and up to {@link #CLOUD_HIGH} high, each drifting round the player (the way the dragon coils) and a
     * little up.
     */
    static void clouds(ClientLevel level, Player player, long ticks, float strength) {
        int count = 3 + Math.round(9.0F * Math.max(0.0F, Math.min(1.0F, strength)));
        double cx = player.m_20185_();
        double cy = player.m_20186_();
        double cz = player.m_20189_();
        for (int i = 0; i < count; i++) {
            double a = CLOUD_RANDOM.nextDouble() * Math.PI * 2.0;
            double r = CLOUD_NEAR + Math.sqrt(CLOUD_RANDOM.nextDouble()) * (CLOUD_FAR - CLOUD_NEAR);
            double h = CLOUD_RANDOM.nextDouble() * CLOUD_RANDOM.nextDouble() * CLOUD_HIGH;
            double swirl = 0.12 + 0.10 * CLOUD_RANDOM.nextDouble();
            double vx = -Math.sin(a) * swirl - Math.cos(a) * 0.02;
            double vz = Math.cos(a) * swirl - Math.sin(a) * 0.02;
            level.m_7106_(ParticleTypes.f_123796_, cx + Math.cos(a) * r, cy + h, cz + Math.sin(a) * r, vx, 0.02 + 0.04 * CLOUD_RANDOM.nextDouble(), vz);
        }
    }

    /** Whether the player's human body is hidden now (the dragon is out). */
    static boolean hidden(int id, float partial) {
        Entry entry = ENTRIES.get(id);
        return entry != null && DragonForm.humanHidden(entry.phase, seconds(entry, partial));
    }

    /** Whether the player is a dragon now (its camera is the dragon's), and how far: 0 to 1 as it forms and goes. */
    static float presence(int id, float partial) {
        Entry entry = ENTRIES.get(id);
        if (entry == null) {
            return 0.0F;
        }
        float s = seconds(entry, partial);
        return switch (entry.phase) {
            case DragonForm.FORMING -> DragonForm.smooth(s / 1.5F);
            case DragonForm.UNFORMING -> 1.0F - DragonForm.smooth((s - 0.4F) / 1.0F);
            default -> 1.0F;
        };
    }

    static float seconds(Entry entry, float partial) {
        return (clock - entry.start + partial) / 20.0F;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clock++;
        Minecraft minecraft = Minecraft.m_91087_();
        if (!kitAdded) {
            kitAdded = true;
            ClientKits.KITS.put(Fruit.UO, new UoClient());
        }
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            return;
        }
        breath(minecraft);
        Iterator<Map.Entry<Integer, Entry>> it = ENTRIES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Entry> e = it.next();
            Entry entry = e.getValue();
            if (!(level.m_6815_(e.getKey()) instanceof Player player)) {
                it.remove();
                DragonRenderer.forget(e.getKey());
                continue;
            }
            follow(entry, player);
            long ticks = clock - entry.start;
            if (player == minecraft.f_91074_ && entry.phase == DragonForm.FORMING) {
                lift(minecraft.f_91074_, ticks);
            }
            // 2.7.0 (the owner: "add some cloud particles in a large area around the player when transforming / de-transforming")
            if (entry.phase == DragonForm.FORMING && ticks < CLOUD_TICKS) {
                clouds(level, player, ticks, 1.0F - ticks / (float) CLOUD_TICKS);
            } else if (entry.phase == DragonForm.UNFORMING && ticks >= DragonForm.UNFORM_TICKS - CLOUD_BACK) {
                clouds(level, player, ticks, (ticks - (DragonForm.UNFORM_TICKS - CLOUD_BACK)) / (float) CLOUD_BACK);
            }
        }
    }

    /** The flight blend from how far the player moved this tick, the facing turned toward the look, the bank from the turn. */
    static void follow(Entry entry, Player player) {
        double px = player.m_20185_();
        double py = player.m_20186_();
        double pz = player.m_20189_();
        double speed = 0.0;
        if (entry.placed) {
            double mx = px - entry.x;
            double my = py - entry.y;
            double mz = pz - entry.z;
            speed = Math.sqrt(mx * mx + mz * mz + my * my * 0.5);
        }
        entry.x = px;
        entry.y = py;
        entry.z = pz;
        entry.placed = true;
        entry.flightO = entry.flight;
        float target = entry.phase == DragonForm.DRAGON || entry.phase == DragonForm.UNFORMING ? DragonForm.flightTarget(speed) : 0.0F;
        // eased over about a second: the body uncoils (its tail's tip sweeps some 60 blocks) rather than whips
        entry.flight += (target - entry.flight) * FLIGHT_EASE;
        float look = player.m_146908_();
        entry.yawO = entry.yaw;
        if (!entry.turned) {
            entry.yaw = look;
            entry.yawO = look;
            entry.turned = true;
        }
        float turn = wrap(look - entry.yaw);
        // 2.9.0 (the owner: "It just rotates on its own, like instantly as soon as the player turns"): the head comes round smoothly
        // (a third of the way a tick in flight, slower hovering) and the body follows the way the head went (DragonForm.Trail)
        float rate = 0.12F + 0.2F * entry.flight;
        entry.yaw += turn * rate;
        entry.bankO = entry.bank;
        float bankTarget = Math.max(-35.0F, Math.min(35.0F, -turn * rate * 2.2F * entry.flight));
        entry.bank += (bankTarget - entry.bank) * 0.2F;
        int at = (entry.newest + 1) % HISTORY;
        double before = entry.count == 0 ? 0.0 : entry.travel[entry.newest];
        entry.travel[at] = before + Math.max(CREEP, speed);
        entry.yawAt[at] = entry.yaw;
        entry.pitchAt[at] = player.m_146909_() * 0.85F * entry.flight;
        entry.newest = at;
        entry.count = Math.min(HISTORY, entry.count + 1);
    }

    /**
     * 2.9.0: the body's trail at {@code partial}: for the pelvis and each tail bone ({@code behind} blocks behind the face, DragonForm's
     * Shape), where the head was when it had gone that much less far, and how far its facing and pitch then lag the head's now.
     */
    static void trail(Entry entry, float partial, float yawNow, float pitchNow, float[] behind) {
        DragonForm.Trail trail = entry.trail;
        if (entry.count < 2 || behind == null) {
            java.util.Arrays.fill(trail.yaw, 0.0F);
            java.util.Arrays.fill(trail.pitch, 0.0F);
            return;
        }
        int last = entry.newest;
        int prev = (last - 1 + HISTORY) % HISTORY;
        double now = entry.travel[prev] + (entry.travel[last] - entry.travel[prev]) * partial;
        for (int i = 0; i < behind.length && i < trail.yaw.length; i++) {
            double want = now - behind[i];
            // walk back from the newest to the two ticks either side of that point
            int k = last;
            int steps = 0;
            while (steps < entry.count - 1 && entry.travel[k] > want) {
                k = (k - 1 + HISTORY) % HISTORY;
                steps++;
            }
            float yawThen;
            float pitchThen;
            if (entry.travel[k] > want) {
                yawThen = entry.yawAt[k]; // older than kept: the oldest
                pitchThen = entry.pitchAt[k];
            } else {
                int n = (k + 1) % HISTORY;
                double span = entry.travel[n] - entry.travel[k];
                float t = span < 1.0E-6 ? 0.0F : (float) Math.min(1.0, (want - entry.travel[k]) / span);
                if (k == last) {
                    t = 0.0F;
                    n = k;
                }
                yawThen = entry.yawAt[k] + wrap(entry.yawAt[n] - entry.yawAt[k]) * t;
                pitchThen = entry.pitchAt[k] + (entry.pitchAt[n] - entry.pitchAt[k]) * t;
            }
            trail.yaw[i] = wrap(yawNow - yawThen);
            trail.pitch[i] = pitchThen - pitchNow;
        }
    }

    static float wrap(float degrees) {
        float d = degrees % 360.0F;
        if (d >= 180.0F) {
            d -= 360.0F;
        }
        if (d < -180.0F) {
            d += 360.0F;
        }
        return d;
    }

    /** The transformation lifts the local player off the ground (flying, as the server set it) for its first ticks. */
    static void lift(LocalPlayer player, long ticks) {
        if (ticks < LIFT_TICKS) {
            player.m_150110_().f_35935_ = true;
            Vec3 motion = player.m_20184_();
            if (motion.f_82480_ < LIFT) {
                player.m_20334_(motion.f_82479_, LIFT, motion.f_82481_);
            }
        }
    }

    /** 2.7.2: whether the local player's breath is on (as last sent). */
    private static boolean flameSent;

    /** 2.7.3: the local player transformed at all (the dragon coming, out or going). */
    public static boolean selfOut() {
        Minecraft minecraft = Minecraft.m_91087_();
        return minecraft.f_91074_ != null && ENTRIES.containsKey(minecraft.f_91074_.m_19879_());
    }

    /** The local player a dragon now (out, not coming or going). */
    static boolean selfDragon() {
        Minecraft minecraft = Minecraft.m_91087_();
        Entry entry = minecraft.f_91074_ == null ? null : ENTRIES.get(minecraft.f_91074_.m_19879_());
        return entry != null && entry.phase == DragonForm.DRAGON;
    }

    /**
     * 2.7.2 (the owner: "M1 (Held): Open your massive jaw and unleash a fire breath as long as M1 is held"): a dragon's M1 is its breath,
     * never a punch or a block broken under it: while it is out the game's attack is taken here, and the breath follows the key's state.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void dragonAttack(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.m_91087_();
        Entry entry = minecraft.f_91074_ == null ? null : ENTRIES.get(minecraft.f_91074_.m_19879_());
        if (entry != null && event.isAttack()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    static void breath(Minecraft minecraft) {
        boolean want = selfDragon() && minecraft.f_91080_ == null && minecraft.f_91066_.f_92096_.m_90857_();
        if (want != flameSent) {
            flameSent = want;
            PieceNetwork.toServer(new C2SDragon(want));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void hideHuman(RenderPlayerEvent.Pre event) {
        if (!ENTRIES.isEmpty() && hidden(event.getEntity().m_19879_(), event.getPartialTick())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void hideHands(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (!ENTRIES.isEmpty() && minecraft.f_91074_ != null && hidden(minecraft.f_91074_.m_19879_(), event.getPartialTick())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || ENTRIES.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            return;
        }
        float partial = event.getPartialTick();
        Vec3 camera = event.getCamera().m_90583_();
        PoseStack stack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        boolean firstPerson = minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON;
        for (Map.Entry<Integer, Entry> e : ENTRIES.entrySet()) {
            Entry entry = e.getValue();
            float s = seconds(entry, partial);
            if (!DragonForm.shown(entry.phase, s) || !(level.m_6815_(e.getKey()) instanceof Player player)) {
                continue;
            }
            if (player == minecraft.f_91074_ && firstPerson) {
                continue; // the eye is inside the dragon: only the world is seen
            }
            double x = player.f_19790_ + (player.m_20185_() - player.f_19790_) * partial;
            double y = player.f_19791_ + (player.m_20186_() - player.f_19791_) * partial;
            double z = player.f_19792_ + (player.m_20189_() - player.f_19792_) * partial;
            float flight = entry.flightO + (entry.flight - entry.flightO) * partial;
            float yaw = entry.yawO + wrap(entry.yaw - entry.yawO) * partial;
            float bank = entry.bankO + (entry.bank - entry.bankO) * partial;
            float pitch = (player.f_19860_ + (player.m_146909_() - player.f_19860_) * partial) * 0.85F * flight;
            float hover = (clock - entry.hoverStart + partial) / 20.0F;
            float age = (clock + partial) / 20.0F + (e.getKey() % 13) * 0.37F;
            int light = LevelRenderer.m_109541_(level, BlockPos.m_274561_(x, y + DragonForm.HEAD_HEIGHT, z));
            int overlay = OverlayTexture.f_118083_;
            trail(entry, partial, yaw, pitch, DragonRenderer.behind());
            DragonRenderer.draw(e.getKey(), DragonTextures.of(entry.colour), stack, buffers, x - camera.f_82479_, y - camera.f_82480_,
                z - camera.f_82481_, entry.phase, s, hover, flight, age, yaw, pitch, bank, light, overlay, UoFx.mouth(e.getKey(), partial),
                UoFx.bite(e.getKey(), partial), entry.mouthOut, entry.trail);
            entry.mouth[0] = x + entry.mouthOut[0];
            entry.mouth[1] = y + entry.mouthOut[1];
            entry.mouth[2] = z + entry.mouthOut[2];
            entry.mouth[3] = entry.mouthOut[3];
            entry.mouth[4] = entry.mouthOut[4];
            entry.mouth[5] = entry.mouthOut[5];
            entry.mouthAge = UoFx.clock();
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ENTRIES.clear();
        DragonRenderer.clear();
        fromFirstPerson = false;
        flameSent = false;
    }

    /** For DragonCamera: the entity's dragon, if it has one. */
    static Entry entry(Entity entity) {
        return entity == null ? null : ENTRIES.get(entity.m_19879_());
    }
}
