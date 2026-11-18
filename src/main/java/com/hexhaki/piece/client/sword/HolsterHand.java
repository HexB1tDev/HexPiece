package com.hexhaki.piece.client.sword;

import com.hexhaki.client.render.ArmamentItemBuffers;
import com.hexhaki.piece.client.StanceLayer;
import com.hexhaki.piece.sword.BladePaths;
import com.hexhaki.piece.sword.DrawView;
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
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

/**
 * 2.5.4 (the owner: "the sword first person, if the sword is holstered the hand should be resting on the holster too ... the first
 * persons are meant to reflect exactly what the player is doing") and 2.5.6 (the owner: "the pull out animation is horrid, if you can
 * even call it that, the pull out animation needs to be really gorgeous to look at, sheath and unsheath both", "I mean the first
 * person"): the local player's katana in first person, every moment but a swing (playerAnimator draws those: the arm and the blade
 * as the cut has them), from {@link DrawView} (the same frames tools/draw_preview.py renders):
 * <ul>
 *   <li>sheathed at the hip: the handle and guard rising from the bottom left, the left hand resting on the end of the handle
 *       ({@link HolsterView}); standing ready, the thumb breaks the saya's seal now and then ({@link Koiguchi});</li>
 *   <li>the draw: the left hand to the saya's mouth, the right hand up to the handle, the seal broken with a glint, the long slide
 *       along the saya's curve as the saya is drawn back, the point leaving the mouth and the blade whipping across the view, settling
 *       into the hand;</li>
 *   <li>drawn between cuts: the blade up from the right hand, the right arm on its handle;</li>
 *   <li>the sheathing: the flick clean, the blade swung back and laid at the mouth, slid home slowly, the guard home, the hands back to
 *       rest;</li>
 *   <li>Yoru (on the back): the hands empty while it is there; drawn, it comes down over the right shoulder into the hand, and goes
 *       back up the same way.</li>
 * </ul>
 * The arms are the game's own, through Forge's arm event as the game draws its own ({@link #arm}: the player's skin, Armament's
 * coat and Ryou's aura with them), the blade black under Armament as in third person (never its saya), everything swaying and bobbing
 * with the view and rising as the sword is taken up, as the game's hands do. Anything in the off hand keeps the left hand. 2.5.8: all
 * of it with the hand empty too (the active katana at the hip); sheathed with the hand empty, the game's own right arm stays.
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
        ItemStack stack0 = event.getItemStack();
        boolean empty = stack0 == null || stack0.m_41619_();
        Katana held = KatanaItem.of(stack0);
        if (player == null || held == null && !empty) {
            return; // something else in the hand: the game's own
        }
        try {
            int id = player.m_19879_();
            // 2.5.8 (the owner: "First person is fucked, theres no unsheath animation"): a Swordsman fights with the hand EMPTY as
            // well (the active katana anywhere in the inventory, worn at the hip: SwordServer.scan), and until now only a katana held
            // as the item was drawn here: with the hand empty the game drew its bare arm through every draw, cut and sheathing
            Katana katana = SwordClient.katana(id);
            if (katana == null || held != null && held != katana) {
                return;
            }
            int anim = SwordClient.anim(id);
            if (SwordClient.swing(anim)) {
                return;
            }
            if (empty && anim == SwordAnims.NONE && !StanceLayer.swordsman(player)) {
                return; // the hand empty, out of the Swordsman's combat mode, the sword doing nothing: the game's own hand
            }
            float partial = event.getPartialTick();
            float ticks = SwordClient.ticks(player, partial);
            boolean slim = "slim".equals(player.m_108564_());
            float equip = event.getEquipProgress();
            float age = player.f_19797_ + partial;
            boolean back = katana.carry() == Katana.Carry.BACK;
            String name = SwordAnims.name(anim, katana);
            DrawView.Pose pose;
            float push = 0.0F;
            boolean rest = false;
            if (anim == SwordAnims.DRAW) {
                pose = back ? DrawView.backDraw(katana, slim, ticks, Math.max(0, BladePaths.drawTick(name)), equip, age)
                    : DrawView.draw(katana, slim, ticks, SwordAnims.duration(anim, katana), BladePaths.drawTick(name), equip, age);
            } else if (anim == SwordAnims.SHEATHE) {
                pose = back ? DrawView.backSheathe(katana, slim, ticks, Math.max(0, BladePaths.sheathTick(name)), equip, age)
                    : DrawView.sheathe(katana, slim, ticks, SwordAnims.duration(anim, katana), BladePaths.sheathTick(name), equip, age);
            } else if (SwordAnims.drawn(anim, ticks, katana)) {
                pose = DrawView.held(katana, slim, equip, age);
            } else if (back) {
                pose = new DrawView.Pose(null, null, null, null, 0.0F); // Yoru on the back: the hands are free
                rest = true;
            } else {
                pose = DrawView.holster(katana, slim, equip, age);
                double ready = anim == SwordAnims.NONE ? StanceLayer.swordReady(player, partial) : -1.0;
                push = ready < 0 ? 0.0F : Koiguchi.push(ready);
                rest = true;
            }
            // the sword sheathed and the hand empty: the right hand is free, and the game keeps drawing it (its punches, its digging)
            // beside the sheathed sword and the left hand on its hilt
            boolean free = empty && rest;
            if (!free) {
                event.setCanceled(true);
            }
            PoseStack stack = event.getPoseStack();
            MultiBufferSource buffers = event.getMultiBufferSource();
            int light = event.getPackedLight();
            if (pose.saya() != null) {
                stack.m_85836_();
                apply(stack, pose.saya());
                KatanaRenderer.draw(stack, buffers, katana, false, true, light, 0.0F);
                stack.m_85849_();
            }
            if (pose.blade() != null) {
                stack.m_85836_();
                apply(stack, pose.blade());
                if (push > 0.0F) {
                    SwordLayer.slide(stack, katana, push);
                }
                MultiBufferSource steel = SwordLayer.blackened(player) && katana.blackens() ? ArmamentItemBuffers.black(buffers) : buffers;
                KatanaRenderer.draw(stack, steel, katana, true, false, light, pose.shine());
                stack.m_85849_();
            }
            EntityRenderer<?> renderer = minecraft.m_91290_().m_114382_(player);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                if (pose.right() != null && !free) {
                    stack.m_85836_();
                    apply(stack, pose.right());
                    arm(playerRenderer, stack, buffers, light, player, true);
                    stack.m_85849_();
                }
                if (pose.left() != null && player.m_21206_().m_41619_()) {
                    stack.m_85836_();
                    apply(stack, pose.left());
                    arm(playerRenderer, stack, buffers, light, player, false);
                    stack.m_85849_();
                }
            }
        } catch (RuntimeException | LinkageError failure) {
            broken = true; // the game's own hand from the next frame
        }
    }

    /**
     * 2.5.8: an arm as the game draws its own first-person arm: Forge's arm event first (the original's Armament answers it with the
     * Haki coat, Ryou's aura and the fist's glow, a Gear arm with its own), the plain arm if no one took it.
     */
    static void arm(PlayerRenderer renderer, PoseStack stack, MultiBufferSource buffers, int light, LocalPlayer player, boolean right) {
        if (ForgeHooksClient.renderSpecificFirstPersonArm(stack, buffers, light, player, right ? HumanoidArm.RIGHT : HumanoidArm.LEFT)) {
            return;
        }
        if (right) {
            renderer.m_117770_(stack, buffers, light, player);
        } else {
            renderer.m_117813_(stack, buffers, light, player);
        }
    }

    /** {@link HolsterView}'s and {@link DrawView}'s steps on the game's pose. */
    static void apply(PoseStack pose, float[][] steps) {
        for (float[] s : steps) {
            switch ((int) s[0]) {
                case HolsterView.MOVE -> pose.m_252880_(s[1], s[2], s[3]);
                case HolsterView.TURN_X -> pose.m_252781_(Axis.f_252529_.m_252977_(s[1]));
                case HolsterView.TURN_Y -> pose.m_252781_(Axis.f_252436_.m_252977_(s[1]));
                case HolsterView.TURN_Z -> pose.m_252781_(Axis.f_252403_.m_252977_(s[1]));
                case DrawView.ROTATE -> pose.m_252781_(new Quaternionf().rotationAxis((float) Math.toRadians(s[1]), s[2], s[3], s[4]));
                default -> {
                }
            }
        }
    }
}
