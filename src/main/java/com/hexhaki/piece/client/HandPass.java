package com.hexhaki.piece.client;

import com.hexhaki.piece.dragon.FireMesh;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 3.1.8 (the owner, with two first-person screenshots of the Gura right click charging, the bubble left of the middle of the view and the
 * fist at the lower right: "whenever a sphere is like charging, it should always be on the fist, even in the first person. Currently,
 * it's not. It's never currently standing on the fist."). Why: one's own first-person charge (the Gura bubble, the Magma fist swelling,
 * the Light laser's gathering light) was drawn in the world, at the fist's place in the hand's view space turned into the world; but the
 * game draws its hand in a pass of its own, with its own field of view (70 degrees, whatever the player's is), its own bobbing and sway:
 * at any other field of view the same point lands elsewhere on the screen. Now those effects are drawn in the hand's pass itself, in the
 * very space and projection the arm is drawn in (the main hand's pose, taken as it is drawn, with client/FirstPersonHand's move on it),
 * right after the arm, so they sit on the fist whatever the field of view, the bobbing or the move.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class HandPass {
    private static Matrix4f hand;

    private HandPass() {
    }

    /** Whether the local player is in first person with a charge of their own to draw on the fist. */
    static boolean wanted(LocalPlayer self) {
        int id = self.m_19879_();
        return com.hexhaki.piece.client.gura.GuraFx.handWanted(id) || com.hexhaki.piece.client.magu.MaguFx.handWanted(id)
            || com.hexhaki.piece.client.pika.PikaFx.handWanted(id) || com.hexhaki.piece.client.hie.HieFx.handWanted(id);
    }

    /** The main hand's pose as the game draws it (after FirstPersonHand's move); after the off hand, the charges on it. */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void hand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer self = minecraft.f_91074_;
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            hand = null;
            if (self != null && minecraft.f_91066_.m_92176_() == CameraType.FIRST_PERSON && wanted(self)) {
                hand = new Matrix4f(event.getPoseStack().m_85850_().m_252922_());
            }
            return;
        }
        if (hand == null || self == null) {
            return;
        }
        Matrix4f matrix = hand;
        hand = null;
        try {
            MultiBufferSource buffers = event.getMultiBufferSource();
            if (buffers instanceof MultiBufferSource.BufferSource source) {
                source.m_109911_(); // the arm first: the charge is seen round it, never under it
            }
            RenderType type = RenderType.m_269166_();
            SINK.matrix = matrix;
            SINK.buffer = buffers.m_6299_(type);
            float partial = event.getPartialTick();
            int id = self.m_19879_();
            com.hexhaki.piece.client.gura.GuraFx.handPass(SINK, id, partial);
            com.hexhaki.piece.client.magu.MaguFx.handPass(SINK, id, partial);
            com.hexhaki.piece.client.pika.PikaFx.handPass(SINK, id, partial);
            com.hexhaki.piece.client.hie.HieFx.handPass(SINK, id, partial); // 3.1.9: the Ice Ball's orb, the ice fist
            FireMesh.palette(FireMesh.BAND);
            if (buffers instanceof MultiBufferSource.BufferSource source) {
                source.m_109912_(type);
            }
        } catch (RuntimeException failure) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: a charge on the first-person fist could not be drawn", failure);
        } finally {
            SINK.buffer = null;
        }
    }

    /** FireMesh's quads into the hand pass's buffer, through the hand's pose. */
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
}
