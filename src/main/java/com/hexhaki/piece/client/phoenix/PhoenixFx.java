package com.hexhaki.piece.client.phoenix;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.HexPiece;
import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.DragonPose;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.phoenix.PhoenixFire;
import com.hexhaki.piece.phoenix.PhoenixForm;
import com.hexhaki.piece.phoenix.PhoenixLook;
import com.hexhaki.piece.server.PhoenixServer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
 * 3.6.9 (the owner: "Tori Tori no Mi, Model: Phoenix ... I made the model ... animate it so it looks good, good flying animations";
 * "its a burning phoenix ... moving flames"; "the animations have to be top tier"): the owner's phoenix drawn over a player in its form
 * (server/PhoenixServer), as the dragon is (client/dragon/DragonRenderer's way: the model skinned on the CPU each frame, written
 * straight into the game's own entity buffers, drawn both sides): coloured as the owner's file colours it (phoenix/PhoenixLook's cel
 * ramp per vertex, full bright: it is fire), its fire flowing (PhoenixForm.flame) and tongues of flame streaming off it
 * (phoenix/PhoenixFire, the house's alpha-blended quads after the particles). Its pose follows how the player moves (PhoenixForm.pose:
 * hovering, flying, gliding, diving, standing on its claws); 3.7.0 it opens out of the human turned to fire limb by limb (its arms
 * rising as wings) and closes back into it as it goes (PhoenixForm's timeline, PhoenixFire.change). The human is hidden while it is
 * out; one's own camera pulls back to see it whole.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class PhoenixFx {
    static final ResourceLocation TEXTURE = ResourceLocation.m_214293_("hexhaki", "textures/piece/phoenix/marco.png");
    static long clock;
    private static boolean kitAdded;
    private static boolean fromFirstPerson;
    private static DragonModel model;
    private static PhoenixForm.Shape shape;
    private static boolean failed;

    /** One player's phoenix on this client. */
    static final class Entry {
        int phase;
        long start;
        final PhoenixForm.Motion motion = new PhoenixForm.Motion();
        final PhoenixForm.Motion was = new PhoenixForm.Motion();
        float beat;
        float beatO;
        /** 3.7.1: its stride's phase walking, and when its claws last touched down (the clock), whether it stood on the ground. */
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
        /** Its fall last tick (blocks a tick, down negative): a touchdown is a fall ending on the ground. */
        double fell;
        DragonPose pose;
        float[][] pos;
        float[][] nrm;
        /** The fire points this frame (world, camera-relative), for the tongues. */
        double[] points;
        /** Its glow's points this frame (camera-relative), and how big it was drawn (blocks a model unit). */
        double[] glow;
        float drawnScale;
        /**
         * 3.7.0: whether it was drawn this frame (its points are this frame's). Was: its points dropped (null) in first person and
         * never made again, so the next frame in third person threw, every phoenix was wiped and the human showed until the server
         * told it again (the owner: "whenever the player goes into third person it reverts to human and then back to phoenix").
         */
        boolean shown;
    }

    static final Map<Integer, Entry> ENTRIES = new HashMap<>();

    private PhoenixFx() {
    }

    static DragonModel model() {
        if (model == null && !failed) {
            try (InputStream in = PhoenixFx.class.getResourceAsStream("/assets/hexhaki/phoenix/marco.sry")) {
                if (in == null) {
                    throw new java.io.IOException("assets/hexhaki/phoenix/marco.sry is missing");
                }
                model = DragonModel.read(in);
                shape = new PhoenixForm.Shape(model);
            } catch (java.io.IOException | RuntimeException e) {
                failed = true;
                HexPiece.LOGGER.error("HexPiece: the phoenix's model could not be read", e);
            }
        }
        return model;
    }

    /** client/dragon/UoFx hands on the phoenix's messages (server/PhoenixServer: 400 to 419). */
    public static void accept(S2CUoFx m) {
        if (m.kind() != PhoenixServer.PHASE) {
            // 3.7.2: the moves' messages (server/PhoenixMoves: 401 to 405)
            PhoenixMoveFx.accept(m);
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        boolean self = minecraft.f_91074_ != null && minecraft.f_91074_.m_19879_() == m.user();
        int phase = Math.round(m.bx());
        Entry entry = ENTRIES.get(m.user());
        if (phase == PhoenixForm.HUMAN) {
            if (entry != null) {
                ENTRIES.remove(m.user());
            }
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
        if (phase == PhoenixForm.FORMING && m.ticks() == 0 && self) {
            out(minecraft);
        }
    }

    /** One's own phoenix came out: third person (if it was first). */
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

    /** How far the player is the phoenix now (0 to 1): its camera pulls back as it forms. */
    static float presence(Entry entry, float partial) {
        return Math.max(0.0F, Math.min(1.0F, PhoenixForm.grow(entry.phase, ticks(entry, partial))));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clock++;
        PhoenixMoveFx.tick();
        if (!kitAdded) {
            kitAdded = true;
            ClientKits.KITS.put(Fruit.TORI, new PhoenixClient());
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
        }
    }

    /**
     * Its flight from how the player moves this tick (PhoenixForm.Motion, each eased): its speed, gliding when fast and level,
     * diving when dropping fast, standing when on the ground; its wingbeat run on at the beat's own rate; its facing turned toward
     * the look and banked into the turn.
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
        PhoenixForm.Motion mo = entry.motion;
        PhoenixForm.Motion was = entry.was;
        was.speed = mo.speed;
        was.glide = mo.glide;
        was.dive = mo.dive;
        was.perch = mo.perch;
        was.walk = mo.walk;
        was.flare = mo.flare;
        was.land = mo.land;
        double flat = Math.sqrt(entry.vx * entry.vx + entry.vz * entry.vz);
        // 3.7.1 (the owner: "I also want a landing animation and being on ground/walk animation, not just flying"): on the ground it
        // stands and walks on its claws (wings folded), in the air it flies; dropping close over the ground it brakes, wings flared,
        // and the touchdown dips it
        boolean ground = player.m_20096_();
        float speed = ground ? 0.0F : (float) Math.min(1.0, flat / 0.9);
        float dive = ground ? 0.0F : (float) Math.max(0.0, Math.min(1.0, (-entry.vy - 0.35) / 0.45));
        float glide = !ground && speed > 0.7F && entry.vy < 0.05 && entry.vy > -0.35 ? 1.0F : 0.0F;
        float perch = ground ? 1.0F : 0.0F;
        float walk = ground ? (float) Math.min(1.0, flat / 0.2) : 0.0F;
        float flare = !ground && entry.vy < -0.05 && groundBelow(player) < FLARE_HEIGHT ? 1.0F : 0.0F;
        if (ground && !entry.grounded && entry.fell < -0.12) {
            entry.landed = clock;
        }
        entry.grounded = ground;
        entry.fell = entry.vy;
        mo.speed += (speed - mo.speed) * 0.15F;
        mo.dive += (dive - mo.dive) * 0.12F;
        // a glide sets in slowly (a few beats at speed first) and breaks off quickly
        mo.glide += (glide - mo.glide) * (glide > mo.glide ? 0.025F : 0.12F);
        mo.perch += (perch - mo.perch) * (perch > mo.perch ? 0.3F : 0.2F);
        mo.walk += (walk - mo.walk) * 0.3F;
        mo.flare += (flare - mo.flare) * 0.25F;
        mo.land = PhoenixForm.landing(clock - entry.landed);
        entry.strideO = entry.stride;
        if (ground) {
            entry.stride += (float) (flat / (shape == null ? 1.8F : PhoenixForm.stride(shape)) * 2.0 * Math.PI);
            if (entry.stride > 2000.0F * Math.PI) {
                entry.stride -= (float) (2000.0 * Math.PI);
                entry.strideO -= (float) (2000.0 * Math.PI);
            }
        }
        entry.beatO = entry.beat;
        entry.beat += PhoenixForm.beatRate(mo) / 20.0F;
        if (entry.phase == PhoenixForm.FORMING) {
            // still as it opens out of the figure of fire, then its first stroke down as it takes its own shape
            float t = clock - entry.start;
            entry.beat = (float) (-Math.PI * 0.5 + Math.max(0.0F, t - PhoenixForm.HUMAN_GONE) / 16.0F * Math.PI);
            entry.beatO = (float) (-Math.PI * 0.5 + Math.max(0.0F, t - 1.0F - PhoenixForm.HUMAN_GONE) / 16.0F * Math.PI);
        }
        if (entry.beat > 2000.0F * Math.PI) {
            entry.beat -= (float) (2000.0 * Math.PI);
            entry.beatO -= (float) (2000.0 * Math.PI);
        }
        float look = player.m_146908_();
        entry.yawO = entry.yaw;
        if (!entry.turned) {
            entry.yaw = look;
            entry.yawO = look;
            entry.turned = true;
        }
        float turn = wrap(look - entry.yaw);
        entry.yaw += turn * 0.4F;
        entry.bankO = entry.bank;
        float bankTarget = Math.max(-40.0F, Math.min(40.0F, -turn * 0.4F * 2.4F * (0.3F + 0.7F * mo.speed) * (1.0F - mo.perch)));
        entry.bank += (bankTarget - entry.bank) * 0.2F;
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void hideHuman(RenderPlayerEvent.Pre event) {
        // 3.7.6 (the owner: "the arms of the player SHOULD become the wings"): while a move's wings are out, the arms are the wings
        // (armWings lays one along each): the arms themselves hidden
        PhoenixMoveFx.Wings w = PhoenixMoveFx.WINGS.isEmpty() ? null : PhoenixMoveFx.WINGS.get(event.getEntity().m_19879_());
        if (w != null && !ENTRIES.containsKey(event.getEntity().m_19879_())
            && PhoenixForm.armHidden(clock + event.getPartialTick() - w.start, w.ticks) && event.getRenderer().m_7200_() instanceof PlayerModel<?> arms) {
            arms.f_102811_.f_104207_ = false;
            arms.f_102812_.f_104207_ = false;
            arms.f_103375_.f_104207_ = false;
            arms.f_103374_.f_104207_ = false;
        }
        if (ENTRIES.isEmpty()) {
            return;
        }
        Entry entry = ENTRIES.get(event.getEntity().m_19879_());
        if (entry == null) {
            return;
        }
        float t = ticks(entry, event.getPartialTick());
        if (PhoenixForm.humanHidden(entry.phase, t)) {
            event.setCanceled(true);
            return;
        }
        // 3.7.0 (the owner: "the players body slowly becomes the phoenix"): each limb the fire has taken is fire only (PhoenixFire's
        // figure of fire drawn there): the arms once it has run up them, the body, the head, then the legs. The renderer shows every
        // part again before each frame (PlayerRenderer.setModelProperties runs before this event), so nothing is put back.
        if ((entry.phase == PhoenixForm.FORMING || entry.phase == PhoenixForm.UNFORMING)
            && event.getRenderer().m_7200_() instanceof PlayerModel<?> model) {
            PhoenixForm.burning(entry.phase, t, BURNT);
            if (BURNT[2] < 0.5F) {
                return;
            }
            boolean arms = BURNT[0] > PhoenixForm.ARMS_TAKEN;
            boolean body = BURNT[1] > PhoenixForm.BODY_TAKEN;
            boolean head = BURNT[1] > PhoenixForm.HEAD_TAKEN;
            boolean legs = BURNT[1] > PhoenixForm.LEGS_TAKEN;
            model.f_102811_.f_104207_ &= !arms;
            model.f_102812_.f_104207_ &= !arms;
            model.f_103375_.f_104207_ &= !arms;
            model.f_103374_.f_104207_ &= !arms;
            model.f_102810_.f_104207_ &= !body;
            model.f_103378_.f_104207_ &= !body;
            model.f_102808_.f_104207_ &= !head;
            model.f_102809_.f_104207_ &= !head;
            model.f_102813_.f_104207_ &= !legs;
            model.f_102814_.f_104207_ &= !legs;
            model.f_103377_.f_104207_ &= !legs;
            model.f_103376_.f_104207_ &= !legs;
        }
    }

    private static final float[] BURNT = new float[3];

    @SubscribeEvent
    public static void hideHands(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        Entry entry = minecraft.f_91074_ == null || ENTRIES.isEmpty() ? null : ENTRIES.get(minecraft.f_91074_.m_19879_());
        if (entry != null && PhoenixForm.humanHidden(entry.phase, ticks(entry, event.getPartialTick()))) {
            event.setCanceled(true);
        }
    }

    /** Scratch (the render thread only): a move's bank. */
    private static final float[] BANK = new float[1];

    /** Scratch (the render thread only). */
    private static final float[] TINT = new float[3];
    private static final float[] PLACE = new float[12];
    private static final PhoenixForm.Motion NOW = new PhoenixForm.Motion();

    /** The phoenixes, after the entities. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void render(RenderLevelStageEvent event) {
        // (3.7.6: the human's temporary wings are drawn on their arms, in the player's own layer: PhoenixClient.renderBody, armWings)
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
                    continue; // the eye is inside the phoenix: only the world is seen
                }
                draw(m, entry, player, stack, out, camera, partial, e.getKey());
            }
            buffers.m_109912_(body);
        } catch (RuntimeException failure) {
            HexPiece.LOGGER.error("HexPiece: a phoenix could not be drawn", failure);
            ENTRIES.clear();
        }
    }

    static void draw(DragonModel m, Entry entry, Player player, PoseStack stack, VertexConsumer out, Vec3 camera, float partial, int id) {
        float t = ticks(entry, partial);
        float grow = PhoenixForm.grow(entry.phase, t);
        if (grow < 0.02F) {
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
            entry.points = new double[shape.anchors.length / 2 * 3];
            entry.glow = new double[GLOW_BONES * 3];
        }
        PhoenixForm.Motion mo = NOW;
        PhoenixForm.Motion a = entry.was;
        PhoenixForm.Motion b = entry.motion;
        mo.speed = a.speed + (b.speed - a.speed) * partial;
        mo.glide = a.glide + (b.glide - a.glide) * partial;
        mo.dive = a.dive + (b.dive - a.dive) * partial;
        mo.perch = a.perch + (b.perch - a.perch) * partial;
        mo.walk = a.walk + (b.walk - a.walk) * partial;
        mo.flare = a.flare + (b.flare - a.flare) * partial;
        mo.land = PhoenixForm.landing(clock + partial - entry.landed);
        mo.stride = entry.strideO + (entry.stride - entry.strideO) * partial;
        float shaped = PhoenixForm.shaped(entry.phase, t);
        mo.form = shaped;
        // 3.7.2: a move's pose (server/PhoenixMoves' acts): swept, ramming, seizing; the whirl turns it, the glide round banks it
        float whirl = PhoenixMoveFx.act(id, clock + partial, mo, BANK);
        float seconds = (clock + partial) / 20.0F + (id % 17) * 0.31F;
        float beat = entry.beatO + (entry.beat - entry.beatO) * partial;
        PhoenixForm.pose(m, shape, entry.pose, mo, beat, seconds);
        double x = player.f_19790_ + (player.m_20185_() - player.f_19790_) * partial;
        double y = player.f_19791_ + (player.m_20186_() - player.f_19791_) * partial;
        double z = player.f_19792_ + (player.m_20189_() - player.f_19792_) * partial;
        float yaw = entry.yawO + wrap(entry.yaw - entry.yawO) * partial + whirl;
        float bank = entry.bankO + (entry.bank - entry.bankO) * partial + BANK[0];
        float lookPitch = player.f_19860_ + (player.m_146909_() - player.f_19860_) * partial;
        // climbing and dropping tilt it with the look, at speed
        float tilt = -lookPitch * 0.45F * mo.speed * (1.0F - mo.perch);
        float s = PhoenixLook.SCALE * grow;
        // in flight its chest held at the player's middle; standing, its claws on the ground; 3.7.0: shaped as the human (the
        // change), its wings' roots where the shoulders are, so the wings open out of the arms
        float anchor = shape.chest + (shape.feetStand - shape.chest) * mo.perch;
        float rise = 0.9F * (1.0F - mo.perch);
        anchor = shape.shoulder + (anchor - shape.shoulder) * shaped;
        rise = SHOULDER + (rise - SHOULDER) * shaped;
        tilt *= shaped;
        bank *= shaped;
        placement(x - camera.f_82479_, y + rise - camera.f_82480_, z - camera.f_82481_, yaw, tilt, bank, s, anchor, PLACE);
        // the view times the place: one 3 x 4 for the points
        Matrix4f view = stack.m_85850_().m_252922_();
        Vector3f o = view.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
        Vector3f ax = view.transformDirection(new Vector3f(1.0F, 0.0F, 0.0F));
        Vector3f ay = view.transformDirection(new Vector3f(0.0F, 1.0F, 0.0F));
        Vector3f az = view.transformDirection(new Vector3f(0.0F, 0.0F, 1.0F));
        float[] v = {ax.x, ay.x, az.x, o.x, ax.y, ay.y, az.y, o.y, ax.z, ay.z, az.z, o.z};
        float[] mat = mul(v, PLACE);
        Matrix3f turn = stack.m_85850_().m_252943_();
        Vector3f ny = turn.transform(new Vector3f(0.0F, 1.0F, 0.0F));
        // 3.7.0 (the owner: "the wings should not get shadows"; "it should also glow"): every normal sent as straight up in the world,
        // so the game's two lights light every side of it fully (no side in shade); the fire's own heat drawn over it
        float upX = ny.x;
        float upY = ny.y;
        float upZ = ny.z;
        for (int i = 0; i < m.parts.length; i++) {
            DragonModel.Part part = m.parts[i];
            PhoenixForm.skin(shape, entry.pose, i, part, entry.pos[i], entry.nrm[i], mo, seconds);
            PhoenixForm.flame(shape, i, part, entry.pos[i], entry.nrm[i], seconds);
            emit(out, shape, i, part, entry.pos[i], entry.nrm[i], mat, upX, upY, upZ, seconds);
        }
        // the glow's points: the chest, the neck, along each wing, the tail's root
        float[] g = entry.pose.global;
        for (int k = 0; k < GLOW_BONES; k++) {
            int bone = glowBone(k);
            float px = bone < 0 ? 0.0F : g[bone * 12 + 3];
            float py = bone < 0 ? 0.0F : g[bone * 12 + 7];
            float pz = bone < 0 ? 0.0F : g[bone * 12 + 11];
            for (int r = 0; r < 3; r++) {
                entry.glow[k * 3 + r] = PLACE[r * 4] * px + PLACE[r * 4 + 1] * py + PLACE[r * 4 + 2] * pz + PLACE[r * 4 + 3];
            }
        }
        entry.drawnScale = s;
        // the fire points, placed (camera-relative) for the tongues drawn after the particles
        int[] an = shape.anchors;
        for (int k = 0; k < an.length / 2; k++) {
            float[] p = entry.pos[an[k * 2]];
            int vtx = an[k * 2 + 1];
            float px = p[vtx * 3];
            float py = p[vtx * 3 + 1];
            float pz = p[vtx * 3 + 2];
            for (int r = 0; r < 3; r++) {
                entry.points[k * 3 + r] = PLACE[r * 4] * px + PLACE[r * 4 + 1] * py + PLACE[r * 4 + 2] * pz + PLACE[r * 4 + 3];
            }
        }
        entry.shown = true;
    }

    /** How high over the ground (blocks) it starts to brake to land when dropping. */
    static final double FLARE_HEIGHT = 3.5;

    /** How far below the player's feet the ground is (blocks; more than 8: none near). */
    static double groundBelow(Player player) {
        Vec3 from = player.m_20182_();
        Vec3 to = from.m_82520_(0.0, -8.0, 0.0);
        BlockHitResult hit = player.m_9236_().m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
        return hit.m_6662_() == HitResult.Type.BLOCK ? from.f_82480_ - hit.m_82450_().f_82480_ : 9.0;
    }

    /** Its glow's size round each point (model units: the blocks it covers grow with it). */
    static final float GLOW_SIZE = 3.2F;
    /** Where its wings' roots are held while it is shaped as the human: the shoulders' height (blocks over the feet). */
    static final float SHOULDER = 1.4F;
    /** The glow's points (bones): the chest, the neck, the tail's root, each wing's elbow, outer joint and hand. */
    static final int GLOW_BONES = 9;

    static int glowBone(int k) {
        return switch (k) {
            case 0 -> shape.body;
            case 1 -> shape.neck;
            case 2 -> shape.tailRoot;
            default -> shape.wing[(k - 3) / 3][1 + (k - 3) % 3];
        };
    }

    /**
     * 3.7.6, every move of the human's (the owner: "the arms of the player SHOULD become the wings not create a layer behind them bro",
     * "and I mean that for every move literally"): the phoenix's own wings (Shape.wingSide), shaped as the human (straight out), one laid
     * along each arm from the shoulder down past the hand (PhoenixForm.armWing), turning with the arm as the move's clip moves it, the
     * arm itself hidden in it (hideHuman). They come and go as the whole change does, never grown or shrunk (PhoenixForm.armFire,
     * armHidden, wingShown): the arms catch fire from the hands up and burn into the wings, which take shape out of the fire from the
     * shoulder to the tip; going, the wings burn away from the tips back, the arms come back burning and the fire dies off them. Drawn in
     * the player's own layer ({@code pose} the player model's frame: PhoenixClient.renderBody).
     */
    static void armWings(PoseStack pose, MultiBufferSource buffers, Player player, PlayerModel<?> body, float partial) {
        int id = player.m_19879_();
        PhoenixMoveFx.Wings w = PhoenixMoveFx.WINGS.isEmpty() ? null : PhoenixMoveFx.WINGS.get(id);
        DragonModel m = w == null || ENTRIES.containsKey(id) ? null : model();
        if (m == null || shape == null) {
            return;
        }
        float t = clock + partial - w.start;
        float fire = PhoenixForm.armFire(t, w.ticks);
        float shown = PhoenixForm.wingShown(t, w.ticks);
        if (fire < 0.01F && shown < 0.01F) {
            return;
        }
        Entry entry = WING_POSES.computeIfAbsent(id, k -> new Entry());
        if (entry.pose == null) {
            entry.pose = new DragonPose(m);
            entry.pos = new float[m.parts.length][];
            entry.nrm = new float[m.parts.length][];
            for (int i = 0; i < m.parts.length; i++) {
                entry.pos[i] = new float[m.parts[i].vertices() * 3];
                entry.nrm[i] = new float[m.parts[i].vertices() * 3];
            }
        }
        PhoenixForm.Motion mo = NOW;
        mo.speed = 0.0F;
        mo.glide = 0.0F;
        mo.dive = 0.0F;
        mo.perch = 0.0F;
        mo.walk = 0.0F;
        mo.flare = 0.0F;
        mo.land = 0.0F;
        mo.form = 0.0F;
        mo.sweep = 0.0F;
        mo.reach = 0.0F;
        mo.ram = 0.0F;
        mo.spread = 0.0F;
        float seconds = (clock + partial) / 20.0F + (id % 13) * 0.27F;
        PhoenixForm.pose(m, shape, entry.pose, mo, seconds * PhoenixForm.beatRate(mo), seconds);
        VertexConsumer out = buffers.m_6299_(RenderType.m_110458_(TEXTURE));
        // every normal the world's up (the game's lights shade no side of it): the model frame's up is its -y
        Vector3f ny = pose.m_85850_().m_252943_().transform(new Vector3f(0.0F, -1.0F, 0.0F));
        // the world's down (the model frame's +y) in the view, to droop the feathers by
        Vector3f down = pose.m_85850_().m_252922_().transformDirection(new Vector3f(0.0F, 1.0F, 0.0F));
        boolean hidden = PhoenixForm.armHidden(t, w.ticks);
        for (int side = 0; side < 2; side++) {
            pose.m_85836_();
            (side == 0 ? body.f_102812_ : body.f_102811_).m_104299_(pose);
            Matrix4f arm = pose.m_85850_().m_252922_();
            Vector3f local = new Matrix4f(arm).invert().transformDirection(new Vector3f(down.x, down.y, down.z));
            float ll = (float) Math.sqrt(local.x * local.x + local.y * local.y + local.z * local.z);
            if (ll > 1.0E-6F) {
                local.x /= ll;
                local.z /= ll;
            }
            // the arm burning (as the whole change's arms): from the hand up while it turns, from the shoulder down to the hand coming back
            float across = side == 0 ? 0.0625F : -0.0625F;
            float wingAt = shown;
            ARM_SINK.matrix = arm;
            ARM_SINK.buffer = buffers.m_6299_(RenderType.m_269166_());
            if (fire > 0.01F && (!hidden || shown < 0.6F)) {
                PhoenixFire.arm(ARM_SINK, across, 0.625, 0.0, across, -0.125, 0.0, fire, clock + partial, id * 2 + side);
            }
            Vector3f o = arm.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
            Vector3f ax = arm.transformDirection(new Vector3f(1.0F, 0.0F, 0.0F));
            Vector3f ay = arm.transformDirection(new Vector3f(0.0F, 1.0F, 0.0F));
            Vector3f az = arm.transformDirection(new Vector3f(0.0F, 0.0F, 1.0F));
            float[] v = {ax.x, ay.x, az.x, o.x, ax.y, ay.y, az.y, o.y, ax.z, ay.z, az.z, o.z};
            PhoenixForm.armWing(entry.pose, shape, side, WINGS_SCALE, local.x, local.z, ARM);
            float[] mat = mul(v, ARM);
            // the wing taken shape so far, from the shoulder (its root) out along its span; its burning edge where it ends
            int root = shape.wing[side][0];
            int tip = shape.wing[side][4];
            float rootX = root < 0 ? 0.0F : entry.pose.global[root * 12 + 3];
            float rootY = root < 0 ? shape.shoulder : entry.pose.global[root * 12 + 7];
            float rootZ = root < 0 ? 0.0F : entry.pose.global[root * 12 + 11];
            float span = side == 0 ? -1.0F : 1.0F;
            float length = Math.max(1.0F, Math.abs((tip < 0 ? rootX + span * 8.0F : entry.pose.global[tip * 12 + 3]) - rootX) * 1.25F);
            if (wingAt > 0.01F) {
                for (int i = 0; i < m.parts.length; i++) {
                    if (shape.wingSide[i] != side) {
                        continue;
                    }
                    DragonModel.Part part = m.parts[i];
                    PhoenixForm.skin(shape, entry.pose, i, part, entry.pos[i], entry.nrm[i], mo, seconds);
                    PhoenixForm.flame(shape, i, part, entry.pos[i], entry.nrm[i], seconds);
                    emit(out, shape, i, part, entry.pos[i], entry.nrm[i], mat, ny.x, ny.y, ny.z, seconds, rootX, span, length, wingAt);
                }
            }
            if (wingAt > 0.01F && wingAt < 0.99F) {
                for (int k = 0; k < 2; k++) {
                    float mx = rootX + span * wingAt * length * 0.85F;
                    float mz = rootZ + 0.6F + 1.6F * k;
                    float lx = ARM[0] * mx + ARM[1] * rootY + ARM[2] * mz + ARM[3];
                    float ly = ARM[4] * mx + ARM[5] * rootY + ARM[6] * mz + ARM[7];
                    float lz = ARM[8] * mx + ARM[9] * rootY + ARM[10] * mz + ARM[11];
                    PhoenixFire.front(ARM_SINK, lx, ly, lz, 0.2 - 0.05 * k, clock + partial, id * 4 + side * 2 + k);
                }
            }
            pose.m_85849_();
        }
    }

    private static final float[] ARM = new float[12];
    /** The arms' fire, in an arm's own frame (armWings). */
    private static final Writer ARM_SINK = new Writer();

    /** Each human's temporary wings' pose (by entity id) and skinned arrays. */
    static final Map<Integer, Entry> WING_POSES = new HashMap<>();
    /** The temporary wings' size (blocks a model unit: each about 1.6 blocks long, from the shoulder past the hand). */
    static final float WINGS_SCALE = 0.18F;

    /**
     * The model's place (3 x 4 row-major, blocks, camera-relative): its point {@code anchor} high (model units) at (x, y, z), facing the
     * game's {@code yaw} (the model faces -Z: turned by 180 - yaw), tilted nose up by {@code tilt} degrees, banked by {@code bank},
     * scaled by {@code s}.
     */
    static void placement(double x, double y, double z, float yaw, float tilt, float bank, float s, float anchor, float[] out) {
        double ty = Math.toRadians(180.0 - yaw);
        double tx = Math.toRadians(tilt);
        double tz = Math.toRadians(bank);
        double cy = Math.cos(ty);
        double sy = Math.sin(ty);
        double cx = Math.cos(tx);
        double sx = Math.sin(tx);
        double cz = Math.cos(tz);
        double sz = Math.sin(tz);
        // R = Ry * Rx * Rz
        double[] ry = {cy, 0, sy, 0, 1, 0, -sy, 0, cy};
        double[] rx = {1, 0, 0, 0, cx, -sx, 0, sx, cx};
        double[] rz = {cz, -sz, 0, sz, cz, 0, 0, 0, 1};
        double[] r = mul3(ry, mul3(rx, rz));
        for (int i = 0; i < 3; i++) {
            out[i * 4] = (float) (r[i * 3] * s);
            out[i * 4 + 1] = (float) (r[i * 3 + 1] * s);
            out[i * 4 + 2] = (float) (r[i * 3 + 2] * s);
        }
        // the anchor point (0, anchor, 0) lands on (x, y, z)
        out[3] = (float) (x - r[1] * s * anchor);
        out[7] = (float) (y - r[4] * s * anchor);
        out[11] = (float) (z - r[7] * s * anchor);
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

    /**
     * A part's corners: the fire parts all at the ramp's lit end (PhoenixLook.glowing: no shadow on them), the rest by the file's cel
     * ramp of its model-space normal; full bright (it is fire), every normal the world's up (the game's lights shade no side of it);
     * 3.7.0 the fire's heat flowing over its skin (PhoenixForm.heat) drawn through the game's own overlay, its white rows (as a
     * creeper's flash): the brighter bands running out along it to the tips.
     */
    static void emit(VertexConsumer out, PhoenixForm.Shape sh, int index, DragonModel.Part part, float[] pos, float[] nrm, float[] m, float upX,
                     float upY, float upZ, float seconds) {
        emit(out, sh, index, part, pos, nrm, m, upX, upY, upZ, seconds, 0.0F, 0.0F, 1.0F, 2.0F);
    }

    /**
     * As above, 3.7.6 only the quads within {@code shown} of the way out along the span (the model's x from {@code rootX} by {@code span},
     * over {@code length}): a wing taking shape out of the fire, or burning away.
     */
    static void emit(VertexConsumer out, PhoenixForm.Shape sh, int index, DragonModel.Part part, float[] pos, float[] nrm, float[] m, float upX,
                     float upY, float upZ, float seconds, float rootX, float span, float length, float shown) {
        int[] q = part.quads;
        float[] uv = part.uv;
        float[] light = PhoenixLook.LIGHT;
        boolean fire = sh.fire[index];
        float[] reach = sh.reach[index];
        if (fire) {
            PhoenixLook.glowing(part.kind, TINT);
        }
        for (int i = 0; i < q.length; i++) {
            int k = q[i];
            if (shown < 1.0F && i % 4 == 0) {
                float far = -Float.MAX_VALUE;
                for (int j = 0; j < 4; j++) {
                    far = Math.max(far, (pos[q[i + j] * 3] - rootX) * span / length);
                }
                if (far > shown) {
                    i += 3;
                    continue;
                }
            }
            float x = pos[k * 3];
            float y = pos[k * 3 + 1];
            float z = pos[k * 3 + 2];
            int overlay = OverlayTexture.f_118083_;
            if (fire) {
                float heat = PhoenixForm.heat(index, reach[k], x, z, seconds);
                overlay = Math.round(15.0F * Math.min(1.0F, heat * PhoenixLook.HEAT / PhoenixLook.WHITEST)) | 10 << 16;
            } else {
                PhoenixLook.tint(part.kind, nrm[k * 3], nrm[k * 3 + 1], nrm[k * 3 + 2], light, TINT);
            }
            out.m_5954_(m[0] * x + m[1] * y + m[2] * z + m[3], m[4] * x + m[5] * y + m[6] * z + m[7], m[8] * x + m[9] * y + m[10] * z + m[11],
                TINT[0], TINT[1], TINT[2], 1.0F, uv[k * 2], uv[k * 2 + 1], overlay, 0xF000F0, upX, upY, upZ);
        }
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

    /** The tongues of flame streaming off every phoenix, and its forming and going, after the particles. */
    @SubscribeEvent
    public static void fire(RenderLevelStageEvent event) {
        // 3.7.5: the moves' fire is drawn whoever is the phoenix or not (this returned whenever nobody was: no human move showed anything)
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ENTRIES.isEmpty() && PhoenixMoveFx.FX.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        RenderType type = RenderType.m_269166_();
        SINK.matrix = pose.m_85850_().m_252922_();
        float partial = event.getPartialTick();
        Vec3 cam = event.getCamera().m_90583_();
        boolean firstPerson = minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON;
        try {
            SINK.buffer = buffers.m_6299_(type);
            // 3.7.2: the moves' fire (client/phoenix/PhoenixMoveFx)
            PhoenixMoveFx.draw(SINK, level, cam, partial);
            for (Map.Entry<Integer, Entry> e : ENTRIES.entrySet()) {
                Entry entry = e.getValue();
                if (!(level.m_6815_(e.getKey()) instanceof Player player)) {
                    continue;
                }
                float t = ticks(entry, partial);
                float grow = Math.max(0.0F, PhoenixForm.grow(entry.phase, t));
                if (entry.shown && grow > 0.05F && shape != null) {
                    // the wind of its own flight streams them back
                    PhoenixFire.burn(SINK, entry.points, shape.anchorSize, entry.points.length / 3, clock + partial, -entry.vx * 0.5, -entry.vy * 0.3,
                        -entry.vz * 0.5, Math.min(1.0, grow), e.getKey());
                    PhoenixFire.glow(SINK, entry.glow, GLOW_BONES, GLOW_SIZE * entry.drawnScale, clock + partial);
                }
                double fx = player.f_19790_ + (player.m_20185_() - player.f_19790_) * partial - cam.f_82479_;
                double fy = player.f_19791_ + (player.m_20186_() - player.f_19791_) * partial - cam.f_82480_;
                double fz = player.f_19792_ + (player.m_20189_() - player.f_19792_) * partial - cam.f_82481_;
                // 3.7.1: the touchdown's fire spreading over the ground round its claws
                if (entry.phase == PhoenixForm.PHOENIX) {
                    PhoenixFire.touchdown(SINK, fx, fy, fz, clock + partial - entry.landed, e.getKey());
                }
                if (player == minecraft.f_91074_ && firstPerson) {
                    continue; // the eye would be inside the figure of fire
                }
                // the human turned to fire, the phoenix opening out of it (forming) and closing back into it (going)
                float bodyYaw = player.f_20884_ + wrap(player.f_20883_ - player.f_20884_) * partial;
                PhoenixFire.change(SINK, fx, fy, fz, bodyYaw, entry.phase, t, e.getKey());
            }
        } catch (RuntimeException failure) {
            HexPiece.LOGGER.error("HexPiece: a phoenix's fire could not be drawn", failure);
        }
        if (SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        SINK.buffer = null;
    }

    /**
     * client/dragon/DragonCamera asks: one's own camera, in third person (back) while the phoenix is out, pulled back and up so it is
     * seen whole (the vanilla 4 blocks would be inside its wings), easing out as it forms and in as it goes; null otherwise.
     */
    public static Vec3 camera(Entity entity, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (ENTRIES.isEmpty() || entity != minecraft.f_91074_ || minecraft.f_91066_.m_92176_() != CameraType.THIRD_PERSON_BACK) {
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
        Vec3 eye = entity.m_20299_(partial);
        Vec3 look = entity.m_20252_(partial);
        // 3.7.0: the phoenix bigger (PhoenixLook.SCALE 0.55), the camera further back
        double back = 4.0 + 7.5 * out;
        double up = 2.2 * out;
        Vec3 raised = com.hexhaki.piece.client.dragon.DragonCamera.clip(entity, eye, eye.m_82520_(0.0, up, 0.0));
        return com.hexhaki.piece.client.dragon.DragonCamera.clip(entity, raised, raised.m_82520_(-look.f_82479_ * back, -look.f_82480_ * back,
            -look.f_82481_ * back));
    }

    /** Whether one's own player is the phoenix (the panel shows its moves). */
    static boolean selfOut() {
        Minecraft minecraft = Minecraft.m_91087_();
        return minecraft.f_91074_ != null && ENTRIES.containsKey(minecraft.f_91074_.m_19879_());
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ENTRIES.clear();
        PhoenixMoveFx.clear();
        WING_POSES.clear();
        fromFirstPerson = false;
    }
}
