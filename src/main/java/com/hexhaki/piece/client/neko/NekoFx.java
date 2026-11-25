package com.hexhaki.piece.client.neko;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.piece.HexPiece;
import com.hexhaki.piece.dragon.DragonModel;
import com.hexhaki.piece.dragon.DragonPose;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.neko.LeopardArt;
import com.hexhaki.piece.neko.LeopardForm;
import com.hexhaki.piece.network.S2CUoFx;
import com.hexhaki.piece.server.NekoMoves;
import com.hexhaki.piece.server.NekoServer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
 * 3.7.9 (the owner: "Neko Neko No Mi ... Model in the Zip, Size: 5 blocks tall. Animate it properly, realistic fluid animations, the mane
 * and body hair IS black flames, the black ribbon around the leopard is black flames as well, We need Idle, running, jumping, transforming
 * and de-transforming animations, facial animations too (simple)"): the owner's leopard drawn over a player in its form (server/NekoServer)
 * as the phoenix is (client/phoenix/PhoenixFx: the model skinned on the CPU each frame, written into the game's own entity buffers, drawn
 * both sides), 5 blocks tall (neko/LeopardForm.SCALE), turned with the body: its own takes for its idle, run (its pace from the ground
 * covered), jump, landing and Six Styles' blows (LeopardForm.pose); the face shown by what it does (snarling as it strikes, wincing when
 * hurt, roaring as it changes); the hands closed into fists as it strikes. Its mane, body hair and ribbon are black flames: drawn after
 * the particles, alpha-blended, licking and flowing (LeopardForm.flame, flameColour). The change burns, never scales (the owner's rule):
 * the human is taken by black flames from the feet up as they slouch, the leopard burns into being from its feet up to its head, the
 * roar's black dome bursts out (neko/LeopardArt); going back it burns away from the head down and the human stands up out of the flames.
 * The human is hidden while it is out; one's own camera pulls back to see it whole.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class NekoFx {
    static final ResourceLocation TEXTURE = ResourceLocation.m_214293_("hexhaki", "textures/piece/neko/lucci.png");
    static long clock;
    private static boolean kitAdded;
    private static boolean fromFirstPerson;
    private static DragonModel model;
    private static LeopardForm.Shape shape;
    private static boolean failed;

    /** One player's leopard on this client. */
    static final class Entry {
        int phase;
        long start;
        final LeopardForm.Motion motion = new LeopardForm.Motion();
        float speed;
        float speedO;
        float stride;
        float strideO;
        float air;
        long leftGround = -100;
        long landed = -100;
        boolean grounded = true;
        double x;
        double y;
        double z;
        boolean placed;
        /** Its health last tick, when it last lost some (the hurt face). */
        float health = -1.0F;
        long hurtAt = -100;
        /** Its last blow (1 to 5, 0 none) and when it was thrown. */
        int blow;
        long blowAt = -100;
        DragonPose pose;
        float[][] pos;
        float[][] nrm;
        /** This frame: drawn, the model to camera-relative place (3 x 4), how far up it is there (model units). */
        boolean shown;
        final float[] place = new float[12];
        float line;
    }

    /** A blow's effect: who, which, as the leopard, where its strike is and where it lands, when. */
    static final class Blow {
        int user;
        int blow;
        boolean leopard;
        boolean landed;
        boolean hit;
        long thrown;
        long land;
        double x;
        double y;
        double z;
        double lx;
        double ly;
        double lz;
        double hx;
        double hy;
        double hz;
    }

    static final Map<Integer, Entry> ENTRIES = new HashMap<>();
    static final List<Blow> BLOWS = new ArrayList<>();
    /** The roars under way: who, where its feet were, when. */
    static final List<double[]> ROARS = new ArrayList<>();

    private NekoFx() {
    }

    static DragonModel model() {
        if (model == null && !failed) {
            try (InputStream in = NekoFx.class.getResourceAsStream("/assets/hexhaki/neko/lucci.sry")) {
                if (in == null) {
                    throw new java.io.IOException("assets/hexhaki/neko/lucci.sry is missing");
                }
                model = DragonModel.read(in);
                shape = new LeopardForm.Shape(model);
            } catch (java.io.IOException | RuntimeException e) {
                failed = true;
                HexPiece.LOGGER.error("HexPiece: the leopard's model could not be read", e);
            }
        }
        return model;
    }

    /** client/dragon/UoFx hands on the Leopard fruit's messages (420 to 439). */
    public static void accept(S2CUoFx m) {
        Minecraft minecraft = Minecraft.m_91087_();
        if (m.kind() == NekoMoves.M1 || m.kind() == NekoMoves.M1_LAND) {
            blow(m);
            return;
        }
        if (m.kind() != NekoServer.PHASE) {
            return;
        }
        boolean self = minecraft.f_91074_ != null && minecraft.f_91074_.m_19879_() == m.user();
        int phase = Math.round(m.bx());
        Entry entry = ENTRIES.get(m.user());
        if (phase == LeopardForm.HUMAN) {
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
        if (phase == LeopardForm.FORMING && m.ticks() == 0 && self && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON) {
            minecraft.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);
            fromFirstPerson = true;
        }
    }

    static void back(Minecraft minecraft) {
        if (fromFirstPerson && minecraft.f_91066_.m_92176_() == CameraType.THIRD_PERSON_BACK) {
            minecraft.f_91066_.m_92157_(CameraType.FIRST_PERSON);
        }
        fromFirstPerson = false;
    }

    /** Six Styles' messages: the blow thrown (its strike), then where it lands. */
    static void blow(S2CUoFx m) {
        int blow = Math.round(m.bx());
        boolean leopard = Math.round(m.by()) == 1;
        if (m.kind() == NekoMoves.M1) {
            Blow b = new Blow();
            b.user = m.user();
            b.blow = blow;
            b.leopard = leopard;
            b.thrown = clock;
            b.land = clock + (blow == NekoMoves.BLOWS ? NekoMoves.M1_HIT + 2 : NekoMoves.M1_HIT);
            b.x = m.x();
            b.y = m.y();
            b.z = m.z();
            b.lx = m.ax();
            b.ly = m.ay();
            b.lz = m.az();
            BLOWS.add(b);
            Entry entry = ENTRIES.get(m.user());
            if (entry != null) {
                entry.blow = blow;
                entry.blowAt = clock;
            }
        } else {
            for (int i = BLOWS.size() - 1; i >= 0; i--) {
                Blow b = BLOWS.get(i);
                if (b.user == m.user() && !b.landed) {
                    b.landed = true;
                    b.hit = m.size() > 0.5F;
                    b.land = clock;
                    b.hx = m.x();
                    b.hy = m.y();
                    b.hz = m.z();
                    break;
                }
            }
        }
    }

    static float ticks(Entry entry, float partial) {
        return clock - entry.start + partial;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clock++;
        if (!kitAdded) {
            kitAdded = true;
            ClientKits.KITS.put(Fruit.NEKO, new NekoClient());
        }
        BLOWS.removeIf(b -> clock - b.thrown > 40);
        ROARS.removeIf(r -> clock - r[3] > LeopardArt.ROAR_LIFE + 1);
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
            Entry entry = e.getValue();
            follow(entry, player);
            if (entry.phase == LeopardForm.FORMING && clock - entry.start == LeopardForm.ROAR) {
                ROARS.add(new double[] {player.m_20185_(), player.m_20186_(), player.m_20189_(), clock});
            }
        }
    }

    /** How many blocks the run's take covers in one of its cycles at the leopard's size (the feet never slide). */
    static final float STRIDE = 5.5F;

    /** Its gait from how the player moves this tick: the speed, the run's phase, off the ground, the take-off and the landing. */
    static void follow(Entry entry, Player player) {
        double px = player.m_20185_();
        double py = player.m_20186_();
        double pz = player.m_20189_();
        double flat = 0.0;
        if (entry.placed) {
            double dx = px - entry.x;
            double dz = pz - entry.z;
            flat = Math.sqrt(dx * dx + dz * dz);
        }
        entry.x = px;
        entry.y = py;
        entry.z = pz;
        entry.placed = true;
        boolean ground = player.m_20096_();
        entry.speedO = entry.speed;
        float want = ground ? (float) Math.min(1.0, flat / 0.3) : entry.speed;
        entry.speed += (want - entry.speed) * 0.3F;
        entry.strideO = entry.stride;
        entry.stride += (float) (flat / STRIDE);
        if (entry.stride > 1000.0F) {
            entry.stride -= 1000.0F;
            entry.strideO -= 1000.0F;
        }
        if (!ground && entry.grounded) {
            entry.leftGround = clock;
        }
        if (ground && !entry.grounded) {
            entry.landed = clock;
        }
        entry.grounded = ground;
        float health = player.m_21223_();
        if (entry.health >= 0.0F && health < entry.health - 0.01F) {
            entry.hurtAt = clock;
        }
        entry.health = health;
        entry.air += ((ground ? 0.0F : 1.0F) - entry.air) * (ground ? 0.5F : 0.35F);
    }

    // ------------------------------------------------------------------------------------------------------ the human hidden

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void hideHuman(RenderPlayerEvent.Pre event) {
        if (ENTRIES.isEmpty()) {
            return;
        }
        Entry entry = ENTRIES.get(event.getEntity().m_19879_());
        if (entry != null && LeopardForm.humanHidden(entry.phase, ticks(entry, event.getPartialTick()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void hideHands(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        Entry entry = minecraft.f_91074_ == null || ENTRIES.isEmpty() ? null : ENTRIES.get(minecraft.f_91074_.m_19879_());
        if (entry != null && LeopardForm.humanHidden(entry.phase, ticks(entry, event.getPartialTick()))) {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------------------------------------------ the leopard drawn

    private static final LeopardForm.Motion NOW = new LeopardForm.Motion();
    private static final float[] TINT = new float[4];

    /** The leopards, after the entities. */
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
                entry.shown = false;
                if (!(level.m_6815_(e.getKey()) instanceof Player player)) {
                    continue;
                }
                if (player == minecraft.f_91074_ && firstPerson) {
                    continue; // the eye is inside the leopard: only the world is seen
                }
                draw(m, entry, player, stack, out, camera, partial);
            }
            buffers.m_109912_(body);
        } catch (RuntimeException failure) {
            HexPiece.LOGGER.error("HexPiece: a leopard could not be drawn", failure);
            ENTRIES.clear();
        }
    }

    /** The motion this frame (eased between ticks), the face and hands by what it does. */
    static LeopardForm.Motion motion(Entry entry, Player player, float partial) {
        LeopardForm.Motion mo = NOW;
        float now = clock + partial;
        mo.speed = entry.speedO + (entry.speed - entry.speedO) * partial;
        mo.stride = (entry.strideO + (entry.stride - entry.strideO) * partial) % 1.0F;
        mo.air = entry.air;
        mo.up = now - entry.leftGround;
        mo.down = entry.grounded ? now - entry.landed : 100.0F;
        float t = ticks(entry, partial);
        mo.roar = entry.phase == LeopardForm.FORMING ? Math.max(0.0F, 1.0F - Math.abs(t - LeopardForm.ROAR - 4.0F) / 8.0F) : 0.0F;
        float blowAge = now - entry.blowAt;
        boolean striking = entry.blow > 0 && blowAge < LeopardForm.BLOW_TICKS[entry.blow - 1];
        mo.blow = striking ? entry.blow : 0;
        mo.blowAge = blowAge;
        mo.face = mo.roar > 0.3F ? LeopardForm.FACE_ROAR : clock - entry.hurtAt < 8 ? LeopardForm.FACE_DAMAGE
            : striking ? LeopardForm.FACE_ATTACK : LeopardForm.FACE_NORMAL;
        int hands = striking ? LeopardForm.HAND_CLOSE : mo.roar > 0.3F ? LeopardForm.HAND_SHUGAN : LeopardForm.HAND_OPEN;
        mo.hands[0] = hands;
        mo.hands[1] = hands;
        return mo;
    }

    static void draw(DragonModel m, Entry entry, Player player, PoseStack stack, VertexConsumer out, Vec3 camera, float partial) {
        float t = ticks(entry, partial);
        float there = LeopardForm.revealed(entry.phase, t);
        if (there < 0.01F) {
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
        LeopardForm.Motion mo = motion(entry, player, partial);
        float seconds = (clock + partial) / 20.0F + (player.m_19879_() % 17) * 0.31F;
        LeopardForm.pose(m, shape, entry.pose, mo, seconds);
        double x = player.f_19790_ + (player.m_20185_() - player.f_19790_) * partial - camera.f_82479_;
        double y = player.f_19791_ + (player.m_20186_() - player.f_19791_) * partial - camera.f_82480_;
        double z = player.f_19792_ + (player.m_20189_() - player.f_19792_) * partial - camera.f_82481_;
        float yaw = player.f_20884_ + wrap(player.f_20883_ - player.f_20884_) * partial;
        place(x, y, z, yaw, LeopardForm.SCALE, entry.place);
        Matrix4f view = stack.m_85850_().m_252922_();
        Vector3f o = view.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
        Vector3f ax = view.transformDirection(new Vector3f(1.0F, 0.0F, 0.0F));
        Vector3f ay = view.transformDirection(new Vector3f(0.0F, 1.0F, 0.0F));
        Vector3f az = view.transformDirection(new Vector3f(0.0F, 0.0F, 1.0F));
        float[] v = {ax.x, ay.x, az.x, o.x, ax.y, ay.y, az.y, o.y, ax.z, ay.z, az.z, o.z};
        float[] mat = mul(v, entry.place);
        Matrix3f turn = stack.m_85850_().m_252943_();
        Vector3f up = turn.transform(new Vector3f(0.0F, 1.0F, 0.0F));
        entry.line = there >= 1.0F ? Float.MAX_VALUE : shape.height * there;
        for (int i = 0; i < m.parts.length; i++) {
            if (!LeopardForm.shown(shape, i, mo)) {
                continue;
            }
            DragonModel.Part part = m.parts[i];
            entry.pose.skin(part, entry.pos[i], entry.nrm[i]);
            if (shape.kind[i] >= LeopardForm.HAIR) {
                LeopardForm.flame(shape, i, part, entry.pos[i], entry.nrm[i], seconds);
                continue; // the black flames: drawn after the particles (fire)
            }
            emit(out, i, part, entry.pos[i], entry.nrm[i], mat, up, entry.line, seconds);
        }
        entry.shown = true;
    }

    /** A skin part into the entity buffer, lit by LeopardForm.tint (every normal sent as up: the shading is its own), below {@code line}. */
    static void emit(VertexConsumer out, int index, DragonModel.Part part, float[] pos, float[] nrm, float[] m, Vector3f up, float line,
                     float seconds) {
        int[] q = part.quads;
        float[] uv = part.uv;
        for (int i = 0; i < q.length; i += 4) {
            if (line < Float.MAX_VALUE) {
                float hi = Math.max(Math.max(pos[q[i] * 3 + 1], pos[q[i + 1] * 3 + 1]), Math.max(pos[q[i + 2] * 3 + 1], pos[q[i + 3] * 3 + 1]));
                if (hi > line) {
                    continue;
                }
            }
            for (int j = 0; j < 4; j++) {
                int k = q[i + j];
                float x = pos[k * 3];
                float y = pos[k * 3 + 1];
                float z = pos[k * 3 + 2];
                LeopardForm.tint(shape, index, part, k, nrm[k * 3], nrm[k * 3 + 1], nrm[k * 3 + 2], y, seconds, TINT);
                out.m_5954_(m[0] * x + m[1] * y + m[2] * z + m[3], m[4] * x + m[5] * y + m[6] * z + m[7], m[8] * x + m[9] * y + m[10] * z + m[11],
                    TINT[0], TINT[1], TINT[2], 1.0F, uv[k * 2], uv[k * 2 + 1], OverlayTexture.f_118083_, 0xF000F0, up.x, up.y, up.z);
            }
        }
    }

    /** The model's place: turned to the body's yaw (its face toward -Z to the player's front), {@code s} blocks a unit, its feet at (x, y, z). */
    static void place(double x, double y, double z, float yaw, float s, float[] out) {
        double ty = Math.toRadians(180.0 - yaw);
        double c = Math.cos(ty);
        double sn = Math.sin(ty);
        out[0] = (float) (c * s);
        out[1] = 0.0F;
        out[2] = (float) (sn * s);
        out[3] = (float) x;
        out[4] = 0.0F;
        out[5] = s;
        out[6] = 0.0F;
        out[7] = (float) y;
        out[8] = (float) (-sn * s);
        out[9] = 0.0F;
        out[10] = (float) (c * s);
        out[11] = (float) z;
    }

    static float[] mul(float[] a, float[] b) {
        float[] o = new float[12];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 4; c++) {
                float v = a[r * 4] * b[c] + a[r * 4 + 1] * b[4 + c] + a[r * 4 + 2] * b[8 + c];
                if (c == 3) {
                    v += a[r * 4 + 3];
                }
                o[r * 4 + c] = v;
            }
        }
        return o;
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

    // ------------------------------------------------------------------------------------------------------ the fire

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
    private static final double[] XYZ = new double[12];
    private static final int[] ARGB = new int[4];

    /** The black flames of every leopard, the change's fire, the roars and Six Styles' strikes, after the particles. */
    @SubscribeEvent
    public static void fire(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ENTRIES.isEmpty() && BLOWS.isEmpty() && ROARS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || model() == null) {
            return;
        }
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        RenderType type = RenderType.m_269166_();
        SINK.matrix = event.getPoseStack().m_85850_().m_252922_();
        float partial = event.getPartialTick();
        Vec3 cam = event.getCamera().m_90583_();
        boolean firstPerson = minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON;
        try {
            SINK.buffer = buffers.m_6299_(type);
            for (Map.Entry<Integer, Entry> e : ENTRIES.entrySet()) {
                Entry entry = e.getValue();
                if (!(level.m_6815_(e.getKey()) instanceof Player player)) {
                    continue;
                }
                float t = ticks(entry, partial);
                float seconds = (clock + partial) / 20.0F + (player.m_19879_() % 17) * 0.31F;
                double fx = player.f_19790_ + (player.m_20185_() - player.f_19790_) * partial - cam.f_82479_;
                double fy = player.f_19791_ + (player.m_20186_() - player.f_19791_) * partial - cam.f_82480_;
                double fz = player.f_19792_ + (player.m_20189_() - player.f_19792_) * partial - cam.f_82481_;
                if (entry.shown) {
                    hair(entry, seconds);
                    tongues(entry, player, seconds);
                }
                boolean self = player == minecraft.f_91074_ && firstPerson;
                change(entry, player, fx, fy, fz, t, e.getKey(), self, partial);
            }
            for (double[] r : ROARS) {
                LeopardArt.roar(SINK, r[0] - cam.f_82479_, r[1] - cam.f_82480_, r[2] - cam.f_82481_, clock - r[3] + partial, (int) r[3]);
            }
            for (Blow b : BLOWS) {
                double big = b.leopard ? 1.7 : 1.0;
                double age = clock + partial - b.land;
                if (b.blow == NekoMoves.BLOWS) {
                    if (b.landed) {
                        LeopardArt.rokuogan(SINK, b.hx - cam.f_82479_, b.hy - cam.f_82480_, b.hz - cam.f_82481_, b.lx, b.ly, b.lz, big, age, (int) b.thrown);
                    }
                } else {
                    LeopardArt.strike(SINK, b.blow, b.x - cam.f_82479_, b.y - cam.f_82480_, b.z - cam.f_82481_, b.lx, b.ly, b.lz, big,
                        clock + partial - b.thrown - 1.0, (int) b.thrown);
                    if (b.landed) {
                        LeopardArt.land(SINK, b.hx - cam.f_82479_, b.hy - cam.f_82480_, b.hz - cam.f_82481_, b.lx, b.ly, b.lz, big, b.hit, age,
                            (int) b.thrown);
                    }
                }
            }
        } catch (RuntimeException failure) {
            HexPiece.LOGGER.error("HexPiece: a leopard's fire could not be drawn", failure);
        }
        if (SINK.buffer != null) {
            buffers.m_109912_(type);
        }
        SINK.buffer = null;
    }

    /** Its mane, body hair and ribbon: black flames, each quad as the model placed it this frame, below the burning line. */
    static void hair(Entry entry, float seconds) {
        DragonModel m = model;
        LeopardForm.Motion mo = NOW;
        float[] p = entry.place;
        for (int i = 0; i < m.parts.length; i++) {
            if (shape.kind[i] < LeopardForm.HAIR) {
                continue;
            }
            float[] pos = entry.pos[i];
            int[] q = m.parts[i].quads;
            for (int k = 0; k < q.length; k += 4) {
                float hi = -Float.MAX_VALUE;
                for (int c = 0; c < 4; c++) {
                    int v = q[k + c];
                    float x = pos[v * 3];
                    float y = pos[v * 3 + 1];
                    float z = pos[v * 3 + 2];
                    hi = Math.max(hi, y);
                    XYZ[c * 3] = p[0] * x + p[1] * y + p[2] * z + p[3];
                    XYZ[c * 3 + 1] = p[4] * x + p[5] * y + p[6] * z + p[7];
                    XYZ[c * 3 + 2] = p[8] * x + p[9] * y + p[10] * z + p[11];
                    ARGB[c] = LeopardForm.flameColour(shape, i, v, x, z, seconds);
                }
                if (hi > entry.line) {
                    continue;
                }
                SINK.quad(XYZ, ARGB);
            }
        }
    }

    private static final float[] BALL = new float[3];

    /**
     * The change (the owner: "the transformation should be a ball of burning flames with evil red eyes within"): the black flames rising
     * round the slouching human and gathering into a ball, burning with two evil red eyes in it, bursting at the roar; going back the
     * bigger ball round the leopard, burning away off the human.
     */
    static void change(Entry entry, Player player, double x, double y, double z, float t, int id, boolean self, float partial) {
        if (entry.phase != LeopardForm.FORMING && entry.phase != LeopardForm.UNFORMING || self) {
            return;
        }
        LeopardForm.ball(entry.phase, t, BALL);
        boolean big = BALL[2] > 0.5F;
        double r = big ? LeopardForm.BALL_BIG : LeopardForm.BALL_SMALL;
        double cy = big ? LeopardForm.BALL_BIG_Y : LeopardForm.BALL_SMALL_Y;
        double yaw = Math.toRadians(player.f_20884_ + wrap(player.f_20883_ - player.f_20884_) * partial);
        LeopardArt.ball(SINK, x, y + cy, z, r, -Math.sin(yaw), Math.cos(yaw), BALL[0], BALL[1], t, id);
        if (entry.phase == LeopardForm.FORMING && t < LeopardForm.BALL_IN + 2.0F) {
            // the flames rising round the human as they slouch, before the ball closes
            LeopardArt.human(SINK, x, y, z, 0.0, Math.min(1.0F, t / 3.0F) * (1.0F - BALL[0] * 0.7F), t, id);
        }
    }

    /** Tongues of black flame rising off the mane, the tail's tuft and the ribbon (Shape.anchors), streamed back by the wind of its run. */
    static void tongues(Entry entry, Player player, float seconds) {
        float[] p = entry.place;
        int[] an = shape.anchors;
        double vx = player.m_20185_() - entry.x;
        double vz = player.m_20189_() - entry.z;
        double wx = -Math.max(-0.6, Math.min(0.6, (player.m_20185_() - player.f_19790_) * 1.5));
        double wz = -Math.max(-0.6, Math.min(0.6, (player.m_20189_() - player.f_19792_) * 1.5));
        for (int k = 0; k < an.length / 2; k++) {
            float[] pos = entry.pos[an[k * 2]];
            int v = an[k * 2 + 1];
            float x = pos[v * 3];
            float y = pos[v * 3 + 1];
            float z = pos[v * 3 + 2];
            if (y > entry.line) {
                continue;
            }
            LeopardArt.tongue(SINK, p[0] * x + p[1] * y + p[2] * z + p[3], p[4] * x + p[5] * y + p[6] * z + p[7], p[8] * x + p[9] * y + p[10] * z + p[11],
                shape.anchorSize[k], wx + vx * 0.0, wz + vz * 0.0, seconds * 20.0, k);
        }
    }

    // ------------------------------------------------------------------------------------------------------ the camera

    /**
     * client/dragon/DragonCamera asks: one's own camera, in third person (back) while the leopard is out, pulled back and up so its 5 blocks
     * are seen whole, easing out as it comes and in as it goes; null otherwise.
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
        float out = LeopardForm.revealed(entry.phase, ticks(entry, partial));
        if (entry.phase == LeopardForm.FORMING) {
            out = Math.max(out, Math.min(1.0F, ticks(entry, partial) / 20.0F));
        }
        if (out <= 0.0F) {
            return null;
        }
        Vec3 eye = entity.m_20299_(partial);
        Vec3 look = entity.m_20252_(partial);
        double back = 4.0 + 4.5 * out;
        double up = 2.0 * out;
        Vec3 raised = com.hexhaki.piece.client.dragon.DragonCamera.clip(entity, eye, eye.m_82520_(0.0, up, 0.0));
        return com.hexhaki.piece.client.dragon.DragonCamera.clip(entity, raised, raised.m_82520_(-look.f_82479_ * back, -look.f_82480_ * back,
            -look.f_82481_ * back));
    }

    /** Whether one's own player is the leopard (the panel shows its moves; client/CombatMode: no Style while it is). */
    public static boolean selfOut() {
        Minecraft minecraft = Minecraft.m_91087_();
        return minecraft.f_91074_ != null && ENTRIES.containsKey(minecraft.f_91074_.m_19879_());
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ENTRIES.clear();
        BLOWS.clear();
        ROARS.clear();
        fromFirstPerson = false;
    }
}
