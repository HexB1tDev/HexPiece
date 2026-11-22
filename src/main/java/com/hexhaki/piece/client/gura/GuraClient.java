package com.hexhaki.piece.client.gura;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.server.GuraKit;
import java.util.List;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;

/**
 * 3.1.1: the Gura Gura no Mi on the client (client/gura/GuraFx puts it in the original's ClientKits): its moves for the panel and the
 * log. No stance at all (the owner took the Light fruit's away in 3.1.0: "this pose presists, its not needed, and it bugs like shit"):
 * between moves the body is the game's own.
 */
final class GuraClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.GURA;
    }

    @Override
    public List<FruitMove> moves() {
        return GuraKit.MOVES;
    }

    @Override
    public String stance(AbstractClientPlayer player, CompoundTag state, long now) {
        return null;
    }
}
