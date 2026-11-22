package com.hexhaki.piece.hie;

/**
 * 3.1.9 (the owner: "Can you create the ice fruit, the Aokiji one?"): the Hie Hie no Mi's own sound events ({@code hexhaki:piece.hie.*},
 * the merged sounds.json; the clips in assets/hexhaki/sounds/piece/hie, every one a CC0 recording from Freesound.org, cut by
 * tools/hie_sounds.py, credited in its SOURCES.txt). Pure: names only (PieceSounds registers them; the frost is a loop the client plays
 * while ice gathers).
 */
public final class HieSounds {
    private HieSounds() {
    }

    /** A body frozen solid (an ice spell's freeze). */
    public static final String FREEZE = "piece.hie.freeze";
    /** Ice taking shape: spreading over the ground, a road, spears, the pheasant (ice growing, frost cracking). */
    public static final String FORM = "piece.hie.form";
    /** A frozen body's ice cracking before it breaks. */
    public static final String CRACK = "piece.hie.crack";
    /** Ice shattering. */
    public static final String SHATTER = "piece.hie.shatter";
    /** A spear, a fist or a beak of ice landing (a chill hit). */
    public static final String HIT = "piece.hie.hit";
    /** Ice Age's wave (an ice avalanche). */
    public static final String AGE = "piece.hie.age";
    /** Frost gathering: a loop while ice is held (the spears, the ball, the saber). */
    public static final String FROST = "piece.hie.frost";

    /** Every event and its clips (assets/hexhaki/sounds/piece/hie/{clip}.ogg). */
    public static final String[][] EVENTS = {
        {FREEZE, "freeze1", "freeze2"}, {FORM, "form1", "form2"}, {CRACK, "crack1", "crack2"}, {SHATTER, "shatter1", "shatter2"},
        {HIT, "hit1"}, {AGE, "age1"}, {FROST, "frost"},
    };
}
