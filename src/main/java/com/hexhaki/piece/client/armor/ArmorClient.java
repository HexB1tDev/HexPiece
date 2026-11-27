package com.hexhaki.piece.client.armor;

import com.hexhaki.piece.armor.PieceArmor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * 2.3.1: an armor piece's client hook, as the pack's own ({@link OnePieceArmorExtensions}, translated from the owner's zip by build.py)
 * does it: a model for each fit, made the first time it is drawn (Forge hands an item its hook from inside Item's constructor, before
 * the item knows which piece it is), the wearer's pose copied in each frame. 2.3.2: the slim fit for the pirates and marines too (all Alex, as the pack's INTEGRATION.md says to add for a narrow-armed NPC),
 * so the fit is chosen here: a player's by its model, a sea fighter slim, anything else the standard fit. 3.8.9: the owner's clothes
 * replacements drawn instead ({@link ClothesModel}).
 */
public final class ArmorClient implements IClientItemExtensions {
    private final Supplier<PieceArmor> piece;
    private final Map<Boolean, ClothesModel> models = new HashMap<>();

    public ArmorClient(Supplier<PieceArmor> piece) {
        this.piece = piece;
    }

    @Override
    public Model getGenericArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        boolean slim = entity instanceof AbstractClientPlayer player ? "slim".equals(player.m_108564_())
            : entity instanceof com.hexhaki.piece.npc.SeaFighter;
        // 3.8.9: the owner's clothes replacements (client/armor/ClothesModel: their own meshes; the pack's box models no longer drawn)
        ClothesModel model = this.models.computeIfAbsent(slim, fit -> new ClothesModel(this.piece.get(), fit));
        model.prepare(original, entity);
        return model;
    }
}
