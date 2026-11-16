package com.hexhaki.piece.client;

import com.hexhaki.fruit.client.FruitClient;

/**
 * 2.1.3: where the original's client code asks HexPiece (calls redirected by {@code patch/PiecePatch.java}'s CALL_REDIRECTS).
 */
public final class PieceHooks {
    private PieceHooks() {
    }

    /**
     * A kit fruit's M1 and right click ({@code FruitKeys}): the kit is active and the player is not in combat mode. R switches between
     * the two (the owner: "tapping R switches from Combat style and fruit (if any is eaten)"); the kit's bound abilities keep their keys.
     */
    public static boolean fruitKeysActive() {
        return FruitClient.active() && !CombatMode.on();
    }
}
