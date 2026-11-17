package com.hexhaki.piece;

import com.hexhaki.piece.clash.ClashRules;
import com.hexhaki.piece.clash.ClashRules.Blow;
import com.hexhaki.piece.clash.ClashRules.Kind;

/** 2.3.3: blows meeting (the owner's clash rules: every move clashable, Armament needed against a blade, Ryou meeting Ryou bursts). */
public final class ClashTests {
    public static void main(String[] args) {
        rules();
        armament();
        throwing();
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
}
