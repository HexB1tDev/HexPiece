package com.hexhaki.piece.client.mochi;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitMove;
import com.hexhaki.fruit.client.ClientKit;
import com.hexhaki.piece.server.MochiKit;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;

/**
 * 3.5.5: the Mochi Mochi no Mi on the client (client/mochi/MochiFx puts it in the original's ClientKits): its moves for the panel and
 * the log; while one's own Awakening is on, the awakened set in Z to B's places (the owner: "M toggles a mode"). No stance.
 */
final class MochiClient implements ClientKit {
    @Override
    public Fruit fruit() {
        return Fruit.MOCHI;
    }

    @Override
    public List<FruitMove> moves() {
        Minecraft minecraft = Minecraft.m_91087_();
        return minecraft.f_91074_ != null && MochiFx.awakened(minecraft.f_91074_.m_19879_()) ? MochiKit.AWAKENED : MochiKit.MOVES;
    }

    @Override
    public String stance(AbstractClientPlayer player, CompoundTag state, long now) {
        return null;
    }
}
