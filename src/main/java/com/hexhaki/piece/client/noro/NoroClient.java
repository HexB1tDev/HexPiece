package com.hexhaki.piece.client.noro;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.server.NoroKit;
import java.util.List;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;

/**
 * 3.3.4: the Noro Noro no Mi on the client (client/noro/NoroFx puts it in the original's ClientKits): its moves for the panel and the
 * log. No stance (between moves the body is the game's own, as every fruit's since 3.1.0).
 */
final class NoroClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.NORO;
    }

    @Override
    public List<FruitMove> moves() {
        return NoroKit.MOVES;
    }

    @Override
    public String stance(AbstractClientPlayer player, CompoundTag state, long now) {
        return null;
    }
}
