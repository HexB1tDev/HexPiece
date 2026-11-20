package com.hexhaki.piece.client;

import com.hexhaki.client.HakiKeys;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 2.7.1 (the owner: "All keys of the mod (but haki) will now be the following Z, X, C, V, B, N, M. R For switching modes / Haki", then:
 * Observation V -> J, the Conqueror's quick abilities K, L, H (M -> H), the Ability Book X -> O, the style's specials Y/U -> Z/X): the
 * new defaults are PiecePatch's (keyDefaults) and PieceKeys', but the game keeps every key a player has in options.txt, so an install
 * that already ran the mod keeps the old ones. Once (a mark in the game folder), each key still on its OLD default moves to its new
 * one; a key the player chose stays theirs.
 */
@Mod.EventBusSubscriber(modid = "hexhaki", value = Dist.CLIENT)
public final class KeyLayout {
    static final String MARK = "hexpiece-keys-2.7.1.txt";
    private static boolean done;

    private KeyLayout() {
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (done || event.phase != TickEvent.Phase.END) {
            return;
        }
        done = true;
        Minecraft minecraft = Minecraft.m_91087_();
        try {
            File mark = new File(minecraft.f_91069_, MARK);
            if (mark.exists()) {
                return;
            }
            boolean moved = false;
            moved |= move(HakiKeys.OBSERVATION, "key.keyboard.v", 74);
            moved |= move(HakiKeys.ACOC, "key.keyboard.j", -1);
            moved |= move(HakiKeys.CONVERGENCE, "key.keyboard.m", 72);
            moved |= move(HakiKeys.MODE, "key.keyboard.h", 88);
            moved |= move(HakiKeys.LOADOUT, "key.keyboard.x", 79);
            moved |= move(HakiKeys.SLOT_8, "key.keyboard.n", 86);
            moved |= move(PieceKeys.SPECIAL_A, "key.keyboard.y", 90);
            moved |= move(PieceKeys.SPECIAL_B, "key.keyboard.u", 88);
            if (moved) {
                KeyMapping.m_90854_();
                minecraft.f_91066_.m_92169_();
            }
            Files.writeString(mark.toPath(), "HexPiece 2.7.1 moved the keys still on their old defaults to the new layout once.\n",
                StandardCharsets.UTF_8);
        } catch (Exception e) {
            com.hexhaki.piece.HexPiece.LOGGER.error("HexPiece: the key layout could not be updated", e);
        }
    }

    /** {@code key} to {@code code} (-1: none) if it is still on {@code old}. */
    static boolean move(KeyMapping key, String old, int code) {
        if (key == null || !old.equals(key.m_90865_())) {
            return false;
        }
        key.m_90848_(InputConstants.Type.KEYSYM.m_84895_(code));
        return true;
    }
}
