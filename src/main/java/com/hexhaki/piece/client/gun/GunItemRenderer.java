package com.hexhaki.piece.client.gun;

import com.hexhaki.piece.gun.Gun;
import com.hexhaki.piece.gun.GunItem;
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
 * 3.8.8: a gun item as it is seen outside the player's own gun layer: the owner's whole mesh on the ground (spinning as items do) and
 * in an item frame, the icon in the inventory (its model's GUI view: build.py writes the models), and in the game's own first-person
 * hand the gun held out ahead (one's own gun is otherwise seen in third person, client/sword/SwordCamera). In a third-person hand it
 * draws nothing: {@link GunLayer} draws it there as the animations move it, so it is never seen twice.
 */
public final class GunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static GunItemRenderer instance;

    GunItemRenderer() {
        super(Minecraft.m_91087_().m_167982_(), Minecraft.m_91087_().m_167973_());
    }

    static GunItemRenderer get() {
        if (instance == null) {
            instance = new GunItemRenderer();
        }
        return instance;
    }

    /** Called from {@code GunItem.initializeClient}: its renderer, and an empty-handed arm pose (the clips pose the arm). */
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
        Gun gun = GunItem.of(stack);
        if (gun == null) {
            return;
        }
        try {
            if (context == ItemDisplayContext.GROUND || context == ItemDisplayContext.FIXED || context == ItemDisplayContext.NONE) {
                // side on, centred in the item's cube, its length across it
                pose.m_85836_();
                pose.m_252880_(0.5F, 0.5F, 0.5F);
                float fill = context == ItemDisplayContext.GROUND ? 0.8F : 1.1F;
                pose.m_85841_(fill, fill, fill);
                pose.m_252781_(Axis.f_252436_.m_252977_(90.0F));
                pose.m_252880_(0.0F, -0.8F * Gun.SCALE, 6.7F * Gun.SCALE);
                GunRenderer.draw(pose, buffers, gun, light);
                pose.m_85849_();
            } else if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                // held out ahead, the barrel forward, a little in towards the middle of the view
                pose.m_85836_();
                pose.m_252880_(0.5F, 0.55F, 0.5F);
                pose.m_252781_(Axis.f_252436_.m_252977_(context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -6.0F : 6.0F));
                pose.m_85841_(1.15F, 1.15F, 1.15F);
                pose.m_252880_(0.0F, 0.0F, 2.0F * Gun.SCALE);
                GunRenderer.draw(pose, buffers, gun, light);
                pose.m_85849_();
            }
        } catch (RuntimeException failure) {
            // a gun that cannot be drawn never stops the frame
        }
    }
}
