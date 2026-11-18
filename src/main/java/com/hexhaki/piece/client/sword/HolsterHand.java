package com.hexhaki.piece.client.sword;

import com.hexhaki.client.render.ArmamentItemBuffers;
import com.hexhaki.piece.client.StanceLayer;
import com.hexhaki.piece.sword.HolsterView;
import com.hexhaki.piece.sword.Katana;
import com.hexhaki.piece.sword.KatanaItem;
import com.hexhaki.piece.sword.Koiguchi;
import com.hexhaki.piece.sword.SwordAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.5.4 (the owner: "the sword first person, if the sword is holstered the hand should be resting on the holster too, not just on the
 * players hand, the first persons are meant to reflect exactly what the player is doing"): while the local player's katana is in its
 * saya at the hip, the game's first-person hand no longer carries it like an item ({@link KatanaItemRenderer#held}): the view shows the
 * sword where it is, its handle and guard rising from the bottom left with the saya's mouth under the guard, and the left hand resting
 * on the end of the handle ({@link HolsterView}: the same steps as tools/holster_preview.py renders). The arm is the game's own
 * ({@code PlayerRenderer.renderLeftHand}: the player's skin, Armament's black and Ryou's aura with it); the hilt black under Armament as
 * in third person; standing ready, the thumb breaks the saya's seal now and then as it does in third person ({@link Koiguchi}). It
 * sways and bobs with the view and rises as the sword is taken up, as the game's hands do.
 * <p>
 * Left to the game and to the sword's animations: a drawn blade (the hand holds it, {@link KatanaItemRenderer#held}), a swing
 * (playerAnimator draws the arm and the blade), and Yoru, carried on the back: there the hands are empty, and nothing is drawn.
 * Anything in the off hand keeps the left hand (only the hilt is shown then).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class HolsterHand {
    /** Something failed: the game's own hand from now on. */
    private static boolean broken;

    private HolsterHand() {
    }

    @SubscribeEvent
    public static void holsterHand(RenderHandEvent event) {
        if (broken || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        LocalPlayer player = minecraft.f_91074_;
        Katana katana = KatanaItem.of(event.getItemStack());
        if (player == null || katana == null) {
            return;
        }
        try {
            int id = player.m_19879_();
            if (SwordClient.katana(id) != katana) {
                return;
            }
            int anim = SwordClient.anim(id);
            float partial = event.getPartialTick();
            if (SwordClient.swing(anim) || SwordAnims.drawn(anim, SwordClient.ticks(player, partial), katana)) {
                return;
            }
            event.setCanceled(true);
            if (katana.carry() == Katana.Carry.BACK) {
                return; // Yoru is on the back: the hands are free
            }
            PoseStack pose = event.getPoseStack();
            MultiBufferSource buffers = event.getMultiBufferSource();
            int light = event.getPackedLight();
            float equip = event.getEquipProgress();
            float age = player.f_19797_ + partial;
            pose.m_85836_();
            apply(pose, HolsterView.katana(equip, age));
            KatanaRenderer.draw(pose, buffers, katana, false, true, light, 0.0F);
            double ready = anim == SwordAnims.NONE ? StanceLayer.swordReady(player, partial) : -1.0;
            float push = ready < 0 ? 0.0F : Koiguchi.push(ready);
            if (push > 0.0F) {
                SwordLayer.slide(pose, katana, push);
            }
            MultiBufferSource hilt = SwordLayer.blackened(player) && katana.blackens() ? ArmamentItemBuffers.black(buffers) : buffers;
            KatanaRenderer.draw(pose, hilt, katana, true, false, light, 0.0F);
            pose.m_85849_();
            if (player.m_21206_().m_41619_()) {
                EntityRenderer<?> renderer = minecraft.m_91290_().m_114382_(player);
                if (renderer instanceof PlayerRenderer playerRenderer) {
                    pose.m_85836_();
                    apply(pose, HolsterView.arm("slim".equals(player.m_108564_()), equip, age));
                    playerRenderer.m_117813_(pose, buffers, light, player);
                    pose.m_85849_();
                }
            }
        } catch (RuntimeException | LinkageError failure) {
            broken = true; // the game's own hand from the next frame
        }
    }

    /** {@link HolsterView}'s steps on the game's pose. */
    static void apply(PoseStack pose, float[][] steps) {
        for (float[] s : steps) {
            switch ((int) s[0]) {
                case HolsterView.MOVE -> pose.m_252880_(s[1], s[2], s[3]);
                case HolsterView.TURN_X -> pose.m_252781_(Axis.f_252529_.m_252977_(s[1]));
                case HolsterView.TURN_Y -> pose.m_252781_(Axis.f_252436_.m_252977_(s[1]));
                default -> pose.m_252781_(Axis.f_252403_.m_252977_(s[1]));
            }
        }
    }
}
