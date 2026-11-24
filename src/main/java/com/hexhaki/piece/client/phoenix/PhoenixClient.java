package com.hexhaki.piece.client.phoenix;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.server.PhoenixKit;
import java.util.List;

/** 3.6.9: the Tori Tori no Mi, Model: Phoenix on the client (the original's ClientKits, put there by client/phoenix/PhoenixFx). */
final class PhoenixClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.TORI;
    }

    @Override
    public List<FruitMove> moves() {
        return PhoenixKit.MOVES;
    }
}
