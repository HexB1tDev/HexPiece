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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
 * hovering, flying, gliding, diving, standing on its claws); it bursts out of a sphere of blue fire as it forms and folds back into a
 * burst of flame as it goes. The human is hidden while it is out; one's own camera pulls back to see it whole.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class PhoenixFx {
    static final ResourceLocation TEXTURE = ResourceLocation.m_214293_("hexhaki", "textures/piece/phoenix/marco.png");
    private static long clock;
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
        DragonPose pose;
        float[][] pos;
        float[][] nrm;
        /** The fire points this frame (world, camera-relative), for the tongues. */
        double[] points;
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
        was.wrap = mo.wrap;
        double flat = Math.sqrt(entry.vx * entry.vx + entry.vz * entry.vz);
        float speed = (float) Math.min(1.0, flat / 0.9);
        float dive = (float) Math.max(0.0, Math.min(1.0, (-entry.vy - 0.35) / 0.45));
        float glide = speed > 0.7F && entry.vy < 0.05 && entry.vy > -0.35 ? 1.0F : 0.0F;
        float perch = player.m_20096_() && flat < 0.25 ? 1.0F : 0.0F;
        mo.speed += (speed - mo.speed) * 0.15F;
        mo.dive += (dive - mo.dive) * 0.12F;
        // a glide sets in slowly (a few beats at speed first) and breaks off quickly
        mo.glide += (glide - mo.glide) * (glide > mo.glide ? 0.025F : 0.12F);
        mo.perch += (perch - mo.perch) * 0.2F;
        mo.wrap = PhoenixForm.wrap(entry.phase, clock - entry.start);
        entry.beatO = entry.beat;
        entry.beat += PhoenixForm.beatRate(mo) / 20.0F;
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
        if (ENTRIES.isEmpty()) {
            return;
        }
        Entry entry = ENTRIES.get(event.getEntity().m_19879_());
        if (entry != null && PhoenixForm.humanHidden(entry.phase, ticks(entry, event.getPartialTick()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void hideHands(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        Entry entry = minecraft.f_91074_ == null || ENTRIES.isEmpty() ? null : ENTRIES.get(minecraft.f_91074_.m_19879_());
        if (entry != null && PhoenixForm.humanHidden(entry.phase, ticks(entry, event.getPartialTick()))) {
            event.setCanceled(true);
        }
    }

    /** Scratch (the render thread only). */
    private static final float[] TINT = new float[3];
    private static final float[] PLACE = new float[12];
    private static final PhoenixForm.Motion NOW = new PhoenixForm.Motion();

    /** The phoenixes, after the entities. */
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
                if (player == minecraft.f_91074_ && firstPerson) {
                    entry.points = null;
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
            entry.points = null;
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
        }
        PhoenixForm.Motion mo = NOW;
        PhoenixForm.Motion a = entry.was;
        PhoenixForm.Motion b = entry.motion;
        mo.speed = a.speed + (b.speed - a.speed) * partial;
        mo.glide = a.glide + (b.glide - a.glide) * partial;
        mo.dive = a.dive + (b.dive - a.dive) * partial;
        mo.perch = a.perch + (b.perch - a.perch) * partial;
        mo.wrap = PhoenixForm.wrap(entry.phase, t);
        float seconds = (clock + partial) / 20.0F + (id % 17) * 0.31F;
        float beat = entry.beatO + (entry.beat - entry.beatO) * partial;
        // forming, it bursts out at the top of a stroke and beats down hard
        if (entry.phase == PhoenixForm.FORMING && t < 20.0F) {
            beat = (float) (-Math.PI * 0.5 + t / 20.0F * Math.PI);
        }
        PhoenixForm.pose(m, shape, entry.pose, mo, beat, seconds);
        double x = player.f_19790_ + (player.m_20185_() - player.f_19790_) * partial;
        double y = player.f_19791_ + (player.m_20186_() - player.f_19791_) * partial;
        double z = player.f_19792_ + (player.m_20189_() - player.f_19792_) * partial;
        float yaw = entry.yawO + wrap(entry.yaw - entry.yawO) * partial;
        float bank = entry.bankO + (entry.bank - entry.bankO) * partial;
        float lookPitch = player.f_19860_ + (player.m_146909_() - player.f_19860_) * partial;
        // climbing and dropping tilt it with the look, at speed
        float tilt = -lookPitch * 0.45F * mo.speed * (1.0F - mo.perch);
        float s = PhoenixLook.SCALE * grow;
        // in flight its chest held at the player's middle; standing, its claws on the ground
        float anchor = shape.chest + (shape.feet - shape.chest) * mo.perch;
        float rise = 0.9F * (1.0F - mo.perch);
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
        Vector3f nx = turn.transform(new Vector3f(1.0F, 0.0F, 0.0F));
        Vector3f ny = turn.transform(new Vector3f(0.0F, 1.0F, 0.0F));
        Vector3f nz = turn.transform(new Vector3f(0.0F, 0.0F, 1.0F));
        float[] nm = {nx.x, ny.x, nz.x, nx.y, ny.y, nz.y, nx.z, ny.z, nz.z};
        int overlay = OverlayTexture.f_118083_;
        for (int i = 0; i < m.parts.length; i++) {
            DragonModel.Part part = m.parts[i];
            entry.pose.skin(part, entry.pos[i], entry.nrm[i]);
            PhoenixForm.flame(shape, i, part, entry.pos[i], entry.nrm[i], seconds);
            emit(out, part, entry.pos[i], entry.nrm[i], mat, nm, overlay);
        }
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
    }

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

    /** A part's corners, each coloured by the file's cel ramp of its model-space normal, full bright (it is fire). */
    static void emit(VertexConsumer out, DragonModel.Part part, float[] pos, float[] nrm, float[] m, float[] n, int overlay) {
        int[] q = part.quads;
        float[] uv = part.uv;
        float[] light = PhoenixLook.LIGHT;
        for (int i = 0; i < q.length; i++) {
            int k = q[i];
            float x = pos[k * 3];
            float y = pos[k * 3 + 1];
            float z = pos[k * 3 + 2];
            float nx = nrm[k * 3];
            float ny = nrm[k * 3 + 1];
            float nz = nrm[k * 3 + 2];
            PhoenixLook.tint(part.kind, nx, ny, nz, light, TINT);
            float wx = n[0] * nx + n[1] * ny + n[2] * nz;
            float wy = n[3] * nx + n[4] * ny + n[5] * nz;
            float wz = n[6] * nx + n[7] * ny + n[8] * nz;
            out.m_5954_(m[0] * x + m[1] * y + m[2] * z + m[3], m[4] * x + m[5] * y + m[6] * z + m[7], m[8] * x + m[9] * y + m[10] * z + m[11],
                TINT[0], TINT[1], TINT[2], 1.0F, uv[k * 2], uv[k * 2 + 1], overlay, 0xF000F0, wx, wy, wz);
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
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ENTRIES.isEmpty() || shape == null) {
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
        try {
            SINK.buffer = buffers.m_6299_(type);
            for (Map.Entry<Integer, Entry> e : ENTRIES.entrySet()) {
                Entry entry = e.getValue();
                if (!(level.m_6815_(e.getKey()) instanceof Player player)) {
                    continue;
                }
                float t = ticks(entry, partial);
                float grow = Math.max(0.0F, PhoenixForm.grow(entry.phase, t));
                if (entry.points != null && grow > 0.05F) {
                    // the wind of its own flight streams them back
                    PhoenixFire.burn(SINK, entry.points, shape.anchorSize, entry.points.length / 3, clock + partial, -entry.vx * 0.5, -entry.vy * 0.3,
                        -entry.vz * 0.5, Math.min(1.0, grow), e.getKey());
                }
                double x = player.f_19790_ + (player.m_20185_() - player.f_19790_) * partial - cam.f_82479_;
                double y = player.f_19791_ + (player.m_20186_() - player.f_19791_) * partial - cam.f_82480_ + 1.0;
                double z = player.f_19792_ + (player.m_20189_() - player.f_19792_) * partial - cam.f_82481_;
                PhoenixFire.change(SINK, x, y, z, entry.phase == PhoenixForm.FORMING, t, e.getKey());
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
        double back = 4.0 + 5.5 * out;
        double up = 1.6 * out;
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
        fromFirstPerson = false;
    }
}
