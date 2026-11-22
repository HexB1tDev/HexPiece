package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

/**
 * 3.1.9 (the owner: "Can you do the same as we did with light and magma? Can you create the ice fruit, the Aokiji one? Design the models,
 * get the references, make sure this one is also equally as smooth. The ice is custom, not Minecraft ice shit. Like actually freeze them
 * and like blocks of ice"; "Find against GARP and all into that, to just make it as accurate as possible"): the Hie Hie no Mi, a Logia
 * the original never had (PiecePatch.uoFruit adds it to the original's fruits as HIE). Kuzan's ice as docs/handoff/hie-ref shows it,
 * against Garp at Hachinosu above all: Ice Block: Partisan's spears, Ice Time's freezing touch, the Ice Ball's prison, Ice Block:
 * Pheasant Beak, Ice Age, the ice road and the ice fist, the Ice Saber. Keys as for every fruit (Z X C V B N, the attack and use
 * buttons); the attack button is the game's own punch (client/hie/HieFx), or the saber's cut while it is out. The moves are
 * server/HieMoves; freezing is server/HieFreeze.
 */
public final class HieKit extends FruitKit {
    public static final FruitMove PARTISAN = new FruitMove("Ice Block: Partisan", "[RMB]", 0, "hie_partisan",
        "A spear of ice thrown where you look. Hold to raise spears of ice in the air round you, one after another (up to eight); let go and"
            + " they all fly at what you aim at. Every spear leaves the cold in what it strikes: three and it freezes solid.");
    public static final FruitMove ICE_TIME = new FruitMove("Ice Time", "[Z]", 0, "hie_ice_time",
        "A lunge, and you take hold of the one you look at: they freeze solid in an instant, locked in a block of ice until it shatters.");
    public static final FruitMove ICE_BALL = new FruitMove("Ice Ball", "[X]", 0, "hie_ice_ball",
        "An orb of ice gathered between your hands and thrown: where it lands a great ball of ice swallows everyone near, frozen inside it,"
            + " until it cracks and shatters.");
    public static final FruitMove PHEASANT = new FruitMove("Ice Block: Pheasant Beak", "[C]", 0, "hie_pheasant",
        "A great pheasant of ice flies where you aim, freezing everything it passes through, and bursts into spikes of ice where it lands.");
    public static final FruitMove ICE_AGE = new FruitMove("Ice Age", "[V]", 0, "hie_ice_age",
        "Your palm to the ground: a freezing wave runs out ahead of you in a wide cone, the ground and the sea frozen into a sheet of ice"
            + " with jagged ridges, and everyone it reaches frozen solid.");
    public static final FruitMove ICE_ROAD = new FruitMove("Ice Road", "[B]", 0, "hie_ice_road",
        "A road of jagged ice shoots along the ground at the one you look at and you slide along it, your fist coated in ice, and drive it"
            + " into them.");
    public static final FruitMove SABER = new FruitMove("Ice Saber", "[N]", 0, "hie_saber",
        "A sword of ice forms in your hand for twenty seconds: your attacks cut with it, and every cut leaves the cold in what it strikes."
            + " Press again to let it shatter.");
    public static final FruitMove BODY = new FruitMove("Ice Body", "passive", 0, null,
        "A Logia: blows and shots pass through you as ice, shattering and coming back together, unless they come coated in Armament Haki;"
            + " whoever strikes you bare-handed like that is frostbitten. The sea freezes under your feet as you walk on it (sneak to sink in),"
            + " and the cold cannot hurt you. The sea takes it all away.");
    public static final List<FruitMove> MOVES = List.of(PARTISAN, ICE_TIME, ICE_BALL, PHEASANT, ICE_AGE, ICE_ROAD, SABER, BODY);

    @Override
    public Fruit fruit() {
        return Fruit.HIE;
    }

    @Override
    public List<FruitMove> moves() {
        return MOVES;
    }

    /** {@code kind}: 0 a quick key down, 1 up, 2 the attack button, 3 the use button down, 4 up. */
    @Override
    public void action(ServerPlayer player, int kind, int ability, int flags) {
        switch (kind) {
            case 0 -> HieMoves.press(player, ability);
            case 1 -> HieMoves.release(player, ability);
            case 2 -> HieMoves.attack(player);
            case 3 -> HieMoves.use(player);
            case 4 -> HieMoves.useUp(player);
            default -> {
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        HieMoves.tick(player);
    }

    @Override
    public void removed(ServerPlayer player) {
        HieMoves.forget(player);
    }

    @Override
    public void cancel(ServerPlayer player) {
        HieMoves.forget(player);
    }

    /** A blow passed through the body of ice (no Haki on it): it shatters and comes back together; a bare-handed attacker is frostbitten. */
    @Override
    public void dodged(ServerPlayer player, DamageSource source) {
        HieMoves.dodged(player, source);
    }

    /** The cold cannot hurt the body of ice (freezing in powder snow, and the like). */
    @Override
    public boolean immune(ServerPlayer player, DamageSource source) {
        return source.m_269533_(net.minecraft.tags.DamageTypeTags.f_268419_);
    }

    @Override
    public void reset() {
        HieMoves.clear();
    }
}
