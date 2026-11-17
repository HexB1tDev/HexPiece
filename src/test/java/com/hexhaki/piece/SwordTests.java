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
        koiguchi();
        bigSlash();
        wave();
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
        // 2.2.1 (the owner: "the sheath and unsheath is too fast ... Don't make the blade usable until it's fully unsheathed ... Unless
        // it's a quick draw"): a slow sheathing, a slow draw that ends drawn, both a second or so; the quick draw as fast as ever
        Check.that(SwordAnims.duration(SwordAnims.SHEATHE) >= 16 && SwordAnims.duration(SwordAnims.SHEATHE) <= 24, "a slow sheathing, about a second");
        Check.that(SwordAnims.duration(SwordAnims.DRAW) == SwordRules.DRAW_TICKS && SwordRules.DRAW_TICKS >= 16 && SwordRules.DRAW_TICKS <= 26,
            "a slow draw, about a second");
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
}
