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
import com.hexhaki.piece.sword.SwordSounds;
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
        koiguchi();
        bigSlash();
        wave();
        sounds();
        dragonSounds();
        holster();
        drawView();
        Check.done("SwordTests");
    }

    static void katanas() {
        Check.that(Katana.values().length == 27, "the owner's 25 katanas and 2.2.7's two legendary blades");
        // 2.2.7: Gryphon and Yoru, the two strongest blades, Yoru the strongest; Yoru on the back, never black, the longest reach
        float best = 0.0F;
        for (Katana katana : Katana.values()) {
            if (!katana.legend()) {
                best = Math.max(best, katana.edge());
                Check.that(katana.edge() == katana.grade().edge() && katana.carry() == Katana.Carry.HIP && katana.blackens(),
                    katana + ": a katana keeps its grade's edge, its saya at the hip, and Armament blackens it");
            }
        }
        Check.that(Katana.GRYPHON.edge() > best && Katana.YORU.edge() > Katana.GRYPHON.edge(), "the two strongest, Yoru the strongest");
        Check.that(Katana.YORU.carry() == Katana.Carry.BACK && Katana.GRYPHON.carry() == Katana.Carry.HIP, "Yoru on the back, Gryphon at the hip");
        Check.that(!Katana.YORU.blackens() && Katana.GRYPHON.blackens(), "Yoru never turns black");
        Check.that(Katana.YORU.reach() > Katana.GRYPHON.reach() && Katana.GRYPHON.reach() >= 0.0, "Yoru reaches furthest");
        reach();
        Check.that(Katana.YORU.ordinal() == 26 && Katana.GRYPHON.ordinal() == 25, "new blades at the end (saved by key, sent by ordinal)");
        for (int rank = 0; rank < 3; rank++) {
            for (int pick = 0; pick < 60; pick++) {
                Check.that(!SeaRules.katana(rank, pick).legend(), "no patrol carries a legendary blade");
            }
        }
        // 2.3.2: the animation names (the owner: "Just copy. They're all the same."): every blade plays the katanas' one set; Yoru
        // alone has its own draw, sheathing and draw into the charge, off its back and onto it
        Check.that("sword_draw_yoru".equals(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.DRAW, Katana.YORU))
            && "sword_sheathe_yoru".equals(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.SHEATHE, Katana.YORU))
            && "sword_charge_draw_yoru".equals(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.CHARGE_DRAW,
                Katana.YORU))
            && "sword_draw".equals(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.DRAW, Katana.GRYPHON))
            && "sword_guard".equals(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.GUARD, Katana.YORU))
            && "sword_draw".equals(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.DRAW, Katana.values()[0]))
            && "sword_kesa".equals(com.hexhaki.piece.sword.SwordAnims.name(
                com.hexhaki.piece.sword.SwordAnims.of(com.hexhaki.piece.sword.SwordMove.KESA), Katana.GRYPHON))
            && "sword_kesa".equals(com.hexhaki.piece.sword.SwordAnims.name(
                com.hexhaki.piece.sword.SwordAnims.of(com.hexhaki.piece.sword.SwordMove.KESA), Katana.YORU)),
            "one set for every blade, Yoru's own off its back");
        for (String name : new String[] {"sword_draw_yoru", "sword_sheathe_yoru", "sword_charge_draw_yoru"}) {
            Check.that(com.hexhaki.piece.sword.BladePaths.of(name) != null, name + ": its blade path is written");
        }
        Check.that(com.hexhaki.piece.sword.BladePaths.drawTick("sword_draw_yoru") > 0
            && com.hexhaki.piece.sword.BladePaths.sheathTick("sword_sheathe_yoru") > 0, "Yoru's draw and sheathe hand it over mid-way");
        Check.that(com.hexhaki.piece.sword.SwordRules.drawTicks(Katana.GRYPHON) == com.hexhaki.piece.sword.SwordRules.DRAW_TICKS
            && com.hexhaki.piece.sword.SwordRules.sheatheTicks(Katana.GRYPHON) == com.hexhaki.piece.sword.SwordRules.SHEATHE_TICKS,
            "Gryphon's saber is drawn and sheathed as a katana is");
        // 2.3.2: each blade slides out of its own saya along its own curve (sword/SayaSlide, fitted from its mesh)
        for (Katana katana : Katana.values()) {
            float[] home = com.hexhaki.piece.sword.SayaSlide.at(katana, 0.0F);
            Check.that(Math.abs(home[0]) < 0.05F && Math.abs(home[1]) < 0.05F && Math.abs(home[2]) < 0.05F, katana + ": home in its saya");
            if (katana.carry() == Katana.Carry.HIP) {
                float[] out = com.hexhaki.piece.sword.SayaSlide.at(katana, 10.0F);
                Check.that(out[2] < -9.0F && out[2] > -10.5F && out[0] > 0.0F, katana + ": drawn out of the mouth, turning with its curve");
            }
        }
        Check.that(com.hexhaki.piece.sword.SayaSlide.at(Katana.GRYPHON, 10.0F)[0] > 2.0F * com.hexhaki.piece.sword.SayaSlide.at(Katana.values()[0], 10.0F)[0],
            "Gryphon's saber curves far harder than a katana (the owner: its pull-out was broken)");
        // the game's back frame and the animations' (tools/sword_rig.py) are the same, or a draw would not close on the handle
        try {
            String rig = Files.readString(Path.of("tools/sword_rig.py"));
            Matcher guard = Pattern.compile("BACK_GUARD = \\(([-0-9.]+), ([-0-9.]+), ([-0-9.]+)\\)").matcher(rig);
            Matcher tilt = Pattern.compile("BACK_TILT = ([-0-9.]+)").matcher(rig);
            Matcher lean = Pattern.compile("BACK_LEAN = ([-0-9.]+)").matcher(rig);
            Check.that(guard.find() && tilt.find() && lean.find(), "the rig's back frame is found");
            Check.near(Double.parseDouble(lean.group(1)), com.hexhaki.piece.sword.BackCarry.LEAN, 1e-9, "the back lean agrees with the rig");
            for (int i = 0; i < 3; i++) {
                Check.near(Double.parseDouble(guard.group(i + 1)), com.hexhaki.piece.sword.BackCarry.GUARD[i], 1e-9, "the back frame agrees with the rig");
            }
            Check.near(Double.parseDouble(tilt.group(1)), com.hexhaki.piece.sword.BackCarry.TILT, 1e-9, "the back tilt agrees with the rig");
        } catch (java.io.IOException failure) {
            throw new AssertionError("tools/sword_rig.py unreadable", failure);
        }
        // the ropes: closed tubes round the torso, over the blade where they cross it, never inside the body
        java.util.List<com.hexhaki.piece.sword.BackCarry.Quad> ropes = com.hexhaki.piece.sword.BackCarry.ropes();
        Check.that(ropes.size() > 200, "two ropes (" + ropes.size() + " quads)");
        for (com.hexhaki.piece.sword.BackCarry.Quad quad : ropes) {
            for (int k = 0; k < 4; k++) {
                float x = quad.xyz()[k * 3];
                float y = quad.xyz()[k * 3 + 1];
                float z = quad.xyz()[k * 3 + 2];
                Check.that(Math.abs(x) > 4.2F || Math.abs(z) > 2.2F || y < -0.1F, "a rope never goes through the body (" + x + ", " + y + ", " + z + ")");
            }
        }
        for (java.util.List<double[]> rope : java.util.List.of(com.hexhaki.piece.sword.BackCarry.baldric(), com.hexhaki.piece.sword.BackCarry.waist())) {
            boolean crosses = false;
            for (double[] p : rope) {
                double[] blade = com.hexhaki.piece.sword.BackCarry.fromBlade(p[0], p[1]);
                if (p[2] > 0 && blade[0] < 0.9) {
                    crosses = true;
                    Check.that(p[2] - com.hexhaki.piece.sword.BackCarry.ROPE > blade[1] + 0.3,
                        "a rope lies over the blade where it crosses it, not through it (" + p[2] + " over " + blade[1] + ")");
                }
            }
            Check.that(crosses, "each rope crosses the blade on the back (it holds it)");
        }
        Set<String> keys = new HashSet<>();
        EnumSet<Grade> grades = EnumSet.noneOf(Grade.class);
        for (Katana katana : Katana.values()) {
            Check.that(keys.add(katana.key()), katana + ": its own model key");
            Check.that(!katana.kindLine().isBlank(), katana + ": its tooltip's second line");
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

    /**
     * 2.4.5 (the owner: "Different swords should have different range (Yoru is the biggest)"): every blade its own reach, by its
     * length and its grade, without an animation of its own: Yoru furthest, Gryphon next, past every graded blade; each grade's blades
     * further on average than the grade below; none so short that a cut ends before the path's own blade (the sweep still reaches past
     * the tip); the numbers the release notes give.
     */
    static void reach() {
        double graded = 0.0;
        for (Katana katana : Katana.values()) {
            if (!katana.legend()) {
                graded = Math.max(graded, katana.reach());
                Check.that(SwordRules.EXTEND + katana.reach() > 0.5, katana + ": its cut still reaches past its point");
                Check.that(katana.tip() > 15.0F && katana.tip() < 30.0F, katana + ": a katana's length");
            }
        }
        Check.that(Katana.GRYPHON.reach() > graded && Katana.YORU.reach() > Katana.GRYPHON.reach() + 0.5,
            "Gryphon past every graded blade, Yoru far past Gryphon");
        double previous = -1.0;
        for (Grade grade : Grade.values()) {
            double sum = 0.0;
            int count = 0;
            for (Katana katana : Katana.values()) {
                if (!katana.legend() && katana.grade() == grade) {
                    sum += katana.reach();
                    count++;
                }
            }
            Check.that(count > 0 && sum / count > previous, grade + ": its blades reach further on average than the grade below");
            previous = sum / count;
        }
        // 2.6.2: the paths are written with the 1.25 times bigger katana (5.25 px longer): Yoru's past them is less, its reach the same
        Check.that(Math.abs(Katana.YORU.reach() - 1.55) < 0.02 && Math.abs(Katana.GRYPHON.reach() - 0.855) < 0.02,
            "Yoru +1.55 and Gryphon +0.855 blocks past the 2.6.2 paths (" + Katana.YORU.reach() + ", " + Katana.GRYPHON.reach() + ")");
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
        // 2.2.1 (the owner: "the sheath and unsheath is too fast ... Don't make the blade usable until it's fully unsheathed ... Unless
        // it's a quick draw"): a slow sheathing, a slow draw that ends drawn, both a second or so; the quick draw as fast as ever
        Check.that(SwordAnims.duration(SwordAnims.SHEATHE) >= 16 && SwordAnims.duration(SwordAnims.SHEATHE) <= 24, "a slow sheathing, about a second");
        Check.that(SwordAnims.duration(SwordAnims.DRAW) == SwordRules.DRAW_TICKS && SwordRules.DRAW_TICKS >= 24 && SwordRules.DRAW_TICKS <= 32,
            "a slow draw, about a second and a half");
        // 2.3.2 (the owner: "The pull out animation is way too fast on some swords, yoru should be slower than all")
        Check.that(SwordRules.drawTicks(Katana.YORU) > SwordRules.drawTicks(Katana.GRYPHON)
            && SwordRules.drawTicks(Katana.GRYPHON) == SwordRules.drawTicks(Katana.values()[0])
            && SwordRules.drawTicks(Katana.values()[0]) == SwordRules.DRAW_TICKS && SwordRules.drawTicks(null) == SwordRules.DRAW_TICKS,
            "Yoru draws slowest of all; Gryphon as a katana (they share the draw)");
        Check.that(SwordRules.sheatheTicks(Katana.YORU) > SwordRules.sheatheTicks(Katana.GRYPHON)
            && SwordRules.sheatheTicks(Katana.GRYPHON) == SwordRules.SHEATHE_TICKS, "and sheathes likewise");
        // 2.3.2: every blade's every animation exists, as long as the server holds it, its blade path written; a legendary blade
        // never plays a draw from a saya (the owner, of Yoru's draw cut: "HERE IT IS the bug": on the back and in the hand at once)
        for (Katana katana : new Katana[] {Katana.values()[0], Katana.GRYPHON, Katana.YORU}) {
            for (int anim = 1; anim < SwordAnims.MOVES + SwordMove.values().length; anim++) {
                String name = SwordAnims.name(anim, katana);
                if (name == null) {
                    continue;
                }
                Path file = ANIMATIONS.resolve(name + ".json");
                Check.that(Files.exists(file), name + ": its animation exists");
                Matcher m = end.matcher(Files.readString(file));
                Check.that(m.find(), name + ": its length");
                int length = Integer.parseInt(m.group(1));
                if (name.equals(SwordAnims.name(anim) + SwordAnims.suffix(anim, katana))) {
                    Check.that(length == SwordAnims.duration(anim, katana), name + ": as long as the server holds it");
                }
                float[] path = BladePaths.of(name);
                Check.that(path != null && path.length == (length * 2 + 1) * 6, name + ": its blade path, every half tick");
                if (katana.legend()) {
                    Check.that(name.endsWith("_yoru") == (katana == Katana.YORU && SwordAnims.back(anim)),
                        name + ": the shared set, Yoru's own off its back");
                    boolean draws = anim == SwordAnims.DRAW || katana == Katana.YORU && anim == SwordAnims.CHARGE_DRAW;
                    Check.that(draws == (BladePaths.drawTick(name) > 0), name + ": a legendary blade is drawn only by its own draw");
                }
            }
            Check.that(Files.exists(ANIMATIONS.resolve(SwordAnims.idle(katana) + ".json")), katana + ": its idle");
        }
        Check.that(!SwordAnims.drawn(SwordAnims.DRAW, 0) && !SwordAnims.drawn(SwordAnims.DRAW, BladePaths.drawTick("sword_draw") - 1)
            && SwordAnims.drawn(SwordAnims.DRAW, SwordRules.DRAW_TICKS), "the draw: in the saya until its point leaves the mouth, in the hand at its end");
        Check.that(BladePaths.drawTick("sword_draw") >= 12, "the blade comes out slowly, not at once");
        Check.that(BladePaths.notoOut("sword_draw") != null, "drawn out along the saya's line");
        Check.that(BladePaths.drawTick("sword_quick_draw") <= 3, "the quick draw is out of the saya as fast as ever");
        Check.that(Files.exists(ANIMATIONS.resolve("sword_draw.json")), "the slow draw's animation");
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

    /** 2.1.5: the thumb on the guard while standing ready. */
    static void koiguchi() {
        Check.that(com.hexhaki.piece.sword.Koiguchi.push(-1) == 0.0F, "not standing ready: the seal stays shut");
        Check.that(com.hexhaki.piece.sword.Koiguchi.push(10) == 0.0F, "settling into the stance first");
        float most = 0.0F;
        int open = 0;
        for (int t = 0; t < 600; t++) {
            float p = com.hexhaki.piece.sword.Koiguchi.push(t);
            Check.that(p >= 0.0F && p <= com.hexhaki.piece.sword.Koiguchi.OUT + 0.1F, "a thumb's width at most (" + p + ")");
            most = Math.max(most, p);
            if (p > 0.0F) {
                open++;
            }
        }
        Check.that(most > 1.1F, "the guard does break the seal");
        Check.that(open > 40 && open < 120, "now and then, not all the time (" + open + " of 600 ticks)");
        for (double t = 40; t < 600; t += 0.25) {
            float a = com.hexhaki.piece.sword.Koiguchi.push(t);
            float b = com.hexhaki.piece.sword.Koiguchi.push(t + 0.25);
            Check.that(Math.abs(a - b) < 0.6F, "it eases out and home, never jumps (" + t + ")");
        }
    }

    /** 2.2.0: the Haki slash at full charge (the owner: "meant to be at max charge like 100 times this"). */
    static void bigSlash() {
        Check.that(SwordRules.SLASH_HEIGHT_MAX >= 60 && SwordRules.SLASH_RANGE_MAX >= 200, "a full slash is huge (64 tall, 220 long)");
        Check.that(SwordRules.SLASH_HEIGHT_MAX * SwordRules.SLASH_RANGE_MAX >= 50 * 6.0 * 36.0, "dozens of times the 2.1.x slash's area");
        for (double height : new double[] {2, 6, 20, 64}) {
            double bottom = SwordRules.slashCentre(height) - height / 2.0 + 1.6;
            Check.that(bottom <= 0.0 && bottom >= -2.51, "the plane stands up from just under the feet (" + bottom + " at " + height + ")");
            // 2.4.9 (the owner: "the sword slash width on all charges should always be 1 block wide, 1x1, never more")
            Check.near(SwordRules.slashThickness(height), 1.0, 1e-9, "a " + height + " slash hits with a wall one block thick");
            for (double v = 0.0; v <= 1.0; v += 0.1) {
                Check.that(SwordRules.trenchAt(height, v) <= 1.0 + 1e-9, "the trench is at most one block wide");
                Check.that(Math.max(1, (int) Math.ceil(SwordRules.trenchAt(height, v) / SwordRules.TRENCH_MAX - 1.0E-9)) == 1,
                    "one cell a row of the trench");
            }
        }
        Check.that(SwordRules.NPC_SLASH_HEIGHT_MAX <= 8 && SwordRules.NPC_SLASH_RANGE_MAX <= 40, "a pirate captain's slash stays small");
        // 2.5.4 (the owner: "vastly increase the cooldown of the sword slash projection move"): 30 seconds between Haki slashes, ten
        // times the 2.1.4 three, and far longer than the steel cut's
        Check.that(SwordRules.SLASH_COOLDOWN == 600 && SwordRules.SLASH_COOLDOWN >= 10 * SwordRules.CUT_COOLDOWN, "the Haki slash cools for 30 s");
    }

    /**
     * 2.2.4: the Haki slash as the manga's energy wave: its solid quads all face the eye (they survive back-face culling: no holes),
     * its glow is there, its body is exactly as thick as the trench the server cuts at every height, and it reaches from the trench
     * to its peak.
     */
    static void wave() {
        for (double height : new double[] {2.0, 6.0, 24.0, 64.0}) {
            for (double[] eye : new double[][] {{-30, 10, -40}, {-12, 3, -2}, {40, 30, 0}, {10, 2, 60}}) {
                com.hexhaki.piece.sword.SlashMesh.Quads solid = new com.hexhaki.piece.sword.SlashMesh.Quads();
                com.hexhaki.piece.sword.SlashMesh.Quads glow = new com.hexhaki.piece.sword.SlashMesh.Quads();
                double[] base = {20 - eye[0], -2 - eye[1], 0 - eye[2]};
                com.hexhaki.piece.sword.SlashMesh.Frame frame = com.hexhaki.piece.sword.SlashMesh.Frame.of(base, 1, 0, height, 1.0);
                com.hexhaki.piece.sword.SlashMesh.wave(solid, glow, frame, 9L, 0x2CF6D2, 12.0, 1.0);
                // 2.2.6: light, not a solid: nothing opaque, every corner see-through, the world shows through it
                Check.that(solid.n == 0 && glow.n > 2000, "a " + height + " wave is all light (" + solid.n + " solid, " + glow.n + ")");
                double top = -Double.MAX_VALUE;
                double bottom = Double.MAX_VALUE;
                int most = 0;
                for (int q = 0; q < glow.n; q++) {
                    for (int k = 0; k < 4; k++) {
                        most = Math.max(most, glow.c[q * 4 + k] >>> 24);
                        top = Math.max(top, glow.v[q * 12 + k * 3 + 1] - base[1]);
                        bottom = Math.min(bottom, glow.v[q * 12 + k * 3 + 1] - base[1]);
                    }
                }
                Check.that(most <= 0.92 * 255 && most >= 0.6 * 255, "see-through everywhere, bright somewhere (" + most + ")");
                // it flows: the same wave a few ticks on is lit differently
                com.hexhaki.piece.sword.SlashMesh.Quads later = new com.hexhaki.piece.sword.SlashMesh.Quads();
                com.hexhaki.piece.sword.SlashMesh.wave(new com.hexhaki.piece.sword.SlashMesh.Quads(), later, frame, 9L, 0x2CF6D2, 14.5, 1.0);
                int changed = 0;
                for (int q = 0; q < Math.min(later.n, glow.n) * 4; q++) {
                    changed += later.c[q] != glow.c[q] ? 1 : 0;
                }
                Check.that(changed > glow.n, "its light moves from tick to tick (" + changed + ")");
                // drawn in layers: the sort keeps each layer together, the far skin before the tongues before the near skin
                later.sortBackToFront();
                for (int q = 1; q < later.n; q++) {
                    Check.that(later.layers[q] >= later.layers[q - 1], "layers in order after the sort");
                }
                Check.that(top >= height * 0.98 && bottom >= -0.2 - height * 0.01, "the wave rises from its trench to its peak (" + height + ")");
                // 2.4.9: never wider than a block: every corner of everything drawn within half a block of its line sideways
                double widest = 0.0;
                for (com.hexhaki.piece.sword.SlashMesh.Quads q : new com.hexhaki.piece.sword.SlashMesh.Quads[] {glow, later}) {
                    for (int c = 0; c < q.n * 4; c++) {
                        widest = Math.max(widest, Math.abs((q.v[c * 3] - base[0]) * frame.side()[0] + (q.v[c * 3 + 2] - base[2]) * frame.side()[2]));
                    }
                }
                Check.that(widest <= 0.5 + 1e-9, "a " + height + " wave is drawn within one block's width (" + widest + " each side)");
            }
            com.hexhaki.piece.sword.SlashMesh.Frame frame = com.hexhaki.piece.sword.SlashMesh.Frame.of(new double[3], 0, 1, height, 1.0);
            for (double v = 0.0; v <= 1.0; v += 0.05) {
                Check.near(frame.across(v) * 2.0, SwordRules.slashThicknessAt(height, v), 1e-9, "the body is the trench's width " + v + " up");
                Check.that(frame.front(v) > frame.back(v), "the body has length along its flight " + v + " up");
            }
            Check.that(SwordRules.slashThicknessAt(height, 0.0) >= SwordRules.slashThicknessAt(height, 0.5)
                && SwordRules.slashThicknessAt(height, 0.5) >= SwordRules.slashThicknessAt(height, 0.95), "widest in the ground, thin at the peak");
        }
        Check.near(SwordRules.slashThicknessAt(64, 0.0), SwordRules.SLASH_BODY, 1e-9, "a full wave's body is the body's width in the ground");
    }

    /** How far a triangle turns its back on the eye at the origin: 0 facing it (or edge on, or a sliver), up to 1 facing away. */
    static double awayShare(double[] a, double[] b, double[] c) {
        double nx = (b[1] - a[1]) * (c[2] - a[2]) - (b[2] - a[2]) * (c[1] - a[1]);
        double ny = (b[2] - a[2]) * (c[0] - a[0]) - (b[0] - a[0]) * (c[2] - a[2]);
        double nz = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
        double mx = a[0] + b[0] + c[0];
        double my = a[1] + b[1] + c[1];
        double mz = a[2] + b[2] + c[2];
        double n = Math.sqrt(nx * nx + ny * ny + nz * nz);
        double m = Math.sqrt(mx * mx + my * my + mz * mz);
        if (n < 1e-12 || m < 1e-12) {
            return 0.0;
        }
        return Math.max(0.0, (nx * mx + ny * my + nz * mz) / (n * m));
    }
    /**
     * 2.2.9 (the owner: "grab sounds from the web for the swords unsheath, swipe, etc, all the sounds must come from the web (be free
     * for use)"): every beat is a registered sound inside its animation, every cut swings, each beat is heard once on its tick, and
     * every clip is defined, mono and credited to its free source.
     */
    static void sounds() throws Exception {
        Set<String> all = new HashSet<>(java.util.Arrays.asList(SwordSounds.ALL));
        Check.that(all.size() == SwordSounds.ALL.length, "no sound event twice");
        Pattern end = Pattern.compile("\"endTick\":\\s*(\\d+)");
        Katana[] blades = {null, Katana.values()[0], Katana.GRYPHON, Katana.YORU};
        int heard = 0;
        for (Katana katana : blades) {
            for (int anim = 1; anim < SwordAnims.MOVES + SwordMove.values().length; anim++) {
                String name = SwordAnims.name(anim, katana);
                if (name == null) {
                    continue;
                }
                Matcher m = end.matcher(Files.readString(ANIMATIONS.resolve(name + ".json")));
                Check.that(m.find(), name + ": its length is found");
                int length = Integer.parseInt(m.group(1));
                SwordSounds.Beat[] beats = SwordSounds.of(anim, katana);
                int last = -1;
                for (SwordSounds.Beat beat : beats) {
                    Check.that(all.contains(beat.sound()), name + ": " + beat.sound() + " is a registered sound");
                    Check.that(beat.tick() >= 0 && beat.tick() < length, name + ": its beat at " + beat.tick() + " falls inside it");
                    Check.that(beat.tick() >= last, name + ": its beats in order");
                    Check.that(beat.volume() > 0.0F && beat.volume() <= 1.5F, name + ": a sane volume");
                    Check.that(beat.pitch() >= 0.5F && beat.pitch() <= 2.0F, name + ": a pitch the game plays (0.5 to 2)");
                    last = beat.tick();
                    heard++;
                }
                int draw = BladePaths.drawTick(name);
                for (SwordSounds.Beat beat : beats) {
                    if (beat.sound().equals(SwordSounds.DRAW)) {
                        Check.that(draw >= 0 && beat.tick() <= draw, name + ": the blade slides out of the saya before it is out");
                    }
                }
                if (anim >= SwordAnims.MOVES) {
                    // every cut swings at the start of its window, the heavy ones deep and ringing
                    for (SwordMove.Cut cut : SwordMove.byOrdinal(anim - SwordAnims.MOVES).cuts()) {
                        boolean heavy = cut.weight().ordinal() >= com.hexhaki.piece.style.Hit.Weight.HEAVY.ordinal();
                        boolean swings = false;
                        boolean rings = false;
                        for (SwordSounds.Beat beat : beats) {
                            swings |= beat.tick() == cut.from() && beat.sound().equals(SwordSounds.swing(heavy));
                            rings |= beat.tick() == cut.from() && beat.sound().equals(SwordSounds.SHING);
                        }
                        Check.that(swings && rings == heavy, name + ": its cut at " + cut.from() + " swings" + (heavy ? " and rings" : ""));
                    }
                }
            }
        }
        Check.that(heard > 100, "the sword is heard");
        // the draw, the sheathe and the release are heard; Yoru's great sword lower than a katana, Gryphon's saber higher
        for (int anim : new int[] {SwordAnims.DRAW, SwordAnims.SHEATHE, SwordAnims.CHARGE_DRAW, SwordAnims.RELEASE, SwordAnims.QUICK_DRAW}) {
            Check.that(SwordSounds.of(anim, null).length > 0 && SwordSounds.of(anim, Katana.YORU).length > 0, anim + ": heard");
        }
        SwordSounds.Beat katanaCut = SwordSounds.of(SwordAnims.of(SwordMove.KESA), Katana.values()[0])[0];
        Check.that(SwordSounds.of(SwordAnims.of(SwordMove.KESA), Katana.YORU)[0].pitch() < katanaCut.pitch()
            && SwordSounds.of(SwordAnims.of(SwordMove.KESA), Katana.GRYPHON)[0].pitch() > katanaCut.pitch(), "each blade its own voice");
        String[] sheathes = {"sword_sheathe", "sword_sheathe_yoru"};
        for (String name : sheathes) {
            SwordSounds.Beat[] beats = SwordSounds.of(name);
            SwordSounds.Beat close = beats[beats.length - 1];
            Check.that(close.sound().equals(SwordSounds.GUARD_CLICK) && close.tick() >= BladePaths.sheathTick(name) - 1,
                name + ": the guard clicks home as the blade goes in");
        }

        // the cursor: each beat once, on its tick; a skipped tick plays it one late; a new start plays them again
        for (Katana katana : blades) {
            for (int anim = 1; anim < SwordAnims.MOVES + SwordMove.values().length; anim++) {
                SwordSounds.Beat[] beats = SwordSounds.of(anim, katana);
                for (int every = 1; every <= 2; every++) {
                    SwordSounds.Cursor cursor = new SwordSounds.Cursor();
                    java.util.List<SwordSounds.Beat> got = new java.util.ArrayList<>();
                    long start = 1000;
                    for (long now = start; now < start + 40; now += every) {
                        for (SwordSounds.Beat beat : cursor.advance(anim, start, now, katana)) {
                            Check.that(now - start >= beat.tick() && now - start <= beat.tick() + every - 1, anim + ": on its tick");
                            got.add(beat);
                        }
                        Check.that(cursor.advance(anim, start, now, katana).length == 0, anim + ": never twice in a tick");
                    }
                    Check.that(got.size() == beats.length, anim + ": every beat heard once (" + got.size() + " of " + beats.length + ")");
                }
            }
        }
        int draw = SwordAnims.DRAW;
        SwordSounds.Cursor cursor = new SwordSounds.Cursor();
        for (SwordSounds.Beat beat : cursor.advance(draw, 0, 12, null)) {
            Check.that(beat.tick() >= 12 - SwordSounds.CATCH_UP, "an animation first seen halfway through: its old beats are not played late");
        }
        cursor = new SwordSounds.Cursor();
        int first = 0;
        for (long now = 0; now < 30; now++) {
            first += cursor.advance(draw, 0, now, null).length;
        }
        int again = 0;
        for (long now = 30; now < 60; now++) {
            again += cursor.advance(draw, 30, now, null).length;
        }
        Check.that(first > 0 && first == again, "drawn again: heard again");

        // the files: every event in sounds.json with its subtitle, every clip there, mono Ogg Vorbis, credited as free to use
        Path dir = Path.of("src/main/resources/assets/hexhaki/sounds/piece/sword");
        String json = Files.readString(Path.of("src/main/resources/assets/hexhaki/sounds.json"));
        String lang = Files.readString(Path.of("src/main/resources/assets/hexhaki/lang/en_us.json"));
        String sources = Files.readString(dir.resolve("SOURCES.txt"));
        for (String name : SwordSounds.ALL) {
            Check.that(json.contains("\"" + name + "\": {"), name + ": defined in sounds.json");
            Check.that(lang.contains("\"subtitles.hexhaki." + name + "\""), name + ": its subtitle");
        }
        Matcher event = Pattern.compile("\"(piece\\.[a-z_.]+)\": \\{").matcher(json);
        while (event.find()) {
            Check.that(all.contains(event.group(1)) || event.group(1).equals(com.hexhaki.piece.clash.ClashRules.SOUND)
                || java.util.Arrays.asList(com.hexhaki.piece.Alignment.SOUNDS).contains(event.group(1))
                || event.group(1).equals(com.hexhaki.piece.haki.ConquerorMix.EXPLOSION)
                || event.group(1).equals(com.hexhaki.piece.haki.ConquerorMix.EXPLOSION_GOOD)
                || event.group(1).equals(com.hexhaki.piece.haki.ConquerorMix.EXPLOSION_EVIL)
                || event.group(1).equals(com.hexhaki.piece.haki.ConquerorMix.CHARGE)
                || event.group(1).equals(com.hexhaki.piece.haki.ConquerorMix.CHARGE_HOLD)
                || java.util.Arrays.stream(com.hexhaki.piece.dragon.UoSounds.EVENTS).anyMatch(e -> e[0].equals(event.group(1)))
                || java.util.Arrays.stream(com.hexhaki.piece.pika.PikaSounds.EVENTS).anyMatch(e -> e[0].equals(event.group(1)))
                || java.util.Arrays.stream(com.hexhaki.piece.gura.GuraSounds.EVENTS).anyMatch(e -> e[0].equals(event.group(1)))
                || java.util.Arrays.stream(com.hexhaki.piece.magu.MaguSounds.EVENTS).anyMatch(e -> e[0].equals(event.group(1)))
                || java.util.Arrays.stream(com.hexhaki.piece.hie.HieSounds.EVENTS).anyMatch(e -> e[0].equals(event.group(1)))
                || java.util.Arrays.stream(com.hexhaki.piece.gear.GearSounds.EVENTS).anyMatch(e -> e[0].equals(event.group(1)))
                || java.util.Arrays.stream(com.hexhaki.piece.noro.NoroSounds.EVENTS).anyMatch(e -> e[0].equals(event.group(1)))
                || java.util.Arrays.stream(com.hexhaki.piece.supa.SupaSounds.EVENTS).anyMatch(e -> e[0].equals(event.group(1)))
                || java.util.Arrays.stream(com.hexhaki.piece.horo.HoroSounds.EVENTS).anyMatch(e -> e[0].equals(event.group(1)))
                || java.util.Arrays.stream(com.hexhaki.piece.ito.ItoSounds.EVENTS).anyMatch(e -> e[0].equals(event.group(1))),
                event.group(1) + ": registered");
        }
        Set<String> used = new HashSet<>();
        Matcher clip = Pattern.compile("\"hexhaki:piece/sword/([a-z0-9_]+)\"").matcher(json);
        while (clip.find()) {
            used.add(clip.group(1));
            Check.that(Files.exists(dir.resolve(clip.group(1) + ".ogg")), clip.group(1) + ".ogg exists");
        }
        int files = 0;
        try (java.util.stream.Stream<Path> list = Files.list(dir)) {
            for (Path file : (Iterable<Path>) list::iterator) {
                String name = file.getFileName().toString();
                if (!name.endsWith(".ogg")) {
                    continue;
                }
                files++;
                String base = name.substring(0, name.length() - 4);
                Check.that(used.contains(base), name + ": played by some sound event");
                Matcher credit = Pattern.compile("(?m)^" + Pattern.quote(name) + ": .* by .+, CC0 1\\.0, https://opengameart\\.org/\\S+$")
                    .matcher(sources);
                Check.that(credit.find(), name + ": credited in SOURCES.txt (its pack, author, CC0 licence and page)");
                byte[] data = Files.readAllBytes(file);
                int at = indexOf(data, "\u0001vorbis".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
                Check.that(data.length > 4 && data[0] == 'O' && data[1] == 'g' && data[2] == 'g' && data[3] == 'S' && at >= 0,
                    name + ": Ogg Vorbis");
                Check.that(data[at + 11] == 1, name + ": mono (the game places only mono sounds in the world)");
                int rate = (data[at + 12] & 0xFF) | (data[at + 13] & 0xFF) << 8 | (data[at + 14] & 0xFF) << 16 | (data[at + 15] & 0xFF) << 24;
                Check.that(rate == 44100, name + ": 44.1 kHz");
            }
        }
        Check.that(files == used.size() && files >= 40, "every clip is played (" + files + " clips)");
        // 2.4.6: the evil Conqueror's sounds (the owner's own clip): defined, subtitled, each a mono 44.1 kHz Ogg Vorbis file, credited
        Path conqueror = Path.of("src/main/resources/assets/hexhaki/sounds/piece/conqueror");
        String credits = Files.readString(conqueror.resolve("SOURCES.txt"));
        for (String name : com.hexhaki.piece.Alignment.SOUNDS) {
            String file = name.substring("piece.conqueror.".length());
            Check.that(json.contains("\"" + name + "\": {") && json.contains("\"hexhaki:piece/conqueror/" + file + "\""), name + ": defined");
            Check.that(lang.contains("\"subtitles.hexhaki.piece.conqueror." + file.replaceAll("_(male|female)$", "") + "\""), name + ": its subtitle");
            byte[] data = Files.readAllBytes(conqueror.resolve(file + ".ogg"));
            int at = indexOf(data, "\u0001vorbis".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            Check.that(data[0] == 'O' && data[1] == 'g' && data[2] == 'g' && data[3] == 'S' && at >= 0 && data[at + 11] == 1,
                file + ".ogg: mono Ogg Vorbis");
            Check.that(credits.contains(file + ".ogg: "), file + ".ogg: credited in SOURCES.txt (the owner's own clip)");
        }
    }

    /**
     * 2.8.6 (the owner: "giving every move of the dragon custom sounds from the web ... don't make any sound yourself"): the Uo Uo no
     * Mi's sounds: every event defined with its subtitle, every clip there and played, each a mono 44.1 kHz Ogg Vorbis file credited in
     * SOURCES.txt as a CC0 recording on Freesound.org with its author and page.
     */
    static void dragonSounds() throws Exception {
        webSounds("uo", com.hexhaki.piece.dragon.UoSounds.EVENTS, 25);
        // 2.9.0 (the owner: "The sounds are atrocious ... I keep telling you to get the sounds from the web"): the Light fruit's too
        webSounds("pika", com.hexhaki.piece.pika.PikaSounds.EVENTS, 30);
        // 3.1.1: the Gura Gura no Mi's (tools/gura_sounds.py)
        webSounds("gura", com.hexhaki.piece.gura.GuraSounds.EVENTS, 11);
        // 3.1.3: the Magu Magu no Mi's (tools/magu_sounds.py)
        webSounds("magu", com.hexhaki.piece.magu.MaguSounds.EVENTS, 9);
        // 3.1.9: the Hie Hie no Mi's (tools/hie_sounds.py)
        webSounds("hie", com.hexhaki.piece.hie.HieSounds.EVENTS, 11);
        // 3.2.3: the Nika fruit's transformation drums (tools/gear_sounds.py)
        webSounds("gear", com.hexhaki.piece.gear.GearSounds.EVENTS, 8);
        // 3.4.9 (the owner: "we also need a sound redesign for most of the abilities ... mostly like cartoony sounds, so go get them from
        // the web"): the original's Gear 5 events play CC0 recordings (tools/gear_sounds.py, sounds/piece/nika)
        redone("nika", com.hexhaki.piece.gear.GearSounds.REDONE, 10);
        // 3.3.4: the Noro Noro no Mi's (tools/noro_sounds.py; the first 3.3.4 push borrowed the mod's own clips: replaced)
        webSounds("noro", com.hexhaki.piece.noro.NoroSounds.EVENTS, 11);
        // 3.3.5: the Supa Supa no Mi's (tools/supa_sounds.py)
        webSounds("supa", com.hexhaki.piece.supa.SupaSounds.EVENTS, 7);
        // 3.3.6: the Horo Horo no Mi's (tools/horo_sounds.py)
        webSounds("horo", com.hexhaki.piece.horo.HoroSounds.EVENTS, 6);
        // 3.3.7: the Ito Ito no Mi's (tools/ito_sounds.py)
        webSounds("ito", com.hexhaki.piece.ito.ItoSounds.EVENTS, 9);
    }

    /** One fruit's web sounds (sounds/piece/{folder}): as {@link #dragonSounds} checks them, at least {@code least} clips. */
    static void webSounds(String folder, String[][] events, int least) throws Exception {
        Path dir = Path.of("src/main/resources/assets/hexhaki/sounds/piece/" + folder);
        String json = Files.readString(Path.of("src/main/resources/assets/hexhaki/sounds.json"));
        String lang = Files.readString(Path.of("src/main/resources/assets/hexhaki/lang/en_us.json"));
        String sources = Files.readString(dir.resolve("SOURCES.txt"));
        Set<String> played = new HashSet<>();
        for (String[] event : events) {
            Check.that(json.contains("\"" + event[0] + "\": {"), event[0] + ": defined in sounds.json");
            Check.that(lang.contains("\"subtitles.hexhaki." + event[0] + "\""), event[0] + ": its subtitle");
            for (int i = 1; i < event.length; i++) {
                Check.that(json.contains("\"hexhaki:piece/" + folder + "/" + event[i] + "\""), event[0] + ": plays " + event[i]);
                played.add(event[i]);
            }
        }
        int files = 0;
        try (java.util.stream.Stream<Path> list = Files.list(dir)) {
            for (Path file : (Iterable<Path>) list::iterator) {
                String name = file.getFileName().toString();
                if (!name.endsWith(".ogg")) {
                    continue;
                }
                files++;
                Check.that(played.contains(name.substring(0, name.length() - 4)), name + ": played by some event");
                Matcher credit = Pattern.compile("(?m)^" + Pattern.quote(name)
                    + ": \".+\" by \\S+, https://freesound\\.org/people/\\S+/sounds/\\d+/ \\(CC0 1\\.0\\)").matcher(sources);
                Check.that(credit.find(), name + ": credited in SOURCES.txt (its title, author, page and CC0 licence)");
                byte[] data = Files.readAllBytes(file);
                int at = indexOf(data, "\u0001vorbis".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
                Check.that(data[0] == 'O' && data[1] == 'g' && data[2] == 'g' && data[3] == 'S' && at >= 0 && data[at + 11] == 1,
                    name + ": mono Ogg Vorbis");
            }
        }
        Check.that(files == played.size() && files >= least, "every " + folder + " clip is played (" + files + " clips)");
    }

    /**
     * 3.4.9: the original's own events given new recordings ({@code events}: the original's event, then its clips in sounds/piece/{folder}):
     * each overridden in sounds.json with only those clips, its subtitle kept; every clip in the folder played, credited and mono.
     */
    static void redone(String folder, String[][] events, int least) throws Exception {
        Path dir = Path.of("src/main/resources/assets/hexhaki/sounds/piece/" + folder);
        String json = Files.readString(Path.of("src/main/resources/assets/hexhaki/sounds.json"));
        String sources = Files.readString(dir.resolve("SOURCES.txt"));
        Set<String> played = new HashSet<>();
        for (String[] event : events) {
            int at = json.indexOf("\"" + event[0] + "\": {");
            Check.that(at >= 0, event[0] + ": overridden in sounds.json");
            int end = json.indexOf("\n  }", at);
            String entry = json.substring(at, end);
            Check.that(entry.contains("\"subtitles.hexhaki." + event[0] + "\""), event[0] + ": its own subtitle kept");
            Check.that(!entry.contains("\"hexhaki:" + event[0] + "\""), event[0] + ": the original's recording no longer played");
            for (int i = 1; i < event.length; i++) {
                Check.that(entry.contains("\"hexhaki:piece/" + folder + "/" + event[i] + "\""), event[0] + ": plays " + event[i]);
                played.add(event[i]);
            }
        }
        int files = 0;
        try (java.util.stream.Stream<Path> list = Files.list(dir)) {
            for (Path file : (Iterable<Path>) list::iterator) {
                String name = file.getFileName().toString();
                if (!name.endsWith(".ogg")) {
                    continue;
                }
                files++;
                Check.that(played.contains(name.substring(0, name.length() - 4)), name + ": played by some event");
                Matcher credit = Pattern.compile("(?m)^" + Pattern.quote(name)
                    + ": \".+\" by \\S+, https://freesound\\.org/people/\\S+/sounds/\\d+/ \\(CC0 1\\.0\\)").matcher(sources);
                Check.that(credit.find(), name + ": credited in SOURCES.txt (its title, author, page and CC0 licence)");
                byte[] data = Files.readAllBytes(file);
                int at = indexOf(data, "\u0001vorbis".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
                Check.that(data[0] == 'O' && data[1] == 'g' && data[2] == 'g' && data[3] == 'S' && at >= 0 && data[at + 11] == 1,
                    name + ": mono Ogg Vorbis");
            }
        }
        Check.that(files == played.size() && files >= least, "every " + folder + " clip is played (" + files + " clips)");
    }

    static int indexOf(byte[] data, byte[] what) {
        outer:
        for (int i = 0; i + what.length <= data.length; i++) {
            for (int j = 0; j < what.length; j++) {
                if (data[i + j] != what[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
    /**
     * 2.5.4 (the owner: "if the sword is holstered the hand should be resting on the holster too ... the first persons are meant to
     * reflect exactly what the player is doing"): the first-person sheathed katana and the left hand on it (sword/HolsterView), seen by
     * the game's first-person hand camera (70 degrees top to bottom), on a 16:9 screen and a phone's 20:9: the guard and the pommel on
     * the screen, the handle rising up and right from the guard, the edge up, the fist resting on the handle (its centre a little over
     * the handle's axis, near its end), the arm coming up from below the screen, everything sinking as the sword is taken up.
     */
    static void holster() {
        double[][] k = com.hexhaki.piece.sword.HolsterView.matrix(com.hexhaki.piece.sword.HolsterView.katana(0.0F, 0.0F));
        double[] guard = apply(k, 0, 0, 0);
        double[] pommel = apply(k, 0, -6.5 * Katana.SIZE / 16.0, 0); // 2.6.2: drawn Katana.SIZE times its mesh
        double[] edge = sub3(apply(k, -1.0 / 16.0, 0, 0), guard);
        for (double aspect : new double[] {16.0 / 9.0, 20.0 / 9.0}) {
            Check.that(onScreen(guard, aspect, 0.98) && onScreen(pommel, aspect, 0.9), "the guard and the pommel in view (" + aspect + ")");
        }
        double[] g = screen(guard);
        double[] p = screen(pommel);
        Check.that(p[0] > g[0] + 0.05 && p[1] > g[1] + 0.05, "the handle rises up and right from the guard");
        Check.that(g[0] < -0.2 && g[1] < -0.6, "the hilt at the bottom left");
        Check.that(edge[1] > 0.5 / 16.0, "the edge up, as a katana is worn");
        for (boolean slim : new boolean[] {false, true}) {
            double[][] a = com.hexhaki.piece.sword.HolsterView.matrix(com.hexhaki.piece.sword.HolsterView.arm(slim, 0.0F, 0.0F));
            // the game's renderLeftHand: the part moves to its pivot and sways -0.1 about z; the fist's centre and the shoulder in it
            double[][] part = com.hexhaki.piece.sword.HolsterView.mul(a, com.hexhaki.piece.sword.HolsterView.step(new float[] {0, 5.0F / 16,
                (slim ? 2.5F : 2.0F) / 16, 0}));
            part = com.hexhaki.piece.sword.HolsterView.mul(part, com.hexhaki.piece.sword.HolsterView.step(new float[] {3,
                (float) Math.toDegrees(-0.1)}));
            double[] fist = apply(part, (slim ? 0.5 : 1.0) / 16.0, 8.5 / 16.0, 0);
            double[] shoulder = apply(part, (slim ? 0.5 : 1.0) / 16.0, -2.0 / 16.0, 0);
            double off = distanceToSegment(fist, guard, pommel) * 16.0;
            Check.that(off > 0.8 * Katana.SIZE && off < 2.4 * Katana.SIZE, "the fist rests on the handle (" + off + " px over it)");
            double along = dot3(sub3(fist, guard), sub3(pommel, guard)) / dot3(sub3(pommel, guard), sub3(pommel, guard));
            Check.that(along > 0.55 && along < 1.0, "on its end, the guard and the saya's mouth left in sight");
            Check.that(onScreen(fist, 16.0 / 9.0, 0.95), "the fist in view");
            Check.that(screen(shoulder)[1] < -1.0, "the arm comes up from below the screen");
            double[] lowered = apply(com.hexhaki.piece.sword.HolsterView.matrix(com.hexhaki.piece.sword.HolsterView.katana(1.0F, 0.0F)), 0, 0, 0);
            Check.near(lowered[1], guard[1] - 0.6, 1e-6, "taken up, it rises from 0.6 below, as the game's hands");
        }
    }

    static double[] apply(double[][] m, double x, double y, double z) {
        return new double[] {m[0][0] * x + m[0][1] * y + m[0][2] * z + m[0][3], m[1][0] * x + m[1][1] * y + m[1][2] * z + m[1][3],
            m[2][0] * x + m[2][1] * y + m[2][2] * z + m[2][3]};
    }

    /** Where a view point lands, the screen's half height 1 (the first-person hand's 70 degrees), x the same scale. */
    static double[] screen(double[] v) {
        double f = 1.0 / Math.tan(Math.toRadians(35.0));
        return new double[] {f * v[0] / -v[2], f * v[1] / -v[2]};
    }

    static boolean onScreen(double[] v, double aspect, double margin) {
        double[] s = screen(v);
        return v[2] < 0 && Math.abs(s[0]) < aspect * margin && Math.abs(s[1]) < margin;
    }

    static double[] sub3(double[] a, double[] b) {
        return new double[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    static double dot3(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    static double distanceToSegment(double[] p, double[] a, double[] b) {
        double[] ab = sub3(b, a);
        double t = Math.max(0, Math.min(1, dot3(sub3(p, a), ab) / dot3(ab, ab)));
        double[] c = {a[0] + ab[0] * t, a[1] + ab[1] * t, a[2] + ab[2] * t};
        double[] d = sub3(p, c);
        return Math.sqrt(dot3(d, d));
    }
    /**
     * 2.5.6 (the owner: "the pull out animation needs to be really gorgeous to look at, sheath and unsheath both", first person):
     * sword/DrawView: the draw starts exactly where the sheathed view rests and ends exactly where the drawn blade is held, the
     * sheathing the other way round; nothing jumps between frames (a quarter of a tick); the right fist stays on the handle while it
     * holds it; everything stays in front of the eye; the blade is clear of the saya by the tick the server lets it cut and home by the
     * tick it counts it sheathed. Every hip blade, wide and slim arms.
     */
    static void drawView() {
        drawSmooth();
        bladeCoat();
        for (Katana katana : new Katana[] {Katana.CRIMSON_DAWN, Katana.GRYPHON}) {
            for (boolean slim : new boolean[] {false, true}) {
                float drawLength = com.hexhaki.piece.sword.SwordAnims.duration(com.hexhaki.piece.sword.SwordAnims.DRAW, katana);
                float sheatheLength = com.hexhaki.piece.sword.SwordAnims.duration(com.hexhaki.piece.sword.SwordAnims.SHEATHE, katana);
                com.hexhaki.piece.sword.DrawView.Pose rest = com.hexhaki.piece.sword.DrawView.holster(katana, slim, 0.0F, 0.0F);
                com.hexhaki.piece.sword.DrawView.Pose held = com.hexhaki.piece.sword.DrawView.held(katana, slim, 0.0F, 0.0F);
                String drawName = com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.DRAW, katana);
                String sheatheName = com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.SHEATHE, katana);
                int drawTick = com.hexhaki.piece.sword.BladePaths.drawTick(drawName);
                int sheathTick = com.hexhaki.piece.sword.BladePaths.sheathTick(sheatheName);
                Check.that(drawTick > 0 && drawTick < drawLength && sheathTick > 0 && sheathTick < sheatheLength, "the server's draw and sheath ticks");
                com.hexhaki.piece.sword.DrawView.Pose first = com.hexhaki.piece.sword.DrawView.draw(katana, slim, 0.0F, drawLength, drawTick, 0.0F, 0.0F);
                com.hexhaki.piece.sword.DrawView.Pose last = com.hexhaki.piece.sword.DrawView.draw(katana, slim, drawLength, drawLength, drawTick, 0.0F, 0.0F);
                Check.that(same(first.blade(), rest.blade()) && same(first.left(), rest.left()), "the draw starts at rest");
                Check.that(same(last.blade(), held.blade()) && same(last.right(), held.right()), "the draw ends where the blade is held");
                com.hexhaki.piece.sword.DrawView.Pose s0 = com.hexhaki.piece.sword.DrawView.sheathe(katana, slim, 0.0F, sheatheLength, sheathTick, 0.0F, 0.0F);
                com.hexhaki.piece.sword.DrawView.Pose s1 = com.hexhaki.piece.sword.DrawView.sheathe(katana, slim, sheatheLength, sheatheLength, sheathTick, 0.0F, 0.0F);
                Check.that(same(s0.blade(), held.blade()) && same(s0.right(), held.right()), "the sheathing starts where the blade is held");
                Check.that(same(s1.blade(), rest.blade()) && same(s1.left(), rest.left()), "the sheathing ends at rest");
                double[] mouth = origin(rest.blade());
                for (int phase = 0; phase < 2; phase++) {
                    float length = phase == 0 ? drawLength : sheatheLength;
                    double[] lastGuard = null;
                    double[] lastTip = null;
                    double[] lastFist = null;
                    for (float t = 0.0F; t <= length + 1e-4; t += 0.25F) {
                        com.hexhaki.piece.sword.DrawView.Pose p = phase == 0
                            ? com.hexhaki.piece.sword.DrawView.draw(katana, slim, t, length, drawTick, 0.0F, 0.0F)
                            : com.hexhaki.piece.sword.DrawView.sheathe(katana, slim, t, length, sheathTick, 0.0F, 0.0F);
                        double[][] m = com.hexhaki.piece.sword.HolsterView.matrix(p.blade());
                        double[] guard = apply(m, 0, 0, 0);
                        double[] tip = apply(m, 0, katana.tip() * katana.size() / 16.0, 0);
                        double[] grip = apply(m, 0, -2.4 * katana.size() / 16.0, 0);
                        Check.that(guard[2] < -0.3 && tip[2] < -0.05, "in front of the eye (" + katana + " t" + t + ")");
                        if (lastGuard != null) {
                            Check.that(distance(guard, lastGuard) < 0.4 && distance(tip, lastTip) < 1.0, "no jump at t" + t + " (" + katana + ")");
                        }
                        if (p.right() != null) {
                            double[] fist = origin(p.right());
                            boolean holding = phase == 0 ? t * 19.5F / drawTick >= 6.0F : t < sheathTick;
                            if (holding) {
                                Check.that(distance(fist, grip) < 1e-4, "the right fist on the handle at t" + t);
                            }
                            if (lastFist != null) {
                                Check.that(distance(fist, lastFist) < 0.4, "the right hand never jumps (t" + t + ")");
                            }
                            lastFist = fist;
                        }
                        if (phase == 0 && Math.abs(t - drawTick) < 1e-4) {
                            // the point at the saya's mouth (where the mouth is now: the left hand draws the saya back) on the very tick
                            // the server lets the blade cut
                            // (measured along the blade's straight line: Gryphon's saber curves hard, its real point a few px off it)
                            Check.that(distance(tip, origin(p.saya())) < (katana == Katana.GRYPHON ? 0.26 : 0.12),
                                "the point leaves the mouth on the server's draw tick ("
                                + katana + ", " + distance(tip, origin(p.saya())) + ")");
                        }
                        if (phase == 1 && sheathTick >= 0 && t >= sheathTick) {
                            Check.that(distance(guard, mouth) < 0.02, "home when the server counts it sheathed (t" + t + ")");
                        }
                        lastGuard = guard;
                        lastTip = tip;
                    }
                }
            }
        }
    }

    /**
     * 2.6.1 (the owner, on 2.6.0's first-person draw: "wtf is this sequence", the blade up, then right with the handle out of the fist's
     * left, then up again within a tick and a half; then "as smooth as possible without buggy frames"): every blade, every quarter tick
     * of the draw and the sheathing:
     * <ul>
     *   <li>the blade never points at (or away from) the eye: at least half of it across the view (2.6.0 shrank it to a stub);</li>
     *   <li>it never turns more than 26 degrees in a quarter tick, nor does the right fist (2.6.0's fist spun 85 degrees in one as the
     *       held blade came to lie along the forearm);</li>
     *   <li>once the point leaves the mouth the draw's blade turns one way only (clockwise in the view, the point rising) to the held
     *       blade, and the sheathing's snap one way only (counterclockwise) down to the left: no back and forth.</li>
     * </ul>
     */
    static void drawSmooth() {
        for (Katana katana : Katana.values()) {
            if (katana.carry() == Katana.Carry.BACK) {
                continue; // Yoru: its own moves over the shoulder (checked below)
            }
            float drawLength = com.hexhaki.piece.sword.SwordAnims.duration(com.hexhaki.piece.sword.SwordAnims.DRAW, katana);
            float sheatheLength = com.hexhaki.piece.sword.SwordAnims.duration(com.hexhaki.piece.sword.SwordAnims.SHEATHE, katana);
            int drawTick = com.hexhaki.piece.sword.BladePaths.drawTick(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.DRAW, katana));
            int sheathTick = com.hexhaki.piece.sword.BladePaths.sheathTick(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.SHEATHE, katana));
            for (int phase = 0; phase < 2; phase++) {
                float length = phase == 0 ? drawLength : sheatheLength;
                double[][] lastBlade = null;
                double[][] lastRight = null;
                double lastAngle = Double.NaN;
                for (float t = 0.0F; t <= length + 1e-4; t += 0.25F) {
                    com.hexhaki.piece.sword.DrawView.Pose p = phase == 0
                        ? com.hexhaki.piece.sword.DrawView.draw(katana, false, t, length, drawTick, 0.0F, 0.0F)
                        : com.hexhaki.piece.sword.DrawView.sheathe(katana, false, t, length, sheathTick, 0.0F, 0.0F);
                    String at = (phase == 0 ? "draw" : "sheathe") + " t" + t + " (" + katana + ")";
                    double[][] m = com.hexhaki.piece.sword.HolsterView.matrix(p.blade());
                    double[] way = {m[0][1], m[1][1], m[2][1]};
                    double across = Math.hypot(way[0], way[1]) / Math.sqrt(dot3(way, way));
                    Check.that(across >= 0.5, "the blade never points at the eye: " + at + " (" + across + ")");
                    double angle = Math.toDegrees(Math.atan2(way[1], way[0]));
                    if (lastBlade != null) {
                        Check.that(turn(m, lastBlade) <= 26.0, "the blade turns at most 26 degrees a quarter tick: " + at + " (" + turn(m, lastBlade) + ")");
                        double step = angle - lastAngle;
                        step = step > 180 ? step - 360 : step < -180 ? step + 360 : step;
                        if (phase == 0 && t > drawTick + 0.25F) {
                            Check.that(step <= 0.5, "the drawn blade's point rises one way only: " + at + " (" + step + " degrees)");
                        }
                        if (phase == 1 && t * 21.0F / sheathTick <= 4.0F) {
                            Check.that(step >= -0.5, "the sheathing's snap goes one way only: " + at + " (" + step + " degrees)");
                        }
                    }
                    if (p.right() != null) {
                        double[][] r = com.hexhaki.piece.sword.HolsterView.matrix(p.right());
                        if (lastRight != null) {
                            Check.that(turn(r, lastRight) <= 26.0, "the right fist never spins: " + at + " (" + turn(r, lastRight) + ")");
                        }
                        lastRight = r;
                    } else {
                        lastRight = null;
                    }
                    lastBlade = m;
                    lastAngle = angle;
                }
            }
        }
        // 2.6.1: the right click's charge (from the held blade, and drawn from the saya), every blade: the same limits, and it starts
        // where the blade is held (from the held blade) or at rest (from the saya)
        for (Katana katana : Katana.values()) {
            boolean back = katana.carry() == Katana.Carry.BACK;
            int free = Math.max(0, com.hexhaki.piece.sword.BladePaths.drawTick(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.CHARGE_DRAW, katana)));
            Check.that(same(com.hexhaki.piece.sword.DrawView.charge(katana, false, 0.0F, 0.0F, 0.0F).blade(),
                com.hexhaki.piece.sword.DrawView.held(katana, false, 0.0F, 0.0F).blade()), "the charge starts where the blade is held (" + katana + ")");
            if (!back) {
                Check.that(same(com.hexhaki.piece.sword.DrawView.chargeDraw(katana, false, 0.0F, 0.0F, 0.0F).blade(),
                    com.hexhaki.piece.sword.DrawView.holster(katana, false, 0.0F, 0.0F).blade()), "the charge from the saya starts at rest (" + katana + ")");
            }
            for (int from = 0; from < 2; from++) {
                double[][] lastBlade = null;
                double[][] lastRight = null;
                for (float t = 0.0F; t <= 60.0F; t += 0.25F) {
                    com.hexhaki.piece.sword.DrawView.Pose p = from == 0 ? com.hexhaki.piece.sword.DrawView.charge(katana, false, t, 0.0F, 0.0F)
                        : back ? com.hexhaki.piece.sword.DrawView.backCharge(katana, false, t, free, 0.0F, 0.0F)
                        : com.hexhaki.piece.sword.DrawView.chargeDraw(katana, false, t, 0.0F, 0.0F);
                    if (p.blade() == null) {
                        lastBlade = null;
                        lastRight = null;
                        continue;
                    }
                    String at = (from == 0 ? "charge" : "charge from the saya") + " t" + t + " (" + katana + ")";
                    double[][] m = com.hexhaki.piece.sword.HolsterView.matrix(p.blade());
                    double[] way = {m[0][1], m[1][1], m[2][1]};
                    Check.that(Math.hypot(way[0], way[1]) / Math.sqrt(dot3(way, way)) >= 0.5, "the blade never points at the eye: " + at);
                    if (lastBlade != null) {
                        Check.that(turn(m, lastBlade) <= 26.0, "the blade turns at most 26 degrees a quarter tick: " + at + " (" + turn(m, lastBlade) + ")");
                    }
                    if (p.right() != null) {
                        double[][] r = com.hexhaki.piece.sword.HolsterView.matrix(p.right());
                        if (lastRight != null) {
                            Check.that(turn(r, lastRight) <= 26.0, "the right fist never spins: " + at + " (" + turn(r, lastRight) + ")");
                        }
                        lastRight = r;
                    }
                    lastBlade = m;
                }
            }
        }
        // Yoru, over the shoulder: the same limits on its turn
        Katana yoru = java.util.Arrays.stream(Katana.values()).filter(k -> k.carry() == Katana.Carry.BACK).findFirst().orElse(null);
        if (yoru != null) {
            int free = Math.max(0, com.hexhaki.piece.sword.BladePaths.drawTick(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.DRAW, yoru)));
            double[][] last = null;
            for (float t = free; t <= free + 8; t += 0.25F) {
                double[][] m = com.hexhaki.piece.sword.HolsterView.matrix(com.hexhaki.piece.sword.DrawView.backDraw(yoru, false, t, free, 0.0F, 0.0F).blade());
                if (last != null) {
                    Check.that(turn(m, last) <= 26.0, "Yoru turns at most 26 degrees a quarter tick (t" + t + ": " + turn(m, last) + ")");
                }
                last = m;
            }
        }
    }

    /**
     * 2.6.1 (the owner: "Haki coating is ONLY on the blade of the swords, never on the handle or saya", "progressive not instant (like
     * the arms slowly turning black)"): the blade's coat waits for the fist, runs up from the guard to the point over about a second,
     * a point of the steel at a time, and draws back from the point.
     */
    static void bladeCoat() {
        Check.near(com.hexhaki.piece.sword.BladeCoat.progress(true, 0L, 0.0F), 0.0, 1e-6, "not at once");
        Check.near(com.hexhaki.piece.sword.BladeCoat.progress(true, com.hexhaki.piece.sword.BladeCoat.DELAY_NANOS, 0.0F), 0.0, 1e-6,
            "the fist first: the blade waits");
        Check.near(com.hexhaki.piece.sword.BladeCoat.progress(true, com.hexhaki.piece.sword.BladeCoat.DELAY_NANOS
            + com.hexhaki.piece.sword.BladeCoat.ON_NANOS, 0.0F), 1.0, 1e-6, "all black after the delay and its run");
        Check.that(com.hexhaki.piece.sword.BladeCoat.progress(true, 0L, 0.4F) > 0.39F, "turned round part way: no new wait");
        Check.near(com.hexhaki.piece.sword.BladeCoat.progress(false, com.hexhaki.piece.sword.BladeCoat.OFF_NANOS, 1.0F), 0.0, 1e-6,
            "off: drawn back within its time");
        float last = -1;
        for (long ms = 0; ms <= 1600; ms += 50) {
            float p = com.hexhaki.piece.sword.BladeCoat.progress(true, ms * 1_000_000L, 0.0F);
            Check.that(p >= last, "the coat only spreads while on (" + ms + " ms)");
            last = p;
        }
        int blackGuard = 0;
        int blackPoint = 0;
        for (int seed = 0; seed < 400; seed++) {
            Check.near(com.hexhaki.piece.sword.BladeCoat.at(0.5F, seed, 0.0F), 0.0, 1e-6, "bare at 0");
            Check.near(com.hexhaki.piece.sword.BladeCoat.at(0.5F, seed, 1.0F), 1.0, 1e-6, "black at 1");
            float before = 0;
            for (float p = 0; p <= 1.0001F; p += 0.02F) {
                float at = com.hexhaki.piece.sword.BladeCoat.at(0.3F, seed, p);
                Check.that(at >= before - 1e-6, "a point of the steel only darkens as the coat spreads");
                before = at;
            }
            blackGuard += com.hexhaki.piece.sword.BladeCoat.at(0.05F, seed, 0.45F) > 0.99F ? 1 : 0;
            blackPoint += com.hexhaki.piece.sword.BladeCoat.at(0.95F, seed, 0.45F) > 0.01F ? 1 : 0;
        }
        Check.that(blackGuard == 400 && blackPoint == 0, "half way: black at the guard, bare at the point (" + blackGuard + ", " + blackPoint + ")");
        // out of step: the same height turns at different moments, point by point
        java.util.Set<Integer> moments = new java.util.HashSet<>();
        for (int seed = 0; seed < 50; seed++) {
            moments.add(Math.round(com.hexhaki.piece.sword.BladeCoat.reveal(0.5F, seed) * 100));
        }
        Check.that(moments.size() > 5, "the front is ragged, not a line (" + moments.size() + " moments)");
    }

    /** The angle (degrees) between two frames' turns (their 3x3 parts, columns scaled out). */
    static double turn(double[][] a, double[][] b) {
        double[][] na = new double[3][3];
        double[][] nb = new double[3][3];
        for (int j = 0; j < 3; j++) {
            double la = Math.sqrt(a[0][j] * a[0][j] + a[1][j] * a[1][j] + a[2][j] * a[2][j]);
            double lb = Math.sqrt(b[0][j] * b[0][j] + b[1][j] * b[1][j] + b[2][j] * b[2][j]);
            for (int i = 0; i < 3; i++) {
                na[i][j] = a[i][j] / la;
                nb[i][j] = b[i][j] / lb;
            }
        }
        double trace = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                trace += na[i][j] * nb[i][j];
            }
        }
        return Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, (trace - 1.0) / 2.0))));
    }

    static boolean same(float[][] a, float[][] b) {
        if (a == null || b == null) {
            return a == b;
        }
        double[][] ma = com.hexhaki.piece.sword.HolsterView.matrix(a);
        double[][] mb = com.hexhaki.piece.sword.HolsterView.matrix(b);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (Math.abs(ma[i][j] - mb[i][j]) > 2e-3) {
                    return false;
                }
            }
        }
        return true;
    }

    static double[] origin(float[][] steps) {
        return new double[] {steps[0][1], steps[0][2], steps[0][3]};
    }

    static double distance(double[] a, double[] b) {
        double[] d = sub3(a, b);
        return Math.sqrt(dot3(d, d));
    }
}
