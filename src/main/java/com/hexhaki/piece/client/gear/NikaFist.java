package com.hexhaki.piece.client.gear;

import com.hexhaki.client.devilfruit.RemoteGearStates;
import com.hexhaki.client.devilfruit.render.RubberLimbGeometry;
import com.hexhaki.client.devilfruit.render.RubberLimbMesh;
import com.hexhaki.devilfruit.data.BajrangPhase;
import com.hexhaki.piece.client.render.Bulk;
import com.hexhaki.piece.dragon.FireMesh;
import com.hexhaki.piece.gear.NikaArt;
import com.hexhaki.piece.gear.NikaSlam;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.player.AbstractClientPlayer;
import org.joml.Vector3f;

/**
 * 3.2.6 (the owner, on the Bajrang Gun: "The fist is like should be ... more shaped like a fist and more rounder ... a big one. And it
 * should be coming first from above, and then the player slowly rotates it downwards and slams it down"): the original's
 * {@code BajrangFistLayer} draws the giant fist as the player's own hand cube blown up, at the end of the stretched arm, overhead while it
 * charges and straight down in the slam. patch/PiecePatch.nikaFist makes it ask here:
 * <ul>
 *   <li>{@link #begin}: whose arm it draws (and since when that player's slam has gone on).</li>
 *   <li>{@link #centre} in place of its fistCentre: while it charges, straight up over the shoulder; in the slam the fist sweeps over the
 *       front and down as the body turns after it (gear/NikaSlam: the clip gear5_bajrang_slam turns the body by the same numbers, so
 *       here the fist's way is put in the turned body's own frame and it still comes down straight at the crater), then out along the
 *       reach to the ground.</li>
 *   <li>{@link #emitSkin} and {@link #emitOther} in place of its three RubberLimbMesh.emit calls (the skin, the sleeve, the Haki coat):
 *       the stretched arm as before but its hand cube half the size (hidden inside), and with the skin's, the rounded fist of
 *       gear/NikaArt around it, its colour the player's own hand texel (cel-shaded in bands, lit evenly).</li>
 * </ul>
 */
public final class NikaFist {
    /** The hand cube drawn at this share of its size (inside the rounded fist). */
    static final float HIDE = 0.5F;
    /** Each player's slam: the game time (and the partial tick) it began. */
    static final Map<Integer, Double> SLAMS = new HashMap<>();
    static AbstractClientPlayer player;
    static double now;

    private NikaFist() {
    }

    /** The original's layer begins to draw {@code who}'s fist ({@code partial} into the tick). */
    public static void begin(AbstractClientPlayer who, float partial) {
        player = who;
        now = who.m_9236_().m_46467_() + (double) partial;
        RemoteGearStates.State state = RemoteGearStates.get(who.m_19879_());
        BajrangPhase phase = state == null ? null : state.bajrang;
        boolean slamming = phase == BajrangPhase.SLAM || phase == BajrangPhase.IMPACT || phase == BajrangPhase.FALL;
        if (slamming) {
            SLAMS.putIfAbsent(who.m_19879_(), now);
        } else {
            SLAMS.remove(who.m_19879_());
        }
        if (SLAMS.size() > 64) {
            SLAMS.clear();
        }
    }

    /**
     * Where the fist's middle is from the shoulder, in the model's own frame (x to the left, y down, z behind), for the original's
     * {@code fistCentre(phase, reach, radius, flow)}.
     */
    public static Vector3f centre(BajrangPhase phase, float reach, float radius, float flow) {
        float k = 1.1F + radius;
        if (phase == BajrangPhase.CHARGE) {
            // held straight up over the shoulder (the body leans back: gear5_bajrang_charge), bobbing with its weight
            float bob = (float) Math.sin(flow * 0.07F) * 0.12F;
            return new Vector3f(-0.25F * k, -k + bob, 0.1F * k);
        }
        Double since = player == null ? null : SLAMS.get(player.m_19879_());
        double t = since == null ? 1000.0 : now - since;
        double theta = NikaSlam.theta(t);
        double angle = Math.toRadians(theta - NikaSlam.pitch(t)); // in the turned body: 0 over the head, 90 ahead, 180 at the feet
        float length = theta < 178.0 ? k : Math.max(k, reach);
        float out = Math.max(0.0F, k - reach * 0.35F) * 0.6F;
        return new Vector3f(-out, (float) (-Math.cos(angle) * length), (float) (-Math.sin(angle) * length));
    }

    /** The skin's pass: the stretched arm with its hand cube hidden, and the rounded fist round it. */
    public static void emitSkin(PoseStack.Pose pose, VertexConsumer out, int light, RubberLimbGeometry geometry, boolean slim, float twist,
                                Vector3f start, Vector3f control, Vector3f end, Vector3f side, float width, float hand, int segments, float a,
                                float b, float c, float d) {
        RubberLimbMesh.emit(pose, out, light, geometry, slim, twist, start, control, end, side, width, hand * HIDE, segments, a, b, c, d);
        try {
            fist(pose, out, light, start, end, side, hand, slim);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: the Bajrang Gun's fist could not be drawn", failure);
        }
    }

    /** The sleeve's and the Haki coat's passes: the stretched arm, its hand cube hidden. */
    public static void emitOther(PoseStack.Pose pose, VertexConsumer out, int light, RubberLimbGeometry geometry, boolean slim, float twist,
                                 Vector3f start, Vector3f control, Vector3f end, Vector3f side, float width, float hand, int segments, float a,
                                 float b, float c, float d) {
        RubberLimbMesh.emit(pose, out, light, geometry, slim, twist, start, control, end, side, width, hand * HIDE, segments, a, b, c, d);
    }

    static final float[] M = new float[12];
    static final float[] N = new float[9];

    /** The skin texel the fist takes its colour from (the right hand's end: the arm's bottom face). */
    static float u;
    static float v;
    static int packed;
    static VertexConsumer target;

    static final FireMesh.Sink SINK = (xyz, argb) -> {
        for (int k = 0; k < 4; k++) {
            int col = argb[k];
            Bulk.vertex(target, M, N, (float) xyz[k * 3], (float) xyz[k * 3 + 1], (float) xyz[k * 3 + 2], ((col >> 16) & 255) / 255.0F,
                ((col >> 8) & 255) / 255.0F, (col & 255) / 255.0F, 1.0F, u, v, net.minecraft.client.renderer.texture.OverlayTexture.f_118083_, packed,
                0.0F, -1.0F, 0.0F);
        }
    };

    /**
     * 3.3.9: a fist of radius {@code r} whose middle is at (cx, cy, cz), knuckles toward (fx, fy, fz), the back of the hand toward (ux,
     * uy, uz), a right hand or a left one, in the skin's colour (client/gear/NikaPistolArm: Dawn Pistol's).
     */
    static void fistAt(PoseStack.Pose pose, VertexConsumer out, int light, float cx, float cy, float cz, float fx, float fy, float fz, float ux,
                       float uy, float uz, float r, boolean slim, boolean right) {
        if (r < 0.02F) {
            return;
        }
        Bulk.read(pose, M, N);
        target = out;
        packed = light;
        u = (slim ? 48.5F : 50.0F) / 64.0F;
        v = 18.0F / 64.0F;
        NikaArt.light(0.33, -0.79, -0.51); // the model's frame: y down, z behind
        NikaArt.fist(SINK, cx, cy, cz, fx, fy, fz, ux, uy, uz, r, r > 1.5F, right);
        target = null;
    }

    static void fist(PoseStack.Pose pose, VertexConsumer out, int light, Vector3f start, Vector3f end, Vector3f side, float hand, boolean slim) {
        float r = hand * 0.125F; // the hand cube's half size: the fist's own radius
        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float dz = end.z - start.z;
        float dl = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (r < 0.02F || dl < 1.0E-5F) {
            return;
        }
        dx /= dl;
        dy /= dl;
        dz /= dl;
        // the back of the hand: square to the arm and its side (the layer's own side: the arm's way across the world's up)
        float ux = side.y * dz - side.z * dy;
        float uy = side.z * dx - side.x * dz;
        float uz = side.x * dy - side.y * dx;
        Bulk.read(pose, M, N);
        target = out;
        packed = light;
        u = (slim ? 48.5F : 50.0F) / 64.0F;
        v = 18.0F / 64.0F;
        NikaArt.light(0.33, -0.79, -0.51); // the model's frame: y down, z behind
        NikaArt.fist(SINK, end.x + dx * r, end.y + dy * r, end.z + dz * r, dx, dy, dz, ux, uy, uz, r, r > 1.5F);
        target = null;
    }
}
