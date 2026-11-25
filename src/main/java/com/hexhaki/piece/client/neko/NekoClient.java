package com.hexhaki.piece.client.neko;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.server.NekoKit;
import java.util.List;

/** 3.7.9: the Neko Neko no Mi, Model: Leopard on the client (put in the original's ClientKits by client/neko/NekoFx): the form's moves. */
final class NekoClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.NEKO;
    }

    @Override
    public List<FruitMove> moves() {
        return NekoFx.selfOut() ? NekoKit.LEOPARD : NekoKit.HUMAN;
    }
}
