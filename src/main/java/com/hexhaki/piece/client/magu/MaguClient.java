package com.hexhaki.piece.client.magu;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.server.MaguKit;
import java.util.List;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;

/**
 * 3.1.3: the Magu Magu no Mi on the client (client/magu/MaguFx puts it in the original's ClientKits): its moves for the panel and the
 * log. No stance at all (the owner took the Light fruit's away in 3.1.0): between moves the body is the game's own.
 */
final class MaguClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.MAGU;
    }

    @Override
    public List<FruitMove> moves() {
        return MaguKit.MOVES;
    }

    @Override
    public String stance(AbstractClientPlayer player, CompoundTag state, long now) {
        return null;
    }
}
