// Compile-only API stub (Minecraft 1.20.1 / Forge 47 / PlayerAnimator). Never packaged; see verify/api-allowlist.txt.
package net.minecraft.world.item;

public class Item implements net.minecraft.world.level.ItemLike {
    Item() {
    }

    public Item(Properties properties) {
    }

    /** 4.1.20: Forge's (Item.java.patch: called from the constructor on a client only), its own name. */
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) { }
    /** 2.3.1: getArmorTexture (Forge's IForgeItem, its own name): an armor item's texture, or null for the material's */
    public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.EquipmentSlot slot, String type) {
        return null;
    }
    /** getUseAnimation (1.3.40) */ public UseAnim m_6164_(ItemStack stack) { throw new UnsupportedOperationException(); }
    /** getUseDuration (1.3.40) */ public int m_8105_(ItemStack stack) { throw new UnsupportedOperationException(); }
    /** appendHoverText (2.1.4; the original's FruitItem overrides it) */
    public void m_7373_(ItemStack stack, net.minecraft.world.level.Level level, java.util.List<net.minecraft.network.chat.Component> lines,
                        TooltipFlag flag) { }
    public static class Properties {
        public Properties() {
        }

        /** food */ public Properties m_41489_(net.minecraft.world.food.FoodProperties food) { throw new UnsupportedOperationException(); }
        /** stacksTo (1.3.2) */ public Properties m_41487_(int count) { throw new UnsupportedOperationException(); }
        /** rarity (2.1.4; the original's ModItems) */ public Properties m_41497_(Rarity rarity) { throw new UnsupportedOperationException(); }
        /** craftRemainder (1.3.40) */ public Properties m_41495_(Item remainder) { throw new UnsupportedOperationException(); }
    }
}
