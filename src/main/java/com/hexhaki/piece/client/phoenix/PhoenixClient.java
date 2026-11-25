package com.hexhaki.piece.client.phoenix;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.server.PhoenixKit;
import java.util.List;

/**
 * 3.6.9: the Tori Tori no Mi, Model: Phoenix on the client (the original's ClientKits, put there by client/phoenix/PhoenixFx). 3.7.2: the
 * panel lists the form's own moves (the human's, or the phoenix's in the same keys), as the Mochi fruit's Awakening does.
 */
final class PhoenixClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.TORI;
    }

    @Override
    public List<FruitMove> moves() {
        return PhoenixFx.selfOut() ? PhoenixKit.PHOENIX : PhoenixKit.HUMAN;
    }
}
