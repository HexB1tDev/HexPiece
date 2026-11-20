package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * 2.7.0: the Uo Uo no Mi's kit in the original's fruit system (its enum's UO, PiecePatch.uoFruit): one move, the dragon form, on Z (the
 * original's sixth quick key, its default for a move named "[Z]"), as the owner asked: "make Z a button to transform". The fruit's
 * quick keys work in combat mode too (R only swaps M1 and the right click), so Z transforms whichever is out.
 */
public final class UoKit extends FruitKit {
    public static final FruitMove DRAGON_FORM = new FruitMove("Dragon Form", "[Z]", 0, null,
        "Become Seiryu, the azure dragon, out of your own body: it bursts up out of you, arcs over and coils in the sky where you stood."
            + " Fly as you fly in creative (jump to rise, sneak to sink, sprint for speed); the dragon follows every turn. Z again and it"
            + " dives back into you. The sea takes it away.");
    public static final List<FruitMove> MOVES = List.of(DRAGON_FORM);

    @Override
    public Fruit fruit() {
        return Fruit.UO;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind} 0 is a key going down; {@code ability} the quick slot it is bound to (the move's own slot by default). */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        if (kind == 0 && ability == DRAGON_FORM.slot()) {
            DragonServer.toggle(player);
        }
    }

    @Override
    public void cancel(ServerPlayer player) {
        DragonServer.revert(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        DragonServer.restore(player);
    }
}
