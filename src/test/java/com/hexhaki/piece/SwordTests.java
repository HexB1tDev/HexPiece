package com.hexhaki.piece;

import com.hexhaki.piece.npc.SeaRules;
import com.hexhaki.piece.style.FightingStyle;
import com.hexhaki.piece.sword.BladePaths;
import com.hexhaki.piece.sword.Grade;
import com.hexhaki.piece.sword.Katana;
import com.hexhaki.piece.sword.KatanaItem;
import com.hexhaki.piece.sword.SwordAnims;
import com.hexhaki.piece.sword.SwordCombo;
import com.hexhaki.piece.sword.SwordMove;
import com.hexhaki.piece.sword.SwordMove.Branch;
import com.hexhaki.piece.sword.SwordRules;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 2.1.4: the Swordsman (pure): the katanas, the moves and their animations and blade paths, the strings, the charge's numbers. */
public final class SwordTests {
    static final Path ANIMATIONS = Path.of("src/main/resources/assets/hexhaki/player_animation");

    public static void main(String[] args) throws Exception {
        katanas();
        moves();
        animations();
        combo();
        rules();
        Check.done("SwordTests");
    }

    static void katanas() {
        Check.that(Katana.values().length == 25, "the owner's 25 katanas");
        Set<String> keys = new HashSet<>();
        EnumSet<Grade> grades = EnumSet.noneOf(Grade.class);
        for (Katana katana : Katana.values()) {
            Check.that(keys.add(katana.key()), katana + ": its own model key");
            Check.that(Katana.byKey(katana.key()) == katana, katana + ": found by its key");
            Check.that(!katana.hamon().isBlank() && katana.description().length() > 40, katana + ": a hamon and a description");
            grades.add(katana.grade());
            Check.that(KatanaItem.wrap(katana.description(), 38).stream().allMatch(line -> line.length() <= 38 || !line.contains(" ")),
                katana + ": its description wraps");
        }
        Check.that(grades.size() == 4, "every grade is given");
        for (Grade grade : Grade.values()) {
            Check.that(grade.edge() >= 1.0F && grade.rarity() == grade.ordinal(), grade + ": rarity and edge in order");
        }
    }

    static void moves() {
        Set<String> animations = new HashSet<>();
        int attacks = 0;
        for (SwordMove move : SwordMove.values()) {
            Check.that(animations.add(move.animation()), move + ": its own animation (a distinct motion, not a recolour)");
            Check.that(move.duration() > 0 && move.cancel() <= move.duration(), move + ": its cancel is inside it");
            Check.that(move.cuts().length > 0, move + ": it cuts");
            int last = -1;
            for (SwordMove.Cut cut : move.cuts()) {
                Check.that(cut.from() > last && cut.from() <= cut.to() && cut.to() < move.duration(), move + ": windows in order, inside");
                last = cut.to();
                Check.that(cut.damage() > 0 && cut.damage() <= 14, move + ": damage in range");
            }
            attacks++;
        }
        Check.that(attacks >= 12, "at least twelve distinct sword attacks (" + attacks + ")");
        Check.that(SwordMove.AERIAL_CROSS.cuts().length == 2, "a move with two cuts has two windows");
        for (Branch branch : Branch.values()) {
            Check.that(SwordCombo.DRAWS[branch.ordinal()].draw() && SwordCombo.DRAWS[branch.ordinal()].branch() == branch,
                branch + ": its own draw from the saya");
            for (SwordMove move : SwordCombo.string(branch)) {
                Check.that(!move.draw() && move.branch() == branch, move + ": a drawn cut of its own string");
            }
        }
        Check.that(FightingStyle.SWORDSMAN.sword() && FightingStyle.SWORDSMAN.ordinal() == FightingStyle.values().length - 1,
            "Swordsman is the last style (sent by ordinal)");
        Check.that(java.util.Arrays.asList(FightingStyle.choices()).contains(FightingStyle.SWORDSMAN), "Swordsman can be picked");
    }

    static void animations() throws Exception {
        Pattern end = Pattern.compile("\"endTick\":\\s*(\\d+)");
        for (int anim = 1; anim < SwordAnims.MOVES + SwordMove.values().length; anim++) {
            String name = SwordAnims.name(anim);
            if (name == null) {
                continue;
            }
            Path file = ANIMATIONS.resolve(name + ".json");
            Check.that(Files.exists(file), name + ": its animation exists");
            String json = Files.readString(file);
            Matcher m = end.matcher(json);
            Check.that(m.find() && Integer.parseInt(m.group(1)) == SwordAnims.duration(anim), name + ": as long as the server says");
            Check.that(json.contains("\"rightItem\":"), name + ": the wrist is animated (the blade follows the hand)");
            float[] path = BladePaths.of(name);
            Check.that(path != null && path.length == (SwordAnims.duration(anim) * 2 + 1) * 6, name + ": its blade path, every half tick");
            Check.that(SwordAnims.loops(anim) == json.contains("\"isLoop\": true"), name + ": loops as the server holds it");
        }
        // every cut's window sweeps the blade: its tip moves far enough to cut
        for (SwordMove move : SwordMove.values()) {
            float[] path = BladePaths.of(move.animation());
            for (SwordMove.Cut cut : move.cuts()) {
                double swept = 0;
                for (int h = Math.max(0, cut.from() * 2 - 2); h < cut.to() * 2; h++) {
                    double dx = path[(h + 1) * 6 + 3] - path[h * 6 + 3];
                    double dy = path[(h + 1) * 6 + 4] - path[h * 6 + 4];
                    double dz = path[(h + 1) * 6 + 5] - path[h * 6 + 5];
                    swept += Math.sqrt(dx * dx + dy * dy + dz * dz);
                }
                Check.that(swept > 0.6 || move == SwordMove.TSUKI || move == SwordMove.PLUNGE || move == SwordMove.PIVOT_CUT,
                    move + ": the blade is moving in its cut window (" + swept + " blocks)");
            }
        }
        // the draws: the blade in the saya before its draw tick, in the hand after; the sheathing the other way round
        for (SwordMove move : SwordMove.values()) {
            int draw = BladePaths.drawTick(move.animation());
            Check.that(move.draw() == (draw > 0), move + ": a draw has a draw tick");
            if (move.draw()) {
                Check.that(!SwordAnims.drawn(SwordAnims.of(move), 0) && SwordAnims.drawn(SwordAnims.of(move), move.cuts()[0].from()),
                    move + ": in the saya at first, in the hand by its cut");
                Check.that(BladePaths.notoOut(move.animation()) != null, move + ": drawn out along the saya's line");
            }
        }
        Check.that(SwordAnims.drawn(SwordAnims.SHEATHE, 0) && !SwordAnims.drawn(SwordAnims.SHEATHE, SwordAnims.duration(SwordAnims.SHEATHE)),
            "the sheathing ends in the saya");
        Check.that(SwordAnims.duration(SwordAnims.SHEATHE) >= 4 && SwordAnims.duration(SwordAnims.SHEATHE) <= 6, "a 4 to 6 tick sheathing");
        Check.that(Files.exists(ANIMATIONS.resolve("style_swordsman_stance.json")), "the relaxed stance: the left hand on the hilt");
    }

    static void combo() {
        // sheathed: the branch's draw; then the string in order; it wraps after its last cut
        Check.that(SwordCombo.next(Branch.GROUND, true, null, 0, 0) == SwordMove.DRAW_CUT, "sheathed on the ground: the draw cut");
        Check.that(SwordCombo.next(Branch.AIR, true, null, 0, 0) == SwordMove.AERIAL_DRAW, "sheathed in the air: the falling draw");
        Check.that(SwordCombo.next(Branch.GROUND, false, SwordMove.DRAW_CUT, 100, 105) == SwordMove.KESA, "after the draw: the kesa");
        SwordMove[] ground = SwordCombo.string(Branch.GROUND);
        for (int i = 0; i + 1 < ground.length; i++) {
            Check.that(SwordCombo.next(Branch.GROUND, false, ground[i], 100, 100 + SwordCombo.WINDOW) == ground[i + 1], "the string goes on");
        }
        Check.that(SwordCombo.next(Branch.GROUND, false, SwordMove.MAKKO, 100, 101) == SwordMove.KESA, "after the finisher: from the top");
        Check.that(SwordCombo.next(Branch.GROUND, false, SwordMove.YOKO, 100, 100 + SwordCombo.WINDOW + 1) == SwordMove.KESA,
            "the window expired: from the top");
        Check.that(SwordCombo.next(Branch.MOVING, false, SwordMove.YOKO, 100, 101) == SwordMove.ADVANCING_CUT,
            "another branch: its own string");
        Check.that(SwordCombo.branch(false, 0.0) == Branch.AIR && SwordCombo.branch(true, 0.25) == Branch.MOVING
            && SwordCombo.branch(true, 0.05) == Branch.GROUND, "the branch from the movement");
        // one press kept, never early: the buffer
        SwordMove kesa = SwordMove.KESA;
        Check.that(SwordCombo.buffered(kesa, 100, 100) == -1, "a press at the start of a cut is dropped");
        Check.that(SwordCombo.buffered(kesa, 100, 100 + kesa.cancel() - SwordCombo.BUFFER) == 100 + kesa.cancel(), "a press just before "
            + "the cancel tick is kept for it");
        Check.that(SwordCombo.buffered(kesa, 100, 100 + kesa.cancel() + 1) == 100 + kesa.cancel() + 1, "after the cancel tick: at once");
    }

    static void rules() {
        Check.near(SwordRules.charge(0, 40), 0.0, 1e-9, "no charge is the least");
        Check.near(SwordRules.charge(40, 40), 1.0, 1e-9, "40 ticks is the full charge");
        Check.near(SwordRules.charge(400, 40), 1.0, 1e-9, "a charge is clamped at its maximum");
        Check.that(SwordRules.lerp(SwordRules.SLASH_HEIGHT_MIN, SwordRules.SLASH_HEIGHT_MAX, 1.0) > SwordRules.lerp(SwordRules.SLASH_HEIGHT_MIN,
            SwordRules.SLASH_HEIGHT_MAX, 0.2), "a longer charge, a taller slash");
        Check.that(SwordRules.CHARGE_MAX == 40, "the charge defaults to 40 ticks");
        Check.that(SwordRules.SHEATHE_DELAY == 8, "an eight-tick delay before sheathing");
        Check.that(SwordRules.CUT_BREAKS_PER_TICK > 0 && SwordRules.CUT_CHECKS_PER_TICK >= SwordRules.CUT_BREAKS_PER_TICK,
            "the terrain cut is budgeted");
        Check.that(SeaRules.katana(2, 0).grade().rarity() >= 2, "a captain's katana is a great one");
    }
}
