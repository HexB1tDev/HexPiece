package com.hexhaki.piece.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.1.1: the fighting style's special keys. 2.7.1 (the owner: "All keys of the mod (but haki) will now be the following Z, X, C, V, B,
 * N, M. R For switching modes / Haki"): Z and X (were Y and U), the specials in combat mode; in fruit mode the same keys are the fruit's
 * quick slots (PiecePatch.keyDefaults), which only answer in fruit mode (PieceHooks.fruitKeysActive), so one press is never both.
 * M1 and the right click with an empty hand are the string and the signature ({@link StyleInput}).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PieceKeys {
    public static final String CATEGORY = "key.categories.hexpiece";
    public static final KeyMapping SPECIAL_A = new KeyMapping("key.hexpiece.special_a", InputConstants.Type.KEYSYM, 90, CATEGORY);
    public static final KeyMapping SPECIAL_B = new KeyMapping("key.hexpiece.special_b", InputConstants.Type.KEYSYM, 88, CATEGORY);
    /** 2.8.4: the three extra style moves (the show's own), C, V and B in combat mode. */
    public static final KeyMapping EXTRA_C = new KeyMapping("key.hexpiece.extra_c", InputConstants.Type.KEYSYM, 67, CATEGORY);
    public static final KeyMapping EXTRA_V = new KeyMapping("key.hexpiece.extra_v", InputConstants.Type.KEYSYM, 86, CATEGORY);
    public static final KeyMapping EXTRA_B = new KeyMapping("key.hexpiece.extra_b", InputConstants.Type.KEYSYM, 66, CATEGORY);

    private PieceKeys() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SPECIAL_A);
        event.register(SPECIAL_B);
        event.register(EXTRA_C);
        event.register(EXTRA_V);
        event.register(EXTRA_B);
    }
}
