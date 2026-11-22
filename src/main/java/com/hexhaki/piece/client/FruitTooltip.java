package com.hexhaki.piece.client;

import com.hexhaki.piece.FruitRanks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 3.2.0: a fruit's rank (FruitRanks) on the line under its name in its tooltip: its stars and its name, in the rank's colour. */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class FruitTooltip {
    private FruitTooltip() {
    }

    @SubscribeEvent
    public static void rank(ItemTooltipEvent event) {
        Item item = event.getItemStack().m_41720_();
        FruitRanks.Rank rank = null;
        if (item instanceof com.hexhaki.fruit.FruitItem fruit) {
            rank = FruitRanks.of(fruit.fruit);
        } else if (item == com.hexhaki.registry.ModItems.NIKA_FRUIT.get()) {
            rank = FruitRanks.NIKA;
        }
        if (rank != null && !event.getToolTip().isEmpty()) {
            event.getToolTip().add(1, Component.m_237113_(rank.line()));
        }
    }
}
