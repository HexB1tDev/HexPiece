package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * 3.9.4 (the owner: "New Fruit: Name: ???? Rarity: Mythical Type: Zoan ... Moves: Only the transformation for now. Z - Transforms
 * (DIsturbing progressive transformation)"; then "Fruit "????" Renamed to "Itsumade""): Saint Marcus Mars' fruit. Z turns the player
 * into the owner's Itsumade and back (server/ItsuServer). 3.9.7: its passive (server/ItsuPassive) and its moves in its form
 * (server/ItsuMoves); 3.9.8: its moves as a human (server/ItsuSaint).
 */
public final class ItsuKit extends FruitKit {
    public static final FruitMove FORM = new FruitMove("Itsumade", "[Z]", 0, null,
        "Your body convulses and tears open as the Itsumade crawls out of it: a monstrous bird ten blocks tall, its fire alive, a ribbon of"
            + " black flame about it. Fly as you fly in creative (jump to rise, sneak to sink, sprint for speed). Z again and it folds back"
            + " into you. The sea takes it away.");
    // 3.9.8: as a human (the owner's spec)
    public static final FruitMove STARE = new FruitMove("Gorosei Stare", "[X]", 0, "itsu_stare",
        "Fix your sight on a foe and glare: a moment later it is thrown far back and felled, as if struck.");
    public static final FruitMove PRESENCE = new FruitMove("Enormous Presence", "[C]", 0, "itsu_presence",
        "For 10 seconds your body lets out the Conqueror's Haki on its own: waves of it press down whoever is near, and now and then a bolt"
            + " of its lightning leaps out of you, stunning whatever it touches.");
    public static final FruitMove BLAST = new FruitMove("Yokai Blast", "[V] hold", 0, "itsu_blast",
        "Point your hand and hold to charge; let go and a beam of orange fire wrapped in black flames pours out where you pointed (it"
            + " cannot be turned): 1 to 5 seconds by the charge, boring through 5 to 15 blocks of ground, which grows back.");
    public static final FruitMove DEATH = new FruitMove("Thy Death", "[B]", 0, "itsu_death",
        "Raise your hand: for 10 seconds the Conqueror's lightning falls from the clouds all round you (25 blocks), every bolt bursting"
            + " where it strikes the ground, hurting and stunning what it catches.");
    // 3.9.9
    public static final FruitMove TELEPORT = new FruitMove("Hellish Teleport", "[N]", 0, "itsu_gate",
        "Opens your three gates. Click an empty one to bind it where you stand; rename, move or clear a bound one. Click a bound one and a"
            + " pentagram forms under you and at the gate at once: ten seconds later everything standing on yours is swallowed by black"
            + " flames, blasted up into the sky and dropped out of it onto the other.");
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
    public static final List<FruitMove> MOVES = List.of(FORM, STARE, PRESENCE, BLAST, DEATH, TELEPORT, WIND_GUST, TALONS, BOW, BITE, YOKAI_BEAM);
    /**
     * 4.0.2 (the owner: "you can see your abilities from Itsumade in the normal form in the HUD ... something that never happens in this
     * mod"): the moves the panel shows as a human, and in its form (client/itsumade/ItsuClient picks by the form the player is in).
     */
    public static final List<FruitMove> HUMAN_MOVES = List.of(FORM, STARE, PRESENCE, BLAST, DEATH, TELEPORT);
    public static final List<FruitMove> FORM_MOVES = List.of(FORM, WIND_GUST, TALONS, BOW, BITE, YOKAI_BEAM);

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
            ItsuSaint.forget(player);
            ItsuServer.toggle(player);
            return;
        }
        // 3.9.8: the same keys are the human's moves and its form's (ItsuMoves and ItsuSaint each act only in theirs)
        switch (kind) {
            case 0 -> {
                ItsuMoves.press(player, ability);
                ItsuSaint.press(player, ability);
            }
            case 1 -> {
                ItsuMoves.release(player, ability);
                ItsuSaint.release(player, ability);
            }
            case 2 -> ItsuMoves.attack(player);
            case 3 -> ItsuMoves.use(player);
            default -> {
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        ItsuMoves.tick(player);
        ItsuSaint.tick(player);
    }

    @Override
    public void cancel(ServerPlayer player) {
        ItsuMoves.forget(player);
        ItsuSaint.forget(player);
        ItsuGates.forget(player);
        ItsuServer.revert(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        ItsuMoves.forget(player);
        ItsuSaint.forget(player);
        ItsuGates.forget(player);
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
