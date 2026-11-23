package com.hexhaki.piece.client.pika;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.pika.LightArt;
import com.hexhaki.piece.server.PikaKit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;

/**
 * 2.7.8: the rebuilt Pika Pika no Mi on the client (in place of the original's LightClient, client/pika/PikaFx puts it in the
 * original's ClientKits): its moves for the panel and the log, Borsalino's stances (in the air he floats or flies as light; on the
 * ground he stands and walks with his hands in his pockets; with Ama no Murakumo out he holds the blade), and the sword of light drawn
 * in the right hand (third person, through the arm's own pose, and in first person).
 */
final class PikaClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.PIKA;
    }

    @Override
    public List<FruitMove> moves() {
        return PikaKit.MOVES;
    }

    @Override
    public String stance(AbstractClientPlayer player, CompoundTag state, long now) {
        // 3.1.0 (the owner, with a screenshot of the player stood with the hands in the pockets: "this pose presists, its not needed,
        // and it bugs like shit"): no stance at all with the Light fruit: the original plays a fruit's stance without end, and these held
        // the arms (and in the air the whole body), so the game's own punch, walk and every other animation could not move them. Only
        // the moves themselves animate; between them the body is the game's own.
        return null;
    }

    /** The sword of light in the right hand (the model's own arm pose), the leg alight while it gathers the light. */
    @Override
    public void renderBody(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model,
                           CompoundTag state, float partial, boolean inWorld) {
        if (!PikaFx.sword(player.m_19879_())) {
            return;
        }
        pose.m_85836_();
        model.f_102811_.m_104299_(pose);
        // 2.8.8: the fist, the blade turned out of its front, the guard just ahead of it: the hand closes on the grip
        apply(pose, LightArt.THIRD_PERSON_GRIP);
        draw(pose, buffers, (player.f_19797_ + partial) * 0.2);
        pose.m_85849_();
    }

    /** First person with the sword out: the blade from the hand at the lower right, up and ahead; the hand itself the game's. */
    @Override
    public boolean firstPerson(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float partial) {
        if (!PikaFx.sword(player.m_19879_()) || com.hexhaki.piece.client.FirstPersonLight.armDrawn(partial)) {
            return false; // 2.9.0: while it cuts, the arm and the sword are drawn posed (client/FirstPersonLight)
        }
        pose.m_85836_();
        // 2.8.8 (the owner: "the sword isnt being grabbed by the handle"): in the game's own first-person right hand, held by the grip
        apply(pose, LightArt.FIRST_PERSON_GRIP);
        draw(pose, buffers, (player.f_19797_ + partial) * 0.2);
        pose.m_85849_();
        return false;
    }

    /** Ama no Murakumo: a long rod of light (blocks from the guard to the point), its radius (LightArt's). */
    static final double BLADE_LENGTH = LightArt.BLADE_LENGTH;
    static final double BLADE_WIDTH = LightArt.BLADE_WIDTH;

    /** A grip's steps (LightArt.FIRST_PERSON_GRIP's form) on the pose. */
    static void apply(PoseStack pose, double[][] steps) {
        for (double[] step : steps) {
            switch ((int) step[0]) {
                case 0 -> pose.m_85837_(step[1], step[2], step[3]);
                case 1 -> pose.m_252781_(Axis.f_252529_.m_252977_((float) step[1]));
                case 2 -> pose.m_252781_(Axis.f_252436_.m_252977_((float) step[1]));
                default -> pose.m_252781_(Axis.f_252403_.m_252977_((float) step[1]));
            }
        }
    }

    static void draw(PoseStack pose, MultiBufferSource buffers, double shimmer) {
        PikaFx.Writer writer = new PikaFx.Writer();
        writer.buffer = buffers.m_6299_(RenderType.m_269166_());
        writer.matrix = pose.m_85850_().m_252922_();
        LightArt.blade(writer, BLADE_LENGTH, BLADE_WIDTH, 0.5 + 0.5 * Math.sin(shimmer));
        if (buffers instanceof MultiBufferSource.BufferSource source) {
            source.m_109912_(RenderType.m_269166_());
        }
    }
}
