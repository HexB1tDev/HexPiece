package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

/**
 * 3.1.1 (the owner: "Now do Gura Gura, get reference gifs from white beard and black beard both"; shown the move set, "Go ahead", and
 * for the landing quake "Add it"): the Gura Gura no Mi, which the original names (Fruit.GURA, its item and lore) but gives no moves at
 * all (Fruits' empty kit). Whitebeard's and Blackbeard's quakes as the references in docs/handoff/gura-ref show them: the quake bubble
 * round the fist, the air cracking like glass where it strikes, the shock racing out, the ground torn into craters and rifts, the
 * tsunami, the island shaken. Keys as the owner set them for every fruit (Z X C V B, the attack and use buttons); the attack button is
 * the game's own punch (client/gura/GuraFx gives it back, as the Light fruit's since 3.0.4). The moves are server/GuraMoves.
 */
public final class GuraKit extends FruitKit {
    public static final FruitMove KAISHIN = new FruitMove("Kaishin", "[RMB]", 0, "gura_kaishin",
        "Sea Quake. A punch into the air with the quake bubble on the fist: the air cracks like glass where it strikes and a shockwave"
            + " races out ahead, throwing everyone it meets. Hold to gather the quake on the drawn-back fist: let go and the air shatters,"
            + " the shock goes much further and tears up the ground, and those it hits go down.");
    public static final FruitMove SHIMA = new FruitMove("Shima Yurashi", "[Z] hold", 0, "gura_shima",
        "Island Shaker. Both arms thrown out to the sides, the air cracking at both fists, and you take hold of the air: while you hold it"
            + " the ground quakes all round you, everyone near staggering, the world tilting. Let go and you pull the island over: everyone"
            + " round you is thrown out and knocked down.");
    public static final FruitMove KABUTOWARI = new FruitMove("Kabutowari", "[X]", 0, "gura_kabutowari",
        "Helmet Splitter. A leap at the one you look at and the quake fist brought down on their head: the air cracks over them, they are"
            + " slammed into the ground, a crater opens and everyone near is thrown up. With no one there, you dive fist first into the"
            + " ground where you look.");
    public static final FruitMove TSUNAMI = new FruitMove("Tsunami", "[C]", 0, "gura_tsunami",
        "Both fists driven out and the sea answers: a wall of water rises ahead of you and rolls on, carrying everyone it meets and"
            + " dumping them down when it breaks.");
    public static final FruitMove GEKISHIN = new FruitMove("Gekishin", "[V] hold", 0, "gura_gekishin",
        "Violent Quake. Gather the bubble on the drawn-back fist; let go and you are on the one you look at, the quake driven point-blank"
            + " into them: the shock bursts out of their back, cracking the air behind them, and they are sent flying through walls. Held"
            + " longer, it hits harder.");
    public static final FruitMove SPLITTER = new FruitMove("Ground Splitter", "[B]", 0, "gura_split",
        "Both fists into the ground: a rift tears open ahead of you, block by block, and everyone along it is thrown into the air. The"
            + " ground grows back.");
    public static final FruitMove TREMOR = new FruitMove("Tremor Body", "passive", 0, null,
        "You never take a fall: where you land hard, the ground quakes round your feet and throws back whoever stands near. The sea takes"
            + " it all away.");
    public static final List<FruitMove> MOVES = List.of(KAISHIN, SHIMA, KABUTOWARI, TSUNAMI, GEKISHIN, SPLITTER, TREMOR);

    @Override
    public Fruit fruit() {
        return Fruit.GURA;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind}: 0 a quick key down, 1 up, 2 the attack button, 3 the use button down, 4 up. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        switch (kind) {
            case 0 -> GuraMoves.press(player, ability);
            case 1 -> GuraMoves.release(player, ability);
            case 3 -> GuraMoves.use(player);
            case 4 -> GuraMoves.useUp(player);
            default -> {
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        GuraMoves.tick(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        GuraMoves.forget(player);
    }

    @Override
    public void cancel(ServerPlayer player) {
        GuraMoves.forget(player);
    }

    /** The landing quake's body takes no fall (and no wall it is thrown into by its own leap). */
    @Override
    public boolean immune(ServerPlayer player, DamageSource source) {
        return source.m_276093_(DamageTypes.f_268671_) || source.m_276093_(DamageTypes.f_268576_);
    }

    @Override
    public void reset() {
        GuraMoves.clear();
    }
}
