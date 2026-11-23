package com.hexhaki.piece.client.horo;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.server.HoroKit;
import java.util.List;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;

/**
 * 3.3.6: the Horo Horo no Mi on the client (client/horo/HoroFx puts it in the original's ClientKits): its moves for the panel and the
 * log. No stance (between moves the body is the game's own, as every fruit's since 3.1.0).
 */
final class HoroClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.HORO;
    }

    @Override
    public List<FruitMove> moves() {
        return HoroKit.MOVES;
    }

    @Override
    public String stance(AbstractClientPlayer player, CompoundTag state, long now) {
        return null;
    }
}
