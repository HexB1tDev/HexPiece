package com.hexhaki.piece;

import com.hexhaki.piece.clash.ClashBlows;
import com.hexhaki.piece.clash.ClashRules;
import com.hexhaki.piece.clash.ClashRules.Blow;
import com.hexhaki.piece.clash.ClashRules.Kind;
import com.hexhaki.piece.clash.ImpactFrame;
import com.hexhaki.piece.style.Hit;
import com.hexhaki.piece.style.StyleMove;
import com.hexhaki.piece.sword.SwordMove;
import com.hexhaki.piece.sword.SwordRules;

/** 2.3.3, 2.3.6: blows meeting (the owner's clash rules: every move clashable, Armament needed against a blade, Ryou meeting Ryou bursts). */
public final class ClashTests {
    public static void main(String[] args) throws Exception {
        rules();
        armament();
        throwing();
        windows();
        meeting();
        frame();
        burst();
        recoils();
        Check.done("ClashTests");
    }

    static Blow fist(boolean armed, int ryou) {
        return new Blow(Kind.FIST, 10, 16, 3.3, 5.0, armed, ryou);
    }

    static Blow blade(boolean armed, int ryou) {
        return new Blow(Kind.BLADE, 12, 18, 4.5, 7.0, armed, ryou);
    }

    static void rules() {
        Blow a = fist(false, 0);
        Blow b = new Blow(Kind.KICK, 11, 15, 3.4, 6.0, false, 0);
        Check.that(ClashRules.meets(a, b, 13, 3.0, true, true), "two bare blows on their way, facing, in reach, meet");
        Check.that(!ClashRules.meets(a, b, 9, 3.0, true, true), "not before either sets out");
        Check.that(!ClashRules.meets(a, b, 17, 3.0, true, true), "not after one has landed (and its late tick)");
        Check.that(ClashRules.meets(a, b, 16, 3.0, true, true), "a tick late still meets");
        Check.that(!ClashRules.meets(a, b, 13, 3.0, true, false), "not when one looks away");
        Check.that(!ClashRules.meets(a, b, 13, 5.0, true, true), "not out of reach");
        Check.that(ClashRules.faces(1, 0, 3, 1), "looking at someone a little off the line faces them");
        Check.that(!ClashRules.faces(1, 0, -3, 0), "looking away does not");
        Check.that(!ClashRules.faces(0, 0, 3, 0), "looking straight up or down faces no one");
        Check.that(ClashRules.burst(fist(true, 0xFF33AAFF), blade(true, 0xFFFF3333)), "Ryou meeting Ryou bursts");
        Check.that(!ClashRules.burst(fist(true, 0xFF33AAFF), blade(true, 0)), "Ryou meeting plain Armament does not");
        Check.that(ClashRules.frameTicks(ClashRules.LEAST) == 2 && ClashRules.frameTicks(ClashRules.MOST) == 4,
            "the impact frame holds 2 ticks for a small clash, 4 for the hardest");
    }

    static void armament() {
        Check.that(!ClashRules.clashable(fist(false, 0), blade(false, 0)), "a bare fist cannot clash a blade");
        Check.that(ClashRules.bladeWins(fist(false, 0), blade(false, 0)), "the blade comes through a bare fist");
        Check.that(ClashRules.bladeWins(blade(true, 0), fist(false, 0)), "either way round");
        Check.that(ClashRules.clashable(fist(true, 0), blade(false, 0)), "with Armament on, the fist clashes the blade");
        Check.that(!ClashRules.bladeWins(fist(true, 0), blade(false, 0)), "and the blade does not simply win");
        Check.that(ClashRules.clashable(blade(false, 0), blade(false, 0)), "steel meets steel without Haki");
        Check.that(ClashRules.clashable(fist(false, 0), fist(false, 0)), "two bare fists always clash");
        Check.that(!ClashRules.meets(fist(false, 0), blade(true, 0), 14, 3.0, true, true), "the blade's own Armament does not help the fist");
    }

    static void throwing() {
        float even = ClashRules.power(5.0, 5.0);
        Check.near(even, ClashRules.ORDINARY, 1e-6, "two ordinary blows make an ordinary clash");
        Check.that(ClashRules.power(0, 0) >= ClashRules.LEAST && ClashRules.power(500, 500) <= ClashRules.MOST, "power stays in range");
        Check.that(!ClashRules.quakes(even) && ClashRules.quakes(ClashRules.power(20, 20)), "only hard clashes shake the ground");
        double a = ClashRules.thrown(even, 1.0);
        Check.near(a, ClashRules.EVEN_PUSH, 1e-9, "an even clash throws both the even push");
        Check.that(ClashRules.thrown(even, 0.5) == 0.0, "twice as strong stands its ground");
        Check.that(ClashRules.thrown(even, 1.5) > a && ClashRules.thrown(even, 0.8) < a, "the weaker is thrown further, the stronger less");
        Check.that(ClashRules.overwhelmed(2.0) && !ClashRules.overwhelmed(1.9), "out-hit twice over, the weaker is flung");
        Check.that(ClashRules.stagger(3.0) > ClashRules.stagger(1.0), "and stunned longer");
        Check.that(ClashRules.lifted(1.0) > 0 && ClashRules.lifted(0.4) == 0.0, "lifted with the throw, not the one standing");
        Check.near(ClashRules.ratio(4.0, 8.0), 2.0, 1e-9, "the ratio is the other's over one's own");
    }

    /** 2.3.6: every move's blows, tick by tick. */
    static void windows() {
        Blow jab = new Blow(Kind.FIST, 0, 20, 3.0, 5.0, false, 0);
        Check.that(!ClashRules.onItsWay(jab, 19 - ClashRules.WINDUP) && ClashRules.onItsWay(jab, 20 - ClashRules.WINDUP),
            "a long wind-up is not coming until its last ticks");
        Check.that(ClashRules.WINDUP <= 4, "two blows meet only when both strike at about the same moment");
        for (StyleMove move : StyleMove.values()) {
            Hit[] hits = move.hits();
            for (int tick = 0; tick < move.duration(); tick++) {
                ClashBlows.Window w = ClashBlows.style(move, tick);
                Hit next = null;
                for (Hit hit : hits) {
                    if (tick <= hit.tick() + ClashRules.LATE) {
                        next = hit;
                        break;
                    }
                }
                if (next == null) {
                    Check.that(w == null, move + " has nothing on its way after its last blow (t" + tick + ")");
                    continue;
                }
                Check.that(w != null && w.lands() == next.tick(), move + " t" + tick + ": the next blow is the one on its way");
                Check.that(w.began() <= w.lands() && w.began() >= 0, move + " t" + tick + ": it sets out before it lands");
                Check.that(w.shot() == next.line() && (!w.shot() || w.began() == w.lands()), move + " t" + tick + ": a shot is on its way only as it flies");
                Check.that(w.kind() != Kind.BLADE, move + ": a style blow is bare");
                Check.near(w.reach(), next.reach(), 1e-9, move + ": as far as the blow reaches");
            }
        }
        Check.that(ClashBlows.style(StyleMove.SP_IRON, 5) == null, "Iron Body throws no blow to clash");
        Check.that(ClashBlows.kind(StyleMove.BL_SNAP) == Kind.KICK && ClashBlows.kind(StyleMove.SP_FINGER_R) == Kind.FIST, "kicks and fists");
        ClashBlows.Window hachis = ClashBlows.style(StyleMove.BL_HACHIS, 11);
        Check.that(hachis.began() == 10 && hachis.lands() == 11, "each kick of Premier Hachis sets out after the one before");
        for (SwordMove move : SwordMove.values()) {
            SwordMove.Cut[] cuts = move.cuts();
            for (int tick = 0; tick < move.duration(); tick++) {
                ClashBlows.Window w = ClashBlows.sword(move, tick, 4.0);
                boolean any = tick <= cuts[cuts.length - 1].to() + ClashRules.LATE;
                Check.that((w != null) == any, move + " t" + tick + ": a cut is on its way until the last one has passed");
                if (w != null) {
                    Check.that(w.kind() == Kind.BLADE && w.lands() >= w.began(), move + ": a blade's blow");
                }
            }
            double reach = ClashBlows.bladeReach(move.animation(), cuts[0].from() - 1, cuts[cuts.length - 1].to(), SwordRules.EXTEND, -1);
            Check.that(reach > 1.4 && reach < 4.5, move + ": the blade reaches across the ground as a blade does (" + reach + ")");
        }
        Check.that(ClashBlows.bladeReach("no such animation", 0, 5, 0.8, 2.5) == 2.5, "no path: the fallback reach");
        Check.that(ClashBlows.release(3, 3.0, 9.0) != null && ClashBlows.release(5, 3.0, 9.0) == null, "the charged cut is one blow");
        Check.that(ClashBlows.quick(8, 0.0, 9.0).reach() == ClashBlows.QUICK_REACH, "the quick draw is one blow along its run");
        Blow at = ClashBlows.style(StyleMove.SP_FINGER_R, 0).at(100, 4.0, true, 0xFF3366FF);
        Check.that(at.began() == 100 && at.lands() == 103 && at.armed() && at.ryou() == 0xFF3366FF && at.hit() == 4.0,
            "a window placed in the world at its move's start");
    }

    /** 2.3.6: where two blows meet. */
    static void meeting() {
        Blow fist = new Blow(Kind.FIST, 0, 5, 3.3, 5.0, true, 0);
        Blow sword = new Blow(Kind.BLADE, 0, 5, 4.6, 6.0, false, 0);
        Check.near(ClashRules.meeting(fist, fist, 3.0), 1.5, 1e-9, "two fists in reach meet in the middle");
        Check.near(ClashRules.meeting(sword, fist, 5.0), 2.5, 1e-9, "a fist and a blade both reaching the middle meet there");
        Check.near(ClashRules.meeting(sword, fist, 7.0), 3.7, 1e-9, "a fist meets the longer blade where the fist reaches");
        Check.near(ClashRules.meeting(fist, sword, 7.0), 3.3, 1e-9, "the same from the fist's side");
        Blow shot = new Blow(Kind.FIST, 5, 5, 18.0, 3.5, false, 0);
        Check.near(ClashRules.meeting(shot, fist, 12.0), 8.7, 1e-9, "a shot meets a fist at the fist");
        Check.near(ClashRules.meeting(shot, shot, 12.0), 6.0, 1e-9, "two shots meet in the middle");
        Blow little = new Blow(Kind.FIST, 0, 5, 1.0, 5.0, false, 0);
        double m = ClashRules.meeting(little, little, 3.0);
        Check.that(m >= 0.0 && m <= 3.0, "out of each other's reach, still somewhere between them");
    }

    /** 2.3.6: the impact frame's beats, on screen. */
    static void frame() {
        for (float power : new float[] {ClashRules.LEAST, ClashRules.ORDINARY, 2.0F, ClashRules.MOST}) {
            int ticks = ClashRules.frameTicks(power);
            Check.that(ImpactFrame.at(-0.1, 1280, 720, 640, 360, power, 0, 0, 7L).isEmpty(), "nothing before the clash");
            Check.that(ImpactFrame.at(ImpactFrame.length(power), 1280, 720, 640, 360, power, 0, 0, 7L).isEmpty(), "nothing once it is over");
            java.util.List<ImpactFrame.Batch> negative = ImpactFrame.at(0.0, 1280, 720, 640, 360, power, 0, 0, 7L);
            Check.that(negative.get(0).mode() == ImpactFrame.INVERT && negative.get(1).mode() == ImpactFrame.CONTRAST,
                "it opens on the view turned over, its contrast pushed");
            java.util.List<ImpactFrame.Batch> black = ImpactFrame.at(ticks * 0.6, 1280, 720, 640, 360, power, 0, 0, 7L);
            Check.that(black.stream().noneMatch(b -> b.mode() != ImpactFrame.BLEND) && (black.get(0).argb()[0] >>> 24) > 0xE0
                && (black.get(0).argb()[0] & 0xFFFFFF) < 0x101010, "then the black frame");
            java.util.List<ImpactFrame.Batch> release = ImpactFrame.at(ticks + 0.5, 1280, 720, 640, 360, power, 0, 0, 7L);
            Check.that(release.stream().allMatch(b -> b.mode() == ImpactFrame.BLEND) && (release.get(0).argb()[0] >>> 24) < 0x80,
                "then the view back, a fading flash");
            for (double age = 0; age < ImpactFrame.length(power); age += 0.25) {
                for (ImpactFrame.Batch b : ImpactFrame.at(age, 1280, 720, 300, 200, power, 0xFF2244, 0x22AAFF, 9L)) {
                    Check.that(b.count() % 3 == 0 && b.xy().length == b.count() * 2, "whole triangles");
                }
            }
        }
        boolean coloured = false;
        for (ImpactFrame.Batch b : ImpactFrame.at(2.0, 1280, 720, 640, 360, ClashRules.MOST, 0xFF2244, 0x22AAFF, 7L)) {
            for (int c : b.argb()) {
                coloured |= (c & 0xFFFFFF) == 0xFF2244 || (c & 0xFFFFFF) == 0x22AAFF;
            }
        }
        Check.that(coloured, "two Ryou users: the black frame's rays in both their colours");
        Check.that(!java.util.Arrays.equals(ImpactFrame.at(0.2, 1280, 720, 640, 360, 1.2F, 0, 0, 7L).get(2).xy(),
            ImpactFrame.at(0.6, 1280, 720, 640, 360, 1.2F, 0, 0, 7L).get(2).xy()), "the lines drawn afresh every half tick");
        Check.that(java.util.Arrays.equals(ImpactFrame.at(0.1, 1280, 720, 640, 360, 1.2F, 0, 0, 7L).get(2).xy(),
            ImpactFrame.at(0.3, 1280, 720, 640, 360, 1.2F, 0, 0, 7L).get(2).xy()), "and held through it");
        double[] eye = {0, 0, 0};
        double[] look = {0, 0, 1};
        double[] up = {0, 1, 0};
        double[] left = {1, 0, 0};
        double[] ahead = ImpactFrame.onScreen(new double[] {0, 0, 5}, eye, look, up, left, 70, 1280, 720);
        Check.near(ahead[0], 640, 1e-6, "straight ahead is the middle of the view (x)");
        Check.near(ahead[1], 360, 1e-6, "and (y)");
        double[] leftward = ImpactFrame.onScreen(new double[] {1, 1, 5}, eye, look, up, left, 70, 1280, 720);
        Check.that(leftward[0] < 640 && leftward[1] < 360 && leftward[2] == 1.0, "up and to the left is up and to the left on screen");
        double[] behind = ImpactFrame.onScreen(new double[] {0, 0, -5}, eye, look, up, left, 70, 1280, 720);
        Check.that(behind[2] == 0.0 && behind[0] == 640 && behind[1] == 360, "behind the eye: the middle of the view");
    }

    /** 2.3.6: two Ryou users' colours tearing out in a sphere. */
    static void burst() {
        java.util.List<com.hexhaki.piece.haki.Bolt> bolts = com.hexhaki.piece.haki.ClashBurst.bolts(1.6F, 0xFF2244, 0x22AAFF, 1.2, 42L);
        int count = com.hexhaki.piece.haki.ClashBurst.count(1.6F);
        Check.that(bolts.size() == count * 2, "each strand its black body and its rim");
        double reach = com.hexhaki.piece.haki.ClashBurst.reach(1.6F);
        Check.that(reach < com.hexhaki.piece.haki.ConquerorStorm.reach(0) / 4, "much smaller than the Conqueror's storm");
        boolean up = false;
        boolean down = false;
        for (int i = 0; i < bolts.size(); i++) {
            com.hexhaki.piece.haki.Bolt bolt = bolts.get(i);
            int strand = i / 2;
            int rim = (strand % 2 == 0 ? 0xFF2244 : 0x22AAFF);
            if (bolt.kind == com.hexhaki.piece.haki.Bolt.EDGE) {
                Check.that((bolt.color & 0xFFFFFF) == rim, "the strands in the two colours by turns");
            } else {
                Check.that(bolt.kind == com.hexhaki.piece.haki.Bolt.BODY && (bolt.color & 0xFFFFFF) == com.hexhaki.piece.haki.Bolt.body(rim),
                    "a black body with its colour's hint");
            }
            for (int k = 0; k < bolt.points.length; k += 3) {
                double r = Math.sqrt(bolt.points[k] * bolt.points[k] + bolt.points[k + 1] * bolt.points[k + 1] + bolt.points[k + 2] * bolt.points[k + 2]);
                Check.that(r <= reach + 3.0, "within its reach");
                Check.that(bolt.points[k + 1] >= -1.2, "never under the ground");
                up |= bolt.points[k + 1] > 2.0;
                down |= bolt.points[k + 1] < -1.0;
            }
        }
        Check.that(up && down, "out in every direction: up into the air and down onto the ground");
        Check.that(com.hexhaki.piece.haki.ClashBurst.bolts(1.6F, 0xFF2244, 0x22AAFF, 1.2, 42L).get(3).points[4]
            == bolts.get(3).points[4], "the same clash, the same bolts");
    }

    /** 2.3.6: the recoils, the sound. */
    static void recoils() throws Exception {
        java.nio.file.Path anims = java.nio.file.Path.of("src/main/resources/assets/hexhaki/player_animation");
        Check.that(ClashRules.RECOIL == com.hexhaki.piece.sword.SwordAnims.CLASH_TICKS
            && com.hexhaki.piece.sword.SwordAnims.duration(com.hexhaki.piece.sword.SwordAnims.CLASH) == ClashRules.RECOIL,
            "a blade's recoil lasts as long as a fist's");
        Check.that(ClashRules.RECOIL <= ClashRules.STAGGER, "the recoil is over by the end of the stagger");
        for (com.hexhaki.piece.style.FightingStyle style : com.hexhaki.piece.style.FightingStyle.values()) {
            if (!style.choice() || style.sword() || !style.armed()) {
                continue; // 3.5.7: Iron Fist throws no blow to clash until it is remade
            }
            java.nio.file.Path file = anims.resolve(ClashRules.recoil(style) + ".json");
            Check.that(java.nio.file.Files.exists(file), style + ": its recoil");
            String json = java.nio.file.Files.readString(file);
            Check.that(json.contains("\"endTick\": " + ClashRules.RECOIL), style + ": its recoil lasts the stagger");
        }
        Check.that("sword_clash".equals(com.hexhaki.piece.sword.SwordAnims.name(com.hexhaki.piece.sword.SwordAnims.CLASH)),
            "a blade's recoil");
        java.nio.file.Path dir = java.nio.file.Path.of("src/main/resources/assets/hexhaki/sounds/piece/clash");
        String json = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/assets/hexhaki/sounds.json"));
        String lang = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/assets/hexhaki/lang/en_us.json"));
        String sources = java.nio.file.Files.readString(dir.resolve("SOURCES.txt"));
        Check.that(json.contains("\"" + ClashRules.SOUND + "\": {") && lang.contains("\"subtitles.hexhaki." + ClashRules.SOUND + "\""),
            "the clash's sound event and its subtitle");
        for (int i = 1; i <= 4; i++) {
            java.nio.file.Path file = dir.resolve("impact" + i + ".ogg");
            byte[] data = java.nio.file.Files.readAllBytes(file);
            Check.that(json.contains("\"hexhaki:piece/clash/impact" + i + "\""), file.getFileName() + ": played");
            Check.that(data.length > 4 && data[0] == 'O' && data[1] == 'g' && data[2] == 'g' && data[3] == 'S', file.getFileName() + ": Ogg");
            Check.that(sources.contains("impact" + i + ".ogg: "), file.getFileName() + ": credited");
        }
    }
}
