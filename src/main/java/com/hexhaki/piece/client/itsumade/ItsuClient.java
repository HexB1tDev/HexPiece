package com.hexhaki.piece.client.itsumade;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.server.ItsuKit;
import java.util.List;

/** 3.9.4: the Itsumade on the client (the original's ClientKits, put there by client/itsumade/ItsuFx): its moves for the panel. */
final class ItsuClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.ITSU;
    }

    @Override
    public List<FruitMove> moves() {
        return ItsuKit.MOVES;
    }
}
