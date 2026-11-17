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
        Check.that(Katana.YORU.ordinal() == 26 && Katana.GRYPHON.ordinal() == 25, "new blades at the end (saved by key, sent by ordinal)");
        for (int rank = 0; rank < 3; rank++) {
            for (int pick = 0; pick < 60; pick++) {
                Check.that(!SeaRules.katana(rank, pick).legend(), "no patrol carries a legendary blade");
            }
        }
        // 2.3.2: the animation names: Gryphon and Yoru each have every animation of their own
        Check.that("sword_draw_yoru".equals(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.DRAW, Katana.YORU))
            && "sword_draw_gryphon".equals(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.DRAW, Katana.GRYPHON))
            && "sword_guard_yoru".equals(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.GUARD, Katana.YORU))
            && "sword_draw".equals(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.DRAW, Katana.values()[0]))
            && "sword_kesa_gryphon".equals(com.hexhaki.piece.sword.SwordAnims.name(
                com.hexhaki.piece.sword.SwordAnims.of(com.hexhaki.piece.sword.SwordMove.KESA), Katana.GRYPHON)),
            "Gryphon's and Yoru's own animations, the katanas' theirs");
        for (String name : new String[] {"sword_draw_yoru", "sword_sheathe_yoru", "sword_charge_draw_yoru", "sword_draw_gryphon",
            "sword_sheathe_gryphon"}) {
            Check.that(com.hexhaki.piece.sword.BladePaths.of(name) != null, name + ": its blade path is written");
        }
        Check.that(com.hexhaki.piece.sword.BladePaths.drawTick("sword_draw_yoru") > 0
            && com.hexhaki.piece.sword.BladePaths.sheathTick("sword_sheathe_yoru") > 0, "Yoru's draw and sheathe hand it over mid-way");
        Check.that(com.hexhaki.piece.sword.BladePaths.notoOut("sword_draw_gryphon") != null
            && com.hexhaki.piece.sword.BladePaths.notoOut("sword_sheathe_gryphon") != null, "Gryphon's saber slides out of its own scabbard");
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
            && SwordRules.drawTicks(Katana.GRYPHON) > SwordRules.drawTicks(Katana.values()[0])
            && SwordRules.drawTicks(Katana.values()[0]) == SwordRules.DRAW_TICKS && SwordRules.drawTicks(null) == SwordRules.DRAW_TICKS,
            "Yoru draws slowest of all, then Gryphon, then a katana");
        Check.that(SwordRules.sheatheTicks(Katana.YORU) > SwordRules.sheatheTicks(Katana.GRYPHON)
            && SwordRules.sheatheTicks(Katana.GRYPHON) > SwordRules.SHEATHE_TICKS, "and sheathes likewise");
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
                if (name.equals(SwordAnims.name(anim) + SwordAnims.suffix(katana))) {
                    Check.that(length == SwordAnims.duration(anim, katana), name + ": as long as the server holds it");
                }
                float[] path = BladePaths.of(name);
                Check.that(path != null && path.length == (length * 2 + 1) * 6, name + ": its blade path, every half tick");
                if (katana.legend()) {
                    Check.that(name.endsWith(SwordAnims.suffix(katana)), name + ": the legend's own");
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
            double t = SwordRules.slashThickness(height);
            Check.that(t >= SwordRules.SLASH_THICKNESS - 1e-9 && t <= SwordRules.SLASH_THICKNESS_MAX + 1e-9, "thickness in range");
            double drop = SwordRules.dropChance(height);
            Check.that(drop > 0 && drop <= 1.0, "a cut block may drop");
        }
        Check.that(SwordRules.dropChance(64) <= 0.07, "a giant cut drops few items (vanilla's explosion rule)");
        Check.that(SwordRules.slashThickness(64) > SwordRules.slashThickness(2), "a taller slash is a wider gash");
        Check.that(SwordRules.NPC_SLASH_HEIGHT_MAX <= 8 && SwordRules.NPC_SLASH_RANGE_MAX <= 40, "a pirate captain's slash stays small");
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
            }
            com.hexhaki.piece.sword.SlashMesh.Frame frame = com.hexhaki.piece.sword.SlashMesh.Frame.of(new double[3], 0, 1, height, 1.0);
            for (double v = 0.0; v <= 1.0; v += 0.05) {
                Check.near(frame.across(v) * 2.0, SwordRules.slashThicknessAt(height, v), 1e-9, "the body is the trench's width " + v + " up");
                Check.that(frame.front(v) > frame.back(v), "the body has length along its flight " + v + " up");
            }
            Check.that(SwordRules.slashThicknessAt(height, 0.0) >= SwordRules.slashThicknessAt(height, 0.5)
                && SwordRules.slashThicknessAt(height, 0.5) >= SwordRules.slashThicknessAt(height, 0.95), "widest in the ground, thin at the peak");
        }
        Check.near(SwordRules.slashThickness(64), 2 * SwordRules.SLASH_SIDE * 64, 1e-9, "a full wave is 9 blocks thick in the ground");
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
        String[] sheathes = {"sword_sheathe", "sword_sheathe_gryphon", "sword_sheathe_yoru"};
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
            Check.that(all.contains(event.group(1)), event.group(1) + ": registered");
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
}
