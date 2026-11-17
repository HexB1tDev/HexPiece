package com.hexhaki.piece.client.sword;

import com.hexhaki.gore.GoreRegistry;
import com.hexhaki.gore.client.BloodStains;
import com.hexhaki.piece.network.S2CFinisher;
import com.hexhaki.piece.sword.Carcass;
import com.hexhaki.piece.sword.CutPose;
import com.hexhaki.piece.sword.Finisher;
import com.hexhaki.piece.sword.Ragdoll;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * The Ryou finisher as every client sees it (server/Finishers), since 2.2.1 HexGhoul's FinisherClient turned upright (the owner: "the
 * slice in half should 100% be taken from Hex Ghoul ... the cut and everything about it. Just make sure instead of horizontal, it's
 * vertical"). Through the stun, a thin line of light draws itself round the body along the blade's upright plane, from the middle of the
 * chest's front both ways at once, up over the face and the crown and down through the crotch ({@link Carcass#wound}; round the box of
 * anything not built like a person, {@link Finisher#line}). When it meets itself the body is no longer drawn (no death animation ever
 * plays on it) and falls apart in two as a ragdoll ({@link Carcass#cut}, {@link Ragdoll}: on the blocks as the game's collision has
 * them), its own skin on each half, what is inside on each cut, the original mod's blood dripping from the cuts and pooling where the
 * halves lie. They lie there {@link #LIFE} ticks (forty seconds), then fade away over {@link #FADE}. The victim, if it is this client's
 * player, also sees the line run down the middle of its own view.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class FinisherFx {
    /** How long the halves lie before they start to fade, and how long the fade takes (ticks). */
    public static final double LIFE = 800.0;
    public static final double FADE = 100.0;
    /** The most bodies lying cut up at once (the oldest go first). */
    static final int MOST = 8;
    /** How far off they are still simulated and drawn (blocks). */
    static final double FAR = 64.0;
    private static final RenderType WHITE = RenderType.m_110473_(SwordFx.WHITE_TEXTURE);
    private static final int FULL_BRIGHT = 0xF000F0;

    /** A body under the finisher: when its line began, how long it takes (ticks), the blade's way, the light and the pose it was last drawn in. */
    private static final class Stunned {
        final double from;
        final int ticks;
        final float dx;
        final float dz;
        int light = FULL_BRIGHT;
        CutPose pose;

        Stunned(double from, int ticks, float dx, float dz) {
            this.from = from;
            this.ticks = Math.max(1, ticks);
            this.dx = dx;
            this.dz = dz;
        }
    }

    /** A body lying in pieces: the halves, their bodies, its skin, the light it was in, when it was cut, which halves have pooled. */
    private static final class Corpse {
        final List<Carcass.Piece> pieces;
        final List<Ragdoll.Body> bodies;
        final RenderType skin;
        final int light;
        final double born;
        final boolean bleeds;
        final boolean[] pooled;

        Corpse(List<Carcass.Piece> pieces, List<Ragdoll.Body> bodies, ResourceLocation skin, int light, double born, boolean bleeds) {
            this.pieces = pieces;
            this.bodies = bodies;
            this.skin = RenderType.m_110473_(skin);
            this.light = light;
            this.born = born;
            this.bleeds = bleeds;
            this.pooled = new boolean[pieces.size()];
        }
    }

    private static final Map<Integer, Stunned> STUNNED = new HashMap<>();
    /** Bodies cut apart, not drawn any more, until when (world ticks). */
    private static final Map<Integer, Double> HIDDEN = new HashMap<>();
    private static final List<Corpse> CORPSES = new ArrayList<>();
    private static Boolean skinLayers3d;

    private FinisherFx() {
    }

    static double now(float partial) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        return level == null ? 0.0 : level.m_46467_() + partial;
    }

    /** The server's word ({@link S2CFinisher#FINISH} or {@link S2CFinisher#SPLIT}). */
    public static void accept(S2CFinisher message) {
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        if (level == null) {
            return;
        }
        double now = now(0.0F);
        if (message.kind() == S2CFinisher.FINISH) {
            if (STUNNED.size() > 64) {
                STUNNED.clear();
            }
            STUNNED.put(message.entity(), new Stunned(now, message.ticks(), message.dx(), message.dz()));
            return;
        }
        Stunned stunned = STUNNED.remove(message.entity());
        if (!(level.m_6815_(message.entity()) instanceof LivingEntity body)) {
            return;
        }
        int light = stunned == null ? FULL_BRIGHT : stunned.light;
        Corpse corpse = corpse(body, light, now, message.yaw(), message.dx(), message.dz(), stunned == null ? null : stunned.pose);
        if (corpse == null) {
            return; // not built like a person: it bleeds and dies as it always did
        }
        HIDDEN.put(message.entity(), now + LIFE + FADE);
        if (CORPSES.size() >= MOST) {
            CORPSES.remove(0);
        }
        CORPSES.add(corpse);
    }

    /** The cut along the blade going ({@code dx}, {@code dz}) through a body turned {@code yaw} degrees: the side it came in from, in the body's own space. */
    static Carcass.Plane plane(float yaw, float dx, float dz) {
        double y = Math.toRadians(yaw);
        // from the world into the body's space: a turn about y by its yaw
        double sx = -dx;
        double sz = -dz;
        return Carcass.Plane.from(new double[] {sx * Math.cos(y) + sz * Math.sin(y), 0.0, -sx * Math.sin(y) + sz * Math.cos(y)});
    }

    /** How {@code body} is built for the finisher: its layout, skin, scale and insides; null if it is not built like a person. */
    record Build(Carcass.Layout layout, ResourceLocation skin, double scale, Finisher.Inside inside) {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static Build build(LivingEntity body) {
        boolean bleeds = com.hexhaki.gore.Gore.bleeds(body);
        if (body instanceof AbstractClientPlayer player) {
            return new Build("slim".equals(player.m_108564_()) ? Carcass.Layout.PLAYER_SLIM : Carcass.Layout.PLAYER_WIDE, player.m_108560_(), 0.9375,
                Finisher.Inside.BODY);
        }
        EntityRenderer renderer = Minecraft.m_91087_().m_91290_().m_114382_(body);
        if (!(renderer instanceof LivingEntityRenderer living) || !(living.m_7200_() instanceof HumanoidModel<?> model) || body.m_6162_()) {
            return null;
        }
        ResourceLocation skin = renderer.m_5478_(body);
        String path = skin.m_135815_();
        Carcass.Layout layout = model instanceof PlayerModel<?> ? Carcass.Layout.PLAYER_WIDE
            : path.contains("skeleton") || path.contains("stray") ? Carcass.Layout.THIN : Carcass.Layout.HUMANOID;
        // The mod's own people (the pirates and marines) have insides; every other mob is meat, or dry bone if it doesn't bleed.
        boolean person = body.getClass().getName().startsWith("com.hexhaki.");
        return new Build(layout, skin, 1.0, !bleeds ? Finisher.Inside.DRY : person ? Finisher.Inside.BODY : Finisher.Inside.MEAT);
    }

    /** {@code body} cut apart: its halves (null if it is not built like a person). */
    static Corpse corpse(LivingEntity body, int light, double now, float yaw, float dx, float dz, CutPose drawn) {
        Build build = build(body);
        if (build == null) {
            bleed(body);
            return null;
        }
        CutPose pose = drawn == null ? new CutPose() : drawn;
        Carcass.Plane plane = plane(yaw, dx, dz);
        List<Carcass.Piece> pieces = Carcass.cut(pose, build.layout(), build.scale(), build.inside(), plane);
        double[] turn = Ragdoll.about(new double[] {0.0, 1.0, 0.0}, -Math.toRadians(yaw));
        double[] feet = {body.m_20185_(), body.m_20186_(), body.m_20189_()};
        List<Ragdoll.Body> bodies = new ArrayList<>();
        for (Carcass.Piece piece : pieces) {
            Ragdoll.Body b = new Ragdoll.Body(piece.boxes.toArray(new double[0][]), feet, turn);
            double[][] launch = Carcass.launch(piece.kind, plane);
            double[] v = Ragdoll.rotate(turn, launch[0]);
            double[] w = Ragdoll.rotate(turn, launch[1]);
            System.arraycopy(v, 0, b.v, 0, 3);
            System.arraycopy(w, 0, b.w, 0, 3);
            bodies.add(b);
        }
        if (pieces.isEmpty()) {
            return null;
        }
        return new Corpse(pieces, bodies, build.skin(), light, now, build.inside() != Finisher.Inside.DRY);
    }

    /** A body not built like a person: the original's blood thrown from where the cut ran. */
    static void bleed(LivingEntity body) {
        if (!com.hexhaki.gore.Gore.bleeds(body)) {
            return;
        }
        Vec3 middle = body.m_20182_().m_82520_(0.0, body.m_20206_() * 0.55, 0.0);
        if (!(body.m_9236_() instanceof ClientLevel level)) {
            return;
        }
        for (int i = 0; i < 24; i++) {
            drop(level, middle, new Vec3((Math.random() - 0.5) * 0.4, 0.05 + Math.random() * 0.25, (Math.random() - 0.5) * 0.4));
        }
    }

    /** One drop of the original mod's blood. */
    static void drop(ClientLevel level, Vec3 at, Vec3 speed) {
        level.m_7106_(GoreRegistry.BLOOD.get(), at.f_82479_, at.f_82480_, at.f_82481_, speed.f_82479_, speed.f_82480_, speed.f_82481_);
    }

    // ------------------------------------------------------------------ the line, and hiding the body once it is cut

    /** The line round a body under the finisher, drawn as it stands (its light and its pose kept for its halves). */
    @SubscribeEvent
    @SuppressWarnings("rawtypes")
    public static void drawn(RenderLivingEvent.Post event) {
        if (STUNNED.isEmpty()) {
            return;
        }
        LivingEntity body = event.getEntity();
        Stunned stunned = STUNNED.get(body.m_19879_());
        if (stunned == null) {
            return;
        }
        stunned.light = event.getPackedLight();
        if (event.getRenderer().m_7200_() instanceof HumanoidModel<?> model) {
            stunned.pose = posed(model);
        }
        double amount = Finisher.round((now(event.getPartialTick()) - stunned.from) * Finisher.STUN / stunned.ticks);
        if (amount <= 0.0) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        pose.m_85836_();
        float yaw = body.f_20884_ + (((body.f_20883_ - body.f_20884_) % 360.0F + 540.0F) % 360.0F - 180.0F) * event.getPartialTick();
        pose.m_252781_(new Quaternionf().rotateAxis((float)Math.toRadians(-yaw), 0.0F, 1.0F, 0.0F));
        Matrix4f matrix = pose.m_85850_().m_252922_();
        Matrix3f normals = pose.m_85850_().m_252943_();
        VertexConsumer out = event.getMultiBufferSource().m_6299_(WHITE);
        Finisher.Sink sink = (a, b, c, d, n, ca, cb, cc, cd) -> {
            for (double[] p : new double[][] {a, b, c, d}) {
                vertex(out, matrix, normals, p, Carcass.GLOW, FULL_BRIGHT, 0.5F, 0.5F, n);
            }
        };
        Carcass.Plane plane = plane(yaw, stunned.dx, stunned.dz);
        Build build = build(body);
        if (build != null && stunned.pose != null) {
            double layer = build.layout() == Carcass.Layout.PLAYER_WIDE || build.layout() == Carcass.Layout.PLAYER_SLIM
                ? skinLayers3d() ? Carcass.VOXEL_LAYER : Carcass.FLAT_LAYER : 0.0;
            Carcass.wound(stunned.pose, build.layout(), build.scale(), amount, layer, plane, sink);
        } else {
            // round the box of anything else, upright along the blade
            double half = body.m_20205_() * 0.5 + 0.02;
            // the line's own frame (x across the cut, z toward where the blade came in) turned onto the cut
            pose.m_252781_(new Quaternionf().rotateAxis((float)Math.atan2(plane.start()[0], plane.start()[2]), 0.0F, 1.0F, 0.0F));
            Matrix4f turned = pose.m_85850_().m_252922_();
            Matrix3f turnedNormals = pose.m_85850_().m_252943_();
            Finisher.line((a, b, c, d, n, ca, cb, cc, cd) -> {
                for (double[] p : new double[][] {a, b, c, d}) {
                    vertex(out, turned, turnedNormals, p, Carcass.GLOW, FULL_BRIGHT, 0.5F, 0.5F, n);
                }
            }, amount, half, body.m_20206_() + 0.02, Carcass.LINE / 16.0, Carcass.GLOW);
        }
        pose.m_85849_();
    }

    /** The pose a humanoid model was just drawn in: each part's pivot, turn and scale as the game placed it. */
    static CutPose posed(HumanoidModel<?> model) {
        CutPose p = new CutPose();
        part(p, "head", model.f_102808_);
        part(p, "torso", model.f_102810_);
        part(p, "rightArm", model.f_102811_);
        part(p, "leftArm", model.f_102812_);
        part(p, "rightLeg", model.f_102813_);
        part(p, "leftLeg", model.f_102814_);
        return p;
    }

    static void part(CutPose pose, String name, ModelPart part) {
        pose.set(name, part.f_104200_, part.f_104201_, part.f_104202_, part.f_104203_, part.f_104204_, part.f_104205_, part.f_233553_, part.f_233554_,
            part.f_233555_);
    }

    /** True if 3D Skin Layers draws the skins' second layer as voxels (the line goes just past them). */
    static boolean skinLayers3d() {
        if (skinLayers3d == null) {
            boolean loaded;
            try {
                loaded = ModList.get() != null && ModList.get().isLoaded("skinlayers3d");
            } catch (RuntimeException unknown) {
                loaded = false;
            }
            skinLayers3d = loaded;
        }
        return skinLayers3d;
    }

    /** A body cut apart is not drawn: its halves are (no death animation ever plays on it). */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SuppressWarnings("rawtypes")
    public static void hideLiving(RenderLivingEvent.Pre event) {
        if (!HIDDEN.isEmpty() && HIDDEN.containsKey(event.getEntity().m_19879_())) {
            event.setCanceled(true);
        }
    }

    /** The same for a player (before its own layers are touched). */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void hidePlayer(RenderPlayerEvent.Pre event) {
        if (!HIDDEN.isEmpty() && HIDDEN.containsKey(event.getEntity().m_19879_())) {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------ the halves

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel level = minecraft.f_91073_;
        if (level == null || minecraft.f_91074_ == null) {
            clear();
            return;
        }
        double now = now(0.0F);
        // A body that lives again (respawned under the same id, or kept by a totem) is drawn again.
        HIDDEN.entrySet().removeIf(e -> now > e.getValue() || level.m_6815_(e.getKey()) instanceof LivingEntity living && living.m_6084_()
            && living.m_21223_() > 0.0F && now > e.getValue() - LIFE - FADE + 40.0);
        STUNNED.entrySet().removeIf(e -> now - e.getValue().from > e.getValue().ticks + 100.0);
        if (CORPSES.isEmpty()) {
            return;
        }
        Ragdoll.Terrain terrain = (a, b) -> {
            BlockHitResult hit = level.m_45547_(new ClipContext(new Vec3(a[0], a[1], a[2]), new Vec3(b[0], b[1], b[2]), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, minecraft.f_91074_));
            if (hit == null || hit.m_6662_() != HitResult.Type.BLOCK) {
                return null;
            }
            Vec3 at = hit.m_82450_();
            return new double[] {at.f_82479_, at.f_82480_, at.f_82481_};
        };
        Vec3 eye = minecraft.f_91074_.m_20182_();
        for (Iterator<Corpse> it = CORPSES.iterator(); it.hasNext(); ) {
            Corpse corpse = it.next();
            double age = now - corpse.born;
            if (age > LIFE + FADE || age < -40.0) {
                it.remove();
                continue;
            }
            if (far(eye, corpse.bodies.get(0).pos)) {
                continue;
            }
            boolean awake = false;
            for (Ragdoll.Body b : corpse.bodies) {
                awake |= !b.asleep;
            }
            if (awake) {
                Ragdoll.tick(corpse.bodies, terrain);
            }
            if (!corpse.bleeds) {
                continue;
            }
            for (int i = 0; i < corpse.pieces.size(); i++) {
                Carcass.Piece piece = corpse.pieces.get(i);
                Ragdoll.Body b = corpse.bodies.get(i);
                double[] cut = cutAt(piece, b);
                if (cut == null) {
                    continue;
                }
                Vec3 at = new Vec3(cut[0], cut[1], cut[2]);
                // Each cut keeps bleeding as it falls, the original's blood; where a half comes to rest, its blood pools.
                if (age < 40.0 && Math.random() < 0.7) {
                    drop(level, at, new Vec3((Math.random() - 0.5) * 0.1, 0.02 + Math.random() * 0.04, (Math.random() - 0.5) * 0.1));
                }
                if (b.asleep && !corpse.pooled[i]) {
                    corpse.pooled[i] = true;
                    BloodStains.pool(level, new Vec3(b.pos[0], Math.floor(b.pos[1] - 0.05) + groundIn(level, b.pos), b.pos[2]), 1.1,
                        (int)(LIFE + FADE), 60);
                }
            }
        }
    }

    /** The height of the ground's top within the block under {@code p} (0..1; 0 if it can't be found). */
    static double groundIn(ClientLevel level, double[] p) {
        BlockHitResult hit = level.m_45547_(new ClipContext(new Vec3(p[0], p[1] + 0.2, p[2]), new Vec3(p[0], p[1] - 1.5, p[2]), ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, Minecraft.m_91087_().f_91074_));
        if (hit == null || hit.m_6662_() != HitResult.Type.BLOCK) {
            return 0.0;
        }
        return hit.m_82450_().f_82480_ - Math.floor(p[1] - 0.05);
    }

    /** Where a half's cut is now (the middle of what is inside it, the world), or null if it has none. */
    static double[] cutAt(Carcass.Piece piece, Ragdoll.Body body) {
        if (piece.caps.isEmpty()) {
            return null;
        }
        double[] sum = new double[3];
        int step = Math.max(1, piece.caps.size() / 24);
        int counted = 0;
        for (int i = 0; i < piece.caps.size(); i += step) {
            double[] p = piece.caps.get(i).p()[0];
            for (int k = 0; k < 3; k++) {
                sum[k] += p[k];
            }
            counted++;
        }
        for (int k = 0; k < 3; k++) {
            sum[k] = sum[k] / counted - body.offset[k];
        }
        return body.toWorld(sum);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || CORPSES.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        Vec3 camera = event.getCamera().m_90583_();
        double now = now(event.getPartialTick());
        PoseStack stack = event.getPoseStack();
        Matrix4f matrix = stack.m_85850_().m_252922_();
        Matrix3f normals = stack.m_85850_().m_252943_();
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        for (Corpse corpse : CORPSES) {
            if (far(camera, corpse.bodies.get(0).pos)) {
                continue;
            }
            double age = now - corpse.born;
            int alpha = (int)Math.round(255.0 * Math.max(0.0, Math.min(1.0, 1.0 - (age - LIFE) / FADE)));
            if (alpha <= 3) {
                continue;
            }
            VertexConsumer skin = buffers.m_6299_(corpse.skin);
            for (int i = 0; i < corpse.pieces.size(); i++) {
                Carcass.Piece piece = corpse.pieces.get(i);
                Ragdoll.Body body = corpse.bodies.get(i);
                for (Carcass.Face face : piece.faces) {
                    double[] n = Ragdoll.rotate(body.q, face.n());
                    for (int k = 0; k < 4; k++) {
                        vertex(skin, matrix, normals, place(body, face.p()[k], camera), alpha << 24 | 0xFFFFFF, corpse.light, (float)face.uv()[k][0],
                            (float)face.uv()[k][1], n);
                    }
                }
            }
            buffers.m_109912_(corpse.skin);
            VertexConsumer caps = buffers.m_6299_(WHITE);
            for (int i = 0; i < corpse.pieces.size(); i++) {
                Carcass.Piece piece = corpse.pieces.get(i);
                Ragdoll.Body body = corpse.bodies.get(i);
                for (Carcass.Cap cap : piece.caps) {
                    double[] n = Ragdoll.rotate(body.q, cap.n());
                    for (int k = 0; k < 4; k++) {
                        vertex(caps, matrix, normals, place(body, cap.p()[k], camera), alpha << 24 | cap.argb()[k] & 0xFFFFFF, corpse.light, 0.5F, 0.5F, n);
                    }
                }
            }
            buffers.m_109912_(WHITE);
        }
    }

    static boolean far(Vec3 from, double[] p) {
        double dx = p[0] - from.f_82479_;
        double dy = p[1] - from.f_82480_;
        double dz = p[2] - from.f_82481_;
        return dx * dx + dy * dy + dz * dz > FAR * FAR;
    }

    /** A point of a half (the finisher's space, about its origin) where its body has it now, from the camera. */
    static double[] place(Ragdoll.Body body, double[] p, Vec3 camera) {
        double[] w = body.toWorld(new double[] {p[0] - body.offset[0], p[1] - body.offset[1], p[2] - body.offset[2]});
        return new double[] {w[0] - camera.f_82479_, w[1] - camera.f_82480_, w[2] - camera.f_82481_};
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix, Matrix3f normals, double[] p, int argb, int light, float u, float v, double[] n) {
        out.m_252986_(matrix, (float)p[0], (float)p[1], (float)p[2])
            .m_6122_((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, argb >>> 24)
            .m_7421_(u, v)
            .m_86008_(OverlayTexture.f_118083_)
            .m_85969_(light)
            .m_252939_(normals, (float)n[0], (float)n[1], (float)n[2])
            .m_5752_();
    }

    // ------------------------------------------------------------------ the victim's own view

    /** The victim's own view: a thin red line down the middle, drawn from the centre up and down as the line goes round them, and staying. */
    static void screen(GuiGraphics graphics, int width, int height, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        Player self = minecraft.f_91074_;
        if (self == null) {
            return;
        }
        Stunned stunned = STUNNED.get(self.m_19879_());
        if (stunned == null) {
            return;
        }
        double amount = Finisher.round((now(partial) - stunned.from) * Finisher.STUN / stunned.ticks);
        if (amount <= 0.0) {
            return;
        }
        int x = width / 2;
        int reach = (int)Math.round(height * 0.5 * Math.min(1.0, amount * 1.15));
        graphics.m_280509_(x - 3, height / 2 - reach, x + 3, height / 2 + reach, 0x40FF1F2E);
        graphics.m_280509_(x - 1, height / 2 - reach, x + 1, height / 2 + reach, 0xFFFF2B3A);
    }

    @Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Overlay {
        private Overlay() {
        }

        @SubscribeEvent
        public static void register(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("hexpiece_finisher", (gui, graphics, partialTick, width, height) -> screen(graphics, width, height, partialTick));
        }
    }

    static void clear() {
        STUNNED.clear();
        HIDDEN.clear();
        CORPSES.clear();
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    /** True if {@code entity} is lying cut in two on this client (not drawn). */
    public static boolean hidden(Entity entity) {
        return !HIDDEN.isEmpty() && HIDDEN.containsKey(entity.m_19879_());
    }
}
