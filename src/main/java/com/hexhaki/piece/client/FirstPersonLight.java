package com.hexhaki.piece.client;

import com.hexhaki.piece.client.npc.StylePoser;
import com.hexhaki.piece.client.render.Bulk;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.pika.LightClone;
import com.hexhaki.piece.style.SkinBox;
import com.hexhaki.piece.style.StyleRig;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 2.9.0 (the owner, on Ama no Murakumo: "the animations of the sword are fucking awful. Not to mention that they don't even work in
 * first person"; on the laser: "It's not on the, in front of the fist. It's just moving around. Like as I move around"): the Light
 * fruit's moves as the player sees them, and where its fingertip really is.
 * <ul>
 *   <li>Every clip each player is told to play (PieceHooks.played) is kept with its start, so each client knows which of the Light
 *       fruit's moves a player is in.</li>
 *   <li>In first person, while the local player cuts with the sword, guards, draws it, points or charges the laser, the game's own hand
 *       is put away and the right arm (the player's own skin, both layers) and the sword of light are drawn posed by the very keyframes the
 *       third person plays (client/npc/StylePoser), hung from the view: the cut sweeps across the screen as the arm does in third
 *       person, the laser points where the player looks. The Light fruit's kicks show their legs (client/FirstPersonLegs).</li>
 *   <li>The fingertip: where the laser leaves the hand and the light gathers, from the pose of the clip playing (the view's in first
 *       person, the body's in third), every frame, so it never drifts off the hand as the player moves.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class FirstPersonLight {
    /** A clip a player was told to play, from what game tick. */
    record Clip(String name, long start) {
    }

    private static final Map<Integer, Clip> CLIPS = new HashMap<>();
    /** The clips whose right arm (and sword) are drawn in first person, and how long each lasts (ticks; 0: until the next clip). */
    static final Map<String, Integer> ARM = new HashMap<>();
    /** The laser's clips: the fingertip comes from their pose. */
    static final Set<String> POINTING = Set.of("pika_laser", "pika_laser_charge", "pika_laser_fire");
    /** The sword's clips: the sword is in the hand. */
    static final Set<String> SWORD = Set.of("pika_sword_1", "pika_sword_2", "pika_murakumo");
    private static final float[] M = new float[12];
    private static final float[] N = new float[9];

    static {
        // 2.9.7: the sword's cuts on the attack button are gone; the lunge (C) ends on the level cut (its flight is the game's own hand
        // holding the sword: the lunge lays the body out flat, an arm hung from that would float below the view)
        ARM.put("pika_sword_1", 10);
        ARM.put("pika_sword_2", 10);
        ARM.put("pika_murakumo", 11);
        ARM.put("pika_laser", 9);
        ARM.put("pika_laser_charge", 0);
        ARM.put("pika_laser_fire", 11);
    }

    private FirstPersonLight() {
    }

    static long now() {
        Minecraft minecraft = Minecraft.m_91087_();
        return minecraft.f_91073_ == null ? 0L : minecraft.f_91073_.m_46467_();
    }

    /** PieceHooks.played: {@code id} was told to play {@code name}. */
    static void played(int id, String name) {
        if (name == null) {
            return;
        }
        long now = now();
        CLIPS.put(id, new Clip(name, now));
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ != null && minecraft.f_91074_.m_19879_() == id && name.startsWith("pika_")) {
            FirstPersonLegs.clip(name, now);
        }
    }

    /** The clip {@code id} is in if it is one of {@code wanted} and not over, else null. */
    static Clip clip(int id, Map<String, Integer> wanted, Set<String> or, float partial) {
        Clip clip = CLIPS.get(id);
        if (clip == null) {
            return null;
        }
        Integer life = wanted != null ? wanted.get(clip.name) : or.contains(clip.name) ? ARM.getOrDefault(clip.name, 0) : null;
        if (life == null) {
            return null;
        }
        float ticks = now() - clip.start + partial;
        return life > 0 && ticks > life ? null : clip;
    }

    /** Whether the local player's right arm is drawn here now (first person, one of the {@link #ARM} clips). */
    static boolean drawing(float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        return player != null && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON && clip(player.m_19879_(), ARM, null, partial) != null;
    }

    /** Whether the Light fruit's own first-person blade (PikaClient) should stand down: the arm drawn here carries it. */
    public static boolean armDrawn(float partial) {
        return drawing(partial);
    }

    // ------------------------------------------------------------------------------------------------------ the frames

    /**
     * A point of the posed body (x to the left, y up, z ahead, from the feet) where it is, relative to the camera: hung from the view
     * (the eye turned by the look's yaw and pitch) in first person, from the feet turned by the body's facing in third.
     */
    static void place(Player player, boolean view, float partial, Vec3 cam, double[] p, double[] out) {
        double x = p[0];
        double y = p[1];
        double z = p[2];
        double yaw;
        double ox;
        double oy;
        double oz;
        if (view) {
            double eye = player.m_20192_();
            y -= eye;
            double pitch = Math.toRadians(player.f_19860_ + (player.m_146909_() - player.f_19860_) * partial);
            double ny = y * Math.cos(pitch) - z * Math.sin(pitch);
            double nz = y * Math.sin(pitch) + z * Math.cos(pitch);
            y = ny;
            z = nz;
            yaw = Math.toRadians(player.f_19859_ + FirstPersonLegs.wrap(player.m_146908_() - player.f_19859_) * partial);
            Vec3 feet = player.m_20318_(partial);
            ox = feet.f_82479_ - cam.f_82479_;
            oy = feet.f_82480_ + eye - cam.f_82480_;
            oz = feet.f_82481_ - cam.f_82481_;
        } else {
            yaw = Math.toRadians(player.f_20884_ + FirstPersonLegs.wrap(player.f_20883_ - player.f_20884_) * partial);
            Vec3 feet = player.m_20318_(partial);
            ox = feet.f_82479_ - cam.f_82479_;
            oy = feet.f_82480_ - cam.f_82480_;
            oz = feet.f_82481_ - cam.f_82481_;
        }
        double c = Math.cos(yaw);
        double s = Math.sin(yaw);
        out[0] = ox + x * c - z * s;
        out[1] = oy + y;
        out[2] = oz + x * s + z * c;
    }

    /**
     * Where {@code player}'s pointing fingertip is (world), or null when they are not in one of the laser's clips: the light leaves
     * from just past the right fist, as the pose has it this frame.
     */
    public static Vec3 fingertip(Player player, float partial) {
        Clip clip = clip(player.m_19879_(), null, POINTING, partial);
        if (clip == null) {
            return null;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        boolean view = player == minecraft.f_91074_ && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON;
        float[][] pose = StylePoser.sample(clip.name, now() - clip.start + partial);
        double[] out = new double[3];
        place(player, view, partial, Vec3.f_82478_, StyleRig.limb(pose, StyleRig.RIGHT_ARM, 0.0, 1.1, -0.5), out);
        return new Vec3(out[0], out[1], out[2]);
    }

    // ------------------------------------------------------------------------------------------------------ first person

    @SubscribeEvent
    public static void hand(RenderHandEvent event) {
        if (drawing(event.getPartialTick())) {
            event.setCanceled(true); // the arm drawn below in its place
        }
    }

    @SubscribeEvent
    public static void stage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || CLIPS.isEmpty()) {
            return;
        }
        float partial = event.getPartialTick();
        if (!drawing(partial)) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        Clip clip = CLIPS.get(player.m_19879_());
        try {
            draw(event, player, clip, partial);
        } catch (RuntimeException failure) {
            CLIPS.remove(player.m_19879_()); // an arm never stops the game
        }
    }

    static void draw(RenderLevelStageEvent event, LocalPlayer player, Clip clip, float partial) {
        Minecraft minecraft = Minecraft.m_91087_();
        Vec3 cam = event.getCamera().m_90583_();
        float[][] pose = StylePoser.sample(clip.name, now() - clip.start + partial);
        MultiBufferSource.BufferSource buffers = minecraft.m_91269_().m_110104_();
        // the right arm, both halves, both layers, in the player's own skin
        RenderType skin = RenderType.m_110458_(player.m_108560_());
        VertexConsumer out = buffers.m_6299_(skin);
        Bulk.read(event.getPoseStack().m_85850_(), M, N);
        Vec3 feet = player.m_20318_(partial);
        int light = LevelRenderer.m_109541_(minecraft.f_91073_, BlockPos.m_274561_(feet.f_82479_, feet.f_82480_ + 1.0, feet.f_82481_));
        double[] world = new double[24];
        double[] p = new double[3];
        double[] q = new double[3];
        for (int half = 0; half < 2; half++) {
            for (int layer = 0; layer < 2; layer++) {
                double[] c = StyleRig.half(pose, StyleRig.RIGHT_ARM, half == 1, layer == 1 ? 0.25 : 0.0);
                for (int k = 0; k < 8; k++) {
                    p[0] = c[k * 3];
                    p[1] = c[k * 3 + 1];
                    p[2] = c[k * 3 + 2];
                    place(player, true, partial, cam, p, q);
                    System.arraycopy(q, 0, world, k * 3, 3);
                }
                for (int[] f : SkinBox.faces(SkinBox.RIGHT_ARM, layer == 1, half * 6, half * 6 + 6)) {
                    FirstPersonLegs.face(out, world, f, light);
                }
            }
        }
        buffers.m_109912_(skin);
        // the sword of light in the fist, as the third person holds it
        if (SWORD.contains(clip.name) || com.hexhaki.piece.client.pika.PikaFx.sword(player.m_19879_())) {
            double[] fist = new double[3];
            double[] front = new double[3];
            double[] elbow = new double[3];
            place(player, true, partial, cam, StyleRig.limb(pose, StyleRig.RIGHT_ARM, 0.0, 0.967, 0.0), fist);
            place(player, true, partial, cam, StyleRig.limb(pose, StyleRig.RIGHT_ARM, 0.0, 0.967, -2.0), front);
            place(player, true, partial, cam, StyleRig.limb(pose, StyleRig.RIGHT_ARM, 0.0, 0.55, 0.0), elbow);
            RenderType glow = RenderType.m_269166_();
            Matrix4f matrix = event.getPoseStack().m_85850_().m_252922_();
            VertexConsumer buffer = buffers.m_6299_(glow);
            FireMesh.Sink sink = (xyz, argb) -> {
                for (int k = 0; k < 4; k++) {
                    int c = argb[k];
                    buffer.m_252986_(matrix, (float) xyz[k * 3], (float) xyz[k * 3 + 1], (float) xyz[k * 3 + 2])
                        .m_85950_(((c >>> 16) & 255) / 255.0F, ((c >>> 8) & 255) / 255.0F, (c & 255) / 255.0F, ((c >>> 24) & 255) / 255.0F)
                        .m_5752_();
                }
            };
            LightClone.sword(sink, fist, front, elbow, 1.0, (player.f_19797_ + partial) * 0.2);
            buffers.m_109912_(glow);
        }
    }

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        CLIPS.clear();
    }
}
