package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

/**
 * 3.1.3 (the owner: "Now do the same as you did with the Gurunomi for the Magunomi, the Magma fruit that is already in the game but has
 * no abilities as well. Search for Akainu's feats and gifts for reference and make his abilities. Make sure not to animate the feet";
 * shown the move set, "Go ahead", and the passive, "Add it"): the Magu Magu no Mi, which the original names (Fruit.MAGU, a Logia, its
 * item and lore) but gives no moves. Akainu's magma as docs/handoff/magu-ref shows it: Dai Funka's giant fist, Meigo's fist through the
 * chest, Ryusei Kazan's rain of magma, the hounds of Inugami Guren, the ground boiling and bursting, the body melting into magma. Keys
 * as for every fruit (Z X C V B, the attack and use buttons); the attack button is the game's own punch (client/magu/MaguFx). The moves
 * are server/MaguMoves.
 */
public final class MaguKit extends FruitKit {
    public static final FruitMove DAI_FUNKA = new FruitMove("Dai Funka", "[RMB]", 0, "magu_dai_funka",
        "Great Eruption. A fist of magma shot out of your fist, a trail of fire and black smoke behind it, bursting where it lands. Hold to"
            + " swell your fist into a giant fist of magma; let go and it flies, exploding where it lands and leaving the ground burning.");
    public static final FruitMove MEIGO = new FruitMove("Meigo", "[Z]", 0, "magu_meigo",
        "Hell Hound. A lunge, and your magma fist driven straight through the one you look at: the magma bursts out of their back, they"
            + " burn and are thrown down.");
    public static final FruitMove RYUSEI = new FruitMove("Ryusei Kazan", "[X] hold", 0, "magu_ryusei",
        "Meteor Volcano. A fist of magma punched up into the sky, and while you hold it, fists of magma rain down like meteors where you"
            + " look, bursting into fire and leaving the ground burning.");
    public static final FruitMove INUGAMI = new FruitMove("Inugami Guren", "[C]", 0, "magu_inugami",
        "Crimson Hound. A giant hound's head of magma surges out along the ground, jaws snapping, mauling and carrying everyone it bites,"
            + " and bursts into a flood of magma.");
    public static final FruitMove BAKURETSU = new FruitMove("Bakuretsu Kazan", "[V]", 0, "magu_bakuretsu",
        "Exploding Volcano. Both fists into the ground: the ground round you boils into magma and bursts in geysers that throw your foes"
            + " up, and it keeps burning for a while.");
    public static final FruitMove FLOW = new FruitMove("Magma Flow", "[B]", 0, "magu_flow",
        "You melt into a stream of magma and slide along the ground where you look, burning whoever it runs over, and rise out of it in an"
            + " eruption.");
    public static final FruitMove BODY = new FruitMove("Magma Body", "passive", 0, null,
        "A Logia: blows and shots pass through you as magma unless they come coated in Armament Haki, and whoever strikes you bare-handed"
            + " like that is burned. Fire and lava cannot hurt you. The sea takes it all away.");
    public static final List<FruitMove> MOVES = List.of(DAI_FUNKA, MEIGO, RYUSEI, INUGAMI, BAKURETSU, FLOW, BODY);

    @Override
    public Fruit fruit() {
        return Fruit.MAGU;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind}: 0 a quick key down, 1 up, 2 the attack button, 3 the use button down, 4 up. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        switch (kind) {
            case 0 -> MaguMoves.press(player, ability);
            case 1 -> MaguMoves.release(player, ability);
            case 3 -> MaguMoves.use(player);
            case 4 -> MaguMoves.useUp(player);
            default -> {
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        MaguMoves.tick(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        MaguMoves.forget(player);
    }

    @Override
    public void cancel(ServerPlayer player) {
        MaguMoves.forget(player);
    }

    /** A blow passed through the body of magma (no Haki on it): whoever struck it bare-handed is burned. */
    @Override
    public void dodged(ServerPlayer player, DamageSource source) {
        MaguMoves.dodged(player, source);
    }

    /** The passive (the owner: "Add it"): fire and lava never hurt the magma body. */
    @Override
    public boolean immune(ServerPlayer player, DamageSource source) {
        return source.m_269533_(net.minecraft.tags.DamageTypeTags.f_268745_);
    }

    @Override
    public void reset() {
        MaguMoves.clear();
    }
}
