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
    // 3.9.7: in its form (the owner's spec, his words in each)
    public static final FruitMove WIND_GUST = new FruitMove("Itsumade: Wind Gust", "[LMB]", 0, "itsu_gust",
        "In its form: a slow move: the great wings raised and brought down in one huge flap, a gust of wind that throws everything in the"
            + " cone in front of you far back.");
    public static final FruitMove TALONS = new FruitMove("Itsumade: Talons", "[RMB]", 0, null,
        "In its form, flying: the talons close on whatever is under you and carry it wherever you fly, for as long as you like; again, and"
            + " they let it go.");
    public static final FruitMove BOW = new FruitMove("Itsumade: Bow", "[X]", 0, "itsu_bow",
        "In its form: for 15 seconds the Conqueror's lightning tears off the whole of your huge body, from its skin, striking and stunning"
            + " whatever is near.");
    public static final FruitMove BITE = new FruitMove("Itsumade: Enormous Bite", "[C]", 0, "itsu_bite",
        "In its form: the great neck lunges forward and the jaws snap shut where you look: a deep wound that bleeds, and broken bones.");
    public static final FruitMove YOKAI_BEAM = new FruitMove("Itsumade: Yokai Beam", "[V] hold", 0, "itsu_beam",
        "In its form: hold to gather a ball of white-hot fire in the open jaws, let go and it pours out as a beam of orange fire wrapped in"
            + " black flames, hotter and longer the longer you held, burning through what it meets and exploding at its end in a great ball"
            + " of fire.");
    public static final List<FruitMove> MOVES = List.of(FORM, WIND_GUST, TALONS, BOW, BITE, YOKAI_BEAM);

    @Override
    public Fruit fruit() {
        return Fruit.ITSU;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind}: 0 a quick key down, 1 up, 2 the attack button, 3 the use button down, 4 up. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        if (kind == 0 && ability == FORM.slot()) {
            ItsuMoves.forget(player);
            ItsuServer.toggle(player);
            return;
        }
        switch (kind) {
            case 0 -> ItsuMoves.press(player, ability);
            case 1 -> ItsuMoves.release(player, ability);
            case 2 -> ItsuMoves.attack(player);
            case 3 -> ItsuMoves.use(player);
            default -> {
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        ItsuMoves.tick(player);
    }

    @Override
    public void cancel(ServerPlayer player) {
        ItsuMoves.forget(player);
        ItsuServer.revert(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        ItsuMoves.forget(player);
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
