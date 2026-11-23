package com.hexhaki.piece.client.supa;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.client.sword.BladeCoatClient;
import com.hexhaki.piece.client.sword.KatanaRenderer;
import com.hexhaki.piece.pika.LightArt;
import com.hexhaki.piece.server.SupaKit;
import com.hexhaki.piece.supa.SupaBlades;
import com.hexhaki.piece.sword.Katana;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;

/**
 * 3.3.5: the Supa Supa no Mi on the client (client/supa/SupaFx puts it in the original's ClientKits): its moves for the panel and the
 * log, and its blades on the body, made of the owner's katana steel (supa/SupaBlades; client/sword/KatanaRenderer.steel: lit, textured,
 * coated black-purple with Armament as the katanas are): the arm blades, the finger blades, the drills, the skate blades and Spider's
 * steel skin, each on its own limb through the model's own pose (the left arm's mirrored from the right's), and the right arm's in the
 * game's own first-person hand. Which show, and since when, is SupaFx's (one small message a move). No stance.
 */
final class SupaClient implements ClientKit {
    /** The steel every blade is made of (supa/SupaBlades: the owner chose it, "Yes, build it"). */
    static final Katana STEEL = Katana.SILVER_CRANE;
    static final float SHINE = 0.7F;

    @Override
    public Fruit fruit() {
        return Fruit.SUPA;
    }

    @Override
    public List<FruitMove> moves() {
        return SupaKit.MOVES;
    }

    @Override
    public String stance(AbstractClientPlayer player, CompoundTag state, long now) {
        return null;
    }

    @Override
    public void renderBody(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model,
                           CompoundTag state, float partial, boolean invisible) {
        int id = player.m_19879_();
        int mask = SupaFx.mask(id);
        if (mask == 0 && !SupaFx.spinning(id) || invisible) {
            return;
        }
        float coat = BladeCoatClient.progress(player);
        double age = SupaFx.age(id, partial);
        if ((mask & SupaFx.STEEL) != 0) {
            ModelPart[] parts = {model.f_102808_, model.f_102810_, model.f_102811_, model.f_102812_, model.f_102813_, model.f_102814_};
            for (int i = 0; i < parts.length; i++) {
                pose.m_85836_();
                parts[i].m_104299_(pose);
                KatanaRenderer.steelBox(pose, buffers, STEEL, "skin" + i, grown(SupaBlades.PARTS[i]), light, SHINE, coat);
                pose.m_85849_();
            }
        }
        if (SupaFx.spinning(id)) {
            // Spar Break: arm blades standing out all round the body's middle, whirling (the body's frame: y down, the front -z)
            pose.m_85836_();
            model.f_102810_.m_104299_(pose);
            pose.m_85837_(0.0, SupaBlades.SPIN_Y / 16.0, 0.0);
            pose.m_252781_(Axis.f_252436_.m_252977_((float) (SupaFx.spinAge(id, partial) * SupaBlades.SPIN_SPEED)));
            for (int k = 0; k < SupaBlades.SPIN_BLADES; k++) {
                pose.m_85836_();
                pose.m_252781_(Axis.f_252436_.m_252977_(k * 360.0F / SupaBlades.SPIN_BLADES));
                pose.m_85837_(-SupaBlades.SPIN_OUT / 16.0, (k % 2 == 0 ? -1.5 : 1.5) / 16.0, 0.0);
                // the blade lies out level (its length along -x, out of the body), its edge leading round the spin
                pose.m_252781_(Axis.f_252403_.m_252977_(-90.0F));
                KatanaRenderer.steel(pose, buffers, STEEL, SupaBlades.ARM, light, SHINE, coat);
                pose.m_85849_();
            }
            pose.m_85849_();
        }
        for (int side = 0; side < 2; side++) {
            boolean right = side == 0;
            ModelPart arm = right ? model.f_102811_ : model.f_102812_;
            if ((mask & (SupaFx.ARMS | SupaFx.CLAWS | SupaFx.DRILLS)) != 0) {
                pose.m_85836_();
                arm.m_104299_(pose);
                if (!right) {
                    pose.m_85841_(-1.0F, 1.0F, 1.0F); // the left arm: the right's blades mirrored
                }
                arm(pose, buffers, light, mask, coat, age, right);
                pose.m_85849_();
            }
            if ((mask & SupaFx.SKATES) != 0) {
                pose.m_85836_();
                (right ? model.f_102813_ : model.f_102814_).m_104299_(pose);
                skate(pose, buffers, light, coat);
                pose.m_85849_();
            }
        }
    }

    @Override
    public boolean firstPerson(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float partial) {
        int id = player.m_19879_();
        int mask = SupaFx.mask(id) & (SupaFx.ARMS | SupaFx.CLAWS | SupaFx.DRILLS);
        if (mask == 0) {
            return false;
        }
        pose.m_85836_();
        // the game's own first-person right arm (LightArt.FIRST_PERSON_GRIP up to the arm's pivot): the blades on it as in third person
        for (int i = 0; i < 8; i++) {
            step(pose, LightArt.FIRST_PERSON_GRIP[i]);
        }
        arm(pose, buffers, light, mask, BladeCoatClient.progress(player), SupaFx.age(id, partial), true);
        pose.m_85849_();
        return false;
    }

    /** One of LightArt's grip steps (0 translate, 1 turn about x, 2 about y, 3 about z). */
    static void step(PoseStack pose, double[] s) {
        switch ((int) s[0]) {
            case 0 -> pose.m_85837_(s[1], s[2], s[3]);
            case 1 -> pose.m_252781_(Axis.f_252529_.m_252977_((float) s[1]));
            case 2 -> pose.m_252781_(Axis.f_252436_.m_252977_((float) s[1]));
            default -> pose.m_252781_(Axis.f_252403_.m_252977_((float) s[1]));
        }
    }

    static float[] grown(float[] box) {
        float g = SupaBlades.SKIN;
        return new float[] {box[0] - g, box[1] - g, box[2] - g, box[3] + g, box[4] + g, box[5] + g};
    }

    /** What the arm carries, in the right arm's frame (blocks, y down the arm): the blade, the finger blades, the drill. */
    static void arm(PoseStack pose, MultiBufferSource buffers, int light, int mask, float coat, double age, boolean right) {
        if ((mask & SupaFx.ARMS) != 0) {
            float[] at = SupaBlades.ARM_AT;
            pose.m_85836_();
            pose.m_85837_(at[0] / 16.0, at[1] / 16.0, at[2] / 16.0);
            pose.m_252781_(Axis.f_252436_.m_252977_(SupaBlades.ARM_TURN));
            KatanaRenderer.steel(pose, buffers, STEEL, SupaBlades.ARM, light, SHINE, coat);
            pose.m_85849_();
        }
        if ((mask & SupaFx.CLAWS) != 0) {
            for (int k = 0; k < SupaBlades.CLAW_X.length; k++) {
                pose.m_85836_();
                pose.m_85837_(SupaBlades.CLAW_X[k] / 16.0, SupaBlades.CLAW_Y / 16.0, 0.0);
                pose.m_252781_(Axis.f_252403_.m_252977_(SupaBlades.CLAW_FAN[k]));
                KatanaRenderer.steel(pose, buffers, STEEL, SupaBlades.CLAW, light, SHINE, coat);
                pose.m_85849_();
            }
        }
        if ((mask & SupaFx.DRILLS) != 0) {
            float[] axis = SupaBlades.DRILL_AXIS;
            float spin = (float) (age * SupaBlades.DRILL_SPIN) * (right ? 1.0F : -1.0F);
            for (int k = 0; k < SupaBlades.DRILL_BLADES; k++) {
                pose.m_85836_();
                pose.m_85837_(axis[0] / 16.0, axis[1] / 16.0, axis[2] / 16.0);
                pose.m_252781_(Axis.f_252436_.m_252977_(spin + k * 360.0F / SupaBlades.DRILL_BLADES));
                pose.m_85837_(-SupaBlades.DRILL_OUT / 16.0, 0.0, 0.0);
                pose.m_252781_(Axis.f_252403_.m_252977_(-SupaBlades.DRILL_LEAN)); // its tip leaning in (+x) toward the drill's point
                pose.m_252781_(Axis.f_252436_.m_252977_(SupaBlades.DRILL_TWIST)); // turned along itself: a screw's thread
                KatanaRenderer.steel(pose, buffers, STEEL, SupaBlades.DRILL, light, SHINE, coat);
                pose.m_85849_();
            }
        }
    }

    /** The skate blade under a foot, in the leg's frame: its heel end at SKATE_AT, running forward (-z), its edge down. */
    static void skate(PoseStack pose, MultiBufferSource buffers, int light, float coat) {
        float[] at = SupaBlades.SKATE_AT;
        pose.m_85836_();
        pose.m_85837_(at[0] / 16.0, at[1] / 16.0, at[2] / 16.0);
        // katana axes onto the leg's: its blade (+y) forward (-z), its edge (-x) down (+y)
        pose.m_252781_(Axis.f_252529_.m_252977_(-90.0F));
        pose.m_252781_(Axis.f_252436_.m_252977_(90.0F));
        KatanaRenderer.steel(pose, buffers, STEEL, SupaBlades.SKATE, light, SHINE, coat);
        pose.m_85849_();
    }
}
