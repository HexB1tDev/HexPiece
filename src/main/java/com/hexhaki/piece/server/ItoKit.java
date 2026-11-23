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
        "Five strings hang from your raised fingers, then rake down over everything in front of you (whoever you aim at in the middle"
            + " of it): a long strip of ground cut in five lines, everyone on it cut.");
    public static final FruitMove OVERHEAT = new FruitMove("Overheat", "[X] hold", 0, "ito_overheat",
        "Hold to wind a red-hot rope of string in front of your pointed fist; let go to fire it as a beam that never comes back: it"
            + " explodes where it lands and its burning strings scatter in every direction. The longer you hold, the further and harder.");
    public static final FruitMove PARASITE = new FruitMove("Parasite", "[C]", 0, "ito_parasite",
        "Threads sink into the one you look at: for five seconds they cannot move on their own, and you drag them wherever you look."
            + " Press again to let go.");
    public static final FruitMove WEB = new FruitMove("Spider's Web", "[V]", 0, "ito_web",
        "A web of string spreads in front of you for six seconds: projectiles stop in it and blows from the front are caught.");
    public static final FruitMove KNIGHT = new FruitMove("Black Knight", "[B]", 0, "ito_knight",
        "You weave a clone of yourself out of string: it follows you, roams and fights on its own for thirty seconds, and it can be"
            + " killed. When it dies, or when you press again, it bursts into strings that cut everyone round it.");
    public static final FruitMove CAGE = new FruitMove("Birdcage", "[N] hold", 0, "ito_cage",
        "Hold, pointing at the sky: a steady stream of strings pours up, the cage growing the longer you hold (up to 40 blocks across)."
            + " Let go: the strings spread down into the Birdcage; its strings cut whoever touches them and no one gets out. Press again:"
            + " it slowly closes, cutting everything inside and pulling everyone to the middle; when it shuts they are cut and bleed.");
    public static final FruitMove AWAKEN = new FruitMove("Awakening", "[M]", 0, "ito_awaken",
        "The ground round you turns to string for 15 seconds: whoever stands on it is impaled by a spike of string shooting up under"
            + " them, cut and bleeding. With Armament on, a toggle (M again: off): while it is on, huge tentacles of string rise out of"
            + " the ground and the trees at every hostile creature near you; their Haki-black tips impale it, lift it into the sky and"
            + " throw it.");
    public static final FruitMove SKY = new FruitMove("Sky Road", "passive", 0, null,
        "Double-tap jump in the air: a string from your fingers catches the clouds, one at a time, for up to thirty seconds. Hold a"
            + " direction to swing from string to string like a pendulum, letting go at the top with a flip (sprint to swing harder); tap"
            + " jump to leap to a string further away; hold jump to climb steps of string like stairs; sneak to drop. Let go of every key"
            + " to hang by one hand. Touching the ground ends it, and no fall hurts you when the strings let go.");
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
            case 1 -> ItoMoves.pressUp(player, ability); // 3.4.2: Overheat and the Birdcage are held
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
