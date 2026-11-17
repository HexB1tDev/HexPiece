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

    /**
     * 2.2.4 (the owner: "The keys should be usable just like that in any form"): the original's "powers off" switch (X without the Gomu
     * fruit, {@code BindsClient.off}) also silenced Armament, Observation, Ryou, the Haki leap and every Haki quick slot. Haki's keys
     * ({@code HakiKeys$ForgeBus.tick}, {@code movementInput}) no longer ask it: it turns the fruit off ({@code FruitClient.active} still
     * reads it), never Haki. Only an open screen pauses them, as before.
     */
    public static boolean hakiKeysOff() {
        return false;
    }
}
