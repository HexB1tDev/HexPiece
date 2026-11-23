package com.hexhaki.piece.client.gear;

import com.hexhaki.client.devilfruit.RubberLimbStates;
import com.hexhaki.client.devilfruit.render.GearOutfitLayer;
import com.hexhaki.client.devilfruit.render.RubberLimbGeometry;
import com.hexhaki.client.devilfruit.render.RubberLimbMesh;
import com.hexhaki.devilfruit.data.StretchStyle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/**
 * 3.3.9: Dawn Pistol's fist (the owner accepted "the stretched arm ending in a big ballooned rubber fist like the Bajrang Gun's (smaller)
 * coated black by Haki"). The original's {@code RubberLimbLayer.draw} draws every stretched limb: the arm through
 * {@code GearOutfitLayer.emitWithCuff} (the skin and the jacket's cuff), the sleeve and, for a Haki throw (the stretch's {@code variant}
 * 1: it is drawn Haki-black, 0.11 0.11 0.14), the dawn fire round it, both through {@code RubberLimbMesh.emit}. patch/PiecePatch
 * .pistolArm makes it ask here: {@link #begin} which stretch it draws; {@link #emitSkin} and {@link #emitOther} in place of those calls.
 * For a Dawn Pistol (the PISTOL style with variant 1) the hand cube is drawn at {@link NikaFist#HIDE} (hidden inside) and the Bajrang
 * Gun's ballooned fist (gear/NikaArt.fist) is drawn round it {@link #BALLOON} times the hand's size, Haki-black ({@link #COAT}), inflating
 * as the arm reaches out; anything else is drawn exactly as before. Third person only: the original's first person is its own
 * (renderFirstPerson), left alone.
 */
public final class NikaPistolArm {
    /** The fist's radius over the hand cube's half size, when the arm is out (it inflates from {@link #FROM} of it). 3.3.9's first
     * renders: 5.5 the owner called "way too big"; 3.0 (about three-quarters of a block across). */
    static final float BALLOON = 3.0F;
    static final float FROM = 0.4F;
    /** How far out (blocks from the shoulder) the arm is when the fist has fully inflated. */
    static final float FULL_AT = 2.5F;
    /** The Haki coat's colour over the cel bands (dark, a little violet, so the knuckles still read). */
    public static final int COAT = 0x3A3248;

    static RubberLimbStates.Stretch current;
    /**
     * 3.4.9 (the owner: "the Dawn pistol, I want it to just be a straight punch from third person. Like if you notice the third person,
     * the arm does a zigzag, which I don't like. It should be a straight punch. Just like the first person, exactly like it, but with the
     * animation that we got, basically the Looney Tunes stuff"): 3.4.1's loop (gear/NikaArc) is gone. A Dawn Pistol's arm is one straight
     * stretch of the original's own limb mesh from the shoulder to the fist (its control point the middle of the way: no bow), out through
     * the struck body to the fist at the stretched skin's end while it stretches; the body's wind-up (gear5_dawn_pistol, Toon Force) stays.
     */
    static final Vector3f MID = new Vector3f();
    static final Vector3f TO = new Vector3f();

    private NikaPistolArm() {
    }

    /** At the head of the original's draw: the stretch it is about to draw. */
    public static void begin(RubberLimbStates.Stretch stretch) {
        current = stretch;
        reaching = false;
    }

    /**
     * 3.4.1 (the owner: "the fist goes through the body, stretching the skin"): while the struck body's skin stretches (client/gear
     * NikaSkinClient), this thrower's arm reaches on through the body to the fist inside the skin's end: its end moved along its own way to
     * the shoulder's distance from the body plus the skin's stretch (the model's frame is the world's at 0.9375 to a block). Set by the
     * skin's pass (it has the player) and kept for the sleeve's and dawn fire's of the same arm.
     */
    static boolean reaching;
    static final Vector3f REACH = new Vector3f();

    static Vector3f through(Vector3f start, Vector3f end, AbstractClientPlayer player) {
        reaching = false;
        net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.m_91087_().f_91073_;
        if (player == null || level == null) {
            return end;
        }
        float partial = net.minecraft.client.Minecraft.m_91087_().m_91296_();
        double more = NikaSkinClient.reach(level, player.m_19879_(), level.m_46467_() + partial);
        LivingEntity body = NikaSkinClient.BODY;
        if (more < 0 || body == null) {
            return end;
        }
        double px = player.f_19790_ + (player.m_20185_() - player.f_19790_) * partial;
        double py = player.f_19791_ + (player.m_20186_() - player.f_19791_) * partial + 1.35;
        double pz = player.f_19792_ + (player.m_20189_() - player.f_19792_) * partial;
        double bx = body.f_19790_ + (body.m_20185_() - body.f_19790_) * partial;
        double by = body.f_19791_ + (body.m_20186_() - body.f_19791_) * partial + body.m_20206_() * 0.55;
        double bz = body.f_19792_ + (body.m_20189_() - body.f_19792_) * partial;
        double world = Math.sqrt((bx - px) * (bx - px) + (by - py) * (by - py) + (bz - pz) * (bz - pz)) + Math.max(0, more);
        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float dz = end.z - start.z;
        float l = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (l < 1.0E-4F) {
            return end;
        }
        float k = (float) (world / 0.9375) / l;
        reaching = true;
        return REACH.set(start.x + dx * k, start.y + dy * k, start.z + dz * k);
    }

    static boolean dawnPistol() {
        RubberLimbStates.Stretch s = current;
        return s != null && s.variant == 1 && s.style == StretchStyle.PISTOL;
    }

    /** The straight way from {@code start} to {@code to} (or REACH while reaching through the body): its middle into MID, its end into TO. */
    static void straight(Vector3f start, Vector3f end) {
        Vector3f to = reaching ? REACH : end;
        TO.set(to.x, to.y, to.z);
        MID.set((start.x + to.x) * 0.5F, (start.y + to.y) * 0.5F, (start.z + to.z) * 0.5F);
    }

    /** The skin's pass (the arm and the jacket's cuff): a Dawn Pistol's fist first, into the same batch, then the straight arm, its hand
     * hidden inside the fist. */
    public static void emitSkin(PoseStack.Pose pose, VertexConsumer out, int light, RubberLimbGeometry geometry, boolean slim, float twist,
                                Vector3f start, Vector3f control, Vector3f end, Vector3f side, float width, float hand, int segments, float a,
                                float b, float c, float d, MultiBufferSource buffers, AbstractClientPlayer player) {
        if (!dawnPistol()) {
            GearOutfitLayer.emitWithCuff(pose, out, light, geometry, slim, twist, start, control, end, side, width, hand, segments, a, b, c, d,
                buffers, player);
            return;
        }
        through(start, end, player);
        straight(start, end);
        try {
            fist(pose, out, light, start, start, TO, side, hand, slim);
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: Dawn Pistol's fist could not be drawn", failure);
        }
        // last: the cuff may move on to the cuff's own batch
        GearOutfitLayer.emitWithCuff(pose, out, light, geometry, slim, twist, start, MID, TO, side, width, hand * NikaFist.HIDE, segments, a, b,
            c, d, buffers, player);
    }

    /** The sleeve's and the dawn fire's passes: a Dawn Pistol's arm straight, its hand cube hidden in the fist. */
    public static void emitOther(PoseStack.Pose pose, VertexConsumer out, int light, RubberLimbGeometry geometry, boolean slim, float twist,
                                 Vector3f start, Vector3f control, Vector3f end, Vector3f side, float width, float hand, int segments, float a,
                                 float b, float c, float d) {
        if (!dawnPistol()) {
            RubberLimbMesh.emit(pose, out, light, geometry, slim, twist, start, control, end, side, width, hand, segments, a, b, c, d);
            return;
        }
        straight(start, end);
        RubberLimbMesh.emit(pose, out, light, geometry, slim, twist, start, MID, TO, side, width, hand * NikaFist.HIDE, segments, a, b, c, d);
    }

    /**
     * The fist at the arm's end, knuckles along the arm's last stretch (from the bow's control point), the back of the hand up (the
     * model's frame: y down), a right hand on the right arm (the owner: "thats the left hand on the right arm"), inflating as it reaches.
     */
    static void fist(PoseStack.Pose pose, VertexConsumer out, int light, Vector3f start, Vector3f control, Vector3f end, Vector3f side,
                     float hand, boolean slim) {
        float ox = end.x - start.x;
        float oy = end.y - start.y;
        float oz = end.z - start.z;
        float reach = (float) Math.sqrt(ox * ox + oy * oy + oz * oz);
        float grow = FROM + (1.0F - FROM) * Math.min(1.0F, reach / FULL_AT);
        float fx = end.x - control.x;
        float fy = end.y - control.y;
        float fz = end.z - control.z;
        float fl = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (fl < 1.0E-3F) {
            fx = ox;
            fy = oy;
            fz = oz;
            fl = reach;
        }
        if (fl < 1.0E-4F) {
            return;
        }
        fx /= fl;
        fy /= fl;
        fz /= fl;
        float r = hand * 0.125F * BALLOON * grow;
        RubberLimbStates.Stretch s = current;
        boolean right = s != null && s.limb != null && s.limb.right();
        int tint = com.hexhaki.piece.gear.NikaArt.tint;
        com.hexhaki.piece.gear.NikaArt.tint = COAT;
        try {
            // the back of the hand up: the model's up is -y
            NikaFist.fistAt(pose, out, light, end.x + fx * r, end.y + fy * r, end.z + fz * r, fx, fy, fz, 0.0F, -1.0F, 0.0F, r, slim, right);
        } finally {
            com.hexhaki.piece.gear.NikaArt.tint = tint;
        }
    }
}
