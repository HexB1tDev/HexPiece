package com.hexhaki.piece.client.itsumade;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.server.ItsuKit;
import java.util.List;

/**
 * 3.9.4: the Itsumade on the client (the original's ClientKits, put there by client/itsumade/ItsuFx): its moves for the panel; 4.0.2: those
 * of the form the player is in.
 */
final class ItsuClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.ITSU;
    }

    @Override
    public List<FruitMove> moves() {
        // 4.0.2: only the moves of the form the player is in (the panel never shows the other's)
        return ItsuFx.selfOut() ? ItsuKit.FORM_MOVES : ItsuKit.HUMAN_MOVES;
    }
}
