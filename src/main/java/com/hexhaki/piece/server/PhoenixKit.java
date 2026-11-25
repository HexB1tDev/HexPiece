package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * 3.6.9 (the owner: "Mythical Fruit: Tori Tori no Mi, Model: Phoenix. Zoan Type. I made the model and sent it in the zip, first of all
 * animate it so it looks good, good flying animations, etc. Z (transforms/detransforms)"): Marco's fruit. Z turns the player into the
 * owner's phoenix and back (server/PhoenixServer). 3.7.2 (the owner: "Proceed"): the reference's moves (docs/handoff/next-ai-3.6.3.md
 * AD), each in both forms (server/PhoenixMoves): X Fujiazami, C Talon Slam (as the phoenix: Hawk Rush, held), 3.7.3 V Phoenix Inferno,
 * B Bluebird (held). The panel shows the form's own list (client/phoenix/PhoenixClient).
 */
public final class PhoenixKit extends FruitKit {
    public static final FruitMove FORM = new FruitMove("Phoenix Form", "[Z]", 0, null,
        "Burst into blue flame and become the phoenix: a great bird of blue and gold fire. Fly as you fly in creative (jump to rise, sneak"
            + " to sink, sprint for speed). Z again and the flames fold back into you. The sea takes it away.");
    public static final FruitMove FUJIAZAMI = new FruitMove("Fujiazami", "[X]", 0, "phoenix_fuji",
        "Blue fire gathers at your feet and you dash forward in a burst of it; whoever you meet is carried along and thrown on when the"
            + " dash ends, in a spray of blue and gold flame.");
    public static final FruitMove TALON_SLAM = new FruitMove("Talon Slam", "[C]", 0, "phoenix_talon",
        "You leap high, staggering whoever is near; at the top your right leg lights with blue fire and you smash down in front of you in"
            + " an explosion of blue flame that leaves a burning crater.");
    public static final FruitMove FUJIAZAMI_PHOENIX = new FruitMove("Phoenix: Fire Tornado", "[X]", 0, "phoenix_fuji",
        "As the phoenix: your wings sweep forward and beat, and a huge tornado of blue and gold flame rises in front of you and runs on the"
            + " way you look: whoever it takes is carried round and up in it, burnt and set alight, then flung out. With the Conqueror's Haki"
            + " and Ryou on it is bigger still and throws lightning.");
    public static final FruitMove HAWK_RUSH = new FruitMove("Phoenix: Hawk Rush", "[C] hold", 0, "phoenix_talon",
        "As the phoenix: wings wrapped forward, you rush ahead low, steering where you look, for as long as you hold (up to five seconds);"
            + " the first one you meet is seized in your claws, whirled round and thrown.");
    /** 3.7.3: V and B. */
    public static final FruitMove INFERNO = new FruitMove("Phoenix Inferno", "[V]", 0, "phoenix_inferno",
        "Phoenix wings and a tail of fire burst out of your back and you rain a barrage of burning kicks on whoever is in front of you,"
            + " holding them there; the last kick throws them high, you backflip away and the wings scatter in a burst of flame.");
    public static final FruitMove BLUEBIRD = new FruitMove("Bluebird", "[B] hold", 0, "phoenix_bluebird",
        "Hold: phoenix wings spread at your back and a sphere of yellow and blue flames grows in front of you, bigger the longer you hold; let"
            + " go and it flies, blowing up on whatever it meets in a great explosion.");
    public static final FruitMove INFERNO_PHOENIX = new FruitMove("Phoenix: Phoenix Inferno", "[V]", 0, "phoenix_inferno",
        "As the phoenix: you spin inside a whirlpool of blue and white fire that burns and holds whoever is in it, then fly up, throwing"
            + " them high, and dive back down into the middle of it in a huge blast of blue flame that throws the ground up.");
    public static final FruitMove BLUEBIRD_PHOENIX = new FruitMove("Phoenix: Bluebird", "[B] hold", 0, "phoenix_bluebird",
        "As the phoenix, hold: your wings spread and the fire gathers round you into a sphere of flames at your chest; let go and one great sphere"
            + " of blue and gold flames flies out and blows up in a massive explosion.");
    /** The human's moves and the phoenix's, in the same keys (the panel shows the form's). */
    public static final List<FruitMove> HUMAN = List.of(FORM, FUJIAZAMI, TALON_SLAM, INFERNO, BLUEBIRD);
    public static final List<FruitMove> PHOENIX = List.of(FORM, FUJIAZAMI_PHOENIX, HAWK_RUSH, INFERNO_PHOENIX, BLUEBIRD_PHOENIX);
    /** Every move (the energy list, the tests). */
    public static final List<FruitMove> MOVES = List.of(FORM, FUJIAZAMI, TALON_SLAM, INFERNO, BLUEBIRD, FUJIAZAMI_PHOENIX, HAWK_RUSH,
        INFERNO_PHOENIX, BLUEBIRD_PHOENIX);

    @Override
    public Fruit fruit() {
        return Fruit.TORI;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind} 0 a key going down, 1 coming up. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        if (kind == 0 && ability == FORM.slot()) {
            PhoenixMoves.forget(player);
            PhoenixServer.toggle(player);
        } else if (kind == 0) {
            PhoenixMoves.press(player, ability);
        } else if (kind == 1) {
            PhoenixMoves.release(player, ability);
        }
    }

    @Override
    public void cancel(ServerPlayer player) {
        PhoenixMoves.forget(player);
        PhoenixServer.revert(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        PhoenixMoves.forget(player);
        PhoenixServer.restore(player);
    }

    @Override
    public void reset() {
        PhoenixMoves.clear();
    }
}
