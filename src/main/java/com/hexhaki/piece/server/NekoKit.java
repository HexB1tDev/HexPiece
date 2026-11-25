package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * 3.7.9 (the owner: "New Fruit: Legendary Leopard Fruit, Name: Neko Neko No Mi, Type: Zoan ... Moves (Contains both transformed and
 * untransformed)"; then "first of all, Z (transformation, get the base animations, and m1s.)"): Rob Lucci's fruit. Z turns the player
 * into the owner's leopard and back (server/NekoServer); M1 is Six Styles in both forms (server/NekoMoves). The reference's other moves
 * (Finger Pistol Barrage, Sonic Kick, Infinity Drive, Unrelenting Assault, Heavens Demise) come later. The panel shows the form's list
 * (client/neko/NekoClient).
 */
public final class NekoKit extends FruitKit {
    public static final FruitMove FORM = new FruitMove("Leopard Form", "[Z]", 0, null,
        "Slouch down into black flames and burst out as the leopard, five blocks tall, its mane, fur and ribbon burning black, with a roar"
            + " that throws everyone near you back; much faster on your feet. Z again and the leopard burns away. The sea takes it away.");
    public static final FruitMove SIX_STYLES = new FruitMove("Six Styles", "[LMB]", 0, null,
        "Click by click, a combo of five: a front kick, a claw swipe, a roundhouse kick, a double punch and Rokuogan, the shock of both"
            + " fists thrust out together that blows whoever it hits far away.");
    public static final FruitMove SIX_STYLES_LEOPARD = new FruitMove("Leopard: Six Styles", "[LMB]", 0, null,
        "As the leopard, the same five blows (as much damage) with its claws and its whole weight, and further: it is five blocks tall.");
    /** The human's moves and the leopard's (the panel shows the form's). */
    public static final List<FruitMove> HUMAN = List.of(FORM, SIX_STYLES);
    public static final List<FruitMove> LEOPARD = List.of(FORM, SIX_STYLES_LEOPARD);
    public static final List<FruitMove> MOVES = List.of(FORM, SIX_STYLES, SIX_STYLES_LEOPARD);

    @Override
    public Fruit fruit() {
        return Fruit.NEKO;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind}: 0 a quick key down, 1 up, 2 the attack button. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        if (kind == 0 && ability == FORM.slot()) {
            NekoMoves.forget(player);
            NekoServer.toggle(player);
        } else if (kind == 2) {
            NekoMoves.m1(player);
        }
    }

    @Override
    public void cancel(ServerPlayer player) {
        NekoMoves.forget(player);
        NekoServer.revert(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        NekoMoves.forget(player);
        NekoServer.restore(player);
    }

    @Override
    public void reset() {
        NekoMoves.clear();
    }
}
