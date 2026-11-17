package com.hexhaki.piece.client.armor;

import com.hexhaki.piece.armor.PieceArmor;
import java.util.function.Supplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * 2.3.1: an armor piece's client hook. The pack's own ({@link OnePieceArmorExtensions}, translated from the owner's zip by build.py) is
 * made the first time the piece is drawn: Forge hands an item its hook from inside Item's constructor, before the item knows which piece
 * it is.
 */
public final class ArmorClient implements IClientItemExtensions {
    private final Supplier<PieceArmor> piece;
    private OnePieceArmorExtensions pack;

    public ArmorClient(Supplier<PieceArmor> piece) {
        this.piece = piece;
    }

    @Override
    public Model getGenericArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        if (this.pack == null) {
            this.pack = new OnePieceArmorExtensions(OnePieceArmorModel.Style.valueOf(this.piece.get().style()));
        }
        return this.pack.getGenericArmorModel(entity, stack, slot, original);
    }
}
