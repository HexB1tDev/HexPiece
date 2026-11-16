package com.hexhaki.piece;

import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.style.Hit;
import com.hexhaki.piece.style.Slot;
import com.hexhaki.piece.style.StyleCombo;
import com.hexhaki.piece.style.StyleMove;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 2.1.1: the fighting styles' movesets, their animations, the string's chaining, renown and mastery numbers. */
public final class StyleTests {
    static final Path ANIMATIONS = Path.of("src/main/resources/assets/hexhaki/player_animation");

    public static void main(String[] args) throws Exception {
        movesets();
        animations();
        combo();
        rules();
        Check.done("StyleTests");
    }

    static void movesets() {
        for (FightingStyle style : FightingStyle.choices()) {
            if (style.sword()) {
                continue; // 2.1.4: the sword's moves are SwordMove's (SwordTests)
            }
            for (Slot slot : Slot.values()) {
                StyleMove move = StyleMove.of(style, slot);
                Check.that(move != null, style + " has a move for " + slot);
                Check.that(move.duration() > 0 && move.cancel() <= move.duration(), move + ": its cancel is inside it");
                Check.that(slot.string() == (move.cooldown() == 0), move + ": string blows have no cooldown, the others do");
                Check.that(slot.string() ? move.mastery() == 0 : true, move + ": the string is open from the start");
                Check.that(slot != Slot.HEAVY || move.mastery() == 0, move + ": the signature is open from the start");
                Check.that(slot != Slot.SPECIAL_A || move.mastery() > 0 && move.mastery() < 150, move + ": the first special opens early");
                Check.that(slot != Slot.SPECIAL_B || move.mastery() >= 150 && move.mastery() <= 400, move + ": the second special opens later");
                int last = -1;
                for (Hit hit : move.hits()) {
                    Check.that(hit.tick() > 0 && hit.tick() < move.duration(), move + ": every blow lands inside the move");
                    Check.that(hit.tick() >= last, move + ": blows in order");
                    last = hit.tick();
                    Check.that(hit.damage() > 0 && hit.damage() <= 14, move + ": damage in range");
                    Check.that(hit.reach() > 0 && hit.reach() <= 25, move + ": reach in range");
                }
                boolean defensive = move.effect() == StyleMove.Effect.GUARD || move.effect() == StyleMove.Effect.COUNTER;
                Check.that(defensive || move.hits().length > 0, move + ": an attack has blows");
                Check.that(move.animation().startsWith("style_" + style.id() + "_"), move + ": its animation is named for its style");
            }
            StyleMove finisher = StyleMove.of(style, Slot.M1_4);
            for (Hit hit : finisher.hits()) {
                Check.that(hit.weight() == Hit.Weight.FINISHER, style + ": the fourth blow is a finisher");
            }
        }
        Check.that(StyleMove.of(FightingStyle.NONE, Slot.M1_1) == null, "bare hands have no moves");
    }

    static void animations() throws Exception {
        Pattern end = Pattern.compile("\"endTick\":\\s*(\\d+)");
        for (StyleMove move : StyleMove.values()) {
            Path file = ANIMATIONS.resolve(move.animation() + ".json");
            Check.that(Files.exists(file), move + ": its animation " + file + " exists");
            String json = Files.readString(file);
            Matcher matcher = end.matcher(json);
            Check.that(matcher.find() && Integer.parseInt(matcher.group(1)) == move.duration(), move + ": its animation lasts as long as the move");
            for (Hit hit : move.hits()) {
                Check.that(json.contains("\"tick\": " + hit.tick() + ","), move + ": its animation has a keyframe on its hit tick " + hit.tick());
            }
        }
        Check.that(Files.exists(ANIMATIONS.resolve("style_fishman_karate_counter_strike.json")), "the counter's answering palm exists");
        // 2.1.2: combat mode's stances: one per style, a loop, the arms only (walking, the body and the head stay the game's)
        Pattern part = Pattern.compile("\"(body|torso|head|rightLeg|leftLeg|rightItem|leftItem)\":");
        Check.that(FightingStyle.FISHMAN_KARATE.stance() == null && FightingStyle.BRAWLER.stance() == null,
            "Fishman Karate and Iron Fist hold no stance: no attacking pose while idle, walking or sprinting");
        for (FightingStyle style : FightingStyle.choices()) {
            if (style.stance() == null) {
                Check.that(!Files.exists(ANIMATIONS.resolve("style_" + style.id() + "_stance.json")), style + ": no stance file left over");
                continue;
            }
            Path file = ANIMATIONS.resolve(style.stance() + ".json");
            Check.that(Files.exists(file), style + ": its combat stance " + file + " exists");
            String json = Files.readString(file);
            Check.that(json.contains("\"isLoop\": true"), style + ": its stance loops");
            Check.that(json.contains("\"rightArm\":") && json.contains("\"leftArm\":"), style + ": its stance holds both arms");
            Check.that(!part.matcher(json).find(), style + ": its stance moves the arms only");
        }
        // 2.1.3: the punches raise the arms only for the blow (no tick-0 pose: they ease in from the player's own arms) and leave the
        // legs to the game
        Pattern legs = Pattern.compile("\"(rightLeg|leftLeg)\":");
        for (StyleMove move : StyleMove.values()) {
            if (move.style() != FightingStyle.FISHMAN_KARATE && move.style() != FightingStyle.BRAWLER) {
                continue;
            }
            String json = Files.readString(ANIMATIONS.resolve(move.animation() + ".json"));
            Check.that(!json.contains("\"tick\": 0,"), move + ": no pose forced on tick 0, the arms rise from where they are");
            boolean legged = move == StyleMove.FK_KNEE || move == StyleMove.FK_WAVE || move == StyleMove.IF_CHARGE;
            Check.that(legged == legs.matcher(json).find(), move + (legged ? ": its legs are part of the blow" : ": the legs stay the game's"));
        }
    }

    static void combo() {
        StyleMove first = StyleMove.of(FightingStyle.BRAWLER, Slot.M1_1);
        Check.that(StyleCombo.startFor(null, 0, 100) == 100, "a first press starts at once");
        StyleMove hook = StyleMove.of(FightingStyle.BLACK_LEG, Slot.M1_2);
        Check.that(StyleCombo.startFor(hook, 100, 101) == -1, "a press early in a long blow's wind-up is dropped");
        Check.that(StyleCombo.startFor(first, 100, 101) == 100 + first.cancel(), "a short jab's press is buffered to its cancel tick");
        long cancel = 100 + first.cancel();
        Check.that(StyleCombo.startFor(first, 100, cancel - 2) == cancel, "a press just before the cancel tick waits for it (buffered)");
        Check.that(StyleCombo.startFor(first, 100, cancel + 1) == cancel + 1, "a press after the cancel tick starts at once");
        Check.that(StyleCombo.next(first, 100, cancel) == Slot.M1_2, "the string goes on");
        Check.that(StyleCombo.next(first, 100, 100 + first.duration() + StyleCombo.WINDOW + 1) == Slot.M1_1, "too late: it starts over");
        StyleMove finisher = StyleMove.of(FightingStyle.BRAWLER, Slot.M1_4);
        Check.that(StyleCombo.next(finisher, 100, 200) == Slot.M1_1, "after the finisher it starts over");
        Check.that(StyleCombo.startFor(finisher, 100, 100 + finisher.duration()) == -1, "the finisher rests before a new string");
    }

    static void rules() {
        Check.near(PieceRules.masteryDamage(0), 1.0, 1e-6, "no mastery, plain damage");
        Check.near(PieceRules.masteryDamage(1000), 1.6, 1e-6, "full mastery, 1.6x");
        Check.that(PieceRules.title(Faction.PIRATE, 0).equals("Nobody") && PieceRules.title(Faction.PIRATE, 150_000_000L).equals("Supernova"),
            "pirate titles by bounty");
        Check.that(PieceRules.title(Faction.MARINE, 0).equals("Seaman Recruit") && PieceRules.title(Faction.MARINE, 7000).equals("Fleet Admiral"),
            "marine ranks by merit");
        Check.that(PieceRules.nextTitle(Faction.PIRATE, 150_000_000L) == 300_000_000L, "the next title");
        Check.that(PieceRules.renownFor(Faction.PIRATE, 2) > PieceRules.renownFor(Faction.PIRATE, 0), "a captain is worth more");
        Check.that(PieceRules.berries(1_234_000L).equals("1,234,000"), "berries print like a poster");
        Check.that(Faction.PIRATE.enemy() == Faction.MARINE && Faction.MARINE.enemy() == Faction.PIRATE, "the two sides");
    }
}
