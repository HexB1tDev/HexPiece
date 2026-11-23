package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * 3.3.6 (the owner: "Horo Horo No Mi (common) ... Negative Hollow ... Mini Hollow / Ghost Bomb ... Toku Hollow ... Ghost Doppelgänger &
 * Astral Projection ... Ghost Network"; the keys asked and accepted as proposed, docs/handoff 8zw): Perona's Horo Horo no Mi, a
 * Paramecia. The attack button is the game's own punch (client/horo/HoroFx); the moves are server/HoroMoves, the ghosts horo/HoroArt.
 */
public final class HoroKit extends FruitKit {
    public static final FruitMove NEGATIVE = new FruitMove("Negative Hollow", "[RMB]", 0, "horo_negative",
        "Tap to send a Negative Hollow, hold to send a stream of up to five. They float through walls and bodies, and whoever they pass"
            + " through sinks into despair: down on the ground, unable to fight, for six seconds.");
    public static final FruitMove MINI = new FruitMove("Mini Hollow", "[Z]", 0, "horo_mini",
        "Four little ghosts fly to the nearest foes and cling to them. Press again to burst them all (they burst by themselves after six"
            + " seconds).");
    public static final FruitMove TOKU = new FruitMove("Toku Hollow", "[X]", 0, "horo_toku",
        "A giant grinning ghost floats out ahead of you. Press again, or let it touch someone or a wall: it explodes in a huge blast.");
    public static final FruitMove ASTRAL = new FruitMove("Astral Projection", "[C]", 0, "horo_astral",
        "Your spirit leaves your body and flies as a ghost for up to twenty seconds: nothing can touch it and mobs lose sight of you. Press"
            + " again, or when the time is up, you are back in your body where you left it.");
    public static final FruitMove NETWORK = new FruitMove("Ghost Network", "[V]", 0, "horo_network",
        "Six ghosts spread out round you for thirty seconds and keep watch: every living thing within 48 blocks glows, seen through walls.");
    public static final List<FruitMove> MOVES = List.of(NEGATIVE, MINI, TOKU, ASTRAL, NETWORK);

    @Override
    public Fruit fruit() {
        return Fruit.HORO;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind}: 0 a quick key down, 1 up, 2 the attack button, 3 the use button down, 4 up. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        switch (kind) {
            case 0 -> HoroMoves.press(player, ability);
            case 3 -> HoroMoves.use(player);
            case 4 -> HoroMoves.useUp(player);
            default -> {
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        HoroMoves.tick(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        HoroMoves.forget(player);
    }

    @Override
    public void cancel(ServerPlayer player) {
        HoroMoves.forget(player);
    }

    @Override
    public void reset() {
        HoroMoves.clear();
    }
}
