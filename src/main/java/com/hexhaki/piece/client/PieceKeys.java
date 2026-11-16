package com.hexhaki.piece.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.1: the fighting style's special keys (Y and U by default, free in the original's layout: R V G H J K L M P X B Z C are its own).
 * M1 and the right click with an empty hand are the string and the signature ({@link StyleInput}).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PieceKeys {
    public static final String CATEGORY = "key.categories.hexpiece";
    public static final KeyMapping SPECIAL_A = new KeyMapping("key.hexpiece.special_a", InputConstants.Type.KEYSYM, 89, CATEGORY);
    public static final KeyMapping SPECIAL_B = new KeyMapping("key.hexpiece.special_b", InputConstants.Type.KEYSYM, 85, CATEGORY);

    private PieceKeys() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SPECIAL_A);
        event.register(SPECIAL_B);
    }
}
