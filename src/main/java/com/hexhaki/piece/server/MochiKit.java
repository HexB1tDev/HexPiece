package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * 3.5.5 (the owner: "Next research katakuris mochi fruit and add it ... Legendary fruit"): Charlotte Katakuri's Mochi Mochi no Mi, a
 * Special Paramecia. 3.6.3 (the owner: "I WANT THE OTHER MOVES, all of them, I want yours gone"; their answers: "Dough base + V2
 * awakened", keep the left-click punches): the moves are the reference's (docs/handoff/next-ai-3.6.0.md M and N), the art our own. The
 * moves are server/MochiMoves, the art mochi/MochiArt and MochiDraw. M turns on the Awakening (its own set: the next version).
 */
public final class MochiKit extends FruitKit {
    public static final FruitMove PUNCH = new FruitMove("Mochi Punch", "[LMB]", 0, null,
        "Your arm stretches out as mochi and its fist slams whoever is in front of you, far further than a fist reaches; one arm, then"
            + " the other.");
    public static final FruitMove GRILLED = new FruitMove("Grilled Mochi", "[Z] hold", 0, "mochi_yaki",
        "Hold: your pointed forearm swells into a great mass of mochi with a fist at its end, the bigger the longer you hold; let go and the"
            + " fist flies out on a jet of fire. Whoever it hits is carried along on it until it explodes: the longer the hold, the bigger"
            + " the explosion.");
    public static final FruitMove MUDDLE = new FruitMove("Mochi Muddle", "[X]", 0, "mochi_muddle",
        "A trail of mochi pours out along the ground in front of you; whoever it reaches or who walks into it is stuck fast and hurt, the"
            + " more the nearer its start.");
    public static final FruitMove LOTUS = new FruitMove("Lotus Mochi Combo", "[C]", 0, "mochi_lotus",
        "A Haki fist out of a donut of mochi pounds the one in front of you five times, uppercuts them high into the air, and a giant fist"
            + " slams them back down into the ground.");
    public static final FruitMove CHESTNUT = new FruitMove("Chestnut Mochi", "[V]", 0, "mochi_chestnut",
        "A spiked donut of mochi rises over your shoulder for five seconds and shoots spikes at whoever is nearest, never missing while"
            + " they are in range.");
    public static final FruitMove ANEMONE = new FruitMove("Mochi Anemone", "[B] hold", 0, "mochi_anemone",
        "Hold: donuts of mochi gather over the place you aim at and Haki fists punch down out of them, over and over; whoever is caught"
            + " inside is held and battered as long as you hold.");
    public static final FruitMove BUZZ_CUT = new FruitMove("Buzz Cut Mochi", "[N]", 0, "mochi_buzzcut",
        "Your body becomes a giant donut of mochi rolling forward; whoever you catch is stuck to your arm, now a giant spiked club, and"
            + " slammed into the ground. Five minutes to come back.");
    public static final FruitMove AWAKEN = new FruitMove("Awakening", "[M]", 0, "mochi_awaken",
        "The Mochi Awakening: its own moves come in the next version.");
    /** The moves as the panel and the Ability Book list them. */
    public static final List<FruitMove> MOVES = List.of(PUNCH, GRILLED, MUDDLE, LOTUS, CHESTNUT, ANEMONE, BUZZ_CUT, AWAKEN);
    /** 3.6.3: the awakened set (DoughV2's) comes in the next version: until then the same. */
    public static final List<FruitMove> AWAKENED = MOVES;
    /** Every move (the energy list, the tests). */
    public static final List<FruitMove> ALL = MOVES;

    @Override
    public Fruit fruit() {
        return Fruit.MOCHI;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind}: 0 a quick key down, 1 up, 2 the attack button, 3 the use button down, 4 up. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        switch (kind) {
            case 0 -> MochiMoves.press(player, ability);
            case 1 -> MochiMoves.pressUp(player, ability);
            case 2 -> MochiMoves.punch(player);
            default -> {
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        MochiMoves.tick(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        MochiMoves.forget(player);
    }

    @Override
    public void cancel(ServerPlayer player) {
        MochiMoves.forget(player);
    }

    @Override
    public void reset() {
        MochiMoves.clear();
    }
}
