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
 * moves are server/MochiMoves, the art mochi/MochiArt, MochiDraw and MochiV2. M turns on the Awakening: its own set, the reference's
 * DoughV2 (3.6.4), in the same keys.
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
        "Turns the Mochi Awakening on (M again turns it off): your keys become its seven moves and your left click swings Mogura, your"
            + " trident.");
    /** 3.6.4: the Awakening's kit (the reference's DoughV2). */
    public static final FruitMove TRIDENT = new FruitMove("Trident Lacerations", "[LMB]", 0, null,
        "Five swings of Mogura, one a click: a thrust, a level slash, a spin all round, a rising slash and a low sweep up that breaks"
            + " through a raised shield and throws them high into the air.");
    public static final FruitMove LANCE = new FruitMove("Grilled Lance", "[Z] hold", 0, "mochi_lance",
        "Hold: mochi spins round Mogura and your arm swells into a great ball of burning Haki mochi; let go and it is driven into whoever"
            + " is in front of you, burns in them and explodes, then explodes again far bigger.");
    public static final FruitMove LASSO = new FruitMove("Elastic Lasso", "[X]", 0, "mochi_lasso",
        "You whirl a lasso of mochi round you and throw it: whoever it catches is yanked back to you.");
    public static final FruitMove OBLIVION = new FruitMove("Oblivion", "[C]", 0, "mochi_oblivion",
        "You stomp: a surge of mochi erupts round whoever is in front of you and holds them in its walls, then bursts.");
    public static final FruitMove POWER = new FruitMove("Power Mochi", "[V]", 0, "mochi_power",
        "Two burning donuts of mochi stand at your shoulders for twenty seconds: your left click becomes their long Haki fists.");
    public static final FruitMove STORM = new FruitMove("Blazing Storm", "[B]", 0, "mochi_storm",
        "Six burning Haki arms batter the place you aim at, over and over, and end in one great strike.");
    public static final FruitMove SCORCH = new FruitMove("Scorching Buzz Cut", "[N]", 0, "mochi_scorch",
        "Buzz Cut Mochi on fire: a burning spiked wheel that rolls over whoever it meets, three wide swings of the giant spiked ball and"
            + " a far bigger slam. Five minutes to come back.");
    /** The moves as the panel and the Ability Book list them. */
    public static final List<FruitMove> MOVES = List.of(PUNCH, GRILLED, MUDDLE, LOTUS, CHESTNUT, ANEMONE, BUZZ_CUT, AWAKEN);
    /** 3.6.4: the awakened set (the reference's DoughV2) in the base kit's places. */
    public static final List<FruitMove> AWAKENED = List.of(TRIDENT, LANCE, LASSO, OBLIVION, POWER, STORM, SCORCH, AWAKEN);
    /** Every move (the energy list, the tests). */
    public static final List<FruitMove> ALL = List.of(PUNCH, GRILLED, MUDDLE, LOTUS, CHESTNUT, ANEMONE, BUZZ_CUT, AWAKEN, TRIDENT, LANCE, LASSO,
        OBLIVION, POWER, STORM, SCORCH);

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
