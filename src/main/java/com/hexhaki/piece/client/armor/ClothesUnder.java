package com.hexhaki.piece.client.armor;

import com.hexhaki.piece.armor.PieceArmorItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.9.5 (the owner: "also the other armors...", a picture of the Marine outfit worn: pale see-through boxes standing out round the
 * shoulders, the arms and the head): the owner's clothes (client/armor/ClothesModel) are cloth on the body, thinner than the skin's own
 * outer layer (its jacket, sleeves, trouser legs and hat, a quarter to half a pixel out), so a skin's outer layer showed round them,
 * its see-through pixels as ghostly boxes. Under a piece of clothes that layer is hidden, as a vanilla armor's thick boxes hide it: the
 * jacket and both sleeves under a shirt or a coat, the trouser legs under trousers, the hat under a cap or a hat. The renderer shows every
 * layer again before each frame (PlayerRenderer.setModelProperties runs before this event), so nothing is put back.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class ClothesUnder {
    private ClothesUnder() {
    }

    @SubscribeEvent
    public static void hide(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!(event.getRenderer().m_7200_() instanceof PlayerModel<?> model)) {
            return;
        }
        if (player.m_6844_(EquipmentSlot.CHEST).m_41720_() instanceof PieceArmorItem) {
            model.f_103378_.f_104207_ = false;
            model.f_103375_.f_104207_ = false;
            model.f_103374_.f_104207_ = false;
        }
        if (player.m_6844_(EquipmentSlot.LEGS).m_41720_() instanceof PieceArmorItem) {
            model.f_103377_.f_104207_ = false;
            model.f_103376_.f_104207_ = false;
        }
        if (player.m_6844_(EquipmentSlot.HEAD).m_41720_() instanceof PieceArmorItem) {
            model.f_102809_.f_104207_ = false;
        }
    }
}
