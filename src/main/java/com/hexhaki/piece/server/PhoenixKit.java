package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * 3.6.9 (the owner: "Mythical Fruit: Tori Tori no Mi, Model: Phoenix. Zoan Type. I made the model and sent it in the zip, first of all
 * animate it so it looks good, good flying animations, etc. Z (transforms/detransforms)"): Marco's fruit. Z turns the player into the
 * owner's phoenix and back (server/PhoenixServer); the moves of the reference's page come after the owner has seen it flying.
 */
public final class PhoenixKit extends FruitKit {
    public static final FruitMove FORM = new FruitMove("Phoenix Form", "[Z]", 0, null,
        "Burst into blue flame and become the phoenix: a great bird of blue and gold fire. Fly as you fly in creative (jump to rise, sneak"
            + " to sink, sprint for speed). Z again and the flames fold back into you. The sea takes it away.");
    public static final List<FruitMove> MOVES = List.of(FORM);

    @Override
    public Fruit fruit() {
        return Fruit.TORI;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind} 0 a key going down. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        if (kind == 0 && ability == FORM.slot()) {
            PhoenixServer.toggle(player);
        }
    }

    @Override
    public void cancel(ServerPlayer player) {
        PhoenixServer.revert(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        PhoenixServer.restore(player);
    }
}
