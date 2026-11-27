package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * 3.9.4 (the owner: "New Fruit: Name: ???? Rarity: Mythical Type: Zoan ... Moves: Only the transformation for now. Z - Transforms
 * (DIsturbing progressive transformation)"; then "Fruit "????" Renamed to "Itsumade""): Saint Marcus Mars' fruit. Z turns the player
 * into the owner's Itsumade and back (server/ItsuServer). The rest of the owner's kit (the passive, Gorosei Stare, Enormous Presence,
 * Yokai Blast, Thy Death, Hellish Teleport; transformed: Wind Gust, the talons, Bow, Enormous Bite, Yokai Beam) comes after.
 */
public final class ItsuKit extends FruitKit {
    public static final FruitMove FORM = new FruitMove("Itsumade", "[Z]", 0, null,
        "Your body convulses and tears open as the Itsumade crawls out of it: a monstrous bird ten blocks tall, its fire alive, a ribbon of"
            + " black flame about it. Fly as you fly in creative (jump to rise, sneak to sink, sprint for speed). Z again and it folds back"
            + " into you. The sea takes it away.");
    public static final List<FruitMove> MOVES = List.of(FORM);

    @Override
    public Fruit fruit() {
        return Fruit.ITSU;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind} 0 a key going down. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        if (kind == 0 && ability == FORM.slot()) {
            ItsuServer.toggle(player);
        }
    }

    @Override
    public void cancel(ServerPlayer player) {
        ItsuServer.revert(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        ItsuServer.restore(player);
        RyouServer.retell(player); // 3.9.7: its Haki colours go with it
    }

    /** 3.9.7: eaten (or its kit given back on joining): every client told its Haki's colours (server/RyouServer.ITSU_COLOR). */
    @Override
    public void granted(ServerPlayer player) {
        RyouServer.retell(player);
    }

    @Override
    public void reset() {
    }
}
