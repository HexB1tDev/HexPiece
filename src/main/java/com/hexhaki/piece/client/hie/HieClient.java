package com.hexhaki.piece.client.hie;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.hie.HieArt;
import com.hexhaki.piece.pika.LightArt;
import com.hexhaki.piece.server.HieKit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;

/**
 * 3.1.9: the Hie Hie no Mi on the client (client/hie/HieFx puts it in the original's ClientKits): its moves for the panel and the log, and
 * the Ice Saber in the right hand (third person through the arm's own pose, and in the game's own first-person hand), held by its grip as
 * the Light fruit's sword is (LightArt's grips). No stance at all (the owner took the Light fruit's away in 3.1.0).
 */
final class HieClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.HIE;
    }

    @Override
    public List<FruitMove> moves() {
        return HieKit.MOVES;
    }

    @Override
    public String stance(AbstractClientPlayer player, CompoundTag state, long now) {
        return null;
    }

    @Override
    public void renderBody(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model,
                           CompoundTag state, float partial, boolean invisible) {
        if (!HieFx.saber(player.m_19879_())) {
            return;
        }
        pose.m_85836_();
        model.f_102811_.m_104299_(pose);
        apply(pose, LightArt.THIRD_PERSON_GRIP);
        draw(pose, buffers, (player.f_19797_ + partial) * 0.02);
        pose.m_85849_();
    }

    @Override
    public boolean firstPerson(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float partial) {
        if (!HieFx.saber(player.m_19879_())) {
            return false;
        }
        pose.m_85836_();
        apply(pose, LightArt.FIRST_PERSON_GRIP);
        draw(pose, buffers, (player.f_19797_ + partial) * 0.02);
        pose.m_85849_();
        return false;
    }

    /** A grip's steps (LightArt's form: 0 translate, 1 turn about x, 2 about y, 3 about z) on the pose. */
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
        HieFx.Writer writer = new HieFx.Writer();
        writer.buffer = buffers.m_6299_(RenderType.m_269166_());
        writer.matrix = pose.m_85850_().m_252922_();
        HieArt.saber(writer, HieArt.SABER_LENGTH, shimmer % 1.0);
        if (buffers instanceof MultiBufferSource.BufferSource source) {
            source.m_109912_(RenderType.m_269166_());
        }
    }
}
