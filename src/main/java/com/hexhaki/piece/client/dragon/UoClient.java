package com.hexhaki.piece.client.dragon;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.server.UoKit;
import java.util.List;

/**
 * 2.7.0: the Uo Uo no Mi's kit on the client (the original's ClientKits, opened by PiecePatch). 2.7.2: the abilities panel shows the
 * form's own moves (human: Z, X, C, V, B; the dragon: Z, M1, X, C); the Captain's Log all of them ({@link #all}).
 */
final class UoClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.UO;
    }

    @Override
    public List<FruitMove> moves() {
        return DragonClient.selfDragon() ? UoKit.DRAGON : UoKit.HUMAN;
    }
}
