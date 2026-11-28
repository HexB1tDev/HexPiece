package com.hexhaki.piece.client.itsumade;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.HexPiece;
import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.DragonPose;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.itsumade.ItsuForm;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.ItsuMoves;
import com.hexhaki.piece.server.ItsuSaint;
import com.hexhaki.piece.server.ItsuServer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 3.9.4 (the owner: "New Fruit ... Rarity: Mythical Type: Zoan ... Z - Transforms (DIsturbing progressive transformation)", "Fruit "????"
 * Renamed to "Itsumade"", "Size: 10 blocks tall, big wing span", "All fire should feel alive ... act like Tori Tori No Mi", "The fire
 * all of it also has to glow, black fire glows black, the rest glows orange"; then, of the shells round it: "This is not glow. This is
 * smoke ... I don't like it, and it's ugly": none now, its fire is bright on its own): the owner's Itsumade drawn over a player in its form
 * (server/ItsuServer), as the phoenix is (client/phoenix/PhoenixFx: the model skinned on the CPU each frame, written straight into the
 * game's entity buffers, drawn both sides): its own colours (a colour a vertex and its head's texture, tools/itsumade_bake.py) shaded by a
 * fixed light on its own form and by the world's light where it stands; its fire full bright, alive (ItsuForm.flow) and hot
 * (ItsuForm.heat). Its pose
 * follows how the player moves (ItsuForm.pose: hovering, flying slow and fast, gliding, diving, flaring to land, the touchdown, standing
 * level as a bird on the ground and stalking). The change (ItsuForm.change, size): it tears out of the convulsing human in lurches and
 * folds back into it. The human is hidden while it is out; one's own camera pulls back to see it whole.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class ItsuFx {
    static final ResourceLocation TEXTURE = ResourceLocation.m_214293_("hexhaki", "textures/piece/itsumade/itsumade.png");
    static long clock;
    private static boolean kitAdded;
    private static boolean fromFirstPerson;
    private static DragonModel model;
    private static ItsuForm.Shape shape;
    /** How high its chest stands over its feet (ItsuForm.chestHeight), found once. */
    private static float chestHeight = 3.0F;
    private static boolean failed;
    /** Its own shading: a light from above, ahead and to its left (model space), and how dark its shade side goes. */
    static final float[] LIGHT = {0.35F, 0.8F, -0.45F};
    static final float SHADE = 0.55F;

    /** One player's Itsumade on this client. */
    static final class Entry {
        int phase;
        long start;
        final ItsuForm.Motion motion = new ItsuForm.Motion();
        final ItsuForm.Motion was = new ItsuForm.Motion();
        float stride;
        float strideO;
        long landed = -100;
        boolean grounded;
        float yaw;
        float yawO;
        boolean turned;
        float bank;
        float bankO;
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;
        boolean placed;
        double fell;
        DragonPose pose;
        float[][] pos;
        float[][] nrm;
        float drawnScale;
        float alive;
        boolean shown;
        /** The human's model scaled this frame (RenderPlayerEvent.Pre pushed, Post pops). */
        boolean scaled;
        /** 3.9.7: its moves (server/ItsuMoves): when the gust and the bite began, the Bow's end, the beam's charge, the beam, the grip. */
        long gustFrom = -1000;
        long biteFrom = -1000;
        long bowUntil = -1;
        long chargeFrom = -1;
        long beamFrom = -1;
        int beamLife;
        double beamWidth;
        double beamLength;
        double beamCharge;
        double[] beamWay = new double[3];
        boolean gripping;
        float grip;
        /** Its mouth this frame (world) and points on its skin (world, 3 a point) for the Bow's bolts. */
        final double[] mouth = new double[3];
        final double[] mouthWay = new double[3];
        double[] skin = new double[0];
    }

    static final Map<Integer, Entry> ENTRIES = new HashMap<>();

    private ItsuFx() {
    }

    static DragonModel model() {
        if (model == null && !failed) {
            try (InputStream in = ItsuFx.class.getResourceAsStream("/assets/hexhaki/itsumade/itsumade.sry")) {
                if (in == null) {
                    throw new java.io.IOException("assets/hexhaki/itsumade/itsumade.sry is missing");
                }
                model = DragonModel.read(in);
                shape = new ItsuForm.Shape(model);
                chestHeight = ItsuForm.chestHeight(model, shape);
            } catch (java.io.IOException | RuntimeException e) {
                failed = true;
                HexPiece.LOGGER.error("HexPiece: the Itsumade's model could not be read", e);
            }
        }
        return model;
    }

    /** client/dragon/UoFx hands on the Itsumade's messages (server/ItsuServer: 440 to 459). */
    public static void accept(S2CUoFx m) {
        if (m.kind() >= com.hexhaki.piece.server.ItsuGates.FORM && m.kind() <= com.hexhaki.piece.server.ItsuGates.END) {
            ItsuGateFx.accept(m); // 3.9.9: Hellish Teleport
            return;
        }
        if (m.kind() >= ItsuSaint.STARE && m.kind() <= ItsuSaint.PULSE) {
            ItsuSaintFx.accept(m); // 3.9.8: the human's moves
            return;
        }
        if (m.kind() == ItsuServer.SMOKE) {
            smoke(m.x(), m.y(), m.z(), m.bx());
            return;
        }
        if (m.kind() >= ItsuMoves.GUST && m.kind() <= ItsuMoves.GRIP) {
            move(m);
            return;
        }
        if (m.kind() != ItsuServer.PHASE) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        boolean self = minecraft.f_91074_ != null && minecraft.f_91074_.m_19879_() == m.user();
        int phase = Math.round(m.bx());
        Entry entry = ENTRIES.get(m.user());
        if (phase == ItsuForm.HUMAN) {
            ENTRIES.remove(m.user());
            if (self) {
                back(minecraft);
            }
            return;
        }
        long start = clock - m.ticks();
        if (entry == null) {
            entry = new Entry();
            ENTRIES.put(m.user(), entry);
        } else if (entry.phase == phase && Math.abs(entry.start - start) <= 2) {
            return; // told again (for whoever came near): nothing new
        }
        entry.phase = phase;
        entry.start = start;
        if (phase == ItsuForm.FORMING && m.ticks() == 0 && self) {
            out(minecraft);
        }
    }

    /**
     * 3.9.7 (the owner's passive: "he vanishes in a black smoke and appears somewhere safe 10 blocks away"): a burst of black smoke the
     * height of a body at the point: the dense dark smoke rolling out and up, thinner wisps round it.
     */
    static void smoke(double x, double y, double z, double height) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 70; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            double r = 0.15 + random.nextDouble() * 0.5;
            double h = random.nextDouble() * height;
            level.m_7106_(net.minecraft.core.particles.ParticleTypes.f_123796_, x + Math.cos(a) * r, y + h, z + Math.sin(a) * r,
                Math.cos(a) * 0.06, 0.03 + random.nextDouble() * 0.05, Math.sin(a) * 0.06);
        }
        for (int i = 0; i < 30; i++) {
            double a = random.nextDouble() * Math.PI * 2.0;
            level.m_7106_(net.minecraft.core.particles.ParticleTypes.f_123762_, x + Math.cos(a) * 0.4, y + random.nextDouble() * height,
                z + Math.sin(a) * 0.4, Math.cos(a) * 0.12, 0.05, Math.sin(a) * 0.12);
        }
    }

    /** 3.9.7: one of its moves (server/ItsuMoves) begun, struck or ended. */
    static void move(S2CUoFx m) {
        Entry entry = ENTRIES.get(m.user());
        Minecraft minecraft = Minecraft.m_91087_();
        if (entry == null || minecraft.f_91073_ == null) {
            return;
        }
        switch (m.kind()) {
            case ItsuMoves.GUST -> entry.gustFrom = clock;
            case ItsuMoves.BITE -> entry.biteFrom = clock;
            case ItsuMoves.BOW -> entry.bowUntil = m.id() == 1 ? clock + m.ticks() : -1;
            case ItsuMoves.CHARGE -> entry.chargeFrom = m.id() == 1 ? clock : -1;
            case ItsuMoves.BEAM -> {
                entry.chargeFrom = -1;
                entry.beamFrom = clock;
                entry.beamLife = m.ticks();
                entry.beamWidth = m.bx();
                entry.beamLength = m.by();
                entry.beamCharge = m.size();
                double l = Math.sqrt(m.ax() * m.ax() + m.ay() * m.ay() + m.az() * m.az());
                entry.beamWay[0] = m.ax() / Math.max(1.0E-6, l);
                entry.beamWay[1] = m.ay() / Math.max(1.0E-6, l);
                entry.beamWay[2] = m.az() / Math.max(1.0E-6, l);
            }
            case ItsuMoves.BEAM_OFF -> entry.beamFrom = -1;
            case ItsuMoves.GRIP -> entry.gripping = m.bx() > 0.5F;
            case ItsuMoves.BOLT -> bolt(minecraft.f_91073_, entry, m.user(), m.x(), m.y(), m.z(), m.id());
            default -> {
            }
        }
    }

    /**
     * 3.9.7 (the owner's Bow: "THe lightning never comes from the inside of the transformation model"): one bolt of the Conqueror's
     * lightning from the point of its skin nearest the target (as it was drawn last) out to it.
     */
    static void bolt(ClientLevel level, Entry entry, int user, double tx, double ty, double tz, long seed) {
        double[] skin = entry.skin;
        if (skin.length < 3) {
            return;
        }
        int best = 0;
        double bestD = Double.MAX_VALUE;
        for (int k = 0; k < skin.length / 3; k++) {
            double dx = skin[k * 3] - tx;
            double dy = skin[k * 3 + 1] - ty;
            double dz = skin[k * 3 + 2] - tz;
            double d = dx * dx + dy * dy + dz * dz;
            if (d < bestD) {
                bestD = d;
                best = k;
            }
        }
        double bx = skin[best * 3];
        double by = skin[best * 3 + 1];
        double bz = skin[best * 3 + 2];
        net.minecraft.world.entity.Entity owner = level.m_6815_(user);
        com.hexhaki.piece.client.haki.HakiBolts.sparks(level, owner, bx, by, bz, com.hexhaki.piece.haki.ConquerorSparks.wifi(new double[3],
            new double[] {tx - bx, ty - by, tz - bz}, 0.9, 3, seed), 0.0, 0.0F);
        com.hexhaki.piece.client.haki.HakiBolts.zap(level, tx, ty, tz, 0.7F);
    }

    /** One's own Itsumade came out: third person (if it was first). */
    static void out(Minecraft minecraft) {
        if (minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON) {
            minecraft.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);
            fromFirstPerson = true;
        }
    }

    /** It went back: first person again if that is where it came from. */
    static void back(Minecraft minecraft) {
        if (fromFirstPerson && minecraft.f_91066_.m_92176_() == CameraType.THIRD_PERSON_BACK) {
            minecraft.f_91066_.m_92157_(CameraType.FIRST_PERSON);
        }
        fromFirstPerson = false;
    }

    static float ticks(Entry entry, float partial) {
        return clock - entry.start + partial;
    }

    /** How far the player is the Itsumade now (0 to 1): its camera pulls back as it grows. */
    static float presence(Entry entry, float partial) {
        return Math.max(0.0F, Math.min(1.0F, ItsuForm.size(entry.phase, ticks(entry, partial))));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clock++;
        if (!kitAdded) {
            kitAdded = true;
            ClientKits.KITS.put(Fruit.ITSU, new ItsuClient());
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || ENTRIES.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<Integer, Entry>> it = ENTRIES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Entry> e = it.next();
            if (!(level.m_6815_(e.getKey()) instanceof Player player)) {
                it.remove();
                continue;
            }
            follow(e.getValue(), player);
            if (clock - e.getValue().gustFrom == ItsuMoves.GUST_AT) {
                gust(level, player);
            }
        }
    }

    /**
     * 3.9.7: the Wind Gust's wind as the wings come down: dust and air thrown out over the cone ahead (fifty degrees either side, the
     * gust's 24 blocks), fast near, slowing as it spreads.
     */
    static void gust(ClientLevel level, Player player) {
        Vec3 look = player.m_20154_();
        double yaw = Math.atan2(-look.f_82479_, look.f_82481_);
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 90; i++) {
            double a = yaw + Math.toRadians((random.nextDouble() * 2.0 - 1.0) * 50.0);
            double fx = -Math.sin(a);
            double fz = Math.cos(a);
            double speed = 0.9 + random.nextDouble() * 1.4;
            double side = (random.nextDouble() * 2.0 - 1.0) * 6.0;
            double x = player.m_20185_() + fx * 3.0 - fz * side;
            double z = player.m_20189_() + fz * 3.0 + fx * side;
            double y = player.m_20186_() + 0.5 + random.nextDouble() * 6.0;
            level.m_7106_(net.minecraft.core.particles.ParticleTypes.f_123759_, x, y, z, fx * speed, -0.05 + random.nextDouble() * 0.1,
                fz * speed);
        }
    }

    /** How high over the ground (blocks) it starts to brake to land when dropping. */
    static final double FLARE_HEIGHT = 7.0;
    /** Blocks walked a stride (both feet). */
    static final float STRIDE = 7.0F;

    /**
     * Its motion from how the player moves this tick (each eased): its speed, gliding when fast and level, diving when dropping fast,
     * standing and walking on the ground, flaring as it drops close over the ground, the touchdown; its facing turned toward the look
     * and banked into the turn.
     */
    static void follow(Entry entry, Player player) {
        double px = player.m_20185_();
        double py = player.m_20186_();
        double pz = player.m_20189_();
        if (entry.placed) {
            entry.vx = px - entry.x;
            entry.vy = py - entry.y;
            entry.vz = pz - entry.z;
        }
        entry.x = px;
        entry.y = py;
        entry.z = pz;
        entry.placed = true;
        ItsuForm.Motion mo = entry.motion;
        ItsuForm.Motion was = entry.was;
        was.speed = mo.speed;
        was.glide = mo.glide;
        was.dive = mo.dive;
        was.perch = mo.perch;
        was.walk = mo.walk;
        was.flare = mo.flare;
        double flat = Math.sqrt(entry.vx * entry.vx + entry.vz * entry.vz);
        boolean ground = player.m_20096_();
        float speed = ground ? 0.0F : (float) Math.min(1.0, flat / 0.9);
        float dive = ground ? 0.0F : (float) Math.max(0.0, Math.min(1.0, (-entry.vy - 0.35) / 0.45));
        float glide = !ground && speed > 0.7F && entry.vy < 0.05 && entry.vy > -0.35 ? 1.0F : 0.0F;
        float perch = ground ? 1.0F : 0.0F;
        float walk = ground ? (float) Math.min(1.0, flat / 0.18) : 0.0F;
        float flare = !ground && entry.vy < -0.05 && groundBelow(player) < FLARE_HEIGHT ? 1.0F : 0.0F;
        if (ground && !entry.grounded && entry.fell < -0.12) {
            entry.landed = clock;
        }
        entry.grounded = ground;
        entry.fell = entry.vy;
        mo.speed += (speed - mo.speed) * 0.12F;
        mo.dive += (dive - mo.dive) * 0.1F;
        mo.glide += (glide - mo.glide) * (glide > mo.glide ? 0.025F : 0.12F);
        mo.perch += (perch - mo.perch) * (perch > mo.perch ? 0.25F : 0.18F);
        mo.walk += (walk - mo.walk) * 0.2F;
        mo.flare += (flare - mo.flare) * 0.2F;
        entry.strideO = entry.stride;
        if (ground) {
            entry.stride += (float) (flat / STRIDE * 2.0 * Math.PI);
            if (entry.stride > 2000.0F * Math.PI) {
                entry.stride -= (float) (2000.0 * Math.PI);
                entry.strideO -= (float) (2000.0 * Math.PI);
            }
        }
        float look = player.m_146908_();
        entry.yawO = entry.yaw;
        if (!entry.turned) {
            entry.yaw = look;
            entry.yawO = look;
            entry.turned = true;
        }
        float turn = wrap(look - entry.yaw);
        entry.yaw += turn * (ground ? 0.25F : 0.35F);
        entry.bankO = entry.bank;
        float bankTarget = Math.max(-35.0F, Math.min(35.0F, -turn * 0.9F * (0.3F + 0.7F * mo.speed) * (1.0F - mo.perch)));
        entry.bank += (bankTarget - entry.bank) * 0.18F;
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

    /** How far below the player's feet the ground is (blocks; more than 12: none near). */
    static double groundBelow(Player player) {
        Vec3 from = player.m_20182_();
        Vec3 to = from.m_82520_(0.0, -12.0, 0.0);
        BlockHitResult hit = player.m_9236_().m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
        return hit.m_6662_() == HitResult.Type.BLOCK ? from.f_82480_ - hit.m_82450_().f_82480_ : 13.0;
    }

    /** The human hidden once the creature is out (or more than half out of it). */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void hideHuman(RenderPlayerEvent.Pre event) {
        if (ENTRIES.isEmpty()) {
            return;
        }
        Entry entry = ENTRIES.get(event.getEntity().m_19879_());
        if (entry != null && ItsuForm.humanHidden(entry.phase, ticks(entry, event.getPartialTick()))) {
            event.setCanceled(true);
        }
    }

    /**
     * 3.9.5 (the owner: "Instead of like the player progressively transforming, no. Just coming out of me"): while it is still the
     * human, the human's own body swells in jolts (ItsuForm.humanScale: taller more than wider) as it convulses, until it is the
     * creature; going back, it shrinks back to itself. Last of the listeners (nothing cancels the drawing after it), undone in Post.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void swell(RenderPlayerEvent.Pre event) {
        if (ENTRIES.isEmpty()) {
            return;
        }
        Entry entry = ENTRIES.get(event.getEntity().m_19879_());
        if (entry == null || entry.scaled) {
            return;
        }
        float k = ItsuForm.humanScale(entry.phase, ticks(entry, event.getPartialTick()));
        if (k > 1.001F) {
            PoseStack pose = event.getPoseStack();
            pose.m_85836_();
            float wide = (float) Math.pow(k, 0.85);
            pose.m_85841_(wide, k, wide);
            entry.scaled = true;
        }
    }

    @SubscribeEvent
    public static void swellDone(RenderPlayerEvent.Post event) {
        if (ENTRIES.isEmpty()) {
            return;
        }
        Entry entry = ENTRIES.get(event.getEntity().m_19879_());
        if (entry != null && entry.scaled) {
            event.getPoseStack().m_85849_();
            entry.scaled = false;
        }
    }

    @SubscribeEvent
    public static void hideHands(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        Entry entry = minecraft.f_91074_ == null || ENTRIES.isEmpty() ? null : ENTRIES.get(minecraft.f_91074_.m_19879_());
        if (entry != null && ItsuForm.humanHidden(entry.phase, ticks(entry, event.getPartialTick()))) {
            event.setCanceled(true);
        }
    }

    private static final ItsuForm.Motion NOW = new ItsuForm.Motion();
    private static final float[] PLACE = new float[12];

    /** The Itsumades, after the entities. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || ENTRIES.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        DragonModel m = model();
        if (level == null || m == null) {
            return;
        }
        float partial = event.getPartialTick();
        Vec3 camera = event.getCamera().m_90583_();
        PoseStack stack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        boolean firstPerson = minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON;
        try {
            RenderType body = RenderType.m_110458_(TEXTURE);
            VertexConsumer out = buffers.m_6299_(body);
            for (Map.Entry<Integer, Entry> e : ENTRIES.entrySet()) {
                Entry entry = e.getValue();
                if (!(level.m_6815_(e.getKey()) instanceof Player player)) {
                    continue;
                }
                entry.shown = false;
                if (player == minecraft.f_91074_ && firstPerson) {
                    continue; // the eye is inside it: only the world is seen
                }
                draw(m, entry, player, level, stack, out, camera, partial, e.getKey());
            }
            buffers.m_109912_(body);
        } catch (RuntimeException failure) {
            HexPiece.LOGGER.error("HexPiece: an Itsumade could not be drawn", failure);
            ENTRIES.clear();
        }
    }

    static void draw(DragonModel m, Entry entry, Player player, ClientLevel level, PoseStack stack, VertexConsumer out, Vec3 camera,
                     float partial, int id) {
        float t = ticks(entry, partial);
        float size = ItsuForm.size(entry.phase, t);
        if (size < 0.02F) {
            return;
        }
        if (entry.pose == null) {
            entry.pose = new DragonPose(m);
            entry.pos = new float[m.parts.length][];
            entry.nrm = new float[m.parts.length][];
            for (int i = 0; i < m.parts.length; i++) {
                entry.pos[i] = new float[m.parts[i].vertices() * 3];
                entry.nrm[i] = new float[m.parts[i].vertices() * 3];
            }
        }
        ItsuForm.Motion mo = NOW;
        ItsuForm.Motion a = entry.was;
        ItsuForm.Motion b = entry.motion;
        boolean changing = entry.phase == ItsuForm.FORMING || entry.phase == ItsuForm.UNFORMING;
        mo.speed = changing ? 0.0F : a.speed + (b.speed - a.speed) * partial;
        mo.glide = changing ? 0.0F : a.glide + (b.glide - a.glide) * partial;
        mo.dive = changing ? 0.0F : a.dive + (b.dive - a.dive) * partial;
        mo.perch = changing ? 1.0F : a.perch + (b.perch - a.perch) * partial;
        mo.walk = changing ? 0.0F : a.walk + (b.walk - a.walk) * partial;
        mo.flare = changing ? 0.0F : a.flare + (b.flare - a.flare) * partial;
        float sinceLand = clock + partial - entry.landed;
        mo.land = sinceLand < 0.0F || sinceLand > 16.0F ? 0.0F : (float) Math.sin(sinceLand / 16.0 * Math.PI);
        mo.stride = entry.strideO + (entry.stride - entry.strideO) * partial;
        mo.look = player.f_19860_ + (player.m_146909_() - player.f_19860_) * partial;
        float bodyYaw = entry.yawO + wrap(entry.yaw - entry.yawO) * partial;
        mo.turn = changing ? 0.0F : wrap(player.m_146908_() - bodyYaw);
        mo.mouth = -1.0F;
        float now = clock + partial;
        float bite = (now - entry.biteFrom) / ItsuMoves.BITE_TICKS;
        mo.bite = changing || bite <= 0.0F || bite >= 1.0F ? 0.0F : bite;
        float gust = (now - entry.gustFrom) / ItsuMoves.GUST_TICKS;
        mo.gust = changing || gust < 0.0F || gust > 1.0F ? -1.0F : gust;
        entry.grip += ((entry.gripping ? 1.0F : 0.0F) - entry.grip) * 0.2F;
        mo.grip = entry.grip;
        mo.charge = entry.chargeFrom < 0 ? 0.0F : Math.min(1.0F, (now - entry.chargeFrom) / 80.0F);
        mo.pour = entry.beamFrom >= 0 && now - entry.beamFrom < entry.beamLife ? 1.0F : 0.0F;
        ItsuForm.change(entry.phase, t, mo);
        float seconds = (clock + partial) / 20.0F + (id % 17) * 0.31F;
        ItsuForm.pose(m, shape, entry.pose, mo, seconds);
        double x = player.f_19790_ + (player.m_20185_() - player.f_19790_) * partial;
        double y = player.f_19791_ + (player.m_20186_() - player.f_19791_) * partial;
        double z = player.f_19792_ + (player.m_20189_() - player.f_19792_) * partial;
        float yaw = entry.yawO + wrap(entry.yaw - entry.yawO) * partial;
        if (changing) {
            yaw = player.f_20884_ + wrap(player.f_20883_ - player.f_20884_) * partial; // as the human's body faces
        }
        float bank = changing ? 0.0F : entry.bankO + (entry.bank - entry.bankO) * partial;
        // climbing and dropping tilt it with the look, at speed
        float tilt = -mo.look * 0.35F * mo.speed * (1.0F - mo.perch);
        float s = ItsuForm.SCALE * size;
        // the anchor (3.9.5, the owner: "when you're landing, you go into the ground"): on the ground its feet's middle on the player's
        // feet; in the air its chest held chestHeight over the player's feet (its heave left to show), so it lands on its feet
        float[] g = entry.pose.global;
        int toeL = shape.leg[0][2];
        int toeR = shape.leg[1][2];
        float footY = Math.min(g[toeL * 12 + 7] - (shape.ry(toeL) - shape.feet), g[toeR * 12 + 7] - (shape.ry(toeR) - shape.feet));
        float ax = (g[toeL * 12 + 3] + g[toeR * 12 + 3]) * 0.5F;
        float az = (g[toeL * 12 + 11] + g[toeR * 12 + 11]) * 0.5F;
        float cx = g[shape.chest * 12 + 3];
        float cy = g[shape.chest * 12 + 7] - ItsuForm.heave(mo, seconds) - chestHeight;
        float cz = g[shape.chest * 12 + 11];
        float p = mo.perch;
        float[] anchor = {cx + (ax - cx) * p, cy + (footY - cy) * p, cz + (az - cz) * p};
        placement(x - camera.f_82479_, y - camera.f_82480_, z - camera.f_82481_, yaw, tilt, bank, s, anchor, PLACE);
        Matrix4f view = stack.m_85850_().m_252922_();
        Vector3f o = view.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
        Vector3f vx = view.transformDirection(new Vector3f(1.0F, 0.0F, 0.0F));
        Vector3f vy = view.transformDirection(new Vector3f(0.0F, 1.0F, 0.0F));
        Vector3f vz = view.transformDirection(new Vector3f(0.0F, 0.0F, 1.0F));
        float[] v = {vx.x, vy.x, vz.x, o.x, vx.y, vy.y, vz.y, o.y, vx.z, vy.z, vz.z, o.z};
        float[] mat = mul(v, PLACE);
        Matrix3f turn = stack.m_85850_().m_252943_();
        Vector3f up = turn.transform(new Vector3f(0.0F, 1.0F, 0.0F));
        // the world's light where it stands (its body), full bright for its fire and its black ribbon
        int light = LevelRenderer.m_109541_(level, BlockPos.m_274561_(x, y + 1.0, z));
        float alive = changing ? mo.grown[ItsuForm.G_FIRE] : 1.0F;
        float wind = (1.0F - mo.perch) * (0.3F + 0.7F * mo.speed) + mo.perch * mo.walk * 0.35F;
        for (int i = 0; i < m.parts.length; i++) {
            DragonModel.Part part = m.parts[i];
            entry.pose.skin(part, entry.pos[i], entry.nrm[i]);
            ItsuForm.flow(shape, i, entry.pos[i], entry.nrm[i], seconds, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, wind, alive);
            ItsuForm.ribbonGrown(shape, entry.pose, i, entry.pos[i], mo.grown[ItsuForm.G_RIBBON]);
            emit(out, i, part, entry.pos[i], entry.nrm[i], mat, up, light, seconds, alive);
        }
        record(entry, m, camera);
        entry.drawnScale = s;
        entry.alive = alive;
        entry.shown = true;
    }

    /** Skin points kept for the Bow's bolts (every this many vertices of the body's parts). */
    static final int SKIN_EVERY = 97;

    /** 3.9.7: where its mouth is (its head bone's place, out along the beak) and points of its skin, in the world, as drawn now. */
    static void record(Entry entry, DragonModel m, Vec3 camera) {
        float[] g = entry.pose.global;
        int head = shape.head;
        double hx = g[head * 12 + 3];
        double hy = g[head * 12 + 7];
        double hz = g[head * 12 + 11];
        // the beak points along the head's -Z (the face toward -Z at rest), turned as the head is
        double fx = -g[head * 12 + 2];
        double fy = -g[head * 12 + 6];
        double fz = -g[head * 12 + 10];
        double fl = Math.max(1.0E-6, Math.sqrt(fx * fx + fy * fy + fz * fz));
        double mx = hx + fx / fl * 1.1;
        double my = hy + fy / fl * 1.1 - 0.15;
        double mz = hz + fz / fl * 1.1;
        for (int r = 0; r < 3; r++) {
            entry.mouth[r] = PLACE[r * 4] * mx + PLACE[r * 4 + 1] * my + PLACE[r * 4 + 2] * mz + PLACE[r * 4 + 3] + (r == 0 ? camera.f_82479_
                : r == 1 ? camera.f_82480_ : camera.f_82481_);
            entry.mouthWay[r] = (PLACE[r * 4] * fx + PLACE[r * 4 + 1] * fy + PLACE[r * 4 + 2] * fz) / fl;
        }
        double wl = Math.max(1.0E-6, Math.sqrt(entry.mouthWay[0] * entry.mouthWay[0] + entry.mouthWay[1] * entry.mouthWay[1]
            + entry.mouthWay[2] * entry.mouthWay[2]));
        for (int r = 0; r < 3; r++) {
            entry.mouthWay[r] /= wl;
        }
        if (entry.bowUntil < clock) {
            return;
        }
        java.util.List<double[]> points = new java.util.ArrayList<>();
        for (int i = 0; i < m.parts.length; i++) {
            if (shape.kind[i] != ItsuForm.BODY) {
                continue;
            }
            float[] pos = entry.pos[i];
            for (int v = (i * 13) % SKIN_EVERY; v < pos.length / 3; v += SKIN_EVERY) {
                double[] w = new double[3];
                for (int r = 0; r < 3; r++) {
                    w[r] = PLACE[r * 4] * pos[v * 3] + PLACE[r * 4 + 1] * pos[v * 3 + 1] + PLACE[r * 4 + 2] * pos[v * 3 + 2] + PLACE[r * 4 + 3]
                        + (r == 0 ? camera.f_82479_ : r == 1 ? camera.f_82480_ : camera.f_82481_);
                }
                points.add(w);
            }
        }
        double[] skin = new double[points.size() * 3];
        for (int k = 0; k < points.size(); k++) {
            System.arraycopy(points.get(k), 0, skin, k * 3, 3);
        }
        entry.skin = skin;
    }

    /**
     * A part's corners: its own colour (the bake's) shaded by its model-space normal against {@link #LIGHT} (the fire and the black
     * ribbon unshaded, the fire hotter where its heat runs: toward white-gold), the world's light (the fire and the ribbon full bright),
     * every normal the world's up (the game's own lights shade no side of it again).
     */
    static void emit(VertexConsumer out, int index, DragonModel.Part part, float[] pos, float[] nrm, float[] m, Vector3f up, int light,
                     float seconds, float alive) {
        int[] q = part.quads;
        float[] uv = part.uv;
        float[] col = part.col;
        int kind = shape.kind[index];
        boolean bright = kind == ItsuForm.FIRE || kind == ItsuForm.RIBBON;
        int lit = bright ? 0xF000F0 : light;
        for (int i = 0; i < q.length; i++) {
            int k = q[i];
            float r = col == null ? 1.0F : col[k * 3];
            float g = col == null ? 1.0F : col[k * 3 + 1];
            float b = col == null ? 1.0F : col[k * 3 + 2];
            if (kind == ItsuForm.FIRE) {
                // embers until it catches (the change), then its living fire
                float heat = 0.5F * alive * ItsuForm.heat(shape, index, k, seconds);
                float lit2 = 0.25F + 0.75F * alive;
                r = Math.min(1.0F, r * 1.1F * lit2 + heat);
                g = Math.min(1.0F, g * 1.1F * lit2 * lit2 + heat * 0.8F);
                b = Math.min(1.0F, b * 1.1F * lit2 * lit2 + heat * 0.35F);
            } else if (kind != ItsuForm.RIBBON) {
                float d = nrm[k * 3] * LIGHT[0] + nrm[k * 3 + 1] * LIGHT[1] + nrm[k * 3 + 2] * LIGHT[2];
                float shade = SHADE + (1.0F - SHADE) * Math.max(0.0F, d / 1.035F);
                r *= shade;
                g *= shade;
                b *= shade;
            }
            float x = pos[k * 3];
            float y = pos[k * 3 + 1];
            float z = pos[k * 3 + 2];
            out.m_5954_(m[0] * x + m[1] * y + m[2] * z + m[3], m[4] * x + m[5] * y + m[6] * z + m[7], m[8] * x + m[9] * y + m[10] * z + m[11],
                r, g, b, 1.0F, uv[k * 2], uv[k * 2 + 1], OverlayTexture.f_118083_, lit, up.x, up.y, up.z);
        }
    }

    /**
     * Model space to the camera's: the anchor (model point) lands on (x, y, z), turned to the game's {@code yaw} (the model faces -Z),
     * tilted nose up by {@code tilt}, banked by {@code bank}, scaled by {@code s}.
     */
    static void placement(double x, double y, double z, float yaw, float tilt, float bank, float s, float[] anchor, float[] out) {
        double ty = Math.toRadians(180.0 - yaw);
        double tx = Math.toRadians(tilt);
        double tz = Math.toRadians(bank);
        double cy = Math.cos(ty);
        double sy = Math.sin(ty);
        double cx = Math.cos(tx);
        double sx = Math.sin(tx);
        double cz = Math.cos(tz);
        double sz = Math.sin(tz);
        double[] ry = {cy, 0, sy, 0, 1, 0, -sy, 0, cy};
        double[] rx = {1, 0, 0, 0, cx, -sx, 0, sx, cx};
        double[] rz = {cz, -sz, 0, sz, cz, 0, 0, 0, 1};
        double[] r = mul3(ry, mul3(rx, rz));
        for (int i = 0; i < 3; i++) {
            out[i * 4] = (float) (r[i * 3] * s);
            out[i * 4 + 1] = (float) (r[i * 3 + 1] * s);
            out[i * 4 + 2] = (float) (r[i * 3 + 2] * s);
        }
        double[] at = {x, y, z};
        for (int i = 0; i < 3; i++) {
            out[i * 4 + 3] = (float) (at[i] - (r[i * 3] * anchor[0] + r[i * 3 + 1] * anchor[1] + r[i * 3 + 2] * anchor[2]) * s);
        }
    }

    static double[] mul3(double[] a, double[] b) {
        double[] o = new double[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                o[i * 3 + j] = a[i * 3] * b[j] + a[i * 3 + 1] * b[3 + j] + a[i * 3 + 2] * b[6 + j];
            }
        }
        return o;
    }

    /** a x b, both 3 x 4 affine row-major. */
    static float[] mul(float[] a, float[] b) {
        float[] o = new float[12];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 4; c++) {
                o[r * 4 + c] = a[r * 4] * b[c] + a[r * 4 + 1] * b[4 + c] + a[r * 4 + 2] * b[8 + c] + (c == 3 ? a[r * 4 + 3] : 0.0F);
            }
        }
        return o;
    }


    /** The house's fire quads (alpha blended, depth tested), camera-relative, through the game's own debugQuads type. */
    static final class Writer implements FireMesh.Sink {
        VertexConsumer buffer;
        Matrix4f matrix;

        @Override
        public void quad(double[] xyz, int[] argb) {
            for (int k = 0; k < 4; k++) {
                int c = argb[k];
                this.buffer.m_252986_(this.matrix, (float) xyz[k * 3], (float) xyz[k * 3 + 1], (float) xyz[k * 3 + 2])
                    .m_85950_(((c >>> 16) & 255) / 255.0F, ((c >>> 8) & 255) / 255.0F, (c & 255) / 255.0F, ((c >>> 24) & 255) / 255.0F).m_5752_();
            }
        }
    }

    static final Writer SINK = new Writer();

    /**
     * 3.9.7: the moves' fire, after the particles: the Yokai Beam's ball of white-hot fire gathering in the open jaws (black flames
     * licking round it, bigger the longer the charge), and the beam itself (the owner: "a beam of bright orange/black flames ... the
     * flames are super clean, beautiful effects like the dragon's breath but more intense"): a white-hot heart of orange fire along it
     * wrapped in rolling black flames, racing out from the jaws to the first block it meets, thinning away at its end.
     */
    @SubscribeEvent
    public static void effects(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ENTRIES.isEmpty()) {
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
        SINK.matrix = event.getPoseStack().m_85850_().m_252922_();
        boolean any = false;
        try {
            SINK.buffer = buffers.m_6299_(type);
            float now = clock + partial;
            for (Map.Entry<Integer, Entry> e : ENTRIES.entrySet()) {
                Entry entry = e.getValue();
                if (!entry.shown) {
                    continue;
                }
                double s = entry.drawnScale;
                double mx = entry.mouth[0] - camera.f_82479_;
                double my = entry.mouth[1] - camera.f_82480_;
                double mz = entry.mouth[2] - camera.f_82481_;
                if (entry.chargeFrom >= 0) {
                    any = true;
                    double c = Math.min(1.0, (now - entry.chargeFrom) / 80.0);
                    ball(mx, my, mz, s * (0.25 + 0.7 * c), now, e.getKey());
                }
                if (entry.beamFrom >= 0 && now - entry.beamFrom < entry.beamLife + 6) {
                    any = true;
                    beam(level, entry, camera, mx, my, mz, now - entry.beamFrom, e.getKey());
                }
            }
        } catch (RuntimeException failure) {
            HexPiece.LOGGER.error("HexPiece: an Itsumade's fire could not be drawn", failure);
        } finally {
            FireMesh.palette(FireMesh.BAND);
        }
        if (SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        SINK.buffer = null;
    }

    /** The ball of fire gathering in the jaws: a white-hot heart, an orange skin, black flames licking out round it (itsumade/ItsuArt). */
    static void ball(double x, double y, double z, double radius, float time, int seed) {
        com.hexhaki.piece.itsumade.ItsuArt.ball(SINK, x, y, z, radius, time, seed);
    }

    /** The beam out of the jaws along its way, as far as the first block (or its length), its front racing out, thinning at its end. */
    static void beam(ClientLevel level, Entry entry, Vec3 camera, double mx, double my, double mz, float age, int seed) {
        beamLine(level, camera, new Vec3(entry.mouth[0], entry.mouth[1], entry.mouth[2]), entry.beamWay, entry.beamLength, entry.beamWidth,
            entry.beamLife, age, seed, com.hexhaki.piece.dragon.UoFire.BEAM_SPEED, 4.0);
    }

    /**
     * 3.9.8: a beam of the Itsumade's fire from {@code from} (world) along {@code w} (unit), as far as the first block (or
     * {@code length}), its front racing out at {@code speed} blocks a tick, thin for its first {@code flare} blocks, fading out over 6
     * ticks after {@code life}: a white-hot heart of orange fire wrapped in rolling black flames. The Yokai Beam's and the Yokai Blast's
     * (client/itsumade/ItsuSaintFx); returns how far it reaches.
     */
    static double beamLine(ClientLevel level, Vec3 camera, Vec3 from, double[] w, double length, double widthAt, int life, float age, int seed,
                           double speed, double flare) {
        double front = Math.min(length, age * speed + 1.0);
        Vec3 to = from.m_82520_(w[0] * front, w[1] * front, w[2] * front);
        net.minecraft.world.phys.BlockHitResult hit = level.m_45547_(new net.minecraft.world.level.ClipContext(from, to,
            net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, null));
        double reach = hit.m_6662_() == net.minecraft.world.phys.HitResult.Type.BLOCK ? hit.m_82450_().m_82554_(from) : front;
        double fade = age > life ? Math.max(0.0, 1.0 - (age - life) / 6.0) : 1.0;
        double width = widthAt * fade * (0.9 + 0.1 * Math.sin(age * 1.7));
        com.hexhaki.piece.itsumade.ItsuArt.beam(SINK, from.f_82479_ - camera.f_82479_, from.f_82480_ - camera.f_82480_,
            from.f_82481_ - camera.f_82481_, w, reach, width, fade, age, seed, flare);
        return reach;
    }

    /**
     * client/dragon/DragonCamera asks: one's own camera, in third person (back) while the Itsumade is out, pulled back and up so it is
     * seen whole (it is ten blocks tall), easing out as it grows and in as it goes; null otherwise.
     */
    public static Vec3 camera(Entity entity, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        CameraType type = minecraft.f_91066_.m_92176_();
        if (ENTRIES.isEmpty() || entity != minecraft.f_91074_ || type == CameraType.FIRST_PERSON) {
            return null;
        }
        Entry entry = ENTRIES.get(entity.m_19879_());
        if (entry == null) {
            return null;
        }
        float out = presence(entry, partial);
        if (out <= 0.0F) {
            return null;
        }
        // 3.9.5 (the owner: "when you switch to one of the modes in third person, you're gonna see just his feet"): both views (behind
        // and in front, the look turned round), pulled back and raised to its body (its chest stands chestHeight over its feet)
        Vec3 eye = entity.m_20299_(partial);
        Vec3 look = entity.m_20252_(partial);
        if (type == CameraType.THIRD_PERSON_FRONT) {
            look = look.m_82490_(-1.0);
        }
        double back = 4.0 + 17.0 * out;
        double up = (chestHeight * ItsuForm.SCALE - 1.0) * out + 1.5 * out;
        Vec3 raised = com.hexhaki.piece.client.dragon.DragonCamera.clip(entity, eye, eye.m_82520_(0.0, up, 0.0));
        return com.hexhaki.piece.client.dragon.DragonCamera.clip(entity, raised, raised.m_82520_(-look.f_82479_ * back, -look.f_82480_ * back,
            -look.f_82481_ * back));
    }

    /** Whether one's own player is the Itsumade (from the change to the way back). */
    public static boolean selfOut() {
        Minecraft minecraft = Minecraft.m_91087_();
        return minecraft.f_91074_ != null && ENTRIES.containsKey(minecraft.f_91074_.m_19879_());
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ENTRIES.clear();
        fromFirstPerson = false;
    }
}
