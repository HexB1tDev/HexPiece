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
        galaxy();
        waterBullet();
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

    /** 2.3.7: the Galaxy Fist is Iron Fist's alone, with Ryou up. */
    static void galaxy() throws Exception {
        for (FightingStyle style : FightingStyle.values()) {
            Check.that(PieceRules.galaxyFist(style, true) == (style == FightingStyle.BRAWLER), style + ": the Galaxy Fist only for Iron Fist");
            Check.that(!PieceRules.galaxyFist(style, false), style + ": never without Ryou up");
        }
        Check.that(!PieceRules.galaxyFist(null, true), "no style chosen: no Galaxy Fist");
        String patch = java.nio.file.Files.readString(java.nio.file.Path.of("patch/PiecePatch.java"));
        Check.that(patch.contains("\"beginConvergence\", \"(Lnet/minecraft/server/level/ServerPlayer;)V\", GALAXY, \"begin\", 1}")
            && patch.contains("{CONTROLLER, \"beginConvergence\", 520, 0, 2}"), "the original's start goes through HexPiece, its mastery gone");
    }

    /** 2.3.8: the Water Bullet: five drops gathered over the raised fist, fired one a tick, homing. */
    static void waterBullet() {
        StyleMove move = StyleMove.of(FightingStyle.FISHMAN_KARATE, Slot.HEAVY);
        Check.that(move == StyleMove.FK_BULLET && move.effect() == StyleMove.Effect.BULLETS, "the signature is the Water Bullet");
        Hit[] hits = move.hits();
        Check.that(hits.length == com.hexhaki.piece.water.WaterBullet.COUNT, "five drops");
        for (int i = 0; i < hits.length; i++) {
            int fire = com.hexhaki.piece.water.WaterBullet.fireTick(i);
            Check.that(hits[i].tick() == fire, "drop " + i + " fires on its tick");
            Check.that(com.hexhaki.piece.water.WaterBullet.grown(i, fire) == 1.0, "drop " + i + " is whole before it is fired");
            Check.that(com.hexhaki.piece.water.WaterBullet.grown(i, com.hexhaki.piece.water.WaterBullet.gathers(i)) == 0.0,
                "drop " + i + " starts as mist");
            Check.that(com.hexhaki.piece.water.WaterBullet.gathers(i) >= com.hexhaki.piece.water.WaterBullet.RAISED, "it gathers once the hand is up");
        }
        double[] a = com.hexhaki.piece.water.WaterBullet.formation(0);
        double[] m = com.hexhaki.piece.water.WaterBullet.formation(2);
        double[] e = com.hexhaki.piece.water.WaterBullet.formation(4);
        Check.that(a[0] < m[0] && m[0] < e[0] && m[1] > a[1] && Math.abs(a[1] - e[1]) < 1e-9 && a[2] == m[2],
            "a flat arch across the fist, its middle highest, level across");
        Check.that(m[1] > com.hexhaki.piece.water.WaterBullet.FIST[1], "over the fist");
        for (int targets = 0; targets <= 7; targets++) {
            for (int i = 0; i < 5; i++) {
                int t = com.hexhaki.piece.water.WaterBullet.target(i, targets);
                Check.that(targets == 0 ? t == -1 : t >= 0 && t < targets, "each drop locks onto one of the foes, or none");
            }
        }
        Check.that(com.hexhaki.piece.water.WaterBullet.target(0, 3) == 0 && com.hexhaki.piece.water.WaterBullet.target(3, 3) == 0,
            "the nearest first, round again");
        double[] v = com.hexhaki.piece.water.WaterBullet.steer(new double[] {0, 0, 1}, new double[] {1, 0, 0});
        double turned = Math.toDegrees(Math.acos(v[2] / Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])));
        Check.near(turned, Math.toDegrees(com.hexhaki.piece.water.WaterBullet.TURN), 1e-6, "it turns after its target a little a tick");
        Check.near(Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]), com.hexhaki.piece.water.WaterBullet.SPEED, 1e-9, "at its speed");
        double[] straight = com.hexhaki.piece.water.WaterBullet.steer(new double[] {0, 0, 1}, new double[] {0.1, 0, 1});
        Check.that(straight[0] > 0 && Math.abs(straight[0] / straight[2] - 0.1) < 1e-9, "a small turn is made at once");
        Check.that(com.hexhaki.piece.water.WaterBullet.ahead(0, 1, 0.2, 5) && !com.hexhaki.piece.water.WaterBullet.ahead(0, 1, 5, -1),
            "it locks onto foes in front, not behind");
        double[] w = com.hexhaki.piece.water.WaterBullet.world(new double[] {1, 0, 0}, 0, 0, 0, 0);
        Check.that(w[0] < -0.99, "the user's right at yaw 0 is west (-x), as the game turns players");
    }
}
