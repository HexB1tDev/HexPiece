package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

/**
 * 3.3.7 (the owner: "New Rare Fruit: Ito Ito No Mi (Rare) The Strings Fruit ... Research Do Flamingo's Devil fruit, get reference gifs,
 * reuse already in the game first person and third person animations"; the keys asked and accepted "As proposed", the Birdcage "40
 * blocks, 45 s": docs/handoff 8zzc): Donquixote Doflamingo's Ito Ito no Mi, a Paramecia. The moves are server/ItoMoves, the strings
 * ito/ItoArt.
 */
public final class ItoKit extends FruitKit {
    public static final FruitMove SLASH = new FruitMove("String Slash", "[LMB]", 0, null,
        "Strings lash out from your fingers: every swing of the attack button slices whoever is in front of you, further than a fist.");
    public static final FruitMove BULLET = new FruitMove("Tamaito", "[RMB]", 0, "ito_bullet",
        "Tap to shoot a string bullet from your fingertip that pierces through everyone in its way; hold to fire a burst of six.");
    public static final FruitMove GOSHIKI = new FruitMove("Goshikito", "[Z]", 0, "ito_goshiki",
        "Five strings slash down from the sky onto the spot you look at, cutting the ground and everyone standing there.");
    public static final FruitMove OVERHEAT = new FruitMove("Overheat", "[X]", 0, "ito_overheat",
        "A red-hot rope of tightly wound string lashes out in a long line in front of you, burning whoever it strikes.");
    public static final FruitMove PARASITE = new FruitMove("Parasite", "[C]", 0, "ito_parasite",
        "Threads sink into the one you look at: for five seconds they cannot move on their own, and you drag them wherever you look."
            + " Press again to let go.");
    public static final FruitMove WEB = new FruitMove("Spider's Web", "[V]", 0, "ito_web",
        "A web of string spreads in front of you for six seconds: projectiles stop in it and blows from the front are caught.");
    public static final FruitMove KNIGHT = new FruitMove("Black Knight", "[B]", 0, "ito_knight",
        "You weave a copy of yourself out of string that fights beside you for fifteen seconds, slicing whoever is nearest.");
    public static final FruitMove CAGE = new FruitMove("Birdcage", "[N]", 0, "ito_cage",
        "A huge cage of strings 40 blocks across closes round you for 45 seconds, shrinking slowly: whoever touches its strings is cut,"
            + " and no one gets out. Press again to take it down.");
    public static final FruitMove AWAKEN = new FruitMove("Awakening", "[M]", 0, "ito_awaken",
        "Break White: the ground round you turns to waves of white string rolling outward, throwing everyone they reach. With Armament"
            + " on, God Thread: sixteen Haki-coated spears of string fired at once at what you look at.");
    public static final FruitMove SKY = new FruitMove("Sora no Michi", "passive", 0, null,
        "Jump again in the air to step up on strings hung from the sky (three steps before you land); no fall damage from them.");
    public static final FruitMove SURGERY = new FruitMove("Self-Surgery", "passive", 0, null,
        "Below a third of your health, strings stitch your wounds: you heal for a few seconds, once a minute.");
    public static final List<FruitMove> MOVES = List.of(SLASH, BULLET, GOSHIKI, OVERHEAT, PARASITE, WEB, KNIGHT, CAGE, AWAKEN, SKY, SURGERY);

    @Override
    public Fruit fruit() {
        return Fruit.ITO;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind}: 0 a quick key down, 1 up, 2 the attack button, 3 the use button down, 4 up. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        switch (kind) {
            case 0 -> ItoMoves.press(player, ability);
            case 2 -> ItoMoves.slash(player);
            case 3 -> ItoMoves.use(player);
            case 4 -> ItoMoves.useUp(player);
            default -> {
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        ItoMoves.tick(player);
    }

    /** Spider's Web: a blow from in front of its user is caught in the web (the original asks before a blow lands; true stops it). */
    @Override
    public boolean blocks(ServerPlayer player, DamageSource source) {
        return ItoMoves.blocks(player, source);
    }

    @Override
    public void removed(ServerPlayer player) {
        ItoMoves.forget(player);
    }

    @Override
    public void cancel(ServerPlayer player) {
        ItoMoves.forget(player);
    }

    @Override
    public void reset() {
        ItoMoves.clear();
    }
}
