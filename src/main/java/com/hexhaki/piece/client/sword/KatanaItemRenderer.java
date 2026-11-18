package com.hexhaki.piece.client.sword;

import com.hexhaki.piece.sword.Katana;
import com.hexhaki.piece.sword.KatanaItem;
import com.hexhaki.piece.sword.SwordAnims;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * 2.1.4: a katana item as it is seen outside the hand: the owner's whole mesh, the blade in its saya, on the ground (spinning as items
 * do) and in an item frame. In the inventory its icon is drawn instead (its model's GUI view is the icon, Forge's separate_transforms;
 * build.py writes the models). In a hand it draws nothing at all: the player's sword layer ({@link SwordLayer}) shows the active katana,
 * sheathed at the hip or drawn in the hand, so a katana is never seen twice. The one exception is the game's own first-person view of a
 * katana held without a sword move playing: there the hand holds the bare blade while the sword is drawn (2.4.5). 2.5.4: while it is
 * sheathed, {@link HolsterHand} shows it at the hip with the left hand resting on it instead; the sheathed sword carried in the hand
 * is left only for a katana that is not the player's active sword. Armament Haki blackens the sword there, never its saya (2.2.6).
 */
public final class KatanaItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static KatanaItemRenderer instance;

    KatanaItemRenderer() {
        super(Minecraft.m_91087_().m_167982_(), Minecraft.m_91087_().m_167973_());
    }

    static KatanaItemRenderer get() {
        if (instance == null) {
            instance = new KatanaItemRenderer();
        }
        return instance;
    }

    /** Called from {@code KatanaItem.initializeClient}: its renderer, and an empty-handed arm pose (the sword is at the hip). */
    public static void register(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return get();
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.EMPTY;
            }
        });
    }

    @Override
    public void m_108829_(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Katana katana = KatanaItem.of(stack);
        if (katana == null) {
            return;
        }
        try {
            if (context == ItemDisplayContext.GROUND) {
                sheathed(pose, buffers, katana, light, 0.62F, 0.0F);
            } else if (context == ItemDisplayContext.FIXED || context == ItemDisplayContext.NONE) {
                sheathed(pose, buffers, katana, light, 0.92F, 0.0F);
            } else if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                held(pose, buffers, katana, light, context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
            }
            // the third-person hands, the head and the inventory: nothing here (the sword layer, or the icon)
        } catch (RuntimeException failure) {
            // a katana that cannot be drawn never stops the frame
        }
    }

    /** The blade in its saya across the item's cube, centred, {@code fill} of the diagonal long. */
    static void sheathed(PoseStack pose, MultiBufferSource buffers, Katana katana, int light, float fill, float lift) {
        pose.m_85836_();
        pose.m_252880_(0.5F, 0.5F + lift, 0.5F);
        pose.m_252781_(Axis.f_252403_.m_252977_(-45.0F));
        float length = katana.carry() == Katana.Carry.BACK ? KatanaRenderer.YORU_LENGTH : KatanaRenderer.LENGTH;
        float scale = fill * 1.41F / (length * KatanaRenderer.SCALE);
        pose.m_85841_(scale, scale, scale);
        pose.m_252880_(0.0F, -KatanaRenderer.MIDDLE * KatanaRenderer.SCALE, 0.0F);
        KatanaRenderer.draw(pose, buffers, katana, true, true, light, 0.0F);
        pose.m_85849_();
    }

    /** The game's own first-person hand holding the katana sheathed, upright and a little forward, as a sword is held there. */
    static void held(PoseStack pose, MultiBufferSource buffers, Katana katana, int light, boolean left) {
        pose.m_85836_();
        pose.m_252880_(0.5F, 0.5F, 0.5F);
        pose.m_252781_(Axis.f_252436_.m_252977_(left ? 90.0F : -90.0F));
        pose.m_252781_(Axis.f_252403_.m_252977_(25.0F));
        pose.m_252880_(1.13F / 16.0F, 3.2F / 16.0F, 1.13F / 16.0F);
        pose.m_85841_(0.68F, 0.68F, 0.68F);
        // the sprite's diagonal: the handle in the fist at the lower corner, the saya up and away
        pose.m_252781_(Axis.f_252403_.m_252977_(-45.0F));
        pose.m_252880_(0.0F, -0.36F, 0.0F);
        pose.m_252880_(0.0F, -KatanaRenderer.GRIP * KatanaRenderer.SCALE, 0.0F);
        // 2.2.6 (the owner: "the sheath of the blade is never to be black when haki is on, it's just the sword itself"): the original
        // blackens whatever an Armament user holds by handing this renderer black buffers (its ItemInHandRendererMixin); the sword takes
        // them, the saya is drawn into the game's own buffers (the ones the first-person hand is drawn with), in its own colours
        MultiBufferSource own = Minecraft.m_91087_().m_91269_().m_110104_();
        // 2.4.5 (the owner: "Gryphon in first person when unsheathed still shows has having the sheath on when it doesnt"): while the
        // sword is out (the guard, the idle, a draw once the blade has left the saya) the hand holds the bare blade, glinting as it does
        // in third person; the saya is drawn only while the blade is in it. Every blade the same.
        boolean bare = drawnNow(katana);
        // 2.2.7: Yoru is never blackened either
        KatanaRenderer.draw(pose, katana.blackens() ? buffers : own, katana, true, false, light, bare ? (katana.blackens() ? 1.0F : 0.45F) : 0.0F);
        if (!bare) {
            KatanaRenderer.draw(pose, own, katana, false, true, light, 0.0F);
        }
        pose.m_85849_();
    }

    /**
     * 2.4.5: is the local player's sword out of its saya this frame ({@link SwordAnims#drawn}, as {@link SwordLayer} asks for every
     * player in third person)? Only the first-person hand asks: that hand is always the local player's.
     */
    static boolean drawnNow(Katana katana) {
        Minecraft minecraft = Minecraft.m_91087_();
        net.minecraft.client.player.LocalPlayer player = minecraft.f_91074_;
        if (player == null) {
            return false;
        }
        int id = player.m_19879_();
        if (SwordClient.katana(id) != katana) {
            return false;
        }
        return SwordAnims.drawn(SwordClient.anim(id), SwordClient.ticks(player, minecraft.m_91296_()), katana);
    }
}
