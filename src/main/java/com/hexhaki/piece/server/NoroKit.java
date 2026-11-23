package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * 3.3.4 (the owner: "New common Fruits ... Noro Noro No Mi (Common) ... It allows the user to emit "Noroma" photon beams that
 * drastically slow down whatever they hit for 30 seconds"; the keys asked and accepted as proposed, docs/handoff 8zw): Foxy's Noro Noro
 * no Mi, a Paramecia. Keys as every fruit's (the attack button the game's own punch: client/noro/NoroFx); the moves are
 * server/NoroMoves, the slowdown and its stored blows server/NoroSlow.
 */
public final class NoroKit extends FruitKit {
    public static final FruitMove PHOTON = new FruitMove("Noroma Photon", "[RMB]", 0, "noro_photon",
        "A burst of pink Noroma photons out of your hands; hold for a steady beam. Whatever they touch moves in extreme slow motion for"
            + " 30 seconds: people, beasts, even arrows and fireballs, which hang in the air crawling along.");
    public static final FruitMove SPREAD = new FruitMove("Noroma Spread", "[Z]", 0, "noro_spread",
        "A wide fan of photon bursts in front of you, slowing everything it catches.");
    public static final FruitMove MIRROR = new FruitMove("Noroma Mirror", "[X]", 0, "noro_mirror",
        "A mirror in the air in front of you for a few seconds. Shots that hit it are turned back the way they came, slowed; your own"
            + " photons bounce off it the same way.");
    public static final FruitMove TIMES_UP = new FruitMove("Time's Up", "[C]", 0, "noro_timesup",
        "Snap your fingers: every slowdown you put on ends now, and every blow they held back lands at once.");
    public static final FruitMove FIELD = new FruitMove("Slow Field", "[V]", 0, "noro_field",
        "A dome of photons rises round you for 8 seconds: whatever comes into it is slowed, shots stopping in the air.");
    public static final FruitMove KINETIC = new FruitMove("Delayed Kinetic Force", "passive", 0, null,
        "Blows on a slowed body do nothing yet: their damage and their push add up, and all land at once when its slowdown ends.");
    public static final List<FruitMove> MOVES = List.of(PHOTON, SPREAD, MIRROR, TIMES_UP, FIELD, KINETIC);

    @Override
    public Fruit fruit() {
        return Fruit.NORO;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind}: 0 a quick key down, 1 up, 2 the attack button, 3 the use button down, 4 up. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        switch (kind) {
            case 0 -> NoroMoves.press(player, ability);
            case 3 -> NoroMoves.use(player);
            case 4 -> NoroMoves.useUp(player);
            default -> {
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        NoroMoves.tick(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        NoroMoves.forget(player);
    }

    @Override
    public void cancel(ServerPlayer player) {
        NoroMoves.forget(player);
    }

    @Override
    public void reset() {
        NoroMoves.clear();
        NoroSlow.clear();
    }
}
