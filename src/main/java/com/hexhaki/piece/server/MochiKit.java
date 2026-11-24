package com.hexhaki.piece.server;

import com.hexhaki.fruit.Fruit;
import com.hexhaki.fruit.FruitKit;
import com.hexhaki.fruit.FruitMove;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

/**
 * 3.5.5 (the owner: "Next research katakuris mochi fruit and add it ... Legendary fruit", the keys asked and accepted: docs/handoff 8zzo):
 * Charlotte Katakuri's Mochi Mochi no Mi, a Special Paramecia. The moves are server/MochiMoves, the art mochi/MochiArt and MochiDraw.
 * The Awakening on M is a mode: while it is on, Z X C V B are its own five techniques ({@link #AWAKENED}).
 */
public final class MochiKit extends FruitKit {
    public static final FruitMove PUNCH = new FruitMove("Mochi Punch", "[LMB]", 0, null,
        "Your arm stretches out as mochi and its fist slams whoever is in front of you, far further than a fist reaches; one arm, then"
            + " the other.");
    public static final FruitMove TSUKI = new FruitMove("Mochi Tsuki", "[RMB] hold", 0, "mochi_tsuki",
        "Hold: your arm twists itself up round your trident Mogura; let go to thrust it out as a drill of mochi that pierces everyone in a"
            + " long line. The longer you hold, the further and harder.");
    public static final FruitMove YANAGI = new FruitMove("Yanagi Mochi", "[Z]", 0, "mochi_yanagi",
        "Your leg splits into tendrils of mochi that stamp the ground in front of you, over and over: everyone there is trampled.");
    public static final FruitMove KAKU = new FruitMove("Kaku Mochi", "[X]", 0, "mochi_kaku",
        "Your arms rise and harden into square blocks of mochi, then slam down in front of you: everyone under them is crushed and"
            + " knocked down.");
    public static final FruitMove YAKI = new FruitMove("Yaki Mochi", "[C]", 0, "mochi_yaki",
        "Your forearm swells with pressure, then bursts: its fist flies out on a jet of fire, fast and far, and explodes where it lands,"
            + " setting it alight.");
    public static final FruitMove ZANGIRI = new FruitMove("Zan Giri Mochi", "[V]", 0, "mochi_zangiri",
        "Your whole body becomes a giant wheel of mochi and rolls forward; the first one you meet is snagged by a sticky arm and slammed"
            + " down by a giant spiked club.");
    public static final FruitMove ANEMONE = new FruitMove("Mochi Anemone", "[B] hold", 0, "mochi_anemone",
        "Hold: rings of mochi float round you and arms burst out of them, punching everyone near you, every way at once.");
    public static final FruitMove STICKY = new FruitMove("Sticky Mochi", "[N]", 0, "mochi_sticky",
        "Throw a glob of sticky mochi: whoever it hits is glued where they stand for three seconds; landing on the ground it stays a while,"
            + " sticking whoever steps in it. Shots flying into it stick fast.");
    public static final FruitMove AWAKEN = new FruitMove("Awakening", "[M]", 0, "mochi_awaken",
        "The ground round you turns to mochi for thirty seconds and Z X C V B become your awakened techniques: Nagare Mochi, Kagami Mochi,"
            + " Amadare Mochi, Muso Donuts and Mochi Ginkaku. Press again to end it.");
    public static final FruitMove DODGE = new FruitMove("Future Sight", "Shift x2", 0, "mochi_dodge",
        "Tap sneak twice: for three seconds every blow aimed at you passes through a hole that opens in your mochi body, then it needs"
            + " twenty seconds to come back.");
    public static final FruitMove NAGARE = new FruitMove("Nagare Mochi", "[Z]", 0, "mochi_nagare",
        "Awakened: the ground in front of you becomes a great wave of mochi rolling forward; everyone it reaches is engulfed and held fast.");
    public static final FruitMove KAGAMI = new FruitMove("Kagami Mochi", "[X]", 0, "mochi_kagami",
        "Awakened: two walls of mochi rise out of the ground either side of the one you aim at and slam together, crushing whoever is"
            + " between them.");
    public static final FruitMove AMADARE = new FruitMove("Amadare Mochi", "[C]", 0, "mochi_amadare",
        "Awakened: a mass of mochi gathers in the sky over the place you aim at and rains down tendrils on everyone under it.");
    public static final FruitMove DONUTS = new FruitMove("Muso Donuts", "[V]", 0, "mochi_donuts",
        "Awakened: rings of mochi appear in the air round you and Chikara Mochi, giant fists, punch out of them one after another at the"
            + " one you aim at, each blow exploding where it lands.");
    public static final FruitMove GINKAKU = new FruitMove("Mochi Ginkaku", "[B]", 0, "mochi_ginkaku",
        "Awakened: a dome of mochi rises over the place you aim at, trapping everyone in it; it squeezes them as long as it stands and"
            + " crushes them as it falls in.");
    /** The moves as the panel and the Ability Book list them; while awakened, the awakened set in Z to B's places. */
    public static final List<FruitMove> MOVES = List.of(PUNCH, TSUKI, YANAGI, KAKU, YAKI, ZANGIRI, ANEMONE, STICKY, AWAKEN, DODGE);
    public static final List<FruitMove> AWAKENED = List.of(PUNCH, TSUKI, NAGARE, KAGAMI, AMADARE, DONUTS, GINKAKU, STICKY, AWAKEN, DODGE);
    /** Every move (the energy list, the tests). */
    public static final List<FruitMove> ALL = List.of(PUNCH, TSUKI, YANAGI, KAKU, YAKI, ZANGIRI, ANEMONE, STICKY, AWAKEN, DODGE, NAGARE, KAGAMI,
        AMADARE, DONUTS, GINKAKU);

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
            case 3 -> MochiMoves.use(player);
            case 4 -> MochiMoves.useUp(player);
            default -> {
            }
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        MochiMoves.tick(player);
    }

    /** Future Sight: while its window is open every blow passes through a hole in the mochi body (the original asks; true stops it). */
    @Override
    public boolean blocks(ServerPlayer player, DamageSource source) {
        return MochiMoves.blocks(player, source);
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
