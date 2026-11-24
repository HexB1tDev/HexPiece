package com.hexhaki.piece.client;

import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.fruit.client.ClientKits;
import com.hexhaki.fruit.client.FruitClient;
import com.hexhaki.fruit.client.FruitKeys;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 3.1.7 (the owner, after 3.1.6 put the Gura ultimate on a key of its own, End: "It doesn't require ultimate key. It's just another
 * extra key for the moves I said ... We're just like adding N to one. That's it."; 2.7.1: "All keys of the mod (but haki) will now be
 * the following Z, X, C, V, B, N, M"): N is one more fruit move key beside Z X C V B. The original's nine quick slots are all taken (G K
 * L H for Haki, B Z C V X for the fruit), so N is a key mapping of its own ({@link PieceKeys#FRUIT_N}, N by default): in fruit mode,
 * when the fruit's kit has a move on "[N]", its press and its let go go to the server through the original's own fruit message
 * ({@code FruitKeys.send}: 0 down, 1 up) with the ability number {@link #ABILITY} (out of the original's 0 to 8), as a quick key's do.
 * Let go is always sent once it was pressed (a screen opening, the mode or the fruit changing let it go too).
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class FruitKeyN {
    // 3.3.7: M and the Ito fruit's air step go the same way (ABILITY_M, ABILITY_SKY)
    /** The N key's ability number (server/GuraUltimate.SLOT takes it). */
    public static final int ABILITY = 40;
    /** 3.3.7: the M key's (the Ito fruit's Awakening; the Nika fruit reads M itself, client/gear/NikaKeys). */
    public static final int ABILITY_M = 41;
    /**
     * 3.3.7: a jump pressed in the air with the Ito fruit (Sora no Michi, server/ItoMoves): the server lets the player step up on strings
     * (it alone moves the body and forgets the fall, so no fall damage is taken for the height walked).
     */
    public static final int ABILITY_SKY = 42;
    private static boolean down;
    private static boolean downM;
    private static boolean jumpWas;
    /** 3.5.0: the client ticks counted here, and the tick of the last press of jump (Sky Road takes two within DOUBLE_TAP ticks). */
    private static long clock;
    private static long lastJump = Long.MIN_VALUE / 2;
    static final int DOUBLE_TAP = 7;

    private FruitKeyN() {
    }

    /** Whether the active fruit has a move on N. */
    static boolean hasN(Minecraft minecraft) {
        return has(minecraft, "[N]");
    }

    /** Whether the active fruit has a move on {@code key} ("[N]", "[M]"). */
    static boolean has(Minecraft minecraft, String key) {
        com.hexhaki.fruit.Fruit fruit = FruitClient.fruit(minecraft.f_91074_.m_19879_());
        ClientKit kit = fruit == null ? null : ClientKits.get(fruit);
        if (kit == null || kit.moves() == null) {
            return false;
        }
        for (FruitMove move : kit.moves()) {
            if (move.key() != null && move.key().startsWith(key)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        boolean can = minecraft.f_91074_ != null && minecraft.f_91080_ == null && PieceHooks.fruitKeysActive() && hasN(minecraft);
        boolean held = can && PieceKeys.FRUIT_N.m_90857_();
        while (PieceKeys.FRUIT_N.m_90859_()) {
            // the clicks are counted by the key itself: only whether it is held matters here
        }
        if (held && !down) {
            down = true;
            FruitKeys.send(0, ABILITY, FruitKeys.flags(minecraft));
        } else if (!held && down) {
            down = false;
            if (minecraft.f_91074_ != null) {
                FruitKeys.send(1, ABILITY, FruitKeys.flags(minecraft));
            }
        }
        // 3.3.7: M, as N, for a fruit with a move on it (the Ito fruit's Awakening)
        boolean canM = minecraft.f_91074_ != null && minecraft.f_91080_ == null && PieceHooks.fruitKeysActive() && has(minecraft, "[M]");
        boolean heldM = canM && PieceKeys.FRUIT_M.m_90857_();
        if (canM) {
            while (PieceKeys.FRUIT_M.m_90859_()) {
                // only whether it is held matters here
            }
        }
        if (heldM && !downM) {
            downM = true;
            FruitKeys.send(0, ABILITY_M, FruitKeys.flags(minecraft));
        } else if (!heldM && downM) {
            downM = false;
            if (minecraft.f_91074_ != null) {
                FruitKeys.send(1, ABILITY_M, FruitKeys.flags(minecraft));
            }
        }
        // 3.3.7: the jump pressed again in the air (not flying, not swimming) with the Ito fruit; 3.4.3: Sky Road, started here at once
        // (client/ito/SkyRoad: the server's word follows); while it runs the jump key climbs instead
        boolean jump = minecraft.f_91074_ != null && minecraft.f_91066_.f_92089_.m_90857_();
        // 3.5.0 (the owner: Sky Road "should only happen when the player double clicks the jump ... right now I only have to click it once
        // and instantly starts the flying"): only the second of two presses DOUBLE_TAP ticks apart or less, in the air
        boolean pressed = jump && !jumpWas;
        boolean twice = pressed && clock - lastJump <= DOUBLE_TAP;
        if (pressed) {
            lastJump = twice ? Long.MIN_VALUE / 2 : clock;
        }
        clock++;
        if (twice && minecraft.f_91080_ == null && PieceHooks.fruitKeysActive()
            && FruitClient.fruit(minecraft.f_91074_.m_19879_()) == com.hexhaki.fruit.Fruit.ITO && !minecraft.f_91074_.m_20096_()
            && !minecraft.f_91074_.m_150110_().f_35935_ && !minecraft.f_91074_.m_20069_() && !minecraft.f_91074_.m_20159_()
            && !com.hexhaki.piece.client.ito.SkyRoad.active(minecraft.f_91074_.m_19879_())) {
            FruitKeys.send(0, ABILITY_SKY, FruitKeys.flags(minecraft));
            com.hexhaki.piece.client.ito.SkyRoad.startLocal(minecraft.f_91074_);
        }
        jumpWas = jump;
    }
}
