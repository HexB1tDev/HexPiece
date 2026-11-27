package com.hexhaki.piece.armor;

import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;

/**
 * 2.3.1: one piece of the owner's One Piece armor, worn in its slot. Iron's protection (as HexGhoul gives the owner's outfits); drawn by
 * the pack's own model through Forge's armor-model hook ({@code client/armor/ArmorClient}) with the pack's texture.
 */
public final class PieceArmorItem extends ArmorItem {
    private final PieceArmor piece;

    public PieceArmorItem(PieceArmor piece) {
        super(ArmorMaterials.IRON, type(piece.slot()), new Item.Properties());
        this.piece = piece;
    }

    public PieceArmor piece() {
        return this.piece;
    }

    static ArmorItem.Type type(PieceArmor.Slot slot) {
        return switch (slot) {
            case HEAD -> ArmorItem.Type.HELMET;
            case CHEST -> ArmorItem.Type.CHESTPLATE;
            case LEGS -> ArmorItem.Type.LEGGINGS;
            case FEET -> ArmorItem.Type.BOOTS;
        };
    }

    /**
     * Forge calls this from Item's constructor (on a client only), before {@link #piece} is set: the hook asks for the piece when it
     * first draws it.
     */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> consumer.accept(new com.hexhaki.piece.client.armor.ArmorClient(this::piece)));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        // 3.8.9: the owner's clothes replacements: one atlas for every piece (tools/clothes_bake.py)
        return PieceArmor.CLOTHES_TEXTURE;
    }
}
