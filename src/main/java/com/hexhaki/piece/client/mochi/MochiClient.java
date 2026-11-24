package com.hexhaki.piece.client.mochi;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.client.sword.KatanaRenderer;
import com.hexhaki.piece.mochi.MochiV2;
import com.hexhaki.piece.pika.LightArt;
import com.hexhaki.piece.server.MochiKit;
import com.hexhaki.piece.sword.Katana;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;

/**
 * 3.5.5: the Mochi Mochi no Mi on the client (client/mochi/MochiFx puts it in the original's ClientKits): its moves for the panel and
 * the log; while one's own Awakening is on, the awakened set in Z to B's places (the owner: "M toggles a mode"). No stance. 3.6.4: while
 * a body is awakened it holds Mogura in its right hand (the reference's DoughV2: the trident always in hand), drawn on the arm itself
 * as Supa's blades are, in third and first person.
 */
final class MochiClient implements ClientKit {
    /** Mogura's dark iron shaft and its pale head (the reference's), a little coated toward the Haki's dark violet. */
    static final Katana SHAFT = Katana.YORU;
    static final Katana HEAD = Katana.SILVER_CRANE;
    static final float SHINE = 0.6F;
    static final float SHAFT_COAT = 0.7F;
    static final float HEAD_COAT = 0.25F;

    @Override
    public Fruit fruit() {
        return Fruit.MOCHI;
    }

    @Override
    public List<FruitMove> moves() {
        Minecraft minecraft = Minecraft.m_91087_();
        return minecraft.f_91074_ != null && MochiFx.awakened(minecraft.f_91074_.m_19879_()) ? MochiKit.AWAKENED : MochiKit.MOVES;
    }

    @Override
    public String stance(AbstractClientPlayer player, CompoundTag state, long now) {
        return null;
    }

    @Override
    public void renderBody(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model,
                           CompoundTag state, float partial, boolean inWorld) {
        if (!MochiFx.awakened(player.m_19879_())) {
            return;
        }
        pose.m_85836_();
        model.f_102811_.m_104299_(pose);
        mogura(pose, buffers, light);
        pose.m_85849_();
    }

    @Override
    public boolean firstPerson(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float partial) {
        if (!MochiFx.awakened(player.m_19879_())) {
            return false;
        }
        pose.m_85836_();
        // the game's own first-person right arm (LightArt.FIRST_PERSON_GRIP up to the arm's pivot): Mogura on it as in third person
        for (int i = 0; i < 8; i++) {
            step(pose, LightArt.FIRST_PERSON_GRIP[i]);
        }
        mogura(pose, buffers, light);
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

    /**
     * Mogura in the right arm's frame (MochiV2.GRIP_*: the fist, its shaft out of the fist's front tipped MochiV2.TILT degrees down the
     * arm; the trails in MochiFx follow the same points): the shaft, the crossbar and three prongs, the middle one longest (model px).
     */
    static void mogura(PoseStack pose, MultiBufferSource buffers, int light) {
        pose.m_85837_(MochiV2.GRIP_X / 16.0, MochiV2.GRIP_Y / 16.0, MochiV2.GRIP_Z / 16.0);
        // the shaft along +y from here: turned about x so +y runs out of the fist's front (-z), MochiV2.TILT down the arm
        pose.m_252781_(Axis.f_252529_.m_252977_((float) (MochiV2.TILT - 90.0)));
        KatanaRenderer.steelBox(pose, buffers, SHAFT, "mogura_shaft", SHAFT_BOX, light, SHINE, SHAFT_COAT);
        for (int i = 0; i < HEAD_BOXES.length; i++) {
            KatanaRenderer.steelBox(pose, buffers, HEAD, "mogura_head" + i, HEAD_BOXES[i], light, SHINE, HEAD_COAT);
        }
    }

    static final float BACK = (float) MochiV2.BACK;
    static final float CROSS = (float) MochiV2.CROSS;
    static final float TIP = (float) MochiV2.TIP;
    static final float[] SHAFT_BOX = {-0.6F, -BACK, -0.6F, 0.6F, CROSS, 0.6F};
    static final float[][] HEAD_BOXES = {
        {-4.6F, CROSS - 0.6F, -0.6F, 4.6F, CROSS + 1.0F, 0.6F},
        {-0.55F, CROSS + 1.0F, -0.55F, 0.55F, TIP, 0.55F},
        {-4.6F, CROSS + 1.0F, -0.5F, -3.6F, TIP - 5.0F, 0.5F},
        {3.6F, CROSS + 1.0F, -0.5F, 4.6F, TIP - 5.0F, 0.5F},
    };
}
